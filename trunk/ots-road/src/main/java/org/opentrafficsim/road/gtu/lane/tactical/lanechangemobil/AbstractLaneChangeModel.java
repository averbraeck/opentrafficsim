package org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil;

import java.util.Collection;
import java.util.Map;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlannerOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.AbstractGTUFollowingModelMobil;
import org.opentrafficsim.road.gtu.lane.tactical.following.DualAccelerationStep;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Common code for a family of lane change models like in M. Treiber and A. Kesting <i>Traffic Flow Dynamics</i>,
 * Springer-Verlag Berlin Heidelberg 2013, pp 239-244.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1401 $, $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, by $Author: averbraeck $,
 *          initial version 4 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractLaneChangeModel implements LaneChangeModel
{

    /** Attempt to overcome rounding errors. */
    private static Acceleration extraThreshold = new Acceleration(0.000001, AccelerationUnit.SI);

    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:parameternumber")
    @Override
    public final LaneMovementStep computeLaneChangeAndAcceleration(final LaneBasedGTU gtu,
            final Collection<Headway> sameLaneGTUs, final Collection<Headway> preferredLaneGTUs,
            final Collection<Headway> nonPreferredLaneGTUs, final Speed speedLimit,
            final Acceleration preferredLaneRouteIncentive, final Acceleration laneChangeThreshold,
            final Acceleration nonPreferredLaneRouteIncentive) throws ParameterException
    {
        try
        {
            Length headway = gtu.getBehavioralCharacteristics().getParameter(ParameterTypes.LOOKAHEAD);
            Map<Lane, Length> positions = gtu.positions(RelativePosition.REFERENCE_POSITION);
            Lane lane = positions.keySet().iterator().next();
            Length longitudinalPosition = positions.get(lane);
            // TODO make this driving side dependent; i.e. implement a general way to figure out on which side of the
            // road cars are supposed to drive
            final LateralDirectionality preferred = LateralDirectionality.RIGHT;
            final LateralDirectionality nonPreferred = LateralDirectionality.LEFT;
            Lane nonPreferredLane = gtu.getPerception().bestAccessibleAdjacentLane(lane, nonPreferred, longitudinalPosition);
            Lane preferredLane = gtu.getPerception().bestAccessibleAdjacentLane(lane, preferred, longitudinalPosition);
            AbstractLaneBasedTacticalPlannerOld albtp = (AbstractLaneBasedTacticalPlannerOld) gtu.getTacticalPlanner();
            if (null == albtp)
            {
                throw new Error(gtu + " returns null for its tactical planner");
            }
            GTUFollowingModelOld gtuFollowingModel = (GTUFollowingModelOld) albtp.getCarFollowingModel();
            if (null == gtuFollowingModel)
            {
                throw new Error(gtu + " has null GTUFollowingModel");
            }
            DualAccelerationStep straightAccelerationSteps =
                    gtuFollowingModel.computeDualAccelerationStep(gtu, sameLaneGTUs, headway, speedLimit);
            if (straightAccelerationSteps.getLeaderAcceleration().getSI() < -9999)
            {
                System.out.println("Problem");
                gtuFollowingModel.computeDualAccelerationStep(gtu, sameLaneGTUs, headway, speedLimit);
            }
            Acceleration straightA = applyDriverPersonality(straightAccelerationSteps).plus(laneChangeThreshold);
            DualAccelerationStep nonPreferredAccelerationSteps =
                    null == nonPreferredLane ? null : gtuFollowingModel.computeDualAccelerationStep(gtu, nonPreferredLaneGTUs,
                            headway, speedLimit);
            if (null != nonPreferredAccelerationSteps
                    && nonPreferredAccelerationSteps.getFollowerAcceleration().getSI() < -gtu.getStrategicalPlanner()
                            .getBehavioralCharacteristics().getParameter(ParameterTypes.B).getSI())
            {
                nonPreferredAccelerationSteps = AbstractGTUFollowingModelMobil.TOODANGEROUS;
            }
            Acceleration nonPreferredA =
                    null == nonPreferredLane ? null : applyDriverPersonality(nonPreferredAccelerationSteps);
            DualAccelerationStep preferredAccelerationSteps =
                    null == preferredLane ? null : gtuFollowingModel.computeDualAccelerationStep(gtu, preferredLaneGTUs,
                            headway, speedLimit);
            if (null != preferredAccelerationSteps
                    && preferredAccelerationSteps.getFollowerAcceleration().getSI() < -gtu.getStrategicalPlanner()
                            .getBehavioralCharacteristics().getParameter(ParameterTypes.B).getSI())
            {
                preferredAccelerationSteps = AbstractGTUFollowingModelMobil.TOODANGEROUS;
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
        catch (GTUException exception)
        {
            throw new RuntimeException(exception);
        }
    }

    /**
     * Return the weighted acceleration as described by the personality. This incorporates the personality of the driver to the
     * lane change decisions.
     * @param accelerationSteps DualAccelerationStep; the DualAccelerationStep that contains the AccelerationStep that the
     *            reference GTU will make and the AccelerationStep that the (new) follower GTU will make
     * @return Acceleration; the acceleration that the personality of the driver uses (in a comparison to a similarly computed
     *         acceleration in the non-, or different-lane-changed state) to decide if a lane change should be performed
     */
    public abstract Acceleration applyDriverPersonality(DualAccelerationStep accelerationSteps);
}
