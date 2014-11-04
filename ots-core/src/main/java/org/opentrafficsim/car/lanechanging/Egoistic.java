package org.opentrafficsim.car.lanechanging;

import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.vector.DoubleVector;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Sep 19, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class Egoistic extends AbstractLaneChangeModel
{
    /**
     * This class should never be instantiated.
     */
    private Egoistic()
    {
        // This class should never be instantiated.
    }

    /** {@inheritDoc} */
    @Override
    public Abs<AccelerationUnit> applyDriverPersonality(final DoubleVector.Abs.Dense<AccelerationUnit> accelerations)
    {
        try
        {
            // The egoistic driver only looks at the effects on him-/herself.
            return accelerations.get(0);
        }
        catch (ValueException exception)
        {
            throw new Error("Cannot happen");
        }
    }

}
