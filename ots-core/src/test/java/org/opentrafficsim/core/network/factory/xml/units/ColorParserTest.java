package org.opentrafficsim.core.network.factory.xml.units;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.awt.Color;

import org.junit.jupiter.api.Test;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Test that color parser works as expected.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
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
        assertEquals(Color.RED, Colors.parseColor("#ff0000"), "should be Red");
        assertEquals(Color.GREEN, Colors.parseColor("#00ff00"), "should be Green");
        assertEquals(Color.BLUE, Colors.parseColor("#0000FF"), "should be Blue"); // Try caps as well
        assertEquals(Color.BLACK, Colors.parseColor("#0"), "should be Black");
        assertEquals(Color.WHITE, Colors.parseColor("#ffffff"), "should be White");

        assertEquals(Color.RED, Colors.parseColor("RGB ( 255 , 0 , 0 ) "), "should be Red"); // lots of extra spaces
        assertEquals(Color.GREEN, Colors.parseColor("RGB(0,255,0)"), "should be Green");
        assertEquals(Color.BLUE, Colors.parseColor("RGB(0,0,255)"), "should be Blue");
        assertEquals(Color.BLACK, Colors.parseColor("RGB(0,0,0)"), "should be Black");
        assertEquals(Color.WHITE, Colors.parseColor("RGB(255,255,255)"), "should be White");

        assertEquals(Color.BLACK, Colors.parseColor("BLACK"), "name should be recognized");
        assertEquals(Color.BLUE, Colors.parseColor("BLUE"), "name should be recognized");
        assertEquals(Color.CYAN, Colors.parseColor("CYAN"), "name should be recognized");
        assertEquals(Color.DARK_GRAY, Colors.parseColor("DARK_GRAY"), "name should be recognized");
        assertEquals(Color.GRAY, Colors.parseColor("GRAY"), "name should be recognized");
        assertEquals(Color.GREEN, Colors.parseColor("GREEN"), "name should be recognized");
        assertEquals(Color.LIGHT_GRAY, Colors.parseColor("LIGHT_GRAY"), "name should be recognized");
        assertEquals(Color.MAGENTA, Colors.parseColor("MAGENTA"), "name should be recognized");
        assertEquals(Color.ORANGE, Colors.parseColor("ORANGE"), "name should be recognized");
        assertEquals(Color.PINK, Colors.parseColor("PINK"), "name should be recognized");
        assertEquals(Color.RED, Colors.parseColor("RED"), "name should be recognized");
        assertEquals(Color.WHITE, Colors.parseColor("WHITE"), "name should be recognized");
        assertEquals(Color.YELLOW, Colors.parseColor("YELLOW"), "name should be recognized");
        try
        {
            Colors.parseColor("SOMEWHAT_YELLOWISH");
            fail("unknown color should have thrown a NetworkException");
        }
        catch (NetworkException ne)
        {
            // Ignore expected exception
        }
        try
        {
            Colors.parseColor("#cdefgh");
            fail("Bad hex digit should have thrown a NumberFormatException");
        }
        catch (NumberFormatException nfe)
        {
            // Ignore expected exception
        }
        try
        {
            Colors.parseColor("RGB(1,2)");
            fail("Too few values should have thrown an IndexOutOfBoundsException");
        }
        catch (IndexOutOfBoundsException ioobe)
        {
            // Ignore expected Exception
        }
        try
        {
            Colors.parseColor("RGB(1,2,)");
            fail("empty last field should have thrown an ArrayIndexOutOfBoundsException");
        }
        catch (ArrayIndexOutOfBoundsException aioobe)
        {
            // Ignore expected exception
        }
        try
        {
            Colors.parseColor("RGB(1,,3)");
            fail("emty middle field should have thrown a NumberFormatException");
        }
        catch (NumberFormatException nfe)
        {
            // Ignore expected exception
        }
        // FIXME: parser gracefully accepts RGB strings without parentheses
        try
        {
            Colors.parseColor("RGB(256, 0, 0)");
            fail("R value > 255 should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException nfe)
        {
            // Ignore expected exception
        }
        try
        {
            Colors.parseColor("RGB(-1, 0, 0)");
            fail("R value < 0 should have thrown an IllegalArgumentException");
        }
        catch (IllegalArgumentException nfe)
        {
            // Ignore expected exception
        }
    }
}
