package org.opentrafficsim.graphs;

import static org.junit.Assert.assertEquals;

import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.Test;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.car.LaneBasedIndividualCar;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version Aug 22, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class TrajectoryPlotTest implements UNITS 
{
    /** Sample interval for the TrajectoryPlot. */
    Time.Rel sampleInterval = new Time.Rel(0.25, SECOND);

    /**
     * Test the TrajectoryPlot.
     * @throws Exception which should not happen, but will be treated as an error by the JUnit framework if it does
     */
    @Test
    public final void trajectoryTest() throws Exception
    {
        Length.Rel minimumDistance = new Length.Rel(1234, METER);
        Length.Rel maximumDistance = new Length.Rel(12345, METER);

        // TODO adapt to new path (List<Lane>) concept
        /*-
        TrajectoryPlot tp = new TrajectoryPlot("Trajectory", this.sampleInterval, minimumDistance, maximumDistance);
        assertTrue("newly created DensityContourPlot should not be null", null != tp);
        assertEquals("Number of trajectories should initially be 0", 0, tp.getSeriesCount());
        for (int i = -10; i <= 10; i++)
        {
            assertEquals("SeriesKey(" + i + ") should return " + i, i, tp.getSeriesKey(i));
        }
        assertEquals("Domain order should be ASCENDING", DomainOrder.ASCENDING, tp.getDomainOrder());
        // Create a car running 50 km.h
        Length.Rel initialPosition = new Length.Rel(2000, METER);
        Speed initialSpeed = new Speed(50, KM_PER_HOUR);
        GTUType carType = new GTUType("Car");
        Length.Rel length = new Length.Rel(5.0, METER);
        Length.Rel width = new Length.Rel(2.0, METER);
        Map<Lane, Length.Rel> initialLongitudinalPositions = new HashMap<>();
        Lane lane = CarTest.makeLane();
        initialLongitudinalPositions.put(lane, initialPosition);
        OTSDEVSSimulator simulator = CarTest.makeSimulator();
        // We want to start the car simulation at t=100s; therefore we have to advance the simulator up to that time.
        simulateUntil(new Time.Abs(100, SECOND), simulator);
        Speed maxSpeed = new Speed(120, KM_PER_HOUR);
        Car car =
            new Car(12345, carType, null, initialLongitudinalPositions, initialSpeed, length, width, maxSpeed,
                simulator);
        // Make the car accelerate with constant acceleration of 0.05 m/s/s for 400 seconds
        Time.Rel duration = new Time.Rel(400, SECOND);
        Time.Abs endTime = DoubleScalar.plus(simulator.getSimulatorTime().getTime(), duration);
        car.setState(new GTUFollowingModelResult(new Acceleration(0.05,
            METER_PER_SECOND_2), endTime));
        // System.out.println("Car end position " + car.getPosition(car.getNextEvaluationTime()));
        tp.addData(car);
        assertEquals("Number of trajectories should now be 1", 1, tp.getSeriesCount());
        verifyTrajectory(car, 0, tp);
        simulateUntil(new Time.Abs(150, SECOND), simulator);
        Car secondCar =
            new Car(2, carType, null, initialLongitudinalPositions, initialSpeed, length, width, maxSpeed,
                simulator);
        // Make the second car accelerate with constant acceleration of 0.03 m/s/s for 500 seconds
        secondCar.setState(new GTUFollowingModelResult(new Acceleration(0.03,
            METER_PER_SECOND_2), endTime));
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
         */
    }

    /**
     * Verify that a sampled trajectory matches the actual trajectory.
     * @param car Car; the car whose trajectory was sampled
     * @param series Integer; the series in the TrajectoryPlot that should correspond to the car
     * @param tp TrajectoryPlot; the TrajectoryPlot that contains the samples
     * @throws NetworkException when car is not on lane anymore
     */
    private void verifyTrajectory(final LaneBasedIndividualCar car, final int series, final TrajectoryPlot tp)
        throws NetworkException
    {
        // XXX we take the first (and only) lane on which the vehicle is registered.
        Lane lane = car.positions(car.getFront()).keySet().iterator().next();
        Time.Abs initialTime = car.getOperationalPlan().getStartTime();
        Time.Rel duration = car.getOperationalPlan().getTotalDuration();
        int expectedNumberOfSamples = (int) (duration.getSI() / this.sampleInterval.getSI());
        assertEquals("Number of samples in trajectory should be ", expectedNumberOfSamples, tp.getItemCount(series));
        // Check that the stored trajectory accurately matches the trajectory of the car at all sampling times
        for (int sample = 0; sample < expectedNumberOfSamples; sample++)
        {
            Time.Rel deltaTime = new Time.Rel(this.sampleInterval.getSI() * sample, SECOND);
            Time.Abs sampleTime = initialTime.plus(deltaTime);
            double sampledTime = tp.getXValue(series, sample);
            assertEquals("Sample should have been taken at " + sampleTime, sampleTime.getSI(), sampledTime, 0.0001);
            sampledTime = tp.getX(series, sample).doubleValue();
            assertEquals("Sample should have been taken at " + sampleTime, sampleTime.getSI(), sampledTime, 0.0001);
            Length.Rel actualPosition = car.position(lane, car.getFront(), sampleTime);
            double sampledPosition = tp.getYValue(series, sample);
            assertEquals("Sample position should have been " + actualPosition, actualPosition.getSI(), sampledPosition,
                0.0001);
            sampledPosition = tp.getY(series, sample).doubleValue();
            assertEquals("Sample position should have been " + actualPosition, actualPosition.getSI(), sampledPosition,
                0.0001);
        }
    }

}
