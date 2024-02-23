package org.opentrafficsim.xml.bindings.types;

import org.opentrafficsim.xml.bindings.types.EncodingType.Encoding;

/**
 * Expression type with Encoding value.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class EncodingType extends ExpressionType<Encoding>
{

    /**
     * Constructor with value.
     * @param value Encoding; value, may be {@code null}.
     */
    public EncodingType(final Encoding value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression String; expression.
     */
    public EncodingType(final String expression)
    {
        super(expression);
    }

    /**
     * Encoding.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public static enum Encoding
    {
        /** Base 64. */
        BASE64
    }
    
}
