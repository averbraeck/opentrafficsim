package org.opentrafficsim.road.network.control.rampmetering;

import java.util.List;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLightColor;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.logger.SimLogger;
import nl.tudelft.simulation.dsol.simtime.SimTimeDoubleUnit;

/**
 * Abstract ramp metering traffic light controller. Subclasses only need to provide the green and red time to
 * {@code startCycle()}.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 12 jun. 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractMeteringLightController implements RampMeteringLightController
{

    /** Whether the controller is enabled. */
    private boolean enabled = false;

    /** Last event. */
    private SimEventInterface<SimTimeDoubleUnit> lastEvent;

    /** Simulator. */
    private final OTSSimulatorInterface simulator;

    /** Traffic lights. */
    private final List<TrafficLight> trafficLights;

    /**
     * @param simulator OTSSimulatorInterface; simulator
     * @param trafficLights List&lt;TrafficLight&gt;; traffic lights
     */
    public AbstractMeteringLightController(final OTSSimulatorInterface simulator, final List<TrafficLight> trafficLights)
    {
        this.simulator = simulator;
        this.trafficLights = trafficLights;
    }

    /** {@inheritDoc} */
    @Override
    public void disable()
    {
        this.enabled = false;
        if (this.lastEvent != null)
        {
            this.simulator.cancelEvent(this.lastEvent);
        }
        for (TrafficLight trafficLight : this.trafficLights)
        {
            trafficLight.setTrafficLightColor(TrafficLightColor.GREEN);
        }
    }

    /**
     * Starts the cycle.
     * @param redTime Duration; red time
     * @param greenTime Duration; green time
     */
    protected void startCycle(final Duration redTime, final Duration greenTime)
    {
        SimLogger.always().info("Traffic light use " + redTime + " (red) and " + greenTime + " (green)");
        if (this.lastEvent != null)
        {
            this.simulator.cancelEvent(this.lastEvent);
        }
        this.enabled = true;
        for (TrafficLight trafficLight : this.trafficLights)
        {
            trafficLight.setTrafficLightColor(TrafficLightColor.RED);
        }
        try
        {
            this.lastEvent = this.simulator.scheduleEventRel(redTime, this, this, "switchLights",
                    new Object[] { TrafficLightColor.GREEN, TrafficLightColor.RED, greenTime, redTime });
        }
        catch (SimRuntimeException exception)
        {
            throw new RuntimeException("Negative red time.", exception);
        }
    }

    /**
     * Switches the color of the traffic lights.
     * @param color1 TrafficLightColor; color 1
     * @param color2 TrafficLightColor; color 2
     * @param duration1 Duration; duration 1
     * @param duration2 Duration; duration 2
     */
    protected void switchLights(final TrafficLightColor color1, final TrafficLightColor color2, final Duration duration1,
            final Duration duration2)
    {
        if (!this.enabled && this.lastEvent != null)
        {
            this.simulator.cancelEvent(this.lastEvent);
        }
        this.lastEvent = null;
        for (TrafficLight trafficLight : this.trafficLights)
        {
            trafficLight.setTrafficLightColor(color1);
        }
        try
        {
            this.lastEvent = this.simulator.scheduleEventRel(duration1, this, this, "switchLights",
                    new Object[] { color2, color1, duration2, duration1 }); // Note this switches the order
        }
        catch (SimRuntimeException exception)
        {
            throw new RuntimeException("Negative green or red time.", exception);
        }
    }

}
