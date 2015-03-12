package org.opentrafficsim.core.gtu.following;

import java.rmi.RemoteException;
import java.util.Collection;

import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;

/**
 * Abstract GTU following model.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jul 2, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface GTUFollowingModel
{
    /**
     * Compute the acceleration that would be used to follow a leader.<br />
     * TODO We should probably add a <i>be ready to stop before</i> argument to prevent vehicles that cannot see their
     * leader, or should slow down for a crossing from accelerating to unsafe speeds.
     * @param follower LaneBasedGTU&lt;?&gt;; the GTU for which acceleration is computed
     * @param leaderSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; the speed of the leader
     * @param headway DoubleScalar.Rel&lt;LengthUnit&gt;; the headway of the leader
     * @param speedLimit DoubleScalarAbs&lt;SpeedUnit&gt;; the local speed limit
     * @return AccelerationStep; the result of application of the GTU following model
     * @throws RemoteException in case of simulator reachability problems
     * @throws NetworkException on network inconsistency
     */
    AccelerationStep computeAcceleration(final LaneBasedGTU<?> follower, final DoubleScalar.Abs<SpeedUnit> leaderSpeed,
            final DoubleScalar.Rel<LengthUnit> headway, final DoubleScalar.Abs<SpeedUnit> speedLimit)
            throws RemoteException, NetworkException;

    /**
     * Compute the acceleration that would be used to follow a leader.<br />
     * TODO We should probably add a <i>be ready to stop before</i> argument to prevent vehicles that cannot see their
     * @param followerSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; the speed of the follower at the current time
     * @param followerMaximumSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; the maximum speed that the follower is capable of
     *            driving at
     * @param leaderSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; the speed of the follower at the current time
     * @param headway DoubleScalar.Rel&lt;LengthUnit&gt;; the <b>net</b> headway (distance between the front of the
     *            follower to the rear of the leader) at the current time
     * @param speedLimit DoubleScalar.Abs&lt;SpeedUnit&gt;; the local speed limit
     * @return DoubleScalar.Abs&lt;AccelerationUnit&gt;; the acceleration (or, if negative, deceleration) resulting from
     *         application of the GTU following model
     * @throws RemoteException on communications failure
     */
    DoubleScalar.Abs<AccelerationUnit> computeAcceleration(final DoubleScalar.Abs<SpeedUnit> followerSpeed,
            Abs<SpeedUnit> followerMaximumSpeed, final DoubleScalar.Abs<SpeedUnit> leaderSpeed,
            final DoubleScalar.Rel<LengthUnit> headway, final DoubleScalar.Abs<SpeedUnit> speedLimit)
            throws RemoteException;

    /**
     * Compute the lowest accelerations (or most severe decelerations) that would be used if a referenceGTU is present
     * (inserted, or not removed) in a set of other GTUs.<br />
     * If any GTU in the set of otherGTUs has a null headway (indicating that the other GTU is in fact parallel to the
     * referenceGTU), prohibitive decelerations shall be returned.<br />
     * Two AccelerationStep values are returned in a DualAccelerationStep.<br />
     * TODO We should probably add a <i>be ready to stop before</i> argument to prevent vehicles that cannot see their
     * leader, or should slow down for a crossing from accelerating to unsafe speeds.
     * @param referenceGTU LaneBasedGTU&lt;?&gt;; the GTU for which the accelerations are computed
     * @param otherGTUs Collection&lt;HeadwayGTU&gt;; the other GTUs. A negative headway value indicates that the other
     *            GTU is a follower. NB. If the referenceGTU is contained in this Collection, it is ignored.
     * @param speedLimit DoubleScalar.Abs&lt;SpeedUnit&gt;; the local speed limit
     * @return DualAccelerationStep; the result with the lowest accelerations (or most severe decelerations) of
     *         application of the GTU following model of the referenceGTU for each leader and follower
     * @throws RemoteException in case of simulator reachability problems
     * @throws NetworkException on network inconsistency
     */
    DualAccelerationStep computeAcceleration(final LaneBasedGTU<?> referenceGTU, final Collection<HeadwayGTU> otherGTUs,
            final DoubleScalar.Abs<SpeedUnit> speedLimit) throws RemoteException, NetworkException;

    /**
     * Compute the acceleration that would be used if the is not leader in sight.
     * @param gtu LaneBasedGTU&lt;?&gt;; the GTU for which acceleration is computed
     * @param speedLimit DoubleScalar.Abs&lt;SpeedUnit&gt;; the local speed limit
     * @return AccelerationStep; the result of application of the GTU following model
     * @throws RemoteException in case of simulator reachability problems
     * @throws NetworkException on network inconsistency
     */
    AccelerationStep computeAccelerationWithNoLeader(final LaneBasedGTU<?> gtu,
            final DoubleScalar.Abs<SpeedUnit> speedLimit) throws RemoteException, NetworkException;

    /**
     * Compute the minimum <b>net></b> headway given the speed of the follower and the leader.<br/>
     * At the returned headway, the follower would decelerate with it's maximum comfortable deceleration.
     * @param followerSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; speed of the follower
     * @param leaderSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; speed of the leader
     * @param precision DoubleScalar.Rel&lt;LengthUnit&gt;; the required precision of the result (must be > 0)
     * @param speedLimit DoubleScalar.Abs&lt;SpeedUnit&gt;; the local speed limit
     * @param followerMaximumSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; the maximum speed that the follower can drive at
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;
     * @throws RemoteException on communications failure
     */
    DoubleScalar.Rel<LengthUnit> minimumHeadway(DoubleScalar.Abs<SpeedUnit> followerSpeed,
            DoubleScalar.Abs<SpeedUnit> leaderSpeed, DoubleScalar.Rel<LengthUnit> precision, Abs<SpeedUnit> speedLimit,
            Abs<SpeedUnit> followerMaximumSpeed) throws RemoteException;

    /**
     * Return the maximum safe deceleration for use in gap acceptance models. This is the deceleration that may be
     * enforced upon a new follower due to entering a road or changing into an adjacent lane. The result shall be a
     * <b>positive value</b>. In most car following models this value is named <cite>b</cite>.
     * @return DoubleScalar.Abs&lt;AccelerationUnit&gt;; must be a positive value!
     */
    DoubleScalar.Abs<AccelerationUnit> maximumSafeDeceleration();

    /**
     * Return the step size of this GTU following model.
     * @return DoubleScalar.Rel&lt;TimeUnit&gt;; the step size of the GTU following model
     * @throws RemoteException on communications failure
     */
    DoubleScalar.Rel<TimeUnit> getStepSize() throws RemoteException;

    /**
     * Return the name of this GTU following model.
     * @return String; just the name of the GTU following model
     */
    String getName();

    /**
     * Return complete textual information about this instantiation of this GTU following model.
     * @return String; the name and parameter values of the GTU following model
     */
    String getLongName();

}
