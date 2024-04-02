package org.opentrafficsim.xml.bindings;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.xml.bindings.types.DoubleType;

/**
 * Adapter for Double expression type (unit interval).
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class DoubleUnitIntervalAdapter extends ExpressionAdapter<Double, DoubleType>
{

    /** {@inheritDoc} */
    @Override
    public DoubleType unmarshal(final String field)
    {
        if (isExpression(field))
        {
            return new DoubleType(trimBrackets(field));
        }
        double value = Double.valueOf(field);
        Throw.when(value < 0.0 || value > 1.0, IllegalArgumentException.class,
                "DoubleUnitInterval value %s is not in unit interval.", value);
        return new DoubleType(value);
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(final DoubleType value)
    {
        Throw.when(!value.isExpression() && (value.getValue() < 0.0 || value.getValue() > 1.0), IllegalArgumentException.class,
                "DoubleUnitInterval value %s is not in unit interval.", value.getValue());
        return super.marshal(value);
    }

}
