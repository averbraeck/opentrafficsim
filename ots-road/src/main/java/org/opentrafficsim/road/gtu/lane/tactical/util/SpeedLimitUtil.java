package org.opentrafficsim.road.gtu.lane.tactical.util;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeAcceleration;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitProspect;
import org.opentrafficsim.road.network.speed.SpeedLimitType;
import org.opentrafficsim.road.network.speed.SpeedLimitTypeSpeedLegal;
import org.opentrafficsim.road.network.speed.SpeedLimitTypes;

/**
 * Static methods regarding speed limits for composition in tactical planners.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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
            speedLimitTypes = new SpeedLimitTypeSpeedLegal[] {SpeedLimitTypes.MAX_LEGAL_VEHICLE_SPEED,
                    SpeedLimitTypes.FIXED_SIGN, SpeedLimitTypes.DYNAMIC_SIGN};
        }
        else
        {
            speedLimitTypes =
                    new SpeedLimitTypeSpeedLegal[] {SpeedLimitTypes.MAX_LEGAL_VEHICLE_SPEED, SpeedLimitTypes.ROAD_CLASS};
        }
        Speed result = Speed.POSITIVE_INFINITY;
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
     * @param parameters parameters
     * @param speed current speed
     * @param speedLimitProspect speed limit prospect
     * @param carFollowingModel car following model
     * @return acceleration for speed limit transitions
     * @throws ParameterException if a required parameter is not found
     */
    public static Acceleration considerSpeedLimitTransitions(final Parameters parameters, final Speed speed,
            final SpeedLimitProspect speedLimitProspect, final CarFollowingModel carFollowingModel) throws ParameterException
    {
        Acceleration out = new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);
        SpeedLimitInfo currentSpeedLimitInfo = speedLimitProspect.getSpeedLimitInfo(Length.ZERO);

        // decelerate for curves and speed bumps
        for (SpeedLimitType<?> speedLimitType : new SpeedLimitType[] {SpeedLimitTypes.CURVATURE, SpeedLimitTypes.SPEED_BUMP})
        {
            for (Length distance : speedLimitProspect.getDownstreamDistances(speedLimitType))
            {
                SpeedLimitInfo speedLimitInfo = speedLimitProspect.buildSpeedLimitInfo(distance, speedLimitType);
                Speed targetSpeed = carFollowingModel.desiredSpeed(parameters, speedLimitInfo);
                Acceleration a = CarFollowingUtil.approachTargetSpeed(carFollowingModel, parameters, speed,
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

}
