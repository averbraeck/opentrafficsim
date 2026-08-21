package org.opentrafficsim.xml.bindings;

import org.opentrafficsim.xml.bindings.types.ArcDirectionType;
import org.opentrafficsim.xml.bindings.types.ArcDirectionType.ArcDirection;

/**
 * ArcDirectionAdapter to convert between XML representations of an arc direction, coded as L | LEFT | R | RIGHT | CLOCKWISE |
 * COUNTERCLOCKWISE, and an enum type.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Wouter Schakel
 */
public class ArcDirectionAdapter extends ExpressionAdapter<ArcDirection, ArcDirectionType>
{

    /**
     * Constructor.
     */
    public ArcDirectionAdapter()
    {
        //
    }

    @Override
    public ArcDirectionType unmarshal(final String field) throws IllegalArgumentException
    {
        if (isExpression(field))
        {
            return new ArcDirectionType(trimBrackets(field));
        }
        return new ArcDirectionType(ArcDirectionType.valueOf(field));
    }

}
