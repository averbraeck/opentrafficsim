package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;

/**
 * Marker interface for all context categories that can update their internal state
 * based on the current vehicle perception.
 */
public interface UpdatableContext {
    /**
     * Updates all relevant values in this context category using the current vehicle perception.
     * @param vehicle the ego vehicle providing perception and parameters
     */
    void updateFromPerception(MirovaTacticalPlanner vehicle);
}
