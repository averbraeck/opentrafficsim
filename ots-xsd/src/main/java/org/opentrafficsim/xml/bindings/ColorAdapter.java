package org.opentrafficsim.xml.bindings;

import java.awt.Color;

import javax.xml.bind.annotation.adapters.XmlAdapter;

import org.djutils.logger.CategoryLogger;

/**
 * ColorAdapter to convert between Color and a String representation of the Color. Allowed representations are:<br>
 * - #RRGGBB as three hexadecimal values<br>
 * - RGB(r,g,b) where r, g and b are bytes - well known color string such as RED, GREEN, BLACK<br>
 * <br>
 * Copyright (c) 2003-2022 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved. See
 * for project information <a href="https://www.simulation.tudelft.nl/" target="_blank">www.simulation.tudelft.nl</a>. The
 * source code and binary code of this software is proprietary information of Delft University of Technology.
 * @author <a href="https://www.tudelft.nl/averbraeck" target="_blank">Alexander Verbraeck</a>
 */
public class ColorAdapter extends XmlAdapter<String, Color>
{
    /** {@inheritDoc} */
    @Override
    public Color unmarshal(final String field) throws IllegalArgumentException
    {
        try
        {
            String colorStr = field.replaceAll("\\s", "");

            if (colorStr.startsWith("#"))
                return Color.decode(colorStr);

            if (colorStr.startsWith("RGB"))
            {
                String c = colorStr.substring(3).replace("(", "").replace(")", "");
                String[] rgb = c.split(",");
                int r = Integer.parseInt(rgb[0].trim());
                int g = Integer.parseInt(rgb[1].trim());
                int b = Integer.parseInt(rgb[2].trim());
                return new Color(r, g, b);
            }

            if (colorStr.equals("BLACK"))
                return Color.BLACK;
            if (colorStr.equals("BLUE"))
                return Color.BLUE;
            if (colorStr.equals("CYAN"))
                return Color.CYAN;
            if (colorStr.equals("DARK_GRAY"))
                return Color.DARK_GRAY;
            if (colorStr.equals("GRAY"))
                return Color.GRAY;
            if (colorStr.equals("GREEN"))
                return Color.GREEN;
            if (colorStr.equals("LIGHT_GRAY"))
                return Color.LIGHT_GRAY;
            if (colorStr.equals("MAGENTA"))
                return Color.MAGENTA;
            if (colorStr.equals("ORANGE"))
                return Color.ORANGE;
            if (colorStr.equals("PINK"))
                return Color.PINK;
            if (colorStr.equals("RED"))
                return Color.RED;
            if (colorStr.equals("WHITE"))
                return Color.WHITE;
            if (colorStr.equals("YELLOW"))
                return Color.YELLOW;
        }
        catch (Exception exception)
        {
            CategoryLogger.always().error(exception, "Problem parsing color '" + field + "'");
            throw new IllegalArgumentException("Error parsing color " + field, exception);
        }
        CategoryLogger.always().error("Problem parsing color '" + field + "'");
        throw new IllegalArgumentException("Error parsing color " + field);
    }

    /** {@inheritDoc} */
    @Override
    public String marshal(final Color color) throws IllegalArgumentException
    {
        return "RGB(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ")";
    }

}
