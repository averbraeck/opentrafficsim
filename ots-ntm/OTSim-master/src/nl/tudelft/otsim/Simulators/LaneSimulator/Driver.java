package nl.tudelft.otsim.Simulators.LaneSimulator;

/**
 * Default wrapper for a driver. The drive method is the main method and should
 * set <tt>a</tt> and <tt>dy</tt> of the vehicle. The longitudinal model is 
 * situated in the calculateAcceleration method. For efficiency accelerations 
 * may be stored between any two vehicles. These can be used multiple times 
 * using the getAcceleration method.
 */
public class Driver {

    // ACCELERATIONS BOOKKEEPING
    /** Acceleration storage for use with the <tt>getAcceleration</tt> methods. */
    protected java.util.HashMap<Movable, java.util.HashMap<java.lang.String, Double>> accelerations =
            new java.util.HashMap<Movable, java.util.HashMap<java.lang.String, Double>>();

    // PARAMETERS
    /** Longitudinal stopping distance [m]. */
    public double s0 = 3;
    
    /** Longitudinal acceleration [m/s^2]. */
    public double a = 1.25;
    
    /** Regular longitudinal acceleration [m/s^2]. */
    public double aMin = 1.25;
    
    /** Longitudinal deceleration [m/s^2]. */
    public double b = 2.09;
    
    /** Maximum deceleration for v>vdes. */
    public double b0 = .5;
    
    /** Longitudinal regular following headway [s]. */
    public double Tmax = 1.2;

    /** LMRS free lane change threshold. */
    public double dFree = .365;
    
    /** LMRS synchronized lane change threshold. */
    public double dSync = .577;
    
    /** LMRS cooperative lane change threshold. */
    public double dCoop = .788;
    
    /** LMRS mandatory lane change time [s]. */
    public double t0 = 43;
    
    /** LMRS mandatory lane change distance [m]. */
    public double x0 = 295;
    
    /** LMRS speed gain [m/s] for full desire. */
    public double vGain = 69.6/3.6;
    
    /** LMRS critical speed [m/s] for a speed gain in the right lane. */
    public double vCong = 60/3.6;
    
    /** LMRS deceleration for lane changes (default: equal to <tt>b</tt>). */
    public double bSafe = 2.09;
    
    /** LMRS minimum time headway [s] for very desired lane change. */
    public double Tmin = .56;
    
    /** LMRS relaxation time [s]. */
    public double tau = 25;

    /** Maximum deceleration [m/s^2] for a yellow traffic light. */
    public double bYellow = 3.5;
    
    /** Maximum aceeleration [m/s^2] for intersections. */
    public double aInter = 2;
    
    /** (Maximum) distance [m] at which RSUs may be noticed. */
    protected double noticeableRange = 300;
    
    /** Speed limit adherence factor. */
    public double fSpeed = 1;
    
    /** Duration [s] of a lane change. */
    public double duration = 3;
    
    /** Factor applied to time estimates at conflicts. */
    public double estTimeFactor = 1.25;
    
    /** Stopping distance for conflicts, for numerical [m]. */
    public double s0conflict = .5;
    
    /** Whether this driver is willing to yield with isPriority in certain circumstances. */
    public boolean yieldWithPriority = true;
    
    // LMRS BOOKKEEPING
    /** Temporary T value between <tt>setT</tt> and <tt>resetT</tt> invocations. */
    protected double Ttmp = Tmax;
    
    /** LMRS lane change desire to the left. */ 
    public double dLeft = 0;
    
    /** LMRS lane change desire to the right. */
    public double dRight = 0;
    
    // DESIRED VELOCITY BOOKKEEPING
    /** Current desired velocity. Use <tt>desiredVelocity()</tt> to get it. */
    protected double vDes;

    // SYNCHRONIZATION
    /** Whether driver is synchronizing with the left lane. */
    public boolean leftSync = false;
    
    /** Whether driver is synchronizing with the right lane. */
    public boolean rightSync = false;
    
    /** Whether driver is yielding for a vehicle in the right lane (for a left lane change). */
    public boolean leftYield = false;
    
    /** Whether driver is yielding for a vehicle in the left lane (for a right lane change). */
    public boolean rightYield = false;
    
    // ANTICIPATED SPEED BOOKKEEPING
    /** Bookkeeping of influence on anticipated speed from a left lane. */
    protected java.util.HashMap<Lane, Double> antFromLeft = new java.util.HashMap<Lane, Double>();
    
    /** Bookkeeping of influence on anticipated speed from given lane. */
    protected java.util.HashMap<Lane, Double> antInLane = new java.util.HashMap<Lane, Double>();
    
    /** Bookkeeping of influence on anticipated speed from a right lane. */
    protected java.util.HashMap<Lane, Double> antFromRight = new java.util.HashMap<Lane, Double>();
    
    // CONFLICT BOOKKEEPING
    /** Set of vehicles that is yielded for, per conflict (public for visualization purposes). */
    protected java.util.HashMap<Conflict.conflictRSU, Movable> conflictYieldPlans = 
            new java.util.HashMap<Conflict.conflictRSU, Movable>();
    
    /** Whether further conflicts can be ignored in the current time step. */
    protected boolean ignoreFurtherConflicts = false;
    
    // QUEUE LANE CHANGE DESIRE
    /** Left lane change desire for queues, set in <tt>notice</tt> method. */
    protected double dLeftIntersection = 0;
    
    /** Right lane change desire for queues, set in <tt>notice</tt> method. */
    protected double dRightIntersection = 0;
    
    /** Whether this driver is blocked for a conflict. */
    protected boolean conflictBlocked;
    
    /** 
     * Set of conflicts that may not be stopped on, i.e. must be stopped in 
     * front of if downstream conflict requires this.
     */
    protected java.util.ArrayList<Conflict.conflictRSU> keepClearConflicts = 
            new java.util.ArrayList<Conflict.conflictRSU>();

    // OTHER
    /** Current value for the headway [s], initiated at <tt>Tmax</tt>. */
    protected double T = Tmax;

    /** Vehicle of the driver. */
    public Vehicle vehicle;
    
    /** Set of time steps when certain actions were performed last. */
    protected java.util.HashMap<String, Integer> kForActions = new java.util.HashMap<String, Integer>();
    
    /**
     * Constructor which links the driver with a vehicle and vice versa.
     * @param vehicle Vehicle of the driver.
     */
    // using this at the end is ok, driver is fully initialized
    public Driver(Vehicle vehicle) {
        this.vehicle = vehicle;
        vehicle.driver = this;
        vehicle.setRSURange(noticeableRange);
    }
    
    /**
     * Sets the range within which RSU are noticed.
     * @param noticeableRange Range within which RSU are noticed [m].
     */
    public void setNoticeableRange(double noticeableRange) {
        this.noticeableRange = noticeableRange;
        vehicle.setRSURange(noticeableRange);
    }

    /**
     * Returns whether the current time step is a new time step and the given
     * action should be performed. This enables the initialization of 
     * bookkeeping each time step for various actions.<br>
     * <br>
     * Be careful with actions that may be performed for multiple reasons. The 
     * last execution time of the action is not updated if the action is 
     * performed without invoking this method, i.e. the action could be 
     * performed again in the same time step, leading to undefined results. To 
     * achieve correct behavior, invoke this method as the first logical 
     * statement in an 'or' statement.
     * @param action Unique name for an action.
     * @return Whether it is a new time step for the action.
     */
    protected boolean isNewTimeStepForAction(String action) {
        boolean out = true;
        try {
            if (vehicle.model.k<=kForActions.get(action)) {
                out =  false;
            }
        } catch (NullPointerException npe) {
            // action never performed, so true
        }
        kForActions.put(action, vehicle.model.k);
        return out;
    }
    
