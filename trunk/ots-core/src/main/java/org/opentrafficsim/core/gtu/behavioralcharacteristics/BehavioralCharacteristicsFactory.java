package org.opentrafficsim.core.gtu.behavioralcharacteristics;

import org.opentrafficsim.core.gtu.GTUType;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 18 nov. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public interface BehavioralCharacteristicsFactory
{

    /** 
     * Creates a set of behavioral characteristics for the provided GTU type.
     * @param defaultCharacteristics default behavioral characteristics
     * @param gtuType GTU type
     */
    void setValues(BehavioralCharacteristics defaultCharacteristics, GTUType gtuType);
    
}
