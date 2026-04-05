package org.opentrafficsim.road.gtu.tactical.util;

import java.util.Map.Entry;
import java.util.Optional;

import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.exceptions.Throw;
import org.djutils.math.AngleUtil;
import org.opentrafficsim.base.DistancedObject;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeAcceleration;
import org.opentrafficsim.base.parameters.constraint.NumericConstraint;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.tactical.TacticalContextEgo;
import org.opentrafficsim.road.network.speed.SpeedLimits;

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
     * Returns the speed for which the given lateral acceleration follows in the curve.
     * @param radius curve radius
     * @param acceleration acceleration to result from speed in curve
     * @return speed for which the given lateral acceleration follows in the curve
     * @throws IllegalArgumentException if radius or acceleration is negative or zero
     */
    public static Speed getSpeedForLateralAcceleration(final Length radius, final Acceleration acceleration)
    {
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
        Acceleration out = Acceleration.POSITIVE_INFINITY;

        InfrastructurePerception infra = context.getPerception().getPerceptionCategory(InfrastructurePerception.class);
        Optional<DistancedObject<Speed>> speedBump = infra.getSpeedBump();
        if (speedBump.isPresent())
        {
            out = CarFollowingUtil.approachTargetSpeed(context, speedBump.get().distance(), speedBump.get().object());
        }

        /*
         * Each segment of the path has a length and a change in angle. This gives a value of curvature if we assume an arc, and
         * thus a radius. Using a maximum lateral acceleration we can compute the maximum speed on the segment.
         */
        Acceleration aLat = context.getParameters().getParameter(A_LAT);
        double d0 = 0.0;
        double phi0 = context.getPosition().getLocation().dirZ;
        Speed minSpeed = context.getSpeed();
        for (Entry<Length, DirectedPoint2d> entry : infra.getPathScan().entrySet())
        {
            double d1 = entry.getKey().si;
            double phi1 = entry.getValue().dirZ;
            double deltaPhi = Math.abs(AngleUtil.normalizeAroundZero(phi1 - phi0));
            Speed targetSpeed = getSpeedForLateralAcceleration(Length.ofSI(.5 * (d1 - d0) / deltaPhi), aLat);
            d0 = d1;
            phi0 = phi1;
            if (targetSpeed.lt(minSpeed))
            {
                minSpeed = targetSpeed;
                out = Acceleration.min(out, CarFollowingUtil.approachTargetSpeed(context, entry.getKey(), targetSpeed));
            }
        }

        // For lower legal speed limits (road class, fixed sign, dynamic sign), we assume that the car-following model will
        // apply some reasonable deceleration after the change. For higher speed limits, we assume car-following acceleration
        // after the change.

        return out;
    }

    /**
     * Returns desired speed proxy as the minimum of maximum vehicle speed and present speed limits.
     * @param speedLimits speed limits
     * @param maxVehicleSpeed maximum vehicle speed
     * @return desired speed proxy
     */
    public static Speed getDesiredSpeedProxy(final SpeedLimits speedLimits, final Speed maxVehicleSpeed)
    {
        Speed desiredSpeedProxy = maxVehicleSpeed;
        if (speedLimits.laneSpeedLimit() != null)
        {
            desiredSpeedProxy = Speed.min(desiredSpeedProxy, speedLimits.laneSpeedLimit().speed());
        }
        if (speedLimits.gtuTypeSpeedLimit() != null)
        {
            desiredSpeedProxy = Speed.min(desiredSpeedProxy, speedLimits.gtuTypeSpeedLimit().speed());
        }
        return desiredSpeedProxy;
    }

}
