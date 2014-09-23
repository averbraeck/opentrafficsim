package nl.tudelft.otsim.GeoObjects;

import java.awt.Color;
import java.util.ArrayList;

import nl.tudelft.otsim.FileIO.ParsedNode;
import nl.tudelft.otsim.FileIO.StaXWriter;
import nl.tudelft.otsim.GUI.GraphicsPanel;
import nl.tudelft.otsim.SpatialTools.Planar;

/**
 * This class holds all aspects of a stop line.
 * 
 * @author gtamminga, Peter Knoppers
 *
 */
public class StopLine extends CrossSectionObject {
	/** Label in XML representation of a StopLine */
	public static final String XMLTAG = "stopLine";	
	/** Label of longitudinalPosition in XML representation of a StopLine	private static final String XML_LONGITUDINALPOSITION = "longitudinalPosition";
	/** Label of length in XML representation of a StopLine */
	private static final String XML_LENGTH = "length";
	/** Label of longitudinalPosition in XML representation of a TrafficLight */
	private static final String XML_LONGITUDINALPOSITION = "longitudinalPosition";
	/** Label of lateralCenter in XML representation of a StopLine */
	private static final String XML_LATERALPOSITION = "lateralPosition";
	/** Label of width in XML representation of a StopLine */
	private static final String XML_WIDTH = "width";
	/** Label of type in XML representation of a StopLine */
	private static final String XML_TYPE = "priorityType";   // type of stop line (yield, priority)
	
	/** Value for type of StopLine */
	public final static String YIELDSTOPLINE = "Yield";
	/** Value for type of StopLine */
	public final static String PRIORITYSTOPLINE = "Priority";
	/** Value for type of StopLine */
	public final static String NOSTOPLINE = "Undefined";
	
	private String type;
	private ArrayList<PriorityConflict> conflictList = null;
	private Color color = new Color(1f, 1f, 1f, 0.3f);
	private boolean opaque = true;
	/** Default value for painting a stopLine */ 
	/**
	 * Create a new StopLine.
	 * @param cse {@link CrossSectionElement} that will own this StopLine
	 * @param type String; type of the new StopLine
	 * @param longitudinalPosition Double; position of the new StopLine
	 * (negative values mean distance from the end of the
	 * {@link CrossSectionElement} that owns this StopLine)
	 * @param longitudinalLength Double; length of the StopLine in the driving direction
	 * @param lateralPosition  Double; position of the StopLine in the lateral direction
	 */
	public StopLine(CrossSectionElement cse, String type, double longitudinalPosition, double longitudinalLength, double lateralPosition) {
		super();
		this.crossSectionElement = cse;
		this.longitudinalPosition = longitudinalPosition;
		this.setLength_w(longitudinalLength);
		this.lateralPosition = lateralPosition;
		this.setType_w(type);
		this.lateralWidth = this.crossSectionElement.getWidth_r();
		if (type == null)
			this.setType_w(NOSTOPLINE);
	}

