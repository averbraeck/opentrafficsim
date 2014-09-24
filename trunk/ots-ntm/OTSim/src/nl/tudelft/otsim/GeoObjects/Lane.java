package nl.tudelft.otsim.GeoObjects;

import java.awt.Color;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import nl.tudelft.otsim.FileIO.StaXWriter;
import nl.tudelft.otsim.GUI.GraphicsPanel;
import nl.tudelft.otsim.SpatialTools.Planar;
import nl.tudelft.otsim.Utilities.Reversed;

// TODO: Guus: Remove unused methods or document them.
/**
 * Class that implements traffic lanes. Used to export network topology to
 * lane simulators.
 * 
 * @author Guus F Tamminga
 */
public class Lane extends CrossSectionObject {
    private final int id;
    //private int capacity;
	private Lane right;
	private Lane left;
	private ArrayList<Lane> upLanes;
    private ArrayList<Lane> downLanes;
    private ArrayList<Lane> crossingYieldToLaneList;
    private ArrayList<Lane> mergingYieldToLaneList;
    //private ArrayList<Lane> splittingLaneList;
    private int origin; 
    private int destination; 
    private boolean goRight;
	private boolean goLeft;
	private Point2D.Double ctrlPointCenter;
	private ArrayList<Vertex> laneVerticesCenter;
	private ArrayList<Vertex> laneVerticesInner;
	private ArrayList<Vertex> laneVerticesOuter;
	private static int laneCount;
	private double maxSpeed;
	private TurnArrow turnArrow;
	private StopLine stopLine;
	/** Indicate that the end of this Lane is <b>not</b> a node */
	final static int NODESTINATION = -1;
	/** Indicate that a the beginning of this Lane is <b>not</b> a node */
	public final static int NOORIGIN = -2;

    /**
     * Create a new Lane.
     * @param cse CrossSectionElement that owns this Lane
     * @param turnArrow TurnArrow for this Lane (may be null)
     * @param stopLine {@link StopLine}; the stopLine of the new Lane
     * @param lateralPosition Double; lateral position of left edge of this Lane
     * in m (measured from the <i>design line</i> of the Link)
     * @param width Double; width of this Lane in m
     * @param origin Integer; node ID of the start of this lane or NOORIGIN
     * @param destination Integer; node ID of the end of this lane or NODESTINATION
     */
    public Lane (CrossSectionElement cse, TurnArrow turnArrow, StopLine stopLine, double lateralPosition, double width, int origin, int destination) {
    	this();
    	this.lateralWidth = width;
    	this.lateralPosition = lateralPosition;
    	this.origin = origin;
    	this.destination = destination;
    	this.turnArrow = turnArrow;
    	this.stopLine = stopLine;
    	// TODO should be illegal for cse to be null
    	this.crossSectionElement = cse;
    }
    
    /**
     * Create a Lane with default values.
     * TODO: make this creator unneeded; it should not exist. 
     * @param lane Example Lane to copy some properties from
     */
    /*
    public Lane(Lane lane) {
    	this();
    	this.lateralWidth = lane.getLateralWidth();
    	this.lateralPosition = lane.getLateralPosition();
    	this.origin = lane.getOrigin();
    	this.destination = lane.getDestination();
    	this.turnArrow = lane.getTurnArrow();
    	this.stopLine = lane.getStopLine();
    	this.laneVerticesCenter = lane.getLaneVerticesCenter();
    	this.laneVerticesInner = lane.getLaneVerticesInner();
    	this.laneVerticesOuter = lane.getLaneVerticesOuter();
    	this.downLanes = lane.getDown();
    	this.upLanes = lane.getUp();
    	this.crossSectionElement = lane.getCse();
	}*/
    /**
     * Create a Lane with default values.
     * TODO: make this creator unneeded; it should not exist. 
     */
    public Lane() {
    	this.id = laneCount;
    	if (laneCount == 49)
    		System.out.println("Creating lane " + laneCount);
    	laneCount++;
	}
    
