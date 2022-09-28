package org.opentrafficsim.base;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Function;

import org.djutils.exceptions.Throw;

/**
 * Utility to calculate a weighted mean and/or sum. This can be used as part of a process or loop with information being
 * accumulated in the object. This is even a memory friendly method as this class only stores 2 double values internally.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <V> value type
 * @param <W> weight type
 */
public class WeightedMeanAndSum<V extends Number, W extends Number>
{
    /** Cumulative numerator of weighted mean fraction, i.e. weighted sum. */
    private double numerator;

    /** Cumulative denominator of weighted mean fraction, i.e. sum of weights. */
    private double denominator;

    /**
     * Constructor.
     */
    public WeightedMeanAndSum()
    {
        //
    }

    /**
     * Returns the weighted mean of available data.
     * @return double; weighted mean of available data
     */
    public final double getMean()
    {
        return this.numerator / this.denominator;
    }

    /**
     * Returns the weighted sum of available data.
     * @return double; weighted sum of available data
     */
    public final double getSum()
    {
        return this.numerator;
    }

    /**
     * Returns the sum of the weights.
     * @return double; sum of the weights
     */
    public final double getWeightSum()
    {
        return this.denominator;
    }

    /**
     * Adds a value with weight.
     * @param value V; value
     * @param weight W; weight
     * @return this WeightedMeanAndSum for method chaining
     */
    public final WeightedMeanAndSum<V, W> add(final V value, final W weight)
    {
        this.numerator += weight.doubleValue() * value.doubleValue();
        this.denominator += weight.doubleValue();
        return this;
    }

    /**
     * Adds a weighted value for each element. Note that iteration order is pivotal in correct operations. This method should
     * not be used with instances of {@code LinkedHashMap} or {@code LinkedHashSet}.
     * @param values Iterable&lt;V&gt;; values
     * @param weights Iterable&lt;W&gt;; weights
     * @return this WeightedMeanAndSum&lt;V, W&gt; for method chaining
     * @throws IllegalArgumentException if the number of values and weights are unequal
     */
    public final WeightedMeanAndSum<V, W> add(final Iterable<V> values, final Iterable<W> weights)
    {
        Iterator<V> itV = values.iterator();
        Iterator<W> itW = weights.iterator();
        while (itV.hasNext())
        {
            Throw.when(!itW.hasNext(), IllegalArgumentException.class, "Unequal number of values and weights.");
            add(itV.next(), itW.next());
        }
        Throw.when(itW.hasNext(), IllegalArgumentException.class, "Unequal number of values and weights.");
        return this;
    }

    /**
     * Adds a weighted value for each element.
     * @param values V[]; values
     * @param weights W[]; weights
     * @return this WeightedMeanAndSum&lt;V, W&gt; for method chaining
     */
    public final WeightedMeanAndSum<V, W> add(final V[] values, final W[] weights)
    {
        Throw.when(values.length != weights.length, IllegalArgumentException.class, "Unequal number of values and weights.");
        for (int i = 0; i < values.length; i++)
        {
            add(values[i], weights[i]);
        }
        return this;
    }

    /**
     * Adds each weighted value from a map.
     * @param map Map&lt;V, W&gt;; map
     * @return this WeightedMeanAndSum&lt;V, W&gt; for method chaining
     */
    public final WeightedMeanAndSum<V, W> add(final Map<V, W> map)
    {
        for (Entry<V, W> entry : map.entrySet())
        {
            add(entry.getKey(), entry.getValue());
        }
        return this;
    }

    /**
     * Adds each value with a weight given by a function.
     * @param collection Collection&lt;V&gt;; values
     * @param weights Function&lt;V, W&gt;; weights
     * @return this WeightedMeanAndSum&lt;V, W&gt; for method chaining
     */
    public final WeightedMeanAndSum<V, W> add(final Collection<V> collection, final Function<V, W> weights)
    {
        for (V v : collection)
        {
            add(v, weights.apply(v));
        }
        return this;
    }

    /**
     * Adds each value with a weight given by a function.
     * @param collection Collection&lt;S&gt;; collection of source objects
     * @param values Function&lt;S, V&gt;; values
     * @param weights Function&lt;S, W&gt;; weights
     * @param <S> type of source object
     * @return this WeightedMeanAndSum&lt;V, W&gt; for method chaining
     */
    public final <S> WeightedMeanAndSum<V, W> add(final Collection<S> collection, final Function<S, V> values,
            final Function<S, W> weights)
    {
        for (S s : collection)
        {
            add(values.apply(s), weights.apply(s));
        }
        return this;
    }
}