    /**
     * Driver behavior for acceleration and lane changes. This is the main
     * function that sets <code>a</code> (acceleration) and <code>dy</code>
     * (lateral change) of the vehicle. The former is set using 
     * <tt>vehicle.setAcceleration(a)</tt> and the latter is either set using 
     * <tt>vehicle.changeLeft(dy)</tt> or <tt>vehicle.changeRight(dy)</tt>. It 
     * combines the Intelligent Driver Model+ (IDM+) and the Lane-change Model 
     * with Relaxation and Synchronisation (LMRS).<br>
     * <br>
     * IDM+<br>
     * This is the longitudinal car-following model that determines acceleration
     * based on the gap, velocity and relative velocity.<br>
     * <br>
     * LMRS<br>
     * This is the lateral lane change model that is based on lane change desire
     * based on incentives of: following a route, gaining speed, and following
     * traffic rules. Depending on the level of desire a vehicle may start to
     * synchronize with the target lane, or a gap may even be created.<br>
     * An additional voluntary lane change incentive is intersection lane change 
     * desire which is set in <tt>notice()</tt> methods.
     */
    public void drive() {
        
        // Notice RSU's
        noticeRSUs();
        
        // Set interaction booleans as false by default
        leftSync = false; // for visualisation only
        rightSync = false;
        leftYield = false;
        rightYield = false;
        vehicle.leftIndicator = false; // for vehicle interaction
        vehicle.rightIndicator = false;
        
        // Perform the lane change model only when not changing lane
        if (vehicle.lcProgress == 0) {

            /* The headway is exponentially relaxed towards the normal value of
             * Tmax. The relaxation time is tau, which can never be smaller than
             * the time step. Smaller values of T can be set within the model.
             */
            T = T + (Tmax-T)*vehicle.model.dt/(tau>=vehicle.model.dt?tau:vehicle.model.dt);

            /* A lane change is not considered over the first 100m of lanes with
             * generators. Vehicles that are (virtually) upstream of the network
             * would influence this decision so the model is not valid here.
             */
            if (vehicle.x>100 || vehicle.lane.generator==null) {
                
                /* === ROUTE ===
                 * First, the lane change desire to leave a lane for the route
                 * on the left, current and right lane is determined based on
                 * remaining distance and remaining time.
                 */
                // Get remaining distance and number of lane changes from lane
                double xCur = vehicle.route.xLaneChanges(vehicle.lane) - vehicle.x;
                int nCur = vehicle.route.nLaneChanges(vehicle.lane);
                /* Desire to leave the current lane is eiter based on remaining
                 * distance or time. For every lane change required, a certain
                 * distance or time is desired.
                 */
                // Towards the left, always ignore taper on current lane
                double dCurRouteFL = Math.max(Math.max(1-(xCur/(nCur*x0)),
                        1-((xCur/vehicle.v)/(nCur*t0))), 0);
                // Towards the right, no desire if on taper
                double dCurRouteFR = 0;
                if (!isOnTaper()) {
                    dCurRouteFR = dCurRouteFL;
                }
                /* Determine desire to leave the left lane if it exists and
                 * allows to follow the route.
                 */
                double dLeftRoute = 0;
                if (vehicle.lane.left!=null && vehicle.route.canBeFollowedFrom(vehicle.lane.left)) {
                    // The first steps are similar as in the current lane
                    int nLeft = vehicle.route.nLaneChanges(vehicle.lane.left);
                    double xLeft = vehicle.route.xLaneChanges(vehicle.lane.left)
                            - vehicle.getAdjacentX(Model.latDirection.LEFT);
                    // We can always include a taper on the left lane if it's there
                    if (!isTaper(vehicle.lane.left)) {
                        dLeftRoute = Math.max(Math.max(1-(xLeft/(nLeft*x0)),
                                1-((xLeft/vehicle.v)/(nLeft*t0))), 0);
                    }
                    
                    /* We now have the desire to leave the current, and to leave
                     * the left lane. 'dLeftRoute' will now become the actual
                     * desire to change to the left lane by comparing the two.
                     * If desire to leave the left lane is lower, the desire to
                     * leave the current lane is used. If they are equal, the
                     * desire is zero. If the left lane is worse, the desire to
                     * go left is negative and equal to the desire to leave the
                     * left lane. In this way we have symmetry which prevents
                     * lane hopping.
                     */
                    if (dLeftRoute<dCurRouteFL) {
                        dLeftRoute = dCurRouteFL;
                    } else if (dLeftRoute>dCurRouteFL) {
                        dLeftRoute = -dLeftRoute;
                    } else {
                        dLeftRoute = 0;
                    }
                } else {
                    // Destination becomes unreachable after lane change
                    dLeftRoute = Double.NEGATIVE_INFINITY;
                }
                // Idem. for right lane
                double dRightRoute = 0;
                if (vehicle.lane.right!=null && vehicle.route.canBeFollowedFrom(vehicle.lane.right)) {
                    int nRight = vehicle.route.nLaneChanges(vehicle.lane.right);
                    double xRight = vehicle.route.xLaneChanges(vehicle.lane.right)
                            - vehicle.getAdjacentX(Model.latDirection.RIGHT);
                    // A taper on the right lane is never applicable
                    dRightRoute = Math.max(Math.max(1-(xRight/(nRight*x0)),
                            1-((xRight/vehicle.v)/(nRight*t0))), 0);
                    
                    if (dRightRoute<dCurRouteFR) {
                        dRightRoute = dCurRouteFR;
                    } else if (dRightRoute>dCurRouteFR) {
                        dRightRoute = -dRightRoute;
                    } else {
                        dRightRoute = 0;
                    }
                } else {
                    dRightRoute = Double.NEGATIVE_INFINITY;
                }

                /* === SPEED GAIN ===
                 * Drivers may change lane to gain speed. They assess an
                 * anticipated speed.
                 */
                // Get anticipated speeds in current and adjacent lanes
                double vAntLeft = anticipatedSpeed(vehicle.lane.left);
                double vAntCur = anticipatedSpeed(vehicle.lane);
                double vAntRight = anticipatedSpeed(vehicle.lane.right);
                /* An acceleration factor is determined. As drivers accelerate
                 * more, their lane change desire for speed reduces. This
                 * prevents slow accelerating vehicles from changing lane when
                 * being overtaken by fast accelerating vehicles. This would
                 * otherwise cause unreasonable lane changes.
                 */
                double aGain = (a-Math.max(calculateAcceleration(vehicle, vehicle.down),0))/a;
                /* Desire to the left is related to a possible speed gain/loss.
                 * The parameter vGain determines for which speed gain the
                 * desire would be 1. Desire is 0 if there is no left lane or if
                 * it is prohibited or impossible to go there.
                 */
                double dLeftSpeed = 0;
                if (vehicle.lane.goLeft) {
                    dLeftSpeed = aGain*(vAntLeft-vAntCur)/vGain;
                }
                /* For the right lane, desire due to a speed gain is slightly
                 * different. As one is not allowed to overtake on the right, a
                 * speed gain is reduced to 0. A speed loss is still considered.
                 * For this, traffic should be free flow as one may overtake on
                 * the right in congestion.
                 */
                double dRightSpeed = 0;
                if (vehicle.lane.goRight) {
                    if (vAntCur>=vCong) {
                        dRightSpeed = aGain*Math.min(vAntRight-vAntCur, 0)/vGain;
                    } else {
                        dRightSpeed = aGain*(vAntRight-vAntCur)/vGain;
                    }
                }
     
                /* === LANE CHANGE BIAS ===
                 * Drivers have to keep right. It is assumed that this is only
                 * obeyed in free flow and when the anticipated speed on the
                 * right lane is equal to the desired speed. Or in other words,
                 * when there is no slower vehicle nearby in the right lane.
                 * The bias is equal to the free threshold, just triggering
                 * drivers to change in free conditions. Another condition is
                 * that there should be no route undesire towards the right
                 * whatsoever.
                 */
                double dLeftBias = 0;
                double dRightBias = 0;
                if (vAntRight==desiredVelocity() && dRightRoute>=0) {
                    dRightBias = dFree;
                }
                
                /* === TOTAL DESIRE ===
                 * Depending on the level of desire from the route (mandatory),
                 * the speed and keep right (voluntary) incentives may be
                 * included partially or not at all. If the incentives are in
                 * the same direction, voluntary incentives are fully included.
                 * Otherwise, the voluntary incentives are less and less
                 * considered within the range dSync < |dRoute| < dCoop. The
                 * absolute value of dRouite is used as negative values may also
                 * dominate voluntary incentives.
                 */
                double thetaLeft = 0; // Assume not included
                double dVoluntary = dLeftSpeed+dLeftBias+dLeftIntersection;
                if (dLeftRoute*dVoluntary>=0 || Math.abs(dLeftRoute)<=dSync) {
                    // Same direction or low mandatory desire
                    thetaLeft = 1;
                } else if (dLeftRoute*dVoluntary<0 && 
                        dSync<Math.abs(dLeftRoute) && Math.abs(dLeftRoute)<dCoop) {
                    // Voluntary incentives paritally included
                    thetaLeft = (dCoop-Math.abs(dLeftRoute)) / (dCoop-dSync);
                }
                dLeft = dLeftRoute + thetaLeft*dVoluntary;
                // Idem. for right
                double thetaRight = 0;
                dVoluntary = dRightSpeed+dRightBias+dRightIntersection;
                if (dRightRoute*dVoluntary>=0 || Math.abs(dRightRoute)<=dSync) {
                    thetaRight = 1;
                } else if (dRightRoute*dVoluntary<0 &&
                        dSync<Math.abs(dRightRoute) && Math.abs(dRightRoute)<dCoop) {
                    thetaRight = (dCoop-Math.abs(dRightRoute)) / (dCoop-dSync);
                }
                dRight = dRightRoute + thetaRight*dVoluntary;

                /* === GAP ACCEPTANCE ===
                 * A gap is accepted or rejected based on the resulting
                 * acceleration of the driver itself and the potential follower.
                 */
                // Determine own acceleration
                double aSelf = 0; // assume ok
                if (vehicle.leftDown!=null && vehicle.getHeadway(vehicle.leftDown)>0) {
                    // Use car-following model
                    setT(dLeft);
                    aSelf = calculateAcceleration(vehicle, vehicle.leftDown);
                    resetT();
                } else if (vehicle.leftDown!=null) {
                    // Negative headway, reject gap
                    aSelf = Double.NEGATIVE_INFINITY;
                }
                // Determine follower acceleration
                double aFollow = 0; // assume ok
                if (vehicle.leftUp != null) {
                    if (vehicle.leftUp.getHeadway(vehicle)>0) {
                        vehicle.leftUp.getDriver().setT(dLeft);
                        aFollow = calculateAcceleration(vehicle.leftUp, vehicle);
                        vehicle.leftUp.getDriver().resetT();
                    } else {
                        aFollow = Double.NEGATIVE_INFINITY;
                    }
                }
                /* The gap is accepted if both accelerations are larger than a
                 * desire depedant threshold.
                 */
                boolean acceptLeft = false;
                if (aSelf >= -bSafe*dLeft && aFollow >= -bSafe*dLeft && vehicle.lane.goLeft) {
                    acceptLeft = true;
                }
                // Idem. for right gap
                aSelf = 0;
                if (vehicle.rightDown!=null && vehicle.getHeadway(vehicle.rightDown)>0) {
                    setT(dRight);
                    aSelf = calculateAcceleration(vehicle, vehicle.rightDown);
                    resetT();
                } else if (vehicle.rightDown!=null) {
                    aSelf = Double.NEGATIVE_INFINITY;
                }
                aFollow = 0;
                if (vehicle.rightUp != null) {
                    if (vehicle.rightUp.getHeadway(vehicle)>0) {
                        vehicle.rightUp.getDriver().setT(dRight);
                        aFollow = calculateAcceleration(vehicle.rightUp, vehicle);
                        vehicle.rightUp.getDriver().resetT();
                    } else {
                        aFollow = Double.NEGATIVE_INFINITY;
                    }
                }
                boolean acceptRight = false;
                if (aSelf >= -bSafe*dRight && aFollow >= -bSafe*dRight && vehicle.lane.goRight) {
                    acceptRight = true;
                }

                /* LANE CHANGE DECISION
                 * A lane change is initiated towards the largest desire if that
                 * gap is accepted. If the gap is rejected, the turn indicator 
                 * may be turned on.
                 */
                if (dLeft>=dRight && dLeft>=dFree && acceptLeft) {
                    // Set dy to the left
                    double dur = Math.min((vehicle.route.xLaneChanges(vehicle.lane)-vehicle.x)/(180/3.6), duration);
                    vehicle.changeLeft(vehicle.model.dt/dur);
                    // Set headway
                    setT(dLeft);
                    // Set response headway of new follower
                    if (vehicle.leftUp != null && vehicle.leftUp.rightDown==vehicle) {
                        vehicle.leftUp.getDriver().setT(dLeft);
                    }
                } else if (dRight>=dLeft && dRight>=dFree && acceptRight) {
                    // Set dy to the right
                    double dur = Math.min((vehicle.route.xLaneChanges(vehicle.lane)-vehicle.x)/(180/3.6), duration);
                    vehicle.changeRight(vehicle.model.dt/dur);
                    // Set headway
                    setT(dRight);
                    if (vehicle.rightUp != null && vehicle.rightUp.leftDown==vehicle) {
                        vehicle.rightUp.getDriver().setT(dRight);
                    }
                } else if (dLeft>=dRight && dLeft>=dCoop) {
                    // Indicate need to left
                    vehicle.leftIndicator = true;
                } else if (dRight>=dLeft && dRight>=dCoop) {
                    // Indicate need to right
                    vehicle.rightIndicator = true;
                }
            } // not on first 100m
            
            /* === LONGITUDINAL ===
             * Follow all applicable vehicles and use lowest acceleration.
             */
            // Follow leader (regular car following)
            lowerAcceleration(calculateAcceleration(vehicle, vehicle.down));
            // Synchronize to perform a lane change
            if (dLeft>=dSync && dLeft>=dRight && vehicle.leftDown!=null) {
                // Apply shorter headway for synchronization
                setT(dLeft);
                lowerAcceleration(safe(calculateAcceleration(vehicle, vehicle.leftDown)));
                resetT();
                leftSync = true;
            } else if (dRight>=dSync && dRight>dLeft && vehicle.rightDown!=null) {
                // Apply shorter headway for synchronization
                setT(dRight);
                lowerAcceleration(safe(calculateAcceleration(vehicle, vehicle.rightDown)));
                resetT();
                rightSync = true;
            }
            // Synchronize to create a gap
            if (vehicle.leftDown!=null && vehicle.leftDown.rightUp==vehicle &&
                    vehicle.leftDown.rightIndicator) {
                // Apply shorter headway for gap-creation
                setT(vehicle.leftDown.getDriver().dRight);
                lowerAcceleration(safe(calculateAcceleration(vehicle, vehicle.leftDown)));
                resetT();
                rightYield = true; // deals with right lane change of other
            }
            if (vehicle.rightDown!=null && vehicle.rightDown.leftUp==vehicle &&
                    vehicle.rightDown.leftIndicator) {
                // Apply shorter headway for gap-creation
                setT(vehicle.rightDown.getDriver().dLeft);
                lowerAcceleration(safe(calculateAcceleration(vehicle, vehicle.rightDown)));
                resetT();
                leftYield = true; // deals with left lane change of other
            }

        } else {

            // Performing a lane change, simply follow both leaders
            lowerAcceleration(calculateAcceleration(vehicle, vehicle.down));
            lowerAcceleration(calculateAcceleration(vehicle, vehicle.lcVehicle.down));
        }
    }
    
