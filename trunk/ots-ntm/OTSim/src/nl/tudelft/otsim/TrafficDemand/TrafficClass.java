package nl.tudelft.otsim.TrafficDemand;

import java.util.Locale;

import nl.tudelft.otsim.FileIO.ParsedNode;
import nl.tudelft.otsim.FileIO.StaXWriter;
import nl.tudelft.otsim.FileIO.XML_IO;

/**
 * This class holds all information about a class of traffic.
 * A traffic class could be a passenger car with driver, a truck with driver, a pedestrian, etc.
 * 
 * @author Peter Knoppers
 */
public class TrafficClass implements XML_IO {
	/** Name for a TrafficClass element when stored in XML format */
	static public String XMLTAG = "trafficClass";
	final static private String XML_NAME = "name";
	final static private String XML_LENGTH = "length";
	final static private String XML_ACTIVATIONLEVEL = "activationLevel";
	final static private String XML_MAXIMUMSPEED = "maximumSpeed";
	final static private String XML_DEFAULTFRACTION = "defaultFraction";
	final static private String XML_TRANSITIONTIME = "transitionTime";
	
    private String name = null;
    private double defaultFraction = Double.NaN;
    private double length = Double.NaN;
    private double maxSpeed = Double.NaN;
    private double activationLevel = Double.NaN;
    private double transitionTime = Double.NaN;
	private double maxDeceleration = -6;	// STUB
    
    /**
     * Create a new TrafficClass.
     * @param pn {@link ParsedNode}; XML node that describes the new TrafficClass
     * @throws Exception
     */
    public TrafficClass (ParsedNode pn) throws Exception {
		for (String fieldName : pn.getKeys()) {
			String value = pn.getSubNode(fieldName, 0).getValue();
			if (fieldName.equals(XML_NAME))
				name = value;
			else if (fieldName.equals(XML_ACTIVATIONLEVEL))
				activationLevel = Double.parseDouble(value);
			else if (fieldName.equals(XML_LENGTH))
				length = Double.parseDouble(value);
			else if (fieldName.equals(XML_MAXIMUMSPEED))
				maxSpeed = Double.parseDouble(value);
			else if (fieldName.equals(XML_DEFAULTFRACTION))
				defaultFraction = Double.parseDouble(value);
			else if (fieldName.equals(XML_TRANSITIONTIME))
				transitionTime = Double.parseDouble(value);
			else
				throw new Exception("Unknown field \"" + fieldName + "\" near " + pn.description());
		}
		if (Double.isNaN(maxSpeed))
			throw new Exception("" + XML_MAXIMUMSPEED + " not defined near " + pn.description());
		if ((defaultFraction < 0) || (defaultFraction > 1.0))
			throw new Exception("Bad " + XML_DEFAULTFRACTION + " value near " + pn.description());
		if ((activationLevel < 0) || (activationLevel > 1.0))
			throw new Exception("Bad " + XML_ACTIVATIONLEVEL + " value near " + pn.description());
		if ((transitionTime < 0))
			throw new Exception("Bad " + XML_TRANSITIONTIME + " value near " + pn.description());

    }

    /**
     * Create a new TrafficClass.
     * @param name Name of the new TrafficClass
     * @param fraction Double; default fraction of the new TrafficClass
     * @param length Double; length of the objects in the new TrafficClass
     * @param maxSpeed Double; maximum speed of the objects in the new TrafficClass
     * @param activationLevel Double; activationLevel of the objects in the new TrafficClass
     */
    public TrafficClass(String name, double fraction, double length, double maxSpeed, double activationLevel, double transitionTime) {
    	this.name = name;
    	this.defaultFraction = fraction;
    	this.length = length;
    	this.maxSpeed = maxSpeed;
    	this.activationLevel = activationLevel;
    	this.transitionTime = transitionTime;
	}

	/**
     * Retrieve the name of this TrafficClass.
     * @return String; the name of this TrafficClass
     */
	public String getName() {
		return name;
	}

	/**
	 * Retrieve the fraction of generated objects that belong to this TrafficClass by default.
	 * @return Double; fraction of generated objects that belong to this TrafficClass by default
	 */
	public double getDefaultFraction() {
		return defaultFraction;
	}

	@Override
	public boolean writeXML(StaXWriter staXWriter) {
		return staXWriter.writeNodeStart(XMLTAG)
				&& staXWriter.writeNode(XML_NAME, name)
				&& staXWriter.writeNode(XML_ACTIVATIONLEVEL, String.format(Locale.US, "%.5f", activationLevel))
				&& staXWriter.writeNode(XML_DEFAULTFRACTION, String.format(Locale.US, "%.5f", defaultFraction))
				&& staXWriter.writeNode(XML_LENGTH, String.format(Locale.US, "%.3f", length))
				&& staXWriter.writeNode(XML_MAXIMUMSPEED, String.format(Locale.US, "%.0f", maxSpeed))
				&& staXWriter.writeNode(XML_TRANSITIONTIME, String.format(Locale.US, "%.0f", transitionTime))
				&& staXWriter.writeNodeEnd(XMLTAG);
	}

	/**
	 * Retrieve the length of objects in this TrafficClass.
	 * @return Double; the length of objects in this TrafficClass
	 */
	public double getLength() {
		return length;
	}

	/**
	 * Retrieve the maximum speed of this TrafficClass
	 * @return Double; the maximum speed of this TrafficClass in km/h
	 */
	public double getMaximumSpeed() {
		return maxSpeed;
	}

	/**
	 * Retrieve the maximum deceleration of this TrafficClass.
	 * @return Double; the maximum deceleration in m/s/s
	 */
	public double getMaximumDeceleration() {
		return maxDeceleration;
	}

	/**
	 * Retrieve the initial activation level of this TrafficClass.
	 * @return Double; the initial activation level of this TrafficClass
	 */
	public double getActivationLevel() {
		return activationLevel;
	}
	
	/**
	 * Retrieve the targeted transition time of this TrafficClass.
	 * @return Double; the targeted transition time of this TrafficClass
	 */
	public double getTransitionTime() {
		return transitionTime;
	}
}
