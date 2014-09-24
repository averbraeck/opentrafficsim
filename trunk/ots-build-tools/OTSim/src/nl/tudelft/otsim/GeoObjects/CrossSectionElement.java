package nl.tudelft.otsim.GeoObjects;

import java.awt.Color;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.TreeSet;

import nl.tudelft.otsim.FileIO.ParsedNode;
import nl.tudelft.otsim.FileIO.StaXWriter;
import nl.tudelft.otsim.FileIO.XML_IO;
import nl.tudelft.otsim.GUI.GraphicsPanel;
import nl.tudelft.otsim.GUI.InputValidator;
import nl.tudelft.otsim.SpatialTools.Planar;
import nl.tudelft.otsim.Utilities.Reversed;
import nl.tudelft.otsim.Utilities.Sorter;

/**
 * A CrossSectionElement describes the surface of one lateral component in a
 * CrossSection.
 * 
 * @author Guus F Tamminga, Peter Knoppers
 */
public class CrossSectionElement implements XML_IO {
	/** Label in XML representation of a CrossSectionElement */
	public static final String XMLTAG = "element";
	/** Lateral reference is left edge */
	public final static int LateralReferenceLeft = 1;
	/** Lateral reference is right edge */
	public final static int LateralReferenceCenter = 2;
	/** Lateral reference is centered between left edge and right edge */
	public final static int LateralReferenceRight = 3;
	
	/** Label of name in XML representation of a CrossSectionElement */
	private static final String XML_NAME = "name";
	/** Label of width in XML representation of a CrossSectionElement */
	private static final String XML_WIDTH = "width";
	/** Label of speedLimit in XML representation of a CrossSectionElement */
	private static final String XML_SPEEDLIMIT = "speedLimit";

    private Double width;
	private String crossSectionElementTypologyName;
	private CrossSectionElementTypology crossSectionElementTypology;
    private TreeSet<CrossSectionObject> objects = new TreeSet<CrossSectionObject> (new CompareObjects());
    private final CrossSection crossSection;
    private CrossSectionElement connectedFrom = null;
	private int neighborIndex = -1;
	private ArrayList<Vertex> verticesInner;
	private ArrayList<Vertex> verticesOuter;
	private double speedLimit = 50 / 3.6;	// 50 km/h in m/s
	
	class CompareObjects implements Comparator<CrossSectionObject> {
		@Override
		public int compare (CrossSectionObject left, CrossSectionObject right) {
			if ((null == left) || (null == right))
				throw new Error("null CrossSectionObject");
			// Compare lateral position
			if (left.lateralPosition < right.lateralPosition)
				return -1;
			if (left.lateralPosition > right.lateralPosition)
				return 1;
			// Compare lateral width
			if (left.lateralWidth < right.lateralWidth)
				return -1;
			if (left.lateralWidth > right.lateralWidth)
				return 1;
			// Compare longitudinal position
			if (left.longitudinalPosition < right.longitudinalPosition)
				return -1;
			if (left.longitudinalPosition > right.longitudinalPosition)
				return 1;
			// Compare longitudinal length
			if (left.longitudinalLength < right.longitudinalLength)
				return 1;
			if (left.longitudinalLength > right.longitudinalLength)
				return -1;
			// Compare the class names
			int diff = left.getClass().getName().compareTo(right.getClass().getName());
			if (0 != diff)
				return diff;
			// Compare the hasCodes
			diff = right.hashCode() - left.hashCode();
			return diff;
		}
	}
	
	private void fixCrossSectionElementTypology() {
		if (null != crossSectionElementTypology)
			return;
		if (null == crossSection)
			throw new Error("CrossSection is null");
		if (null == crossSection.getLink())
			throw new Error("Link is null");
		if (null == crossSection.getLink().network)
			throw new Error("Network is null");
		for (CrossSectionElementTypology cset : crossSection.getLink().network.getCrossSectionElementTypologyList()) {
			if (cset.getName_r().equals(crossSectionElementTypologyName)) {
				crossSectionElementTypology = cset;
				return;
			}
		}
		throw new Error("Cannot find CrossSectionElementTypology \"" 
				+ crossSectionElementTypologyName 
				+ "\" in the Network of the Link of this CrossSection");
	}

	/**
	 * Create a CrossSectionElement with specified parent CrossSection, name, 
	 * ID, CrossSectionElementTypology, width, RoadMarkerAlong list and TurnArrow list
	 * @param parent CrossSection that the new CrossSectionElement will belong 
	 * to. The new CrossSectionElement is <b>not</b> automatically added to the
	 * CrossSectionElement list of the parent. This is the responsibility of the
	 * caller.
	 * @param crossSectionElementTypologyName name of the CrossSectionElementTypology of the
	 * new CrossSectionElement.
	 * @param crossSectionElementWidth Double; width of the new CrossSectionElement in m
	 * @param roadMarkerAlongList ArrayList&lt;RoadMarkerAlong&gt; List of 
	 * longitudinal road markers of the new CrossSectionElement
	 * @param turnArrowList ArrayList&lt;TurnArrow&gt; List of TurnArrows on
	 * the new CrossSectionElement
	 */
	public CrossSectionElement(CrossSection parent, String crossSectionElementTypologyName, Double crossSectionElementWidth, ArrayList<RoadMarkerAlong> roadMarkerAlongList, ArrayList<TurnArrow> turnArrowList) {
		if (null == parent)
			throw new Error("parent cannot be null");
		if (null == crossSectionElementTypologyName)
			throw new Error("crossSectionElementTypologyName cannot be null");
		this.crossSection = parent;
		this.crossSectionElementTypologyName = crossSectionElementTypologyName;
        this.width = crossSectionElementWidth; 
		//this.roadMarkerAlongList = roadMarkerAlongList;
		//this.turnArrowList = turnArrowList;
        for (RoadMarkerAlong rma : roadMarkerAlongList) {
        	rma.setCrossSectionElement(this);
        	objects.add(rma);
        }
        if (null != turnArrowList)
        	for (TurnArrow ta : turnArrowList)
        		objects.add(ta);
		// HACK
		//if (crossSectionElementTypologyName.equals("road"))
		//	objects.add(new VehicleDetector(this, 50d, 10d, 0d, 3d, "Detector1"));
	}

	/**
	 * Duplicate a CrossSectionElement, replacing the CrossSection by the
	 * specified one.
	 * @param cse CrossSectionElement that must be duplicated
	 * @param cs CrossSection that will be the parent of the new CrossSectionElement
	 */
	public CrossSectionElement(CrossSectionElement cse, CrossSection cs) {
		this.crossSection = cs;
		this.crossSectionElementTypology = cse.crossSectionElementTypology;
        this.width = cse.width; 
        // Duplicate the objects
        for (CrossSectionObject cso : cse.objects)
        	this.objects.add(cso);
		//this.roadMarkerAlongList = new ArrayList<RoadMarkerAlong>();
		//for (RoadMarkerAlong rma : cse.getRoadMarkersAlongList())
		//	this.roadMarkerAlongList.add(new RoadMarkerAlong(rma));
	}
	
