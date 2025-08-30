package org.opentrafficsim.road.gtu.lane.tactical;

import java.util.function.Supplier;

/**
 * Supplier of a model component. Additional to {@code ModelComponentFactory} this defines a {@code get()} method to obtain the
 * component without input. Factories that require input to return a component should extend {@code ModelComponentFactory} and
 * define their own method to obtain the component.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> component type
 */
public interface ModelComponentSupplier<T> extends ModelComponentFactory, Supplier<T>
{
}
