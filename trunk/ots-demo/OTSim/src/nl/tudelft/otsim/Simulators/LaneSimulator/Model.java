package nl.tudelft.otsim.Simulators.LaneSimulator;

import nl.tudelft.otsim.GUI.Log;
import nl.tudelft.otsim.GUI.Main;
import nl.tudelft.otsim.Simulators.SimulatedModel;

/**
 * Main model object. This functions as the main interface with the model. It
 * contains general settings, the network, all vehicles etc. Furthermore, it
 * represents the world.
 */
public class Model implements SimulatedModel {
    /** Time step number. Always starts as 0. */
    protected int k;
    
    /** Current time of the model [s]. Always starts at 0. */
    protected double t;
    
    /** Time step size of the model [s]. */
    public double dt;
    
    /** Maximum simulation period [s]. */
    public double period;
    
    public double getPeriod() {
		return period;
	}

	public void setPeriod(double period) {
		this.period = period;
	}



	/** Absolute start time of simulation. */
    public java.util.Date startTime;
    
    /** Total movables generated */
    public int nextMovableId = 0;
    
    /** 
     * Generator for random numbers. This generator is connected to the seed and
     * should be used for all operations that are random and should be 
     * repeatable. Before the generator can be used, the seed needs to be set
     * using <tt>setSeed</tt>.
     */
    protected java.util.Random rand;
    
    /** Set of all vehicles in simulation. */
    protected java.util.ArrayList<Vehicle> vehicles = new java.util.ArrayList<Vehicle>();
    
    /** Set of all temporary lane change vehicles in simulation. */
    protected java.util.ArrayList<LCVehicle> lcVehicles = new java.util.ArrayList<LCVehicle>();
    
    /** Set of lanes that make up the network. */
    public Lane[] network = new Lane[0];
    
    /** Set of all vehicle-driver classes. */
    protected java.util.ArrayList<VehicleDriver> classes = new java.util.ArrayList<VehicleDriver>();
    
    /** Set of controllers, both local and regional. */
    protected java.util.ArrayList<Controller> controllers = new java.util.ArrayList<Controller>();
    
    /** Total number of deleted vehicles. */
    protected int deleted = 0;
    
    /** 
     * Enables debugging mode. User-defined classes can use this to feedback 
     * information. 
     */
    public boolean debug = false;
    
    /** 
     * Enabled during vehicle generation. This is used to disable acceleration 
     * bookkeeping during generation attempts. 
     */
    protected boolean generating = false;
    
    /** Expandable set of general modeling settings. */
    public Settings settings = new Settings();
    
    /** Temporary trajectory storage before a set is saved to disk. */
    protected java.util.ArrayList<TrajectoryData> trajectories = 
            new java.util.ArrayList<TrajectoryData>();
    
    /** Counter of all saved trajectories. This is part of the filename. */
    protected int trajectoriesSaved = 0;
    
    /**
     * Constructor that sets some default settings.<br>
     * <br><pre>
     * boolean "storeTrajectoryData" = false
     * double  "trajectoryPeriod"    = 1 [s]
     * int     "trajectoryBuffer"    = 50
     * boolean "storeDetectorData"   = false
     * double  "detectorDelay"       = 120 [s]
     * double  "detectorPeriod"      = 60 [s]
     * String  "outputDir"           = "output"</pre>
     */
    public Model() {
        // trajectory data for analysis
        settings.putBoolean("storeTrajectoryData", false);
        settings.putDouble("trajectoryPeriod", 1); // sampling period
        settings.putInteger("trajectoryBuffer", 50); // trajectories saved on disc
        // detector data for analysis
        settings.putBoolean("storeDetectorData", false);
        settings.putDouble("detectorDelay", 120); // send delay
        settings.putDouble("detectorPeriod", 60); // aggregation period
        // directory
        settings.putString("outputDir", "output");
    }
    
    /**
     * Sets the seed and initializes the random generator.
     * @param s Random seed.
     */
    public void setSeed(long s) {
        rand = new java.util.Random();
        rand.setSeed(s);
    }
    
    /**
     * Initializes the model. This includes setting the lane change info per
     * lane and destination, initializing the vehicle generation and RSUs of the
     * lanes and initializing controllers. This method needs to be called before
     * running the model.
     */
    public void init() {
        // Set attributes
        k = 0;
        t = 0;
        vehicles = new java.util.ArrayList<Vehicle>();
        lcVehicles = new java.util.ArrayList<LCVehicle>();

        // Initialize lanes
        for (Lane l : network)
        	l.init();
        
        // Initialize controllers
        for (Controller c : controllers)
        	c.init();
    }

