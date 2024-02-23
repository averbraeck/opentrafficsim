package org.opentrafficsim.xml.bindings.types;

import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Cooperation;

/**
 * Expression type with Cooperation value.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class CooperationType extends ExpressionType<Cooperation>
{

    /**
     * Constructor with value.
     * @param value Cooperation; value, may be {@code null}.
     */
    public CooperationType(final Cooperation value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression String; expression.
     */
    public CooperationType(final String expression)
    {
        super(expression);
    }

}
