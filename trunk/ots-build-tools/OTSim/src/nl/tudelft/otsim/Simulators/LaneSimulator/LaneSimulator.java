package nl.tudelft.otsim.Simulators.LaneSimulator;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;
import java.util.Random;

import nl.tudelft.otsim.Events.Scheduler;
import nl.tudelft.otsim.Events.Step;
import nl.tudelft.otsim.GUI.FakeFundamentalDiagram;
import nl.tudelft.otsim.GUI.GraphicsPanel;
import nl.tudelft.otsim.GUI.Main;
import nl.tudelft.otsim.GUI.ObjectInspector;
import nl.tudelft.otsim.GUI.WED;
import nl.tudelft.otsim.Simulators.Measurement;
import nl.tudelft.otsim.Simulators.LaneSimulator.Conflict;
import nl.tudelft.otsim.Simulators.ShutDownAble;
import nl.tudelft.otsim.Simulators.SimulatedObject;
import nl.tudelft.otsim.Simulators.SimulatedTrafficLightController;
import nl.tudelft.otsim.Simulators.Simulator;
import nl.tudelft.otsim.SpatialTools.Planar;
import nl.tudelft.otsim.Utilities.TimeScaleFunction;

/**
 * This class holds all data of a laneSimulator.
 * 
 * @author Wouter J Schakel
 */
public class LaneSimulator extends Simulator implements ShutDownAble {
	
	private final Model model = new Model();
	private ArrayList<LaneGraphic> laneGraphics = new ArrayList<LaneGraphic>();
	private final Scheduler scheduler;
	/** Type of this Simulator */
	public static final String simulatorType = "Lane simulator";
	
	private int highestLaneID = 0;

	/**
	 * Retrieve the {@link Model} of this laneSimulator.
	 * @return {@link Model}; the model of this laneSimulator
	 */
	public final Model getModel() {
		return model;
	}

