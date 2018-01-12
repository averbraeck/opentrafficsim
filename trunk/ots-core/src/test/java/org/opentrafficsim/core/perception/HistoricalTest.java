package org.opentrafficsim.core.perception;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
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
    private HistoryManager historyManager =
            new HistoryManagerDEVS(this.simulator, Duration.createSI(10.0), Duration.createSI(10.0));

    /**
     * Returns the set simulation time.
     * @return set simulation time
     */
    final Time getTime()
    {
        return this.time;
    }

    /**
     * Tests HistoricalSingle
     */
    @SuppressWarnings("unused")
    @Test
    public void SingleTest()
    {
        Duration history = Duration.createSI(10.0);
        Historical<String> hist = new Historical<>(this.historyManager);
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
        hist.clear("2.5");
        assertEquals(msg, "2.0", hist.get(Time.createSI(2.6)));
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
    @SuppressWarnings("unused")
    @Test
    public void CollectionTest()
    {
        Duration history = Duration.createSI(10.0);
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
    @SuppressWarnings("unused")
    @Test
    public void ListTest()
    {
        Duration history = Duration.createSI(10.0);
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
        assertEquals(msg, Arrays.asList("1.0", "2.0", "0.0"), hist.fill(Time.createSI(2.05), new ArrayList<>()));
    }

}
