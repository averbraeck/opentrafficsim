package org.opentrafficsim.xml.bindings;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.xml.bindings.types.DoubleType;

/**
 * Adapter for Double expression type (positive).
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class DoublePositiveAdapter extends ExpressionAdapter<Double, DoubleType>
{

    /**
     * Constructor.
     */
    public DoublePositiveAdapter()
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
        double value = Double.valueOf(field);
        Throw.when(value <= 0.0, IllegalArgumentException.class, "DoublePositive value %s is negative or 0.", value);
        return new DoubleType(value);
    }

    @Override
    public String marshal(final DoubleType value)
    {
        Throw.when(!value.isExpression() && value.getValue() <= 0.0, IllegalArgumentException.class,
                "DoublePositive value %s is negative or 0.", value.getValue());
        return super.marshal(value);
    }

}
