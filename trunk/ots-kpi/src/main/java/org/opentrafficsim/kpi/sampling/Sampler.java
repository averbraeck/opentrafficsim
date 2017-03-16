package org.opentrafficsim.kpi.sampling;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import org.djunits.value.vdouble.scalar.Acceleration;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Speed;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.kpi.interfaces.GtuDataInterface;
import org.opentrafficsim.kpi.sampling.data.ExtendedDataType;
import org.opentrafficsim.kpi.sampling.meta.MetaData;
import org.opentrafficsim.kpi.sampling.meta.MetaDataType;

import nl.tudelft.simulation.language.Throw;

/**
 * Sampler is the highest level organizer for sampling.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Sep 22, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public abstract class Sampler
{

    /** Map with all sampling data. */
    private final Map<KpiLaneDirection, TrajectoryGroup> trajectories = new LinkedHashMap<>();

    /** End times of active samplings. */
    private final Map<KpiLaneDirection, Time> endTimes = new LinkedHashMap<>();

    /** Registration of current trajectories of each GTU per lane. */
    private final Map<String, Map<KpiLaneDirection, Trajectory>> trajectoryPerGtu = new LinkedHashMap<>();

    /** Registration of included extended data types. */
    private final Set<ExtendedDataType<?>> extendedDataTypes = new LinkedHashSet<>();

    /** Set of registered meta data types. */
    private Set<MetaDataType<?>> registeredMetaDataTypes = new LinkedHashSet<>();

    /**
     * @param spaceTimeRegion space-time region
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
    }

    /**
     * Returns the current simulation time.
     * @return current simulation time
     */
    public abstract Time now();

    /**
     * Schedules the start of recording for a given lane-direction.
     * @param time time to start recording
     * @param kpiLaneDirection lane-direction to start recording
     */
    public abstract void scheduleStartRecording(final Time time, final KpiLaneDirection kpiLaneDirection);

    /**
     * Schedules the stop of recording for a given lane-direction.
     * @param time time to stop recording
     * @param kpiLaneDirection lane-direction to stop recording
     */
    public abstract void scheduleStopRecording(final Time time, final KpiLaneDirection kpiLaneDirection);

    /**
     * Registers meta data types that will be stored with the trajectories.
     * @param metaDataTypes meta data types to register
     */
    public final void registerMetaDataTypes(final Set<MetaDataType<?>> metaDataTypes)
    {
        Throw.whenNull(metaDataTypes, "MetaDataTypes may not be null.");
        this.registeredMetaDataTypes.addAll(metaDataTypes);
    }

    /**
     * Registers extended data type that will be stored with the trajectories.
     * @param extendedDataType extended data type to register
     */
    public final void registerExtendedDataType(final ExtendedDataType<?> extendedDataType)
    {
        Throw.whenNull(extendedDataType, "ExtendedDataType may not be null.");
        this.extendedDataTypes.add(extendedDataType);
    }

    /**
     * Whether this sampler has the given extended data type registered to it.
     * @param extendedDataType extended data type
     * @return whether this sampler has the given extended data type registered to it
     */
    public boolean contains(final ExtendedDataType<?> extendedDataType)
    {
        return this.extendedDataTypes.contains(extendedDataType);
    }

    /**
     * Start recording at the given time (which should be the current time) on the given lane direction.
     * @param kpiLaneDirection lane direction
     */
    public final void startRecording(final KpiLaneDirection kpiLaneDirection)
    {
        Throw.whenNull(kpiLaneDirection, "KpiLaneDirection may not be null.");
        if (this.trajectories.containsKey(kpiLaneDirection))
        {
            return;
        }
        this.trajectories.put(kpiLaneDirection, new TrajectoryGroup(now(), kpiLaneDirection));
        initRecording(kpiLaneDirection);
    }

    /**
     * Adds listeners to start recording.
     * @param kpiLaneDirection lane direction to initialize recording for
     */
    public abstract void initRecording(final KpiLaneDirection kpiLaneDirection);

    /**
     * Stop recording at given lane direction.
     * @param kpiLaneDirection lane direction
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
     * @param kpiLaneDirection lane direction to finalize recording for
     */
    public abstract void finalizeRecording(final KpiLaneDirection kpiLaneDirection);

    /**
     * Creates a trajectory with the current snapshot of a GTU.
     * @param kpiLaneDirection lane direction the gtu is at
     * @param position position of the gtu on the lane
     * @param speed speed of the gtu
     * @param acceleration acceleration of the gtu
     * @param time current time
     * @param gtu gtu
     */
    public final void processGtuAddEvent(final KpiLaneDirection kpiLaneDirection, final Length position, final Speed speed,
            final Acceleration acceleration, final Time time, final GtuDataInterface gtu)
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
        Trajectory trajectory = new Trajectory(gtu, makeMetaData(gtu), this.extendedDataTypes, kpiLaneDirection);
        if (!this.trajectoryPerGtu.containsKey(gtuId))
        {
            Map<KpiLaneDirection, Trajectory> map = new LinkedHashMap<>();
            this.trajectoryPerGtu.put(gtuId, map);
        }
        this.trajectoryPerGtu.get(gtuId).put(kpiLaneDirection, trajectory);
        this.trajectories.get(kpiLaneDirection).addTrajectory(trajectory);
        processGtuMoveEvent(kpiLaneDirection, position, speed, acceleration, time, gtu);
    }

    /**
     * Adds a new snapshot of a GTU to its recording trajectory, if recorded. This method may be invoked on GTU that are not
     * being recorded; the event will then be ignored.
     * @param kpiLaneDirection lane direction the gtu is at
     * @param position position of the gtu on the lane
     * @param speed speed of the gtu
     * @param acceleration acceleration of the gtu
     * @param time current time
     * @param gtu gtu
     */
    public final void processGtuMoveEvent(final KpiLaneDirection kpiLaneDirection, final Length position, final Speed speed,
            final Acceleration acceleration, final Time time, final GtuDataInterface gtu)
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
     * @param kpiLaneDirection lane direction the gtu is at
     * @param position position of the gtu on the lane
     * @param speed speed of the gtu
     * @param acceleration acceleration of the gtu
     * @param time current time
     * @param gtu gtu
     */
    public final void processGtuRemoveEvent(final KpiLaneDirection kpiLaneDirection, final Length position, final Speed speed,
            final Acceleration acceleration, final Time time, final GtuDataInterface gtu)
    {
        processGtuMoveEvent(kpiLaneDirection, position, speed, acceleration, time, gtu);
        processGtuRemoveEvent(kpiLaneDirection, gtu);
    }

    /**
     * Finalizes a trajectory.
     * @param kpiLaneDirection lane direction the gtu is at
     * @param gtu gtu
     */
    public final void processGtuRemoveEvent(final KpiLaneDirection kpiLaneDirection, final GtuDataInterface gtu)
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
     * @param gtu gtu to return meta data for
     * @param <T> underlying type of a meta data type
     * @return meta data for the given gtu
     */
    @SuppressWarnings("unchecked")
    private <T> MetaData makeMetaData(final GtuDataInterface gtu)
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
     * @param kpiLaneDirection lane direction
     * @return whether there is data for the give lane direction
     */
    public final boolean contains(final KpiLaneDirection kpiLaneDirection)
    {
        return this.trajectories.containsKey(kpiLaneDirection);
    }

    /**
     * Returns the trajectory group of given lane direction.
     * @param kpiLaneDirection lane direction
     * @return trajectory group of given lane direction, {@code null} if none
     */
    public final TrajectoryGroup getTrajectoryGroup(final KpiLaneDirection kpiLaneDirection)
    {
        return this.trajectories.get(kpiLaneDirection);
    }

    /**
     * Write the contents of the sampler in to a file. By default this is zipped and numeric data is formated %.3f.
     * @param file file
     */
    public final void writeToFile(final String file)
    {
        writeToFile(file, "%.3f", true);
    }

    /**
     * Write the contents of the sampler in to a file.
     * @param file file
     * @param format number format, as used in {@code String.format()}
     * @param zipped whether to zip the file
     */
    // TODO This returns all data, regardless of registered space-time regions. We need a query to have space-time regions.
    public final void writeToFile(String file, final String format, final boolean zipped)
    {
        String name = null;
        if (zipped)
        {
            File f = new File(file);
            name = f.getName();
            if (!file.endsWith(".zip"))
            {
                file += ".zip";
            }
        }
        int counter = 0;
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        OutputStreamWriter osw = null;
        BufferedWriter bw = null;
        try
        {
            fos = new FileOutputStream(file);
            if (zipped)
            {
                zos = new ZipOutputStream(fos);
                zos.putNextEntry(new ZipEntry(name));
                osw = new OutputStreamWriter(zos);
            }
            else
            {
                osw = new OutputStreamWriter(fos);
            }
            bw = new BufferedWriter(osw);
            // gather all meta data types for the header line
            List<MetaDataType<?>> allMetaDataTypes = new ArrayList<>();
            for (KpiLaneDirection kpiLaneDirection : this.trajectories.keySet())
            {
                for (Trajectory trajectory : this.trajectories.get(kpiLaneDirection).getTrajectories())
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
            List<ExtendedDataType<?>> allExtendedDataTypes = new ArrayList<>();
            for (KpiLaneDirection kpiLaneDirection : this.trajectories.keySet())
            {
                for (Trajectory trajectory : this.trajectories.get(kpiLaneDirection).getTrajectories())
                {
                    for (ExtendedDataType<?> extendedDataType : trajectory.getExtendedDataTypes())
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
            for (ExtendedDataType<?> extendedDataType : allExtendedDataTypes)
            {
                str.append(",");
                str.append(extendedDataType.getId());
            }
            bw.write(str.toString());
            bw.newLine();
            for (KpiLaneDirection kpiLaneDirection : this.trajectories.keySet())
            {
                for (Trajectory trajectory : this.trajectories.get(kpiLaneDirection).getTrajectories())
                {
                    counter++;
                    float[] t = trajectory.getT();
                    float[] x = trajectory.getX();
                    float[] v = trajectory.getV();
                    float[] a = trajectory.getA();
                    for (int i = 0; i < t.length; i++)
                    {
                        str = new StringBuilder();
                        str.append(counter);
                        str.append(",");
                        if (i == 0)
                        {
                            str.append(kpiLaneDirection.getLaneData().getLinkData().getId());
                            str.append(",");
                            str.append(kpiLaneDirection.getLaneData().getId());
                            str.append(kpiLaneDirection.getKpiDirection().isPlus() ? "+" : "-");
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
                        for (ExtendedDataType<?> extendedDataType : allExtendedDataTypes)
                        {
                            str.append(",");
                            if (trajectory.contains(extendedDataType))
                            {
                                //
                                try
                                {
                                    str.append(extendedDataType.formatValue(format,
                                            castValue(trajectory.getExtendedData(extendedDataType).get(i))));
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
                if (osw != null)
                {
                    osw.close();
                }
                if (zos != null)
                {
                    zos.close();
                }
                if (fos != null)
                {
                    fos.close();
                }
            }
            catch (IOException ex)
            {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Cast value to type.
     * @param value value to casts
     * @return cast value
     */
    @SuppressWarnings("unchecked")
    private <T> T castValue(Object value)
    {
        // is only called on value directly taken from an ExtendedDataType
        return (T) value;
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
        Sampler other = (Sampler) obj;
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

}
