package org.opentrafficsim.core.units.distributions;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;
import nl.tudelft.simulation.jstats.distributions.DistDiscreteConstant;
import nl.tudelft.simulation.jstats.streams.Java2Random;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

import org.djunits.unit.Unit;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-26 01:01:13 +0200 (Sun, 26 Jul 2015) $, @version $Revision: 1155 $, by $Author: averbraeck $,
 * initial version Feb 2, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public abstract class DiscreteDistScalar
{
    /** The wrapped distribution function. */
    private final DistDiscrete distribution;

    /** the unit. */
    private final Unit<?> unit;

    /** The dummy stream for the constant values. Is never really used. */
    private static final StreamInterface DUMMY_STREAM = new Java2Random();

    /**
     * @param distribution the wrapped distribution function.
     * @param unit the unit.
     */
    protected DiscreteDistScalar(final DistDiscrete distribution, final Unit<?> unit)
    {
        super();
        this.distribution = distribution;
        this.unit = unit;
    }

    /**
     * @param constant the constant value.
     * @param unit the unit.
     */
    protected DiscreteDistScalar(final long constant, final Unit<?> unit)
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
