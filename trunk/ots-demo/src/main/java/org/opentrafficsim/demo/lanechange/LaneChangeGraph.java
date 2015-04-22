package org.opentrafficsim.demo.lanechange;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.geom.Line2D;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import javax.naming.NamingException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.gui.swing.TablePanel;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.StandardChartTheme;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.event.PlotChangeEvent;
import org.jfree.chart.plot.Plot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.DomainOrder;
import org.jfree.data.general.DatasetChangeListener;
import org.jfree.data.general.DatasetGroup;
import org.jfree.data.xy.XYDataset;
import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.following.HeadwayGTU;
import org.opentrafficsim.core.gtu.following.IDM;
import org.opentrafficsim.core.gtu.following.IDMPlus;
import org.opentrafficsim.core.gtu.lane.changing.Altruistic;
import org.opentrafficsim.core.gtu.lane.changing.Egoistic;
import org.opentrafficsim.core.gtu.lane.changing.LaneChangeModel;
import org.opentrafficsim.core.gtu.lane.changing.LaneMovementStep;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.factory.LaneFactory;
import org.opentrafficsim.core.network.geotools.NodeGeotools;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.LaneType;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar;
import org.opentrafficsim.simulationengine.SimpleSimulator;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Create a plot that characterizes a lane change graph.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 18 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneChangeGraph extends JFrame implements OTSModelInterface
{
    /** */
    private static final long serialVersionUID = 20141118L;

    /** Standard speed values in km/h. */
    static double standardSpeeds[] = {30, 50, 80, 100, 120};

    /** The main window. */
    LaneChangeGraph mainWindow;

    /** The car following model. */
    GTUFollowingModel carFollowingModel;

    /** The graphs. */
    ChartPanel[][] charts;

    /** Start of two lane road. */
    static final DoubleScalar.Rel<LengthUnit> lowerBound = new DoubleScalar.Rel<LengthUnit>(-500, LengthUnit.METER);

    /** Position of reference vehicle on the two lane road. */
    static final DoubleScalar.Rel<LengthUnit> midPoint = new DoubleScalar.Rel<LengthUnit>(0, LengthUnit.METER);

    /** End of two lane road. */
    static final DoubleScalar.Rel<LengthUnit> upperBound = new DoubleScalar.Rel<LengthUnit>(500, LengthUnit.METER);

    /** The JFrame with the lane change graphs. */
    static LaneChangeGraph lcs;
    
    /**
     * Create a Lane Change Graph
     * @param title String; title text of the window
     * @param mainPanel JPanel; panel that will (indirectly?) contain the charts
     */
    LaneChangeGraph(final String title, JPanel mainPanel)
    {
        super(title);
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.charts = new ChartPanel[2][standardSpeeds.length];
    }
    
    /**
     * Main entry point; now Swing thread safe (I hope).
     * @param args
     * @throws GTUException 
     * @throws SimRuntimeException 
     * @throws NetworkException 
     * @throws NamingException 
     * @throws RemoteException 
     */
    public static void main(final String[] args) throws RemoteException, NamingException, NetworkException, SimRuntimeException, GTUException
    {
        try
        {
            SwingUtilities.invokeAndWait(new Runnable()
            {

                @Override
                public void run()
                {
                    try
                    {
                        buildGUI(args);
                    }
                    catch (RemoteException | NamingException | NetworkException | SimRuntimeException | GTUException exception)
                    {
                        exception.printStackTrace();
                    }
                }
                
            });
        }
        catch (InvocationTargetException | InterruptedException exception)
        {
            exception.printStackTrace();
        }
        for (int row = 0; row < lcs.charts.length; row++)
        {
            LaneChangeModel laneChangeModel = 0 == row ? new Egoistic() : new Altruistic();
            for (int index = 0; index < standardSpeeds.length; index++)
            {
                DoubleScalar.Abs<SpeedUnit> speed =
                        new DoubleScalar.Abs<SpeedUnit>(standardSpeeds[index], SpeedUnit.KM_PER_HOUR);
                // System.out.println("speed " + speed);
                double startSpeedDifference = -30;// standardSpeeds[index];
                double endSpeedDifference = startSpeedDifference + 60;// 150;
                ChartData data = (ChartData) lcs.charts[row][index].getChart().getXYPlot().getDataset();
                int beginRightKey = data.addSeries("Begin of no lane change to right");
                int endRightKey = data.addSeries("End of no lane change to right");
                int beginLeftKey = data.addSeries("Begin of no lane change to left");
                int endLeftKey = data.addSeries("End of no lane change to left");
                for (double speedDifference = startSpeedDifference; speedDifference <= endSpeedDifference; speedDifference +=
                        1)
                {
                    DoubleScalar.Rel<LengthUnit> criticalHeadway =
                            lcs.findDecisionPoint(LaneChangeGraph.lowerBound, midPoint, speed,
                                    new DoubleScalar.Rel<SpeedUnit>(speedDifference, SpeedUnit.KM_PER_HOUR),
                                    laneChangeModel, true);
                    if (null != criticalHeadway)
                    {
                        data.addXYPair(beginRightKey, speedDifference, criticalHeadway.getInUnit(LengthUnit.METER));
                    }
                    criticalHeadway =
                            lcs.findDecisionPoint(midPoint, LaneChangeGraph.upperBound, speed,
                                    new DoubleScalar.Rel<SpeedUnit>(speedDifference, SpeedUnit.KM_PER_HOUR),
                                    laneChangeModel, true);
                    if (null != criticalHeadway)
                    {
                        data.addXYPair(endRightKey, speedDifference, criticalHeadway.getInUnit(LengthUnit.METER));
                    }
                    criticalHeadway =
                            lcs.findDecisionPoint(LaneChangeGraph.lowerBound, midPoint, speed,
                                    new DoubleScalar.Rel<SpeedUnit>(speedDifference, SpeedUnit.KM_PER_HOUR),
                                    laneChangeModel, false);
                    if (null != criticalHeadway)
                    {
                        data.addXYPair(beginLeftKey, speedDifference, criticalHeadway.getInUnit(LengthUnit.METER));
                    }
                    else
                    {
                        lcs.findDecisionPoint(LaneChangeGraph.lowerBound, midPoint, speed,
                                new DoubleScalar.Rel<SpeedUnit>(speedDifference, SpeedUnit.KM_PER_HOUR),
                                laneChangeModel, false);
                    }
                    criticalHeadway =
                            lcs.findDecisionPoint(midPoint, LaneChangeGraph.upperBound, speed,
                                    new DoubleScalar.Rel<SpeedUnit>(speedDifference, SpeedUnit.KM_PER_HOUR),
                                    laneChangeModel, false);
                    if (null != criticalHeadway)
                    {
                        data.addXYPair(endLeftKey, speedDifference, criticalHeadway.getInUnit(LengthUnit.METER));
                    }
                    Plot plot = lcs.charts[row][index].getChart().getPlot();
                    plot.notifyListeners(new PlotChangeEvent(plot));
                }
            }
        }

    }

    /**
     * Then execution start point.
     * @param args String[]; the command line arguments (not used)
     * @throws NamingException
     * @throws RemoteException
     * @throws NetworkException
     * @throws SimRuntimeException
     * @throws GTUException
     */
    public static void buildGUI(String[] args) throws RemoteException, NamingException, NetworkException,
            SimRuntimeException, GTUException
    {
        JPanel mainPanel = new JPanel(new BorderLayout());
        lcs = new LaneChangeGraph("Lane change graphs", mainPanel);
        TablePanel chartsPanel = new TablePanel(standardSpeeds.length, 2);
        mainPanel.add(chartsPanel, BorderLayout.CENTER);
        for (int index = 0; index < standardSpeeds.length; index++)
        {
            lcs.charts[0][index] =
                    new ChartPanel(lcs.createChart(
                            String.format("Egoistic reference car at %.0fkm/h", standardSpeeds[index]),
                            standardSpeeds[index]));
            chartsPanel.setCell(lcs.charts[0][index], index, 0);
        }
        for (int index = 0; index < standardSpeeds.length; index++)
        {
            lcs.charts[1][index] =
                    new ChartPanel(lcs.createChart(
                            String.format("Altruistic reference car at %.0fkm/h", standardSpeeds[index]),
                            standardSpeeds[index]));
            chartsPanel.setCell(lcs.charts[1][index], index, 1);
        }
        lcs.pack();
        lcs.setExtendedState(Frame.MAXIMIZED_BOTH);
        lcs.setVisible(true);
    }

    /**
     * Find the headway at which the decision to merge right changes.
     * @param low minimum headway to consider
     * @param high maximum headway to consider
     * @param referenceSpeed DoubleScalar.Abs&lt;SpeedUnit&gt;; speed of the reference car
     * @param speedDifference DoubleScalar.Rel&lt;SpeedUnit&gt;; speed of the other car minus speed of the reference car
     * @param laneChangeModel LaneChangeModel; the lane change model to apply
     * @param mergeRight boolean; if true; merge right is tested; if false; merge left is tested
     * @return DoubleScalar.Rel&lt;LengthUnit&gt;
     * @throws RemoteException
     * @throws NamingException
     * @throws NetworkException
     * @throws GTUException
     */
    private DoubleScalar.Rel<LengthUnit> findDecisionPoint(DoubleScalar.Rel<LengthUnit> low,
            DoubleScalar.Rel<LengthUnit> high, DoubleScalar.Abs<SpeedUnit> referenceSpeed,
            DoubleScalar.Rel<SpeedUnit> speedDifference, LaneChangeModel laneChangeModel, boolean mergeRight)
            throws RemoteException, NamingException, NetworkException, SimRuntimeException, GTUException
    {
        // Set up the network
        GTUType<String> gtuType = GTUType.makeGTUType("car");
        LaneType<String> laneType = new LaneType<String>("CarLane");
        laneType.addPermeability(gtuType);
        final DoubleScalar.Abs<SpeedUnit> speedLimit = new DoubleScalar.Abs<SpeedUnit>(120, SpeedUnit.KM_PER_HOUR);

        Lane lanes[] =
                LaneFactory.makeMultiLane("Road with two lanes",
                        new NodeGeotools.STR("From", new Coordinate(lowerBound.getSI(), 0, 0)), new NodeGeotools.STR(
                                "To", new Coordinate(upperBound.getSI(), 0, 0)), null, 2, laneType, speedLimit, null);
        // Create the reference vehicle
        Map<Lane, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions =
                new HashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
        initialLongitudinalPositions.put(lanes[mergeRight ? 0 : 1], new DoubleScalar.Rel<LengthUnit>(0,
                LengthUnit.METER));
        // The reference car only needs a simulator
        // But that needs a model (which this class implements)
        SimpleSimulator simpleSimulator =
                new SimpleSimulator(new DoubleScalar.Abs<TimeUnit>(0.0, TimeUnit.SECOND),
                        new DoubleScalar.Rel<TimeUnit>(0.0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(3600.0,
                                TimeUnit.SECOND), this);
        this.carFollowingModel =
                new IDMPlus(new DoubleScalar.Abs<AccelerationUnit>(1, AccelerationUnit.METER_PER_SECOND_2),
                        new DoubleScalar.Abs<AccelerationUnit>(1.5, AccelerationUnit.METER_PER_SECOND_2),
                        new DoubleScalar.Rel<LengthUnit>(2, LengthUnit.METER), new DoubleScalar.Rel<TimeUnit>(1,
                                TimeUnit.SECOND), 1d);
        this.carFollowingModel =
                new IDM(new DoubleScalar.Abs<AccelerationUnit>(1, AccelerationUnit.METER_PER_SECOND_2),
                        new DoubleScalar.Abs<AccelerationUnit>(1.5, AccelerationUnit.METER_PER_SECOND_2),
                        new DoubleScalar.Rel<LengthUnit>(2, LengthUnit.METER), new DoubleScalar.Rel<TimeUnit>(1,
                                TimeUnit.SECOND), 1d);

        LaneBasedIndividualCar<String> referenceCar =
                new LaneBasedIndividualCar<String>("ReferenceCar", gtuType, this.carFollowingModel, laneChangeModel,
                        initialLongitudinalPositions, referenceSpeed, new DoubleScalar.Rel<LengthUnit>(4,
                                LengthUnit.METER), new DoubleScalar.Rel<LengthUnit>(2, LengthUnit.METER),
                        new DoubleScalar.Abs<SpeedUnit>(150, SpeedUnit.KM_PER_HOUR), new Route(
                                new ArrayList<Node<?, ?>>()),
                        (OTSDEVSSimulatorInterface) simpleSimulator.getSimulator());
        Collection<HeadwayGTU> sameLaneGTUs = new HashSet<HeadwayGTU>();
        sameLaneGTUs.add(new HeadwayGTU(referenceCar, 0));
        // TODO play with the speed limit
        // TODO play with the preferredLaneRouteIncentive
        LaneMovementStep lowResult =
                computeLaneChange(referenceCar, sameLaneGTUs, speedLimit, laneChangeModel, low, lanes[1],
                        speedDifference, mergeRight);
        LaneMovementStep highResult =
                computeLaneChange(referenceCar, sameLaneGTUs, speedLimit, laneChangeModel, high, lanes[1],
                        speedDifference, mergeRight);
        DoubleScalar.Rel<LengthUnit> mid = null;
        if (lowResult.getLaneChange() != highResult.getLaneChange())
        {
            // Use bisection to home in onto the decision point
            final double delta = 0.1; // [m]
            final int stepsNeeded =
                    (int) Math.ceil(Math.log(DoubleScalar.minus(high, low).getSI() / delta) / Math.log(2));
            for (int step = 0; step < stepsNeeded; step++)
            {
                MutableDoubleScalar.Rel<LengthUnit> mutableMid = DoubleScalar.plus(low, high);
                mutableMid.divide(2);
                mid = mutableMid.immutable();
                LaneMovementStep midResult =
                        computeLaneChange(referenceCar, sameLaneGTUs, speedLimit, laneChangeModel, mid, lanes[1],
                                speedDifference, mergeRight);
                // System.out.println(String.format ("mid %.2fm: %s", mid.getSI(), midResult));
                if (midResult.getLaneChange() != lowResult.getLaneChange())
                {
                    high = mid;
                    highResult = midResult;
                }
                else
                {
                    low = mid;
                    lowResult = midResult;
                }
            }
        }
        else
        {
            // System.out.println("Bisection failed");
            computeLaneChange(referenceCar, sameLaneGTUs, speedLimit, laneChangeModel, low, lanes[1], speedDifference,
                    mergeRight);
        }
        return mid;
    }

    /**
     * @param referenceCar
     * @param sameLaneGTUs
     * @param speedLimit
     * @param laneChangeModel
     * @param otherCarPosition
     * @param otherCarLane
     * @param deltaV
     * @param mergeRight
     * @return
     * @throws RemoteException
     * @throws NamingException
     * @throws SimRuntimeException
     * @throws NetworkException
     * @throws GTUException
     */
    private LaneMovementStep computeLaneChange(LaneBasedIndividualCar<String> referenceCar,
            Collection<HeadwayGTU> sameLaneGTUs, DoubleScalar.Abs<SpeedUnit> speedLimit,
            LaneChangeModel laneChangeModel, DoubleScalar.Rel<LengthUnit> otherCarPosition, Lane otherCarLane,
            Rel<SpeedUnit> deltaV, boolean mergeRight) throws RemoteException, NamingException, NetworkException,
            SimRuntimeException, GTUException
    {
        Map<Lane, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions =
                new HashMap<Lane, DoubleScalar.Rel<LengthUnit>>();
        initialLongitudinalPositions.put(otherCarLane, otherCarPosition);
        LaneBasedIndividualCar<String> otherCar =
                new LaneBasedIndividualCar<String>("otherCar", referenceCar.getGTUType(), this.carFollowingModel,
                        laneChangeModel, initialLongitudinalPositions, DoubleScalar.plus(
                                referenceCar.getLongitudinalVelocity(), deltaV).immutable(),
                        new DoubleScalar.Rel<LengthUnit>(4, LengthUnit.METER), new DoubleScalar.Rel<LengthUnit>(2,
                                LengthUnit.METER), new DoubleScalar.Abs<SpeedUnit>(150, SpeedUnit.KM_PER_HOUR),
                        new Route(new ArrayList<Node<?, ?>>()), referenceCar.getSimulator());
        Collection<HeadwayGTU> preferredLaneGTUs = new HashSet<HeadwayGTU>();
        Collection<HeadwayGTU> nonPreferredLaneGTUs = new HashSet<HeadwayGTU>();
        DoubleScalar.Rel<LengthUnit> referenceCarPosition =
                referenceCar.position(referenceCar.positions(referenceCar.getReference()).keySet().iterator().next(),
                        referenceCar.getReference());
        HeadwayGTU otherGTU =
                new HeadwayGTU(otherCar, DoubleScalar.minus(otherCarPosition, referenceCarPosition).getSI());
        if (mergeRight)
        {
            preferredLaneGTUs.add(otherGTU);
        }
        else
        {
            sameLaneGTUs.add(otherGTU);
        }
        // System.out.println(referenceCar);
        // System.out.println(otherCar);
        LaneMovementStep result =
                laneChangeModel.computeLaneChangeAndAcceleration(referenceCar, sameLaneGTUs, mergeRight
                        ? preferredLaneGTUs : null, mergeRight ? null : nonPreferredLaneGTUs, speedLimit,
                        new DoubleScalar.Rel<AccelerationUnit>(0.3, AccelerationUnit.METER_PER_SECOND_2),
                        new DoubleScalar.Rel<AccelerationUnit>(0.1, AccelerationUnit.METER_PER_SECOND_2),
                        new DoubleScalar.Rel<AccelerationUnit>(-0.3, AccelerationUnit.METER_PER_SECOND_2));
        // System.out.println(result);
        sameLaneGTUs.remove(otherCar);
        otherCar.destroy();
        return result;
    }

    /**
     * @param caption String; the caption for the chart
     * @param speed double; the speed of the reference vehicle
     * @return
     */
    private JFreeChart createChart(String caption, double speed)
    {
        ChartFactory.setChartTheme(new StandardChartTheme("JFree/Shadow", false));
        ChartData chartData = new ChartData();
        JFreeChart chartPanel =
                ChartFactory.createXYLineChart(caption, "", "", chartData, PlotOrientation.VERTICAL, false, false,
                        false);
        NumberAxis xAxis = new NumberAxis("\u2192 " + "\u0394v (other car speed minus reference car speed) [km/h]");
        xAxis.setAutoRangeIncludesZero(true);
        double minimumDifference = -30;
        xAxis.setRange(minimumDifference, minimumDifference + 60);
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        NumberAxis yAxis = new NumberAxis("\u2192 " + "gross headway (\u0394s) [m]");
        yAxis.setAutoRangeIncludesZero(true);
        yAxis.setRange(lowerBound.getSI(), upperBound.getSI());
        yAxis.setInverted(true);
        chartPanel.getXYPlot().setDomainAxis(xAxis);
        chartPanel.getXYPlot().setRangeAxis(yAxis);
        final XYLineAndShapeRenderer renderer = (XYLineAndShapeRenderer) chartPanel.getXYPlot().getRenderer();
        renderer.setBaseLinesVisible(true);
        renderer.setBaseShapesVisible(false);
        renderer.setBaseShape(new Line2D.Float(0, 0, 0, 0));
        return chartPanel;
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel(SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> simulator)
            throws SimRuntimeException, RemoteException
    {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> getSimulator() throws RemoteException
    {
        return null;
    }

}

/** */
class ChartData implements XYDataset
{

    /** The X values. */
    ArrayList<ArrayList<Double>> xValues = new ArrayList<ArrayList<Double>>();

    /** The Y values. */
    ArrayList<ArrayList<Double>> yValues = new ArrayList<ArrayList<Double>>();

    /** The names of the series. */
    ArrayList<String> seriesKeys = new ArrayList<String>();

    /** List of parties interested in changes of this ContourPlot. */
    private transient EventListenerList listenerList = new EventListenerList();

    /** Not used internally. */
    private DatasetGroup datasetGroup = null;

    /**
     * Add storage for another series of XY values.
     * @param seriesName String; the name of the new series
     * @return int; the index to use to address the new series
     */
    public int addSeries(String seriesName)
    {
        this.xValues.add(new ArrayList<Double>());
        this.yValues.add(new ArrayList<Double>());
        this.seriesKeys.add(seriesName);
        return this.xValues.size() - 1;
    }

    /**
     * Add an XY pair to the data
     * @param seriesKey int; key to the data series
     * @param x double; x value of the pair
     * @param y double; y value of the pair
     */
    public void addXYPair(int seriesKey, double x, double y)
    {
        this.xValues.get(seriesKey).add(x);
        this.yValues.get(seriesKey).add(y);
    }

    /** {@inheritDoc} */
    @Override
    public int getSeriesCount()
    {
        return this.seriesKeys.size();
    }

    /** {@inheritDoc} */
    @Override
    public Comparable<?> getSeriesKey(int series)
    {
        return this.seriesKeys.get(series);
    }

    /** {@inheritDoc} */
    @Override
    public int indexOf(@SuppressWarnings("rawtypes") Comparable seriesKey)
    {
        return this.seriesKeys.indexOf(seriesKey);
    }

    /** {@inheritDoc} */
    @Override
    public void addChangeListener(DatasetChangeListener listener)
    {
        this.listenerList.add(DatasetChangeListener.class, listener);
    }

    /** {@inheritDoc} */
    @Override
    public void removeChangeListener(DatasetChangeListener listener)
    {
        this.listenerList.remove(DatasetChangeListener.class, listener);
    }

    /** {@inheritDoc} */
    @Override
    public DatasetGroup getGroup()
    {
        return this.datasetGroup;
    }

    /** {@inheritDoc} */
    @Override
    public void setGroup(DatasetGroup group)
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
    public int getItemCount(int series)
    {
        return this.xValues.get(series).size();
    }

    /** {@inheritDoc} */
    @Override
    public Number getX(int series, int item)
    {
        return this.xValues.get(series).get(item);
    }

    /** {@inheritDoc} */
    @Override
    public double getXValue(int series, int item)
    {
        return this.xValues.get(series).get(item);
    }

    /** {@inheritDoc} */
    @Override
    public Number getY(int series, int item)
    {
        return this.yValues.get(series).get(item);
    }

    /** {@inheritDoc} */
    @Override
    public double getYValue(int series, int item)
    {
        return this.yValues.get(series).get(item);
    }

}
