package org.opentrafficsim.base.parameters;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opentrafficsim.base.parameters.constraint.DualBound;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Aug 16, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
            assertTrue("toString returns something sensible", db.toString().indexOf("DualBound") >= 0);
            for (double testValue : testValues)
            {
                boolean actualResult = db.accept(testValue);
                // System.out.println("testValue=" + testValue + ", DoubleBound=" + db.toString() + ", actualResult="
                // + actualResult);
                if (testValue < low)
                {
                    assertFalse("test value below range should fail", actualResult);
                }
                if (testValue == low && includeLow && low != high)
                {
                    assertTrue("test value at low end of range should not fail", actualResult);
                }
                if (testValue == low && testValue != high && includeLow)
                {
                    assertTrue("test value at low end of range should not fail", actualResult);
                }
                if (testValue > low && testValue < high)
                {
                    assertTrue("test value within range should not fail", actualResult);
                }
                if (testValue == high && testValue != low && includeHigh)
                {
                    assertTrue("test value at high end of range should not fail", actualResult);
                }
                if (testValue == high && includeHigh)
                {
                    assertTrue("test value at high end of range should not fail", actualResult);
                }
                if (testValue == high && !includeHigh && low != high)
                {
                    assertFalse("test value at high end of range should fail", actualResult);
                }
                if (testValue > high)
                {
                    assertFalse("test value above range should fail", actualResult);
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
