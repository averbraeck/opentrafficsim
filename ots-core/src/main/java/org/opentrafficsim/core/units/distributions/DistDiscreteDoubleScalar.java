package org.opentrafficsim.core.units.distributions;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;
import nl.tudelft.simulation.jstats.distributions.DistDiscreteConstant;
import nl.tudelft.simulation.jstats.streams.Java2Random;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

import org.djunits.unit.Unit;
import org.djunits.value.Absolute;
import org.djunits.value.Relative;
import org.djunits.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-26 01:01:13 +0200 (Sun, 26 Jul 2015) $, @version $Revision: 1155 $, by $Author: averbraeck $,
 * initial version Feb 2, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @param <U> the unit type.
 */
public abstract class DistDiscreteDoubleScalar<U extends Unit<U>>
{
    /** the wrapped distribution function. */
    private final DistDiscrete distribution;

    /** the unit. */
    private final U unit;

    /** the dummy stream for the constant values. Is never really used. */
    private static final StreamInterface DUMMY_STREAM = new Java2Random();

    /**
     * @param distribution the wrapped distribution function.
     * @param unit the unit.
     */
    protected DistDiscreteDoubleScalar(final DistDiscrete distribution, final U unit)
    {
        super();
        this.distribution = distribution;
        this.unit = unit;
    }

    /**
     * @param value the constant value.
     * @param unit the unit.
     */
    protected DistDiscreteDoubleScalar(final long value, final U unit)
    {
        super();
        this.distribution = new DistDiscreteConstant(DUMMY_STREAM, value);
        this.unit = unit;
    }

    /**
     * @return distribution.
     */
    public final DistDiscrete getDistribution()
    {
        return this.distribution;
    }

    /**
     * @return unit.
     */
    public final U getUnit()
    {
        return this.unit;
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "DistDoubleScalar [distribution=" + this.distribution + ", unit=" + this.unit + "]";
    }

    /**
     * Absolute value.
     * @param <U> Unit
     */
    public static class Abs<U extends Unit<U>> extends DistDiscreteDoubleScalar<U> implements Absolute
    {
        /**
         * @param distribution the wrapped distribution function.
         * @param unit the unit.
         */
        protected Abs(final DistDiscrete distribution, final U unit)
        {
            super(distribution, unit);
        }

        /**
         * @return a drawn number from the distribution in the given unit.
         */
        public final DoubleScalar.Abs<U> draw()
        {
            return new DoubleScalar.Abs<U>(getDistribution().draw(), getUnit());
        }
    }

    /**
     * Relative value.
     * @param <U> Unit
     */
    public static class Rel<U extends Unit<U>> extends DistDiscreteDoubleScalar<U> implements Relative
    {
        /**
         * @param distribution the wrapped distribution function.
         * @param unit the unit.
         */
        protected Rel(final DistDiscrete distribution, final U unit)
        {
            super(distribution, unit);
        }

        /**
         * @return a drawn number from the distribution in the given unit.
         */
        public final DoubleScalar.Rel<U> draw()
        {
            return new DoubleScalar.Rel<U>(getDistribution().draw(), getUnit());
        }
    }

}
