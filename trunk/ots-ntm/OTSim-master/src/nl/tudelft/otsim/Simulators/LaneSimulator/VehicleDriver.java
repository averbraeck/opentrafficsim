package nl.tudelft.otsim.Simulators.LaneSimulator;

/**
 * General shell describing a vehicle-driver class. <tt>jClass</tt> is not to be
 * confused with java classes. This class is a default wrapping of a
 * vehicle-driver class. The <tt>defaultVehicle</tt> property houses a user 
 * defined vehicle that defines the class. The <tt>generateVehicle()</tt> method 
 * returns a copy of this vehicle. Additionally, stochastic parameters of the 
 * vehicle and the driver can be defined by <tt>addStochasticVehicleParameter</tt> 
 * and <tt>addStochasticDriverParameter</tt>. Stochastic parameters must be
 * <tt>double</tt>.<br>
 * For more complex definitions of stochastic parameters, new java classes have
 * to be defined extending this class. In that class override the
 * <tt>generateVehicle()</tt> method and use <tt>super.generateVehicle()</tt>
 * to provide the default vehicle with default stochastic parameters, which can
 * then be adapted. Note that both the OBU and the driver also belong to the
 * vehicle, including all their attributes (parameters).
 */
public class VehicleDriver {

    /** Connection to the main model. */
    public Model model;

    /** Defines the class, including driver and OBU. */
    protected Vehicle defaultVehicle;

    /** Number of the class which is for the user only. */
    protected int id;

    /** Set of distributions for vehicle parameters. */
    protected java.util.HashMap<String, distr> stochasticVehicleParameters = 
            new java.util.HashMap<String, distr>();

    /** Set of distributions for driver parameters. */
    protected java.util.HashMap<String, distr> stochasticDriverParameters =
            new java.util.HashMap<String, distr>();

    /**
     * Constructor.
     * @param model Main model.
     * @param vehicle Default vehicle of this class. This vehicle defines the class.
     * @param id Number for class recogniztion by the user.
     */
    // using this at the end is ok, class is fully initialized
    public VehicleDriver(Model model, Vehicle vehicle, int id){
        this.model = model;
        this.id = id;
        this.defaultVehicle = vehicle;
        model.addClass(this);
    }

    /**
     * Generates a new vehicle. By default this is a copy of <tt>defaultVehicle</tt>
     * including the driver and OBU with optional stochastic parameters of the 
     * driver and the vehicle. The OBU is initialized.
     * @return A new vehicle of this class.
     */
    public Vehicle generateVehicle() {
        // start of with the default vehicle
        Vehicle veh = copyDefaultVehicle();
        // set any stochastic parameters
        setStochasticParameters(veh);
        // initialize OBU
        if (veh.isEquipped()) {
            veh.OBU.init();
        }
        return veh;
    }

