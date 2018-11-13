package org.djutils.hexdump;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.djutils.hexdump.HexDump;
import org.junit.Test;

/**
 * Test the method in the HexDump class,
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Nov 13, 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class HexDumpTest
{

    /**
     * Test the HexDump class.
     */
    @Test
    public final void testHexDump()
    {
        assertTrue("Empty input yields empty output", HexDump.hexDump(new byte[] {}).length() == 0);
        byte[] input = new byte[] { 1, 2 };
        String output = HexDump.hexDump(input);
        assertTrue("Output starts with address \"00000000: \"", output.startsWith("00000000: "));
        for (int length = 1; length < 100; length++)
        {
            input = new byte[length];
            assertTrue("Output ends on newline", HexDump.hexDump(input).endsWith("\n"));
        }
        input = new byte[1];
        for (int value = 0; value < 256; value++)
        {
            input[0] = (byte) value;
            output = HexDump.hexDump(input);
            // System.out.print(String.format("%3d -> %s", value, output));
            assertTrue("Output contains hex value of the only input byte embedded between spaces",
                    output.contains(String.format(" %02x ", value)));
        }
        assertEquals("output of 16 byte input fills one lines", 1,
                HexDump.hexDump(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15 }).split("\n").length);
        assertEquals("output of 17 byte input fills two lines", 2,
                HexDump.hexDump(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 }).split("\n").length);
    }

}
