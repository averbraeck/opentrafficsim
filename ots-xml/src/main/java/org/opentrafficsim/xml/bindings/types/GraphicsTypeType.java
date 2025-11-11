package org.opentrafficsim.xml.bindings.types;

import org.opentrafficsim.xml.bindings.types.GraphicsTypeType.GraphicsType;

/**
 * Expression type with GraphicsType value.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class GraphicsTypeType extends ExpressionType<GraphicsType>
{

    /** */
    private static final long serialVersionUID = 20251111L;

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
        super(expression);
    }

    /**
     * GraphicsType.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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
