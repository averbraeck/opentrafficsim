package org.opentrafficsim.xml.bindings;

import org.opentrafficsim.xml.bindings.types.BooleanType;

/**
 * Adapter for Boolean expression type.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class BooleanAdapter extends ExpressionAdapter<Boolean, BooleanType>
{

    /**
     * Constructor.
     */
    public BooleanAdapter()
    {
        //
    }

    @Override
    public BooleanType unmarshal(final String field)
    {
        if (isExpression(field))
        {
            return new BooleanType(trimBrackets(field));
        }
        return new BooleanType(Boolean.valueOf(field));
    }

}
