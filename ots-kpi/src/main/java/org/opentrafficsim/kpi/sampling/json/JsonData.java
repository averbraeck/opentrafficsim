package org.opentrafficsim.kpi.sampling.json;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.function.Consumer;

import org.djutils.exceptions.Throw;
import org.djutils.primitives.Primitive;
import org.opentrafficsim.kpi.sampling.Column;
import org.opentrafficsim.kpi.sampling.ListTable;
import org.opentrafficsim.kpi.sampling.Row;
import org.opentrafficsim.kpi.sampling.Table;
import org.opentrafficsim.kpi.sampling.serialization.TextSerializationException;
import org.opentrafficsim.kpi.sampling.serialization.TextSerializer;

import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;

/**
 * JsonData takes care of reading and writing of table data in JSON format. The reader and writer use a streaming API to avoid
 * excessive memory use. The class can be used, e.g., as follows:
 * 
 * <pre>
 * Table dataTable = new ListTable("data", "dataTable", columns);
 * Writer writer = new FileWriter("c:/data/data.json");
 * JsonData.writeData(writer, dataTable);
 * </pre>
 * 
 * The JSON document has the following structure:
 * 
 * <pre>
 * {
 * &nbsp;&nbsp;"table": {
 * &nbsp;&nbsp;&nbsp;&nbsp;"id": "tableId",
 * &nbsp;&nbsp;&nbsp;&nbsp;"description": "table description",
 * &nbsp;&nbsp;&nbsp;&nbsp;"class": "org.djutils.data.ListTable"",
 * &nbsp;&nbsp;&nbsp;&nbsp;"columns": [
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"nr": "0",
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"id": "time",
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"description": "time in [s]",
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"class": "org.djtils.vdouble.scalar.Time",
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;},
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"nr": "1",
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"id": "value",
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"description": "value [cm]",
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"class": "double",
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;},
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;{
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"nr": "2",
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"id": "comment",
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"description": "comment",
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;"class": "java.lang.String",
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;},
 * &nbsp;&nbsp;&nbsp;&nbsp;] 
 * &nbsp;&nbsp;},
 * &nbsp;&nbsp;"data": [
 * &nbsp;&nbsp;&nbsp;&nbsp;[ { "0" : "2" }, { "1": "14.6" }, { "2" : "normal" } ],   
 * &nbsp;&nbsp;&nbsp;&nbsp;[ { "0" : "4" }, { "1": "18.7" }, { "2" : "normal" } ],   
 * &nbsp;&nbsp;&nbsp;&nbsp;[ { "0" : "6" }, { "1": "21.3" }, { "2" : "abnormal" } ]
 * &nbsp;&nbsp;]
 * }
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
public final class JsonData
{
    /**
     * Utility class, no public constructor.
     */
    private JsonData()
    {
        // utility class
    }

    /**
     * Write the data from the data table in JSON format.
     * @param writer Writer; the writer that writes the data, e.g. to a file
     * @param dataTable Table; the data table to write
     * @throws IOException on I/O error when writing the data
     * @throws TextSerializationException on unknown data type for serialization
     */
    @SuppressWarnings("resource")
    public static void writeData(final Writer writer, final Table dataTable) throws IOException, TextSerializationException
    {
        try (JsonWriter jw = new JsonWriter(writer);)
        {

            jw.setIndent("  ");

            // write the table metadata
            jw.beginObject();
            jw.name("table").beginObject();
            jw.name("id").value(dataTable.getId());
            jw.name("description").value(dataTable.getDescription());
            jw.name("class").value(dataTable.getClass().getName());
            jw.name("columns").beginArray();
            int index = 0;
            for (Column<?> column : dataTable.getColumns())
            {
                jw.beginObject();
                jw.name("nr").value(index++);
                jw.name("id").value(column.getId());
                jw.name("description").value(column.getDescription());
                jw.name("type").value(column.getValueType().getName());
                if (column.getUnit() != null)
                {
                    jw.name("unit").value(column.getUnit());
                }
                jw.endObject();
            }
            jw.endArray(); // columns
            jw.endObject(); // table

            // initialize the serializers
            TextSerializer<?>[] serializers = new TextSerializer[dataTable.getNumberOfColumns()];
            for (int i = 0; i < dataTable.getNumberOfColumns(); i++)
            {
                Column<?> column = dataTable.getColumns().get(i);
                serializers[i] = TextSerializer.resolve(column.getValueType());
            }

            // write the data
            jw.name("data").beginArray();

            // write the records
            for (Row row : dataTable)
            {
                Object[] values = row.getValues();
                jw.beginArray();
                jw.setIndent("");
                for (int i = 0; i < dataTable.getNumberOfColumns(); i++)
                {
                    jw.beginObject().name(String.valueOf(i)).value(TextSerializer.serialize(serializers[i], values[i]))
                            .endObject();
                }
                jw.endArray(); // record
                jw.setIndent("  ");
            }

            // end JSON document
            jw.endArray(); // data array
            jw.endObject(); // data
        }
    }

    /**
     * Write the data from the data table in JSON format.
     * @param filename String; the file name to write the data to
     * @param dataTable Table; the data table to write
     * @throws IOException on I/O error when writing the data
     * @throws TextSerializationException on unknown data type for serialization
     */
    public static void writeData(final String filename, final Table dataTable) throws IOException, TextSerializationException
    {
        FileWriter fw = null;
        try
        {
            fw = new FileWriter(filename);
            writeData(fw, dataTable);
        }
        finally
        {
            if (null != fw)
            {
                fw.close();
            }
        }
    }

    /**
     * Read the data from the csv-file into the data table. Use the metadata to reconstruct the data table.
     * @param reader Reader; the reader that can read the data, e.g. from a file
     * @return dataTable the data table reconstructed from the meta data and filled with the data
     * @throws IOException on I/O error when reading the data
     * @throws TextSerializationException on unknown data type for serialization
     */
    public static Table readData(final Reader reader) throws IOException, TextSerializationException
    {
        try (JsonReader jr = new JsonReader(reader))
        {
            // read the metadata and reconstruct the data table
            jr.beginObject();
            readName(jr, "table");
            jr.beginObject();
            String[] tableProperties = new String[3];
            tableProperties[0] = readValue(jr, "id");
            tableProperties[1] = readValue(jr, "description");
            tableProperties[2] = readValue(jr, "class");

            // column metadata
            List<Column<?>> columns = new ArrayList<>();
            int index = 0;
            readName(jr, "columns");
            jr.beginArray();
            while (jr.peek().equals(JsonToken.BEGIN_OBJECT))
            {
                String[] columnProperties = new String[5];
                jr.beginObject();
                columnProperties[0] = readValue(jr, "nr");
                columnProperties[1] = readValue(jr, "id");
                columnProperties[2] = readValue(jr, "description");
                columnProperties[3] = readValue(jr, "type");
                columnProperties[4] = jr.peek().equals(JsonToken.END_OBJECT) ? null : readValue(jr, "unit");
                jr.endObject();

                if (Integer.valueOf(columnProperties[0]).intValue() != index)
                {
                    throw new IOException("column nr not ok");
                }
                String type = columnProperties[3];
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
                Column<?> column = new Column<>(columnProperties[1], columnProperties[2], valueClass, columnProperties[4]);
                columns.add(column);
                index++;
            }
            jr.endArray(); // columns
            jr.endObject(); // table

            // create table
            Table table;
            Consumer<Object[]> unserializableTable;
            if (tableProperties[2].equals(ListTable.class.getName()))
            {
                ListTable listTable = new ListTable(tableProperties[0], tableProperties[1], columns);
                table = listTable;
                unserializableTable = (data) -> listTable.addRow(data);
            }
            else
            {
                // fallback
                ListTable listTable = new ListTable(tableProperties[0], tableProperties[1], columns);
                table = listTable;
                unserializableTable = (data) -> listTable.addRow(data);
            }

            // obtain the serializers
            TextSerializer<?>[] serializers = new TextSerializer[table.getNumberOfColumns()];
            for (int i = 0; i < table.getNumberOfColumns(); i++)
            {
                serializers[i] = TextSerializer.resolve(columns.get(i).getValueType());
            }

            // read the data file records
            readName(jr, "data");
            jr.beginArray();
            while (jr.peek().equals(JsonToken.BEGIN_ARRAY))
            {
                Object[] values = new Object[columns.size()];
                jr.beginArray();
                for (int i = 0; i < table.getNumberOfColumns(); i++)
                {
                    jr.beginObject();
                    values[i] = TextSerializer.deserialize(serializers[i], readValue(jr, "" + i), columns.get(i));
                    jr.endObject();
                }
                jr.endArray(); // row
                unserializableTable.accept(values); // addRow
            }

            // end JSON document
            jr.endArray(); // data array
            jr.endObject(); // data
            return table;
        }
    }

    /**
     * Read a name - value pair from the JSON file where name has to match the given tag name.
     * @param jr JsonReader; the JSON stream reader
     * @param tag String; the tag to retrieve
     * @return the value belonging to the tag
     * @throws IllegalFormatException when the next element in the file did not contain the right tag
     * @throws IOException when reading from the stream raises an exception
     */
    private static String readValue(final JsonReader jr, final String tag) throws IllegalFormatException, IOException
    {
        Throw.when(!jr.nextName().equals(tag), IllegalFormatException.class, "readValue: no %s object", tag);
        if (jr.peek().equals(JsonToken.NULL))
        {
            jr.nextNull();
            return null;
        }
        return jr.nextString();
    }

    /**
     * Read a name -from the JSON file where name has to match the given tag name.
     * @param jr JsonReader; the JSON stream reader
     * @param tag String; the tag to retrieve
     * @throws IllegalFormatException when the next element in the file did not contain the right tag
     * @throws IOException when reading from the stream raises an exception
     */
    private static void readName(final JsonReader jr, final String tag) throws IllegalFormatException, IOException
    {
        Throw.when(!jr.nextName().equals(tag), IllegalFormatException.class, "readName: no %s object", tag);
    }

    /**
     * Read the data from the csv-file into the data table. Use the metadata to reconstruct the data table.
     * @param filename String; the file name to read the data from
     * @return dataTable the data table reconstructed from the meta data and filled with the data
     * @throws IOException on I/O error when reading the data
     * @throws TextSerializationException on unknown data type for serialization
     */
    public static Table readData(final String filename) throws IOException, TextSerializationException
    {
        try (FileReader fr = new FileReader(filename);)
        {
            return readData(fr);
        }
    }

}
