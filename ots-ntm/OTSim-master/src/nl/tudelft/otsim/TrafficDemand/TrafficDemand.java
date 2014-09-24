package nl.tudelft.otsim.TrafficDemand;

import java.util.ArrayList;
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
import nl.tudelft.otsim.ShortesPathAlgorithms.Path;
import nl.tudelft.otsim.SpatialTools.SpatialQueries;

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
	/** To identify  a Zone in the XML file with the description of TripPatterns */
	public static final String ZONE_PREFIX = "z";
	/** To identify  an Activity Location in the XML file with the description of TripPatterns */
    public static final String ACTIVTITYLOCATION_PREFIX = "a";
    private boolean modified = false;

    /**
	 * Create new TrafficDemand
	 * @param model {@link Model} that will own this TrafficDemand
	 * list of successive locations (zones, activities, nodes) where activities are conducted 
	 */
    public TrafficDemand(Model model) {
		this.model = model;
	}
	
	public TrafficDemand(Model model, ArrayList<TripPattern> tripPatternList) {
		this.model = model;
		this.tripPatternList = tripPatternList;
	}

	public TrafficDemand(Model model, ParsedNode demandRoot) throws Exception {
		this.model = model;
		System.out.print(demandRoot.toString(""));
		for (int index = 0; index < demandRoot.size(TripPattern.XMLTAG); index++)
			tripPatternList.add(new TripPattern(this, demandRoot.getSubNode(TripPattern.XMLTAG, index)));
	}
	
	public void rebuild() {
		if (this.tripPatternList != null)
			createLocationToNodesList(this.tripPatternList);
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
	public void createLocationToNodesList(ArrayList<TripPattern> tripPatternList)   {
		/*		
		 * Initially a list of TripPatterns is read with an ordered list of activity locations
		 * This method connects the locations to nodes in the network
		 * Every zone has a list of associated nodes
		 * For every activity location the nearest node is looked up
		 */	
		for (TripPattern tripPattern : tripPatternList) {
			int numberOfLocations = tripPattern.getLocationList().size();
			double numberOfTrips = tripPattern.getNumberOfTrips();
			ArrayList<ArrayList<Node>> listNodeList = new ArrayList<ArrayList<Node>>();
			
			/* The total amount of possible paths:
			 * For instance we have a Pattern "Z12 Z13 Z15" that represents activities at three locations
			 * And the corresponding nodes are:
			 * - Z12: node numbers 6, 8 and 9
			 * - Z13: node numbers 7, 12 and 12
			 * - Z15: node numbers 122 and 125
			 *  the total amount of paths is 18 (3*3*2)
			*/
			int totalPathCombinations = 1;
			for (int i=0; i < numberOfLocations; i++) {
				String locationList = (String) tripPattern.getLocationList().get(i);
				String[] splitted = locationList.split("\\s");
				// Retrieve the separate locations 
				for (String location: splitted) { 
					int n = 0;
					ArrayList<Node> nodeList = new ArrayList<Node>();
					/*
					 * If the trip pattern uses a {@link Zone} as location type, connect the zones with 
					 * the associated nodes 
					 * Else if the location is a ActivityLocation search the nearest node/link/lane
					 */
					if (location.startsWith(ZONE_PREFIX))  {
						int locationNumber = Integer.parseInt(location.substring(1));
						MicroZone microZone = model.network.lookupMicroZone(locationNumber);
						if (null == microZone)
							WED.showProblem(WED.ENVIRONMENTERROR, "Cannot find micro zone %d", locationNumber);
						else
							for (Integer nodeNumber : microZone.getNodeList())  {
								Node node = model.network.lookupNode(nodeNumber, true);
								nodeList.add(node);
								n++;
							}
					}
					else if (location.startsWith(ACTIVTITYLOCATION_PREFIX))  {
						int locationNumber = Integer.parseInt(location.substring(1));
						ActivityLocation activityLocation = model.network.lookupActivityLocation(locationNumber);									
						double bound = 1000;
						if (activityLocation != null)  {
							double maxSpeed = 80;
							double maxSearchDistance = 1000;
							// find the nearest node 
							ConnectZones.NearestPointAtLink(SpatialQueries.createLinkQuery(model.network.getLinkList(), bound), activityLocation, maxSearchDistance, maxSpeed);
							nodeList.add(activityLocation.getFromNodeNearLocation());
						}
						n++;
					}
					totalPathCombinations = totalPathCombinations * n;
					listNodeList.add(nodeList);
				}
			}
			numberOfLocations = listNodeList.size();
			double weight = 1.0 / totalPathCombinations;
			// for every ActivityLocation or Zone we create an index pointing to the corresponding Nodes 
			int[] index = new int[numberOfLocations];
			// loop through all corresponding nodes and start with activity/zone with index 0 and 1
			int start = 0;
			int next = 1;
			int step = 1;
			//int iter = 0;
			TripPatternPath tripPatternPath = null;
			tripPattern.setTripPatternPathList(new ArrayList<TripPatternPath>());
			// for all combinations of nodes (associated to a tripPattern from A, B, C etc.) 
			// the routes are being prepared
			for (int i = 0; i < totalPathCombinations; i++) {				
				ArrayList<Node> nodeList = new ArrayList<Node>();
				String p = "test";	
				// add the first combination of nodes (all index values are 0)
				for (int j = 0; j < numberOfLocations; j++) {
					nodeList.add(listNodeList.get(j).get(index[j]));
					p = p +  index[j];
				}
				//Debug:
				System.out.println(p);
				
				// increase the index[start]
				index[start]++;
				// when reaching the last item 
				if (index[start] >= listNodeList.get(start).size()) {
					// reset the index[start] to 0
					index[start] = 0;
					if (next > start + step)  {
						index[start + step]++;
						if (index[start + step] >= listNodeList.get(start + step).size())  {
							index[start + step] = 0;
							step++;
							index[start + step]++;
							if (next == start + step)   {
								start = 0;
								index[next] = 0;
								next++;
								index[next]++;
								step = 1;
							}
							step = 1;//laatst
						}
					}
					else {
						start = 0;
						index[next]++;
						while (index[next] >= listNodeList.get(next).size()  && next < index.length) {
							index[next] = 0;
							next = next + 1;
							if (next < index.length)
								index[next]++;
							else
								break;
						}							
					}
					
				}
				
				tripPatternPath = new TripPatternPath(tripPattern,  weight * numberOfTrips, nodeList);
				tripPatternPath.setPathList(new ArrayList<Path>());
				// Create the associated Paths, with only the departing and arriving node
				// the complete Path will be included later by the createShortestPath method
				Node oldNode = null;
				for (int indexNode = 0; indexNode < nodeList.size(); indexNode++)  {				
					Path path = new Path();
					Node node = nodeList.get(indexNode);
					if (indexNode > 0)  {
						ArrayList<Node> pairOfNodes = new ArrayList<Node>();
						pairOfNodes.add(oldNode);
						pairOfNodes.add(node);
						path.setNodeList(pairOfNodes);
						path.setTripPatternPath(tripPatternPath);
						tripPatternPath.getPathList().add(path);
					}
					oldNode = node;					
				}
				tripPattern.getTripPatternPathList().add(tripPatternPath);

			}	
			
		}

	}

/*	public TripPattern createTripPatternList(PersonTripPlan personTripPlan) {
		LinkedList<Long> timeList = new LinkedList<Long>();
		for (ActivityType activityType: personTripPlan.getActivityTypeList()) {
			Long startTimeSeconds = ( activityType.getPreferredStartTime().getTime() - model.settings.getSimulationSettings().getStartTimeSimulation().getTime() ) / 1000;
			timeList.add(startTimeSeconds);
		}		
		TripPattern tripPattern = new TripPattern(personTripPlan.getMovingPerson(), 
				personTripPlan.getActivityLocationList(), timeList);
		return tripPattern;
	}*/
	
	/**
	 * The export of traffic demand to the simulation invokes conversion the list of trip patterns 
	 * Data are written to the result and imported in the LaneSimulator
	 * As we have sequential trips, we have to check that a connecting trip does not start before finishing the activity!
	 */
/*    public String exportTrafficDemand() {
    	String result = "";
    	//List<Lane> laneList = createLaneList();
		for(TripPattern tripPattern: tripPatternList) {
			result += String.format(Main.locale, "TripChain\tPersonID\t%d", tripPattern.getMovingPerson().getID());
			java.util.Iterator<ActivityLocation> itActivityLocation = tripPattern.getActivityLocationList().iterator();
			java.util.Iterator<Long> itDepartureTime = tripPattern.getDepartureTimeList().iterator();
			while (itActivityLocation.hasNext())  {
				ActivityLocation activityLocation = itActivityLocation.next();
				Long time = itDepartureTime.next();
				result += String.format(Main.locale, "\tTime\t%d", time);
				result += String.format(Main.locale, "\tBuilding\tX\t%f", activityLocation.getX());
				result += String.format(Main.locale, "\tY\t%f", activityLocation.getY());
				result += String.format(Main.locale, "\tZ\t%f", activityLocation.getZ());
				result += "\n";
			}
		}
		return result;
    }*/

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
    	//private String exportTripPattern(HashMap<Node, Double> tripsByNode) {	
    	String result = "";
        for (TripPattern tripPattern : getTripPatternList())   {
        	Double totalTrips = tripPattern.getNumberOfTrips();
        	result += String.format(Locale.US, "TripPattern\tnumberOfTrips:\t%.2f\tLocationPattern:\t%s\n", totalTrips, tripPattern.getLocationList().toString());
            for (TripPatternPath tripPatternPath : tripPattern.getTripPatternPathList())   {
            	//Node nodeStart = tripPatternPath.getNodeList().get(0);
            	//Double totalTrips = tripsByNode.get(nodeStart);
            	double numberOfTrips = 0;
            	if (totalTrips > 0)
            		numberOfTrips = tripPatternPath.getNumberOfTrips();
            	result += String.format(Locale.US, "TripPatternPath\tnumberOfTrips:\t%.3f\tNodePattern:\t%s\n", numberOfTrips, tripPatternPath.getNodeList().toString());
				int index = 0;
            	for (Path path : tripPatternPath.getPathList()) {
		     		result += String.format("Path:\t%s\tnodes:", index);
            		for (Node node : path.getNodeList())
			     		result += String.format("\t%d", node.getNodeID());
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
				&& writeTripPatternsXML(staXWriter)
				&& staXWriter.writeNodeEnd(XMLTAG);
		// TODO Should also write the ActivityLocationIDList of the tripPattern
		
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

}