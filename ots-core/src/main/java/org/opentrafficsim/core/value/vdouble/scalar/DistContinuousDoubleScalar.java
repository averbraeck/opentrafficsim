package org.opentrafficsim.core.value.vdouble.scalar;

import nl.tudelft.simulation.jstats.distributions.DistConstant;
import nl.tudelft.simulation.jstats.distributions.DistContinuous;
import nl.tudelft.simulation.jstats.streams.Java2Random;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

import org.opentrafficsim.core.unit.Unit;
import org.opentrafficsim.core.value.Absolute;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version eb 2, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @param <U> the unit type.
 */
public abstract class DistContinuousDoubleScalar<U extends Unit<U>>
{
    /** The wrapped distribution function. */
    private final DistContinuous distribution;

    /** The unit of the generated values. */
    private final U unit;

    /** The dummy stream for the constant values. Is never really used. */
    private static final StreamInterface DUMMY_STREAM = new Java2Random();

    /**
     * @param distribution the wrapped distribution function.
     * @param unit the unit.
     */
    protected DistContinuousDoubleScalar(final DistContinuous distribution, final U unit)
    {
        super();
        this.distribution = distribution;
        this.unit = unit;
    }

    /**
     * @param value the constant value.
     * @param unit the unit.
     */
    protected DistContinuousDoubleScalar(final double value, final U unit)
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
        return "DistDoubleScalar [distribution=" + this.distribution + ", unit=" + this.unit + "]";
    }

    /**
     * Absolute value.
     * @param <U> Unit
     */
    public static class Abs<U extends Unit<U>> extends DistContinuousDoubleScalar<U> implements Absolute
    {
        /**
         * @param distribution the wrapped distribution function.
         * @param unit the unit.
         */
        public Abs(final DistContinuous distribution, final U unit)
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
    public static class Rel<U extends Unit<U>> extends DistContinuousDoubleScalar<U> implements Absolute
    {
        /**
         * @param distribution the wrapped distribution function.
         * @param unit the unit.
         */
        public Rel(final DistContinuous distribution, final U unit)
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
