package org.opentrafficsim.core.gtu.perception;

import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.network.NetworkException;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 29, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class EgoPerception extends AbstractPerceptionCategory
{

    /** Speed. */
    private TimeStampedObject<Speed> speed;
    
    /**
     * @param perception perception
     */
    public EgoPerception(final Perception perception)
    {
        super(perception);
    }

    /**
     * Update speed.
     */
    public void updateSpeed()
    {
        //
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
     * Return the time stamped speed.
     * @return time stamped speed
     */
    public final TimeStampedObject<Speed> getTimeStampedSpeed()
    {
        return this.speed;
    }
    
    /** {@inheritDoc} */
    @Override
    public final void updateAll() throws GTUException, NetworkException, ParameterException
    {
        updateSpeed();
    }

}
