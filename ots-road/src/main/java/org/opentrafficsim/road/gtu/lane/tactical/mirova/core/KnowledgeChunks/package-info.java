/**
 * Layer 2 (Cognition / Motivation) of the MiRoVA tactical planner framework.
 * <p>
 * This package contains declarative knowledge components, known as
 * {@link org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks.KnowledgeChunk}s.
 * These chunks evaluate the vehicle's current context (provided by Layer 1) to formulate
 * specific driving motivations, represented as {@link org.opentrafficsim.road.gtu.lane.tactical.mirova.core.Desire} vectors.
 * </p>
 * <p>
 * Crucially, according to the 4-layer MiRoVA architecture, components in this package
 * <b>strictly compute desires</b> and do not directly execute or trigger physical maneuvers.
 * Their aggregated output is passed to Layer 3 (Decision / Strategy) for maneuver arbitration.
 * </p>
 * <p>
 * Core chunks include:
 * <ul>
 * <li>{@link org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks.MandatoryLaneChangeChunk}: Desires based on route following and lane drops.</li>
 * <li>{@link org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks.DiscretionaryLaneChangeChunk}: Desires for speed gain and keep-right rules.</li>
 * <li>{@link org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks.SocialInteractionsChunk}: Desires shaped by social pressure from other drivers.</li>
 * <li>{@link org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks.CongestionChunk}: Inhibitory desires to prevent ping-pong lane changes in traffic jams.</li>
 * <li>{@link org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks.MergeCooperationChunk}: Recognition of cooperative merging scenarios.</li>
 * </ul>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks;