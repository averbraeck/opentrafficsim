package org.opentrafficsim.road.gtu.tactical;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.road.gtu.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Provides bundled and easy-access information that tactical models and their components need regarding the ego-vehicle or a
 * perceived GTU.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface TacticalContext
{

    /**
     * Returns the parameters.
     * @return parameters
     */
    Parameters getParameters();

    /**
     * Returns the car-following model.
     * @return car-following model
     */
    CarFollowingModel getCarFollowingModel();

    /**
     * Returns the speed limit info at the current location.
     * @return speed limit info
     */
    SpeedLimitInfo getSpeedLimitInfo();

    /**
     * Return ego length.
     * @return ego length
     */
    Length getLength();

    /**
     * Return ego width.
     * @return ego width
     */
    Length getWidth();

    /**
     * Return ego speed.
     * @return ego speed
     */
    Speed getSpeed();

    /**
     * Return ego acceleration.
     * @return ego acceleration
     */
    Acceleration getAcceleration();

}
