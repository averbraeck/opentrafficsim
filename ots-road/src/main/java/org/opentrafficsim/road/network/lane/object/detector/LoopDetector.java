package org.opentrafficsim.road.network.lane.object.detector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.data.Column;
import org.djutils.data.Row;
import org.djutils.data.Table;
import org.djutils.event.EventType;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LanePosition;

/**
 * Detector, measuring a dynamic set of measurements typical for single- or dual-loop detectors. A subsidiary detector is placed
 * at a position downstream based on the detector length, that triggers when the rear leaves the detector.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LoopDetector extends LaneDetector
{

    /** */
    private static final long serialVersionUID = 20180312L;

    /** Trigger event. Payload: [Id of LaneBasedGtu]. */
    public static final EventType LOOP_DETECTOR_TRIGGERED =
            new EventType("LOOPDETECTOR.TRIGGER", new MetaData("Dual loop detector triggered", "Dual loop detector triggered",
                    new ObjectDescriptor[] {new ObjectDescriptor("Id of GTU", "Id of GTU", String.class)}));

    /** Aggregation event. Payload: [Frequency, measurement...]/ */
    public static final EventType LOOP_DETECTOR_AGGREGATE = new EventType("LOOPDETECTOR.AGGREGATE", MetaData.NO_META_DATA);

    /** Mean speed measurement. */
    public static final LoopDetectorMeasurement<Double, Speed> MEAN_SPEED =
            new LoopDetectorMeasurement<Double, Speed>("v", "mean speed", () -> 0.0, Speed.class, true)
            {
                @Override
                protected Double accumulateEntry(final Double cumulative, final LaneBasedGtu gtu,
                        final LoopDetector loopDetector)
                {
                    return cumulative + gtu.getSpeed().si;
                }

                @Override
                protected Double accumulateExit(final Double cumulative, final LaneBasedGtu gtu,
                        final LoopDetector loopDetector)
                {
                    return cumulative;
                }

                @Override
                protected Speed aggregate(final Double cumulative, final int gtuCount, final Duration aggregation)
                {
                    return new Speed(3.6 * cumulative / gtuCount, SpeedUnit.KM_PER_HOUR);
                }

                @Override
                public String getUnit()
                {
                    return "km/h";
                }
            };

    /** Harmonic mean speed measurement. */
    public static final LoopDetectorMeasurement<Double, Speed> HARMONIC_MEAN_SPEED =
            new LoopDetectorMeasurement<Double, Speed>("vHarm", "harmonic mean speed", () -> 0.0, Speed.class, true)
            {
                @Override
                protected Double accumulateEntry(final Double cumulative, final LaneBasedGtu gtu,
                        final LoopDetector loopDetector)
                {
                    return cumulative + (1.0 / gtu.getSpeed().si);
                }

                @Override
                protected Double accumulateExit(final Double cumulative, final LaneBasedGtu gtu,
                        final LoopDetector loopDetector)
                {
                    return cumulative;
                }

                @Override
                protected Speed aggregate(final Double cumulative, final int gtuCount, final Duration aggregation)
                {
                    return new Speed(3.6 * gtuCount / cumulative, SpeedUnit.KM_PER_HOUR);
                }

                @Override
                public String getUnit()
                {
                    return "km/h";
                }
            };

    /** Occupancy measurement. */
    public static final LoopDetectorMeasurement<Double, Double> OCCUPANCY = new LoopDetectorMeasurement<Double, Double>(
            "occupancy", "occupancy as fraction of time", () -> 0.0, Double.class, true)
    {
        /** Time the last GTU triggered the upstream detector. */
        private double lastEntry = Double.NaN;

        /** Id of last GTU that triggered the upstream detector. */
        private String lastId;

        @Override
        protected Double accumulateEntry(final Double cumulative, final LaneBasedGtu gtu, final LoopDetector loopDetector)
        {
            this.lastEntry = gtu.getSimulator().getSimulatorTime().si;
            this.lastId = gtu.getId();
            return cumulative;
        }

        @Override
        protected Double accumulateExit(final Double cumulative, final LaneBasedGtu gtu, final LoopDetector loopDetector)
        {
            if (!gtu.getId().equals(this.lastId))
            {
                // vehicle entered the lane along the length of the detector; we can skip as occupancy detector are not long
                return cumulative;
            }
            this.lastId = null;
            return cumulative + (gtu.getSimulator().getSimulatorTime().si - this.lastEntry);
        }

        @Override
        protected Double aggregate(final Double cumulative, final int gtuCount, final Duration aggregation)
        {
            return cumulative / aggregation.si;
        }
    };

    /** Passages measurement. */
    public static final LoopDetectorMeasurement<List<Duration>, List<Duration>> PASSAGES =
            new LoopDetectorMeasurement<List<Duration>, List<Duration>>("passage times", "list of vehicle passage time",
                    ArrayList::new, Duration.class, false)
            {
                @Override
                protected List<Duration> accumulateEntry(final List<Duration> cumulative, final LaneBasedGtu gtu,
                        final LoopDetector loopDetector)
                {
                    cumulative.add(gtu.getSimulator().getSimulatorTime());
                    return cumulative;
                }

                @Override
                protected List<Duration> accumulateExit(final List<Duration> cumulative, final LaneBasedGtu gtu,
                        final LoopDetector loopDetector)
                {
                    return cumulative;
                }

                @Override
                protected List<Duration> aggregate(final List<Duration> cumulative, final int gtuCount,
                        final Duration aggregation)
                {
                    return cumulative;
                }

                @Override
                public String getUnit()
                {
                    return "s";
                }
            };

    /** Aggregation time. */
    private final Duration aggregation;

    /** Aggregation time of current period, may differ due to offset at start. */
    private Duration currentAggregation;

    /** First aggregation. */
    private final Time firstAggregation;

    /** Detector length. */
    private final Length length;

    /** Period number. */
    private int period = 1;

    /** All measurements. */
    private final LoopDetectorMeasurement<?, ?>[] measurements;

    /** Measurements that are periodic. */
    private final Set<LoopDetectorMeasurement<?, ?>> periodicMeasurements;

    /** Data per GTU type. */
    private final Map<GtuType, GtuTypeData> data = new LinkedHashMap<>();

    /**
     * Constructor for regular Dutch dual-loop detectors measuring flow and mean speed aggregated over 60s.
     * @param id detector id
     * @param lane lane
     * @param longitudinalPosition position
     * @param detectorType detector type.
     * @throws NetworkException on network exception
     */
    public LoopDetector(final String id, final Lane lane, final Length longitudinalPosition, final DetectorType detectorType)
            throws NetworkException
    {
        // Note: length not important for flow and mean speed
        this(id, new LanePosition(lane, longitudinalPosition), Length.ZERO, detectorType, Time.instantiateSI(60.0),
                Duration.instantiateSI(60.0), MEAN_SPEED);
    }

    /**
     * Constructor.
     * @param id detector id
     * @param position lane position
     * @param length length
     * @param firstAggregation time of first aggregation
     * @param aggregation aggregation period
     * @param measurements measurements to obtain
     * @param detectorType detector type.
     * @throws NetworkException on network exception
     */
    public LoopDetector(final String id, final LanePosition position, final Length length, final DetectorType detectorType,
            final Time firstAggregation, final Duration aggregation, final LoopDetectorMeasurement<?, ?>... measurements)
            throws NetworkException
    {
        super(id, position.lane(), position.position(), RelativePosition.FRONT, detectorType);
        Throw.when(aggregation.si <= 0.0, IllegalArgumentException.class, "Aggregation time should be positive.");
        this.length = length;
        this.currentAggregation = Duration.instantiateSI(firstAggregation.si);
        this.aggregation = aggregation;
        this.firstAggregation = firstAggregation;
        Try.execute(() -> getSimulator().scheduleEventAbsTime(firstAggregation, this, "aggregate", null), "");
        this.measurements = measurements;
        this.periodicMeasurements = Arrays.stream(measurements).filter((m) -> m.isPeriodic()).collect(Collectors.toSet());
        this.data.put(null, new GtuTypeData());

        // rear detector
        class RearDetector extends LaneDetector
        {
            /** */
            private static final long serialVersionUID = 20180315L;

            /**
             * Constructor.
             * @param idRear id
             * @param laneRear lane
             * @param longitudinalPositionRear position
             * @param detectorType detector type.
             * @throws NetworkException on network exception
             */
            RearDetector(final String idRear, final Lane laneRear, final Length longitudinalPositionRear,
                    final DetectorType detectorType) throws NetworkException
            {
                super(idRear, laneRear, longitudinalPositionRear, RelativePosition.REAR, detectorType);
            }

            /** {@inheritDoc} */
            @Override
            protected void triggerResponse(final LaneBasedGtu gtu)
            {
                for (GtuTypeData dat : forData(gtu))
                {
                    for (LoopDetectorMeasurement<?, ?> measurement : LoopDetector.this.measurements)
                    {
                        accumulate(measurement, gtu, dat, false);
                    }
                }
            }
        }
        Length pos = position.position().plus(length);
        new RearDetector(id + "_rear", position.lane(), pos, detectorType);
    }

    /**
     * Store data specific to GTU type.
     * @param gtuTypes GTU type
     */
    public void specificDataFor(final GtuType... gtuTypes)
    {
        for (GtuType gtuType : gtuTypes)
        {
            this.data.put(gtuType, new GtuTypeData());
        }
    }

    /**
     * Returns an iterable over valid data objects to collect the GTU. This is for all GTU types, and possibly also for the
     * specific GTU type.
     * @param gtu GTU
     * @return iterable over valid data objects to collect the GTU
     */
    private Iterable<GtuTypeData> forData(final LaneBasedGtu gtu)
    {
        if (this.data.containsKey(gtu.getType()))
        {
            return Set.of(this.data.get(null), this.data.get(gtu.getType()));
        }
        return Set.of(this.data.get(null));
    }

    /** {@inheritDoc} */
    @Override
    public Length getLength()
    {
        return this.length;
    }

    /** {@inheritDoc} */
    @Override
    protected void triggerResponse(final LaneBasedGtu gtu)
    {
        for (GtuTypeData dat : forData(gtu))
        {
            dat.gtuCountCurrentPeriod++;
            for (LoopDetectorMeasurement<?, ?> measurement : this.measurements)
            {
                accumulate(measurement, gtu, dat, true);
            }
        }
        this.fireTimedEvent(LOOP_DETECTOR_TRIGGERED, new Object[] {gtu.getId()}, getSimulator().getSimulatorTime());
    }

    /**
     * Accumulates a measurement.
     * @param measurement measurement to accumulate
     * @param gtu GTU
     * @param dat relevant GTU type data
     * @param front triggered by front entering (or rear leaving when false)
     * @param <C> accumulated type
     */
    @SuppressWarnings("unchecked")
    <C> void accumulate(final LoopDetectorMeasurement<C, ?> measurement, final LaneBasedGtu gtu, final GtuTypeData dat,
            final boolean front)
    {
        if (front)
        {
            dat.currentCumulativeDataMap.put(measurement,
                    measurement.accumulateEntry((C) dat.currentCumulativeDataMap.get(measurement), gtu, this));
        }
        else
        {
            dat.currentCumulativeDataMap.put(measurement,
                    measurement.accumulateExit((C) dat.currentCumulativeDataMap.get(measurement), gtu, this));
        }
    }

    /**
     * Aggregation.
     */
    @SuppressWarnings("unused") // called by event
    private void aggregate()
    {
        for (GtuTypeData dat : this.data.values())
        {
            Frequency frequency =
                    new Frequency(3600.0 * dat.gtuCountCurrentPeriod / this.currentAggregation.si, FrequencyUnit.PER_HOUR);
            dat.flow.add(frequency);
            for (LoopDetectorMeasurement<?, ?> measurement : this.periodicMeasurements)
            {
                aggregate(measurement, this.currentAggregation, dat);
                dat.currentCumulativeDataMap.put(measurement, measurement.identity());
            }
            dat.gtuCountCurrentPeriod = 0;
            if (!getListenerReferences(LOOP_DETECTOR_AGGREGATE).isEmpty())
            {
                Object[] dataArray = new Object[dat.periodicDataMap.size() + 1];
                dataArray[0] = frequency;
                int i = 1;
                for (LoopDetectorMeasurement<?, ?> measurement : dat.periodicDataMap.keySet())
                {
                    List<?> list = dat.periodicDataMap.get(measurement);
                    dataArray[i] = list.get(list.size() - 1);
                    i++;
                }
                this.fireTimedEvent(LOOP_DETECTOR_AGGREGATE, dataArray, getSimulator().getSimulatorTime());
            }
        }
        this.currentAggregation = this.aggregation; // after first possibly irregular period, all periods regular
        Time time = Time.instantiateSI(this.firstAggregation.si + this.aggregation.si * this.period++);
        Try.execute(() -> getSimulator().scheduleEventAbsTime(time, this, "aggregate", null), "");
    }

    /**
     * Returns whether the detector has aggregated data available.
     * @return whether the detector has aggregated data available
     */
    public boolean hasLastValue()
    {
        return !this.data.get(null).flow.isEmpty();
    }

    /**
     * Returns the last flow.
     * @return last flow
     */
    public Frequency getLastFlow()
    {
        List<Frequency> flow = this.data.get(null).flow;
        return flow.get(flow.size() - 1);
    }

    /**
     * Returns the last value of the detector measurement.
     * @param detectorMeasurement detector measurement
     * @return last value of the detector measurement
     * @param <A> aggregate value type of the detector measurement
     */
    public <A> A getLastValue(final LoopDetectorMeasurement<?, A> detectorMeasurement)
    {
        @SuppressWarnings("unchecked")
        List<A> list = (List<A>) this.data.get(null).periodicDataMap.get(detectorMeasurement);
        return list.get(list.size() - 1);
    }

    /**
     * Aggregates a periodic measurement.
     * @param measurement measurement to aggregate
     * @param agg aggregation period
     * @param dat GTU type data
     * @param <C> accumulated type
     * @param <A> aggregated type
     */
    @SuppressWarnings("unchecked")
    private <C, A> void aggregate(final LoopDetectorMeasurement<C, A> measurement, final Duration agg, final GtuTypeData dat)
    {
        ((List<A>) dat.periodicDataMap.get(measurement)).add(getAggregateValue(measurement, agg, dat));
    }

    /**
     * Returns the aggregated value of the measurement.
     * @param measurement measurement to aggregate
     * @param agg aggregation period
     * @param dat GTU type data
     * @return aggregated value of the measurement
     * @param <C> accumulated type
     * @param <A> aggregated type
     */
    @SuppressWarnings("unchecked")
    private <C, A> A getAggregateValue(final LoopDetectorMeasurement<C, A> measurement, final Duration agg,
            final GtuTypeData dat)
    {
        return measurement.aggregate((C) dat.currentCumulativeDataMap.get(measurement), dat.gtuCountCurrentPeriod, agg);
    }

    /**
     * Returns a map of non-periodic measurements, mapping measurement type and the data.
     * @param dat GTU type data
     * @return map of non-periodic measurements
     */
    private Map<LoopDetectorMeasurement<?, ?>, Object> getNonPeriodicMeasurements(final GtuTypeData dat)
    {
        Map<LoopDetectorMeasurement<?, ?>, Object> map = new LinkedHashMap<>();
        for (LoopDetectorMeasurement<?, ?> measurement : dat.currentCumulativeDataMap.keySet())
        {
            if (!measurement.isPeriodic())
            {
                map.put(measurement,
                        getAggregateValue(measurement, getSimulator().getSimulatorAbsTime().minus(Time.ZERO), dat));
            }
        }
        return map;
    }

    /**
     * Return aggregated data for the measurement for all GTU types.
     * @param <T> measurement value type, use {@code null} to obtain flow data which is measured by default
     * @param measurement measurement
     * @return aggregated data for the measurement for all GTU types
     * @throws IllegalArgumentException when the measurement is not periodic
     */
    public <T> List<T> getPeriodicData(final LoopDetectorMeasurement<?, T> measurement)
    {
        return getPeriodicData(measurement, null);
    }

    /**
     * Return aggregated data for the measurement and GTU type.
     * @param <T> measurement value type, use {@code null} to obtain flow data which is measured by default
     * @param measurement measurement
     * @param gtuType GTU type
     * @return aggregated data for the measurement and GTU type
     * @throws IllegalArgumentException when there is no data for the GTU type, or the measurement is not periodic
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> getPeriodicData(final LoopDetectorMeasurement<?, T> measurement, final GtuType gtuType)
    {
        Throw.when(!this.data.containsKey(gtuType), IllegalArgumentException.class, "No data for %s.", gtuType);
        if (measurement == null)
        {
            return new ArrayList<>((List<T>) this.data.get(gtuType).flow);
        }
        Throw.when(!measurement.isPeriodic(), IllegalArgumentException.class, "Measurement %s is not periodic.",
                measurement.getName());
        return new ArrayList<>((List<T>) this.data.get(gtuType).periodicDataMap.get(measurement));
    }

    /**
     * Return non-periodic data for the measurement for all GTU types.
     * @param <T> measurement value type
     * @param measurement measurement
     * @return non-periodic data for the measurement for all GTU types
     * @throws IllegalArgumentException when the measurement is not non-periodic
     */
    public <T> T getNonPeriodicData(final LoopDetectorMeasurement<?, T> measurement)
    {
        return getNonPeriodicData(measurement, null);
    }

    /**
     * Return non-periodic data for the measurement and GTU type.
     * @param <T> measurement value type
     * @param measurement measurement
     * @param gtuType GTU type
     * @return non-periodic data for the measurement and GTU type
     * @throws IllegalArgumentException when there is no data for the GTU type, or the measurement is not non-periodic
     */
    public <T> T getNonPeriodicData(final LoopDetectorMeasurement<?, T> measurement, final GtuType gtuType)
    {
        Throw.when(!this.data.containsKey(gtuType), IllegalArgumentException.class, "No data for %s.", gtuType);
        Throw.when(measurement.isPeriodic(), IllegalArgumentException.class, "Measurement %s is not non-periodic.",
                measurement.getName());
        return (T) getAggregateValue(measurement, getSimulator().getSimulatorAbsTime().minus(Time.ZERO),
                this.data.get(gtuType));
    }

    /**
     * Returns a Table with loop detector positions.
     * @param network network from which all detectors are found.
     * @return with loop detector positions.
     */
    public static Table asTablePositions(final RoadNetwork network)
    {
        Set<LoopDetector> detectors = getLoopDetectors(network);
        Collection<Column<?>> columns = new LinkedHashSet<>();
        columns.add(new Column<>("id", "detector id", String.class, null));
        columns.add(new Column<>("laneId", "lane id", String.class, null));
        columns.add(new Column<>("linkId", "link id", String.class, null));
        columns.add(new Column<>("position", "detector position on the lane", Length.class, "m"));

        return new Table("detectors", "list of all loop-detectors", columns)
        {
            /** {@inheritDoc} */
            @Override
            public Iterator<Row> iterator()
            {
                Iterator<LoopDetector> iterator = detectors.iterator();
                return new Iterator<>()
                {
                    /** {@inheritDoc} */
                    @Override
                    public boolean hasNext()
                    {
                        return iterator.hasNext();
                    }

                    /** {@inheritDoc} */
                    @Override
                    public Row next()
                    {
                        LoopDetector detector = iterator.next();
                        return new Row(table(), new Object[] {detector.getId(), detector.getLane().getId(),
                                detector.getLane().getLink().getId(), detector.getLongitudinalPosition()});
                    }
                };
            }

            /**
             * Returns this table instance for inner classes as {@code Table.this} is not possible in an anonymous Table class.
             * @return this table instance for inner classes.
             */
            private Table table()
            {
                return this;
            }

            /** {@inheritDoc} */
            @Override
            public boolean isEmpty()
            {
                return detectors.isEmpty();
            }
        };
    }

    /**
     * Returns a Table with all periodic data, such as flow and speed per minute.
     * @param network network from which all detectors are found.
     * @param gtuTypes GTU types to include. When left empty, data for all is exported without GTU type column. To include data
     *            for all GTU types among data for specific GTU types, include {@code null} value.
     * @return with all periodic data, such as flow and speed per minute.
     */
    public static Table asTablePeriodicData(final RoadNetwork network, final GtuType... gtuTypes)
    {
        Set<LoopDetector> detectors = getLoopDetectors(network);
        Set<LoopDetectorMeasurement<?, ?>> measurements = getMeasurements(detectors, true);
        Collection<Column<?>> columns = new LinkedHashSet<>();
        columns.add(new Column<>("id", "detector id", String.class, null));
        boolean includeGtuTypeColumn = gtuTypes.length > 0;
        if (includeGtuTypeColumn)
        {
            columns.add(new Column<>("GTU type", "GTU type", String.class, null));
        }
        columns.add(new Column<>("t", "time (start of aggregation period)", Duration.class, "s"));
        columns.add(new Column<>("q", "flow", Frequency.class, "/h"));
        for (LoopDetectorMeasurement<?, ?> measurement : measurements)
        {
            columns.add(new Column<>(measurement.getName(), measurement.getDescription(), measurement.getValueType(),
                    measurement.getUnit()));
        }

        return new Table("periodic", "periodic measurements", columns)
        {
            /** {@inheritDoc} */
            @Override
            public Iterator<Row> iterator()
            {
                Iterator<LoopDetector> iterator = detectors.iterator();
                Predicate<Entry<GtuType, GtuTypeData>> gtuTypeEntryFilter =
                        (entry) -> !includeGtuTypeColumn && entry.getKey() == null
                                || Arrays.stream(gtuTypes).anyMatch((g) -> Objects.equals(g, entry.getKey()));
                return new Iterator<>()
                {
                    /** GTU type data iterator. */
                    private Iterator<Entry<GtuType, GtuTypeData>> dataIterator = Collections.emptyIterator();

                    /** Index iterator. */
                    private Iterator<Integer> indexIterator = Collections.emptyIterator();

                    /** Current loop detector. */
                    private LoopDetector loopDetector;

                    /** Current GTU type data. */
                    private Entry<GtuType, GtuTypeData> dat;

                    /** {@inheritDoc} */
                    @Override
                    public boolean hasNext()
                    {
                        while (!this.indexIterator.hasNext())
                        {
                            while (!this.dataIterator.hasNext())
                            {
                                if (!iterator.hasNext())
                                {
                                    return false;
                                }
                                this.loopDetector = iterator.next();
                                this.dataIterator =
                                        this.loopDetector.data.entrySet().stream().filter(gtuTypeEntryFilter).iterator();
                            }
                            this.dat = this.dataIterator.next();
                            this.indexIterator = IntStream.range(0, this.loopDetector.data.get(null).flow.size()).iterator();
                        }
                        return true;
                    }

                    /** {@inheritDoc} */
                    @Override
                    public Row next()
                    {
                        Throw.when(!hasNext(), NoSuchElementException.class, "Periodic data unavailable.");
                        Object[] data = new Object[columns.size()];
                        int index = this.indexIterator.next();

                        double t = index == 0 ? 0.0
                                : this.loopDetector.firstAggregation.si + (index - 1) * this.loopDetector.aggregation.si;
                        int dataIndex = 0;
                        data[dataIndex++] = this.loopDetector.getId();
                        if (includeGtuTypeColumn)
                        {
                            data[dataIndex++] = this.dat.getKey() == null ? "" : this.dat.getKey().getId();
                        }
                        data[dataIndex++] = Duration.instantiateSI(t < 0.0 ? 0.0 : t);
                        data[dataIndex++] = this.dat.getValue().flow.get(index);
                        for (LoopDetectorMeasurement<?, ?> measurement : measurements)
                        {
                            if (this.dat.getValue().periodicDataMap.containsKey(measurement))
                            {
                                data[dataIndex++] = this.dat.getValue().periodicDataMap.get(measurement).get(index);
                            }
                            else
                            {
                                // this data is not available for this detector
                                data[dataIndex++] = null;
                            }
                        }
                        return new Row(table(), data);
                    }
                };
            }

            /**
             * Returns this table instance for inner classes as {@code Table.this} is not possible in an anonymous Table class.
             * @return this table instance for inner classes.
             */
            private Table table()
            {
                return this;
            }

            /** {@inheritDoc} */
            @Override
            public boolean isEmpty()
            {
                return detectors.isEmpty() || !detectors.iterator().next().hasLastValue();
            }
        };
    }

    /**
     * Returns a Table with all non-periodic data, such as vehicle passage times or platoon counts.
     * @param network network from which all detectors are found.
     * @param gtuTypes GTU types to include. When left empty, data for all is exported without GTU type column. To include data
     *            for all GTU types among data for specific GTU types, include {@code null} value.
     * @return with all non-periodic data, such as vehicle passage times or platoon counts.
     */
    public static Table asTableNonPeriodicData(final RoadNetwork network, final GtuType... gtuTypes)
    {
        Set<LoopDetector> detectors = getLoopDetectors(network);
        Set<LoopDetectorMeasurement<?, ?>> measurements = getMeasurements(detectors, false);
        Collection<Column<?>> columns = new LinkedHashSet<>();
        columns.add(new Column<>("id", "detector id", String.class, null));
        boolean includeGtuTypeColumn = gtuTypes.length > 0;
        if (includeGtuTypeColumn)
        {
            columns.add(new Column<>("GTU type", "GTU type", String.class, null));
        }
        columns.add(new Column<>("measurement", "measurement type", String.class, null));
        columns.add(new Column<>("data", "data in any form", String.class, null));

        return new Table("non-periodic", "non-periodic measurements", columns)
        {
            /** {@inheritDoc} */
            @Override
            public Iterator<Row> iterator()
            {
                Iterator<LoopDetector> iterator = detectors.iterator();
                Predicate<Entry<GtuType, GtuTypeData>> gtuTypeEntryFilter =
                        (entry) -> !includeGtuTypeColumn && entry.getKey() == null
                                || Arrays.stream(gtuTypes).anyMatch((g) -> Objects.equals(g, entry.getKey()));
                return new Iterator<>()
                {
                    /** GTU type data iterator. */
                    private Iterator<Entry<GtuType, GtuTypeData>> dataIterator = Collections.emptyIterator();

                    /** Measurement iterator. */
                    private Iterator<LoopDetectorMeasurement<?, ?>> measurementIterator = Collections.emptyIterator();

                    /** Current loop detector. */
                    private LoopDetector loopDetector;

                    /** Current GTU type data. */
                    private Entry<GtuType, GtuTypeData> dat;

                    /** Map of measurement data per measurement, updated for each detector. */
                    private Map<LoopDetectorMeasurement<?, ?>, Object> map;

                    /** Current measurement. */
                    private LoopDetectorMeasurement<?, ?> measurement;

                    /** {@inheritDoc} */
                    @Override
                    public boolean hasNext()
                    {
                        if (this.measurement != null)
                        {
                            return true; // catch consecutive 'hasNext' calls before next 'next' call
                        }
                        while (!this.measurementIterator.hasNext())
                        {
                            while (!this.dataIterator.hasNext())
                            {
                                if (!iterator.hasNext())
                                {
                                    return false;
                                }
                                this.loopDetector = iterator.next();
                                this.dataIterator =
                                        this.loopDetector.data.entrySet().stream().filter(gtuTypeEntryFilter).iterator();
                            }
                            this.dat = this.dataIterator.next();
                            this.map = this.loopDetector.getNonPeriodicMeasurements(this.dat.getValue());
                            Set<LoopDetectorMeasurement<?, ?>> set = new LinkedHashSet<>(measurements);
                            set.retainAll(this.map.keySet()); // only those that are available in this detector
                            this.measurementIterator = set.iterator();
                        }
                        this.measurement = this.measurementIterator.next();
                        return true;
                    }

                    /** {@inheritDoc} */
                    @Override
                    public Row next()
                    {
                        Throw.when(!hasNext(), NoSuchElementException.class, "Non-periodic data unavailable.");
                        Object[] data = new Object[columns.size()];
                        int dataIndex = 0;
                        data[dataIndex++] = this.loopDetector.getId();
                        if (includeGtuTypeColumn)
                        {
                            data[dataIndex++] = this.dat.getKey() == null ? "" : this.dat.getKey().getId();
                        }
                        data[dataIndex++] = this.measurement.getName();
                        data[dataIndex++] = this.map.get(this.measurement).toString();
                        this.measurement = null;
                        return new Row(table(), data);
                    }
                };
            }

            /**
             * Returns this table instance for inner classes as {@code Table.this} is not possible in an anonymous Table class.
             * @return this table instance for inner classes.
             */
            private Table table()
            {
                return this;
            }

            /** {@inheritDoc} */
            @Override
            public boolean isEmpty()
            {
                return detectors.isEmpty() || measurements.isEmpty();
            }
        };
    }

    /**
     * Gathers all loop detectors from the network and puts them in a set sorted by loop detector id.
     * @param network network.
     * @return set of loop detector sorted by loop detector id.
     */
    private static Set<LoopDetector> getLoopDetectors(final RoadNetwork network)
    {
        Set<LoopDetector> detectors = new TreeSet<>(new Comparator<LoopDetector>()
        {
            @Override
            public int compare(final LoopDetector o1, final LoopDetector o2)
            {
                return o1.getId().compareTo(o2.getId());
            }
        });
        detectors.addAll(network.getObjectMap(LoopDetector.class).values().toCollection());
        return detectors;
    }

    /**
     * Returns all measurement type that are found across a set of loop detectors.
     * @param detectors set of loop detectors.
     * @param periodic gather the periodic measurements {@code true}, or the non-periodic measurements {@code false}.
     * @return set of periodic or non-periodic measurements from the detectors.
     */
    private static Set<LoopDetectorMeasurement<?, ?>> getMeasurements(final Set<LoopDetector> detectors, final boolean periodic)
    {
        Set<LoopDetectorMeasurement<?, ?>> set = new LinkedHashSet<>();
        for (LoopDetector detector : detectors)
        {
            for (LoopDetectorMeasurement<?, ?> measurement : detector.measurements)
            {
                if (measurement.isPeriodic() == periodic)
                {
                    set.add(measurement);
                }
            }
        }
        return set;
    }

    /**
     * Interface for what detectors measure.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     * @param <C> accumulated type
     * @param <A> aggregated type
     */
    public abstract static class LoopDetectorMeasurement<C, A>
    {
        /** Name. */
        private final String name;

        /** Description. */
        private final String description;

        /** Identity supplier. */
        private final Supplier<C> identity;

        /** Value type. */
        private final Class<?> valueType;

        /** Periodic. */
        private final boolean periodic;

        /**
         * Constructor.
         * @param name name
         * @param description description
         * @param identity identity supplier
         * @param valueType value type, either same as {@code A}, or e.g. {@code T} when {@code A} is {@code List<T>}
         * @param periodic periodic measurement (or non-periodic)
         */
        public LoopDetectorMeasurement(final String name, final String description, final Supplier<C> identity,
                final Class<?> valueType, final boolean periodic)
        {
            this.name = name;
            this.description = description;
            this.identity = identity;
            this.valueType = valueType;
            this.periodic = periodic;
        }

        /**
         * Returns the initial value before accumulation.
         * @return initial value before accumulation
         */
        public C identity()
        {
            return this.identity.get();
        }

        /**
         * Returns an accumulated value for when the front reaches the detector. GTU's may trigger an exit without having
         * triggered an entry due to a lane change. Reversely, GTU's may not trigger an exit while they did trigger an entry.
         * @param cumulative accumulated value
         * @param gtu gtu
         * @param loopDetector loop detector
         * @return accumulated value
         */
        protected abstract C accumulateEntry(C cumulative, LaneBasedGtu gtu, LoopDetector loopDetector);

        /**
         * Returns an accumulated value for when the rear leaves the detector. GTU's may trigger an exit without having
         * triggered an entry due to a lane change. Reversely, GTU's may not trigger an exit while they did trigger an entry.
         * @param cumulative accumulated value
         * @param gtu gtu
         * @param loopDetector loop detector
         * @return accumulated value
         */
        protected abstract C accumulateExit(C cumulative, LaneBasedGtu gtu, LoopDetector loopDetector);

        /**
         * Returns whether the measurement aggregates every aggregation period (or only over the entire simulation).
         * @return whether the measurement aggregates every aggregation period (or only over the entire simulation)
         */
        public boolean isPeriodic()
        {
            return this.periodic;
        }

        /**
         * Returns an aggregated value.
         * @param cumulative accumulated value
         * @param gtuCount GTU gtuCount
         * @param aggregation aggregation period
         * @return aggregated value
         */
        protected abstract A aggregate(C cumulative, int gtuCount, Duration aggregation);

        /**
         * Returns the value name.
         * @return value name
         */
        public String getName()
        {
            return this.name;
        }

        /**
         * Measurement description.
         * @return measurement description.
         */
        public String getDescription()
        {
            return this.description;
        }

        /**
         * Returns the unit string, default is {@code null}.
         * @return unit string.
         */
        public String getUnit()
        {
            return null;
        }

        /**
         * Returns the data type.
         * @return data type.
         */
        public Class<?> getValueType()
        {
            return this.valueType;
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return getName();
        }
    }

    /**
     * Data holder per GTU type.
     */
    private class GtuTypeData
    {
        /** Flow per aggregation period. */
        private final List<Frequency> flow = new ArrayList<>();

        /** Measurements per aggregation period. */
        private final Map<LoopDetectorMeasurement<?, ?>, List<?>> periodicDataMap = new LinkedHashMap<>();

        /** Count in current period. */
        private int gtuCountCurrentPeriod = 0;

        /** Current cumulative measurements. */
        private final Map<LoopDetectorMeasurement<?, ?>, Object> currentCumulativeDataMap = new LinkedHashMap<>();

        /**
         * Constructor.
         */
        GtuTypeData()
        {
            for (LoopDetectorMeasurement<?, ?> measurement : LoopDetector.this.measurements)
            {
                this.currentCumulativeDataMap.put(measurement, measurement.identity());
                if (measurement.isPeriodic())
                {
                    this.periodicDataMap.put(measurement, new ArrayList<>());
                }
            }
        }
    }

    /**
     * Measurement of platoon sizes based on time between previous GTU exit and GTU entry.
     */
    public static class PlatoonSizes extends LoopDetectorMeasurement<PlatoonMeasurement, List<Integer>>
    {
        /** Maximum time between two vehicles that are considered to be in the same platoon. */
        private final Duration threshold;

        /**
         * Constructor.
         * @param threshold maximum time between two vehicles that are considered to be in the same platoon
         */
        public PlatoonSizes(final Duration threshold)
        {
            super("platoon sizes", "", () -> new PlatoonMeasurement(), Integer.class, false);
            this.threshold = threshold;
        }

        /** {@inheritDoc} */
        @Override
        protected PlatoonMeasurement accumulateEntry(final PlatoonMeasurement cumulative, final LaneBasedGtu gtu,
                final LoopDetector loopDetector)
        {
            Time now = gtu.getSimulator().getSimulatorAbsTime();
            if (now.si - cumulative.lastExitTime.si < this.threshold.si)
            {
                cumulative.gtuCount++;
            }
            else
            {
                if (cumulative.gtuCount > 0) // 0 means this is the first vehicle of the first platoon
                {
                    cumulative.platoons.add(cumulative.gtuCount);
                }
                cumulative.gtuCount = 1;
            }
            cumulative.enteredGTUs.add(gtu);
            cumulative.lastExitTime = now; // should we change lane before triggering the exit
            return cumulative;
        }

        /** {@inheritDoc} */
        @Override
        protected PlatoonMeasurement accumulateExit(final PlatoonMeasurement cumulative, final LaneBasedGtu gtu,
                final LoopDetector loopDetector)
        {
            int index = cumulative.enteredGTUs.indexOf(gtu);
            if (index >= 0)
            {
                cumulative.lastExitTime = gtu.getSimulator().getSimulatorAbsTime();
                // gtu is likely the oldest gtu in the list at index 0, but sometimes an older gtu may have left the detector by
                // changing lane, by clearing up to this gtu, older gtu's are automatically removed
                cumulative.enteredGTUs.subList(0, index).clear();
            }
            return cumulative;
        }

        /** {@inheritDoc} */
        @Override
        protected List<Integer> aggregate(final PlatoonMeasurement cumulative, final int count, final Duration aggregation)
        {
            if (cumulative.gtuCount > 0)
            {
                cumulative.platoons.add(cumulative.gtuCount);
                cumulative.gtuCount = 0; // prevent that the last platoon is added again if the same output is saved again
            }
            return cumulative.platoons;
        }

        /** {@inheritDoc} */
        @Override
        public String getDescription()
        {
            return "list of platoon sizes (threshold: " + this.threshold + ")";
        }
    }

    /**
     * Cumulative information for platoon size measurement.
     */
    static class PlatoonMeasurement
    {
        /** GTU's counted so far in the current platoon. */
        private int gtuCount = 0;

        /** Time the last GTU exited the detector. */
        private Time lastExitTime = Time.instantiateSI(Double.NEGATIVE_INFINITY);

        /** Stored sizes of earlier platoons. */
        private List<Integer> platoons = new ArrayList<>();

        /** GTU's currently on the detector, some may have left by a lane change. */
        private List<LaneBasedGtu> enteredGTUs = new ArrayList<>();
    }

}
