package org.opentrafficsim.xml.bindings.types;

import org.djunits.unit.SpeedUnit;

/**
 * Expression type with SpeedUnit value.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class SpeedUnitType extends ExpressionType<SpeedUnit>
{

    /**
     * Constructor with value.
     * @param value SpeedUnit; value, may be {@code null}.
     */
    public SpeedUnitType(final SpeedUnit value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression String; expression.
     */
    public SpeedUnitType(final String expression)
    {
        super(expression);
    }

}
