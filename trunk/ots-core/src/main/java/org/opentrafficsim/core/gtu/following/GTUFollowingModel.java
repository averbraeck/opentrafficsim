package org.opentrafficsim.core.gtu.following;

import java.util.Collection;

import org.opentrafficsim.core.OTS_SCALAR;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.NetworkException;

/**
 * GTU following model interface. <br>
 * GTU following models following this interface compute an acceleration.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Jul 2, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface GTUFollowingModel extends OTS_SCALAR
{
    /**
     * Compute the acceleration that would be used to follow a leader.<br>
     * TODO We should probably add a <i>be ready to stop before</i> argument to prevent vehicles that cannot see their leader,
     * or should slow down for a crossing from accelerating to unsafe speeds.
     * @param follower LaneBasedGTU; the GTU for which acceleration is computed
     * @param leaderSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; the speed of the leader
     * @param headway DoubleScalar.Rel&lt;LengthUnit&gt;; the headway of the leader
     * @param speedLimit DoubleScalarAbs&lt;SpeedUnit&gt;; the local speed limit
     * @return AccelerationStep; the result of application of the GTU following model
     * @throws NetworkException on network inconsistency
     */
    AccelerationStep computeAcceleration(final LaneBasedGTU follower, final Speed.Abs leaderSpeed,
        final Length.Rel headway, final Speed.Abs speedLimit) throws NetworkException;

    /**
     * Compute the acceleration that would be used to follow a leader.<br>
     * TODO We should probably add a <i>be ready to stop before</i> argument to prevent vehicles that cannot see their leader,
     * or should slow down for a crossing from accelerating to unsafe speeds.
     * @param followerSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; the speed of the follower at the current time
     * @param followerMaximumSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; the maximum speed that the follower is capable of driving
     *            at
     * @param leaderSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; the speed of the follower at the current time
     * @param headway DoubleScalar.Rel&lt;LengthUnit&gt;; the <b>net</b> headway (distance between the front of the follower to
     *            the rear of the leader) at the current time
     * @param speedLimit DoubleScalar.Abs&lt;SpeedUnit&gt;; the local speed limit
     * @return DoubleScalar.Abs&lt;AccelerationUnit&gt;; the acceleration (or, if negative, deceleration) resulting from
     *         application of the GTU following model
     */
    Acceleration.Abs computeAcceleration(final Speed.Abs followerSpeed, Speed.Abs followerMaximumSpeed,
        final Speed.Abs leaderSpeed, final Length.Rel headway, final Speed.Abs speedLimit);

    /**
     * Compute the lowest accelerations (or most severe decelerations) that would be used if a referenceGTU is present
     * (inserted, or not removed) in a set of other GTUs.<br>
     * If any GTU in the set of otherGTUs has a null headway (indicating that the other GTU is in fact parallel to the
     * referenceGTU), prohibitive decelerations shall be returned.<br>
     * Two AccelerationStep values are returned in a DualAccelerationStep.<br>
     * TODO We should probably add a <i>be ready to stop before</i> argument to prevent vehicles that cannot see their leader,
     * or should slow down for a crossing from accelerating to unsafe speeds.
     * @param referenceGTU LaneBasedGTU; the GTU for which the accelerations are computed
     * @param otherGTUs Collection&lt;HeadwayGTU&gt;; the other GTUs. A negative headway value indicates that the other GTU is a
     *            follower. NB. If the referenceGTU is contained in this Collection, it is ignored.
     * @param speedLimit DoubleScalar.Abs&lt;SpeedUnit&gt;; the local speed limit
     * @return DualAccelerationStep; the result with the lowest accelerations (or most severe decelerations) of application of
     *         the GTU following model of the referenceGTU for each leader and follower
     * @throws NetworkException on network inconsistency
     */
    DualAccelerationStep computeAcceleration(final LaneBasedGTU referenceGTU, final Collection<HeadwayGTU> otherGTUs,
        final Speed.Abs speedLimit) throws NetworkException;

    /**
     * Compute the acceleration that would be used if the is not leader in sight.
     * @param gtu LaneBasedGTU; the GTU for which acceleration is computed
     * @param speedLimit DoubleScalar.Abs&lt;SpeedUnit&gt;; the local speed limit
     * @return AccelerationStep; the result of application of the GTU following model
     * @throws NetworkException on network inconsistency
     */
    AccelerationStep computeAccelerationWithNoLeader(final LaneBasedGTU gtu, final Speed.Abs speedLimit)
        throws NetworkException;

    /**
     * Compute the minimum <b>net</b> headway given the speed of the follower and the leader.<br>
     * At the returned headway, the follower would decelerate with it's maximum comfortable deceleration.
     * @param followerSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; speed of the follower
     * @param leaderSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; speed of the leader
     * @param precision DoubleScalar.Rel&lt;LengthUnit&gt;; the required precision of the result (must be &gt; 0)
     * @param speedLimit DoubleScalar.Abs&lt;SpeedUnit&gt;; the local speed limit
     * @param followerMaximumSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; the maximum speed that the follower can drive at
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;
     */
    Length.Rel minimumHeadway(Speed.Abs followerSpeed, Speed.Abs leaderSpeed, Length.Rel precision,
        Speed.Abs speedLimit, Speed.Abs followerMaximumSpeed);

    /**
     * Return the maximum safe deceleration for use in gap acceptance models. This is the deceleration that may be enforced upon
     * a new follower due to entering a road or changing into an adjacent lane. The result shall be a <b>positive value</b>. In
     * most car following models this value is named <cite>b</cite>.
     * @return DoubleScalar.Abs&lt;AccelerationUnit&gt;; must be a positive value!
     */
    Acceleration.Abs maximumSafeDeceleration();

    /**
     * Return the step size of this GTU following model.
     * @return DoubleScalar.Rel&lt;TimeUnit&gt;; the step size of the GTU following model
     */
    Time.Rel getStepSize();

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
