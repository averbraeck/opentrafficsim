package org.opentrafficsim.core.gtu.perception;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.network.NetworkException;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 29, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class EgoPerception extends AbstractPerceptionCategory
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** Speed. */
    private TimeStampedObject<Speed> speed;
    
    /** Length. */
    private TimeStampedObject<Length> length;
    
    /** Width. */
    private TimeStampedObject<Length> width;
    
    /**
     * @param perception perception
     */
    public EgoPerception(final Perception perception)
    {
        super(perception);
    }

    /**
     * Update speed.
     * @throws GTUException if the GTU has not been initialized
     */
    public final void updateSpeed() throws GTUException
    {
        this.speed = new TimeStampedObject<>(getGtu().getSpeed(), getTimestamp());
    }
    
    /**
     * Update length.
     * @throws GTUException if the GTU has not been initialized
     */
    public final void updateLength() throws GTUException
    {
        this.length = new TimeStampedObject<>(getGtu().getLength(), getTimestamp());
    }
    
    /**
     * Update width.
     * @throws GTUException if the GTU has not been initialized
     */
    public final void updateWidth() throws GTUException
    {
        this.width = new TimeStampedObject<>(getGtu().getWidth(), getTimestamp());
    }
    
    /**
     * Returns the speed.
     * @return speed
     */
    public final Speed getSpeed()
    {
        return this.speed.getObject();
    }
    
    /**
     * Returns the length.
     * @return length
     */
    public final Length getLength()
    {
        return this.length.getObject();
    }
    
    /**
     * Returns the width.
     * @return width
     */
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
    public final void updateAll() throws GTUException, NetworkException, ParameterException
    {
        updateSpeed();
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "EgoPerception [speed=" + this.speed + "]";
    }

}
