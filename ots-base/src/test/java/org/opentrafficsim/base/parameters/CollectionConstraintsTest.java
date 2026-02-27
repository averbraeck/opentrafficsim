package org.opentrafficsim.base.parameters;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Set;

import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.parameters.constraint.ClassCollectionConstraint;
import org.opentrafficsim.base.parameters.constraint.CollectionConstraint;
import org.opentrafficsim.base.parameters.constraint.SubCollectionConstraint;

/**
 * Test for {@link CollectionConstraint}, {@link SubCollectionConstraint} and {@link ClassCollectionConstraint}.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
class CollectionConstraintsTest
{

    /**
     * Test collection constraints.
     */
    @Test
    void testCollectionsConstraints()
    {

        CollectionConstraint<String> constraintA = new CollectionConstraint<>(Set.of("A", "B", "C", "D", "E"));
        assertTrue(constraintA.accept("A"));
        assertFalse(constraintA.accept("F"));
        assertTrue(constraintA.failMessage().contains("%s"));
        assertTrue(constraintA.toString().contains("CollectionConstraint"));
        assertTrue(constraintA.toString().contains("B"));

        SubCollectionConstraint<String> constraintB = new SubCollectionConstraint<>(Set.of("A", "B", "C", "D", "E"));
        assertTrue(constraintB.accept(Set.of("A")));
        assertTrue(constraintB.accept(Set.of("A", "C", "D")));
        assertTrue(constraintB.accept(Set.of("A", "B", "C", "D", "E")));
        assertFalse(constraintB.accept(Set.of("F")));
        assertFalse(constraintB.accept(Set.of("A", "C", "F")));
        assertTrue(constraintB.failMessage().contains("%s"));
        assertTrue(constraintB.toString().contains("SubCollectionConstraint"));
        assertTrue(constraintB.toString().contains("B"));

        ClassCollectionConstraint<Number> constraintC =
                new ClassCollectionConstraint<>(Set.of(Double.class, Float.class, Integer.class));
        assertTrue(constraintC.accept(Set.of(Double.class)));
        assertTrue(constraintC.accept(Set.of(Double.class, Float.class)));
        assertTrue(constraintC.accept(Set.of(Double.class, Float.class, Integer.class)));
        assertFalse(constraintC.accept(Set.of(Byte.class)));
        assertFalse(constraintC.accept(Set.of(Double.class, Float.class, Byte.class)));
        assertTrue(constraintC.failMessage().contains("%s"));
        assertTrue(constraintC.toString().contains("ClassCollectionConstraint"));
        assertTrue(constraintC.toString().contains("class java.lang.Float"));

    }

}
