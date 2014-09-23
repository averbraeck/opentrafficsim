package nl.tudelft.otsim.GeoObjects;

import java.awt.Color;
import java.util.ArrayList;

import nl.tudelft.otsim.FileIO.ParsedNode;
import nl.tudelft.otsim.FileIO.StaXWriter;
import nl.tudelft.otsim.FileIO.XML_IO;
import nl.tudelft.otsim.GUI.GraphicsPanel;
import nl.tudelft.otsim.GUI.Main;
import nl.tudelft.otsim.SpatialTools.Planar;

/**
 * This class holds the data for road markers in the driving direction.
 *  
 * @author gftamminga, Peter Knoppers
 */
public class RoadMarkerAlong extends CrossSectionObject implements XML_IO {
	/** Name for a RoadMarkerAlong element when stored in XML format */
	public static final String XMLTAG = "roadMarkerAlong";
	
	/** Name for a RoadMarkerAlong type element when stored in XML format */
	private static final String XML_NAME = "type"; 
	///** Name for a RoadMarkerAlong width element when stored in XML format */
	//private static final String XML_WIDTH = "width";
	/** Name for a RoadMarkerAlong lateralPosition element when stored in XML format */
	private static final String XML_LATERALPOSITION = "lateralPosition";
	private String type;
	private ArrayList<Vertex> vertices;
	
	/**
	 * Retrieve the width of <b>each stripe element</b> this RoadMarkerAlong.
	 * @return double; the width of this RoadMarkerAlong in m
	 */
	public double getMarkerWidth() {
		return lateralWidth;
	}

	/**
	 * Change the width of <b>each stripe element</b> this RoadMarkerAlong.
	 * @param lateralWidth Double; the new width of this RoadMarkerAlong in m
	 */
	public void setMarkerWidth(double lateralWidth) {
		this.lateralWidth = lateralWidth;
	}

	/**
	 * Create a new RoadMarkerAlong.
	 * @param type String; type of the new RoadMarkerAlong; this should be one
	 * of the values "|", "||", ":", ":|", "|:"
	 * @param lateralPosition Double; lateral position of the new RoadMarkerAlong
	 * in m from the left of the left edge of the {@link CrossSectionElement}
	 * that holds this new RoadMarkerAlong
	 */
	public RoadMarkerAlong(String type, double lateralPosition) {
		this.type = type;
		this.lateralPosition = lateralPosition;	
	}

	/**
	 * Duplicate a RoadMarkerAlong
	 * @param rma RoadMarkerAlong that must be duplicated
	 */
	public RoadMarkerAlong(RoadMarkerAlong rma) {
		this.type = rma.type;
		this.lateralPosition = rma.lateralPosition;
	}

	/**
	 * Create a RoadMarkerAlong from a parsed XML file.
	 * @param parent {@link CrossSectionElement} owner of the new RoadMarkerAlong
	 * @param pn {@link ParsedNode}; root of the RoadMarkerAlong in the parsed XML file
	 * @throws Exception
	 */
	public RoadMarkerAlong(CrossSectionElement parent, ParsedNode pn) throws Exception {
		crossSectionElement = parent;
		type = null;
		lateralPosition = Double.NaN;
		
		for (String fieldName : pn.getKeys()) {
			String value = pn.getSubNode(fieldName, 0).getValue();
			if (fieldName.equals(XML_NAME))
				type = value;
			else if (fieldName.equals(XML_LATERALPOSITION))
				lateralPosition = Double.parseDouble(value);
			else
				throw new Exception("Unknown field in RoadMarkerAlong: " + fieldName);
		}
		if (null == type)
			throw new Exception("RoadMarkerAlong has no type " + pn.lineNumber + ", " + pn.columnNumber);
		if (Double.isNaN(lateralPosition))
			throw new Exception("RoadMarkerAlong has not lateralPosition " + pn.lineNumber + ", " + pn.columnNumber);
	}

	/**
	 * Retrieve the list of {@link Vertex Vertices} that describes the path of this RoadMarkerAlong
	 * @return ArrayList&lt;{@link Vertex}&gt;; the list of Vertices that describes the path of this RoadMarkerAlong
	 */
	public ArrayList<Vertex> getVertices() {
		return vertices;
	}

	public void setVertices(ArrayList<Vertex> vertices) {
		this.vertices = vertices;
	}

	/**
	 * Create or re-create the vertices for the path of this RoadMarkerAlong.
	 */
	public void createVertices(CrossSectionElement cse) {
		if (null == cse)
			throw new Error ("The crossSectionElement of this RoadMarkerAlong is null");
		ArrayList<Vertex> innerVertices = cse.createAndCleanLinkPointListInner(false, true, false);
		ArrayList<Vertex> outerVertices = cse.createAndCleanLinkPointListOuter(false, true, false);
		while (innerVertices.size() != outerVertices.size()) {
			System.out.println("RMA: createVertices: inner and outer vertices have unequal length:");
			System.out.println("inner: " + Planar.verticesToString(innerVertices));
			System.out.println("outer: " + Planar.verticesToString(outerVertices));
			// This it not perfect, but it probably does nicely
			if (innerVertices.size() > outerVertices.size())
				innerVertices.remove(1);
			else
				outerVertices.remove(1);
		}
		vertices = new ArrayList<Vertex>(); 
		double ratio = getLateralPosition() / cse.getCrossSectionElementWidth();
		for (int i = 0; i < innerVertices.size(); i++)
			vertices.add(Vertex.weightedVertex(ratio, innerVertices.get(i), outerVertices.get(i)));
	}
	
	/**
	 * Retrieve the type of this RoadMarkerAlong.
	 * @return String; the type of this RoadMarkerAlong
	 */
	public String getType() {
		return type;
	}

	/**
	 * Modify the type of this RoadMarkerAlong.
	 * @param type String; the new type of this RoadmarkerAlong; this should be 
	 * one of the values "|", "||", ":", ":|", "|:"
	 */
	public void setType(String type) {
		this.type = type;
	}


	@Override
	public void paint(GraphicsPanel graphicsPanel) {
		ArrayList<Vertex> vertices = getVertices();
		if (null == vertices) {
			System.err.println("RoadMarkerAlong.paint: getRmaVertices returned null");
			return;
		}
		final double stripeSpacing = 0.3;
		graphicsPanel.setColor(Color.YELLOW);
		for (int i = 0; i < type.length(); i++) {
			String stripeType = type.substring(i, i + 1);
			if (stripeType.equals(":"))
				graphicsPanel.setStroke(GraphicsPanel.DASHED, (float) lateralWidth, 0f);
			else if (stripeType.equals("|"))
				graphicsPanel.setStroke(GraphicsPanel.SOLID, (float) lateralWidth, 0f);
			else
				throw new Error("Unknown stripe type \"" + stripeType + "\"");
			double offset = stripeSpacing * (i - (type.length() - 1) / 2.0);
			ArrayList<Vertex> stripeCenterLine = Planar.createParallelVertices(vertices, null, offset, offset);
			graphicsPanel.drawPolyLine(stripeCenterLine);
		}
	}
	
	@Override
	public String toString() {
		return String.format(Main.locale, "RoadMarkerAlong of type %s at lateralPosition %.3fm", type, lateralPosition);
	}

	@Override
	public boolean writeXML(StaXWriter staXWriter) {
		return staXWriter.writeNodeStart(XMLTAG)
				&& staXWriter.writeNode(XML_NAME, getType())
				&& staXWriter.writeNode(XML_LATERALPOSITION, Double.toString(getLateralPosition()))
				&& staXWriter.writeNodeEnd(XMLTAG);
	}
	
}
