package org.opentrafficsim.xml.bindings.types;

import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Tailgating;

/**
 * Expression type with Tailgating value.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TailgatingType extends ExpressionType<Tailgating>
{

    /** */
    private static final long serialVersionUID = 20251111L;

    /**
     * Constructor with value.
     * @param value value, may be {@code null}.
     */
    public TailgatingType(final Tailgating value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression expression.
     */
    public TailgatingType(final String expression)
    {
        super(expression);
    }

}
