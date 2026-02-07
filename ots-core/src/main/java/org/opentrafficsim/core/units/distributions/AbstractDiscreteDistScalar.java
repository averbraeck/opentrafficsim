package org.opentrafficsim.core.units.distributions;

import org.djunits.unit.Unit;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;
import nl.tudelft.simulation.jstats.distributions.DistDiscreteConstant;
import nl.tudelft.simulation.jstats.streams.Java2Random;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Discrete distribution with unit.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 */
public abstract class AbstractDiscreteDistScalar
{
    /** The wrapped distribution function. */
    private final DistDiscrete distribution;

    /** The unit. */
    private final Unit<?> unit;

    /** The dummy stream for the constant values. Is never really used. */
    private static final StreamInterface DUMMY_STREAM = new Java2Random();

    /**
     * Constructor.
     * @param distribution the wrapped distribution function.
     * @param unit the unit.
     */
    protected AbstractDiscreteDistScalar(final DistDiscrete distribution, final Unit<?> unit)
    {
        this.distribution = distribution;
        this.unit = unit;
    }

    /**
     * Constructor.
     * @param constant the constant value.
     * @param unit the unit.
     */
    protected AbstractDiscreteDistScalar(final long constant, final Unit<?> unit)
    {
        this(new DistDiscreteConstant(DUMMY_STREAM, constant), unit);
    }

    /**
     * Returns the distribution.
     * @return distribution.
     */
    public final DistDiscrete getDistribution()
    {
        return this.distribution;
    }

    /**
     * Returns the unit.
     * @return the unit
     */
    public final Unit<?> getUnit()
    {
        return this.unit;
    }

    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "DiscreteDistScalar [distribution=" + this.distribution + ", unit=" + this.unit + "]";
    }
}
