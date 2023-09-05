package org.opentrafficsim.xml.bindings.types;

/**
 * Expression type with Integer value.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class IntegerType extends ExpressionType<Integer>
{

    /**
     * Constructor with value.
     * @param value Integer; value, may be {@code null}.
     */
    public IntegerType(final Integer value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression String; expression.
     */
    public IntegerType(final String expression)
    {
        super(expression);
    }

}