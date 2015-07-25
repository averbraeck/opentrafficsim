package org.opentrafficsim.core.gtu.lane.changing;

import org.opentrafficsim.core.gtu.following.DualAccelerationStep;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * The egoistic drive changes lane when this yields is personal advantage (totally ignoring any disadvantage to others).
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$,
 *          initial version Sep 19, 2014 <br>
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

    /** {@inheritDoc} */
    @Override
    public String getName()
    {
        return "Egoistic";
    }

    /** {@inheritDoc} */
    @Override
    public String getLongName()
    {
        return "Egoistic lane change model (as described by Treiber).";
    }

}
