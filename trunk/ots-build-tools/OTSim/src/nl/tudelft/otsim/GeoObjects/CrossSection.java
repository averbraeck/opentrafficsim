package nl.tudelft.otsim.GeoObjects;

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import nl.tudelft.otsim.FileIO.ParsedNode;
import nl.tudelft.otsim.FileIO.StaXWriter;
import nl.tudelft.otsim.FileIO.XML_IO;
import nl.tudelft.otsim.GUI.InputValidator;
import nl.tudelft.otsim.GeoObjects.Node.DirectionalLink;
import nl.tudelft.otsim.SpatialTools.Planar;
import nl.tudelft.otsim.Utilities.Reversed;

/**
 * A CrossSection is a lateral description of the composition of a {@link Link}.
 * Each CrossSection consists of one or more {@link CrossSectionElement}s.
 * <br />
 * The first CrossSectionElement of a link is positioned at the lateralOffset
 * of this CrossSection. Subsequent CrossSectionElements are placed next to the
 * preceding one.
 * 
 * @author Guus F Tamminga, Peter Knoppers
 *
 */
public class CrossSection implements XML_IO {
	/** Label in XML representation of a CrossSection */
	public static final String XMLTAG = "crossSection";
	
	/** Label of ID in XML representation of a CrossSection */
	private static final String XML_ID = "ID";
	/** Label of longitudinal position in XML representation of a CrossSection */
	private static final String XML_LONGITUDALPOSITION= "crossSectionPositionLongitudinal";
	/** Label of lateral offset in XML representation of a CrossSection */
	private static final String XML_LATERALOFFSET= "elementOffsetLateral";
	/** Label of lateral offset in XML representation at end of a CrossSection */
	private static final String XML_ENDLATERALOFFSET= "elementEndOffsetLateral";

    private Link link;
    private double longitudinalPosition;
    private double lateralOffset;
    private double endLateralOffset = 0;
    private ArrayList<CrossSectionElement> crossSectionElementList;

    /**
     * Create a new CrossSection owned by the specified link and containing the 
     * specified CrossSectionElements.
     * @param longitudalPosition Position in m along the length of the 
     * {@link Link}
     * @param lateralOffset Lateral offset of the first 
     * {@link CrossSectionElement} of this CrossSection in m with respect to the design line of the {@link Link} 
     * @param sectionElementList ArrayList<{@link CrossSectionElement} specifying the CrossSectionElements of the new CrossSection
     */
	public CrossSection(double longitudalPosition, double lateralOffset, ArrayList<CrossSectionElement> sectionElementList) {
		if (sectionElementList != null)
	      setCrossSectionElementList_w(sectionElementList);
		this.longitudinalPosition = longitudalPosition; 
		this.lateralOffset = lateralOffset;
	}
	
	public void setEndLateralOffset_w(double newOffset) throws Exception {
		ArrayList<CrossSection> parentList = link.getCrossSections_r();
		int myIndex = parentList.indexOf(this);
		if (myIndex != parentList.size() - 1)
			throw new Exception("Cannot set lateral offset of non-last CrossSection");
		endLateralOffset = newOffset;
	}
	
	public double getEndLateralOffset_r() {
		ArrayList<CrossSection> parentList = link.getCrossSections_r();
		int myIndex = parentList.indexOf(this);
		if (myIndex == parentList.size() - 1)
			return endLateralOffset;
		return (parentList.get(myIndex + 1).getLateralOffset_r());
	}
	
	/**
	 * Create a String describing the CrossSection.
	 */
	@Override
	public String toString() {
		return String.format("%d, %.2fm, %.2fm", getCrossSectionID(), longitudinalPosition, lateralOffset);
		//return String.format("%d %fm %fm %s", crossSectionID, longitudalPosition, lateralOffset, crossSectionElementList.toString());
	}
	
	/**
	 * Duplicate a CrossSection (the duplicate inherits the network, lateral
	 * and longitudinal positions from the given CrossSection and gets copies 
	 * of all CrossSectionElements).
	 * @param crossSection original CrossSection
	 */
	public CrossSection(CrossSection crossSection) {
        //this.linkID = crossSection.linkID;
		// Duplicate the CrossSectionElementList
		crossSectionElementList = new ArrayList<CrossSectionElement>();
		for (CrossSectionElement cse : crossSection.getCrossSectionElementList_r())
			crossSectionElementList.add(new CrossSectionElement(cse, this));
	    this.longitudinalPosition = crossSection.longitudinalPosition; 
	    this.lateralOffset = crossSection.lateralOffset;
	}

