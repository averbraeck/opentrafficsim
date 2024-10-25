package org.opentrafficsim.base.parameters.constraint;

import org.djunits.value.vdouble.scalar.base.DoubleScalar;
import org.djutils.exceptions.Throw;

/**
 * Continuous constraints with a single bound. To allow both {@code Double} and {@code DoubleScalar<?, ?>} constraints, the
 * generic type is restricted to {@code Number}. However, that also allows other subclasses of {@code Number}, e.g.
 * {@code Integer}. Due to rounding and value limits from the type (e.g. {@code Integer.MAX_VALEU}), bounds may not function
 * correctly after a call to {@code Number.doubleValue()}. To restrict the usage, the constructor is private and static factory
 * methods for {@code Double} and {@code DoubleScalar<?, ?>} are supplied.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> value type
 */
public class SingleBound<T extends Number> implements Constraint<T>
{

    /** The bound. */
    private final Bound bound;

    /** Message about values that do not comply with the bound. */
    private final String failMessage;

    /**
     * Creates a lower inclusive bound; {@code value >= bound}.
     * @param bound bound value
     * @return lower inclusive bound
     */
    public static final SingleBound<Double> lowerInclusive(final double bound)
    {
        return createLowerInclusive(bound);
    }

    /**
     * Creates a lower inclusive bound; {@code value >= bound}.
     * @param bound bound value
     * @param <T> value type
     * @return lower inclusive bound
     */
    public static final <T extends DoubleScalar<?, ?>> SingleBound<T> lowerInclusive(final T bound)
    {
        return createLowerInclusive(bound);
    }

    /**
     * Creates a lower inclusive bound; {@code value >= bound}.
     * @param bound bound value
     * @param <T> value type
     * @return lower inclusive bound
     */
    private static <T extends Number> SingleBound<T> createLowerInclusive(final T bound)
    {
        return new SingleBound<>(new LowerBoundInclusive<>(bound),
                String.format("Value is not greater than or equal to %s", bound));
    }

    /**
     * Creates a lower exclusive bound; {@code value > bound}.
     * @param bound bound value
     * @return lower exclusive bound
     */
    public static final SingleBound<Double> lowerExclusive(final double bound)
    {
        return createLowerExclusive(bound);
    }

    /**
     * Creates a lower exclusive bound; {@code value > bound}.
     * @param bound bound value
     * @param <T> value type
     * @return lower exclusive bound
     */
    public static final <T extends DoubleScalar<?, ?>> SingleBound<T> lowerExclusive(final T bound)
    {
        return createLowerExclusive(bound);
    }

    /**
     * Creates a lower exclusive bound; {@code value > bound}.
     * @param bound bound value
     * @param <T> value type
     * @return lower exclusive bound
     */
    private static <T extends Number> SingleBound<T> createLowerExclusive(final T bound)
    {
        return new SingleBound<>(new LowerBoundExclusive<>(bound), String.format("Value is not greater than %s", bound));
    }

    /**
     * Creates an upper inclusive bound; {@code value <= bound}.
     * @param bound bound value
     * @return upper inclusive bound
     */
    public static final SingleBound<Double> upperInclusive(final double bound)
    {
        return createUpperInclusive(bound);
    }

    /**
     * Creates an upper inclusive bound; {@code value <= bound}.
     * @param bound bound value
     * @return upper inclusive bound
     * @param <T> value type
     */
    public static final <T extends DoubleScalar<?, ?>> SingleBound<T> upperInclusive(final T bound)
    {
        return createUpperInclusive(bound);
    }

    /**
     * Creates an upper inclusive bound; {@code value <= bound}.
     * @param bound bound value
     * @param <T> value type
     * @return upper inclusive bound
     */
    private static <T extends Number> SingleBound<T> createUpperInclusive(final T bound)
    {
        return new SingleBound<>(new UpperBoundInclusive<>(bound),
                String.format("Value is not smaller than or equal to %s", bound));
    }

    /**
     * Creates an upper exclusive bound; {@code value < bound}.
     * @param bound bound value
     * @return upper exclusive bound
     */
    public static final SingleBound<Double> upperExclusive(final double bound)
    {
        return createUpperExclusive(bound);
    }

