package org.opentrafficsim.core.gtu.lane.changing;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Map;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.following.AbstractGTUFollowingModel;
import org.opentrafficsim.core.gtu.following.DualAccelerationStep;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.HeadwayGTU;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.lane.Lane;

/**
 * Common code for a family of lane change models like in M. Treiber and A. Kesting <i>Traffic Flow Dynamics</i>,
 * Springer-Verlag Berlin Heidelberg 2013, pp 239-244.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version 4 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class AbstractLaneChangeModel implements LaneChangeModel
{
    /** {@inheritDoc} */
    @SuppressWarnings("checkstyle:parameternumber")
    @Override
    public final LaneMovementStep computeLaneChangeAndAcceleration(final LaneBasedGTU gtu,
        final Collection<HeadwayGTU> sameLaneGTUs, final Collection<HeadwayGTU> preferredLaneGTUs,
        final Collection<HeadwayGTU> nonPreferredLaneGTUs, final DoubleScalar.Abs<SpeedUnit> speedLimit,
        final DoubleScalar.Rel<AccelerationUnit> preferredLaneRouteIncentive,
        final DoubleScalar.Rel<AccelerationUnit> laneChangeThreshold,
        final DoubleScalar.Rel<AccelerationUnit> nonPreferredLaneRouteIncentive) throws RemoteException
    {
        try
        {
            Map<Lane, DoubleScalar.Rel<LengthUnit>> positions = gtu.positions(RelativePosition.REFERENCE_POSITION);
            Lane lane = positions.keySet().iterator().next();
            DoubleScalar.Rel<LengthUnit> longitudinalPosition = positions.get(lane);
            // TODO make this driving side dependent; i.e. implement a general way to figure out on which side of the
            // road cars are supposed to drive
            final LateralDirectionality preferred = LateralDirectionality.RIGHT;
            final LateralDirectionality nonPreferred = LateralDirectionality.LEFT;
            Lane nonPreferredLane = gtu.bestAccessibleAdjacentLane(lane, nonPreferred, longitudinalPosition);
            Lane preferredLane = gtu.bestAccessibleAdjacentLane(lane, preferred, longitudinalPosition);
            GTUFollowingModel gtuFollowingModel = gtu.getGTUFollowingModel();
            if (null == gtuFollowingModel)
            {
                throw new Error("GTU " + gtu + " has null GTUFollowingModel");
            }
            DualAccelerationStep straightAccelerationSteps =
                gtuFollowingModel.computeAcceleration(gtu, sameLaneGTUs, speedLimit);
            if (straightAccelerationSteps.getLeaderAcceleration().getSI() < -9999)
            {
                System.out.println("Problem");
                gtu.getGTUFollowingModel().computeAcceleration(gtu, sameLaneGTUs, speedLimit);
            }
            DoubleScalar.Abs<AccelerationUnit> straightA =
                DoubleScalar.plus(applyDriverPersonality(straightAccelerationSteps), laneChangeThreshold);
            DualAccelerationStep nonPreferrredAccelerationSteps =
                null == nonPreferredLane ? null : gtu.getGTUFollowingModel().computeAcceleration(gtu, nonPreferredLaneGTUs,
                    speedLimit);
            if (null != nonPreferrredAccelerationSteps
                && nonPreferrredAccelerationSteps.getFollowerAcceleration().getSI() < -gtu.getGTUFollowingModel()
                    .maximumSafeDeceleration().getSI())
            {
                nonPreferrredAccelerationSteps = AbstractGTUFollowingModel.TOODANGEROUS;
            }
            DoubleScalar.Abs<AccelerationUnit> nonPreferredA =
                null == nonPreferredLane ? null : applyDriverPersonality(nonPreferrredAccelerationSteps);
            DualAccelerationStep preferredAccelerationSteps =
                null == preferredLane ? null : gtu.getGTUFollowingModel().computeAcceleration(gtu, preferredLaneGTUs,
                    speedLimit);
            if (null != preferredAccelerationSteps
                && preferredAccelerationSteps.getFollowerAcceleration().getSI() < -gtu.getGTUFollowingModel()
                    .maximumSafeDeceleration().getSI())
            {
                preferredAccelerationSteps = AbstractGTUFollowingModel.TOODANGEROUS;
            }
            DoubleScalar.Abs<AccelerationUnit> preferredA =
                null == preferredLane ? null : applyDriverPersonality(preferredAccelerationSteps);
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
                    if (DoubleScalar.plus(nonPreferredA, nonPreferredLaneRouteIncentive).gt(straightA))
                    {
                        // Merge to the nonPreferred lane; i.e. start an overtaking procedure
                        return new LaneMovementStep(nonPreferrredAccelerationSteps.getLeaderAccelerationStep(), nonPreferred);
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
                if (DoubleScalar.plus(preferredA, preferredLaneRouteIncentive).gt(straightA))
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
            DoubleScalar.Rel<AccelerationUnit> preferredAttractiveness =
                DoubleScalar.minus(DoubleScalar.plus(preferredA, preferredLaneRouteIncentive), straightA)
                    ;
            DoubleScalar.Rel<AccelerationUnit> nonPreferredAttractiveness =
                DoubleScalar.minus(DoubleScalar.plus(nonPreferredA, nonPreferredLaneRouteIncentive), straightA)
                    ;
            if (preferredAttractiveness.getSI() <= 0 && nonPreferredAttractiveness.getSI() < 0)
            {
                // Stay in current lane
                return new LaneMovementStep(straightAccelerationSteps.getLeaderAccelerationStep(), null);

            }
            if (preferredAttractiveness.getSI() > 0 && preferredAttractiveness.gt(nonPreferredAttractiveness))
            {
                // Merge to the preferred lane; i.e. finish (or cancel) an overtaking procedure
                return new LaneMovementStep(preferredAccelerationSteps.getLeaderAccelerationStep(), preferred);
            }
            // Merge to the adjacent nonPreferred lane; i.e. start an overtaking procedure
            return new LaneMovementStep(nonPreferrredAccelerationSteps.getLeaderAccelerationStep(), nonPreferred);
        }
        catch (NetworkException exception)
        {
            exception.printStackTrace();
        }
        throw new Error("Cannot happen: computeLaneChangeAndAcceleration failed to decide whether or not to change lane");
    }

    /**
     * Return the weighted acceleration as described by the personality. This incorporates the personality of the driver to the
     * lane change decisions.
     * @param accelerationSteps DualAccelerationStep; the DualAccelerationStep that contains the AccelerationStep that the
     *            reference GTU will make and the AccelerationStep that the (new) follower GTU will make
     * @return DoubleScalar.Abs&lt;AccelerationUnit&gt;; the acceleration that the personality of the driver uses (in a
     *         comparison to a similarly computed acceleration in the non-, or different-lane-changed state) to decide if a lane
     *         change should be performed
     */
    public abstract DoubleScalar.Abs<AccelerationUnit> applyDriverPersonality(DualAccelerationStep accelerationSteps);
}
