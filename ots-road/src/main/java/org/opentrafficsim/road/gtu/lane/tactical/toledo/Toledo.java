package org.opentrafficsim.road.gtu.lane.tactical.toledo;

import java.util.Iterator;
import java.util.Random;

import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.LinearDensityUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
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
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.AbstractHeadwayGTU;
import org.opentrafficsim.road.gtu.lane.perception.InfrastructureLaneChangeInfo;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceivedSurroundings;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.util.CarFollowingUtil;
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
    private static final long serialVersionUID = 20160711L;

    /** Defines light vs heavy vehicles. */
    static final Length MAX_LIGHT_VEHICLE_LENGTH = new Length(9.14, LengthUnit.METER);

    /** Distance defining tail gating. */
    static final Length TAILGATE_LENGTH = new Length(10, LengthUnit.METER);

    /** Density for tail gating (Level Of Service (LOS) A, B or C). */
    static final LinearDensity LOS_DENSITY = new LinearDensity(16, LinearDensityUnit.PER_KILOMETER);

    /** Random number generator. */
    public static final Random RANDOM = new Random();

    /** Currently changing left. */
    private boolean changingLeft = false;

    /** Currently changing right. */
    private boolean changingRight = false;

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
        LanePerception perception = gtuLane.getPerception();
        perception.perceive();
        SpeedLimitProspect slp = perception.getSpeedLimitProspect(RelativeLane.CURRENT);
        SpeedLimitInfo sli = slp.getSpeedLimitInfo(Length.ZERO);
        BehavioralCharacteristics bc = gtuLane.getBehavioralCharacteristics();

        // 3rd layer of model: Target gap model
        // TODO vehicle not ahead and not backwards but completely adjacent
        GapInfo gapFwdL = getGapInfo(gtu, bc, perception, Gap.FORWARD, RelativeLane.LEFT);
        GapInfo gapAdjL = getGapInfo(gtu, bc, perception, Gap.ADJACENT, RelativeLane.LEFT);
        GapInfo gapBckL = getGapInfo(gtu, bc, perception, Gap.BACKWARD, RelativeLane.LEFT);
        GapInfo gapFwdR = getGapInfo(gtu, bc, perception, Gap.FORWARD, RelativeLane.RIGHT);
        GapInfo gapAdjR = getGapInfo(gtu, bc, perception, Gap.ADJACENT, RelativeLane.RIGHT);
        GapInfo gapBckR = getGapInfo(gtu, bc, perception, Gap.BACKWARD, RelativeLane.RIGHT);
        double emuTgL =
            Math.log(Math.exp(gapFwdL.getUtility()) + Math.exp(gapAdjL.getUtility()) + Math.exp(gapBckL.getUtility()));
        double emuTgR =
            Math.log(Math.exp(gapFwdR.getUtility()) + Math.exp(gapAdjR.getUtility()) + Math.exp(gapBckR.getUtility()));

        // 2nd layer of model: Gap-acceptance model
        // gap acceptance random terms (variable over time, equal for left and right)
        double eLead = RANDOM.nextGaussian() * Math.pow(bc.getParameter(ToledoLaneChangeParameters.SIGMA_LEAD), 2);
        double eLag = RANDOM.nextGaussian() * Math.pow(bc.getParameter(ToledoLaneChangeParameters.SIGMA_LAG), 2);
        GapAcceptanceInfo gapAcceptL = getGapAcceptanceInfo(gtu, bc, perception, emuTgL, eLead, eLag, RelativeLane.LEFT);
        GapAcceptanceInfo gapAcceptR = getGapAcceptanceInfo(gtu, bc, perception, emuTgR, eLead, eLag, RelativeLane.RIGHT);

        // 1st layer of model: Target lane model
        double vL = laneUtility(gtu, bc, perception, gapAcceptL.getEmu(), sli, RelativeLane.LEFT);
        double vC = laneUtility(gtu, bc, perception, 0, sli, RelativeLane.CURRENT);
        double vR = laneUtility(gtu, bc, perception, gapAcceptR.getEmu(), sli, RelativeLane.RIGHT);

        // change lane?
        boolean changeLeft = vL > vR && vL > vC && gapAcceptL.isAcceptable();
        boolean changeRight = vR > vL && vR > vC && gapAcceptR.isAcceptable();
        if (changeLeft)
        {
            this.changingLeft = true;
        }
        if (changeRight)
        {
            this.changingRight = true;
        }

        // acceleration model
        Acceleration acceleration;
        if (this.changingLeft)
        {
            acceleration =
                CarFollowingUtil.followLeaders(getCarFollowingModel(), bc, gtu.getSpeed(), sli, perception
                    .getLeaders(RelativeLane.LEFT));
        }
        else if (this.changingRight)
        {
            acceleration =
                CarFollowingUtil.followLeaders(getCarFollowingModel(), bc, gtu.getSpeed(), sli, perception
                    .getLeaders(RelativeLane.RIGHT));
        }
        else if ((vC > vR && vC > vL)
            || (!perception.getLeaders(RelativeLane.CURRENT).isEmpty() && perception.getLeaders(RelativeLane.CURRENT)
                .first().getDistance().lt(getCarFollowingModel().desiredHeadway(bc, gtu.getSpeed()))))
        {
            acceleration =
                CarFollowingUtil.followLeaders(getCarFollowingModel(), bc, gtu.getSpeed(), sli, perception
                    .getLeaders(RelativeLane.CURRENT));
        }
        else
        {
            GapInfo gapAdj;
            GapInfo gapFwd;
            GapInfo gapBck;
            if (vL > vR && vL > vC)
            {
                gapAdj = gapAdjL;
                gapFwd = gapFwdL;
                gapBck = gapBckL;
            }
            else
            {
                gapAdj = gapAdjR;
                gapFwd = gapFwdR;
                gapBck = gapBckR;
            }
            if (gapAdj.getUtility() > gapFwd.getUtility() && gapAdj.getUtility() > gapBck.getUtility())
            {
                // adjacent gap selected
                double eadj =
                    Toledo.RANDOM.nextGaussian() * bc.getParameter(ToledoLaneChangeParameters.SIGMA_ADJ)
                        * bc.getParameter(ToledoLaneChangeParameters.SIGMA_ADJ);
                acceleration =
                    new Acceleration(bc.getParameter(ToledoLaneChangeParameters.C_ADJ_ACC)
                        * (bc.getParameter(ToledoLaneChangeParameters.BETA_DP) * gapAdj.getLength().si - gapAdj
                            .getDistance().si) + eadj, AccelerationUnit.SI);
            }
            else if (gapFwd.getUtility() > gapAdj.getUtility() && gapFwd.getUtility() > gapBck.getUtility())
            {
                // forward gap selected
                Length desiredPosition =
                    new Length(gapFwd.getDistance().si + bc.getParameter(ToledoLaneChangeParameters.BETA_DP)
                        * gapFwd.getLength().si, LengthUnit.SI);
                double deltaV =
                    gapFwd.getSpeed().si > gtu.getSpeed().si ? bc.getParameter(ToledoLaneChangeParameters.LAMBDA_FWD_POS)
                        * (gapFwd.getSpeed().si - gtu.getSpeed().si) : bc
                        .getParameter(ToledoLaneChangeParameters.LAMBDA_FWD_NEG)
                        * (gtu.getSpeed().si - gapFwd.getSpeed().si);
                double efwd =
                    Toledo.RANDOM.nextGaussian() * bc.getParameter(ToledoLaneChangeParameters.SIGMA_FWD)
                        * bc.getParameter(ToledoLaneChangeParameters.SIGMA_FWD);
                acceleration =
                    new Acceleration(bc.getParameter(ToledoLaneChangeParameters.C_FWD_ACC)
                        * Math.pow(desiredPosition.si, bc.getParameter(ToledoLaneChangeParameters.BETA_FWD))
                        * Math.exp(deltaV) + efwd, AccelerationUnit.SI);
            }
            else
            {
                // backward gap selected
                Length desiredPosition =
                    new Length(gapBck.getDistance().si + (1 - bc.getParameter(ToledoLaneChangeParameters.BETA_DP))
                        * gapBck.getLength().si, LengthUnit.SI);
                double deltaV =
                    gapBck.getSpeed().si > gtu.getSpeed().si ? bc.getParameter(ToledoLaneChangeParameters.LAMBDA_BCK_POS)
                        * (gapBck.getSpeed().si - gtu.getSpeed().si) : bc
                        .getParameter(ToledoLaneChangeParameters.LAMBDA_BCK_NEG)
                        * (gtu.getSpeed().si - gapBck.getSpeed().si);
                double ebck =
                    Toledo.RANDOM.nextGaussian() * bc.getParameter(ToledoLaneChangeParameters.SIGMA_BCK)
                        * bc.getParameter(ToledoLaneChangeParameters.SIGMA_BCK);
                acceleration =
                    new Acceleration(bc.getParameter(ToledoLaneChangeParameters.C_BCK_ACC)
                        * Math.pow(desiredPosition.si, bc.getParameter(ToledoLaneChangeParameters.BETA_BCK))
                        * Math.exp(deltaV) + ebck, AccelerationUnit.SI);
            }
        }
        return null;
    }

    /**
     * Returns info regarding a gap.
     * @param gtu GTU
     * @param bc behavioral characteristics
     * @param perception perception
     * @param gap gap
     * @param lane lane
     * @return utility of gap
     * @throws ParameterException if parameter is not given
     */
    private GapInfo getGapInfo(final GTU gtu, final BehavioralCharacteristics bc, final PerceivedSurroundings perception,
        final Gap gap, final RelativeLane lane) throws ParameterException
    {

        // capture no leaders/follower cases for forward and backward gaps
        if (gap.equals(Gap.FORWARD) && perception.getLeaders(lane).isEmpty())
        {
            // no leaders
            return new GapInfo(Double.NEGATIVE_INFINITY, null, null, null);
        }
        if (gap.equals(Gap.BACKWARD) && perception.getFollowers(lane).isEmpty())
        {
            // no followers
            return new GapInfo(Double.NEGATIVE_INFINITY, null, null, null);
        }

        double constant; // utility function: constant
        double alpha; // utility function: alpha parameter
        Length distanceToGap; // utility function: distance to gap
        int deltaFrontVehicle; // utility function: dummy whether the gap is limited by vehicle in front
        Length putativeLength; // acceleration model: gap length, not affected by vehicle in front
        Length putativeDistance; // acceleration model: distance to gap, different from 'distanceToGap' in utility function
        Speed putativeSpeed; // acceleration model: speed of putative follower or leader
        Length leaderDist; // leader of (effective) gap
        Speed leaderSpeed;
        Length followerDist; // follower of (effective) gap
        Speed followerSpeed;

        if (gap.equals(Gap.FORWARD))
        {
            constant = bc.getParameter(ToledoLaneChangeParameters.C_FWD_TG);
            alpha = 0;
            if (perception.getLeaders(lane).size() > 1)
            {
                // two leaders
                Iterator<AbstractHeadwayGTU> it = perception.getLeaders(lane).iterator();
                it.next();
                AbstractHeadwayGTU leader = it.next();
                leaderDist = leader.getDistance();
                leaderSpeed = leader.getSpeed();
                putativeLength =
                    leaderDist.minus(perception.getLeaders(lane).first().getDistance()).minus(
                        perception.getLeaders(lane).first().getLength());
            }
            else
            {
                // TODO infinite -> some limited space, speed also
                // one leader
                leaderDist = new Length(Double.POSITIVE_INFINITY, LengthUnit.SI);
                leaderSpeed = new Speed(Double.POSITIVE_INFINITY, SpeedUnit.SI);
                putativeLength = leaderDist;
            }
            // distance to nose, so add length
            followerDist =
                perception.getLeaders(lane).first().getDistance().plus(perception.getLeaders(lane).first().getLength());
            followerSpeed = perception.getLeaders(lane).first().getSpeed();
            distanceToGap = followerDist; // same as distance to nose of first leader
            putativeDistance = distanceToGap;
            putativeSpeed = followerSpeed;
        }
        else if (gap.equals(Gap.ADJACENT))
        {
            constant = 0;
            alpha = bc.getParameter(ToledoLaneChangeParameters.ALPHA_ADJ);
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
                followerDist = perception.getFollowers(lane).first().getDistance().plus(gtu.getLength());
                followerSpeed = perception.getFollowers(lane).first().getSpeed();
                putativeDistance = followerDist;
            }
            else
            {
                followerDist = new Length(Double.NEGATIVE_INFINITY, LengthUnit.SI);
                followerSpeed = new Speed(Double.NEGATIVE_INFINITY, SpeedUnit.SI);
                putativeDistance = new Length(Double.POSITIVE_INFINITY, LengthUnit.SI);
            }
            putativeSpeed = null;
            putativeLength = leaderDist.plus(followerDist).plus(gtu.getLength());
        }
        else
        {
            constant = bc.getParameter(ToledoLaneChangeParameters.C_BCK_TG);
            alpha = bc.getParameter(ToledoLaneChangeParameters.ALPHA_BCK);
            deltaFrontVehicle = 0;
            if (perception.getFollowers(lane).size() > 1)
            {
                // two followers
                Iterator<AbstractHeadwayGTU> it = perception.getFollowers(lane).iterator();
                it.next();
                AbstractHeadwayGTU follower = it.next();
                followerDist = follower.getDistance();
                followerSpeed = follower.getSpeed();
                putativeLength =
                    followerDist.minus(perception.getFollowers(lane).first().getDistance()).minus(
                        perception.getFollowers(lane).first().getLength());
            }
            else
            {
                // one follower
                followerDist = new Length(Double.NEGATIVE_INFINITY, LengthUnit.SI);
                followerSpeed = new Speed(Double.NEGATIVE_INFINITY, SpeedUnit.SI);
                putativeLength = followerDist;
            }
            // add vehicle length to get distance to tail of 1st follower (and then whole negative)
            leaderDist =
                perception.getFollowers(lane).first().getDistance().plus(perception.getLeaders(lane).first().getLength());
            leaderSpeed = perception.getFollowers(lane).first().getSpeed();
            distanceToGap = perception.getFollowers(lane).first().getDistance().plus(gtu.getLength()); // own nose to nose
            putativeDistance = distanceToGap.plus(perception.getFollowers(lane).first().getLength());
            putativeSpeed = leaderSpeed;
        }

        // limit by leader in current lane
        if (!gap.equals(Gap.BACKWARD) && !perception.getLeaders(RelativeLane.CURRENT).isEmpty()
            && perception.getLeaders(RelativeLane.CURRENT).first().getDistance().lt(leaderDist))
        {
            leaderDist = perception.getLeaders(RelativeLane.CURRENT).first().getDistance();
            leaderSpeed = perception.getLeaders(RelativeLane.CURRENT).first().getSpeed();
            deltaFrontVehicle = 1;
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
        double util = constant 
                + bc.getParameter(ToledoLaneChangeParameters.BETA_DTG) * distanceToGap.si
                + bc.getParameter(ToledoLaneChangeParameters.BETA_EG) * effectiveGap.si
                + bc.getParameter(ToledoLaneChangeParameters.BETA_FV) * deltaFrontVehicle
                + bc.getParameter(ToledoLaneChangeParameters.BETA_RGS) * dV.si
                + alpha * bc.getParameter(ToledoLaneChangeParameters.ERROR_TERM);
        // {@formatter:on}
        return new GapInfo(util, putativeLength, putativeDistance, putativeSpeed);

    }

    /**
     * Returns info regarding gap-acceptance.
     * @param gtu GTU
     * @param bc behavioral characteristics
     * @param perception perception
     * @param emuTg emu from target gap model
     * @param eLead lead error
     * @param eLag lag error
     * @param lane lane to evaluate
     * @return info regarding gap-acceptance
     * @throws ParameterException if parameter is not defined
     */
    private GapAcceptanceInfo getGapAcceptanceInfo(final GTU gtu, final BehavioralCharacteristics bc,
        final PerceivedSurroundings perception, final double emuTg, final double eLead, final double eLag,
        final RelativeLane lane) throws ParameterException
    {

        // get lead and lag speed difference and distance
        Speed dVLead;
        Length sLead;
        if (!perception.getLeaders(lane).isEmpty())
        {
            dVLead = perception.getLeaders(lane).first().getSpeed().minus(gtu.getSpeed());
            sLead = perception.getLeaders(lane).first().getDistance();
        }
        else
        {
            dVLead = Speed.ZERO;
            sLead = new Length(Double.POSITIVE_INFINITY, LengthUnit.SI);
        }
        Speed dVLag;
        Length sLag;
        if (!perception.getFollowers(lane).isEmpty())
        {
            dVLag = perception.getFollowers(lane).first().getSpeed().minus(gtu.getSpeed());
            sLag = perception.getFollowers(lane).first().getDistance();
        }
        else
        {
            dVLag = Speed.ZERO;
            sLag = new Length(Double.POSITIVE_INFINITY, LengthUnit.SI);
        }

        // critical gaps
        // {@formatter:off}
        Length gLead = new Length(Math.exp(bc.getParameter(ToledoLaneChangeParameters.C_LEAD) 
            + bc.getParameter(ToledoLaneChangeParameters.BETA_POS_LEAD) * Speed.max(dVLead, Speed.ZERO).si
            + bc.getParameter(ToledoLaneChangeParameters.BETA_NEG_LEAD) * Speed.min(dVLead, Speed.ZERO).si
            + bc.getParameter(ToledoLaneChangeParameters.BETA_EMU_LEAD) * emuTg 
            + bc.getParameter(ToledoLaneChangeParameters.ALPHA_TL_LEAD) * bc.getParameter(ToledoLaneChangeParameters.ERROR_TERM)
            + eLead), LengthUnit.SI);
        Length gLag = new Length(Math.exp(bc.getParameter(ToledoLaneChangeParameters.C_LAG) 
            + bc.getParameter(ToledoLaneChangeParameters.BETA_POS_LAG) * Speed.max(dVLag, Speed.ZERO).si
            + bc.getParameter(ToledoLaneChangeParameters.BETA_EMU_LAG) * emuTg 
            + bc.getParameter(ToledoLaneChangeParameters.ALPHA_TL_LAG) * bc.getParameter(ToledoLaneChangeParameters.ERROR_TERM)
            + eLag), LengthUnit.SI);
        // {@formatter:on}

        // lead and lag utilities
        double vLead =
            Math.log(gLead.si)
                - (bc.getParameter(ToledoLaneChangeParameters.C_LEAD)
                    + bc.getParameter(ToledoLaneChangeParameters.BETA_POS_LEAD) * Speed.max(dVLead, Speed.ZERO).si
                    + bc.getParameter(ToledoLaneChangeParameters.BETA_NEG_LEAD) * Speed.min(dVLead, Speed.ZERO).si + bc
                    .getParameter(ToledoLaneChangeParameters.BETA_EMU_LEAD)
                    * emuTg);
        double vLag =
            Math.log(gLag.si)
                - (bc.getParameter(ToledoLaneChangeParameters.C_LAG)
                    + bc.getParameter(ToledoLaneChangeParameters.BETA_POS_LAG) * Speed.max(dVLag, Speed.ZERO).si + bc
                    .getParameter(ToledoLaneChangeParameters.BETA_EMU_LAG)
                    * emuTg);

        // gap acceptance emu
        double vRatLead = vLead / bc.getParameter(ToledoLaneChangeParameters.SIGMA_LEAD);
        double vRatLag = vLag / bc.getParameter(ToledoLaneChangeParameters.SIGMA_LAG);
        double emuGa =
            vLead * cumNormDist(vRatLead) + bc.getParameter(ToledoLaneChangeParameters.SIGMA_LEAD) * normDist(vRatLead)
                + vLag * cumNormDist(vRatLag) + bc.getParameter(ToledoLaneChangeParameters.SIGMA_LAG) * normDist(vRatLag);

        // return info
        return new GapAcceptanceInfo(emuGa, gLead.lt(sLead) && gLag.lt(sLag));

    }

    /**
     * Returns the utility of a lane.
     * @param gtu GTU
     * @param bc behavioral characteristics
     * @param perception perception
     * @param emuGa emu from gap acceptance model
     * @param sli speed limit info
     * @param lane lane to evaluate
     * @return utility of lane
     * @throws ParameterException if parameter is not defined
     */
    private double laneUtility(final GTU gtu, final BehavioralCharacteristics bc, final PerceivedSurroundings perception,
        final double emuGa, final SpeedLimitInfo sli, final RelativeLane lane) throws ParameterException
    {

        // get infrastructure info
        boolean takeNextOffRamp = true; // TODO from perception
        int deltaNextExit = takeNextOffRamp ? 1 : 0;

        Length dExit;
        if (!perception.getInfrastructureLaneChangeInfo(RelativeLane.CURRENT).isEmpty())
        {
            dExit = perception.getInfrastructureLaneChangeInfo(RelativeLane.CURRENT).first().getRemainingDistance();
        }
        else
        {
            dExit = new Length(Double.POSITIVE_INFINITY, LengthUnit.SI);
        }

        int[] delta = new int[3];
        int deltaAdd = 0;
        if (!perception.getInfrastructureLaneChangeInfo(lane).isEmpty())
        {
            InfrastructureLaneChangeInfo ilciLef = perception.getInfrastructureLaneChangeInfo(lane).first();
            if (ilciLef.getRequiredNumberOfLaneChanges() > 1 && ilciLef.getRequiredNumberOfLaneChanges() < 5)
            {
                deltaAdd = ilciLef.getRequiredNumberOfLaneChanges() - 2;
                delta[deltaAdd] = 1;
            }
        }

        // heavy neighbor
        Length leaderLength =
            !perception.getFirstLeaders(lane.getLateralDirectionality()).isEmpty() ? perception.getFirstLeaders(
                lane.getLateralDirectionality()).first().getLength() : Length.ZERO;
        Length followerLength =
            !perception.getFirstFollowers(lane.getLateralDirectionality()).isEmpty() ? perception.getFirstFollowers(
                lane.getLateralDirectionality()).first().getDistance() : Length.ZERO;
        int deltaHeavy = leaderLength.gt(MAX_LIGHT_VEHICLE_LENGTH) || followerLength.gt(MAX_LIGHT_VEHICLE_LENGTH) ? 1 : 0;

        // get density
        LinearDensity d = getDensityInLane(gtu, perception, lane);

        // tail gating
        int deltaTailgate = 0;
        if (lane.equals(RelativeLane.CURRENT) && !perception.getFollowers(RelativeLane.CURRENT).isEmpty()
            && perception.getFollowers(RelativeLane.CURRENT).first().getDistance().le(TAILGATE_LENGTH))
        {
            LinearDensity dL = getDensityInLane(gtu, perception, RelativeLane.LEFT);
            LinearDensity dR = getDensityInLane(gtu, perception, RelativeLane.RIGHT);
            LinearDensity dRoad = new LinearDensity((dL.si + d.si + dR.si) / 3, LinearDensityUnit.SI);
            if (dRoad.le(LOS_DENSITY))
            {
                deltaTailgate = 1;
            }
        }

        // right most
        // TODO definition of 'right most lane' does not account for ramps and weaving sections
        int deltaRightMost = 0;
        if (lane.equals(RelativeLane.CURRENT) && !perception.getCurrentCrossSection().contains(RelativeLane.RIGHT))
        {
            deltaRightMost = 1;
        }
        else if (lane.equals(RelativeLane.RIGHT) && perception.getCurrentCrossSection().contains(RelativeLane.RIGHT)
            && !perception.getCurrentCrossSection().contains(RelativeLane.SECOND_RIGHT))
        {
            deltaRightMost = 1;
        }

        // current lane traffic
        Speed vFront;
        Length sFront;
        if (lane.equals(RelativeLane.CURRENT))
        {
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
        }
        else
        {
            vFront = Speed.ZERO;
            sFront = Length.ZERO;
        }

        // target lane utility
        double constant = 0;
        double error = 0;
        if (lane.equals(RelativeLane.CURRENT))
        {
            constant = bc.getParameter(ToledoLaneChangeParameters.C_CL);
            error =
                bc.getParameter(ToledoLaneChangeParameters.ALPHA_CL)
                    * bc.getParameter(ToledoLaneChangeParameters.ERROR_TERM);
        }
        else if (lane.equals(RelativeLane.RIGHT))
        {
            constant = bc.getParameter(ToledoLaneChangeParameters.C_RL);
            error =
                bc.getParameter(ToledoLaneChangeParameters.ALPHA_RL)
                    * bc.getParameter(ToledoLaneChangeParameters.ERROR_TERM);
        }
        // {@formatter:off}
        return constant
                + bc.getParameter(ToledoLaneChangeParameters.BETA_RIGHT_MOST) * deltaRightMost // 0 for LEFT
                + bc.getParameter(ToledoLaneChangeParameters.BETA_VFRONT) * vFront.si // 0 for LEFT/RIGHT
                + bc.getParameter(ToledoLaneChangeParameters.BETA_SFRONT) * sFront.si // 0 for LEFT/RIGHT
                + bc.getParameter(ToledoLaneChangeParameters.BETA_DENSITY) * d.getInUnit(LinearDensityUnit.PER_KILOMETER)
                + bc.getParameter(ToledoLaneChangeParameters.BETA_HEAVY_NEIGHBOUR) * deltaHeavy
                + bc.getParameter(ToledoLaneChangeParameters.BETA_TAILGATE) * deltaTailgate
                + Math.pow(dExit.getInUnit(LengthUnit.KILOMETER), bc.getParameter(ToledoLaneChangeParameters.THETA_MLC)) 
                * (bc.getParameter(ToledoLaneChangeParameters.BETA1) * delta[0] 
                        + bc.getParameter(ToledoLaneChangeParameters.BETA2) * delta[1] 
                        + bc.getParameter(ToledoLaneChangeParameters.BETA3) * delta[2])
                + bc.getParameter(ToledoLaneChangeParameters.BETA_NEXT_EXIT) * deltaNextExit
                + bc.getParameter(ToledoLaneChangeParameters.BETA_ADD) * deltaAdd
                + bc.getParameter(ToledoLaneChangeParameters.BETA_EMU_GA) * emuGa // 0 for CURRENT (given correct input)
                + error; // 0 for LEFT
        // {@formatter:on}
    }

    /**
     * Returns the density in the given lane based on the following and leading vehicles.
     * @param gtu subject GTU
     * @param perception perception
     * @param lane lane to get density of
     * @return density in the given lane based on the following and leading vehicles
     */
    private LinearDensity getDensityInLane(final GTU gtu, final PerceivedSurroundings perception, final RelativeLane lane)
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
                d1 = d1.plus(perception.getFollowers(lane).last().getLength().divideBy(2.0));
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
                d2 = d2.plus(perception.getLeaders(lane).last().getLength().divideBy(2.0));
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

    /**
     * Returns the cumulative density function (CDF) at given value of the standard normal distribution.
     * @param x value
     * @return cumulative density function (CDF) at given value of the standard normal distribution
     */
    private static double cumNormDist(final double x)
    {
        return .5 * (1 + erf(x / Math.sqrt(2)));
    }

    /**
     * Error function approximation using Horner's method.
     * @param x value
     * @return error function approximation
     */
    private static double erf(final double x)
    {
        double t = 1.0 / (1.0 + 0.5 * Math.abs(x));
        // use Horner's method
        // {@formatter:off}
        double tau = t * Math.exp(-x * x - 1.26551223 + t * (1.00002368 + t * (0.37409196 
                + t * (0.09678418 + t * (0.18628806 + t * (0.27886807 + t * (-1.13520398 
                        + t * (1.48851587 + t * (-0.82215223 + t * (0.17087277))))))))));
        // {@formatter:on}
        return x >= 0 ? 1 - tau : tau - 1;
    }

    /**
     * Returns the probability density function (PDF) at given value of the standard normal distribution.
     * @param x value
     * @return probability density function (PDF) at given value of the standard normal distribution
     */
    private static double normDist(final double x)
    {
        return Math.exp(-x * x / 2) / Math.sqrt(2 * Math.PI);
    }

    /**
     * Gap indicator in adjacent lane.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 11, 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private enum Gap
    {
        /** Gap in front of leader in adjacent lane. */
        FORWARD,

        /** Gap between follower and leader in adjacent lane. */
        ADJACENT,

        /** Gap behind follower in adjacent lane. */
        BACKWARD;
    }

    /**
     * Contains info regarding an adjacent gap.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 11, 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private class GapInfo
    {

        /** Utility of the gap. */
        private final double utility;

        /** Length of the gap. */
        private final Length length;

        /** Distance towards the gap. */
        private final Length distance;

        /** Speed of the vehicle in front or behind the gap, always the closest. */
        private final Speed speed;

        /**
         * @param utility utility of gap
         * @param length length of the gap
         * @param distance distance towards the gap
         * @param speed speed of the vehicle in front or behind the gap, always the closest
         */
        GapInfo(final double utility, final Length length, final Length distance, final Speed speed)
        {
            this.utility = utility;
            this.length = length;
            this.distance = distance;
            this.speed = speed;
        }

        /**
         * @return utility
         */
        public final double getUtility()
        {
            return this.utility;
        }

        /**
         * @return length
         */
        public final Length getLength()
        {
            return this.length;
        }

        /**
         * @return distance
         */
        public final Length getDistance()
        {
            return this.distance;
        }

        /**
         * @return speed
         */
        public final Speed getSpeed()
        {
            return this.speed;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "GapAcceptanceInfo [utility = " + this.utility + ", length = " + this.length + ", distance = "
                + this.distance + ", speed = " + this.speed + "]";
        }

    }

    /**
     * Contains info regarding gap-acceptance.
     * <p>
     * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 11, 2016 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private class GapAcceptanceInfo
    {

        /** Emu value of gap-acceptance. */
        private final double emu;

        /** Whether gap is acceptable. */
        private final boolean acceptable;

        /**
         * @param emu emu
         * @param acceptable whether gap is acceptable
         */
        GapAcceptanceInfo(final double emu, final boolean acceptable)
        {
            this.emu = emu;
            this.acceptable = acceptable;
        }

        /**
         * @return emu
         */
        public final double getEmu()
        {
            return this.emu;
        }

        /**
         * @return acceptable
         */
        public final boolean isAcceptable()
        {
            return this.acceptable;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "GapAcceptanceInfo [emu = " + this.emu + ", acceptable = " + this.acceptable + "]";
        }

    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "Toledo tactical planner.";
    }

}
