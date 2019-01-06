package org.djutils.hexdump;

import java.io.ByteArrayOutputStream;
import java.io.Flushable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Create hexadecimal dumps of data. <br>
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Nov 13, 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class HexDump implements Flushable
{

    /** Index in an output line (excluding prefix) where the first hexadecimal digit of the first dumped byte will appear. */
    private static final int HEXOFFSET = 10;

    /** Index in an output line (excluding prefix) where the character dump of the first dumped byte will appear. */
    private static final int CHAROFFSET = 60;

    /** Prototype output line. */
    private static final String PROTOTYPELINE =
            "                                                                             \n";

    /** Counter for address field. */
    private int nextAddress;

    /** Output stream for completed output lines. */
    private OutputStream outputStream = System.out;

    /** String builder for current output line. */
    private StringBuilder line = new StringBuilder();

    /** If true, 3 or more output lines containing the same 16 bytes are compressed. */
    private boolean suppressMultipleIdenticalLines = false;

    /** Number of identical lines in output. */
    private int suppressedCount = 0;

    /** Used in conjunction with <code>suppressMultipleIdenticalLines</code>. */
    private String lastPattern = PROTOTYPELINE; // Initial content must be at least HEXOFFSET characters long

    /** Output line PrefixGenerator that outputs the empty string. */
    public static final PrefixGenerator EMPTYPREFIXER = new PrefixGenerator()
    {

        @Override
        public String generatePrefix()
        {
            return "";
        }
    };

    /** Output line PrefixGenerator that outputs a simple time stamp. */
    public static final PrefixGenerator TIMESTAMPPREFIXER = new PrefixGenerator()
    {

        @Override
        public String generatePrefix()
        {
            long now = System.currentTimeMillis();
            return String.format("%d.%03d ", now / 1000, now % 1000);
        }
    };

    /** Currently active generator for prefix of the output lines. */
    private PrefixGenerator prefixer = EMPTYPREFIXER;

    /**
     * Construct a new HexDump object.
     * @param addressOffset int; the address of the first byte
     */
    public HexDump(final int addressOffset)
    {
        this.nextAddress = addressOffset;
    }

    /**
     * Construct a new HexDump object with initial address 0.
     */
    public HexDump()
    {
        this(0);
    }

    /**
     * Set or replace the active output line prefix generator.
     * @param newPrefixer PrefixGenerator; the new PrefixGenerator to use
     * @return HexDump; this HexDump object (for method chaining)
     */
    public HexDump setPrefixer(final PrefixGenerator newPrefixer)
    {
        this.prefixer = newPrefixer;
        return this;
    }

    /**
     * Set or replace the active output stream. (The default output stream is <code>System.out</code>.)
     * @param newOutputStream OutputStream; the new output stream
     * @return HexDump; this HexDump object (for method chaining)
     */
    public HexDump setOutputStream(final OutputStream newOutputStream)
    {
        this.outputStream = newOutputStream;
        return this;
    }

    /**
     * Set the output compression mode.
     * @param newState boolean; if true; groups of three or more output lines with the same 16 bytes are compressed; if false;
     *            no output is suppressed
     * @return HexDump; this HexDump object (for method chaining)
     */
    public HexDump setSuppressMultipleIdenticalLines(final boolean newState)
    {
        this.suppressMultipleIdenticalLines = newState;
        return this;
    }

    /**
     * Write some output.
     * @param outputText String; text to write.
     * @throws IOException when an outputStream has been set and it throws an IOException
     */
    private void writeOutput(final String outputText) throws IOException
    {
        this.outputStream.write(outputText.getBytes());
    }

    /**
     * Write some output, applying suppression of multiple lines with the same dumped bytes (if that option is active).
     * @param outputText String; text to write.
     * @throws IOException when an outputStream has been set and it throws an IOException
     */
    private void writeFilteringOutput(final String outputText) throws IOException
    {
        if (outputText.length() == 0)
        {
            return;
        }
        String dataPattern = outputText;
        if (this.suppressedCount > 0
                && ((!this.suppressMultipleIdenticalLines) || (!dataPattern.substring(HEXOFFSET).equals(
                        this.lastPattern.substring(HEXOFFSET)))))
        {
            if (!outputText.equals(this.lastPattern))
            {
                this.outputStream.write(this.lastPattern.getBytes());
            }
            this.suppressedCount = 0;
        }
        if ((!this.suppressMultipleIdenticalLines)
                || (!dataPattern.substring(HEXOFFSET).equals(this.lastPattern.substring(HEXOFFSET))))
        {
            this.outputStream.write(outputText.getBytes());
        }
        else
        {
            // Suppress output
            if (1 == this.suppressedCount++)
            {
                this.outputStream.write("*\n".getBytes());
            }
        }
        this.lastPattern = dataPattern;
    }

    /**
     * Append one byte to this HexDump.
     * @param b byte; the byte to add
     * @return HexDump; this HexDump object (for method chaining)
     * @throws IOException when an outputStream has been set and it throws an IOException
     */
    public HexDump append(final byte b) throws IOException
    {
        int lineByte = this.nextAddress % 16;
        if (this.line.length() == 0)
        {
            this.line.append(PROTOTYPELINE);
            replace(0, String.format("%07x0:", this.nextAddress / 16));
            writeOutput(this.prefixer.generatePrefix());
        }
        replace(HEXOFFSET + lineByte * 3 + lineByte / 8, String.format("%02x", b));
        replace(CHAROFFSET + lineByte * 1 + lineByte / 8, (b >= 32 && b < 127 ? "" + (char) b : "."));
        if (15 == lineByte)
        {
            writeFilteringOutput(this.line.toString());
            this.line.setLength(0);
        }
        this.nextAddress++;
        return this;
    }

    /**
     * Append an array of bytes.
     * @param bytes byte[]; the bytes to append
     * @return HexDump; this HexDump object (for method chaining)
     * @throws IOException when an outputStream has been set and it throws an IOException
     */
    public HexDump append(final byte[] bytes) throws IOException
    {
        return append(bytes, 0, bytes.length);
    }

    /**
     * Append a slice of an array of bytes.
     * @param bytes byte[]; byte array from which to take the bytes to append
     * @param start int; index of first byte in <code>bytes</code> to append (NB. using non-zero does <b>not</b> cause a jump in
     *            the address that is printed before the dumped bytes)
     * @param len int; number of bytes to append
     * @return HexDump; this HexDump object (for method chaining)
     * @throws IOException when an outputStream has been set and it throws an IOException
     */
    public HexDump append(final byte[] bytes, final int start, final int len) throws IOException
    {
        for (int pos = start; pos < start + len; pos++)
        {
            append(bytes[pos]);
        }
        return this;
    }

    /**
     * Consume an entire input stream and append what it produces to this HexDump. The input stream is <b>not</b> closed by this
     * <code>append</code> method. This method does not return until the <code>inputStream</code> returns end of file, or throws
     * an IOException (which is - actually - not a return to the caller).
     * @param inputStream InputStream; the input stream that is to be consumed
     * @return HexDump; this HexDump object (for method chaining)
     * @throws IOException when the <code>inputStream</code> throws that exception, or when an output stream has been set and
     *             that throws an IOException
     */
    public HexDump append(final InputStream inputStream) throws IOException
    {
        byte[] buffer = new byte[8192];
        int read;
        while ((read = inputStream.read(buffer)) >= 0)
        {
            append(buffer, 0, read);
        }
        return this;
    }

    /** {@inheritDoc} */
    @Override
    public void flush() throws IOException
    {
        boolean saveSuppress = this.suppressMultipleIdenticalLines;
        this.suppressMultipleIdenticalLines = false;
        if (this.suppressedCount > 0)
        {
            writeFilteringOutput(this.lastPattern);
        }
        writeFilteringOutput(this.line.toString());
        this.suppressMultipleIdenticalLines = saveSuppress;
        this.line.setLength(0);
    }

    /**
     * Replace partial contents of a StringBuilder.
     * @param position int; position in the StringBuilder where replacement starts
     * @param replacement String; text to put at the indicated position (replacing whatever was there)
     */
    private void replace(final int position, final String replacement)
    {
        this.line.replace(position, position + replacement.length(), replacement);
    }

    /**
     * Create a HexDump object; use it to dump an array of bytes and return the dump as a String.
     * @param addressOffset int; address of the first byte
     * @param bytes byte[]; the bytes to hex-dump
     * @return String; the hexadecimal dump of the <code>bytes</code>
     */
    public static String hexDump(final int addressOffset, final byte[] bytes)
    {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try
        {
            new HexDump(addressOffset).setOutputStream(baos).append(bytes).flush();
        }
        catch (IOException exception)
        {
            // Cannot happen because ByteOutputStream.write(byte[]) cannot fail
        }
        return baos.toString();
    }

    /**
     * Create a HexDump object with addressOffset 0; use it to dump an array of bytes and return the dump as a String.
     * @param bytes byte[]; the bytes to hex-dump
     * @return String; the hexadecimal dump of the <code>bytes</code>
     */
    public static String hexDump(final byte[] bytes)
    {
        return hexDump(0, bytes);
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "HexDump [nextAddress=" + this.nextAddress + ", outputStream=" + this.outputStream + "]";
    }

    /**
     * Interface defining a method that generates a prefix for an output line of the HexDump class.
     */
    interface PrefixGenerator
    {
        /**
         * Return a prefix for the next HexDump output line; e.g. a time stamp. This method is called when the HexDump object
         * handles the first byte of an output line.
         * @return String; the prefix for the next HexDump output line. If the result is not the empty string; it should
         *         probably end with space to separate the prefix from the address field.
         */
        String generatePrefix();
    }

}
