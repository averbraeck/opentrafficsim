package org.opentrafficsim.core.gtu.lane.changing;

import org.opentrafficsim.core.gtu.following.DualAccelerationStep;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * The egoistic drive changes lane when this yields is personal advantage (totally ignoring any disadvantage to others).
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
    /** {@inheritDoc} */
    @Override
    public DoubleScalar.Abs<AccelerationUnit> applyDriverPersonality(final DualAccelerationStep accelerations)
    {
        // The egoistic driver only looks at the effects on him-/herself.
        return accelerations.getLeaderAcceleration();
    }

}
