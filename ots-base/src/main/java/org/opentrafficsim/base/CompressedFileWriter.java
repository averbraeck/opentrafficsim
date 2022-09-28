package org.opentrafficsim.base;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * File writer.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public final class CompressedFileWriter
{

    /**
     * Constructor.
     */
    private CompressedFileWriter()
    {
        // utility
    }

    /**
     * Returns a {@code BufferedWriter} to a file, zipped or not.
     * @param file String; file
     * @param zipped boolean; zipped or not
     * @return BufferedWriter
     */
    public static BufferedWriter create(final String file, final boolean zipped)
    {
        String name = null;
        String file2 = file;
        if (zipped)
        {
            File f = new File(file);
            name = f.getName();
            if (!file.endsWith(".zip"))
            {
                file2 += ".zip";
            }
        }
        try
        {
            FileOutputStream fos = new FileOutputStream(file2);
            OutputStreamWriter osw;
            if (zipped)
            {
                ZipOutputStream zos = new ZipOutputStream(fos);
                zos.putNextEntry(new ZipEntry(name));
                osw = new OutputStreamWriter(zos);
            }
            else
            {
                osw = new OutputStreamWriter(fos);
            }
            return new BufferedWriter(osw);
        }
        catch (IOException exception)
        {
            throw new RuntimeException("Could not write to file.", exception);
        }
    }

}
