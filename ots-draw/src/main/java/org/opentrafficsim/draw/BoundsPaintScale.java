package org.opentrafficsim.draw;

import java.awt.Color;
import java.io.Serializable;
import java.util.Arrays;

import org.djutils.exceptions.Throw;
import org.djutils.logger.CategoryLogger;

/**
 * Paint scale interpolating between colors at values.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class BoundsPaintScale implements ColorPaintScale, Serializable
{

    /** 3-color scale from green to red. */
    public static final Color[] GREEN_RED = new Color[] {Color.GREEN, Color.YELLOW, Color.RED};

    /** 5-color scale from green to red with dark edges. */
    public static final Color[] GREEN_RED_DARK =
            new Color[] {Color.GREEN.darker(), Color.GREEN, Color.YELLOW, Color.RED, Color.RED.darker()};

    /** */
    private static final long serialVersionUID = 20181008L;

    /** Boundary values for this ColorPaintScale. */
    private double[] bounds;

    /** Color values to use at the boundary values. */
    private Color[] boundColors;

    /**
     * Constructor.
     * @param bounds value bounds
     * @param boundColors colors at bounds
     * @throws IllegalArgumentException if less than 2 bounds, unequal number of bounds and colors, or duplicate bounds
     */
    public BoundsPaintScale(final double[] bounds, final Color[] boundColors) throws IllegalArgumentException
    {
        Throw.when(bounds.length < 2, IllegalArgumentException.class, "bounds must have >= 2 entries");
        Throw.when(bounds.length != boundColors.length, IllegalArgumentException.class,
                "bounds must have same length as boundColors");
        this.bounds = new double[bounds.length];
        this.boundColors = new Color[bounds.length];
        // Store the bounds and boundColors in order of increasing bound value.
        // This is as inefficient as bubble sorting, but we're dealing with only a few values here.
        for (int nextBound = 0; nextBound < bounds.length; nextBound++)
        {
            // Find the lowest not-yet used bound
            double currentLowest = Double.POSITIVE_INFINITY;
            int bestIndex = -1;
            int index;
            for (index = 0; index < bounds.length; index++)
            {
                if (bounds[index] < currentLowest && (nextBound == 0 || bounds[index] > this.bounds[nextBound - 1]))
                {
                    bestIndex = index;
                    currentLowest = bounds[index];
                }
            }
            Throw.when(bestIndex < 0, IllegalArgumentException.class, "duplicate value in bounds");
            this.bounds[nextBound] = bounds[bestIndex];
            this.boundColors[nextBound] = boundColors[bestIndex];
        }
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

    @Override
    public Color getPaint(final double value)
    {
        if (Double.isNaN(value))
        {
            return Color.BLACK;
        }
        if (value < this.bounds[0])
        {
            return this.boundColors[0];
        }
        if (value > this.bounds[this.bounds.length - 1])
        {
            return this.boundColors[this.bounds.length - 1];
        }
        int index;
        for (index = 0; index < this.bounds.length - 1; index++)
        {
            if (value < this.bounds[index + 1])
            {
                break;
            }
        }
        final double ratio;
        if (index >= this.bounds.length - 1)
        {
            index = this.bounds.length - 2;
            ratio = 1.0;
        }
        else
        {
            ratio = (value - this.bounds[index]) / (this.bounds[index + 1] - this.bounds[index]);
        }
        if (Double.isInfinite(ratio))
        {
            CategoryLogger.always().error("Interpolation value for color is infinite based on {} in {} obtaining index {}.",
                    value, this.bounds, index);
        }
        Color mix = ColorInterpolator.interpolateColor(this.boundColors[index], this.boundColors[index + 1], ratio);
        return mix;
    }

    @Override
    public final double getLowerBound()
    {
        return this.bounds[0];
    }

    @Override
    public final double getUpperBound()
    {
        return this.bounds[this.bounds.length - 1];
    }

    @Override
    public String toString()
    {
        return "BoundsPaintScale [bounds=" + Arrays.toString(this.bounds) + ", boundColors=" + Arrays.toString(this.boundColors)
                + "]";
    }

}
