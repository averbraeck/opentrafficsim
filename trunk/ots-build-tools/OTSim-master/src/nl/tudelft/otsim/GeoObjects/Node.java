package nl.tudelft.otsim.GeoObjects;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Collections;
import java.util.SortedSet;
import java.util.TreeSet;

import nl.tudelft.otsim.FileIO.ParsedNode;
import nl.tudelft.otsim.FileIO.StaXWriter;
import nl.tudelft.otsim.FileIO.XML_IO;
import nl.tudelft.otsim.GUI.GraphicsPanel;
import nl.tudelft.otsim.GUI.InputValidator;
import nl.tudelft.otsim.GUI.Main;
import nl.tudelft.otsim.GeoObjects.PriorityConflict.conflictType;
import nl.tudelft.otsim.SpatialTools.Circle;
import nl.tudelft.otsim.SpatialTools.Curves;
import nl.tudelft.otsim.SpatialTools.Planar;

/**
 *
 * @author gtamminga
 */
public class Node extends Vertex implements XML_IO {
	private static final long serialVersionUID = 1L;
	
	/** Name for a Node element when stored in XML format */
	public static final String XMLTAG = "node";
	
	/** Name for a Node name element when stored in XML format */
	private static final String XML_NAME = "name";
	/** Name for a Node ID element when stored in XML format */
	private static final String XML_ID = "ID";
	
