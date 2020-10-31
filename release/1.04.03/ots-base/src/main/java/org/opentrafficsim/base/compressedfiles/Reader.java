package org.opentrafficsim.base.compressedfiles;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.GZIPInputStream;
import java.util.zip.ZipFile;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;

/**
 * Reader for compressed files.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Oct 25, 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class Reader
{
    /**
     * Class with only static methods should not be instantiated.
     */
    private Reader()
    {
        // Do not instantiate.
    }

    /**
     * Construct a InputStream for a compressed data file.
     * @param fileName String; the name of the file
     * @param compressionType CompressionType; the expected type of the data compression in the file
     * @return InputStream that can yield the expanded content of the file.
     * @throws IOException when the file could not be read
     */
    public static InputStream createInputStream(final String fileName, final CompressionType compressionType) throws IOException
    {
        CompressionType useCompressionType =
                CompressionType.AUTODETECT.equals(compressionType) ? autoDetectCompressionType(fileName) : compressionType;
        switch (useCompressionType)
        {
            case AUTODETECT:
                throw new IOException("Cannot happen");

            case BZIP2:
                // BUG create with "true" as second argument: see https://issues.apache.org/jira/browse/COMPRESS-224
                return new BZip2CompressorInputStream(new FileInputStream(fileName), true);

            case GZIP:
                return new GZIPInputStream(new FileInputStream(fileName));

            case NONE:
                return new FileInputStream(fileName);

            case ZIP:
            {
                ZipFile zipFile = new ZipFile(fileName);
                return new ZipInputStream(zipFile, zipFile.getInputStream(zipFile.entries().nextElement()));
            }

            default:
                // Cannot happen
                throw new IOException("Don't know how to create input stream for compression type " + compressionType);

        }
    }

    /**
     * Construct a InputStream for a compressed data file. The type of compression is auto-detected.
     * @param fileName String; the name of the file
     * @return InputStream that can yield the expanded content of the file.
     * @throws IOException when the file can not be opened or read
     */
    public static InputStream createInputStream(final String fileName) throws IOException
    {
        return createInputStream(fileName, CompressionType.AUTODETECT);
    }

    /**
     * Determine the type of compression used in a file.
     * <p>
     * Derived from <a href="http://stackoverflow.com/questions/4818468/how-to-check-if-inputstream-is-gzipped">
     * http://stackoverflow.com/questions/4818468/how-to-check-if-inputstream-is-gzipped</a>. <br>
     * Gzip inflate an inputStream (if it is indeed gzip compressed), otherwise return an InputStream that yields the same data
     * as the <cite>input</cite> argument.
     * @param fileName String; the name of the file to check
     * @return InputStream yielding the inflated data
     * @throws IOException when errors occur reading the signature bytes
     */
    public static CompressionType autoDetectCompressionType(final String fileName) throws IOException
    {
        final int signatureSize = 10;
        BufferedInputStream bufferedInputStream = new BufferedInputStream(new FileInputStream(new File(fileName)));
        byte[] signature = new byte[signatureSize];
        bufferedInputStream.read(signature); // read the signature
        bufferedInputStream.close();
        // for (int i = 0; i < signatureSize; i++)
        // {
        // System.err.println("byte " + i + " is " + String.format("%02x", signature[i]));
        // }
        if (isGZipCompressed(signature))
        {
            return CompressionType.GZIP;
        }
        else if (isBZipCompressed(signature))
        {
            return CompressionType.BZIP2;
        }
        else if (isZipCompressed(signature))
        {
            return CompressionType.ZIP;
        }
        return CompressionType.NONE;
    }

    /**
     * Determine if bytes match the GZip compression signature. Derived from
     * <a href="http://stackoverflow.com/questions/4818468/how-to-check-if-inputstream-is-gzipped">
     * http://stackoverflow.com/questions/4818468/how-to-check-if-inputstream-is-gzipped</a>. <br>
     * Determines if a byte array is compressed. The java.util.zip GZip implementation does not expose the GZip header so it is
     * difficult to determine if a string is compressed.
     * @param bytes byte[]; at least 2 bytes from the start of the stream to determine compression type
     * @return boolean; true if the data appears to be GZip compressed; false otherwise
     * @throws java.io.IOException if the byte array couldn't be read
     */
    public static boolean isGZipCompressed(final byte[] bytes) throws IOException
    {
        return (bytes[0] == (byte) (GZIPInputStream.GZIP_MAGIC)) && (bytes[1] == (byte) (GZIPInputStream.GZIP_MAGIC >> 8));
    }

    /**
     * Determine if bytes match the BZip2 compression signature.
     * @param bytes byte[]; at least 10 bytes from the start of the stream to determine compression type.
     * @return boolean; true if bytes indicates the start of a BZip compressed stream
     */
    private static boolean isBZipCompressed(final byte[] bytes)
    {
        return bytes[0] == 'B' && bytes[1] == 'Z' && (bytes[2] == 'h' || bytes[2] == '0') && Character.isDigit(bytes[3])
                && bytes[4] == 0x31 && bytes[5] == 0x41 && bytes[6] == 0x59 && bytes[7] == 0x26 && bytes[8] == 0x53
                && bytes[9] == 0x59;
    }

    /**
     * Determine if bytes match a ZIP archive signature. Derived from <a href=
     * "https://en.wikipedia.org/wiki/List_of_file_signatures">https://en.wikipedia.org/wiki/List_of_file_signatures</a>.
     * @param bytes byte[]; at least 4 bytes from the start of the stream to determine compression type.
     * @return boolean; true if bytes indicates the start of a ZIP archive; false otherwise
     */
    private static boolean isZipCompressed(final byte[] bytes)
    {
        if (bytes[0] != 0x50 || bytes[1] != 0x4b)
        {
            return false;
        }
        return 0x03 == bytes[2] && 0x04 == bytes[3] || 0x05 == bytes[2] && 0x06 == bytes[3]
                || 0x07 == bytes[2] && 0x08 == bytes[3];
    }

    /**
     * Container for a ZipFile that implements Readable and closes the contained ZipFile on close.
     */
    static class ZipInputStream extends InputStream implements Closeable
    {
        /** The ZipFile that needs to be closed when the input stream is closed. */
        private final ZipFile zipFile;

        /** The input stream. */
        private final InputStream inputStream;

        /**
         * Construct a new ZipInputStream.
         * @param zipFile ZipFile; the opened ZIP file
         * @param inputStream InputStream; input stream of (the first) entry in the ZIP file
         */
        ZipInputStream(final ZipFile zipFile, final InputStream inputStream)
        {
            this.inputStream = inputStream;
            this.zipFile = zipFile;
        }

        /**
         * Close down the reader and release all resources.
         * @throws IOException when closing the reader fails
         */
        @Override
        public void close() throws IOException
        {
            super.close();
            this.zipFile.close();
        }

        /** {@inheritDoc} */
        @Override
        public int read() throws IOException
        {
            return this.inputStream.read();
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "ZipInputStream [zipFile=" + this.zipFile + ", inputStream=" + this.inputStream + "]";
        }

    }

}
