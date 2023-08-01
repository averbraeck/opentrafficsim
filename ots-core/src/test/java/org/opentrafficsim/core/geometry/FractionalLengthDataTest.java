package org.opentrafficsim.core.geometry;

import static org.junit.Assert.assertEquals;

import org.djutils.exceptions.Try;
import org.junit.Test;

/**
 * FractionalLengthDataTest test.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class FractionalLengthDataTest
{

    /**
     * FractionalLengthDataTest test.
     */
    @Test
    public void testFractionalLengthData()
    {
        FractionalLengthData data;
        
        data = Try.testFail(() -> FractionalLengthData.of(0.5), "At least 2 values", IllegalArgumentException.class);
        data = Try.testFail(() -> FractionalLengthData.of(-0.1, 1.0), "No fractions < 0", IllegalArgumentException.class);
        data = Try.testFail(() -> FractionalLengthData.of(1.1, 1.0), "No fractions > 1", IllegalArgumentException.class);
        data = Try.testFail(() -> FractionalLengthData.of(0.5, 1.0, 0.7), "No uneven inputs", IllegalArgumentException.class);
        
        data = FractionalLengthData.of(0.2, 1.0, 0.4, 2.0, 0.6, 2.0, 0.8, 3.0);
        double precision = 1e-9; 
        assertEquals("Fraction < 0.0 not extended", data.get(-1.0), 1.0, precision);
        assertEquals("Fraction > 1.0 not extended", data.get(2.0), 3.0, precision);
        assertEquals("Fraction < smallest fraction not extended", data.get(0.1), 1.0, precision);
        assertEquals("Fraction > largest fraction not extended", data.get(0.9), 3.0, precision);
        assertEquals("Exact fraction, no exact value", data.get(0.2), 1.0, 0.0);
        assertEquals("Exact fraction, no exact value", data.get(0.4), 2.0, 0.0);
        assertEquals("Exact fraction, no exact value", data.get(0.6), 2.0, 0.0);
        assertEquals("Exact fraction, no exact value", data.get(0.8), 3.0, 0.0);
        assertEquals("Fraction not correctly interpolated", data.get(0.3), 1.5, precision);
        assertEquals("Fraction not correctly interpolated", data.get(0.5), 2.0, precision);
        assertEquals("Fraction not correctly interpolated", data.get(0.7), 2.5, precision);
    }
    
}
