package org.opentrafficsim.xml.bindings;

import java.awt.Color;

import org.opentrafficsim.xml.bindings.types.ColorType;

/**
 * ColorAdapter to convert between Color and a String representation of the Color. Allowed representations are:
 * <ul>
 * <li>#RRGGBB as three hexadecimal values</li>
 * <li>RGB(r,g,b) where r, g and b are bytes</li>
 * <li>well known color string (in {@code Color} class) such as RED, GREEN, BLACK</li>
 * </ul>
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Alexander Verbraeck
 * @author Wouter Schakel
 */
public class ColorAdapter extends ExpressionAdapter<Color, ColorType>
{

    /**
     * Constructor.
     */
    public ColorAdapter()
    {
        //
    }

    @Override
    public ColorType unmarshal(final String field) throws IllegalArgumentException
    {
        if (isExpression(field))
        {
            return new ColorType(trimBrackets(field));
        }
        return new ColorType(ColorType.valueOf(field));
    }

    @Override
    public String marshal(final ColorType color) throws IllegalArgumentException
    {
        return marshalAsExpressionOrValue(color, (c) -> "RGB(" + c.getRed() + "," + c.getGreen() + "," + c.getBlue() + ")");
    }

}
