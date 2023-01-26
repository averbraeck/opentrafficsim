package org.opentrafficsim.kpi.sampling.xml;

import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamReader;
import javax.xml.stream.XMLStreamWriter;

import org.djutils.exceptions.Throw;
import org.djutils.primitives.Primitive;
import org.opentrafficsim.kpi.sampling.Column;
import org.opentrafficsim.kpi.sampling.ListTable;
import org.opentrafficsim.kpi.sampling.Row;
import org.opentrafficsim.kpi.sampling.Table;
import org.opentrafficsim.kpi.sampling.serialization.TextSerializationException;
import org.opentrafficsim.kpi.sampling.serialization.TextSerializer;

/**
 * XmlData takes care of reading and writing of table data in XML format. The reader and writer use a streaming API to avoid
 * excessive memory use. The class can be used, e.g., as follows:
 * 
 * <pre>
 * Table dataTable = new ListTable("data", "dataTable", columns);
 * Writer writer = new FileWriter("c:/data/data.xml");
 * XmlData.writeData(writer, dataTable);
 * </pre>
 * 
 * The XML document has the following structure:
 * 
 * <pre>
 * &lt;xmldata&gt;
 * &nbsp;&nbsp;&lt;table id="tableId" description="description" class="org.djutils.data.ListTable"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;column nr="0" id="obsNr" description="observation nr" type="int"&gt;&lt;/column&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;column nr="1" id="value" description="observation value" type="double"&gt;&lt;/column&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;column nr="2" id="comment" description="comment" type="java.lang.String"&gt;&lt;/column&gt;
 * &nbsp;&nbsp;&lt;/table&gt;
 * &nbsp;&nbsp;&lt;data&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;record index="0"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;value nr="0" content="2"&gt;&lt;/value&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;value nr="1" content="18.6"&gt;&lt;/value&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;value nr="2" content="normal"&gt;&lt;/value&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/record&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;record index="1"&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;value nr="0" content="4"&gt;&lt;/value&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;value nr="1" content="36.18"&gt;&lt;/value&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&lt;value nr="2" content="normal"&gt;&lt;/value&gt;
 * &nbsp;&nbsp;&nbsp;&nbsp;&lt;/record&gt;
 * &nbsp;&nbsp;&lt;/data&gt;
 * &lt;/xmldata&gt;
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
public final class XmlData
{
    /**
     * Utility class, no public constructor.
     */
    private XmlData()
    {
        // utility class
    }

    /**
     * Write the data from the data table in XML format.
     * @param writer Writer; the writer that writes the data, e.g. to a file
     * @param dataTable Table; the data table to write
     * @throws IOException on I/O error when writing the data
     * @throws TextSerializationException on unknown data type for serialization
     * @throws XMLStreamException on XML write error
     */
    public static void writeData(final Writer writer, final Table dataTable)
            throws IOException, TextSerializationException, XMLStreamException
    {
        XMLStreamWriter xmlw = null;
        try
        {
            XMLOutputFactory xmlOutputFactory = XMLOutputFactory.newInstance();
            xmlw = xmlOutputFactory.createXMLStreamWriter(writer);

            // XML header
            xmlw.writeStartDocument();
            xmlw.writeCharacters("\n");

            // write the table metadata
            xmlw.writeStartElement("xmldata");
            xmlw.writeCharacters("\n");
            xmlw.writeCharacters("  ");
            xmlw.writeStartElement("table");
            xmlw.writeAttribute("id", dataTable.getId());
            xmlw.writeAttribute("description", dataTable.getDescription());
            xmlw.writeAttribute("class", dataTable.getClass().getName());
            xmlw.writeCharacters("\n");
            int index = 0;
            for (Column<?> column : dataTable.getColumns())
            {
                xmlw.writeCharacters("    ");
                xmlw.writeEmptyElement("column");
                xmlw.writeAttribute("nr", String.valueOf(index++));
                xmlw.writeAttribute("id", column.getId());
                xmlw.writeAttribute("description", column.getDescription());
                xmlw.writeAttribute("type", column.getValueType().getName());
                if (column.getUnit() != null)
                {
                    xmlw.writeAttribute("unit", column.getUnit());
                }
                xmlw.writeCharacters("\n");
            }
            xmlw.writeCharacters("  ");
            xmlw.writeEndElement(); // table
            xmlw.writeCharacters("\n");

            // initialize the serializers
            TextSerializer<?>[] serializers = new TextSerializer[dataTable.getNumberOfColumns()];
            for (int i = 0; i < dataTable.getNumberOfColumns(); i++)
            {
                Column<?> column = dataTable.getColumns().get(i);
                serializers[i] = TextSerializer.resolve(column.getValueType());
            }

            // write the data
            xmlw.writeCharacters("  ");
            xmlw.writeStartElement("data");
            xmlw.writeCharacters("\n");

            // write the records
            int recordNr = 0;
            for (Row row : dataTable)
            {
                Object[] values = row.getValues();
                xmlw.writeCharacters("    ");
                xmlw.writeStartElement("row");
                xmlw.writeAttribute("index", String.valueOf(recordNr++));
                xmlw.writeCharacters("\n");
                for (int i = 0; i < dataTable.getNumberOfColumns(); i++)
                {
                    xmlw.writeCharacters("      ");
                    xmlw.writeEmptyElement("value");
                    xmlw.writeAttribute("nr", String.valueOf(i));
                    String content = TextSerializer.serialize(serializers[i], values[i]);
                    if (content != null)
                    {
                        xmlw.writeAttribute("content", content);
                    }
                    xmlw.writeCharacters("\n");
                }
                xmlw.writeCharacters("    ");
                xmlw.writeEndElement(); // row
                xmlw.writeCharacters("\n");
            }

            // end XML document
            xmlw.writeCharacters("  ");
            xmlw.writeEndElement(); // data
            xmlw.writeCharacters("\n");
            xmlw.writeEndElement(); // xmldata
            xmlw.writeCharacters("\n");
            xmlw.writeEndDocument();
        }
        finally
        {
            if (null != xmlw)
            {
                xmlw.close();
            }
        }
    }

    /**
     * Write the data from the data table in XML format.
     * @param filename String; the file name to write the data to
     * @param dataTable Table; the data table to write
     * @throws IOException on I/O error when writing the data
     * @throws TextSerializationException on unknown data type for serialization
     * @throws XMLStreamException on XML write error
     */
    public static void writeData(final String filename, final Table dataTable)
            throws IOException, TextSerializationException, XMLStreamException
    {
        try (FileWriter fw = new FileWriter(filename))
        {
            writeData(fw, dataTable);
        }
    }

    /**
     * Read the data from the XML-file into the data table. Use the metadata to reconstruct the data table.
     * @param reader Reader; the reader that can read the data, e.g. from a file
     * @return dataTable the data table reconstructed from the meta data and filled with the data
     * @throws IOException on I/O error when reading the data
     * @throws TextSerializationException on unknown data type for serialization
     * @throws XMLStreamException on XML read error
     */
    public static Table readData(final Reader reader) throws IOException, TextSerializationException, XMLStreamException
    {
        XMLStreamReader xmlr = null;
        try
        {
            // read the metadata file and reconstruct the data table
            XMLInputFactory xmlInputFactory = XMLInputFactory.newInstance();
            xmlr = xmlInputFactory.createXMLStreamReader(reader);

            // wait for the xmldata tag
            waitFor(xmlr, "xmldata");

            // wait for the table tag
            waitFor(xmlr, "table");
            String[] tableProperties = getAttributes(xmlr, "id", "description", "class");

            // column metadata
            List<Column<?>> columns = new ArrayList<>();
            int index = 0;
            while (waitFor(xmlr, "column", "table"))
            {
                String[] columnProperties;
                if (xmlr.getAttributeCount() == 5)
                {
                    columnProperties = getAttributes(xmlr, "nr", "id", "description", "type", "unit");
                }
                else
                {
                    // null unit
                    columnProperties = getAttributes(xmlr, "nr", "id", "description", "type");
                }
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
                Column<?> column = new Column<>(columnProperties[1], columnProperties[2], valueClass,
                        columnProperties.length >= 5 ? columnProperties[4] : null);
                columns.add(column);
                index++;
            }
            
            // create table
            Table table;
            Consumer<Object[]> unserializableTable;
            if (tableProperties[2].equals(ListTable.class.getName()))
            {
                ListTable listTable = new ListTable(tableProperties[0], tableProperties[1], columns);
                table = listTable;
                unserializableTable = listTable;
            }
            else
            {
                // fallback
                ListTable listTable = new ListTable(tableProperties[0], tableProperties[1], columns);
                table = listTable;
                unserializableTable = listTable;
            }

            // obtain the serializers
            TextSerializer<?>[] serializers = new TextSerializer[table.getNumberOfColumns()];
            for (int i = 0; i < table.getNumberOfColumns(); i++)
            {
                serializers[i] = TextSerializer.resolve(columns.get(i).getValueType());
            }

            // read the data file records
            waitFor(xmlr, "data");
            while (waitFor(xmlr, "row", "data"))
            {
                String[] data = new String[columns.size()];
                while (waitFor(xmlr, "value", "row"))
                {
                    if (xmlr.getAttributeCount() == 2)
                    {
                        String[] valueProperties = getAttributes(xmlr, "nr", "content");
                        data[Integer.valueOf(valueProperties[0]).intValue()] = valueProperties[1];
                    }
                    else
                    {
                        // null content
                        String[] valueProperties = getAttributes(xmlr, "nr");
                        data[Integer.valueOf(valueProperties[0]).intValue()] = null;
                    }
                }
                Object[] values = new Object[columns.size()];
                for (int i = 0; i < values.length; i++)
                {
                    values[i] = TextSerializer.deserialize(serializers[i], data[i], columns.get(i));
                }
                unserializableTable.accept(values); // addRow
            }
            return table;
        }
        finally
        {
            if (null != xmlr)
            {
                xmlr.close();
            }
        }
    }

    /**
     * Read from the XML file until a START_ELEMENT with the id equal to the provided tag is encountered.
     * @param xmlr XMLStreamReader; the XML stream reader
     * @param tag String; the tag to retrieve
     * @throws XMLStreamException on error reading from the XML stream
     * @throws IOException when the stream ended without finding the tag
     */
    private static void waitFor(final XMLStreamReader xmlr, final String tag) throws XMLStreamException, IOException
    {
        while (xmlr.hasNext())
        {
            xmlr.next();
            if (xmlr.getEventType() == XMLStreamConstants.START_ELEMENT)
            {
                if (xmlr.getLocalName().equals(tag))
                {
                    return;
                }
            }
        }
        throw new IOException("Unexpected end of stream");
    }

    /**
     * Read from the XML file until a START_ELEMENT with the id equal to the provided tag is encountered, or until the
     * stopEndTag is reached. This can be used to get the starting tag in a repeat group. When the starting tag is found, the
     * method returns true. When the end tag of the repeat group is found, false is returned.
     * @param xmlr XMLStreamReader; the XML stream reader
     * @param tag String; the tag to retrieve, usually a tag in a repeat group
     * @param stopEndTag String; the tag to indicate the end of the repeat group
     * @return true when the tag in the repeat group was found; false when the stop tag was found
     * @throws XMLStreamException on error reading from the XML stream
     * @throws IOException when the stream ended without finding the tag or the stop tag
     */
    private static boolean waitFor(final XMLStreamReader xmlr, final String tag, final String stopEndTag)
            throws XMLStreamException, IOException
    {
        while (xmlr.hasNext())
        {
            xmlr.next();
            if (xmlr.getEventType() == XMLStreamConstants.START_ELEMENT)
            {
                if (xmlr.getLocalName().equals(tag))
                {
                    return true;
                }
            }
            else if (xmlr.getEventType() == XMLStreamConstants.END_ELEMENT)
            {
                if (xmlr.getLocalName().equals(stopEndTag))
                {
                    return false;
                }
            }
        }
        throw new IOException("Unexpected end of stream");
    }

    /**
     * Read the attributes into an array and return the array. The position of each attribute is indicated by the vararg
     * parameter 'attributes'.
     * @param xmlr XMLStreamReader; the XML stream reader
     * @param attributes String...; the attributes that are expected
     * @return the array of atribute values, in the order of the vararg parameter 'attributes'
     * @throws XMLStreamException on error reading from the XML stream
     * @throws IOException when the current element does not contain the right (number of) attributes
     */
    private static String[] getAttributes(final XMLStreamReader xmlr, final String... attributes)
            throws XMLStreamException, IOException
    {
        String[] result = new String[attributes.length];
        int found = 0;
        for (int i = 0; i < xmlr.getAttributeCount(); i++)
        {
            String localName = xmlr.getAttributeLocalName(i);
            String value = xmlr.getAttributeValue(i);
            for (int j = 0; j < attributes.length; j++)
            {
                if (localName.equals(attributes[j]))
                {
                    result[j] = value;
                    found++;
                }
            }
        }
        Throw.when(found != attributes.length, IOException.class, "attribute data does not contain %d fields",
                attributes.length);
        return result;
    }

    /**
     * Read the data from the XML-file into the data table. Use the metadata to reconstruct the data table.
     * @param filename String; the file name to read the data from
     * @return dataTable the data table reconstructed from the meta data and filled with the data
     * @throws IOException on I/O error when reading the data
     * @throws TextSerializationException on unknown data type for serialization
     * @throws XMLStreamException on XML read error
     */
    public static Table readData(final String filename) throws IOException, TextSerializationException, XMLStreamException
    {
        try (FileReader fr = new FileReader(filename))
        {
            return readData(fr);
        }
    }

}
