package org.opentrafficsim.draw.graphs;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.DomainOrder;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.draw.core.BoundsPaintScale;
import org.opentrafficsim.draw.graphs.ContourDataSource.ContourDataType;
import org.opentrafficsim.draw.graphs.ContourDataSource.Dimension;

/**
 * Class for contour plots. The data that is plotted is stored in a {@code ContourDataSource}, which may be shared among several
 * contour plots along the same path. This abstract class takes care of the interactions between the plot and the data pool. Sub
 * classes only need to specify a few plot specific variables and functionalities.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 4 okt. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <Z> z-value type
 */
public abstract class AbstractContourPlot<Z extends Number> extends AbstractSamplerPlot
        implements XYInterpolatedDataset, ActionListener
{

    /** Color scale for the graph. */
    private final BoundsPaintScale paintScale;

    /** Difference of successive values in the legend. */
    private final Z legendStep;

    /** Format string used to create the captions in the legend. */
    private final String legendFormat;

    /** Format string used to create status label (under the mouse). */
    private final String valueFormat;

    /** Data pool. */
    private final ContourDataSource<?> dataPool;

    /** Block renderer in chart. */
    private XYInterpolatedBlockRenderer blockRenderer = null;

    /**
     * Constructor with specified paint scale.
     * @param caption String; caption
     * @param simulator OTSSimulatorInterface; simulator
     * @param dataPool ContourDataSource&lt;?&gt;; data pool
     * @param paintScale BoundsPaintScale; paint scale
     * @param legendStep Z; increment between color legend entries
     * @param legendFormat String; format string for the captions in the color legend
     * @param valueFormat String; format string used to create status label (under the mouse)
     */
    public AbstractContourPlot(final String caption, final OTSSimulatorInterface simulator, final ContourDataSource<?> dataPool,
            final BoundsPaintScale paintScale, final Z legendStep, final String legendFormat, final String valueFormat)
    {
        super(caption, dataPool.getUpdateInterval(), simulator, dataPool.getSampler(), dataPool.getPath(), dataPool.getDelay());
        dataPool.registerContourPlot(this);
        this.dataPool = dataPool;
        this.paintScale = paintScale;
        this.legendStep = legendStep;
        this.legendFormat = legendFormat;
        this.valueFormat = valueFormat;
        this.blockRenderer = new XYInterpolatedBlockRenderer(this);
        this.blockRenderer.setPaintScale(this.paintScale);
        this.blockRenderer.setBlockHeight(dataPool.getGranularity(Dimension.DISTANCE));
        this.blockRenderer.setBlockWidth(dataPool.getGranularity(Dimension.TIME));
        setChart(createChart());
    }

    /**
     * Constructor with default paint scale.
     * @param caption String; caption
     * @param simulator OTSSimulatorInterface; simulator
     * @param dataPool ContourDataSource&lt;?&gt;; data pool
     * @param legendStep Z; increment between color legend entries
     * @param legendFormat String; format string for the captions in the color legend
     * @param minValue Z; minimum value
     * @param maxValue Z; maximum value
     * @param valueFormat String; format string used to create status label (under the mouse)
     */
    @SuppressWarnings("parameternumber")
    public AbstractContourPlot(final String caption, final OTSSimulatorInterface simulator, final ContourDataSource<?> dataPool,
            final Z legendStep, final String legendFormat, final Z minValue, final Z maxValue, final String valueFormat)
    {
        this(caption, simulator, dataPool, createPaintScale(minValue, maxValue), legendStep, legendFormat, valueFormat);
    }

    /**
     * Creates a default paint scale from red, via yellow to green.
     * @param minValue Number; minimum value
     * @param maxValue Number; maximum value
     * @return BoundsPaintScale; default paint scale
     */
    private static BoundsPaintScale createPaintScale(final Number minValue, final Number maxValue)
    {
        Throw.when(minValue.doubleValue() >= maxValue.doubleValue(), IllegalArgumentException.class,
                "Minimum value %s is below or equal to maxumum value %s.", minValue, maxValue);
        double[] boundaries =
                {minValue.doubleValue(), (minValue.doubleValue() + maxValue.doubleValue()) / 2.0, maxValue.doubleValue()};
        Color[] colorValues = {Color.RED, Color.YELLOW, Color.GREEN};
        return new BoundsPaintScale(boundaries, colorValues);
    }

    /**
     * Create a chart.
     * @return JFreeChart; chart
     */
    private JFreeChart createChart()
    {
        NumberAxis xAxis = new NumberAxis("Time [s] \u2192");
        NumberAxis yAxis = new NumberAxis("Distance [m] \u2192");
        XYPlot plot = new XYPlot(this, xAxis, yAxis, this.blockRenderer);
        LegendItemCollection legend = new LegendItemCollection();
        for (int i = 0;; i++)
        {
            double value = this.paintScale.getLowerBound() + i * this.legendStep.doubleValue();
            if (value > this.paintScale.getUpperBound() + 1e-6)
            {
                break;
            }
            legend.add(new LegendItem(String.format(this.legendFormat, scale(value)), this.paintScale.getPaint(value)));
        }
        legend.add(new LegendItem("No data", Color.BLACK));
        plot.setFixedLegendItems(legend);
        final JFreeChart chart = new JFreeChart(getCaption(), plot);
        return chart;
    }

    /**
     * Returns the time granularity, just for information.
     * @return double; time granularity
     */
    public double getTimeGranularity()
    {
        return this.dataPool.getGranularity(Dimension.TIME);
    }

    /**
     * Returns the space granularity, just for information.
     * @return double; space granularity
     */
    public double getSpaceGranularity()
    {
        return this.dataPool.getGranularity(Dimension.DISTANCE);
    }

    /**
     * Sets the correct space granularity radio button to selected. This is done from a {@code DataPool} to keep multiple plots
     * consistent.
     * @param granularity double; space granularity
     */
    public final void setSpaceGranularity(final double granularity)
    {
        this.blockRenderer.setBlockHeight(granularity);
    }

    /**
     * Sets the correct time granularity radio button to selected. This is done from a {@code DataPool} to keep multiple plots
     * consistent.
     * @param granularity double; time granularity
     */
    public final void setTimeGranularity(final double granularity)
    {
        this.blockRenderer.setBlockWidth(granularity);
    }

    /**
     * Sets the check box for interpolated rendering and block renderer setting. This is done from a {@code DataPool} to keep
     * multiple plots consistent.
     * @param interpolate boolean; selected or not
     */
    public final void setInterpolation(final boolean interpolate)
    {
        this.blockRenderer.setInterpolate(interpolate);
    }
    
    /**
     * Returns the data pool for sub classes.
     * @return ContourDataSource; data pool for subclasses
     */
    public final ContourDataSource<?> getDataPool()
    {
        return this.dataPool;
    }

    /** {@inheritDoc} */
    @Override
    public final int getItemCount(final int series)
    {
        return this.dataPool.getBinCount(Dimension.DISTANCE) * this.dataPool.getBinCount(Dimension.TIME);
    }

    /** {@inheritDoc} */
    @Override
    public final Number getX(final int series, final int item)
    {
        return getXValue(series, item);
    }

    /** {@inheritDoc} */
    @Override
    public final double getXValue(final int series, final int item)
    {
        return this.dataPool.getAxisValue(Dimension.TIME, item);
    }

    /** {@inheritDoc} */
    @Override
    public final Number getY(final int series, final int item)
    {
        return getYValue(series, item);
    }

    /** {@inheritDoc} */
    @Override
    public final double getYValue(final int series, final int item)
    {
        return this.dataPool.getAxisValue(Dimension.DISTANCE, item);
    }

    /** {@inheritDoc} */
    @Override
    public final Number getZ(final int series, final int item)
    {
        return getZValue(series, item);
    }

    /** {@inheritDoc} */
    @Override
    public final Comparable<String> getSeriesKey(final int series)
    {
        return getCaption();
    }

    /** {@inheritDoc} */
    @SuppressWarnings("rawtypes")
    @Override
    public final int indexOf(final Comparable seriesKey)
    {
        return 0;
    }

    /** {@inheritDoc} */
    @Override
    public final DomainOrder getDomainOrder()
    {
        return DomainOrder.ASCENDING;
    }

    /** {@inheritDoc} */
    @Override
    public final double getZValue(final int series, final int item)
    {
        // default 1 series
        return getValue(item, this.dataPool.getGranularity(Dimension.DISTANCE), this.dataPool.getGranularity(Dimension.TIME));
    }

    /** {@inheritDoc} */
    @Override
    public final int getSeriesCount()
    {
        return 1; // default
    }

    /** {@inheritDoc} */
    @Override
    public int getRangeBinCount()
    {
        return this.dataPool.getBinCount(Dimension.DISTANCE);
    }

    /**
     * Returns the status label when the mouse is over the given location.
     * @param domainValue double; domain value (x-axis)
     * @param rangeValue double; range value (y-axis)
     * @return String; status label when the mouse is over the given location
     */
    @Override
    public final String getStatusLabel(final double domainValue, final double rangeValue)
    {
        if (this.dataPool == null)
        {
            return String.format("time %.0fs, distance %.0fm", domainValue, rangeValue);
        }
        int i = this.dataPool.getAxisBin(Dimension.DISTANCE, rangeValue);
        int j = this.dataPool.getAxisBin(Dimension.TIME, domainValue);
        int item = j * this.dataPool.getBinCount(Dimension.DISTANCE) + i;
        double zValue = scale(
                getValue(item, this.dataPool.getGranularity(Dimension.DISTANCE), this.dataPool.getGranularity(Dimension.TIME)));
        return String.format("time %.0fs, distance %.0fm, " + this.valueFormat, domainValue, rangeValue, zValue);
    }

    /** {@inheritDoc} */
    @Override
    protected final void increaseTime(final Time time)
    {
        if (this.dataPool != null) // dataPool is null at construction
        {
            this.dataPool.increaseTime(time);
        }
    }

    /**
     * Obtain value for cell from the data pool.
     * @param item int; item number
     * @param cellLength double; cell length
     * @param cellSpan double; cell duration
     * @return double; value for cell from the data pool
     */
    protected abstract double getValue(int item, double cellLength, double cellSpan);

    /**
     * Scale the value from SI to the desired unit for users.
     * @param si double; SI value
     * @return double; scaled value
     */
    protected abstract double scale(double si);

    /**
     * Returns the contour data type for use in a {@code ContourDataSource}.
     * @return CountorDataType; contour data type
     */
    protected abstract ContourDataType<Z, ?> getContourDataType();

    public XYInterpolatedBlockRenderer getBlockRenderer()
    {
        return blockRenderer;
    }

    @Override
    public final void actionPerformed(final ActionEvent actionEvent)
    {
        String command = actionEvent.getActionCommand();
        if (command.equalsIgnoreCase("setSpaceGranularity"))
        {
            // The source field is abused to contain the granularity
            double granularity = (double) actionEvent.getSource();
            setSpaceGranularity(granularity);
        }
        else if (command.equalsIgnoreCase("setTimeGranularity"))
        {
            // The source field is abused to contain the granularity
            double granularity = (double) actionEvent.getSource();
            setTimeGranularity(granularity);
        }
        else
        {
            throw new RuntimeException("Unhandled ActionEvent");
        }
    }

}
