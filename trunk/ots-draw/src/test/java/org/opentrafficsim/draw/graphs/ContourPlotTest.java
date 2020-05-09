package org.opentrafficsim.draw.graphs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.naming.NamingException;
import javax.swing.JOptionPane;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.unit.util.UNITS;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.immutablecollections.ImmutableArrayList;
import org.jfree.data.DomainOrder;
import org.junit.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opentrafficsim.core.dsol.OTSSimulator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSGeometryException;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.draw.graphs.GraphPath.Section;
import org.opentrafficsim.draw.graphs.road.GraphLaneUtil;
import org.opentrafficsim.kpi.interfaces.LaneDataInterface;
import org.opentrafficsim.kpi.sampling.KpiLaneDirection;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedCFLCTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.FixedAccelerationModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;
import org.opentrafficsim.road.gtu.lane.tactical.following.SequentialFixedAccelerationModel;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.Egoistic;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.LaneChangeModel;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.network.OTSRoadNetwork;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneDirection;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.OTSRoadNode;
import org.opentrafficsim.road.network.sampling.RoadSampler;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.simtime.SimTimeDoubleUnit;

/**
 * Test the non-GUI part of the ContourPlot class.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version Aug 21, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class ContourPlotTest implements UNITS
{

    /** Mocked GraphPath. */
    @SuppressWarnings("unchecked")
    GraphPath<KpiLaneDirection> mockedPath = Mockito.mock(GraphPath.class);

    Section<KpiLaneDirection> section0 = Mockito.mock(Section.class);

    Section<KpiLaneDirection> section1 = Mockito.mock(Section.class);

    KpiLaneDirection direction0 = Mockito.mock(KpiLaneDirection.class);

    KpiLaneDirection direction1 = Mockito.mock(KpiLaneDirection.class);

    LaneDataInterface mockedLane0 = Mockito.mock(LaneDataInterface.class);

    LaneDataInterface mockedLane1 = Mockito.mock(LaneDataInterface.class);

    RoadSampler mockedRoadSampler = Mockito.mock(RoadSampler.class);

    OTSSimulatorInterface mockedSimulator = Mockito.mock(OTSSimulatorInterface.class);

    SimEventInterface<SimTimeDoubleUnit> lastScheduledEvent = null;

    /**
     * Create a network and a path for the tests.
     * @param simulator OTSSimulatorInterface; the simulator
     * @param network OTSRoadNetwork; the network
     * @return GraphPath&lt;KpiLaneDirection&gt;; the dummy path
     * @throws Exception when something goes wrong (should not happen)
     */
    private GraphPath<KpiLaneDirection> dummyPath(final OTSSimulatorInterface simulator, final OTSRoadNetwork network)
            throws Exception
    {
        LaneType laneType = network.getLaneType(LaneType.DEFAULTS.TWO_WAY_LANE);
        OTSRoadNode b = new OTSRoadNode(network, "B", new OTSPoint3D(12345, 0, 0), Direction.ZERO);
        ArrayList<Lane> result = new ArrayList<Lane>();
        Lane[] lanes = LaneFactory.makeMultiLane(network, "AtoB",
                new OTSRoadNode(network, "A", new OTSPoint3D(1234, 0, 0), Direction.ZERO), b, null, 1, laneType,
                new Speed(100, KM_PER_HOUR), simulator);
        result.add(lanes[0]);
        // Make a continuation lane to prevent errors when the operational plan exceeds the available remaining length
        lanes = LaneFactory.makeMultiLane(network, "BtoC", b,
                new OTSRoadNode(network, "C", new OTSPoint3D(99999, 0, 0), Direction.ZERO), null, 1, laneType,
                new Speed(100, KM_PER_HOUR), null);
        return GraphLaneUtil.createPath("AtoB", new LaneDirection(lanes[0], GTUDirectionality.DIR_PLUS));
    }

    /**
     * Code common to all contour plot tests.
     * @throws SimRuntimeException if that happens uncaught; this test has failed
     */
    public final void setUp() throws SimRuntimeException
    {
        Mockito.when(this.mockedPath.getTotalLength()).thenReturn(Length.valueOf("2000m"));
        Mockito.when(this.mockedPath.getNumberOfSeries()).thenReturn(2);
        Mockito.when(this.mockedPath.get(0)).thenReturn(this.section0);
        Mockito.when(this.mockedPath.get(1)).thenReturn(this.section1);
        Mockito.when(this.mockedPath.getStartDistance(this.section0)).thenReturn(Length.ZERO);
        Mockito.when(this.mockedPath.getStartDistance(this.section1)).thenReturn(Length.valueOf("1234m"));
        Mockito.when(this.mockedPath.getSpeedLimit()).thenReturn(Speed.valueOf("100 km/h"));
        List<Section<KpiLaneDirection>> sectionList = new ArrayList<>();
        sectionList.add(this.section0);
        sectionList.add(this.section1);
        Mockito.when(this.mockedLane0.getLength()).thenReturn(Length.valueOf("1234m"));
        Mockito.when(this.mockedLane1.getLength()).thenReturn(Length.valueOf("766m"));
        Mockito.when(this.direction0.getLaneData()).thenReturn(this.mockedLane0);
        Mockito.when(this.direction1.getLaneData()).thenReturn(this.mockedLane1);
        Set<KpiLaneDirection> set0 = new HashSet<>();
        set0.add(this.direction0);
        Mockito.when(this.section0.iterator()).thenReturn(set0.iterator());
        Set<KpiLaneDirection> set1 = new HashSet<>();
        set1.add(this.direction1);
        Mockito.when(this.section0.iterator()).thenReturn(set0.iterator());
        Mockito.when(this.section1.iterator()).thenReturn(set1.iterator());
        Mockito.when(this.mockedPath.getSections()).thenReturn(new ImmutableArrayList<>(sectionList));
        Mockito.when(this.section0.getLength()).thenReturn(Length.valueOf("1234m"));
        Mockito.when(this.section1.getLength()).thenReturn(Length.valueOf("766m"));
        Mockito.when(this.mockedSimulator.scheduleEventAbs(ArgumentMatchers.any(Time.class), ArgumentMatchers.any(),
                ArgumentMatchers.any(), ArgumentMatchers.anyString(), ArgumentMatchers.isNull()))
                .thenAnswer(new Answer<SimEventInterface<SimTimeDoubleUnit>>()
                {
                    @Override
                    public SimEventInterface<SimTimeDoubleUnit> answer(final InvocationOnMock invocation) throws Throwable
                    {
                        ContourPlotTest.this.lastScheduledEvent = new SimEvent.TimeDoubleUnit(invocation.getArgument(0),
                                invocation.getArgument(1), invocation.getArgument(2), "update", null);
                        return ContourPlotTest.this.lastScheduledEvent;
                    }
                });
        Mockito.when(this.mockedSimulator.getSimulatorTime()).thenReturn(Time.ZERO);
    }

    /**
     * Test the ContourPlotAcceleration.
     * @throws Exception when something goes wrong (should not happen)
     */
    @Test
    public final void accelerationContourTest() throws Exception
    {
        setUp();
        ContourDataSource<?> dataPool = new ContourDataSource<>(this.mockedRoadSampler, this.mockedPath);
        ContourPlotAcceleration acp = new ContourPlotAcceleration("acceleration", this.mockedSimulator, dataPool);
        assertTrue("newly created AccelerationContourPlot should not be null", null != acp);
        assertEquals("SeriesKey should be \"acceleration\"", "acceleration", acp.getSeriesKey(0));
        standardContourTests(this.mockedSimulator, acp, this.mockedPath, Double.NaN, 0);
    }

    /**
     * Test the ContourPlotDensity.
     * @throws Exception when something goes wrong (should not happen)
     */
    @Test
    public final void densityContourTest() throws Exception
    {
        OTSRoadNetwork network =
                new OTSRoadNetwork("density contour test network", true, new OTSSimulator("Simulator for densityContourTest"));
        OTSSimulatorInterface simulator = new OTSSimulator("densityContourTest");
        GraphPath<KpiLaneDirection> path = dummyPath(simulator, network);
        RoadSampler sampler = new RoadSampler(simulator);
        ContourDataSource<?> dataPool = new ContourDataSource<>(sampler, path);
        ContourPlotDensity dcp = new ContourPlotDensity("Density", simulator, dataPool);
        assertTrue("newly created DensityContourPlot should not be null", null != dcp);
        assertEquals("SeriesKey should be \"density\"", "density", dcp.getSeriesKey(0));
        GTUType gtuType = network.getGtuType(GTUType.DEFAULTS.CAR);
        standardContourTests(simulator, dcp, path, 0, Double.NaN);
    }

    /**
     * Test the ContourPlotFlow.
     * @throws Exception when something goes wrong (should not happen)
     */
    @Test
    public final void flowContourTest() throws Exception
    {
        OTSRoadNetwork network =
                new OTSRoadNetwork("flow contour test network", true, new OTSSimulator("Simulator for densityContourTest"));
        OTSSimulatorInterface simulator = new OTSSimulator("flowContourTest");
        GraphPath<KpiLaneDirection> path = dummyPath(simulator, network);
        RoadSampler sampler = new RoadSampler(simulator);
        ContourDataSource<?> dataPool = new ContourDataSource<>(sampler, path);
        ContourPlotFlow fcp = new ContourPlotFlow("Density", simulator, dataPool);
        assertTrue("newly created DensityContourPlot should not be null", null != fcp);
        assertEquals("SeriesKey should be \"flow\"", "flow", fcp.getSeriesKey(0));
        GTUType gtuType = network.getGtuType(GTUType.DEFAULTS.CAR);
        standardContourTests(simulator, fcp, path, 0, Double.NaN);
    }

    /**
     * Test the SpeedContourPlot.
     * @throws Exception when something goes wrong (should not happen)
     */
    @Test
    public final void speedContourTest() throws Exception
    {
        OTSRoadNetwork network =
                new OTSRoadNetwork("flow contour test network", true, new OTSSimulator("Simulator for densityContourTest"));
        OTSSimulatorInterface simulator = new OTSSimulator("speedContourTest");
        GraphPath<KpiLaneDirection> path = dummyPath(simulator, network);
        RoadSampler sampler = new RoadSampler(simulator);
        ContourDataSource<?> dataPool = new ContourDataSource<>(sampler, path);
        ContourPlotSpeed scp = new ContourPlotSpeed("Density", simulator, dataPool);
        assertTrue("newly created DensityContourPlot should not be null", null != scp);
        assertEquals("SeriesKey should be \"speed\"", "speed", scp.getSeriesKey(0));
        GTUType gtuType = network.getGtuType(GTUType.DEFAULTS.CAR);
        standardContourTests(simulator, scp, path, Double.NaN, 50);
    }

    /**
     * Debugging method.
     * @param cp AbstractContourPlot&lt;?&gt;; a contour plot
     * @param fromX int; lower bound of the x coordinate to print (inclusive)
     * @param toX int; upper bound of the x coordinate to print (inclusive)
     * @param fromY int; lower bound of the y coordinate to print (inclusive)
     * @param toY int; upper bound of the y coordinate to print (inclusive)
     */
    static void printMatrix(final AbstractContourPlot<?> cp, final int fromX, final int toX, final int fromY, final int toY)
    {
        System.out.println("Contour plot data:");
        int maxItem = cp.getItemCount(0);
        for (int y = fromY; y <= toY; y++)
        {
            System.out.print(String.format("y=%3d ", y));
            for (int x = fromX; x <= toX; x++)
            {
                // Find the item with the requested x and y
                int item;
                for (item = 0; item < maxItem; item++)
                {
                    if (cp.getXValue(0, item) == x && cp.getYValue(0, item) == y)
                    {
                        break;
                    }
                }
                if (item < maxItem)
                {
                    System.out.print(String.format("%10.6f", cp.getZValue(0, item)));
                }
                else
                {
                    System.out.print(" -------- ");
                }
            }
            System.out.println("");
        }
        System.out.print("");
    }

    /**
     * Test various properties of a ContourPlot that has no observed data added.
     * @param simulator OTSSimulatorInterface; the simulator
     * @param cp AbstractContourPlot&lt;?&gt;; the ContourPlot to test
     * @param path GraphPath&lt;?&gt;; the path
     * @param expectedZValue double; the value that getZ and getZValue should return for a valid item when no car has passed
     * @param expectedZValueWithTraffic double; the value that getZ and getZValue should return a valid item where a car has
     *            traveled at constant speed of 50 km/h. Supply Double.NaN if the value varies but differs from the value
     *            expected when no car has passed
     * @throws Exception when something goes wrong (should not happen)
     */
    public static void standardContourTests(final OTSSimulatorInterface simulator, final AbstractContourPlot<?> cp,
            final GraphPath<?> path, final double expectedZValue, final double expectedZValueWithTraffic) throws Exception
    {
        assertEquals("seriesCount should be 1", 1, cp.getSeriesCount());
        assertEquals("domainOrder should be ASCENDING", DomainOrder.ASCENDING, cp.getDomainOrder());
        assertEquals("indexOf always returns 0", 0, cp.indexOf(0));
        assertEquals("indexOf always returns 0", 0, cp.indexOf("abc"));
        assertNull("getGroup always returns null", cp.getGroup());
        int xBins = cp.getDataPool().timeAxis.getBinCount();
        int yBins = cp.getDataPool().spaceAxis.getBinCount();
        int expectedXBins = (int) Math.ceil(((AbstractPlot.DEFAULT_INITIAL_UPPER_TIME_BOUND).getSI())
                / ContourDataSource.DEFAULT_TIME_GRANULARITIES[ContourDataSource.DEFAULT_TIME_GRANULARITY_INDEX]
                + (cp.getDataPool().timeAxis.isInterpolate() ? 1 : 0));
        assertEquals("Initial xBins should be " + expectedXBins, expectedXBins, xBins);
        int expectedYBins = (int) Math.ceil(path.getTotalLength().getSI()
                / ContourDataSource.DEFAULT_SPACE_GRANULARITIES[ContourDataSource.DEFAULT_SPACE_GRANULARITY_INDEX]
                + (cp.getDataPool().timeAxis.isInterpolate() ? 1 : 0));
        assertEquals("yBins should be " + expectedYBins, expectedYBins, yBins);
        int bins = cp.getItemCount(0);
        assertEquals("Total bin count is product of xBins * yBins", xBins * yBins, bins);
        String initialUpperTimeBoundString = AbstractPlot.DEFAULT_INITIAL_UPPER_TIME_BOUND.toString();
        // Vary the x granularity
        for (double timeGranularity : ContourDataSource.DEFAULT_TIME_GRANULARITIES)
        {
            cp.actionPerformed(new ActionEvent(timeGranularity, 0, "setTimeGranularity"));
            for (double distanceGranularity : ContourDataSource.DEFAULT_SPACE_GRANULARITIES)
            {
                cp.actionPerformed(new ActionEvent(distanceGranularity, 0, "setSpaceGranularity"));
                cp.notifyPlotChange();
                expectedXBins = (int) Math.ceil((AbstractPlot.DEFAULT_INITIAL_UPPER_TIME_BOUND.getSI()) / timeGranularity);
                xBins = cp.getDataPool().timeAxis.getBinCount();
                assertEquals("Modified xBins should be " + expectedXBins, expectedXBins, xBins);
                expectedYBins = (int) Math.ceil(path.get(0).getLength().getSI() / distanceGranularity);
                yBins = cp.getDataPool().spaceAxis.getBinCount();
                assertEquals("Modified yBins should be " + expectedYBins, expectedYBins, yBins);
                bins = cp.getItemCount(0);
                assertEquals("Total bin count is product of xBins * yBins", xBins * yBins, bins);
                for (int item = 0; item < bins; item++)
                {
                    double x = cp.getXValue(0, item);
                    assertTrue("X should be >= 0", x >= 0);
                    assertTrue("X should be <= " + initialUpperTimeBoundString,
                            x <= AbstractPlot.DEFAULT_INITIAL_UPPER_TIME_BOUND.getSI());
                    Number alternateX = cp.getX(0, item);
                    assertEquals("getXValue and getX should return things that have the same value", x,
                            alternateX.doubleValue(), 0.000001);
                    double y = cp.getYValue(0, item);
                    Number alternateY = cp.getY(0, item);
                    assertEquals("getYValue and getY should return things that have the same value", y,
                            alternateY.doubleValue(), 0.000001);
                    double z = cp.getZValue(0, item);
                    if (Double.isNaN(expectedZValue))
                    {
                        assertTrue("Z value should be NaN", Double.isNaN(z));
                    }
                    else
                    {
                        assertEquals("Z value should be " + expectedZValue, expectedZValue, z, 0.0001);
                    }
                    Number alternateZ = cp.getZ(0, item);
                    if (Double.isNaN(expectedZValue))
                    {
                        assertTrue("Alternate Z value should be NaN", Double.isNaN(alternateZ.doubleValue()));
                    }
                    else
                    {
                        assertEquals("Alternate Z value should be " + expectedZValue, expectedZValue, alternateZ.doubleValue(),
                                0.0000);
                    }
                }
                try
                {
                    cp.getXValue(0, -1);
                    fail("Should have thrown an Exception");
                }
                catch (RuntimeException e)
                {
                    // Ignore expected exception
                }
                try
                {
                    cp.getXValue(0, bins);
                    fail("Should have thrown an Exception");
                }
                catch (RuntimeException e)
                {
                    // Ignore expected exception
                }
                // try
                // {
                // cp.yAxisBin(-1);
                // fail("Should have thrown an Exception");
                // }
                // catch (RuntimeException e)
                // {
                // // Ignore expected exception
                // }
                // try
                // {
                // cp.yAxisBin(bins);
                // fail("Should have thrown an Exception");
                // }
                // catch (RuntimeException e)
                // {
                // // Ignore expected exception
                // }
            }
        }
        // Test some ActionEvents that ContourPlot can not handle
        try
        {
            cp.actionPerformed(new ActionEvent(cp, 0, "blabla"));
            fail("Should have thrown an Exception");
        }
        catch (RuntimeException e)
        {
            // Ignore expected exception
        }
        try
        {
            cp.actionPerformed(new ActionEvent(cp, 0, "setDistanceGranularity -1"));
            fail("Should have thrown an Exception");
        }
        catch (RuntimeException e)
        {
            // Ignore expected exception
        }
        try
        {
            cp.actionPerformed(new ActionEvent(cp, 0, "setDistanceGranularity abc"));
            fail("Should have thrown an Exception");
        }
        catch (RuntimeException e)
        {
            // Ignore expected exception
        }
        try
        {
            cp.actionPerformed(new ActionEvent(cp, 0, "setDistanceGranularitIE 10")); // typo in the event name
            fail("Should have thrown an Exception");
        }
        catch (RuntimeException e)
        {
            // Ignore expected exception
        }
        // Make the time granularity a bit more reasonable
        final double useTimeGranularity = 30; // [s]
        cp.actionPerformed(new ActionEvent(useTimeGranularity, 0, "setTimeGranularity"));
        final double useDistanceGranularity =
                ContourDataSource.DEFAULT_SPACE_GRANULARITIES[ContourDataSource.DEFAULT_SPACE_GRANULARITIES.length - 1];
        cp.actionPerformed(new ActionEvent(useDistanceGranularity, 0, "setDistanceGranularity"));
        cp.notifyPlotChange();
        bins = cp.getItemCount(0);
        Time initialTime = new Time(0, TimeUnit.BASE_SECOND);
        Length initialPosition = new Length(100, METER);
        Speed initialSpeed = new Speed(50, KM_PER_HOUR);
        // Create a car running 50 km.h
        SequentialFixedAccelerationModel gtuFollowingModel =
                new SequentialFixedAccelerationModel(simulator, new Acceleration(2.0, AccelerationUnit.METER_PER_SECOND_2));
        // Make the car run at constant speed for one minute
        gtuFollowingModel
                .addStep(new FixedAccelerationModel(new Acceleration(0, METER_PER_SECOND_2), new Duration(60, SECOND)));
        // Make the car run at constant speed for another minute
        gtuFollowingModel
                .addStep(new FixedAccelerationModel(new Acceleration(0, METER_PER_SECOND_2), new Duration(600, SECOND)));
        // Make the car run at constant speed for five more minutes
        gtuFollowingModel
                .addStep(new FixedAccelerationModel(new Acceleration(0, METER_PER_SECOND_2), new Duration(300, SECOND)));
        LaneChangeModel laneChangeModel = new Egoistic();

        // Check that the initial data in the graph contains no trace of any car.
        for (int item = 0; item < bins; item++)
        {
            double x = cp.getXValue(0, item);
            assertTrue("X should be >= 0", x >= 0);
            assertTrue("X should be <= " + initialUpperTimeBoundString, x <= AbstractPlot.DEFAULT_INITIAL_UPPER_TIME_BOUND.si);
            Number alternateX = cp.getX(0, item);
            assertEquals("getXValue and getX should return things that have the same value", x, alternateX.doubleValue(),
                    0.000001);
            double y = cp.getYValue(0, item);
            Number alternateY = cp.getY(0, item);
            assertEquals("getYValue and getY should return things that have the same value", y, alternateY.doubleValue(),
                    0.000001);
            double z = cp.getZValue(0, item);
            if (Double.isNaN(expectedZValue))
            {
                assertTrue("Z value should be NaN (got " + z + ")", Double.isNaN(z));
            }
            else
            {
                assertEquals("Z value should be " + expectedZValue, expectedZValue, z, 0.0001);
            }
            Number alternateZ = cp.getZ(0, item);
            if (Double.isNaN(expectedZValue))
            {
                assertTrue("Alternate Z value should be NaN", Double.isNaN(alternateZ.doubleValue()));
            }
            else
            {
                assertEquals("Alternate Z value should be " + expectedZValue, expectedZValue, alternateZ.doubleValue(), 0.0000);
            }
        }

        // LaneBasedIndividualGTU car =
        // makeReferenceCar("0", gtuType, ((CrossSectionLink) network.getLink("AtoB")).getLanes().get(0), initialPosition,
        // initialSpeed, simulator, null, laneChangeModel, network);
        // car.getParameters().setParameter(ParameterTypes.LOOKAHEAD, new Length(10, LengthUnit.KILOMETER));
        //
        // // System.out.println("Running simulator from " + simulator.getSimulatorTime() + " to "
        // // + gtuFollowingModel.timeAfterCompletionOfStep(0));
        // double stopTime = gtuFollowingModel.timeAfterCompletionOfStep(0).si;
        // simulator.runUpToAndIncluding(new Time(stopTime, TimeUnit.BASE_SECOND));
        // while (simulator.isStartingOrRunning())
        // {
        // try
        // {
        // Thread.sleep(10);
        // }
        // catch (InterruptedException ie)
        // {
        // ie = null; // ignore
        // }
        // }
        // // System.out.println("Simulator is now at " + simulator.getSimulatorTime());
        // // System.out.println("Car at start time " + car.getOperationalPlan().getStartTime() + " is at "
        // // + car.getPosition(car.getOperationalPlan().getStartTime()));
        // // System.out.println("At time " + simulator.getSimulator().getSimulatorTime() + " car is at " + car);
        // for (int item = 0; item < bins; item++)
        // {
        // double x = cp.getXValue(0, item);
        // assertTrue("X should be >= 0", x >= 0);
        // assertTrue("X should be <= " + initialUpperTimeBoundString, x <= AbstractPlot.DEFAULT_INITIAL_UPPER_TIME_BOUND.si);
        // Number alternateX = cp.getX(0, item);
        // assertEquals("getXValue and getX should return things that have the same value", x, alternateX.doubleValue(),
        // 0.000001);
        // double y = cp.getYValue(0, item);
        // Number alternateY = cp.getY(0, item);
        // assertEquals("getYValue and getY should return things that have the same value", y, alternateY.doubleValue(),
        // 0.000001);
        // double z = cp.getZValue(0, item);
        // // figure out if the car has traveled through this cell
        // // if (x >= 180)
        // // System.out.println(String.format("t=%.3f, x=%.3f z=%f, exp=%.3f, carLastEval=%s, carNextEval=%s", x, y, z,
        // // expectedZValue, car.getOperationalPlan().getStartTime().getSI(), car.getOperationalPlan().getEndTime()
        // // .getSI()));
        // boolean hit = false;
        // if (x + useTimeGranularity >= 0// car.getOperationalPlan().getStartTime().getSI()
        // && x < 60)// car.getOperationalPlan().getEndTime().getSI())
        // {
        // // the car MAY have contributed to this cell
        // Time cellStartTime =
        // new Time(Math.max(car.getOperationalPlan().getStartTime().getSI(), x), TimeUnit.BASE_SECOND);
        // Time cellEndTime = new Time(Math.min(car.getOperationalPlan().getEndTime().getSI(), x + useTimeGranularity),
        // TimeUnit.BASE_SECOND);
        // // System.out.println("cellStartTime=" + cellStartTime + ", cellEndTime=" + cellEndTime);
        // // The next if statement is the problem
        // // if (cellStartTime.lt(cellEndTime)
        // // && car.position(lane, car.getRear(), cellStartTime).getSI() <= y + useDistanceGranularity
        // // && car.position(lane, car.getRear(), cellEndTime).getSI() >= y)
        // double xAtCellStartTime = initialPosition.si + initialSpeed.si * cellStartTime.si;
        // double xAtCellEndTime = initialPosition.si + initialSpeed.si * cellEndTime.si;
        // if (xAtCellStartTime < y + useDistanceGranularity && xAtCellEndTime >= y)
        // {
        // hit = true;
        // }
        // }
        // // System.out.println(String.format(
        // // "hit=%s, t=%.3f, x=%.3f z=%f, exp=%.3f, carLastEval=%s, carNextEval=%s, simulatortime=%s", hit, x, y, z,
        // // expectedZValue, car.getOperationalPlan().getStartTime().getSI(), car.getOperationalPlan().getEndTime()
        // // .getSI(), car.getSimulator().getSimulatorTime()));
        // Number alternateZ = cp.getZ(0, item);
        // if (hit)
        // {
        // if (!Double.isNaN(expectedZValueWithTraffic))
        // {
        // if (Double.isNaN(z))
        // {
        // printMatrix(cp, 0, 10, 0, 10);
        // System.out.println("Oops - z is NaN, expected z value with traffic is " + expectedZValueWithTraffic);
        // }
        // assertEquals("Z value should be " + expectedZValueWithTraffic, expectedZValueWithTraffic, z, 0.0001);
        // assertEquals("Z value should be " + expectedZValueWithTraffic, expectedZValueWithTraffic,
        // alternateZ.doubleValue(), 0.0001);
        // }
        // else
        // {
        // if (Double.isNaN(expectedZValue))
        // { // FIXME looks wrong / PK
        // assertFalse("Z value should not be NaN", Double.isNaN(z));
        // }
        // }
        // }
        // else
        // {
        // if (Double.isNaN(expectedZValue))
        // {
        // // if (!Double.isNaN(z))
        // // {
        // // System.out.println("Oops");
        // // Time cellStartTime = new Time(x, SECOND);
        // // Time cellEndTime =
        // // new Time(Math.min(car.getOperationalPlan().getEndTime().getSI(), x + useTimeGranularity),
        // // SECOND);
        // // double xAtCellStartTime = initialPosition.si + initialSpeed.si * cellStartTime.si;
        // // double xAtCellEndTime = initialPosition.si + initialSpeed.si * cellEndTime.si;
        // // System.out.println("cellStartTime=" + cellStartTime + " cellEndTime=" + cellEndTime
        // // + " xAtCellStartTime=" + xAtCellStartTime + " xAtCellEndTime=" + xAtCellEndTime);
        // // double cellX = cp.getXValue(0, item);
        // // double cellY = cp.getYValue(0, item);
        // // double cellZ = cp.getZValue(0, item);
        // // System.out.println("cellX=" + cellX + " cellY=" + cellY + " cellZ=" + cellZ + " timeGranularity="
        // // + useTimeGranularity + " distanceGranularity=" + useDistanceGranularity);
        // // cp.getZValue(0, item);
        // // }
        // assertTrue("Z value should be NaN", Double.isNaN(z));
        // }
        // else
        // {
        // assertEquals("Z value should be " + expectedZValue, expectedZValue, z, 0.0001);
        // }
        // if (Double.isNaN(expectedZValue))
        // {
        // assertTrue("Alternate Z value should be NaN", Double.isNaN(alternateZ.doubleValue()));
        // }
        // else
        // {
        // assertEquals("Alternate Z value should be " + expectedZValue, expectedZValue, alternateZ.doubleValue(),
        // 0.0000);
        // }
        // }
        // }
        // // System.out.println("Running simulator from " + simulator.getSimulatorTime() + " to "
        // // + gtuFollowingModel.timeAfterCompletionOfStep(1));
        // stopTime = gtuFollowingModel.timeAfterCompletionOfStep(1).si;
        // simulator.runUpToAndIncluding(new Time(stopTime, TimeUnit.BASE_SECOND));
        // while (simulator.isStartingOrRunning())
        // {
        // try
        // {
        // Thread.sleep(10);
        // }
        // catch (InterruptedException ie)
        // {
        // ie = null; // ignore
        // }
        // }
        // // System.out.println("Simulator is now at " + simulator.getSimulatorTime());
        // // Check that the time range has expanded
        // xBins = cp.getDataPool().timeAxis.getBinCount();
        // bins = cp.getItemCount(0);
        // double observedHighestTime = Double.MIN_VALUE;
        // for (int bin = 0; bin < bins; bin++)
        // {
        // double xValue = cp.getXValue(0, bin);
        // if (xValue > observedHighestTime)
        // {
        // observedHighestTime = xValue;
        // }
        // }
        // double expectedHighestTime =
        // Math.floor((car.getSimulator().getSimulatorTime().si - 0.001) / useTimeGranularity) * useTimeGranularity;
        // assertEquals("Time range should run up to " + expectedHighestTime, expectedHighestTime, observedHighestTime, 0.0001);
        // Check the updateHint method in the PointerHandler
        // First get the panel that stores the result of updateHint (this is ugly)
        // JLabel hintPanel = null;
        // ChartPanel chartPanel = null;
        // for (Component c0 : cp.getComponents())
        // {
        // for (Component c1 : ((Container) c0).getComponents())
        // {
        // if (c1 instanceof Container)
        // {
        // for (Component c2 : ((Container) c1).getComponents())
        // {
        // // System.out.println("c2 is " + c2);
        // if (c2 instanceof Container)
        // {
        // for (Component c3 : ((Container) c2).getComponents())
        // {
        // // System.out.println("c3 is " + c3);
        // if (c3 instanceof JLabel)
        // {
        // if (null == hintPanel)
        // {
        // hintPanel = (JLabel) c3;
        // }
        // else
        // {
        // fail("There should be only one JPanel in a ContourPlot");
        // }
        // }
        // if (c3 instanceof ChartPanel)
        // {
        // if (null == chartPanel)
        // {
        // chartPanel = (ChartPanel) c3;
        // }
        // else
        // {
        // fail("There should be only one ChartPanel in a ContourPlot");
        // }
        // }
        // }
        // }
        // }
        // }
        // }
        // }
        // if (null == hintPanel)
        // {
        // fail("Could not find a JLabel in ContourPlot");
        // }
        // if (null == chartPanel)
        // {
        // fail("Could not find a ChartPanel in ContourPlot");
        // }
        // assertEquals("Initially the text should be a single space", " ", hintPanel.getText());
        // PointerHandler ph = null;
        // for (MouseListener ml : chartPanel.getMouseListeners())
        // {
        // if (ml instanceof PointerHandler)
        // {
        // if (null == ph)
        // {
        // ph = (PointerHandler) ml;
        // }
        // else
        // {
        // fail("There should be only one PointerHandler on the chartPanel");
        // }
        // }
        // }
        // if (null == ph)
        // {
        // fail("Could not find the PointerHandler for the chartPanel");
        // }
        // ph.updateHint(1, 2);
        // // System.out.println("Hint text is now " + hintPanel.getText());
        // assertFalse("Hint should not be a single space", " ".equals(hintPanel.getText()));
        // ph.updateHint(Double.NaN, Double.NaN);
        // assertEquals("The text should again be a single space", " ", hintPanel.getText());
    }

    /**
     * Create a new Car.
     * @param id String; the name (number) of the Car
     * @param gtuType GTUType; the type of the new car
     * @param lane Lane; the lane on which the new Car is positioned
     * @param initialPosition Length; the initial longitudinal position of the new Car
     * @param initialSpeed Speed; the initial speed
     * @param simulator OTSDEVVSimulator; the simulator that controls the new Car (and supplies the initial value for
     *            getLastEvalutionTime())
     * @param gtuFollowingModel GTUFollowingModel; the GTU following model
     * @param laneChangeModel LaneChangeModel; the lane change model
     * @param network the network
     * @return Car; the new Car
     * @throws NamingException on network error when making the animation
     * @throws NetworkException when the GTU cannot be placed on the given lane.
     * @throws SimRuntimeException when the move method cannot be scheduled.
     * @throws GTUException when construction of the GTU fails (probably due to an invalid parameter)
     * @throws OTSGeometryException when the initial path is wrong
     */
    private static LaneBasedIndividualGTU makeReferenceCar(final String id, final GTUType gtuType, final Lane lane,
            final Length initialPosition, final Speed initialSpeed, final OTSSimulatorInterface simulator,
            final GTUFollowingModelOld gtuFollowingModel, final LaneChangeModel laneChangeModel, final OTSRoadNetwork network)
            throws NamingException, NetworkException, SimRuntimeException, GTUException, OTSGeometryException
    {
        Length length = new Length(5.0, METER);
        Length width = new Length(2.0, METER);
        Set<DirectedLanePosition> initialLongitudinalPositions = new LinkedHashSet<>(1);
        initialLongitudinalPositions.add(new DirectedLanePosition(lane, initialPosition, GTUDirectionality.DIR_PLUS));
        Speed maxSpeed = new Speed(120, KM_PER_HOUR);
        LaneBasedIndividualGTU gtu =
                new LaneBasedIndividualGTU(id, gtuType, length, width, maxSpeed, length.times(0.5), simulator, network);
        LaneBasedStrategicalPlanner strategicalPlanner = new LaneBasedStrategicalRoutePlanner(
                new LaneBasedCFLCTacticalPlanner(gtuFollowingModel, laneChangeModel, gtu), gtu);
        gtu.init(strategicalPlanner, initialLongitudinalPositions, initialSpeed);

        return gtu;
    }

    /**
     * Run the DensityContourPlot stand-alone for profiling.
     * @param args String[]; the command line arguments (not used)
     * @throws Exception when something goes wrong (should not happen)
     */
    public static void main(final String[] args) throws Exception
    {
        ContourPlotTest cpt = new ContourPlotTest();
        System.out.println("Click the OK button");
        JOptionPane.showMessageDialog(null, "ContourPlot", "Start experiment", JOptionPane.INFORMATION_MESSAGE);
        System.out.println("Running ...");
        cpt.densityContourTest();
        System.out.println("Finished");
    }

}
