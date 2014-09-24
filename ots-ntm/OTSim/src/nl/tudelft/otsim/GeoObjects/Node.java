package nl.tudelft.otsim.GeoObjects;

import java.awt.Color;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Locale;
import java.util.SortedSet;
import java.util.TreeSet;

import nl.tudelft.otsim.FileIO.ParsedNode;
import nl.tudelft.otsim.FileIO.StaXWriter;
import nl.tudelft.otsim.FileIO.XML_IO;
import nl.tudelft.otsim.GeoObjects.ExpandUncontrolledIntersection;
import nl.tudelft.otsim.GUI.GraphicsPanel;
import nl.tudelft.otsim.GUI.InputValidator;
import nl.tudelft.otsim.GUI.Main;
import nl.tudelft.otsim.SpatialTools.Circle;
import nl.tudelft.otsim.SpatialTools.Planar;

/**
 * This class encapsulates the data that is specific to a node.
 * 
 * @author Peter Knoppers & Guus F Tamminga
 */
public class Node extends Vertex implements XML_IO {
	/** Name for a Node element when stored in XML format */
	public static final String XMLTAG = "node";
	
	
	/** Name for a Node name element when stored in XML format */
	private static final String XML_NAME = "name";
	/** Name for a Node ID element when stored in XML format */
	private static final String XML_ID = "ID";
	
	private int nodeCount = 0;
	private String name = null;
    private int nodeID = -1;
    private boolean sink = false;
    private boolean source = false;
	private TreeSet<DirectionalLink> links = null;
	private SimplePolygon area = null;
	private ArrayList<ArrayList<Vertex>> closingLines = new ArrayList<ArrayList<Vertex>>();
	private TrafficLightController trafficLightController = null;
	private final Network parentNetwork;
	private Network subNetwork = null;
	private Circle circle;
	private NodeExpander nodeExpander = new ExpandUncontrolledIntersection(this, "");

	/**
	 * Create a new Node.
	 * @param network {@link Network}; network to which the new Node belongs
	 * @param name String; name of the new Node
	 * @param X Double; X-Coordinate of the new Node
	 * @param Y Double; Y-Coordinate of the new Node
	 * @param Z Double; Z-Coordinate of the new Node
	 */
    public Node (Network network, String name, double X, double Y, double Z) {
        super(X, Y, Z);
        this.name = name;
        this.nodeID = nodeCount;
        this.parentNetwork = network;
    }
   
    /**
     * Create a new Node.
	 * @param network {@link Network}; network to which the new Node belongs
	 * @param name String; name of the new Node
     * @param nodeID Integer; to be depreciated
	 * @param X Double; X-Coordinate of the new Node
	 * @param Y Double; Y-Coordinate of the new Node
	 * @param Z Double; Z-Coordinate of the new Node
     */
    public Node (Network network, String name, int nodeID, double X, double Y, double Z) {
    	this(network, name, X, Y, Z);
        this.nodeID = nodeID;
    }
    
	/**
	 * Create a Node from a parsed XML file.
	 * @param parentNetwork {@link Network}; the Network that will own the new Node
	 * @param pn {@link ParsedNode}; root of the Node field in the parsed XML file
	 * @throws Exception
	 */
    public Node(Network parentNetwork, ParsedNode pn) throws Exception {
    	// Initialize everything to some invalid value
    	this.parentNetwork = parentNetwork;
    	x = y = z = Double.NaN;
		for (String fieldName : pn.getKeys()) {
			if (pn.size(fieldName) != 1)
				throw new Exception("Field " + fieldName + " has " + pn.size(fieldName) + "elements (should be 1)");
			if (fieldName.equals(TrafficLightController.XMLTAG)) {
				trafficLightController = new TrafficLightController(parentNetwork, pn.getSubNode(TrafficLightController.XMLTAG, 0));
				continue;
			} 
			String value = pn.getSubNode(fieldName, 0).getValue();
			if (null == value)
				throw new Exception("Value of " + fieldName + " is null");
			if (fieldName.equals(XML_NAME))
				name = value;
			else if (fieldName.equals(XML_ID))
				nodeID = Integer.parseInt(value);
			else if (fieldName.equals(XML_X))
				x = Double.parseDouble(value);
			else if (fieldName.equals(XML_Y))
				y = Double.parseDouble(value);
			else if (fieldName.equals(XML_Z))
				z = Double.parseDouble(value);
			else if (fieldName.equals(Network.XMLTAG))
				subNetwork = new Network(pn.getSubNode(Network.XMLTAG, 0), this);
			else
				throw new Exception("Unknown field in Node: \"" + fieldName + "\"");
		}
		if (null == name)
			throw new Exception("Node has no name " + pn.lineNumber + ", " + pn.columnNumber);
		if (-1 == nodeID)
			throw new Exception("Node has no ID " + pn.lineNumber + ", " + pn.columnNumber);
		if (Double.isNaN(x) || Double.isNaN(y))
			throw new Exception("Node has no valid X and/or Y " + pn.lineNumber + ", " + pn.columnNumber);
	}

