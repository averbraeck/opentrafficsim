package org.opentrafficsim.road.gtu.lane.tactical.following;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.SortedMap;

import org.djunits.unit.TimeUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Length.Rel;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;

/**
 * Extended version of FixedAccelerationModel. The addition is that this GTUFollowingModel stores a series of acceleration and
 * duration values. Mostly used for testing.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 1401 $, $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, by $Author: averbraeck $,
 *          initial version 6 feb. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SequentialFixedAccelerationModel extends AbstractGTUFollowingModelMobil implements Serializable
{
    /** */
    private static final long serialVersionUID = 20150206L;

    /** The list of result values of this SequentialFixedAccelerationModel. */
    private final List<FixedAccelerationModel> steps = new ArrayList<FixedAccelerationModel>();

    /** The simulator engine. */
    private final OTSDEVSSimulatorInterface simulator;

    /** The maximum safe deceleration. */
    private final Acceleration maximumSafeDeceleration;

    /**
     * Construct a SequentialFixedAccelerationModel with empty list of FixedAccelerationModel steps.
     * @param simulator DEVSSimulator; the simulator (needed to obtain the current simulation time)
     * @param maximumSafeDeceleration specified maximum safe deceleration
     */
    public SequentialFixedAccelerationModel(final OTSDEVSSimulatorInterface simulator,
        final Acceleration maximumSafeDeceleration)
    {
        this.simulator = simulator;
        this.maximumSafeDeceleration = maximumSafeDeceleration;
    }

    /**
     * Construct a SequentialFixedAccelerationModel and load it with a list of FixedAccelerationModel steps.
     * @param simulator DEVSSimulator; the simulator (needed to obtain the current simulation time)
     * @param maximumSafeDeceleration specified maximum safe deceleration
     * @param steps Set&lt;FixedAccelerationModel&gt;; the list of FixedAccelerationModel steps.
     */
    public SequentialFixedAccelerationModel(final OTSDEVSSimulatorInterface simulator,
        final Acceleration maximumSafeDeceleration, final Set<FixedAccelerationModel> steps)
    {
        this(simulator, maximumSafeDeceleration);
        this.steps.addAll(steps);
    }

    /**
     * Add one FixedAccelerationModel step to this SequentialFixedAccelerationModel.
     * @param step FixedAccelerationModel; the step to add
     * @return SequentialFixedAccelerationModel; this modified SequentialFixedAccelerationModel
     */
    public final SequentialFixedAccelerationModel addStep(final FixedAccelerationModel step)
    {
        this.steps.add(step);
        return this;
    }

    /**
     * Retrieve the number of FixedAccelerationModel steps in this SequentialFixedAccelerationModel.
     * @return int; the number of steps in this SequentialFixedAccelerationModel
     */
    public final int size()
    {
        return this.steps.size();
    }

    /**
     * Retrieve one FixedAccelerationModel step.
     * @param index int; the index of the retrieved FixedAccelerationModel step
     * @return FixedAccelerationModel
     */
    public final FixedAccelerationModel get(final int index)
    {
        return this.steps.get(index);
    }

    /**
     * Retrieve the simulation time at the end of the Nth step of this SequentialFixedAccelerationModel.
     * @param index int; the step
     * @return Time.Abs
     */
    public final Time.Abs timeAfterCompletionOfStep(final int index)
    {
        Time.Abs sum = new Time.Abs(0, TimeUnit.SI);
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
     * @return FixedAccelerationModel; the FixedAccelerationModel that starts at the current simulator time
     */
    private FixedAccelerationModel getAccelerationModel()
    {
        Time.Abs when = this.simulator.getSimulatorTime().getTime();
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
        final Speed leaderSpeed, final Length.Rel headway, final Speed speedLimit)
    {
        return getAccelerationModel().getAcceleration();
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration computeAcceleration(final Speed followerSpeed, final Speed followerMaximumSpeed,
        final Speed leaderSpeed, final Length.Rel headway, final Speed speedLimit, final Time.Rel stepSize)
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
    public final Time.Rel getStepSize()
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

    // The following is inherited from CarFollowingModel

    /** {@inheritDoc} */
    @Override
    public final Speed desiredSpeed(final BehavioralCharacteristics behavioralCharacteristics, final SpeedInfo speedInfo)
        throws ParameterException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Rel desiredHeadway(final BehavioralCharacteristics behavioralCharacteristics, final Speed speed)
        throws ParameterException
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration followingAcceleration(final BehavioralCharacteristics behavioralCharacteristics,
        final Speed speed, final SpeedInfo speedInfo, final SortedMap<Rel, Speed> leaders) throws ParameterException
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
