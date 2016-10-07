package org.opentrafficsim.demo.lanechange;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import javax.naming.NamingException;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.gui.swing.TablePanel;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.djunits.unit.TimeUnit;
import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.DoubleScalar.Abs;
import org.djunits.value.vdouble.scalar.DoubleScalar.Rel;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.plot.XYPlot;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeEvent;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYDataset;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.network.route.CompleteRoute;
import org.opentrafficsim.gui.SimulatorFrame;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.simulationengine.SimpleSimulator;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 15 apr. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class SuitabilityGraph implements OTSModelInterface, UNITS
{
    /** */
    private static final long serialVersionUID = 20150415L;

    /** The JPanel that contains all the graphs. */
    private JPanel graphPanel;

    /** Number of lanes on the main roadway (do not set higher than size of colorTable). */
    private static final int LANECOUNT = 4;

    /** Speed limit values in km/h. */
    private static final double[] SPEEDLIMITS = { 30, 50, 80, 120 };

    /** Arrangements of lanes to aim for. Negative numbers indicate lanes on right side of the roadway. */
    private static final int[] TARGETLANES = { 1, 2, -2, -1 };

    /** Time horizon for lane changes. */
    private Duration timeHorizon = new Duration(100, SECOND);

    /** Time range for graphs (also adjusts distance range). */
    private Duration timeRange = new Duration(110, SECOND);

    /** Colors that correspond to the lanes; taken from electrical resistor color codes. */
    private static final Color[] COLORTABLE = { new Color(160, 82, 45) /* brown */, Color.RED, Color.ORANGE, Color.YELLOW,
            Color.GREEN, Color.BLUE, new Color(199, 21, 133) /* violet */, Color.GRAY, Color.WHITE };

    /** The graphs. */
    private JFreeChart[][] charts;

    /**
     * Start the program.
     * @param args String[]; command line arguments (not used)
     * @throws SimRuntimeException should never happen
     */
    public static void main(final String[] args) throws SimRuntimeException
    {
        SwingUtilities.invokeLater(new Runnable()
        {
            @Override
            public void run()
            {
                SuitabilityGraph suitabilityGraph = new SuitabilityGraph();
                new SimulatorFrame("Suitability graph", suitabilityGraph.getPanel());
                try
                {
                    suitabilityGraph.drawPlots();
                }
                catch (NamingException | NetworkException | SimRuntimeException | OTSGeometryException | GTUException exception)
                {
                    exception.printStackTrace();
                }
            }
        });
    }

    /**
     * Draw the plots.
     * @throws NetworkException on network inconsistency
     * @throws NamingException on ???
     * @throws SimRuntimeException on ???
     * @throws OTSGeometryException
     * @throws GTUException
     */
    protected final void drawPlots() throws NamingException, NetworkException, SimRuntimeException, OTSGeometryException,
            GTUException
    {
        SimpleSimulator simulator =
                new SimpleSimulator(new Time(0, TimeUnit.SI), new Duration(0, TimeUnit.SI), new Duration(99999, TimeUnit.SI),
                        this);
        final int rows = SPEEDLIMITS.length;
        final int columns = TARGETLANES.length;
        for (int row = 0; row < rows; row++)
        {
            int targetLaneConfiguration = TARGETLANES[row];
            for (int column = 0; column < columns; column++)
            {
                Network network = new OTSNetwork("suitability graph network");
                Speed speedLimit = new Speed(SPEEDLIMITS[column], KM_PER_HOUR);
                double mainLength = speedLimit.getSI() * this.timeRange.getSI();
                OTSNode from = new OTSNode(network, "From", new OTSPoint3D(-mainLength, 0, 0));
                OTSNode branchPoint = new OTSNode(network, "Branch point", new OTSPoint3D(0, 0, 0));
                GTUType gtuType = new GTUType("Car");
                Set<GTUType> compatibility = new HashSet<GTUType>();
                compatibility.add(gtuType);
                LaneType laneType = new LaneType("CarLane", compatibility);
                Lane[] lanes =
                        LaneFactory.makeMultiLane(network, "Test road", from, branchPoint, null, LANECOUNT, laneType,
                                speedLimit, simulator, LongitudinalDirectionality.DIR_PLUS);
                OTSNode destination =
                        new OTSNode(network, "Destination", new OTSPoint3D(1000, targetLaneConfiguration > 0 ? 100 : -100, 0));
                LaneFactory.makeMultiLane(network, "DestinationLink", branchPoint, destination, null,
                        Math.abs(targetLaneConfiguration), targetLaneConfiguration > 0 ? 0 : LANECOUNT
                                + targetLaneConfiguration, 0, laneType, speedLimit, simulator,
                        LongitudinalDirectionality.DIR_PLUS);
                OTSNode nonDestination =
                        new OTSNode(network, "Non-Destination", new OTSPoint3D(1000, targetLaneConfiguration > 0 ? -100 : 100,
                                0));
                LaneFactory.makeMultiLane(network, "Non-DestinationLink", branchPoint, nonDestination, null,
                        LANECOUNT - Math.abs(targetLaneConfiguration), targetLaneConfiguration > 0 ? LANECOUNT
                                - targetLaneConfiguration : 0, 0, laneType, speedLimit, simulator,
                        LongitudinalDirectionality.DIR_PLUS);
                CompleteRoute route = new CompleteRoute("route", gtuType);
                route.addNode(from);
                route.addNode(branchPoint);
                route.addNode(destination);
                SuitabilityData dataset = (SuitabilityData) ((XYPlot) (this.charts[row][column].getPlot())).getDataset();
                for (int laneIndex = 0; laneIndex < LANECOUNT; laneIndex++)
                {
                    int key = dataset.addSeries("Lane " + (laneIndex + 1));
                    Lane lane = lanes[laneIndex];
                    for (int position = 0; position <= mainLength; position += 10)
                    {
                        Length longitudinalPosition = new Length(position, METER);
                        // TODO Length suitability =
                        // navigator.suitability(lane, longitudinalPosition, null, this.timeHorizon);
                        // if (suitability.getSI() <= mainLength)
                        // {
                        // dataset.addXYPair(key, mainLength - position, suitability.getSI());
                        // }
                    }
                    dataset.reGraph();
                }
            }
        }
    }

    /**
     * Instantiate the class.
     */
    public SuitabilityGraph()
    {
        this.graphPanel = new JPanel(new BorderLayout());
        final int rows = SPEEDLIMITS.length;
        final int columns = TARGETLANES.length;
        TablePanel chartsPanel = new TablePanel(rows, rows);
        this.graphPanel.add(chartsPanel, BorderLayout.CENTER);
        this.charts = new JFreeChart[rows][columns];
        for (int row = 0; row < rows; row++)
        {
            int targetLaneConfiguration = TARGETLANES[row];
            String targetLaneDescription =
                    String.format("%s lane %s exit", Math.abs(targetLaneConfiguration) == 1 ? "single" : "double",
                            targetLaneConfiguration > 0 ? "left" : "right");
            for (int column = 0; column < columns; column++)
            {
                Speed speedLimit = new Speed(SPEEDLIMITS[column], KM_PER_HOUR);
                JFreeChart chart =
                        createChart(String.format("Speed limit %.0f%s, %s", speedLimit.getInUnit(), speedLimit.getUnit(),
                                targetLaneDescription), speedLimit);
                chartsPanel.setCell(new ChartPanel(chart), column, row);
                this.charts[row][column] = chart;
            }
        }
    }

    /**
     * @param caption String; the caption for the chart
     * @param speedLimit Speed; the speed limit
     * @return JFreeChart; the newly created graph
     */
    private JFreeChart createChart(final String caption, final Speed speedLimit)
    {
        ChartFactory.setChartTheme(new StandardChartTheme("JFree/Shadow", false));
        XYDataset chartData = new SuitabilityData();
        JFreeChart chartPanel =
                ChartFactory.createXYLineChart(caption, "", "", chartData, PlotOrientation.VERTICAL, true, false, false);
        chartPanel.setBorderVisible(true);
        chartPanel.setBorderPaint(new Color(192, 192, 192));
        NumberAxis timeAxis = new NumberAxis("\u2192 " + "Remaining time to junction [s]");
        double distanceRange = this.timeRange.getSI() * speedLimit.getSI();
        NumberAxis distanceAxis = new NumberAxis("\u2192 " + "Remaining distance to junction [m]");
        distanceAxis.setRange(0, distanceRange);
        distanceAxis.setInverted(true);
        distanceAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        timeAxis.setAutoRangeIncludesZero(true);
        timeAxis.setRange(0, this.timeRange.getSI());
        timeAxis.setInverted(true);
        timeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        // time axis gets messed up on auto range (probably due to all data being relative to distance axis)
        // ((XYPlot) chartPanel.getPlot()).setDomainAxis(1, timeAxis);
        NumberAxis yAxis = new NumberAxis("\u2192 " + "Distance to vacate lane [m]");
        yAxis.setAutoRangeIncludesZero(true);
        yAxis.setRange(-0.1, distanceRange);
        chartPanel.getXYPlot().setDomainAxis(distanceAxis);
        chartPanel.getXYPlot().setRangeAxis(yAxis);
        final XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) chartPanel.getXYPlot().getRenderer();
        renderer.setBaseLinesVisible(true);
        renderer.setBaseShapesVisible(false);
        // Set paint color and stroke for each series
        for (int index = 0; index < LANECOUNT; index++)
        {
            renderer.setSeriesPaint(index, COLORTABLE[index]);
            renderer.setSeriesStroke(index, new BasicStroke(4.0f));
        }

        return chartPanel;
    }

    /**
     * Return the JPanel that contains all the graphs.
     * @return JPanel
     */
    public final JPanel getPanel()
    {
        return this.graphPanel;
    }

    /** {@inheritDoc} */
    @Override
    public final void constructModel(final SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> simulator)
            throws SimRuntimeException, RemoteException
    {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public final SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> getSimulator() throws RemoteException
    {
        return null;
    }

}

