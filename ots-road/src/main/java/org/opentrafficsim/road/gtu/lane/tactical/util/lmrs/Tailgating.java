package org.opentrafficsim.road.gtu.lane.tactical.util.lmrs;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.base.parameters.constraint.ConstraintInterface;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtu;

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

    /** No tailgating. */
    Tailgating NONE = new Tailgating()
    {
        @Override
        public void tailgate(final LanePerception perception, final Parameters parameters)
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
        public void tailgate(final LanePerception perception, final Parameters parameters)
        {
            PerceptionCollectable<PerceivedGtu, LaneBasedGtu> leaders =
                    perception.getPerceptionCategoryOrNull(NeighborsPerception.class).getLeaders(RelativeLane.CURRENT);
            if (leaders == null || leaders.isEmpty())
            {
                return;
            }
            try
            {
                Speed speed = perception.getPerceptionCategoryOrNull(EgoPerception.class).getSpeed();
                Speed vCong = parameters.getParameter(ParameterTypes.VCONG);
                Length x0 = parameters.getParameter(ParameterTypes.LOOKAHEAD);
                Speed vGain = parameters.getParameter(LmrsParameters.VGAIN);
                PerceivedGtu leader = leaders.first();
                Speed desiredSpeed = Try.assign(() -> perception.getGtu().getDesiredSpeed(), "Could not obtain the GTU.");
                double rho = socialPressure(speed, vCong, desiredSpeed, leader.getSpeed(), vGain, leader.getDistance(), x0);
                parameters.setClaimedParameter(RHO, rho, this);
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
        public void tailgate(final LanePerception perception, final Parameters parameters)
        {
            PerceptionCollectable<PerceivedGtu, LaneBasedGtu> leaders =
                    perception.getPerceptionCategoryOrNull(NeighborsPerception.class).getLeaders(RelativeLane.CURRENT);
            if (leaders == null || leaders.isEmpty())
            {
                return;
            }
            try
            {
                Speed speed = perception.getPerceptionCategoryOrNull(EgoPerception.class).getSpeed();
                Speed vCong = parameters.getParameter(ParameterTypes.VCONG);
                Duration t = parameters.getParameter(ParameterTypes.T);
                Duration tMin = parameters.getParameter(ParameterTypes.TMIN);
                Duration tMax = parameters.getParameter(ParameterTypes.TMAX);
                Length x0 = parameters.getParameter(ParameterTypes.LOOKAHEAD);
                Speed vGain = parameters.getParameter(LmrsParameters.VGAIN);
                PerceivedGtu leader = leaders.first();
                Speed desiredSpeed = Try.assign(() -> perception.getGtu().getDesiredSpeed(), "Could not obtain the GTU.");
                double rho = socialPressure(speed, vCong, desiredSpeed, leader.getSpeed(), vGain, leader.getDistance(), x0);
                parameters.setClaimedParameter(RHO, rho, this);
                double tNew = rho * tMin.si + (1.0 - rho) * tMax.si;
                if (tNew < t.si)
                {
                    parameters.setClaimedParameter(ParameterTypes.T, Duration.ofSI(tNew), LmrsUtil.T_KEY);
                }
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
     * @param speed speed
     * @param vCong speed indicating congestion
     * @param desiredSpeed desired speed
     * @param leaderSpeed leader speed
     * @param vGain vGain parameter
     * @param headway headway to the leader
     * @param x0 anticipation distance
     * @return normalized social pressure
     */
    static double socialPressure(final Speed speed, final Speed vCong, final Speed desiredSpeed, final Speed leaderSpeed,
            final Speed vGain, final Length headway, final Length x0)
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
     * @param perception perception
     * @param parameters parameters
     */
    void tailgate(LanePerception perception, Parameters parameters);

}
