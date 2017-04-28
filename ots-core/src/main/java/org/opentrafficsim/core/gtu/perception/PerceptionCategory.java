package org.opentrafficsim.core.gtu.perception;

import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Super interface for all perception categories.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 13 feb. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public interface PerceptionCategory
{
    
    /**
     * Update all information in the perception category.
     * @throws GTUException if the GTU was not initialized
     * @throws NetworkException when lanes are not properly linked
     * @throws ParameterException when a necessary parameter to carry our perception is not defined (e.g., LOOKAHEAD)
     */
    void updateAll() throws GTUException, NetworkException, ParameterException;

}