	/**
	 * Create a new LaneSimulator.
	 * @param definition String; textual definition of the network and demand
	 * @param graphicsPanel {@link GraphicsPanel}; output device for drawing
	 * the current state
	 * @param scheduler Scheduler for this LaneSimulator
	 * @throws Exception 
	 */
	public LaneSimulator(String definition, GraphicsPanel graphicsPanel, Scheduler scheduler) throws Exception {
		this.scheduler = scheduler;
		/* test the flip operations
		for (int direction = 0; direction < 6; direction++) {
			System.out.print("Direction " + direction + " is " + Movable.directionToString(direction));
			for (int flip = 0; flip <= 30; flip += 10)
				System.out.print(" with " + Movable.flipToString(flip) + " " + Movable.directionToString(Movable.flipDirection(direction, flip)));
			System.out.println("");
		}
		*/
		
		// STUB set reasonable defaults
		model.period = 1800;
        model.dt = .2;
        model.debug = true;
        model.setSeed(1);
        model.settings.putBoolean("storeDetectorData", true);
        ArrayList<Lane> microNetwork = new ArrayList<Lane>(); 
    	ArrayList<ExportTripPattern> tripList = new ArrayList<ExportTripPattern>(); 
    	ArrayList<Measurement> measurements = new ArrayList<Measurement>();
    	int vehicleDriverCount = 0;
    	double[] classProbabilities = null;

    	// Two passes; this is pass 1; extract the lane descriptions
        for(String line : definition.split("\n")) {
        	String[] fields = line.split("\t");
        	
        	if (null == fields[0])
        		continue;
        	if (fields[0].equals("Lane")) {	
        		System.out.println("description: " + line);
        		int id = Integer.parseInt(fields[2]);
        		if (id < highestLaneID)
        			highestLaneID = id;
        		double speedLimit = Double.parseDouble(fields[4]);
        		int numberOfPoints = (fields.length - 6);
        		double[] x = new double[numberOfPoints];
        		double[] y = new double[numberOfPoints];
        		for (int i = 0; i < numberOfPoints; i++) {
        			String subField = fields[i + 6];
        			String[] subFields = subField.split(",");
        			if (subFields.length != 2)
        				throw new Error ("Bad number of subFields in " + subField);
        			x[i] = Double.parseDouble(subFields[0]);
        			y[i] = Double.parseDouble(subFields[1]);
        		}
        		// Add it to the network
        		//System.out.println(String.format("microNetwork.add(new jLane(model, %s, %s, %d", x.toString(), y.toString(), id));
        		Lane newLane = new Lane(model, x, y, id);
        		// set origin and destination as default to none
        		newLane.destination = 0;
        		newLane.origin = 0;   
        		newLane.vLim = speedLimit;
        		microNetwork.add(newLane);
        	}
        }
    	//double numberOfTripsPath = 0;
        TimeScaleFunction flowGraph = null;
    	ExportTripPattern exportTripPattern = null;
    	// pass 2; extract everything else
        for(String line : definition.split("\n")) {
        	String[] fields = line.split("\t");
        	if (! fields[0].equals("Lane"))	
        		System.out.println("description: " + line);
        	
        	if (null == fields[0])
        		continue;
        	else if (fields[0].equals("EndTime:"))
        		model.period = Double.parseDouble(fields[1]);
        	else if (fields[0].equals("Seed:"))
        		model.setSeed(Integer.parseInt(fields[1]));
        	else if (fields[0].equals("Lane"))
        		continue;
        	else if (fields[0].equals("LaneData")) {	
				int id = Integer.parseInt(fields[2]);
				Lane lane = lookupLane(id, microNetwork);
				for (int i = 3; i < fields.length; i += 2) {
        			if (fields[i].equals("up:"))  {
        				int up = Integer.parseInt(fields[i+1]);
        				Lane upLane = lookupLane(up, microNetwork);
        				if (!lane.ups.isEmpty())
        					lane.ups.add(upLane);
        				else if (lane.up != null)  {
        					lane.ups.add(lane.up);
        					lane.ups.add(upLane);
        					lane.up = null;
        				}
        				else
        					lane.up = upLane;
        			} else if (fields[i].equals("down:"))  {
        				int down = Integer.parseInt(fields[i+1]);
        				Lane downLane = lookupLane(down, microNetwork);
        				if (!lane.downs.isEmpty())
        					lane.downs.add(downLane);
        				else if (lane.down != null)  {
        					lane.downs.add(lane.down);
        					lane.downs.add(downLane);
        					lane.down = null;
        				}
        				else
        					lane.down = downLane;
        			} else if (fields[i].equals("crossingYieldTo:")) {
        				int laneCrossID  = Integer.parseInt(fields[i + 1]);
        				Lane crossLane = lookupLane(laneCrossID, microNetwork);
        				Conflict.createCrossing(lane, crossLane, true);
        			} else if (fields[i].equals("mergingYieldTo:")) {
        				int laneMergeID  = Integer.parseInt(fields[i + 1]);
        				Lane mergeLane = lookupLane(laneMergeID, microNetwork);
        				Lane downLane = lane.down;
        				if (null == downLane)
        					throw new Exception("Cannot create merge onto lane " + lane.id() + " because it does has " + (null == lane.downs ? 0 : lane.downs.size()) + " downs (should be 1)");
        				Conflict.createMerge(downLane, lane, mergeLane);
        			} else if (fields[i].equals("right:")) {
        				int right = Integer.parseInt(fields[i + 1]);
        				Lane rightLane = lookupLane(right, microNetwork);
        				lane.right = rightLane;
        			} else if (fields[i].equals("left:")) {
        				int left = Integer.parseInt(fields[i + 1]);
        				Lane leftLane = lookupLane(left, microNetwork);
        				lane.left = leftLane;
        			} else if (fields[i].equals("goRight:")) {
        				String goRight = fields[i + 1];
        				lane.goRight = Boolean.parseBoolean(goRight);
        			} else if (fields[i].equals("goLeft:")) {
        				String goLeft = fields[i + 1];
        				lane.goLeft = Boolean.parseBoolean(goLeft);
        			} else if (fields[i].equals("origin:")) {
        				int origin = Integer.parseInt(fields[i + 1]);
        				lane.origin = origin;
        			} else if (fields[i].equals("destination:")) {
        				int destination = Integer.parseInt(fields[i + 1]);
        				lane.destination = destination; 
        			}
        		}
        		// detect conflicts
        		if (lane.downs.size() >= 2) {
        			int itel = 0;
        			for (Lane down : lane.downs) {
        				itel++;
        				for (int jj = itel; jj < lane.downs.size(); jj++)
        					Conflict.createSplit(down, lane.downs.get(jj));
        			}
        		}
        	} else if (fields[0].equals("TripPattern")) {
        		classProbabilities = new double[vehicleDriverCount];
        		int startField = 6;
        		double sum = 0;
        		for (int classIndex = 0; classIndex < fields.length - startField; classIndex++) {
        			String[] subFields = fields[classIndex + startField].split("[:]");
        			sum += classProbabilities[classIndex] = Double.parseDouble(subFields[1]);
        		}
        		if (Math.abs(sum - 1.0) > 0.0001)
        			throw new Error("Sum of Fractions does not add up to 1");
        		continue;
        	} else if (fields[0].equals("TripPatternPath"))
        		flowGraph = new TimeScaleFunction(fields[2]);
        		//numberOfTripsPath = Double.parseDouble(fields[2]);	// Only numberOfTrips is actually used
        	else if (fields[0].equals("Path:")) {
    			if (null != exportTripPattern)
    				tripList.add(exportTripPattern);
    			exportTripPattern = new ExportTripPattern(flowGraph, classProbabilities);
        		ArrayList<Integer> route = new ArrayList<Integer>(); 
        		for (int i = 3; i < fields.length; i++) {
        			String field = fields[i];
        			if (field.endsWith("a"))
        				route.add(Integer.parseInt(field.substring(0, field.length() - 1)));
        			if (! field.endsWith("a"))
        				route.add(Integer.parseInt(field));
        		}
        		double routeProbability = Double.parseDouble(fields[1]);
        		exportTripPattern.addRoute(route, routeProbability);
        	} else if (fields[0].equals("Section LaneGeom"))
        		continue;
        	else if (fields[0].equals("Section LaneData"))
        		continue;
        	else if (fields[0].equals("Section TrafficDemand"))
        		continue;
        	else if (fields[0].equals("NumberOfZones:"))
        		continue;	// FIXME; is there a reason to export the number of zones?
        	else if (fields[0].equals("TrafficLight")) {
        		for (String location : fields[2].split(",")) {
        			String[] subFields = location.split(" ");
        			Lane lane = lookupLane(Integer.parseInt(subFields[0]), microNetwork);
        			if (null == lane)
        				throw new Exception("TrafficLight " + fields[1] + " lies on undefined lane " + subFields[0]);
        			model.addController(new TrafficLight(lane, Double.parseDouble(subFields[1]), fields[1], Planar.coordinatesToPoints(fields, 3, fields.length)));
        		}
        	} else if (fields[0].equals("TrafficLightController")) {
        		String controllerURL = null;
        		if (fields.length > 4)
        			controllerURL = fields[4];
        		// This depends on all TrafficLights and Detectors to be created BEFORE the SimulatedTrafficLightController
        		SimulatedTrafficLightController tlc = new SimulatedTrafficLightController(scheduler, controllerURL);
        		for (String tlName : fields[2].split(",")) {
        			for (Controller controller : model.controllers)
        				if (controller instanceof TrafficLight) {
        					TrafficLight tl = (TrafficLight) controller;
        					if (tl.name().equals(tlName))
        	        			tlc.addTrafficLight(tl);
        				}
        		}
        		for (String dName : fields[3].split(",")) {
        			for (Controller controller : model.controllers)
        				if (controller instanceof OccupancyDetector) {
        					OccupancyDetector detector = (OccupancyDetector) controller;
        					if (detector.name().equals(dName))
        	        			tlc.addDetector(detector);
        				}
        		}
        	} else if (fields[0].equals("Detector")) {
        		for (String location : fields[2].split(",")) {
        			String[] subFields = location.split(" ");
        			if ((null == subFields[0]) || (subFields[0].equals("")))
        				throw new Exception("Detector " + fields[1] + " lies on no lane");
        			Lane lane = lookupLane(Integer.parseInt(subFields[0]), microNetwork);
        			if (null == lane)
        				throw new Exception("Detector " + fields[1] + " lies on undefined lane " + subFields[0]);
        			model.addController(new OccupancyDetector(lane, Double.parseDouble(subFields[1]), fields[1], Double.parseDouble(subFields[2]), Planar.coordinatesToPoints(fields, 3, fields.length)));
        		}
        	} else if (fields[0].equals("MeasurementPlan"))
        		measurements.add(new Measurement(fields[1], fields[2], fields[3], this, scheduler));
        	else if (fields[0].equals("TrafficClass")) {
                Vehicle veh = new Vehicle(model);
                veh.l = Double.parseDouble(fields[2]);
                veh.vMax = Double.parseDouble(fields[3]);
                veh.marker = fields[1];
                veh.aMin = Double.parseDouble(fields[4]);
                try {
                    veh.trajectory = new Trajectory(veh, "nl.tudelft.otsim.Simulators.LaneSimulator.FCD");
                } catch (ClassNotFoundException cnfe) {
                    System.err.println("failed to make a new Trajectory");
                }
                Driver driver = new Driver(veh);
                VehicleDriver clazz = new VehicleDriver(model, veh, ++vehicleDriverCount);
                /*
                if (veh.l < 6)
               		clazz.addStochasticDriverParameter("fSpeed", VehicleDriver.distribution.GAUSSIAN, 123.7/120, 12.0/120);
                else {
                   clazz.addStochasticVehicleParameter("vMax", VehicleDriver.distribution.GAUSSIAN, 85, 2.5);
                   driver.a = 0.4;
                }
                */
                
                if (veh.l < 6)
                	clazz.addStochasticDriverParameter("fSpeed", VehicleDriver.distribution.GAUSSIAN, 123.7/120, 12.0/120);
                else {
                    clazz.addStochasticVehicleParameter("vMax", VehicleDriver.distribution.GAUSSIAN, 85, 2.5);
                    driver.a = 0.4;
                }
                // Additional parameter(s) for evacuation modeling
                driver.activationLevel = Double.parseDouble(fields[5]);
                driver.transitionTime = Double.parseDouble(fields[6]);
                
                if (driver.activationLevel == 0) {
                	driver.ActLevel = driver.activationLevel;
                    driver.RandomAct = 0;
                }else if ((driver.activationLevel <= 1) && (driver.activationLevel > 0)) {
                	clazz.addStochasticDriverParameter("RandomAct", VehicleDriver.distribution.GAUSSIAN, 0, 0.1);
                }else {
                	throw new Exception("The targeted ActivationLevel input is not a valid value!");
                	//System.out.println("The ActivationLevel input is not a valid value!");
                }
        	} else if (fields[0].equals("VMS")) {
        		for (String location : fields[2].split(",")) {
        			String[] subFields = location.split(" ");
        			if ((null == subFields[0]) || (subFields[0].equals("")))
        				System.err.println("VMS " + fields[1] + " lies on no lane");
        			else {
        			Lane lane = lookupLane(Integer.parseInt(subFields[0]), microNetwork);
        			if (null == lane)
        				throw new Exception("VMS " + fields[1] + " lies on undefined lane " + subFields[0]);
        			model.addController(new VMS(lane, Double.parseDouble(subFields[1]), fields[1]));
        			}
        		}

        	} else
        		throw new Exception("Unknown object in LaneSimulator: \"" + fields[0] + "\"");        	
    	}
		if (null != exportTripPattern)
			tripList.add(exportTripPattern);
        Collections.sort(tripList, new Comparator<ExportTripPattern>() {
			@Override
			public int compare(ExportTripPattern arg0, ExportTripPattern arg1) {
				return arg0.getRoutes().get(0).get(0) - arg1.getRoutes().get(0).get(0);
			}
        });
        int currentNode = Integer.MAX_VALUE;
        ArrayList<Double> routeFlows = new ArrayList<Double>();
        ArrayList<ArrayList<Integer>> routeList = new ArrayList<ArrayList<Integer>>();
        //double flow = 0;
        flowGraph = new TimeScaleFunction();
        flowGraph.insertPair(0, 0);
        double[] fractions = null;
        int endNode = -1;
        for (ExportTripPattern trip : tripList) {
			int nextNode = trip.getRoutes().get(0).get(0);
			// When nextNode changes: create a generator for the current Node
			if (nextNode > currentNode) {
				makeGenerator(routeFlows, currentNode, microNetwork, routeList, flowGraph, fractions);
				routeFlows.clear();
				routeList.clear();
		        flowGraph = new TimeScaleFunction();
		        flowGraph.insertPair(0, 0);
		        endNode = -1;
			}
			currentNode = nextNode;
			int currentEndNode = trip.getRoutes().get(0).get(trip.getRoutes().get(0).size() - 1); 
			if (endNode != currentEndNode) {
				flowGraph = flowGraph.add(trip.flowGraph);
				System.out.println("flowgraph is now " + flowGraph.export());
				endNode = currentEndNode;
			}
			for (int i = 0; i < trip.routeProbabilities.size(); i++) {
				routeList.add(trip.getRoutes().get(i));
				routeFlows.add(trip.getFlow(i).getFactor(0d));
			}
			fractions = trip.getFractions();
		}
        if (Integer.MAX_VALUE != currentNode) // Make the last generator
        	makeGenerator(routeFlows, currentNode, microNetwork, routeList, flowGraph, fractions);

        model.network = microNetwork.toArray(new Lane[0]);
        // Add the tapers to the list of lane objects
        for (Lane lane : model.network)
        	if (lane.taper == lane)
        		laneGraphics.add(new LaneGraphic(lane));
        // Then add the non-tapers
        for (Lane lane : model.network)
        	if (lane.taper != lane)
        		laneGraphics.add(new LaneGraphic(lane));
        ConsistencyCheck.checkPreInit(model);
        System.out.println("model.init()");
        model.init();
        ConsistencyCheck.checkPostInit(model);
        System.out.println(String .format("model created: %d lanes", model.network.length));
        scheduler.enqueueEvent(0d, new Stepper(this));
        ffdList = new ArrayList<FakeFundamentalDiagram>();
        //ffdList.add(new FakeFundamentalDiagram("20\t-180\t180\t-180\t180\t-20\t20\t-20", "[0/0:15/0:20/8:25/50:40/90]", scheduler, 5));
        //ffdList.add(new FakeFundamentalDiagram("200\t200\t1600\t200\t1600\t1700\t200\t1700", "[0/0:15/0:20/8:25/50:40/150]", scheduler, 5));
        //ffdList.add(new FakeFundamentalDiagram("1900\t200\t3300\t200\t3300\t1700\t1900\t1700", "[0/0:15/0:20/8:25/50:40/90]", scheduler, 5));
	}
	
