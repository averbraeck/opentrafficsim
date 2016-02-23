package org.opentrafficsim.core.network.route;

import java.io.Serializable;
import java.util.List;

import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

import org.opentrafficsim.core.network.NetworkException;

/**
 * Generate one of a set of routes, based on a discrete probability density function.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 20 Mar 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class ProbabilisticRouteGenerator implements RouteGenerator
{
    /** The list of RouteProbability. */
    private final List<RouteProbability> routeProbabilities;

    /** Cumulative probabilities or frequencies corresponding to the routes. */
    private final double[] cumulativeProbabilities;

    /** The uniform random generator used to select from routes. */
    private final DistUniform random;

    /**
     * Construct a new ProbabilistiRouteGenerator using the given random stream.
     * @param routeProbabilities the Routes with the (non-cumulative) probabilities for each one. Instead of probabilities,
     *            (observed) frequencies may be used; The provided values are internally normalized to add up to 1.0.
     * @param stream the random stream to use
     * @throws NetworkException when the probabilities or frequencies are invalid (negative, or all zero)
     */
    public ProbabilisticRouteGenerator(final List<RouteProbability> routeProbabilities, final StreamInterface stream)
        throws NetworkException
    {
        double sum = 0;
        for (RouteProbability rp : routeProbabilities)
        {
            double frequency = rp.getProbability();
            if (frequency < 0)
            {
                throw new NetworkException("Negative probability or frequency is not allowed (got " + frequency + ")");
            }
            sum += frequency;
        }
        if (0 == sum)
        {
            throw new NetworkException("Sum of probabilities or freqencies must be > 0");
        }
        this.cumulativeProbabilities = new double[routeProbabilities.size()];
        int index = 0;
        double cumFreq = 0.0;
        for (RouteProbability rp : routeProbabilities)
        {
            double frequency = rp.getProbability();
            cumFreq += frequency;
            this.cumulativeProbabilities[index++] = cumFreq / sum;
        }
        this.random = new DistUniform(stream, 0, 1);
        this.routeProbabilities = routeProbabilities;
    }

    /** {@inheritDoc} */
    @Override
    public final Route generateRoute()
    {
        double randomValue = this.random.draw();
        for (int index = 0; index < this.cumulativeProbabilities.length; index++)
        {
            if (this.cumulativeProbabilities[index] >= randomValue)
            {
                return this.routeProbabilities.get(index).getRoute();
            }
        }
        return this.routeProbabilities.get(0).getRoute();
    }

    /**
     * Combination of route and probability or frequency, <i>non</i>-cumulative.
     * <p>
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * $LastChangedDate$, @version $Revision$, by $Author$,
     * initial version 20 Mar 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    public static class RouteProbability implements Serializable
    {
        /** */
        private static final long serialVersionUID = 20150722L;

        /** the route. */
        private final Route route;

        /** the probability or frequency of the route. */
        private final double probability;

        /**
         * @param route the route belonging to this probability.
         * @param probability the probability or frequency of the route, <i>non</i>-cumulative.
         */
        public RouteProbability(final Route route, final double probability)
        {
            super();
            this.route = route;
            this.probability = probability;
        }

        /**
         * @return route.
         */
        public final Route getRoute()
        {
            return this.route;
        }

        /**
         * @return probability.
         */
        public final double getProbability()
        {
            return this.probability;
        }

    }
}
