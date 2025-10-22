package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.VehicleTypes;

import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunk.CongestionChunk.CongestionChunk;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunk.DiscretionaryLaneChangeChunk.DiscretionaryLaneChangeChunk;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunk.MandatoryLaneChangeChunk.MandatoryLaneChangeChunk;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunk.MergeCooperationChunk.MergeCooperationChunk;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunk.SocialInteractionsChunk.SocialInteractionsChunk;

/**
 * HumanDriverVehicle.java.
 *  */
public class HumanDriverVehicle extends AbstractMirovaVehicle {

    /**
     * @param model
     * @param gtu
     * @param perception
     * @param planner
     * @throws OperationalPlanException
     */
    public HumanDriverVehicle(final CarFollowingModel model, final LaneBasedGtu gtu,
                              final LanePerception perception, final MirovaTacticalPlanner planner)
            throws OperationalPlanException
    {
        super(model, gtu, perception, planner);

        // knowledge configuration
        addKnowledgeChunk(new DiscretionaryLaneChangeChunk(this));
        addKnowledgeChunk(new MandatoryLaneChangeChunk(this));
        addKnowledgeChunk(new SocialInteractionsChunk(this));
        addKnowledgeChunk(new MergeCooperationChunk(this));
        addKnowledgeChunk(new CongestionChunk(this));
    }

}
