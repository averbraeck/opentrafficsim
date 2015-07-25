package org.opentrafficsim.core.network.route;

import java.io.Serializable;
import java.util.List;

import nl.tudelft.simulation.jstats.distributions.DistUniform;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

import org.opentrafficsim.core.network.NetworkException;

/**
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
     * @param routeProbabilities the Routes with the probabilities for each one. Instead of probabilities, (observed)
     *            frequencies may be used; i.e. the provided values are internally scaled to add up to 1.0.
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
        this.cumulativeProbabilities = new double[this.routeProbabilities.size()];
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
    public final RouteNavigator generateRouteNavigator()
    {
        double randomValue = this.random.draw();
        for (int index = 0; index < this.cumulativeProbabilities.length; index++)
        {
            if (this.cumulativeProbabilities[index] >= randomValue)
            {
                return this.routeProbabilities.get(index).getRouteNavigator();
            }
        }
        return this.routeProbabilities.get(0).getRouteNavigator();
    }

    /**
     * Combination of route and probability or frequency.
     * <p>
     * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @version Jul 22, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
     */
    public class RouteProbability implements Serializable
    {
        /** */
        private static final long serialVersionUID = 20150722L;

        /** the route navigator. */
        private final RouteNavigator routeNavigator;

        /** the probability or frequency of the route. */
        private final double probability;

        /**
         * @param routeNavigator the route navigator.
         * @param probability the probability or frequency of the route.
         */
        public RouteProbability(final RouteNavigator routeNavigator, final double probability)
        {
            super();
            this.routeNavigator = routeNavigator;
            this.probability = probability;
        }

        /**
         * @return route.
         */
        public final RouteNavigator getRouteNavigator()
        {
            return this.routeNavigator;
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
