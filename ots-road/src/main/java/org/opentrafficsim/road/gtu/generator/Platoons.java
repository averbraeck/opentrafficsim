package org.opentrafficsim.road.gtu.generator;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.naming.NamingException;

import org.djunits.value.ValueRuntimeException;
import org.djunits.value.storage.StorageType;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Time;
import org.djunits.value.vdouble.vector.FrequencyVector;
import org.djunits.value.vdouble.vector.TimeVector;
import org.djunits.value.vdouble.vector.base.DoubleVector;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.WeightedMeanAndSum;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.distributions.ProbabilityException;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.geometry.OtsGeometryException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuCharacteristicsGenerator;
import org.opentrafficsim.road.gtu.generator.characteristics.LaneBasedGtuCharacteristicsGeneratorOd;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.od.Category;
import org.opentrafficsim.road.od.Interpolation;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.jstats.streams.StreamInterface;

/**
 * Connects with a lane-based GTU generator to disable it over some time and generate a platoon instead. Platoons are added as:
 * {@code Platoons.ofCategory(...).addPlatoon(...).addGtu(...).addGtu(...).addPlatoon(...).addGtu(...).addGtu(...).start();}.
 * Method {@code addGtu(...)} may only determine a generation time if other info is set by {@code fixInfo(...)}.<br>
 * <br>
 * This class may be used with a {@code LaneBasedGtuCharacteristicsGenerator} or {@code GtuCharacteristicsGeneratorOD}. Use
 * {@code ofGtuType()} or {@code ofCategory()} respectively.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <T> type of demand category, typically a Category in an OdMatrix or a GtuType
 */
public abstract class Platoons<T>
{

    /** GTU generator to disable. */
    private LaneBasedGtuGenerator gen;

    /** Simulator. */
    private final OtsSimulatorInterface simulator;

    /** Position to generate the GTU's at. */
    private final Set<Lane> position;

    /** Queue of GTU information to generate GTU's with. */
    private final Queue<PlatoonGtu<T>> queue = new PriorityQueue<>();

    /** Map of platoon start and end times. */
    private final SortedMap<Time, Time> periods = new TreeMap<>();

    /** Start time of current platoon. */
    private Time startTime;

    /** End time of current platoon. */
    private Time endTime;

    /** Origin to use on added GTU. */
    private Node fixedOrigin;

    /** Destination to use on added GTU. */
    private Node fixedDestination;

    /** Category to use on added GTU. */
    private T fixedCategory;

    /** Number of GTUs that will be generated. */
    private Map<T, Integer> numberOfGtus = new LinkedHashMap<>();

    /** Whether the Platoons was started, after which nothing may be added. */
    private boolean started = false;

    /**
     * Constructor.
     * @param simulator OtsSimulatorInterface; simulator
     * @param position Set&lt;Lane&gt;; position
     */
    private Platoons(final OtsSimulatorInterface simulator, final Set<Lane> position)
    {
        this.simulator = simulator;
        this.position = position;
    }

    /**
     * Creates a {@code Platoon&lt;Category&gt;} instance for platoons.
     * @param characteristics GtuCharacteristicsGeneratorOD; characteristics generator
     * @param simulator OtsSimulatorInterface; simulator
     * @param stream StreamInterface; random number stream
     * @param position Set&lt;Lane&gt;; position
     * @return Platoons&lt;Category&gt;; platoons based on OD
     */
    public static Platoons<Category> ofCategory(final LaneBasedGtuCharacteristicsGeneratorOd characteristics,
            final OtsSimulatorInterface simulator, final StreamInterface stream, final Set<Lane> position)
    {
        return new Platoons<Category>(simulator, position)
        {
            /** Characteristics generator OD based. */
            private final LaneBasedGtuCharacteristicsGeneratorOd characteristicsOD = characteristics;

            /** Random number stream. */
            private final StreamInterface strm = stream;

            /** {@inheritDoc} */
            @Override
            protected void placeGtu(final PlatoonGtu<Category> platoonGtu) throws SimRuntimeException, NamingException,
                    GtuException, NetworkException, OtsGeometryException, ProbabilityException, ParameterException
            {
                getGenerator().queueGtu(this.characteristicsOD.draw(platoonGtu.getOrigin(), platoonGtu.getDestination(),
                        platoonGtu.getCategory(), this.strm), getPosition());
                start();
            }
        };
    }

    /**
     * Creates a {@code Platoon&lt;GtuType&gt;} instance for platoons.
     * @param characteristics LaneBasedGtuCharacteristicsGenerator; characteristics generator
     * @param simulator OtsSimulatorInterface; simulator
     * @param stream StreamInterface; random number stream
     * @param position Set&lt;Lane&gt;; position
     * @return Platoons&lt;GtuType&gt;; platoons based on OD
     */
    @SuppressWarnings("synthetic-access")
    public static Platoons<GtuType> ofGtuType(final LaneBasedGtuCharacteristicsGenerator characteristics,
            final OtsSimulatorInterface simulator, final StreamInterface stream, final Set<Lane> position)
    {
        return new Platoons<GtuType>(simulator, position)
        {
            /** Characteristics generator. */
            private final LaneBasedGtuCharacteristicsGenerator chrctrstcs = characteristics;

            /** {@inheritDoc} */
            @Override
            protected void placeGtu(final PlatoonGtu<GtuType> platoonGtu) throws SimRuntimeException, NamingException,
                    GtuException, NetworkException, OtsGeometryException, ProbabilityException, ParameterException
            {
                // we actually do nothing with the platoonGtu here
                getGenerator().queueGtu(this.chrctrstcs.draw(), getPosition());
                start();
            }
        };
    }

