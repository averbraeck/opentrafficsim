package org.opentrafficsim.core.units.distributions;

import nl.tudelft.simulation.jstats.distributions.DistConstant;
import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.streams.Java2Random;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

import org.djunits.unit.Unit;
import org.djunits.value.Absolute;
import org.djunits.value.vfloat.scalar.FloatScalar;

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
public abstract class DistContinuousFloatScalar<U extends Unit<U>>
{
    /** the wrapped distribution function. */
    private final DistContinuous distribution;

    /** the unit. */
    private final U unit;

    /** the dummy stream for the constant values. Is never really used. */
    private static final StreamInterface DUMMY_STREAM = new Java2Random();

    /**
     * @param distribution the wrapped distribution function.
     * @param unit the unit.
     */
    protected DistContinuousFloatScalar(final DistContinuous distribution, final U unit)
    {
        super();
        this.distribution = distribution;
        this.unit = unit;
    }

    /**
     * @param value the constant value.
     * @param unit the unit.
     */
    protected DistContinuousFloatScalar(final float value, final U unit)
    {
        super();
        this.distribution = new DistConstant(DUMMY_STREAM, value);
        this.unit = unit;
    }

    /**
     * @return distribution.
     */
    public final DistContinuous getDistribution()
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
        return "DistFloatScalar [distribution=" + this.distribution + ", unit=" + this.unit + "]";
    }

    /**
     * Absolute value.
     * @param <U> Unit
     */
    public static class Abs<U extends Unit<U>> extends DistContinuousFloatScalar<U> implements Absolute
    {
        /**
         * @param distribution the wrapped distribution function.
         * @param unit the unit.
         */
        protected Abs(final DistContinuous distribution, final U unit)
        {
            super(distribution, unit);
        }

        /**
         * @return a drawn number from the distribution in the given unit.
         */
        public final FloatScalar.Abs<U> draw()
        {
            return new FloatScalar.Abs<U>((float) getDistribution().draw(), getUnit());
        }
    }

    /**
     * Relative value.
     * @param <U> Unit
     */
    public static class Rel<U extends Unit<U>> extends DistContinuousFloatScalar<U> implements Absolute
    {
        /**
         * @param distribution the wrapped distribution function.
         * @param unit the unit.
         */
        protected Rel(final DistContinuous distribution, final U unit)
        {
            super(distribution, unit);
        }

        /**
         * @return a drawn number from the distribution in the given unit.
         */
        public final FloatScalar.Rel<U> draw()
        {
            return new FloatScalar.Rel<U>((float) getDistribution().draw(), getUnit());
        }
    }

}
