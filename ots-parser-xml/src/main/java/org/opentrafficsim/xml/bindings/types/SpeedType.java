package org.opentrafficsim.xml.bindings.types;

import org.djunits.value.vdouble.scalar.Speed;

/**
 * Expression type with Speed value.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@SuppressWarnings("serial")
public class SpeedType extends ExpressionType<Speed>
{

    /** Function to convert output from expression to the right type. */
    private static final SerializableFunction<Object, Speed> TO_TYPE = (o) -> Speed.instantiateSI(((Number) o).doubleValue());

    /**
     * Constructor with value.
     * @param value value, may be {@code null}.
     */
    public SpeedType(final Speed value)
    {
        super(value, TO_TYPE);
    }

    /**
     * Constructor with expression.
     * @param expression expression.
     */
    public SpeedType(final String expression)
    {
        super(expression, TO_TYPE);
    }

}