	/**
     * Write this Node to an XML file.
     * @param staXWriter {@link StaXWriter}; writer for the XML file
     * @return Boolean; true on success; false on failure
     */
    @Override
	public boolean writeXML (StaXWriter staXWriter) {
		return staXWriter.writeNodeStart(XMLTAG)
				&& staXWriter.writeNode(XML_NAME, getName_r())
				&& staXWriter.writeNode(XML_ID, Integer.toString(getNodeID()))
				&& writeVertexXML(staXWriter)
				&& ((null == trafficLightController) || trafficLightController.writeXML(staXWriter))
				&& ((null == subNetwork) || (subNetwork.writeXML(staXWriter)))
				&& staXWriter.writeNodeEnd(XMLTAG);
    }
    
    /**
     * Retrieve the name of this Node.
     * @return String; the name of this Node
     */
    public String getName_r() {
		return name;
	}

    /**
     * Change the name of this Node.
     * @param nodeName String; new name for this Node
     */
	public void setName_w(String nodeName) {
		this.name = nodeName;
	}
	
	/**
	 * Checker for proposed name of a Node.
	 * @return InputValidator for Node name.
	 */
	public InputValidator validateName_v() {
		return new InputValidator(new InputValidator.CustomValidator() {
			@Override
			public boolean validate(String originalValue, String proposedValue) {
				if (! proposedValue.matches("[a-zA-Z_][-a-zA-Z0-9_.]*"))
					return false;	// not a decent name
				if (proposedValue.equals(originalValue))
					return true;	// current name is OK
				// Anything else must be unique among the node names
				return parentNetwork.lookupNode(proposedValue, false) == null;
			}
		});
	}

	/**
	 * To be depreciated <br />
	 * Retrieve the ID of this Node.
	 * @return Integer; the numerical ID of this Node
	 */
	public int getNodeID() {
		return nodeID;
	}

	/**
	 * Delete all {@link DirectionalLink DirectionalLinks} of this Node.
	 */
	public void clearLinks() {
		links = new TreeSet<DirectionalLink>();
	}
	
	/**
	 * Add one {@link Link} to this Node.
	 * @param link {@link Link} link to add to this Node
	 * @param incoming Boolean; true if the Link points towards this Node;
	 * false if the Link points away from this Node
	 */
	public void addLink(Link link, boolean incoming) {
		links.add(new DirectionalLink(link, incoming));
	}
	
	/**
	 * Retrieve all {@link DirectionalLink DirectionalLinks} of this Node.
	 * @return TreeSet&lt;{@link DirectionalLink}&gt; the set of directional
	 * links of this Node
	 */
	public final TreeSet<DirectionalLink> getDirectionalLinks() {
		return links; 
	}
	
	/**
	 * Retrieve all {@link Link Links} of this Node.
	 * @return ArrayList&lt;{@link Link}&gt; set of Links of this Node
	 */
	public ArrayList<Link> getLinks_r () {
		ArrayList<Link> result = new ArrayList<Link>();
		if (null == links) {
			System.err.println("getLinks_r: links is null!");
			return result;
		}
		for (DirectionalLink dl : links)
			result.add(dl.link);
		return result;
	}
	
