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
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.kpi.sampling.KpiGtuDirectionality;
import org.opentrafficsim.kpi.sampling.KpiLaneDirection;
import org.opentrafficsim.kpi.sampling.Sampler;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneDirection;

import nl.tudelft.simulation.dsol.SimRuntimeException;
import nl.tudelft.simulation.dsol.formalisms.eventscheduling.SimEventInterface;
import nl.tudelft.simulation.dsol.simtime.SimTimeDoubleUnit;
import nl.tudelft.simulation.dsol.simulators.DEVSSimulatorInterface;
import nl.tudelft.simulation.event.EventInterface;
import nl.tudelft.simulation.event.EventListenerInterface;
import nl.tudelft.simulation.event.TimedEvent;

/**
 * Implementation of kpi sampler for OTS.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version 12 okt. 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class RoadSampler extends Sampler<GtuData> implements EventListenerInterface
{

    /** Simulator. */
    private final DEVSSimulatorInterface.TimeDoubleUnit simulator;

    /** Sampling interval. */
    private final Duration samplingInterval;

    /** Registration of sampling events of each GTU per lane, if interval based. */
    private final Map<String, Map<LaneDirection, SimEventInterface<SimTimeDoubleUnit>>> eventPerGtu = new LinkedHashMap<>();

    /** List of lane the sampler is listening to for each GTU. Usually 1, could be 2 during a trajectory transition. */
    private final Map<String, Set<LaneDirection>> listenersPerGtu = new LinkedHashMap<>();

    /**
     * Constructor which uses the operational plan updates of GTU's as sampling interval.
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; simulator
     * @throws NullPointerException if the simulator is {@code null}
     */
    public RoadSampler(final DEVSSimulatorInterface.TimeDoubleUnit simulator)
    {
        Throw.whenNull(simulator, "Simulator may not be null.");
        this.simulator = simulator;
        this.samplingInterval = null;
    }

    /**
     * Constructor which uses the given frequency to determine the sampling interval.
     * @param simulator DEVSSimulatorInterface.TimeDoubleUnit; simulator
     * @param frequency Frequency; sampling frequency
     * @throws NullPointerException if an input is {@code null}
     * @throws IllegalArgumentException if frequency is negative or zero
     */
    public RoadSampler(final DEVSSimulatorInterface.TimeDoubleUnit simulator, final Frequency frequency)
    {
        Throw.whenNull(simulator, "Simulator may not be null.");
        Throw.whenNull(frequency, "Frequency may not be null.");
        Throw.when(frequency.le(Frequency.ZERO), IllegalArgumentException.class,
                "Negative or zero sampling frequency is not permitted.");
        this.simulator = simulator;
        this.samplingInterval = new Duration(1.0 / frequency.si, DurationUnit.SI);
    }

    /** {@inheritDoc} */
    @Override
    public final Time now()
    {
        return this.simulator.getSimulatorTime();
    }

    /** {@inheritDoc} */
    @Override
    public final void scheduleStartRecording(final Time time, final KpiLaneDirection kpiLaneDirection)
    {
        try
        {
            this.simulator.scheduleEventAbs(time, this, this, "startRecording", new Object[] {kpiLaneDirection});
        }
        catch (SimRuntimeException exception)
        {
            throw new RuntimeException("Cannot start recording.", exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void scheduleStopRecording(final Time time, final KpiLaneDirection kpiLaneDirection)
    {
        try
        {
            this.simulator.scheduleEventAbs(time, this, this, "stopRecording", new Object[] {kpiLaneDirection});
        }
        catch (SimRuntimeException exception)
        {
            throw new RuntimeException("Cannot stop recording.", exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void initRecording(final KpiLaneDirection kpiLaneDirection)
    {
        Lane lane = ((LaneData) kpiLaneDirection.getLaneData()).getLane();
        lane.addListener(this, Lane.GTU_ADD_EVENT, true);
        lane.addListener(this, Lane.GTU_REMOVE_EVENT, true);
        int count = 1;
        for (LaneBasedGTU gtu : lane.getGtuList())
        {
            try
            {
                if (sameDirection(kpiLaneDirection.getKpiDirection(), gtu.getDirection(lane)))
                {
                    // Payload: Object[] {String gtuId, LaneBasedGTU gtu, int count_after_addition}
                    notify(new TimedEvent<>(Lane.GTU_ADD_EVENT, lane, new Object[] {gtu.getId(), gtu, count},
                            gtu.getSimulator().getSimulatorTime()));
                }
                count++;
            }
            catch (RemoteException | GTUException exception)
            {
                throw new RuntimeException("Position cannot be obtained for GTU that is registered on a lane", exception);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void finalizeRecording(final KpiLaneDirection kpiLaneDirection)
    {
        Lane lane = ((LaneData) kpiLaneDirection.getLaneData()).getLane();
        lane.removeListener(this, Lane.GTU_ADD_EVENT);
        lane.removeListener(this, Lane.GTU_REMOVE_EVENT);
        // Lane lane = ((LaneData) kpiLaneDirection.getLaneData()).getLane();
        // int count = 0;
        // List<LaneBasedGTU> currentGtus = new ArrayList<>();
        // try
        // {
        // for (LaneBasedGTU gtu : lane.getGtuList())
        // {
        // DirectedLanePosition dlp = gtu.getReferencePosition();
        // if (dlp.getLane().equals(lane) && sameDirection(kpiLaneDirection.getKpiDirection(), dlp.getGtuDirection()))
        // {
        // currentGtus.add(gtu);
        // count++;
        // }
        // }
        // for (LaneBasedGTU gtu : currentGtus)
        // {
        // // Payload: Object[] {String gtuId, LaneBasedGTU gtu, int count_after_removal, Length position}
        // notify(new TimedEvent<>(Lane.GTU_REMOVE_EVENT, lane, new Object[] { gtu.getId(), gtu, count },
        // gtu.getSimulator().getSimulatorTime()));
        // count--;
        // }
        // }
        // catch (RemoteException | GTUException exception)
        // {
        // throw new RuntimeException("Position cannot be obtained for GTU that is registered on a lane", exception);
        // }
    }

    /**
     * Compares a {@link KpiGtuDirectionality} and a {@link GTUDirectionality}.
     * @param kpiGtuDirectionality KpiGtuDirectionality; kpi gtu direction
     * @param gtuDirectionality GTUDirectionality; gtu direction
     * @return whether both are in the same direction
     */
    private boolean sameDirection(final KpiGtuDirectionality kpiGtuDirectionality, final GTUDirectionality gtuDirectionality)
    {
        if (kpiGtuDirectionality.equals(KpiGtuDirectionality.DIR_PLUS))
        {
            return gtuDirectionality.equals(GTUDirectionality.DIR_PLUS);
        }
        return gtuDirectionality.equals(GTUDirectionality.DIR_MINUS);
    }

    /** {@inheritDoc} */
    @Override
    public final void notify(final EventInterface event) throws RemoteException
    {
        if (event.getType().equals(LaneBasedGTU.LANEBASED_MOVE_EVENT))
        {
            // Payload: [String gtuId, DirectedPoint position, Speed speed, Acceleration acceleration, TurnIndicatorStatus
            // turnIndicatorStatus, Length odometer, Lane referenceLane, Length positionOnReferenceLane]
            Object[] payload = (Object[]) event.getContent();
            KpiLaneDirection laneDirection =
                    new KpiLaneDirection(new LaneData((Lane) payload[6]), KpiGtuDirectionality.DIR_PLUS);
            processGtuMoveEvent(laneDirection, (Length) payload[7], (Speed) payload[2], (Acceleration) payload[3], now(),
                    new GtuData((LaneBasedGTU) event.getSource()));
        }
        else if (event.getType().equals(Lane.GTU_ADD_EVENT))
        {
            // Payload: Object[] {String gtuId, LaneBasedGTU gtu, int count_after_addition}
            Lane lane = (Lane) event.getSource();
            // TODO GTUDirectionality from Lane.GTU_ADD_EVENT
            KpiLaneDirection laneDirection = new KpiLaneDirection(new LaneData(lane), KpiGtuDirectionality.DIR_PLUS);
            if (!contains(laneDirection))
            {
                // we are not sampling this LaneDirection
                return;
            }
            Object[] payload = (Object[]) event.getContent();
            LaneBasedGTU gtu = (LaneBasedGTU) payload[1];
            Length position;
            try
            {
                // TODO Length from Lane.GTU_ADD_EVENT
                position = gtu.position(lane, RelativePosition.REFERENCE_POSITION);
            }
            catch (GTUException exception)
            {
                throw new RuntimeException(exception);
            }
            Speed speed = gtu.getSpeed();
            Acceleration acceleration = gtu.getAcceleration();
            processGtuAddEvent(laneDirection, position, speed, acceleration, now(), new GtuData(gtu));
            LaneDirection lDirection = new LaneDirection(lane, GTUDirectionality.DIR_PLUS);
            if (isIntervalBased())
            {
                scheduleSamplingEvent(gtu, lDirection);
            }
            else
            {
                if (!this.listenersPerGtu.containsKey(gtu.getId()))
                {
                    this.listenersPerGtu.put(gtu.getId(), new LinkedHashSet<>());
                }
                this.listenersPerGtu.get(gtu.getId()).add(lDirection);
                gtu.addListener(this, LaneBasedGTU.LANEBASED_MOVE_EVENT, true);
            }
        }
        else if (event.getType().equals(Lane.GTU_REMOVE_EVENT))
        {
            // Payload: Object[] {String gtuId, LaneBasedGTU gtu, int count_after_removal, Length position}
            Lane lane = (Lane) event.getSource();
            // TODO GTUDirectionality from Lane.GTU_REMOVE_EVENT
            KpiLaneDirection kpiLaneDirection = new KpiLaneDirection(new LaneData(lane), KpiGtuDirectionality.DIR_PLUS);
            Object[] payload = (Object[]) event.getContent();
            LaneBasedGTU gtu = (LaneBasedGTU) payload[1];
            Length position = (Length) payload[3];
            Speed speed = gtu.getSpeed();
            Acceleration acceleration = gtu.getAcceleration();
            processGtuRemoveEvent(kpiLaneDirection, position, speed, acceleration, now(), new GtuData(gtu));
            LaneDirection lDirection = new LaneDirection(lane, GTUDirectionality.DIR_PLUS);
            if (isIntervalBased())
            {
                String gtuId = (String) payload[0];

                if (this.eventPerGtu.get(gtuId) != null)
                {
                    if (this.eventPerGtu.get(gtuId).containsKey(lDirection))
                    {
                        this.simulator.cancelEvent(this.eventPerGtu.get(gtuId).get(lDirection));
                    }
                    this.eventPerGtu.get(gtuId).remove(lDirection);
                    if (this.eventPerGtu.get(gtuId).isEmpty())
                    {
                        this.eventPerGtu.remove(gtuId);
                    }
                }
            }
            else
            {
                // Should not remove if just added on other lane
                this.listenersPerGtu.get(gtu.getId()).remove(lDirection);
                if (this.listenersPerGtu.get(gtu.getId()).isEmpty())
                {
                    this.listenersPerGtu.remove(gtu.getId());
                    gtu.removeListener(this, LaneBasedGTU.LANEBASED_MOVE_EVENT);
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
     * @param gtu LaneBasedGTU; gtu to sample
     * @param laneDirection LaneDirection; lane direction where the gtu is at
     */
    private void scheduleSamplingEvent(final LaneBasedGTU gtu, final LaneDirection laneDirection)
    {
        SimEventInterface<SimTimeDoubleUnit> simEvent;
        try
        {
            // this.simulator.scheduleEvent(simEvent);
            simEvent = this.simulator.scheduleEventRel(this.samplingInterval, this, this, "notifySample",
                    new Object[] {gtu, laneDirection});
        }
        catch (SimRuntimeException exception)
        {
            // should not happen with getSimulatorTime.add()
            throw new RuntimeException("Scheduling sampling in the past.", exception);
        }
        String gtuId = gtu.getId();
        if (!this.eventPerGtu.containsKey(gtuId))
        {
            Map<LaneDirection, SimEventInterface<SimTimeDoubleUnit>> map = new LinkedHashMap<>();
            this.eventPerGtu.put(gtuId, map);
        }
        this.eventPerGtu.get(gtuId).put(laneDirection, simEvent);
    }

    /**
     * Samples a gtu and schedules the next sampling event.
     * @param gtu LaneBasedGTU; gtu to sample
     * @param laneDirection LaneDirection; lane direction where the gtu is at
     */
    public final void notifySample(final LaneBasedGTU gtu, final LaneDirection laneDirection)
    {
        KpiLaneDirection kpiLaneDirection = new KpiLaneDirection(new LaneData(laneDirection.getLane()),
                laneDirection.getDirection().isPlus() ? KpiGtuDirectionality.DIR_PLUS : KpiGtuDirectionality.DIR_MINUS);
        try
        {
            this.processGtuMoveEvent(kpiLaneDirection,
                    gtu.position(laneDirection.getLane(), RelativePosition.REFERENCE_POSITION), gtu.getSpeed(),
                    gtu.getAcceleration(), now(), new GtuData(gtu));
        }
        catch (GTUException exception)
        {
            throw new RuntimeException("Requesting position on lane, but the GTU is not on the lane.", exception);
        }
        scheduleSamplingEvent(gtu, laneDirection);
    }

    /** {@inheritDoc} */
    @Override
    public final int hashCode()
    {
        final int prime = 31;
        int result = super.hashCode();
        result = prime * result + ((this.eventPerGtu == null) ? 0 : this.eventPerGtu.hashCode());
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
        if (this.eventPerGtu == null)
        {
            if (other.eventPerGtu != null)
            {
                return false;
            }
        }
        else if (!this.eventPerGtu.equals(other.eventPerGtu))
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

}
