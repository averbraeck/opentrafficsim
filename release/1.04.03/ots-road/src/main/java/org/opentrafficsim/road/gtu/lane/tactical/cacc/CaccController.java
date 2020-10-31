package org.opentrafficsim.road.gtu.lane.tactical.cacc;


import org.djunits.Throw;
import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeAcceleration;
import org.opentrafficsim.base.parameters.ParameterTypeDouble;
import org.opentrafficsim.base.parameters.ParameterTypeDuration;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.perception.EgoPerception;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.DownstreamNeighborsIterable;
import org.opentrafficsim.road.gtu.lane.perception.InfrastructureLaneChangeInfo;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.InfrastructurePerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;

/**
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 27 sep. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class CaccController implements LongitudinalController
{

    /** Declare parameters. */
    public static final ParameterTypeDuration T_SYSTEM_CACC = CaccParameters.T_SYSTEM_CACC;

    public static final ParameterTypeDuration T_SYSTEM_ACC = CaccParameters.T_SYSTEM_ACC;

    public static final ParameterTypeDouble K = CaccParameters.K;

    public static final ParameterTypeDouble K_A = CaccParameters.K_A;

    public static final ParameterTypeDouble K_V = CaccParameters.K_V;

    public static final ParameterTypeDouble K_D = CaccParameters.K_D;

    public static final ParameterTypeAcceleration A_MIN = CaccParameters.A_MIN;

    public static final ParameterTypeAcceleration A_MAX = CaccParameters.A_MAX;

    public static final ParameterTypeDouble R_MIN = CaccParameters.R_MIN;

    public static final ParameterTypeLength STANDSTILL = CaccParameters.STANDSTILL;

    // public static final ParameterTypeSpeed SET_SPEED = CaccParameters.SET_SPEED;
    
    private GTUType caccGTUType = null;

    /** Platoon. */
    private Platoon platoon;

    private Speed setSpeed;

    /**
     * Sets the platoon.
     * @param platoon Platoon; platoon
     */
    public void setPlatoon(final Platoon platoon)
    {
        this.platoon = platoon;
    }
    
    /**
     * Set the GTU type that has CACC.
     * @param gtuType GTUType; the GTU type that has CACC
     */
    public void setCACCGTUType(final GTUType gtuType)
    {
        this.caccGTUType = gtuType;
    }

    /**
     * {@inheritDoc}
     * @throws OperationalPlanException
     * @throws ParameterException
     * @Override
     */
    @Override
    public Acceleration calculateAcceleration(final LaneBasedGTU gtu) throws OperationalPlanException, ParameterException
    {
        Parameters parameters = gtu.getParameters();
        LanePerception perception = gtu.getTacticalPlanner().getPerception();
        CaccPerceptionCategory caccPerception = perception.getPerceptionCategory(CaccPerceptionCategory.class);
        EgoPerception<?, ?> ego = perception.getPerceptionCategory(EgoPerception.class);

        DownstreamNeighborsIterable leaders = caccPerception.getLeaders(RelativeLane.CURRENT);
        DownstreamNeighborsIterable leadersLeft = caccPerception.getLeaders(RelativeLane.LEFT);
        DownstreamNeighborsIterable leadersRight = caccPerception.getLeaders(RelativeLane.RIGHT);
        Speed newSetSpeed;

        if (leaders.isEmpty() || (this.platoon != null && !this.platoon.isInPlatoon(leaders.first().getId())))
        {
            //
            newSetSpeed = parameters.getParameter(CaccParameters.SET_SPEED);
        }
        else
        {
            newSetSpeed = parameters.getParameter(CaccParameters.SET_SPEED).plus(Speed.instantiateSI(10.0));
        }

        this.setSpeed = Speed.min(newSetSpeed, gtu.getMaximumSpeed());

        // car-following
        Acceleration a = followLeader(leaders, parameters, ego);

        for (DownstreamNeighborsIterable ldrs : new DownstreamNeighborsIterable[] {leadersLeft, leadersRight})
        {
            if (ldrs != null && !ldrs.isEmpty() && this.platoon != null && this.platoon.isInPlatoon(ldrs.first().getId()))
            {
                a = Acceleration.min(a, followLeader(ldrs, parameters, ego));
            }
        }

        // Synchronization for dead-end
        // stop for end
        Length remainingDist = null;
        for (InfrastructureLaneChangeInfo ili : perception.getPerceptionCategory(InfrastructurePerception.class)
                .getInfrastructureLaneChangeInfo(RelativeLane.CURRENT))
        {
            if (remainingDist == null || remainingDist.gt(ili.getRemainingDistance()))
            {
                remainingDist = ili.getRemainingDistance();
            }
        }
        if (remainingDist != null)
        {
            Speed speed = ego.getSpeed();
            Acceleration bCrit = parameters.getParameter(ParameterTypes.BCRIT);
            remainingDist = remainingDist.minus(parameters.getParameter(STANDSTILL));

            remainingDist = remainingDist.minus(Length.instantiateSI(10));
            Length remainingDistStart = remainingDist.minus(Length.instantiateSI(10));
            if (remainingDistStart.le0())
            {
                if (speed.gt0())
                {
                    a = Acceleration.min(a, bCrit.neg());
                }
                else
                {
                    a = Acceleration.min(a, Acceleration.ZERO);
                }
            }
            else
            {
                Acceleration bMin = new Acceleration(.5 * speed.si * speed.si / remainingDist.si, AccelerationUnit.SI);
                if (bMin.ge(bCrit))
                {
                    a = Acceleration.min(a, bMin.neg());
                }
            }
        }

        return a;

    }

    /**
     * Follow leaders in a lane
     * @param leaders
     * @param parameters
     * @param ego
     * @return following acceleration
     * @throws ParameterException
     */
    private Acceleration followLeader(final PerceptionCollectable<HeadwayGTU, LaneBasedGTU> leaders,
            final Parameters parameters, final EgoPerception<?, ?> ego) throws ParameterException
    {

        HeadwayGTU leader = leaders == null || leaders.isEmpty() ? null : leaders.first();

        // Parameters
        Acceleration amin = parameters.getParameter(A_MIN);
        Acceleration amax = parameters.getParameter(A_MAX);
        double k = parameters.getParameter(K);
        double ka = parameters.getParameter(K_A);
        double kv = parameters.getParameter(K_V);
        double kd = parameters.getParameter(K_D);
        double rmin = parameters.getParameter(R_MIN);
        Length standstill = parameters.getParameter(STANDSTILL);

        Throw.whenNull(this.caccGTUType, "Oops, forgot to set caccGTUType?");
        if (leader != null && leader.getGtuType().isOfType(this.caccGTUType))
        {
            // CACC control algorithm

            // Variables
            Speed v = ego.getSpeed();
            Acceleration ap = leader.getAcceleration();
            Speed vp = leader.getSpeed();
            Length r = leader.getDistance();
            double tsystem;

            // Check if leading vehicle is part of current platoon, set time headway accordingly
            if (this.platoon != null && this.platoon.isInPlatoon(leader.getId()))
            {
                tsystem = parameters.getParameter(T_SYSTEM_CACC).doubleValue();
            }
            else
            {
                tsystem = parameters.getParameter(T_SYSTEM_ACC).doubleValue();
            }

            // Calculate spacing
            double rsystem = (tsystem * v.si) + standstill.si;
            double rsafe = rsystem; // System should take time headway as minimum spacing (desired spacing)
            double rref = Math.max(rmin, Math.min(rsafe, rsystem));

            // Calculate acceleration
            double av = k * (this.setSpeed.si - v.si) + kd * (r.si - rref);
            double ad = ka * ap.si + kv * (vp.si - v.si) + kd * (r.si - rref);
            double aCACC;

            if (ap.si > 0)
            {
                aCACC = ad;
            }
            else
            {
                aCACC = Math.min(av, ad);
            }

            return Acceleration.instantiateSI(aCACC < amax.si ? (aCACC > amin.si ? aCACC : amin.si) : amax.si);

        }
        else
        {
            // ACC & CC control algorithm
            // ACC: when leading vehicle is present
            // CC: when no leading vehicle is present

            // Variables
            Speed v = ego.getSpeed();
            Double r;
            Speed vp;

            // Parameters specific to ACC/CC control
            double tsystem = parameters.getParameter(T_SYSTEM_ACC).doubleValue();

            // Calculate spacing
            double rsystem = tsystem * v.si + standstill.si;
            double rref = Math.max(rmin, rsystem);

            // CC in case of no leader, ACC in case of leader
            double ad;
            if (leader == null)
            {
                ad = Double.POSITIVE_INFINITY;
            }
            else
            {
                r = leader.getDistance().si - standstill.si;
                vp = leader.getSpeed();
                ad = (kv * (vp.si - v.si)) + (kd * (r - rref));
            }

            // Calculate acceleration
            double av = k * (this.setSpeed.si - v.si);

            double aACC = Math.min(av, ad);
            return Acceleration.instantiateSI(aACC < amax.si ? (aACC > amin.si ? aACC : amin.si) : amax.si);
        }
    }

}
