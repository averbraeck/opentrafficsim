package nl.tudelft.otsim.Simulators.MacroSimulator;


import java.awt.event.MouseEvent;
import java.util.ArrayList;




import java.util.HashSet;

import nl.tudelft.otsim.Events.Scheduler;
import nl.tudelft.otsim.Events.Step;
import nl.tudelft.otsim.GUI.GraphicsPanel;
import nl.tudelft.otsim.GUI.WED;
import nl.tudelft.otsim.GeoObjects.Vertex;
import nl.tudelft.otsim.Simulators.ShutDownAble;
import nl.tudelft.otsim.Simulators.SimulatedObject;
import nl.tudelft.otsim.Simulators.Simulator;
import nl.tudelft.otsim.Simulators.MacroSimulator.MacroSimulator;
import nl.tudelft.otsim.Simulators.MacroSimulator.FundamentalDiagrams.IFD;
import nl.tudelft.otsim.Simulators.MacroSimulator.FundamentalDiagrams.FDSmulders;
import nl.tudelft.otsim.Simulators.MacroSimulator.Nodes.Node;
import nl.tudelft.otsim.Simulators.MacroSimulator.Nodes.NodeBoundaryIn;
import nl.tudelft.otsim.Simulators.MacroSimulator.Nodes.NodeBoundaryOut;
import nl.tudelft.otsim.Simulators.MacroSimulator.Nodes.NodeInterior;
import nl.tudelft.otsim.Simulators.MacroSimulator.Nodes.NodeInteriorTampere;
//import nl.tudelft.otsim.Simulators.MacroSimulator.MacroModel;
import nl.tudelft.otsim.Utilities.TimeScaleFunction;

/**
 * Macro Simulator for OpenTraffic
 * 
 * @author Friso Scholten
 */
public class MacroSimulator extends Simulator implements ShutDownAble{
	/** Type of this Simulator */
	public static final String simulatorType = "Macro simulator";
	
	private final Model model = new Model();
	private final Scheduler scheduler;
	//private double endTime = 1000;	// should be overridden in the configuration
	private double randomSeed = 0;	// idem
	
	
	
	
	private ArrayList<MacroCell> macroCells = new ArrayList<MacroCell>();
	private ArrayList<Node> nodes = new ArrayList<Node>();

