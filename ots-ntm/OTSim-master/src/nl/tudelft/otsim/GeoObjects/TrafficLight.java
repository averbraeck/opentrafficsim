package nl.tudelft.otsim.GeoObjects;

import java.awt.Color;
import java.util.ArrayList;

import nl.tudelft.otsim.FileIO.ParsedNode;
import nl.tudelft.otsim.FileIO.StaXWriter;
import nl.tudelft.otsim.GUI.GraphicsPanel;
import nl.tudelft.otsim.GUI.InputValidator;
import nl.tudelft.otsim.GUI.Main;
import nl.tudelft.otsim.SpatialTools.Planar;
import nl.tudelft.otsim.Utilities.Reversed;

/**
 * This class implements a traffic light for vehicles.
 * 
 * @author Peter Knoppers
 *
 */
public class TrafficLight extends CrossSectionObject {
	private String ID;
	/** Label in XML representation of a TrafficLight */
	public static final String XMLTAG = "trafficLight";
	
	/** Label of ID in XML representation of a TrafficLight */
	private static final String XML_ID = "ID";
	/** Label of longitudinalPosition in XML representation of a TrafficLight */
	private static final String XML_LONGITUDINALPOSITION = "longitudinalPosition";
	/** Label of length in XML representation of a TrafficLight */
	private static final String XML_LENGTH = "length";
	/** Label of lateralCenter in XML representation of a TrafficLight */
	private static final String XML_LATERALPOSITION = "lateralCenter";
	/** Label of width in XML representation of a TrafficLight */
	private static final String XML_WIDTH = "width";
	
	private static final String ADD_CONTROLLER = "Create new traffic light controller";
	
	private TrafficLightController trafficLightController = null;
	
	/**
	 * Create a new TrafficLight
	 * @param CSE The {@link CrossSectionElement} on which this detector is
	 * positioned
	 * @param longitudinalPosition Double; the distance from the beginning of 
	 * the CrossSectionElement where this TrafficLight begins. If negative, 
	 * the distance is measured from the end of the CrossSectionElement
	 * @param length Double; length of this TrafficLight
	 * @param lateralCenter Double; position of the center of this 
	 * TrafficLight from the center of the CrossSectionElement
	 * @param width Double; width of this TrafficLight
	 * @param ID String; name of this TrafficLight
	 */
	public TrafficLight(CrossSectionElement CSE, double longitudinalPosition, Double length, double lateralCenter, double width, String ID) {
		lateralReference = CrossSectionElement.LateralReferenceCenter;
		this.longitudinalPosition = longitudinalPosition;
		longitudinalLength = length;
		lateralPosition = lateralCenter;
		lateralWidth = width;
		this.ID = ID;
		this.crossSectionElement = CSE;
	}

	/**
	 * Create a new TrafficLight with default longitudinalPosition, length, 
	 * lateralCenter, width and ID.
	 * <br /> The CrossSectionElement must be properly linked into a
	 * CrossSection which must have a valid Link field.
	 * @param CSE The {@link CrossSectionElement} on which this detector is
	 * positioned
	 */
	public TrafficLight(CrossSectionElement CSE) {
		longitudinalPosition = -2;
		lateralReference = CrossSectionElement.LateralReferenceCenter;
		lateralPosition = 0;
		lateralWidth = CSE.getWidth_r();
		// For most situations, uniqueness of traffic light IDs should be
		// enforced over all traffic lights around one node; the code below 
		// only tries to make detector IDs unique within ONE CrossSectionElement
		final String idPrefix = "TL_";
		for (int idRank = 1; null == ID; idRank++) {
			ID = idPrefix + idRank;
			for (CrossSectionObject cso : CSE.getCrossSectionObjects(TrafficLight.class))
				if (((TrafficLight) cso).ID.equals(ID)) {
					ID = null;	// try the next possible value
					break;
				}
		}
		longitudinalLength = 1;
		this.crossSectionElement = CSE;
	}

	/**
	 * Create a TrafficLight from a parsed XML file.
	 * @param crossSectionElement {@link CrossSectionElement}; owner of the new TrafficLight
	 * @param pn {@link ParsedNode}; the root of the TrafficLight in the parsed XML file
	 * @throws Exception
	 */
	public TrafficLight(CrossSectionElement crossSectionElement, ParsedNode pn) throws Exception {
		this.crossSectionElement = crossSectionElement;
		lateralReference = CrossSectionElement.LateralReferenceCenter;
		longitudinalPosition = longitudinalLength = lateralPosition = lateralWidth = Double.NaN;
		ID = null;
		for (String fieldName : pn.getKeys()) {
			String value = pn.getSubNode(fieldName, 0).getValue();
			if (fieldName.equals(XML_ID))
				ID = value;
			else if (fieldName.equals(XML_LONGITUDINALPOSITION))
				longitudinalPosition = Double.parseDouble(value);
			else if (fieldName.equals(XML_LENGTH))
				longitudinalLength = Double.parseDouble(value);
			else if (fieldName.equals(XML_LATERALPOSITION))
				lateralPosition = Double.parseDouble(value);
			else if (fieldName.equals(XML_WIDTH))
				lateralWidth = Double.parseDouble(value);
			else
				throw new Exception("TrafficLight does not have a field " + fieldName);
		}
		if ((null == ID) || Double.isNaN(longitudinalPosition) || Double.isNaN(longitudinalLength) || Double.isNaN(lateralPosition) || Double.isNaN(lateralWidth))
			throw new Exception("TrafficLight is not completely defined" + pn.lineNumber + ", " + pn.columnNumber);
	}

