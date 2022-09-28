package org.opentrafficsim.base.compressedfiles;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.zip.GZIPOutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Writer for compressed files.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Oct 24, 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class Writer
{
    /**
     * Class with only static methods should not be instantiated.
     */
    private Writer()
    {
        // Do not instantiate
    }

    /**
     * Construct a new OutputStream that performs a requested data compression.
     * @param fileName String; name of the file that will be created (possible
     * @param compressionType CompressionType; the type of data compression to use
     * @return OutputStream that accepts data to write and should eventually be closed
     * @throws IOException when the requested file could not be opened for writing, or the compression type is unknown
     */
    public static OutputStream createOutputStream(final String fileName, final CompressionType compressionType)
            throws IOException
    {
        if (CompressionType.AUTODETECT.equals(compressionType))
        {
            throw new IOException("AUTODETECT not allowed for Writer");
        }
        String extension = "." + compressionType.getExtension();
        int suffixPos = fileName.length() - extension.length();
        String currentSuffix = suffixPos > 0 ? fileName.substring(suffixPos) : "";
        String fixedFileName =
                (currentSuffix.equalsIgnoreCase(extension) ? fileName.substring(0, suffixPos) : fileName) + extension;
        FileOutputStream fileOutputStream = new FileOutputStream(fixedFileName);
        switch (compressionType)
        {
            case AUTODETECT:
                // Cannot happen
                throw new IOException("AUTODETECT not allowed for Writer");

            case GZIP:
                return new GZIPOutputStream(fileOutputStream);

            case NONE:
                return fileOutputStream;

            case ZIP:
            {
                ZipOutputStream zipOutputStream = new ZipOutputStream(fileOutputStream);
                // Construct the name of the (single) TOC entry from the supplied file name
                int lastSeparatorPos =
                        Math.max(Math.max(fileName.lastIndexOf("/"), fileName.lastIndexOf("\\")), fileName.lastIndexOf(":"));
                String entryName = fileName.substring(lastSeparatorPos + 1);
                zipOutputStream.putNextEntry(new ZipEntry(entryName));
                return zipOutputStream;
            }

            default:
                // Cannot happen
                throw new IOException("Don't know how to create writer for compression type " + compressionType);

        }
    }

}
