package org.opentrafficsim.graphs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingConstants;
import javax.swing.event.EventListenerList;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYZDataset;
import org.opentrafficsim.car.Car;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * Common code for a contour plot. <br>
 * The data collection code for acceleration assumes constant acceleration during the evaluation period of the GTU.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Jul 16, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class ContourPlot extends JFrame implements ActionListener, XYZDataset
{
    /** */
    private static final long serialVersionUID = 20140716L;

    /** The ChartPanel for this ContourPlot. */
    private final ChartPanel chartPanel;

    /**
     * @return chartPanel
     */
    public final ChartPanel getChartPanel()
    {
        return this.chartPanel;
    }

    /** Area to show status information. */
    private final JLabel statusLabel;

    /** Definition of the X-axis. */
    private final Axis xAxis;

    /** Definition of the Y-axis. */
    private final Axis yAxis;

    /** Time granularity values. */
    protected static final double[] STANDARDTIMEGRANULARITIES = { 1, 2, 5, 10, 20, 30, 60, 120, 300, 600 };

    /** Index of the initial time granularity in standardTimeGranularites. */
    protected static final int STANDARDINITIALTIMEGRANULARITYINDEX = 3;

    /** Distance granularity values. */
    protected static final double[] STANDARDDISTANCEGRANULARITIES = { 10, 20, 50, 100, 200, 500, 1000 };

    /** Index of the initial distance granularity in standardTimeGranularites. */
    protected static final int STANDARDINITIALDISTANCEGRANULARITYINDEX = 3;

    /** Initial lower bound for the time scale. */
    protected static final DoubleScalar.Abs<TimeUnit> INITIALLOWERTIMEBOUND = new DoubleScalar.Abs<TimeUnit>(0,
            TimeUnit.SECOND);

    /** Initial upper bound for the time scale. */
    protected static final DoubleScalar.Abs<TimeUnit> INITIALUPPERTIMEBOUND = new DoubleScalar.Abs<TimeUnit>(300,
            TimeUnit.SECOND);

    /**
     * Create a new ContourPlot.
     * @param caption String; text to show above the plotting area
     * @param xAxis Axis; the X (time) axis
     * @param yAxis Axis; the Y axis
     * @param redValue Double; contour value that will be rendered in Red
     * @param yellowValue Double; contour value that will be rendered in Yellow
     * @param greenValue Double; contour value that will be rendered in Green
     * @param valueFormat String; format string for the contour values
     * @param legendFormat String; format string for the captions in the color legend
     * @param legendStep Double; increment between color legend entries
     */
    public ContourPlot(final String caption, final Axis xAxis, final Axis yAxis, final double redValue,
            final double yellowValue, final double greenValue, final String valueFormat, final String legendFormat,
            final double legendStep)
    {
        this.xAxis = xAxis;
        this.yAxis = yAxis;
        extendXRange(xAxis.getMaximumValue());
        double[] boundaries = { redValue, yellowValue, greenValue };
        this.chartPanel = new ChartPanel(createChart(caption, valueFormat, this, boundaries, legendFormat, legendStep));
        final PointerHandler ph = new PointerHandler()
        {
            /** {@inheritDoc} */
            @Override
            void updateHint(final double domainValue, final double rangeValue)
            {
                if (Double.isNaN(domainValue))
                {
                    setStatusText(" ");
                    return;
                }
                XYPlot plot = (XYPlot) getChartPanel().getChart().getPlot();
                XYZDataset dataset = (XYZDataset) plot.getDataset();
                String value = "";
                double roundedTime = domainValue;
                double roundedDistance = rangeValue;
                for (int item = dataset.getItemCount(0); --item >= 0;)
                {
                    double x = dataset.getXValue(0, item);
                    if (x + xAxis.getCurrentGranularity() / 2 < domainValue
                            || x - xAxis.getCurrentGranularity() / 2 >= domainValue)
                    {
                        continue;
                    }
                    double y = dataset.getYValue(0, item);
                    if (y + yAxis.getCurrentGranularity() / 2 < rangeValue
                            || y - yAxis.getCurrentGranularity() / 2 >= rangeValue)
                    {
                        continue;
                    }
                    roundedTime = x;
                    roundedDistance = y;
                    double valueUnderMouse = dataset.getZValue(0, item);
                    // System.out.println("Value under mouse is " + valueUnderMouse);
                    if (Double.isNaN(valueUnderMouse))
                    {
                        break;
                    }
                    String format =
                            ((ContinuousColorPaintScale) (((XYBlockRenderer) (plot.getRenderer(0))).getPaintScale()))
                                    .getFormat();
                    value = String.format(format, valueUnderMouse);
                }
                setStatusText(String.format("time %.0fs, distance %.0fm, %s", roundedTime, roundedDistance, value));
            }

        };
        this.chartPanel.addMouseMotionListener(ph);
        this.chartPanel.addMouseListener(ph);
        this.chartPanel.setMouseWheelEnabled(true);
        add(this.chartPanel, BorderLayout.CENTER);
        this.statusLabel = new JLabel(" ", SwingConstants.CENTER);
        add(this.statusLabel, BorderLayout.SOUTH);
        final JPopupMenu popupMenu = this.chartPanel.getPopupMenu();
        popupMenu.insert(buildMenu("Distance granularity", "%.0f m", "setDistanceGranularity", yAxis.getGranularities(),
                yAxis.getCurrentGranularity()), 0);
        popupMenu.insert(buildMenu("Time granularity", "%.0f s", "setTimeGranularity", xAxis.getGranularities(), xAxis
                .getCurrentGranularity()), 1);
        reGraph();
    }

    /**
     * Update the status text.
     * @param newText String; the new text to show
     */
    public final void setStatusText(final String newText)
    {
        this.statusLabel.setText(newText);
    }

    /**
     * Create a JMenu to let the user set the granularity of the XYBlockChart.
     * @param caption String; caption for the new JMenu
     * @param format String; format string for the values in the items under the new JMenu
     * @param commandPrefix String; prefix for the actionCommand of the items under the new JMenu
     * @param values double[]; array of values to be formatted using the format strings to yield the items under the new JMenu
     * @param currentValue double; the currently selected value (used to put the bullet on the correct item)
     * @return JMenu with JRadioMenuItems for the values and a bullet on the currentValue item
     */
    private JMenu buildMenu(final String caption, final String format, final String commandPrefix, final double[] values,
            final double currentValue)
    {
        final JMenu result = new JMenu(caption);
        // Enlighten me: Do the menu items store a reference to the ButtonGroup so it won't get garbage collected?
        final ButtonGroup group = new ButtonGroup();
        for (double value : values)
        {
            final JRadioButtonMenuItem item = new JRadioButtonMenuItem(String.format(format, value));
            item.setSelected(value == currentValue);
            item.setActionCommand(commandPrefix + String.format(Locale.US, " %f", value));
            item.addActionListener(this);
            result.add(item);
            group.add(item);
        }
        return result;
    }

    /**
     * Create a XYBlockChart.
     * @param caption String; text to show above the chart
     * @param valueFormat String; format string used to render the value under the mouse in the status bar
     * @param dataset XYZDataset with the values to render
     * @param boundaries double[]; array of three boundary values corresponding to Red, Yellow and Green
     * @param legendFormat String; the format string for captions in the legend
     * @param legendStep value difference for successive colors in the legend. The first legend value displayed is equal to the
     *            lowest value in boundaries.
     * @return JFreeChart; the new XYBlockChart
     */
    private static JFreeChart createChart(final String caption, final String valueFormat, final XYZDataset dataset,
            final double[] boundaries, final String legendFormat, final double legendStep)
    {
        final NumberAxis xAxis = new NumberAxis("\u2192 " + "time [s]");
        xAxis.setLowerMargin(0.0);
        xAxis.setUpperMargin(0.0);
        final NumberAxis yAxis = new NumberAxis("\u2192 " + "Distance [m]");
        yAxis.setAutoRangeIncludesZero(false);
        yAxis.setLowerMargin(0.0);
        yAxis.setUpperMargin(0.0);
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        XYBlockRenderer renderer = new XYBlockRenderer();
        final Color[] colorValues = { Color.RED, Color.YELLOW, Color.GREEN };
        final ContinuousColorPaintScale paintScale = new ContinuousColorPaintScale(valueFormat, boundaries, colorValues);
        renderer.setPaintScale(paintScale);
        final XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        final LegendItemCollection legend = new LegendItemCollection();
        for (int i = 0;; i++)
        {
            double value = paintScale.getLowerBound() + i * legendStep;
            if (value > paintScale.getUpperBound())
            {
                break;
            }
            legend.add(new LegendItem(String.format(legendFormat, value), paintScale.getPaint(value)));
        }
        legend.add(new LegendItem("No data", Color.BLACK));
        plot.setFixedLegendItems(legend);
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        final JFreeChart chart = new JFreeChart(caption, plot);
        chart.setBackgroundPaint(Color.white);
        return chart;
    }

    /** {@inheritDoc} */
    @Override
    public final void actionPerformed(final ActionEvent actionEvent)
    {
        final String command = actionEvent.getActionCommand();
        // System.out.println("command is \"" + command + "\"");
        String[] fields = command.split("[ ]");
        if (fields.length == 2)
        {
            final NumberFormat nf = NumberFormat.getInstance(Locale.US);
            double value;
            try
            {
                value = nf.parse(fields[1]).doubleValue();
            }
            catch (ParseException e)
            {
                throw new Error("Bad value: " + fields[1]);
            }
            if (fields[0].equalsIgnoreCase("setDistanceGranularity"))
            {
                this.getYAxis().setCurrentGranularity(value);
            }
            else if (fields[0].equalsIgnoreCase("setTimeGranularity"))
            {
                this.getXAxis().setCurrentGranularity(value);
            }
            else
            {
                throw new Error("Unknown ActionEvent");
            }
            reGraph();
        }
        else
        {
            throw new Error("Unknown ActionEvent: " + command);
        }
    }

    /**
     * Redraw this ContourGraph (after the underlying data, or a granularity setting has been changed).
     */
    public final void reGraph()
    {
        notifyListeners(new DatasetChangeEvent(this, null)); // This guess work actually works!
        final XYPlot plot = this.chartPanel.getChart().getXYPlot();
        plot.notifyListeners(new PlotChangeEvent(plot));
        final XYBlockRenderer blockRenderer = (XYBlockRenderer) plot.getRenderer();
        blockRenderer.setBlockHeight(this.getYAxis().getCurrentGranularity());
        blockRenderer.setBlockWidth(this.getXAxis().getCurrentGranularity());
    }

    /**
     * Notify interested parties of an event affecting this ContourPlot.
     * @param event DatasetChangedEvent
     */
    private void notifyListeners(final DatasetChangeEvent event)
    {
        for (DatasetChangeListener dcl : this.listenerList.getListeners(DatasetChangeListener.class))
        {
            dcl.datasetChanged(event);
        }
    }

    /** List of parties interested in changes of this ContourPlot. */
    private transient EventListenerList listenerList = new EventListenerList();

    /** {@inheritDoc} */
    @Override
    public final int getSeriesCount()
    {
        return 1;
    }

    /**
     * Retrieve the number of cells to use along the distance axis.
     * @return Integer; the number of cells to use along the distance axis
     */
    protected final int yAxisBins()
    {
        return this.getYAxis().getAggregatedBinCount();
    }

    /**
     * Return the y-axis bin number (the row number) of an item. <br>
     * Do not rely on the (current) fact that the data is stored column by column!
     * @param item Integer; the item
     * @return Integer; the bin number along the y axis of the item
     */
    protected final int yAxisBin(final int item)
    {
        if (item < 0 || item >= getItemCount(0))
        {
            throw new Error("yAxisBin: item out of range (value is " + item + "), valid range is 0.." + getItemCount(0));
        }
        return item % yAxisBins();
    }

    /**
     * Return the x-axis bin number (the column number) of an item. <br>
     * Do not rely on the (current) fact that the data is stored column by column!
     * @param item Integer; the item
     * @return Integer; the bin number along the x axis of the item
     */
    protected final int xAxisBin(final int item)
    {
        if (item < 0 || item >= getItemCount(0))
        {
            throw new Error("xAxisBin: item out of range (value is " + item + "), valid range is 0.." + getItemCount(0));
        }
        return item / yAxisBins();
    }

    /**
     * Retrieve the number of cells to use along the time axis.
     * @return Integer; the number of cells to use along the time axis
     */
    protected final int xAxisBins()
    {
        return this.getXAxis().getAggregatedBinCount();
    }

    /** {@inheritDoc} */
    @Override
    public final int getItemCount(final int series)
    {
        return yAxisBins() * xAxisBins();
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
        double result = this.getXAxis().getValue(xAxisBin(item));
        // System.out.println(String.format("XValue(%d, %d) -> %.3f, binCount=%d", series, item, result,
        // this.yAxisDefinition.getAggregatedBinCount()));
        return result;
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
        return this.getYAxis().getValue(yAxisBin(item));
    }

    /** {@inheritDoc} */
    @Override
    public final Number getZ(final int series, final int item)
    {
        return getZValue(series, item);
    }

    /** {@inheritDoc} */
    @Override
    public final void addChangeListener(final DatasetChangeListener listener)
    {
        this.listenerList.add(DatasetChangeListener.class, listener);
    }

    /** {@inheritDoc} */
    @Override
    public final void removeChangeListener(final DatasetChangeListener listener)
    {
        this.listenerList.remove(DatasetChangeListener.class, listener);
    }

    /** {@inheritDoc} */
    @Override
    public final DatasetGroup getGroup()
    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void setGroup(final DatasetGroup group)
    {
        // ignore
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

    /**
     * Add a fragment of a trajectory to this ContourPlot.
     * @param car Car; the GTU that is being sampled (should be a GTU)
     */
    public final void addData(final Car<?> car) throws RemoteException
    {
        final DoubleScalar.Abs<TimeUnit> fromTime = car.getLastEvaluationTime();
        final DoubleScalar.Abs<TimeUnit> toTime = car.getNextEvaluationTime();
        if (toTime.getSI() > this.getXAxis().getMaximumValue().getSI())
        {
            extendXRange(toTime);
            this.getXAxis().adjustMaximumValue(toTime);
        }
        if (toTime.getSI() <= fromTime.getSI()) // degenerate sample???
        {
            return;
        }
        /*-
        System.out.println(String.format("addData: fromTime=%.1f, toTime=%.1f, fromDist=%.2f, toDist=%.2f", fromTime
                .getValueSI(), toTime.getValueSI(), car.position(fromTime).getValueSI(), car.position(toTime)
                .getValueSI()));
         */
        // The "relative" values are "counting" distance or time in the minimum bin size unit
        final double relativeFromDistance =
                (car.positionOfFront(fromTime).getLongitudinalPosition().getSI() - this.getYAxis().getMinimumValue().getSI())
                        / this.getYAxis().getGranularities()[0];
        final double relativeToDistance =
                (car.positionOfFront(toTime).getLongitudinalPosition().getSI() - this.getYAxis().getMinimumValue().getSI())
                        / this.getYAxis().getGranularities()[0];
        double relativeFromTime =
                (fromTime.getSI() - this.getXAxis().getMinimumValue().getSI()) / this.getXAxis().getGranularities()[0];
        final double relativeToTime =
                (toTime.getSI() - this.getXAxis().getMinimumValue().getSI()) / this.getXAxis().getGranularities()[0];
        final int fromTimeBin = (int) Math.floor(relativeFromTime);
        final int toTimeBin = (int) Math.floor(relativeToTime) + 1;
        double relativeMeanSpeed = (relativeToDistance - relativeFromDistance) / (relativeToTime - relativeFromTime);
        // The code for acceleration assumes that acceleration is constant (which is correct for IDM+, but may be
        // wrong for other car following algorithms).
        double acceleration = car.getAcceleration(car.getLastEvaluationTime()).getSI();
        for (int timeBin = fromTimeBin; timeBin < toTimeBin; timeBin++)
        {
            if (timeBin < 0)
            {
                continue;
            }
            double binEndTime = timeBin + 1;
            if (binEndTime > relativeToTime)
            {
                binEndTime = relativeToTime;
            }
            if (binEndTime <= relativeFromTime)
            {
                continue; // no time spent in this timeBin
            }
            double binDistanceStart =
                    (car.positionOfFront(
                            new DoubleScalar.Abs<TimeUnit>(relativeFromTime * this.getXAxis().getGranularities()[0],
                                    TimeUnit.SECOND)).getLongitudinalPosition().getSI() - this.getYAxis().getMinimumValue()
                            .getSI())
                            / this.getYAxis().getGranularities()[0];
            double binDistanceEnd =
                    (car.positionOfFront(
                            new DoubleScalar.Abs<TimeUnit>(binEndTime * this.getXAxis().getGranularities()[0],
                                    TimeUnit.SECOND)).getLongitudinalPosition().getSI() - this.getYAxis().getMinimumValue()
                            .getSI())
                            / this.getYAxis().getGranularities()[0];

            // Compute the time in each distanceBin
            for (int distanceBin = (int) Math.floor(binDistanceStart); distanceBin <= binDistanceEnd; distanceBin++)
            {
                double relativeDuration = 1;
                if (relativeFromTime > timeBin)
                {
                    relativeDuration -= relativeFromTime - timeBin;
                }
                if (distanceBin == (int) Math.floor(binDistanceEnd))
                {
                    // This GTU does not move out of this distanceBin before the binEndTime
                    if (binEndTime < timeBin + 1)
                    {
                        relativeDuration -= timeBin + 1 - binEndTime;
                    }
                }
                else
                {
                    // This GTU moves out of this distanceBin before the binEndTime
                    // Interpolate the time when this GTU crosses into the next distanceBin
                    // Using f.i. Newton-Rhaphson interpolation would yield a slightly more precise result...
                    double timeToBinBoundary = (distanceBin + 1 - binDistanceStart) / relativeMeanSpeed;
                    double endTime = relativeFromTime + timeToBinBoundary;
                    relativeDuration -= timeBin + 1 - endTime;
                }
                final double duration = relativeDuration * this.getXAxis().getGranularities()[0];
                final double distance = duration * relativeMeanSpeed * this.getYAxis().getGranularities()[0];
                /*-
                System.out.println(String.format("tb=%d, db=%d, t=%.2f, d=%.2f", timeBin, distanceBin, duration,
                        distance));
                 */
                incrementBinData(timeBin, distanceBin, duration, distance, acceleration);
                relativeFromTime += relativeDuration;
                binDistanceStart = distanceBin + 1;
            }
            relativeFromTime = timeBin + 1;
        }

    }

    /**
     * Increase storage for sample data. <br>
     * This is only implemented for the time axis.
     * @param newUpperLimit DoubleScalar&lt;?&gt; new upper limit for the X range
     */
    public abstract void extendXRange(DoubleScalar<?> newUpperLimit);

    /**
     * Increment the data of one bin.
     * @param timeBin Integer; the rank of the bin on the time-scale
     * @param distanceBin Integer; the rank of the bin on the distance-scale
     * @param duration Double; the time spent in this bin
     * @param distanceCovered Double; the distance covered in this bin
     * @param acceleration Double; the average acceleration in this bin
     */
    public abstract void incrementBinData(int timeBin, int distanceBin, double duration, double distanceCovered,
            double acceleration);

    /** {@inheritDoc} */
    @Override
    public final double getZValue(final int series, final int item)
    {
        final int timeBinGroup = xAxisBin(item);
        final int distanceBinGroup = yAxisBin(item);
        // System.out.println(String.format("getZValue(s=%d, i=%d) -> tbg=%d, dbg=%d", series, item, timeBinGroup,
        // distanceBinGroup));
        final int timeGroupSize = (int) (this.getXAxis().getCurrentGranularity() / this.getXAxis().getGranularities()[0]);
        final int firstTimeBin = timeBinGroup * timeGroupSize;
        final int distanceGroupSize =
                (int) (this.getYAxis().getCurrentGranularity() / this.getYAxis().getGranularities()[0]);
        final int firstDistanceBin = distanceBinGroup * distanceGroupSize;
        final int endTimeBin = Math.min(firstTimeBin + timeGroupSize, this.getXAxis().getBinCount());
        final int endDistanceBin = Math.min(firstDistanceBin + distanceGroupSize, this.getYAxis().getBinCount());
        return computeZValue(firstTimeBin, endTimeBin, firstDistanceBin, endDistanceBin);
    }

    /**
     * Combine values in a range of time bins and distance bins to obtain a combined density value of the ranges.
     * @param firstTimeBin Integer; the first time bin to use
     * @param endTimeBin Integer; one higher than the last time bin to use
     * @param firstDistanceBin Integer; the first distance bin to use
     * @param endDistanceBin Integer; one higher than the last distance bin to use
     * @return Double; the density value (or Double.NaN if no value can be computed)
     */
    public abstract double computeZValue(int firstTimeBin, int endTimeBin, int firstDistanceBin, int endDistanceBin);

    /**
     * Get the X axis.
     * @return Axis
     */
    public final Axis getXAxis()
    {
        return this.xAxis;
    }

    /**
     * Get the Y axis.
     * @return Axis
     */
    public final Axis getYAxis()
    {
        return this.yAxis;
    }

}
