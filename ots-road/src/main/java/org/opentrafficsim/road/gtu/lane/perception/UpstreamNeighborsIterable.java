package org.opentrafficsim.road.gtu.lane.perception;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.HeadwayGtuType;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.perception.structure.LaneRecordInterface;

/**
 * Iterable to find upstream GTU's.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class UpstreamNeighborsIterable extends AbstractPerceptionIterable<HeadwayGtu, LaneBasedGtu, Integer>
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
     * @param perceivingGtu perceiving GTU
     * @param root root record
     * @param initialPosition position on the root record
     * @param maxDistance maximum distance to search
     * @param relativePosition position to which distance are calculated by subclasses
     * @param headwayGtuType type of HeadwayGtu to return
     * @param lane relative lane (used for a left/right distinction to prevent dead-locks)
     */
    public UpstreamNeighborsIterable(final LaneBasedGtu perceivingGtu, final LaneRecordInterface<?> root,
            final Length initialPosition, final Length maxDistance, final RelativePosition relativePosition,
            final HeadwayGtuType headwayGtuType, final RelativeLane lane)
    {
        super(perceivingGtu, root, initialPosition, false, maxDistance, relativePosition, null);
        this.headwayGtuType = headwayGtuType;
        this.margin = lane.getLateralDirectionality().isLeft() ? LEFT : RIGHT;
    }

    /** {@inheritDoc} */
    @Override
    protected Entry getNext(final LaneRecordInterface<?> record, final Length position, final Integer counter)
            throws GtuException
    {
        int n;
        LaneBasedGtu next;
        Length pos;
        if (counter == null)
        {
            if (position.ge(record.getLane().getLength()))
            {
                next = record.getLane().getLastGtu();
            }
            else
            {
                Length searchPos = position.plus(this.margin);
                next = record.getLane().getGtuBehind(searchPos, RelativePosition.FRONT,
                        record.getLane().getLink().getSimulator().getSimulatorAbsTime());
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
                pos = pos.minus(next.getLength());
                return getNext(record, pos, n);
            }
        }
        else
        {
            n = counter - 1;
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
    protected Length getDistance(final LaneBasedGtu object, final LaneRecordInterface<?> record, final Length position)
    {
        return record.getDistanceToPosition(position).neg().plus(getDx());
    }

    /** {@inheritDoc} */
    @Override
    public HeadwayGtu perceive(final LaneBasedGtu perceivingGtu, final LaneBasedGtu object, final Length distance)
            throws GtuException, ParameterException
    {
        return this.headwayGtuType.createUpstreamGtu(perceivingGtu, object, distance);
    }

}
