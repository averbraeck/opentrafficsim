package org.opentrafficsim.core.network.route;

import java.io.Serializable;
import java.util.List;

import org.opentrafficsim.core.distributions.Distribution;
import org.opentrafficsim.core.distributions.Generator;
import org.opentrafficsim.core.distributions.ProbabilityException;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Generate one of a set of routes, based on a discrete probability density function.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 20 Mar 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class ProbabilisticRouteGenerator implements Generator<Route>, Serializable
{

    /** */
    private static final long serialVersionUID = 20150000L;

    /** The Distribution from which routes are drawn. */
    private final Distribution<Route> distribution;

    /**
     * Create a new Probabilistic Route Generator.
     * @param generators List&lt;Distribution.FrequencyAndObject&lt;Route&gt;&gt;; list of routes and frequencies
     * @param stream StreamInterface; the entropy source
     * @throws ProbabilityException when the probabilities are invalid
     */
    public ProbabilisticRouteGenerator(final List<Distribution.FrequencyAndObject<Route>> generators,
            final StreamInterface stream) throws ProbabilityException
    {
        this.distribution = new Distribution<Route>(generators, stream);
    }

    /** {@inheritDoc} */
    @Override
    public final Route draw() throws ProbabilityException
    {
        return this.distribution.draw();
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "ProbabilisticRouteGenerator [distribution=" + this.distribution + "]";
    }
}
