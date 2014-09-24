package nl.tudelft.otsim.GeoObjects;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import nl.tudelft.otsim.FileIO.ParsedNode;
import nl.tudelft.otsim.FileIO.StaXWriter;
import nl.tudelft.otsim.FileIO.XML_IO;
import nl.tudelft.otsim.GUI.GraphicsPanel;

public class PolyZone implements XML_IO {
	/** Name for a PolyZone element when stored in XML format */
	public static final String XMLTAG = "polyZone";
	
	/** Name for a PolyZone name element when stored in XML format */
	private static final String XML_NAME = "name";
	/** Name for a PolyZone ID element when stored in XML format */
	private static final String XML_ID = "ID";
	/** Name for a PolyZone building element when stored in XML format */
	private static final String XML_BUILDING = "building";
	/** Name for a PolyZone polygon element when stored in XML format */
	private static final String XML_POLYGON = "polygon";
	
	private int zoneCount = 0;
	private String name;
    private int zoneID;
	private ArrayList<Vertex> pointList;
	private List<ActivityLocation> activityLocationList;
	private final Network network;
	
    public PolyZone (Network network, String zoneName, List<ActivityLocation> activityLocationList, ArrayList<Vertex> pointList) {
        this.name = zoneName;
        this.zoneID = zoneCount;
        this.network = network;
        this.activityLocationList = activityLocationList;
        this.pointList = pointList;
    }
   
    public PolyZone (Network network, String zoneName, List<ActivityLocation> activityLocationList, int zoneID, ArrayList<Vertex> pointList) {
        this.name = zoneName;
        this.zoneID = zoneID;
        this.network = network;
        this.activityLocationList = activityLocationList;
        this.pointList = pointList;
    }
 
	public List<ActivityLocation> getActivityLocationList() {
		return activityLocationList;
	}

	public void setActivityLocationList(List<ActivityLocation> activityLocationList) {
		this.activityLocationList = activityLocationList;
	}

	// Duplicate a polyZone
    public PolyZone (Network network, PolyZone aZone) {
        this(network, aZone.getName_r(), aZone.getActivityLocationList(), aZone.getZoneID(), aZone.getPointList());
    }
    
    public PolyZone(Network network, ParsedNode pn) throws Exception {
    	this.network = network;
    	zoneID = -1;
    	name = null;
    	activityLocationList = new ArrayList<ActivityLocation>();
    	pointList = new ArrayList<Vertex>();
    	
		for (String fieldName : pn.getKeys()) {
			String value = pn.getSubNode(fieldName, 0).getValue();
			if (fieldName.equals(XML_NAME))
				name = value;
			else if (fieldName.equals(XML_ID)) {
				int id = Integer.parseInt(value);
				if (null != network.lookupPolyZone(id))
					throw new Exception("PolyZone with ID " + value + " already defined");
				zoneID = id;
			} else if (fieldName.equals(XML_BUILDING))
				; // FIXME; ignored for now
			else if (fieldName.equals(XML_POLYGON)) {
				// TODO fix the format of a polygon in the XML file
				ParsedNode verticesNode = pn.getSubNode(fieldName, 0);
				for (int index = 0; index < verticesNode.size(Vertex.XML_X); index++)
					pointList.add(new Vertex(Double.parseDouble(verticesNode.getSubNode(Vertex.XML_X, index).getValue()), 
							Double.parseDouble(verticesNode.getSubNode(Vertex.XML_Y, index).getValue()), 
							Double.parseDouble(verticesNode.getSubNode(Vertex.XML_Z, index).getValue())));			
			} else
				throw new Exception("Unknown field in PolyZone: " + fieldName);
		}
		if (-1 ==zoneID)
			throw new Exception("ID of PolyZone not defined " + pn.lineNumber + ", " + pn.columnNumber);
		if (null == name)
			throw new Exception("Name of PolyZone not defined " + pn.lineNumber + ", " + pn.columnNumber);
		if (pointList.size() < 3)
			throw new Exception("Polygon of PolyZone not or poorly defined " + pn.lineNumber + ", " + pn.columnNumber);
	}

	public ArrayList<Vertex> getPointList() {
		return pointList;
	}

	public void setPointList(ArrayList<Vertex> pointList) {
		this.pointList = pointList;
	}

	public String getName_r() {
		return name;
	}

	public void setName_w(String nodeName) {
		this.name = nodeName;
	}

	public int getZoneID() {
		return zoneID;
	}
	
	public void setZoneID(int zoneID) {
		this.zoneID = zoneID;
	}
	
	/**
	 * Paint this PolyZone on a {@link GraphicsPanel}.
	 * @param graphicsPanel {@link GraphicsPanel}; the graphicsPanel to paint
	 * this PolyZone on
	 */
	public void paint(GraphicsPanel graphicsPanel) {
    	Point2D.Double[] outline = new Point2D.Double[getPointList().size()];
    	int nextPoint = 0;
    	for (Vertex v : getPointList())
    		outline[nextPoint++] = v.getPoint();
    	nextPoint = outline.length;
    	graphicsPanel.setStroke(3F);
    	Color color = new Color(1f, 0f, 0f, 0.1f);
    	graphicsPanel.setColor(color);
    	graphicsPanel.drawPolygon(outline);
        graphicsPanel.setStroke(8f);
        graphicsPanel.setColor(Color.BLACK);
        graphicsPanel.drawString(getName_r(), outline[0]);
	}

	@Override
	public boolean writeXML(StaXWriter staXWriter) {
		// TODO Auto-generated method stub
		return false;
	}
}