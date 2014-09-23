package nl.tudelft.otsim.Simulators.LaneSimulator;

import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.geom.Point2D.Double;

import nl.tudelft.otsim.GUI.GraphicsPanel;
import nl.tudelft.otsim.GUI.Main;
import nl.tudelft.otsim.Simulators.SimulatedObject;

/**
 * Default wrapper for a vehicle. It contains a driver and possibly an OBU.
 */
public class Vehicle extends Movable implements SimulatedObject {

    /** OBU within the vehicle, may be <tt>null</tt>. */
    public OBU OBU;
    
    /** Driver of the vehicle. */
    public Driver driver;
    
    /** Current lateral speed in 'amount of lane per time step' [0...1]. */
    public double dy;
    
    /** Total progress of a lane change in 'amount of lane' [0...1]. */
    public double lcProgress;
    
    /** Lane change direction. */
    public Model.latDirection lcDirection;
    
    /** Temporary lane change vehicle, if any. */
    public LCVehicle lcVehicle;
    
    /** Maximum vehicle speed [km/h]. */
    public double vMax;
    
    /** Maximum vehicle deceleration (a value below 0) [m/s^2]. */
    public double aMin;
    
    /** Route of the vehicle (or driver). */
    public Route route;
    
    /** Vehicle trajectory. */
    public Trajectory trajectory;
    
    /** Vehicle class ID. */
    public int classID;
    
    /** Set of ordered RSUs in <tt>RSURange</tt>. */
    protected java.util.ArrayList<RSU> RSUsInRange = new java.util.ArrayList<RSU>();
    
    /** Range within which RSUs are connected [m]. */
    protected double RSURange;
    
    /** Time step of last move */
    protected int moved = -1;
    
    /** Lane where the rear is located, used for heading determination. */ 
    protected Lane rearLane;

    /** 
     * Constructor connecting the vehicle with the main model. 
     * @param model Main model.
     */
    public Vehicle(Model model) {
        super(model);
    }

    /**
     * Sets the acceleration which is limited by vehicle capabilities and by
     * reaching a speed of zero.
     * @param a Desired acceleration.
     */
    public void setAcceleration(double a) {
        // limit acceleration to maximum deceleration
        a = a < aMin ? aMin : a;
        // limit acceleration to lack of speed (getting to zero at end of time step)
        double aZero = -v / model.dt;
        a = a < aZero ? aZero : a;
        this.a = a;
    }
    
    /**
     * Moves the vehicle in longitudinal and lateral direction based on 
     * <tt>a</tt> and <tt>dy</tt>.
     */
    public void move() {
    	// skip if moved
    	if (moved >= getLane().model.k)
    		return;
    	/*
    	// move leaders first
    	Movable leader = getNeighbor(Movable.DOWN);
    	if (null != leader)
    		leader.getDriver().vehicle.move();
    	if (null != lcVehicle) {
    		Movable LCLeader = lcVehicle.getNeighbor(Movable.DOWN);
    		if (null != LCLeader)
    			LCLeader.getDriver().vehicle.move();
    	}
    	*/
    	//tag as moved
    	moved = getLane().model.k;
        // lateral
        lcProgress += dy;
        // longitudinal
        double dt = model.dt;
        double dx = dt * v + .5 * a * dt * dt;
        dx = dx >= 0 ? dx : 0;
        v = v+dt*a;
        // limit speed to positive values
        v = v >= 0 ? v : 0;
        // clip very small values to zero for v==0 evaluations
        v = v < 0.000001 ? 0 : v;
        translate(dx);
        setXY();
    }

