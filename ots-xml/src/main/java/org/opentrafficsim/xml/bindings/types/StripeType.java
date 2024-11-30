package org.opentrafficsim.xml.bindings.types;

/**
 * Expression type with Stripe.StripeType value.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@SuppressWarnings("serial")
public class StripeType extends ExpressionType<org.opentrafficsim.road.network.lane.Stripe.StripeType>
{

    /**
     * Constructor with value.
     * @param value value, may be {@code null}
     */
    public StripeType(final org.opentrafficsim.road.network.lane.Stripe.StripeType value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression expression
     */
    public StripeType(final String expression)
    {
        super(expression);
    }

}
