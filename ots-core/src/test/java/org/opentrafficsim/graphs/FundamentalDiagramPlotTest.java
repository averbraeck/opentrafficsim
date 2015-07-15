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
import java.util.HashMap;
import java.util.Map;

import javax.swing.JLabel;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.simulators.SimulatorInterface;

import org.jfree.chart.ChartPanel;
import org.junit.Test;
import org.opentrafficsim.core.car.CarTest;
import org.opentrafficsim.core.car.LaneBasedIndividualCar;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;
import org.opentrafficsim.core.gtu.GTUType;
import org.opentrafficsim.core.gtu.following.FixedAccelerationModel;
import org.opentrafficsim.core.gtu.following.GTUFollowingModel;
import org.opentrafficsim.core.gtu.lane.changing.Egoistic;
import org.opentrafficsim.core.gtu.lane.changing.LaneChangeModel;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.core.network.lane.Lane;
import org.opentrafficsim.core.network.lane.LaneType;
import org.opentrafficsim.core.network.route.Route;
import org.opentrafficsim.core.unit.AccelerationUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.unit.SpeedUnit;
import org.opentrafficsim.core.unit.TimeUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Abs;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar.Rel;
import org.opentrafficsim.simulationengine.SimpleSimulator;

/**
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate$, @version $Revision$, by $Author: pknoppers
 * $, initial version ug 25, 2014 <br>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class FundamentalDiagramPlotTest implements OTSModelInterface
{
    /** */
    private static final long serialVersionUID = 20150226L;

    /**
     * Test the FundamentalDiagram.
     * @throws Exception when something goes wrong (should not happen)
     */
    @SuppressWarnings("static-method")
    @Test
    public final void fundamentalDiagramTest() throws Exception
    {
        DoubleScalar.Rel<TimeUnit> aggregationTime = new DoubleScalar.Rel<TimeUnit>(30, TimeUnit.SECOND);
        DoubleScalar.Rel<LengthUnit> position = new DoubleScalar.Rel<LengthUnit>(123, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> carPosition = new DoubleScalar.Rel<LengthUnit>(122.5, LengthUnit.METER);
        LaneType<String> laneType = new LaneType<String>("CarLane");
        GTUType<String> gtuType = GTUType.makeGTUType("Car");
        laneType.addCompatibility(gtuType);
        Lane lane = CarTest.makeLane(laneType);
        FundamentalDiagram fd = new FundamentalDiagram("Fundamental Diagram", aggregationTime, lane, position);
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
        DoubleScalar.Abs<SpeedUnit> speed = new DoubleScalar.Abs<SpeedUnit>(100, SpeedUnit.KM_PER_HOUR);
        DoubleScalar.Abs<TimeUnit> time = new DoubleScalar.Abs<TimeUnit>(123, TimeUnit.SECOND);
        DoubleScalar.Rel<LengthUnit> length = new DoubleScalar.Rel<LengthUnit>(5.0, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> width = new DoubleScalar.Rel<LengthUnit>(2.0, LengthUnit.METER);
        DoubleScalar.Abs<SpeedUnit> maxSpeed = new DoubleScalar.Abs<SpeedUnit>(120, SpeedUnit.KM_PER_HOUR);
        Map<Lane, DoubleScalar.Rel<LengthUnit>> initialLongitudinalPositions = new HashMap<>();
        initialLongitudinalPositions.put(lane, carPosition);
        SimpleSimulator simulator =
                new SimpleSimulator(new DoubleScalar.Abs<TimeUnit>(0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(
                        0, TimeUnit.SECOND), new DoubleScalar.Rel<TimeUnit>(1800, TimeUnit.SECOND), this);
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
        GTUFollowingModel gtuFollowingModel =
                new FixedAccelerationModel(new DoubleScalar.Abs<AccelerationUnit>(0,
                        AccelerationUnit.METER_PER_SECOND_2), new DoubleScalar.Rel<TimeUnit>(1000, TimeUnit.SECOND));
        // Construct a car
        new LaneBasedIndividualCar<Integer>(1, gtuType, gtuFollowingModel, laneChangeModel,
                initialLongitudinalPositions, speed, length, width, maxSpeed, new Route(new ArrayList<Node<?, ?>>()),
                simulator);
        simulator.runUpTo(new DoubleScalar.Abs<TimeUnit>(124, TimeUnit.SECOND));
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
            // System.out.println("value " + value);
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
        speed = new DoubleScalar.Abs<SpeedUnit>(10, SpeedUnit.KM_PER_HOUR);
        new LaneBasedIndividualCar<Integer>(1234, gtuType, gtuFollowingModel, laneChangeModel,
                initialLongitudinalPositions, speed, length, width, maxSpeed, new Route(new ArrayList<Node<?, ?>>()),
                simulator);
        simulator.runUpTo(new DoubleScalar.Abs<TimeUnit>(125, TimeUnit.SECOND));
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
        DoubleScalar.Rel<TimeUnit> aggregationTime = new DoubleScalar.Rel<TimeUnit>(30, TimeUnit.SECOND);
        DoubleScalar.Rel<LengthUnit> position = new DoubleScalar.Rel<LengthUnit>(123, LengthUnit.METER);
        LaneType<String> laneType = new LaneType<String>("CarLane");
        FundamentalDiagram fd =
                new FundamentalDiagram("Fundamental Diagram", aggregationTime, CarTest.makeLane(laneType), position);
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
    public void constructModel(SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> arg0)
            throws SimRuntimeException, RemoteException
    {
        // Do nothing
    }

    /** {@inheritDoc} */
    @Override
    public SimulatorInterface<Abs<TimeUnit>, Rel<TimeUnit>, OTSSimTimeDouble> getSimulator() throws RemoteException
    {
        return null;
    }

}
