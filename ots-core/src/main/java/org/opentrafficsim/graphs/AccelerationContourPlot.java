package org.opentrafficsim.graphs;

import java.util.ArrayList;
import java.util.List;

import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.vector.MutableDoubleVector;

/**
 * Acceleration contour plot.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial versionJul 31, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class AccelerationContourPlot extends ContourPlot
{
    /** */
    private static final long serialVersionUID = 20140731L;

    /**
     * Create a new AccelerationContourPlot.
     * @param caption String; text to show above the AccelerationContourPlot
     * @param path List&lt;Lane&gt;; the series of Lanes that will provide the data for this TrajectoryPlot
     */
    public AccelerationContourPlot(final String caption, final List<Lane> path)
    {
        super(caption, new Axis(INITIALLOWERTIMEBOUND, INITIALUPPERTIMEBOUND, STANDARDTIMEGRANULARITIES,
            STANDARDTIMEGRANULARITIES[STANDARDINITIALTIMEGRANULARITYINDEX], "", "Time", "%.0fs"), path, -5d, 0d, 3d,
            "acceleration %.1f m/s/s", "%.1f m/s/s", 1d);
    }

    /** Storage for the total time spent in each cell. */
    private ArrayList<MutableDoubleVector.Abs<TimeUnit>> cumulativeTimes;

    /** Storage for the total acceleration executed in each cell. */
    private ArrayList<MutableDoubleVector.Abs<AccelerationUnit>> cumulativeAccelerations;

    /** {@inheritDoc} */
    @Override
    public final Comparable<String> getSeriesKey(final int series)
    {
        return "acceleration";
    }

    /** {@inheritDoc} */
    @Override
    public final void extendXRange(final DoubleScalar<?> newUpperLimit)
    {
        if (null == this.cumulativeTimes)
        {
            this.cumulativeTimes = new ArrayList<MutableDoubleVector.Abs<TimeUnit>>();
            this.cumulativeAccelerations = new ArrayList<MutableDoubleVector.Abs<AccelerationUnit>>();
        }
        int highestBinNeeded =
            (int) Math.floor(this.getXAxis().getRelativeBin(newUpperLimit) * this.getXAxis().getCurrentGranularity()
                / this.getXAxis().getGranularities()[0]);
        while (highestBinNeeded >= this.cumulativeTimes.size())
        {
            try
            {
                this.cumulativeTimes.add(new MutableDoubleVector.Abs.Sparse<TimeUnit>(new double[this.getYAxis()
                    .getBinCount()], TimeUnit.SECOND));
                this.cumulativeAccelerations.add(new MutableDoubleVector.Abs.Sparse<AccelerationUnit>(new double[this
                    .getYAxis().getBinCount()], AccelerationUnit.METER_PER_SECOND_2));
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
        MutableDoubleVector.Abs<AccelerationUnit> accelerationValues = this.cumulativeAccelerations.get(timeBin);
        try
        {
            timeValues.setSI(distanceBin, timeValues.getSI(distanceBin) + duration);
            accelerationValues.setSI(distanceBin, accelerationValues.getSI(distanceBin) + acceleration * duration);
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
        double cumulativeAccelerationInSI = 0;
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
                MutableDoubleVector.Abs<AccelerationUnit> accelerationValues =
                    this.cumulativeAccelerations.get(timeBinIndex);
                for (int distanceBinIndex = firstDistanceBin; distanceBinIndex < endDistanceBin; distanceBinIndex++)
                {
                    cumulativeTimeInSI += timeValues.getSI(distanceBinIndex);
                    cumulativeAccelerationInSI += accelerationValues.getSI(distanceBinIndex);
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
        return cumulativeAccelerationInSI / cumulativeTimeInSI;
    }

}
