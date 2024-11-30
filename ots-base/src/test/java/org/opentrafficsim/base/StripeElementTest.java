package org.opentrafficsim.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Color;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.vector.LengthVector;
import org.djutils.exceptions.Try;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.base.StripeElement.StripeLateralSync;

/**
 * Test StripeElementTest.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class StripeElementTest
{

    /**
     * Tests StripeElement.
     */
    @Test
    public void testElement()
    {
        StripeElement el = StripeElement.gap(Length.ONE);
        assertEquals(1.0, el.width().si, 1e-6);
        assertNull(el.color());
        assertNull(el.dashes());
        assertTrue(el.isGap());
        assertFalse(el.isDashed());
        assertFalse(el.isContinuous());

        el = StripeElement.continuous(Length.ONE, Color.YELLOW);
        assertEquals(1.0, el.width().si, 1e-6);
        assertEquals(el.color(), Color.YELLOW);
        assertNull(el.dashes());
        assertFalse(el.isGap());
        assertFalse(el.isDashed());
        assertTrue(el.isContinuous());

        el = StripeElement.dashed(Length.ONE, Color.YELLOW, new LengthVector(new double[] {1.0, 2.0}));
        assertEquals(1.0, el.width().si, 1e-6);
        assertEquals(el.color(), Color.YELLOW);
        assertEquals(1.0, el.dashes().get(0).si, 1e-6);
        assertEquals(2.0, el.dashes().get(1).si, 1e-6);
        assertFalse(el.isGap());
        assertTrue(el.isDashed());
        assertFalse(el.isContinuous());

        // Meaningless combination
        Try.testFail(() -> new StripeElement(Length.ONE, null, new LengthVector(new double[] {1.0, 2.0})),
                IllegalArgumentException.class);

        assertFalse(StripeLateralSync.NONE.isLinkBased());
        assertTrue(StripeLateralSync.LINK.isLinkBased());
        assertTrue(StripeLateralSync.SNAP.isLinkBased());
    }

}
