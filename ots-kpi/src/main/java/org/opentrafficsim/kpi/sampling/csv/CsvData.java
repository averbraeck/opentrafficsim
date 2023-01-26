package org.opentrafficsim.kpi.sampling.csv;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.djutils.exceptions.Throw;
import org.djutils.primitives.Primitive;
import org.opentrafficsim.base.CompressedFileWriter;
import org.opentrafficsim.kpi.sampling.Column;
import org.opentrafficsim.kpi.sampling.ListTable;
import org.opentrafficsim.kpi.sampling.Row;
import org.opentrafficsim.kpi.sampling.Table;
import org.opentrafficsim.kpi.sampling.serialization.TextSerializationException;
import org.opentrafficsim.kpi.sampling.serialization.TextSerializer;

import de.siegmar.fastcsv.reader.NamedCsvReader;
import de.siegmar.fastcsv.reader.NamedCsvRow;
import de.siegmar.fastcsv.writer.CsvWriter;
import de.siegmar.fastcsv.writer.LineDelimiter;

/**
 * CsvData takes care of reading and writing of table data in CSV format. The class can be used, e.g., as follows:
 * 
 * <pre>
 * Table Table = new ListTable("data", "Table", columns);
 * Writer writer = new FileWriter("c:/data/data.csv");
 * Writer metaWriter = new FileWriter("c:/data/data.meta.csv");
 * CsvData.writeData(writer, metaWriter, Table);
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
public final class CsvData
{
    /**
     * Utility class, no public constructor.
     */
    private CsvData()
    {
        // utility class
    }

    /**
     * Write the data from the data table in CSV format. The writer writes the data, whereas the metaWriter writes the metadata.
     * The metadata consists of a CSV file with three columns: the id, the description, and the class. The first row after the
     * header contains the id, description, and class of the data table itself. The second and further rows contain information
     * about the columns of the data table.
     * @param writer Writer; the writer that writes the data, e.g. to a file
     * @param metaWriter Writer; the writer for the metadata
     * @param table Table; the data table to write
     * @param separator char; the delimiter to use for separating entries
     * @param quotechar char; the character to use for quoted elements
     * @param lineDelimiter String; the line terminator to use, can be LineDelimiter.CR, LF, CRLF or PLATFORM
     * @throws IOException on I/O error when writing the data
     * @throws TextSerializationException on unknown data type for serialization
     */
    public static void writeData(final Writer writer, final Writer metaWriter, final Table table, final char separator,
            final char quotechar, final LineDelimiter lineDelimiter) throws IOException, TextSerializationException
    {
        writeMeta(metaWriter, true, table, separator, quotechar, lineDelimiter);
        writeData(writer, true, table, separator, quotechar, lineDelimiter);
    }

    /**
     * Write the data from the data table in CSV format. The data file and meta data file are zipped. The metadata consists of a
     * CSV file with three columns: the id, the description, and the class. The first row after the header contains the id,
     * description, and class of the data table itself. The second and further rows contain information about the columns of the
     * data table.
     * @param writer Writer; the writer that writes the data, e.g. to a file
     * @param csvName String; name of the csv file within the zip file
     * @param metaName String; name of the meta data file within the zip file
     * @param table Table; the data table to write
     * @param separator char; the delimiter to use for separating entries
     * @param quotechar char; the character to use for quoted elements
     * @param lineDelimiter String; the line terminator to use, can be LineDelimiter.CR, LF, CRLF or PLATFORM
     * @throws IOException on I/O error when writing the data
     * @throws TextSerializationException on unknown data type for serialization
     */
    public static void writeZippedData(final CompressedFileWriter writer, final String csvName, final String metaName,
            final Table table, final char separator, final char quotechar, final LineDelimiter lineDelimiter)
            throws IOException, TextSerializationException
    {
        writeMeta(writer.next(metaName), false, table, separator, quotechar, lineDelimiter);
        writeData(writer.next(csvName), true, table, separator, quotechar, lineDelimiter);
    }

    /**
     * Write the data from the data table in CSV format. The data file and meta data file are zipped. The metadata consists of a
     * CSV file with three columns: the id, the description, and the class. The first row after the header contains the id,
     * description, and class of the data table itself. The second and further rows contain information about the columns of the
     * data table.
     * @param writer Writer; the writer that writes the data, e.g. to a file
     * @param csvName String; name of the CSV file within the zip file
     * @param metaName String; name of the meta data file within the zip file
     * @param table Table; the data table to write
     * @throws IOException on I/O error when writing the data
     * @throws TextSerializationException on unknown data type for serialization
     */
    public static void writeZippedData(final CompressedFileWriter writer, final String csvName, final String metaName,
            final Table table) throws IOException, TextSerializationException
    {
        writeZippedData(writer, csvName, metaName, table, ',', '"', LineDelimiter.CRLF);
    }

    /**
     * Writes the meta data.
     * @param metaWriter Writer; the writer for the metadata
     * @param closeWriter boolean; whether to close the stream
     * @param table Table; the data table to write
     * @param separator char; the delimiter to use for separating entries
     * @param quotechar char; the character to use for quoted elements
     * @param lineDelimiter String; the line terminator to use, can be LineDelimiter.CR, LF, CRLF or PLATFORM
     * @throws IOException on I/O error when writing the data
     */
    private static void writeMeta(final Writer metaWriter, final boolean closeWriter, final Table table, final char separator,
            final char quotechar, final LineDelimiter lineDelimiter) throws IOException
    {
        CsvWriter csvMetaWriter = null;
        try
        {
            csvMetaWriter = CsvWriter.builder().fieldSeparator(separator).quoteCharacter(quotechar).lineDelimiter(lineDelimiter)
                    .build(metaWriter);
            csvMetaWriter.writeRow("id", "description", "className", "unit");
            csvMetaWriter.writeRow(table.getId(), table.getDescription(), table.getClass().getName(), "");
            for (Column<?> column : table.getColumns())
            {
                if (column.getUnit() == null)
                {
                    csvMetaWriter.writeRow(column.getId(), column.getDescription(), column.getValueType().getName(), "");
                }
                else
                {
                    csvMetaWriter.writeRow(column.getId(), column.getDescription(), column.getValueType().getName(),
                            column.getUnit());
                }
            }
        }
        finally
        {
            if (closeWriter && csvMetaWriter != null)
            {
                csvMetaWriter.close();
            }
        }
    }

    /**
     * Writes the data.
     * @param writer Writer; the writer that writes the data, e.g. to a file
     * @param closeWriter boolean; whether to close the stream
     * @param table Table; the data table to write
     * @param separator char; the delimiter to use for separating entries
     * @param quotechar char; the character to use for quoted elements
     * @param lineDelimiter String; the line terminator to use, can be LineDelimiter.CR, LF, CRLF or PLATFORM
     * @throws IOException on I/O error when writing the data
     * @throws TextSerializationException on unknown data type for serialization
     */
    private static void writeData(final Writer writer, final boolean closeWriter, final Table table, final char separator,
            final char quotechar, final LineDelimiter lineDelimiter) throws IOException, TextSerializationException
    {
        // Assemble the serializer array
        TextSerializer<?>[] serializers = new TextSerializer[table.getNumberOfColumns()];
        for (int i = 0; i < table.getNumberOfColumns(); i++)
        {
            Column<?> column = table.getColumns().get(i);
            serializers[i] = TextSerializer.resolve(column.getValueType());
        }

        // Write the data file
        CsvWriter csvWriter = null;
        try
        {
            csvWriter = CsvWriter.builder().fieldSeparator(separator).quoteCharacter(quotechar).lineDelimiter(lineDelimiter)
                    .build(writer);
            csvWriter.writeRow(table.getColumnIds());
            String[] textFields = new String[table.getNumberOfColumns()];
            for (Row row : table)
            {
                Object[] values = row.getValues();
                for (int i = 0; i < table.getNumberOfColumns(); i++)
                {
                    textFields[i] = TextSerializer.serialize(serializers[i], values[i]);
                }
                csvWriter.writeRow(textFields);
            }
        }
        finally
        {
            if (closeWriter && csvWriter != null)
            {
                csvWriter.close();
            }
        }
    }

    /**
     * Write the data from the data table in CSV format. The writer writes the data, whereas the metaWriter writes the metadata.
     * The metadata consists of a CSV file with three columns: the id, the description, and the class. The first row after the
     * header contains the id, description, and class of the data table itself. The second and further rows contain information
     * about the columns of the data table. The line ending used will be CRLF which is RFC 4180 compliant.
     * @param writer Writer; the writer that writes the data, e.g. to a file
     * @param metaWriter Writer; the writer for the metadata
     * @param table Table; the data table to write
     * @throws IOException on I/O error when writing the data
     * @throws TextSerializationException on unknown data type for serialization
     */
    public static void writeData(final Writer writer, final Writer metaWriter, final Table table)
            throws IOException, TextSerializationException
    {
        writeData(writer, metaWriter, table, ',', '"', LineDelimiter.CRLF);
    }

    /**
     * Write the data from the data table in CSV format.
     * @param filename String; the file name to write the data to
     * @param metaFilename String; the file name to write the metadata to
     * @param table Table; the data table to write
     * @throws IOException on I/O error when writing the data
     * @throws TextSerializationException on unknown data type for serialization
     */
    public static void writeData(final String filename, final String metaFilename, final Table table)
            throws IOException, TextSerializationException
    {
        try (FileWriter fw = new FileWriter(filename); FileWriter mfw = new FileWriter(metaFilename);)
        {
            writeData(fw, mfw, table);
        }
    }

    /**
     * Read the data from the CSV-file into the data table. Use the metadata to reconstruct the data table.
     * @param reader Reader; the reader that can read the data, e.g. from a file
     * @param metaReader Reader; the writer for the metadata
     * @return Table the data table reconstructed from the meta data and filled with the data
     * @param separator char; the delimiter to use for separating entries
     * @param quotechar char; the character to use for quoted elements
     * @throws IOException when the CSV data was not formatted right
     * @throws TextSerializationException on unknown data type for serialization
     */
    public static Table readData(final Reader reader, final Reader metaReader, final char separator, final char quotechar)
            throws IOException, TextSerializationException
    {
        // Read the metadata file and reconstruct the data table
        try (NamedCsvReader csvMetaReader =
                NamedCsvReader.builder().fieldSeparator(separator).quoteCharacter(quotechar).build(metaReader))
        {
            Set<String> metaHeader = csvMetaReader.getHeader();
            Throw.when(
                    metaHeader.size() != 4 || !metaHeader.contains("id") || !metaHeader.contains("description")
                            || !metaHeader.contains("className") || !metaHeader.contains("unit"),
                    IOException.class,
                    "header of the metafile does not contain 'id, description, className, unit' as fields, but %s: ",
                    metaHeader);

            // table metadata
            List<Column<?>> columns = new ArrayList<>();
            Map<String, String> tableRow = new LinkedHashMap<>();
            Iterator<NamedCsvRow> it = csvMetaReader.iterator();
            while (it.hasNext())
            {
                NamedCsvRow row = it.next();
                // table metadata
                if (tableRow.size() == 0)
                {
                    tableRow.putAll(row.getFields());
                }
                else
                {
                    // column metadata
                    String type = row.getField("className");
                    Class<?> valueClass = Primitive.forName(type);
                    if (valueClass == null)
                    {
                        try
                        {
                            valueClass = Class.forName(type);
                        }
                        catch (ClassNotFoundException exception)
                        {
                            throw new IOException("Could not find class " + type, exception);
                        }
                    }
                    Column<?> column =
                            new Column<>(row.getField("id"), row.getField("description"), valueClass, row.getField("unit"));
                    columns.add(column);
                }
            }

            Throw.when(tableRow == null, IOException.class, "no table information in the metafile");

            // create table
            Table table;
            Consumer<Object[]> unserializableTable;
            if (tableRow.get("className").equals(ListTable.class.getName()))
            {
                ListTable listTable = new ListTable(tableRow.get("id"), tableRow.get("description"), columns);
                table = listTable;
                unserializableTable = listTable;
            }
            else
            {
                // fallback
                ListTable listTable = new ListTable(tableRow.get("id"), tableRow.get("description"), columns);
                table = listTable;
                unserializableTable = listTable;
            }

            // Assemble the serializer array
            TextSerializer<?>[] serializers = new TextSerializer[table.getNumberOfColumns()];
            for (int i = 0; i < table.getNumberOfColumns(); i++)
            {
                serializers[i] = TextSerializer.resolve(columns.get(i).getValueType());
            }

            // Read the data file
            try (NamedCsvReader csvReader =
                    NamedCsvReader.builder().fieldSeparator(separator).quoteCharacter(quotechar).build(reader))
            {
                Set<String> header = csvReader.getHeader();
                Throw.when(header.size() != columns.size(), IOException.class,
                        "Number of columns in the data file does not match column metadata size");
                for (int i = 0; i < columns.size(); i++)
                {
                    Throw.when(!header.contains(columns.get(i).getId()), IOException.class,
                            "Header with id %s not found in the data file", columns.get(i).getId());
                }

                // Read the data file records
                csvReader.forEach(row ->
                {
                    Object[] values = new Object[columns.size()];
                    for (int i = 0; i < columns.size(); i++)
                    {
                        values[i] = TextSerializer.deserialize(serializers[i], row.getField(columns.get(i).getId()),
                                columns.get(i));
                    }
                    unserializableTable.accept(values); // addRow
                });
                return table;
            }
        }
    }

    /**
     * Read the data from the CSV-file into the data table. Use the metadata to reconstruct the data table.
     * @param reader Reader; the reader that can read the data, e.g. from a file
     * @param metaReader Reader; the writer for the metadata
     * @return Table the data table reconstructed from the meta data and filled with the data
     * @throws IOException when the CSV data was not formatted right
     * @throws TextSerializationException on unknown data type for serialization
     */
    public static Table readData(final Reader reader, final Reader metaReader) throws IOException, TextSerializationException
    {
        return readData(reader, metaReader, ',', '"');
    }

    /**
     * Read the data from the CSV-file into the data table. Use the metadata to reconstruct the data table.
     * @param filename String; the file name to read the data from
     * @param metaFilename String; the file name to read the metadata from
     * @return Table the data table reconstructed from the meta data and filled with the data
     * @throws IOException when the CSV data was not formatted right
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
     * Read the data from a CSV-file inside a zip file. The metadata file should be in the same zipfile. Use the metadata to
     * reconstruct the data table.
     * @param fileName String; file name of the zip file
     * @param csvName String; name of the CSV-file, without path
     * @param metaName String; name of the metadata file, without path
     * @return Table the data table reconstructed from the meta data and filled with the data
     * @throws IOException when the CSV data was not formatted right
     * @throws TextSerializationException on unknown data type for serialization
     */
    public static Table readZippedData(final String fileName, final String csvName, final String metaName)
            throws IOException, TextSerializationException
    {
        return readZippedData(fileName, csvName, metaName, ',', '"');
    }

    /**
     * Read the data from a CSV-file inside a zip file. The metadata file should be in the same zipfile. Use the metadata to
     * reconstruct the data table.
     * @param fileName String; file name of the zip file
     * @param csvName String; name of the CSV-file, without path
     * @param metaName String; name of the metadata file, without path
     * @param separator char; the delimiter to use for separating entries
     * @param quotechar char; the character to use for quoted elements
     * @return Table the data table reconstructed from the meta data and filled with the data
     * @throws IOException when the CSV data was not formatted right
     * @throws TextSerializationException on unknown data type for serialization
     */
    public static Table readZippedData(final String fileName, final String csvName, final String metaName, final char separator,
            final char quotechar) throws IOException, TextSerializationException
    {
        try (ZipFile zipFile = new ZipFile(fileName))
        {
            Reader reader = null;
            Reader metaReader = null;
            Iterator<? extends ZipEntry> iterator = zipFile.entries().asIterator();
            while (iterator.hasNext())
            {
                ZipEntry zipEntry = iterator.next();
                if (zipEntry.getName().equals(csvName))
                {
                    reader = new InputStreamReader(zipFile.getInputStream(zipEntry));
                }
                else if (zipEntry.getName().equals(metaName))
                {
                    metaReader = new InputStreamReader(zipFile.getInputStream(zipEntry));
                }
            }
            Throw.whenNull(reader, "File %s not found in %s.", csvName, fileName);
            Throw.whenNull(metaReader, "File %s not found in %s.", metaName, fileName);
            return readData(reader, metaReader, separator, quotechar);
        }
    }

}
