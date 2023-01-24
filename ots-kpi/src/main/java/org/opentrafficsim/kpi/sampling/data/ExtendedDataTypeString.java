package org.opentrafficsim.kpi.sampling.data;

import org.opentrafficsim.kpi.interfaces.GtuData;

/**
 * Extended data type for string values.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <G> gtu data type
 */
public abstract class ExtendedDataTypeString<G extends GtuData> extends ExtendedDataTypeList<String, G>
{

    /**
     * Constructor.
     * @param id String; id
     * @param description String; description
     */
    public ExtendedDataTypeString(final String id, final String description)
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
