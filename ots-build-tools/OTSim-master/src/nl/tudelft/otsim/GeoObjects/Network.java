package nl.tudelft.otsim.GeoObjects;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Iterator;
import java.util.Set;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;

import nl.tudelft.otsim.FileIO.ParsedNode;
import nl.tudelft.otsim.FileIO.StaXWriter;
import nl.tudelft.otsim.FileIO.XML_IO;
import nl.tudelft.otsim.GUI.GraphicsPanel;
import nl.tudelft.otsim.GUI.GraphicsPanelClient;
import nl.tudelft.otsim.GUI.Main;
import nl.tudelft.otsim.GUI.ObjectInspector;
import nl.tudelft.otsim.GUI.Storable;
import nl.tudelft.otsim.SpatialTools.Planar;

/**
 * The Network class holds the geographical information about a network.
 * <br />
 * Some geographical information is re-generated when needed. This is
 * implemented with a <i>dirty</i> flag that should be set whenever something 
 * changes.
 * <br />
 * A <i>modified</i> flag is used to track unsaved changes that the user may
 * want to write to permanent storage instead of discarding.
 * 
 * @author gjtamminga, Peter Knoppers
 */
public class Network implements GraphicsPanelClient, ActionListener, XML_IO, Storable {
	/** Name for a Network element when stored in XML format */
	public static final String XMLTAG = "Network";
	/** File extension for Network files */
	public static final String FILETYPE = "otsn";
	
	private HashMap<Integer, Node> nodes = new HashMap<Integer, Node>();
	private HashMap<Integer, MicroZone> microZoneList = new HashMap<Integer, MicroZone>();
	private List<Node> expandedNodeList = new ArrayList<Node>();
    private int nodeMaxId = -1;
	private HashMap<String, Link> links = new HashMap<String, Link>();
	private HashMap<Integer, ActivityLocation> activityLocationList = new HashMap<Integer, ActivityLocation>();
	private HashMap<Integer, PolyZone> polyZoneList = new HashMap<Integer,PolyZone>();
	private HashMap<String, TrafficLightController> trafficLightControllers = new HashMap<String, TrafficLightController> ();
	private List<CrossSectionElementTypology> crossSectionElementTypologyList = new ArrayList<CrossSectionElementTypology>();
	private List<RoadMarkerAlongTemplate> roadMarkerAlongTemplates = new ArrayList<RoadMarkerAlongTemplate>();
	private boolean dirty = false;
	private boolean modified = false;
	/** Node selected as starting node for editing */
	public Node startNode = null;
	/** Node selected as ending node for editing */
	public Node endNode = null;
	/** Node selected for editing */
	public Node selectedNode = null;
	/** MicroZone selected for editing */
	public MicroZone selectedMicroZone = null;
	/** MicroZone selected for editing */
	public ActivityLocation selectedActivityLocation = null;
	/** Link selected for editing */
	public Link selectedLink = null;
	/** Lane selected for editing */
	public Lane selectedLane = null;
	/** Lane control point selected for editing */
	public Point2D.Double selectedLaneControlPoint = null;
	/** CrossSection selected for editing */
	public CrossSection selectedCrossSection = null;
	private Component repaintComponent = null;
	private IntBuffer[][] routesBetweenMicroZones;

	/**
	 * Clear the <i>modified</i> flag. (Should be called when this Network has 
	 * been successfully saved to a file.)
	 */
	@Override
	public void clearModified() {
		modified = false;
	}
	
	/**
	 * Create a new empty network.
	 */
	public Network() {
		// Minimal code is needed to create an empty network
		addCrossSectionElementTypology("road", true, CrossSectionElementTypology.COUPLING_PRIORITY_AUTOMATIC);
		addCrossSectionElementTypology("barrier", false, CrossSectionElementTypology.COUPLING_PRIORITY_AUTOMATIC);
		addCrossSectionElementTypology("grass", false, CrossSectionElementTypology.COUPLING_PRIORITY_AUTOMATIC);
		addRoadMarkerAlongTemplate("|",0.2);
		addRoadMarkerAlongTemplate(":",0.2);
		addRoadMarkerAlongTemplate(":|",0.2);
		addRoadMarkerAlongTemplate("|:",0.2);
		modified = false;
	}
	
	/**
	 * Create a Network from a parsed XML file.
	 * @param networkRoot {@link ParsedNode}; root of the Network field in the parsed XML file
	 * @throws Exception
	 */
	public Network(ParsedNode networkRoot) throws Exception {
		// First obtain all CrossSectionElementTypologies
		for (int index = 0; index < networkRoot.size(CrossSectionElementTypology.XMLTAG); index++)
			crossSectionElementTypologyList.add(new CrossSectionElementTypology(networkRoot.getSubNode(CrossSectionElementTypology.XMLTAG, index)));
		for (int index = 0; index < networkRoot.size(RoadMarkerAlongTemplate.XMLTAG); index++)
			roadMarkerAlongTemplates.add(new RoadMarkerAlongTemplate(networkRoot.getSubNode(RoadMarkerAlongTemplate.XMLTAG, index)));
		for (int index = 0; index < networkRoot.size(Node.XMLTAG); index++) {
			ParsedNode nodeNode = networkRoot.getSubNode(Node.XMLTAG, index);
			Node newNode = new Node(this, nodeNode);
			if (null != nodes.get(newNode.getNodeID()))
				throw new Exception("Duplicate node id at " + nodeNode.lineNumber + ", " + nodeNode.columnNumber);
			nodes.put(newNode.getNodeID(), newNode);
		}
		for (int index = 0; index < networkRoot.size(Link.XMLTAG); index++) {
			Link newLink = new Link(this, networkRoot.getSubNode(Link.XMLTAG, index));
			if (null != lookupLink(newLink.getName_r()))
				throw new Exception("Link name " + newLink.getName_r() + " already defined");
			links.put(newLink.getName_r(), newLink);
		}
		for (int index = 0; index < networkRoot.size(PolyZone.XMLTAG); index++) {
			PolyZone newPolyZone = new PolyZone(this, networkRoot.getSubNode(PolyZone.XMLTAG, index));
			if (null != lookupPolyZone(newPolyZone.getZoneID()))
				throw new Exception("PolyZone " + newPolyZone.getZoneID() + " already defined");
			polyZoneList.put(newPolyZone.getZoneID(), newPolyZone);
		}
		for (int index = 0; index < networkRoot.size(MicroZone.XMLTAG); index++) {
			MicroZone newMicroZone = new MicroZone(this, networkRoot.getSubNode(MicroZone.XMLTAG, index));
			if (null != microZoneList.get(newMicroZone.getZoneID()))
				throw new Exception("Microzone with id " + newMicroZone.getZoneID() + " already defined");
			microZoneList.put(newMicroZone.getZoneID(), newMicroZone);
		}
		for (int index = 0; index < networkRoot.size(ActivityLocation.XMLTAG); index++) {
			ActivityLocation newActivityLocation = new ActivityLocation(this, networkRoot.getSubNode(ActivityLocation.XMLTAG, index));
			if (null != activityLocationList.get(newActivityLocation.getActivityLocationID_r()))
				throw new Exception("Microzone with id " + newActivityLocation.getActivityLocationID_r() + " already defined");
			activityLocationList.put(newActivityLocation.getActivityLocationID_r(), newActivityLocation);
		}
		for (int index = 0; index < networkRoot.size(TrafficLightController.XMLTAG); index++) {
			TrafficLightController newTLC = new TrafficLightController(this, networkRoot.getSubNode(TrafficLightController.XMLTAG, index));
			if (null != trafficLightControllers.get(newTLC.getName_r()))
				throw new Exception("TrafficLightController with name " + newTLC.getName_r() + " already defined");
			trafficLightControllers.put(newTLC.getName_r(), newTLC);
		}
		/*
		for (String key : networkRoot.getKeys()) {
			else if (key.equals(MicroZone.XMLTAG))
				parseMicroZone(model.network, data.data.get(key));
			else if (key.equals(ActivityLocation.XMLTAG))
				parseActivityLocation(model.network, data.data.get(key));
			else
		        throw new Exception ("Unknown network object " + key + " at " + data.lineNumber + ", " + data.columnNumber);
		}
		*/
		dirty = true;
		rebuild();
		modified = false;
	}

