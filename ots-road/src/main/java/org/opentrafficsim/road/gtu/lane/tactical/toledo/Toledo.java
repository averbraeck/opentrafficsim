package org.opentrafficsim.road.gtu.lane.tactical.toledo;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Random;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.LinearDensityUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.LinearDensity;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.geometry.DirectedPoint;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.CategoricalLanePerception;
import org.opentrafficsim.road.gtu.lane.perception.InfrastructureLaneChangeInfo;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.DirectNeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.HeadwayGtuType;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneChange;
import org.opentrafficsim.road.gtu.lane.plan.operational.LaneOperationalPlanBuilder;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.CarFollowingModel;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;
import org.opentrafficsim.road.network.speed.SpeedLimitProspect;

/**
 * Implementation of the model of Toledo (2003).<br>
 * <br>
 * Tomer Toledo (2003) "Integrated Driving Behavior Modeling", Massachusetts Institute of Technology.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */

public class Toledo extends AbstractLaneBasedTacticalPlanner
{

    /** */
    private static final long serialVersionUID = 20160711L;

    /** Look ahead parameter type. */
    protected static final ParameterTypeLength LOOKAHEAD = ParameterTypes.LOOKAHEAD;

    /** Defines light vs heavy vehicles. */
    static final Length MAX_LIGHT_VEHICLE_LENGTH = new Length(9.14, LengthUnit.METER);

    /** Distance defining tail gating. */
    static final Length TAILGATE_LENGTH = new Length(10, LengthUnit.METER);

    /** Density for tail gating (Level Of Service (LOS) A, B or C). */
    static final LinearDensity LOS_DENSITY = new LinearDensity(16, LinearDensityUnit.PER_KILOMETER);

    /** Random number generator. */
    public static final Random RANDOM = new Random();

    /** Lane change status. */
    private final LaneChange laneChange;

    /**
     * Constructor.
     * @param carFollowingModel CarFollowingModel; Car-following model.
     * @param gtu LaneBasedGtu; GTU
     */
    public Toledo(final CarFollowingModel carFollowingModel, final LaneBasedGtu gtu)
    {
        super(carFollowingModel, gtu, new CategoricalLanePerception(gtu));
        this.laneChange = Try.assign(() -> new LaneChange(gtu), "Parameter LCDUR is required.", GtuException.class);
        getPerception().addPerceptionCategory(new ToledoPerception(getPerception()));
        getPerception().addPerceptionCategory(new DirectNeighborsPerception(getPerception(), HeadwayGtuType.WRAP));
    }

