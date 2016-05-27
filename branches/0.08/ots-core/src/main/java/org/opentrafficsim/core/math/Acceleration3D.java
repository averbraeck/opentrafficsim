package org.opentrafficsim.core.math;

import java.util.Locale;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.AngleUnit;
import org.djunits.value.StorageType;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.vector.AccelerationVector;

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
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Dec 10, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class Acceleration3D
{
    /** The acceleration in 3D (XYZ coded). */
    private final AccelerationVector acceleration;

    /**
     * Construct a new Acceleration3D from vector of strongly typed Cartesian coordinates.
     * @param acceleration the accelerations in 3D (YPR coded)
     * @throws ValueException in case the vector does not have exactly three elements
     */
    public Acceleration3D(final AccelerationVector acceleration) throws ValueException
    {
        super();
        if (acceleration.size() != 3)
        {
            throw new ValueException("Size of an RPY-acceleration vector should be exactly 3. Got: " + acceleration);
        }
        this.acceleration = acceleration;
    }

    /**
     * Construct a new Acceleration3D from three strongly typed Cartesian coordinates.
     * @param x the acceleration in the x-direction
     * @param y the acceleration in the y-direction
     * @param z the acceleration in the z-direction
     * @throws ValueException in case the units are incorrect
     */
    public Acceleration3D(final Acceleration x, final Acceleration y, final Acceleration z) throws ValueException
    {
        super();
        this.acceleration = new AccelerationVector(new Acceleration[]{x, y, z}, StorageType.DENSE);
    }

    /**
     * Construct a new Acceleration3D from three double Cartesian coordinates and a acceleration unit.
     * @param x the acceleration in the x-direction
     * @param y the acceleration in the y-direction
     * @param z the acceleration in the z-direction
     * @param unit the unit of the xyz parameters
     * @throws ValueException in case the units are incorrect
     */
    public Acceleration3D(final double x, final double y, final double z, final AccelerationUnit unit)
        throws ValueException
    {
        super();
        this.acceleration = new AccelerationVector(new double[]{x, y, z}, unit, StorageType.DENSE);
    }

    /**
     * Construct a new Acceleration3D from a strongly typed acceleration and polar coordinates.
     * @param acceleration Acceleration; the acceleration in the direction of the angle along the vector
     * @param theta Angle.Abs; the angle from the z direction
     * @param phi Angle.Abs; the projected angle in the xy-plane from the x direction
     * @throws ValueException in case the vector does not have exactly three elements
     */
    public Acceleration3D(final Acceleration acceleration, final Angle.Abs theta, final Angle.Abs phi)
        throws ValueException
    {
        super();
        double[] xyz = Scalar3D.polarToCartesian(acceleration.getInUnit(), theta.si, phi.si);
        this.acceleration = new AccelerationVector(xyz, acceleration.getUnit(), StorageType.DENSE);
    }

    /**
     * Retrieve the x-component of this Acceleration3D.
     * @return the acceleration in the x-direction.
     */
    public final Acceleration getX()
    {
        try
        {
            return this.acceleration.get(0);
        }
        catch (ValueException exception)
        {
            // should be impossible as we constructed the vector always with three elements
            throw new RuntimeException("getX() gave an exception; apparently vector " + this.acceleration
                + " was not constructed right", exception);
        }
    }

    /**
     * Retrieve the y-component of this Acceleration3D.
     * @return the acceleration in the y-direction.
     */
    public final Acceleration getY()
    {
        try
        {
            return this.acceleration.get(1);
        }
        catch (ValueException exception)
        {
            // should be impossible as we constructed the vector always with three elements
            throw new RuntimeException("getY() gave an exception; apparently vector " + this.acceleration
                + " was not constructed right", exception);
        }
    }

    /**
     * Retrieve the z-component of this Acceleration3D.
     * @return the acceleration in the z-direction.
     */
    public final Acceleration getZ()
    {
        try
        {
            return this.acceleration.get(2);
        }
        catch (ValueException exception)
        {
            // should be impossible as we constructed the vector always with three elements
            throw new RuntimeException("getZ() gave an exception; apparently vector " + this.acceleration
                + " was not constructed right", exception);
        }
    }

    /**
     * Retrieve the theta of this Acceleration3D.
     * @return the angle of direction perpendicular to the xy-plane
     */
    public final Angle.Abs getTheta()
    {
        return Scalar3D.cartesianToTheta(getX().si, getY().si, getZ().si);
    }

    /**
     * Retrieve the phi of this Acceleration3D.
     * @return the projected angle of direction in the xy-plane
     */
    public final Angle.Abs getPhi()
    {
        return Scalar3D.cartesianToPhi(getX().si, getY().si);
    }

    /**
     * Retrieve the norm of this Acceleration3D.
     * @return the combined acceleration in the direction of the angle
     */
    public final Acceleration getAcceleration()
    {
        return new Acceleration(Scalar3D.cartesianToRadius(getX().si, getY().si, getZ().si), AccelerationUnit.SI);
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        return String.format(Locale.US, "Acceleration3D %s (%s, theta %s, phi %s)", this.acceleration,
            getAcceleration(), new Angle.Abs(getTheta().getInUnit(AngleUnit.DEGREE), AngleUnit.DEGREE), new Angle.Abs(
                getPhi().getInUnit(AngleUnit.DEGREE), AngleUnit.DEGREE));
    }

}
