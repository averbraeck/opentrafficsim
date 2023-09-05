package org.opentrafficsim.xml.bindings.types;

import org.djunits.value.vdouble.scalar.Angle;

/**
 * Expression type with Angle value.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class AngleType extends ExpressionType<Angle>
{

    /**
     * Constructor with value.
     * @param value Angle; value, may be {@code null}.
     */
    public AngleType(final Angle value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression String; expression.
     */
    public AngleType(final String expression)
    {
        super(expression);
    }

}
