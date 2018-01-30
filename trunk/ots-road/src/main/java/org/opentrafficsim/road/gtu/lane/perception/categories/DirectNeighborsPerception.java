package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LaneStructureRecord;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;

import nl.tudelft.simulation.language.Throw;

/**
 * Perception of surrounding traffic on the own road, i.e. without crossing traffic.
 * <p>
 * Copyright (c) 2013-2017 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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

    /** Set of followers per relative lane. */
    private final Map<RelativeLane, TimeStampedObject<SortedSet<HeadwayGTU>>> followers = new HashMap<>();

    /** Set of leaders per relative lane. */
    private final Map<RelativeLane, TimeStampedObject<SortedSet<HeadwayGTU>>> leaders = new HashMap<>();

    /** Set of first followers per lane upstream of merge per lateral direction, i.e. in the left or right lane. */
    private final Map<LateralDirectionality, TimeStampedObject<SortedSet<HeadwayGTU>>> firstFollowers = new HashMap<>();

    /** Set of first leaders per lane downstream of split per lateral direction, i.e. in the left or right lane. */
    private final Map<LateralDirectionality, TimeStampedObject<SortedSet<HeadwayGTU>>> firstLeaders = new HashMap<>();

    /** Whether a GTU is alongside per lateral direction, i.e. in the left or right lane. */
    private final Map<LateralDirectionality, TimeStampedObject<Boolean>> gtuAlongside = new HashMap<>();

    /** Headway GTU type that should be used. */
    private final HeadwayGtuType headwayGtuType;

    /**
     * Margin used for neighbor search in some cases to prevent possible deadlock. This does not affect calculated distances to
     * neighbors, but only whether they are considered a leader or follower.
     */
    private static final Length MARGIN = new Length(0.000001, LengthUnit.SI);

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
        if (getPerception().getLaneStructure().getCrossSection().contains(RelativeLane.LEFT))
        {
            updateFirstLeaders(LateralDirectionality.LEFT);
            updateFirstFollowers(LateralDirectionality.LEFT);
            updateGtuAlongside(LateralDirectionality.LEFT);
        }
        if (getPerception().getLaneStructure().getCrossSection().contains(RelativeLane.RIGHT))
        {
            updateFirstLeaders(LateralDirectionality.RIGHT);
            updateFirstFollowers(LateralDirectionality.RIGHT);
            updateGtuAlongside(LateralDirectionality.RIGHT);
        }
        this.leaders.clear();
        this.followers.clear();
        for (RelativeLane lane : getPerception().getLaneStructure().getCrossSection())
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
        SortedSet<HeadwayGTU> headwaySet = getFirstDownstreamGTUs(lat, RelativePosition.FRONT, RelativePosition.REAR);
        this.firstLeaders.put(lat, new TimeStampedObject<>(headwaySet, getTimestamp()));
    }

    /** {@inheritDoc} */
    @Override
    public final void updateFirstFollowers(final LateralDirectionality lat)
            throws GTUException, ParameterException, NetworkException
    {
        checkLateralDirectionality(lat);
        SortedSet<HeadwayGTU> headwaySet = getFirstUpstreamGTUs(lat, RelativePosition.REAR, RelativePosition.FRONT);
        this.firstFollowers.put(lat, new TimeStampedObject<>(headwaySet, getTimestamp()));
    }

    /** {@inheritDoc} */
    @Override
    public final void updateGtuAlongside(final LateralDirectionality lat) throws GTUException, ParameterException
    {

        checkLateralDirectionality(lat);
        // check if any GTU is downstream of the rear, within the vehicle length
        SortedSet<HeadwayGTU> headwaySet = getFirstDownstreamGTUs(lat, RelativePosition.REAR, RelativePosition.FRONT);
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
    private SortedSet<HeadwayGTU> getFirstDownstreamGTUs(final LateralDirectionality lat,
            final RelativePosition.TYPE egoRelativePosition, final RelativePosition.TYPE otherRelativePosition)
            throws GTUException, ParameterException
    {
        SortedSet<HeadwayGTU> headwaySet = new TreeSet<>();
        Set<LaneStructureRecord> currentSet = new LinkedHashSet<>();
        Set<LaneStructureRecord> nextSet = new LinkedHashSet<>();
        LaneStructureRecord record = getPerception().getLaneStructure().getLaneLSR(new RelativeLane(lat, 1));
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
                    headwaySet.add(this.headwayGtuType.createHeadwayGtu(down,
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
    private SortedSet<HeadwayGTU> getFirstUpstreamGTUs(final LateralDirectionality lat,
            final RelativePosition.TYPE egoRelativePosition, final RelativePosition.TYPE otherRelativePosition)
            throws GTUException, ParameterException
    {
        SortedSet<HeadwayGTU> headwaySet = new TreeSet<>();
        Set<LaneStructureRecord> currentSet = new LinkedHashSet<>();
        Set<LaneStructureRecord> prevSet = new LinkedHashSet<>();
        LaneStructureRecord record = getPerception().getLaneStructure().getLaneLSR(new RelativeLane(lat, 1));
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
                    headwaySet.add(this.headwayGtuType.createHeadwayGtu(up, record.getStartDistance().neg()
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
        SortedSet<HeadwayGTU> headwaySet = new TreeSet<>();
        Set<LaneStructureRecord> currentSet = new LinkedHashSet<>();
        Set<LaneStructureRecord> nextSet = new LinkedHashSet<>();
        LaneStructureRecord initRecord = getPerception().getLaneStructure().getLaneLSR(lane);
        currentSet.add(initRecord);
        double lookahead = getGtu().getParameters().getParameter(ParameterTypes.LOOKAHEAD).si;
        double ds = getGtu().getFront().getDx().si - getGtu().getReference().getDx().si;
        // move downstream over branches
        while (!currentSet.isEmpty())
        {
            for (LaneStructureRecord record : currentSet)
            {
                int first;
                double loc = -record.getStartDistance().si + ds;
                boolean plus = record.getDirection().isPlus();
                if (lane.getLateralDirectionality().isLeft())
                {
                    loc = plus ? loc - MARGIN.si : loc + MARGIN.si;
                }
                else if (lane.getLateralDirectionality().isRight())
                {
                    loc = plus ? loc + MARGIN.si : loc - MARGIN.si;
                }
                LaneBasedGTU down = record.getLane().getGtuAhead(Length.createSI(loc), record.getDirection(),
                        RelativePosition.FRONT, getTimestamp());
                if (down == null)
                {
                    first = plus ? record.getLane().numberOfGtus() : -1; // none
                }
                else
                {
                    first = record.getLane().indexOfGtu(down); // from first downstream till last
                }
                // loop GTU's and create HeadwayGTU's
                int di = plus ? 1 : -1;
                for (int i = first; i < record.getLane().numberOfGtus() & i >= 0; i += di)
                {
                    LaneBasedGTU gtu = record.getLane().getGtu(i);
                    double position = gtu.position(record.getLane(), gtu.getRear()).si;
                    boolean rearOnUpstreamLane = plus ? position < 0.0 : position > record.getLane().getLength().si;
                    if (rearOnUpstreamLane && !record.equals(initRecord))
                    {
                        // rear still on previous lane; it is found there
                        continue;
                    }
                    double distance =
                            record.getStartDistance().si + (plus ? position : record.getLane().getLength().si - position) - ds;
                    // only within lookahead
                    if (distance <= lookahead && !gtu.equals(getGtu()))
                    {
                        // TODO remove this fix to ignore on-ramp block
                        if (!gtu.getId().equals("999999"))
                        {
                            headwaySet.add(this.headwayGtuType.createHeadwayGtu(gtu, Length.createSI(distance)));
                        }
                    }
                }
                // add next lanes
                for (LaneStructureRecord next : record.getNext())
                {
                    nextSet.add(next);
                }
                // TODO break search, but how to guarantee that the rear of further GTU's is not within lookahead?
            }
            currentSet = nextSet;
            nextSet = new LinkedHashSet<>();
        }
        this.leaders.put(lane, new TimeStampedObject<>(headwaySet, getTimestamp()));

    }

    /** {@inheritDoc} */
    @Override
    public final void updateFollowers(final RelativeLane lane) throws GTUException, NetworkException, ParameterException
    {
        Throw.whenNull(lane, "Lane may not be null.");
        SortedSet<HeadwayGTU> headwaySet = new TreeSet<>();
        Set<LaneStructureRecord> currentSet = new LinkedHashSet<>();
        Set<LaneStructureRecord> prevSet = new LinkedHashSet<>();
        LaneStructureRecord initRecord = getPerception().getLaneStructure().getLaneLSR(lane);
        currentSet.add(initRecord);
        double lookback = getGtu().getParameters().getParameter(ParameterTypes.LOOKBACK).si;
        double dsFront = getGtu().getFront().getDx().si - getGtu().getReference().getDx().si;
        double dsRear = getGtu().getRear().getDx().si - getGtu().getReference().getDx().si;
        // move upstream over branches
        while (!currentSet.isEmpty())
        {
            for (LaneStructureRecord record : currentSet)
            {
                int first;
                double loc = -record.getStartDistance().si + dsFront;
                boolean plus = record.getDirection().isPlus();
                if (lane.getLateralDirectionality().isLeft())
                {
                    loc = record.getDirection().isPlus() ? loc + MARGIN.si : loc - MARGIN.si;
                }
                else if (lane.getLateralDirectionality().isRight())
                {
                    loc = record.getDirection().isPlus() ? loc - MARGIN.si : loc + MARGIN.si;
                }
                LaneBasedGTU up = record.getLane().getGtuBehind(Length.createSI(loc), record.getDirection(),
                        RelativePosition.FRONT, getTimestamp());
                if (up == null)
                {
                    first = plus ? -1 : record.getLane().numberOfGtus(); // none
                }
                else
                {
                    first = record.getLane().indexOfGtu(up); // from first upstream till last
                }
                // loop GTU's and create HeadwayGTU's
                int di = plus ? -1 : 1;
                for (int i = first; i < record.getLane().numberOfGtus() & i >= 0; i += di)
                {
                    LaneBasedGTU gtu = record.getLane().getGtu(i);
                    double distance =
                            -record.getStartDistance().si - gtu.position(record.getLane(), gtu.getFront()).si + dsRear;
                    // only within lookback, and ignore self
                    if (distance <= lookback && !gtu.getId().equals(getGtu().getId()))
                    {
                        headwaySet.add(this.headwayGtuType.createHeadwayGtu(gtu, Length.createSI(distance)));
                    }
                }
                // add prev lanes
                for (LaneStructureRecord prev : record.getPrev())
                {
                    prevSet.add(prev);
                }
                // TODO break search, but how to guarantee that the front of further GTU's is not within lookback?
            }
            currentSet = prevSet;
            prevSet = new LinkedHashSet<>();
        }
        this.followers.put(lane, new TimeStampedObject<>(headwaySet, getTimestamp()));

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
    public final SortedSet<HeadwayGTU> getLeaders(final RelativeLane lane)
    {
        return getObjectOrNull(this.leaders.get(lane));
    }

    /** {@inheritDoc} */
    @Override
    public final SortedSet<HeadwayGTU> getFollowers(final RelativeLane lane)
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
    public final TimeStampedObject<SortedSet<HeadwayGTU>> getTimeStampedLeaders(final RelativeLane lane)
    {
        return this.leaders.get(lane);
    }

    /**
     * Set of followers on a lane, including adjacent GTU's who's REAR is back of the own vehicle REAR. Follower are are sorted
     * by tailway value.
     * @param lane relative lateral lane
     * @return set of followers on a lane, including adjacent GTU's who's REAR is back of the own vehicle REAR
     */
    public final TimeStampedObject<SortedSet<HeadwayGTU>> getTimeStampedFollowers(final RelativeLane lane)
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
                        && !getPerception().getLaneStructure().getCrossSection().contains(RelativeLane.LEFT))
                        || (lat.equals(LateralDirectionality.RIGHT)
                                && !getPerception().getLaneStructure().getCrossSection().contains(RelativeLane.RIGHT)),
                IllegalArgumentException.class, "Lateral directionality may only point to an existing adjacent lane.");
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DirectNeighborsPerception";
    }

}
