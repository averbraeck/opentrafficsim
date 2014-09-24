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
 * This class implements a detector for vehicles.
 * <br />
 * As some simulators will only inform detectors when a vehicle enters the
 * detector range, this class holds an ArrayList of vehicles that have entered
 * the range and interrogates those whenever a current census is requested.
 * 
 * @author Peter Knoppers
 */
public class VehicleDetector extends CrossSectionObject {
	/** Minimum longitudinal length of a VehicleDetector */
	final public double minimumLongitudinalLength = 0.001;	// meter
	/** Label in XML representation of a VehicleDetector */
	public static final String XMLTAG = "vehicleDetector";
	
	/** Label of ID in XML representation of a VehicleDetector */
	private static final String XML_ID = "ID";
	/** Label of longitudinalPosition in XML representation of a VehicleDetector */
	private static final String XML_LONGITUDINALPOSITION = "longitudinalPosition";
	/** Label of length in XML representation of a VehicleDetector */
	private static final String XML_LENGTH = "length";
	/** Label of lateralCenter in XML representation of a VehicleDetector */
	private static final String XML_LATERALPOSITION = "lateralCenter";
	/** Label of width in XML representation of a VehicleDetector */
	private static final String XML_WIDTH = "width";
	
	private String ID;
	private Node node;

	/**
	 * Create a new VehicleDetector
	 * @param CSE The {@link CrossSectionElement} on which this detector is
	 * positioned
	 * @param longitudinalPosition Double; the distance from the beginning of 
	 * the CrossSectionElement where this VehicleDetector begins. If negative, 
	 * the distance is measured from the end of the CrossSectionElement
	 * @param length Double; length of this VehicleDetector
	 * @param lateralCenter Double; position of the center of this 
	 * VehicleDetector from the center of the CrossSectionElement
	 * @param width Double; width of this VehicleDetector
	 * @param ID String; name of this VehicleDetector
	 */
	public VehicleDetector(CrossSectionElement CSE, double longitudinalPosition, double length, double lateralCenter, double width, String ID) {
		lateralReference = CrossSectionElement.LateralReferenceCenter;
		this.longitudinalPosition = longitudinalPosition;
		longitudinalLength = length;
		lateralPosition = lateralCenter;
		lateralWidth = width;
		this.ID = ID;
		this.crossSectionElement = CSE;
	}
	
	/**
	 * Create a new VehicleDetector with default longitudinalPosition, length, 
	 * lateralCenter, width and ID.
	 * <br /> The CrossSectionElement must be properly linked into a
	 * CrossSection which must have a valid Link field.
	 * @param CSE The {@link CrossSectionElement} on which this detector is
	 * positioned
	 */
	public VehicleDetector(CrossSectionElement CSE) {
		// Find a suitable longitudinalPosition for a new VehicleDetector
		longitudinalPosition = 0;
		for (CrossSectionObject cso : CSE.getCrossSectionObjects(VehicleDetector.class)) {
			VehicleDetector other = (VehicleDetector) cso;
			if ((longitudinalPosition >= other.longitudinalPosition) && (longitudinalPosition <= other.longitudinalPosition + other.longitudinalLength))
				longitudinalPosition = other.longitudinalPosition + other.longitudinalLength + 1;
		}
		if (longitudinalPosition >= CSE.getCrossSection().getLongitudinalLength())
			longitudinalPosition = 0;	// No reasonable position found; reset to 0m
		lateralReference = CrossSectionElement.LateralReferenceCenter;
		lateralPosition = 0;
		lateralWidth = CSE.getWidth_r();
		// For most situations, uniqueness of detector IDs should be
		// enforced over all detectors around one node; the code below only
		// tries to make detector IDs unique within ONE CrossSectionElement
		final String idPrefix = "VD_";
		for (int idRank = 1; null == ID; idRank++) {
			ID = idPrefix + idRank;
			for (CrossSectionObject cso : CSE.getCrossSectionObjects(VehicleDetector.class))
				if (((VehicleDetector) cso).ID.equals(ID)) {
					ID = null;	// try the next possible value
					break;
				}
		}
		longitudinalLength = 1;
		this.crossSectionElement = CSE;
	}
	
