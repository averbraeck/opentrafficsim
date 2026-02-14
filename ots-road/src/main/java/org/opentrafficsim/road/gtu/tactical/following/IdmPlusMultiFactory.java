package org.opentrafficsim.road.gtu.tactical.following;

import java.util.function.Supplier;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class IdmPlusMultiFactory extends AbstractIdmFactory<IdmPlusMulti>
{

    /**
     * Constructor.
     * @param randomStream random number stream
     */
    public IdmPlusMultiFactory(final StreamInterface randomStream)
    {
        super(() -> new IdmPlusMulti(), randomStream);
    }

    /**
     * Constructor.
     * @param randomStream random number stream
     * @param desiredHeadway desired headway model
     * @param desiredSpeed desired speed model
     */
    public IdmPlusMultiFactory(final StreamInterface randomStream, final Supplier<? extends DesiredHeadwayModel> desiredHeadway,
            final Supplier<? extends DesiredSpeedModel> desiredSpeed)
    {
        super(() -> new IdmPlusMulti(desiredHeadway.get(), desiredSpeed.get()), randomStream);
    }

    @Override
    public final String toString()
    {
        return "IdmPlusMultiFactory";
    }

}