	/**
	 * Create a CrossSectionElement from a parsed XML file.
	 * @param parent {@link CrossSection}; the CrossSection that will own the new CrossSectionElement
	 * @param pn {@link ParsedNode} the root of the CrossSectionElement in the XML file
	 * @throws Exception
	 */
	public CrossSectionElement(CrossSection parent, ParsedNode pn) throws Exception {
		if (null == parent)
			throw new Exception("Parent may not be null");
		crossSection = parent;
		crossSectionElementTypologyName = null;
		width = Double.NaN;
		for (String fieldName : pn.getKeys()) {
			String value = pn.getSubNode(fieldName, 0).getValue();
			if (fieldName.equals(XML_NAME))
				crossSectionElementTypologyName = value;
			else if (fieldName.equals(XML_WIDTH))
				width = Double.parseDouble(value);
			else if (fieldName.equals(XML_SPEEDLIMIT))
				speedLimit = Double.parseDouble(value) / 3.6;
			else if (fieldName.equals(RoadMarkerAlong.XMLTAG))
				for (int index = 0; index < pn.size(fieldName); index++)
					objects.add(new RoadMarkerAlong(this, pn.getSubNode(fieldName, index)));
			else if (fieldName.equals(TurnArrow.XMLTAG))
				for (int index = 0; index < pn.size(fieldName); index++)
					objects.add(new TurnArrow(this, pn.getSubNode(fieldName, index)));
			else if (fieldName.equals(StopLine.XMLTAG))
						for (int index = 0; index < pn.size(fieldName); index++)
							objects.add(new StopLine(this, pn.getSubNode(fieldName, index)));			
			else if (fieldName.equals(TrafficLight.XMLTAG))
				for (int index = 0; index < pn.size(fieldName); index++)
					objects.add(new TrafficLight(this, pn.getSubNode(fieldName, index)));			
			else if (fieldName.equals(VehicleDetector.XMLTAG))
				for (int index = 0; index < pn.size(fieldName); index++)
					objects.add(new VehicleDetector(this, pn.getSubNode(fieldName, index)));			
			else if (fieldName.equals(VMS.XMLTAG))
				for (int index = 0; index < pn.size(fieldName); index++)
					objects.add(new VMS(this, pn.getSubNode(fieldName, index)));			
			else
				throw new Exception("Unknown field in CrossSectionElement: " + fieldName);
		}
		if (null == crossSectionElementTypologyName)
			throw new Exception("TypologyName of CrossSectionElement not defined " + pn.lineNumber + ", " + pn.columnNumber);
		if (Double.isNaN(width))
			throw new Exception("Width of CrossSectionElement not defined " + pn.lineNumber + ", " + pn.columnNumber);
	}

	/**
	 * Retrieve the parent CrossSection of this CrossSectionElement.
	 * @return CrossSection; the parent of this CrossSectionElement
	 */
	public final CrossSection getCrossSection() {
		return crossSection;
	}
	
	/**
	 * Retrieve the name of the crossSectionElementTypology of this
	 * CrossSectionElement.
	 * @return String; the name of the crossSectionElementTypology
	 */
	public String getName_r() {
		fixCrossSectionElementTypology();
		return crossSectionElementTypology.getName_r();
	}

	/**
	 * Retrieve the width of this CrossSectionElement
	 * @return Double; Width of this CrossSectionElement in m
	 */
	public double getWidth_r() {
		return width;
	}

	/**
	 * Change the width of this CrossSectionElement.
	 * @param width Double; new width of this CrossSectionElement in m
	 */
	public void setWidth_w(double width) {
		this.width = width;
	}

	/**
	 * Retrieve the speed limit in km/h.
	 * @return String; the speed limit in km/h
	 */
	public String getSpeedLimit_r() {
		return String.format("%.0f", speedLimit * 3.6);
	}
	
	/**
	 * Retrieve the speed limit im m/s.
	 * @return Double; the speed limit in m/s
	 */
	public double getSpeedLimit() {
		return speedLimit;
	}
	
	/**
	 * Set/Change the speed limit.
	 * @param newLimit Double; new speed limit in km/h
	 */
	public void setSpeedLimit_w(double newLimit) {
		speedLimit = newLimit / 3.6;
	}
	
	/**
	 * Get an {@link InputValidator} for speed limit values.
	 * @return {@link InputValidator}; to check speed limit values
	 */
	@SuppressWarnings("static-method")
	public InputValidator validateSpeedLimit_v() {
		return new InputValidator("[1-9][0-9]*", 1, 200);
	}
	/**
	 * Obtain the lateral offset of the left edge of this CrossSectionElement
	 * @return Double; lateral offset of the left edge of this CrossSectionElement
	 */
	public double getInnerLateralPosition_r() {
		return getLateralPosition(LateralReferenceLeft);
	}
	
	/**
	 * Obtain the lateral offset of the right edge of this CrossSectionElement
	 * @return Double; lateral offset of the right edge of this CrossSectionElement
	 */
	public double getOuterLateralPosition_r() {
		return getLateralPosition(LateralReferenceRight);
	}
	
	/**
	 * Obtain the lateral offset of an edge of this CrossSectionElement
	 * @param lateralReference Integer; select the inner edge, outer edge, or
	 * center line of this CrossSectionElement
	 * @return Double; lateral offset from the design line of the link
	 */
    public double getLateralPosition(int lateralReference) {
		double lateralPosition = crossSection.getLateralOffset_r();
        for (CrossSectionElement cse : crossSection.getCrossSectionElementList_r()) {
        	double elementWidth = cse.getCrossSectionElementWidth();
        	if (this != cse)
        		lateralPosition += elementWidth;
        	else {
	        	if (LateralReferenceLeft == lateralReference)
	        		return lateralPosition;
	        	else if (LateralReferenceCenter == lateralReference)
	        		return lateralPosition + elementWidth / 2;
	        	else if (LateralReferenceRight == lateralReference)
	        		return lateralPosition + elementWidth;
	            throw new Error("Bad lateralReference: " + lateralReference);
        	}
		}
        throw new Error("Cannot find myself in CrossSectionElementList of crossSection");
	}

    /**
     * Obtain the index of the CrossSectionElement that is paired with this 
     * one in the adjoining CrossSection.
     * @return Integer (-1 if this CrossSectionElement is not paired)
     */
	public int getNeighborIndex() {
		return neighborIndex;
	}
	
	/**
	 * Set the index of this CrossSectionElement to which it is paired in the
	 * adjoining CrossSection.
	 * @param predecessorIndex
	 */
	public void setNeighborIndex(int predecessorIndex) {
		this.neighborIndex = predecessorIndex;
	}
	
	/**
	 * Obtain a String describing this CrossSectionElement.
	 */
	@Override
	public String toString() {
		fixCrossSectionElementTypology();
		return String.format("%s, %.2fm %s", getName_r(), width, crossSectionElementTypology.toString());
	}
	
	/**
	 * Check whether this CrossSectionElement can be paired with another
	 * CrossSectionElement.
	 * <br /> The current implementation only compares the names of the
	 * underlying {@link CrossSectionElementTypology 
	 * CrossSectionElementTypologies} and the width.
	 * @param other CrossSectionElement to check compatibility with
	 * @return Boolean; true if the other CrossSectionElement is compatible
	 * with this one; false otherwise
	 */
	public boolean compatibleWith (CrossSectionElement other) {
		if (! getName_r().equals(other.getName_r()))
			return false;
		return width == other.width;
	}

	/**
	 * Set/change the predecessor of this CrossSectionElement.
	 * @param from CrossSectionElement; the new predecessor of this
	 * CrossSectionElement (may be null)
	 */
    public void setConnectedFrom(CrossSectionElement from) {
    	connectedFrom = from;
    }
    
