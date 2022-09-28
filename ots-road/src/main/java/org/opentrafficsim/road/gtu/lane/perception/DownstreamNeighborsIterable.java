package org.opentrafficsim.road.gtu.lane.perception;

import java.util.LinkedHashSet;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.HeadwayGtuType;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;

/**
 * Iterable to find downstream GTU's.<br>
 * <br>
 * The behavior of this search is slightly altered using {@code boolean ignoreIfUpstream}. This is to deal with the following
 * situations in case a GTU with it's rear upstream of the considered lane is found:<br>
 * <br>
 * Following downstream GTUs ({@code ignoreIfUpstream = true})
 * <ol>
 * <li>From the same direction (or not a merge): the GTU can be ignored as it is also found on the upstream lane.</li>
 * <li>From the other direction of a merge: the GTU can be ignored as it is followed through considering the merge conflict.
 * Note that we cannot follow the GTU in a regular fashion. If the rear of the GTU is upstream of the conflict, the subject GTU
 * can move up to the conflict without hitting the GTU from the other direction. Considering the GTU through the conflict deals
 * with this, and the GTU can be ignored for regular following of downstream GTUs.</li>
 * </ol>
 * <br>
 * GTUs downstream of a conflict ({@code ignoreIfUpstream = false})
 * <ol>
 * <li>From the same direction: the GTU is considered both through the conflict and as a regular downstream GTU.</li>
 * <li>From the other direction of a merge: the GTU needs to be considered.</li>
 * </ol>
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 19 feb. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class DownstreamNeighborsIterable extends AbstractPerceptionIterable<HeadwayGTU, LaneBasedGTU, Integer>
{

    /** Margin in case of a left lane. */
    private static final Length LEFT = Length.instantiateSI(-0.000001);

    /** Margin in case of a right lane. */
    private static final Length RIGHT = Length.instantiateSI(0.000001);

    /** Headway GTU type that should be used. */
    private final HeadwayGtuType headwayGtuType;

    /** Added GTU's so far. */
    private final Set<String> ids = new LinkedHashSet<>();

    /**
     * Margin used for neighbor search in some cases to prevent possible deadlock. This does not affect calculated distances to
     * neighbors, but only whether they are considered a leader or follower.
     */
    private final Length margin;

    /** Ignore downstream GTU's if their rear is upstream of the lane start. Note that equal GTU id's are always ignored. */
    private final boolean ignoreIfUpstream;

    /**
     * Constructor.
     * @param perceivingGtu LaneBasedGTU; perceiving GTU
     * @param root LaneRecord&lt;?&gt;; root record
     * @param initialPosition Length; position on the root record
     * @param maxDistance Length; maximum distance to search
     * @param relativePosition RelativePosition; position to which distance are calculated by subclasses
     * @param headwayGtuType HeadwayGtuType; type of HeadwayGTU to return
     * @param lane RelativeLane; relative lane (used for a left/right distinction to prevent dead-locks)
     * @param ignoreIfUpstream boolean; whether to ignore GTU that are partially upstream of a record
     */
    public DownstreamNeighborsIterable(final LaneBasedGTU perceivingGtu, final LaneRecord<?> root, final Length initialPosition,
            final Length maxDistance, final RelativePosition relativePosition, final HeadwayGtuType headwayGtuType,
            final RelativeLane lane, final boolean ignoreIfUpstream)
    {
        super(perceivingGtu, root, initialPosition, true, maxDistance, relativePosition, null);
        this.headwayGtuType = headwayGtuType;
        this.margin = lane.getLateralDirectionality().isLeft() ? LEFT : RIGHT;
        if (perceivingGtu != null)
        {
            this.ids.add(perceivingGtu.getId());
        }
        this.ignoreIfUpstream = ignoreIfUpstream;
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
            Length searchPos = (plus ? position.plus(this.margin) : position.minus(this.margin));
            next = record.getLane().getGtuAhead(searchPos, record.getDirection(), RelativePosition.FRONT,
                    record.getLane().getParentLink().getSimulator().getSimulatorAbsTime());
            if (next == null)
            {
                return null;
            }
            n = record.getLane().indexOfGtu(next);
            pos = next.position(record.getLane(), next.getRear());

            if (this.ids.contains(next.getId()))
            {
                // rear still on previous lane; it is found there, get next gtu
                pos = plus ? pos.plus(next.getLength()) : pos.minus(next.getLength());
                return getNext(record, pos, n);
            }
            if (this.ignoreIfUpstream)
            {
                if (plus ? pos.si < 0.0 : pos.si > record.getLane().getLength().si)
                {
                    pos = plus ? pos.plus(next.getLength()) : pos.minus(next.getLength());
                    return getNext(record, pos, n);
                }
            }
        }
        else
        {
            n = plus ? counter + 1 : counter - 1;
            if (n < 0 || n >= record.getLane().numberOfGtus())
            {
                return null;
            }
            next = record.getLane().getGtu(n);
            pos = next.position(record.getLane(), next.getRear());
            if (this.ids.contains(next.getId()))
            {
                // skip self
                pos = plus ? pos.plus(next.getLength()) : pos.minus(next.getLength());
                return getNext(record, pos, n);
            }
        }
        return new Entry(next, n, pos);
    }

    /** {@inheritDoc} */
    @Override
    protected Length getDistance(final LaneBasedGTU object, final LaneRecord<?> record, final Length position)
    {
        return record.getDistanceToPosition(position).minus(getDx());
    }

    /** {@inheritDoc} */
    @Override
    public HeadwayGTU perceive(final LaneBasedGTU perceivingGtu, final LaneBasedGTU object, final Length distance)
            throws GTUException, ParameterException
    {
        return this.headwayGtuType.createDownstreamGtu(perceivingGtu, object, distance);
    }

}