	/**
	 * Write this Network to an XML file.
	 * @param staXWriter {@link StaXWriter} writer for the XML file
	 * @return Boolean; true on success, false on failure
	 */
	@Override
	public boolean writeXML(StaXWriter staXWriter) {
		if (! staXWriter.writeNodeStart(XMLTAG))
			return false;
		// Write the Nodes
		for (Node node : getNodeList(true))
			if (! node.writeXML(staXWriter))
				return false;
		// Write the microZones
		for (MicroZone microZone : getMicroZoneList())
			if (! microZone.writeXML(staXWriter))
				return false;
		// Write the links; sorted by name (this is a bit expensive).
		// Without this, the order is unpredictable which makes comparing
		// network files very difficult.
		ArrayList<Link> sortedLinks = new ArrayList<Link> ();
		for (Link link : getLinkList()) 
			if (! link.isAutoGenerated())
				sortedLinks.add(link);
		Collections.sort(sortedLinks, new Comparator<Link>() {
			@Override
			public int compare(Link first, Link second) {
				return first.getName_r().compareTo(second.getName_r());
			}
		});
		for (Link link : sortedLinks)
			if (! link.writeXML(staXWriter))
				return false;
		// Write CrossSectionElementTypologies
		for (CrossSectionElementTypology cset : getCrossSectionElementTypologyList())
			if (! cset.writeXML(staXWriter))
				return false;
		// Write the RoadMarkerAlongTemplates
		for (RoadMarkerAlongTemplate rmat : getRoadMarkerTemplateList())
			if (! rmat.writeXML(staXWriter))
				return false;
		// Write the TrafficLightControllers
		for (TrafficLightController tlc : trafficLightControllers.values())
			if (! tlc.writeXML(staXWriter))
				return false;
		if (! staXWriter.writeNodeEnd(XMLTAG))
			return false;
		// TODO write PolyZones
		// TODO write MicroZones
		// TODO write ActivityLocations
		return true;
	}
	
	/**
	 * Create a (possibly very long) string with a text description of all
	 * boundary lines of drive-able areas in the network.
	 * @return String; list of polylines. Each polyline starts on a new line,
	 * all coordinate values are tab-separated and have two decimal places
	 */
	public String exportDrivableBoundaries() {
		String polyLines = "";
    	// walk through all links
    	for (Link link : getLinkList()) {
    		if (! link.isAutoGenerated())   {
	    		// walk through the crossSections of this link
				for (CrossSection cs : link.getCrossSections_r()) {
					// walk through the crossSectionElements of this crossSection
					for (CrossSectionElement cse : cs.getCrossSectionElementList_r()) {
						// output the contour lines of all drive-able crossSectionElements
						if (cse.getCrossSectionElementTypology().getDrivable()) {
							ArrayList<Vertex> linkPointList = cse.getVerticesInner();
							// output the inner contour line if it exists
							if (null != linkPointList) {
								polyLines += "Border\t";
								for (Vertex v : linkPointList)
			                    	polyLines += String.format(Locale.US, "%.2f\t%.2f\t", v.getX(), v.getY());
			                    polyLines += "\n";							
							}
							linkPointList = cse.getVerticesOuter();
							// output the outer contour line if it exists
							if (null != linkPointList) {
								polyLines += "Border\t";
								for (Vertex v : linkPointList)
			                    	polyLines += String.format(Locale.US, "%.2f\t%.2f\t", v.getX(), v.getY());
			                    polyLines += "\n";
							}
						}
					}
				}
    		}
		}
		for (Node node : getNodeList(true)) {
			for (ArrayList<Vertex> alv : node.closingLines) {
				polyLines += "Border\t";
				for (Vertex v : alv)
					polyLines += String.format(Locale.US, "%.2f\t%.2f\t", v.getX(), v.getY());
            	polyLines += "\n";
			}
		}
		//System.out.println("Polylines:\r\n" + polyLines);
		return polyLines;
	}

	/**
	 * Obtain a list of all {@link Lane Lanes} in this network.
	 * @return List&lt;Lane&gt; List of lanes ordered by lane ID.
	 */
	// TODO: It is ridiculous that this method modifies origins and destinations of the lanes
	// (should have been done at creation time of the Lanes)
	public List<Lane> createLaneList() {
		class CompareLaneNumbers implements Comparator<Lane> {
		    @Override
			public int compare(Lane lane1, Lane lane2) {
		        return lane1.getID() - lane2.getID();
		    }
		}

		List<Lane> laneList = new ArrayList<Lane>();
		for(Link link : getLinkList()) {
			//System.out.println("link: " + link.toString());
			int indexCs = -1;
			for (CrossSection cs : link.getCrossSections_r()) {
				//System.out.println("    cs: " + cs.toString());
				indexCs++;
				for (Lane lane : cs.collectLanes()) {
					//Every lane gets an Origin and a Destination
					// FIXME: This should have been done at Lane creation time
					if (link.getFromNode_r().isSource() && indexCs == 0)
						lane.setOrigin(link.getFromNodeExpand().getNodeID());
					if (indexCs == link.getCrossSections_r().size() - 1)
						lane.setDestination(link.getToNodeExpand().getNodeID());
					//System.out.println("            " + lane.toString());
					laneList.add(lane);
				}
			}
		}
		// sort list by laneId
		Collections.sort(laneList, new CompareLaneNumbers());
		return laneList;
	}

	/**
	 * Retrieve a list of all {@link Node Nodes} in the network
	 * @param mayRebuild Boolean: true if details in the network may be rebuilt;
	 * false to get the current list without rebuilding anything
	 * @return List&lt;Node&gt; List of all Nodes in the network
	 */
	public final Collection<Node> getNodeList(boolean mayRebuild) {
		if (mayRebuild)
			rebuild();
		return nodes.values();
	}
	
	/**
	 * Retrieve a list of all {@link Node Nodes} in the network
	 * @param mayRebuild Boolean: true if details in the network may be rebuilt;
	 * false to get the current list without rebuilding anything
	 * @return List&lt;Node&gt; List of all Nodes in the network
	 */
	public final Collection<MicroZone> getMicroZoneList(boolean mayRebuild) {
		if (mayRebuild)
			rebuild();
		return microZoneList.values();
	}
	
	/**
	 * Find a Node with a given ID.
	 * @param ID Integer; ID of the Node
	 * @param mayRebuild Boolean; true if details of the network bay be rebuilt;
	 * false to get the current Node with this ID without rebuilding anything
	 * @return Node; the Node with the specified ID or null of no such Node
	 * exists in the Network
	 */
	// TODO: this should probably use a Map
	public Node lookupNode(int ID, boolean mayRebuild) {
		for (Node node : getAllNodeList(mayRebuild))
			if (node.getNodeID() == ID)
				return node;
		return null;
	}

	/**
	 * Find a Node with a given name.
	 * @param name String; name of the Node
	 * @param mayRebuild Boolean; true if details of the network bay be rebuilt;
	 * false to get the current Node with this ID without rebuilding anything
	 * @return Node; the Node with the specified name or null of no such Node
	 * exists in the Network
	 */
	// TODO: this should probably use a Map
	public Node lookupNode(String name, boolean mayRebuild) {
		for (Node node : getAllNodeList(mayRebuild))
			if (node.getName_r().equals(name))
				return node;
		return null;
	}

	/**
	 * Find a Link with a given name.
	 * @param name String; name of the Link
	 * @return Link; the Link with the specified name, or null if no such Link
	 * exists in the Network
	 */
	public Link lookupLink(String name) {
		return links.get(name);
	}

	/**
	 * Lookup a {@link MicroZone} by ID.
	 * @param zoneID Integer; ID of the zone
	 * @return {@link MicroZone}; the zone with the specified ID, or null if no
	 * such zone is defined in this {@link Network}
	 */
	public MicroZone lookupMicroZone(int zoneID) {
		return microZoneList.get(zoneID);
	}
	
	/* NEVER USED
	// TODO: Guus: explain what this does/is
	public Link lookupLinkUp(Link linkSplitted) {
		for (Link link : linkList)
			if (linkSplitted.getFromNode_r().equals(link.getToNode_r()))
				return link;
		return null;
	}
	*/

	/* NEVER USED
	public Link lookupLinkDown(Link linkMerged) {
		for (Link link : linkList)
			if (linkMerged.getToNode_r().equals(link.getFromNode_r()))
				return link;
		return null;
	}
	*/
	
	/**
	 * Create a new {@link MicroZone} and add it to this Network.
	 * @param zoneName String; name of the new MicroZone
	 * @param nodeIDs String; list of node IDs (separated by white space)
	 * @param zoneID Integer; ID of the new MicroZone (may not already exist
	 * in this Network)
	 * @param X Double; X-Coordinate of the center of the new MicroZone
	 * @param Y Double; Y-Coordinate of the center of the new MicroZone
	 * @param Z Double; Z-Coordinate of the center of the new MicroZone
	 */
	public void addMicroZone(String zoneName, String nodeIDs, int zoneID, double X, double Y, double Z) {
		if (null != lookupMicroZone(zoneID))
			throw new Error(String.format("Zone ID %d already exists", zoneID));
		List<Integer> nodeIDS = new ArrayList<Integer>();
		if (null != nodeIDs) {
			String[] fields = nodeIDs.split("\\s");
			for (String nodeIDString : fields)
				nodeIDS.add(Integer.parseInt(nodeIDString));
		} else	// This should probably be fixed in those network files.
			System.out.print("Zone has no list of nodes");
			//WED.showProblem(WED.ENVIRONMENTERROR, "Zone %s has no list of nodes", zoneName);
		microZoneList.put(zoneID, new MicroZone(this, zoneName, nodeIDS, null, zoneID, X, Y, Z));
		setModified();
	}
	
