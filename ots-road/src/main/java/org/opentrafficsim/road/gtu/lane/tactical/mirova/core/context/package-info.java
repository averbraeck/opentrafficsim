/**
 * Layer 1 (Perception & Context) of the MiRoVA tactical planner framework.
 * <p>
 * This package provides the components to manage the "world knowledge" of the autonomous agent.
 * It structures raw sensor data (from OpenTrafficSim's perception module) into semantic,
 * lazily-evaluated context categories. These categories are subsequently used by the higher
 * cognitive layers (Knowledge Chunks and Maneuver Patterns) to formulate desires and make
 * tactical decisions.
 * </p>
 * <p>
 * Core components include:
 * <ul>
 * <li>{@link org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.VehicleContextManager}: The central hub synchronizing all context updates per simulation tick.</li>
 * <li>{@link org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.EgoContext}: Ego-vehicle states (e.g., speed, acceleration, deceleration thresholds).</li>
 * <li>{@link org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.NeighborsContext}: Dynamic interactions, headways, and relative speeds of adjacent vehicles.</li>
 * <li>{@link org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.InfrastructureContext}: Static and dynamic road infrastructure (e.g., speed limits, lane ends).</li>
 * <li>{@link org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.MacroTrafficContext}: Macroscopic traffic flow states (e.g., density, average speed).</li>
 * </ul>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context;