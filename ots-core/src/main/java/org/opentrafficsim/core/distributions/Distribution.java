package org.opentrafficsim.core.distributions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.djutils.exceptions.Throw;

import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Generic implementation of a set of objects that have a draw method with corresponding probabilities / frequencies.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @param <O> Type of the object returned by the draw method
 */
public class Distribution<O> implements Generator<O>, Serializable
{
    /** */
    private static final long serialVersionUID = 20160301L;

    /** The generators (with their probabilities or frequencies). */
    private final List<FrequencyAndObject<O>> generators = new ArrayList<>();

    /** Sum of all probabilities or frequencies. */
    private double cumulativeTotal;

    /** The uniform random generator used to select a Generator. */
    private final DistUniform random;

    /**
     * Construct a new Distribution.
     * @param generators the generators and their frequencies (or probabilities)
     * @param stream source for randomness
     * @throws NullPointerException when generators is null or stream is null
     * @throws IllegalArgumentException when a frequency (or probability) is negative
     */
    public Distribution(final List<FrequencyAndObject<O>> generators, final StreamInterface stream)
    {
        this(stream);
        Throw.whenNull(generators, "generators");
        // Store a defensive copy of the generator list (the generators are immutable; a list of them is not) and make sure it
        // is a List that supports add, remove, etc.
        this.generators.addAll(generators);
        fixProbabilities();
    }

    /**
     * Construct a new Distribution with no generators.
     * @param stream source for randomness
     * @throws NullPointerException when generators is null or stream is null
     */
    public Distribution(final StreamInterface stream)
    {
        Throw.whenNull(stream, "stream");
        this.random = new DistUniform(stream, 0, 1);
    }

    /**
     * Compute the cumulative frequencies of the storedGenerators.
     * @throws IllegalArgumentException on negative frequency
     */
    private void fixProbabilities()
    {
        if (0 == this.generators.size())
        {
            return;
        }
        this.cumulativeTotal = 0;
        for (FrequencyAndObject<O> generator : this.generators)
        {
            double frequency = generator.frequency();
            this.cumulativeTotal += frequency;
        }
    }

    @Override
    public final O draw()
    {
        Throw.when(0 == this.generators.size(), IllegalStateException.class, "Cannot draw from empty collection");
        Throw.when(0 == this.cumulativeTotal, IllegalStateException.class, "Sum of frequencies or probabilities must be > 0");

        double randomValue = this.random.draw() * this.cumulativeTotal;
        for (FrequencyAndObject<O> fAndO : this.generators)
        {
            double frequency = fAndO.frequency();
            if (frequency >= randomValue)
            {
                return fAndO.object();
            }
            randomValue -= frequency;
        }
        // If we get here we missed the intended object by a few ULP; return the first object that has non-zero frequency
        FrequencyAndObject<O> useThisOne = this.generators.get(0);
        for (FrequencyAndObject<O> fAndO : this.generators)
        {
            if (fAndO.frequency() > 0)
            {
                useThisOne = fAndO;
                break;
            }
        }
        return useThisOne.object();
    }

    /**
     * Append a generator to the internally stored list.
     * @param generator the generator to add
     * @return this
     * @throws NullPointerException when generator is null
     */
    public final Distribution<O> add(final FrequencyAndObject<O> generator)
    {
        Throw.whenNull(generator, "generator");
        return add(this.generators.size(), generator);
    }

    /**
     * Insert a generator at the specified position in the internally stored list.
     * @param index position to store the generator
     * @param generator the generator to add
     * @return this
     * @throws IndexOutOfBoundsException when index is &lt; 0 or &gt;= size
     * @throws NullPointerException when generator is null
     */
    public final Distribution<O> add(final int index, final FrequencyAndObject<O> generator)
    {
        Throw.whenNull(generator, "generator");
        this.generators.add(index, generator);
        fixProbabilities();
        return this;
    }

    /**
     * Remove the generator at the specified position from the internally stored list.
     * @param index the position
     * @return this
     * @throws IndexOutOfBoundsException when index is &lt; 0 or &gt;= size
     */
    public final Distribution<O> remove(final int index)
    {
        this.generators.remove(index);
        fixProbabilities();
        return this;
    }

    /**
     * Replace the generator at the specified position.
     * @param index the position of the generator that must be replaced
     * @param generator the new generator and the frequency (or probability)
     * @return this
     * @throws IndexOutOfBoundsException when index is &lt; 0 or &gt;= size
     * @throws NullPointerException when generator is null
     */
    public final Distribution<O> set(final int index, final FrequencyAndObject<O> generator)
    {
        Throw.whenNull(generator, "generator");
        this.generators.set(index, generator);
        fixProbabilities();
        return this;
    }

    /**
     * Alter the frequency (or probability) of one of the stored generators.
     * @param index index of the stored generator
     * @param frequency new frequency (or probability)
     * @return this
     * @throws IndexOutOfBoundsException when index is &lt; 0 or &gt;= size
     * @throws IllegalArgumentException when the frequency (or probability) &lt; 0
     */
    public final Distribution<O> modifyFrequency(final int index, final double frequency)
    {
        Throw.when(index < 0 || index >= this.size(), IndexOutOfBoundsException.class, "Index %s out of range (0..%d)", index,
                this.size() - 1);
        return set(index, new FrequencyAndObject<O>(frequency, this.generators.get(index).object()));
    }

    /**
     * Empty the internally stored list.
     * @return this
     */
    public final Distribution<O> clear()
    {
        this.generators.clear();
        return this;
    }

    /**
     * Retrieve one of the internally stored generators.
     * @param index the index of the FrequencyAndObject to retrieve
     * @return the generator stored at position <cite>index</cite>
     * @throws IndexOutOfBoundsException when index &lt; 0 or &gt;= size()
     */
    public final FrequencyAndObject<O> get(final int index)
    {
        return this.generators.get(index);
    }

    /**
     * Report the number of generators.
     * @return the number of generators
     */
    public final int size()
    {
        return this.generators.size();
    }

    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = 1;
        long temp;
        temp = Double.doubleToLongBits(this.cumulativeTotal);
        result = prime * result + (int) (temp ^ (temp >>> 32));
        result = prime * result + ((this.generators == null) ? 0 : this.generators.hashCode());
        result = prime * result + ((this.random == null) ? 0 : this.random.hashCode());
        return result;
    }

    @Override
    @SuppressWarnings("checkstyle:needbraces")
    public final boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Distribution<?> other = (Distribution<?>) obj;
        if (Double.doubleToLongBits(this.cumulativeTotal) != Double.doubleToLongBits(other.cumulativeTotal))
            return false;
        if (this.generators == null)
        {
            if (other.generators != null)
                return false;
        }
        else if (!this.generators.equals(other.generators))
            return false;
        if (this.random == null)
        {
            if (other.random != null)
                return false;
        }
        else if (!this.random.equals(other.random))
            return false;
        return true;
    }

    @Override
    public final String toString()
    {
        StringBuilder result = new StringBuilder();
        result.append("Distribution [");
        String separator = "";
        for (FrequencyAndObject<O> fAndO : this.generators)
        {
            result.append(separator + fAndO.frequency() + "->" + fAndO.object());
            separator = ", ";
        }
        result.append(']');
        return result.toString();
    }

}