	 /*
	 public boolean compatibleWith(CrossSection other) {
		if (crossSectionElementList.size() != other.crossSectionElementList.size())
			return false;
		int i = 0;
		for (CrossSectionElement cse : crossSectionElementList)
			if (! cse.compatibleWith(other.crossSectionElementList.get(i)))
				return false;
		return true;
	}*/
	
	/**
	 * Create a CrossSection from a parsed XML file
	 * @param link {@link Link}; the Link that will own the new CrossSection
	 * @param pn {@link ParsedNode}; the root of the CrossSection in the parsed XML file
	 * @throws Exception
	 */
	public CrossSection(Link link, ParsedNode pn) throws Exception {
		this.link = link;
		crossSectionElementList = new ArrayList<CrossSectionElement>();
		longitudinalPosition = lateralOffset = Double.NaN;
		
		for (String fieldName : pn.getKeys()) {
			String value = pn.getSubNode(fieldName, 0).getValue();
			if (fieldName.equals(XML_ID))
				;	// ignore
			else if (fieldName.equals(XML_LONGITUDALPOSITION))
				longitudinalPosition = Double.parseDouble(value);
			else if (fieldName.equals(XML_LATERALOFFSET))
				lateralOffset = Double.parseDouble(value);
			else if (fieldName.equals(CrossSectionElement.XMLTAG))
				for (int index = 0; index < pn.size(fieldName); index++)
					crossSectionElementList.add(new CrossSectionElement(this, pn.getSubNode(fieldName, index)));
			else
				throw new Exception("Unknown field in CrossSection: " + fieldName);
		}
		if (Double.isNaN(longitudinalPosition))
			throw new Exception("CrossSection has not longitudinalPosition " + pn.lineNumber + ", " + pn.columnNumber);
		if (Double.isNaN(lateralOffset))
			throw new Exception("CrossSection has not lateralOffset " + pn.lineNumber + ", " + pn.columnNumber);
	}

	/**
	 * Return a CrossSectionElement as seen from the beginning or ending of the
	 * CrossSection.
	 * @param atEnd Boolean; true for end, false for begin
	 * @param countFromLeft Boolean; true to count from the left; false to 
	 * count from the right
	 * @param rank Integer; rank number of the CrossSectionElement to return
	 * @return CrossSectionElement
	 */
	public CrossSectionElement crossSectionElementFromNode(boolean atEnd, boolean countFromLeft, int rank) {
		int elementIndex = rank / 2;
		if (atEnd == countFromLeft)
			elementIndex = crossSectionElementList.size() - 1 - elementIndex;
		if (elementIndex < 0)   {
			System.out.print("index negative");
		}
		//System.out.println(String.format("atEnd=%s, fromLeft=%s, rank=%d -> elementIndex=%d", atEnd ? "true" : "false", countFromLeft ? "true" : "false", rank, elementIndex));
		return crossSectionElementList.get(elementIndex);
	}

	/**
	 * Return the polygon describing the surface of a CrossSectionElement
	 * @param atEnd Boolean; true for counting CrossSectionElements from the end
	 * of the CrossSection; false for counting CrossSectionElements from the
	 * begin of the CrossSection
	 * @param countFromLeft Boolean; true to count from the left; false to count
	 * from the right
	 * @param rank Integer; rank number of the CrossSectionElement to select
	 * @param adjust Boolean; true to apply corrections needed to connect this
	 * CrossSectionElement to the next (or to the next Node); false to return
	 * the "unfixed" polygon.
	 * @return ArrayList&lt;Vertex&gt; polygon of the CrossSectionElement
	 */
	private ArrayList<Vertex> verticesFromNode(boolean atEnd, boolean countFromLeft, int rank, boolean adjust) {
		boolean inner = ((rank % 2 == 0) == countFromLeft) != atEnd;
		//System.out.println(String.format("side %s", inner ? "inner" : "outer"));
		CrossSectionElement cse = crossSectionElementFromNode(atEnd, countFromLeft, rank);
		return inner ? cse.createAndCleanLinkPointListInner(true, true, false) : cse.createAndCleanLinkPointListOuter(true, true, false);
	}

