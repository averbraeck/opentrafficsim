package nl.tudelft.otsim.ShortesPathAlgorithms;

import java.util.ArrayList;

import nl.tudelft.otsim.GeoObjects.Link;
import nl.tudelft.otsim.GeoObjects.Network;
import nl.tudelft.otsim.GeoObjects.Node;

/**
 * General shape of all Shortest path algorithm implementations
 * 
 * The methods in this class are supposed to be used like this:
 * <br />1: call ShortestPathAlgorithm (network) to create an instance of this class.
 * <br />2: call execute (startNode, endNode) to set the start and end nodes.
 * <br />3: call hasNext to verify that a path exist (if none exists go to step 6, or go to step 2)
 * <br />4: call getCost, getPathLinks and getPathNodes to obtain the properties of the path
 * <br />5: call hasNext to check if another path exists (if none exists go to step 6, or go to step 2; otherwise go to step 4)
 * <br />6: dispose this instance of ShortedPathAlgorithm
 * 
 * @author Peter Knoppers
 */
public abstract class ShortestPathAlgorithm {
	@SuppressWarnings("unused")
	private final Network network;	// will be used in the implementation of execute
	
	/**
	 * Create an instance of the ShortestPathAlgorithm
	 * @param network {@link Network}; the Network in which paths must be found
	 */
	public ShortestPathAlgorithm (Network network) {
		this.network = network;
	}
	
	/**
	 * Find lowest cost paths between two {@link Node Nodes}.
	 * @param startNode {@link Node}; starting point of all paths
	 * @param endNode {@link Node}; end point of all paths
	 */
	public abstract void execute (Node startNode, Node endNode);
	
	/**
	 * Report whether there is a (further) path and (if there is a next path)
	 * make that the <i>current</i> path for calls to getCost and getPath.
	 * @return Boolean; true if there is a(nother) path; false if there is not
	 */
	public abstract boolean hasNext();
	
	/**
	 * Retrieve the cost of travel from the specified {@link Node startNode} to the endNode.
	 * @return Double; the cost of a trip from the specified startNode to the endNode
	 */
	public abstract double getCost ();
	
	/**
	 * Retrieve the lowest cost path from {@link Node startNode} to the endNode.
	 * @return ArrayList&lt;{@link Link}&gt;; the ordered list of links passed in the lowest cost path
	 */
	public abstract ArrayList<Link> getPathLinks ();
	
	/**
	 * Retrieve the lowest cost path from {@link Node startNode} to the endNode.
	 * @return ArrayList&lt;{@link Node}&gt;; the ordered list of nodes passed in the lowest cost path
	 */
	public abstract ArrayList<Node> getPathNodes ();
		
}
