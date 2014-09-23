package nl.tudelft.otsim.TrafficDemand;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import nl.tudelft.otsim.Activities.ConnectZones;
import nl.tudelft.otsim.FileIO.ParsedNode;
import nl.tudelft.otsim.FileIO.StaXWriter;
import nl.tudelft.otsim.GUI.Model;
import nl.tudelft.otsim.GUI.Storable;
import nl.tudelft.otsim.GUI.WED;
import nl.tudelft.otsim.GeoObjects.ActivityLocation;
import nl.tudelft.otsim.GeoObjects.MicroZone;
import nl.tudelft.otsim.GeoObjects.Node;
import nl.tudelft.otsim.ShortesPathAlgorithms.DijkstraAlgorithm;
import nl.tudelft.otsim.ShortesPathAlgorithms.ShortestPathAlgorithm;
import nl.tudelft.otsim.SpatialTools.SpatialQueries;
import nl.tudelft.otsim.Utilities.Sorter;
import nl.tudelft.otsim.Utilities.TimeScaleFunction;

/**
 * Traffic Demand: describes the characteristics of trip patterns that arise from the activities by the population
 * 
 * @author gtamminga
 */
public class TrafficDemand implements Storable {
	/** XML node in for a TrafficDemand */
	public static final String XMLTAG = "Demand";
    /** Extension of traffic demand file */
	public static final String FILETYPE = "otsd";
	
	private Model model;
	private ArrayList<TripPattern> tripPatternList = new ArrayList<TripPattern>();
	private HashMap<String, TrafficClass> trafficClasses = new HashMap<String, TrafficClass>();
	/** To identify  a Zone in the XML file with the description of TripPatterns */
	public static final String ZONE_PREFIX = "z";
	/** To identify  an Activity Location in the XML file with the description of TripPatterns */
    public static final String ACTIVTITYLOCATION_PREFIX = "a";
    private TimeScaleFunction timeScaleFunction = null;
    private boolean modified = false;

    /**
	 * Create new TrafficDemand
	 * @param model {@link Model} that will own this TrafficDemand
	 * list of successive locations (zones, activities, nodes) where activities are conducted 
	 */
    public TrafficDemand(Model model) {
		this.model = model;
	}
    
    /**
     * Look up a {@link TrafficClass}.
     * @param name String; name of the {@link TrafficClass}
     * @return {@link TrafficClass}; the TrafficClass with the specified name, 
     * or null if no TrafficClass with the specified name exists
     */
    public TrafficClass lookupTrafficClass(String name) {
    	return trafficClasses.get(name);
    }
	
	public TrafficDemand(Model model, ArrayList<TripPattern> tripPatternList) {
		this.model = model;
		this.tripPatternList = tripPatternList;
	}

	public TrafficDemand(Model model, ParsedNode demandRoot) throws Exception {
		this.model = model;
		//System.out.print(demandRoot.toString(""));
		if (demandRoot.size(TimeScaleFunction.XMLTAG) > 0) {
			timeScaleFunction = new TimeScaleFunction(demandRoot.getSubNode(TimeScaleFunction.XMLTAG, 0));
			timeScaleFunction.setStorable (this);
		}
		double sumFractions = 0;
		for (int index = 0; index < demandRoot.size(TrafficClass.XMLTAG); index++) {
			TrafficClass tc = new TrafficClass(demandRoot.getSubNode(TrafficClass.XMLTAG, index));
			trafficClasses.put(tc.getName(), tc);
			sumFractions += tc.getDefaultFraction();
		}
		if ((trafficClasses.size() > 0) && (Math.abs(sumFractions - 1.0) > 0.0001))
			throw new Exception("Sum of " + TrafficClass.XMLTAG + " probabilities is not 1.0");
		for (int index = 0; index < demandRoot.size(TripPattern.XMLTAG); index++)
			tripPatternList.add(new TripPattern(this, demandRoot.getSubNode(TripPattern.XMLTAG, index)));
		/*
		// Debugging... make a non-trivial TimeScaleFunction
		if (null == timeScaleFunction) {
			timeScaleFunction = new TimeScaleFunction(this);
			timeScaleFunction.insertPair(60, 1);
			timeScaleFunction.insertPair(60.1, 0);
			timeScaleFunction.insertPair(120, 0);
			timeScaleFunction.insertPair(120.1, 2);
		}
		*/
		if (null == timeScaleFunction) {
			timeScaleFunction = new TimeScaleFunction();
			timeScaleFunction.insertPair(0, 1);
		}	
	}
	
