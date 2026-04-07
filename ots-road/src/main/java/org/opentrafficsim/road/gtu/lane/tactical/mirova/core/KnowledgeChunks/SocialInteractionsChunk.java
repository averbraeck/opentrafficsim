package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.KnowledgeChunks;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.MirovaTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.Desire;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.context.EgoContext;

/**
 * KnowledgeChunk modeling social interactions based on Schakel et al. (2023).
 * <p>
 * This component belongs to <b>Layer 2 (Cognition / Motivation)</b> of the MiRoVA architecture.
 * It handles "Get out of the way" and "Stay out of the way" behaviors resulting from social pressure.
 * </p>
 * <p>
 * Copyright (c) 2025 Marvin Baumann / KIT. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 *
 * @author <a href="https://github.com/baumarv">Marvin Baumann</a>
 */
public class SocialInteractionsChunk extends KnowledgeChunk
{

    /**
     * Constructs a new SocialInteractionsChunk.
     *
     * @param vehicle the tactical planner governing the ego agent
     * @throws OperationalPlanException if chunk instantiation fails
     */
    public SocialInteractionsChunk(final MirovaTacticalPlanner vehicle) throws OperationalPlanException
    {
        super(vehicle);
    }

    /**
     * Determines if the social interactions logic is applicable.
     * <p>
     * Applicable only if the ego vehicle's speed is above a certain threshold (e.g., 20 m/s),
     * as social interaction pressures are typically negligible at low speeds.
     * </p>
     *
     * @return {@code true} if ego speed is above 20 m/s, {@code false} otherwise
     * @throws ParameterException if parameter evaluation fails
     */
    @Override
    public boolean isApplicable() throws ParameterException
    {
        try
        {
            // Applicable if speed is above threshold (e.g., 20 m/s)
            return getEgoPerception().getSpeed().gt(Speed.instantiateSI(20.0));
        }
        catch (Exception e)
        {
            return false;
        }
    }

    /**
     * Computes the desire to change lanes based on social pressure.
     *
     * @return a {@link Desire} object containing discretionary desire values for left and right directions
     * @throws ParameterException       if required parameters are missing
     * @throws OperationalPlanException if perception limits cannot be retrieved
     * @throws GtuException             if the GTU state is invalid
     * @throws NetworkException         if network data cannot be accessed
     */
    @Override
    public Desire computeDesire() throws ParameterException, OperationalPlanException, GtuException, NetworkException
    {
        Double rhoEgo = egoSocialPressure(RelativeLane.CURRENT);
        getMirovaTacticalPlanner().setSocioSpeedPressure(rhoEgo == null ? 0.0 : rhoEgo);

        Double dLeft = 0.0;
        Double dRight = 0.0;

        double socioSpeedSensitivity = getMirovaTacticalPlanner().getSocioSpeedSensitivity();

        // ---------------------------------------------------------
        // Stay out of the way (left lane)
        // ---------------------------------------------------------
        Double rhoPotentialFollower = followerSocialPressure(RelativeLane.LEFT);
        Double rhoEgoPotential = egoSocialPressure(RelativeLane.LEFT);

        try
        {
            if (getInfrastructurePerception().getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.LEFT).gt(getParameters().getParameter(ParameterTypes.LOOKAHEAD))
                && rhoPotentialFollower != null && rhoEgoPotential != null
                && rhoPotentialFollower * socioSpeedSensitivity > rhoEgoPotential)
            {
                // Suppress lane change to left with negative incentive
                dLeft = -rhoPotentialFollower * socioSpeedSensitivity;
            }
        }
        catch (Exception e)
        {
            // Ignore and keep dLeft 0.0
        }

        // ---------------------------------------------------------
        // Get out of the way (right lane)
        // ---------------------------------------------------------
        Double rhoActualFollower = followerSocialPressure(RelativeLane.CURRENT);
        Double rhoEgoRight = egoSocialPressure(RelativeLane.RIGHT);
        rhoEgoRight = rhoEgoRight == null ? 0.0 : rhoEgoRight;

