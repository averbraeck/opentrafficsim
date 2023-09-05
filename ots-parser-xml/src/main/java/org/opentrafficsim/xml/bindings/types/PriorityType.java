package org.opentrafficsim.xml.bindings.types;

import org.opentrafficsim.road.network.lane.CrossSectionLink.Priority;

/**
 * Expression type with Priority value.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class PriorityType extends ExpressionType<Priority>
{

    /**
     * Constructor with value.
     * @param value Priority; value, may be {@code null}.
     */
    public PriorityType(final Priority value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression String; expression.
     */
    public PriorityType(final String expression)
    {
        super(expression);
    }

}
