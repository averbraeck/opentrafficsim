package org.opentrafficsim.core.units.distributions;

import java.io.Serializable;

import org.djunits.unit.Unit;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;
import nl.tudelft.simulation.jstats.distributions.DistDiscreteConstant;
import nl.tudelft.simulation.jstats.streams.Java2Random;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Discrete distribution with unit.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 */
public abstract class AbstractDiscreteDistScalar implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150000L;

    /** The wrapped distribution function. */
    private final DistDiscrete distribution;

    /** The unit. */
    private final Unit<?> unit;

    /** The dummy stream for the constant values. Is never really used. */
    private static final StreamInterface DUMMY_STREAM = new Java2Random();

    /**
     * @param distribution DistDiscrete; the wrapped distribution function.
     * @param unit Unit&lt;?&gt;; the unit.
     */
    protected AbstractDiscreteDistScalar(final DistDiscrete distribution, final Unit<?> unit)
    {
        this.distribution = distribution;
        this.unit = unit;
    }

    /**
     * @param constant long; the constant value.
     * @param unit Unit&lt;?&gt;; the unit.
     */
    protected AbstractDiscreteDistScalar(final long constant, final Unit<?> unit)
    {
        this(new DistDiscreteConstant(DUMMY_STREAM, constant), unit);
    }

    /**
     * @return distribution.
     */
    public final DistDiscrete getDistribution()
    {
        return this.distribution;
    }

    /**
     * @return the unit
     */
    public final Unit<?> getUnit()
    {
        return this.unit;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "DiscreteDistScalar [distribution=" + this.distribution + ", unit=" + this.unit + "]";
    }
}
