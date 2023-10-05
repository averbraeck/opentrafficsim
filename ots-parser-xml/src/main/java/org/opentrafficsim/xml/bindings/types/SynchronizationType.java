package org.opentrafficsim.xml.bindings.types;

import org.opentrafficsim.road.gtu.lane.tactical.util.lmrs.Synchronization;

/**
 * Expression type with Synchronization value.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class SynchronizationType extends ExpressionType<Synchronization>
{

    /**
     * Constructor with value.
     * @param value Synchronization; value, may be {@code null}.
     */
    public SynchronizationType(final Synchronization value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression String; expression.
     */
    public SynchronizationType(final String expression)
    {
        super(expression);
    }

}
