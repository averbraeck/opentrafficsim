package org.opentrafficsim.xml.bindings.types;

import org.djunits.value.vdouble.scalar.Length;

/**
 * Expression type with Length value.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class LengthType extends ExpressionType<Length>
{

    /**
     * Constructor with value.
     * @param value Length; value, may be {@code null}.
     */
    public LengthType(final Length value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression String; expression.
     */
    public LengthType(final String expression)
    {
        super(expression);
    }

}
