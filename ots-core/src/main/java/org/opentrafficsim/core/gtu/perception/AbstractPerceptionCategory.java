package org.opentrafficsim.core.gtu.perception;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.Type;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.network.NetworkException;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 14, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractPerceptionCategory extends Type<AbstractPerceptionCategory> implements Serializable
{

    /** */
    private static final long serialVersionUID = 20160811L;
    
    /** Connected perception. */
    private final Perception perception;
    
    /**
     * Constructor setting the perception.
     * @param perception perception
     */
    public AbstractPerceptionCategory(final Perception perception) 
    {
        this.perception = perception;
    }
    
    /**
     * Returns the connected perception.
     * @return connected perception
     */
    @SuppressWarnings("checkstyle:designforextension")
    public Perception getPerception()
    {
        return this.perception;
    }
    
    /**
     * Returns the connected GTU.
     * @return connected GTU
     * @throws GTUException if the GTU has not been initialized
     */
    @SuppressWarnings("checkstyle:designforextension")
    public GTU getGtu() throws GTUException
    {
        return this.perception.getGtu();
    }
    
    /**
     * Returns the current time.
     * @return current time
     * @throws GTUException if the GTU has not been initialized
     */
    public final Time getTimestamp() throws GTUException
    {
        if (getGtu() == null)
        {
            throw new GTUException("gtu value has not been initialized for LanePerception when perceiving.");
        }
        return getGtu().getSimulator().getSimulatorTime().getTime();
    }
    
    /**
     * Updates all information in the category.
     * @throws GTUException if the GTU was not initialized
     * @throws NetworkException 
     * @throws ParameterException 
     */
    public abstract void updateAll() throws GTUException, NetworkException, ParameterException;

}
