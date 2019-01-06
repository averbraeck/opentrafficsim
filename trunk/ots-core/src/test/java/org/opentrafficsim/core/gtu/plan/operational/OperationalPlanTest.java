package org.opentrafficsim.core.gtu.plan.operational;

import static org.junit.Assert.assertEquals;
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
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;

import nl.tudelft.simulation.language.d3.DirectedPoint;

/**
 * Test the OperationalPlan and OperationalPlanBuilder classes.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Dec 15, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
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
        Time startTime = new Time(100, TimeUnit.BASE);
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
            op.getLocation(new Time(99.5, TimeUnit.BASE));
            fail("getLocation for absolute time before start time should have thrown an OperationalPlanException");
        }
        catch (OperationalPlanException ope)
        {
            // Ignore expected exception
        }
        op.getLocation(new Time(100.1, TimeUnit.BASE)); // Should NOT throw an exception
        op.getLocation(new Time(159.9, TimeUnit.BASE)); // Should NOT throw an exception
        try
        {
            op.getLocation(new Time(160.1, TimeUnit.BASE));
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
            Time absTime = new Time(stepTime, TimeUnit.BASE);
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
}
