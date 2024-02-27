package org.opentrafficsim.xml.bindings.types;

import java.awt.Color;

/**
 * Expression type with Color value.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ColorType extends ExpressionType<Color>
{

    /**
     * Constructor with value.
     * @param value Color; value, may be {@code null}.
     */
    public ColorType(final Color value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression String; expression.
     */
    public ColorType(final String expression)
    {
        super(expression);
    }

}