	/**
	 * Create a MacroSimulator.
	 * @param configuration String; textual description of the network,
	 * traffic demand and measurement plans
	 * @param graphicsPanel {@link GraphicsPanel} to draw on
	 * @param scheduler {@link Scheduler} for this simulation
	 * @throws Exception 
	 */
	public MacroSimulator(String configuration, GraphicsPanel graphicsPanel, Scheduler scheduler) throws Exception {
		System.out.println("Creating a new MacroSimulator based on description:\n" + configuration);
		this.scheduler = scheduler;
		scheduler.enqueueEvent(0, new Stepper(this));	// Set up my first evaluation
		model.period = 1800;
		model.dt = 0.2;
		
		// Set minimum length of cells to be generated (in [m])
		double minLengthCells = 100;
		
		// Set used fundamental diagram
		IFD fd = new FDSmulders();
		
		// Set inflow at boundaries in (in vehicles per sec per lane)
		double inflowBoundary = (2000.0/3600.0);
		
		//ArrayList<MacroCell> cells = new ArrayList<MacroCell>();
		ArrayList<MacroCell> copySimPaths = new ArrayList<MacroCell>();
		
		Routes routes = new Routes();
		TimeScaleFunction nrTripsPattern = new TimeScaleFunction();
		
		/*
		 * It does make sense to first join successive roadway sections that
		 * have the same capacity and speed limit. Then split up longer roadways 
		 * into multiple cells and create a class that macro-simulates one cell.
		 */
		for (String line : configuration.split("\n")) {
			String[] fields = line.split("\t");
			if (fields.length == 0)
				continue;	// Ignore empty lines in configuration
			else if (fields[0].equals("EndTime:"))
				model.period = Double.parseDouble(fields[1]);
			else if (fields[0].equals("Seed:"))
				this.randomSeed = Double.parseDouble(fields[1]);
			else if (fields[0].equals("Roadway:")) {
				MacroCell sp = new MacroCell(model);
				// set ID of MacroCell
				sp.setId(Integer.parseInt(fields[1]));
				
				for (int i = 2; i < fields.length; i++) {
					if (fields[i].equals("from"))
						sp.setConfigNodeIn(Integer.parseInt(fields[++i]));
					if (fields[i].equals("to"))
						sp.setConfigNodeOut(Integer.parseInt(fields[++i]));
					if (fields[i].equals("speedlimit"))
						sp.setVLim(Double.parseDouble(fields[++i])/3.6);
					if (fields[i].equals("lanes"))
						sp.setWidth(3.5 * Double.parseDouble(fields[++i]));
					else if (fields[i].equals("vertices")) {
						// add vertices
						while (fields[++i].startsWith("(")) {
							Vertex tmp = new Vertex(fields[i]);
							sp.addVertex(tmp);
							
						}
						
						// decrease i to start check the right field in the following loop
						--i;
					
					} else if (fields[i].equals("ins")) {
						// add all incoming links to MacroCell
						while (!fields[++i].startsWith("outs")) {
							sp.addIn(Integer.valueOf(fields[i]));
						}
						// decrease i to start check the right field in the following loop
						--i;
					} else if (fields[i].equals("outs")) {
						// add all outgoing links to MacroCell
						while (++i <= fields.length-1) {
							sp.addOut(Integer.valueOf(fields[i]));
						}
					}
			
				} 
				copySimPaths.add(sp.getId(),sp); 

			} else if (fields[0].equals("TripPatternPath")) { 
				nrTripsPattern = new TimeScaleFunction(fields[2]);
			}
			else if (fields[0].equals("Path:")) {
    			//if (null != exportTripPattern)
    			//	tripList.add(exportTripPattern);
    			//exportTripPattern = new ExportTripPattern(flowGraph, classProbabilities);
				
        		ArrayList<Integer> route = new ArrayList<Integer>(); 
        		for (int i = 3; i < fields.length; i++) {
        			String field = fields[i];
        			if (field.endsWith("a"))
        				route.add(Integer.parseInt(field.substring(0, field.length() - 1)));
        			if (! field.endsWith("a"))
        				route.add(Integer.parseInt(field));
        		}
        		double routeProbability = Double.parseDouble(fields[1]);
        		routes.addRoute(route, nrTripsPattern.getFactor(0)*routeProbability);
        		//exportTripPattern.addRoute(route, routeProbability);
			} else {
				//throw new Exception("Don't know how to parse " + line);
			}
			
			// TODO: write code to handle the not-yet-handled lines in the configuration
		}
		
		// Now all macrocells are generated, link upstream and downstream macrocells together. 
		for (MacroCell mc: copySimPaths) {
			
			
			for (Integer i: mc.ins) {
				mc.addIn(copySimPaths.get((int) i));
			}
			for (Integer j: mc.outs) {
				mc.addOut(copySimPaths.get((int) j));
			}
			
		}
		
		// Next step: join cells as much as possible
		// Cells are joined when no difference in speed limit, no difference in lane, and no merges and splits are present
		
		// make list of links that still need to be joined
		ArrayList<Integer> todo = new ArrayList<Integer>();
		for (int i=0; i<copySimPaths.size(); i++) {
			todo.add((Integer) i);
		}
		
		
		
		// while there are links to be joined
		while (!(todo.size() == 0)) {
			// make new cell with the same properties as the first cell in the to do list
			MacroCell snew = new MacroCell(model);
			MacroCell sbegin = copySimPaths.get(todo.get(0));
			
			todo.remove(0);
			snew.id = sbegin.id;
			snew.vertices.addAll(0, sbegin.vertices);
			snew.ups = (ArrayList<MacroCell>) sbegin.ups.clone();
			for (MacroCell c: snew.ups) {
				c.downs.remove(sbegin);
				c.downs.add(snew);
			}
			snew.downs = (ArrayList<MacroCell>) sbegin.downs.clone();
			for (MacroCell c: snew.downs) {
				c.ups.remove(sbegin);
				c.ups.add(snew);
			}
			snew.setWidth(sbegin.getWidth());
			snew.setVLim(sbegin.getVLim());
			snew.setConfigNodeIn(sbegin.getConfigNodeIn());
			snew.setConfigNodeOut(sbegin.getConfigNodeOut());
			// if there is only one cell upstream of considered cell
			while((snew.ups.size() == 1)) {
				
				MacroCell sp = snew.ups.get(0);
				if (sp.getConfigNodeOut() == 0 && snew.getConfigNodeIn() == 0) {
					snew.setConfigNodeIn(sp.getConfigNodeIn());
					sp.setConfigNodeOut(snew.getConfigNodeOut());
				}
				// test if cell upstream has the right nr of lanes and speedlimit
				if (!(sp.downs.size() == 1) || (!(sp.getWidth() == snew.getWidth())) || (!(sp.getVLim() == snew.getVLim()))) {
					
					break;
				} else {
					// cell upstream has the right nr of lanes and speed limit
					
					// add vertices of cell in front of vertices of current cell 
					snew.vertices.addAll(0,sp.vertices);
					
					// new cell to be considered is the upstream cell
					snew.ups = (ArrayList<MacroCell>) sp.ups.clone();
					// update links to upstream cells
					for (MacroCell c: snew.ups) {
						c.downs.remove(sp);
						c.downs.add(snew);
					}
					int configNodeIn = sp.getConfigNodeIn();
					if (configNodeIn != 0) {
					snew.setConfigNodeIn(configNodeIn);
					}
					
					// remove the upstream cell from to do list
					todo.remove(new Integer(sp.getId()));
				}
			}
			// test if cell downstream has the right nr of lanes and speedlimit
			while((snew.downs.size() == 1)) {
				MacroCell sp = snew.downs.get(0);
				if (sp.getConfigNodeIn() == 0 && snew.getConfigNodeOut() == 0) {
					snew.setConfigNodeOut(sp.getConfigNodeOut());
					sp.setConfigNodeIn(snew.getConfigNodeIn());
				}
				if (!(sp.ups.size() == 1) || (!(sp.getWidth() == snew.getWidth())) || (!(sp.getVLim() == snew.getVLim()))) {
					
					break;
				} else {
					// cell downstream has the right nr of lanes and speed limit
					
					// add vertices of cell at the end of vertices of current cell
					snew.vertices.remove(snew.vertices.size() -1);
					snew.vertices.addAll(sp.vertices);
					
					// new cell to be considered is the downstream cell
					snew.downs = (ArrayList<MacroCell>) sp.downs.clone();
					// update links to downstream cells
					for (MacroCell c: snew.downs) {
						c.ups.remove(sp);
						c.ups.add(snew);
					}
					if (sp.getConfigNodeOut() != 0) {
					snew.setConfigNodeOut(sp.getConfigNodeOut());
					}
					// remove the downstream cell from todo list
					todo.remove(new Integer(sp.getId()));
				}
			}
			
			// add new (joined) cell to the list of cells
			macroCells.add(snew);
			
			
		}
		System.out.println(routes.routes);
		HashSet<Integer> nodesUsed = new HashSet<Integer>();
		for (MacroCell m: macroCells) {
			if (m.downs.size()==1 & m.getConfigNodeOut()==0) {
				m.setConfigNodeOut(m.downs.get(0).getConfigNodeOut());
			}
			if (m.ups.size()==1 & m.getConfigNodeIn()==0) {
				m.setConfigNodeIn(m.ups.get(0).getConfigNodeIn());
			}
		}
		for (MacroCell m: macroCells) {
			System.out.println("Vertices pre-split: "+m.vertices.toString());
			System.out.println("Node at In: "+m.getConfigNodeIn());
			System.out.println("Node at Out: "+m.getConfigNodeOut());
			
			nodesUsed.add(m.getConfigNodeIn());
			nodesUsed.add(m.getConfigNodeOut());
		}
		
		System.out.println(nodesUsed);
		routes.cleanRoutes(nodesUsed);
		System.out.println(routes.routes);
		System.out.println(routes.flows);
		// Next step: split the joined cells into smaller cells of similar size
		ArrayList<MacroCell> copyCells = new ArrayList<MacroCell>();
		for (MacroCell m: macroCells) {
			// determine number of parts in which the cell must be split
			int nrParts = (int) (m.calcLength()/minLengthCells);
			if (nrParts == 0)
				nrParts = 1;
			// add the cells that are split to the list
			copyCells.addAll(m.splitInParts(nrParts));
		}
		macroCells = copyCells;
		
		// give all the cells in the list new IDs
		int tel = 0;
		for (MacroCell m: macroCells) {
			m.setId(tel);
			tel++;
		}
		
		for (MacroCell m: macroCells) {
			if (m.nodeIn == null) {
				
				if (m.ups.size() > 0) {
					NodeInteriorTampere n = new NodeInteriorTampere(m.vertices.get(0));
					for (MacroCell c: m.ups.get(0).downs) {
						n.cellsOut.add(c);
						c.nodeIn = n;
						
					}
					for (MacroCell c: m.ups) {
						n.cellsIn.add(c);
						c.nodeOut = n;
						c.vertices.add(m.vertices.get(0));
					}
					nodes.add(n);
				} else {
					
					NodeBoundaryIn n = new NodeBoundaryIn(m.vertices.get(0),0);
					n.cellsOut.add(m);
					m.nodeIn = n;
					nodes.add(n);
				}
				
				
			}
			if (m.nodeOut == null) {
				
				if (m.downs.size() > 0) {
					NodeInteriorTampere n = new NodeInteriorTampere(m.vertices.get(m.vertices.size()-1));
					for (MacroCell c: m.downs.get(0).ups) {
						n.cellsIn.add(c);
						c.nodeOut = n;
					}
				
					for (MacroCell c: m.downs) {
						n.cellsOut.add(c);
						c.nodeIn = n;
						c.vertices.add(0,m.vertices.get(m.vertices.size()-1));
					}
					nodes.add(n);
				} else {
					NodeBoundaryOut n = new NodeBoundaryOut(m.vertices.get(0));
					n.cellsIn.add(m);
					m.nodeOut = n;
					nodes.add(n);
				}
				
			}
		}
		ArrayList<NodeInterior> junctionNodes = new ArrayList<NodeInterior>();
		ArrayList<NodeBoundaryIn> inflowNodes = new ArrayList<NodeBoundaryIn>();
		ArrayList<NodeBoundaryOut> outflowNodes = new ArrayList<NodeBoundaryOut>();
		for (Node n: nodes) {
			if ((n.cellsIn.size() != 1 || n.cellsOut.size() != 1) && (n.cellsOut.size()+n.cellsIn.size() != 0)) {
			HashSet<Integer> nodeIds = new HashSet<Integer>();
			
			for(MacroCell up: n.cellsIn) {
				nodeIds.add(up.getConfigNodeOut());
			}
			for(MacroCell down: n.cellsOut) {
				nodeIds.add(down.getConfigNodeIn());
			}
			System.out.println(nodeIds);
			if (nodeIds.size() == 1) {
				if (n.cellsIn.size() != 0) {
					n.setId(n.cellsIn.get(0).getConfigNodeOut());
				} else { 
					n.setId(n.cellsOut.get(0).getConfigNodeIn());
				}
				
				if ((n.cellsIn.size() != 0 && n.cellsOut.size() != 0)) {
					junctionNodes.add((NodeInterior) n);
				} else if (n.cellsIn.size() == 0) {
					inflowNodes.add((NodeBoundaryIn) n);
				} else if (n.cellsIn.size() == 0) {
					outflowNodes.add((NodeBoundaryOut) n);
				}
			} else {
				throw new Error("Wrong references to nodes in adjacent cells");
			}
			}
		}
		System.out.println(junctionNodes);
		// initialize all cells (e.g. determine parameters needed for simulation) and add to the model
		
		for (Node n: nodes) {
			
			n.init();
			n.setDefaultTurningRatio();
			model.addNode(n);
			
			
		}
		
for (MacroCell m: macroCells) {
			//System.out.println("Vertices1: "+m.vertices.toString());
			m.smoothVertices(0.8);
			//System.out.println("Vertices2: "+m.vertices.toString());
			
		}


		for (MacroCell m: macroCells) {
			
			m.fd = fd;
			m.init();
			model.addMacroCell(m);
			
		}
		routes.setTurnFractions(junctionNodes);
		routes.setInflowBoundaries(inflowNodes);
		
		for (MacroCell m: macroCells) {
			//System.out.println("length:"+m.l);
			//System.out.println("NodeIn: "+m.indexNodeIn);
			//System.out.println("NodeOut: "+m.indexNodeOut);
		}
		model.init();	
		for (MacroCell m: macroCells) {
			System.out.println("index: "+macroCells.indexOf(m)+" from:"+m.vertices.get(0));
			//System.out.println("NodeIn: "+m.indexNodeIn);
			//System.out.println("NodeOut: "+m.indexNodeOut);
		}
	}
	
	
	
