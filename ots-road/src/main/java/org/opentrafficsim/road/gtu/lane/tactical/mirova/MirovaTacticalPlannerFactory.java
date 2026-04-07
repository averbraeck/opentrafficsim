package org.opentrafficsim.road.gtu.lane.tactical.mirova;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionFactory;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlannerFactory;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModelFactory;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.MirovaParameters;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks.CongestionChunk;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks.DiscretionaryLaneChangeChunk;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks.MandatoryLaneChangeChunk;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks.MergeCooperationChunk;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.exclusive.GapSearchPattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.exclusive.SimpleLaneChangePattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.parallel.AnticipatingUpstreamMergingSpeedPattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.parallel.MergeCooperationPattern;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPatterns.parallel.PreventUndercuttingPattern;
import org.opentrafficsim.road.gtu.lane.tactical.util.ConflictUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.TrafficLightUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsUtil;

/**
 * Factory that creates instances of {@link MirovaTacticalPlanner}.
 * <p>
 * This factory initializes the cognitive architecture of the MiRoVA framework for a GTU.
 * It sets up the foundational layers by registering the declarative knowledge (Layer 2)
 * via {@link org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks.KnowledgeChunk}s
 * and the procedural knowledge (Layer 4) via {@link org.opentrafficsim.road.gtu.lane.tactical.mirova.core.ManeuverPattern}s.
 * It also provides the default parameter sets required for the perception and tactical models.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class MirovaTacticalPlannerFactory extends AbstractLaneBasedTacticalPlannerFactory<MirovaTacticalPlanner> implements Serializable
{

    /**
     * Constructor allowing custom car-following model and perception factory.
     *
     * @param carFollowingModelFactory factory to generate the car-following model
     * @param perceptionFactory factory to generate the perception module
     */
    public MirovaTacticalPlannerFactory(
            final CarFollowingModelFactory<? extends CarFollowingModel> carFollowingModelFactory,
            final PerceptionFactory perceptionFactory)
    {
        super(carFollowingModelFactory, perceptionFactory);
    }

    /**
     * Creates a fully initialized {@link MirovaTacticalPlanner} for the given GTU.
     *
     * @param gtu the lane-based GTU to attach the tactical planner to
     * @return the generated MiRoVA tactical planner
     */
    @Override
    public MirovaTacticalPlanner create(final LaneBasedGtu gtu)
    {
        try {
            gtu.setParameters(getParameters());
            MirovaTacticalPlanner planner = new MirovaTacticalPlanner(nextCarFollowingModel(gtu), gtu, getPerceptionFactory().generatePerception(gtu));
            addKnowledgeChunks(planner);
            addManeuverPatterns(planner);
            return planner;
        } catch (Exception e) {
            throw new RuntimeException("Could not create MirovaTacticalPlanner.", e);
        }
    }

    /**
     * Retrieves the parameters required for the MiRoVA tactical planner.
     *
     * @return a set of default parameters
     * @throws ParameterException if a parameter cannot be initialized
     */
    @Override
    public Parameters getParameters() throws ParameterException
    {
        return getDefaultParameters();
    }

    /**
     * Builds and returns the default parameter set for the MiRoVA framework.
     *
     * @return a {@link Parameters} set containing all base settings
     * @throws ParameterException if setting a default parameter fails
     */
    public Parameters getDefaultParameters() throws ParameterException
    {
        ParameterSet parameters = new ParameterSet();
        parameters.setDefaultParameters(ConflictUtil.class);
        parameters.setDefaultParameters(TrafficLightUtil.class);
        parameters.setDefaultParameters(LmrsUtil.class);
        parameters.setDefaultParameters(LmrsParameters.class);

        parameters.setDefaultParameter(ParameterTypes.VCONG);
        parameters.setDefaultParameter(ParameterTypes.T0);
        parameters.setDefaultParameter(ParameterTypes.LCDUR);

        parameters.setDefaultParameter(ParameterTypes.A);
        parameters.setDefaultParameter(ParameterTypes.B);
        parameters.setDefaultParameter(ParameterTypes.BCRIT);
        parameters.setDefaultParameter(ParameterTypes.B0);
        parameters.setDefaultParameter(ParameterTypes.TMIN);
        parameters.setDefaultParameter(ParameterTypes.TMAX);
        parameters.setDefaultParameter(ParameterTypes.TAU);
        parameters.setDefaultParameter(ParameterTypes.LOOKAHEAD);
        parameters.setDefaultParameter(ParameterTypes.LOOKBACK);

        getCarFollowingParameters().setAllIn(parameters);
        getPerceptionFactory().getParameters().setAllIn(parameters);

        parameters.setDefaultParameters(MirovaParameters.class);

        // Overwrite default DT for MiRoVA specific simulation precision
        parameters.setParameter(ParameterTypes.DT, Duration.instantiateSI(0.2));

        return parameters;
    }

    /**
     * Registers the initial declarative knowledge components (Layer 2) to the tactical planner.
     *
     * @param planner the MiRoVA tactical planner instance
     * @throws ParameterException if required parameters are missing
     * @throws OperationalPlanException if planning capabilities are compromised
     */
    protected void addKnowledgeChunks(final MirovaTacticalPlanner planner) throws ParameterException, OperationalPlanException
    {
        planner.addKnowledgeChunk(new DiscretionaryLaneChangeChunk(planner));
        planner.addKnowledgeChunk(new MandatoryLaneChangeChunk(planner));
        planner.addKnowledgeChunk(new MergeCooperationChunk(planner));
        planner.addKnowledgeChunk(new CongestionChunk(planner));
    }

    /**
     * Registers the initial procedural maneuver patterns (Layer 4) to the tactical planner.
     *
     * @param planner the MiRoVA tactical planner instance
     * @throws ParameterException if required parameters are missing
     */
    protected void addManeuverPatterns(final MirovaTacticalPlanner planner) throws ParameterException
    {
        // Exclusive maneuvers (one at a time)
        planner.addExclusiveManeuverPattern(new GapSearchPattern(planner));
        planner.addExclusiveManeuverPattern(new SimpleLaneChangePattern(planner));

        // Parallel maneuvers (can run simultaneously alongside standard car-following)
        planner.addParallelManeuverPattern(new MergeCooperationPattern(planner));
        planner.addParallelManeuverPattern(new PreventUndercuttingPattern(planner));
        planner.addParallelManeuverPattern(new AnticipatingUpstreamMergingSpeedPattern(planner));
    }

}