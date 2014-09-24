package nl.tudelft.otsim.TrafficDemand;

import java.util.ArrayList;

public class ExportTripPattern {
	int startNode;
	int endNode;
	Double pathNumberOfTrips;
	Double totalNumberOfTrips;
	ArrayList<Integer> route;
	/**
	 * @param startNode
	 * @param endNode
	 * @param share
	 * @param totalNumberOfTrips
	 * @param route
	 */
	public ExportTripPattern(int startNode, int endNode,
			Double share, Double totalNumberOfTrips, ArrayList<Integer> route) {
		this.startNode = startNode;
		this.endNode = endNode;
		this.pathNumberOfTrips = share;
		this.totalNumberOfTrips = totalNumberOfTrips;
		this.route = route;
	}
	public int getStartNode() {
		return startNode;
	}
	public void setStartNode(int startNode) {
		this.startNode = startNode;
	}
	public int getEndNode() {
		return endNode;
	}
	public void setEndNode(int endNode) {
		this.endNode = endNode;
	}
	public Double getPathNumberOfTrips() {
		return pathNumberOfTrips;
	}
	public void setPathNumberOfTrips(Double share) {
		this.pathNumberOfTrips = share;
	}
	public Double getTotalNumberOfTrips() {
		return totalNumberOfTrips;
	}
	public void setTotalNumberOfTrips(Double totalNumberOfTrips) {
		this.totalNumberOfTrips = totalNumberOfTrips;
	}
	public ArrayList<Integer> getRoute() {
		return route;
	}
	public void setRoute(ArrayList<Integer> route) {
		this.route = route;
	}
	
}