    /**
     * Function to translate a distance, moving onto downstream lanes as needed.
     * If a destination is reached the vehicle is deleted.
     * @param dx Distance [m] to translate.
     */
    @Override
	public void translate(double dx) {
        // Update RSUs in range and pass them if appropriate
        double s = 0;
        // Get point to search from
        Lane lastLane;
        double lastX;
        if (RSUsInRange.isEmpty()) {
            // search downstream of vehicle
            lastLane = getLane();
            lastX = x;
            s = 0;
        } else {
            // search downstream of last RSU
            RSU rsu = RSUsInRange.get(RSUsInRange.size() - 1);
            if (rsu instanceof Lane.splitRSU) {
                // at a split, search from x=0 at the next lane
            	//if (3 == id)
            	//	System.out.println("Vehicle " + id + " finds splitRSU " + ((Lane.splitRSU) rsu).toString());
                lastLane = ((Lane.splitRSU) rsu).getLaneForRoute(route);
                lastX = 0;
            } else {
                lastLane = rsu.lane;
                lastX = rsu.x;
            }
            s = getDistanceToRSU(rsu) - dx;
        }
        // Add new RSUs in range
        java.util.ArrayList<RSU> next = new java.util.ArrayList<RSU>();
        int attempt = 0;
        while ((s < RSURange) && (null != lastLane)) {
            // lastLane may become null at a split where the route has no
            // appropriate downstream lane, as the vehicle has to change lane
            // before the split
        	// FIXME this is just an ugly hack to break infinite loops
        	if (++attempt > 100) {
        		s = RSURange;
        		break;
        	}
            next = lastLane.findRSU(lastX, RSURange - s);
            if (!next.isEmpty()) {
                s = getDistanceToRSU(next.get(0)) - dx;
                for (RSU j : next) {
                    RSUsInRange.add(j);
                    if (j instanceof Lane.splitRSU) {	// continue search on next lane after split
                        try {
							lastLane = ((Lane.splitRSU) j).getLaneForRoute(route);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							lastLane = ((Lane.splitRSU) j).getLaneForRoute(route);
						}
                        lastX = 0;
                    } else {	// continue search after RSU
                        lastLane = j.lane;
                        lastX = j.x;
                    }
                }
            } else
                s = RSURange; // stop loop
        }
        // Pass RSUs
        for (java.util.Iterator<RSU> it = RSUsInRange.iterator(); it.hasNext(); ) {
            RSU rsu = it.next();
            s = getDistanceToRSU(rsu) - dx;
            if (s < 0) {
            	//if ((3 == id) && (rsu instanceof Lane.splitRSU))
            	//	System.out.println("Vehicle " + id + " passes splitRSU " + ((Lane.splitRSU) rsu).toString());
                if (rsu.passable || rsu.noticeable) {
                    rsu.pass(this);
                    if (! model.vehicles.contains(this))
                    	return;	// this vehicle was deleted by rsu.pass
                }
                it.remove();
            }
        }

        // Move movable downstream
        x += dx;
        //justExceededLane = false;
        while (x > getLane().l) {
            //justExceededLane = true;
            if ((null == getLane().down) && (0 == getLane().destination)) {
                model.deleted++;
                System.out.println("Vehicle deleted as lane " + getLane().id + " is exceeded (" + model.deleted + "), dead end");
                delete();
                return;
            } else if (route.destinations().length == 1 && getLane().destination==route.destinations()[0]) {
                // vehicle has reached (a) destination
                if (model.settings.getBoolean("storeTrajectoryData") && trajectory != null)
                    model.saveTrajectoryData(trajectory);
                delete();
                return;
            } else {	// update route
                if (getLane().destination > 0)
                    route = route.subRouteAfter(getLane().destination);
                /*
                // check whether route is still reachable
                if (!route.canBeFollowedFrom(getLane().down)) {
	                model.deleted++;
	                System.out.println("Vehicle deleted as lane " + getLane().id + " is exceeded (" + model.deleted + "), route " + route.toString() + " unreachable");
	                System.out.println(getLane().lanechanges.entrySet());
	                System.out.println(toString());
	                delete();
	                return;
                } 
                */
            	// abort impossible lane change
                if (null != lcVehicle )
                    if ((lcDirection==Model.latDirection.RIGHT && (getLane().down.right==null || getLane().down.right!=lcVehicle.getLane().down)) ||
                            (lcDirection==Model.latDirection.LEFT && (getLane().down.left==null || getLane().down.left!=lcVehicle.getLane().down)))
                        abortLaneChange();
                // check whether adjacent neighbors need to be reset
                // these will be found automatically by updateNeighbour() in
                // the main model loop
                if (null == getLane().down) {
	                model.deleted++;
	                System.out.println("Vehicle deleted as it reaches a dead end on " + getLane().id + " (" + model.deleted + "), route " + route.toString() + " unreachable");
	                delete();
	                return;
                }
                if ((null != getLane().left) && (getLane().left.down != getLane().down.left)) {
                	setNeighbor(Movable.LEFT_UP, null);
                	setNeighbor(Movable.LEFT_DOWN, null);
                }
                if ((null != getLane().right) && (getLane().right.down != getLane().down.right)) {
                	setNeighbor(Movable.RIGHT_UP, null);
                	setNeighbor(Movable.RIGHT_DOWN, null);
                }
                // put on downstream lane
                x -= getLane().l;
                getLane().cut(this);
                getLane().down.paste(this, x);
                if (getLane().down.isMerge()) {
                    // register the current lane as the origin of the last  
                    // entering vehicle on the next lane
                    getLane().down.mergeOrigin = getLane();
                }
                setLane(getLane().down);
                if (getLane().isMerge() || getLane().isSplit()) {
                	Movable downNeighbor = getNeighbor(Movable.DOWN);
                	if (null != downNeighbor) {
                		if (downNeighbor instanceof Vehicle)
                			((Vehicle) downNeighbor).move();
                		else if (downNeighbor instanceof LCVehicle)
                			((LCVehicle) downNeighbor).vehicle.move();
                		else
                			throw new Error ("Do not know how to move an object of type " + downNeighbor.getClass());
                	}
                    Lane lTmp = getLane();
                    cut();
                    paste(lTmp, x);
                }
            }
        }
        getLane().cut(this);
        getLane().paste(this, this.x);
        // Move lane change vehicle
        if (lcVehicle != null) {
            double xNew = 0;
            double xAdj = 0;
            if (lcDirection == Model.latDirection.LEFT) {
                xNew = getAdjacentX(Model.latDirection.LEFT);
                xAdj = lcVehicle.getLane().xAdj(getLane().left);
            } else {
                xNew = getAdjacentX(Model.latDirection.RIGHT);
                xAdj = lcVehicle.getLane().xAdj(getLane().right);
            }
            lcVehicle.translate(xNew+xAdj - lcVehicle.x);
            lcVehicle.a = a;
            lcVehicle.v = v;
            lcVehicle.setXY();
        }
        
        // Append trajectory
        if (model.settings.getBoolean("storeTrajectoryData") && trajectory != null)
            trajectory.append();
     }
    
