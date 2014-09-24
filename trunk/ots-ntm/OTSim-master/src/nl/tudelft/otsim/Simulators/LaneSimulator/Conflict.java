package nl.tudelft.otsim.Simulators.LaneSimulator;

/**
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
public class Conflict {
    
    /** Whether vehicles should keep the conflict area clear. */
    protected boolean clear;

    /** Type of the conflict being either split, merge or crossing. */
    protected conflictType type;
    
    /** RSU on road with priority. */
    protected conflictRSU p;
    
    /** RSU on road without priority. */
    protected conflictRSU y;
    
    /** Distance between lane centers where conflicts are located [m]. */
    protected static double D_CONF = 2.625;
    
    /** Length of the conflict [m]. */
    protected double length;
    
    /** Range at minor road where major road is visible [m]. */
    protected double visibility = Double.POSITIVE_INFINITY; 
    
    /** Lane of location where major road is visible from minor road. */
    protected Lane vLane;
    
    /** Location on vLane where major road is visible from minor road [m]. */
    protected double vX;
    
    /**
     * Constructor. Not public as conflicts should be created by using 
     * <tt>createSplit</tt>, <tt>createMerge</tt> or <tt>createCrossing</tt>.
     * Note that some properties have no effect for some conflict types, i.e. 
     * priority order on a split and <tt>clear</tt> on a merge or split.
     * @param pLane Lane with priority.
     * @param pX Coordinate on priority lane [m].
     * @param yLane Lane without priority.
     * @param yX Coordinate on lane without priority [m].
     * @param clear Whether traffic should keep the conflict clear.
     * @param type Type of conflict either split, merge or crossing.
     * @param length The length of the conflict [m].
     */
    protected Conflict(Lane pLane, double pX, Lane yLane, double yX,
            boolean clear, conflictType type, double length) {
        this.clear = clear;
        this.type = type;
        this.length = length;
        p = new conflictRSU(pLane, pX, true);
        y = new conflictRSU(yLane, yX, false);
    }
    
    /**
     * Creates a merge conflict at the end of two lanes <i>and</i> sets the 
     * lanes as merge lanes.
     * @param mLane Lane that other lanes will merge on.
     * @param pLane Lane with priority.
     * @param yLane Lane without priority.
     * @return Merge conflict, <tt>null</tt> if lanes do not intersect.
     */
    public static Conflict createMerge(Lane mLane, Lane pLane, Lane yLane) {
        mLane.addMergeLane(pLane);
        mLane.addMergeLane(yLane);
        return createMerge(pLane, yLane);
    }
    
    /**
     * Creates a merge conflict at the end of two lanes.
     * @param pLane Lane with priority.
     * @param yLane Lane without priority.
     * @return Merge conflict, <tt>null</tt> if lanes do not intersect.
     */
    protected static Conflict createMerge(Lane pLane, Lane yLane) {
        coord intersect;
        if (pLane.x[pLane.x.length-1]==yLane.x[yLane.x.length-1] &&
                pLane.y[pLane.y.length-1]==yLane.y[yLane.y.length-1]) {
            // equal end coordinates
            intersect = new coord(pLane.l, yLane.l, pLane.x[pLane.x.length-1], pLane.y[pLane.y.length-1]);
        } else {
            // find intersect
            intersect = intersect(pLane, yLane);
        }
        if (intersect==null) {
            return null;
        } else {
            coord[] clearance = findClearanceSpace(pLane, yLane, intersect, false);
            if (clearance!=null && clearance[0]!=null) {
                return new Conflict(pLane, intersect.x1, yLane, intersect.x2,  
                        false, conflictType.MERGE, intersect.x1-clearance[0].x1);
            } else {
                return null;
            }
        }
    }
    
    /**
     * Creates a split conflict at the start of two lanes <i>and</i> sets the
     * lanes as split lanes.
     * @param sLane Lane that other lanes will split from.
     * @param lane1 1st lane.
     * @param lane2 2nd lane.
     * @return Split conflict, <tt>null</tt> if lanes do not intersect.
     */
    public static Conflict createSplit(Lane sLane, Lane lane1, Lane lane2) {
        sLane.addSplitLane(lane1);
        sLane.addSplitLane(lane2);
        return createSplit(lane1, lane2);
    }
    
    /**
     * Creates a split conflict at the start of two lanes.
     * @param lane1 1st lane.
     * @param lane2 2nd lane.
     * @return Split conflict, <tt>null</tt> if lanes do not intersect.
     */
    protected static Conflict createSplit(Lane lane1, Lane lane2) {
        coord intersect;
        if (lane1.x[0]==lane2.x[0] && lane1.y[0]==lane2.y[0]) {
            // equal start coordinates
            intersect = new coord(0, 0, lane1.x[0], lane2.y[0]);
        } else {
            // find intersect
            intersect = intersect(lane1, lane2);
        }
        if (intersect==null) {
            return null;
        } else {
            coord[] clearance = findClearanceSpace(lane1, lane2, intersect, false);
            if (clearance!=null && clearance[1]!=null) {
                return new Conflict(lane1, clearance[1].x1, lane2, clearance[1].x2,
                        false, conflictType.SPLIT, clearance[1].x1);
            } else {
                return null;
            }
        }
    }
    
    /**
     * Creates a crossing conflict at the intersection of two lanes.
     * @param pLane Lane with priority.
     * @param yLane Lane without priority.
     * @param clear Whether to keep the crossing clear.
     * @return Crossing conflict, <tt>null</tt> if lanes do not intersect.
     */
    public static Conflict createCrossing(Lane pLane, Lane yLane, boolean clear) {
        coord intersect = intersect(pLane, yLane);
        if (intersect==null) {
            return null;
        } else {
            coord[] clearance = findClearanceSpace(pLane, yLane, intersect, true);
            if (clearance!=null && clearance[0]!=null && clearance[1]!=null) {
                return new Conflict(pLane, clearance[1].x1, yLane, clearance[1].x2,  
                        clear, conflictType.CROSSING, clearance[1].x1-clearance[0].x1);
            } else {
                return null;
            }
        }
    }
    
    /**
     * Sets the distance between lane centers where two conflict areas start or 
     * end. The default value is 2.625m which is determined as .5*(1.75+3.5), 
     * i.e. half of the vehicle and lane width, which allows the distance 
     * between a vehicle and the conflicting lane to be zero in the most 
     * critical situation (parallel lanes).
     * @param d Abslolute distance between the start and/or end of two conflict areas [m].
     */
    public static void setConflictDistance(double d) {
        D_CONF = d;
    }
    
    /**
     * Sets the distance between lane centers where two conflict areas start or
     * end to the default value of 2.625m which is determined as .5*(1.75+3.5),
     * i.e. half of the vehicle and lane width, which allows the distance 
     * between a vehicle and the conflicting lane to be zero in the most 
     * critical situation (parallel lanes).
     */
    public static void setDefaultConflictDistance() {
        D_CONF = 2.625;
    }
    
    /**
     * Returns the global and lane coordinates of the intersection of two lanes.
     * @param lane1 1st lane.
     * @param lane2 2nd lane.
     * @return Global and lane coordinates of the intersect of two lanes, 
     * <tt>null</tt> if lanes do not intersect.
     */
    protected static coord intersect(Lane lane1, Lane lane2) {
        // Loop sections of lane 1
        double xCumul1 = 0;
        for (int i=0; i<lane1.x.length-1; i++) {
            // Get parameters of lane 1 as: y1 = a1 + b1*x1
            double dx1 = lane1.x[i+1] - lane1.x[i];
            double dy1 = lane1.y[i+1] - lane1.y[i];
            double b1 = dy1/dx1;
            double a1 = lane1.y[i] - lane1.x[i]*b1;
            // Loop sections of lane 2
            double xCumul2 = 0;
            for (int j=0; j<lane2.x.length-1; j++) {
                // Get parameters of lane 2 as: y2 = a2 + b2*x2
                double dx2 = lane2.x[j+1] - lane2.x[j];
                double dy2 = lane2.y[j+1] - lane2.y[j];
                double b2 = dy2/dx2;
                double a2 = lane2.y[j] - lane2.x[j]*b2;
                
                // Find coordinates of intersect
                if (Double.isInfinite(b1) && Double.isInfinite(b2)) {
                    // Both vertical = no intersect
                } else {
                    double x;
                    double y;
                    if (Double.isInfinite(b1)) {
                        x = lane1.x[i];
                        y = a2 + b2*x;
                    } else if (Double.isInfinite(b2)) {
                        x = lane2.x[j];
                        y = a1 + b1*x;
                    } else {
                        x = -(a1-a2)/(b1-b2);
                        y = a1 + b1*x;
                    }
                    // Check whether intersect coordinate is on both lines
                    if (((x>=lane1.x[i] && x<=lane1.x[i+1]) || (x<=lane1.x[i] && x>=lane1.x[i+1])) &&
                            ((x>=lane2.x[j] && x<=lane2.x[j+1]) || (x<=lane2.x[j] && x>=lane2.x[j+1])) &&
                            ((y>=lane1.y[i] && y<=lane1.y[i+1]) || (y<=lane1.y[i] && y>=lane1.y[i+1])) &&
                            ((y>=lane2.y[j] && y<=lane2.y[j+1]) || (y<=lane2.y[j] && y>=lane2.y[j+1]))) {
                        // Calculate cumulative distances to intersect point
                        dx1 = x-lane1.x[i];
                        dy1 = y-lane1.y[i];
                        dx2 = x-lane2.x[j];
                        dy2 = y-lane2.y[j];
                        xCumul1 += Math.sqrt(dx1*dx1 + dy1*dy1);
                        xCumul2 += Math.sqrt(dx2*dx2 + dy2*dy2);
                        // Return the coordinates
                        return new coord(xCumul1, xCumul2, x, y);
                    }
                }
                // Next section on lane 2
                xCumul2 += Math.sqrt(dx2*dx2 + dy2*dy2);
            }
            // Next ssection on lane 1
            xCumul1 += Math.sqrt(dx1*dx1 + dy1*dy1);
        }
        // No intersect found
        return null;
    }
    
    /**
     * Returns coordinates for upstream and downstream locations (relative to 
     * intersection coordinates) where sufficient clearance is available.
     * @param lane1 1st lane.
     * @param lane2 2nd lane.
     * @param intersect Intersection coordinates.
     * @param crossing Whether the clearance is for a crossing (which allows direction reversal).
     * @return Coordinates of upstream and downstream locations (lane coordinates only).
     */
    protected static coord[] findClearanceSpace(Lane lane1, Lane lane2, coord intersect, boolean crossing) {
        java.util.ArrayList<coord> out = new java.util.ArrayList<coord>();
        // get curvature
        double[] xs1 = lane1.x;
        double[] ys1 = lane1.y;
        double[] xs2 = lane2.x;
        double[] ys2 = lane2.y;
        // reverse lane2 if angle of crossing is opposite (>90 degree)
        boolean reversed = false;
        double ang;
        if (crossing) {
            java.awt.geom.Point2D.Double h1 = lane1.heading(intersect.x1);
            java.awt.geom.Point2D.Double h2 = lane2.heading(intersect.x2);
            ang = Math.acos(h1.x*h2.x + h1.y*h2.y);
            if (ang>Math.PI/2) {
                reversed = true;
                int n = xs2.length;
                double[] xs2B = new double[n];
                double[] ys2B = new double[n];
                for (int i=0; i<n; i++) {
                    xs2B[i] = xs2[n-i-1];
                    ys2B[i] = ys2[n-i-1];
                }
                xs2 = xs2B;
                ys2 = ys2B;
                intersect.x2 = lane2.l - intersect.x2;
            }
        }
        // get initial sections of intersect coordinates
        double xCumul = 0;
        int i;
        for (i=0; i<xs1.length-1; i++) {
            double dx = xs1[i+1] - xs1[i];
            double dy = ys1[i+1] - ys1[i];
            xCumul += Math.sqrt(dx*dx + dy*dy);
            if (xCumul>=intersect.x1) {
                break;
            }
        }
        xCumul = 0;
        int j;
        for (j=0; j<xs2.length-1; j++) {
            double dx = xs2[j+1] - xs2[j];
            double dy = ys2[j+1] - ys2[j];
            xCumul += Math.sqrt(dx*dx + dy*dy);
            if (xCumul>=intersect.x2) {
                break;
            }
        }
        int iInit = i;
        int jInit = j;
        
        // Upstream
        if (intersect.x1==0 || intersect.x2==0) {
            // no upstream point
            out.add(null);
        } else {
            // Global end coordinate of current subsections (initially the intersect point)
            double xEnd1 = intersect.x;
            double xEnd2 = intersect.x;
            double yEnd1 = intersect.y;
            double yEnd2 = intersect.y;
            // Space between coordinates so far
            double d = 0;
            // Distance moved so far
            xCumul = 0;
            // Move upstream until sufficient space is found
            while (i>=0 && j>=0) {
                // Get lengths
                double dx1 = xEnd1 - xs1[i];
                double dy1 = yEnd1 - ys1[i];
                double dx2 = xEnd2 - xs2[j];
                double dy2 = yEnd2 - ys2[j];
                double len1 = Math.sqrt(dx1*dx1 + dy1*dy1);
                double len2 = Math.sqrt(dx2*dx2 + dy2*dy2);
                // Get angle and required distance
                double xUp;
                if (len1==0 || len2==0) {
                    // go to next section
                    xUp = Double.POSITIVE_INFINITY;
                } else {
                    ang = Math.acos((dx1*dx2 + dy1*dy2)/(len1*len2));
                    xUp = (.5*(D_CONF-d))/Math.sin(.5*ang);
                }
                if (xUp>len1 || xUp>len2) {
                    // Subsection length too short, move a subsection and 
                    // increase space between lanes so far.
                    if (len1<len2) {
                        xCumul += len1;
                        xEnd2 = xEnd2*(1-len1/len2) + xs2[j]*len1/len2;
                        yEnd2 = yEnd2*(1-len1/len2) + ys2[j]*len1/len2;
                        xEnd1 = xs1[i];
                        yEnd1 = ys1[i];
                        i--;
                        d += (D_CONF-d)*len1/xUp;
                    } else {
                        xCumul += len2;
                        xEnd1 = xEnd1*(1-len2/len1) + xs1[i]*len2/len1;
                        yEnd1 = yEnd1*(1-len2/len1) + ys1[i]*len2/len1;
                        xEnd2 = xs2[j];
                        yEnd2 = ys2[j];
                        j--;
                        d += (D_CONF-d)*len2/xUp;
                    }
                } else {
                    // Both sections are longer than required distance
                    xCumul += xUp;
                    out.add(new coord(intersect.x1-xCumul, intersect.x2-xCumul, 0, 0));
                    i=-1; // exit while loop
                }
            }
            // No appropriate point found
            if (out.isEmpty()) {
                out.add(null);
            }
        }
        
        // Downstream
        if (intersect.x1==lane1.l || intersect.x2==lane2.l) {
            // no downstream point
            out.add(null);
        } else {
            // get initial sections of coordinates
            i = iInit;
            j = jInit;
            // Start coordinate of current subsections (initially the intersect point)
            double xStart1 = intersect.x;
            double xStart2 = intersect.x;
            double yStart1 = intersect.y;
            double yStart2 = intersect.y;
            // Space between coordinates so far
            double d = 0;
            // Distance moved so far
            xCumul = 0;
            // Move downstream until sufficient space is found
            while (i<xs1.length-1 && j<xs2.length-1) {
                // Get lengths
                double dx1 = xs1[i+1] - xStart1;
                double dy1 = ys1[i+1] - yStart1;
                double dx2 = xs2[j+1] - xStart2;
                double dy2 = ys2[j+1] - yStart2;
                double len1 = Math.sqrt(dx1*dx1 + dy1*dy1);
                double len2 = Math.sqrt(dx2*dx2 + dy2*dy2);
                // Get angle and required distance
                double xDown;
                if (len1==0 || len2==0) {
                    // go to next section
                    xDown = Double.POSITIVE_INFINITY;
                } else {
                    ang = Math.acos((dx1*dx2 + dy1*dy2)/(len1*len2));
                    xDown = (.5*(D_CONF-d))/Math.sin(.5*ang);
                }
                if (xDown>len1 || xDown>len2) {
                    // Subsection length too short, move a subsection and 
                    // increase space between lanes so far.
                    if (len1<len2) {
                        xCumul += len1;
                        xStart2 = xStart2*(1-len1/len2) + xs2[j+1]*len1/len2;
                        yStart2 = yStart2*(1-len1/len2) + ys2[j+1]*len1/len2;
                        xStart1 = xs1[i+1];
                        yStart1 = ys1[i+1];
                        i++;
                        d += (D_CONF-d)*len1/xDown;
                    } else {
                        xCumul += len2;
                        xStart1 = xStart1*(1-len2/len1) + xs1[i+1]*len2/len1;
                        yStart1 = yStart1*(1-len2/len1) + ys1[i+1]*len2/len1;
                        xStart2 = xs2[j+1];
                        yStart2 = ys2[j+1];
                        j++;
                        d += (D_CONF-d)*len2/xDown;
                    }
                } else {
                    // Both sections are longer than required distance
                    xCumul += xDown;
                    out.add(new coord(intersect.x1+xCumul, intersect.x2+xCumul, 0, 0));
                    i=xs1.length; // exit while loop
                }
            }
            // No appropriate point found
            if (out.size()==1) {
                out.add(null);
            }
        }
        
        // Reverse results of 2nd lane if it was reversed
        if (reversed && out.get(0)!=null && out.get(1)!=null) {
            // switch x's of 2nd lane (up = down and vice versa)
            double x = out.get(0).x2;
            out.get(0).x2 = out.get(1).x2;
            out.get(1).x2 = x;
            // reverse length coordinates
            intersect.x2 = lane2.l - intersect.x2;
            out.get(0).x2 = lane2.l - out.get(0).x2;
            out.get(1).x2 = lane2.l - out.get(1).x2;
        }
        
        // Return coordinates
        return out.toArray(new coord[0]);
    }
    
    /**
     * Sets the visibility range on the minor road to start at given location.
     * @param lane Lane where visibility starts.
     * @param x Location on the lane where visibility starts [m].
     */
    public void setVisibilityFrom(Lane lane, double x) {
        vLane = lane;
        vX = x;
    }
    
    /**
     * RSU which makes drivers notice the conflict and behave accordingly.
     */
    public class conflictRSU extends SpeedReduction {
        
    	boolean marked = false;
    	
        /** Whether traffic from this side has priority. */
        private final boolean priority;
        
        /** Most downstream vehicle that is upstream of or partially on the conflict. */
        private Movable up;
        
        /** Set of merge conflicts where the nearest vehicle may be upstream of. */
        protected java.util.ArrayList<Conflict.conflictRSU> upstreamMergeConflicts = 
            new java.util.ArrayList<Conflict.conflictRSU>();
        
        /** Merge conflict which delivered an upstream vehicle. */
        protected Conflict.conflictRSU mergeConflictOfUp = null;
        
        /**
         * Constructor which sets the appropriate information.
         * @param lane Lane at which this RSU is located.
         * @param x Location on the lane [m].
         * @param priority Whether traffic from this lane has priority.
         */
        public conflictRSU(Lane lane, double x, boolean priority) {
            super(lane, x);
            this.priority = priority;
        }
        
        /**
         * Returns the first (partially) upstream vehicle of the conflict end.  
         * @return First (partially) upstream vehicle of the conflict end. 
         */
        public Movable up() {
            // check whether upstream vehicle has not been deleted
            if (!model.exists(up)) {
                up = null;
                control();
            }
            return up;
        }
        
        /**
         * Returns the first (partially) upstream vehicle of the coupled RSU.  
         * @return First (partially) upstream vehicle of the coupled RSU. 
         */
        public Movable otherUp() {
            if (this==p) {
                return y.up();
            } else {
                return p.up();
            }
        }
        
        /**
         * Returns the coupled RSU on the other lane of this conflict.
         * @return Coupled RSU on the other lane of this conflict.
         */
        public conflictRSU otherRSU() {
            if (this==p) {
                return y;
            } else {
                return p;
            }
        }
        
        /** 
         * Returns whether traffic on this lane has priority. 
         */
        public boolean isPriority() {
            return priority;
        }
                
        /** 
         * Returns whether vehicles should keep the conflict clear when being 
         * blocked. 
         */
        public boolean keepClear() {
            return clear;
        }
        
        /** 
         * Returns whether this conflict is a split conflict. 
         */
        public boolean isSplit() {
            return type==conflictType.SPLIT;
        }
        
        /** 
         * Returns whether this conflict is a merge conflict. 
         */
        public boolean isMerge() {
            return type==conflictType.MERGE;
        }
        
        /** 
         * Returns whether this conflict is a crossing conflict. 
         */
        public boolean isCrossing() {
            return type==conflictType.CROSSING;
        }
        
        /**
         * Returns the length of this conflict [m].
         * @return Length of this conflict [m].
         */
        public double length() {
            return length;
        }
        
        /**
         * Returns the range on the minor road where the major road is visible.
         * @return Visibility range on the minor road where the major road is visible [m].
         */
        public double visibility() {
            if (vLane!=null) {
                double dx = vLane.xAdj(y.lane);
                if (dx==0 && vLane!=y.lane) {
                    System.err.println("No visibility was set as the lanes "+y.lane.id+
                            " and "+vLane.id+" are not up- or downstream of one another.");
                } else {
                    visibility = y.x+dx - vX;
                }
                vLane = null;
            }
            return visibility;
        }

        /**
         * Finds and sets any upstream merge conflict where upstream vehicles 
         * may be found from. In this way, drivers may respond to conflicting 
         * vehicles that are nearby, but upstream of some intermediate merge 
         * (for example at a roundabout).
         */
        @Override
        public void init() {
            super.init();
            // move upstream
            Lane j = lane;
            while (j!=null && !j.isMerge()) {
                j = j.up;
            }
            // stopped as merge was found?
            if (j!=null && j.isMerge()) {
                for (Lane k : j.ups) {
                    // find appropriate rsu on lane
                	System.out.println("Checking up " + k.id);
                    double xRsu = 0;
                    boolean found = false;
                    while (!found) {
                        java.util.ArrayList<RSU> rsus = k.findNoticeableRSU(xRsu, k.l-xRsu);
                        for (RSU rsu : rsus) {
                            if (rsu instanceof Conflict.conflictRSU) {
                                Conflict.conflictRSU rsu2 = (Conflict.conflictRSU) rsu;
                                if (rsu2.isMerge()) {
                                    // add merge conflict to list of upstream merge conflicts
                                    upstreamMergeConflicts.add(rsu2);
                                    found = true;
                                    break;
                                }
                            }
                        }
                        System.out.println("isEmpty returns " + (rsus.isEmpty() ? "empty" : "not empty"));
                        if (!found && !rsus.isEmpty()) {
                            // next cross section with rsu(s)
                            xRsu = rsus.get(0).x;
                        }
                    }
                }
            }
        }

        /**
         * Maintain bookkeeping regarding which vehicle is upstream. The 
         * previous upstream vehicle may have passed the RSU fully, or may have 
         * split into another direction. If an upstream merge is closer than any
         * upstream vehicle, a vehicle on each merging lane is an upstream 
         * vehicle of this conflict. The vehicle that is expected to arrive 
         * earliest, assuming a fixed speed equal to the current speed, is 
         * selected.
         */
        @Override
        public void control() {
        	if (marked)
        		return;
        	marked = true;
            // remove jLcVehicle as up if the lane change has ended (vehicle==null)
            if (up!=null && up instanceof LCVehicle && ((LCVehicle) up).vehicle==null) {
                up = null;
            }
            // move downstream for as long as the leader is still upstream, this
            // leader may have changed lane
            if (up!=null) {
                while (up.down!=null && up.down.getDistanceToRSU(this)>0) {
                    up = up.down;
                }
            }
            // check existing vehicle
            if (up!=null && mergeConflictOfUp==null) {
                // upstream vehicle which is not upstream of a merge conflict
                if (up.lane==lane || up.lane.xAdj(lane)!=0) {
                    double s = up.getDistanceToRSU(this)+up.l;
                    if (s<0) {
                        // passed the rsu fully, no longer the upstream vehicle
                        up = null;
                    }
                } else {
                    // diverged at a split, no longer upstream of this rsu
                    up = null;
                }
            } else if (up!=null) {
                // upstream vehicle which is/was upstream of a merge conflict
                // either find upstream vehicle, not upstream of merge conflict,
                // or recheck time to enter the merge
                mergeConflictOfUp = null;
                up = null;
            }
            
            // find vehicle upstream of rsu
            if (up==null) {
                up = lane.findVehicle(x, Model.longDirection.UP);
            }
            // find vehicle upstream of upstream merge
            if (up==null) {
                double tte; // time to enter the merge by upstream vehicle
                double tteMin = Double.POSITIVE_INFINITY; 
                for (Conflict.conflictRSU rsu : upstreamMergeConflicts) {
                    Movable up2 = rsu.up();
                    if (up2!=null) {
                        // select if smallest tte
                        tte = up2.getDistanceToRSU(rsu)/up2.v;
                        if (tte<tteMin) {
                            up = up2;
                            tteMin = tte;
                            mergeConflictOfUp = rsu;
                        }
                    }
                }
            }
            marked = false;
        }
    }
    
    /**
     * Convenience class to return intersection/conflict coordinates of lanes.
     */
    protected static class coord {
        /** Coordinate on lane 1 [m]. */
        public double x1;
        
        /** Coordinate on lane 2 [m]. */
        public double x2;
        
        /** Global x-coordinate [m]. */
        public double x;
        
        /** Global y-coordinate [m]. */
        public double y;
        
        /**
         * Constructor which sets the coordinates.
         * @param x1 Coordinate on lane 1 [m].
         * @param x2 Coordinate on lane 1 [m].
         * @param x Global x-coordinate [m].
         * @param y Global y-coordinate [m].
         */
        public coord(double x1, double x2, double x, double y) {
            this.x1 = x1;
            this.x2 = x2;
            this.x = x;
            this.y = y;
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