    /* (non-Javadoc)
	 * @see nl.tudelft.otsim.Simulators.LaneSimulator.Model#run(int)
	 */
   
	public void run(int n) {
        // Simulate n steps
        //int i = 0;
    	for (int nn = 0; (nn < n) && (t < period); nn++) {
            // Run on-board units, road-side units and controllers
            runUnits();
    		//System.out.println("test Run timesteps "+ t);
    		//i++;
            // Vehicle generation
            generating = true;
            for (Lane l : network)
            	if (null != l.generator)
            		l.generator.run();
            generating = false;

            // Drive
            // copy pointer array as vehicles may be deleted
            java.util.ArrayList<Vehicle> tmp = new java.util.ArrayList<Vehicle>(vehicles);
            for (Vehicle v : tmp) {
            	v.driver.drive();	// sets a and dy
            	if ((v.dy != 0) && (v.lcProgress == 0))
            		v.startLaneChange();
            }
            // Move
            // copy pointer array as vehicles may be deleted
            tmp = new java.util.ArrayList<Vehicle>(vehicles);
            for (Vehicle v : tmp)
            	v.move();	// performs a and dy

            // End lane changes
            for (Vehicle v : vehicles)
            	if (v.lcProgress >= 1)
            		v.endLaneChange();
            
            if (debug) {
                for (Vehicle veh : vehicles) {
                    // Check for collisions
                    Movable leader = veh.getNeighbor(Movable.DOWN);
                    if (veh == leader)
                    	continue;
                    if ((null != leader) && (veh.getHeadway(leader) < 0) &&
                            ((veh.getLane() == leader.getLane()) || !leader.getLane().isMerge())) {
                    	String problem = String.format("Collision: %s %.2f@%d collided with %s %.2f@%d", veh.toString(), veh.x, veh.getLane().id, leader.toString(), leader.x, leader.getLane().id);
                        System.err.println(problem);
                        //veh.getHeadway(leader);
                        //throw new RuntimeException(problem);
                    }
                    // Check reciprocity of UP and DOWN neighbor links
                    int[] directions = { Movable.UP, Movable.DOWN };
                    for (int direction : directions) {
                    	Movable other = veh.getNeighbor(direction);
                    	if (null != other) {
                    		Movable back = other.getNeighbor(Movable.flipDirection(direction, Movable.FLIP_UD));
                    		if ((null != back) && (back != veh)) {
                    			String problem = String.format ("Warning: Movable %s has non reciprocal %s link to %s (reverse link goes to %s)", veh.toString(), Movable.directionToString(direction), other.toString(), back.toString());
                                System.err.println(problem);
                                //throw new RuntimeException(problem);
                    		}
                    	}                   		
                    }
                }
                // Check that all vehicles on all lanes are correctly linked to each other
                for (Lane l : network) {
                	java.util.ArrayList<Movable> vehiclesOnLane = l.getVehicles();
                	Movable prevM = null;
                	for (Movable m : vehiclesOnLane) {
                		if (null != prevM) {
                			Movable neighbor = prevM.getNeighbor(Movable.DOWN);
                			if ((null != neighbor) && (prevM.x > neighbor.x)) {
                				String problem = String.format("Movable %s is not correctly sorted with respect to movable %s", prevM.toString(), neighbor.toString());
                				System.err.println(problem);
                				//throw new RuntimeException (problem);                				
                			}
                			if (prevM == m) {
                				String problem = String.format("Movable %s is linked multiple times to a lane", m.toString());
                				System.err.println(problem);
                				//throw new RuntimeException (problem);                				
                			}
                			if (null == neighbor) {
                				String problem = String.format("Movable %s has unset DOWN (should be %s)", prevM.toString(), m.toString());
                				System.err.println(problem);
                				//throw new RuntimeException (problem);
                			} else if (m != neighbor) {
                				String problem = String.format("Movable %s has DOWN set to %s (should be %s)", prevM.toString(), neighbor.toString(), m.toString());
                				System.err.println(problem);
                				//throw new RuntimeException (problem);                				
                			}
                			neighbor = m.getNeighbor(Movable.UP);
                			if (null == neighbor) {
                				String problem = String.format("Movable %s has unset UP (should be %s)", m.toString(), prevM.toString());
                				System.err.println(problem);
                				//throw new RuntimeException (problem);                				
                			} else if (prevM != neighbor) {
                				String problem = String.format("Movable %s has UP set to %s (should be %s)", m.toString(), neighbor.toString(), prevM.toString());
                				System.err.println(problem);
                				//throw new RuntimeException (problem);                				
                			}
                		}
                		prevM = m;
                	}
                }
            }
            // Update time
            k++; // Increment time step number
            t = k * dt; // time [s]
        }
    }
    
    
    
