package org.opentrafficsim.core.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.djunits.value.ValueException;
import org.junit.Test;

/**
 * Test the Solver class.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Dec 10, 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SolverTest
{

    /**
     * Linear solver.
     */
    @SuppressWarnings("checkstyle:methodlength")
    @Test
    public final void solverTest()
    {
        // Linear equations
        double[] result = Solver.solve(0, 0);
        assertEquals("length of result should be 0", 0, result.length);
        result = Solver.solve(0, 0, 0);
        assertEquals("length of result should be 0", 0, result.length);
        result = Solver.solve(0, 1);
        assertEquals("length of result should be 0", 0, result.length);
        result = Solver.solve(0, 0, 1);
        assertEquals("length of result should be 0", 0, result.length);
        try
        {
            Solver.firstSolutionAfter(0, 0, 0, 0);
            fail("Unsolvable linear equation should have thrown an exception");
        }
        catch (ValueException ve)
        {
            // Ignore expected exception
        }
        result = Solver.solve(3, 1);
        assertEquals("length of result should be 1", 1, result.length);
        assertEquals("solution should match", -0.3333333333, result[0], 0.000001);
        try
        {
            Solver.firstSolutionAfter(0, 0, 3, 1);
            fail("No solution after boundary should have thrown an exception");
        }
        catch (ValueException exception)
        {
            // Ignore expected exception
        }
        try
        {
            assertEquals("solution should match", -0.3333333333, Solver.firstSolutionAfter(-0.5, 0, 3, 1), 0.000001);
        }
        catch (ValueException exception)
        {
            fail("Caught unexpected exception");
        }
        result = Solver.solve(0, 3, 1);
        assertEquals("length of result should be 1", 1, result.length);
        assertEquals("solution should match", -0.3333333333, result[0], 0.000001);
        // Quadratic equations
        result = Solver.solve(1, 0, 1);
        assertEquals("length of result should be 0", 0, result.length);
        try
        {
            Solver.firstSolutionAfter(0, 1, 0, 1);
            fail("Equation with no solutions should have thrown an exception");
        }
        catch (ValueException exception)
        {
            // Ignore expected exception
        }
        result = Solver.solve(1, 0, 0);
        assertEquals("length of result should be 1", 1, result.length);
        assertEquals("solution should match", 0, result[0], 0.000001);
        try
        {
            assertEquals("solution should match", 0, Solver.firstSolutionAfter(-0.00001, 1, 0, 0), 0.000001);
        }
        catch (ValueException exception)
        {
            fail("Caught unexpected exception");
        }
        result = Solver.solve(1, 0, -1);
        assertEquals("length of result should be 2", 2, result.length);
        assertEquals("Smallest solution should match", -1, Math.min(result[0], result[1]), 0.000001);
        assertEquals("Largest solution should match", 1, Math.max(result[0], result[1]), 0.000001);
        try
        {
            assertEquals("First solution after -10 should be -1", -1, Solver.firstSolutionAfter(-10, 1, 0, -1), 0.000001);
        }
        catch (ValueException exception)
        {
            fail("Caught unexpected exception");
        }
        try
        {
            assertEquals("First solution after 0 should be 1", 1, Solver.firstSolutionAfter(0, 1, 0, -1), 0.000001);
        }
        catch (ValueException exception)
        {
            fail("Caught unexpected exception");
        }
        try
        {
            Solver.firstSolutionAfter(2, 1, 0, -1);
            fail("First solution after 2 should have thrown an exception");
        }
        catch (ValueException exception)
        {
            // Ignore expected exception
        }
        result = Solver.solve(1, 2, -3);
        // System.out.println("x0=" + result[0] + " x1=" + result[1]);
        assertEquals("length of result should be 2", 2, result.length);
        assertEquals("Smallest solution should match", -3, Math.min(result[0], result[1]), 0.000001);
        assertEquals("Largest solution should match", 1, Math.max(result[0], result[1]), 0.000001);
        // System.out.println("solutions are " + Solver.solve(1, 2, -3)[0] + ", " + Solver.solve(1, 2, -3)[1]);
        try
        {
            assertEquals("First solution after -10 should be -3", -3, Solver.firstSolutionAfter(-10, 1, 2, -3), 0.000001);
        }
        catch (ValueException exception)
        {
            fail("Caught unexpected exception");
        }
        try
        {
            assertEquals("First solution after 0 should be 1", 1, Solver.firstSolutionAfter(0, 1, 2, -3), 0.000001);
        }
        catch (ValueException exception)
        {
            fail("Caught unexpected exception");
        }
        try
        {
            Solver.firstSolutionAfter(2, 1, 2, -3);
            fail("First solution after 2 should have thrown an exception");
        }
        catch (ValueException exception)
        {
            // Ignore expected exception
        }
        // Invert a (so the other branch of the if statement gets tested)
        // System.out.println("solutions are " + Solver.solve(-1, -2, 3)[0] + ", " + Solver.solve(-1, -2, 3)[1]);
        try
        {
            assertEquals("First solution after -10 should be -3", -3, Solver.firstSolutionAfter(-10, -1, -2, 3), 0.000001);
        }
        catch (ValueException exception)
        {
            fail("Caught unexpected exception");
        }
        try
        {
            assertEquals("First solution after 0 should be 1", 1, Solver.firstSolutionAfter(0, -1, -2, 3), 0.000001);
        }
        catch (ValueException exception)
        {
            fail("Caught unexpected exception");
        }
        try
        {
            Solver.firstSolutionAfter(2, -1, -2, 3);
            fail("First solution after 2 should have thrown an exception");
        }
        catch (ValueException exception)
        {
            // Ignore expected exception
        }

    }
}