    /**
     * Sets the acceleration of the vehicle only if the given value is lower 
     * than the current value or in case of a new time step.
     * @param a Vehicle acceleration [m/s^2].
     */
    public void lowerAcceleration(double a) {
        if (isNewTimeStepForAction("lower_acceleration") || a<vehicle.a) {
            vehicle.setAcceleration(a);
        }
    }
    
    /**
     * Returns a limited value of acceleration which remains comfortable and 
     * safe according to a >= -bSafe. This limit can be used for synchronization
     * with an adjacent lane and not for car-following as 'safe' does not mean
     * collision free, but safe with regard to upstream followers.
     * @param a Acceleration as calculated for an adjacent leader [m/s^2].
     * @return Limited safe acceleration [m/s^2].
     */
    public double safe(double a) {
        return a >= -bSafe ? a : -bSafe;
    }

    /**
     * Return the current value of the internal use headway value [s].
     * @return Current value of the internal use headway value [s].
     */
    public double T() {
        return T;
    }
    
    /**
     * Sets T depending on the level of lane change desire. This method will
     * never increase the current T value. Lane change desire may be of another
     * driver. If the T value is only set for an evaluation that does not result
     * in an actual action, the value for T should be reset using
     * <code>resetT()</code>. If one calls this method consequentially without
     * calling the reset method in between, the regular car following headway
     * becomes lost.
     * @param d Desire for lane change.
     */
    public void setT(double d) {
        Ttmp = T;
        if (d>0 && d<1) {
            double Tint = d*Tmin + (1-d)*Tmax;
            T = T <= Tint ? T : Tint;
        } else if (d<=0) {
            T = T <= Tmax ? T : Tmax;
        } else {
            T = T <= Tmin ? T : Tmin;
        }
    }