    // GUUS has added some tests for computer time taken by "Control"
    // see also Conflict - control()
    long rsuTime1 = 0;	// visible within package
    long rsuTime2 = 0;	// visible within package
    long rsuTime3 = 0;	// visible within package
    long rsuTime4 = 0;	// visible within package
    private long beginTime = System.currentTimeMillis();
    private long time = System.currentTimeMillis() - beginTime;
    long numberOfRSUCalls = 0;
    private long numberOfRSU = 0;
    /** Set to true to output lots of info regarding RSU performance */
    public boolean debugRSURuntime = false;
    
    /**
     * Runs all road-side units, on-board units and controllers. This is part of
     * a regular time step, as well as gathering the final data after simulation.
     * This is because the model will only run while t&lt;period. If however a
     * units needs to aggregate data at t=period, that will not happen in the
     * main model loop.
     */
    protected void runUnits() {	// Run road-side units
    	int i = 0;
		int j = 0;
		final int clusterSize = 100;
    	for (Lane l : network) {
    		if (i > clusterSize)  {
    			time = System.currentTimeMillis() - beginTime;
    			int lanesProcessed = j * clusterSize + i;
        		
    			if (debugRSURuntime) {
	    			System.out.println("test network number of lanes processed " + lanesProcessed);
	    			System.out.println( " number of RSU's " + numberOfRSU);
	    			System.out.println("Total time RSU " + rsuTime1 + " number of RSU's calls " + numberOfRSUCalls);
	    			System.out.println("time RSU 2 " + rsuTime2 );
	    			System.out.println("time RSU 3 " + rsuTime3 );
	    			System.out.println("time RSU 4 " + rsuTime4 );
	
	    			System.out.println("total time " + time);
    			}
    			i = 0;
    			j++;
    		}
    		i++;
    		for (RSU rsu : l.RSUs) {
    			numberOfRSU++;
        		rsu.run();
    		}
    	}
        // Run on-board units
    	i = 0;
    	for (Vehicle v : vehicles)  {
    		if (v.isEquipped())  {
        		System.out.println("test OBU" + i);
        		i++;
    			v.OBU.run();
    		}
    	}
        // Run controllers
    	i = 0;
    	for (Controller c : controllers)  {
    		//System.out.println("test Controller" + i);
    		//i++;
    		c.run();
    	}
    }

    /** 
     * Adds a class to the model.
     * @param cls Class to add.
     */
    protected void addClass(VehicleDriver cls) {
        classes.add(cls);
    }
    
    /**
     * Returns the class with given id.
     * @param id Id of requested class.
     * @return Class with given id.
     */
    public VehicleDriver getClass(int id) {
    	for (VehicleDriver vd : classes)
    		if (id == vd.id)
    			return vd;
        return null;
    }
    
    /**
     * Adds a vehicle in the simulation.
     * @param vehicle Vehicle to add.
     */
    public void addVehicle(Movable vehicle) {
        if (vehicle instanceof Vehicle) {
            vehicles.add((Vehicle) vehicle);
            vehicle.setXY();
            String logFileName = Main.mainFrame.getVehicleLifeLogFileName();
            if (null != logFileName)
            	Log.logMessage(logFileName, false, "Created at\t%.3f\t%s\t%s\t\n", t, vehicle.toString(), vehicle.marker); //export TrafficClass + Destination & Origin??? + vehicle.getDriver().activationLevel
        }
        else if (vehicle instanceof LCVehicle)
            lcVehicles.add((LCVehicle) vehicle);
    }
    
    /**
     * Removes a vehicle from the simulation.
     * @param vehicle Vehicle to remove.
     */
    public void removeVehicle(Movable vehicle) {
        if (vehicle instanceof Vehicle) {
            String logFileName = Main.mainFrame.getVehicleLifeLogFileName();
            if (null != logFileName)
            	Log.logMessage(logFileName, false, "Destroyed at\t%.3f\t%s\t%s\t\n", t, vehicle.toString(), vehicle.marker); //Destroy veh. only contains [last node]
            vehicles.remove(vehicle);
        }
        else if (vehicle instanceof LCVehicle)
            lcVehicles.remove(vehicle);
    }
    
