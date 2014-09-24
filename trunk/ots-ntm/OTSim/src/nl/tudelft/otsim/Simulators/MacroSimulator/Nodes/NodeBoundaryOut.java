package nl.tudelft.otsim.Simulators.MacroSimulator.Nodes;

import nl.tudelft.otsim.GeoObjects.Vertex;

public class NodeBoundaryOut extends Node {
	public NodeBoundaryOut(Vertex loc) {
		super(loc);
	}
	
	public void calcFlux() {
		// Currently, only unrestricted outflow
		if (nrIn == 1 && nrOut == 0) {
			double res = cellsIn.get(0).Demand;
			fluxesIn[0] = res;
			fluxesOut[0] = res;
		}

	}
}