	/**
	 * Report weather this Node is a sink (has only incoming Links). 
	 * @return Boolean; true if this Node is a sink. false if this Node is not
	 * a sink
	 */
	public boolean isSink() {
		return sink;
	}

	/**
	 * Report weather this Node is a source (has only outgoing Links).
	 * @return Boolean; true if this Node is a source; false if this Node is
	 * not a source
	 */
	public boolean isSource() {
		return source;
	}

	/**
	 * Retrieve the {@link nl.tudelft.otsim.SpatialTools.Circle} that indicates the extent of this Node.
	 * @return {@link nl.tudelft.otsim.SpatialTools.Circle}; circle that indicates the extent of this Node.
	 */
	public final Circle getCircle() {
		return circle;
	}
	
	@Override
	public String toString() {
		return String.format(Main.locale, "%s ID=%d (%.2fm, %.2fm, %.2fm)", name, nodeID, getX(), getY(), getZ());
	}
	
	/**
	 * This class combines a {@link Link}, angle and direction into one object.
	 * 
	 * @author Peter Knoppers
	 */
	public class DirectionalLink implements Comparable<DirectionalLink> {
		Link link;
		double angle;
		boolean incoming;

		/**
		 * Create a DirectionalLink.
		 * @param link {@link Link} link of the new DirectionalLink
		 * @param incoming Boolean; must be true if the {@link Link} points 
		 * into the {@link Node} that will own this DirectionalLink; must be 
		 * false if the Link points away from the {@link Node} that will own
		 * this DirectionalLink 
		 */
		public DirectionalLink(Link link, boolean incoming) {
			this.link = link;
			this.incoming = incoming;
			ArrayList<Vertex> vertices = link.getVertices();
			Point2D.Double p0 = vertices.get(incoming ? vertices.size() - 1 : 0).getPoint();
			Point2D.Double p1 = vertices.get(incoming ? vertices.size() - 2 : 1).getPoint();
			angle = Planar.normalizeAngle(Math.atan2(p1.y - p0.y, p1.x - p0.x));
		}
		
		@Override
		public String toString() {
			return String.format("link %s, angle %.0f, %s", link.getName_r(), angle * 180 / Math.PI, incoming ? "incoming" : "outgoing");
		}
		
		@Override
		public int compareTo(DirectionalLink other) {
			// return +1 if this must be sorted after other; etc.
			if (angle == other.angle) {
				if (incoming == other.incoming)
					return 0;
				else if (incoming)
					return 1;
				return -1;
			}
			else if (angle > other.angle)
				return 1;
			return -1;
		}
		
	}

	final double veryClose = 0.0001;
	
	/**
	 * Compute the boundary of the area that all connections for this Node
	 * occupy. Both the circle and the convex hull of the conflictArea are
	 * computed.
	 * are taken into account; if false, all CrossSectionElements are used.
	 */
	public void determineNodeBoundary() {
		area = nodeExpander.requiredSpace();
		circle = Planar.circleCoveringPoints(area.getProjection());
	}
	
	private ArrayList<Vertex> truncateHeadAtConflictArea(ArrayList<Vertex> vertices) {
		// Generate the polygon of the conflictArea
		Point2D.Double polygon[] = area.getProjection(); 
		ArrayList<Vertex> result = new ArrayList<Vertex>();
		result.add(vertices.get(0));
		Point2D.Double prevPoint = null;
		int numberOfReplacedVertices = 0;
		for (Vertex v : vertices) {
			Point2D.Double p = v.getPoint();
			if (null != prevPoint) {
				Line2D.Double line = new Line2D.Double(prevPoint, p);
				double closest = Double.MAX_VALUE;
				Point2D.Double replacementPoint = null;
				ArrayList<Point2D.Double> intersections = Planar.lineIntersectsPolygon(line, polygon);
				// find intersections with the polygon
				for (Point2D.Double intersection : intersections) {
					// if statement added 
					if (intersection != null) {
						double distance = intersection.distance(p);
						if (distance < closest) {
							replacementPoint = intersection;
							closest = distance;
						}
					}
				}
				// find very near misses with the polygon
				for (Point2D.Double pp : polygon) {
					if (Planar.distanceLineSegmentToPoint(line, pp) < veryClose) {
						double distance = pp.distance(p);
						if (distance < closest) {
							replacementPoint = pp;
							closest = distance;
						}
							
					}
				}
				if (null != replacementPoint && numberOfReplacedVertices == 0) {
					numberOfReplacedVertices++;						
					result.clear();
					result.add(new Vertex(replacementPoint, z));
				}
				// 20140120/PK: WRONG: else	(must ALWAYS add vertex v)
				result.add(v);
			}
			prevPoint = p;
		}		
		if (result.size() < 2) {
			System.err.println("Malformed vertices");
			System.out.println("vertices are:" + Planar.verticesToString(vertices));
			System.out.println("polygon is: " + Planar.pointsToString(polygon));
			// Uncomment to try again for debugging
			//truncateHeadAtConflictArea(vertices);
		}
		return result;
	}
	
