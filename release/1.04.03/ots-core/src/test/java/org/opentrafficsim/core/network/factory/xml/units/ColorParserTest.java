package org.opentrafficsim.core.network.factory.xml.units;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.awt.Color;

import org.junit.Test;
import org.opentrafficsim.core.network.NetworkException;

/**
 * Test that color parser works as expected.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jan 17, 2017 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
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
        assertEquals("should be Red", Color.RED, Colors.parseColor("#ff0000"));
        assertEquals("should be Green", Color.GREEN, Colors.parseColor("#00ff00"));
        assertEquals("should be Blue", Color.BLUE, Colors.parseColor("#0000FF")); // Try caps as well
        assertEquals("should be Black", Color.BLACK, Colors.parseColor("#0"));
        assertEquals("should be White", Color.WHITE, Colors.parseColor("#ffffff"));

        assertEquals("should be Red", Color.RED, Colors.parseColor("RGB ( 255 , 0 , 0 ) ")); // lots of extra spaces
        assertEquals("should be Green", Color.GREEN, Colors.parseColor("RGB(0,255,0)"));
        assertEquals("should be Blue", Color.BLUE, Colors.parseColor("RGB(0,0,255)"));
        assertEquals("should be Black", Color.BLACK, Colors.parseColor("RGB(0,0,0)"));
        assertEquals("should be White", Color.WHITE, Colors.parseColor("RGB(255,255,255)"));

        assertEquals("name should be recognized", Color.BLACK, Colors.parseColor("BLACK"));
        assertEquals("name should be recognized", Color.BLUE, Colors.parseColor("BLUE"));
        assertEquals("name should be recognized", Color.CYAN, Colors.parseColor("CYAN"));
        assertEquals("name should be recognized", Color.DARK_GRAY, Colors.parseColor("DARK_GRAY"));
        assertEquals("name should be recognized", Color.GRAY, Colors.parseColor("GRAY"));
        assertEquals("name should be recognized", Color.GREEN, Colors.parseColor("GREEN"));
        assertEquals("name should be recognized", Color.LIGHT_GRAY, Colors.parseColor("LIGHT_GRAY"));
        assertEquals("name should be recognized", Color.MAGENTA, Colors.parseColor("MAGENTA"));
        assertEquals("name should be recognized", Color.ORANGE, Colors.parseColor("ORANGE"));
        assertEquals("name should be recognized", Color.PINK, Colors.parseColor("PINK"));
        assertEquals("name should be recognized", Color.RED, Colors.parseColor("RED"));
        assertEquals("name should be recognized", Color.WHITE, Colors.parseColor("WHITE"));
        assertEquals("name should be recognized", Color.YELLOW, Colors.parseColor("YELLOW"));
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
