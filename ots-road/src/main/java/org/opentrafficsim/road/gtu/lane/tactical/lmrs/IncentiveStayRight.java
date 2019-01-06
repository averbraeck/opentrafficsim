package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.road.gtu.lane.perception.InfrastructureLaneChangeInfo;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LaneStructureRecord;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.LmrsParameters;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.VoluntaryIncentive;

/**
 * Incentive for trucks to remain on the two right-hand lanes, unless the route requires otherwise.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 4 mrt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class IncentiveStayRight implements VoluntaryIncentive
{

    /** {@inheritDoc} */
    @Override
    public Desire determineDesire(final Parameters parameters, final LanePerception perception,
            final CarFollowingModel carFollowingModel, final Desire mandatoryDesire, final Desire voluntaryDesire)
            throws ParameterException, OperationalPlanException
    {
        InfrastructurePerception infra = perception.getPerceptionCategory(InfrastructurePerception.class);
        LaneStructureRecord root = perception.getLaneStructure().getRootRecord();
        LaneStructureRecord record = root;
        RelativeLane lane = RelativeLane.CURRENT;
        Route route = Try.assign(() -> perception.getGtu().getStrategicalPlanner().getRoute(), "");
        GTUType gtuType = Try.assign(() -> perception.getGtu().getGTUType(), "");
        Speed speed = perception.getPerceptionCategory(EgoPerception.class).getSpeed();
        // move all the way left
        while (record.physicalLeft())
        {
            lane = lane.getLeft();
            record = record.getLeft();
        }
        // move right until we find 'the right-hand lane',
        double curUrgency = urgency(infra.getInfrastructureLaneChangeInfo(lane), parameters, speed);
        while (true)
        {
            try
            {
                if (!record.physicalRight() || !record.getRight().allowsRoute(route, gtuType))
                {
                    // next lane considered not there, we've found 'the right-hand lane'
                    break;
                }
            }
            catch (NetworkException exception)
            {
                throw new RuntimeException(exception);
            }
            double rightUrgency = urgency(infra.getInfrastructureLaneChangeInfo(lane.getRight()), parameters, speed);
            if (rightUrgency > curUrgency)
            {
                // next lane is worse for the route, current lane is allowable for the route
                break;
            }
            lane = lane.getRight();
            record = record.getRight();
            curUrgency = rightUrgency;
        }
        if (lane.getLateralDirectionality().isRight() && lane.getNumLanes() > 1)
        {
            // must change right
            return new Desire(root.legalLeft() ? -1.0 : 0.0, parameters.getParameter(LmrsParameters.DSYNC));
        }
        if (lane.isRight())
        {
            // must not change left
            return new Desire(root.legalLeft() ? -1.0 : 0.0, 0.0);
        }
        return new Desire(0.0, 0.0);
    }

    /**
     * Returns the urgency to leave a lane.
     * @param laneChangeInfo SortedSet&lt;InfrastructureLaneChangeInfo&gt;; lane change info on the lane
     * @param parameters Parameters; parameters
     * @param speed Speed; current speed
     * @return double; urgency to leave the lane
     * @throws ParameterException if parameter is not given
     */
    private double urgency(final SortedSet<InfrastructureLaneChangeInfo> laneChangeInfo, final Parameters parameters,
            final Speed speed) throws ParameterException
    {
        double urgency = 0.0;
        for (InfrastructureLaneChangeInfo info : laneChangeInfo)
        {
            double nextUrgency = IncentiveRoute.getDesireToLeave(parameters, info.getRemainingDistance(),
                    info.getRequiredNumberOfLaneChanges(), speed);
            urgency = urgency > nextUrgency ? urgency : nextUrgency;
        }
        return urgency;
    }

}
