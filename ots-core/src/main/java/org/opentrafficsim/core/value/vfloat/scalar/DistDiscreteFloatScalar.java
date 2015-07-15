package org.opentrafficsim.core.value.vfloat.scalar;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;
import nl.tudelft.simulation.jstats.distributions.DistDiscreteConstant;
import nl.tudelft.simulation.jstats.streams.Java2Random;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.Absolute;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial versionFeb 2, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @param <U> the unit type.
 */
public abstract class DistDiscreteFloatScalar<U extends Unit<U>>
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
    protected DistDiscreteFloatScalar(final DistDiscrete distribution, final U unit)
    {
        super();
        this.distribution = distribution;
        this.unit = unit;
    }

    /**
     * @param value the constant value.
     * @param unit the unit.
     */
    protected DistDiscreteFloatScalar(final long value, final U unit)
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
        return "DistFloatScalar [distribution=" + this.distribution + ", unit=" + this.unit + "]";
    }

    /**
     * Absolute value.
     * @param <U> Unit
     */
    public static class Abs<U extends Unit<U>> extends DistDiscreteFloatScalar<U> implements Absolute
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
        public final FloatScalar.Abs<U> draw()
        {
            return new FloatScalar.Abs<U>(getDistribution().draw(), getUnit());
        }
    }

    /**
     * Relative value.
     * @param <U> Unit
     */
    public static class Rel<U extends Unit<U>> extends DistDiscreteFloatScalar<U> implements Absolute
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
        public final FloatScalar.Rel<U> draw()
        {
            return new FloatScalar.Rel<U>(getDistribution().draw(), getUnit());
        }
    }

}
