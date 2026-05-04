package org.opentrafficsim.road.gtu.lane.tactical.mirova.following;

import java.io.Serializable;

import org.opentrafficsim.road.gtu.lane.tactical.following.AbstractIdmFactory;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Factory for creating {@link MirovaIdmPlus} instances.
 * <p>
 * This factory extends {@link AbstractIdmFactory} to seamlessly integrate into the OpenTrafficSim (OTS) architecture. It
 * provides a fresh instance of the {@link MirovaIdmPlus} car-following model for newly generated GTUs and accepts a random
 * stream for potential stochastic parameter assignments, strictly matching the standard OTS factory pattern.
 * </p>
 * <p>
 * Copyright (c) 2026 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class MirovaIdmPlusFactory extends AbstractIdmFactory<MirovaIdmPlus> implements Serializable
{

    /** Serial version UID. */
    private static final long serialVersionUID = 20260430L;

    /**
     * Constructor for the MiRoVA IDM+ factory.
     * @param randomStream StreamInterface; the random number stream used for stochastic processes.
     */
    public MirovaIdmPlusFactory(final StreamInterface randomStream)
    {
        super(() -> new MirovaIdmPlus(), randomStream);
    }

    /**
     * Returns a string representation of the factory.
     * @return String; a descriptive string for this factory.
     */
    @Override
    public final String toString()
    {
        return "MirovaIdmPlusFactory";
    }

}
