package nl.tudelft.otsim.Simulators.LaneSimulator;

/**
 * This class has the common functionality of regular vehicles and temporary
 * lane change vehicles. This is the position on the network and relative to
 * neighbouring movables. Common methods are related to position, neighbours and
 * visualisation.
 */

public abstract class Movable  {

    /** Main model. */
    public Model model;

    /** Lane where the movable is at. */
    public Lane lane;

    /** Position on the lane. */
    public double x;

    /** Speed of the movable [m/s]. */
    public double v;
    
    /** Acceleration of the movable [m/s^2]. */
    public double a;

    /** Movable length [m]. */
    public double l;

    /** Global x coordinate. */
    public double globalX;

    /** Global y coordinate. */
    public double globalY;
    
    /** Normalized heading of the vehicle. */
    public java.awt.geom.Point2D.Double heading = new java.awt.geom.Point2D.Double();

    /** Upstream movable, if any */
    public Movable up;

    /** Downstream movable, if any */
    public Movable down;

    /** Left upstream movable, if any */
    public Movable leftUp;

    /** Left downstream movable, if any */
    public Movable leftDown;

    /** Right upstream movable, if any */
    public Movable rightUp;

    /** Right downstream movable, if any */
    public Movable rightDown;

    /** Marker string for Matlab. */
    public java.lang.String marker;

    /** Matlab handle(s), which are simply double values. */
    public double[] handle;

    /** Left indicator on. */
    public boolean leftIndicator = false;

    /** Right indicator on. */
    public boolean rightIndicator = false;

    /** Boolean which is used for pointer book-keeping. */
    protected boolean justExceededLane = false;
    
    /**
     * Constructor that sets the main model.
     * @param model Main model.
     */
    public Movable(Model model) {
        this.model = model;
    }

    /** 
     * Finds neighbours in adjacent lanes or checks for overtaking. This will
     * make sure that the adjacent neighbours are correct.
     */
    public void updateNeighbours() {
        double xAdj;
        // left neighbours
        if (lane.left!=null) {
            xAdj = getAdjacentX(Model.latDirection.LEFT);
            /* Remove adjacent neighbour in case of split, as this vehicle may
             * split the other way and become untreatable for regular overtake 
             * checks. Instead, refind the vehicle if appropriate.
             */ 
            if (leftUp!=null && leftUp.lane.downSplit!=lane.left.downSplit) {
                leftUp = null;
            }
            // update current neighbour
            if (leftUp==null) {
                // if there is no leftUp, find it
                leftUp = lane.left.findVehicle(xAdj, Model.longDirection.UP);
            } else {
                // if there is a leftUp, check overtake
                Lane adjlane = lane.left;
                // search upstream while neighbour is downstream
                while (leftUp!=null && leftUp.x+adjlane.xAdj(leftUp.lane)>=xAdj) {
                    leftUp = leftUp.up;
                }
                // search downstream while neighbours down is upstream
                while (leftUp!=null && leftUp.down!=null &&
                        leftUp.down.x+adjlane.xAdj(leftUp.down.lane)<=xAdj) {
                    leftUp = leftUp.down;
                }
            }
            // find closest downstream vehicle
            if (leftUp!=null && leftUp.lane.downSplit==lane.left.downSplit) {
                // through upstream vehicle if no split in between
                leftDown = leftUp.down;
            } else {
                leftDown = lane.left.findVehicle(xAdj, Model.longDirection.DOWN);
                // one vehicle, at a perfectly equal coordinate, may be in between
                if (leftDown!=null && leftDown.up!=null && leftDown.up!=leftUp) {
                    leftDown = leftDown.up;
                }
            }
        } else {
            // no adjacent lane, no adjacent neighbours
            leftDown = null;
            leftUp = null;
        }

        // right neighbours
        if (lane.right!=null) {
            xAdj = getAdjacentX(Model.latDirection.RIGHT);
            /* Remove adjacent neighbour in case of split, as this vehicle may
             * split the other way and become untreatable for regular overtake 
             * checks. Instead, refind the vehicle if appropriate.
             */
            if (rightUp!=null && rightUp.lane.downSplit!=lane.right.downSplit) {
                rightUp = null;
            }
            // update current neighbour
            if (rightUp==null) {
                // if there is no rightUp, find it
                rightUp = lane.right.findVehicle(xAdj, Model.longDirection.UP);
            } else {
                // if there is a rightUp, check overtake
                Lane adjlane = lane.right;
                // search upstream while neighbour is downstream
                while (rightUp!=null && rightUp.x+adjlane.xAdj(rightUp.lane)>=xAdj) {
                    rightUp = rightUp.up;
                }
                // search downstream while neighbours down is upstream
                while (rightUp!=null && rightUp.down!=null &&
                        rightUp.down.x+adjlane.xAdj(rightUp.down.lane)<xAdj) {
                    rightUp = rightUp.down;
                }
            }
            // find closest downstream vehicle
            if (rightUp!=null && rightUp.lane.downSplit==lane.right.downSplit) {
                // through upstream vehicle if no split in between
                rightDown = rightUp.down;
            } else {
                rightDown = lane.right.findVehicle(xAdj, Model.longDirection.DOWN);
                // one vehicle, at a perfectly equal coordinate, may be in between
                if (rightDown!=null && rightDown.up!=null && rightDown.up!=rightUp) {
                    rightDown = rightDown.up;
                }
            }
        } else {
            // no adjacent lane, no adjacent neighbours
            rightDown = null;
            rightUp = null;
        }
    }