    /**
     * Sets the range within which RSUs are linked to the vehicle.
     * @param range Range within which RSUs are linked [m].
     */
    public void setRSURange(double range) {
        RSURange = range;
    }

    /**
     * Starts a lane change by creating an <tt>lcVehicle</tt>.
     */
    public void startLaneChange() {
        lcVehicle = new LCVehicle(this);//
        model.addVehicle(lcVehicle);//
        Lane atLane;
        if (lcDirection==Model.latDirection.LEFT)
            atLane = getLane().left;
        else
            atLane = getLane().right;
        double atX = getAdjacentX(lcDirection);
        lcVehicle.paste(atLane, atX);
    }

    /**
     * Ends a lane change by deleting the <tt>lcVehicle</tt> and changing the lane.
     */
    public void endLaneChange() {
        // set vehicle at target lane and delete temporary vehicle
        lcVehicle.delete();
        Lane targetLane;
        double targetX = getAdjacentX(lcDirection);
        if (lcDirection==Model.latDirection.LEFT)
            targetLane = getLane().left;
        else
            targetLane = getLane().right;
        cut();
        paste(targetLane, targetX);
        lcProgress = 0;
        dy = 0;
        // remove RSUs in range due to new lane
        RSUsInRange.clear();
        rearLane = null;
    }

