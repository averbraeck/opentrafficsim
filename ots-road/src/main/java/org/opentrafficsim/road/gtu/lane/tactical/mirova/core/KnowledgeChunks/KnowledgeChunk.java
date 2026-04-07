package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks;

import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.perception.DirectEgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.perception.categories.AnticipationTrafficPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectDefaultSimplePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DirectInfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.DirectNeighborsPerception;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.Desire;

/**
 * Abstract base class representing a declarative KnowledgeChunk in the cognitive driving model.
 * <p>
 * Forms the core of <b>Layer 2 (Cognition / Motivation)</b> in the MiRoVA architecture.
 * A KnowledgeChunk corresponds to an ACT-R "chunk" of declarative knowledge.
 * It provides mechanisms to:
 * <ul>
 * <li>Evaluate whether this knowledge is currently applicable (context relevance).</li>
 * <li>Compute an activation level representing how "available" or "salient" this knowledge is.</li>
 * <li>Contribute to the overall driving desire (e.g., LMRS-style components: route following, overtaking, keeping right, cooperation).</li>
 * </ul>
 * Critically, in accordance with the 4-layer architecture, KnowledgeChunks strictly compute
 * <b>desires</b>, not actions. Procedural actions are handled separately by ManeuverPatterns in Layer 4.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public abstract class KnowledgeChunk
{
    /** Vehicle context (Tactical planner executing this chunk). */
    protected final MirovaTacticalPlanner vehicle;

    /** Last computed desire vector. */
    protected Desire desire;

    /** Direct infrastructure perception category. */
    private final DirectInfrastructurePerception infrastructurePerception;
    /** Anticipation traffic perception category. */
    private final AnticipationTrafficPerception trafficPerception;
    /** Direct ego perception category. */
    private final DirectEgoPerception egoPerception;
    /** Direct neighbors perception category. */
    private final DirectNeighborsPerception neighborsPerception;
    /** Direct default simple perception category. */
    private final DirectDefaultSimplePerception directDefaultSimplePerception;

    /** Vehicle parameters. */
    private final Parameters parameters;

    /**
     * Initializes the KnowledgeChunk and links it to the vehicle's perception categories.
     *
     * @param vehicle the vehicle (tactical planner) owning this knowledge chunk
     * @throws OperationalPlanException when perception categories cannot be accessed
     */
    public KnowledgeChunk(final MirovaTacticalPlanner vehicle) throws OperationalPlanException
    {
        this.vehicle = vehicle;
        this.desire = Desire.zero();

        this.infrastructurePerception =
                vehicle.getPerception().getPerceptionCategory(DirectInfrastructurePerception.class);
        this.trafficPerception =
                vehicle.getPerception().getPerceptionCategory(AnticipationTrafficPerception.class);
        this.egoPerception =
                vehicle.getPerception().getPerceptionCategory(DirectEgoPerception.class);
        this.neighborsPerception =
                vehicle.getPerception().getPerceptionCategory(DirectNeighborsPerception.class);
        this.directDefaultSimplePerception =
                vehicle.getPerception().getPerceptionCategory(DirectDefaultSimplePerception.class);

        this.parameters = vehicle.getGtu().getParameters();
    }

    // ----------------------------------------------------------------------
    // CORE COGNITIVE INTERFACE
    // ----------------------------------------------------------------------

    /**
     * Checks if this knowledge chunk is currently relevant given the driving context.
     * <p>
     * For example, knowledge about signalized intersections is not applicable on a motorway.
     * </p>
     *
     * @return {@code true} if this knowledge is relevant in the current context, {@code false} otherwise
     * @throws ParameterException if a parameter lookup fails during evaluation
     */
    public abstract boolean isApplicable() throws ParameterException;

    /**
     * Computes the contribution of this chunk to the current driving desires.
     * <p>
     * These represent LMRS-style components for route following, overtaking, keeping right, cooperation, etc.
     * </p>
     *
     * @return a {@link Desire} object representing directional preferences
     * @throws ParameterException if parameters cannot be read
     * @throws OperationalPlanException if generation of operational plan limits fails
     * @throws GtuException if the GTU state cannot be accessed
     * @throws NetworkException if the network state cannot be accessed
     */
    public abstract Desire computeDesire() throws ParameterException, OperationalPlanException, GtuException, NetworkException;

    // ----------------------------------------------------------------------
    // ACCESSORS
    // ----------------------------------------------------------------------

    /**
     * Returns the MiRoVA tactical planner governing this knowledge chunk.
     *
     * @return the tactical planner
     */
    public MirovaTacticalPlanner getMirovaTacticalPlanner() {
        return this.vehicle;
    }

    /**
     * Returns the infrastructure perception category.
     *
     * @return the direct infrastructure perception
     */
    public DirectInfrastructurePerception getInfrastructurePerception() {
        return this.infrastructurePerception;
    }

    /**
     * Returns the traffic perception category.
     *
     * @return the anticipation traffic perception
     */
    public AnticipationTrafficPerception getTrafficPerception() {
        return this.trafficPerception;
    }

    /**
     * Returns the ego perception category.
     *
     * @return the direct ego perception
     */
    public DirectEgoPerception getEgoPerception() {
        return this.egoPerception;
    }

    /**
     * Returns the neighbors perception category.
     *
     * @return the direct neighbors perception
     */
    public DirectNeighborsPerception getNeighborsPerception() {
        return this.neighborsPerception;
    }

    /**
     * Returns the default simple perception category.
     *
     * @return the direct default simple perception
     */
    public DirectDefaultSimplePerception getDirectDefaultSimplePerception() {
        return this.directDefaultSimplePerception;
    }

    /**
     * Returns the parameters associated with the ego vehicle.
     *
     * @return the parameter set
     */
    public Parameters getParameters() {
        return this.parameters;
    }

    /**
     * Returns the most recently computed desire of this chunk.
     *
     * @return the last computed desire
     */
    public Desire getDesire() {
        return this.desire;
    }

}