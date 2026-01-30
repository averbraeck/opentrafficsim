package org.opentrafficsim.road.gtu.lane.tactical.util;

import java.util.UUID;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionIterable;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionIterableSet;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedObject;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedObject.Kinematics;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedObject.Kinematics.Overlap;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedObject.ObjectType;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedObjectBase;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Static methods regarding car-following for composition in tactical planners.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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
     * @param parameters parameters
     * @param speed current speed
     * @param speedLimitInfo speed limit info
     * @param distance distance
     * @param leaderSpeed speed of the leader
     * @return acceleration for following the leader
     * @throws ParameterException if a parameter is not given or out of bounds
     */
    public static Acceleration followSingleLeader(final CarFollowingModel carFollowingModel, final Parameters parameters,
            final Speed speed, final SpeedLimitInfo speedLimitInfo, final Length distance, final Speed leaderSpeed)
            throws ParameterException
    {
        return carFollowingModel.followingAcceleration(parameters, speed, speedLimitInfo, createLeader(distance, leaderSpeed));
    }

    /**
     * Follow a set of headway GTUs.
     * @param carFollowingModel car-following model
     * @param parameters parameters
     * @param speed current speed
     * @param speedLimitInfo speed limit info
     * @param leader leader
     * @return acceleration for following the leader
     * @throws ParameterException if a parameter is not given or out of bounds
     */
    public static Acceleration followSingleLeader(final CarFollowingModel carFollowingModel, final Parameters parameters,
            final Speed speed, final SpeedLimitInfo speedLimitInfo, final PerceivedObject leader) throws ParameterException
    {
        return carFollowingModel.followingAcceleration(parameters, speed, speedLimitInfo, new PerceptionIterableSet<>(leader));
    }

    /**
     * Stop within given distance.
     * @param carFollowingModel car-following model
     * @param parameters parameters
     * @param speed current speed
     * @param speedLimitInfo speed limit info
     * @param distance distance to stop over
     * @return acceleration to stop over distance
     * @throws ParameterException if a parameter is not given or out of bounds
     */
    public static Acceleration stop(final CarFollowingModel carFollowingModel, final Parameters parameters, final Speed speed,
            final SpeedLimitInfo speedLimitInfo, final Length distance) throws ParameterException
    {
        return carFollowingModel.followingAcceleration(parameters, speed, speedLimitInfo, createLeader(distance, Speed.ZERO));
    }

    /**
     * Return constant acceleration in order to stop in specified distance. The car-following model is used to determine the
     * stopping distance (i.e. distance remaining at stand still, e.g. 1-3m).
     * @param carFollowingModel car-following model
     * @param parameters parameters
     * @param speed current speed
     * @param distance distance to stop over
     * @return constant acceleration in order to stop in specified distance
     * @throws NullPointerException if any input is {@code null}
     * @throws ParameterException on missing parameter
     */
    public static Acceleration constantAccelerationStop(final CarFollowingModel carFollowingModel, final Parameters parameters,
            final Speed speed, final Length distance) throws ParameterException
    {
        Throw.whenNull(carFollowingModel, "carFollowingModel");
        Throw.whenNull(parameters, "parameters");
        Throw.whenNull(speed, "speed");
        Throw.whenNull(distance, "distance");
        Length s0 = carFollowingModel.desiredHeadway(parameters, Speed.ZERO);
        return new Acceleration(-0.5 * speed.si * speed.si / (distance.si - s0.si), AccelerationUnit.SI);
    }

    /**
     * Calculate free acceleration.
     * @param carFollowingModel car-following model
     * @param parameters parameters
     * @param speed current speed
     * @param speedLimitInfo speed limit info
     * @return acceleration free acceleration
     * @throws NullPointerException if any input is {@code null}
     * @throws ParameterException if a parameter is not given or out of bounds
     */
    public static Acceleration freeAcceleration(final CarFollowingModel carFollowingModel, final Parameters parameters,
            final Speed speed, final SpeedLimitInfo speedLimitInfo) throws ParameterException
    {
        Throw.whenNull(carFollowingModel, "carFollowingModel");
        Throw.whenNull(parameters, "parameters");
        Throw.whenNull(speed, "speed");
        Throw.whenNull(speedLimitInfo, "speedLimitInfo");
        PerceptionIterableSet<PerceivedObject> leaders = new PerceptionIterableSet<>();
        return carFollowingModel.followingAcceleration(parameters, speed, speedLimitInfo, leaders);
    }

    /*-
     * Matlab code testing the functionality of the method below:
     *
     * % Initialization
     * vTar = 0:0.1:(120/3.6);
     * vInit = 0:0.1:(120/3.6);
     * result = zeros(length(vTar), length(vInit));
     * dt = 0.01;
     *
     * % Loop grid
     * for i = 1:length(vTar)
     *     for j = 1:length(vInit)
     *         s = 300;
     *         v = vInit(j);
     *         aMin = inf;
     *         % Model loop
     *         while s > 0
     *             if v > 0
     *                 vVirt = vTar(i)^2/v;
     *             else
     *                 vVirt = inf;
     *             end
     *             sVirt = s + 3 + vVirt * 1.2;
     *             a = idm(sVirt, v, vVirt);
     *             s = s - v*dt - 0.5*a*dt^2;
     *             v = max(0, v + a*dt);
     *             aMin = min(aMin, a);
     *         end
     *         result(i,j) = aMin;
     *     end
     * end
     *
     * % Plot init-speed vs. target speed surface
     * surf(vTars, vInit, result, 'EdgeColor', 'none');
     * xlabel('Initial speed [m/s]')
     * ylabel('Target speed [m/s]')
     * set(colorbar().Label, 'String', 'Maximum deceleration [m/s^2]')
     *
     * % IDM+ model with fixed parameters
     * function a = idm(s, v, vLead)
     *     ss = 3 + max(0, v * 1.2 + v * (v-vLead) / (2 * sqrt(1.25 * 2.09)));
     *     a = 1.25 * min(1 - (v/(120.0/3.6))^4, 1 - (ss/s)^2);
     * end
     */

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
     * @param parameters parameters
     * @param speed current speed
     * @param speedLimitInfo info regarding the desired speed for car-following
     * @param distance distance to the location of the target speed
     * @param targetSpeed target speed
     * @return acceleration acceleration based on the car-following model in order to adjust the speed
     * @throws ParameterException if parameter exception occurs
     * @throws NullPointerException if any input is {@code null}
     * @throws IllegalArgumentException if the distance or target speed is not at least 0
     */
    public static Acceleration approachTargetSpeed(final CarFollowingModel carFollowingModel, final Parameters parameters,
            final Speed speed, final SpeedLimitInfo speedLimitInfo, final Length distance, final Speed targetSpeed)
            throws ParameterException
    {
        Throw.whenNull(parameters, "parameters");
        Throw.whenNull(speed, "speed");
        Throw.whenNull(speedLimitInfo, "speedLimitInfo");
        Throw.whenNull(distance, "distance");
        Throw.whenNull(targetSpeed, "targetSpeed");
        Throw.when(distance.lt0(), IllegalArgumentException.class, "Distance must be at least 0.");
        Throw.when(targetSpeed.lt0(), IllegalArgumentException.class, "Target speed must be at least 0.");
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
        Length virtualDistance = distance.plus(carFollowingModel.desiredHeadway(parameters, virtualSpeed));
        // calculate acceleration towards virtual vehicle with car-following model
        return carFollowingModel.followingAcceleration(parameters, speed, speedLimitInfo,
                createLeader(virtualDistance, virtualSpeed));
    }

    /**
     * Create a single leader set.
     * @param headway distance to the leader
     * @param speed leader speed
     * @return set with a single leader
     */
    private static PerceptionIterable<PerceivedObject> createLeader(final Length headway, final Speed speed)
    {
        PerceptionIterable<PerceivedObject> leaders = Try.assign(
                () -> new PerceptionIterableSet<>(new PerceivedObjectBase(UUID.randomUUID().toString(), ObjectType.GTU,
                        Length.ONE, new Kinematics.Record(headway, speed, Acceleration.ZERO, true, Overlap.AHEAD))),
                "Exception during headway creation.");
        return leaders;
    }

}
