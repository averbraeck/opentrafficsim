package org.opentrafficsim.road.gtu.lane.tactical.following;

import java.util.Collection;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;

/**
 * GTU following model interface. <br>
 * GTU following models following this interface compute an acceleration.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1401 $, $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, by $Author: averbraeck $,
 *          initial version Jul 2, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public interface GTUFollowingModelOld
{
    /**
     * Compute the acceleration that would be used to follow a leader.<br>
     * @param gtu LaneBasedGTU; the GTU for which acceleration is computed
     * @param leaderSpeed Speed; the speed of the leader
     * @param headway Length.Rel; the headway of the leader
     * @param maxDistance Length.Rel; the maximum distance we can cover at the current time, e.g. as the result of a lane drop
     * @param speedLimit Speed; the local speed limit
     * @return AccelerationStep; the result of application of the GTU following model
     * @throws GTUException when the velocity of the gtu cannot be determined
     */
    AccelerationStep computeAccelerationStep(final LaneBasedGTU gtu, final Speed leaderSpeed, final Length.Rel headway,
        final Length.Rel maxDistance, final Speed speedLimit) throws GTUException;

    /**
     * Compute the acceleration that would be used to follow a leader.<br>
     * @param gtu LaneBasedGTU; the GTU for which acceleration is computed
     * @param leaderSpeed Speed; the speed of the leader
     * @param headway Length.Rel; the headway of the leader
     * @param maxDistance Length.Rel; the maximum distance we can cover at the current time, e.g. as the result of a lane drop
     * @param speedLimit Speed; the local speed limit
     * @param stepSize given step size, which can be longer or shorter than the provided step size in the algorithms.
     * @return AccelerationStep; the result of application of the GTU following model
     * @throws GTUException when the velocity of the gtu cannot be determined
     */
    AccelerationStep computeAccelerationStep(final LaneBasedGTU gtu, final Speed leaderSpeed, final Length.Rel headway,
        final Length.Rel maxDistance, final Speed speedLimit, final Time.Rel stepSize) throws GTUException;

    /**
     * Compute the acceleration that would be used to follow a leader.<br>
     * @param followerSpeed Speed; the speed of the follower at the current time
     * @param followerMaximumSpeed Speed; the maximum speed that the follower is capable of driving at
     * @param leaderSpeed Speed; the speed of the follower at the current time
     * @param headway Length.Rel; the <b>net</b> headway (distance between the front of the follower to the rear of the leader)
     *            at the current time, or the maximum distance we can cover at the current time, e.g. as the result of a lane
     *            drop
     * @param speedLimit Speed; the local speed limit
     * @return Acceleration; the acceleration (or, if negative, deceleration) resulting from application of the GTU following
     *         model
     */
    Acceleration computeAcceleration(final Speed followerSpeed, Speed followerMaximumSpeed, final Speed leaderSpeed,
        final Length.Rel headway, final Speed speedLimit);

    /**
     * Compute the acceleration that would be used to follow a leader.<br>
     * @param followerSpeed Speed; the speed of the follower at the current time
     * @param followerMaximumSpeed Speed; the maximum speed that the follower is capable of driving at
     * @param leaderSpeed Speed; the speed of the follower at the current time
     * @param headway Length.Rel; the <b>net</b> headway (distance between the front of the follower to the rear of the leader)
     *            at the current time, or the maximum distance we can cover at the current time, e.g. as the result of a lane
     *            drop
     * @param speedLimit Speed; the local speed limit
     * @param stepSize given step size, which can be longer or shorter than the provided step size in the algorithms.
     * @return Acceleration; the acceleration (or, if negative, deceleration) resulting from application of the GTU following
     *         model
     */
    Acceleration computeAcceleration(final Speed followerSpeed, Speed followerMaximumSpeed, final Speed leaderSpeed,
        final Length.Rel headway, final Speed speedLimit, final Time.Rel stepSize);

    /**
     * Compute the acceleration that would be used to follow a leader.<br>
     * @param followerSpeed Speed; the speed of the follower at the current time
     * @param leaderSpeed Speed; the speed of the follower at the current time
     * @param headway Length.Rel; the <b>net</b> headway (distance between the front of the follower to the rear of the leader)
     *            at the current time, or the maximum distance we can cover at the current time, e.g. as the result of a lane
     *            drop
     * @param speedLimit Speed; the local speed limit
     * @param currentTime to be used to determine the validity of the AccelerationStep
     * @return Acceleration; the acceleration (or, if negative, deceleration) resulting from application of the GTU following
     *         model
     */
    AccelerationStep computeAccelerationStep(Speed followerSpeed, Speed leaderSpeed, Length.Rel headway,
        Speed speedLimit, Time.Abs currentTime);

    /**
     * Compute the acceleration that would be used to follow a leader.<br>
     * @param followerSpeed Speed; the speed of the follower at the current time
     * @param leaderSpeed Speed; the speed of the follower at the current time
     * @param headway Length.Rel; the <b>net</b> headway (distance between the front of the follower to the rear of the leader)
     *            at the current time, or the maximum distance we can cover at the current time, e.g. as the result of a lane
     *            drop
     * @param speedLimit Speed; the local speed limit
     * @param currentTime to be used to determine the validity of the AccelerationStep
     * @param stepSize given step size, which can be longer or shorter than the provided step size in the algorithms.
     * @return Acceleration; the acceleration (or, if negative, deceleration) resulting from application of the GTU following
     *         model
     */
    AccelerationStep computeAccelerationStep(Speed followerSpeed, Speed leaderSpeed, Length.Rel headway,
        Speed speedLimit, Time.Abs currentTime, final Time.Rel stepSize);

    /**
     * Compute the lowest accelerations (or most severe decelerations) that would be used if a referenceGTU is present
     * (inserted, or not removed) in a set of other GTUs.<br>
     * If any GTU in the set of otherGTUs has a null headway (indicating that the other GTU is in fact parallel to the
     * referenceGTU), prohibitive decelerations shall be returned.<br>
     * Two AccelerationStep values are returned in a DualAccelerationStep.<br>
     * or should slow down for a crossing from accelerating to unsafe speeds.
     * @param gtu LaneBasedGTU; the GTU for which the accelerations are computed
     * @param otherGtuHeadways Collection&lt;HeadwayGTU&gt;; the other GTUs. A negative headway value indicates that the other
     *            GTU is a follower. NB. If the referenceGTU is contained in this Collection, it is ignored.
     * @param maxDistance Length.Rel; the maximum distance we can cover at the current time, e.g. as the result of a lane drop
     * @param speedLimit Speed; the local speed limit
     * @return DualAccelerationStep; the result with the lowest accelerations (or most severe decelerations) of application of
     *         the GTU following model of the referenceGTU for each leader and follower
     * @throws GTUException when the velocity of the gtu cannot be determined
     */
    DualAccelerationStep computeDualAccelerationStep(final LaneBasedGTU gtu,
        final Collection<HeadwayGTU> otherGtuHeadways, final Length.Rel maxDistance, final Speed speedLimit) throws GTUException;

    /**
     * Compute the lowest accelerations (or most severe decelerations) that would be used if a referenceGTU is present
     * (inserted, or not removed) in a set of other GTUs.<br>
     * If any GTU in the set of otherGTUs has a null headway (indicating that the other GTU is in fact parallel to the
     * referenceGTU), prohibitive decelerations shall be returned.<br>
     * Two AccelerationStep values are returned in a DualAccelerationStep.<br>
     * or should slow down for a crossing from accelerating to unsafe speeds.
     * @param gtu LaneBasedGTU; the GTU for which the accelerations are computed
     * @param otherGtuHeadways Collection&lt;HeadwayGTU&gt;; the other GTUs. A negative headway value indicates that the other
     *            GTU is a follower. NB. If the referenceGTU is contained in this Collection, it is ignored.
     * @param maxDistance Length.Rel; the maximum distance we can cover at the current time, e.g. as the result of a lane drop
     * @param speedLimit Speed; the local speed limit
     * @param stepSize given step size, which can be longer or shorter than the provided step size in the algorithms.
     * @return DualAccelerationStep; the result with the lowest accelerations (or most severe decelerations) of application of
     *         the GTU following model of the referenceGTU for each leader and follower
     * @throws GTUException when the velocity of the gtu cannot be determined
     */
    DualAccelerationStep computeDualAccelerationStep(final LaneBasedGTU gtu,
        final Collection<HeadwayGTU> otherGtuHeadways, final Length.Rel maxDistance, final Speed speedLimit,
        final Time.Rel stepSize) throws GTUException;

    /**
     * Compute the acceleration that would be used if the is not leader in sight.
     * @param gtu LaneBasedGTU; the GTU for which acceleration is computed
     * @param maxDistance Length.Rel; the maximum distance we can cover at the current time, e.g. as the result of a lane drop
     * @param speedLimit Speed; the local speed limit
     * @return AccelerationStep; the result of application of the GTU following model
     * @throws GTUException when the velocity of the gtu cannot be determined
     */
    AccelerationStep computeAccelerationStepWithNoLeader(final LaneBasedGTU gtu, final Length.Rel maxDistance,
        final Speed speedLimit) throws GTUException;

    /**
     * Compute the acceleration that would be used if the is not leader in sight.
     * @param gtu LaneBasedGTU; the GTU for which acceleration is computed
     * @param maxDistance Length.Rel; the maximum distance we can cover at the current time, e.g. as the result of a lane drop
     * @param speedLimit Speed; the local speed limit
     * @param stepSize given step size, which can be longer or shorter than the provided step size in the algorithms.
     * @return AccelerationStep; the result of application of the GTU following model
     * @throws GTUException when the velocity of the gtu cannot be determined
     */
    AccelerationStep computeAccelerationStepWithNoLeader(final LaneBasedGTU gtu, final Length.Rel maxDistance,
        final Speed speedLimit, final Time.Rel stepSize) throws GTUException;

    /**
     * Compute the minimum <b>net</b> headway given the speed of the follower and the leader.<br>
     * At the returned headway, the follower would decelerate with it's maximum comfortable deceleration.
     * @param followerSpeed Speed; speed of the follower
     * @param leaderSpeed Speed; speed of the leader
     * @param precision Length.Rel; the required precision of the result (must be &gt; 0)
     * @param maxDistance Length.Rel; the maximum distance we can cover at the current time, e.g. as the result of a lane drop
     * @param speedLimit Speed; the local speed limit
     * @param followerMaximumSpeed Speed; the maximum speed that the follower can drive at
     * @return Length.Rel
     */
    Length.Rel minimumHeadway(Speed followerSpeed, Speed leaderSpeed, Length.Rel precision,
        final Length.Rel maxDistance, Speed speedLimit, Speed followerMaximumSpeed);

    /**
     * Return the maximum safe deceleration for use in gap acceptance models. This is the deceleration that may be enforced upon
     * a new follower due to entering a road or changing into an adjacent lane. The result shall be a <b>positive value</b>. In
     * most car following models this value is named <cite>b</cite>.
     * @return Acceleration; must be a positive value!
     */
    Acceleration getMaximumSafeDeceleration();

    /**
     * Return the standard step size of this GTU following model.
     * @return Time.Rel; the standard step size of the GTU following model
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