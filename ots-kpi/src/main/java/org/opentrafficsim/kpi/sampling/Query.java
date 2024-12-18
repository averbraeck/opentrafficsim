package org.opentrafficsim.kpi.sampling;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.UUID;

import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.base.Identifiable;
import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.ImmutableIterator;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.interfaces.LaneData;
import org.opentrafficsim.kpi.interfaces.LinkData;
import org.opentrafficsim.kpi.sampling.filter.FilterDataSet;
import org.opentrafficsim.kpi.sampling.filter.FilterDataType;

/**
 * A query defines which subset of trajectory information should be included. This is in terms of space-time regions, and in
 * terms of filter data of trajectories, e.g. only include trajectories of trucks.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <G> GTU data type
 * @param <L> lane data type
 */
public final class Query<G extends GtuData, L extends LaneData<L>> implements Identifiable
{

    /** unique id. */
    private final String id;

    /** Sampling. */
    private final Sampler<G, L> sampler;

    /** Description. */
    private final String description;

    /** Filter data set. */
    private final FilterDataSet filterDataSet;

    /** Update frequency. */
    private final Frequency updateFrequency;

    /** Interval to gather statistics over. */
    private final Duration interval;

    /** List of space-time regions of this query. */
    private final List<SpaceTimeRegion<? extends L>> spaceTimeRegions = new ArrayList<>();

    /**
     * Constructor.
     * @param sampler sampler
     * @param id id
     * @param description description
     * @param filterDataSet filter data
     * @throws NullPointerException if sampling, description or filterDataSet is null
     */
    public Query(final Sampler<G, L> sampler, final String id, final String description, final FilterDataSet filterDataSet)
    {
        this(sampler, id, description, filterDataSet, null, null);
    }

    /**
     * Constructor with time interval and update frequency.
     * @param sampler sampler
     * @param id id, may be {@code null}
     * @param description description
     * @param filterDataSet filter data
     * @param updateFrequency update frequency, used by external controller, may be {@code null}
     * @param interval interval to gather statistics over, used by external controller, may be {@code null}
     * @throws NullPointerException if sampling, description or filterDataSet is {@code null}
     */
    public Query(final Sampler<G, L> sampler, final String id, final String description, final FilterDataSet filterDataSet,
            final Frequency updateFrequency, final Duration interval)
    {
        Throw.whenNull(sampler, "Sampling may not be null.");
        Throw.whenNull(description, "Description may not be null.");
        Throw.whenNull(filterDataSet, "Meta data may not be null.");
        this.sampler = sampler;
        this.filterDataSet = new FilterDataSet(filterDataSet);
        this.id = id == null ? UUID.randomUUID().toString() : id;
        this.description = description;
        this.updateFrequency = updateFrequency;
        this.interval = interval;
    }

    @Override
    public String getId()
    {
        return this.id.toString();
    }

    /**
     * Returns the description.
     * @return description
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * Returns the update frequency.
     * @return updateFrequency.
     */
    public Frequency getUpdateFrequency()
    {
        return this.updateFrequency;
    }

    /**
     * Returns the time interval.
     * @return interval.
     */
    public Duration getInterval()
    {
        return this.interval;
    }

    /**
     * Returns the number of filter datas.
     * @return number of filter data entries
     */
    public int filterSize()
    {
        return this.filterDataSet.size();
    }

    /**
     * Returns an iterator over the filter datas and the related data sets.
     * @return iterator over filter data entries, removal is not allowed
     */
    public Iterator<Entry<FilterDataType<?, ?>, Set<?>>> getFilterDataSetIterator()
    {
        return this.filterDataSet.getFilterDataSetIterator();
    }

    /**
     * Defines a region in space and time for which this query is valid. All lanes in the link are included.
     * @param link link
     * @param startPosition start position
     * @param endPosition end position
     * @param startTime start time
     * @param endTime end time
     */
    public void addSpaceTimeRegionLink(final LinkData<? extends L> link, final Length startPosition, final Length endPosition,
            final Time startTime, final Time endTime)
    {
        Throw.whenNull(link, "Link may not be null.");
        Throw.whenNull(startPosition, "Start position may not be null.");
        Throw.whenNull(endPosition, "End position may not be null.");
        Throw.whenNull(startTime, "Start time may not be null.");
        Throw.whenNull(endTime, "End time may not be null.");
        Throw.when(endPosition.lt(startPosition), IllegalArgumentException.class,
                "End position should be greater than start position.");
        Throw.when(endTime.lt(startTime), IllegalArgumentException.class, "End time should be greater than start time.");
        double fStart = startPosition.si / link.getLength().si;
        double fEnd = endPosition.si / link.getLength().si;
        for (L lane : link.getLanes())
        {
            Length x0 = lane.getLength().times(fStart);
            Length x1 = lane.getLength().times(fEnd);
            addSpaceTimeRegion(lane, x0, x1, startTime, endTime);
        }
    }