	/**
	 * Truncate a list of {@link Vertex Vertices} at the conflict area of this
	 * Node. The provided list is modified.
	 * @param vertices ArrayList&lt;{@link Vertex}&gt;; the vertices to
	 * truncate
	 * @return ArrayList&lt;{$link Vertex}&gt; the modified list of vertices
	 */
	public ArrayList<Vertex> truncateAtConflictArea(ArrayList<Vertex> vertices) {
		if (vertices.size() < 2)
			throw new Error("Malformed vertices");
		if (! hasConflictArea())
			return vertices;	// First take care of the easy cases	
		
		if (null == circle)
			throw new Error("Circle not set");
		double distanceStart = vertices.get(0).getPoint().distance(circle.center());
		double distanceEnd = vertices.get(vertices.size() - 1).getPoint().distance(circle.center());
		if ((distanceStart < circle.radius()) && (distanceEnd < circle.radius())) {
			System.err.println("Vertices begin AND end within circle of node " + name);
			return vertices;
		} 
		if (distanceStart < circle.radius()) {
			vertices = truncateHeadAtConflictArea(vertices);
			if (vertices.size() < 2)
				throw new Error("Malformed vertices");
			return vertices;
		}
		if (distanceEnd < circle.radius()) {
			// This severely clobbers the provided list of vertices
			// We'd better make sure that the caller does not mind...
			Collections.reverse(vertices);
			vertices = truncateHeadAtConflictArea(vertices);
			if (vertices.size() < 2)
				throw new Error("Malformed vertices");
			Collections.reverse(vertices);
			return vertices;
		}
		// Neither end of vertices is near the conflict area
		return vertices;
	}
	
