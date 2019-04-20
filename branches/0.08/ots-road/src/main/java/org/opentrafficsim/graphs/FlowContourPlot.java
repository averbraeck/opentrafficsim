package org.opentrafficsim.graphs;

import java.util.ArrayList;
import java.util.List;

import org.djunits.unit.LengthUnit;
import org.djunits.value.StorageType;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.vector.MutableLengthVector;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.simulationengine.OTSSimulationException;

/**
 * Flow contour plot.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-03 13:38:01 +0200 (Thu, 03 Sep 2015) $, @version $Revision: 1378 $, by $Author: averbraeck $,
 * initial version Jul 29, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class FlowContourPlot extends ContourPlot
{
    /** */
    private static final long serialVersionUID = 20140729L;

    /**
     * Create a new FlowContourPlot.
     * @param caption String; text to show above the FlowContourPlot
     * @param path List&lt;Lane&gt;; the series of Lanes that will provide the data for this TrajectoryPlot
     * @throws OTSSimulationException in case of problems initializing the graph
     */
    public FlowContourPlot(final String caption, final List<Lane> path) throws OTSSimulationException
    {
        super(caption, new Axis(INITIALLOWERTIMEBOUND, INITIALUPPERTIMEBOUND, STANDARDTIMEGRANULARITIES,
            STANDARDTIMEGRANULARITIES[STANDARDINITIALTIMEGRANULARITYINDEX], "", "Time", "%.0fs"), path, 2500d, 1500d,
            0d, "flow %.0f veh/h", "%.0f veh/h", 500d);
    }

    /** Storage for the total length traveled in each cell. */
    private ArrayList<MutableLengthVector.Abs> cumulativeLengths;

    /** {@inheritDoc} */
    @Override
    public final Comparable<String> getSeriesKey(final int series)
    {
        return "flow";
    }

    /** {@inheritDoc} */
    @Override
    public final void extendXRange(final DoubleScalar<?> newUpperLimit)
    {
        if (null == this.cumulativeLengths)
        {
            this.cumulativeLengths = new ArrayList<MutableLengthVector.Abs>();
        }
        final int highestBinNeeded =
            (int) Math.floor(this.getXAxis().getRelativeBin(newUpperLimit) * this.getXAxis().getCurrentGranularity()
                / this.getXAxis().getGranularities()[0]);
        while (highestBinNeeded >= this.cumulativeLengths.size())
        {
            try
            {
                this.cumulativeLengths.add(new MutableLengthVector.Abs(new double[this.getYAxis().getBinCount()],
                    LengthUnit.METER, StorageType.DENSE));
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
        while (timeBin >= this.cumulativeLengths.size())
        {
            try
            {
                this.cumulativeLengths.add(new MutableLengthVector.Abs(new double[this.getYAxis().getBinCount()],
                    LengthUnit.METER, StorageType.DENSE));
            }
            catch (ValueException exception)
            {
                exception.printStackTrace();
            }
        }
        MutableLengthVector.Abs values = this.cumulativeLengths.get(timeBin);
        try
        {
            values.setSI(distanceBin, values.getSI(distanceBin) + distanceCovered);
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
        double cumulativeLengthInSI = 0;
        if (firstTimeBin >= this.cumulativeLengths.size())
        {
            return Double.NaN;
        }
        try
        {
            for (int timeBinIndex = firstTimeBin; timeBinIndex < endTimeBin; timeBinIndex++)
            {
                if (timeBinIndex >= this.cumulativeLengths.size())
                {
                    break;
                }
                MutableLengthVector.Abs values = this.cumulativeLengths.get(timeBinIndex);
                for (int distanceBinIndex = firstDistanceBin; distanceBinIndex < endDistanceBin; distanceBinIndex++)
                {
                    cumulativeLengthInSI += values.getSI(distanceBinIndex);
                }
            }
        }
        catch (ValueException exception)
        {
            System.err.println(String.format("Error in getZValue(timeBinRange=[%d-%d], distanceBinRange=[%d-%d]",
                firstTimeBin, endTimeBin, firstDistanceBin, endDistanceBin));
            exception.printStackTrace();
        }
        return 3600 * cumulativeLengthInSI / this.getXAxis().getCurrentGranularity()
            / this.getYAxis().getCurrentGranularity();
    }

}