package org.opentrafficsim.core.math;

import java.io.Serializable;
import java.util.Locale;

import org.djunits.unit.AngleUnit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.vector.AngleVector;

/**
 * 3D-rotation, RPY coded (longitudinal roll along the x-axis, lateral pitch along the y-axis and vertical yaw along the
 * z-axis), also called Tait–Bryan angles or Cardan angles. Angles are relative, and can relate to e.g. the inertial frame of a
 * GTU.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class Angle3d implements Serializable
{
    /** */
    private static final long serialVersionUID = 20160000L;

    /** The rotations of the rotation in 3D (RPY coded). */
    private final AngleVector rotation;

    /**
     * @param rotation AngleVector; the angles of the rotation in 3D (RPY coded)
     * @throws ValueRuntimeException in case the vector does not have exactly three elements
     */
    public Angle3d(final AngleVector rotation) throws ValueRuntimeException
    {
        if (rotation.size() != 3)
        {
            throw new ValueRuntimeException("Size of an RPY-rotation vector should be exactly 3. Got: " + rotation);
        }
        this.rotation = rotation;
    }

    /**
     * @param roll Angle; (phi) the rotation around the x-axis
     * @param pitch Angle; (theta) the rotation around the y-axis
     * @param yaw Angle; (psi) the rotation around the z-axis
     * @throws ValueRuntimeException in case the units are incorrect
     */
    public Angle3d(final Angle roll, final Angle pitch, final Angle yaw) throws ValueRuntimeException
    {
        this.rotation = new AngleVector(new Angle[] {roll, pitch, yaw}, AngleUnit.SI);
    }

    /**
     * @param roll double; (phi) the rotation around the x-axis
     * @param pitch double; (theta) the rotation around the y-axis
     * @param yaw double; (psi) the rotation around the z-axis
     * @param unit AngleUnit; the unit of the RPY parameters
     * @throws ValueRuntimeException in case the units are incorrect
     */
    public Angle3d(final double roll, final double pitch, final double yaw, final AngleUnit unit) throws ValueRuntimeException
    {
        this.rotation = new AngleVector(new double[] {roll, pitch, yaw}, unit);
    }

    /**
     * @return the roll.
     */
    public final Angle getRoll()
    {
        try
        {
            return this.rotation.get(0);
        }
        catch (ValueRuntimeException exception)
        {
            // should be impossible as we constructed the vector always with three elements
            throw new RuntimeException(
                    "getRoll() gave an exception; apparently vector " + this.rotation + " was not constructed right",
                    exception);
        }
    }

    /**
     * @return the pitch.
     */
    public final Angle getPitch()
    {
        try
        {
            return this.rotation.get(1);
        }
        catch (ValueRuntimeException exception)
        {
            // should be impossible as we constructed the vector always with three elements
            throw new RuntimeException(
                    "getPitch() gave an exception; apparently vector " + this.rotation + " was not constructed right",
                    exception);
        }
    }

    /**
     * @return the yaw.
     */
    public final Angle getYaw()
    {
        try
        {
            return this.rotation.get(2);
        }
        catch (ValueRuntimeException exception)
        {
            // should be impossible as we constructed the vector always with three elements
            throw new RuntimeException(
                    "getYaw() gave an exception; apparently vector " + this.rotation + " was not constructed right", exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return String.format(Locale.US, "Angle3d.Rel roll %s, pitch %s, yaw %s", getRoll(), getPitch(), getYaw());
    }
}
