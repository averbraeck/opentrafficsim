package org.opentrafficsim.road.gtu.lane.tactical.following;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Factory for IDM.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 15, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class IDMFactory extends AbstractIDMFactory<IDM>
{

    /**
     * Constructor.
     * @param randomStream StreamInterface; random number stream
     */
    public IDMFactory(final StreamInterface randomStream)
    {
        super(new IDM(), randomStream);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "IDMFactory";
    }

}
