package org.opentrafficsim.base.parameters.constraint;

import org.djunits.value.vdouble.scalar.base.AbstractDoubleScalar;
import org.djutils.exceptions.Throw;

/**
 * Continuous constraints with a dual bound. To allow both {@code Double} and {@code AbstractDoubleScalar<?, ?>} constraints,
 * the generic type is restricted to {@code Number}. However, that also allows other subclasses of {@code Number}, e.g.
 * {@code Integer}. Due to rounding and value limits from the type (e.g. {@code Integer.MAX_VALEU}), bounds may not function
 * correctly after a call to {@code Number.doubleValue()}. To restrict the usage, the constructor is private and static factory
 * methods for {@code Double} and {@code AbstractDoubleScalar<?, ?>} are supplied.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 8 sep. 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <T> value type
 */
public final class DualBound<T extends Number> extends SingleBound<T>
{

    /**
     * Standard dual bound on the unit interval [0...1]. This can be used for both {@code Double} and
     * {@code AbstractDoubleScalar<?, ?>} parameters.
     */
    public static final DualBound<Number> UNITINTERVAL = createClosed(0.0, 1.0);

    /** The upper bound. */
    private final Bound upperBound;

    /**
     * Creates a dual bound <i>including</i> the bounds; {@code lowerBound <= value <= upperBound}. Bounds may be equal.
     * @param lowerBound double; lower bound value
     * @param upperBound double; upper bound value
     * @return closed dual bound
     */
    public static DualBound<Double> closed(final double lowerBound, final double upperBound)
    {
        return createClosed(lowerBound, upperBound);
    }

    /**
     * Creates a dual bound <i>including</i> the bounds; {@code lowerBound <= value <= upperBound}.
     * @param lowerBound T; lower bound value
     * @param upperBound T; upper bound value
     * @param <T> value type
     * @return closed dual bound
     */
    public static <T extends AbstractDoubleScalar<?, ?>> DualBound<T> closed(final T lowerBound, final T upperBound)
    {
        return createClosed(lowerBound, upperBound);
    }

    /**
     * Creates a dual bound <i>including</i> the bounds; {@code lowerBound <= value <= upperBound}.
     * @param lowerBound T; lower bound value
     * @param upperBound T; upper bound value
     * @param <T> value type
     * @return closed dual bound
     */
    private static <T extends Number> DualBound<T> createClosed(final T lowerBound, final T upperBound)
    {
        Throw.when(lowerBound.doubleValue() > upperBound.doubleValue(), IllegalArgumentException.class,
                "Lower bound must be smaller or equal than the upper bound for a closed interval.");
        return new DualBound<>(new LowerBoundInclusive<>(lowerBound), new UpperBoundInclusive<>(upperBound), String
                .format("Value is not greater than or equal to %s and smaller than or equal to %s", lowerBound, upperBound));
    }

    /**
     * Creates a dual bound <i>excluding</i> the bounds; {@code lowerBound < value < upperBound}.
     * @param lowerBound double; lower bound value
     * @param upperBound double; upper bound value
     * @return open dual bound
     */
    public static DualBound<Double> open(final double lowerBound, final double upperBound)
    {
        return createOpen(lowerBound, upperBound);
    }

    /**
     * Creates a dual bound <i>excluding</i> the bounds; {@code lowerBound < value < upperBound}.
     * @param lowerBound T; lower bound value
     * @param upperBound T; upper bound value
     * @param <T> value type
     * @return open dual bound
     */
    public static <T extends AbstractDoubleScalar<?, ?>> DualBound<T> open(final T lowerBound, final T upperBound)
    {
        return createOpen(lowerBound, upperBound);
    }

    /**
     * Creates a dual bound <i>excluding</i> the bounds; {@code lowerBound < value < upperBound}.
     * @param lowerBound T; lower bound value
     * @param upperBound T; upper bound value
     * @param <T> value type
     * @return open dual bound
     */
    private static <T extends Number> DualBound<T> createOpen(final T lowerBound, final T upperBound)
    {
        checkSeparation(lowerBound, upperBound);
        return new DualBound<>(new LowerBoundExclusive<>(lowerBound), new UpperBoundExclusive<>(upperBound),
                String.format("Value is not greater than %s and smaller than %s", lowerBound, upperBound));
    }

    /**
     * Creates a dual bound <i>excluding</i> the lower bound and <i>including</i> the upper bound;
     * {@code lowerBound < value <= upperBound}.
     * @param lowerBound double; lower bound value
     * @param upperBound double; upper bound value
     * @return dual bound; excluding the lower bound and including the upper bound
     */
    public static DualBound<Double> leftOpenRightClosed(final double lowerBound, final double upperBound)
    {
        return createLeftOpenRightClosed(lowerBound, upperBound);
    }

