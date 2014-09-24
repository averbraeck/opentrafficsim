package nl.tudelft.otsim.TrafficDemand;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Locale;

import nl.tudelft.otsim.FileIO.ParsedNode;
import nl.tudelft.otsim.FileIO.StaXWriter;
import nl.tudelft.otsim.FileIO.XML_IO;
import nl.tudelft.otsim.Population.MovingPerson;
import nl.tudelft.otsim.Utilities.Sorter;
import nl.tudelft.otsim.Utilities.TimeScaleFunction;

/**
 * The Trip Pattern is the final list of characteristics of the population and its activities 
 * It contains information about every trip chain that is relevant for movements between activities
 * This version concentrates on vehicle simulation
 * @author G.Tamminga
 */
public class TripPattern implements XML_IO {
	/** Name for a TripPattern element when stored in XML format */
	public static final String XMLTAG = "tripPattern";
	
	/** Name for a TripPattern personID element when stored in XML format */
	private static final String XML_PERSONID = "personID";
	/** Name for a TripPattern activityID element when stored in XML format */
	private static final String XML_ACTIVITYID = "activityID";
	/** Name for a TripPattern trips element when stored in XML format */
	private static final String XML_NUMBEROFTRIPS = "trips";
	private static final String XML_FLOWGRAPH = "flowGraph";
	/** Name for a TripPattern location element when stored in XML format */
	private static final String XML_LOCATION = "location";
	
	private TrafficDemand trafficDemand;
	private HashMap<TrafficClass, Double> classDemand = new HashMap<TrafficClass, Double>();
	private MovingPerson movingPerson ;
	private ArrayList<Object> locationList;
	private LinkedList<Long> departureTimeList;
	protected TimeScaleFunction flowGraph = null;
	private ArrayList<TripPatternPath> tripPatternPathList;	
	
	public TripPattern(TrafficDemand trafficDemand, double numberOfTrips, ArrayList<Object> locationList) {
		this.trafficDemand = trafficDemand;
		flowGraph = new TimeScaleFunction();
		flowGraph.insertPair(0, numberOfTrips);
		this.locationList = locationList;
	}
	
	public TripPattern(TrafficDemand trafficDemand, int numberOfTrips, ArrayList<Object> locationList,
			LinkedList<Long> departureTimeList) {
		this.trafficDemand = trafficDemand;
		flowGraph = new TimeScaleFunction();
		flowGraph.insertPair(0, numberOfTrips);
		this.locationList = locationList;
		this.departureTimeList = departureTimeList;

	}
	
	public TripPattern(TrafficDemand trafficDemand, MovingPerson movingPerson, ArrayList<Object> locationList,
			LinkedList<Long> departureTimeList) {
		this.trafficDemand = trafficDemand;
		this.movingPerson = movingPerson;
		this.locationList = locationList;
		this.departureTimeList = departureTimeList;
	}

	public TripPattern(TrafficDemand trafficDemand, ParsedNode pn) throws Exception {
		this.trafficDemand = trafficDemand;
		movingPerson = null;
		locationList = new ArrayList<Object>();
		departureTimeList = new LinkedList<Long>();
		tripPatternPathList = new ArrayList<TripPatternPath>();
		double sumClassDemand = 0d;
		
		for (String fieldName : pn.getKeys()) {
			String value = pn.getSubNode(fieldName, 0).getValue();
			if (fieldName.equals(XML_NUMBEROFTRIPS)) {
				for (int i = pn.size(XML_NUMBEROFTRIPS); --i >= 0; ) {
					value = pn.getSubNode(fieldName,  i).getValue();
					sumClassDemand += Double.parseDouble(value);
					String trafficClassName = pn.getSubNode(XML_NUMBEROFTRIPS, i).getAttributeValue(TrafficClass.XMLTAG);
					if (null != trafficClassName) {
						TrafficClass tc = trafficDemand.lookupTrafficClass(trafficClassName);
						if (null == tc)
							throw new Exception("TrafficDemand refers to undefined traffic class \"" + trafficClassName + "\" near " + pn.description());
						classDemand.put(tc, Double.parseDouble(value));
					}
				}
			} else if (fieldName.equals(XML_FLOWGRAPH)) {
				flowGraph = new TimeScaleFunction(pn.getSubNode(XML_FLOWGRAPH, 0));
			} else if (fieldName.equals(XML_LOCATION)) {
				// FIXME: XML format for the location list is bad
				for(String location : value.split(" "))
					locationList.add(location);
			} else
				throw new Exception("Unknown field in TrafficDemand: \"" + fieldName + "\" near " + pn.description());
		}
		if (null == flowGraph) {
			flowGraph = new TimeScaleFunction();
			flowGraph.insertPair(0, sumClassDemand);
		}
	}

	public TrafficDemand getTrafficDemand() {
		return trafficDemand;
	}
	
	public MovingPerson getMovingPerson() {
		return movingPerson;
	}

	public void setMovingPerson(MovingPerson movingPerson) {
		this.movingPerson = movingPerson;
	}