    /**
     * Sets this vehicle as adjacent neighbour of own adjacent neighbours. When
     * put on a lane a vehicle may be the adjacent neighbour for many vehicles
     * on adjacent lanes. This method checks if the adjacent neighbours of these
     * vehicles are either the <tt>up</tt> or <tt>down</tt> vehicle and adjusts 
     * that to this vehicle if so.
     */
    public void updateNeighboursInv() {
        // indirect pointers
        /* 
         * Vehicle A is put on the right lane. Then if vehicle B has C as
         * rightUp or vehicle B has no rightUp, A becomes the rightUp of B. This
         * procedure is continued for as long as B or vehicles downstream are in
         * any of these situations. Note that the use of isSameLane() prevents
         * that invalid pointers passed merges or splits are defined.
         * ---------    ---------    ---------
         *        B           B          |  B
         * ---------    ---------    ---------
         *  C  A           A           A |
         * ---------    ---------    ---------
         */
        Movable ld = leftDown;
        while (ld!=null) {
            if ((ld.rightUp==up && up!=null) ||
                    (ld.rightUp==null && ld.lane.right!=null && ld.lane.right.isSameLane(lane))) {
                ld.rightUp = this;
                ld = ld.down;
            } else {
                ld = null; // stop search
            }
        }
        // The other three mirrored principles
        Movable rd = rightDown;
        while (rd!=null) {
            if ((rd.leftUp==up && up!=null) || 
                    (rd.leftUp==null && rd.lane.left!=null && rd.lane.left.isSameLane(lane))) {
                rd.leftUp = this;
                rd = rd.down;
            } else {
                rd = null;
            }
        }
        Movable lu = leftUp;
        while (lu!=null) {
            if ((lu.rightDown==down && down!=null) || 
                    (lu.rightDown==null && lu.lane.right!=null && lu.lane.right.isSameLane(lane))) {
                lu.rightDown = this;
                lu = lu.up;
            } else {
                lu = null;
            }
        }
        Movable ru = rightUp;
        while (ru!=null) {
            if ((ru.leftDown==down && down!=null) || 
                    (ru.leftDown==null && ru.lane.left!=null && ru.lane.left.isSameLane(lane))) {
                ru.leftDown = this;
                ru = ru.up;
            } else {
                ru = null;
            }
        }

        // one-directional pointers
        /*
         * --------------------------
         *  C   A                    >>
         * -- . . . ----- . . . -----
         *   \      >>   \  B   >>
         *    ------      ------
         * Vehicle A is pasted onto the road. When vehicle B does not have a
         * leftUp or if it is C, A will become the leftUp of B. Search
         * downstream untill vehicles are found within the lane. This situation
         * also applies with up/downstream switched.
         */
        pasteAsAdjacentLeader(lane, Model.longDirection.DOWN);
        pasteAsAdjacentLeader(lane, Model.longDirection.UP);
    }

