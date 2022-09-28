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
import org.opentrafficsim.kpi.interfaces.GtuDataInterface;
import org.opentrafficsim.kpi.interfaces.LaneDataInterface;
import org.opentrafficsim.kpi.interfaces.LinkDataInterface;
import org.opentrafficsim.kpi.sampling.meta.FilterDataSet;
import org.opentrafficsim.kpi.sampling.meta.FilterDataType;

/**
 * A query defines which subset of trajectory information should be included. This is in terms of space-time regions, and in
 * terms of meta data of trajectories, e.g. only include trajectories of trucks.
 * <p>
 * Copyright (c) 2013-2022 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Sep 21, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 * @param <G> gtu data type
 */
public final class Query<G extends GtuDataInterface> implements Identifiable
{
    /** unique id. */
    private final String id;

    /** Sampling. */
    private final Sampler<G> sampler;

    /** Description. */
    private final String description;

    /** Meta data set. */
    private final FilterDataSet filterDataSet;

    /** Update frequency. */
    private final Frequency updateFrequency;

    /** Interval to gather statistics over. */
    private final Duration interval;

    /** List of space-time regions of this query. */
    private final List<SpaceTimeRegion> spaceTimeRegions = new ArrayList<>();

    /**
     * @param sampler Sampler&lt;G&gt;; sampler
     * @param id String; id
     * @param description String; description
     * @param filterDataSet filterDataSet; meta data
     * @throws NullPointerException if sampling, description or filterDataSet is null
     */
    public Query(final Sampler<G> sampler, final String id, final String description, final FilterDataSet filterDataSet)
    {
        this(sampler, description, filterDataSet, null, null);
    }

    /**
     * @param sampler Sampler&lt;G&gt;; sampler
     * @param id String; id
     * @param description String; description
     * @param filterDataSet filterDataSet; meta data
     * @param interval Duration; interval to gather statistics over
     * @throws NullPointerException if sampling, description or filterDataSet is null
     */
    public Query(final Sampler<G> sampler, final String id, final String description, final FilterDataSet filterDataSet,
            final Duration interval)
    {
        this(sampler, id, description, filterDataSet, null, interval);
    }

    /**
     * @param sampler Sampler&lt;G&gt;; sampler
     * @param id String; id
     * @param description String; description
     * @param filterDataSet filterDataSet; meta data
     * @param updateFrequency Frequency; update frequency
     * @throws NullPointerException if sampling, description or filterDataSet is null
     */
    public Query(final Sampler<G> sampler, final String id, final String description, final FilterDataSet filterDataSet,
            final Frequency updateFrequency)
    {
        this(sampler, id, description, filterDataSet, updateFrequency, null);
    }