    /**
     * Returns a new copy of the default vehicle including a copy of the OBU and
     * driver. All accessible primitive attributes of the OBU and driver are 
     * copied to the new OBU or driver.
     * @return Default vehicle copy.
     */
    protected Vehicle copyDefaultVehicle() {
        // new vehicle
        Vehicle veh = new Vehicle(model);

        // copy or set all relevant vehicle attributes
        veh.aMin = defaultVehicle.aMin;
        veh.l = defaultVehicle.l;
        veh.marker = defaultVehicle.marker;
        if (defaultVehicle.trajectory != null) {
            try {
                veh.trajectory = defaultVehicle.trajectory.getClass().getDeclaredConstructor(
                        defaultVehicle.getClass(), String.class).newInstance(veh, defaultVehicle.trajectory.getFCDclass().getName());
            } catch (Exception e) {
                throw new java.lang.RuntimeException("Could not instantiate a new trajectory for class "+
                    defaultVehicle.trajectory.getClass().getName()+". Make sure the class has "+
                    "a public constructor with (jVehicle, String) input.", e);
            }
        }
        veh.vMax = defaultVehicle.vMax;
        try {
            veh.driver = defaultVehicle.driver.getClass().getDeclaredConstructor(
                    defaultVehicle.getClass()).newInstance(veh);
        } catch (Exception e) {
            throw new java.lang.RuntimeException("Could not instantiate a new driver of class "+
                defaultVehicle.driver.getClass().getName()+". Make sure the class has "+
                "a public constructor with a single jVehicle parameter.", e);
        }
        veh.classID = id;

        // set OBU
        if (defaultVehicle.isEquipped()) {
            try {
                veh.OBU = defaultVehicle.OBU.getClass().getDeclaredConstructor(
                        defaultVehicle.getClass()).newInstance(veh);
            } catch (Exception e) {
                throw new java.lang.RuntimeException("Could not instantiate a new OBU of class "+
                    defaultVehicle.OBU.getClass().getName()+". Make sure the class has "+
                    "a public constructor with a single jVehicle parameter.", e);
            }
            // copy OBU parameters
            java.lang.reflect.Field[] fields = veh.OBU.getClass().getFields();
            for (int i=0; i<fields.length; i++) {
                if (fields[i].getType().isPrimitive()) {
                    try {
                        if (fields[i].get(defaultVehicle.OBU) instanceof Double) {
                            fields[i].setDouble(veh.OBU, fields[i].getDouble(defaultVehicle.OBU));
                        } else if (fields[i].get(defaultVehicle.OBU) instanceof Integer) {
                            fields[i].setInt(veh.OBU, fields[i].getInt(defaultVehicle.OBU));
                        } else if (fields[i].get(defaultVehicle.OBU) instanceof Boolean) {
                            fields[i].setBoolean(veh.OBU, fields[i].getBoolean(defaultVehicle.OBU));
                        } else if (fields[i].get(defaultVehicle.OBU) instanceof Character) {
                            fields[i].setChar(veh.OBU, fields[i].getChar(defaultVehicle.OBU));
                        } else if (fields[i].get(defaultVehicle.OBU) instanceof Long) {
                            fields[i].setLong(veh.OBU, fields[i].getLong(defaultVehicle.OBU));
                        } else if (fields[i].get(defaultVehicle.OBU) instanceof Float) {
                            fields[i].setFloat(veh.OBU, fields[i].getFloat(defaultVehicle.OBU));
                        } else if (fields[i].get(defaultVehicle.OBU) instanceof Byte) {
                            fields[i].setByte(veh.OBU, fields[i].getByte(defaultVehicle.OBU));
                        } else if (fields[i].get(defaultVehicle.OBU) instanceof Short) {
                            fields[i].setShort(veh.OBU, fields[i].getShort(defaultVehicle.OBU));
                        }
                    } catch (IllegalAccessException e) {
                        
                    }
                }
            }
        }

        // copy driver parameters
        java.lang.reflect.Field[] fields = veh.driver.getClass().getFields();
        for (int i=0; i<fields.length; i++) {
            if (fields[i].getType().isPrimitive()) {
                try {
                    if (fields[i].get(defaultVehicle.driver) instanceof Double) {
                        fields[i].setDouble(veh.driver, fields[i].getDouble(defaultVehicle.driver));
                    } else if (fields[i].get(defaultVehicle.driver) instanceof Integer) {
                        fields[i].setInt(veh.driver, fields[i].getInt(defaultVehicle.driver));
                    } else if (fields[i].get(defaultVehicle.driver) instanceof Boolean) {
                        fields[i].setBoolean(veh.driver, fields[i].getBoolean(defaultVehicle.driver));
                    } else if (fields[i].get(defaultVehicle.driver) instanceof Character) {
                        fields[i].setChar(veh.driver, fields[i].getChar(defaultVehicle.driver));
                    } else if (fields[i].get(defaultVehicle.driver) instanceof Long) {
                        fields[i].setLong(veh.driver, fields[i].getLong(defaultVehicle.driver));
                    } else if (fields[i].get(defaultVehicle.driver) instanceof Float) {
                        fields[i].setFloat(veh.driver, fields[i].getFloat(defaultVehicle.driver));
                    } else if (fields[i].get(defaultVehicle.driver) instanceof Byte) {
                        fields[i].setByte(veh.driver, fields[i].getByte(defaultVehicle.driver));
                    } else if (fields[i].get(defaultVehicle.driver) instanceof Short) {
                        fields[i].setShort(veh.driver, fields[i].getShort(defaultVehicle.driver));
                    }
                } catch (IllegalAccessException e) {
                    
                }
            }
        }
        return veh;
    }

    /**
     * Sets all stochastic parameters of the driver and vehicle. Also calls the
     * <tt>correlateParameter()</tt> methods of the vehicle and driver.
     * @param veh Vehicle for stochastic parameters
     */
    protected void setStochasticParameters(Vehicle veh) {
        // Set stochastic driver parameters
        
        if (stochasticDriverParameters.size()>0) {
            int setParams = 0;
            java.lang.reflect.Field[] fields = veh.getDriver().getClass().getFields();
            for (int i=1; i<fields.length; i++) {
                java.lang.String param = fields[i].getName();
                if (stochasticDriverParameters.containsKey(param)) {
                    double value = stochasticDriverParameters.get(param).rand();
                    try {
                        fields[i].setDouble(veh.getDriver(), value);
                        setParams++;
                    } catch (IllegalAccessException e) {
                        throw new java.lang.RuntimeException("Unable to set stochastic driver parameter "+
                                param+" as it could not be accessed.", e);
                    }
                }
            }
            if (setParams<stochasticDriverParameters.size()) {
                throw new java.lang.RuntimeException("Not all stochastic driver parameters correspond to a driver attribute.");
            }
        }

        // Set stochastic vehicle parameters
        if (stochasticVehicleParameters.size()>0) {
            int setParams = 0;
            java.lang.reflect.Field[] fields = veh.getClass().getFields();
            for (int i=1; i<fields.length; i++) {
                java.lang.String param = fields[i].getName();
                if (stochasticVehicleParameters.containsKey(param)) {
                    double value = stochasticVehicleParameters.get(param).rand();
                    try {
                        fields[i].setDouble(veh, value);
                        setParams++;
                    } catch (IllegalAccessException e) {
                        throw new java.lang.RuntimeException("Unable to set stochastic vehicle parameter "+
                                param+" as it could not be accessed.", e);
                    }
                }
            }
            if (setParams<stochasticVehicleParameters.size()) {
                throw new java.lang.RuntimeException("Not all stochastic vehicle parameters correspond to a vehicle attribute.");
            }
        }

        // Set correlated parameters
        veh.correlateParameters();
        veh.getDriver().correlateParameters();
    }
    
