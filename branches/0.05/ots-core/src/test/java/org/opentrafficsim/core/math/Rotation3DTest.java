package org.opentrafficsim.core.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.djunits.unit.AngleUnit;
import org.djunits.value.StorageType;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.vector.AngleVector;
import org.junit.Test;

/**
 * Test the Rotation3D class.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * @throws ValueException
     */
    @Test
    public void constructorTest() throws ValueException
    {
        double roll = Math.toRadians(10);
        double pitch = Math.toRadians(20);
        double yaw = Math.toRadians(30);
        Rotation3D.Abs r3da = new Rotation3D.Abs(roll, pitch, yaw, AngleUnit.RADIAN);
        checkRotation3D(r3da, roll, pitch, yaw);
        r3da = new Rotation3D.Abs(new AngleVector.Abs(new double[] { roll, pitch, yaw }, AngleUnit.RADIAN, StorageType.DENSE));
        checkRotation3D(r3da, roll, pitch, yaw);
        r3da = new Rotation3D.Abs(new AngleVector.Abs(new double[] { roll, pitch, yaw }, AngleUnit.RADIAN, StorageType.SPARSE));
        checkRotation3D(r3da, roll, pitch, yaw);
        r3da =
                new Rotation3D.Abs(new Angle.Abs(roll, AngleUnit.RADIAN), new Angle.Abs(pitch, AngleUnit.RADIAN),
                        new Angle.Abs(yaw, AngleUnit.RADIAN));
        checkRotation3D(r3da, roll, pitch, yaw);
        try
        {
            new Rotation3D.Abs(new AngleVector.Abs(new double[] { roll, pitch }, AngleUnit.RADIAN, StorageType.DENSE));
            fail("Short vector should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
        try
        {
            new Rotation3D.Abs(new AngleVector.Abs(new double[] { roll, pitch, yaw, pitch }, AngleUnit.RADIAN,
                    StorageType.DENSE));
            fail("Long vector should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
        try
        {
            new Rotation3D.Abs(new AngleVector.Abs(new double[] { roll, pitch }, AngleUnit.RADIAN, StorageType.SPARSE));
            fail("Short vector should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
        try
        {
            new Rotation3D.Abs(new AngleVector.Abs(new double[] { roll, pitch, yaw, pitch }, AngleUnit.RADIAN,
                    StorageType.SPARSE));
            fail("Long vector should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
        Rotation3D.Rel r3dr = new Rotation3D.Rel(roll, pitch, yaw, AngleUnit.RADIAN);
        checkRotation3D(r3dr, roll, pitch, yaw);
        r3dr = new Rotation3D.Rel(new AngleVector.Rel(new double[] { roll, pitch, yaw }, AngleUnit.RADIAN, StorageType.DENSE));
        checkRotation3D(r3dr, roll, pitch, yaw);
        r3dr = new Rotation3D.Rel(new AngleVector.Rel(new double[] { roll, pitch, yaw }, AngleUnit.RADIAN, StorageType.SPARSE));
        checkRotation3D(r3dr, roll, pitch, yaw);
        r3dr =
                new Rotation3D.Rel(new Angle.Rel(roll, AngleUnit.RADIAN), new Angle.Rel(pitch, AngleUnit.RADIAN),
                        new Angle.Rel(yaw, AngleUnit.RADIAN));
        checkRotation3D(r3dr, roll, pitch, yaw);
        try
        {
            new Rotation3D.Rel(new AngleVector.Rel(new double[] { roll, pitch }, AngleUnit.RADIAN, StorageType.DENSE));
            fail("Short vector should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
        try
        {
            new Rotation3D.Rel(new AngleVector.Rel(new double[] { roll, pitch, yaw, pitch }, AngleUnit.RADIAN,
                    StorageType.DENSE));
            fail("Long vector should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
        try
        {
            new Rotation3D.Rel(new AngleVector.Rel(new double[] { roll, pitch }, AngleUnit.RADIAN, StorageType.SPARSE));
            fail("Short vector should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
        try
        {
            new Rotation3D.Rel(new AngleVector.Rel(new double[] { roll, pitch, yaw, pitch }, AngleUnit.RADIAN,
                    StorageType.SPARSE));
            fail("Long vector should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
    }

    /**
     * Verify the values of the fields in a Rotation3D.Abs.
     * @param r3da Rotation3D.Abs; the Rotation3D.Abs
     * @param roll double; the expected roll value
     * @param pitch double; the expected pitch value
     * @param yaw double; the expected yaw value
     */
    private void checkRotation3D(final Rotation3D.Abs r3da, final double roll, final double pitch, final double yaw)
    {
        assertEquals("roll", roll, r3da.getRoll().si, 0.00001);
        assertEquals("pitch", pitch, r3da.getPitch().si, 0.00001);
        assertEquals("yaw", yaw, r3da.getYaw().si, 0.00001);
    }

    /**
     * Verify the values of the fields in a Rotation3D.Rel.
     * @param r3dr Rotation3D.Rel; the Rotation3D.Rel
     * @param roll double; the expected roll value
     * @param pitch double; the expected pitch value
     * @param yaw double; the expected yaw value
     */
    private void checkRotation3D(final Rotation3D.Rel r3dr, final double roll, final double pitch, final double yaw)
    {
        assertEquals("roll", roll, r3dr.getRoll().si, 0.00001);
        assertEquals("pitch", pitch, r3dr.getPitch().si, 0.00001);
        assertEquals("yaw", yaw, r3dr.getYaw().si, 0.00001);
    }

}
