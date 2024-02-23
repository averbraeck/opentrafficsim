package org.opentrafficsim.xml.bindings.types;

import org.djunits.unit.FrequencyUnit;

/**
 * Expression type with FrequencyUnit value.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class FrequencyUnitType extends ExpressionType<FrequencyUnit>
{

    /**
     * Constructor with value.
     * @param value FrequencyUnit; value, may be {@code null}.
     */
    public FrequencyUnitType(final FrequencyUnit value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression String; expression.
     */
    public FrequencyUnitType(final String expression)
    {
        super(expression);
    }

}
