package nl.tudelft.otsim.ShortesPathAlgorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.tudelft.otsim.GeoObjects.CrossSectionElement;
import nl.tudelft.otsim.GeoObjects.Link;
import nl.tudelft.otsim.GeoObjects.Network;
import nl.tudelft.otsim.GeoObjects.Node;

/**
 *
 * @author not gtamminga
 */
/**
 * from the Internet (devogella)
 * http://www.vogella.com/articles/JavaAlgorithmsDijkstra/article.html#dijkstra
 * @author gtamminga
 */
public class DijkstraAlgorithm extends ShortestPathAlgorithm {
	//private final List<Node> nodes;
	private final Collection<Link> edges;
	private Set<Node> settledNodes;
	private Set<Node> unSettledNodes;
	private Map<Node, Node> predecessors;
	private Map<Node, Double> cost;
	private final int totalNodes;
	private final double maximumTotalCostRatio;
	private double shortestPathCosts = Double.NaN;
	private Node startNode;
	private Node endNode;
	private ArrayList<ArrayList<Node>> returnedPaths = new ArrayList<ArrayList<Node>>(); 
	final int superNodeID = 999999999;
	
	HashMap<Link,Double> penalties = new HashMap<Link,Double>();

	/**
	 * Prepare to run the Dijkstra algorithm.
	 * @param network {@link Network}; the Network to run Dijkstra on
	 * @param maximumTotalCostRatio maximum ratio of cost of most expensive path considered in hasNext and the lowest cost path 
	 */
	public DijkstraAlgorithm(Network network, double maximumTotalCostRatio) {
		super (network);
		totalNodes = network.getAllVisitableNodes(true, true).size();
		edges = network.getAllLinks();
		this.maximumTotalCostRatio = maximumTotalCostRatio;
	}
	
	/**
	 * Run the Dijkstra algorithm to compute routes and distances to a specific {@link Node}.
	 * @param newStartNode {@link Node}; the Node to compute the routes and distances for 
	 * @param newEndNode {@link Node}; the Node to compute the route to
	 */
	@Override
	public void execute(Node newStartNode, Node newEndNode) {
		this.endNode = newEndNode;
		penalties.clear();
		returnedPaths.clear();
		if (this.startNode == newStartNode)
			return;
		this.startNode = newStartNode;
	}
	
	private ArrayList<Node> findPath() {
		settledNodes = new HashSet<Node>();
		unSettledNodes = new HashSet<Node>();
		cost = new HashMap<Node, Double>();
		predecessors = new HashMap<Node, Node>();
		cost.put(startNode, 0.0);
		unSettledNodes.add(startNode);
		while (unSettledNodes.size() > 0) {
			Node node = getMinimum(unSettledNodes);
			settledNodes.add(node);
			unSettledNodes.remove(node);
			List<Node> adjacentNodes = getNeighbors(node);
			for (Node target : adjacentNodes) {
				if (getLowestCost(target) > getLowestCost(node) + getCost(node, target)) {
					cost.put(target, getLowestCost(node) + getCost(node, target));
					predecessors.put(target, node);
					unSettledNodes.add(target);
				}
			}
		}
		if (totalNodes != settledNodes.size())
			System.out.println(String.format("Dijkstra: Disjunct network: total nodes; %s, settled nodes: %d, start node %s, unsetteldNodes contains %s", totalNodes, settledNodes.size(), startNode.toString(), unSettledNodes.toString()));
		if (null == cost.get(endNode))
			return null;
		ArrayList<Node> pathNodes = new ArrayList<Node>();
		Node step = endNode;
		pathNodes.add(step);
		while (predecessors.get(step) != null) {
			step = predecessors.get(step);
			pathNodes.add(step);
		}
		// Put it into the correct order
		Collections.reverse(pathNodes);		
		return pathNodes;
	}

	@Override
	public double getCost () {
		double totalCost = 0;
		ArrayList<Link> links = getPathLinks();
		for (Link link : links)
			if (superNodeID!=link.getToNodeExpand().getNodeID()) //NGI Simulation: super-node ID = superNodeID (0-cost hyper-links)
				totalCost += computeCost(link, true);
		return totalCost;
	}

	private double computeCost(Link link, boolean ignorePenalties) {
		// Currently the cost is the driving time (in seconds) of a link
		//double result = link.getLength() / (link.getMaxSpeed_r() / 3.6);
		double linkLength = link.getLength();
		double linkSpeed = link.getMaxSpeed_r() / 3.6;
		// Look for a speed limit in the first CrossSection
		for (CrossSectionElement cse : link.getCrossSections_r().get(0).getCrossSectionElementList_r()) {
			String speedLimit = cse.getSpeedLimit_r();
			if (null != speedLimit)
				linkSpeed = Double.parseDouble(speedLimit) / 3.6;
		}
		double result = linkLength / linkSpeed;
		if (ignorePenalties)
			return result;
		Double penalty = penalties.get(link);
		if (null != penalty)
			result *= (1 + penalty);
		return result;
	}
	
