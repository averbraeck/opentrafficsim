package org.opentrafficsim.draw;

import java.awt.Color;
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
public class BoundsPaintScale implements ColorPaintScale
{

    /** Boundary values for this ColorPaintScale. */
    private final double[] bounds;

    /** Color values to use at the boundary values. */
    private final Color[] boundColors;

    /** Color for non applicable values. */
    private final Color notApplicable;

    /**
     * Constructor.
     * @param bounds value bounds
     * @param boundColors colors at bounds
     * @throws IllegalArgumentException if less than 2 bounds, unequal number of bounds and colors, or duplicate bounds
     */
    public BoundsPaintScale(final double[] bounds, final Color[] boundColors) throws IllegalArgumentException
    {
        this(bounds, boundColors, Color.BLACK);
    }

    /**
     * Constructor.
     * @param bounds value bounds
     * @param boundColors colors at bounds
     * @param notApplicable color for NaN values
     * @throws IllegalArgumentException if less than 2 bounds, unequal number of bounds and colors, or duplicate bounds
     */
    public BoundsPaintScale(final double[] bounds, final Color[] boundColors, final Color notApplicable)
            throws IllegalArgumentException
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
        this.notApplicable = notApplicable;
    }

    @Override
    public Color getPaint(final double value)
    {
        if (Double.isNaN(value))
        {
            return this.notApplicable;
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

    /**
     * Returns the bound values.
     * @return bound values
     */
    public double[] getBounds()
    {
        return Arrays.copyOf(this.bounds, this.bounds.length);
    }

    /**
     * Returns the bound colors.
     * @return bound colors
     */
    public Color[] getBoundColors()
    {
        return Arrays.copyOf(this.boundColors, this.boundColors.length);
    }

    @Override
    public String toString()
    {
        return "BoundsPaintScale [bounds=" + Arrays.toString(this.bounds) + ", boundColors=" + Arrays.toString(this.boundColors)
                + "]";
    }

}
