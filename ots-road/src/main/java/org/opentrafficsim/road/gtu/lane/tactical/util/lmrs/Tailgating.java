package org.opentrafficsim.road.gtu.lane.tactical.util.lmrs;

import java.util.NoSuchElementException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeBoolean;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.constraint.ConstraintInterface;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtu;
import org.opentrafficsim.road.gtu.lane.tactical.TacticalContextEgo;
import org.opentrafficsim.road.network.lane.LanePosition;

/**
 * Interface for LMRS tailgating behavior.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface Tailgating
{

    /** Social pressure applied to the leader. */
    ParameterTypeDouble RHO = new ParameterTypeDouble("rho", "Social pressure", 0.0, ConstraintInterface.UNITINTERVAL);

    /** Parameter to deviate laterally for social pressure. */
    ParameterTypeBoolean DEV_RHO = new ParameterTypeBoolean("dev_split", "Deviate laterally for social pressure.", false);

    /** No tailgating. */
    Tailgating NONE = new Tailgating()
    {
        @Override
        public void tailgate(final TacticalContextEgo context)
        {
            //
        }

        @Override
        public String toString()
        {
            return "NONE";
        }
    };

    /** No tailgating, but social pressure exists. */
    Tailgating RHO_ONLY = new Tailgating()
    {
        @Override
        public void tailgate(final TacticalContextEgo context)
        {
            PerceptionCollectable<PerceivedGtu,
                    LaneBasedGtu> leaders = context.getPerception().getPerceptionCategoryOptional(NeighborsPerception.class)
                            .orElseThrow(() -> new NoSuchElementException("No neighbors perception category."))
                            .getLeaders(RelativeLane.CURRENT);
            try
            {
                if (leaders == null || leaders.isEmpty())
                {
                    context.getParameters().setClaimedParameter(RHO, 0.0, this);
                    Tailgating.deviate(context, 0.0);
                    return;
                }
                Length x0 = context.getParameters().getParameter(ParameterTypes.LOOKAHEAD);
                Speed vGain = context.getParameters().getParameter(LmrsParameters.VGAIN);
                PerceivedGtu leader = leaders.first();
                Speed desiredSpeed = context.getDesiredSpeed();
                double rho = Tailgating.socialPressure(desiredSpeed, leader.getSpeed(), vGain, leader.getDistance(), x0);
                context.getParameters().setClaimedParameter(RHO, rho, this);
                Tailgating.deviate(context, rho);
            }
            catch (ParameterException exception)
            {
                throw new OtsRuntimeException("Could not obtain or set parameter value.", exception);
            }
        }

        @Override
        public String toString()
        {
            return "RHO_ONLY";
        }
    };

    /** Tailgating based on speed pressure. */
    Tailgating PRESSURE = new Tailgating()
    {
        @Override
        public void tailgate(final TacticalContextEgo context)
        {
            PerceptionCollectable<PerceivedGtu,
                    LaneBasedGtu> leaders = context.getPerception().getPerceptionCategoryOptional(NeighborsPerception.class)
                            .orElseThrow(() -> new NoSuchElementException("No neighbors perception category."))
                            .getLeaders(RelativeLane.CURRENT);
            try
            {
                if (leaders == null || leaders.isEmpty())
                {
                    context.getParameters().setClaimedParameter(RHO, 0.0, this);
                    Tailgating.deviate(context, 0.0);
                    return;
                }
                Duration t = context.getParameters().getParameter(ParameterTypes.T);
                Duration tMin = context.getParameters().getParameter(ParameterTypes.TMIN);
                Duration tMax = context.getParameters().getParameter(ParameterTypes.TMAX);
                Length x0 = context.getParameters().getParameter(ParameterTypes.LOOKAHEAD);
                Speed vGain = context.getParameters().getParameter(LmrsParameters.VGAIN);
                PerceivedGtu leader = leaders.first();
                Speed desiredSpeed = context.getDesiredSpeed();
                double rho = Tailgating.socialPressure(desiredSpeed, leader.getSpeed(), vGain, leader.getDistance(), x0);
                context.getParameters().setClaimedParameter(RHO, rho, this);
                double tNew = rho * tMin.si + (1.0 - rho) * tMax.si;
                if (tNew < t.si)
                {
                    context.getParameters().setClaimedParameter(ParameterTypes.T, Duration.ofSI(tNew), LmrsUtil.T_KEY);
                }
                Tailgating.deviate(context, rho);
            }
            catch (ParameterException exception)
            {
                throw new OtsRuntimeException("Could not obtain or set parameter value.", exception);
            }
        }

        @Override
        public String toString()
        {
            return "PRESSURE";
        }
    };

    /**
     * Returns a normalized social pressure, equal to (vDesired - vLead) / vGain.
     * @param desiredSpeed desired speed
     * @param leaderSpeed leader speed
     * @param vGain vGain parameter
     * @param headway headway to the leader
     * @param x0 anticipation distance
     * @return normalized social pressure
     */
    static double socialPressure(final Speed desiredSpeed, final Speed leaderSpeed, final Speed vGain, final Length headway,
            final Length x0)
    {
        double dv = desiredSpeed.si - leaderSpeed.si;
        if (dv < 0 || headway.gt(x0)) // larger headway may happen due to perception errors
        {
            return 0.0;
        }
        return 1.0 - Math.exp(-(dv / vGain.si) * (1.0 - (headway.si / x0.si)));
    }

    /**
     * Apply tailgating.
     * @param context tactical information such as parameters and car-following model
     */
    void tailgate(TacticalContextEgo context);

    /**
     * Add lateral deviation intent based on level of social pressure.
     * @param context tactical information such as parameters and car-following model
     * @param rho level of social pressure
     */
    static void deviate(final TacticalContextEgo context, final double rho)
    {
        if (context.getParameters().getOptionalParameter(DEV_RHO).orElse(false))
        {
            LanePosition position = context.getGtu().getPosition();
            // TODO: direction depends on left/right traffic
            Length deviation = position.lane().getWidth(position.position()).minus(context.getWidth()).times(0.5 * rho);
            context.addIntent(deviation, Length.ZERO);
        }
    }

}
