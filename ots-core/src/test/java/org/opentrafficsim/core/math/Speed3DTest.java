package org.opentrafficsim.core.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import org.djunits.unit.DirectionUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.storage.StorageType;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.vector.SpeedVector;
import org.djunits.value.vdouble.vector.base.DoubleVector;
import org.junit.Test;

/**
 * Test the Speed3D class.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class Speed3DTest
{

    /**
     * Test the constructors and getters of the Speed3D class.
     * @throws ValueRuntimeException should not happen; test has failed if it does happen
     */
    @Test
    public final void speed3DTest() throws ValueRuntimeException
    {
        double x = 2.2;
        double y = 3.3;
        double z = 5.5;
        Speed3D s3d = new Speed3D(x, y, z, SpeedUnit.SI);
        checkSpeed(s3d, x, y, z);
        SpeedVector sv = DoubleVector.instantiate(new double[] {x, y, z}, SpeedUnit.SI, StorageType.DENSE);
        s3d = new Speed3D(sv);
        checkSpeed(s3d, x, y, z);
        sv = DoubleVector.instantiate(new double[] {x, y, z}, SpeedUnit.SI, StorageType.SPARSE);
        s3d = new Speed3D(sv);
        checkSpeed(s3d, x, y, z);
        sv = DoubleVector.instantiate(new double[] {x, y}, SpeedUnit.SI, StorageType.DENSE);
        try
        {
            new Speed3D(sv);
            fail("Short vector should have thrown an exception");
        }
        catch (ValueRuntimeException ve)
        {
            // Ignore expected exception
        }
        sv = DoubleVector.instantiate(new double[] {x, y, z, x}, SpeedUnit.SI, StorageType.DENSE);
        try
        {
            new Speed3D(sv);
            fail("Long vector should have thrown an exception");
        }
        catch (ValueRuntimeException ve)
        {
            // Ignore expected exception
        }
        sv = DoubleVector.instantiate(new double[] {x, y}, SpeedUnit.SI, StorageType.SPARSE);
        try
        {
            new Speed3D(sv);
            fail("Short vector should have thrown an exception");
        }
        catch (ValueRuntimeException ve)
        {
            // Ignore expected exception
        }
        sv = DoubleVector.instantiate(new double[] {x, y, z, x}, SpeedUnit.SI, StorageType.SPARSE);
        try
        {
            new Speed3D(sv);
            fail("Long vector should have thrown an exception");
        }
        catch (ValueRuntimeException ve)
        {
            // Ignore expected exception
        }
        s3d = new Speed3D(Speed.instantiateSI(x), Speed.instantiateSI(y), Speed.instantiateSI(z));
        checkSpeed(s3d, x, y, z);
        s3d = new Speed3D(new Speed(x, SpeedUnit.KM_PER_HOUR), new Speed(y, SpeedUnit.KM_PER_HOUR),
                new Speed(z, SpeedUnit.KM_PER_HOUR));
        checkSpeed(s3d, x / 3.6, y / 3.6, z / 3.6);
        double theta = Math.PI * 0.4;
        double phi = Math.PI * 0.3;
        double length = 10;
        s3d = new Speed3D(Speed.instantiateSI(length), new Direction(theta, DirectionUnit.EAST_RADIAN),
                new Direction(phi, DirectionUnit.EAST_RADIAN));
        checkSpeed(s3d, length * Math.cos(phi) * Math.sin(theta), length * Math.sin(phi) * Math.sin(theta),
                length * Math.cos(theta));
        assertTrue("toString output contains class name", s3d.toString().contains("Speed3D"));
    }

    /**
     * Verify the x, y, z, theta, phi and norm of a Speed3D object.
     * @param s3d Speed3D; the Speed3D object
     * @param x double; the expected x value
     * @param y double; the expected y value
     * @param z double; the expected z value
     */
    private void checkSpeed(final Speed3D s3d, final double x, final double y, final double z)
    {
        assertEquals("x", x, s3d.getX().si, 0.00001);
        assertEquals("y", y, s3d.getY().si, 0.00001);
        assertEquals("z", z, s3d.getZ().si, 0.00001);
        assertEquals("phi", Math.atan2(y, x), s3d.getPhi().si, 0.00001);
        assertEquals("theta", Math.atan2(Math.sqrt(y * y + x * x), z), s3d.getTheta().si, 0.00001);
        assertEquals("norm", Math.sqrt(x * x + y * y + z * z), s3d.getSpeed().si, 0.001);
    }
}