	/**
	 * Return the first two vertices a CrossSectionElement at one end of a
	 * CrossSection.
	 * @param atEnd Boolean; true for counting CrossSectionELements from the end 
	 * of the CrossSection; false for counting CrossSectionElements from the
	 * begin of the CrossSection
	 * @param countFromLeft Boolean; true to count from the left; false to count
	 * from the right
	 * @param rank Integer; rank number of the crossSectionElement to use
	 * @param adjust Boolean; true to apply corrections needed to connect this
	 * CrossSectionElement to the next (or to the next Node); false to return
	 * the first two vertices of the "unfixed" polygon.
	 * @return Line2D.Double; the first two vertices of the polygon of the
	 * CrossSectionElement
	 */
	public Line2D.Double vectorAtNode(boolean atEnd, boolean countFromLeft, int rank, boolean adjust) {
		ArrayList<Vertex> vertices = verticesFromNode(atEnd, countFromLeft, rank, adjust);
		if (vertices.size() < 2)
			return null;
		int firstIndex = atEnd ? vertices.size() - 1 : 0;
		int secondIndex = firstIndex == 0 ? 1 : firstIndex - 1;
		return new Line2D.Double(vertices.get(firstIndex).getPoint(), vertices.get(secondIndex).getPoint());
	}

	/**
	 * Return the first Vertex of the polygon of a CrossSectionElement
	 * @param atEnd Boolean; true for counting CrossSectionELements from the end 
	 * of the CrossSection; false for counting CrossSectionElements from the
	 * begin of the CrossSection
	 * @param countFromLeft Boolean; true to count from the left; false to count
	 * from the right
	 * @param rank Integer; rank number of the crossSectionElement to use
	 * @param index Integer; Index of the Vertex to return
	 * @param adjust Boolean; true to apply corrections needed to connect this
	 * CrossSectionElement to the next (or to the next Node); false to return
	 * the first Vertex of the "unfixed" polygon.
	 * @return Vertex; the first vertex of the polygon of the 
	 * CrossSectionElement
	 */
	public Vertex vertexFromNode(boolean atEnd, boolean countFromLeft, int rank, int index, boolean adjust) {
		ArrayList<Vertex> vertices = verticesFromNode(atEnd, countFromLeft, rank, adjust);
		if (0 == vertices.size()) {
			System.err.println("vertexFromNode: verticesFromNode returned empty list");
			return null;
		}
		if (atEnd)
			index = vertices.size() - 1 - index;
		if ((index < 0) || (index >= vertices.size())) {
			System.err.println("vertexFromNode: index out of range");
			return null;
		}
		return (vertices.get(index));
	}
	
	/**
	 * Return a CrossSectionElement as seen for a particular end of the
	 * CrossSection.
	 * @param atEnd Boolean; true for counting CrossSectionElements from the end
	 * of the CrossSection; false for counting CrossSectionElements from the
	 * beginning of the CrossSection
	 * @param countFromLeft Boolean; true to count from the left; false to count
	 * from the right
	 * @param rank Integer; rank number of the crossSectionElement to use
	 * @return CrossSectionElement
	 */
	public CrossSectionElement elementFromNode(boolean atEnd, boolean countFromLeft, int rank) {
		return crossSectionElementFromNode(atEnd, countFromLeft, rank);
	}
	
	/*
	public void setEndPoint(boolean atEnd, boolean countFromLeft, int rank, Vertex newEndPoint) {
		ArrayList<Vertex> vertices = verticesFromNode(atEnd, countFromLeft, rank, true);
		int index = atEnd ? vertices.size() - 1 : 0;
		vertices.get(index).setPoint(newEndPoint);
	}
	*/
		
