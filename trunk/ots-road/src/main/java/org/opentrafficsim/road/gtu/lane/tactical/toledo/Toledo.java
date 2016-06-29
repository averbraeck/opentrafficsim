package org.opentrafficsim.road.gtu.lane.tactical.toledo;

import java.util.Iterator;
import java.util.Random;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.LinearDensityUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.GTU;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.AbstractHeadwayGTU;
import org.opentrafficsim.road.gtu.lane.perception.InfrastructureLaneChangeInfo;
import org.opentrafficsim.road.gtu.lane.perception.PerceivedSurroundings;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitProspect;

/**
 * Implementation of the model of Toledo (2003).<br>
 * <br>
 * Tomer Toledo (2003) "Integrated Driving Behavior Modeling", Massachusetts Institute of Technology.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jun 21, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */

public class Toledo extends AbstractLaneBasedTacticalPlanner
{

    /** */
    private static final long serialVersionUID = 1L;

    /** Defines light vs heavy vehicles. */
    static final Length MAX_LIGHT_VEHICLE_LENGTH = new Length(9.14, LengthUnit.METER);

    /** Distance defining tailgating. */
    static final Length TAILGATE_LENGTH = new Length(10, LengthUnit.METER);

    /** Density for tailgating (Level Of Service (LOS) A, B or C). */
    static final LinearDensity LOS_DENSITY = new LinearDensity(16, LinearDensityUnit.PER_KILOMETER);

    /** Random number generator. */
    private static final Random RANDOM = new Random();

    /**
     * Constructor.
     * @param carFollowingModel Car-following model.
     */
    public Toledo(final CarFollowingModel carFollowingModel)
    {
        super(carFollowingModel);
    }