    /**
     * Sets the global x and y positions, as implemented by a subclass.
     */
    public abstract void setXY();
    
    /**
     * Returns the global x and y at the lane centre.
     * @return 2-element array with x and y at the lane.
     */
    public java.awt.geom.Point2D.Double atLaneXY() {
        return lane.XY(x);
    }

    /**
     * Returns the location on the adjacent lane, keeping lane length
     * difference in mind.
     * @param dir Defines direction. Use -1 for left lane, 1 for right lane.
     * @return X location on adjacent lane.
     */
    public double getAdjacentX(Model.latDirection dir) {
        return lane.getAdjacentX(x, dir);
    }

    /**
     * Abstract method to translate a vehicle.
     * @param dx Distance to be translated [m].
     */
    public abstract void translate(double dx);

    /**
     * Returns the net headway to a given vehicle. This vehicle should be on the
     * same or an adjacent lane, or anywhere up- or downstream of those lanes.
     * @param leader Leading vehicle, not necessarily the 'down' vehicle.
     * @return Net headway with leader [m].
     */
    public double getHeadway(Movable leader) {
        // Ignore leader which is on the other side of a merge but which came
        // from another lane. It should also be only partially past the conflict.
        // The conflict should deal with the situation.
        if (leader==down && leader.lane.upMerge!=null && 
                leader.lane.upMerge!=lane.upMerge &&
                !leader.lane.upMerge.mergeOrigin.isSameLane(lane) && 
                leader.lane.upMerge.xAdj(leader.lane)+leader.x<leader.l) {
            return Double.POSITIVE_INFINITY;
        }
        double s = 0;
        double xAdjTmp;
        if (lane==leader.lane) {
            // same lane
            s = leader.x - x;
        } else if (lane==leader.lane.left) {
            // leader is right
            s = leader.getAdjacentX(Model.latDirection.LEFT) - x;
        } else if (lane==leader.lane.right) {
            // leader is left
            s = leader.getAdjacentX(Model.latDirection.RIGHT) - x;
        } else if ((xAdjTmp=lane.xAdj(leader.lane))!=0) {
            // leader is up- or downstream
            s = leader.x + xAdjTmp - x;
        } else if ((xAdjTmp=lane.xAdj(leader.lane.left))!=0) {
            // leader is on right lane up- or downstream
            s = leader.getAdjacentX(Model.latDirection.LEFT) + xAdjTmp - x;
        } else if (lane.right!=null && (xAdjTmp=lane.right.xAdj(leader.lane))!=0) {
            // leader is on right lane up- or downstream (no up/down lane)
            s = leader.x + xAdjTmp - getAdjacentX(Model.latDirection.RIGHT);
        } else if ((xAdjTmp=lane.xAdj(leader.lane.right))!=0) {
            // leader is on left lane up- or downstream
            s = leader.getAdjacentX(Model.latDirection.RIGHT) + xAdjTmp - x;
        } else if (lane.left!=null && (xAdjTmp=lane.left.xAdj(leader.lane))!=0) {
            // leader is on left lane up- or downstream (no up/down lane)
            s = leader.x + xAdjTmp - getAdjacentX(Model.latDirection.LEFT);
        } else if (this instanceof Vehicle) {
            // leader may actually be a leader of the lane change vehicle
            /*
             * This happens for a neighbour of an lcVehicle as:
             * ------------
             *          A
             * ------------
             *     B
             * ------------
             *     C
             * ------------
             * Vehicle A wants the acceleration B->A so it won't cut B off. The
             * acceleration is calculated by the driver of vehicle B. However, B
             * is a lane change vehicle (of C) and has no driver.
             * "B.getDriver()" returns the driver of vehicle C. That driver then
             * needs a headway between it's vehicle (C) and A. This will not be
             * found and so the headway between B and A will be needed.
             */
            Vehicle veh = (Vehicle) this;
            if (veh.lcVehicle==null) {
                // give warning as vehicles are not adjacent
                System.err.println("Headway not found from lanes: "+x+"@"+lane.id+"->"+leader.x+"@"+leader.lane.id+", returning Inf");
                s = Double.POSITIVE_INFINITY;
            } else {
                s = veh.lcVehicle.getHeadway(leader);
            }
        }
        s = s-leader.l; // gross -> net
        return s;
    }
    