    /**
     * Retrieve the predecessor of this CrossSectionElement.
     * @return CrossSectionElement; the predecessor of this CrossSectionElement
     * (may be null)
     */
    public CrossSectionElement getConnectedFrom() {
    	return connectedFrom;
    }
    
    
    /**
     * Generate the reference line for this CrossSectionElement.
     * @param lateralReference One of the values {@link #LateralReferenceLeft},
     * {@link #LateralReferenceCenter}, or {@link #LateralReferenceRight}.
     * @param adjust Boolean; if true, the returned reference line is adjusted
     * to connect to the corresponding preceding CrossSectionElement; if false,
     * the unadjusted reference line is returned
     * @param adjustLink Boolean; if true, the returned reference line is adjusted
     * to connect to the corresponding CrossSectionElement of the preceding link; if false,
     * the unadjusted reference line is returned 
     * @return ArrayList&lt;{@link Vertex}&gt;; the list of vertices that
     * describes the requested reference line
     */
	public ArrayList<Vertex> getLinkPointList(int lateralReference, boolean adjust, boolean adjustLink) {
		return generateLinkPointList(lateralReference, adjust, adjustLink);
	}
	
	private ArrayList<Vertex> generateLinkPointList(int lateralReference, boolean adjust, boolean adjustLink) {
		if (null == crossSection)
			throw new Error("parent is null");
        ArrayList<Vertex> referenceVertices = crossSection.getVertices_r();
        ArrayList<Vertex> prevReferenceVertices = null;
        if ((this.connectedFrom != null) && adjustLink) {
        	prevReferenceVertices = new ArrayList<Vertex> ();
        	if (lateralReference == LateralReferenceLeft)
        		prevReferenceVertices = this.connectedFrom.getVerticesInner();
        	else if (lateralReference == LateralReferenceRight)
        		prevReferenceVertices = this.connectedFrom.getVerticesOuter();  
        }	
        if (null == referenceVertices)
        	throw new Error("referenceVertices is null");
        if (referenceVertices.size() < 2) {
        	System.err.println("List of referenceVertices for link " + crossSection.getLink().getName_r() + " from node " + crossSection.getLink().getFromNode_r().getName_r() + " to node " + crossSection.getLink().getToNode_r().getName_r() + " is too short");
        	return new ArrayList<Vertex>();
        }	
        
        double myLateralPosition = getLateralPosition(lateralReference);
        double previousLateralPosition = myLateralPosition;
        int myRank = crossSection.getLink().getCrossSections_r().indexOf(crossSection);
        if (adjust && (null != connectedFrom) && (myRank >= 0))
        	previousLateralPosition = connectedFrom.getLateralPosition(lateralReference);
        else if (adjust && (myRank >= 0)) {
        	ArrayList<CrossSectionElement> cseList = crossSection.getCrossSectionElementList_r();
        	int myLateralIndex = cseList.indexOf(this);
        	int otherIndex;
        	// Look for a lower ranked CrossSectionElement that IS connected
        	for (otherIndex = myLateralIndex; --otherIndex >= 0; ) {
        		CrossSectionElement cse = cseList.get(otherIndex);
        		if (null != cse.connectedFrom) {
        			previousLateralPosition = cse.connectedFrom.getLateralPosition(LateralReferenceRight);
        			break;
        		}
        	}
        	if (otherIndex < 0)	// Look for a higher ranked CrossSectionElement that is connected
        		for (otherIndex = myLateralIndex; ++otherIndex < cseList.size(); ) {
            		CrossSectionElement cse = cseList.get(otherIndex);
            		if (null != cse.connectedFrom) {
            			previousLateralPosition = cse.connectedFrom.getLateralPosition(LateralReferenceRight);
            			break;
            		}
        		}
    		//if (otherIndex >= cseList.size())
        	//	System.out.println("No connected CrossSectionElement found");
        }

		if (referenceVertices.size() < 2)
			throw new Error("Malformed reference vertices");
        ArrayList<Vertex> result = Planar.createParallelVertices(referenceVertices, prevReferenceVertices, previousLateralPosition, myLateralPosition);
		if (result.size() < 2) {
			System.err.println("Malformed parallel vertices");
			return result;
			//throw new Error("Malformed parallel vertices");
		}
        ArrayList<CrossSection> csList = crossSection.getLink().getCrossSections_r();
        if (crossSection == csList.get(0) && this.connectedFrom == null)
        	result = crossSection.getLink().getFromNode_r().truncateAtConflictArea(result);
		if (result.size() < 2)
			throw new Error("Malformed truncated parallel vertices");
        //&& ! csList.get(csList.size() - 1).getLink().getToNodeExpand().equals(csList.get(csList.size() - 1).getLink().getToNode_r())
        if (crossSection == csList.get(csList.size() - 1)  && ! adjustLink)  {
        	result = crossSection.getLink().getToNode_r().truncateAtConflictArea(result);
        	//if (crossSection.getLongitudinalPosition_r()== 0)
        		//System.out.println("Node at start");       		
        }
        if (result.size() < 2)
        	System.err.println("too short");
        final double huge = 2000;	// [m]
        Vertex prevVertex = null;
        for (Vertex v : result) {
        	if (null != prevVertex)
        		if (prevVertex.distance(v) > huge) {
        			System.out.println("Huge gap in generated LinkPointList");
        			crossSection.getVertices_r();
        		}
        	prevVertex = v;
        }
        return result;
	}

	/**
	 * Retrieve a {@link Vertex} of the inner reference line of this 
	 * CrossSectionElement.
	 * @param index Integer; rank of the returned Vertex; negative values
	 * select the Vertex counting from the end of the reference line
	 * @param cleanup Boolean; if true, duplicate vertices are removed from the
	 * reference line; if false, duplicate vertices are retained
	 * @param adjust Boolean; if true, the reference line is adjusted to
	 * connect to the corresponding preceding CrossSectionElement; if false,
	 * the reference line is not adjusted
	 * @param adjustLink Boolean; if true; the result is adjusted to connect
	 * to the preceding {@link Link}; if false; no such adjustment is performed
	 * @return {@link Vertex} the selected Vertex from the inner reference line
	 */
	public final Vertex getLinkPointInner(int index, boolean cleanup, boolean adjust, boolean adjustLink) {
		ArrayList<Vertex> list = cleanLinkPointList(generateLinkPointList(LateralReferenceLeft, adjust, adjustLink), cleanup);
		if (index < 0)
			index = list.size() + index;
		return list.get(index);
	}
	
	/**
	 * Retrieve a {@link Vertex} of the outer reference line of this 
	 * CrossSectionElement.
	 * @param index Integer; rank of the returned Vertex; negative values
	 * select the Vertex counting from the end of the reference line
	 * @param cleanup Boolean; if true, duplicate vertices are removed from the
	 * reference line; if false, duplicate vertices are retained
	 * @param adjust Boolean; if true, the reference line is adjusted to
	 * connect to the corresponding preceding CrossSectionElement; if false,
	 * the reference line is not adjusted
	 * @param adjustLink Boolean; if true; the result is adjusted to connect
	 * to the preceding {@link Link}; if false; no such adjustment is performed
	 * @return {@link Vertex} the selected Vertex from the inner reference line
	 */
	public Vertex getLinkPointOuter(int index, boolean cleanup, boolean adjust, boolean adjustLink) {
		ArrayList<Vertex> list = cleanLinkPointList(generateLinkPointList(LateralReferenceRight, adjust, adjustLink), cleanup);
		if (index < 0)
			index = list.size() + index;
		return list.get(index);
	}
	
