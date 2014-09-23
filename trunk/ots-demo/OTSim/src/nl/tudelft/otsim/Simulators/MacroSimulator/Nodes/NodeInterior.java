package nl.tudelft.otsim.Simulators.MacroSimulator.Nodes;

import nl.tudelft.otsim.GeoObjects.Vertex;


// For now: turnfractions are hardcoded to 50% in each direction
public class NodeInterior extends Node {
	public NodeInterior(Vertex loc) {
		super(loc);
	}
	
	public void calcFlux() {
		if (nrIn == 1 && nrOut == 1) {
			double res = Math.min(cellsOut.get(0).Supply, cellsIn.get(0).Demand);
			fluxesIn[0] = res;
			fluxesOut[0] = res;
		}
		if (nrIn == 1 && nrOut > 1) {
			double totIn = 0;
						
			for (int i=0; i<nrOut; i++) {
				fluxesOut[i]=Math.min((cellsIn.get(0).Demand)/nrOut,cellsOut.get(i).Supply);
				totIn += fluxesOut[i];
    		}
			fluxesIn[0] = totIn;
		}
		if (nrIn >1 && nrOut == 1) {
			double totOut = 0;
    		for (int i=0; i<nrIn; i++) {
    			//System.out.println(FluxIn2.length);
    			//System.out.println(Supply);
    			double Sstar = ((cellsOut.get(0).Supply)/nrIn);
    			//System.out.println(Sstar);
    			double S = Sstar;
    			fluxesIn[i] = Math.min(cellsIn.get(i).Demand,S);
    			totOut += fluxesIn[i];
    		}
    		fluxesOut[0] = totOut;
		}
		
	}
}
