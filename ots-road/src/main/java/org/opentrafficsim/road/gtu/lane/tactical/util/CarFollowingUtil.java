package org.opentrafficsim.road.gtu.lane.tactical.util;

import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Static methods regarding car-following for composition in tactical planners.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version May 23, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class CarFollowingUtil
{

    /**
     * Do not instantiate.
     */
    private CarFollowingUtil()
    {
        //
    }

    /**
     * Follow a set of headway GTUs.
     * @param carFollowingModel car-following model
     * @param behavioralCharacteristics behavioral characteristics
     * @param speed current speed
     * @param speedLimitInfo speed limit info
     * @param leaders leaders
     * @return acceleration for following the leader
     * @throws ParameterException if a parameter is not given or out of bounds
     */
    public static Acceleration followLeaders(final CarFollowingModel carFollowingModel,
            final BehavioralCharacteristics behavioralCharacteristics, final Speed speed, final SpeedLimitInfo speedLimitInfo,
            final SortedSet<HeadwayGTU> leaders) throws ParameterException
    {
        SortedMap<Length, Speed> leaderMap = new TreeMap<>();
        for (HeadwayGTU headwayGTU : leaders)
        {
            leaderMap.put(headwayGTU.getDistance(), headwayGTU.getSpeed());
        }
        return carFollowingModel.followingAcceleration(behavioralCharacteristics, speed, speedLimitInfo, leaderMap);
    }

    /**
     * Stop within given distance.
     * @param carFollowingModel car-following model
     * @param behavioralCharacteristics behavioral characteristics
     * @param speed current speed
     * @param speedLimitInfo speed limit info
     * @param distance distance to stop over
     * @return acceleration to stop over distance
     * @throws ParameterException if a parameter is not given or out of bounds
     */
    public static Acceleration stop(final CarFollowingModel carFollowingModel,
            final BehavioralCharacteristics behavioralCharacteristics, final Speed speed, final SpeedLimitInfo speedLimitInfo,
            final Length distance) throws ParameterException
    {
        SortedMap<Length, Speed> leaderMap = new TreeMap<>();
        leaderMap.put(distance, Speed.ZERO);
        return carFollowingModel.followingAcceleration(behavioralCharacteristics, speed, speedLimitInfo, leaderMap);
    }

    /**
     * Return constant acceleration in order to stop in specified distance. The car-following model is used to determine the
     * stopping distance (i.e. distance remaining at stand still, e.g. 1-3m).
     * @param carFollowingModel car-following model
     * @param behavioralCharacteristics behavioral characteristics
     * @param speed current speed
     * @param distance distance to stop over
     * @return constant acceleration in order to stop in specified distance
     * @throws ParameterException on missing parameter
     */
    public static Acceleration constantAccelerationStop(final CarFollowingModel carFollowingModel,
            final BehavioralCharacteristics behavioralCharacteristics, final Speed speed, final Length distance)
            throws ParameterException
    {
        Length s0 = carFollowingModel.desiredHeadway(behavioralCharacteristics, Speed.ZERO);
        return new Acceleration(-0.5 * speed.si * speed.si / (distance.si - s0.si), AccelerationUnit.SI);
    }

    /**
     * Calculate free acceleration.
     * @param carFollowingModel car-following model
     * @param behavioralCharacteristics behavioral characteristics
     * @param speed current speed
     * @param speedLimitInfo speed limit info
     * @return acceleration free acceleration
     * @throws ParameterException if a parameter is not given or out of bounds
     */
    public static Acceleration freeAcceleration(final CarFollowingModel carFollowingModel,
            final BehavioralCharacteristics behavioralCharacteristics, final Speed speed, final SpeedLimitInfo speedLimitInfo)
            throws ParameterException
    {
        SortedMap<Length, Speed> leaderMap = new TreeMap<>();
        return carFollowingModel.followingAcceleration(behavioralCharacteristics, speed, speedLimitInfo, leaderMap);
    }

}