    /**
     * Resets the T value to regular car-following. The T value for this is
     * stored when <code>setT(d)</code> is called.
     */
    public void resetT() {
        T = Ttmp;
    }

    /**
     * Returns an anticipated speed based on the speed limit, maximum vehicle
     * speed and the speed of leaders. Vehicles on adjacent lanes with 
     * indicators turned on towards the lane are also considered. Internal
     * bookkeeping prevents multiple loops over leading vehicles on any
     * particular lane by storing the anticipation speed for adjacent lanes.
     * @param lane Lane at which an aticipated speed is required.
     * @return Anticipated speed on lanes [left, current, right].
     */
    protected double anticipatedSpeed(Lane lane) {
        if (lane==null) {
            return 0;
        }
        // Be sure we exclude the vehicle itself
        double x = vehicle.x+0.001;
        // In case of an adjacent lane, get the adjacent x
        if (lane!=vehicle.lane) {
            if (lane.right!=null && lane.right==vehicle.lane) {
                x = vehicle.getAdjacentX(Model.latDirection.LEFT); // lane = left lane of vehicle
            } else {
                x = vehicle.getAdjacentX(Model.latDirection.RIGHT); // lane = right lane of vehicle
            }
        }
        // Clear bookkeeping in case of new time step
        if (isNewTimeStepForAction("anticipation_speed")) {
            antFromLeft.clear();
            antInLane.clear();
            antFromRight.clear();
        }
        // Initialize as infinite
        double vLeft = Double.POSITIVE_INFINITY;
        double vCur = Double.POSITIVE_INFINITY;
        double vRight = Double.POSITIVE_INFINITY;
        // Calculate anticipation speed in current lane
        if (!antInLane.containsKey(lane)) {
            anticipatedSpeedFromLane(lane, x);
        }
        vCur = antInLane.get(lane); // all in lane
        // Calculate anticipation speed in left lane
        if (lane.left!=null) {
            if (!antFromLeft.containsKey(lane)) {
                double xleft = lane.getAdjacentX(x, Model.latDirection.LEFT);
                anticipatedSpeedFromLane(lane.left, xleft);
            }
            vLeft = antFromLeft.get(lane); // indicators only
        }
        // Calculate anticipation speed in right lane
        if (lane.right!=null) {
            if (!antFromRight.containsKey(lane)) {
                double xright = lane.getAdjacentX(x, Model.latDirection.RIGHT);
                anticipatedSpeedFromLane(lane.right, xright);
            }
            vRight = antFromRight.get(lane); // indicators only
        }
        // Return minimum of all
        return Math.min(vCur, Math.min(vLeft, vRight));
    }

    /**
     * Supporting method that loops all leaders in a lane and calculates their
     * influence on the given and adjacent lanes, where only vehicles with
     * indicators turned on are considered for the adjacent lanes. This method
     * stores the information for up to three lanes, preventing multiple loops
     * over the leaders in the requested lane. These speeds can be retreived
     * from the hashmaps <code>antFromLeft</code>, <code>antInLane</code> and
     * <code>antFromRight</code> by using their <code>.get(jLane)</code> method
     * where the lane is the lane for which the anticipation speed is required.
     * @param lane Requested lane of influence on this or an adjacent lane.
     * @param x Location on the lane.
     */
    protected void anticipatedSpeedFromLane(Lane lane, double x) {
        // Initialize as desired velocity (= no influence).
        double vLeft = desiredVelocity();
        double vCur = desiredVelocity();
        double vRight = desiredVelocity();
        // Find first leader
        Movable down = lane.findVehicle(x, Model.longDirection.DOWN);
        double s = 0;
        double v = 0;
        double v0 = desiredVelocity();
        // Loop leaders while within anticipation region
        while (down!=null && s<=x0) {
            // interpolate from "v(s=x0) = vDes" to "v(s=0) = down.v" e.g.
            // with headway = 0 take vehicle fully into account, and with
            // headway > x0 ignore vehicle, in between interpolate linearly
            s = down.x+lane.xAdj(down.lane) - down.l - x;
            // only consider if new headway is within consideration range and
            // speed is below the desired speed, otherwise there is no influence
            if (s<=x0 && down.v<v0 && down!=vehicle) {
                // influence of a single vehicle
                v = (1-(s/x0))*v + (s/x0)*v0;
                // take minimum
                vCur = Math.min(vCur, v);
                /* Indicators from the current lane are not included for the
                 * anticipated speeds of the adjacent lanes. This is to prevent
                 * a slow queue adjacent to an empty lane, where the anticipated
                 * speeds are then consequently low.
                 */
                if (down.leftIndicator && !lane.isSameLane(vehicle.lane)) {
                    vLeft = Math.min(vLeft, v);
                } else if (down.rightIndicator && !lane.isSameLane(vehicle.lane)) {
                    vRight = Math.min(vRight, v);
                }
            }
            // go to next vehicle
            down = down.down;
        }
        // store anticipated speeds
        if (lane.right!=null) {
            antFromLeft.put(lane.right, vRight); // this lane is the left lane of lane.right
        }
        antInLane.put(lane, vCur);
        if (lane.left!=null) {
            antFromRight.put(lane.left, vLeft); // this lane is the right lane of lane.left
        }
    }

