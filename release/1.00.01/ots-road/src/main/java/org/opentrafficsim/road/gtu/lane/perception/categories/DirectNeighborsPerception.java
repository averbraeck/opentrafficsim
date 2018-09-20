package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.Collection;
import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.Try;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.DownstreamNeighborsIterable;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LaneStructureRecord;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.UpstreamNeighborsIterable;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;

import nl.tudelft.simulation.language.Throw;

/**
 * Perception of surrounding traffic on the own road, i.e. without crossing traffic.
 * <p>
 * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 22, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class DirectNeighborsPerception extends LaneBasedAbstractPerceptionCategory implements NeighborsPerception
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** Look ahead parameter type. */
    protected static final ParameterTypeLength LOOKAHEAD = ParameterTypes.LOOKAHEAD;

    /** Look back parameter type. */
    protected static final ParameterTypeLength LOOKBACK = ParameterTypes.LOOKBACK;

    /** Set of followers per relative lane. */
    private final Map<RelativeLane, TimeStampedObject<PerceptionCollectable<HeadwayGTU, LaneBasedGTU>>> followers =
            new HashMap<>();

    /** Set of leaders per relative lane. */
    private final Map<RelativeLane, TimeStampedObject<PerceptionCollectable<HeadwayGTU, LaneBasedGTU>>> leaders =
            new HashMap<>();

    /** Set of first followers per lane upstream of merge per lateral direction, i.e. in the left or right lane. */
    private final Map<LateralDirectionality, TimeStampedObject<SortedSet<HeadwayGTU>>> firstFollowers = new HashMap<>();

    /** Set of first leaders per lane downstream of split per lateral direction, i.e. in the left or right lane. */
    private final Map<LateralDirectionality, TimeStampedObject<SortedSet<HeadwayGTU>>> firstLeaders = new HashMap<>();

    /** Whether a GTU is alongside per lateral direction, i.e. in the left or right lane. */
    private final Map<LateralDirectionality, TimeStampedObject<Boolean>> gtuAlongside = new HashMap<>();

    /** Headway GTU type that should be used. */
    private final HeadwayGtuType headwayGtuType;

    /**
     * @param perception perception
     * @param headwayGtuType type of headway gtu to generate
     */
    public DirectNeighborsPerception(final LanePerception perception, final HeadwayGtuType headwayGtuType)
    {
        super(perception);
        this.headwayGtuType = headwayGtuType;
    }

    /** {@inheritDoc} */
    @Override
    public final void updateAll() throws GTUException, NetworkException, ParameterException
    {
        this.firstLeaders.clear();
        this.firstFollowers.clear();
        this.gtuAlongside.clear();
        if (getPerception().getLaneStructure().getExtendedCrossSection().contains(RelativeLane.LEFT))
        {
            updateFirstLeaders(LateralDirectionality.LEFT);
            updateFirstFollowers(LateralDirectionality.LEFT);
            updateGtuAlongside(LateralDirectionality.LEFT);
        }
        if (getPerception().getLaneStructure().getExtendedCrossSection().contains(RelativeLane.RIGHT))
        {
            updateFirstLeaders(LateralDirectionality.RIGHT);
            updateFirstFollowers(LateralDirectionality.RIGHT);
            updateGtuAlongside(LateralDirectionality.RIGHT);
        }
        this.leaders.clear();
        this.followers.clear();
        for (RelativeLane lane : getPerception().getLaneStructure().getExtendedCrossSection())
        {
            updateLeaders(lane);
            updateFollowers(lane);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void updateFirstLeaders(final LateralDirectionality lat)
            throws ParameterException, GTUException, NetworkException
    {
        checkLateralDirectionality(lat);
        SortedSet<HeadwayGTU> headwaySet =
                new SortedNeighborsSet(getFirstDownstreamGTUs(lat, RelativePosition.FRONT, RelativePosition.REAR),
                        this.headwayGtuType, getGtu(), true);
        this.firstLeaders.put(lat, new TimeStampedObject<>(headwaySet, getTimestamp()));
    }

    /** {@inheritDoc} */
    @Override
    public final void updateFirstFollowers(final LateralDirectionality lat)
            throws GTUException, ParameterException, NetworkException
    {
        checkLateralDirectionality(lat);
        SortedSet<HeadwayGTU> headwaySet = new SortedNeighborsSet(
                getFirstUpstreamGTUs(lat, RelativePosition.REAR, RelativePosition.FRONT), this.headwayGtuType, getGtu(), false);
        this.firstFollowers.put(lat, new TimeStampedObject<>(headwaySet, getTimestamp()));
    }

    /** {@inheritDoc} */
    @Override
    public final void updateGtuAlongside(final LateralDirectionality lat) throws GTUException, ParameterException
    {

        checkLateralDirectionality(lat);
        // check if any GTU is downstream of the rear, within the vehicle length
        SortedSet<DistanceGTU> headwaySet = getFirstDownstreamGTUs(lat, RelativePosition.REAR, RelativePosition.FRONT);
        if (!headwaySet.isEmpty() && headwaySet.first().getDistance().le0())
        {
            this.gtuAlongside.put(lat, new TimeStampedObject<>(true, getTimestamp()));
            return;
        }
        // check if any GTU is upstream of the front, within the vehicle length
        headwaySet = getFirstUpstreamGTUs(lat, RelativePosition.FRONT, RelativePosition.REAR);
        if (!headwaySet.isEmpty() && headwaySet.first().getDistance().le0())
        {
            this.gtuAlongside.put(lat, new TimeStampedObject<>(true, getTimestamp()));
            return;
        }
        // no such GTU
        this.gtuAlongside.put(lat, new TimeStampedObject<>(false, getTimestamp()));

    }

    /**
     * Returns a set of first leaders per branch, relative to given relative position. Helper method to find first leaders and
     * GTU's alongside.
     * @param lat LEFT or RIGHT
     * @param egoRelativePosition position of GTU to start search from
     * @param otherRelativePosition position of other GTU
     * @return set of first leaders per branch
     * @throws GTUException if the GTU was not initialized
     * @throws ParameterException if a parameter was not present or out of bounds
     */
    private SortedSet<DistanceGTU> getFirstDownstreamGTUs(final LateralDirectionality lat,
            final RelativePosition.TYPE egoRelativePosition, final RelativePosition.TYPE otherRelativePosition)
            throws GTUException, ParameterException
    {
        SortedSet<DistanceGTU> headwaySet = new TreeSet<>();
        Set<LaneStructureRecord> currentSet = new LinkedHashSet<>();
        Set<LaneStructureRecord> nextSet = new LinkedHashSet<>();
        LaneStructureRecord record = getPerception().getLaneStructure().getFirstRecord(new RelativeLane(lat, 1));
        Length dxSearch = getGtu().getRelativePositions().get(egoRelativePosition).getDx();
        Length dxHeadway = getGtu().getFront().getDx();
        branchUpstream(record, dxSearch, currentSet);
        // move downstream over branches as long as no vehicles are found
        while (!currentSet.isEmpty())
        {
            Iterator<LaneStructureRecord> iterator = currentSet.iterator();
            while (iterator.hasNext())
            {
                record = iterator.next();
                /*-
                 *                                             _ _ _ ______________________ _ _ _
                 *                                                             _|___    |
                 * find any vehicle downstream of this point on lane A |      |__o__| A |
                 *                                             _ _ _ ___________|_______|__ _ _ _ 
                 *                                                     (--------) negative distance
                 */
                LaneBasedGTU down = record.getLane().getGtuAhead(record.getStartDistance().neg().plus(dxSearch),
                        record.getDirection(), otherRelativePosition, getTimestamp());
                if (down != null)
                {
                    // GTU found, add to set
                    // headwaySet.add(this.headwayGtuType.createHeadwayGtu(getGtu(), down,
                    // record.getStartDistance().plus(down.position(record.getLane(), down.getRear())).minus(dxHeadway),
                    // true));
                    headwaySet.add(new DistanceGTU(down,
                            record.getStartDistance().plus(down.position(record.getLane(), down.getRear())).minus(dxHeadway)));
                }
                else
                {
                    // no GTU found, search on next lanes in next loop and maintain cumulative length
                    for (LaneStructureRecord next : record.getNext())
                    {
                        nextSet.add(next);
                    }
                }
            }
            currentSet = nextSet;
            nextSet = new LinkedHashSet<>();
        }
        return headwaySet;
    }

    /**
     * Returns a set of lanes to start from for a downstream search, upstream of the reference lane if the tail is before this
     * lane.
     * @param record start record
     * @param dx distance between reference point and point to search from
     * @param set set of lanes that is recursively built up, starting with the reference record
     */
    private void branchUpstream(final LaneStructureRecord record, final Length dx, final Set<LaneStructureRecord> set)
    {
        Length pos = record.getStartDistance().neg().minus(dx);
        if (pos.lt0() && !record.getPrev().isEmpty())
        {
            for (LaneStructureRecord prev : record.getPrev())
            {
                branchUpstream(prev, dx, set);
            }
        }
        else
        {
            set.add(record);
        }
    }

    /**
     * Returns a set of first followers per branch, relative to given relative position. Helper method to find first followers
     * and GTU's alongside.
     * @param lat LEFT or RIGHT
     * @param egoRelativePosition position of GTU to start search from
     * @param otherRelativePosition position of other GTU
     * @return set of first followers per branch
     * @throws GTUException if the GTU was not initialized
     * @throws ParameterException if a parameter was not present or out of bounds
     */
    private SortedSet<DistanceGTU> getFirstUpstreamGTUs(final LateralDirectionality lat,
            final RelativePosition.TYPE egoRelativePosition, final RelativePosition.TYPE otherRelativePosition)
            throws GTUException, ParameterException
    {
        SortedSet<DistanceGTU> headwaySet = new TreeSet<>();
        Set<LaneStructureRecord> currentSet = new LinkedHashSet<>();
        Set<LaneStructureRecord> prevSet = new LinkedHashSet<>();
        LaneStructureRecord record = getPerception().getLaneStructure().getFirstRecord(new RelativeLane(lat, 1));
        Length dxSearch = getGtu().getRelativePositions().get(egoRelativePosition).getDx();
        Length dxHeadway = getGtu().getRear().getDx();
        branchDownstream(record, dxSearch, currentSet);
        // move upstream over branches as long as no vehicles are found
        while (!currentSet.isEmpty())
        {
            Iterator<LaneStructureRecord> iterator = currentSet.iterator();
            while (iterator.hasNext())
            {
                record = iterator.next();
                /*-
                 * _ _ _ ______________________ _ _ _ 
                 *         |    ___|_   
                 *         | A |__o__|      | find any upstream of this point on lane A
                 * _ _ _ __|_______|___________ _ _ _ 
                 *         (----------------) distance
                 */
                LaneBasedGTU up = record.getLane().getGtuBehind(record.getStartDistance().neg().plus(dxSearch),
                        record.getDirection(), otherRelativePosition, getTimestamp());
                if (up != null)
                {
                    // GTU found, add to set
                    // headwaySet.add(this.headwayGtuType.createHeadwayGtu(getGtu(), up,
                    // record.getStartDistance().neg().minus(up.position(record.getLane(), up.getFront())).plus(dxHeadway),
                    // false));
                    headwaySet.add(new DistanceGTU(up, record.getStartDistance().neg()
                            .minus(up.position(record.getLane(), up.getFront())).plus(dxHeadway)));
                }
                else
                {
                    // no GTU found, search on next lanes in next loop and maintain cumulative length
                    for (LaneStructureRecord prev : record.getPrev())
                    {
                        prevSet.add(prev);
                    }
                }
            }
            currentSet = prevSet;
            prevSet = new LinkedHashSet<>();
        }
        return headwaySet;
    }

    /**
     * Returns a set of lanes to start from for an upstream search, downstream of the reference lane if the front is after this
     * lane.
     * @param record start record
     * @param dx distance between reference point and point to search from
     * @param set set of lanes that is recursively built up, starting with the reference record
     */
    private void branchDownstream(final LaneStructureRecord record, final Length dx, final Set<LaneStructureRecord> set)
    {
        Length pos = record.getStartDistance().neg().plus(dx);
        if (pos.gt(record.getLane().getLength()))
        {
            for (LaneStructureRecord next : record.getNext())
            {
                branchDownstream(next, dx, set);
            }
        }
        else
        {
            set.add(record);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final void updateLeaders(final RelativeLane lane) throws ParameterException, GTUException, NetworkException
    {
        Throw.whenNull(lane, "Lane may not be null.");
        LaneStructureRecord record = getPerception().getLaneStructure().getFirstRecord(lane);
        Length pos = record.getStartDistance().neg();
        pos = record.getDirection().isPlus() ? pos.plus(getGtu().getFront().getDx()) : pos.minus(getGtu().getFront().getDx());
        boolean ignoreIfUpstream = true;
        PerceptionCollectable<HeadwayGTU, LaneBasedGTU> it = new DownstreamNeighborsIterable(getGtu(), record,
                Length.max(Length.ZERO, pos), getGtu().getParameters().getParameter(LOOKAHEAD), getGtu().getFront(),
                this.headwayGtuType, getGtu(), lane, ignoreIfUpstream);
        this.leaders.put(lane, new TimeStampedObject<>(it, getTimestamp()));
    }

    /** {@inheritDoc} */
    @Override
    public final void updateFollowers(final RelativeLane lane) throws GTUException, NetworkException, ParameterException
    {
        Throw.whenNull(lane, "Lane may not be null.");
        LaneStructureRecord record = getPerception().getLaneStructure().getFirstRecord(lane);
        Length pos = record.getStartDistance().neg();
        pos = record.getDirection().isPlus() ? pos.plus(getGtu().getFront().getDx()) : pos.minus(getGtu().getFront().getDx());
        PerceptionCollectable<HeadwayGTU, LaneBasedGTU> it =
                new UpstreamNeighborsIterable(getGtu(), record, Length.max(Length.ZERO, pos),
                        getGtu().getParameters().getParameter(LOOKBACK), getGtu().getRear(), this.headwayGtuType, lane);
        this.followers.put(lane, new TimeStampedObject<>(it, getTimestamp()));
    }

    /** {@inheritDoc} */
    @Override
    public final SortedSet<HeadwayGTU> getFirstLeaders(final LateralDirectionality lat)
            throws ParameterException, NullPointerException, IllegalArgumentException
    {
        checkLateralDirectionality(lat);
        return this.firstLeaders.get(lat).getObject();
    }

    /** {@inheritDoc} */
    @Override
    public final SortedSet<HeadwayGTU> getFirstFollowers(final LateralDirectionality lat)
            throws ParameterException, NullPointerException, IllegalArgumentException
    {
        checkLateralDirectionality(lat);
        return getObjectOrNull(this.firstFollowers.get(lat));
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isGtuAlongside(final LateralDirectionality lat)
            throws ParameterException, NullPointerException, IllegalArgumentException
    {
        checkLateralDirectionality(lat);
        return getObjectOrNull(this.gtuAlongside.get(lat));
    }

    /** {@inheritDoc} */
    @Override
    public final PerceptionCollectable<HeadwayGTU, LaneBasedGTU> getLeaders(final RelativeLane lane)
    {
        return getObjectOrNull(this.leaders.get(lane));
    }

    /** {@inheritDoc} */
    @Override
    public final PerceptionCollectable<HeadwayGTU, LaneBasedGTU> getFollowers(final RelativeLane lane)
    {
        return getObjectOrNull(this.followers.get(lane));
    }

    /**
     * Set of leaders on a lane, which is usually 0 or 1, but possibly more in case of a downstream split with no intermediate
     * GTU. This is shown below. Suppose A needs to go straight. If A considers a lane change to the left, both GTUs B (who's
     * tail ~ is still on the straight lane) and C need to be considered for whether it's safe to do so. In case of multiple
     * splits close to one another, the returned set may contain even more than 2 leaders. Leaders are sorted by headway value.
     * 
     * <pre>
     *          | |
     * _________/B/_____
     * _ _?_ _ _~_ _C_ _
     * _ _A_ _ _ _ _ _ _
     * _________________
     * </pre>
     * 
     * @param lat LEFT or RIGHT
     * @return list of followers on a lane
     * @throws ParameterException if parameter is not defined
     * @throws NullPointerException if {@code lat} is {@code null}
     * @throws IllegalArgumentException if {@code lat} is {@code NONE}
     */
    public final TimeStampedObject<SortedSet<HeadwayGTU>> getTimeStampedFirstLeaders(final LateralDirectionality lat)
            throws ParameterException, NullPointerException, IllegalArgumentException
    {
        checkLateralDirectionality(lat);
        return this.firstLeaders.get(lat);
    }

    /**
     * Set of followers on a lane, which is usually 0 or 1, but possibly more in case of an upstream merge with no intermediate
     * GTU. This is shown below. If A considers a lane change to the left, both GTUs B and C need to be considered for whether
     * it's safe to do so. In case of multiple merges close to one another, the returned set may contain even more than 2
     * followers. Followers are sorted by tailway value.
     * 
     * <pre>
     *        | |
     *        |C| 
     * ________\ \______
     * _ _B_|_ _ _ _ _?_
     * _ _ _|_ _ _ _ _A_ 
     * _____|___________
     * </pre>
     * 
     * @param lat LEFT or RIGHT
     * @return list of followers on a lane
     * @throws ParameterException if parameter is not defined
     * @throws NullPointerException if {@code lat} is {@code null}
     * @throws IllegalArgumentException if {@code lat} is {@code NONE}
     */
    public final TimeStampedObject<SortedSet<HeadwayGTU>> getTimeStampedFirstFollowers(final LateralDirectionality lat)
            throws ParameterException, NullPointerException, IllegalArgumentException
    {
        checkLateralDirectionality(lat);
        return this.firstFollowers.get(lat);
    }

    /**
     * Whether there is a GTU alongside, i.e. with overlap, in an adjacent lane.
     * @param lat LEFT or RIGHT
     * @return whether there is a GTU alongside, i.e. with overlap, in an adjacent lane
     * @throws ParameterException if parameter is not defined
     * @throws NullPointerException if {@code lat} is {@code null}
     * @throws IllegalArgumentException if {@code lat} is {@code NONE}
     */
    public final TimeStampedObject<Boolean> isGtuAlongsideTimeStamped(final LateralDirectionality lat)
            throws ParameterException, NullPointerException, IllegalArgumentException
    {
        checkLateralDirectionality(lat);
        return this.gtuAlongside.get(lat);
    }

    /**
     * Set of leaders on a lane, including adjacent GTU's who's FRONT is ahead of the own vehicle FRONT. Leaders are sorted by
     * headway value.
     * @param lane relative lateral lane
     * @return set of leaders on a lane, including adjacent GTU's who's FRONT is ahead of the own vehicle FRONT
     */
    public final TimeStampedObject<PerceptionCollectable<HeadwayGTU, LaneBasedGTU>> getTimeStampedLeaders(
            final RelativeLane lane)
    {
        return this.leaders.get(lane);
    }

    /**
     * Set of followers on a lane, including adjacent GTU's who's REAR is back of the own vehicle REAR. Follower are are sorted
     * by tailway value.
     * @param lane relative lateral lane
     * @return set of followers on a lane, including adjacent GTU's who's REAR is back of the own vehicle REAR
     */
    public final TimeStampedObject<PerceptionCollectable<HeadwayGTU, LaneBasedGTU>> getTimeStampedFollowers(
            final RelativeLane lane)
    {
        return this.followers.get(lane);
    }

    /**
     * Checks that lateral directionality is either left or right and an existing lane.
     * @param lat LEFT or RIGHT
     * @throws ParameterException if parameter is not defined
     * @throws NullPointerException if {@code lat} is {@code null}
     * @throws IllegalArgumentException if {@code lat} is {@code NONE}
     */
    private void checkLateralDirectionality(final LateralDirectionality lat)
            throws ParameterException, NullPointerException, IllegalArgumentException
    {
        // TODO not use this check when synchronizing or cooperating
        Throw.whenNull(lat, "Lateral directionality may not be null.");
        Throw.when(lat.equals(LateralDirectionality.NONE), IllegalArgumentException.class,
                "Lateral directionality may not be NONE.");
        Throw.when(
                (lat.equals(LateralDirectionality.LEFT)
                        && !getPerception().getLaneStructure().getExtendedCrossSection().contains(RelativeLane.LEFT))
                        || (lat.equals(LateralDirectionality.RIGHT)
                                && !getPerception().getLaneStructure().getExtendedCrossSection().contains(RelativeLane.RIGHT)),
                IllegalArgumentException.class, "Lateral directionality may only point to an existing adjacent lane.");
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DirectNeighborsPesrception";
    }

    /**
     * GTU at a distance, as preliminary info towards perceiving it. For instance, as a set from a search algorithm.
     * <p>
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 22 apr. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private class DistanceGTU implements Comparable<DistanceGTU>
    {

        /** GTU. */
        private LaneBasedGTU gtu;

        /** Distance. */
        private Length distance;

        /**
         * Constructor.
         * @param gtu LaneBasedGTU; GTU
         * @param distance Length; distance
         */
        DistanceGTU(final LaneBasedGTU gtu, final Length distance)
        {
            this.gtu = gtu;
            this.distance = distance;
        }

        /**
         * Returns the GTU.
         * @return LaneBasedGTU; GTU
         */
        public LaneBasedGTU getGTU()
        {
            return this.gtu;
        }

        /**
         * Returns the distance.
         * @return Length; distance
         */
        public Length getDistance()
        {
            return this.distance;
        }

        /** {@inheritDoc} */
        @Override
        public int compareTo(final DistanceGTU o)
        {
            return this.distance.compareTo(o.distance);
        }
    }

    /**
     * Translation from a set of {@code DistanceGTU}'s, to a sorted set of {@code HeadwayGTU}'s. This bridges the gap between a
     * raw network search, and the perceived result.
     * <p>
     * Copyright (c) 2013-2018 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
     * <p>
     * @version $Revision$, $LastChangedDate$, by $Author$, initial version 22 apr. 2018 <br>
     * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
     * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
     * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
     */
    private static class SortedNeighborsSet implements SortedSet<HeadwayGTU>
    {

        /** Base set of GTU's at distance. */
        private final SortedSet<DistanceGTU> base;

        /** Headway type for perceived GTU's. */
        private final HeadwayGtuType headwayGtuType;

        /** Perceiving GTU. */
        private final LaneBasedGTU perceivingGtu;

        /** Whether the GTU's are downstream. */
        private final boolean downstream;

        /** Contains all GTU's preceived so far, to prevent re-perception. */
        private final SortedMap<String, HeadwayGTU> all = new TreeMap<>();

        /**
         * Constructor.
         * @param base SortedSet; base set of GTU's at distance
         * @param headwayGtuType HeadwayGtuType; headway type for perceived GTU's
         * @param perceivingGtu LaneBasedGTU; perceiving GTU
         * @param downstream boolean; whether the GTU's are downstream
         */
        SortedNeighborsSet(final SortedSet<DistanceGTU> base, final HeadwayGtuType headwayGtuType,
                final LaneBasedGTU perceivingGtu, final boolean downstream)
        {
            this.base = base;
            this.headwayGtuType = headwayGtuType;
            this.perceivingGtu = perceivingGtu;
            this.downstream = downstream;
        }

        /** {@inheritDoc} */
        @Override
        public int size()
        {
            return this.base.size();
        }

        /** {@inheritDoc} */
        @Override
        public boolean isEmpty()
        {
            return this.base.isEmpty();
        }

        /**
         * Make sure all GTU are available in perceived for. Helper method.
         */
        private void getAll()
        {
            Iterator<HeadwayGTU> it = iterator();
            while (it.hasNext())
            {
                @SuppressWarnings("unused")
                HeadwayGTU gtu = it.next(); // iterator creates all HeadwayGTU's
            }
        }

        /** {@inheritDoc} */
        @Override
        public boolean contains(final Object o)
        {
            getAll();
            return this.all.containsValue(o);
        }

        /** {@inheritDoc} */
        @Override
        public Iterator<HeadwayGTU> iterator()
        {
            return new Iterator<HeadwayGTU>()
            {
                @SuppressWarnings("synthetic-access")
                private Iterator<DistanceGTU> it = SortedNeighborsSet.this.base.iterator();

                @Override
                public boolean hasNext()
                {
                    return this.it.hasNext();
                }

                @SuppressWarnings("synthetic-access")
                @Override
                public HeadwayGTU next()
                {
                    DistanceGTU next = this.it.next();
                    if (next == null)
                    {
                        throw new ConcurrentModificationException();
                    }
                    HeadwayGTU out = SortedNeighborsSet.this.all.get(next.getGTU().getId());
                    if (out == null)
                    {
                        out = Try.assign(() -> SortedNeighborsSet.this.headwayGtuType.createHeadwayGtu(
                                SortedNeighborsSet.this.perceivingGtu, next.getGTU(), next.getDistance(),
                                SortedNeighborsSet.this.downstream), "Exception while perceiving a neighbor.");
                        SortedNeighborsSet.this.all.put(next.getGTU().getId(), out);
                    }
                    return out;
                }
            };
        }

        /** {@inheritDoc} */
        @Override
        public Object[] toArray()
        {
            getAll();
            return this.all.values().toArray();
        }

        /** {@inheritDoc} */
        @Override
        public <T> T[] toArray(final T[] a)
        {
            getAll();
            return this.all.values().toArray(a);
        }

        /** {@inheritDoc} */
        @Override
        public boolean add(final HeadwayGTU e)
        {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public boolean remove(final Object o)
        {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public boolean containsAll(final Collection<?> c)
        {
            getAll();
            return this.all.values().containsAll(c);
        }

        /** {@inheritDoc} */
        @Override
        public boolean addAll(final Collection<? extends HeadwayGTU> c)
        {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public boolean retainAll(final Collection<?> c)
        {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public boolean removeAll(final Collection<?> c)
        {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public void clear()
        {
            throw new UnsupportedOperationException();
        }

        /** {@inheritDoc} */
        @Override
        public Comparator<? super HeadwayGTU> comparator()
        {
            return null;
        }

        /**
         * Helper method for sub-lists to find distance-GTU from the perceived GTU.
         * @param element HeadwayGTU; perceived GTU
         * @return DistanceGTU; pertaining to given GTU
         */
        private DistanceGTU getGTU(final HeadwayGTU element)
        {
            for (DistanceGTU distanceGtu : this.base)
            {
                if (distanceGtu.getGTU().getId().equals(element.getId()))
                {
                    return distanceGtu;
                }
            }
            throw new IllegalArgumentException("GTU used to obtain a subset is not in the set.");
        }

        /** {@inheritDoc} */
        @Override
        public SortedSet<HeadwayGTU> subSet(final HeadwayGTU fromElement, final HeadwayGTU toElement)
        {
            return new SortedNeighborsSet(this.base.subSet(getGTU(fromElement), getGTU(toElement)), this.headwayGtuType,
                    this.perceivingGtu, this.downstream);
        }

        /** {@inheritDoc} */
        @Override
        public SortedSet<HeadwayGTU> headSet(final HeadwayGTU toElement)
        {
            return new SortedNeighborsSet(this.base.headSet(getGTU(toElement)), this.headwayGtuType, this.perceivingGtu,
                    this.downstream);
        }

        /** {@inheritDoc} */
        @Override
        public SortedSet<HeadwayGTU> tailSet(final HeadwayGTU fromElement)
        {
            return new SortedNeighborsSet(this.base.tailSet(getGTU(fromElement)), this.headwayGtuType, this.perceivingGtu,
                    this.downstream);
        }

        /** {@inheritDoc} */
        @Override
        public HeadwayGTU first()
        {
            return iterator().next();
        }

        /** {@inheritDoc} */
        @Override
        public HeadwayGTU last()
        {
            getAll();
            return this.all.get(this.all.lastKey());
        }

    }

}
