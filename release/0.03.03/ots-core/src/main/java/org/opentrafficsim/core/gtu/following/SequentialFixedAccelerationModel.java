package org.opentrafficsim.core.gtu.following;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;

/**
 * Extended version of FixedAccelerationModel. The addition is that this GTUFollowingModel stores a series of acceleration and
 * duration values. Mostly used for testing.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version 6 feb. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SequentialFixedAccelerationModel extends AbstractGTUFollowingModel
{
    /** The list of result values of this SequentialFixedAccelerationModel. */
    private final List<FixedAccelerationModel> steps = new ArrayList<FixedAccelerationModel>();

    /** The simulator engine. */
    private final OTSDEVSSimulatorInterface simulator;

    /**
     * Construct a SequentialFixedAccelerationModel with empty list of FixedAccelerationModel steps.
     * @param simulator DEVSSimulator; the simulator (needed to obtain the current simulation time)
     */
    public SequentialFixedAccelerationModel(final OTSDEVSSimulatorInterface simulator)
    {
        this.simulator = simulator;
    }

    /**
     * Construct a SequentialFixedAccelerationModel and load it with a list of FixedAccelerationModel steps.
     * @param simulator DEVSSimulator; the simulator (needed to obtain the current simulation time)
     * @param steps Set&lt;FixedAccelerationModel&gt;; the list of FixedAccelerationModel steps.
     */
    public SequentialFixedAccelerationModel(final OTSDEVSSimulatorInterface simulator,
        final Set<FixedAccelerationModel> steps)
    {
        this(simulator);
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
     * @return DoubleScalar.Abs&lt;TimeUnit&gt;
     */
    public final Time.Abs timeAfterCompletionOfStep(final int index)
    {
        Time.Abs sum = new Time.Abs(0, SECOND);
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
    public final Acceleration.Abs computeAcceleration(final Speed.Abs followerSpeed,
        final Speed.Abs followerMaximumSpeed, final Speed.Abs leaderSpeed, final Length.Rel headway,
        final Speed.Abs speedLimit)
    {
        return getAccelerationModel().getAcceleration();
    }

    /** {@inheritDoc} */
    @Override
    public final Acceleration.Abs maximumSafeDeceleration()
    {
        return new Acceleration.Abs(2, METER_PER_SECOND_2);
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

}
