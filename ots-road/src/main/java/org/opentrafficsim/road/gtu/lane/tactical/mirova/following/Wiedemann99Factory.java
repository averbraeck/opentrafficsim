package org.opentrafficsim.road.gtu.lane.tactical.mirova.following;

import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Factory for creating {@link Wiedemann99} car-following models.
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class Wiedemann99Factory extends AbstractWiedemannFactory<Wiedemann99>
{
    /**
     * Constructor for the Wiedemann-99 model factory.
     *
     * @param randomStream the random number stream used to generate stochastic parameters
     */
    public Wiedemann99Factory(final StreamInterface randomStream)
    {
        super(() -> new Wiedemann99(randomStream), randomStream);
    }

    @Override
    public final String toString()
    {
        return "Wiedemann99Factory";
    }
}