	private int nodeCount = 0;
	private String name;
    private int nodeID;
    private boolean sink = false;
    private boolean source = false;
	private TreeSet<DirectionalLink> links = null;
	ArrayList<Vertex> conflictArea;
	ArrayList<ArrayList<Vertex>> closingLines;
	private Network network;
	Circle circle;

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
        this.network = network;
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
        super(X, Y, Z);
        this.name = name;
        this.nodeID = nodeID;
        this.network = network;
    }
    
	/**
	 * Create a Node from a parsed XML file.
	 * @param network {@link Network}; the Network that will own the new Node
	 * @param pn {@link ParsedNode}; root of the Node field in the parsed XML file
	 * @throws Exception
	 */
    public Node(Network network, ParsedNode pn) throws Exception {
    	// Initialize everything to some invalid value
    	name = null;
    	nodeID = -1;
    	this.network = network;
    	x = y = z = Double.NaN;
		for (String fieldName : pn.getKeys()) {
			if (pn.size(fieldName) != 1)
				throw new Exception("Field " + fieldName + " has " + pn.size(fieldName) + "elements (should be 1)");
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
				&& staXWriter.writeNodeEnd(XMLTAG);
    }
    
    /*
    // Duplicate a node
    public Node (Network network, Node aNode) {
        this(network, aNode.getName_r(), aNode.getNodeID(), aNode.getX(), aNode.getY(), aNode.getZ());
    }
    */
    
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
				return network.lookupNode(proposedValue, false) == null;
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
	 * Retrieve the X-coordinate of this Node.
	 * @return Double; X-coordinate of this Node
	 */
	public double X_r() {
		return getX();
	}
	
	/**
	 * Retrieve the Y-coordinate of this Node.
	 * @return Double; Y-coordinate of this Node
	 */
	public double Y_r() {
		return getY();
	}
	
	/**
	 * Retrieve the Z-coordinate of this Node.
	 * @return Double; Z-coordinate of this Node
	 */
	public double Z_r() {
		return getZ();
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
		return String.format(Main.locale, "%s (%.2fm, %.2fm, %.2fm)", name, getX(), getY(), getZ());
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
			angle = Math.atan2(p1.y - p0.y, p1.x - p0.x);
			if (angle < 0)
				angle += 2 * Math.PI;
			if (angle >= 2 * Math.PI)
				angle -= 2 * Math.PI;
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
	 * @param drivable Boolean; if true only drive-able CrossSectionElements
	 * are taken into account; if false, all CrossSectionElements are used.
	 */
	public void determineNodeBoundary(boolean drivable) {
		//System.out.println("Determine node boundary of " + name);
		ArrayList<Point2D.Double> pointCloud = new ArrayList<Point2D.Double>(); 
		// Add all end points of (drive-able) crossSectionElements to the pointCloud
		for (DirectionalLink dl : links) {
			CrossSection cs = dl.link.getCrossSectionAtNode(dl.incoming);
			//if (name.equals("node_een"))
			//	Log.logToFile("d:/hull.txt", false, "selecting crosssection %s %s %s", cs.toString(), dl.incoming ? "from" : "to", dl.incoming ? dl.link.getFromNode_r().getName_r() : dl.link.getToNode_r().getName_r());
			for (int index = 2 * cs.getCrossSectionElementList_r().size(); --index >= 0; )
				if ((! drivable) || cs.elementFromNode(dl.incoming, true, index).getCrossSectionElementTypology().getDrivable()) {
					Line2D.Double dlLine = cs.vectorAtNode(dl.incoming, true, index, false);
					if (null == dlLine)
						continue;
					pointCloud.add((Point2D.Double) dlLine.getP1());
					//if (name.equals("node_een"))
					//	Log.logToFile("d:/hull.txt", false, "added endpoint %d %.3f,%.3f", index, dlLine.x1, dlLine.y1);
					// Add the intersections of the boundaries of different (drive-able) crossSectionElements
					for (DirectionalLink otherDL : links) {
						if (otherDL.angle >= dl.angle) // only search up to dl (and NEVER include dl itself)
							break;	// this way we'll find each intersection only ONCE
						CrossSection otherCS = otherDL.link.getCrossSectionAtNode(otherDL.incoming);
						for (int otherIndex = 2 * otherCS.getCrossSectionElementList_r().size(); --otherIndex >= 0; )
							if ((! drivable) || (otherCS.elementFromNode(otherDL.incoming, true, otherIndex).getCrossSectionElementTypology().getDrivable())) {
								Line2D.Double otherDLLine = otherCS.vectorAtNode(otherDL.incoming, true, otherIndex, false);
								if (null == otherDLLine)
									continue;
								//if (name.equals("node_een"))
								//	Log.logToFile("d:/hull.txt", false, "checking intersection between %s and %s", GeometryTools.Line2DToString(dlLine), GeometryTools.Line2DToString(otherDLLine));
								if (Planar.lineIntersectsLine(dlLine, otherDLLine)) {
									Point2D.Double intersection = Planar.intersection(dlLine, otherDLLine);
									//System.out.println("Adding boundary intersection " + intersection);
									pointCloud.add(intersection);
									//if (name.equals("node_een"))
									//	Log.logToFile("d:/hull.txt", false, "added boundary intersection %.3f,%.3f", intersection.x, intersection.y);
								}
							}
					}
				}
		}

		//System.out.println("pointCloud of " + name + " contains these points: " + pointCloud.toString());
		if (0 == pointCloud.size())
			pointCloud.add(getPoint());	// add design point of this node
		circle = Planar.circleCoveringPoints(pointCloud);
		//ArrayList<Point2D.Double> ppp = new ArrayList<Point2D.Double>();
		//ppp.add(new Point2D.Double(this.x, this.y));
		//circle = GeometryTools.circleCoveringPoints(ppp);
		//System.out.format(Main.Locale, "covering circle is %s\r\n", circle.toString());
		// Create the convex hull consisting of the points where the drive-able parts of the links enter the covering circle
		ArrayList<Point2D.Double> points = new ArrayList<Point2D.Double>();
		for (DirectionalLink dl : links) {
			CrossSection cs = dl.link.getCrossSectionAtNode(dl.incoming);
			for (int index = 2 * cs.getCrossSectionElementList_r().size(); --index >= 0; )
				if (cs.elementFromNode(dl.incoming, true, index).getCrossSectionElementTypology().getDrivable()) {
					Line2D.Double line = cs.vectorAtNode(dl.incoming, true, index, false);
					if (null == line)
						continue;
					Point2D.Double intersections[] = Planar.intersectRayAndCircle(line, circle);
					//System.out.format(Main.Locale, "line %s intersects circle %s at %d point(s)\r\n", GeometryTools.Line2DToString(line), circle.toString(), intersections.length);
					if (intersections.length > 1) {
						System.err.println("Peter thinks this never happens...");
						// use the one that is closest to the far end of the line
						if (intersections[0].distance(line.getP2()) < intersections[1].distance(line.getP2()))
							points.add(intersections[0]);
						else
							points.add(intersections[1]);
					}
					else if (intersections.length > 0)
						points.add(intersections[0]);
					else {
						System.out.println("line " + Planar.Line2DToString(line) + " does not intersect the circle " + circle.toString());
						// probably a very near miss
						double ratio = circle.radius() / line.getP1().distance(circle.center());
						if ((ratio > 0.99) && (ratio < 1.01))
							points.add(Planar.logPoint("adding almost intersection", (Point2D.Double)(line.getP1())));
						else
							System.err.println("Total miss: ratio is " + ratio + " (" + circle.radius() / line.getP2().distance(circle.center()) + ")");
					}
				}
		}
		if (points.size() == 0)
			return;
		// Compute the convex hull (in 2D) and convert that into an ArrayList<Vertex>
		//System.out.println("Computing convex hull of " + points.toString());
		//if (name.equals("node_een"))
		//	Log.logToFile("d:/hull.txt", false, "points is %s", points.toString());
		conflictArea = new ArrayList<Vertex>();
		for (Point2D.Double p : Planar.convexHull(points))
			conflictArea.add(new Vertex(p, z));	// use Z-component of this node
		conflictArea.add(conflictArea.get(0));	// close the polygon
		//System.out.println("convex hull is " + conflictArea.toString());
		//if (name.equals("node_een"))
		//	Log.logToFile("d:/hull.txt", false, "hull is %s", conflictArea.toString());
	}
	
	private ArrayList<Vertex> truncateHeadAtConflictArea(ArrayList<Vertex> vertices) {
		// Generate the polygon of the conflictArea
		Point2D.Double polygon[] = new Point2D.Double[conflictArea.size()];
		for (int i = conflictArea.size(); --i >= 0; )
			polygon[i] = conflictArea.get(i).getPoint();
		ArrayList<Vertex> result = new ArrayList<Vertex>();
		result.add(vertices.get(0));
		Point2D.Double prevPoint = null;
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
				if (null != replacementPoint) {
					result.clear();
					result.add(new Vertex(replacementPoint, z));
				}
				result.add(v);
			}
			prevPoint = p;
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
		if (null == conflictArea)
			return vertices;	// First take care of the easy cases		
		double distanceStart = vertices.get(0).getPoint().distance(circle.center());
		double distanceEnd = vertices.get(vertices.size() - 1).getPoint().distance(circle.center());
		if ((distanceStart < circle.radius()) && (distanceEnd < circle.radius())) {
			System.err.println("Vertices begins AND ends within circle of node " + name);
			return vertices;
		} 
		if (distanceStart < circle.radius())
			return truncateHeadAtConflictArea(vertices);
		if (distanceEnd < circle.radius()) {
			// This severely clobbers the provided list of vertices
			// We'd better make sure that the caller does not mind...
			Collections.reverse(vertices);
			vertices = truncateHeadAtConflictArea(vertices);
			Collections.reverse(vertices);
			return vertices;
		}
		// Neither end of vertices is near the conflict area
		return vertices;
	}
	
	public void fixGeometry () {
		System.out.println(String.format("Entering fixGeometry: Directional links at node %d (%s) %s", nodeID, toString(), links.toString()));
		//conflictArea = null;
		closingLines = new ArrayList<ArrayList<Vertex>>();
		if (links.size() <= 1) {
			System.out.println("Returning from fixGeometry because links.size is " + links.size() + " which is <= 1");
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
							if (intersection.distance(circle.center()) > circle.radius())
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
		System.out.println("Returning from fixGeometry; Done node " + name);
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
							System.out.println(String.format("Missing link from %s to %s index=%d, otherIndex=%d", fromVertex.toString(), toVertex.toString(), index, otherIndex));
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
	
	private ArrayList<DirectionalLink> getLinks() {
		ArrayList<DirectionalLink> result = new ArrayList<DirectionalLink> ();
		for (DirectionalLink dl : links)
			result.add(dl);
		return result;
	}
	
    public void determineSinkOrSource() {
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
	    	ArrayList<DirectionalLink> dlList = getLinks();
			int incomingIndex = dlList.get(0).incoming ? 0 : 1;  
			int leavingIndex = 1 - incomingIndex;
			if (dlList.get(incomingIndex).link.getFromNode_r().equals(dlList.get(leavingIndex).link.getToNode_r()))
				sink = source = true;  
    	}
    }
    
    private class OutLaneInfo {
    	private Lane outLane;
    	private int linkRank;
    	private double angleDifference;
    	private Lane inLane;
    	
    	private OutLaneInfo(Lane outLane, Double angleDifference, int linkRank)  {
    		this.outLane = outLane;
    		this.linkRank = linkRank;
    		this.angleDifference = angleDifference;
    	}
    	
		public Lane getLane() {
			return outLane;
		}
		
		public int getLinkRank() {
			return linkRank;
		}
		
		public double getAngleDifference() {
			return angleDifference;
		}

		public Lane getInLane() {
			return inLane;
		}
		public void setInLane(Lane inLane) {
			this.inLane = inLane;
		}	

    }
    
    
	public ArrayList<DirectionalLink> getLinksFromJunction()  {
	    determineSinkOrSource();
	    ArrayList<DirectionalLink> dlList = null;
		if (0 == links.size())
			return dlList;
		// the number of incoming and leaving links from this node

		// retrieve all directional links that are connected to this node
		return dlList = getLinks();
	}
	
	/**
	 * Try to link {@link CrossSectionElement CrossSectionElements} of the
	 * {@link Link Links} that meet at this Node. 
	 */
    /**
     * Connect the {@link Link Links} at this Node.
     */
    public void fixLinkConnections() {
    	
    	ArrayList<DirectionalLink> dlList = getLinksFromJunction();
		int inCount = incomingCount();
		int outCount = leavingCount();

		// Create junction expansion
		// At junctions every entering link and every exiting link receives a new node
		// This new node replaces the original one, and gets the coordinates of the driving lane
		// At the junctions, links are created by generating links between the new node 
		// These links receive  a new crossSection, and one crossSection element: the road with road markings and
		// finally driving lanes
		// FIXME: assumes that all turns are permitted (should be dependent on road markings, signs, etc.
		// TODO Later on: these turning lanes will only be generated if turning arrows show if a turn exists

		ArrayList<Link> newLinks = new ArrayList<Link>();
		// the simple case: two links 

		// a type of junction 
		if ((inCount >= 1) && (outCount >= 1) && (inCount + outCount > 2)) {
			// Expand nodes and create junction connecting links/lanes (connectors)
			int incomingArm = 0; // index of the incoming arm: start at zero
			for (DirectionalLink incoming : dlList)   {
				// for the incoming lanes: detect the turning movements and create turning lanes
				// if turning arrows are provided: connect by that information
				// else an automated search
				if (incoming.incoming) {
					// automated search: uses some logic to define turning movements from one lane to another 
					// select the lanes from the incoming link into the junction
		    		int size = incoming.link.getCrossSections_r().size();
		    		// collect all entering lanes from one link
		    		ArrayList<Lane> inLanesAll = incoming.link.getCrossSections_r().get(size - 1).collectLanesReversed();
		    		
		            //step through the leaving links and collect information for all leaving lanes
		    		// the OutLaneInfo object is created to collect that information and use it later on
		    		ArrayList<OutLaneInfo> outLanesAndLinkList = new ArrayList<OutLaneInfo>() ;
		    		// determine the angle between the entering and leaving link
		    		// the angle is represented in radians. Starting at west (0) and going anti-clockwise.
		    		// So north is around 1.5, east = pi, south = 4.5
		    		// GT should be: So north is around 4.5, east = pi, south = 1.5
		    		int linkRank = 1;  // rank of the leaving links from a certain incoming arm in anti-clockwise order
    				for (int arm = 1; arm < dlList.size(); arm++) {
		    			int itel = arm + incomingArm;
		    			if (arm + incomingArm > dlList.size() - 1)
		    				itel  = arm + incomingArm - dlList.size();
		    			DirectionalLink leaving = dlList.get(itel);
		    			// select all leaving lanes from this junction
		    			if (! leaving.incoming && ! incoming.link.getFromNode_r().equals(leaving.link.getToNode_r())) {
		    				double angleDif;
	            			if (incoming.angle > leaving.angle)
	            				angleDif = 2 * Math.PI - incoming.angle + leaving.angle ;
	            			else
	            				angleDif = leaving.angle - incoming.angle;
		    	    		CrossSection outCs = leaving.link.getCrossSections_r().get(0);
	    	    			// collect information on the index of the leaving link, its lanes and 
	    	    			// the angle with the entering lane
		    	    		for (Lane lane : outCs.collectLanesReversed())
		    	            	outLanesAndLinkList.add(new OutLaneInfo(lane, angleDif, linkRank));
		    	    		// increase the leaving link index
		    	    		linkRank++;
		    			}
		    		}
    				
    				//There are situations with two incoming links and one outgoing link (U-turn to incoming)
    				//In this case no junction is constructed
    				if (outLanesAndLinkList.size() > 0)   {
	            		int outLaneIndex = 0;
	            		double shareOfOutLanes = 0;
	            		double shareOfInLanes = 0;
	            		//shareOfOutLanes =  shareOfOutLanes + outLanesRoad.get(indexOutlane) / outLanesAll.size();
	            		ArrayList<RoadMarkerAlong> rmaList = new ArrayList<RoadMarkerAlong>();
			    		int turnCount = 0;
			    		// loop through the lanes of the entering link
			    		// sometimes a lane needs to be connected more than once (more turning movements per lane)
			    		// in that case the index is reset minus 1
	            		for (int indexInlane = 0; indexInlane < inLanesAll.size(); indexInlane++)	{
				    		Lane currentInLane = inLanesAll.get(indexInlane);
				    		Lane currentOutLane = null;
							// if a lane has got no stopLine (no priority defined) an "empty"  stopLine is added
				    		if (currentInLane.getStopLine() == null)  {
				    			StopLine stopLine = new StopLine(currentInLane.getCse(), StopLine.NOSTOPLINE,-4.0, 2.0, 0.0); 
				    			currentInLane.setStopLine(stopLine);
				    		}
	            			// if turning movements are defined, the new turning lanes are constructed accordingly
	            			if (currentInLane.getTurnArrow().getOutLinkNumbers() != null)  {
	            				// retrieve the index of the outgoing links (one by one)
	            				int outLinkRank = inLanesAll.get(indexInlane).getTurnArrow().getOutLinkNumbers()[turnCount];
	            				System.out.print("test  " + outLinkRank );
	            				// we look for the connecting outLane by looping through all outlanes at this node
	            				OutLaneInfo currentOutlaneInfo = outLanesAndLinkList.get(outLaneIndex);
	            				// connect to the lane of a leaving arm
	            				// find the index of the outgoing link first
								while ( outLinkRank != currentOutlaneInfo.getLinkRank() ) {
									if (outLaneIndex >= 0)   {
										if (outLinkRank < currentOutlaneInfo.getLinkRank())
											outLaneIndex--;
										else
											outLaneIndex++;
									}
									// retrieve the index of the current outlane
									if (outLaneIndex >= outLanesAndLinkList.size())
										break;
									currentOutlaneInfo = outLanesAndLinkList.get(outLaneIndex);
								}
								// for an inlane, all outlanes of the corresponding outLink are checked:
								while (outLinkRank == currentOutlaneInfo.getLinkRank() && outLaneIndex < outLanesAndLinkList.size()) {
									currentOutlaneInfo = outLanesAndLinkList.get(outLaneIndex);
									int inlaneNext = 1;
									boolean connectOnlyOneLane = false;
									while (indexInlane + inlaneNext < inLanesAll.size())  {										
										if (inLanesAll.get(indexInlane + inlaneNext).getTurnArrow().getOutLinkNumbers()[0] == outLinkRank)  {
											connectOnlyOneLane = true;
											break;
										}
										inlaneNext++;
									}

									if (currentOutlaneInfo.inLane == null && outLinkRank == currentOutlaneInfo.getLinkRank())  {
										currentOutlaneInfo.setInLane(currentInLane);
										if (connectOnlyOneLane) {	
											break;
										}
									}
									outLaneIndex++;
								}
								// connect the inlane to the outlane
								// later on we will loop through the outLanesAndLinkList to generate new junction connectors 
								// but first continue to inspect the other in lanes and their turns!
	            				// if more than one turning movement from an incoming lane?
	            				// keep this lane at the next loop (until all turns are dealt with)
	            				turnCount++; 
	            				if (turnCount < inLanesAll.get(indexInlane).getTurnArrow().getOutLinkNumbers().length)  {
	            					indexInlane--;
	            					outLaneIndex = 0;
	            				}
	            				else  {
	            					// go to the next inLane and reset turncount to zero
	            					turnCount = 0;
	            				}
								
	            			}
	            			
				    		// else if no turning movements are defined, construct the movements with some simple rules
	            			else if (currentInLane.getTurnArrow().getOutLinkNumbers() == null) {
	
	            				OutLaneInfo currentOutlaneInfo = outLanesAndLinkList.get(outLaneIndex);  
	            				currentOutLane = currentOutlaneInfo.getLane();
		            			// compute share of outlanes per leaving CrossSectionElement
			            		// only if no turn movement definition is available
		            			if (indexInlane == 0)
		            				// share of the number of outlanes of the first leaving link as part of all outgoing lanes
		            				shareOfOutLanes = shareOfOutLanes + (double) currentOutLane.getCse().getCrossSectionObjects(Lane.class).size() / outLanesAndLinkList.size();
		            			else if (indexInlane > 0) {
		            				// only if shifting to a new leaving link: the current lane is part of the next arm, 
		            				// than the number of outlanes from that link is added to the shareOfOutLanes
		            				if (outLaneIndex>0)
		            					if (! currentOutLane.getCse().getCrossSection().getLink().equals(outLanesAndLinkList.
			            					get(outLaneIndex - 1).getLane().getCse().getCrossSection().getLink()) )	
		            						shareOfOutLanes = shareOfOutLanes + (double) currentOutLane.getCse().getCrossSectionObjects(Lane.class).size() / outLanesAndLinkList.size();		            			
			            		}
		            			// the share of this and previous inLanes to the total number of inLanes 
		            			shareOfInLanes = (double) (indexInlane + 1) / inLanesAll.size();
		            			
		            			// only in case there are more outgoing lanes than incoming lanes it becomes a puzzle....
								if (inLanesAll.size() <= outLanesAndLinkList.size()) {
									boolean gotoNextOutlink = false; // go to the next Lane??
									
									// yes, when .. 
									if (shareOfInLanes >= shareOfOutLanes && currentOutlaneInfo.getAngleDifference() < 0.75 * Math.PI) {
										gotoNextOutlink = true;
									}
									else if (shareOfInLanes > shareOfOutLanes  && currentOutlaneInfo.getAngleDifference() <= 1.25 * Math.PI)  {
										gotoNextOutlink = true;		
									}
									if (gotoNextOutlink && shareOfOutLanes < 1) {
										indexInlane--;  // in this case more outgoing lanes are being connected to an incoming lane
									}
									if (gotoNextOutlink) {
										OutLaneInfo exploreOutlaneInfo = outLanesAndLinkList.get(outLaneIndex); 
										int outLinkRank = exploreOutlaneInfo.getLinkRank();
										while ( outLinkRank == exploreOutlaneInfo.getLinkRank()  ) {
											//&& !(currentOutlaneInfo.getInLane() == null)
											outLaneIndex++;
											if (outLaneIndex >= outLanesAndLinkList.size()) {
												System.err.format("indexOutLane (%d) is not in range of outLanesAndLinkList (0..%d)\r\n", outLaneIndex, outLanesAndLinkList.size() - 1);
												break;
											}
											exploreOutlaneInfo = outLanesAndLinkList.get(outLaneIndex);
										}
										outLaneIndex--;
									}
									outLaneIndex++;
									
								}
								 // strange junction
								if (inLanesAll.size() > outLanesAndLinkList.size())  {
									if (((double) (indexInlane + 1) / inLanesAll.size() > shareOfOutLanes) && (shareOfOutLanes < 1)) 
										outLaneIndex++;  // in case there are more outlanes connected to one incoming lane
								}
	            				currentOutlaneInfo.setInLane(currentInLane);
	            				
		            		}	
							// finished after all outlanes have passed!
							if (outLaneIndex > outLanesAndLinkList.size()-1) 
								break;
	            		}
	            	
            		
	            		outLaneIndex = 0;
						ArrayList<Lane> newLanes = new ArrayList<Lane>();
						Lane newLane = null;
						// when all incoming lanes are "inspected", the link and lane connectors are created
						// for every connection between an incoming and leaving arm, one link is created
	    				for (OutLaneInfo outLaneInfo:  outLanesAndLinkList) {
		    				Lane currentOutLane = outLaneInfo.getLane();
		    				Lane currentInLane = outLaneInfo.getInLane();
		    				boolean sameOutLink = false;
		    				if (!(currentInLane == null) && !(currentOutLane == null) )  {
		    				//create a new lane that connect this incoming lane and this leaving lane		
		    						    				
			    				boolean addLink = false;
			    				// add a new link if...	
			    				if (outLaneIndex > 0)
			        				if (outLaneInfo.getLinkRank() > outLanesAndLinkList.get(outLaneIndex-1).getLinkRank()) {
			    						addLink = true;
			    						sameOutLink = false;
			        				}
			        				else
			        					sameOutLink = true;
			    				
			    				if (addLink) {
		            				Link newLink = createJunctionLink(incoming, newLanes, rmaList);
		            				newLinks.add(newLink);
				            		rmaList = new ArrayList<RoadMarkerAlong>();
									newLanes = new ArrayList<Lane>();
									addLink = false;
									newLanes.clear();
		            			}
			    				boolean createCurve = false;
			    				Lane oldLane = null;
			    				if (newLanes.isEmpty())
			    					createCurve = true;
			    				if (newLane != null)  {
			    					oldLane = new Lane(newLane);
			    				}
								newLane = newLaneConnection(currentInLane, currentOutLane, createCurve, oldLane, sameOutLink);				    					
			    				newLanes.add(0, newLane);
		    				}
		    				if (outLaneIndex == outLanesAndLinkList.size() - 1)  {
	            				Link newLink = createJunctionLink(incoming, newLanes, rmaList);
	            				newLinks.add(newLink);
		    				}
		    				outLaneIndex++;	
	
	    				}
    				}
        		}
				// index of the current directional link (increase for the next loop)
				incomingArm++;
        	}	
			
			// revisit the new Links to investigate conflicting lanes (merge, split, cross)
			for (Link link : newLinks) {
				for (Link compareToLink : newLinks) {
					Point2D.Double p1 = link.getFromNode_r().getPoint();
					Point2D.Double p2 = link.getToNode_r().getPoint();
					Point2D.Double p3 = compareToLink.getFromNode_r().getPoint();
					Point2D.Double p4 = compareToLink.getToNode_r().getPoint();
					PriorityConflict priorityConflict = null;
					conflictType cType;
					//if ( !p1.equals(p3) && !p2.equals(p4))  {
							// merging links
							if (link.getToNode_r().getPoint().equals(compareToLink.getToNode_r().getPoint()))   {
								cType =  conflictType.MERGE;
								//System.err.println("fixLinkConnections: skipping null lane");
							}
							// splitting links
							else if (link.getFromNode_r().getPoint().equals(compareToLink.getFromNode_r().getPoint()))  {
								cType =  conflictType.SPLIT;
							}
							// conflicting links
							else  {
								cType =  conflictType.CROSSING;
							}
							for (CrossSectionObject lane1 : link.getCrossSections_r().get(0).getCrossSectionElementList_r().get(0).getCrossSectionObjects(Lane.class))   {
								for (CrossSectionObject lane2 : compareToLink.getCrossSections_r().get(0).getCrossSectionElementList_r().get(0).getCrossSectionObjects(Lane.class))   {									
									if (null == lane1)
										System.err.println("fixLinkConnections: skipping null lane");
									else  {
										Lane laneA = (Lane) lane1;
										Lane laneB = (Lane) lane2;
										// we only visit pairs of lanes ones 
										// select only crossing conflicts
										if (laneA.getID() > laneB.getID() & cType != conflictType.SPLIT)  {
											// identify yield and priority lane
											Lane pLane = new Lane();
											Lane yLane = new Lane();										

											Lane upA = laneA.getUp().get(0);
											Lane upB = laneB.getUp().get(0);
											Lane downA = laneA.getDown().get(0);
											Lane downB = laneB.getDown().get(0);
											Link linkUpA = upA.getCse().getCrossSection().getLink();
											Link linkUpB = upB.getCse().getCrossSection().getLink();
											Link linkDownA = downA.getCse().getCrossSection().getLink();
											Link linkDownB = downB.getCse().getCrossSection().getLink();
											boolean yieldA = true;
											boolean yieldB = true;
											double angleIncomingA = Double.NaN;
											double angleIncomingB = Double.NaN;
											double angleLeavingA = Double.NaN;
											double angleLeavingB = Double.NaN;
											double turnAngleA = Double.NaN;
											double turnAngleB = Double.NaN;
											
											if ((upA.getStopLine() != null)) {
												if (upA.getStopLine().getType() == StopLine.PRIORITYSTOPLINE)
													yieldA = false;
											}
											
											if ((upB.getStopLine() != null)) {
												if (upB.getStopLine().getType() == StopLine.PRIORITYSTOPLINE)
													yieldB = false;
											}
	
											if (yieldA == true && yieldB == false)   {
												yLane = laneA;
												pLane = laneB;
											}
											else if (yieldA == false && yieldB == true)   {
												yLane = laneB;
												pLane = laneA;
											}
											// conflict is based on general priority rules
											// could be on a junction with no rules or two opposing roads (both priority or yield)
											else if ( (yieldA == false && yieldB == false) || (yieldA == true && yieldB == true) )   {
												for (DirectionalLink incoming : dlList)  {
													if (incoming.incoming) {
														if (incoming.link.equals(linkUpA)) {
															angleIncomingA = incoming.angle;															
														}
														else if (incoming.link.equals(linkUpB)) {
															angleIncomingB = incoming.angle;															
														}	
													}
														else if (! incoming.incoming) {
															if (incoming.link.equals(linkDownA)) {
																angleLeavingA = incoming.angle;															
															}
															else if (incoming.link.equals(linkDownB)) {
																angleLeavingB = incoming.angle;															
															}
														}
												}
												double angleDif;
						            			if (angleIncomingA > angleIncomingB)
						            				angleDif = 2 * Math.PI - angleIncomingA + angleIncomingB;
						            			else
						            				angleDif = angleIncomingA - angleIncomingB;
						            			
						            			//	lane B comes from right
												if ( angleDif < 0.75 * Math.PI)  {
													yLane = laneA;
													pLane = laneB;
												}
						            			//	lane B comes from left
												else if ( angleDif > 1.25 * Math.PI)  {
													pLane = laneA;
													yLane = laneB;
												}
												// opposing flows: turning movement determines priority rules
												else {
							            			if (angleIncomingA > angleLeavingA)
							            				turnAngleA = 2 * Math.PI - angleIncomingA + angleLeavingA;
							            			else
							            				turnAngleA = angleIncomingA - angleLeavingA;
							            			if (angleIncomingB > angleLeavingB)
							            				turnAngleB = 2 * Math.PI - angleIncomingB + angleLeavingB;
							            			else
							            				turnAngleB = angleIncomingB - angleLeavingB;
							            			// turn with smallest angle has priority
							            			if (turnAngleA < turnAngleB)  {
														pLane = laneA;
														yLane = laneB;
							            			}
							            			else {
														yLane = laneA;
														pLane = laneB;
							            			}
							            				
												}
											}
											
											// Determine location at the lanes at the start of the conflict Area:
											Point2D.Double pInIn = new Point2D.Double(); 
											Point2D.Double pInOut = new Point2D.Double(); 
											Point2D.Double pOutIn = new Point2D.Double(); 
											Point2D.Double pOutOut = new Point2D.Double(); 
											
											// A: yields
											// B: has priority
											double longitudinalInInYield = 0;
											double longitudinalInInPriority = 0;
											ArrayList<Vertex> verticesYield = yLane.getLaneVerticesInner();
											ArrayList<Vertex> verticesPriority = pLane.getLaneVerticesInner();											
											pInIn = getConflictIntersectionPoint(verticesYield, verticesPriority, longitudinalInInYield, longitudinalInInPriority);

											double longitudinalInOutYield = 0;
											double longitudinalInOutPriority = 0;
											verticesYield = yLane.getLaneVerticesInner();
											verticesPriority = pLane.getLaneVerticesOuter();
											pInOut = getConflictIntersectionPoint(verticesYield, verticesPriority, longitudinalInOutYield, longitudinalInOutPriority);
	
											double longitudinalOutInYield = 0;
											double longitudinalOutInPriority = 0;
											verticesYield = yLane.getLaneVerticesOuter();
											verticesPriority = pLane.getLaneVerticesInner();
											pOutIn = getConflictIntersectionPoint(verticesYield, verticesPriority, longitudinalOutInYield, longitudinalOutInPriority);
	
											double longitudinalOutOutYield = 0;
											double longitudinalOutOutPriority = 0;
											verticesYield = yLane.getLaneVerticesOuter();
											verticesPriority = pLane.getLaneVerticesOuter();
											pOutOut = getConflictIntersectionPoint(verticesYield, verticesPriority, longitudinalOutOutYield, longitudinalOutOutPriority);
											
											if (! (pInIn == null & pInOut == null & pInIn == null & pInIn == null) )  {
											
												double longitudinalYield = 0;
												double longitudinalPriority = 0;
		
												Polygon conflictArea = new Polygon();
												if (longitudinalInInYield <= longitudinalInOutYield)  {
													if (longitudinalInInYield <= longitudinalOutInYield)  {
														longitudinalYield = longitudinalInInYield;
													}
													else {
														longitudinalYield = longitudinalOutInYield;
													}
												}
												else if (longitudinalInOutYield < longitudinalInInYield)  {
													if (longitudinalInOutYield <= longitudinalOutOutYield)  {
														longitudinalYield = longitudinalInOutYield;
													}
													else {
														longitudinalYield = longitudinalOutOutYield;
													}
												}
												if (longitudinalInInPriority <= longitudinalInOutPriority)  {
													if (longitudinalInInPriority <= longitudinalOutInPriority)  {
														longitudinalPriority = longitudinalInInPriority;
													}
													else {
														longitudinalPriority = longitudinalOutInPriority;
													}
												}
												else if (longitudinalInOutPriority < longitudinalInInPriority)  {
													if (longitudinalInOutPriority <= longitudinalOutOutPriority)  {
														longitudinalPriority = longitudinalInOutPriority;
													}
													else {
														longitudinalPriority = longitudinalOutOutPriority;
													}
												}
		
												// determine the stopLines of the incoming Link
												StopLine stopLine = yLane.getUp().get(0).getStopLine();
												Double x = null;
												Double y = null;
												if (pInIn != null)  {
													x = pInIn.getX();
													y = pInIn.getY();
													conflictArea.addPoint(x.intValue(), y.intValue());
												}
												if (pInOut != null)  {
													x = pInOut.getX();
													y = pInOut.getY();
													conflictArea.addPoint(x.intValue(), y.intValue());
												}
												if (pOutIn != null)  {
													x = pOutIn.getX();
													y = pOutIn.getY();
													conflictArea.addPoint(x.intValue(), y.intValue());
												}
												if (pOutOut != null)  {	
													x = pOutOut.getX();
													y = pOutOut.getY();
													conflictArea.addPoint(x.intValue(), y.intValue());
												}
												if (cType.equals(conflictType.MERGE))
													yLane.addMergingYieldToLaneList(pLane);
												else if (cType.equals(conflictType.CROSSING))
													yLane.addCrossingYieldToLaneList(pLane);
												priorityConflict = new PriorityConflict(pLane, longitudinalYield, yLane, longitudinalPriority, cType, conflictArea);
												//  add conflict to the relevant stopLine 
												if (stopLine == null)		
													System.out.print("no Stopline created or found");
												else
													stopLine.addConflicts(priorityConflict);
											}
										}
									}
								}
							}

					//}
				}
			}
			
			System.out.format("Adding %d links to network for node %s\n", newLinks.size(), name);
		}
    }
    
    
    public Point2D.Double getConflictIntersectionPoint(ArrayList<Vertex> verticesA, ArrayList<Vertex> verticesB, double longitudinalA, double longitudinalB)   {
		Vertex prevA = null;
		Vertex prevB = null;
		Point2D.Double p = null; 
		for (Vertex vA : verticesA)  {
			if (! (prevA == null)) {
				longitudinalB = 0;
				for (Vertex vB : verticesB) {
					if (! (prevB == null))   {
						Line2D.Double l1 = new Line2D.Double(prevA.getX(),prevA.getY(),vA.getX(),vA.getY());
						Line2D.Double l2 = new Line2D.Double(prevB.getX(),prevB.getY(),vB.getX(),vB.getY());
						if (Planar.lineIntersectsLine(l1, l2)) {
							p =  Planar.intersection(l1, l2);
						}
						if (p != null)  {
							longitudinalA += Math.pow(p.getX()-prevA.getX(), p.getY()-prevA.getY());  															
							longitudinalB += Math.pow(p.getX()-prevB.getX(), p.getY()-prevB.getY());  															
							break;
						}
						longitudinalB += Math.pow(vB.getX()-prevB.getX(), vB.getY()-prevB.getY());  
					}
					prevB = vB;
				}
				longitudinalA += Math.pow(vA.getX()-prevA.getX(), vA.getY()-prevA.getY()); 
				if (p != null)
					break;
			}
			prevA = vA;												
		}
		return p;
    }
    
    private static Lane newLaneConnection(Lane upLane, Lane downLane, boolean createCurve, Lane oldLane, boolean sameOutlink) {
		// create a new lane that connects the in and outgoing lane 
		double laneWidth = downLane.getWidth();
		Lane newLane = new Lane(null, null, null, 0, laneWidth, Lane.NOORIGIN, Lane.NODESTINATION);
		newLane.addDownLane(downLane);
		newLane.addUpLane(upLane);							
		downLane.addUpLane(newLane);
		upLane.addDownLane(newLane);
		if (null == upLane.getLaneVerticesCenter()) {
			System.err.println("newLaneConnection: linkPointList is null");
			upLane.getLaneVerticesCenter();
			return null;
		}
		if (null == downLane.getLaneVerticesCenter()) {
			System.err.println("newLaneConnection: linkPointList is null");
			return null;
		}
		if (createCurve)  {
			ArrayList<Vertex> up = upLane.getLaneVerticesCenter();
			ArrayList<Vertex> down = downLane.getLaneVerticesCenter();		
			Point2D.Double ctrlPoint = 	Curves.createControlPoint(up, down);
			newLane.setCtrlPointCenter(ctrlPoint);
			ArrayList<Vertex> laneVerticesCenter = new ArrayList<Vertex>();
			laneVerticesCenter = Curves.connectVerticesCurve(up, down, ctrlPoint, 0.3);
			// laneVerticesCenter.get(0).setPoint(up.get(up.size()-1).getX(), up.get(up.size()-1).getY(), up.get(up.size()-1).getZ());
			// laneVerticesCenter.get(laneVerticesCenter.size()-1).setPoint(down.get(0).getX(), down.get(0).getY(), down.get(0).getZ());
			newLane.setDesignLine(laneVerticesCenter);			
			newLane.setLaneVerticesInner(Planar.createParallelVertices(laneVerticesCenter, - newLane.getLateralWidth() / 2));
			newLane.setLaneVerticesOuter(Planar.createParallelVertices(laneVerticesCenter, + newLane.getLateralWidth() / 2));
		}
		else  {
			boolean sameUp = false;
			boolean sameDown = false;
			ArrayList<Vertex> laneVerticesCenter = new ArrayList<Vertex>();
			ArrayList<Vertex> prevLaneVerticesCenter = oldLane.getUp().get(0).getLaneVerticesCenter();
			Vertex oldUp = prevLaneVerticesCenter.get(prevLaneVerticesCenter.size()-1);
			prevLaneVerticesCenter = oldLane.getDown().get(0).getLaneVerticesCenter();
			Vertex oldDown = prevLaneVerticesCenter.get(0);
			Vertex up = upLane.getLaneVerticesCenter().get(upLane.getLaneVerticesCenter().size()-1);
			Vertex down = downLane.getLaneVerticesCenter().get(0);		
			
			if (up.equals(oldUp)  && sameOutlink)
				sameUp = true;
			if (down.equals(oldDown))
				sameDown = true;
			// create parallel vertices
			prevLaneVerticesCenter = oldLane.getLaneVerticesCenter();
			laneVerticesCenter = Planar.createParallelVertices(prevLaneVerticesCenter, -oldLane.getLateralWidth());
			// if same upLane: create weighted vertices inbetween
			if (sameUp == true)  {
				laneVerticesCenter = Planar.createPartlyParallelVertices(prevLaneVerticesCenter, laneVerticesCenter, sameUp, sameDown);
			}
			newLane.setDesignLine(laneVerticesCenter);
			newLane.setLaneVerticesInner(Planar.createParallelVertices(laneVerticesCenter, - newLane.getLateralWidth() / 2));
			newLane.setLaneVerticesOuter(Planar.createParallelVertices(laneVerticesCenter, + newLane.getLateralWidth() / 2));
		}
		return newLane;   	
    }
    
    /**
     * Create a {@link Link} at a junction (turning movements).
     * @param currentInLane
     * @param currentOutLane
     * @param incoming
     * @param newLanes
     * @param rmaList
     * @return {@link Link}; the newly created Link
     */
    private Link createJunctionLink(DirectionalLink incoming, ArrayList<Lane> newLanes, ArrayList<RoadMarkerAlong> rmaList)  {    	
		CrossSectionElement inCse = newLanes.get(0).getUp().get(0).getCse();
		Node expandNode1 = expandNode(true, incoming.link, inCse);
		CrossSectionElement outCse = newLanes.get(0).getDown().get(0).getCse();
		Link outLink = outCse.getCrossSection().getLink();
		Node expandNode2 = expandNode(false, outLink, outCse);
		ArrayList<CrossSectionElement> cseList = new ArrayList<CrossSectionElement>();
		CrossSection newCs = new CrossSection(0, 0, cseList);
		
		double width = 0.0;
		String typeContinuous = RoadMarkerAlongTemplate.ALONG_CONTINUOUS;
		String typeStriped = RoadMarkerAlongTemplate.ALONG_STRIPED;
		RoadMarkerAlong rma = null;
		int count = 0;
		Lane prev = null;
		ArrayList<Vertex> tempVertices = new ArrayList<Vertex>(); 
		for (Lane lane : newLanes)  {
			if (null == lane) {
				System.err.println("createJunctionLink: Skipping null lane");
				// TODO figure out if this still happens and fix it
				continue;
			}
			if (count == 0)   {
				rma = new RoadMarkerAlong(typeContinuous, width);
			}
			else {
				rma = new RoadMarkerAlong(typeStriped, width);
			}
			rma.setVertices(lane.getLaneVerticesInner());
			count++;
			rmaList.add(rma);
			width += lane.getLateralWidth();
			if (prev != null)  {
				lane.setLeft(prev);
				prev.setRight(lane);
				if (rma.getType() == typeStriped)  {
					lane.setGoLeft(true);
					prev.setGoRight(true);
				}				
			}
			prev = lane;
			tempVertices = lane.getLaneVerticesOuter();
		}
		// closing roadMarkerAlong 
		rma = new RoadMarkerAlong(typeContinuous, width);
		rma.setVertices(tempVertices);
		rmaList.add(rma);		
		CrossSectionElement newCse = new CrossSectionElement(newCs, "road", width, rmaList, null);
		
		ArrayList<Vertex> vertexInner = rmaList.get(0).getVertices();
		newCse.setVerticesInner(rmaList.get(0).getVertices());
		newCse.setVerticesOuter(tempVertices);
		for (Lane lane : newLanes) {
			newCse.addCrossSectionObject(lane);
			lane.setCse(newCse);
		}
		cseList.add(newCse);
		ArrayList<CrossSection> csList = new ArrayList<CrossSection>();
		csList.add(newCs);
		// Generate a unique name for the new link that makes some sense
		String linkName = "_" + expandNode1.nodeID + "_" + expandNode2.nodeID + "_";
		for (int i = 1; ; i++)
			if (null == network.lookupLink(linkName + i)) {
				linkName += i;
				break;
			}
		Link newLink = network.addLink(linkName, expandNode1.nodeID, expandNode2.nodeID, 0, false, csList,  vertexInner);
		newCs.setLink(newLink);
		newLink.setFromNodeExpand(expandNode1);
		newLink.setToNodeExpand(expandNode2);
		newLink.setAutoGenerated(true);
		// if there are parallel lanes for a turn, provide the left and right info
		return newLink;
    }
 
    private static ArrayList<Vertex> connectVertices(ArrayList<Vertex> up, ArrayList<Vertex> down) {
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();	
		Vertex start = up.get(up.size() - 1);
		Vertex end = down.get(0);
		vertices.add(start);
		vertices.add(end);
		return vertices;
    }

    
    private Node expandNode(boolean in, Link link, CrossSectionElement inCse) {
    	Node expandedNode = null;
    	if ((in && (link.getToNodeExpand() == link.getToNode_r())) || 
    			((!in) && (link.getFromNodeExpand() == link.getFromNode_r()))) {
    		// Node is not (yet) expanded for this link; create an extra node
			String nodeName = "" + link.getFromNode_r().getNodeID() + "_" + link.getToNode_r().getNodeID();
			// TODO: rewrite this using vertexFromNode
			ArrayList<Vertex> linkPointList = inCse.createAndCleanLinkPointListInner(true, true, false);
			if (linkPointList.size() < 1) {
				System.err.println("expandNode: linkPointList is too short");
				return null;	// TODO figure out if this is fatal further down the road
			}
			//System.out.format("expandNode: inserting extra node %s between nodes %s and %s\r\n", 
			//		nodeName, link.getFromNode_r().getName_r(), link.getToNode_r().getName_r());
			Vertex v = linkPointList.get(in ? linkPointList.size() - 1 : 0);
			expandedNode = new Node(network, nodeName, network.nextNodeID(), v.x, v.y, v.z);
			network.addExpandedNode(expandedNode);
			// Connect the Link to the newly created Node
			if (in)
				link.setToNodeExpand(expandedNode);
			else
				link.setFromNodeExpand(expandedNode);
	    } else if (in)	// incoming link and node is already expanded
			expandedNode = link.getToNodeExpand();
		else			// outgoing link and node is already expanded
			expandedNode = link.getFromNodeExpand();
		return expandedNode;
    }
    
    /**
     * Draw this Node on a GraphicsPanel.
     * @param graphicsPanel GraphicsPanel; graphicsPanel to draw onto
     */
	public void paint(GraphicsPanel graphicsPanel) {
        final int nonSelectedNodeDiameter = 6;
        final int selectedNodeDiameter = 20;

    	Point2D.Double point = getPoint();            
    	graphicsPanel.setStroke(1F);
        final Color color = network.isExpandedNode(this) ? Color.blue : Color.RED;
        graphicsPanel.setColor(color);
        graphicsPanel.drawString(getName_r(), point);
        graphicsPanel.setStroke(0f);
        graphicsPanel.drawCircle(point, color, nonSelectedNodeDiameter);
        graphicsPanel.setStroke(6f);
    	if (network.selectedMicroZone != null)  {
        	for (Integer nodeNumber : network.selectedMicroZone.getNodeList()) {
				Node node = network.lookupNode(nodeNumber, true);
				if (this == node)
					graphicsPanel.drawCircle(point, Color.BLUE, selectedNodeDiameter);
        	}
    	}
        if ((null != network.startNode) && (getNodeID() == network.startNode.getNodeID()))
        	graphicsPanel.drawCircle(point, Color.RED, selectedNodeDiameter);
        if ((null != network.endNode) && (getNodeID() == network.endNode.getNodeID()))
        	graphicsPanel.drawCircle(point, Color.PINK, selectedNodeDiameter);
        if (null != conflictArea) {
        	graphicsPanel.setStroke(1F);
        	graphicsPanel.setColor(Color.CYAN);
        	graphicsPanel.drawPolyLine(conflictArea);
        }
        if (null != circle) {
        	graphicsPanel.setStroke(1F);
        	double r = graphicsPanel.translate(new Point2D.Double(circle.radius(), 0)).distance(graphicsPanel.translate(new Point2D.Double(0, 0)));
        	graphicsPanel.drawCircle(circle.center(), Color.CYAN, (int) (2 * r));
        }
	}

}