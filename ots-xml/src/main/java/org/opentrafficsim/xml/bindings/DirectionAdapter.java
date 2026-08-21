package org.opentrafficsim.xml.bindings;

import org.djunits.value.vdouble.scalar.Direction;
import org.opentrafficsim.xml.bindings.types.DirectionType;

/**
 * DirectionAdapter converts between the XML String for an Direction and the DJUnits Direction. EAST is taken as zero degrees,
 * and the Direction adapts an ENU (East-North-Up) model, where positive x is East, positive y is North, positive z is up, and
 * degrees go anti-clockwise from the positive x-axis (East).
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Wouter Schakel
 */
public class DirectionAdapter extends ScalarAdapter<Direction, DirectionType>
{

    /**
     * Constructor.
     */
    public DirectionAdapter()
    {
        //
    }

    @Override
    public DirectionType unmarshal(final String field)
    {
        if (isExpression(field))
        {
            return new DirectionType(trimBrackets(field));
        }
        return new DirectionType(DirectionType.valueOf(field));
    }

}