	/**
	 * Fix the geometry of this Node. This method must be called when something 
	 * in the {@link Network} (or at least in any the {@link Link Links} 
	 * starting or ending at this Node has changed. 
	 */
	public void fixGeometry () {
		//System.out.println(String.format("Entering fixGeometry: Directional links at node %d (%s) %s", nodeID, toString(), links.toString()));
		//conflictArea = null;
		closingLines = new ArrayList<ArrayList<Vertex>>();
		if (links.size() <= 1) {
			//System.out.println("Returning from fixGeometry because links.size is " + links.size() + " which is <= 1");
			return;	// that was the really easy case
		}
		DirectionalLink prevDL = links.last();
		for (DirectionalLink dl : links) {
			boolean prevIncoming = prevDL.incoming;
			boolean thisIncoming = dl.incoming;
			CrossSection prevCS = prevDL.link.getCrossSectionAtNode(prevIncoming);
			CrossSection thisCS = dl.link.getCrossSectionAtNode(thisIncoming);
			//System.out.println(String.format("prevCS (%s) %s", prevIncoming ? "incoming" : "outgoing", prevCS.toString()));
			//System.out.println(String.format("thisCS (%s) %s", thisIncoming ? "incoming" : "outgoing", thisCS.toString()));
			// Connect the CrossSectionElements of dl to the corresponding CrossSectionElements of prevDL
			// Stop after the first boundary of a drivable element is done, or at the before-last CrossSectionElement
			// FIXME: this will fail if there is more than one drivable element.
			// But I haven't figured out how that case should be handled anyway...
			// FIXME: this also fails if (almost) parallel links meet at the node.
			int thisCSEIndex = 0;
			int thisLastCSEIndex = thisCS.getCrossSectionElementList_r().size() * 2 - 1;
			int prevCSEIndex = 0;
			int prevLastCSEIndex = prevCS.getCrossSectionElementList_r().size() * 2 - 1;
			while ((thisCSEIndex != thisLastCSEIndex) && (prevCSEIndex != prevLastCSEIndex)) {
				CrossSectionElementTypology prevTypology = prevCS.elementFromNode(prevIncoming, true, prevCSEIndex).getCrossSectionElementTypology();
				CrossSectionElementTypology thisTypology = thisCS.elementFromNode(thisIncoming, false, thisCSEIndex).getCrossSectionElementTypology();
				//System.out.println(String.format("prevCSEIndex=%d (%s), thisCSEIndex=%d (%s)", prevCSEIndex, prevTypology.getName_r(), thisCSEIndex, thisTypology.getName_r()));
				if (! prevTypology.getName_r().equals(thisTypology.getName_r())) {
					// CrossSectionElement is of a different type
					if (! (prevTypology.getDrivable() && thisTypology.getDrivable())) {
						// And not both are drive-able
						if (prevTypology.getDrivable())
							thisCSEIndex++;
						else if (thisTypology.getDrivable())
							prevCSEIndex++;
						// Both are not drive-able; skip the narrowest
						else if (prevCS.elementFromNode(prevIncoming, true, prevCSEIndex).getCrossSectionElementWidth() < thisCS.elementFromNode(thisIncoming, true, thisCSEIndex).getCrossSectionElementWidth())
							prevCSEIndex++;
						else
							thisCSEIndex++;
						continue;						
					}
					// Both are drive-able (but not of the same type)
				}
				// Connect two adjacent drive-able CrossSectionElements
				Line2D.Double prevVector = prevCS.vectorAtNode(prevIncoming, true, prevCSEIndex, true);
				if (null != prevVector) {
					//System.out.println(String.format("prevVector %s", line2D2String(prevVector)));
					Line2D.Double thisVector = thisCS.vectorAtNode(thisIncoming, false, thisCSEIndex, true);
					if (null != thisVector) {
						//System.out.println(String.format("thisVector %s", line2D2String(thisVector)));
						Point2D.Double intersection = Planar.intersection(prevVector, thisVector);
						// Remark: what happens at junctions with sharp bends (in and out)?
						if (null == intersection)
							System.out.println("Node-Geometry: intersection is null!");
						//else if (GeometryTools.distanceLineSegmentToPoint(thisVector, intersection) > tooFar) {
						//	System.out.println("intersection is too far away: " + GeometryTools.distanceLineSegmentToPoint(thisVector, intersection) + "m");
						//} 
						else {
							//System.out.println(String.format("Intersection %s", intersection.toString()));
							if ((null != circle) && (intersection.distance(circle.center()) > circle.radius()))
								System.out.println("Intersection lies outside AOI");
							//Vertex endPoint = new Vertex(intersection.x, intersection.y, 0);
							//prevCS.setEndPoint(prevIncoming, true, prevCSEIndex, endPoint);
							//thisCS.setEndPoint(thisIncoming, false, thisCSEIndex, endPoint);
						}
					}
					thisCSEIndex++;
				}
				prevCSEIndex++;
				if (prevTypology.getDrivable())
					break;
			}			
			//System.out.println("Done one link pair");
			prevDL = dl;
		}
		SortedSet<Integer> sortedPriorities = new TreeSet<Integer>();
		for (DirectionalLink dl : links) {
			CrossSection cs = dl.link.getCrossSectionAtNode(dl.incoming);
			for (CrossSectionElement cse : cs.getCrossSectionElementList_r())
				sortedPriorities.add(cse.getCrossSectionElementTypology().getCouplingPriority());
		}
		//System.out.println("Returning from fixGeometry; Done node " + name);
	}
	
