package org.opentrafficsim.xml.bindings;

import org.opentrafficsim.xml.bindings.types.BooleanType;

/**
 * Adapter for OnOff expression type. This results in a boolean.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class OnOffAdapter extends ExpressionAdapter<Boolean, BooleanType>
{

    /**
     * Constructor.
     */
    public OnOffAdapter()
    {
        //
    }

    @Override
    public BooleanType unmarshal(final String field)
    {
        if (isExpression(field))
        {
            return new BooleanType(trimBrackets("on".equals(field.toLowerCase()) ? "true" : " false"));
        }
        return new BooleanType("on".equals(field.toLowerCase()));
    }

}
