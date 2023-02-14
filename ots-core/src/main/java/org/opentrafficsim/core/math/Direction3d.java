package org.opentrafficsim.core.math;

import java.io.Serializable;
import java.util.Locale;

import org.djunits.unit.DirectionUnit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.storage.StorageType;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.vector.DirectionVector;
import org.djunits.value.vdouble.vector.base.DoubleVector;

/**
 * 3D-rotation, RPY coded (longitudinal roll along the x-axis, lateral pitch along the y-axis and vertical yaw along the
 * z-axis), also called Tait–Bryan angles or Cardan angles. Angles are absolute and relate to the absolute XYZ-frame of the
 * world.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class Direction3d implements Serializable
{
    /** */
    private static final long serialVersionUID = 20160000L;

    /** The angles of the rotation in 3D (RPY coded). */
    private final DirectionVector rotation;

    /**
     * @param rotation DirectionVector; the angles in 3D (RPY coded)
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
     * @param roll Direction; (phi) the rotation around the x-axis
     * @param pitch Direction; (theta) the rotation around the y-axis
     * @param yaw Direction; (psi) the rotation around the z-axis
     * @throws ValueRuntimeException in case the units are incorrect
     */
    public Direction3d(final Direction roll, final Direction pitch, final Direction yaw) throws ValueRuntimeException
    {
        this.rotation = DoubleVector.instantiate(new Direction[] {roll, pitch, yaw}, roll.getDisplayUnit(), StorageType.DENSE);
    }

    /**
     * @param roll double; (phi) the rotation around the x-axis
     * @param pitch double; (theta) the rotation around the y-axis
     * @param yaw double; (psi) the rotation around the z-axis
     * @param unit DirectionUnit; the unit of the RPY parameters
     * @throws ValueRuntimeException in case the units are incorrect
     */
    public Direction3d(final double roll, final double pitch, final double yaw, final DirectionUnit unit)
            throws ValueRuntimeException
    {
        this.rotation = DoubleVector.instantiate(new double[] {roll, pitch, yaw}, unit, StorageType.DENSE);
    }

    /**
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

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return String.format(Locale.US, "Rotation3d.Abs roll %s, pitch %s, yaw %s", getRoll(), getPitch(), getYaw());
    }
}
