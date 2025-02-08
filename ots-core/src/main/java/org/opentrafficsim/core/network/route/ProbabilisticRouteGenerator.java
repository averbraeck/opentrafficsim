package org.opentrafficsim.core.network.route;

import java.io.Serializable;
import java.util.List;

import org.opentrafficsim.core.distributions.ObjectDistribution;
import org.opentrafficsim.core.distributions.FrequencyAndObject;
import org.opentrafficsim.core.distributions.Generator;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Generate one of a set of routes, based on a discrete probability density function.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version 20 Mar 2015 <br>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public class ProbabilisticRouteGenerator implements Generator<Route>, Serializable
{

    /** */
    private static final long serialVersionUID = 20150000L;

    /** The Distribution from which routes are drawn. */
    private final ObjectDistribution<Route> distribution;

    /**
     * Create a new Probabilistic Route Generator.
     * @param generators List&lt;Distribution.FrequencyAndObject&lt;Route&gt;&gt;; list of routes and frequencies
     * @param stream the entropy source
     */
    public ProbabilisticRouteGenerator(final List<FrequencyAndObject<Route>> generators, final StreamInterface stream)
    {
        this.distribution = new ObjectDistribution<Route>(generators, stream);
    }

    @Override
    public final Route draw()
    {
        return this.distribution.draw();
    }

    @Override
    public final String toString()
    {
        return "ProbabilisticRouteGenerator [distribution=" + this.distribution + "]";
    }
}