    /**
     * Reset lane ID counter to 0.
     */
    public static void resetLaneIDGenerator() {
    	laneCount = 0;
    }

	/**
     * Retrieve the CrossSectionElement that owns this Lane.
     * @return CrossSectionElement that owns this Lane
     */
    public CrossSectionElement getCse() {
		return crossSectionElement;
	}

    // TODO explain why/when the CrossSectionElement of an existing Lane needs to be changed or remove the need
    /**
     * Replace the CrossSectionElement that owns this Lane.
     * @param cse CrossSectionElement; new owner of this Lane
     */
	public void setCse(CrossSectionElement cse) {
		this.crossSectionElement = cse;
	}

	/**
	 * Retrieve the destination of this Lane.
	 * @return Integer; number of the destination of this Lane
	 */
	public int getDestination() {
		return destination;
	}

	/**
	 * Set the destination of this Lane.
	 * @param destination Integer; new value for the destination of this Lane
	 */
	// TODO make this function go away
	public void setDestination(int destination) {
		this.destination = destination;
	}

	public ArrayList<Lane> getMergingYieldToLaneList() {
		return mergingYieldToLaneList;
	}

	public void addMergingYieldToLaneList(Lane lane) {
		if (mergingYieldToLaneList == null)
			mergingYieldToLaneList = new ArrayList<Lane>();
		mergingYieldToLaneList.add(lane);
	}

	/**
	 * Retrieve the list of Lanes that conflict with this Lane.
	 * @return ArrayList&lt;Lane&gt; list of conflicting Lanes
	 */
	public ArrayList<Lane> getCrossingYieldToLaneList() {
		return crossingYieldToLaneList;
	}
	
	/**
	 * Add one Lane to the list of Lanes that conflict with this Lane.
	 * @param lane Lane to add to the list of conflicting lanes
	 */
	public void addCrossingYieldToLaneList(Lane lane) {
		if (crossingYieldToLaneList == null)
			crossingYieldToLaneList = new ArrayList<Lane>();
		crossingYieldToLaneList.add(lane);
	}
	
	/**
	 * Add one Lane to the list of upLanes of this Lane.
	 * @param lane Lane to add to the list of upLanes
	 */
	public void addUpLane(Lane lane) {
		if (null == upLanes)
			upLanes = new ArrayList<Lane>();
		upLanes.add(lane);
	}
	
	/**
	 * Clear the list of upLanes of this Lane.
	 */
	public void clearUpLanes () {
		upLanes = null;
	}
	
	/**
	 * Generate a list of lane IDs of all upLanes of this Lane.
	 * @return ArrayList&lt;Integer&gt; List of lane IDs
	 */
	public ArrayList<Integer> getUpLaneIDs() {
		ArrayList<Integer> result = new ArrayList<Integer> ();
		if (null != upLanes)
			for (Lane lane : upLanes)
				result.add(lane.id);
		return result;
	}
	
	/**
	 * Add one Lane to the list of downLanes of this Lane.
	 * @param lane Lane to add to the list of downLanes
	 */
	public void addDownLane(Lane lane) {
		if (null == downLanes)
			downLanes = new ArrayList<Lane>();
		downLanes.add(lane);
	}
	
	/**
	 * Clear the list of downLanes of this Lane.
	 */
	public void clearDownLanes() {
		downLanes = null;
	}

	/**
	 * Generate a list lane IDs of all downLanes of this Lane.
	 * @return ArrayList&lt;Integer&gt; List of lane IDs
	 */
	public ArrayList<Integer> getDownLaneIDs() {
		ArrayList<Integer> result = new ArrayList<Integer> ();
		if (null != downLanes)
			for (Lane lane : downLanes)
				result.add(lane.id);
		return result;
	}
	
	/**
	 * Retrieve the origin of this Lane.
	 * @return Integer; number of the origin of this Lane
	 */
	public int getOrigin() {
		return origin;
	}