    /**
     * Aborts a lane change by deleting the <tt>lcVehicle</tt>.
     */
    public void abortLaneChange() {
        // instantaneous abort of lane change
        lcVehicle.delete();
        lcProgress = 0;
        dy = 0;
        rearLane = null;
    }

    /**
     * Initiates a lateral movement to the left.
     * @param newDY Initial speed of the lateral movement in 'amount of lane per time step'.
     */
    public void changeLeft(double newDY) {
        lcDirection = Model.latDirection.LEFT;
        if (Math.abs(newDY) > 2)
        	System.out.println("Excessive lateral speed: " + newDY);
        this.dy = newDY;
    }

    /**
     * Initiates a lateral movement to the right.
     * @param newDY Initial speed of the lateral movement in 'amount of lane per time step'.
     */
    public void changeRight(double newDY) {
        lcDirection = Model.latDirection.RIGHT;
        if (Math.abs(newDY) > 2)
        	System.out.println("Excessive lateral speed: " + newDY);
        this.dy = newDY;
    }

    /**
     * Sets global x and y coordinates and heading. This may be in between two 
     * lanes in case of a lane change.
     */
    @Override
	public void setXY() {
        java.awt.geom.Point2D.Double coord = atLaneXY();
        if (lcVehicle != null) // interpolate between own and lcVehicle global X and Y
            global = new java.awt.geom.Point2D.Double(coord.x * (1 - lcProgress) + lcVehicle.global.x * lcProgress,
            		coord.y * (1 - lcProgress) + lcVehicle.global.y * lcProgress);
        else
        	global = new java.awt.geom.Point2D.Double(coord.x, coord.y);
        setHeading();
        if (lcVehicle != null)
            lcVehicle.heading = heading;
    }
    
    /**
     * Sets the heading of this vehicle based on lane curvature.
     */
    public void setHeading() {
        java.awt.geom.Point2D.Double p1 = getLane().XY(x);
        java.awt.geom.Point2D.Double p2;
        if ((x > l) || (null == rearLane)) {
            rearLane = getLane();
            p2 = getLane().XY (x - l);
        } else {
            // update rearLane
        	int attempt = 0;
            double xRear = rearLane.xAdj(getLane()) + x - l;
            while (xRear > rearLane.l) {
            	// FIXME
            	if (++attempt > 100)
            		return;
                if (null != rearLane.down)
                    rearLane = rearLane.down;
                else if (rearLane.isSplit()) {
                    for (int i = 0; i < rearLane.RSUcount(); i++) {
                        if (rearLane.getRSU(i) instanceof Lane.splitRSU) {
                            Lane.splitRSU split = (Lane.splitRSU) rearLane.getRSU(i);
                            Lane tmp = split.getLaneForRoute(route);
                            if (null != tmp) {
                                rearLane = tmp;
                                break;
                            }
                            rearLane = getLane(); // route has been updated before the rear passed a split
                        }
                    }
                }
                xRear = rearLane.xAdj (getLane()) + x - l;
            }
            p2 = rearLane.XY (xRear);
        }
        double xx = p1.x - p2.x;
        double yy = p1.y - p2.y;
        // Normalize
        double f = Math.sqrt(xx * xx + yy * yy);
        heading = new java.awt.geom.Point2D.Double(xx / f, yy / f);
    }

    /**
     * Returns the maximum vehicle speed in m/s.
     * @return Maximum vehicle speed [m/s].
     */
    public double getVMax() {
        return vMax / 3.6;
    }