	private double getCost(Node node, Node target) {
		if (superNodeID==target.getNodeID()) //NGI Simulation: super-node ID = superNodeID (0-cost hyper-links)
			return 0;
		for (Link link : edges)
			if (link.getFromNodeExpand().equals(node) && link.getToNodeExpand().equals(target))
				return computeCost(link, false);
		throw new RuntimeException("Should not happen");
	}

	private List<Node> getNeighbors(Node node) {
		List<Node> neighbors = new ArrayList<Node>();
		// FIXME this is bloody expensive; there is however no easy way to find the links connected to an autogenerated node...
		for (Link link : edges)
			if (link.getFromNodeExpand().equals(node) && !isSettled(link.getToNodeExpand()))
				neighbors.add(link.getToNodeExpand());
		return neighbors;
	}

	private Node getMinimum(Set<Node> vertexes) {
		Node minimum = null;
		for (Node node : vertexes)
			if (minimum == null)
				minimum = node;
			else if (getLowestCost(node) < getLowestCost(minimum))
				minimum = node;
		return minimum;
	}

	private boolean isSettled(Node nodes) {
		return settledNodes.contains(nodes);
	}

	private double getLowestCost(Node nodeB) {
		Double d = cost.get(nodeB);
		if (d == null)
			return Double.MAX_VALUE;
		return d;
	}

	/**
	 * Create a path (a set of {@link Link Links} connecting a set of {@link Node Nodes}.
	 * <br /> Remark: assumes that there is no more than one link between any pair of nodes. 
	 * @return ArrayList&lt;{@link Link}&gt;; the list of Links that form the path
	 */
	@Override
	public ArrayList<Link> getPathLinks () {
		return nodeListToLinkList(getPathNodes());
    }
	
	private ArrayList<Link> nodeListToLinkList (ArrayList<Node> path) {
    	ArrayList<Link> pathLinks = new ArrayList<Link>();
        Node previousNode = null;
        for (Node node : path) {
            if (null != previousNode)
                for (Link link : edges)
                    if (link.getFromNodeExpand().equals(previousNode) && link.getToNodeExpand().equals(node))
                    	pathLinks.add(link);
            previousNode = node;
        }
        Collections.reverse(pathLinks);
        return pathLinks;		
	}
	
	/**
	 * This method returns the path from the source to the selected target
	 * @return ArrayList&lt;{@link Node}&gt;; the set of Nodes that form the
	 * source to the target 
	 */
	@Override
	public ArrayList<Node> getPathNodes() {
		// Check if a path exists
		if (null == predecessors.get(endNode))
			throw new Error("No (additional) path exists");
		ArrayList<Node> pathNodes = new ArrayList<Node>();
		Node step = endNode;
		pathNodes.add(step);
		while (predecessors.get(step) != null) {
			step = predecessors.get(step);
			pathNodes.add(step);
		}
		// Put it into the correct order
		Collections.reverse(pathNodes);		
		return pathNodes;
	}

	private static boolean comparePaths(ArrayList<Node> path1, ArrayList<Node> path2) {
		if (path1.size() != path2.size())
			return false;
		for (int i = 0; i < path1.size(); i++)
			if (path1.get(i) != path2.get(i))
				return false;
		return true;
	}
	
	private final double penaltyIncrease = 0.1;
	
	private void increasePenalties (ArrayList<Node> path) {
		ArrayList<Link> links = nodeListToLinkList(path);
		for (Link link : links) {
			Double penalty = penalties.get(link);
			if (null == penalty)
				penalty = 0d;
			penalty += penaltyIncrease;
			penalties.put(link, penalty);
		}
	}
	
	private final int maxIteration = 50;
	
	@Override
	public boolean hasNext() {
		if (returnedPaths.size() == 0) {
			ArrayList<Node> path = findPath();
			if (null == path)
				return false;
			returnedPaths.add(path);
			shortestPathCosts = cost.get(endNode);
			return true;
		} 
		for (int iteration = 0; iteration < maxIteration; iteration++) {
			ArrayList<Node> path = findPath();
			if (cost.get(endNode) > shortestPathCosts * maximumTotalCostRatio)
				return false;
			boolean known = false;
			for (ArrayList<Node> knownPath : returnedPaths)
				if (comparePaths(path, knownPath)) {
					known = true;
					increasePenalties(path);
				}
			if (! known) {
				returnedPaths.add(path);
				return true;
			}
		}
		return false;
	}

}