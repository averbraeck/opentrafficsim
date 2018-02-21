package org.opentrafficsim.road.gtu.lane.perception;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.categories.HeadwayGtuType;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;

/**
 * Iterable to find upstream GTU's.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 19 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <R> record type
 */
public class UpstreamNeighborsIterable<R extends LaneRecord<R>> extends AbstractPerceptionIterable<HeadwayGTU, R, Integer>
{

    /** Margin in case of a left lane. */
    private final static Length LEFT = Length.createSI(0.000001);

    /** Margin in case of a right lane. */
    private final static Length RIGHT = Length.createSI(-0.000001);

    /** Headway GTU type that should be used. */
    private final HeadwayGtuType headwayGtuType;

    /** GTU. */
    private final GTU gtu;

    /**
     * Margin used for neighbor search in some cases to prevent possible deadlock. This does not affect calculated distances to
     * neighbors, but only whether they are considered a leader or follower.
     */
    private final Length margin;

    /**
     * Constructor.
     * @param root R; root record
     * @param initialPosition Length; position on the root record
     * @param maxDistance Length; maximum distance to search
     * @param relativePosition RelativePosition; position to which distance are calculated by subclasses 
     * @param headwayGtuType HeadwayGtuType; type of HeadwayGTU to return
     * @param gtu GTU; the GTU, may be {@code null}
     * @param lane RelativeLane; relative lane (used for a left/right distinction to prevent dead-locks)
     */
    public UpstreamNeighborsIterable(final R root, final Length initialPosition, final Length maxDistance,
            final RelativePosition relativePosition, final HeadwayGtuType headwayGtuType, final GTU gtu,
            final RelativeLane lane)
    {
        super(root, initialPosition, false, maxDistance, relativePosition, null);
        this.headwayGtuType = headwayGtuType;
        this.gtu = gtu;
        this.margin = lane.getLateralDirectionality().isLeft() ? LEFT : RIGHT;
    }

    /** {@inheritDoc} */
    @Override
    protected Entry getNext(final R record, final Length position, final Integer counter) throws GTUException
    {
        int n;
        LaneBasedGTU next;
        Length pos;
        boolean plus = record.getDirection().isPlus();
        if (counter == null)
        {
            if (plus ? position.ge(record.getLane().getLength()) : position.eq0())
            {
                next = record.getLane().getLastGtu(record.getDirection());
            }
            else
            {
                Length searchPos = (plus ? position.plus(this.margin) : position.minus(this.margin));
                next = record.getLane().getGtuBehind(searchPos, record.getDirection(), RelativePosition.FRONT,
                        record.getLane().getParentLink().getSimulator().getSimulatorTime().getTime());
            }
            if (next == null)
            {
                return null;
            }
            n = record.getLane().indexOfGtu(next);
            pos = next.position(record.getLane(), next.getFront());

            if (this.gtu != null && next.getId().equals(this.gtu.getId()))
            {
                // ignore self
                pos = plus ? pos.minus(next.getLength()) : pos.plus(next.getLength());
                return getNext(record, pos, n);
            }
        }
        else
        {
            n = plus ? counter - 1 : counter + 1;
            if (n < 0 || n >= record.getLane().numberOfGtus())
            {
                return null;
            }
            next = record.getLane().getGtu(n);
            pos = next.position(record.getLane(), next.getFront());
        }
        Length distance = record.getDistanceToPosition(pos).neg().plus(getDx());
        pos = plus ? pos.minus(next.getLength()) : pos.plus(next.getLength());
        return new Entry(this.headwayGtuType.createHeadwayGtu(next, distance), n, pos);
    }

}
