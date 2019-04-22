package org.opentrafficsim.xml.bindings;

import static org.junit.Assert.assertEquals;

import javax.xml.bind.JAXBException;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Try;
import org.junit.Test;

/**
 * LengthAdapterTest for Length and SignedLength. <br>
 * <br>
 * Copyright (c) 2003-2018 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class LengthAdapterTest
{
    /** the allowed units. */
    LengthUnit[] units = new LengthUnit[] {LengthUnit.MILLIMETER, LengthUnit.CENTIMETER, LengthUnit.DECIMETER, LengthUnit.METER,
            LengthUnit.DEKAMETER, LengthUnit.HECTOMETER, LengthUnit.KILOMETER, LengthUnit.MILE, LengthUnit.YARD,
            LengthUnit.FOOT};

    /** the corresponding strings. */
    String[] unitStrings = new String[] {"mm", "cm", "dm", "m", "dam", "hm", "km", "mi", "yd", "ft"};

    /**
     * Test the SignedLengthAdapter
     */
    @Test
    public void testSignedLengthAdapter() throws JAXBException
    {
        LengthAdapter lengthAdapter = new LengthAdapter();

        for (int i = 0; i < this.units.length; i++)
        {
            final LengthUnit unit = this.units[i];
            final String us = this.unitStrings[i];

            assertEquals(new Length(2.3, unit), lengthAdapter.unmarshal("2.3 " + us));
            assertEquals(new Length(2.3, unit), lengthAdapter.unmarshal("+2.3 " + us));
            assertEquals(new Length(2.3, unit), lengthAdapter.unmarshal("2.3" + us));
            assertEquals(new Length(2.3, unit), lengthAdapter.unmarshal("+2.3" + us));
            assertEquals(new Length(-2.3, unit), lengthAdapter.unmarshal("-2.3 " + us));
            assertEquals(new Length(-2.3, unit), lengthAdapter.unmarshal("-2.3" + us));

            assertEquals("2.3 " + us, lengthAdapter.marshal(new Length(2.3, unit)));
            assertEquals("-2.3 " + us, lengthAdapter.marshal(new Length(-2.3, unit)));

            assertEquals(new Length(0.0, unit), lengthAdapter.unmarshal("0.0 " + us));
            assertEquals(new Length(0.0, unit), lengthAdapter.unmarshal("+0.0 " + us));
            assertEquals(new Length(0.0, unit), lengthAdapter.unmarshal("0.0" + us));
            assertEquals(new Length(0.0, unit), lengthAdapter.unmarshal("+0.0" + us));
            assertEquals(new Length(-0.0, unit), lengthAdapter.unmarshal("-0.0 " + us));
            assertEquals(new Length(-0.0, unit), lengthAdapter.unmarshal("-0.0" + us));

            assertEquals("0.0 " + us, lengthAdapter.marshal(new Length(0.0, unit)));
            assertEquals("-0.0 " + us, lengthAdapter.marshal(new Length(-0.0, unit)));
        }
    }

    /**
     * Test the LengthAdapter
     */
    @Test
    public void testLengthAdapter() throws JAXBException
    {
        PositiveLengthAdapter lengthAdapter = new PositiveLengthAdapter();

        for (int i = 0; i < this.units.length; i++)
        {
            final LengthUnit unit = this.units[i];
            final String us = this.unitStrings[i];

            assertEquals(new Length(2.3, unit), lengthAdapter.unmarshal("2.3 " + us));
            assertEquals(new Length(2.3, unit), lengthAdapter.unmarshal("+2.3 " + us));
            assertEquals(new Length(2.3, unit), lengthAdapter.unmarshal("2.3" + us));
            assertEquals(new Length(2.3, unit), lengthAdapter.unmarshal("+2.3" + us));
            Try.testFail(() -> lengthAdapter.unmarshal("-2.3 " + us));
            Try.testFail(() -> lengthAdapter.unmarshal("-2.3" + us));

            assertEquals("2.3 " + us, lengthAdapter.marshal(new Length(2.3, unit)));
            Try.testFail(() -> lengthAdapter.marshal(new Length(-2.3, unit)));

            assertEquals(new Length(0.0, unit), lengthAdapter.unmarshal("0.0 " + us));
            assertEquals(new Length(0.0, unit), lengthAdapter.unmarshal("+0.0 " + us));
            assertEquals(new Length(0.0, unit), lengthAdapter.unmarshal("0.0" + us));
            assertEquals(new Length(0.0, unit), lengthAdapter.unmarshal("+0.0" + us));
            Try.testFail(() -> lengthAdapter.unmarshal("-0.0 " + us));
            Try.testFail(() -> lengthAdapter.unmarshal("-0.0" + us));

            assertEquals("0.0 " + us, lengthAdapter.marshal(new Length(0.0, unit)));
            Try.testFail(() -> lengthAdapter.marshal(new Length(-0.0, unit)));
        }
    }

}
