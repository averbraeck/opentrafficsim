package nl.tudelft.otsim.Simulators.LaneSimulator;

import nl.tudelft.otsim.GUI.Main;

/**
 * A single lane stretch of road. Different lanes are connected in the
 * longitudinal or lateral direction. The <tt>jLane</tt> object also provides a
 * few network utilities to find vehicles and to get the longitudinal distance
 * between lanes.<br>
 * <br>
 * In case one lane should split into multiple lanes or multiple lanes should 
 * merge into one, leave the <tt>down</tt> or <tt>up</tt> field at <tt>null</tt> 
 * and use <tt>addSplitLane</tt> and <tt>addMergeLane</tt> instead. Also, do not
 * use <tt>conntectLong</tt>.
 */
public class Lane {
	
	boolean marked;

    /** Array of x-coordinates defining the lane curvature. */
    public double[] x;

    /** Array of y-coordinates defining the lane curvature. */
    public double[] y;

    /** Length of the lane [m]. */
    public double l;

    /** Main model. */
    public Model model;

    //public jModel getModel() {
	//	return model;
	//}

	//public void setModel(jModel model) {
	//	this.model = model;
	//}

	/** ID of lane for user recognition. */
    protected int id;

    /** Downstream lane that is a taper (if any). */
    public Lane taper;

    /** Upstream lane (if any). */
    public Lane up;
    
    /** Set of upstream lanes in case of a merge. */
    public java.util.ArrayList<Lane> ups = new java.util.ArrayList<Lane>();

    /** Downstream lane (if any). */
    public Lane down;
    
    /** Set of downstream lanes in case of a split. */
    public java.util.ArrayList<Lane> downs = new java.util.ArrayList<Lane>();

    /** Left lane (if any). */
    public Lane left;

    /** Right lane (if any). */
    public Lane right;

    /** Whether one can change to the left lane. */
    public boolean goLeft;

    /** Whether one can change to the right lane. */
    public boolean goRight;

    /** Set of RSUs, ordered by position. */
    protected java.util.ArrayList<RSU> RSUs = new java.util.ArrayList<RSU>();

    /** All movables on this lane, in no particular order. */
    public java.util.ArrayList<Movable> vehicles = new java.util.ArrayList<Movable>(0);

    /** Destination number, -999 GT if no destination. */
    public int destination;

    /** Destination number, -999 GT if no destination. */
    public int origin;

    /** Legal speed limit [km/h]. */
    public double vLim = 120;

    /**
     * Number of lane changes to be performed from this lane towards a certain
     * destination number. This is automatically filled with the model
     * initialization.
     */
    public java.util.HashMap<Integer, Integer> lanechanges = new java.util.HashMap<Integer, Integer>();

    /**
     * Distance [m] in which lane changes have to be performed towards a certain
     * destination number. This is automatically filled with the model
     * initialization.
     */
    public java.util.HashMap<Integer, Double> endpoints = new java.util.HashMap<Integer, Double>();

    /** Vehicle generator, if any. */
    public Generator generator;

    /**
     * Set of calculated x adjustments with longitudinally linked lanes. These
     * will be calculated and stored as needed.
     */
    protected java.util.HashMap<Integer, Double> xAdjust = new java.util.HashMap<Integer, Double>();
    
    /**
     * First downstream splitting lane. This is used for neighbor bookkeeping 
     * where pointers past a split from downstream are invalid (and thus removed).
     */
    public Lane downSplit;
    
    /**
     * First upstream merging lane. This is used for neighbor bookkeeping 
     * where pointers past a merge from upstream are invalid (and thus removed).
     */
    public Lane upMerge;
    
    /** 
     * Lane from which a vehicle entered the upstream side of a merge lane. This
     * is used to determine whether a vehicle upstream of a merge should follow
     * its leader downstream of a merge. If that vehicle came from the other
     * direction of the merge, it should not be followed.
     */
    public Lane mergeOrigin;
    
    /**
     * Constructor that will calculate the lane length from the x and y
     * coordinates.
     * @param x X coordinates of curvature.
     * @param y Y coordinates of curvature.
     * @param id User recognizable lane id.
     * @param model Main model.
     */
    public Lane(Model model, double[] x, double[] y, int id) {
        this.model = model;
        this.x = x;
        this.y = y;
        this.id = id;
        calculateLength();
    }
    
