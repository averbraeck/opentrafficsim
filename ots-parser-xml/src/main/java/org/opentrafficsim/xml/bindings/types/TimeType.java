package org.opentrafficsim.xml.bindings.types;

import java.util.function.Function;

import org.djunits.value.vdouble.scalar.Time;

/**
 * Expression type with Time value.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TimeType extends ExpressionType<Time>
{

    /** Function to convert output from expression to the right type. */
    private static final Function<Object, Time> TO_TYPE = (o) -> Time.instantiateSI(((Number) o).doubleValue());

    /**
     * Constructor with value.
     * @param value Time; value, may be {@code null}.
     */
    public TimeType(final Time value)
    {
        super(value, TO_TYPE);
    }

    /**
     * Constructor with expression.
     * @param expression String; expression.
     */
    public TimeType(final String expression)
    {
        super(expression, TO_TYPE);
    }

}
