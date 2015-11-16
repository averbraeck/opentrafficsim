package org.opentrafficsim.core.gtu.plan.operational;

import java.util.ArrayList;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan.SpeedSegment;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Builder for several often used operational plans. E.g., decelerate to come to a full stop at the end of a shape; accelerate
 * to reach a certain speed at the end of a curve; drive constant on a curve; decelerate or accelerate to reach a given end
 * speed at the end of a curve, etc.<br>
 * TODO driving with negative speeds (backward driving) is not yet supported.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 15, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class OperationalPlanBuilder
{
    /** maximum acceleration for unbounded accelerations: 1E12 m/s2. */
    private static final Acceleration MAX_ACCELERATION = new Acceleration(1E12, AccelerationUnit.SI);

    /** maximum deceleration for unbounded accelerations: -1E12 m/s2. */
    private static final Acceleration MAX_DECELERATION = new Acceleration(-1E12, AccelerationUnit.SI);

    /** private constructor. */
    private OperationalPlanBuilder()
    {
        // class should not be instantiated
    }

    /**
     * Build a plan with a path and a given start speed to try to reach a provided end speed, exactly at the end of the curve.
     * The acceleration (and deceleration) are capped by maxAcceleration and maxDeceleration. Therefore, there is no guarantee
     * that the end speed is actually reached by this plan.
     * @param path the path to drive (provides the length)
     * @param startTime the current time or a time in the future when the plan should start
     * @param startSpeed the speed at the start of the path
     * @param endSpeed the required end speed
     * @param maxAcceleration the maximum acceleration that can be applied, provided as a POSITIVE number
     * @param maxDeceleration the maximum deceleration that can be applied, provided as a NEGATIVE number
     * @return the operational plan to accomplish the given end speed
     * @throws NetworkException when the length of the path and the calculated driven distance implied by the constructed
     *             segment list differ more than a given threshold
     */
    public static OperationalPlan buildGradualAccelerationPlan(final OTSLine3D path, final Time.Abs startTime,
        final Speed startSpeed, final Speed endSpeed, final Acceleration maxAcceleration,
        final Acceleration maxDeceleration) throws NetworkException
    {
        Length.Rel length = path.getLength();
        OperationalPlan.Segment segment;
        if (startSpeed.eq(endSpeed))
        {
            segment = new SpeedSegment(length.divideBy(startSpeed));
        }
        else
        {
            // t = 2x / (vt + v0); a = (vt - v0) / t
            Time.Rel duration = length.multiplyBy(2.0).divideBy(endSpeed.plus(startSpeed));
            Acceleration acceleration = endSpeed.minus(startSpeed).divideBy(duration);
            if (acceleration.si < 0.0 && acceleration.lt(maxDeceleration))
            {
                acceleration = maxDeceleration;
                duration = new Time.Rel(abc(acceleration.si / 2, startSpeed.si, -length.si), TimeUnit.SI);
            }
            if (acceleration.si > 0.0 && acceleration.gt(maxAcceleration))
            {
                acceleration = maxAcceleration;
                duration = new Time.Rel(abc(acceleration.si / 2, startSpeed.si, -length.si), TimeUnit.SI);
            }
            segment = new OperationalPlan.AccelerationSegment(duration, acceleration);
        }
        ArrayList<OperationalPlan.Segment> segmentList = new ArrayList<>();
        segmentList.add(segment);
        return new OperationalPlan(path, startTime, startSpeed, segmentList);
    }

    /**
     * Build a plan with a path and a given start speed to reach a provided end speed, exactly at the end of the curve.
     * Acceleration and deceleration are virtually unbounded (1E12 m/s2) to reach the end speed (e.g., to come to a complete
     * stop).
     * @param path the path to drive (provides the length)
     * @param startTime the current time or a time in the future when the plan should start
     * @param startSpeed the speed at the start of the path
     * @param endSpeed the required end speed
     * @return the operational plan to accomplish the given end speed
     * @throws NetworkException when the length of the path and the calculated driven distance implied by the constructed
     *             segment list differ more than a given threshold
     */
    public static OperationalPlan buildGradualAccelerationPlan(final OTSLine3D path, final Time.Abs startTime,
        final Speed startSpeed, final Speed endSpeed) throws NetworkException
    {
        return buildGradualAccelerationPlan(path, startTime, startSpeed, endSpeed, MAX_ACCELERATION, MAX_DECELERATION);
    }

    /**
     * Build a plan with a path and a given start speed to try to reach a provided end speed. Acceleration or deceleration is as
     * provided, until the end speed is reached. After this, constant end speed is used to reach the end point of the path.
     * There is no guarantee that the end speed is actually reached by this plan. If the end speed is zero, and it is reached
     * before completing the path, a truncated path that ends where the GTU stops is used instead.
     * @param path the path to drive (provides the length)
     * @param startTime the current time or a time in the future when the plan should start
     * @param startSpeed the speed at the start of the path
     * @param endSpeed the required end speed
     * @param acceleration the acceleration to use if endSpeed &gt; startSpeed, provided as a POSITIVE number
     * @param deceleration the deceleration to use if endSpeed &lt; startSpeed, provided as a NEGATIVE number
     * @return the operational plan to accomplish the given end speed
     * @throws NetworkException when the length of the path and the calculated driven distance implied by the constructed
     *             segment list differ more than a given threshold
     */
    public static OperationalPlan buildMaximumAccelerationPlan(final OTSLine3D path, final Time.Abs startTime,
        final Speed startSpeed, final Speed endSpeed, final Acceleration acceleration, final Acceleration deceleration)
        throws NetworkException
    {
        Length.Rel length = path.getLength();
        ArrayList<OperationalPlan.Segment> segmentList = new ArrayList<>();
        if (startSpeed.eq(endSpeed))
        {
            segmentList.add(new OperationalPlan.SpeedSegment(length.divideBy(startSpeed)));
        }
        else
        {
            if (endSpeed.gt(startSpeed))
            {
                Time.Rel t = endSpeed.minus(startSpeed).divideBy(acceleration);
                Length.Rel x = startSpeed.multiplyBy(t).plus(acceleration.multiplyBy(0.5).multiplyBy(t).multiplyBy(t));
                if (x.ge(length))
                {
                    // we cannot reach the end speed in the given distance with the given acceleration
                    Time.Rel duration = new Time.Rel(abc(acceleration.si / 2, startSpeed.si, -length.si), TimeUnit.SI);
                    segmentList.add(new OperationalPlan.AccelerationSegment(duration, acceleration));
                }
                else
                {
                    // we reach the (higher) end speed before the end of the segment. Make two segments.
                    segmentList.add(new OperationalPlan.AccelerationSegment(t, acceleration));
                    Time.Rel duration = length.minus(x).divideBy(endSpeed);
                    segmentList.add(new OperationalPlan.SpeedSegment(duration));
                }
            }
            else
            {
                Time.Rel t = endSpeed.minus(startSpeed).divideBy(deceleration);
                Length.Rel x = startSpeed.multiplyBy(t).plus(deceleration.multiplyBy(0.5).multiplyBy(t).multiplyBy(t));
                if (x.ge(length))
                {
                    // we cannot reach the end speed in the given distance with the given deceleration
                    Time.Rel duration = new Time.Rel(abc(deceleration.si / 2, startSpeed.si, -length.si), TimeUnit.SI);
                    segmentList.add(new OperationalPlan.AccelerationSegment(duration, deceleration));
                }
                else
                {
                    if (endSpeed.si == 0.0)
                    {
                        // if endSpeed == 0, we cannot reach the end of the path. Therefore, build a partial path.
                        OTSLine3D partialPath = path.truncate(x.si);
                        segmentList.add(new OperationalPlan.AccelerationSegment(t, deceleration));
                        return new OperationalPlan(partialPath, startTime, startSpeed, segmentList);
                    }
                    // we reach the (lower) end speed, larger than zero, before the end of the segment. Make two segments.
                    segmentList.add(new OperationalPlan.AccelerationSegment(t, deceleration));
                    Time.Rel duration = length.minus(x).divideBy(endSpeed);
                    segmentList.add(new OperationalPlan.SpeedSegment(duration));
                }
            }
        }
        return new OperationalPlan(path, startTime, startSpeed, segmentList);
    }

    /**
     * Build a plan with a path and a given start speed to try to come to a stop with a given deceleration. If the GTU can stop
     * before completing the given path, a truncated path that ends where the GTU stops is used instead. There is no guarantee
     * that the OperationalPlan will lead to a complete stop.
     * @param path the path to drive (provides the length)
     * @param startTime the current time or a time in the future when the plan should start
     * @param startSpeed the speed at the start of the path
     * @param deceleration the deceleration to use if endSpeed &lt; startSpeed, provided as a NEGATIVE number
     * @return the operational plan to accomplish the given end speed
     * @throws NetworkException when the length of the path and the calculated driven distance implied by the constructed
     *             segment list differ more than a given threshold
     */
    public static OperationalPlan buildStopPlan(final OTSLine3D path, final Time.Abs startTime, final Speed startSpeed,
        final Acceleration deceleration) throws NetworkException
    {
        return buildMaximumAccelerationPlan(path, startTime, startSpeed, new Speed(0.0, SpeedUnit.SI),
            new Acceleration(1.0, AccelerationUnit.SI), deceleration);
    }

    /**
     * ABC-formula. Returns the largest positive number for x where ax^2 + bx + c = 0.
     * @param a param for the x-square term
     * @param b param for the x-term
     * @param c param for the additional term
     * @return the largest positive number for x where ax^2 + bx + c = 0.
     */
    private static double abc(final double a, final double b, final double c)
    {
        if (a == 0)
        {
            return -c / b;
        }
        double det = Math.sqrt(b * b - 4.0 * a * c);
        double x1 = (-b + det) / (2.0 * a);
        double x2 = (-b - det) / (2.0 * a);
        return x1 > x2 ? x1 : x2;
    }

    /**
     * Test.
     * @param args args for main
     * @throws NetworkException on error
     */
    public static void main(final String[] args) throws NetworkException
    {
        OTSLine3D path1 = new OTSLine3D(new OTSPoint3D[]{new OTSPoint3D(0.0, 0.0), new OTSPoint3D(100.0, 0.0)});

        // go from 0 to 10 m/s over entire distance. This should take 20 sec with a=0.5 m/s2.
        OperationalPlan plan1 =
            buildGradualAccelerationPlan(path1, new Time.Abs(0.0, TimeUnit.SI), new Speed(0.0, SpeedUnit.SI),
                new Speed(10.0, SpeedUnit.METER_PER_SECOND));
        System.out.println(plan1);

        // go from 0 to 10 m/s over entire distance, but limit a to 0.1 m/s2.
        // This should take 44.72 sec with a=0.1 m/s2, and an end speed of 4.472 m/s.
        OperationalPlan plan2 =
            buildGradualAccelerationPlan(path1, new Time.Abs(0.0, TimeUnit.SI), new Speed(0.0, SpeedUnit.SI),
                new Speed(10.0, SpeedUnit.METER_PER_SECOND),
                new Acceleration(0.1, AccelerationUnit.METER_PER_SECOND_2), new Acceleration(-0.1,
                    AccelerationUnit.METER_PER_SECOND_2));
        System.out.println(plan2);

        // go from 0 to 10 m/s with a = 1 m/s2, followed by a constant speed of 10 m/s.
        // This should take 10 sec with a = 1 m/s2, reaching 50 m. After that, 50 m with 10 m/s in 5 sec.
        OperationalPlan plan3 =
            buildMaximumAccelerationPlan(path1, new Time.Abs(0.0, TimeUnit.SI), new Speed(0.0, SpeedUnit.SI),
                new Speed(10.0, SpeedUnit.METER_PER_SECOND),
                new Acceleration(1.0, AccelerationUnit.METER_PER_SECOND_2), new Acceleration(-1.0,
                    AccelerationUnit.METER_PER_SECOND_2));
        System.out.println(plan3);

        // go from 10 to 0 m/s with a = -1 m/s2, which should truncate the path at 50 m.
        // This should take 10 sec with a = -1 m/s2, reaching 50 m. After that, the plan should stop.
        OperationalPlan plan4 =
            buildMaximumAccelerationPlan(path1, new Time.Abs(0.0, TimeUnit.SI), new Speed(10.0, SpeedUnit.SI),
                new Speed(0.0, SpeedUnit.METER_PER_SECOND), new Acceleration(1.0, AccelerationUnit.METER_PER_SECOND_2),
                new Acceleration(-1.0, AccelerationUnit.METER_PER_SECOND_2));
        System.out.println(plan4);

        // try to stop with a = -2 m/s2, which should truncate the path at 25 m.
        // This should take 5 sec with a = -2 m/s2, reaching 25 m. After that, the plan should stop.
        OperationalPlan plan5 =
            buildStopPlan(path1, new Time.Abs(0.0, TimeUnit.SI), new Speed(10.0, SpeedUnit.SI), new Acceleration(-2.0,
                AccelerationUnit.METER_PER_SECOND_2));
        System.out.println(plan5);

    }
}
