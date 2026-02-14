package org.opentrafficsim.road.network.control.rampmetering;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.object.DetectorType;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.network.object.detector.LaneDetector;
import org.opentrafficsim.road.network.object.trafficlight.TrafficLight;
import org.opentrafficsim.road.network.object.trafficlight.TrafficLightColor;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;

/**
 * Controller using a cycle time.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class CycleTimeLightController implements RampMeteringLightController
{

    /** Minimum red duration. */
    private static final Duration MIN_RED_TIME = Duration.ofSI(2.0);

    /** Whether the controller is enabled. */
    private boolean enabled = false;

    /** Time when red phase was started. */
    private Map<TrafficLight, Duration> greenStarts = new LinkedHashMap<>();

    /** Cycle time. */
    private Duration cTime;

    /** Simulator. */
    private final OtsSimulatorInterface simulator;

    /** Traffic lights. */
    private final List<TrafficLight> trafficLights;

    /** Scheduled red event. */
    private Map<TrafficLight, SimEventInterface<Duration>> redEvents = new LinkedHashMap<>();

    /** Scheduled green event. */
    private Map<TrafficLight, SimEventInterface<Duration>> greenEvents = new LinkedHashMap<>();

    /**
     * Constructor.
     * @param simulator simulator
     * @param trafficLights traffic lights
     * @param detectorType detector type.
     */
    public CycleTimeLightController(final OtsSimulatorInterface simulator, final List<TrafficLight> trafficLights,
            final DetectorType detectorType)
    {
        this.simulator = simulator;
        this.trafficLights = trafficLights;
        for (TrafficLight trafficLight : trafficLights)
        {
            Try.execute(() -> new RampMeteringDetector(trafficLight, detectorType), OtsRuntimeException.class,
                    "Unexpected exception while creating a detector with a ramp metering traffic light.");
            this.greenStarts.put(trafficLight, Duration.NEGATIVE_INFINITY);
        }
    }

    @Override
    public void disable()
    {
        Iterator<TrafficLight> it = this.redEvents.keySet().iterator();
        while (it.hasNext())
        {
            TrafficLight trafficLight = it.next();
            this.simulator.cancelEvent(this.redEvents.get(trafficLight));
            it.remove();
        }
        it = this.greenEvents.keySet().iterator();
        while (it.hasNext())
        {
            TrafficLight trafficLight = it.next();
            this.simulator.cancelEvent(this.greenEvents.get(trafficLight));
            it.remove();
        }
        this.enabled = false;
        for (TrafficLight trafficLight : this.trafficLights)
        {
            trafficLight.setTrafficLightColor(TrafficLightColor.GREEN);
        }
    }

    /**
     * Starts the cycle.
     * @param cycleTime cycle time
     */
    @Override
    public void enable(final Duration cycleTime)
    {
        Logger.ots().info("Traffic light uses " + cycleTime);
        this.cTime = cycleTime;
        if (!this.enabled)
        {
            this.enabled = true;
            for (TrafficLight trafficLight : this.trafficLights)
            {
                setGreen(trafficLight);
            }
        }
    }

    /**
     * Sets the traffic light to red. Can be scheduled.
     * @param trafficLight traffic light
     */
    protected void setRed(final TrafficLight trafficLight)
    {
        this.redEvents.remove(trafficLight);
        Logger.ots().info("Traffic light set to RED");
        trafficLight.setTrafficLightColor(TrafficLightColor.RED);
    }

    /**
     * Sets the traffic light to green. Can be scheduled and remembers the green time.
     * @param trafficLight traffic light
     */
    protected void setGreen(final TrafficLight trafficLight)
    {
        this.greenEvents.remove(trafficLight);
        this.greenStarts.put(trafficLight, this.simulator.getSimulatorTime());
        Logger.ots().info("Traffic light set to GREEN");
        trafficLight.setTrafficLightColor(TrafficLightColor.GREEN);
    }

    /** Ramp metering sensor. */
    private class RampMeteringDetector extends LaneDetector
    {

        /** The traffic light. */
        private final TrafficLight trafficLight;

        /**
         * @param trafficLight traffic light
         * @param detectorType detector type.
         * @throws NetworkException when the position on the lane is out of bounds
         */
        RampMeteringDetector(final TrafficLight trafficLight, final DetectorType detectorType) throws NetworkException
        {
            super(trafficLight.getId() + "_sensor", trafficLight.getLane(), trafficLight.getLongitudinalPosition(),
                    RelativePosition.FRONT, detectorType);
            this.trafficLight = trafficLight;
        }

        @SuppressWarnings("synthetic-access")
        @Override
        protected void triggerResponse(final LaneBasedGtu gtu)
        {
            if (CycleTimeLightController.this.enabled && this.trafficLight.getTrafficLightColor().isGreen())
            {
                try
                {
                    // schedule green
                    Duration minRedTime = CycleTimeLightController.this.simulator.getSimulatorTime().plus(MIN_RED_TIME);
                    Duration cycleRedTime = CycleTimeLightController.this.greenStarts.get(this.trafficLight)
                            .plus(CycleTimeLightController.this.cTime);
                    Duration green;
                    if (minRedTime.ge(cycleRedTime))
                    {
                        Logger.ots().info("Traffic light set to RED");
                        this.trafficLight.setTrafficLightColor(TrafficLightColor.RED);
                        green = minRedTime;
                    }
                    else
                    {
                        Logger.ots().info("Traffic light set to YELLOW (RED over 'MIN_RED_TIME')");
                        this.trafficLight.setTrafficLightColor(TrafficLightColor.YELLOW);
                        CycleTimeLightController.this.redEvents.put(this.trafficLight, CycleTimeLightController.this.simulator
                                .scheduleEventRel(MIN_RED_TIME, () -> CycleTimeLightController.this.setRed(this.trafficLight)));
                        green = cycleRedTime;
                    }
                    CycleTimeLightController.this.greenEvents.put(this.trafficLight,
                            CycleTimeLightController.this.simulator.scheduleEventAbs(Duration.ofSI(green.si),
                                    () -> CycleTimeLightController.this.setGreen(this.trafficLight)));
                }
                catch (SimRuntimeException exception)
                {
                    throw new OtsRuntimeException(exception);
                }
            }
        }

    }

}