	/**
	 * Retrieve a list of {@link CrossSectionObject CrossSectionObjects} of 
	 * this CrossSectionElement. 
	 * @param klass Class of the CrossSectionObjects to select
	 * @return ArrayList&lt;{@link CrossSectionObject}&gt; the list of
	 * CrossSectionObjects of this CrossSectionEleement that have the specified 
	 * type
	 */
	public List<CrossSectionObject> getCrossSectionObjects(Class<?> klass) {
		ArrayList<CrossSectionObject> result = new ArrayList<CrossSectionObject> ();
		for (CrossSectionObject cso : objects)
			if (klass.isInstance(cso))
				result.add(cso);
		return Sorter.asSortedList(result);
	}
	
	/**
	 * Retrieve a list of {@link CrossSectionObject CrossSectionObjects} of 
	 * this CrossSectionElement.
	 * <br /> This method is provided primarily for the 
	 * {@link nl.tudelft.otsim.GUI.ObjectInspector}.
	 * @return ArrayList&lt;{@link CrossSectionObject}&gt; the list of all
	 * CrossSectionObjects of this CrossSectionElement
	 */
	public List<CrossSectionObject> getCrossSectionObjects_r() {
		return getCrossSectionObjects(CrossSectionObject.class);
	}
	
	/**
	 * Delete all {@link CrossSectionObject CrossSectionObjects} of a
	 * specified class of this CrossSectionElement.
	 * <br /> To delete all CrossSectionObjects use 
	 * <code>CrossSectionObject.class</code> as the <code>klass</code>
	 * parameter.
	 * @param klass Class of the CrossSectionObjects to delete
	 */
	public void deleteCrossSectionObjects(Class<?> klass) {
		// Create a new one and copy everything NOT matching the specified Class
		TreeSet<CrossSectionObject> newList = new TreeSet<CrossSectionObject>(new CompareObjects());
		for (CrossSectionObject cso : objects)
			if (! (klass.isInstance(cso)))
				newList.add(cso);
		// Replace the stored set by the new set with the copied objects
		objects = newList;
		crossSection.getLink().network.setModified();
	}
	
	/**
	 * Create an item list for {@link CrossSectionObject CrossSectionObjects}
	 * that can be added through the {@link nl.tudelft.otsim.GUI.ObjectInspector}.
	 * @return ArrayList&lt;String&gt;; the list of item texts
	 */
	public ArrayList<String> itemizeAdd_i () {
		ArrayList<String> result = new ArrayList<String>();
		result.add("Traffic light");
		if (crossSectionElementTypology.getDrivable())
			result.add("Vehicle detector");
		result.add("Variable Message Sign (VMS)");
		return result;
	}
	
	/**
	 * Return a fixed string to be used to label the tree node in the
	 * {@link nl.tudelft.otsim.GUI.ObjectInspector} that must be clicked to add a
	 * {@link CrossSectionObject} to this CrossSectionElement.
	 * @return String; <code>"new cross section object"</code>
	 */
	@SuppressWarnings("static-method")
	public String getAdd_r () {
		return "new cross section object";
	}
	
	/**
	 * Add a {@link CrossSectionObject} to this CrossSectionElement.
	 * @param object Object; must be a String; description of the new
	 * {@link CrossSectionObject} that is to be created
	 */
	public void setAdd_w (Object object) {
		String description = (String) object;
		System.out.println("Should create (another) " + description);
		if (description.contains("Vehicle detector"))
			addCrossSectionObject(new VehicleDetector(this));
		else if (description.contains("Traffic light"))
			addCrossSectionObject(new TrafficLight(this));
		else if (description.contains("Variable Message Sign"))
			addCrossSectionObject(new VMS(this));
		else
			throw new Error("Do not know how to add a " + description);
		crossSection.getLink().network.setModified();
	}
	
	/**
	 * Delete one {@link CrossSectionObject} from the list of this 
	 * CrossSectionElement.
	 * @param object {@link CrossSectionObject}; the CrossSectionObject to
	 * delete
	 */
	public void deleteCrossSectionObject(CrossSectionObject object) {
		objects.remove(object);
		crossSection.getLink().network.setModified();
	}
	
    /**
     * Add a {@link CrossSectionObject} to this CrossSectionElement.
     * @param cso {@link CrossSectionObject}; the object to add to this 
     * CrossSectionElement
     */
	public void addCrossSectionObject(CrossSectionObject cso) {
		objects.add(cso);
	}

	/**
	 * Retrieve the {@link CrossSectionElementTypology} of this 
	 * CrossSectionElement.
	 * @return {@link CrossSectionElementTypology}
	 */
	public CrossSectionElementTypology getCrossSectionElementTypology() {
		fixCrossSectionElementTypology();
		return crossSectionElementTypology;
	}

	/**
	 * Set/change the {@link CrossSectionElementTypology} of this
	 * CrossSectionElement.
	 * @param crossSectionElementTypology {@link CrossSectionElementTypology};
	 * the new CrossSectionElementTypology of this CrossSectionElement
	 */
	public void setCrossSectionElementTypology(CrossSectionElementTypology crossSectionElementTypology) {
		this.crossSectionElementTypology= crossSectionElementTypology;
	}
	
	private static ArrayList<Vertex> cleanLinkPointList(ArrayList<Vertex> linkPointList, boolean cleanup) {
		if (! cleanup)
			return linkPointList;
		ArrayList<Vertex> result = new ArrayList<Vertex>();
		final double veryClose = 0.0001;
		Vertex prevVertex = null;
		for (Vertex v : linkPointList) {
			if ((null != prevVertex) && (v.distance(prevVertex) < veryClose))
				continue;	// skip this one
			result.add(v);
			prevVertex = v;
		}
		return result;
	}

	/**
	 * Retrieve the inner reference line of this CrossSectionElement.
	 * @param cleanup Boolean; if true, duplicate vertices are removed from the
	 * reference line; if false, duplicate vertices are retained
	 * @param adjust Boolean; if true, the reference line is adjusted to
	 * connect to the corresponding preceding CrossSectionElement; if false,
	 * the reference line is not adjusted
	 * @param adjustLink Boolean; if true; the result is adjusted to match the 
	 * last point on the preceding CrossSectionElement; if false; the result is
	 * not adjusted to match the last point on the preceding CrossSectionElement
	 * @return ArrayList&lt;{@link Vertex}&gt;; the vertices that describe the
	 * inner reference line
	 */
	public ArrayList<Vertex> createAndCleanLinkPointListInner(boolean cleanup, boolean adjust, boolean adjustLink) {
		return cleanLinkPointList(generateLinkPointList(LateralReferenceLeft, adjust, adjustLink), cleanup);
	}

