package org.opentrafficsim.road.network.lane.object.detector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Detector, measuring a dynamic set of measurements typical for single- or dual-loop detectors. A subsidiary detector is placed
 * at a position downstream based on the detector length, that triggers when the rear leaves the detector.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
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
    public static final LoopDetectorMeasurement<Double, Speed> MEAN_SPEED = new LoopDetectorMeasurement<Double, Speed>()
    {
        @Override
        public Double identity()
        {
            return 0.0;
        }

        @Override
        public Double accumulateEntry(final Double cumulative, final LaneBasedGtu gtu, final LoopDetector loopDetector)
        {
            return cumulative + gtu.getSpeed().si;
        }

        @Override
        public Double accumulateExit(final Double cumulative, final LaneBasedGtu gtu, final LoopDetector loopDetector)
        {
            return cumulative;
        }

        @Override
        public boolean isPeriodic()
        {
            return true;
        }

        @Override
        public Speed aggregate(final Double cumulative, final int gtuCount, final Duration aggregation)
        {
            return new Speed(3.6 * cumulative / gtuCount, SpeedUnit.KM_PER_HOUR);
        }

        @Override
        public String getName()
        {
            return "v";
        }

        @Override
        public String toString()
        {
            return getName();
        }

        @Override
        public String getDescription()
        {
            return "mean speed";
        }

        @Override
        public String getUnit()
        {
            return "km/h";
        }

        @Override
        public Class<Speed> getValueType()
        {
            return Speed.class;
        }
    };

    /** Harmonic mean speed measurement. */
    public static final LoopDetectorMeasurement<Double, Speed> HARMONIC_MEAN_SPEED =
            new LoopDetectorMeasurement<Double, Speed>()
            {
                @Override
                public Double identity()
                {
                    return 0.0;
                }

                @Override
                public Double accumulateEntry(final Double cumulative, final LaneBasedGtu gtu, final LoopDetector loopDetector)
                {
                    return cumulative + (1.0 / gtu.getSpeed().si);
                }

                @Override
                public Double accumulateExit(final Double cumulative, final LaneBasedGtu gtu, final LoopDetector loopDetector)
                {
                    return cumulative;
                }

                @Override
                public boolean isPeriodic()
                {
                    return true;
                }

                @Override
                public Speed aggregate(final Double cumulative, final int gtuCount, final Duration aggregation)
                {
                    return new Speed(3.6 * gtuCount / cumulative, SpeedUnit.KM_PER_HOUR);
                }

                @Override
                public String getName()
                {
                    return "vHarm";
                }

                @Override
                public String toString()
                {
                    return getName();
                }

                @Override
                public String getDescription()
                {
                    return "harmonic mean speed";
                }

                @Override
                public String getUnit()
                {
                    return "km/h";
                }

                @Override
                public Class<Speed> getValueType()
                {
                    return Speed.class;
                }
            };

    /** Occupancy measurement. */
    public static final LoopDetectorMeasurement<Double, Double> OCCUPANCY = new LoopDetectorMeasurement<Double, Double>()
    {
        /** Time the last GTU triggered the upstream detector. */
        private double lastEntry = Double.NaN;

        /** Id of last GTU that triggered the upstream detector. */
        private String lastId;

        @Override
        public Double identity()
        {
            return 0.0;
        }

        @Override
        public Double accumulateEntry(final Double cumulative, final LaneBasedGtu gtu, final LoopDetector loopDetector)
        {
            this.lastEntry = gtu.getSimulator().getSimulatorTime().si;
            this.lastId = gtu.getId();
            return cumulative;
        }

        @Override
        public Double accumulateExit(final Double cumulative, final LaneBasedGtu gtu, final LoopDetector loopDetector)
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
        public boolean isPeriodic()
        {
            return true;
        }

        @Override
        public Double aggregate(final Double cumulative, final int gtuCount, final Duration aggregation)
        {
            return cumulative / aggregation.si;
        }

        @Override
        public String getName()
        {
            return "occupancy";
        }

        @Override
        public String toString()
        {
            return getName();
        }

        @Override
        public String getDescription()
        {
            return "occupancy as fraction of time";
        }

        @Override
        public Class<Double> getValueType()
        {
            return Double.class;
        }
    };

    /** Passages measurement. */
    public static final LoopDetectorMeasurement<List<Duration>, List<Duration>> PASSAGES =
            new LoopDetectorMeasurement<List<Duration>, List<Duration>>()
            {
                @Override
                public List<Duration> identity()
                {
                    return new ArrayList<>();
                }

                @Override
                public List<Duration> accumulateEntry(final List<Duration> cumulative, final LaneBasedGtu gtu,
                        final LoopDetector loopDetector)
                {
                    cumulative.add(gtu.getSimulator().getSimulatorTime());
                    return cumulative;
                }

                @Override
                public List<Duration> accumulateExit(final List<Duration> cumulative, final LaneBasedGtu gtu,
                        final LoopDetector loopDetector)
                {
                    return cumulative;
                }

                @Override
                public boolean isPeriodic()
                {
                    return false;
                }

                @Override
                public List<Duration> aggregate(final List<Duration> cumulative, final int gtuCount, final Duration aggregation)
                {
                    return cumulative;
                }

                @Override
                public String getName()
                {
                    return "passage times";
                }

                @Override
                public String toString()
                {
                    return getName();
                }

                @Override
                public String getDescription()
                {
                    return "list of vehicle passage time";
                }

                @Override
                public String getUnit()
                {
                    return "s";
                }

                @Override
                public Class<Duration> getValueType()
                {
                    return Duration.class;
                }
            };

    /** Aggregation time. */
    private final Duration aggregation;

    /** Aggregation time of current period, may differ due to offset at start. */
    private Duration currentAggregation;

    /** First aggregation. */
    private final Time firstAggregation;

    /** Flow per aggregation period. */
    private final List<Frequency> flow = new ArrayList<>();

    /** Measurements per aggregation period. */
    private final Map<LoopDetectorMeasurement<?, ?>, List<?>> periodicDataMap = new LinkedHashMap<>();

    /** Detector length. */
    private final Length length;

    /** Period number. */
    private int period = 1;

    /** Count in current period. */
    private int gtuCountCurrentPeriod = 0;

    /** Count overall. */
    private int overallGtuCount = 0;

    /** Current cumulative measurements. */
    private final Map<LoopDetectorMeasurement<?, ?>, Object> currentCumulativeDataMap = new LinkedHashMap<>();

    /**
     * Constructor for regular Dutch dual-loop detectors measuring flow and mean speed aggregated over 60s.
     * @param id String; detector id
     * @param lane Lane; lane
     * @param longitudinalPosition Length; position
     * @param simulator OtsSimulatorInterface; simulator
     * @param detectorType DetectorType; detector type.
     * @throws NetworkException on network exception
     */
    public LoopDetector(final String id, final Lane lane, final Length longitudinalPosition, final DetectorType detectorType,
            final OtsSimulatorInterface simulator) throws NetworkException
    {
        // Note: length not important for flow and mean speed
        this(id, lane, longitudinalPosition, Length.ZERO, detectorType, simulator, Time.instantiateSI(60.0),
                Duration.instantiateSI(60.0), MEAN_SPEED);
    }

    /**
     * Constructor.
     * @param id String; detector id
     * @param lane Lane; lane
     * @param longitudinalPosition Length; position
     * @param length Length; length
     * @param simulator OtsSimulatorInterface; simulator
     * @param firstAggregation Time; time of first aggregation
     * @param aggregation Duration; aggregation period
     * @param measurements DetectorMeasurement&lt;?, ?&gt;...; measurements to obtain
     * @param detectorType DetectorType; detector type.
     * @throws NetworkException on network exception
     */
    public LoopDetector(final String id, final Lane lane, final Length longitudinalPosition, final Length length,
            final DetectorType detectorType, final OtsSimulatorInterface simulator, final Time firstAggregation,
            final Duration aggregation, final LoopDetectorMeasurement<?, ?>... measurements) throws NetworkException
    {
        super(id, lane, longitudinalPosition, RelativePosition.FRONT, simulator, detectorType);
        Throw.when(aggregation.si <= 0.0, IllegalArgumentException.class, "Aggregation time should be positive.");
        this.length = length;
        this.currentAggregation = Duration.instantiateSI(firstAggregation.si);
        this.aggregation = aggregation;
        this.firstAggregation = firstAggregation;
        Try.execute(() -> simulator.scheduleEventAbsTime(firstAggregation, this, "aggregate", null), "");
        for (LoopDetectorMeasurement<?, ?> measurement : measurements)
        {
            this.currentCumulativeDataMap.put(measurement, measurement.identity());
            if (measurement.isPeriodic())
            {
                this.periodicDataMap.put(measurement, new ArrayList<>());
            }
        }

        // rear detector
        /** Abstract detector. */
        class RearDetector extends LaneDetector
        {
            /** */
            private static final long serialVersionUID = 20180315L;

            /**
             * Constructor.
             * @param idRear String; id
             * @param laneRear Lane; lane
             * @param longitudinalPositionRear Length; position
             * @param simulatorRear OtsSimulatorInterface; simulator
             * @param detectorType DetectorType; detector type.
             * @throws NetworkException on network exception
             */
            @SuppressWarnings("synthetic-access")
            RearDetector(final String idRear, final Lane laneRear, final Length longitudinalPositionRear,
                    final OtsSimulatorInterface simulatorRear, final DetectorType detectorType) throws NetworkException
            {
                super(idRear, laneRear, longitudinalPositionRear, RelativePosition.REAR, simulatorRear, detectorType);
            }

            /** {@inheritDoc} */
            @SuppressWarnings("synthetic-access")
            @Override
            protected void triggerResponse(final LaneBasedGtu gtu)
            {
                for (LoopDetectorMeasurement<?, ?> measurement : LoopDetector.this.currentCumulativeDataMap.keySet())
                {
                    accumulate(measurement, gtu, false);
                }
            }
        }
        Length position = longitudinalPosition.plus(length);
        Throw.when(position.gt(lane.getLength()), IllegalStateException.class,
                "A Detector can not be placed at a lane boundary");
        new RearDetector(id + "_rear", lane, position, simulator, detectorType);
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
        this.gtuCountCurrentPeriod++;
        this.overallGtuCount++;
        for (LoopDetectorMeasurement<?, ?> measurement : this.currentCumulativeDataMap.keySet())
        {
            accumulate(measurement, gtu, true);
        }
        this.fireTimedEvent(LOOP_DETECTOR_TRIGGERED, new Object[] {gtu.getId()}, getSimulator().getSimulatorTime());
    }

    /**
     * Accumulates a measurement.
     * @param measurement DetectorMeasurement&lt;C, ?&gt;; measurement to accumulate
     * @param gtu LaneBasedGtu; gtu
     * @param front boolean; triggered by front entering (or rear leaving when false)
     * @param <C> accumulated type
     */
    @SuppressWarnings("unchecked")
    <C> void accumulate(final LoopDetectorMeasurement<C, ?> measurement, final LaneBasedGtu gtu, final boolean front)
    {
        if (front)
        {
            this.currentCumulativeDataMap.put(measurement,
                    measurement.accumulateEntry((C) this.currentCumulativeDataMap.get(measurement), gtu, this));
        }
        else
        {
            this.currentCumulativeDataMap.put(measurement,
                    measurement.accumulateExit((C) this.currentCumulativeDataMap.get(measurement), gtu, this));
        }
    }

    /**
     * Aggregation.
     */
    private void aggregate()
    {
        Frequency frequency = Frequency.instantiateSI(this.gtuCountCurrentPeriod / this.currentAggregation.si);
        this.flow.add(frequency);
        for (LoopDetectorMeasurement<?, ?> measurement : this.periodicDataMap.keySet())
        {
            aggregate(measurement, this.gtuCountCurrentPeriod, this.currentAggregation);
            this.currentCumulativeDataMap.put(measurement, measurement.identity());
        }
        this.gtuCountCurrentPeriod = 0;
        this.currentAggregation = this.aggregation; // after first possibly irregular period, all periods regular
        if (!getListenerReferences(LOOP_DETECTOR_AGGREGATE).isEmpty())
        {
            Object[] data = new Object[this.periodicDataMap.size() + 1];
            data[0] = frequency;
            int i = 1;
            for (LoopDetectorMeasurement<?, ?> measurement : this.periodicDataMap.keySet())
            {
                List<?> list = this.periodicDataMap.get(measurement);
                data[i] = list.get(list.size() - 1);
                i++;
            }
            this.fireTimedEvent(LOOP_DETECTOR_AGGREGATE, data, getSimulator().getSimulatorTime());
        }
        Time time = Time.instantiateSI(this.firstAggregation.si + this.aggregation.si * this.period++);
        Try.execute(() -> getSimulator().scheduleEventAbsTime(time, this, "aggregate", null), "");
    }

    /**
     * Returns whether the detector has aggregated data available.
     * @return boolean; whether the detector has aggregated data available
     */
    public boolean hasLastValue()
    {
        return !this.flow.isEmpty();
    }

    /**
     * Returns the last flow.
     * @return last flow
     */
    public Frequency getLastFlow()
    {
        return this.flow.get(this.flow.size() - 1);
    }

    /**
     * Returns the last value of the detector measurement.
     * @param detectorMeasurement DetectorMeasurement&lt;?,A&gt;; detector measurement
     * @return last value of the detector measurement
     * @param <A> aggregate value type of the detector measurement
     */
    public <A> A getLastValue(final LoopDetectorMeasurement<?, A> detectorMeasurement)
    {
        @SuppressWarnings("unchecked")
        List<A> list = (List<A>) this.periodicDataMap.get(detectorMeasurement);
        return list.get(list.size() - 1);
    }

    /**
     * Aggregates a periodic measurement.
     * @param measurement DetectorMeasurement&lt;C, A&gt;; measurement to aggregate
     * @param gtuCount int; number of GTUs
     * @param agg Duration; aggregation period
     * @param <C> accumulated type
     * @param <A> aggregated type
     */
    @SuppressWarnings("unchecked")
    private <C, A> void aggregate(final LoopDetectorMeasurement<C, A> measurement, final int gtuCount, final Duration agg)
    {
        ((List<A>) this.periodicDataMap.get(measurement)).add(getAggregateValue(measurement, gtuCount, agg));
    }

    /**
     * Returns the aggregated value of the measurement.
     * @param measurement DetectorMeasurement&lt;C, A&gt;; measurement to aggregate
     * @param gtuCount int; number of GTUs
     * @param agg Duration; aggregation period
     * @return A; aggregated value of the measurement
     * @param <C> accumulated type
     * @param <A> aggregated type
     */
    @SuppressWarnings("unchecked")
    private <C, A> A getAggregateValue(final LoopDetectorMeasurement<C, A> measurement, final int gtuCount, final Duration agg)
    {
        return measurement.aggregate((C) this.currentCumulativeDataMap.get(measurement), gtuCount, agg);
    }

    /**
     * Returns a map of non-periodic measurements, mapping measurement type and the data.
     * @return Map&lt;DetectorMeasurement, Object&gt;; map of non-periodic measurements
     */
    private Map<LoopDetectorMeasurement<?, ?>, Object> getNonPeriodicMeasurements()
    {
        Map<LoopDetectorMeasurement<?, ?>, Object> map = new LinkedHashMap<>();
        for (LoopDetectorMeasurement<?, ?> measurement : this.currentCumulativeDataMap.keySet())
        {
            if (!measurement.isPeriodic())
            {
                map.put(measurement, getAggregateValue(measurement, this.overallGtuCount,
                        this.getSimulator().getSimulatorAbsTime().minus(Time.ZERO)));
            }
        }
        return map;
    }

    /**
     * Returns a Table with loop detector positions.
     * @param network RoadNetwork; network from which all detectors are found.
     * @return Table; with loop detector positions.
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
             * @return Table; this table instance for inner classes.
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
     * @param network RoadNetwork; network from which all detectors are found.
     * @return Table; with all periodic data, such as flow and speed per minute.
     */
    public static Table asTablePeriodicData(final RoadNetwork network)
    {
        Set<LoopDetector> detectors = getLoopDetectors(network);
        Set<LoopDetectorMeasurement<?, ?>> measurements = getMeasurements(detectors, true);
        Collection<Column<?>> columns = new LinkedHashSet<>();
        columns.add(new Column<>("id", "detector id", String.class, null));
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
                return new Iterator<>()
                {
                    /** Index iterator. */
                    private Iterator<Integer> indexIterator = Collections.emptyIterator();

                    /** Current loop detector. */
                    private LoopDetector loopDetector;

                    /** Map of measurement data per measurement, updated for each detector. */
                    private Map<LoopDetectorMeasurement<?, ?>, List<?>> map;

                    /** {@inheritDoc} */
                    @Override
                    public boolean hasNext()
                    {
                        while (!this.indexIterator.hasNext())
                        {
                            if (!iterator.hasNext())
                            {
                                return false;
                            }
                            this.loopDetector = iterator.next();
                            this.loopDetector.aggregate();
                            this.indexIterator = IntStream.range(0, this.loopDetector.flow.size()).iterator();
                            this.map = this.loopDetector.periodicDataMap;
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

                        double t = this.loopDetector.firstAggregation.si + (index - 1) * this.loopDetector.aggregation.si;
                        data[0] = this.loopDetector.getId();
                        data[1] = Duration.instantiateSI(t < 0.0 ? 0.0 : t);
                        data[2] = this.loopDetector.flow.get(index);
                        int dataIndex = 3;
                        for (LoopDetectorMeasurement<?, ?> measurement : measurements)
                        {
                            if (this.map.containsKey(measurement))
                            {
                                data[dataIndex++] = this.map.get(measurement).get(index);
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
             * @return Table; this table instance for inner classes.
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
     * @param network RoadNetwork; network from which all detectors are found.
     * @return Table; with all non-periodic data, such as vehicle passage times or platoon counts.
     */
    public static Table asTableNonPeriodicData(final RoadNetwork network)
    {
        Set<LoopDetector> detectors = getLoopDetectors(network);
        Set<LoopDetectorMeasurement<?, ?>> measurements = getMeasurements(detectors, false);
        Collection<Column<?>> columns = new LinkedHashSet<>();
        columns.add(new Column<>("id", "detector id", String.class, null));
        columns.add(new Column<>("measurement", "measurement type", String.class, null));
        columns.add(new Column<>("data", "data in any form", String.class, null));

        return new Table("non-periodic", "non-periodic measurements", columns)
        {
            /** {@inheritDoc} */
            @Override
            public Iterator<Row> iterator()
            {
                Iterator<LoopDetector> iterator = detectors.iterator();
                return new Iterator<>()
                {
                    /** Index iterator. */
                    private Iterator<LoopDetectorMeasurement<?, ?>> measurementIterator = Collections.emptyIterator();

                    /** Current loop detector. */
                    private LoopDetector loopDetector;

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
                            if (!iterator.hasNext())
                            {
                                return false;
                            }
                            this.loopDetector = iterator.next();
                            this.measurementIterator = measurements.iterator();
                            this.map = this.loopDetector.getNonPeriodicMeasurements();
                        }
                        // skip if data is not available for this detector
                        this.measurement = this.measurementIterator.next();
                        if (!this.map.containsKey(this.measurement))
                        {
                            this.measurement = null;
                            return hasNext();
                        }
                        return true;
                    }

                    /** {@inheritDoc} */
                    @Override
                    public Row next()
                    {
                        Throw.when(!hasNext(), NoSuchElementException.class, "Non-periodic data unavailable.");
                        Object[] data = new Object[columns.size()];

                        data[0] = this.loopDetector.getId();
                        data[1] = this.measurement.getName();
                        data[2] = this.map.get(this.measurement).toString();
                        this.measurement = null;
                        return new Row(table(), data);
                    }
                };
            }

            /**
             * Returns this table instance for inner classes as {@code Table.this} is not possible in an anonymous Table class.
             * @return Table; this table instance for inner classes.
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
     * @param network RoadNetwork; network.
     * @return Set&lt;LoopDetector&gt;; set of loop detector sorted by loop detector id.
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
     * Returns all measurement type that are found accross a set of loop detectors.
     * @param detectors Set&lt;LoopDetector&gt;; set of loop detectors.
     * @param periodic boolean; gather the periodic measurements {@code true}, or the non-periodic measurements {@code false}.
     * @return Set&lt;LoopDetectorMeasurement&lt;?, ?&gt;&gt;; set of periodic or non-periodic measurements from the detectors.
     */
    private static Set<LoopDetectorMeasurement<?, ?>> getMeasurements(final Set<LoopDetector> detectors, final boolean periodic)
    {
        return detectors.stream().flatMap((det) -> det.currentCumulativeDataMap.keySet().stream())
                .filter((measurement) -> measurement.isPeriodic() == periodic)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    /**
     * Interface for what detectors measure.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     * @param <C> accumulated type
     * @param <A> aggregated type
     */
    public interface LoopDetectorMeasurement<C, A>
    {
        /**
         * Returns the initial value before accumulation.
         * @return C; initial value before accumulation
         */
        C identity();

        /**
         * Returns an accumulated value for when the front reaches the detector. GTU's may trigger an exit without having
         * triggered an entry due to a lane change. Reversely, GTU's may not trigger an exit while they did trigger an entry.
         * @param cumulative C; accumulated value
         * @param gtu LaneBasedGtu; gtu
         * @param loopDetector Detector; loop detector
         * @return C; accumulated value
         */
        C accumulateEntry(C cumulative, LaneBasedGtu gtu, LoopDetector loopDetector);

        /**
         * Returns an accumulated value for when the rear leaves the detector. GTU's may trigger an exit without having
         * triggered an entry due to a lane change. Reversely, GTU's may not trigger an exit while they did trigger an entry.
         * @param cumulative C; accumulated value
         * @param gtu LaneBasedGtu; gtu
         * @param loopDetector Detector; loop detector
         * @return C; accumulated value
         */
        C accumulateExit(C cumulative, LaneBasedGtu gtu, LoopDetector loopDetector);

        /**
         * Returns whether the measurement aggregates every aggregation period (or only over the entire simulation).
         * @return boolean; whether the measurement aggregates every aggregation period (or only over the entire simulation)
         */
        boolean isPeriodic();

        /**
         * Returns an aggregated value.
         * @param cumulative C; accumulated value
         * @param gtuCount int; GTU gtuCount
         * @param aggregation Duration; aggregation period
         * @return A; aggregated value
         */
        A aggregate(C cumulative, int gtuCount, Duration aggregation);

        /**
         * Returns the value name.
         * @return String; value name
         */
        String getName();

        /**
         * Measurement description.
         * @return String; measurement description.
         */
        String getDescription();

        /**
         * Returns the unit string, default is {@code null}.
         * @return String; unit string.
         */
        default String getUnit()
        {
            return null;
        }

        /**
         * Returns the data type.
         * @return Class&lt;?&gt;; data type.
         */
        Class<?> getValueType();
    }

    /**
     * Measurement of platoon sizes based on time between previous GTU exit and GTU entry.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public static class PlatoonSizes implements LoopDetectorMeasurement<PlatoonMeasurement, List<Integer>>
    {

        /** Maximum time between two vehicles that are considered to be in the same platoon. */
        private final Duration threshold;

        /**
         * Constructor.
         * @param threshold Duration; maximum time between two vehicles that are considered to be in the same platoon
         */
        public PlatoonSizes(final Duration threshold)
        {
            this.threshold = threshold;
        }

        /** {@inheritDoc} */
        @Override
        public PlatoonMeasurement identity()
        {
            return new PlatoonMeasurement();
        }

        /** {@inheritDoc} */
        @SuppressWarnings("synthetic-access")
        @Override
        public PlatoonMeasurement accumulateEntry(final PlatoonMeasurement cumulative, final LaneBasedGtu gtu,
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
        @SuppressWarnings("synthetic-access")
        @Override
        public PlatoonMeasurement accumulateExit(final PlatoonMeasurement cumulative, final LaneBasedGtu gtu,
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
        public boolean isPeriodic()
        {
            return false;
        }

        /** {@inheritDoc} */
        @SuppressWarnings("synthetic-access")
        @Override
        public List<Integer> aggregate(final PlatoonMeasurement cumulative, final int count, final Duration aggregation)
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
        public String getName()
        {
            return "platoon sizes";
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return getName();
        }

        /** {@inheritDoc} */
        @Override
        public String getDescription()
        {
            return "list of platoon sizes (threshold: " + this.threshold + ")";
        }

        /** {@inheritDoc} */
        @Override
        public Class<Integer> getValueType()
        {
            return Integer.class;
        }

    }

    /**
     * Cumulative information for platoon size measurement.
     * <p>
     * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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
