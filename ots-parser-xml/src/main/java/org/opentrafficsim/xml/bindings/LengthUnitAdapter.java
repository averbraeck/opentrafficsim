package org.opentrafficsim.xml.bindings;

import org.djunits.unit.LengthUnit;
import org.opentrafficsim.xml.bindings.types.LengthUnitType;

/**
 * Adapter for LengthUnit expression type.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class LengthUnitAdapter extends ExpressionAdapter<LengthUnit, LengthUnitType>
{

    /** {@inheritDoc} */
    @Override
    public LengthUnitType unmarshal(final String field)
    {
        if (isExpression(field))
        {
            return new LengthUnitType(trimBrackets(field));
        }
        return new LengthUnitType(LengthUnit.BASE.of(field));
    }

}
