package org.opentrafficsim.core.gtu.perception;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.core.gtu.Gtu;
import org.opentrafficsim.core.gtu.GtuException;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <G> GTU type
 * @param <P> perception type
 */

public class DirectEgoPerception<G extends Gtu, P extends Perception<G>> extends AbstractPerceptionCategory<G, P>
        implements EgoPerception<G, P>
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** Speed. */
    private TimeStampedObject<Speed> speed;

    /** Speed. */
    private TimeStampedObject<Acceleration> acceleration;

    /** Length. */
    private TimeStampedObject<Length> length;

    /** Width. */
    private TimeStampedObject<Length> width;

    /**
     * @param perception P; perception
     */
    public DirectEgoPerception(final P perception)
    {
        super(perception);
    }

    /** {@inheritDoc} */
    @Override
    public final void updateSpeed() throws GtuException
    {
        synchronized (getGtu())
        {
            this.speed = new TimeStampedObject<Speed>(getGtu().getSpeed(), getTimestamp());
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void updateAcceleration() throws GtuException
    {
        synchronized (getGtu())
        {
            this.acceleration = new TimeStampedObject<Acceleration>(getGtu().getAcceleration(), getTimestamp());
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void updateLength() throws GtuException
    {
        synchronized (getGtu())
        {
            this.length = new TimeStampedObject<Length>(getGtu().getLength(), getTimestamp());
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void updateWidth() throws GtuException
    {
        synchronized (getGtu())
        {
            this.width = new TimeStampedObject<Length>(getGtu().getWidth(), getTimestamp());
        }
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration getAcceleration()
    {
        return this.acceleration.getObject();
    }

    /** {@inheritDoc} */
    @Override
    public final Speed getSpeed()
    {
        return this.speed.getObject();
    }

    /** {@inheritDoc} */
    @Override
    public final Length getLength()
    {
        return this.length.getObject();
    }

    /** {@inheritDoc} */
    @Override
    public final Length getWidth()
    {
        return this.width.getObject();
    }

    /**
     * Return the time stamped speed.
     * @return time stamped speed
     */
    public final TimeStampedObject<Speed> getTimeStampedSpeed()
    {
        return this.speed;
    }

    /**
     * Return the time stamped acceleration.
     * @return time stamped acceleration
     */
    public final TimeStampedObject<Acceleration> getTimeStampedAcceleration()
    {
        return this.acceleration;
    }

    /**
     * Return the time stamped length.
     * @return time stamped length
     */
    public final TimeStampedObject<Length> getTimeStampedLength()
    {
        return this.length;
    }

    /**
     * Return the time stamped width.
     * @return time stamped width
     */
    public final TimeStampedObject<Length> getTimeStampedWidth()
    {
        return this.width;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DirectEgoPerception [speed=" + this.speed + ", acceleration=" + this.acceleration + ", length=" + this.length
                + ", width=" + this.width + "]";
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.acceleration == null) ? 0 : this.acceleration.hashCode());
        result = prime * result + ((this.length == null) ? 0 : this.length.hashCode());
        result = prime * result + ((this.speed == null) ? 0 : this.speed.hashCode());
        result = prime * result + ((this.width == null) ? 0 : this.width.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        DirectEgoPerception other = (DirectEgoPerception) obj;
        if (this.acceleration == null)
        {
            if (other.acceleration != null)
                return false;
        }
        else if (!this.acceleration.equals(other.acceleration))
            return false;
        if (this.length == null)
        {
            if (other.length != null)
                return false;
        }
        else if (!this.length.equals(other.length))
            return false;
        if (this.speed == null)
        {
            if (other.speed != null)
                return false;
        }
        else if (!this.speed.equals(other.speed))
            return false;
        if (this.width == null)
        {
            if (other.width != null)
                return false;
        }
        else if (!this.width.equals(other.width))
            return false;
        return true;
    }

}