	public final Model getModel() {
		return model;
	}
	
	@Override
	public void setModified() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void repaintGraph(GraphicsPanel graphicsPanel) {
		for (MacroCell sp : macroCells) {
			sp.draw(graphicsPanel);
		}
		for (Node n: nodes) {
			n.draw(graphicsPanel);
		}
	}

	@Override
	public void mousePressed(GraphicsPanel graphicsPanel, MouseEvent evt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseDragged(GraphicsPanel graphicsPanel, MouseEvent evt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseReleased(GraphicsPanel graphicsPanel, MouseEvent evt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseMoved(GraphicsPanel graphicsPanel, MouseEvent evt) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void ShutDown() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void preStep() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void postStep() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Scheduler getScheduler() {
		return scheduler;
	}

	@Override
	public ArrayList<SimulatedObject> SampleMovables() {
		// TODO Auto-generated method stub
		return null;
	}
class Stepper implements Step {
		final private MacroSimulator macroSimulator;
		
		public Stepper (MacroSimulator macroSimulator) {
			this.macroSimulator = macroSimulator;
		}
		

	@Override
	public Scheduler.SchedulerState step(double now) {
    	//System.out.println("step entered");
    	Model model = macroSimulator.getModel();
    	//System.out.println(Double.toString(model.period));
    	//System.out.println(Double.toString(now));
    	//System.out.println(Double.toString(model.t()));
    	if (now >= model.period)
    		return Scheduler.SchedulerState.EndTimeReached;
    	while (model.t() < now) {
    		//System.out.println("step calling run(1)");
    		try {
    			//System.out.format(Main.locale, "Time is %.3f\r\n", now);
    			model.run(1);
    		} catch (RuntimeException e) {
    			WED.showProblem(WED.ENVIRONMENTERROR, "Error in MacroSimulator:\r\n%s", WED.exeptionStackTraceToString(e));
    			return Scheduler.SchedulerState.SimulatorError;
    		}
    	}
    	// re-schedule myself
    	macroSimulator.getScheduler().enqueueEvent(model.t() + model.dt, this);
    	//System.out.println("step returning true");
		return null;
	}
	
	
}
}
