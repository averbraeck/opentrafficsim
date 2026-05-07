package org.opentrafficsim.kpi.sampling.filter;

import java.util.Optional;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.kpi.interfaces.GtuData;

/**
 * Accepts trajectories with a route included in a set in a query.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Peter Knoppers
 * @author Wouter Schakel
 */
public class FilterDataRoute extends FilterDataType<String, GtuData>
{

    /**
     * Constructor.
     */
    public FilterDataRoute()
    {
        super("route", "Route id", String.class);
    }

    @Override
    public final Optional<String> getValue(final GtuData gtu)
    {
        Throw.whenNull(gtu, "GTU may not be null.");
        return Optional.of(gtu.getRouteId());
    }

    @Override
    public String toString()
    {
        return "FilterDataRoute";
    }

}
