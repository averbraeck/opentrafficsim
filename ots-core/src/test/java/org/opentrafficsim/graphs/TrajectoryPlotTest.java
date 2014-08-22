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
import org.opentrafficsim.car.following.CarFollowingModel.CarFollowingModelResult;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarAbs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalarRel;

/**
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
 * @version Aug 22, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class TrajectoryPlotTest
{
    /** Sample interval for the TrajectoryPlot */
    DoubleScalarRel<TimeUnit> sampleInterval = new DoubleScalarRel<TimeUnit>(0.25, TimeUnit.SECOND);

    /**
     * Test the TrajectoryPlot
     */
    @SuppressWarnings("static-method")
    @Test
    public void TrajectoryTest()
    {
        DoubleScalarAbs<LengthUnit> minimumDistance = new DoubleScalarAbs<LengthUnit>(1234, LengthUnit.METER);
        DoubleScalarAbs<LengthUnit> maximumDistance = new DoubleScalarAbs<LengthUnit>(12345, LengthUnit.METER);

        TrajectoryPlot tp = new TrajectoryPlot("Trajectory", this.sampleInterval, minimumDistance, maximumDistance);
        assertTrue("newly created DensityContourPlot should not be null", null != tp);
        assertEquals("Number of trajectories should initially be 0", 0, tp.getSeriesCount());
        for (int i = -10; i <= 10; i++)
            assertEquals("SeriesKey(" + i + ") should return " + i, i, tp.getSeriesKey(i));
        assertEquals("Domain order should be ASCENDING", DomainOrder.ASCENDING, tp.getDomainOrder());
        DoubleScalarAbs<TimeUnit> initialTime = new DoubleScalarAbs<TimeUnit>(100, TimeUnit.SECOND);
        DoubleScalarAbs<LengthUnit> initialPosition = new DoubleScalarAbs<LengthUnit>(2000, LengthUnit.METER);
        DoubleScalarRel<SpeedUnit> initialSpeed = new DoubleScalarRel<SpeedUnit>(50, SpeedUnit.KM_PER_HOUR);
        // Create a car running 50 km.h
        Car car = new Car(1, null, null, initialTime, initialPosition, initialSpeed);
        // Make the car accelerate with constant acceleration of 0.05 m/s/s for 500 seconds
        DoubleScalarAbs<TimeUnit> endTime =
                new DoubleScalarAbs<TimeUnit>(initialTime.getValueSI() + 400, TimeUnit.SECOND);
        car.setState(new CarFollowingModelResult(new DoubleScalarAbs<AccelerationUnit>(0.05,
                AccelerationUnit.METER_PER_SECOND_2), endTime, 0));
        //System.out.println("Car end position " + car.getPosition(car.getNextEvaluationTime()));
        tp.addData(car);
        assertEquals("Number of trajectories should now be 1", 1, tp.getSeriesCount());
        verifyTrajectory(car, 0, tp);
        initialTime = new DoubleScalarAbs<TimeUnit>(150, TimeUnit.SECOND);
        Car secondCar = new Car(2, null, null, initialTime, initialPosition, initialSpeed);
        // Make the second car accelerate with constant acceleration of 0.03 m/s/s for 500 seconds
        secondCar.setState(new CarFollowingModelResult(new DoubleScalarAbs<AccelerationUnit>(0.03,
                AccelerationUnit.METER_PER_SECOND_2), endTime, 0));
        //System.out.println("Second car end position " + car.getPosition(secondCar.getNextEvaluationTime()));
        tp.addData(secondCar);
        assertEquals("Number of trajectories should now be 2", 2, tp.getSeriesCount());
        verifyTrajectory(car, 0, tp); // first car trajectory should not change by adding the second
        verifyTrajectory(secondCar, 1, tp);
        // Check the updateHint method in the PointerHandler
        // First get the panel that stores the result of updateHint (this is ugly)
        JLabel hintPanel = null;
        ChartPanel chartPanel = null;
        for (Component c0 : tp.getComponents())
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

    /**
     * Verify that a sampled trajectory matches the actual trajectory.
     * @param car Car; the car whose trajectory was sampled
     * @param series Integer; the series in the TrajectoryPlot that should correspond to the car
     * @param tp TrajectoryPlot; the TrajectoryPlot that contains the samples
     */
    private void verifyTrajectory(Car car, int series, TrajectoryPlot tp)
    {
        DoubleScalarAbs<TimeUnit> initialTime = car.getLastEvaluationTime();
        DoubleScalarRel<TimeUnit> duration =
                DoubleScalar.minus(car.getNextEvaluationTime(), car.getLastEvaluationTime());
        int expectedNumberOfSamples = (int) (duration.getValueSI() / this.sampleInterval.getValueSI());
        assertEquals("Number of samples in trajectory should be ", expectedNumberOfSamples, tp.getItemCount(series));
        // Check that the stored trajectory accurately matches the trajectory of the car at all sampling times
        for (int sample = 0; sample < expectedNumberOfSamples; sample++)
        {
            DoubleScalarRel<TimeUnit> deltaTime =
                    new DoubleScalarRel<TimeUnit>(this.sampleInterval.getValueSI() * sample, TimeUnit.SECOND);
            DoubleScalarAbs<TimeUnit> sampleTime = DoubleScalar.plus(initialTime, deltaTime);
            double sampledTime = tp.getXValue(series, sample);
            assertEquals("Sample should have been taken at " + sampleTime, sampleTime.getValueSI(), sampledTime,
                    0.0001);
            sampledTime = tp.getX(series, sample).doubleValue();
            assertEquals("Sample should have been taken at " + sampleTime, sampleTime.getValueSI(), sampledTime,
                    0.0001);
            DoubleScalarAbs<LengthUnit> actualPosition = car.getPosition(sampleTime);
            double sampledPosition = tp.getYValue(series, sample);
            assertEquals("Sample position should have been " + actualPosition, actualPosition.getValueSI(),
                    sampledPosition, 0.0001);
            sampledPosition = tp.getY(series, sample).doubleValue();
            assertEquals("Sample position should have been " + actualPosition, actualPosition.getValueSI(),
                    sampledPosition, 0.0001);
        }
    }

}
