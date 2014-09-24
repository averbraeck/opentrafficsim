package nl.tudelft.otsim.GeoObjects;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import nl.tudelft.otsim.FileIO.ParsedNode;
import nl.tudelft.otsim.FileIO.StaXWriter;
import nl.tudelft.otsim.FileIO.XML_IO;
import nl.tudelft.otsim.GUI.GraphicsPanel;
import nl.tudelft.otsim.GUI.InputValidator;

public class MicroZone extends Vertex implements XML_IO {
	private static final long serialVersionUID = 1L;
	
	/** Name for a MicroZone element when stored in XML format */
	public static final String XMLTAG = "microZone";
	
	/** Name for a MicroZone name element when stored in XML format */
	private static final String XML_NAME = "name";
	/** Name for a MicroZone ID element when stored in XML format */
	private static final String XML_ID = "ID";
	/** Name for a MicroZone nodes element when stored in XML format */
	private static final String XML_NODES = "nodes";
	/** Name for a MicroZone activities element when stored in XML format */
	private static final String XML_ACTIVITIES = "activities";
	
	private String name;
    private int zoneID;
	private Network network;
	private List<ActivityLocation> activityList;
	private List<Link> microZoneLink;
	private List<Integer> nodeList;
   
    public MicroZone (Network network, String zoneName, List<Integer> nodeList, List<ActivityLocation> activityList, int zoneID, double X, double Y, double Z) {
        super(X, Y, Z);
        // this.vertex = new Vertex(X,Y,Z);
        this.name = zoneName;
        this.zoneID = zoneID;
        this.network = network;
        this.activityList = activityList;
        this.nodeList = nodeList;
    }
    
    // Duplicate a microZone
    public MicroZone (Network network, MicroZone aZone) {
        this(network, aZone.getName_r(), aZone.getNodeList(), aZone.getActivityList(), aZone.getZoneID(), aZone.getX(), aZone.getY(), aZone.getZ());
    }
    
    /**
     * Create a MicroZone from a parsed XML file.
     * @param network {@link Network} that will own the new MicroZone
     * @param pn {@link ParsedNode}; the root of the MicroZone in the parsed XML file
     * @throws Exception
     */
	public MicroZone(Network network, ParsedNode pn) throws Exception {
		this.network = network;
		activityList = new ArrayList<ActivityLocation>();
		microZoneLink = new ArrayList<Link>();
		nodeList = new ArrayList<Integer>();
		name = null;
		x = y = z = Double.NaN;
		zoneID = -1;
		
		for (String fieldName : pn.getKeys()) {
			String value = pn.getSubNode(fieldName, 0).getValue();
			if (fieldName.equals(XML_NAME))
				name = value;
			else if (fieldName.equals(XML_ACTIVITIES))
				; // FIXME: parse the activities
			else if (fieldName.equals(XML_ID)) {
				zoneID = Integer.parseInt(value);
				if (null != network.lookupMicroZone(zoneID))
					throw new Exception("MicroZone with ID " + value + " already defined");
			} else if (fieldName.equals(XML_NODES))
				for (int index = 0; index < pn.size(fieldName); index++)
					nodeList.add(new Integer(Integer.parseInt(pn.getSubNode(fieldName, index).getValue())));
			else if (fieldName.equals(Vertex.XML_X))
				x = Double.parseDouble(value);
			else if (fieldName.equals(Vertex.XML_Y))
				y = Double.parseDouble(value);
			else if (fieldName.equals(Vertex.XML_Z))
				z = Double.parseDouble(value);
			else
				throw new Exception("Unknown field in MicroZone: " + fieldName + " " + pn.lineNumber + ", " + pn.columnNumber);
		}
		if (null == name)
			throw new Exception("MicroZone does not have a name " + pn.lineNumber + pn.columnNumber);
		if (-1 == zoneID)
			throw new Exception("MicroZone doew not have an ID " + pn.lineNumber + ", " + pn.columnNumber);
		// TODO should probably complain if x and/or y are NaN
	}

	public List<Link> getMicroZoneLink() {
		return microZoneLink;
	}

	public void setMicroZoneLink(List<Link> microZoneLink) {
		this.microZoneLink = microZoneLink;
	}
	
	public String getName_r() {
		return name;
	}

	public void setName_w(String nodeName) {
		this.name = nodeName;
	}

	public List<Integer> getNodeList() {
		return nodeList;
	}

	public void setNodeList(List<Integer> nodeList) {
		this.nodeList = nodeList;
	}

	public int getZoneID() {
		return zoneID;
	}

	public List<ActivityLocation> getActivityList() {
		return activityList;
	}

	public void setActivityList(List<ActivityLocation> activityList) {
		this.activityList = activityList;
	}

	public double X_r() {
		return getX();
	}
	
	public double Y_r() {
		return getY();
	}
	
	public double Z_r() {
		return getZ();
	}
	
	public void setZoneID(int zoneID) {
		this.zoneID = zoneID;
	}
	
	public InputValidator validateName_v() {
		return new InputValidator(new InputValidator.CustomValidator() {
			@Override
			public boolean validate(String originalValue, String proposedValue) {
				if (! proposedValue.matches("[a-zA-Z_][-a-zA-Z0-9_.]*"))
					return false;	// not a decent name
				if (proposedValue.equals(originalValue))
					return true;	// current name is OK
				// Anything else must be unique among the node names
				return network.lookupNode(proposedValue, false) == null;
			}
		});
	}
	
	/**
	 * Draw this MicroZone on a {@link GraphicsPanel}.
	 * @param graphicsPanel GraphicsPanel to draw this MicroZone on
	 */
	public void paint(GraphicsPanel graphicsPanel) {
        final int nonSelectedMicroZoneDiameter = 15;
        final int selectedMicroZoneDiameter = 25;
    	Point2D.Double point = getPoint();            
    	graphicsPanel.setStroke(3F);
        graphicsPanel.setColor(Color.BLACK);
        graphicsPanel.drawString(getName_r(), point);
        graphicsPanel.drawCircle(point, Color.RED, nonSelectedMicroZoneDiameter);
        graphicsPanel.setStroke(8f);
        if (network.selectedMicroZone == this)   {
            graphicsPanel.drawCircle(point, Color.BLUE, selectedMicroZoneDiameter); 
        }
	}
	
	private boolean writeNodeList(StaXWriter staXWriter) {
		for (int nodeNumber : nodeList)
			if (! staXWriter.writeNode(XML_NODES, Integer.toString(nodeNumber)))
				return false;
		return true;
	}

	@Override
	public boolean writeXML(StaXWriter staXWriter) {
		return staXWriter.writeNodeStart(XMLTAG)
				&& staXWriter.writeNode(XML_NAME, getName_r())
				&& staXWriter.writeNode(XML_ID, Integer.toString(getZoneID()))
				&& staXWriter.writeNode(Vertex.XML_X, Double.toString(getX()))
				&& staXWriter.writeNode(Vertex.XML_Y, Double.toString(getY()))
				&& staXWriter.writeNode(Vertex.XML_Z, Double.toString(getZ()))
				&& writeNodeList(staXWriter)
				&& staXWriter.writeNodeEnd(XMLTAG);
	}
}