package org.opentrafficsim.draw;

import java.awt.Color;

/**
 * List of colors to use for various legends.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class Colors
{

    /** OTS blue (#0066c4). */
    public static final Color OTS_BLUE = new Color(0, 102, 196);

    /** 3-color scale from green to red. */
    public static final Color[] GREEN_RED = new Color[] {Color.GREEN, Color.YELLOW, Color.RED};

    /** 5-color scale from green to red with dark edges. */
    public static final Color[] GREEN_RED_DARK =
            new Color[] {Color.GREEN.darker(), Color.GREEN, Color.YELLOW, Color.RED, Color.RED.darker()};

    /** 6-color scale from magenta, through red-green, to blue. */
    public static final Color[] ULTRA =
            new Color[] {Color.MAGENTA, Color.RED, Color.ORANGE, Color.YELLOW, Color.GREEN, Color.BLUE};

    /** 10-color scale for enumeration. */
    public static final Color[] ENUMERATE = new Color[] {Color.BLACK, new Color(0xa5, 0x2a, 0x2a), Color.RED, Color.ORANGE,
            Color.YELLOW, Color.GREEN, Color.BLUE, Color.MAGENTA, Color.GRAY, Color.WHITE};

    /** Names of the enumerated colors. */
    public static final String[] ENUMERATE_NAMES =
            new String[] {"black", "brown", "red", "orange", "yellow", "green", "blue", "magenta", "gray", "white"};

    /**
     * Constructor.
     */
    private Colors()
    {
        //
    }

    /**
     * Reverses the color array.
     * @param colors array of colors
     * @return reversed color array
     */
    public static Color[] reverse(final Color[] colors)
    {
        Color[] out = new Color[colors.length];
        for (int i = 0; i < colors.length; i++)
        {
            out[colors.length - i - 1] = colors[i];
        }
        return out;
    }

    /**
     * Creates an array of {@code n} colors with varying hue.
     * @param n number of colors.
     * @return array of {@code n} colors with varying hue
     */
    public static Color[] hue(final int n)
    {
        Color[] out = new Color[n];
        for (int i = 0; i < n; i++)
        {
            out[i] = new Color(Color.HSBtoRGB(((float) i) / n, 1.0f, 1.0f));
        }
        return out;
    }

    /**
     * Returns a color for the index. Modulo is applied for indices outside of the normal range.
     * @param index index.
     * @return color for index.
     */
    public static Color getEnumerated(final int index)
    {
        return ENUMERATE[mod(index)];
    }

    /**
     * Returns the name of a color for the index. Modulo is applied for indices outside of the normal range.
     * @param index index.
     * @return name of color for index.
     */
    public static String nameEnumerated(final int index)
    {
        return ENUMERATE_NAMES[mod(index)];
    }

    /**
     * Returns the modulo of the index given the number of colors we have.
     * @param index index.
     * @return index in range of colors.
     */
    private static int mod(final int index)
    {
        return Math.abs(index % ENUMERATE.length);
    }

    /**
     * Returns a color from an array, where the used index is determined based on the id. If the last character of the id is a
     * digit, that value is used. Otherwise it is the absolute hash code of the id. The modulo of either of these values given
     * the number of colors is the index in the array of the color returned.
     * @param id object id
     * @param colors colors to select from
     * @return color
     */
    public static Color getIdColor(final String id, final Color[] colors)
    {
        return colors[Character.isDigit(id.charAt(id.length() - 1)) ? id.charAt(id.length() - 1) - '0'
                : Math.abs(id.hashCode()) % colors.length];
    }

}
