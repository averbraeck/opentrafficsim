package org.opentrafficsim.road.gtu.lane.tactical.util.lmrs;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.TacticalContextEgo;

/**
 * Different forms of cooperation.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface Cooperation extends LmrsParameters
{

    /** Simple passive cooperation. */
    Cooperation PASSIVE = new Cooperation()
    {
        @Override
        public Acceleration cooperate(final TacticalContextEgo context, final LateralDirectionality lat, final Desire ownDesire)
                throws ParameterException, OperationalPlanException
        {
            if (!context.getPerception().getLaneStructure().exists(lat.isRight() ? RelativeLane.RIGHT : RelativeLane.LEFT))
            {
                return new Acceleration(Double.MAX_VALUE, AccelerationUnit.SI);
            }
            Acceleration b = context.getParameters().getParameter(ParameterTypes.B);
            Acceleration a = new Acceleration(Double.MAX_VALUE, AccelerationUnit.SI);
            double dCoop = context.getParameters().getParameter(DCOOP);
            RelativeLane relativeLane = new RelativeLane(lat, 1);
            for (PerceivedGtu leader : context.getPerception().getPerceptionCategory(NeighborsPerception.class)
                    .getLeaders(relativeLane))
            {
                double desire = lat.equals(LateralDirectionality.LEFT) ? leader.getBehavior().rightLaneChangeDesire()
                        : lat.equals(LateralDirectionality.RIGHT) ? leader.getBehavior().leftLaneChangeDesire() : 0.0;
                if (desire >= dCoop && (leader.getSpeed().gt0() || leader.getDistance().gt0()))
                {
                    Acceleration aSingle =
                            LmrsUtil.singleAcceleration(context, leader.getDistance(), leader.getSpeed(), desire);
                    a = Acceleration.min(a, aSingle);
                }
            }
            return Acceleration.max(a, b.neg());
        }

        @Override
        public String toString()
        {
            return "PASSIVE";
        }
    };

    /** Same as passive cooperation, except that cooperation is fully ignored if the potential lane changer brakes heavily. */
    Cooperation PASSIVE_MOVING = new Cooperation()
    {
        @Override
        public Acceleration cooperate(final TacticalContextEgo context, final LateralDirectionality lat, final Desire ownDesire)
                throws ParameterException, OperationalPlanException
        {
            if (!context.getPerception().getLaneStructure().exists(lat.isRight() ? RelativeLane.RIGHT : RelativeLane.LEFT))
            {
                return new Acceleration(Double.MAX_VALUE, AccelerationUnit.SI);
            }
            Acceleration bCrit = context.getParameters().getParameter(ParameterTypes.BCRIT);
            Acceleration a = new Acceleration(Double.MAX_VALUE, AccelerationUnit.SI);
            double dCoop = context.getParameters().getParameter(DCOOP);
            RelativeLane relativeLane = new RelativeLane(lat, 1);
            NeighborsPerception neighbours = context.getPerception().getPerceptionCategory(NeighborsPerception.class);
            PerceptionCollectable<PerceivedGtu, LaneBasedGtu> leaders = neighbours.getLeaders(RelativeLane.CURRENT);
            Speed thresholdSpeed = Speed.ofSI(6.86); // 295m / 43s
            boolean leaderInCongestion = leaders.isEmpty() ? false : leaders.first().getSpeed().lt(thresholdSpeed);
            for (PerceivedGtu leader : neighbours.getLeaders(relativeLane))
            {
                double desire = lat.equals(LateralDirectionality.LEFT) ? leader.getBehavior().rightLaneChangeDesire()
                        : lat.equals(LateralDirectionality.RIGHT) ? leader.getBehavior().leftLaneChangeDesire() : 0.0;
                // TODO: only cooperate if merger still quite fast or there's congestion downstream anyway (which we can better
                // estimate than only considering the direct leader
                if (desire >= dCoop && (leader.getSpeed().gt0() || leader.getDistance().gt0())
                        && (leader.getSpeed().ge(thresholdSpeed) || leaderInCongestion))
                {
                    Acceleration aSingle =
                            LmrsUtil.singleAcceleration(context, leader.getDistance(), leader.getSpeed(), desire);
                    a = Acceleration.min(a, aSingle);
                }
            }
            return Acceleration.max(a, bCrit.neg());
        }

        @Override
        public String toString()
        {
            return "PASSIVE_MOVING";
        }
    };

    /** Cooperation similar to the default, with nuanced differences of when to ignore. */
    Cooperation ACTIVE = new Cooperation()
    {
        @Override
        public Acceleration cooperate(final TacticalContextEgo context, final LateralDirectionality lat, final Desire ownDesire)
                throws ParameterException, OperationalPlanException
        {
            if (!context.getPerception().getLaneStructure().exists(lat.isRight() ? RelativeLane.RIGHT : RelativeLane.LEFT))
            {
                return new Acceleration(Double.MAX_VALUE, AccelerationUnit.SI);
            }
            Acceleration a = new Acceleration(Double.MAX_VALUE, AccelerationUnit.SI);
            double dCoop = context.getParameters().getParameter(DCOOP);
            RelativeLane relativeLane = new RelativeLane(lat, 1);
            for (PerceivedGtu leader : context.getPerception().getPerceptionCategory(NeighborsPerception.class)
                    .getLeaders(relativeLane))
            {
                double desire = leader.getManeuver().isChangingLane(lat.flip()) ? 1.0
                        : (lat.equals(LateralDirectionality.LEFT) ? leader.getBehavior().rightLaneChangeDesire()
                                : lat.equals(LateralDirectionality.RIGHT) ? leader.getBehavior().leftLaneChangeDesire() : 0.0);
                if (desire >= dCoop && leader.getDistance().gt0()
                        && leader.getAcceleration().gt(context.getParameters().getParameter(ParameterTypes.BCRIT).neg()))
                {
                    Acceleration aSingle =
                            LmrsUtil.singleAcceleration(context, leader.getDistance(), leader.getSpeed(), desire);
                    a = Acceleration.min(a, Synchronization.gentleUrgency(aSingle, desire, context.getParameters()));
                }
            }
            return a;
        }

        @Override
        public String toString()
        {
            return "ACTIVE";
        }
    };

    /**
     * Determine acceleration for cooperation.
     * @param context tactical information such as parameters and car-following model
     * @param lat lateral direction for cooperation
     * @param ownDesire own lane change desire
     * @return acceleration for synchronization
     * @throws ParameterException if a parameter is not defined
     * @throws OperationalPlanException perception exception
     */
    Acceleration cooperate(TacticalContextEgo context, LateralDirectionality lat, Desire ownDesire)
            throws ParameterException, OperationalPlanException;
}
