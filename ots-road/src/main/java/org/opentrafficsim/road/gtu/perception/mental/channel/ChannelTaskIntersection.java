package org.opentrafficsim.road.gtu.perception.mental.channel;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.UUID;
import java.util.function.Function;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.djutils.immutablecollections.ImmutableSet;
import org.opentrafficsim.base.DistancedObject;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.base.parameters.constraint.DualBound;
import org.opentrafficsim.base.parameters.constraint.NumericConstraint;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.perception.LanePerception;
import org.opentrafficsim.road.gtu.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.perception.categories.IntersectionPerception;
import org.opentrafficsim.road.gtu.perception.categories.neighbors.PerceivedGtuType;
import org.opentrafficsim.road.gtu.perception.mental.AbstractTask;
import org.opentrafficsim.road.gtu.perception.object.PerceivedGtu;
import org.opentrafficsim.road.network.conflict.Conflict;
import org.opentrafficsim.road.network.speed.SpeedLimit;

/**
 * Task demand due to intersection, including conflicts. This class implements the task demand model by Yiyun et al. (2026).
 * This defines the task demand as:<br>
 * <br>
 * <i>TD</i> = <i>b</i> * exp(<i>x</i> / <i>Beta_yl</i>) + <i>c</i> * exp(<i>TTCP</i> / <i>Beta_con</i>)<br>
 * <br>
 * where <i>x</i> is the distance to the yield line and <i>TTCP</i> is the time-to-conflict-point of the nearest (least
 * <i>TTCP</i>) conflicting vehicle.
 * <p>
 * This class extents this model to perception with channels. A channel is added for each group of conflicts. A group of
 * conflicts is formed if there is any overlap of their upstream nodes within <i>x0</i>. This upstream search branches at merges
 * but stops at splits.
 * <p>
 * For each channel <i>i</i>, task demand is determined as:<br>
 * <br>
 * <i>TDi</i> = <i>Fi</i> * <i>b</i> * exp(<i>x</i> / <i>Beta_yl</i>) + <i>c</i> * exp(<i>TTCPi</i> / <i>Beta_con</i>)<br>
 * <br>
 * Here, <i>Fi</i> is the factor of the first component that assigns some of the yield line related task demand to channel
 * <i>i</i>. This factor is the results of a weighted average, where each weight is defined as:<br>
 * <br>
 * <i>Wi</i> = 1 + <i>c</i> * exp(<i>TTCPi</i> / <i>Beta_con</i>)<br>
 * <br>
 * This equation captures a balance between uniform attention distribution, and attention being given only to the most critical
 * channel. Finally, <i>TTCPi</i> is the least <i>TTCP</i> of all conflicting vehicles upstream of conflicts in the group of
 * channel <i>i</i>.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
// TODO add @see ref to paper by Yiyun et al.
public class ChannelTaskIntersection extends AbstractTask implements ChannelTask
{

    /** Look-ahead distance. */
    public static final ParameterTypeLength LOOKAHEAD = ParameterTypes.LOOKAHEAD;

    /** Maximum ego task demand. */
    public static final ParameterTypeDouble TD_B = new ParameterTypeDouble("TD_B",
            "Maximum task demand due to ego distance to intersection.", 0.3 / (0.3 + 0.1), DualBound.UNITINTERVAL)
    {
        @Override
        public void check(final Double value, final Parameters params) throws ParameterException
        {
            Throw.when(params.contains(TD_C) && params.getParameter(TD_C) + value >= 1.0, ParameterException.class,
                    "Values for TD_B and TD_C should sum to a value below 1.0");
        }
    };

    /** Ego decay parameter for distance to the yield line. */
    public static final ParameterTypeLength BETA_YL = new ParameterTypeLength("Beta_yl",
            "Exponential decay of conflict task by ego distance.", Length.ofSI(25.12), NumericConstraint.POSITIVEZERO);

    /** Maximum task demand due to conflicting vehicle. */
    public static final ParameterTypeDouble TD_C =
            new ParameterTypeDouble("TD_C", "Maximum task demand due to time-to-conflict-point of conflicting vehicle.",
                    0.1 / (0.3 + 0.1), DualBound.UNITINTERVAL)
            {
                @Override
                public void check(final Double value, final Parameters params) throws ParameterException
                {
                    Throw.when(params.contains(TD_B) && params.getParameter(TD_B) + value >= 1.0, ParameterException.class,
                            "Values for TD_B and TD_C should sum to a value below 1.0");
                }
            };

    /** Conflicting vehicle decay parameter. */
    public static final ParameterTypeDuration BETA_CON = new ParameterTypeDuration("Beta_con",
            "Exponential decay of conflict task from time-to-conflict-point of conflicting vehicle.", Duration.ofSI(12.13),
            NumericConstraint.POSITIVEZERO);

    /** Speed of ghost vehicle when no speed limit is given. */
    private static final Speed GHOST_SPEED = new Speed(50.0, SpeedUnit.KM_PER_HOUR);

    /**
     * Standard supplier that supplies a task per grouped set of conflicts based on common upstream nodes.
     */
    public static final Function<LanePerception, Set<ChannelTask>> SUPPLIER = (perception) ->
    {
        Set<ChannelTask> tasksOut = new LinkedHashSet<>();
        ChannelMental channelMental =
                (perception.getMental().isPresent() && perception.getMental().get() instanceof ChannelMental m) ? m : null;
        Set<SortedSet<DistancedObject<Conflict>>> groups = findConflictGroups(perception);
        IntersectionTaskGroup intersectionTaskGroup = new IntersectionTaskGroup();
        if (!groups.isEmpty())
        {
            DistancedObject<Conflict> first = null;
            for (SortedSet<DistancedObject<Conflict>> group : groups)
            {
                for (DistancedObject<Conflict> conflict : group)
                {
                    if (!conflict.object().getConflictType().isSplit()
                            && (first == null || first.distance().gt(conflict.distance())))
                    {
                        first = conflict;
                    }
                }
            }

            // add task without any conflict that will relate to FRONT for the case there are no conflicting moving vehicles
            tasksOut.add(new ChannelTaskIntersection(perception.getGtu(), first, new TreeSet<>(), intersectionTaskGroup));

            // groups are inherently ordered as perception returns conflicts from close to far
            for (SortedSet<DistancedObject<Conflict>> group : groups)
            {
                splitCarFollowing(tasksOut, group, channelMental);
                if (!group.isEmpty())
                {
                    tasksOut.add(new ChannelTaskIntersection(perception.getGtu(), first, group, intersectionTaskGroup));
                    // make sure the channel (key is first conflict) can be found for all individual conflicts
                    if (channelMental != null)
                    {
                        group.forEach((c) -> channelMental.mapToChannel(c.object(), group.first().object()));
                    }
                }
            }
        }
        return tasksOut;
    };

    /** GTU. */
    private final LaneBasedGtu gtu;

    /** First conflict on intersection. */
    private final DistancedObject<Conflict> first;

    /** Conflicts in the group. */
    private final SortedSet<DistancedObject<Conflict>> conflicts;

    /** Group of all instantaneous intersection tasks. */
    private final IntersectionTaskGroup intersectionTaskGroup;

    /** Conflicting task demand. */
    private Double conflictingTaskDemand;

    /**
     * Constructor.
     * @param gtu GTU
     * @param first first conflict in the intersection
     * @param conflicts conflicts in the group
     * @param intersectionTaskGroup group of all instantaneous intersection tasks
     */
    protected ChannelTaskIntersection(final LaneBasedGtu gtu, final DistancedObject<Conflict> first,
            final SortedSet<DistancedObject<Conflict>> conflicts, final IntersectionTaskGroup intersectionTaskGroup)
    {
        super(getId(conflicts));
        this.gtu = gtu;
        this.first = first;
        this.conflicts = conflicts;
        this.intersectionTaskGroup = intersectionTaskGroup;
        intersectionTaskGroup.addTask(this);
    }

    /**
     * Creates an ID for this task based on the conflicts.
     * @param conflicts conflicts
     * @return ID for this task based on the conflicts
     */
    private static String getId(final SortedSet<DistancedObject<Conflict>> conflicts)
    {
        if (conflicts.isEmpty())
        {
            return UUID.randomUUID().toString();
        }
        return conflicts.first().object().getFullId();
    }

    @Override
    public Object getChannel()
    {
        return this.conflicts.isEmpty() ? FRONT : this.conflicts.first().object();
    }

    @Override
    public double calculateTaskDemand(final LanePerception perception)
    {
        Length betaYl = Try.assign(() -> this.gtu.getParameters().getParameter(BETA_YL), "Parameter Beta_yl not present.");
        double tdB = Try.assign(() -> this.gtu.getParameters().getParameter(TD_B), "Parameter TD_B not present.");
        double egoDistance = this.first.distance().si < 0.0 ? 0.0 : this.first.distance().si;
        return this.intersectionTaskGroup.getWeightedFactor(this) * tdB * Math.exp(-egoDistance / betaYl.si)
                + getConflictingTaskDemand();
    }

    /**
     * Returns the relevance of this specific channel in the general intersection context.
     * @return relevance of this specific channel in the general intersection context
     */
    private double getWeight()
    {
        return this.conflicts.isEmpty() ? 0.0 : 1.0 + getConflictingTaskDemand();
    }

    /**
     * Returns conflicting task demand.
     * @return conflicting task demand
     */
    private double getConflictingTaskDemand()
    {
        if (this.conflictingTaskDemand == null)
        {
            Duration conflictingTimeToConflict = Duration.POSITIVE_INFINITY;
            Length x0 = this.gtu.getParameters().getOptionalParameter(LOOKAHEAD)
                    .orElseThrow(() -> new OtsRuntimeException("Parameter Lookahead not present."));
            for (DistancedObject<Conflict> conflict : this.conflicts)
            {
                if (conflict.distance().ge0())
                {
                    PerceptionCollectable<PerceivedGtu, LaneBasedGtu> conflictingGtus =
                            conflict.object().getOtherConflict().getUpstreamGtus(this.gtu, PerceivedGtuType.WRAP, x0);
                    if (conflictingGtus.isEmpty())
                    {
                        Optional<SpeedLimit> speedLimitLane = conflict.object().getOtherConflict().getLane().getSpeedLimit();
                        Speed speedLimit = speedLimitLane.isPresent() ? speedLimitLane.get().speed() : GHOST_SPEED;
                        conflictingTimeToConflict = Duration.min(conflictingTimeToConflict, x0.divide(speedLimit));
                    }
                    else
                    {
                        PerceivedGtu conflictingGtu = conflictingGtus.first();
                        conflictingTimeToConflict =
                                Duration.min(conflictingTimeToConflict, conflictingGtu.getKinematics().getOverlap().isParallel()
                                        ? Duration.ZERO : conflictingGtu.getDistance().divide(conflictingGtu.getSpeed()));
                    }
                }
            }
            double tdC = this.gtu.getParameters().getOptionalParameter(TD_C)
                    .orElseThrow(() -> new OtsRuntimeException("Parameter TD_C not present."));
            Duration betaCon = this.gtu.getParameters().getOptionalParameter(BETA_CON)
                    .orElseThrow(() -> new OtsRuntimeException("Parameter Beta_con not present."));
            this.conflictingTaskDemand = tdC * Math.exp(-conflictingTimeToConflict.si / betaCon.si);
        }
        return this.conflictingTaskDemand;
    }

    /**
     * Returns conflict groups, which are grouped based on overlap in the upstream nodes of the conflicting lanes.
     * @param perception perception
     * @return conflict groups
     */
    private static Set<SortedSet<DistancedObject<Conflict>>> findConflictGroups(final LanePerception perception)
    {
        IntersectionPerception intersection =
                Try.assign(() -> perception.getPerceptionCategory(IntersectionPerception.class), "No intersection perception.");
        Iterator<DistancedObject<Conflict>> conflicts =
                intersection.getConflicts(RelativeLane.CURRENT).underlyingWithDistance();

        // Find groups of conflicts when their upstream nodes are intersecting sets
        Map<SortedSet<DistancedObject<Conflict>>, Set<Node>> groups = new LinkedHashMap<>();
        Length x0 = perception.getGtu().getParameters().getOptionalParameter(LOOKAHEAD)
                .orElseThrow(() -> new OtsRuntimeException("No x0 parameter."));
        while (conflicts.hasNext())
        {
            DistancedObject<Conflict> conflict = conflicts.next();
            Set<Node> nodes = getUpstreamNodes(conflict.object().getOtherConflict(), x0);
            // find overlap
            Entry<SortedSet<DistancedObject<Conflict>>, Set<Node>> group = null;
            Iterator<Entry<SortedSet<DistancedObject<Conflict>>, Set<Node>>> groupIterator = groups.entrySet().iterator();
            while (groupIterator.hasNext())
            {
                Entry<SortedSet<DistancedObject<Conflict>>, Set<Node>> entry = groupIterator.next();
                if (entry.getValue().stream().anyMatch(nodes::contains))
                {
                    // overlap with this entry
                    if (group == null)
                    {
                        entry.getKey().add(conflict);
                        entry.getValue().addAll(nodes);
                        group = entry;
                        // keep looping to also merge other groups if they overlap with the upstream nodes of this conflict
                    }
                    else
                    {
                        // the nodes overlap with multiple groups that did so far not yet overlap, merge the other group too
                        group.getKey().addAll(entry.getKey());
                        group.getValue().addAll(entry.getValue());
                        groupIterator.remove();
                    }
                }
            }
            if (group == null)
            {
                // no overlap found, make new group
                SortedSet<DistancedObject<Conflict>> key = new TreeSet<>();
                key.add(conflict);
                groups.put(key, nodes);
            }
        }
        return groups.keySet();
    }

    /**
     * Finds all nodes within a given distance upstream of a conflict, stopping at any diverge, branging at merges.
     * @param conflict conflict.
     * @param x0 distance to loop upstream.
     * @return set of all upstream nodes within distance.
     */
    private static Set<Node> getUpstreamNodes(final Conflict conflict, final Length x0)
    {
        Set<Node> nodes = new LinkedHashSet<>();
        Link link = conflict.getLane().getLink();
        Length distance = link.getLength().times(conflict.getLane().fraction(conflict.getLongitudinalPosition()) - 1.0);
        appendUpstreamNodes(link, distance, x0, nodes);
        return nodes;
    }

    /**
     * Append upstream nodes, branging upstream at merges, stopping at any diverge.
     * @param link next link to move along.
     * @param distance distance between end of link and conflict, upstream of conflict.
     * @param x0 search distance.
     * @param nodes collected nodes.
     */
    private static void appendUpstreamNodes(final Link link, final Length distance, final Length x0, final Set<Node> nodes)
    {
        Length nextDistance = distance.plus(link.getLength());
        if (nextDistance.le(x0))
        {
            Node start = link.getStartNode();
            ImmutableSet<Link> links = start.getLinks();
            Set<Link> upstreamLinks = new LinkedHashSet<>();
            for (Link next : links)
            {
                if (!next.equals(link))
                {
                    if (next.getStartNode().equals(start))
                    {
                        // diverge
                        nodes.add(start);
                        return;
                    }
                    upstreamLinks.add(next);
                }
            }
            nodes.add(start);
            for (Link upstreamLink : upstreamLinks)
            {
                appendUpstreamNodes(upstreamLink, nextDistance, x0, nodes);
            }
        }
    }

    /**
     * Apply car-following task on each split in the group, and remove it from the group.
     * @param tasks tasks to add any split related task to
     * @param group group of conflicts
     * @param channelMental mental module, can be {@code null}
     */
    private static void splitCarFollowing(final Set<ChannelTask> tasks, final SortedSet<DistancedObject<Conflict>> group,
            final ChannelMental channelMental)
    {
        Iterator<DistancedObject<Conflict>> iterator = group.iterator();
        while (iterator.hasNext())
        {
            DistancedObject<Conflict> conflict = iterator.next();
            if (conflict.object().getConflictType().isSplit())
            {
                iterator.remove();
                tasks.add(new ChannelTaskCarFollowing((p) ->
                {
                    // this provides the first leader on the other split conflict with distance towards perceiving GTU
                    Conflict otherconflict = conflict.object().getOtherConflict();
                    PerceptionCollectable<PerceivedGtu, LaneBasedGtu> conflictingGtus =
                            otherconflict.getDownstreamGtus(p.getGtu(), PerceivedGtuType.WRAP, otherconflict.getLength());
                    if (conflictingGtus.isEmpty())
                    {
                        return null;
                    }
                    DistancedObject<LaneBasedGtu> leader = conflictingGtus.underlyingWithDistance().next();
                    return new DistancedObject<LaneBasedGtu>(leader.object(), conflict.distance().plus(leader.distance()));
                }));
                // make sure the channel (key is front) can be found for the split conflict
                if (channelMental != null)
                {
                    channelMental.mapToChannel(conflict.object(), FRONT);
                }
            }
        }
    }

    /**
     * Group of intersection tasks.
     */
    private static final class IntersectionTaskGroup
    {

        /** Set of currently relevant intersection tasks. */
        private final Map<ChannelTaskIntersection, Double> weights = new LinkedHashMap<>();

        /** Total weight. */
        private double totalWeight = 0.0;

        /**
         * Add task.
         * @param task task
         */
        public void addTask(final ChannelTaskIntersection task)
        {
            double weight = task.getWeight();
            this.weights.put(task, weight);
            this.totalWeight += weight;
        }

        /**
         * Get weighted factor of task demand for this specific task (for a specific channel).
         * @param channelTaskIntersection task
         * @return weighted factor of task demand for this specific task (for a specific channel)
         */
        public double getWeightedFactor(final ChannelTaskIntersection channelTaskIntersection)
        {
            if (channelTaskIntersection.conflicts.isEmpty())
            {
                return this.totalWeight == 0.0 ? 1.0 : 0.0;
            }
            return this.totalWeight == 0.0 ? 0.0 : (this.weights.get(channelTaskIntersection) / this.totalWeight);
        }

    }

}