	/**
	 * Set/change the origin of this Lane.
	 * @param source Integer; new value for the origin of this Lane
	 */
	// TODO make this function go away
	public void setOrigin(int source) {
		this.origin = source;
	}

	/**
	 * Retrieve the turnArrow of this Lane.
	 * @return TurnArrow; the turnArrow of this Lane, or null if this Lane has no turnArrow.
	 */
	public TurnArrow getTurnArrow() {
		return turnArrow;
	}

	/*
	public void setTurnArrow(TurnArrow turnArrow) {
		this.turnArrow = turnArrow;
	}*/

	/**
	 * Retrieve the {@link StopLine} of this Lane. 
	 * @return {@link StopLine}; or null if this Lane does not have a stop line
	 */
	public StopLine getStopLine() {
		return stopLine;
	}

	public void setStopLine(StopLine stopLine) {
		this.stopLine = stopLine;
	}

	/**
	 * Retrieve the lane ID of this Lane. Lane IDs should be unique.
	 * @return Integer; the ID of this Lane
	 */
	public int getID() {
        return id;
    }
	
	/*
    public int getCapacity() {
        return capacity;
    }
    */

	/*
    public void setLaneId(int laneId) {
        this.laneId = laneId;
    }
    */

	/*
	public int getLinkId() {
		return linkId;
	}
	*/

	/*
	public void setLinkId(int linkId) {
		this.linkId = linkId;
	}
	*/

	/*
    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }
    */

	/**
	 * Retrieve the right neighbor of this Lane.
	 * @return Lane; or null if this Lane has no right neighbor
	 */
    public Lane getRight() {
		return right;
	}

	/**
	 * Set the Lane that is the right neighbor of this Lane. 
	 * @param right Lane; new right neighbor of this Lane (or null to indicate
	 * that this Lane has no right neighbor)
	 */
	public void setRight(Lane right) {
		this.right = right;
	}

	/**
	 * Retrieve the left neighbor of this Lane.
	 * @return Lane; or null if this Lane has no left neighbor
	 */
	public Lane getLeft() {
		return left;
	}

	/**
	 * Set the Lane that is the left neighbor of this Lane.
	 * @param left Lane; new left neighbor of this Lane (or null to indicate
	 * that this Lane has no left neighbor)
	 */
	public void setLeft(Lane left) {
		this.left = left;
	}

	public ArrayList<Lane> getUp() {
		return upLanes;
	}

	/* NEVER USED
	public void setUp(ArrayList<Lane> up) {
		this.upLanes = up;
	}*/

	public ArrayList<Lane> getDown() {
		return downLanes;
	}

	/* NEVER USED
	public void setDown(ArrayList<Lane> down) {
		this.downLanes = down;
	}*/

	public boolean isGoRight() {
		return goRight;
	}

	public void setGoRight(boolean goRight) {
		this.goRight = goRight;
	}

	public boolean isGoLeft() {
		return goLeft;
	}

	public void setGoLeft(boolean goLeft) {
		this.goLeft = goLeft;
	}

    public void connectLateralRight(Lane rightNeighbor) {
        this.right = rightNeighbor;
        rightNeighbor.left = this;
    }
    
    public void connectLateralLeft(Lane leftNeighbor) {
        this.left = leftNeighbor;
        leftNeighbor.right = this;
    }
    
    public double getWidth() {
		return lateralWidth;
	}

	public void setWidth(double lateralWidth) {
		this.lateralWidth = lateralWidth;
	}

	public ArrayList<Vertex> getLaneVerticesInner() {
		return laneVerticesInner;
	}
	
	public ArrayList<Vertex> getLaneVerticesOuter() {
		return laneVerticesOuter;
	}
	
