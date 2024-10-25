package org.opentrafficsim.xml.bindings;

import java.awt.Color;

import org.djutils.logger.CategoryLogger;
import org.djutils.reflection.ClassUtil;
import org.opentrafficsim.xml.bindings.types.ColorType;

/**
 * ColorAdapter to convert between Color and a String representation of the Color. Allowed representations are:
 * <ul>
 * <li>#RRGGBB as three hexadecimal values</li>
 * <li>RGB(r,g,b) where r, g and b are bytes</li>
 * <li>well known color string (in {@code Color} class) such as RED, GREEN, BLACK</li>
 * </ul>
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck" target="_blank">Alexander Verbraeck</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ColorAdapter extends ExpressionAdapter<Color, ColorType>
{

    @Override
    public ColorType unmarshal(final String field) throws IllegalArgumentException
    {
        if (isExpression(field))
        {
            return new ColorType(trimBrackets(field));
        }
        try
        {
            String colorStr = field.replaceAll("\\s", "");

            if (colorStr.startsWith("#"))
                return new ColorType(Color.decode(colorStr));

            if (colorStr.startsWith("RGB"))
            {
                String c = colorStr.substring(3).replace("(", "").replace(")", "");
                String[] rgb = c.split(",");
                int r = Integer.parseInt(rgb[0].trim());
                int g = Integer.parseInt(rgb[1].trim());
                int b = Integer.parseInt(rgb[2].trim());
                return new ColorType(new Color(r, g, b));
            }

            return new ColorType((Color) ClassUtil.resolveField(Color.class, colorStr).get(null));
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing color '" + field + "'");
            throw new IllegalArgumentException("Error parsing color " + field, exception);
        }
    }

    @Override
    public String marshal(final ColorType color) throws IllegalArgumentException
    {
        return marshal(color, (c) -> "RGB(" + c.getRed() + "," + c.getGreen() + "," + c.getBlue() + ")");
    }

}
