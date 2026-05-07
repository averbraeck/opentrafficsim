package org.opentrafficsim.road.network.conflict;

/**
 * Conflict rule for split conflicts.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 * @author Wouter Schakel
 */
public class SplitConflictRule implements ConflictRule
{

    /**
     * Constructor.
     */
    public SplitConflictRule()
    {
        //
    }

    @Override
    public final ConflictPriority determinePriority(final Conflict conflict)
    {
        return ConflictPriority.SPLIT;
    }

}
