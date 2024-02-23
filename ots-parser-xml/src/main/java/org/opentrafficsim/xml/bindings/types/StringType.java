package org.opentrafficsim.xml.bindings.types;

/**
 * Expression type with String value.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class StringType extends ExpressionType<String>
{

    /**
     * Constructor with input that is either the value or an expression.
     * @param input String; input, either the value or an expression, may be {@code null} as value.
     * @param isExpression boolean; whether the input is an expression.
     */
    public StringType(final String input, final boolean isExpression)
    {
        super(input, isExpression);
    }

}
