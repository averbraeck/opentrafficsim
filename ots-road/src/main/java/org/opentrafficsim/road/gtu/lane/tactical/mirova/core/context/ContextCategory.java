package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context;

import java.util.HashMap;
import java.util.Map;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.VehicleTypes.AbstractMirovaVehicle;

/**
 * Abstract base class for all context categories in the MIROVA framework.
 * <p>
 * Provides:
 * <ul>
 *   <li>Lazy evaluation and per-tick caching for all context values</li>
 *   <li>Automatic cache invalidation per simulation tick</li>
 *   <li>Unified interface for storing and retrieving context values</li>
 * </ul>
 * </p>
 */
public abstract class ContextCategory {

    /** Category name (e.g., "Neighbors", "Infrastructure"). */
    protected final String name;

    /** Associated vehicle. */
    protected final AbstractMirovaVehicle vehicle;

    /** Local storage of all context values (key → value). */
    protected final Map<String, Object> values = new HashMap<>();

    /** Per-tick cache for lazily evaluated quantities. */
    protected final Map<String, Object> cache = new HashMap<>();

    /** Tick ID for which this cache is valid. */
    protected long cacheTick = -1;

    // ----------------------------------------------------------------------
    // Construction
    // ----------------------------------------------------------------------

    protected ContextCategory(final String name, final AbstractMirovaVehicle vehicle) {
        this.name = name;
        this.vehicle = vehicle;
    }

    // ----------------------------------------------------------------------
    // Lazy evaluation and cache interface
    // ----------------------------------------------------------------------

    /** Clears all cached values (called once per simulation tick). */
    public void invalidateCache() {
        this.cache.clear();
        this.cacheTick = -1;
    }

    /** Returns whether the cache is valid for the current simulation tick. */
    protected boolean isCacheValid() {
        if (this.vehicle == null || this.vehicle.getContextManager() == null)
            return false;
        return this.cacheTick == this.vehicle.getContextManager().getCurrentTick();
    }

    /** Marks the cache as updated for the current simulation tick. */
    protected void markCacheValid() {
        if (this.vehicle != null && this.vehicle.getContextManager() != null)
            this.cacheTick = this.vehicle.getContextManager().getCurrentTick();
    }

    /** Stores a value in the cache and optionally in the context value map. */
    protected <T> void cacheValue(final String key, final T value, final boolean storeInValues) {
        this.cache.put(key, value);
        if (storeInValues)
            this.values.put(key, value);
        markCacheValid();
    }

    /** Retrieves a cached value, or null if not present or outdated. */
    @SuppressWarnings("unchecked")
    protected <T> T getCachedValue(final String key, final Class<T> type) {
        if (!isCacheValid())
            return null;
        Object v = this.cache.get(key);
        return (v != null && type.isInstance(v)) ? (T) v : null;
    }

    // ----------------------------------------------------------------------
    // Value store interface (ContextCategory base functionality)
    // ----------------------------------------------------------------------

    /** Generic setter for a context value. */
    public <T> void setValue(final String key, final T value) {
        this.values.put(key, value);
    }

    /** Generic getter for a context value. */
    @SuppressWarnings("unchecked")
    public <T> T getValue(final String key, final Class<T> type) {
        Object v = this.values.get(key);
        return (v != null && type.isInstance(v)) ? (T) v : null;
    }

    /** Returns all context values currently stored. */
    public Map<String, Object> getAllValues() {
        return Map.copyOf(this.values);
    }

    public String getName() {
        return this.name;
    }

    public AbstractMirovaVehicle getVehicle() {
        return this.vehicle;
    }

    @Override
    public String toString() {
        return this.name + "Context[" + this.values + "]";
    }
}
