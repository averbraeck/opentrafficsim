package org.opentrafficsim.road.gtu.lane.tactical.util.lmrs;

import static org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Tailgating.socialPressure;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djutils.exceptions.Try;
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
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;

/**
 * Interface for LMRS tailgating behavior.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public interface Tailgating
{

    /** Social pressure applied to the leader. */
    ParameterTypeDouble RHO = new ParameterTypeDouble("rho", "Social pressure", 0.0, ConstraintInterface.UNITINTERVAL);

    /** No tailgating. */
    Tailgating NONE = new Tailgating()
    {
        /** {@inheritDoc} */
        @Override
        public void tailgate(final LanePerception perception, final Parameters parameters)
        {
            //
        }
    };

    /** No tailgating, but social pressure exists. */
    Tailgating RHO_ONLY = new Tailgating()
    {
        /** {@inheritDoc} */
        @Override
        public void tailgate(final LanePerception perception, final Parameters parameters)
        {
            PerceptionCollectable<HeadwayGtu, LaneBasedGtu> leaders =
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
                HeadwayGtu leader = leaders.first();
                Speed desiredSpeed = Try.assign(() -> perception.getGtu().getDesiredSpeed(), "Could not obtain the GTU.");
                double rho = socialPressure(speed, vCong, desiredSpeed, leader.getSpeed(), vGain, leader.getDistance(), x0);
                parameters.setParameter(RHO, rho);
            }
            catch (ParameterException exception)
            {
                throw new RuntimeException("Could not obtain or set parameter value.", exception);
            }
        }
    };

    /** Tailgating based on speed pressure. */
    Tailgating PRESSURE = new Tailgating()
    {
        /** {@inheritDoc} */
        @Override
        public void tailgate(final LanePerception perception, final Parameters parameters)
        {
            PerceptionCollectable<HeadwayGtu, LaneBasedGtu> leaders =
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
                HeadwayGtu leader = leaders.first();
                Speed desiredSpeed = Try.assign(() -> perception.getGtu().getDesiredSpeed(), "Could not obtain the GTU.");
                double rho = socialPressure(speed, vCong, desiredSpeed, leader.getSpeed(), vGain, leader.getDistance(), x0);
                parameters.setParameter(RHO, rho);
                double tNew = rho * tMin.si + (1.0 - rho) * tMax.si;
                if (tNew < t.si)
                {
                    parameters.setParameter(ParameterTypes.T, Duration.instantiateSI(tNew));
                }
            }
            catch (ParameterException exception)
            {
                throw new RuntimeException("Could not obtain or set parameter value.", exception);
            }
        }
    };

    /**
     * Returns a normalized social pressure, equal to (vDesired - vLead) / vGain.
     * @param speed Speed; speed
     * @param vCong Speed; speed indicating congestion
     * @param desiredSpeed Speed; desired speed
     * @param leaderSpeed Speed; leader speed
     * @param vGain Speed; vGain parameter
     * @param headway Length; headway to the leader
     * @param x0 Length; anticipation distance
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
     * @param perception LanePerception; perception
     * @param parameters Parameters; parameters
     */
    void tailgate(LanePerception perception, Parameters parameters);

}