    /** {@inheritDoc} */
    @Override
    public final OperationalPlan generateOperationalPlan(final Time startTime, final DirectedPoint locationAtStartTime)
            throws OperationalPlanException, GtuException, NetworkException, ParameterException
    {

        // obtain objects to get info
        LanePerception perception = getPerception();
        SpeedLimitProspect slp =
                perception.getPerceptionCategory(ToledoPerception.class).getSpeedLimitProspect(RelativeLane.CURRENT);
        SpeedLimitInfo sli = slp.getSpeedLimitInfo(Length.ZERO);
        Parameters params = getGtu().getParameters();

        Acceleration acceleration = null;

        // if (this.laneChangeDirectionality == null)
        LateralDirectionality initiatedLaneChange;
        NeighborsPerception neighbors = perception.getPerceptionCategory(NeighborsPerception.class);
        if (!this.laneChange.isChangingLane())
        {
            // not changing lane

            // 3rd layer of model: Target gap model
            // TODO vehicle not ahead and not backwards but completely adjacent
            GapInfo gapFwdL = getGapInfo(params, perception, Gap.FORWARD, RelativeLane.LEFT);
            GapInfo gapAdjL = getGapInfo(params, perception, Gap.ADJACENT, RelativeLane.LEFT);
            GapInfo gapBckL = getGapInfo(params, perception, Gap.BACKWARD, RelativeLane.LEFT);
            GapInfo gapFwdR = getGapInfo(params, perception, Gap.FORWARD, RelativeLane.RIGHT);
            GapInfo gapAdjR = getGapInfo(params, perception, Gap.ADJACENT, RelativeLane.RIGHT);
            GapInfo gapBckR = getGapInfo(params, perception, Gap.BACKWARD, RelativeLane.RIGHT);
            double emuTgL =
                    Math.log(Math.exp(gapFwdL.getUtility()) + Math.exp(gapAdjL.getUtility()) + Math.exp(gapBckL.getUtility()));
            double emuTgR =
                    Math.log(Math.exp(gapFwdR.getUtility()) + Math.exp(gapAdjR.getUtility()) + Math.exp(gapBckR.getUtility()));

            // 2nd layer of model: Gap-acceptance model
            // gap acceptance random terms (variable over time, equal for left and right)
            double eLead = RANDOM.nextGaussian() * Math.pow(params.getParameter(ToledoLaneChangeParameters.SIGMA_LEAD), 2);
            double eLag = RANDOM.nextGaussian() * Math.pow(params.getParameter(ToledoLaneChangeParameters.SIGMA_LAG), 2);
            GapAcceptanceInfo gapAcceptL =
                    getGapAcceptanceInfo(getGtu(), params, perception, emuTgL, eLead, eLag, RelativeLane.LEFT);
            GapAcceptanceInfo gapAcceptR =
                    getGapAcceptanceInfo(getGtu(), params, perception, emuTgR, eLead, eLag, RelativeLane.RIGHT);

            // 1st layer of model: Target lane model
            double vL = laneUtility(getGtu(), params, perception, gapAcceptL.getEmu(), sli, RelativeLane.LEFT);
            double vC = laneUtility(getGtu(), params, perception, 0, sli, RelativeLane.CURRENT);
            double vR = laneUtility(getGtu(), params, perception, gapAcceptR.getEmu(), sli, RelativeLane.RIGHT);

            // change lane?
            if (vL > vR && vL > vC && gapAcceptL.isAcceptable())
            {
                initiatedLaneChange = LateralDirectionality.LEFT;
            }
            else if (vR > vL && vR > vC && gapAcceptR.isAcceptable())
            {
                initiatedLaneChange = LateralDirectionality.RIGHT;
            }
            else
            {
                initiatedLaneChange = LateralDirectionality.NONE;
            }

            // accelerate for gap selection
            if (initiatedLaneChange.isNone())
            {
                if ((vC > vR && vC > vL)
                        || (!neighbors.getLeaders(RelativeLane.CURRENT).isEmpty() && neighbors.getLeaders(RelativeLane.CURRENT)
                                .first().getDistance().lt(getCarFollowingModel().desiredHeadway(params, getGtu().getSpeed()))))
                {
                    acceleration = getCarFollowingModel().followingAcceleration(params, getGtu().getSpeed(), sli,
                            neighbors.getLeaders(RelativeLane.CURRENT));
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
                        double eadj = Toledo.RANDOM.nextGaussian() * params.getParameter(ToledoLaneChangeParameters.SIGMA_ADJ)
                                * params.getParameter(ToledoLaneChangeParameters.SIGMA_ADJ);
                        acceleration =
                                new Acceleration(
                                        params.getParameter(ToledoLaneChangeParameters.C_ADJ_ACC)
                                                * (params.getParameter(ToledoLaneChangeParameters.BETA_DP)
                                                        * gapAdj.getLength().si - gapAdj.getDistance().si)
                                                + eadj,
                                        AccelerationUnit.SI);
                    }
                    else if (gapFwd.getUtility() > gapAdj.getUtility() && gapFwd.getUtility() > gapBck.getUtility())
                    {
                        // forward gap selected
                        Length desiredPosition = new Length(
                                gapFwd.getDistance().si
                                        + params.getParameter(ToledoLaneChangeParameters.BETA_DP) * gapFwd.getLength().si,
                                LengthUnit.SI);
                        double deltaV = gapFwd.getSpeed().si > getGtu().getSpeed().si
                                ? params.getParameter(ToledoLaneChangeParameters.LAMBDA_FWD_POS)
                                        * (gapFwd.getSpeed().si - getGtu().getSpeed().si)
                                : params.getParameter(ToledoLaneChangeParameters.LAMBDA_FWD_NEG)
                                        * (getGtu().getSpeed().si - gapFwd.getSpeed().si);
                        double efwd = Toledo.RANDOM.nextGaussian() * params.getParameter(ToledoLaneChangeParameters.SIGMA_FWD)
                                * params.getParameter(ToledoLaneChangeParameters.SIGMA_FWD);
                        acceleration = new Acceleration(params.getParameter(ToledoLaneChangeParameters.C_FWD_ACC)
                                * Math.pow(desiredPosition.si, params.getParameter(ToledoLaneChangeParameters.BETA_FWD))
                                * Math.exp(deltaV) + efwd, AccelerationUnit.SI);
                    }
                    else
                    {
                        // backward gap selected
                        Length desiredPosition = new Length(
                                gapBck.getDistance().si
                                        + (1 - params.getParameter(ToledoLaneChangeParameters.BETA_DP)) * gapBck.getLength().si,
                                LengthUnit.SI);
                        double deltaV = gapBck.getSpeed().si > getGtu().getSpeed().si
                                ? params.getParameter(ToledoLaneChangeParameters.LAMBDA_BCK_POS)
                                        * (gapBck.getSpeed().si - getGtu().getSpeed().si)
                                : params.getParameter(ToledoLaneChangeParameters.LAMBDA_BCK_NEG)
                                        * (getGtu().getSpeed().si - gapBck.getSpeed().si);
                        double ebck = Toledo.RANDOM.nextGaussian() * params.getParameter(ToledoLaneChangeParameters.SIGMA_BCK)
                                * params.getParameter(ToledoLaneChangeParameters.SIGMA_BCK);
                        acceleration = new Acceleration(params.getParameter(ToledoLaneChangeParameters.C_BCK_ACC)
                                * Math.pow(desiredPosition.si, params.getParameter(ToledoLaneChangeParameters.BETA_BCK))
                                * Math.exp(deltaV) + ebck, AccelerationUnit.SI);
                    }
                }
            }
        }
        else
        {
            initiatedLaneChange = LateralDirectionality.NONE;
        }