        try
        {
            if (getInfrastructurePerception().getLegalLaneChangePossibility(RelativeLane.CURRENT, LateralDirectionality.RIGHT).gt(getParameters().getParameter(ParameterTypes.LOOKAHEAD))
                && rhoActualFollower != null && rhoEgo != null
                && rhoActualFollower * socioSpeedSensitivity > rhoEgo
                && rhoActualFollower > rhoEgoRight)
            {
                // Encourage lane change to right with positive incentive
                dRight = rhoActualFollower * socioSpeedSensitivity;
            }
        }
        catch (Exception e)
        {
            // Ignore and keep dRight 0.0
        }

        this.desire = new Desire(dLeft, dRight, false); // discretionary desire
        return this.desire;
    }

    /**
     * Calculates the social pressure exerted by a follower in the specified lane.
     *
     * @param lane the relative lane to check for followers
     * @return the computed social pressure, or {@code null} if no follower exists
     * @throws ParameterException if a parameter lookup fails
     */
    private Double followerSocialPressure(final RelativeLane lane) throws ParameterException
    {
        if (getNeighborsPerception().getFollowers(lane).isEmpty())
        {
            return null; // no follower
        }

        EgoContext egoContext = getMirovaTacticalPlanner().getContext(EgoContext.class);
        Speed vGain = getMirovaTacticalPlanner().getVGain();
        Speed vLeader = egoContext.getEgoSpeed();

        HeadwayGtu follower = getNeighborsPerception().getFollowers(lane).first();
        Speed followerDesiredSpeed = follower.getDesiredSpeed();
        Length headway = follower.getDistance();
        Length followerLookahead = follower.getParameters().getParameter(ParameterTypes.LOOKAHEAD);

        return socialPressure(followerDesiredSpeed, vLeader, vGain, headway, followerLookahead);
    }

    /**
     * Calculates the social pressure experienced by the ego vehicle from a leader in the specified lane.
     *
     * @param lane the relative lane to check for leaders
     * @return the computed social pressure, or {@code null} if no leader exists
     * @throws ParameterException if a parameter lookup fails
     */
    private Double egoSocialPressure(final RelativeLane lane) throws ParameterException
    {
        if (getNeighborsPerception().getLeaders(lane).isEmpty())
        {
            return null; // no leader
        }

        Speed vGain = getMirovaTacticalPlanner().getVGain();
        Speed followerDesiredSpeed = getMirovaTacticalPlanner().getGtu().getDesiredSpeed();
        Length followerLookahead = getParameters().getParameter(ParameterTypes.LOOKAHEAD);

        HeadwayGtu leader = getNeighborsPerception().getLeaders(lane).first();
        Speed vLeader = leader.getSpeed();      // same-lane leader
        Length headway = leader.getDistance();  // same-lane spacing

        return socialPressure(followerDesiredSpeed, vLeader, vGain, headway, followerLookahead);
    }

    /**
     * Returns a normalized social pressure, equal to (vDesired - vLead) / vGain.
     * * @param followerDesiredSpeed desired speed of the follower
     * @param leaderSpeed          actual speed of the leader
     * @param vGain                speed gain sensitivity parameter
     * @param headway              distance headway to the leader
     * @param followerLookahead    anticipation distance of the follower
     * @return normalized social pressure
     */
    static double socialPressure(final Speed followerDesiredSpeed, final Speed leaderSpeed,
            final Speed vGain, final Length headway, final Length followerLookahead)
    {
        double dv = followerDesiredSpeed.si - leaderSpeed.si;
        // Larger headway may happen due to perception errors
        if (dv < 0 || headway.gt(followerLookahead))
        {
            return 0.0;
        }
        return 1.0 - Math.exp(-(dv / vGain.si) * (1.0 - (headway.si / followerLookahead.si)));
    }
}