	/**
	 * Retrieve the list of {@link MicroZone MicroZones} in this Network
	 * @return Collection&lt;{@link MicroZone}&gt; the list of MicroZones
	 */
	public Collection<MicroZone> getMicroZoneList() {
		return microZoneList.values();
	}

	public IntBuffer[][] getRoutesBetweenMicroZones() {
		return routesBetweenMicroZones;
	}

	public void setRoutesBetweenMicroZones(IntBuffer[][] routesBetweenMicroZones) {
		this.routesBetweenMicroZones = routesBetweenMicroZones;
	}

	/**
	 * Create a new Node in this Network.
	 * @param nodeName String; name of the new Node (must not yet exist)
	 * @param nodeID Integer; ID of the new Node (must not yet exist)
	 * @param X Double; X-coordinate of the new Node
	 * @param Y Double; Y-coordinate of the new Node
	 * @param Z Double; Z-coordinate of the new Node
	 */
	public void addNode(String nodeName, int nodeID, double X, double Y, double Z) {
		if (null != lookupNode(nodeID, false))
			throw new Error(String.format("Node ID %d already exists", nodeID));
		if (null != lookupNode(nodeName, false))
			throw new Error(String.format("Node name %s already exists",  nodeName));
		nodes.put(nodeID, new Node(this, nodeName, nodeID, X, Y, Z));
		setModified();
	}

	/**
	 * Remove a Node from this Network.
	 * <br />
	 * The Node may have no Links connected to it. To delete a node that has
	 * Links connected to it; remove those Links first.
	 * @param node Node to remove.
	 */
	private void deleteNode(Node node) {
		if (node.getLinks_r().size() > 0)
			throw new Error("Cannot delete node that has Links");
		nodes.remove(node);
		setModified();
	}

	/**
	 * Retrieve the list of all {@link Link Links} in this Network.
	 * @return List&lt;Link&gt; List of all Links in this Network
	 */
	public final Collection<Link> getLinkList() {
		rebuild();
		return links.values();
	}
	
	/**
	 * Determine if a {@link Node} was created through node expansion.
	 * @param node {@link Node} to check
	 * @return Boolean; true if the node was created through node expansion;
	 * false if the node does not exist or was <b>not</> creeate through node
	 * expansion
	 */
	public boolean isExpandedNode(Node node) {
		return expandedNodeList.contains(node);
	}

	/**
	 * Retrieve a list of all {@link Node Nodes} in this Network, including
	 * Nodes that are automatically added to fix geometry at junctions. The
	 * returned list is sorted by the IDs of the Nodes. 
	 * @param mayRebuild Boolean; true to permit reconstruction of geometry at 
	 * junctions; false to prevent reconstruction of geomtry at junctions
	 * @return List&lt;Node&gt; List of Nodes in this Network
	 */
	public ArrayList<Node> getAllNodeList(boolean mayRebuild) {
		class ComparatorNodeNumbers implements Comparator<Node> {
		    @Override
			public int compare(Node node1, Node node2) {
		        return node1.getNodeID() - node2.getNodeID();
		    }
		}
		ArrayList<Node> allNodeList = new ArrayList<Node>();
		allNodeList.addAll(getNodeList(mayRebuild));
		allNodeList.addAll(expandedNodeList);
		Collections.sort(allNodeList, new ComparatorNodeNumbers());
		return allNodeList;
	}

	/**
	 * Add a Link to this Network.
	 * @param linkName String; name of the new Link (must be unique)
	 * @param fromNodeID Integer; ID of the {@link Node} where the new Link starts
	 * @param toNodeID Integer; ID of the {@link Node} where the new Link ends
	 * @param length Double; Length of the new Link in m
	 * @param priority Boolean; Priority of the new Link
	 * @param crossSections ArrayList&lt;{@link CrossSection}&gt; List of 
	 * CrossSections of the new Link 
	 * @param intermediateVertices ArrayList&lt;{@link Vertex}&gt; List of
	 * vertices that specify additional points on the <i>design line</i> of the 
	 * new Link
	 * @return {@link Link}; the newly created Link
	 */
	public Link addLink(String linkName, int fromNodeID, int toNodeID, double length, boolean priority, ArrayList<CrossSection> crossSections, ArrayList<Vertex> intermediateVertices) {
		if (null != lookupLink(linkName))			
			throw new Error(String.format("Link with name %s already exists", linkName));
		Link result;
		links.put(linkName, result = new Link(this, linkName, lookupNode(fromNodeID, false), lookupNode(toNodeID, false), length, priority, crossSections, intermediateVertices));
		setModified();
		return result;
	}
	
	/**
	 * Retrieve the entire set of {@link ActivityLocation ActivityLocations} of 
	 * this Network.
	 * @return Collection&lt;{@link ActivityLocation}&gt;; the set of 
	 * ActivityLocations of this Network
	 */
	public final Collection<ActivityLocation> getActivityLocationList() {
		rebuild();
		return activityLocationList.values();
	}
	
	/**
	 * Create a new ActivityLocation and add it to this Network.
	 * <br /> The ID of the new ActivityLocation may not already exist in this
	 * Network.
	 * @param activityLocationName String; name of the new ActivityLocation
	 * @param activityLocationID Integer; ID of the new ActivityLocation
	 * @param parkingSpaces Integer; number of parking spaces of the new
	 * ActivityLocation
	 * @param vertex Vertex; center of the new ActivityLocation
	 */
	public void addActivityLocation(String activityLocationName, int activityLocationID, int parkingSpaces, Vertex vertex) {
		if (null != lookupActivityLocation(activityLocationID))
			throw new Error(String.format("activityLocation ID %d already exists", activityLocationID));
		this.activityLocationList.put(activityLocationID, new ActivityLocation(this, activityLocationID, activityLocationName, parkingSpaces,vertex));
		setModified();
	}	

	/**
	 * Retrieve an {@link ActivityLocation} by ID.
	 * @param activityLocationID Integer; ID of the ActivityLocation to
	 * retrieve
	 * @return {@link ActivityLocation}; the ActivityLocation with the
	 * specified ID, or null if no such ActivityLocation is present in this
	 * Network
	 */
	public ActivityLocation lookupActivityLocation(int activityLocationID) {
		return activityLocationList.get(activityLocationID);
	}	

	/**
	 * Retrieve the entire set of {@link PolyZone PolyZones} of this Network.
	 * @return Collection&lt;{@link PolyZone}&gt;; the set of PolyZones of this
	 * Network
	 */
	public final Collection<PolyZone> getPolyZoneList() {
		rebuild();
		return polyZoneList.values();
	}
	
	/**
	 * Create a new {@link PolyZone} and add it to this Network.
	 * @param zoneName String; name of the new PolyZone
	 * @param zoneID Integer; ID of the new PolyZone
	 * @param activityList ArrayList&lt;{@link ActivityLocation}&gt;; list of
	 * activities for the new PolyZone
	 * @param pointList ArrayList&lt;{@link Vertex}&gt;; vertices of the
	 * outline of the new PolyZone
	 */
	public void addPolyZone(String zoneName, int zoneID, ArrayList<ActivityLocation> activityList, ArrayList<Vertex> pointList) {
		if (null != lookupPolyZone(zoneID))
			throw new Error(String.format("Zone ID %d already exists", zoneID));
		this.polyZoneList.put(zoneID, new PolyZone(this, zoneName, activityList, zoneID, pointList));
		setModified();
	}	

	/**
	 * Retrieve a {@link PolyZone} by ID.
	 * @param ID Integer; ID of the PolyZone to retrieve
	 * @return {@link PolyZone}; the PolyZone that matches the ID, or null if
	 * no such PolyZone is present in this Network
	 */
	public PolyZone lookupPolyZone(int ID) {
		return polyZoneList.get(ID);
	}
	
	/**
	 * Retrieve the list of {@link CrossSectionElementTypology 
	 * CrossSectionElementTypologies} of this Network.
	 * @return List&lt;CrossSectionElementTypology&gt; List of
	 * CrossSectionElementTypologies
	 */
	public final List<CrossSectionElementTypology> getCrossSectionElementTypologyList() {
		rebuild();
		return crossSectionElementTypologyList;
	}