	/**
	 * Rebuild this TrafficDemand after something may have changed.
	 */
	public void rebuild() {
		if (0 == trafficClasses.size()) {
			// Create two simple classes
			trafficClasses.put("passengerCar", new TrafficClass("passengerCar", 0.9, 4.0, 160.0, 0.0, 0.0));
			trafficClasses.put("truck", new TrafficClass("truck", 0.1, 15.0, 85.0, 0.0, 0.0));
		}
		if (this.tripPatternList != null)
			createLocationToNodesList();
	}

	/**
	 * This methods creates node Paths
	 * The {@link TripPattern} list contains information of different types of locations:
	 * {@link MicroZone} or {@link nl.tudelft.otsim.GeoObjects.PolyZone} objects; these zones may refer to a list of 
	 * {@link Node} elements 
	 * {@link ActivityLocation} objects
	 * {@link Node} objects
	 * @param tripPatternList
	 */
	private void createLocationToNodesList()   {
		/*		
		 * Initially a list of TripPatterns is read with an ordered list of activity locations
		 * This method connects the locations to nodes in the network
		 * Every zone has a list of associated nodes
		 * For every activity location the nearest node is looked up
		 */	
		for (TripPattern tripPattern : tripPatternList) {
			int numberOfLocations = tripPattern.getLocationList().size();
			//double numberOfTrips = tripPattern.getNumberOfTrips();
			ArrayList<ArrayList<Node>> listNodeList = new ArrayList<ArrayList<Node>>();
			
			/* The total amount of possible paths:
			 * For instance we have a Pattern "Z12 Z13 Z15" that represents activities at three locations
			 * And the corresponding nodes are:
			 * - Z12: node numbers 6, 8 and 9
			 * - Z13: node numbers 7, 12 and 13
			 * - Z15: node numbers 122 and 125
			 *  the total amount of paths is 18 (3*3*2)
			*/
			int totalPathCombinations = 1;
			for (int i = 0; i < numberOfLocations; i++) {
				String locationList = (String) tripPattern.getLocationList().get(i);
				// Retrieve the separate locations 
				for (String location: locationList.split("\\s")) { 
					ArrayList<Node> nodeList = new ArrayList<Node>();
					/*
					 * If the trip pattern uses a {@link Zone} as location type, connect the zones with 
					 * the associated nodes 
					 * Else if the location is a ActivityLocation search the nearest node/link/lane
					 */
					if (location.startsWith(ZONE_PREFIX)) {
						int locationNumber = Integer.parseInt(location.substring(1));
						MicroZone microZone = model.network.lookupMicroZone(locationNumber);
						if (null == microZone)
							WED.showProblem(WED.ENVIRONMENTERROR, "Cannot find micro zone %d", locationNumber);
						else
							for (Integer nodeNumber : microZone.getNodeList())  {
								Node node = model.network.lookupNode(nodeNumber, true);
								nodeList.add(node);
							}
					} else if (location.startsWith(ACTIVTITYLOCATION_PREFIX))  {
						int locationNumber = Integer.parseInt(location.substring(1));
						ActivityLocation activityLocation = model.network.lookupActivityLocation(locationNumber);									
						double bound = 1000;
						if (activityLocation != null) {
							double maxSpeed = 80;
							double maxSearchDistance = 1000;
							// find the nearest node 
							ConnectZones.NearestPointAtLink(SpatialQueries.createLinkQuery(model.network.getLinkList(), bound), activityLocation, maxSearchDistance, maxSpeed);
							nodeList.add(activityLocation.getFromNodeNearLocation());
						} else
							WED.showProblem(WED.ENVIRONMENTERROR, "Cannot find activity location %d", locationNumber);
					}
					if (0 == nodeList.size())
						throw new Error("There is no node in " + location);
					totalPathCombinations *= nodeList.size();
					listNodeList.add(nodeList);
				}
			}
			if (listNodeList.size() != numberOfLocations)
				throw new Error("Cannot happen");
			//numberOfLocations = listNodeList.size();
			if (2 > numberOfLocations)
				throw new Error("Too few locations in listNodeList");
			if (totalPathCombinations == 0)
				throw new Error("No path found");
			double weight = 1.0 / totalPathCombinations;
			// loop through all corresponding nodes and start with activity/zone with index 0 and 1
			tripPattern.clearTripPatternPaths();
			int depth = 0;
			int[] indices = new int[numberOfLocations];
			indices[0] = 0;
			/*
			for (int i = 0; i < numberOfLocations; i++) {
				System.out.print("zone " + i + " is " + (String) tripPattern.getLocationList().get(i) + ": ");
				for (int j = 0; j < listNodeList.get(i).size(); j++)
					System.out.print(listNodeList.get(i).get(j) + " ");
				System.out.println("");
			}
			*/
			while (indices[0] < listNodeList.get(0).size()) {
				if (depth == numberOfLocations) {
					// create a route
					//System.out.println("Adding a route");
					ArrayList<Node> path = new ArrayList<Node> (numberOfLocations);
					for (int i = 0; i < numberOfLocations; i++)
						path.add(listNodeList.get(i).get(indices[i]));
					TripPatternPath tripPatternPath = new TripPatternPath(tripPattern,  new TimeScaleFunction(timeScaleFunction, weight), path);
					tripPattern.addTripPatternPath(tripPatternPath);
					depth--;
					indices[depth]++;
				}
				int limit = listNodeList.get(depth).size();
				if (indices[depth] >= limit) {
					depth--;
					if (depth >= 0)
						indices[depth]++;
				} else {
					depth++;
					if (depth < indices.length)
						indices[depth] = 0;
				}				
			}
		}
	}

