package org.opentrafficsim.base.parameters;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.function.Function;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.base.DoubleScalar;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.parameters.constraint.DualBound;

/**
 * Test {@link DualBound}.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class DualBoundConstraintsTest
{

    /** Test values for the test. */
    private static double[] testValues =
            {Double.NEGATIVE_INFINITY, Double.MIN_VALUE, -100, -10, 0, 20, 200, Double.MAX_VALUE, Double.POSITIVE_INFINITY};

    /** */
    private DualBoundConstraintsTest()
    {
        // do not instantiate test class
    }

    /**
     * Test the DoubleBound Constraints.
     */
    @Test
    public void testNumericalConstraints()
    {
        loopCases((d) -> d, Double.class);
        loopCases((d) -> Duration.ofSI(d), Duration.class);
    }

    /**
     * Test cases for a type.
     * @param <T> type
     * @param function function from double to type
     * @param clazz class of value type
     */
    public <T extends Number> void loopCases(final Function<Double, T> function, final Class<T> clazz)
    {
        for (double low : testValues)
        {
            for (double high : testValues)
            {
                checkConstraint(low, high, false, false, function, clazz);
                checkConstraint(low, high, false, true, function, clazz);
                checkConstraint(low, high, true, false, function, clazz);
                checkConstraint(low, high, true, true, function, clazz);
            }
        }
    }

    /**
     * Create a DoubleBound for the given values and test it.
     * @param <T> type
     * @param low the low limit of the DoubleBound
     * @param high the high limit of the DoubleBound
     * @param includeLow does the DoubleBound include the low limit value
     * @param includeHigh does the DoubleBound include the high limit value
     * @param clazz class of value type
     * @param function function from double to type
     */
    public <T extends Number> void checkConstraint(final double low, final double high, final boolean includeLow,
            final boolean includeHigh, final Function<Double, T> function, final Class<T> clazz)
    {
        if (high < low || (high == low && (!includeLow || !includeHigh)))
        {
            try
            {
                create(low, high, includeLow, includeHigh, function, clazz);
            }
            catch (IllegalArgumentException iae)
            {
                // Ignore expected exception
            }
        }
        else
        {
            DualBound<T> db = create(low, high, includeLow, includeHigh, function, clazz);
            // if (includeLow)
            // {
            // assertTrue("DoubleBound includes low", db.includesLowerBound());
            // }
            // else
            // {
            // assertFalse("DoubleBound does not include low", db.includesLowerBound());
            // }
            // if (includeHigh)
            // {
            // assertTrue("DoubleBound includes high", db.includesUpperBound());
            // }
            // else
            // {
            // assertFalse("DoubleBound does not include high", db.includesUpperBound());
            // }
            // assertEquals("lower bound", low, db.getLowerBound());
            // assertEquals("upper bound", high, db.getUpperBound());
            assertTrue(db.toString().indexOf("DualBound") >= 0, "toString returns something sensible");
            for (double testValue : testValues)
            {
                T typeValue = function.apply(testValue);
                boolean actualResult = db.accept(typeValue);
                // System.out.println("testValue=" + testValue + ", DoubleBound=" + db.toString() + ", actualResult="
                // + actualResult);
                if (typeValue.doubleValue() < low)
                {
                    assertFalse(actualResult, "test value below range should fail");
                }
                if (typeValue.doubleValue() == low && includeLow && low != high)
                {
                    assertTrue(actualResult, "test value at low end of range should not fail");
                }
                if (typeValue.doubleValue() == low && typeValue.doubleValue() != high && includeLow)
                {
                    assertTrue(actualResult, "test value at low end of range should not fail");
                }
                if (typeValue.doubleValue() > low && typeValue.doubleValue() < high)
                {
                    assertTrue(actualResult, "test value within range should not fail");
                }
                if (typeValue.doubleValue() == high && typeValue.doubleValue() != low && includeHigh)
                {
                    assertTrue(actualResult, "test value at high end of range should not fail");
                }
                if (typeValue.doubleValue() == high && includeHigh)
                {
                    assertTrue(actualResult, "test value at high end of range should not fail");
                }
                if (typeValue.doubleValue() == high && !includeHigh && low != high)
                {
                    assertFalse(actualResult, "test value at high end of range should fail");
                }
                if (typeValue.doubleValue() > high)
                {
                    assertFalse(actualResult, "test value above range should fail");
                }
            }
        }
    }

    /**
     * Creates a dual bound depending on exclusion.
     * @param <T> type
     * @param low lower bound
     * @param high upper bound
     * @param includeLow whether to include the lower bound
     * @param includeHigh whether to include the upper bound
     * @param function function from double to type
     * @param clazz class of value type
     * @return dual bound depending on exclusion
     */
    @SuppressWarnings("unchecked")
    private <T extends Number> DualBound<T> create(final double low, final double high, final boolean includeLow,
            final boolean includeHigh, final Function<Double, T> function, final Class<T> clazz)
    {
        if (DoubleScalar.class.isAssignableFrom(clazz))
        {
            Function<Double, DoubleScalar<?, ?>> doubleFunction = (Function<Double, DoubleScalar<?, ?>>) function;
            if (includeLow && includeHigh)
            {
                return (DualBound<T>) DualBound.closed(doubleFunction.apply(low), doubleFunction.apply(high));
            }
            else if (includeLow)
            {
                return (DualBound<T>) DualBound.leftClosedRightOpen(doubleFunction.apply(low), doubleFunction.apply(high));
            }
            else if (includeHigh)
            {
                return (DualBound<T>) DualBound.leftOpenRightClosed(doubleFunction.apply(low), doubleFunction.apply(high));
            }
            return (DualBound<T>) DualBound.open(doubleFunction.apply(low), doubleFunction.apply(high));
        }
        Function<Double, Double> doubleFunction = (Function<Double, Double>) function;
        if (includeLow && includeHigh)
        {
            return (DualBound<T>) DualBound.closed(doubleFunction.apply(low), doubleFunction.apply(high));
        }
        else if (includeLow)
        {
            return (DualBound<T>) DualBound.leftClosedRightOpen(doubleFunction.apply(low), doubleFunction.apply(high));
        }
        else if (includeHigh)
        {
            return (DualBound<T>) DualBound.leftOpenRightClosed(doubleFunction.apply(low), doubleFunction.apply(high));
        }
        return (DualBound<T>) DualBound.open(doubleFunction.apply(low), doubleFunction.apply(high));
    }
}
