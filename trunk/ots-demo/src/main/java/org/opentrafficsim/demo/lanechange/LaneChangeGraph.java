package org.opentrafficsim.demo.lanechange;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.geom.Line2D;
import java.lang.reflect.InvocationTargetException;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.naming.NamingException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.event.EventListenerList;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.gui.swing.TablePanel;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.djunits.unit.TimeUnit;
import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.DoubleScalar.Abs;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
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
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.perception.Headway;
import org.opentrafficsim.road.gtu.lane.perception.HeadwayGTUSimple;
import org.opentrafficsim.road.gtu.lane.perception.LanePerceptionFull;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedCFLCTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.IDMPlusOld;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.Altruistic;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.Egoistic;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.LaneChangeModel;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.LaneMovementStep;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.simulationengine.SimpleSimulator;

/**
 * Create a plot that characterizes a lane change graph.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$,
 * initial version 18 nov. 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneChangeGraph extends JFrame implements OTSModelInterface, UNITS
{
    /** */
    private static final long serialVersionUID = 20141118L;

    /** Standard speed values in km/h. */
    static final double[] STANDARDSPEEDS = {30, 50, 80, 100, 120};

    /** The car following model. */
    private GTUFollowingModelOld carFollowingModel;

    /** The graphs. */
    private ChartPanel[][] charts;

    /** Start of two lane road. */
    private static final Length LOWERBOUND = new Length(-500, METER);

    /** Position of reference vehicle on the two lane road. */
    private static final Length MIDPOINT = new Length(0, METER);

    /** End of two lane road. */
    private static final Length UPPERBOUND = new Length(500, METER);

    /** The JFrame with the lane change graphs. */
    private static LaneChangeGraph lcs;

    /** The network. */
    private OTSNetwork network = new OTSNetwork("network");

    /**
     * Create a Lane Change Graph.
     * @param title String; title text of the window
     * @param mainPanel JPanel; panel that will (indirectly?) contain the charts
     */
    LaneChangeGraph(final String title, final JPanel mainPanel)
    {
        super(title);
        setContentPane(mainPanel);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.charts = new ChartPanel[2][STANDARDSPEEDS.length];
    }

    /**
     * Main entry point; now Swing thread safe (I hope).
     * @param args String[]; the command line arguments (not used)
     * @throws GTUException on error during GTU construction
     * @throws SimRuntimeException on ???
     * @throws NetworkException on network inconsistency
     * @throws NamingException on ???
     * @throws OTSGeometryException
     * @throws ParameterException in case of a parameter problem.
     */
    public static void main(final String[] args) throws NamingException, NetworkException, SimRuntimeException,
        GTUException, OTSGeometryException, ParameterException
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
                    catch (NamingException | NetworkException | SimRuntimeException | GTUException exception)
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
            for (int index = 0; index < STANDARDSPEEDS.length; index++)
            {
                Speed speed = new Speed(STANDARDSPEEDS[index], KM_PER_HOUR);
                // System.out.println("speed " + speed);
                double startSpeedDifference = -30; // standardSpeeds[index];
                double endSpeedDifference = startSpeedDifference + 60; // 150;
                ChartData data = (ChartData) lcs.charts[row][index].getChart().getXYPlot().getDataset();
                int beginRightKey = data.addSeries("Begin of no lane change to right");
                int endRightKey = data.addSeries("End of no lane change to right");
                int beginLeftKey = data.addSeries("Begin of no lane change to left");
                int endLeftKey = data.addSeries("End of no lane change to left");
                for (double speedDifference = startSpeedDifference; speedDifference <= endSpeedDifference; speedDifference +=
                    1)
                {
                    Length criticalHeadway =
                        lcs.findDecisionPoint(LaneChangeGraph.LOWERBOUND, MIDPOINT, speed, new Speed(speedDifference,
                            KM_PER_HOUR), laneChangeModel, true);
                    if (null != criticalHeadway)
                    {
                        data.addXYPair(beginRightKey, speedDifference, criticalHeadway.getInUnit(METER));
                    }
                    criticalHeadway =
                        lcs.findDecisionPoint(MIDPOINT, LaneChangeGraph.UPPERBOUND, speed, new Speed(speedDifference,
                            KM_PER_HOUR), laneChangeModel, true);
                    if (null != criticalHeadway)
                    {
                        data.addXYPair(endRightKey, speedDifference, criticalHeadway.getInUnit(METER));
                    }
                    criticalHeadway =
                        lcs.findDecisionPoint(LaneChangeGraph.LOWERBOUND, MIDPOINT, speed, new Speed(speedDifference,
                            KM_PER_HOUR), laneChangeModel, false);
                    if (null != criticalHeadway)
                    {
                        data.addXYPair(beginLeftKey, speedDifference, criticalHeadway.getInUnit(METER));
                    }
                    else
                    {
                        lcs.findDecisionPoint(LaneChangeGraph.LOWERBOUND, MIDPOINT, speed, new Speed(speedDifference,
                            KM_PER_HOUR), laneChangeModel, false);
                    }
                    criticalHeadway =
                        lcs.findDecisionPoint(MIDPOINT, LaneChangeGraph.UPPERBOUND, speed, new Speed(speedDifference,
                            KM_PER_HOUR), laneChangeModel, false);
                    if (null != criticalHeadway)
                    {
                        data.addXYPair(endLeftKey, speedDifference, criticalHeadway.getInUnit(METER));
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
     * @throws NamingException on ???
     * @throws NetworkException on network inconsistency
     * @throws SimRuntimeException on ???
     * @throws GTUException on error during GTU construction
     */
    public static void buildGUI(final String[] args) throws NamingException, NetworkException, SimRuntimeException,
        GTUException
    {
        JPanel mainPanel = new JPanel(new BorderLayout());
        lcs = new LaneChangeGraph("Lane change graphs", mainPanel);
        TablePanel chartsPanel = new TablePanel(STANDARDSPEEDS.length, 2);
        mainPanel.add(chartsPanel, BorderLayout.CENTER);
        for (int index = 0; index < STANDARDSPEEDS.length; index++)
        {
            lcs.charts[0][index] =
                new ChartPanel(lcs.createChart(String.format("Egoistic reference car at %.0fkm/h", STANDARDSPEEDS[index]),
                    STANDARDSPEEDS[index]));
            chartsPanel.setCell(lcs.charts[0][index], index, 0);
        }
        for (int index = 0; index < STANDARDSPEEDS.length; index++)
        {
            lcs.charts[1][index] =
                new ChartPanel(lcs.createChart(String.format("Altruistic reference car at %.0fkm/h", STANDARDSPEEDS[index]),
                    STANDARDSPEEDS[index]));
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
     * @param referenceSpeed Speed; speed of the reference car
     * @param speedDifference Speed; speed of the other car minus speed of the reference car
     * @param laneChangeModel LaneChangeModel; the lane change model to apply
     * @param mergeRight boolean; if true; merge right is tested; if false; merge left is tested
     * @return Length
     * @throws NamingException on ???
     * @throws NetworkException on network inconsistency
     * @throws SimRuntimeException on ???
     * @throws GTUException on error during GTU construction
     * @throws OTSGeometryException
     * @throws ParameterException in case of a parameter problem.
     */
    private Length findDecisionPoint(Length low, Length high, final Speed referenceSpeed, final Speed speedDifference,
        final LaneChangeModel laneChangeModel, final boolean mergeRight) throws NamingException, NetworkException,
        SimRuntimeException, GTUException, OTSGeometryException, ParameterException
    {
        // Set up the network
        GTUType gtuType = new GTUType("car");
        Set<GTUType> compatibility = new HashSet<GTUType>();
        compatibility.add(gtuType);
        LaneType laneType = new LaneType("CarLane", compatibility);
        final Speed speedLimit = new Speed(120, KM_PER_HOUR);

        Lane[] lanes =
            LaneFactory.makeMultiLane("Road with two lanes", new OTSNode("From", new OTSPoint3D(LOWERBOUND.getSI(), 0, 0)),
                new OTSNode("To", new OTSPoint3D(UPPERBOUND.getSI(), 0, 0)), null, 2, laneType, speedLimit, null,
                LongitudinalDirectionality.DIR_PLUS);
        // Create the reference vehicle
        Set<DirectedLanePosition> initialLongitudinalPositions = new LinkedHashSet<>(1);
        initialLongitudinalPositions.add(new DirectedLanePosition(lanes[mergeRight ? 0 : 1], new Length(0, METER),
            GTUDirectionality.DIR_PLUS));

        // The reference car only needs a simulator
        // But that needs a model (which this class implements)
        SimpleSimulator simpleSimulator =
            new SimpleSimulator(new Time(0.0, SECOND), new Duration(0.0, SECOND), new Duration(3600.0, SECOND), this);
        this.carFollowingModel =
            new IDMPlusOld(new Acceleration(1, METER_PER_SECOND_2), new Acceleration(1.5, METER_PER_SECOND_2), new Length(2,
                METER), new Duration(1, SECOND), 1d);
        this.carFollowingModel =
            new IDMOld(new Acceleration(1, METER_PER_SECOND_2), new Acceleration(1.5, METER_PER_SECOND_2), new Length(2,
                METER), new Duration(1, SECOND), 1d);

        BehavioralCharacteristics behavioralCharacteristics = new BehavioralCharacteristics();
        // LaneBasedBehavioralCharacteristics drivingCharacteristics =
        // new LaneBasedBehavioralCharacteristics(this.carFollowingModel, laneChangeModel);
        LaneBasedStrategicalPlanner strategicalPlanner =
            new LaneBasedStrategicalRoutePlanner(behavioralCharacteristics, new LaneBasedCFLCTacticalPlanner(
                this.carFollowingModel, laneChangeModel));
        LaneBasedIndividualGTU referenceCar =
            new LaneBasedIndividualGTU("ReferenceCar", gtuType, initialLongitudinalPositions, referenceSpeed, new Length(4,
                METER), new Length(2, METER), new Speed(150, KM_PER_HOUR), simpleSimulator, strategicalPlanner,
                new LanePerceptionFull(), this.network);
        Collection<Headway> sameLaneGTUs = new LinkedHashSet<>();
        sameLaneGTUs.add(new HeadwayGTUSimple(referenceCar.getId(), referenceCar.getGTUType(), Length.ZERO, referenceCar
            .getLength(), referenceCar.getSpeed(), null));
        // TODO play with the speed limit
        // TODO play with the preferredLaneRouteIncentive
        LaneMovementStep lowResult =
            computeLaneChange(referenceCar, sameLaneGTUs, speedLimit, laneChangeModel, low, lanes[1], speedDifference,
                mergeRight);
        LaneMovementStep highResult =
            computeLaneChange(referenceCar, sameLaneGTUs, speedLimit, laneChangeModel, high, lanes[1], speedDifference,
                mergeRight);
        Length mid = null;
        if (lowResult.getLaneChange() != highResult.getLaneChange())
        {
            // Use bisection to home in onto the decision point
            final double delta = 0.1; // [m]
            final int stepsNeeded = (int) Math.ceil(Math.log(DoubleScalar.minus(high, low).getSI() / delta) / Math.log(2));
            for (int step = 0; step < stepsNeeded; step++)
            {
                Length mutableMid = low.plus(high).divideBy(2);
                mid = mutableMid;
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
     * @param referenceCar LaneBasedIndifidualCar&lt;String&gt;; the reference GTU
     * @param sameLaneGTUs Collection&lt;HeadwayGTU&gt;; the set of GTUs in the same lane as the <cite>referenceCar</cite>
     * @param speedLimit Speed; the speed limit
     * @param laneChangeModel LaneChangeModel; the lane change model
     * @param otherCarPosition Length; the position of the other car
     * @param otherCarLane Lane; the lane of the other car
     * @param deltaV Speed; the speed difference
     * @param mergeRight boolean; if true; merging direction is to the right; if false; merging direction is to the left
     * @return LaneMovementStep
     * @throws NamingException on ???
     * @throws SimRuntimeException on ???
     * @throws NetworkException on network inconsistency
     * @throws GTUException on error during GTU construction
     * @throws OTSGeometryException when the initial position is outside the lane's center line
     * @throws ParameterException in case of a parameter problem.
     */
    private LaneMovementStep computeLaneChange(final LaneBasedIndividualGTU referenceCar,
        final Collection<Headway> sameLaneGTUs, final Speed speedLimit, final LaneChangeModel laneChangeModel,
        final Length otherCarPosition, final Lane otherCarLane, final Speed deltaV, final boolean mergeRight)
        throws NamingException, NetworkException, SimRuntimeException, GTUException, OTSGeometryException,
        ParameterException
    {
        Set<DirectedLanePosition> initialLongitudinalPositions = new LinkedHashSet<>(1);
        initialLongitudinalPositions
            .add(new DirectedLanePosition(otherCarLane, otherCarPosition, GTUDirectionality.DIR_PLUS));
        BehavioralCharacteristics behavioralCharacteristics = new BehavioralCharacteristics();
        // LaneBasedBehavioralCharacteristics drivingCharacteristics =
        // new LaneBasedBehavioralCharacteristics(this.carFollowingModel, laneChangeModel);
        LaneBasedStrategicalPlanner strategicalPlanner =
            new LaneBasedStrategicalRoutePlanner(behavioralCharacteristics, new LaneBasedCFLCTacticalPlanner(
                this.carFollowingModel, laneChangeModel));
        LaneBasedIndividualGTU otherCar =
            new LaneBasedIndividualGTU("otherCar", referenceCar.getGTUType(), initialLongitudinalPositions, referenceCar
                .getSpeed().plus(deltaV), new Length(4, METER), new Length(2, METER), new Speed(150, KM_PER_HOUR),
                referenceCar.getSimulator(), strategicalPlanner, new LanePerceptionFull(), this.network);
        Collection<Headway> preferredLaneGTUs = new LinkedHashSet<>();
        Collection<Headway> nonPreferredLaneGTUs = new LinkedHashSet<>();
        Length referenceCarPosition =
            referenceCar.position(referenceCar.positions(referenceCar.getReference()).keySet().iterator().next(),
                referenceCar.getReference());
        Headway otherHeadwayGTU =
            new HeadwayGTUSimple(otherCar.getId(), otherCar.getGTUType(), otherCarPosition.minus(referenceCarPosition),
                otherCar.getLength(), otherCar.getSpeed(), null);
        if (mergeRight)
        {
            preferredLaneGTUs.add(otherHeadwayGTU);
        }
        else
        {
            sameLaneGTUs.add(otherHeadwayGTU);
        }
        // System.out.println(referenceCar);
        // System.out.println(otherCar);
        LaneMovementStep result =
            laneChangeModel.computeLaneChangeAndAcceleration(referenceCar, sameLaneGTUs, mergeRight ? preferredLaneGTUs
                : null, mergeRight ? null : nonPreferredLaneGTUs, speedLimit, new Acceleration(0.3, METER_PER_SECOND_2),
                new Acceleration(0.1, METER_PER_SECOND_2), new Acceleration(-0.3, METER_PER_SECOND_2));
        // System.out.println(result);
        sameLaneGTUs.remove(otherHeadwayGTU);
        otherCar.destroy();
        return result;
    }

    /**
     * @param caption String; the caption for the chart
     * @param speed double; the speed of the reference vehicle
     * @return new JFreeChart
     */
    private JFreeChart createChart(final String caption, final double speed)
    {
        ChartFactory.setChartTheme(new StandardChartTheme("JFree/Shadow", false));
        ChartData chartData = new ChartData();
        JFreeChart chartPanel =
            ChartFactory.createXYLineChart(caption, "", "", chartData, PlotOrientation.VERTICAL, false, false, false);
        NumberAxis xAxis = new NumberAxis("\u2192 " + "\u0394v (other car speed minus reference car speed) [km/h]");
        xAxis.setAutoRangeIncludesZero(true);
        double minimumDifference = -30;
        xAxis.setRange(minimumDifference, minimumDifference + 60);
        xAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());
        NumberAxis yAxis = new NumberAxis("\u2192 " + "gross headway (\u0394s) [m]");
        yAxis.setAutoRangeIncludesZero(true);
        yAxis.setRange(LOWERBOUND.getSI(), UPPERBOUND.getSI());
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
    public void constructModel(
        final SimulatorInterface<Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> simulator)
        throws SimRuntimeException, RemoteException
    {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public final SimulatorInterface<Abs<TimeUnit>, DoubleScalar.Rel<TimeUnit>, OTSSimTimeDouble> getSimulator()
        throws RemoteException
    {
        return null;
    }

}

/** */
class ChartData implements XYDataset
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
    public final DomainOrder getDomainOrder()
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