/** */
class SuitabilityData implements XYDataset
{

    /** The X values. */
    private ArrayList<ArrayList<Double>> xValues = new ArrayList<ArrayList<Double>>();

    /** The Y values. */
    private ArrayList<ArrayList<Double>> yValues = new ArrayList<ArrayList<Double>>();

    /** The names of the series. */
    private ArrayList<String> seriesKeys = new ArrayList<String>();

    /** List of parties interested in changes of this ContourPlot. */
    private transient EventListenerList listenerList = new EventListenerList();

    /** Not used internally. */
    private DatasetGroup datasetGroup = null;

    /**
     * Redraw this graph (after the underlying data has been changed).
     */
    public final void reGraph()
    {
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

    /**
     * Add storage for another series of XY values.
     * @param seriesName String; the name of the new series
     * @return int; the index to use to address the new series
     */
    public final int addSeries(final String seriesName)
    {
        this.xValues.add(new ArrayList<Double>());
        this.yValues.add(new ArrayList<Double>());
        this.seriesKeys.add(seriesName);
        return this.xValues.size() - 1;
    }

    /**
     * Add an XY pair to the data.
     * @param seriesKey int; key to the data series
     * @param x double; x value of the pair
     * @param y double; y value of the pair
     */
    public final void addXYPair(final int seriesKey, final double x, final double y)
    {
        this.xValues.get(seriesKey).add(x);
        this.yValues.get(seriesKey).add(y);
    }

    /** {@inheritDoc} */
    @Override
    public final int getSeriesCount()
    {
        return this.seriesKeys.size();
    }

    /** {@inheritDoc} */
    @Override
    public final Comparable<?> getSeriesKey(final int series)
    {
        return this.seriesKeys.get(series);
    }

    /** {@inheritDoc} */
    @Override
    public final int indexOf(@SuppressWarnings("rawtypes") final Comparable seriesKey)
    {
        return this.seriesKeys.indexOf(seriesKey);
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
    public DomainOrder getDomainOrder()
    {
        return DomainOrder.ASCENDING;
    }

    /** {@inheritDoc} */
    @Override
    public final int getItemCount(final int series)
    {
        return this.xValues.get(series).size();
    }

    /** {@inheritDoc} */
    @Override
    public final Number getX(final int series, final int item)
    {
        return this.xValues.get(series).get(item);
    }

    /** {@inheritDoc} */
    @Override
    public final double getXValue(final int series, final int item)
    {
        return this.xValues.get(series).get(item);
    }

    /** {@inheritDoc} */
    @Override
    public final Number getY(final int series, final int item)
    {
        return this.yValues.get(series).get(item);
    }

    /** {@inheritDoc} */
    @Override
    public final double getYValue(final int series, final int item)
    {
        return this.yValues.get(series).get(item);
    }

}
