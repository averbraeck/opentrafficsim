package org.opentrafficsim.core.perception;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Try;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.opentrafficsim.core.perception.collections.HistoricalHashMap;
import org.opentrafficsim.core.perception.collections.HistoricalLinkedHashSet;
import org.opentrafficsim.core.perception.collections.HistoricalLinkedList;
import org.opentrafficsim.core.perception.collections.HistoricalMap;
import org.opentrafficsim.core.perception.collections.HistoricalSet;

import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;

/**
 * Test of subclasses of Historical.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 2 jan. 2018 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class HistoricalTest
{
    /** Local time object used in simulator MockUp. Can be set for testing at different simulation times. */
    Time time;

    /** Clean-up time. */
    private Duration cleanUp = Duration.createSI(10.0);

    /** MockUp simulator. */
    private DEVSSimulatorInterface.TimeDoubleUnit simulator;

    /** History manager. */
    private HistoryManagerDEVS historyManager;

    /** */
    public HistoricalTest()
    {
        this.simulator = createSimulatorMock();
        this.historyManager = new HistoryManagerDEVS(this.simulator, this.cleanUp, this.cleanUp);
    }

    /**
     * @return a mock of the simulator that uses this.time as the time for getSimulatorTime()
     */
    private DEVSSimulatorInterface.TimeDoubleUnit createSimulatorMock()
    {
        DEVSSimulatorInterface.TimeDoubleUnit simulatorMock = Mockito.mock(DEVSSimulatorInterface.TimeDoubleUnit.class);
        Answer<Time> answerTime = new Answer<Time>()
        {
            @Override
            public Time answer(final InvocationOnMock invocation) throws Throwable
            {
                return HistoricalTest.this.time;
            }

        };
        Mockito.when(simulatorMock.getSimulatorTime()).then(answerTime);
        return simulatorMock;
    }

    /**
     * Tests HistoricalValue.
     */
    @Test
    public void valueTest()
    {
        Historical<String> hist = new HistoricalValue<>(this.historyManager);
        this.time = Time.ZERO;
        String msg = "HistoricalSingle value is incorrect.";
        assertEquals(msg, null, hist.get(Time.createSI(-1.0)));
        hist.set("0.0");
        this.time = Time.createSI(1.0);
        hist.set("1.0");
        this.time = Time.createSI(2.0);
        hist.set("2.0");
        this.time = Time.createSI(2.5);
        hist.set("2.5");
        this.time = Time.createSI(3.0);
        hist.set("3.0");
        assertEquals(msg, "0.0", hist.get(Time.createSI(-1.0)));
        assertEquals(msg, "0.0", hist.get(Time.ZERO));
        assertEquals(msg, "2.5", hist.get(Time.createSI(2.6)));
        assertEquals(msg, "3.0", hist.get(Time.createSI(3.1)));
        assertEquals(msg, "3.0", hist.get());
        this.time = Time.createSI(11.9);
        assertEquals(msg, "1.0", hist.get(Time.createSI(1.85)));
        this.time = Time.createSI(12.1);
        assertEquals(msg, "2.0", hist.get(Time.createSI(2.05)));
    }

    /**
     * Tests HistoricalCollection.
     */
    @Test
    public void collectionTest()
    {
        HistoricalSet<String> hist = new HistoricalLinkedHashSet<>(this.historyManager, new LinkedHashSet<>());
        this.time = Time.ZERO;
        String msg = "HistoricalCollection contents is incorrect.";
        assertEquals(msg, asSet(), hist.get(Time.createSI(-1.0)));
        hist.add("0.0");
        this.time = Time.createSI(1.0);
        hist.add("1.0");
        this.time = Time.createSI(2.0);
        hist.add("2.0");
        this.time = Time.createSI(3.0);
        hist.add("3.0");
        hist.remove("2.0");
        this.time = Time.createSI(4.0);
        hist.remove("3.0");
        hist.add("4.0");
        this.time = Time.createSI(6.0);
        hist.clear();
        hist.addAll(asSet("5.0", "6.0", "7.0", "8.0", "9.0"));
        hist.removeAll(asSet("5.0", "6.0"));
        hist.retainAll(asSet("5.0", "6.0", "7.0", "10.0"));
        Try.testFail(() ->
        {
            Iterator<String> it = hist.iterator();
            it.next();
            it.remove();
        }, "HistoricalSet iterator remove should fail.", UnsupportedOperationException.class);
        assertEquals(msg, asSet("0.0"), hist.get(Time.createSI(0.0)));
        assertEquals(msg, asSet("0.0", "1.0", "2.0"), hist.get(Time.createSI(2.0)));
        assertEquals(msg, asSet("0.0", "1.0", "3.0"), hist.get(Time.createSI(3.0)));
        assertEquals(msg, asSet("0.0", "1.0", "4.0"), hist.get(Time.createSI(4.0)));
        assertEquals(msg, asSet("0.0", "1.0", "4.0"), hist.get(Time.createSI(5.0)));
        this.time = Time.createSI(11.9);
        assertEquals(msg, asSet("0.0", "1.0"), hist.get(Time.createSI(1.85)));
        this.time = Time.createSI(12.1);
        this.historyManager.cleanUpHistory();
        assertEquals(msg, asSet("0.0", "1.0", "2.0"), hist.get(Time.createSI(0.95))); // oldest available
        assertEquals(msg, asSet("0.0", "1.0", "2.0"), hist.get(Time.createSI(2.05)));
        assertEquals(msg, asSet("7.0"), hist.get(Time.createSI(6.0)));
    }

    /**
     * @param values values
     * @param <T> value type
     * @return set of values
     */
    private <T> Set<T> asSet(@SuppressWarnings("unchecked") final T... values)
    {
        Set<T> set = new LinkedHashSet<>();
        for (T t : values)
        {
            set.add(t);
        }
        return set;
    }

    /**
     * Tests HistoricalLinkedList.
     */
    @Test
    public void listTest()
    {
        HistoricalLinkedList<String> hist = new HistoricalLinkedList<>(this.historyManager);
        this.time = Time.ZERO;
        String msg = "HistoricalLinkedList contents is incorrect.";
        assertEquals(msg, Arrays.asList(), hist.get(Time.createSI(-1.0)));
        hist.offer("0.0"); // 0.0
        this.time = Time.createSI(1.0);
        hist.push("1.0"); // 1.0, 0.0
        this.time = Time.createSI(2.0);
        hist.add(1, "2.0"); // 1.0, 2.0, 0.0
        this.time = Time.createSI(3.0);
        hist.add(0, "3.0"); // 3.0, 1.0, 2.0, 0.0
        hist.remove("2.0"); // 3.0, 1.0, 0.0"
        assertEquals(msg, "3.0", hist.peek());
        this.time = Time.createSI(4.0);
        assertEquals(msg, "3.0", hist.poll()); // 1.0, 0.0
        hist.addLast("4.0"); // 1.0, 0.0, 4.0
        this.time = Time.createSI(6.0);
        hist.clear();
        hist.addAll(asSet("5.0", "6.0", "7.0", "8.0", "9.0"));
        hist.removeAll(asSet("5.0", "6.0"));
        hist.retainAll(asSet("5.0", "6.0", "7.0", "10.0"));
        Try.testFail(() ->
        {
            Iterator<String> it = hist.iterator();
            it.next();
            it.remove();
        }, "Iterator remove should fail.", UnsupportedOperationException.class);
        assertEquals(msg, Arrays.asList("0.0"), hist.get(Time.createSI(0.0)));
        assertEquals(msg, Arrays.asList("1.0", "2.0", "0.0"), hist.get(Time.createSI(2.0)));
        assertEquals(msg, Arrays.asList("3.0", "1.0", "0.0"), hist.get(Time.createSI(3.0)));
        assertEquals(msg, Arrays.asList("1.0", "0.0", "4.0"), hist.get(Time.createSI(4.0)));
        assertEquals(msg, Arrays.asList("1.0", "0.0", "4.0"), hist.get(Time.createSI(5.0)));
        this.time = Time.createSI(11.9);
        assertEquals(msg, Arrays.asList("1.0", "0.0"), hist.get(Time.createSI(1.85)));
        this.time = Time.createSI(12.1);
        this.historyManager.cleanUpHistory();
        assertEquals(msg, Arrays.asList("1.0", "2.0", "0.0"), hist.get(Time.createSI(0.95))); // oldest
        assertEquals(msg, Arrays.asList("1.0", "2.0", "0.0"), hist.get(Time.createSI(2.05)));
        assertEquals(msg, Arrays.asList("7.0"), hist.get(Time.createSI(6.0)));
    }

    /**
     * Tests HistoricalMap.
     */
    @Test
    public void mapTest()
    {
        HistoricalMap<Integer, String> hist = new HistoricalHashMap<>(this.historyManager);
        this.time = Time.ZERO;
        String msg = "HistoricalMap contents is incorrect.";
        assertEquals(msg, new LinkedHashMap<>(), hist.get(Time.createSI(-1.0)));
        hist.put(0, "0.0"); // 0=0.0
        this.time = Time.createSI(1.0);
        hist.put(0, "1.0"); // 0=1.0
        this.time = Time.createSI(2.0);
        hist.put(1, "2.0"); // 0=1.0, 1=2.0
        this.time = Time.createSI(3.0);
        hist.put(0, "3.0"); // 0=3.0, 1=2.0
        hist.remove(1); // 0=3.0
        this.time = Time.createSI(4.0);
        hist.put(2, "4.0"); // 0=3.0, 2=4.0
        Try.testFail(() -> hist.keySet().clear(), "HistoricalMap keySet clear should fail.",
                UnsupportedOperationException.class);
        Try.testSucceed(() -> hist.keySet().size(), "HistoricalMap keySet size should not fail.",
                UnsupportedOperationException.class);
        this.time = Time.createSI(6.0);
        hist.clear();
        hist.putAll(asMap(5, "5.0", 6, "6.0"));
        assertEquals(msg, asMap(0, "0.0"), hist.get(Time.createSI(0.0)));
        assertEquals(msg, asMap(0, "1.0"), hist.get(Time.createSI(1.0)));
        assertEquals(msg, asMap(0, "1.0", 1, "2.0"), hist.get(Time.createSI(2.0)));
        assertEquals(msg, asMap(0, "3.0"), hist.get(Time.createSI(3.0)));
        assertEquals(msg, asMap(0, "3.0", 2, "4.0"), hist.get(Time.createSI(4.0)));
        assertEquals(msg, asMap(0, "3.0", 2, "4.0"), hist.get(Time.createSI(5.0)));
        this.time = Time.createSI(11.9);
        assertEquals(msg, asMap(0, "1.0"), hist.get(Time.createSI(1.85)));
        this.time = Time.createSI(12.1);
        this.historyManager.cleanUpHistory();
        assertEquals(msg, asMap(0, "1.0", 1, "2.0"), hist.get(Time.createSI(0.95))); // oldest available
        assertEquals(msg, asMap(0, "1.0", 1, "2.0"), hist.get(Time.createSI(2.05)));
        assertEquals(msg, asMap(5, "5.0", 6, "6.0"), hist.get(Time.createSI(6.0)));
    }

    /**
     * Creates a map with 1 pair.
     * @param int1 key 1
     * @param str1 value 1
     * @return map with 1 value pair
     */
    @SuppressWarnings("serial")
    private Map<Integer, String> asMap(final int int1, final String str1)
    {
        return new LinkedHashMap<Integer, String>()
        {
            {
                put(int1, str1);
            }
        };
    }

    /**
     * Creates a map with 2 pairs.
     * @param int1 key 1
     * @param str1 value 1
     * @param int2 key 2
     * @param str2 value 2
     * @return map with 2 value pairs
     */
    @SuppressWarnings("serial")
    private Map<Integer, String> asMap(final int int1, final String str1, final int int2, final String str2)
    {
        return new LinkedHashMap<Integer, String>()
        {
            {
                put(int1, str1);
                put(int2, str2);
            }
        };
    }

}
