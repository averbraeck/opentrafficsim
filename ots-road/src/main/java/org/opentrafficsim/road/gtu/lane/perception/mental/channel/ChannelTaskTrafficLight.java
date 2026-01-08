package org.opentrafficsim.road.gtu.lane.perception.mental.channel;

import java.util.Iterator;
import java.util.Set;
import java.util.function.Function;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.core.gtu.Stateless;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable.UnderlyingDistance;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.IntersectionPerception;
import org.opentrafficsim.road.gtu.lane.perception.mental.AbstractTask;
import org.opentrafficsim.road.gtu.lane.perception.mental.ar.ArTaskCarFollowingExp;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;

/**
 * Task demand for traffic lights. This is defined as {@code exp(-T/h)} where {@code T} is the time headway to the traffic light
 * and {@code h} is the car-following task parameter that scales it.
 * <p>
 * Copyright (c) 2024-2025 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ChannelTaskTrafficLight extends AbstractTask implements ChannelTask, Stateless<ChannelTaskTrafficLight>
{

    /** Car-following task parameter. */
    public static final ParameterTypeDuration HEXP = ArTaskCarFollowingExp.HEXP;

    /** Singleton instance. */
    public static final ChannelTaskTrafficLight SINGLETON = new ChannelTaskTrafficLight();

    /** Default set that is returned by the supplier. */
    private static final Set<ChannelTask> SET = Set.of(SINGLETON);

    /** Standard supplier that supplies a single instance of the traffic light task. */
    public static final Function<LanePerception, Set<ChannelTask>> SUPPLIER = (p) -> SET;

    /**
     * Constructor.
     */
    public ChannelTaskTrafficLight()
    {
        super("traffic-light (front)");
    }

    @Override
    public ChannelTaskTrafficLight get()
    {
        return SINGLETON;
    }

    @Override
    public Object getChannel()
    {
        return SINGLETON;
    }

    @Override
    public double calculateTaskDemand(final LanePerception perception)
    {
        IntersectionPerception intersection = Try.assign(() -> perception.getPerceptionCategory(IntersectionPerception.class),
                "IntersectionPerception not present.");
        Iterator<UnderlyingDistance<TrafficLight>> trafficLights =
                intersection.getTrafficLights(RelativeLane.CURRENT).underlyingWithDistance();
        if (!trafficLights.hasNext())
        {
            return 0.0;
        }
        EgoPerception<?, ?> ego =
                Try.assign(() -> perception.getPerceptionCategory(EgoPerception.class), "EgoPerception not present.");
        Duration headway = trafficLights.next().distance().divide(ego.getSpeed());
        Duration h = Try.assign(() -> perception.getGtu().getParameters().getParameter(HEXP), "Parameter h_exp not present.");
        return Math.exp(-headway.si / h.si);
    }

}
