package org.opentrafficsim.road.gtu.tactical.lmrs;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djutils.immutablecollections.ImmutableLinkedHashMap;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.Stateless;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.LaneBasedGtu;
import org.opentrafficsim.road.gtu.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.perception.categories.IntersectionPerception;
import org.opentrafficsim.road.gtu.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.perception.object.PerceivedConflict;
import org.opentrafficsim.road.gtu.perception.object.PerceivedGtu;
import org.opentrafficsim.road.gtu.perception.object.PerceivedTrafficLight;
import org.opentrafficsim.road.gtu.tactical.TacticalContextEgo;
import org.opentrafficsim.road.gtu.tactical.util.CarFollowingUtil;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.Desire;
import org.opentrafficsim.road.gtu.tactical.util.lmrs.VoluntaryIncentive;
import org.opentrafficsim.road.network.conflict.Conflict;
import org.opentrafficsim.road.network.object.trafficlight.TrafficLight;

/**
 * Incentive to join the shortest queue near intersections.
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
    public Desire determineDesire(final TacticalContextEgo context, final Desire mandatoryDesire,
            final ImmutableLinkedHashMap<Class<? extends VoluntaryIncentive>, Desire> voluntaryDesire)
            throws ParameterException, OperationalPlanException
    {
        if (!context.getPerception().contains(IntersectionPerception.class))
        {
            return Desire.ZERO;
        }
        double aCur = context.getCarFollowingAcceleration().si;
        if (aCur <= 0.0 && context.getSpeed().eq0())
        {
            return Desire.ZERO;
        }
        IntersectionPerception inter = context.getPerception().getPerceptionCategory(IntersectionPerception.class);
        PerceptionCollectable<PerceivedConflict, Conflict> conflicts = inter.getConflicts(RelativeLane.CURRENT);
        PerceptionCollectable<PerceivedTrafficLight, TrafficLight> lights = inter.getTrafficLights(RelativeLane.CURRENT);
        // TODO: a ramp-metering traffic light triggers this incentive with possible cooperation from the main line
        // possible solution: make this a state-full class using Lane/LinkTypes to recognize intersections
        if (conflicts.isEmpty() && lights.isEmpty())
        {
            return Desire.ZERO;
        }
        Acceleration a = context.getParameters().getParameter(ParameterTypes.A);
        NeighborsPerception neigbors = context.getPerception().getPerceptionCategory(NeighborsPerception.class);
        InfrastructurePerception infra = context.getPerception().getPerceptionCategory(InfrastructurePerception.class);

        double dLeft = 0.0;
        if (infra.getCrossSection().contains(RelativeLane.LEFT))
        {
            PerceptionCollectable<PerceivedGtu, LaneBasedGtu> leaders = neigbors.getLeaders(RelativeLane.LEFT);
            if (!leaders.isEmpty())
            {
                Acceleration acc = CarFollowingUtil.followSingleLeader(context, leaders.first());
                dLeft = (acc.si - aCur) / a.si;
            }
        }
        double dRight = 0.0;
        if (infra.getCrossSection().contains(RelativeLane.RIGHT))
        {
            PerceptionCollectable<PerceivedGtu, LaneBasedGtu> leaders = neigbors.getLeaders(RelativeLane.RIGHT);
            if (!leaders.isEmpty())
            {
                Acceleration acc = CarFollowingUtil.followSingleLeader(context, leaders.first());
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
