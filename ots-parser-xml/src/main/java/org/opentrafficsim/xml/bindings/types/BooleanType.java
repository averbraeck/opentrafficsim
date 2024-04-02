package org.opentrafficsim.xml.bindings.types;

/**
 * Expression type with Boolean value.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class BooleanType extends ExpressionType<Boolean>
{

    /**
     * Constructor with value.
     * @param value Boolean; value, may be {@code null}.
     */
    public BooleanType(final Boolean value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression String; expression.
     */
    public BooleanType(final String expression)
    {
        super(expression);
    }

}
