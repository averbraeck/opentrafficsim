package org.opentrafficsim.road.gtu.lane.perception.mental.channel;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.UnderlyingDistance;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.mental.AbstractTask;
import org.opentrafficsim.road.gtu.lane.perception.mental.ar.ArTaskCarFollowingExp;

/**
 * Task demand for car-following. This is defined as {@code exp(-T/h)} where {@code T} is the time headway to the leader and
 * {@code h} is the car-following task parameter that scales it.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ChannelTaskCarFollowing extends AbstractTask implements ChannelTask
{

    /** Car-following task parameter. */
    public static final ParameterTypeDuration HEXP = ArTaskCarFollowingExp.HEXP;

    /** Singleton instance. */
    private static final ChannelTaskCarFollowing DEFAULT = new ChannelTaskCarFollowing();

    /** Default set that is returned by the supplier. */
    private static final Set<ChannelTask> SET = Set.of(DEFAULT);

    /** Standard supplier that supplies a single instance of the car-following task. */
    public static final Function<LanePerception, Set<ChannelTask>> SUPPLIER = (p) -> SET;

    /** Leader supplier. */
    private final Function<LanePerception, UnderlyingDistance<LaneBasedGtu>> leaderSupplier;

    /**
     * Constructor that will use the first leader from NeighborsPerception in the current lane.
     */
    public ChannelTaskCarFollowing()
    {
        this((perception) ->
        {
            NeighborsPerception neighbors = Try.assign(() -> perception.getPerceptionCategory(NeighborsPerception.class),
                    "NeighborsPerception not present.");
            Iterator<UnderlyingDistance<LaneBasedGtu>> leader =
                    neighbors.getLeaders(RelativeLane.CURRENT).underlyingWithDistance();
            if (!leader.hasNext())
            {
                return null;
            }
            return leader.next();
        });
    }

    /**
     * Constructor that provides a supplier for a leader that follows a non-default logic.
     * @param leaderSupplier leader supplier
     */
    public ChannelTaskCarFollowing(final Function<LanePerception, UnderlyingDistance<LaneBasedGtu>> leaderSupplier)
    {
        super("car-following (front)");
        this.leaderSupplier = leaderSupplier;
    }

    @Override
    public String getId()
    {
        return "car-following (front)";
    }

    @Override
    public Object getChannel()
    {
        return FRONT;
    }

    @Override
    public double calculateTaskDemand(final LanePerception perception)
    {
        UnderlyingDistance<LaneBasedGtu> leader = this.leaderSupplier.apply(perception);
        if (leader == null)
        {
            return 0.0;
        }
        EgoPerception<?, ?> ego =
                Try.assign(() -> perception.getPerceptionCategory(EgoPerception.class), "EgoPerception not present.");
        Duration headway = leader.distance().divide(ego.getSpeed());
        Duration h = Try.assign(() -> perception.getGtu().getParameters().getParameter(HEXP), "Parameter h_exp not present.");
        return headway.si <= 0.0 ? 0.999 : Math.exp(-headway.si / h.si);
    }

}