    /**
     * Determine acceleration based on specific vehicle. If this has been
     * calculated this time step, take previously calculated value. Stored
     * accelerations are automatically cleared each time step. This is useful if
     * acceleration between a set of vehicles is needed multiple times within
     * each time step. Otherwise one should circumvent the look-up and store
     * procedure by directly using <code>calculateAcceleration()</code>. Note
     * that by supplying a follower, the <code>getAcceleration()</code> will be
     * called on the correct driver, which may be different from the original
     * driver on which it is called.
     * @param follower Vehicle whos acceleration is required.
     * @param leader Acceleration is based on this vehicle.
     * @return Acceleration [m/s^2].
     */
    public double getAcceleration(Movable follower, Movable leader) {
        // default: without type label
        return getAcceleration(follower, leader, "");
    }
    
    /**
     * Determine acceleration based on specific vehicle and label. If this has
     * been calculated this time step, take previously calculated value. Stored
     * accelerations are automatically cleared each time step. The label can be
     * used to distinct between different acceleration types when storing or
     * retreiving them from memory. Note that to calculate actual different
     * values, parameters need to be set before <code>getAcceleration()</code>
     * is called. Note that by supplying a follower, the <code>getAcceleration()
     * </code> will be called on the correct driver, which may be different from
     * the original driver on which it is called.
     * @param follower Vehicle of considered driver.
     * @param leader Acceleration is based on this vehicle.
     * @param label Label for type of acceleration (e.g. following, lane change)
     * used for storage.
     * @return Acceleration [m/s^2].
     */
    public double getAcceleration(Movable follower, Movable leader, java.lang.String label) {
        double acc = 0;
        if (follower.getDriver()==this || (vehicle.lcProgress!=0 && follower==vehicle.lcVehicle)) {
            // reset stored accelerations each time step
            if (isNewTimeStepForAction("calculate_acceleration")) {
                accelerations.clear();
            }
            if (!vehicle.model.isGenerating() && accelerations.containsKey(leader) && accelerations.get(leader).containsKey(label)) {
                // get acceleration that has allready been calculated
                acc = accelerations.get(leader).get(label);
            } else if (vehicle.model.isGenerating()) {
                // calculate and do not store acceleration as vehicle generation
                // may just be testing the current location
                acc = calculateAcceleration(leader);
            } else {
                // calculate and store acceleration
                acc = calculateAcceleration(leader);
                if (accelerations.containsKey(leader)) {
                    accelerations.get(leader).put(label, acc); // may include 'null' as leader
                } else {
                    java.util.HashMap<java.lang.String, Double> submap =
                            new java.util.HashMap<java.lang.String, Double>();
                    submap.put(label, acc);
                    accelerations.put(leader, submap);
                }
             }
        } else {
            // acceleration from other vehicle, find correct driver
            if (follower.getDriver()==null) {
                throw new java.lang.RuntimeException("No driver found: "+follower.x+"@"+
                        follower.lane.id+" - "+leader.x+"@"+leader.lane.id+", vehicle was deleted.");
            }
            acc = follower.getDriver().getAcceleration(follower, leader);
        }
        return acc;
    }

    /**
     * Same as <code>calculateAcceleration()</code> with single input. This
     * method additionally searches for the correct driver in the following
     * vehicle.
     * @param follower Vehicle of considered driver.
     * @param leader Acceleration is based on this vehicle.
     * @return Acceleration [ms/^2].
     */
    public static double calculateAcceleration(Movable follower, Movable leader) {
        return follower.getDriver().calculateAcceleration(leader);
    }
    
    /**
     * Calculation of acceleration based on a specific leader. The default is
     * the IDM+ car following model.
     * @param leader Acceleration is based on this vehicle.
     * @return Acceleration [m/s^2].
     */
    public double calculateAcceleration(Movable leader) {
        // Longitudinal model
        // get input
        double v = vehicle.v; // own speed [m/s]
        double s; // net headway [m]
        double dv; // speed difference [m/s]
        double v0 = desiredVelocity();
        if (leader!=null) {
            s = vehicle.getHeadway(leader);
            dv = v-leader.v;
        } else {
            s = Double.POSITIVE_INFINITY;
            dv = 0;
        }
        // calculate acceleration
        return longitudinal(v, dv, v0, s);
    }

    /**
     * Calculates the acceleration towards an object.
     * @param v Own velocity.
     * @param dv Velocity difference with object.
     * @param v0 Desired velocity.
     * @param s Distance to object.
     * @return Acceleration towards object.
     */
    public double longitudinal(double v, double dv, double v0, double s) {
        double ss = desiredEquilibriumHeadway()+(v*dv)/(2*Math.sqrt(a*b)); // dynamic desired gap
        /* Because of the power of 2, the longitudinal inteprets a negative sign of
         * either s and ss as positive. In all cases this makes no sense.
         */
        ss = ss>=0 ? ss : 0;
        s = s>1e-99 ? s : 1e-99; // no division by zero
        double aFree = aFree(v, v0);
        double sf = ss/s;
        double aInt = a*(1 - sf*sf);
        return aInt<aFree ? aInt : aFree;
    }
    
    /**
     * Returns the acceleration if there is no leading vehicle.
     * @param v Current speed [m/s].
     * @param v0 Desired speed [m/s].
     * @return Acceleration if there is no leading vehicle.
     */
    protected double aFree(double v, double v0) {
        double aFree = a*(1-Math.pow((v/v0),4));
        return aFree>=-b0 ? aFree : -b0;
    }

    /**
     * The desired headway method returns the desired <b>equilibrium</b> net
     * headway. It is used by the vehicle generator, and possibly the driver
     * itself.
     * @return Desired net headway [m]
     */
    public double desiredEquilibriumHeadway() {
        double v = vehicle.v;
        return s0 + v*T;
    }

    /**
     * This method returns the desired speed on the current lane. It is used by
     * the vehicle generator, and possibly the driver itself.
     * @return Desired velocity [m/s]
     */
    public double desiredVelocity() {
        if (isNewTimeStepForAction("desired_velocity")) {
            vDes = desiredVelocity(vehicle.lane);
        }
        return vDes;
    }
    
    /**
     * This method returns the desired speed on the given lane.
     * @param lane Lane to derive desired speed on.
     * @return Desired velocity [m/s]
     */
    public double desiredVelocity(Lane lane) {
        double vMax = vehicle.getVMax();
        double vWant = fSpeed*lane.getVLim();
        return vWant<vMax ? vWant : vMax;
    }
    
    /**
     * This method calls <tt>notice(jRSU)</tt> on all noticable RSUs within the 
     * range <code>noticeableRange</code>. Also, parameters are reset which
     * the notice methods may overrule with irregular values.
     */
    public void noticeRSUs() {
        // Reset lane change desire
        dLeftIntersection = 0;
        dRightIntersection = 0;
        // Reset acceleration
        a = aMin;
        // Notice RSUs in range
        for (RSU rsu : vehicle.RSUsInRange) {
            if (rsu.noticeable) {
                notice(rsu);
            }
        }
    }

    /**
     * Drivers may notice a RSU. For each specific type, a <tt>notice(type)
     * </tt> method should be present in the driver class. This method is a 
     * general notice which will invoke the appropriate notice method.
     * @param rsu Noticed RSU.
     */
    public final void notice(RSU rsu) {
    	java.lang.reflect.Method method;
        try {
            method = getClass().getMethod("notice", rsu.getClass());
            method.invoke(this, rsu);
            return;
        } catch (NoSuchMethodException nsme) {
            throw new java.lang.RuntimeException("No 'notice("+rsu.getClass().getName()
                    +")' method in driver class "+getClass().getName()+".");
        } catch (IllegalAccessException iae) {
            throw new java.lang.RuntimeException("Method 'notice("+rsu.getClass().getName()
                    +")' in driver class "+getClass().getName()+" is inaccessible.");
        } catch (java.lang.reflect.InvocationTargetException ite) {
            throw new java.lang.RuntimeException(ite);
        }
    }
    
