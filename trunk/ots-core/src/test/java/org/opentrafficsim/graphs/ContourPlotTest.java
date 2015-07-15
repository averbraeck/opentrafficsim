package org.opentrafficsim.graphs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JOptionPane;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.jfree.chart.ChartPanel;
import org.jfree.data.DomainOrder;
import org.junit.Test;
import org.opentrafficsim.core.car.CarTest;
import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.following.FixedAccelerationModel;
import org.opentrafficsim.core.gtu.following.SequentialFixedAccelerationModel;
import org.opentrafficsim.core.gtu.lane.changing.Egoistic;
import org.opentrafficsim.core.gtu.lane.changing.LaneChangeModel;
import org.opentrafficsim.core.network.factory.LaneFactory;
import org.opentrafficsim.core.network.geotools.NodeGeotools;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.LaneType;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.simulationengine.SimpleSimulator;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * Test the non-GUI part of the ContourPlot class.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial versionAug 21, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class ContourPlotTest
{
    /**
     * Create a dummy path for the tests.
     * @return List&lt;Lane&gt;; the dummy path
     * @throws Exception when something goes wrong (should not happen)
     */
    private List<Lane> dummyPath(final LaneType<String> laneType) throws Exception
    {
        ArrayList<Lane> result = new ArrayList<Lane>();
        Lane[] lanes =
                LaneFactory.makeMultiLane("AtoB", new NodeGeotools.STR("A", new Coordinate(1234, 0, 0)),
                        new NodeGeotools.STR("B", new Coordinate(12345, 0, 0)), null, 1, laneType,
                        new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR), null);
        result.add(lanes[0]);
        return result;
    }

    /**
     * Test the AccelerationContourPlot.
     * @throws Exception when something goes wrong (should not happen)
     */
    @SuppressWarnings("static-method")
    @Test
    public final void accelerationContourTest() throws Exception
    {
        LaneType<String> laneType = new LaneType<String>("CarLane");
        GTUType<?> gtuType = GTUType.makeGTUType("Car");
        laneType.addCompatibility(gtuType);
        List<Lane> path = dummyPath(laneType);
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
    @Test
    public final void densityContourTest() throws Exception
    {
        LaneType<String> laneType = new LaneType<String>("CarLane");
        GTUType<?> gtuType = GTUType.makeGTUType("Car");
        laneType.addCompatibility(gtuType);
        List<Lane> path = dummyPath(laneType);
        DensityContourPlot dcp = new DensityContourPlot("Density", path);
        assertTrue("newly created DensityContourPlot should not be null", null != dcp);
        assertEquals("SeriesKey should be \"density\"", "density", dcp.getSeriesKey(0));
        standardContourTests(dcp, path.get(0), gtuType, 0, Double.NaN);
    }

    /**
     * Test the FlowContourPlot.
     * @throws Exception when something goes wrong (should not happen)
     */
    @SuppressWarnings("static-method")
    @Test
    public final void flowContourTest() throws Exception
    {
        LaneType<String> laneType = new LaneType<String>("CarLane");
        GTUType<?> gtuType = GTUType.makeGTUType("Car");
        laneType.addCompatibility(gtuType);
        List<Lane> path = dummyPath(laneType);
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
    @Test
    public final void speedContourTest() throws Exception
    {
        LaneType<String> laneType = new LaneType<String>("CarLane");
        GTUType<?> gtuType = GTUType.makeGTUType("Car");
        laneType.addCompatibility(gtuType);
        List<Lane> path = dummyPath(laneType);
        SpeedContourPlot scp = new SpeedContourPlot("Density", path);
        assertTrue("newly created DensityContourPlot should not be null", null != scp);
        assertEquals("SeriesKey should be \"speed\"", "speed", scp.getSeriesKey(0));
        standardContourTests(scp, path.get(0), gtuType, Double.NaN, 50);
    }

    /**
     * Test various properties of a ContourPlot that has no observed data added.
     * @param cp ContourPlot; the ContourPlot to test
     * @param lane Lane; the lane on which the test GTUs are run
     * @param gtuType GTUType&lt;?&gt;; the type of GTU
     * @param expectedZValue double; the value that getZ and getZValue should return for a valid item when no car has
     *            passed
     * @param expectedZValueWithTraffic double; the value that getZ and getZValue should return a valid item where a car
     *            has traveled at constant speed of 50 km/h. Supply Double.NaN if the value varies but differs from the
     *            value expected when no car has passed
     * @throws Exception when something goes wrong (should not happen)
     */
    public static void standardContourTests(final ContourPlot cp, Lane lane, GTUType<?> gtuType,
            final double expectedZValue, final double expectedZValueWithTraffic) throws Exception
    {
        assertEquals("seriesCount should be 1", 1, cp.getSeriesCount());
        assertEquals("domainOrder should be ASCENDING", DomainOrder.ASCENDING, cp.getDomainOrder());
        assertEquals("indexOf always returns 0", 0, cp.indexOf(0));
        assertEquals("indexOf always returns 0", 0, cp.indexOf("abc"));
        assertEquals("getGroup always returns null", null, cp.getGroup());
        int xBins = cp.xAxisBins();
        int yBins = cp.yAxisBins();
        int expectedXBins =
                (int) Math.ceil((DoubleScalar.minus(ContourPlot.INITIALUPPERTIMEBOUND,
                        ContourPlot.INITIALLOWERTIMEBOUND).getSI())
                        / ContourPlot.STANDARDTIMEGRANULARITIES[ContourPlot.STANDARDINITIALTIMEGRANULARITYINDEX]);
        assertEquals("Initial xBins should be " + expectedXBins, expectedXBins, xBins);
        int expectedYBins =
                (int) Math
                        .ceil(lane.getLength().getSI()
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
                expectedXBins =
                        (int) Math.ceil((DoubleScalar.minus(ContourPlot.INITIALUPPERTIMEBOUND,
                                ContourPlot.INITIALLOWERTIMEBOUND).getSI()) / timeGranularity);
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
                    assertTrue("X should be >= " + initialLowerTimeBoundString,
                            x >= ContourPlot.INITIALLOWERTIMEBOUND.getSI());
                    assertTrue("X should be <= " + initialUpperTimeBoundString,
                            x <= ContourPlot.INITIALUPPERTIMEBOUND.getSI());
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
                        assertEquals("Alternate Z value should be " + expectedZValue, expectedZValue,
                                alternateZ.doubleValue(), 0.0000);
                    }
                }
                try
                {
                    cp.getXValue(0, -1);
                    fail("Should have thrown an Error");
                }
                catch (Error e)
                {
                    // Ignore
                }
                try
                {
                    cp.getXValue(0, bins);
                    fail("Should have thrown an Error");
                }
                catch (Error e)
                {
                    // Ignore
                }
                try
                {
                    cp.yAxisBin(-1);
                    fail("Should have thrown an Error");
                }
                catch (Error e)
                {
                    // Ignore
                }
                try
                {
                    cp.yAxisBin(bins);
                    fail("Should have thrown an Error");
                }
                catch (Error e)
                {
                    // Ignore
                }
            }
        }
        // Test some ActionEvents that ContourPlot can not handle
        try
        {
            cp.actionPerformed(new ActionEvent(cp, 0, "blabla"));
            fail("Should have thrown an Error");
        }
        catch (Error e)
        {
            // Ignore
        }
        try
        {
            cp.actionPerformed(new ActionEvent(cp, 0, "setDistanceGranularity -1"));
            fail("Should have thrown an Error");
        }
        catch (Error e)
        {
            // ignore
        }
        try
        {
            cp.actionPerformed(new ActionEvent(cp, 0, "setDistanceGranularity abc"));
            fail("Should have thrown an Error");
        }
        catch (Error e)
        {
            // ignore
        }
        try
        {
            cp.actionPerformed(new ActionEvent(cp, 0, "setDistanceGranularitIE 10")); // typo in the event name
            fail("Should have thrown an Error");
        }
        catch (Error e)
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
        DoubleScalar.Abs<TimeUnit> initialTime = new DoubleScalar.Abs<TimeUnit>(0, TimeUnit.SECOND);
        DoubleScalar.Rel<LengthUnit> initialPosition = new DoubleScalar.Rel<LengthUnit>(100, LengthUnit.METER);
        DoubleScalar.Abs<SpeedUnit> initialSpeed = new DoubleScalar.Abs<SpeedUnit>(50, SpeedUnit.KM_PER_HOUR);
        ContourPlotModel model = new ContourPlotModel();
        SimpleSimulator simulator =
                new SimpleSimulator(initialTime, new DoubleScalar.Rel<TimeUnit>(0, TimeUnit.SECOND),
                        new DoubleScalar.Rel<TimeUnit>(1800, TimeUnit.SECOND), model);
        // Create a car running 50 km.h
        SequentialFixedAccelerationModel gtuFollowingModel = new SequentialFixedAccelerationModel(simulator);
        // Make the car run at constant speed for one minute
        gtuFollowingModel.addStep(new FixedAccelerationModel(new DoubleScalar.Abs<AccelerationUnit>(0,
                AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Rel<TimeUnit>(60, TimeUnit.SECOND)));
        // Make the car run at constant speed for another minute
        gtuFollowingModel.addStep(new FixedAccelerationModel(new DoubleScalar.Abs<AccelerationUnit>(0,
                AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Rel<TimeUnit>(600, TimeUnit.SECOND)));
        // Make the car run at constant speed for five more minutes
        gtuFollowingModel.addStep(new FixedAccelerationModel(new DoubleScalar.Abs<AccelerationUnit>(0,
                AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Rel<TimeUnit>(300, TimeUnit.SECOND)));
        LaneChangeModel laneChangeModel = new Egoistic();
        LaneBasedIndividualCar<Integer> car =
                CarTest.makeReferenceCar(0, gtuType, lane, initialPosition, initialSpeed, simulator, gtuFollowingModel,
                        laneChangeModel);
        // Check that the initial data in the graph contains no trace of any car.
        for (int item = 0; item < bins; item++)
        {
            double x = cp.getXValue(0, item);
            assertTrue("X should be >= " + ContourPlot.INITIALLOWERTIMEBOUND,
                    x >= ContourPlot.INITIALLOWERTIMEBOUND.getSI());
            assertTrue("X should be <= " + ContourPlot.INITIALUPPERTIMEBOUND,
                    x <= ContourPlot.INITIALUPPERTIMEBOUND.getSI());
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
                assertEquals("Alternate Z value should be " + expectedZValue, expectedZValue, alternateZ.doubleValue(),
                        0.0000);
            }
        }
        simulator.runUpTo(gtuFollowingModel.timeAfterCompletionOfStep(0));
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
        }        // System.out.println("Car at start time " + car.getLastEvaluationTime() + " is at "
        // + car.getPosition(car.getLastEvaluationTime()));
        // System.out.println("At time " + simulator.getSimulator().getSimulatorTime().get() + " car is at " + car);
        for (int item = 0; item < bins; item++)
        {
            double x = cp.getXValue(0, item);
            assertTrue("X should be >= " + ContourPlot.INITIALLOWERTIMEBOUND,
                    x >= ContourPlot.INITIALLOWERTIMEBOUND.getSI());
            assertTrue("X should be <= " + ContourPlot.INITIALUPPERTIMEBOUND,
                    x <= ContourPlot.INITIALUPPERTIMEBOUND.getSI());
            Number alternateX = cp.getX(0, item);
            assertEquals("getXValue and getX should return things that have the same value", x,
                    alternateX.doubleValue(), 0.000001);
            double y = cp.getYValue(0, item);
            Number alternateY = cp.getY(0, item);
            assertEquals("getYValue and getY should return things that have the same value", y,
                    alternateY.doubleValue(), 0.000001);
            double z = cp.getZValue(0, item);
            // figure out if the car has traveled through this cell
            // if (x >= 180)
            // System.out.println(String.format("t=%.3f, x=%.3f z=%f, exp=%.3f, carLast=%s, carNext=%s", x, y, z,
            // expectedZValue, car.getLastEvaluationTime().getSI(), car.getNextEvaluationTime().getSI()));
            boolean hit = false;
            if (x + useTimeGranularity >= car.getLastEvaluationTime().getSI()
                    && x <= car.getNextEvaluationTime().getSI())
            {
                // the car MAY have contributed to this cell
                DoubleScalar.Abs<TimeUnit> cellStartTime =
                        new DoubleScalar.Abs<TimeUnit>(Math.max(car.getLastEvaluationTime().getSI(), x),
                                TimeUnit.SECOND);
                DoubleScalar.Abs<TimeUnit> cellEndTime =
                        new DoubleScalar.Abs<TimeUnit>(Math.min(car.getNextEvaluationTime().getSI(), x
                                + useTimeGranularity), TimeUnit.SECOND);
                if (cellStartTime.lt(cellEndTime)
                        && car.position(lane, car.getReference(), cellStartTime).getSI() <= y + useDistanceGranularity
                        && car.position(lane, car.getReference(), cellEndTime).getSI() >= y)
                {
                    hit = true;
                }
            }
            // System.out.println(String.format("hit=%s, t=%.3f, x=%.3f z=%f, exp=%.3f", hit, x, y, z, expectedZValue));
            Number alternateZ = cp.getZ(0, item);
            if (hit)
            {
                if (!Double.isNaN(expectedZValueWithTraffic))
                {
                    assertEquals("Z value should be " + expectedZValueWithTraffic, expectedZValueWithTraffic, z, 0.0001);
                    assertEquals("Z value should be " + expectedZValueWithTraffic, expectedZValueWithTraffic,
                            alternateZ.doubleValue(), 0.0001);
                }
                else
                {
                    if (Double.isNaN(expectedZValue))
                    {// FIXME looks wrong / PK
                        assertFalse("Z value should not be NaN", Double.isNaN(z));
                    }
                }
            }
            else
            {
                if (Double.isNaN(expectedZValue))
                {
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
                    assertEquals("Alternate Z value should be " + expectedZValue, expectedZValue,
                            alternateZ.doubleValue(), 0.0000);
                }
            }
        }
        simulator.runUpTo(gtuFollowingModel.timeAfterCompletionOfStep(1));
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
        DoubleScalar.Abs<TimeUnit> carEndTime = car.getNextEvaluationTime();
        double expectedHighestTime = Math.floor((carEndTime.getSI() - 0.001) / useTimeGranularity) * useTimeGranularity;
        assertEquals("Time range should run up to " + expectedHighestTime, expectedHighestTime, observedHighestTime,
                0.0001);
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
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author$, initial version9 feb. 2015 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
class ContourPlotModel implements OTSModelInterface
{

    /** */
    private static final long serialVersionUID = 20150209L;

    /** {@inheritDoc} */
    @Override
    public void constructModel(SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> simulator)
            throws SimRuntimeException, RemoteException
    {
        // NOT USED
    }

    /** {@inheritDoc} */
    @Override
    public SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> getSimulator() throws RemoteException
    {
        return null;
    }

}
