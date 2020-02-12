package org.opentrafficsim.core.math;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.junit.Test;

import nl.tudelft.simulation.jstats.streams.StreamException;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Test the Draw class.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Feb 12, 2020 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class DrawTest
{

    /**
     * Test the Draw class.
     */
    @Test
    public void drawTest()
    {
        try
        {
            Draw.drawWeighted(null, new FixedStream(0));
            fail("Should have thrown an exception");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }

        Map<Integer, Double> population = new LinkedHashMap<>();
        try
        {
            Draw.drawWeighted(population, null);
            fail("Should have thrown an exception");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }

        try
        {
            Draw.drawWeighted(population, new FixedStream(0));
        }
        catch (IllegalArgumentException iae)
        {
            // Ignore expected exception
        }

        population.put(0, 0d);
        try
        {
            Draw.drawWeighted(population, new FixedStream(0));
            fail("Should have thrown an exception");
        }
        catch (IllegalArgumentException iae)
        {
            // Ignore expected exception
        }

        population.put(1, -0.1);
        try
        {
            Draw.drawWeighted(population, new FixedStream(0));
            fail("Should have thrown an exception");
        }
        catch (IllegalArgumentException iae)
        {
            // Ignore expected exception
        }

        population.remove(1);
        population.put(1, 0.1);

        for (int step = 0; step < 100; step++)
        {
            assertEquals("result should be 1", 1, Draw.drawWeighted(population, new FixedStream(0.01 * step)), 0);
        }
        assertEquals("If rounding errors cause no element to be selected; last element with nonzero probability is returned", 1,
                Draw.drawWeighted(population, new FixedStream(1.01)), 0);
        population.put(2, 0.9);
        for (int step = 0; step < 100; step++)
        {
            int result = Draw.drawWeighted(population, new FixedStream(0.01 * step));
            if (step == 10)
            {
                assertTrue("result is either 1, or 2", result == 1 || result == 2);
            }
            else
            {
                int expected = step < 10 ? 1 : 2;
                assertEquals("result should be " + expected, expected, result, 0);
            }
        }
        population.clear();
        population.put(4, 2d);
        assertEquals("Result should be 4", 4, Draw.drawWeighted(population, new FixedStream(0)), 0);

        try
        {
            Draw.draw(null, new FixedStream(0));
            fail("null collection should have thrown an exception");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }

        Collection<Integer> options = new ArrayList<>();
        try
        {
            Draw.draw(options, null);
            fail("null stream should have thrown an exception");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }

        try
        {
            Draw.draw(options, new FixedStream(0));
            fail("empty collection should have thrown an exception");
        }
        catch (IllegalArgumentException iae)
        {
            // Ignore expected exception
        }
        options.add(0);
        assertEquals("result is 0", 0, Draw.draw(options, new FixedStream(0)), 0);
        options.clear();
        for (int i = 0; i < 10; i++)
        {
            options.add(i);
        }
        for (int step = 0; step < 100; step++)
        {
            int result = Draw.draw(options, new FixedStream(0.01 * step));
            int expected = step / 10;
            assertEquals("result should be " + expected, expected, result, 0);
        }
    }

    /**
     * Implementation of StreamInterface that returns a constant value.
     */
    static class FixedStream implements StreamInterface
    {
        /** ... */
        private static final long serialVersionUID = 1L;

        /** Fixed result value. */
        private double result;

        /**
         * Construct a new FixedStream.
         * @param result double; the result of the nextDouble method. All other methods return a suitable approximation of this
         *            value.
         */
        FixedStream(final double result)
        {
            this.result = result;
        }

        /** {@inheritDoc} */
        @Override
        public boolean nextBoolean()
        {
            return this.result != 0d;
        }

        /** {@inheritDoc} */
        @Override
        public double nextDouble()
        {
            return this.result;
        }

        /** {@inheritDoc} */
        @Override
        public float nextFloat()
        {
            return (float) this.result;
        }

        /** {@inheritDoc} */
        @Override
        public int nextInt()
        {
            return (int) this.result;
        }

        /** {@inheritDoc} */
        @Override
        public int nextInt(final int i, final int j)
        {
            return (int) this.result; // May not be in compliance with the specification
        }

        /** {@inheritDoc} */
        @Override
        public long nextLong()
        {
            return (long) this.result;
        }

        /** {@inheritDoc} */
        @Override
        public long getSeed()
        {
            return (long) this.result;
        }

        /** {@inheritDoc} */
        @Override
        public void setSeed(final long seed)
        {
            // Do nothing
        }

        /** {@inheritDoc} */
        @Override
        public void reset()
        {
            // Do nothing
        }

        /** {@inheritDoc} */
        @Override
        public byte[] saveState() throws StreamException
        {
            // Do nothing
            return null;
        }

        /** {@inheritDoc} */
        @Override
        public void restoreState(final byte[] state) throws StreamException
        {
            // Do nothing
        }

    }

}
