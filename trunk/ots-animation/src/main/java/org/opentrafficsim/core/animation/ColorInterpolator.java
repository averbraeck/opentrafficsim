package org.opentrafficsim.core.animation;

import java.awt.Color;

/**
 * Interpolate between two color values.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision: 4763 $, $LastChangedDate: 2018-11-19 11:04:56 +0100 (Mon, 19 Nov 2018) $, by $Author: averbraeck $,
 *          initial version 27 mei 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public final class ColorInterpolator
{
    /** Static use only; this class cannot be instantiated. */
    private ColorInterpolator()
    {
        // Cannot be instantiated
    }

    /**
     * Generate a Color that is interpolated between two given Color values. Interpolation is simply done per channel (R, G, B).
     * @param zero Color; the color that corresponds to ratio == 0
     * @param one Color; the color that corresponds to ratio == 1
     * @param ratio double; the ratio (should be between 0 and 1)
     * @return Color; the interpolated color
     */
    public static Color interpolateColor(final Color zero, final Color one, final double ratio)
    {
        if (ratio < 0 || ratio > 1)
        {
            throw new RuntimeException("Bad ratio (should be between 0 and 1; got " + ratio + ")");
        }
        double complement = 1 - ratio;
        return new Color((int) (zero.getRed() * complement + one.getRed() * ratio),
                (int) (zero.getGreen() * complement + one.getGreen() * ratio),
                (int) (zero.getBlue() * complement + one.getBlue() * ratio));
    }
}
