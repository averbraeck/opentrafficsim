package org.opentrafficsim.road.network.control.rampmetering;

import org.opentrafficsim.core.dsol.OTSSimulatorInterface;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.logger.SimLogger;

/**
 * Ramp metering. This consist of a {@code RampMeteringSwitch} and a {@code RampMeteringLightController}.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version May 29, 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class RampMetering
{

    /** Simulator. */
    private final OTSSimulatorInterface simulator;

    /** Ramp metering switch. */
    private final RampMeteringSwitch rampSwitch;

    /** Ramp metering light controller. */
    private final RampMeteringLightController rampLightController;

    /**
     * @param simulator OTSSimulatorInterface; simulator
     * @param rampSwitch RampMeteringSwitch; ramp metering switch
     * @param rampLightController RampMeteringLightController; ramp metering light controller
     */
    public RampMetering(final OTSSimulatorInterface simulator, final RampMeteringSwitch rampSwitch,
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
            SimLogger.always().info("Ramp-metering enabled.");
            this.rampLightController.enable(this.rampSwitch);
        }
        else
        {
            SimLogger.always().info("Ramp-metering disabled.");
            this.rampLightController.disable();
        }
        try
        {
            this.simulator.scheduleEventRel(this.rampSwitch.getInterval(), this, this, "control", null);
        }
        catch (SimRuntimeException exception)
        {
            throw new RuntimeException("Interval from ramp metering switch is not a valid positive duration.", exception);
        }
    }

}
