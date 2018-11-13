package org.djutils.hexdump;

/**
 * Create hexadecimal dumps of data.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Nov 13, 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class HexDump
{
    /**
     * Do not instantiate.
     */
    private HexDump()
    {
        // Do not instantiate.
    }

    /**
     * Create a nicely formatted hexadecimal dump of a bunch of bytes.
     * @param bytes byte[]; the bytes to dump
     * @return String; the hex dump of the <code>bytes</code>
     */
    public static String hexDump(final byte[] bytes)
    {
        StringBuilder result = new StringBuilder();
        StringBuilder lhs = new StringBuilder();
        StringBuilder rhs = new StringBuilder();
        for (int pos = 0; pos < bytes.length; pos++)
        {
            byte b = bytes[pos];
            if (pos % 16 == 0)
            {
                if (lhs.length() > 0)
                {
                    result.append(lhs);
                    result.append(rhs);
                    result.append("\n");
                    lhs = new StringBuilder();
                    rhs = new StringBuilder();
                }
                lhs.append(String.format("%08x: ", pos));
            }
            lhs.append(String.format("%02x", b));
            rhs.append(b >= 32 && b < 127 ? ((char) b) : '.');
            if (pos % 8 == 0 && pos % 16 != 0)
            {
                lhs.append("  ");
                rhs.append(" ");
            }
            else
            {
                lhs.append(" ");
            }
        }
        if (lhs.length() > 0)
        {
            for (int pos = bytes.length % 16; pos < 16; pos++)
            {
                lhs.append("   ");
                rhs.append(" ");
                if (8 == pos)
                {
                    lhs.append(" ");
                }
            }
            result.append(lhs);
            result.append(rhs);
        }
        if (result.length() > 0)
        {
            result.append("\n");
        }
        return result.toString();
    }
    
}
