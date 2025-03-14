package org.opentrafficsim.core.geometry;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.djutils.exceptions.Try;
import org.junit.jupiter.api.Test;

/**
 * FractionalLengthDataTest test.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class FractionalLengthDataTest
{

    /** */
    private FractionalLengthDataTest()
    {
        // do not instantiate test class
    }

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
        assertEquals(data.apply(-1.0), 1.0, precision, "Fraction < 0.0 not extended");
        assertEquals(data.apply(2.0), 3.0, precision, "Fraction > 1.0 not extended");
        assertEquals(data.apply(0.1), 1.0, precision, "Fraction < smallest fraction not extended");
        assertEquals(data.apply(0.9), 3.0, precision, "Fraction > largest fraction not extended");
        assertEquals(data.apply(0.2), 1.0, 0.0, "Exact fraction, no exact value");
        assertEquals(data.apply(0.4), 2.0, 0.0, "Exact fraction, no exact value");
        assertEquals(data.apply(0.6), 2.0, 0.0, "Exact fraction, no exact value");
        assertEquals(data.apply(0.8), 3.0, 0.0, "Exact fraction, no exact value");
        assertEquals(data.apply(0.3), 1.5, precision, "Fraction not correctly interpolated");
        assertEquals(data.apply(0.5), 2.0, precision, "Fraction not correctly interpolated");
        assertEquals(data.apply(0.7), 2.5, precision, "Fraction not correctly interpolated");
    }

}