    /**
     * Add a platoon. The generator is disabled during the provided time frame. Individual GTU's should be added using
     * {@code addGtu}.
     * @param start Time; start time
     * @param end Time; end time
     * @return Platoons&lt;T&gt;; for method chaining
     * @throws SimRuntimeException on exception
     */
    public Platoons<T> addPlatoon(final Time start, final Time end) throws SimRuntimeException
    {
        Throw.when(this.started, IllegalStateException.class, "Cannot add a platoon after the Platoons was started.");
        Throw.whenNull(start, "Start may not be null.");
        Throw.whenNull(end, "End may not be null.");
        this.startTime = start;
        this.endTime = end;
        this.periods.put(start, end);
        return this;
    }

    /**
     * Fix all info except time for GTU's added hereafter.
     * @param origin Node; origin
     * @param destination Node; destination
     * @param category T; category
     * @return Platoons&lt;T&gt;; for method chaining
     */
    public Platoons<T> fixInfo(final Node origin, final Node destination, final T category)
    {
        this.fixedOrigin = origin;
        this.fixedDestination = destination;
        this.fixedCategory = category;
        return this;
    }

    /**
     * Add GTU to the current platoon. Per platoon, GTU may be given in any order. This method uses info set with
     * {@code fixInfo}.
     * @param time Time; time of generation
     * @return Platoons&lt;T&gt;; for method chaining
     * @throws IllegalStateException if no fixed info was set using {@code fixInfo}
     */
    public Platoons<T> addGtu(final Time time)
    {
        Throw.when(this.fixedOrigin == null || this.fixedDestination == null || this.fixedCategory == null,
                IllegalStateException.class, "When using addGtu(Time), used fixInfo(...) before to set other info.");
        return addGtu(time, this.fixedOrigin, this.fixedDestination, this.fixedCategory);
    }

    /**
     * Add GTU to the current platoon. Per platoon, GTU may be given in any order.
     * @param time Time; time of generation
     * @param origin Node; origin
     * @param destination Node; destination
     * @param category T; category
     * @return Platoons&lt;T&gt;; for method chaining
     * @throws IllegalStateException if no platoon was started or time is outside of the platoon time range
     */
    public Platoons<T> addGtu(final Time time, final Node origin, final Node destination, final T category)
    {
        Throw.when(this.started, IllegalStateException.class, "Cannot add a GTU after the Platoons was started.");
        Throw.when(this.startTime == null || this.endTime == null, IllegalStateException.class,
                "First call addPlatoon() before calling addGtu()");
        Throw.when(time.gt(this.endTime) || time.lt(this.startTime), IllegalArgumentException.class,
                "Time %s is not between %s and %s", time, this.startTime, this.endTime);
        this.queue.add(new PlatoonGtu<>(time, origin, destination, category));
        this.numberOfGtus.put(category, this.numberOfGtus.getOrDefault(category, 0) + 1);
        return this;
    }

    /**
     * Sets the generator and starts the events.
     * @param generator LaneBasedGtuGenerator; GTU generator
     * @throws SimRuntimeException if start of first platoon is in the past
     */
    public void start(final LaneBasedGtuGenerator generator) throws SimRuntimeException
    {
        Throw.when(this.started, IllegalStateException.class, "Cannot start the Platoons, it was already started.");
        this.gen = generator;
        // check platoon overlap
        Time prevEnd = null;
        for (Map.Entry<Time, Time> entry : this.periods.entrySet())
        {
            Time start = entry.getKey();
            Throw.when(prevEnd != null && start.le(prevEnd), IllegalStateException.class, "Platoons are overlapping.");
            prevEnd = entry.getValue();
            this.gen.disable(start, prevEnd, this.position);
        }
        this.started = true;
        start();
    }

    /**
     * Returns the vehicle generator for sub classes.
     * @return LaneBasedGtuGenerator; vehicle generator for sub classes
     */
    protected LaneBasedGtuGenerator getGenerator()
    {
        return this.gen;
    }

    /**
     * Returns the position for sub classes.
     * @return Set&lt;Lane&gt;; position for sub classes
     */
    protected Set<Lane> getPosition()
    {
        return this.position;
    }

    /**
     * Starts the events.
     * @throws SimRuntimeException if start of first platoon is in the past
     */
    protected void start() throws SimRuntimeException
    {
        if (!this.queue.isEmpty())
        {
            this.simulator.scheduleEventAbsTime(this.queue.peek().getTime(), this, "placeGtu", new Object[] {this.queue.poll()});
        }
    }

