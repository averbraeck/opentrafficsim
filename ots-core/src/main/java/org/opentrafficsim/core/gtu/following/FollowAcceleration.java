package org.opentrafficsim.core.gtu.following;

import java.rmi.RemoteException;
import java.util.Collection;

import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.conversions.Calc;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * Determine acceleration (deceleration) for a GTU that follows another GTU.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Sep 19, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class FollowAcceleration
{
    /** Prohibitive deceleration used to construct the TOODANGEROUS result below. */
    private final static AccelerationStep PROHIBITIVEACCELERATIONSTEP = new AccelerationStep(
            new DoubleScalar.Abs<AccelerationUnit>(Double.NEGATIVE_INFINITY, AccelerationUnit.SI),
            new DoubleScalar.Abs<TimeUnit>(Double.NaN, TimeUnit.SI));

    /** Return value if lane change causes immediate collision. */
    private final static AccelerationStep[] TOODANGEROUS = new AccelerationStep[]{PROHIBITIVEACCELERATIONSTEP,
            PROHIBITIVEACCELERATIONSTEP};

    /**
     * This class should never be instantiated.
     */
    private FollowAcceleration()
    {
        // This class should never be instantiated
    }

    /**
     * Compute the acceleration (or deceleration) for a GTU following another GTU.
     * @param follower LaneBaseGTU; the GTU that is following
     * @param leader LaneBaseGTU; the GTU that is leading
     * @param when DoubleScalar.Abs&lt;TimeUnit&gt;; the current time
     * @param gtuFollowingModel GTUFollowingModel; the GTU following model that is used to compute the result
     * @param speedLimit DoubleScalar.Abs&lt;SpeedUnit&gt;; the speed limit
     * @return DoubleScalar.Abs&lt;AccelerationUnit&gt;; the acceleration (deceleration) for the following GTU in order
     *         to not collide with the leader GTU
     * @throws RemoteException on communication failure
     * @throws NetworkException if follower and leader do not have a common CrossSectionLink
     */
    /*
     * public static DoubleScalar.Abs<AccelerationUnit> acceleration(final LaneBasedGTU<?> follower, final
     * LaneBasedGTU<?> leader, final DoubleScalar.Abs<TimeUnit> when, final GTUFollowingModel gtuFollowingModel, final
     * DoubleScalar.Abs<SpeedUnit> speedLimit) throws RemoteException, NetworkException { if (null != leader && null !=
     * follower) { // find a lane where follower and leader are jointly // OOPS DONT DO IT THIS WAY: Set<Lane> lanes =
     * leader.getLongitudinalPositions().keySet(); // Create a NEW Set (so we don't modify the HashSet of the leader).
     * Set<Lane> lanes = new HashSet<Lane>(leader.positions(leader.getFront()).keySet());
     * lanes.retainAll(follower.positions(follower.getFront()).keySet()); // TODO expand to lanes for next links as
     * well, to a certain distance (which is...?) if (lanes.size() > 0) { Lane lane = lanes.iterator().next(); // Use
     * the first one try { MutableDoubleScalar.Rel<LengthUnit> headway = DoubleScalar.minus(leader.position(lane,
     * leader.getRear(), when), follower.position(lane, follower.getFront(), when)); if (headway.getSI() <= 0) { //
     * Immediate collision; return a prohibitive negative value return new
     * DoubleScalar.Abs<AccelerationUnit>(Double.NEGATIVE_INFINITY, AccelerationUnit.METER_PER_SECOND_2); } } catch
     * (NetworkException ne) { throw new Error("Cannot happen -- both vehicles are on these lanes"); } } else { // Not
     * on the same lane. Find a Link that is shared by both Set<CrossSectionLink<?, ?>> links = new
     * HashSet<CrossSectionLink<?, ?>>(); for (Lane lane : leader.positions(leader.getFront()).keySet()) {
     * links.add(lane.getParentLink()); } if (links.size() == 0) { throw new
     * NetworkException("Leader is not on any link"); } Set<CrossSectionLink<?, ?>> followerLinks = new
     * HashSet<CrossSectionLink<?, ?>>(); for (Lane lane : follower.positions(follower.getFront()).keySet()) {
     * followerLinks.add(lane.getParentLink()); } links.retainAll(followerLinks); if (links.size() == 0) { throw new
     * NetworkException("Leader and follower do not have a common CrossSectionLink"); } CrossSectionLink<?, ?>
     * commonLink = links.iterator().next(); // Use the first one Lane leaderLane = null; for (Lane lane :
     * leader.positions(leader.getFront()).keySet()) { if (lane.getParentLink() == commonLink) { leaderLane = lane;
     * break; } } if (null == leaderLane) { throw new Error("Cannot happen -- Cannot find leaderLane"); } Lane
     * followerLane = null; for (Lane lane : follower.positions(RelativePosition.REFERENCE_POSITION).keySet()) { if
     * (lane.getParentLink() == commonLink) { followerLane = lane; break; } } if (null == followerLane) { throw new
     * Error("Cannot happen -- Cannot find leaderLane"); } // Get the difference of the projections of both lanes onto
     * the design line of the link // leader.positionOfRear(leaderLane, when); double leaderRatio =
     * leader.fractionalPosition(leaderLane, leader.getRear(), when); double followerRatio =
     * follower.fractionalPosition(followerLane, follower.getFront(), when); double ratioDifference = leaderRatio -
     * followerRatio; // TODO prove that this is always correct // Scale that difference by the length of the lane of
     * the follower to obtain the headway DoubleScalar.Rel<LengthUnit> headway = new
     * DoubleScalar.Rel<LengthUnit>(followerLane.getLength().getSI() * ratioDifference, LengthUnit.METER); return
     * gtuFollowingModel.computeAcceleration(follower, leader.getLongitudinalVelocity(when), headway,
     * speedLimit).getAcceleration(); } } // follower == null || leader == null return
     * gtuFollowingModel.computeAcceleration(follower, leader, speedLimit).getAcceleration(); }
     */

    /**
     * Compute the acceleration of this GTU and the (new) follower GTU in the current lane, or after a considered lane
     * change. Both values are returned; the acceleration for the reference car at index 0, the acceleration of the
     * (new) follower GTU at index 1. If changing lane is not possible because it would result in dangerous deceleration
     * or collision, the returned value in both fields is Double.NEGATIVE_INFINITY.
     * @param referenceGTU AbstractLaneBasedGTU&lt;?&gt;; the car that considers changing lane and whose
     *            gtuFollowingModel will be used for all estimations of the resulting accelerations and decelerations
     * @param otherGTUs Set&lt;HeadwayGTU&gt;; the GTUs in the target lane and their headways from the referenceGTU
     * @param speedLimit DoubleScalar.Abs&lt;SpeedUnit&gt;; the speed limit
     * @return DoubleVector.Abs&lt;AccelerationUnit&gt;; the lowest accelerations (highest decelerations) incurred if
     *         the GTU is inserted among otherGTUs
     * @throws RemoteException on communication failure
     * @throws NetworkException when the network is inconsistent
     */
    public static AccelerationStep[] acceleration(final LaneBasedGTU<?> referenceGTU,
            final Collection<HeadwayGTU> otherGTUs, final DoubleScalar.Abs<SpeedUnit> speedLimit)
            throws RemoteException, NetworkException
    {
        DoubleScalar.Abs<TimeUnit> when = referenceGTU.getSimulator().getSimulatorTime().get();
        // Find out if there is an immediate collision
        for (HeadwayGTU headwayGTU : otherGTUs)
        {
            if (null == headwayGTU.getDistance())
            {
                return TOODANGEROUS;
            }
        }
        AccelerationStep followerAccelerationStep = null;
        AccelerationStep referenceGTUAccelerationStep = null;
        // Find the leader and the follower that cause/experience the least positive (most negative) acceleration.
        for (HeadwayGTU headwayGTU : otherGTUs)
        {
            if (null == headwayGTU.getOtherGTU())
            {
                System.out.println("WTF");
            }
            if (headwayGTU.getOtherGTU() == referenceGTU)
            {
                continue;
            }
            if (headwayGTU.getDistanceSI() < 0)
            {
                // This one is behind
                AccelerationStep as =
                        referenceGTU.getGTUFollowingModel().computeAcceleration(headwayGTU.getOtherGTU(),
                                referenceGTU.getLongitudinalVelocity(when),
                                new DoubleScalar.Rel<LengthUnit>(-headwayGTU.getDistanceSI(), LengthUnit.SI),
                                speedLimit);
                if (null == followerAccelerationStep
                        || as.getAcceleration().getSI() < followerAccelerationStep.getAcceleration().getSI())
                {
                    followerAccelerationStep = as;
                }
            }
            else
            {
                // This one is ahead
                AccelerationStep as =
                        referenceGTU.getGTUFollowingModel().computeAcceleration(referenceGTU,
                                headwayGTU.getOtherGTU().getLongitudinalVelocity(when), headwayGTU.getDistance(),
                                speedLimit);
                if (null == referenceGTUAccelerationStep
                        || (as.getAcceleration().getSI() < referenceGTUAccelerationStep.getAcceleration().getSI()))
                {
                    referenceGTUAccelerationStep = as;
                }
            }
        }
        if (null == followerAccelerationStep)
        {
            followerAccelerationStep =
                    new AccelerationStep(new DoubleScalar.Abs<AccelerationUnit>(0, AccelerationUnit.SI), DoubleScalar
                            .plus(referenceGTU.getSimulator().getSimulatorTime().get(),
                                    referenceGTU.getGTUFollowingModel().getStepSize()).immutable());
        }
        if (null == referenceGTUAccelerationStep)
        {
            referenceGTUAccelerationStep =
                    referenceGTU.getGTUFollowingModel().computeAcceleration(
                            referenceGTU,
                            referenceGTU.getLongitudinalVelocity(when),
                            Calc.speedSquaredDividedByDoubleAcceleration(referenceGTU.getMaximumVelocity(),
                                    referenceGTU.getGTUFollowingModel().maximumSafeDeceleration()), speedLimit);
        }
        System.out.println("FollowAcceleration reference: " + referenceGTU + " otherGTUs: "+otherGTUs);
        System.out.println("result: [" + referenceGTUAccelerationStep + ", " + followerAccelerationStep + "]");
        return new AccelerationStep[]{referenceGTUAccelerationStep, followerAccelerationStep};
    }

}
