package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context;

import java.util.HashMap;
import java.util.Map;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;

/**
 * Central manager that aggregates and updates all contextual information
 * for a single vehicle. Provides modular access to multiple context categories.
 */
public class VehicleContextManager {

    /** All registered context categories, keyed by name. */
    private final Map<String, ContextCategory> categories = new HashMap<>();

    /** Reference to the associated ego vehicle. */
    private final MirovaTacticalPlanner vehicle;

    private long currentTick = 0;


    // ----------------------------------------------------------------------
    // Construction
    // ----------------------------------------------------------------------

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

    /** Registers a new context category. */
    public void registerCategory(final ContextCategory category) {
        this.categories.put(category.getName(), category);
    }

    /** Retrieves a category by its name and type. */
    @SuppressWarnings("unchecked")
    public <T extends ContextCategory> T getCategory(final String name, final Class<T> clazz) {
        ContextCategory cat = this.categories.get(name);
        return clazz.isInstance(cat) ? clazz.cast(cat) : null;
    }

    /** Returns all registered categories. */
    public Map<String, ContextCategory> getAllCategories() {
        return Map.copyOf(this.categories);
    }

    /** Called at the start of each simulation tick. */
    public void advanceTick() {
        this.currentTick++;
        for (ContextCategory cat : this.categories.values()) {
            cat.invalidateCache();
        }
    }

    public long getCurrentTick() {
        return this.currentTick;
    }

    // ----------------------------------------------------------------------
    // Update logic
    // ----------------------------------------------------------------------

    /**
     * Called once per simulation tick to refresh all context values from perception.
     */
    public void updateFromPerception() {
        for (ContextCategory category : this.categories.values()) {
            if (category instanceof UpdatableContext updatable) {
                try {
                    updatable.updateFromPerception(this.vehicle);
                } catch (Exception e) {
                    System.err.println("[ContextManager] Failed to update " + category.getName());
                    e.printStackTrace();
                }
            }
        }
    }
}
