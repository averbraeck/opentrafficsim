package org.opentrafficsim.graphs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Paint;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.event.EventListenerList;

import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.plot.PlotRenderingInfo;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.PaintScale;
import org.jfree.chart.renderer.xy.XYBlockRenderer;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.DefaultXYZDataset;
import org.jfree.data.xy.XYDataset;
import org.jfree.data.xy.XYZDataset;
import org.jfree.ui.RefineryUtilities;
import org.opentrafficsim.car.Car;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.ValueException;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarAbs;
import org.opentrafficsim.core.value.vdouble.vector.DoubleVectorAbs;
import org.opentrafficsim.core.value.vdouble.vector.DoubleVectorAbsDense;

/**
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
public class ContourPlot extends JFrame implements MouseMotionListener, ActionListener
{
    /**
     * Enumeration of the possible contour graphs.
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
     * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the
     * following disclaimer.</li>
     * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
     * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
     * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse
     * or promote products derived from this software without specific prior written permission.</li>
     * </ul>
     * This software is provided by the copyright holders and contributors "as is" and any express or implied
     * warranties, including, but not limited to, the implied warranties of merchantability and fitness for a particular
     * purpose are disclaimed. In no event shall the copyright holder or contributors be liable for any direct,
     * indirect, incidental, special, exemplary, or consequential damages (including, but not limited to, procurement of
     * substitute goods or services; loss of use, data, or profits; or business interruption) however caused and on any
     * theory of liability, whether in contract, strict liability, or tort (including negligence or otherwise) arising
     * in any way out of the use of this software, even if advised of the possibility of such damage.
     * @version Jul 16, 2014 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    public enum Type {
        /** Density contour graph */
        DENSITY,
        /** Speed contour graph */
        SPEED,
        /** Flow contour graph */
        FLOW,
    }

    /** */
    private static final long serialVersionUID = 20140716L;

    /** The ChartPanel for this ContourPlot */
    protected final ChartPanel chartPanel;

    /** Area to show status information */
    protected final JLabel statusLabel;

    /** Time granularity in seconds */
    protected double timeGranularity = timeGranularities[0];

    /** Distance granularity in meters */
    protected double distanceGranularity = distanceGranularities[0];

    /** List of parties interested in changes of this ContourPlot */
    transient EventListenerList listenerList = new EventListenerList();

    /** Minimum distance used in this ContourPlot */
    protected final double minimumDistance;

    /** Maximum distance used in this ContourPlot */
    protected final double maximumDistance;

    /** Time range of this ContourPlot (automatically extended when data is added) */
    protected double timeRange = 300;

    /** Granularity values for the distancePopupMenu */
    protected final static double[] distanceGranularities = {10, 20, 50, 100};

    /** Granularity values for the timePopupMenu */
    protected final static double[] timeGranularities = {1, 2, 5, 10, 20, 30, 60, 120, 300, 600};

    /** Number of distance bins used */
    protected final int distanceBinCount;

    /**
     * Create a new ContourPlot
     * @param caption String; text to show above the contour plot
     * @param type Contourplot.Type; the type of this contour plot
     * @param minimumDistance double; the minimum distance value used in this contour plot
     * @param maximumDistance double; the maximum distance value used in this contour plot
     */
    public ContourPlot(String caption, Type type, final double minimumDistance, final double maximumDistance)
    {
        this.minimumDistance = distanceGranularities[0] * Math.floor(minimumDistance / distanceGranularities[0]);
        this.maximumDistance = distanceGranularities[0] * Math.ceil(maximumDistance / distanceGranularities[0]);
        this.distanceBinCount = (int) ((maximumDistance - minimumDistance) / distanceGranularities[0]);
        this.setLayout(new BorderLayout());
        String valueFormat;
        double[] boundaries = new double[3];
        ContourDataSet contourDataSet = null;

        switch (type)
        {
            case DENSITY:
                valueFormat = "density %.1f veh/km";
                boundaries[0] = 0;
                boundaries[1] = 30;
                boundaries[2] = 100;
                contourDataSet = new DensityContourDataSet();
                break;
            case FLOW:
                valueFormat = "flow %.0f veh/hour";
                boundaries[0] = 0;
                boundaries[1] = 1000;
                boundaries[2] = 3000;
                break;
            case SPEED:
                valueFormat = "speed %.1f km/h";
                boundaries[0] = 0;
                boundaries[1] = 40;
                boundaries[2] = 150;
                break;
            default:
                throw new Error("Bad switch; Cannot happen");
        }
        this.chartPanel =
                new ChartPanel(createChart(caption, "\u2192 Distance", ", " + valueFormat, contourDataSet, boundaries));
        this.chartPanel.setPreferredSize(new java.awt.Dimension(500, 270));
        this.chartPanel.addMouseMotionListener(this);
        this.add(this.chartPanel, BorderLayout.CENTER);
        this.statusLabel = new JLabel(" ");
        this.add(this.statusLabel, BorderLayout.SOUTH);
        JPopupMenu popupMenu = this.chartPanel.getPopupMenu();
        popupMenu.insert(buildMenu("Distance granularity", "%.0f m", "setDistanceGranularity", distanceGranularities),
                0);
        popupMenu.insert(buildMenu("Time granularity", "%.0f s", "setTimeGranularity", timeGranularities), 1);
    }

    /** The sub-menu that sets the distance granularity */
    protected JMenu distanceGranularityMenu;

    /** The sub-menu that sets the time granularity */
    protected JMenu timeGranularityMenu;

    /**
     * Create a JMenu to let the user set the granularity of the XYBlockChart.
     * @param caption String; caption for the new JMenu
     * @param format String; format string for the values in the items under the new JMenu
     * @param commandPrefix String; prefix for the actionCommand of the items under the new JMenu
     * @param values double[]; array of values to be formatted using the format strings to yield the items under the new
     *            JMenu
     * @return
     */
    private JMenu buildMenu(String caption, String format, String commandPrefix, double[] values)
    {
        JMenu result = new JMenu(caption);
        for (double value : values)
        {
            JMenuItem item = new JMenuItem(String.format(format, value));
            item.setActionCommand(commandPrefix + String.format(Locale.US, " %f", value));
            item.addActionListener(this);
            result.add(item);
        }
        return result;
    }

    /**
     * Storage for the contour data of this ContourPlot
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
     * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the
     * following disclaimer.</li>
     * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
     * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
     * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse
     * or promote products derived from this software without specific prior written permission.</li>
     * </ul>
     * This software is provided by the copyright holders and contributors "as is" and any express or implied
     * warranties, including, but not limited to, the implied warranties of merchantability and fitness for a particular
     * purpose are disclaimed. In no event shall the copyright holder or contributors be liable for any direct,
     * indirect, incidental, special, exemplary, or consequential damages (including, but not limited to, procurement of
     * substitute goods or services; loss of use, data, or profits; or business interruption) however caused and on any
     * theory of liability, whether in contract, strict liability, or tort (including negligence or otherwise) arising
     * in any way out of the use of this software, even if advised of the possibility of such damage.
     * @version Jul 16, 2014 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    abstract class ContourDataSet implements XYZDataset
    {
        // Implements everything except getZValue and getSeriesKey.
        @Override
        public int getSeriesCount()
        {
            return 1;
        }

        /**
         * Retrieve the number of cells to use along the distance axis.
         * @return Integer; the number of cells to use along the distance axis
         */
        private int distances()
        {
            return (int) Math.ceil((ContourPlot.this.maximumDistance - ContourPlot.this.minimumDistance)
                    / ContourPlot.this.distanceGranularity);
        }

        /**
         * Retrieve the number of cells to use along the time axis.
         * @return Integer; the number of cells to use along the time axis
         */
        private int times()
        {
            return (int) Math.ceil(ContourPlot.this.timeRange / ContourPlot.this.timeGranularity);
        }

        @Override
        public int getItemCount(int series)
        {
            return distances() * times();
        }

        @Override
        public Number getX(int series, int item)
        {
            return new Double(getXValue(series, item));
        }

        @Override
        public double getXValue(int series, int item)
        {
            return item / distances() * ContourPlot.this.timeGranularity;
        }

        @Override
        public Number getY(int series, int item)
        {
            return new Double(getYValue(series, item));
        }

        @Override
        public double getYValue(int series, int item)
        {
            return item % distances() * ContourPlot.this.distanceGranularity;
        }

        @Override
        public Number getZ(int series, int item)
        {
            return new Double(getZValue(series, item));
        }

        @Override
        public void addChangeListener(DatasetChangeListener listener)
        {
            ContourPlot.this.listenerList.add(DatasetChangeListener.class, listener);
        }

        @Override
        public void removeChangeListener(DatasetChangeListener listener)
        {
            ContourPlot.this.listenerList.remove(DatasetChangeListener.class, listener);
        }

        @Override
        public DatasetGroup getGroup()
        {
            return null;
        }

        @Override
        public void setGroup(DatasetGroup group)
        {
            // ignore
        }

        @SuppressWarnings("rawtypes")
        @Override
        public int indexOf(Comparable seriesKey)
        {
            return 0;
        }

        @Override
        public DomainOrder getDomainOrder()
        {
            return DomainOrder.ASCENDING;
        }

        /**
         * Add a sub-trajectory to the ContourPlot.
         * @param fromTime Double; start time of the sub-trajectory
         * @param toTime Double; end time of the sub-trajectory
         * @param car Car; the GTU that is being sampled TODO: replace Car by GTU
         */
        public void addData(DoubleScalarAbs<TimeUnit> fromTime, DoubleScalarAbs<TimeUnit> toTime, Car car)
        {
            if (toTime.getValueSI() <= fromTime.getValueSI()) // degenerate sample???
                return;
            double relativeFromDistance =
                    (car.position(fromTime).getValueSI() - ContourPlot.this.minimumDistance) / distanceGranularities[0];
            double relativeToDistance =
                    (car.position(toTime).getValueSI() - ContourPlot.this.minimumDistance) / distanceGranularities[0];
            double relativeFromTime = (fromTime.getValueSI() - 0) / timeGranularities[0];
            double relativeToTime = (toTime.getValueSI() - 0) / timeGranularities[0];
            int fromTimeBin = (int) Math.floor(relativeFromTime);
            int toTimeBin = (int) Math.ceil(relativeToTime);
            double relativeMeanSpeed =
                    (relativeToDistance - relativeFromDistance) / (relativeToTime - relativeFromTime);
            for (int timeBin = fromTimeBin; timeBin < toTimeBin; timeBin++)
            {
                if (timeBin < 0)
                    continue;
                double binEndTime = fromTimeBin + 1;
                if (binEndTime > relativeToTime)
                    binEndTime = relativeToTime;
                if (binEndTime <= relativeFromTime)
                    continue; // no time spent in this timeBin
                double binDistanceStart =
                        (car.position(
                                new DoubleScalarAbs<TimeUnit>(relativeFromTime * timeGranularities[0], TimeUnit.SECOND))
                                .getValueSI() - ContourPlot.this.minimumDistance)
                                / distanceGranularities[0];
                double binDistanceEnd =
                        (car.position(new DoubleScalarAbs<TimeUnit>(binEndTime * timeGranularities[0], TimeUnit.SECOND))
                                .getValueSI() - ContourPlot.this.minimumDistance)
                                / distanceGranularities[0];

                // Compute the time in each distanceBin (assuming constant speed during the timeBin)
                for (int distanceBin = (int) Math.floor(binDistanceStart); distanceBin <= binDistanceEnd; distanceBin++)
                {
                    double relativeDuration = 1;
                    if (relativeFromTime > fromTimeBin)
                        relativeDuration -= relativeFromTime - fromTimeBin;
                    if (distanceBin == (int) Math.floor(binDistanceEnd))
                    {
                        // This GTU does not move out of this distanceBin before the binEndTime
                        if (binEndTime < timeBin + 1)
                            relativeDuration -= timeBin + 1 - binEndTime;
                    }
                    else
                    {
                        // This GTU moves out of this distanceBin before the binEndTime
                        // Interpolate the time that this GTU crosses into the next distanceBin
                        double timeToBinBoundary = (distanceBin + 1 - binDistanceStart) / relativeMeanSpeed;
                        double endTime = relativeFromTime + timeToBinBoundary;
                        relativeDuration -= timeBin + 1 - endTime;
                    }
                    incrementData(timeBin, distanceBin, relativeDuration * timeGranularities[0], relativeDuration
                            * relativeMeanSpeed * distanceGranularities[0]);
                }
                relativeFromTime = timeBin + 1;
            }

        }

        /**
         * Increment the data of one bin
         * @param timeBin Integer; the rank of the bin on the time-scale
         * @param distanceBin Integer; the rank of the bin on the distance-scale
         * @param duration Double; the time spent in this bin
         * @param distanceCovered Double; the distance covered in this bin
         */
        public abstract void incrementData(int timeBin, int distanceBin, double duration, double distanceCovered);

    }

    /**
     * Store the data needed for a density contour graph.
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
     * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the
     * following disclaimer.</li>
     * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
     * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
     * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse
     * or promote products derived from this software without specific prior written permission.</li>
     * </ul>
     * This software is provided by the copyright holders and contributors "as is" and any express or implied
     * warranties, including, but not limited to, the implied warranties of merchantability and fitness for a particular
     * purpose are disclaimed. In no event shall the copyright holder or contributors be liable for any direct,
     * indirect, incidental, special, exemplary, or consequential damages (including, but not limited to, procurement of
     * substitute goods or services; loss of use, data, or profits; or business interruption) however caused and on any
     * theory of liability, whether in contract, strict liability, or tort (including negligence or otherwise) arising
     * in any way out of the use of this software, even if advised of the possibility of such damage.
     * @version Jul 17, 2014 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    class DensityContourDataSet extends ContourDataSet
    {
        /** Storage for the total time spent in each cell */
        private ArrayList<DoubleVectorAbs<TimeUnit>> cumulativeTimes = new ArrayList<DoubleVectorAbs<TimeUnit>>();

        @Override
        public double getZValue(int timeBinGroup, int distanceBinGroup)
        {
            final int timeGroupSize = (int) (ContourPlot.this.timeGranularity / timeGranularities[0]);
            final int firstTimeBin = timeBinGroup * timeGroupSize;
            double cumulativeTimeInSI = 0;
            if (firstTimeBin >= this.cumulativeTimes.size())
                return Double.NaN;
            final int distanceGroupSize = (int) (ContourPlot.this.distanceGranularity / distanceGranularities[0]);
            final int firstDistanceBin = distanceBinGroup * distanceGroupSize;
            if (firstDistanceBin * distanceGranularities[0] >= ContourPlot.this.maximumDistance)
                return Double.NaN;
            try
            {
                for (int timeBinIndex = firstTimeBin; timeBinIndex < firstTimeBin + timeGroupSize; timeBinIndex++)
                {
                    if (timeBinIndex >= this.cumulativeTimes.size())
                        break;
                    DoubleVectorAbs<TimeUnit> values = this.cumulativeTimes.get(timeBinIndex);
                    for (int distanceBinIndex = firstDistanceBin; distanceBinIndex < firstDistanceBin
                            + distanceGroupSize; distanceBinIndex++)
                    {
                        cumulativeTimeInSI += values.getSI(distanceBinIndex);
                    }
                }
            }
            catch (ValueException exception)
            {
                System.err.println("Error in getZValue(timeBinGroup=" + timeBinGroup + ", distanceBinGroup="
                        + distanceBinGroup + ")");
                exception.printStackTrace();
            }
            return 1000 * cumulativeTimeInSI / ContourPlot.this.timeGranularity / ContourPlot.this.distanceGranularity;
        }

        @SuppressWarnings("rawtypes")
        @Override
        public Comparable getSeriesKey(int series)
        {
            return "density";
        }

        /**
         * @see org.opentrafficsim.graphs.ContourPlot.ContourDataSet#incrementData(int, int, double, double)
         */
        @Override
        public void incrementData(int timeBin, int distanceBin, double duration, double distanceCovered)
        {
            if (timeBin < 0 || distanceBin < 0 || 0 == duration)
                return;
            while (timeBin >= this.cumulativeTimes.size())
                this.cumulativeTimes.add(new DoubleVectorAbsDense<TimeUnit>(
                        new double[ContourPlot.this.distanceBinCount], TimeUnit.SECOND));
            DoubleVectorAbs<TimeUnit> values = this.cumulativeTimes.get(timeBin);
            try
            {
                values.setSI(distanceBin, values.getSI(distanceBin) + duration);
            }
            catch (ValueException exception)
            {
                System.err.println("Error in incrementData:");
                exception.printStackTrace();
            }
        }

    }

    /**
     * Create a XYBlockChart.
     * @param caption String; text to show above the chart
     * @param contourType String; type of value plotted in the chart
     * @param valueFormat String; format string used to render the value in the status bar
     * @param dataset XYZDataset with the values to render
     * @param boundaries double[]; array of three boundary values corresponding to Red, Yellow and Green
     * @return JFreeChart; the new XYBlockChart
     */
    private static JFreeChart createChart(String caption, String contourType, String valueFormat, XYZDataset dataset,
            double[] boundaries)
    {
        NumberAxis xAxis = new NumberAxis("\u2192 time");
        xAxis.setLowerMargin(0.0);
        xAxis.setUpperMargin(0.0);
        NumberAxis yAxis = new NumberAxis(contourType);
        yAxis.setAutoRangeIncludesZero(false);
        yAxis.setLowerMargin(0.0);
        yAxis.setUpperMargin(0.0);
        yAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        XYBlockRenderer renderer = new XYBlockRenderer();
        Color[] colorValues = {Color.RED, Color.YELLOW, Color.GREEN};
        ColorPaintScale paintScale = new ColorPaintScale(valueFormat, boundaries, colorValues);
        renderer.setPaintScale(paintScale);
        XYPlot plot = new XYPlot(dataset, xAxis, yAxis, renderer);
        plot.setBackgroundPaint(Color.lightGray);
        plot.setDomainGridlinePaint(Color.white);
        plot.setRangeGridlinePaint(Color.white);
        plot.setForegroundAlpha(0.66f);
        JFreeChart chart = new JFreeChart(caption, plot);
        chart.removeLegend();
        chart.setBackgroundPaint(Color.white);
        return chart;
    }

    /**
     * Create a continuous color paint scale for contour plots.
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
     * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the
     * following disclaimer.</li>
     * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
     * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
     * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse
     * or promote products derived from this software without specific prior written permission.</li>
     * </ul>
     * This software is provided by the copyright holders and contributors "as is" and any express or implied
     * warranties, including, but not limited to, the implied warranties of merchantability and fitness for a particular
     * purpose are disclaimed. In no event shall the copyright holder or contributors be liable for any direct,
     * indirect, incidental, special, exemplary, or consequential damages (including, but not limited to, procurement of
     * substitute goods or services; loss of use, data, or profits; or business interruption) however caused and on any
     * theory of liability, whether in contract, strict liability, or tort (including negligence or otherwise) arising
     * in any way out of the use of this software, even if advised of the possibility of such damage.
     * @version Jul 16, 2014 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    static class ColorPaintScale implements PaintScale
    {
        /** Boundary values for this ColorPaintScale */
        private double[] bounds;

        /** Color values to use at the boundary values */
        private Color[] boundColors;

        /** Format string to render values in a human readable format (used in tool tip texts) */
        final String format;

        /**
         * Create a new ColorPaintScale.
         * @param format Format string to render the value under the mouse in a human readable format
         * @param bounds Double[] array of boundary values (must be ordered by increasing value)
         * @param boundColors Color[] array of the colors to use at the boundary values
         */
        ColorPaintScale(String format, double bounds[], Color boundColors[])
        {
            this.format = format;
            if (bounds.length < 2)
                throw new Error("bounds must have >= 2 entries");
            if (bounds.length != boundColors.length)
                throw new Error("bounds must have same length as boundColors");
            for (int i = 1; i < bounds.length; i++)
                if (bounds[i] - bounds[i - 1] <= 0)
                    throw new Error("bounds values must be strictly ascending");
            this.bounds = bounds;
            this.boundColors = boundColors;
        }

        @Override
        public double getLowerBound()
        {
            return this.bounds[0];
        }

        /**
         * Create a mixed color. Depending on the value of ratio the result varies from <i>low</i> to <i>high</i>.
         * @param ratio Double; value between 0.0 and 1.0.
         * @param low Integer; this value is returned when ratio equals 0.0
         * @param high Integer; this value is returned when ratio equals 1.0
         * @return Integer; the ratio-weighted average of <i>low</i> and <i>high</i>
         */
        private static int mixComponent(double ratio, int low, int high)
        {
            double mix = low * (1 - ratio) + high * ratio;
            int result = (int) mix;
            if (result < 0)
                result = 0;
            if (result > 255)
                result = 255;
            return result;
        }

        @Override
        public Paint getPaint(double value)
        {
            int bucket;
            for (bucket = 0; bucket < this.bounds.length - 1; bucket++)
                if (value < this.bounds[bucket + 1])
                    break;
            if (bucket >= this.bounds.length - 1)
                bucket = this.bounds.length - 2;
            double ratio = (value - this.bounds[bucket]) / (this.bounds[bucket + 1] - this.bounds[bucket]);

            Color mix =
                    new Color(mixComponent(ratio, this.boundColors[bucket].getRed(),
                            this.boundColors[bucket + 1].getRed()), mixComponent(ratio,
                            this.boundColors[bucket].getGreen(), this.boundColors[bucket + 1].getGreen()),
                            mixComponent(ratio, this.boundColors[bucket].getBlue(),
                                    this.boundColors[bucket + 1].getBlue()));
            return mix;
        }

        @Override
        public double getUpperBound()
        {
            return this.bounds[this.bounds.length - 1];
        }

    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseDragged(MouseEvent e)
    {
        // not used
    }

    /**
     * @see java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
     */
    @Override
    public void mouseMoved(MouseEvent mouseEvent)
    {
        ChartPanel cp = (ChartPanel) mouseEvent.getSource();
        XYPlot plot = (XYPlot) cp.getChart().getPlot();
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
            XYDataset dataset = plot.getDataset();
            String value = "";
            for (int item = dataset.getItemCount(0); --item >= 0;)
            {
                double x = dataset.getXValue(0, item);
                if ((x + this.timeGranularity / 2 < t) || (x - this.timeGranularity / 2 >= t))
                    continue;
                double y = dataset.getYValue(0, item);
                if ((y + this.distanceGranularity / 2 < distance) || (y - this.timeGranularity / 2 >= distance))
                    continue;
                double valueUnderMouse = ((XYZDataset) dataset).getZValue(0, item);
                if (Double.isNaN(valueUnderMouse))
                    continue;
                String format = ((ColorPaintScale) (((XYBlockRenderer) (plot.getRenderer(0))).getPaintScale())).format;
                value = String.format(format, valueUnderMouse);
            }
            this.statusLabel.setText(String.format("time %.0fs, distance %.0fm%s", t, distance, value));
        }
        else
            this.statusLabel.setText(" ");
    }

    /**
     * Main for stand alone running
     * @param args
     */
    public static void main(String[] args)
    {
        ContourPlot cp = new ContourPlot("Contour Graph", Type.DENSITY, 100, 500);
        cp.setTitle("Stand-alone demo of Contour Graph");
        cp.setPreferredSize(new java.awt.Dimension(500, 270));
        cp.pack();
        RefineryUtilities.centerFrameOnScreen(cp);
        cp.setVisible(true);
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent actionEvent)
    {
        String command = actionEvent.getActionCommand();
        System.out.println("command is \"" + command + "\"");
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
            {
                this.distanceGranularity = value;
            }
            else if (fields[0].equalsIgnoreCase("setTimeGranularity"))
            {
                this.timeGranularity = value;
            }
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
        blockRenderer.setBlockHeight(this.distanceGranularity);
        blockRenderer.setBlockWidth(this.timeGranularity);
    }

    /**
     * Notify interested parties of an event affecting this ContourPlot
     * @param event
     */
    private void notifyListeners(DatasetChangeEvent event)
    {
        for (DatasetChangeListener dcl : this.listenerList.getListeners(DatasetChangeListener.class))
            dcl.datasetChanged(event);
    }

}
