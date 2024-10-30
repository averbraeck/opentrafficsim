package org.opentrafficsim.road.network.sampling.data;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.kpi.sampling.filter.FilterDataType;
import org.opentrafficsim.road.network.sampling.GtuDataRoad;

/**
 * Filter data for GTU width.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class FilterDataWidth extends FilterDataType<Length, GtuDataRoad>
{

    /**
     * Constructor.
     */
    public FilterDataWidth()
    {
        super("width", "GTU width", Length.class);
    }

    @Override
    public Length getValue(final GtuDataRoad gtu)
    {
        return gtu.getGtu().getWidth();
    }

}
