package org.opentrafficsim.road.network.route;

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
 * $LastChangedDate: 2015-07-26 01:01:13 +0200 (Sun, 26 Jul 2015) $, @version $Revision: 1155 $, by $Author: averbraeck $,
 * initial version 20 Mar 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class ProbabilisticLaneBasedRouteGenerator implements LaneBasedRouteGenerator
{
    /** The list of RouteProbability. */
    private final List<LaneBasedRouteProbability> laneBasedRouteProbabilities;

    /** Cumulative probabilities or frequencies corresponding to the routes. */
    private final double[] cumulativeProbabilities;

    /** The uniform random generator used to select from routes. */
    private final DistUniform random;

    /**
     * Construct a new ProbabilistiRouteGenerator using the given random stream.
     * @param laneBasedRouteProbabilities the Routes with the probabilities for each one. Instead of probabilities, (observed)
     *            frequencies may be used; i.e. the provided values are internally scaled to add up to 1.0.
     * @param stream the random stream to use
     * @throws NetworkException when the probabilities or frequencies are invalid (negative, or all zero)
     */
    public ProbabilisticLaneBasedRouteGenerator(final List<LaneBasedRouteProbability> laneBasedRouteProbabilities,
        final StreamInterface stream) throws NetworkException
    {
        double sum = 0;
        for (LaneBasedRouteProbability rp : laneBasedRouteProbabilities)
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
        this.cumulativeProbabilities = new double[laneBasedRouteProbabilities.size()];
        int index = 0;
        double cumFreq = 0.0;
        for (LaneBasedRouteProbability rp : laneBasedRouteProbabilities)
        {
            double frequency = rp.getProbability();
            cumFreq += frequency;
            this.cumulativeProbabilities[index++] = cumFreq / sum;
        }
        this.random = new DistUniform(stream, 0, 1);
        this.laneBasedRouteProbabilities = laneBasedRouteProbabilities;
    }

    /** {@inheritDoc} */
    @Override
    public final CompleteLaneBasedRouteNavigator generateRouteNavigator()
    {
        double randomValue = this.random.draw();
        for (int index = 0; index < this.cumulativeProbabilities.length; index++)
        {
            if (this.cumulativeProbabilities[index] >= randomValue)
            {
                return this.laneBasedRouteProbabilities.get(index).getRouteNavigator();
            }
        }
        return this.laneBasedRouteProbabilities.get(0).getRouteNavigator();
    }

    /**
     * Combination of route and probability or frequency.
     * <p>
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. <br>
     * All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @version Jul 22, 2015 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
     */
    public static class LaneBasedRouteProbability implements Serializable
    {
        /** */
        private static final long serialVersionUID = 20150722L;

        /** the route navigator. */
        private final CompleteLaneBasedRouteNavigator laneBasedRouteNavigator;

        /** the probability or frequency of the route. */
        private final double probability;

        /**
         * @param routeNavigator the route navigator.
         * @param probability the probability or frequency of the route.
         */
        public LaneBasedRouteProbability(final CompleteLaneBasedRouteNavigator routeNavigator, final double probability)
        {
            super();
            this.laneBasedRouteNavigator = routeNavigator;
            this.probability = probability;
        }

        /**
         * @return route.
         */
        public final CompleteLaneBasedRouteNavigator getRouteNavigator()
        {
            return this.laneBasedRouteNavigator;
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
