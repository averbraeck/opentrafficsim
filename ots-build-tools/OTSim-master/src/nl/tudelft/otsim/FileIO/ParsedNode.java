package nl.tudelft.otsim.FileIO;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.XMLEvent;

/**
 * Parse an XML file and build a tree structure from its contents.
 * 
 * @author Peter Knoppers
 */
public class ParsedNode {
	private Map<String, ArrayList<ParsedNode>> subNodes = null;
	String value = null;
	/** Line number in the XML file that defined this ParsedNode */
	public final int lineNumber;
	/** ColumnNumber in the line in the XML file that defined this ParsedNode */
	public final int columnNumber;
	
	/**
	 * Create a tree from an XML file.
	 * @param fileName String; name of the XML file
	 * @throws Exception 
	 */
	public ParsedNode(String fileName) throws Exception {
		lineNumber = columnNumber = -1;
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		InputStream in = null;
		Exception firstException = null;
		
		try {
			in = new FileInputStream(fileName);
			XMLEventReader eventReader = inputFactory.createXMLEventReader(in);
			expand (eventReader, fileName, null);
		} catch (Exception e) {
			firstException = e;
		}
		if (null != in) {
			try {
				in.close();
			} catch (IOException e) {
				// Ignore secondary error
			}
		}
		if (null != firstException)
			throw firstException;
	}
	
	private void expand (XMLEventReader er, String name, Location location) throws XMLStreamException {
		while (er.hasNext()) {
			XMLEvent event = er.nextEvent();
			switch(event.getEventType()) {
			case XMLStreamConstants.START_ELEMENT:
				String childName = event.asStartElement().getName().getLocalPart();
				//System.out.println("Adding child \"" + childName + "\"");
				addNode(childName, new ParsedNode(er, childName, event.getLocation()));
				break;
				
			case XMLStreamConstants.END_ELEMENT:
				return;
				
			case XMLStreamConstants.CHARACTERS:
				String reduced = event.asCharacters().getData().trim();
				if (reduced.length() == 0)
					break;
				addData(event.asCharacters().getData());
				break;
				
			case XMLStreamConstants.START_DOCUMENT:
				break;
				
			case XMLStreamConstants.END_DOCUMENT:
				return;
				
			case XMLStreamConstants.COMMENT:
				break;
				
			default:
				throw new Error("Do not know how to handle XMLStream event " + event.getEventType() + " near " + event.getLocation());
			}
		}
		throw new Error("Unexpected EOF in event " + name + " started at " + location);		
	}
	
	private ParsedNode(XMLEventReader er, String name, Location location) throws XMLStreamException {
		if (null == location)
			lineNumber = columnNumber = -1;
		else {
			lineNumber = location.getLineNumber();
			columnNumber = location.getColumnNumber();
		}
		expand(er, name, location);
	}
	
	private void addNode(String key, ParsedNode node) {
		if (null == subNodes)
			subNodes = new HashMap<String, ArrayList<ParsedNode>>();
		ArrayList<ParsedNode> siblings = subNodes.get(key);
		if (null == siblings)
			siblings = new ArrayList<ParsedNode>();
		siblings.add(node);
		subNodes.put(key, siblings);
	}
	
	private void addData(String newValue) {
		this.value = newValue;
	}
	
	/**
	 * Retrieve a sub-node of this ParsedNode.
	 * @param key String; the name of the sub-node
	 * @param index Integer; the rank of the subNode (within the set of sub-nodes with the specified key)
	 * @return ParsedNode; the addressed subNode, or null if no such subNode exists
	 */
	public ParsedNode getSubNode(String key, int index) {
		ArrayList<ParsedNode> list = subNodes.get(key);
		if (null == list)
			return null;
		return list.get(index);
	}

	/**
	 * Retrieve the number of keys defined in this ParsedNode.
	 * @return Integer; the number of keys
	 */
	public int size() {
		if (null == subNodes)
			return 0;
		return subNodes.size();
	}
	
	/**
	 * Retrieve the number of sub-nodes of this ParsedNode that have the specified name.
	 * @param key String; name of the sub-nodes
	 * @return Integer; the number of sub-nodes that have the specified name
	 */
	public int size(String key) {
		if (null == subNodes)
			return 0;
		ArrayList<ParsedNode> nodes = subNodes.get(key);
		if (null == nodes)
			return 0;
		return nodes.size();
	}
	
	/**
	 * Retrieve the names of the sub-nodes of this ParsedNode.
	 * @return Set; the names of the sub-nodes of this ParsedNode.
	 */
	public Set<String> getKeys() {
		if (null == subNodes)
			return new HashSet<String>();
		return subNodes.keySet();
	}
	
	/**
	 * Retrieve the content of this ParsedNode.
	 * @return String; the content of this ParsedNode, or null if this 
	 * ParsedNode has no content (except for sub-nodes)
	 */
	public String getValue() {
		return value;
	}
	
	/**
	 * Convert this ParsedNode and every sub-node to a string representation
	 * @param prefix String; prefix for this ParsedNode
	 * @return String; textual representation of this ParsedNode and all its sub-nodes
	 */
	public String toString(String prefix) {
		String result = "";
		for (String key : getKeys()) {
			ArrayList<ParsedNode> subList = subNodes.get(key);
			int index = 0;
			for(ParsedNode node: subList) {
				String indexString = String.format("[%d]", index++);
    			result += node.toString(prefix + "/" + key + indexString);
    		}
		}
		if (null != value)
			result += prefix + ": \"" + value + "\"\n";
		return result;
	}

}