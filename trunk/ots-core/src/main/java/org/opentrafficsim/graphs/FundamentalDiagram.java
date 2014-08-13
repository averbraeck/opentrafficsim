package org.opentrafficsim.graphs;

import static org.opentrafficsim.core.unit.unitsystem.UnitSystem.SI_DERIVED;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

import javax.swing.ButtonGroup;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.SwingConstants;
import javax.swing.event.EventListenerList;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.event.AxisChangeEvent;
import org.jfree.chart.labels.XYItemLabelGenerator;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYDataset;
import org.opentrafficsim.car.Car;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.MassUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarAbs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarRel;

/**
 * The Fundamental Diagram Graph; see <a href="http://en.wikipedia.org/wiki/Fundamental_diagram_of_traffic_flow">
 * Wikipedia: http://en.wikipedia.org/wiki/Fundamental_diagram_of_traffic_flow</a>.
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
 * @version Jul 31, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class FundamentalDiagram extends JFrame implements XYDataset, ActionListener
{
    /** */
    private static final long serialVersionUID = 20140701L;

    /** The ChartPanel for this Fundamental Diagram. */
    protected JFreeChart chartPanel;

    /** Caption for this Fundamental Diagram */
    final String caption;

    /** Area to show status information. */
    protected final JLabel statusLabel;

    /** Sample duration of the detector that generates this Fundamental Diagram. */
    protected final DoubleScalarRel<TimeUnit> aggregationTime;

    /** Storage for the Samples; one for each lane covered by the detector */
    private ArrayList<ArrayList<Sample>> sampleSets;

    // TODO we need a linear density unit (1/m, 1/km). Now badly abusing MassUnit.KILOGRAM.
    /** Definition of the density axis. */
    Axis densityAxis = new Axis(new DoubleScalarAbs<MassUnit>(0, MassUnit.KILOGRAM), new DoubleScalarAbs<MassUnit>(200,
            MassUnit.KILOGRAM), null, 0d, "Density [veh/km]", "Density", "density %.1f veh/km");

    /** Definition of the speed axis */
    Axis speedAxis = new Axis(new DoubleScalarAbs<SpeedUnit>(0, SpeedUnit.KM_PER_HOUR), new DoubleScalarAbs<SpeedUnit>(
            180, SpeedUnit.KM_PER_HOUR), null, 0d, "Speed [km/h]", "Speed", "speed %.0f km/h");

    /** Definition of the flow axis */
    Axis flowAxis = new Axis(new DoubleScalarAbs<FrequencyUnit>(0, new FrequencyUnit(TimeUnit.HOUR,
            "FrequencyUnit.PerHour", "FrequencyUnit.PerH", SI_DERIVED)), new DoubleScalarAbs<FrequencyUnit>(3000d,
            FrequencyUnit.HERTZ), null, 0d, "Flow [veh/h]", "Flow", "flow %.0f veh/h");

    /** The currently shown X-axis */
    Axis xAxis;

    /** The currently shown Y-axis */
    Axis yAxis;

    /** List of parties interested in changes of this ContourPlot. */
    transient EventListenerList listenerList = new EventListenerList();

    /** Not used internally. */
    private DatasetGroup datasetGroup = null;

    /**
     * Graph a Fundamental Diagram.
     * @param caption String; the caption shown above the graphing area.
     * @param numberOfLanes Integer; the number of lanes covered by the detector that generates the data for this
     *            Fundamental diagram
     * @param aggregationTime DoubleScalarRel&lt;TimeUnit&gt;; the aggregation of the detector that generates the data
     *            for this Fundamental diagram
     */
    public FundamentalDiagram(final String caption, final int numberOfLanes,
            final DoubleScalarRel<TimeUnit> aggregationTime)
    {
        this.aggregationTime = aggregationTime;
        this.sampleSets = new ArrayList<ArrayList<Sample>>(numberOfLanes);
        for (int i = 0; i < numberOfLanes; i++)
            this.sampleSets.add(new ArrayList<Sample>());
        this.caption = caption;
        ChartFactory.setChartTheme(new StandardChartTheme("JFree/Shadow", false));
        this.chartPanel =
                ChartFactory.createXYLineChart(this.caption, "", "", this, PlotOrientation.VERTICAL, false, false,
                        false);
        XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) this.chartPanel.getXYPlot().getRenderer();
        renderer.setBaseLinesVisible(true);
        renderer.setBaseShapesVisible(true);
        renderer.setBaseItemLabelGenerator(new XYItemLabelGenerator()
        {
            @Override
            public String generateLabel(final XYDataset dataset, final int series, final int item)
            {
                return String.format("%.0fs", item * aggregationTime.getValueSI());
            }
        });
        renderer.setBaseItemLabelsVisible(true);
        ChartPanel cp = new ChartPanel(this.chartPanel);
        cp.addMouseMotionListener(new PointerHandler()
        {
            /**
             * @see org.opentrafficsim.graphs.PointerHandler#updateHint(double, double)
             */
            @Override
            void updateHint(double domainValue, double rangeValue)
            {
                String s1 = String.format(FundamentalDiagram.this.xAxis.format, domainValue);
                String s2 = String.format(FundamentalDiagram.this.yAxis.format, rangeValue);
                FundamentalDiagram.this.statusLabel.setText(s1 + ", " + s2);
            }

            /**
             * @see org.opentrafficsim.graphs.PointerHandler#clearHint()
             */
            @Override
            void clearHint()
            {
                FundamentalDiagram.this.statusLabel.setText(" ");
            }
        });
        cp.setMouseWheelEnabled(true);
        setPreferredSize(new java.awt.Dimension(500, 270));
        JMenu subMenu = new JMenu("Set layout");
        ButtonGroup group = new ButtonGroup();
        JRadioButtonMenuItem defaultItem = addMenuItem(subMenu, group, this.densityAxis, this.flowAxis, true);
        addMenuItem(subMenu, group, this.flowAxis, this.speedAxis, false);
        addMenuItem(subMenu, group, this.densityAxis, this.speedAxis, false);
        actionPerformed(new ActionEvent(this, 0, defaultItem.getActionCommand()));
        JPopupMenu popupMenu = cp.getPopupMenu();
        popupMenu.insert(subMenu, 0);
        this.add(cp, BorderLayout.CENTER);
        this.statusLabel = new JLabel(" ", SwingConstants.CENTER);
        this.add(this.statusLabel, BorderLayout.SOUTH);
    }

    /**
     * Build one JRadioButtonMenuItem for the sub menu of the context menu.
     * @param subMenu JMenu; the menu to which the new JRadioButtonMenuItem is added
     * @param group ButtonGroup; the buttonGroup for the new JRadioButtonMenuItem
     * @param newXAxis Axis; the Axis that will become X-axis when this item is clicked
     * @param newYAxis Axis; the Axis that will become Y-axis when this item is clicked
     * @param selected Boolean; if true, the new JRadioButtonMenuItem will be selected; if false, the new
     *            JRadioButtonMenuItem will <b>not</b> be selected
     */
    private JRadioButtonMenuItem addMenuItem(JMenu subMenu, ButtonGroup group, Axis newXAxis, Axis newYAxis,
            boolean selected)
    {
        JRadioButtonMenuItem item = new JRadioButtonMenuItem(newYAxis.shortName + " / " + newXAxis.shortName);
        item.setSelected(selected);
        item.setActionCommand(newYAxis.shortName + "/" + newXAxis.shortName);
        item.addActionListener(this);
        subMenu.add(item);
        group.add(item);
        return item;
    }

    /**
     * Add the effect of one passing car to this Fundamental Diagram.
     * @param lane Integer; the lane on which the car passes
     * @param car Car; the car that passes FIXME replace Car by GTU
     * @param detectionTime DoubleScalarAbs&lt;TimeUnit&gt;; the time at which the GTU passes the detector
     */
    public void addData(int lane, Car car, DoubleScalarAbs<TimeUnit> detectionTime)
    {
        ArrayList<Sample> laneData = this.sampleSets.get(lane);
        // Figure out the time bin
        int timeBin = (int) Math.floor(detectionTime.getValueSI() / this.aggregationTime.getValueSI());
        // Extend storage if needed
        while (timeBin >= laneData.size())
            laneData.add(new Sample());
        Sample sample = laneData.get(timeBin);
        sample.addData(car.getVelocity(detectionTime));
    }

    /**
     * Set up a JFreeChart axis.
     * @param valueAxis ValueAxis; the axis to set up
     * @param axis Axis; the Axis that provides the data to setup the ValueAxis
     */
    private static void configureAxis(ValueAxis valueAxis, Axis axis)
    {
        valueAxis.setLabel("\u2192 " + axis.name);
        valueAxis.setRange(axis.getMinimumValue().getValueInUnit(), axis.getMaximumValue().getValueInUnit());
    }

    /**
     * Redraw this TrajectoryGraph (after the underlying data has been changed, or to change axes).
     */
    public void reGraph()
    {
        NumberAxis numberAxis = new NumberAxis();
        configureAxis(numberAxis, this.xAxis);
        this.chartPanel.getXYPlot().setDomainAxis(numberAxis);
        this.chartPanel.getPlot().axisChanged(new AxisChangeEvent(numberAxis));
        numberAxis = new NumberAxis();
        configureAxis(numberAxis, this.yAxis);
        this.chartPanel.getXYPlot().setRangeAxis(numberAxis);
        this.chartPanel.getPlot().axisChanged(new AxisChangeEvent(numberAxis));
        notifyListeners(new DatasetChangeEvent(this, null)); // This guess work actually works!
    }

    /**
     * Notify interested parties of an event affecting this TrajectoryPlot.
     * @param event
     */
    private void notifyListeners(final DatasetChangeEvent event)
    {
        for (DatasetChangeListener dcl : this.listenerList.getListeners(DatasetChangeListener.class))
            dcl.datasetChanged(event);
    }

    /**
     * @see org.jfree.data.general.SeriesDataset#getSeriesCount()
     */
    @Override
    public int getSeriesCount()
    {
        return this.sampleSets.size();
    }

    /**
     * @see org.jfree.data.general.SeriesDataset#getSeriesKey(int)
     */
    @Override
    public Comparable<Integer> getSeriesKey(final int series)
    {
        return series;
    }

    /**
     * @see org.jfree.data.general.SeriesDataset#indexOf(java.lang.Comparable)
     */
    @SuppressWarnings("rawtypes")
    @Override
    public int indexOf(final Comparable seriesKey)
    {
        if (seriesKey instanceof Integer)
            return (Integer) seriesKey;
        return -1;
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
        return this.datasetGroup;
    }

    /**
     * @see org.jfree.data.general.Dataset#setGroup(org.jfree.data.general.DatasetGroup)
     */
    @Override
    public void setGroup(final DatasetGroup group)
    {
        this.datasetGroup = group;
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
     * @see org.jfree.data.xy.XYDataset#getItemCount(int)
     */
    @Override
    public int getItemCount(final int series)
    {
        if (series < 0 || series >= this.sampleSets.size())
            return 0;
        return this.sampleSets.get(series).size();
    }

    /**
     * Retrieve a value from the recorded samples
     * @param lane Integer; the lane
     * @param item Integer; the rank number of the sample
     * @param axis Axis; the axis that determines which quantity to retrieve
     * @return Double; the requested value, or Double.NaN if the sample does not (yet) exist
     */
    private Double getSample(final int lane, final int item, final Axis axis)
    {
        if (lane < 0 || lane >= this.sampleSets.size() || item < 0)
            return Double.NaN;
        ArrayList<Sample> laneDetections = this.sampleSets.get(lane);
        if (item >= laneDetections.size())
            return Double.NaN;
        double result = laneDetections.get(item).getValue(axis);
        /*-
        System.out.println(String.format("getSample(lane=%d, item=%d, axis=%s) returns %f", lane, item, axis.name,
                result));
         */
        return result;
    }

    /**
     * @see org.jfree.data.xy.XYDataset#getX(int, int)
     */
    @Override
    public Number getX(final int series, final int item)
    {
        return getSample(series, item, this.xAxis);
    }

    /**
     * @see org.jfree.data.xy.XYDataset#getXValue(int, int)
     */
    @Override
    public double getXValue(final int series, final int item)
    {
        return getSample(series, item, this.xAxis);
    }

    /**
     * @see org.jfree.data.xy.XYDataset#getY(int, int)
     */
    @Override
    public Number getY(final int series, final int item)
    {
        return getSample(series, item, this.yAxis);
    }

    /**
     * @see org.jfree.data.xy.XYDataset#getYValue(int, int)
     */
    @Override
    public double getYValue(final int series, final int item)
    {
        return getSample(series, item, this.yAxis);
    }

    /**
     * Storage for one sample of data collected by a point-detector that accumulates harmonic mean speed and flow.
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
     * @version Jul 31, 2014 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    class Sample
    {
        /** Harmonic mean speed observed during this sample [m/s] */
        double harmonicMeanSpeed;

        /** Flow observed during this sample [veh/s] */
        double flow;

        /**
         * Retrieve a value stored in this Sample.
         * @param axis Axis; the axis along which the data is requested
         * @return double; the retrieved value
         */
        public double getValue(final Axis axis)
        {
            if (axis == FundamentalDiagram.this.densityAxis)
                return this.flow * 3600 / FundamentalDiagram.this.aggregationTime.getValueSI() / this.harmonicMeanSpeed;
            else if (axis == FundamentalDiagram.this.flowAxis)
                return this.flow * 3600 / FundamentalDiagram.this.aggregationTime.getValueSI();
            else if (axis == FundamentalDiagram.this.speedAxis)
                return this.harmonicMeanSpeed * 3600 / 1000;
            else
                throw new Error("Bad switch. Cannot happen");
        }

        /**
         * Add one Car detection to this Sample.
         * @param speed DoubleScalarRel&lt;SpeedUnit&gt;; the detected speed
         */
        public void addData(DoubleScalarRel<SpeedUnit> speed)
        {
            double sumReciprocalSpeeds = 0;
            if (this.flow > 0)
                sumReciprocalSpeeds = this.flow / this.harmonicMeanSpeed;
            this.flow += 1;
            sumReciprocalSpeeds += 1d / speed.getValueSI();
            this.harmonicMeanSpeed = this.flow / sumReciprocalSpeeds;
        }
    }

    /**
     * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
     */
    @Override
    public void actionPerformed(ActionEvent actionEvent)
    {
        String command = actionEvent.getActionCommand();
        // System.out.println("command is \"" + command + "\"");
        String[] fields = command.split("[/]");
        if (fields.length == 2)
        {
            for (String field : fields)
            {
                if (field.equalsIgnoreCase(this.densityAxis.shortName))
                    if (field == fields[0])
                        this.yAxis = this.densityAxis;
                    else
                        this.xAxis = this.densityAxis;
                else if (field.equalsIgnoreCase(this.flowAxis.shortName))
                    if (field == fields[0])
                        this.yAxis = this.flowAxis;
                    else
                        this.xAxis = this.flowAxis;
                else if (field.equalsIgnoreCase(this.speedAxis.shortName))
                    if (field == fields[0])
                        this.yAxis = this.speedAxis;
                    else
                        this.xAxis = this.speedAxis;
                else
                    throw new Error("Cannot find axis name: " + field);
            }
            reGraph();
        }
        else
            throw new Error("Unknown ActionEvent");
    }

}
