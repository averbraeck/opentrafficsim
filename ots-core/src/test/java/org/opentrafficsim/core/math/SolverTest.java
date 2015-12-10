package org.opentrafficsim.core.math;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test the Solver class.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Dec 10, 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SolverTest
{

    /**
     * Linear solver.
     */
    @Test
    public void linearTest()
    {
        // Linear equations
        double[] result = Solver.solve(0, 0);
        assertEquals("length of result should be 0", 0, result.length);
        result = Solver.solve(0, 0, 0);
        assertEquals("length of result should be 0", 0, result.length);
        result = Solver.solve(0,  1);
        assertEquals("length of result should be 0", 0, result.length);
        result = Solver.solve(0, 0,  1);
        assertEquals("length of result should be 0", 0, result.length);
        result = Solver.solve(3, 1);
        assertEquals("length of result should be 1", 1, result.length);
        assertEquals("solution should match", -0.3333333333, result[0], 0.000001);
        result = Solver.solve(0, 3, 1);
        assertEquals("length of result should be 1", 1, result.length);
        assertEquals("solution should match", -0.3333333333, result[0], 0.000001);
        // Quadratic equations
        result = Solver.solve(1, 0, 1);
        assertEquals("length of result should be 0", 0, result.length);
        result = Solver.solve(1, 0, 0);
        assertEquals("length of result should be 1", 1, result.length);
        assertEquals("solution should match", 0, result[0], 0.000001);
        result = Solver.solve(1, 0, -1);
        assertEquals("length of result should be 2", 2, result.length);
        assertEquals("Smallest solution should match", -1, Math.min(result[0], result[1]), 0.000001);
        assertEquals("Largest solution should match", 1, Math.max(result[0], result[1]), 0.000001);
        result = Solver.solve(1, 2, -3);
        //System.out.println("x0=" + result[0] + " x1=" + result[1]);
        assertEquals("length of result should be 2", 2, result.length);
        assertEquals("Smallest solution should match", -3, Math.min(result[0], result[1]), 0.000001);
        assertEquals("Largest solution should match", 1, Math.max(result[0], result[1]), 0.000001);
        
        
        
        
        
    }
}