	/**
	 * Retrieve the outer reference line of this CrossSectionElement.
	 * @param cleanup Boolean; if true, duplicate vertices are removed from the
	 * reference line; if false, duplicate vertices are retained
	 * @param adjust Boolean; if true, the reference line is adjusted to
	 * connect to the corresponding preceding CrossSectionElement; if false,
	 * the reference line is not adjusted
	 * @param adjustLink Boolean; if true; the result is adjusted to match the 
	 * last point on the preceding CrossSectionElement; if false; the result is
	 * not adjusted to match the last point on the preceding CrossSectionElement
	 * @return ArrayList&lt;{@link Vertex}&gt;; the vertices that describe the
	 * outer reference line
	 */
	public ArrayList<Vertex> createAndCleanLinkPointListOuter(boolean cleanup, boolean adjust, boolean adjustLink) {
		return cleanLinkPointList(generateLinkPointList(LateralReferenceRight, adjust, adjustLink), cleanup);
	}
	
	public ArrayList<Vertex> getVerticesInner() {
		return verticesInner;
	}

	public ArrayList<Vertex> getVerticesOuter() {
		return verticesOuter;
	}

	/**
	 * Retrieve the outer reference line of this CrossSectionElement.
	 * @param cleanup Boolean; if true, duplicate vertices are removed from the
	 * reference line; if false, duplicate vertices are retained
	 * @param adjust Boolean; if true, the reference line is adjusted to
	 * connect to the corresponding preceding CrossSectionElement; if false,
	 * the reference line is not adjusted
	 * @param adjustLink Boolean; if true; the result is adjusted to connect
	 * to the preceding CrossSectionElement; if false; no such adjustment is
	 * performed
	 * @return ArrayList&lt;{@link Vertex}&gt;; the vertices that describe the
	 * reference line
	 */
	public ArrayList<Vertex> getLinkPointListOuter(boolean cleanup, boolean adjust, boolean adjustLink) {
		return cleanLinkPointList(generateLinkPointList(LateralReferenceRight, adjust, adjustLink), cleanup);
	}

	/**
	 * Retrieve the name of the {@link CrossSectionElementTypology} of this
	 * CrossSectionElement.
	 * @return String; the name of the {@link CrossSectionElementTypology} of
	 * this CrossSectionElement
	 */
	public String getType_r() {
		if (null == crossSectionElementTypology)
			throw new Error("null crossSectionElementTypology");
		return crossSectionElementTypology.getName_r();
	}
	
	/**
	 * Generate a list of allowed new values for the 
	 * {@link CrossSectionElementTypology} for this CrossSectionElement. 
	 * @return ArrayList&lt;String&gt;; the list of possible values
	 */
	public ArrayList<String> itemizeType_i() {
		System.out.println("entering itemizeType_i");
		ArrayList<String> result = new ArrayList<String> ();
		System.out.println("crossSection is " + toString());
		for (CrossSectionElementTypology cset : crossSection.getLink().network.getCrossSectionElementTypologyList())
			result.add(cset.getName_r());
		System.out.println("returning from itemizeType_i");
		return result;
	}
	
	/**
	 * Set/change the type of {@link CrossSectionElementTypology} of this
	 * CrossSectionElement. The provided name is matched agains the names in
	 * the CrossSectionElementTypologyList of the Network to which the
	 * {@link Link} of the {@link CrossSection} of  this CrossSectionElement 
	 * belongs.
	 * @param crossSectionElementTypeName String; name of the new
	 * CrossSectionElementTypology for this CrossSectionElement
	 */
	public void setType_w(String crossSectionElementTypeName) {
		for (CrossSectionElementTypology cset : crossSection.getLink().network.getCrossSectionElementTypologyList())
			if (cset.getName_r().equals(crossSectionElementTypeName)) {
				crossSectionElementTypology = cset;
				crossSection.getLink().network.setModified();
				return;
			}
		throw new Error("Unknown crossSectionElementTypeName");
	}

	/**
	 * Retrieve the width of this CrossSectionElement.
	 * @return Double; the width of this CrossSectionElement in meters
	 */
	public Double getCrossSectionElementWidth() {
		return width;
	}

	/**
	 * Set/change the width of this CrossSectionElement.
	 * @param crossSectionElementWidth Double; the new width of this
	 * CrossSectionElement
	 */
	public void setCrossSectionElementWidth(double crossSectionElementWidth) {
		this.width = crossSectionElementWidth;
		crossSection.getLink().network.setModified();
	}
	
    /**
     * Fix areas where the number of lanes in connected CrossSectionElements
     * changes.
     * @param prevCSE CrossSectionElement; the linked predecessor of this
     * CrossSectionElement
     */
    public void fixLaneJump(CrossSectionElement prevCSE) {
    	// retrieve the lanes from the previous (upstream) crossSection
    	List<CrossSectionObject> prevCSECSO = prevCSE.getCrossSectionObjects(Lane.class);
    	// retrieve the lanes from the current crossSection
    	List<CrossSectionObject> curCSO = getCrossSectionObjects(Lane.class);
    	// is there an increase (or decrease: negative value) of the number of lanes?
    	int increaseOfLanes = curCSO.size() - prevCSECSO.size();
    	connectUnequalSections(prevCSE, prevCSECSO,  curCSO, increaseOfLanes);
    }
  
    public TreeSet<CrossSectionObject> getObjects() {
		return objects;
	}


	private static void ConnectLanes(Lane upLane, Lane downLane)   {
		// all lanes of two crossSections are connected to each other: 
		// 		prevLane(0) with curLane(0)
		// 		prevLane(1) with curLane(1)  ... etc.
		System.out.println("Connecting lane " + upLane.getID() + " to " + downLane.getID());
		 if ((802 == downLane.getID()) || (802 == upLane.getID()))
			System.out.println("Connecting lane 802");
		upLane.addDownLane(downLane);
		downLane.addUpLane(upLane);
    }
    
    private static void ConnectLanes(List<CrossSectionObject> prevCSECSO, List<CrossSectionObject> curCSO, int prev, int current)   {
		// all lanes of two crossSections are connected to each other: 
		// 		prevLane(0) with curLane(0)
		// 		prevLane(1) with curLane(1)  ... etc.
    	int dif = current - prev;
    	for (int i = current; i < prevCSECSO.size(); i++) {
    		Lane prevLane = (Lane) prevCSECSO.get(i - dif);
    		Lane curLane = (Lane) curCSO.get(i);
    		ConnectLanes(prevLane, curLane);
    		//prevLane.addDownLane(curLane);
    		//curLane.addUpLane(prevLane);
    	}
    }
    
