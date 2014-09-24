package nl.tudelft.otsim.GeoObjects;

import java.util.HashMap;
import java.util.Map;
import nl.tudelft.otsim.FileIO.ParsedNode;
import nl.tudelft.otsim.FileIO.StaXWriter;
import nl.tudelft.otsim.FileIO.XML_IO;
import nl.tudelft.otsim.GUI.InputValidator;
import nl.tudelft.otsim.GUI.WED;

/**
 * This class connects a set of TrafficDetectors and a set of TrafficLights to 
 * a traffic control program.
 * 
 * @author Peter Knoppers
 */
public class TrafficLightController implements XML_IO {
	/** Tag for a TrafficLightController in an XML file */
	public static String XMLTAG = "TrafficLightController";
	private static String XML_NAME = "ID";
	private static String XML_TRAFFICLIGHT = "Light";
	private static String XML_TRAFFICDETECTOR = "Detector";
	private static String XML_CONTROL_URL = "ControlURL";
	
	private Map<String, TrafficLight> trafficLights = new HashMap<String, TrafficLight> ();
	private Map<String, VehicleDetector> vehicleDetectors = new HashMap<String, VehicleDetector> ();
	private String name = null;
	String controlProgramURL = null;
	final Network network;
	
	/**
	 * Create a new TrafficLightController and set its name.
	 * @param network {@link Network}; the Network that will own the new
	 * TrafficLightController
	 * @param name String; the name of the new TrafficLightController
	 */
	public TrafficLightController (Network network, String name) {
		this.network = network;
		this.name = name;
	}
	/**
	 * Create a TrafficLightController from a parsed XML file.
	 * @param network Network that owns this TrafficLightController
	 * @param pn {@link ParsedNode}; the root of the TrafficLightController in the parsed XML file
	 * @throws Exception
	 */
	public TrafficLightController (Network network, ParsedNode pn) throws Exception {
		this.network = network;
		for (String fieldName : pn.getKeys()) {
			String value = pn.getSubNode(fieldName, 0).getValue();
			if (fieldName.equals(XML_TRAFFICLIGHT)) {
				for (int index = 0; index < pn.size(XML_TRAFFICLIGHT); index++) {
					String trafficLightName = pn.getSubNode(XML_TRAFFICLIGHT, index).getValue();
					TrafficLight tl = network.lookupTrafficLight(trafficLightName);
					if (null == tl)
						WED.showProblem(WED.ENVIRONMENTERROR, "No traffic light with ID %s found", value);
					else
						tl.setTrafficLightController_w(this);
				}
			} else if (fieldName.equals(XML_TRAFFICDETECTOR)) {
				for (int index = 0; index < pn.size(XML_TRAFFICDETECTOR); index++) {
					String detectorName = pn.getSubNode(XML_TRAFFICDETECTOR, index).getValue();
					VehicleDetector vd = network.lookupVehicleDetector(detectorName);
					if (null == vd)
						WED.showProblem(WED.ENVIRONMENTERROR, "No vehicle detector with ID %s found", value);
					else
						vd.setTrafficLightController_w(this);
				}
			} else if (fieldName.equals(XML_CONTROL_URL))
				controlProgramURL = value;
			else if (fieldName.equals(XML_NAME))
				name = value;
			else
				throw new Exception("Unknown field in TrafficLightController: " + fieldName + " at " + pn.lineNumber + ", " + pn.columnNumber);
		}
		//if (null == controlProgramURL)
		//	throw new Exception("No " + XML_CONTROL_URL + " defined for TrafficLightController at " + pn.lineNumber + ", " + pn.columnNumber);
		if (null == name)
			throw new Exception("No " + XML_NAME + " defined for TrafficLightController at " + pn.lineNumber + ", " + pn.columnNumber);
	}
	
	private boolean writeDetectors (StaXWriter staXWriter) {
		for (String key : vehicleDetectors.keySet())
			if (! staXWriter.writeNode(XML_TRAFFICDETECTOR, key))
				return false;
		return true;
	}
	
	private boolean writeLights (StaXWriter staXWriter) {
		for (String key : trafficLights.keySet())
			if (! staXWriter.writeNode(XML_TRAFFICLIGHT, key))
				return false;
		return true;
	}
	
	@Override
	public boolean writeXML(StaXWriter staXWriter) {
		return staXWriter.writeNodeStart(XMLTAG)
				&& staXWriter.writeNode(XML_NAME, name)
				&& staXWriter.writeNode(XML_CONTROL_URL, controlProgramURL)
				&& writeDetectors(staXWriter)
				&& writeLights(staXWriter)
				&& staXWriter.writeNodeEnd(XMLTAG);
	}

	/**
	 * Retrieve the name of this TrafficLightController.
	 * @return String; the name of this TrafficLightController
	 */
	public String getName_r() {
		return name;
	}
	
	/** 
	 * Check that a proposed name is acceptable for this TrafficLightController.
	 * @return {@link InputValidator} to check a proposed name for this TrafficLightController
	 */
	public InputValidator validateName_v() {
		return new InputValidator(new InputValidator.CustomValidator() {
			@Override
			public boolean validate(String originalValue, String proposedValue) {
				if (! proposedValue.matches("[a-zA-Z_][-a-zA-Z0-9_.]*"))
					return false;	// not a decent name
				if (proposedValue.equals(originalValue))
					return true;	// current name is OK
				// Anything else must be unique within the list of TrafficLightControllers names
				return network.lookupTrafficLightController(proposedValue) == null;
			}
		});
	}
	