	/**
	 * Return the design line of the CrossSection
	 * @return ArrayList&lt;Vertex&gt; List of vertices defining the <i>design 
	 * line</i> of this CrossSection 
	 */
	public ArrayList<Vertex> getVertices_r() {
		//System.out.println("Entering getVertices_r for link " + link.getName_r());
		ArrayList<Vertex> vertices = new ArrayList<Vertex>();
		Vertex prevVertex = null;
		double distanceAlongLink = 0;
		double crossSectionEnd = Double.MAX_VALUE;
		ArrayList<CrossSection> parentList = link.getCrossSections_r(); 
		int nextCrossSectionIndex = parentList.indexOf(this) + 1;
		if (nextCrossSectionIndex < parentList.size())
			crossSectionEnd = parentList.get(nextCrossSectionIndex).longitudinalPosition;
		if (crossSectionEnd == longitudinalPosition)
			System.err.println("Zero length crossSection");
		// System.out.format("CrossSection runs from %.3f to %.3f\r\n", longitudinalPosition, crossSectionEnd);
		ArrayList<Vertex> linkVertices = link.getVertices();
		double distance = linkVertices.get(0).getPoint().distance(linkVertices.get(linkVertices.size() - 1).getPoint());
		if (distance < 0.0001)
			System.err.println("Oops: linkVertices " + linkVertices.toString() + " cover no only " + distance + "m");
		for (Vertex v : linkVertices) {
			Point2D.Double p = v.getPoint();
			if (null != prevVertex) {
				double length = prevVertex.getPoint().distance(p);
				double useLength = (length == 0) ? 0.001 : length;
				//System.out.println("Examining link points "+ prevVertex.toString() + " and " + v.toString() + " length=" + length);
				if ((longitudinalPosition >= distanceAlongLink) && (longitudinalPosition <= distanceAlongLink + length) && (0 == vertices.size()))	// add start vertex
					vertices.add(Vertex.weightedVertex((longitudinalPosition - distanceAlongLink) / useLength, prevVertex, v));
				if ((longitudinalPosition < distanceAlongLink + length) && (crossSectionEnd > distanceAlongLink + length))	// add intermediate vertex
					vertices.add((new Vertex(v)));
				if (crossSectionEnd <= distanceAlongLink + length)	// add end vertex
					vertices.add(Vertex.weightedVertex((crossSectionEnd - distanceAlongLink) / useLength, prevVertex, v));
				distanceAlongLink += length;
				if (distanceAlongLink > crossSectionEnd)
					break;
			}
			prevVertex = v;
		}
		if (vertices.size() == 1) {	// rounding error?
			System.err.println("Hmmm; vertices has one entry");
			vertices.add(linkVertices.get(linkVertices.size() - 1));
		}
		if (vertices.get(0).distance(vertices.get(vertices.size() - 1)) < 0.0001)
			System.err.println("Vertices cover very short distance " + vertices.toString() + " linkVertices is " + vertices.toString());
		double lateralOffsetChange = getEndLateralOffset_r() - this.lateralOffset;
		ArrayList<Vertex> endList = Planar.createParallelVertices(vertices, lateralOffsetChange);
		ArrayList<Vertex> weightedList = new ArrayList<Vertex>(vertices.size());
		for (int i = 0; i < vertices.size(); i++)
			weightedList.add(Vertex.weightedVertex(1.0 * i / vertices.size(), vertices.get(i), endList.get(i)));
		vertices = weightedList;
		return vertices;
	}

	/**
	 * Return the index of this CrossSection in the list of CrossSections of
	 * the link that owns it.
	 * @return Integer; index in CrossSection list of link
	 */
	public int getCrossSectionID() {
		//System.out.println("getCrossSectionID: lpos is " + longitudinalPosition);
		if (null == link)
			throw new Error("oops: this.link is null");
		return link.getCrossSections_r().indexOf(this);
	}

	/**
	 * Set or change the link that owns this CrossSection.
	 * <br />
	 * This method does <b>not</b> alter the list of CrossSections of either
	 * the old or new parent. Consistency must be ensured by the caller of this
	 * method.
	 * @param link New Link for this CrossSection
	 */
	public void setLink(Link link) {
		this.link = link;
	}
	
	/**
	 * Return the link that owns this CrossSection
	 * @return Link that owns this CrossSection
	 */
	public Link getLink() {
		return link;
	}

	/**
	 * Return the longitudinal position of this CrossSection 
	 * @return Double; the longitudinal position of this CrossSection along
	 * the link that it belongs to
	 */
	public double getLongitudinalPosition_r() {
		return longitudinalPosition;
	}
	
	/**
	 * Determine the longitudinal length of this CrossSection.
	 * truncated at autogenerated {@link Node Nodes} that it passes
	 * @return Double; the longitudinal length of this CrossSection
	 */
	public double getLongitudinalLength() {
		ArrayList<CrossSection> crossSections = link.getCrossSections_r();
		int index = crossSections.indexOf(this);
		return ((index < crossSections.size() - 1) ? crossSections.get(index + 1).longitudinalPosition : link.getLength()) - longitudinalPosition;
	}

