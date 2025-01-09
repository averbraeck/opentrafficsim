package org.opentrafficsim.core.distributions;

/**
 * Interface for classes that have a draw method with no arguments.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @param <O> type of the object returned by the draw method
 */
public interface Generator<O>
{
    /**
     * Generate the next object.
     * @return an object randomly selected from the stored collection
     */
    O draw();
}
