package org.opentrafficsim.road.gtu.lane.perception;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.HeadwayGTUType;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;

/**
 * Iterable to find upstream GTU's.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class UpstreamNeighborsIterable extends AbstractPerceptionIterable<HeadwayGTU, LaneBasedGTU, Integer>
{

    /** Margin in case of a left lane. */
    private static final Length LEFT = Length.instantiateSI(0.000001);

    /** Margin in case of a right lane. */
    private static final Length RIGHT = Length.instantiateSI(-0.000001);

    /** Headway GTU type that should be used. */
    private final HeadwayGTUType headwayGtuType;

    /**
     * Margin used for neighbor search in some cases to prevent possible deadlock. This does not affect calculated distances to
     * neighbors, but only whether they are considered a leader or follower.
     */
    private final Length margin;

    /**
     * Constructor.
     * @param perceivingGtu LaneBasedGtu; perceiving GTU
     * @param root LaneRecord&lt;?&gt;; root record
     * @param initialPosition Length; position on the root record
     * @param maxDistance Length; maximum distance to search
     * @param relativePosition RelativePosition; position to which distance are calculated by subclasses
     * @param headwayGtuType HeadwayGTUType; type of HeadwayGTU to return
     * @param lane RelativeLane; relative lane (used for a left/right distinction to prevent dead-locks)
     */
    public UpstreamNeighborsIterable(final LaneBasedGTU perceivingGtu, final LaneRecord<?> root, final Length initialPosition,
            final Length maxDistance, final RelativePosition relativePosition, final HeadwayGTUType headwayGtuType,
            final RelativeLane lane)
    {
        super(perceivingGtu, root, initialPosition, false, maxDistance, relativePosition, null);
        this.headwayGtuType = headwayGtuType;
        this.margin = lane.getLateralDirectionality().isLeft() ? LEFT : RIGHT;
    }

    /** {@inheritDoc} */
    @Override
    protected Entry getNext(final LaneRecord<?> record, final Length position, final Integer counter) throws GtuException
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
            throws GtuException, ParameterException
    {
        return this.headwayGtuType.createUpstreamGtu(perceivingGtu, object, distance);
    }

}