    /**
     * Returns whether a vehicle still exists in simulation. This can be used by
     * for example controllers that maintain an internal bookkeeping of 
     * vehicles. Vehicles that no longer exist can be removed.
     * @param vehicle Vehicle to check existence of.
     * @return Whether vehicle exists in simulation.
     */
    public boolean exists(Movable vehicle) {
        if (vehicle instanceof Vehicle)
            return vehicles.contains(vehicle);
        return lcVehicles.contains(vehicle);
    }
    
    /**
     * Returns the a shallow copy of the current array of vehicles in simulation.
     * @return Array of vehicles in simulation.
     */
    @SuppressWarnings("unchecked")
	public java.util.ArrayList<Vehicle> getVehicles() {
        return (java.util.ArrayList<Vehicle>) vehicles.clone();
    }
    
    /**
     * Returns the current array of lcVehicles in simulation.
     * @return Array of lcVehicles in simulation.
     */
    public java.util.ArrayList<LCVehicle> getLcVehicles() {
        return lcVehicles;
    }
    
    private static void checkCut(Movable cutMovable, Movable other) {
        final int[] directions = { Movable.UP, Movable.DOWN};
        for (int direction : directions) {
        	if (other.getNeighbor(direction) == cutMovable) {
            	String whatIsIt;
            	if (cutMovable instanceof Vehicle)
            		whatIsIt = "Vehicle";
            	else if (cutMovable instanceof LCVehicle)
            		whatIsIt = "LCVehicle";
            	else
            		whatIsIt = "Movable";	// That should never happen ...
        		String description = String.format(Main.locale, "Cut %s %s %.3f@%d is still connected from movable %s %.2f@%d in direction %s", 
        				whatIsIt, cutMovable.toString(), cutMovable.x, cutMovable.getLane().id, other.toString(), other.x, other.getLane().id, 
                        Movable.directionToString(direction));
            	System.err.println(description);
                throw new RuntimeException(description);    	        		
        	}
        }
    }
    
    /**
     * Displays messages whenever any movable in simulation has a pointer to the
     * given movable. This can be used to check whether the given movable was 
     * correctly cut from simulation after the <tt>jMovable.cut()</tt> method.
     * @param movable Cut movable.
     */
    public void checkForRemainingPointers(Movable movable) {
        for (java.util.Iterator<Vehicle> iter = vehicles.iterator(); iter.hasNext(); )
        	checkCut(movable, iter.next());
        for (java.util.Iterator<LCVehicle> iter = lcVehicles.iterator(); iter.hasNext(); )
        	checkCut(movable, iter.next());
    }
    
    /**
     * Adds a controller to the model.
     * @param controller Controller to add.
     */
    public void addController(Controller controller) {
        controllers.add(controller);
    }
    
    /**
     * Derives the current absolute time of the simulation as being <tt>t</tt>
     * seconds after the given <tt>startTime</tt>. If no <tt>startTime</tt> is
     * given, <tt>null</tt> is returned.
     * @return Absolute current time.
     */
    public java.util.Date currentTime() {
        if (startTime == null)
            return null;    
        return new java.util.Date(startTime.getTime() + (long) (t * 1000));
    }
    
    /**
     * Returns the current time in simulation.
     * @return Current time [s].
     */
    public double t() {
        return t;
    }
    
    /**
     * Returns the random generator.
     * @return Random generator.
     */
    public java.util.Random random() {
        return rand;
    }
    
    /**
     * Returns the number of deleted vehicles.
     * @return Number of deleted vehicles.
     */
    public int deletedVehicles() {
        return deleted;
    }
    
    /**
     * Returns whether the model is in the vehicle generation phase.
     * @return Whether the model is in the vehicle generation phase.
     */
    public boolean isGenerating() {
        return generating;
    }

    /**
     * Returns a lognormally distributed random number where the lognormal
     * distribution has mean <tt>m</tt> and standard deviation <tt>s</tt>. Note 
     * that these are not the same as the mu and sigma of the variable's natural 
     * logarithm.
     * @param m Mean of the lognormal distribution.
     * @param s Standard deviation of the lognormal distribution.
     * @return Double; random lognormally distributed number.
     */
    public double lognormal(double m, double s) {
        double mu = Math.log(m*m / Math.sqrt(s*s+m*m));
        double sigma = Math.sqrt(Math.log((s*s/(m*m)) + 1));
        double r = Math.exp(mu + rand.nextDouble()*sigma);
        return r;
    }

