package org.opentrafficsim.core.gtu.lane.changing;

import org.opentrafficsim.core.gtu.following.DualAccelerationStep;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * The altruistic driver changes lane when that is beneficial for all drivers.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author: pknoppers
 *          $, initial version 5 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class Altruistic extends AbstractLaneChangeModel
{

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<AccelerationUnit> applyDriverPersonality(final DualAccelerationStep accelerationSteps)
    {
        // The unit of the result is the acceleration unit of the leader acceleration.
        // Discussion. The altruistic driver personality in Treiber adds two accelerations together. This reduces the
        // "sensitivity" for keep lane, keep right and follow route incentives.
        // This implementation returns the average of the two in order to avoid this sensitivity problem.
        AccelerationUnit unit = accelerationSteps.getLeaderAcceleration().getUnit();
        return new DoubleScalar.Abs<AccelerationUnit>(
                (accelerationSteps.getLeaderAcceleration().getInUnit() + accelerationSteps.getFollowerAcceleration()
                        .getInUnit(unit)) / 2, unit);
    }

    /** {@inheritDoc} */
    @Override
    public final String getName()
    {
        return "Altruistic";
    }

    /** {@inheritDoc} */
    @Override
    public final String getLongName()
    {
        return "Altruistic lane change model (as described by Treiber).";
    }

}
