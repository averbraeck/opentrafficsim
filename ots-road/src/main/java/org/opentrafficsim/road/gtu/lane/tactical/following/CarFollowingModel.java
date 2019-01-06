package org.opentrafficsim.road.gtu.lane.tactical.following;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeClass;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionIterable;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
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
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 22, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface CarFollowingModel extends DesiredHeadwayModel, DesiredSpeedModel, Initialisable
{

    /** Parameter type for car-following model. */
    ParameterTypeClass<CarFollowingModel> CAR_FOLLOWING_MODEL = new ParameterTypeClass<>("cf.model", "car-following model",
            ParameterTypeClass.getValueClass(CarFollowingModel.class));

    /**
     * Determination of car-following acceleration, possibly based on multiple leaders. The implementation should be able to
     * deal with:<br>
     * <ul>
     * <li>The current speed being higher than the desired speed.</li>
     * <li>The headway being negative.</li>
     * </ul>
     * @param parameters Parameters; parameters
     * @param speed Speed; current speed
     * @param speedLimitInfo SpeedLimitInfo; info regarding the desired speed for car-following
     * @param leaders PerceptionIterable&lt;? extends Headway&gt;; set of leader headways and speeds, ordered by headway
     *            (closest first)
     * @throws ParameterException if parameter exception occurs
     * @return car-following acceleration
     */
    Acceleration followingAcceleration(Parameters parameters, Speed speed, SpeedLimitInfo speedLimitInfo,
            PerceptionIterable<? extends Headway> leaders) throws ParameterException;

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

    /** {@inheritDoc} */
    @Override
    default void init(LaneBasedGTU gtu)
    {
        //
    }

}