    /**
     * Returns the distance between a vehicle and a RSU.
     * @param rsu RSU.
     * @return Distance [m] between vehicle and RSU.
     */
    public double getDistanceToRSU(RSU rsu) {
        return rsu.x + lane.xAdj(rsu.lane) - x;
    }

    /**
     * Deletes a vehicle entirerly while taking care of any neighbour reference
     * to the vehicle.
     */
    public void delete() {
        /* When deleting a vehicle, all pointers to it need to be removed in
         * order for the garbage collector to remove the object from memory.
         * Vehicles are referenced from: model, lane, OBU, driver, trajectory,
         * lcVehicle<->vehicle and neighbouring vehicles.
         */
        
        // remove from lane and neighbours
        cut();

        // remove from various objects
        if (this instanceof Vehicle) {
            Vehicle veh = (Vehicle) this;
            // lcVehicle
            if (veh.lcVehicle!=null) {
                veh.lcVehicle.delete(); // will be removed from memory
                veh.lcVehicle = null;
            }
            // model
            model.removeVehicle(veh);
            // trajectory
            if (veh.trajectory!=null) {
                veh.trajectory.vehicle = null; // data remains, vehicle does not
                veh.trajectory = null;
            }
            // OBU
            if (veh.isEquipped()) {
                veh.OBU.delete(); // should remove pointers set in constructor
                veh.OBU.vehicle = null;
                veh.OBU = null; // OBU will be removed from memory
            }
            // delete storages of driver
            veh.driver.accelerations = null;
            veh.driver.antFromLeft = null;
            veh.driver.antFromRight = null;
            veh.driver.antInLane = null;
            // driver
            veh.driver.vehicle = null;
            veh.driver = null; // driver will be removed from memory
        } else if (this instanceof LCVehicle) {
            LCVehicle veh = (LCVehicle) this;
            // model
            model.removeVehicle(veh);
            // vehicle
            veh.vehicle.lcVehicle = null;
            veh.vehicle = null;
        }
    }

