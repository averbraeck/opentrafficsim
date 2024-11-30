package org.opentrafficsim.xml.bindings;

import org.djunits.unit.LinearDensityUnit;
import org.opentrafficsim.xml.bindings.types.LinearDensityUnitType;

/**
 * Adapter for LinearDensityUnit expression type.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LinearDensityUnitAdapter extends ExpressionAdapter<LinearDensityUnit, LinearDensityUnitType>
{

    @Override
    public LinearDensityUnitType unmarshal(final String field)
    {
        if (isExpression(field))
        {
            return new LinearDensityUnitType(trimBrackets(field));
        }
        return new LinearDensityUnitType(LinearDensityUnit.BASE.of(field));
    }

}