	/**
	 * Add a CrossSectionElementTypology to this Network, or modify an existing
	 * CrossSectionElementTypology.
	 * @param name String; name of the CrossSectionElementTypology
	 * @param drivable Boolean; true if vehicles are allowed to drive on
	 * {@link CrossSection CrossSections} with this CrossSectionElementTypology;
	 * false otherwise
	 * @param couplingPriority Integer; Desirability of linking 
	 * {@link CrossSectionElement CrossSectionElements} with this
	 * {@link CrossSectionElementTypology} at {@link CrossSection} boundaries.
	 * Lower values indicate a higher desirability
	 */
	public void addCrossSectionElementTypology(String name, boolean drivable, int couplingPriority) {
		CrossSectionElementTypology cset = new CrossSectionElementTypology(name, drivable, couplingPriority);
		// If one with the same name exists; replace it.
		for (CrossSectionElementTypology checkCSET : crossSectionElementTypologyList) {
			if (checkCSET.getName_r().equals(name)) {
				crossSectionElementTypologyList.set(crossSectionElementTypologyList.indexOf(checkCSET), cset);
				setModified();
				return;
			}
		}
		crossSectionElementTypologyList.add(cset);
		setModified();	// probably not needed;
	}
	
	/**
	 * Retrieve the list of known {@link RoadMarkerAlongTemplate 
	 * RoadMarkerAlongTemplates}.
	 * @return List<RoadMarkerAlongTemplate>; list or RoadMarkerAlongTemplates
	 * known in this Network.
	 */
	public final List<RoadMarkerAlongTemplate> getRoadMarkerTemplateList() {
		rebuild();
		return roadMarkerAlongTemplates;
	}

	/** 
	 * Add a {@link RoadMarkerAlongTemplate} to the list known in this Network
	 * @param type String; type of the RoadMarkerAlongTemplate
	 * @param width Double; width of the RoadMarkerAlongTemplate 
	 */
	public void addRoadMarkerAlongTemplate(String type, double width) {
		roadMarkerAlongTemplates.add(new RoadMarkerAlongTemplate(type, width));
	}
	
	/**
	 * Paint the {@link Link Links} of this Network on a {@link GraphicsPanel}.
	 * @param graphicsPanel GraphicsPanel; output "device" to draw onto
	 * @param showOnlyDrivable Boolean; if true only drive able
	 * CrossSectionElements will be drawn; if false all CrossSectionElements
	 * will be drawn
	 * @param showLaneIDs Boolean; if true, the Lane IDs are drawn; if false,
	 * the lane IDs are not drawn
	 */
    private void paintLinks(GraphicsPanel graphicsPanel, boolean showOnlyDrivable, boolean showLaneIDs) {
        for (Link link :  getLinkList())
        	for (CrossSection cs : link.getCrossSections_r())
                for (CrossSectionElement cse : cs.getCrossSectionElementList_r())
                    if ((! showOnlyDrivable) || cse.getCrossSectionElementTypology().getDrivable())
                    	cse.paint(graphicsPanel);
        if (showLaneIDs) // These must be drawn last; else some might be invisible
            for (Link link :  getLinkList())
            	for (CrossSection cs : link.getCrossSections_r())
                    for (CrossSectionElement cse : cs.getCrossSectionElementList_r())
                		for (CrossSectionObject cso : cse.getCrossSectionObjects(Lane.class))
                			((Lane) cso).paintID(graphicsPanel);
    }    
    
    private void paintActivityLocations(GraphicsPanel graphicsPanel)
    {
        for (ActivityLocation activityLocation : getActivityLocationList())
        	activityLocation.paint(graphicsPanel);
    }

    private void paintPolyZones(GraphicsPanel graphicsPanel)
    {
        for (PolyZone polyZone : getPolyZoneList())
        	polyZone.paint(graphicsPanel);
    }
    
    private void paintMicroZones(GraphicsPanel graphicsPanel)
    {
        for (MicroZone microZone : getMicroZoneList())   {
        	microZone.paint(graphicsPanel);
        }
    }
    
    private void paintNodes(GraphicsPanel graphicsPanel)
    {
        for (Node node : getAllNodeList(true))
        	node.paint(graphicsPanel);
    }
    
    /**
     * Draw the path from startNode to endNode.
     * <br />
     * If startNode or endNode is null; this method does nothing.
     * @param graphicsPanel GraphicsPanel; output device to draw onto
     */
    /*public void paintPaths(GraphicsPanel graphicsPanel) {
    	if ((null == startNode) || (null == endNode))
    		return;
    	int rank = 0;
    	int startNodeIndex = -1;
    	int endNodeIndex = -1;
    	for (Node node : getAllNodeList(true)) {
    		if (node == startNode)
    			startNodeIndex = rank;
    		if (node == endNode)
    			endNodeIndex = rank;
    		rank++;
    	}
    	if (startNodeIndex < 0)
    		throw new Error("Cannot find rank of node " + startNode.toString());
    	if (endNodeIndex < 0)
    		throw new Error("Cannot find rank of node " + endNode.toString());
       // ShortestPathAlgorithms.PathNodesList path = ShortestPathAlgorithms.CreatePaths.pathFromTo[startNodeIndex][endNodeIndex];
        // TODO Guus: aanpassen!!!!
    	IntBuffer path = ShortestPathAlgorithms.CreatePaths.getPathFromToInt()[startNodeIndex][endNodeIndex];
        if (null != path) {
            graphicsPanel.setColor(Color.ORANGE);
            graphicsPanel.setStroke(6f);
            // FIXME: use a descriptive name instead of i
            int i = 0;
        	int from = path.get(i);
        	i++;
        	Node nodeFrom = lookupNode(from, true);
        	Node nodeTo = null;     	
        	while (path.get(i) != ShortestPathAlgorithms.DijkstraAlgorithm.ENDBUFFER) {
	        	nodeTo = lookupNode(path.get(i), true);
            	if (from >= 0)  {
            		graphicsPanel.drawLine(nodeFrom.getPoint(), nodeTo.getPoint());
            	}
            	nodeFrom = nodeTo;
        		i++;
        	}
        }
    }*/

	private static volatile boolean reBuilding = false;
	/**
	 * Clear, then rebuild the automatically generated geometry at junctions.
	 * <br />
	 * If the {@link #dirty} flag is cleared (false) this method returns
	 * immediately; if the flag is set the geometry at junctions is removed
	 * and re-created and if no errors occurr during that process the dirty
	 * flag is cleared. 
	 */
	public void rebuild() {
		if (reBuilding)
			return;
		if (dirty) {
			reBuilding = true;
			System.out.println("Network rebuild started");
			try {
				deleteAutoGeneratedLinks();
			    expandedNodeList = new ArrayList<Node>();
			    // adding some plain CrossSection Element-info
				fixLinksPhase1();
				// determine the links for every node and define a "node boundary circle"
				fixNodeAreas();
				// within a link: connect all successive cross sections by cs-element (weglaten???)
				fixParallelSectionElements();
				// add junction connection lanes (generates new lanes and links)
				fixNodes();
				// create the RMA vertices 
				// create lanes for the first time, with lateral connections (right or left neighbours)
				// and initial centerlines
				// still without connections between successive cross sections
				rebuildLanes();
				// connect the connections between links 
				// at a node with more than two links a junction is generated!				
				fixLinkConnections();
				// connect the connections of lanes and CSEs between cross sections at a link
				fixConnectionLanesAtLink();
				// Create  a smooth and correct geometry
				fixCSEVertices();				
				// Still unused: calculate link length
				fixLinkLengths();
				
				for (TrafficLightController tlc : trafficLightControllers.values())
					tlc.fixClients();
			} catch (Exception e) {
				System.err.println("Network rebuild failed");
				e.printStackTrace();
			}
			System.out.println("Network rebuild finished");
			reBuilding = false;
		}
		dirty = false;
	}
	
	private void deleteAutoGeneratedLinks() {
		HashMap<String,Link> newList = new HashMap<String, Link>();
	    for (Iterator<Link> iter = links.values().iterator(); iter.hasNext();) {
	        Link link = iter.next();
	        if (! (link.isAutoGenerated()))
	        	newList.put(link.getName_r(), link);
	    }
	    links = newList;
	}
	
	/*
	 * How should the fix up phase at nodes work?
	 * 1: Connect (extend/reduce) drive-able CrossSectionElements in a credible manner
	 * 2: Align (wrap) the non-drive-able CrossSectionElements around the drive-able CrossSectionElements
	 * Currently it does neither task very well...
	 * 
	 */
	
	private void fixLinksPhase1() {
		for (Link link : links.values())
			link.fixPhase1();
	}
	
	private void fixNodeAreas() {
		// Determine the maximum node ID in this Network
		nodeMaxId = -1;
		for (Node node : nodes.values())
			if (node.getNodeID() > nodeMaxId)
				nodeMaxId = node.getNodeID();
		// rebuild the list of DirectionalLinks at this node
		for (Node node : nodes.values()) {
			node.clearLinks();
			for (Link link : links.values()) {
				if (link.getFromNode_r() == node)
					node.addLink(link, false);
				if (link.getToNode_r() == node)
					node.addLink(link, true);
			}
			if (node.getLinks_r().size() > 2)
				node.determineNodeBoundary(false);		
		}
	}
	
	private void fixNodes() {
		for (Node node : nodes.values()) {
			// Only for junctions (more than two links at one node) 
			// create directional links and close holes
			if (node.getLinks_r().size() > 2)   {
				node.fixGeometry();
				node.closeHoles();
			}
		}
	}
	
	
	/**
	 * Return the next available (unique) node ID.
	 * @return Integer; suitable to use as ID for a new Node
	 */
	public int nextNodeID() {
		return ++nodeMaxId;
	}
	
