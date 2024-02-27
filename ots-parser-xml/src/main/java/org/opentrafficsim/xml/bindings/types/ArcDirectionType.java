package org.opentrafficsim.xml.bindings.types;

import org.opentrafficsim.xml.bindings.types.ArcDirectionType.ArcDirection;

/**
 * Expression type with ArcDirection value.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ArcDirectionType extends ExpressionType<ArcDirection>
{

    /**
     * Constructor with value.
     * @param value ArcDirection; value, may be {@code null}.
     */
    public ArcDirectionType(final ArcDirection value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression String; expression.
     */
    public ArcDirectionType(final String expression)
    {
        super(expression);
    }
    
    /**
     * Direction of the arc; LEFT or RIGHT.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
     */
    public enum ArcDirection
    {
        /** Left = counter-clockwise. */
        LEFT,
        
        /** Right = clockwise. */
        RIGHT;
    }

}
