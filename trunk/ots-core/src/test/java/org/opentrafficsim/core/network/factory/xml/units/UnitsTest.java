package org.opentrafficsim.core.network.factory.xml.units;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Test the AccelerationUnits, AngleUnits and similar classes.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jan 16, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class UnitsTest
{

    /**
     * Test the basics of the AccelerationUnits parser.
     * @throws NetworkException if that happens uncaught; this test has failed
     */
    @Test
    public final void testAccelerationUnits() throws NetworkException
    {
        checkUnitAndValue(AccelerationUnits.parseAccelerationUnit("129600km/h^2"),
                AccelerationUnits.parseAcceleration("129600km/h^2").si, "km/h^2", 10);
        checkUnitAndValue(AccelerationUnits.parseAccelerationUnit("80547mi/h^2"),
                AccelerationUnits.parseAcceleration("80547mi/h^2").si, "mi/h^2", 10);
        checkUnitAndValue(AccelerationUnits.parseAccelerationUnit("100 m/s^2"),
                AccelerationUnits.parseAcceleration("100 m/s^2").si, "m/s^2", 100);
        checkUnitAndValue(AccelerationUnits.parseAccelerationUnit("100 ft/s^2"),
                AccelerationUnits.parseAcceleration("100 ft/s^2").si, "ft/s^2", 30.48);
        try
        {
            AccelerationUnits.parseAccelerationUnit("123 m/s");
            fail("non-acceleration unit should have thrown a ... NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            AccelerationUnits.parseAcceleration("123.34.56 m/s^2");
            fail("badly formatted number should have thrown a ... NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
    }

    /**
     * Test the basics of the AngleUnits parser.
     * @throws NetworkException if that happens uncaught; this test has failed
     */
    @Test
    public final void testAngleUnits() throws NetworkException
    {
        checkUnitAndValue(AngleUnits.parseAngleUnit("270deg"), AngleUnits.parseAngle("270deg").si, "deg", 1.5 * Math.PI);
        checkUnitAndValue(AngleUnits.parseAngleUnit("2 rad"), AngleUnits.parseAngle("2 rad").si, "rad", 2);
        try
        {
            AngleUnits.parseAngleUnit("123 m/s");
            fail("non-angle unit should have thrown a ... NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            AngleUnits.parseAngle("123.34.56 rad");
            fail("badly formatted number should have thrown a ... NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
    }

    /**
     * Test the basics of the AngleUnits parser.
     * @throws NetworkException if that happens uncaught; this test has failed
     */
    @Test
    public final void testDirectionUnits() throws NetworkException
    {
        checkUnitAndValue(AngleUnits.parseAngleUnit("270deg"), AngleUnits.parseDirection("270deg").si, "deg", 1.5 * Math.PI);
        checkUnitAndValue(AngleUnits.parseAngleUnit("2 rad"), AngleUnits.parseDirection("2 rad").si, "rad", 2);
        try
        {
            AngleUnits.parseAngleUnit("123 m/s");
            fail("non-angle unit should have thrown a ... NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            AngleUnits.parseDirection("123.34.56 rad");
            fail("badly formatted number should have thrown a ... NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
    }

    /**
     * Test the basics of the LengthUnits parser.
     * @throws NetworkException if that happens uncaught; this test has failed
     */
    @Test
    public final void testLengthUnits() throws NetworkException
    {
        checkUnitAndValue(LengthUnits.parseLengthUnit("123456mm"), LengthUnits.parseLength("123456mm").si, "mm", 123.456);
        checkUnitAndValue(LengthUnits.parseLengthUnit("123456cm"), LengthUnits.parseLength("123456cm").si, "cm", 1234.56);
        checkUnitAndValue(LengthUnits.parseLengthUnit("123456dm"), LengthUnits.parseLength("123456dm").si, "dm", 12345.6);
        checkUnitAndValue(LengthUnits.parseLengthUnit("123.456dam"), LengthUnits.parseLength("123.456dam").si, "dam", 1234.56);
        checkUnitAndValue(LengthUnits.parseLengthUnit("123.456hm"), LengthUnits.parseLength("123.456hm").si, "hm", 12345.6);
        checkUnitAndValue(LengthUnits.parseLengthUnit("123.456m"), LengthUnits.parseLength("123.456m").si, "m", 123.456);
        checkUnitAndValue(LengthUnits.parseLengthUnit("123.456km"), LengthUnits.parseLength("123.456km").si, "km", 123456);
        checkUnitAndValue(LengthUnits.parseLengthUnit("123.456mi"), LengthUnits.parseLength("123.456mi").si, "mi",
                198683.172864);
        checkUnitAndValue(LengthUnits.parseLengthUnit("123456y"), LengthUnits.parseLength("123456y").si, "y", 112888.1664);
        checkUnitAndValue(LengthUnits.parseLengthUnit("123456ft"), LengthUnits.parseLength("123456ft").si, "ft", 37629.3888);
        try
        {
            // FIXME: "123 m/s" is accepted by the parser
            LengthUnits.parseLengthUnit("123 s");
            fail("non-length unit should have thrown a ... NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            LengthUnits.parseLength("123.34.56 m");
            fail("badly formatted number should have thrown a ... NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
    }

    /**
     * Test the basics of the LengthUnits parser.
     * @throws NetworkException if that happens uncaught; this test has failed
     */
    @Test
    public final void testPosition() throws NetworkException
    {
        checkUnitAndValue(LengthUnits.parseLengthUnit("123456mm"), PositionUnits.parsePosition("123456mm").si, "mm", 123.456);
        checkUnitAndValue(LengthUnits.parseLengthUnit("123456cm"), PositionUnits.parsePosition("123456cm").si, "cm", 1234.56);
        checkUnitAndValue(LengthUnits.parseLengthUnit("123456dm"), PositionUnits.parsePosition("123456dm").si, "dm", 12345.6);
        checkUnitAndValue(LengthUnits.parseLengthUnit("123.456dam"), PositionUnits.parsePosition("123.456dam").si, "dam",
                1234.56);
        checkUnitAndValue(LengthUnits.parseLengthUnit("123.456hm"), PositionUnits.parsePosition("123.456hm").si, "hm", 12345.6);
        checkUnitAndValue(LengthUnits.parseLengthUnit("123.456m"), PositionUnits.parsePosition("123.456m").si, "m", 123.456);
        checkUnitAndValue(LengthUnits.parseLengthUnit("123.456km"), PositionUnits.parsePosition("123.456km").si, "km", 123456);
        checkUnitAndValue(LengthUnits.parseLengthUnit("123.456mi"), PositionUnits.parsePosition("123.456mi").si, "mi",
                198683.172864);
        checkUnitAndValue(LengthUnits.parseLengthUnit("123456y"), PositionUnits.parsePosition("123456y").si, "y", 112888.1664);
        checkUnitAndValue(LengthUnits.parseLengthUnit("123456ft"), PositionUnits.parsePosition("123456ft").si, "ft",
                37629.3888);
        try
        {
            // FIXME: "123 m/s" is accepted by the parser
            LengthUnits.parseLengthUnit("123 s");
            fail("non-length unit should have thrown a ... NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            PositionUnits.parsePosition("123.34.56 m");
            fail("badly formatted number should have thrown a ... NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
    }

    /**
     * Test the basics of the LengthUnits parser.
     * @throws NetworkException if that happens uncaught; this test has failed
     */
    @Test
    public final void testPerLengthUnits() throws NetworkException
    {
        checkUnitAndValue(LengthUnits.parsePerLengthUnit("123.456/mm"), LengthUnits.parseLinearDensity("123.456/mm").si, "/mm",
                123456);
        checkUnitAndValue(LengthUnits.parsePerLengthUnit("123.456/cm"), LengthUnits.parseLinearDensity("123.456/cm").si, "/cm",
                12345.6);
        checkUnitAndValue(LengthUnits.parsePerLengthUnit("123.456 /dm"), LengthUnits.parseLinearDensity("123.456 /dm").si,
                "/dm", 1234.56);
        checkUnitAndValue(LengthUnits.parsePerLengthUnit("123.456 /dam"), LengthUnits.parseLinearDensity("123.456 /dam").si,
                "/dam", 12.3456);
        checkUnitAndValue(LengthUnits.parsePerLengthUnit("123.456 /hm"), LengthUnits.parseLinearDensity("123.456 /hm").si,
                "/hm", 1.23456);
        checkUnitAndValue(LengthUnits.parsePerLengthUnit("123.456 /m"), LengthUnits.parseLinearDensity("123.456 /m").si, "/m",
                123.456);
        checkUnitAndValue(LengthUnits.parsePerLengthUnit("123456 /km"), LengthUnits.parseLinearDensity("123456 /km").si, "/km",
                123.456);
        checkUnitAndValue(LengthUnits.parsePerLengthUnit("123456 /mi"), LengthUnits.parseLinearDensity("123456 /mi").si, "/mi",
                76.7120019);
        checkUnitAndValue(LengthUnits.parsePerLengthUnit("123.456 /y"), LengthUnits.parseLinearDensity("123.456 /y").si, "/y",
                135.01312);
        checkUnitAndValue(LengthUnits.parsePerLengthUnit("123.456 /ft"), LengthUnits.parseLinearDensity("123.456 /ft").si,
                "/ft", 405.03937);
        try
        {
            LengthUnits.parsePerLengthUnit("123 s");
            fail("non-length unit should have thrown a ... NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            LengthUnits.parseLinearDensity("123.34.56 /m");
            fail("badly formatted number should have thrown a ... NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
    }

    /**
     * Test the basics of the TimeUnits parser.
     * @throws NetworkException if that happens uncaught; this test has failed
     */
    @Test
    public final void testTimeUnits() throws NetworkException
    {
        checkUnitAndValue(TimeUnits.parseTimeUnit("123456ms"), TimeUnits.parseTime("123456ms").si, "ms", 123.456);
        checkUnitAndValue(TimeUnits.parseTimeUnit("123456s"), TimeUnits.parseTime("123456s").si, "s", 123456);
        checkUnitAndValue(TimeUnits.parseTimeUnit("123.456m"), TimeUnits.parseTime("123.456m").si, "m", 7407.36);
        checkUnitAndValue(TimeUnits.parseTimeUnit("123.456min"), TimeUnits.parseTime("123.456min").si, "min", 7407.36);
        checkUnitAndValue(TimeUnits.parseTimeUnit("123.456h"), TimeUnits.parseTime("123.456h").si, "h", 444441.6);
        checkUnitAndValue(TimeUnits.parseTimeUnit("123.456hr"), TimeUnits.parseTime("123.456hr").si, "hr", 444441.6);
        checkUnitAndValue(TimeUnits.parseTimeUnit("1.23456 day"), TimeUnits.parseTime("1.23456 day").si, "day", 106665.984);
        checkUnitAndValue(TimeUnits.parseTimeUnit("1.23456 wk"), TimeUnits.parseTime("1.23456 wk").si, "wk", 746661.888);
        checkUnitAndValue(TimeUnits.parseTimeUnit("1.23456 week"), TimeUnits.parseTime("1.23456 week").si, "week", 746661.888);
        try
        {
            // FIXME: "123 km" is accepted by the parser
            TimeUnits.parseTimeUnit("123 kg");
            fail("non-time unit should have thrown a ... NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            TimeUnits.parseTime("123.34.56 s");
            fail("badly formatted number should have thrown a ... NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
    }

    /**
     * Test the basics of the TimeUnits parser.
     * @throws NetworkException if that happens uncaught; this test has failed
     */
    @Test
    public final void testDurationUnits() throws NetworkException
    {
        checkUnitAndValue(TimeUnits.parseTimeUnit("123456ms"), DurationUnits.parseDuration("123456ms").si, "ms", 123.456);
        checkUnitAndValue(TimeUnits.parseTimeUnit("123456s"), DurationUnits.parseDuration("123456s").si, "s", 123456);
        checkUnitAndValue(TimeUnits.parseTimeUnit("123.456m"), DurationUnits.parseDuration("123.456m").si, "m", 7407.36);
        checkUnitAndValue(TimeUnits.parseTimeUnit("123.456min"), DurationUnits.parseDuration("123.456min").si, "min", 7407.36);
        checkUnitAndValue(TimeUnits.parseTimeUnit("123.456h"), DurationUnits.parseDuration("123.456h").si, "h", 444441.6);
        checkUnitAndValue(TimeUnits.parseTimeUnit("123.456hr"), DurationUnits.parseDuration("123.456hr").si, "hr", 444441.6);
        checkUnitAndValue(TimeUnits.parseTimeUnit("1.23456 day"), DurationUnits.parseDuration("1.23456 day").si, "day",
                106665.984);
        checkUnitAndValue(TimeUnits.parseTimeUnit("1.23456 wk"), DurationUnits.parseDuration("1.23456 wk").si, "wk",
                746661.888);
        checkUnitAndValue(TimeUnits.parseTimeUnit("1.23456 week"), DurationUnits.parseDuration("1.23456 week").si, "week",
                746661.888);
        try
        {
            // FIXME: "123 km" is accepted by the parser
            TimeUnits.parseTimeUnit("123 kg");
            fail("non-time unit should have thrown a ... NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            DurationUnits.parseDuration("123.34.56 s");
            fail("badly formatted number should have thrown a ... NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
    }

    /**
     * Test the basics of the FrequencyUnits parser.
     * @throws NetworkException if that happens uncaught; this test has failed
     */
    @Test
    public final void testFrequencyUnits() throws NetworkException
    {
        checkUnitAndValue(DurationUnits.parseFrequencyUnit("123.456/ms"), DurationUnits.parseFrequency("123.456/ms").si, "/ms",
                123456);
        checkUnitAndValue(DurationUnits.parseFrequencyUnit("123456/s"), DurationUnits.parseFrequency("123456/s").si, "/s",
                123456);
        checkUnitAndValue(DurationUnits.parseFrequencyUnit("123.456/m"), DurationUnits.parseFrequency("123.456/m").si, "/m",
                2.0576);
        checkUnitAndValue(DurationUnits.parseFrequencyUnit("123.456/min"), DurationUnits.parseFrequency("123.456/min").si,
                "/min", 2.0576);
        checkUnitAndValue(DurationUnits.parseFrequencyUnit("123.456/h"), DurationUnits.parseFrequency("123.456/h").si, "/h",
                0.03429);
        checkUnitAndValue(DurationUnits.parseFrequencyUnit("123.456/hr"), DurationUnits.parseFrequency("123.456/hr").si, "/hr",
                0.03429);
        checkUnitAndValue(DurationUnits.parseFrequencyUnit("123456 /day"), DurationUnits.parseFrequency("123456 /day").si,
                "/day", 1.42888888);
        checkUnitAndValue(DurationUnits.parseFrequencyUnit("123456 /wk"), DurationUnits.parseFrequency("123456 /wk").si, "/wk",
                0.20412698);
        checkUnitAndValue(DurationUnits.parseFrequencyUnit("123456 /week"), DurationUnits.parseFrequency("123456 /week").si,
                "/week", 0.20412698);
        try
        {
            DurationUnits.parseFrequencyUnit("123 kg");
            fail("non-frequency unit should have thrown a ... NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            DurationUnits.parseFrequency("123.34.56 /s");
            fail("badly formatted number should have thrown a ... NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
    }

    /**
     * Test the basics of the SpeedUnits parser.
     * @throws NetworkException if that happens uncaught; this test has failed
     */
    @Test
    public final void testSpeedUnits() throws NetworkException
    {
        checkUnitAndValue(SpeedUnits.parseSpeedUnit("123.456km/h"), SpeedUnits.parseSpeed("123.456km/h").si, "km/h",
                34.29333333);
        checkUnitAndValue(SpeedUnits.parseSpeedUnit("123.456mi/h"), SpeedUnits.parseSpeed("123.456mi/h").si, "mi/h", 55.18977);
        checkUnitAndValue(SpeedUnits.parseSpeedUnit("123456 m/s"), SpeedUnits.parseSpeed("123456 m/s").si, "m/s", 123456);
        checkUnitAndValue(SpeedUnits.parseSpeedUnit("123.456ft/s"), SpeedUnits.parseSpeed("123.456ft/s").si, "ft/s",
                37.6293888);
        try
        {
            SpeedUnits.parseSpeedUnit("123 s");
            fail("non-speed unit should have thrown a ... NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            SpeedUnits.parseSpeed("123.34.56 m/s");
            fail("badly formatted number should have thrown a ... NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
    }

    /**
     * Check the parsed unit and the parsed value.
     * @param parsedUnit String; should match the abbreviation key of the unit
     * @param parsedValue double; the SI value of the parsed expression
     * @param expectedSubKey String; the expected abbreviation key
     * @param expectedValue double; the expected SI value
     */
    private void checkUnitAndValue(final String parsedUnit, final double parsedValue, final String expectedSubKey,
            final double expectedValue)
    {
        String subKey = getSubKey(parsedUnit);
        assertEquals("sub key should equal expected sub key", expectedSubKey, subKey);
        assertEquals("value should match expected value", expectedValue, parsedValue, 0.01);
    }

    /**
     * Extract the last element from the localization key.
     * @param key String; the localization key
     * @return String; the part of the localization key after the last dot, or the whole localization key if no dot was found
     */
    private String getSubKey(final String key)
    {
        int pos = key.lastIndexOf(".");
        if (pos >= 0)
        {
            return key.substring(pos + 1);
        }
        return key;
    }
}
