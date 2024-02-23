package org.opentrafficsim.kpi.sampling.meta;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.kpi.interfaces.GtuData;

/**
 * Accepts trajectories with a Route included in a set in a query.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class FilterDataRoute extends FilterDataType<String>
{

    /**
     * Constructor.
     */
    public FilterDataRoute()
    {
        super("route", "Route id");
    }

    /** {@inheritDoc} */
    @Override
    public final String getValue(final GtuData gtu)
    {
        Throw.whenNull(gtu, "GTU may not be null.");
        return gtu.getRouteId();
    }

    /** {@inheritDoc} */
    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "FilterDataRoute: [id=" + getId() + "]";
    }

}