    public Lane(double[] x, double[] y, int id) {
        this.x = x;
        this.y = y;
        this.id = id;
        calculateLength();
    }   
    /**
     * Sets the lane length based on the x and y coordinates. This method is 
     * called within the constructor and should only be used if coordinates are
     * changed afterwards (for instance to nicely connect lanes at the same 
     * point).
     */
    public void calculateLength() {
        // compute and set length
        double cumLength = 0;
        double dx;
        double dy;
        for (int i=1; i<=x.length-1; i++) {
            dx = this.x[i]-this.x[i-1];
            dy = this.y[i]-this.y[i-1];
            cumLength = cumLength + Math.sqrt(dx*dx + dy*dy);
        }
        l = cumLength;
    }

    /**
     * Initializes lane change info, taper/merge/split presence, vehicle 
     * generation, RSUs.
     */
    @SuppressWarnings("unused")
	public void init() {
    	System.out.println("init lane " + id);
    	if (19 == id)
    		System.out.println("opletten");
        // Merge
        if (isMerge())
            setUpstreamMerge(this);
        // Split
        if (isSplit()) {
            setDownstreamSplit(this);
            // Add split RSU at splitting lanes
            boolean hasSplit = false;
            for (RSU rsu : RSUs) {
                if (rsu instanceof splitRSU)
                    hasSplit = true;
            }
            if (!hasSplit)
                new splitRSU();		// only if no split present
        }
        // Taper
        if (taper==this) {
            Lane upLane = up;
            while (upLane!=null && upLane.left!=null) {
                upLane.taper = this;
                upLane = upLane.up;
            }
        }
        // Lane change info
        initLaneChangeInfo();
        // Generator
        if (generator!=null)
            generator.init();
        // RSUs
        for (int i=0; i<RSUs.size(); i++)
            RSUs.get(i).init();
    }
    
    /**
     * Sets the downstream split to the given lane for this and upstream lanes, 
     * also past a merge, untill the next split.
     * @param split Splitting lane.
     */
    protected void setDownstreamSplit(Lane split) {
        downSplit = split;
        if (up!=null && !up.isSplit())
            up.setDownstreamSplit(split);
        for (Lane j : ups) {
            if (!j.isSplit())
                j.setDownstreamSplit(split);
        }
    }
    
    /**
     * Sets the upstream merge to the given lane for this and downstream lanes, 
     * also past a split, untill the next merge.
     * @param split Merging lane.
     */
    protected void setUpstreamMerge(Lane merge) {
        upMerge = merge;
        if (down!=null && !down.isMerge()) {
            down.setUpstreamMerge(merge);
        }
        for (Lane j : downs) {
            if (!j.isMerge()) {
                j.setUpstreamMerge(merge);
            }
        }
    }
    
