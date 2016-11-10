package org.opentrafficsim.graphs;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.MouseListener;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Set;

import javax.swing.JLabel;

import org.djunits.unit.UNITS;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.jfree.chart.ChartPanel;
import org.junit.Test;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.BehavioralCharacteristics;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.DefaultTestParameters;
import org.opentrafficsim.road.car.CarTest;
import org.opentrafficsim.road.gtu.lane.LaneBasedIndividualGTU;
import org.opentrafficsim.road.gtu.lane.tactical.LaneBasedCFLCTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.following.FixedAccelerationModel;
import org.opentrafficsim.road.gtu.lane.tactical.following.GTUFollowingModelOld;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.Egoistic;
import org.opentrafficsim.road.gtu.lane.tactical.lanechangemobil.LaneChangeModel;
import org.opentrafficsim.road.gtu.strategical.LaneBasedStrategicalPlanner;
import org.opentrafficsim.road.gtu.strategical.route.LaneBasedStrategicalRoutePlanner;
import org.opentrafficsim.road.network.lane.DirectedLanePosition;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneType;
import org.opentrafficsim.road.network.lane.object.sensor.SinkSensor;
import org.opentrafficsim.simulationengine.SimpleSimulator;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

/**
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-09-14 01:33:02 +0200 (Mon, 14 Sep 2015) $, @version $Revision: 1401 $, by $Author: averbraeck $,
 * initial version Aug 25, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class FundamentalDiagramPlotTest implements OTSModelInterface, UNITS
{
    /** */
    private static final long serialVersionUID = 20150226L;

    /** The network. */
    private OTSNetwork network = new OTSNetwork("network");
    
    /** the simulator. */
    private OTSDEVSSimulatorInterface simulator;

    /**
     * Test the FundamentalDiagram.
     * @throws Exception when something goes wrong (should not happen)
     */
    @SuppressWarnings("static-method")
    @Test
    public final void fundamentalDiagramTest() throws Exception
    {
        this.simulator =
                new SimpleSimulator(new Time(0, SECOND), new Duration(0, SECOND), new Duration(1800, SECOND), this);

        Duration aggregationTime = new Duration(30, SECOND);
        Length position = new Length(123, METER);
        Length carPosition = new Length(122.5, METER);
        GTUType gtuType = new GTUType("Car");
        Set<GTUType> compatibility = new HashSet<GTUType>();
        compatibility.add(gtuType);
        LaneType laneType = new LaneType("CarLane", compatibility);
        Lane lane = CarTest.makeLane(this.network, laneType);
        FundamentalDiagram fd = new FundamentalDiagram("Fundamental Diagram", aggregationTime, lane, position, this.simulator);
        assertEquals("SeriesCount should match numberOfLanes", 1, fd.getSeriesCount());
        assertEquals("Position should match the supplied position", position.getSI(), fd.getPosition().getSI(), 0.0001);
        try
        {
            fd.getXValue(-1, 0);
            fail("Bad series should have thrown an Error");
        }
        catch (Error e)
        {
            // Ignore
        }
        try
        {
            fd.getXValue(1, 0);
            fail("Bad series should have thrown an Error");
        }
        catch (Error e)
        {
            // Ignore
        }
        double value = fd.getXValue(0, 0);
        assertTrue("No data should result in NaN", java.lang.Double.isNaN(value));
        value = fd.getX(0, 0).doubleValue();
        assertTrue("No data should result in NaN", java.lang.Double.isNaN(value));
        value = fd.getYValue(0, 0);
        assertTrue("No data should result in NaN", java.lang.Double.isNaN(value));
        value = fd.getY(0, 0).doubleValue();
        assertTrue("No data should result in NaN", java.lang.Double.isNaN(value));
        ActionEvent setXToSpeed = new ActionEvent(fd, 0, "Speed/Speed");
        ActionEvent resetAxis = new ActionEvent(fd, 0, "Flow/Density");
        Speed speed = new Speed(100, KM_PER_HOUR);
        Time time = new Time(123, SECOND);
        Length length = new Length(5.0, METER);
        Length width = new Length(2.0, METER);
        Speed maxSpeed = new Speed(120, KM_PER_HOUR);
        Set<DirectedLanePosition> initialLongitudinalPositions = new LinkedHashSet<>(1);
        initialLongitudinalPositions.add(new DirectedLanePosition(lane, carPosition, GTUDirectionality.DIR_PLUS));

        // add a sink 100 meter before the end of the lane.
        new SinkSensor(lane, new Length(lane.getLength().getSI() - 100, METER), simulator);

        simulator.runUpTo(time);
        while (simulator.isRunning())
        {
            try
            {
                Thread.sleep(1);
            }
            catch (InterruptedException ie)
            {
                ie = null; // ignore
            }
        }
        int bucket = (int) Math.floor(time.getSI() / aggregationTime.getSI());
        LaneChangeModel laneChangeModel = new Egoistic();
        GTUFollowingModelOld gtuFollowingModel =
                new FixedAccelerationModel(new Acceleration(0, METER_PER_SECOND_2), new Duration(1000, SECOND));
        // Construct a car
        BehavioralCharacteristics behavioralCharacteristics = DefaultTestParameters.create();
        LaneBasedIndividualGTU gtu = new LaneBasedIndividualGTU("1", gtuType, length, width, maxSpeed, simulator, this.network);
        LaneBasedStrategicalPlanner strategicalPlanner =
                new LaneBasedStrategicalRoutePlanner(behavioralCharacteristics, new LaneBasedCFLCTacticalPlanner(
                        gtuFollowingModel, laneChangeModel, gtu), gtu);
        gtu.init(strategicalPlanner, initialLongitudinalPositions, speed);
        simulator.runUpTo(new Time(124, SECOND));
        while (simulator.isRunning())
        {
            try
            {
                Thread.sleep(1);
            }
            catch (InterruptedException ie)
            {
                ie = null; // ignore
            }
        }
        for (int sample = 0; sample < 10; sample++)
        {
            boolean shouldHaveData = sample == bucket;
            value = fd.getXValue(0, sample);
            // System.out.println("value of sample " + sample + " is " + value);
            if (shouldHaveData)
            {
                double expectedDensity = 3600 / aggregationTime.getSI() / speed.getSI();
                assertEquals("Density should be " + expectedDensity, expectedDensity, value, 0.00001);
            }
            else
            {
                assertTrue("Data should be NaN", java.lang.Double.isNaN(value));
            }
            value = fd.getX(0, sample).doubleValue();
            if (shouldHaveData)
            {
                double expectedDensity = 3600 / aggregationTime.getSI() / speed.getSI();
                assertEquals("Density should be " + expectedDensity, expectedDensity, value, 0.00001);
            }
            else
            {
                assertTrue("Data should be NaN", java.lang.Double.isNaN(value));
            }
            shouldHaveData = sample <= bucket;
            value = fd.getYValue(0, sample);
            if (shouldHaveData)
            {
                double expectedFlow = sample == bucket ? 3600 / aggregationTime.getSI() : 0;
                assertEquals("Flow should be " + expectedFlow, expectedFlow, value, 0.00001);
            }
            else
            {
                assertTrue("Data should be NaN", java.lang.Double.isNaN(value));
            }
            value = fd.getY(0, sample).doubleValue();
            if (shouldHaveData)
            {
                double expectedFlow = sample == bucket ? 3600 / aggregationTime.getSI() : 0;
                assertEquals("Flow should be " + expectedFlow, expectedFlow, value, 0.00001);
            }
            else
            {
                assertTrue("Data should be NaN", java.lang.Double.isNaN(value));
            }
            fd.actionPerformed(setXToSpeed);
            value = fd.getYValue(0, sample);
            if (shouldHaveData)
            {
                double expectedSpeed = sample == bucket ? speed.getInUnit() : 0;
                assertEquals("Speed should be " + expectedSpeed, expectedSpeed, value, 0.00001);
            }
            else
            {
                assertTrue("Data should be NaN", java.lang.Double.isNaN(value));
            }
            value = fd.getY(0, sample).doubleValue();
            if (shouldHaveData)
            {
                double expectedSpeed = sample == bucket ? speed.getInUnit() : 0;
                assertEquals("Speed should be " + expectedSpeed, expectedSpeed, value, 0.00001);
            }
            else
            {
                assertTrue("Data should be NaN", java.lang.Double.isNaN(value));
            }
            fd.actionPerformed(resetAxis);
        }
        // Check that harmonic mean speed is computed
        speed = new Speed(10, KM_PER_HOUR);
        behavioralCharacteristics = DefaultTestParameters.create();
        LaneBasedIndividualGTU gtuX =
                new LaneBasedIndividualGTU("1234", gtuType, length, width, maxSpeed, simulator, this.network);
        strategicalPlanner =
                new LaneBasedStrategicalRoutePlanner(behavioralCharacteristics, new LaneBasedCFLCTacticalPlanner(
                        gtuFollowingModel, laneChangeModel, gtuX), gtuX);
        gtuX.init(strategicalPlanner, initialLongitudinalPositions, speed);
        simulator.runUpTo(new Time(125, SECOND));
        while (simulator.isRunning())
        {
            try
            {
                Thread.sleep(1);
            }
            catch (InterruptedException ie)
            {
                ie = null; // ignore
            }
        }
        fd.actionPerformed(setXToSpeed);
        value = fd.getYValue(0, bucket);
        double expected = 2d / (1d / 100 + 1d / 10);
        // System.out.println("harmonic speed is " + value + ", expected is " + expected);
        assertEquals("Harmonic mean of 10 and 100 is " + expected, expected, value, 0.0001);
        // Test the actionPerformed method with various malformed ActionEvents.
        try
        {
            fd.actionPerformed(new ActionEvent(fd, 0, "bla"));
            fail("Bad ActionEvent should have thrown an Error");
        }
        catch (Error e)
        {
            // Ignore
        }
        try
        {
            fd.actionPerformed(new ActionEvent(fd, 0, "Speed/bla"));
            fail("Bad ActionEvent should have thrown an Error");
        }
        catch (Error e)
        {
            // Ignore
        }
        try
        {
            fd.actionPerformed(new ActionEvent(fd, 0, "Flow/bla"));
            fail("Bad ActionEvent should have thrown an Error");
        }
        catch (Error e)
        {
            // Ignore
        }
        try
        {
            fd.actionPerformed(new ActionEvent(fd, 0, "Density/bla"));
            fail("Bad ActionEvent should have thrown an Error");
        }
        catch (Error e)
        {
            // Ignore
        }
        try
        {
            fd.actionPerformed(new ActionEvent(fd, 0, "bla/Speed"));
            fail("Bad ActionEvent should have thrown an Error");
        }
        catch (Error e)
        {
            // Ignore
        }
        try
        {
            fd.actionPerformed(new ActionEvent(fd, 0, "bla/Flow"));
            fail("Bad ActionEvent should have thrown an Error");
        }
        catch (Error e)
        {
            // Ignore
        }
        try
        {
            fd.actionPerformed(new ActionEvent(fd, 0, "bla/Density"));
            fail("Bad ActionEvent should have thrown an Error");
        }
        catch (Error e)
        {
            // Ignore
        }
    }

    /**
     * Test the updateHint method in the PointerHandler.
     * @throws Exception when something goes wrong (should not happen)
     */
    @SuppressWarnings("static-method")
    @Test
    public final void testHints() throws Exception
    {
        this.simulator =
                new SimpleSimulator(new Time(0, SECOND), new Duration(0, SECOND), new Duration(1800, SECOND), this);

        Duration aggregationTime = new Duration(30, SECOND);
        Length position = new Length(123, METER);
        Set<GTUType> compatibility = new HashSet<GTUType>();
        compatibility.add(GTUType.ALL);
        LaneType laneType = new LaneType("CarLane", compatibility);
        FundamentalDiagram fd =
                new FundamentalDiagram("Fundamental Diagram", aggregationTime, CarTest.makeLane(this.network, laneType),
                        position, this.simulator);
        // First get the panel that stores the result of updateHint (this is ugly)
        JLabel hintPanel = null;
        ChartPanel chartPanel = null;
        for (Component c0 : fd.getComponents())
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
                                        fail("There should be only one JPanel in a FundamentalDiagram");
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
                                        fail("There should be only one ChartPanel in a FundamentalDiagram");
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
            fail("Could not find a JLabel in FundamentalDiagram");
        }
        if (null == chartPanel)
        {
            fail("Could not find a ChartPanel in FundamentalDiagram");
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
        ph.updateHint(java.lang.Double.NaN, java.lang.Double.NaN);
        assertEquals("The text should again be a single space", " ", hintPanel.getText());
    }

    /** {@inheritDoc} */
    @Override
    public void constructModel(SimulatorInterface<Time, Duration, OTSSimTimeDouble> arg0)
            throws SimRuntimeException
    {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public SimulatorInterface<Time, Duration, OTSSimTimeDouble> getSimulator()

    {
        return this.simulator;
    }

}
