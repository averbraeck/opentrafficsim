package org.opentrafficsim.draw.colorer;

import java.awt.Color;
import java.util.function.Function;

/**
 * Colorer based on a function to obtain a value from the colored object, and a function to translate the value to a color.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> type of object to color
 * @param <V> value type obtained from object and used to determine the color
 */
public abstract class AbstractColorer<T, V> implements Colorer<T>
{

    /** Value function. */
    private Function<? super T, V> valueFunction;

    /** Color function. */
    private Function<V, Color> colorFunction;

    /**
     * Constructor.
     * @param valueFunction value function
     * @param colorFunction color function
     */
    public AbstractColorer(final Function<? super T, V> valueFunction, final Function<V, Color> colorFunction)
    {
        this.valueFunction = valueFunction;
        this.colorFunction = colorFunction;
    }

    @Override
    public Color getColor(final T object)
    {
        return this.colorFunction.apply(this.valueFunction.apply(object));
    }

}