	public ArrayList<TripPattern> getTripPatternList() {
		return tripPatternList;
	}

	/**
	 * Modify the TripPattern list.
	 * @param tripPatternList ArrayList<TripPattern>; 
	 */
	public void setTripPatternList(ArrayList<TripPattern> tripPatternList) {
		this.tripPatternList = tripPatternList;
		modified = true;
	}

	/**
	 * write the TripPattern list to file.
	 */
	private boolean writeTripPatternsXML(StaXWriter staXWriter) {
		if ((null != timeScaleFunction) && (! timeScaleFunction.isTrivial()))
			if (! timeScaleFunction.writeXML(staXWriter))
				return false;
		for (TripPattern tp : getTripPatternList())
			if (! tp.writeXML(staXWriter))
				return false;
		return true;
	}
	
	/**
	 * Export this TrafficDemand in textual form.
	 * @return String; the textual representation of this TrafficDemand
	 */
    public String exportTripPattern() {
    	String result = "";
    	for (String tcName : trafficClassNames()) {
    		TrafficClass tc = lookupTrafficClass(tcName);
    		result += String.format(Locale.US, "TrafficClass\t%s\t%.3f\t%.3f\t%.3f\t%.6f\t%.3f\n", tcName, tc.getLength(), tc.getMaximumSpeed(), tc.getMaximumDeceleration(), tc.getActivationLevel(), tc.getTransitionTime());
    	}
    	createRoutes();
        for (TripPattern tripPattern : getTripPatternList()) {
        	TimeScaleFunction totalFlow = new TimeScaleFunction(tripPattern.flowGraph, timeScaleFunction);
        	//Double totalTrips = tripPattern.getNumberOfTrips();
        	result += String.format(Locale.US, "TripPattern\tnumberOfTrips:\t%s\tLocationPattern:\t%s\tFractions%s\n", totalFlow.export(), tripPattern.getLocationList().toString(), tripPattern.getClasslFlows());
        	double factor = 1d / tripPattern.getTripPatternPathList().size();
        	totalFlow = new TimeScaleFunction(totalFlow, factor);
            for (TripPatternPath tripPatternPath : tripPattern.getTripPatternPathList()) {
            	//double numberOfTrips = 0;
            	//if (totalTrips > 0)
            	//	numberOfTrips = tripPatternPath.getNumberOfTrips();
            	result += String.format(Locale.US, "TripPatternPath\tnumberOfTrips:\t%s\tNodePattern:\t%s\n", totalFlow.export(), tripPatternPath.getNodeList().toString());
				int index = 0;
            	for (ArrayList<Node> path : tripPatternPath.getDetailedPathList()) {
		     		result += String.format(Locale.US, "Path:\t%f\tnodes:", tripPatternPath.getProbability(index));
            		for (Node node : path) {
			     		//result += String.format("\t%d%s", node.getNodeID(), node.isAutoGenerated() ? "a" : "");
            			result += String.format("\t%d%s", node.getNodeID(), node.getLinksFromJunction(false).size() > 1 ? "a" : "");
            		}
            		System.out.println("result is \"" + result + "\"");
    				result += "\n";
    				index++;
            	}
            }
		}
		return result;
    }

