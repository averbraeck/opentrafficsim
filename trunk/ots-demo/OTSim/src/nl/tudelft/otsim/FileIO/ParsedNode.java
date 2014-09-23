package nl.tudelft.otsim.FileIO;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.xml.stream.Location;
import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamConstants;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.Attribute;
import javax.xml.stream.events.XMLEvent;

/**
 * Parse an XML file and build a tree structure from its contents.
 * 
 * @author Peter Knoppers
 */
public class ParsedNode {
	private Map<String, ArrayList<ParsedNode>> subNodes = null;
	final String name;
	String value = null;
	HashMap<String, String> attributes = null;
	/** Line number in the XML file that defined this ParsedNode */
	public final int lineNumber;
	/** ColumnNumber in the line in the XML file that defined this ParsedNode */
	public final int columnNumber;
	/** Parent node if this ParsedNoded */
	public final ParsedNode parent;
	
	/**
	 * Create a tree from an XML file.
	 * @param fileName String; name of the XML file, or URL starting with "http://"
	 * @throws Exception 
	 */
	public ParsedNode(String fileName) throws Exception {
		name = fileName;
		lineNumber = columnNumber = -1;
		parent = null;
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		InputStream in = null;
		Exception firstException = null;
		
		try {
			if (name.startsWith("http://")) {
				URL url = new URL(name);
				in = url.openStream();
			} else {
				in = new FileInputStream(fileName);
			}
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
	
	/**
	 * Create a ParsedNode from an InputStream.
	 * @param inputStream InputStream that should yield XML
	 * @throws Exception
	 */
	public ParsedNode(InputStream inputStream) throws Exception {
		name = "";
		lineNumber = columnNumber = -1;
		parent = null;
		XMLInputFactory inputFactory = XMLInputFactory.newInstance();
		XMLEventReader eventReader = inputFactory.createXMLEventReader(inputStream);
		expand (eventReader, "[InputStream]", null);
	}
	
	private void expand (XMLEventReader er, String eventName, Location location) throws XMLStreamException {
		while (er.hasNext()) {
			XMLEvent event = er.nextEvent();
			switch(event.getEventType()) {
			case XMLStreamConstants.START_ELEMENT:
				String childName = event.asStartElement().getName().getLocalPart();
				//System.out.println("Adding child \"" + childName + "\"");
				ParsedNode newNode = new ParsedNode(er, childName, this, event.getLocation());
				for (@SuppressWarnings("unchecked")
				Iterator<Attribute> it = event.asStartElement().getAttributes(); it.hasNext(); ) {
					Attribute attr = it.next();
					//System.out.println(String.format("attr \"%s\" -> \"%s\"", attr.getName(), attr.getValue()));
					if (null == newNode.attributes)
						newNode.attributes = new HashMap<String, String>();
					newNode.attributes.put("" + attr.getName(), attr.getValue());
				}
				addNode(childName, newNode);
				break;
				
			case XMLStreamConstants.END_ELEMENT:
				return;
				
			case XMLStreamConstants.CHARACTERS:
				String reduced = event.asCharacters().getData().trim();
				if (reduced.length() == 0)
					break;
				this.value = event.asCharacters().getData();
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
		throw new Error("Unexpected EOF in event " + eventName + " started at " + location);		
	}
	
	private ParsedNode(XMLEventReader er, String name, ParsedNode parent, Location location) throws XMLStreamException {
		this.name = name;
		if (null == location)
			lineNumber = columnNumber = -1;
		else {
			lineNumber = location.getLineNumber();
			columnNumber = location.getColumnNumber();
		}
		this.parent = parent;
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
	 * Retrieve the set of attribute names for this ParsedNode.
	 * @return Set<String>; the (possibly empty) Set of Strings that identify the attributes of this ParsedNode
	 */
	public Set<String> getAttributeKeys() {
		final Set<String> emptyset = Collections.emptySet();
		if (null == attributes)
			return emptyset;
		return attributes.keySet();
	}
	
	/**
	 * Retrieve the value of an attribute of this ParsedNode.
	 * <br /> If this ParsedNode has no attribute with the specified key, null is returned.
	 * @param key String; the String that identifies the attribute
	 * @return String the value of the attribute
	 */
	public String getAttributeValue(String key) {
		if (null == attributes)
			return null;
		return attributes.get(key);
	}
	
	/**
	 * Convert this ParsedNode and every sub-node to a string representation
	 * @param prefix String; prefix for this ParsedNode
	 * @param limit Integer; maximum number of sub-nodes of the same type to 
	 * report for this ParsedNode and each recursively visited ParsedNode
	 * @return String; textual representation of this ParsedNode and all its sub-nodes
	 */
	public String toString(String prefix, int limit) {
		String result = "";
		//prefix += "/" + name;
		for (String key : getAttributeKeys())
			result += prefix + ":" + key + "=\"" + getAttributeValue(key) + "\"\n";
		for (String key : getKeys()) {
			ArrayList<ParsedNode> subList = subNodes.get(key);
			int index = 0;
			for(ParsedNode node : subList) {
				if (index >= limit) {
					int remainder = subList.size() - index;
					result += prefix + String.format("/" + node.name + " ... (%d item%s suppressed)\n", remainder, remainder == 1 ? "" : "s");
					break;
				}
				String indexString = String.format("[%d]", index++);
    			result += node.toString(prefix + "/" + node.name + indexString, limit);
    		}
		}
		if (null != value)
			result += prefix + "/" + name +  ": \"" + value + "\"\n";
		return result;
	}
	
	/**
	 * Convert this ParsedNode and every sub-node to a string representation
	 * @param prefix String; prefix for this ParsedNode
	 * @return String; textual representation of this ParsedNode and all its sub-nodes
	 */
	public String toString(String prefix) {
		return toString (prefix, Integer.MAX_VALUE);
	}
	
	/**
	 * Retrieve the name of this tree of ParsedNodes.
	 * @return String; the name of the tree
	 */
	public String treeName() {
		ParsedNode parentNode = this;
		while (null != parentNode.parent)
			parentNode = parentNode.parent;
		return parentNode.name;
	}
	
	private static String lineOrColumn (int number) {
		if (-1 == number)
			return "";
		return "." + number;
	}
	
	/**
	 * Return a description of this ParsedNode to be used in diagnostics.
	 * @return String; description of this ParsedNode
	 */
	public String description() {
		return String.format(Locale.US,  "%s%s%s", treeName(), lineOrColumn(lineNumber), lineOrColumn(columnNumber));
	}

}