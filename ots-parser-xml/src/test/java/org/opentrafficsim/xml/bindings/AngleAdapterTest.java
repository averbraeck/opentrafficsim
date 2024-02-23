package org.opentrafficsim.xml.bindings;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import javax.xml.bind.JAXBException;

import org.djunits.unit.AngleUnit;
import org.djunits.value.vdouble.scalar.Angle;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.xml.bindings.types.AngleType;

/**
 * AngleAdapterTest.java.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
        Locale.setDefault(Locale.US);
        AngleAdapter angleAdapter = new AngleAdapter();

        assertEquals(new AngleType(new Angle(2.3, AngleUnit.DEGREE)), angleAdapter.unmarshal("2.3 deg"));
        // assertEquals(new AngleType(new Angle(2.3, AngleUnit.DEGREE)), angleAdapter.unmarshal("+2.3 deg"));
        assertEquals(new AngleType(new Angle(2.3, AngleUnit.DEGREE)), angleAdapter.unmarshal("2.3deg"));
        // assertEquals(new AngleType(new Angle(2.3, AngleUnit.DEGREE)), angleAdapter.unmarshal("+2.3deg"));
        assertEquals(new AngleType(new Angle(-2.3, AngleUnit.DEGREE)), angleAdapter.unmarshal("-2.3 deg"));
        assertEquals(new AngleType(new Angle(-2.3, AngleUnit.DEGREE)), angleAdapter.unmarshal("-2.3deg"));

        assertEquals(new AngleType(new Angle(2.3, AngleUnit.RADIAN)), angleAdapter.unmarshal("2.3 rad"));
        // assertEquals(new AngleType(new Angle(2.3, AngleUnit.RADIAN)), angleAdapter.unmarshal("+2.3 rad"));
        assertEquals(new AngleType(new Angle(2.3, AngleUnit.RADIAN)), angleAdapter.unmarshal("2.3rad"));
        // assertEquals(new AngleType(new Angle(2.3, AngleUnit.RADIAN)), angleAdapter.unmarshal("+2.3rad"));
        assertEquals(new AngleType(new Angle(-2.3, AngleUnit.RADIAN)), angleAdapter.unmarshal("-2.3 rad"));
        assertEquals(new AngleType(new Angle(-2.3, AngleUnit.RADIAN)), angleAdapter.unmarshal("-2.3rad"));

        assertEquals("2.3 deg", angleAdapter.marshal(new AngleType(new Angle(2.3, AngleUnit.DEGREE))));
        assertEquals("-2.3 deg", angleAdapter.marshal(new AngleType(new Angle(-2.3, AngleUnit.DEGREE))));
        assertEquals("2.3 rad", angleAdapter.marshal(new AngleType(new Angle(2.3, AngleUnit.RADIAN))));
        assertEquals("-2.3 rad", angleAdapter.marshal(new AngleType(new Angle(-2.3, AngleUnit.RADIAN))));

        assertEquals(new AngleType(new Angle(0.0, AngleUnit.DEGREE)), angleAdapter.unmarshal("0.0 deg"));
        // assertEquals(new AngleType(new Angle(0.0, AngleUnit.DEGREE)), angleAdapter.unmarshal("+0.0 deg"));
        assertEquals(new AngleType(new Angle(0.0, AngleUnit.DEGREE)), angleAdapter.unmarshal("0.0deg"));
        // assertEquals(new AngleType(new Angle(0.0, AngleUnit.DEGREE)), angleAdapter.unmarshal("+0.0deg"));
        assertEquals(new AngleType(new Angle(-0.0, AngleUnit.DEGREE)), angleAdapter.unmarshal("-0.0 deg"));
        assertEquals(new AngleType(new Angle(-0.0, AngleUnit.DEGREE)), angleAdapter.unmarshal("-0.0deg"));

        assertEquals(new AngleType(new Angle(0.0, AngleUnit.RADIAN)), angleAdapter.unmarshal("0.0 rad"));
        // assertEquals(new AngleType(new Angle(0.0, AngleUnit.RADIAN)), angleAdapter.unmarshal("+0.0 rad"));
        assertEquals(new AngleType(new Angle(0.0, AngleUnit.RADIAN)), angleAdapter.unmarshal("0.0rad"));
        // assertEquals(new AngleType(new Angle(0.0, AngleUnit.RADIAN)), angleAdapter.unmarshal("+0.0rad"));
        assertEquals(new AngleType(new Angle(-0.0, AngleUnit.RADIAN)), angleAdapter.unmarshal("-0.0 rad"));
        assertEquals(new AngleType(new Angle(-0.0, AngleUnit.RADIAN)), angleAdapter.unmarshal("-0.0rad"));

        assertEquals("0.0 deg", angleAdapter.marshal(new AngleType(new Angle(0.0, AngleUnit.DEGREE))));
        assertEquals("-0.0 deg", angleAdapter.marshal(new AngleType(new Angle(-0.0, AngleUnit.DEGREE))));
        assertEquals("0.0 rad", angleAdapter.marshal(new AngleType(new Angle(0.0, AngleUnit.RADIAN))));
        assertEquals("-0.0 rad", angleAdapter.marshal(new AngleType(new Angle(-0.0, AngleUnit.RADIAN))));

    }
}
