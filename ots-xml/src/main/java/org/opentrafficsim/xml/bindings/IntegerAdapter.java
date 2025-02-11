package org.opentrafficsim.xml.bindings;

import org.opentrafficsim.xml.bindings.types.IntegerType;

/**
 * Adapter for Integer expression type.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class IntegerAdapter extends ExpressionAdapter<Integer, IntegerType>
{

    /**
     * Constructor.
     */
    public IntegerAdapter()
    {
        //
    }

    @Override
    public IntegerType unmarshal(final String field)
    {
        if (isExpression(field))
        {
            return new IntegerType(trimBrackets(field));
        }
        return new IntegerType(Integer.valueOf(field));
    }

}
