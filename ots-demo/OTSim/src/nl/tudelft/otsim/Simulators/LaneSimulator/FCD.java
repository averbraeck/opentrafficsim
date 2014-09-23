package nl.tudelft.otsim.Simulators.LaneSimulator;

/**
 * Snapshot of vehicle state. A series of snapshots can be used as a trajectory  
 * or floating car data. To extend the information in such data this class can
 * be extended. Any additional fields are automatically available in 
 * <tt>jTrajectory</tt> and <tt>jTrajectoryData</tt>. Subclasses should have a
 * constructor with a <tt>jVehicle</tt> as input. Use <tt>addScalars</tt> to 
 * include scalar fields in stored trajectory data (<tt>jTrajectoryData</tt>) 
 * and use <tt>asPrimitive</tt> for non-primitive data types to translate them
 * into a primitive data type (e.g. by giving an id).
 */
public class FCD {

    /** Time of snapshot. */
    public double t;

    /** Position of vehicle on the lane. */
    public double x;

    /** Speed of vehicle [m/s]. */
    public double v;

    /** Acceleration of vehicle [m/s^2]. */
    public double a;

    /** Lane at which the vehicle is. */
    public Lane lane;

    /** Lane changing progress of vehicle, including direction [-1...1]. */
    public double lcProgress;

    /**
     * Constructs a snapshot of the given vehicle.
     * @param veh
     */
    public FCD(Vehicle veh) {
        t = veh.model.t;
        x = veh.x;
        v = veh.v;
        a = veh.a;
        lane = veh.getLane();
        if (veh.lcDirection==Model.latDirection.LEFT)
            lcProgress = -veh.lcProgress; // [-1...0]
        else
            lcProgress = veh.lcProgress; // [0...1]
    }
    
    /**
     * Adds scalar fields in trajectory data. This method can be overridden by 
     * subclasses to add additional scalar fields in a <tt>jTrajectoryData</tt>.
     * @param t Trajectory data object.
     * @param veh Vehicle to get information from.
     */
    public static void addScalars(TrajectoryData t, Vehicle veh) {
        t.put("classID", veh.classID);
    }
    
    /**
     * Represents a non-primitive field of an FCD object as a primitive data 
     * type. A typical example is to get the id from objects. This method can
     * be overridden by subclasses to add additional primitive representations 
     * of non-primitive data fields.
     * @param field Field of FCD object.
     * @param obj Object that is the field in underlying FCD object.
     * @return Primitive representation of non-primitive field.
     */
    public static Object asPrimitive(java.lang.String field, Object obj) {
        if (field.equals("lane"))
            return ((Lane) obj).id;
        return null;
    }
}