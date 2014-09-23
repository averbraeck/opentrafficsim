package nl.tudelft.otsim.Simulators.MacroSimulator.Nodes;

import nl.tudelft.otsim.GeoObjects.Vertex;

public class NodeBoundaryIn extends Node {
	//Currently, hardcoded inflow is taken
	private double inflowPerLane;
	public NodeBoundaryIn(Vertex loc, double inflow) {
		super(loc);
		setInflowPerLane(inflow);
	}
	
	public double getInflowPerLane() {
		return inflowPerLane;
	}
	public void setInflowPerLane(double in) {
		inflowPerLane = in;
	}
	public void setInflow(double in) {
		inflowPerLane = in/cellsOut.get(0).lanes;
	}
	public double  getInflow() {
		return inflowPerLane*cellsOut.get(0).lanes;
	}
	public void calcFlux() {
		
		if (nrIn == 0 && nrOut == 1) {
			double res = Math.min(cellsOut.get(0).Supply, inflowPerLane*cellsOut.get(0).lanes);
			fluxesIn[0] = res;
			fluxesOut[0] = res;
		}
				
	}
}
