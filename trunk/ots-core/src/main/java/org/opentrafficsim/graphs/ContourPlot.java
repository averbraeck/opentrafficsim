package org.opentrafficsim.graphs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingConstants;
import javax.swing.event.EventListenerList;

import org.djunits.unit.LengthUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.value.ValueException;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.vector.DoubleVector;
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
import org.opentrafficsim.core.OTS_SCALAR;
import org.opentrafficsim.core.gtu.lane.AbstractLaneBasedGTU;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.lane.Lane;

/**
 * Common code for a contour plot. <br>
 * The data collection code for acceleration assumes constant acceleration during the evaluation period of the GTU.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jul 16, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class ContourPlot extends JFrame implements ActionListener, XYZDataset, MultipleViewerChart,
    LaneBasedGTUSampler, OTS_SCALAR
{
    /** */
    private static final long serialVersionUID = 20140716L;

    /** Caption of the graph. */
    private final String caption;

    /** Color scale for the graph. */
    private final ContinuousColorPaintScale paintScale;

    /** Definition of the X-axis. */
    @SuppressWarnings("visibilitymodifier")
    protected final Axis xAxis;

    /** Definition of the Y-axis. */
    @SuppressWarnings("visibilitymodifier")
    protected final Axis yAxis;

    /** Difference of successive values in the legend. */
    private final double legendStep;

    /** Format string used to create the captions in the legend. */
    private final String legendFormat;

    /** Time granularity values. */
    protected static final double[] STANDARDTIMEGRANULARITIES = {1, 2, 5, 10, 20, 30, 60, 120, 300, 600};

    /** Index of the initial time granularity in standardTimeGranularites. */
    protected static final int STANDARDINITIALTIMEGRANULARITYINDEX = 3;

    /** Distance granularity values. */
    protected static final double[] STANDARDDISTANCEGRANULARITIES = {10, 20, 50, 100, 200, 500, 1000};

    /** Index of the initial distance granularity in standardTimeGranularites. */
    protected static final int STANDARDINITIALDISTANCEGRANULARITYINDEX = 3;

    /** Initial lower bound for the time scale. */
    protected static final Time.Abs INITIALLOWERTIMEBOUND = new Time.Abs(0,
        TimeUnit.SECOND);

    /** Initial upper bound for the time scale. */
    protected static final Time.Abs INITIALUPPERTIMEBOUND = new Time.Abs(300,
        TimeUnit.SECOND);

    /** The series of Lanes that provide the data for this TrajectoryPlot. */
    private final ArrayList<Lane> path;

    /** The cumulative lengths of the elements of path. */
    private final DoubleVector.Rel.Dense<LengthUnit> cumulativeLengths;

    /**
     * Create a new ContourPlot.
     * @param caption String; text to show above the plotting area
     * @param xAxis Axis; the X (time) axis
     * @param path ArrayList&lt;Lane&gt;; the series of Lanes that will provide the data for this TrajectoryPlot
     * @param redValue Double; contour value that will be rendered in Red
     * @param yellowValue Double; contour value that will be rendered in Yellow
     * @param greenValue Double; contour value that will be rendered in Green
     * @param valueFormat String; format string for the contour values
     * @param legendFormat String; format string for the captions in the color legend
     * @param legendStep Double; increment between color legend entries
     */
    public ContourPlot(final String caption, final Axis xAxis, final List<Lane> path, final double redValue,
        final double yellowValue, final double greenValue, final String valueFormat, final String legendFormat,
        final double legendStep)
    {
        this.caption = caption;
        this.path = new ArrayList<Lane>(path); // make a copy
        double[] endLengths = new double[path.size()];
        double cumulativeLength = 0;
        DoubleVector.Rel.Dense<LengthUnit> lengths = null;
        for (int i = 0; i < path.size(); i++)
        {
            Lane lane = path.get(i);
            lane.addSampler(this);
            cumulativeLength += lane.getLength().getSI();
            endLengths[i] = cumulativeLength;
        }
        try
        {
            lengths = new DoubleVector.Rel.Dense<LengthUnit>(endLengths, LengthUnit.SI);
        }
        catch (ValueException exception)
        {
            exception.printStackTrace();
        }
        this.cumulativeLengths = lengths;
        this.xAxis = xAxis;
        this.yAxis =
            new Axis(new Length.Rel(0, LengthUnit.METER), getCumulativeLength(-1),
                STANDARDDISTANCEGRANULARITIES, STANDARDDISTANCEGRANULARITIES[STANDARDINITIALDISTANCEGRANULARITYINDEX], "",
                "Distance", "%.0fm");
        this.legendStep = legendStep;
        this.legendFormat = legendFormat;
        extendXRange(xAxis.getMaximumValue());
        double[] boundaries = {redValue, yellowValue, greenValue};
        final Color[] colorValues = {Color.RED, Color.YELLOW, Color.GREEN};
        this.paintScale = new ContinuousColorPaintScale(valueFormat, boundaries, colorValues);
        createChart(this);
        reGraph();
    }

    /**
     * Retrieve the cumulative length of the sampled path at the end of a path element.
     * @param index int; the index of the path element; if -1, the total length of the path is returned
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; the cumulative length at the end of the specified path element
     */
    public final Length.Rel getCumulativeLength(final int index)
    {
        int useIndex = -1 == index ? this.cumulativeLengths.size() - 1 : index;
        try
        {
            return new Length.Rel(this.cumulativeLengths.get(useIndex));
        }
        catch (ValueException exception)
        {
            exception.printStackTrace();
        }
        return null; // NOTREACHED
    }

    /**
     * Create a JMenu to let the user set the granularity of the XYBlockChart.
     * @param menuName String; caption for the new JMenu
     * @param format String; format string for the values in the items under the new JMenu
     * @param commandPrefix String; prefix for the actionCommand of the items under the new JMenu
     * @param values double[]; array of values to be formatted using the format strings to yield the items under the new JMenu
     * @param currentValue double; the currently selected value (used to put the bullet on the correct item)
     * @return JMenu with JRadioMenuItems for the values and a bullet on the currentValue item
     */
    private JMenu buildMenu(final String menuName, final String format, final String commandPrefix, final double[] values,
        final double currentValue)
    {
        final JMenu result = new JMenu(menuName);
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
     * @param container JFrame; the JFrame that will be populated with the chart and the status label
     * @return JFreeChart; the new XYBlockChart
     */
    private JFreeChart createChart(final JFrame container)
    {
        final JLabel statusLabel = new JLabel(" ", SwingConstants.CENTER);
        container.add(statusLabel, BorderLayout.SOUTH);
        final NumberAxis xAxis1 = new NumberAxis("\u2192 " + "time [s]");
        xAxis1.setLowerMargin(0.0);
        xAxis1.setUpperMargin(0.0);
        final NumberAxis yAxis1 = new NumberAxis("\u2192 " + "Distance [m]");
        yAxis1.setAutoRangeIncludesZero(false);
        yAxis1.setLowerMargin(0.0);
        yAxis1.setUpperMargin(0.0);
        yAxis1.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        XYBlockRenderer renderer = new XYBlockRenderer();
        renderer.setPaintScale(this.paintScale);
        final XYPlot plot = new XYPlot(this, xAxis1, yAxis1, renderer);
        final LegendItemCollection legend = new LegendItemCollection();
        for (int i = 0;; i++)
        {
            double value = this.paintScale.getLowerBound() + i * this.legendStep;
            if (value > this.paintScale.getUpperBound())
            {
                break;
            }
            legend.add(new LegendItem(String.format(this.legendFormat, value), this.paintScale.getPaint(value)));
        }
        legend.add(new LegendItem("No data", Color.BLACK));
        plot.setFixedLegendItems(legend);
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        final JFreeChart chart = new JFreeChart(this.caption, plot);
        FixCaption.fixCaption(chart);
        chart.setBackgroundPaint(Color.white);
        final ChartPanel cp = new ChartPanel(chart);
        final PointerHandler ph = new PointerHandler()
        {
            /** {@inheritDoc} */
            @Override
            void updateHint(final double domainValue, final double rangeValue)
            {
                if (Double.isNaN(domainValue))
                {
                    statusLabel.setText(" ");
                    return;
                }
                // XYPlot plot = (XYPlot) getChartPanel().getChart().getPlot();
                XYZDataset dataset = (XYZDataset) plot.getDataset();
                String value = "";
                double roundedTime = domainValue;
                double roundedDistance = rangeValue;
                for (int item = dataset.getItemCount(0); --item >= 0;)
                {
                    double x = dataset.getXValue(0, item);
                    if (x + ContourPlot.this.xAxis.getCurrentGranularity() / 2 < domainValue
                        || x - ContourPlot.this.xAxis.getCurrentGranularity() / 2 >= domainValue)
                    {
                        continue;
                    }
                    double y = dataset.getYValue(0, item);
                    if (y + ContourPlot.this.yAxis.getCurrentGranularity() / 2 < rangeValue
                        || y - ContourPlot.this.yAxis.getCurrentGranularity() / 2 >= rangeValue)
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
                statusLabel.setText(String.format("time %.0fs, distance %.0fm, %s", roundedTime, roundedDistance, value));
            }

        };
        cp.addMouseMotionListener(ph);
        cp.addMouseListener(ph);
        container.add(cp, BorderLayout.CENTER);
        cp.setMouseWheelEnabled(true);
        JPopupMenu popupMenu = cp.getPopupMenu();
        popupMenu.add(new JPopupMenu.Separator());
        popupMenu.add(StandAloneChartWindow.createMenuItem(this));
        popupMenu.insert(buildMenu("Distance granularity", "%.0f m", "setDistanceGranularity",
            this.yAxis.getGranularities(), this.yAxis.getCurrentGranularity()), 0);
        popupMenu.insert(buildMenu("Time granularity", "%.0f s", "setTimeGranularity", this.xAxis.getGranularities(),
            this.xAxis.getCurrentGranularity()), 1);
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
                clearCachedValues();
            }
            else if (fields[0].equalsIgnoreCase("setTimeGranularity"))
            {
                this.getXAxis().setCurrentGranularity(value);
                clearCachedValues();
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
        for (DatasetChangeListener dcl : this.listenerList.getListeners(DatasetChangeListener.class))
        {
            if (dcl instanceof XYPlot)
            {
                final XYPlot plot = (XYPlot) dcl;
                plot.notifyListeners(new PlotChangeEvent(plot));
                final XYBlockRenderer blockRenderer = (XYBlockRenderer) plot.getRenderer();
                blockRenderer.setBlockHeight(this.getYAxis().getCurrentGranularity());
                blockRenderer.setBlockWidth(this.getXAxis().getCurrentGranularity());
                // configureAxis(((XYPlot) dcl).getDomainAxis(), this.maximumTime.getSI());
            }
        }
        notifyListeners(new DatasetChangeEvent(this, null)); // This guess work actually works!
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

    /** Cached result of yAxisBins. */
    private int cachedYAxisBins = -1;

    /**
     * Retrieve the number of cells to use along the distance axis.
     * @return Integer; the number of cells to use along the distance axis
     */
    protected final int yAxisBins()
    {
        if (this.cachedYAxisBins >= 0)
        {
            return this.cachedYAxisBins;
        }
        this.cachedYAxisBins = this.getYAxis().getAggregatedBinCount();
        return this.cachedYAxisBins;
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

    /** Cached result of xAxisBins. */
    private int cachedXAxisBins = -1;

    /**
     * Retrieve the number of cells to use along the time axis.
     * @return Integer; the number of cells to use along the time axis
     */
    protected final int xAxisBins()
    {
        if (this.cachedXAxisBins >= 0)
        {
            return this.cachedXAxisBins;
        }
        this.cachedXAxisBins = this.getXAxis().getAggregatedBinCount();
        return this.cachedXAxisBins;
    }

    /** Cached result of getItemCount. */
    private int cachedItemCount = -1;

    /** {@inheritDoc} */
    @Override
    public final int getItemCount(final int series)
    {
        if (this.cachedItemCount >= 0)
        {
            return this.cachedItemCount;
        }
        this.cachedItemCount = yAxisBins() * xAxisBins();
        return this.cachedItemCount;
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
     * Make sure that the results of the most called methods are re-calculated.
     */
    private void clearCachedValues()
    {
        this.cachedItemCount = -1;
        this.cachedXAxisBins = -1;
        this.cachedYAxisBins = -1;
    }

    /** {@inheritDoc} */
    @Override
    public final void addData(final AbstractLaneBasedGTU car, final Lane lane) throws RemoteException, NetworkException
    {
        // System.out.println("addData car: " + car + ", lastEval: " + startTime);
        // Convert the position of the car to a position on path.
        double lengthOffset = 0;
        int index = this.path.indexOf(lane);
        if (index >= 0)
        {
            if (index > 0)
            {
                try
                {
                    lengthOffset = this.cumulativeLengths.getSI(index - 1);
                }
                catch (ValueException exception)
                {
                    exception.printStackTrace();
                }
            }
        }
        else
        {
            throw new Error("Cannot happen: Lane is not in the path");
        }
        final Time.Abs fromTime = car.getLastEvaluationTime();
        if (car.position(lane, car.getReference(), fromTime).getSI() < 0 && lengthOffset > 0)
        {
            return;
        }
        final Time.Abs toTime = car.getNextEvaluationTime();
        if (toTime.getSI() > this.getXAxis().getMaximumValue().getSI())
        {
            extendXRange(toTime);
            clearCachedValues();
            this.getXAxis().adjustMaximumValue(toTime);
        }
        if (toTime.le(fromTime)) // degenerate sample???
        {
            return;
        }
        /*-
        System.out.println(String.format("addData: fromTime=%.1f, toTime=%.1f, fromDist=%.2f, toDist=%.2f", fromTime
                .getValueSI(), toTime.getValueSI(), car.position(fromTime).getValueSI() + lengthOffset, 
                car.position(toTime).getValueSI() + lengthOffset));
         */
        // The "relative" values are "counting" distance or time in the minimum bin size unit
        final double relativeFromDistance =
            (car.position(lane, car.getReference(), fromTime).getSI() + lengthOffset)
                / this.getYAxis().getGranularities()[0];
        final double relativeToDistance =
            (car.position(lane, car.getReference(), toTime).getSI() + lengthOffset) / this.getYAxis().getGranularities()[0];
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
                (car.position(
                    lane,
                    car.getReference(),
                    new Time.Abs(relativeFromTime * this.getXAxis().getGranularities()[0], TimeUnit.SECOND))
                    .getSI()
                    - this.getYAxis().getMinimumValue().getSI() + lengthOffset)
                    / this.getYAxis().getGranularities()[0];
            double binDistanceEnd =
                (car.position(lane, car.getReference(),
                    new Time.Abs(binEndTime * this.getXAxis().getGranularities()[0], TimeUnit.SECOND))
                    .getSI()
                    - this.getYAxis().getMinimumValue().getSI() + lengthOffset)
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

    /** {@inheritDoc} */
    @Override
    public final JFrame addViewer()
    {
        JFrame result = new JFrame(this.caption);
        JFreeChart newChart = createChart(result);
        newChart.setTitle((String) null);
        addChangeListener(newChart.getPlot());
        reGraph();
        return result;
    }

}
