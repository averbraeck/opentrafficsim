package org.opentrafficsim.draw.colorer;

import java.awt.Color;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;

/**
 * Colorer based on a function to obtain a value from the colored object, and a function to translate the value to a color.
 * Additionally the colorer support a legend.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> type of object to color
 * @param <V> value type obtained from GTU and used to determine the color
 */
public abstract class AbstractLegendColorer<T, V> extends AbstractColorer<T, V> implements LegendColorer<T>
{

    /** Legend. */
    private final List<LegendEntry> legend;

    /**
     * Constructor.
     * @param valueFunction value function
     * @param colorFunction color function
     * @param legend legend
     */
    public AbstractLegendColorer(final Function<? super T, Optional<V>> valueFunction, final Function<V, Color> colorFunction,
            final List<LegendEntry> legend)
    {
        super(valueFunction, colorFunction);
        this.legend = Collections.unmodifiableList(legend);
    }

    @Override
    public List<LegendEntry> getLegend()
    {
        return this.legend;
    }

}
