package org.opentrafficsim.road.gtu.lane.tactical.directedlanechange;

import java.util.Collection;
import java.util.Map;

import org.djunits.unit.AccelerationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.tactical.following.AbstractGTUFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.DualAccelerationStep;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.HeadwayGTU;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Common code for a family of lane change models like in M. Treiber and A. Kesting <i>Traffic Flow Dynamics</i>,
 * Springer-Verlag Berlin Heidelberg 2013, pp 239-244.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1401 $, $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, by $Author: averbraeck $,
 *          initial version 4 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractDirectedLaneChangeModel implements DirectedLaneChangeModel
{
    /** Attempt to overcome rounding errors. */
    private static Acceleration extraThreshold = new Acceleration(0.000001, AccelerationUnit.SI);

    /** {@inheritDoc} */
    @Override
    public final DirectedLaneMovementStep computeLaneChangeAndAcceleration(final LaneBasedGTU gtu,
        final LateralDirectionality direction, final Collection<HeadwayGTU> sameLaneGTUs,
        final Collection<HeadwayGTU> otherLaneGTUs, final Length.Rel maxDistance, final Speed speedLimit,
        final Acceleration otherLaneRouteIncentive, final Acceleration laneChangeThreshold) throws GTUException
    {
        Map<Lane, Length.Rel> positions = gtu.positions(RelativePosition.REFERENCE_POSITION);
        Lane lane = positions.keySet().iterator().next();
        Length.Rel longitudinalPosition = positions.get(lane);
        Lane preferredLane = gtu.getPerception().bestAccessibleAdjacentLane(lane, direction, longitudinalPosition);
        GTUFollowingModel gtuFollowingModel = gtu.getDrivingCharacteristics().getGTUFollowingModel();
        if (null == gtuFollowingModel)
        {
            throw new Error("GTU " + gtu + " has null GTUFollowingModel");
        }
        DualAccelerationStep straightAccelerationSteps =
            gtuFollowingModel.computeDualAccelerationStep(gtu, sameLaneGTUs, maxDistance, speedLimit);
        if (straightAccelerationSteps.getLeaderAcceleration().getSI() < -9999)
        {
            System.out.println("Problem");
            gtu.getStrategicalPlanner().getDrivingCharacteristics().getGTUFollowingModel()
                .computeDualAccelerationStep(gtu, sameLaneGTUs, maxDistance, speedLimit);
        }
        Acceleration straightA = applyDriverPersonality(straightAccelerationSteps).plus(laneChangeThreshold);
        DualAccelerationStep preferredAccelerationSteps =
            null == preferredLane ? null : gtu.getStrategicalPlanner().getDrivingCharacteristics()
                .getGTUFollowingModel().computeDualAccelerationStep(gtu, otherLaneGTUs, maxDistance, speedLimit);
        if (null != preferredAccelerationSteps
            && preferredAccelerationSteps.getFollowerAcceleration().getSI() < -gtu.getStrategicalPlanner()
                .getDrivingCharacteristics().getGTUFollowingModel().getMaximumSafeDeceleration().getSI())
        {
            preferredAccelerationSteps = AbstractGTUFollowingModel.TOODANGEROUS;
        }
        Acceleration preferredA = null == preferredLane ? null : applyDriverPersonality(preferredAccelerationSteps);
        if (null == preferredA)
        {
            // No lane change possible; this is definitely the easy case
            return new DirectedLaneMovementStep(straightAccelerationSteps.getLeaderAccelerationStep(), null);
        }
        // A merge to the preferredLane is possible
        if (DoubleScalar.plus(preferredA, otherLaneRouteIncentive).plus(extraThreshold).ge(straightA))
        {
            // Merge to the preferred lane; i.e. finish (or cancel) an overtaking procedure
            return new DirectedLaneMovementStep(preferredAccelerationSteps.getLeaderAccelerationStep(), direction);
        }
        else
        {
            // Stay in current lane
            return new DirectedLaneMovementStep(straightAccelerationSteps.getLeaderAccelerationStep(), null);
        }
    }

    /**
     * Return the weighted acceleration as described by the personality. This incorporates the personality of the driver to the
     * lane change decisions.
     * @param accelerationStep DualAccelerationStep; the DualAccelerationStep that contains the AccelerationStep that the
     *            reference GTU will make and the AccelerationStep that the (new) follower GTU will make
     * @return Acceleration; the acceleration that the personality of the driver uses (in a comparison to a similarly computed
     *         acceleration in the non-, or different-lane-changed state) to decide if a lane change should be performed
     */
    public abstract Acceleration applyDriverPersonality(DualAccelerationStep accelerationStep);
}
