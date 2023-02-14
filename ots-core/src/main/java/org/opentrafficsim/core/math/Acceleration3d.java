package org.opentrafficsim.core.math;

import java.io.Serializable;
import java.util.Locale;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.DirectionUnit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.storage.StorageType;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.vector.AccelerationVector;
import org.djunits.value.vdouble.vector.base.DoubleVector;

/**
 * A 3D acceleration vector, decomposed in X, Y, and Z-acceleration with easy conversion from and to a spherical coordinate
 * system. <br>
 * <a href="https://en.wikipedia.org/wiki/Spherical_coordinate_system">Physicists and mathematicians <strong>do not</strong>
 * agree on the meaning of theta and phi.</a> In this class the convention in the physics domain is used:
 * <ul>
 * <li>theta is the angle from the z direction.</li>
 * <li>phi is the projected angle in the xy-plane from the x direction.</li>
 * </ul>
 * N.B. In the geography domain yet another convention is used. <br>
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class Acceleration3d implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150000L;

    /** The acceleration in 3D (XYZ coded). */
    private final AccelerationVector acceleration;

    /**
     * Construct a new Acceleration3d from vector of strongly typed Cartesian coordinates.
     * @param acceleration AccelerationVector; the accelerations in 3D (YPR coded)
     * @throws ValueRuntimeException in case the vector does not have exactly three elements
     */
    public Acceleration3d(final AccelerationVector acceleration) throws ValueRuntimeException
    {
        if (acceleration.size() != 3)
        {
            throw new ValueRuntimeException("Size of an RPY-acceleration vector should be exactly 3. Got: " + acceleration);
        }
        this.acceleration = acceleration;
    }

    /**
     * Construct a new Acceleration3d from three strongly typed Cartesian coordinates.
     * @param x Acceleration; the acceleration in the x-direction
     * @param y Acceleration; the acceleration in the y-direction
     * @param z Acceleration; the acceleration in the z-direction
     * @throws ValueRuntimeException in case the units are incorrect
     */
    public Acceleration3d(final Acceleration x, final Acceleration y, final Acceleration z) throws ValueRuntimeException
    {
        this.acceleration = DoubleVector.instantiate(new Acceleration[] {x, y, z}, AccelerationUnit.SI, StorageType.DENSE);
    }

    /**
     * Construct a new Acceleration3d from three double Cartesian coordinates and a acceleration unit.
     * @param x double; the acceleration in the x-direction
     * @param y double; the acceleration in the y-direction
     * @param z double; the acceleration in the z-direction
     * @param unit AccelerationUnit; the unit of the xyz parameters
     * @throws ValueRuntimeException in case the units are incorrect
     */
    public Acceleration3d(final double x, final double y, final double z, final AccelerationUnit unit)
            throws ValueRuntimeException
    {
        this.acceleration = DoubleVector.instantiate(new double[] {x, y, z}, unit, StorageType.DENSE);
    }

    /**
     * Construct a new Acceleration3d from a strongly typed acceleration and polar coordinates.
     * @param acceleration Acceleration; the acceleration in the direction of the angle along the vector
     * @param theta Direction; the angle from the z direction
     * @param phi Direction; the projected angle in the xy-plane from the x direction
     * @throws ValueRuntimeException in case the vector does not have exactly three elements
     */
    public Acceleration3d(final Acceleration acceleration, final Direction theta, final Direction phi)
            throws ValueRuntimeException
    {
        double[] xyz = Scalar3d.polarToCartesian(acceleration.getInUnit(), theta.si, phi.si);
        this.acceleration = DoubleVector.instantiate(xyz, acceleration.getDisplayUnit(), StorageType.DENSE);
    }

    /**
     * Retrieve the x-component of this Acceleration3d.
     * @return the acceleration in the x-direction.
     */
    public final Acceleration getX()
    {
        try
        {
            return this.acceleration.get(0);
        }
        catch (ValueRuntimeException exception)
        {
            // should be impossible as we constructed the vector always with three elements
            throw new RuntimeException(
                    "getX() gave an exception; apparently vector " + this.acceleration + " was not constructed right",
                    exception);
        }
    }

    /**
     * Retrieve the y-component of this Acceleration3d.
     * @return the acceleration in the y-direction.
     */
    public final Acceleration getY()
    {
        try
        {
            return this.acceleration.get(1);
        }
        catch (ValueRuntimeException exception)
        {
            // should be impossible as we constructed the vector always with three elements
            throw new RuntimeException(
                    "getY() gave an exception; apparently vector " + this.acceleration + " was not constructed right",
                    exception);
        }
    }

    /**
     * Retrieve the z-component of this Acceleration3d.
     * @return the acceleration in the z-direction.
     */
    public final Acceleration getZ()
    {
        try
        {
            return this.acceleration.get(2);
        }
        catch (ValueRuntimeException exception)
        {
            // should be impossible as we constructed the vector always with three elements
            throw new RuntimeException(
                    "getZ() gave an exception; apparently vector " + this.acceleration + " was not constructed right",
                    exception);
        }
    }

    /**
     * Retrieve the theta of this Acceleration3d.
     * @return the angle of direction perpendicular to the xy-plane
     */
    public final Direction getTheta()
    {
        return Scalar3d.cartesianToTheta(getX().si, getY().si, getZ().si);
    }

    /**
     * Retrieve the phi of this Acceleration3d.
     * @return the projected angle of direction in the xy-plane
     */
    public final Direction getPhi()
    {
        return Scalar3d.cartesianToPhi(getX().si, getY().si);
    }

    /**
     * Retrieve the norm of this Acceleration3d.
     * @return the combined acceleration in the direction of the angle
     */
    public final Acceleration getAcceleration()
    {
        return new Acceleration(Scalar3d.cartesianToRadius(getX().si, getY().si, getZ().si), AccelerationUnit.SI);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return String.format(Locale.US, "Acceleration3d %s (%s, theta %s, phi %s)", this.acceleration, getAcceleration(),
                new Direction(getTheta().getInUnit(DirectionUnit.EAST_DEGREE), DirectionUnit.EAST_DEGREE),
                new Direction(getPhi().getInUnit(DirectionUnit.EAST_DEGREE), DirectionUnit.EAST_DEGREE));
    }

}
