package org.opentrafficsim.xml.bindings.types;

import org.djunits.unit.DurationUnit;

/**
 * Expression type with DurationUnit value.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class DurationUnitType extends ExpressionType<DurationUnit>
{

    /** Serialization version UID. */
    private static final long serialVersionUID = 20251111L;

    /** Function to convert output from expression to the right type. */
    private static final SerializableFunction<Object, DurationUnit> TO_TYPE =
            SerializableFunction.of(DurationUnit.class, DurationUnit.BASE::getUnitByAbbreviation);

    /**
     * Constructor with value.
     * @param value value, may be {@code null}.
     */
    public DurationUnitType(final DurationUnit value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression expression.
     */
    public DurationUnitType(final String expression)
    {
        super(expression, TO_TYPE);
    }

}
