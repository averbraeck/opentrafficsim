package org.opentrafficsim.road.network.sampling;

import java.rmi.RemoteException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

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
import org.djutils.exceptions.Try;
import org.opentrafficsim.core.dsol.OtsSimulatorInterface;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.kpi.sampling.Sampler;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataType;
import org.opentrafficsim.kpi.sampling.meta.FilterDataType;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.network.OtsRoadNetwork;
import org.opentrafficsim.road.network.lane.CrossSectionLink;
import org.opentrafficsim.road.network.lane.Lane;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;

/**
 * Implementation of kpi sampler for OTS.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class RoadSampler extends Sampler<GtuDataRoad, LaneDataRoad> implements EventListener
{

    /** */
    private static final long serialVersionUID = 20200228L;

    /** Simulator. */
    private final OtsSimulatorInterface simulator;

    /** Network. */
    private final OtsRoadNetwork network;

    /** Sampling interval. */
    private final Duration samplingInterval;

    /** Registration of sampling events of each GTU per lane, if interval based. */
    private final Map<String, Map<Lane, SimEventInterface<Duration>>> eventsPerGtu = new LinkedHashMap<>();

    /** Set of lanes the sampler knows each GTU to be at. Usually 1, could be 2 during a trajectory transition. */
    private final Map<String, Set<Lane>> activeLanesPerGtu = new LinkedHashMap<>();

    /** Set of actively sampled GTUs. */
    private final Set<String> activeGtus = new LinkedHashSet<>();

    /**
     * Constructor which uses the operational plan updates of GTU's as sampling interval.
     * @param network OTSRoadNetwork; the network
     * @throws NullPointerException if the simulator is {@code null}
     */
    public RoadSampler(final OtsRoadNetwork network)
    {
        this(new LinkedHashSet<>(), new LinkedHashSet<>(), network);
    }

    /**
     * Constructor which uses the operational plan updates of GTU's as sampling interval.
     * @param extendedDataTypes Set&lt;ExtendedDataType&lt;?, ?, ?, GtuData&gt;&gt;; extended data types
     * @param filterDataTypes Set&lt;FilterDataType&lt;?&gt;&gt;; filter data types
     * @param network OTSRoadNetwork; the network
     * @throws NullPointerException if the simulator is {@code null}
     */
    public RoadSampler(final Set<ExtendedDataType<?, ?, ?, GtuDataRoad>> extendedDataTypes,
            final Set<FilterDataType<?>> filterDataTypes, final OtsRoadNetwork network)
    {
        super(extendedDataTypes, filterDataTypes);
        Throw.whenNull(network, "Network may not be null.");
        this.network = network;
        this.simulator = network.getSimulator();
        this.samplingInterval = null;
    }

    /**
     * Constructor which uses the given frequency to determine the sampling interval.
     * @param network OTSRoadNetwork; the network
     * @param frequency Frequency; sampling frequency
     * @throws NullPointerException if an input is {@code null}
     * @throws IllegalArgumentException if frequency is negative or zero
     */
    public RoadSampler(final OtsRoadNetwork network, final Frequency frequency)
    {
        this(new LinkedHashSet<>(), new LinkedHashSet<>(), network, frequency);
    }

    /**
     * Constructor which uses the given frequency to determine the sampling interval.
     * @param extendedDataTypes Set&lt;ExtendedDataType&lt;?, ?, ?, GtuData&gt;&gt;; extended data types
     * @param filterDataTypes Set&lt;FilterDataType&lt;?&gt;&gt;; filter data types
     * @param network OTSRoadNetwork; the network
     * @param frequency Frequency; sampling frequency
     * @throws NullPointerException if an input is {@code null}
     * @throws IllegalArgumentException if frequency is negative or zero
     */
    public RoadSampler(final Set<ExtendedDataType<?, ?, ?, GtuDataRoad>> extendedDataTypes,
            final Set<FilterDataType<?>> filterDataTypes, final OtsRoadNetwork network, final Frequency frequency)
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

    /** {@inheritDoc} */
    @Override
    public final Time now()
    {
        return this.simulator.getSimulatorAbsTime();
    }

    /** {@inheritDoc} */
    @Override
    public final void scheduleStartRecording(final Time time, final LaneDataRoad lane)
    {
        try
        {
            this.simulator.scheduleEventAbsTime(time, this, "startRecording", new Object[] {lane});
        }
        catch (SimRuntimeException exception)
        {
            throw new RuntimeException("Cannot start recording.", exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void scheduleStopRecording(final Time time, final LaneDataRoad lane)
    {
        try
        {
            this.simulator.scheduleEventAbsTime(time, this, "stopRecording", new Object[] {lane});
        }
        catch (SimRuntimeException exception)
        {
            throw new RuntimeException("Cannot stop recording.", exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void initRecording(final LaneDataRoad lane)
    {
        Lane roadLane = lane.getLane();
        roadLane.addListener(this, Lane.GTU_ADD_EVENT, ReferenceType.WEAK);
        roadLane.addListener(this, Lane.GTU_REMOVE_EVENT, ReferenceType.WEAK);
        int count = 1;
        for (LaneBasedGtu gtu : roadLane.getGtuList())
        {
            try
            {
                // Payload: Object[] {String gtuId, Lane source}
                notify(new TimedEvent<>(Lane.GTU_ADD_EVENT,
                        new Object[] {gtu.getId(), count, roadLane.getId(), roadLane.getParentLink().getId()},
                        gtu.getSimulator().getSimulatorTime()));
                count++;
            }
            catch (Exception exception)
            {
                throw new RuntimeException("Position cannot be obtained for GTU that is registered on a lane", exception);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void finalizeRecording(final LaneDataRoad lane)
    {
        Lane roadLane = lane.getLane();
        roadLane.removeListener(this, Lane.GTU_ADD_EVENT);
        roadLane.removeListener(this, Lane.GTU_REMOVE_EVENT);
    }

    /** {@inheritDoc} */
    @Override
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

            if (!this.activeGtus.contains(gtu.getId()))
            {
                // GTU add was skipped during add event due to an improper phase of initialization, do here instead
                processGtuAddEvent(laneData, new GtuDataRoad(gtu));
                this.activeGtus.add(gtu.getId());
            }
            processGtuMoveEvent(laneData, (Length) payload[9], (Speed) payload[3], (Acceleration) payload[4], now(),
                    new GtuDataRoad(gtu));
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

            // Skip add when first encountering this GTU, it is in an improper phase of initialization.
            // If interval-based, a GTU is also not in the active list in the moment of an instantaneous lane-change.
            boolean active = this.activeGtus.contains(gtu.getId());
            if (active)
            {
                Length position = Try.assign(() -> gtu.position(lane, RelativePosition.REFERENCE_POSITION),
                        "Could not determine position.");
                Speed speed = gtu.getSpeed();
                Acceleration acceleration = gtu.getAcceleration();
                processGtuAddEventWithMove(laneData, position, speed, acceleration, now(), new GtuDataRoad(gtu));
            }

            if (isIntervalBased())
            {
                Duration nowOnFirstEncounterOtherwiseAtInterval = active ? this.samplingInterval : Duration.ZERO;
                scheduleSamplingInterval(gtu, lane, nowOnFirstEncounterOtherwiseAtInterval);
            }
            else
            {
                this.activeLanesPerGtu.computeIfAbsent(gtu.getId(), (key) -> new LinkedHashSet<>()).add(lane);
                gtu.addListener(this, LaneBasedGtu.LANEBASED_MOVE_EVENT, ReferenceType.WEAK);
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

            processGtuRemoveEventWithMove(laneData, position, speed, acceleration, now(), new GtuDataRoad(gtu));

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
                    this.activeGtus.remove(gtu.getId());
                }
            }
            else
            {
                this.activeLanesPerGtu.get(gtu.getId()).remove(lane);
                if (this.activeLanesPerGtu.get(gtu.getId()).isEmpty())
                {
                    this.activeLanesPerGtu.remove(gtu.getId());
                    gtu.removeListener(this, LaneBasedGtu.LANEBASED_MOVE_EVENT);
                    this.activeGtus.remove(gtu.getId());
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
     * @param gtu LaneBasedGtu; gtu to sample
     * @param lane Lane; lane where the gtu is at
     * @param inTime Duration; relative time to schedule
     */
    private void scheduleSamplingInterval(final LaneBasedGtu gtu, final Lane lane, final Duration inTime)
    {
        SimEventInterface<Duration> simEvent;
        try
        {
            simEvent = this.simulator.scheduleEventRel(inTime, this, this, "notifySample", new Object[] {gtu, lane});
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
     * @param gtu LaneBasedGtu; gtu to sample
     * @param lane Lane; lane direction where the gtu is at
     */
    public final void notifySample(final LaneBasedGtu gtu, final Lane lane)
    {
        LaneDataRoad laneData = new LaneDataRoad(lane);
        try
        {
            Length position = gtu.position(lane, RelativePosition.REFERENCE_POSITION);
            if (this.activeGtus.contains(gtu.getId()))
            {
                // already recording this GTU, just trigger a record through a move
                processGtuMoveEvent(laneData, position, gtu.getSpeed(), gtu.getAcceleration(), now(), new GtuDataRoad(gtu));
            }
            else
            {
                // first time encountering this GTU so add, which also triggers a record through a move
                processGtuAddEventWithMove(laneData, position, gtu.getSpeed(), gtu.getAcceleration(), now(),
                        new GtuDataRoad(gtu));
                this.activeGtus.add(gtu.getId());
            }
        }
        catch (GtuException exception)
        {
            throw new RuntimeException("Requesting position on lane, but the GTU is not on the lane.", exception);
        }
        scheduleSamplingInterval(gtu, lane, this.samplingInterval);
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.eventsPerGtu == null) ? 0 : this.eventsPerGtu.hashCode());
        result = prime * result + ((this.samplingInterval == null) ? 0 : this.samplingInterval.hashCode());
        result = prime * result + ((this.simulator == null) ? 0 : this.simulator.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public final boolean equals(final Object obj)
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
        if (this.eventsPerGtu == null)
        {
            if (other.eventsPerGtu != null)
            {
                return false;
            }
        }
        else if (!this.eventsPerGtu.equals(other.eventsPerGtu))
        {
            return false;
        }
        if (this.samplingInterval == null)
        {
            if (other.samplingInterval != null)
            {
                return false;
            }
        }
        else if (!this.samplingInterval.equals(other.samplingInterval))
        {
            return false;
        }
        if (this.simulator == null)
        {
            if (other.simulator != null)
            {
                return false;
            }
        }
        else if (!this.simulator.equals(other.simulator))
        {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "RoadSampler [samplingInterval=" + this.samplingInterval + "]";
        // do not use "this.eventPerGtu", it creates circular toString and hence stack overflow
    }

    /**
     * Returns a factory to create a sampler.
     * @param network OTSRoadNetwork; network
     * @return Factory; factory to create a sampler
     */
    public static Factory build(final OtsRoadNetwork network)
    {
        return new Factory(network);
    }

    /** Factory for {@code RoadSampler}. */
    public static final class Factory
    {

        /** Simulator. */
        private final OtsRoadNetwork network;

        /** Registration of included extended data types. */
        private final Set<ExtendedDataType<?, ?, ?, GtuDataRoad>> extendedDataTypes = new LinkedHashSet<>();

        /** Set of registered filter data types. */
        private final Set<FilterDataType<?>> filterDataTypes = new LinkedHashSet<>();

        /** Frequency. */
        private Frequency freq;

        /**
         * Constructor.
         * @param network OTSRoadNetwork; network
         */
        Factory(final OtsRoadNetwork network)
        {
            this.network = network;
        }

        /**
         * Register extended data type.
         * @param extendedDataType ExtendedDataType&lt;?, ?, ?, GtuData&gt;; extended data type
         * @return Factory; this factory
         */
        public Factory registerExtendedDataType(final ExtendedDataType<?, ?, ?, GtuDataRoad> extendedDataType)
        {
            Throw.whenNull(extendedDataType, "Extended data type may not be null.");
            this.extendedDataTypes.add(extendedDataType);
            return this;
        }

        /**
         * Register filter data type.
         * @param filterDataType FilterDataType&lt;?&gt;; filter data type
         * @return Factory; this factory
         */
        public Factory registerFilterDataType(final FilterDataType<?> filterDataType)
        {
            Throw.whenNull(filterDataType, "Filter data type may not be null.");
            this.filterDataTypes.add(filterDataType);
            return this;
        }

        /**
         * Sets the frequency. If no frequency is set, a sampler is created that records on move events of GTU's.
         * @param frequency Frequency; frequency
         * @return Factory; this factory
         */
        public Factory setFrequency(final Frequency frequency)
        {
            this.freq = frequency;
            return this;
        }

        /**
         * Create sampler.
         * @return RoadSampler; sampler
         */
        public RoadSampler create()
        {
            return this.freq == null ? new RoadSampler(this.extendedDataTypes, this.filterDataTypes, this.network)
                    : new RoadSampler(this.extendedDataTypes, this.filterDataTypes, this.network, this.freq);
        }

    }

}
