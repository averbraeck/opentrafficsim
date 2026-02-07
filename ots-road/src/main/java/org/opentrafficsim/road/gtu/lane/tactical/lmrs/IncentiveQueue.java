package org.opentrafficsim.road.gtu.lane.tactical.lmrs;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.Stateless;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.IntersectionPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedConflict;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtu;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedTrafficLight;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.VoluntaryIncentive;
import org.opentrafficsim.road.network.lane.conflict.Conflict;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Incentive to join the shortest queue near intersection.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class IncentiveQueue implements VoluntaryIncentive, Stateless<IncentiveQueue>
{

    /** Singleton instance. */
    public static final IncentiveQueue SINGLETON = new IncentiveQueue();

    @Override
    public IncentiveQueue get()
    {
        return SINGLETON;
    }

    /**
     * Constructor.
     */
    private IncentiveQueue()
    {
        //
    }

    @Override
    public Desire determineDesire(final Parameters parameters, final LanePerception perception,
            final CarFollowingModel carFollowingModel, final Desire mandatoryDesire, final Desire voluntaryDesire)
            throws ParameterException, OperationalPlanException
    {
        if (!perception.contains(IntersectionPerception.class))
        {
            return Desire.ZERO;
        }
        EgoPerception<?, ?> ego = perception.getPerceptionCategory(EgoPerception.class);
        double aCur = Try.assign(() -> perception.getGtu().getCarFollowingAcceleration().si, OperationalPlanException.class,
                "Could not obtain the car-following acceleration.");
        if (aCur <= 0.0 && ego.getSpeed().eq0())
        {
            return Desire.ZERO;
        }
        IntersectionPerception inter = perception.getPerceptionCategoryOrNull(IntersectionPerception.class);
        PerceptionCollectable<PerceivedConflict, Conflict> conflicts = inter.getConflicts(RelativeLane.CURRENT);
        PerceptionCollectable<PerceivedTrafficLight, TrafficLight> lights = inter.getTrafficLights(RelativeLane.CURRENT);
        // TODO: a ramp-metering traffic light triggers this incentive with possible cooperation from the main line
        if (conflicts.isEmpty() && lights.isEmpty())
        {
            return Desire.ZERO;
        }
        Acceleration a = parameters.getParameter(ParameterTypes.A);
        NeighborsPerception neigbors = perception.getPerceptionCategory(NeighborsPerception.class);
        InfrastructurePerception infra = perception.getPerceptionCategory(InfrastructurePerception.class);

        SpeedLimitInfo sli = infra.getSpeedLimitProspect(RelativeLane.CURRENT).getSpeedLimitInfo(Length.ZERO);

        double dLeft = 0.0;
        if (infra.getCrossSection().contains(RelativeLane.LEFT))
        {
            PerceptionCollectable<PerceivedGtu, LaneBasedGtu> leaders = neigbors.getLeaders(RelativeLane.LEFT);
            if (!leaders.isEmpty())
            {
                Acceleration acc = CarFollowingUtil.followSingleLeader(carFollowingModel, parameters, ego.getSpeed(), sli,
                        leaders.first());
                dLeft = (acc.si - aCur) / a.si;
            }
        }
        double dRight = 0.0;
        if (infra.getCrossSection().contains(RelativeLane.RIGHT))
        {
            PerceptionCollectable<PerceivedGtu, LaneBasedGtu> leaders = neigbors.getLeaders(RelativeLane.RIGHT);
            if (!leaders.isEmpty())
            {
                Acceleration acc = CarFollowingUtil.followSingleLeader(carFollowingModel, parameters, ego.getSpeed(), sli,
                        leaders.first());
                dRight = (acc.si - aCur) / a.si;
            }
        }
        return new Desire(dLeft, dRight);
    }

    @Override
    public String toString()
    {
        return "IncentiveQueue";
    }

}
