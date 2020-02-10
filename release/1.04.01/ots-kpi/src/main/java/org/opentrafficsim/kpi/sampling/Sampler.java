package org.opentrafficsim.kpi.sampling;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.CompressedFileWriter;
import org.opentrafficsim.kpi.interfaces.GtuDataInterface;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataType;
import org.opentrafficsim.kpi.sampling.meta.MetaData;
import org.opentrafficsim.kpi.sampling.meta.MetaDataSet;
import org.opentrafficsim.kpi.sampling.meta.MetaDataType;

/**
 * Sampler is the highest level organizer for sampling.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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

    /** Map with all sampling data. */
    private final Map<KpiLaneDirection, TrajectoryGroup<G>> trajectories = new LinkedHashMap<>();

    /** End times of active samplings. */
    private final Map<KpiLaneDirection, Time> endTimes = new LinkedHashMap<>();

    /** Registration of current trajectories of each GTU per lane. */
    private final Map<String, Map<KpiLaneDirection, Trajectory<G>>> trajectoryPerGtu = new LinkedHashMap<>();

    /** Registration of included extended data types. */
    private final Set<ExtendedDataType<?, ?, ?, G>> extendedDataTypes = new LinkedHashSet<>();

    /** Set of registered meta data types. */
    private Set<MetaDataType<?>> registeredMetaDataTypes = new LinkedHashSet<>();

    /** Space time regions. */
    private Set<SpaceTimeRegion> spaceTimeRegions = new LinkedHashSet<>();

    /**
     * @param spaceTimeRegion SpaceTimeRegion; space-time region
     * @throws IllegalStateException if data is not available from the requested start time
     */
    public final void registerSpaceTimeRegion(final SpaceTimeRegion spaceTimeRegion)
    {
        Throw.whenNull(spaceTimeRegion, "SpaceTimeRegion may not be null.");
        Time firstPossibleDataTime;
        if (this.trajectories.containsKey(spaceTimeRegion.getLaneDirection()))
        {
            firstPossibleDataTime = this.trajectories.get(spaceTimeRegion.getLaneDirection()).getStartTime();
        }
        else
        {
            firstPossibleDataTime = now();
        }
        Throw.when(spaceTimeRegion.getStartTime().lt(firstPossibleDataTime), IllegalStateException.class,
                "Space time region with start time %s is defined while data is available from %s onwards.",
                spaceTimeRegion.getStartTime(), firstPossibleDataTime);
        if (this.trajectories.containsKey(spaceTimeRegion.getLaneDirection()))
        {
            this.endTimes.put(spaceTimeRegion.getLaneDirection(),
                    Time.max(this.endTimes.get(spaceTimeRegion.getLaneDirection()), spaceTimeRegion.getEndTime()));
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
    public abstract void scheduleStartRecording(final Time time, final KpiLaneDirection kpiLaneDirection);

    /**
     * Schedules the stop of recording for a given lane-direction.
     * @param time Time; time to stop recording
     * @param kpiLaneDirection KpiLaneDirection; lane-direction to stop recording
     */
    public abstract void scheduleStopRecording(final Time time, final KpiLaneDirection kpiLaneDirection);

    /**
     * Registers meta data types that will be stored with the trajectories.
     * @param metaDataTypes Set&lt;MetaDataType&lt;?&gt;&gt;; meta data types to register
     */
    public final void registerMetaDataTypes(final Set<MetaDataType<?>> metaDataTypes)
    {
        Throw.whenNull(metaDataTypes, "MetaDataTypes may not be null.");
        this.registeredMetaDataTypes.addAll(metaDataTypes);
    }

    /**
     * Registers extended data type that will be stored with the trajectories.
     * @param extendedDataType ExtendedDataType&lt;?,?,?,G&gt;; extended data type to register
     */
    public final void registerExtendedDataType(final ExtendedDataType<?, ?, ?, G> extendedDataType)
    {
        Throw.whenNull(extendedDataType, "ExtendedDataType may not be null.");
        this.extendedDataTypes.add(extendedDataType);
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
     * Start recording at the given time (which should be the current time) on the given lane direction.
     * @param kpiLaneDirection KpiLaneDirection; lane direction
     */
    public final void startRecording(final KpiLaneDirection kpiLaneDirection)
    {
        Throw.whenNull(kpiLaneDirection, "KpiLaneDirection may not be null.");
        if (this.trajectories.containsKey(kpiLaneDirection))
        {
            return;
        }
        this.trajectories.put(kpiLaneDirection, new TrajectoryGroup<>(now(), kpiLaneDirection));
        initRecording(kpiLaneDirection);
    }

    /**
     * Adds listeners to start recording.
     * @param kpiLaneDirection KpiLaneDirection; lane direction to initialize recording for
     */
    public abstract void initRecording(final KpiLaneDirection kpiLaneDirection);

    /**
     * Stop recording at given lane direction.
     * @param kpiLaneDirection KpiLaneDirection; lane direction
     */
    public final void stopRecording(final KpiLaneDirection kpiLaneDirection)
    {
        Throw.whenNull(kpiLaneDirection, "KpiLaneDirection may not be null.");
        if (!this.trajectories.containsKey(kpiLaneDirection) || this.endTimes.get(kpiLaneDirection).gt(now()))
        {
            return;
        }
        finalizeRecording(kpiLaneDirection);
    }

    /**
     * Remove listeners to stop recording.
     * @param kpiLaneDirection KpiLaneDirection; lane direction to finalize recording for
     */
    public abstract void finalizeRecording(final KpiLaneDirection kpiLaneDirection);

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
        this.trajectories.get(kpiLaneDirection).addTrajectory(trajectory);
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
        for (MetaDataType<?> metaDataType : this.registeredMetaDataTypes)
        {
            T value = (T) metaDataType.getValue(gtu);
            if (value != null)
            {
                metaData.put((MetaDataType<T>) metaDataType, value);
            }
        }
        return metaData;
    }

    /**
     * Returns whether there is data for the give lane direction.
     * @param kpiLaneDirection KpiLaneDirection; lane direction
     * @return whether there is data for the give lane direction
     */
    public final boolean contains(final KpiLaneDirection kpiLaneDirection)
    {
        return this.trajectories.containsKey(kpiLaneDirection);
    }

    /**
     * Returns the trajectory group of given lane direction.
     * @param kpiLaneDirection KpiLaneDirection; lane direction
     * @return trajectory group of given lane direction, {@code null} if none
     */
    public final TrajectoryGroup<G> getTrajectoryGroup(final KpiLaneDirection kpiLaneDirection)
    {
        return this.trajectories.get(kpiLaneDirection);
    }

    /**
     * Write the contents of the sampler in to a file. By default this is zipped and numeric data is formated %.3f.
     * @param file String; file
     */
    public final void writeToFile(final String file)
    {
        writeToFile(file, "%.3f", CompressionMethod.ZIP);
    }

    /**
     * Write the contents of the sampler in to a file.
     * @param file String; file
     * @param format String; number format, as used in {@code String.format()}
     * @param compression CompressionMethod; how to compress the data
     */
    public final void writeToFile(final String file, final String format, final CompressionMethod compression)
    {
        int counter = 0;
        BufferedWriter bw = CompressedFileWriter.create(file, compression.equals(CompressionMethod.ZIP));
        // create Query, as this class is designed to filter for space-time regions
        Query<G> query = new Query<>(this, "", new MetaDataSet());
        for (SpaceTimeRegion str : this.spaceTimeRegions)
        {
            query.addSpaceTimeRegion(str.getLaneDirection(), str.getStartPosition(), str.getEndPosition(), str.getStartTime(),
                    str.getEndTime());
        }
        List<TrajectoryGroup<G>> groups = query.getTrajectoryGroups(Time.instantiateSI(Double.POSITIVE_INFINITY));
        try
        {
            // gather all meta data types for the header line
            List<MetaDataType<?>> allMetaDataTypes = new ArrayList<>();
            for (TrajectoryGroup<G> group : groups)
            {
                for (Trajectory<G> trajectory : group.getTrajectories())
                {
                    for (MetaDataType<?> metaDataType : trajectory.getMetaDataTypes())
                    {
                        if (!allMetaDataTypes.contains(metaDataType))
                        {
                            allMetaDataTypes.add(metaDataType);
                        }
                    }
                }
            }
            // gather all extended data types for the header line
            List<ExtendedDataType<?, ?, ?, ?>> allExtendedDataTypes = new ArrayList<>();
            for (TrajectoryGroup<G> group : groups)
            {
                for (Trajectory<?> trajectory : group.getTrajectories())
                {
                    for (ExtendedDataType<?, ?, ?, ?> extendedDataType : trajectory.getExtendedDataTypes())
                    {
                        if (!allExtendedDataTypes.contains(extendedDataType))
                        {
                            allExtendedDataTypes.add(extendedDataType);
                        }
                    }
                }
            }
            // create header line
            StringBuilder str = new StringBuilder();
            str.append("traj#,linkId,laneId&dir,gtuId,t,x,v,a");
            for (MetaDataType<?> metaDataType : allMetaDataTypes)
            {
                str.append(",");
                str.append(metaDataType.getId());
            }
            for (ExtendedDataType<?, ?, ?, ?> extendedDataType : allExtendedDataTypes)
            {
                str.append(",");
                str.append(extendedDataType.getId());
            }
            bw.write(str.toString());
            bw.newLine();
            for (TrajectoryGroup<G> group : groups)
            {
                for (Trajectory<G> trajectory : group.getTrajectories())
                {
                    counter++;
                    float[] t = trajectory.getT();
                    float[] x = trajectory.getX();
                    float[] v = trajectory.getV();
                    float[] a = trajectory.getA();
                    Map<ExtendedDataType<?, ?, ?, ?>, Object> extendedData = new LinkedHashMap<>();
                    for (ExtendedDataType<?, ?, ?, ?> extendedDataType : allExtendedDataTypes)
                    {
                        if (trajectory.contains(extendedDataType))
                        {
                            try
                            {
                                extendedData.put(extendedDataType, trajectory.getExtendedData(extendedDataType));
                            }
                            catch (SamplingException exception)
                            {
                                // should not occur, we obtain the extended data types from the trajectory
                                throw new RuntimeException("Error while loading extended data type.", exception);
                            }
                        }
                    }
                    for (int i = 0; i < t.length; i++)
                    {
                        str = new StringBuilder();
                        str.append(counter);
                        str.append(",");
                        if (!compression.equals(CompressionMethod.OMIT_DUPLICATE_INFO) || i == 0)
                        {
                            str.append(group.getLaneDirection().getLaneData().getLinkData().getId());
                            str.append(",");
                            str.append(group.getLaneDirection().getLaneData().getId());
                            str.append(group.getLaneDirection().getKpiDirection().isPlus() ? "+" : "-");
                            str.append(",");
                            str.append(trajectory.getGtuId());
                            str.append(",");
                        }
                        else
                        {
                            // one trajectory is on the same lane and pertains to the same GTU, no need to repeat data
                            str.append(",,,");
                        }
                        str.append(String.format(format, t[i]));
                        str.append(",");
                        str.append(String.format(format, x[i]));
                        str.append(",");
                        str.append(String.format(format, v[i]));
                        str.append(",");
                        str.append(String.format(format, a[i]));
                        for (MetaDataType<?> metaDataType : allMetaDataTypes)
                        {
                            str.append(",");
                            if (i == 0 && trajectory.contains(metaDataType))
                            {
                                // no need to repeat meta data
                                str.append(metaDataType.formatValue(format, castValue(trajectory.getMetaData(metaDataType))));
                            }
                        }
                        for (ExtendedDataType<?, ?, ?, ?> extendedDataType : allExtendedDataTypes)
                        {
                            str.append(",");
                            if (trajectory.contains(extendedDataType))
                            {
                                try
                                {
                                    str.append(
                                            extendedDataType.formatValue(format, castValue(extendedData, extendedDataType, i)));
                                }
                                catch (SamplingException exception)
                                {
                                    // should not occur, we obtain the extended data types from the trajectory
                                    throw new RuntimeException("Error while loading extended data type.", exception);
                                }
                            }
                        }
                        bw.write(str.toString());
                        bw.newLine();
                    }
                }
            }
        }
        catch (IOException exception)
        {
            throw new RuntimeException("Could not write to file.", exception);
        }
        // close file on fail
        finally
        {
            try
            {
                if (bw != null)
                {
                    bw.close();
                }
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Cast value to type for meta data.
     * @param value Object; value object to cast
     * @return cast value
     */
    @SuppressWarnings("unchecked")
    private <T> T castValue(final Object value)
    {
        return (T) value;
    }

    /**
     * Cast value to type for extended data.
     * @param extendedData Map&lt;ExtendedDataType&lt;?,?,?,?&gt;,Object&gt;; extended data of trajectory in output form
     * @param extendedDataType ExtendedDataType&lt;?,?,?,?&gt;; extended data type
     * @param i int; index of value to return
     * @return cast value
     * @throws SamplingException when the found index is out of bounds
     */
    @SuppressWarnings("unchecked")
    private <T, O, S> T castValue(final Map<ExtendedDataType<?, ?, ?, ?>, Object> extendedData,
            final ExtendedDataType<?, ?, ?, ?> extendedDataType, final int i) throws SamplingException
    {
        // is only called on value directly taken from an ExtendedDataType within range of trajectory
        ExtendedDataType<T, O, S, ?> edt = (ExtendedDataType<T, O, S, ?>) extendedDataType;
        return edt.getOutputValue((O) extendedData.get(edt), i);
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.endTimes == null) ? 0 : this.endTimes.hashCode());
        result = prime * result + ((this.extendedDataTypes == null) ? 0 : this.extendedDataTypes.hashCode());
        result = prime * result + ((this.registeredMetaDataTypes == null) ? 0 : this.registeredMetaDataTypes.hashCode());
        result = prime * result + ((this.trajectories == null) ? 0 : this.trajectories.hashCode());
        result = prime * result + ((this.trajectoryPerGtu == null) ? 0 : this.trajectoryPerGtu.hashCode());
        return result;
    }

    /** {@inheritDoc} */
    @Override
    public boolean equals(final Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (obj == null)
        {
            return false;
        }
        if (getClass() != obj.getClass())
        {
            return false;
        }
        Sampler<?> other = (Sampler<?>) obj;
        if (this.endTimes == null)
        {
            if (other.endTimes != null)
            {
                return false;
            }
        }
        else if (!this.endTimes.equals(other.endTimes))
        {
            return false;
        }
        if (this.extendedDataTypes == null)
        {
            if (other.extendedDataTypes != null)
            {
                return false;
            }
        }
        else if (!this.extendedDataTypes.equals(other.extendedDataTypes))
        {
            return false;
        }
        if (this.registeredMetaDataTypes == null)
        {
            if (other.registeredMetaDataTypes != null)
            {
                return false;
            }
        }
        else if (!this.registeredMetaDataTypes.equals(other.registeredMetaDataTypes))
        {
            return false;
        }
        if (this.trajectories == null)
        {
            if (other.trajectories != null)
            {
                return false;
            }
        }
        else if (!this.trajectories.equals(other.trajectories))
        {
            return false;
        }
        if (this.trajectoryPerGtu == null)
        {
            if (other.trajectoryPerGtu != null)
            {
                return false;
            }
        }
        else if (!this.trajectoryPerGtu.equals(other.trajectoryPerGtu))
        {
            return false;
        }
        return true;
    }

    /**
     * Defines the compression method for stored data.
     * <p>
     * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 3 mei 2017 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    public enum CompressionMethod
    {
        /** No compression. */
        NONE,

        /** Duplicate info per trajectory is only stored at the first sample, and empty for other samples. */
        OMIT_DUPLICATE_INFO,

        /** Zip compression. */
        ZIP,

    }

}
