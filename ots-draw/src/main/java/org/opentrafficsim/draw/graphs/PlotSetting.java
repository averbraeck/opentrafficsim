package org.opentrafficsim.draw.graphs;

import java.util.Arrays;
import java.util.List;
import java.util.function.DoubleFunction;
import java.util.stream.Collectors;

import org.djutils.immutablecollections.ImmutableArrayList;
import org.djutils.immutablecollections.ImmutableList;

/**
 * Convenience record to define UI setting options.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param values values
 * @param defaultValueIndex default value index
 * @param <T> value type
 */
public record PlotSetting<T>(ImmutableList<T> values, int defaultValueIndex)
{

    /**
     * Returns the default value.
     * @return default value
     */
    T getDefaultValue()
    {
        return values().get(this.defaultValueIndex);
    }

    /**
     * Create instance from list.
     * @param <T> value type
     * @param values values
     * @param defaultValueIndex default value index
     * @return typed setting instance
     */
    static <T> PlotSetting<T> of(final List<T> values, final int defaultValueIndex)
    {
        return new PlotSetting<>(new ImmutableArrayList<>(values), defaultValueIndex);
    }

    /**
     * Create a typed instance from double values.
     * @param <T> value type
     * @param values values
     * @param factory factory for type from {@code double}
     * @param defaultValueIndex default value index
     * @return typed setting instance
     */
    static <T> PlotSetting<T> of(final double[] values, final DoubleFunction<T> factory, final int defaultValueIndex)
    {
        ImmutableList<T> list = new ImmutableArrayList<>(Arrays.stream(values).mapToObj(factory).collect(Collectors.toList()));
        return new PlotSetting<>(list, defaultValueIndex);
    }

}