	ArrayList<FakeFundamentalDiagram> ffdList;
	
	private void makeGenerator(ArrayList<Double> routeFlows, int node, ArrayList<Lane> lanes, ArrayList<ArrayList<Integer>> routes, TimeScaleFunction flowGraph, double classProbabilities[]) throws Exception {
		double numberOfTrips = 0;
		for (int i = 0; i < routeFlows.size(); i++)
			numberOfTrips += routeFlows.get(i);
		System.out.print(String.format(Locale.US, "Creating generator at node %d, numberOfTrips %.4f, class probabilities [", node, numberOfTrips));
		for (int i = 0; i < classProbabilities.length; i++)
			System.out.print(String.format("%s%.6f", i > 0 ? ", " : "", classProbabilities[i]));
		System.out.println("] flows and routes [");
		for (int i = 0; i < routes.size(); i++)
			System.out.println(String.format(Locale.US, "r%2d: %10.4f veh/h %s", i,  routeFlows.get(i), routes.get(i)));
		System.out.println("and flow graph {" + flowGraph.export() + "}\n");
		int routeCount = routeFlows.size();
		double probabilities[] = new double[routeCount];
		Route[] routePaths = new Route[routeCount];
		Lane laneOrigin = lookupOrigin(node, lanes);
		if (null == laneOrigin) {
			System.err.println("LookupOrigin failed for node " + node);
			return;
		}
    	for (int index = 0; index < routeCount; index++) {
    		ArrayList<Integer> routePath = routes.get(index);
    		int[] routeNodes = new int[routePath.size() - 1];
    		int fillPoint = 0;
    		for (int i = 1; i < routePath.size(); i++)
    			routeNodes[fillPoint++] = routePath.get(i);
    		routePaths[index] = new Route(routeNodes);
    		probabilities[index] = routeFlows.get(index) / numberOfTrips;
    	}
    	int mergeCount = 0;
    	Lane priorityLane = null;
    	for (Lane lane : lanes)
    		if (lane.down == laneOrigin) {
    			mergeCount++;
    			priorityLane = lane;
    		}
    	System.out.println("mergeCount is " + mergeCount);
    	if (mergeCount > 1) {
    		System.err.println ("Don't know how to create an N-merge for N=" + mergeCount + " at lane " + laneOrigin.id);
    		return;
    	}
    	if (mergeCount == 1) {
    		double x[] = new double[2];
    		x[0] = laneOrigin.x[0] - 50;
    		x[1] = laneOrigin.x[0];
    		double y[] = new double[2];
    		y[0] = laneOrigin.y[0] - 50;
    		y[1] = laneOrigin.y[0];
    		Lane hiddenLane = new Lane(model, x, y, ++highestLaneID);
    		hiddenLane.vLim = 50;
			if (!laneOrigin.ups.isEmpty())
				laneOrigin.ups.add(hiddenLane);
			else if (laneOrigin.up != null)  {
				laneOrigin.ups.add(laneOrigin.up);
				laneOrigin.ups.add(hiddenLane);
				laneOrigin.up = null;
			}
			else
				laneOrigin.up = hiddenLane;
			hiddenLane.down = laneOrigin;
    		Conflict.createMerge(laneOrigin, priorityLane, hiddenLane);
    		laneOrigin = hiddenLane;
    		hiddenLane.setVisible(false);
    		lanes.add(hiddenLane);
    	}
		Generator generator = new Generator(laneOrigin, Generator.distribution.EXPONENTIAL);
		generator.routes = routePaths;
		generator.setRouteProbabilities(probabilities);
		generator.setClassProbabilities(classProbabilities);
		generator.setDemand(flowGraph);
		// TODO numberOfTrips must be converted to flow [veh/h]
		// STUB 
		//numberOfTrips = 1000;
		//generator.setDemand(numberOfTrips);
	}
	
