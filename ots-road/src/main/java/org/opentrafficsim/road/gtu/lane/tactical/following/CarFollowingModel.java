package org.opentrafficsim.road.gtu.lane.tactical.following;

import java.util.SortedMap;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Methods that a car-following model has to implement. The behavioral characteristics are supplied to obtain parameters. The
 * phrase 'car-following model' is the commonly used and therefore intuitive name, but in actuality it is much more.
 * <ul>
 * <li>Following other vehicle types: van, bus, truck.</li>
 * <li>Following other GTU's: bicycle, pedestrian.</li>
 * <li>Free driving.</li>
 * <li>Approaching (theoretically different from following, usually the same formula).</li>
 * <li>Stopping for a traffic light, intersection conflict, etc,</li>
 * </ul>
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 22, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface CarFollowingModel
{

    /**
     * Determines the desired speed.
     * @param behavioralCharacteristics behavioral characteristics
     * @param speedInfo info regarding the desired speed for car-following
     * @throws ParameterException if parameter exception occurs
     * @return desired speed
     */
    Speed desiredSpeed(BehavioralCharacteristics behavioralCharacteristics, SpeedLimitInfo speedInfo) throws ParameterException;

    /**
     * Determines the desired headway in equilibrium conditions, i.e. no speed difference with the leader.
     * @param behavioralCharacteristics behavioral characteristics
     * @param speed speed to determine the desired headway at
     * @throws ParameterException if parameter exception occurs
     * @return desired headway
     */
    Length desiredHeadway(BehavioralCharacteristics behavioralCharacteristics, Speed speed) throws ParameterException;

    /**
     * Determination of car-following acceleration, possibly based on multiple leaders. The implementation should be able to
     * deal with:<br>
     * <ul>
     * <li>The current speed being higher than the desired speed.</li>
     * <li>The headway being negative.</li>
     * </ul>
     * @param behavioralCharacteristics behavioral characteristics
     * @param speed current speed
     * @param speedLimitInfo info regarding the desired speed for car-following
     * @param leaders set of leader headways and speeds, ordered by headway (closest first)
     * @throws ParameterException if parameter exception occurs
     * @return car-following acceleration
     */
    Acceleration followingAcceleration(BehavioralCharacteristics behavioralCharacteristics, Speed speed,
        SpeedLimitInfo speedLimitInfo, SortedMap<Length, Speed> leaders) throws ParameterException;

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

}