	/**
	 * Add boundary lines to fence off holes between adjacent drive-able
	 * {@link CrossSectionElement CrossSectionElements} that meet at this Node.
	 */
	public void closeHoles() {
		int dlIndex = 0;
		for (DirectionalLink dl : links) {
			CrossSection cs = dl.link.getCrossSectionAtNode(dl.incoming);
			// find the right boundary of a drive-able CrossSectionElement
			for (int index = 1; index < 2 * cs.getCrossSectionElementList_r().size(); index += 2) {
				CrossSectionElement cse = cs.elementFromNode(dl.incoming, false, index);
				if (! cse.getCrossSectionElementTypology().getDrivable())
					continue;
				// Find the left boundary of the next drive-able CrossSectionElement
				DirectionalLink otherDL = dl;
				int otherDLIndex = dlIndex; 
				int otherIndex = index + 1;
				while (true) {
					CrossSection otherCS = otherDL.link.getCrossSectionAtNode(otherDL.incoming);
					boolean foundIt = false;
					for ( ; otherIndex < 2 * otherCS.getCrossSectionElementList_r().size(); otherIndex += 2) {
						CrossSectionElement otherCSE = otherCS.elementFromNode(otherDL.incoming, false, otherIndex);
						if (otherCSE.getCrossSectionElementTypology().getDrivable()) {
							foundIt = true;
							break;
						}
					}
					if (foundIt) {
						Vertex fromVertex = cs.vertexFromNode(dl.incoming, false, index, 0, true);
						Vertex toVertex = otherCS.vertexFromNode(otherDL.incoming, false, otherIndex, 0, true);
						if ((null == fromVertex) || (null == toVertex))
							System.err.println("null vertex in closeHoles");
						else if (fromVertex.getPoint().distance(toVertex.getPoint()) > veryClose) {
							System.out.println(String.format("Adding closing line from %s to %s index=%d, otherIndex=%d", fromVertex.toString(), toVertex.toString(), index, otherIndex));
							ArrayList<Vertex> closingLine = new ArrayList<Vertex>();
							closingLine.add(new Vertex(fromVertex));
							closingLine.add(new Vertex(toVertex));
							closingLines.add(closingLine);
						}
						break;
					}
					// search for a drive-able CrossSectionElement in the next link
					//System.out.println("searching the next link");
					if (++otherIndex >= 2 * otherCS.getCrossSectionElementList_r().size()/*links.size()*/) {
						otherIndex = 0;
						otherDLIndex++;
						if (otherDLIndex >= links.size())
							otherDLIndex = 0;
						otherDL = (DirectionalLink) links.toArray()[otherDLIndex];
					} else
						System.out.println("Cannot happen???");
				}					
			}
			dlIndex++;
		}
	}
	
	private int directionalCount(boolean in) {
		int result = 0;
		for (DirectionalLink dl : links)
			if (dl.incoming == in)
				result++;
		return result;
	}
	
	/**
	 * Determine the number of incoming {@link Link Links} at this Node.
	 * @return Integer; the number of incoming {@link Link Links} at this Node
	 */
	public int incomingCount() {
		return directionalCount(true);
	}
	
	/**
	 * Determine the number of departing {@link Link Links} at this Node.
	 * @return Integer; the number of departing {@link Link Links} at this Node
	 */
	public int leavingCount() {
		return directionalCount(false);
	}
	
	private ArrayList<DirectionalLink> getLinks(Boolean which) {
		ArrayList<DirectionalLink> result = new ArrayList<DirectionalLink> ();
		if (null == links) {
			System.err.println("Links is null (this is node " + toString() + ")");
			return result;
		}
		for (DirectionalLink dl : links)
			if ((null == which) || (which == dl.incoming))
				result.add(dl);
		return result;
	}
	