    /**
     * Defines a region in space and time for which this query is valid.
     * @param lane lane
     * @param startPosition start position
     * @param endPosition end position
     * @param startTime start time
     * @param endTime end time
     */
    public void addSpaceTimeRegion(final L lane, final Length startPosition, final Length endPosition, final Time startTime,
            final Time endTime)
    {
        Throw.whenNull(lane, "Lane direction may not be null.");
        Throw.whenNull(startPosition, "Start position may not be null.");
        Throw.whenNull(endPosition, "End position may not be null.");
        Throw.whenNull(startTime, "Start time may not be null.");
        Throw.whenNull(endTime, "End time may not be null.");
        Throw.when(endPosition.lt(startPosition), IllegalArgumentException.class,
                "End position should be greater than start position.");
        Throw.when(endTime.lt(startTime), IllegalArgumentException.class, "End time should be greater than start time.");
        SpaceTimeRegion<L> spaceTimeRegion = new SpaceTimeRegion<>(lane, startPosition, endPosition, startTime, endTime);
        this.sampler.registerSpaceTimeRegion(spaceTimeRegion);
        this.spaceTimeRegions.add(spaceTimeRegion);
    }

    /**
     * Returns the number of space-time regions.
     * @return number of space-time regions
     */
    public int spaceTimeRegionSize()
    {
        return this.spaceTimeRegions.size();
    }

    /**
     * Returns an iterator over the space-time regions.
     * @return iterator over space-time regions, removal is not allowed
     */
    public Iterator<SpaceTimeRegion<? extends L>> getSpaceTimeIterator()
    {
        return new ImmutableIterator<>(this.spaceTimeRegions.iterator());
    }

    /**
     * Returns a list of TrajectoryGroups in accordance with the query. Each {@code TrajectoryGroup} contains {@code Trajectory}
     * objects pertaining to a {@code SpaceTimeRegion} from the query. A {@code Trajectory} is only included if all the filter
     * data of this query accepts the trajectory. This method uses {@code Time.ZERO} as start.
     * @param endTime end time of interval to get trajectory groups for
     * @param <T> underlying class of filter data type and its value
     * @return list of trajectory groups in accordance with the query
     */
    public <T> List<TrajectoryGroup<G>> getTrajectoryGroups(final Time endTime)
    {
        return getTrajectoryGroups(Time.ZERO, endTime);
    }

