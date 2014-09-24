package nl.tudelft.otsim.Simulators.LaneSimulator;

import java.util.Locale;

import nl.tudelft.otsim.Utilities.TimeScaleFunction;

/**
 * Standard vehicle generator with various types of vehicle generation.
 */
public class Generator extends Controller {

    /** Current demand level [veh/h] */
    protected double demand = 0;

    /** Dynamic demand as two columns with [t, demand]. May be <tt>null</tt>. */
    protected double[][] dynamicDemand;
    
    protected TimeScaleFunction tsfDemand = null;

    /** Interpolate or stepwise dynamic demand. */
    public boolean interpDemand = true;

    /** Lane at the start of which vehicles need to be generated. */
    public Lane lane;

    /** Distribution of headways. */
    protected distribution dist;

    /** 
     * Static route probabilities. These may be changed dynamically by an 
     * external controller.
     */
    private double[] routeProb;

    /** Available routes from this generator. */
    public Route[] routes;

    /** Time when next vehicle will enter the link. */
    protected double tNext = 0;

    /** Next vehicle that will be placed on the lane. */
    protected Vehicle nextVehicle; // next vehicle

    /** 
     * Static class probabilities. These may be changed dynamically by an 
     * external controller. 
     */
    public java.util.HashMap<Integer, Double> classProbs = new java.util.HashMap<Integer, Double>(0);

    /** Vehicles in upstream queue as they could not be generated. */
    protected int queue = 0;

    /** Total number of vehicles generated. */
    protected int generated = 0;

    /** Generation time of vehicles in case of predefined headway distribution. */
    public double[] preTime;

    /** 
     * Class IDs of vehicles in case of predefined headway distribution.
     */
    public int[] preClass;

    /** Speed of vehicles in case of predefined headway distribution. */
    public double[] preVelocity; // velocities

    /** 
     * Route indices (0, 1, 2, ...) of vehicles in case of predefined headway
     * distribution.
     */
    public int[] preRoute; // routes
    
    /**
     * Array of probabilities of which the nth index will be the probability of
     * the nth created class in the containing jModel. If this array is not null
     * it will be used during initialization.
     */
    protected double[] probabilities;

    /**
     * Constructor setting the lane and distribution and linking the generator
     * with a lane and vice versa.
     * @param lane Lane to generate vehicles on.
     * @param dist Headway distribution.
     */
    // using this at the end is ok, generator is fully initialized
    public Generator(Lane lane, distribution dist) {
        super(lane.model);
    	//System.out.println("Creating generator for lane " + lane);
        this.lane = lane;
        this.dist = dist;
        lane.generator = this;
    }

    /**
     * Vehicle generation method. A new vehicle is generated if appropriate.
     */
    @Override
	public void control() {
        // Dynamic demand
        if (dynamicDemand != null) {
            int lastIndex = dynamicDemand.length-1;
            if ((lane.model.t >= dynamicDemand[lastIndex][0]) && (demand != dynamicDemand[lastIndex][1]))
                setDemand(dynamicDemand[lastIndex][1]);	// set latest demand value
            else if (lane.model.t<dynamicDemand[lastIndex][0]) {
                // find index of latest demand
                int index = 0;
                while (lane.model.t>=dynamicDemand[index][0])
                    index++;
                index--; // subtract one as we found index of first upcoming demand
                if (interpDemand) {
                    // interpolate demand
                    double t1 = dynamicDemand[index][0];
                    double t2 = dynamicDemand[index + 1][0];
                    double d1 = dynamicDemand[index][1];
                    double d2 = dynamicDemand[index + 1][1];
                    double d;
                    if (d1 != d2)
                        d = ((lane.model.t - t1) * d2 + (t2 - lane.model.t) * d1) / (t2 - t1);
                    else
                        d = d1;
                    setDemand(d);
                } else if (demand != dynamicDemand[index][1])
                    setDemand(dynamicDemand[index][1]); // update demand stepwise
            }
        }

        // Create vehicle(s)
        if (dist == distribution.PREDEFINED) {
            // Generate vehicles from list
            while (tNext <= lane.model.t) {
                // select class with right id
                VehicleDriver vehClass = lane.model.getClass(preClass[generated]);
                // generate vehicle
                nextVehicle = vehClass.generateVehicle();
                nextVehicle.v = preVelocity[generated];
                nextVehicle.route = routes[preRoute[generated]];
                // put at lane
                nextVehicle.paste(lane, nextVehicle.v * (lane.model.t - tNext));
                lane.model.addVehicle(nextVehicle);
                nextVehicle.setXY();
                // pass RSUs
                passRSUs(nextVehicle);
                // update status
                generated++;
                tNext = tNext + headway();
            }
        } else {
            // Regular vehicle generation
            // a vehicle is needed
            if (nextVehicle == null)
                randomNextVehicle();
            boolean success = true; // to have a first attempt
            while ((queue > 0) && success) {
                // while there is a queue and the last vehicle could be
                // generated, attempt one more vehicle.
                success = addQueueVehicle();
            }
            while (tNext <= lane.model.t) {
                // while tNext is in the past, generate new vehicle (or add to
                // queue).
                success = addFreeVehicle();
                tNext = tNext + headway(); // may be within the same time step
            }
        }
    }

