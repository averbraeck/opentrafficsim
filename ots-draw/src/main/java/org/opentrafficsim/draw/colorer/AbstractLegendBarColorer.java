package org.opentrafficsim.draw.colorer;

import java.awt.Color;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

import org.opentrafficsim.draw.BoundsPaintScale;

/**
 * Abstract implementation for colorers that support a colorbar, additional to a legend.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> type of object to color
 * @param <V> value type obtained from GTU and used to determine the color
 */
public abstract class AbstractLegendBarColorer<T, V> extends AbstractLegendColorer<T, V> implements ColorbarColorer<T>
{

    /** Bounds paint scale to draw the colorbar. */
    private final BoundsPaintScale boundsPaintScale;

    /**
     * Constructor.
     * @param valueFunction value function
     * @param colorFunction color function
     * @param legend legend to draw the legend
     * @param boundsPaintScale bounds paint scale to draw the colorbar
     */
    public AbstractLegendBarColorer(final Function<? super T, Optional<V>> valueFunction,
            final Function<V, Color> colorFunction, final List<LegendEntry> legend, final BoundsPaintScale boundsPaintScale)
    {
        super(valueFunction, colorFunction, legend);
        this.boundsPaintScale = boundsPaintScale;
    }

    @Override
    public BoundsPaintScale getBoundsPaintScale()
    {
        return this.boundsPaintScale;
    }

}
