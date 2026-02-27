package org.opentrafficsim.base.parameters;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.djunits.value.vdouble.scalar.Duration;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.parameters.constraint.SingleBound;

/**
 * Test {@link SingleBound}.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
class SingleBoundConstraintTest
{

    /**
     * Test bound.
     */
    @Test
    void testBound()
    {

        // lower
        SingleBound<Double> bound = SingleBound.lowerInclusive(5.0);
        assertFalse(bound.accept(4.0));
        assertTrue(bound.accept(5.0));
        assertTrue(bound.accept(6.0));

        bound = SingleBound.lowerExclusive(5.0);
        assertFalse(bound.accept(4.0));
        assertFalse(bound.accept(5.0));
        assertTrue(bound.accept(6.0));

        SingleBound<Duration> typeBound = SingleBound.lowerInclusive(Duration.ofSI(5.0));
        assertFalse(typeBound.accept(Duration.ofSI(4.0)));
        assertTrue(typeBound.accept(Duration.ofSI(5.0)));
        assertTrue(typeBound.accept(Duration.ofSI(6.0)));

        typeBound = SingleBound.lowerExclusive(Duration.ofSI(5.0));
        assertFalse(typeBound.accept(Duration.ofSI(4.0)));
        assertFalse(typeBound.accept(Duration.ofSI(5.0)));
        assertTrue(typeBound.accept(Duration.ofSI(6.0)));

        // upper
        bound = SingleBound.upperInclusive(5.0);
        assertTrue(bound.accept(4.0));
        assertTrue(bound.accept(5.0));
        assertFalse(bound.accept(6.0));

        bound = SingleBound.upperExclusive(5.0);
        assertTrue(bound.accept(4.0));
        assertFalse(bound.accept(5.0));
        assertFalse(bound.accept(6.0));

        typeBound = SingleBound.upperInclusive(Duration.ofSI(5.0));
        assertTrue(typeBound.accept(Duration.ofSI(4.0)));
        assertTrue(typeBound.accept(Duration.ofSI(5.0)));
        assertFalse(typeBound.accept(Duration.ofSI(6.0)));

        typeBound = SingleBound.upperExclusive(Duration.ofSI(5.0));
        assertTrue(typeBound.accept(Duration.ofSI(4.0)));
        assertFalse(typeBound.accept(Duration.ofSI(5.0)));
        assertFalse(typeBound.accept(Duration.ofSI(6.0)));

        typeBound.toString().contains("SingleBound");

    }

}