    /**
     * Returns a list of TrajectoryGroups in accordance with the query. Each {@code TrajectoryGroup} contains {@code Trajectory}
     * objects pertaining to a {@code SpaceTimeRegion} from the query. A {@code Trajectory} is only included if all the filter
     * data of this query accepts the trajectory.
     * @param startTime start time of interval to get trajectory groups for
     * @param endTime start time of interval to get trajectory groups for
     * @param <T> underlying class of filter data type and its value
     * @return list of trajectory groups in accordance with the query
     */
    @SuppressWarnings("unchecked")
    public <T> List<TrajectoryGroup<G>> getTrajectoryGroups(final Time startTime, final Time endTime)
    {
        Throw.whenNull(startTime, "Start t may not be null.");
        Throw.whenNull(endTime, "End t may not be null.");
        // Step 1) gather trajectories per GTU, truncated over space and time
        Map<String, TrajectoryAcceptList> trajectoryAcceptLists = new LinkedHashMap<>();
        List<TrajectoryGroup<G>> trajectoryGroupList = new ArrayList<>();
        for (SpaceTimeRegion<? extends L> spaceTimeRegion : this.spaceTimeRegions)
        {
            Time start = startTime.gt(spaceTimeRegion.startTime()) ? startTime : spaceTimeRegion.startTime();
            Time end = endTime.lt(spaceTimeRegion.endTime()) ? endTime : spaceTimeRegion.endTime();
            TrajectoryGroup<G> trajectoryGroup;
            if (this.sampler.getSamplerData().getTrajectoryGroup(spaceTimeRegion.lane()) == null)
            {
                trajectoryGroup = new TrajectoryGroup<>(start, spaceTimeRegion.lane());
            }
            else
            {
                trajectoryGroup = this.sampler.getSamplerData().getTrajectoryGroup(spaceTimeRegion.lane())
                        .getTrajectoryGroup(spaceTimeRegion.startPosition(), spaceTimeRegion.endPosition(), start, end);
            }
            for (Trajectory<G> trajectory : trajectoryGroup.getTrajectories())
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
            trajectoryAcceptListCombined.acceptAll(); // refuse only if any filter data type refuses
            for (FilterDataType<?, ?> filterDataType : this.filterDataSet.getFilterDataTypes())
            {
                // create safe copy per filter data type, with defaults accepts = false
                TrajectoryAcceptList trajectoryAcceptListCopy = copyTrajectoryAcceptList(trajectoryAcceptLists.get(gtuId));
                // request filter data type to accept or reject
                ((FilterDataType<T, ?>) filterDataType).accept(trajectoryAcceptListCopy,
                        (Set<T>) new LinkedHashSet<>(this.filterDataSet.get(filterDataType)));
                // combine acceptance/rejection of filter data types so far
                for (int i = 0; i < trajectoryAcceptListCopy.size(); i++)
                {
                    Trajectory<?> trajectory = trajectoryAcceptListCopy.getTrajectory(i);
                    trajectoryAcceptListCombined.acceptTrajectory(trajectory,
                            trajectoryAcceptListCombined.isAccepted(trajectory)
                                    && trajectoryAcceptListCopy.isAccepted(trajectory));
                }
            }
        }
        // Step 3) filter trajectories
        List<TrajectoryGroup<G>> out = new ArrayList<>();
        for (TrajectoryGroup<G> full : trajectoryGroupList)
        {
            TrajectoryGroup<G> filtered = new TrajectoryGroup<>(full.getStartTime(), full.getLane());
            for (Trajectory<G> trajectory : full.getTrajectories())
            {
                String gtuId = trajectory.getGtuId();
                if (trajectory.size() > 0 && trajectoryAcceptLists.get(gtuId).isAccepted(trajectory))
                {
                    filtered.addTrajectory(trajectory);
                }
            }
            out.add(filtered);
        }
        return out;
    }

    /**
     * Returns a copy of the trajectory accept list, with all assumed not accepted.
     * @param trajectoryAcceptList trajectory accept list to copy
     * @return copy of the trajectory accept list, with all assumed not accepted
     */
    private TrajectoryAcceptList copyTrajectoryAcceptList(final TrajectoryAcceptList trajectoryAcceptList)
    {
        TrajectoryAcceptList trajectoryAcceptListCopy = new TrajectoryAcceptList();
        for (int i = 0; i < trajectoryAcceptList.size(); i++)
        {
            trajectoryAcceptListCopy.addTrajectory(trajectoryAcceptList.getTrajectory(i),
                    trajectoryAcceptList.getTrajectoryGroup(i));
        }
        return trajectoryAcceptListCopy;
    }

    /**
     * Returns the sampler.
     * @return sampler.
     */
    public Sampler<G, L> getSampler()
    {
        return this.sampler;
    }

    @Override
    public int hashCode()
    {
        final int prime = 31;
        int result = 1;
        result = prime * result + this.description.hashCode();
        result = prime * result + ((this.interval == null) ? 0 : this.interval.hashCode());
        result = prime * result + this.filterDataSet.hashCode();
        result = prime * result + this.sampler.hashCode();
        result = prime * result + this.spaceTimeRegions.hashCode();
        result = prime * result + this.id.hashCode();
        result = prime * result + ((this.updateFrequency == null) ? 0 : this.updateFrequency.hashCode());
        return result;
    }

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
        Query<?, ?> other = (Query<?, ?>) obj;
        if (!this.description.equals(other.description))
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
        if (!this.filterDataSet.equals(other.filterDataSet))
        {
            return false;
        }
        if (!this.sampler.equals(other.sampler))
        {
            return false;
        }
        if (!this.spaceTimeRegions.equals(other.spaceTimeRegions))
        {
            return false;
        }
        if (!this.id.equals(other.id))
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

    @Override
    public String toString()
    {
        return "Query (" + this.description + ")";
    }

}
