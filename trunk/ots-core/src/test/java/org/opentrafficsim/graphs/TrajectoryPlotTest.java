package org.opentrafficsim.graphs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseListener;

import javax.swing.JLabel;

import org.jfree.chart.ChartPanel;
import org.jfree.data.DomainOrder;
import org.junit.Test;
import org.opentrafficsim.car.Car;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel.GTUFollowingModelResult;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.MutableDoubleScalar;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Aug 22, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class TrajectoryPlotTest
{
    /** Sample interval for the TrajectoryPlot. */
    DoubleScalar.Rel<TimeUnit> sampleInterval = new DoubleScalar.Rel<TimeUnit>(0.25, TimeUnit.SECOND);

    /**
     * Test the TrajectoryPlot.
     */
    @Test
    public final void trajectoryTest()
    {
        DoubleScalar.Abs<LengthUnit> minimumDistance = new DoubleScalar.Abs<LengthUnit>(1234, LengthUnit.METER);
        DoubleScalar.Abs<LengthUnit> maximumDistance = new DoubleScalar.Abs<LengthUnit>(12345, LengthUnit.METER);

        TrajectoryPlot tp = new TrajectoryPlot("Trajectory", this.sampleInterval, minimumDistance, maximumDistance);
        assertTrue("newly created DensityContourPlot should not be null", null != tp);
        assertEquals("Number of trajectories should initially be 0", 0, tp.getSeriesCount());
        for (int i = -10; i <= 10; i++)
        {
            assertEquals("SeriesKey(" + i + ") should return " + i, i, tp.getSeriesKey(i));
        }
        assertEquals("Domain order should be ASCENDING", DomainOrder.ASCENDING, tp.getDomainOrder());
        DoubleScalar.Abs<TimeUnit> initialTime = new DoubleScalar.Abs<TimeUnit>(100, TimeUnit.SECOND);
        DoubleScalar.Abs<LengthUnit> initialPosition = new DoubleScalar.Abs<LengthUnit>(2000, LengthUnit.METER);
        DoubleScalar.Rel<SpeedUnit> initialSpeed = new DoubleScalar.Rel<SpeedUnit>(50, SpeedUnit.KM_PER_HOUR);
        // Create a car running 50 km.h
        Car car = new Car(1, null, null, initialTime, initialPosition, initialSpeed);
        // Make the car accelerate with constant acceleration of 0.05 m/s/s for 500 seconds
        DoubleScalar.Abs<TimeUnit> endTime = new DoubleScalar.Abs<TimeUnit>(initialTime.getSI() + 400, TimeUnit.SECOND);
        car.setState(new GTUFollowingModelResult(new DoubleScalar.Abs<AccelerationUnit>(0.05,
                AccelerationUnit.METER_PER_SECOND_2), endTime, 0));
        // System.out.println("Car end position " + car.getPosition(car.getNextEvaluationTime()));
        tp.addData(car);
        assertEquals("Number of trajectories should now be 1", 1, tp.getSeriesCount());
        verifyTrajectory(car, 0, tp);
        initialTime = new DoubleScalar.Abs<TimeUnit>(150, TimeUnit.SECOND);
        Car secondCar = new Car(2, null, null, initialTime, initialPosition, initialSpeed);
        // Make the second car accelerate with constant acceleration of 0.03 m/s/s for 500 seconds
        secondCar.setState(new GTUFollowingModelResult(new DoubleScalar.Abs<AccelerationUnit>(0.03,
                AccelerationUnit.METER_PER_SECOND_2), endTime, 0));
        // System.out.println("Second car end position " + car.getPosition(secondCar.getNextEvaluationTime()));
        tp.addData(secondCar);
        assertEquals("Number of trajectories should now be 2", 2, tp.getSeriesCount());
        verifyTrajectory(car, 0, tp); // first car trajectory should not change by adding the second
        verifyTrajectory(secondCar, 1, tp);
        // Check the updateHint method in the PointerHandler
        // First get the panel that stores the result of updateHint (this is ugly)
        JLabel hintPanel = null;
        ChartPanel chartPanel = null;
        for (Component c0 : tp.getComponents())
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
     * Verify that a sampled trajectory matches the actual trajectory.
     * @param car Car; the car whose trajectory was sampled
     * @param series Integer; the series in the TrajectoryPlot that should correspond to the car
     * @param tp TrajectoryPlot; the TrajectoryPlot that contains the samples
     */
    private void verifyTrajectory(final Car car, final int series, final TrajectoryPlot tp)
    {
        DoubleScalar.Abs<TimeUnit> initialTime = car.getLastEvaluationTime();
        DoubleScalar.Rel<TimeUnit> duration =
                MutableDoubleScalar.minus(car.getNextEvaluationTime(), car.getLastEvaluationTime()).immutable();
        int expectedNumberOfSamples = (int) (duration.getSI() / this.sampleInterval.getSI());
        assertEquals("Number of samples in trajectory should be ", expectedNumberOfSamples, tp.getItemCount(series));
        // Check that the stored trajectory accurately matches the trajectory of the car at all sampling times
        for (int sample = 0; sample < expectedNumberOfSamples; sample++)
        {
            DoubleScalar.Rel<TimeUnit> deltaTime =
                    new DoubleScalar.Rel<TimeUnit>(this.sampleInterval.getSI() * sample, TimeUnit.SECOND);
            DoubleScalar.Abs<TimeUnit> sampleTime = MutableDoubleScalar.plus(initialTime, deltaTime).immutable();
            double sampledTime = tp.getXValue(series, sample);
            assertEquals("Sample should have been taken at " + sampleTime, sampleTime.getSI(), sampledTime, 0.0001);
            sampledTime = tp.getX(series, sample).doubleValue();
            assertEquals("Sample should have been taken at " + sampleTime, sampleTime.getSI(), sampledTime, 0.0001);
            DoubleScalar.Abs<LengthUnit> actualPosition = car.getPosition(sampleTime);
            double sampledPosition = tp.getYValue(series, sample);
            assertEquals("Sample position should have been " + actualPosition, actualPosition.getSI(), sampledPosition,
                    0.0001);
            sampledPosition = tp.getY(series, sample).doubleValue();
            assertEquals("Sample position should have been " + actualPosition, actualPosition.getSI(), sampledPosition,
                    0.0001);
        }
    }

}
