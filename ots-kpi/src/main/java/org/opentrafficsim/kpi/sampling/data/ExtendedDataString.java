package org.opentrafficsim.kpi.sampling.data;

import org.opentrafficsim.kpi.interfaces.GtuData;

/**
 * Extended data type for string values.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <G> gtu data type
 */
public abstract class ExtendedDataString<G extends GtuData> extends ExtendedDataList<String, G>
{

    /**
     * Constructor.
     * @param id id
     * @param description description
     */
    public ExtendedDataString(final String id, final String description)
    {
        super(id, description, String.class);
    }

    /** {@inheritDoc} */
    @Override
    public String parseValue(final String string)
    {
        return string;
    }

}