    private void determineSinkOrSource() {
    	sink = false;
    	source = false;
    	int inCount = incomingCount();
    	int outCount = leavingCount();
    	// Take care of the easy cases first
    	if ((outCount == 0) && (inCount == 0))
    		return;	// we don't label unconnected nodes as source or sink
    	else if ((outCount == 0) && (inCount > 0))
    		sink = true;
    	else if((outCount > 0) && (inCount == 0))
    		source = true;
    	else if ((inCount > 1) || (outCount > 1))
    		return;
    	else {
	    	// This node has at precisely one incoming and one outgoing link
    		// If both connect to the same other node, this node is BOTH a
    		// source AND a sink
    		if (getLinks(true).get(0).link.getFromNode_r().equals(getLinks(false).get(0).link.getToNode_r()))
    			sink = source = true;
    	}
    }
    
    /**
     * Return a list of {@link DirectionalLink DirectionalLinks} of this Node.
     * @param which Boolean; if true, all incoming DirectionalLinks are returned;
     * if false, all outgoing DirectionalLinks are returned; if null, all
     * DirectionalLinks are returned
     * @return ArrayList&lt;{@link DirectionalLink}&gt;; the list of DirectionalLinks of this Node 
     */
	public ArrayList<DirectionalLink> getLinksFromJunction(Boolean which) {
	    determineSinkOrSource();	// FIXME: is this call needed?
		return getLinks(which);
	}
	

    /**
     * Return a caption for the pop up menu of the {@link nl.tudelft.otsim.GUI.ObjectInspector}.
     * @return String; caption for the pop up menu of the {@link nl.tudelft.otsim.GUI.ObjectInspector}
     */
    @SuppressWarnings("static-method")
	public String itemizeTrafficLightController_caption() {
    	return "Traffic light controller";
    }
    
    /**
     * We need this one too for the caption of the pop up menu of the 
     * {@link nl.tudelft.otsim.GUI.ObjectInspector}.
     * @return String; caption for the pop up menu of the {@link nl.tudelft.otsim.GUI.ObjectInspector}
     */
    public String getTrafficLightController_r() {
     	return "Click twice to create, edit, or remove a Traffic light controller at node " + name;
    }
    
    private static final String addTrafficLightController = "Create a traffic light controller at node ";
    private static final String editTrafficLightController = "Edit the traffic light controller at node ";
    private static final String deleteTrafficLightController = "Delete the traffic light controller at node ";
    
    /**
     * Create or remove a {@link TrafficLightController} at this Node.
     * @param object Object; should be a String
     */
    public void setTrafficLightController_w (Object object) {
    	System.out.println("setTrafficLightController_w called; object is " + object.toString());
    	if (object instanceof String) {
    		String value = (String) object;
    		if (value.startsWith(addTrafficLightController))
    			this.trafficLightController = new TrafficLightController(parentNetwork, "TL_" + name);
    		else if (value.startsWith(editTrafficLightController))
    			parentNetwork.openObjectInspector(trafficLightController);
    		else if (value.startsWith(deleteTrafficLightController)) {
    			trafficLightController.deleteAllLightsAndDetectors();
    			trafficLightController = null;
    		} else
    			throw new Error("setTrafficLightController does not know what \"" + value + "\" means");
    	} else
    		throw new Error("setTrafficLightController should be called with a String object");
    }
    
    /**
     * Return a list of items for the pop up menu of the
     * {@link nl.tudelft.otsim.GUI.ObjectInspector}.
     * @return ArrayList&lt;String&gt;; the list of one item for the pop up
     * menu of the {@link nl.tudelft.otsim.GUI.ObjectInspector}
     */
    public ArrayList<String> itemizeTrafficLightController_i() {
    	ArrayList<String> result = new ArrayList<String>();
    	if (null == trafficLightController)
    		result.add(addTrafficLightController + name);
    	else {
    		result.add(editTrafficLightController + name);
    		result.add(deleteTrafficLightController + name);
    	}
    	return result;
    }
    
    /**
     * Create a GeneralPath that describes the conflictArea of this Node.
     * @return GeneralPath; the contour of the conflictArea of this Node
     */
	public GeneralPath createJunctionPolygon()   {
		return area.getGeneralPath(); 
	}
    
