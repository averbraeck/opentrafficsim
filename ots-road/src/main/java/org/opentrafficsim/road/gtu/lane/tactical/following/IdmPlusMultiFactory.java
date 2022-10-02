package org.opentrafficsim.road.gtu.lane.tactical.following;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class IdmPlusMultiFactory extends AbstractIdmFactory<IdmPlusMulti>
{

    /**
     * Constructor.
     * @param randomStream StreamInterface; random number stream
     */
    public IdmPlusMultiFactory(final StreamInterface randomStream)
    {
        super(new IdmPlusMulti(), randomStream);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "IDMPlusMultiFactory";
    }

}
