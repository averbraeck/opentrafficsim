package org.opentrafficsim.base.parameters;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.parameters.constraint.CollectionConstraint;
import org.opentrafficsim.base.parameters.constraint.MultiConstraint;

/**
 * Test for {@link MultiConstraint}.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
class MultiConstraintTest
{

    /**
     * Test multi-constraint.
     */
    @Test
    void testMultiConstraint()
    {

        CollectionConstraint<String> constraintA = new CollectionConstraint<>(Set.of("A", "B", "C", "D", "E"));
        CollectionConstraint<Object> constraintB = new CollectionConstraint<>(Set.of("C", "D", "E", "F", "G"));
        MultiConstraint<String> constraintC = new MultiConstraint<>(Set.of(constraintA, constraintB));

        // message when no accept() has yet been called, so the constraint does not know which sub-constraint supposedly failed
        assertTrue(constraintC.failMessage().contains("%s"));
        assertTrue(constraintC.accept("C"));
        assertFalse(constraintC.accept("A"));
        assertFalse(constraintC.accept("G"));
        assertFalse(constraintC.accept("Z"));
        assertTrue(constraintC.failMessage().contains("%s"));
        assertTrue(constraintC.failMessage().contains("likely")); // it now assumes some specific sub-constraint failed
        assertTrue(constraintC.toString().contains("MultiConstraint"));
        assertTrue(constraintC.toString().contains("2")); // it has two sub-constraints

    }

}