    private static void connectUnequalSections(CrossSectionElement prevCSE, List<CrossSectionObject> prevCSECSO, List<CrossSectionObject> curCSO, int increaseOfLanes) {
    	List<CrossSectionObject> wide;
    	List<CrossSectionObject> narrow;
    	if (increaseOfLanes <= 0) {
    		wide = prevCSECSO;
    		narrow = curCSO;
    	} else {
			wide = curCSO;
			narrow = prevCSECSO;    			
    	}
    	// FIXME: This SHOULD happen in the code below, but that code fails to do it...
    	if ((wide.size() == 1) && (narrow.size() == 1)) {
    		ConnectLanes(wide, narrow, 0, 0);
    		return;
    	}
    		
    	// if there is a decrease in lanes, we try to find out which of the
		// previous lanes are connected and which previous lane(s) not
		int narrowLaneIndex = 0; // start with the lane nearest to the centerline of the link
		int wideLanesToConnect = wide.size();    		
		int narrowLanesToConnect = narrow.size();
		// Loop through all lanes of the widest crossSection (upstream)
    	for (int wideLaneIndex = 1; wideLaneIndex < wide.size(); wideLaneIndex++) {
    		//System.out.format("decreasing number of lanes: wideLaneIndex=%d of %d, narrowLaneIndex=%d of %d\r\n", wideLaneIndex, wide.size(), narrowLaneIndex, narrow.size());
    		if (narrowLaneIndex >= narrow.size()) 
	        	break; // All lanes of the current link are now connected. Break from this loop
    		if (wideLanesToConnect == narrowLanesToConnect) {
    			ConnectLanes(wide, narrow, wideLaneIndex - 1, narrowLaneIndex);
    			break;
    		}
    		// the lane of the narrow CrossSection
    		Lane narrowLane = (Lane) narrow.get(narrowLaneIndex);
    		// the first lane of the previous CrossSection
    		Lane wideLaneOne = (Lane) wide.get(wideLaneIndex - 1);    		
    		// the second lane of the wide CrossSection
    		Lane wideLaneTwo = (Lane) wide.get(wideLaneIndex);
    		// the difference in lateral position between the two wide lanes and the narrow one 
    		double difOne = Math.abs(narrowLane.getLateralPosition() - wideLaneOne.getLateralPosition()); 
    		double difTwo = Math.abs(narrowLane.getLateralPosition() - wideLaneTwo.getLateralPosition()); 
    		// Connect the current lane to the previous lane (one or two) that has the closest lateral position 
            if (difOne < difTwo) {
            	//connect the lanes   
				if (increaseOfLanes < 0) { 
					ConnectLanes(wideLaneOne, narrowLane);
					//fixRoadMarkerPoint(prevCSE, this, wideLaneIndex, narrowLaneIndex);
				}
				else if (increaseOfLanes > 0) {
					ConnectLanes(narrowLane, wideLaneOne);
					//fixRoadMarkerPoint(prevCSE, this, narrowLaneIndex, wideLaneIndex);
				}
	    		//reduce the amount of lanes to be connected on both crossSections by 1
	    		wideLanesToConnect--;
	    		narrowLanesToConnect--;
	    		// loop to the next lane of the narrow CrossObject
	    		narrowLaneIndex++;
	    		// if we are at the last lane of the wide crossSection:
	    		if (wideLaneIndex == wide.size() - 1) {
	    			//only connect if there is a narrow "available"
	    			if (narrowLaneIndex < narrow.size()) {
	    				narrowLane = (Lane) narrow.get(narrowLaneIndex);
	    				if (increaseOfLanes < 0)  {
	    					ConnectLanes(wideLaneTwo, narrowLane);
				    		//fixRoadMarkerPoint(prevCSE, this, wideLaneIndex, narrowLaneIndex);
	    				}
	    				else if (increaseOfLanes > 0)  {
	    					ConnectLanes(narrowLane, wideLaneTwo);
	    					//fixRoadMarkerPoint(prevCSE, this, narrowLaneIndex, wideLaneIndex);
	    				}
	    			}
		    		else if (wide == curCSO)
		    			wideLaneTwo.clearUpLanes();
		    		else
		    			wideLaneTwo.clearDownLanes();
	    		}
	    	// the wide laneTwo is closest to the narrow lane
            } else if (difTwo <= difOne) {
	    		// the wide laneOne is not connected to the narrow crossSection
    			if (wide == curCSO)
    				wideLaneOne.clearUpLanes();
    			else
    				wideLaneOne.clearDownLanes();
	    		// the remaining lanes from the wide crossSection that can be connected decrease by 1
	    		wideLanesToConnect--;
	    		// TODO ??? This one should remove the road marker during an increase of lanes
	    		//cse.getRoadMarkersAlongList().get(i).getLinkPointList().remove(0);
	    		
	    		// if this is the last wideLane, it gets connected to the narrowLane
	    		// if not, we compare the next pair of Lanes from the wide crossSection
	    		if (wideLaneIndex == wide.size() - 1) {
    				if (increaseOfLanes < 0)  {
    					ConnectLanes(wideLaneTwo, narrowLane);
			    		//fixRoadMarkerPoint(prevCSE, this, wideLaneIndex, narrowLaneIndex);
    				}
    				if (increaseOfLanes > 0)  {
    					ConnectLanes(narrowLane, wideLaneTwo);
    					//fixRoadMarkerPoint(prevCSE, this, narrowLaneIndex, wideLaneIndex);
    					//    					fixRoadMarkerPoint(this, prevCSE, indexNarrowLane, indexWideLane);
    				}
	    		}
            }
    	}	
    }

    // Adjust geometry of vertices when number of lanes changes (narrow or widen)
    public static void fixRoadMarkerPoint(List<CrossSectionObject> prevRMAList, List<CrossSectionObject> thisRMAList, List<CrossSectionObject> prevLanes, List<CrossSectionObject> thisLanes) { 
    	ArrayList<Vertex> lpl = ((RoadMarkerAlong) prevRMAList.get(0)).getVertices();
    	if (null == lpl) {
    		System.err.println("Network.fixRoadMarkerPoint: rmaVertices is null");
    		return;
    	}
    	int sizePrevVertices = ((RoadMarkerAlong) prevRMAList.get(0)).getVertices().size();
    	// the inner and outerRMA of a lane are adjusted
    	int sizePrevLanes = prevLanes.size();
    	int sizeThisLane = thisLanes.size();
    	int iLanePrev = 0;
    	int jLaneThis = 0;


    	//if (sizePrevLanes != sizeThisLane)  {
    		while (iLanePrev < prevLanes.size() && jLaneThis < thisLanes.size())  {
	    		Lane prevLane = ((Lane) prevLanes.get(iLanePrev));
	    		if (prevLane.getDown()==null)  {
	    			iLanePrev++;
	    		}
	    		else if (prevLane.getDown().size() > 0)  {
		    		if (prevLane.getDown().get(0).equals(thisLanes.get(jLaneThis))) {
		    			for (int i = 0; i < 2; i++)  {
			    			lpl = ((RoadMarkerAlong) prevRMAList.get(iLanePrev + i)).getVertices();
			    			Vertex vertex = new Vertex(lpl.get(sizePrevVertices - 1));
			    			ArrayList<Vertex> vertices = ((RoadMarkerAlong) thisRMAList.get(jLaneThis + i)).getVertices();
			    			if (null == vertices)
			    				System.err.println("fixRoadMarkerPoint: vertices is null");
			    			else
			    				vertices.set(0, vertex);
			    			createLaneVertices(thisRMAList, thisLanes);
		    			}
		    			iLanePrev++;
		    			jLaneThis++;
		    		}
		    		else {
		    	    	if (sizePrevLanes < sizeThisLane)     			
		    	    		jLaneThis++;
		    	    	else if (sizePrevLanes > sizeThisLane)     			
		        	    	iLanePrev++;
		    	    	else if (sizePrevLanes == sizeThisLane)   {
		    	    		jLaneThis++;
		    	    		System.out.println("STRANGE");
		    	    	}
		    		}
	    		}
	    		else
	    			iLanePrev++;
    		}
    	//}
    }
    
