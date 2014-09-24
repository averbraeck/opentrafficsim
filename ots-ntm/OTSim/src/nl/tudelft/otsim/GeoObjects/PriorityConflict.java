package nl.tudelft.otsim.GeoObjects;

import java.awt.Color;
import java.awt.Polygon;
import java.awt.geom.GeneralPath;
import java.awt.geom.Path2D;

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
    private StopLine stopLine;
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
    protected PriorityConflict(Lane priorityLane, Lane yieldLane, conflictType type, Polygon conflictArea) {
    	assert(priorityLane != yieldLane);
        this.setStopLine(stopLine);
        this.conflictType = type;
        this.conflictArea = conflictArea;
    }
       
	private void setStopLine(StopLine stopLine) {
		this.stopLine = stopLine;
	}

	/**
	 * Retrieve the conflictArea of this PriorityConflict.
	 * @return Polygon; the conflictArea of this PriorityConflict
	 */
	public Polygon getConflictArea() {
		return conflictArea;
	}

	/**
	 * Return a closed GeneralPath that describes the area of this PriorityConflict.
	 * @return GeneralPath; the area of this PriorityConflict
	 */
	public GeneralPath createPolygon()   {
		GeneralPath polygon = new GeneralPath(Path2D.WIND_EVEN_ODD);
		if (conflictArea.npoints == 0)
			throw new Error("Degenerate polygon");
			//return polygon;
		polygon.moveTo(conflictArea.xpoints[0], conflictArea.ypoints[0]);
		for (int i = 1; i < conflictArea.npoints; i++)
			polygon.lineTo(conflictArea.xpoints[i],  conflictArea.ypoints[i]);
		polygon.closePath();
		return polygon;
	}
	
	/**
	 * Paint this PolyZone on a {@link GraphicsPanel}.
	 * @param graphicsPanel {@link GraphicsPanel}; the graphicsPanel to paint
	 * this PolyZone on
	 */
	public void paint(GraphicsPanel graphicsPanel) {
    	Color color = new Color(1f, 0f, 0f, 0.2f);
		GeneralPath polygon = this.createPolygon();  	
    	graphicsPanel.setStroke(1F);
    	if (polygon != null)  {
    		graphicsPanel.setColor(color);	
    		Color lineColor = color;
    		Color fillColor = color;
    		graphicsPanel.drawGeneralPath(polygon, lineColor, fillColor);
    	}
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