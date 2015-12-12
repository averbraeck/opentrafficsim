package org.opentrafficsim.core.math;

import java.util.Locale;

import org.djunits.unit.AngleUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.StorageType;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Angle;
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
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Dec 10, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class Speed3D
{
    /** The speed in 3D (XYZ coded). */
    private final SpeedVector speed;

    /**
     * Construct a new Speed3D from vector of strongly typed Cartesian coordinates.
     * @param speed the speeds in 3D (YPR coded)
     * @throws ValueException in case the vector does not have exactly three elements
     */
    public Speed3D(final SpeedVector speed) throws ValueException
    {
        super();
        if (speed.size() != 3)
        {
            throw new ValueException("Size of an RPY-speed vector should be exactly 3. Got: " + speed);
        }
        this.speed = speed;
    }

    /**
     * Construct a new Speed3D from three strongly typed Cartesian coordinates.
     * @param x the speed in the x-direction
     * @param y the speed in the y-direction
     * @param z the speed in the z-direction
     * @throws ValueException in case the units are incorrect
     */
    public Speed3D(final Speed x, final Speed y, final Speed z) throws ValueException
    {
        super();
        this.speed = new SpeedVector(new Speed[] { x, y, z }, StorageType.DENSE);
    }

    /**
     * Construct a new Speed3D from three double Cartesian coordinates and a speed unit.
     * @param x the speed in the x-direction
     * @param y the speed in the y-direction
     * @param z the speed in the z-direction
     * @param unit the unit of the xyz parameters
     * @throws ValueException in case the units are incorrect
     */
    public Speed3D(final double x, final double y, final double z, final SpeedUnit unit) throws ValueException
    {
        super();
        this.speed = new SpeedVector(new double[] { x, y, z }, unit, StorageType.DENSE);
    }

    /**
     * Construct a new Speed3D from a strongly typed speed and polar coordinates.
     * @param speed Speed; the speed in the direction of the angle along the vector
     * @param theta Angle.Abs; the angle from the z direction
     * @param phi Angle.Abs; the projected angle in the xy-plane from the x direction
     * @throws ValueException in case the vector does not have exactly three elements
     */
    public Speed3D(final Speed speed, final Angle.Abs theta, final Angle.Abs phi) throws ValueException
    {
        super();
        double[] xyz = Scalar3D.polarToCartesian(speed.getInUnit(), theta.si, phi.si);
        this.speed = new SpeedVector(xyz, speed.getUnit(), StorageType.DENSE);
    }

    /**
     * Retrieve the x-component of this Speed3D.
     * @return the speed in the x-direction.
     */
    public final Speed getX()
    {
        try
        {
            return this.speed.get(0);
        }
        catch (ValueException exception)
        {
            // should be impossible as we constructed the vector always with three elements
            throw new RuntimeException("getX() gave an exception; apparently vector " + this.speed
                    + " was not constructed right", exception);
        }
    }

    /**
     * Retrieve the y-component of this Speed3D.
     * @return the speed in the y-direction.
     */
    public final Speed getY()
    {
        try
        {
            return this.speed.get(1);
        }
        catch (ValueException exception)
        {
            // should be impossible as we constructed the vector always with three elements
            throw new RuntimeException("getY() gave an exception; apparently vector " + this.speed
                    + " was not constructed right", exception);
        }
    }

    /**
     * Retrieve the z-component of this Speed3D.
     * @return the speed in the z-direction.
     */
    public final Speed getZ()
    {
        try
        {
            return this.speed.get(2);
        }
        catch (ValueException exception)
        {
            // should be impossible as we constructed the vector always with three elements
            throw new RuntimeException("getZ() gave an exception; apparently vector " + this.speed
                    + " was not constructed right", exception);
        }
    }

    /**
     * Retrieve the theta of this Speed3D.
     * @return the angle of direction perpendicular to the xy-plane
     */
    public final Angle.Abs getTheta()
    {
        return Scalar3D.cartesianToTheta(getX().si, getY().si, getZ().si);
    }

    /**
     * Retrieve the phi of this Speed3D.
     * @return the projected angle of direction in the xy-plane
     */
    public final Angle.Abs getPhi()
    {
        return Scalar3D.cartesianToPhi(getX().si, getY().si);
    }

    /**
     * Retrieve the norm of this Speed3D.
     * @return the combined speed in the direction of the angle
     */
    public final Speed getSpeed()
    {
        return new Speed(Scalar3D.cartesianToRadius(getX().si, getY().si, getZ().si), SpeedUnit.SI);
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        return String.format(Locale.US, "Speed3D %s (%s, theta %s, phi %s)", this.speed, getSpeed(), new Angle.Abs(getTheta()
                .getInUnit(AngleUnit.DEGREE), AngleUnit.DEGREE), new Angle.Abs(getPhi().getInUnit(AngleUnit.DEGREE),
                AngleUnit.DEGREE));
    }

}
