package org.opentrafficsim.road.gtu.lane.perception.object;

import java.util.Optional;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.geometry.OtsLine2d;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.PerceivedGtuType;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.conflict.Conflict;
import org.opentrafficsim.road.network.lane.conflict.ConflictPriority;
import org.opentrafficsim.road.network.lane.conflict.ConflictRule;
import org.opentrafficsim.road.network.lane.conflict.ConflictType;

/**
 * Interface for perceived {@code Conflict} objects. A standard implementation is provided under {@code of(...)} which wraps a
 * {@code Conflict} and returns most values as is.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface PerceivedConflict extends PerceivedLaneBasedObject
{

    /**
     * Returns the conflict type.
     * @return conflict type
     */
    ConflictType getConflictType();

    /**
     * Returns whether this is a crossing conflict.
     * @return whether this is a crossing conflict
     */
    boolean isCrossing();

    /**
     * Returns whether this is a merge conflict.
     * @return whether this is a merge conflict
     */
    boolean isMerge();

    /**
     * Returns whether this is a split conflict.
     * @return whether this is a split conflict
     */
    boolean isSplit();

    /**
     * Returns the conflict priority.
     * @return conflict priority
     */
    ConflictPriority getConflictPriority();

    /**
     * Returns the length of the conflict on the conflicting lane.
     * @return length of the conflict on the conflicting lane
     */
    Length getConflictingLength();

    /**
     * Returns a set of conflicting GTU's upstream of the <i>start</i> of the conflict ordered close to far from the conflict.
     * @return set of conflicting GTU's upstream of the <i>start</i> of the conflict ordered close to far from the conflict
     */
    PerceptionCollectable<PerceivedGtu, LaneBasedGtu> getUpstreamConflictingGTUs();

    /**
     * Returns a set of conflicting GTU's downstream of the <i>start</i> of the conflict ordered close to far from the conflict.
     * Distance is given relative to the <i>end</i> of the conflict, or null for conflicting vehicles on the conflict. In the
     * latter case the overlap is used.
     * @return set of conflicting GTU's downstream of the <i>start</i> of the conflict ordered close to far from the conflict
     */
    PerceptionCollectable<PerceivedGtu, LaneBasedGtu> getDownstreamConflictingGTUs();

    /**
     * Returns the visibility on the conflicting lane within which conflicting vehicles are visible. All upstream conflicting
     * GTUs have a distance smaller than the visibility. Depending on a limited visibility, a certain (lower) speed may be
     * required while approaching the conflict.
     * @return visibility on the conflicting lane within which conflicting vehicles are visible
     */
    Length getConflictingVisibility();

    /**
     * Returns the speed limit on the conflicting lane.
     * @return speed limit on the conflicting lane
     */
    Speed getConflictingSpeedLimit();

    /**
     * Returns the conflicting link.
     * @return the conflicting link
     */
    CrossSectionLink getConflictingLink();

    /**
     * Returns the stop line.
     * @return stop line
     */
    PerceivedObject getStopLine();

    /**
     * Returns the stop line on the conflicting lane.
     * @return stop line
     */
    PerceivedObject getConflictingStopLine();

    /**
     * Returns the conflict rule type.
     * @return conflict rule type
     */
    Class<? extends ConflictRule> getConflictRuleType();

    /**
     * Returns the distance of a traffic light upstream on the conflicting lane.
     * @return distance of a traffic light upstream on the conflicting lane, empty if no traffic light
     */
    Optional<Length> getConflictingTrafficLightDistance();

    /**
     * Whether the conflict is permitted by the traffic light.
     * @return whether the conflict is permitted by the traffic light
     */
    boolean isPermitted();

    /**
     * Returns the width at the given fraction.
     * @param fraction fraction from 0 to 1
     * @return width at the given fraction
     */
    Length getWidthAtFraction(double fraction);

    /**
     * Returns a standard implementation of this interface that wraps a {@code Conflict} and uses a given perceived GTU type to
     * perceive the upstream and downstream GTUs.
     * @param perceivingGtu perceiving GTU
     * @param conflict conflict to perceive
     * @param perceivedGtuType perceived GTU type
     * @param distance distance from perceiving GTU to conflict
     * @param conflictingVisibility visibility range at other conflict
     * @return perceived conflict
     */
    @SuppressWarnings("methodlength")
    static PerceivedConflict of(final LaneBasedGtu perceivingGtu, final Conflict conflict,
            final PerceivedGtuType perceivedGtuType, final Length distance, final Length conflictingVisibility)
    {
        final Kinematics kinematics = Kinematics.staticAhead(distance);
        // TODO stop lines (current models happen not to use this, but should be possible)
        final PerceivedObject stopLine =
                new PerceivedObjectBase("stopLineId", ObjectType.STOPLINE, Length.ZERO, Kinematics.staticAhead(Length.ZERO));
        final PerceivedObject conflictingStopLine = new PerceivedObjectBase("conflictingStopLineId", ObjectType.STOPLINE,
                Length.ZERO, Kinematics.staticAhead(Length.ZERO));
        final Speed conflictingSpeedLimit = Try.assign(() -> conflict.getOtherConflict().getLane().getHighestSpeedLimit(),
                "Unable to obtain higest speed limit on conflicting lane.");
        final Length conflictingTrafficLightDistance =
                conflict.getOtherConflict().getTrafficLightDistance(conflictingVisibility);

        final PerceptionCollectable<PerceivedGtu, LaneBasedGtu> upstreamConflictingGTUs =
                conflict.getOtherConflict().getUpstreamGtus(perceivingGtu, perceivedGtuType, conflictingVisibility);
        final PerceptionCollectable<PerceivedGtu, LaneBasedGtu> downstreamConflictingGTUs =
                conflict.getOtherConflict().getDownstreamGtus(perceivingGtu, perceivedGtuType, conflictingVisibility);

        Length pos1a = conflict.getLongitudinalPosition();
        Length pos2a = conflict.getOtherConflict().getLongitudinalPosition();
        Length pos1b = Length.min(pos1a.plus(conflict.getLength()), conflict.getLane().getLength());
        Length pos2b = Length.min(pos2a.plus(conflict.getOtherConflict().getLength()),
                conflict.getOtherConflict().getLane().getLength());
        OtsLine2d line1 = conflict.getLane().getCenterLine();
        OtsLine2d line2 = conflict.getOtherConflict().getLane().getCenterLine();
        double dStart = line1.getLocation(pos1a).distance(line2.getLocation(pos2a));
        double dEnd = line1.getLocation(pos1b).distance(line2.getLocation(pos2b));
        double startWidth = dStart + .5 * conflict.getLane().getWidth(pos1a).si
                + .5 * conflict.getOtherConflict().getLane().getWidth(pos2a).si;
        double endWidth = dEnd + .5 * conflict.getLane().getWidth(pos1b).si
                + .5 * conflict.getOtherConflict().getLane().getWidth(pos2b).si;

        return new PerceivedConflict()
        {
            @Override
            public Lane getLane()
            {
                return conflict.getLane();
            }

            @Override
            public ObjectType getObjectType()
            {
                return ObjectType.CONFLICT;
            }

            @Override
            public Length getLength()
            {
                return conflict.getLength();
            }

            @Override
            public Kinematics getKinematics()
            {
                return kinematics;
            }

            @Override
            public String getId()
            {
                return conflict.getId();
            }

            @Override
            public ConflictType getConflictType()
            {
                return conflict.getConflictType();
            }

            @Override
            public boolean isCrossing()
            {
                return conflict.getConflictType().isCrossing();
            }

            @Override
            public boolean isMerge()
            {
                return conflict.getConflictType().isMerge();
            }

            @Override
            public boolean isSplit()
            {
                return conflict.getConflictType().isSplit();
            }

            @Override
            public ConflictPriority getConflictPriority()
            {
                return conflict.conflictPriority();
            }

            @Override
            public Length getConflictingLength()
            {
                return conflict.getOtherConflict().getLength();
            }

            @Override
            public PerceptionCollectable<PerceivedGtu, LaneBasedGtu> getUpstreamConflictingGTUs()
            {
                return upstreamConflictingGTUs;
            }

            @Override
            public PerceptionCollectable<PerceivedGtu, LaneBasedGtu> getDownstreamConflictingGTUs()
            {
                return downstreamConflictingGTUs;
            }

            @Override
            public Length getConflictingVisibility()
            {
                return conflictingVisibility;
            }

            @Override
            public Speed getConflictingSpeedLimit()
            {
                return conflictingSpeedLimit;
            }

            @Override
            public CrossSectionLink getConflictingLink()
            {
                return conflict.getOtherConflict().getLane().getLink();
            }

            @Override
            public PerceivedObject getStopLine()
            {
                return stopLine;
            }

            @Override
            public PerceivedObject getConflictingStopLine()
            {
                return conflictingStopLine;
            }

            @Override
            public Class<? extends ConflictRule> getConflictRuleType()
            {
                return conflict.getConflictRule().getClass();
            }

            @Override
            public Optional<Length> getConflictingTrafficLightDistance()
            {
                return Optional.of(conflictingTrafficLightDistance);
            }

            @Override
            public boolean isPermitted()
            {
                return conflict.isPermitted();
            }

            @Override
            public Length getWidthAtFraction(final double fraction)
            {
                return Length.ofSI((1.0 - fraction) * startWidth + fraction * endWidth);
            }
        };
    }

}
