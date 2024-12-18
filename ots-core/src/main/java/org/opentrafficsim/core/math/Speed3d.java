package org.opentrafficsim.core.math;

import java.io.Serializable;
import java.util.Locale;

import org.djunits.unit.DirectionUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.ValueRuntimeException;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.vector.SpeedVector;

/**
 * A 3D speed vector, decomposed in X, Y, and Z-speed with easy conversion from and to a spherical coordinate system. <br>
 * <a href="https://en.wikipedia.org/wiki/Spherical_coordinate_system">Physicists and mathematicians <strong>do not</strong>
 * agree on the meaning of theta and phi.</a> In this class the convention in the physics domain is used:
 * <ul>
 * <li>theta is the angle from the z direction.</li>
 * <li>phi is the projected angle in the xy-plane from the x direction.</li>
 * </ul>
 * N.B. In the geography domain yet another convention is used. <br>
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class Speed3d implements Serializable
{
    /** */
    private static final long serialVersionUID = 20160000L;

    /** The speed in 3D (XYZ coded). */
    private final SpeedVector speed;

    /**
     * Construct a new Speed3d from vector of strongly typed Cartesian coordinates.
     * @param speed the speeds in 3D (YPR coded)
     * @throws ValueRuntimeException in case the vector does not have exactly three elements
     */
    public Speed3d(final SpeedVector speed) throws ValueRuntimeException
    {
        if (speed.size() != 3)
        {
            throw new ValueRuntimeException("Size of an RPY-speed vector should be exactly 3. Got: " + speed);
        }
        this.speed = speed;
    }

    /**
     * Construct a new Speed3d from three strongly typed Cartesian coordinates.
     * @param x the speed in the x-direction
     * @param y the speed in the y-direction
     * @param z the speed in the z-direction
     * @throws ValueRuntimeException in case the units are incorrect
     */
    public Speed3d(final Speed x, final Speed y, final Speed z) throws ValueRuntimeException
    {
        this.speed = new SpeedVector(new Speed[] {x, y, z}, x.getDisplayUnit());
    }

    /**
     * Construct a new Speed3d from three double Cartesian coordinates and a speed unit.
     * @param x the speed in the x-direction
     * @param y the speed in the y-direction
     * @param z the speed in the z-direction
     * @param unit the unit of the xyz parameters
     * @throws ValueRuntimeException in case the units are incorrect
     */
    public Speed3d(final double x, final double y, final double z, final SpeedUnit unit) throws ValueRuntimeException
    {
        this.speed = new SpeedVector(new double[] {x, y, z}, unit);
    }

    /**
     * Construct a new Speed3d from a strongly typed speed and polar coordinates.
     * @param speed the speed in the direction of the angle along the vector
     * @param theta the angle from the z direction
     * @param phi the projected angle in the xy-plane from the x direction
     * @throws ValueRuntimeException in case the vector does not have exactly three elements
     */
    public Speed3d(final Speed speed, final Direction theta, final Direction phi) throws ValueRuntimeException
    {
        double[] xyz = Scalar3d.polarToCartesian(speed.getInUnit(), theta.si, phi.si);
        this.speed = new SpeedVector(xyz, speed.getDisplayUnit());
    }

    /**
     * Retrieve the x-component of this Speed3d.
     * @return the speed in the x-direction.
     */
    public final Speed getX()
    {
        try
        {
            return this.speed.get(0);
        }
        catch (ValueRuntimeException exception)
        {
            // should be impossible as we constructed the vector always with three elements
            throw new RuntimeException(
                    "getX() gave an exception; apparently vector " + this.speed + " was not constructed right", exception);
        }
    }

    /**
     * Retrieve the y-component of this Speed3d.
     * @return the speed in the y-direction.
     */
    public final Speed getY()
    {
        try
        {
            return this.speed.get(1);
        }
        catch (ValueRuntimeException exception)
        {
            // should be impossible as we constructed the vector always with three elements
            throw new RuntimeException(
                    "getY() gave an exception; apparently vector " + this.speed + " was not constructed right", exception);
        }
    }

    /**
     * Retrieve the z-component of this Speed3d.
     * @return the speed in the z-direction.
     */
    public final Speed getZ()
    {
        try
        {
            return this.speed.get(2);
        }
        catch (ValueRuntimeException exception)
        {
            // should be impossible as we constructed the vector always with three elements
            throw new RuntimeException(
                    "getZ() gave an exception; apparently vector " + this.speed + " was not constructed right", exception);
        }
    }

    /**
     * Retrieve the theta of this Speed3d.
     * @return the angle of direction perpendicular to the xy-plane
     */
    public final Direction getTheta()
    {
        return Scalar3d.cartesianToTheta(getX().si, getY().si, getZ().si);
    }

    /**
     * Retrieve the phi of this Speed3d.
     * @return the projected angle of direction in the xy-plane
     */
    public final Direction getPhi()
    {
        return Scalar3d.cartesianToPhi(getX().si, getY().si);
    }

    /**
     * Retrieve the norm of this Speed3d.
     * @return the combined speed in the direction of the angle
     */
    public final Speed getSpeed()
    {
        return new Speed(Scalar3d.cartesianToRadius(getX().si, getY().si, getZ().si), SpeedUnit.SI);
    }

    @Override
    public final String toString()
    {
        return String.format(Locale.US, "Speed3d %s (%s, theta %s, phi %s)", this.speed, getSpeed(),
                new Direction(getTheta().getInUnit(DirectionUnit.EAST_DEGREE), DirectionUnit.EAST_DEGREE),
                new Direction(getPhi().getInUnit(DirectionUnit.EAST_DEGREE), DirectionUnit.EAST_DEGREE));
    }

}