	/**
	 * Return the ID of this TrafficLight.
	 * @return String; the ID of this TrafficLight
	 */
	public String getID_r() {
		return ID;
	}
	
	/**
	 * Change the ID of this TrafficLight.
	 * @param newName String; the new name for this TrafficLight
	 */
	public void setID_w(String newName) {
		if (null != trafficLightController)
			trafficLightController.deleteLight(this);
		this.ID = newName;
		if (null != trafficLightController)
			trafficLightController.addLight(this);
	}
	
	/**
	 * Create an {@link InputValidator} that ensures a proper ID for this
	 * TrafficLight.
	 * @return {@link InputValidator} for a proper TrafficLight ID
	 */
	public InputValidator validateID_v() {
		return new InputValidator(new InputValidator.CustomValidator() {
			@Override
			public boolean validate(String originalValue, String proposedValue) {
				if (! proposedValue.matches("[a-zA-Z_][-a-zA-Z0-9_.]*"))
					return false;	// not a decent name
				if (proposedValue.equals(originalValue))
					return true;	// current name is OK
				// Anything else must be unique among the TrafficLights of the TrafficLightController
				if (null == trafficLightController)
					return true;	// no TrafficLightController; no conflict
				return trafficLightController.lookupTrafficLight(proposedValue) == null;
			}
		});
	}
	
	/**
	 * Retrieve the lateral position of this TrafficLight.
	 * @return Double; the lateral position of this TrafficLight in m from
	 * the center line of the parent {@link CrossSectionElement}
	 */
	public double getLateralPosition_r() {
		return lateralPosition;
	}
	
	/**
	 * Change the lateral position of this TrafficLight.
	 * @param lateralPosition Double; the new lateral position in m from the
	 * center line of the parent (@link CrossSectionElement}
	 */
	public void setLateralPosition_w(double lateralPosition) {
		this.lateralPosition = lateralPosition;
	}
	
	/**
	 * Return an {@link InputValidator} for the lateral position of this
	 * TrafficLight.
	 * @return {@link InputValidator} for the lateral position of this
	 * TrafficLight
	 */
	public InputValidator validateLateralPosition_v() {
		double range = crossSectionElement.getWidth_r() - lateralWidth;
		return new InputValidator("[-.0-9].*", -range / 2, range / 2);
	}
	
	/**
	 * Retrieve the lateral width of this TrafficLight.
	 * @return Double; the lateral width of this TrafficLight in m
	 */
	public double getWidth_r() {
		return lateralWidth;
	}
	
	/**
	 * Change the lateral width of this TrafficLight.
	 * @param width Double; the new lateral width of this TrafficLight in m
	 */
	public void setWidth_w(double width) {
		lateralWidth = width;
	}
	
	/**
	 * Return an {@link InputValidator} for the lateral width of this
	 * TrafficLight.
	 * @return {@link InputValidator} for the lateral width of this
	 * TrafficLight
	 */
	public InputValidator validateWidth_v() {
		double limit = crossSectionElement.getWidth_r() - Math.abs(lateralPosition);
		return new InputValidator("[.0-9].*", 0.1, limit);
	}

	/**
	 * Supply the menu items that allow the user to link this TrafficLight with
	 * a new or an existing TrafficLightController.
	 * @return ArrayList&lt;String&gt;; the items for the menu
	 */
	public ArrayList<String> itemizeTrafficLightController_i() {
		ArrayList<String> result = new ArrayList<String> (crossSectionElement.getCrossSection().getLink().network.trafficControllerList());
		result.add(0, ADD_CONTROLLER);
		return result;
	}
	
	/**
	 * Return a caption to use for the pop up menu of the {@link nl.tudelft.otsim.GUI.ObjectInspector}.
	 * @return String; caption for the pop up menu of the {@link nl.tudelft.otsim.GUI.ObjectInspector}
	 */
	@SuppressWarnings("static-method")
	public String itemizeTrafficLightController_caption() {
		return "Select or create a traffic light controller for this traffic light";
	}
	