	/**
	 * Add Node created by junction expansion to the Network. This method may
	 * only be called during a network rebuild.
	 * @param node Node that must be added
	 */
	public void addExpandedNode(Node node) {
		if (! reBuilding)
			throw new Error("addExpandedNode should only be called during rebuild");
		expandedNodeList.add(node);
	}

	/**
	 * Each section may have several section elements parallel to each other (road, barrier, grass)
	 * The geometry of these parallel section elements is generated here.
	 * If the section make angles, the common section points (start and end) 
	 * should be connected smoothly. This necessitates some corrections.
	 */	
    private void fixParallelSectionElements() {
		if (! reBuilding)
			throw new Error("addExpandedNode should only be called during rebuild");
		for (Link link : links.values()) {
			CrossSection prevCS = null;
			for (CrossSection cs : link.getCrossSections_r()) {
	    		if (null != prevCS)	 {   			
	    			prevCS.linkToCrossSection(cs);	    			    			
	    		}
    			prevCS = cs;
			}
		}
    }
    
    /**
     * Each cross section will have road elements
     * Every road element should have a drive-able area
     * If there are road markers along, the lanes are created here!
     * TODO update the next comment line when / if that is done
     * The geometry will be added later!
     */
    private void rebuildLanes() {
    	for (Link link : links.values())
    		link.rebuildLanes();
    }
    
    private void fixLinkConnections() {
		for (Node node : getNodeList(false))
			node.fixLinkConnections();
	}

    private void fixConnectionLanesAtLink() {
        for (Link link : getLinkList())
        	Link.connectSuccessiveLanesAtLink(link.getCrossSections_r());
        	
		for (Node node : getNodeList(false))
			Link.connectSuccessiveLanesAtNode(node);
    }

    private void fixCSEVertices()  {
    	for (Link link : getLinkList())   {
    		//if (! link.isAutoGenerated())  {
	    		for (CrossSection cs : link.getCrossSections_r())  {
	    			for (CrossSectionElement cse : cs.getCrossSectionElementList_r()) {
	    				cse.setVerticesInner(cse.createAndCleanLinkPointListInner(false, true, false));
	    				cse.setVerticesOuter(cse.createAndCleanLinkPointListOuter(false, true, false));         		
	    			}
	    		}
    		//}
    	}
    	
    	for (Node node : getNodeList(false))  {
    		ArrayList<CrossSection> CSList = CrossSection.getCrossSectionsAtNode(node);
    		if (CSList != null)  {
	    		if (CSList.size() == 2) {
		    		for (CrossSectionElement cse : CSList.get(CSList.size()-1).getCrossSectionElementList_r()) {
						ArrayList<Vertex> verticesInner = cse.createAndCleanLinkPointListInner(false, true, true);
						//cse.setVerticesInner(verticesInner);
						cse.getVerticesInner().set(0, verticesInner.get(0));
						ArrayList<Vertex> prevVerticesInner = cse.getConnectedFrom().getVerticesInner();
						prevVerticesInner.get(prevVerticesInner.size()-1).setPoint(verticesInner.get(0));
						ArrayList<Vertex> verticesOuter = cse.createAndCleanLinkPointListOuter(false, true, true);
						//cse.setVerticesOuter(verticesOuter);
						cse.getVerticesOuter().set(0, verticesOuter.get(0));
						ArrayList<Vertex> prevVerticesOuter = cse.getConnectedFrom().getVerticesOuter();
						prevVerticesOuter.get(prevVerticesOuter.size()-1).setPoint(verticesOuter.get(0));
		    		}
	    		}
    		}
    	}
        	
    	for (Link link : getLinkList())   {
    		if (! link.isAutoGenerated())  {
    			ArrayList<CrossSectionObject> prevRMAList = new ArrayList<CrossSectionObject>();
    			ArrayList<CrossSectionObject> prevLaneList = new ArrayList<CrossSectionObject>();
	    		for (CrossSection cs : link.getCrossSections_r())  {
	    			for (CrossSectionElement cse : cs.getCrossSectionElementList_r()) {
						ArrayList<CrossSectionObject> RMAList = new ArrayList<CrossSectionObject>();
						ArrayList<CrossSectionObject> LaneList = new ArrayList<CrossSectionObject>();		
						RMAList = cse.getCrossSectionObjects(RoadMarkerAlong.class);
						LaneList = cse.getCrossSectionObjects(Lane.class);
						if (prevRMAList != null)  {
							if (! (prevRMAList.isEmpty() || RMAList.isEmpty() ) ) {
								CrossSectionElement.fixRoadMarkerPoint(prevRMAList, RMAList, prevLaneList, LaneList);
								prevRMAList.clear();
								prevLaneList.clear();
							}
						}
						if (! RMAList.isEmpty())
							prevRMAList.addAll(RMAList);
						if (! LaneList.isEmpty())
							prevLaneList.addAll(LaneList);
	    			}
	    		}
    		}
    	}
    }
    
    private void fixLinkLengths() {
    	for (Link link : links.values()) {
    		link.calculateLength(link.getVertices());
    		// TODO: do something useful ...
    		//double distance = link.getLength();
    	}
    	
    }
    
    /**
     * Look for a {@link Link} near a point.
     * <br />
     * The matched Link is assigned to {@link #selectedLink}. If no Link is
     * found, the value of selectedLink is not changed.
     * @param graphicsPanel GraphicsPanel on which the point is located
     * @param p Point2D.Double the point
     * @return Double; distance to the Link or Double.MAX_VALUE if no link
     * was found near p
     */
    public double SelectLink(GraphicsPanel graphicsPanel, Point2D.Double p) {
    	//System.out.println(String.format("Searching Link near %f,%f", p.x, p.y));
    	final int maxDistance = 10; // pixels
    	double bestDistance = Double.MAX_VALUE;
    	
    	for (Link link : getLinkList()) {
    		Point2D.Double prevPoint = null;
			double cumulativeLongitudinalDistance = 0;
    		for (Vertex v : link.getVertices()) {
    			Point2D.Double point = graphicsPanel.translate(v.getPoint());
    			if (prevPoint != null) {
    				double distance = Planar.distanceLineSegmentToPoint(new Line2D.Double(prevPoint, point), p);
    				if ((distance < maxDistance) && (distance < bestDistance)) {
    					selectedLink = link;
    					bestDistance = distance;
    					selectedCrossSection = null;
    					// figure out the crossSection nearest this point
    					Line2D.Double nearestLine = new Line2D.Double(prevPoint, point);
    					Point2D.Double nearestPoint = Planar.nearestPointOnLine(nearestLine, p);
    					double thisLongitudinalDistance = cumulativeLongitudinalDistance + prevPoint.distance(nearestPoint);
    					for (CrossSection cs : link.getCrossSections_r()) {
    						System.out.println("cs: " + cs.toString());
    						if (cs.getLongitudinalPosition_r() <= thisLongitudinalDistance) {
    							selectedCrossSection = cs;
    							popupLongitudinalDistance = thisLongitudinalDistance;
    						} else
    							break;
    					}
    					System.out.println("selectedCrossSection " + selectedCrossSection.toString());
    				}
    				cumulativeLongitudinalDistance += prevPoint.distance(point);
    			}
    			prevPoint = point;
    		}
    	}
    	return bestDistance;
    }
  
    /**
     * Look for a {@link Link} near a point.
     * <br />
     * The matched Link is assigned to {@link #selectedLink}. If no Link is
     * found, the value of selectedLink is not changed.
     * @param graphicsPanel GraphicsPanel on which the point is located
     * @param p Point2D.Double the point
     * @return Double; distance to the Link or Double.MAX_VALUE if no link
     * was found near p
     */
    public double SelectLane(GraphicsPanel graphicsPanel, Point2D.Double p) {
    	//System.out.println(String.format("Searching Link near %f,%f", p.x, p.y));
    	double bestDistance = Double.MAX_VALUE;
    	
    	for (Lane lane: createLaneList()) {
    		GeneralPath path = lane.createLanePolygon();    		
    		if (graphicsPanel.translatePath(path).contains(p)) {
				selectedLane = lane;
				bestDistance = 0.0;
				System.out.println("selectedCrossSection " + selectedLane.toString());
    		}
    	}
        if (null == selectedLane)
        	System.out.println(String.format("No lane found near %f,%f", p.x, p.y));
    	return bestDistance;
    }
 
