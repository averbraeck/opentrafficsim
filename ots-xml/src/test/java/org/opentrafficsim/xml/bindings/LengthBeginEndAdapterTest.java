package org.opentrafficsim.xml.bindings;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Try;
import org.opentrafficsim.xml.bindings.types.LengthBeginEndType;
import org.opentrafficsim.xml.bindings.types.LengthBeginEndType.LengthBeginEnd;

import jakarta.xml.bind.JAXBException;

/**
 * LengthBeginEndAdapterTest for LengthBeginEnd.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
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
    // TODO: repair @Test (problem with Locale when creating Length from String "2.3mm")
    public void testLengthBeginEndAdapter() throws JAXBException
    {
        LengthBeginEndAdapter lbeAdapter = new LengthBeginEndAdapter();

        LengthBeginEndType lbeBegin = new LengthBeginEndType(new LengthBeginEnd(true, Length.ZERO));
        LengthBeginEndType lbeEnd = new LengthBeginEndType(new LengthBeginEnd(false, Length.ZERO));

        assertEquals(lbeBegin.getValue(), lbeAdapter.unmarshal("BEGIN").getValue());
        assertEquals(lbeEnd.getValue(), lbeAdapter.unmarshal("END").getValue());

        assertEquals("BEGIN", lbeAdapter.marshal(lbeBegin));
        assertEquals("END", lbeAdapter.marshal(lbeEnd));

        LengthBeginEndType lbeFraction00 = new LengthBeginEndType(new LengthBeginEnd(0.0));
        LengthBeginEndType lbeFraction05 = new LengthBeginEndType(new LengthBeginEnd(0.5));
        LengthBeginEndType lbeFraction10 = new LengthBeginEndType(new LengthBeginEnd(1.0));

        assertEquals(lbeFraction00.getValue(), lbeAdapter.unmarshal("0.0").getValue());
        assertEquals(lbeFraction00.getValue(), lbeAdapter.unmarshal(".0").getValue());
        assertEquals(lbeFraction00.getValue(), lbeAdapter.unmarshal("0").getValue());
        assertEquals(lbeFraction00.getValue(), lbeAdapter.unmarshal("0.00").getValue());

        assertEquals(lbeFraction05.getValue(), lbeAdapter.unmarshal("0.5").getValue());
        assertEquals(lbeFraction05.getValue(), lbeAdapter.unmarshal(".5").getValue());
        assertEquals(lbeFraction05.getValue(), lbeAdapter.unmarshal("0.50").getValue());

        assertEquals(lbeFraction10.getValue(), lbeAdapter.unmarshal("1.0").getValue());
        assertEquals(lbeFraction10.getValue(), lbeAdapter.unmarshal("1").getValue());
        assertEquals(lbeFraction10.getValue(), lbeAdapter.unmarshal("1.00").getValue());

        assertEquals(lbeFraction00.getValue(), lbeAdapter.unmarshal("0.0%").getValue());
        assertEquals(lbeFraction00.getValue(), lbeAdapter.unmarshal(".0%").getValue());
        assertEquals(lbeFraction00.getValue(), lbeAdapter.unmarshal("0%").getValue());
        assertEquals(lbeFraction00.getValue(), lbeAdapter.unmarshal("0.00%").getValue());

        assertEquals(lbeFraction00.getValue(), lbeAdapter.unmarshal("0.0 %").getValue());
        assertEquals(lbeFraction00.getValue(), lbeAdapter.unmarshal(".0 %").getValue());
        assertEquals(lbeFraction00.getValue(), lbeAdapter.unmarshal("0 %").getValue());
        assertEquals(lbeFraction00.getValue(), lbeAdapter.unmarshal("0.00 %").getValue());

        assertEquals(lbeFraction05.getValue(), lbeAdapter.unmarshal("50%").getValue());
        assertEquals(lbeFraction05.getValue(), lbeAdapter.unmarshal("50.0%").getValue());

        assertEquals(lbeFraction05.getValue(), lbeAdapter.unmarshal("50 %").getValue());
        assertEquals(lbeFraction05.getValue(), lbeAdapter.unmarshal("50.0 %").getValue());

        assertEquals(lbeFraction10.getValue(), lbeAdapter.unmarshal("100%").getValue());
        assertEquals(lbeFraction10.getValue(), lbeAdapter.unmarshal("100.0%").getValue());

        assertEquals(lbeFraction10.getValue(), lbeAdapter.unmarshal("100 %").getValue());
        assertEquals(lbeFraction10.getValue(), lbeAdapter.unmarshal("100.0 %").getValue());

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
                final LengthBeginEndType lbe23 = new LengthBeginEndType(new LengthBeginEnd(begin, new Length(2.3, unit)));
                final LengthBeginEndType lbe00 = new LengthBeginEndType(new LengthBeginEnd(begin, new Length(0.0, unit)));

                assertEquals(lbe23.getValue(), lbeAdapter.unmarshal(prefix + "2.3 " + us).getValue());
                assertEquals(lbe23.getValue(), lbeAdapter.unmarshal(prefix + "2.3" + us).getValue());
                Try.testFail(() -> lbeAdapter.unmarshal(prefix + "-2.3 " + us));
                Try.testFail(() -> lbeAdapter.unmarshal(prefix + "-2.3" + us));

                assertEquals(prefix + "2.3 " + us, lbeAdapter.marshal(lbe23));
                Try.testFail(
                        () -> lbeAdapter.marshal(new LengthBeginEndType(new LengthBeginEnd(begin, new Length(-2.3, unit)))));

                assertEquals(lbe00.getValue(), lbeAdapter.unmarshal(prefix + "0.0 " + us).getValue());
                assertEquals(lbe00.getValue(), lbeAdapter.unmarshal(prefix + "0.0" + us).getValue());
                Try.testFail(() -> lbeAdapter.unmarshal(prefix + "-0.0 " + us));
                Try.testFail(() -> lbeAdapter.unmarshal(prefix + "-0.0" + us));

                assertEquals(begin ? "BEGIN" : "END", lbeAdapter.marshal(lbe00));
                Try.testFail(
                        () -> lbeAdapter.marshal(new LengthBeginEndType(new LengthBeginEnd(begin, new Length(-0.0, unit)))));
            }
        }
    }

}
