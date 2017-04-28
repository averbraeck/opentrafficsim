package org.opentrafficsim.graphs;

import java.util.ArrayList;
import java.util.List;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.StorageType;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.DoubleScalarInterface;
import org.djunits.value.vdouble.vector.MutableAccelerationVector;
import org.djunits.value.vdouble.vector.MutableTimeVector;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.simulationengine.OTSSimulationException;

/**
 * Acceleration contour plot.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-03 13:38:01 +0200 (Thu, 03 Sep 2015) $, @version $Revision: 1378 $, by $Author: averbraeck $,
 * initial version Jul 31, 2014 <br>
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
     * @throws OTSSimulationException in case of problems initializing the graph
     */
    public AccelerationContourPlot(final String caption, final List<Lane> path) throws OTSSimulationException
    {
        super(caption, new Axis(INITIALLOWERTIMEBOUND, INITIALUPPERTIMEBOUND, STANDARDTIMEGRANULARITIES,
                STANDARDTIMEGRANULARITIES[STANDARDINITIALTIMEGRANULARITYINDEX], "", "Time", "%.0fs"), path, -5d, 0d, 3d,
                "acceleration %.1f m/s/s", "%.1f m/s/s", 1d);
    }

    /** {@inheritDoc} */
    @Override
    public final GraphType getGraphType()
    {
        return GraphType.ACCELERATION_CONTOUR;
    }

    /** Storage for the total time spent in each cell. */
    private ArrayList<MutableTimeVector> cumulativeTimes;

    /** Storage for the total acceleration executed in each cell. */
    private ArrayList<MutableAccelerationVector> cumulativeAccelerations;

    /** {@inheritDoc} */
    @Override
    public final Comparable<String> getSeriesKey(final int series)
    {
        return "acceleration";
    }

    /** {@inheritDoc} */
    @Override
    public final void extendXRange(final DoubleScalarInterface newUpperLimit)
    {
        if (null == this.cumulativeTimes)
        {
            this.cumulativeTimes = new ArrayList<MutableTimeVector>();
            this.cumulativeAccelerations = new ArrayList<MutableAccelerationVector>();
        }
        int highestBinNeeded =
                (int) Math.floor(this.getXAxis().getRelativeBin(newUpperLimit) * this.getXAxis().getCurrentGranularity()
                        / this.getXAxis().getGranularities()[0]);
        while (highestBinNeeded >= this.cumulativeTimes.size())
        {
            try
            {
                this.cumulativeTimes.add(new MutableTimeVector(new double[this.getYAxis().getBinCount()], TimeUnit.SECOND,
                        StorageType.DENSE));
                this.cumulativeAccelerations.add(new MutableAccelerationVector(new double[this.getYAxis().getBinCount()],
                        AccelerationUnit.METER_PER_SECOND_2, StorageType.DENSE));
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
        MutableTimeVector timeValues = this.cumulativeTimes.get(timeBin);
        MutableAccelerationVector accelerationValues = this.cumulativeAccelerations.get(timeBin);
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
                MutableTimeVector timeValues = this.cumulativeTimes.get(timeBinIndex);
                MutableAccelerationVector accelerationValues = this.cumulativeAccelerations.get(timeBinIndex);
                for (int distanceBinIndex = firstDistanceBin; distanceBinIndex < endDistanceBin; distanceBinIndex++)
                {
                    cumulativeTimeInSI += timeValues.getSI(distanceBinIndex);
                    cumulativeAccelerationInSI += accelerationValues.getSI(distanceBinIndex);
                }
            }
        }
        catch (ValueException exception)
        {
            System.err.println(String.format("Error in getZValue(timeBinRange=[%d-%d], distanceBinRange=[%d-%d]", firstTimeBin,
                    endTimeBin, firstDistanceBin, endDistanceBin));
            exception.printStackTrace();
        }
        if (0 == cumulativeTimeInSI)
        {
            return Double.NaN;
        }
        return cumulativeAccelerationInSI / cumulativeTimeInSI;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "AccelerationContourPlot [cumulativeTimes.size" + this.cumulativeTimes.size()
                + ", cumulativeAccelerations.size=" + this.cumulativeAccelerations.size() + "]";
    }

}