    /**
     * Generate the design lines for each Lane.
     */
    public void fixLanePoints() {
    	//System.out.println(objects.toString());
    	int index = 0;
    	RoadMarkerAlong prevRMA = null;
    	for (CrossSectionObject cso : (getCrossSectionObjects(RoadMarkerAlong.class))) {
    		RoadMarkerAlong rma = (RoadMarkerAlong) cso;
			if (prevRMA != null) {
    			Lane lane = (Lane) (getCrossSectionObjects(Lane.class).get(index));
				ArrayList<Vertex> designLine = new ArrayList<Vertex>();
				ArrayList<Vertex> rmaPoints = rma.getVertices();
				if (null == rmaPoints)
					System.err.println("fixLanePoints: rmaPoints is null");
				else {
					if (rma.getVertices().size() != prevRMA.getVertices().size())
						System.err.println("rma and prevRMA have different sizes");
					System.out.println("prevRMA: " + Planar.verticesToString(prevRMA.getVertices()));
					System.out.println("    rma: " + Planar.verticesToString(rma.getVertices()));
					for (int i = 0; i < rma.getVertices().size(); i++)
						designLine.add(Vertex.weightedVertex(0.5,  rma.getVertices().get(i), prevRMA.getVertices().get(i)));
					//System.out.println(" center:" + Planar.verticesToString(designLine));
					lane.setDesignLine(designLine);
				}
    			index++;
	    	}
			prevRMA = rma;
    	}
    }


    /**
     * Re-generate the {@link Lane Lanes} of this CrossSectionElement
     */
	public void createLanes() {
		ArrayList<Lane> laneList = new ArrayList<Lane>();
    	RoadMarkerAlong rmaPrev = null;
    	Lane prevLane = null;
    	// FIXME This code assumes that adjacent drive-able CrossSectionElements can not occur
    	for (CrossSectionObject cso : getCrossSectionObjects(RoadMarkerAlong.class)) {
    		RoadMarkerAlong rma = (RoadMarkerAlong) cso;
    		if (null != rmaPrev) {
    			TurnArrow turn = new TurnArrow(null, null, 0, -10);
    			for (CrossSectionObject cso2 : getCrossSectionObjects(TurnArrow.class)) {
    				TurnArrow turnArrow = (TurnArrow) cso2;
    				if (turnArrow.getLateralPosition() > rmaPrev.getLateralPosition()
    						&& turnArrow.getLateralPosition() < rma.getLateralPosition()) {
    					turn = turnArrow;
    					break;
    				}
    			}
    			
    			StopLine stopLineLane = null;
    			for (CrossSectionObject cso2 : getCrossSectionObjects(StopLine.class)) {
    				StopLine stopLine = (StopLine) cso2;
    				double inner = rmaPrev.getLateralPosition();
    				double outer = rma.getLateralPosition();
    						
    				if (stopLine.getLateralPosition() <= inner + (outer - inner)/3
    						&& stopLine.getLateralPosition() + stopLine.getLateralWidth() >= inner + (outer - inner)*2/3) {
    					stopLineLane = new StopLine(stopLine.crossSectionElement, stopLine);
    					stopLineLane.lateralPosition= inner + 1;
    					stopLineLane.lateralWidth = outer - inner - 2;
    					break;
    				}
    			}

    			int myIndex = crossSection.getLink().getCrossSections_r().indexOf(crossSection);
    			int origin = Lane.NOORIGIN;
    			if (0 == myIndex)
    				origin = crossSection.getLink().getFromNode_r().getNodeID();
    			int destination = Lane.NODESTINATION;
    			if (crossSection.getLink().getCrossSections_r().size() - 1 == myIndex)
    				destination = crossSection.getLink().getToNode_r().getNodeID();
    			Lane lane = createLane(rmaPrev, rma, turn, stopLineLane, origin, destination);
    			
    			if (prevLane != null)
    				prevLane.connectLateralRight(lane);	            				
    			// add an initial centerline for every lane
				ArrayList<Vertex> lineVertices = new ArrayList<Vertex>();
				ArrayList<Vertex> rmaPoints = rma.getVertices();
				if (null == rmaPoints)
					System.err.println("fixLanePoints: rmaPoints is null");
				else if (rmaPoints.size() < 2)
					System.err.println("fixLanePoints: rmaPoints is too shorts");
				else {
					if (rma.getVertices().size() != rmaPrev.getVertices().size())
						System.err.println("rma and prevRMA have different sizes");
					for (int i = 0; i < rma.getVertices().size(); i++)
						lineVertices.add(Vertex.weightedVertex(0.5, rma.getVertices().get(i), rmaPrev.getVertices().get(i)));
					//System.out.println(" center:" + Planar.verticesToString(lineVertices));
					lane.setDesignLine(lineVertices);
					lineVertices = new ArrayList<Vertex>();
					// FIXME: calling weightedVertex with a weight of 0.0 or 1.0 is an expensive way to duplicate a Vertex ...
					for (int i = 0; i < rma.getVertices().size(); i++) {
						lineVertices.add(Vertex.weightedVertex(0.0, rma.getVertices().get(i), rmaPrev.getVertices().get(i)));
					}
					lane.setLaneVerticesInner(lineVertices);
					lineVertices = new ArrayList<Vertex>();
					for (int i = 0; i < rma.getVertices().size(); i++)  {
						lineVertices.add(Vertex.weightedVertex(1.0, rma.getVertices().get(i), rmaPrev.getVertices().get(i)));
					}
					lane.setLaneVerticesOuter(lineVertices);
				}
    			
    			laneList.add(0, lane);
    			prevLane = lane;
    		}
			rmaPrev = rma;
    	}
		deleteCrossSectionObjects(Lane.class);
		for (Lane lane : laneList)
			objects.add(lane);
	}
	
	private static void createLaneVertices(List<CrossSectionObject> RMAList, List<CrossSectionObject> laneList)  {
		RoadMarkerAlong rmaPrev = null;
		int j = 0;
		for (CrossSectionObject cso : RMAList) {
			RoadMarkerAlong RMA = (RoadMarkerAlong) cso;
			// add an initial centerline for every lane
			if (rmaPrev != null)  {
				ArrayList<Vertex> lineVertices = new ArrayList<Vertex>();
				ArrayList<Vertex> rmaPoints = RMA.getVertices();
				if (null == rmaPoints)
					System.err.println("fixLanePoints: rmaPoints is null");
				else {
					CrossSectionObject csoLane = laneList.get(j);
					Lane lane = (Lane) csoLane;
					if (RMA.getVertices().size() != rmaPrev.getVertices().size())
						System.err.println("rma and prevRMA have different sizes");

					for (int i = 0; i < RMA.getVertices().size(); i++) {
						lineVertices.add(Vertex.weightedVertex(0.5,  RMA.getVertices().get(i), rmaPrev.getVertices().get(i)));
					}
					lane.setDesignLine(lineVertices);
					lineVertices = new ArrayList<Vertex>();
					for (int i = 0; i < RMA.getVertices().size(); i++) {
						lineVertices.add(Vertex.weightedVertex(0.0,  RMA.getVertices().get(i), rmaPrev.getVertices().get(i)));
					}
					lane.setLaneVerticesInner(lineVertices);
					lineVertices = new ArrayList<Vertex>();
					for (int i = 0; i < RMA.getVertices().size(); i++) {
						lineVertices.add(Vertex.weightedVertex(1.0,  RMA.getVertices().get(i), rmaPrev.getVertices().get(i)));
					}
					lane.setLaneVerticesOuter(lineVertices);
					j++;
				}
			}
			rmaPrev = RMA;
		}		
	}
	
