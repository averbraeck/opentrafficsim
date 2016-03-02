package org.opentrafficsim.core.distributions;

import java.util.ArrayList;
import java.util.List;

import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Generic implementation of a set of objects that have a draw method with corresponding probabilities / frequencies.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 1, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @param <O> Type of the object returned by the draw method
 */
public class Distribution<O> implements Generator<O>
{
    /** The generators (with their probabilities or frequencies). */
    private final List<ProbabilityAndObject<O>> generators;

    /** Array with the cumulative probabilities of the generators. */
    private final double[] cumulativeProbabilities;

    /** The uniform random generator used to select a Generator. */
    private final DistUniform random;

    /**
     * Construct a new Distribution.
     * @param generators List&lt;ProbabilityAndObject&lt;O&gt;&gt;; the generators and their probabilities or frequencies
     * @param stream StreamInterface; source for randomness
     * @throws ProbabilityException when the probabilities or frequencies are invalid (negative or all zero)
     */
    public Distribution(List<ProbabilityAndObject<O>> generators, final StreamInterface stream) throws ProbabilityException
    {
        double sum = 0;
        for (ProbabilityAndObject<O> generator : generators)
        {
            double frequency = generator.getProbabilityOrFrequency();
            if (frequency < 0)
            {
                throw new ProbabilityException("Negative probability or frequency is not allowed (got " + frequency + ")");
            }
            sum += frequency;
        }
        if (0 == sum)
        {
            throw new ProbabilityException("Sum of probabilities or freqencies must be > 0");
        }
        this.cumulativeProbabilities = new double[generators.size()];
        int index = 0;
        double cumFreq = 0.0;
        for (ProbabilityAndObject<O> generator : generators)
        {
            double frequency = generator.getProbabilityOrFrequency();
            cumFreq += frequency;
            this.cumulativeProbabilities[index++] = cumFreq / sum;
        }
        this.random = new DistUniform(stream, 0, 1);
        // Store a defensive copy of the generator list (the generators are immutable; a list of them is not)
        this.generators = new ArrayList<ProbabilityAndObject<O>>(generators);
    }

    /** {@inheritDoc} */
    public O draw()
    {
        double randomValue = this.random.draw();
        for (int index = 0; index < this.cumulativeProbabilities.length; index++)
        {
            if (this.cumulativeProbabilities[index] >= randomValue)
            {
                return this.generators.get(index).draw();
            }
        }
        // We missed the intended one by a few ULP
        return this.generators.get(0).draw();
    }

    /**
     * Storage of a probability or frequency and a Generator.
     * <p>
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 1, 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @param <O> Type of the object returned by the draw method
     */
    public static class ProbabilityAndObject<O>
    {
        /** Probability or frequency of an object. */
        private final double probabilityOrFrequency;

        /** The object. */
        private final O object;

        /**
         * Construct a new ProbabilityAndObject instance.
         * @param probabilityOrFrequency double; the (<b>not cumulative</b>) probability or frequency of the
         *            <cite>generatingObject</cite>
         * @param object O; an object
         */
        public ProbabilityAndObject(final double probabilityOrFrequency, final O object)
        {
            this.probabilityOrFrequency = probabilityOrFrequency;
            this.object = object;
        }

        /**
         * Retrieve the probability or frequency of this ProbabilityAndObject.
         * @return double; the probability or frequency of this ProbabilityAndObject
         */
        public final double getProbabilityOrFrequency()
        {
            return this.probabilityOrFrequency;
        }

        /**
         * Call the draw method of the generatingObject and return its result.
         * @return O; the result of a call to the draw method of the generatingObject
         */
        public final O draw()
        {
            return this.object;
        }
    }

}

