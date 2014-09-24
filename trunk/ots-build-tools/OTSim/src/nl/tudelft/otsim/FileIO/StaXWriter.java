package nl.tudelft.otsim.FileIO;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.DTD;

import nl.tudelft.otsim.GUI.WED;
import nl.tudelft.otsim.Utilities.Sorter;

/**
 * Write to an XML file.
 * 
 * @author gtamminga & Peter Knoppers
 *
 */
public class StaXWriter {
	final String finalFileName;
	final String temporaryFileName;
	private XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
	FileOutputStream fileOutputStream = null;
	private XMLEventWriter eventWriter = null;
	private XMLEventFactory eventFactory = XMLEventFactory.newInstance();
	private boolean errorOccurred = false;
	private final DTD lineEnd = eventFactory.createDTD("\n");
	private final DTD tab = eventFactory.createDTD("  ");
	private int nestingLevel = 0;
	private boolean closed = false;
	
	/**
	 * Create a new StaXWriter.
	 * @param fileName String; name of the XML file
	 * @param useTempFile Boolean; if true; first create a temporary file to
	 * write to and use that to replace/create the given fileName; if false;
	 * write directly to the given fileName
	 * @throws Exception
	 */
	public StaXWriter(String fileName, boolean useTempFile) throws Exception {
		finalFileName = fileName;
		if (useTempFile)
			temporaryFileName = fileName + ".tmp";
		else
			temporaryFileName = fileName;
		fileOutputStream = new FileOutputStream(temporaryFileName);
		startUp(fileOutputStream);
	}
	
