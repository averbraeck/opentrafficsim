package org.opentrafficsim.core.network.route;

import java.util.List;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

import org.opentrafficsim.core.distributions.Distribution;
import org.opentrafficsim.core.distributions.ProbabilityException;

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

    /** The Distribution from which routes are drawn. */
    private final Distribution<Route> distribution;
    /**
     * Create a new Probabilistic Route Generator.
     * @param generators
     * @param stream
     * @throws ProbabilityException
     */
    public ProbabilisticRouteGenerator(
            List<Distribution.FrequencyAndObject<Route>> generators,
            StreamInterface stream) throws ProbabilityException
    {
        this.distribution = new Distribution<Route>(generators, stream);
    }
    
    /** {@inheritDoc} */
    @Override
    public Route draw() throws ProbabilityException
    {
        return this.distribution.draw();
    }
}
