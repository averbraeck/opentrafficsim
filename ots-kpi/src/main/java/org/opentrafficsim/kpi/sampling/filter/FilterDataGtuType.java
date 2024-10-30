package org.opentrafficsim.kpi.sampling.filter;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.kpi.interfaces.GtuData;

/**
 * Accepts trajectories with a GtuType included in a set in a query.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class FilterDataGtuType extends FilterDataType<String, GtuData>
{

    /**
     * Constructor.
     */
    public FilterDataGtuType()
    {
        super("gtuType", "GTU type id", String.class);
    }

    @Override
    public final String getValue(final GtuData gtu)
    {
        Throw.whenNull(gtu, "GTU may not be null.");
        return gtu.getGtuTypeId();
    }

    @Override
    @SuppressWarnings("checkstyle:designforextension")
    public String toString()
    {
        return "FilterDataGtuType: [id=" + getId() + "]";
    }

}
