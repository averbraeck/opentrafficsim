package org.opentrafficsim.road.gtu.lane.tactical.following;

import java.util.function.Supplier;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Factory for IDM+.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class IdmPlusFactory extends AbstractIdmFactory<IdmPlus>
{

    /**
     * Constructor.
     * @param randomStream random number stream
     */
    public IdmPlusFactory(final StreamInterface randomStream)
    {
        super(() -> new IdmPlus(), randomStream);
    }

    /**
     * Constructor.
     * @param randomStream random number stream
     * @param desiredHeadway desired headway model
     * @param desiredSpeed desired speed model
     */
    public IdmPlusFactory(final StreamInterface randomStream, final Supplier<? extends DesiredHeadwayModel> desiredHeadway,
            final Supplier<? extends DesiredSpeedModel> desiredSpeed)
    {
        super(() -> new IdmPlus(desiredHeadway.get(), desiredSpeed.get()), randomStream);
    }

    @Override
    public final String toString()
    {
        return "IdmPlusFactory";
    }

}
