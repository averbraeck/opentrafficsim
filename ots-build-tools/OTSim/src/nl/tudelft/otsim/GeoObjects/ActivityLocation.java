package nl.tudelft.otsim.GeoObjects;

import java.awt.Color;
import java.awt.geom.Point2D;

import nl.tudelft.otsim.FileIO.ParsedNode;
import nl.tudelft.otsim.FileIO.StaXWriter;
import nl.tudelft.otsim.FileIO.XML_IO;
import nl.tudelft.otsim.GUI.GraphicsPanel;
import nl.tudelft.otsim.GUI.InputValidator;

public class ActivityLocation extends Vertex implements XML_IO {
	/** Name for an ActivityLocation element when stored in XML format */
	public static final String XMLTAG = "activityLocation";
	
	/** Name for an ActivityLocation ID element when stored in XML format */
	private static final String XML_ID = "ID";
	/** Name for an ActivityLocation geometry element when stored in XML format */
	private static final String XML_GEOMETRY = "geometry";
	/** Name for an ActivityLocation name element when stored in XML format */
	private static final String XML_NAME = "name";
	/** Name for an ActivityLocation activityTypes element when stored in XML format */
	private static final String XML_ACTIVITYTYPES = "activityTypes";
	/** Name for an ActivityLocation parkingLots element when stored in XML format */
	private static final String XML_PARKINGLOTS = "parkingLots";
	
	private String name;
	private int activityLocationID;
	private int parkingLots;
	private Node fromNodeNearLocation;
	private Lane laneNearLocation;
	private Link linkNearLocation;
	private Vertex pointAtLinkNearLocation;	
	private Network network;
	private PolyZone polyZone;	
	private MicroZone microZone;
	
	/**
	 * Create a new ActivityLocation.
	 * @param network Network that owns the ActivityLocation
	 * @param name Description of the ActivityLocation
	 * @param parkingLots Number of parking lots per building (???)
	 * @param point Location of the ActivityLocation
	 */
	public ActivityLocation(Network network, String name, int parkingLots, Vertex point) {
		super(point);
		this.name = name;
		this.parkingLots = parkingLots;
		this.network = network;
	}

	public ActivityLocation(Network network, String name, int activityLocationID, MicroZone microZone, int parkingLots, Vertex point) {
		super(point);
		this.name = name;
		this.activityLocationID = activityLocationID;
		this.parkingLots = parkingLots;
		this.network = network;
		this.microZone = microZone;
	}
	
	public ActivityLocation(Network network, String name, int activityLocationID, PolyZone polyZone, int parkingLots, Vertex point) {
		super(point);
		this.name = name;
		this.activityLocationID = activityLocationID;
		this.parkingLots = parkingLots;
		this.network = network;
		this.polyZone = polyZone;
	}
	
	public ActivityLocation(Network network, int activityLocationID, String name, int parkingLots, Vertex point) {
		super(point);
		this.name = name;
		this.activityLocationID = activityLocationID;
		this.parkingLots = parkingLots;
		this.network = network;
	}
	
	public ActivityLocation(Network network, ParsedNode pn) throws Exception {
		this.network = network;
		x = y = z = Double.NaN;
		name = null;
		activityLocationID = parkingLots = -1;
		microZone = null;
		polyZone = null;
		
		//System.out.print("pn is " + pn.toString(""));
		for (String fieldName : pn.getKeys()) {
			String value = pn.getSubNode(fieldName, 0).getValue();
			if (fieldName.equals(XML_NAME))
				name = value;
			else if (fieldName.equals(XML_ID))
				activityLocationID = Integer.parseInt(value);
			else if (fieldName.equals(XML_PARKINGLOTS))
				parkingLots = Integer.parseInt(value);
			else if (fieldName.equals(Vertex.XML_X))
				x = Double.parseDouble(value);
			else if (fieldName.equals(Vertex.XML_Y))
				y = Double.parseDouble(value);
			else if (fieldName.equals(Vertex.XML_Z))
				z = Double.parseDouble(value);
			else
				throw new Exception("Unknown / unhandled field in ActivityLocation: " + fieldName + " at " + pn.lineNumber + ", " + pn.columnNumber);
		}
		if (null == name)
			throw new Exception("ActivityLocation does not have a name at " + pn.lineNumber + ", " + pn.columnNumber);
		if (-1 == activityLocationID)
			throw new Exception("ActivityLocation does not have an ID at " + pn.lineNumber + ", " + pn.columnNumber);
		// TODO: complain about all other fields that MUST be defined 
	}

	/**
	 * Retrieve the name of this ActivityLocation.
	 * @return String; the name of this ActivityLocation
	 */
	public String getName_r() {
		return name;
	}

	/**
	 * Set/change the name of this ActivityLocation.
	 * @param name String; the new name of this ActivityLocation
	 */
	public void setName_w(String name) {
		this.name = name;
	}

	public int getActivityLocationID_r() {
		return activityLocationID;
	}

	public void setActivityLocationID_w(int activityLocationID) {
		this.activityLocationID = activityLocationID;
	}
	
	public Lane getLaneNearLocation() {
		return laneNearLocation;
	}

	public void setLaneNearLocation(Lane laneNearLocation) {
		this.laneNearLocation = laneNearLocation;
	}

	public Vertex getPointAtLinkNearLocation() {
		return pointAtLinkNearLocation;
	}

	public Node getFromNodeNearLocation() {
		return fromNodeNearLocation;
	}

	public void setFromNodeNearLocation(Node node) {
		this.fromNodeNearLocation = node;
	}

	public void setPointAtLinkNearLocation(Vertex pointAtLinkNearLocation) {
		this.pointAtLinkNearLocation = pointAtLinkNearLocation;
	}

	public Link getLinkNearLocation() {
		return linkNearLocation;
	}

	public void setLinkNearLocation(Link linkNearLocation) {
		this.linkNearLocation = linkNearLocation;
	}

	public PolyZone getPolyZone() {
		return polyZone;
	}

	public void setPolyZone(PolyZone polyZone) {
		this.polyZone = polyZone;
	}

	public MicroZone getMicroZone() {
		return microZone;
	}

	public void setMicroZone(MicroZone microZone) {
		this.microZone = microZone;
	}

	public int getParkingLots_r() {
		return parkingLots;
	}

	public void setParkingLots_w(int parkingLots) {
		this.parkingLots = parkingLots;
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
	 * Paint this ActivityLocation on a {@link GraphicsPanel}.
	 * @param graphicsPanel {@link GraphicsPanel}; the painting surface
	 */
	@Override
	public void paint(GraphicsPanel graphicsPanel) {
        final int nonSelectedActivityLocationDiameter = 8;
        final int selectedActivityLocationDiameter = 25;
    	Point2D.Double point = getPoint();            
    	graphicsPanel.setStroke(1F);
        graphicsPanel.setColor(Color.BLACK);
        graphicsPanel.drawString(getName_r(), point);
        graphicsPanel.drawCircle(point, Color.BLUE, nonSelectedActivityLocationDiameter);
        graphicsPanel.setStroke(8f);
        if (network.selectedActivityLocation == this)   {
            graphicsPanel.drawCircle(point, Color.BLUE, selectedActivityLocationDiameter); 
        }

/*        Vertex point2 = getPointAtLinkNearLocation();
        point = new Point2D.Double(point2.getX(), point2.getY());
        graphicsPanel.drawCircle(point, Color.RED, nonSelectedBuildingDiameter);*/
	}

	@Override
	public boolean writeXML(StaXWriter staXWriter) {
		// TODO Auto-generated method stub
		return false;
	}
	
}