package org.opentrafficsim.core.gtu;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version13 jul. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class RelativePositionTest
{

    /**
     * Test constructors and getters of RelativePosition.
     */
    @Test
    public void relativePositionTest()
    {
        DoubleScalar.Rel<LengthUnit> deltaX = new DoubleScalar.Rel<LengthUnit>(12, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> deltaY = new DoubleScalar.Rel<LengthUnit>(23, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> deltaZ = new DoubleScalar.Rel<LengthUnit>(34, LengthUnit.METER);
        RelativePosition.TYPE type = new RelativePosition.TYPE("TestType");
        assertEquals("type name", type.getName(), "TestType");
        assertTrue("type is equal to itself", type.equals(type));
        assertFalse("type is not equals to REFERENCE", type.equals(RelativePosition.REFERENCE));
        RelativePosition rp = new RelativePosition(deltaX, deltaY, deltaZ, type);
        assertTrue("deltaX", deltaX.eq(rp.getDx()));
        assertTrue("deltaY", deltaY.eq(rp.getDy()));
        assertTrue("deltaZ", deltaZ.eq(rp.getDz()));
        assertEquals("type", type, rp.getType());

        RelativePosition rpCopy = new RelativePosition(rp);
        assertTrue("deltaX", deltaX.eq(rpCopy.getDx()));
        assertTrue("deltaY", deltaY.eq(rpCopy.getDy()));
        assertTrue("deltaZ", deltaZ.eq(rpCopy.getDz()));
        assertEquals("type", type, rpCopy.getType());
        assertTrue("equals", rp.equals(rpCopy));
        RelativePosition rp2 = new RelativePosition(deltaX, deltaY, deltaZ, type);
        assertTrue("equals", rp.equals(rp2));
        DoubleScalar.Rel<LengthUnit> deltaX2 = new DoubleScalar.Rel<LengthUnit>(45, LengthUnit.METER);
        RelativePosition rp3 = new RelativePosition(deltaX2, deltaY, deltaZ, type);
        assertFalse("different", rp3.equals(rp));
        assertTrue("hascode should differ with extreme likelihood", rp.hashCode() != rp3.hashCode());

        RelativePosition reference = RelativePosition.REFERENCE_POSITION;
        assertEquals("reference is 0,0,0", reference.getDx().getSI(), 0, 0.0000001);
        assertEquals("reference is 0,0,0", reference.getDy().getSI(), 0, 0.0000001);
        assertEquals("reference is 0,0,0", reference.getDz().getSI(), 0, 0.0000001);
        assertEquals("reference has type REFERENCE_POSITION", reference.getType(), RelativePosition.REFERENCE);
    }

}
