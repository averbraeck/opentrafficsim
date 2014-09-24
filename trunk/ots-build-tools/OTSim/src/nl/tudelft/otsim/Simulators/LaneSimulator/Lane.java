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
	boolean markedForXadj;
	
    /** Array of x-coordinates defining the lane curvature. */
    public double[] x;

    /** Array of y-coordinates defining the lane curvature. */
    public double[] y;

    /** Length of the lane [m]. */
    public double l;

    /** Main model. */
    public Model model;

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

    /** All Movables on this lane, in order of increasing x. */
    private java.util.ArrayList<Movable> vehicles = new java.util.ArrayList<Movable>(0);

    /** Destination number, NODESTINATION if no destination. */
    public int destination;

    /** Origin number, NOORIGIN if no origin. */
    public int origin;

    /** Legal speed limit [km/h]. */
    public double vLim = 120;
    
    private void checkOrdering () {
    	Movable prevM = null;
    	for (Movable thisM : vehicles) {
    		if (null != prevM)
    			if (prevM.x > thisM.x)
    				throw new Error("cannot happen");
    		prevM = thisM;
    	}
    }
    
    /**
     * Insert a Movable in the list of vehicles on this Lane.
     * @param m Movable to insert
     * @param pos Double; longitudinal position on this lane
     */
    public void paste (Movable m, double pos) {
    	vehicles.add(findVehicleIndex(pos), m);
    	checkOrdering();
    }
    
    /**
     * Remove a Movable from this Lane.
     * @param m Movable to remove
     */
    public void cut (Movable m) {
    	vehicles.remove(m);
    	checkOrdering();
    }
    
    /** 
     * Report presence of Movables on this Lane
     * @return Boolean; true if no Movables are on this Lane; false otherwise
     */
    public boolean isEmpty() {
    	return vehicles.isEmpty();
    }
    
    /**
     * Return the index where a Movable at position pos should be inserted in the vehicles list.
     * @param pos Double; the position of the vehicle to be inserted
     * @return index where a Movable at position pos should be inserted in the vehicles list
     */
    public int findVehicleIndex(double pos) {
    	if (vehicles.size() == 0)
    		return 0;
    	int result = (int) (pos / l * vehicles.size());
    	// Move backward while we're too far
    	while ((result > 0) && (vehicles.get(result - 1).x > pos))
    		result--;
    	if (result < 0)
    		return 0;
    	while ((result < vehicles.size()) && (vehicles.get(result).x < pos))
    		result++;
    	return result;
    }
    
    /**
     * Return a copy of the vehicles list.
     * @return ArrayList&lt;Movable&gt;; the copy of the vehicles list
     */
    public java.util.ArrayList<Movable> getVehicles() {
    	return new java.util.ArrayList<Movable> (vehicles);
    }

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

	private boolean visible = true;	// Default is to paint the lane
    
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
    
    /* Never used
    public Lane(double[] x, double[] y, int id) {
        this.x = x;
        this.y = y;
        this.id = id;
        calculateLength();
    } 
    */
    
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
    	//System.out.println("init lane " + id);
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
     * also past a merge, until the next split.
     * @param split Splitting lane.
     */
    protected void setDownstreamSplit(Lane split) {
        downSplit = split;
        if (up!=null && !up.isSplit())
            up.setDownstreamSplit(split);
        for (Lane j : ups)
            if (!j.isSplit())
                j.setDownstreamSplit(split);
    }
    
    /**
     * Sets the upstream merge to the given lane for this and downstream lanes, 
     * also past a split, until the next merge.
     * @param split Merging lane.
     */
    protected void setUpstreamMerge(Lane merge) {
        upMerge = merge;
        if (down!=null && !down.isMerge())
            down.setUpstreamMerge(merge);
        for (Lane j : downs)
            if (!j.isMerge())
                j.setUpstreamMerge(merge);
    }
    
    /**
     * Initializes the lane change info throughout the network for a possible 
     * destination of this lane.
     */
    public void initLaneChangeInfo() {
    	//if (destination > 0 && leadsTo(destination))
    	//	endpoints.put(destination, l);
    	// 20140313/PK: changed > into >= in the next line
    	//int depth = 0;
		if ((destination >= 0) && (!leadsTo(destination))) {
            // find all lanes in cross section with same destination and set initial info
            java.util.ArrayList<Lane> curlanes = new java.util.ArrayList<Lane>();
            curlanes.add(this);
            lanechanges.put(destination, 0);
            endpoints.put(destination, l);
            Lane lane = left;
			while ((lane != null) && (lane.destination == destination)) {
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
            while (! curlanes.isEmpty()) {
            	/*
                if (2 == destination) {
                	System.out.println("Carving route at depth " + depth + ", destination " + destination + "; curlanes contains");
                	for (Lane cl : curlanes) {
                		System.out.println("  lane " + cl.id() + " lanechanges: " + cl.lanechanges);
                	}
                    depth++;
                }
                */
                // move left
                int n = curlanes.size();
                for (int i=0; i<n; i++) {
                    Lane curlane = curlanes.get(i);
                    int lcs = 0;
                    while (curlane.left!=null && curlane.left.goRight && 
                            !curlanes.contains(curlane.left) &&
                            !curlane.left.lanechanges.containsKey(destination)
                            && (curlane.left.destination == 0)) {
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
                            !curlane.right.lanechanges.containsKey(destination)
                            && (curlane.right.destination == 0)) {
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
                    	//if (0 == curlane.up.destination) {
	                        uplanes.add(curlane.up); // add to uplanes
	                        // copy number of lane changes
	                        curlane.up.lanechanges.put(destination, curlane.lanechanges.get(destination));
	                        // increase with own length
	                        curlane.up.endpoints.put(destination, curlane.endpoints.get(destination)+curlane.up.l);
                    	//}
                    }
                    for (Lane j : curlane.ups) {
                    	//if (0 == j.destination) {
	                        uplanes.add(j);
	                        j.lanechanges.put(destination, curlane.lanechanges.get(destination));
	                        if (null != j.endpoints.get(destination))
	                        	System.out.println("updating cost; current cost is " + j.endpoints.get(destination));
	                        j.endpoints.put(destination, curlane.endpoints.get(destination)+j.l);
                    	//}
                    }
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
            if (rsu.x <= RSUs.get(0).x)
                index = 0; // RSU before first
            else if (RSUs.get(RSUs.size()-1).x <= rsu.x)
                index = RSUs.size(); // RSU after last
            else {
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
     * @param startX Double; Start location [m] for the search.
     * @param updown Whether to search up or downstream.
     * @return Found movable, <tt>null</tt> if none found.
     */
    public Movable findVehicle(double startX, Model.longDirection updown, double maxDistance) {
    	if (maxDistance < 0)
    		return null;
    	if (startX > l + 0.002) {		// UGLY! This margin must be bigger than margin used in Driver.anticipatedSpeed
    		System.err.println("StartX is out of range"); // FIXME
    		return null;	// STUB
    	}
    	int index = findVehicleIndex(startX);
        if (updown == Model.longDirection.UP) {
        	if (index > 0)
        		return vehicles.get(--index);
        	if (null != up) {
        		markedForXadj = true;
        		Movable result = up.lastVehicle(maxDistance - l);
        		markedForXadj = false;
        		return result;
        	}
        } else {
        	if (index < vehicles.size())
        		return vehicles.get(index);
        	if (null != down) {
        		markedForXadj = true;
        		Movable result = down.firstVehicle(maxDistance - l);
        		markedForXadj = false;
        		return result;
        	}
        }
    	return null;
    }
    
    private Movable lastVehicle (double maxDistance) {
    	if (! isEmpty())
    		return vehicles.get(vehicles.size() - 1);
    	if ((null == up) || up.markedForXadj)
    		return null;
    	markedForXadj = true;
    	Movable result = up.lastVehicle(maxDistance - l);
    	markedForXadj = false;
    	return result;
    }
    
    private Movable firstVehicle (double maxDistance) {
    	if (maxDistance < 0)
    		return null;
    	if (! isEmpty())
    		return vehicles.get(0);
    	if ((null == down) || down.markedForXadj)
    		return null;
    	markedForXadj = true;
    	Movable result = down.firstVehicle(maxDistance - l);
    	markedForXadj = false;
    	return result;
    }
    
    /*
    public Movable oldfindVehicle(double startX, Model.longDirection updown) {
        Movable veh = null;
        if (updown == Model.longDirection.UP) {
            // if there are vehicles on the lane, pick any vehicle
            if (!vehicles.isEmpty())
                veh = vehicles.get(0);
            else {
            	//double distance = 0;   //GUUS
                // search for upstream lane with vehicles            	
                Lane j = up;
                while ((j != null) && j.vehicles.isEmpty()) {
                	if (j.marked) {
                		j = null;
                		//System.out.println("Loop detected in \"up\" links");
                	} else {
                		j.marked = true;
                		j = j.up;
                	}	
                }
                // pick any vehicle
                if (j!=null && !j.vehicles.isEmpty())
                    veh = j.vehicles.get(0);
                // remove the marks
                for (j = up; j!= null && j.marked; j = j.up)
                	j.marked = false;
            }
            if (veh != null) {
                // search downstream; then upstream to find first vehicle with x > startX
            	while ((null != veh.getNeighbor(Movable.DOWN)) && (veh.getNeighbor(Movable.DOWN).x + xAdj(veh.getNeighbor(Movable.DOWN).getLane()) <= startX))
            		veh = veh.getNeighbor(Movable.DOWN);
            	while ((null != veh) && (veh.x + xAdj(veh.getLane()) > startX))
            		veh = veh.getNeighbor(Movable.UP);
            }
        } else if (updown==Model.longDirection.DOWN) {
            // if there are vehicle on the lane, pick any vehicle
            if (!vehicles.isEmpty())
                veh = vehicles.get(0);
            else {
                // search for downstream lane with vehicles
                Lane j = down;
                while ((j != null) && j.vehicles.isEmpty()) {
                	if (j.marked) {
                		j = null;
                		//System.out.println("Loop detected in \"down\" links");
                	} else {
                		j.marked = true;
                		j = j.down;
                	}
                }
                // pick any vehicle
                if (j!=null && !j.vehicles.isEmpty())
                    veh = j.vehicles.get(0);
                // remove the marks
                for (j = down; j!= null && j.marked; j = j.down)
                	j.marked = false;
            }
            if (veh != null) {
                // search upstream; then downstream to find first vehicle with x < startX
            	while ((null != veh.getNeighbor(Movable.UP)) && (veh.getNeighbor(Movable.UP).x + xAdj(veh.getNeighbor(Movable.UP).getLane()) >= startX))
            		veh = veh.getNeighbor(Movable.UP);
            	while ((null != veh) && (veh.x + xAdj(veh.getLane()) < startX))
            		veh = veh.getNeighbor(Movable.DOWN);
            }
        }
        return veh;
    }
    */

    /**
     * Finds the first RSU downstream of a location (not at) within a certain 
     * range.
     * @param startX Double; Start location of search [m].
     * @param range Range of search [m].
     * @return Next RSU, multiple if multiple at the same location.
     */
    public java.util.ArrayList<RSU> findRSU(double startX, double range) {
        java.util.ArrayList<RSU> out = new java.util.ArrayList<RSU>();
        Lane atLane = this;
        double searchRange = 0;
        while (atLane!=null && searchRange<=range) {
            // Loop all RSUs on this lane
            for (int i=0; i<atLane.RSUcount(); i++) {
                double xAdj = xAdj(atLane);
                if (xAdj+atLane.getRSU(i).x>startX 
                        && xAdj+atLane.getRSU(i).x-startX<=range) {
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
                searchRange = xAdj+atLane.getRSU(i).x-startX;
                if (searchRange>range) {
                    return out;
                }
            }
            // If no RSUs, move to next lane
            atLane = atLane.down;
            // Update searchRange at start of new lane
            if (atLane!=null)
                searchRange = xAdj(atLane)-startX;
        }
        return out;
    }
    
    /**
     * Finds the first noticeable RSU downstream of a location (not at) within 
     * a certain range.
     * @param startX Double; Start location of search [m].
     * @param range Range of search [m].
     * @return Next noticeable RSU, multiple if multiple at the same location.
     */
    public java.util.ArrayList<RSU> findNoticeableRSU(double startX, double range) {
        java.util.ArrayList<RSU> out = new java.util.ArrayList<RSU>();
        Lane atLane = this;
        double searchRange = 0;
        while ((null != atLane) && (searchRange <= range)) {
            // Loop all RSUs on this lane
            for (int i = 0; i < atLane.RSUcount(); i++) {
                if (atLane.getRSU(i).noticeable && (xAdj(atLane) + atLane.getRSU(i).x > startX) 
                        && (xAdj(atLane) + atLane.getRSU(i).x - startX <= range)) {
                    out.add(atLane.getRSU(i));
                    // Add additional RSUs at the same location
                    double xRsu = atLane.getRSU(i).x();
                    i++;
                    while ((i < atLane.RSUcount()) && (atLane.getRSU(i).x() == xRsu)) {
                        out.add(atLane.getRSU(i));
                        i++;
                    }
                    return out;
                }
                // Update search range and quit if possible
                searchRange = xAdj(atLane) + atLane.getRSU(i).x - startX;
                if (searchRange > range)
                    return out;
            }
            // If no noticeable RSUs, move to next lane
            atLane = atLane.down;
            // Update searchRange at start of new lane
            if (null != atLane)
                searchRange = xAdj(atLane) - startX;
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
     * any set of lanes are calculated once, and then stored for successive use.
     * @param otherLane Lane from which the adjustment is required.
     * @return Distance [m] to other lane.
     */
    public double xAdj(Lane otherLane) {
        // 0 for self or no other lane
        if ((this == otherLane) || (null == otherLane))
            return 0;
        // return earlier found value
        if (xAdjust.containsKey(otherLane.id))
            return xAdjust.get(otherLane.id);
        // find, store (in helper method) and return value
        return xAdj(otherLane, 0, new Object());
    }
    
    // distance marker for xAdj recursion to stop in case of (longer) loop
    private double xAdjDistance = Double.POSITIVE_INFINITY;
    // last lane on which xAdj(otherLane) was called, the search of which reached this lane
    //private Lane xAdjLane = null;
    // last key when xAdj(otherLane) was called, the search of which reached this lane
    protected Object xAdjKey = null;

    /**
     * Performs the actual work of <tt>xAdj(jLane)</tt>.
     * @param otherLane Lane from which the adjustment is required.
     * @param dir Direction of search, use <tt>null</tt> for both.
     * @return Distance [m] to other lane.
     */
    protected double xAdj(Lane otherLane, double distance, final Object key) {
        // return ‘not found’ value if better value has already been found (to stop recursion in a loop)
        // this will overwrite information in case distance<xAdjDistance as this is a shorter connection
        if ((key == xAdjKey) && (distance >= xAdjDistance))
            return 0;
        xAdjDistance = distance; // remember distance traveled up to this lane
        xAdjKey = key; // remember to which original lane this pertains    
        double dx = 0; // longitudinal difference between two lanes
        double dx2; // used for recursive search
        if (null != down) {
            if (down == otherLane)
                dx = l;
            else { // use further recursion
                dx2 = down.xAdj(otherLane, distance + l, key);
                if (dx2 > 0)
                    dx = dx2 + l;
            }
        } else if (isSplit()) {
            if (downs.contains(otherLane))
                dx = l;
            else { // find the minimum distance from any of the splits
                double minDx2 = Double.POSITIVE_INFINITY;
                for (Lane j : downs) { // use further recursion
                    dx2 = j.xAdj(otherLane, distance + l, key);
                    if ((dx2 > 0) && (dx2 < minDx2))
                    	minDx2 = dx2;
                }
                if (!Double.isInfinite(minDx2))
                    dx = minDx2 + l;
            }
        }
        xAdjust.put(otherLane.id, dx);
        return dx;
    }

    /*
    protected double oldxAdj(Lane otherLane, Model.longDirection dir) {
        // Skip if already marked or same lane or no lane
        if (markedForXadj || otherLane==this || otherLane==null) {
            return 0;
        }
        // Check whether it has been found before
        if (xAdjust.containsKey(otherLane.id)) {
            return xAdjust.get(otherLane.id);
        }
        // Search
        double dx = 0; // longitudinal difference between two lanes
        double dx2; // used for recursive search
        boolean found = false;
        markedForXadj = true;
        // Search downstream
        if (null==dir || Model.longDirection.DOWN==dir) {
            if (null!=down) {
                if (down==otherLane) {
                    found = true;
                    dx = l;
                } else {
                    // use further recursion
                    dx2 = down.xAdj(otherLane, Model.longDirection.DOWN);
                    if (dx2>0) {
                        found = true;
                        dx += dx2+l;
                    }
                }
            } else if (isSplit()) {
                if (downs.contains(otherLane)) {
                    found = true;
                    dx = l;
                } else {
                    for (Lane j : downs) {
                        // use further recursion
                        dx2 = j.xAdj(otherLane, Model.longDirection.DOWN);
                        if (dx2>0) {
                            found = true;
                            dx += dx2+l;
                            break;
                        }
                    }
                }
            }
        }
        if (!found) {
            dx = 0;
        }
        // Store if found, or not found but searched in both directions
        if (found || dir==null) { 
            xAdjust.put(otherLane.id, dx);
        }
        markedForXadj = false;
        return dx;
    }
    */
    
    /**
     * Checks whether two <tt>jLane</tt>s are in the same physical lane, e.g.
     * <tt>true</tt> if the two <tt>jLane</tt>s are downstream or upstream of 
     * one another. This is considered <tt>false</tt> if there is a merge or
     * split in between the two lanes (although <tt>xAdj!=0</tt> holds).
     * @param otherLane The other <tt>jLane</tt>.
     * @return <tt>true</tt> if the lanes are in the same physical lane.
     */
    public boolean isSameLane(Lane otherLane) {
        if (otherLane == this)
            return true;
        else if (otherLane == null)
            return false;
        else
            return xAdj(otherLane) != 0 && downSplit==otherLane.downSplit && upMerge==otherLane.upMerge;
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
     * @param myX Location on this lane [m].
     * @param dir Left or right.
     * @return Adjacent location [m].
     */
    public double getAdjacentX(double myX, Model.latDirection dir) {
        if ((dir == Model.latDirection.LEFT) && !goLeft && !left.goRight)
            return myX * left.l/l; // maybe not physically adjacent, use total length only
        else if (dir==Model.latDirection.RIGHT && !goRight && !right.goLeft)
            return myX * right.l/l; // maybe not physically adjacent, use total length only
        else {
            // get appropriate section, and fraction within section
            double xCumul = 0; // length at end of appropriate section
            int section = 0;
            double dx = 0;
            double dy = 0;
            if (myX>l) {
                // last section
                section = this.x.length-2;
                dx = this.x[section+1]-this.x[section];
                dy = this.y[section+1]-this.y[section];
                xCumul = l;
            } else if (myX<=0) {
                // first section
                dx = this.x[section+1]-this.x[section];
                dy = this.y[section+1]-this.y[section];
                xCumul = Math.sqrt(dx*dx + dy*dy);
            } else {
                // find section by looping
                while (xCumul<myX) {
                    dx = this.x[section+1]-this.x[section];
                    dy = this.y[section+1]-this.y[section];
                    xCumul = xCumul + Math.sqrt(dx*dx + dy*dy);
                    section++;
                }
                section--;
            }
            double lSection = Math.sqrt(dx*dx + dy*dy); // length of appropriate section
            double fSection = 1-(xCumul-myX)/lSection; // fraction within appropriate section
            // loop appropriate adjacent lane
            Lane lane = null;
            if (dir==Model.latDirection.LEFT)
                lane = left;
            else if (dir==Model.latDirection.RIGHT)
                lane = right;
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
     * Method to connect this lane with the right lane.
     * @param newGoRight Whether change from this lane to right is possible.
     * @param rightLane The right lane.
     * @param newGoLeft Whether change from right to this lane is possible.
     */
    public void connectLat(boolean newGoRight, Lane rightLane, boolean newGoLeft) {
        this.right = rightLane;
        this.goRight = newGoRight;
        rightLane.left = this;
        rightLane.goLeft = newGoLeft;
    }

    /**
     * Method to connect this lane with the upstream lane.
     * @param upLane The upstream lane.
     */
    public void connectLong(Lane upLane) {
        upLane.down = this;
        this.up = upLane;
    }

    /**
     * Returns the global x and y at the lane center.
     * @param pos Position [m] on the lane.
     * @return Point with x and y coordinate.
     */
    public java.awt.geom.Point2D.Double XY(double pos) {
        double cumlength[] = new double[x.length];
        cumlength[0] = 0;
        double dx; // section distance in x
        double dy; // section distance in y
        int section = -1; // current section of vehicle
        // calculate cumulative lengths until x of vehicle is passed
        for (int i = 1; i < x.length; i++) {
            dx = x[i] - x[i - 1];
            dy = y[i] - y[i - 1];
            cumlength[i] = cumlength[i - 1] + java.lang.Math.sqrt(dx * dx + dy * dy);
            if (section==-1 && cumlength[i]>pos) {
                section = i;
                i = x.length; // stop loop
            }
        }
        if (section == -1) {
            // the vehicle is probably beyond the lane, extrapolate from last section
            section = x.length-1;
        }
        double x0 = x[section - 1]; // start of current section
        double y0 = y[section - 1];
        double x1 = x[section]; // end of current section
        double y1 = y[section];
        double res = pos - cumlength[section - 1]; // distance within section
        double sec = cumlength[section] - cumlength[section - 1]; // section length
        return new java.awt.geom.Point2D.Double(x0 + (x1 - x0)*(res / sec), y0 + (y1 - y0) * (res / sec));
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
     * @param whichDestination Destination of interest.
     * @return Whether this lane leads to the given destination.
     */
    public boolean leadsTo(int whichDestination) {
    	if (lanechanges.isEmpty()) {
    		for (Lane downLane : downs)
    			if (downLane.leadsTo(whichDestination))
    				return true;
    		// 20140314/PK also check down (if non-null)
    		//if (null != down)
    		//	if (down.leadsTo(whichDestination))
    		//		return true;
    	}
        return lanechanges.containsKey(whichDestination);
    }
    
    /**
     * Returns the number of lane changes required to go to the given destination.
     * @param whichDestination Destination of interest.
     * @return The number of lane changes for the destination.
     */
    public int nLaneChanges(int whichDestination) {
    	if (null == lanechanges)
    		return 0;	// STUB FIXME
    	if (null == lanechanges.get(whichDestination))
    		return 0;	// STUB FIXME
        return lanechanges.get(whichDestination);
    }
    
    /**
     * Returns the number of lane changes that need to be performed to go to
     * the destination from this lane.
     * @param whichDestination Destination of interest.
     * @return Number of lane changes that needs to be performed for this destination.
     */
    public double xLaneChanges(int whichDestination) {
    	Double result = endpoints.get(whichDestination);
    	if (null == result) {
    		System.out.println("Unknown number of lanechanges; faking 1");
    		return 1;
    	}
        return endpoints.get(whichDestination);
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
            if (lan != null) {
                if (vehicle.lcVehicle != null) {
                    if ((vehicle.lcDirection == Model.latDirection.RIGHT && (lan.right == null || lan.right != vehicle.lcVehicle.getLane().down)) ||
                            (vehicle.lcDirection == Model.latDirection.LEFT && (lan.left == null || lan.left != vehicle.lcVehicle.getLane().down))) {
                    	vehicle.abortLaneChange();
                    }
                }
                // move vehicle to appropriate lane
                double atX = vehicle.x - l;
                while (atX > lan.l) {
                    if (lan.isSplit()) {
                        atX = atX - lan.l;
                        for (int i = 0; i < lan.RSUcount(); i++) {
                            if (lan.getRSU(i) instanceof splitRSU) {
                                splitRSU split = (splitRSU) lan.getRSU(i);
                                Lane tmp = split.getLaneForRoute(vehicle.route);
                                if (tmp != null) {
                                    lan = tmp;
                                    break;
                                }
                            }
                        }
                    } else if (lan.down!=null) {
                        atX = atX - lan.l;
                        lan = lan.down;
                    } else
                        break;
                }
                vehicle.cut();
                vehicle.paste(lan, atX);
                //vehicle.setNeighbor(Movable.LEFT_DOWN, null);
                //vehicle.setNeighbor(Movable.LEFT_UP, null);
                //vehicle.setNeighbor(Movable.RIGHT_DOWN, null);
                //vehicle.setNeighbor(Movable.RIGHT_UP, null);
            } else {
                getLaneForRoute(vehicle.route);
                vehicle.model.deleted++;
                System.err.println("Vehicle deleted while split was entered ("+model.deleted+"), no applicable downstream lane.");
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
        	double cost = Double.POSITIVE_INFINITY;
        	Lane bestLane = null;
            for (Lane lan : downs)
                if (route.canBeFollowedFrom(lan)) {
                	// 20140314/PK: STUB: If primary destination is not in lan.endpoints; just select this lane
                    Double thisCost = lan.endpoints.get(route.destinations[0]);
                    if (null == thisCost)
                    	bestLane = lan;
                    //System.out.println(String.format("destination %d: lane %d cost %.2f", route.destinations[0], lan.id, thisCost));
                    else if (thisCost < cost) {
                    	cost = thisCost;
                    	bestLane = lan;
                    }
                }
            return bestLane;
        }

        /** Empty, needs to be implemented. */
        @Override
        public void control() {}

        /** Empty, needs to be implemented. */
        @Override
        public void noControl() {}
        
        /**
         * Return a human readable description of this splitRSU.
         */
        @Override
		public String toString() {
        	return String.format("splitRSU " + super.toString() + " on lane " + lane.id);
        }
    }
    
    @Override
	public String toString() {
    	String result = String.format(Main.locale, "%d: Length %.2fm, [", id, l);
    	for (int i = 0; i < x.length; i++)
    		result += String.format(Main.locale, "%s(%.3f,%.3f)", i > 0 ? " " : "", x[i], y[i]);
    	return result + "]";
    }
    
    /**
     * Retrieve the upstream connected Lane of this Lane.
     * @return Lane; the upstream connected Lane of this Lane
     */
    public Lane getUp_r() {
    	return up;
    }
    
    /**
     * Retrieve the downstream connected Lane of this Lane.
     * @return Lane; the downstream connected Lane of this Lane
     */
    public Lane getDown_r() {
    	return down;
    }
    
    /**
     * Retrieve the left Lane of this Lane.
     * @return Lane; the left Lane of this Lane
     */
    public Lane getLeft_r() {
    	return left;
    }
    
    /**
     * Retrieve the right Lane of this Lane.
     * @return Lane; the right Lane of this Lane
     */
    public Lane getRight_r() {
    	return right;
    }
    
    /**
     * Return the destination of this Lane.
     * @return Integer; the destination of this Lane, or a negative value if
     * this Lane is not a destination
     */
    public int getDestination_r() {
    	return destination;
    }
    
    /**
     * Return the origin of this Lane.
     * @return Integer; the origin of this Lane or a negative value if this
     * Lane is not an origin
     */
    public int getOrigin_r() {
    	return origin;
    }
    
    /**
     * Retrieve the speed limit on this Lane.
     * @return Double; the speed limit on this Lane in m/s
     */
    public double getSpeedLimit_r() {
    	return vLim;
    }
    
    /**
     * Return whether it is possible to merge to the left from this Lane
     * @return Boolean; true if it is possible to change to the left from this
     * Lane; false if it is not possible to change to the left from this Lane
     */
    public boolean getCanMergeLeft_r() {
    	return goLeft;
    }
    
    /**
     * Return whether it is possible to merge to the right from this Lane
     * @return Boolean; true if it is possible to change to the right from this
     * Lane; false if it is not possible to change to the right from this Lane
     */
    public boolean getCanMergeRight_r() {
    	return goRight;
    }
    
    /**
     * Retrieve the list of {@link RSU RSUs} on this Lane.
     * @return ArrayList&lt;{@link RSU}&gt;; the list of RSUs on this Lane
     */
    public java.util.ArrayList<RSU> getRSUs_r() {
    	return RSUs;
    }

	/**
	 * @param visible Boolean; true if this Lane is to be painted (default);
	 * false if this Lane must not be painted
	 */
	public void setVisible(boolean visible) {
		this.visible = visible;
	}

	/**
	 * @return Boolean; true if this lane is visible (this is the default);
	 * false if this lane is invisible (hidden)
	 */
	public boolean isVisible() {
		return visible;
	}
}