	static Lane lookupLane(int id, ArrayList<Lane> lanes) {
		for (Lane lane : lanes)
			if (lane.id() == id)
				return lane;
		return null;
	}
	
	static Lane lookupOrigin(int origin, ArrayList<Lane> lanes) {
		for (Lane lane : lanes)
			if (lane.origin == origin)
				return lane;
		return null;
	}
	
	class LaneData {
		int id;
		double[] x;
		double[] y;
		int fromNode;
		int toNode;
	}
	
	class LaneGraphic {
		private Lane lane;
		private poly pol;
		boolean drawLines;
		
		public LaneGraphic(Lane lane) {
			this.lane = lane;
			pol = new poly(lane);
			if (lane.taper == lane)
				drawLines = false;
			else
				drawLines = true;
		}
		
		public void paint(GraphicsPanel graphicsPanel) {
			if (! lane.isVisible())
				return;
			graphicsPanel.setColor(Color.GRAY);
			graphicsPanel.setStroke(GraphicsPanel.SOLID, 1, 0);
			
            // up line
            if (lane.up==null && lane.generator==null && !lane.isMerge())
            	graphicsPanel.drawPolyLine(pol.upEdge(), false);

            // down line
            if (lane.down==null && lane.destination==0 && !lane.isSplit())
            	graphicsPanel.drawPolyLine(pol.downEdge(), false);

            // left line
            Point2D.Double[] edge;
            if (lane.left==null || (!lane.goLeft && !lane.left.goRight)) {
                // continuous normal
                edge = pol.leftEdge();
                graphicsPanel.setStroke(1, 1f, (float) edge[0].x);
            } else if (lane.goLeft && lane.left.goRight) {
                // dashed normal
                edge = pol.leftEdge();
                graphicsPanel.setStroke(2, 1f, (float) edge[0].x);
            } else if (!lane.goLeft && lane.left.goRight) {
                // continuous near
                edge = pol.leftNearEdge();
                graphicsPanel.setStroke(1, 1f, (float) edge[0].x);
            } else {
                // dashed near
                edge = pol.leftNearEdge();
                graphicsPanel.setStroke(2, 1f, (float) edge[0].x);
            }
            graphicsPanel.drawPolyLine(edge, false);

            // right line
            boolean drawRight = false;
            if (lane.right==null || (!lane.goRight && !lane.right.goLeft)) {
                // continuous normal
                // also right if both not allowed, may be non-adjacent but 
                // linked lanes for synchronization
                edge = pol.rightEdge();
                graphicsPanel.setStroke(1, 1f, (float) edge[0].x);
                drawRight = true;
            } else if (lane.right.goLeft && !lane.goRight) {
                // continuous near
                edge = pol.rightNearEdge();
                graphicsPanel.setStroke(1, 1f, (float) edge[0].x);
                drawRight = true;
            } else if (!lane.right.goLeft && lane.goRight) {
                // dashed near
                edge = pol.rightNearEdge();
                graphicsPanel.setStroke(2, 1f, (float) edge[0].x);
                drawRight = true;
            }
            if (drawRight) {
                graphicsPanel.drawPolyLine(edge, false);
            } 		
        }
	}

    private class poly {
        
        /** x coordinates of entire area. */
        private double[] xArea;
        
        /** y coordinates of entire area. */
        private double[] yArea;
        
        /** x coordinates of left side. */
        private double[] xLeftEdge;
        
        /** y coordinates of left side. */
        private double[] yLeftEdge;
        
        /** x coordinates of right side. */
        private double[] xRightEdge;
        
        /** y coordinates of right side. */
        private double[] yRightEdge;
        
        /** x coordinates of left side in case of dual line markings. */
        private double[] xNearLeftEdge;
        
        /** y coordinates of left side in case of dual line markings. */
        private double[] yNearLeftEdge;
        
        /** x coordinates of right side in case of dual line markings. */
        private double[] xNearRightEdge;
        
        /** y coordinates of right side in case of dual line markings. */
        private double[] yNearRightEdge;
        
