package org.opentrafficsim.xml.bindings.types;

/**
 * Expression type with Class value.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@SuppressWarnings("rawtypes")
public class ClassType extends ExpressionType<Class>
{

    /**
     * Constructor with value.
     * @param value Class&lt;?&gt;; value, may be {@code null}.
     */
    public ClassType(final Class<?> value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression String; expression.
     */
    public ClassType(final String expression)
    {
        super(expression);
    }

}
