package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context;

import java.util.HashMap;
import java.util.Map;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;

/**
 * Central manager that aggregates and updates all contextual information
 * for a single vehicle. Provides modular access to multiple context categories.
 * <p>
 * This class is the core orchestrator of <b>Layer 1 (Perception & Context)</b> in the
 * MiRoVA architecture. It acts as the central hub for the vehicle's "world knowledge"
 * by managing the lifecycle of all {@link ContextCategory} instances. It handles the
 * tick-based synchronization and cache invalidation to ensure data consistency during
 * tactical reasoning.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class VehicleContextManager {

    /** All registered context categories, keyed by name. */
    private final Map<String, ContextCategory> categories = new HashMap<>();

    /** Reference to the associated ego vehicle. */
    private final MirovaTacticalPlanner vehicle;

    /** The current simulation tick counter for cache validation. */
    private long currentTick = 0;

    // ----------------------------------------------------------------------
    // Construction
    // ----------------------------------------------------------------------

    /**
     * Initializes the context manager and registers the default MiRoVA context categories.
     *
     * @param vehicle the ego vehicle (tactical planner) executing this manager
     */
    public VehicleContextManager(final MirovaTacticalPlanner vehicle) {
        this.vehicle = vehicle;

        // Register default context categories
        registerCategory(new EgoContext(vehicle));
        registerCategory(new MacroTrafficContext(vehicle));
        registerCategory(new InfrastructureContext(vehicle));
        registerCategory(new NeighborsContext(vehicle));
    }

    // ----------------------------------------------------------------------
    // Category management
    // ----------------------------------------------------------------------

    /**
     * Registers a new context category to the manager.
     *
     * @param category the context category to register
     */
    public void registerCategory(final ContextCategory category) {
        this.categories.put(category.getName(), category);
    }

    /**
     * Retrieves a context category by its name and expected class type.
     *
     * @param <T>   the expected type of the context category
     * @param name  the unique name of the category (e.g., "Neighbors")
     * @param clazz the class representing the expected type
     * @return the requested context category, or {@code null} if not found or type mismatch
     */
    @SuppressWarnings("unchecked")
    public <T extends ContextCategory> T getCategory(final String name, final Class<T> clazz) {
        ContextCategory cat = this.categories.get(name);
        return clazz.isInstance(cat) ? clazz.cast(cat) : null;
    }

    /**
     * Returns an unmodifiable map of all registered context categories.
     *
     * @return all registered categories keyed by their names
     */
    public Map<String, ContextCategory> getAllCategories() {
        return Map.copyOf(this.categories);
    }

    /**
     * Advances the internal simulation tick counter and invalidates all category caches.
     * <p>
     * This method must be called exactly once at the beginning of each simulation
     * step in "The Loop" to ensure that lazy-evaluated perception data is refreshed.
     * </p>
     */
    public void advanceTick() {
        this.currentTick++;
        for (ContextCategory cat : this.categories.values()) {
            cat.invalidateCache();
        }
    }

    /**
     * Returns the current internal simulation tick counter.
     *
     * @return the current tick number
     */
    public long getCurrentTick() {
        return this.currentTick;
    }

    // ----------------------------------------------------------------------
    // Update logic
    // ----------------------------------------------------------------------

    /**
     * Triggers the update process for all registered categories that implement
     * the {@link UpdatableContext} interface.
     * <p>
     * Called once per simulation tick to refresh all context values from perception.
     * </p>
     */
    public void updateFromPerception() {
        for (ContextCategory category : this.categories.values()) {
            if (category instanceof UpdatableContext) {
                try {
                    ((UpdatableContext) category).updateFromPerception(this.vehicle);
                } catch (Exception e) {
                    System.err.println("[ContextManager] Failed to update " + category.getName());
                    e.printStackTrace();
                }
            }
        }
    }
}