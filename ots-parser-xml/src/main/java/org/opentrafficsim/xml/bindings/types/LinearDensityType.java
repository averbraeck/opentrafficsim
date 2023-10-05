package org.opentrafficsim.xml.bindings.types;

import java.util.function.Function;

import org.djunits.value.vdouble.scalar.LinearDensity;

/**
 * Expression type with LinearDensity value.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class LinearDensityType extends ExpressionType<LinearDensity>
{

    /** Function to convert output from expression to the right type. */
    private static final Function<Object, LinearDensity> TO_TYPE =
            (o) -> LinearDensity.instantiateSI(((Number) o).doubleValue());

    /**
     * Constructor with value.
     * @param value LinearDensity; value, may be {@code null}.
     */
    public LinearDensityType(final LinearDensity value)
    {
        super(value, TO_TYPE);
    }

    /**
     * Constructor with expression.
     * @param expression String; expression.
     */
    public LinearDensityType(final String expression)
    {
        super(expression, TO_TYPE);
    }

}
