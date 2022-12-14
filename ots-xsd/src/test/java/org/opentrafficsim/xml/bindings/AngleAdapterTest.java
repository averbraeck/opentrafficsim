package org.opentrafficsim.xml.bindings;

import static org.junit.Assert.assertEquals;

import javax.xml.bind.JAXBException;

import org.djunits.unit.AngleUnit;
import org.djunits.value.vdouble.scalar.Angle;
import org.junit.Test;

/**
 * AngleAdapterTest.java.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class AngleAdapterTest
{
    /**
     * Test the AngleAdapter
     */
    @Test
    public void testAngleAdapter() throws JAXBException
    {
        AngleAdapter angleAdapter = new AngleAdapter();

        assertEquals(new Angle(2.3, AngleUnit.DEGREE), angleAdapter.unmarshal("2.3 deg"));
        assertEquals(new Angle(2.3, AngleUnit.DEGREE), angleAdapter.unmarshal("+2.3 deg"));
        assertEquals(new Angle(2.3, AngleUnit.DEGREE), angleAdapter.unmarshal("2.3deg"));
        assertEquals(new Angle(2.3, AngleUnit.DEGREE), angleAdapter.unmarshal("+2.3deg"));
        assertEquals(new Angle(-2.3, AngleUnit.DEGREE), angleAdapter.unmarshal("-2.3 deg"));
        assertEquals(new Angle(-2.3, AngleUnit.DEGREE), angleAdapter.unmarshal("-2.3deg"));

        assertEquals(new Angle(2.3, AngleUnit.RADIAN), angleAdapter.unmarshal("2.3 rad"));
        assertEquals(new Angle(2.3, AngleUnit.RADIAN), angleAdapter.unmarshal("+2.3 rad"));
        assertEquals(new Angle(2.3, AngleUnit.RADIAN), angleAdapter.unmarshal("2.3rad"));
        assertEquals(new Angle(2.3, AngleUnit.RADIAN), angleAdapter.unmarshal("+2.3rad"));
        assertEquals(new Angle(-2.3, AngleUnit.RADIAN), angleAdapter.unmarshal("-2.3 rad"));
        assertEquals(new Angle(-2.3, AngleUnit.RADIAN), angleAdapter.unmarshal("-2.3rad"));

        assertEquals("2.3 deg", angleAdapter.marshal(new Angle(2.3, AngleUnit.DEGREE)));
        assertEquals("-2.3 deg", angleAdapter.marshal(new Angle(-2.3, AngleUnit.DEGREE)));
        assertEquals("2.3 rad", angleAdapter.marshal(new Angle(2.3, AngleUnit.RADIAN)));
        assertEquals("-2.3 rad", angleAdapter.marshal(new Angle(-2.3, AngleUnit.RADIAN)));

        assertEquals(new Angle(0.0, AngleUnit.DEGREE), angleAdapter.unmarshal("0.0 deg"));
        assertEquals(new Angle(0.0, AngleUnit.DEGREE), angleAdapter.unmarshal("+0.0 deg"));
        assertEquals(new Angle(0.0, AngleUnit.DEGREE), angleAdapter.unmarshal("0.0deg"));
        assertEquals(new Angle(0.0, AngleUnit.DEGREE), angleAdapter.unmarshal("+0.0deg"));
        assertEquals(new Angle(-0.0, AngleUnit.DEGREE), angleAdapter.unmarshal("-0.0 deg"));
        assertEquals(new Angle(-0.0, AngleUnit.DEGREE), angleAdapter.unmarshal("-0.0deg"));

        assertEquals(new Angle(0.0, AngleUnit.RADIAN), angleAdapter.unmarshal("0.0 rad"));
        assertEquals(new Angle(0.0, AngleUnit.RADIAN), angleAdapter.unmarshal("+0.0 rad"));
        assertEquals(new Angle(0.0, AngleUnit.RADIAN), angleAdapter.unmarshal("0.0rad"));
        assertEquals(new Angle(0.0, AngleUnit.RADIAN), angleAdapter.unmarshal("+0.0rad"));
        assertEquals(new Angle(-0.0, AngleUnit.RADIAN), angleAdapter.unmarshal("-0.0 rad"));
        assertEquals(new Angle(-0.0, AngleUnit.RADIAN), angleAdapter.unmarshal("-0.0rad"));

        assertEquals("0.0 deg", angleAdapter.marshal(new Angle(0.0, AngleUnit.DEGREE)));
        assertEquals("-0.0 deg", angleAdapter.marshal(new Angle(-0.0, AngleUnit.DEGREE)));
        assertEquals("0.0 rad", angleAdapter.marshal(new Angle(0.0, AngleUnit.RADIAN)));
        assertEquals("-0.0 rad", angleAdapter.marshal(new Angle(-0.0, AngleUnit.RADIAN)));

    }
}
