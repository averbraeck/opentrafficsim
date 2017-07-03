package org.opentrafficsim.base.parameters;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 30 jun. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> value type to for constraint
 */
public interface Constraint<T>
{

    /**
     * Checks whether the value fails to comply with constraints.
     * @param value Value to check.
     * @return Whether the value fails to comply with constraints.
     */
    boolean fails(T value);
    
    /**
     * Returns a message for value failure, pointing to a parameter using '%s'.
     * @return Message for value failure, pointing to a parameter using '%s'.
     */
    String failMessage();
    
}
