package nl.tudelft.otsim.TrafficDemand;

import java.util.ArrayList;

import nl.tudelft.otsim.GeoObjects.Node;
import nl.tudelft.otsim.Utilities.TimeScaleFunction;

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
	//private double numberOfTrips;
	private TimeScaleFunction flowPattern = null;
	//private ArrayList<Node> nodeList;	
	private Node fromNode;
	private Node toNode;
	//private ArrayList<Path> pathList = new ArrayList<Path> ();
	//private ArrayList<ArrayList<Node>> coursePaths = new ArrayList<ArrayList<Node>> ();
	private ArrayList<ArrayList<Node>> detailedPaths = new ArrayList<ArrayList<Node>> ();
	private ArrayList<Double> costs = new ArrayList<Double> ();
	private ArrayList<Double> probabilities = null;

	public TripPatternPath(TripPattern tripPattern, TimeScaleFunction flowPattern, Node fromNode, Node toNode) {
		this.tripPattern = tripPattern;
		this.flowPattern = flowPattern;
		this.fromNode = fromNode;
		this.toNode = toNode;
	}
	public TripPatternPath(TripPattern tripPattern, double numberOfTrips, Node fromNode, Node toNode) {
		this.tripPattern = tripPattern;
		flowPattern = new TimeScaleFunction();
		flowPattern.insertPair(0, numberOfTrips);
		//this.numberOfTrips = numberOfTrips;
		this.fromNode = fromNode;
		this.toNode = toNode;
	}
	
	public TripPatternPath(TripPattern tripPattern, TimeScaleFunction flowPattern, ArrayList<Node> nodeList) {
		this.tripPattern = tripPattern;
		this.flowPattern = flowPattern;
		this.setNodeList(nodeList);
		if (nodeList.size() != 2)
			System.err.println("Hmm node list size is " + nodeList.size());
	}

	public TripPatternPath(TripPattern tripPattern, double numberOfTrips, ArrayList<Node> nodeList) {
		this.tripPattern = tripPattern;
		flowPattern = new TimeScaleFunction();
		flowPattern.insertPair(0, numberOfTrips);
		//this.numberOfTrips = numberOfTrips;
		this.setNodeList(nodeList);
		if (nodeList.size() != 2)
			System.err.println("Hmm node list size is " + nodeList.size());
	}

	public TripPattern getTripPattern() {
		return tripPattern;
	}

	//public void setTripPattern(TripPattern tripPattern) {
	//	this.tripPattern = tripPattern;
	//}

	//public double getNumberOfTrips() {
	//	return numberOfTrips;
	//}

	//public void setNumberOfTrips(double numberOfTrips) {
	//	this.numberOfTrips = numberOfTrips;
	//}

	public void setCoursePath (Node fromNode, Node toNode) {
		this.fromNode = fromNode;
		this.toNode = toNode;
	}
	
	public ArrayList<Node> getNodeList() {
		ArrayList<Node> result = new ArrayList<Node> (2);
		result.add(fromNode);
		result.add(toNode);
		return result;
		//return nodeList;
	}

	public void setNodeList(ArrayList<Node> nodeList) {
		if (nodeList.size() != 2)
			throw new Error("Oops! the node list contains " + nodeList.size() + " entries");
		fromNode = nodeList.get(0);
		toNode = nodeList.get(1);
		//this.nodeList = nodeList;
	}

	public void addDetailedPath(ArrayList<Node> path, double cost) {
		detailedPaths.add(path);
		costs.add(cost);
		probabilities = null;
	}
	
	public void clearDetailedPath() {
		detailedPaths.clear();
		probabilities = null;
	}

	public ArrayList<ArrayList<Node>> getDetailedPathList() {
		return detailedPaths;
	}

	/**
	 * Retrieve the {@link Node fromNode} of this TripPatternPath.
	 * @return {@link Node}; the fromNode of this TripPatternPath
	 */
	public Node getFromNode() {
		return fromNode;
	}

	/**
	 * Retrieve the {@link Node toNode} of this TripPatternPath.
	 * @return {@link Node}; the toNode of this TripPatternPath
	 */
	public Node getToNode() {
		return toNode;
	}

	private void computeRouteProbabilities() {
		if (costs.size() < 1)
			throw new Error("No routes");
		CostsToProbabilities costsToProbabilities = new LogitModel(-0.1);
		probabilities = costsToProbabilities.probabilities(costs);
	}

	public double getProbability(int index) {
		if (null == probabilities)
			computeRouteProbabilities();
		return probabilities.get(index);
	}

}