package org.djutils.hexdump;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import org.djutils.logger.CategoryLogger;
import org.junit.Test;

/**
 * Test the method in the HexDump class,
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
     * @throws InterruptedException if that happens; this test has failed.
     * @throws IOException if that happens; this test has failed.
     */
    @Test
    public final void testHexDump() throws InterruptedException, IOException
    {
        assertEquals("Empty input yields empty output", "", HexDump.hexDump(new byte[] {}));
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
        assertTrue("address offset is printed at start of output",
                HexDump.hexDump(0x12345, new byte[] { 0, 1 }).startsWith("00012340"));
        HexDump hd = new HexDump(0x12345);
        assertTrue("toString makes some sense", hd.toString().startsWith("HexDump"));

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        hd = new HexDump().setOutputStream(baos);
        for (int i = 0; i < 100; i++)
        {
            hd.append((byte) i);
            // System.out.println("i=" + i + ", hd=" + hd + " baos=" + baos);
            assertEquals("Number of lines check", Math.max(1, (i + 1) / 16), baos.toString().split("\n").length);
        }
        // System.out.println(hd.getDump());
        for (int i = 33; i < 127; i++)
        {
            String dump = HexDump.hexDump(new byte[] { (byte) i });
            String letter = "" + (char) i;
            String trimmed = dump.trim();
            String lastLetter = trimmed.substring(trimmed.length() - 1);
            // System.out.print("i=" + i + " letter=" + letter + ", output is: " + dump);
            assertEquals("Output ends with the provided printable character", letter, lastLetter);
        }
        baos.reset();
        hd = new HexDump().setPrefixer(HexDump.TIMESTAMPPREFIXER).setOutputStream(baos);
        long startTimeStamp = System.currentTimeMillis();
        hd.append((byte) 10);
        long endTimeStamp = System.currentTimeMillis();
        Thread.sleep(100);
        hd.append((byte) 20);
        String result = baos.toString();
        int spacePosition = result.indexOf(" ");
        long recorded = Long.parseLong(result.substring(0, spacePosition).replace(".", "").replace(",", ""));
        assertTrue("Time stamp should be within interval", startTimeStamp <= recorded);
        assertTrue("Time stamp should be within interval", endTimeStamp >= recorded);
        hd = new HexDump().setOutputStream(new OutputStream()
        {

            @Override
            public void write(final int b) throws IOException
            {
                throw new IOException("testing exception handling");
            }
        });
        try
        {
            hd.append(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 });
            fail("Writing sufficient number of bytes to output that throws an exception should have thrown an exception");
        }
        catch (Exception exception)
        {
            // Ignore expected exception
        }
        baos.reset();
        hd = new HexDump().setOutputStream(baos);
        hd.append(new byte[] { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16 });
        // By now there should be something in the ByteArrayOutputStream
        assertTrue("ByteArrayOutputStream contains start of hex dump", baos.toString().startsWith("00000000: 00 "));
        baos.reset();
        PrintStream oldErrOutput = System.err;
        PrintStream ps = new PrintStream(new BufferedOutputStream(baos));
        System.setErr(ps);
        // Redirect the output to a CategoryLogger
        hd = new HexDump().setOutputStream(new OutputStream()
        {
            /** The string builder. */
            private StringBuilder sb = new StringBuilder();

            @Override
            public void write(final int b) throws IOException
            {
                if ('\n' == b)
                {
                    CategoryLogger.always().error(this.sb.toString());
                    this.sb.setLength(0);
                }
                else
                {
                    this.sb.append((char) b);
                }
            }
        });
        for (int value = 0; value < 256; value++)
        {
            input[0] = (byte) value;
            hd.append(input);
        }
        Thread.sleep(200);
        ps.close();
        System.setErr(oldErrOutput);
        result = baos.toString();
        assertEquals("Result should be 16 lines", 16, result.split("\n").length);
        // System.out.print("baos contains:\n" + result);
        baos.reset();
        hd = new HexDump().setOutputStream(baos).append(new byte[] { 1, 2, 3, 4, 5, 6, 7, 8 }, 4, 2);
        hd.flush();
        result = baos.toString();
        assertTrue("start and length parameter select the correct bytes", result.startsWith("00000000: 05 06    "));
        baos.reset();
        hd = new HexDump().setOutputStream(baos);
        hd.append(new InputStream()
        {
            private int callCount = 0;

            @Override
            public int read() throws IOException
            {
                if (this.callCount < 10)
                {
                    return this.callCount++;
                }
                return -1;
            }
        });
        hd.flush();
        result = baos.toString();
        // System.out.println(result);
        assertTrue("Ten bytes should have been accumulated",
                result.startsWith("00000000: 00 01 02 03 04 05 06 07  08 09    "));
        baos.reset();
        hd = new HexDump().setSuppressMultipleIdenticalLines(true).setOutputStream(baos);
        for (int line = 0; line < 20; line++)
        {
            hd.append(new byte[] { 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57 });
        }
        hd.flush();
        assertEquals("Suppression reduced the output to three lines", 3, baos.toString().split("\n").length);
        // System.out.println(baos);
        baos.reset();
        hd = new HexDump().setSuppressMultipleIdenticalLines(true).setOutputStream(baos);
        for (int line = 0; line < 20; line++)
        {
            hd.append(new byte[] { 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57 });
        }
        hd.append(new byte[] { 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56 });
        hd.flush();
        assertEquals("Suppression reduced the output to four lines", 4, baos.toString().split("\n").length);
        // System.out.println(baos);
        baos.reset();
        hd = new HexDump().setSuppressMultipleIdenticalLines(true).setOutputStream(baos);
        for (int line = 0; line < 20; line++)
        {
            hd.append(new byte[] { 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 57 });
        }
        hd.append(new byte[] { 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55, 56, 99 });
        hd.flush();
        assertEquals("Suppression reduced the output to four lines", 4, baos.toString().split("\n").length);
        System.out.println(baos);
        // FIXME: not exhaustively testing switching compression on and off between append calls.
    }
}
