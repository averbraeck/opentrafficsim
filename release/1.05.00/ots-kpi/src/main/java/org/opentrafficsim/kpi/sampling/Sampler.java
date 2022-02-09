package org.opentrafficsim.kpi.sampling;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.kpi.interfaces.GtuDataInterface;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataType;
import org.opentrafficsim.kpi.sampling.meta.FilterDataType;
import org.opentrafficsim.kpi.sampling.meta.MetaData;

/**
 * Sampler is the highest level organizer for sampling.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 22, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <G> gtu data type
 */
public abstract class Sampler<G extends GtuDataInterface>
{

    /** Sampler data. */
    private final SamplerData<G> samplerData;

    /** Registration of included extended data types. */
    private final Set<ExtendedDataType<?, ?, ?, G>> extendedDataTypes;

    /** Set of registered filter data types. */
    private final Set<FilterDataType<?>> filterDataTypes;

    /** Registration of current trajectories of each GTU per lane. */
    private final Map<String, Map<KpiLaneDirection, Trajectory<G>>> trajectoryPerGtu = new LinkedHashMap<>();

    /** End times of active samplings. */
    private final Map<KpiLaneDirection, Time> endTimes = new LinkedHashMap<>();

    /** Space time regions. */
    private Set<SpaceTimeRegion> spaceTimeRegions = new LinkedHashSet<>();

    /**
     * Constructor.
     * @param extendedDataTypes Set&lt;ExtendedDataType&lt;?, ?, ?, G&gt;&gt;; extended data types
     * @param filterDataTypes Set&lt;FilterDataType&lt;?&gt;&gt;; filter data types
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Sampler(final Set<ExtendedDataType<?, ?, ?, G>> extendedDataTypes, final Set<FilterDataType<?>> filterDataTypes)
    {
        this.extendedDataTypes = new LinkedHashSet<>(extendedDataTypes);
        this.filterDataTypes = new LinkedHashSet<>(filterDataTypes);
        Set<Column<?>> columns = new LinkedHashSet<>();
        // TODO: fixed columns!
        for (ExtendedDataType<?, ?, ?, G> extendedDataType : this.extendedDataTypes)
        {
            columns.add(new SimpleColumn(extendedDataType.getId(), extendedDataType.getId(), extendedDataType.getType()));
        }
        for (FilterDataType<?> filterDataType : this.filterDataTypes)
        {
            columns.add(new SimpleColumn(filterDataType.getId(), filterDataType.getId(), String.class));
        }
        this.samplerData = new SamplerData<>(columns);
    }

    /**
     * Underlying sampler data.
     * @return SamplerData&lt;G&gt;; underlying sampler data
     */
    public SamplerData<G> getSamplerData()
    {
        return this.samplerData;
    }

    /**
     * Whether this sampler has the given extended data type registered to it.
     * @param extendedDataType ExtendedDataType&lt;?,?,?,?&gt;; extended data type
     * @return whether this sampler has the given extended data type registered to it
     */
    public boolean contains(final ExtendedDataType<?, ?, ?, ?> extendedDataType)
    {
        return this.extendedDataTypes.contains(extendedDataType);
    }

    /**
     * Registers a space-time region. Data will be recorded across the entire length of a lane, but only during specified time
     * periods.
     * @param spaceTimeRegion SpaceTimeRegion; space-time region
     * @throws IllegalStateException if data is not available from the requested start time
     */
    public final void registerSpaceTimeRegion(final SpaceTimeRegion spaceTimeRegion)
    {
        Throw.whenNull(spaceTimeRegion, "SpaceTimeRegion may not be null.");
        Time firstPossibleDataTime;
        if (this.samplerData.contains(spaceTimeRegion.getLaneDirection()))
        {
            firstPossibleDataTime = this.samplerData.getTrajectoryGroup(spaceTimeRegion.getLaneDirection()).getStartTime();
        }
        else
        {
            firstPossibleDataTime = now();
        }
        Throw.when(spaceTimeRegion.getStartTime().lt(firstPossibleDataTime), IllegalStateException.class,
            "Space time region with start time %s is defined while data is available from %s onwards.", spaceTimeRegion
                .getStartTime(), firstPossibleDataTime);
        if (this.samplerData.contains(spaceTimeRegion.getLaneDirection()))
        {
            this.endTimes.put(spaceTimeRegion.getLaneDirection(), Time.max(this.endTimes.get(spaceTimeRegion
                .getLaneDirection()), spaceTimeRegion.getEndTime()));
        }
        else
        {
            this.endTimes.put(spaceTimeRegion.getLaneDirection(), spaceTimeRegion.getEndTime());
            scheduleStartRecording(spaceTimeRegion.getStartTime(), spaceTimeRegion.getLaneDirection());
        }
        scheduleStopRecording(this.endTimes.get(spaceTimeRegion.getLaneDirection()), spaceTimeRegion.getLaneDirection());
        this.spaceTimeRegions.add(spaceTimeRegion);
    }