    /**
     * Draw this Node on a GraphicsPanel.
     * @param graphicsPanel GraphicsPanel; graphicsPanel to draw onto
     */
	@Override
	public void paint(GraphicsPanel graphicsPanel) {
        final int nonSelectedNodeDiameter = 6;
        final int selectedNodeDiameter = 20;

    	Point2D.Double point = getPoint();            
    	graphicsPanel.setStroke(1F);
        final Color color = null != nodeExpander ? Color.blue : Color.RED;
        graphicsPanel.setColor(color);
        if ((null == nodeExpander) || (Main.mainFrame.showLabelsOnAutogeneratedNodes.isSelected()))
        	graphicsPanel.drawString(getName_r(), point);
        graphicsPanel.setStroke(0f);
        graphicsPanel.drawCircle(point, color, nonSelectedNodeDiameter);
        graphicsPanel.setStroke(6f);
    	if (parentNetwork.selectedMicroZone != null)  {
        	for (Integer nodeNumber : parentNetwork.selectedMicroZone.getNodeList()) {
				Node node = parentNetwork.lookupNode(nodeNumber, true);
				if (this == node)
					graphicsPanel.drawCircle(point, Color.BLUE, selectedNodeDiameter);
        	}
    	}
        if ((null != parentNetwork.startNode) && (getNodeID() == parentNetwork.startNode.getNodeID()))
        	graphicsPanel.drawCircle(point, Color.RED, selectedNodeDiameter);
        if ((null != parentNetwork.endNode) && (getNodeID() == parentNetwork.endNode.getNodeID()))
        	graphicsPanel.drawCircle(point, Color.PINK, selectedNodeDiameter);
        if (null != area) {
        	graphicsPanel.setStroke(0F);
        	graphicsPanel.setColor(Color.CYAN);
        	graphicsPanel.drawGeneralPath(area.getGeneralPath(), Color.CYAN, null);
        }
        if (null != circle) {
        	graphicsPanel.setStroke(0F);
        	double r = graphicsPanel.translate(new Point2D.Double(circle.radius(), 0)).distance(graphicsPanel.translate(new Point2D.Double(0, 0)));
        	graphicsPanel.drawCircle(circle.center(), Color.CYAN, (int) (2 * r));
        }
        if (null != subNetwork)
        	subNetwork.repaintGraph(graphicsPanel);
	}

	/**
	 * Retrieve the {@link TrafficLightController} of this Node
	 * @return {@link TrafficLightController}; the TrafficLightController of
	 * this Node, or null if this Node does not have a TrafficLightController
	 */
	public TrafficLightController getTrafficLightController() {
		return trafficLightController;
	}

	/**
	 * Retrieve the lines needed to make the drive-able boundaries of this node <i>water tight</i>.
	 * @return String; the textual description of the closing lines at this Node
	 */
	public String getClosingLines() {
		String result = "";
		for (ArrayList<Vertex> alv : closingLines) {
			result += "Border\t";
			for (Vertex v : alv)
				result += String.format(Locale.US, "%.2f\t%.2f\t", v.getX(), v.getY());
        	result += "\n";
		}
		return result;
	}

	/**
	 * Determine if this Node has a conflict area.
	 * @return Boolean; true if this Node has a real conflict area; false if 
	 * this Node does not have a real conflict area
	 */
	public boolean hasConflictArea() {
		return (null != area) && (area.surfaceArea() != 0);
	}

	/**
	 * Expand this Node
	 */
	public void expand() {
		if (null != nodeExpander)
			subNetwork = nodeExpander.expandNode();
	}
	
	/**
	 * Retrieve the parent {@link Network} of this Node.
	 * @return {@link Network}; the parent Network of this Node
	 */
	public Network getParentNetwork_r() {
		return parentNetwork;
	}

	/**
	 * Report if this Node has an auto-generated sub-{@link Network}.
	 * @return Boolean; true if this Node has an auto-generated sub-{@link Network};
	 * false if this Node does not have an auto-generated sub-Network
	 */
	public boolean isAutoGenerated() {
		return (null != nodeExpander) && (null != subNetwork);
	}
	
	/**
	 * @return {@link Network}; the sub-Network of this Node (which may be null).
	 */
	public Network getSubNetwork () {
		return subNetwork;
	}

}
