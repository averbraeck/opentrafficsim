package nl.tudelft.otsim.ShortesPathAlgorithms;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import nl.tudelft.otsim.GeoObjects.Link;
import nl.tudelft.otsim.GeoObjects.Network;
import nl.tudelft.otsim.GeoObjects.Node;

/**
 *
 * @author not gtamminga
 */
/**
 * from the internet (devogella)
 * http://www.vogella.com/articles/JavaAlgorithmsDijkstra/article.html#dijkstra
 * @author gtamminga
 */
public class DijkstraAlgorithm {
	//private final List<Node> nodes;
	private final Collection<Link> edges;
	private Set<Node> settledNodes;
	private Set<Node> unSettledNodes;
	private Map<Node, Node> predecessors;
	private Map<Node, Double> distance;

	public DijkstraAlgorithm(Network network) {
		// Create a copy of the array so that we can operate on this array
		//this.nodes = new ArrayList<Node>(network.getAllNodeList());
		edges = network.getLinkList(); //new ArrayList<Link>(network.getLinkList());
	}
	
	public void execute(Node nodeA) {
		settledNodes = new HashSet<Node>();
		unSettledNodes = new HashSet<Node>();
		distance = new HashMap<Node, Double>();
		predecessors = new HashMap<Node, Node>();
		distance.put(nodeA, 0.0);
		unSettledNodes.add(nodeA);
		while (unSettledNodes.size() > 0) {
			Node node = getMinimum(unSettledNodes);
			settledNodes.add(node);
			unSettledNodes.remove(node);
			findMinimalDistances(node);
		}
	}

	private void findMinimalDistances(Node node) {
		List<Node> adjacentNodes = getNeighbors(node);
		for (Node target : adjacentNodes) {
			if (getShortestDistance(target) > getShortestDistance(node) + getDistance(node, target)) {
				distance.put(target, getShortestDistance(node) + getDistance(node, target));
				predecessors.put(target, node);
				unSettledNodes.add(target);
			}
		}
	}

	private double getDistance(Node node, Node target) {
		for (Link link : edges)
			if (link.getFromNodeExpand().equals(node) && link.getToNodeExpand().equals(target))
				return link.getLength();
		throw new RuntimeException("Should not happen");
	}

	private List<Node> getNeighbors(Node node) {
		List<Node> neighbors = new ArrayList<Node>();
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
			else if (getShortestDistance(node) < getShortestDistance(minimum))
				minimum = node;
		return minimum;
	}

	private boolean isSettled(Node nodes) {
		return settledNodes.contains(nodes);
	}

	private double getShortestDistance(Node nodeB) {
		Double d = distance.get(nodeB);
		if (d == null)
			return Double.MAX_VALUE;
		return d;
	}

	public LinkedList<Link> getPathLinks(LinkedList<Node> path) {
    	LinkedList<Link> pathLinks = new LinkedList<Link>();
        Node nodeStart=null;
        int i = 0;
        for (Node pathNodes : path) {
            i++;
            Node nodeEnd = pathNodes;
            if (i > 1)
                for (Link link : edges)
                    if (link.getFromNodeExpand().equals(nodeStart) && link.getToNodeExpand().equals(nodeEnd))
                    	pathLinks.add(link);
            nodeStart = pathNodes;
        }
        Collections.reverse(pathLinks);
        return pathLinks;
    }
	
	/*
	 * This method returns the path from the source to the selected target or
	 * NULL if no path exists
	 */
	public ArrayList<Node> getPathNodes(Node target) {

		ArrayList<Node> pathNodes = new ArrayList<Node>();
		Node step = target;
		// Check if a path exists
		if (null == predecessors.get(step))
			return null;
		pathNodes.add(step);
		while (predecessors.get(step) != null) {
			step = predecessors.get(step);
			pathNodes.add(step);
		}
		// Put it into the correct order
		Collections.reverse(pathNodes);		
		return pathNodes;
	}
/*	private static final int BSIZE = 1024;
	public static final int ENDBUFFER = -999;
	public IntBuffer getPathNodesBytes(Node target) {

	    ByteBuffer bb = ByteBuffer.allocate(BSIZE);
	    IntBuffer ib = bb.asIntBuffer();
		Node step = target;
		// Check if a path exists
		if (null == predecessors.get(step))
			return null;
		ib.put(step.getNodeID());
		while (predecessors.get(step) != null) {
			step = predecessors.get(step);
			ib.put(step.getNodeID());
		}
		ib.put(ENDBUFFER);

		// TODO Put it into the correct order		
		return ib;
	}*/

}