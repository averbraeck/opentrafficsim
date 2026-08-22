package org.opentrafficsim.xml.bindings.types;

import org.opentrafficsim.road.gtu.perception.categories.neighbors.Estimation;

/**
 * Expression type with Estimation value.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class EstimationType extends ExpressionType<Estimation>
{

    /** Serialization version UID. */
    private static final long serialVersionUID = 20251111L;

    /** Function to convert output from expression to the right type. */
    private static final SerializableFunction<Object, Estimation> TO_TYPE =
            SerializableFunction.ofStaticField(Estimation.class);

    /**
     * Constructor with value.
     * @param value value, may be {@code null}.
     */
    public EstimationType(final Estimation value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression expression.
     */
    public EstimationType(final String expression)
    {
        super(expression, TO_TYPE);
    }

}
