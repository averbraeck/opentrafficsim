package org.opentrafficsim.core.distributions;

import org.opentrafficsim.base.parameters.ParameterException;

/**
 * Interface for classes that have a draw method with no arguments.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 1, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <O> type of the object returned by the draw method
 */
public interface Generator<O>
{
    /**
     * Generate the next object.
     * @return O; an object randomly selected from the stored collection
     * @throws ProbabilityException if the stored collection is empty
     * @throws ParameterException in case of a parameter exception
     */
    O draw() throws ProbabilityException, ParameterException;
}
