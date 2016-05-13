package org.opentrafficsim.road.gtu.lane.tactical.following;

import java.util.SortedMap;
import java.util.TreeMap;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.Throw;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitTypeSpeedLegal;
import org.opentrafficsim.road.network.speed.SpeedLimitTypes;

/**
 * Default implementation where desired speed and headway are pre-calculated for car-following.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Apr 2016 <br>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractCarFollowingModel implements CarFollowingModel
{

    /**
     * Forwards the calculation to a similar method with desired speed and desired (equilibrium) headway pre-calculated.
     * Additionally, if the headway to the (first) leader is negative, <tt>Double.NEGATIVE_INFINITY</tt> is returned as an
     * 'inappropriate' acceleration, since car-following models are then undefined. This may for example occur when checking a
     * gap in an adjacent lane for lane changing. It is then up to the client to decide what to do. E.g. limit deceleration to
     * an extent depending on the circumstances, or divert from a certain behavior.
     * @param behavioralCharacteristics behavioral characteristics
     * @param speed current speed
     * @param speedLimitInfo info regarding the desired speed for car-following
     * @param leaders set of leader headways and speeds, ordered by headway (closest first)
     * @return car-following acceleration
     * @throws ParameterException if parameter exception occurs
     * @throws NullPointerException if any input is null
     */
    @Override
    public final Acceleration followingAcceleration(final BehavioralCharacteristics behavioralCharacteristics,
        final Speed speed, final SpeedLimitInfo speedLimitInfo, final SortedMap<Length, Speed> leaders)
        throws ParameterException
    {
        Throw.whenNull(behavioralCharacteristics, "Behavioral characteristics may not be null.");
        Throw.whenNull(speed, "Speed may not be null.");
        Throw.whenNull(speedLimitInfo, "Speed limit info may not be null.");
        Throw.whenNull(leaders, "Leaders may not be null.");
        // Catch negative headway
        if (leaders.firstKey().si <= 0)
        {
            return new Acceleration(Double.NEGATIVE_INFINITY, AccelerationUnit.SI);
        }
        // Forward to method with desired speed and headway predetermined by this car-following model.
        return followingAcceleration(behavioralCharacteristics, speed, desiredSpeed(behavioralCharacteristics,
            speedLimitInfo), desiredHeadway(behavioralCharacteristics, speed), leaders);
    }

    /**
     * Determination of car-following acceleration, possibly based on multiple leaders.
     * @param behavioralCharacteristics behavioral characteristics
     * @param speed current speed
     * @param desiredSpeed desired speed
     * @param desiredHeadway desired headway
     * @param leaders set of leader headways (guaranteed positive) and speeds, ordered by headway (closest first)
     * @return car-following acceleration
     * @throws ParameterException if parameter exception occurs
     */
    protected abstract Acceleration followingAcceleration(BehavioralCharacteristics behavioralCharacteristics, Speed speed,
        Speed desiredSpeed, Length desiredHeadway, SortedMap<Length, Speed> leaders) throws ParameterException;

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:designforextension")
    @Override
    public String toString()
    {
        return getLongName();
    }

    /**
     * Returns the minimum speed of speed limit types MAX_LEGAL_VEHICLE_SPEED, ROAD_CLASS, FIXED_SIGN and DYNAMIC_SIGN. This
     * method may be overridden by subclasses to implement additional behavior regarding legal speed limits.
     * @param speedLimitInfo speed limit info
     * @return minimum of speed of speed limit types MAX_LEGAL_VEHICLE_SPEED, ROAD_CLASS, FIXED_SIGN and DYNAMIC_SIGN
     * @throws NullPointerException if speed limit info is null
     */
    @SuppressWarnings("checkstyle:designforextension")
    public Speed getLegalSpeedLimit(final SpeedLimitInfo speedLimitInfo)
    {
        Throw.whenNull(speedLimitInfo, "Speed limit info may not be null.");
        Speed result = new Speed(Double.POSITIVE_INFINITY, SpeedUnit.SI);
        if (speedLimitInfo.containsType(SpeedLimitTypes.FIXED_SIGN)
            || speedLimitInfo.containsType(SpeedLimitTypes.DYNAMIC_SIGN))
        {
            for (SpeedLimitTypeSpeedLegal lsl : new SpeedLimitTypeSpeedLegal[] {SpeedLimitTypes.MAX_LEGAL_VEHICLE_SPEED,
                SpeedLimitTypes.FIXED_SIGN, SpeedLimitTypes.DYNAMIC_SIGN})
            {
                if (speedLimitInfo.containsType(lsl))
                {
                    Speed s = speedLimitInfo.getSpeedInfo(lsl);
                    result = s.lt(result) ? s : result;
                }
            }
        }
        else
        {
            for (SpeedLimitTypeSpeedLegal lsl : new SpeedLimitTypeSpeedLegal[] {SpeedLimitTypes.MAX_LEGAL_VEHICLE_SPEED,
                SpeedLimitTypes.ROAD_CLASS})
            {
                if (speedLimitInfo.containsType(lsl))
                {
                    Speed s = speedLimitInfo.getSpeedInfo(lsl);
                    result = s.lt(result) ? s : result;
                }
            }
        }
        return result;
    }

    /**
     * Returns the speed of speed limit type MAX_VEHICLE_SPEED. This method may be overridden by subclasses to implement
     * additional behavior regarding maximum vehicle speed limits.
     * @param speedLimitInfo speed limit info
     * @return speed of speed limit type MAX_VEHICLE_SPEED
     * @throws NullPointerException if speed limit info is null
     */
    @SuppressWarnings("checkstyle:designforextension")
    public Speed getMaximumVehicleSpeed(final SpeedLimitInfo speedLimitInfo)
    {
        Throw.whenNull(speedLimitInfo, "Speed limit info may not be null.");
        return speedLimitInfo.getSpeedInfo(SpeedLimitTypes.MAX_VEHICLE_SPEED);
    }

    /**
     * Returns an acceleration based on the car-following model in order to adjust the speed to a given value at some location
     * ahead. This is done by placing a virtual vehicle somewhere near the location. Both the location and speed of this virtual
     * vehicle are dynamically adjusted to resemble a car-following situation. To explain, first consider the situation where a
     * virtual vehicle is placed at the target speed and such that the equilibrium headway is in line with the location:
     * 
     * <pre>
     * 
     *  ___    location of target speed -->|        ___
     * |___|<--------------s--------------> <--h-->|___| >>> vTar
     * </pre>
     * 
     * Here, {@code s} is the distance to the target speed, and {@code h} is the desired headway if the vehicle would drive at
     * the target speed {@code vTar}.<br>
     * <br>
     * In this way car-following models will first underestimate the required deceleration, as the virtual vehicle is actually
     * stationary and does not move with {@code vTar} at all. Because of this underestimation, strong deceleration is required
     * later. This behavior is not in line with the sensitivity parameters of the car-following model.<br>
     * <br>
     * To correct for the fact that the virtual vehicle is actually not moving, the speed difference should be larger, i.e. the
     * speed of the virtual vehicle {@code vTar'} should be lower. We require:
     * <ul>
     * <li>if {@code v = vTar} then {@code vTar' = vTar}, otherwise there is an incentive to accelerate or decelerate for no
     * good reason</li>
     * <li>if {@code vTar ~ 0} then {@code vTar' ~ 0}, as car-following models are suitable for stopping and need no additional
     * incentive to decelerate in such cases</li>
     * <li>if {@code 0 < vTar < v} then {@code vTar' < vTar}, introducing additional deceleration to compensate for the fact
     * that the virtual vehicle does not move
     * </ul>
     * These requirements are met by {@code vTar' = vTar * (vTar/v) = vTar^2/v}.<br>
     * <br>
     * Furthermore, if {@code v < vTar} we get {@code vTar' > vTar} leading to additional acceleration. Acceleration is then
     * appropriate, and possibly limited by a free term in the car-following model.<br>
     * <br>
     * The virtual vehicle is thus placed with speed {@code vTar'} at a distance {@code s + h'} where {@code h'} is the desired
     * headway if the vehicle would drive at speed {@code vTar'}. Both {@code vTar'} and {@code h'} depend on the current speed
     * of the vehicle, so the virtual vehicle in this case actually moves, but not with {@code vTar}.<br>
     * <br>
     * This approach has been tested with the IDM+ to deliver decelerations in line with the parameters. On a plane with initial
     * speed ranging from 0 to 33.33m/s and a target speed in 300m also ranging from 0 to 33.33m/s, strongest deceleration is
     * equal to the car-following model stopping from 33.33m/s to a stand-still vehicle in 300m (+ stopping distance of 3m).
     * Throughout the plane the maximum deceleration of each scenario is close to this value, unless the initial speed is so
     * low, and the target speed is so high, that such levels of deceleration are never required.<br>
     * <br>
     * @param behavioralCharacteristics behavioral characteristics
     * @param speed current speed
     * @param speedLimitInfo info regarding the desired speed for car-following
     * @param distance distance to the location of the target speed
     * @param targetSpeed target speed
     * @return acceleration acceleration based on the car-following model in order to adjust the speed
     * @throws ParameterException if parameter exception occurs
     * @throws NullPointerException if any input is null
     * @throws IllegalArgumentException if the distance or target speed is not at least 0
     */
    public final Acceleration approachTargetSpeed(final BehavioralCharacteristics behavioralCharacteristics,
        final Speed speed, final SpeedLimitInfo speedLimitInfo, final Length distance, final Speed targetSpeed)
        throws ParameterException
    {
        Throw.whenNull(behavioralCharacteristics, "Behavioral characteristics may not be null.");
        Throw.whenNull(speed, "Speed may not be null.");
        Throw.whenNull(speedLimitInfo, "Speed limit info may not be null.");
        Throw.whenNull(distance, "Distance may not be null");
        Throw.whenNull(targetSpeed, "Target speed may not be null");
        Throw.when(distance.si < 0, IllegalArgumentException.class, "Distance must be at least 0.");
        Throw.when(targetSpeed.si < 0, IllegalArgumentException.class, "Target speed must be at least 0.");
        // adjust speed of virtual vehicle to add deceleration incentive as the virtual vehicle does not move
        Speed virtualSpeed;
        if (speed.si > 0)
        {
            virtualSpeed = new Speed(targetSpeed.si * targetSpeed.si / speed.si, SpeedUnit.SI);
        }
        else
        {
            virtualSpeed = new Speed(Double.MAX_VALUE, SpeedUnit.SI);
        }
        // set distance in line with equilibrium headway at virtual speed
        Length virtualDistance = distance.plus(desiredHeadway(behavioralCharacteristics, virtualSpeed));
        // calculate acceleration towards virtual vehicle with car-following model
        SortedMap<Length, Speed> leaders = new TreeMap<>();
        leaders.put(virtualDistance, virtualSpeed);
        return followingAcceleration(behavioralCharacteristics, speed, desiredSpeed(behavioralCharacteristics,
            speedLimitInfo), desiredHeadway(behavioralCharacteristics, speed), leaders);
    }

}
