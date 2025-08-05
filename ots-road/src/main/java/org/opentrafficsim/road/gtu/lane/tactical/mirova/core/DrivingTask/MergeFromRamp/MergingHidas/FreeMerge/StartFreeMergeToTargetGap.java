package org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.FreeMerge;

import java.util.Iterator;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.jheaps.MergeableAddressableHeap;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlanException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.plan.operational.SimpleOperationalPlan;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.*;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.DrivingTask;
import org.opentrafficsim.road.gtu.lane.tactical.mirova.core.DrivingTask.MergeFromRamp.MergingHidas.MergingHidas;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;

/**
 * Represents the initial state for a free merge maneuver into a target gap.
 * <p>
 * This state is responsible for initiating the merging process by updating the operational values of the merge gap and
 * calculating the required acceleration to position the vehicle within the target gap. It also determines the next state based
 * on the progress of the merge.
 * </p>
 * <h2>Responsibilities:</h2>
 * <ul>
 * <li>Update operational values of the merge gap, such as distances, speeds, and accelerations of the leading and following
 * vehicles in the gap.</li>
 * <li>Calculate the required acceleration to reach the target gap.</li>
 * <li>Transition to the next state based on the progress of the merge.</li>
 * </ul>
 * <h2>Transitions:</h2>
 * <ul>
 * <li>To {@code AccelerateToTargetGap} if the merge progresses as planned.</li>
 * <li>To {@code AbortFreeMergeToTargetGap} if the merging gap becomes invalid.</li>
 * </ul>
 */
public class StartFreeMergeToTargetGap extends ActionState
{
    /**
     * The driving task associated with the merge maneuver.
     */
    protected MergingHidas drivingTask;

    /**
     * Constructs the initial state for a free merge maneuver.
     * @param drivingTask the driving task associated with the merge maneuver
     */
    public StartFreeMergeToTargetGap(final MergingHidas drivingTask)
    {
        super(drivingTask);
    }

    @Override
    public SimpleOperationalPlan update()
            throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException
    {
        this.updateMergeGapOperationalValues();
        this.calculateMergeAcceleration();
        return super.update();
    }

    @Override
    public SimpleOperationalPlan executeControl() throws ParameterException, OperationalPlanException
    {
        return null; // No execution in this state
    }

    @Override
    public void next() throws OperationalPlanException, ParameterException, NullPointerException, IllegalArgumentException
    {
        this.drivingTask.getAbstractMirovaVehicle().setCurrentActionState(new AccelerateToTargetGap(this.drivingTask));
    }

    /**
     * Checks if the merging gap is still valid and transitions to an abort state if necessary.
     * <p>
     * This method verifies whether the gap follower is still following the correct gap leader. If not, it transitions to the
     * {@code AbortFreeMergeToTargetGap} state.
     * </p>
     * @throws ParameterException
     */
    @Override
    public void abort() throws ParameterException
    {
        Iterator<LaneBasedGtu> followers =
                this.drivingTask.getNeighborsPerception().getFollowers(RelativeLane.LEFT).underlying();

        LaneBasedGtu currentGapLeader = followers.hasNext() ? followers.next() : null;
        LaneBasedGtu currentGapFollower = followers.hasNext() ? followers.next() : null;

        while (currentGapLeader != null && currentGapFollower != null)
        {
            if (currentGapFollower.getFullId() == this.drivingTask.getGapFollowerGtu().getFullId())
            {
                if (currentGapLeader.getFullId() != this.drivingTask.getGapLeaderGtu().getFullId())
                {
                    // The gap follower is not following the current gap leader, abort the merge
                    this.drivingTask.getAbstractMirovaVehicle()
                            .setCurrentActionState(new EndFreeMergeToTargetGap(this.drivingTask));
                }
                else
                {
                    if (this.drivingTask.checkFreeMerge(this.drivingTask.getGapLeaderHeadway(),
                            this.drivingTask.getGapFollowerHeadway()) == false)
                        // The gap is not valid anymore, abort the merge
                    {
                        this.drivingTask.getAbstractMirovaVehicle()
                                .setCurrentActionState(new EndFreeMergeToTargetGap(this.drivingTask));
                    }
                    else
                    {
                        return; // The gap is valid, continue the merge
                    }
                }

            }
            currentGapLeader = currentGapFollower;
            currentGapFollower = followers.hasNext() ? followers.next() : null;
        }
        // Gap is out of sight or invalid, abort the merge
        this.drivingTask.getAbstractMirovaVehicle().setCurrentActionState(new EndFreeMergeToTargetGap(this.drivingTask));
    }