    /**
     * Returns the ID of this class.
     * @return ID of this class.
     */
    public int id() {
        return id;
    }
    
    /**
     * Adds a stochastic parameter for vehicles of this class.
     * @param param Name of stochastic vehicle parameters.
     * @param distr Distribution of random parameter.
     * @param mean Mean of distribution.
     * @param std Standard deviation of distribution (only used if the distribution has it).
     */
    public void addStochasticVehicleParameter(java.lang.String param,
            distribution distr, double mean, double std) {
        if (distr==distribution.GAUSSIAN) {
            stochasticVehicleParameters.put(param, new gaussian(mean, std));
        } else if (distr==distribution.EXPONENTIAL) {
            stochasticVehicleParameters.put(param, new exponential(mean));
        } else if (distr==distribution.LOGNORMAL) {
            stochasticVehicleParameters.put(param, new lognormal(mean, std));
        }
    }
    
    /**
     * Adds a stochastic parameter for drivers of this class.
     * @param param Name of stochastic driver parameters.
     * @param distr Distribution of random parameter.
     * @param mean Mean of distribution.
     * @param std Standard deviation of distribution (only used if the distribution has it).
     */
    public void addStochasticDriverParameter(java.lang.String param,
            distribution distr, double mean, double std) {
        if (distr==distribution.GAUSSIAN) {
            stochasticDriverParameters.put(param, new gaussian(mean, std));
        } else if (distr==distribution.EXPONENTIAL) {
            stochasticDriverParameters.put(param, new exponential(mean));
        } else if (distr==distribution.LOGNORMAL) {
            stochasticDriverParameters.put(param, new lognormal(mean, std));
        }
    }
    
    /** Enumeration of possible parameter distributions. */
    public enum distribution {
        /** Gaussian distribution. */
        GAUSSIAN,
        /** Exponential distribution. */
        EXPONENTIAL,
        /** Log-normal distribution. */
        LOGNORMAL
    }
    
    /**
     * Abstract shell for a distribution class.
     */
    protected abstract class distr {
        /**
         * Returns a distributed random number.
         * @return Distributed random number.
         */
        public abstract double rand();
    }
    
    /**
     * Gaussian distribution.
     */
    protected class gaussian extends distr {
        
        /** Mean of distribution. */
        private double mean;
        
        /** Standard deviation of distribution. */
        private double std;
        
        /**
         * Constructor defining mean and standard deviation.
         * @param mean Mean of distribution.
         * @param std Standard deviation of distribution.
         */
        public gaussian(double mean, double std) {
            this.mean = mean;
            this.std = std;
        }
        
        /**
         * Returns a random Gaussian distributed number.
         * @return Gaussian distributed number.
         */
        @Override
		public double rand() {
            return model.random().nextGaussian()*std + mean;
        }
    }
    
    /**
     * Exponential distribution.
     */
    protected class exponential extends distr {
        
        /** Mean of distribution. */
        private double mean;
        
        /**
         * Constructor defining mean (which is also the standard deviation).
         * @param mean Mean of distribution.
         */
        public exponential(double mean) {
            this.mean = mean;
        }
        
        /**
         * Returns a random exponentially distributed number.
         * @return Exponentially distributed number.
         */
        @Override
		public double rand() {
            // note: r = -log(uniform)/gamma & mean = 1/gamma
            return -Math.log(model.random().nextDouble()) * mean;
        }
    }
    
    /**
     * Log-normal distribution.
     */
    protected class lognormal extends distr {
        
        /** Mean of distribution. */
        private double mean;
        
        /** Standard deviation of distribution. */
        private double std;
        
        /**
         * Constructor defining mean and standard deviation.
         * @param mean Mean of distribution.
         * @param std Standard deviation of distribution.
         */
        public lognormal(double mean, double std) {
            this.mean = mean;
            this.std = std;
        }
        
        /**
         * Returns a random log-normal distributed number.
         * @return Log-normal distributed number.
         */
        @Override
		public double rand() {
            return model.lognormal(mean, std);
        }
    }
}