	/**
	 * Change the longitudinal position of this CrossSection.
	 * <br />
	 * This method does not enforce that all CrossSections along a link have
	 * distinct and increasing longitudinal positions with the first starting
	 * at 0. This is left as a responsibility to the caller.
	 * @param crossSectionlongitudinalPosition Double; new longitudinal position 
	 * of this CrossSection
	 */
	public void setLongitudalPosition_w(double crossSectionlongitudinalPosition) {
		this.longitudinalPosition = crossSectionlongitudinalPosition;
	}
	
	/**
	 * Create an InputValidator for the longitudinal position of this
	 * CrossSection.
	 * @return A new InputValidator that ensures that a proposed value lies
	 * between the longitudinal positions of the precedessor and the successor
	 * of this CrossSection.
	 */
	public InputValidator validateLongitudalPosition_v() {
		double upperLimit = 0;
		if (this != link.getCrossSections_r().get(0))
			upperLimit = link.getLength();
		return new InputValidator("[.,0-9].*", 0, upperLimit);
	}

	/**
	 * Return the list of CrossSectionElements of this CrossSection.
	 * <br />
	 * The returned list should be treated read-only!
	 * @return ArrayList&lt;CrossSectionElement&gt; List of CrossSectionElements
	 */
	public ArrayList<CrossSectionElement> getCrossSectionElementList_r() {
		return crossSectionElementList;
	}

	/**
	 * Replace the list of CrossSectionElements of this CrossSection.
	 * <br />
	 * All elements of the new list should have this CrossSection as parent.
	 * @param crossSectionElementList ArrayList&lt;CrossSectionElement&gt;
	 * List of CrossSectionElements that must replace the current one.
	 */
	public void setCrossSectionElementList_w(ArrayList<CrossSectionElement> crossSectionElementList) {
		for (CrossSectionElement cse : crossSectionElementList)
			if (this != cse.getCrossSection())
				throw new Error("attempt to assign CrossSectionElement that does not belong to me");
		this.crossSectionElementList = crossSectionElementList;
	}
	
	/**
	 * Return the lateral offset of this CrossSection. Lateral offset is the
	 * offset of the first CrossSectionElement of this CrossSection from the
	 * <i>design line</>.
	 * @return Double; offset from the <i>design line</i> in m
	 */
    public double getLateralOffset_r() {
		return lateralOffset;
	}

    /**
     * Modify the lateral offset of this CrossSection. Lateral offset is the
	 * offset of the first CrossSectionElement of this CrossSection from the
	 * <i>design line</>.
     * @param elementOffsetLateral Double; new value of the lateral offset
     */
	public void setLateralOffset_w(double elementOffsetLateral) {
		this.lateralOffset = elementOffsetLateral;
	}
	
	/**
	 * Check that a CrossSection may be deleted from the parent Link. As each
	 * Link must have at least one CrossSection, deletion of the last one is
	 * prohibited.
	 * @return Boolean; true if the link has more than one CrossSection, false
	 * otherwise
	 */
	public boolean mayDeleteCrossSection_d() {
		return link.getCrossSections_r().size() > 1;
	}
	
	/**
	 * Delete a CrossSection from the parent Link.
	 * <br />
	 * This method ensures that the last CrossSection of a link can not be
	 * deleted.
	 * <br />
	 * If the first CrossSection of a link is deleted, the longitudinal
	 * position of the next (now first) is set to 0 to ensure that the first
	 * CrossSection of a link has longitudinal position 0. 
	 */
	public void deleteCrossSection_d() {
		if (! mayDeleteCrossSection_d())
			throw new Error ("Cannot delete CrossSection");
		ArrayList<CrossSection> csList = link.getCrossSections_r();
		int index = csList.indexOf(this);
		if (0 == index) {
			csList.remove(0);
			csList.get(0).setLongitudalPosition_w(0);
		} else
			csList.remove(index);
		link.network.setModified();
	}
	