        /** Number of points in lane position vector. */
        private int n;
        
        /**
         * Constructor that derives all needed information.
         * @param lane Lane object.
         */
        private poly(Lane lane) {
            // check whether the lane is a taper
            boolean mergeTaper = false;
            boolean divergeTaper = false;
            if (lane.taper==lane && lane.down==null) {
                mergeTaper = true;
            } else if (lane.taper==lane && lane.up==null) {
                divergeTaper = true;
            }
            // set width numbers
            double w = 1.75; // half lane width
            double near = 0.375; // half distance between dual lane marking
            double f = 1; // factor that reduces width along tapers
            
            // first point
            n = lane.x.length;
            xLeftEdge = new double[n];
            yLeftEdge = new double[n];
            xRightEdge = new double[n];
            yRightEdge = new double[n];
            xNearLeftEdge = new double[n];
            yNearLeftEdge = new double[n];
            xNearRightEdge = new double[n];
            yNearRightEdge = new double[n];
            java.awt.geom.Point2D.Double[] start = new java.awt.geom.Point2D.Double[0];
            java.awt.geom.Point2D.Double[] startNear = new java.awt.geom.Point2D.Double[0];
            if (divergeTaper) {
                f = -1;  
            }
            if (lane.up!=null) {
                int nUp = lane.up.x.length;
                start = intersect(w*f, w,
                        lane.up.x[nUp-2], lane.x[0], lane.x[1],
                        lane.up.y[nUp-2], lane.y[0], lane.y[1]);
                startNear = intersect(w*f-near, w-near,
                        lane.up.x[nUp-2], lane.x[0], lane.x[1],
                        lane.up.y[nUp-2], lane.y[0], lane.y[1]);
            } else {
                start = intersect(w*f, w,
                        lane.x[0] - (lane.x[1]-lane.x[0]), lane.x[0], lane.x[1],
                        lane.y[0] - (lane.y[1]-lane.y[0]), lane.y[0], lane.y[1]);
                startNear = intersect(w*f-near, w-near,
                        lane.x[0] - (lane.x[1]-lane.x[0]), lane.x[0], lane.x[1],
                        lane.y[0] - (lane.y[1]-lane.y[0]), lane.y[0], lane.y[1]);
            }
            xLeftEdge[0] = start[1].x;
            yLeftEdge[0] = start[1].y;
            xRightEdge[0] = start[0].x;
            yRightEdge[0] = start[0].y;
            xNearLeftEdge[0] = startNear[1].x;
            yNearLeftEdge[0] = startNear[1].y;
            xNearRightEdge[0] = startNear[0].x;
            yNearRightEdge[0] = startNear[0].y;
            
            // middle points
            java.awt.geom.Point2D.Double[] point = new java.awt.geom.Point2D.Double[0];
            java.awt.geom.Point2D.Double[] pointNear = new java.awt.geom.Point2D.Double[0];
            f = 1; // default for no taper
            double dx = lane.x[1]-lane.x[0];
            double dy = lane.y[1]-lane.y[0];
            double len = Math.sqrt(dx*dx + dy*dy); // cumulative length for tapers
            for (int i=1; i<n-1; i++) {
                if (mergeTaper) {
                    // reducing width
                    f = 1 - 2*len/lane.l;
                    dx = lane.x[i+1]-lane.x[i];
                    dy = lane.y[i+1]-lane.y[i];
                    len = len + Math.sqrt(dx*dx + dy*dy);
                } else if (divergeTaper) {
                    // increasing width
                    f = -1 + 2*len/lane.l;
                    dx = lane.x[i+1]-lane.x[i];
                    dy = lane.y[i+1]-lane.y[i];
                    len = len + Math.sqrt(dx*dx + dy*dy);
                }
                point = intersect(w*f, w,
                        lane.x[i-1], lane.x[i], lane.x[i+1],
                        lane.y[i-1], lane.y[i], lane.y[i+1]);
                pointNear = intersect(w*f-near, w-near,
                        lane.x[i-1], lane.x[i], lane.x[i+1],
                        lane.y[i-1], lane.y[i], lane.y[i+1]);
                xLeftEdge[i] = point[1].x;
                yLeftEdge[i] = point[1].y;
                xRightEdge[i] = point[0].x;
                yRightEdge[i] = point[0].y;
                xNearLeftEdge[i] = pointNear[1].x;
                yNearLeftEdge[i] = pointNear[1].y;
                xNearRightEdge[i] = pointNear[0].x;
                yNearRightEdge[i] = pointNear[0].y;
            }
            
            // last point
            java.awt.geom.Point2D.Double[] end = new java.awt.geom.Point2D.Double[0];
            java.awt.geom.Point2D.Double[] endNear = new java.awt.geom.Point2D.Double[0];
            if (mergeTaper) {
                f = -1;
            } else {
                f = 1;
            }
            if (lane.down!=null) {
                end = intersect(w*f, w,
                        lane.x[n-2], lane.x[n-1], lane.down.x[1],
                        lane.y[n-2], lane.y[n-1], lane.down.y[1]);
                endNear = intersect(w*f-near, w-near,
                        lane.x[n-2], lane.x[n-1], lane.down.x[1],
                        lane.y[n-2], lane.y[n-1], lane.down.y[1]);
            } else {
                end = intersect(w*f, w,
                        lane.x[n-2], lane.x[n-1], lane.x[n-1]+(lane.x[n-1]-lane.x[n-2]),
                        lane.y[n-2], lane.y[n-1], lane.y[n-1]+(lane.y[n-1]-lane.y[n-2]));
                endNear = intersect(w*f-near, w-near,
                        lane.x[n-2], lane.x[n-1], lane.x[n-1]+(lane.x[n-1]-lane.x[n-2]),
                        lane.y[n-2], lane.y[n-1], lane.y[n-1]+(lane.y[n-1]-lane.y[n-2]));
            }
            xLeftEdge[n-1] = end[1].x;
            yLeftEdge[n-1] = end[1].y;
            xRightEdge[n-1] = end[0].x;
            yRightEdge[n-1] = end[0].y;
            xNearLeftEdge[n-1] = endNear[1].x;
            yNearLeftEdge[n-1] = endNear[1].y;
            xNearRightEdge[n-1] = endNear[0].x;
            yNearRightEdge[n-1] = endNear[0].y;
            
            // combine area from edges
            xArea = new double[n*2+1];
            yArea = new double[n*2+1];
            for (int i=0; i<n; i++) {
                xArea[i] = xRightEdge[i];
                yArea[i] = yRightEdge[i];
            }
            for (int i=0; i<n; i++) {
                xArea[i+n] = xLeftEdge[n-i-1];
                yArea[i+n] = yLeftEdge[n-i-1];
            }
            xArea[n*2] = xRightEdge[0];
            yArea[n*2] = yRightEdge[0];
        }
        