	/**
	 * Return the {@link TrafficLightController} associated with this TrafficLight.
	 * @return {@link TrafficLightController}; the controller that operates 
	 * this TrafficLight, or null if no TrafficLightController has been set
	 */
	public Object getTrafficLightController_r() {
		if (null == trafficLightController)
			return "None; click to select or create a traffic light controller";
		return trafficLightController;
	}
	
	/**
	 * Associate this TrafficLight with a {@link TrafficLightController} or
	 * replace the current association and (if needed) create a new
	 * TrafficLightController.
	 * @param object Object; must be a String; name of the
	 * {@link TrafficLightController} or the string ADD_CONTROLLER
	 */
	public void setTrafficLightController_w(Object object) {
		if (null != trafficLightController)
			trafficLightController.deleteLight(this);	// delete current association
		if (object instanceof String) {
			String tlcName = (String) object;
			if (tlcName.equals(ADD_CONTROLLER))	// create a new TrafficLightController
				trafficLightController = crossSectionElement.getCrossSection().getLink().network.addTrafficLightController();
			else	// lookup an existing TrafficLightController
				trafficLightController = crossSectionElement.getCrossSection().getLink().network.lookupTrafficLightController(tlcName);
		} else if (object instanceof TrafficLightController)
			trafficLightController = (TrafficLightController) object;
		if (null != trafficLightController)
			trafficLightController.addLight(this);	// associate with the (new) TrafficLightController
	}
	
	/**
	 * Return the Vertices that form the outline of the detection area of this
	 * TrafficLight
	 * @return ArrayList&lt;{@link Vertex}&gt; vertices of the polygon of this 
	 * TrafficLight
	 */
	public ArrayList<Vertex> getPolygon_r() {
		ArrayList<Vertex> guideLine = Planar.slicePolyline(crossSectionElement.getLinkPointList(lateralReference, true, false), longitudinalPosition, longitudinalLength);
		ArrayList<Vertex> result = Planar.createParallelVertices(guideLine, null, -lateralWidth / 2,  -lateralWidth / 2);
		for (Vertex v : Reversed.reversed(Planar.createParallelVertices(guideLine, null, lateralWidth / 2, lateralWidth / 2)))
			result.add(v);
		return result;
	}
	
	/**
	 * Retrieve the longitudinalPosition of this TrafficLight.
	 * @return Double; the longitudinalPosition of this TrafficLight
	 */
	public double getLongitudinalPosition_r() {
		return longitudinalPosition;
	}
	
	/**
	 * Change the longitudinalPosition of this TrafficLight.
	 * @param longitudinalPosition Double; the new longitudinalPosition of this
	 * TrafficLight
	 */
	public void setLongitudinalPosition_w(double longitudinalPosition) {
		this.longitudinalPosition = longitudinalPosition;
	}
	
	/**
	 * Validate a proposed longitudinalPosition for this TrafficLight.
	 * @return InputValidator for proposed values of the longitudinalPosition 
	 * of this TrafficLight
	 */
	public InputValidator validateLongitudinalPosition_v() {
		double length = crossSectionElement.getCrossSection().getLongitudinalLength();
		return new InputValidator("[-.,0-9].*", -length, length);
	}
	
	/**
	 * A TrafficLight can always be deleted.
	 * <br /> This method is only used by the {@link nl.tudelft.otsim.GUI.ObjectInspector}.
	 * @return Boolean; always true
	 */
	@SuppressWarnings("static-method")
	public boolean mayDeleteTrafficLight_d() {
		return true;
	}
	
	/**
	 * Delete this TrafficLight.
	 */
	public void deleteTrafficLight_d() {
		crossSectionElement.deleteCrossSectionObject(this);
	}
	
	@Override
	public String toString() {
		return String.format(Main.locale, "TrafficLight %s at longitudinalPosition %.3fm, width %.3fm", ID, longitudinalPosition, lateralWidth);
	}
	
	@Override
	public void paint(GraphicsPanel graphicsPanel) {
		graphicsPanel.setStroke(0F);
		graphicsPanel.setColor(Color.RED);
		ArrayList<Vertex> polygon = getPolygon_r();
		//System.out.println("polygon is " + GeometryTools.verticesToString(polygon));
		if (polygon.size() > 0)
			graphicsPanel.drawPolygon(polygon.toArray());
	}

	@Override
	public boolean writeXML(StaXWriter staXWriter) {
		return staXWriter.writeNodeStart(XMLTAG)
				&& staXWriter.writeNode(XML_ID, getID_r())
				&& staXWriter.writeNode(XML_LATERALPOSITION, Double.toString(lateralPosition))
				&& staXWriter.writeNode(XML_WIDTH, Double.toString(lateralWidth))
				&& staXWriter.writeNode(XML_LENGTH, Double.toString(longitudinalLength))
				&& staXWriter.writeNode(XML_LONGITUDINALPOSITION, Double.toString(longitudinalPosition))
				&& staXWriter.writeNodeEnd(XMLTAG);
	}

}
