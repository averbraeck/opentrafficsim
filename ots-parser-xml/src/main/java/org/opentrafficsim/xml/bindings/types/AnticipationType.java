package org.opentrafficsim.xml.bindings.types;

import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.Anticipation;

/**
 * Expression type with Anticipation value.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AnticipationType extends ExpressionType<Anticipation>
{

    /**
     * Constructor with value.
     * @param value Anticipation; value, may be {@code null}.
     */
    public AnticipationType(final Anticipation value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression String; expression.
     */
    public AnticipationType(final String expression)
    {
        super(expression);
    }

}
