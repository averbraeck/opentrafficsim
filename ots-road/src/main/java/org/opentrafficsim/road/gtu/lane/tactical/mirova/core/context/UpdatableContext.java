package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context;

import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;

/**
 * Marker and functional interface for all context categories that can update their
 * internal state based on the current vehicle perception.
 * <p>
 * This interface is a crucial part of <b>Layer 1 (Perception & Context)</b> in the
 * MiRoVA architecture. It ensures that every context category provides a standardized
 * hook to refresh its cached data (or invalidate its lazy-evaluation caches) once
 * per simulation tick.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public interface UpdatableContext {

    /**
     * Updates or invalidates all relevant cached values in this context category
     * using the current vehicle perception. This is typically called once per
     * simulation tick by the {@link VehicleContextManager}.
     *
     * @param vehicle the ego vehicle (tactical planner) providing perception and parameters
     */
    void updateFromPerception(MirovaTacticalPlanner vehicle);
}