package org.opentrafficsim.xml.bindings.types;

import org.opentrafficsim.xml.bindings.types.GraphicsTypeType.GraphicsType;

/**
 * Expression type with GraphicsType value.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class GraphicsTypeType extends ExpressionType<GraphicsType>
{

    /** */
    private static final long serialVersionUID = 20251111L;

    /** Function to convert output from expression to the right type. */
    private static final SerializableFunction<Object, GraphicsType> TO_TYPE =
            SerializableFunction.ofStaticField(GraphicsType.class);

    /**
     * Constructor with value.
     * @param value value, may be {@code null}.
     */
    public GraphicsTypeType(final GraphicsType value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression expression.
     */
    public GraphicsTypeType(final String expression)
    {
        super(expression, TO_TYPE);
    }

    /**
     * GraphicsType.
     */
    public enum GraphicsType
    {
        /** Portable network graphics. */
        PNG,

        /** Graphics interchange format. */
        GIF,

        /** Bitmap. */
        BMP,

        /** Jpeg. */
        JPG
    }

}
