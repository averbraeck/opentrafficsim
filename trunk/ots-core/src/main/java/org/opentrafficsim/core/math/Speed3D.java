package org.opentrafficsim.core.math;

import org.djunits.unit.AngleUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.StorageType;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.Angle;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.vector.SpeedVector;

/**
 * A 3D speed vector, decomposed in X, Y, and Z-speed.
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
    /** the speed in 3D (XYZ coded). */
    private final SpeedVector speed;

    /**
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
     * @param x the speed in the x-direction
     * @param y the speed in the y-direction
     * @param z the speed in the z-direction
     * @throws ValueException in case the units are incorrect
     */
    public Speed3D(final Speed x, final Speed y, final Speed z) throws ValueException
    {
        super();
        this.speed = new SpeedVector(new Speed[]{x, y, z}, StorageType.DENSE);
    }

    /**
     * @param x the speed in the x-direction
     * @param y the speed in the y-direction
     * @param z the speed in the z-direction
     * @param unit the unit of the xyz parameters
     * @throws ValueException in case the units are incorrect
     */
    public Speed3D(final double x, final double y, final double z, final SpeedUnit unit) throws ValueException
    {
        super();
        this.speed = new SpeedVector(new double[]{x, y, z}, unit, StorageType.DENSE);
    }

    /**
     * Construct the 3D speed in polar coordinates.
     * @param speed the speeds in the direction of the angle long the vector
     * @param theta the angle perpendicular to the xy-plane
     * @param phi the projected angle in the xy-plane
     * @throws ValueException in case the vector does not have exactly three elements
     */
    public Speed3D(final Speed speed, final Angle.Abs theta, final Angle.Abs phi) throws ValueException
    {
        super();
        double r = speed.getInUnit();
        double x = r * Math.sin(theta.si) * Math.cos(phi.si);
        double y = r * Math.sin(theta.si) * Math.sin(phi.si);
        double z = r * Math.cos(theta.si);
        this.speed = new SpeedVector(new double[]{x, y, z}, speed.getUnit(), StorageType.DENSE);
    }

    /**
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
     * @return the angle of direction perpendicular to the xy-plane
     */
    public final Angle.Abs getTheta()
    {
        double x = getX().si;
        double y = getY().si;
        double z = getZ().si;
        double r = Math.sqrt(x * x + y * y + z * z);
        return new Angle.Abs(Math.acos(z / r), AngleUnit.SI);
    }

    /**
     * @return the projected angle of direction in the xy-plane
     */
    public final Angle.Abs getPhi()
    {
        double x = getX().si;
        double y = getY().si;
        return new Angle.Abs(Math.atan2(y, x), AngleUnit.SI);
    }

    /**
     * @return the combined speed in the direction of the angle
     */
    public final Speed getSpeed()
    {
        double x = getX().si;
        double y = getY().si;
        double z = getZ().si;
        return new Speed(Math.sqrt(x * x + y * y + z * z), SpeedUnit.SI);
    }
}
