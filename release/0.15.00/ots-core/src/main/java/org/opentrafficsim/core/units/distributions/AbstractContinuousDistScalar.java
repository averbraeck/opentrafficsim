package org.opentrafficsim.core.units.distributions;

import java.io.Serializable;

import org.djunits.unit.Unit;

import nl.tudelft.simulation.jstats.distributions.DistConstant;
import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.streams.Java2Random;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Continuous distribution with unit.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-26 01:01:13 +0200 (Sun, 26 Jul 2015) $, @version $Revision: 1155 $, by $Author: averbraeck $,
 * initial version Feb 2, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public abstract class AbstractContinuousDistScalar implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150000L;

    /** The wrapped distribution function. */
    private final DistContinuous distribution;

    /** The unit. */
    private final Unit<?> unit;

    /** The dummy stream for the constant values. Is never really used. */
    private static final StreamInterface DUMMY_STREAM = new Java2Random();

    /**
     * @param distribution the wrapped distribution function.
     * @param unit the unit.
     */
    protected AbstractContinuousDistScalar(final DistContinuous distribution, final Unit<?> unit)
    {
        super();
        this.distribution = distribution;
        this.unit = unit;
    }

    /**
     * @param constant the constant value.
     * @param unit the unit.
     */
    protected AbstractContinuousDistScalar(final double constant, final Unit<?> unit)
    {
        this(new DistConstant(DUMMY_STREAM, constant), unit);
    }

    /**
     * @return distribution.
     */
    public final DistContinuous getDistribution()
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
        return "ContinuousDistDoubleScalar [distribution=" + this.distribution + ", unit=" + this.unit + "]";
    }
}
