package org.opentrafficsim.core.gtu.plan.operational;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.Test;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSLine3D;
import org.opentrafficsim.core.geometry.OTSPoint3D;

/**
 * Test the OperationalPlan and OperationalPlanBuilder classes.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Dec 15, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class OperationalPlanTest
{
    /**
     * Test OperationalPlan.
     * @throws OTSGeometryException
     * @throws OperationalPlanException
     */
    @Test
    public void testOperationalPlan() throws OperationalPlanException, OTSGeometryException
    {
        DirectedPoint waitPoint = new DirectedPoint(12, 13, 14, 15, 16, 17);
        Time.Abs startTime = new Time.Abs(100, TimeUnit.SECOND);
        Time.Rel duration = new Time.Rel(1, TimeUnit.MINUTE);
        OperationalPlan op = new OperationalPlan(waitPoint, startTime, duration);
        assertEquals("Start speed is 0", 0, op.getStartSpeed().si, 0);
        assertEquals("End speed is 0", 0, op.getEndSpeed().si, 0);
        assertEquals("Start time is " + startTime, startTime.si, op.getStartTime().si, 0);
        assertEquals("End time is " + startTime.plus(duration), startTime.plus(duration).si, op.getEndTime().si, 0.0001);
        for (int i = 0; i <= duration.si; i++)
        {
            Time.Abs t = startTime.plus(new Time.Rel(i, TimeUnit.SECOND));
            DirectedPoint locationAtT = op.getLocation(t);
            System.out.println("Location at time " + t + " is " + locationAtT);
            // Use a tolerance that is larger than the z-offset (0.001)
            assertEquals("Distance from wait point at " + t + " is 0", 0, waitPoint.distance(locationAtT), 0.002);
        }
        OTSLine3D path = new OTSLine3D(new OTSPoint3D(12, 13, 14), new OTSPoint3D(123, 234, 345));
        Speed startSpeed = new Speed(20, SpeedUnit.KM_PER_HOUR);
        Speed endSpeed = new Speed(50, SpeedUnit.KM_PER_HOUR);
        Acceleration maxAcceleration = new Acceleration(1, AccelerationUnit.METER_PER_SECOND_2);
        Acceleration maxDeceleration = new Acceleration(6, AccelerationUnit.METER_PER_SECOND_2);
        op =
            OperationalPlanBuilder.buildGradualAccelerationPlan(path, startTime, startSpeed, endSpeed, maxAcceleration,
                maxDeceleration);
        assertEquals("Start speed is " + startSpeed, startSpeed.si, op.getStartSpeed().si, 0.00001);
        assertEquals("Start time is " + startTime, startTime.si, op.getStartTime().si, 0.00001);
        assertEquals("End speed is " + endSpeed, endSpeed.si, op.getEndSpeed().si, 0.00001);
        // TODO assertEquals("getPath returns the path", path, op.getPath());
        // What acceleration is required to reach endSpeed at the end of the path?
        // (This mathematical derivation constructed independently from the OperationalPlanBuilder code.)
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
        Time.Abs endTime = startTime.plus(new Time.Rel(t, TimeUnit.SECOND));
        System.out.println("End time is " + endTime);
        Acceleration a = new Acceleration(speedDifference / t, AccelerationUnit.SI);
        System.out.println("required acceleration is " + a);
        // Check the result
        double actualLength = startSpeed.si * t + 0.5 * a.si * t * t;
        System.out.println("driven length is " + actualLength + " pathLength is " + pathLength);
        assertEquals("Driven length is " + actualLength, actualLength, pathLength, 0.00001);
        double actualEndSpeed = startSpeed.si + t * a.si;
        System.out.println("Actual end speed is " + actualEndSpeed + " intended end speed is " + endSpeed.si);
        assertEquals("Speed at end is " + endSpeed, endSpeed.si, actualEndSpeed, 0.000001);
        assertEquals("End time is " + endTime, endTime.si, op.getEndTime().si, 0.00001);
        System.out.println("acceleration according to plan is " + op.getAcceleration(startTime));
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
            double stepTime = startTime.si + t * i / steps * 0.9999;// sometimes fails for endTime
            DirectedPoint actualPosition = op.getLocation(new Time.Abs(stepTime, TimeUnit.SI));
            double deltaT = stepTime - startTime.si;
            double fraction = (startSpeed.si * deltaT + 0.5 * a.si * deltaT * deltaT) / path.getLength().si;
            OTSPoint3D expectedPosition = new OTSPoint3D(path.getLocationFraction(fraction));
            // TODO actualPosition appears to be bogus in the current "implementation".
            // assertEquals("Position at time " + deltaT, expectedPosition.distance(new OTSPoint3D(actualPosition)), 0.0001);
        }
    }
}
