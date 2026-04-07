/**
 * Core components of the MiRoVA (Migration of Road Vehicle Automation) tactical planner.
 * <p>
 * This package contains the fundamental building blocks for the cognitive and procedural
 * layers of the MiRoVA architecture. It defines the core data structures, parameters,
 * and state machines used by the tactical planner, including:
 * <ul>
 * <li><b>Desire and Cognition:</b> The {@link org.opentrafficsim.road.gtu.lane.tactical.mirova.core.Desire} class and declarative knowledge chunks representing Layer 2.</li>
 * <li><b>Procedural Knowledge (FSM):</b> The {@link org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern} and {@link org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ActionState} classes representing the atomic units of Layer 4.</li>
 * <li><b>Parameters:</b> Domain-specific parameters defined in {@link org.opentrafficsim.road.gtu.lane.tactical.mirova.core.MirovaParameters}.</li>
 * </ul>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
package org.opentrafficsim.road.gtu.lane.tactical.mirova.core;