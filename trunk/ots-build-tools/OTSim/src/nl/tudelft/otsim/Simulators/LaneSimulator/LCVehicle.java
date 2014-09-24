package nl.tudelft.otsim.Simulators.LaneSimulator;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

import nl.tudelft.otsim.GUI.GraphicsPanel;

/**
 * Temporary vehicle as a place-holder during a lane change.
 */
public class LCVehicle extends Movable {

    /** Regular vehicle of the lane change. */
    public Vehicle vehicle;

    /**
     * Constructor that initializes the speed and length according to the
     * vehicle.
     * @param vehicle Lane changing vehicle.
     */
    public LCVehicle(Vehicle vehicle) {
        super(vehicle.model);
        this.vehicle = vehicle;
        a = vehicle.a;
        v = vehicle.v;
        l = vehicle.l;
    }

    /**
     * Moves the movable a certain distance downstream, entering new lanes as
     * required.
     * @param dx Distance to translate.
     */
    @Override
	public void translate(double dx) {
        // Move movable downstream
        x += dx;
        //justExceededLane = false;
        if (x > getLane().l) {
            //justExceededLane = true;
            // check whether adjacent neighbors need to be reset
            // these will be found automatically by updateNeighbour() in
            // the main model loop
            if ((getLane().left != null) && (getLane().left.down != getLane().down.left)) {
            	setNeighbor(Movable.LEFT_UP, null);
            	setNeighbor(Movable.LEFT_DOWN, null);
            }
            if ((getLane().right != null) && (getLane().right.down != getLane().down.right)) {
            	setNeighbor(Movable.RIGHT_UP, null);
            	setNeighbor(Movable.RIGHT_DOWN, null);
            }
            // put on downstream lane
            x -= getLane().l;
            getLane().cut(this);
            getLane().down.paste(this, x);
            setLane(getLane().down);
            if (getLane().isMerge() || getLane().isSplit()) {
                Lane lTmp = getLane();
                cut();
                paste(lTmp, x);
            }
        }
        getLane().cut(this);
        getLane().paste(this, this.x);
    }

    /**
     * Sets global x and y coordinates of lane change vehicle.
     */
    @Override
	public void setXY() {
        // global position is always at lane
        java.awt.geom.Point2D.Double coord = atLaneXY();
        global = new java.awt.geom.Point2D.Double(coord.x, coord.y);
    }
    
    /**
     * Returns the driver of the attached regular vehicle.
     * @return Driver of regular vehicle.
     */
    @Override
	public Driver getDriver() {
        return vehicle.getDriver();
    }
    
	private Double[] outline() {
		double stepFraction = 0; // FIXME
    	Point2D.Double[] result = new Point2D.Double[4];
        final double halfWidth = 1;	// m
        final double xFront = global.x + heading.x * stepFraction;
        final double yFront = global.y + heading.y * stepFraction;
        
        result[0] = new Point2D.Double(xFront + heading.y * halfWidth, yFront - heading.x * halfWidth);
        result[1] = new Point2D.Double(xFront - heading.y * halfWidth, yFront + heading.x * halfWidth);

        final double xRear = xFront - heading.x * l;
        final double yRear = yFront - heading.y * l;
        
        result[2] = new Point2D.Double(xRear - heading.y * halfWidth, yRear + heading.x * halfWidth);
        result[3] = new Point2D.Double(xRear + heading.y * halfWidth, yRear - heading.x * halfWidth);      	
    	return result;
	}

	/**
	 * Paint a ghost image of this lane changing vehicle
	 * @param when Double; simulation time at which the ghost image must be drawn
	 * @param graphicsPanel {@link GraphicsPanel} drawing target
	 */
	public void paint(double when, GraphicsPanel graphicsPanel) {
		if (! getLane().isVisible())
			return;
        if (null == global) {
        	System.err.println("LCVehicle.outline: global is NULL");
        	return;
        }
        graphicsPanel.setColor(Color.BLACK);
        graphicsPanel.setStroke(0.1f);
        graphicsPanel.drawPolyLine(outline(), true);
        Point2D.Double[] outline = outline();
		graphicsPanel.setColor(new Color(255,192,192));
        graphicsPanel.setStroke(0f);
        graphicsPanel.drawPolygon(outline);
	}
}