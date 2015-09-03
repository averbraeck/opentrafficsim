package org.opentrafficsim.core.units.distributions;

import nl.tudelft.simulation.jstats.distributions.DistDiscrete;

import org.djunits.unit.Unit;
import org.djunits.value.Absolute;
import org.djunits.value.Relative;
import org.djunits.value.vfloat.scalar.FloatScalar;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-26 01:01:13 +0200 (Sun, 26 Jul 2015) $, @version $Revision: 1155 $, by $Author: averbraeck $,
 * initial version Feb 2, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public interface DiscreteDistFloatScalar
{
    /**
     * Absolute value.
     * @param <T> The absolute floatscalar type
     * @param <U> The unit type used
     */
    public static class Abs<T extends FloatScalar.Abs<U>, U extends Unit<U>> extends DiscreteDistScalar implements
        Absolute
    {
        /**
         * @param distribution the wrapped distribution function.
         * @param unit the unit.
         */
        public Abs(final DistDiscrete distribution, final U unit)
        {
            super(distribution, unit);
        }

        /**
         * @param constant the constant value.
         * @param unit the unit.
         */
        public Abs(final int constant, final U unit)
        {
            super(constant, unit);
        }

        /**
         * @return a drawn number from the distribution in the given unit.
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        public final T draw()
        {
            return (T) new FloatScalar.Abs(getDistribution().draw(), getUnit());
        }
    }

    /**
     * Relative value.
     * @param <T> The absolute floatscalar type
     * @param <U> The unit type used
     */
    public static class Rel<T extends FloatScalar.Rel<U>, U extends Unit<U>> extends DiscreteDistScalar implements
        Relative
    {
        /**
         * @param distribution the wrapped distribution function.
         * @param unit the unit.
         */
        public Rel(final DistDiscrete distribution, final U unit)
        {
            super(distribution, unit);
        }

        /**
         * @param constant the constant value.
         * @param unit the unit.
         */
        public Rel(final int constant, final U unit)
        {
            super(constant, unit);
        }

        /**
         * @return a drawn number from the distribution in the given unit.
         */
        @SuppressWarnings({"unchecked", "rawtypes"})
        public final T draw()
        {
            return (T) new FloatScalar.Rel(getDistribution().draw(), getUnit());
        }
    }

}