    /** Empty, needs to be implemented. */
    @Override
	public void noControl() {}
    
    /**
     * Initializes vehicle generation.
     */
    @Override
	public void init() {
        if ((dist == distribution.PREDEFINED) && (preTime != null))
            tNext = preTime[0];
        else if (probabilities != null)
            for (int i = 0; i < probabilities.length; i++)
                classProbs.put(lane.model.classes.get(i).id(), probabilities[i]);
        double sumProb = 0;
        for (int i = 0; i < routeProb.length; i++) {
        	System.out.println(String.format(Locale.US, "probability %.6f, route %s", routeProb[i], routes[i]));
        	sumProb += routeProb[i];
        }
        if (Math.abs(sumProb - 1.0) > 0.0001)
        	throw new Error("Probabilities do not add up to approximately 1.0 (sum is " + sumProb + ")");
    }
    
    /**
     * Set the distribution type.
     * @param dist Distribution type.
     */
    public void setDistibution(distribution dist) {
        this.dist = dist;
    }

    /**
     * Private method to generate a free flowing vehicle. These are generated if
     * the acceleration >= 0. If the vehicle is not generated, it is added to
     * the queue.
     * @return Whether the vehicle could be generated.
     */
    protected boolean addFreeVehicle() {
        Lane genLane = lane;
        Lane vehLane = lane;
        boolean success = false;
        while (!success && (genLane != null)) {
            // set speed of downstream vehicle, if any
            Movable down = genLane.findVehicle(0, Model.longDirection.DOWN, Driver.maximumSearchDistance);
            double downX;
            if (down != null) {
                nextVehicle.v = Math.min(down.v, nextVehicle.getDriver().desiredVelocity(genLane));
                downX = down.x + genLane.xAdj(down.getLane());
            } else {
                nextVehicle.v = nextVehicle.driver.desiredVelocity(genLane);
                downX = Double.POSITIVE_INFINITY;
            }
            // paste at lane
            double x = nextVehicle.v * (lane.model.t - tNext);
            if (x > downX) {
            	success = false;
            	break;
            }
            vehLane = genLane;
            while (x > vehLane.l) {
                // vehicle is generated beyond lane length
                x = x - vehLane.l;
                vehLane = vehLane.down;
            }
            nextVehicle.paste(vehLane, x);
            // headway positive
            double s = 0;
            if (down != null) {
                s = nextVehicle.getHeadway(down);
                // acceleration ok?
                if (s >= 0)
                    nextVehicle.driver.drive();
            }
            // If there is no down, generate always, otherwise check acceleration
            if ((down != null) && ((s < 0) || (nextVehicle.a < 0) || (x > downX))) {
                genLane = null;
                nextVehicle.cut();
                nextVehicle.lcProgress = 0;
                nextVehicle.a = 0;
            } else {
                nextVehicle.cut();
                nextVehicle.paste(vehLane, x);
                success = true;
                lane.model.addVehicle(nextVehicle);
                nextVehicle.setXY();
                // pass RSUs
                passRSUs(nextVehicle);
                randomNextVehicle(); // new next vehicle
            }
        }
        if (!success) {
            // try to add as queue vehicle
            queue++;
            success = addQueueVehicle();
        }
        return success;
    }

