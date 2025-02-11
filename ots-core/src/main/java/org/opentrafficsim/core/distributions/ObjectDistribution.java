package org.opentrafficsim.core.distributions;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

import org.djutils.exceptions.Throw;

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
public class ObjectDistribution<O> implements Supplier<O>, Serializable
{
    /** */
    private static final long serialVersionUID = 20160301L;

    /** The objects (with their probabilities or frequencies). */
    private final List<FrequencyAndObject<O>> objects = new ArrayList<>();

    /** Sum of all probabilities or frequencies. */
    private double cumulativeTotal;

    /** The uniform random object used to select an object. */
    private final StreamInterface stream;

    /**
     * Construct a new ObjectDistribution.
     * @param objects the objects and their frequencies (or probabilities)
     * @param stream source for randomness
     * @throws NullPointerException when objects is null or stream is null
     * @throws IllegalArgumentException when a frequency (or probability) is negative
     */
    public ObjectDistribution(final List<FrequencyAndObject<O>> objects, final StreamInterface stream)
    {
        this(stream);
        Throw.whenNull(objects, "objects");
        // Store a defensive copy of the object list (the objects are immutable; a list of them is not) and make sure it
        // is a List that supports add, remove, etc.
        this.objects.addAll(objects);
        fixProbabilities();
    }

    /**
     * Construct a new ObjectDistribution with no objects.
     * @param stream source for randomness
     * @throws NullPointerException when objects is null or stream is null
     */
    public ObjectDistribution(final StreamInterface stream)
    {
        Throw.whenNull(stream, "stream");
        this.stream = stream;
    }

    /**
     * Compute the cumulative frequencies of the stored objects.
     */
    private void fixProbabilities()
    {
        if (0 == this.objects.size())
        {
            return;
        }
        this.cumulativeTotal = 0;
        for (FrequencyAndObject<O> object : this.objects)
        {
            double frequency = object.frequency();
            this.cumulativeTotal += frequency;
        }
    }

    @Override
    public O get()
    {
        Throw.when(0 == this.objects.size(), IllegalStateException.class, "Cannot draw from empty collection");
        Throw.when(0 == this.cumulativeTotal, IllegalStateException.class, "Sum of frequencies or probabilities must be > 0");

        double randomValue = this.stream.nextDouble() * this.cumulativeTotal;
        for (FrequencyAndObject<O> fAndO : this.objects)
        {
            double frequency = fAndO.frequency();
            if (frequency >= randomValue)
            {
                return fAndO.object();
            }
            randomValue -= frequency;
        }
        // If we get here we missed the intended object by a few ULP; return the first object that has non-zero frequency
        FrequencyAndObject<O> useThisOne = this.objects.get(0);
        for (FrequencyAndObject<O> fAndO : this.objects)
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
     * Append an object to the internally stored list.
     * @param object the object to add
     * @return this
     * @throws NullPointerException when object is null
     */
    public ObjectDistribution<O> add(final FrequencyAndObject<O> object)
    {
        Throw.whenNull(object, "object");
        return add(this.objects.size(), object);
    }

    /**
     * Insert an object at the specified position in the internally stored list.
     * @param index position to store the object
     * @param object the object to add
     * @return this
     * @throws IndexOutOfBoundsException when index is &lt; 0 or &ge; size
     * @throws NullPointerException when object is null
     */
    public ObjectDistribution<O> add(final int index, final FrequencyAndObject<O> object)
    {
        Throw.whenNull(object, "object");
        this.objects.add(index, object);
        fixProbabilities();
        return this;
    }

    /**
     * Remove the object at the specified position from the internally stored list.
     * @param index the position
     * @return this
     * @throws IndexOutOfBoundsException when index is &lt; 0 or &ge; size
     */
    public ObjectDistribution<O> remove(final int index)
    {
        this.objects.remove(index);
        fixProbabilities();
        return this;
    }

    /**
     * Replace the object at the specified position.
     * @param index the position of the object that must be replaced
     * @param object the new object and the frequency (or probability)
     * @return this
     * @throws IndexOutOfBoundsException when index is &lt; 0 or &ge; size
     * @throws NullPointerException when object is null
     */
    public ObjectDistribution<O> set(final int index, final FrequencyAndObject<O> object)
    {
        Throw.whenNull(object, "object");
        this.objects.set(index, object);
        fixProbabilities();
        return this;
    }

    /**
     * Alter the frequency (or probability) of one of the stored objects.
     * @param index index of the stored object
     * @param frequency new frequency (or probability)
     * @return this
     * @throws IndexOutOfBoundsException when index is &lt; 0 or &ge; size
     * @throws IllegalArgumentException when the frequency (or probability) &lt; 0
     */
    public ObjectDistribution<O> modifyFrequency(final int index, final double frequency)
    {
        Throw.when(index < 0 || index >= this.size(), IndexOutOfBoundsException.class, "Index %s out of range (0..%d)", index,
                this.size() - 1);
        return set(index, new FrequencyAndObject<O>(frequency, this.objects.get(index).object()));
    }

    /**
     * Empty the internally stored list.
     * @return this
     */
    public ObjectDistribution<O> clear()
    {
        this.objects.clear();
        return this;
    }

    /**
     * Retrieve one of the internally stored objects.
     * @param index the index of the FrequencyAndObject to retrieve
     * @return the object stored at position <cite>index</cite>
     * @throws IndexOutOfBoundsException when index &lt; 0 or &ge; size()
     */
    public FrequencyAndObject<O> get(final int index)
    {
        return this.objects.get(index);
    }

    /**
     * Report the number of objects.
     * @return the number of objects
     */
    public int size()
    {
        return this.objects.size();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(this.cumulativeTotal, this.objects, this.stream);
    }

    @Override
    @SuppressWarnings("checkstyle:needbraces")
    public boolean equals(final Object obj)
    {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ObjectDistribution<?> other = (ObjectDistribution<?>) obj;
        return Double.doubleToLongBits(this.cumulativeTotal) == Double.doubleToLongBits(other.cumulativeTotal)
                && Objects.equals(this.objects, other.objects) && Objects.equals(this.stream, other.stream);
    }

    @Override
    public String toString()
    {
        StringBuilder result = new StringBuilder();
        result.append("Distribution [");
        String separator = "";
        for (FrequencyAndObject<O> fAndO : this.objects)
        {
            result.append(separator + fAndO.frequency() + "->" + fAndO.object());
            separator = ", ";
        }
        result.append(']');
        return result.toString();
    }

}
