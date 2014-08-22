package org.opentrafficsim.graphs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;

import javax.swing.JLabel;
import javax.swing.JPanel;

import org.jfree.chart.ChartPanel;
import org.jfree.data.DomainOrder;
import org.junit.Test;
import org.opentrafficsim.car.Car;
import org.opentrafficsim.car.following.CarFollowingModel.CarFollowingModelResult;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarAbs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarRel;

/**
 * Test the non-GUI part of the ContourPlot class.
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
 * @version Aug 21, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class ContourPlotTest
{
    /** Lower bound of test distance range */
    static DoubleScalarAbs<LengthUnit> minimumDistance = new DoubleScalarAbs<LengthUnit>(1234, LengthUnit.METER);

    /** Upper bound of test distance range */
    static DoubleScalarAbs<LengthUnit> maximumDistance = new DoubleScalarAbs<LengthUnit>(12345, LengthUnit.METER);

    /**
     * Test the AccelerationContourPlot
     */
    @SuppressWarnings("static-method")
    @Test
    public void accelerationContourTest()
    {
        AccelerationContourPlot acp = new AccelerationContourPlot("Acceleration", minimumDistance, maximumDistance);
        assertTrue("newly created AccelerationContourPlot should not be null", null != acp);
        assertEquals("SeriesKey should be \"acceleration\"", "acceleration", acp.getSeriesKey(0));
        standardContourTests(acp, Double.NaN, 0);
    }

    /**
     * Test the DensityContourPlot
     */
    @SuppressWarnings("static-method")
    @Test
    public void densityContourTest()
    {
        DensityContourPlot dcp = new DensityContourPlot("Density", minimumDistance, maximumDistance);
        assertTrue("newly created DensityContourPlot should not be null", null != dcp);
        assertEquals("SeriesKey should be \"density\"", "density", dcp.getSeriesKey(0));
        standardContourTests(dcp, 0, Double.NaN);
    }

    /**
     * Test the FlowContourPlot
     */
    @SuppressWarnings("static-method")
    @Test
    public void flowContourTest()
    {
        FlowContourPlot fcp = new FlowContourPlot("Density", minimumDistance, maximumDistance);
        assertTrue("newly created DensityContourPlot should not be null", null != fcp);
        assertEquals("SeriesKey should be \"flow\"", "flow", fcp.getSeriesKey(0));
        standardContourTests(fcp, 0, Double.NaN);
    }

    /**
     * Test the SpeedContourPlot
     */
    @SuppressWarnings("static-method")
    @Test
    public void speedContourTest()
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
     */
    public static void standardContourTests(ContourPlot cp, double expectedZValue, double expectedZValueWithTraffic)
    {
        assertEquals("seriesCount should be 1", 1, cp.getSeriesCount());
        assertEquals("domainOrder should be ASCENDING", DomainOrder.ASCENDING, cp.getDomainOrder());
        assertEquals("indexOf always returns 0", 0, cp.indexOf(0));
        assertEquals("indexOf always returns 0", 0, cp.indexOf("abc"));
        assertEquals("getGroup always returns null", null, cp.getGroup());
        int xBins = cp.xAxisBins();
        int yBins = cp.yAxisBins();
        int expectedXBins =
                (int) Math.ceil((DoubleScalar.minus(ContourPlot.initialUpperTimeBound,
                        ContourPlot.initialLowerTimeBound).getValueSI())
                        / ContourPlot.standardTimeGranularities[ContourPlot.standardInitialTimeGranularityIndex]);
        assertEquals("Initial xBins should be " + expectedXBins, expectedXBins, xBins);
        int expectedYBins =
                (int) Math
                        .ceil((DoubleScalar.minus(maximumDistance, minimumDistance).getValueSI())
                                / ContourPlot.standardDistanceGranularities[ContourPlot.standardInitialDistanceGranularityIndex]);
        assertEquals("yBins should be " + expectedYBins, expectedYBins, yBins);
        int bins = cp.getItemCount(0);
        assertEquals("Total bin count is product of xBins * yBins", xBins * yBins, bins);
        // Vary the x granularity
        for (double timeGranularity : ContourPlot.standardTimeGranularities)
        {
            cp.actionPerformed(new ActionEvent(cp, 0, "setTimeGranularity " + timeGranularity));
            for (double distanceGranularity : ContourPlot.standardDistanceGranularities)
            {
                cp.actionPerformed(new ActionEvent(cp, 0, "setDistanceGranularity " + distanceGranularity));
                cp.reGraph();
                expectedXBins =
                        (int) Math.ceil((DoubleScalar.minus(ContourPlot.initialUpperTimeBound,
                                ContourPlot.initialLowerTimeBound).getValueSI()) / timeGranularity);
                xBins = cp.xAxisBins();
                assertEquals("Modified xBins should be " + expectedXBins, expectedXBins, xBins);
                expectedYBins =
                        (int) Math.ceil((DoubleScalar.minus(maximumDistance, minimumDistance).getValueSI())
                                / distanceGranularity);
                yBins = cp.yAxisBins();
                assertEquals("Modified yBins should be " + expectedYBins, expectedYBins, yBins);
                bins = cp.getItemCount(0);
                assertEquals("Total bin count is product of xBins * yBins", xBins * yBins, bins);
                for (int item = 0; item < bins; item++)
                {
                    double x = cp.getXValue(0, item);
                    assertTrue("X should be >= " + ContourPlot.initialLowerTimeBound,
                            x >= ContourPlot.initialLowerTimeBound.getValueSI());
                    assertTrue("X should be <= " + ContourPlot.initialUpperTimeBound,
                            x <= ContourPlot.initialUpperTimeBound.getValueSI());
                    Number alternateX = cp.getX(0, item);
                    assertEquals("getXValue and getX should return things that have the same value", x,
                            alternateX.doubleValue(), 0.000001);
                    double y = cp.getYValue(0, item);
                    assertTrue("Y should be >= " + minimumDistance, y >= minimumDistance.getValueSI());
                    assertTrue("Y should be <= " + maximumDistance, y <= maximumDistance.getValueSI());
                    Number alternateY = cp.getY(0, item);
                    assertEquals("getYValue and getY should return things that have the same value", y,
                            alternateY.doubleValue(), 0.000001);
                    double z = cp.getZValue(0, item);
                    if (Double.isNaN(expectedZValue))
                        assertTrue("Z value should be NaN", Double.isNaN(z));
                    else
                        assertEquals("Z value should be " + expectedZValue, expectedZValue, z, 0.0001);
                    Number alternateZ = cp.getZ(0, item);
                    if (Double.isNaN(expectedZValue))
                        assertTrue("Alternate Z value should be NaN", Double.isNaN(alternateZ.doubleValue()));
                    else
                        assertEquals("Alternate Z value should be " + expectedZValue, expectedZValue,
                                alternateZ.doubleValue(), 0.0000);
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
                ContourPlot.standardDistanceGranularities[ContourPlot.standardDistanceGranularities.length - 1];
        cp.actionPerformed(new ActionEvent(cp, 0, "setDistanceGranularity " + useDistanceGranularity));
        cp.reGraph();
        bins = cp.getItemCount(0);
        DoubleScalarAbs<TimeUnit> initialTime = new DoubleScalarAbs<TimeUnit>(100, TimeUnit.SECOND);
        DoubleScalarAbs<LengthUnit> initialPosition = new DoubleScalarAbs<LengthUnit>(20, LengthUnit.METER);
        DoubleScalarRel<SpeedUnit> initialSpeed = new DoubleScalarRel<SpeedUnit>(50, SpeedUnit.KM_PER_HOUR);
        // Create a car running 50 km.h
        Car car = new Car(0, null, null, initialTime, initialPosition, initialSpeed);
        // Make the car run at constant speed for one minute
        car.setState(new CarFollowingModelResult(new DoubleScalarAbs<AccelerationUnit>(0,
                AccelerationUnit.METER_PER_SECOND_2), new DoubleScalarAbs<TimeUnit>(initialTime.getValueSI() + 60,
                TimeUnit.SECOND), 0));
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
            assertTrue("X should be >= " + ContourPlot.initialLowerTimeBound,
                    x >= ContourPlot.initialLowerTimeBound.getValueSI());
            assertTrue("X should be <= " + ContourPlot.initialUpperTimeBound,
                    x <= ContourPlot.initialUpperTimeBound.getValueSI());
            Number alternateX = cp.getX(0, item);
            assertEquals("getXValue and getX should return things that have the same value", x,
                    alternateX.doubleValue(), 0.000001);
            double y = cp.getYValue(0, item);
            assertTrue("Y should be >= " + minimumDistance, y >= minimumDistance.getValueSI());
            assertTrue("Y should be <= " + maximumDistance, y <= maximumDistance.getValueSI());
            Number alternateY = cp.getY(0, item);
            assertEquals("getYValue and getY should return things that have the same value", y,
                    alternateY.doubleValue(), 0.000001);
            double z = cp.getZValue(0, item);
            if (Double.isNaN(expectedZValue))
                assertTrue("Z value should be NaN", Double.isNaN(z));
            else
                assertEquals("Z value should be " + expectedZValue, expectedZValue, z, 0.0001);
            Number alternateZ = cp.getZ(0, item);
            if (Double.isNaN(expectedZValue))
                assertTrue("Alternate Z value should be NaN", Double.isNaN(alternateZ.doubleValue()));
            else
                assertEquals("Alternate Z value should be " + expectedZValue, expectedZValue, alternateZ.doubleValue(),
                        0.0000);
        }
        // Make the car run at constant speed for another minute
        car.setState(new CarFollowingModelResult(new DoubleScalarAbs<AccelerationUnit>(0,
                AccelerationUnit.METER_PER_SECOND_2), new DoubleScalarAbs<TimeUnit>(car.getNextEvaluationTime()
                .getValueSI() + 60, TimeUnit.SECOND), 0));
        // System.out.println("Car at start time " + car.getLastEvaluationTime() + " is at "
        // + car.getPosition(car.getLastEvaluationTime()));
        // System.out.println("Car at end time " + car.getNextEvaluationTime() + " is at "
        // + car.getPosition(car.getNextEvaluationTime()));
        cp.addData(car);
        for (int item = 0; item < bins; item++)
        {
            double x = cp.getXValue(0, item);
            assertTrue("X should be >= " + ContourPlot.initialLowerTimeBound,
                    x >= ContourPlot.initialLowerTimeBound.getValueSI());
            assertTrue("X should be <= " + ContourPlot.initialUpperTimeBound,
                    x <= ContourPlot.initialUpperTimeBound.getValueSI());
            Number alternateX = cp.getX(0, item);
            assertEquals("getXValue and getX should return things that have the same value", x,
                    alternateX.doubleValue(), 0.000001);
            double y = cp.getYValue(0, item);
            assertTrue("Y should be >= " + minimumDistance, y >= minimumDistance.getValueSI());
            assertTrue("Y should be <= " + maximumDistance, y <= maximumDistance.getValueSI());
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
            if (x + useTimeGranularity >= car.getLastEvaluationTime().getValueSI()
                    && x <= car.getNextEvaluationTime().getValueSI())
            {
                // the car MAY have hit contributed to this cell
                DoubleScalarAbs<TimeUnit> cellStartTime =
                        new DoubleScalarAbs<TimeUnit>(Math.max(car.getLastEvaluationTime().getValueSI(), x),
                                TimeUnit.SECOND);
                DoubleScalarAbs<TimeUnit> cellEndTime =
                        new DoubleScalarAbs<TimeUnit>(Math.min(car.getNextEvaluationTime().getValueSI(), x
                                + useTimeGranularity), TimeUnit.SECOND);
                if (car.getPosition(cellStartTime).getValueSI() <= y + useDistanceGranularity
                        && car.getPosition(cellEndTime).getValueSI() >= y)
                    hit = true;
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
                        assertFalse("Z value should not be NaN", Double.isNaN(z));
                }
            }
            else
            {
                if (Double.isNaN(expectedZValue))
                    assertTrue("Z value should be NaN", Double.isNaN(z));
                else
                    assertEquals("Z value should be " + expectedZValue, expectedZValue, z, 0.0001);
                if (Double.isNaN(expectedZValue))
                    assertTrue("Alternate Z value should be NaN", Double.isNaN(alternateZ.doubleValue()));
                else
                    assertEquals("Alternate Z value should be " + expectedZValue, expectedZValue,
                            alternateZ.doubleValue(), 0.0000);
            }
        }
        // Make the car run at constant speed for five more minutes
        car.setState(new CarFollowingModelResult(new DoubleScalarAbs<AccelerationUnit>(0,
                AccelerationUnit.METER_PER_SECOND_2), new DoubleScalarAbs<TimeUnit>(car.getNextEvaluationTime()
                .getValueSI() + 300, TimeUnit.SECOND), 0));
        cp.addData(car);
        // Check that the time range has expanded
        xBins = cp.xAxisBins();
        bins = cp.getItemCount(0);
        double observedHighestTime = Double.MIN_VALUE;
        for (int bin = 0; bin < bins; bin++)
        {
            double xValue = cp.getXValue(0, bin);
            if (xValue > observedHighestTime)
                observedHighestTime = xValue;
        }
        DoubleScalarAbs<TimeUnit> carEndTime = car.getNextEvaluationTime();
        double expectedHighestTime = Math.floor(carEndTime.getValueSI() / useTimeGranularity) * useTimeGranularity;
        assertEquals("Time range should run up to " + expectedHighestTime, expectedHighestTime, observedHighestTime,
                0.0001);
        // Check the updateHint method in the PointerHandler
        // First get the panel that stores the result of updateHint (this is ugly)
        JLabel hintPanel = null;
        ChartPanel chartPanel = null;
        for (Component c0 : cp.getComponents())
            for (Component c1 : ((Container) c0).getComponents())
                if (c1 instanceof Container)
                    for (Component c2 : ((Container) c1).getComponents())
                    {
                        //System.out.println("c2 is " + c2);
                        if (c2 instanceof Container)
                            for (Component c3 : ((Container) c2).getComponents())
                            {
                                //System.out.println("c3 is " + c3);
                                if (c3 instanceof JLabel)
                                    if (null == hintPanel)
                                        hintPanel = (JLabel) c3;
                                    else
                                        fail("There should be only one JPanel in a ContourPlot");
                                if (c3 instanceof ChartPanel)
                                    if (null == chartPanel)
                                        chartPanel = (ChartPanel) c3;
                                    else
                                        fail("There should be only one ChartPanel in a ContourPlot");
                            }
                    }
        if (null == hintPanel)
            fail("Could not find a JLabel in ContourPlot");
        if (null == chartPanel)
            fail("Could not find a ChartPanel in ContourPlot");
        assertEquals("Initially the text should be a single space", " ", hintPanel.getText());
        PointerHandler ph = null;
        for (MouseListener ml : chartPanel.getMouseListeners())
            if (ml instanceof PointerHandler)
                if (null == ph)
                    ph = (PointerHandler) ml;
                else
                    fail("There should be only one PointerHandler on the chartPanel");
        if (null == ph)
            fail("Could not find the PointerHandler for the chartPanel");
        ph.updateHint(1, 2);
        //System.out.println("Hint text is now " + hintPanel.getText());
        assertFalse("Hint should not be a single space", " ".equals(hintPanel.getText()));
        ph.updateHint(Double.NaN, Double.NaN);
        assertEquals("The text should again be a single space", " ", hintPanel.getText());
    }
}
