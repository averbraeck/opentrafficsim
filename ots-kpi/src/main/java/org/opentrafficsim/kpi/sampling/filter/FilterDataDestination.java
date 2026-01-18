package org.opentrafficsim.kpi.sampling.filter;

import java.util.Optional;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.kpi.interfaces.GtuData;

/**
 * Accepts trajectories with a destination node included in a set in a query.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class FilterDataDestination extends FilterDataType<String, GtuData>
{

    /**
     * Constructor.
     */
    public FilterDataDestination()
    {
        super("destination", "Destination node id", String.class);
    }

    @Override
    public final Optional<String> getValue(final GtuData gtu)
    {
        Throw.whenNull(gtu, "GTU may not be null.");
        return Optional.of(gtu.getDestinationId());
    }

    @Override
    public String toString()
    {
        return "FilterDataDestination";
    }

}
