package org.opentrafficsim.road.network.control.rampmetering;

import org.djunits.value.vdouble.scalar.Duration;

/**
 * Determines whether the controller should be on or off.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version May 29, 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface RampMeteringSwitch
{
    
    /**
     * Returns the control interval.
     * @return Duration; the control interval
     */
    Duration getInterval();
    
    /**
     * Evaluates whether the ramp metering should be enabled.
     * @return boolean; whether the ramp metering should be enabled
     */
    boolean isEnabled();
    
    /**
     * Returns the cycle time.
     * @return Duration; the cycle time
     */
    Duration getCycleTime();

}
