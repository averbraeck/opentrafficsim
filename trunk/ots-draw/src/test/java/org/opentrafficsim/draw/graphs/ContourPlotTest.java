package org.opentrafficsim.draw.graphs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import org.djunits.unit.AccelerationUnit;
import org.djunits.unit.LengthUnit;
import org.djunits.unit.TimeUnit;
import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.jfree.chart.ChartPanel;
import org.jfree.data.DomainOrder;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.network.Network;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.road.car.CarTest;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.tactical.following.FixedAccelerationModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.SequentialFixedAccelerationModel;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.Egoistic;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.LaneChangeModel;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.OTSRoadNode;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.model.inputparameters.InputParameterMap;
import nl.tudelft.simulation.dsol.model.outputstatistics.OutputStatistic;
import nl.tudelft.simulation.dsol.simtime.SimTimeDoubleUnit;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

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
    /**
     * Create a dummy path for the tests.
     * @param network RoadNetwork; the network
     * @param laneType the lane type
     * @param gtuType the GTU type
     * @return List&lt;Lane&gt;; the dummy path
     * @throws Exception when something goes wrong (should not happen)
     */
    private List<Lane> dummyPath(final RoadNetwork network, final LaneType laneType, final GTUType gtuType) throws Exception
    {
        OTSRoadNode b = new OTSRoadNode(network, "B", new OTSPoint3D(12345, 0, 0), Direction.ZERO);
        ArrayList<Lane> result = new ArrayList<Lane>();
        Lane[] lanes = LaneFactory.makeMultiLane(network, "AtoB",
                new OTSRoadNode(network, "A", new OTSPoint3D(1234, 0, 0), Direction.ZERO), b, null, 1, laneType,
                new Speed(100, KM_PER_HOUR), null);
        result.add(lanes[0]);
        // Make a continuation lane to prevent errors when the operational plan exceeds the available remaining length
        lanes = LaneFactory.makeMultiLane(network, "BtoC", b,
                new OTSRoadNode(network, "C", new OTSPoint3D(99999, 0, 0), Direction.ZERO), null, 1, laneType,
                new Speed(100, KM_PER_HOUR), null);
        // System.out.println("continuation lane is " + lanes[0] + " length is " + lanes[0].getLength());
        // System.out.println("next lanes is " + result.get(0).nextLanes(gtuType));
        return result;
    }

    /**
     * Test the AccelerationContourPlot.
     * @throws Exception when something goes wrong (should not happen)
     */
    @SuppressWarnings("static-method")
    // TODO @Test
    public final void accelerationContourTest() throws Exception
    {
        Network network = new OTSNetwork("contour test network", true);
        GTUType gtuType = CAR;
        LaneType laneType = LaneType.TWO_WAY_LANE;
        List<Lane> path = dummyPath(network, laneType, gtuType);
        AccelerationContourPlot acp = new AccelerationContourPlot("Acceleration", path);
        assertTrue("newly created AccelerationContourPlot should not be null", null != acp);
        assertEquals("SeriesKey should be \"acceleration\"", "acceleration", acp.getSeriesKey(0));
        standardContourTests(acp, path.get(0), gtuType, Double.NaN, 0);
    }

    /**
     * Test the DensityContourPlot.
     * @throws Exception when something goes wrong (should not happen)
     */
    @SuppressWarnings("static-method")
    // TODO @Test
    public final void densityContourTest() throws Exception
    {
        Network network = new OTSNetwork("contour test network", true);
        GTUType gtuType = CAR;
        LaneType laneType = LaneType.TWO_WAY_LANE;
        List<Lane> path = dummyPath(network, laneType, gtuType);
        DensityContourPlot dcp = new DensityContourPlot("Density", path);
        assertTrue("newly created DensityContourPlot should not be null", null != dcp);
        assertEquals("SeriesKey should be \"density\"", "density", dcp.getSeriesKey(0));
        standardContourTests(dcp, path.get(0), gtuType, 0, Double.NaN);
    }

    /**
     * Debugging method.
     * @param cp cp
     * @param fromX fromX
     * @param toX toX
     * @param fromY fromY
     * @param toY toY
     */
    static void printMatrix(final ContourPlot cp, final int fromX, final int toX, final int fromY, final int toY)
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
     * Test the FlowContourPlot.
     * @throws Exception when something goes wrong (should not happen)
     */
    @SuppressWarnings("static-method")
    // TODO @Test
    public final void flowContourTest() throws Exception
    {
        Network network = new OTSNetwork("contour test network", true);
        GTUType gtuType = CAR;
        LaneType laneType = LaneType.TWO_WAY_LANE;
        List<Lane> path = dummyPath(network, laneType, gtuType);
        FlowContourPlot fcp = new FlowContourPlot("Density", path);
        assertTrue("newly created DensityContourPlot should not be null", null != fcp);
        assertEquals("SeriesKey should be \"flow\"", "flow", fcp.getSeriesKey(0));
        standardContourTests(fcp, path.get(0), gtuType, 0, Double.NaN);
    }

    /**
     * Test the SpeedContourPlot.
     * @throws Exception when something goes wrong (should not happen)
     */
    @SuppressWarnings("static-method")
    // TODO@Test
    public final void speedContourTest() throws Exception
    {
        Network network = new OTSNetwork("contour test network", true);
        GTUType gtuType = CAR;
        LaneType laneType = LaneType.TWO_WAY_LANE;
        List<Lane> path = dummyPath(network, laneType, gtuType);
        SpeedContourPlot scp = new SpeedContourPlot("Density", path);
        assertTrue("newly created DensityContourPlot should not be null", null != scp);
        assertEquals("SeriesKey should be \"speed\"", "speed", scp.getSeriesKey(0));
        standardContourTests(scp, path.get(0), gtuType, Double.NaN, 50);
    }

    /**
     * Test various properties of a ContourPlot that has no observed data added.
     * @param cp ContourPlot; the ContourPlot to test
     * @param lane Lane; the lane on which the test GTUs are run
     * @param gtuType GTUType; the type of GTU
     * @param expectedZValue double; the value that getZ and getZValue should return for a valid item when no car has passed
     * @param expectedZValueWithTraffic double; the value that getZ and getZValue should return a valid item where a car has
     *            traveled at constant speed of 50 km/h. Supply Double.NaN if the value varies but differs from the value
     *            expected when no car has passed
     * @throws Exception when something goes wrong (should not happen)
     */
    public static void standardContourTests(final ContourPlot cp, final Lane lane, final GTUType gtuType,
            final double expectedZValue, final double expectedZValueWithTraffic) throws Exception
    {
        assertEquals("seriesCount should be 1", 1, cp.getSeriesCount());
        assertEquals("domainOrder should be ASCENDING", DomainOrder.ASCENDING, cp.getDomainOrder());
        assertEquals("indexOf always returns 0", 0, cp.indexOf(0));
        assertEquals("indexOf always returns 0", 0, cp.indexOf("abc"));
        assertNull("getGroup always returns null", cp.getGroup());
        int xBins = cp.xAxisBins();
        int yBins = cp.yAxisBins();
        int expectedXBins = (int) Math
                .ceil((DoubleScalar.minus(ContourPlot.INITIALUPPERTIMEBOUND, ContourPlot.INITIALLOWERTIMEBOUND).getSI())
                        / ContourPlot.STANDARDTIMEGRANULARITIES[ContourPlot.STANDARDINITIALTIMEGRANULARITYINDEX]);
        assertEquals("Initial xBins should be " + expectedXBins, expectedXBins, xBins);
        int expectedYBins = (int) Math.ceil(lane.getLength().getSI()
                / ContourPlot.STANDARDDISTANCEGRANULARITIES[ContourPlot.STANDARDINITIALDISTANCEGRANULARITYINDEX]);
        assertEquals("yBins should be " + expectedYBins, expectedYBins, yBins);
        int bins = cp.getItemCount(0);
        assertEquals("Total bin count is product of xBins * yBins", xBins * yBins, bins);
        // Cache the String equivalents of minimumDistance and maximumDistance, INITIALLOWERTIMEBOUND and
        // INITUALUPPERTIMEBOUND
        String initialLowerTimeBoundString = ContourPlot.INITIALLOWERTIMEBOUND.toString();
        String initialUpperTimeBoundString = ContourPlot.INITIALUPPERTIMEBOUND.toString();
        // Vary the x granularity
        for (double timeGranularity : ContourPlot.STANDARDTIMEGRANULARITIES)
        {
            cp.actionPerformed(new ActionEvent(cp, 0, "setTimeGranularity " + timeGranularity));
            for (double distanceGranularity : ContourPlot.STANDARDDISTANCEGRANULARITIES)
            {
                cp.actionPerformed(new ActionEvent(cp, 0, "setDistanceGranularity " + distanceGranularity));
                cp.reGraph();
                expectedXBins = (int) Math
                        .ceil((DoubleScalar.minus(ContourPlot.INITIALUPPERTIMEBOUND, ContourPlot.INITIALLOWERTIMEBOUND).getSI())
                                / timeGranularity);
                xBins = cp.xAxisBins();
                assertEquals("Modified xBins should be " + expectedXBins, expectedXBins, xBins);
                expectedYBins = (int) Math.ceil(lane.getLength().getSI() / distanceGranularity);
                yBins = cp.yAxisBins();
                assertEquals("Modified yBins should be " + expectedYBins, expectedYBins, yBins);
                bins = cp.getItemCount(0);
                assertEquals("Total bin count is product of xBins * yBins", xBins * yBins, bins);
                for (int item = 0; item < bins; item++)
                {
                    double x = cp.getXValue(0, item);
                    assertTrue("X should be >= " + initialLowerTimeBoundString, x >= ContourPlot.INITIALLOWERTIMEBOUND.getSI());
                    assertTrue("X should be <= " + initialUpperTimeBoundString, x <= ContourPlot.INITIALUPPERTIMEBOUND.getSI());
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
                    // Ignore
                }
                try
                {
                    cp.getXValue(0, bins);
                    fail("Should have thrown an Exception");
                }
                catch (RuntimeException e)
                {
                    // Ignore
                }
                try
                {
                    cp.yAxisBin(-1);
                    fail("Should have thrown an Exception");
                }
                catch (RuntimeException e)
                {
                    // Ignore
                }
                try
                {
                    cp.yAxisBin(bins);
                    fail("Should have thrown an Exception");
                }
                catch (RuntimeException e)
                {
                    // Ignore
                }
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
            // Ignore
        }
        try
        {
            cp.actionPerformed(new ActionEvent(cp, 0, "setDistanceGranularity -1"));
            fail("Should have thrown an Exception");
        }
        catch (RuntimeException e)
        {
            // ignore
        }
        try
        {
            cp.actionPerformed(new ActionEvent(cp, 0, "setDistanceGranularity abc"));
            fail("Should have thrown an Exception");
        }
        catch (RuntimeException e)
        {
            // ignore
        }
        try
        {
            cp.actionPerformed(new ActionEvent(cp, 0, "setDistanceGranularitIE 10")); // typo in the event name
            fail("Should have thrown an Exception");
        }
        catch (RuntimeException e)
        {
            // ignore
        }
        // Make the time granularity a bit more reasonable
        final double useTimeGranularity = 30; // [s]
        cp.actionPerformed(new ActionEvent(cp, 0, "setTimeGranularity " + useTimeGranularity));
        final double useDistanceGranularity =
                ContourPlot.STANDARDDISTANCEGRANULARITIES[ContourPlot.STANDARDDISTANCEGRANULARITIES.length - 1];
        cp.actionPerformed(new ActionEvent(cp, 0, "setDistanceGranularity " + useDistanceGranularity));
        cp.reGraph();
        bins = cp.getItemCount(0);
        Time initialTime = new Time(0, TimeUnit.BASE_SECOND);
        Length initialPosition = new Length(100, METER);
        Speed initialSpeed = new Speed(50, KM_PER_HOUR);
        ContourPlotModel model = new ContourPlotModel();
        SimpleSimulator simulator =
                new SimpleSimulator(initialTime, new Duration(0, SECOND), new Duration(1800, SECOND), model);
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
        OTSNetwork network = new OTSNetwork("network");

        // Check that the initial data in the graph contains no trace of any car.
        for (int item = 0; item < bins; item++)
        {
            double x = cp.getXValue(0, item);
            assertTrue("X should be >= " + ContourPlot.INITIALLOWERTIMEBOUND, x >= ContourPlot.INITIALLOWERTIMEBOUND.getSI());
            assertTrue("X should be <= " + ContourPlot.INITIALUPPERTIMEBOUND, x <= ContourPlot.INITIALUPPERTIMEBOUND.getSI());
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

        LaneBasedIndividualGTU car = CarTest.makeReferenceCar("0", gtuType, lane, initialPosition, initialSpeed, simulator,
                gtuFollowingModel, laneChangeModel, network);
        car.getParameters().setParameter(ParameterTypes.LOOKAHEAD, new Length(10, LengthUnit.KILOMETER));

        // System.out.println("Running simulator from " + simulator.getSimulatorTime() + " to "
        // + gtuFollowingModel.timeAfterCompletionOfStep(0));
        double stopTime = gtuFollowingModel.timeAfterCompletionOfStep(0).si;
        simulator.runUpToAndIncluding(new Time(stopTime, TimeUnit.BASE_SECOND));
        while (simulator.isRunning())
        {
            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException ie)
            {
                ie = null; // ignore
            }
        }
        // System.out.println("Simulator is now at " + simulator.getSimulatorTime());
        // System.out.println("Car at start time " + car.getOperationalPlan().getStartTime() + " is at "
        // + car.getPosition(car.getOperationalPlan().getStartTime()));
        // System.out.println("At time " + simulator.getSimulator().getSimulatorTime() + " car is at " + car);
        for (int item = 0; item < bins; item++)
        {
            double x = cp.getXValue(0, item);
            assertTrue("X should be >= " + ContourPlot.INITIALLOWERTIMEBOUND, x >= ContourPlot.INITIALLOWERTIMEBOUND.getSI());
            assertTrue("X should be <= " + ContourPlot.INITIALUPPERTIMEBOUND, x <= ContourPlot.INITIALUPPERTIMEBOUND.getSI());
            Number alternateX = cp.getX(0, item);
            assertEquals("getXValue and getX should return things that have the same value", x, alternateX.doubleValue(),
                    0.000001);
            double y = cp.getYValue(0, item);
            Number alternateY = cp.getY(0, item);
            assertEquals("getYValue and getY should return things that have the same value", y, alternateY.doubleValue(),
                    0.000001);
            double z = cp.getZValue(0, item);
            // figure out if the car has traveled through this cell
            // if (x >= 180)
            // System.out.println(String.format("t=%.3f, x=%.3f z=%f, exp=%.3f, carLastEval=%s, carNextEval=%s", x, y, z,
            // expectedZValue, car.getOperationalPlan().getStartTime().getSI(), car.getOperationalPlan().getEndTime()
            // .getSI()));
            boolean hit = false;
            if (x + useTimeGranularity >= 0// car.getOperationalPlan().getStartTime().getSI()
                    && x < 60)// car.getOperationalPlan().getEndTime().getSI())
            {
                // the car MAY have contributed to this cell
                Time cellStartTime =
                        new Time(Math.max(car.getOperationalPlan().getStartTime().getSI(), x), TimeUnit.BASE_SECOND);
                Time cellEndTime = new Time(Math.min(car.getOperationalPlan().getEndTime().getSI(), x + useTimeGranularity),
                        TimeUnit.BASE_SECOND);
                // System.out.println("cellStartTime=" + cellStartTime + ", cellEndTime=" + cellEndTime);
                // The next if statement is the problem
                // if (cellStartTime.lt(cellEndTime)
                // && car.position(lane, car.getRear(), cellStartTime).getSI() <= y + useDistanceGranularity
                // && car.position(lane, car.getRear(), cellEndTime).getSI() >= y)
                double xAtCellStartTime = initialPosition.si + initialSpeed.si * cellStartTime.si;
                double xAtCellEndTime = initialPosition.si + initialSpeed.si * cellEndTime.si;
                if (xAtCellStartTime < y + useDistanceGranularity && xAtCellEndTime >= y)
                {
                    hit = true;
                }
            }
            // System.out.println(String.format(
            // "hit=%s, t=%.3f, x=%.3f z=%f, exp=%.3f, carLastEval=%s, carNextEval=%s, simulatortime=%s", hit, x, y, z,
            // expectedZValue, car.getOperationalPlan().getStartTime().getSI(), car.getOperationalPlan().getEndTime()
            // .getSI(), car.getSimulator().getSimulatorTime()));
            Number alternateZ = cp.getZ(0, item);
            if (hit)
            {
                if (!Double.isNaN(expectedZValueWithTraffic))
                {
                    if (Double.isNaN(z))
                    {
                        printMatrix(cp, 0, 10, 0, 10);
                        System.out.println("Oops - z is NaN, expected z value with traffic is " + expectedZValueWithTraffic);
                    }
                    assertEquals("Z value should be " + expectedZValueWithTraffic, expectedZValueWithTraffic, z, 0.0001);
                    assertEquals("Z value should be " + expectedZValueWithTraffic, expectedZValueWithTraffic,
                            alternateZ.doubleValue(), 0.0001);
                }
                else
                {
                    if (Double.isNaN(expectedZValue))
                    { // FIXME looks wrong / PK
                        assertFalse("Z value should not be NaN", Double.isNaN(z));
                    }
                }
            }
            else
            {
                if (Double.isNaN(expectedZValue))
                {
                    // if (!Double.isNaN(z))
                    // {
                    // System.out.println("Oops");
                    // Time cellStartTime = new Time(x, SECOND);
                    // Time cellEndTime =
                    // new Time(Math.min(car.getOperationalPlan().getEndTime().getSI(), x + useTimeGranularity),
                    // SECOND);
                    // double xAtCellStartTime = initialPosition.si + initialSpeed.si * cellStartTime.si;
                    // double xAtCellEndTime = initialPosition.si + initialSpeed.si * cellEndTime.si;
                    // System.out.println("cellStartTime=" + cellStartTime + " cellEndTime=" + cellEndTime
                    // + " xAtCellStartTime=" + xAtCellStartTime + " xAtCellEndTime=" + xAtCellEndTime);
                    // double cellX = cp.getXValue(0, item);
                    // double cellY = cp.getYValue(0, item);
                    // double cellZ = cp.getZValue(0, item);
                    // System.out.println("cellX=" + cellX + " cellY=" + cellY + " cellZ=" + cellZ + " timeGranularity="
                    // + useTimeGranularity + " distanceGranularity=" + useDistanceGranularity);
                    // cp.getZValue(0, item);
                    // }
                    assertTrue("Z value should be NaN", Double.isNaN(z));
                }
                else
                {
                    assertEquals("Z value should be " + expectedZValue, expectedZValue, z, 0.0001);
                }
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
        }
        // System.out.println("Running simulator from " + simulator.getSimulatorTime() + " to "
        // + gtuFollowingModel.timeAfterCompletionOfStep(1));
        stopTime = gtuFollowingModel.timeAfterCompletionOfStep(1).si;
        simulator.runUpToAndIncluding(new Time(stopTime, TimeUnit.BASE_SECOND));
        while (simulator.isRunning())
        {
            try
            {
                Thread.sleep(10);
            }
            catch (InterruptedException ie)
            {
                ie = null; // ignore
            }
        }
        // System.out.println("Simulator is now at " + simulator.getSimulatorTime());
        // Check that the time range has expanded
        xBins = cp.xAxisBins();
        bins = cp.getItemCount(0);
        double observedHighestTime = Double.MIN_VALUE;
        for (int bin = 0; bin < bins; bin++)
        {
            double xValue = cp.getXValue(0, bin);
            if (xValue > observedHighestTime)
            {
                observedHighestTime = xValue;
            }
        }
        double expectedHighestTime =
                Math.floor((car.getSimulator().getSimulatorTime().si - 0.001) / useTimeGranularity) * useTimeGranularity;
        assertEquals("Time range should run up to " + expectedHighestTime, expectedHighestTime, observedHighestTime, 0.0001);
        // Check the updateHint method in the PointerHandler
        // First get the panel that stores the result of updateHint (this is ugly)
        JLabel hintPanel = null;
        ChartPanel chartPanel = null;
        for (Component c0 : cp.getComponents())
        {
            for (Component c1 : ((Container) c0).getComponents())
            {
                if (c1 instanceof Container)
                {
                    for (Component c2 : ((Container) c1).getComponents())
                    {
                        // System.out.println("c2 is " + c2);
                        if (c2 instanceof Container)
                        {
                            for (Component c3 : ((Container) c2).getComponents())
                            {
                                // System.out.println("c3 is " + c3);
                                if (c3 instanceof JLabel)
                                {
                                    if (null == hintPanel)
                                    {
                                        hintPanel = (JLabel) c3;
                                    }
                                    else
                                    {
                                        fail("There should be only one JPanel in a ContourPlot");
                                    }
                                }
                                if (c3 instanceof ChartPanel)
                                {
                                    if (null == chartPanel)
                                    {
                                        chartPanel = (ChartPanel) c3;
                                    }
                                    else
                                    {
                                        fail("There should be only one ChartPanel in a ContourPlot");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        if (null == hintPanel)
        {
            fail("Could not find a JLabel in ContourPlot");
        }
        if (null == chartPanel)
        {
            fail("Could not find a ChartPanel in ContourPlot");
        }
        assertEquals("Initially the text should be a single space", " ", hintPanel.getText());
        PointerHandler ph = null;
        for (MouseListener ml : chartPanel.getMouseListeners())
        {
            if (ml instanceof PointerHandler)
            {
                if (null == ph)
                {
                    ph = (PointerHandler) ml;
                }
                else
                {
                    fail("There should be only one PointerHandler on the chartPanel");
                }
            }
        }
        if (null == ph)
        {
            fail("Could not find the PointerHandler for the chartPanel");
        }
        ph.updateHint(1, 2);
        // System.out.println("Hint text is now " + hintPanel.getText());
        assertFalse("Hint should not be a single space", " ".equals(hintPanel.getText()));
        ph.updateHint(Double.NaN, Double.NaN);
        assertEquals("The text should again be a single space", " ", hintPanel.getText());
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

/**
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version feb. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class ContourPlotModel implements OTSModelInterface
{

    /** */
    private static final long serialVersionUID = 20150209L;

    /** {@inheritDoc} */
    @Override
    public void constructModel(final SimulatorInterface<Time, Duration, SimTimeDoubleUnit> simulator) throws SimRuntimeException
    {
        // NOT USED
    }

    /** {@inheritDoc} */
    @Override
    public OTSSimulatorInterface getSimulator()

    {
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public OTSNetwork getNetwork()
    {
        return null;
    }

    @Override
    public void constructModel() throws SimRuntimeException
    {
        // TODO Auto-generated method stub
        
    }

    @Override
    public InputParameterMap getInputParameterMap()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public List<OutputStatistic<?>> getOutputStatistics()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getShortName()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getDescription()
    {
        // TODO Auto-generated method stub
        return null;
    }

}