    /**
     * Cuts a vehicle from a lane. All pointers from the lane and neighbours
     * to this vehicle are updated or removed.
     */
    public void cut() {

        // remove from lane vector
        lane.vehicles.remove(this);

        /*
         * All pointers from neighbours need to be updated or removed. Finding
         * all vehicles with pointers to this could be performed in two ways:
         *   1) loop all vehicles in the model
         *   2) start at own neighbours and search
         * For efficiency, the 2nd option is chosen. This can become a bit
         * complex as pointers can be:
         *   1) Asymmetric: vehicles have pointers to each other but not as each
         *      others opposites. This can happen especially for a lane changing
         *      vehicle as the lcVehicle is exactly adjacent. As a result the
         *      vehicles are up- or downstream depending on double precision
         *      evaluation of '<', '<=', '=>' and '>'. This can be different
         *      when evaluating from one to the other or the other way around.
         *      Asymmetric pointers can be found by looking one vehicle up- or
         *      downstream. More is not needed as these pointers only occur for
         *      practically adjacent vehicles.
         *   2) Indirect: vehicle 1 has a pointer to vehicle 2 but vehicle 2
         *      does not have a pointer to vehicle 1. However, the two vehicles
         *      are still indirectly connected. For example:
         *        --------------------
         *         A             G
         *        --------------------
         *          B  C  D  E  F   H
         *        --------------------
         *      where vehicles B-F have G as leftDown but G only has F as
         *      rightUp. This situation is solved by moving upstream from
         *      vehicle F while the leftDown vehicle is G.
         *   3) One-directional: This is a pointer where one vehicle lacks a
         *      certain neighbour that would make it indirect. These situations
         *      are discussed below.
         */

        // reset pointers in own lane
        if (up!=null && up.down==this) {
            up.down = down;
        }
        if (down!=null && down.up==this) {
            down.up = up;
        }
        /* 
         * Own lane pointers are one-directional for the last vehicle on a 
         * split, or the first vehicle on a merge. Loop all split or merge 
         * lanes, find the nearest vehicle and update pointers.
         */
        if (down==null && lane.downSplit!=null) {
            java.util.ArrayList<Movable> downs = findVehiclesDownstreamOfSplit(lane);
            for (Movable d : downs) {
                if (d.up==this) {
                    d.up = up;
                }
            }
        }
        if (up==null && lane.upMerge!=null) {
            java.util.ArrayList<Movable> ups = findVehiclesUpstreamOfMerge(lane);
            for (Movable d : ups) {
                if (d.down==this) {
                    d.down = down;
                }
            }
         }

        // reset pointers in adjacent lanes
        // left down
        Movable ld = leftDown;
        if (ld!=null && ld.rightDown==this) {
            ld.rightDown = down; // asymmetric pointer
            ld = ld.down;
        }
        while (ld!=null && ld.rightUp==this) {
            ld.rightUp = up; // indirect pointer
            ld = ld.down;
        }
        // left up
        Movable lu = leftUp;
        if (lu!=null && lu.rightUp==this) {
            lu.rightUp = up; // asymmetric pointer
            lu = lu.up;
        }
        while (lu!=null && lu.rightDown==this) {
            lu.rightDown = down; // indirect pointer
            lu = lu.up;
        }
        // right down
        Movable rd = rightDown;
        if (rd!=null && rd.leftDown==this) {
            rd.leftDown = down; // asymmetric pointer
            rd = rd.down;
        }
        while (rd!=null && rd.leftUp==this) {
            rd.leftUp = up; // indirect pointer
            rd = rd.down;
        }
        // right up
        Movable ru = rightUp;
        if (ru!=null && ru.leftUp==this) {
            ru.leftUp = up; // asymmetric pointer
            ru = ru.up;
        }
        while (ru!=null && ru.leftDown==this) {
            ru.leftDown = down; // indirect pointer
            ru = ru.up;
        }

        // one-directional pointers
        /*
         * --------------------------
         *      A                    >>
         * -- . . . ----- . . . -----
         *   \      >>   \  B   >>
         *    ------      ------
         * Vehicle B has A as leftUp, but A has no rightDown. When A is cut, B
         * (and any downstream) needs to be found by moving downstream from A
         * as long as there are no vehicles and then check vehicles on adjacent
         * lanes.
         */
        cutAsAdjacentLeader(lane, Model.longDirection.DOWN);
        cutAsAdjacentLeader(lane, Model.longDirection.UP);
        
        /*
         * -----------
         *            \    A
         * . . . . . . -----------
         *      C        B        >>
         * -----------------------
         * Vehicle A will be deleted, vehicle C has A as leftDown but vehicle B
         * does not anymore because there is no adjacent lane. This can only
         * happen as vehicles move beyond the end of the lane. Therefore only an
         * upstream search is usefull.
         */
        if (x>lane.l) {
            if (lane.right!=null) {
                Movable veh = lane.right.findVehicle(lane.right.l, Model.longDirection.UP);
                while (veh!=null && veh.leftDown==this) {
                    veh.leftDown = null;
                    veh = veh.up;
                }
            }
            if (lane.left!=null) {
                Movable veh = lane.left.findVehicle(lane.left.l, Model.longDirection.UP);
                while (veh!=null && veh.rightDown==this) {
                    veh.rightDown = null;
                    veh = veh.up;
                }
            }
        }

        // check connection consistency (debug)
        if (model.debug) {
            model.checkForRemainingPointers(this);
        }

        // delete own references
        up = null;
        down = null;
        leftUp = null;
        leftDown = null;
        rightUp = null;
        rightDown = null;
    }
    
