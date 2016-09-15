package org.opentrafficsim.graphs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.MouseListener;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.media.j3d.Bounds;
import javax.swing.JLabel;

import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEvent;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.event.EventType;
import nl.tudelft.simulation.language.d3.DirectedPoint;

import org.djunits.unit.SpeedUnit;
import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.DoubleScalar;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.jfree.chart.ChartPanel;
import org.jfree.data.DomainOrder;
import org.junit.Test;
import org.opentrafficsim.core.dsol.OTSDEVSSimulator;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.geometry.OTSPoint3D;
import org.opentrafficsim.core.gtu.AbstractGTU;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.RelativePosition.TYPE;
import org.opentrafficsim.core.gtu.TurnIndicatorStatus;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.gtu.plan.operational.OperationalPlan;
import org.opentrafficsim.core.idgenerator.IdGenerator;
import org.opentrafficsim.core.network.LongitudinalDirectionality;
import org.opentrafficsim.core.network.OTSLink;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.core.network.OTSNode;
import org.opentrafficsim.core.perception.PerceivableContext;
import org.opentrafficsim.road.car.CarTest;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.FixedAccelerationModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.Egoistic;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.LaneChangeModel;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.network.factory.LaneFactory;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.simulationengine.SimpleAnimator;
import org.opentrafficsim.simulationengine.SimpleSimulatorInterface;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version Aug 22, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class TrajectoryPlotTest implements UNITS
{
    /** Sample interval for the TrajectoryPlot. */
    Duration sampleInterval = new Duration(0.25, SECOND);

    /**
     * Test the TrajectoryPlot.
     * @throws Exception which should not happen, but will be treated as an error by the JUnit framework if it does
     */
    @Test
    public final void trajectoryTest() throws Exception
    {
        OTSDEVSSimulator simulator = CarTest.makeSimulator();
        GTUType gtuType = new GTUType("Car");
        Set<GTUType> gtuTypes = new HashSet<GTUType>();
        gtuTypes.add(gtuType);
        LaneType laneType = new LaneType("CarLane", gtuTypes);
        OTSNetwork network = new OTSNetwork("trajectory plot test network");
        OTSNode node1 = new OTSNode(network, "node 1", new OTSPoint3D(100, 100, 0));
        OTSNode node2 = new OTSNode(network, "node 2", new OTSPoint3D(1100, 100, 0)); 
        OTSNode node3 = new OTSNode(network, "node 3", new OTSPoint3D(10100, 100, 0)); 
        List<Lane> trajectory = new ArrayList<Lane>();
        Speed speedLimit = new Speed(50, SpeedUnit.KM_PER_HOUR);
        Lane lane1 =
                LaneFactory.makeMultiLane(network, "12", node1, node2, null, 1, 0, 0, laneType, speedLimit, simulator,
                        LongitudinalDirectionality.DIR_PLUS)[0];
        trajectory.add(lane1);
        Lane lane2 =
                LaneFactory.makeMultiLane(network, "23", node2, node3, null, 1, 0, 0, laneType, speedLimit, simulator,
                        LongitudinalDirectionality.DIR_PLUS)[0];
        trajectory.add(lane2);
        TrajectoryPlot tp = new TrajectoryPlot("TestTrajectory", this.sampleInterval, trajectory, simulator);
        assertEquals("Number of trajectories should initially be 0", 0, tp.getSeriesCount());
        for (int i = -10; i <= 10; i++)
        {
            assertEquals("SeriesKey(" + i + ") should return " + i, i, tp.getSeriesKey(i));
        }
        assertEquals("Domain order should be ASCENDING", DomainOrder.ASCENDING, tp.getDomainOrder());
        // Create a car running 50 km.h
        Length initialPosition = new Length(200, METER);
        Speed initialSpeed = new Speed(50, KM_PER_HOUR);
        Length length = new Length(5.0, METER);
        Length width = new Length(2.0, METER);
        Map<Lane, Length> initialLongitudinalPositions = new HashMap<>();
        initialLongitudinalPositions.put(lane1, initialPosition);
        // We want to start the car simulation at t=100s; therefore we have to advance the simulator up to that time.
        simulator.runUpTo(new Time(100, SECOND));
        Speed maxSpeed = new Speed(120, KM_PER_HOUR);
        GTUFollowingModelOld gtuFollowingModel =
                new FixedAccelerationModel(new Acceleration(0, METER_PER_SECOND_2), new Duration(10, SECOND));
        LaneChangeModel laneChangeModel = new Egoistic();
        LaneBasedIndividualGTU car =
                CarTest.makeReferenceCar("12345", gtuType, lane1, initialPosition, initialSpeed, simulator, gtuFollowingModel,
                        laneChangeModel, network);
        // Make the car accelerate with constant acceleration of 0.05 m/s/s for 400 seconds
        Duration duration = new Duration(400, SECOND);
        Time endTime = simulator.getSimulatorTime().getTime().plus(duration);
        car.setState(new GTUFollowingModelResult(new Acceleration(0.05, METER_PER_SECOND_2), endTime));
        // System.out.println("Car end position " + car.getPosition(car.getNextEvaluationTime()));
        tp.addData(car);
        assertEquals("Number of trajectories should now be 1", 1, tp.getSeriesCount());
        verifyTrajectory(car, 0, tp);
        simulateUntil(new Time(150, SECOND), simulator);
        Car secondCar =
                new Car(2, carType, null, initialLongitudinalPositions, initialSpeed, length, width, maxSpeed, simulator);
        // Make the second car accelerate with constant acceleration of 0.03 m/s/s for 500 seconds
        secondCar.setState(new GTUFollowingModelResult(new Acceleration(0.03, METER_PER_SECOND_2), endTime));
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
     * @throws GTUException when car is not on lane anymore
     */
    private void verifyTrajectory(final LaneBasedIndividualGTU car, final int series, final TrajectoryPlot tp)
            throws GTUException
    {
        // XXX we take the first (and only) lane on which the vehicle is registered.
        Lane lane = car.positions(car.getFront()).keySet().iterator().next();
        Time initialTime = car.getOperationalPlan().getStartTime();
        Duration duration = car.getOperationalPlan().getTotalDuration();
        int expectedNumberOfSamples = (int) (duration.getSI() / this.sampleInterval.getSI());
        assertEquals("Number of samples in trajectory should be ", expectedNumberOfSamples, tp.getItemCount(series));
        // Check that the stored trajectory accurately matches the trajectory of the car at all sampling times
        for (int sample = 0; sample < expectedNumberOfSamples; sample++)
        {
            Duration deltaTime = new Duration(this.sampleInterval.getSI() * sample, SECOND);
            Time sampleTime = initialTime.plus(deltaTime);
            double sampledTime = tp.getXValue(series, sample);
            assertEquals("Sample should have been taken at " + sampleTime, sampleTime.getSI(), sampledTime, 0.0001);
            sampledTime = tp.getX(series, sample).doubleValue();
            assertEquals("Sample should have been taken at " + sampleTime, sampleTime.getSI(), sampledTime, 0.0001);
            Length actualPosition = car.position(lane, car.getFront(), sampleTime);
            double sampledPosition = tp.getYValue(series, sample);
            assertEquals("Sample position should have been " + actualPosition, actualPosition.getSI(), sampledPosition, 0.0001);
            sampledPosition = tp.getY(series, sample).doubleValue();
            assertEquals("Sample position should have been " + actualPosition, actualPosition.getSI(), sampledPosition, 0.0001);
        }
    }
    

}
