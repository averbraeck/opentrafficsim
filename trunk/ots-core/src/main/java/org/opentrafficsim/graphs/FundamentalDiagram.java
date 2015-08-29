package org.opentrafficsim.graphs;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.rmi.RemoteException;
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
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.lane.AbstractSensor;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.LinearDensityUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * The Fundamental Diagram Graph; see <a href="http://en.wikipedia.org/wiki/Fundamental_diagram_of_traffic_flow"> Wikipedia:
 * http://en.wikipedia.org/wiki/Fundamental_diagram_of_traffic_flow</a>.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version Jul 31, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class FundamentalDiagram extends JFrame implements XYDataset, ActionListener
{
    /** */
    private static final long serialVersionUID = 20140701L;

    /** The ChartPanel for this Fundamental Diagram. */
    private JFreeChart chartPanel;

    /** Caption for this Fundamental Diagram. */
    private final String caption;

    /** Position of this Fundamental Diagram sensor. */
    private final DoubleScalar.Rel<LengthUnit> position;

    /** Area to show status information. */
    private final JLabel statusLabel;

    /** Sample duration of the detector that generates this Fundamental Diagram. */
    private final DoubleScalar.Rel<TimeUnit> aggregationTime;

    /**
     * @return aggregationTime
     */
    public final DoubleScalar.Rel<TimeUnit> getAggregationTime()
    {
        return this.aggregationTime;
    }

    /** Storage for the Samples. */
    private ArrayList<Sample> samples = new ArrayList<Sample>();

    /** Definition of the density axis. */
    private Axis densityAxis = new Axis(new DoubleScalar.Abs<LinearDensityUnit>(0, LinearDensityUnit.PER_KILOMETER),
        new DoubleScalar.Abs<LinearDensityUnit>(200, LinearDensityUnit.PER_KILOMETER), null, 0d, "Density [veh/km]",
        "Density", "density %.1f veh/km");

    /**
     * @return densityAxis
     */
    public final Axis getDensityAxis()
    {
        return this.densityAxis;
    }

    /** Definition of the speed axis. */
    private Axis speedAxis = new Axis(new DoubleScalar.Abs<SpeedUnit>(0, SpeedUnit.KM_PER_HOUR),
        new DoubleScalar.Abs<SpeedUnit>(180, SpeedUnit.KM_PER_HOUR), null, 0d, "Speed [km/h]", "Speed", "speed %.0f km/h");

    /**
     * @return speedAxis
     */
    public final Axis getSpeedAxis()
    {
        return this.speedAxis;
    }

    /**
     * @return flowAxis
     */
    public final Axis getFlowAxis()
    {
        return this.flowAxis;
    }

    /** Definition of the flow axis. */
    private Axis flowAxis =
        new Axis(new DoubleScalar.Abs<FrequencyUnit>(0, FrequencyUnit.PER_HOUR), new DoubleScalar.Abs<FrequencyUnit>(3000d,
            FrequencyUnit.HERTZ), null, 0d, "Flow [veh/h]", "Flow", "flow %.0f veh/h");

    /** The currently shown X-axis. */
    private Axis xAxis;

    /** The currently shown Y-axis. */
    private Axis yAxis;

    /** List of parties interested in changes of this ContourPlot. */
    private transient EventListenerList listenerList = new EventListenerList();

    /** Not used internally. */
    private DatasetGroup datasetGroup = null;

    /**
     * Retrieve the format string for the Y axis.
     * @return format string
     */
    public final String getYAxisFormat()
    {
        return this.yAxis.getFormat();
    }

    /**
     * Retrieve the format string for the X axis.
     * @return format string
     */
    public final String getXAxisFormat()
    {
        return this.xAxis.getFormat();
    }

    /**
     * Graph a Fundamental Diagram.
     * @param caption String; the caption shown above the graphing area.
     * @param aggregationTime DoubleScalarRel&lt;TimeUnit&gt;; the aggregation of the detector that generates the data for this
     *            Fundamental diagram
     * @param lane Lane; the Lane on which the traffic will be sampled
     * @param position DoubleScalarRel&lt;LengthUnit&gt;; longitudinal position of the detector on the Lane
     * @throws NetworkException on network inconsistency
     */
    public FundamentalDiagram(final String caption, final DoubleScalar.Rel<TimeUnit> aggregationTime, final Lane lane,
        final DoubleScalar.Rel<LengthUnit> position) throws NetworkException
    {
        if (aggregationTime.getSI() <= 0)
        {
            throw new Error("Aggregation time must be > 0 (got " + aggregationTime + ")");
        }
        this.aggregationTime = aggregationTime;
        this.caption = caption;
        this.position = position;
        ChartFactory.setChartTheme(new StandardChartTheme("JFree/Shadow", false));
        this.chartPanel =
            ChartFactory.createXYLineChart(this.caption, "", "", this, PlotOrientation.VERTICAL, false, false, false);
        FixCaption.fixCaption(this.chartPanel);
        final XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) this.chartPanel.getXYPlot().getRenderer();
        renderer.setBaseLinesVisible(true);
        renderer.setBaseShapesVisible(true);
        renderer.setBaseItemLabelGenerator(new XYItemLabelGenerator()
        {
            @Override
            public String generateLabel(final XYDataset dataset, final int series, final int item)
            {
                return String.format("%.0fs", item * aggregationTime.getSI());
            }
        });
        renderer.setBaseItemLabelsVisible(true);
        final ChartPanel cp = new ChartPanel(this.chartPanel);
        PointerHandler ph = new PointerHandler()
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
                String s1 = String.format(getXAxisFormat(), domainValue);
                String s2 = String.format(getYAxisFormat(), rangeValue);
                setStatusText(s1 + ", " + s2);
            }

        };
        cp.addMouseMotionListener(ph);
        cp.addMouseListener(ph);
        cp.setMouseWheelEnabled(true);
        final JMenu subMenu = new JMenu("Set layout");
        final ButtonGroup group = new ButtonGroup();
        final JRadioButtonMenuItem defaultItem = addMenuItem(subMenu, group, getDensityAxis(), this.flowAxis, true);
        addMenuItem(subMenu, group, this.flowAxis, this.speedAxis, false);
        addMenuItem(subMenu, group, this.densityAxis, this.speedAxis, false);
        actionPerformed(new ActionEvent(this, 0, defaultItem.getActionCommand()));
        final JPopupMenu popupMenu = cp.getPopupMenu();
        popupMenu.insert(subMenu, 0);
        this.add(cp, BorderLayout.CENTER);
        this.statusLabel = new JLabel(" ", SwingConstants.CENTER);
        this.add(this.statusLabel, BorderLayout.SOUTH);
        new FundamentalDiagramSensor(lane, position, null);
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
     * Retrieve the position of the detector.
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;; the position of the detector
     */
    public final DoubleScalar.Rel<LengthUnit> getPosition()
    {
        return this.position;
    }

    /**
     * Build one JRadioButtonMenuItem for the sub menu of the context menu.
     * @param subMenu JMenu; the menu to which the new JRadioButtonMenuItem is added
     * @param group ButtonGroup; the buttonGroup for the new JRadioButtonMenuItem
     * @param xAxisToSelect Axis; the Axis that will become X-axis when this item is clicked
     * @param yAxisToSelect Axis; the Axis that will become Y-axis when this item is clicked
     * @param selected Boolean; if true, the new JRadioButtonMenuItem will be selected; if false, the new JRadioButtonMenuItem
     *            will <b>not</b> be selected
     * @return JRatioButtonMenuItem; the newly added item
     */
    private JRadioButtonMenuItem addMenuItem(final JMenu subMenu, final ButtonGroup group, final Axis xAxisToSelect,
        final Axis yAxisToSelect, final boolean selected)
    {
        final JRadioButtonMenuItem item =
            new JRadioButtonMenuItem(yAxisToSelect.getShortName() + " / " + xAxisToSelect.getShortName());
        item.setSelected(selected);
        item.setActionCommand(yAxisToSelect.getShortName() + "/" + xAxisToSelect.getShortName());
        item.addActionListener(this);
        subMenu.add(item);
        group.add(item);
        return item;
    }

    /**
     * Add the effect of one passing car to this Fundamental Diagram.
     * @param gtu AbstractLaneBasedGTU; the GTU that passes the detection point
     */
    public final void addData(final LaneBasedGTU gtu)
    {
        try
        {
            DoubleScalar.Abs<TimeUnit> detectionTime = gtu.getSimulator().getSimulatorTime().get();
            // Figure out the time bin
            final int timeBin = (int) Math.floor(detectionTime.getSI() / this.aggregationTime.getSI());
            // Extend storage if needed
            while (timeBin >= this.samples.size())
            {
                this.samples.add(new Sample());
            }
            Sample sample = this.samples.get(timeBin);
            sample.addData(gtu.getLongitudinalVelocity(detectionTime));
        }
        catch (RemoteException exception)
        {
            exception.printStackTrace();
        }
    }

    /**
     * Set up a JFreeChart axis.
     * @param valueAxis ValueAxis; the axis to set up
     * @param axis Axis; the Axis that provides the data to setup the ValueAxis
     */
    private static void configureAxis(final ValueAxis valueAxis, final Axis axis)
    {
        valueAxis.setLabel("\u2192 " + axis.getName());
        valueAxis.setRange(axis.getMinimumValue().getInUnit(), axis.getMaximumValue().getInUnit());
    }

    /**
     * Redraw this TrajectoryGraph (after the underlying data has been changed, or to change axes).
     */
    public final void reGraph()
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
     * @param event DatasetChangedEvent
     */
    private void notifyListeners(final DatasetChangeEvent event)
    {
        for (DatasetChangeListener dcl : this.listenerList.getListeners(DatasetChangeListener.class))
        {
            dcl.datasetChanged(event);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final int getSeriesCount()
    {
        return 1;
    }

    /** {@inheritDoc} */
    @Override
    public final Comparable<Integer> getSeriesKey(final int series)
    {
        return series;
    }

    /** {@inheritDoc} */
    @SuppressWarnings("rawtypes")
    @Override
    public final int indexOf(final Comparable seriesKey)
    {
        if (seriesKey instanceof Integer)
        {
            return (Integer) seriesKey;
        }
        return -1;
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
        return this.datasetGroup;
    }

    /** {@inheritDoc} */
    @Override
    public final void setGroup(final DatasetGroup group)
    {
        this.datasetGroup = group;
    }

    /** {@inheritDoc} */
    @Override
    public final DomainOrder getDomainOrder()
    {
        return DomainOrder.ASCENDING;
    }

    /** {@inheritDoc} */
    @Override
    public final int getItemCount(final int series)
    {
        return this.samples.size();
    }

    /**
     * Retrieve a value from the recorded samples.
     * @param item Integer; the rank number of the sample
     * @param axis Axis; the axis that determines which quantity to retrieve
     * @return Double; the requested value, or Double.NaN if the sample does not (yet) exist
     */
    private Double getSample(final int item, final Axis axis)
    {
        if (item >= this.samples.size())
        {
            return Double.NaN;
        }
        double result = this.samples.get(item).getValue(axis);
        /*-
        System.out.println(String.format("getSample(item=%d, axis=%s) returns %f", item, axis.name,
                result));
         */
        return result;
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
        return getSample(item, this.xAxis);
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
        return getSample(item, this.yAxis);
    }

    /**
     * Storage for one sample of data collected by a point-detector that accumulates harmonic mean speed and flow.
     * <p>
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <p>
     * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
     * <p>
     * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
     * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
     * following conditions are met:
     * <ul>
     * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
     * disclaimer.</li>
     * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
     * disclaimer in the documentation and/or other materials provided with the distribution.</li>
     * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
     * promote products derived from this software without specific prior written permission.</li>
     * </ul>
     * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
     * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
     * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
     * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or services;
     * loss of use, data, or profits; or business interruption) however caused and on any theory of liability, whether in
     * contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use of this
     * software, even if advised of the possibility of such damage. $LastChangedDate: 2015-07-15 11:18:39 +0200 (Wed, 15 Jul
     * 2015) $, @version $Revision$, by $Author$, initial versionJul 31, 2014 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    class Sample
    {
        /** Harmonic mean speed observed during this sample [m/s]. */
        private double harmonicMeanSpeed;

        /** Flow observed during this sample [veh/s]. */
        private double flow;

        /**
         * Retrieve a value stored in this Sample.
         * @param axis Axis; the axis along which the data is requested
         * @return double; the retrieved value
         */
        public double getValue(final Axis axis)
        {
            if (axis == getDensityAxis())
            {
                return this.flow * 3600 / getAggregationTime().getSI() / this.harmonicMeanSpeed;
            }
            else if (axis == getFlowAxis())
            {
                return this.flow * 3600 / getAggregationTime().getSI();
            }
            else if (axis == getSpeedAxis())
            {
                return this.harmonicMeanSpeed * 3600 / 1000;
            }
            else
            {
                throw new Error("Sample.getValue: Can not identify axis");
            }
        }

        /**
         * Add one Car detection to this Sample.
         * @param speed DoubleScalar.Rel&lt;SpeedUnit&gt;; the detected speed
         */
        public void addData(final DoubleScalar.Abs<SpeedUnit> speed)
        {
            double sumReciprocalSpeeds = 0;
            if (this.flow > 0)
            {
                sumReciprocalSpeeds = this.flow / this.harmonicMeanSpeed;
            }
            this.flow += 1;
            sumReciprocalSpeeds += 1d / speed.getSI();
            this.harmonicMeanSpeed = this.flow / sumReciprocalSpeeds;
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void actionPerformed(final ActionEvent actionEvent)
    {
        final String command = actionEvent.getActionCommand();
        // System.out.println("command is \"" + command + "\"");
        final String[] fields = command.split("[/]");
        if (fields.length == 2)
        {
            for (String field : fields)
            {
                if (field.equalsIgnoreCase(this.densityAxis.getShortName()))
                {
                    if (field == fields[0])
                    {
                        this.yAxis = this.densityAxis;
                    }
                    else
                    {
                        this.xAxis = this.densityAxis;
                    }
                }
                else if (field.equalsIgnoreCase(this.flowAxis.getShortName()))
                {
                    if (field == fields[0])
                    {
                        this.yAxis = this.flowAxis;
                    }
                    else
                    {
                        this.xAxis = this.flowAxis;
                    }
                }
                else if (field.equalsIgnoreCase(this.speedAxis.getShortName()))
                {
                    if (field == fields[0])
                    {
                        this.yAxis = this.speedAxis;
                    }
                    else
                    {
                        this.xAxis = this.speedAxis;
                    }
                }
                else
                {
                    throw new Error("Cannot find axis name: " + field);
                }
            }
            reGraph();
        }
        else
        {
            throw new Error("Unknown ActionEvent");
        }
    }

    /**
     * Internal Sensor class.
     * <p>
     * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * $LastChangedDate$, @version $Revision$, by $Author$,
     * initial version feb. 2015 <br>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     */
    class FundamentalDiagramSensor extends AbstractSensor
    {
        /** */
        private static final long serialVersionUID = 20150203L;

        /**
         * Construct a FundamentalDiagramSensor.
         * @param lane Lane; the Lane on which the new FundamentalDiagramSensor is to be added
         * @param longitudinalPosition DoubleScalar.Abs&lt;LengthUnit&gt;; longitudinal position on the Lane of the new
         *            FundamentalDiagramSensor
         * @param simulator simulator to allow animation
         * @throws NetworkException on network inconsistency
         */
        public FundamentalDiagramSensor(final Lane lane, final DoubleScalar.Rel<LengthUnit> longitudinalPosition,
            final OTSSimulatorInterface simulator) throws NetworkException
        {
            super(lane, longitudinalPosition, RelativePosition.REFERENCE, "FUNDAMENTAL_DIAGRAM_SENSOR@" + lane.toString(),
                simulator);
            lane.addSensor(this, GTUType.ALL);
        }

        /** {@inheritDoc} */
        @Override
        public void trigger(final LaneBasedGTU gtu) throws RemoteException
        {
            addData(gtu);
        }

        /** {@inheritDoc} */
        public final String toString()
        {
            return "FundamentalDiagramSensor at " + getLongitudinalPosition();
        }

    }

}