    /**
     * Reduce speed such that the speed is equal to the appropriate speed at the 
     * location of the speed reduction once that location is reached.
     * @param sr Speed reduction.
     */
    public void notice(SpeedReduction sr) {
        double vLim = desiredVelocity(sr.lane);
        if (vLim<vehicle.v) {
            double s = vehicle.getDistanceToRSU(sr);
            double bMin = .5*(vehicle.v*vehicle.v - vLim*vLim)/s;
            lowerAcceleration(-bMin*bMin/b);
        }
    }
    
    /**
     * Notices a split RSU, i.e. follow downstream vehicle.
     * @param split RSU located at the split.
     */
    public void notice(Lane.splitRSU split) {
        if (vehicle.down==null) {
            // get appropriate lane
            Lane lane = split.getLaneForRoute(vehicle.route);
            // lane may be null if not appropriate for the route (will change lane before)
            if (lane!=null) {
                // follow downstream vehicle
                Movable down = lane.findVehicle(0, Model.longDirection.DOWN);
                if (down!=null) {
                    lowerAcceleration(calculateAcceleration(down));
                }
            }
        }
    }

    /**
     * Notice a conflict RSU.
     * @param conflict Conflict RSU.
     */
    public void notice(Conflict.conflictRSU conflict) {

        // Notice as speed reduction
        notice((SpeedReduction) conflict);
        
        // Initialize new time step
        if (isNewTimeStepForAction("conflicts")) {
            conflictBlocked = false;
            keepClearConflicts.clear();
            ignoreFurtherConflicts = false;
        }
        
        // Ignore further conflicts
        if (ignoreFurtherConflicts) {
            return;
        }
        
        // Queue lane change desire
        noticeIntersection(conflict);
        
        // Register conflict as one to keep clear if a later conflict forces a stop
        double sSelf = vehicle.getDistanceToRSU(conflict);
        if (conflict.keepClear() && sSelf>conflict.length()) {
            keepClearConflicts.add(conflict);
        }
        
        // Conflict is only valid with conflict vehicle
        Movable up = conflict.otherUp();
        if (up==null) {
            return;
        }
        
        // Calculate and preallocate a few often used parameters
        double v0 = desiredVelocity();
        double sOther = up.getDistanceToRSU(conflict.otherRSU());

        // Split or merge conflict?
        // Follow downstream vehicles on the conflict while being on the
        // conflict or while being the most downstream vehicle upstream of the 
        // conflict.
        if (!conflict.isCrossing() && (sSelf<conflict.length() || vehicle.down==null || 
                vehicle.down.getDistanceToRSU(conflict)<conflict.length())) {
            
            // Declare some variables
            double s = 0;
            Movable upFollow = null;
            double sFollow = 0;

            // Loop vehicles while upstream vehicle which is on the conflict to 
            // select a vehicle to follow
            boolean loop = true;
            while (up!=null && loop) {
                // Get headway from conflict vehicle to conflict
                sOther = up.getDistanceToRSU(conflict.otherRSU());
                // Get headway from own vehicle to conflict vehicle
                s = sSelf - up.l - sOther;
                if (s>-up.l && sOther<conflict.length()) {
                    // Vehicle downstream of own vehicle and (partially) on the conflict
                    upFollow = up; // this vehicle will be followed (or the next)
                    sFollow = s; // headway to follow that vehicle
                    // Get next upstream vehicle
                    if (sOther<0 && up.up==null && conflict.isMerge()) {
                        // At a merge, the first vehicle may be partially past the 
                        // conflict and not have an upstream vehicle connected.
                        Movable up2 = conflict.otherRSU().lane.findVehicle(
                                conflict.otherRSU().lane.l, Model.longDirection.UP);
                        // Nullify if same vehicle (i.e. it was not partially past the conflict)
                        up = up2!=up ? up2 : null;
                    } else {
                        up = up.up; // next upstream vehicle
                    }
                } else {
                    // Vehicle upstream of self or not on the conflict, stop loop
                    loop = false;
                }
            }

            // Follow selected vehicle (or stop for merge)
            if (upFollow!=null) {
                if (conflict.isMerge() && sFollow<sSelf-conflict.length()) {
                    // Vehicle to follow is partially upstream of a merge, we 
                    // can move up to the conflict.
                    stopForConflict(conflict, false, false);
                } else {
                    // Follow vehicle
                    double dv = vehicle.v - upFollow.v;
                    lowerAcceleration(longitudinal(vehicle.v, dv, v0, sFollow));
                }
            }
        }
        
        // Further actions only if the changes up is a not null, for a merge or 
        // crossing and if upstream of conflict area
        if (up==null || conflict.isSplit() || sSelf<conflict.length()) {
            return;
        }

        // Calculate and preallocate a few often used parameters
        double s;
        double aFree = aFree(vehicle.v, v0);
        // a merge is cleared or made passable at the start, crossings at the end
        double dMerge = conflict.isMerge() ? -conflict.length() : 0;

        /* Evaluation of conflicts
         * Behavior on conflicts is for a large part based on anticipated 
         * momvements of the own and the conflicting vehicle. These movements 
         * are anticipated assuming a fixed acceleration value (not necessarily 
         * the current acceleration of a vehicle). Given current distances, this 
         * results in various estimated times:
         * 
         *    tte: time till vehicle will enter the conflict
         *    ttc: time till vehicle will clear the conflict
         *    ttp: time till vehicle will make conflict passable (allow 
         *         sufficient space after the conflict)</lu>
         * 
         * It is also indicated to which vehicle the anticipated time pertains:
         * 
         *    o:   own vehicle
         *    c:   conflicting vehicle
         *    d:   downstream vehicle
         * 
         * Finally, the anticipated times can be calculated for multiple assumed
         * accelerations, in which case the variable will be appended with a 
         * number, e.g. ttp_d2.
         */
        if (conflict.isPriority()) {
            
            // PRIORITY AT MERGE OR CROSSING
            
            // Clear any previous courtesy yield if both vehicles are standing still
            if (conflictYieldPlans.containsKey(conflict)) {
                if (sSelf<conflict.length() || (vehicle.v==0 && up.v==0)) {
                    // Clear yield plan if on conflict or if both vehicles are fully stopped
                    conflictYieldPlans.remove(conflict);
                }
            }
            
            // Calculate ttp_d and tte_o assuming constant speed. These may indicate
            // that the vehicle is being blocked and might yield out of courtesy.
            double ttp_d = 0; // passable now if no downstream vehicle
            if (vehicle.down!=null) {
                s = vehicle.down.getDistanceToRSU(conflict)+vehicle.l+s0+vehicle.down.l+dMerge;
                ttp_d = anticipateConflictMovement(s, vehicle.down.v, 0);
            }
            s = sSelf-conflict.length();
            double tte_o = anticipateConflictMovement(s, vehicle.v, 0);
            
            // Most downstream vehicle fully upstream of conflict?
            boolean isFirstUp = sSelf>conflict.length() &&
                    (vehicle.down==null || vehicle.down.getDistanceToRSU(conflict)<conflict.length());
            
            /* Courtesy yielding
             * A number of conditions is required for a driver to yield out of 
             * courtesy while having priority. These are:
             * 
             *  - The driver inhibits this sort of behavior
             *  - The conflicting vehicle can pass and clear the conflict
             *  - There is no downstream vehicle before the conflict
             *  - The conflicting vehicle of a planned yield has not become the
             *    downstream vehicle (may occur at a merge)
             *  - The conflict is on the route of the conflict vehicle
             *  - And eihter or both of the following:
             *     o It was decided before to yield for the conflicting vehicle
             *     o The driver itself is blocked (ttp_d>tte_o) and the other
             *       vehicle is within a distance sYield of the conflict
             */
            if (yieldWithPriority && !up.getDriver().isConflictBlocked() && 
                    isFirstUp && vehicle.down!=conflictYieldPlans.get(conflict) && 
                    up.getDriver().vehicle.route.canBeFollowedFrom(conflict.otherRSU().lane) &&
                    ( conflictYieldPlans.get(conflict)==up || 
                      (tte_o<ttp_d && up.v==0) )) {
                
                // Stop before conflict to yield, but only with safe 
                // deceleration and without stopping for any upstream conflict
                stopForConflict(conflict, true, false);
                
                // Register yield plan
                conflictYieldPlans.put(conflict, up);
                
            } else {
                
                // Take priority, but avoid collision
                if (conflict.isCrossing()) {
                    
                    // Avoid collision if conflicting vehicle is on the conflict
                    if (sOther < conflict.otherRSU().length()) {
                        
                        // Calculate ttc_c and tte_o to evaluate if both 
                        // vehicles have overlapping use of the conflict.
                        s = sOther + up.l + dMerge;
                        double ttc_c = anticipateConflictMovement(s, up.v, 0);
                        s = sSelf - conflict.length();
                        tte_o = anticipateConflictMovement(s, vehicle.v, aFree);
                        
                        /* Avoid collision if the conflict will not be cleared 
                         * in time. Also, the conflicting vehicle may not be
                         * blocked in order to prevent vehicles to block each 
                         * other in a circle without any movement.
                         * Note that this may result in overlapping use of a 
                         * crossing conflict, but it is an effective way te 
                         * prevent a dead-lock.
                         */
                        if (tte_o<ttc_c && up.v>0) {
                            boolean stop = false;
                            double acc = 0;
                            if (Double.isInfinite(ttc_c)) {
                                stop = true;
                            } else {
                                // Solve using parabolic speed profile: s = v*t+.5*a*t^2
                                acc = 2*(s-s0conflict-vehicle.v*ttc_c)/(ttc_c*ttc_c);
                                // Time until v=0
                                double ttz = vehicle.v/-acc;
                                stop = ttz<ttc_c;
                            }
                            if (stop) {
                                // Need to come to a full stop in s as parabolic  
                                // profile includes a negative speed part
                                stopForConflict(conflict, false, false);
                            } else {
                                // Regular reduction of speed (not till zero)
                                lowerAcceleration(acc);
                            }
                        }
                        
                        // Register as being blocked if crossing vehicle or downstream
                        // vehicle is blocking and not moving and own speed is zero
                        if ((Double.isInfinite(ttc_c) || Double.isInfinite(ttp_d)) && vehicle.v==0) {
                            conflictBlocked = true;
                        }
                    }
                }
            }
            
        } else {
            
            // NO PRIORITY AT MERGE OR CROSSING
            
            // Calculate ttp_d assuming no or comfortable deceleration
            double ttp_d = 0; // passable now if no downstream vehicle
            double ttp_d2 = 0; // passable now if no downstream vehicle
            if (vehicle.down!=null) {
                s = vehicle.down.getDistanceToRSU(conflict)+vehicle.l+s0+vehicle.down.l+dMerge;
                // ttp_d is only of interest on conflicts that need to be kept clear
                if (conflict.keepClear()) {
                    ttp_d = anticipateConflictMovement(s, vehicle.down.v, 0);
                }
                // ttp_d2 is only of interest at a crossing
                if (conflict.isCrossing()) {
                    ttp_d2 = anticipateConflictMovement(s, vehicle.down.v, -b);
                }
            }
            
            // Derive time untill conflict can be passed if downstream vehicle
            // decelerates (to be more sure the conflict can be passed) and if
            // own vehicle can make it.
            s = sSelf + vehicle.l + dMerge;
            double ttc_o = anticipateConflictMovement(s, vehicle.v, aFree);
            
            // Calculate tte_c for current acceleration and comfortable 
            // deceleration
            s = sOther - conflict.otherRSU().length();
            double tte_c = anticipateConflictMovement(s, up.v, up.a);
            double tte_c2 = anticipateConflictMovement(s, up.v, -b);
            
            // Now perform the gap acception
            boolean gapOK = false;
            if (conflict.visibility()<sSelf) {
                // If major road not visible, reject gap
                
            } else if (!up.getDriver().vehicle.route.canBeFollowedFrom(conflict.otherRSU().lane)) {
                
                // Crossing vehicle's route does not pass the conflict
                gapOK = true;
                
            } else {
                
                // Add time for speed difference at merge, as simply clearing
                // the conflict is not sufficient with a speed difference
                if (conflict.isMerge()) {
                    
                    // Get conflicting vehicle speed at entering, assuming deceleration b
                    double vOther = up.v + -b*ttc_o;
                    // Get own speed at clearing, assuming maximum acceleration
                    double vSelf = vehicle.v + aFree*ttc_o;
                    // Increase time if own speed will be lower assuming a further deceleration b
                    double t_dv = 0;
                    if (vSelf<vOther) {
                        t_dv += (vOther-vSelf)/b;
                    }
                    /* Accept gap on merge if:
                     *  i)  it is expected that the conflict can be cleared in 
                     *      time assuming current acceleration of conflict 
                     *      vehicle and maximum acceleration of the own vehicle
                     *  ii) the conflict vehicle can delay entering the conflict 
                     *      during the required time with comfortable deceleration
                     */
                    if (ttc_o*estTimeFactor<tte_c && 
                            (ttc_o+t_dv)*estTimeFactor<tte_c2) {
                        gapOK = true;
                    }
                    
                } else {
                    
                    /* Accept gap on crossing if:
                     *  i)   the downstream vehicle is expected to allow 
                     *       sufficient space once before the conflict vehicle 
                     *       is expected to enter the conflict
                     *  ii)  the conflict is expected to be cleared before the
                     *       conflict vehicle is expected to enter the conflict
                     *  iii) the conflict vehicle can delay entering the 
                     *       conflict during the required time with comfortable 
                     *       deceleration
                     */
                    if (ttp_d*estTimeFactor<tte_c && 
                            ttc_o*estTimeFactor<tte_c &&
                            ttp_d2*estTimeFactor<tte_c2) {
                        gapOK = true;
                    }
                }
            }
            
            // Gap rejected?
            if (!gapOK) { 
                
                // Stop for conflict (and upstream conflicts)
                stopForConflict(conflict, false, true);
                
                // Register as being blocked if downstream vehicle is 
                // blocking and not moving and own speed is zero.
                double ttc_c = anticipateConflictMovement(sOther-conflict.length(), up.v, 0);
                if (conflict.isCrossing() && vehicle.v==0 && 
                        (Double.isInfinite(ttp_d) || Double.isInfinite(ttc_c))) {
                    conflictBlocked = true;
                }
                
            }
        }
    }
    
