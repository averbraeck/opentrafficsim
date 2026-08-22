package org.opentrafficsim.xml.bindings.types;

import org.djunits.value.vdouble.scalar.Acceleration;

/**
 * Expression type with Acceleration value.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class AccelerationType extends ExpressionType<Acceleration>
{

    /** Serialization version UID. */
    private static final long serialVersionUID = 20251111L;

    /** Function to convert output from expression to the right type. */
    private static final SerializableFunction<Object, Acceleration> TO_TYPE =
            SerializableFunction.ofNumeric(Acceleration.class, Acceleration::valueOf, Acceleration::ofSI);

    /**
     * Constructor with value.
     * @param value value, may be {@code null}.
     */
    public AccelerationType(final Acceleration value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression expression.
     */
    public AccelerationType(final String expression)
    {
        super(expression, TO_TYPE);
    }

}
