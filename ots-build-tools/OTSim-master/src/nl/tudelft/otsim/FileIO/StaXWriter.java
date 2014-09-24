package nl.tudelft.otsim.FileIO;

import java.io.File;
import java.io.FileOutputStream;
import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.DTD;

import nl.tudelft.otsim.GUI.WED;

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
		eventWriter = outputFactory.createXMLEventWriter(fileOutputStream);
		// Create and write Start Document event
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
		if (errorOccurred)
			return false;
		try {
			writeIndent();
			eventWriter.add(eventFactory.createStartElement("", "", name));
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
		if (errorOccurred)
			return false;
		try {
			// Write indentation
			writeIndent();
			// Create start node
			eventWriter.add(eventFactory.createStartElement("", "", name));
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
		if ((! errorOccurred) && (! finalFileName.equals(temporaryFileName))) {
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
	
	/**
	 * Save the Demand of a traffic Model to a file. 
	 * @param fileName String; name of the file
	 * @param model Model; traffic model with the demand data to write
	 * @throws Exception
	 */
	/*
	public static void saveDemand(String fileName, Model model) throws Exception {
		XMLEventWriter eventWriter = null;
		FileOutputStream fileOutputStream = null;
		try {
			nestingLevel = 0;
			// Create a XMLOutputFactory
			XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
			// Create XMLEventWriter
			fileOutputStream = new FileOutputStream(fileName);
			eventWriter = outputFactory.createXMLEventWriter(fileOutputStream);
			// Create a EventFactory
			XMLEventFactory eventFactory = XMLEventFactory.newInstance();
			// Create and write Start Tag
			StartDocument startDocument = eventFactory.createStartDocument();
			eventWriter.add(startDocument);
			eventWriter.add(eventFactory.createDTD("\n"));
			addStartElement(eventWriter, TrafficDemand.TrafficDemand.XMLTAG);
			// Write the tripPatternList
			for (TripPattern tripPattern : model.trafficDemand.getTripPatternList()) {
				addStartElement(eventWriter, TripPattern.XMLTAG);
					if (tripPattern.getMovingPerson() != null) {
						addNode(eventWriter, TripPattern.XML_PERSONID, Integer.toString(tripPattern.getMovingPerson().getID()));
					}
					addNode(eventWriter, TripPattern.XML_NUMBEROFTRIPS, Double.toString(tripPattern.getNumberOfTrips()));
					//TODO Guus make it correct 
					//if (tripPattern.getActivityLocationIDList() != null)  { 
					//	for (Integer ID : tripPattern.getActivityLocationIDList())  {
					//		addNode(eventWriter, TripPattern.XML_activityID, Integer.toString(ID));
					//	}					
					//}
				addEndElement(eventWriter, TripPattern.XMLTAG);
			}
			addEndElement(eventWriter, TrafficDemand.TrafficDemand.XMLTAG);
			eventWriter.add(eventFactory.createEndDocument());
		} finally {
			if (null != eventWriter)
				eventWriter.close();
			if (null != fileOutputStream)
				fileOutputStream.close();
		}
	}
	
	private static void addStartElement(XMLEventWriter eventWriter, String tag) throws XMLStreamException {
		writeIndent(eventWriter);
		XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		eventWriter.add(eventFactory.createStartElement("", "", tag));
		eventWriter.add(eventFactory.createDTD("\n"));
		nestingLevel++;
	}
	
	private static void addEndElement(XMLEventWriter eventWriter, String tag) throws XMLStreamException {
		nestingLevel--;
		writeIndent(eventWriter);
		XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		eventWriter.add(eventFactory.createEndElement("", "", tag));
		eventWriter.add(eventFactory.createDTD("\n"));
	}

	private static void addNode(XMLEventWriter eventWriter, String name, String value) throws XMLStreamException {
		// Write indentation
		writeIndent(eventWriter);
		// Create start node
		XMLEventFactory eventFactory = XMLEventFactory.newInstance();
		eventWriter.add(eventFactory.createStartElement("", "", name));
		// Create content of the node
		eventWriter.add(eventFactory.createCharacters(value));
		// Create end node
		eventWriter.add(eventFactory.createEndElement("", "", name));
		// Write line termination
		eventWriter.add(eventFactory.createDTD("\n"));
	}
	*/

}