package org.opentrafficsim.xml.bindings;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.xml.bindings.types.IntegerType;

/**
 * Adapter for Integer expression type (positive).
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class PositiveIntegerAdapter extends ExpressionAdapter<Integer, IntegerType>
{

    /** {@inheritDoc} */
    @Override
    public IntegerType unmarshal(final String field)
    {
        if (isExpression(field))
        {
            return new IntegerType(trimBrackets(field));
        }
        Integer value = Integer.valueOf(field);
        Throw.when(value < 1, IllegalArgumentException.class, "PositiveInteger value %s is not a positive value.", value);
        return new IntegerType(value);
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(final IntegerType value)
    {
        Throw.when(!value.isExpression() && value.getValue() < 1, IllegalArgumentException.class,
                "PositiveInteger value %s is not a positive value.", value.getValue());
        return super.marshal(value);
    }

}
