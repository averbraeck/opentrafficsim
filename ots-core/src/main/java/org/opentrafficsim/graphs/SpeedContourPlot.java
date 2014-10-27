package org.opentrafficsim.graphs;

import java.util.ArrayList;

import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.vector.MutableDoubleVector;

/**
 * Speed contour plot.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jul 29, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SpeedContourPlot extends ContourPlot
{
    /** */
    private static final long serialVersionUID = 20140729L;

    /**
     * Create a new SpeedContourPlot.
     * @param caption String; text to show above the SpeedContourPlot
     * @param minimumDistance DoubleScalar.Abs&lt;LengthUnit&gt;; minimum distance along the Distance (Y) axis
     * @param maximumDistance DoubleScalar.Abs&lt;LengthUnit&gt;; maximum distance along the Distance (Y) axis
     */
    public SpeedContourPlot(final String caption, final DoubleScalar.Abs<LengthUnit> minimumDistance,
            final DoubleScalar.Abs<LengthUnit> maximumDistance)
    {
        super(caption, new Axis(INITIALLOWERTIMEBOUND, INITIALUPPERTIMEBOUND, STANDARDTIMEGRANULARITIES,
                STANDARDTIMEGRANULARITIES[STANDARDINITIALTIMEGRANULARITYINDEX], "", "Time", "%.0fs"), new Axis(
                minimumDistance, maximumDistance, STANDARDDISTANCEGRANULARITIES,
                STANDARDDISTANCEGRANULARITIES[STANDARDINITIALDISTANCEGRANULARITYINDEX], "", "Distance", "%.0fm"), 0d,
                40d, 150d, "speed %.1f km/h", "%.1f km/h", 20d);
    }

    /** Storage for the total time spent in each cell. */
    private ArrayList<MutableDoubleVector.Abs<TimeUnit>> cumulativeTimes;

    /** Storage for the total length traveled in each cell. */
    private ArrayList<MutableDoubleVector.Abs<LengthUnit>> cumulativeLengths;

    /** {@inheritDoc} */
    @Override
    public final Comparable<String> getSeriesKey(final int series)
    {
        return "speed";
    }

    /** {@inheritDoc} */
    @Override
    public final void extendXRange(final DoubleScalar<?> newUpperLimit)
    {
        if (null == this.cumulativeTimes)
        {
            this.cumulativeTimes = new ArrayList<MutableDoubleVector.Abs<TimeUnit>>();
            this.cumulativeLengths = new ArrayList<MutableDoubleVector.Abs<LengthUnit>>();
        }
        int highestBinNeeded =
                (int) Math.floor(this.getXAxis().getRelativeBin(newUpperLimit)
                        * this.getXAxis().getCurrentGranularity() / this.getXAxis().getGranularities()[0]);
        while (highestBinNeeded >= this.cumulativeTimes.size())
        {
            try
            {
                this.cumulativeTimes.add(new MutableDoubleVector.Abs.Sparse<TimeUnit>(new double[this.getYAxis()
                        .getBinCount()], TimeUnit.SECOND));
                this.cumulativeLengths.add(new MutableDoubleVector.Abs.Sparse<LengthUnit>(new double[this.getYAxis()
                        .getBinCount()], LengthUnit.METER));
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
        MutableDoubleVector.Abs<TimeUnit> timeValues = this.cumulativeTimes.get(timeBin);
        MutableDoubleVector.Abs<LengthUnit> lengthValues = this.cumulativeLengths.get(timeBin);
        try
        {
            timeValues.setSI(distanceBin, timeValues.getSI(distanceBin) + duration);
            lengthValues.setSI(distanceBin, lengthValues.getSI(distanceBin) + distanceCovered);
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
        double cumulativeLengthInSI = 0;
        if (firstTimeBin >= this.cumulativeTimes.size())
        {
            return Double.NaN;
        }
        try
        {
            for (int timeBinIndex = firstTimeBin; timeBinIndex < endTimeBin; timeBinIndex++)
            {
                if (timeBinIndex >= this.cumulativeTimes.size())
                {
                    break;
                }
                MutableDoubleVector.Abs<TimeUnit> timeValues = this.cumulativeTimes.get(timeBinIndex);
                MutableDoubleVector.Abs<LengthUnit> lengthValues = this.cumulativeLengths.get(timeBinIndex);
                for (int distanceBinIndex = firstDistanceBin; distanceBinIndex < endDistanceBin; distanceBinIndex++)
                {
                    cumulativeTimeInSI += timeValues.getSI(distanceBinIndex);
                    cumulativeLengthInSI += lengthValues.getSI(distanceBinIndex);
                }
            }
        }
        catch (ValueException exception)
        {
            System.err.println(String.format("Error in getZValue(timeBinRange=[%d-%d], distanceBinRange=[%d-%d]",
                    firstTimeBin, endTimeBin, firstDistanceBin, endDistanceBin));
            exception.printStackTrace();
        }
        if (0 == cumulativeTimeInSI)
        {
            return Double.NaN;
        }
        return 3600d / 1000 * cumulativeLengthInSI / cumulativeTimeInSI;
    }

}
