package org.djutils.hexdump;

/**
 * Create hexadecimal dumps of data.
 * <br>
 * Possible features to add:
 * <ul>
 * <li>Input from ByteStream</li>
 * <li>User specified address offset (instead of 0)</li> 
 * <li>Custom prefix for each output line</li>
 * <li>Output to OutputStream</li>
 * <li>Output to logger</li>
 * <li>Output with time stamp in prefix of each line</li>
 * <li>Suppression of multiple lines with identical data</li>
 * </ul>
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

    /** Index in an output line where the first hexadecimal digit of the first dumped byte will appear. */
    private static final int HEXOFFSET = 10;

    /** Index in an output line where the character dump of the first dumped byte will appear. */
    private static final int CHAROFFSET = 60;

    /** Prototype output line. */
    private static final String PROTOTYPELINE =
            "                                                                             \n";

    /**
     * Create a nicely formatted hexadecimal dump of a bunch of bytes.
     * @param bytes byte[]; the bytes to dump
     * @return String; the hex dump of the <code>bytes</code>
     */
    public static String hexDump(final byte[] bytes)
    {
        StringBuilder result = new StringBuilder();
        StringBuilder line = new StringBuilder();
        int lineByte = 15;
        for (int pos = 0; pos < bytes.length; pos++)
        {
            byte b = bytes[pos];
            lineByte = pos % 16;
            if (pos % 16 == 0)
            {
                line.setLength(0);
                line.append(PROTOTYPELINE);
                replace(line, 0, String.format("%08x:", pos));
            }
            replace(line, HEXOFFSET + lineByte * 3 + lineByte / 8, String.format("%02x", b));
            replace(line, CHAROFFSET + lineByte * 1 + lineByte / 8, (b >= 32 && b < 127 ? "" + b : "."));
            if (15 == lineByte)
            {
                result.append(line);
            }
        }
        if (lineByte < 15)
        {
            result.append(line);
        }
        return result.toString();
    }

    /**
     * Replace partial contents of a StringBuilder.
     * @param stringBuilder StringBuilder; the StringBuilder
     * @param position int; position in the StringBuilder where replacement starts
     * @param replacement String; text to put at the indicated position (replacing whatever was there)
     */
    private static void replace(final StringBuilder stringBuilder, final int position, final String replacement)
    {
        stringBuilder.replace(position, position + replacement.length(), replacement);
    }

}
