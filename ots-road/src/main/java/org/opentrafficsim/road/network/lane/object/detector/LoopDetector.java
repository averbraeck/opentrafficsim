package org.opentrafficsim.road.network.lane.object.detector;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import org.djunits.unit.FrequencyUnit;
import org.djunits.unit.SpeedUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.event.EventType;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.djutils.io.CompressedFileWriter;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuType;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.OtsRoadNetwork;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * Detector, measuring a dynamic set of measurements typical for single- or dual-loop detectors. A subsidiary detector is placed
 * at a position downstream based on the detector length, that triggers when the rear leaves the detector.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class LoopDetector extends Detector
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
            return Speed.instantiateSI(cumulative / gtuCount);
        }

        @Override
        public String getName()
        {
            return "v[km/h]";
        }

        @Override
        public String stringValue(final Speed aggregate, final String format)
        {
            return String.format(format, aggregate.getInUnit(SpeedUnit.KM_PER_HOUR));
        }

        @Override
        public String toString()
        {
            return getName();
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
                    return Speed.instantiateSI(gtuCount / cumulative);
                }

                @Override
                public String getName()
                {
                    return "vHarm[km/h]";
                }

                @Override
                public String stringValue(final Speed aggregate, final String format)
                {
                    return String.format(format, aggregate.getInUnit(SpeedUnit.KM_PER_HOUR));
                }

                @Override
                public String toString()
                {
                    return getName();
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
        public String stringValue(final Double aggregate, final String format)
        {
            return String.format(format, aggregate);
        }

        @Override
        public String toString()
        {
            return getName();
        }
    };

    /** Passages measurement. */
    public static final LoopDetectorMeasurement<List<Double>, List<Double>> PASSAGES =
            new LoopDetectorMeasurement<List<Double>, List<Double>>()
            {
                @Override
                public List<Double> identity()
                {
                    return new ArrayList<>();
                }

                @Override
                public List<Double> accumulateEntry(final List<Double> cumulative, final LaneBasedGtu gtu,
                        final LoopDetector loopDetector)
                {
                    cumulative.add(gtu.getSimulator().getSimulatorTime().si);
                    return cumulative;
                }

                @Override
                public List<Double> accumulateExit(final List<Double> cumulative, final LaneBasedGtu gtu,
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
                public List<Double> aggregate(final List<Double> cumulative, final int gtuCount, final Duration aggregation)
                {
                    return cumulative;
                }

                @Override
                public String getName()
                {
                    return "passage times";
                }

                @Override
                public String stringValue(final List<Double> aggregate, final String format)
                {
                    return printListDouble(aggregate, format);
                }

                @Override
                public String toString()
                {
                    return getName();
                }
            };

    /** Aggregation time. */
    private final Duration aggregation;

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
     * @param gtuType GtuType; GTU type.
     * @param simulator OTSSimulatorInterface; simulator
     * @param detectorType DetectorType; detector type.
     * @throws NetworkException on network exception
     */
    public LoopDetector(final String id, final Lane lane, final Length longitudinalPosition, final GtuType gtuType,
            final DetectorType detectorType, final OtsSimulatorInterface simulator) throws NetworkException
    {
        // Note: length not important for flow and mean speed
        this(id, lane, longitudinalPosition, Length.ZERO, gtuType, detectorType, simulator, Duration.instantiateSI(60.0),
                MEAN_SPEED);
    }

    /**
     * Constructor.
     * @param id String; detector id
     * @param lane Lane; lane
     * @param longitudinalPosition Length; position
     * @param length Length; length
     * @param gtuType GtuType; GTU type.
     * @param simulator OTSSimulatorInterface; simulator
     * @param aggregation Duration; aggregation period
     * @param measurements DetectorMeasurement&lt;?, ?&gt;...; measurements to obtain
     * @param detectorType DetectorType; detector type.
     * @throws NetworkException on network exception
     */
    public LoopDetector(final String id, final Lane lane, final Length longitudinalPosition, final Length length,
            final GtuType gtuType, final DetectorType detectorType, final OtsSimulatorInterface simulator,
            final Duration aggregation, final LoopDetectorMeasurement<?, ?>... measurements) throws NetworkException
    {
        super(id, lane, longitudinalPosition, RelativePosition.FRONT, simulator, detectorType);
        Throw.when(aggregation.si <= 0.0, IllegalArgumentException.class, "Aggregation time should be positive.");
        this.length = length;
        this.aggregation = aggregation;
        Try.execute(() -> simulator.scheduleEventAbsTime(Time.instantiateSI(aggregation.si), this, "aggregate", null), "");
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
        class RearDetector extends Detector
        {
            /** */
            private static final long serialVersionUID = 20180315L;

            /**
             * Constructor.
             * @param idRear String; id
             * @param laneRear Lane; lane
             * @param longitudinalPositionRear Length; position
             * @param simulatorRear OTSSimulatorInterface; simulator
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

    /**
     * Returns the detector length.
     * @return Length; the detector length
     */
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
        Frequency frequency = Frequency.instantiateSI(this.gtuCountCurrentPeriod / this.aggregation.si);
        this.flow.add(frequency);
        for (LoopDetectorMeasurement<?, ?> measurement : this.periodicDataMap.keySet())
        {
            aggregate(measurement, this.gtuCountCurrentPeriod, this.aggregation);
            this.currentCumulativeDataMap.put(measurement, measurement.identity());
        }
        this.gtuCountCurrentPeriod = 0;
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
        this.period++;
        double t = this.aggregation.si * this.period;
        Time time = Time.instantiateSI(t);
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
     * Returns a map of non-periodical measurements.
     * @return Map&lt;DetectorMeasurement, Object&gt;; map of non-periodical measurements
     */
    private Map<LoopDetectorMeasurement<?, ?>, Object> getMesoMeasurements()
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
     * Write the contents of all detectors in to a file.
     * @param network OTSRoadNetwork; network
     * @param file String; file
     * @param periodic boolean; periodic data
     */
    public static final void writeToFile(final OtsRoadNetwork network, final String file, final boolean periodic)
    {
        writeToFile(network, file, periodic, "%.3f", CompressionMethod.ZIP);
    }

    /**
     * Write the contents of all detectors in to a file.
     * @param network OTSRoadNetwork; network
     * @param file String; file
     * @param periodic boolean; periodic data
     * @param format String; number format, as used in {@code String.format()}
     * @param compression CompressionMethod; how to compress the data
     * @param <C> accumulated type
     */
    @SuppressWarnings("unchecked")
    public static final <C> void writeToFile(final OtsRoadNetwork network, final String file, final boolean periodic,
            final String format, final CompressionMethod compression)
    {
        try (BufferedWriter bw = CompressedFileWriter.create(file, compression.equals(CompressionMethod.ZIP)))
        {
            // gather all DetectorMeasurements and Detectors (sorted)
            Set<LoopDetectorMeasurement<?, ?>> measurements = new LinkedHashSet<>();
            Set<LoopDetector> detectors = new TreeSet<>(new Comparator<LoopDetector>()
            {
                @Override
                public int compare(final LoopDetector o1, final LoopDetector o2)
                {
                    return o1.getId().compareTo(o2.getId());
                }
            });
            for (LoopDetector detector : network.getObjectMap(LoopDetector.class).values())
            {
                detectors.add(detector);
            }
            for (LoopDetector detector : detectors)
            {
                for (LoopDetectorMeasurement<?, ?> measurement : detector.currentCumulativeDataMap.keySet())
                {
                    if (measurement.isPeriodic() == periodic)
                    {
                        measurements.add(measurement);
                    }
                }
            }
            // create headerline
            StringBuilder str = periodic ? new StringBuilder("id,t[s],q[veh/h]") : new StringBuilder("id,measurement,data");
            if (periodic)
            {
                for (LoopDetectorMeasurement<?, ?> measurement : measurements)
                {
                    str.append(",");
                    str.append(measurement.getName());
                }
            }
            bw.write(str.toString());
            bw.newLine();
            // create data lines
            for (LoopDetector detector : detectors)
            {
                String id = detector.getFullId();
                // meso
                if (!periodic)
                {
                    Map<LoopDetectorMeasurement<?, ?>, Object> map = detector.getMesoMeasurements();
                    for (LoopDetectorMeasurement<?, ?> measurement : measurements)
                    {
                        if (map.containsKey(measurement))
                        {
                            // TODO: values can contain ","; use csv writer
                            bw.write(id + "," + measurement.getName() + "," + ((LoopDetectorMeasurement<?, C>) measurement)
                                    .stringValue((C) map.get(measurement), format));
                            bw.newLine();
                        }
                    }
                }
                else
                {
                    // periodic
                    detector.aggregate();
                    double t = 0.0;
                    for (int i = 0; i < detector.flow.size(); i++)
                    {
                        str = new StringBuilder(id + "," + removeTrailingZeros(String.format(format, t)) + ",");
                        str.append(removeTrailingZeros(
                                String.format(format, detector.flow.get(i).getInUnit(FrequencyUnit.PER_HOUR))));
                        for (LoopDetectorMeasurement<?, ?> measurement : measurements)
                        {
                            str.append(",");
                            List<?> list = detector.periodicDataMap.get(measurement);
                            if (list != null)
                            {
                                str.append(removeTrailingZeros(
                                        ((LoopDetectorMeasurement<?, C>) measurement).stringValue((C) list.get(i), format)));
                            }
                        }
                        bw.write(str.toString());
                        bw.newLine();
                        t += detector.aggregation.si;
                    }
                }
            }
        }
        catch (IOException exception)
        {
            throw new RuntimeException("Could not write to file.", exception);
        }
    }

    /**
     * Remove any trailing zeros.
     * @param string String; string of number
     * @return String; string without trailing zeros
     */
    public static final String removeTrailingZeros(final String string)
    {
        return string.replaceFirst("\\.0*$|(\\.\\d*?)0+$", "$1");
    }

    /**
     * Prints a list of doubles in to a formatted string.
     * @param list List&lt;Double&gt;; double values
     * @param format String; format string
     * @return formatted string of doubles
     */
    public static final String printListDouble(final List<Double> list, final String format)
    {
        StringBuilder str = new StringBuilder("[");
        String sep = "";
        for (double t : list)
        {
            str.append(sep);
            str.append(removeTrailingZeros(String.format(format, t)));
            sep = ", ";
        }
        str.append("]");
        return str.toString();
    }

    /**
     * Defines the compression method for stored data.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
     */
    public enum CompressionMethod
    {
        /** No compression. */
        NONE,

        /** Zip compression. */
        ZIP,
    }

    /**
     * Interface for what detectors measure.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
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
         * Returns a string representation of the aggregate result.
         * @param aggregate A; aggregate result
         * @param format String; format string
         * @return String; string representation of the aggregate result
         */
        String stringValue(A aggregate, String format);
    }

    /**
     * Measurement of platoon sizes based on time between previous GTU exit and GTU entry.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
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
        public String stringValue(final List<Integer> aggregate, final String format)
        {
            return aggregate.toString();
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return getName();
        }

    }

    /**
     * Cumulative information for platoon size measurement.
     * <p>
     * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * <p>
     * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
     * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
     * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
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
