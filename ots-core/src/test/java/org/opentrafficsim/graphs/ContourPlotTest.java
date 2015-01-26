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

import javax.naming.NamingException;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;

import org.jfree.chart.ChartPanel;
import org.jfree.data.DomainOrder;
import org.junit.Test;
import org.opentrafficsim.car.Car;
import org.opentrafficsim.car.CarTest;
import org.opentrafficsim.core.dsol.OTSDEVSSimulator;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel.GTUFollowingModelResult;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;

/**
 * Test the non-GUI part of the ContourPlot class.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Aug 21, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class ContourPlotTest
{
    /** Lower bound of test distance range. */
    static DoubleScalar.Rel<LengthUnit> minimumDistance = new DoubleScalar.Rel<LengthUnit>(1234, LengthUnit.METER);

    /** Upper bound of test distance range. */
    static DoubleScalar.Rel<LengthUnit> maximumDistance = new DoubleScalar.Rel<LengthUnit>(12345, LengthUnit.METER);

    /**
     * Test the AccelerationContourPlot.
     * @throws NamingException
     * @throws SimRuntimeException
     * @throws NetworkException
     * @throws RemoteException
     */
    @SuppressWarnings("static-method")
    @Test
    public final void accelerationContourTest() throws RemoteException, NetworkException, SimRuntimeException,
            NamingException
    {
        AccelerationContourPlot acp = new AccelerationContourPlot("Acceleration", minimumDistance, maximumDistance);
        assertTrue("newly created AccelerationContourPlot should not be null", null != acp);
        assertEquals("SeriesKey should be \"acceleration\"", "acceleration", acp.getSeriesKey(0));
        standardContourTests(acp, Double.NaN, 0);
    }

    /**
     * Test the DensityContourPlot.
     * @throws NamingException
     * @throws SimRuntimeException
     * @throws NetworkException
     * @throws RemoteException
     */
    @SuppressWarnings("static-method")
    @Test
    public final void densityContourTest() throws RemoteException, NetworkException, SimRuntimeException,
            NamingException
    {
        DensityContourPlot dcp = new DensityContourPlot("Density", minimumDistance, maximumDistance);
        assertTrue("newly created DensityContourPlot should not be null", null != dcp);
        assertEquals("SeriesKey should be \"density\"", "density", dcp.getSeriesKey(0));
        standardContourTests(dcp, 0, Double.NaN);
    }

    /**
     * Test the FlowContourPlot.
     * @throws NamingException
     * @throws SimRuntimeException
     * @throws NetworkException
     * @throws RemoteException
     */
    @SuppressWarnings("static-method")
    @Test
    public final void flowContourTest() throws RemoteException, NetworkException, SimRuntimeException, NamingException
    {
        FlowContourPlot fcp = new FlowContourPlot("Density", minimumDistance, maximumDistance);
        assertTrue("newly created DensityContourPlot should not be null", null != fcp);
        assertEquals("SeriesKey should be \"flow\"", "flow", fcp.getSeriesKey(0));
        standardContourTests(fcp, 0, Double.NaN);
    }

    /**
     * Test the SpeedContourPlot.
     * @throws NamingException
     * @throws SimRuntimeException
     * @throws NetworkException
     * @throws RemoteException
     */
    @SuppressWarnings("static-method")
    @Test
    public final void speedContourTest() throws RemoteException, NetworkException, SimRuntimeException, NamingException
    {
        SpeedContourPlot scp = new SpeedContourPlot("Density", minimumDistance, maximumDistance);
        assertTrue("newly created DensityContourPlot should not be null", null != scp);
        assertEquals("SeriesKey should be \"speed\"", "speed", scp.getSeriesKey(0));
        standardContourTests(scp, Double.NaN, 50);
    }

    /**
     * Test various properties of a ContourPlot that has no observed data added.
     * @param cp ContourPlot; the ContourPlot to test
     * @param expectedZValue double; the value that getZ and getZValue should return for a valid item when no car has
     *            passed
     * @param expectedZValueWithTraffic double; the value that getZ and getZValue should return a valid item where a car
     *            has travelled at constant speed of 50 km/h. Supply Double.NaN if the value varies but differs from the
     *            value expected when no car has passed
     * @throws NetworkException
     * @throws RemoteException
     * @throws NamingException
     * @throws SimRuntimeException
     */
    public static void standardContourTests(final ContourPlot cp, final double expectedZValue,
            final double expectedZValueWithTraffic) throws NetworkException, RemoteException, SimRuntimeException,
            NamingException
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
                        .ceil((DoubleScalar.minus(maximumDistance, minimumDistance).getSI())
                                / ContourPlot.STANDARDDISTANCEGRANULARITIES[ContourPlot.STANDARDINITIALDISTANCEGRANULARITYINDEX]);
        assertEquals("yBins should be " + expectedYBins, expectedYBins, yBins);
        int bins = cp.getItemCount(0);
        assertEquals("Total bin count is product of xBins * yBins", xBins * yBins, bins);
        // Cache the String equivalents of minimumDistance and maximumDistance, INITIALLOWERTIMEBOUND and
        // INITUALUPPERTIMEBOUND
        String minimumDistanceString = minimumDistance.toString();
        String maximumDistanceString = maximumDistance.toString();
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
                expectedYBins =
                        (int) Math.ceil((DoubleScalar.minus(maximumDistance, minimumDistance).getSI())
                                / distanceGranularity);
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
                    assertTrue("Y should be >= " + minimumDistanceString, y >= minimumDistance.getSI());
                    assertTrue("Y should be <= " + maximumDistanceString, y <= maximumDistance.getSI());
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
        DoubleScalar.Abs<TimeUnit> initialTime = new DoubleScalar.Abs<TimeUnit>(100, TimeUnit.SECOND);
        DoubleScalar.Rel<LengthUnit> initialPosition = new DoubleScalar.Rel<LengthUnit>(20, LengthUnit.METER);
        DoubleScalar.Abs<SpeedUnit> initialSpeed = new DoubleScalar.Abs<SpeedUnit>(50, SpeedUnit.KM_PER_HOUR);
        Lane lane = CarTest.makeLane();
        OTSDEVSSimulator simulator = CarTest.makeSimulator();
        new ContourPlotTest().simulateUntil(initialTime, simulator);
        // Create a car running 50 km.h
        Car<Integer> car = CarTest.makeReferenceCar(0, lane, initialPosition, initialSpeed, simulator);
        // Make the car run at constant speed for one minute
        car.setState(new GTUFollowingModelResult(new DoubleScalar.Abs<AccelerationUnit>(0,
                AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Abs<TimeUnit>(initialTime.getSI() + 60,
                TimeUnit.SECOND)));
        // System.out.println("Car at start time " + car.getLastEvaluationTime() + " is at "
        // + car.getPosition(car.getLastEvaluationTime()));
        // System.out.println("Car at end time " + car.getNextEvaluationTime() + " is at "
        // + car.getPosition(car.getNextEvaluationTime()));
        cp.addData(car);
        // This car does not enter the area sampled in the first minute; check that the data in the ContourPlot does not
        // change
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
            assertTrue("Y should be >= " + minimumDistance, y >= minimumDistance.getSI());
            assertTrue("Y should be <= " + maximumDistance, y <= maximumDistance.getSI());
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
        // Make the car run at constant speed for another minute
        car.setState(new GTUFollowingModelResult(new DoubleScalar.Abs<AccelerationUnit>(0,
                AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Abs<TimeUnit>(car.getNextEvaluationTime()
                .getSI() + 60, TimeUnit.SECOND)));
        // System.out.println("Car at start time " + car.getLastEvaluationTime() + " is at "
        // + car.getPosition(car.getLastEvaluationTime()));
        // System.out.println("Car at end time " + car.getNextEvaluationTime() + " is at "
        // + car.getPosition(car.getNextEvaluationTime()));
        cp.addData(car);
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
            assertTrue("Y should be >= " + minimumDistance, y >= minimumDistance.getSI());
            assertTrue("Y should be <= " + maximumDistance, y <= maximumDistance.getSI());
            Number alternateY = cp.getY(0, item);
            assertEquals("getYValue and getY should return things that have the same value", y,
                    alternateY.doubleValue(), 0.000001);
            double z = cp.getZValue(0, item);
            // figure out if the car has traveled through this cell
            // if (x >= 180)
            // System.out.println(String.format("t=%.3f, x=%.3f z=%f, exp=%.3f, carLast=%s, carNext=%s", x, y, z,
            // expectedZValue, car.getLastEvaluationTime().getValueSI(), car.getNextEvaluationTime()
            // .getValueSI()));
            boolean hit = false;
            if (x + useTimeGranularity >= car.getLastEvaluationTime().getSI()
                    && x <= car.getNextEvaluationTime().getSI())
            {
                // the car MAY have hit contributed to this cell
                DoubleScalar.Abs<TimeUnit> cellStartTime =
                        new DoubleScalar.Abs<TimeUnit>(Math.max(car.getLastEvaluationTime().getSI(), x),
                                TimeUnit.SECOND);
                DoubleScalar.Abs<TimeUnit> cellEndTime =
                        new DoubleScalar.Abs<TimeUnit>(Math.min(car.getNextEvaluationTime().getSI(), x
                                + useTimeGranularity), TimeUnit.SECOND);
                if (car.position(lane, car.getFront(), cellStartTime).getSI() <= y + useDistanceGranularity
                        && car.position(lane, car.getFront(), cellEndTime).getSI() >= y)
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
                    {
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
        // Make the car run at constant speed for five more minutes
        car.setState(new GTUFollowingModelResult(new DoubleScalar.Abs<AccelerationUnit>(0,
                AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Abs<TimeUnit>(car.getNextEvaluationTime()
                .getSI() + 300, TimeUnit.SECOND)));
        cp.addData(car);
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
        double expectedHighestTime = Math.floor(carEndTime.getSI() / useTimeGranularity) * useTimeGranularity;
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

    /** Set to true when the stop event is executed by the simulator. */
    private volatile boolean stopped;

    /**
     * Run a simulator up to the specified stop time.
     * @param stopTime DoubleScalar.Abs&lt;TimeUnit&gt;; the stop time
     * @param simulator DEVSSimulatorInterface; the simulator
     */
    private void simulateUntil(DoubleScalar.Abs<TimeUnit> stopTime,
            DEVSSimulatorInterface<Abs<TimeUnit>, ?, ?> simulator)
    {
        this.stopped = false;
        try
        {
            simulator.scheduleEventAbs(stopTime, this, this, "stop", null);
        }
        catch (RemoteException | SimRuntimeException exception)
        {
            exception.printStackTrace();
        }
        while (!this.stopped)
        {
            try
            {
                simulator.step();
            }
            catch (RemoteException | SimRuntimeException exception)
            {
                exception.printStackTrace();
            }
        }
    }

    /**
     * Event for the simulator.
     */
    @SuppressWarnings("unused")
    private void stop()
    {
        this.stopped = true;
    }

    /**
     * Run the DensityContourPlot stand-alone for profiling
     * @param args
     * @throws RemoteException
     * @throws NetworkException
     * @throws SimRuntimeException
     * @throws NamingException
     */
    public static void main(final String[] args) throws RemoteException, NetworkException, SimRuntimeException,
            NamingException
    {
        ContourPlotTest cpt = new ContourPlotTest();
        System.out.println("Click the OK button");
        JOptionPane.showMessageDialog(null, "ContourPlot", "Start experiment", JOptionPane.INFORMATION_MESSAGE);
        System.out.println("Running ...");
        cpt.densityContourTest();
        System.out.println("Finished");
    }

}
