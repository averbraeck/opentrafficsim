package trafficcontrol;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.immutablecollections.ImmutableSet;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opentrafficsim.core.dsol.OTSModelInterface;
import org.opentrafficsim.core.dsol.OTSSimulator;
import org.opentrafficsim.core.dsol.OTSSimulatorInterface;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.OTSNetwork;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLight;
import org.opentrafficsim.road.network.lane.object.trafficlight.TrafficLightColor;
import org.opentrafficsim.trafficcontrol.FixedTimeController;
import org.opentrafficsim.trafficcontrol.FixedTimeController.SignalGroup;

import nl.tudelft.simulation.dsol.SimRuntimeException;

/**
 * Test the fixed time traffic controller class.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Feb 21, 2019 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class TestFixedTimeController
{

    /**
     * Test the constructors and initializers of the signal group and fixed time controller classes.
     * @throws SimRuntimeException if that happens uncaught; this test has failed
     * @throws NamingException if that happens uncaught; this test has failed
     * @throws NetworkException on exception
     */
    // TODO: @Test
    public void testConstructors() throws SimRuntimeException, NamingException, NetworkException
    {
        String signalGroupId = "sgId";
        Set<String> trafficLightIds = new LinkedHashSet<>();
        String trafficLightId = "08.1";
        trafficLightIds.add(trafficLightId);
        Duration signalGroupOffset = Duration.instantiateSI(5);
        Duration preGreen = Duration.instantiateSI(2);
        Duration green = Duration.instantiateSI(10);
        Duration yellow = Duration.instantiateSI(3.5);
        try
        {
            new SignalGroup(null, trafficLightIds, signalGroupOffset, preGreen, green, yellow);
            fail("Null pointer for signalGroupId should have thrown a null pointer exception");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }
        try
        {
            new SignalGroup(signalGroupId, null, signalGroupOffset, preGreen, green, yellow);
            fail("Null pointer for trafficLightIds should have thrown a null pointer exception");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }
        try
        {
            new SignalGroup(signalGroupId, trafficLightIds, null, preGreen, green, yellow);
            fail("Null pointer for signalGroupOffset should have thrown a null pointer exception");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }
        try
        {
            new SignalGroup(signalGroupId, trafficLightIds, signalGroupOffset, null, green, yellow);
            fail("Null pointer for preGreen should have thrown a null pointer exception");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }
        try
        {
            new SignalGroup(signalGroupId, trafficLightIds, signalGroupOffset, preGreen, null, yellow);
            fail("Null pointer for green should have thrown a null pointer exception");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }
        try
        {
            new SignalGroup(signalGroupId, trafficLightIds, signalGroupOffset, preGreen, green, null);
            fail("Null pointer for yellow should have thrown a null pointer exception");
        }
        catch (NullPointerException npe)
        {
            // Ignore expected exception
        }
        try
        {
            new SignalGroup(signalGroupId, new LinkedHashSet<String>(), signalGroupOffset, preGreen, green, yellow);
            fail("Empty list of traffic light ids should have thrown an illegal argument exception");
        }
        catch (IllegalArgumentException iae)
        {
            // Ignore expected exception
        }
        // Test the controller that adds default for pre green time
        SignalGroup sg = new SignalGroup(signalGroupId, trafficLightIds, signalGroupOffset, green, yellow);
        assertEquals("default for pre green", 0, sg.getPreGreen().si, 0);
        assertEquals("green", green.si, sg.getGreen().si, 0);
        assertEquals("yellow", yellow.si, sg.getYellow().si, 0);
        // Now that we've tested all ways that the constructor should have told us to go to hell, create a signal group
        sg = new SignalGroup(signalGroupId, trafficLightIds, signalGroupOffset, preGreen, green, yellow);
        assertEquals("group id", signalGroupId, sg.getId());
        assertTrue("toString returns something descriptive", sg.toString().startsWith("SignalGroup ["));

        String ftcId = "FTCid";
        OTSSimulatorInterface simulator = new OTSSimulator("TestFixedTimeController");
        simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600), createModelMock());
        Map<String, TrafficLight> trafficLightMap = new LinkedHashMap<String, TrafficLight>();
        String networkId = "networkID";
        trafficLightMap.put(trafficLightId, createTrafficLightMock(trafficLightId, networkId, simulator));
        OTSNetwork network = new OTSNetwork(networkId, true);
        network.addObject(trafficLightMap.get(trafficLightId));

        Duration cycleTime = Duration.instantiateSI(90);
        Duration offset = Duration.instantiateSI(20);
        Set<SignalGroup> signalGroups = new LinkedHashSet<>();
        ImmutableSet<String> ids = sg.getTrafficLightIds();
        for (String tlId : ids)
        {
            assertTrue("returned id is in provided set", trafficLightMap.containsKey(tlId));
        }
        for (String tlId : trafficLightMap.keySet())
        {
            assertTrue("provided id is returned", ids.contains(tlId));
        }
        signalGroups.add(sg);
        try
        {
            new FixedTimeController(null, simulator, network, cycleTime, offset, signalGroups);
            fail("Null pointer for controller id should have thrown an exception");
        }
        catch (NullPointerException npe)
        {
            // Ignore
        }
        try
        {
            new FixedTimeController(ftcId, null, network, cycleTime, offset, signalGroups);
            fail("Null pointer for simulator should have thrown an exception");
        }
        catch (NullPointerException npe)
        {
            // Ignore
        }
        try
        {
            new FixedTimeController(ftcId, simulator, null, cycleTime, offset, signalGroups);
            fail("Null pointer for network should have thrown an exception");
        }
        catch (NullPointerException npe)
        {
            // Ignore
        }
        try
        {
            new FixedTimeController(ftcId, simulator, network, null, offset, signalGroups);
            fail("Null pointer for cycle time should have thrown an exception");
        }
        catch (NullPointerException npe)
        {
            // Ignore
        }
        try
        {
            new FixedTimeController(ftcId, simulator, network, cycleTime, null, signalGroups);
            fail("Null pointer for offset should have thrown an exception");
        }
        catch (NullPointerException npe)
        {
            // Ignore
        }
        try
        {
            new FixedTimeController(ftcId, simulator, network, cycleTime, offset, null);
            fail("Null pointer for signal groups should have thrown an exception");
        }
        catch (NullPointerException npe)
        {
            // Ignore
        }
        try
        {
            new FixedTimeController(ftcId, simulator, network, cycleTime, offset, new LinkedHashSet<SignalGroup>());
            fail("Empty signal groups should have thrown an exception");
        }
        catch (IllegalArgumentException iae)
        {
            // Ignore
        }
        try
        {
            new FixedTimeController(ftcId, simulator, network, Duration.instantiateSI(0), offset, signalGroups);
            fail("Illegal cycle time should hav thrown an exception");
        }
        catch (IllegalArgumentException iae)
        {
            // Ignore
        }
        try
        {
            new FixedTimeController(ftcId, simulator, network, Duration.instantiateSI(-10), offset, signalGroups);
            fail("Illegal cycle time should hav thrown an exception");
        }
        catch (IllegalArgumentException iae)
        {
            // Ignore
        }
        // Not testing check for identical signal groups; yet
        // Now that we've tested all ways that the constructor should have told us to go to hell, create a controller
        FixedTimeController ftc = new FixedTimeController(ftcId, simulator, network, cycleTime, offset, signalGroups);
        assertEquals("FTC id", ftcId, ftc.getId());
        assertTrue("toString returns something descriptive", ftc.toString().startsWith("FixedTimeController ["));

        simulator.runUpTo(Time.instantiateSI(1));
        while (simulator.isRunning())
        {
            try
            {
                Thread.sleep(100);
            }
            catch (InterruptedException exception)
            {
                exception.printStackTrace();
            }
        }
        for (TrafficLight tl : sg.getTrafficLights())
        {
            assertTrue("acquired traffic light is in the proved set", trafficLightMap.containsKey(tl.getId()));
        }
        assertEquals("red time makes up remainder of cycle time", cycleTime.minus(preGreen).minus(green).minus(yellow).si,
                sg.getRed().si, 0.0001);
    }

    /**
     * Test detection of non-disjoint sets of traffic lights.
     * @throws NamingException on exception
     * @throws SimRuntimeException on exception
     * @throws NetworkException on exception
     */
    // TODO: @Test
    public void testDisjoint() throws SimRuntimeException, NamingException, NetworkException
    {
        String signalGroupId = "sgId1";
        Set<String> trafficLightIds1 = new LinkedHashSet<>();
        String trafficLightId = "08.1";
        trafficLightIds1.add(trafficLightId);
        Duration signalGroupOffset = Duration.instantiateSI(5);
        Duration preGreen = Duration.instantiateSI(2);
        Duration green = Duration.instantiateSI(10);
        Duration yellow = Duration.instantiateSI(3.5);
        SignalGroup sg1 = new SignalGroup(signalGroupId, trafficLightIds1, signalGroupOffset, preGreen, green, yellow);
        String signalGroupId2 = "sgId2";
        Set<String> trafficLightIds2 = new LinkedHashSet<>();
        trafficLightIds2.add(trafficLightId);
        SignalGroup sg2 = new SignalGroup(signalGroupId2, trafficLightIds2, signalGroupOffset, preGreen, green, yellow);

        String ftcId = "FTCid";
        OTSSimulatorInterface simulator = new OTSSimulator("TestFixedTimeController");
        simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600), createModelMock());
        Map<String, TrafficLight> trafficLightMap = new LinkedHashMap<String, TrafficLight>();
        String networkId = "networkID";
        trafficLightMap.put(trafficLightId, createTrafficLightMock(trafficLightId, networkId, simulator));
        OTSNetwork network = new OTSNetwork(networkId, true);
        network.addObject(trafficLightMap.get(trafficLightId));

        Duration cycleTime = Duration.instantiateSI(90);
        Duration offset = Duration.instantiateSI(20);
        Set<SignalGroup> signalGroups = new LinkedHashSet<>();
        signalGroups.add(sg1);
        signalGroups.add(sg2);
        try
        {
            new FixedTimeController(ftcId, simulator, network, cycleTime, offset, signalGroups);
            fail("Same traffic light in different signal groups should have thrown an IllegalArgumnentException");
        }
        catch (IllegalArgumentException iae)
        {
            // Ignore
        }
    }

    /**
     * Test timing of fixed time controller.
     * @throws SimRuntimeException if that happens uncaught; this test has failed
     * @throws NamingException if that happens uncaught; this test has failed
     * @throws NetworkException on exception
     */
    // TODO: @Test
    public void testTimings() throws SimRuntimeException, NamingException, NetworkException
    {
        String signalGroupId = "sgId";
        Set<String> trafficLightIds = new LinkedHashSet<>();
        String trafficLightId = "08.1";
        trafficLightIds.add(trafficLightId);
        Set<SignalGroup> signalGroups = new LinkedHashSet<>();
        for (int cycleTime : new int[] {60, 90})
        {
            Duration cycle = Duration.instantiateSI(cycleTime);
            for (int ftcOffsetTime : new int[] {-100, -10, 0, 10, 100})
            {
                Duration ftcOffset = Duration.instantiateSI(ftcOffsetTime);
                for (int sgOffsetTime : new int[] {-99, -9, 0, 9, 99})
                {
                    Duration sgOffset = Duration.instantiateSI(sgOffsetTime);
                    for (int preGreenTime : new int[] {0, 3})
                    {
                        Duration preGreen = Duration.instantiateSI(preGreenTime);
                        for (int greenTime : new int[] {5, 15, 100})
                        {
                            Duration green = Duration.instantiateSI(greenTime);
                            for (double yellowTime : new double[] {0, 3.5, 4.5})
                            {
                                Duration yellow = Duration.instantiateSI(yellowTime);
                                double minimumCycleTime = preGreenTime + greenTime + yellowTime;
                                SignalGroup sg =
                                        new SignalGroup(signalGroupId, trafficLightIds, sgOffset, preGreen, green, yellow);
                                signalGroups.clear();
                                signalGroups.add(sg);
                                String ftcId = "FTCid";
                                OTSSimulatorInterface simulator = new OTSSimulator("TestFixedTimeController");
                                simulator.initialize(Time.ZERO, Duration.ZERO, Duration.instantiateSI(3600), createModelMock());
                                Map<String, TrafficLight> trafficLightMap = new LinkedHashMap<String, TrafficLight>();
                                String networkId = "networkID";
                                trafficLightMap.put(trafficLightId,
                                        createTrafficLightMock(trafficLightId, networkId, simulator));
                                OTSNetwork network = new OTSNetwork(networkId, true);
                                network.addObject(trafficLightMap.get(trafficLightId));
                                // System.out.println(cycle);
                                FixedTimeController ftc =
                                        new FixedTimeController(ftcId, simulator, network, cycle, ftcOffset, signalGroups);
                                // System.out.print(ftc);
                                if (cycleTime < minimumCycleTime)
                                {
                                    PrintStream originalError = System.err;
                                    boolean exceptionThrown = false;
                                    try
                                    {
                                        while (simulator.getSimulatorTime().si <= 0)
                                        {
                                            System.setErr(new PrintStream(new ByteArrayOutputStream()));
                                            try
                                            {
                                                simulator.step();
                                            }
                                            finally
                                            {
                                                System.setErr(originalError);
                                            }
                                        }
                                    }
                                    catch (SimRuntimeException exception)
                                    {
                                        exceptionThrown = true;
                                        assertTrue("exception explains cycle time problem",
                                                exception.getCause().getCause().getMessage().contains("Cycle time shorter "));
                                    }
                                    assertTrue("Too short cycle time should have thrown a SimRuntimeException",
                                            exceptionThrown);
                                }
                                else
                                {
                                    // All transitions are at multiples of 0.5 seconds; check the state at 0.25 and 0.75 in each
                                    // second
                                    for (int second = 0; second <= 300; second++)
                                    {
                                        Object[] args = new Object[] {simulator, ftc, Boolean.TRUE};
                                        simulator.scheduleEventAbs(Time.instantiateSI(second + 0.25), this, this, "checkState",
                                                args);
                                        simulator.scheduleEventAbs(Time.instantiateSI(second + 0.75), this, this, "checkState",
                                                args);
                                    }
                                    Time stopTime = Time.instantiateSI(300);
                                    simulator.runUpTo(stopTime);
                                    while (simulator.isRunning())
                                    {
                                        try
                                        {
                                            Thread.sleep(1);
                                        }
                                        catch (InterruptedException exception)
                                        {
                                            exception.printStackTrace();
                                        }
                                    }
                                    if (simulator.getSimulatorTime().lt(stopTime))
                                    {
                                        // something went wrong; call checkState with stopSimulatorOnError set to false
                                        checkState(simulator, ftc, Boolean.FALSE);
                                        fail("checkState should have thrown an assert error");
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Check that the current state of a fixed time traffic light controller matches the design.
     * @param simulator OTSSimulatorInterface; the simulator
     * @param ftc FixedTimeController; the fixed time traffic light controller
     * @param stopSimulatorOnError boolean; if true; stop the simulator on error; if false; execute the failing assert on error
     */
    public void checkState(final OTSSimulatorInterface simulator, final FixedTimeController ftc,
            final boolean stopSimulatorOnError)
    {
        double cycleTime = ftc.getCycleTime().si;
        double time = simulator.getSimulatorTime().si;
        double mainOffset = ftc.getOffset().si;
        for (SignalGroup sg : ftc.getSignalGroups())
        {
            double phaseOffset = sg.getOffset().si + mainOffset;
            double phase = time + phaseOffset;
            while (phase < 0)
            {
                phase += cycleTime;
            }
            phase %= cycleTime;
            TrafficLightColor expectedColor = null;
            if (phase < sg.getPreGreen().si)
            {
                expectedColor = TrafficLightColor.PREGREEN;
            }
            else if (phase < sg.getPreGreen().plus(sg.getGreen()).si)
            {
                expectedColor = TrafficLightColor.GREEN;
            }
            else if (phase < sg.getPreGreen().plus(sg.getGreen()).plus(sg.getYellow()).si)
            {
                expectedColor = TrafficLightColor.YELLOW;
            }
            else
            {
                expectedColor = TrafficLightColor.RED;
            }
            // Verify the color of all traffic lights
            for (TrafficLight tl : sg.getTrafficLights())
            {
                if (!expectedColor.equals(tl.getTrafficLightColor()))
                {
                    if (stopSimulatorOnError)
                    {
                        try
                        {
                            simulator.stop();
                        }
                        catch (SimRuntimeException exception)
                        {
                            exception.printStackTrace();
                        }
                    }
                    else
                    {
                        assertEquals(
                                "Traffic light color mismatch at simulator time " + simulator.getSimulatorTime()
                                        + " of signal group " + sg,
                                expectedColor + " which is in phase " + phase + " of cycle time " + cycleTime,
                                tl.getTrafficLightColor());
                    }
                }
            }
        }
    }

    /**
     * Create a mocked OTSModelInterface.
     * @return OTSModelInterface
     */
    public OTSModelInterface createModelMock()
    {
        return Mockito.mock(OTSModelInterface.class);
    }

    /** Remember current state of all mocked traffic lights. */
    Map<String, TrafficLightColor> currentTrafficLightColors = new LinkedHashMap<>();

    /**
     * Mock a traffic light.
     * @param id String; value that will be returned by the getId method
     * @param networkId String; name of network (prepended to id for result of getFullId method)
     * @param simulator TODO
     * @return TrafficLight
     */
    public TrafficLight createTrafficLightMock(final String id, final String networkId, OTSSimulatorInterface simulator)
    {
        TrafficLight result = Mockito.mock(TrafficLight.class);
        Mockito.when(result.getId()).thenReturn(id);
        Mockito.when(result.getFullId()).thenReturn(networkId + "." + id);
        Mockito.when(result.getTrafficLightColor()).thenAnswer(new Answer<TrafficLightColor>()
        {
            @Override
            public TrafficLightColor answer(InvocationOnMock invocation) throws Throwable
            {
                return TestFixedTimeController.this.currentTrafficLightColors.get(result.getFullId());
            }
        });
        Mockito.doAnswer((Answer<Void>) invocation ->
        {
            TrafficLightColor tlc = invocation.getArgument(0);
            // System.out.println(simulator.getSimulatorTime() + " changing color of " + result.getFullId() + " from "
            // + this.currentTrafficLightColors.get(result.getFullId()) + " to " + tlc);
            this.currentTrafficLightColors.put(result.getFullId(), tlc);
            return null;
        }).when(result).setTrafficLightColor(ArgumentMatchers.any(TrafficLightColor.class));
        this.currentTrafficLightColors.put(result.getFullId(), TrafficLightColor.BLACK);
        return result;
    }

}