        if (initiatedLaneChange.isLeft())
        {
            // changing left
            getCarFollowingModel().followingAcceleration(params, getGtu().getSpeed(), sli,
                    neighbors.getLeaders(RelativeLane.LEFT));
        }
        else if (initiatedLaneChange.isRight())
        {
            // changing right
            getCarFollowingModel().followingAcceleration(params, getGtu().getSpeed(), sli,
                    neighbors.getLeaders(RelativeLane.RIGHT));
        }

        if (acceleration == null)
        {
            throw new Error("Acceleration from toledo model is null.");
        }

        // operational plan
        if (initiatedLaneChange.isNone())
        {
            try
            {
                return LaneOperationalPlanBuilder.buildAccelerationPlan(getGtu(), startTime, getGtu().getSpeed(), acceleration,
                        params.getParameter(ToledoLaneChangeParameters.DT), false);
            }
            catch (OTSGeometryException exception)
            {
                throw new OperationalPlanException(exception);
            }
        }

        try
        {
            OperationalPlan plan = LaneOperationalPlanBuilder.buildAccelerationLaneChangePlan(getGtu(), initiatedLaneChange,
                    getGtu().getLocation(), startTime, getGtu().getSpeed(), acceleration,
                    params.getParameter(ToledoLaneChangeParameters.DT), this.laneChange);
            return plan;
        }
        catch (OTSGeometryException exception)
        {
            throw new OperationalPlanException(exception);
        }
    }

    /**
     * Returns info regarding a gap.
     * @param params Parameters; parameters
     * @param perception LanePerception; perception
     * @param gap Gap; gap
     * @param lane RelativeLane; lane
     * @return utility of gap
     * @throws ParameterException if parameter is not given
     * @throws OperationalPlanException perception exception
     */
    private GapInfo getGapInfo(final Parameters params, final LanePerception perception, final Gap gap, final RelativeLane lane)
            throws ParameterException, OperationalPlanException
    {

        // capture no leaders/follower cases for forward and backward gaps
        NeighborsPerception neighbors = perception.getPerceptionCategory(NeighborsPerception.class);
        if (gap.equals(Gap.FORWARD) && (neighbors.getLeaders(lane) == null || neighbors.getLeaders(lane).isEmpty()))
        {
            // no leaders
            return new GapInfo(Double.NEGATIVE_INFINITY, null, null, null);
        }
        if (gap.equals(Gap.BACKWARD) && (neighbors.getFollowers(lane) == null || neighbors.getFollowers(lane).isEmpty()))
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
            constant = params.getParameter(ToledoLaneChangeParameters.C_FWD_TG);
            alpha = 0;
            PerceptionCollectable<HeadwayGtu, LaneBasedGtu> itAble = neighbors.getLeaders(lane);
            HeadwayGtu second = null;
            if (!itAble.isEmpty())
            {
                Iterator<HeadwayGtu> it = itAble.iterator();
                it.next();
                second = it.next();
            }
            if (second != null)
            {
                // two leaders
                Iterator<HeadwayGtu> it = neighbors.getLeaders(lane).iterator();
                it.next();
                HeadwayGtu leader = it.next();
                leaderDist = leader.getDistance();
                leaderSpeed = leader.getSpeed();
                putativeLength = leaderDist.minus(neighbors.getLeaders(lane).first().getDistance())
                        .minus(neighbors.getLeaders(lane).first().getLength());
            }
            else
            {
                // TODO infinite -> some limited space, speed also
                // one leader
                leaderDist = Length.POSITIVE_INFINITY;
                leaderSpeed = Speed.POSITIVE_INFINITY;
                putativeLength = leaderDist;
            }
            // distance to nose, so add length
            followerDist =
                    neighbors.getLeaders(lane).first().getDistance().plus(neighbors.getLeaders(lane).first().getLength());
            followerSpeed = neighbors.getLeaders(lane).first().getSpeed();
            distanceToGap = followerDist; // same as distance to nose of first leader
            putativeDistance = distanceToGap;
            putativeSpeed = followerSpeed;
        }
        else if (gap.equals(Gap.ADJACENT))
        {
            constant = 0;
            alpha = params.getParameter(ToledoLaneChangeParameters.ALPHA_ADJ);
            distanceToGap = Length.ZERO;
            if (neighbors.getLeaders(lane) != null && !neighbors.getLeaders(lane).isEmpty())
            {
                leaderDist = neighbors.getLeaders(lane).first().getDistance();
                leaderSpeed = neighbors.getLeaders(lane).first().getSpeed();
            }
            else
            {
                leaderDist = Length.POS_MAXVALUE;
                leaderSpeed = Speed.POS_MAXVALUE;
            }
            if (neighbors.getFollowers(lane) != null && !neighbors.getFollowers(lane).isEmpty())
            {
                // plus own vehicle length for distance from nose of own vehicle (and then whole negative)
                followerDist = neighbors.getFollowers(lane).first().getDistance().plus(getGtu().getLength());
                followerSpeed = neighbors.getFollowers(lane).first().getSpeed();
                putativeDistance = followerDist;
            }
            else
            {
                followerDist = Length.NEG_MAXVALUE;
                followerSpeed = Speed.NEG_MAXVALUE;
                putativeDistance = Length.POS_MAXVALUE;
            }
            putativeSpeed = null;
            putativeLength = leaderDist.plus(followerDist).plus(getGtu().getLength());
        }
        else
        {
            constant = params.getParameter(ToledoLaneChangeParameters.C_BCK_TG);
            alpha = params.getParameter(ToledoLaneChangeParameters.ALPHA_BCK);
            deltaFrontVehicle = 0;
            PerceptionCollectable<HeadwayGtu, LaneBasedGtu> itAble = neighbors.getFollowers(lane);
            HeadwayGtu second = null;
            if (!itAble.isEmpty())
            {
                Iterator<HeadwayGtu> it = itAble.iterator();
                it.next();
                second = it.next();
            }
            if (second != null)
            {
                // two followers
                Iterator<HeadwayGtu> it = neighbors.getFollowers(lane).iterator();
                it.next();
                HeadwayGtu follower = it.next();
                followerDist = follower.getDistance();
                followerSpeed = follower.getSpeed();
                putativeLength = followerDist.minus(neighbors.getFollowers(lane).first().getDistance())
                        .minus(neighbors.getFollowers(lane).first().getLength());
            }
            else
            {
                // one follower
                followerDist = Length.NEGATIVE_INFINITY;
                followerSpeed = Speed.NEGATIVE_INFINITY;
                putativeLength = followerDist;
            }
            // add vehicle length to get distance to tail of 1st follower (and then whole negative)
            leaderDist =
                    neighbors.getFollowers(lane).first().getDistance().plus(neighbors.getLeaders(lane).first().getLength());
            leaderSpeed = neighbors.getFollowers(lane).first().getSpeed();
            distanceToGap = neighbors.getFollowers(lane).first().getDistance().plus(getGtu().getLength()); // own nose to nose
            putativeDistance = distanceToGap.plus(neighbors.getFollowers(lane).first().getLength());
            putativeSpeed = leaderSpeed;
        }

        // limit by leader in current lane
        if (!gap.equals(Gap.BACKWARD) && !neighbors.getLeaders(RelativeLane.CURRENT).isEmpty()
                && neighbors.getLeaders(RelativeLane.CURRENT).first().getDistance().lt(leaderDist))
        {
            leaderDist = neighbors.getLeaders(RelativeLane.CURRENT).first().getDistance();
            leaderSpeed = neighbors.getLeaders(RelativeLane.CURRENT).first().getSpeed();
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
                + params.getParameter(ToledoLaneChangeParameters.BETA_DTG) * distanceToGap.si
                + params.getParameter(ToledoLaneChangeParameters.BETA_EG) * effectiveGap.si
                + params.getParameter(ToledoLaneChangeParameters.BETA_FV) * deltaFrontVehicle
                + params.getParameter(ToledoLaneChangeParameters.BETA_RGS) * dV.si
                + alpha * params.getParameter(ToledoLaneChangeParameters.ERROR_TERM);
        // {@formatter:on}
        return new GapInfo(util, putativeLength, putativeDistance, putativeSpeed);

    }

    /**
     * Returns info regarding gap-acceptance.
     * @param gtu LaneBasedGtu; GTU
     * @param params Parameters; parameters
     * @param perception LanePerception; perception
     * @param emuTg double; emu from target gap model
     * @param eLead double; lead error
     * @param eLag double; lag error
     * @param lane RelativeLane; lane to evaluate
     * @return info regarding gap-acceptance
     * @throws ParameterException if parameter is not defined
     * @throws OperationalPlanException perception exception
     */
    private GapAcceptanceInfo getGapAcceptanceInfo(final LaneBasedGtu gtu, final Parameters params,
            final LanePerception perception, final double emuTg, final double eLead, final double eLag, final RelativeLane lane)
            throws ParameterException, OperationalPlanException
    {

        // get lead and lag utility and acceptance
        double vLead;
        double vLag;
        boolean acceptLead;
        boolean acceptLag;

        NeighborsPerception neighbors = perception.getPerceptionCategory(NeighborsPerception.class);
        if (neighbors.getLeaders(lane) != null && !neighbors.getLeaders(lane).isEmpty())
        {
            Speed dVLead = neighbors.getLeaders(lane).first().getSpeed().minus(gtu.getSpeed());
            Length sLead = neighbors.getLeaders(lane).first().getDistance();
            // critical gap
            Length gLead =
                    new Length(
                            Math.exp(params.getParameter(ToledoLaneChangeParameters.C_LEAD)
                                    + params.getParameter(ToledoLaneChangeParameters.BETA_POS_LEAD)
                                            * Speed.max(dVLead, Speed.ZERO).si
                                    + params.getParameter(ToledoLaneChangeParameters.BETA_NEG_LEAD)
                                            * Speed.min(dVLead, Speed.ZERO).si
                                    + params.getParameter(ToledoLaneChangeParameters.BETA_EMU_LEAD) * emuTg
                                    + params.getParameter(ToledoLaneChangeParameters.ALPHA_TL_LEAD)
                                            * params.getParameter(ToledoLaneChangeParameters.ERROR_TERM)
                                    + eLead),
                            LengthUnit.SI);
            vLead = Math.log(gLead.si) - (params.getParameter(ToledoLaneChangeParameters.C_LEAD)
                    + params.getParameter(ToledoLaneChangeParameters.BETA_POS_LEAD) * Speed.max(dVLead, Speed.ZERO).si
                    + params.getParameter(ToledoLaneChangeParameters.BETA_NEG_LEAD) * Speed.min(dVLead, Speed.ZERO).si
                    + params.getParameter(ToledoLaneChangeParameters.BETA_EMU_LEAD) * emuTg);
            acceptLead = gLead.lt(sLead);
        }
        else
        {
            vLead = 0;
            acceptLead = false;
        }

        if (neighbors.getFollowers(lane) != null && !neighbors.getFollowers(lane).isEmpty())
        {
            Speed dVLag = neighbors.getFollowers(lane).first().getSpeed().minus(gtu.getSpeed());
            Length sLag = neighbors.getFollowers(lane).first().getDistance();
            // critical gap
            Length gLag =
                    new Length(
                            Math.exp(params.getParameter(ToledoLaneChangeParameters.C_LAG)
                                    + params.getParameter(ToledoLaneChangeParameters.BETA_POS_LAG)
                                            * Speed.max(dVLag, Speed.ZERO).si
                                    + params.getParameter(ToledoLaneChangeParameters.BETA_EMU_LAG) * emuTg
                                    + params.getParameter(ToledoLaneChangeParameters.ALPHA_TL_LAG)
                                            * params.getParameter(ToledoLaneChangeParameters.ERROR_TERM)
                                    + eLag),
                            LengthUnit.SI);
            vLag = Math.log(gLag.si) - (params.getParameter(ToledoLaneChangeParameters.C_LAG)
                    + params.getParameter(ToledoLaneChangeParameters.BETA_POS_LAG) * Speed.max(dVLag, Speed.ZERO).si
                    + params.getParameter(ToledoLaneChangeParameters.BETA_EMU_LAG) * emuTg);
            acceptLag = gLag.lt(sLag);
        }
        else
        {
            vLag = 0;
            acceptLag = false;
        }

        // gap acceptance emu
        double vRatLead = vLead / params.getParameter(ToledoLaneChangeParameters.SIGMA_LEAD);
        double vRatLag = vLag / params.getParameter(ToledoLaneChangeParameters.SIGMA_LAG);
        double emuGa = vLead * cumNormDist(vRatLead)
                + params.getParameter(ToledoLaneChangeParameters.SIGMA_LEAD) * normDist(vRatLead) + vLag * cumNormDist(vRatLag)
                + params.getParameter(ToledoLaneChangeParameters.SIGMA_LAG) * normDist(vRatLag);

        // return info
        return new GapAcceptanceInfo(emuGa, acceptLead && acceptLag);

    }

    /**
     * Returns the utility of a lane.
     * @param gtu LaneBasedGtu; GTU
     * @param params Parameters; parameters
     * @param perception LanePerception; perception
     * @param emuGa double; emu from gap acceptance model
     * @param sli SpeedLimitInfo; speed limit info
     * @param lane RelativeLane; lane to evaluate
     * @return utility of lane
     * @throws ParameterException if parameter is not defined
     * @throws OperationalPlanException perception exception
     */
    private double laneUtility(final LaneBasedGtu gtu, final Parameters params, final LanePerception perception,
            final double emuGa, final SpeedLimitInfo sli, final RelativeLane lane)
            throws ParameterException, OperationalPlanException
    {

        ToledoPerception toledo = perception.getPerceptionCategory(ToledoPerception.class);
        if (!perception.getLaneStructure().getExtendedCrossSection().contains(lane))
        {
            return 0.0;
        }

        // get infrastructure info
        boolean takeNextOffRamp = false;
        for (InfrastructureLaneChangeInfoToledo info : toledo.getInfrastructureLaneChangeInfo(RelativeLane.CURRENT))
        {
            if (info.getSplitNumber() == 1)
            {
                takeNextOffRamp = true;
            }
        }
        int deltaNextExit = takeNextOffRamp ? 1 : 0;

        Length dExit;
        if (!toledo.getInfrastructureLaneChangeInfo(RelativeLane.CURRENT).isEmpty())
        {
            dExit = toledo.getInfrastructureLaneChangeInfo(RelativeLane.CURRENT).first().getRemainingDistance();
        }
        else
        {
            dExit = Length.POSITIVE_INFINITY;
        }

        int[] delta = new int[3];
        int deltaAdd = 0;
        if (!toledo.getInfrastructureLaneChangeInfo(lane).isEmpty())
        {
            InfrastructureLaneChangeInfo ilciLef = toledo.getInfrastructureLaneChangeInfo(lane).first();
            if (ilciLef.getRequiredNumberOfLaneChanges() > 1 && ilciLef.getRequiredNumberOfLaneChanges() < 5)
            {
                deltaAdd = ilciLef.getRequiredNumberOfLaneChanges() - 2;
                delta[deltaAdd] = 1;
            }
        }

        // heavy neighbor
        NeighborsPerception neighbors = perception.getPerceptionCategory(NeighborsPerception.class);
        Length leaderLength = !lane.isCurrent() && !neighbors.getFirstLeaders(lane.getLateralDirectionality()).isEmpty()
                ? neighbors.getFirstLeaders(lane.getLateralDirectionality()).first().getLength() : Length.ZERO;
        Length followerLength = !lane.isCurrent() && !neighbors.getFirstFollowers(lane.getLateralDirectionality()).isEmpty()
                ? neighbors.getFirstFollowers(lane.getLateralDirectionality()).first().getDistance() : Length.ZERO;
        int deltaHeavy = leaderLength.gt(MAX_LIGHT_VEHICLE_LENGTH) || followerLength.gt(MAX_LIGHT_VEHICLE_LENGTH) ? 1 : 0;

        // get density
        LinearDensity d = getDensityInLane(gtu, perception, lane);

        // tail gating
        int deltaTailgate = 0;
        if (lane.equals(RelativeLane.CURRENT) && !neighbors.getFollowers(RelativeLane.CURRENT).isEmpty()
                && neighbors.getFollowers(RelativeLane.CURRENT).first().getDistance().le(TAILGATE_LENGTH))
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
        if (lane.equals(RelativeLane.CURRENT) && !toledo.getCrossSection().contains(RelativeLane.RIGHT))
        {
            deltaRightMost = 1;
        }
        else if (lane.equals(RelativeLane.RIGHT) && toledo.getCrossSection().contains(RelativeLane.RIGHT)
                && !toledo.getCrossSection().contains(RelativeLane.SECOND_RIGHT))
        {
            deltaRightMost = 1;
        }

        // current lane traffic
        Speed vFront;
        Length sFront;
        if (lane.equals(RelativeLane.CURRENT))
        {
            if (!neighbors.getLeaders(RelativeLane.CURRENT).isEmpty())
            {
                vFront = neighbors.getLeaders(RelativeLane.CURRENT).first().getSpeed();
                sFront = neighbors.getLeaders(RelativeLane.CURRENT).first().getDistance();
            }
            else
            {
                vFront = getCarFollowingModel().desiredSpeed(params, sli);
                sFront = getCarFollowingModel().desiredHeadway(params, gtu.getSpeed());
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
            constant = params.getParameter(ToledoLaneChangeParameters.C_CL);
            error = params.getParameter(ToledoLaneChangeParameters.ALPHA_CL)
                    * params.getParameter(ToledoLaneChangeParameters.ERROR_TERM);
        }
        else if (lane.equals(RelativeLane.RIGHT))
        {
            constant = params.getParameter(ToledoLaneChangeParameters.C_RL);
            error = params.getParameter(ToledoLaneChangeParameters.ALPHA_RL)
                    * params.getParameter(ToledoLaneChangeParameters.ERROR_TERM);
        }
        // {@formatter:off}
        return constant
                + params.getParameter(ToledoLaneChangeParameters.BETA_RIGHT_MOST) * deltaRightMost // 0 for LEFT
                + params.getParameter(ToledoLaneChangeParameters.BETA_VFRONT) * vFront.si // 0 for LEFT/RIGHT
                + params.getParameter(ToledoLaneChangeParameters.BETA_SFRONT) * sFront.si // 0 for LEFT/RIGHT
                + params.getParameter(ToledoLaneChangeParameters.BETA_DENSITY) * d.getInUnit(LinearDensityUnit.PER_KILOMETER)
                + params.getParameter(ToledoLaneChangeParameters.BETA_HEAVY_NEIGHBOUR) * deltaHeavy
                + params.getParameter(ToledoLaneChangeParameters.BETA_TAILGATE) * deltaTailgate
                + Math.pow(dExit.getInUnit(LengthUnit.KILOMETER), params.getParameter(ToledoLaneChangeParameters.THETA_MLC)) 
                * (params.getParameter(ToledoLaneChangeParameters.BETA1) * delta[0] 
                        + params.getParameter(ToledoLaneChangeParameters.BETA2) * delta[1] 
                        + params.getParameter(ToledoLaneChangeParameters.BETA3) * delta[2])
                + params.getParameter(ToledoLaneChangeParameters.BETA_NEXT_EXIT) * deltaNextExit
                + params.getParameter(ToledoLaneChangeParameters.BETA_ADD) * deltaAdd
                + params.getParameter(ToledoLaneChangeParameters.BETA_EMU_GA) * emuGa // 0 for CURRENT (given correct input)
                + error; // 0 for LEFT
        // {@formatter:on}
    }

    /**
     * Returns the density in the given lane based on the following and leading vehicles.
     * @param gtu LaneBasedGtu; subject GTU
     * @param perception LanePerception; perception
     * @param lane RelativeLane; lane to get density of
     * @return density in the given lane based on the following and leading vehicles
     * @throws OperationalPlanException perception exception
     */
    private LinearDensity getDensityInLane(final LaneBasedGtu gtu, final LanePerception perception, final RelativeLane lane)
            throws OperationalPlanException
    {
        int nVehicles = 0;
        NeighborsPerception neighbors = perception.getPerceptionCategory(NeighborsPerception.class);
        Length up = Length.ZERO;
        Length down = Length.ZERO;
        for (HeadwayGtu neighbor : neighbors.getFollowers(lane))
        {
            nVehicles++;
            down = neighbor.getDistance();
        }
        for (HeadwayGtu neighbor : neighbors.getLeaders(lane))
        {
            nVehicles++;
            up = neighbor.getDistance();
        }
        if (nVehicles > 0)
        {
            return new LinearDensity(nVehicles / up.plus(down).getInUnit(LengthUnit.KILOMETER),
                    LinearDensityUnit.PER_KILOMETER);
        }
        return LinearDensity.ZERO;
    }

    /**
     * Returns the cumulative density function (CDF) at given value of the standard normal distribution.
     * @param x double; value
     * @return cumulative density function (CDF) at given value of the standard normal distribution
     */
    private static double cumNormDist(final double x)
    {
        return .5 * (1 + erf(x / Math.sqrt(2)));
    }

    /**
     * Error function approximation using Horner's method.
     * @param x double; value
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
     * @param x double; value
     * @return probability density function (PDF) at given value of the standard normal distribution
     */
    private static double normDist(final double x)
    {
        return Math.exp(-x * x / 2) / Math.sqrt(2 * Math.PI);
    }

    /**
     * Gap indicator in adjacent lane.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
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
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    private class GapInfo implements Serializable
    {

        /** */
        private static final long serialVersionUID = 20160811L;

        /** Utility of the gap. */
        private final double utility;

        /** Length of the gap. */
        private final Length length;

        /** Distance towards the gap. */
        private final Length distance;

        /** Speed of the vehicle in front or behind the gap, always the closest. */
        private final Speed speed;

        /**
         * @param utility double; utility of gap
         * @param length Length; length of the gap
         * @param distance Length; distance towards the gap
         * @param speed Speed; speed of the vehicle in front or behind the gap, always the closest
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
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    private class GapAcceptanceInfo implements Serializable
    {

        /** */
        private static final long serialVersionUID = 20160811L;

        /** Emu value of gap-acceptance. */
        private final double emu;

        /** Whether gap is acceptable. */
        private final boolean acceptable;

        /**
         * @param emu double; emu
         * @param acceptable boolean; whether gap is acceptable
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
