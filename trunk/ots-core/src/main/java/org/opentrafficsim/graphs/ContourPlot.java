package org.opentrafficsim.graphs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingConstants;
import javax.swing.event.EventListenerList;

import nl.tudelft.simulation.dsol.simulators.DEVSSimulator;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.LegendItem;
import org.jfree.chart.LegendItemCollection;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYZDataset;
import org.opentrafficsim.car.Car;
import org.opentrafficsim.car.following.CarFollowingModel;
import org.opentrafficsim.car.following.CarFollowingModel.CarFollowingModelResult;
import org.opentrafficsim.car.following.IDMPlus;
import org.opentrafficsim.core.location.Line;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarAbs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarRel;

/**
 * Common code for a contour plot. <br />
 * The data collection code for acceleration assumes constant acceleration during the evaluation period of the GTU.
 * <p>
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved.
 * <p>
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
 * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or
 * services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability,
 * whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use
 * of this software, even if advised of the possibility of such damage.
 * @version Jul 16, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public abstract class ContourPlot extends JFrame implements MouseMotionListener, ActionListener, XYZDataset
{
    /** */
    private static final long serialVersionUID = 20140716L;

    /** The ChartPanel for this ContourPlot. */
    protected final ChartPanel chartPanel;

    /** Area to show status information. */
    protected final JLabel statusLabel;

    /** Definition of the X-axis. */
    protected final Axis xAxis;

    /** Definition of the Y-axis. */
    protected final Axis yAxis;

    /** Time granularity values. */
    protected static final double[] standardTimeGranularities = {1, 2, 5, 10, 20, 30, 60, 120, 300, 600};

    /** Distance granularity values. */
    protected static final double[] standardDistanceGranularities = {10, 20, 50, 100, 200, 500, 1000};

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
        double[] boundaries = {redValue, yellowValue, greenValue};
        this.chartPanel = new ChartPanel(createChart(caption, valueFormat, this, boundaries, legendFormat, legendStep));
        this.chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        this.chartPanel.addMouseMotionListener(this);
        this.chartPanel.setMouseWheelEnabled(true);
        this.chartPanel.addMouseMotionListener(this);
        add(this.chartPanel, BorderLayout.CENTER);
        this.statusLabel = new JLabel(" ", SwingConstants.CENTER);
        add(this.statusLabel, BorderLayout.SOUTH);
        JPopupMenu popupMenu = this.chartPanel.getPopupMenu();
        popupMenu.insert(
                buildMenu("Distance granularity", "%.0f m", "setDistanceGranularity", yAxis.granularities,
                        yAxis.getCurrentGranularity()), 0);
        popupMenu.insert(
                buildMenu("Time granularity", "%.0f s", "setTimeGranularity", xAxis.granularities,
                        xAxis.getCurrentGranularity()), 1);
        reGraph();
    }

    /**
     * Create a JMenu to let the user set the granularity of the XYBlockChart.
     * @param caption String; caption for the new JMenu
     * @param format String; format string for the values in the items under the new JMenu
     * @param commandPrefix String; prefix for the actionCommand of the items under the new JMenu
     * @param values double[]; array of values to be formatted using the format strings to yield the items under the new
     *            JMenu
     * @return
     */
    private JMenu buildMenu(final String caption, final String format, final String commandPrefix,
            final double[] values, final double currentValue)
    {
        JMenu result = new JMenu(caption);
        // Enlighten me: Do the menu items store a reference to the ButtonGroup so it won't get garbage collected?
        ButtonGroup group = new ButtonGroup();
        for (double value : values)
        {
            JRadioButtonMenuItem item = new JRadioButtonMenuItem(String.format(format, value));
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
     * @param legendStep value difference for successive colors in the legend. The first legend value displayed is equal
     *            to the lowest value in boundaries.
     * @return JFreeChart; the new XYBlockChart
     */
    private static JFreeChart createChart(final String caption, final String valueFormat, final XYZDataset dataset,
            final double[] boundaries, final String legendFormat, final double legendStep)
    {
        NumberAxis xAxis = new NumberAxis("\u2192 " + "time [s]");
        xAxis.setLowerMargin(0.0);
        xAxis.setUpperMargin(0.0);
        NumberAxis yAxis = new NumberAxis("\u2192 " + "Distance [m]");
        yAxis.setAutoRangeIncludesZero(false);
        yAxis.setLowerMargin(0.0);
        yAxis.setUpperMargin(0.0);
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        XYBlockRenderer renderer = new XYBlockRenderer();
        Color[] colorValues = {Color.RED, Color.YELLOW, Color.GREEN};
        ContinuousColorPaintScale paintScale = new ContinuousColorPaintScale(valueFormat, boundaries, colorValues);
        renderer.setPaintScale(paintScale);
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        LegendItemCollection legend = new LegendItemCollection();
        for (int i = 0;; i++)
        {
            double value = paintScale.getLowerBound() + i * legendStep;
            if (value > paintScale.getUpperBound())
                break;
            legend.add(new LegendItem(String.format(legendFormat, value), paintScale.getPaint(value)));
        }
        legend.add(new LegendItem("No data", Color.BLACK));
        plot.setFixedLegendItems(legend);
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        JFreeChart chart = new JFreeChart(caption, plot);
        chart.setBackgroundPaint(Color.white);
        return chart;
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseDragged(final MouseEvent e)
    {
        // not used
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseMoved(final MouseEvent mouseEvent)
    {
        ChartPanel cp = (ChartPanel) mouseEvent.getSource();
        XYPlot plot = (XYPlot) cp.getChart().getPlot();
        // Show a cross hair cursor while the mouse is on the graph
        boolean showCrossHair = cp.getScreenDataArea().contains(mouseEvent.getPoint());
        if (cp.getHorizontalAxisTrace() != showCrossHair)
        {
            cp.setHorizontalAxisTrace(showCrossHair);
            cp.setVerticalAxisTrace(showCrossHair);
            plot.notifyListeners(new PlotChangeEvent(plot));
        }
        if (showCrossHair)
        {
            Point2D p = cp.translateScreenToJava2D(mouseEvent.getPoint());
            PlotRenderingInfo pi = cp.getChartRenderingInfo().getPlotInfo();
            double t = plot.getDomainAxis().java2DToValue(p.getX(), pi.getDataArea(), plot.getDomainAxisEdge());
            double distance = plot.getRangeAxis().java2DToValue(p.getY(), pi.getDataArea(), plot.getRangeAxisEdge());
            XYZDataset dataset = (XYZDataset) plot.getDataset();
            String value = "";
            double roundedTime = t;
            double roundedDistance = distance;
            for (int item = dataset.getItemCount(0); --item >= 0;)
            {
                double x = dataset.getXValue(0, item);
                if (x + this.xAxis.getCurrentGranularity() / 2 < t || x - this.xAxis.getCurrentGranularity() / 2 >= t)
                    continue;
                double y = dataset.getYValue(0, item);
                if (y + this.yAxis.getCurrentGranularity() / 2 < distance
                        || y - this.yAxis.getCurrentGranularity() / 2 >= distance)
                    continue;
                roundedTime = x;
                roundedDistance = y;
                double valueUnderMouse = dataset.getZValue(0, item);
                // System.out.println("Value under mouse is " + valueUnderMouse);
                if (Double.isNaN(valueUnderMouse))
                    break;
                String format =
                        ((ContinuousColorPaintScale) (((XYBlockRenderer) (plot.getRenderer(0))).getPaintScale())).format;
                value = String.format(format, valueUnderMouse);
            }
            this.statusLabel.setText(String.format("time %.0fs, distance %.0fm, %s", roundedTime, roundedDistance,
                    value));
        }
        else
            this.statusLabel.setText(" ");
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(final ActionEvent actionEvent)
    {
        String command = actionEvent.getActionCommand();
        // System.out.println("command is \"" + command + "\"");
        String[] fields = command.split("[ ]");
        if (fields.length == 2)
        {
            NumberFormat nf = NumberFormat.getInstance(Locale.US);
            double value;
            try
            {
                value = nf.parse(fields[1]).doubleValue();
            }
            catch (ParseException e)
            {
                e.printStackTrace();
                return;
            }
            if (fields[0].equalsIgnoreCase("setDistanceGranularity"))
                this.yAxis.setCurrentGranularity(value);
            else if (fields[0].equalsIgnoreCase("setTimeGranularity"))
                this.xAxis.setCurrentGranularity(value);
            else
                throw new Error("Unknown ActionEvent");
            reGraph();
        }
        else
            throw new Error("Unknown ActionEvent");
    }

    /**
     * Redraw this ContourGraph (after the underlying data, or a granularity setting has been changed).
     */
    private void reGraph()
    {
        notifyListeners(new DatasetChangeEvent(this, null)); // This guess work actually works!
        XYPlot plot = this.chartPanel.getChart().getXYPlot();
        plot.notifyListeners(new PlotChangeEvent(plot));
        XYBlockRenderer blockRenderer = (XYBlockRenderer) plot.getRenderer();
        blockRenderer.setBlockHeight(this.yAxis.getCurrentGranularity());
        blockRenderer.setBlockWidth(this.xAxis.getCurrentGranularity());
    }

    /**
     * Notify interested parties of an event affecting this ContourPlot.
     * @param event
     */
    private void notifyListeners(final DatasetChangeEvent event)
    {
        for (DatasetChangeListener dcl : this.listenerList.getListeners(DatasetChangeListener.class))
            dcl.datasetChanged(event);
    }

    /** List of parties interested in changes of this ContourPlot. */
    transient EventListenerList listenerList = new EventListenerList();

    /**
     * @see org.jfree.data.general.SeriesDataset#getSeriesCount()
     */
    @Override
    public int getSeriesCount()
    {
        return 1;
    }

    /**
     * Retrieve the number of cells to use along the distance axis.
     * @return Integer; the number of cells to use along the distance axis
     */
    protected int yAxisBins()
    {
        return this.yAxis.getAggregatedBinCount();
    }

    /**
     * Retrieve the number of cells to use along the time axis.
     * @return Integer; the number of cells to use along the time axis
     */
    private int xAxisBins()
    {
        return this.xAxis.getAggregatedBinCount();
    }

    /**
     * @see org.jfree.data.xy.XYDataset#getItemCount(int)
     */
    @Override
    public int getItemCount(final int series)
    {
        return yAxisBins() * xAxisBins();
    }

    /**
     * @see org.jfree.data.xy.XYDataset#getX(int, int)
     */
    @Override
    public Number getX(final int series, final int item)
    {
        return new Double(getXValue(series, item));
    }

    /**
     * @see org.jfree.data.xy.XYDataset#getXValue(int, int)
     */
    @Override
    public double getXValue(final int series, final int item)
    {
        double result = this.xAxis.getValue(item / this.yAxis.getAggregatedBinCount());
        // System.out.println(String.format("XValue(%d, %d) -> %.3f, binCount=%d", series, item, result,
        // this.yAxisDefinition.getAggregatedBinCount()));
        return result;
    }

    /**
     * @see org.jfree.data.xy.XYDataset#getY(int, int)
     */
    @Override
    public Number getY(final int series, final int item)
    {
        return new Double(getYValue(series, item));
    }

    /**
     * @see org.jfree.data.xy.XYDataset#getYValue(int, int)
     */
    @Override
    public double getYValue(final int series, final int item)
    {
        return this.yAxis.getValue(item % this.yAxis.getAggregatedBinCount());
    }

    /**
     * @see org.jfree.data.xy.XYZDataset#getZ(int, int)
     */
    @Override
    public Number getZ(final int series, final int item)
    {
        return new Double(getZValue(series, item));
    }

    /**
     * @see org.jfree.data.general.Dataset#addChangeListener(org.jfree.data.general.DatasetChangeListener)
     */
    @Override
    public void addChangeListener(final DatasetChangeListener listener)
    {
        this.listenerList.add(DatasetChangeListener.class, listener);
    }

    /**
     * @see org.jfree.data.general.Dataset#removeChangeListener(org.jfree.data.general.DatasetChangeListener)
     */
    @Override
    public void removeChangeListener(final DatasetChangeListener listener)
    {
        this.listenerList.remove(DatasetChangeListener.class, listener);
    }

    /**
     * @see org.jfree.data.general.Dataset#getGroup()
     */
    @Override
    public DatasetGroup getGroup()
    {
        return null;
    }

    /**
     * @see org.jfree.data.general.Dataset#setGroup(org.jfree.data.general.DatasetGroup)
     */
    @Override
    public void setGroup(final DatasetGroup group)
    {
        // ignore
    }

    /**
     * @see org.jfree.data.general.SeriesDataset#indexOf(java.lang.Comparable)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public int indexOf(final Comparable seriesKey)
    {
        return 0;
    }

    /**
     * @see org.jfree.data.xy.XYDataset#getDomainOrder()
     */
    @Override
    public DomainOrder getDomainOrder()
    {
        return DomainOrder.ASCENDING;
    }

    /**
     * Add a fragment of a trajectory to this ContourPlot.
     * @param car Car; the GTU that is being sampled TODO: replace Car by GTU
     */
    public void addData(final Car car)
    {
        DoubleScalarAbs<TimeUnit> fromTime = car.getLastEvaluationTime();
        DoubleScalarAbs<TimeUnit> toTime = car.getNextEvaluationTime();
        if (toTime.getValueSI() > this.xAxis.getMaximumValue().getValueSI())
        {
            extendXRange(toTime);
            this.xAxis.adjustMaximumValue(toTime);
        }
        if (toTime.getValueSI() <= fromTime.getValueSI()) // degenerate sample???
            return;
        /*-
        System.out.println(String.format("addData: fromTime=%.1f, toTime=%.1f, fromDist=%.2f, toDist=%.2f", fromTime
                .getValueSI(), toTime.getValueSI(), car.position(fromTime).getValueSI(), car.position(toTime)
                .getValueSI()));
         */
        // The "relative" values are "counting" distance or time in the minimum bin size unit
        double relativeFromDistance =
                (car.position(fromTime).getValueSI() - this.yAxis.getMinimumValue().getValueSI())
                        / this.yAxis.granularities[0];
        double relativeToDistance =
                (car.position(toTime).getValueSI() - this.yAxis.getMinimumValue().getValueSI())
                        / this.yAxis.granularities[0];
        double relativeFromTime =
                (fromTime.getValueSI() - this.xAxis.getMinimumValue().getValueSI()) / this.xAxis.granularities[0];
        double relativeToTime =
                (toTime.getValueSI() - this.xAxis.getMinimumValue().getValueSI()) / this.xAxis.granularities[0];
        int fromTimeBin = (int) Math.floor(relativeFromTime);
        int toTimeBin = (int) Math.floor(relativeToTime) + 1;
        double relativeMeanSpeed = (relativeToDistance - relativeFromDistance) / (relativeToTime - relativeFromTime);
        // FIXME: The code for acceleration assumes that acceleration is constant (which is correct for IDM+, but may be
        // wrong for other car following algorithms).
        double acceleration = car.getAcceleration(car.getLastEvaluationTime()).getValueSI();
        for (int timeBin = fromTimeBin; timeBin < toTimeBin; timeBin++)
        {
            if (timeBin < 0)
                continue;
            double binEndTime = timeBin + 1;
            if (binEndTime > relativeToTime)
                binEndTime = relativeToTime;
            if (binEndTime <= relativeFromTime)
                continue; // no time spent in this timeBin
            double binDistanceStart =
                    (car.position(
                            new DoubleScalarAbs<TimeUnit>(relativeFromTime * this.xAxis.granularities[0],
                                    TimeUnit.SECOND)).getValueSI() - this.yAxis.getMinimumValue().getValueSI())
                            / this.yAxis.granularities[0];
            double binDistanceEnd =
                    (car.position(
                            new DoubleScalarAbs<TimeUnit>(binEndTime * this.xAxis.granularities[0], TimeUnit.SECOND))
                            .getValueSI() - this.yAxis.getMinimumValue().getValueSI())
                            / this.yAxis.granularities[0];

            // Compute the time in each distanceBin
            for (int distanceBin = (int) Math.floor(binDistanceStart); distanceBin <= binDistanceEnd; distanceBin++)
            {
                double relativeDuration = 1;
                if (relativeFromTime > timeBin)
                    relativeDuration -= relativeFromTime - timeBin;
                if (distanceBin == (int) Math.floor(binDistanceEnd))
                {
                    // This GTU does not move out of this distanceBin before the binEndTime
                    if (binEndTime < timeBin + 1)
                        relativeDuration -= timeBin + 1 - binEndTime;
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
                final double duration = relativeDuration * this.xAxis.granularities[0];
                final double distance = duration * relativeMeanSpeed * this.yAxis.granularities[0];
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
     * Increase storage for sample data. <br />
     * This is only implemented for the time axis.
     * @param newUpperLimit DoubleScalar<?> new upper limit for the X range
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

    /**
     * @see org.jfree.data.xy.XYZDataset#getZValue(int, int)
     */
    @Override
    public double getZValue(final int series, final int item)
    {
        int timeBinGroup = item / yAxisBins();
        int distanceBinGroup = item % yAxisBins();
        // System.out.println(String.format("getZValue(s=%d, i=%d) -> tbg=%d, dbg=%d", series, item, timeBinGroup,
        // distanceBinGroup));
        final int timeGroupSize = (int) (this.xAxis.getCurrentGranularity() / this.xAxis.granularities[0]);
        final int firstTimeBin = timeBinGroup * timeGroupSize;
        if (firstTimeBin * this.xAxis.granularities[0] >= this.xAxis.getMaximumValue().getValueSI())
            return Double.NaN;
        final int distanceGroupSize = (int) (this.yAxis.getCurrentGranularity() / this.yAxis.granularities[0]);
        final int firstDistanceBin = distanceBinGroup * distanceGroupSize;
        if (firstDistanceBin * this.yAxis.granularities[0] >= this.yAxis.getMaximumValue().getValueSI())
            return Double.NaN;
        return computeZValue(firstTimeBin, firstTimeBin + timeGroupSize, firstDistanceBin, firstDistanceBin
                + distanceGroupSize);
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
     * Main for stand alone running.
     * @param args String[]; the program arguments (not used)
     */
    public static void main(final String[] args)
    {
        JOptionPane.showMessageDialog(null, "ContourPlot", "Start experiment", JOptionPane.INFORMATION_MESSAGE);
        ArrayList<ContourPlot> contourPlots = new ArrayList<ContourPlot>();
        DoubleScalarAbs<LengthUnit> minimumDistance = new DoubleScalarAbs<LengthUnit>(0, LengthUnit.METER);
        DoubleScalarAbs<LengthUnit> maximumDistance = new DoubleScalarAbs<LengthUnit>(5000, LengthUnit.METER);
        ContourPlot cp;
        int left = 200;
        int deltaLeft = 100;
        int top = 100;
        int deltaTop = 50;

        cp = new DensityContourPlot("DensityPlot", minimumDistance, maximumDistance);
        cp.setTitle("Density Contour Graph");
        cp.setBounds(left + contourPlots.size() * deltaLeft, top + contourPlots.size() * deltaTop, 600, 400);
        cp.pack();
        cp.setVisible(true);
        contourPlots.add(cp);

        cp = new SpeedContourPlot("SpeedPlot", minimumDistance, maximumDistance);
        cp.setTitle("Speed Contour Graph");
        cp.setBounds(left + contourPlots.size() * deltaLeft, top + contourPlots.size() * deltaTop, 600, 400);
        cp.pack();
        cp.setVisible(true);
        contourPlots.add(cp);

        cp = new FlowContourPlot("FlowPlot", minimumDistance, maximumDistance);
        cp.setTitle("FLow Contour Graph");
        cp.setBounds(left + contourPlots.size() * deltaLeft, top + contourPlots.size() * deltaTop, 600, 400);
        cp.pack();
        cp.setVisible(true);
        contourPlots.add(cp);

        cp = new AccelerationContourPlot("AccelerationPlot", minimumDistance, maximumDistance);
        cp.setTitle("Acceleration Contour Graph");
        cp.setBounds(left + contourPlots.size() * deltaLeft, top + contourPlots.size() * deltaTop, 600, 400);
        cp.pack();
        cp.setVisible(true);
        contourPlots.add(cp);

        DEVSSimulator simulator = new DEVSSimulator();
        CarFollowingModel carFollowingModel = new IDMPlus<Line<String>>();
        DoubleScalarAbs<LengthUnit> initialPosition = new DoubleScalarAbs<LengthUnit>(0, LengthUnit.METER);
        DoubleScalarRel<SpeedUnit> initialSpeed = new DoubleScalarRel<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);
        DoubleScalarAbs<SpeedUnit> speedLimit = new DoubleScalarAbs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);
        final double endTime = 1800; // [s]
        final double headway = 3600.0 / 1500.0; // 1500 [veh / hour] == 2.4s headway
        double thisTick = 0;
        final double tick = 0.5;
        int carsCreated = 0;
        ArrayList<Car> cars = new ArrayList<Car>();
        double nextSourceTick = 0;
        double nextMoveTick = 0;
        while (thisTick < endTime)
        {
            // System.out.println("thisTick is " + thisTick);
            if (thisTick == nextSourceTick)
            {
                // Time to generate another car
                DoubleScalarAbs<TimeUnit> initialTime = new DoubleScalarAbs<TimeUnit>(thisTick, TimeUnit.SECOND);
                Car car =
                        new Car(++carsCreated, simulator, carFollowingModel, initialTime, initialPosition, initialSpeed);
                cars.add(0, car);
                // System.out.println(String.format("thisTick=%.1f, there are now %d vehicles", thisTick, cars.size()));
                nextSourceTick += headway;
            }
            if (thisTick == nextMoveTick)
            {
                // Time to move all vehicles forward (this works even though they do not have simultaneous clock ticks)
                /*
                 * Debugging if (thisTick == 700) { DoubleScalarAbs<TimeUnit> now = new
                 * DoubleScalarAbs<TimeUnit>(thisTick, TimeUnit.SECOND); for (int i = 0; i < cars.size(); i++)
                 * System.out.println(cars.get(i).toString(now)); }
                 */
                /*
                 * TODO: Currently all cars have to be moved "manually". This functionality should go to the simulator.
                 */
                for (int carIndex = 0; carIndex < cars.size(); carIndex++)
                {
                    DoubleScalarAbs<TimeUnit> now = new DoubleScalarAbs<TimeUnit>(thisTick, TimeUnit.SECOND);
                    Car car = cars.get(carIndex);
                    if (car.position(now).getValueSI() > 5000)
                    {
                        cars.remove(carIndex);
                        break;
                    }
                    Collection<Car> leaders = new ArrayList<Car>();
                    if (carIndex < cars.size() - 1)
                        leaders.add(cars.get(carIndex + 1));
                    if (thisTick >= 300 && thisTick < 500)
                    {
                        // Add a stationary car at 4000m to simulate an opening bridge
                        Car block =
                                new Car(99999, simulator, carFollowingModel, now, new DoubleScalarAbs<LengthUnit>(4000,
                                        LengthUnit.METER), new DoubleScalarRel<SpeedUnit>(0, SpeedUnit.KM_PER_HOUR));
                        leaders.add(block);
                    }
                    CarFollowingModelResult cfmr = carFollowingModel.computeAcceleration(car, leaders, speedLimit);
                    car.setState(cfmr);
                    // Add the movement of this Car to the contour plots
                    for (ContourPlot contourPlot : contourPlots)
                        contourPlot.addData(car);
                }
                nextMoveTick += tick;
            }
            thisTick = Math.min(nextSourceTick, nextMoveTick);
        }
        // Notify the contour plots that the underlying data has changed
        for (ContourPlot contourPlot : contourPlots)
            contourPlot.reGraph();
    }

}