	public GeneralPath createLanePolygon()   {
    	ArrayList<Vertex> inner = getLaneVerticesInner();
    	ArrayList<Vertex> outer = getLaneVerticesOuter();
    	if ((null == inner) || (null == outer)) {
    		System.err.println("Lane.paint: laneVertices is null");
    		return null;
    	}
		GeneralPath polygon = new GeneralPath(Path2D.WIND_EVEN_ODD);
		boolean firstPoint = true;
		ArrayList<Vertex> v1 = new ArrayList<Vertex>();
		ArrayList<Vertex> v2 = new ArrayList<Vertex>();
		double scale = 0.33;
		for (int i = 0; i < inner.size(); i++)  {
			Vertex vTemp = new Vertex();
			vTemp.x  = inner.get(i).getX() + scale * (outer.get(i).getX() - inner.get(i).getX()); 
			vTemp.y  = inner.get(i).getY() + scale * (outer.get(i).getY() - inner.get(i).getY()); 
			v1.add(vTemp);
			vTemp = new Vertex();
			vTemp.x  = inner.get(i).getX() + (1 - scale) * (outer.get(i).getX() - inner.get(i).getX()); 
			vTemp.y  = inner.get(i).getY() + (1 - scale) * (outer.get(i).getY() - inner.get(i).getY()); 
			v2.add(vTemp);			
		}
		
		for (Vertex v : v1) {
			if (firstPoint)
				polygon.moveTo(v.getX(), v.getY());
			else
				polygon.lineTo(v.getX(), v.getY());
			firstPoint = false;
		}
		for (Vertex v : Reversed.reversed(v2))	// reverse the outer point list
			 polygon.lineTo(v.getX(), v.getY());
		if (! firstPoint)
			polygon.closePath();
		return polygon;
	}
	
	public double getMaxSpeed() {
		return maxSpeed;
	}

	public void setMaxSpeed(double maxSpeed) {
		this.maxSpeed = maxSpeed;
	}
	
	public boolean isParkingLane() {
		return maxSpeed < 5;
	}
	
	/*
	public Point2D.Double getCtrlPointCenter() {
		return ctrlPointCenter;
	}

	public void setCtrlPointCenter(Point2D.Double ctrlPointCenter) {
		this.ctrlPointCenter = ctrlPointCenter;
	}
	*/

	public void setDesignLine(ArrayList<Vertex> laneVerticesCenter) {
		this.laneVerticesCenter = laneVerticesCenter;
	}
	public ArrayList<Vertex> getLaneVerticesCenter() {
		return laneVerticesCenter;
	}
	
	public void setLaneVerticesInner(ArrayList<Vertex> laneVerticesInner) {
		this.laneVerticesInner = laneVerticesInner;
	}

	public void setLaneVerticesOuter(ArrayList<Vertex> laneVerticesOuter) {
		this.laneVerticesOuter = laneVerticesOuter;
	}
	
	public ArrayList<Lane> getUpLanes_r () {
		return upLanes;
	}
	
	public ArrayList<Lane> getDownLanes_r () {
		return downLanes;
	}
	
	public String getDestination_r () {
		return "" + destination;
	}
	
	public Link getLink_r() {
		return getCse().getCrossSection().getLink();
	}
	
	@Override
	public String toString() {
		return String.format("Lane %d from %d to %d, left %.3fm, right %.3fm, length %.3fm", 
				id, origin, destination, lateralPosition, lateralPosition + lateralWidth, 
				Planar.length(getLaneVerticesCenter()));
	}
	
