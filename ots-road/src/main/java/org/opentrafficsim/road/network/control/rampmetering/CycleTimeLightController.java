package org.opentrafficsim.road.network.control.rampmetering;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Try;
import org.opentrafficsim.core.compatibility.Compatible;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.CrossSectionElement;
import org.opentrafficsim.road.network.lane.object.sensor.AbstractSensor;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLightColor;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;

/**
 * Controller using a cycle time.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class CycleTimeLightController implements RampMeteringLightController
{

    /** Minimum red duration. */
    private static final Duration MIN_RED_TIME = Duration.instantiateSI(2.0);

    /** Whether the controller is enabled. */
    private boolean enabled = false;

    /** Time when red phase was started. */
    private Map<TrafficLight, Time> greenStarts = new LinkedHashMap<>();

    /** Cycle time. */
    private Duration cTime;

    /** Simulator. */
    private final OTSSimulatorInterface simulator;

    /** Traffic lights. */
    private final List<TrafficLight> trafficLights;

    /** Scheduled red event. */
    private Map<TrafficLight, SimEventInterface<Duration>> redEvents = new LinkedHashMap<>();

    /** Scheduled green event. */
    private Map<TrafficLight, SimEventInterface<Duration>> greenEvents = new LinkedHashMap<>();

    /**
     * @param simulator OTSSimulatorInterface; simulator
     * @param trafficLights List&lt;TrafficLight&gt;; traffic lights
     * @param compatible Compatible; GTU types that trigger the detector, and hence the light to red
     */
    public CycleTimeLightController(final OTSSimulatorInterface simulator, final List<TrafficLight> trafficLights,
            final Compatible compatible)
    {
        this.simulator = simulator;
        this.trafficLights = trafficLights;
        for (TrafficLight trafficLight : trafficLights)
        {
            Try.execute(() -> new RampMeteringSensor(trafficLight, simulator, compatible),
                    "Unexpected exception while creating a detector with a ramp metering traffic light.");
            this.greenStarts.put(trafficLight, Time.instantiateSI(Double.NEGATIVE_INFINITY));
        }
    }

    /** {@inheritDoc} */
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
     * @param cycleTime Duration; cycle time
     */
    @Override
    public void enable(final Duration cycleTime)
    {
        this.simulator.getLogger().always().info("Traffic light uses " + cycleTime);
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
     * @param trafficLight TrafficLight; traffic light
     */
    protected void setRed(final TrafficLight trafficLight)
    {
        this.redEvents.remove(trafficLight);
        this.simulator.getLogger().always().info("Traffic light set to RED");
        trafficLight.setTrafficLightColor(TrafficLightColor.RED);
    }

    /**
     * Sets the traffic light to green. Can be scheduled and remembers the green time.
     * @param trafficLight TrafficLight; traffic light
     */
    protected void setGreen(final TrafficLight trafficLight)
    {
        this.greenEvents.remove(trafficLight);
        this.greenStarts.put(trafficLight, this.simulator.getSimulatorAbsTime());
        this.simulator.getLogger().always().info("Traffic light set to GREEN");
        trafficLight.setTrafficLightColor(TrafficLightColor.GREEN);
    }

    /** Ramp metering sensor. */
    private class RampMeteringSensor extends AbstractSensor
    {

        /** */
        private static final long serialVersionUID = 20190618L;

        /** The traffic light. */
        private final TrafficLight trafficLight;

        /**
         * @param trafficLight TrafficLight; traffic light
         * @param simulator TimeDoubleUnit; simulator
         * @param detectedGTUTypes Compatible; GTU types
         * @throws NetworkException when the position on the lane is out of bounds
         */
        RampMeteringSensor(final TrafficLight trafficLight, final OTSSimulatorInterface simulator,
                final Compatible detectedGTUTypes) throws NetworkException
        {
            super(trafficLight.getId() + "_sensor", trafficLight.getLane(), trafficLight.getLongitudinalPosition(),
                    RelativePosition.FRONT, simulator, detectedGTUTypes);
            this.trafficLight = trafficLight;
        }

        /** {@inheritDoc} */
        @SuppressWarnings("synthetic-access")
        @Override
        protected void triggerResponse(final LaneBasedGTU gtu)
        {
            if (CycleTimeLightController.this.enabled && this.trafficLight.getTrafficLightColor().isGreen())
            {
                try
                {
                    // schedule green
                    Time minRedTime = CycleTimeLightController.this.simulator.getSimulatorAbsTime().plus(MIN_RED_TIME);
                    Time cycleRedTime = CycleTimeLightController.this.greenStarts.get(this.trafficLight)
                            .plus(CycleTimeLightController.this.cTime);
                    Time green;
                    if (minRedTime.ge(cycleRedTime))
                    {
                        getSimulator().getLogger().always().info("Traffic light set to RED");
                        this.trafficLight.setTrafficLightColor(TrafficLightColor.RED);
                        green = minRedTime;
                    }
                    else
                    {
                        getSimulator().getLogger().always().info("Traffic light set to YELLOW (RED over 'MIN_RED_TIME')");
                        this.trafficLight.setTrafficLightColor(TrafficLightColor.YELLOW);
                        CycleTimeLightController.this.redEvents.put(this.trafficLight,
                                CycleTimeLightController.this.simulator.scheduleEventRel(MIN_RED_TIME, this,
                                        CycleTimeLightController.this, "setRed", new Object[] {this.trafficLight}));
                        green = cycleRedTime;
                    }
                    CycleTimeLightController.this.greenEvents.put(this.trafficLight,
                            CycleTimeLightController.this.simulator.scheduleEventAbsTime(green, this,
                                    CycleTimeLightController.this, "setGreen", new Object[] {this.trafficLight}));
                }
                catch (SimRuntimeException exception)
                {
                    throw new RuntimeException(exception);
                }
            }
        }

        /** {@inheritDoc} */
        @Override
        public AbstractSensor clone(final CrossSectionElement newCSE, final OTSSimulatorInterface newSimulator)
                throws NetworkException
        {
            return null; // TODO: should be cloned as part of the ramp metering
        }

    }

}