    /**
     * Initializes the lane change info throughout the network for a possible 
     * destination of this lane.
     */
    public void initLaneChangeInfo() {
        if (destination>0 && !leadsTo(destination)) {
            // find all lanes in cross section with same destination and set initial info
            java.util.ArrayList<Lane> curlanes = new java.util.ArrayList<Lane>();
            curlanes.add(this);
            lanechanges.put(destination, 0);
            endpoints.put(destination, l);
            Lane lane = left;
            while (lane!=null && lane.destination==destination) {
                curlanes.add(lane);
                lane.lanechanges.put(destination, 0);
                lane.endpoints.put(destination, lane.l);
                lane = lane.left;
            }
            lane = right;
            while (lane!=null && lane.destination==destination) {
                curlanes.add(lane);
                lane.lanechanges.put(destination, 0);
                lane.endpoints.put(destination, lane.l);
                lane = lane.right;
            }
            // move through network and set lane change information
            while (!curlanes.isEmpty()) {
                // move left
                int n = curlanes.size();
                for (int i=0; i<n; i++) {
                    Lane curlane = curlanes.get(i);
                    int lcs = 0;
                    while (curlane.left!=null && curlane.left.goRight && 
                            !curlanes.contains(curlane.left) &&
                            !curlane.left.lanechanges.containsKey(destination)) {
                        // left lane is not in current set and has not been covered yet
                        lcs = lcs+1; // additional lane change required
                        curlanes.add(curlane.left); // add to current set
                        curlane.left.lanechanges.put(destination, lcs); // set # of lane changes
                        curlane.left.endpoints.put(destination, curlane.left.l);
                        curlane = curlane.left; // next left lane
                    }
                }
                // move right
                for (int i=0; i<n; i++) {
                    Lane curlane = curlanes.get(i);
                    int lcs = 0;
                    while (curlane.right!=null && curlane.right.goLeft && 
                            !curlanes.contains(curlane.right) &&
                            !curlane.right.lanechanges.containsKey(destination)) {
                        // right lane is not in current set and has not been covered yet
                        lcs = lcs+1; // additional lane change required
                        curlanes.add(curlane.right); // add to current set
                        curlane.right.lanechanges.put(destination, lcs); // set # of lane changes
                        curlane.right.endpoints.put(destination, curlane.right.l);
                        curlane = curlane.right; // next right lane
                    }
                }
                // move upstream
                java.util.ArrayList<Lane> uplanes = new java.util.ArrayList<Lane>();
                for (int i=0; i<curlanes.size(); i++) {
                    Lane curlane = curlanes.get(i);
                    if (curlane.up!=null && (!curlane.up.lanechanges.containsKey(destination) 
                            || curlane.up.lanechanges.get(destination)>/*=*/curlane.lanechanges.get(destination)) ) {
                        // upstream lane is not covered yet or 
                        // can be used with less lane changes or 
                        // can be used with more remaining space 
                        //  (equal number of lane changes, but now coming from downstream)
                        uplanes.add(curlane.up); // add to uplanes
                        // copy number of lane changes
                        curlane.up.lanechanges.put(destination, curlane.lanechanges.get(destination));
                        // increase with own length
                        curlane.up.endpoints.put(destination, curlane.endpoints.get(destination)+curlane.up.l);
                    } //else if (curlane.isMerge()) {
                        for (Lane j : curlane.ups) {
                            uplanes.add(j);
                            j.lanechanges.put(destination, curlane.lanechanges.get(destination));
                            j.endpoints.put(destination, curlane.endpoints.get(destination)+j.l);
                        }
                    //}
                }
                // set curlanes for next loop
                curlanes = uplanes;
            }
        }
    }

    /**
     * Add RSU to lane. RSUs are ordered by position.
     * @param rsu
     */
    public void addRSU(RSU rsu) {
        // order of RSUs is maintained
        int index = 0;
        if (!RSUs.isEmpty()) {
            if (rsu.x <= RSUs.get(0).x) {
                // RSU before first
                index = 0;
            } else if (RSUs.get(RSUs.size()-1).x <= rsu.x) {
                // RSU after last
                index = RSUs.size();
            } else {
                // RSU in between
                for (int i=0; i<RSUs.size()-1; i++) {
                    if (RSUs.get(i).x<= rsu.x && rsu.x <= RSUs.get(i+1).x) {
                        index = i+1;
                        i = RSUs.size(); // stop loop
                    }
                }
            }
        }
        RSUs.add(index, rsu);
    }

    /**
     * Removes RSU from this lane.
     * @param rsu RSU to remove.
     */
    public void removeRSU(RSU rsu) {
        RSUs.remove(rsu);
    }

    /**
     * Returns the number of RSUs at this lane.
     * @return Number of RSUs.
     */
    public int RSUcount() {
        return RSUs.size();
    }

    /**
     * Returns the RSU at the given index.
     * @param index Index of requested RSU.
     * @return RSU at index.
     */
    public RSU getRSU(int index) {
        return RSUs.get(index);
    }
    
    /* Never used junk
    public int getDestination() {
		return destination;
	}
    
	public void setDestination(int destination) {
		this.destination = destination;
	}
	
	public int getOrigin() {
		return origin;
	}
	
	public void setOrigin(int origin) {
		this.origin = origin;
	}*/
    
	/**
     * Returns the ID of the lane.
     * @return ID of the lane.
     */
    public int id() {
        return id;
    }

