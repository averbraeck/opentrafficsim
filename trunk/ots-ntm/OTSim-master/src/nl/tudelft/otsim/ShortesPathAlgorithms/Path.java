package nl.tudelft.otsim.ShortesPathAlgorithms;

import java.util.ArrayList;

import nl.tudelft.otsim.GeoObjects.Node;
import nl.tudelft.otsim.TrafficDemand.TripPatternPath;

/**
 * This Class describes the path details (series of nodes/links).
 *  <br />
 * The path is computed in the package shortest paths and attached
 * to TripPatterns.
 * 
 * @author gtamminga
 */
public class Path {
	private ArrayList<Node> nodeList;
	private TripPatternPath tripPatternPath;
	
	/**
	 * Create a new Path with {@link TripPatternPath} and {@link Node} list.
	 * @param tripPatternPath {@link TripPatternPath} trip pattern of the new Path
	 * @param nodeList ArrayList&lt;{@link Node}&gt; list of nodes of the new Path
	 */
	public Path(TripPatternPath tripPatternPath, ArrayList<Node> nodeList) {
		this.nodeList = nodeList;
		this.tripPatternPath = tripPatternPath;
	}

	/**
	 * Create an empty Path.
	 */
	public Path() {
	}

	/**
	 * Retrieve the list of {@link Node Nodes} of this Path.
	 * @return ArrayList&lt;{@link Node}&gt;; the Nodes of this Path
	 */
	public ArrayList<Node> getNodeList() {
		return nodeList;
	}
	
	/**
	 * Set/replace the list of {@link Node Nodes} of this Path.
	 * @param nodeList ArrayList&lt;{@link Node}&gt;; the new list of Nodes of
	 * this Path
	 */
	public void setNodeList(ArrayList<Node> nodeList) {
		this.nodeList = nodeList;
	}

	/**
	 * Retrieve the {@link TripPatternPath} of this Path.
	 * @return ArrayList&lt;{@link TripPatternPath}&gt;; the tripPatternPath of
	 * this Path
	 */
	public TripPatternPath getTripPatternPath() {
		return tripPatternPath;
	}

	/**
	 * Set/replace the {@link TripPatternPath} of this Path.
	 * @param tripPatternPath {@link TripPatternPath}; the new TripPatternPath
	 * of this Path
	 */
	public void setTripPatternPath(TripPatternPath tripPatternPath) {
		this.tripPatternPath = tripPatternPath;
	}
	
}