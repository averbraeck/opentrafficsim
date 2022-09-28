package org.opentrafficsim.core.network.factory.xml.units;

import java.awt.Color;

import org.opentrafficsim.core.network.NetworkException;

/**
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public final class Colors
{
    /** Utility class. */
    private Colors()
    {
        // do not instantiate
    }

    /**
     * @param colorStr String; the color as a string.
     * @return the color.
     * @throws NetworkException in case of unknown model.
     */
    @SuppressWarnings("checkstyle:needbraces")
    public static Color parseColor(final String colorStr) throws NetworkException
    {
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

        throw new NetworkException("Unknown color: " + colorStr);
    }
}
