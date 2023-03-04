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

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Duration;
import org.djunits.value.vdouble.scalar.Frequency;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.ImmutableIterator;
import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.kpi.interfaces.GtuData;
import org.opentrafficsim.kpi.interfaces.LaneData;
import org.opentrafficsim.kpi.interfaces.LinkData;
import org.opentrafficsim.kpi.sampling.meta.FilterDataSet;
import org.opentrafficsim.kpi.sampling.meta.FilterDataType;

/**
 * A query defines which subset of trajectory information should be included. This is in terms of space-time regions, and in
 * terms of filter data of trajectories, e.g. only include trajectories of trucks.
 * <p>
 * Copyright (c) 2013-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 * @param <G> gtu data type
 * @param <L> lane data type
 */
public final class Query<G extends GtuData, L extends LaneData> implements Identifiable
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
     * @param sampler Sampler&lt;G, L&gt;; sampler
     * @param id String; id
     * @param description String; description
     * @param filterDataSet FilterDataSet; filter data
     * @throws NullPointerException if sampling, description or filterDataSet is null
     */
    public Query(final Sampler<G, L> sampler, final String id, final String description, final FilterDataSet filterDataSet)
    {
        this(sampler, description, filterDataSet, null, null);
    }

    /**
     * Constructor with time interval.
     * @param sampler Sampler&lt;G, L&gt;; sampler
     * @param id String; id
     * @param description String; description
     * @param filterDataSet FilterDataSet; filter data
     * @param interval Duration; interval to gather statistics over
     * @throws NullPointerException if sampling, description or filterDataSet is null
     */
    public Query(final Sampler<G, L> sampler, final String id, final String description, final FilterDataSet filterDataSet,
            final Duration interval)
    {
        this(sampler, id, description, filterDataSet, null, interval);
    }

    /**
     * Constructor with update frequency.
     * @param sampler Sampler&lt;G, L&gt;; sampler
     * @param id String; id
     * @param description String; description
     * @param filterDataSet FilterDataSet; filter data
     * @param updateFrequency Frequency; update frequency
     * @throws NullPointerException if sampling, description or filterDataSet is null
     */
    public Query(final Sampler<G, L> sampler, final String id, final String description, final FilterDataSet filterDataSet,
            final Frequency updateFrequency)
    {
        this(sampler, id, description, filterDataSet, updateFrequency, null);
    }

    /**
     * Constructor with time interval and update frequency.
     * @param sampler Sampler&lt;G, L&gt;; sampler
     * @param id String; id
     * @param description String; description
     * @param filterDataSet FilterDataSet; filter data
     * @param updateFrequency Frequency; update frequency
     * @param interval Duration; interval to gather statistics over
     * @throws NullPointerException if sampling, description or filterDataSet is null
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

    /**
     * Constructor without id.
     * @param sampler Sampler&lt;G, L&gt;; sampler
     * @param description String; description
     * @param filterDataSet FilterDataSet; filter data
     * @throws NullPointerException if sampling, description or filterDataSet is null
     */
    public Query(final Sampler<G, L> sampler, final String description, final FilterDataSet filterDataSet)
    {
        this(sampler, null, description, filterDataSet, null, null);
    }

    /**
     * Constructor without id, with time interval.
     * @param sampler Sampler&lt;G, L&gt;; sampler
     * @param description String; description
     * @param filterDataSet filterDataSet; filter data
     * @param interval Duration; interval to gather statistics over
     * @throws NullPointerException if sampling, description or filterDataSet is null
     */
    public Query(final Sampler<G, L> sampler, final String description, final FilterDataSet filterDataSet,
            final Duration interval)
    {
        this(sampler, null, description, filterDataSet, null, interval);
    }

    /**
     * Constructor without id, with time update frequency.
     * @param sampler Sampler&lt;G, L&gt;; sampler
     * @param description String; description
     * @param filterDataSet filterDataSet; filter data
     * @param updateFrequency Frequency; update frequency
     * @throws NullPointerException if sampling, description or filterDataSet is null
     */
    public Query(final Sampler<G, L> sampler, final String description, final FilterDataSet filterDataSet,
            final Frequency updateFrequency)
    {
        this(sampler, null, description, filterDataSet, updateFrequency, null);
    }

    /**
     * Constructor without id, with time interval and update frequency.
     * @param sampler Sampler&lt;G, L&gt;; sampler
     * @param description String; description
     * @param filterDataSet filterDataSet; filter data
     * @param updateFrequency Frequency; update frequency
     * @param interval Duration; interval to gather statistics over
     * @throws NullPointerException if sampling, description or filterDataSet is null
     */
    public Query(final Sampler<G, L> sampler, final String description, final FilterDataSet filterDataSet,
            final Frequency updateFrequency, final Duration interval)
    {
        this(sampler, null, description, filterDataSet, updateFrequency, interval);
    }

    /**
     * Returns the unique id for the query.
     * @return String; the unique id for the query
     */
    @Override
    public String getId()
    {
        return this.id.toString();
    }

    /**
     * Returns the description.
     * @return String; description
     */
    public String getDescription()
    {
        return this.description;
    }

    /**
     * Returns the update frequency.
     * @return Frequency; updateFrequency.
     */
    public Frequency getUpdateFrequency()
    {
        return this.updateFrequency;
    }

    /**
     * Returns the time interval.
     * @return Duration; interval.
     */
    public Duration getInterval()
    {
        return this.interval;
    }

    /**
     * Returns the number of filter datas.
     * @return int; number of filter data entries
     */
    public int filterSize()
    {
        return this.filterDataSet.size();
    }

    /**
     * Returns an iterator over the filter datas and the related data sets.
     * @return Iterator&lt;Entry&lt;FilterDataType&lt;?&gt;, Set&lt;?&gt;&gt;&gt;; iterator over filter data entries, removal is
     *         not allowed
     */
    public Iterator<Entry<FilterDataType<?>, Set<?>>> getFilterDataSetIterator()
    {
        return this.filterDataSet.getFilterDataSetIterator();
    }

    /**
     * Defines a region in space and time for which this query is valid. All lanes in the link are included.
     * @param link LinkData&lt;? extends L&gt;; link
     * @param startPosition Length; start position
     * @param endPosition Length; end position
     * @param startTime Time; start time
     * @param endTime Time; end time
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
        for (L lane : link.getLaneDatas())
        {
            Length x0 = new Length(lane.getLength().si * startPosition.si / link.getLength().si, LengthUnit.SI);
            Length x1 = new Length(lane.getLength().si * endPosition.si / link.getLength().si, LengthUnit.SI);
            addSpaceTimeRegion(lane, x0, x1, startTime, endTime);
        }
    }

    /**
     * Defines a region in space and time for which this query is valid.
     * @param lane L; lane
     * @param startPosition Length; start position
     * @param endPosition Length; end position
     * @param startTime Time; start time
     * @param endTime Time; end time
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
     * @return int; number of space-time regions
     */
    public int spaceTimeRegionSize()
    {
        return this.spaceTimeRegions.size();
    }

    /**
     * Returns an iterator over the space-time regions.
     * @return Iterator&lt;SpaceTimeRegion&lt;? extends L&gt;&gt;; iterator over space-time regions, removal is not allowed
     */
    public Iterator<SpaceTimeRegion<? extends L>> getSpaceTimeIterator()
    {
        return new ImmutableIterator<>(this.spaceTimeRegions.iterator());
    }

    /**
     * Returns a list of TrajectoryGroups in accordance with the query. Each {@code TrajectoryGroup} contains {@code Trajectory}
     * objects pertaining to a {@code SpaceTimeRegion} from the query. A {@code Trajectory} is only included if all the filter
     * data of this query accepts the trajectory. This method uses {@code Time.ZERO} as start.
     * @param endTime Time; end time of interval to get trajectory groups for
     * @param <T> underlying class of filter data type and its value
     * @return List&lt;TrajectoryGroup&lt;G&gt;&gt;; list of trajectory groups in accordance with the query
     */
    public <T> List<TrajectoryGroup<G>> getTrajectoryGroups(final Time endTime)
    {
        return getTrajectoryGroups(Time.ZERO, endTime);
    }

    /**
     * Returns a list of TrajectoryGroups in accordance with the query. Each {@code TrajectoryGroup} contains {@code Trajectory}
     * objects pertaining to a {@code SpaceTimeRegion} from the query. A {@code Trajectory} is only included if all the filter
     * data of this query accepts the trajectory.
     * @param startTime Time; start time of interval to get trajectory groups for
     * @param endTime Time; start time of interval to get trajectory groups for
     * @param <T> underlying class of filter data type and its value
     * @return List&lt;TrajectoryGroup&lt;G&gt;&gt;; list of trajectory groups in accordance with the query
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
            Time start = startTime.gt(spaceTimeRegion.getStartTime()) ? startTime : spaceTimeRegion.getStartTime();
            Time end = endTime.lt(spaceTimeRegion.getEndTime()) ? endTime : spaceTimeRegion.getEndTime();
            TrajectoryGroup<G> trajectoryGroup;
            if (this.sampler.getSamplerData().getTrajectoryGroup(spaceTimeRegion.getLane()) == null)
            {
                trajectoryGroup = new TrajectoryGroup<>(start, spaceTimeRegion.getLane());
            }
            else
            {
                trajectoryGroup = this.sampler.getSamplerData().getTrajectoryGroup(spaceTimeRegion.getLane())
                        .getTrajectoryGroup(spaceTimeRegion.getStartPosition(), spaceTimeRegion.getEndPosition(), start, end);
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
            TrajectoryAcceptList trajectoryAcceptListCombined = copyTrajectoryAcceptList(trajectoryAcceptLists.get(gtuId));
            trajectoryAcceptListCombined.acceptAll(); // refuse only if any filter data type refuses
            for (FilterDataType<?> filterDataType : this.filterDataSet.getMetaDataTypes())
            {
                // create safe copy per filter data type, with defaults accepts = false
                TrajectoryAcceptList trajectoryAcceptListCopy = copyTrajectoryAcceptList(trajectoryAcceptLists.get(gtuId));
                // request filter data type to accept or reject
                ((FilterDataType<T>) filterDataType).accept(trajectoryAcceptListCopy,
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
     * Returns a copy of the trajectory accept list, with all assumed not accepted.
     * @param trajectoryAcceptList TrajectoryAcceptList; trajectory accept list to copy.
     * @return TrajectoryAcceptList; copy of the trajectory accept list, with all assumed not accepted.
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
     * @return Sampler&lt;G, L&gt;; sampler.
     */
    public Sampler<G, L> getSampler()
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
        result = prime * result + ((this.filterDataSet == null) ? 0 : this.filterDataSet.hashCode());
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
        Query<?, ?> other = (Query<?, ?>) obj;
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
        if (this.filterDataSet == null)
        {
            if (other.filterDataSet != null)
            {
                return false;
            }
        }
        else if (!this.filterDataSet.equals(other.filterDataSet))
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
