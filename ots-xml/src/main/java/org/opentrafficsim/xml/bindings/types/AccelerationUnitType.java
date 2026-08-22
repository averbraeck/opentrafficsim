package org.opentrafficsim.xml.bindings.types;

import org.djunits.unit.AccelerationUnit;

/**
 * Expression type with AccelerationUnit value.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class AccelerationUnitType extends ExpressionType<AccelerationUnit>
{

    /** Serialization version UID. */
    private static final long serialVersionUID = 20251111L;

    /** Function to convert output from expression to the right type. */
    private static final SerializableFunction<Object, AccelerationUnit> TO_TYPE =
            SerializableFunction.of(AccelerationUnit.class, AccelerationUnit.BASE::getUnitByAbbreviation);

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
        super(expression, TO_TYPE);
    }

}