    /**
     * Constructor. The filter data types must be registered with the sampler.
     * @param sampler Sampler&lt;G&gt;; sampler
     * @param id String; id
     * @param description String; description
     * @param filterDataSet filterDataSet; filter data
     * @param updateFrequency Frequency; update frequency
     * @param interval Duration; interval to gather statistics over
     * @throws NullPointerException if sampling, description or filterDataSet is null
     */
    public Query(final Sampler<G> sampler, final String id, final String description, final FilterDataSet filterDataSet,
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
     * Constructor. The filter data types must be registered with the sampler.
     * @param sampler Sampler&lt;G&gt;; sampler
     * @param description String; description
     * @param filterDataSet filterDataSet; filter data
     * @throws NullPointerException if sampling, description or filterDataSet is null
     */
    public Query(final Sampler<G> sampler, final String description, final FilterDataSet filterDataSet)
    {
        this(sampler, null, description, filterDataSet, null, null);
    }

    /**
     * Constructor. The filter data types must be registered with the sampler.
     * @param sampler Sampler&lt;G&gt;; sampler
     * @param description String; description
     * @param filterDataSet filterDataSet; filter data
     * @param interval Duration; interval to gather statistics over
     * @throws NullPointerException if sampling, description or filterDataSet is null
     */
    public Query(final Sampler<G> sampler, final String description, final FilterDataSet filterDataSet, final Duration interval)
    {
        this(sampler, null, description, filterDataSet, null, interval);
    }

    /**
     * Constructor. The filter data types must be registered with the sampler.
     * @param sampler Sampler&lt;G&gt;; sampler
     * @param description String; description
     * @param filterDataSet filterDataSet; filter data
     * @param updateFrequency Frequency; update frequency
     * @throws NullPointerException if sampling, description or filterDataSet is null
     */
    public Query(final Sampler<G> sampler, final String description, final FilterDataSet filterDataSet,
            final Frequency updateFrequency)
    {
        this(sampler, null, description, filterDataSet, updateFrequency, null);
    }

    /**
     * Constructor. The filter data types must be registered with the sampler.
     * @param sampler Sampler&lt;G&gt;; sampler
     * @param description String; description
     * @param filterDataSet filterDataSet; filter data
     * @param updateFrequency Frequency; update frequency
     * @param interval Duration; interval to gather statistics over
     * @throws NullPointerException if sampling, description or filterDataSet is null
     */
    public Query(final Sampler<G> sampler, final String description, final FilterDataSet filterDataSet,
            final Frequency updateFrequency, final Duration interval)
    {
        this(sampler, null, description, filterDataSet, updateFrequency, interval);
    }

    /**
     * return the unique id for the query.
     * @return String; the unique id for the query
     */
    @Override
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
    public int metaFilterSize()
    {
        return this.filterDataSet.size();
    }

    /**
     * @return iterator over filter data entries, removal is not allowed
     */
    public Iterator<Entry<FilterDataType<?>, Set<?>>> getFilterDataSetIterator()
    {
        return this.filterDataSet.getFilterDataSetIterator();
    }

    /**
     * Defines a region in space and time for which this query is valid. All lanes in the link are included.
     * @param link LinkDataInterface; link
     * @param direction KpiGtuDirectionality; direction
     * @param startPosition Length; start position
     * @param endPosition Length; end position
     * @param startTime Time; start time
     * @param endTime Time; end time
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
     * @param laneDirection KpiLaneDirection; lane direction
     * @param startPosition Length; start position
     * @param endPosition Length; end position
     * @param startTime Time; start time
     * @param endTime Time; end time
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
     * @param endTime Time; end time of interval to get trajectory groups for
     * @param <T> underlying class of meta data type and its value
     * @return list of trajectory groups in accordance with the query
     */
    public <T> List<TrajectoryGroup<G>> getTrajectoryGroups(final Time endTime)
    {
        return getTrajectoryGroups(Time.ZERO, endTime);
    }

    /**
     * Returns a list of TrajectoryGroups in accordance with the query. Each {@code TrajectoryGroup} contains {@code Trajectory}
     * objects pertaining to a {@code SpaceTimeRegion} from the query. A {@code Trajectory} is only included if all the meta
     * data of this query accepts the trajectory.
     * @param startTime Time; start time of interval to get trajectory groups for
     * @param endTime Time; start time of interval to get trajectory groups for
     * @param <T> underlying class of meta data type and its value
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
        for (SpaceTimeRegion spaceTimeRegion : this.spaceTimeRegions)
        {
            Time start = startTime.gt(spaceTimeRegion.getStartTime()) ? startTime : spaceTimeRegion.getStartTime();
            Time end = endTime.lt(spaceTimeRegion.getEndTime()) ? endTime : spaceTimeRegion.getEndTime();
            TrajectoryGroup<G> trajectoryGroup;
            if (this.sampler.getSamplerData().getTrajectoryGroup(spaceTimeRegion.getLaneDirection()) == null)
            {
                trajectoryGroup = new TrajectoryGroup<>(start, spaceTimeRegion.getLaneDirection());
            }
            else
            {
                trajectoryGroup = this.sampler.getSamplerData().getTrajectoryGroup(spaceTimeRegion.getLaneDirection())
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
            TrajectoryAcceptList trajectoryAcceptListCombined = trajectoryAcceptLists.get(gtuId);
            trajectoryAcceptListCombined.acceptAll(); // refuse only if any meta data type refuses
            for (FilterDataType<?> metaDataType : this.filterDataSet.getMetaDataTypes())
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
                ((FilterDataType<T>) metaDataType).accept(trajectoryAcceptListCopy,
                        (Set<T>) new LinkedHashSet<>(this.filterDataSet.get(metaDataType)));
                // combine acceptance/rejection of meta data type so far
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
            TrajectoryGroup<G> filtered = new TrajectoryGroup<>(full.getStartTime(), full.getLaneDirection());
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
     * @return sampling.
     */
    public Sampler<?> getSampler()
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
        Query<?> other = (Query<?>) obj;
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
