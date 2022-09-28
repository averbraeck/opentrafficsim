package org.opentrafficsim.core.gtu.plan.operational;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DurationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.Test;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;

/**
 * Test the OperationalPlan and OperationalPlanBuilder classes.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class OperationalPlanTest
{
    /**
     * Test OperationalPlan.
     * @throws OTSGeometryException Should not happen - test fails if it does
     * @throws OperationalPlanException Should not happen - test fails if it does
     */
    @Test
    public final void testOperationalPlan() throws OperationalPlanException, OTSGeometryException
    {
        DirectedPoint waitPoint = new DirectedPoint(12, 13, 14, 15, 16, 17);
        Time startTime = new Time(100, TimeUnit.DEFAULT);
        Duration duration = new Duration(1, DurationUnit.MINUTE);
        OperationalPlan op = new OperationalPlan(null, waitPoint, startTime, duration);
        assertEquals("Start speed is 0", 0, op.getStartSpeed().si, 0);
        assertEquals("End speed is 0", 0, op.getEndSpeed().si, 0);
        assertEquals("Start time is " + startTime, startTime.si, op.getStartTime().si, 0);
        assertEquals("End time is " + startTime.plus(duration), startTime.plus(duration).si, op.getEndTime().si, 0.0001);
        assertEquals("Segment list contains 1 segment", 1, op.getOperationalPlanSegmentList().size());
        OperationalPlan.Segment segment = op.getOperationalPlanSegmentList().get(0);
        assertEquals("Duration is " + duration, duration.si, segment.getDuration().si, 0.00001);
        assertEquals("DurationSI is " + duration.si, duration.si, segment.getDurationSI(), 0.00001);
        assertEquals("End location is " + waitPoint, 0, waitPoint.distance(op.getEndLocation()), 0.0001);
        try
        {
            op.getLocation(new Duration(-0.1, DurationUnit.SI));
            fail("getLocation for negative relative time should have thrown an OperationalPlanException");
        }
        catch (OperationalPlanException ope)
        {
            // Ignore expected exception
        }
        try
        {
            op.getLocation(new Time(99.5, TimeUnit.DEFAULT));
            fail("getLocation for absolute time before start time should have thrown an OperationalPlanException");
        }
        catch (OperationalPlanException ope)
        {
            // Ignore expected exception
        }
        op.getLocation(new Time(100.1, TimeUnit.DEFAULT)); // Should NOT throw an exception
        op.getLocation(new Time(159.9, TimeUnit.DEFAULT)); // Should NOT throw an exception
        try
        {
            op.getLocation(new Time(160.1, TimeUnit.DEFAULT));
            fail("getLocation for absolute time after end time should have thrown an OperationalPlanException");
        }
        catch (OperationalPlanException ope)
        {
            // Ignore expected exception
        }
        for (int i = 0; i <= duration.si; i++)
        {
            Time t = startTime.plus(new Duration(i, DurationUnit.SECOND));
            DirectedPoint locationAtT = op.getLocation(t);
            // System.out.println("Location at time " + t + " is " + locationAtT);
            // Use a tolerance that is larger than the z-offset (0.001)
            assertEquals("Distance from wait point at " + t + " is 0", 0, waitPoint.distance(locationAtT), 0.002);
        }
        assertEquals("end location matches start location", 0,
                new OTSPoint3D(op.getEndLocation()).distance(new OTSPoint3D(waitPoint)).si, 0.0001);
        OTSLine3D path = new OTSLine3D(new OTSPoint3D(12, 13, 14), new OTSPoint3D(123, 234, 345));
        Speed startSpeed = new Speed(20, SpeedUnit.KM_PER_HOUR);
        Speed endSpeed = new Speed(50, SpeedUnit.KM_PER_HOUR);
        Acceleration maxAcceleration = new Acceleration(1, AccelerationUnit.METER_PER_SECOND_2);
        Acceleration maxDeceleration = new Acceleration(6, AccelerationUnit.METER_PER_SECOND_2);
        op = OperationalPlanBuilder.buildGradualAccelerationPlan(null, path, startTime, startSpeed, endSpeed, maxAcceleration,
                maxDeceleration);
        assertEquals("Start speed is " + startSpeed, startSpeed.si, op.getStartSpeed().si, 0.00001);
        assertEquals("Start time is " + startTime, startTime.si, op.getStartTime().si, 0.00001);
        assertEquals("End speed is " + endSpeed, endSpeed.si, op.getEndSpeed().si, 0.00001);
        // TODO assertEquals("getPath returns the path", path, op.getPath());
        // What acceleration is required to reach endSpeed at the end of the path?
        // (This mathematical derivation constructed independently from the OperationalPlanBuilder code.)
        OTSLine3D returnedPath = op.getPath();
        // This fails: assertEquals("returned path should match path", path, returnedPath);
        assertEquals("size of path should match", path.size(), returnedPath.size());
        for (int i = 0; i < path.size(); i++)
        {
            assertEquals("position of point " + i, 0, path.get(i).distance(returnedPath.get(i)).si, 0.0001);
        }
        double pathLength = path.getLengthSI();
        // Solve eq 1: startSpeed * t + 0.5 * a * t * t == pathLength
        // And eq 2: startSpeed + t * a == endSpeed
        // ==> t * a = endSpeed - startSpeed
        double speedDifference = endSpeed.minus(startSpeed).si;
        // ==> a == speedDifference / t
        // Replace a in eq 1:
        // startSpeed * t + 0.5 * speedDifference / t * t * t == pathLength
        // startSpeed * t + 0.5 * speedDifference * t == pathLength
        // (startSpeed + 0.5 * speedDifference) * t == pathLength
        // t == pathLength / (startSpeed + 0.5 * speedDifference)
        double t = pathLength / (startSpeed.si + 0.5 * speedDifference);
        Time endTime = startTime.plus(new Duration(t, DurationUnit.SECOND));
        // System.out.println("End time is " + endTime);
        Acceleration a = new Acceleration(speedDifference / t, AccelerationUnit.SI);
        // System.out.println("required acceleration is " + a);
        // Check the result
        double actualLength = startSpeed.si * t + 0.5 * a.si * t * t;
        // System.out.println("driven length is " + actualLength + " pathLength is " + pathLength);
        assertEquals("Driven length is " + actualLength, actualLength, pathLength, 0.00001);
        double actualEndSpeed = startSpeed.si + t * a.si;
        // System.out.println("Actual end speed is " + actualEndSpeed + " intended end speed is " + endSpeed.si);
        assertEquals("Speed at end is " + endSpeed, endSpeed.si, actualEndSpeed, 0.000001);
        assertEquals("End time is " + endTime, endTime.si, op.getEndTime().si, 0.00001);
        // System.out.println("acceleration according to plan is " + op.getAcceleration(startTime));
        assertEquals("Required acceleration is " + a, a.si, op.getAcceleration(startTime).si, 0.000001);
        assertEquals("total duration", endTime.minus(startTime).si, op.getTotalDuration().si, 0.00001);
        DirectedPoint dp = op.getEndLocation();
        try
        {
            assertEquals("end location", 0, new OTSPoint3D(dp).distanceSI(path.get(1)), 0.00001);
        }
        catch (OTSGeometryException exception)
        {
            fail("Caught unexpected exception");
            exception.printStackTrace();
        }
        int steps = 20;
        for (int i = 0; i <= steps; i++)
        {
            double stepTime = startTime.si + t * i / steps * 0.9999; // sometimes fails for endTime
            Time absTime = new Time(stepTime, TimeUnit.DEFAULT);
            double deltaT = stepTime - startTime.si;
            Duration relTime = new Duration(deltaT, DurationUnit.SI);
            double expectedDistance = startSpeed.si * deltaT + 0.5 * a.si * deltaT * deltaT;
            double fraction = expectedDistance / path.getLength().si;
            OTSPoint3D expectedPosition = new OTSPoint3D(path.getLocationFraction(fraction));
            DirectedPoint actualPosition = op.getLocation(absTime);
            assertEquals("Position at abs time " + deltaT, 0, expectedPosition.distance(new OTSPoint3D(actualPosition)).si,
                    0.002);
            actualPosition = op.getLocation(relTime);
            assertEquals("Position at rel time " + deltaT, 0, expectedPosition.distance(new OTSPoint3D(actualPosition)).si,
                    0.002);
            double expectedSpeed = startSpeed.si + a.si * deltaT;
            Speed actualSpeed = op.getSpeed(absTime);
            assertEquals("Speed at abs time " + deltaT, expectedSpeed, actualSpeed.si, 0.0001);
            actualSpeed = op.getSpeed(relTime);
            assertEquals("Speed at rel time " + deltaT, expectedSpeed, actualSpeed.si, 0.0001);
            Time actualTimeAtPosition = op.timeAtDistance(new Length(fraction * path.getLength().si, LengthUnit.SI));
            assertEquals("TimeAtDistance matches time", startTime.si + deltaT, actualTimeAtPosition.si, 0.0001);
            double actualAcceleration = op.getAcceleration(absTime).si;
            assertEquals("acceleration at abs time", a.si, actualAcceleration, 0.00001);
            actualAcceleration = op.getAcceleration(relTime).si;
            assertEquals("acceleration at rel time", a.si, actualAcceleration, 0.00001);
            assertEquals("traveled distance at abs time", expectedDistance, op.getTraveledDistance(absTime).si, 0.0001);
            assertEquals("traveled distance at rel time", expectedDistance, op.getTraveledDistance(relTime).si, 0.0001);
            assertEquals("traveled distanceSI at abs time", expectedDistance, op.getTraveledDistanceSI(absTime), 0.0001);
            assertEquals("traveled distanceSI at rel time", expectedDistance, op.getTraveledDistanceSI(relTime), 0.0001);
        }
    }

    /**
     * Test the constant speed plan builder.
     * @throws OperationalPlanException when that happens uncaught; this test has failed
     * @throws OTSGeometryException when that happens uncaught; this test has failed
     */
    @Test
    public void constantSpeedPlanBuilderTest() throws OperationalPlanException, OTSGeometryException
    {
        OTSLine3D path = new OTSLine3D(new OTSPoint3D(0, 0, 0), new OTSPoint3D(1000, 0, 0));
        Time startTime = Time.valueOf("100 s");
        Speed speed = Speed.valueOf("20 m/s");
        OperationalPlan csp = OperationalPlanBuilder.buildConstantSpeedPlan(null, path, startTime, speed);
        assertEquals("path is returned", path, csp.getPath());
        assertEquals("start time is returned", startTime, csp.getStartTime());
        Time endTime = startTime.plus(path.getLength().divide(speed));
        assertEquals("endTime matches", endTime.si, csp.getEndTime().si, (endTime.si - startTime.si) / 10000);
        assertEquals("startSpeed is speed", speed, csp.getStartSpeed());
        assertEquals("endSpeed is speed", speed, csp.getEndSpeed());
        DirectedPoint endLocation = csp.getEndLocation();
        assertEquals("endLocation matched end of path", 0, path.get(path.size() - 1).distanceSI(new OTSPoint3D(endLocation)),
                0.001);
        // Test at a couple of intermediate times (this is more like testing the operational plan)
        for (int step = 0; step < 10; step++)
        {
            double fraction = step / 10d;
            Time when = startTime.plus(endTime.minus(startTime).times(fraction));
            DirectedPoint actualLocation = csp.getLocation(when);
            OTSPoint3D expectedLocation = new OTSPoint3D(path.getLocationFraction(fraction));
            assertEquals("actual location matches expected location", 0,
                    expectedLocation.distanceSI(new OTSPoint3D(actualLocation)), 0.001);
        }
    }

    /**
     * Test the builder for constant acceleration plans (misnomered gradual acceleration) and the builder for maximum
     * acceleration plans.
     * @throws OperationalPlanException when that happens uncaught; this test has failed
     * @throws OTSGeometryException when that happens uncaught; this test has failed
     */
    @Test
    public void constantAccelerationPlanBuilderTest() throws OTSGeometryException, OperationalPlanException
    {
        OTSLine3D path = new OTSLine3D(new OTSPoint3D(0, 0, 0), new OTSPoint3D(1000, 0, 0));
        Time startTime = Time.valueOf("100 s");
        for (double startSpeedDouble : new double[] {0, 10, 20, 30})
        {
            Speed startSpeed = Speed.instantiateSI(startSpeedDouble);
            for (double endSpeedDouble : new double[] {0, 10, 20, 30})
            {
                Speed endSpeed = Speed.instantiateSI(endSpeedDouble);
                if (startSpeedDouble == 0 && endSpeedDouble == 0)
                {
                    try
                    {
                        OperationalPlanBuilder.buildGradualAccelerationPlan(null, path, startTime, startSpeed, endSpeed);
                        fail("operational plan builder should have throw an OperationalPlanException when trying to build a "
                                + "constant acceleration plan with 0 start and 0 end speed and non-zero path length");
                    }
                    catch (OperationalPlanException ope)
                    {
                        // Ignore expected exception
                    }

                    try
                    {
                        OperationalPlanBuilder.buildGradualAccelerationPlan(null, path, startTime, startSpeed, endSpeed,
                                Acceleration.instantiateSI(5), Acceleration.instantiateSI(-5));
                        fail("operational plan builder should have throw an OperationalPlanException when trying to build a "
                                + "constant acceleration plan with 0 start and 0 end speed and non-zero path length");
                    }
                    catch (OperationalPlanException ope)
                    {
                        // Ignore expected exception
                    }

                    try
                    {
                        OperationalPlanBuilder.buildMaximumAccelerationPlan(null, path, startTime, startSpeed, endSpeed,
                                Acceleration.ONE, Acceleration.ONE.neg());
                        fail("operational plan builder should have throw an OperationalPlanException when trying to build a "
                                + "constant acceleration plan with 0 start and 0 end speed and non-zero path length");
                    }
                    catch (OperationalPlanException ope)
                    {
                        // Ignore expected exception
                    }

                    continue;
                }
                OperationalPlan cap =
                        OperationalPlanBuilder.buildGradualAccelerationPlan(null, path, startTime, startSpeed, endSpeed);
                assertEquals("path is returned", path, cap.getPath());
                assertEquals("start time is returned", startTime, cap.getStartTime());
                assertEquals("startSpeed is start speed", startSpeed, cap.getStartSpeed());
                assertEquals("endSpeed is end speed", endSpeed, cap.getEndSpeed());
                Time endTime = cap.getEndTime();
                Acceleration a = cap.getAcceleration(startTime);
                assertEquals("acceleration is the same at start time and end time", a.si, cap.getAcceleration(endTime).si,
                        0.0001);
                for (int step = 0; step < 10; step++)
                {
                    double fraction = step / 10d;
                    Duration fractionTime = endTime.minus(startTime).times(fraction);
                    a = cap.getAcceleration(startTime.plus(fractionTime));
                    assertEquals("acceleration is the same at any time", a.si, cap.getAcceleration(endTime).si, 0.0001);
                    // S(t) v0 * t + 0.5 * a * t * t
                    double distance = startSpeed.si * fractionTime.si + 0.5 * a.si * fractionTime.si * fractionTime.si;
                    OTSPoint3D expectedPoint = new OTSPoint3D(path.getLocationFraction(distance / path.getLengthSI()));
                    DirectedPoint p = cap.getLocation(fractionTime);
                    assertEquals("position along the way matches", 0, expectedPoint.distance(new OTSPoint3D(p)).si, 0.001);
                }
                Acceleration maximumAcceleration = Acceleration.instantiateSI(5);
                Acceleration maximumDeceleration = Acceleration.instantiateSI(-1);
                OperationalPlan map = OperationalPlanBuilder.buildMaximumAccelerationPlan(null, path, startTime, startSpeed,
                        endSpeed, maximumAcceleration, maximumDeceleration);
                if (endSpeed.si != 0) // if endSpeed == 0; path may have been reduced.
                {
                    assertEquals("path is returned", path, map.getPath());
                }
                assertEquals("start time is returned", startTime, map.getStartTime());
                assertEquals("startSpeed is start speed", startSpeed, map.getStartSpeed());
                assertEquals("endSpeed is end speed", endSpeed, cap.getEndSpeed());
            }
        }
        OperationalPlan op = OperationalPlanBuilder.buildGradualAccelerationPlan(null, path, startTime, Speed.instantiateSI(10),
                Speed.instantiateSI(100), Acceleration.ONE, Acceleration.ONE.neg());
        assertEquals("acceleration is limited", Acceleration.ONE, op.getAcceleration(startTime));
        op = OperationalPlanBuilder.buildGradualAccelerationPlan(null, path, startTime, Speed.instantiateSI(100),
                Speed.instantiateSI(10), Acceleration.ONE, Acceleration.ONE.neg());
        assertEquals("deceleration is limited", Acceleration.ONE.neg(), op.getAcceleration(startTime));
        op = OperationalPlanBuilder.buildMaximumAccelerationPlan(null, path, startTime, Speed.instantiateSI(10),
                Speed.instantiateSI(100), Acceleration.ONE, Acceleration.ONE.neg());
        assertEquals("acceleration is limited", Acceleration.ONE, op.getAcceleration(startTime));
        op = OperationalPlanBuilder.buildMaximumAccelerationPlan(null, path, startTime, Speed.instantiateSI(100),
                Speed.instantiateSI(10), Acceleration.ONE, Acceleration.ONE.neg());
        assertEquals("deceleration is limited", Acceleration.ONE.neg(), op.getAcceleration(startTime));
    }

    /**
     * Test the stop plan builder.
     * @throws OperationalPlanException when that happens uncaught; this test has failed
     * @throws OTSGeometryException when that happens uncaught; this test has failed
     */
    @Test
    public void stopPlanBuilderTest() throws OTSGeometryException, OperationalPlanException
    {
        OTSLine3D path = new OTSLine3D(new OTSPoint3D(0, 0, 0), new OTSPoint3D(1000, 0, 0));
        Time startTime = Time.valueOf("100 s");
        Acceleration deceleration = Acceleration.instantiateSI(-4);
        for (double startSpeedDouble : new double[] {10, 20, 30})
        {
            Speed startSpeed = Speed.instantiateSI(startSpeedDouble);
            OperationalPlan sp = OperationalPlanBuilder.buildStopPlan(null, path, startTime, startSpeed, deceleration);
            assertEquals("start time is returned", startTime, sp.getStartTime());
            assertEquals("startSpeed is start speed", startSpeed, sp.getStartSpeed());
            assertEquals("endSpeed is end speed", Speed.ZERO, sp.getEndSpeed());
            OTSLine3D reducedPath = sp.getPath();
            assertTrue("length of path was reduced", reducedPath.getLengthSI() < path.getLengthSI());
        }
        Speed startSpeed = Speed.instantiateSI(100);
        path = new OTSLine3D(new OTSPoint3D(0, 0, 0), new OTSPoint3D(100, 0, 0));
        OperationalPlan sp = OperationalPlanBuilder.buildStopPlan(null, path, startTime, startSpeed, deceleration);
        assertEquals("entire path is returned", path, sp.getPath());
        assertTrue("speed at end is larger than zero", sp.getEndSpeed().si > 0);
        try
        {
            OperationalPlanBuilder.buildStopPlan(null, path, startTime, Speed.ZERO, deceleration);
            fail("Path extraction should have failed");
        }
        catch (OperationalPlanException ope)
        {
            // Ignore expected exception
        }
    }

}
