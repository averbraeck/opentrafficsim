package org.opentrafficsim.car.lanechanging;

import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.vector.DoubleVector;

/**
 * The altruistic driver changes lane when that is beneficial for all drivers.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 5 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class Altruistic extends AbstractLaneChangeModel
{

    /** {@inheritDoc} */
    @Override
    public final DoubleScalar.Abs<AccelerationUnit> applyDriverPersonality(
        final DoubleVector.Abs.Dense<AccelerationUnit> accelerations)
    {
        try
        {
            return new DoubleScalar.Abs<AccelerationUnit>(accelerations.getInUnit(0) + accelerations.getInUnit(1),
                accelerations.getUnit());
        }
        catch (ValueException exception)
        {
            throw new Error("Cannot happen");
        }
    }

}
