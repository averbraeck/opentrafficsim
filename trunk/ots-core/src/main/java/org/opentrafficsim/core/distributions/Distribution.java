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
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 1, 2016 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
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
     * @param generators List&lt;FrequencyAndObject&lt;O&gt;&gt;; the generators and their frequencies (or probabilities)
     * @param stream StreamInterface; source for randomness
     * @throws ProbabilityException when a frequency (or probability) is negative, or when generators is null or stream is null
     */
    public Distribution(final List<FrequencyAndObject<O>> generators, final StreamInterface stream) throws ProbabilityException
    {
        this(stream);
        Throw.when(null == generators, ProbabilityException.class, "generators may not be null");
        // Store a defensive copy of the generator list (the generators are immutable; a list of them is not) and make sure it
        // is a List that supports add, remove, etc.
        this.generators.addAll(generators);
        fixProbabilities();
    }

    /**
     * Construct a new Distribution with no generators.
     * @param stream StreamInterface; source for randomness
     * @throws ProbabilityException when a frequency (or probability) is negative, or when generators is null or stream is null
     */
    public Distribution(final StreamInterface stream) throws ProbabilityException
    {
        Throw.when(null == stream, ProbabilityException.class, "random stream may not be null");
        this.random = new DistUniform(stream, 0, 1);
    }

    /**
     * Compute the cumulative frequencies of the storedGenerators.
     * @throws ProbabilityException on negative frequency
     */
    private void fixProbabilities() throws ProbabilityException
    {
        if (0 == this.generators.size())
        {
            return;
        }
        this.cumulativeTotal = 0;
        for (FrequencyAndObject<O> generator : this.generators)
        {
            double frequency = generator.getFrequency();
            Throw.when(frequency < 0, ProbabilityException.class,
                    "Negative frequency or probability is not allowed (got " + frequency + ")");
            this.cumulativeTotal += frequency;
        }
    }

    /** {@inheritDoc} */
    @Override
    public final O draw() throws ProbabilityException
    {
        Throw.when(0 == this.generators.size(), ProbabilityException.class, "Cannot draw from empty collection");
        Throw.when(0 == this.cumulativeTotal, ProbabilityException.class, "Sum of frequencies or probabilities must be > 0");

        double randomValue = this.random.draw() * this.cumulativeTotal;
        for (FrequencyAndObject<O> fAndO : this.generators)
        {
            double frequency = fAndO.getFrequency();
            if (frequency >= randomValue)
            {
                return fAndO.getObject();
            }
            randomValue -= frequency;
        }
        // If we get here we missed the intended object by a few ULP; return the first object that has non-zero frequency
        FrequencyAndObject<O> useThisOne = this.generators.get(0);
        for (FrequencyAndObject<O> fAndO : this.generators)
        {
            if (fAndO.getFrequency() > 0)
            {
                useThisOne = fAndO;
                break;
            }
        }
        return useThisOne.getObject();
    }

    /**
     * Append a generator to the internally stored list.
     * @param generator FrequencyAndObject&lt;O&gt;; the generator to add
     * @return Distribution&lt;O&gt;; this
     * @throws ProbabilityException when frequency less than zero
     */
    public final Distribution<O> add(final FrequencyAndObject<O> generator) throws ProbabilityException
    {
        return add(this.generators.size(), generator);
    }

    /**
     * Insert a generator at the specified position in the internally stored list.
     * @param index int; position to store the generator
     * @param generator FrequencyAndObject&lt;O&gt;; the generator to add
     * @return Distribution&lt;O&gt;; this
     * @throws ProbabilityException when frequency less than zero
     */
    public final Distribution<O> add(final int index, final FrequencyAndObject<O> generator) throws ProbabilityException
    {
        Throw.when(generator.getFrequency() < 0, ProbabilityException.class,
                "frequency (or probability) must be >= 0 (got " + generator.getFrequency() + ")");
        this.generators.add(index, generator);
        fixProbabilities();
        return this;
    }

    /**
     * Remove the generator at the specified position from the internally stored list.
     * @param index int; the position
     * @return this
     * @throws IndexOutOfBoundsException when index is &lt; 0 or &gt;= size
     * @throws ProbabilityException if the sum of the remaining probabilities or frequencies adds up to 0
     */
    public final Distribution<O> remove(final int index) throws IndexOutOfBoundsException, ProbabilityException
    {
        this.generators.remove(index);
        fixProbabilities();
        return this;
    }

    /**
     * Replace the generator at the specified position.
     * @param index int; the position of the generator that must be replaced
     * @param generator FrequencyAndObject&lt;O&gt;; the new generator and the frequency (or probability)
     * @return this
     * @throws ProbabilityException when the frequency (or probability) &lt; 0, or when index is &lt; 0 or &gt;= size
     */
    public final Distribution<O> set(final int index, final FrequencyAndObject<O> generator) throws ProbabilityException
    {
        Throw.when(generator.getFrequency() < 0, ProbabilityException.class,
                "frequency (or probability) must be >= 0 (got " + generator.getFrequency() + ")");
        try
        {
            this.generators.set(index, generator);
        }
        catch (IndexOutOfBoundsException ie)
        {
            throw new ProbabilityException("Index out of bounds for set operation, index=" + index);
        }
        fixProbabilities();
        return this;
    }

    /**
     * Alter the frequency (or probability) of one of the stored generators.
     * @param index int; index of the stored generator
     * @param frequency double; new frequency (or probability)
     * @return this
     * @throws ProbabilityException when the frequency (or probability) &lt; 0, or when index is &lt; 0 or &gt;= size
     */
    public final Distribution<O> modifyFrequency(final int index, final double frequency) throws ProbabilityException
    {
        Throw.when(index < 0 || index >= this.size(), ProbabilityException.class, "Index %s out of range (0..%d)", index,
                this.size() - 1);
        return set(index, new FrequencyAndObject<O>(frequency, this.generators.get(index).getObject()));
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
     * @param index int; the index of the FrequencyAndObject to retrieve
     * @return FrequencyAndObject&lt;O&gt;; the generator stored at position <cite>index</cite>
     * @throws ProbabilityException when index &lt; 0 or &gt;= size()
     */
    public final FrequencyAndObject<O> get(final int index) throws ProbabilityException
    {
        try
        {
            return this.generators.get(index);
        }
        catch (IndexOutOfBoundsException ie)
        {
            throw new ProbabilityException("Index out of bounds for set operation, index=" + index);
        }
    }

    /**
     * Report the number of generators.
     * @return int; the number of generators
     */
    public final int size()
    {
        return this.generators.size();
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        StringBuilder result = new StringBuilder();
        result.append("Distribution [");
        String separator = "";
        for (FrequencyAndObject<O> fAndO : this.generators)
        {
            result.append(separator + fAndO.getFrequency() + "->" + fAndO.getObject());
            separator = ", ";
        }
        result.append(']');
        return result.toString();
    }

    /**
     * Immutable storage for a frequency (or probability) plus a Generator.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands.<br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 1, 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @param <O> Type of the object returned by the draw method
     */
    public static class FrequencyAndObject<O> implements Serializable
    {
        /** */
        private static final long serialVersionUID = 20160301L;

        /** Frequency (or probability) of an object. */
        private final double frequency;

        /** The object. */
        private final O object;

        /**
         * Construct a new FrequencyAndObject instance.
         * @param frequency double; the (<b>not cumulative</b>) frequency (or probability) of the <cite>generatingObject</cite>
         * @param object O; an object
         */
        public FrequencyAndObject(final double frequency, final O object)
        {
            this.frequency = frequency;
            this.object = object;
        }

        /**
         * Retrieve the frequency (or probability) of this FrequencyAndObject.
         * @return double; the frequency (or probability) of this FrequencyAndObject
         */
        public final double getFrequency()
        {
            return this.frequency;
        }

        /**
         * Call the draw method of the generatingObject and return its result.
         * @return O; the result of a call to the draw method of the generatingObject
         */
        public final O getObject()
        {
            return this.object;
        }

        /** {@inheritDoc} */
        @Override
        public final int hashCode()
        {
            final int prime = 31;
            int result = 1;
            long temp;
            temp = Double.doubleToLongBits(this.frequency);
            result = prime * result + (int) (temp ^ (temp >>> 32));
            result = prime * result + ((this.object == null) ? 0 : this.object.hashCode());
            return result;
        }

        /** {@inheritDoc} */
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
            FrequencyAndObject<?> other = (FrequencyAndObject<?>) obj;
            if (Double.doubleToLongBits(this.frequency) != Double.doubleToLongBits(other.frequency))
                return false;
            if (this.object == null)
            {
                if (other.object != null)
                    return false;
            }
            else if (!this.object.equals(other.object))
                return false;
            return true;
        }

        /** {@inheritDoc} */
        @Override
        public final String toString()
        {
            return "FrequencyAndObject [frequency=" + this.frequency + ", object=" + this.object + "]";
        }

    }

}
