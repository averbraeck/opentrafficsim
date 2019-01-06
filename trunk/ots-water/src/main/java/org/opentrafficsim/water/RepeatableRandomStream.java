package org.opentrafficsim.water;

import nl.tudelft.simulation.jstats.streams.Java2Random;

/**
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * <p>
 * Based on software from the IDVV project, which is Copyright (c) 2013 Rijkswaterstaat - Dienst Water, Verkeer en Leefomgeving
 * and licensed without restrictions to Delft University of Technology, including the right to sub-license sources and derived
 * products to third parties.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Nov 6, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public final class RepeatableRandomStream extends Java2Random
{

    /** */
    private static final long serialVersionUID = 1L;

    /**
     * Do not instantiate from outside.
     */
    private RepeatableRandomStream()
    {
        super();
    }

    /**
     * Do not instantiate from outside.
     * @param seed long; the seed to instantiate the stream.
     */
    private RepeatableRandomStream(final long seed)
    {
        super(seed);
    }

    /**
     * Return a random stream that is the same every time for a certain name, but can be changed based on a number (e.g. a year)
     * @param identifier String; the identifier for repeatability
     * @param seed long; the seed
     * @return a random stream that is the same every time for a certain name and integer
     */
    public static RepeatableRandomStream create(final String identifier, final long seed)
    {
        return new RepeatableRandomStream(identifier.hashCode() + seed);
    }

    /**
     * Return a random stream that is the same every time for a certain name.
     * @param identifier String; the identifier for repeatability
     * @return a random stream that is the same every time for a certain name
     */
    public static RepeatableRandomStream create(final String identifier)
    {
        return new RepeatableRandomStream(identifier.hashCode());
    }
}