    /**
     * Finds a movable beginning at some location and moving either up- or
     * downstream. The search will not pass merging or splitting lanes in the
     * direction where multiple lanes become available (i.e. only the 
     * <tt>up</tt> and <tt>down</tt> field of lanes are used).
     * @param x Start location [m] for the search.
     * @param updown Whether to search up or downstream.
     * @return Found movable, <tt>null</tt> if none found.
     */
    public Movable findVehicle(double x, Model.longDirection updown) {
        Movable veh = null;
        if (updown==Model.longDirection.UP) {
            // if there are vehicles on the lane, pick any vehicle
            if (!vehicles.isEmpty()) {
                veh = vehicles.get(0);
            } else {
                // search for upstream lane with vehicles
                Lane j = up;
                while (j!=null && j.vehicles.isEmpty()) {
                	if (j.marked){
                		j = null;
                		System.out.println("Loop detected in \"up\" links");
                	} else {
                		j.marked = true;
                		j = j.up;
                	}	
                }
                // pick any vehicle
                if (j!=null && !j.vehicles.isEmpty()) {
                    veh = j.vehicles.get(0);
                }
                // remove the marks
                for (j = up; j!= null && j.marked; j = j.up)
                	j.marked = false;
            }
            // search up/downstream to match x
            if (veh != null) {
                while (veh.down != null && veh.down.x + xAdj(veh.down.lane) <= x) {
                    veh = veh.down;
                }
                while (veh != null && veh.x + xAdj(veh.lane) > x) {
                    veh = veh.up;
                }
            }
        } else if (updown==Model.longDirection.DOWN) {
            // if there are vehicle on the lane, pick any vehicle
            if (!vehicles.isEmpty()) {
                veh = vehicles.get(0);
            } else {
                // search for downstream lane with vehicles
                Lane j = down;
                while (j!=null && j.vehicles.isEmpty()) {
                	if (j.marked) {
                		j = null;
                		System.out.println("Loop detected in \"down\" links");
                	} else {
                		j.marked = true;
                		j = j.down;
                	}
                }
                // pick any vehicle
                if (j!=null && !j.vehicles.isEmpty()) {
                    veh = j.vehicles.get(0);
                }
                // remove the marks
                for (j = down; j!= null && j.marked; j = j.down)
                	j.marked = false;
            }
            // search up/downstream to match x
            if (veh != null) {
                while (veh.up != null && veh.up.x + xAdj(veh.up.lane) >= x) {
                    veh = veh.up;
                }
                while (veh != null && veh.x + xAdj(veh.lane) < x) {
                    veh = veh.down;
                }
            }
        }
        return veh;
    }

    /**
     * Finds the first RSU downstream of a location (not at) within a certain 
     * range.
     * @param x Start location of search [m].
     * @param range Range of search [m].
     * @return Next RSU, multiple if multiple at the same location.
     */
    public java.util.ArrayList<RSU> findRSU(double x, double range) {
        java.util.ArrayList<RSU> out = new java.util.ArrayList<RSU>();
        Lane atLane = this;
        double searchRange = 0;
        while (atLane!=null && searchRange<=range) {
            // Loop all RSUs on this lane
            for (int i=0; i<atLane.RSUcount(); i++) {
                double xAdj = xAdj(atLane);
                if (xAdj+atLane.getRSU(i).x>x 
                        && xAdj+atLane.getRSU(i).x-x<=range) {
                    out.add(atLane.getRSU(i));
                    // Add additional RSUs at the same location
                    double xRsu = atLane.getRSU(i).x();
                    i++;
                    while (i<atLane.RSUcount() && atLane.getRSU(i).x()==xRsu) {
                        out.add(atLane.getRSU(i));
                        i++;
                    }
                    return out;
                }
                // Update search range and quit if possible
                searchRange = xAdj+atLane.getRSU(i).x-x;
                if (searchRange>range) {
                    return out;
                }
            }
            // If no RSUs, move to next lane
            atLane = atLane.down;
            // Update searchrange at start of new lane
            if (atLane!=null) {
                searchRange = xAdj(atLane)-x;
            }
        }
        return out;
    }
    
