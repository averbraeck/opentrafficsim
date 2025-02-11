package org.opentrafficsim.core.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.djunits.unit.AngleUnit;
import org.djunits.unit.DirectionUnit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.storage.StorageType;
import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.vector.AngleVector;
import org.djunits.value.vdouble.vector.DirectionVector;
import org.junit.jupiter.api.Test;

/**
 * Test the Direction3d and Angle3d classes. These classes are extremely similar. An Angle3d is a relative, a Direction is
 * absolute.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class Rotation3dTest
{

    /**
     * Constructor.
     */
    public Rotation3dTest()
    {
        //
    }

    /**
     * Test the constructors and getters.
     * @throws ValueRuntimeException should not happen; test has failed if it does happen
     */
    @Test
    public final void constructorTest() throws ValueRuntimeException
    {
        double roll = Math.toRadians(10);
        double pitch = Math.toRadians(20);
        double yaw = Math.toRadians(30);
        Direction3d r3d = new Direction3d(roll, pitch, yaw, DirectionUnit.EAST_RADIAN);
        checkRotation3d(r3d, roll, pitch, yaw);
        r3d = new Direction3d(new DirectionVector(new double[] {roll, pitch, yaw}, DirectionUnit.EAST_RADIAN));
        checkRotation3d(r3d, roll, pitch, yaw);
        r3d = new Direction3d(
                new DirectionVector(new double[] {roll, pitch, yaw}, DirectionUnit.EAST_RADIAN, StorageType.SPARSE));
        checkRotation3d(r3d, roll, pitch, yaw);
        r3d = new Direction3d(new Direction(roll, DirectionUnit.EAST_RADIAN), new Direction(pitch, DirectionUnit.EAST_RADIAN),
                new Direction(yaw, DirectionUnit.EAST_RADIAN));
        checkRotation3d(r3d, roll, pitch, yaw);
        try
        {
            new Direction3d(new DirectionVector(new double[] {roll, pitch}, DirectionUnit.EAST_RADIAN));
            fail("Short vector should have thrown an exception");
        }
        catch (ValueRuntimeException ve)
        {
            // Ignore expected exception
        }
        try
        {
            new Direction3d(
                    new DirectionVector(new double[] {roll, pitch, yaw, pitch}, DirectionUnit.EAST_RADIAN, StorageType.DENSE));
            fail("Long vector should have thrown an exception");
        }
        catch (ValueRuntimeException ve)
        {
            // Ignore expected exception
        }
        try
        {
            new Direction3d(new DirectionVector(new double[] {roll, pitch}, DirectionUnit.EAST_RADIAN, StorageType.SPARSE));
            fail("Short vector should have thrown an exception");
        }
        catch (ValueRuntimeException ve)
        {
            // Ignore expected exception
        }
        try
        {
            new Direction3d(
                    new DirectionVector(new double[] {roll, pitch, yaw, pitch}, DirectionUnit.EAST_RADIAN, StorageType.SPARSE));
            fail("Long vector should have thrown an exception");
        }
        catch (ValueRuntimeException ve)
        {
            // Ignore expected exception
        }
        assertTrue(r3d.toString().contains("Rotation3d"), "toString contains class name");
        Angle3d a3d = new Angle3d(roll, pitch, yaw, AngleUnit.RADIAN);
        checkRotation3d(a3d, roll, pitch, yaw);
        a3d = new Angle3d(new AngleVector(new double[] {roll, pitch, yaw}, AngleUnit.RADIAN));
        checkRotation3d(a3d, roll, pitch, yaw);
        a3d = new Angle3d(new AngleVector(new double[] {roll, pitch, yaw}, AngleUnit.RADIAN, StorageType.SPARSE));
        checkRotation3d(a3d, roll, pitch, yaw);
        a3d = new Angle3d(new Angle(roll, AngleUnit.RADIAN), new Angle(pitch, AngleUnit.RADIAN),
                new Angle(yaw, AngleUnit.RADIAN));
        checkRotation3d(a3d, roll, pitch, yaw);
        try
        {
            new Angle3d(new AngleVector(new double[] {roll, pitch}, AngleUnit.RADIAN));
            fail("Short vector should have thrown an exception");
        }
        catch (ValueRuntimeException ve)
        {
            // Ignore expected exception
        }
        try
        {
            new Angle3d(new AngleVector(new double[] {roll, pitch, yaw, pitch}, AngleUnit.RADIAN));
            fail("Long vector should have thrown an exception");
        }
        catch (ValueRuntimeException ve)
        {
            // Ignore expected exception
        }
        try
        {
            new Angle3d(new AngleVector(new double[] {roll, pitch}, AngleUnit.RADIAN, StorageType.SPARSE));
            fail("Short vector should have thrown an exception");
        }
        catch (ValueRuntimeException ve)
        {
            // Ignore expected exception
        }
        try
        {
            new Angle3d(new AngleVector(new double[] {roll, pitch, yaw, pitch}, AngleUnit.RADIAN));
            fail("Long vector should have thrown an exception");
        }
        catch (ValueRuntimeException ve)
        {
            // Ignore expected exception
        }
        assertTrue(a3d.toString().contains("Angle3d"), "toString contains class name");
    }

    /**
     * Verify the values of the fields in a Direction3d.
     * @param r3da the Direction3d
     * @param roll the expected roll value
     * @param pitch the expected pitch value
     * @param yaw the expected yaw value
     */
    private void checkRotation3d(final Direction3d r3da, final double roll, final double pitch, final double yaw)
    {
        assertEquals(roll, r3da.getRoll().si, 0.00001, "roll");
        assertEquals(pitch, r3da.getPitch().si, 0.00001, "pitch");
        assertEquals(yaw, r3da.getYaw().si, 0.00001, "yaw");
    }

    /**
     * Verify the values of the fields in a Angle3d.
     * @param r3dr the Angle3d
     * @param roll the expected roll value
     * @param pitch the expected pitch value
     * @param yaw the expected yaw value
     */
    private void checkRotation3d(final Angle3d r3dr, final double roll, final double pitch, final double yaw)
    {
        assertEquals(roll, r3dr.getRoll().si, 0.00001, "roll");
        assertEquals(pitch, r3dr.getPitch().si, 0.00001, "pitch");
        assertEquals(yaw, r3dr.getYaw().si, 0.00001, "yaw");
    }

}