    /** {@inheritDoc} */
    @Override
    public final OperationalPlan generateOperationalPlan(final GTU gtu, final Time startTime,
        final DirectedPoint locationAtStartTime) throws OperationalPlanException, GTUException, NetworkException,
        ParameterException
    {

        // obtain objects to get info
        LaneBasedGTU gtuLane = (LaneBasedGTU) gtu;
        PerceivedSurroundings perception = (PerceivedSurroundings) gtu.getPerception();
        SpeedLimitProspect slp = perception.getSpeedLimitProspect(RelativeLane.CURRENT);
        SpeedLimitInfo sli = slp.getSpeedLimitInfo(Length.ZERO);
        BehavioralCharacteristics bc = gtuLane.getBehavioralCharacteristics();

        // get infrastructure info
        InfrastructureLaneChangeInfo ilciCur = perception.getInfrastructureLaneChangeInfo(RelativeLane.CURRENT).first();
        Length dExit = ilciCur.getRemainingDistance();
        int[] deltaCur = new int[3];
        // number of lane changes without the off ramp itself, so -1
        if (ilciCur.getRequiredNumberOfLaneChanges() > 1 && ilciCur.getRequiredNumberOfLaneChanges() < 5)
        {
            deltaCur[ilciCur.getRequiredNumberOfLaneChanges() - 2] = 1;
        }
        InfrastructureLaneChangeInfo ilciLef = perception.getInfrastructureLaneChangeInfo(RelativeLane.LEFT).first();
        int[] deltaLef = new int[3];
        if (ilciLef.getRequiredNumberOfLaneChanges() > 1 && ilciLef.getRequiredNumberOfLaneChanges() < 5)
        {
            deltaLef[ilciLef.getRequiredNumberOfLaneChanges() - 2] = 1;
        }
        InfrastructureLaneChangeInfo ilciRig = perception.getInfrastructureLaneChangeInfo(RelativeLane.RIGHT).first();
        int[] deltaRig = new int[3];
        if (ilciRig.getRequiredNumberOfLaneChanges() > 1 && ilciRig.getRequiredNumberOfLaneChanges() < 5)
        {
            deltaRig[ilciRig.getRequiredNumberOfLaneChanges() - 2] = 1;
        }
        // TODO next off ramp
        int deltaNextExitLef = 0;
        int deltaNextExitCur = 0;
        int deltaNextExitRig = 0;
        int deltaAddLef = 0; // 1, 2
        int deltaAddCur = 0;
        int deltaAddRig = 0;

        // heavy neighbor
        // TODO not getDistance but getLength
        Length leaderLength =
            !perception.getFirstLeaders(null).isEmpty() ? perception.getFirstLeaders(null).first().getDistance()
                : Length.ZERO;
        Length followerLength =
            !perception.getFirstFollowers(null).isEmpty() ? perception.getFirstFollowers(null).first().getDistance()
                : Length.ZERO;
        int deltaHeavyCur = leaderLength.gt(MAX_LIGHT_VEHICLE_LENGTH) || followerLength.gt(MAX_LIGHT_VEHICLE_LENGTH) ? 1 : 0;

        leaderLength =
            !perception.getFirstLeaders(LateralDirectionality.LEFT).isEmpty() ? perception.getFirstLeaders(
                LateralDirectionality.LEFT).first().getDistance() : Length.ZERO;
        followerLength =
            !perception.getFirstFollowers(LateralDirectionality.LEFT).isEmpty() ? perception.getFirstFollowers(
                LateralDirectionality.LEFT).first().getDistance() : Length.ZERO;
        int deltaHeavyLef = leaderLength.gt(MAX_LIGHT_VEHICLE_LENGTH) || followerLength.gt(MAX_LIGHT_VEHICLE_LENGTH) ? 1 : 0;

        leaderLength =
            !perception.getFirstLeaders(LateralDirectionality.RIGHT).isEmpty() ? perception.getFirstLeaders(
                LateralDirectionality.RIGHT).first().getDistance() : Length.ZERO;
        followerLength =
            !perception.getFirstFollowers(LateralDirectionality.RIGHT).isEmpty() ? perception.getFirstFollowers(
                LateralDirectionality.RIGHT).first().getDistance() : Length.ZERO;
        int deltaHeavyRig = leaderLength.gt(MAX_LIGHT_VEHICLE_LENGTH) || followerLength.gt(MAX_LIGHT_VEHICLE_LENGTH) ? 1 : 0;

        // get densities
        LinearDensity dLef = getDensityInLane(gtu, perception, RelativeLane.LEFT);
        LinearDensity dCur = getDensityInLane(gtu, perception, RelativeLane.CURRENT);
        LinearDensity dRig = getDensityInLane(gtu, perception, RelativeLane.RIGHT);

        // tailgating
        int deltaTailgate = 0;
        if (!perception.getFollowers(RelativeLane.CURRENT).isEmpty()
            && perception.getFollowers(RelativeLane.CURRENT).first().getDistance().le(TAILGATE_LENGTH))
        {
            LinearDensity dRoad = new LinearDensity((dLef.si + dCur.si + dRig.si) / 3, LinearDensityUnit.SI);
            if (dRoad.le(LOS_DENSITY))
            {
                deltaTailgate = 1;
            }
        }

        // right most
        // TODO definition of 'right most lane' does not account for ramps and weaving sections
        int deltaRightMostCur = 0;
        int deltaRightMostRig = 0;
        if (perception.getCurrentCrossSection().contains(RelativeLane.RIGHT)
            && !perception.getCurrentCrossSection().contains(RelativeLane.SECOND_RIGHT))
        {
            deltaRightMostRig = 1;
        }
        else if (!perception.getCurrentCrossSection().contains(RelativeLane.RIGHT))
        {
            deltaRightMostCur = 1;
        }

        // current lane traffic
        Speed vFront;
        Length sFront;
        if (!perception.getLeaders(RelativeLane.CURRENT).isEmpty())
        {
            vFront = perception.getLeaders(RelativeLane.CURRENT).first().getSpeed();
            sFront = perception.getLeaders(RelativeLane.CURRENT).first().getDistance();
        }
        else
        {
            vFront = getCarFollowingModel().desiredSpeed(bc, sli);
            sFront = getCarFollowingModel().desiredHeadway(bc, gtu.getSpeed());
        }

        // TODO figure out...
        double EMU_GAleft = 0;
        double EMU_GAright = 0;

        // target lane utilities
        // {@formatter:off}
        double vCur = 
                bc.getParameter(ToledoParameters.C_CL) 
                + bc.getParameter(ToledoParameters.BETA_RIGHT_MOST) * deltaRightMostCur 
                + bc.getParameter(ToledoParameters.BETA_VFRONT) * vFront.si 
                + bc.getParameter(ToledoParameters.BETA_SFRONT) * sFront.si
                + bc.getParameter(ToledoParameters.BETA_DENSITY) * dCur.getInUnit(LinearDensityUnit.PER_KILOMETER)
                + bc.getParameter(ToledoParameters.BETA_HEAVY_NEIGHBOUR) * deltaHeavyCur
                + bc.getParameter(ToledoParameters.BETA_TAILGATE) * deltaTailgate
                + Math.pow(dExit.getInUnit(LengthUnit.KILOMETER), bc.getParameter(ToledoParameters.THETA_MLC)) 
                * (bc.getParameter(ToledoParameters.BETA1) * deltaCur[0] 
                        + bc.getParameter(ToledoParameters.BETA2) * deltaCur[1] 
                        + bc.getParameter(ToledoParameters.BETA3) * deltaCur[2])
                + bc.getParameter(ToledoParameters.BETA_NEXT_EXIT) * deltaNextExitCur
                + bc.getParameter(ToledoParameters.BETA_ADD) * deltaAddCur
                + bc.getParameter(ToledoParameters.ALPHA_CL) * bc.getParameter(ToledoParameters.ERROR_TERM);
        
        double vRig = 
                bc.getParameter(ToledoParameters.C_RL) 
                + bc.getParameter(ToledoParameters.BETA_RIGHT_MOST) * deltaRightMostRig 
                + bc.getParameter(ToledoParameters.BETA_DENSITY) * dRig.getInUnit(LinearDensityUnit.PER_KILOMETER)
                + bc.getParameter(ToledoParameters.BETA_HEAVY_NEIGHBOUR) * deltaHeavyRig
                + Math.pow(dExit.getInUnit(LengthUnit.KILOMETER), bc.getParameter(ToledoParameters.THETA_MLC)) 
                * (bc.getParameter(ToledoParameters.BETA1) * deltaRig[0] 
                        + bc.getParameter(ToledoParameters.BETA2) * deltaRig[1] 
                        + bc.getParameter(ToledoParameters.BETA3) * deltaRig[2])
                + bc.getParameter(ToledoParameters.BETA_NEXT_EXIT) * deltaNextExitRig
                + bc.getParameter(ToledoParameters.BETA_ADD) * deltaAddRig
                + bc.getParameter(ToledoParameters.BETA_EMU_GA) * EMU_GAright
                + bc.getParameter(ToledoParameters.ALPHA_RL) * bc.getParameter(ToledoParameters.ERROR_TERM);
        
        double vLef = 
                bc.getParameter(ToledoParameters.BETA_DENSITY) * dLef.getInUnit(LinearDensityUnit.PER_KILOMETER)
                + bc.getParameter(ToledoParameters.BETA_HEAVY_NEIGHBOUR) * deltaHeavyLef
                + Math.pow(dExit.getInUnit(LengthUnit.KILOMETER), bc.getParameter(ToledoParameters.THETA_MLC)) 
                * (bc.getParameter(ToledoParameters.BETA1) * deltaLef[0] 
                        + bc.getParameter(ToledoParameters.BETA2) * deltaLef[1] 
                        + bc.getParameter(ToledoParameters.BETA3) * deltaLef[2])
                + bc.getParameter(ToledoParameters.BETA_NEXT_EXIT) * deltaNextExitLef
                + bc.getParameter(ToledoParameters.BETA_ADD) * deltaAddLef
                + bc.getParameter(ToledoParameters.BETA_EMU_GA) * EMU_GAleft;
        // {@formatter:on}

        // gap acceptance
        Speed dVLeadLef;
        Length sLeadLef;
        if (!perception.getLeaders(RelativeLane.LEFT).isEmpty())
        {
            dVLeadLef = perception.getLeaders(RelativeLane.LEFT).first().getSpeed().minus(gtu.getSpeed());
            sLeadLef = perception.getLeaders(RelativeLane.LEFT).first().getDistance();
        }
        else
        {
            dVLeadLef = Speed.ZERO;
            sLeadLef = new Length(Double.POSITIVE_INFINITY, LengthUnit.SI);
        }
        Speed dVLeadRig;
        Length sLeadRig;
        if (!perception.getLeaders(RelativeLane.RIGHT).isEmpty())
        {
            dVLeadRig = perception.getLeaders(RelativeLane.RIGHT).first().getSpeed().minus(gtu.getSpeed());
            sLeadRig = perception.getLeaders(RelativeLane.RIGHT).first().getDistance();
        }
        else
        {
            dVLeadRig = Speed.ZERO;
            sLeadRig = new Length(Double.POSITIVE_INFINITY, LengthUnit.SI);
        }
        Speed dVLagLef;
        Length sLagLef;
        if (!perception.getFollowers(RelativeLane.LEFT).isEmpty())
        {
            dVLagLef = perception.getFollowers(RelativeLane.LEFT).first().getSpeed().minus(gtu.getSpeed());
            sLagLef = perception.getFollowers(RelativeLane.LEFT).first().getDistance();
        }
        else
        {
            dVLagLef = Speed.ZERO;
            sLagLef = new Length(Double.POSITIVE_INFINITY, LengthUnit.SI);
        }
        Speed dVLagRig;
        Length sLagRig;
        if (!perception.getFollowers(RelativeLane.RIGHT).isEmpty())
        {
            dVLagRig = perception.getFollowers(RelativeLane.RIGHT).first().getSpeed().minus(gtu.getSpeed());
            sLagRig = perception.getFollowers(RelativeLane.RIGHT).first().getDistance();
        }
        else
        {
            dVLagRig = Speed.ZERO;
            sLagRig = new Length(Double.POSITIVE_INFINITY, LengthUnit.SI);
        }

        // TODO figure out...
        double EMU_TGleft = 0;
        double EMU_TGright = 0;

        // gap acceptance random terms
        double eLead =
            RANDOM.nextGaussian() * bc.getParameter(ToledoParameters.SIGMA_LEAD)
                * bc.getParameter(ToledoParameters.SIGMA_LEAD);
        double eLag =
            RANDOM.nextGaussian() * bc.getParameter(ToledoParameters.SIGMA_LAG)
                * bc.getParameter(ToledoParameters.SIGMA_LAG);

        // critical gaps
        // {@formatter:off}
        Length gLeadLef = new Length(Math.exp(bc.getParameter(ToledoParameters.C_LEAD) 
            + bc.getParameter(ToledoParameters.BETA_POS_LEAD) * Speed.max(dVLeadLef, Speed.ZERO).si
            + bc.getParameter(ToledoParameters.BETA_NEG_LEAD) * Speed.min(dVLeadLef, Speed.ZERO).si
            + bc.getParameter(ToledoParameters.BETA_EMU_LEAD) * EMU_TGleft 
            + bc.getParameter(ToledoParameters.ALPHA_TL_LEAD) * bc.getParameter(ToledoParameters.ERROR_TERM)
            + eLead), LengthUnit.SI);
        Length gLagLef = new Length(Math.exp(bc.getParameter(ToledoParameters.C_LAG) 
            + bc.getParameter(ToledoParameters.BETA_POS_LAG) * Speed.max(dVLagLef, Speed.ZERO).si
            + bc.getParameter(ToledoParameters.BETA_EMU_LAG) * EMU_TGleft 
            + bc.getParameter(ToledoParameters.ALPHA_TL_LAG) * bc.getParameter(ToledoParameters.ERROR_TERM)
            + eLag), LengthUnit.SI);
        Length gLeadRig = new Length(Math.exp(bc.getParameter(ToledoParameters.C_LEAD) 
            + bc.getParameter(ToledoParameters.BETA_POS_LEAD) * Speed.max(dVLeadRig, Speed.ZERO).si
            + bc.getParameter(ToledoParameters.BETA_NEG_LEAD) * Speed.min(dVLeadRig, Speed.ZERO).si
            + bc.getParameter(ToledoParameters.BETA_EMU_LEAD) * EMU_TGright 
            + bc.getParameter(ToledoParameters.ALPHA_TL_LEAD) * bc.getParameter(ToledoParameters.ERROR_TERM)
            + eLead), LengthUnit.SI);
        Length gLagRig = new Length(Math.exp(bc.getParameter(ToledoParameters.C_LAG) 
            + bc.getParameter(ToledoParameters.BETA_POS_LAG) * Speed.max(dVLagRig, Speed.ZERO).si
            + bc.getParameter(ToledoParameters.BETA_EMU_LAG) * EMU_TGright 
            + bc.getParameter(ToledoParameters.ALPHA_TL_LAG) * bc.getParameter(ToledoParameters.ERROR_TERM)
            + eLag), LengthUnit.SI);
        // {@formatter:on}

        // change lane?
        boolean changeLeft = vLef > vRig && vLef > vCur && gLeadLef.lt(sLeadLef) && gLagLef.lt(sLagLef);
        boolean changeRight = vRig > vLef && vRig > vCur && gLeadRig.lt(sLeadRig) && gLagRig.lt(sLagRig);

        // TODO vehicle not ahead and not backwards but completely adjacent

        double vFwdLef = gapUtility(gtu, bc, perception, Gap.FORWARD, RelativeLane.LEFT);
        double vBckLef = gapUtility(gtu, bc, perception, Gap.BACKWARD, RelativeLane.LEFT);
        double vFwdRig = gapUtility(gtu, bc, perception, Gap.FORWARD, RelativeLane.RIGHT);
        double vBckRig = gapUtility(gtu, bc, perception, Gap.BACKWARD, RelativeLane.RIGHT);

        return null;
    }