	/**
	 * Retrieve the total flow for this TripPattern.
	 * @return Double; the total flow for this TripPattern
	 */
	//public double getNumberOfTrips() {
	//	return numberOfTrips;
	//}
	
	/*   DEMOTED JAVADOC (removed the double asterisk)
	 * Return the flow for a specified TrafficClass.
	 * @param tc {@link TrafficClass}; the TrafficClass to return the flow for
	 * @return Double; the flow for the specified {@link TrafficClass}
	 */
	
	/*public double getNumberOfTrips(TrafficClass tc) {
		if (classDemand.size() == 0)
			return tc.getDefaultFraction() * getNumberOfTrips();
		if (null == classDemand.get(tc))
			return 0;
		return classDemand.get(tc) * getNumberOfTrips();
	}*/

	public ArrayList<Object> getLocationList() {
		return locationList;
	}

	public void setLocationList(ArrayList<Object> locationList) {
		this.locationList = locationList;
	}

	public LinkedList<Long> getDepartureTimeList() {
		return departureTimeList;
	}
	
	public void setDepartureTimeList(LinkedList<Long> departureTimeList) {
		this.departureTimeList = departureTimeList;
	}

	public ArrayList<TripPatternPath> getTripPatternPathList() {
		return tripPatternPathList;
	}

	public void clearTripPatternPaths() {
		tripPatternPathList.clear();
	}
	
	//public void setTripPatternPathList(ArrayList<TripPatternPath> tripPatternPathList) {
	//	this.tripPatternPathList = tripPatternPathList;
	//}
	
	private boolean writeMovingPerson(StaXWriter staXWriter) {
		MovingPerson mp = getMovingPerson();
		if (null != mp)
			if (! mp.writeXML(staXWriter))
				return false;
		return true;
	}
	
	private boolean writeActivityLocationIDList(StaXWriter staXWriter) {
		// Concatenate the locations with one space as separator
		String locations = null;
		for (Object location : locationList)
			if (null == locations)
				locations = location.toString();
			else
				locations += " " + location;
		return staXWriter.writeNode(XML_LOCATION, locations);
	}

	@Override
	public boolean writeXML(StaXWriter staXWriter) {
		return staXWriter.writeNodeStart(XMLTAG)
				&& writeMovingPerson(staXWriter)
				&& writeActivityLocationIDList(staXWriter)
				&& writeTrips(staXWriter)
				&& staXWriter.writeNodeEnd(XMLTAG);
	}

	private boolean writeTrips(StaXWriter staXWriter) {
		if (classDemand.size() == 0) {
			if (flowGraph.isTrivial())
				return staXWriter.writeNode(XML_NUMBEROFTRIPS, String.format(Locale.US, "%f", flowGraph.getFactor(0)));
			return flowGraph.writeXML(staXWriter);
		}
		for (String className : Sorter.asSortedList(trafficDemand.trafficClassNames())) {
			TrafficClass tc = trafficDemand.lookupTrafficClass(className);
			Double flow = classDemand.get(tc);
			if (null != flow) {
				HashMap<String, String> attributes = new HashMap<String, String>();
				attributes.put(TrafficClass.XMLTAG, className);
				if (! staXWriter.writeNode(XML_NUMBEROFTRIPS, attributes, String.format(Locale.US, "%.6f", flow)))
					return false;
			}
		}
		return true;
	}

	/**
	 * Add one {@link TripPatternPath} to this TripPattern.
	 * @param tripPatternPath {@link TripPatternPath}; the TripPatternPath that must be added to this TripPattern
	 */
	public void addTripPatternPath(TripPatternPath tripPatternPath) {
		tripPatternPathList.add(tripPatternPath);
	}

	/**
	 * Return the distribution over the various {@link TrafficClass TrafficClasses} of this TripPattern.
	 * @return String; a text representation of the {@link TrafficClass} frequencies
	 */
	public String getClasslFlows() {
		String result = "";
		if (0 == classDemand.size()) {
			for (String name : Sorter.asSortedList(trafficDemand.trafficClassNames()))
				result += String.format(Locale.US, "\t%s:%.6f", name, trafficDemand.lookupTrafficClass(name).getDefaultFraction());
			return result;
		}
		// FIXME: This code is very inefficient...
		ArrayList<String> names = new ArrayList<String>();
		double totalDemand = 0;
		for (TrafficClass tc : classDemand.keySet()) {
			names.add(tc.getName());
			totalDemand += classDemand.get(tc);
		}
		for (String name : Sorter.asSortedList(names)) {
			TrafficClass trafficClass = null;
			for (TrafficClass tc : classDemand.keySet())
				if (tc.getName().equals(name))
					trafficClass = tc;
			if (null == trafficClass)
				throw new Error("Cannot happen");
			result += String.format(Locale.US, "\t%s:%.6f", trafficClass.getName(), classDemand.get(trafficClass) / totalDemand);
		}
		return result;
	}
	
}