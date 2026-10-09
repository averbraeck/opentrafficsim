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
                final LmrsData lmrsData, final Desire ownDesire) throws ParameterException, OperationalPlanException
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
                final LmrsData lmrsData, final Desire ownDesire) throws ParameterException, OperationalPlanException
        {
            if (!context.getPerception().getLaneStructure().exists(lat.isRight() ? RelativeLane.RIGHT : RelativeLane.LEFT))
            {
                return Acceleration.POS_MAXVALUE;
            }
            double b = -context.getParameters().getParameter(ParameterTypes.B).si;
            double a = Double.MAX_VALUE;
            double dCoop = context.getParameters().getParameter(LmrsParameters.DCOOP);
            RelativeLane relativeLane = new RelativeLane(lat, 1);
            for (PerceivedGtu leader : context.getPerception().getPerceptionCategory(NeighborsPerception.class)
                    .getLeaders(relativeLane))
            {
                double desire = getLaneChangeDesire(leader, lat.flip(), dCoop);
                if (desire >= dCoop)
                {
                    if (lmrsData != null)
                    {
                        lmrsData.setSynchronizationState(Synchronizable.State.COOPERATING);
                    }
                    Acceleration aSingle =
                            LmrsUtil.relaxedAcceleration(context, leader.getDistance(), leader.getSpeed(), desire);
                    a = a < aSingle.si ? a : aSingle.si;
                }
            }
            return Acceleration.ofSI(a > b ? a : b);
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
        private static double getLaneChangeDesire(final PerceivedGtu leader, final LateralDirectionality lat,
                final double dCoop)
        {
            if (LateralDirectionality.NONE.equals(lat))
            {
                return 0.0;
            }
            double d = leader.getBehavior().getLaneChangeDesire(lat);
            if (d < dCoop && leader.getSignals().isIndicatorOn(lat))
            {
                return 1.0;
            }
            return d;
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
                final LmrsData lmrsData, final Desire ownDesire) throws ParameterException, OperationalPlanException
        {
            if (context.getSpeed().si < context.getParameters().getParameter(Synchronization.CREEP_SPEED).si)
            {
                return Acceleration.POS_MAXVALUE;
            }
            return PASSIVE.cooperate(context, lat, lmrsData, ownDesire);
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
     * @param lmrsData lmrs data to store COOPERATION synchronization state in, may be {@code null}
     * @param ownDesire own lane change desire
     * @return acceleration for synchronization
     * @throws ParameterException if a parameter is not defined
     * @throws OperationalPlanException perception exception
     */
    Acceleration cooperate(TacticalContextEgo context, LateralDirectionality lat, LmrsData lmrsData, Desire ownDesire)
            throws ParameterException, OperationalPlanException;
}