    /**
     * Stores all requested data. This may include all trajectories of vehicles 
     * still in simulation and in the buffer and all detectors. Typically, this
     * method is called after the simulation has finished.
     */
    public void storeData() {        
        // Run on-board units, road-side units and controllers
        runUnits();
        
        // Store remaining vehicles
        if (settings.getBoolean("storeTrajectoryData")) {
        	for (Vehicle v : vehicles)
        		if (null != v.trajectory)
        			saveTrajectoryData(v.trajectory);
            saveTrajectoryBufferToDisk();
        }
        if (settings.getBoolean("storeDetectorData")) {
            // Store detector data
        	for (Lane l : network)
        		for (RSU rsu : l.RSUs)
        			if (rsu instanceof Detector)
        				saveDetectorData((Detector) rsu);
        }
    }

    /**
     * Saves trajectory in memory and saves buffer to disk if it is full.
     * @param trajectory Trajectory to save.
     */
    public synchronized void saveTrajectoryData(Trajectory trajectory) {
        trajectories.add(trajectory.asSerializable());
        if (trajectories.size() >= settings.getInteger("trajectoryBuffer"))
            saveTrajectoryBufferToDisk();
    }
    
    /**
     * Saves the trajectory buffer, no matter what size, to disk. The buffer
     * will be empty afterwards.
     */
    protected synchronized void saveTrajectoryBufferToDisk() {
        // Save trajectories to disk
        java.text.DecimalFormat df = new java.text.DecimalFormat("000000");
        for (TrajectoryData td : trajectories)
        	saveData(td, "trajectories", "trajectory" + df.format(++trajectoriesSaved) + ".dat");
        trajectories.clear();
    }

    /**
     * Utility to load trajectory data from file.
     * @param file .dat file of object.
     * @return Trajectory data.
     */
    public static TrajectoryData loadTrajectoryData(String file) {
        TrajectoryData out = null;
        try {
            out = (TrajectoryData) loadData(file);
        } catch (java.lang.ClassCastException cce) {
            throw new RuntimeException("File "+file+" is no trajectory data.", cce);
        }
        return out;
    }

    /**
     * Saves detector data to disk in a file with the detector id in the name.
     * @param detector Detector.
     */
    public void saveDetectorData(Detector detector) {
        saveData(new DetectorData(detector), "detectors", "detector"+detector.id()+".dat");
    }

    /**
     * Utility to load detector data from file.
     * @param file File of object.
     * @return Detector data.
     */
    public static DetectorData loadDetectorData(String file) {
        DetectorData out = null;
        try {
            out = (DetectorData) loadData(file);
        } catch (java.lang.ClassCastException cce) {
            throw new RuntimeException("File "+file+" is no detector data.", cce);
        }
        return out;
    }
    
    /**
     * Saves an object to disk in the output folder.
     * @param obj Serializable object to save.
     * @param subPath Subpath in the output folder, may be <tt>null</tt> or empty.
     * @param fileName Filename, including extension.
     */
    public void saveData(Object obj, String subPath, String fileName) {
        try {
            java.io.File f;
            if (subPath==null || subPath.isEmpty())
                f = new java.io.File(settings.getString("outputDir"));
            else
                f = new java.io.File(settings.getString("outputDir"), subPath);
            f.mkdir();
            java.io.FileOutputStream fos;
            if (subPath==null || subPath.isEmpty())
                fos = new java.io.FileOutputStream(settings.getString("outputDir") + "/" + fileName);
            else
                fos = new java.io.FileOutputStream(settings.getString("outputDir") + "/" + subPath + "/" + fileName);
            java.io.BufferedOutputStream bos = new java.io.BufferedOutputStream(fos);
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(obj.getClass().cast(obj));
            oos.close();
        } catch (Exception e) {
            throw new RuntimeException("Unable to write to file " + fileName + ".", e);
        }
    }
    
    /**
     * Load file as a java object. Explicit casting is required to define the 
     * object as being from the actual class.
     * @param file File.
     * @return Object that is loaded.
     * @throws Exception If file could not be loaded.
     */
    public static Object loadData(String file) {
        Object out = null;
        try {
            java.io.FileInputStream fis = new java.io.FileInputStream(file);
            java.io.ObjectInputStream ois = new java.io.ObjectInputStream(fis);
            out = ois.readObject();
            fis.close();
        } catch (Exception e) {
            throw new RuntimeException("Unable to load file " + file + ".", e);
        }
        return out;
    }

    /** Enumeration of longitudinal directions. */
    public enum longDirection {
        /** Upstream direction. */
        UP, 
        /** Downstream direction. */
        DOWN
    }
    
    /** Enumeration of lateral directions. */
    public enum latDirection {
        /** Left direction. */
        LEFT, 
        /** Right direction. */
        RIGHT
    }

	@Override
	public String saveStateToString() {
		// TODO Auto-generated method stub
		return null;
	}
}