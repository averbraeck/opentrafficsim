package org.opentrafficsim.core.distributions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.Test;
import org.opentrafficsim.core.distributions.Distribution.FrequencyAndObject;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Test the Distribution class.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Mar 4, 2016 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class DistributionTest
{
    /**
     * Test the Distribution class.
     * @throws ProbabilityException the test fails if this happens uncaught
     */
    @Test
    @SuppressFBWarnings("NP_NULL_PARAM_DEREF_NONVIRTUAL")
    public final void distributionTest() throws ProbabilityException
    {
        StreamInterface si = new MersenneTwister(1234);
        try
        {
            new Distribution<TestObject>(null, si);
            fail("Null pointer for generators should have thrown a ProbabilityException");
        }
        catch (ProbabilityException npe)
        {
            // Ignore expected exception
        }
        List<Distribution.FrequencyAndObject<TestObject>> generators =
                new ArrayList<Distribution.FrequencyAndObject<TestObject>>();
        try
        {
            new Distribution<TestObject>(generators, null);
            fail("Null pointer for stream interface should have thrown a ProbabilityException");
        }
        catch (ProbabilityException npe)
        {
            // Ignore expected exception
        }
        Distribution<TestObject> dist = new Distribution<TestObject>(generators, si);
        assertEquals("size should be 0", 0, dist.size());
        try
        {
            dist.draw();
            fail("draw with empty set should have thrown a ProbabilityException");
        }
        catch (ProbabilityException pe)
        {
            // Ignore expected exception
        }
        TestObject to = new TestObject();
        Distribution.FrequencyAndObject<TestObject> generator =
                new Distribution.FrequencyAndObject<DistributionTest.TestObject>(123, to);
        try
        {
            dist.add(1, generator);
            fail("should have thrown an IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException e)
        {
            // Ignore expected exception
        }
        try
        {
            dist.add(-1, generator);
            fail("should have thrown an IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException e)
        {
            // Ignore expected exception
        }
        dist.add(0, generator);
        assertEquals("size should now be 1", 1, dist.size());
        dist.remove(0);
        assertEquals("size should be 0", 0, dist.size());
        dist.add(generator);
        assertEquals("size should now be 1", 1, dist.size());
        try
        {
            dist.remove(1);
            fail("Bad index for remove should have thrown an IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException e)
        {
            // Ignore expected exception
        }
        try
        {
            dist.remove(-1);
            fail("Bad index for remove should have thrown an IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException e)
        {
            // Ignore expected exception
        }
        for (int i = 0; i < 1000; i++)
        {
            TestObject to2 = dist.draw();
            assertEquals("Result of draw() should be equal to to", to, to2);
        }
        try
        {
            dist.modifyFrequency(1, 1);
            fail("Bad index for modify should have thrown an IndexOutOfBoundsException");
        }
        catch (ProbabilityException e)
        {
            // Ignore expected exception
        }
        try
        {
            dist.modifyFrequency(-1, 1);
            fail("Bad index for modify should have thrown an IndexOutOfBoundsException");
        }
        catch (ProbabilityException e)
        {
            // Ignore expected exception
        }
        try
        {
            dist.modifyFrequency(0, -1);
            fail("Bad frequency for modify should have thrown a ProbabilityException");
        }
        catch (ProbabilityException pe)
        {
            // Ignore expected exception
        }
        dist.modifyFrequency(0, 0);
        try
        {
            dist.draw();
            fail("Sum of frequencies == 0 should have thrown a ProbabilityException");
        }
        catch (ProbabilityException pe)
        {
            // Ignore expected exception
        }
        TestObject to2 = new TestObject();
        dist.add(new Distribution.FrequencyAndObject<TestObject>(10, to2));
        assertEquals("element 0 should be to", to, dist.get(0).getObject());
        assertEquals("element 1 should be to2", to2, dist.get(1).getObject());
        assertEquals("frequency of element 0 should be 0", 0, dist.get(0).getFrequency(), 0.00001);
        assertEquals("frequency of element 1 should be 10", 10, dist.get(1).getFrequency(), 0.00001);
        for (int i = 0; i < 1000; i++)
        {
            TestObject to3 = dist.draw();
            assertEquals("Result of draw() should be equal to to2", to2, to3);
        }
        dist.modifyFrequency(0, 5);
        int[] observed = new int[2];
        for (int i = 0; i < 10000; i++)
        {
            TestObject to3 = dist.draw();
            if (to3.equals(to))
            {
                observed[0]++;
            }
            else if (to3.equals(to2))
            {
                observed[1]++;
            }
            else
            {
                fail("draw returned something we didn't add");
            }
        }
        // With the seeded MersenneTwister observed contains the values 3385 and 6615
        // System.out.println("Observed frequencies: [" + observed[0] + ", " + observed[1] + "]");
        assertEquals("Total number of draws should add up to 10000", 10000, observed[0] + observed[1]);
        assertTrue("observed frequency of to should be about 3333", 2500 < observed[0] && observed[0] < 4000);
        dist.clear();
        assertEquals("after clear the set should be empty", 0, dist.size());
        try
        {
            dist.draw();
            fail("Empty set should throw a ProbabilityException");
        }
        catch (ProbabilityException pe)
        {
            // Ignore expected exception
        }
        // Construct a Distribution from a List of generators
        generators = new ArrayList<Distribution.FrequencyAndObject<TestObject>>();
        generators.add(new FrequencyAndObject<DistributionTest.TestObject>(123, to));
        generators.add(new FrequencyAndObject<DistributionTest.TestObject>(456, to2));
        dist = new Distribution<TestObject>(generators, si);
        assertEquals("element 0 should be to", to, dist.get(0).getObject());
        assertEquals("element 1 should be to2", to2, dist.get(1).getObject());
        assertEquals("frequency of element 0 should be 123", 123, dist.get(0).getFrequency(), 0.00001);
        assertEquals("frequency of element 1 should be 456", 456, dist.get(1).getFrequency(), 0.00001);
        generators.set(1, new FrequencyAndObject<DistributionTest.TestObject>(-1, to2));
        try
        {
            new Distribution<TestObject>(generators, si);
            fail("Negative frequency should have thrown a ProbabilityException");
        }
        catch (ProbabilityException pe)
        {
            // Ignore expected exception
        }
        try
        {
            dist.add(generators.get(1));
            fail("Negative frequency should have thrown a ProbabilityException");
        }
        catch (ProbabilityException pe)
        {
            // Ignore expected exception
        }
        try
        {
            dist.set(-1, new FrequencyAndObject<DistributionTest.TestObject>(456, to2));
        }
        catch (ProbabilityException pe)
        {
            // Ignore expected exception
        }
        try
        {
            dist.set(dist.size(), new FrequencyAndObject<DistributionTest.TestObject>(456, to2));
        }
        catch (ProbabilityException pe)
        {
            // Ignore expected exception
        }
        // Modify the internal value "cumulativeTotal" to increase the odds that Distribution resorts to returning the first
        // non-zero frequency object
        double badTotal = 1000;
        try
        {
            Field field = dist.getClass().getDeclaredField("cumulativeTotal");
            field.setAccessible(true);
            field.setDouble(dist, badTotal);
        }
        catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException exception)
        {
            exception.printStackTrace();
        }
        observed = new int[2];
        for (int i = 0; i < 10000; i++)
        {
            TestObject to3 = dist.draw();
            if (to3.equals(to))
            {
                observed[0]++;
            }
            else if (to3.equals(to2))
            {
                observed[1]++;
            }
            else
            {
                fail("draw returned something we didn't add");
            }
        }
        // System.out.println("dist is " + dist);
        double badFrequency = badTotal - dist.get(0).getFrequency() - dist.get(1).getFrequency();
        double expectedIn0 = (dist.get(0).getFrequency() + badFrequency) / badTotal * 10000;
        // System.out.println("Observed frequencies: [" + observed[0] + ", " + observed[1] + "]; expected in 0 " + expectedIn0);
        assertEquals("Total number of draws should add up to 10000", 10000, observed[0] + observed[1]);
        assertTrue("observed frequency of to should be about " + expectedIn0,
                expectedIn0 - 500 < observed[0] && observed[0] < expectedIn0 + 500);
        // When frequency of element 0 is 0; the draw method should never return it; even if the cumulativeTotal is wrong
        dist.modifyFrequency(0, 0);
        try
        {
            Field field = dist.getClass().getDeclaredField("cumulativeTotal");
            field.setAccessible(true);
            field.setDouble(dist, badTotal);
        }
        catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException exception)
        {
            exception.printStackTrace();
        }
        observed = new int[2];
        for (int i = 0; i < 10000; i++)
        {
            TestObject to3 = dist.draw();
            if (to3.equals(to))
            {
                observed[0]++;
            }
            else if (to3.equals(to2))
            {
                observed[1]++;
            }
            else
            {
                fail("draw returned something we didn't add");
            }
        }
        // System.out.println("dist is " + dist);
        // System.out.println("Observed frequencies: [" + observed[0] + ", " + observed[1] + "]; expected in 0 " + expectedIn0);
        assertEquals("Total number of draws should add up to 10000", 10000, observed[0] + observed[1]);
        assertEquals("observed frequency of to should be 0", 0, observed[0]);
        try
        {
            dist.modifyFrequency(dist.size(), 0);
        }
        catch (ProbabilityException pe)
        {
            // Ignore expected exception
        }
        try
        {
            dist.modifyFrequency(-1, 0);
        }
        catch (ProbabilityException pe)
        {
            // Ignore expected exception
        }
        // Execute the clear method and verify that size is 0
        dist.clear();
        assertEquals("after clear the set should be empty", 0, dist.size());
    }

    /** Object used as generic parameter. */
    class TestObject
    {
        //
    }

}
