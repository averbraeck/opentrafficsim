package org.opentrafficsim.core.perception;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Time;
import org.junit.Test;
import org.opentrafficsim.core.dsol.OTSDEVSSimulatorInterface;
import org.opentrafficsim.core.dsol.OTSSimTimeDouble;

import mockit.Mock;
import mockit.MockUp;

/**
 * Test of subclasses of Historical.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
    private Time time;

    /** Clean-up time. */
    private Duration cleanUp = Duration.createSI(10.0);

    /** MockUp simulator. */
    private OTSDEVSSimulatorInterface simulator = new MockUp<OTSDEVSSimulatorInterface>()
    {
        @Mock
        OTSSimTimeDouble getSimulatorTime()
        {
            return new OTSSimTimeDouble(HistoricalTest.this.getTime());
        }
    }.getMockInstance();

    /** History manager. */
    private HistoryManager historyManager = new HistoryManagerDEVS(this.simulator, this.cleanUp, this.cleanUp);

    /**
     * Returns the set simulation time.
     * @return set simulation time
     */
    final Time getTime()
    {
        return this.time;
    }

    /**
     * Tests HistoricalValue
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
     * Tests HistoricalCollection
     */
    @Test
    public void collectionTest()
    {
        HistoricalCollection<String, Set<String>> hist = new HistoricalCollection<>(this.historyManager, new HashSet<>());
        this.time = Time.ZERO;
        String msg = "HistoricalCollection contents is incorrect.";
        assertEquals(msg, asSet(), hist.fill(Time.createSI(-1.0), new HashSet<>()));
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
        assertEquals(msg, asSet("0.0"), hist.fill(Time.createSI(0.0), new HashSet<>()));
        assertEquals(msg, asSet("0.0", "1.0", "2.0"), hist.fill(Time.createSI(2.0), new HashSet<>()));
        assertEquals(msg, asSet("0.0", "1.0", "3.0"), hist.fill(Time.createSI(3.0), new HashSet<>()));
        assertEquals(msg, asSet("0.0", "1.0", "4.0"), hist.fill(Time.createSI(4.0), new HashSet<>()));
        assertEquals(msg, asSet("0.0", "1.0", "4.0"), hist.fill(Time.createSI(5.0), new HashSet<>()));
        this.time = Time.createSI(11.9);
        assertEquals(msg, asSet("0.0", "1.0"), hist.fill(Time.createSI(1.85), new HashSet<>()));
        this.time = Time.createSI(12.1);
        hist.cleanUpHistory(this.cleanUp);
        assertEquals(msg, asSet("0.0", "1.0", "2.0"), hist.fill(Time.createSI(0.95), new HashSet<>())); // oldest available
        assertEquals(msg, asSet("0.0", "1.0", "2.0"), hist.fill(Time.createSI(2.05), new HashSet<>()));
    }

    /**
     * @param values values
     * @param <T> value type
     * @return set of values
     */
    private <T> Set<T> asSet(@SuppressWarnings("unchecked") final T... values)
    {
        Set<T> set = new HashSet<>();
        for (T t : values)
        {
            set.add(t);
        }
        return set;
    }

    /**
     * Tests HistoricalList
     */
    @Test
    public void listTest()
    {
        HistoricalList<String, List<String>> hist = new HistoricalList<>(this.historyManager, new ArrayList<>());
        this.time = Time.ZERO;
        String msg = "HistoricalList contents is incorrect.";
        assertEquals(msg, Arrays.asList(), hist.fill(Time.createSI(-1.0), new ArrayList<>()));
        hist.add(0, "0.0"); // 0.0
        this.time = Time.createSI(1.0);
        hist.add(0, "1.0"); // 1.0, 0.0
        this.time = Time.createSI(2.0);
        hist.add(1, "2.0"); // 1.0, 2.0, 0.0
        this.time = Time.createSI(3.0);
        hist.add(0, "3.0"); // 3.0, 1.0, 2.0, 0.0
        hist.remove("2.0"); // 3.0, 1.0, 0.0
        this.time = Time.createSI(4.0);
        hist.remove("3.0"); // 1.0, 0.0
        hist.add(2, "4.0"); // 1.0, 0.0, 4.0
        assertEquals(msg, Arrays.asList("0.0"), hist.fill(Time.createSI(0.0), new ArrayList<>()));
        assertEquals(msg, Arrays.asList("1.0", "2.0", "0.0"), hist.fill(Time.createSI(2.0), new ArrayList<>()));
        assertEquals(msg, Arrays.asList("3.0", "1.0", "0.0"), hist.fill(Time.createSI(3.0), new ArrayList<>()));
        assertEquals(msg, Arrays.asList("1.0", "0.0", "4.0"), hist.fill(Time.createSI(4.0), new ArrayList<>()));
        assertEquals(msg, Arrays.asList("1.0", "0.0", "4.0"), hist.fill(Time.createSI(5.0), new ArrayList<>()));
        this.time = Time.createSI(11.9);
        assertEquals(msg, Arrays.asList("1.0", "0.0"), hist.fill(Time.createSI(1.85), new ArrayList<>()));
        this.time = Time.createSI(12.1);
        hist.cleanUpHistory(this.cleanUp);
        assertEquals(msg, Arrays.asList("1.0", "2.0", "0.0"), hist.fill(Time.createSI(0.95), new ArrayList<>())); // oldest
        assertEquals(msg, Arrays.asList("1.0", "2.0", "0.0"), hist.fill(Time.createSI(2.05), new ArrayList<>()));
    }

    /**
     * Tests HistoricalMap
     */
    @Test
    public void mapTest()
    {
        HistoricalMap<Integer, String, HashMap<Integer, String>> hist =
                new HistoricalMap<>(this.historyManager, new HashMap<>());
        this.time = Time.ZERO;
        String msg = "HistoricalMap contents is incorrect.";
        assertEquals(msg, new HashMap<>(), hist.fill(Time.createSI(-1.0), new HashMap<>()));
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
        assertEquals(msg, asMap(0, "0.0"), hist.fill(Time.createSI(0.0), new HashMap<>()));
        assertEquals(msg, asMap(0, "1.0"), hist.fill(Time.createSI(1.0), new HashMap<>()));
        assertEquals(msg, asMap(0, "1.0", 1, "2.0"), hist.fill(Time.createSI(2.0), new HashMap<>()));
        assertEquals(msg, asMap(0, "3.0"), hist.fill(Time.createSI(3.0), new HashMap<>()));
        assertEquals(msg, asMap(0, "3.0", 2, "4.0"), hist.fill(Time.createSI(4.0), new HashMap<>()));
        assertEquals(msg, asMap(0, "3.0", 2, "4.0"), hist.fill(Time.createSI(5.0), new HashMap<>()));
        this.time = Time.createSI(11.9);
        assertEquals(msg, asMap(0, "1.0"), hist.fill(Time.createSI(1.85), new HashMap<>()));
        this.time = Time.createSI(12.1);
        hist.cleanUpHistory(this.cleanUp);
        assertEquals(msg, asMap(0, "1.0", 1, "2.0"), hist.fill(Time.createSI(0.95), new HashMap<>())); // oldest available
        assertEquals(msg, asMap(0, "1.0", 1, "2.0"), hist.fill(Time.createSI(2.05), new HashMap<>()));
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
        return new HashMap<Integer, String>()
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
        return new HashMap<Integer, String>()
        {
            {
                put(int1, str1);
                put(int2, str2);
            }
        };
    }

}
