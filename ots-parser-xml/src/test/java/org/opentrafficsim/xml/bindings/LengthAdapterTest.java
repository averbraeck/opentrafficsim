package org.opentrafficsim.xml.bindings;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.Locale;

import javax.xml.bind.JAXBException;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Try;
import org.junit.jupiter.api.Test;
import org.opentrafficsim.xml.bindings.types.LengthType;

/**
 * LengthAdapterTest for Length and SignedLength.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class LengthAdapterTest
{
    /** the allowed units. */
    LengthUnit[] units = new LengthUnit[] {LengthUnit.MILLIMETER, LengthUnit.CENTIMETER, LengthUnit.DECIMETER, LengthUnit.METER,
            LengthUnit.DECAMETER, LengthUnit.HECTOMETER, LengthUnit.KILOMETER, LengthUnit.MILE, LengthUnit.YARD,
            LengthUnit.FOOT};

    /** the corresponding strings. */
    String[] unitStrings = new String[] {"mm", "cm", "dm", "m", "dam", "hm", "km", "mi", "yd", "ft"};

    /**
     * Test the SignedLengthAdapter
     */
    @Test
    public void testSignedLengthAdapter() throws JAXBException
    {
        Locale.setDefault(Locale.US);
        LengthAdapter lengthAdapter = new LengthAdapter();

        for (int i = 0; i < this.units.length; i++)
        {
            final LengthUnit unit = this.units[i];
            final String us = this.unitStrings[i];

            assertEquals(new LengthType(new Length(2.3, unit)), lengthAdapter.unmarshal("2.3 " + us));
            // assertEquals(new LengthType(new Length(2.3, unit)), lengthAdapter.unmarshal("+2.3 " + us));
            assertEquals(new LengthType(new Length(2.3, unit)), lengthAdapter.unmarshal("2.3" + us));
            // assertEquals(new LengthType(new Length(2.3, unit)), lengthAdapter.unmarshal("+2.3" + us));
            assertEquals(new LengthType(new Length(-2.3, unit)), lengthAdapter.unmarshal("-2.3 " + us));
            assertEquals(new LengthType(new Length(-2.3, unit)), lengthAdapter.unmarshal("-2.3" + us));

            assertEquals("2.3 " + us, lengthAdapter.marshal(new LengthType(new Length(2.3, unit))));
            assertEquals("-2.3 " + us, lengthAdapter.marshal(new LengthType(new Length(-2.3, unit))));

            assertEquals(new LengthType(new Length(0.0, unit)), lengthAdapter.unmarshal("0.0 " + us));
            // assertEquals(new LengthType(new Length(0.0, unit)), lengthAdapter.unmarshal("+0.0 " + us));
            assertEquals(new LengthType(new Length(0.0, unit)), lengthAdapter.unmarshal("0.0" + us));
            // assertEquals(new LengthType(new Length(0.0, unit)), lengthAdapter.unmarshal("+0.0" + us));
            assertEquals(new LengthType(new Length(-0.0, unit)), lengthAdapter.unmarshal("-0.0 " + us));
            assertEquals(new LengthType(new Length(-0.0, unit)), lengthAdapter.unmarshal("-0.0" + us));

            assertEquals("0.0 " + us, lengthAdapter.marshal(new LengthType(new Length(0.0, unit))));
            assertEquals("-0.0 " + us, lengthAdapter.marshal(new LengthType(new Length(-0.0, unit))));
        }
    }

    /**
     * Test the LengthAdapter
     */
    // TODO: Repair @Test
    public void testLengthAdapter() throws JAXBException
    {
        Locale.setDefault(Locale.US);
        PositiveLengthAdapter lengthAdapter = new PositiveLengthAdapter();

        for (int i = 0; i < this.units.length; i++)
        {
            final LengthUnit unit = this.units[i];
            final String us = this.unitStrings[i];

            assertEquals(new LengthType(new Length(2.3, unit)), lengthAdapter.unmarshal("2.3 " + us));
            // assertEquals(new LengthType(new Length(2.3, unit)), lengthAdapter.unmarshal("+2.3 " + us));
            assertEquals(new LengthType(new Length(2.3, unit)), lengthAdapter.unmarshal("2.3" + us));
            // assertEquals(new LengthType(new Length(2.3, unit)), lengthAdapter.unmarshal("+2.3" + us));
            Try.testFail(() -> lengthAdapter.unmarshal("-2.3 " + us));
            Try.testFail(() -> lengthAdapter.unmarshal("-2.3" + us));

            assertEquals("2.3 " + us, lengthAdapter.marshal(new LengthType(new Length(2.3, unit))));
            Try.testFail(() -> lengthAdapter.marshal(new LengthType(new Length(-2.3, unit))));

            assertEquals(new LengthType(new Length(0.0, unit)), lengthAdapter.unmarshal("0.0 " + us));
            // assertEquals(new LengthType(new Length(0.0, unit)), lengthAdapter.unmarshal("+0.0 " + us));
            assertEquals(new LengthType(new Length(0.0, unit)), lengthAdapter.unmarshal("0.0" + us));
            // assertEquals(new LengthType(new Length(0.0, unit)), lengthAdapter.unmarshal("+0.0" + us));
            Try.testFail(() -> lengthAdapter.unmarshal("-0.0 " + us));
            Try.testFail(() -> lengthAdapter.unmarshal("-0.0" + us));

            assertEquals("0.0 " + us, lengthAdapter.marshal(new LengthType(new Length(0.0, unit))));
            Try.testFail(() -> lengthAdapter.marshal(new LengthType(new Length(-0.0, unit))));
        }
    }

}