    /**
     * Finds the first noticeable RSU downstream of a location (not at) within 
     * a certain range.
     * @param x Start location of search [m].
     * @param range Range of search [m].
     * @return Next noticeable RSU, multiple if multiple at the same location.
     */
    public java.util.ArrayList<RSU> findNoticeableRSU(double x, double range) {
        java.util.ArrayList<RSU> out = new java.util.ArrayList<RSU>();
        Lane atLane = this;
        double searchRange = 0;
        while (atLane!=null && searchRange<=range) {
            // Loop all RSUs on this lane
            for (int i=0; i<atLane.RSUcount(); i++) {
                if (atLane.getRSU(i).noticeable && xAdj(atLane)+atLane.getRSU(i).x>x 
                        && xAdj(atLane)+atLane.getRSU(i).x-x<=range) {
                    out.add(atLane.getRSU(i));
                    // Add additional RSUs at the same location
                    double xRsu = atLane.getRSU(i).x();
                    i++;
                    while (i<atLane.RSUcount() && atLane.getRSU(i).x()==xRsu) {
                        out.add(atLane.getRSU(i));
                        i++;
                    }
                    return out;
                }
                // Update search range and quit if possible
                searchRange = xAdj(atLane)+atLane.getRSU(i).x-x;
                if (searchRange>range) {
                    return out;
                }
            }
            // If no noticable RSUs, move to next lane
            atLane = atLane.down;
            // Update searchrange at start of new lane
            if (atLane!=null) {
                searchRange = xAdj(atLane)-x;
            }
        }
        return out;
    }

    /**
     * Finds the adjustment required to compare positions of two objects on
     * different but longitudinally connected lanes. A value is returned that 
     * can be added to the position of an object at <tt>otherLane</tt> to get
     * the appropriate position from the start of this lane. Note that the value
     * should always be added, no matter if <tt>otherLane</tt> is up- or 
     * downstream, as negative adjustments may be returned. If the two lanes are
     * not up- or downstream from one another, 0 is returned.<br>
     * <br>
     * The search is performed passed merges and splits meaning that a value is
     * found if there is <i>a</i> possibility to move from one lane to the 
     * other.<br>
     * <br>
     * Note that this method can be called often, as adjustment values between
     * any set of lanes are calculated once, and then stored for sucessive use.
     * @param otherLane Lane from which the adjustment is required.
     * @return Distance [m] to other lane.
     */
    public double xAdj(Lane otherLane) {
        return xAdj(otherLane, null);
    }
    
    /**
     * Performs the actual work of <tt>xAdj(jLane)</tt>. This method may be 
     * called recursively with a decreasing remaining range.
     * @param otherLane Lane from which the adjustment is required.
     * @param range Maximum search range [m].
     * @param dir Direction of search, use <tt>null</tt> for both.
     * @return Distance [m] to other lane.
     */
    protected double xAdj(Lane otherLane, Model.longDirection dir) {
        double dx = 0;
        if (otherLane!=this) { // && otherLane!=null) {
            try {
                dx = xAdjust.get(otherLane.id) ;
            } catch (NullPointerException npe) {
                // If no other lane specified, return 0.
                if (otherLane==null) {
                    return 0;
                }
                // If value is not in the map, retrieve and store it.
                // Calculate dx and store in xAdjust
                boolean found = false;
                Lane j = this;
                if (dir==null || dir==Model.longDirection.DOWN) {
                    // search downstream
                    while (j!=null && !found) {
                        // increase downstream distance
                        dx = dx + j.l;
                        if (j.down == otherLane) {
                            // lane found
                            found = true;
                        } else if (j.isSplit()) {
                            // at split, forward request to downstream lanes
                            double dx2;
                            for (Lane lane : j.downs) {
                                if (lane==otherLane) {
                                    found = true;
                                    break;
                                } else {
                                	if (lane.marked)
                                		continue;
                                	lane.marked = true;
                                    dx2 = lane.xAdj(otherLane, Model.longDirection.DOWN);
                                    if (dx2>0) {
                                        dx = dx + dx2;
                                        found = true;
                                        lane.marked = false;
                                        break;
                                    }
                                    lane.marked = false;
                                }
                            }
                        }
                        j = j.down;
                    }
                }
                if (!found && (dir==null || dir==Model.longDirection.UP)) {
                    // not found, search upstream
                    dx = 0;
                    j = this;
                    while (j!=null && !found) {
                        // reduce upstream distance
                        if (j.up != null) {
                            dx = dx - j.up.l;
                        }
                        if (j.up == otherLane) {
                            found = true;
                        } else if (j.isMerge()) {
                            // at merge, forward request to upstream lanes
                            double dx2;
                            for (Lane lane : j.ups) {
                                dx -= lane.l;
                                if (lane==otherLane) {
                                    found = true;
                                    break;
                                } else {
                                	if (lane.marked)
                                		continue;
                                	lane.marked = true;
                                    dx2 = lane.xAdj(otherLane, Model.longDirection.UP);
                                    if (dx2<0) {
                                        dx = dx + dx2;
                                        found = true;
                                        lane.marked = false;
                                        break;
                                    }
                                    lane.marked = false;
                                }
                                if (!found) {
                                    dx += lane.l;
                                }
                            }
                        }
                        j = j.up;
                    }
                }
                if (!found) {
                    dx = 0;
                }
                // Store if found (or searched in both directions and not found)
                if (found || dir==null) {
                    xAdjust.put(otherLane.id, dx);
                    // also store the reverse value for the other lane
                    otherLane.xAdjust.put(id, -dx);
                }
            }
        }
        return dx;
    }
    
