package org.opentrafficsim.road.gtu.lane.tactical.util;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionIterable;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionIterableSet;
import org.opentrafficsim.road.gtu.lane.perception.headway.AbstractHeadway;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
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
     * @param carFollowingModel CarFollowingModel; car-following model
     * @param parameters Parameters; parameters
     * @param speed Speed; current speed
     * @param speedLimitInfo SpeedLimitInfo; speed limit info
     * @param distance Length; distance
     * @param leaderSpeed Speed; speed of the leader
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
     * @param carFollowingModel CarFollowingModel; car-following model
     * @param parameters Parameters; parameters
     * @param speed Speed; current speed
     * @param speedLimitInfo SpeedLimitInfo; speed limit info
     * @param leader HeadwayGtu; leader
     * @return acceleration for following the leader
     * @throws ParameterException if a parameter is not given or out of bounds
     */
    public static Acceleration followSingleLeader(final CarFollowingModel carFollowingModel, final Parameters parameters,
            final Speed speed, final SpeedLimitInfo speedLimitInfo, final HeadwayGtu leader) throws ParameterException
    {
        return carFollowingModel.followingAcceleration(parameters, speed, speedLimitInfo, new PerceptionIterableSet<>(leader));
    }

    /**
     * Stop within given distance.
     * @param carFollowingModel CarFollowingModel; car-following model
     * @param parameters Parameters; parameters
     * @param speed Speed; current speed
     * @param speedLimitInfo SpeedLimitInfo; speed limit info
     * @param distance Length; distance to stop over
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
     * @param carFollowingModel CarFollowingModel; car-following model
     * @param parameters Parameters; parameters
     * @param speed Speed; current speed
     * @param distance Length; distance to stop over
     * @return constant acceleration in order to stop in specified distance
     * @throws ParameterException on missing parameter
     */
    public static Acceleration constantAccelerationStop(final CarFollowingModel carFollowingModel, final Parameters parameters,
            final Speed speed, final Length distance) throws ParameterException
    {
        Length s0 = carFollowingModel.desiredHeadway(parameters, Speed.ZERO);
        return new Acceleration(-0.5 * speed.si * speed.si / (distance.si - s0.si), AccelerationUnit.SI);
    }

    /**
     * Calculate free acceleration.
     * @param carFollowingModel CarFollowingModel; car-following model
     * @param parameters Parameters; parameters
     * @param speed Speed; current speed
     * @param speedLimitInfo SpeedLimitInfo; speed limit info
     * @return acceleration free acceleration
     * @throws ParameterException if a parameter is not given or out of bounds
     */
    public static Acceleration freeAcceleration(final CarFollowingModel carFollowingModel, final Parameters parameters,
            final Speed speed, final SpeedLimitInfo speedLimitInfo) throws ParameterException
    {
        PerceptionIterableSet<Headway> leaders = new PerceptionIterableSet<>();
        return carFollowingModel.followingAcceleration(parameters, speed, speedLimitInfo, leaders);
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
     * @param carFollowingModel CarFollowingModel; car-following model to use
     * @param parameters Parameters; parameters
     * @param speed Speed; current speed
     * @param speedLimitInfo SpeedLimitInfo; info regarding the desired speed for car-following
     * @param distance Length; distance to the location of the target speed
     * @param targetSpeed Speed; target speed
     * @return acceleration acceleration based on the car-following model in order to adjust the speed
     * @throws ParameterException if parameter exception occurs
     * @throws NullPointerException if any input is null
     * @throws IllegalArgumentException if the distance or target speed is not at least 0
     */
    public static Acceleration approachTargetSpeed(final CarFollowingModel carFollowingModel, final Parameters parameters,
            final Speed speed, final SpeedLimitInfo speedLimitInfo, final Length distance, final Speed targetSpeed)
            throws ParameterException
    {
        Throw.whenNull(parameters, "Parameters may not be null.");
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
        Length virtualDistance = distance.plus(carFollowingModel.desiredHeadway(parameters, virtualSpeed));
        // calculate acceleration towards virtual vehicle with car-following model
        return carFollowingModel.followingAcceleration(parameters, speed, speedLimitInfo,
                createLeader(virtualDistance, virtualSpeed));
    }

    /**
     * Create a single leader set.
     * @param headway Length; distance to the leader
     * @param speed Speed; leader speed
     * @return Set; set with a single leader
     */
    private static PerceptionIterable<Headway> createLeader(final Length headway, final Speed speed)
    {
        PerceptionIterable<Headway> leaders =
                Try.assign(() -> new PerceptionIterableSet<>(new CarFollowingHeadway(headway, speed)),
                        "Exception during headway creation.");
        return leaders;
    }

    /**
     * Simple headway implementation for minimum car-following information.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public static class CarFollowingHeadway extends AbstractHeadway
    {
        /** */
        private static final long serialVersionUID = 20180226L;

        /** Speed of the leader. */
        private final Speed speed;

        /**
         * Constructor.
         * @param headway Length; distance to the leader
         * @param speed Speed; leader speed
         * @throws GtuException on exception
         */
        public CarFollowingHeadway(final Length headway, final Speed speed) throws GtuException
        {
            super(headway);
            this.speed = speed;
        }

        /** {@inheritDoc} */
        @Override
        public String getId()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Length getLength()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Speed getSpeed()
        {
            return this.speed;
        }

        /** {@inheritDoc} */
        @Override
        public ObjectType getObjectType()
        {
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public Acceleration getAcceleration()
        {
            return null;
        }
    }

}
