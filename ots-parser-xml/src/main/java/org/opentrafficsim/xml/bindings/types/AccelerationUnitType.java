package org.opentrafficsim.xml.bindings.types;

import org.djunits.unit.AccelerationUnit;

/**
 * Expression type with AccelerationUnit value.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AccelerationUnitType extends ExpressionType<AccelerationUnit>
{

    /**
     * Constructor with value.
     * @param value value, may be {@code null}.
     */
    public AccelerationUnitType(final AccelerationUnit value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression expression.
     */
    public AccelerationUnitType(final String expression)
    {
        super(expression);
    }

}