	/**
	 * Change the name of this TrafficLightController.
	 * @param newName String; the new name of this TrafficLightController
	 */
	public void setName_w(String newName) {
		name = newName;
	}
	
	/**
	 * Retrieve the URL that identifies the (external) control program for this
	 * TrafficLightController.
	 * @return String; the URL that identifies the (external) control program
	 * for this TrafficLightController
	 */
	public String getControlProgramURL_r() {
		return controlProgramURL;
	}
	
	/**
	 * Set/change the URL that identifies the (external) control program for
	 * this TrafficLightController.
	 * @param newURL String; the new URL for the (external) control program
	 * for this TrafficLightController
	 */
	public void setControlProgramURL_w(String newURL) {
		controlProgramURL = newURL;
	}
	
	/**
	 * Remove a {@link TrafficLight} from the list of traffic lights of this
	 * TrafficLightController.
	 * @param trafficLight {@link TrafficLight}; the traffic light that must
	 * be removed
	 */
	public void deleteLight(TrafficLight trafficLight) {
		trafficLights.remove(trafficLight);
	}

	/**
	 * Add a {@link TrafficLight} to the list of traffic lights of this
	 * TrafficLightController.
	 * <br />The names of all traffic lights associated with a particular
	 * TrafficLightController must be unique.
	 * @param trafficLight {@link TrafficLight} the traffic light that must
	 * be added
	 */
	public void addLight(TrafficLight trafficLight) {
		trafficLights.put(trafficLight.getID_r(), trafficLight);		
	}
	
	@Override
	public String toString() {
		return "Traffic light controller " + name;
	}
	
	/**
	 * Check and fix association between {@link TrafficLight TrafficLights}
	 * and this TrafficLightController.
	 * <br /> This ugly code is required to detect and fix situations where
	 * several TrafficLightControllers own a particular TrafficLight. There
	 * should be a cleaner way to accomplish this consistency...
	 */
	public void fixClients() {
		for (boolean reCheck = true; reCheck; ) {
			reCheck = false;
			TrafficLight deleteLight = null;
			TrafficLight addLight = null;
			for (TrafficLight tl : trafficLights.values()) {
				Object object = tl.getTrafficLightController_r();
				if (object instanceof TrafficLightController) {
					if (! object.equals(this))
						deleteLight = (TrafficLight) object;
				} else
					addLight = (TrafficLight) object;
			}
			if (null != deleteLight) {
				System.err.println("Removing traffic light " + deleteLight.getID_r() + " from traffic light controller " + name);
				deleteLight(deleteLight);
				reCheck = true;
			}
			if (null != addLight) {
				System.err.println("Adding traffic light " + deleteLight.getID_r() + " to traffic light controller " + name);
				addLight.setTrafficLightController_w(this);
				reCheck = true;
			}
		}
	}

	/**
	 * Retrieve a {@link TrafficLight} of this TrafficLightController by
	 * specifying the name.
	 * @param trafficLightName String; name of the sought for {@link TrafficLight}
	 * @return {@link TrafficLight}; the matching TrafficLight, or null if no
	 * TrafficLight of this TrafficLightController matches the specified name.
	 */
	public TrafficLight lookupTrafficLight(String trafficLightName) {
		return trafficLights.get(trafficLightName);
	}
	
	/**
	 * Retrieve a {@link VehicleDetector} of this TrafficLightController by
	 * specifying the name.
	 * @param vehicleDetectorName String; name of the sought for
	 * {@link VehicleDetector}
	 * @return {@link VehicleDetector}; the matching VehicleDetector, or null
	 * if no VehicleDetector of this TrafficLightController matches the
	 * specified name
	 */
	public VehicleDetector lookupDetector(String vehicleDetectorName) {
		return vehicleDetectors.get(vehicleDetectorName);
	}
	
	/**
	 * Remove a {@link VehicleDetector} from the list of vehicle detectors of 
	 * this TrafficLightController.
	 * @param vehicleDetector {@link VehicleDetector}; the vehicle detector 
	 * that must be removed
	 */	
	public void deleteDetector(VehicleDetector vehicleDetector) {
		vehicleDetectors.remove(vehicleDetector);
	}
	
	/**
	 * Add a {@link VehicleDetector} to the list of vehicle detectors of this
	 * TrafficLightController.
	 * <br />The names of all vehicle detectors associated with a particular
	 * TrafficLightController must be unique.
	 * @param vehicleDetector {@link VehicleDetector} the vehicle detector that 
	 * must be added
	 */
	public void addDetector(VehicleDetector vehicleDetector) {
		vehicleDetectors.put(vehicleDetector.getID_r(), vehicleDetector);		
	}
	
	/**
	 * Generate a comma-separated list of the names of the 
	 * {@link VehicleDetector VehicleDetectors} associated with this
	 * TrafficLichtController.
	 * @return String; comma-separated list of names of {@link VehicleDetector VehicleDetectors}
	 */
	public String getDetectors() {
		String result = "";
		for (String detectorName : vehicleDetectors.keySet()) {
			if (result.length() > 0)
				result += ",";
			result += detectorName;
		}
		return result;
	}
	
	/**
	 * Generate a comma-separated list of the names of the
	 * {@link TrafficLight TrafficLights} associated with this
	 * TrafficLightController.
	 * @return String; comma-separated list of names of {@link TrafficLight TrafficLights}
	 */
	public String getLights() {
		String result = "";
		for (String trafficLightName : trafficLights.keySet()) {
			if (result.length() > 0)
				result += ",";
			result += trafficLightName;
		}
		return result;
	}

}