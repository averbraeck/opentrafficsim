package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context;

import java.util.HashMap;
import java.util.Map;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;

/**
 * Abstract base class for all context categories in the MiRoVA framework.
 * <p>
 * Context categories form the foundation of <b>Layer 1 (Perception & Context)</b>
 * in the MiRoVA architecture. They manage the "world knowledge" of the agent by
 * structuring raw sensor data into semantic contexts (e.g., Ego, Neighbors, Infrastructure).
 * </p>
 * <p>
 * This base class provides:
 * <ul>
 * <li>Lazy evaluation and per-tick caching for all context values</li>
 * <li>Automatic cache invalidation per simulation tick</li>
 * <li>A unified interface for storing and retrieving context values</li>
 * </ul>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public abstract class ContextCategory {

    /** Category name (e.g., "Neighbors", "Infrastructure"). */
    protected final String name;

    /** Associated vehicle. */
    protected final MirovaTacticalPlanner vehicle;

    /** Local storage of all context values (key → value). */
    protected final Map<String, Object> values = new HashMap<>();

    /** Per-tick cache for lazily evaluated quantities. */
    protected final Map<String, Object> cache = new HashMap<>();

    /** Tick ID for which this cache is valid. */
    protected long cacheTick = -1;

    // ----------------------------------------------------------------------
    // Construction
    // ----------------------------------------------------------------------

    /**
     * Initializes a new context category.
     *
     * @param name    the name of the category (e.g., "Ego", "Neighbors")
     * @param vehicle the MiRoVA tactical planner (ego vehicle) managing this context
     */
    protected ContextCategory(final String name, final MirovaTacticalPlanner vehicle) {
        this.name = name;
        this.vehicle = vehicle;
    }

    // ----------------------------------------------------------------------
    // Lazy evaluation and cache interface
    // ----------------------------------------------------------------------

    /** * Clears all cached values. This should be called once per simulation tick.
     */
    public void invalidateCache() {
        this.cache.clear();
        this.cacheTick = -1;
    }

    /** * Returns whether the cache is valid for the current simulation tick.
     *
     * @return {@code true} if the cache is up to date, {@code false} otherwise
     */
    protected boolean isCacheValid() {
        if (this.vehicle == null || this.vehicle.getContextManager() == null) {
            return false;
        }
        return this.cacheTick == this.vehicle.getContextManager().getCurrentTick();
    }

    /** * Marks the cache as valid and updated for the current simulation tick.
     */
    protected void markCacheValid() {
        if (this.vehicle != null && this.vehicle.getContextManager() != null) {
            this.cacheTick = this.vehicle.getContextManager().getCurrentTick();
        }
    }

    /** * Stores a value in the cache and optionally in the persistent context value map.
     *
     * @param <T>           the type of the value
     * @param key           the unique identifier for the value
     * @param value         the actual value to cache
     * @param storeInValues {@code true} to also persist the value in the regular value map, {@code false} for cache only
     */
    public <T> void cacheValue(final String key, final T value, final boolean storeInValues) {
        this.cache.put(key, value);
        if (storeInValues) {
            this.values.put(key, value);
        }
        markCacheValid();
    }

    /** * Retrieves a cached value, or returns {@code null} if not present or outdated.
     *
     * @param <T>  the expected type of the value
     * @param key  the unique identifier for the value
     * @param type the class representing the expected type
     * @return the cached value, or {@code null} if the cache is invalid, the key is missing, or the type mismatches
     */
    @SuppressWarnings("unchecked")
    public <T> T getCachedValue(final String key, final Class<T> type) {
        if (!isCacheValid()) {
            return null;
        }
        Object v = this.cache.get(key);
        return (v != null && type.isInstance(v)) ? (T) v : null;
    }

    // ----------------------------------------------------------------------
    // Value store interface (ContextCategory base functionality)
    // ----------------------------------------------------------------------

    /** * Sets or updates a context value.
     *
     * @param <T>   the type of the value
     * @param key   the unique identifier for the value
     * @param value the value to store
     */
    public <T> void setValue(final String key, final T value) {
        this.values.put(key, value);
    }

    /** * Retrieves a stored context value.
     *
     * @param <T>  the expected type of the value
     * @param key  the unique identifier for the value
     * @param type the class representing the expected type
     * @return the stored value, or {@code null} if the key is missing or the type mismatches
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(final String key, final Class<T> type) {
        Object v = this.values.get(key);
        return (v != null && type.isInstance(v)) ? (T) v : null;
    }

    /** * Returns a copy of all context values currently stored in this category.
     *
     * @return an unmodifiable map containing all stored key-value pairs
     */
    public Map<String, Object> getAllValues() {
        return Map.copyOf(this.values);
    }

    /** * Returns the name of this context category.
     *
     * @return the category name
     */
    public String getName() {
        return this.name;
    }

    /** * Returns the vehicle associated with this context category.
     *
     * @return the MiRoVA tactical planner
     */
    public MirovaTacticalPlanner getVehicle() {
        return this.vehicle;
    }

    /**
     * Returns a string representation of this context category.
     *
     * @return the string representation
     */
    @Override
    public String toString() {
        return this.name + "Context[" + this.values + "]";
    }
}