package org.opentrafficsim.draw.core;

import java.awt.Color;

/**
 * List of colors to use for various legends.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public final class Colors
{

    /** List of colors. */
    public static Color[] COLORS = new Color[] {Color.BLACK, new Color(0xa5, 0x2a, 0x2a), Color.RED, Color.ORANGE, Color.YELLOW,
            Color.GREEN, Color.BLUE, Color.MAGENTA, Color.GRAY, Color.WHITE};

    /** Names of the colors. */
    public static String[] NAMES =
            new String[] {"black", "brown", "red", "orange", "yellow", "green", "blue", "magenta", "gray", "white"};

    /**
     * Returns a color for the index. Modulo is applied for indices outside of the normal range.
     * @param index int; index.
     * @return Color; color for index.
     */
    public static Color get(final int index)
    {
        return COLORS[mod(index)];
    }

    /**
     * Returns the name of a color for the index. Modulo is applied for indices outside of the normal range.
     * @param index int; index.
     * @return String; name of color for index.
     */
    public static String name(final int index)
    {
        return NAMES[mod(index)];
    }

    /**
     * Returns the modulo of the index given the number of colors we have.
     * @param index int; index.
     * @return int; index in range of colors.
     */
    private static int mod(final int index)
    {
        return Math.abs(index % COLORS.length);
    }

}
