package nl.tudelft.otsim.Simulators.LaneSimulator;

import java.awt.Color;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import nl.tudelft.otsim.Events.Scheduler;
import nl.tudelft.otsim.Events.Step;
import nl.tudelft.otsim.GUI.GraphicsPanel;
import nl.tudelft.otsim.GUI.Main;
import nl.tudelft.otsim.GUI.ObjectInspector;
import nl.tudelft.otsim.GUI.WED;
import nl.tudelft.otsim.Simulators.SimulatedObject;
import nl.tudelft.otsim.Simulators.SimulatedTrafficLightController;
import nl.tudelft.otsim.Simulators.Simulator;
import nl.tudelft.otsim.SpatialTools.Planar;
import nl.tudelft.otsim.TrafficDemand.ExportTripPattern;

public class LaneSimulator extends Simulator {
	
	private final Model jmodel = new Model();
	private ArrayList<LaneGraphic> laneGraphics = new ArrayList<LaneGraphic>();
	private final Scheduler scheduler;
	/** Type of this Simulator */
	public static final String simulatorType = "Lane simulator";

	public final Model getJModel() {
		return jmodel;
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
		// STUB set reasonable defaults
		jmodel.period = 1860;
        jmodel.dt = .2;
        jmodel.debug = true;
        jmodel.setSeed(1);
        jmodel.settings.putBoolean("storeDetectorData", true);
        ArrayList<Lane> microNetwork = new ArrayList<Lane>(); 
    	ArrayList<ExportTripPattern> tripList = new ArrayList<ExportTripPattern>(); 

    	// Two passes; this is pass 1; extract the lane descriptions
        for(String line : definition.split("\n")) {
    		System.out.println("description: " + line);
        	String[] fields = line.split("\t");
        	
        	if (null == fields[0])
        		continue;
        	if (fields[0].equals("Lane")) {	
        		int id = Integer.parseInt(fields[2]);
        		if (id == 34)
        			System.out.println("lane34");
        		int numberOfPoints = (fields.length - 4);
        		double[] x = new double[numberOfPoints];
        		double[] y = new double[numberOfPoints];
        		for (int i = 0; i < numberOfPoints; i++) {
        			String subField = fields[i + 4];
        			String[] subFields = subField.split(",");
        			if (subFields.length != 2)
        				throw new Error ("Bad number of subFields in " + subField);
        			x[i] = Double.parseDouble(subFields[0]);
        			y[i] = Double.parseDouble(subFields[1]);
        		}
        		// Add it to the network
        		System.out.println(String.format("microNetwork.add(new jLane(jmodel, %s, %s, %d", x.toString(), y.toString(), id));
        		Lane newLane = new Lane(jmodel, x, y, id);
        		// set origin and destination as default to -999
        		newLane.destination = -999;
        		newLane.origin = -999;        		
        		microNetwork.add(newLane);
        	}
        }
    	double numberOfTripsPath = 0;
    	double numberOfTrips = 0;
    	// pass 2; extract everything else
        for(String line : definition.split("\n")) {
    		System.out.println("description: " + line);
        	String[] fields = line.split("\t");
        	
        	if (null == fields[0])
        		continue;
        	else if (fields[0].equals("Lane"))
        		continue;
        	else if (fields[0].equals("LaneData")) {	
				int id = Integer.parseInt(fields[2]);
				Lane lane = lookupLane(id, microNetwork);
            	int i = 3;
        		while (i < fields.length){
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
        			}
        			if (fields[i].equals("down:"))  {
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
        			}
        			if (fields[i].equals("crossingYieldTo:"))  {
        				int laneCrossID  = Integer.parseInt(fields[i+1]);
        				Lane crossLane = lookupLane(laneCrossID, microNetwork);
        				Conflict.createCrossing(lane, crossLane, true);
        			}
        			if (fields[i].equals("mergingYieldTo:"))  {
        				int laneMergeID  = Integer.parseInt(fields[i+1]);
        				Lane mergeLane = lookupLane(laneMergeID, microNetwork);
        				Conflict.createMerge(lane, mergeLane);
        			}
        			if (fields[i].equals("right:"))  {
        				int right = Integer.parseInt(fields[i+1]);
        				Lane rightLane = lookupLane(right, microNetwork);
        				lane.right = rightLane;
        			}
        			if (fields[i].equals("left:"))  {
        				int left = Integer.parseInt(fields[i+1]);
        				Lane leftLane = lookupLane(left, microNetwork);
        				lane.left = leftLane;
        			}
        			if (fields[i].equals("goRight:"))  {
        				String goRight =fields[i+1];
        				lane.goRight = Boolean.parseBoolean(goRight);
        			}
        			if (fields[i].equals("goLeft:"))  {
        				String goLeft =fields[i+1];
        				lane.goLeft = Boolean.parseBoolean(goLeft);
        			}
        			if (fields[i].equals("origin:"))  {
        				int origin = Integer.parseInt(fields[i+1]);
        				lane.origin = origin;
        			}
        			if (fields[i].equals("destination:"))  {
        				int destination = Integer.parseInt(fields[i+1]);
        				lane.destination = destination; 
        			}
        			i = i + 2;
        		}
        		// detect conflicts
        		if (lane.downs.size() >= 2)  {
        			int itel = 0;
        			for (Lane down : lane.downs)   {
        				itel++;
        				for (int jj = itel; jj < lane.downs.size(); jj++)
        					Conflict.createSplit(down, lane.downs.get(jj));
        			}
        		}

        	} else if (fields[0].equals("TripPattern")) {
        		int i = 1;
        		while (i < fields.length){
        			if (fields[i].equals("numberOfTrips:"))  {
        				i++;
        				numberOfTrips = Double.parseDouble(fields[i]);
        			}
		            i = i + 1;
        		}
        	} else if (fields[0].equals("TripPatternPath")) {
        		int i = 1;
        		while (i < fields.length){
        			if (fields[i].equals("numberOfTrips:"))  {
        				i++;
        				numberOfTripsPath = Double.parseDouble(fields[i]);
        			}
		            i = i + 1;
        		}
        	} else if (fields[0].equals("Path:")) {
        		int i = 1;
        		int pathNo = Integer.parseInt(fields[i]);
        		i++;
        		ArrayList<Integer> route = new ArrayList<Integer>(); 
        		while (i < fields.length){
        			if (fields[i].equals("nodes:"))
    		            i++;
        			route.add(Integer.parseInt(fields[i]));
		            i++;
        		}
        		ExportTripPattern trip = new ExportTripPattern(route.get(0), route.get(route.size()-1), numberOfTripsPath, numberOfTrips, route);
        		tripList.add(trip);
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
        			jmodel.addController(new TrafficLight(lane, Double.parseDouble(subFields[1]), fields[1], Planar.coordinatesToPoints(fields, 3, fields.length)));
        		}
        	} else if (fields[0].equals("TrafficLightController")) {
        		// This depends on all TrafficLights and Detectors to be created BEFORE the SimulatedTrafficLightController
        		SimulatedTrafficLightController tlc = new SimulatedTrafficLightController(scheduler, fields[4]);
        		for (String tlName : fields[2].split(",")) {
        			for (Controller controller : jmodel.controllers)
        				if (controller instanceof TrafficLight) {
        					TrafficLight tl = (TrafficLight) controller;
        					if (tl.name().equals(tlName))
        	        			tlc.addTrafficLight(tl);
        				}
        		}
        		for (String dName : fields[3].split(",")) {
        			for (Controller controller : jmodel.controllers)
        				if (controller instanceof OccupancyDetector) {
        					OccupancyDetector detector = (OccupancyDetector) controller;
        					if (detector.name().equals(dName))
        	        			tlc.addDetector(detector);
        				}
        		}
        	} else if (fields[0].equals("Detector")) {
        		for (String location : fields[2].split(",")) {
        			String[] subFields = location.split(" ");
        			Lane lane = lookupLane(Integer.parseInt(subFields[0]), microNetwork);
        			if (null == lane)
        				throw new Exception("Detector " + fields[1] + " lies on undefined lane " + subFields[0]);
        			jmodel.addController(new OccupancyDetector(lane, Double.parseDouble(subFields[1]), fields[1], Double.parseDouble(subFields[2]), Planar.coordinatesToPoints(fields, 3, fields.length)));
        		}
        	} else
        		throw new Error("Unknown object in LaneSimulator: \"" + fields[0] + "\"");        	
    	}
        
    	class CompareNodeNumbers implements Comparator<ExportTripPattern> {
			@Override
			public int compare(ExportTripPattern trip1, ExportTripPattern trip2) {
				return trip1.getStartNode() - trip2.getStartNode();
			}
		}
		// sort list by NodeID (nodenumber)
		Collections.sort(tripList, new CompareNodeNumbers());
		
        int prevNode = Integer.MAX_VALUE;
        double totalTripsFrom = 0;
        ArrayList<Double> probabilityList= new ArrayList<Double>();
        ArrayList<ArrayList<Integer>> routeList = new ArrayList<ArrayList<Integer>>();
        double tripsByNode = 0;
		for ( int i=0;  i < tripList.size(); i++)  {
			ExportTripPattern trip = tripList.get(i);
			int startNode = trip.getStartNode();
			//When next node: report data of previous node
			if (startNode > prevNode)  {
				int size = probabilityList.size();
				for (double prob : probabilityList) {
					if (totalTripsFrom>0)
						prob = prob / totalTripsFrom;
				}
				double probabilities[] = new double[size];
				Route[] routeListMin2 = new Route[size];
				Route[] routeListLast = new Route[size];
				Lane laneOrigin = lookupOrigin(prevNode, microNetwork);
		    	
				//routeList[destination] = new jRoute(route);
		    	for (int index = 0; index < size; index++)   {
		    		int[] routeLast = new int[1]; //last node
		    		int last = routeList.get(index).size();
		    		routeLast[0] = routeList.get(index).get(last-1);
			    	int[] route_min2 = new int[last-2]; //total route
		    		for (int i1 = 2; i1 < last; i1++)  {
		    			route_min2[i1-2] = routeList.get(index).get(i1);
		    		}
		    		routeListLast[index] = new Route(routeLast);
		    		routeListMin2[index] = new Route(route_min2);
		    		probabilities[index] = probabilityList.get(index).doubleValue();
		    	}
				Generator generator = new Generator(laneOrigin, Generator.distribution.EXPONENTIAL);
				generator.routes = routeListLast;
				generator.routeProb = probabilities;
				generator.setClassProbabilities(new double[] { 0.9, 0.1});
				generator.setDemand(totalTripsFrom);	
				probabilityList.clear();
				routeList.clear();
				tripsByNode = 0;
				totalTripsFrom = 0;
			}
			prevNode = startNode;
			tripsByNode = tripsByNode + trip.getPathNumberOfTrips();
			totalTripsFrom = totalTripsFrom + tripsByNode;
			routeList.add(trip.getRoute());
			probabilityList.add(trip.getPathNumberOfTrips());
			
			if (i == tripList.size() - 1)  {
				int size = probabilityList.size();
				double probabilities[] = new double[size];
				Route[] routeListMin2 = new Route[size];
				Route[] routeListLast = new Route[size];
				Lane laneOrigin = lookupOrigin(prevNode, microNetwork);
		    	int[] routeLast = new int[1]; //last node
				//routeList[destination] = new jRoute(route);
		    	for (int index = 0; index < size; index++)   {
		    		int last = routeList.get(index).size();
		    		routeLast[0] = routeList.get(index).get(last-1);
			    	int[] route_min2 = new int[last-2]; //total route
		    		for (int i1 = 2; i1 < last; i1++)  {
		    			route_min2[i1-2] = routeList.get(index).get(i1);
		    		}
		    		routeListLast[index] = new Route(routeLast);
		    		routeListMin2[index] = new Route(routeLast);
		    		probabilities[index] = probabilityList.get(index).doubleValue();
		    	}
				Generator generator = new Generator(laneOrigin, Generator.distribution.EXPONENTIAL);
				generator.routes = routeListLast;
				generator.routeProb = probabilities;
				generator.setClassProbabilities(new double[] { 0.9, 0.1});
				generator.setDemand(totalTripsFrom);	
			}
		}

        // Set speed limits
        for (Lane lane : microNetwork)
        	lane.vLim = 50;
        
        jmodel.network = microNetwork.toArray(new Lane[0]);
        // Add the tapers to the list of lane objects
        for (Lane lane : jmodel.network)
        	if (lane.taper == lane)
        		laneGraphics.add(new LaneGraphic(lane));
        // Then add the non-tapers
        for (Lane lane : jmodel.network)
        	if (lane.taper != lane)
        		laneGraphics.add(new LaneGraphic(lane));
        // class
        Vehicle veh = new Vehicle(jmodel);
        veh.l = 4;
        veh.vMax = 160;
        veh.marker = "o";
        veh.aMin = -6;
        try {
            veh.trajectory = new Trajectory(veh, "microModel.jFCD");
        } catch (ClassNotFoundException cnfe) {
            
        }
        Driver driver = new Driver(veh);
        VehicleDriver clazz = new VehicleDriver(jmodel, veh, 1);
        clazz.addStochasticDriverParameter("fSpeed", VehicleDriver.distribution.GAUSSIAN, 123.7/120, 12.0/120);
        
        veh = new Vehicle(jmodel);
        veh.l = 15;
        veh.marker = "s";
        veh.aMin = -6;
        try {
            veh.trajectory = new Trajectory(veh, "microModel.jFCD");
        } catch (ClassNotFoundException cnfe) {
            
        }
        driver = new Driver(veh);
        driver.a = 0.4;
        clazz = new VehicleDriver(jmodel, veh, 2);
        clazz.addStochasticVehicleParameter("vMax", VehicleDriver.distribution.GAUSSIAN, 85, 2.5);
        ConsistencyCheck.checkPreInit(jmodel);
        System.out.println("model.init()");
        jmodel.init();
        //ConsistencyCheck.checkPostInit(jmodel);
        System.out.println("model created");
        scheduler.enqueueEvent(0d, new Stepper(this));
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
			if (lane.id == 34)
				System.out.println("making pol for 34");
			pol = new poly(lane);
			if (lane.taper == lane)
				drawLines = false;
			else
				drawLines = true;
		}
		
		public void paint(GraphicsPanel graphicsPanel) {
			graphicsPanel.setColor(Color.GRAY);
			graphicsPanel.setStroke(GraphicsPanel.SOLID, 1, 0);
			
            // up line
            if (lane.up==null && lane.generator==null && !lane.isMerge())
            	graphicsPanel.drawPolyLine(pol.upEdge());

            // down line
            if (lane.down==null && lane.destination==0 && !lane.isSplit())
            	graphicsPanel.drawPolyLine(pol.downEdge());

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
            graphicsPanel.drawPolyLine(edge);

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
                graphicsPanel.drawPolyLine(edge);
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
            boolean addRight = false;
            boolean addLeft = false;
            boolean subRight = false;
            boolean subLeft = false;
            if (lane.taper==lane && lane.down==null) {
                mergeTaper = true;
            } else if (lane.taper==lane && lane.up==null) {
                divergeTaper = true;
            }
            if (lane.down==null && lane.right==null && lane.left!=null) {
                subRight = true;
            } else if (lane.down==null && lane.right!=null && lane.left==null) {
                subLeft = true;
            } 
            if (lane.up==null && lane.right==null && lane.left!=null) {
                addRight = true;
            } else if (lane.up==null && lane.right!=null && lane.left==null) {
                addLeft = true;
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
        private Vehicle vehicle;
        
        /**
         * Constructor that sets the vehicle.
         * @param vehicle Concerned vehicle.
         */
        public VehicleGraphic(Vehicle vehicle) {
            this.vehicle = vehicle;
        }
        
        /**
         * Returns the concerned vehicle.
         * @return Vehicle of this vehicle graphic.
         */
        public Vehicle getVehicle() {
            return vehicle;
        }
        
        /**
         * Checks whether the vehicle is still in the simulation.
         * @param model The model object.
         * @return Whether the vehicle is still in the simulation.
         */
        public boolean exists(Model model) {
            return model.getVehicles().contains(vehicle);
        }
        
        /**
         * Returns the bounding box of the vehicle position.
         * @return By default <tt>null</tt>.
         */
        public java.awt.Rectangle.Double getGlobalBounds() {
            return null;
        }
        
        public Point2D.Double[] outline() {
        	Point2D.Double[] result = new Point2D.Double[4];
            double w = 2;
            result[0] = new Point2D.Double(vehicle.globalX + vehicle.heading.y * w / 2, vehicle.globalY - vehicle.heading.x * w / 2);
            result[1] = new Point2D.Double(vehicle.globalX - vehicle.heading.y * w / 2, vehicle.globalY + vehicle.heading.x * w / 2);

            double x2 = vehicle.globalX - vehicle.heading.x * vehicle.l;
            double y2 = vehicle.globalY - vehicle.heading.y * vehicle.l;
            
            result[2] = new Point2D.Double(x2 - vehicle.heading.y * w / 2, y2 + vehicle.heading.x * w / 2);
            result[3] = new Point2D.Double(x2 + vehicle.heading.y * w / 2, y2 - vehicle.heading.x * w / 2);      	
        	return result;
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
            Color vehCol = Color.RED;
            graphicsPanel.setColor(vehCol);
            Point2D.Double[] outline = outline();
            graphicsPanel.drawPolygon(outline);
            if (showDownStream) {
            	Point2D.Double[] line = new Point2D.Double[2];
            	line[0] = new Point2D.Double(vehicle.globalX, vehicle.globalY);
                if (vehicle.leftDown!=null) {
                	graphicsPanel.setColor(new Color(255,0,0));
                    java.awt.Point.Double h = vehicle.leftDown.heading;
                	line[1] = new Point2D.Double(vehicle.leftDown.globalX - h.x * vehicle.leftDown.l, 
                            vehicle.leftDown.globalY - h.y * vehicle.leftDown.l);
                    graphicsPanel.drawPolyLine(line);
                }
                if (vehicle.down!=null) {
                	graphicsPanel.setColor(new Color(0, 255, 0));
                    java.awt.Point.Double h = vehicle.down.heading;
                    line[1] = new Point2D.Double(vehicle.down.globalX - h.x * vehicle.down.l,
                            vehicle.down.globalY - h.y * vehicle.down.l);
                    graphicsPanel.drawPolyLine(line);
                }
                if (vehicle.rightDown!=null) {
                	graphicsPanel.setColor(new Color(0, 0, 255));
                    java.awt.Point.Double h = vehicle.rightDown.heading;
                    line[1] = new Point2D.Double(vehicle.rightDown.globalX - h.x * vehicle.rightDown.l,
                            vehicle.rightDown.globalY - h.y * vehicle.rightDown.l);
                    graphicsPanel.drawPolyLine(line);
                }
            }
            if (showUpStream) {
            	Point2D.Double[] line = new Point2D.Double[2];
            	line[0] = new Point2D.Double(vehicle.globalX, vehicle.globalY);
                if (vehicle.leftUp!=null) {
                    line[1] = new Point2D.Double(vehicle.leftUp.globalX, vehicle.leftUp.globalY);
                    graphicsPanel.drawPolyLine(line);
                }
                if (vehicle.up!=null) {
                	graphicsPanel.setColor(new Color(255, 255, 0));
                    line[1] = new Point2D.Double(vehicle.up.globalX, vehicle.up.globalY);
                    graphicsPanel.drawPolyLine(line);
                }
                if (vehicle.rightUp!=null) {
                	graphicsPanel.setColor(new Color(0, 255, 255));
                    line[1] = new Point2D.Double(vehicle.rightUp.globalX, vehicle.rightUp.globalY);
                    graphicsPanel.drawPolyLine(line);
                }
            }
        }
    } 

	private Point2D.Double mouseDown = null;
	private ObjectInspector objectInspector = null;
	Vehicle selectedVehicle = null;
	
	double selectVehicle(GraphicsPanel graphicsPanel, Point2D.Double p) {
    	System.out.println(String.format("Searching vehicle near %f,%f (rev %f,%f", p.x, p.y, graphicsPanel.reverseTranslate(p).getX(), graphicsPanel.reverseTranslate(p).getY()));
        final int maxDistance = 10;	// pixels
        double bestDistance = Double.MAX_VALUE;
        Vehicle prevSelectedVehicle = selectedVehicle;

        for (Vehicle vehicle : jmodel.getVehicles()) {
        	Point2D.Double[] vehiclePolygon = (new VehicleGraphic(vehicle)).outline();
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
		for (Controller controller : jmodel.controllers)
			if (controller instanceof SimulatedObject)
				((SimulatedObject) controller).paint(jmodel.t, graphicsPanel);
		for (LaneGraphic lg : laneGraphics)
			lg.paint(graphicsPanel);
		boolean showLeader = Main.mainFrame.checkBoxShowLeader.isSelected();
		boolean showFollower = Main.mainFrame.checkBoxShowFollower.isSelected();
		for (Vehicle vehicle : jmodel.getVehicles())
			new VehicleGraphic(vehicle).paint(graphicsPanel, showLeader, showFollower);
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
	public void Shutdown() {
		for (Step s : scheduler.scheduledEvents())
			if (s instanceof SimulatedTrafficLightController)
				((SimulatedTrafficLightController) s).shutdown();		
	}

}

class Stepper implements Step {
	final private LaneSimulator laneSimulator;
	
	public Stepper (LaneSimulator laneSimulator) {
		this.laneSimulator = laneSimulator;
	}
	
	@Override
	public boolean step(double now) {
    	//System.out.println("step entered");
    	Model jmodel = laneSimulator.getJModel();
    	if (now >= jmodel.period)
    		return false;
    	while (jmodel.t < now) {
    		//System.out.println("step calling run(1)");
    		try {
    			jmodel.run(1);
    		} catch (RuntimeException e) {
    			WED.showProblem(WED.ENVIRONMENTERROR, "Error in LaneSimulator:\r\n%s", WED.exeptionStackTraceToString(e));
    			return false;
    		}
    	}
    	// re-schedule myself
    	laneSimulator.getScheduler().enqueueEvent(jmodel.t + jmodel.dt, this);
    	//System.out.println("step returning true");
		return true;
	}
	
}