    /**
     * Look for a {@link Node} near a point.
     * <br />
     * The matched Node is assigned to {@link #selectedNode}. If no Node is
     * found, the value of selectedNode is not changed.
     * @param graphicsPanel GraphicsPanel on which the point is located
     * @param p Point2D.Double the point
     * @return Double; distance to the Link or Double.MAX_VALUE if no link
     * was found near p
     */
    public double SelectNode(GraphicsPanel graphicsPanel, Point2D.Double p) {
    	//System.out.println(String.format("Searching Node near %f,%f (rev %f,%f", p.x, p.y, graphicsPanel.reverseTranslate(p).getX(), graphicsPanel.reverseTranslate(p).getY()));
        final int maxDistance = 10;	// pixels
        double bestDistance = Double.MAX_VALUE;
        Node prevSelectedNode = selectedNode;

        for (Node node : getNodeList(true)) {
            double distance = graphicsPanel.translate(node.getPoint()).distance(p);
    		if ((distance < maxDistance) && (distance < bestDistance)) {
    			selectedNode = node;
                bestDistance = distance;
    		}
        }
        if (null == selectedNode)
        	System.out.println(String.format("No node found near %f,%f", p.x, p.y));
        // Repaint is only required if selected nodes are painted different from non-selected nodes
        // Actually, GraphicsPanel ALWAYS generates a repaint so these two lines are redundant
        if (prevSelectedNode != selectedNode)
        	graphicsPanel.repaint();
        return bestDistance;
    }

    /**
     * Look for a {@link MicroZone} near a point.
     * <br />
     * The matched Node is assigned to {@link #selectedMicroZone}. If no MicroZone is
     * found, the value of selectedMicroZone is not changed.
     * @param graphicsPanel GraphicsPanel on which the point is located
     * @param p Point2D.Double the point
     * @return Double; distance to the MicroZone or Double.MAX_VALUE if no MicroZone 
     * was found near p
     */
    public double SelectMicroZone(GraphicsPanel graphicsPanel, Point2D.Double p) {
    	//System.out.println(String.format("Searching Node near %f,%f (rev %f,%f", p.x, p.y, graphicsPanel.reverseTranslate(p).getX(), graphicsPanel.reverseTranslate(p).getY()));
        final int maxDistance = 10;	// pixels
        double bestDistance = Double.MAX_VALUE;
        MicroZone prevSelectedMicroZone = selectedMicroZone;

        for (MicroZone microZone : getMicroZoneList(true)) {
            double distance = graphicsPanel.translate(microZone.getPoint()).distance(p);
    		if ((distance < maxDistance) && (distance < bestDistance)) {
    			selectedMicroZone = microZone;
                bestDistance = distance;
    		}
        }
        if (null == selectedMicroZone)
        	System.out.println(String.format("No microZone found near %f,%f", p.x, p.y));
        // Repaint is only required if selected nodes are painted different from non-selected nodes
        // Actually, GraphicsPanel ALWAYS generates a repaint so these two lines are redundant
        if (prevSelectedMicroZone != selectedMicroZone)
        	graphicsPanel.repaint();
        return bestDistance;
    }

    /**
     * Look for a {@link MicroZone} near a point.
     * <br />
     * The matched Node is assigned to {@link #selectedMicroZone}. If no MicroZone is
     * found, the value of selectedMicroZone is not changed.
     * @param graphicsPanel GraphicsPanel on which the point is located
     * @param p Point2D.Double the point
     * @return Double; distance to the MicroZone or Double.MAX_VALUE if no MicroZone 
     * was found near p
     */
    public double SelectActivityLocation(GraphicsPanel graphicsPanel, Point2D.Double p) {
    	//System.out.println(String.format("Searching Node near %f,%f (rev %f,%f", p.x, p.y, graphicsPanel.reverseTranslate(p).getX(), graphicsPanel.reverseTranslate(p).getY()));
        final int maxDistance = 10;	// pixels
        double bestDistance = Double.MAX_VALUE;
        ActivityLocation prevSelectedMicroZone = selectedActivityLocation;

        for (ActivityLocation activityLocation : getActivityLocationList()) {
            double distance = graphicsPanel.translate(activityLocation.getPoint()).distance(p);
    		if ((distance < maxDistance) && (distance < bestDistance)) {
    			selectedActivityLocation = activityLocation;
                bestDistance = distance;
    		}
        }
        if (null == selectedActivityLocation)
        	System.out.println(String.format("No microZone found near %f,%f", p.x, p.y));
        // Repaint is only required if selected nodes are painted different from non-selected nodes
        // Actually, GraphicsPanel ALWAYS generates a repaint so these two lines are redundant
        if (prevSelectedMicroZone != selectedActivityLocation)
        	graphicsPanel.repaint();
        return bestDistance;
    }

    
    /**
     * Draw this Network onto a {@link GraphicsPanel}.
     */
	@Override
	public void repaintGraph(GraphicsPanel graphicsPanel) {
		Main mf = Main.mainFrame;
        if (mf.showLinks.isSelected())
        	paintLinks(graphicsPanel, mf.showDrivable.isSelected(), mf.showLaneIDs.isSelected());
        if (mf.showNodes.isSelected())
        	paintNodes(graphicsPanel);
        //if (mf.showPaths.isSelected()) 
          //  paintPaths(graphicsPanel);
        if (mf.showBuildings.isSelected()) 
            paintActivityLocations(graphicsPanel);
        if (mf.showPolyZones.isSelected()) {
            paintPolyZones(graphicsPanel);
        	paintMicroZones(graphicsPanel);
        }
	}
	
	private Point2D.Double mouseDown = null;
	private GraphicsPanel popupGraphicsPanel = null;
	private ObjectInspector objectInspector = null;

	@Override
	public void mousePressed(GraphicsPanel graphicsPanel, MouseEvent evt) {
        if (null != mouseDown) {// Ignore mouse presses that occur
            return;            	//  when user is already drawing a curve.
        }//    (This can happen if the user presses two mouse buttons at the same time.)
        
    	if (null != objectInspector) {
    		objectInspector.dispose();
    		objectInspector = null;
    	}
    	if (evt.getButton() == MouseEvent.BUTTON1) {
	        mouseDown = new Point2D.Double(evt.getX(), evt.getY());
    		if (creatingLink) {
    			if (SelectNode(graphicsPanel, mouseDown) != Double.MAX_VALUE) {
    				if (selectedNode == popupNode) {
    					Main.mainFrame.setStatus(-1, "A link can not connect to it's own origin");
    					return;
    				}
    				// Create a new link
    				// Generate a unique name
    				String name;
    				for (int i = 1; ; i++) {
    					name = "link_" + i;
    					if (null == lookupLink(name))
    						break;
    				}
    				ArrayList<CrossSectionElement> sectionElementList = new ArrayList<CrossSectionElement>();
    				CrossSection crossSection = new CrossSection(0, 0, sectionElementList);
            		ArrayList<RoadMarkerAlong> rmaList = new ArrayList<RoadMarkerAlong>();
    				sectionElementList.add(new CrossSectionElement(crossSection, "road", 4.5, rmaList, null));
    				ArrayList<CrossSection> crossSections = new ArrayList<CrossSection>();
    				crossSections.add(crossSection);
    				addLink (name, popupNode.getNodeID(), selectedNode.getNodeID(), 0, false, crossSections, new ArrayList<Vertex>());
    				creatingLink = false;
    			}
    			// else ignore this mouse click
    		} else {
		        repaintComponent = (Component) evt.getSource();
		        System.out.println(String.format(Main.locale, "Mouse down at %.0f,%.0f", mouseDown.x, mouseDown.y));
		        // First check if the click is near a node
		        if (SelectMicroZone(graphicsPanel, mouseDown) != Double.MAX_VALUE) {
		        	objectInspector = new ObjectInspector(selectedMicroZone, this);
		        	return;
		        } 
		        if (SelectActivityLocation(graphicsPanel, mouseDown) != Double.MAX_VALUE) {
		        	objectInspector = new ObjectInspector(selectedActivityLocation, this);
		        	return;
		        } 
		        if (SelectNode(graphicsPanel, mouseDown) != Double.MAX_VALUE) {
		        	objectInspector = new ObjectInspector(selectedNode, this);		        	
		        	return;
		        } 
		        // No node found; check if the click is near a lane
		        if (SelectLane(graphicsPanel, mouseDown) != Double.MAX_VALUE) {
		        	objectInspector = new ObjectInspector(selectedLane, this);
		        	return;
		        }
		        // No node and lane found; check if the click is near a link
		        if (SelectLink(graphicsPanel, mouseDown) != Double.MAX_VALUE) {
		        	objectInspector = new ObjectInspector(selectedLink, this);
		        	return;
		        } 

    		}
        // No link found either
    	}
        tryPopup(graphicsPanel, evt);
	}
	
