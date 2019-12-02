package org.opentrafficsim.core.gtu.plan.operational;

import java.util.ArrayList;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan.SpeedSegment;
import org.opentrafficsim.core.math.Solver;

/**
 * Builder for several often used operational plans. E.g., decelerate to come to a full stop at the end of a shape; accelerate
 * to reach a certain speed at the end of a curve; drive constant on a curve; decelerate or accelerate to reach a given end
 * speed at the end of a curve, etc.<br>
 * TODO driving with negative speeds (backward driving) is not yet supported.<br>
 * TODO plan with a constant speed.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 15, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class OperationalPlanBuilder
{
    /** The maximum acceleration for unbounded accelerations: 1E12 m/s2. */
    private static final Acceleration MAX_ACCELERATION = new Acceleration(1E12, AccelerationUnit.SI);

    /** The maximum deceleration for unbounded accelerations: -1E12 m/s2. */
    private static final Acceleration MAX_DECELERATION = new Acceleration(-1E12, AccelerationUnit.SI);

    /** Private constructor prevents instantiation. */
    private OperationalPlanBuilder()
    {
        // class should not be instantiated
    }

    /**
     * Build a plan with a path and a given speed.
     * @param gtu GTU; the GTU for debugging purposes
     * @param path OTSLine3D; the path to drive (provides the length)
     * @param startTime Time; the current time or a time in the future when the plan should start
     * @param speed Speed; the speed at the start of the path
     * @return the operational plan to accomplish the given end speed
     * @throws OperationalPlanException when the length of the path and the calculated driven distance implied by the
     *             constructed segment list differ more than a given threshold
     */
    public static OperationalPlan buildConstantSpeedPlan(final GTU gtu, final OTSLine3D path, final Time startTime,
            final Speed speed) throws OperationalPlanException
    {
        Length length = path.getLength();
        OperationalPlan.Segment segment;
        segment = new SpeedSegment(length.divide(speed));
        ArrayList<OperationalPlan.Segment> segmentList = new ArrayList<>();
        segmentList.add(segment);
        return new OperationalPlan(gtu, path, startTime, speed, segmentList);
    }

    /**
     * Build a plan with a path and a given start speed to try to reach a provided end speed, exactly at the provided
     * <code>endTime</code>. The acceleration (and deceleration) are capped by maxAcceleration and maxDeceleration. Therefore,
     * there is no guarantee that the end speed is actually reached by this plan. <p>
     * TODO: rename this method buildConstantAccelerationPlan.
     * @param gtu GTU; the GTU for debugging purposes
     * @param path OTSLine3D; the path to drive (provides the length)
     * @param startTime Time; the current time or a time in the future when the plan should start
     * @param startSpeed Speed; the speed at the start of the path
     * @param endSpeed Speed; the required end speed
     * @param maximumAcceleration Acceleration; the maximum acceleration that can be applied, provided as a POSITIVE number
     * @param maximumDeceleration Acceleration; the maximum deceleration that can be applied, provided as a NEGATIVE number
     * @return the operational plan to accomplish the given end speed
     * @throws OperationalPlanException when the length of the path and the calculated driven distance implied by the
     *             constructed segment list differ more than a given threshold
     */
    public static OperationalPlan buildGradualAccelerationPlan(final GTU gtu, final OTSLine3D path, final Time startTime,
            final Speed startSpeed, final Speed endSpeed, final Acceleration maximumAcceleration,
            final Acceleration maximumDeceleration) throws OperationalPlanException
    {
        Length length = path.getLength();
        OperationalPlan.Segment segment;
        if (startSpeed.eq(endSpeed))
        {
            segment = new SpeedSegment(length.divide(startSpeed));
        }
        else
        {
            // t = 2x / (vt + v0); a = (vt - v0) / t
            Duration duration = length.times(2.0).divide(endSpeed.plus(startSpeed));
            Acceleration acceleration = endSpeed.minus(startSpeed).divide(duration);
            try
            {
                if (acceleration.si < 0.0 && acceleration.lt(maximumDeceleration))
                {
                    acceleration = maximumDeceleration;
                    // duration = new Duration(abc(acceleration.si / 2, startSpeed.si, -length.si), DurationUnit.SI);
                    duration = new Duration(Solver.firstSolutionAfter(0, acceleration.si / 2, startSpeed.si, -length.si),
                            DurationUnit.SI);
                }
                if (acceleration.si > 0.0 && acceleration.gt(maximumAcceleration))
                {
                    acceleration = maximumAcceleration;
                    // duration = new Duration(abc(acceleration.si / 2, startSpeed.si, -length.si), DurationUnit.SI);
                    duration = new Duration(Solver.firstSolutionAfter(0, acceleration.si / 2, startSpeed.si, -length.si),
                            DurationUnit.SI);
                }
            }
            catch (ValueRuntimeException exception)
            {
                throw new OperationalPlanException("Caught unexpected exception: " + exception);
            }
            segment = new OperationalPlan.AccelerationSegment(duration, acceleration);
        }
        ArrayList<OperationalPlan.Segment> segmentList = new ArrayList<>();
        segmentList.add(segment);
        return new OperationalPlan(gtu, path, startTime, startSpeed, segmentList);
    }

    /**
     * Build a plan with a path and a given start speed to reach a provided end speed, exactly at the end of the curve.
     * Acceleration and deceleration are virtually unbounded (1E12 m/s2) to reach the end speed (e.g., to come to a complete
     * stop).
     * @param gtu GTU; the GTU for debugging purposes
     * @param path OTSLine3D; the path to drive (provides the length)
     * @param startTime Time; the current time or a time in the future when the plan should start
     * @param startSpeed Speed; the speed at the start of the path
     * @param endSpeed Speed; the required end speed
     * @return the operational plan to accomplish the given end speed
     * @throws OperationalPlanException when the length of the path and the calculated driven distance implied by the
     *             constructed segment list differ more than a given threshold
     */
    public static OperationalPlan buildGradualAccelerationPlan(final GTU gtu, final OTSLine3D path, final Time startTime,
            final Speed startSpeed, final Speed endSpeed) throws OperationalPlanException
    {
        return buildGradualAccelerationPlan(gtu, path, startTime, startSpeed, endSpeed, MAX_ACCELERATION, MAX_DECELERATION);
    }

    /**
     * Build a plan with a path and a given start speed to try to reach a provided end speed. Acceleration or deceleration is as
     * provided, until the end speed is reached. After this, constant end speed is used to reach the end point of the path.
     * There is no guarantee that the end speed is actually reached by this plan. If the end speed is zero, and it is reached
     * before completing the path, a truncated path that ends where the GTU stops is used instead. The maximum acceleration and
     * deceleration is limited by the provided values. If these prevent the <code>endSpeed</code> from being reached, the
     * generated plan is a constant acceleration plan using the limiting value.
     * @param gtu GTU; the GTU for debugging purposes
     * @param path OTSLine3D; the path to drive (provides the length)
     * @param startTime Time; the current time or a time in the future when the plan should start
     * @param startSpeed Speed; the speed at the start of the path
     * @param endSpeed Speed; the required end speed
     * @param maximumAcceleration Acceleration; the acceleration to use if endSpeed &gt; startSpeed, provided as a POSITIVE
     *            number
     * @param maximumDeceleration Acceleration; the deceleration to use if endSpeed &lt; startSpeed, provided as a NEGATIVE
     *            number
     * @return the operational plan to accomplish the given end speed
     * @throws OperationalPlanException when the length of the path and the calculated driven distance implied by the
     *             constructed segment list differ more than a given threshold
     */
    public static OperationalPlan buildMaximumAccelerationPlan(final GTU gtu, final OTSLine3D path, final Time startTime,
            final Speed startSpeed, final Speed endSpeed, final Acceleration maximumAcceleration,
            final Acceleration maximumDeceleration) throws OperationalPlanException
    {
        Length length = path.getLength();
        ArrayList<OperationalPlan.Segment> segmentList = new ArrayList<>();
        if (startSpeed.eq(endSpeed))
        {
            segmentList.add(new OperationalPlan.SpeedSegment(length.divide(startSpeed)));
        }
        else
        {
            try
            {
                if (endSpeed.gt(startSpeed))
                {
                    Duration t = endSpeed.minus(startSpeed).divide(maximumAcceleration);
                    Length x = startSpeed.times(t).plus(maximumAcceleration.times(0.5).times(t).times(t));
                    if (x.ge(length))
                    {
                        // we cannot reach the end speed in the given distance with the given acceleration
                        // Duration duration = new Duration(abc(acceleration.si / 2, startSpeed.si, -length.si),
                        // DurationUnit.SI);
                        Duration duration = new Duration(
                                Solver.firstSolutionAfter(0, maximumAcceleration.si / 2, startSpeed.si, -length.si),
                                DurationUnit.SI);
                        segmentList.add(new OperationalPlan.AccelerationSegment(duration, maximumAcceleration));
                    }
                    else
                    {
                        // we reach the (higher) end speed before the end of the segment. Make two segments.
                        segmentList.add(new OperationalPlan.AccelerationSegment(t, maximumAcceleration));
                        Duration duration = length.minus(x).divide(endSpeed);
                        segmentList.add(new OperationalPlan.SpeedSegment(duration));
                    }
                }
                else
                {
                    Duration t = endSpeed.minus(startSpeed).divide(maximumDeceleration);
                    Length x = startSpeed.times(t).plus(maximumDeceleration.times(0.5).times(t).times(t));
                    if (x.ge(length))
                    {
                        // we cannot reach the end speed in the given distance with the given deceleration
                        // Duration duration = new Duration(abc(deceleration.si / 2, startSpeed.si, -length.si),
                        // DurationUnit.SI);
                        Duration duration =
                                new Duration(Solver.firstSolutionAfter(0, maximumDeceleration.si / 2, startSpeed.si, -length.si),
                                        DurationUnit.SI);
                        segmentList.add(new OperationalPlan.AccelerationSegment(duration, maximumDeceleration));
                    }
                    else
                    {
                        if (endSpeed.si == 0.0)
                        {
                            // if endSpeed == 0, we cannot reach the end of the path. Therefore, build a partial path.
                            OTSLine3D partialPath = path.truncate(x.si);
                            segmentList.add(new OperationalPlan.AccelerationSegment(t, maximumDeceleration));
                            return new OperationalPlan(gtu, partialPath, startTime, startSpeed, segmentList);
                        }
                        // we reach the (lower) end speed, larger than zero, before the end of the segment. Make two segments.
                        segmentList.add(new OperationalPlan.AccelerationSegment(t, maximumDeceleration));
                        Duration duration = length.minus(x).divide(endSpeed);
                        segmentList.add(new OperationalPlan.SpeedSegment(duration));
                    }
                }
            }
            catch (ValueRuntimeException | OTSGeometryException exception)
            {
                throw new OperationalPlanException("Caught unexpected exception: " + exception);
            }
        }
        return new OperationalPlan(gtu, path, startTime, startSpeed, segmentList);
    }

    /**
     * Build a plan with a path and a given start speed to try to come to a stop with a given deceleration. If the GTU can stop
     * before completing the given path, a truncated path that ends where the GTU stops is used instead. There is no guarantee
     * that the OperationalPlan will lead to a complete stop.
     * @param gtu GTU; the GTU for debugging purposes
     * @param path OTSLine3D; the path to drive (provides the length)
     * @param startTime Time; the current time or a time in the future when the plan should start
     * @param startSpeed Speed; the speed at the start of the path
     * @param deceleration Acceleration; the deceleration to use if endSpeed &lt; startSpeed, provided as a NEGATIVE number
     * @return the operational plan to accomplish the given end speed
     * @throws OperationalPlanException when the length of the path and the calculated driven distance implied by the
     *             constructed segment list differ more than a given threshold
     */
    public static OperationalPlan buildStopPlan(final GTU gtu, final OTSLine3D path, final Time startTime,
            final Speed startSpeed, final Acceleration deceleration) throws OperationalPlanException
    {
        return buildMaximumAccelerationPlan(gtu, path, startTime, startSpeed, Speed.ZERO,
                new Acceleration(1.0, AccelerationUnit.SI), deceleration);
    }

    /**
     * Test.
     * @param args String[]; args for main
     * @throws OperationalPlanException on error
     * @throws OTSGeometryException on error
     */
    public static void main(final String[] args) throws OperationalPlanException, OTSGeometryException
    {
        OTSLine3D path1 = new OTSLine3D(new OTSPoint3D[] { new OTSPoint3D(0.0, 0.0), new OTSPoint3D(100.0, 0.0) });

        // go from 0 to 10 m/s over entire distance. This should take 20 sec with a=0.5 m/s2.
        OperationalPlan plan1 =
                buildGradualAccelerationPlan(null, path1, Time.ZERO, Speed.ZERO, new Speed(10.0, SpeedUnit.METER_PER_SECOND));
        System.out.println(plan1);

        // go from 0 to 10 m/s over entire distance, but limit a to 0.1 m/s2.
        // This should take 44.72 sec with a=0.1 m/s2, and an end speed of 4.472 m/s.
        OperationalPlan plan2 = buildGradualAccelerationPlan(null, path1, Time.ZERO, Speed.ZERO,
                new Speed(10.0, SpeedUnit.METER_PER_SECOND), new Acceleration(0.1, AccelerationUnit.METER_PER_SECOND_2),
                new Acceleration(-0.1, AccelerationUnit.METER_PER_SECOND_2));
        System.out.println(plan2);

        // go from 0 to 10 m/s with a = 1 m/s2, followed by a constant speed of 10 m/s.
        // This should take 10 sec with a = 1 m/s2, reaching 50 m. After that, 50 m with 10 m/s in 5 sec.
        OperationalPlan plan3 = buildMaximumAccelerationPlan(null, path1, Time.ZERO, Speed.ZERO,
                new Speed(10.0, SpeedUnit.METER_PER_SECOND), new Acceleration(1.0, AccelerationUnit.METER_PER_SECOND_2),
                new Acceleration(-1.0, AccelerationUnit.METER_PER_SECOND_2));
        System.out.println(plan3);

        // go from 10 to 0 m/s with a = -1 m/s2, which should truncate the path at 50 m.
        // This should take 10 sec with a = -1 m/s2, reaching 50 m. After that, the plan should stop.
        OperationalPlan plan4 =
                buildMaximumAccelerationPlan(null, path1, Time.ZERO, new Speed(10.0, SpeedUnit.METER_PER_SECOND),
                        new Speed(0.0, SpeedUnit.METER_PER_SECOND), new Acceleration(1.0, AccelerationUnit.METER_PER_SECOND_2),
                        new Acceleration(-1.0, AccelerationUnit.METER_PER_SECOND_2));
        System.out.println(plan4);

        // try to stop with a = -2 m/s2, which should truncate the path at 25 m.
        // This should take 5 sec with a = -2 m/s2, reaching 25 m. After that, the plan should stop.
        OperationalPlan plan5 = buildStopPlan(null, path1, Time.ZERO, new Speed(10.0, SpeedUnit.METER_PER_SECOND),
                new Acceleration(-2.0, AccelerationUnit.METER_PER_SECOND_2));
        System.out.println(plan5);

    }
}
