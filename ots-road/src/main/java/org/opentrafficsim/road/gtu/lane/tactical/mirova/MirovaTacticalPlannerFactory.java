package org.opentrafficsim.road.gtu.lane.tactical.mirova;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.Duration;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterSet;
import org.opentrafficsim.base.parameters.ParameterType;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
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
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks.SocialInteractionsChunk;
import org.opentrafficsim.road.gtu.lane.tactical.util.ConflictUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.TrafficLightUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsUtil;

/**
 * Factory that creates instances of {@link MirovaTacticalPlannerOLD}.
 * <p>
 * This version reuses the standard {@link LmrsPerceptionFactory}
 * so no custom perception is required initially.
 * </p>
 */
public class MirovaTacticalPlannerFactory  extends AbstractLaneBasedTacticalPlannerFactory<MirovaTacticalPlanner> implements Serializable
{
    /** Car-following model to use (e.g. IDM+). */
//    private final CarFollowingModel carFollowingModel;

    /** Perception factory (default: LMRS perception). */
//    private final PerceptionFactory perceptionFactory;


    /**
     * Constructor allowing custom model or perception.
     * @param carFollowingModel Car-following model to use
     * @param perceptionFactory Perception factory to use
     */
    public MirovaTacticalPlannerFactory(
            final CarFollowingModelFactory<? extends CarFollowingModel> carFollowingModelFactory,
            final PerceptionFactory perceptionFactory)
    {

        super(carFollowingModelFactory, perceptionFactory);

    }
    @Override
    public MirovaTacticalPlanner create(final LaneBasedGtu gtu)
    {
        try
        {
            gtu.setParameters(getParameters());
            MirovaTacticalPlanner planner = new MirovaTacticalPlanner(nextCarFollowingModel(gtu), gtu, getPerceptionFactory().generatePerception(gtu));
            planner.addKnowledgeChunk(new DiscretionaryLaneChangeChunk(planner));
            planner.addKnowledgeChunk(new MandatoryLaneChangeChunk(planner));
            planner.addKnowledgeChunk(new SocialInteractionsChunk(planner));
//            planner.addKnowledgeChunk(new MergeCooperationChunk(planner));
//            planner.addKnowledgeChunk(new CongestionChunk(planner));
            return planner;
        }
        catch (Exception e)
        {
            throw new RuntimeException("Could not create MirovaTacticalPlanner.", e);
        }
    }

    @Override
    public Parameters getParameters() throws ParameterException
    {
        return getDefaultParameters();
    }

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
        parameters.setDefaultParameter(ParameterTypes.DT);
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
        parameters.setParameter(ParameterTypes.DT, Duration.instantiateSI(0.2));


        return parameters;
    }


}
