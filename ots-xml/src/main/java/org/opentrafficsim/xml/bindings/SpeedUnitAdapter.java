package org.opentrafficsim.xml.bindings;

import org.djunits.unit.SpeedUnit;
import org.opentrafficsim.xml.bindings.types.SpeedUnitType;

/**
 * Adapter for SpeedUnit expression type.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class SpeedUnitAdapter extends ExpressionAdapter<SpeedUnit, SpeedUnitType>
{

    /**
     * Constructor.
     */
    public SpeedUnitAdapter()
    {
        //
    }

    @Override
    public SpeedUnitType unmarshal(final String field)
    {
        if (isExpression(field))
        {
            return new SpeedUnitType(trimBrackets(field));
        }
        return new SpeedUnitType(SpeedUnit.BASE.of(field));
    }

}
