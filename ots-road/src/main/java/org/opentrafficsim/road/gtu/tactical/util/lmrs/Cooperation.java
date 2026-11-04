package org.opentrafficsim.road.gtu.tactical.util.lmrs;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.opentrafficsim.base.NamedConstants;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.perception.RelativeLane;
import org.opentrafficsim.road.gtu.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.perception.object.PerceivedGtu;
import org.opentrafficsim.road.gtu.tactical.Synchronizable;
import org.opentrafficsim.road.gtu.tactical.TacticalContextEgo;

/**
 * Different forms of cooperation.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 * @author Wouter Schakel
 */
public interface Cooperation extends NamedConstants
{

    /**
     * No synchronization.
     */
    Cooperation NONE = new Cooperation()
    {
        @Override
        public Acceleration cooperate(final TacticalContextEgo context, final LateralDirectionality lat,
                final LmrsData lmrsData) throws ParameterException, OperationalPlanException
        {
            return Acceleration.POS_MAXVALUE;
        }

        @Override
        public String name()
        {
            return "NONE";
        }
    };

    /**
     * Cooperation to any leader with d &ge; dCoop.
     */
    Cooperation PASSIVE = new Cooperation()
    {
        @Override
        public Acceleration cooperate(final TacticalContextEgo context, final LateralDirectionality lat,
                final LmrsData lmrsData) throws ParameterException, OperationalPlanException
        {
            if (!context.getPerception().getLaneStructure().exists(lat.isRight() ? RelativeLane.RIGHT : RelativeLane.LEFT))
            {
                return Acceleration.POS_MAXVALUE;
            }
            Acceleration b0 = context.getParameters().getParameter(ParameterTypes.B0);
            Acceleration b = context.getParameters().getParameter(ParameterTypes.B);
            double a = Double.MAX_VALUE;
            double dCoop = context.getParameters().getParameter(LmrsParameters.DCOOP);
            RelativeLane relativeLane = RelativeLane.of(lat, 1);
            Relaxation relaxation = context.getCarFollowingModel().getRelaxation();
            String coop = null;
            for (PerceivedGtu leader : context.getPerception().getPerceptionCategory(NeighborsPerception.TYPE)
                    .getLeaders(relativeLane))
            {
                SingleUrgency urgency = getLaneChangeUrgency(leader, lat.flip(), dCoop);
                if (urgency.desire() >= dCoop)
                {
                    double u = urgency.urgency();
                    Acceleration bUrg = u >= dCoop ? b : b0;
                    double k = relaxation.requiredRelaxationLevel(context.getParameters(), context.getSpeed(), leader, bUrg)
                            .orElse(urgency.desire());
                    if (k <= 1.0)
                    {
                        if (lmrsData != null)
                        {
                            coop = coop == null ? leader.getId() : null;
                            lmrsData.setSynchronizationState(Synchronizable.State.COOPERATING);
                        }
                        double aSingle = LmrsUtil.synchronizationAcceleration(context, leader.getDistance(), leader.getSpeed(),
                                Math.max(0.0, k)).si;
                        aSingle = Math.max(aSingle, -bUrg.si);
                        a = Math.min(a, aSingle);
                    }
                }
            }
            return Acceleration.ofSI(a);
        }

        /**
         * Returns the level of lane change desire as perceived for the given vehicle. There are a few rules that describe this:
         * <ul>
         * <li>If {@code lat} equals {@code NONE}, 0 is returned.</li>
         * <li>If the behavioral value {@code d} has {@code d < dCoop} but the indicator is on, 1 is returned. This assumes an
         * automated system that wants to perform a lane change.</li>
         * <li>Otherwise {@code d} is returned, assuming a human is in control with whatever level of lane change desire.</li>
         * </ul>
         * @param leader leader
         * @param lat lateral direction
         * @return the level of lane change desire as perceived for the given vehicle
         */
        private static SingleUrgency getLaneChangeUrgency(final PerceivedGtu leader, final LateralDirectionality lat,
                final double dCoop)
        {
            if (LateralDirectionality.NONE.equals(lat))
            {
                return SingleUrgency.ZERO;
            }
            double d = leader.getBehavior().getLaneChangeDesire(lat);
            double u = leader.getBehavior().getLaneChangeUrgency(lat);
            // The follow triggers cooperation when the indicator is on for whatever
            // if (d < dCoop && leader.getSignals().isIndicatorOn(lat))
            // {
            // return 1.0;
            // }
            return new SingleUrgency(d, u);
        }

        record SingleUrgency(double desire, double urgency)
        {
            /** Zero instance. */
            private static final SingleUrgency ZERO = new SingleUrgency(0.0, 0.0);
        }

        @Override
        public String name()
        {
            return "PASSIVE";
        }
    };

    /**
     * Cooperation to any leader with d &ge; dCoop.
     * <p>
     * Cooperation is disabled for v &lt; {@link Synchronization#CREEP_SPEED}.
     */
    Cooperation PASSIVE_MOVING = new Cooperation()
    {
        @Override
        public Acceleration cooperate(final TacticalContextEgo context, final LateralDirectionality lat,
                final LmrsData lmrsData) throws ParameterException, OperationalPlanException
        {
            if (context.getSpeed().si < context.getParameters().getParameter(Synchronization.CREEP_SPEED).si)
            {
                return Acceleration.POS_MAXVALUE;
            }
            return PASSIVE.cooperate(context, lat, lmrsData);
        }

        @Override
        public String name()
        {
            return "PASSIVE_MOVING";
        }
    };

    /**
     * Determine acceleration for cooperation.
     * @param context tactical information such as parameters and car-following model
     * @param lat lateral direction for cooperation
     * @param lmrsData LMRS data to store COOPERATION synchronization state in, may be {@code null}
     * @return acceleration for synchronization
     * @throws ParameterException if a parameter is not defined
     * @throws OperationalPlanException perception exception
     */
    Acceleration cooperate(TacticalContextEgo context, LateralDirectionality lat, LmrsData lmrsData)
            throws ParameterException, OperationalPlanException;
}
