package org.opentrafficsim.core.network;

import org.opentrafficsim.core.gtu.GTUType;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Aug 21, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @param <TYPE> the return type of compatibility, e.g., Boolean, Double, etc.
 */
public interface Compatibility<TYPE>
{
    /** 
     * @param laneType the lane type.
     * @param gtuType the GTU type.
     * @return the 'level' of compatibility between a type of lane and a type of GTU. 
     */
    TYPE isCompatible(LaneType<?> laneType, GTUType<?> gtuType);
}
