package org.opentrafficsim.road.network.control.rampmetering;

import org.opentrafficsim.core.dsol.OtsSimulatorInterface;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Ramp metering. This consist of a {@code RampMeteringSwitch} and a {@code RampMeteringLightController}.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class RampMetering
{

    /** Simulator. */
    private final OtsSimulatorInterface simulator;

    /** Ramp metering switch. */
    private final RampMeteringSwitch rampSwitch;

    /** Ramp metering light controller. */
    private final RampMeteringLightController rampLightController;

    /**
     * @param simulator simulator
     * @param rampSwitch ramp metering switch
     * @param rampLightController ramp metering light controller
     */
    public RampMetering(final OtsSimulatorInterface simulator, final RampMeteringSwitch rampSwitch,
            final RampMeteringLightController rampLightController)
    {
        this.simulator = simulator;
        this.rampSwitch = rampSwitch;
        this.rampLightController = rampLightController;
        control();
    }

    /**
     * Executes control in a cyclic manner.
     */
    private void control()
    {
        if (this.rampSwitch.isEnabled())
        {
            this.simulator.getLogger().always().info("Ramp-metering enabled.");
            this.rampLightController.enable(this.rampSwitch.getCycleTime());
        }
        else
        {
            this.simulator.getLogger().always().info("Ramp-metering disabled.");
            this.rampLightController.disable();
        }
        try
        {
            this.simulator.scheduleEventRel(this.rampSwitch.getInterval(), this, "control", null);
        }
        catch (SimRuntimeException exception)
        {
            throw new RuntimeException("Interval from ramp metering switch is not a valid positive duration.", exception);
        }
    }

}
