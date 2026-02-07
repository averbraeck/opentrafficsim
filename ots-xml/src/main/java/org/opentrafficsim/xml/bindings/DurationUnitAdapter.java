package org.opentrafficsim.xml.bindings;

import org.djunits.unit.DurationUnit;
import org.opentrafficsim.xml.bindings.types.DurationUnitType;

/**
 * Adapter for DurationUnit expression type.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class DurationUnitAdapter extends ExpressionAdapter<DurationUnit, DurationUnitType>
{

    /**
     * Constructor.
     */
    public DurationUnitAdapter()
    {
        //
    }

    @Override
    public DurationUnitType unmarshal(final String field)
    {
        if (isExpression(field))
        {
            return new DurationUnitType(trimBrackets(field));
        }
        return new DurationUnitType(DurationUnit.BASE.of(field));
    }

}
