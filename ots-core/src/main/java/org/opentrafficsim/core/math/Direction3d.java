package org.opentrafficsim.core.math;

import java.io.Serializable;
import java.util.Locale;

import org.djunits.unit.DirectionUnit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.vector.DirectionVector;

/**
 * 3D-rotation, RPY coded (longitudinal roll along the x-axis, lateral pitch along the y-axis and vertical yaw along the
 * z-axis), also called Tait–Bryan angles or Cardan angles. Angles are absolute and relate to the absolute XYZ-frame of the
 * world.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class Direction3d implements Serializable
{
    /** */
    private static final long serialVersionUID = 20160000L;

    /** The angles of the rotation in 3D (RPY coded). */
    private final DirectionVector rotation;

    /**
     * Constructor.
     * @param rotation the angles in 3D (RPY coded)
     * @throws ValueRuntimeException in case the vector does not have exactly three elements
     */
    public Direction3d(final DirectionVector rotation) throws ValueRuntimeException
    {
        if (rotation.size() != 3)
        {
            throw new ValueRuntimeException("Size of an RPY-rotation vector should be exactly 3. Got: " + rotation);
        }
        this.rotation = rotation;
    }

    /**
     * Constructor.
     * @param roll (phi) the rotation around the x-axis
     * @param pitch (theta) the rotation around the y-axis
     * @param yaw (psi) the rotation around the z-axis
     * @throws ValueRuntimeException in case the units are incorrect
     */
    public Direction3d(final Direction roll, final Direction pitch, final Direction yaw) throws ValueRuntimeException
    {
        this.rotation = new DirectionVector(new Direction[] {roll, pitch, yaw}, roll.getDisplayUnit());
    }

    /**
     * Constructor.
     * @param roll (phi) the rotation around the x-axis
     * @param pitch (theta) the rotation around the y-axis
     * @param yaw (psi) the rotation around the z-axis
     * @param unit the unit of the RPY parameters
     * @throws ValueRuntimeException in case the units are incorrect
     */
    public Direction3d(final double roll, final double pitch, final double yaw, final DirectionUnit unit)
            throws ValueRuntimeException
    {
        this.rotation = new DirectionVector(new double[] {roll, pitch, yaw}, unit);
    }

    /**
     * Returns the roll.
     * @return the roll.
     */
    public final Direction getRoll()
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
     * Returns the pitch.
     * @return the pitch.
     */
    public final Direction getPitch()
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
     * Returns the yaw.
     * @return the yaw.
     */
    public final Direction getYaw()
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

    @Override
    public final String toString()
    {
        return String.format(Locale.US, "Rotation3d.Abs roll %s, pitch %s, yaw %s", getRoll(), getPitch(), getYaw());
    }
}