    /**
     * Anticipates a movement by assessing the time it takes to travel a certain
     * distance assuming s = v&middot;t + .5&middot;v&middot;a&middot;t<sup>2</sup>. 
     * The following special cases may occur:
     * <ul><li>If the distance to cover is smaller or equal to zero, zero is returned.
     * <li>If the acceleration and speed are zero, positive infinity is retuned.
     * <li>If the acceleration is such that the distance will never be covered, positive infinity is returned.</ul>
     * @param s Distance to travel [m].
     * @param v Initial speed [m/s].
     * @param a Assumed constant acceleration [m/s2].
     * @return Time to travel over a distance of <tt>s</tt> [s].
     */
    protected static double anticipateConflictMovement(double s, double v, double a) {
        if (s<=0) {
            // Distance is negative, e.g. has been covered already
            return 0;
        } else if (a==0) {
            // No acceleration
            if (v>0) {
                // Simple distance over speed
                return s/v;
            } else {
                // Standing still
                return Double.POSITIVE_INFINITY;
            }
        } else {
            // Constant acceleration, use parabolic profile: s = v*t+.5*a*t^2
            double tmp = v*v+2*a*s;
            if (tmp<0) {
                // Sqrt of negative means distance will never be covered
                return Double.POSITIVE_INFINITY;
            } else {
                // Finish parabolic method
                return (Math.sqrt(tmp)-v)/a;
            }
        }
    }
    