        /**
         * Method that finds the intersection of the left and right side of 
         * two lane sections.
         * @param wLeft Width towards the left [m].
         * @param wRight Width towards the right [m].
         * @param x1 1st x coordinate of upstream section [m].
         * @param x2 x coordinate of common point [m].
         * @param x3 2nd coordinate of downstream section [m].
         * @param y1 1st y coordinate of upstream section [m].
         * @param y2 y coordinate of common point [m].
         * @param y3 2nd y coordinate of downstream section [m].
         * @return Two points at the left and right side intersections.
         */
        private java.awt.geom.Point2D.Double[] intersect(
                double wLeft, double wRight,
                double x1, double x2, double x3,  
                double y1, double y2, double y3) {
            
            // get headings
            double dx1 = x2-x1;
            double dy1 = y2-y1;
            double dx2 = x3-x2;
            double dy2 = y3-y2;
            
            // normalization factors
            double f1 = 1/Math.sqrt(dx1*dx1 + dy1*dy1);
            double f2 = 1/Math.sqrt(dx2*dx2 + dy2*dy2);
            
            // get coordinates of left adjacent lanes
            double xLeft1  = x1+dy1*f1*wLeft;
            double xLeft2a = x2+dy1*f1*wLeft;
            double xLeft2b = x2+dy2*f2*wLeft;
            double xLeft3  = x3+dy2*f2*wLeft;
            double yLeft1  = y1-dx1*f1*wLeft;
            double yLeft2a = y2-dx1*f1*wLeft;
            double yLeft2b = y2-dx2*f2*wLeft;
            double yLeft3  = y3-dx2*f2*wLeft;
            
            // get coordinates of right adjacent lanes
            double xRight1  = x1-dy1*f1*wRight;
            double xRight2a = x2-dy1*f1*wRight;
            double xRight2b = x2-dy2*f2*wRight;
            double xRight3  = x3-dy2*f2*wRight;
            double yRight1  = y1+dx1*f1*wRight;
            double yRight2a = y2+dx1*f1*wRight;
            double yRight2b = y2+dx2*f2*wRight;
            double yRight3  = y3+dx2*f2*wRight;
            
            // intersect left lines
            double a1 = (yLeft2a-yLeft1)/(xLeft2a-xLeft1);
            double b1 = yLeft1 - xLeft1*a1;
            double a2 = (yLeft3-yLeft2b)/(xLeft3-xLeft2b);
            double b2 = yLeft2b - xLeft2b*a2;
            double xLeft;
            double yLeft;
            if (Math.abs(a1-a2)<0.001 || (Double.isInfinite(a1) && Double.isInfinite(a2))) {
                xLeft = xLeft2a;
                yLeft = yLeft2a;
            } else if (Double.isInfinite(a1)) {
                xLeft = xLeft1;
                yLeft = a2*xLeft+b2;
            } else if (Double.isInfinite(a2)) {
                xLeft = xLeft3;
                yLeft = a1*xLeft+b1;
            } else {
                xLeft = -(b1-b2)/(a1-a2);
                yLeft = a1*xLeft+b1;
            }
            
            // intersect right lines
            a1 = (yRight2a-yRight1)/(xRight2a-xRight1);
            b1 = yRight1 - xRight1*a1;
            a2 = (yRight3-yRight2b)/(xRight3-xRight2b);
            b2 = yRight2b - xRight2b*a2;
            double xRight;
            double yRight;
            if (Math.abs(a1-a2)<0.001 || (Double.isInfinite(a1) && Double.isInfinite(a2))) {
                xRight = xRight2a;
                yRight = yRight2a;
            } else if (Double.isInfinite(a1)) {
                xRight = xRight1;
                yRight = a2*xRight+b2;
            } else if (Double.isInfinite(a2)) {
                xRight = xRight3;
                yRight = a1*xRight+b1;
            } else {
                xRight = -(b1-b2)/(a1-a2);
                yRight = a1*xRight+b1;
            }
            
            // gather output
            java.awt.geom.Point2D.Double[] out = new java.awt.geom.Point2D.Double[2];
            out[0] = new java.awt.geom.Point2D.Double(xLeft, yLeft);
            out[1] = new java.awt.geom.Point2D.Double(xRight, yRight);
            return out;
        }
        
        /**
         * Returns a polygon of the lane area.
         * @return Polygon of lane area.
         */
        /*
        private java.awt.Polygon area() {
            int[] x = new int[2*n+1];
            int[] y = new int[2*n+1];
            java.awt.Point point = new java.awt.Point();
            for (int i=0; i<xArea.length; i++) {
                point = getPoint(xArea[i], yArea[i]);
                x[i] = point.x;
                y[i] = point.y;
            }
            return new java.awt.Polygon(x, y, n*2);
        }
        */
        /**
         * Returns a <tt>polyLine</tt> of the left side.
         * @return Polyline of the left side.
         */
        private Point2D.Double[] leftEdge() {
            double[] x = new double[n];
            double[] y = new double[n];
            for (int i=0; i<xLeftEdge.length; i++) {
                x[i] = xLeftEdge[i];
                y[i] = yLeftEdge[i];
            }
            return polyLine(x, y);
        }
        
        /**
         * Returns a <tt>polyLine</tt> of the right side.
         * @return Polyline of the right side.
         */
        private Point2D.Double[] rightEdge() {
            double[] x = new double[n];
            double[] y = new double[n];
            for (int i=0; i<xRightEdge.length; i++) {
                x[i] = xRightEdge[i];
                y[i] = yRightEdge[i];
            }
            return polyLine(x, y);
        }
        
        /**
         * Returns a <tt>polyLine</tt> of the left side in case of dual line markings.
         * @return Polyline of the left side.
         */
        private Point2D.Double[] leftNearEdge() {
            double[] x = new double[n];
            double[] y = new double[n];
            for (int i=0; i<xNearLeftEdge.length; i++) {
                x[i] = xNearLeftEdge[i];
                y[i] = yNearLeftEdge[i];
            }
            return polyLine(x, y);
        }
        
        /**
         * Returns a <tt>polyLine</tt> of the right side in case of dual line markings.
         * @return Polyline of the right side.
         */
        private Point2D.Double[] rightNearEdge() {
            double[] x = new double[n];
            double[] y = new double[n];
            for (int i=0; i<xNearRightEdge.length; i++) {
                x[i] = xNearRightEdge[i];
                y[i] = yNearRightEdge[i];
            }
            return polyLine(x, y);
        }
        
