package org.opentrafficsim.road.network.sampling;

import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

import org.djunits.unit.DurationUnit;
import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.TimedEvent;
import org.djutils.event.reference.ReferenceType;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.kpi.sampling.Sampler;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataType;
import org.opentrafficsim.kpi.sampling.filter.FilterDataType;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.RoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;

/**
 * Implementation of kpi sampler for OTS.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class RoadSampler extends Sampler<GtuDataRoad, LaneDataRoad> implements EventListener
{

    /** */
    private static final long serialVersionUID = 20200228L;

    /** Simulator. */
    private final OtsSimulatorInterface simulator;

    /** Network. */
    private final RoadNetwork network;

    /** Sampling interval. */
    private final Duration samplingInterval;

    /** Registration of sampling events of each GTU per lane, if interval based. */
    private final Map<String, Map<Lane, SimEventInterface<Duration>>> eventsPerGtu = new LinkedHashMap<>();

    /** Set of lanes the sampler knows each GTU to be at. Usually 1, could be 2 during a trajectory transition. */
    private final Map<String, Set<Lane>> activeLanesPerGtu = new LinkedHashMap<>();

    /** Unique id. */
    private final UUID uuid = UUID.randomUUID();

    /**
     * Constructor which uses the operational plan updates of GTU's as sampling interval.
     * @param network the network
     * @throws NullPointerException if the simulator is {@code null}
     */
    public RoadSampler(final RoadNetwork network)
    {
        this(new LinkedHashSet<>(), new LinkedHashSet<>(), network);
    }

    /**
     * Constructor which uses the operational plan updates of GTU's as sampling interval.
     * @param extendedDataTypes extended data types
     * @param filterDataTypes filter data types
     * @param network the network
     * @throws NullPointerException if the simulator is {@code null}
     */
    public RoadSampler(final Set<ExtendedDataType<?, ?, ?, ? super GtuDataRoad>> extendedDataTypes,
            final Set<FilterDataType<?, ? super GtuDataRoad>> filterDataTypes, final RoadNetwork network)
    {
        super(extendedDataTypes, filterDataTypes);
        Throw.whenNull(network, "Network may not be null.");
        this.network = network;
        this.simulator = network.getSimulator();
        this.samplingInterval = null;
    }

    /**
     * Constructor which uses the given frequency to determine the sampling interval.
     * @param network the network
     * @param frequency sampling frequency
     * @throws NullPointerException if an input is {@code null}
     * @throws IllegalArgumentException if frequency is negative or zero
     */
    public RoadSampler(final RoadNetwork network, final Frequency frequency)
    {
        this(new LinkedHashSet<>(), new LinkedHashSet<>(), network, frequency);
    }

    /**
     * Constructor which uses the given frequency to determine the sampling interval.
     * @param extendedDataTypes extended data types
     * @param filterDataTypes filter data types
     * @param network the network
     * @param frequency sampling frequency
     * @throws NullPointerException if an input is {@code null}
     * @throws IllegalArgumentException if frequency is negative or zero
     */
    public RoadSampler(final Set<ExtendedDataType<?, ?, ?, ? super GtuDataRoad>> extendedDataTypes,
            final Set<FilterDataType<?, ? super GtuDataRoad>> filterDataTypes, final RoadNetwork network,
            final Frequency frequency)
    {
        super(extendedDataTypes, filterDataTypes);
        Throw.whenNull(network, "Network may not be null.");
        Throw.whenNull(frequency, "Frequency may not be null.");
        Throw.when(frequency.le(Frequency.ZERO), IllegalArgumentException.class,
                "Negative or zero sampling frequency is not permitted.");
        this.network = network;
        this.simulator = network.getSimulator();
        this.samplingInterval = new Duration(1.0 / frequency.si, DurationUnit.SI);
    }

    @Override
    public final Time now()
    {
        return this.simulator.getSimulatorAbsTime();
    }

    @Override
    public final void scheduleStartRecording(final Time time, final LaneDataRoad lane)
    {
        try
        {
            this.simulator.scheduleEventAbs(Duration.instantiateSI(time.si), () -> startRecording(lane));
        }
        catch (SimRuntimeException exception)
        {
            throw new RuntimeException("Cannot start recording.", exception);
        }
    }

    @Override
    public final void scheduleStopRecording(final Time time, final LaneDataRoad lane)
    {
        try
        {
            this.simulator.scheduleEventAbs(Duration.instantiateSI(time.si), () -> stopRecording(lane));
        }
        catch (SimRuntimeException exception)
        {
            throw new RuntimeException("Cannot stop recording.", exception);
        }
    }

    @Override
    public final void initRecording(final LaneDataRoad lane)
    {
        Lane roadLane = lane.getLane();
        // @docs/02-model-structure/djutils.md#event-producers-and-listeners
        roadLane.addListener(this, Lane.GTU_ADD_EVENT, ReferenceType.WEAK);
        roadLane.addListener(this, Lane.GTU_REMOVE_EVENT, ReferenceType.WEAK);
        // @end
        int count = 1;
        for (LaneBasedGtu gtu : roadLane.getGtuList())
        {
            try
            {
                // Payload: Object[] {String gtuId, Lane source}
                notify(new TimedEvent<>(Lane.GTU_ADD_EVENT,
                        new Object[] {gtu.getId(), count, roadLane.getId(), roadLane.getLink().getId()},
                        gtu.getSimulator().getSimulatorTime()));
                count++;
            }
            catch (Exception exception)
            {
                throw new RuntimeException("Position cannot be obtained for GTU that is registered on a lane", exception);
            }
        }
    }

    @Override
    public final void finalizeRecording(final LaneDataRoad lane)
    {
        Lane roadLane = lane.getLane();
        roadLane.removeListener(this, Lane.GTU_ADD_EVENT);
        roadLane.removeListener(this, Lane.GTU_REMOVE_EVENT);
    }

    @Override
    // @docs/02-model-structure/djutils.md#event-producers-and-listeners (if-structure + add/removeListener(...))
    public final void notify(final Event event) throws RemoteException
    {
        if (event.getType().equals(LaneBasedGtu.LANEBASED_MOVE_EVENT))
        {
            // Payload: [String gtuId, PositionVector currentPosition, Direction currentDirection, Speed speed, Acceleration
            // acceleration, TurnIndicatorStatus turnIndicatorStatus, Length odometer, Link id of referenceLane, Lane id of
            // referenceLane, Length positionOnReferenceLane]
            Object[] payload = (Object[]) event.getContent();
            CrossSectionLink link = (CrossSectionLink) this.network.getLink(payload[7].toString());
            Lane lane = (Lane) link.getCrossSectionElement(payload[8].toString());
            LaneBasedGtu gtu = (LaneBasedGtu) this.network.getGTU(payload[0].toString());
            LaneDataRoad laneData = new LaneDataRoad(lane);
            snapshot(laneData, (Length) payload[9], (Speed) payload[3], (Acceleration) payload[4], now(), new GtuDataRoad(gtu));
        }
        else if (event.getType().equals(Lane.GTU_ADD_EVENT))
        {
            // Payload: Object[] {String gtuId, int count_after_addition, String laneId, String linkId}
            Object[] payload = (Object[]) event.getContent();
            Lane lane = (Lane) ((CrossSectionLink) this.network.getLink((String) payload[3]))
                    .getCrossSectionElement((String) payload[2]);
            LaneDataRoad laneData = new LaneDataRoad(lane);
            if (!getSamplerData().contains(laneData))
            {
                return; // we are not sampling this Lane
            }
            LaneBasedGtu gtu = (LaneBasedGtu) this.network.getGTU((String) payload[0]);

            Length position = gtu.getPosition(lane, RelativePosition.REFERENCE_POSITION);
            GtuDataRoad gtuData = new GtuDataRoad(gtu);
            addGtuWithSnapshot(laneData, position, gtu.getSpeed(), gtu.getAcceleration(), now(), gtuData);

            if (isIntervalBased())
            {
                double currentTime = now().getSI();
                int steps = (int) Math.ceil(currentTime / this.samplingInterval.getSI());
                // add 1 step if right now happens to be synchronous with sampling interval
                scheduleSamplingInterval(gtu, lane, steps + (steps * this.samplingInterval.getSI() > currentTime ? 0 : 1));
            }
            else
            {
                Set<Lane> lanes = this.activeLanesPerGtu.computeIfAbsent(gtu.getId(), (key) -> new LinkedHashSet<>());
                if (lanes.isEmpty())
                {
                    gtu.addListener(this, LaneBasedGtu.LANEBASED_MOVE_EVENT, ReferenceType.WEAK);
                }
                lanes.add(lane);
            }
        }
        else if (event.getType().equals(Lane.GTU_REMOVE_EVENT))
        {
            // Payload: Object[] {String gtuId, LaneBasedGtu gtu, int count_after_removal, Length position, String laneId,
            // String linkId}
            Object[] payload = (Object[]) event.getContent();
            Lane lane = (Lane) ((CrossSectionLink) this.network.getLink((String) payload[5]))
                    .getCrossSectionElement((String) payload[4]);
            LaneDataRoad laneData = new LaneDataRoad(lane);
            LaneBasedGtu gtu = (LaneBasedGtu) payload[1];
            Length position = (Length) payload[3];
            Speed speed = gtu.getSpeed();
            Acceleration acceleration = gtu.getAcceleration();

            removeGtuWithSnapshot(laneData, position, speed, acceleration, now(), new GtuDataRoad(gtu));

            if (isIntervalBased())
            {
                Map<Lane, SimEventInterface<Duration>> events = this.eventsPerGtu.get(gtu.getId());
                SimEventInterface<Duration> e = events.remove(lane);
                if (e != null)
                {
                    this.simulator.cancelEvent(e);
                }
                if (events.isEmpty())
                {
                    this.eventsPerGtu.remove(gtu.getId());
                }
            }
            else
            {
                Set<Lane> lanes = this.activeLanesPerGtu.get(gtu.getId());
                lanes.remove(lane);
                if (lanes.isEmpty())
                {
                    this.activeLanesPerGtu.remove(gtu.getId());
                    gtu.removeListener(this, LaneBasedGtu.LANEBASED_MOVE_EVENT);
                }
            }
        }
    }

    /**
     * @return whether sampling is interval based
     */
    private boolean isIntervalBased()
    {
        return this.samplingInterval != null;
    }

    /**
     * Schedules a sampling event for the given gtu on the given lane for the sampling interval from the current time.
     * @param gtu gtu to sample
     * @param lane lane where the gtu is at
     * @param steps number of steps in interval based sampling
     */
    private void scheduleSamplingInterval(final LaneBasedGtu gtu, final Lane lane, final int steps)
    {
        SimEventInterface<Duration> simEvent;
        try
        {
            simEvent =
                    this.simulator.scheduleEventAbs(this.samplingInterval.times(steps), () -> notifySample(gtu, lane, steps));
        }
        catch (SimRuntimeException exception)
        {
            // should not happen with getSimulatorTime.add()
            throw new RuntimeException("Scheduling sampling in the past.", exception);
        }
        this.eventsPerGtu.computeIfAbsent(gtu.getId(), (key) -> new LinkedHashMap<>()).put(lane, simEvent);
    }

    /**
     * Samples a gtu and schedules the next sampling event. This is used for interval-based sampling.
     * @param gtu gtu to sample
     * @param lane lane where the gtu is at
     * @param steps number of steps in interval based sampling
     */
    private void notifySample(final LaneBasedGtu gtu, final Lane lane, final int steps)
    {
        LaneDataRoad laneData = new LaneDataRoad(lane);
        Length position = gtu.getPosition(lane, RelativePosition.REFERENCE_POSITION);
        snapshot(laneData, position, gtu.getSpeed(), gtu.getAcceleration(), now(), new GtuDataRoad(gtu));
        scheduleSamplingInterval(gtu, lane, steps + 1);
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + Objects.hash(this.uuid);
        return result;
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj))
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        RoadSampler other = (RoadSampler) obj;
        return Objects.equals(this.uuid, other.uuid);
    }

    @Override
    public final String toString()
    {
        return "RoadSampler [samplingInterval=" + this.samplingInterval + "]";
    }

    /**
     * Returns a factory to create a sampler.
     * @param network network
     * @return factory to create a sampler
     */
    public static Factory build(final RoadNetwork network)
    {
        return new Factory(network);
    }

    /** Factory for {@code RoadSampler}. */
    public static final class Factory
    {

        /** Simulator. */
        private final RoadNetwork network;

        /** Registration of included extended data types. */
        private final Set<ExtendedDataType<?, ?, ?, ? super GtuDataRoad>> extendedDataTypes = new LinkedHashSet<>();

        /** Set of registered filter data types. */
        private final Set<FilterDataType<?, ? super GtuDataRoad>> filterDataTypes = new LinkedHashSet<>();

        /** Frequency. */
        private Frequency freq;

        /**
         * Constructor.
         * @param network network
         */
        Factory(final RoadNetwork network)
        {
            this.network = network;
        }

        /**
         * Register extended data type.
         * @param extendedDataType extended data type
         * @return this factory
         */
        public Factory registerExtendedDataType(final ExtendedDataType<?, ?, ?, ? super GtuDataRoad> extendedDataType)
        {
            Throw.whenNull(extendedDataType, "Extended data type may not be null.");
            this.extendedDataTypes.add(extendedDataType);
            return this;
        }

        /**
         * Register filter data type.
         * @param filterDataType filter data type
         * @return this factory
         */
        public Factory registerFilterDataType(final FilterDataType<?, ? super GtuDataRoad> filterDataType)
        {
            Throw.whenNull(filterDataType, "Filter data type may not be null.");
            this.filterDataTypes.add(filterDataType);
            return this;
        }

        /**
         * Sets the frequency. If no frequency is set, a sampler is created that records on move events of GTU's.
         * @param frequency frequency
         * @return this factory
         */
        public Factory setFrequency(final Frequency frequency)
        {
            this.freq = frequency;
            return this;
        }

        /**
         * Create sampler.
         * @return sampler
         */
        public RoadSampler create()
        {
            return this.freq == null ? new RoadSampler(this.extendedDataTypes, this.filterDataTypes, this.network)
                    : new RoadSampler(this.extendedDataTypes, this.filterDataTypes, this.network, this.freq);
        }

    }

}