    /**
     * Checks whether two <tt>jLane</tt>s are in the same physical lane, e.g.
     * <tt>true</tt> if the two <tt>jLane</tt>s are downstream or upstream of 
     * one another. This is considered <tt>false</tt> if there is a merge or
     * split in between the two lanes (although <tt>xAdj!=0</tt> holds).
     * @param otherLane The other <tt>jLane</tt>.
     * @return <tt>true</tt> if the lanes are in the same physical lane.
     */
    public boolean isSameLane(Lane otherLane) {
        if (otherLane==this) {
            return true;
        } else if (otherLane==null) {
            return false;
        } else {
            return xAdj(otherLane)!=0 && downSplit==otherLane.downSplit && upMerge==otherLane.upMerge;
        }
    }

    /**
     * Returns the speed limit as m/s.
     * @return Speed limit [m/s]
     */
    public double getVLim() {
        return vLim/3.6;
    }

    /**
     * Returns the location of x on an adjacent lane keeping lane length 
     * difference and curvature in mind. If either lane change is possible the 
     * lanes are physically adjacent and it is assumed that curvature of both 
     * lanes is defined in adjacent straight sub-sections. If neither lane 
     * change is possible, the lanes may not be physically adjacent and only 
     * total length is considered.
     * @param x Location on this lane [m].
     * @param dir Left or right.
     * @return Adjacent location [m].
     */
    public double getAdjacentX(double x, Model.latDirection dir) {

        if (dir==Model.latDirection.LEFT && !goLeft && !left.goRight) {
            // maybe not physically adjacent, use total length only
            return x * left.l/l;
        } else if (dir==Model.latDirection.RIGHT && !goRight && !right.goLeft) {
            // maybe not physically adjacent, use total length only
            return x * right.l/l;
        } else {
            // get appropriate section, and fraction within section
            double xCumul = 0; // length at end of appropriate section
            int section = 0;
            double dx = 0;
            double dy = 0;
            if (x>l) {
                // last section
                section = this.x.length-2;
                dx = this.x[section+1]-this.x[section];
                dy = this.y[section+1]-this.y[section];
                xCumul = l;
            } else if (x<=0) {
                // first section
                dx = this.x[section+1]-this.x[section];
                dy = this.y[section+1]-this.y[section];
                xCumul = Math.sqrt(dx*dx + dy*dy);
            } else {
                // find section by looping
                while (xCumul<x) {
                    dx = this.x[section+1]-this.x[section];
                    dy = this.y[section+1]-this.y[section];
                    xCumul = xCumul + Math.sqrt(dx*dx + dy*dy);
                    section++;
                }
                section--;
            }
            double lSection = Math.sqrt(dx*dx + dy*dy); // length of appropriate section
            double fSection = 1-(xCumul-x)/lSection; // fraction within appropriate section
            // loop appropriate adjacent lane
            Lane lane = null;
            if (dir==Model.latDirection.LEFT) {
                lane = left;
            } else if (dir==Model.latDirection.RIGHT) {
                lane = right;
            }
            // loop preceding sections
            double xStart = 0;
            for (int i=0; i<section; i++) {
                dx = lane.x[i+1]-lane.x[i];
                dy = lane.y[i+1]-lane.y[i];
                xStart = xStart + Math.sqrt(dx*dx + dy*dy);
            }
            // add part of appropriate section
            dx = lane.x[section+1]-lane.x[section];
            dy = lane.y[section+1]-lane.y[section];
            return xStart + fSection*Math.sqrt(dx*dx + dy*dy);
        }

 
    	
    }

    /**
     * Utility to connect this lane with the right lane.
     * @param goRight Whether change from this lane to right is possible.
     * @param right The right lane.
     * @param goLeft Whether change from right to this lane is possible.
     */
    public void connectLat(boolean goRight, Lane right, boolean goLeft) {
        this.right = right;
        this.goRight = goRight;
        right.left = this;
        right.goLeft = goLeft;
    }

