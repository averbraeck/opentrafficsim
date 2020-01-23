package org.opentrafficsim.core.gtu.perception;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUException;

/**
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 29, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <G> GTU type
 * @param <P> perception type
 */

public class DirectEgoPerception<G extends GTU, P extends Perception<G>> extends AbstractPerceptionCategory<G, P>
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
    public final void updateSpeed() throws GTUException
    {
        this.speed = new TimeStampedObject<>(getGtu().getSpeed(), getTimestamp());
    }

    /** {@inheritDoc} */
    @Override
    public final void updateAcceleration() throws GTUException
    {
        this.acceleration = new TimeStampedObject<>(getGtu().getAcceleration(), getTimestamp());
    }

    /** {@inheritDoc} */
    @Override
    public final void updateLength() throws GTUException
    {
        this.length = new TimeStampedObject<>(getGtu().getLength(), getTimestamp());
    }

    /** {@inheritDoc} */
    @Override
    public final void updateWidth() throws GTUException
    {
        this.width = new TimeStampedObject<>(getGtu().getWidth(), getTimestamp());
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

}
