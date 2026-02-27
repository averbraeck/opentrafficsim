package org.opentrafficsim.base.parameters;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.parameters.constraint.NumericConstraint;

/**
 * Tests {@link NumericConstraint}.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
class NumericConstraintTest
{

    /**
     * Test numeric constraints.
     */
    @Test
    void testNumericConstraint()
    {
        for (NumericConstraint constraint : NumericConstraint.values())
        {
            assertNotNull(constraint.failMessage());
            assertTrue(constraint.failMessage().contains("%s"));
        }

        assertTrue(NumericConstraint.POSITIVE.accept(1.0));
        assertFalse(NumericConstraint.POSITIVE.accept(0.0));
        assertFalse(NumericConstraint.POSITIVE.accept(-1.0));

        assertTrue(NumericConstraint.POSITIVEZERO.accept(1.0));
        assertTrue(NumericConstraint.POSITIVEZERO.accept(0.0));
        assertFalse(NumericConstraint.POSITIVEZERO.accept(-1.0));

        assertFalse(NumericConstraint.NEGATIVE.accept(1.0));
        assertFalse(NumericConstraint.NEGATIVE.accept(0.0));
        assertTrue(NumericConstraint.NEGATIVE.accept(-1.0));

        assertFalse(NumericConstraint.NEGATIVEZERO.accept(1.0));
        assertTrue(NumericConstraint.NEGATIVEZERO.accept(0.0));
        assertTrue(NumericConstraint.NEGATIVEZERO.accept(-1.0));

        assertTrue(NumericConstraint.NONZERO.accept(1.0));
        assertFalse(NumericConstraint.NONZERO.accept(0.0));
        assertTrue(NumericConstraint.NONZERO.accept(-1.0));

        assertTrue(NumericConstraint.ATLEASTONE.accept(2.0));
        assertTrue(NumericConstraint.ATLEASTONE.accept(1.0));
        assertFalse(NumericConstraint.ATLEASTONE.accept(0.5));
        assertFalse(NumericConstraint.ATLEASTONE.accept(0.0));
        assertFalse(NumericConstraint.ATLEASTONE.accept(-1.0));

    }

}
