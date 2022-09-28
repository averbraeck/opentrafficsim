package org.opentrafficsim.road.gtu.lane.tactical.following;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class IDMPlusMultiFactory extends AbstractIDMFactory<IDMPlusMulti>
{

    /**
     * Constructor.
     * @param randomStream StreamInterface; random number stream
     */
    public IDMPlusMultiFactory(final StreamInterface randomStream)
    {
        super(new IDMPlusMulti(), randomStream);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "IDMPlusMultiFactory";
    }

}
