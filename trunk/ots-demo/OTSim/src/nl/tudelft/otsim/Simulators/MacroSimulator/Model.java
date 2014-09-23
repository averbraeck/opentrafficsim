package nl.tudelft.otsim.Simulators.MacroSimulator;


import nl.tudelft.otsim.Simulators.SimulatedModel;
import nl.tudelft.otsim.Simulators.MacroSimulator.Nodes.Node;
import nl.tudelft.otsim.Simulators.MacroSimulator.Nodes.NodeBoundaryIn;



public class Model implements SimulatedModel {

	/** Time step number. Always starts as 0. */
	protected int k;

	/** Current time of the model [s]. Always starts at 0. */
	protected double t;

	/** Time step size of the model [s]. */
	public double dt;

	/** Maximum simulation period [s]. */
	public double period;

	protected int nrCells;
	protected int nrInflowNodes;


	public double getPeriod() {
		return period;
	}

	public void setPeriod(double period) {
		this.period = period;
	}

	private java.util.ArrayList<MacroCell> cells = new java.util.ArrayList<MacroCell>();

	private java.util.ArrayList<Node> nodes = new java.util.ArrayList<Node>();
	private java.util.ArrayList<NodeBoundaryIn> inflowNodes = new java.util.ArrayList<NodeBoundaryIn>();

	protected double[] state;

	public void init() {
		// Set attributes
		/* k = 0;
        t = 0;
        cells = new java.util.ArrayList<MacroCell>();
        nodes = new java.util.ArrayList<Node>();*/

		nrCells = getCells().size();
		state = new double[nrCells];
		if (inflowNodes.isEmpty()) {
		for (Node n: nodes) {
			if (n instanceof NodeBoundaryIn) {
				inflowNodes.add((NodeBoundaryIn) n);
			}
		}
		}
		nrInflowNodes = inflowNodes.size();

	}


	public void run(int n) {
		// Simulate n steps
		//int i = 0;
		//System.out.println("testrun");
		for (int nn = 0; (nn < n) && (t < period); nn++) {
			//System.out.println("test");
			//java.util.ArrayList<MacroCell> tmp2 = new java.util.ArrayList<MacroCell>(cells);
			//System.out.println("size Arraylist: " + Integer.toString(tmp2.size()));
			for (MacroCell c : getCells()) {
				//System.out.println("ID:\t" + Integer.toString(c.id()));
				//System.out.println(Double.toString(c.getK_r()));
				//System.out.println(Double.toString(c.getV_r()));
				//System.out.println("Ins: "+c.getIns() + " outs: " + c.getOuts());
				//System.out.println(Integer.toString(k));
				c.calcDemand();
				c.calcSupply();

			}
			for (Node node: getNodes() ) {
				node.calcFlux();
				//
				/*if (node.nrIn + node.nrOut != 2) {
    				for (double v: node.fluxesIn) {
    					System.out.println("FluxIn node:" + v);
    				}
    				for (double v: node.fluxesOut) {
    					System.out.println("FluxOut node:" + v);
    				}

    			}*/
			}
			for (MacroCell c2: getCells()) {
				c2.calcFluxOut();
				c2.calcFluxIn();
				c2.updateDensity();
				//System.out.println(Double.toString(c2.qCap));
				//System.out.println(Double.toString(c2.getV_r()));
			}
			// Update time
			k++; // Increment time step number
			t = k * dt; // time [s]
		}
	}
	public double t() {
		return t;
	}
	public void addMacroCell(MacroCell m) {
		getCells().add(m);
	}
	public void addNode(Node m) {
		getNodes().add(m);
	}
	public String saveStateToString() {
		String res = "[";
		for (MacroCell c: getCells()) {
			res = res + Double.toString(c.KCell) + ",";
		}
		res = res.substring(0, res.length()-1) + "]";
		return res;
	}
	public double[] saveStateToArray(String outputType) {
		double[] tmpstate;
		if (outputType == "density") {
			tmpstate = new double[nrCells];
			for (int i=0; i<getCells().size(); i++) {
				tmpstate[i] = getCells().get(i).KCell;
			}
		} else if (outputType == "speed") {
			tmpstate = new double[nrCells];
			for (int i=0; i<getCells().size(); i++) {
				tmpstate[i] = getCells().get(i).VCell;
				if (Double.isNaN(tmpstate[i])) {
					System.out.println("is nan");
				}
			}
		} else if (outputType == "inflow") {
			tmpstate = new double[nrInflowNodes];
			for (int i=0; i<nrInflowNodes; i++) {
				tmpstate[i] = inflowNodes.get(i).getInflow();
			}

		} else if (outputType == "criticalDensity") {
			tmpstate = new double[nrCells];
			for (int i=0; i<getCells().size(); i++) {
				tmpstate[i] = getCells().get(i).kCri;
			}

		} else if (outputType == "speedLimit") {
			tmpstate = new double[nrCells];
			for (int i=0; i<getCells().size(); i++) {
				tmpstate[i] = getCells().get(i).vLim;
			}

		} else if (outputType == "jamDensity") {
			tmpstate = new double[nrCells];
			for (int i=0; i<getCells().size(); i++) {
				tmpstate[i] = getCells().get(i).kJam;
			}

		} else {

			throw new IllegalStateException(getClass().getSimpleName() + ": wrong outputType in saveStateToArray class");
		}

		return tmpstate;
	}
	public void restoreState(double[] array, String outputType) {
		if (outputType == "density") {
			if (array.length != nrCells) {
				throw new Error("Wrong number of state variables");
			} else {
				for (int i=0; i<nrCells; i++) {
					getCells().get(i).KCell = array[i];
					
				}
			}
		} else if (outputType == "inflow") {
			if (array.length != nrInflowNodes) {
				throw new Error("Wrong number of inflow nodes");
			} else {
				for (int i=0; i<nrInflowNodes; i++) {
					inflowNodes.get(i).setInflow(array[i]);
				}
			}

		} else if (outputType == "criticalDensity") {
			if (array.length != nrCells) {
				throw new Error("Wrong number of state variables");
			} else {
				for (int i=0; i<nrCells; i++) {
					getCells().get(i).kCri = array[i];
				}
			}

		} else if (outputType == "speedLimit") {
			if (array.length != nrCells) {
				throw new Error("Wrong number of state variables");
			} else {
				for (int i=0; i<nrCells; i++) {
					getCells().get(i).setVLim(array[i]);
				}
			}

		} else if (outputType == "jamDensity") {
			if (array.length != nrCells) {
				throw new Error("Wrong number of state variables");
			} else {
				for (int i=0; i<nrCells; i++) {
					getCells().get(i).kJam = array[i];
				}
			}

		} else {
			throw new Error("Wrong parameter to be restored");
		}
	}

	/**
	 * @return the cells
	 */
	public java.util.ArrayList<MacroCell> getCells() {
		return cells;
	}

	/**
	 * @return the nodes
	 */
	public java.util.ArrayList<Node> getNodes() {
		return nodes;
	}

	/**
	 * @param nodes the nodes to set
	 */
	public void setNodes(java.util.ArrayList<Node> nodes) {
		this.nodes = nodes;
	}


}
