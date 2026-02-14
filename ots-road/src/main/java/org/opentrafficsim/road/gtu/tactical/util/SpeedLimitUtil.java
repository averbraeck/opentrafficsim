package org.opentrafficsim.road.gtu.tactical.util;

import java.util.Optional;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeAcceleration;
import org.opentrafficsim.base.parameters.constraint.NumericConstraint;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.tactical.TacticalContextEgo;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitProspect;
import org.opentrafficsim.road.network.speed.SpeedLimitType;
import org.opentrafficsim.road.network.speed.SpeedLimitTypeSpeedLegal;
import org.opentrafficsim.road.network.speed.SpeedLimitTypes;

/**
 * Static methods regarding speed limits for composition in tactical planners.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
            "Maximum comfortable lateral acceleration", Acceleration.ofSI(1.0), NumericConstraint.POSITIVE);

    /**
     * Returns the minimum speed of the applicable speed limit types MAX_LEGAL_VEHICLE_SPEED, ROAD_CLASS, FIXED_SIGN and
     * DYNAMIC_SIGN. ROAD_CLASS is only used if FIXED_SIGN and DYNAMIC_SIGN are not present.
     * @param speedLimitInfo speed limit info
     * @return minimum of speed of speed limit types
     * @throws NullPointerException if speed limit info is null
     */
    public static Speed getLegalSpeedLimit(final SpeedLimitInfo speedLimitInfo)
    {
        Throw.whenNull(speedLimitInfo, "speedLimitInfo");
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
     * Returns the speed of speed limit type MAX_VEHICLE_SPEED.
     * @param speedLimitInfo speed limit info
     * @return speed of speed limit type MAX_VEHICLE_SPEED
     * @throws NullPointerException if speed limit info is null
     */
    public static Speed getMaximumVehicleSpeed(final SpeedLimitInfo speedLimitInfo)
    {
        Throw.whenNull(speedLimitInfo, "speedLimitInfo");
        return speedLimitInfo.getSpeedInfo(SpeedLimitTypes.MAX_VEHICLE_SPEED);
    }

    /**
     * Return the radius of the curvature speed limit type.
     * @param speedLimitInfo speed limit info
     * @return radius of the curvature speed limit type, empty if no such speed limit type
     * @throws NullPointerException if speed limit info is null
     */
    public static Optional<Length> getCurveRadius(final SpeedLimitInfo speedLimitInfo)
    {
        Throw.whenNull(speedLimitInfo, "speedLimitInfo");
        if (!speedLimitInfo.containsType(SpeedLimitTypes.CURVATURE))
        {
            return Optional.empty();
        }
        return Optional.of(speedLimitInfo.getSpeedInfo(SpeedLimitTypes.CURVATURE).radius());
    }

    /**
     * Returns the speed for which the given lateral acceleration follows in the curve.
     * @param radius curve radius
     * @param acceleration acceleration to result from speed in curve
     * @return speed for which the given lateral acceleration follows in the curve
     * @throws NullPointerException if radius or acceleration is null
     * @throws IllegalArgumentException if radius or acceleration is negative or zero
     */
    public static Speed getSpeedForLateralAcceleration(final Length radius, final Acceleration acceleration)
    {
        Throw.whenNull(radius, "radius");
        Throw.whenNull(acceleration, "acceleration");
        Throw.when(radius.le0(), IllegalArgumentException.class, "Radius mus be greater than zero.");
        Throw.when(acceleration.le0(), IllegalArgumentException.class, "Radius mus be greater than zero.");
        // a=v*v/r => v=sqrt(a*r)
        return new Speed(Math.sqrt(acceleration.si * radius.si), SpeedUnit.SI);
    }

    /**
     * Acceleration for speed limit transitions. This implementation decelerates before curves and speed bumps. For this it uses
     * {@code approachTargetSpeed()} of the car-following utility. All remaining transitions happen in the default manner, i.e.
     * deceleration and acceleration after the speed limit change and governed by the car-following model.
     * @param context tactical information such as parameters and car-following model
     * @param lane lane to consider
     * @return acceleration for speed limit transitions
     * @throws ParameterException if a required parameter is not found
     * @throws OperationalPlanException if there is no infrastructure perception
     */
    public static Acceleration considerSpeedLimitTransitions(final TacticalContextEgo context, final RelativeLane lane)
            throws ParameterException, OperationalPlanException
    {
        SpeedLimitProspect speedLimitProspect =
                context.getPerception().getPerceptionCategory(InfrastructurePerception.class).getSpeedLimitProspect(lane);
        Acceleration out = new Acceleration(Double.POSITIVE_INFINITY, AccelerationUnit.SI);

        // decelerate for curves and speed bumps
        for (SpeedLimitType<?> speedLimitType : new SpeedLimitType[] {SpeedLimitTypes.CURVATURE, SpeedLimitTypes.SPEED_BUMP})
        {
            for (Length distance : speedLimitProspect.getDownstreamDistances(speedLimitType))
            {
                SpeedLimitInfo speedLimitInfo = speedLimitProspect.buildSpeedLimitInfo(distance, speedLimitType);
                Speed targetSpeed = context.getCarFollowingModel().desiredSpeed(context.getParameters(), speedLimitInfo);
                Acceleration a = CarFollowingUtil.approachTargetSpeed(context, distance, targetSpeed);
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
