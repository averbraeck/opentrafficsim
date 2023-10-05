package org.opentrafficsim.xml.bindings;

import org.opentrafficsim.road.network.lane.CrossSectionLink.Priority;
import org.opentrafficsim.xml.bindings.types.PriorityType;

/**
 * PriorityAdapter to convert between XML representations of an arc direction, coded as PRIORITY | NONE | TURN_ON_RED | YIELD |
 * STOP | ALL_STOP | BUS_STOP, and an enum type.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class PriorityAdapter extends ExpressionAdapter<Priority, PriorityType>
{

    /** {@inheritDoc} */
    @Override
    public PriorityType unmarshal(final String value) throws Exception
    {
        if (isExpression(value))
        {
            return new PriorityType(trimBrackets(value));
        }
        return new PriorityType(Priority.valueOf(value));
    }

}
