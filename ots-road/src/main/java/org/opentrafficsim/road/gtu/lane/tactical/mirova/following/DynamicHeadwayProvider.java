package org.opentrafficsim.road.gtu.lane.tactical.mirova.following;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;

/**
 * Interface to expose the dynamic desired headway capabilities of a car-following model.
 * <p>
 * In the MiRoVA framework, cognitive layers (such as the Context or ActionState) often need to calculate relaxation deficits.
 * By implementing this interface, a {@link CarFollowingModel} can agnostically expose its internal dynamic headway calculations
 * (which include approaching speeds) without breaking encapsulation or tying the cognitive logic to a specific model.
 * </p>
 * <p>
 * Copyright (c) 2026 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public interface DynamicHeadwayProvider extends CarFollowingModel
{
    /**
     * Calculates the dynamic desired headway based on the current state and parameters.
     * <p>
     * Unlike the static desired headway, the dynamic headway considers the relative speed to the leader, providing a realistic
     * target distance for gap acceptance and relaxation.
     * </p>
     * @param parameters Parameters; the parameter set of the GTU.
     * @param speed Speed; the current speed of the ego GTU.
     * @param desiredHeadway Length; the static desired equilibrium headway.
     * @param leaderSpeed Speed; the speed of the leading vehicle.
     * @return Length; the calculated dynamic desired headway (often referred to as s*).
     * @throws ParameterException if a required parameter cannot be retrieved.
     */
    Length calculateDynamicDesiredHeadway(Parameters parameters, Speed speed, Length desiredHeadway, Speed leaderSpeed)
            throws ParameterException;
}