        /**
         * Returns a <tt>polyLine</tt> of the upstream side.
         * @return Polyline of the upstream side.
         */
        private Point2D.Double[] upEdge() {
            double[] x = new double[2];
            double[] y = new double[2];
            x[0] = xLeftEdge[0];
            y[0] = yLeftEdge[0];
            x[1] = xRightEdge[0];
            y[1] = yRightEdge[0];
            return polyLine(x, y);
        }
        
        /**
         * Returns a <tt>polyLine</tt> of the downstream side.
         * @return Polyline of the downstream side.
         */
        private Point2D.Double[] downEdge() {
            double[] x = new double[2];
            double[] y = new double[2];
            x[0] = xLeftEdge[n-1];
            y[0] = yLeftEdge[n-1];
            x[1] = xRightEdge[n-1];
            y[1] = yRightEdge[n-1];
            return polyLine(x, y);
        }
    }
    
    static Point2D.Double[] polyLine(double[] x, double[] y) {
    	Point2D.Double[] result;
    	if (x.length != y.length)
    		throw new Error("X and Y arrays differ in length");
    	result = new Point2D.Double[x.length];
    	for (int i = 0; i < x.length; i++)
    		result[i] = new Point2D.Double(x[i], y[i]);
    	return result;
    }
    
    private class VehicleGraphic {
        /** The concerned vehicle object. */
        private Movable vehicle;
        
        /**
         * Constructor that sets the vehicle.
         * @param vehicle Concerned vehicle.
         */
        public VehicleGraphic(Vehicle vehicle) {
            this.vehicle = vehicle;
        }
        
        public VehicleGraphic(LCVehicle lcv) {
        	this.vehicle = lcv;
        }
        
        /**
         * Paints the vehicle on a {@link GraphicsPanel}.
         * @param graphicsPanel {@link GraphicsPanel}; device to paint onto
         * @param showDownStream Boolean; if true, a line is drawn to the down
         * stream leader of the vehicle
         * @param showUpStream Boolean; if true, a line is drawn to the up
         * steam follower of the vehicle
         */
        public void paint(GraphicsPanel graphicsPanel, boolean showDownStream, boolean showUpStream) {
        	if (vehicle instanceof Vehicle)
        		((Vehicle) vehicle).paint(scheduler.getSimulatedTime(), graphicsPanel);
        	else if (vehicle instanceof LCVehicle)
        		((LCVehicle) vehicle).paint(scheduler.getSimulatedTime(), graphicsPanel);
            //Point2D.Double[] outline = vehicle.outline(scheduler.getSimulatedTime());
            //graphicsPanel.drawPolygon(outline);
            if (showDownStream) {
            	if (null == vehicle.global) {
            		System.err.println("LaneSimulator.paint: movable global is NULL");
            		return;
            	}
            	int[] directions = { Movable.LEFT_DOWN, Movable.DOWN, Movable.RIGHT_DOWN };
            	Point2D.Double[] line = new Point2D.Double[2];
            	line[0] = vehicle.global;
            	for (int direction : directions) {
            		Movable other = vehicle.getNeighbor(direction);
            		if (null != other) {
            			graphicsPanel.setColor (
            					Movable.LEFT_DOWN == direction ? new Color(255,0,0)
            					: Movable.DOWN == direction ? new Color(0, 255, 0)
            					: new Color(0, 0, 255));
                        java.awt.Point.Double h = other.heading;
                        if (null == other.global)
                        	System.err.println("neighbor " + other.toString() + " (" + Movable.directionToString(direction) + " of " + vehicle.toString() + " has bad global");
                        else {
	                    	line[1] = new Point2D.Double(other.global.x - h.x * other.l, 
	                                other.global.y - h.y * other.l);
	                        graphicsPanel.drawPolyLine(line, false);
                        }
            		}
            	}
            }
            if (showUpStream) {
            	int[] directions = { Movable.LEFT_UP, Movable.UP, Movable.RIGHT_UP };
            	Point2D.Double[] line = new Point2D.Double[2];
            	line[0] = vehicle.global;
            	for (int direction : directions) {
            		Movable other = vehicle.getNeighbor(direction);
            		if (null != other) {
            			graphicsPanel.setColor (
            					Movable.LEFT_UP == direction ? new Color(0, 255,255)
            					: Movable.UP == direction ? new Color(255, 0, 255)
            					: new Color(0, 255, 255));
                        java.awt.Point.Double h = other.heading;
                        if (null == other.global)
                        	System.err.println("neighbor " + other.toString() + " (" + Movable.directionToString(direction) + " of " + vehicle.toString() + " has bad global");
                        else {
	                    	line[1] = new Point2D.Double(other.global.x - h.x * other.l, 
	                                other.global.y - h.y * other.l);
	                        graphicsPanel.drawPolyLine(line, false);
                        }
            		}
            	}
            }
        }
    } 

	private Point2D.Double mouseDown = null;
	private ObjectInspector objectInspector = null;
	Vehicle selectedVehicle = null;
	
	double selectVehicle(GraphicsPanel graphicsPanel, Point2D.Double p) {
    	//System.out.println(String.format("Searching vehicle near %f,%f (rev %f,%f", p.x, p.y, graphicsPanel.reverseTranslate(p).getX(), graphicsPanel.reverseTranslate(p).getY()));
        final int maxDistance = 10;	// pixels
        double bestDistance = Double.MAX_VALUE;
        Vehicle prevSelectedVehicle = selectedVehicle;

        for (Vehicle vehicle : model.getVehicles()) {
        	Point2D.Double[] vehiclePolygon = vehicle.outline(scheduler.getSimulatedTime());
        	for (Point2D.Double pp : vehiclePolygon) {
        		Point2D.Double translated = graphicsPanel.translate(pp);
        		pp.x = translated.x;
        		pp.y = translated.y;
        	}
        	double distance = Planar.distancePolygonToPoint(vehiclePolygon, p);
    		if ((distance < maxDistance) && (distance < bestDistance)) {
    			selectedVehicle = vehicle;
                bestDistance = 0;
    		}
        }
        if (null == selectedVehicle)
        	System.out.println(String.format("No vehicle found near %f,%f", p.x, p.y));
        // Repaint is only required if selected nodes are painted different from non-selected nodes
        // Actually, GraphicsPanel ALWAYS generates a repaint so these two lines are redundant
        if (prevSelectedVehicle != selectedVehicle)
        	graphicsPanel.repaint();
        return bestDistance;
	}

	@Override
	public void mousePressed(GraphicsPanel graphicsPanel, MouseEvent evt) {
        if (null != mouseDown){ // Ignore mouse presses that occur
            return;            	//  when user is already drawing a curve.
        }//    (This can happen if the user presses two mouse buttons at the same time.)

    	if (null != objectInspector) {
    		objectInspector.dispose();
    		objectInspector = null;
    	}
        mouseDown = new Point2D.Double(evt.getX(), evt.getY());
        if (selectVehicle(graphicsPanel, mouseDown) != Double.MAX_VALUE)
        	objectInspector = new ObjectInspector(selectedVehicle, this);
	}

