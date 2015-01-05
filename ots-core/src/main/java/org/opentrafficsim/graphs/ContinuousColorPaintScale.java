package org.opentrafficsim.graphs;

import java.awt.Color;
import java.awt.Paint;

import org.jfree.chart.renderer.PaintScale;

/**
 * Create a continuous color paint scale. <br>
 * Primarily intended for the contour plots, but sufficiently abstract for more general use. <br>
 * A continuous color paint scale creates paints (actually simple Colors) by linearly interpolating between a limited set of RGB
 * Color values that correspond to given input values.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jul 30, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class ContinuousColorPaintScale implements PaintScale
{
    /** Boundary values for this ColorPaintScale. */
    private double[] bounds;

    /** Color values to use at the boundary values. */
    private Color[] boundColors;

    /** Format string to render values in a human readable format (used in tool tip texts). */
    private final String format;

    /**
     * Create a new ContinuousColorPaintScale.
     * @param format Format string to render the value under the mouse in a human readable format
     * @param bounds Double[] array of boundary values (all values must be distinct and the number of values must be >= 2)
     * @param boundColors Color[] array of the colors to use at the boundary values (must have same size as bounds)
     */
    ContinuousColorPaintScale(final String format, final double[] bounds, final Color[] boundColors)
    {
        this.format = format;
        if (bounds.length < 2)
        {
            throw new Error("bounds must have >= 2 entries");
        }
        if (bounds.length != boundColors.length)
        {
            throw new Error("bounds must have same length as boundColors");
        }
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
            if (bestIndex < 0)
            {
                throw new Error("duplicate value in bounds");
            }
            this.bounds[nextBound] = bounds[bestIndex];
            this.boundColors[nextBound] = boundColors[bestIndex];
        }
    }

    /** {@inheritDoc} */
    @Override
    public final double getLowerBound()
    {
        return this.bounds[0];
    }

    /**
     * Create a mixed color component. When ratio varies from 0.0 to 1.0, the result varies from <i>low</i> to <i>high</i>. If
     * ratio is outside the range 0.0 to 1.0, the result value can be outside the <i>range</i> <i>low</i> to <i>high</i>.
     * However, the result is always limited to the range 0..255.
     * @param ratio Double; value (normally) between 0.0 and 1.0.
     * @param low Integer; this value is returned when ratio equals 0.0
     * @param high Integer; this value is returned when ratio equals 1.0
     * @return Integer; the ratio-weighted average of <i>low</i> and <i>high</i>
     */
    private static int mixComponent(final double ratio, final int low, final int high)
    {
        final double mix = low * (1 - ratio) + high * ratio;
        int result = (int) mix;
        if (result < 0)
        {
            result = 0;
        }
        if (result > 255)
        {
            result = 255;
        }
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public final Paint getPaint(final double value)
    {
        int bucket;
        for (bucket = 0; bucket < this.bounds.length - 1; bucket++)
        {
            if (value < this.bounds[bucket + 1])
            {
                break;
            }
        }
        if (bucket >= this.bounds.length - 1)
        {
            bucket = this.bounds.length - 2;
        }
        final double ratio = (value - this.bounds[bucket]) / (this.bounds[bucket + 1] - this.bounds[bucket]);
        Color mix =
            new Color(mixComponent(ratio, this.boundColors[bucket].getRed(), this.boundColors[bucket + 1].getRed()),
                mixComponent(ratio, this.boundColors[bucket].getGreen(), this.boundColors[bucket + 1].getGreen()),
                mixComponent(ratio, this.boundColors[bucket].getBlue(), this.boundColors[bucket + 1].getBlue()));
        return mix;
    }

    /** {@inheritDoc} */
    @Override
    public final double getUpperBound()
    {
        return this.bounds[this.bounds.length - 1];
    }

    /**
     * Retrieve the format string.
     * @return format string
     */
    public final String getFormat()
    {
        return this.format;
    }

}
