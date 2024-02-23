package org.opentrafficsim.xml.bindings.types;

import org.opentrafficsim.road.network.lane.Stripe.Type;

/**
 * Expression type with Stripe.Type value.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class StripeType extends ExpressionType<Type>
{

    /**
     * Constructor with value.
     * @param value Stripe.Type; value, may be {@code null}.
     */
    public StripeType(final Type value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression String; expression.
     */
    public StripeType(final String expression)
    {
        super(expression);
    }

}
