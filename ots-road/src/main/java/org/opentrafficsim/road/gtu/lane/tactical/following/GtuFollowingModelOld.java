package org.opentrafficsim.road.gtu.lane.tactical.following;

import java.util.Collection;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;

/**
 * GTU following model interface. <br>
 * GTU following models following this interface compute an acceleration.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public interface GtuFollowingModelOld extends CarFollowingModel
{
    /**
     * Compute the acceleration that would be used to follow a leader.<br>
     * @param gtu the GTU for which acceleration is computed
     * @param leaderSpeed the speed of the leader
     * @param headway the headway of the leader
     * @param maxDistance the maximum distance we can cover at the current time, e.g. as the result of a lane drop
     * @param speedLimit the local speed limit
     * @return the result of application of the GTU following model
     * @throws GtuException when the speed of the gtu cannot be determined
     */
    AccelerationStep computeAccelerationStep(LaneBasedGtu gtu, Speed leaderSpeed, Length headway, Length maxDistance,
            Speed speedLimit) throws GtuException;

    /**
     * Compute the acceleration that would be used to follow a leader.<br>
     * @param gtu the GTU for which acceleration is computed
     * @param leaderSpeed the speed of the leader
     * @param headway the headway of the leader
     * @param maxDistance the maximum distance we can cover at the current time, e.g. as the result of a lane drop
     * @param speedLimit the local speed limit
     * @param stepSize given step size, which can be longer or shorter than the provided step size in the algorithms.
     * @return the result of application of the GTU following model
     * @throws GtuException when the speed of the gtu cannot be determined
     */
    AccelerationStep computeAccelerationStep(LaneBasedGtu gtu, Speed leaderSpeed, Length headway, Length maxDistance,
            Speed speedLimit, Duration stepSize) throws GtuException;

    /**
     * Compute the acceleration that would be used to follow a leader.<br>
     * @param followerSpeed the speed of the follower at the current time
     * @param followerMaximumSpeed the maximum speed that the follower is capable of driving at
     * @param leaderSpeed the speed of the follower at the current time
     * @param headway the <b>net</b> headway (distance between the front of the follower to the rear of the leader) at the
     *            current time, or the maximum distance we can cover at the current time, e.g. as the result of a lane drop
     * @param speedLimit the local speed limit
     * @return the acceleration (or, if negative, deceleration) resulting from application of the GTU following model
     */
    Acceleration computeAcceleration(Speed followerSpeed, Speed followerMaximumSpeed, Speed leaderSpeed, Length headway,
            Speed speedLimit);

    /**
     * Compute the acceleration that would be used to follow a leader.<br>
     * @param followerSpeed the speed of the follower at the current time
     * @param followerMaximumSpeed the maximum speed that the follower is capable of driving at
     * @param leaderSpeed the speed of the follower at the current time
     * @param headway the <b>net</b> headway (distance between the front of the follower to the rear of the leader) at the
     *            current time, or the maximum distance we can cover at the current time, e.g. as the result of a lane drop
     * @param speedLimit the local speed limit
     * @param stepSize given step size, which can be longer or shorter than the provided step size in the algorithms.
     * @return the acceleration (or, if negative, deceleration) resulting from application of the GTU following model
     */
    Acceleration computeAcceleration(Speed followerSpeed, Speed followerMaximumSpeed, Speed leaderSpeed, Length headway,
            Speed speedLimit, Duration stepSize);

    /**
     * Compute the acceleration that would be used to follow a leader.<br>
     * @param followerSpeed the speed of the follower at the current time
     * @param leaderSpeed the speed of the follower at the current time
     * @param headway the <b>net</b> headway (distance between the front of the follower to the rear of the leader) at the
     *            current time, or the maximum distance we can cover at the current time, e.g. as the result of a lane drop
     * @param speedLimit the local speed limit
     * @param currentTime to be used to determine the validity of the AccelerationStep
     * @return the acceleration (or, if negative, deceleration) resulting from application of the GTU following model
     */
    AccelerationStep computeAccelerationStep(Speed followerSpeed, Speed leaderSpeed, Length headway, Speed speedLimit,
            Time currentTime);

    /**
     * Compute the acceleration that would be used to follow a leader.<br>
     * @param followerSpeed the speed of the follower at the current time
     * @param leaderSpeed the speed of the follower at the current time
     * @param headway the <b>net</b> headway (distance between the front of the follower to the rear of the leader) at the
     *            current time, or the maximum distance we can cover at the current time, e.g. as the result of a lane drop
     * @param speedLimit the local speed limit
     * @param currentTime to be used to determine the validity of the AccelerationStep
     * @param stepSize given step size, which can be longer or shorter than the provided step size in the algorithms.
     * @return the acceleration (or, if negative, deceleration) resulting from application of the GTU following model
     */
    AccelerationStep computeAccelerationStep(Speed followerSpeed, Speed leaderSpeed, Length headway, Speed speedLimit,
            Time currentTime, Duration stepSize);

    /**
     * Compute the lowest accelerations (or most severe decelerations) that would be used if a referenceGTU is present
     * (inserted, or not removed) in a set of other GTUs.<br>
     * If any GTU in the set of otherGTUs has a null headway (indicating that the other GTU is in fact parallel to the
     * referenceGTU), prohibitive decelerations shall be returned.<br>
     * Two AccelerationStep values are returned in a DualAccelerationStep.<br>
     * or should slow down for a crossing from accelerating to unsafe speeds.
     * @param gtu the GTU for which the accelerations are computed
     * @param otherHeadways the other GTUs. A negative headway value indicates that the other GTU is a follower. NB. If the
     *            referenceGTU is contained in this Collection, it is ignored.
     * @param maxDistance the maximum distance we can cover at the current time, e.g. as the result of a lane drop
     * @param speedLimit the local speed limit
     * @return the result with the lowest accelerations (or most severe decelerations) of application of the GTU following model
     *         of the referenceGTU for each leader and follower
     * @throws GtuException when the speed of the gtu cannot be determined
     */
    DualAccelerationStep computeDualAccelerationStep(LaneBasedGtu gtu, Collection<Headway> otherHeadways, Length maxDistance,
            Speed speedLimit) throws GtuException;

    /**
     * Compute the lowest accelerations (or most severe decelerations) that would be used if a referenceGTU is present
     * (inserted, or not removed) in a set of other GTUs.<br>
     * If any GTU in the set of otherGTUs has a null headway (indicating that the other GTU is in fact parallel to the
     * referenceGTU), prohibitive decelerations shall be returned.<br>
     * Two AccelerationStep values are returned in a DualAccelerationStep.<br>
     * or should slow down for a crossing from accelerating to unsafe speeds.
     * @param gtu the GTU for which the accelerations are computed
     * @param otherHeadways the other GTUs. A negative headway value indicates that the other GTU is a follower. NB. If the
     *            referenceGTU is contained in this Collection, it is ignored.
     * @param maxDistance the maximum distance we can cover at the current time, e.g. as the result of a lane drop
     * @param speedLimit the local speed limit
     * @param stepSize given step size, which can be longer or shorter than the provided step size in the algorithms.
     * @return the result with the lowest accelerations (or most severe decelerations) of application of the GTU following model
     *         of the referenceGTU for each leader and follower
     * @throws GtuException when the speed of the gtu cannot be determined
     */
    DualAccelerationStep computeDualAccelerationStep(LaneBasedGtu gtu, Collection<Headway> otherHeadways, Length maxDistance,
            Speed speedLimit, Duration stepSize) throws GtuException;

    /**
     * Compute the acceleration that would be used if the is not leader in sight.
     * @param gtu the GTU for which acceleration is computed
     * @param maxDistance the maximum distance we can cover at the current time, e.g. as the result of a lane drop
     * @param speedLimit the local speed limit
     * @return the result of application of the GTU following model
     * @throws GtuException when the speed of the gtu cannot be determined
     */
    AccelerationStep computeAccelerationStepWithNoLeader(LaneBasedGtu gtu, Length maxDistance, Speed speedLimit)
            throws GtuException;

    /**
     * Compute the acceleration that would be used if the is not leader in sight.
     * @param gtu the GTU for which acceleration is computed
     * @param maxDistance the maximum distance we can cover at the current time, e.g. as the result of a lane drop
     * @param speedLimit the local speed limit
     * @param stepSize given step size, which can be longer or shorter than the provided step size in the algorithms.
     * @return the result of application of the GTU following model
     * @throws GtuException when the speed of the gtu cannot be determined
     */
    AccelerationStep computeAccelerationStepWithNoLeader(LaneBasedGtu gtu, Length maxDistance, Speed speedLimit,
            Duration stepSize) throws GtuException;

    /**
     * Compute the minimum <b>net</b> headway given the speed of the follower and the leader.<br>
     * At the returned headway, the follower would decelerate with it's maximum comfortable deceleration.
     * @param followerSpeed speed of the follower
     * @param leaderSpeed speed of the leader
     * @param precision the required precision of the result (must be &gt; 0)
     * @param maxDistance the maximum distance we can cover at the current time, e.g. as the result of a lane drop
     * @param speedLimit the local speed limit
     * @param followerMaximumSpeed the maximum speed that the follower can drive at
     * @return Length
     */
    Length minimumHeadway(Speed followerSpeed, Speed leaderSpeed, Length precision, Length maxDistance, Speed speedLimit,
            Speed followerMaximumSpeed);

    /**
     * Return the maximum safe deceleration for use in gap acceptance models. This is the deceleration that may be enforced upon
     * a new follower due to entering a road or changing into an adjacent lane. The result shall be a <b>positive value</b>. In
     * most car following models this value is named <cite>b</cite>.
     * @return must be a positive value!
     */
    Acceleration getMaximumSafeDeceleration();

    /**
     * Return the standard step size of this GTU following model.
     * @return the standard step size of the GTU following model
     */
    Duration getStepSize();

    /**
     * Set value of acceleration parameter.
     * @param a value to set
     */
    void setA(Acceleration a);

    /**
     * Set value of desired headway.
     * @param t desired headway
     */
    void setT(Duration t);

    /**
     * Set value of desired speed factor.
     * @param fSpeed desired speed factor
     */
    void setFspeed(double fSpeed);

}
