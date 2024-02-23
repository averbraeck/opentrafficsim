package org.opentrafficsim.xml.bindings.types;

import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.Estimation;

/**
 * Expression type with Estimation value.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class EstimationType extends ExpressionType<Estimation>
{

    /**
     * Constructor with value.
     * @param value Estimation; value, may be {@code null}.
     */
    public EstimationType(final Estimation value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression String; expression.
     */
    public EstimationType(final String expression)
    {
        super(expression);
    }

}
