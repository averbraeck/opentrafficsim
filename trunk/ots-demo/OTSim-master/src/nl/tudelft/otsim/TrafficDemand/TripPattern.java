package nl.tudelft.otsim.TrafficDemand;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Locale;

import nl.tudelft.otsim.FileIO.ParsedNode;
import nl.tudelft.otsim.FileIO.StaXWriter;
import nl.tudelft.otsim.FileIO.XML_IO;
import nl.tudelft.otsim.Population.MovingPerson;

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
	/** Name for a TripPattern location element when stored in XML format */
	private static final String XML_LOCATION = "location";
	
	private TrafficDemand trafficDemand;
	private MovingPerson movingPerson ;
	private ArrayList<Object> locationList;
	private LinkedList<Long> departureTimeList;	
	private double numberOfTrips;
	private ArrayList<TripPatternPath> tripPatternPathList;	
	
	
	public TripPattern(TrafficDemand trafficDemand, double numberOfTrips, ArrayList<Object> locationList) {
		this.trafficDemand = trafficDemand;
		this.numberOfTrips = numberOfTrips;
		this.locationList = locationList;
	}
	
	public TripPattern(TrafficDemand trafficDemand, int numberOfTrips, ArrayList<Object> locationList,
			LinkedList<Long> departureTimeList) {
		this.trafficDemand = trafficDemand;
		this.numberOfTrips = numberOfTrips;
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
		numberOfTrips = Double.NaN;
		locationList = new ArrayList<Object>();
		departureTimeList = new LinkedList<Long>();
		tripPatternPathList = new ArrayList<TripPatternPath>();
		
		for (String fieldName : pn.getKeys()) {
			String value = pn.getSubNode(fieldName, 0).getValue();
			if (fieldName.equals(XML_NUMBEROFTRIPS))
				numberOfTrips = Double.parseDouble(value);
			else if (fieldName.equals(XML_LOCATION)) {
				// FIXME: XML format for the location list is bad
				for(String location : value.split(" "))
					locationList.add(location);
			} else
				throw new Exception("Unknown field in TrafficDemand: " + fieldName);
		}
		if (Double.isNaN(numberOfTrips))
			throw new Exception("TripPattern does not define NumberOfTrips " + pn.lineNumber + ", " + pn.columnNumber);
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

	public double getNumberOfTrips() {
		return numberOfTrips;
	}

	public void setNumberOfTrips(int numberOfTrips) {
		this.numberOfTrips = numberOfTrips;
	}


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

	public void setTripPatternPathList(ArrayList<TripPatternPath> tripPatternPathList) {
		this.tripPatternPathList = tripPatternPathList;
	}
	
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
				&& staXWriter.writeNode(XML_NUMBEROFTRIPS, String.format(Locale.US, "%f", numberOfTrips))
				&& staXWriter.writeNodeEnd(XMLTAG);
	}
	
}