	private void tryPopup(GraphicsPanel graphicsPanel, MouseEvent evt) {
        if (evt.isPopupTrigger()) {
        	System.out.println("event is popup trigger");
            mouseDown = new Point2D.Double(evt.getX(), evt.getY());
            popupGraphicsPanel = graphicsPanel;
        	JPopupMenu popupMenu = new JPopupMenu();
        	boolean onNode = SelectNode(graphicsPanel, mouseDown) != Double.MAX_VALUE;
        	popupNode = selectedNode;
        	boolean onLink = SelectLink(graphicsPanel, mouseDown) != Double.MAX_VALUE;
        	popupLink = selectedLink;
        	popupCrossSection = selectedCrossSection;
        	//System.out.println("selected cs: " + selectedCrossSection.toString());
        	addMenuItem(popupMenu, "Add node", "addNode", !onNode, null);
        	addMenuItem(popupMenu, "Set start node", "setStartNode", onNode, null);
        	addMenuItem(popupMenu, "Set end node", "setEndNode", onNode, null);
        	addMenuItem(popupMenu, "Delete node", "deleteNode", onNode && (popupNode.getLinks_r().size() == 0), null);
        	addMenuItem(popupMenu, "Add link", "addLink", onNode, null);
        	addMenuItem(popupMenu, "Insert cross section", "insertCrossSection", onLink, null);
        	JMenu subMenu = new JMenu("Modify cross section element");
        	subMenu.setEnabled(onLink);
        	popupMenu.add(subMenu);
        	if (null != selectedCrossSection)
	        	for (CrossSectionElement cse : selectedCrossSection.getCrossSectionElementList_r())
	        		addMenuItem(subMenu, cse.toString(), "editCrossSectionElement", true, cse);
        	addMenuItem(popupMenu, "Delete cross section", "deleteCrossSection", onLink && (popupLink.getCrossSections_r().size() > 1), null);
        	popupMenu.show(evt.getComponent(), evt.getX(), evt.getY());
        }		
	}
	
	private JMenuItem addMenuItem(JComponent parent, String caption, String actionCommand, boolean enabled, Object object) {
		JMenuItem newItem;
		if (null == object)
			newItem = new JMenuItem(caption);
		else
			newItem = new MenuItemWithObject(caption, object);
		newItem.setActionCommand(actionCommand);
		newItem.setEnabled(enabled);
		newItem.addActionListener(this);
		parent.add(newItem);
		return newItem;
	}
	
	class MenuItemWithObject extends JMenuItem {
		private static final long serialVersionUID = 1L;

		Object object;
		
		MenuItemWithObject(Object object) {
			super();
			this.object = object;
		}
		
		MenuItemWithObject(String caption, Object object) {
			super(caption);
			this.object = object;
		}
	}
	
	@Override
	public void mouseDragged(GraphicsPanel graphicsPanel, MouseEvent evt) {
        if (null == mouseDown)
            return;			// processing a gesture

        Point2D.Double position = new Point2D.Double(evt.getX(), evt.getY());
        repaintComponent = (Component) evt.getSource();

        if ((null != selectedNode) && (! Main.mainFrame.showPaths.isSelected())) {
            Point2D.Double reverseTranslated = graphicsPanel.reverseTranslate(position);
            //System.out.println(String.format("pos=%f,%f, revtrans=%f,%f (is new node location)", position.x, position.y, reverseTranslated.x, reverseTranslated.y));
            selectedNode.setPoint(reverseTranslated);
            setModified();
        } 
        else if ((null != selectedLane) && (! Main.mainFrame.showPaths.isSelected())) {
           /* Point2D.Double reverseTranslated = graphicsPanel.reverseTranslate(position);
            //System.out.println(String.format("pos=%f,%f, revtrans=%f,%f (is new node location)", position.x, position.y, reverseTranslated.x, reverseTranslated.y));
            selectedlane.setPoint(reverseTranslated);
            setModified();*/
        }
        else
            ((GraphicsPanel) evt.getSource()).addPan(position.x - mouseDown.x, position.y - mouseDown.y);
        // Update "current" position
        mouseDown = position;
	}

	@Override
	public void mouseReleased(GraphicsPanel graphicsPanel, MouseEvent evt) {
        mouseDown = null;
        selectedNode = null;
//        selectedLane = null;
        selectedActivityLocation = null;
//        selectedMicroZone = null;
        tryPopup(graphicsPanel, evt);
	}
	
	private Node popupNode = null;
	private Link popupLink = null;
	private CrossSection popupCrossSection = null;
	private double popupLongitudinalDistance = 0;
	boolean creatingLink = false;

	@Override
	public void mouseMoved(GraphicsPanel graphicsPanel, MouseEvent evt) {
		Point2D.Double mousePosition = new Point2D.Double(evt.getX(), evt.getY());
		Point2D.Double reversePosition = graphicsPanel.reverseTranslate(mousePosition);
		if (creatingLink) {
			if ((SelectNode(popupGraphicsPanel, mousePosition) == Double.MAX_VALUE) || (selectedNode == popupNode))
				Main.mainFrame.setStatus(-1, "Select target node for new link");
			else
				Main.mainFrame.setStatus(-1, "Click to set target for new link to %s (%d)", selectedNode.getName_r(), selectedNode.getNodeID());
		} else {
			Main.mainFrame.setStatus(-1, "Mouse pointer at %.2f,%.2f", reversePosition.x, reversePosition.y);
		}
	}

	@Override
	public void setModified() {
		dirty = true;
		modified = true;
		if(null != repaintComponent)
			repaintComponent.repaint();
	}

