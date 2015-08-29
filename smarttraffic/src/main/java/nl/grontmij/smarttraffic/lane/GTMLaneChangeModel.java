package nl.grontmij.smarttraffic.lane;

import java.rmi.RemoteException;
import java.util.Collection;
import java.util.Map;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.following.AbstractGTUFollowingModel;
import org.opentrafficsim.core.gtu.following.DualAccelerationStep;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.HeadwayGTU;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.gtu.lane.changing.LaneChangeModel;
import org.opentrafficsim.core.gtu.lane.changing.LaneMovementStep;
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
 * @version $Revision: 1155 $, $LastChangedDate: 2015-07-26 01:01:13 +0200 (Sun, 26 Jul 2015) $, by $Author: averbraeck $,
 *          initial version 4 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class GTMLaneChangeModel implements LaneChangeModel
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
            Map<Lane, Rel<LengthUnit>> positions = gtu.positions(RelativePosition.REFERENCE_POSITION);
            Lane lane = positions.keySet().iterator().next();
            DoubleScalar.Rel<LengthUnit> longitudinalPosition = positions.get(lane);
            // TODO make this driving side dependent; i.e. implement a general way to figure out on which side of the
            // road cars are supposed to drive
            final LateralDirectionality preferred = LateralDirectionality.RIGHT;
            final LateralDirectionality nonPreferred = LateralDirectionality.LEFT;

            // Zorg er voor dat er niet wordt afgeslagen van de hoofdroute onder normale omstandigheden.
            Lane nonPreferredLane = gtu.bestAccessibleAdjacentLane(lane, nonPreferred, longitudinalPosition);
            if (nonPreferredLane != null && !suitable(nonPreferredLane, gtu))
                nonPreferredLane = null;
            Lane preferredLane = gtu.bestAccessibleAdjacentLane(lane, preferred, longitudinalPosition);
            if (preferredLane != null && !suitable(preferredLane, gtu))
                preferredLane = null;
            boolean currSuit = suitable(lane, gtu);
            if (currSuit && nonPreferredLane != null && gtu.getVelocity().getSI() < 5) // 2.7777)
                nonPreferredLane = null; // do not change to other lane when slow and on a preferred lane already
            if (currSuit && preferredLane != null && gtu.getVelocity().getSI() < 5) // 2.7777)
                preferredLane = null; // do not change to other lane when slow and on a preferred lane already

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
                DoubleScalar.plus(applyDriverPersonality(straightAccelerationSteps), laneChangeThreshold).immutable();
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
                DoubleScalar.minus(DoubleScalar.plus(preferredA, preferredLaneRouteIncentive).immutable(), straightA)
                    .immutable();
            DoubleScalar.Rel<AccelerationUnit> nonPreferredAttractiveness =
                DoubleScalar.minus(DoubleScalar.plus(nonPreferredA, nonPreferredLaneRouteIncentive).immutable(), straightA)
                    .immutable();
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

    private boolean suitable(Lane lane, LaneBasedGTU gtu)
    {
        if (lane.nextLanes(gtu.getGTUType()).size() == 0) // try to get off a lane that is ending
            return false;
        return true;
        // CompleteRoute route;
        // RouteNavigator navigator = ((LaneBasedIndividualCar) gtu).getRouteNavigator();
        // if (navigator instanceof StraightRouteNavigator)
        // route = ((StraightRouteNavigator) navigator).straightRoute;
        // else
        // route = ((CompleteLaneBasedRouteNavigator) navigator).getRoute();
        // // if the lane connects to the main route: good, otherwise: bad
        // if (route.getNodes().contains(lane.getParentLink().getEndNode()))
        // {
        // if (lane.nextLanes().size() == 0) // try to get off a lane that is ending
        // return false;
        // Lane nextLane = (Lane) lane.nextLanes().iterator().next();
        // if (nextLane != null && route.getNodes().contains(nextLane.getParentLink().getEndNode()))
        // return true;
        // else
        // return false;
        // }
        // else
        // return false;
    }

    /**
     * Return the weighted acceleration as described by the personality. This incorporates the personality of the driver to the
     * lane change decisions.
     * @param accelerations DualAccelerationStep; the DualAccelerationStep that contains the AccelerationStep that the reference
     *            GTU will make and the AccelerationStep that the (new) follower GTU will make
     * @return DoubleScalar.Abs&lt;AccelerationUnit&gt;; the acceleration that the personality of the driver uses (in a
     *         comparison to a similarly computed acceleration in the non-, or different-lane-changed state) to decide if a lane
     *         change should be performed
     */
    public DoubleScalar.Abs<AccelerationUnit> applyDriverPersonality(final DualAccelerationStep accelerations)
    {
        // The egoistic driver only looks at the effects on him-/herself.
        return accelerations.getLeaderAcceleration();
    }

    /** {@inheritDoc} */
    @Override
    public String getName()
    {
        return "GTM StraightRoute";
    }

    /** {@inheritDoc} */
    @Override
    public String getLongName()
    {
        return "GTM StraightRoute lane change model (based on Treiber).";
    }

}
