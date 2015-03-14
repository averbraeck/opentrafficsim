package org.opentrafficsim.core.gtu.following;

import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * Container for two instances of an AccelerationStep.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 11 mrt. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class DualAccelerationStep
{
    /** AccelerationStep for the leader. */
    private final AccelerationStep leaderAccelerationStep;

    /** AccelerationStep for the (new) follower. */
    private final AccelerationStep followerAccelerationStep;

    /**
     * Create a new DualAccelerationStep.
     * @param leaderAccelerationStep AccelerationStep; the acceleration and time step size for the leader
     * @param followerAccelerationStep AccelerationStep; the acceleration and time step size for the (new) follower
     */
    public DualAccelerationStep(final AccelerationStep leaderAccelerationStep,
            final AccelerationStep followerAccelerationStep)
    {
        this.leaderAccelerationStep = leaderAccelerationStep;
        this.followerAccelerationStep = followerAccelerationStep;
    }
    
    /**
     * Retrieve the AccelerationStep for the leader GTU.
     * @return AccelerationStep; the acceleration and time step size for the leader
     */
    public final AccelerationStep getLeaderAccelerationStep()
    {
        return this.leaderAccelerationStep;
    }
    
    /**
     * Retrieve the AccelerationStep for the (new) follower GTU.
     * @return AccelerationStep; the acceleration and time step size for the (new) follower
     */
    public final AccelerationStep getFollowerAccelerationStep()
    {
        return this.followerAccelerationStep;
    }
    
    /**
     * Return the acceleration of the leader.
     * @return DoubleScalar&lt;AccelerationUnit&gt;; the acceleration of the leader
     */
    public final DoubleScalar.Abs<AccelerationUnit> getLeaderAcceleration()
    {
        return getLeaderAccelerationStep().getAcceleration();
    }

    /**
     * Return the acceleration of the follower.
     * @return DoubleScalar&lt;AccelerationUnit&gt;; the acceleration of the follower
     */
    public final DoubleScalar.Abs<AccelerationUnit> getFollowerAcceleration()
    {
        return getFollowerAccelerationStep().getAcceleration();
    }

    /**
     * Return the time up to which the result of the leader is valid.
     * @return DoubleScalar&lt;TimeUnit&gt;; the time up to which the result of the leader is valid
     */
    public final DoubleScalar.Abs<TimeUnit> getLeaderValidUntil()
    {
        return getLeaderAccelerationStep().getValidUntil();
    }

    /**
     * Return the time up to which the result of the follower is valid.
     * @return DoubleScalar&lt;TimeUnit&gt;; the time up to which the result of the follower is valid
     */
    public final DoubleScalar.Abs<TimeUnit> getFollowerValidUntil()
    {
        return getFollowerAccelerationStep().getValidUntil();
    }
    
    /** {@inheritDoc} */
    public String toString()
    {
        return "Follower: " + getFollowerAccelerationStep() + ", Leader: " + getLeaderAccelerationStep();
    }

}
