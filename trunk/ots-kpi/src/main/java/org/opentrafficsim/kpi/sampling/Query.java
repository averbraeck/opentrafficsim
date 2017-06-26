package org.opentrafficsim.kpi.sampling;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.kpi.interfaces.LaneDataInterface;
import org.opentrafficsim.kpi.interfaces.LinkDataInterface;
import org.opentrafficsim.kpi.sampling.meta.MetaDataSet;
import org.opentrafficsim.kpi.sampling.meta.MetaDataType;

import nl.tudelft.simulation.immutablecollections.ImmutableIterator;
import nl.tudelft.simulation.language.Throw;

/**
 * A query defines which subset of trajectory information should be included. This is in terms of space-time regions, and in
 * terms of meta data of trajectories, e.g. only include trajectories of trucks.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Sep 21, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public final class Query implements Identifiable
{
    /** unique id. */
    private final String id;

    /** Sampling. */
    private final Sampler sampler;

    /** Description. */
    private final String description;

    /** Meta data set. */
    private final MetaDataSet metaDataSet;

    /** Update frequency. */
    private final Frequency updateFrequency;

    /** Interval to gather statistics over. */
    private final Duration interval;

    /** List of space-time regions of this query. */
    private final List<SpaceTimeRegion> spaceTimeRegions = new ArrayList<>();

    /**
     * @param sampler sampler
     * @param id id
     * @param description description
     * @param metaDataSet meta data
     * @throws NullPointerException if sampling, description or metaDataSet is null
     */
    public Query(final Sampler sampler, final String id, final String description, final MetaDataSet metaDataSet)
    {
        this(sampler, description, metaDataSet, null, null);
    }

    /**
     * @param sampler sampler
     * @param id id
     * @param description description
     * @param metaDataSet meta data
     * @param interval interval to gather statistics over
     * @throws NullPointerException if sampling, description or metaDataSet is null
     */
    public Query(final Sampler sampler, final String id, final String description, final MetaDataSet metaDataSet,
            final Duration interval)
    {
        this(sampler, id, description, metaDataSet, null, interval);
    }

    /**
     * @param sampler sampler
     * @param id id
     * @param description description
     * @param metaDataSet meta data
     * @param updateFrequency update frequency
     * @throws NullPointerException if sampling, description or metaDataSet is null
     */
    public Query(final Sampler sampler, final String id, final String description, final MetaDataSet metaDataSet,
            final Frequency updateFrequency)
    {
        this(sampler, id, description, metaDataSet, updateFrequency, null);
    }

    /**
     * @param sampler sampler
     * @param id id
     * @param description description
     * @param metaDataSet meta data
     * @param updateFrequency update frequency
     * @param interval interval to gather statistics over
     * @throws NullPointerException if sampling, description or metaDataSet is null
     */
    public Query(final Sampler sampler, final String id, final String description, final MetaDataSet metaDataSet,
            final Frequency updateFrequency, final Duration interval)
    {
        Throw.whenNull(sampler, "Sampling may not be null.");
        Throw.whenNull(description, "Description may not be null.");
        Throw.whenNull(metaDataSet, "Meta data may not be null.");
        this.sampler = sampler;
        this.metaDataSet = new MetaDataSet(metaDataSet);
        this.id = id == null ? UUID.randomUUID().toString() : id;
        this.description = description;
        this.updateFrequency = updateFrequency;
        this.interval = interval;
        sampler.registerMetaDataTypes(metaDataSet.getMetaDataTypes());
    }

    /**
     * @param sampler sampler
     * @param description description
     * @param metaDataSet meta data
     * @throws NullPointerException if sampling, description or metaDataSet is null
     */
    public Query(final Sampler sampler, final String description, final MetaDataSet metaDataSet)
    {
        this(sampler, null, description, metaDataSet, null, null);
    }

    /**
     * @param sampler sampler
     * @param description description
     * @param metaDataSet meta data
     * @param interval interval to gather statistics over
     * @throws NullPointerException if sampling, description or metaDataSet is null
     */
    public Query(final Sampler sampler, final String description, final MetaDataSet metaDataSet, final Duration interval)
    {
        this(sampler, null, description, metaDataSet, null, interval);
    }

    /**
     * @param sampler sampler
     * @param description description
     * @param metaDataSet meta data
     * @param updateFrequency update frequency
     * @throws NullPointerException if sampling, description or metaDataSet is null
     */
    public Query(final Sampler sampler, final String description, final MetaDataSet metaDataSet,
            final Frequency updateFrequency)
    {
        this(sampler, null, description, metaDataSet, updateFrequency, null);
    }

    /**
     * @param sampler sampler
     * @param description description
     * @param metaDataSet meta data
     * @param updateFrequency update frequency
     * @param interval interval to gather statistics over
     * @throws NullPointerException if sampling, description or metaDataSet is null
     */
    public Query(final Sampler sampler, final String description, final MetaDataSet metaDataSet,
            final Frequency updateFrequency, final Duration interval)
    {
        this(sampler, null, description, metaDataSet, updateFrequency, interval);
    }

    /**
     * return the unique id for the query.
     * @return String; the unique id for the query
     */
    public String getId()
    {
        return this.id.toString();
    }

    /**
     * @return description
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * @return updateFrequency.
     */
    public Frequency getUpdateFrequency()
    {
        return this.updateFrequency;
    }

    /**
     * @return interval.
     */
    public Duration getInterval()
    {
        return this.interval;
    }

    /**
     * @return number of meta data entries
     */
    public int metaDataSize()
    {
        return this.metaDataSet.size();
    }

    /**
     * @return iterator over meta data entries, removal is not allowed
     */
    public Iterator<Entry<MetaDataType<?>, Set<?>>> getMetaDataSetIterator()
    {
        return this.metaDataSet.getMetaDataSetIterator();
    }

    /**
     * Defines a region in space and time for which this query is valid. All lanes in the link are included.
     * @param link link
     * @param direction direction
     * @param startPosition start position
     * @param endPosition end position
     * @param startTime start time
     * @param endTime end time
     */
    public void addSpaceTimeRegionLink(final LinkDataInterface link, final KpiGtuDirectionality direction,
            final Length startPosition, final Length endPosition, final Time startTime, final Time endTime)
    {
        Throw.whenNull(link, "Link may not be null.");
        Throw.whenNull(direction, "Direction may not be null.");
        Throw.whenNull(startPosition, "Start position may not be null.");
        Throw.whenNull(endPosition, "End position may not be null.");
        Throw.whenNull(startTime, "Start time may not be null.");
        Throw.whenNull(endTime, "End time may not be null.");
        Throw.when(endPosition.lt(startPosition), IllegalArgumentException.class,
                "End position should be greater than start position.");
        Throw.when(endTime.lt(startTime), IllegalArgumentException.class, "End time should be greater than start time.");
        for (LaneDataInterface lane : link.getLaneDatas())
        {
            Length x0 = new Length(lane.getLength().si * startPosition.si / link.getLength().si, LengthUnit.SI);
            Length x1 = new Length(lane.getLength().si * endPosition.si / link.getLength().si, LengthUnit.SI);
            addSpaceTimeRegion(new KpiLaneDirection(lane, direction), x0, x1, startTime, endTime);
        }
    }

    /**
     * Defines a region in space and time for which this query is valid.
     * @param laneDirection lane direction
     * @param startPosition start position
     * @param endPosition end position
     * @param startTime start time
     * @param endTime end time
     */
    public void addSpaceTimeRegion(final KpiLaneDirection laneDirection, final Length startPosition, final Length endPosition,
            final Time startTime, final Time endTime)
    {
        Throw.whenNull(laneDirection, "Lane direction may not be null.");
        Throw.whenNull(startPosition, "Start position may not be null.");
        Throw.whenNull(endPosition, "End position may not be null.");
        Throw.whenNull(startTime, "Start time may not be null.");
        Throw.whenNull(endTime, "End time may not be null.");
        Throw.when(endPosition.lt(startPosition), IllegalArgumentException.class,
                "End position should be greater than start position.");
        Throw.when(endTime.lt(startTime), IllegalArgumentException.class, "End time should be greater than start time.");
        SpaceTimeRegion spaceTimeRegion = new SpaceTimeRegion(laneDirection, startPosition, endPosition, startTime, endTime);
        this.sampler.registerSpaceTimeRegion(spaceTimeRegion);
        this.spaceTimeRegions.add(spaceTimeRegion);
    }

    /**
     * @return number of space-time regions
     */
    public int spaceTimeRegionSize()
    {
        return this.spaceTimeRegions.size();
    }

    /**
     * @return iterator over space-time regions, removal is not allowed
     */
    public Iterator<SpaceTimeRegion> getSpaceTimeIterator()
    {
        return new ImmutableIterator<>(this.spaceTimeRegions.iterator());
    }

    /**
     * Returns a list of TrajectoryGroups in accordance with the query. Each {@code TrajectoryGroup} contains {@code Trajectory}
     * objects pertaining to a {@code SpaceTimeRegion} from the query. A {@code Trajectory} is only included if all the meta
     * data of this query accepts the trajectory. This method uses {@code Time.ZERO} as start.
     * @param endTime start time of interval to get trajectory groups for
     * @param <T> underlying class of meta data type and its value
     * @return list of trajectory groups in accordance with the query
     */
    public <T> List<TrajectoryGroup> getTrajectoryGroups(final Time endTime)
    {
        return getTrajectoryGroups(Time.ZERO, endTime);
    }

    /**
     * Returns a list of TrajectoryGroups in accordance with the query. Each {@code TrajectoryGroup} contains {@code Trajectory}
     * objects pertaining to a {@code SpaceTimeRegion} from the query. A {@code Trajectory} is only included if all the meta
     * data of this query accepts the trajectory.
     * @param startTime start time of interval to get trajectory groups for
     * @param endTime start time of interval to get trajectory groups for
     * @param <T> underlying class of meta data type and its value
     * @return list of trajectory groups in accordance with the query
     */
    @SuppressWarnings("unchecked")
    public <T> List<TrajectoryGroup> getTrajectoryGroups(final Time startTime, final Time endTime)
    {
        Throw.whenNull(startTime, "Start t may not be null.");
        Throw.whenNull(endTime, "End t may not be null.");
        // Step 1) gather trajectories per GTU, truncated over space and time
        Map<String, TrajectoryAcceptList> trajectoryAcceptLists = new HashMap<>();
        List<TrajectoryGroup> trajectoryGroupList = new ArrayList<>();
        for (SpaceTimeRegion spaceTimeRegion : this.spaceTimeRegions)
        {
            Time start = startTime.gt(spaceTimeRegion.getStartTime()) ? startTime : spaceTimeRegion.getStartTime();
            Time end = endTime.lt(spaceTimeRegion.getEndTime()) ? endTime : spaceTimeRegion.getEndTime();
            TrajectoryGroup trajectoryGroup;
            if (this.sampler.getTrajectoryGroup(spaceTimeRegion.getLaneDirection()) == null)
            {
                trajectoryGroup = new TrajectoryGroup(start, spaceTimeRegion.getLaneDirection());
            }
            else
            {
                trajectoryGroup = this.sampler.getTrajectoryGroup(spaceTimeRegion.getLaneDirection())
                        .getTrajectoryGroup(spaceTimeRegion.getStartPosition(), spaceTimeRegion.getEndPosition(), start, end);
            }
            for (Trajectory trajectory : trajectoryGroup.getTrajectories())
            {
                if (!trajectoryAcceptLists.containsKey(trajectory.getGtuId()))
                {
                    trajectoryAcceptLists.put(trajectory.getGtuId(), new TrajectoryAcceptList());
                }
                trajectoryAcceptLists.get(trajectory.getGtuId()).addTrajectory(trajectory, trajectoryGroup);
            }
            trajectoryGroupList.add(trajectoryGroup);
        }
        // Step 2) accept per GTU
        Iterator<String> iterator = trajectoryAcceptLists.keySet().iterator();
        while (iterator.hasNext())
        {
            String gtuId = iterator.next();
            TrajectoryAcceptList trajectoryAcceptListCombined = trajectoryAcceptLists.get(gtuId);
            trajectoryAcceptListCombined.acceptAll(); // refuse only if any meta data type refuses
            for (MetaDataType<?> metaDataType : this.metaDataSet.getMetaDataTypes())
            {
                // create safe copy per meta data type, with defaults accepts = false
                TrajectoryAcceptList trajectoryAcceptList = trajectoryAcceptLists.get(gtuId);
                TrajectoryAcceptList trajectoryAcceptListCopy = new TrajectoryAcceptList();
                for (int i = 0; i < trajectoryAcceptList.size(); i++)
                {
                    trajectoryAcceptListCopy.addTrajectory(trajectoryAcceptList.getTrajectory(i),
                            trajectoryAcceptList.getTrajectoryGroup(i));
                }
                // request meta data type to accept or reject
                ((MetaDataType<T>) metaDataType).accept(trajectoryAcceptListCopy,
                        (Set<T>) new HashSet<>(this.metaDataSet.get(metaDataType)));
                // combine acceptance/rejection of meta data type so far
                for (int i = 0; i < trajectoryAcceptListCopy.size(); i++)
                {
                    Trajectory trajectory = trajectoryAcceptListCopy.getTrajectory(i);
                    trajectoryAcceptListCombined.acceptTrajectory(trajectory,
                            trajectoryAcceptListCombined.isAccepted(trajectory)
                                    && trajectoryAcceptListCopy.isAccepted(trajectory));
                }
            }
        }
        // Step 3) filter trajectories
        List<TrajectoryGroup> out = new ArrayList<>();
        for (TrajectoryGroup full : trajectoryGroupList)
        {
            TrajectoryGroup filtered = new TrajectoryGroup(full.getStartTime(), full.getLaneDirection());
            for (Trajectory trajectory : full.getTrajectories())
            {
                String gtuId = trajectory.getGtuId();
                if (trajectoryAcceptLists.get(gtuId).isAccepted(trajectory))
                {
                    filtered.addTrajectory(trajectory);
                }
            }
            out.add(filtered);
        }
        return out;
    }

    /**
     * @return sampling.
     */
    public Sampler getSampler()
    {
        return this.sampler;
    }

    /** {@inheritDoc} */
    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.description == null) ? 0 : this.description.hashCode());
        result = prime * result + ((this.interval == null) ? 0 : this.interval.hashCode());
        result = prime * result + ((this.metaDataSet == null) ? 0 : this.metaDataSet.hashCode());
        result = prime * result + ((this.sampler == null) ? 0 : this.sampler.hashCode());
        result = prime * result + ((this.spaceTimeRegions == null) ? 0 : this.spaceTimeRegions.hashCode());
        result = prime * result + ((this.id == null) ? 0 : this.id.hashCode());
        result = prime * result + ((this.updateFrequency == null) ? 0 : this.updateFrequency.hashCode());
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
        Query other = (Query) obj;
        if (this.description == null)
        {
            if (other.description != null)
            {
                return false;
            }
        }
        else if (!this.description.equals(other.description))
        {
            return false;
        }
        if (this.interval == null)
        {
            if (other.interval != null)
            {
                return false;
            }
        }
        else if (!this.interval.equals(other.interval))
        {
            return false;
        }
        if (this.metaDataSet == null)
        {
            if (other.metaDataSet != null)
            {
                return false;
            }
        }
        else if (!this.metaDataSet.equals(other.metaDataSet))
        {
            return false;
        }
        if (this.sampler == null)
        {
            if (other.sampler != null)
            {
                return false;
            }
        }
        else if (!this.sampler.equals(other.sampler))
        {
            return false;
        }
        if (this.spaceTimeRegions == null)
        {
            if (other.spaceTimeRegions != null)
            {
                return false;
            }
        }
        else if (!this.spaceTimeRegions.equals(other.spaceTimeRegions))
        {
            return false;
        }
        if (this.id == null)
        {
            if (other.id != null)
            {
                return false;
            }
        }
        else if (!this.id.equals(other.id))
        {
            return false;
        }
        if (this.updateFrequency == null)
        {
            if (other.updateFrequency != null)
            {
                return false;
            }
        }
        else if (!this.updateFrequency.equals(other.updateFrequency))
        {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Query (" + this.description + ")";
    }

}
