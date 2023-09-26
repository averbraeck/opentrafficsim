package org.opentrafficsim.xml.bindings.types;

import java.util.function.Function;

import org.djunits.value.vdouble.scalar.Frequency;

/**
 * Expression type with Frequency value.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class FrequencyType extends ExpressionType<Frequency>
{

    /** Function to convert output from expression to the right type. */
    private static final Function<Object, Frequency> TO_TYPE = (o) -> Frequency.instantiateSI(((Number) o).doubleValue());

    /**
     * Constructor with value.
     * @param value Frequency; value, may be {@code null}.
     */
    public FrequencyType(final Frequency value)
    {
        super(value, TO_TYPE);
    }

    /**
     * Constructor with expression.
     * @param expression String; expression.
     */
    public FrequencyType(final String expression)
    {
        super(expression, TO_TYPE);
    }

}
