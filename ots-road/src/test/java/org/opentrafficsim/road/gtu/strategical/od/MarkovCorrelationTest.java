package org.opentrafficsim.road.gtu.strategical.od;

import static org.junit.jupiter.api.Assertions.fail;

import org.junit.jupiter.api.Test;
import org.opentrafficsim.road.gtu.generator.MarkovCorrelation;

import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class MarkovCorrelationTest
{

    /**
     * Tests the input tests.
     */
    @SuppressWarnings("unused")
    @Test
    public void testInput()
    {
        MarkovCorrelation<String, Double> markov = new MarkovCorrelation<>();

        // addState(state, correlation)
        // nulls
        testAddState(markov, null, 0.0, "Null state should fail.");
        // correlations
        double[] corr = new double[] {-2.0, -1.0, 1.0, 2.0};
        int n = 1;
        for (int i = 0; i < corr.length; i++)
        {
            testAddState(markov, "n" + n, corr[i], String.format("Correlation of %.2f should fail.", corr[i]));
            n++;
        }
        corr = new double[] {-0.99, -0.5, 0.0, 0.5, 0.99};
        String superState = "";
        for (int i = 0; i < corr.length; i++)
        {
            try
            {
                markov.addState("n" + n, corr[i]);
                if (superState.equals(""))
                {
                    superState = "n" + n; // for following test
                }
                n++;
            }
            catch (IllegalArgumentException ex)
            {
                fail(String.format("Correlation of %.2f should not fail.", corr[i]));
            }
        }

        // addState(previousState, state, correlation)
        // nulls
        testAddState(markov, null, "n" + n, 0.0, "Null super-state should fail.");
        n++;
        testAddState(markov, superState, null, 0.0, "Null state should fail."); // super exists
        n++;
        // correlations
        corr = new double[] {-2.0, -1.0, 1.0, 2.0};
        for (int i = 0; i < corr.length; i++)
        {
            testAddState(markov, superState, "n" + n, corr[i], String.format("Correlation of %.2f should fail.", corr[i]));
            n++;
        }
        corr = new double[] {0.11, 0.5, 0.99};
        for (int i = 0; i < corr.length; i++)
        {
            try
            {
                markov.addState(superState, "n" + n, corr[i]);
                n++;
            }
            catch (IllegalArgumentException ex)
            {
                fail(String.format("Correlation of %.2f should not fail.", corr[i]));
            }
        }
        // no existing super check
        testAddState(markov, "inexistent", "n" + n, 0.0, "Adding state with unspecified superStates should not work.");
        n++;

    }

    /**
     * Tests a specific case of addState().
     * @param markov markov
     * @param state state
     * @param correlation correlation
     * @param message message
     */
    private static void testAddState(final MarkovCorrelation<String, Double> markov, final String state,
            final double correlation, final String message)
    {
        try
        {
            markov.addState(state, correlation);
            fail(message);
        }
        catch (IllegalArgumentException | NullPointerException ex)
        {
            // expected
        }
    }

    /**
     * Tests a specific case of addState().
     * @param markov markov
     * @param previousState previous state
     * @param state state
     * @param correlation correlation
     * @param message message
     */
    private static void testAddState(final MarkovCorrelation<String, Double> markov, final String previousState,
            final String state, final double correlation, final String message)
    {
        try
        {
            markov.addState(previousState, state, correlation);
            fail(message);
        }
        catch (IllegalArgumentException | NullPointerException ex)
        {
            // expected
        }
    }

    /**
     * Tests whether the right probabilities result.
     */
    @Test
    public void probabilityTest()
    {

        // without correlation
        MarkovCorrelation<String, Double> markov = new MarkovCorrelation<>();
        String[] states = new String[] {"Car", "Bus", "Truck"};
        for (int i = 0; i < states.length; i++)
        {
            markov.addState(states[i], 0.0);
        }
        StreamInterface stream = new MersenneTwister(1L);
        int nTot = 1000000;
        Double[] ss = new Double[] {.7 * nTot, .2 * nTot, .1 * nTot}; // steady state
        for (int i = 0; i < states.length; i++)
        {
            int[] n = new int[states.length];
            for (int k = 0; k < nTot; k++)
            {
                String next = markov.drawState(states[i], states, ss, stream);
                for (int j = 0; j < states.length; j++)
                {
                    if (next.equals(states[j]))
                    {
                        n[j]++;
                    }
                }
            }
            for (int j = 0; j < states.length; j++)
            {
                int expected = (int) ss[j].doubleValue();
                // System.out.println("Generated " + n[j] + " vehicles, expected " + expected);
                if (Math.abs((((double) n[j]) / expected) - 1) > 0.01)
                {
                    fail("Number of new states generated of type " + states[j] + " with previous type " + states[i]
                            + " deviates by more than 1%.");
                }
            }
        }

        // with correlation
        markov = new MarkovCorrelation<>();
        double[] correlation = new double[] {0.2, 0.3, 0.8};
        for (int i = 0; i < states.length; i++)
        {
            markov.addState(states[i], correlation[i]);
        }
        for (int i = 0; i < states.length; i++)
        {
            int[] n = new int[states.length];
            for (int k = 0; k < nTot; k++)
            {
                String next = markov.drawState(states[i], states, ss, stream);
                for (int j = 0; j < states.length; j++)
                {
                    if (next.equals(states[j]))
                    {
                        n[j]++;
                    }
                }
            }
            double[] p = new double[states.length];
            double pSum = 0.0;
            for (int j = 0; j < states.length; j++)
            {
                if (j != i)
                {
                    // expected probabilities (not on the diagonal) are easily calculated by multiplying the steady-state
                    // proportion by (1 - c_i)*(1 - c_j)
                    p[j] = (ss[j] / nTot) * (1.0 - correlation[i]) * (1.0 - correlation[j]);
                    pSum += p[j];
                }
            }
            p[i] = 1.0 - pSum; // diagonal values make the rows sum to 1
            for (int j = 0; j < states.length; j++)
            {
                int expected = (int) (p[j] * nTot);
                // System.out.println("Generated " + n[j] + " vehicles, expected " + expected);
                if (Math.abs((((double) n[j]) / expected) - 1) > 0.02)
                {
                    fail("Number of new states generated of type " + states[j] + " with previous type " + states[i]
                            + " deviates by more than 2%.");
                }
            }
        }

        // with subgroup
        markov = new MarkovCorrelation<>();
        states = new String[] {"Car", "SlowVehicle", "Bus", "Truck"};
        ss = new Double[] {.6 * nTot, .2 * nTot, .15 * nTot, .05 * nTot};
        correlation = new double[] {0.2, 0.4, 0.7, 0.7};
        double[] correlation2 = new double[4];
        for (int i = 1; i < states.length; i++)
        {
            correlation2[i] = (correlation[i] - correlation[1]) / (1 - correlation[1]);
        }
        for (int i = 0; i < states.length; i++)
        {
            if (i <= 1)
            {
                markov.addState(states[i], correlation[i]);
            }
            else
            {
                markov.addState(states[1], states[i], correlation[i]);
            }
        }
        for (int i = 0; i < states.length; i++)
        {
            int[] n = new int[states.length];
            for (int k = 0; k < nTot; k++)
            {
                String next = markov.drawState(states[i], states, ss, stream);
                for (int j = 0; j < states.length; j++)
                {
                    if (next.equals(states[j]))
                    {
                        n[j]++;
                    }
                }
            }
            double[] p = new double[states.length];
            double pSum = 0.0;
            for (int j = 0; j < states.length; j++)
            {
                // with sub-groups this is kind of complex...
                if (i == 0)
                {
                    if (j != i)
                    {
                        p[j] = (1 - (ss[0] / nTot)) * (1 - correlation[0]) * (1 - correlation[1]) // probability in root matrix
                                * (ss[j] / nTot) / (1 - (ss[0] / nTot)); // normalized steady state in sub (i is not in group)
                        pSum += p[j];
                    }
                }
                else
                {
                    if (j == 0)
                    {
                        p[j] = (ss[0] / nTot) * (1 - correlation[0]) * (1 - correlation[1]); // probability in root matrix
                        // no additional probability, root says: from 'group' to i (not group), so group is not even used here
                    }
                    else
                    {
                        if (j != i)
                        {
                            p[j] = (1 - (ss[0] / nTot) * (1 - correlation[0]) * (1 - correlation[1])) // probability in root
                                    * ((ss[j] / nTot) / (1 - (ss[0] / nTot))) // base probability in sub matrix
                                    * (1 - correlation2[i]) * (1 - correlation2[j]); // correlation factors in sub matrix
                        }
                    }
                    pSum += p[j];
                }
            }
            p[i] = 1.0 - pSum; // diagonal values make the rows sum to 1
            for (int j = 0; j < states.length; j++)
            {
                int expected = (int) (p[j] * nTot);
                // System.out.println("Generated " + n[j] + " vehicles, expected " + expected);
                if (Math.abs((((double) n[j]) / expected) - 1) > 0.02)
                {
                    fail("Number of new states generated of type " + states[j] + " with previous type " + states[i]
                            + " deviates by more than 2%.");
                }
            }
        }

    }
}
