package org.opentrafficsim.core.gtu;

import java.util.function.Supplier;

/**
 * Defines a component as stateless. It adds no methods relative to {@code Supplier} but functions as a tagging interface for
 * components that are stateless. It also forces subclasses to provide an instance of themselves.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> component type
 */
@FunctionalInterface
public interface Stateless<T> extends Supplier<T>
{

    /**
     * Returns the single stateless instance.
     * @return single stateless instance
     */
    @Override
    T get();

}
