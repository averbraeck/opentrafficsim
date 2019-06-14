package org.opentrafficsim.road.network.control.rampmetering;

/**
 * Interface for controllers of traffic lights for ramp metering.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version May 29, 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface RampMeteringLightController
{

    /**
     * Disables the traffic lights.
     */
    void disable();
    
    /**
     * Enables, or keep enabled, the controller.
     * @param rampMeteringSwitch RampMeteringSwitch; ramp metering switch which supplies the red time
     */
    void enable(RampMeteringSwitch rampMeteringSwitch);
    
}