    /**
     * Will stop in front of a conflict. May also stop before any upstream 
     * conflict which will not allow sufficient space after it to keep it clear. 
     * Any conflict which allows passing but must be kept clear is added to 
     * <tt>keepClearConflicts</tt> per time step, from which distances will be 
     * assessed. In case space is insufficient, the stop command is assessed
     * for the upstream conflict. This may happen successively, keeping multiple
     * conflicts clear between which no sufficient space to stop can be found.
     * @param conflict Conflict to stop for.
     * @param safe Whether to apply decelerations of unsafe levels (i.e. below <tt>-b</tt>).
     * @param stopUpstream Whether to also stop for upstream conflicts that do not allow sufficient space.
     */
    protected void stopForConflict(Conflict.conflictRSU conflict, boolean safe, boolean stopUpstream) {
        
        // If requested, stop for upstream conflicts that must be kept clear.
        if (stopUpstream) {
            for (Conflict.conflictRSU rsu : keepClearConflicts) {
                // Distance adjustment between lanes
                double dxLanes = conflict.lane.xAdj(rsu.lane);
                // Distance between end of conflicts
                double dxRSUs = conflict.x-rsu.x+dxLanes;
                if (dxRSUs-conflict.length()<vehicle.l+s0conflict && dxRSUs>0) {
                    // Space between conflicts is insufficient & the conflict is
                    // upstream of the current conflict, so stop for that conflict
                    stopForConflict(rsu, safe, stopUpstream);
                    return; // no need to stop for the current conflict anymore
                }
            }
        }
        
        // Headway to conflict start
        double s = vehicle.getDistanceToRSU(conflict) - conflict.length();
        // Only stop if upstream of conflict
        if (s>0) {
            double s0tmp = this.s0; // remember regular stopping distance value
            this.s0 = s0conflict; // set small value for numerical overshoot
            // Decelerate using car-following model
            double acc = longitudinal(vehicle.v, vehicle.v, desiredVelocity(), s);
            if (safe) {
                if (acc>-b) {
                    lowerAcceleration(acc);
                    ignoreFurtherConflicts = true;
                }
            } else {
                lowerAcceleration(acc);
                // We can be sure that further conflicts have no effect anymore,
                // ignore them for efficiency
                ignoreFurtherConflicts = true;
            }
            this.s0 = s0tmp; // reset regular stopping distance value
        }
    }
    
    /**
     * Returns whether the driver is being blocked at a conflict. Being blocked
     * means that the speed is zero and either a crossing vehicle is standing 
     * still on a conflict, or the downstream vehicle does not allow sufficient
     * space to cross a conflict. In that case it is not useful to yield for the 
     * vehicle.
     * @return Whether the driver is being blocked at a conflict.
     */
    public boolean isConflictBlocked() {
        return conflictBlocked;
    }
    
    /**
     * Sets lane change desire regarding the fact that some RSU is approached.
     * The RSUs of interest can be priority conflicts, traffic lights or other
     * RSUs that indicate some kind of interuption for which drivers may want
     * to change lane. Drivers are probable to select the lane which presents 
     * them with the largest headway.<br>
     * <br>
     * Sets acceleration to <tt>aInter</tt>.
     * @param rsu RSU that triggers intersection lane change desire.
     */
    public void noticeIntersection(RSU rsu) {
        
        // Clear bookkeeping in a new time step
        if (!isNewTimeStepForAction("intersection_desire")) {
            return;
        }
        
        // Set acceleration for intersection
        a = aInter;
        
        // Only consider if there is a downstream vehicle
        if (vehicle.down!=null) {
            // Acceleration in current lane
            double aCur = calculateAcceleration(vehicle.down);
            // Distance to rsu
            double x = vehicle.getDistanceToRSU(rsu);
            // Limit to a minimum due to very low speed 
            // (otherwise acceleration gain is overestimated)
            double aDt = -vehicle.v/vehicle.model.dt;
            aCur = aCur>=aDt ? aCur : aDt;
            // Consider left lane if route can be followed and conflict is 
            // located within the distance where a lane change is required for
            // the route (i.e the conflict is the next point of interest).
            if (vehicle.lane.goLeft && vehicle.route.canBeFollowedFrom(vehicle.lane.left) && 
                    ( (vehicle.route.xLaneChanges(vehicle.lane.left) -
                    vehicle.lane.getAdjacentX(vehicle.x, Model.latDirection.LEFT)) > x) ) {
                // Acceleration on left lane
                double aLeft = calculateAcceleration(vehicle.leftDown);
                // Desire is given by the acceleration gain, normalized by the 
                // regularly maximum possible acceleration gain.
                dLeftIntersection = (aLeft-aCur)/(a+b);
            }
            // Idem. for right lane.
            if (vehicle.lane.goRight && vehicle.route.canBeFollowedFrom(vehicle.lane.right) && 
                    ( (vehicle.route.xLaneChanges(vehicle.lane.right) -
                    vehicle.lane.getAdjacentX(vehicle.x, Model.latDirection.RIGHT)) > x) ) {
                double aRight = calculateAcceleration(vehicle.rightDown);
                dRightIntersection = (aRight-aCur)/(a+b);
            }
        }
    }

    /**
     * Sets deceleration for a traffic light, if needed, and sets queue lane
     * change desire.
     * @param trafficLight Traffic light that was noticed.
     */
    public void notice(TrafficLight trafficLight) {
        // Consider queue lane change desire for traffic lights
        noticeIntersection(trafficLight);
        // Ignore green
        if (!trafficLight.isGreen()) {
            // Stop for yellow (and red) using a deceleration of bYellow. This 
            // should stop vehicles or let them pass the yellow light before it 
            // turns red.
            double s = vehicle.getDistanceToRSU(trafficLight);
            double bTmp = b;
            b = bYellow;
            double acc = longitudinal(vehicle.v, vehicle.v, desiredVelocity(), s);
            if (acc>-bYellow) {
                lowerAcceleration(acc);
            }
            b = bTmp;
        }
    }

    /**
     * Is called once at vehicle generation <i>after</i> the stochastic
     * vehicle and driver parameters have been set. It can be used to define
     * parameter correlations.
     */
    public void correlateParameters() {
        T = Tmax;
        aMin = a;
    }

    /**
     * Whether the driver is on a taper lane <i>that is applicable</i> to this 
     * driver.
     * @return Whether the driver is on a taper lane.
     */
    public boolean isOnTaper() {
        return isTaper(vehicle.lane);
    }

    /**
     * Same as isOnTaper(), but if the vehicle would be on the given lane.
     * @param lane Lane for which it needs to be known whether it is an applicable taper.
     * @return Whether lane is an applicable taper to this driver.
     */
    public boolean isTaper(Lane lane) {
        return (lane.taper!=null && vehicle.route.canBeFollowedFrom(lane.taper));
    }
    
    @Override
	public String toString() {
    	return String.format("s0=%.2fm", s0);
    }
    
    public double getStoppingDistance_r() {
    	return s0;
    }
    
    public double getLaneChangeDesireRight_r() {
    	return dRight;
    }
    
    public double getLaneChangeDesireLeft_r() {
    	return dLeft;
    }
}