	/**
	 * Catch an XML representation without creating any real file.
	 * <br /> Used primarily for testing. 
	 * @param xml_writing_object {@link XML_IO} object whose writeXML method will be called
	 * @return String; XML output of the xml_writing_object
	 * @throws Exception
	 */
	public static String XMLString(XML_IO xml_writing_object) throws Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		StaXWriter writer = new StaXWriter(outputStream);
		xml_writing_object.writeXML(writer);
		writer.close();
		String xmlText = outputStream.toString();
		outputStream.close();
		return xmlText;
	}
	
	/**
	 * Create a new StaXWriter that outputs to a caller-created OutputStream.
	 * <br /> This is mostly used for testing (where it is convenient to store
	 * the entire output in memory for verification afterwards).
	 * @param outputStream OutputStream where the output is sent to
	 * @throws Exception
	 */
	public StaXWriter(OutputStream outputStream) throws Exception {
		finalFileName = null;
		temporaryFileName = null;
		startUp(outputStream);
	}
	
	/**
	 * Create the XMLEventWriter and write Start Document event.
	 * @param outputStream OutputStream to send the XML text to
	 * @throws Exception
	 */
	private void startUp(OutputStream outputStream) throws Exception {
		eventWriter = outputFactory.createXMLEventWriter(outputStream);
		eventWriter.add(eventFactory.createStartDocument("UTF-8", "1.0"));
		eventWriter.add(lineEnd);		
	}
	
	/**
	 * Write a start node to the XML file.
	 * <br > Exactly one end node should be written for each start node
	 * @param name String; tag of the start node
	 * @return Boolean; true on success; false if some error occurred
	 */
	public boolean writeNodeStart(String name) {
		return writeNodeStart(name, null);
	}
	
	private ArrayList<Attribute> buildAttributeList (HashMap<String, String> map) {
		ArrayList<Attribute> result = new ArrayList<Attribute>();
		for (String key : Sorter.asSortedList(map.keySet()))
			result.add(eventFactory.createAttribute(key, map.get(key)));
		return result;
	}
	
	/**
	 * Write a start node with attributes to the XML file.
	 * <br > Exactly one end node should be written for each start node
	 * @param name String; tag of the start node
	 * @param attributes HashMap&lt;String, String&gt;; the list of attributes for the start node; may be null
	 * @return Boolean; true on success; false if some error occurred
	 */
	public boolean writeNodeStart(String name, HashMap<String, String> attributes) {
		if (errorOccurred)
			return false;
		try {
			writeIndent();
			if (null == attributes)
				eventWriter.add(eventFactory.createStartElement("", "", name));
			else
				eventWriter.add(eventFactory.createStartElement("", "", name, buildAttributeList(attributes).iterator(), null));
			eventWriter.add(lineEnd);
			nestingLevel++;
		} catch (XMLStreamException e) {
			errorOccurred = true;
			WED.showProblem(WED.ENVIRONMENTERROR, "Could not write start node %s: %s", name, WED.exeptionStackTraceToString(e));
		}
		return ! errorOccurred;
	}
	
	/**
	 * Write an end node to the XML file.
	 * @param name String; Tag of the end node (should match the corresponding start node)
	 * @return Boolean; true on success, false if some error occurred
	 */
	public boolean writeNodeEnd(String name) {
		if (errorOccurred)
			return false;
		nestingLevel--;
		if (nestingLevel < 0) {
			WED.showProblem(WED.PROGRAMERROR, "XML nesting level error (too many end node tags");
			errorOccurred = true;
			return false;
		}
		try {
			writeIndent();
			eventWriter.add(XMLEventFactory.newInstance().createEndElement("", "", name));
			eventWriter.add(lineEnd);
		} catch (XMLStreamException e) {
			errorOccurred = true;
			WED.showProblem(WED.ENVIRONMENTERROR, "Could not write end node %s: %s", name, WED.exeptionStackTraceToString(e));
		}
		return ! errorOccurred;
	}
	
	/**
	 * Write a node with only one value and no embedded XML elements.
	 * @param name String; tag of the node
	 * @param value String; value of the node
	 * @return Boolean; true on success, false if some error occurred
	 */
	public boolean writeNode(String name, String value) {
		return (writeNode(name, null, value));
	}
	
	/**
	 * Write a node with attributes and only one value and no embedded XML elements.
	 * @param name String; tag of the node
	 * @param attributes HashMap&lt;String, String&gt;; the list of attributes for the start node; may be null
	 * @param value String; value of the node
	 * @return Boolean; true on success, false if some error occurred
	 */
	public boolean writeNode(String name, HashMap<String, String> attributes, String value) {
		if (errorOccurred)
			return false;
		try {
			// Write indentation
			writeIndent();
			// Create start node
			if (null == attributes)
				eventWriter.add(eventFactory.createStartElement("", "", name));
			else
				eventWriter.add(eventFactory.createStartElement("", "", name, buildAttributeList(attributes).iterator(), null));
			// Create content of the node
			eventWriter.add(eventFactory.createCharacters(value));
			// Create end node
			eventWriter.add(eventFactory.createEndElement("", "", name));
			// Write line termination
			eventWriter.add(lineEnd);
		} catch (XMLStreamException e) {
			errorOccurred = true;
			WED.showProblem(WED.ENVIRONMENTERROR, "Could not write node %s: %s", name, WED.exeptionStackTraceToString(e));
		}
		return ! errorOccurred;
	}
	
	/**
	 * Close this StaXWriter.
	 * <br /> After closing no further actions can be performed on/by this
	 * StaXWriter.
	 * @return Boolean; true on success, false if some error occurred
	 */
	public boolean close() {
		if (closed)
			throw new Error("StaXWriter is already closed");
		try {
			if (null != eventWriter)
				eventWriter.close();
			if (null != fileOutputStream)
				fileOutputStream.close();
		} catch (Exception e) {
			if (! errorOccurred)
				WED.showProblem(WED.ENVIRONMENTERROR, "Error closing file %s: %s", temporaryFileName, WED.exeptionStackTraceToString(e));
			errorOccurred = true;
		}
		eventWriter = null;		// Ensure that any subsequent attempts to use this StaXWriter fail
		fileOutputStream = null;
		if (nestingLevel != 0) {
			WED.showProblem(WED.PROGRAMERROR, "XML nesting level error (%d end node tag(s) missing)", nestingLevel);
			errorOccurred = true;
		}
		if ((! errorOccurred) && (null != finalFileName) && (! finalFileName.equals(temporaryFileName))) {
			File tempFile = new File(temporaryFileName);
			File finalFile = new File(finalFileName);
			// first try to rename atomically
			boolean success = false;
			try {
				success = tempFile.renameTo(finalFile);
			} catch (Exception e) {
				;	// Ignore any exception thrown
			}
			if (! success) {
				// atomic rename failed; try to do it in two steps
				try {
					finalFile.delete();
				} catch (Exception e) {
					// Ignore this one too
				}
				try {
					success = tempFile.renameTo(finalFile);
				} catch (Exception e) {
					// Now it's serious
					WED.showProblem(WED.ENVIRONMENTERROR, "Could not rename temporary file \"%s\" to \"%s\"", temporaryFileName, finalFileName);
					errorOccurred = true;
				}
			}
		}
		if (! errorOccurred)
			closed = true;
		return ! errorOccurred;
	}
	
	private void writeIndent() throws XMLStreamException {
		for (int i = 0; i < nestingLevel; i++)
			eventWriter.add(tab);		
	}
	
}