package org.opentrafficsim.xml.bindings.types;

import java.lang.reflect.Field;

/**
 * Expression type with Field value.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class FieldType extends ExpressionType<Field>
{

    /**
     * Constructor with value.
     * @param value value, may be {@code null}.
     */
    public FieldType(final Field value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression expression.
     */
    public FieldType(final String expression)
    {
        super(expression);
    }

}
