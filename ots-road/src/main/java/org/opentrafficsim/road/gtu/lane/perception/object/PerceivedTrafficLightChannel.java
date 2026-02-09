package org.opentrafficsim.road.gtu.lane.perception.object;

import java.util.function.Supplier;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLightColor;

/**
 * Traffic light perceived through delayed perception channel.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class PerceivedTrafficLightChannel extends PerceivedTrafficLight
{

    /** Perception delay supplier. */
    private final Supplier<Duration> perceptionDelay;

    /** Simulator. */
    private final OtsSimulatorInterface simulator;

    /**
     * Construct a new Headway information object, for a traffic light ahead of us (or behind us, although that does not seem
     * very useful).
     * @param trafficLight the traffic light object for further observation, can not be null.
     * @param distance the distance to the traffic light, distance cannot be null.
     * @param turnOnRed whether the perceiving GTU may turn on red.
     * @param perceptionDelay perception delay supplier.
     */
    public PerceivedTrafficLightChannel(final TrafficLight trafficLight, final Length distance, final boolean turnOnRed,
            final Supplier<Duration> perceptionDelay)
    {
        super(trafficLight, distance, turnOnRed);
        this.perceptionDelay = perceptionDelay;
        this.simulator = trafficLight.getSimulator();
    }

    /**
     * Returns the traffic light color.
     * @return the traffic light color.
     */
    @Override
    public TrafficLightColor getTrafficLightColor()
    {
        Duration when = this.simulator.getSimulatorTime().minus(this.perceptionDelay.get());
        return getTrafficLight().getTrafficLightColor(when);
    }

}