	/**
	 * Create a new CrossSection and insert it in the list of the parent Link.
	 * @param longitudinalDistance Double; longitudinal position of the new
	 * CrossSection. If null; this CrossSection will be split at the mid
	 * point.
	 * <br />
	 * The new CrossSection will have a list of CrossSectionElements that is
	 * identical to those of this CrossSection. It will be added at the
	 * correct position in the list of CrossSections of the parent Link.
	 */
	public void duplicateCrossSection_2(Double longitudinalDistance) {
		ArrayList<CrossSection> csList = link.getCrossSections_r();
		int index = csList.indexOf(this);
		CrossSection newCS = new CrossSection(csList.get(index));
		if (null == longitudinalDistance) {
			double nextLongitudinalDistance = link.getLength();
			if (csList.size() > index + 1)
				nextLongitudinalDistance = csList.get(index + 1).longitudinalPosition;
			longitudinalDistance = (this.longitudinalPosition + nextLongitudinalDistance) / 2;
		}
		newCS.setLongitudalPosition_w(longitudinalDistance);
		newCS.setLink(link);
		csList.add(index + 1, newCS);
		link.network.setModified();
	}

	/* Retrieve {@link CrossSection CrossSections} from a node with only two links;
	 * @param node Node; 
	 * */
	public static ArrayList<CrossSection> getCrossSectionsAtNode(Node node) {
		int inCount = node.incomingCount();
		int outCount = node.leavingCount();
		// the simple case: two links 
		ArrayList<CrossSection> csList = null;
		if ((inCount == 1) && (outCount == 1))	{
			// set the neighbor index of the end cross section of the entering
			// and the starting cross section of the leaving link
	    	Link fromLink = node.getLinksFromJunction(true).get(0).link;
	    	Link toLink = node.getLinksFromJunction(false).get(0).link;
	    	if (! fromLink.getFromNode_r().equals(toLink.getToNode_r())) {
		    	csList = new ArrayList<CrossSection>();
				CrossSection inCS = fromLink.getCrossSections_r().get(fromLink.getCrossSections_r().size() - 1);
				CrossSection outCS = toLink.getCrossSections_r().get(0);
				csList.add(inCS);
				csList.add(outCS);	
	    	}
		}
		return csList;
	}
	
	/**
	 * Link the {@link CrossSectionElement CrossSectionElements} of this
	 * CrossSection to compatible CrossSectionElements of another CrossSection.
	 * CrossSectionElements with higher couplingPriority are connected first.
	 * @param otherCS CrossSection; the other CrossSection to link this one to
	 */
    public void linkToCrossSection(CrossSection otherCS) {
    	//System.out.println("Entering linkCrossSections from " + fromCS.toString() + " to " + toCS.toString());
    	// Clear the NeighborIndices
    	for (CrossSectionElement cse : getCrossSectionElementList_r())
    		cse.setNeighborIndex(-1);
    	for (CrossSectionElement cse : otherCS.getCrossSectionElementList_r()) {
    		cse.setNeighborIndex(-1);
    		cse.setConnectedFrom(null);
    	}
    	// (Re-)link
    	matchCrossSections(0, getCrossSectionElementList_r().size(), otherCS, 0, otherCS.getCrossSectionElementList_r().size());
    }
    