    /**
     * Creates an upper exclusive bound; {@code value < bound}.
     * @param bound bound value
     * @param <T> value type
     * @return upper exclusive bound
     */
    public static final <T extends DoubleScalar<?, ?>> SingleBound<T> upperExclusive(final T bound)
    {
        return createUpperExclusive(bound);
    }

    /**
     * Creates an upper exclusive bound; {@code value < bound}.
     * @param bound bound value
     * @param <T> value type
     * @return upper exclusive bound
     */
    private static <T extends Number> SingleBound<T> createUpperExclusive(final T bound)
    {
        return new SingleBound<>(new UpperBoundExclusive<>(bound), String.format("Value is not smaller than %s", bound));
    }

    /**
     * Constructor.
     * @param bound bound
     * @param failMessage message about values that do not comply with the bound
     */
    SingleBound(final Bound bound, final String failMessage)
    {
        this.bound = bound;
        this.failMessage = failMessage;
    }

    /** {@inheritDoc} */
    @Override
    public boolean accept(final T value)
    {
        return this.bound.accept(value);
    }

    /** {@inheritDoc} */
    @Override
    public String failMessage()
    {
        return this.failMessage;
    }

    /**
     * Returns the bound.
     * @return bound.
     */
    public Bound getBound()
    {
        return this.bound;
    }

    /**
     * Super class for classes that implement a specific numeric check.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    abstract static class Bound
    {

        /** Value of the bound. */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        final Number bound;

        /** Hashcode of the value class. */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        final int classHashcode;

        /** String representation of this bound with %s for the value. */
        private final String stringFormat;

        /**
         * Constructor.
         * @param bound value of the bound
         * @param stringFormat string representation of this bound with %s for the value
         */
        Bound(final Number bound, final String stringFormat)
        {
            Throw.whenNull(bound, "Bound may not be null.");
            Throw.whenNull(bound, "String format may not be null.");
            Throw.when(Double.isNaN(bound.doubleValue()), IllegalArgumentException.class, "Bound value may not be NaN.");
            this.bound = bound;
            this.classHashcode = bound.getClass().hashCode();
            this.stringFormat = stringFormat;
        }

        /**
         * Returns true if the bound accepts the value.
         * @param value the value to check
         * @return true if the bound accepts the value
         */
        abstract boolean accept(Number value);

        @Override
        public final String toString()
        {
            return String.format(this.stringFormat, this.bound);
        }
    }

    /**
     * Class implementing a lower inclusive bound; {@code value >= bound}.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     * @param <T> value type
     */
    static class LowerBoundInclusive<T extends Number> extends Bound
    {

        /**
         * Constructor.
         * @param bound bound
         */
        LowerBoundInclusive(final T bound)
        {
            super(bound, "%s <= value");
        }

        @Override
        protected boolean accept(final Number value)
        {
            return this.bound.doubleValue() <= value.doubleValue();
        }

    }

    /**
     * Class implementing a lower exclusive bound; {@code value > bound}.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     * @param <T> value type
     */
    static class LowerBoundExclusive<T extends Number> extends Bound
    {

        /**
         * Constructor.
         * @param bound bound
         */
        LowerBoundExclusive(final T bound)
        {
            super(bound, "%s < value");
        }

        @Override
        protected boolean accept(final Number value)
        {
            return this.bound.doubleValue() < value.doubleValue();
        }

    }

    /**
     * Class implementing an upper inclusive bound; {@code value <= bound}.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     * @param <T> value type
     */
    static class UpperBoundInclusive<T extends Number> extends Bound
    {

        /**
         * Constructor.
         * @param bound bound
         */
        UpperBoundInclusive(final T bound)
        {
            super(bound, "value <= %s");
        }

        @Override
        protected boolean accept(final Number value)
        {
            return this.bound.doubleValue() >= value.doubleValue();
        }

    }

    /**
     * Class implementing an upper exclusive bound; {@code value < bound}.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     * @param <T> value type
     */
    static class UpperBoundExclusive<T extends Number> extends Bound
    {

        /**
         * Constructor.
         * @param bound bound
         */
        UpperBoundExclusive(final T bound)
        {
            super(bound, "value < %s");
        }

        @Override
        protected boolean accept(final Number value)
        {
            return this.bound.doubleValue() > value.doubleValue();
        }

    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "SingleBound [" + this.bound + "]";
    }

}
