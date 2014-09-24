package nl.tudelft.otsim.Simulators.LaneSimulator;

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
        justExceededLane = false;
        if (x > lane.l) {
            justExceededLane = true;
            // check whether adjacent neighbours need to be reset
            // these will be found automatically by updateNeighbour() in
            // the main model loop
            if (lane.left!=null && lane.left.down!=lane.down.left) {
                leftUp = null;
                leftDown = null;
            }
            if (lane.right!=null && lane.right.down!=lane.down.right) {
                rightUp = null;
                rightDown = null;
            }
            // put on downstream lane
            x -= lane.l;
            lane.vehicles.remove(this);
            lane.down.vehicles.add(this);
            lane = lane.down;
        }
    }

    /**
     * Sets global x and y coordinates of lane change vehicle.
     */
    @Override
	public void setXY() {
        // global position is always at lane
        java.awt.geom.Point2D.Double coord = atLaneXY();
        globalX = coord.x;
        globalY = coord.y;
    }
    
    /**
     * Returns the driver of the attached regular vehicle.
     * @return Driver of regular vehicle.
     */
    @Override
	public Driver getDriver() {
        return vehicle.getDriver();
    }
}