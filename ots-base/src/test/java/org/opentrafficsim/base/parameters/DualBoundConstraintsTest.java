package org.opentrafficsim.base.parameters;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.parameters.constraint.DualBound;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class DualBoundConstraintsTest
{

    /** Test values for the test. */
    private static double[] testValues =
            {Double.NEGATIVE_INFINITY, Double.MIN_VALUE, -100, -10, 0, 20, 200, Double.MAX_VALUE, Double.POSITIVE_INFINITY};

    /**
     * Test the DoubleBound Constraints.
     */
    @Test
    public final void testNumericalConstraints()
    {
        for (double low : testValues)
        {
            for (double high : testValues)
            {
                checkConstraint(low, high, false, false);
                checkConstraint(low, high, false, true);
                checkConstraint(low, high, true, false);
                checkConstraint(low, high, true, true);
            }
        }
    }

    /**
     * Create a DoubleBound for the given values and test it.
     * @param low double; the low limit of the DoubleBound
     * @param high double; the high limit of the DoubleBound
     * @param includeLow boolean; does the DoubleBound include the low limit value
     * @param includeHigh boolean; does the DoubleBound include the high limit value
     */
    public final void checkConstraint(final double low, final double high, final boolean includeLow, final boolean includeHigh)
    {
        if (high < low || (high == low && (!includeLow || !includeHigh)))
        {
            try
            {
                create(low, high, includeLow, includeHigh);
            }
            catch (IllegalArgumentException iae)
            {
                // Ignore expected exception
            }
        }
        else
        {
            DualBound<Double> db = create(low, high, includeLow, includeHigh);
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
                boolean actualResult = db.accept(testValue);
                // System.out.println("testValue=" + testValue + ", DoubleBound=" + db.toString() + ", actualResult="
                // + actualResult);
                if (testValue < low)
                {
                    assertFalse(actualResult, "test value below range should fail");
                }
                if (testValue == low && includeLow && low != high)
                {
                    assertTrue(actualResult, "test value at low end of range should not fail");
                }
                if (testValue == low && testValue != high && includeLow)
                {
                    assertTrue(actualResult, "test value at low end of range should not fail");
                }
                if (testValue > low && testValue < high)
                {
                    assertTrue(actualResult, "test value within range should not fail");
                }
                if (testValue == high && testValue != low && includeHigh)
                {
                    assertTrue(actualResult, "test value at high end of range should not fail");
                }
                if (testValue == high && includeHigh)
                {
                    assertTrue(actualResult, "test value at high end of range should not fail");
                }
                if (testValue == high && !includeHigh && low != high)
                {
                    assertFalse(actualResult, "test value at high end of range should fail");
                }
                if (testValue > high)
                {
                    assertFalse(actualResult, "test value above range should fail");
                }
            }
        }
    }

    /**
     * Creates a dual bound depending on exclusion.
     * @param low lower bound
     * @param high upper bound
     * @param includeLow whether to include the lower bound
     * @param includeHigh whether to include the upper bound
     * @return dual bound depending on exclusion
     */
    private DualBound<Double> create(final double low, final double high, final boolean includeLow, final boolean includeHigh)
    {
        if (includeLow && includeHigh)
        {
            return DualBound.closed(low, high);
        }
        else if (includeLow)
        {
            return DualBound.leftClosedRightOpen(low, high);
        }
        else if (includeHigh)
        {
            return DualBound.leftOpenRightClosed(low, high);
        }
        return DualBound.open(low, high);
    }
}