    /**
     * Calculates utility of gap.
     * @param gtu GTU
     * @param bc behavioral characteristics
     * @param perception perception
     * @param gap gap
     * @param lane lane
     * @return utility of gap
     * @throws ParameterException if parameter is not given
     */
    private double gapUtility(final GTU gtu, final BehavioralCharacteristics bc, final PerceivedSurroundings perception,
        final Gap gap, final RelativeLane lane) throws ParameterException
    {

        // capture no leaders/follower cases for forward and backward gaps
        if (gap.equals(Gap.FORWARD) && perception.getLeaders(lane).isEmpty())
        {
            // no leaders
            return Double.NEGATIVE_INFINITY;
        }
        if (gap.equals(Gap.BACKWARD) && perception.getFollowers(lane).isEmpty())
        {
            // no followers
            return Double.NEGATIVE_INFINITY;
        }

        // values to be determined directly
        double constant;
        double alpha;
        Length distanceToGap;
        int deltaFrontVehicle;

        // values to determine effective gap and relative gap speed
        Length leaderDist; // leader of gap
        Speed leaderSpeed;
        Length followerDist; // follower of gap
        Speed followerSpeed;

        // depending on gap, set values, find leader and follower
        if (gap.equals(Gap.FORWARD))
        {
            constant = bc.getParameter(ToledoParameters.C_FWD);
            alpha = 0;
            if (perception.getLeaders(lane).size() > 1)
            {
                // two leaders
                // TODO perception list instead of sorted set???
                Iterator<AbstractHeadwayGTU> it = perception.getLeaders(lane).iterator();
                it.next();
                AbstractHeadwayGTU leader = it.next();
                leaderDist = leader.getDistance();
                leaderSpeed = leader.getSpeed();
            }
            else
            {
                // one leader
                leaderDist = new Length(Double.POSITIVE_INFINITY, LengthUnit.SI);
                leaderSpeed = new Speed(Double.POSITIVE_INFINITY, SpeedUnit.SI);
            }
            // TODO getLength() instead of getDistance() (Second call only)
            // distance to nose, so add length
            followerDist =
                perception.getLeaders(lane).first().getDistance().plus(perception.getLeaders(lane).first().getDistance());
            followerSpeed = perception.getLeaders(lane).first().getSpeed();
            distanceToGap = followerDist; // same as distance to nose of first leader
        }
        else if (gap.equals(Gap.ADJACENT))
        {
            constant = 0;
            alpha = bc.getParameter(ToledoParameters.ALPHA_ADJ);
            distanceToGap = Length.ZERO;
            if (!perception.getLeaders(lane).isEmpty())
            {
                leaderDist = perception.getLeaders(lane).first().getDistance();
                leaderSpeed = perception.getLeaders(lane).first().getSpeed();
            }
            else
            {
                leaderDist = new Length(Double.POSITIVE_INFINITY, LengthUnit.SI);
                leaderSpeed = new Speed(Double.POSITIVE_INFINITY, SpeedUnit.SI);
            }
            if (!perception.getFollowers(lane).isEmpty())
            {
                // plus own vehicle length for distance from nose of own vehicle (and then whole negative)
                followerDist = perception.getFollowers(lane).first().getDistance().plus(gtu.getLength()).multiplyBy(-1.0);
                followerSpeed = perception.getFollowers(lane).first().getSpeed();
            }
            else
            {
                followerDist = new Length(Double.NEGATIVE_INFINITY, LengthUnit.SI);
                followerSpeed = new Speed(Double.NEGATIVE_INFINITY, SpeedUnit.SI);
            }
        }
        else
        {
            constant = bc.getParameter(ToledoParameters.C_BCK);
            alpha = bc.getParameter(ToledoParameters.ALPHA_BCK);
            deltaFrontVehicle = 0;
            if (perception.getFollowers(lane).size() > 1)
            {
                // two followers
                Iterator<AbstractHeadwayGTU> it = perception.getFollowers(lane).iterator();
                it.next();
                AbstractHeadwayGTU follower = it.next();
                followerDist = follower.getDistance().multiplyBy(-1.0);
                followerSpeed = follower.getSpeed();
            }
            else
            {
                // one follower
                followerDist = new Length(Double.NEGATIVE_INFINITY, LengthUnit.SI);
                followerSpeed = new Speed(Double.NEGATIVE_INFINITY, SpeedUnit.SI);
            }
            // TODO getLength() instead of getDistance() (Second call only)
            // add vehicle length to get distance to tail of 1st follower (and then whole negative)
            leaderDist =
                perception.getFollowers(lane).first().getDistance().plus(perception.getLeaders(lane).first().getDistance())
                    .multiplyBy(-1.0);
            leaderSpeed = perception.getFollowers(lane).first().getSpeed();
            distanceToGap = perception.getFollowers(lane).first().getDistance().plus(gtu.getLength()); // own nose to nose
        }

        // limit by leader in current lane
        if (!perception.getLeaders(RelativeLane.CURRENT).isEmpty()
            && perception.getLeaders(RelativeLane.CURRENT).first().getDistance().lt(leaderDist))
        {
            leaderDist = perception.getLeaders(RelativeLane.CURRENT).first().getDistance();
            leaderSpeed = perception.getLeaders(RelativeLane.CURRENT).first().getSpeed();
            deltaFrontVehicle = 1; // never happens for backward headway due to negative distances
        }
        else
        {
            deltaFrontVehicle = 0;
        }

        // calculate relative gap speed and effective gap
        Speed dV = followerSpeed.minus(leaderSpeed);
        Length effectiveGap = leaderDist.minus(followerDist);

        // {@formatter:off}
        // calculate utility 
        return constant 
                + bc.getParameter(ToledoParameters.BETA_DTG) * distanceToGap.si
                + bc.getParameter(ToledoParameters.BETA_EG) * effectiveGap.si
                + bc.getParameter(ToledoParameters.BETA_FV) * deltaFrontVehicle
                + bc.getParameter(ToledoParameters.BETA_RGS) * dV.si
                + alpha * bc.getParameter(ToledoParameters.ERROR_TERM);
        // {@formatter:on}

    }

