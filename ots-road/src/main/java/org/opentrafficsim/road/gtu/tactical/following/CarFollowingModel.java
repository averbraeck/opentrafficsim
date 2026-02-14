package org.opentrafficsim.road.gtu.tactical.following;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeClass;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.perception.PerceptionIterable;
import org.opentrafficsim.road.gtu.perception.object.PerceivedObject;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Methods that a car-following model has to implement. The parameters are supplied to obtain parameters. The phrase
 * 'car-following model' is the commonly used and therefore intuitive name, but in actuality it is much more.
 * <ul>
 * <li>Following other vehicle types: van, bus, truck.</li>
 * <li>Following other GTU's: bicycle, pedestrian.</li>
 * <li>Free driving.</li>
 * <li>Approaching (theoretically different from following, usually the same formula).</li>
 * <li>Stopping for a traffic light, intersection conflict, etc,</li>
 * </ul>
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface CarFollowingModel extends DesiredHeadwayModel, DesiredSpeedModel, Initialisable
{

    /** Parameter type for car-following model. */
    ParameterTypeClass<CarFollowingModel> CAR_FOLLOWING_MODEL =
            new ParameterTypeClass<>("cf.model", "car-following model", CarFollowingModel.class, IdmPlus.class);

    /**
     * Determination of car-following acceleration, possibly based on multiple leaders. The implementation should be able to
     * deal with:<br>
     * <ul>
     * <li>The current speed being higher than the desired speed.</li>
     * <li>The headway being negative.</li>
     * </ul>
     * @param parameters parameters
     * @param speed current speed
     * @param speedLimitInfo info regarding the desired speed for car-following
     * @param leaders set of leader headways and speeds, ordered by headway (closest first)
     * @throws ParameterException if parameter exception occurs
     * @return car-following acceleration
     */
    Acceleration followingAcceleration(Parameters parameters, Speed speed, SpeedLimitInfo speedLimitInfo,
            PerceptionIterable<? extends PerceivedObject> leaders) throws ParameterException;

    /**
     * Return the name of the car-following model.
     * @return name of the car-following model
     */
    String getName();

    /**
     * Return the complete name of the car-following model.
     * @return complete name of the car-following model
     */
    String getLongName();

    @Override
    default void init(final LaneBasedGtu gtu)
    {
        //
    }

}