    // --- Helper methods for vehicle data access (implement as needed) ---

    protected void updateMergeGapOperationalValues()
    {
        // Implement logic to update operational values for the merge gap
    }

    protected void calculateMergeAcceleration()
    {
        // Implement logic to calculate required acceleration for merging
    }

    // Placeholder methods for vehicle data access (replace with actual implementation)
    protected Integer getGapFollowerLeadTargetNo()
    {
        return 0;
    }

    protected Integer getGapLeaderId()
    {
        return 0;
    }

    protected int getGapFollowerLaneLinkNo()
    {
        return 0;
    }

    protected int getCurrentLinkId()
    {
        return 0;
    }

    protected double getGapFollowerPos()
    {
        return 0.0;
    }

    protected double getCurrentPosition()
    {
        return 0.0;
    }

    /**
     * Calculates the required acceleration for the ego vehicle to merge into the target gap within a specified time.
     * <p>
     * This method determines the minimum acceleration needed so that the ego vehicle can reach and stay within the merge gap
     * between the gap follower and gap leader, given a desired merge duration. It considers the current speeds and positions
     * of the ego vehicle, the gap follower, and the gap leader, and ensures that the acceleration is sufficient to satisfy
     * both the start and end boundaries of the gap.
     * </p>
     *
     * <p>
     * The calculation is based on kinematic equations, converting all speeds to SI units (m/s) and using the following logic:
     * <ul>
     *   <li>Compute the required acceleration to reach the start of the gap within the given time, considering the gap follower's speed.</li>
     *   <li>Compute the required acceleration to reach the end of the gap within the given time, considering the gap leader's speed and length.</li>
     *   <li>Return the maximum of these two accelerations to ensure both constraints are satisfied.</li>
     * </ul>
     * </p>
     *
     * @param timeToMerge the desired time to complete the merge, as a {@link Duration} (in seconds)
     * @return the required acceleration as an {@link Acceleration} (in m/s²) to achieve the merge within the given time
     */
    protected Acceleration calculateAccelerationForMergeTime(final Duration timeToMerge)
    {
        // Convert speeds from km/h to m/s
        Speed egoSpeed = getEgoSpeed();
        Speed followerSpeed = getGapFollowerSpeed();
        Speed leaderSpeed = getGapLeaderSpeed();

        // Start and end positions of the gap
        // Distances are positive and getting smaller, if vehicles on main roads are faster than Ego
        Length distanceToGapStart = getDistanceToGapStart();
        Length distanceToGapEnd = getDistanceToGapEnd();

        // Calculate acceleration for start and end of the gap
        Acceleration accelerationForStartGap = Acceleration
                .instantiateSI(2 * (distanceToGapStart.si + egoSpeed.si * timeToMerge.si - followerSpeed.si * timeToMerge.si)
                        / (timeToMerge.si * timeToMerge.si));

        Acceleration accelerationForEndGap = Acceleration
                .instantiateSI(2 * (distanceToGapEnd.si + egoSpeed.si * timeToMerge.si - leaderSpeed.si * timeToMerge.si)
                        / (timeToMerge.si * timeToMerge.si));

        // Choose the acceleration that satisfies both conditions
        return Acceleration.max(accelerationForStartGap, accelerationForEndGap);
    }

    protected Speed getEgoSpeed()
    {
        return this.drivingTask.getEgoPerception().getSpeed();
    }

    protected Speed getGapFollowerSpeed()
    {
        return this.drivingTask.getGapFollowerHeadway().getSpeed();
    }

    protected Speed getGapLeaderSpeed()
    {
        return this.drivingTask.getGapLeaderHeadway().getSpeed();
    }

    protected Length getDistanceToGapStart()
    {
        return this.drivingTask.getGapFollowerHeadway().getDistance();
    }

    protected Length getDistanceToGapEnd()
    {
        return this.drivingTask.getGapLeaderHeadway().getDistance().plus(this.drivingTask.getGapLeaderHeadway().getLength())
                .plus(this.drivingTask.getEgoPerception().getLength());
    }

}