    /**
     * Creates a dual bound <i>excluding</i> the lower bound and <i>including</i> the upper bound;
     * {@code lowerBound < value <= upperBound}.
     * @param lowerBound T; lower bound value
     * @param upperBound T; upper bound value
     * @param <T> value type
     * @return dual bound; excluding the lower bound and including the upper bound
     */
    public static <T extends AbstractDoubleScalar<?, ?>> DualBound<T> leftOpenRightClosed(final T lowerBound,
            final T upperBound)
    {
        return createLeftOpenRightClosed(lowerBound, upperBound);
    }

    /**
     * Creates a dual bound <i>excluding</i> the lower bound and <i>including</i> the upper bound;
     * {@code lowerBound < value <= upperBound}.
     * @param lowerBound T; lower bound value
     * @param upperBound T; upper bound value
     * @param <T> value type
     * @return dual bound; excluding the lower bound and including the upper bound
     */
    private static <T extends Number> DualBound<T> createLeftOpenRightClosed(final T lowerBound, final T upperBound)
    {
        checkSeparation(lowerBound, upperBound);
        return new DualBound<>(new LowerBoundExclusive<>(lowerBound), new UpperBoundInclusive<>(upperBound),
                String.format("Value is not greater than %s and smaller than or equal to %s", lowerBound, upperBound));
    }

    /**
     * Creates a dual bound <i>including</i> the lower bound and <i>excluding</i> the upper bound;
     * {@code lowerBound <= value < upperBound}.
     * @param lowerBound double; lower bound value
     * @param upperBound double; upper bound value
     * @return dual bound; including the lower bound and excluding the upper bound
     */
    public static DualBound<Double> leftClosedRightOpen(final double lowerBound, final double upperBound)
    {
        return createLeftClosedRightOpen(lowerBound, upperBound);
    }

    /**
     * Creates a dual bound <i>including</i> the lower bound and <i>excluding</i> the upper bound;
     * {@code lowerBound <= value < upperBound}.
     * @param lowerBound T; lower bound value
     * @param upperBound T; upper bound value
     * @param <T> value type
     * @return dual bound; including the lower bound and excluding the upper bound
     */
    public static <T extends AbstractDoubleScalar<?, ?>> DualBound<T> leftClosedRightOpen(final T lowerBound,
            final T upperBound)
    {
        return createLeftClosedRightOpen(lowerBound, upperBound);
    }

    /**
     * Creates a dual bound <i>including</i> the lower bound and <i>excluding</i> the upper bound;
     * {@code lowerBound <= value < upperBound}.
     * @param lowerBound T; lower bound value
     * @param upperBound T; upper bound value
     * @param <T> value type
     * @return dual bound; including the lower bound and excluding the upper bound
     */
    private static <T extends Number> DualBound<T> createLeftClosedRightOpen(final T lowerBound, final T upperBound)
    {
        checkSeparation(lowerBound, upperBound);
        return new DualBound<>(new LowerBoundInclusive<>(lowerBound), new UpperBoundExclusive<>(upperBound),
                String.format("Value is not greater than or equal to %s and smaller than %s", lowerBound, upperBound));
    }

    /**
     * Checks whether both values for the bounds are positively separated. This should not be used for a closed interval where
     * equal bounds can be accepted.
     * @param lowerBound T; lower bound value
     * @param upperBound T; upper bound value
     * @throws IllegalArgumentException if the bound values are not positively separated
     * @param <T> value type
     */
    private static <T extends Number> void checkSeparation(final T lowerBound, final T upperBound)
    {
        Throw.when(lowerBound.doubleValue() >= upperBound.doubleValue(), IllegalArgumentException.class,
                "Lower bound must be smaller than the upper bound.");
    }

    /**
     * Constructor.
     * @param lowerBound Bound; lower bound
     * @param upperBound Bound; upper bound
     * @param failMessage String; message about values that do not comply with the bound
     */
    private DualBound(final Bound lowerBound, final Bound upperBound, final String failMessage)
    {
        super(lowerBound, failMessage);
        this.upperBound = upperBound;
    }

    /** {@inheritDoc} */
    @Override
    public boolean accept(final T value)
    {
        return super.accept(value) && this.upperBound.accept(value);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "DualBound [" + this.getBound() + ", " + this.upperBound + "]";
    }

}
