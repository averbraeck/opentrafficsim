package nl.tudelft.otsim.Simulators.LaneSimulator;

/**
 * Simple class to store setting of several primitive types and objects. The 
 * settings are set and retrieved with the put/get methods per data type.
 */
public class Settings {

    /** Set of stored string settings. */
    protected java.util.HashMap<String, String> strings =
            new java.util.HashMap<String, String>();
    
    /** Set of stored double settings. */
    protected java.util.HashMap<String, java.lang.Double> doubles =
            new java.util.HashMap<String, java.lang.Double>();
    
    /** Set of stored boolean settings. */
    protected java.util.HashMap<String, java.lang.Boolean> booleans =
            new java.util.HashMap<String, java.lang.Boolean>();
    
    /** Set of stored integer settings. */
    protected java.util.HashMap<String, java.lang.Integer> integers =
            new java.util.HashMap<String, java.lang.Integer>();
    
    /** Set of stored object settings. */
    protected java.util.HashMap<String, java.lang.Object> objects =
            new java.util.HashMap<String, java.lang.Object>();

    /**
     * Put a string setting.
     * @param key Name of setting.
     * @param val String value of setting.
     */
    public void putString(String key, String val) {
        strings.put(key, val);
    }
    /**
     * Get a string setting.
     * @param key Name of setting.
     * @return String value of setting.
     */
    public String getString(String key) {
        check(strings, key);
        return strings.get(key);
    }
    /**
     * Returns whether the settings contain the string.
     * @param key Name of the setting.
     * @return Whether the setting exists.
     */
    public boolean containsString(String key) {
        return strings.containsKey(key);
    }

    /**
     * Put a double setting.
     * @param key Name of setting.
     * @param val Double value of setting.
     */
    public void putDouble(String key, double val) {
        doubles.put(key, val);
    }
    /**
     * Get a double setting.
     * @param key Name of setting.
     * @return Double value of setting.
     */
    public java.lang.Double getDouble(String key) {
        check(doubles, key);
        return doubles.get(key);
    }
    /**
     * Returns whether the settings contain the double.
     * @param key Name of the setting.
     * @return Whether the setting exists.
     */
    public boolean containsDouble(String key) {
        return doubles.containsKey(key);
    }

    /**
     * Put a boolean setting.
     * @param key Name of setting.
     * @param val Boolean value of setting.
     */
    public void putBoolean(String key, boolean val) {
        booleans.put(key, val);
    }
    /**
     * Get a boolean setting.
     * @param key Name of setting.
     * @return Boolean value of setting.
     */
    public boolean getBoolean(String key) {
        check(booleans, key);
        return booleans.get(key);
    }
    /**
     * Returns whether the settings contain the boolean.
     * @param key Name of the setting.
     * @return Whether the setting exists.
     */
    public boolean containsBoolean(String key) {
        return booleans.containsKey(key);
    }

    /**
     * Put an integer setting.
     * @param key Name of setting.
     * @param val Integer value of setting.
     */
    public void putInteger(String key, int val) {
        integers.put(key, val);
    }
    /**
     * Get an integer setting.
     * @param key Name of setting.
     * @return Integer value of setting.
     */
    public int getInteger(String key) {
        check(integers, key);
        return integers.get(key);
    }
    /**
     * Returns whether the settings contain the integer.
     * @param key Name of the setting.
     * @return Whether the setting exists.
     */
    public boolean containsInteger(String key) {
        return integers.containsKey(key);
    }

    /**
     * Put an object setting.
     * @param key Name of setting.
     * @param obj Object of setting.
     */
    public void putObject(String key, java.lang.Object obj) {
        objects.put(key, obj);
    }
    /**
     * Get an object setting.
     * @param key Name of setting.
     * @return Object of setting.
     */
    public java.lang.Object getObject(String key) {
        check(objects, key);
        return objects.get(key);
    }
    /**
     * Returns whether the settings contain the object.
     * @param key Name of the setting.
     * @return Whether the setting exists.
     */
    public boolean containsObject(String key) {
        return objects.containsKey(key);
    }
    
    /**
     * Checks whether the given map contains the requested key. Creates an error
     * if not.
     */
    protected static void check(java.util.HashMap<?, ?> map, String key) {
        if (! map.containsKey(key))
            throw new java.lang.RuntimeException("Setting '" + key + "' does not exist as the requested type.");
    }
}