package org.opentrafficsim.kpi.sampling.filter;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.kpi.interfaces.GtuData;

/**
 * Accepts trajectories with an origin node included in a set in a query.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class FilterDataOrigin extends FilterDataType<String, GtuData>
{

    /**
     * Constructor.
     */
    public FilterDataOrigin()
    {
        super("origin", "Origin node id", String.class);
    }

    @Override
    public final String getValue(final GtuData gtu)
    {
        Throw.whenNull(gtu, "GTU may not be null.");
        return gtu.getOriginId();
    }

    @Override
    public String toString()
    {
        return "FilterDataOrigin";
    }

}
