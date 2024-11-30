package org.opentrafficsim.xml.bindings;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.xml.bindings.types.DoubleType;

/**
 * Adapter for positive factors.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class PositiveFactorAdapter extends ExpressionAdapter<Double, DoubleType>
{

    @Override
    public DoubleType unmarshal(final String value)
    {
        if (isExpression(value))
        {
            return new DoubleType(trimBrackets(value));
        }
        double factor = value.endsWith("%") ? Double.parseDouble(value.substring(0, value.length() - 1)) / 100.0
                : Double.parseDouble(value);
        Throw.when(factor < 0.0, RuntimeException.class, "Factor %d is not positive.", factor);
        return new DoubleType(factor);
    }

}
