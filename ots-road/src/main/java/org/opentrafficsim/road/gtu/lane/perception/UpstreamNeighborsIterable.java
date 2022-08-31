package org.opentrafficsim.road.gtu.lane.perception;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.HeadwayGtuType;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;

/**
 * Iterable to find upstream GTU's.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 19 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class UpstreamNeighborsIterable extends AbstractPerceptionIterable<HeadwayGTU, LaneBasedGTU, Integer>
{

    /** Margin in case of a left lane. */
    private static final Length LEFT = Length.instantiateSI(0.000001);

    /** Margin in case of a right lane. */
    private static final Length RIGHT = Length.instantiateSI(-0.000001);

    /** Headway GTU type that should be used. */
    private final HeadwayGtuType headwayGtuType;

    /**
     * Margin used for neighbor search in some cases to prevent possible deadlock. This does not affect calculated distances to
     * neighbors, but only whether they are considered a leader or follower.
     */
    private final Length margin;

    /**
     * Constructor.
     * @param perceivingGtu LaneBasedGTU; perceiving GTU
     * @param root LaneRecord&lt;?&gt;; root record
     * @param initialPosition Length; position on the root record
     * @param maxDistance Length; maximum distance to search
     * @param relativePosition RelativePosition; position to which distance are calculated by subclasses
     * @param headwayGtuType HeadwayGtuType; type of HeadwayGTU to return
     * @param lane RelativeLane; relative lane (used for a left/right distinction to prevent dead-locks)
     */
    public UpstreamNeighborsIterable(final LaneBasedGTU perceivingGtu, final LaneRecord<?> root, final Length initialPosition,
            final Length maxDistance, final RelativePosition relativePosition, final HeadwayGtuType headwayGtuType,
            final RelativeLane lane)
    {
        super(perceivingGtu, root, initialPosition, false, maxDistance, relativePosition, null);
        this.headwayGtuType = headwayGtuType;
        this.margin = lane.getLateralDirectionality().isLeft() ? LEFT : RIGHT;
    }

    /** {@inheritDoc} */
    @Override
    protected Entry getNext(final LaneRecord<?> record, final Length position, final Integer counter) throws GTUException
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
                        record.getLane().getParentLink().getSimulator().getSimulatorAbsTime());
            }
            if (next == null)
            {
                return null;
            }
            n = record.getLane().indexOfGtu(next);
            pos = next.position(record.getLane(), next.getFront());

            if (this.getGtu() != null && next.getId().equals(this.getGtu().getId()))
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
        return new Entry(next, n, pos);
    }

    /** {@inheritDoc} */
    @Override
    protected Length getDistance(final LaneBasedGTU object, final LaneRecord<?> record, final Length position)
    {
        return record.getDistanceToPosition(position).neg().plus(getDx());
    }

    /** {@inheritDoc} */
    @Override
    public HeadwayGTU perceive(final LaneBasedGTU perceivingGtu, final LaneBasedGTU object, final Length distance)
            throws GTUException, ParameterException
    {
        return this.headwayGtuType.createUpstreamGtu(perceivingGtu, object, distance);
    }

}