    /**
     * Private method to generate a vehicle from queue. These are created at
     * their desired headway.
     * @return Whether the vehicle could be generated.
     */
    protected boolean addQueueVehicle() {
        Lane genLane = lane;
        Lane vehLane = lane;
        boolean success = false;
        while (!success && (genLane != null)) {
            Movable down = genLane.findVehicle(0, Model.longDirection.DOWN, Driver.maximumSearchDistance);
            nextVehicle.paste(genLane, 0);
            double downX=0;
            double downL=0;
            if (down!=null) {
                downX = down.x+genLane.xAdj(down.getLane());
                nextVehicle.v = Math.min(down.v, nextVehicle.driver.desiredVelocity());
                downL = down.l;
            } else
                throw new java.lang.RuntimeException("Trying to generate queue vehicle without downstream vehicle.");
            if (downX-downL > nextVehicle.driver.desiredEquilibriumHeadway()) {
                double x = downX - downL - nextVehicle.driver.desiredEquilibriumHeadway();
                // check that x<=v*t            
                vehLane = genLane;
                while (x > vehLane.l) {
                    // vehicle is generated beyond lane length
                    x = x - vehLane.l;
                    vehLane = vehLane.down;
                }
                // make sure the vehicle is at the right location (was initially located at x=0)
                nextVehicle.cut();
                nextVehicle.paste(vehLane, x);
                success = true;
                lane.model.addVehicle(nextVehicle);
                nextVehicle.setXY();
                // pass RSUs
                passRSUs(nextVehicle);
                randomNextVehicle(); // new next vehicle
                queue--;
            } else {
                genLane = null;
                nextVehicle.cut();
            }
        }
        return success;
    }

    /**
     * Private method to set a random new vehicle.
     */
    protected void randomNextVehicle() {
        // select a random class
        nextVehicle = randomClass().generateVehicle();
        // give random destination
        double r = lane.model.random().nextDouble();
        double lowerLim = 0;
        int routeInd = 0;
        while (lowerLim + routeProb[routeInd] < r) {
            lowerLim = lowerLim + routeProb[routeInd];
            routeInd++;
        }
        nextVehicle.route = routes[routeInd];
        //System.out.println("randomVehicle: id= " + nextVehicle.id + ", r = " + r + ", route: " + nextVehicle.route.toString());
    }

    /**
     * Updates the current demand value. The time of the next vehicle is adjusted.
     * @param dem New demand level [veh/h].
     */
    public void setDemand(double dem) {
        /*
         * A certain time of the headway between tNext and t (now) remains.
         * Adjust this time with a fraction of demand/dem, i.e. larger demand is
         * smaller remaining time.
         */
        if (tNext >= lane.model.t) {
            if ((dem > 0) && (demand > 0)) {
                double f = demand / dem;
                demand = dem;
                tNext = lane.model.t + (tNext - lane.model.t) * f;
            } else if (dem > 0) {
                // demand was zero, start at random headway
                demand = dem;
                tNext = lane.model.t + lane.model.random().nextDouble() * headway();
            } else {
                // demand will be zero
                demand = dem;
                tNext = Double.POSITIVE_INFINITY;
            }
        }
    }

    /**
     * Returns the dynamic demand array.
     * @return Demand array.
     */
    public double[][] getDemand() {
        return dynamicDemand;
    }
    
    /**
     * Returns the queue size of the generator, which is the number of vehicles
     * that could not be generated, but will as soon as possible.
     * @return Number of vehicles in queue.
     */
    public int getQueue() {
        return queue;
    }

    /**
     * Sets the dynamic demand. A value may be appended to cover the time after
     * the last given demand value.
     * @param dem Dynamic demand as two columns [t, demand].
     */
    public void setDemand(double[][] dem) {
        if (dem[dem.length - 1][0] < lane.model.period) {
            // append an infinite time value
            double[][] dem2 = new double[dem.length + 1][2];
            // first copy data
            for (int i = 0; i < dem.length; i++) {
                dem2[i][0] = dem[i][0];
                dem2[i][1] = dem[i][1];
            }
            // appended value (constant demand after last given time)
            dem2[dem.length][0] = lane.model.period;
            dem2[dem.length][1] = dem[dem.length - 1][1];
            dynamicDemand = dem2;
        } else
            dynamicDemand = dem;
    }
    