    private Lane createLane(RoadMarkerAlong leftRMA, RoadMarkerAlong rightRMA, TurnArrow turn, StopLine stopLine, 
    		int origin, int destination) {
		double laneWidth = rightRMA.getLateralPosition() - (leftRMA.getLateralPosition() + leftRMA.getMarkerWidth());
        double lateralStart = getLateralPosition(CrossSectionElement.LateralReferenceLeft) + leftRMA.getLateralPosition() + leftRMA.getMarkerWidth();
        if (turn.crossSectionElement == null)
        	turn.setCrossSectionElement(this);
		Lane lane = new Lane(this, turn, stopLine, lateralStart, laneWidth, origin, destination);
		//System.out.println("created lane at " + lateralStart + " + " + laneWidth + "; left is " + rightRMA.getType() + " right is " + leftRMA.getType() + ": " + lane.toString());
		if (leftRMA.getType().endsWith(RoadMarkerAlongTemplate.ALONG_STRIPED))
			lane.setGoLeft(true);
		else if (leftRMA.getType().endsWith(RoadMarkerAlongTemplate.ALONG_CONTINUOUS))
			lane.setGoLeft(false) ;
		if (rightRMA.getType().startsWith(RoadMarkerAlongTemplate.ALONG_STRIPED))
			lane.setGoRight(true);
		else if (rightRMA.getType().startsWith(RoadMarkerAlongTemplate.ALONG_CONTINUOUS))
			lane.setGoRight(false);
		else
			lane.setMaxSpeed(crossSection.getLink().getMaxSpeed_r());
		return lane;
    }

	public  GeneralPath createCSEPolygon()   {
    	ArrayList<Vertex> inner = getVerticesInner();
    	ArrayList<Vertex> outer = getVerticesOuter();
    	if ((null == inner) || (null == outer)) {
    		System.err.println("CSE.createCSEPolygon: CSEVertices is null");
    		return null;
    	}
		GeneralPath polygon = new GeneralPath(Path2D.WIND_EVEN_ODD);
		boolean firstPoint = true;
		for (Vertex v : inner)  {
			if (firstPoint)
				polygon.moveTo(v.getX(), v.getY());
			else
				polygon.lineTo(v.getX(), v.getY());
			firstPoint = false;
		}
		if (outer.size() < 2) {
			System.err.println("Oops: outer.size() is " + outer.size());
		}
		for (Vertex v : Reversed.reversed(outer))	// reverse the outer point list
			 polygon.lineTo(v.getX(), v.getY());
		if (! firstPoint)
		polygon.closePath();
		return polygon;
	}
	
    /**
     * Draw this CrossSectionElement on a {@link GraphicsPanel}
     * @param graphicsPanel {@link GraphicsPanel} to draw on
     * @param showFormPoints Draw the form points of this CrossSectionElement
     * @param showFormLines Draw lines connecting the form points of this CrossSectionElement
     */
    public void paint(GraphicsPanel graphicsPanel, boolean showFormPoints, boolean showFormLines) {
		GeneralPath polygon = this.createCSEPolygon();  	
    	graphicsPanel.setStroke(1F);
    	if (polygon != null) {
            Color lineColor = Color.darkGray;
            Color fillColor = Color.darkGray;
    		if (this.crossSection.getLink().getFromNode_r().hasConflictArea())
    			this.crossSection.getLink().getFromNode_r().createJunctionPolygon();
    		graphicsPanel.setColor(getCrossSectionElementTypology().getColor_r());	
    		lineColor = Color.BLUE;
    		fillColor = getCrossSectionElementTypology().getColor_r();
    		graphicsPanel.drawGeneralPath(polygon, lineColor, fillColor);
    		if (showFormPoints) {
	    		Color color = getCrossSectionElementTypology().getColor_Vertex_r();
	        	for (Vertex v : this.getVerticesInner())
	        		v.paint(graphicsPanel, color);
	        	for (Vertex v : this.getVerticesOuter())
	        		v.paint(graphicsPanel, color);
    		}
    		if (showFormLines) {
	    		Color color = getCrossSectionElementTypology().getColor_Vertex_r();
	    		graphicsPanel.setColor(color);
	    		graphicsPanel.setStroke((float) (0.01 * graphicsPanel.getZoom()));
	    		Point2D.Double prevPoint = null;
	        	for (Vertex v : this.getVerticesInner() ) {
        			Point2D.Double p = v.getPoint();
	        		if (null != prevPoint)
	        			graphicsPanel.drawLine(prevPoint, p);
        			prevPoint = p;
	        	}
	    		prevPoint = null;
	        	for (Vertex v : this.getVerticesOuter() ) {
	        		Point2D.Double p = v.getPoint();		        			
		        	if (null != prevPoint)
	        			graphicsPanel.drawLine(prevPoint, p);
	        		prevPoint = p;
	        	}
    			
    		}
    	}
    	for (CrossSectionObject cso : getCrossSectionObjects(Lane.class))
    		cso.paint(graphicsPanel);
		for (CrossSectionObject cso : getCrossSectionObjects(RoadMarkerAlong.class))
			cso.paint(graphicsPanel);
		// Paint everything else
		for (CrossSectionObject cso : objects)
			if ((! (cso instanceof RoadMarkerAlong)) && (! (cso instanceof Lane)))
				cso.paint(graphicsPanel);
    }
    private boolean writeCrossSectionObjectsXML(StaXWriter staXWriter) {
    	for (CrossSectionObject cso : getCrossSectionObjects(CrossSectionObject.class))
    		if (! cso.writeXML(staXWriter))
    			return false;
    	return true;
    }
    
    /**
     * Write this CrossSectionElement to an XML file.
     * @param staXWriter {@link StaXWriter}; writer for the XML file
     * @return Boolean; true on success; false on failure
     */
	@Override
	public boolean writeXML(StaXWriter staXWriter) {
		return staXWriter.writeNodeStart(XMLTAG)
				&& staXWriter.writeNode(XML_NAME, getType_r())
				&& staXWriter.writeNode(XML_WIDTH, Double.toString(getCrossSectionElementWidth()))
				&& ((! crossSectionElementTypology.getDrivable()) || staXWriter.writeNode(XML_SPEEDLIMIT, getSpeedLimit_r()))
				&& writeCrossSectionObjectsXML(staXWriter)
				&& staXWriter.writeNodeEnd(XMLTAG);
	}

	/**
	 * Re-generate the cashed ArrayLists of inner and outer vertices of this CrossSectionElement.
	 * <br /> This method must be called if the shape of this CrossSectionElement may have changed.
	 */
	public void regenerateVertices() {
		setVertices(createAndCleanLinkPointListInner(false, true, false), createAndCleanLinkPointListOuter(false, true, false));         		
	}

	/**
	 * Set the cached inner and outer vertices of this CrossSectionElement to the specified shapes. 
	 * @param inner ArrayList&lt;{@link Vertex}&gt; the list of vertices that 
	 * describes the inner boundary of this CrossSectionElement
	 * @param outer ArrayList&lt;{@link Vertex}&gt; the list of vertices that 
	 * describes the outer boundary of this CrossSectionElement
	 */
	public void setVertices(ArrayList<Vertex> inner, ArrayList<Vertex> outer) {
		verticesInner = inner;
		verticesOuter = outer;
		if (verticesInner.size() != verticesOuter.size())
			System.out.println("link 1 " );
		if ((inner.size() < 2) || (outer.size() < 2))
			System.err.println("Oops verticesInner.size() is " + inner.size() + " ...outer.size() is " + outer.size());
	}

}
