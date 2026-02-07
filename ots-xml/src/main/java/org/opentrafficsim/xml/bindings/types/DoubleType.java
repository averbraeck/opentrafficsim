package org.opentrafficsim.xml.bindings.types;

/**
 * Expression type with Double value.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class DoubleType extends ExpressionType<Double>
{

    /** */
    private static final long serialVersionUID = 20251111L;

    /** Function to convert output from expression to the right type. */
    private static final SerializableFunction<Object, Double> TO_TYPE = (o) -> ((Number) o).doubleValue();

    /**
     * Constructor with value.
     * @param value value, may be {@code null}.
     */
    public DoubleType(final Double value)
    {
        super(value, TO_TYPE);
    }

    /**
     * Constructor with expression.
     * @param expression expression.
     */
    public DoubleType(final String expression)
    {
        super(expression, TO_TYPE);
    }

}
