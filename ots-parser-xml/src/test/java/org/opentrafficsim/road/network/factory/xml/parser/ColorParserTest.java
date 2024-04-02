package org.opentrafficsim.road.network.factory.xml.parser;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.Color;

import org.junit.jupiter.api.Test;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.factory.xml.utils.ColorParser;

/**
 * Test that color parser works as expected.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ColorParserTest
{

    /**
     * Test the color parser.
     * @throws NetworkException if that happens uncaught; this test has failed
     */
    @Test
    public final void testColors() throws NetworkException
    {
        assertEquals(Color.RED, ColorParser.parseColor("#ff0000"), "should be Red");
        assertEquals(Color.GREEN, ColorParser.parseColor("#00ff00"), "should be Green");
        assertEquals(Color.BLUE, ColorParser.parseColor("#0000FF"), "should be Blue"); // Try caps as well
        assertEquals(Color.BLACK, ColorParser.parseColor("#0"), "should be Black");
        assertEquals(Color.WHITE, ColorParser.parseColor("#ffffff"), "should be White");

        assertEquals(Color.RED, ColorParser.parseColor("RGB ( 255 , 0 , 0 ) "), "should be Red"); // lots of extra spaces
        assertEquals(Color.GREEN, ColorParser.parseColor("RGB(0,255,0)"), "should be Green");
        assertEquals(Color.BLUE, ColorParser.parseColor("RGB(0,0,255)"), "should be Blue");
        assertEquals(Color.BLACK, ColorParser.parseColor("RGB(0,0,0)"), "should be Black");
        assertEquals(Color.WHITE, ColorParser.parseColor("RGB(255,255,255)"), "should be White");

        assertEquals(Color.BLACK, ColorParser.parseColor("BLACK"), "name should be recognized");
        assertEquals(Color.BLUE, ColorParser.parseColor("BLUE"), "name should be recognized");
        assertEquals(Color.CYAN, ColorParser.parseColor("CYAN"), "name should be recognized");
        assertEquals(Color.DARK_GRAY, ColorParser.parseColor("DARK_GRAY"), "name should be recognized");
        assertEquals(Color.GRAY, ColorParser.parseColor("GRAY"), "name should be recognized");
        assertEquals(Color.GREEN, ColorParser.parseColor("GREEN"), "name should be recognized");
        assertEquals(Color.LIGHT_GRAY, ColorParser.parseColor("LIGHT_GRAY"), "name should be recognized");
        assertEquals(Color.MAGENTA, ColorParser.parseColor("MAGENTA"), "name should be recognized");
        assertEquals(Color.ORANGE, ColorParser.parseColor("ORANGE"), "name should be recognized");
        assertEquals(Color.PINK, ColorParser.parseColor("PINK"), "name should be recognized");
        assertEquals(Color.RED, ColorParser.parseColor("RED"), "name should be recognized");
        assertEquals(Color.WHITE, ColorParser.parseColor("WHITE"), "name should be recognized");
        assertEquals(Color.YELLOW, ColorParser.parseColor("YELLOW"), "name should be recognized");
        try
        {
            ColorParser.parseColor("SOMEWHAT_YELLOWISH");
            fail("unknown color should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            ColorParser.parseColor("#cdefgh");
            fail("Bad hex digit should have thrown a NumberFormatException");
        }
        catch (NumberFormatException nfe)
        {
            // Ignore expected exception
        }
        try
        {
            ColorParser.parseColor("RGB(1,2)");
            fail("Too few values should have thrown an IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException ioobe)
        {
            // Ignore expected Exception
        }
        try
        {
            ColorParser.parseColor("RGB(1,2,)");
            fail("empty last field should have thrown an ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException aioobe)
        {
            // Ignore expected exception
        }
        try
        {
            ColorParser.parseColor("RGB(1,,3)");
            fail("emty middle field should have thrown a NumberFormatException");
        }
        catch (NumberFormatException nfe)
        {
            // Ignore expected exception
        }
        // FIXME: parser gracefully accepts RGB strings without parentheses
        try
        {
            ColorParser.parseColor("RGB(256, 0, 0)");
            fail("R value > 255 should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException nfe)
        {
            // Ignore expected exception
        }
        try
        {
            ColorParser.parseColor("RGB(-1, 0, 0)");
            fail("R value < 0 should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException nfe)
        {
            // Ignore expected exception
        }
    }
}
