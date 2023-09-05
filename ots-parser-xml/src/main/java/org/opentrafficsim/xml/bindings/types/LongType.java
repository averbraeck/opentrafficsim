package org.opentrafficsim.xml.bindings.types;

/**
 * Expression type with Long value.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class LongType extends ExpressionType<Long>
{

    /**
     * Constructor with value.
     * @param value Long; value, may be {@code null}.
     */
    public LongType(final Long value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression String; expression.
     */
    public LongType(final String expression)
    {
        super(expression);
    }

}