    /**
     * Correlates some parameters to other stochastic parameters. By default
     * this method is empty. Sub classes can define content for this method. The
     * method will be called after the stochastic parameters are set.
     */
    public void correlateParameters() {}

    /**
     * Returns whether the vehicle is equipped with an OBU.
     * @return Whether the car is equipped.
     */
    public boolean isEquipped() {
        return OBU != null;
    }
    
    /**
     * Returns the driver of this vehicle.
     * @return Driver of this vehicle.
     */
    @Override
	public Driver getDriver() {
        return driver;
    }
    
    @Override
	public String toString() {
    	String location = "null";
    	if (null != global)
    		location = String.format(Main.locale, "%.3f,%.3f", global.x, global.y);
    	return String.format (Main.locale, "%d at (%s), route %s", id, location, route.toString());
    }
    
    /**
     * Retrieve the length of this Vehicle
     * @return String; the length of this Vehicle
     */
    public String getLength_r() {
    	return String.format (Main.locale, "%.2f m", l);
    }
    
    /**
     * Retrieve the speed of this Vehicle
     * @return String; the speed of this Vehicle
     */
    public String getSpeed_r() {
    	return String.format (Main.locale, "%.2f km/h", v * 3.6);
    }
    
    /**
     * Retrieve the acceleration of this Vehicle.
     * @return String; the acceleration of this vehicle
     */
    public String getAcceleration_r() {
    	return String.format (Main.locale, "%.2f m/s/s", a);
    }
    
    /**
     * Retrieve the maximum acceleration of this Vehicle
     * @return String; the maximum acceleration of this Vehicle
     */
    public String getMaximumDeceleration_r () {
    	return String.format (Main.locale, "%.2f m/s/s", aMin);
    }
    
    /**
     * Retrieve the {@link Driver} of this Vehicle.
     * @return {@link Driver}; the Driver of this Vehicle 
     */
    public Driver getDriver_r() {
    	return driver;
    }
    
    /**
     * Retrieve the maximum speed of this Vehicle.
     * @return String; the maximum speed of this Vehicle
     */
    public String getMaximumSpeed_r() {
    	return String.format(Main.locale, "%.2f km/h", vMax);
    }
    
    /**
     * Retrieve the list of {@link RSU RSUs} of this Vehicle.
     * @return ArrayList&lt;{@link RSU}&gt;; the list of {@link RSU RSUs} of this Vehicle
     */
    public java.util.ArrayList<RSU> getRSUsInRange_r() {
    	return RSUsInRange;
    }
    
    /**
     * Retrieve the {@link LCVehicle} of this Vehicle.
     * @return {@link LCVehicle}; the LCVehicle of this Vehicle (may be null)
     */
    public LCVehicle getLCVehicle_r() {
    	return lcVehicle;
    }
    
    /**
     * Make the route visible for the ObjectInspector.
     * @return {@link Route}
     */
    public Route getRoute_r() {
    	return route;
    }

	@Override
	public void paint(double when, GraphicsPanel graphicsPanel) {
		if ((! getLane().isVisible()) || ((null != rearLane) && (! rearLane.isVisible())))
			return;
        graphicsPanel.setColor(Color.BLACK);
        graphicsPanel.setStroke(0.1f);
        graphicsPanel.drawPolyLine(outline(when), true);
        Point2D.Double[] outline = outline(when);
		graphicsPanel.setColor(Color.RED);
        graphicsPanel.setStroke(0f);
        graphicsPanel.drawPolygon(outline);
	}

	@Override
	public Double[] outline(double when) {
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

	@Override
	public Point2D.Double center(double when) {
		return global;
	}

	/**
	 * Change behavior when passing a VMS
	 * @param message String; the message read
	 * @param vms VMS; the Variable Message Sign
	 */
	public void passVMS(String message, VMS vms) {
    	System.out.println("Vehicle " + toString() + " reads VMS displaying \"" + message + "\"");
		// TODO Let something happen depending on the actual message
	}

}