    /**
     * Returns a set of vehicles that is upstream of this, or an upstream merge.
     * In case no vehicle is found upstream of one of the merge lanes, any 
     * further merge is used to continue searching.
     * @param merge Lane to look for vehicles upstream of.
     * @return Set of vehicles that is upstream of this, or an upstream merge.
     */
    protected java.util.ArrayList<Movable> findVehiclesUpstreamOfMerge(Lane merge) {
        java.util.ArrayList<Movable> out = new java.util.ArrayList<Movable>();
        for (Lane j : merge.upMerge.ups) {
        	if (j.marked)
        		continue;
        	j.marked = true;
            Movable d = j.findVehicle(j.l, Model.longDirection.UP);
            if (d!=null) {
                out.add(d);
            } else if (j.upMerge!=null) {
                out.addAll(findVehiclesUpstreamOfMerge(j));
            }
            j.marked = false;
        }
        return out;
    }
    
    /**
     * Returns a set of vehicles that is downstream of this, or a downstream 
     * split. In case no vehicle is found downstream of one of the split lanes,
     * any further split is used to continue searching.
     * @param split Lane to look for vehicles upstream of.
     * @return Set of vehicles that is downstream of this, or a downstream split.
     */
    protected java.util.ArrayList<Movable> findVehiclesDownstreamOfSplit(Lane split) {
        java.util.ArrayList<Movable> out = new java.util.ArrayList<Movable>();
        for (Lane j : split.downSplit.downs) {
        	if (j.marked)
        		continue;
        	j.marked = true;
            Movable d = j.findVehicle(0, Model.longDirection.DOWN);
            if (d!=null) {
                out.add(d);
            } else if (j.downSplit!=null) {
                out.addAll(findVehiclesDownstreamOfSplit(j));
            }
            j.marked = false;
        }
        return out;
    }
    
    /**
     * Removes movable as adjacent leader for vehicles on up- or downstream 
     * lanes including merging and splitting lanes.
     * @param k Lane to move up- or downstream from.
     * @param lon Longitudinal direction.
     */
    protected void cutAsAdjacentLeader(Lane k, Model.longDirection lon) {
        if (Model.longDirection.DOWN==lon) {
            for (Lane n : k.downs) {
                cutAsAdjacentLeader0(n, lon);
            }
            if (k.down!=null) {
                cutAsAdjacentLeader0(k.down, lon);
            }
        } else {
            for (Lane n : k.ups) {
                cutAsAdjacentLeader0(n, lon);
            }
            if (k.up!=null) {
                cutAsAdjacentLeader0(k.up, lon);
            }
        }
    }
    