	/**
	 * Paint this Lane on a {@link GraphicsPanel}.
	 * <br /> if the Link to which this Lane is a child is autoGenerated, this
	 * Link is painted at reduced width.
	 * @param graphicsPanel {@link GraphicsPanel} to paint on
	 */
	@Override
	public void paint(GraphicsPanel graphicsPanel) {
    	Color fillColor = Color.LIGHT_GRAY;
    	GeneralPath polygon = createLanePolygon();
    	graphicsPanel.setStroke(0f);	// hair line
    	Color lineColor = Color.WHITE;
    	graphicsPanel.drawGeneralPath(polygon, lineColor, fillColor);
    	graphicsPanel.setColor(Color.RED);
    	if (stopLine != null)  {
    		boolean oldOpaque = stopLine.isOpaque();
    		Color oldColor = stopLine.getColor();
    		Color color = new Color(1f, 1f, 1f, 1f);
    		stopLine.setColor(color);
    		stopLine.setOpaque(true);
    		stopLine.paint(graphicsPanel);
    		stopLine.setColor(oldColor);
    		stopLine.setOpaque(oldOpaque);
    	}
    	if (turnArrow != null)
    		turnArrow.paint(graphicsPanel);
    	
        if (this.getCse().getCrossSection().getLink().network.selectedLane == this)   {
        	fillColor = Color.BLACK;
        	lineColor = Color.RED;
        	graphicsPanel.drawGeneralPath(polygon, lineColor, fillColor);
        	//this.getCse().getCrossSection().getLink().network.selectedLaneControlPoint = this.getCtrlPointCenter();
        	/*
        	if (this.getCtrlPointCenter() != null)  {
                graphicsPanel.setStroke(8f);
                graphicsPanel.drawCircle(this.getCtrlPointCenter(), Color.BLACK, 6);
        	}
        	*/
    	}
    	//TODO: if turn arrows are property of a lane; these should be painted here as well

	}

	@Override
	public boolean writeXML(StaXWriter staxWriter) {
		return true;	// All lanes are automatically generated and never stored
	}

	/**
	 * Draw the Lane ID on this Lane.
	 * @param graphicsPanel {@link GraphicsPanel}; output device to paint on
	 */
	public void paintID(GraphicsPanel graphicsPanel) {
		// Find a suitable point to paint the lane number
		Vertex firstPoint = laneVerticesCenter.get(0);
		Vertex lastPoint = laneVerticesCenter.get(laneVerticesCenter.size() - 1);
		final double ratio = 0.1;	// put the label on the lane, near the beginning of the lane
		Point2D.Double textPoint = Vertex.weightedVertex(ratio, firstPoint, lastPoint).getPoint();
		graphicsPanel.setColor(Color.WHITE);
		graphicsPanel.drawString(String.format("%d", id), textPoint);
	}

	/**
	 * Export a this Lane.
	 * @return String; textual description of this Lane
	 */
	public String export() {
		String result = String.format("LaneData\tlaneID:\t%d", getID());

		for (Integer laneID : getUpLaneIDs())				
			result += String.format("\tup:\t%d", laneID); 
		for (Integer laneID : getDownLaneIDs())
			result += String.format("\tdown:\t%d", laneID); 
		if (getCrossingYieldToLaneList() != null)  {
			for (Lane yLane : getCrossingYieldToLaneList())  {
				result += String.format("\tcrossingYieldTo:\t%d", yLane.getID()); 
			}
		}
		if (getMergingYieldToLaneList() != null)  {
			for (Lane yLane : getMergingYieldToLaneList())  {
				result += String.format("\tmergingYieldTo:\t%d", yLane.getID()); 
			}
		}
		if (getLeft() != null)
			result += String.format("\tleft:\t%d", getLeft().getID()); 
		if (getRight() != null)
			result += String.format("\tright:\t%d", getRight().getID()); 
		if (isGoLeft())
			result += String.format("\tgoLeft:\t%s", isGoLeft()); 
		if (isGoRight())
			result += String.format("\tgoRight:\t%s", isGoRight()); 
		if (getOrigin() >= 0)
			result += String.format("\torigin:\t%d", getOrigin()); 
		// FIXME Does not write destination nodes that are not only a sink
		if ((getDestination() >= 0) || crossSectionElement.getCrossSection().getLink().getToNode_r().isSink())
			result += String.format("\tdestination:\t%d", getDestination()); 
		result += "\n";
		return result;
	}
    
}
