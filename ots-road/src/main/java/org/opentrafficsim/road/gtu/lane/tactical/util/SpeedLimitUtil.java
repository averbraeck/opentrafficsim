package org.opentrafficsim.road.gtu.lane.tactical.util;

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
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypeAcceleration;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitProspect;
import org.opentrafficsim.road.network.speed.SpeedLimitType;
import org.opentrafficsim.road.network.speed.SpeedLimitTypeSpeedLegal;
import org.opentrafficsim.road.network.speed.SpeedLimitTypes;

/**
 * Static methods regarding speed limits for composition in tactical planners.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version May 20, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class SpeedLimitUtil
{

    /**
     * Do not instantiate.
     */
    private SpeedLimitUtil()
    {
        //
    }

    /** Maximum comfortable acceleration in the lateral direction. */
    public static final ParameterTypeAcceleration A_LAT = new ParameterTypeAcceleration("aLat",
        "Maximum comfortable lateral acceleration", new Acceleration(1.0, AccelerationUnit.SI));

    /**
     * Returns the minimum speed of the applicable speed limit types MAX_LEGAL_VEHICLE_SPEED, ROAD_CLASS, FIXED_SIGN and
     * DYNAMIC_SIGN. ROAD_CLASS is only used if FIXED_SIGN and DYNAMIC_SIGN are not present. This method may be overridden by
     * subclasses to implement additional behavior regarding legal speed limits.
     * @param speedLimitInfo speed limit info
     * @return minimum of speed of speed limit types
     * @throws NullPointerException if speed limit info is null
     */
    public static Speed getLegalSpeedLimit(final SpeedLimitInfo speedLimitInfo)
    {
        Throw.whenNull(speedLimitInfo, "Speed limit info may not be null.");
        SpeedLimitTypeSpeedLegal[] speedLimitTypes;
        if (speedLimitInfo.containsType(SpeedLimitTypes.FIXED_SIGN)
            || speedLimitInfo.containsType(SpeedLimitTypes.DYNAMIC_SIGN))
        {
            speedLimitTypes =
                new SpeedLimitTypeSpeedLegal[] {SpeedLimitTypes.MAX_LEGAL_VEHICLE_SPEED, SpeedLimitTypes.FIXED_SIGN,
                    SpeedLimitTypes.DYNAMIC_SIGN};
        }
        else
        {
            speedLimitTypes =
                new SpeedLimitTypeSpeedLegal[] {SpeedLimitTypes.MAX_LEGAL_VEHICLE_SPEED, SpeedLimitTypes.ROAD_CLASS};
        }
        Speed result = new Speed(Double.POSITIVE_INFINITY, SpeedUnit.SI);
        for (SpeedLimitTypeSpeedLegal lsl : speedLimitTypes)
        {
            if (speedLimitInfo.containsType(lsl))
            {
                Speed s = speedLimitInfo.getSpeedInfo(lsl);
                result = s.lt(result) ? s : result;
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
    public static Speed getMaximumVehicleSpeed(final SpeedLimitInfo speedLimitInfo)
    {
        Throw.whenNull(speedLimitInfo, "Speed limit info may not be null.");
        return speedLimitInfo.getSpeedInfo(SpeedLimitTypes.MAX_VEHICLE_SPEED);
    }

    /**
     * Acceleration for speed limit transitions. This implementation decelerates before curves and speed bumps. For this it uses
     * {@code approachTargetSpeed()} of the abstract car-following model implementation. All remaining transitions happen in the
     * default manner, i.e. deceleration and acceleration after the speed limit change and governed by the car-following model.
     * @param behavioralCharacteristics behavioral characteristics
     * @param speed current speed
     * @param speedLimitProspect speed limit prospect
     * @param carFollowingModel car following model
     * @return acceleration for speed limit transitions
     * @throws ParameterException if a required parameter is not found
     */
    public static Acceleration considerSpeedLimitTransitions(final BehavioralCharacteristics behavioralCharacteristics,
        final Speed speed, final SpeedLimitProspect speedLimitProspect, final CarFollowingModel carFollowingModel)
        throws ParameterException
    {
        Acceleration out = new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
        SpeedLimitInfo currentSpeedLimitInfo = speedLimitProspect.getSpeedLimitInfo(Length.ZERO);

        // decelerate for curves and speed bumps
        for (SpeedLimitType<?> speedLimitType : new SpeedLimitType[] {SpeedLimitTypes.CURVATURE, SpeedLimitTypes.SPEED_BUMP})
        {
            for (Length distance : speedLimitProspect.getDownstreamDistances(speedLimitType))
            {
                SpeedLimitInfo speedLimitInfo = speedLimitProspect.buildSpeedLimitInfo(distance, speedLimitType);
                Speed targetSpeed = carFollowingModel.desiredSpeed(behavioralCharacteristics, speedLimitInfo);
                Acceleration a =
                    SpeedLimitUtil.approachTargetSpeed(carFollowingModel, behavioralCharacteristics, speed,
                        currentSpeedLimitInfo, distance, targetSpeed);
                if (a.lt(out))
                {
                    out = a;
                }
            }
        }

        // For lower legal speed limits (road class, fixed sign, dynamic sign), we assume that the car-following model will
        // apply some reasonable deceleration after the change. For higher speed limits, we assume car-following acceleration
        // after the change.

        return out;
    }
    
    /**
     * Returns an acceleration based on the car-following model in order to adjust the speed to a given value at some location
     * ahead. This is done by placing a virtual vehicle somewhere near the location. Both the location and speed of this virtual
     * vehicle are dynamically adjusted to resemble a car-following situation. To explain, first consider the situation where a
     * virtual vehicle is placed at the target speed and such that the equilibrium headway is in line with the location:
     * 
     * <pre>
     * 
     *  ___    location of target speed --)|        ___
     * |___|(--------------s--------------) (--h--)|___| ))) vTar
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
     * @param carFollowingModel car-following model to use
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
    public static Acceleration approachTargetSpeed(final CarFollowingModel carFollowingModel,
        final BehavioralCharacteristics behavioralCharacteristics, final Speed speed, final SpeedLimitInfo speedLimitInfo,
        final Length distance, final Speed targetSpeed) throws ParameterException
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
        Length virtualDistance = distance.plus(carFollowingModel.desiredHeadway(behavioralCharacteristics, virtualSpeed));
        // calculate acceleration towards virtual vehicle with car-following model
        SortedMap<Length, Speed> leaders = new TreeMap<>();
        leaders.put(virtualDistance, virtualSpeed);
        return carFollowingModel.followingAcceleration(behavioralCharacteristics, speed, speedLimitInfo, leaders);
    }

}
