package org.opentrafficsim.core.distributions;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import nl.tudelft.simulation.jstats.streams.MersenneTwister;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Test the Distribution class.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 */
public final class DistributionTest
{

    /** */
    private DistributionTest()
    {
        // do not instantiate test class
    }

    /**
     * Test the Distribution class.
     */
    @Test
    public void distributionTest()
    {
        StreamInterface si = new MersenneTwister(1234);
        try
        {
            new ObjectDistribution<TestObject>(null, si);
            fail("Null pointer for generators should have thrown a NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }

        List<FrequencyAndObject<TestObject>> generators = new ArrayList<FrequencyAndObject<TestObject>>();
        try
        {
            new ObjectDistribution<TestObject>(generators, null);
            fail("Null pointer for stream interface should have thrown a NullPointerException");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }

        ObjectDistribution<TestObject> dist = new ObjectDistribution<TestObject>(generators, si);
        assertEquals(0, dist.size(), "size should be 0");
        try
        {
            dist.get();
            fail("draw with empty set should have thrown a IllegalStateException");
        }
        catch (IllegalStateException pe)
        {
            // Ignore expected exception
        }

        TestObject to = new TestObject();
        FrequencyAndObject<TestObject> generator = new FrequencyAndObject<DistributionTest.TestObject>(123, to);
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
        assertEquals(1, dist.size(), "size should now be 1");
        dist.remove(0);
        assertEquals(0, dist.size(), "size should be 0");
        dist.add(generator);
        assertEquals(1, dist.size(), "size should now be 1");
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
            TestObject to2 = dist.get();
            assertEquals(to, to2, "Result of draw() should be equal to to");
        }
        try
        {
            dist.modifyFrequency(1, 1);
            fail("Bad index for modify should have thrown an IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException e)
        {
            // Ignore expected exception
        }

        try
        {
            dist.modifyFrequency(-1, 1);
            fail("Bad index for modify should have thrown an IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException e)
        {
            // Ignore expected exception
        }

        try
        {
            dist.modifyFrequency(0, -1);
            fail("Bad frequency for modify should have thrown a IllegalArgumentException");
        }
        catch (IllegalArgumentException pe)
        {
            // Ignore expected exception
        }

        dist.modifyFrequency(0, 0);
        try
        {
            dist.get();
            fail("Sum of frequencies == 0 should have thrown a IllegalStateException");
        }
        catch (IllegalStateException pe)
        {
            // Ignore expected exception
        }

        TestObject to2 = new TestObject();
        dist.add(new FrequencyAndObject<TestObject>(10, to2));
        assertEquals(to, dist.get(0).object(), "element 0 should be to");
        assertEquals(to2, dist.get(1).object(), "element 1 should be to2");
        assertEquals(0, dist.get(0).frequency(), 0.00001, "frequency of element 0 should be 0");
        assertEquals(10, dist.get(1).frequency(), 0.00001, "frequency of element 1 should be 10");
        for (int i = 0; i < 1000; i++)
        {
            TestObject to3 = dist.get();
            assertEquals(to2, to3, "Result of draw() should be equal to to2");
        }
        try
        {
            dist.get(-1);
            fail("Negative index should have thrown in a IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException pe)
        {
            // Ignore expected exception
        }

        try
        {
            dist.get(2);
            fail("Too high index should have thrown in a IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException pe)
        {
            // Ignore expected exception
        }

        dist.modifyFrequency(0, 5);
        int[] observed = new int[2];
        for (int i = 0; i < 10000; i++)
        {
            TestObject to3 = dist.get();
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
        assertEquals(10000, observed[0] + observed[1], "Total number of draws should add up to 10000");
        assertTrue(2500 < observed[0] && observed[0] < 4000, "observed frequency of to should be about 3333");
        dist.clear();
        assertEquals(0, dist.size(), "after clear the set should be empty");
        try
        {
            dist.get();
            fail("Empty set should throw a IllegalStateException");
        }
        catch (IllegalStateException pe)
        {
            // Ignore expected exception
        }
        // Construct a Distribution from a List of generators
        generators = new ArrayList<FrequencyAndObject<TestObject>>();
        generators.add(new FrequencyAndObject<DistributionTest.TestObject>(123, to));
        generators.add(new FrequencyAndObject<DistributionTest.TestObject>(456, to2));
        dist = new ObjectDistribution<TestObject>(generators, si);
        assertEquals(to, dist.get(0).object(), "element 0 should be to");
        assertEquals(to2, dist.get(1).object(), "element 1 should be to2");
        assertEquals(123, dist.get(0).frequency(), 0.00001, "frequency of element 0 should be 123");
        assertEquals(456, dist.get(1).frequency(), 0.00001, "frequency of element 1 should be 456");
        try
        {
            new FrequencyAndObject<DistributionTest.TestObject>(-1, to2);
            fail("Negative frequency should have thrown a IllegalArgumentException");
        }
        catch (IllegalArgumentException pe)
        {
            // Ignore expected exception
        }

        try
        {
            dist.set(-1, new FrequencyAndObject<DistributionTest.TestObject>(456, to2));
        }
        catch (IndexOutOfBoundsException pe)
        {
            // Ignore expected exception
        }

        try
        {
            dist.set(dist.size(), new FrequencyAndObject<DistributionTest.TestObject>(456, to2));
        }
        catch (IndexOutOfBoundsException pe)
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
            TestObject to3 = dist.get();
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
        double badFrequency = badTotal - dist.get(0).frequency() - dist.get(1).frequency();
        double expectedIn0 = (dist.get(0).frequency() + badFrequency) / badTotal * 10000;
        // System.out.println("Observed frequencies: [" + observed[0] + ", " + observed[1] + "]; expected in 0 " + expectedIn0);
        assertEquals(10000, observed[0] + observed[1], "Total number of draws should add up to 10000");
        assertTrue(expectedIn0 - 500 < observed[0] && observed[0] < expectedIn0 + 500,
                "observed frequency of to should be about " + expectedIn0);
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
            TestObject to3 = dist.get();
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
        assertEquals(10000, observed[0] + observed[1], "Total number of draws should add up to 10000");
        assertEquals(0, observed[0], "observed frequency of to should be 0");
        try
        {
            dist.modifyFrequency(0, -0.01);
        }
        catch (IllegalArgumentException pe)
        {
            // Ignore expected exception
        }

        try
        {
            dist.modifyFrequency(-1, 0);
        }
        catch (IndexOutOfBoundsException pe)
        {
            // Ignore expected exception
        }

        // Execute the clear method and verify that size is 0
        dist.clear();
        assertEquals(0, dist.size(), "after clear the set should be empty");
    }

    /** Object used as generic parameter. */
    class TestObject
    {
        /**
         * Constructor.
         */
        TestObject()
        {
            //
        }
    }

    /**
     * Test hashCode and equals.
     */
    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void testHashCodeAndEquals()
    {
        StreamInterface si = new MersenneTwister(1234);
        ObjectDistribution<Double> distribution = new ObjectDistribution<>(si);
        distribution.add(new FrequencyAndObject<Double>(Math.PI, 10d));
        distribution.add(new FrequencyAndObject<Double>(2 * Math.PI, 20d));
        assertTrue(distribution.equals(distribution), "distribution is equal to itself");
        assertFalse(distribution.equals(null), "distribution is not equal to null");
        assertFalse(distribution.equals("junk"), "distribution is not equal to something totally different");
        ObjectDistribution<Double> distribution2 = new ObjectDistribution<>(si);
        assertFalse(distribution.equals(distribution2),
                "Distribution is not equal to other distribution containing different set of frequency and object");
        assertNotEquals(distribution.hashCode(), distribution2.hashCode());
        distribution2.add(new FrequencyAndObject<Double>(Math.PI, 10d));
        distribution2.add(new FrequencyAndObject<Double>(2 * Math.PI, 20d));
        assertTrue(distribution.equals(distribution2),
                "Distributions is equal to other distribution containing exact same frequencies and objects and random source");
        assertEquals(distribution.hashCode(), distribution2.hashCode());
        assertTrue(distribution.toString().startsWith("Distribution"), "The toString method returns something descriptive");
    }

    /**
     * Test the FrequencyAndObject sub class.
     */
    @SuppressWarnings("unlikely-arg-type")
    @Test
    public void frequencyAndObjectTest()
    {
        FrequencyAndObject<String> fao1 = new FrequencyAndObject<>(Math.PI, "One");
        assertEquals(Math.PI, fao1.frequency(), 0, "frequency matches");
        assertEquals("One", fao1.object(), "object matchs");
        assertTrue(fao1.equals(fao1), "FreqencyAndObject is equal to itself");
        assertFalse(fao1.equals(null), "FrequencyAndObject is not equal to null");
        assertFalse(fao1.equals("Bla"), "FrequencyAndObject is not equal to some totally unrelated object");
        FrequencyAndObject<String> fao2 = new FrequencyAndObject<>(Math.PI, "One");
        assertTrue(fao1.equals(fao2),
                "FrequencyAndObject is equal to another FrequencyAndObject with same frequency and same object");
        assertEquals(fao1.hashCode(), fao2.hashCode(),
                "FrequencyAndObject has same hashCode as another FrequencyAndObject with same frequency and same object");
        fao2 = new FrequencyAndObject<>(Math.PI, "Two");
        assertFalse(fao1.equals(fao2),
                "FrequencyAndObject is not equal to another FrequencyAndObject with same frequency but other object");
        fao2 = new FrequencyAndObject<>(Math.E, "One");
        assertFalse(fao1.equals(fao2),
                "FrequencyAndObject is not equal to another FrequencyAndObject with different frequency but same object");
        assertNotEquals(fao1.hashCode(), fao2.hashCode(),
                "FrequencyAndObject has different hashCode than another FrequencyAndObject with different frequency "
                        + "and same object");
        fao2 = new FrequencyAndObject<>(Math.PI, null);
        assertFalse(fao1.equals(fao2),
                "FrequencyAndObject is not equal to another FrequencyAndObject with same frequency but null object");
        assertFalse(fao2.equals(fao1),
                "FrequencyAndObject is not equal to another FrequencyAndObject with same frequency but null object");
        assertTrue(fao1.toString().startsWith("FrequencyAndObject"), "The toString methods returns something descriptive");
    }

}
