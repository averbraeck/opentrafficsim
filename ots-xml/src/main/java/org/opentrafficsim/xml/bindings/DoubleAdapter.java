package org.opentrafficsim.xml.bindings;

import org.opentrafficsim.xml.bindings.types.DoubleType;

/**
 * Adapter for Double expression type.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class DoubleAdapter extends ExpressionAdapter<Double, DoubleType>
{

    /**
     * Constructor.
     */
    public DoubleAdapter()
    {
        //
    }

    @Override
    public DoubleType unmarshal(final String field)
    {
        if (isExpression(field))
        {
            return new DoubleType(trimBrackets(field));
        }
        return new DoubleType(Double.valueOf(field));
    }

}
