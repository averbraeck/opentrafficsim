package org.opentrafficsim.road.gtu.lane.tactical.following;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.Parameters;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionIterable;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.network.speed.SpeedLimitInfo;

/**
 * Extended version of FixedAccelerationModel. The addition is that this GtuFollowingModel stores a series of acceleration and
 * duration values. Mostly used for testing.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 */
public class SequentialFixedAccelerationModel extends AbstractGtuFollowingModelMobil implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150206L;

    /** The list of result values of this SequentialFixedAccelerationModel. */
    private final List<FixedAccelerationModel> steps = new ArrayList<>();

    /** The simulator engine. */
    private final OtsSimulatorInterface simulator;

    /** The maximum safe deceleration. */
    private final Acceleration maximumSafeDeceleration;

    /**
     * Construct a SequentialFixedAccelerationModel with empty list of FixedAccelerationModel steps.
     * @param simulator the simulator (needed to obtain the current simulation time)
     * @param maximumSafeDeceleration specified maximum safe deceleration
     */
    public SequentialFixedAccelerationModel(final OtsSimulatorInterface simulator, final Acceleration maximumSafeDeceleration)
    {
        this.simulator = simulator;
        this.maximumSafeDeceleration = maximumSafeDeceleration;
    }

    /**
     * Construct a SequentialFixedAccelerationModel and load it with a list of FixedAccelerationModel steps.
     * @param simulator the simulator (needed to obtain the current simulation time)
     * @param maximumSafeDeceleration specified maximum safe deceleration
     * @param steps the list of FixedAccelerationModel steps.
     */
    public SequentialFixedAccelerationModel(final OtsSimulatorInterface simulator, final Acceleration maximumSafeDeceleration,
            final Set<FixedAccelerationModel> steps)
    {
        this(simulator, maximumSafeDeceleration);
        this.steps.addAll(steps);
    }

    /**
     * Add one FixedAccelerationModel step to this SequentialFixedAccelerationModel.
     * @param step the step to add
     * @return this modified SequentialFixedAccelerationModel
     */
    public final SequentialFixedAccelerationModel addStep(final FixedAccelerationModel step)
    {
        this.steps.add(step);
        return this;
    }

    /**
     * Retrieve the number of FixedAccelerationModel steps in this SequentialFixedAccelerationModel.
     * @return the number of steps in this SequentialFixedAccelerationModel
     */
    public final int size()
    {
        return this.steps.size();
    }

    /**
     * Retrieve one FixedAccelerationModel step.
     * @param index the index of the retrieved FixedAccelerationModel step
     * @return FixedAccelerationModel
     */
    public final FixedAccelerationModel get(final int index)
    {
        return this.steps.get(index);
    }

    /**
     * Retrieve the simulation time at the end of the Nth step of this SequentialFixedAccelerationModel.
     * @param index the step
     * @return Time
     */
    public final Time timeAfterCompletionOfStep(final int index)
    {
        Time sum = new Time(0, TimeUnit.DEFAULT);
        for (int i = 0; i <= index; i++)
        {
            sum = sum.plus(this.steps.get(i).getDuration());
        }
        return sum;
    }

    /** Maximum error of the simulator clock. */
    private static final double MAXIMUMTIMEERROR = 0.001; // 1 millisecond

    /**
     * Find the FixedAccelerationModel that starts at the current simulator time.
     * @return the FixedAccelerationModel that starts at the current simulator time
     */
    private FixedAccelerationModel getAccelerationModel()
    {
        Time when = this.simulator.getSimulatorAbsTime();
        double remainingTime = when.getSI();
        for (FixedAccelerationModel step : this.steps)
        {
            if (remainingTime < -MAXIMUMTIMEERROR)
            {
                throw new Error("FixedSequentialAcceleration does not have a result for " + when);
            }
            if (remainingTime < MAXIMUMTIMEERROR)
            {
                return step;
            }
            remainingTime -= step.getDuration().getSI();
        }
        throw new Error("FixedSequentialAcceleration does not have a result for " + when);
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration computeAcceleration(final Speed followerSpeed, final Speed followerMaximumSpeed,
            final Speed leaderSpeed, final Length headway, final Speed speedLimit)
    {
        return getAccelerationModel().getAcceleration();
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration computeAcceleration(final Speed followerSpeed, final Speed followerMaximumSpeed,
            final Speed leaderSpeed, final Length headway, final Speed speedLimit, final Duration stepSize)
    {
        // TODO incorporate stepSize
        return getAccelerationModel().getAcceleration();
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration getMaximumSafeDeceleration()
    {
        return this.maximumSafeDeceleration;
    }

    /** {@inheritDoc} */
    @Override
    public final Duration getStepSize()
    {
        return getAccelerationModel().getStepSize();
    }

    /** {@inheritDoc} */
    @Override
    public final String getName()
    {
        return "FSAM";
    }

    /** {@inheritDoc} */
    @Override
    public final String getLongName()
    {
        return "Fixed sequential acceleration model";
    }

    /** {@inheritDoc} */
    @Override
    public final void setA(final Acceleration a)
    {
        //
    }

    /** {@inheritDoc} */
    @Override
    public final void setT(final Duration t)
    {
        //
    }

    /** {@inheritDoc} */
    @Override
    public final void setFspeed(final double fSpeed)
    {
        //
    }

    // The following is inherited from CarFollowingModel

    /** {@inheritDoc} */
    @Override
    public final Speed desiredSpeed(final Parameters parameters, final SpeedLimitInfo speedInfo) throws ParameterException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Length desiredHeadway(final Parameters parameters, final Speed speed) throws ParameterException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration followingAcceleration(final Parameters parameters, final Speed speed,
            final SpeedLimitInfo speedInfo, final PerceptionIterable<? extends Headway> leaders) throws ParameterException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "SequentialFixedAccelerationModel [steps=" + this.steps + ", maximumSafeDeceleration="
                + this.maximumSafeDeceleration + "]";
    }

}
