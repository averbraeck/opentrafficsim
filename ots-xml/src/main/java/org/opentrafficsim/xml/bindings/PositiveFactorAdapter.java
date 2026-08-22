package org.opentrafficsim.xml.bindings;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.xml.bindings.types.DoubleType;

/**
 * Adapter for positive factors.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class PositiveFactorAdapter extends ExpressionAdapter<Double, DoubleType>
{

    /**
     * Constructor.
     */
    public PositiveFactorAdapter()
    {
        //
    }

    @Override
    public DoubleType unmarshal(final String field)
    {
        if (isExpression(field))
        {
            DoubleType type = new DoubleType(trimBrackets(field));
            type.setExpressionCheck((v) -> v.doubleValue() > 0.0);
            return type;
        }
        double factor = field.endsWith("%") ? Double.parseDouble(field.substring(0, field.length() - 1)) / 100.0
                : Double.parseDouble(field);
        Throw.when(factor < 0.0, OtsRuntimeException.class, "Factor %d is not positive.", factor);
        return new DoubleType(factor);
    }

    @Override
    public String marshal(final DoubleType value)
    {
        Throw.when(!value.isExpression() && value.getValue() < 0.0, IllegalArgumentException.class,
                "Factor %s is not a positive value.", value.getValue());
        return super.marshal(value);
    }

}
