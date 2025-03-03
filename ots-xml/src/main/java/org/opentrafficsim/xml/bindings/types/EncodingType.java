package org.opentrafficsim.xml.bindings.types;

import org.opentrafficsim.xml.bindings.types.EncodingType.Encoding;

/**
 * Expression type with Encoding value.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@SuppressWarnings("serial")
public class EncodingType extends ExpressionType<Encoding>
{

    /**
     * Constructor with value.
     * @param value value, may be {@code null}.
     */
    public EncodingType(final Encoding value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression expression.
     */
    public EncodingType(final String expression)
    {
        super(expression);
    }

    /**
     * Encoding.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public enum Encoding
    {
        /** Base 64. */
        BASE64
    }

}
