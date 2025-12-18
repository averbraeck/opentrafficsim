package org.opentrafficsim.demo.mirova.scenariomanagement;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * ScenarioParameters
 * ------------------
 *
 * A structured container for all configurable scenario parameters.
 * This class is intended to:
 *
 *  - Store scalar input values (densities, demand, desired-speed configs, etc.)
 *  - Provide a consistent configuration passed into ScenarioGenerator
 *  - Support parameter sweeps across multiple runs
 *  - Provide immutable accessors to avoid accidental modification
 *
 * Parameters are stored as a typed map internally but exposed via strongly typed getters/setters.
 *
 */
public class ScenarioParameters {

    /** Internal storage of parameters. */
    private final Map<String, Object> params = new HashMap<>();

    // ----------------------------------------------------------------------
    // Constructors
    // ----------------------------------------------------------------------

    public ScenarioParameters() {
        // put optional defaults here, or leave empty
    }

    public ScenarioParameters(final Map<String, Object> initialValues) {
        this.params.putAll(initialValues);
    }

    // ----------------------------------------------------------------------
    // Generic setter & getter
    // ----------------------------------------------------------------------

    /**
     * Sets a parameter to a given value.
     *
     * @param key   the parameter name
     * @param value the parameter value
     */
    public ScenarioParameters set(final String key, final Object value) {
        this.params.put(key, value);
        return this;
    }

    /**
     * Returns a parameter value or null if missing.
     */
    @SuppressWarnings("unchecked")
    public <T> T get(final String key, final Class<T> type) {
        Object value = this.params.get(key);
        if (value == null)
            return null;

        if (!type.isInstance(value)) {
            throw new IllegalArgumentException(
                "Parameter '" + key + "' expected type " + type.getSimpleName() +
                " but found " + value.getClass().getSimpleName()
            );
        }
        return (T) value;
    }

    /**
     * Returns a parameter with a default value if missing.
     */
    public <T> T getOrDefault(final String key, final T defaultValue, final Class<T> type) {
        T value = get(key, type);
        return value != null ? value : defaultValue;
    }

    /**
     * Returns all parameter entries in an unmodifiable map.
     */
    public Map<String, Object> asUnmodifiableMap() {
        return Collections.unmodifiableMap(this.params);
    }

    /**
     * Applies all parameters from the given ScenarioParameters object onto this instance.
     * Only parameters present in the override set will be overwritten.
     *
     * @param overrides ScenarioParameters containing the values to override
     * @return this (for fluent chaining)
     */
    public ScenarioParameters applyOverridesFrom(final ScenarioParameters overrides) {
        if (overrides == null) {
            return this;
        }
        for (Map.Entry<String, Object> entry : overrides.asUnmodifiableMap().entrySet()) {
            this.params.put(entry.getKey(), entry.getValue());
        }
        return this;
    }



    // ----------------------------------------------------------------------
    // Convenience typed getters/setters for common parameters
    // ----------------------------------------------------------------------

    // Typical keys used in simulation scenario design
    public static final String KEY_DEMAND = "demand";
    public static final String KEY_TRUCK_SHARE = "truckShare";
    public static final String KEY_SEED = "seed";
    public static final String KEY_DESIRED_SPEED_DISTRIBUTION = "desiredSpeedDistribution";
    public static final String KEY_NETWORK_NAME = "networkName";
    public static final String KEY_RANDOM_STREAM = "randomStream";
    public static final String KEY_MERGE_SHARE = "mergeShare";

    // ----- demand -----
    public ScenarioParameters setDemand(final double vehPerHour) {
        return set(KEY_DEMAND, vehPerHour);
    }

    public Double getDemand() {
        return get(KEY_DEMAND, Double.class);
    }

    // ----- truck share -----
    public ScenarioParameters setTruckShare(final double share) {
        return set(KEY_TRUCK_SHARE, share);
    }

    public Double getTruckShare() {
        return get(KEY_TRUCK_SHARE, Double.class);
    }

    // ----- random seed -----
    public ScenarioParameters setSeed(final long seed) {
        return set(KEY_SEED, seed);
    }

    public Long getSeed() {
        return get(KEY_SEED, Long.class);
    }

    // ----- Merge share -----
    public ScenarioParameters setMergeShare(final double share) {
        return set(KEY_MERGE_SHARE, share);
    }

    public Double getMergeShare() {
        return get(KEY_MERGE_SHARE, Double.class);
    }

    // ----- speed distribution -----
    public ScenarioParameters setDesiredSpeedDistribution(final Object dist) {
        return set(KEY_DESIRED_SPEED_DISTRIBUTION, dist);
    }

    public Object getDesiredSpeedDistribution() {
        return get(KEY_DESIRED_SPEED_DISTRIBUTION, Object.class);
    }

    // ----- network selection (optional) -----
    public ScenarioParameters setNetworkName(final String name) {
        return set(KEY_NETWORK_NAME, name);
    }

    public String getNetworkName() {
        return get(KEY_NETWORK_NAME, String.class);
    }

    // ----------------------------------------------------------------------
    // Helper
    // ----------------------------------------------------------------------

    /**
     * @return
     */
    public ScenarioParameters copy() {
        return new ScenarioParameters(new HashMap<>(this.params));
    }

    @Override
    public String toString() {
        return "ScenarioParameters" + this.params.toString();
    }
}