    /**
     * Set a {@link TimeScaleFunction} as demand pattern.
     * @param tsf {@link TimeScaleFunction}; the demand pattern
     */
    public void setDemand (TimeScaleFunction tsf) {
    	tsfDemand = tsf;
    }

    /**
     * Determines a headway value based on generator settings.
     * @return Headway [s].
     */
    public double headway() {
        double headway = 0;
        if (null != tsfDemand) {
        	double currentDemand = tsfDemand.getFactor(model.t);
        	if (0d == currentDemand)
        		headway = Double.POSITIVE_INFINITY;
        	else
        		headway = 3600d / currentDemand;
        } else if (dist == distribution.PREDEFINED) {
            if (generated >= preTime.length) {
                // all vehicles were generated
                headway = Double.POSITIVE_INFINITY;
            } else if (generated == 0)
                headway = preTime[0]; // first vehicle
            else
                headway = preTime[generated] - preTime[generated - 1]; // headway is time difference between 2 consecutive vehicles
        } else {
            if (demand > 0) {
                double dt = 3600 / demand; // average headway
                if (dist == distribution.UNIFORM)
                    headway = dt; // always the average headway
                else if (dist == distribution.EXPONENTIAL)
                    headway = -Math.log(lane.model.random().nextDouble()) * dt; // note: r = -log(uniform)/gamma & mean = 1/gamma
            } else
                headway = Double.POSITIVE_INFINITY; // no demand
        }
        return headway;
    }

    /**
     * Randomly selects a class given generator specific class probabilities.
     * @return Randomly selected class.
     */
    public VehicleDriver randomClass() {
        double r = lane.model.random().nextDouble();
        double lowerLim = 0;
        java.util.Iterator<Integer> inter = classProbs.keySet().iterator();
        Integer id = inter.next();
        while (inter.hasNext() && (lowerLim + classProbs.get(id) < r)) {
            lowerLim = lowerLim + classProbs.get(id);
            id = inter.next();
        }
        return lane.model.getClass(id);
    }

    /**
     * Passes RSUs that are upstream of the location where a vehicle was
     * generated.
     * @param veh Generated vehicle.
     */
    protected static void passRSUs(Vehicle veh) {
        // upstream lanes (if any)
        Lane l = veh.getLane().up;
        while (l != null) {
        	if (l.marked)
        		break;
        	l.marked = true;
            for (int i = 0; i < l.RSUcount(); i++)
                if (l.getRSU(i).passable)
                    l.getRSU(i).pass(veh);
            l = l.up;
        }
        // lane itself
        for (int i = 0; i < veh.getLane().RSUcount(); i++)
            if ((veh.getLane().getRSU(i).x <= veh.x) && veh.getLane().getRSU(i).passable)
                veh.getLane().getRSU(i).pass(veh);
        for (l = veh.getLane().up; (l != null) && l.marked; l = l.up)
        	l.marked = false;
    }
    
    /**
     * Convenience method that sets the nth element of <tt>probabilities</tt> as
     * the probability of the nth class that was or will be constructed for the 
     * containing <tt>jModel</tt>. Note: the actual work happens during 
     * initialization. This method simply stores the array.
     * @param probabilities Class probabilities.
     */
    public void setClassProbabilities(double[] probabilities) {
        this.probabilities = probabilities;
    }

    /** Enumeration of possible headway distributions. */
    public enum distribution {
        /** Headways as given by pre-defined arrival time array. */
        PREDEFINED, 
        /** 
         * Exponential distribution. Very short headways may result. The default
         * vehicle generation is able to deal with this by delaying vehicle 
         * generation and generating a queued vehicle (at following headway).
         */
        EXPONENTIAL, 
        /** Vehicles are uniformly spread over time (fixed headway). */
        UNIFORM;
    }

    /**
     * Set the probabilities for the routes (must add up to approximately 1.0).
     * @param newProbabilities Double[] the probabilities for the routes
     */
	public void setRouteProbabilities(double[] newProbabilities) {
		routeProb = newProbabilities;
        double sumProb = 0;
        for (int i = 0; i < routeProb.length; i++)
        	sumProb += routeProb[i];
        if (Math.abs(sumProb - 1.0) > 0.0001)
        	throw new Error("Probabilities do not add up to approximately 1.0 (sum is " + sumProb + ")");
	}
}