    /**
     * Removes movable as adjacent leader on adjacent lanes and continues an up-
     * or downstream movement if required.
     * @param k Current lane to look left and right from.
     * @param lon Longitudinal direction.
     */
    protected void cutAsAdjacentLeader0(Lane k, Model.longDirection lon) {
        Movable m = null;
        if (Model.longDirection.DOWN==lon) {
            // look downstream
            if (k.right!=null) { // look right
                m = k.right.findVehicle(0, Model.longDirection.DOWN);
                if (m!=null && m.leftUp!=this) {
                    m = m.down; // most upstream vehicle may form a one-directional pair as it may have just moved onto the lane
                }
                while (m!=null && m.leftUp==this) {
                    m.leftUp = up;
                    m = m.down; // next vehicle may form a one-directional pair
                }
            }
            if (k.left!=null) { // look left
                m = k.left.findVehicle(0, Model.longDirection.DOWN);
                if (m!=null && m.rightUp!=this) {
                    m = m.down; // most upstream vehicle may form a one-directional pair as it may have just moved onto the lane
                }
                while (m!=null && m.rightUp==this) {
                    m.rightUp = up;
                    m = m.down; // next vehicle may form a one-directional pair
                }
            }
            if (k.vehicles.isEmpty()) {
                // no vehicles encountered, move on
            	if (k.marked)
            		return;
            	k.marked = true;
                cutAsAdjacentLeader(k, lon);
                k.marked = false;
                // else stop: further vehicles have vehicles at k as neighbour
            }
        } else {
            // look upstream
            if (k.right!=null) { // look right
                m = k.right.findVehicle(k.right.l, Model.longDirection.UP);
                while (m!=null && m.leftDown==this) {
                    m.leftDown = down;
                    m = m.up; // next vehicle may form a one-directional pair
                }
            }
            if (k.left!=null) { // look left
                m = k.left.findVehicle(k.left.l, Model.longDirection.UP);
                while (m!=null && m.rightDown==this) {
                    m.rightDown = down;
                    m = m.up; // next vehicle may form a one-directional pair
                }
            }
            if (k.vehicles.isEmpty()) {
                // no vehicles encountered, move on
            	if (k.marked)
            		return;
            	k.marked = true;
                cutAsAdjacentLeader(k, lon);
                k.marked = false;
            } else {
                // check if there is any vehicle that has not just exceeded its lane
                int i = 0;
                while (k!=null && i<k.vehicles.size()) {
                    if (!k.vehicles.get(i).justExceededLane) {
                        /* 
                         * A vehicle was found that has been there for at least
                         * one time step. This vehicle is the neighbour of any
                         * further upstream vehicles. The search can be stopped.
                         */ 
                        k = null;
                        break;
                    }
                    i++;
                }
                if (k!=null) {
                    /*
                     * Vehicle(s) were found, but they all just exceeded its 
                     * lane. Search needs to be continued as adjacent vehicles
                     * may still have pointers to this movable which is cut.
                     */
                    cutAsAdjacentLeader(k, lon);
                }
            }
        }
    }
    
    /**
     * Sets movable as adjacent leader for vehicles on up- or downstream 
     * lanes including merging and splitting lanes.
     * @param k Lane to move up- or downstream from.
     * @param lon Longitudinal direction.
     */
    protected void pasteAsAdjacentLeader(Lane k, Model.longDirection lon) {
        if (Model.longDirection.DOWN==lon) {
            for (Lane n : k.downs) {
                pasteAsAdjacentLeader0(n, lon);
            }
            if (k.down!=null) {
                pasteAsAdjacentLeader0(k.down, lon);
            }
        } else {
            for (Lane n : k.ups) {
                pasteAsAdjacentLeader0(n, lon);
            }
            if (k.up!=null) {
                pasteAsAdjacentLeader0(k.up, lon);
            }
        }
    }
    
