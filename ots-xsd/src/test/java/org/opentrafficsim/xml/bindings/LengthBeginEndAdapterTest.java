package org.opentrafficsim.xml.bindings;

import static org.junit.Assert.assertEquals;

import javax.xml.bind.JAXBException;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Try;
import org.opentrafficsim.xml.bindings.types.LengthBeginEnd;

/**
 * LengthBeginEndAdapterTest for LengthBeginEnd.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class LengthBeginEndAdapterTest
{
    /** the allowed units. */
    LengthUnit[] units = new LengthUnit[] {LengthUnit.MILLIMETER, LengthUnit.CENTIMETER, LengthUnit.DECIMETER, LengthUnit.METER,
            LengthUnit.DECAMETER, LengthUnit.HECTOMETER, LengthUnit.KILOMETER, LengthUnit.MILE, LengthUnit.YARD,
            LengthUnit.FOOT};

    /** the corresponding strings. */
    String[] unitStrings = new String[] {"mm", "cm", "dm", "m", "dam", "hm", "km", "mi", "yd", "ft"};

    /**
     * Test the LengthBeginEndAdapter
     */
    // TODO: repair @Test
    public void testLengthBeginEndAdapter() throws JAXBException
    {
        LengthBeginEndAdapter lbeAdapter = new LengthBeginEndAdapter();

        LengthBeginEnd lbeBegin = new LengthBeginEnd(true, Length.ZERO);
        LengthBeginEnd lbeEnd = new LengthBeginEnd(false, Length.ZERO);

        assertEquals(lbeBegin, lbeAdapter.unmarshal("BEGIN"));
        assertEquals(lbeEnd, lbeAdapter.unmarshal("END"));

        assertEquals("BEGIN", lbeAdapter.marshal(lbeBegin));
        assertEquals("END", lbeAdapter.marshal(lbeEnd));

        LengthBeginEnd lbeFraction00 = new LengthBeginEnd(0.0);
        LengthBeginEnd lbeFraction05 = new LengthBeginEnd(0.5);
        LengthBeginEnd lbeFraction10 = new LengthBeginEnd(1.0);

        assertEquals(lbeFraction00, lbeAdapter.unmarshal("0.0"));
        assertEquals(lbeFraction00, lbeAdapter.unmarshal(".0"));
        assertEquals(lbeFraction00, lbeAdapter.unmarshal("0"));
        assertEquals(lbeFraction00, lbeAdapter.unmarshal("0.00"));

        assertEquals(lbeFraction05, lbeAdapter.unmarshal("0.5"));
        assertEquals(lbeFraction05, lbeAdapter.unmarshal(".5"));
        assertEquals(lbeFraction05, lbeAdapter.unmarshal("0.50"));

        assertEquals(lbeFraction10, lbeAdapter.unmarshal("1.0"));
        assertEquals(lbeFraction10, lbeAdapter.unmarshal("1"));
        assertEquals(lbeFraction10, lbeAdapter.unmarshal("1.00"));

        assertEquals(lbeFraction00, lbeAdapter.unmarshal("0.0%"));
        assertEquals(lbeFraction00, lbeAdapter.unmarshal(".0%"));
        assertEquals(lbeFraction00, lbeAdapter.unmarshal("0%"));
        assertEquals(lbeFraction00, lbeAdapter.unmarshal("0.00%"));

        assertEquals(lbeFraction00, lbeAdapter.unmarshal("0.0 %"));
        assertEquals(lbeFraction00, lbeAdapter.unmarshal(".0 %"));
        assertEquals(lbeFraction00, lbeAdapter.unmarshal("0 %"));
        assertEquals(lbeFraction00, lbeAdapter.unmarshal("0.00 %"));

        assertEquals(lbeFraction05, lbeAdapter.unmarshal("50%"));
        assertEquals(lbeFraction05, lbeAdapter.unmarshal("50.0%"));

        assertEquals(lbeFraction05, lbeAdapter.unmarshal("50 %"));
        assertEquals(lbeFraction05, lbeAdapter.unmarshal("50.0 %"));

        assertEquals(lbeFraction10, lbeAdapter.unmarshal("100%"));
        assertEquals(lbeFraction10, lbeAdapter.unmarshal("100.0%"));

        assertEquals(lbeFraction10, lbeAdapter.unmarshal("100 %"));
        assertEquals(lbeFraction10, lbeAdapter.unmarshal("100.0 %"));

        assertEquals("0.0", lbeAdapter.marshal(lbeFraction00));
        assertEquals("0.5", lbeAdapter.marshal(lbeFraction05));
        assertEquals("1.0", lbeAdapter.marshal(lbeFraction10));

        Try.testFail(() -> lbeAdapter.unmarshal("XYZ"));
        Try.testFail(() -> lbeAdapter.unmarshal("BEGIN XYZ"));
        Try.testFail(() -> lbeAdapter.unmarshal("END XYZ"));
        Try.testFail(() -> lbeAdapter.unmarshal("END-XYZ"));
        Try.testFail(() -> lbeAdapter.unmarshal("END-100"));
        Try.testFail(() -> lbeAdapter.unmarshal("-0.5"));
        Try.testFail(() -> lbeAdapter.unmarshal("-50%"));
        Try.testFail(() -> lbeAdapter.unmarshal("END+10m"));

        for (boolean begin : new boolean[] {false, true})
        {
            System.out.println(begin);
            for (int i = 0; i < this.units.length; i++)
            {
                final LengthUnit unit = this.units[i];
                final String us = this.unitStrings[i];
                final String prefix = begin ? "" : "END-";
                final LengthBeginEnd lbe23 = new LengthBeginEnd(begin, new Length(2.3, unit));
                final LengthBeginEnd lbe00 = new LengthBeginEnd(begin, new Length(0.0, unit));

                assertEquals(lbe23, lbeAdapter.unmarshal(prefix + "2.3 " + us));
                assertEquals(lbe23, lbeAdapter.unmarshal(prefix + "2.3" + us));
                Try.testFail(() -> lbeAdapter.unmarshal(prefix + "-2.3 " + us));
                Try.testFail(() -> lbeAdapter.unmarshal(prefix + "-2.3" + us));

                assertEquals(prefix + "2.3 " + us, lbeAdapter.marshal(lbe23));
                Try.testFail(() -> lbeAdapter.marshal(new LengthBeginEnd(begin, new Length(-2.3, unit))));

                assertEquals(lbe00, lbeAdapter.unmarshal(prefix + "0.0 " + us));
                assertEquals(lbe00, lbeAdapter.unmarshal(prefix + "0.0" + us));
                Try.testFail(() -> lbeAdapter.unmarshal(prefix + "-0.0 " + us));
                Try.testFail(() -> lbeAdapter.unmarshal(prefix + "-0.0" + us));

                assertEquals(begin ? "BEGIN" : "END", lbeAdapter.marshal(lbe00));
                Try.testFail(() -> lbeAdapter.marshal(new LengthBeginEnd(begin, new Length(-0.0, unit))));
            }
        }
    }

}
