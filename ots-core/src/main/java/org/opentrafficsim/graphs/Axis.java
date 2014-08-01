package org.opentrafficsim.graphs;

import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * Definition of one axis for a ContourPlot.
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
 * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or
 * services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability,
 * whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use
 * of this software, even if advised of the possibility of such damage.
 * @version Jul 28, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class Axis
{
    /** Lowest value along this axis. */
    private DoubleScalar<?> minimumValue;

    /** Highest value along this axis. */
    private DoubleScalar<?> maximumValue;

    /** Aggregation values along this axis (all values must be an integer multiple of the first value). */
    final double[] granularities;

    /** Current aggregation value (must be one of the values in granularities). */
    private double currentGranularity;

    /** Name to describe the axis and to name the pop up menu that changes the current granularity. */
    protected final String name;
    
    /** Name to identify this axis */
    protected final String shortName;

    /** Format for rendering a value along this axis. */
    protected String format;

    /**
     * Create a new AxisDefinition.
     * @param minimumValue DoubleScalar; the minimum value along this axis
     * @param maximumValue DoubleScalar; the maximum value along this axis
     * @param granularities double[]; the aggregation values along this axis (all values must be an integer multiple of
     *            the first value)
     * @param initialGranularity double; initial aggregation value (must be one of the values in granularities)
     * @param name String; the name to describe the axis and to name the pop up menu that changes the current
     *            granularity
     * @param shortName String; the name identifying this axis for use in a menu
     * @param format String; format string for rendering a value along this axis
     */
    public Axis(final DoubleScalar<?> minimumValue, final DoubleScalar<?> maximumValue,
            final double[] granularities, final double initialGranularity, final String name, final String shortName, final String format)
    {
        this.minimumValue = minimumValue;
        this.setMaximumValue(maximumValue);
        this.granularities = granularities;
        if(null != granularities)
            this.setCurrentGranularity(initialGranularity);
        this.name = name;
        this.shortName = shortName;
        this.format = format;
    }

    /**
     * Compute the floating point bin number for a value.
     * @param value DoubleScalar; the value
     * @return double; the bin that belongs to the value
     */
    public double getRelativeBin(final DoubleScalar<?> value)
    {
        return (value.getValueSI() - this.getMinimumValue().getValueSI()) / this.granularities[0];
    }

    /**
     * Adjust (increase) the range of this AxisDefinition.
     * @param newMaximum DoubleScalar; the new maximum value of the axis
     */
    public void adjustMaximumValue(final DoubleScalar<?> newMaximum)
    {
        // System.out.println("extending axis " + this.name + " from " + this.maximumValue + " to " + newMaximum);
        this.setMaximumValue(newMaximum);
    }

    /**
     * Return the value for an aggregated bin number.
     * @param aggregatedBin Integer; the number of a bin
     * @return Double; the value corresponding to the center of aggregateBin
     */
    public double getValue(final int aggregatedBin)
    {
        return this.getMinimumValue().getValueSI() + 1.0 * aggregatedBin * this.getCurrentGranularity();
    }

    /**
     * @return Integer; the number of bins along this axis
     */
    public int getAggregatedBinCount()
    {
        return (int) Math.ceil((this.getMaximumValue().getValueSI() - this.getMinimumValue().getValueSI())
                / this.getCurrentGranularity());
    }

    /**
     * @return Integer; the number of aggregated bins along this axis
     */
    public int getBinCount()
    {
        return (int) Math.ceil((this.getMaximumValue().getValueSI() - this.getMinimumValue().getValueSI())
                / this.granularities[0]);
    }

    /**
     * Get the granularity of this axis.
     * @return double; the granularity of this axis
     */
    public double getCurrentGranularity()
    {
        return this.currentGranularity;
    }

    /**
     * Change the granularity for this axis.
     * <br/> The new value must be present in the granularities.
     * @param newGranularity double; the new value for the granularity of this axis
     */
    public void setCurrentGranularity(final double newGranularity)
    {
        for (double g : this.granularities)
            if (g == newGranularity)
            {
                this.currentGranularity = newGranularity;
                return;
            }
        throw new Error("Illegal granularity " + newGranularity);
    }

    /**
     * Get the maximum value of this axis.
     * @return DoubleScalar; the current maximum value of this axis
     */
    public DoubleScalar<?> getMaximumValue()
    {
        return this.maximumValue;
    }

    /**
     * Change the maximum value of this axis.
     * <br /> The maximum value can only be increased.
     * @param newMaximumValue DoubleScalar; the new maximum value of this axis
     */
    public void setMaximumValue(final DoubleScalar<?> newMaximumValue)
    {
        if (null != this.maximumValue && newMaximumValue.getValueSI() < this.maximumValue.getValueSI())
            throw new Error("maximum value may not be decreased");
        this.maximumValue = newMaximumValue;
    }

    /**
     * Get the minimum value of this axis.
     * @return DoubleScalar; the minimum value of this axis
     */
    public DoubleScalar<?> getMinimumValue()
    {
        return this.minimumValue;
    }
}
