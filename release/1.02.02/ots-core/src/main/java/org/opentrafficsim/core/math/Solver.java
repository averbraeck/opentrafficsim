package org.opentrafficsim.core.math;

import org.djunits.value.ValueException;

/**
 * Solvers for simple equations.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Dec 10, 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class Solver
{
    /**
     * 
     */
    private Solver()
    {
        // cannot be instantiated.
    }

    /**
     * Solve quadratic equation <cite>ax<sup>2</sup>+bx+c=0</cite> for <cite>x</cite>. Degenerate case <cite>a == 0</cite> is
     * allowed.
     * @param a double; the coefficient of <cite>x<sup>2</sup></cite>
     * @param b double; the coefficient of <cite>x</cite>
     * @param c double;
     * @return double[]; array with zero, one, or two elements (depending on the number of solutions of the equation)
     */
    public static double[] solve(final double a, final double b, final double c)
    {
        if (Math.abs(a) < 1E-8) // rounding errors will yield incorrect solutions if we allow very small a
        {
            // Degenerate; linear equation
            return solve(b, c);
        }
        // Quadratic equation
        double discriminant = b * b - 4 * a * c;
        if (discriminant < 0)
        {
            return new double[0];
        }
        if (0 == discriminant)
        {
            return new double[] {-b / 2 / a};
        }
        discriminant = Math.sqrt(discriminant);
        return new double[] {(-b + discriminant) / 2 / a, (-b - discriminant) / 2 / a};
    }

    /**
     * Solve a quadratic or linear equation and return the solution that is closest (but not less than) a boundary.
     * @param lowerBound double; minimum value of good solution
     * @param a double; quadratic coefficient
     * @param b double; linear coefficient
     * @param c double; value of the quadratic function for x==0
     * @return double; the solution that is closest (but not less than) a boundary
     * @throws ValueException if there is no acceptable solution
     */
    public static double firstSolutionAfter(final double lowerBound, final double a, final double b, final double c)
            throws ValueException
    {
        double[] solutions = solve(a, b, c);
        if (0 == solutions.length)
        {
            throw new ValueException("No solutions");
        }
        else if (1 == solutions.length)
        {
            if (solutions[0] >= lowerBound)
            {
                return solutions[0];
            }
            throw new ValueException("Only one solution and it is before lowerBound");
        }
        // Two solutions
        if (solutions[0] < lowerBound && solutions[1] < lowerBound)
        {
            throw new ValueException("Both solutions are before lowerBound");
        }
        if (solutions[0] < lowerBound)
        {
            return solutions[1];
        }
        if (solutions[1] < lowerBound)
        {
            return solutions[0];
        }
        return Math.min(solutions[0], solutions[1]);
    }

    /**
     * Solve linear equation <cite>ax+b=0</cite> for <cite>x</cite>.
     * @param a double; the coefficient of <cite>x</cite>
     * @param b double;
     * @return double[]; array with one or zero elements (depending on the number of solutions of the equation). The case where
     *         both <cite>a</cite> and <cite>b</cite> are zero returns an array of length 0.
     */
    public static double[] solve(final double a, final double b)
    {
        if (0 == a)
        {
            // Degenerate; no solution (or infinitely many solutions)
            return new double[0];
        }
        return new double[] {-b / a};
    }

}
