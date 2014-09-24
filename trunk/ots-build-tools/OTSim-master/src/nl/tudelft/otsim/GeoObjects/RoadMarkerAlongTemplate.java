package nl.tudelft.otsim.GeoObjects;

import nl.tudelft.otsim.FileIO.ParsedNode;
import nl.tudelft.otsim.FileIO.StaXWriter;
import nl.tudelft.otsim.FileIO.XML_IO;

/**
 * This class is used to define common road markers.
 * 
 * @author Peter Knoppers
 */
public class RoadMarkerAlongTemplate implements XML_IO {
	/** Name for a RoadMarkerType in XML format */
	public static final String XMLTAG = "roadMarkerType";
	
	/** Name for a RoadMarkerAlongTemplate type element in XML format */
	private static final String XML_TYPE = "type";
	/** Name for a RoadmarkerAlongTemplate width element in XML format */
	private static final String XML_WIDTH = "width";

	private String type;
	private double markerWidth;
	public static final String ALONG_CONTINUOUS = "|";
	public static final String ALONG_STRIPED = ":";
	/**
	 * Create a RoadmarkerAlongTemplate.
	 * @param type String; type of the RoadMarkerAlongTemplate
	 * @param width Double; width (in m) of the road marker
	 */
	public RoadMarkerAlongTemplate(String type, double width) {
		this.type = type;
		this.markerWidth = width;
	}
	
	/**
	 * Create a RoadMarkerAlongTemplate from a parsed XML file.
	 * @param pn {@link ParsedNode}; the root of the RoadMarkerAlongTemplate in the parsed XML file
	 * @throws Exception
	 */
	public RoadMarkerAlongTemplate(ParsedNode pn) throws Exception {
    	type = null;
    	markerWidth = Double.NaN;
    	
		for (String fieldName : pn.getKeys()) {
			if (pn.size(fieldName) != 1)
				throw new Exception("Field " + fieldName + " has " + pn.size(fieldName) + "elements (should be 1)");
			String value = pn.getSubNode(fieldName, 0).getValue();
			if (null == value)
				throw new Exception("Value of " + fieldName + " is null");
			if (fieldName.equals(XML_TYPE))
				type = value;
			else if (fieldName.equals(XML_WIDTH))
				markerWidth = Double.parseDouble(value);
			else
				throw new Exception("Unknown field in RoadMarkerAlongTemplate: \"" + fieldName + "\"");
		}
		if (null == type)
			throw new Exception("Type of RoadMarkerAlongTemplate not defined");
	}

	/**
	 * Retrieve the type of a RoadMarkerAlongTemplate.
	 * @return String; the type of the RoadMarkerAlongTemplate
	 */
	public String getType() {
		return type;
	}

	/* Un-comment this if we really think it is wise to be able to promote an
	 * existing RoadMarkerAlongTemplate to another type.
	public void setType(String type) {
		this.type = type;
	}
	*/

	/**
	 * Retrieve the width of a RoadMarkerAlongTemplate.
	 * @return Double; the width of the RoadMarkerAlongTemplate
	 */
	public double getMarkerWidth() {
		return markerWidth;
	}

	/**
	 * Write this RoadMarkerAlongTemplate to an XML file.
	 * @param staXWriter {@link StaXWriter}; writer for the XML file
	 * @return Boolean; true on success; false on failure
	 */
	@Override
	public boolean writeXML(StaXWriter staXWriter) {
		return staXWriter.writeNodeStart(XMLTAG)
				&& staXWriter.writeNode(XML_TYPE, getType())
				&& staXWriter.writeNode(XML_WIDTH, Double.toString(getMarkerWidth()))
				&& staXWriter.writeNodeEnd(XMLTAG);
	}

	/* Un-comment this if we really think it is wise to be able to change the
	 * width of an existing RoadmarkerAlongTemplate
	public void setMarkerWidth(double markerWidth) {
		this.markerWidth = markerWidth;
	}
	*/

}