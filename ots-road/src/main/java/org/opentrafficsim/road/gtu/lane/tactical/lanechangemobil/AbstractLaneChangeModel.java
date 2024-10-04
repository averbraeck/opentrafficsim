package org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil;

import java.util.Collection;
import java.util.Map;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.base.DoubleScalar;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeAcceleration;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.DefaultSimplePerception;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.AbstractGtuFollowingModelMobil;
import org.opentrafficsim.road.gtu.lane.tactical.following.DualAccelerationStep;
import org.opentrafficsim.road.gtu.lane.tactical.following.GtuFollowingModelOld;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Common code for a family of lane change models like in M. Treiber and A. Kesting <i>Traffic Flow Dynamics</i>,
 * Springer-Verlag Berlin Heidelberg 2013, pp 239-244.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public abstract class AbstractLaneChangeModel implements LaneChangeModel
{

    /** Look ahead parameter type. */
    protected static final ParameterTypeLength LOOKAHEAD = ParameterTypes.LOOKAHEAD;

    /** Comfortable deceleration parameter type. */
    protected static final ParameterTypeAcceleration B = ParameterTypes.B;

    /** Attempt to overcome rounding errors. */
    private static Acceleration extraThreshold = new Acceleration(0.000001, AccelerationUnit.SI);

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:parameternumber")
    @Override
    public final LaneMovementStep computeLaneChangeAndAcceleration(final LaneBasedGtu gtu,
            final Collection<Headway> sameLaneGTUs, final Collection<Headway> preferredLaneGTUs,
            final Collection<Headway> nonPreferredLaneGTUs, final Speed speedLimit,
            final Acceleration preferredLaneRouteIncentive, final Acceleration laneChangeThreshold,
            final Acceleration nonPreferredLaneRouteIncentive) throws ParameterException, OperationalPlanException
    {
        try
        {
            LanePerception perception = gtu.getTacticalPlanner().getPerception();
            Length headway = gtu.getParameters().getParameter(LOOKAHEAD);
            Map<Lane, Length> positions = gtu.positions(RelativePosition.REFERENCE_POSITION);
            Lane lane = positions.keySet().iterator().next();
            Length longitudinalPosition = positions.get(lane);
            // TODO make this driving side dependent; i.e. implement a general way to figure out on which side of the
            // road cars are supposed to drive
            final LateralDirectionality preferred = LateralDirectionality.RIGHT;
            final LateralDirectionality nonPreferred = LateralDirectionality.LEFT;
            DefaultSimplePerception simplePerception = perception.getPerceptionCategory(DefaultSimplePerception.class);
            simplePerception.updateAccessibleAdjacentLanes();
            Lane nonPreferredLane = simplePerception.bestAccessibleAdjacentLane(lane, nonPreferred, longitudinalPosition);
            Lane preferredLane = simplePerception.bestAccessibleAdjacentLane(lane, preferred, longitudinalPosition);
            AbstractLaneBasedTacticalPlanner albtp = (AbstractLaneBasedTacticalPlanner) gtu.getTacticalPlanner();
            if (null == albtp)
            {
                throw new NullPointerException(gtu + " returns null for its tactical planner");
            }
            GtuFollowingModelOld gtuFollowingModel = (GtuFollowingModelOld) albtp.getCarFollowingModel();
            if (null == gtuFollowingModel)
            {
                throw new NullPointerException(gtu + " has null GtuFollowingModel");
            }
            DualAccelerationStep straightAccelerationSteps =
                    gtuFollowingModel.computeDualAccelerationStep(gtu, sameLaneGTUs, headway, speedLimit);
            if (straightAccelerationSteps.getLeaderAcceleration().getSI() < -9999)
            {
                System.out.println("Problem");
                gtuFollowingModel.computeDualAccelerationStep(gtu, sameLaneGTUs, headway, speedLimit);
            }
            Acceleration straightA = applyDriverPersonality(straightAccelerationSteps).plus(laneChangeThreshold);
            DualAccelerationStep nonPreferredAccelerationSteps = null == nonPreferredLane ? null
                    : gtuFollowingModel.computeDualAccelerationStep(gtu, nonPreferredLaneGTUs, headway, speedLimit);
            if (null != nonPreferredAccelerationSteps && nonPreferredAccelerationSteps.getFollowerAcceleration()
                    .getSI() < -gtu.getParameters().getParameter(B).getSI())
            {
                nonPreferredAccelerationSteps = AbstractGtuFollowingModelMobil.TOODANGEROUS;
            }
            Acceleration nonPreferredA =
                    null == nonPreferredLane ? null : applyDriverPersonality(nonPreferredAccelerationSteps);
            DualAccelerationStep preferredAccelerationSteps = null == preferredLane ? null
                    : gtuFollowingModel.computeDualAccelerationStep(gtu, preferredLaneGTUs, headway, speedLimit);
            if (null != preferredAccelerationSteps && preferredAccelerationSteps.getFollowerAcceleration()
                    .getSI() < -gtu.getParameters().getParameter(B).getSI())
            {
                preferredAccelerationSteps = AbstractGtuFollowingModelMobil.TOODANGEROUS;
            }
            Acceleration preferredA = null == preferredLane ? null : applyDriverPersonality(preferredAccelerationSteps);
            if (null == preferredA)
            {
                // Lane change to the preferred lane is not possible
                if (null == nonPreferredA)
                {
                    // No lane change possible; this is definitely the easy case
                    return new LaneMovementStep(straightAccelerationSteps.getLeaderAccelerationStep(), null);
                }
                else
                {
                    // Merge to nonPreferredLane is possible; merge to preferredLane is NOT possible
                    if (DoubleScalar.plus(nonPreferredA, nonPreferredLaneRouteIncentive).gt(straightA.plus(extraThreshold)))
                    {
                        // Merge to the nonPreferred lane; i.e. start an overtaking procedure
                        return new LaneMovementStep(nonPreferredAccelerationSteps.getLeaderAccelerationStep(), nonPreferred);
                    }
                    else
                    {
                        // Stay in the current lane
                        return new LaneMovementStep(straightAccelerationSteps.getLeaderAccelerationStep(), null);
                    }
                }
            }
            // A merge to the preferredLane is possible
            if (null == nonPreferredA)
            {
                // Merge to preferredLane is possible; merge to nonPreferred lane is NOT possible
                if (DoubleScalar.plus(preferredA, preferredLaneRouteIncentive).plus(extraThreshold).ge(straightA))
                {
                    // Merge to the preferred lane; i.e. finish (or cancel) an overtaking procedure
                    return new LaneMovementStep(preferredAccelerationSteps.getLeaderAccelerationStep(), preferred);
                }
                else
                {
                    // Stay in current lane
                    return new LaneMovementStep(straightAccelerationSteps.getLeaderAccelerationStep(), null);
                }
            }
            // All merges are possible
            Acceleration preferredAttractiveness =
                    preferredA.plus(preferredLaneRouteIncentive).minus(straightA).plus(extraThreshold);
            Acceleration nonPreferredAttractiveness =
                    nonPreferredA.plus(nonPreferredLaneRouteIncentive).minus(straightA).minus(extraThreshold);
            if (preferredAttractiveness.getSI() < 0 && nonPreferredAttractiveness.getSI() <= 0)
            {
                // Stay in current lane
                return new LaneMovementStep(straightAccelerationSteps.getLeaderAccelerationStep(), null);

            }
            if (preferredAttractiveness.getSI() >= 0 && preferredAttractiveness.gt(nonPreferredAttractiveness))
            {
                // Merge to the preferred lane; i.e. finish (or cancel) an overtaking procedure
                return new LaneMovementStep(preferredAccelerationSteps.getLeaderAccelerationStep(), preferred);
            }
            // Merge to the adjacent nonPreferred lane; i.e. start an overtaking procedure
            return new LaneMovementStep(nonPreferredAccelerationSteps.getLeaderAccelerationStep(), nonPreferred);
        }
        catch (GtuException exception)
        {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Return the weighted acceleration as described by the personality. This incorporates the personality of the driver to the
     * lane change decisions.
     * @param accelerationSteps the DualAccelerationStep that contains the AccelerationStep that the reference GTU will make and
     *            the AccelerationStep that the (new) follower GTU will make
     * @return the acceleration that the personality of the driver uses (in a comparison to a similarly computed acceleration in
     *         the non-, or different-lane-changed state) to decide if a lane change should be performed
     */
    public abstract Acceleration applyDriverPersonality(DualAccelerationStep accelerationSteps);

}
