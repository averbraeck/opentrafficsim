package org.opentrafficsim.road.gtu.perception.mental.channel;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.function.Function;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.immutablecollections.ImmutableSet;
import org.opentrafficsim.base.DistancedObject;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.constraint.NumericConstraint;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.network.Link;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.perception.LanePerception;
import org.opentrafficsim.road.gtu.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.perception.categories.IntersectionPerception;
import org.opentrafficsim.road.gtu.perception.categories.neighbors.PerceivedGtuType;
import org.opentrafficsim.road.gtu.perception.mental.AbstractTask;
import org.opentrafficsim.road.gtu.perception.mental.Mental;
import org.opentrafficsim.road.gtu.perception.object.PerceivedGtu;
import org.opentrafficsim.road.network.conflict.Conflict;

/**
 * Task demand for a group of conflicts pertaining to the same conflicting road. This is defined as
 * {@code exp(-max(T_ego, min(T_conf))/h)} where {@code T_ego} is the ego time until the first conflict, {@code T_conf} is the
 * time until the respective conflict of a conflicting vehicle and {@code h} is the car-following task parameter that scales it.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class ChannelTaskConflict extends AbstractTask implements ChannelTask
{
    /** Look-ahead distance. */
    public static final ParameterTypeLength LOOKAHEAD = ParameterTypes.LOOKAHEAD;

    /** Ego decay parameter. */
    public static final ParameterTypeDuration HEGO = new ParameterTypeDuration("h_ego",
            "Exponential decay of conflict task by ego approaching time.", Duration.ofSI(4.46), NumericConstraint.POSITIVEZERO);

    /** Conflicting decay parameter. */
    public static final ParameterTypeDuration HCONF =
            new ParameterTypeDuration("h_conf", "Exponential decay of conflict task by conflicting approaching time.",
                    Duration.ofSI(2.49), NumericConstraint.POSITIVEZERO);

    /**
     * Standard supplier that supplies a task per grouped set of conflicts based on common upstream nodes. This also adds a
     * scanning task demand to each of these channels.
     */
    public static final Function<LanePerception, Set<ChannelTask>> SUPPLIER = (perception) ->
    {
        Set<ChannelTask> tasks = new LinkedHashSet<>();
        Optional<Mental> mental = perception.getMental();
        ChannelMental channelMental = (mental.isPresent() && mental.get() instanceof ChannelMental m) ? m : null;
        for (SortedSet<DistancedObject<Conflict>> group : findConflictGroups(perception))
        {
            splitCarFollowing(tasks, group, channelMental);
            if (!group.isEmpty())
            {
                tasks.add(new ChannelTaskConflict(group));
                tasks.add(new ChannelTaskScan(group.first().object()));
                // make sure the channel (key is first conflict) can be found for all individual conflicts
                if (channelMental != null)
                {
                    group.forEach((c) -> channelMental.mapToChannel(c.object(), group.first().object()));
                }
            }
        }
        return tasks;
    };

    /** Conflicts in the group. */
    private final SortedSet<DistancedObject<Conflict>> conflicts;

    /**
     * Constructor.
     * @param conflicts conflicts in the group.
     */
    private ChannelTaskConflict(final SortedSet<DistancedObject<Conflict>> conflicts)
    {
        super("conflicts");
        this.conflicts = conflicts;
    }

    @Override
    public String getId()
    {
        return this.conflicts.first().object().getFullId();
    }

    @Override
    public Object getChannel()
    {
        return this.conflicts.first().object();
    }

    @Override
    public double calculateTaskDemand(final LanePerception perception)
    {
        // In the following, 'headway' means time until static conflict is reached, i.e. approaching time.

        // Get minimum headway of first vehicle on each conflict in the group
        Duration conflictHeadway = Duration.POSITIVE_INFINITY;
        LaneBasedGtu gtu = perception.getGtu();
        Length x0 = perception.getGtu().getParameters().getOptionalParameter(LOOKAHEAD)
                .orElseThrow(() -> new OtsRuntimeException("No x0 parameter."));
        for (DistancedObject<Conflict> conflict : this.conflicts)
        {
            PerceptionCollectable<PerceivedGtu, LaneBasedGtu> conflictingGtus =
                    conflict.object().getOtherConflict().getUpstreamGtus(gtu, PerceivedGtuType.WRAP, x0);
            if (!conflictingGtus.isEmpty())
            {
                PerceivedGtu conflictingGtu = conflictingGtus.first();
                conflictHeadway = Duration.min(conflictHeadway, conflictingGtu.getKinematics().getOverlap().isParallel()
                        ? Duration.ZERO : conflictingGtu.getDistance().divide(conflictingGtu.getSpeed()));
            }
        }

        // Get own approaching time
        EgoPerception<?, ?> ego = perception.getPerceptionCategoryOptional(EgoPerception.class)
                .orElseThrow(() -> new NoSuchElementException("EgoPerception not present."));
        Duration egoHeadway = this.conflicts.first().distance().divide(ego.getSpeed());

        // Find least critical
        Duration hEgo = perception.getGtu().getParameters().getOptionalParameter(HEGO)
                .orElseThrow(() -> new OtsRuntimeException("Parameter h_ego not present."));
        Duration hConf = perception.getGtu().getParameters().getOptionalParameter(HCONF)
                .orElseThrow(() -> new OtsRuntimeException("Parameter h_conf not present."));
        return Math.min(0.999, Math.exp(-Math.min(egoHeadway.si / hEgo.si, conflictHeadway.si / hConf.si)));
    }

    /**
     * Returns conflict groups, which are grouped based on overlap in the upstream nodes of the conflicting lanes.
     * @param perception perception
     * @return conflict groups
     */
    private static Set<SortedSet<DistancedObject<Conflict>>> findConflictGroups(final LanePerception perception)
    {
        IntersectionPerception intersection = perception.getPerceptionCategoryOptional(IntersectionPerception.class)
                .orElseThrow(() -> new NoSuchElementException("No intersection perception."));
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

}
