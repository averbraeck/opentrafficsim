package org.opentrafficsim.xml.bindings.types;

import org.djunits.value.vdouble.scalar.Duration;

/**
 * Expression type with Duration value.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@SuppressWarnings("serial")
public class DurationType extends ExpressionType<Duration>
{

    /** Function to convert output from expression to the right type. */
    private static final SerializableFunction<Object, Duration> TO_TYPE = (o) -> Duration.ofSI(((Number) o).doubleValue());

    /**
     * Constructor with value.
     * @param value value, may be {@code null}.
     */
    public DurationType(final Duration value)
    {
        super(value, TO_TYPE);
    }

    /**
     * Constructor with expression.
     * @param expression expression.
     */
    public DurationType(final String expression)
    {
        super(expression, TO_TYPE);
    }

}
