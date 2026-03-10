package org.opentrafficsim.draw.graphs;

import java.awt.Color;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djutils.event.EventListener;
import org.djutils.event.EventType;
import org.djutils.exceptions.Throw;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.data.DomainOrder;
import org.opentrafficsim.draw.BoundsPaintScale;
import org.opentrafficsim.draw.graphs.AbstractContourPlot.ContourPaintState;
import org.opentrafficsim.draw.graphs.AbstractPlot.PaintState;
import org.opentrafficsim.draw.graphs.ContourDataSource.ContourAdditionalDataType;
import org.opentrafficsim.draw.graphs.ContourDataSource.ContourDataType;
import org.opentrafficsim.draw.graphs.ContourDataSource.Dimension;

/**
 * Class for contour plots. The data that is plotted is stored in a {@code ContourDataSource}, which may be shared among several
 * contour plots along the same path. This abstract class takes care of the interactions between the plot and the data source.
 * Sub-classes only need to specify a few plot specific variables and functionalities.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <Z> z-value type
 */
public abstract class AbstractContourPlot<Z extends Number> extends AbstractSpaceTimePlot<ContourPaintState>
        implements XyInterpolatedDataset
{

    /** Color scale for the graph. */
    private final BoundsPaintScale paintScale;

    /** Label data. */
    private final LabelData<Z> labelData;

    /** Data source. */
    private final ContourDataSource source;

    /** Contour data type. */
    private final ContourDataType<Z> contourDataType;

    /** Block renderer in chart. */
    private XyInterpolatedBlockRenderer blockRenderer = null;

    /**
     * Constructor with specified paint scale.
     * @param caption caption
     * @param source data source
     * @param contourDataType contour data type
     * @param paintScale paint scale
     * @param labelData label data
     */
    public AbstractContourPlot(final String caption, final ContourDataSource source, final ContourDataType<Z> contourDataType,
            final BoundsPaintScale paintScale, final LabelData<Z> labelData)
    {
        super(caption, source.getInitialUpdateInterval(), source.getPlotScheduler(), source.getDelay(),
                DEFAULT_INITIAL_UPPER_TIME_BOUND);
        this.source = Throw.whenNull(source, "dataPool");
        this.contourDataType = Throw.whenNull(contourDataType, "contourDataType");
        this.paintScale = Throw.whenNull(paintScale, "paintScale");
        this.labelData = Throw.whenNull(labelData, "labelData");
        this.blockRenderer = new XyInterpolatedBlockRenderer(this);
        this.blockRenderer.setPaintScale(this.paintScale);
        this.blockRenderer.setBlockHeight(source.getGranularity(Dimension.DISTANCE));
        this.blockRenderer.setBlockWidth(source.getGranularity(Dimension.TIME));
        source.addPlot(this);
        setChart(createChart());
    }

    /**
     * Constructor with default paint scale (red at minimum, yellow at mid-point, green at maximum).
     * @param caption caption
     * @param source data source
     * @param contourDataType contour data type
     * @param bounds paint scale bounds
     * @param labelData label data
     */
    public AbstractContourPlot(final String caption, final ContourDataSource source,
            final ContourAdditionalDataType<Z, ?> contourDataType, final Bounds<Z> bounds, final LabelData<Z> labelData)
    {
        this(caption, source, contourDataType, createPaintScale(bounds), labelData);
    }

    /**
     * Creates a default paint scale from red, via yellow to green.
     * @param bounds paint scale bounds
     * @return default paint scale
     */
    private static BoundsPaintScale createPaintScale(final Bounds<?> bounds)
    {
        Throw.when(bounds.minimum().doubleValue() >= bounds.maximum().doubleValue(), IllegalArgumentException.class,
                "Minimum value %s is above or equal to maximum value %s.", bounds.minimum(), bounds.maximum());
        double[] boundaries = {bounds.minimum().doubleValue(),
                (bounds.minimum().doubleValue() + bounds.maximum().doubleValue()) / 2.0, bounds.maximum().doubleValue()};
        Color[] colorValues = {Color.RED, Color.YELLOW, Color.GREEN};
        return new BoundsPaintScale(boundaries, colorValues);
    }

    /**
     * Create a chart.
     * @return chart
     */
    private JFreeChart createChart()
    {
        NumberAxis xAxis = new NumberAxis("Time [s] \u2192");
        NumberAxis yAxis = new NumberAxis("Distance [m] \u2192");
        XYPlot plot = new XYPlot(this, xAxis, yAxis, this.blockRenderer);
        LegendItemCollection legend = new LegendItemCollection();
        for (int i = 0;; i++)
        {
            double value = this.paintScale.getLowerBound() + i * this.labelData.legendStep().doubleValue();
            if (value > this.paintScale.getUpperBound() + 1e-6)
            {
                break;
            }
            legend.add(new LegendItem(String.format(this.labelData.legendFormat(), scale(value)),
                    this.paintScale.getPaint(value)));
        }
        legend.add(new LegendItem("No data", Color.BLACK));
        plot.setFixedLegendItems(legend);
        final JFreeChart chart = new JFreeChart(getCaption(), plot);
        return chart;
    }

    @Override
    protected ContourPaintState emptyPaintState()
    {
        return new ContourPaintState(new float[0], 1.0, 1.0, 0, false, Duration.ZERO);
    }

    /**
     * Returns the contour data type for use in a {@link ContourDataSource}.
     * @return contour data type
     */
    protected ContourDataType<Z> getContourDataType()
    {
        return this.contourDataType;
    }

    /**
     * Scale the z-value from SI to the desired unit for users, in line with unit in {@link LabelData#labelFormat}.
     * @param si SI value
     * @return scaled value
     */
    protected abstract double scale(double si);

    @Override
    protected void setPaintState()
    {
        super.setPaintState();
        this.blockRenderer.setInterpolate(getPaintState().interpolate());
        this.blockRenderer.setBlockHeight(getPaintState().dx());
        this.blockRenderer.setBlockWidth(getPaintState().dt());
    }

    /**
     * Returns the status label when the mouse is over the given location.
     * @param domainValue domain value (x-axis)
     * @param rangeValue range value (y-axis)
     * @return status label when the mouse is over the given location
     */
    @Override
    public String getStatusLabel(final double domainValue, final double rangeValue)
    {
        if (getPaintState().data().length == 0)
        {
            return String.format("time %.0fs, distance %.0fm", domainValue, rangeValue);
        }
        int i = getPaintState().getSpaceSlice(rangeValue);
        int j = getPaintState().getTimeSlice(domainValue);
        int item = j * getPaintState().nSpaceSlices() + i;
        double zValue = scale(getZValue(1, item));
        return String.format("time %.0fs, distance %.0fm, " + this.labelData.labelFormat(), domainValue, rangeValue, zValue);
    }

    // ===== XyInterpolatedDataset =====

    @Override
    public int getItemCount(final int series)
    {
        return getPaintState().getItemCount();
    }

    @Override
    public Number getX(final int series, final int item)
    {
        return getXValue(series, item);
    }

    @Override
    public double getXValue(final int series, final int item)
    {
        return getPaintState().getTimeValue(item);
    }

    @Override
    public Number getY(final int series, final int item)
    {
        return getYValue(series, item);
    }

    @Override
    public double getYValue(final int series, final int item)
    {
        return getPaintState().getSpaceValue(item);
    }

    @Override
    public Number getZ(final int series, final int item)
    {
        return getZValue(series, item);
    }

    @Override
    public Comparable<String> getSeriesKey(final int series)
    {
        return getCaption();
    }

    @SuppressWarnings("rawtypes")
    @Override
    public int indexOf(final Comparable seriesKey)
    {
        return 0;
    }

    @Override
    public DomainOrder getDomainOrder()
    {
        return DomainOrder.ASCENDING;
    }

    @Override
    public double getZValue(final int series, final int item)
    {
        // default 1 series
        return getPaintState().data()[item];
    }

    @Override
    public int getSeriesCount()
    {
        return 1; // default
    }

    @Override
    public int getRangeBinCount()
    {
        return getPaintState().nSpaceSlices();
    }

    // ===== Delegate forwards =====

    /**
     * Add listener to synchronize UI element when another UI element changes a setting, or the setting is changed
     * programmatically.
     * @param listener listener
     * @param eventType event type (i.e. {@code ContourDataSource.GRANULARITY}, {@code ContourDataSource.INTERPOLATE} or
     *            {@code ContourDataSource.SMOOTH})
     */
    public void addListener(final EventListener listener, final EventType eventType)
    {
        this.source.addListener(listener, eventType);
    }

    /**
     * Returns the available granularities that a linked plot may use.
     * @param dimension space or time
     * @return available granularities that a linked plot may use
     */
    public double[] getGranularities(final Dimension dimension)
    {
        return this.source.getGranularities(dimension);
    }

    /**
     * Returns the selected granularity that a linked plot should use.
     * @param dimension space or time
     * @return granularity that a linked plot should use
     */
    public double getGranularity(final Dimension dimension)
    {
        return this.source.getGranularity(dimension);
    }

    /**
     * Sets the granularity of the plot. This will invalidate the plot triggering a redraw.
     * @param dimension space or time
     * @param granularity granularity in space or time (SI unit)
     */
    public void setGranularity(final Dimension dimension, final double granularity)
    {
        this.source.setGranularity(dimension, granularity);
        invalidate();
    }

    /**
     * Sets bi-linear interpolation enabled or disabled. This will invalidate the plot triggering a redraw.
     * @param interpolate whether to enable interpolation
     */
    public void setInterpolate(final boolean interpolate)
    {
        this.source.setInterpolate(interpolate);
        invalidate();
    }

    /**
     * Sets the adaptive smoothing enabled or disabled. This will invalidate the plot triggering a redraw.
     * @param smooth whether to smooth the plot
     */
    public void setSmooth(final boolean smooth)
    {
        this.source.setSmooth(smooth);
        invalidate();
    }

    @Override
    protected void calculatePaintState(final Duration time)
    {
        this.source.calculatePaintStateSafe(time);
    }

    @Override
    protected Length getEndLocation()
    {
        return this.source.getPath().getTotalLength();
    }

    // ===== Helper classes =====

    /**
     * Bounds for the color bounds scale.
     * @param minimum minimum value
     * @param maximum maximum value
     * @param <Z> value type
     */
    public record Bounds<Z extends Number>(Z minimum, Z maximum)
    {
    }

    /**
     * Input container for label input.
     * @param legendStep increment between color legend entries
     * @param legendFormat format string for the captions in the color legend, for example {@code %.1fm/s} for {@code 30.2m/s}
     * @param labelFormat format string used to create status label (under the mouse), e.g. {@code desired speed %.1f m/s}
     * @param <Z> value type
     */
    public record LabelData<Z extends Number>(Z legendStep, String legendFormat, String labelFormat)
    {
    }

    /**
     * Paint state for contour plots.
     * @param data data as flat array
     * @param dx space granularity
     * @param dt time granularity
     * @param nSpaceSlices number of slices for space
     * @param interpolate whether to interpolate
     * @param getAvailableTime available time
     */
    public record ContourPaintState(float[] data, double dx, double dt, int nSpaceSlices, boolean interpolate,
            Duration getAvailableTime) implements PaintState
    {

        /**
         * Returns the number of available items.
         * @return number of available items
         */
        public int getItemCount()
        {
            return data().length;
        }

        /**
         * Returns the time slice number of the item.
         * @param item item number
         * @return time slice number of the item
         */
        public double getTimeValue(final int item)
        {
            return dt() * (item / nSpaceSlices());
        }

        /**
         * Returns the space slice number of the item.
         * @param item item number
         * @return space slice number of the item
         */
        public double getSpaceValue(final int item)
        {
            return dx() * (item % nSpaceSlices());
        }

        /**
         * Returns the time slice index for the given time value.
         * @param t time value
         * @return time slice index
         */
        public int getTimeSlice(final double t)
        {
            int n = (int) (t / dt());
            int nMax = data().length / nSpaceSlices();
            return n < 0 ? 0 : (n > nMax ? nMax : n);
        }

        /**
         * Returns the space slice index for the given space value.
         * @param x space value
         * @return space slice index
         */
        public int getSpaceSlice(final double x)
        {
            int n = (int) (x / dx());
            return n < 0 ? 0 : (n > nSpaceSlices() ? nSpaceSlices() : n);
        }

    }

}