	/**
	 * Create a VehicleDetector from a parsed XML file.
	 * @param crossSectionElement {@link CrossSectionElement}; owner of the new VehicleDetector
	 * @param pn {@link ParsedNode}; the root of the VehicleDetector in the parsed XML file
	 * @throws Exception
	 */
	public VehicleDetector(CrossSectionElement crossSectionElement, ParsedNode pn) throws Exception {
		this.crossSectionElement = crossSectionElement;
		lateralReference = CrossSectionElement.LateralReferenceCenter;
		ID = null;
		longitudinalPosition = lateralPosition = lateralWidth = longitudinalLength = Double.NaN;
		
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
				throw new Exception("VehicleDetector does not have a field " + fieldName);
		}
		if ((null == ID) || Double.isNaN(longitudinalPosition) || Double.isNaN(longitudinalLength) || Double.isNaN(lateralPosition) || Double.isNaN(lateralWidth))
			throw new Exception("VehicleDetector is not completely defined " + pn.lineNumber + ", " + pn.columnNumber);
	}

	/**
	 * Return the ID of this VehicleDetector.
	 * @return String; the ID of this VehicleDetector
	 */
	public String getID_r() {
		return ID;
	}
	
	/**
	 * Change the ID of this Detector.
	 * @param newName String; the new name for this Detector
	 */
	public void setID_w(String newName) {
		this.ID = newName;
	}
	
	/**
	 * Create an {@link InputValidator} that ensures a proper ID for this
	 * VehicleDetector.
	 * @return {@link InputValidator} for a proper VehicleDetector ID
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
				if (null == node)
					return true;	// no TrafficLightController; no conflict
				TrafficLightController tc = node.getTrafficLightController();
				if (null == tc)
					return true;
				return tc.lookupDetector(proposedValue) == null;
			}
		});
	}
	
	/**
	 * Supply the menu items that allow the user to link this TrafficLight with
	 * a new or an existing TrafficLightController.
	 * @return ArrayList&lt;String&gt;; the items for the menu
	 */
	public ArrayList<String> itemizeTrafficLightController_i() {
		ArrayList<String> result = new ArrayList<String>();
		for (TrafficLightController tlc : crossSectionElement.getCrossSection().getLink().network.trafficLightControllerList())
			result.add(tlc.getName_r());
		return result;
	}
	
	/**
	 * Return a caption to use for the pop up menu of the {@link nl.tudelft.otsim.GUI.ObjectInspector}.
	 * @return String; caption for the pop up menu of the {@link nl.tudelft.otsim.GUI.ObjectInspector}
	 */
	@SuppressWarnings("static-method")
	public String itemizeTrafficLightController_caption() {
		return "Select or create a traffic light controller for this detector";
	}
	
	private TrafficLightController getTrafficLightController() {
		if (null == node)
			return null;
		return node.getTrafficLightController();
	}
	
	/**
	 * Return the {@link TrafficLightController} associated with this VehicleDetector.
	 * @return {@link TrafficLightController}; the controller that operates 
	 * this VehicleDetector, or null if no TrafficLightController has been set
	 */
	public Object getTrafficLightController_r() {
		TrafficLightController tc = getTrafficLightController();
		if (null == tc)
			return "None; click to select or create a traffic light controller";
		return tc;
	}
	
	/**
	 * Associate this TrafficLight with a {@link TrafficLightController} or
	 * replace the current association and (if needed) create a new
	 * TrafficLightController.
	 * @param object Object; must be a String; name of the
	 * {@link TrafficLightController} or the string ADD_CONTROLLER
	 */
	public void setTrafficLightController_w(Object object) {
		TrafficLightController tc = getTrafficLightController();
		if (null != tc)
			tc.deleteDetector(this);	// delete current association
		if (object instanceof String) {
			String tlcName = (String) object;
			tc = crossSectionElement.getCrossSection().getLink().network.lookupTrafficLightController(tlcName);
		} else if (object instanceof TrafficLightController)
			tc = (TrafficLightController) object;
		if (null != tc)
			tc.addDetector(this);	// associate with the (new) TrafficLightController
	}
	
	/**
	 * Return the Vertices that form the outline of the detection area of this
	 * VehicleDetector
	 * @return ArrayList&lt;{@link Vertex}&gt; vertices of the polygon of this 
	 * VehicleDetector
	 */
	public ArrayList<Vertex> getPolygon_r() {
		ArrayList<Vertex> guideLine = Planar.slicePolyline(crossSectionElement.getLinkPointList(lateralReference, true, false), longitudinalPosition, longitudinalLength);
		while (guideLine.size() > 2)	// We only want the first and last point
			guideLine.remove(1);
		ArrayList<Vertex> result = Planar.createParallelVertices(guideLine, null, lateralPosition - lateralWidth / 2,  lateralPosition - lateralWidth / 2);
		for (Vertex v : Reversed.reversed(Planar.createParallelVertices(guideLine, null, lateralPosition + lateralWidth / 2, lateralPosition + lateralWidth / 2)))
			result.add(v);
		return result;
	}
	
	/**
	 * Retrieve the longitudinalPosition of this VehicleDetector.
	 * @return Double; the longitudinalPosition of this VehicleDetector
	 */
	public double getLongitudinalPosition_r() {
		return longitudinalPosition;
	}
	
	/**
	 * Change the longitudinalPosition of this VehicleDetector.
	 * @param longitudinalPosition Double; the new longitudinalPosition of this
	 * VehicleDetector
	 */
	public void setLongitudinalPosition_w(double longitudinalPosition) {
		this.longitudinalPosition = longitudinalPosition;
	}
	
	/**
	 * Validate a proposed longitudinalPosition for this VehicleDetector.
	 * @return InputValidator for proposed values of the longitudinalPosition 
	 * of this VehicleDetector
	 */
	public InputValidator validateLongitudinalPosition_v() {
		return new InputValidator(new InputValidator.CustomValidator() {
			@Override
			public boolean validate(String originalValue, String proposedValue) {
				if (! proposedValue.matches("[.,0-9].*"))
					return false;	// does not look like a number
				try {
					double length = crossSectionElement.getCrossSection().getLongitudinalLength();
					double value = Double.parseDouble(proposedValue);
					if (value < 0)
						value += length;
					return (value >= 0) && (value <= length - longitudinalLength);
				} catch (NumberFormatException e) {
					return false;
				}
			}
		});
	}
	
	/**
	 * Retrieve the longitudinalLength of this VehicleDetector.
	 * @return Double; the longitudinalLength of this VehicleDetector
	 */
	public double getLongitudinalLength_r() {
		return longitudinalLength;
	}
	
	/**
	 * Change the longitudinalLength of this VehicleDetector.
	 * @param longitudinalLength Double; the new longitudinalLenght of this
	 * VehicleDetector
	 */
	public void setLongitudinalLength_w(double longitudinalLength) {
		this.longitudinalLength = longitudinalLength;
	}
	
	/**
	 * Validate a proposed longitudinalLength for this VehicleDetector.
	 * @return InputValidator for proposed values of the longitudinalLength
	 * of this VehicleDetector 
	 */
	public InputValidator validateLongitudinalLength_v() {
		double length = crossSectionElement.getCrossSection().getLongitudinalLength();
		double currentPosition = longitudinalPosition;
		if (currentPosition < 0)
			currentPosition += length;
		return new InputValidator("[.,0-9].*", minimumLongitudinalLength, length - currentPosition);
	}
	
	/**
	 * Retrieve the lateral position of this VehicleDetector.
	 * @return Double; the lateral position of this VehicleDetector in m from
	 * the center line of the parent {@link CrossSectionElement}
	 */
	public double getLateralPosition_r() {
		return lateralPosition;
	}
	
	/**
	 * Change the lateral position of this VehicleDetector.
	 * @param lateralPosition Double; the new lateral position in m from the
	 * center line of the parent (@link CrossSectionElement}
	 */
	public void setLateralPosition_w(double lateralPosition) {
		this.lateralPosition = lateralPosition;
	}
	
	/**
	 * Return an {@link InputValidator} for the lateral position of this
	 * VehicleDetector.
	 * @return {@link InputValidator} for the lateral position of this
	 * VehicleDetector
	 */
	public InputValidator validateLateralPosition_v() {
		double range = crossSectionElement.getWidth_r() - lateralWidth;
		return new InputValidator("[-.0-9].*", -range / 2, range / 2);
	}
	
	/**
	 * Retrieve the lateral width of this VehicleDetector.
	 * @return Double; the lateral width of this VehicleDetector in m
	 */
	public double getWidth_r() {
		return lateralWidth;
	}
	
	/**
	 * Change the lateral width of this VehicleDetector.
	 * @param width Double; the new lateral width of this VehicleDetector in m
	 */
	public void setWidth_w(double width) {
		lateralWidth = width;
	}
	
	/**
	 * Return an {@link InputValidator} for the lateral width of this
	 * VehicleDetector.
	 * @return {@link InputValidator} for the lateral width of this
	 * VehicleDetector
	 */
	public InputValidator validateWidth_v() {
		double limit = crossSectionElement.getWidth_r() - Math.abs(lateralPosition);
		return new InputValidator("[.0-9].*", 0.1, limit);
	}
	
	/**
	 * A VehicleDetector can always be deleted.
	 * <br /> This method is only used by the {@link nl.tudelft.otsim.GUI.ObjectInspector}.
	 * @return Boolean; always true
	 */
	@SuppressWarnings("static-method")
	public boolean mayDeleteVehicleDetector_d() {
		return true;
	}
	
	/**
	 * Delete this VehicleDetector.
	 */
	public void deleteVehicleDetector_d() {
		crossSectionElement.deleteCrossSectionObject(this);
	}
	
	@Override
	public String toString() {
		return String.format(Main.locale, "VehicleDetector %s at longitudinalPosition %.3fm, width %.3fm", ID, longitudinalPosition, lateralWidth);
	}
	
	@Override
	public void paint(GraphicsPanel graphicsPanel) {
		graphicsPanel.setStroke(0F);
		graphicsPanel.setColor(Color.BLUE);
		ArrayList<Vertex> polygon = getPolygon_r();
		//System.out.println("polygon is " + GeometryTools.verticesToString(polygon));
		if (polygon.size() > 0)
			graphicsPanel.drawPolygon(polygon.toArray());
	}

	/**
	 * Write this VehicleDetector to an XML file.
	 * @param staXWriter {@link StaXWriter}; writer for the XML file
	 * @return Boolean; true on success; false on failure
	 */
	@Override
	public boolean writeXML(StaXWriter staXWriter) {
		return staXWriter.writeNodeStart(XMLTAG)
				&& staXWriter.writeNode(XML_ID, ID)
				&& staXWriter.writeNode(XML_LATERALPOSITION, Double.toString(lateralPosition))
				&& staXWriter.writeNode(XML_WIDTH, Double.toString(lateralWidth))
				&& staXWriter.writeNode(XML_LENGTH, Double.toString(longitudinalLength))
				&& staXWriter.writeNode(XML_LONGITUDINALPOSITION, Double.toString(longitudinalPosition))
				&& staXWriter.writeNodeEnd(XMLTAG);
	}
}
