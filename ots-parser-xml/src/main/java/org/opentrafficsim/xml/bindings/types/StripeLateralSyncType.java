package org.opentrafficsim.xml.bindings.types;

import org.opentrafficsim.base.StripeElement.StripeLateralSync;

/**
 * Expression type with StripeLateralSync value.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@SuppressWarnings("serial")
public class StripeLateralSyncType extends ExpressionType<StripeLateralSync>
{

    /**
     * Constructor with value.
     * @param value value, may be {@code null}.
     */
    public StripeLateralSyncType(final StripeLateralSync value)
    {
        super(value);
    }

    /**
     * Constructor with expression.
     * @param expression expression.
     */
    public StripeLateralSyncType(final String expression)
    {
        super(expression);
    }

}
