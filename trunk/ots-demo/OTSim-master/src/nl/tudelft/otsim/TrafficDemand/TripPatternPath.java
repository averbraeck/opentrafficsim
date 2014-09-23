package nl.tudelft.otsim.TrafficDemand;

import java.util.ArrayList;

import nl.tudelft.otsim.GeoObjects.Node;
import nl.tudelft.otsim.ShortesPathAlgorithms.Path;

/**
 * This class provides details of a (part of a) trip
 * <br />
 * A <TrafficPattern> includes a set of TripPatternPaths. These TripPatternPaths describe a trip in detail:
 * going from A to B to C to ... with the number of trips, the destinations of this trip and the paths 
 * between these trip destinations 
 * @author Guus Tamminga
 */
public class TripPatternPath {
	
	private TripPattern tripPattern;
	private double numberOfTrips;
	private ArrayList<Node> nodeList;	
	private ArrayList<Path> pathList;

	public TripPatternPath(TripPattern tripPattern, double numberOfTrips) {
		this.tripPattern = tripPattern;
		this.numberOfTrips = numberOfTrips;
	}
	
	public TripPatternPath(TripPattern tripPattern, double numberOfTrips, ArrayList<Node> nodeList) {
		this.tripPattern = tripPattern;
		this.numberOfTrips = numberOfTrips;
		this.setNodeList(nodeList);
	}

	public TripPatternPath(TripPattern tripPattern, double numberOfTrips, ArrayList<Node> nodeList, ArrayList<Path> pathList) {
		this.tripPattern = tripPattern;
		this.numberOfTrips = numberOfTrips;
		this.setNodeList(nodeList);
		this.pathList = pathList;
	}
	
	public TripPattern getTripPattern() {
		return tripPattern;
	}

	public void setTripPattern(TripPattern tripPattern) {
		this.tripPattern = tripPattern;
	}

	public double getNumberOfTrips() {
		return numberOfTrips;
	}

	public void setNumberOfTrips(double numberOfTrips) {
		this.numberOfTrips = numberOfTrips;
	}


	public ArrayList<Path> getPathList() {
		return pathList;
	}

	public void setPathList(ArrayList<Path> pathList) {
		this.pathList = pathList;
	}

	public ArrayList<Node> getNodeList() {
		return nodeList;
	}

	public void setNodeList(ArrayList<Node> nodeList) {
		this.nodeList = nodeList;
	}
	
}