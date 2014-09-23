package nl.tudelft.otsim.Simulators.MacroSimulator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

import nl.tudelft.otsim.Simulators.MacroSimulator.Nodes.Node;
import nl.tudelft.otsim.Simulators.MacroSimulator.Nodes.NodeBoundaryIn;
import nl.tudelft.otsim.Simulators.MacroSimulator.Nodes.NodeInterior;

public class Routes {
	protected ArrayList<ArrayList<Integer>> routes = new ArrayList<ArrayList<Integer>>();
	protected ArrayList<Double> flows = new ArrayList<Double>();
	
	public Routes() {
		
	}
	
	public void addRoute(ArrayList<Integer> route, Double flow) {
		routes.add(route);
		int index = routes.indexOf(route);
		flows.add(index, flow);
	}
	public void deleteNode(Integer node) {
		
		for (ArrayList<Integer> route: routes) {
			if (route.contains(node)) {
				int index = route.indexOf(node);
				route.remove(index);
				//flows.remove(index);
			}
			
		}


	}
	public void cleanRoutes(HashSet<Integer> usedNodes) {
		ArrayList<ArrayList<Integer>> tmproutes = new ArrayList<ArrayList<Integer>>();
		for (ArrayList<Integer> route: routes) {
			ArrayList<Integer> tmproute = new ArrayList<Integer>();
			for (Integer i: route) {
				if (usedNodes.contains(i)) {
					tmproute.add(i);
					//flows.remove(index);
				}
					
			}
			tmproutes.add(tmproute);
		}
		routes = tmproutes;
	}
	public void setTurnFractions(ArrayList<NodeInterior> junctionNodes) {
		for (Node n : junctionNodes) {
			double[] flowIn = new double[n.cellsIn.size()];
			double[] flowOut = new double[n.cellsOut.size()];
			Arrays.fill(flowIn, 0);
			Arrays.fill(flowOut, 0);
			
			double[][] assignedFlows = new double[n.cellsIn.size()][n.cellsOut.size()];
			
			for (ArrayList<Integer> route: routes) {
				int indexInRoute = route.indexOf(n.getId());
				if (indexInRoute != -1) {
				int idOfUpstreamNode = route.get(indexInRoute-1);
				int idOfDownstreamNode = route.get(indexInRoute+1);
				double flow = flows.get(routes.indexOf(route));
				
				for (int i = 0; i < n.cellsIn.size(); i++) {
					MacroCell mcIn = n.cellsIn.get(i);
				
					if (mcIn.getConfigNodeIn() == idOfUpstreamNode) {
						for (int j = 0; j < n.cellsOut.size(); j++) {
							MacroCell mcOut = n.cellsOut.get(j);
							if (mcOut.getConfigNodeOut() == idOfDownstreamNode) {
								assignedFlows[i][j] = flow;
							}
							
							
						}
					}
				}
				
				}
				
			}
			
		
			n.setTurningRatio(assignedFlows);
		}
	}
	public void setInflowBoundaries(ArrayList<NodeBoundaryIn> inflowNodes) {
		for (NodeBoundaryIn n: inflowNodes) {
			
			for (ArrayList<Integer> route: routes) {
				
				if (route.get(0) == n.getId()) {
					
					n.setInflow(n.getInflow() + flows.get(routes.indexOf(route))/3600.0);
				}
			
			}
		}
	}

}