	/**
	 * Copy a StopLine to another {@link CrossSectionElement}.
	 * @param cse {@link CrossSectionElement}; the owner of the copy
	 * @param stopLine StopLine; the stop line to copy
	 */
	public StopLine(CrossSectionElement cse, StopLine stopLine) {
		this(cse, stopLine.getType(), stopLine.getLongitudinalPosition(), stopLine.longitudinalLength, stopLine.lateralPosition);
	}

	
	/**
	 * Create a StopLine from a parsed XML file.
	 * @param crossSectionElement {@link CrossSectionElement}; owner of the new StopLine
	 * @param pn {@link ParsedNode}; the root of the TrafficLight in the parsed XML file
	 * @throws Exception
	 */
	public StopLine(CrossSectionElement crossSectionElement, ParsedNode pn) throws Exception {
		this.crossSectionElement = crossSectionElement;
		lateralReference = CrossSectionElement.LateralReferenceCenter;
		longitudinalPosition = longitudinalLength = lateralPosition = lateralWidth = Double.NaN;
		for (String fieldName : pn.getKeys()) {
			String value = pn.getSubNode(fieldName, 0).getValue();
/*			if (fieldName.equals(XML_ID))
				ID = value;*/
			if (fieldName.equals(XML_TYPE))
				type = value;			
			else if (fieldName.equals(XML_LONGITUDINALPOSITION))
				longitudinalPosition = Double.parseDouble(value);
			else if (fieldName.equals(XML_LENGTH))
				longitudinalLength = Double.parseDouble(value);
			else if (fieldName.equals(XML_LATERALPOSITION))
				lateralPosition = Double.parseDouble(value);
			else if (fieldName.equals(XML_WIDTH))
				;	// not used; ignored
			else
				throw new Exception("TrafficLight does not have a field " + fieldName);
		}
		this.lateralWidth = this.crossSectionElement.getWidth_r();
		if (Double.isNaN(longitudinalPosition) || Double.isNaN(longitudinalLength) || Double.isNaN(lateralPosition))
			throw new Exception("TrafficLight is not completely defined" + pn.lineNumber + ", " + pn.columnNumber);
	}

	public ArrayList<PriorityConflict> addConflicts(PriorityConflict conflict)  {
		if (conflictList == null)
			conflictList = new ArrayList<PriorityConflict>();
		conflictList.add(conflict);
		return conflictList;
	}
	
	/**
	 * Retrieve the type of this StopLine.
	 * @return String; the type of this StopLine
	 */
	public String getType() {
		return type;
	}

	/**
	 * Modify the type of this StopLine.
	 * @param type String; the new type of this StopLine
	 */
	public void setType_w(String type) {
		if (type.equals(YIELDSTOPLINE) || type.equals(PRIORITYSTOPLINE) || type.equals(NOSTOPLINE))
			this.type = type;
		else
			throw new Error("Invalid stop line type: " + type);
	}

	public void setLength_w(Double length) {
		longitudinalLength = length;
	}

	public Color getColor() {
		return color;
	}

	public void setColor(Color color) {
		this.color = color;
	}

	public boolean isOpaque() {
		return opaque;
	}

	public void setOpaque(boolean opaque) {
		this.opaque = opaque;
	}

	@Override
	public void paint(GraphicsPanel graphicsPanel) {
		if (null == crossSectionElement)
			return;
		ArrayList<Vertex> outLine = Planar.slicePolyline(crossSectionElement.getLinkPointList(CrossSectionElement.LateralReferenceLeft, true, false), longitudinalPosition, longitudinalLength);
		ArrayList<Vertex> add = Planar.slicePolyline(crossSectionElement.getLinkPointList(CrossSectionElement.LateralReferenceRight, true, false), longitudinalPosition, longitudinalLength);		
		ArrayList<Vertex> result = new ArrayList<Vertex>();
		for (int i = 0; (i < outLine.size()) && (i < add.size()); i++)
			result.add(Vertex.weightedVertex(this.lateralPosition / crossSectionElement.getWidth_r(), outLine.get(i), add.get(i)));
		for (int i = outLine.size(); --i >= 0; )
			if (i < add.size())
				result.add(Vertex.weightedVertex( (this.lateralPosition + this.lateralWidth) / crossSectionElement.getWidth_r(), outLine.get(i), add.get(i)));
		graphicsPanel.setColor(color);
		graphicsPanel.setOpaque(opaque);
		if (result.size() > 0)
			graphicsPanel.drawPolygon(result.toArray());
		if (this.conflictList != null) {
			for (PriorityConflict p: this.conflictList) {
				if (p.getConflictArea().npoints > 0) {
					p.paint(graphicsPanel);
				}
			}
		}
	}

	@Override
	public boolean writeXML(StaXWriter staxWriter) {
		return true;	// Currently (!) all StopLines are automatically generated and never stored
	}

}