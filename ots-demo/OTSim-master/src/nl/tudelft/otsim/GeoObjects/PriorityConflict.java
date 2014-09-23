package nl.tudelft.otsim.GeoObjects;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.geom.Point2D;

import nl.tudelft.otsim.GUI.GraphicsPanel;


/**
 * @author WouterSchakel, adapted and adjusted by gtamminga
 * Represents a conflict between two lanes. Conflicts can be split, merge or
 * crossing conflicts and are included in the driver behavior by using two RSUs. 
 * The RSUs are located at the <b>end</b> of the conflict area and also function
 * as a speed reduction such that drivers will have the speed according to the 
 * lane at the end of the conflict area. A conflict, along with the RSUs, can be
 * created with the static methods <tt>createSplit</tt>, <tt>createMerge</tt> 
 * and <tt>createCrossing</tt>. The first two methods will also connect the 
 * lanes longitudinally in an appropriate way. A crossing conflict may or may not 
 * be an area that needs to be kept clear (depending on driver compliance). 
 */
public class PriorityConflict {

    /** Type of the conflict being either split, merge or crossing. */
    protected conflictType conflictType;
    private Lane priorityLane;
    private Lane yieldLane;
    private StopLine stopLine;
	private double priorityLongPosition;
	private double yieldLongPosition;
	private Polygon conflictArea;
       
    /**
     * Constructor. Not public as conflicts should be created by using 
     * <tt>createSplit</tt>, <tt>createMerge</tt> or <tt>createCrossing</tt>.
     * Note that some properties have no effect for some conflict types, i.e. 
     * priority order on a split and <tt>clear</tt> on a merge or split.
     * @param priorityLane Lane with priority.
     * @param yieldLane Lane without priority.
     * @param mergeLane Lane where lanes split of merge.
     * 

     */
    protected PriorityConflict(Lane pLane, double pLongitudinal, Lane yLane, double yLongitudinal, conflictType type, Polygon conflictArea) {
        this.setStopLine(stopLine);
        this.priorityLane = pLane;
        this.setPriorityLongPosition(pLongitudinal);
        this.yieldLane = yLane;
        this.yieldLongPosition = yLongitudinal;
        this.conflictType = type;
        this.conflictArea = conflictArea;
    }
        
    public double getYieldLongPosition() {
		return yieldLongPosition;
	}

	public void setYieldLongPosition(double yieldLongPosition) {
		this.yieldLongPosition = yieldLongPosition;
	}

	public Lane getPriorityLane() {
		return priorityLane;
	}

	public void setPriorityLane(Lane priorityLane) {
		this.priorityLane = priorityLane;
	}

	public Lane getYieldLane() {
		return yieldLane;
	}

	public void setYieldLane(Lane yieldLane) {
		this.yieldLane = yieldLane;
	}

	public StopLine getStopLine() {
		return stopLine;
	}

	public void setStopLine(StopLine stopLine) {
		this.stopLine = stopLine;
	}

	public double getPriorityLongPosition() {
		return priorityLongPosition;
	}

	public void setPriorityLongPosition(double priorityLongPosition) {
		this.priorityLongPosition = priorityLongPosition;
	}

	public Polygon getConflictArea() {
		return conflictArea;
	}

	public void setConflictArea(Polygon conflictArea) {
		this.conflictArea = conflictArea;
	}

	
	/**
	 * Paint this PolyZone on a {@link GraphicsPanel}.
	 * @param graphicsPanel {@link GraphicsPanel}; the graphicsPanel to paint
	 * this PolyZone on
	 */
	public void paint(GraphicsPanel graphicsPanel) {
    	Point2D.Double[] outline = new Point2D.Double[getConflictArea().npoints];
    	double[] x = new double[getConflictArea().npoints];
    	double[] y = new double[getConflictArea().npoints];

    	for (int i=0; i < getConflictArea().npoints; i++ )  {
        	x[i] = getConflictArea().xpoints[i];
        	y[i] = getConflictArea().ypoints[i];
        	Point2D.Double p = new Point2D.Double(x[i],y[i]);
        	outline[i] = p;
    	}
    	graphicsPanel.setStroke(1F);
    	Color color = new Color(1f, 0f, 0f, 0.2f);
    	graphicsPanel.setColor(color);
    	graphicsPanel.drawPolygon(outline);
	}
	/**
     * Enumeration of conflict types.
     */
    protected enum conflictType {
        /** Split conflict. */
        SPLIT,
        /** Merge conflict. */
        MERGE,
        /** Crossing conflict. */
        CROSSING,
    }
	
}