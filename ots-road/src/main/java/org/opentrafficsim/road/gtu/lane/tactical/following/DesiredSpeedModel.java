package org.opentrafficsim.road.gtu.lane.tactical.following;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Desired speed model.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface DesiredSpeedModel
{

    /**
     * Determines the desired speed.
     * @param parameters parameters
     * @param speedInfo info regarding the desired speed for car-following
     * @throws ParameterException if parameter exception occurs
     * @return desired speed
     */
    Speed desiredSpeed(Parameters parameters, SpeedLimitInfo speedInfo) throws ParameterException;

}