    /**
     * Sets movable as adjacent leader on adjacent lanes and continues an up-
     * or downstream movement if required.
     * @param k Current lane to look left and right from.
     * @param lon Longitudinal direction.
     */
    protected void pasteAsAdjacentLeader0(Lane k, Model.longDirection lon) {
        Movable m = null;
        if (Model.longDirection.DOWN==lon) {
            // look downstream
            if (k.right!=null) { // look right
                m = k.right.findVehicle(0, Model.longDirection.DOWN);
                while (m!=null && m.leftUp==up && m.leftUp!=null) {
                    m.leftUp = this;
                    m = m.down; // next vehicle may form a one-directional pair
                }
            }
            if (k.left!=null) { // look left
                m = k.left.findVehicle(0, Model.longDirection.DOWN);
                while (m!=null && m.rightUp==up && m.rightUp!=null) {
                    m.rightUp = this;
                    m = m.down;
                }
            }
            if (k.vehicles.isEmpty()) {
            	if (k.marked)
            		return;
            	k.marked = true;
                pasteAsAdjacentLeader(k, lon); // look downstream
                k.marked = false;
            } else {
                k = null; // stop; further vehicles have vehicles at k as neighbour
            }
        } else {
            // look upstream
            if (k.right!=null) { // look right
                m = k.right.findVehicle(k.right.l, Model.longDirection.UP);
                while (m!=null && m.leftDown==down && m.leftDown!=null) {
                    m.leftDown = this;
                    m = m.up; // next vehicle may form a one-directional pair
                }
            }
            if (k.left!=null) { // look left
                m = k.left.findVehicle(k.left.l, Model.longDirection.UP);
                while (m!=null && m.rightDown==down && m.rightDown!=null) {
                    m.rightDown = this;
                    m = m.up; // next vehicle may form a one-directional pair
                }
            }
            if (k.vehicles.isEmpty()) {
            	if (k.marked)
            		return;
            	k.marked = true;
                pasteAsAdjacentLeader(k, lon); // look upstream
                k.marked = false;
            } else {
                k = null; // stop: further vehicles have vehicles at k as neighbour
            }
        }
    }

    /**
     * Places a vehicle on a lane, sets new neighbours and sets this vehicle as
     * neighbour of surrounding vehicles.
     * @param atLane Lane where the vehicle needs to be placed at.
     * @param atX Location where the vehicle needs to be placed at.
     */
    public void paste(Lane atLane, double atX) {
        // In case the lane is exceeded, change the lane to search on. This
        // could occur when searching for neighbours when ending a lane change
        // within the same time step a lane is exceeded.
        if (atX>atLane.l && atLane.down!=null) {
            paste(atLane.down, atX-atLane.l);
            return;
        }
        // find up/down neighbours
        up = atLane.findVehicle(atX, Model.longDirection.UP);
        if (up!=null) {
            down = up.down; // in between
            if (up.down==null && up.lane.downSplit!=atLane.downSplit) {
                // just passed split, so up has no down (as this was cut)
                down = atLane.findVehicle(atX, Model.longDirection.DOWN);
            } else {
                // same lane, up has this as down
                up.down = this;
            }
        } else {
            down = atLane.findVehicle(atX, Model.longDirection.DOWN);
        }
        if (down!=null && down.lane.upMerge==atLane.upMerge) {
            // same lane, down has this as up
            down.up = this;
        }
        // set properties
        lane = atLane;
        x = atX;
        // Set pointers to this of vehicles at other side of split or merge.
        if (lane.upMerge!=null && up==null) {
            java.util.ArrayList<Movable> ups = findVehiclesUpstreamOfMerge(lane);
            for (Movable d : ups) {
                if ((d.down==null || d.down==down) && 
                        (d.lane.downSplit==null || d.lane.downSplit==lane.downSplit)) {
                    d.down = this;
                }
            }
        }
        if (lane.downSplit!=null && down==null) {
            java.util.ArrayList<Movable> downs = findVehiclesDownstreamOfSplit(lane);
            for (Movable d : downs) {
                if ((d.up==null || d.up==up) && 
                        (d.lane.upMerge==null || d.lane.upMerge==lane.upMerge)) {
                    d.up = this;
                }
            }
        }
        // add to lane vector
        atLane.vehicles.add(this);
        // set adjacent neighbours
        updateNeighbours();
        // set adjacent neighbours' new neighbours (this vehicle)
        updateNeighboursInv();
    }
    
    /**
     * Returns the driver of any movable.
     * @return Driver of the movable.
     */
    public abstract Driver getDriver();

    public Lane getLane_r() {
    	return lane;
    }
    
}