package org.opentrafficsim.core.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DirectionUnit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.storage.StorageType;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.vector.AccelerationVector;
import org.djunits.value.vdouble.vector.base.DoubleVector;
import org.junit.Test;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class Acceleration3DTest
{

    /**
     * Test the constructors and getters of the Acceleration3D class.
     * @throws ValueRuntimeException Should not happen; test fails if it does
     */
    @Test
    public final void acceleration3DTest() throws ValueRuntimeException
    {
        double x = 2.2;
        double y = 3.3;
        double z = 5.5;
        Acceleration3D a3d = new Acceleration3D(x, y, z, AccelerationUnit.SI);
        checkAcceleration(a3d, x, y, z);
        AccelerationVector sv = DoubleVector.instantiate(new double[] {x, y, z}, AccelerationUnit.SI, StorageType.DENSE);
        a3d = new Acceleration3D(sv);
        checkAcceleration(a3d, x, y, z);
        sv = DoubleVector.instantiate(new double[] {x, y, z}, AccelerationUnit.SI, StorageType.SPARSE);
        a3d = new Acceleration3D(sv);
        checkAcceleration(a3d, x, y, z);
        sv = DoubleVector.instantiate(new double[] {x, y}, AccelerationUnit.SI, StorageType.DENSE);
        try
        {
            new Acceleration3D(sv);
            fail("Short vector should have thrown an exception");
        }
        catch (ValueRuntimeException ve)
        {
            // Ignore expected exception
        }
        sv = DoubleVector.instantiate(new double[] {x, y, z, x}, AccelerationUnit.SI, StorageType.DENSE);
        try
        {
            new Acceleration3D(sv);
            fail("Long vector should have thrown an exception");
        }
        catch (ValueRuntimeException ve)
        {
            // Ignore expected exception
        }
        sv = DoubleVector.instantiate(new double[] {x, y}, AccelerationUnit.SI, StorageType.SPARSE);
        try
        {
            new Acceleration3D(sv);
            fail("Short vector should have thrown an exception");
        }
        catch (ValueRuntimeException ve)
        {
            // Ignore expected exception
        }
        sv = DoubleVector.instantiate(new double[] {x, y, z, x}, AccelerationUnit.SI, StorageType.SPARSE);
        try
        {
            new Acceleration3D(sv);
            fail("Long vector should have thrown an exception");
        }
        catch (ValueRuntimeException ve)
        {
            // Ignore expected exception
        }
        a3d = new Acceleration3D(new Acceleration(x, AccelerationUnit.SI), new Acceleration(y, AccelerationUnit.SI),
                new Acceleration(z, AccelerationUnit.SI));
        checkAcceleration(a3d, x, y, z);
        a3d = new Acceleration3D(new Acceleration(x, AccelerationUnit.STANDARD_GRAVITY),
                new Acceleration(y, AccelerationUnit.STANDARD_GRAVITY), new Acceleration(z, AccelerationUnit.STANDARD_GRAVITY));
        checkAcceleration(a3d, x * 9.80665, y * 9.80665, z * 9.80665);
        double theta = Math.PI * 0.4;
        double phi = Math.PI * 0.3;
        double length = 10;
        a3d = new Acceleration3D(new Acceleration(length, AccelerationUnit.SI), new Direction(theta, DirectionUnit.EAST_RADIAN),
                new Direction(phi, DirectionUnit.EAST_RADIAN));
        checkAcceleration(a3d, length * Math.cos(phi) * Math.sin(theta), length * Math.sin(phi) * Math.sin(theta),
                length * Math.cos(theta));
        assertTrue("toString output contains the class name", a3d.toString().contains("Acceleration3D"));
    }

    /**
     * Verify the x, y, z, theta, phi and norm of a Acceleration3D object.
     * @param s3d Acceleration3D; the Acceleration3D object
     * @param x double; the expected x value
     * @param y double; the expected y value
     * @param z double; the expected z value
     */
    private void checkAcceleration(final Acceleration3D s3d, final double x, final double y, final double z)
    {
        assertEquals("x", x, s3d.getX().si, 0.00001);
        assertEquals("y", y, s3d.getY().si, 0.00001);
        assertEquals("z", z, s3d.getZ().si, 0.00001);
        assertEquals("phi", Math.atan2(y, x), s3d.getPhi().si, 0.00001);
        assertEquals("theta", Math.atan2(Math.sqrt(y * y + x * x), z), s3d.getTheta().si, 0.00001);
        assertEquals("norm", Math.sqrt(x * x + y * y + z * z), s3d.getAcceleration().si, 0.001);
    }

}
