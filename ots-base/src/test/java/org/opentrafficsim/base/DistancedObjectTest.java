package org.opentrafficsim.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.junit.jupiter.api.Test;

/**
 * Test for DistancedObject.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class DistancedObjectTest
{

    /**
     * Test constructor.
     */
    @Test
    final void constructorNull()
    {
        assertThrows(NullPointerException.class, () -> new DistancedObject<>(null, Length.ofSI(3.0)));
        assertThrows(NullPointerException.class, () -> new DistancedObject<>(new Object(), null));
    }

    /**
     * Test not equal by distance.
     */
    @Test
    final void compareDifferentDistances()
    {
        DistancedObject<Object> d1 = new DistancedObject<>(Duration.ofSI(3.0), Length.ofSI(3.0));
        DistancedObject<Object> d2 = new DistancedObject<>(Duration.ofSI(3.0), Length.ofSI(5.0));
        assertTrue(d1.compareTo(d2) < 0);
        assertTrue(d2.compareTo(d1) > 0);
    }

    /**
     * Test not equal by object.
     */
    @Test
    final void compareEqualDistanceUsesObject()
    {
        DistancedObject<Object> d1 = new DistancedObject<>(Duration.ofSI(3.0), Length.ofSI(3.0));
        DistancedObject<Object> d2 = new DistancedObject<>(Duration.ofSI(5.0), Length.ofSI(3.0));
        assertTrue(d1.compareTo(d2) < 0);
        assertTrue(d2.compareTo(d1) > 0);
    }

    /**
     * Test equal.
     */
    @Test
    final void compareEqualDistanceAndObject()
    {
        DistancedObject<Object> d1 = new DistancedObject<>(Duration.ofSI(3.0), Length.ofSI(3.0));
        DistancedObject<Object> d2 = new DistancedObject<>(Duration.ofSI(3.0), Length.ofSI(3.0));
        assertEquals(0, d1.compareTo(d2));
    }

}
