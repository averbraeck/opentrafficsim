package org.opentrafficsim.xml.bindings.types;

import org.djunits.value.vdouble.scalar.Angle;

/**
 * Expression type with Angle value.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AngleType extends ExpressionType<Angle>
{

    /** */
    private static final long serialVersionUID = 20251111L;

    /** Function to convert output from expression to the right type. */
    private static final SerializableFunction<Object, Angle> TO_TYPE = (o) -> Angle.ofSI(((Number) o).doubleValue());

    /**
     * Constructor with value.
     * @param value value, may be {@code null}.
     */
    public AngleType(final Angle value)
    {
        super(value, TO_TYPE);
    }

    /**
     * Constructor with expression.
     * @param expression expression.
     */
    public AngleType(final String expression)
    {
        super(expression, TO_TYPE);
    }

}
