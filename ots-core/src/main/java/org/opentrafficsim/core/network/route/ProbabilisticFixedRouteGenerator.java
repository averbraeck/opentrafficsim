package org.opentrafficsim.core.network.route;

import java.util.ArrayList;
import java.util.SortedMap;

import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;

import org.opentrafficsim.core.network.NetworkException;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version 0 mrt. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class ProbabilisticFixedRouteGenerator implements RouteGenerator
{
    /** The ordered list of RouteGenerators. */
    private final ArrayList<RouteGenerator> routes = new ArrayList<RouteGenerator>();

    /** Probabilities corresponding to the routes. */
    private final double[] probabilities;

    /** The uniform random generator used to select from routes. */
    private final DistUniform random;

    /**
     * Construct a new ProbabilisticFixedRouteGenerator using <code>System.currentTimeMillis()</code> to seed the random
     * number generator.
     * @param routeProbabilities SortedMap&lt;RouteGenerator, Double&gt;; the RouteGenerators with the probabilities for
     *            each one. In stead of probabilities, (observed) counts may be used; i.e. the provided values are
     *            internally scaled to add up to 1.0.
     * @throws NetworkException when the probabilities or frequencies are invalid (negative, or all zero)
     */
    public ProbabilisticFixedRouteGenerator(final SortedMap<RouteGenerator, Double> routeProbabilities)
            throws NetworkException
    {
        this(routeProbabilities, System.currentTimeMillis());
        System.err.println("Seeding random generator from clock");
    }

    /**
     * Construct a new ProbabilisticFixedRouteGenerator.
     * @param routeProbabilities SortedMap&lt;RouteGenerator, Double&gt;; the RouteGenerators with the probabilities for
     *            each one. In stead of probabilities, (observed) counts may be used; i.e. the provided values are
     *            internally scaled to add up to 1.0.
     * @param seed long; the seed for the random number generator that will be used
     * @throws NetworkException when the probabilities or frequencies are invalid (negative, or all zero)
     */
    public ProbabilisticFixedRouteGenerator(final SortedMap<RouteGenerator, Double> routeProbabilities, final long seed)
            throws NetworkException
    {
        double sum = 0;
        for (RouteGenerator rg : routeProbabilities.keySet())
        {
            Double frequency = routeProbabilities.get(rg);
            if (frequency < 0)
            {
                throw new NetworkException("Negative probability or frequency is not allowed (got " + frequency + ")");
            }
            sum += frequency;
            this.routes.add(rg);
        }
        if (0 == sum)
        {
            throw new NetworkException("Sum of probabilities or freqencies must be > 0");
        }
        this.probabilities = new double[this.routes.size()];
        int index = 0;
        for (RouteGenerator rg : routeProbabilities.keySet())
        {
            Double frequency = routeProbabilities.get(rg);
            this.probabilities[index++] = frequency / sum;
        }
        this.random = new DistUniform(new MersenneTwister(seed), 0, 1);
    }

    /** {@inheritDoc} */
    @Override
    public final Route generateRoute()
    {
        double randomValue = this.random.draw();
        for (int index = 0; index < this.probabilities.length; index++)
        {
            double bucketSize = this.probabilities[index];
            if (bucketSize >= randomValue)
            {
                // System.out.println("calling " + this.routes.get(index));
                return this.routes.get(index).generateRoute();
            }
            randomValue -= bucketSize;
        }
        if (randomValue > 0.001)
        {
            throw new Error("Cannot happen");
        }
        return this.routes.get(0).generateRoute();
    }

}
