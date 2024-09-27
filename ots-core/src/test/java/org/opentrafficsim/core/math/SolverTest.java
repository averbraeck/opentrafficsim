package org.opentrafficsim.core.math;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import org.djunits.value.ValueRuntimeException;
import org.junit.jupiter.api.Test;

/**
 * Test the Solver class.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
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
        assertEquals(0, result.length, "length of result should be 0");
        result = Solver.solve(0, 0, 0);
        assertEquals(0, result.length, "length of result should be 0");
        result = Solver.solve(0, 1);
        assertEquals(0, result.length, "length of result should be 0");
        result = Solver.solve(0, 0, 1);
        assertEquals(0, result.length, "length of result should be 0");
        try
        {
            Solver.firstSolutionAfter(0, 0, 0, 0);
            fail("Unsolvable linear equation should have thrown an exception");
        }
        catch (ValueRuntimeException ve)
        {
            // Ignore expected exception
        }
        result = Solver.solve(3, 1);
        assertEquals(1, result.length, "length of result should be 1");
        assertEquals(-0.3333333333, result[0], 0.000001, "solution should match");
        try
        {
            Solver.firstSolutionAfter(0, 0, 3, 1);
            fail("No solution after boundary should have thrown an exception");
        }
        catch (ValueRuntimeException exception)
        {
            // Ignore expected exception
        }
        try
        {
            assertEquals(-0.3333333333, Solver.firstSolutionAfter(-0.5, 0, 3, 1), 0.000001, "solution should match");
        }
        catch (ValueRuntimeException exception)
        {
            fail("Caught unexpected exception");
        }
        result = Solver.solve(0, 3, 1);
        assertEquals(1, result.length, "length of result should be 1");
        assertEquals(-0.3333333333, result[0], 0.000001, "solution should match");
        // Quadratic equations
        result = Solver.solve(1, 0, 1);
        assertEquals(0, result.length, "length of result should be 0");
        try
        {
            Solver.firstSolutionAfter(0, 1, 0, 1);
            fail("Equation with no solutions should have thrown an exception");
        }
        catch (ValueRuntimeException exception)
        {
            // Ignore expected exception
        }
        result = Solver.solve(1, 0, 0);
        assertEquals(1, result.length, "length of result should be 1");
        assertEquals(0, result[0], 0.000001, "solution should match");
        try
        {
            assertEquals(0, Solver.firstSolutionAfter(-0.00001, 1, 0, 0), 0.000001, "solution should match");
        }
        catch (ValueRuntimeException exception)
        {
            fail("Caught unexpected exception");
        }
        result = Solver.solve(1, 0, -1);
        assertEquals(2, result.length, "length of result should be 2");
        assertEquals(-1, Math.min(result[0], result[1]), 0.000001, "Smallest solution should match");
        assertEquals(1, Math.max(result[0], result[1]), 0.000001, "Largest solution should match");
        try
        {
            assertEquals(-1, Solver.firstSolutionAfter(-10, 1, 0, -1), 0.000001, "First solution after -10 should be -1");
        }
        catch (ValueRuntimeException exception)
        {
            fail("Caught unexpected exception");
        }
        try
        {
            assertEquals(1, Solver.firstSolutionAfter(0, 1, 0, -1), 0.000001, "First solution after 0 should be 1");
        }
        catch (ValueRuntimeException exception)
        {
            fail("Caught unexpected exception");
        }
        try
        {
            Solver.firstSolutionAfter(2, 1, 0, -1);
            fail("First solution after 2 should have thrown an exception");
        }
        catch (ValueRuntimeException exception)
        {
            // Ignore expected exception
        }
        result = Solver.solve(1, 2, -3);
        // System.out.println("x0=" + result[0] + " x1=" + result[1]);
        assertEquals(2, result.length, "length of result should be 2");
        assertEquals(-3, Math.min(result[0], result[1]), 0.000001, "Smallest solution should match");
        assertEquals(1, Math.max(result[0], result[1]), 0.000001, "Largest solution should match");
        // System.out.println("solutions are " + Solver.solve(1, 2, -3)[0] + ", " + Solver.solve(1, 2, -3)[1]);
        try
        {
            assertEquals(-3, Solver.firstSolutionAfter(-10, 1, 2, -3), 0.000001, "First solution after -10 should be -3");
        }
        catch (ValueRuntimeException exception)
        {
            fail("Caught unexpected exception");
        }
        try
        {
            assertEquals(1, Solver.firstSolutionAfter(0, 1, 2, -3), 0.000001, "First solution after 0 should be 1");
        }
        catch (ValueRuntimeException exception)
        {
            fail("Caught unexpected exception");
        }
        try
        {
            Solver.firstSolutionAfter(2, 1, 2, -3);
            fail("First solution after 2 should have thrown an exception");
        }
        catch (ValueRuntimeException exception)
        {
            // Ignore expected exception
        }
        // Invert a (so the other branch of the if statement gets tested)
        // System.out.println("solutions are " + Solver.solve(-1, -2, 3)[0] + ", " + Solver.solve(-1, -2, 3)[1]);
        try
        {
            assertEquals(-3, Solver.firstSolutionAfter(-10, -1, -2, 3), 0.000001, "First solution after -10 should be -3");
        }
        catch (ValueRuntimeException exception)
        {
            fail("Caught unexpected exception");
        }
        try
        {
            assertEquals(1, Solver.firstSolutionAfter(0, -1, -2, 3), 0.000001, "First solution after 0 should be 1");
        }
        catch (ValueRuntimeException exception)
        {
            fail("Caught unexpected exception");
        }
        try
        {
            Solver.firstSolutionAfter(2, -1, -2, 3);
            fail("First solution after 2 should have thrown an exception");
        }
        catch (ValueRuntimeException exception)
        {
            // Ignore expected exception
        }

    }
}
