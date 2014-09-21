package org.opentrafficsim.graphs;

import java.util.ArrayList;

import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.vector.MutableDoubleVector;

/**
 * Density contour plot.
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following
 * conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties, including,
 * but not limited to, the implied warranties of merchantability and fitness for a particular purpose are disclaimed. In no
 * event shall the copyright holder or contributors be liable for any direct, indirect, incidental, special, exemplary, or
 * consequential damages (including, but not limited to, procurement of substitute goods or services; loss of use, data, or
 * profits; or business interruption) however caused and on any theory of liability, whether in contract, strict liability, or
 * tort (including negligence or otherwise) arising in any way out of the use of this software, even if advised of the
 * possibility of such damage.
 * @version Jul 28, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class DensityContourPlot extends ContourPlot
{
    /** */
    private static final long serialVersionUID = 20140729L;

    /**
     * Create a new DensityContourPlot.
     * @param caption String; text to show above the DensityContourPlot
     * @param minimumDistance DoubleScalarAbs&lt;LengthUnit&gt;; minimum distance along the Distance (Y) axis
     * @param maximumDistance DoubleScalarAbs&lt;LengthUnit&gt;; maximum distance along the Distance (Y) axis
     */
    public DensityContourPlot(final String caption, final DoubleScalar.Abs<LengthUnit> minimumDistance,
            final DoubleScalar.Abs<LengthUnit> maximumDistance)
    {
        super(caption, new Axis(INITIALLOWERTIMEBOUND, INITIALUPPERTIMEBOUND, STANDARDTIMEGRANULARITIES,
                STANDARDTIMEGRANULARITIES[STANDARDINITIALTIMEGRANULARITYINDEX], "", "Time", "%.0fs"), new Axis(minimumDistance,
                maximumDistance, STANDARDDISTANCEGRANULARITIES,
                STANDARDDISTANCEGRANULARITIES[STANDARDINITIALDISTANCEGRANULARITYINDEX], "", "Distance", "%.0fm"), 120d, 10d,
                0d, "density %.1f veh/km", "%.1f veh/km", 20d);
    }

    /** Storage for the total time spent in each cell. */
    private ArrayList<MutableDoubleVector.Abs<TimeUnit>> cumulativeTimes;

    /** {@inheritDoc} */
    @Override
    public final Comparable<String> getSeriesKey(final int series)
    {
        return "density";
    }

    /** {@inheritDoc} */
    @Override
    public final void extendXRange(final DoubleScalar<?> newUpperLimit)
    {
        if (null == this.cumulativeTimes)
        {
            this.cumulativeTimes = new ArrayList<MutableDoubleVector.Abs<TimeUnit>>();
        }
        final int highestBinNeeded =
                (int) Math.floor(this.xAxis.getRelativeBin(newUpperLimit) * this.xAxis.getCurrentGranularity()
                        / this.xAxis.granularities[0]);
        while (highestBinNeeded >= this.cumulativeTimes.size())
        {
            this.cumulativeTimes.add(new MutableDoubleVector.Abs.Sparse<TimeUnit>(new double[this.yAxis.getBinCount()],
                    TimeUnit.SECOND));
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void incrementBinData(final int timeBin, final int distanceBin, final double duration,
            final double distanceCovered, final double acceleration)
    {
        if (timeBin < 0 || distanceBin < 0 || 0 == duration || distanceBin >= this.yAxis.getBinCount())
        {
            return;
        }
        MutableDoubleVector.Abs<TimeUnit> values = this.cumulativeTimes.get(timeBin);
        try
        {
            values.setSI(distanceBin, values.getSI(distanceBin) + duration);
        }
        catch (ValueException exception)
        {
            System.err.println("Error in incrementData:");
            exception.printStackTrace();
        }
    }

    /** {@inheritDoc} */
    @Override
    public final double computeZValue(final int firstTimeBin, final int endTimeBin, final int firstDistanceBin,
            final int endDistanceBin)
    {
        double cumulativeTimeInSI = 0;
        if (null == this.cumulativeTimes)
        {
            return Double.NaN;
        }
        try
        {
            for (int timeBinIndex = firstTimeBin; timeBinIndex < endTimeBin; timeBinIndex++)
            {
                MutableDoubleVector.Abs<TimeUnit> values = this.cumulativeTimes.get(timeBinIndex);
                for (int distanceBinIndex = firstDistanceBin; distanceBinIndex < endDistanceBin; distanceBinIndex++)
                {
                    cumulativeTimeInSI += values.getSI(distanceBinIndex);
                }
            }
        }
        catch (ValueException exception)
        {
            System.err.println(String.format("Error in getZValue(timeBinRange=[%d-%d], distanceBinRange=[%d-%d]", firstTimeBin,
                    endTimeBin, firstDistanceBin, endDistanceBin));
            exception.printStackTrace();
        }
        return 1000 * cumulativeTimeInSI / this.xAxis.getCurrentGranularity() / this.yAxis.getCurrentGranularity();
    }

}
