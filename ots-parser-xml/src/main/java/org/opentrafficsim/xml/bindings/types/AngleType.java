package org.opentrafficsim.xml.bindings.types;

import java.util.function.Function;

import org.djunits.value.vdouble.scalar.Angle;

/**
 * Expression type with Angle value.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AngleType extends ExpressionType<Angle>
{

    /** Function to convert output from expression to the right type. */
    private static final Function<Object, Angle> TO_TYPE = (o) -> Angle.instantiateSI(((Number) o).doubleValue());

    /**
     * Constructor with value.
     * @param value Angle; value, may be {@code null}.
     */
    public AngleType(final Angle value)
    {
        super(value, TO_TYPE);
    }

    /**
     * Constructor with expression.
     * @param expression String; expression.
     */
    public AngleType(final String expression)
    {
        super(expression, TO_TYPE);
    }

}
