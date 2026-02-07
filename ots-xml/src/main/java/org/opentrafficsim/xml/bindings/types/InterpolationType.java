package org.opentrafficsim.xml.bindings.types;

import org.opentrafficsim.road.od.Interpolation;

/**
 * Expression type with Interpolation value.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class InterpolationType extends ExpressionType<Interpolation>
{

    /** */
    private static final long serialVersionUID = 20251111L;

    /**
     * Constructor with value.
     * @param value value, may be {@code null}.
     */
    public InterpolationType(final Interpolation value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression expression.
     */
    public InterpolationType(final String expression)
    {
        super(expression);
    }

}
