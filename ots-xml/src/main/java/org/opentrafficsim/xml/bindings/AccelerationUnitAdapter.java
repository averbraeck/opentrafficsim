package org.opentrafficsim.xml.bindings;

import org.djunits.unit.AccelerationUnit;
import org.opentrafficsim.xml.bindings.types.AccelerationUnitType;

/**
 * Adapter for AccelerationUnit expression type.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AccelerationUnitAdapter extends ExpressionAdapter<AccelerationUnit, AccelerationUnitType>
{

    @Override
    public AccelerationUnitType unmarshal(final String field)
    {
        if (isExpression(field))
        {
            return new AccelerationUnitType(trimBrackets(field));
        }
        return new AccelerationUnitType(AccelerationUnit.BASE.of(field));
    }

}
