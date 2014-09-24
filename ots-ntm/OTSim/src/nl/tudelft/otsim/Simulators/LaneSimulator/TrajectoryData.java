package nl.tudelft.otsim.Simulators.LaneSimulator;

/**
 * Storable trajectory data from a <tt>jTrajectory</tt> object.
 */
public class TrajectoryData implements java.io.Serializable {
	private static final long serialVersionUID = 1L;
	/** List of data objects by field name. */
    protected java.util.HashMap<String, Object> data = new java.util.HashMap<String, Object>();

    /**
     * Constructs a data object from the given <tt>jTrajectory</tt>.
     * @param trajectory Trajectory of which the data needs to be stored.
     */
    public TrajectoryData(Trajectory trajectory) {
        // Get FCD fields as array
        for (java.lang.reflect.Field field : trajectory.getFCDclass().getFields())
            data.put(field.getName(), trajectory.getAsPrimitive(field));
        // Add scalar fields
        trajectory.addScalars(this);
    }
    
    /**
     * Returns the appropriate data.
     * @param field Name of field in FCD that is required.
     * @return Data given by field.
     */
    public Object get(java.lang.String field) {
        return data.get(field);
    }
    
    /**
     * Adds an object in the data memory. The data should be serializable.
     * @param field Name of data field.
     * @param additionalData Data to add.
     */
    public void put(String field, Object additionalData) {
        this.data.put(field, additionalData);
    }
    
    /**
     * Returns the set of fields present in the data.
     * @return Set of fields present in the data.
     */
    public String[] getFields() {
        return data.keySet().toArray(new String[0]);
    }
}