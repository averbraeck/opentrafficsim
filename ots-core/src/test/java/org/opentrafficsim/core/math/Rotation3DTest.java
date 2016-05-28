package org.opentrafficsim.core.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.djunits.unit.AngleUnit;
import org.djunits.value.StorageType;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.vector.AngleVector;
import org.djunits.value.vdouble.vector.DirectionVector;
import org.junit.Test;

/**
 * Test the Rotation3D class.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Dec 11, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class Rotation3DTest
{

    /**
     * Test the constructor.
     * @throws ValueException should not happen; test has failed if it does happen
     */
    @Test
    public final void constructorTest() throws ValueException
    {
        double roll = Math.toRadians(10);
        double pitch = Math.toRadians(20);
        double yaw = Math.toRadians(30);
        Direction3D r3da = new Direction3D(roll, pitch, yaw, AngleUnit.RADIAN);
        checkRotation3D(r3da, roll, pitch, yaw);
        r3da = new Direction3D(new DirectionVector(new double[] { roll, pitch, yaw }, AngleUnit.RADIAN, StorageType.DENSE));
        checkRotation3D(r3da, roll, pitch, yaw);
        r3da = new Direction3D(new DirectionVector(new double[] { roll, pitch, yaw }, AngleUnit.RADIAN, StorageType.SPARSE));
        checkRotation3D(r3da, roll, pitch, yaw);
        r3da =
                new Direction3D(new Direction(roll, AngleUnit.RADIAN), new Direction(pitch, AngleUnit.RADIAN), new Direction(
                        yaw, AngleUnit.RADIAN));
        checkRotation3D(r3da, roll, pitch, yaw);
        try
        {
            new Direction3D(new DirectionVector(new double[] { roll, pitch }, AngleUnit.RADIAN, StorageType.DENSE));
            fail("Short vector should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
        try
        {
            new Direction3D(new DirectionVector(new double[] { roll, pitch, yaw, pitch }, AngleUnit.RADIAN, StorageType.DENSE));
            fail("Long vector should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
        try
        {
            new Direction3D(new DirectionVector(new double[] { roll, pitch }, AngleUnit.RADIAN, StorageType.SPARSE));
            fail("Short vector should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
        try
        {
            new Direction3D(new DirectionVector(new double[] { roll, pitch, yaw, pitch }, AngleUnit.RADIAN, StorageType.SPARSE));
            fail("Long vector should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
        Angle3D r3dr = new Angle3D(roll, pitch, yaw, AngleUnit.RADIAN);
        checkRotation3D(r3dr, roll, pitch, yaw);
        r3dr = new Angle3D(new AngleVector(new double[] { roll, pitch, yaw }, AngleUnit.RADIAN, StorageType.DENSE));
        checkRotation3D(r3dr, roll, pitch, yaw);
        r3dr = new Angle3D(new AngleVector(new double[] { roll, pitch, yaw }, AngleUnit.RADIAN, StorageType.SPARSE));
        checkRotation3D(r3dr, roll, pitch, yaw);
        r3dr =
                new Angle3D(new Angle(roll, AngleUnit.RADIAN), new Angle(pitch, AngleUnit.RADIAN), new Angle(yaw,
                        AngleUnit.RADIAN));
        checkRotation3D(r3dr, roll, pitch, yaw);
        try
        {
            new Angle3D(new AngleVector(new double[] { roll, pitch }, AngleUnit.RADIAN, StorageType.DENSE));
            fail("Short vector should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
        try
        {
            new Angle3D(new AngleVector(new double[] { roll, pitch, yaw, pitch }, AngleUnit.RADIAN, StorageType.DENSE));
            fail("Long vector should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
        try
        {
            new Angle3D(new AngleVector(new double[] { roll, pitch }, AngleUnit.RADIAN, StorageType.SPARSE));
            fail("Short vector should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
        try
        {
            new Angle3D(new AngleVector(new double[] { roll, pitch, yaw, pitch }, AngleUnit.RADIAN, StorageType.SPARSE));
            fail("Long vector should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
    }

    /**
     * Verify the values of the fields in a Direction3D.
     * @param r3da Direction3D; the Direction3D
     * @param roll double; the expected roll value
     * @param pitch double; the expected pitch value
     * @param yaw double; the expected yaw value
     */
    private void checkRotation3D(final Direction3D r3da, final double roll, final double pitch, final double yaw)
    {
        assertEquals("roll", roll, r3da.getRoll().si, 0.00001);
        assertEquals("pitch", pitch, r3da.getPitch().si, 0.00001);
        assertEquals("yaw", yaw, r3da.getYaw().si, 0.00001);
    }

    /**
     * Verify the values of the fields in a Angle3D.
     * @param r3dr Angle3D; the Angle3D
     * @param roll double; the expected roll value
     * @param pitch double; the expected pitch value
     * @param yaw double; the expected yaw value
     */
    private void checkRotation3D(final Angle3D r3dr, final double roll, final double pitch, final double yaw)
    {
        assertEquals("roll", roll, r3dr.getRoll().si, 0.00001);
        assertEquals("pitch", pitch, r3dr.getPitch().si, 0.00001);
        assertEquals("yaw", yaw, r3dr.getYaw().si, 0.00001);
    }

}