	@Override
	public void mouseDragged(GraphicsPanel graphicsPanel, MouseEvent evt) {
        if (null == mouseDown)
            return;			// processing a gesture

        Point2D.Double position = new Point2D.Double(evt.getX(), evt.getY());

        ((GraphicsPanel) evt.getSource()).addPan(position.x - mouseDown.x, position.y - mouseDown.y);
        // Update "current" position
        mouseDown = position;
	}

	@Override
	public void mouseReleased(GraphicsPanel graphicsPanel, MouseEvent evt) {
        mouseDown = null;
	}

	@Override
	public void mouseMoved(GraphicsPanel graphicsPanel, MouseEvent evt) {
        Point2D.Double reversePosition = graphicsPanel.reverseTranslate(new Point2D.Double(evt.getX(), evt.getY()));
		Main.mainFrame.setStatus(-1, "Mouse pointer at %.2f,%.2f", reversePosition.x, reversePosition.y);
	}

	@Override
	public void repaintGraph(GraphicsPanel graphicsPanel) {
		for (Controller controller : model.controllers)
			if (controller instanceof SimulatedObject)
				((SimulatedObject) controller).paint(model.t, graphicsPanel);
		for (LaneGraphic lg : laneGraphics)
			lg.paint(graphicsPanel);
		boolean showLeader = Main.mainFrame.checkBoxShowLeader.isSelected();
		boolean showFollower = Main.mainFrame.checkBoxShowFollower.isSelected();
		for (Vehicle vehicle : model.getVehicles())
			new VehicleGraphic(vehicle).paint(graphicsPanel, showLeader, showFollower);
		if (Main.mainFrame.checkBoxShowLCVehicles.isSelected())
			for (LCVehicle lcv : model.getLcVehicles())
				new VehicleGraphic(lcv).paint(graphicsPanel, showLeader, showFollower);
		for (Lane l : model.network)
			for (RSU rsu : l.getRSUs_r())
				if (rsu instanceof Conflict.conflictRSU)
					((Conflict.conflictRSU)rsu).drawLineToUpStreamVehicle(graphicsPanel);
		for (FakeFundamentalDiagram ffd : ffdList)
			ffd.paint(scheduler.getSimulatedTime(), graphicsPanel);
	}

	@Override
	public void setModified() {
		// NO OP; cannot happen
	}

	@Override
	public void preStep() {
		// nothing to do
	}

	@Override
	public void postStep() {
		// nothing to do
	}

	@Override
	public Scheduler getScheduler() {
		return scheduler;
	}

	@Override
	public ArrayList<SimulatedObject> SampleMovables() {
		ArrayList<SimulatedObject> result = new ArrayList<SimulatedObject>();
		for (Vehicle v : model.vehicles)
			result.add(v);
		return result;
	}

	@Override
	public void ShutDown() {
	}

}

class Stepper implements Step {
	final private LaneSimulator laneSimulator;
	
	public Stepper (LaneSimulator laneSimulator) {
		this.laneSimulator = laneSimulator;
	}
	
	@Override
	public Scheduler.SchedulerState step(double now) {
    	//System.out.println("step entered");
    	Model model = laneSimulator.getModel();
    	if (now >= model.period)
    		return Scheduler.SchedulerState.EndTimeReached;
    	while (model.t < now) {
    		//System.out.println("step calling run(1)");
    		try {
    			//System.out.format(Main.locale, "Time is %.3f\r\n", now);
    			model.run(1);
    		} catch (RuntimeException e) {
    			WED.showProblem(WED.ENVIRONMENTERROR, "Error in LaneSimulator:\r\n%s", WED.exeptionStackTraceToString(e));
    			return Scheduler.SchedulerState.SimulatorError;
    		}
    	}
    	// re-schedule myself
    	laneSimulator.getScheduler().enqueueEvent(model.t + model.dt, this);
    	//System.out.println("step returning true");
		return null;
	}
	
}

class ExportTripPattern {
	//final Double flow;
	final TimeScaleFunction flowGraph;
	final ArrayList<ArrayList<Integer>> routes = new ArrayList<ArrayList<Integer>>();
	final ArrayList<Double> routeProbabilities = new ArrayList<Double>();
	final double[] fractions;
	final Random randomGenerator = new Random(54321);
	
	private void checkFractions () throws Exception {
		double sum = 0;
		for (int i = 0; i < fractions.length; i++)
			sum += fractions[i];
		if (Math.abs(sum - 1.0) > 0.001)
			throw new Exception("vehicleTypeFractions add up to " + sum + " which is not (nearly) equal to one");		
	}
	
	/**
	 * Create an exportTripPattern.
	 * @param flow Double; flow on the new ExportTripPattern
	 * @param vehicleTypeFractions double[]; list of fractions corresponding to the defined {@link TrafficClass TrafficClasses}
	 * @throws Exception if the routeProbabilities do not add up to (approximately) 1.0
	 */
	public ExportTripPattern(Double flow, double[] vehicleTypeFractions) throws Exception {
		//this.flow = flow;
		flowGraph = new TimeScaleFunction();
		flowGraph.insertPair(0, flow);
		this.fractions = vehicleTypeFractions;
	}
	
	/**
	 * Create an ExportTripPattern.
	 * @param flowGraph TimeScaleFunction; the flow values at a set of times
	 * @param vehicleTypeFractions double[]; list of fractions corresponding to the defined {@link TrafficClass TrafficClasses}
	 * @throws Exception if the routeProbabilities do not add up to (approximately) 1.0
	 */
	public ExportTripPattern(TimeScaleFunction flowGraph, double[] vehicleTypeFractions) throws Exception {
		this.flowGraph = new TimeScaleFunction(flowGraph);
		this.fractions = vehicleTypeFractions;
		checkFractions();
	}
	
	public void addRoute(ArrayList<Integer> route, double probability) {
		if (routes.size() != 0)
			if (routes.get(0).get(0) != route.get(0))
				throw new Error("All routes in ExportTripPattern must start at the same node");
		routes.add(route);
		routeProbabilities.add(probability);
	}

	public TimeScaleFunction getFlow(int routeIndex) {
		if ((routeIndex < 0) || (routeIndex >= routeProbabilities.size()))
			throw new Error("no routes defined");
		return new TimeScaleFunction(flowGraph, routeProbabilities.get(routeIndex));
	}

	public double[] getFractions() {
		return fractions;
	}
	
	public ArrayList<ArrayList<Integer>> getRoutes() {
		return routes;
	}
	
}
