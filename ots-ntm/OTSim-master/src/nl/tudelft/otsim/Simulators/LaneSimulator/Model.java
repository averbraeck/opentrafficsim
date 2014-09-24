package nl.tudelft.otsim.Simulators.LaneSimulator;

/**
 * Main model object. This functions as the main interface with the model. It
 * contains general settings, the network, all vehicles etc. Furthermore, it
 * represents the world.
 */
public class Model {
    /** Time step number. Always starts as 0. */
    protected int k;
    
    /** Current time of the model [s]. Always starts at 0. */
    protected double t;
    
    /** Time step size of the model [s]. */
    public double dt;
    
    /** Maximum simulation period [s]. */
    public double period;
    
    /** Absolute start time of simulation. */
    public java.util.Date startTime;
    
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
    
    /** Expandable set of general modelling settings. */
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
        for (int i=0; i<network.length; i++) {
            network[i].init();
        }
        
        // Initialize controllers
        for (int i=0; i<controllers.size(); i++) {
            controllers.get(i).init();
        }
    }

    /**
     * Performs the main model loop. This entails the RSUs, OBUs, controllers, 
     * vehicle generators and drivers (in this order).
     * @param n Number of loops to be run before returning.
     */
    public void run(int n) {

        // loop n times
        int nn = 0;
        while (nn<n && t<period) {

            // Run on-board units, road-side units and controllers
            runUnits();

            // Vehicle generation
            generating = true;
            for (int i=0; i<network.length; i++) {
                if (network[i].generator!=null) {
                    network[i].generator.run();
                }
            }
            generating = false;

            // Drive
            // copy pointer array as vehicles may be deleted
            java.util.ArrayList<Vehicle> tmp = new java.util.ArrayList<Vehicle>(vehicles.size());
            for (int i=0; i<vehicles.size(); i++) {
                tmp.add(i, vehicles.get(i));
            }
            for (int i=0; i<tmp.size(); i++) {
                tmp.get(i).driver.drive(); // sets a and dy
                if (tmp.get(i).dy!=0 && tmp.get(i).lcProgress==0) {
                    tmp.get(i).startLaneChange();
                }
            }

            // Move
            // copy pointer array as vehicles may be deleted
            tmp.clear();
            for (int i=0; i<vehicles.size(); i++) {
                tmp.add(i, vehicles.get(i));
            }
            for (int i=0; i<tmp.size(); i++) {
                tmp.get(i).move(); // performs a and dy
            }

            // Update all neighbour references (overtaking)
            for (int i=0; i<vehicles.size(); i++) {
                vehicles.get(i).updateNeighbours();
            }
            for (int i=0; i<lcVehicles.size(); i++) {
                lcVehicles.get(i).updateNeighbours();
            }

            // End lane changes
            for (int i=0; i<vehicles.size(); i++) {
                if (vehicles.get(i).lcProgress>=1) {
                    vehicles.get(i).endLaneChange();
                }
            }
            
            // Check for collisions
            if (debug) {
                Vehicle veh;
                for (int i=0; i<vehicles.size(); i++) {
                    veh = vehicles.get(i);
                    if (veh.down!=null && veh.getHeadway(veh.down)<0 &&
                            (veh.lane==veh.down.lane || !veh.down.lane.isMerge())) {
                        System.err.println("Collision: "+veh.x+"@"+veh.lane.id);
                    }
                }
            }

            // Update time
            k = k+1; // time step number
            t = k*dt; // time [s]

            nn++;
        }
    }
    
    /**
     * Runs all road-side units, on-board units and controllers. This is part of
     * a regular time step, aswell as gathering the final data after simulation.
     * This is because the model will only run while t&lt;period. If however a
     * units needs to aggregate data at t=period, that will not happen in the
     * main model loop.
     */
    protected void runUnits() {
        // Run road-side units
        for (int i=0; i<network.length; i++) {
            for (int j=0; j<network[i].RSUcount(); j++) {
                network[i].getRSU(j).run();
            }
        }
        // Run on-board units
        for (int i=0; i<vehicles.size(); i++) {
            if (vehicles.get(i).isEquipped()) {
                vehicles.get(i).OBU.run();
            }
        }
        // Run controllers
        for (int i=0; i<controllers.size(); i++) {
            controllers.get(i).run();
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
        for (int i=0; i<classes.size(); i++) {
            if (classes.get(i).id() == id) {
                return classes.get(i);
            }
        }
        return null;
    }
    
    /**
     * Adds a vehicle in the simulation.
     * @param vehicle Vehicle to add.
     */
    public void addVehicle(Movable vehicle) {
        if (vehicle instanceof Vehicle) {
            vehicles.add((Vehicle) vehicle);
        } else if (vehicle instanceof LCVehicle) {
            lcVehicles.add((LCVehicle) vehicle);
        }
    }
    
    /**
     * Removes a vehicle from the simulation.
     * @param vehicle Vehicle to remove.
     */
    public void removeVehicle(Movable vehicle) {
        if (vehicle instanceof Vehicle) {
            vehicles.remove(vehicle);
        } else if (vehicle instanceof LCVehicle) {
            lcVehicles.remove(vehicle);
        }
    }
    
    /**
     * Returns whether a vehicle still exists in simulation. This can be used by
     * for example controllers that maintain an internal bookkeeping of 
     * vehicles. Vehicles that no longer exist can be removed.
     * @param vehicle Vehicle to check existence of.
     * @return Whether vehicle exists in simulation.
     */
    public boolean exists(Movable vehicle) {
        if (vehicle instanceof Vehicle) {
            return vehicles.contains(vehicle);
        } else {
            return lcVehicles.contains(vehicle);
        }
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
    
    /**
     * Displays messages whenever any movable in simulation has a pointer to the
     * given movable. This can be used to check whether the given movable was 
     * correctly cut from simulation after the <tt>jMovable.cut()</tt> method.
     * @param movable Cut movable.
     */
    public void checkForRemainingPointers(Movable movable) {
        Lane lane = movable.lane;
        double x = movable.x;
        java.util.Iterator<Vehicle> iter = vehicles.iterator();
        while (iter.hasNext()) {
            Vehicle veh = iter.next();
            if (veh.up==movable || veh.down==movable || veh.leftUp==movable ||
                    veh.leftDown==movable || veh.rightUp==movable || veh.rightDown==movable) {
                throw new RuntimeException("Cut vehicle: "+x+"@"+lane.id+
                        ", still connected: "+veh.x+"@"+veh.lane.id);
            }
        }
        java.util.Iterator<LCVehicle> iterLc = lcVehicles.iterator();
        while (iterLc.hasNext()) {
            LCVehicle veh = iterLc.next();
            if (veh.up==movable || veh.down==movable || veh.leftUp==movable ||
                    veh.leftDown==movable || veh.rightUp==movable || veh.rightDown==movable) {
                throw new java.lang.RuntimeException("Cut vehicle: "+x+"@"+lane.id+
                        ", still connected: "+veh.x+"@"+veh.lane.id);
            }
        }
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
        if (startTime==null) {
            return null;    
        } else {
            return new java.util.Date(startTime.getTime() + (long) (t*1000));
        }
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
     * @return Random lognormally distributed number.
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
            for (int i=0; i<vehicles.size(); i++) {
                if (vehicles.get(i).trajectory!=null) {
                    saveTrajectoryData(vehicles.get(i).trajectory);
                }
            }
            saveTrajectoryBufferToDisk();
        }
        if (settings.getBoolean("storeDetectorData")) {
            // Store detector data
            for (int i=0; i<network.length; i++) {
                for (int j=0; j<network[i].RSUcount(); j++) {
                    if (network[i].getRSU(j) instanceof Detector) {
                        saveDetectorData((Detector) network[i].getRSU(j));
                    }
                }
            }
        }
    }

    /**
     * Saves trajectory in memory and saves buffer to disk if it is full.
     * @param trajectory Trajectory to save.
     */
    public synchronized void saveTrajectoryData(Trajectory trajectory) {
        trajectories.add(trajectory.asSerializable());
        if (trajectories.size() >= settings.getInteger("trajectoryBuffer")) {
            saveTrajectoryBufferToDisk();
        }
    }
    
    /**
     * Saves the trajectory buffer, no matter what size, to disk. The buffer
     * will be empty afterwards.
     */
    protected synchronized void saveTrajectoryBufferToDisk() {
        // Save trajectories to disk
        java.text.DecimalFormat df = new java.text.DecimalFormat("000000");
        for (int i=0; i<trajectories.size(); i++) {
            trajectoriesSaved++;
            saveData(trajectories.get(i), "trajectories", "trajectory"+df.format(trajectoriesSaved)+".dat");
        }
        // Clear trajectories
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
            if (subPath==null || subPath.isEmpty()) {
                f = new java.io.File(settings.getString("outputDir"));
            } else {
                f = new java.io.File(settings.getString("outputDir"), subPath);
            }
            boolean ok = f.mkdir();
            java.io.FileOutputStream fos;
            if (subPath==null || subPath.isEmpty()) {
                fos = new java.io.FileOutputStream(
                    settings.getString("outputDir")+"/"+fileName);
            } else {
                fos = new java.io.FileOutputStream(
                    settings.getString("outputDir")+"/"+subPath+"/"+fileName);
            }
            java.io.BufferedOutputStream bos = new java.io.BufferedOutputStream(fos);
            java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(obj.getClass().cast(obj));
            oos.close();
        } catch (Exception e) {
            throw new RuntimeException("Unable to write to file "+fileName+".", e);
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
            throw new RuntimeException("Unable to load file "+file+".", e);
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
}