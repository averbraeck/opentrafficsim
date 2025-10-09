package org.opentrafficsim.xml.bindings.types;

import org.djunits.value.vdouble.scalar.Time;

/**
 * Expression type with Time value.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@SuppressWarnings("serial")
public class TimeType extends ExpressionType<Time>
{

    /** Function to convert output from expression to the right type. */
    private static final SerializableFunction<Object, Time> TO_TYPE = (o) -> Time.ofSI(((Number) o).doubleValue());

    /**
     * Constructor with value.
     * @param value value, may be {@code null}.
     */
    public TimeType(final Time value)
    {
        super(value, TO_TYPE);
    }

    /**
     * Constructor with expression.
     * @param expression expression.
     */
    public TimeType(final String expression)
    {
        super(expression, TO_TYPE);
    }

}
