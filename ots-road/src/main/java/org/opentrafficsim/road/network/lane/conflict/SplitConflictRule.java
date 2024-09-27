package org.opentrafficsim.road.network.lane.conflict;

/**
 * Conflict rule for split conflicts.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class SplitConflictRule implements ConflictRule
{

    /** {@inheritDoc} */
    @Override
    public final ConflictPriority determinePriority(final Conflict conflict)
    {
        return ConflictPriority.SPLIT;
    }

}
