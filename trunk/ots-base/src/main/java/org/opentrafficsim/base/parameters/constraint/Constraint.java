package org.opentrafficsim.base.parameters.constraint;

/**
 * Interface for a parameter constraint.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 30 jun. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> value type
 */
public interface Constraint<T>
{

    /**
     * Checks whether the value complies with constraints.
     * @param value T; Value to check.
     * @return Whether the value complies with constraints.
     */
    boolean accept(T value);

    /**
     * Returns a message for value failure, pointing to a parameter using '%s'.
     * @return Message for value failure, pointing to a parameter using '%s'.
     */
    String failMessage();

}
