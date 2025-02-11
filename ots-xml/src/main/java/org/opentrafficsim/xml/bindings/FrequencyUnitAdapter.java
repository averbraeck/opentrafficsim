package org.opentrafficsim.xml.bindings;

import org.djunits.unit.FrequencyUnit;
import org.opentrafficsim.xml.bindings.types.FrequencyUnitType;

/**
 * Adapter for FrequencyUnit expression type.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class FrequencyUnitAdapter extends ExpressionAdapter<FrequencyUnit, FrequencyUnitType>
{

    /**
     * Constructor.
     */
    public FrequencyUnitAdapter()
    {
        //
    }

    @Override
    public FrequencyUnitType unmarshal(final String field)
    {
        if (isExpression(field))
        {
            return new FrequencyUnitType(trimBrackets(field));
        }
        return new FrequencyUnitType(FrequencyUnit.BASE.of(field));
    }

}
