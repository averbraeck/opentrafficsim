package org.opentrafficsim.graphs;

import java.util.ArrayList;
import java.util.List;

import org.djunits.unit.TimeUnit;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.vector.MutableDoubleVector;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Density contour plot.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-03 13:38:01 +0200 (Thu, 03 Sep 2015) $, @version $Revision: 1378 $, by $Author: averbraeck $,
 * initial version Jul 28, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class DensityContourPlot extends ContourPlot
{
    /** */
    private static final long serialVersionUID = 20140729L;

    /**
     * Create a new DensityContourPlot.
     * @param caption String; text to show above the DensityContourPlot
     * @param path List&lt;Lane&gt;; the series of Lanes that will provide the data for this TrajectoryPlot
     */
    public DensityContourPlot(final String caption, final List<Lane> path)
    {
        super(caption, new Axis(INITIALLOWERTIMEBOUND, INITIALUPPERTIMEBOUND, STANDARDTIMEGRANULARITIES,
            STANDARDTIMEGRANULARITIES[STANDARDINITIALTIMEGRANULARITYINDEX], "", "Time", "%.0fs"), path, 120d, 10d, 0d,
            "density %.1f veh/km", "%.1f veh/km", 20d);
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
            (int) Math.floor(this.getXAxis().getRelativeBin(newUpperLimit) * this.getXAxis().getCurrentGranularity()
                / this.getXAxis().getGranularities()[0]);
        while (highestBinNeeded >= this.cumulativeTimes.size())
        {
            try
            {
                this.cumulativeTimes.add(new MutableDoubleVector.Abs.Dense<TimeUnit>(new double[this.getYAxis()
                    .getBinCount()], TimeUnit.SECOND));
            }
            catch (ValueException exception)
            {
                exception.printStackTrace();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void incrementBinData(final int timeBin, final int distanceBin, final double duration,
        final double distanceCovered, final double acceleration)
    {
        if (timeBin < 0 || distanceBin < 0 || 0 == duration || distanceBin >= this.getYAxis().getBinCount())
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
            System.err.println(String.format("Error in getZValue(timeBinRange=[%d-%d], distanceBinRange=[%d-%d]",
                firstTimeBin, endTimeBin, firstDistanceBin, endDistanceBin));
            exception.printStackTrace();
        }
        return 1000 * cumulativeTimeInSI / this.getXAxis().getCurrentGranularity() / this.getYAxis().getCurrentGranularity();
    }

}
