package org.opentrafficsim.xml.bindings.types;

import org.djunits.unit.SpeedUnit;

/**
 * Expression type with SpeedUnit value.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class SpeedUnitType extends ExpressionType<SpeedUnit>
{

    /** Serialization version UID. */
    private static final long serialVersionUID = 20251111L;

    /** Function to convert output from expression to the right type. */
    private static final SerializableFunction<Object, SpeedUnit> TO_TYPE =
            SerializableFunction.of(SpeedUnit.class, SpeedUnit.BASE::getUnitByAbbreviation);

    /**
     * Constructor with value.
     * @param value value, may be {@code null}.
     */
    public SpeedUnitType(final SpeedUnit value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression expression.
     */
    public SpeedUnitType(final String expression)
    {
        super(expression, TO_TYPE);
    }

}