    private final LinearDensity getDensityInLane(final GTU gtu, final PerceivedSurroundings perception,
        final RelativeLane lane)
    {
        int nVehicles = 0;
        nVehicles += perception.getFollowers(lane).size();
        nVehicles += perception.getLeaders(lane).size();
        if (nVehicles > 0)
        {
            Length d1;
            if (!perception.getFollowers(lane).isEmpty())
            {
                d1 = perception.getFollowers(lane).last().getDistance();
                d1 = d1.plus(gtu.getLength().divideBy(2.0));
                // TODO not getDistance but getLength
                d1 = d1.plus(perception.getFollowers(lane).last().getDistance().divideBy(2.0));
            }
            else
            {
                d1 = Length.ZERO;
            }
            Length d2;
            if (!perception.getLeaders(lane).isEmpty())
            {
                d2 = perception.getLeaders(lane).last().getDistance();
                d2 = d2.plus(gtu.getLength().divideBy(2.0));
                // TODO not getDistance but getLength
                d2 = d2.plus(perception.getLeaders(lane).last().getDistance().divideBy(2.0));
            }
            else
            {
                d2 = Length.ZERO;
            }
            return new LinearDensity(nVehicles / d1.plus(d2).getInUnit(LengthUnit.KILOMETER),
                LinearDensityUnit.PER_KILOMETER);
        }
        return LinearDensity.ZERO;
    }

    private enum Gap
    {
        FORWARD, ADJACENT, BACKWARD;
    }

}
