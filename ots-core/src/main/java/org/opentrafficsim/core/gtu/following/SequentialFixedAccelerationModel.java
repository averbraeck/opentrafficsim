package org.opentrafficsim.core.gtu.following;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar;

/**
 * Extended version of FixedAccelerationModel. The addition is that this GTUFollowingModel stores a series of
 * acceleration and duration values.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 6 feb. 2015 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SequentialFixedAccelerationModel extends AbstractGTUFollowingModel
{
    /** The list of result values of this SequentialFixedAccelerationModel. */
    private final List<FixedAccelerationModel> steps = new ArrayList<FixedAccelerationModel>();

    /**
     * Construct a SequentialFixedAccelerationModel with empty list of FixedAccelerationModel steps.
     */
    public SequentialFixedAccelerationModel()
    {
    }

    /**
     * Construct a SequentialFixedAccelerationModel and load it with a list of FixedAccelerationModel steps.
     * @param steps Set&lt;FixedAccelerationModel&gt;; the list of FixedAccelerationModel steps.
     */
    public SequentialFixedAccelerationModel(final Set<FixedAccelerationModel> steps)
    {
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
    public final DoubleScalar.Abs<TimeUnit> timeAfterCompletionOfStep(final int index)
    {
        MutableDoubleScalar.Abs<TimeUnit> sum = new MutableDoubleScalar.Abs<TimeUnit>(0, TimeUnit.SECOND);
        for (int i = 0; i <= index; i++)
        {
            sum.incrementBy(this.steps.get(i).getDuration());
        }
        return sum.immutable();
    }

    /** Maximum error of the simulator clock. */
    private static final double MAXIMUMTIMEERROR = 0.001; // 1 millisecond

    /**
     * Find the AccelerationStep that starts at the current simulator time.
     * @param when DoubleScalar.Abs&lt;TimeUnit&gt;; the current simulator time
     * @return AccelerationStep; the AccelerationStep that starts at the current simulator time
     * @throws RemoteException on communications failure
     * @throws NetworkException on network inconsistency
     */
    private AccelerationStep getAccelerationStep(final DoubleScalar.Abs<TimeUnit> when) throws RemoteException,
            NetworkException
    {
        double remainingTime = when.getSI();
        for (FixedAccelerationModel step : this.steps)
        {
            if (remainingTime < -MAXIMUMTIMEERROR)
            {
                throw new Error("FixedSequentialAcceleration does not have a result for " + when);
            }
            if (remainingTime < MAXIMUMTIMEERROR)
            {
                return new AccelerationStep(step.getAcceleration(), DoubleScalar.plus(when, step.getDuration())
                        .immutable());
            }
        }
        throw new Error("FixedSequentialAcceleration does not have a result for " + when);
    }

    /** {@inheritDoc} */
    @Override
    public final AccelerationStep computeAcceleration(final LaneBasedGTU<?> follower,
            final DoubleScalar.Abs<SpeedUnit> leaderSpeed, final DoubleScalar.Rel<LengthUnit> headway,
            final DoubleScalar.Abs<SpeedUnit> speedLimit) throws RemoteException, NetworkException
    {
        return getAccelerationStep(follower.getSimulator().getSimulatorTime().get());
    }

    /** {@inheritDoc} */
    @Override
    public AccelerationStep computeAccelerationWithNoLeader(LaneBasedGTU<?> gtu, Abs<SpeedUnit> speedLimit)
            throws RemoteException
    {
        try
        {
            return getAccelerationStep(gtu.getSimulator().getSimulatorTime().get());
        }
        catch (NetworkException networkException)
        {
            throw new Error("Caught an impossible NetworkException: " + networkException);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<AccelerationUnit> maximumSafeDeceleration()
    {
        return new DoubleScalar.Abs<AccelerationUnit>(2, AccelerationUnit.METER_PER_SECOND_2);
    }

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Rel<TimeUnit> getStepSize()
    {
        // We'll have to fake this one (the step size may not be constant); return the step size of the first entry
        return this.steps.get(0).getStepSize();
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
