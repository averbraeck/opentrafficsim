package org.opentrafficsim.xml.bindings.types;

/**
 * Expression type with Integer value.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@SuppressWarnings("serial")
public class IntegerType extends ExpressionType<Integer>
{

    /** Function to convert output from expression to the right type. */
    private static final SerializableFunction<Object, Integer> TO_TYPE = (o) -> ((Number) o).intValue();

    /**
     * Constructor with value.
     * @param value value, may be {@code null}.
     */
    public IntegerType(final Integer value)
    {
        super(value, TO_TYPE);
    }

    /**
     * Constructor with expression.
     * @param expression expression.
     */
    public IntegerType(final String expression)
    {
        super(expression, TO_TYPE);
    }

}
