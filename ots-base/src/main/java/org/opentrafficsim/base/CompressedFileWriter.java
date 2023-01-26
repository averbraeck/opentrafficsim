package org.opentrafficsim.base;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * File writer for multiple files in to a zip file. Typical use is:
 * 
 * <pre>
 * try (CompressedFileWriter compressedFileWriter = new CompressedFileWriter("CsvData.zip"))
 * {
 *     BufferedWriter bufferedWriter = compressedFileWriter.next("data_2023.csv");
 *     
 *     // write data for data_2023
 *     bufferedWriter.write(...);
 *     
 *     compressedFileWriter.next("data_2024.csv");
 *     
 *     // write data for data_2024
 *     bufferedWriter.write(...);
 * }
 * </pre>
 * 
 * If the {@code BufferedWriter} is closed, so too is the {@code CompressedFileWriter}. Any consumers of the
 * {@code BufferedWriter} should thus not close it.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public final class CompressedFileWriter implements AutoCloseable
{

    /** Zip output stream to create new zip entries. */
    private final ZipOutputStream zipOutputStream;

    /** Buffered writer to write in to. */
    private BufferedWriter bufferedWriter;

    /**
     * Constructor.
     * @param file String; file, if this does not end with .zip (case insensitive), ".zip" will be appended to it
     * @throws FileNotFoundException if the zip file can not be written
     */
    public CompressedFileWriter(final String file) throws FileNotFoundException
    {
        this.zipOutputStream =
                new ZipOutputStream(new FileOutputStream(file.toLowerCase().endsWith(".zip") ? file : file + ".zip"));
        this.bufferedWriter = new BufferedWriter(new OutputStreamWriter(this.zipOutputStream));
    }

    /**
     * Closes the previous file in the zip file, and opens up the next file. The {@code BufferedWriter} returned is the same for
     * each call on a {@code CompressedFileWriter}.
     * @param name String; name of the nex file in the zip file
     * @return BufferedWriter; writer to write the next file in to.
     * @throws IOException if no next entry could be created in the zip file
     */
    public BufferedWriter next(final String name) throws IOException
    {
        if (this.bufferedWriter != null)
        {
            this.bufferedWriter.flush();
        }
        this.zipOutputStream.putNextEntry(new ZipEntry(name));
        return this.bufferedWriter;
    }

    /** {@inheritDoc} */
    @Override
    public void close() throws IOException
    {
        if (this.bufferedWriter != null)
        {
            this.bufferedWriter.flush();
        }
        this.zipOutputStream.close();
    }

    /**
     * Creates a writer to write to a file. This file may or may not be inside a zip file. In particular if
     * {@code zipped = true}, then with {@code file = "myFile.csv"}, a file {@code myFile.csv.zip} will result in which a file
     * {@code myFile.csv} is located. Writing occurs on this file.
     * @param file String; file
     * @param zipped boolean; whether to contain the file in a zip file
     * @return BufferedWriter writer tot write in to
     */
    public static BufferedWriter create(final String file, final boolean zipped)
    {
        try
        {
            if (zipped)
            {
                ZipOutputStream zipOutputStream =
                        new ZipOutputStream(new FileOutputStream(file.toLowerCase().endsWith(".zip") ? file : file + ".zip"));
                String name = new File(file).getName();
                name = name.toLowerCase().endsWith(".zip") ? name.substring(0, name.length() - 4) : name;
                zipOutputStream.putNextEntry(new ZipEntry(name));
                return new BufferedWriter(new OutputStreamWriter(zipOutputStream));
            }
            return new BufferedWriter(new FileWriter(file));
        }
        catch (IOException exception)
        {
            throw new RuntimeException(exception);
        }
    }

}
