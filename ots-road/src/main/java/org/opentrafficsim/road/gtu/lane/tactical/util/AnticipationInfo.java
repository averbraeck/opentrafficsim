package org.opentrafficsim.road.gtu.lane.tactical.util;

import java.io.Serializable;

import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Utility class that stores duration and end-speed for a given anticipated movement.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param duration duration of movement
 * @param endSpeed end speed of movement
 */
public final record AnticipationInfo(Duration duration, Speed endSpeed) implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /**
     * Returns info of the anticipation assuming constant acceleration.
     * @param distance distance to cover
     * @param initialSpeed initial speed
     * @param acceleration (assumed) acceleration
     * @return duration to cover given distance with given initial speed and acceleration
     */
    public static AnticipationInfo anticipateMovement(final Length distance, final Speed initialSpeed,
            final Acceleration acceleration)
    {
        return anticipateMovementSpeedLimited(distance, initialSpeed, acceleration, Speed.POSITIVE_INFINITY);
    }

    /**
     * Returns info of the anticipation assuming constant acceleration, without exceeding maximum speed.
     * @param distance distance to cover
     * @param initialSpeed initial speed
     * @param acceleration (assumed) acceleration
     * @param maxSpeed maximum speed
     * @return duration to cover given distance with given initial speed and acceleration, without exceeding maximum speed
     */
    public static AnticipationInfo anticipateMovementSpeedLimited(final Length distance, final Speed initialSpeed,
            final Acceleration acceleration, final Speed maxSpeed)
    {
        if (distance.lt0())
        {
            return new AnticipationInfo(Duration.ZERO, initialSpeed);
        }
        // solve constant speed movement
        if (acceleration.eq(Acceleration.ZERO))
        {
            if (initialSpeed.gt0())
            {
                return new AnticipationInfo(distance.divide(initialSpeed), initialSpeed);
            }
            // stand-still, so infinite
            return new AnticipationInfo(new Duration(Double.POSITIVE_INFINITY, DurationUnit.SI), Speed.ZERO);
        }
        // solve parabolic movement
        double tmp = initialSpeed.si * initialSpeed.si + 2.0 * acceleration.si * distance.si;
        if (tmp < 0)
        {
            // will never cover distance due to deceleration
            return new AnticipationInfo(new Duration(Double.POSITIVE_INFINITY, DurationUnit.SI), Speed.ZERO);
        }
        // parabolic solution
        Duration d = new Duration((Math.sqrt(tmp) - initialSpeed.si) / acceleration.si, DurationUnit.SI);
        // check max speed
        Speed endSpeed = initialSpeed.plus(acceleration.times(d));
        if (endSpeed.le(maxSpeed))
        {
            return new AnticipationInfo(d, endSpeed);
        }
        // maximum speed exceeded, calculate in two steps
        Duration d1 = maxSpeed.minus(initialSpeed).divide(acceleration);
        Length x2 = new Length(distance.si - initialSpeed.si * d1.si - .5 * acceleration.si * d1.si * d1.si, LengthUnit.SI);
        return new AnticipationInfo(d1.plus(x2.divide(maxSpeed)), maxSpeed);
    }

    /**
     * Returns info of the anticipation using free acceleration from car-following model.
     * @param distance distance to cover
     * @param initialSpeed initial speed
     * @param parameters parameters of the anticipated GTU
     * @param carFollowingModel car-following model of the anticipated GTU
     * @param speedLimitInfo speed limit info of the anticipated GTU
     * @param timeStep time step to use
     * @return info regarding anticipation of movement
     * @throws ParameterException if parameter is not defined
     */
    public static AnticipationInfo anticipateMovementFreeAcceleration(final Length distance, final Speed initialSpeed,
            final Parameters parameters, final CarFollowingModel carFollowingModel, final SpeedLimitInfo speedLimitInfo,
            final Duration timeStep) throws ParameterException
    {
        if (distance.lt0())
        {
            return new AnticipationInfo(Duration.ZERO, initialSpeed);
        }
        Duration out = Duration.ZERO;
        if (distance.lt0())
        {
            return new AnticipationInfo(out, initialSpeed);
        }
        Length xCumul = Length.ZERO;
        Speed speed = initialSpeed;
        while (xCumul.lt(distance))
        {
            Acceleration a = CarFollowingUtil.freeAcceleration(carFollowingModel, parameters, speed, speedLimitInfo);
            Length add = new Length(speed.si * timeStep.si + .5 * a.si * timeStep.si * timeStep.si, LengthUnit.SI);
            Length remain = distance.minus(xCumul);
            if (add.lt(remain))
            {
                xCumul = xCumul.plus(add);
                speed = speed.plus(a.times(timeStep));
                out = out.plus(timeStep);
            }
            else
            {
                Duration timeInStep;
                double tmp = Math.sqrt(2 * a.si * remain.si + speed.si * speed.si) - speed.si;
                if (tmp < 0.000001)
                {
                    // (near) constant speed
                    timeInStep = remain.divide(speed);
                }
                else
                {
                    timeInStep = new Duration(tmp / a.si, DurationUnit.SI);
                    speed = speed.plus(a.times(timeInStep));
                }
                out = out.plus(timeInStep);
                return new AnticipationInfo(out, speed);
            }
        }
        // should not happen
        throw new RuntimeException("Distance for anticipation of conflict movement is surpassed.");
    }

}