	private void reFocus() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				popupGraphicsPanel.requestFocusInWindow();
				//System.out.println("Focus requested for popupGraphicsPanel");
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent actionEvent) {
		System.out.println("Network:actionPerformed: " + actionEvent.getActionCommand());
		String command = actionEvent.getActionCommand();
		if ("addNode".equals(command)) {
			Point2D.Double where = popupGraphicsPanel.reverseTranslate(mouseDown);
			System.out.println("Add node at " + where.toString());
			// generate a unique name
			String name;
			for (int i = 1; ; i++) {
				name = "node_" + i;
				if (null == lookupNode(name, true))
					break;
			}
			// generate a unique nodeID
			int nodeID = 1;
			while (null != lookupNode(nodeID, true))
				nodeID++;
			addNode(name, nodeID, where.x, where.y, 0);
		} else if ("addLink".equals(command)) {
			creatingLink = true;
			mouseDown = null;	// Don't you forget it! (MouseReleased event was consumed by the popupMenu)
		} else if ("setStartNode".equals(command))
			startNode = popupNode;
		else if ("setEndNode".equals(command))
			endNode = popupNode;
		else if ("deleteNode".equals(command))
			deleteNode(popupNode);
		else if ("insertCrossSection".equals(command))
			popupCrossSection.duplicateCrossSection_2(popupLongitudinalDistance);
		else if ("deleteCrossSection".equals(command))
			popupCrossSection.deleteCrossSection_d();
		else if ("editCrossSectionElement".equals(command)) {
			ArrayList<Object> path = new ArrayList<Object>();
			//path.add(popupLink);
			path.add(popupLink.getCrossSections_r());
			path.add(popupCrossSection);
			path.add(popupCrossSection.getCrossSectionElementList_r());
			path.add(((MenuItemWithObject) (actionEvent.getSource())).object);
			//Object source = actionEvent.getSource();
	    	if (null != objectInspector) {
	    		objectInspector.dispose();
	    		objectInspector = null;
	    	}
	    	objectInspector = new ObjectInspector(popupLink, this);
	    	objectInspector.expandPath(path);
		} else
			System.out.println("Unhandled ActionEvent" + command);
		reFocus();
		mouseDown = null;
		popupGraphicsPanel.repaint();
	}

	/**
	 * Export the lanes of this Network as a long String.
	 * @return String; the textual representation of the lanes in this Network
	 */
	public String exportLanes() {
    	String result = "";
	   	List<Lane> laneList = createLaneList();
   		result += String.format("Section LaneGeom\n");
		for(Lane lane : laneList) {
			result += String.format("Lane\tlaneID:\t%d\tXY:", lane.getID());
    		for (Vertex coord : lane.getLaneVerticesCenter())
				result += String.format(Locale.US, "\t%.2f,%.2f", coord.getX(), coord.getY()); 
 			result += "\n";
		}
   		result += String.format("Section LaneData\n");
 
		for(Lane lane : laneList) {
			result += String.format("LaneData\tlaneID:\t%d", lane.getID());

			for (Integer laneID : lane.getUpLaneIDs())				
				result += String.format("\tup:\t%d", laneID); 
			for (Integer laneID : lane.getDownLaneIDs())
				result += String.format("\tdown:\t%d", laneID); 
			if (lane.getCrossingYieldToLaneList() != null)  {
				for (Lane yLane : lane.getCrossingYieldToLaneList())  {
					result += String.format("\tcrossingYieldTo:\t%d", yLane.getID()); 
				}
			}
			if (lane.getMergingYieldToLaneList() != null)  {
				for (Lane yLane : lane.getMergingYieldToLaneList())  {
					result += String.format("\tmergingYieldTo:\t%d", yLane.getID()); 
				}
			}
			if (lane.getLeft() != null)
				result += String.format("\tleft:\t%d", lane.getLeft().getID()); 
			if (lane.getRight() != null)
				result += String.format("\tright:\t%d", lane.getRight().getID()); 
			if (lane.isGoLeft())
				result += String.format("\tgoLeft:\t%s", lane.isGoLeft()); 
			if (lane.isGoRight())
				result += String.format("\tgoRight:\t%s", lane.isGoRight()); 
			if (lane.getOrigin() >= 0)
				result += String.format("\torigin:\t%d", lane.getOrigin()); 
			if (lane.getDestination() >= 0)
				result += String.format("\tdestination:\t%d", lane.getDestination()); 
			result += "\n";
		}
   		result += String.format(Main.locale, "Section TrafficDemand\n");
   		int maxMicroZones = getMicroZoneList().size(); 
   		result += String.format(Main.locale, "NumberOfZones:\t%s\n", maxMicroZones);
		return result;
	}

	/**
	 * Create a text description of all {@link VehicleDetector VehicleDetectors}
	 * in this Network.
	 * <br /> For each detector one line of text is produced consisting of the 
	 * word <pre>Detector</pre>, a tab character, the name of the detector,
 	 * another tab character and the tab-separated X and Y coordinates of the
 	 * vertices of the outline of the detector.
	 * @return String; one line per detector
	 */
	public String exportDetectors() {
		String result = "";
		for (Link link : getLinkList())
			for (CrossSection cs : link.getCrossSections_r())
				for (CrossSectionElement cse : cs.getCrossSectionElementList_r())
					for (CrossSectionObject vdcso : cse.getCrossSectionObjects(VehicleDetector.class)) {
						VehicleDetector vd = (VehicleDetector) vdcso;
						result += "Detector\t" + vd.getID_r() + "\t";
						String separator = "";
						for (CrossSectionObject lcso : cse.getCrossSectionObjects(Lane.class)) {
							Lane lane = (Lane) lcso;
							// Add only those lanes whose center line intersects the detector
							if (Planar.polyLineIntersectsPolyLine(Planar.getAlignment(lane.getLaneVerticesCenter()), Planar.closePolyline(Planar.getAlignment(vd.getPolygon_r())))) {
								result += String.format(Locale.US, "%s%d %.3f %.3f", separator, lane.getID(), vd.longitudinalPosition, vd.longitudinalLength);
								separator = ",";
							}
						}
						ArrayList<Vertex> vertices = vd.getPolygon_r();
						if (vertices.size() > 0)
							for (Vertex v : vertices)
								result += String.format(Locale.US, "\t%.2f\t%.2f", v.x, v.y);
						result += "\n";
					}
		return result;
	}
	
	/**
	 * Retrieve a list of all {@link TrafficLight TrafficLights} in this Network.
	 * @return ArrayList&lt;{@link TrafficLight}&gt;; the list of all TrafficLights in this Network
	 */
	public ArrayList<TrafficLight> getTrafficLights() {
		ArrayList<TrafficLight> result = new ArrayList<TrafficLight> ();
		for (Link link : getLinkList())
			for (CrossSection cs : link.getCrossSections_r())
				for (CrossSectionElement cse : cs.getCrossSectionElementList_r())
					for (CrossSectionObject cso : cse.getCrossSectionObjects(TrafficLight.class))
						result.add((TrafficLight) cso);
		return result;
	}

	/**
	 * Lookup a TrafficLight by ID
	 * @param id String; id of the TrafficLight
	 * @return {@link TrafficLight}; the TrafficLight with the specified ID, or
	 * null if no such TrafficLight exists in this Network
	 */
	public TrafficLight lookupTrafficLight(String id) {
		for (TrafficLight tl : getTrafficLights())
			if (tl.getID_r().equals(id))
				return tl;
		return null;
	}
	
	/**
	 * Retrieve a list of all {@link VehicleDetector VehicleDetectors} in this Network.
	 * @return ArrayList&lt;{@link VehicleDetector}&gt;; the list of all TrafficLights in this Network
	 */
	public ArrayList<VehicleDetector> getVehicleDetectors() {
		ArrayList<VehicleDetector> result = new ArrayList<VehicleDetector> ();
		for (Link link : getLinkList())
			for (CrossSection cs : link.getCrossSections_r())
				for (CrossSectionElement cse : cs.getCrossSectionElementList_r())
					for (CrossSectionObject cso : cse.getCrossSectionObjects(VehicleDetector.class))
						result.add((VehicleDetector) cso);
		return result;
	}

	/**
	 * Lookup a VehicleDetector by ID
	 * @param id String; id of the VehicleDetector
	 * @return {@link VehicleDetector}; the VehicleDetector with the specified 
	 * ID, or null if no such VehicleDetector exists in this Network
	 */
	public VehicleDetector lookupVehicleDetector(String id) {
		for (VehicleDetector vd : getVehicleDetectors())
			if (vd.getID_r().equals(id))
				return vd;
		return null;
	}
	
	/**
	 * Retrieve a {@link TrafficLightController} by specifying its ID.
	 * @param id String; id of the {@link TrafficLightController} to find
	 * @return {@link TrafficLightController}; the TrafficLightController with
	 * the specified ID, or null if no such TrafficLightController exists in
	 * this Network
	 */
	public TrafficLightController lookupTrafficLightController (String id) {
		return trafficLightControllers.get(id);
	}
	
	/**
	 * Retrieve the set of the names of all {@link TrafficLightController 
	 * Controllers} in this Network
	 * @return Set&lt;String&gt;; the set of names of all 
	 * {@link TrafficLightController TrafficLightControllers} in this Network
	 */
	public Set<String> trafficControllerList () {
		return trafficLightControllers.keySet();
	}

	/**
	 * Add a {@link TrafficLightController} to this Network.
	 * <br /> It is (currently) not possible to specify (or change) the name
	 * of a TrafficLightController.
	 * @return {@link TrafficLightController}; the newly created
	 * TrafficLightController.
	 */
	public TrafficLightController addTrafficLightController() {
		String prefix = "TLC_";
		int rank = 1;
		while (lookupTrafficLightController (prefix + rank) != null)
			rank++;
		TrafficLightController result = new TrafficLightController(this, prefix + rank);
		trafficLightControllers.put(result.getName_r(), result);
		return result;
	}

	/**
	 * Create a text description of all {@link TrafficLight TrafficLights} in
	 * this Network.
	 * <br/>
	 * For each traffic light one line of text is produced consisting of the word
	 * <pre>TrafficLight</pre>, a tab character, the name of the traffic light,
	 * another tab character, the name of the {@link TrafficLightController},
 	 * another tab character and the tab-separated X and Y coordinates of the
 	 * vertices of the outline of the TrafficLight.
	 * @return String; one line per traffic light
	 */
	public String exportTrafficLights() {
		String result = "";
		for (Link link : getLinkList())
			for (CrossSection cs : link.getCrossSections_r())
				for (CrossSectionElement cse : cs.getCrossSectionElementList_r())
					for (CrossSectionObject tlcso : cse.getCrossSectionObjects(TrafficLight.class)) {
						TrafficLight tl = (TrafficLight) tlcso;
						result += "TrafficLight\t" + tl.getID_r() + "\t";
						String separator = "";
						for (CrossSectionObject lcso : cse.getCrossSectionObjects(Lane.class)) {
							Lane lane = (Lane) lcso;
							double position = tl.longitudinalPosition;
							if (position < 0)
								position += Planar.length(tl.crossSectionElement.getLinkPointList(CrossSectionElement.LateralReferenceCenter, true, false));
							result += String.format(Locale.US, "%s%d %.3f", separator, lane.getID(), position);
							separator = ",";
						}
						ArrayList<Vertex> vertices = tl.getPolygon_r();
						if (vertices.size() > 0)
							for (Vertex v : vertices)
								result += String.format(Locale.US, "\t%.2f\t%.2f", v.x, v.y);
						result += "\n";
					}
		return result;
	}

	/**
	 * Create a text description of all {@link TrafficLightController 
	 * TrafficLightControllers} in this Network.
	 * <br /> For each traffic light controller one line of text is produced
	 * consisting of the word <pre>TrafficLightController</pre>, a tab
	 * character, a comma-separated list of detector IDs, a tab character,
	 * a comma-separated list of traffic light IDs, another tab character,
	 * the URL needed to link to the control program.
	 * @return String; one line per traffic light controller
	 */
	public String exportTrafficLightControllers() {
		String result = "";
		for (TrafficLightController tlc : trafficLightControllers.values())
			result += String.format(Locale.US, "TrafficLightController\t%s\t%s\t%s\t%s\n",
					tlc.getName_r(), tlc.getLights(), tlc.getDetectors(), tlc.getControlProgramURL_r());
		return result;
	}

	@Override
	public String description() {
		return "Network topology description";
	}

	@Override
	public String fileType() {
		return FILETYPE;
	}

	/**
	 * Retrieve the <i>modified</i> flag.
	 * @return Boolean; true if the network has been modified; false otherwise
	 */
	@Override
	public boolean isModified() {
		return modified;
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