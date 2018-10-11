package org.opentrafficsim.graphs;

import java.io.Serializable;

import org.djunits.value.vdouble.scalar.DoubleScalarInterface;

/**
 * Definition of one axis for a ContourPlot.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-08-30 00:16:51 +0200 (Sun, 30 Aug 2015) $, @version $Revision: 1329 $, by $Author: averbraeck $,
 * initial version Jul 28, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class Axis implements Serializable
{
    /** */
    private static final long serialVersionUID = 20140000L;

    /** Lowest value along this axis. */
    private final DoubleScalarInterface minimumValue;

    /** Highest value along this axis. */
    private DoubleScalarInterface maximumValue;

    /** Aggregation values along this axis (all values must be an integer multiple of the first value). */
    private final double[] granularities;

    /** Current aggregation value (must be one of the values in granularities). */
    private double currentGranularity;

    /** Name to describe the axis and to name the pop up menu that changes the current granularity. */
    private final String name;

    /** Name to identify this axis. */
    private final String shortName;

    /** Format for rendering a value along this axis. */
    private String format;

    /**
     * Create a new AxisDefinition.
     * @param minimumValue DoubleScalarInterface; the minimum value along this axis
     * @param maximumValue DoubleScalarInterface; the maximum value along this axis
     * @param granularities double[]; the aggregation values along this axis (all values must be an integer multiple of the
     *            first value)
     * @param initialGranularity double; initial aggregation value (must be one of the values in granularities)
     * @param name String; the name to describe the axis and to name the pop up menu that changes the current granularity
     * @param shortName String; the name identifying this axis for use in a menu
     * @param format String; format string for rendering a value along this axis
     */
    public Axis(final DoubleScalarInterface minimumValue, final DoubleScalarInterface maximumValue,
            final double[] granularities, final double initialGranularity, final String name, final String shortName,
            final String format)
    {
        this.minimumValue = minimumValue;
        this.setMaximumValue(maximumValue);
        this.granularities = granularities;
        if (null != granularities)
        {
            this.setCurrentGranularity(initialGranularity);
        }
        this.name = name;
        this.shortName = shortName;
        this.format = format;
    }

    /**
     * Compute the floating point bin number for a value.
     * @param value DoubleScalarInterface; the value
     * @return double; the bin that belongs to the value
     */
    public final double getRelativeBin(final DoubleScalarInterface value)
    {
        return (value.getSI() - this.getMinimumValue().getSI()) / this.getGranularities()[0];
    }

    /**
     * Adjust (increase) the range of this AxisDefinition.
     * @param newMaximum DoubleScalarInterface; the new maximum value of the axis
     */
    public final void adjustMaximumValue(final DoubleScalarInterface newMaximum)
    {
        // System.out.println("extending axis " + this.name + " from " + this.maximumValue + " to " + newMaximum);
        this.setMaximumValue(newMaximum);
    }

    /**
     * Return the value for an aggregated bin number.
     * @param aggregatedBin int; the number of a bin
     * @return Double; the value corresponding to the center of aggregateBin
     */
    public final double getValue(final int aggregatedBin)
    {
        return this.getMinimumValue().getSI() + 1.0 * aggregatedBin * this.getCurrentGranularity();
    }

    /**
     * @return Integer; the number of bins along this axis
     */
    public final int getAggregatedBinCount()
    {
        return (int) Math
                .ceil((this.getMaximumValue().getSI() - this.getMinimumValue().getSI()) / this.getCurrentGranularity());
    }

    /**
     * @return Integer; the number of aggregated bins along this axis
     */
    public final int getBinCount()
    {
        return (int) Math.ceil((this.getMaximumValue().getSI() - this.getMinimumValue().getSI()) / this.getGranularities()[0]);
    }

    /**
     * Get the granularity of this axis.
     * @return double; the granularity of this axis
     */
    public final double getCurrentGranularity()
    {
        return this.currentGranularity;
    }

    /**
     * Change the granularity for this axis. <br>
     * The new value must be present in the granularities.
     * @param newGranularity double; the new value for the granularity of this axis
     */
    public final void setCurrentGranularity(final double newGranularity)
    {
        for (double g : this.getGranularities())
        {
            if (g == newGranularity)
            {
                this.currentGranularity = newGranularity;
                return;
            }
        }
        throw new RuntimeException("Illegal granularity " + newGranularity);
    }

    /**
     * Get the maximum value of this axis.
     * @return DoubleScalar; the current maximum value of this axis
     */
    public final DoubleScalarInterface getMaximumValue()
    {
        return this.maximumValue;
    }

    /**
     * Change the maximum value of this axis. <br>
     * The maximum value can only be increased.
     * @param newMaximumValue DoubleScalarInterface; the new maximum value of this axis
     */
    public final void setMaximumValue(final DoubleScalarInterface newMaximumValue)
    {
        if (null != this.maximumValue && newMaximumValue.getSI() < this.maximumValue.getSI())
        {
            throw new RuntimeException("maximum value may not be decreased");
        }
        this.maximumValue = newMaximumValue;
    }

    /**
     * Get the minimum value of this axis.
     * @return DoubleScalar; the minimum value of this axis
     */
    public final DoubleScalarInterface getMinimumValue()
    {
        return this.minimumValue;
    }

    /**
     * Retrieve the possible granularities for this Axis.
     * @return granularities
     */
    public final double[] getGranularities()
    {
        return this.granularities;
    }

    /**
     * Retrieve the format for displaying values along this Axis.
     * @return format
     */
    public final String getFormat()
    {
        return this.format;
    }

    /**
     * Retrieve the short name for this Axis.
     * @return String; the short name for this Axis
     */
    public final String getShortName()
    {
        return this.shortName;
    }

    /**
     * Retrieve the name of this Axis.
     * @return String; the name of this Axis
     */
    public final String getName()
    {
        return this.name;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "Axis [minimumValue=" + this.minimumValue + ", maximumValue=" + this.maximumValue + ", currentGranularity="
                + this.currentGranularity + ", name=" + this.name + ", shortName=" + this.shortName + "]";
    }

}