    /**
     * Creates a demand vector in which the platoon demand has been compensated from the input demand vector. Only demand
     * pertaining to the location where the platoons are generated should be compensated.
     * @param category T; category
     * @param demand FrequencyVector; demand vector
     * @param time TimeVector; time vector
     * @param interpolation Interpolation; interpolation
     * @return FrequencyVector; demand vector in which the platoon demand has been compensated from the input demand vector
     */
    public FrequencyVector compensate(final T category, final FrequencyVector demand, final TimeVector time,
            final Interpolation interpolation)
    {
        Throw.whenNull(category, "Category may not be null.");
        Throw.whenNull(demand, "Demand may not be null.");
        Throw.whenNull(time, "Time may not be null.");
        Throw.whenNull(interpolation, "Interpolation may not be null.");
        Throw.when(demand.size() != time.size(), IllegalArgumentException.class, "Demand and time have unequal length.");
        try
        {
            WeightedMeanAndSum<Double, Double> weightedSumLost = new WeightedMeanAndSum<>();
            for (Map.Entry<Time, Time> entry : this.periods.entrySet())
            {
                Time start = entry.getKey();
                Time end = entry.getValue();
                for (int i = 0; i < demand.size() - 1; i++)
                {
                    Time s = Time.max(start, time.get(i));
                    Time e = Time.min(end, time.get(i + 1));
                    if (s.lt(e))
                    {
                        Frequency fStart = interpolation.interpolateVector(s, demand, time, true);
                        // TODO: end time of platoon may be in next demand period, which makes the demand non-linear
                        Frequency fEnd = interpolation.interpolateVector(e, demand, time, false);
                        weightedSumLost.add((fStart.si + fEnd.si) / 2, e.si - s.si);
                    }
                }
            }
            WeightedMeanAndSum<Double, Double> weightedSumTotal = new WeightedMeanAndSum<>();
            for (int i = 0; i < demand.size() - 1; i++)
            {
                Frequency fStart = interpolation.interpolateVector(time.get(i), demand, time, true);
                Frequency fEnd = interpolation.interpolateVector(time.get(i + 1), demand, time, false);
                weightedSumTotal.add((fStart.si + fEnd.si) / 2, time.getSI(i + 1) - time.getSI(i));

            }
            // calculate factor
            double lost = weightedSumLost.getSum();
            double total = weightedSumTotal.getSum();
            int platooning = this.numberOfGtus.getOrDefault(category, 0);
            double factor = (total - platooning) / (total - lost);
            if (factor < 0.0)
            {
                this.simulator.getLogger().always().warn("Reducing demand of {} by {}, demand is set to 0.", total,
                        total - factor * total);
                factor = 0.0;
            }
            // create and return factor copy
            double[] array = new double[demand.size()];
            for (int i = 0; i < array.length - 1; i++)
            {
                array[i] = demand.getInUnit(i) * factor;
            }
            return DoubleVector.instantiate(array, demand.getDisplayUnit(), StorageType.DENSE);
        }
        catch (ValueRuntimeException exception)
        {
            throw new RuntimeException("Unexpected exception while looping vector.", exception);
        }
    }

    /**
     * Places the next platoon GTU and schedules the next one.
     * @param platoonGtu PlatoonGtu&lt;T&gt;; info of GTU to generate
     * @throws SimRuntimeException on exception
     * @throws NamingException on exception
     * @throws GtuException on exception
     * @throws NetworkException on exception
     * @throws OtsGeometryException on exception
     * @throws ParameterException on exception
     * @throws ProbabilityException on exception
     */
    protected abstract void placeGtu(PlatoonGtu<T> platoonGtu) throws SimRuntimeException, NamingException, GtuException,
            NetworkException, OtsGeometryException, ProbabilityException, ParameterException;

    /**
     * Class containing info of a GTU to generate.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     * @param <K> type of demand category, typically a Category in an OdMatrix or a GtuType
     */
    private static class PlatoonGtu<K> implements Comparable<PlatoonGtu<K>>
    {

        /** Time to generate. */
        private final Time time;

        /** Origin. */
        private final Node origin;

        /** Destination. */
        private final Node destination;

        /** Category. */
        private final K category;

        /**
         * Constructor.
         * @param time Time; time to generate
         * @param origin Node; origin
         * @param destination Node; destination
         * @param category K; category
         */
        PlatoonGtu(final Time time, final Node origin, final Node destination, final K category)
        {
            this.time = time;
            this.origin = origin;
            this.destination = destination;
            this.category = category;
        }

        /** {@inheritDoc} */
        @Override
        public int compareTo(final PlatoonGtu<K> o)
        {
            if (o == null)
            {
                return 1;
            }
            return this.time.compareTo(o.time);
        }

        /**
         * @return time.
         */
        protected Time getTime()
        {
            return this.time;
        }

        /**
         * @return origin.
         */
        protected Node getOrigin()
        {
            return this.origin;
        }

        /**
         * @return destination.
         */
        protected Node getDestination()
        {
            return this.destination;
        }

        /**
         * @return category.
         */
        protected K getCategory()
        {
            return this.category;
        }

    }
}
