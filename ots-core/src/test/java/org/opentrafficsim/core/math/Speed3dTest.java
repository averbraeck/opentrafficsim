package org.opentrafficsim.core.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import org.djunits.unit.DirectionUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.storage.StorageType;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.vector.SpeedVector;
import org.junit.jupiter.api.Test;

/**
 * Test the Speed3d class.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class Speed3dTest
{

    /**
     * Constructor.
     */
    public Speed3dTest()
    {
        //
    }

    /**
     * Test the constructors and getters of the Speed3d class.
     * @throws ValueRuntimeException should not happen; test has failed if it does happen
     */
    @Test
    public final void speed3DTest() throws ValueRuntimeException
    {
        double x = 2.2;
        double y = 3.3;
        double z = 5.5;
        Speed3d s3d = new Speed3d(x, y, z, SpeedUnit.SI);
        checkSpeed(s3d, x, y, z);
        SpeedVector sv = new SpeedVector(new double[] {x, y, z}, SpeedUnit.SI);
        s3d = new Speed3d(sv);
        checkSpeed(s3d, x, y, z);
        sv = new SpeedVector(new double[] {x, y, z}, SpeedUnit.SI, StorageType.SPARSE);
        s3d = new Speed3d(sv);
        checkSpeed(s3d, x, y, z);
        sv = new SpeedVector(new double[] {x, y}, SpeedUnit.SI);
        try
        {
            new Speed3d(sv);
            fail("Short vector should have thrown an exception");
        }
        catch (ValueRuntimeException ve)
        {
            // Ignore expected exception
        }
        sv = new SpeedVector(new double[] {x, y, z, x}, SpeedUnit.SI);
        try
        {
            new Speed3d(sv);
            fail("Long vector should have thrown an exception");
        }
        catch (ValueRuntimeException ve)
        {
            // Ignore expected exception
        }
        sv = new SpeedVector(new double[] {x, y}, SpeedUnit.SI, StorageType.SPARSE);
        try
        {
            new Speed3d(sv);
            fail("Short vector should have thrown an exception");
        }
        catch (ValueRuntimeException ve)
        {
            // Ignore expected exception
        }
        sv = new SpeedVector(new double[] {x, y, z, x}, SpeedUnit.SI, StorageType.SPARSE);
        try
        {
            new Speed3d(sv);
            fail("Long vector should have thrown an exception");
        }
        catch (ValueRuntimeException ve)
        {
            // Ignore expected exception
        }
        s3d = new Speed3d(Speed.ofSI(x), Speed.ofSI(y), Speed.ofSI(z));
        checkSpeed(s3d, x, y, z);
        s3d = new Speed3d(new Speed(x, SpeedUnit.KM_PER_HOUR), new Speed(y, SpeedUnit.KM_PER_HOUR),
                new Speed(z, SpeedUnit.KM_PER_HOUR));
        checkSpeed(s3d, x / 3.6, y / 3.6, z / 3.6);
        double theta = Math.PI * 0.4;
        double phi = Math.PI * 0.3;
        double length = 10;
        s3d = new Speed3d(Speed.ofSI(length), new Direction(theta, DirectionUnit.EAST_RADIAN),
                new Direction(phi, DirectionUnit.EAST_RADIAN));
        checkSpeed(s3d, length * Math.cos(phi) * Math.sin(theta), length * Math.sin(phi) * Math.sin(theta),
                length * Math.cos(theta));
        assertTrue(s3d.toString().contains("Speed3d"), "toString output contains class name");
    }

    /**
     * Verify the x, y, z, theta, phi and norm of a Speed3d object.
     * @param s3d the Speed3d object
     * @param x the expected x value
     * @param y the expected y value
     * @param z the expected z value
     */
    private void checkSpeed(final Speed3d s3d, final double x, final double y, final double z)
    {
        assertEquals(x, s3d.getX().si, 0.00001, "x");
        assertEquals(y, s3d.getY().si, 0.00001, "y");
        assertEquals(z, s3d.getZ().si, 0.00001, "z");
        assertEquals(Math.atan2(y, x), s3d.getPhi().si, 0.00001, "phi");
        assertEquals(Math.atan2(Math.sqrt(y * y + x * x), z), s3d.getTheta().si, 0.00001, "theta");
        assertEquals(Math.sqrt(x * x + y * y + z * z), s3d.getSpeed().si, 0.001, "norm");
    }
}
