/**
 * The MiRoVA (Migration of Road Vehicle Automation) tactical planner framework.
 * <p>
 * This package provides the main entry points and orchestrators for the cognitive
 * 4-layer tactical planner architecture in OpenTrafficSim. It contains:
 * <ul>
 * <li><b>Perception & Context:</b> Initialized via the {@link org.opentrafficsim.road.gtu.lane.tactical.mirova.DefaultMirovaPerceptionFactory}.</li>
 * <li><b>Tactical Planner:</b> The {@link org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner} orchestrating the central "Loop" (Context -> Cognition -> Decision -> Action).</li>
 * <li><b>Factory:</b> The {@link org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlannerFactory} which registers all knowledge chunks and maneuver patterns to the GTU.</li>
 * </ul>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
package org.opentrafficsim.road.gtu.lane.tactical.mirova;