    /**
     * Utility to connect this lane with the upstream lane.
     * @param up The upstream lane.
     */
    public void connectLong(Lane up) {
        up.down = this;
        this.up = up;
    }

    /**
     * Returns the global x and y at the lane centre.
     * @param pos Position [m] on the lane.
     * @return Point with x and y coordinate.
     */
    public java.awt.geom.Point2D.Double XY(double pos) {
        double cumlength[] = new double[x.length];
        cumlength[0] = 0;
        double dx; // section distance in x
        double dy; // section distance in y
        int section = -1; // current section of vehicle
        // calculate cumulative lengths untill x of vehicle is passed
        for (int i=1; i<x.length; i++) {
            dx = x[i] - x[i-1];
            dy = y[i] - y[i-1];
            cumlength[i] = cumlength[i-1] + java.lang.Math.sqrt(dx*dx + dy*dy);
            if (section==-1 && cumlength[i]>pos) {
                section = i;
                i = x.length; // stop loop
            }
        }
        if (section==-1) {
            // the vehicle is probably beyond the lane, extrapolate from last section
            section = x.length-1;
        }
        double x0 = x[section-1]; // start of current section
        double y0 = y[section-1];
        double x1 = x[section]; // end of current section
        double y1 = y[section];
        double res = pos-cumlength[section-1]; // distance within section
        double sec = cumlength[section] - cumlength[section-1]; // section length
        return new java.awt.geom.Point2D.Double(x0 + (x1-x0)*(res/sec), y0 + (y1-y0)*(res/sec));
    }
    
    /**
     * Returns the heading on the lane at the given position. The returned
     * <tt>Point2D.Double</tt> object is not actually a point. Instead, the x
     * and y values are the x and y headings.<br>
     * <pre><tt>
     *            x
     *       ----------
     *       |'-.
     *     y |   '-. 
     *       |      '-. heading, length = sqrt(x^2 + y^2) = 1</tt></pre> 
     * @param pos Position [m] on the lane.
     * @return Point where x and y are the x and y headings.
     */
    public java.awt.geom.Point2D.Double heading(double pos) {
        double cumlength[] = new double[x.length];
        cumlength[0] = 0;
        double dx; // section distance in x
        double dy; // section distance in y
        int section = -1; // current section of vehicle
        // calculate cumulative lengths untill x of vehicle is passed
        for (int i=1; i<x.length; i++) {
            dx = x[i] - x[i-1];
            dy = y[i] - y[i-1];
            cumlength[i] = cumlength[i-1] + java.lang.Math.sqrt(dx*dx + dy*dy);
            if (section==-1 && cumlength[i]>pos) {
                section = i;
                i = x.length; // stop loop
            }
        }
        if (section==-1) {
            // the point is probably beyond the lane, extrapolate from last section
            section = x.length-1;
        }
        dx = x[section] - x[section-1];
        dy = y[section] - y[section-1];
        double f = 1/Math.sqrt(dx*dx + dy*dy);
        return new java.awt.geom.Point2D.Double(dx*f, dy*f);
    }
    
    /**
     * Returns whether the destination can be reached from this lane.
     * @param destination Destination of interest.
     * @return Whether this lane leads to the given destination.
     */
    public boolean leadsTo(int destination) {
        return lanechanges.containsKey(destination);
    }
    
    /**
     * Returns the number of lane changes required to go to the given destination.
     * @param destination Destination of interest.
     * @return The number of lane changes for the destination.
     */
    public int nLaneChanges(int destination) {
        return lanechanges.get(destination);
    }
    
    /**
     * Returns the number of lane changes that need to be performed to go to
     * the destination from this lane.
     * @param destination Destination of interest.
     * @return Number of lane changes that needs to be performed for this destination.
     */
    public double xLaneChanges(int destination) {
        return endpoints.get(destination);
    }
    
    /**
     * Adds the given lane as one of several downstream lanes that this lane 
     * splits into.
     * @param split One of several downstream lanes.
     */    
    public void addSplitLane(Lane split) {
        if (!downs.contains(split)) {
            downs.add(split);
            split.up = this;
        }
    }
    