    /**
     * Returns the current simulation time.
     * @return current simulation time
     */
    public abstract Time now();

    /**
     * Schedules the start of recording for a given lane-direction.
     * @param time Time; time to start recording
     * @param kpiLaneDirection KpiLaneDirection; lane-direction to start recording
     */
    public abstract void scheduleStartRecording(Time time, KpiLaneDirection kpiLaneDirection);

    /**
     * Schedules the stop of recording for a given lane-direction.
     * @param time Time; time to stop recording
     * @param kpiLaneDirection KpiLaneDirection; lane-direction to stop recording
     */
    public abstract void scheduleStopRecording(Time time, KpiLaneDirection kpiLaneDirection);

    /**
     * Start recording at the given time (which should be the current time) on the given lane direction.
     * @param kpiLaneDirection KpiLaneDirection; lane direction
     */
    public final void startRecording(final KpiLaneDirection kpiLaneDirection)
    {
        Throw.whenNull(kpiLaneDirection, "KpiLaneDirection may not be null.");
        if (this.samplerData.contains(kpiLaneDirection))
        {
            return;
        }
        this.samplerData.putTrajectoryGroup(kpiLaneDirection, new TrajectoryGroup<>(now(), kpiLaneDirection));
        initRecording(kpiLaneDirection);
    }

    /**
     * Adds listeners to start recording.
     * @param kpiLaneDirection KpiLaneDirection; lane direction to initialize recording for
     */
    public abstract void initRecording(KpiLaneDirection kpiLaneDirection);

    /**
     * Stop recording at given lane direction.
     * @param kpiLaneDirection KpiLaneDirection; lane direction
     */
    public final void stopRecording(final KpiLaneDirection kpiLaneDirection)
    {
        Throw.whenNull(kpiLaneDirection, "KpiLaneDirection may not be null.");
        if (!this.samplerData.contains(kpiLaneDirection) || this.endTimes.get(kpiLaneDirection).gt(now()))
        {
            return;
        }
        finalizeRecording(kpiLaneDirection);
    }

    /**
     * Remove listeners to stop recording.
     * @param kpiLaneDirection KpiLaneDirection; lane direction to finalize recording for
     */
    public abstract void finalizeRecording(KpiLaneDirection kpiLaneDirection);

    /**
     * Creates a trajectory with the current snapshot of a GTU.
     * @param kpiLaneDirection KpiLaneDirection; lane direction the gtu is at
     * @param position Length; position of the gtu on the lane
     * @param speed Speed; speed of the gtu
     * @param acceleration Acceleration; acceleration of the gtu
     * @param time Time; current time
     * @param gtu G; gtu
     */
    public final void processGtuAddEvent(final KpiLaneDirection kpiLaneDirection, final Length position, final Speed speed,
            final Acceleration acceleration, final Time time, final G gtu)
    {
        Throw.whenNull(kpiLaneDirection, "KpiLaneDirection may not be null.");
        Throw.whenNull(position, "Position may not be null.");
        Throw.whenNull(speed, "Speed may not be null.");
        Throw.whenNull(acceleration, "Acceleration may not be null.");
        Throw.whenNull(time, "Time may not be null.");
        Throw.whenNull(gtu, "GtuDataInterface may not be null.");
        if (kpiLaneDirection.getLaneData().getLength().lt(position))
        {
            // ignore event if beyond lane length (may happen during lane change)
            return;
        }
        String gtuId = gtu.getId();
        Trajectory<G> trajectory = new Trajectory<>(gtu, makeMetaData(gtu), this.extendedDataTypes, kpiLaneDirection);
        if (!this.trajectoryPerGtu.containsKey(gtuId))
        {
            Map<KpiLaneDirection, Trajectory<G>> map = new LinkedHashMap<>();
            this.trajectoryPerGtu.put(gtuId, map);
        }
        this.trajectoryPerGtu.get(gtuId).put(kpiLaneDirection, trajectory);
        this.samplerData.getTrajectoryGroup(kpiLaneDirection).addTrajectory(trajectory);
        processGtuMoveEvent(kpiLaneDirection, position, speed, acceleration, time, gtu);
    }

