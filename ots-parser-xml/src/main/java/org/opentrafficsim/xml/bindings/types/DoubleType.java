package org.opentrafficsim.xml.bindings.types;

/**
 * Expression type with Double value.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class DoubleType extends ExpressionType<Double>
{

    /**
     * Constructor with value.
     * @param value Double; value, may be {@code null}.
     */
    public DoubleType(final Double value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression String; expression.
     */
    public DoubleType(final String expression)
    {
        super(expression);
    }

}
