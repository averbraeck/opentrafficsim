package org.opentrafficsim.kpi.sampling.csv;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;

import org.opentrafficsim.base.CompressedFileWriter;
import org.opentrafficsim.kpi.sampling.Table;
import org.opentrafficsim.kpi.sampling.serialization.TextSerializationException;

import de.siegmar.fastcsv.writer.LineDelimiter;

/**
 * TsvData takes care of reading and writing of table data in Tab-Separated-Value format. The class can be used, e.g., as
 * follows:
 * 
 * <pre>
 * Table dataTable = new ListTable("data", "dataTable", columns);
 * Writer writer = new FileWriter("c:/data/data.tsv");
 * Writer metaWriter = new FileWriter("c:/data/data.meta.tsv");
 * TsvData.writeData(writer, metaWriter, dataTable);
 * </pre>
 * 
 * Copyright (c) 2020-2023 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://djutils.org" target="_blank"> https://djutils.org</a>. The DJUTILS project is
 * distributed under a three-clause BSD-style license, which can be found at
 * <a href="https://djutils.org/docs/license.html" target="_blank"> https://djutils.org/docs/license.html</a>. <br>
 * @author <a href="https://www.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class TsvData
{
    /**
     * Utility class, no public constructor.
     */
    private TsvData()
    {
        // utility class
    }

    /**
     * Write the data from the data table in TSV format. The writer writes the data, whereas the metaWriter writes the metadata.
     * The metadata consists of a TSV file with three columns: the id, the description, and the class. The first row after the
     * header contains the id, description, and class of the data table itself. The second and further rows contain information
     * about the columns of the data table.
     * @param writer Writer; the writer that writes the data, e.g. to a file
     * @param metaWriter Writer; the writer for the metadata
     * @param dataTable Table; the data table to write
     * @throws IOException on I/O error when writing the data
     * @throws TextSerializationException on unknown data type for serialization
     */
    public static void writeData(final Writer writer, final Writer metaWriter, final Table dataTable)
            throws IOException, TextSerializationException
    {
        CsvData.writeData(writer, metaWriter, dataTable, '\t', '\u0000', LineDelimiter.CRLF);
    }

    /**
     * Write the data from the data table in TSV format.
     * @param filename String; the file name to write the data to
     * @param metaFilename String; the file name to write the metadata to
     * @param dataTable Table; the data table to write
     * @throws IOException on I/O error when writing the data
     * @throws TextSerializationException on unknown data type for serialization
     */
    public static void writeData(final String filename, final String metaFilename, final Table dataTable)
            throws IOException, TextSerializationException
    {
        try (FileWriter fw = new FileWriter(filename); FileWriter mfw = new FileWriter(metaFilename);)
        {
            writeData(fw, mfw, dataTable);
        }
    }

    /**
     * Write the data from the data table in TSV format. The data file and meta data file are zipped. The metadata consists of a
     * TSV file with three columns: the id, the description, and the class. The first row after the header contains the id,
     * description, and class of the data table itself. The second and further rows contain information about the columns of the
     * data table.
     * @param writer Writer; the writer that writes the data, e.g. to a file
     * @param tsvName String; name of the TSV file within the zip file
     * @param metaName String; name of the meta data file within the zip file
     * @param table Table; the data table to write
     * @throws IOException on I/O error when writing the data
     * @throws TextSerializationException on unknown data type for serialization
     */
    public static void writeZippedData(final CompressedFileWriter writer, final String tsvName, final String metaName,
            final Table table) throws IOException, TextSerializationException
    {
        CsvData.writeZippedData(writer, tsvName, metaName, table, '\t', '\u0000', LineDelimiter.CRLF);
    }

    /**
     * Read the data from the TSV-file into the data table. Use the metadata to reconstruct the data table.
     * @param reader Reader; the reader that can read the data, e.g. from a file
     * @param metaReader Reader; the writer for the metadata
     * @return dataTable the data table reconstructed from the meta data and filled with the data
     * @throws IOException on I/O error when reading the data
     * @throws TextSerializationException on unknown data type for serialization
     */
    public static Table readData(final Reader reader, final Reader metaReader) throws IOException, TextSerializationException
    {
        return CsvData.readData(reader, metaReader, '\t', '\u0000');
    }

    /**
     * Read the data from the TSV-file into the data table. Use the metadata to reconstruct the data table.
     * @param filename String; the file name to read the data from
     * @param metaFilename String; the file name to read the metadata from
     * @return dataTable the data table reconstructed from the meta data and filled with the data
     * @throws IOException on I/O error when reading the data
     * @throws TextSerializationException on unknown data type for serialization
     */
    public static Table readData(final String filename, final String metaFilename)
            throws IOException, TextSerializationException
    {
        try (FileReader fr = new FileReader(filename); FileReader mfr = new FileReader(metaFilename);)
        {
            return readData(fr, mfr);
        }
    }

    /**
     * Read the data from a TSV-file inside a zip file. The metadata file should be in the same zipfile. Use the metadata to
     * reconstruct the data table.
     * @param fileName String; file name of the zip file
     * @param tsvName String; name of the TSV-file, without path
     * @param metaName String; name of the metadata file, without path
     * @return Table the data table reconstructed from the meta data and filled with the data
     * @throws IOException when the CSV data was not formatted right
     * @throws TextSerializationException on unknown data type for serialization
     */
    public static Table readZippedData(final String fileName, final String tsvName, final String metaName)
            throws IOException, TextSerializationException
    {
        return CsvData.readZippedData(fileName, tsvName, metaName, '\t', '\u0000');
    }

}
