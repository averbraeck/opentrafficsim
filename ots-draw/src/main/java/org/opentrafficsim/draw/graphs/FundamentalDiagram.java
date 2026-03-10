package org.opentrafficsim.draw.graphs;

import java.awt.Color;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Duration;
import org.djutils.event.EventListener;
import org.djutils.event.EventType;
import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.ImmutableList;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.DomainOrder;
import org.jfree.data.xy.XYDataset;
import org.opentrafficsim.draw.graphs.AbstractPlot.PaintState;
import org.opentrafficsim.draw.graphs.FundamentalDiagram.FdPaintState;

/**
 * Fundamental diagram from various sources.
 * <p>
 * Copyright (c) 2013-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class FundamentalDiagram extends AbstractBoundedPlot<FdPaintState> implements XYDataset
{

    /** Source providing the data. */
    private final FdDataSource source;

    /** Fundamental diagram line. */
    private final FdLine fdLine;

    /** Quantity on domain axis. */
    private Quantity domainQuantity;

    /** Quantity on range axis. */
    private Quantity rangeQuantity;

    /** The other, 3rd quantity. */
    private Quantity otherQuantity;

    /** Labels of series. */
    private final List<String> seriesLabels = new ArrayList<>();

    /** Property for chart listener to provide time info for status label. */
    private String timeInfo = "";

    /** Legend to change text color to indicate visibility. */
    private LegendItemCollection legend;

    /** Whether each lane is visible or not. */
    private final List<Boolean> laneVisible = new ArrayList<>();

    /**
     * Constructor.
     * @param caption caption
     * @param domainQuantity initial quantity on the domain axis
     * @param rangeQuantity initial quantity on the range axis
     * @param source source providing the data
     * @param fdLine fundamental diagram line, may be {@code null}
     */
    public FundamentalDiagram(final String caption, final Quantity domainQuantity, final Quantity rangeQuantity,
            final FdDataSource source, final FdLine fdLine)
    {
        super(source.getPlotScheduler(), caption, source.getAggregationPeriod(), source.getDelay());
        Throw.when(domainQuantity.equals(rangeQuantity), IllegalArgumentException.class,
                "Domain and range quantity should not be equal.");
        this.fdLine = fdLine;
        this.setDomainQuantity(domainQuantity);
        this.setRangeQuantity(rangeQuantity);
        Set<Quantity> quantities = EnumSet.allOf(Quantity.class);
        quantities.remove(domainQuantity);
        quantities.remove(rangeQuantity);
        this.setOtherQuantity(quantities.iterator().next());
        this.source = source;
        int d = 0;
        if (fdLine != null)
        {
            d = 1;
            this.seriesLabels.add(fdLine.getName());
            this.laneVisible.add(true);
        }
        for (int series = 0; series < source.getNumberOfSeries(); series++)
        {
            this.seriesLabels.add(series + d, source.getName(series));
            this.laneVisible.add(true);
        }
        setChart(createChart());
        setLowerDomainBound(0.0);
        setLowerRangeBound(0.0);

        // let this diagram be notified by the source
        source.addPlot(this);
    }

    /**
     * Create a chart.
     * @return chart
     */
    private JFreeChart createChart()
    {
        NumberAxis xAxis = new NumberAxis(this.getDomainQuantity().label());
        NumberAxis yAxis = new NumberAxis(this.getRangeQuantity().label());
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer()
        {
            /** */
            private static final long serialVersionUID = 20181022L;

            @Override
            public boolean isSeriesVisible(final int series)
            {
                return FundamentalDiagram.this.laneVisible.get(series);
            }
        }; // XYDotRenderer doesn't support different markers
        renderer.setDefaultLinesVisible(false);
        if (hasLineFD())
        {
            int series = getNumberOfSeries();
            renderer.setSeriesLinesVisible(series, true);
            renderer.setSeriesPaint(series, Color.BLACK);
            renderer.setSeriesShapesVisible(series, false);
        }
        XYPlot plot = new XYPlot(this, xAxis, yAxis, renderer);
        boolean showLegend = true;
        if (!hasLineFD() && getNumberOfSeries() < 2)
        {
            plot.setFixedLegendItems(null);
            showLegend = false;
        }
        else
        {
            this.legend = new LegendItemCollection();
            for (int i = 0; i < getNumberOfSeries(); i++)
            {
                LegendItem li = new LegendItem(this.source.getName(i));
                li.setSeriesKey(i); // lane series, not curve series
                li.setShape(renderer.lookupLegendShape(i));
                li.setFillPaint(renderer.lookupSeriesPaint(i));
                this.legend.add(li);
            }
            if (hasLineFD())
            {
                LegendItem li = new LegendItem(this.fdLine.getName());
                li.setSeriesKey(-1);
                this.legend.add(li);
            }
            plot.setFixedLegendItems(this.legend);
            showLegend = true;
        }
        return new JFreeChart(getCaption(), JFreeChart.DEFAULT_TITLE_FONT, plot, showLegend);
    }

    /**
     * Returns the possible updates per period values.
     * @return possible update per period values
     */
    public ImmutableList<Integer> getPossibleUpdatesPerPeriod()
    {
        return this.source.getUpdatesPerPeriodSetting().values();
    }

    /**
     * Returns the default updates per period.
     * @return default updates per period
     */
    public int getDefaultUpdatesPerPeriod()
    {
        return this.source.getUpdatesPerPeriodSetting().getDefaultValue();
    }

    /**
     * Returns the possible aggregation period values.
     * @return possible aggregation period values
     */
    public ImmutableList<Duration> getPossibleAggregationPeriods()
    {
        return this.source.getAggregationPeriodSetting().values();
    }

    /**
     * Returns the default aggregation period.
     * @return default aggregation period
     */
    public Duration getDefaultAggregationPeriod()
    {
        return this.source.getAggregationPeriodSetting().getDefaultValue();
    }

    /**
     * Add listener to synchronize UI element when another UI element changes a setting, or the setting is changed
     * programmatically.
     * @param listener listener
     * @param eventType event type (i.e. {@code UPDATES_PER_PERIOD} or {@code AGGREGATION_PERIOD})
     */
    public void addListener(final EventListener listener, final EventType eventType)
    {
        this.source.addListener(listener, eventType);
    }

    /**
     * Returns the number of series.
     * @return number of series
     */
    public int getNumberOfSeries()
    {
        return this.source.getNumberOfSeries();
    }

    /**
     * Returns the update interval.
     * @return update interval
     */
    public Duration getUpdateInterval()
    {
        return this.source.getUpdateInterval();
    }

    /**
     * Sets the number of updates per period.
     * @param n number of updates per period
     */
    public void setUpdatesPerPeriod(final int n)
    {
        this.source.setUpdatesPerPeriod(n);
        invalidate();
    }

    /**
     * Sets the aggregation period.
     * @param period aggregation period
     */
    public void setAggregationPeriod(final Duration period)
    {
        this.source.setAggregationPeriod(period);
        invalidate();
    }

    @Override
    protected FdPaintState emptyPaintState()
    {
        return new FdPaintState(new FdSeries[0], Duration.ZERO);
    }

    @Override
    protected void calculatePaintState(final Duration time)
    {
        this.source.calculatePaintStateSafe(time);
    }

    @Override
    protected void setPaintState()
    {
        super.setPaintState();
        getChart().getXYPlot().zoomDomainAxes(0, null, null);
        getChart().getXYPlot().zoomRangeAxes(0, null, null);
    }

    @Override
    public int getSeriesCount()
    {
        return getPaintState().getSeriesCount() + (hasLineFD() ? 1 : 0);
    }

    @Override
    public Comparable<String> getSeriesKey(final int series)
    {
        return this.seriesLabels.get(series);
    }

    @SuppressWarnings("rawtypes")
    @Override
    public int indexOf(final Comparable seriesKey)
    {
        int index = this.seriesLabels.indexOf(seriesKey);
        return index < 0 ? 0 : index;
    }

    @Override
    public DomainOrder getDomainOrder()
    {
        return DomainOrder.NONE;
    }

    @Override
    public int getItemCount(final int series)
    {
        if (hasLineFD() && series == getSeriesCount() - 1)
        {
            return this.fdLine.getValues(this.domainQuantity).length;
        }
        return getPaintState().getItemCount(series);
    }

    @Override
    public Number getX(final int series, final int item)
    {
        return getXValue(series, item);
    }

    @Override
    public double getXValue(final int series, final int item)
    {
        if (hasLineFD() && series == getSeriesCount() - 1)
        {
            return this.fdLine.getValues(this.domainQuantity)[item];
        }
        return getPaintState().getValue(getDomainQuantity(), series, item);
    }

    @Override
    public Number getY(final int series, final int item)
    {
        return getYValue(series, item);
    }

    @Override
    public double getYValue(final int series, final int item)
    {
        if (hasLineFD() && series == getSeriesCount() - 1)
        {
            return this.fdLine.getValues(this.rangeQuantity)[item];
        }
        return getPaintState().getValue(getRangeQuantity(), series, item);
    }

    @Override
    public GraphType getGraphType()
    {
        return GraphType.FUNDAMENTAL_DIAGRAM;
    }

    @Override
    public String getStatusLabel(final double domainValue, final double rangeValue)
    {
        return getDomainQuantity().format(domainValue) + ", " + getRangeQuantity().format(rangeValue) + ", "
                + getOtherQuantity().format(getDomainQuantity().computeOther(getRangeQuantity(), domainValue, rangeValue))
                + getTimeInfo();
    }

    /**
     * Retrieve the legend of this FundamentalDiagram.
     * @return the legend
     */
    public LegendItemCollection getLegend()
    {
        return this.legend;
    }

    /**
     * Return the list of lane visibility flags.
     * @return the list of lane visibility flags
     */
    public List<Boolean> getLaneVisible()
    {
        return this.laneVisible;
    }

    /**
     * Return the domain quantity.
     * @return the domain quantity
     */
    public Quantity getDomainQuantity()
    {
        return this.domainQuantity;
    }

    /**
     * Set the domain quantity.
     * @param domainQuantity the new domain quantity
     */
    public void setDomainQuantity(final Quantity domainQuantity)
    {
        this.domainQuantity = domainQuantity;
    }

    /**
     * Get the other (non domain; vertical axis) quantity.
     * @return the quantity for the vertical axis
     */
    public Quantity getOtherQuantity()
    {
        return this.otherQuantity;
    }

    /**
     * Set the other (non domain; vertical axis) quantity.
     * @param otherQuantity the quantity for the vertical axis
     */
    public void setOtherQuantity(final Quantity otherQuantity)
    {
        this.otherQuantity = otherQuantity;
    }

    /**
     * Get the range quantity.
     * @return the range quantity
     */
    public Quantity getRangeQuantity()
    {
        return this.rangeQuantity;
    }

    /**
     * Set the range quantity.
     * @param rangeQuantity the new range quantity
     */
    public void setRangeQuantity(final Quantity rangeQuantity)
    {
        this.rangeQuantity = rangeQuantity;
    }

    /**
     * Retrieve the time info.
     * @return the time info
     */
    public String getTimeInfo()
    {
        return this.timeInfo;
    }

    /**
     * Set the time info.
     * @param timeInfo the new time info
     */
    public void setTimeInfo(final String timeInfo)
    {
        this.timeInfo = timeInfo;
    }

    /**
     * Return whether the plot has a fundamental diagram line.
     * @return whether the plot has a fundamental diagram line
     */
    public boolean hasLineFD()
    {
        return this.fdLine != null;
    }

    @Override
    public String toString()
    {
        return "FundamentalDiagram [domainQuantity=" + this.getDomainQuantity() + ", rangeQuantity=" + this.getRangeQuantity()
                + "]";
    }

    // ===== Helper classes =====

    /**
     * Quantity enum defining density, flow and speed.
     */
    public enum Quantity
    {
        /** Density. */
        DENSITY
        {
            @Override
            public String label()
            {
                return "Density [veh/km] \u2192";
            }

            @Override
            public String format(final double value)
            {
                return String.format("%.0f veh/km", value);
            }

            @Override
            public double computeOther(final Quantity pairing, final double thisValue, final double pairedValue)
            {
                // .......................... speed = flow / density .. flow = density * speed
                return pairing.equals(FLOW) ? pairedValue / thisValue : thisValue * pairedValue;
            }
        },

        /** Flow. */
        FLOW
        {
            @Override
            public String label()
            {
                return "Flow [veh/h] \u2192";
            }

            @Override
            public String format(final double value)
            {
                return String.format("%.0f veh/h", value);
            }

            @Override
            public double computeOther(final Quantity pairing, final double thisValue, final double pairedValue)
            {
                // speed = flow * density ... density = flow / speed
                return pairing.equals(DENSITY) ? thisValue * pairedValue : thisValue / pairedValue;
            }
        },

        /** Speed. */
        SPEED
        {
            @Override
            public String label()
            {
                return "Speed [km/h] \u2192";
            }

            @Override
            public String format(final double value)
            {
                return String.format("%.1f km/h", value);
            }

            @Override
            public double computeOther(final Quantity pairing, final double thisValue, final double pairedValue)
            {
                // ............................. flow = speed * density .. density = flow / speed
                return pairing.equals(DENSITY) ? thisValue * pairedValue : pairedValue / thisValue;
            }
        };

        /**
         * Returns an axis label of the quantity.
         * @return axis label of the quantity
         */
        public abstract String label();

        /**
         * Formats a value for status display.
         * @param value value
         * @return formatted string including quantity
         */
        public abstract String format(double value);

        /**
         * Compute the value of the 3rd quantity.
         * @param pairing quantity on other axis
         * @param thisValue value of this quantity
         * @param pairedValue value of the paired quantity on the other axis
         * @return value of the 3rd quantity
         */
        public abstract double computeOther(Quantity pairing, double thisValue, double pairedValue);

    }

    /**
     * Defines a line plot for a fundamental diagram.
     */
    public interface FdLine
    {

        /**
         * Return the values for the given quantity. For two quantities, this should result in a 2D fundamental diagram line.
         * @param quantity quantity to return value for.
         * @return values for quantity
         */
        double[] getValues(Quantity quantity);

        /**
         * Returns the name of the line, as shown in the legend.
         * @return name of the line, as shown in the legend
         */
        String getName();

    }

    /**
     * One data series within a fundamental diagram paint state.
     * @param q flow
     * @param v speed
     * @param k density
     */
    public record FdSeries(float[] q, float[] v, float[] k)
    {

        /**
         * Returns one value for a quantity.
         * @param quantity quantity
         * @param item item in series
         * @return one value for a quantity
         */
        public double getValue(final Quantity quantity, final int item)
        {
            switch (quantity)
            {
                case DENSITY:
                    return k()[item];
                case FLOW:
                    return q()[item];
                case SPEED:
                    return v()[item];
                default:
                    throw new IllegalArgumentException("Unknown quantity " + quantity);
            }
        }

    }

    /**
     * Paint state for fundamental diagram.
     * @param fdSeries data series
     * @param getAvailableTime available time
     */
    public record FdPaintState(FdSeries[] fdSeries, Duration getAvailableTime) implements PaintState
    {

        /**
         * Returns the number of series.
         * @return number of series
         */
        public int getSeriesCount()
        {
            return this.fdSeries().length;
        }

        /**
         * Get number of items for series.
         * @param series series
         * @return number of items for series
         */
        private int getItemCount(final int series)
        {
            return fdSeries()[series].k().length;
        }

        /**
         * Get value for quantity.
         * @param quantity quantity
         * @param series series
         * @param item item in series
         * @return value for quantity
         */
        private double getValue(final Quantity quantity, final int series, final int item)
        {
            return fdSeries()[series].getValue(quantity, item);
        }

    }

}