    /**
     * Adds a new snapshot of a GTU to its recording trajectory, if recorded. This method may be invoked on GTU that are not
     * being recorded; the event will then be ignored.
     * @param kpiLaneDirection KpiLaneDirection; lane direction the gtu is at
     * @param position Length; position of the gtu on the lane
     * @param speed Speed; speed of the gtu
     * @param acceleration Acceleration; acceleration of the gtu
     * @param time Time; current time
     * @param gtu G; gtu
     */
    public final void processGtuMoveEvent(final KpiLaneDirection kpiLaneDirection, final Length position, final Speed speed,
            final Acceleration acceleration, final Time time, final G gtu)
    {
        Throw.whenNull(kpiLaneDirection, "KpiLaneDirection may not be null.");
        Throw.whenNull(position, "Position may not be null.");
        Throw.whenNull(speed, "Speed may not be null.");
        Throw.whenNull(acceleration, "Acceleration may not be null.");
        Throw.whenNull(time, "Time may not be null.");
        Throw.whenNull(gtu, "GtuDataInterface may not be null.");
        String gtuId = gtu.getId();
        if (this.trajectoryPerGtu.containsKey(gtuId) && this.trajectoryPerGtu.get(gtuId).containsKey(kpiLaneDirection))
        {
            this.trajectoryPerGtu.get(gtuId).get(kpiLaneDirection).add(position, speed, acceleration, time, gtu);
        }
    }

    /**
     * Finalizes a trajectory with the current snapshot of a GTU.
     * @param kpiLaneDirection KpiLaneDirection; lane direction the gtu is at
     * @param position Length; position of the gtu on the lane
     * @param speed Speed; speed of the gtu
     * @param acceleration Acceleration; acceleration of the gtu
     * @param time Time; current time
     * @param gtu G; gtu
     */
    public final void processGtuRemoveEvent(final KpiLaneDirection kpiLaneDirection, final Length position, final Speed speed,
            final Acceleration acceleration, final Time time, final G gtu)
    {
        processGtuMoveEvent(kpiLaneDirection, position, speed, acceleration, time, gtu);
        processGtuRemoveEvent(kpiLaneDirection, gtu);
    }

    /**
     * Finalizes a trajectory.
     * @param kpiLaneDirection KpiLaneDirection; lane direction the gtu is at
     * @param gtu G; gtu
     */
    public final void processGtuRemoveEvent(final KpiLaneDirection kpiLaneDirection, final G gtu)
    {
        Throw.whenNull(kpiLaneDirection, "KpiLaneDirection may not be null.");
        Throw.whenNull(gtu, "GtuDataInterface may not be null.");
        String gtuId = gtu.getId();
        if (this.trajectoryPerGtu.containsKey(gtuId))
        {
            this.trajectoryPerGtu.get(gtuId).remove(kpiLaneDirection);
            if (this.trajectoryPerGtu.get(gtuId).isEmpty())
            {
                this.trajectoryPerGtu.remove(gtuId);
            }
        }
    }

    /**
     * @param gtu G; gtu to return meta data for
     * @param <T> underlying type of a meta data type
     * @return meta data for the given gtu
     */
    @SuppressWarnings("unchecked")
    private <T> MetaData makeMetaData(final G gtu)
    {
        MetaData metaData = new MetaData();
        for (FilterDataType<?> metaDataType : this.filterDataTypes)
        {
            T value = (T) metaDataType.getValue(gtu);
            if (value != null)
            {
                metaData.put((FilterDataType<T>) metaDataType, value);
            }
        }
        return metaData;
    }

    // TODO: hashCode / equals

}