    /**
     * Adds the given lane as one of several upstream lanes that this lane 
     * merges from.
     * @param merge One of several upstream lanes.
     */
    public void addMergeLane(Lane merge) {
        if (!ups.contains(merge)) {
            ups.add(merge);
            merge.down = this;
        }
    }
       
    /**
     * Returns whether this lane splits, i.e. whether there are split lanes.
     * @return Whether this lane splits.
     */
    public boolean isSplit() {
        return !downs.isEmpty();
    }
    
    /**
     * Return whether this lane merges, i.e. whether there are merge lanes.
     * @return Whether this lane merges.
     */
    public boolean isMerge() {
        return !ups.isEmpty();
    }
    
    /**
     * Nested class of a RSU that will move each passing vehicle to a lane
     * according to the route of the vehicle. Also lets drivers react to 
     * whatever is downstream.
     */
    public class splitRSU extends RSU {

        /**
         * Default constructor, setting the split at the end of the lane and 
         * both passable and noticeable.
         */
        public splitRSU() {
            super(Lane.this, l, true, true);
        }

        /** Empty, needs to be implemented. */
        @Override
        public void init() {}

        /**
         * Set a vehicle that has just passed the split on the correct lane 
         * based on the route. The first lane which allows the route will be 
         * assigned.
         * @param vehicle Vehicle that has just entered the split.
         */
        @Override
        public void pass(Vehicle vehicle) {
            Lane lan = getLaneForRoute(vehicle.route);
            if (lan!=null) {
                if (vehicle.lcVehicle!=null) {
                    if ((vehicle.lcDirection==Model.latDirection.RIGHT && (lan.right==null || lan.right!=vehicle.lcVehicle.lane.down)) ||
                            (vehicle.lcDirection==Model.latDirection.LEFT && (lan.left==null || lan.left!=vehicle.lcVehicle.lane.down))) {
                    	vehicle.abortLaneChange();
                    }
                }
                // move vehicle to appropriate lane
                double atX = vehicle.x - l;
                while (atX > lan.l) {
                    if (lan.isSplit()) {
                        atX = atX - lan.l;
                        for (int i=0; i<lan.RSUcount(); i++) {
                            if (lan.getRSU(i) instanceof splitRSU) {
                                splitRSU split = (splitRSU) lan.getRSU(i);
                                Lane tmp = split.getLaneForRoute(vehicle.route);
                                if (tmp!=null) {
                                    lan = tmp;
                                    break;
                                }
                            }
                        }
                    } else if (lan.down!=null) {
                        atX = atX - lan.l;
                        lan = lan.down;
                    } else {
                        break;
                    }
                }
                vehicle.cut();
                vehicle.paste(lan, atX);
            } else {
                vehicle.model.deleted++;
                System.out.println("Vehicle deleted while split was entered ("+model.deleted+"), no applicable downstream lane.");
                vehicle.delete();
            }
        }
        
        /**
         * Returns the downstream lane for the given route. The first lane 
         * which allows the route is returned, where the order is equal to the
         * order in which lanes were added using <tt>addSplitLane</tt>.
         * @param route Route to be followed.
         * @return Downstream lane for the given route, <tt>null</tt> if none applies.
         */
        public Lane getLaneForRoute(Route route) {
            for (Lane lan : downs) {
                if (route.canBeFollowedFrom(lan)) {
                    return lan;
                }
            }
            return null;
        }

        /** Empty, needs to be implemented. */
        @Override
        public void control() {}

        /** Empty, needs to be implemented. */
        @Override
        public void noControl() {}
    }
    
    @Override
	public String toString() {
    	String result = String.format("Main.locale, %d: Length %.2fm, [", id, l);
    	for (int i = 0; i < x.length; i++)
    		result += String.format(Main.locale, "%s(%.3f,%.3f)", i > 0 ? " " : "", x[i], y[i]);
    	return result + "]";
    }
    
    public Lane getUp_r() {
    	return up;
    }
    
    public Lane getDown_r() {
    	return down;
    }
    
    public Lane getLeft_r() {
    	return left;
    }
    
    public Lane getRight_r() {
    	return right;
    }
    
    public int getDestination_r() {
    	return destination;
    }
    
    public int getOrigin_r() {
    	return origin;
    }
    
    public double getSpeedLimit_r() {
    	return vLim;
    }
    
    public boolean getCanMergeLeft_r() {
    	return goLeft;
    }
    
    public boolean getCanMergeRight_r() {
    	return goRight;
    }
}