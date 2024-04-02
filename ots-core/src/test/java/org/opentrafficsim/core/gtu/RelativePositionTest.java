package org.opentrafficsim.core.gtu;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.junit.jupiter.api.Test;

/**
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version 13 jul. 2015 <br>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class RelativePositionTest
{

    /**
     * Test constructors and getters of RelativePosition.
     */
    @Test
    public final void relativePositionTest()
    {
        Length deltaX = new Length(12, LengthUnit.METER);
        Length deltaY = new Length(23, LengthUnit.METER);
        Length deltaZ = new Length(34, LengthUnit.METER);
        RelativePosition.TYPE type = new RelativePosition.TYPE("TestType");
        assertEquals(type.getName(), "TestType", "type name");
        assertTrue(type.equals(type), "type is equal to itself");
        assertFalse(type.equals(RelativePosition.REFERENCE), "type is not equals to REFERENCE");
        RelativePosition rp = new RelativePosition(deltaX, deltaY, deltaZ, type);
        assertTrue(deltaX.eq(rp.getDx()), "deltaX");
        assertTrue(deltaY.eq(rp.getDy()), "deltaY");
        assertTrue(deltaZ.eq(rp.getDz()), "deltaZ");
        assertEquals(type, rp.getType(), "type");

        RelativePosition rpCopy = new RelativePosition(rp);
        assertTrue(deltaX.eq(rpCopy.getDx()), "deltaX");
        assertTrue(deltaY.eq(rpCopy.getDy()), "deltaY");
        assertTrue(deltaZ.eq(rpCopy.getDz()), "deltaZ");
        assertEquals(type, rpCopy.getType(), "type");
        assertTrue(rp.equals(rpCopy), "equals");
        RelativePosition rp2 = new RelativePosition(deltaX, deltaY, deltaZ, type);
        assertTrue(rp.equals(rp2), "equals");
        Length deltaX2 = new Length(45, LengthUnit.METER);
        RelativePosition rp3 = new RelativePosition(deltaX2, deltaY, deltaZ, type);
        assertFalse(rp3.equals(rp), "different");
        assertTrue(rp.hashCode() != rp3.hashCode(), "hascode should differ with extreme likelihood");

        RelativePosition reference = RelativePosition.REFERENCE_POSITION;
        assertEquals(reference.getDx().getSI(), 0, 0.0000001, "reference is 0,0,0");
        assertEquals(reference.getDy().getSI(), 0, 0.0000001, "reference is 0,0,0");
        assertEquals(reference.getDz().getSI(), 0, 0.0000001, "reference is 0,0,0");
        assertEquals(reference.getType(), RelativePosition.REFERENCE, "reference has type REFERENCE_POSITION");
    }

}
