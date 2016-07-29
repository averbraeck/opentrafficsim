package org.opentrafficsim.core.gtu.perception;

import org.opentrafficsim.core.Type;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.network.NetworkException;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 14, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class AbstractPerceptionCategory extends Type<AbstractPerceptionCategory>
{

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
    public final Perception getPerception()
    {
        return this.perception;
    }
    
    /**
     * Returns the connected GTU.
     * @return connected GTU
     */
    @SuppressWarnings("checkstyle:designforextension")
    public GTU getGtu()
    {
        return this.perception.getGtu();
    }
    
    /**
     * Updates all information in the category.
     * @throws GTUException 
     * @throws NetworkException 
     * @throws ParameterException 
     */
    public abstract void updateAll() throws GTUException, NetworkException, ParameterException;

}