	@Override
	public boolean writeXML(StaXWriter staXWriter) {
		return staXWriter.writeNodeStart(XMLTAG)
				&& writeTrafficClassesXML(staXWriter)
				&& writeTripPatternsXML(staXWriter)
				&& staXWriter.writeNodeEnd(XMLTAG);
		// TODO Should also write the ActivityLocationIDList of the tripPattern
		
	}

	private boolean writeTrafficClassesXML(StaXWriter staXWriter) {
		for (String name : Sorter.asSortedList(trafficClasses.keySet()))
			if (! trafficClasses.get(name).writeXML(staXWriter))
				return false;
		return true;
	}

	@Override
	public String description() {
		return "traffic demand";
	}

	@Override
	public String fileType() {
		return FILETYPE;
	}

	@Override
	public boolean isModified() {
		return modified;
	}

	@Override
	public void clearModified() {
		modified = false;
	}

	private String fileName = null;
	
	@Override
	public String storageName() {
		return fileName;
	}

	@Override
	public void setStorageName(String name) {
		fileName = name;
	}

	/**
	 * Retrieve the set of names of the defined (@link TrafficClass TrafficClasses}.
	 * @return Set&lt;String&gt;; the set of names of the defined {@link TrafficClass TrafficClasses}
	 */
	public List<String> trafficClassNames() {
		return Sorter.asSortedList(trafficClasses.keySet());
	}
	
	
    /**
     * Create paths between all pairs of nodes, if possible.
     * Save the paths in a list of paths
     */
    private void createRoutes() {
		ShortestPathAlgorithm shortestPathAlgorithm = new DijkstraAlgorithm(model.network, 2);
		ArrayList<TripPatternPath> ptps = new ArrayList<TripPatternPath>();
		for (TripPattern tp : tripPatternList)
			for (TripPatternPath tpp : tp.getTripPatternPathList())
				ptps.add(tpp);
		
		class CompareTripPatternPaths implements Comparator<TripPatternPath> {
			@Override
			public int compare(TripPatternPath o1, TripPatternPath o2) {
				return o1.getFromNode().getNodeID() - o2.getFromNode().getNodeID();
			}
		}
		Collections.sort(ptps, new CompareTripPatternPaths());
		for (TripPatternPath tpp : ptps) {
			Node startNode = tpp.getFromNode();
			Node endNode = tpp.getToNode();
			shortestPathAlgorithm.execute(startNode, endNode);
			boolean routeFound = false;
			while (shortestPathAlgorithm.hasNext()) {
    			routeFound = true;
        		ArrayList<Node> path = shortestPathAlgorithm.getPathNodes();
        		double cost = shortestPathAlgorithm.getCost();
				tpp.addDetailedPath(path, cost);
			}
    		if (! routeFound)
    			System.out.println("no valid path found between " + startNode.getNodeID() + " and " + endNode.getNodeID());
		}	
    }

	@Override
	public void setModified() {
		modified = true;
	}

}