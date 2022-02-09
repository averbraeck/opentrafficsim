package org.opentrafficsim.road.gtu.lane.tactical.following;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Desired speed model.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 1 nov. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface DesiredSpeedModel
{

    /**
     * Determines the desired speed.
     * @param parameters Parameters; parameters
     * @param speedInfo SpeedLimitInfo; info regarding the desired speed for car-following
     * @throws ParameterException if parameter exception occurs
     * @return desired speed
     */
    Speed desiredSpeed(Parameters parameters, SpeedLimitInfo speedInfo) throws ParameterException;

}