    private void matchCrossSections(int fromLow, int fromHigh, CrossSection otherCS, int toLow, int toHigh)
	{
    	//System.out.println(String.format("matching %d-%d to %d-%d", fromLow, fromHigh, toLow, toHigh));
    	// find the highest priority crossSectionElement in fromCS and in toCS
    	// if there are several select the lowest numbered one
    	int fromPriority = Integer.MAX_VALUE;
    	int fromPivot = -1;
    	for (int i = fromLow; i < fromHigh; i++) {
    		CrossSectionElement fromElement = getCrossSectionElementList_r().get(i);
    		int thisCouplingPriority = fromElement.getCrossSectionElementTypology().getCouplingPriority();
    		if (thisCouplingPriority < fromPriority) {
    			fromPriority = thisCouplingPriority;
    			fromPivot = i;
    		}
    	}
    	int toPriority = Integer.MAX_VALUE;
    	int toPivot = -1;
    	for (int i = toLow; i < toHigh; i++) {
    		CrossSectionElement toElement = otherCS.getCrossSectionElementList_r().get(i);
    		int thisCouplingPriority = toElement.getCrossSectionElementTypology().getCouplingPriority();
    		if (thisCouplingPriority < toPriority) {
    			toPriority = thisCouplingPriority;
    			toPivot = i;
    		}
    	}
    	//System.out.println(String.format("fromPivot=%d, toPivot=%d", fromPivot, toPivot));
    	if (fromPriority == toPriority) {
    		if (fromPriority == Integer.MAX_VALUE)
    			return;		// empty lists, or lists contain only non-matchable CrossSection elements
    		// link up the CrossSectionElements
    		otherCS.getCrossSectionElementList_r().get(toPivot).setNeighborIndex(fromPivot);
    		otherCS.getCrossSectionElementList_r().get(toPivot).setConnectedFrom(getCrossSectionElementList_r().get(fromPivot));
    		getCrossSectionElementList_r().get(fromPivot).setNeighborIndex(toPivot);
    		// fix the CrossSectionElements left of this one
    		matchCrossSections(fromLow, fromPivot, otherCS, toLow, toPivot);
    		// fix the CrossSectionElements right of this one
    		matchCrossSections(fromPivot + 1, fromHigh, otherCS, toPivot + 1, toHigh);
    	} else if (fromPriority > toPriority) {
    		// the CrossSectionElement in toCS starts here.
    		// Temporarily assign this element lowest priority and connect everything else
    		otherCS.getCrossSectionElementList_r().get(toPivot).getCrossSectionElementTypology().setCouplingPriority(Integer.MAX_VALUE);
    		try {
    			matchCrossSections(fromLow, fromHigh, otherCS, toLow, toHigh);
    		} finally {
    			otherCS.getCrossSectionElementList_r().get(toPivot).getCrossSectionElementTypology().setCouplingPriority(toPriority);
    		}
    	} else {
    		// the CrossSectionElement in fromCS ends here.
    		// Temporarily assign this element lowest priority and connect everything else
    		getCrossSectionElementList_r().get(fromPivot).getCrossSectionElementTypology().setCouplingPriority(Integer.MAX_VALUE);
        	try {
    			matchCrossSections(fromLow, fromHigh, otherCS, toLow, toHigh);
    		} finally {
    			getCrossSectionElementList_r().get(fromPivot).getCrossSectionElementTypology().setCouplingPriority(fromPriority);
    		}
    	}
	}	
    
    /**
     * Build and return a list of all Lanes in this CrossSection.
     * @return ArrayList&lt;{@link Lane}&gt;; the Lanes in this CrossSection
     */
    public ArrayList<Lane> collectLanes() {
    	ArrayList<Lane> lanes = new ArrayList<Lane>();
	 	for (CrossSectionElement cse : Reversed.reversed(getCrossSectionElementList_r()))
	    	if (cse.getCrossSectionElementTypology().getDrivable())
	    		for (CrossSectionObject cso : cse.getCrossSectionObjects(Lane.class))
	        		lanes.add((Lane) cso);
	 	return lanes;
    }
    
    /**
     * Build and return a list of all Lanes in this CrossSection in reversed order.
     * @return ArrayList&lt;{@link Lane}&gt;; the Lanes in this CrossSection
     */
    public ArrayList<Lane> collectLanesReversed() {
    	ArrayList<Lane> lanes = new ArrayList<Lane>();
	 	for (CrossSectionElement cse : Reversed.reversed(getCrossSectionElementList_r()))
	    	if (cse.getCrossSectionElementTypology().getDrivable())
	    		for (CrossSectionObject cso : Reversed.reversed(cse.getCrossSectionObjects(Lane.class)))
	        		lanes.add((Lane) cso);
	 	return lanes;
    }
    
	private boolean writeCrossSectionElementsXML(StaXWriter staXWriter) {
		for (CrossSectionElement cse : getCrossSectionElementList_r())
			if (! cse.writeXML(staXWriter))
				return false;
		return true;
	}    

	/**
	 * Write this CrossSection to an XML file.
	 * @param staXWriter {@link StaXWriter}; writer for the XML file
	 * @return Boolean; true on success; false on failure
	 */
	@Override
	public boolean writeXML(StaXWriter staXWriter) {
		return staXWriter.writeNodeStart(XMLTAG)
				&& staXWriter.writeNode(XML_ID, Integer.toString(getCrossSectionID()))
				&& staXWriter.writeNode(XML_LONGITUDALPOSITION, Double.toString(getLongitudinalPosition_r()))
				&& staXWriter.writeNode(XML_LATERALOFFSET, Double.toString(getLateralOffset_r()))
				&& writeCrossSectionElementsXML(staXWriter)
				&& staXWriter.writeNodeEnd(XMLTAG);
	}

}
