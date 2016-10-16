package org.opentrafficsim.road.gtu.lane.perception.categories;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import org.djunits.unit.LengthUnit;
import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterException;
import org.opentrafficsim.core.gtu.behavioralcharacteristics.ParameterTypes;
import org.opentrafficsim.core.gtu.perception.TimeStampedObject;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LaneStructureRecord;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.headway.AbstractHeadwayGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.Headway;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayDistance;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTUReal;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTUSimple;
import org.opentrafficsim.road.gtu.lane.tactical.AbstractLaneBasedTacticalPlanner;
import org.opentrafficsim.road.gtu.lane.tactical.LanePathInfo;
import org.opentrafficsim.road.network.lane.Lane;
import org.opentrafficsim.road.network.lane.LaneDirection;

import nl.tudelft.simulation.language.Throw;

/**
 * Perception of surrounding traffic on the own road, i.e. without crossing traffic.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/current/license.html">OpenTrafficSim License</a>.
 * <p>
 * @version $Revision$, $LastChangedDate$, by $Author$, initial version Jul 22, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.transport.citg.tudelft.nl">Wouter Schakel</a>
 */
public class NeighborsPerception extends LaneBasedAbstractPerceptionCategory
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** Set of followers per relative lane. */
    private final Map<RelativeLane, TimeStampedObject<SortedSet<AbstractHeadwayGTU>>> followers = new HashMap<>();

    /** Set of leaders per relative lane. */
    private final Map<RelativeLane, TimeStampedObject<SortedSet<AbstractHeadwayGTU>>> leaders = new HashMap<>();

    /** Set of first followers per lane upstream of merge per lateral direction, i.e. in the left or right lane. */
    private final Map<LateralDirectionality, TimeStampedObject<SortedSet<AbstractHeadwayGTU>>> firstFollowers =
        new HashMap<>();

    /** Set of first leaders per lane downstream of split per lateral direction, i.e. in the left or right lane. */
    private final Map<LateralDirectionality, TimeStampedObject<SortedSet<AbstractHeadwayGTU>>> firstLeaders =
        new HashMap<>();

    /** Whether a GTU is alongside per lateral direction, i.e. in the left or right lane. */
    private final Map<LateralDirectionality, TimeStampedObject<Boolean>> gtuAlongside = new HashMap<>();

    /**
     * @param perception perception
     */
    public NeighborsPerception(final LanePerception perception)
    {
        super(perception);
    }

    /** {@inheritDoc} */
    @Override
    public final void updateAll() throws GTUException, NetworkException, ParameterException
    {
        updateLanePathInfo(); // TODO remove this line
        this.firstLeaders.clear();
        this.firstFollowers.clear();
        this.gtuAlongside.clear();
        for (LateralDirectionality lat : new LateralDirectionality[] {LateralDirectionality.LEFT,
            LateralDirectionality.RIGHT})
        {
            updateFirstLeaders(lat);
            updateFirstFollowers(lat);
            updateGtuAlongside(lat);
        }
        this.leaders.clear();
        this.followers.clear();
        // TODO use for-loop with working lane structure instead of this dummy code
        updateLeaders(RelativeLane.CURRENT); // dummy
        updateFollowers(RelativeLane.CURRENT); // dummy
        /*-
        for (RelativeLane lane : getPerception().getLaneStructure().getCrossSection(getTimestamp()))
        {
            updateLeaders(lane);
            updateFollowers(lane);
        }
        */
    }

    /**
     * Update set of leaders on a lane, which is usually 0 or 1, but possibly more in case of a downstream split with no
     * intermediate GTU.
     * @param lat LEFT or RIGHT
     * @throws NetworkException in case of a network exception
     * @throws GTUException if the GTU was not initialized
     * @throws ParameterException if a parameter was not present or out of bounds
     * @throws NullPointerException if {@code lat} is {@code null}
     * @throws IllegalArgumentException if {@code lat} is {@code NONE}
     */
    public final void updateFirstLeaders(final LateralDirectionality lat) throws ParameterException, GTUException,
        NetworkException
    {
        checkLateralDirectionality(lat);

        // TODO remove this if-statement and its contents
        if (true)
        {
            if (lat.equals(LateralDirectionality.NONE))
            {
                updateLeaders(RelativeLane.CURRENT);
                SortedSet<AbstractHeadwayGTU> set = new TreeSet<>();
                if (!getLeaders(RelativeLane.CURRENT).isEmpty())
                {
                    set.add(getLeaders(RelativeLane.CURRENT).first());
                }
                this.firstLeaders.put(lat, new TimeStampedObject<>(set, getTimestamp()));
            }
            else
            {
                this.firstLeaders.put(lat, new TimeStampedObject<>(new TreeSet<>(), getTimestamp()));
            }
            return;
        }

        SortedSet<AbstractHeadwayGTU> headwaySet = getFirstDownstreamGTUs(lat, RelativePosition.FRONT);
        this.firstLeaders.put(lat, new TimeStampedObject<>(headwaySet, getTimestamp()));

    }

    /**
     * Update set of followers on a lane, which is usually 0 or 1, but possibly more in case of an upstream merge with no
     * intermediate GTU.
     * @param lat LEFT or RIGHT
     * @throws NetworkException in case of a network exception
     * @throws GTUException if the GTU was not initialized
     * @throws ParameterException if a parameter was not present or out of bounds
     * @throws NullPointerException if {@code lat} is {@code null}
     * @throws IllegalArgumentException if {@code lat} is {@code NONE}
     */
    public final void updateFirstFollowers(final LateralDirectionality lat) throws GTUException, ParameterException,
        NetworkException
    {
        checkLateralDirectionality(lat);

        // TODO remove this if-statement and its contents
        if (true)
        {
            if (lat.equals(LateralDirectionality.NONE))
            {
                updateFollowers(RelativeLane.CURRENT);
                SortedSet<AbstractHeadwayGTU> set = new TreeSet<>();
                if (!getFollowers(RelativeLane.CURRENT).isEmpty())
                {
                    set.add(getFollowers(RelativeLane.CURRENT).first());
                }
                this.firstFollowers.put(lat, new TimeStampedObject<>(set, getTimestamp()));
            }
            else
            {
                this.firstFollowers.put(lat, new TimeStampedObject<>(new TreeSet<>(), getTimestamp()));
            }
            return;
        }

        SortedSet<AbstractHeadwayGTU> headwaySet = getFirstUpstreamGTUs(lat, RelativePosition.REAR);
        this.firstFollowers.put(lat, new TimeStampedObject<>(headwaySet, getTimestamp()));

    }

    /**
     * Update whether there is a GTU alongside, i.e. with overlap, in an adjacent lane.
     * @param lat LEFT or RIGHT
     * @throws GTUException if the GTU was not initialized
     * @throws ParameterException if parameter is not defined
     * @throws NullPointerException if {@code lat} is {@code null}
     * @throws IllegalArgumentException if {@code lat} is {@code NONE}
     */
    public final void updateGtuAlongside(final LateralDirectionality lat) throws GTUException, ParameterException
    {
        checkLateralDirectionality(lat);

        // TODO remove this if-statement and its contents
        if (true)
        {
            this.gtuAlongside.put(lat, new TimeStampedObject<>(true, getTimestamp()));
            return;
        }

        // check if any GTU is downstream of the rear, within the vehicle length
        SortedSet<AbstractHeadwayGTU> headwaySet = getFirstDownstreamGTUs(lat, RelativePosition.REAR);
        if (!headwaySet.isEmpty() && headwaySet.first().getDistance().le(getGtu().getLength()))
        {
            this.gtuAlongside.put(lat, new TimeStampedObject<>(true, getTimestamp()));
            return;
        }
        // check if any GTU is upstream of the front, within the vehicle length
        headwaySet = getFirstUpstreamGTUs(lat, RelativePosition.FRONT);
        if (!headwaySet.isEmpty() && headwaySet.first().getDistance().le(getGtu().getLength()))
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
     * @param relativePosition position of GTU to start search from
     * @return set of first leaders per branch
     * @throws GTUException if the GTU was not initialized
     * @throws ParameterException if a parameter was not present or out of bounds
     */
    private SortedSet<AbstractHeadwayGTU> getFirstDownstreamGTUs(final LateralDirectionality lat,
        final RelativePosition.TYPE relativePosition) throws GTUException, ParameterException
    {
        SortedSet<AbstractHeadwayGTU> headwaySet = new TreeSet<>();
        Map<LaneStructureRecord, Length> currentSet = new HashMap<>();
        Map<LaneStructureRecord, Length> nextSet = new HashMap<>();
        LaneStructureRecord record = getPerception().getLaneStructure().getLaneLSR(new RelativeLane(lat, 1), getTimestamp());
        double fraction =
            getGtu().fractionalPosition(getPerception().getLaneStructure().getRootLSR().getLane(),
                getGtu().getRelativePositions().get(relativePosition));
        Length pos = record.getLane().getLength().multiplyBy(fraction);
        currentSet.put(record, pos.multiplyBy(-1.0)); // by later adding lane length, we get distance to start of next lane
        // move downstream over branches as long as no vehicles are found
        while (!currentSet.isEmpty())
        {
            Iterator<LaneStructureRecord> iterator = currentSet.keySet().iterator();
            while (iterator.hasNext())
            {
                record = iterator.next();
                // we search downstream of "minus current distance to start of lane" (is pos in first loop)
                // in this way, GTU's are found who's rear is not on the lane of its reference point
                /*-
                 *                                             _ _ _ ______________________ _ _ _
                 *                                                             _|___    |
                 * find any vehicle downstream of this point on lane A |      |__o__| A |
                 *                                             _ _ _ ___________|_______|__ _ _ _ 
                 *                                                     (--------) negative distance
                 */
                LaneBasedGTU down =
                    record.getLane().getGtuAhead(currentSet.get(record).multiplyBy(-1.0), record.getDirection(),
                        RelativePosition.REAR, getTimestamp());
                if (down != null)
                {
                    // GTU found, add to set
                    headwaySet.add(new HeadwayGTUReal(down, currentSet.get(record).plus(
                        down.position(record.getLane(), down.getRear()))));
                }
                else
                {
                    // no GTU found, search on next lanes in next loop and maintain cumulative length
                    Length length = currentSet.get(record).plus(record.getLane().getLength());
                    for (LaneStructureRecord next : record.getNext())
                    {
                        nextSet.put(next, length);
                    }
                }
            }
            currentSet = nextSet;
            nextSet = new HashMap<>();
        }
        return headwaySet;
    }

    /**
     * Returns a set of first followers per branch, relative to given relative position. Helper method to find first followers
     * and GTU's alongside.
     * @param lat LEFT or RIGHT
     * @param relativePosition position of GTU to start search from
     * @return set of first followers per branch
     * @throws GTUException if the GTU was not initialized
     * @throws ParameterException if a parameter was not present or out of bounds
     */
    private SortedSet<AbstractHeadwayGTU> getFirstUpstreamGTUs(final LateralDirectionality lat,
        final RelativePosition.TYPE relativePosition) throws GTUException, ParameterException
    {
        SortedSet<AbstractHeadwayGTU> headwaySet = new TreeSet<>();
        Map<LaneStructureRecord, Length> currentSet = new HashMap<>();
        Map<LaneStructureRecord, Length> prevSet = new HashMap<>();
        LaneStructureRecord record = getPerception().getLaneStructure().getLaneLSR(new RelativeLane(lat, 1), getTimestamp());
        double fraction =
            getGtu().fractionalPosition(getPerception().getLaneStructure().getRootLSR().getLane(),
                getGtu().getRelativePositions().get(relativePosition));
        Length pos = record.getLane().getLength().multiplyBy(fraction);
        currentSet.put(record, pos.minus(record.getLane().getLength())); // adding lane length gets distance to prev's lane end
        // move upstream over branches as long as no vehicles are found
        while (!currentSet.isEmpty())
        {
            Iterator<LaneStructureRecord> iterator = currentSet.keySet().iterator();
            while (iterator.hasNext())
            {
                record = iterator.next();
                // we search upstream of "distance to end of lane + lane length" (is pos in first loop)
                // in this way, GTU's are found who's front is not on the lane of its reference point
                /*-
                 * _ _ _ ______________________ _ _ _ 
                 *         |    ___|_   
                 *         | A |__o__|      | find any upstream of this point on lane A
                 * _ _ _ __|_______|___________ _ _ _ 
                 *         (----------------) distance
                 */
                LaneBasedGTU up =
                    record.getLane().getGtuBehind(currentSet.get(record).plus(record.getLane().getLength()),
                        record.getDirection(), RelativePosition.FRONT, getTimestamp());
                if (up != null)
                {
                    // GTU found, add to set
                    Length distFromEnd = record.getLane().getLength().minus(up.position(record.getLane(), up.getFront()));
                    headwaySet.add(new HeadwayGTUReal(up, currentSet.get(record).plus(distFromEnd)));
                }
                else
                {
                    // no GTU found, search on next lanes in next loop and maintain cumulative length
                    Length length = currentSet.get(record).plus(record.getLane().getLength());
                    for (LaneStructureRecord prev : record.getPrev())
                    {
                        prevSet.put(prev, length);
                    }
                }
            }
            currentSet = prevSet;
            prevSet = new HashMap<>();
        }
        return headwaySet;
    }

    /**
     * Update set of leaders on a lane, including adjacent GTU's who's FRONT is ahead of the own vehicle FRONT.
     * @param lane relative lateral lane
     * @throws NetworkException in case of a network exception
     * @throws GTUException if the GRU was not initialized
     * @throws ParameterException if a parameter was not present or out of bounds
     */
    public final void updateLeaders(final RelativeLane lane) throws ParameterException, GTUException, NetworkException
    {
        Throw.whenNull(lane, "Lane may not be null.");

        // TODO remove this if-statement and its contents
        if (true)
        {
            SortedSet<AbstractHeadwayGTU> set = new TreeSet<>();
            if (lane.equals(RelativeLane.CURRENT))
            {
                if (this.lanePathInfo == null || this.lanePathInfo.getTimestamp().ne(getTimestamp()))
                {
                    updateLanePathInfo();
                }
                Length maximumForwardHeadway =
                    getGtu().getBehavioralCharacteristics().getParameter(ParameterTypes.LOOKAHEAD);
                Headway gtu = forwardHeadway(maximumForwardHeadway);
                if (gtu != null)
                {
                    set.add((AbstractHeadwayGTU) gtu);
                }
            }
            this.leaders.put(lane, new TimeStampedObject<>(set, getTimestamp()));
            return;
        }

        SortedSet<AbstractHeadwayGTU> headwaySet = new TreeSet<>();
        Map<LaneStructureRecord, Length> currentSet = new HashMap<>();
        Map<LaneStructureRecord, Length> nextSet = new HashMap<>();
        LaneStructureRecord initRecord = getPerception().getLaneStructure().getLaneLSR(lane, getTimestamp());
        double fraction =
            getGtu().fractionalPosition(getPerception().getLaneStructure().getRootLSR().getLane(), getGtu().getFront());
        Length pos = initRecord.getLane().getLength().multiplyBy(fraction);
        currentSet.put(initRecord, pos.multiplyBy(-1.0)); // by later adding lane length, we get distance to start of next lane
        Length lookahead = getGtu().getBehavioralCharacteristics().getParameter(ParameterTypes.LOOKAHEAD);
        // move downstream over branches
        while (!currentSet.isEmpty())
        {
            Iterator<LaneStructureRecord> iterator = currentSet.keySet().iterator();
            while (iterator.hasNext())
            {
                LaneStructureRecord record = iterator.next();
                int first;
                LaneBasedGTU down =
                    record.getLane().getGtuAhead(currentSet.get(record).multiplyBy(-1.0), record.getDirection(),
                        RelativePosition.FRONT, getTimestamp());
                if (down == null)
                {
                    first = record.getLane().getGtuList().size(); // none
                }
                else
                {
                    first = record.getLane().getGtuList().indexOf(down); // from first downstream till last
                }
                // loop GTU's and create HeadwayGTU's
                for (int i = first; i < record.getLane().getGtuList().size(); i++)
                {
                    LaneBasedGTU gtu = record.getLane().getGtuList().get(i);
                    Length distance = currentSet.get(record).plus(gtu.position(record.getLane(), gtu.getRear()));
                    // only within lookahead
                    if (distance.le(lookahead))
                    {
                        headwaySet.add(new HeadwayGTUReal(gtu, distance));
                    }
                }
                // add next lanes
                Length length = currentSet.get(record).plus(record.getLane().getLength());
                for (LaneStructureRecord next : record.getNext())
                {
                    nextSet.put(next, length);
                }
                // TODO break search, but how to guarantee that the rear of further GTU's is not within lookahead?
            }
            currentSet = nextSet;
            nextSet = new HashMap<>();
        }
        this.leaders.put(lane, new TimeStampedObject<>(headwaySet, getTimestamp()));

    }

    /**
     * Update set of followers on a lane, including adjacent GTU's who's REAR is back of the own vehicle REAR.
     * @param lane relative lateral lane
     * @throws NetworkException in case of a network exception
     * @throws GTUException if the GRU was not initialized
     * @throws ParameterException if a parameter was not present or out of bounds
     */
    public final void updateFollowers(final RelativeLane lane) throws GTUException, NetworkException, ParameterException
    {
        Throw.whenNull(lane, "Lane may not be null.");

        // TODO remove this if-statement and its contents
        if (true)
        {
            SortedSet<AbstractHeadwayGTU> set = new TreeSet<>();
            if (lane.equals(RelativeLane.CURRENT))
            {
                if (this.lanePathInfo == null || this.lanePathInfo.getTimestamp().ne(getTimestamp()))
                {
                    updateLanePathInfo();
                }
                Length maximumReverseHeadway =
                    getGtu().getBehavioralCharacteristics().getParameter(ParameterTypes.LOOKBACKOLD);
                Headway gtu = backwardHeadway(maximumReverseHeadway);
                if (gtu != null)
                {
                    set.add((AbstractHeadwayGTU) gtu);
                }
            }
            this.followers.put(lane, new TimeStampedObject<>(set, getTimestamp()));
            return;
        }

        SortedSet<AbstractHeadwayGTU> headwaySet = new TreeSet<>();
        Map<LaneStructureRecord, Length> currentSet = new HashMap<>();
        Map<LaneStructureRecord, Length> prevSet = new HashMap<>();
        LaneStructureRecord initRecord = getPerception().getLaneStructure().getLaneLSR(lane, getTimestamp());
        double fraction =
            getGtu().fractionalPosition(getPerception().getLaneStructure().getRootLSR().getLane(), getGtu().getRear());
        Length pos = initRecord.getLane().getLength().multiplyBy(fraction);
        currentSet.put(initRecord, pos.minus(initRecord.getLane().getLength())); // add lane len. gets dist. to prev's lane end
        Length lookback = getGtu().getBehavioralCharacteristics().getParameter(ParameterTypes.LOOKBACK);
        // move upstream over branches
        while (!currentSet.isEmpty())
        {
            Iterator<LaneStructureRecord> iterator = currentSet.keySet().iterator();
            while (iterator.hasNext())
            {
                LaneStructureRecord record = iterator.next();
                int first;
                LaneBasedGTU up =
                    record.getLane().getGtuBehind(currentSet.get(record).plus(record.getLane().getLength()),
                        record.getDirection(), RelativePosition.REAR, getTimestamp());
                if (up == null)
                {
                    first = -1; // none
                }
                else
                {
                    first = record.getLane().getGtuList().indexOf(up); // from first upstream till last
                }
                // loop GTU's and create HeadwayGTU's
                for (int i = first; i >= 0; i--)
                {
                    LaneBasedGTU gtu = record.getLane().getGtuList().get(i);
                    Length distance =
                        currentSet.get(record).plus(
                            record.getLane().getLength().minus(gtu.position(record.getLane(), gtu.getFront())));
                    // only within lookback
                    if (distance.le(lookback))
                    {
                        headwaySet.add(new HeadwayGTUReal(gtu, distance));
                    }
                }
                // add prev lanes
                Length length = currentSet.get(record).plus(record.getLane().getLength());
                for (LaneStructureRecord prev : record.getPrev())
                {
                    prevSet.put(prev, length);
                }
                // TODO break search, but how to guarantee that the front of further GTU's is not within lookback?
            }
            currentSet = prevSet;
            prevSet = new HashMap<>();
        }
        this.followers.put(lane, new TimeStampedObject<>(headwaySet, getTimestamp()));

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
     * @param lat LEFT or RIGHT
     * @return list of followers on a lane
     * @throws ParameterException if parameter is not defined
     * @throws NullPointerException if {@code lat} is {@code null}
     * @throws IllegalArgumentException if {@code lat} is {@code NONE}
     */
    public final SortedSet<AbstractHeadwayGTU> getFirstLeaders(final LateralDirectionality lat) throws ParameterException,
        NullPointerException, IllegalArgumentException
    {
        checkLateralDirectionality(lat);
        return this.firstLeaders.get(lat).getObject();
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
     * @param lat LEFT or RIGHT
     * @return list of followers on a lane
     * @throws ParameterException if parameter is not defined
     * @throws NullPointerException if {@code lat} is {@code null}
     * @throws IllegalArgumentException if {@code lat} is {@code NONE}
     */
    public final SortedSet<AbstractHeadwayGTU> getFirstFollowers(final LateralDirectionality lat) throws ParameterException,
        NullPointerException, IllegalArgumentException
    {
        checkLateralDirectionality(lat);
        return this.firstFollowers.get(lat).getObject();
    }

    /**
     * Whether there is a GTU alongside, i.e. with overlap, in an adjacent lane.
     * @param lat LEFT or RIGHT
     * @return whether there is a GTU alongside, i.e. with overlap, in an adjacent lane
     * @throws ParameterException if parameter is not defined
     * @throws NullPointerException if {@code lat} is {@code null}
     * @throws IllegalArgumentException if {@code lat} is {@code NONE}
     */
    public final boolean isGtuAlongside(final LateralDirectionality lat) throws ParameterException, NullPointerException,
        IllegalArgumentException
    {
        checkLateralDirectionality(lat);
        return this.gtuAlongside.get(lat).getObject();
    }

    /**
     * Set of leaders on a lane, including adjacent GTU's who's FRONT is ahead of the own vehicle FRONT. Leaders are sorted by
     * headway value.
     * @param lane relative lateral lane
     * @return set of leaders on a lane, including adjacent GTU's who's FRONT is ahead of the own vehicle FRONT
     */
    public final SortedSet<AbstractHeadwayGTU> getLeaders(final RelativeLane lane)
    {
        return this.leaders.get(lane).getObject();
    }

    /**
     * Set of followers on a lane, including adjacent GTU's who's REAR is back of the own vehicle REAR. Follower are are sorted
     * by tailway value.
     * @param lane relative lateral lane
     * @return set of followers on a lane, including adjacent GTU's who's REAR is back of the own vehicle REAR
     */
    public final SortedSet<AbstractHeadwayGTU> getFollowers(final RelativeLane lane)
    {
        return this.followers.get(lane).getObject();
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
     * @param lat LEFT or RIGHT
     * @return list of followers on a lane
     * @throws ParameterException if parameter is not defined
     * @throws NullPointerException if {@code lat} is {@code null}
     * @throws IllegalArgumentException if {@code lat} is {@code NONE}
     */
    public final TimeStampedObject<SortedSet<AbstractHeadwayGTU>>
        getTimeStampedFirstLeaders(final LateralDirectionality lat) throws ParameterException, NullPointerException,
            IllegalArgumentException
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
     * @param lat LEFT or RIGHT
     * @return list of followers on a lane
     * @throws ParameterException if parameter is not defined
     * @throws NullPointerException if {@code lat} is {@code null}
     * @throws IllegalArgumentException if {@code lat} is {@code NONE}
     */
    public final TimeStampedObject<SortedSet<AbstractHeadwayGTU>> getTimeStampedFirstFollowers(
        final LateralDirectionality lat) throws ParameterException, NullPointerException, IllegalArgumentException
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
    public final TimeStampedObject<SortedSet<AbstractHeadwayGTU>> getTimeStampedLeaders(final RelativeLane lane)
    {
        return this.leaders.get(lane);
    }

    /**
     * Set of followers on a lane, including adjacent GTU's who's REAR is back of the own vehicle REAR. Follower are are sorted
     * by tailway value.
     * @param lane relative lateral lane
     * @return set of followers on a lane, including adjacent GTU's who's REAR is back of the own vehicle REAR
     */
    public final TimeStampedObject<SortedSet<AbstractHeadwayGTU>> getTimeStampedFollowers(final RelativeLane lane)
    {
        return this.followers.get(lane);
    }

    /**
     * Checks that lateral directionality is either left or right.
     * @param lat LEFT or RIGHT
     * @throws ParameterException if parameter is not defined
     * @throws NullPointerException if {@code lat} is {@code null}
     * @throws IllegalArgumentException if {@code lat} is {@code NONE}
     */
    private void checkLateralDirectionality(final LateralDirectionality lat) throws ParameterException,
        NullPointerException, IllegalArgumentException
    {
        Throw.whenNull(lat, "Lateral directionality may not be null.");
        Throw.when(lat.equals(LateralDirectionality.NONE), IllegalArgumentException.class,
            "Lateral directionality may not be NONE.");
        // TODO enable the check below once the lane structure operates
        /*-
        Throw
            .when(
                (lat.equals(LateralDirectionality.LEFT) && getPerception().getLaneStructure().getRootLSR().getLeft() == null)
                    || (lat.equals(LateralDirectionality.RIGHT) && getPerception().getLaneStructure().getRootLSR()
                        .getRight() == null), IllegalArgumentException.class,
                "Lateral directionality may only point to an adjacent lane.");
         */
    }

    /** {@inheritDoc} */
    public final String toString()
    {
        return "NeighborsCategory";
    }

    // TODO remove code below
    /**************************************************************************************************************************/
    /****** The code below has been copied from DefaultAlexander and should only be used by a first dummy implementation ******/
    /**************************************************************************************************************************/

    /** The lanes and path we expect to take if we do not change lanes. */
    private TimeStampedObject<LanePathInfo> lanePathInfo;

    /**
     * @throws GTUException when the GTU was not initialized yet.
     * @throws NetworkException when the speed limit for a GTU type cannot be retrieved from the network.
     * @throws ParameterException in case of not being able to retrieve parameter ParameterTypes.LOOKAHEAD
     */
    public final void updateLanePathInfo() throws GTUException, NetworkException, ParameterException
    {
        Time timestamp = getTimestamp();
        this.lanePathInfo =
            new TimeStampedObject<>(AbstractLaneBasedTacticalPlanner.buildLanePathInfo(getGtu(), getGtu()
                .getBehavioralCharacteristics().getParameter(ParameterTypes.LOOKAHEAD)), timestamp);
    }

    /**
     * Retrieve the last perceived lane path info.
     * @return LanePathInfo
     */
    public final LanePathInfo getLanePathInfo()
    {
        return this.lanePathInfo.getObject();
    }

    /**
     * Retrieve the time stamped last perceived lane path info.
     * @return LanePathInfo
     */
    public final TimeStampedObject<LanePathInfo> getTimeStampedLanePathInfo()
    {
        return this.lanePathInfo;
    }

    /**
     * Determine which GTU is in front of this GTU. This method looks in all lanes where this GTU is registered, and not further
     * than the value of the given maxDistance. The minimum headway is returned of all Lanes where the GTU is registered. When
     * no GTU is found within the given maxDistance, a HeadwayGTU with <b>null</b> as the gtuId and maxDistance as the distance
     * is returned. The search will extend into successive lanes if the maxDistance is larger than the remaining length on the
     * lane. When Lanes (or underlying CrossSectionLinks) diverge, a route planner may be used to determine which kinks and
     * lanes to take into account and which ones not. When the Lanes (or underlying CrossSectionLinks) converge, "parallel"
     * traffic is not taken into account in the headway calculation. Instead, gap acceptance algorithms or their equivalent
     * should guide the merging behavior.<br>
     * <b>Note:</b> Headway is the net headway and calculated on a front-to-back basis.
     * @param maxDistance the maximum distance to look for the nearest GTU; positive values search forwards; negative values
     *            search backwards
     * @return HeadwayGTU; the headway and the GTU information
     * @throws GTUException when there is an error with the next lanes in the network.
     * @throws NetworkException when there is a problem with the route planner
     */
    private Headway forwardHeadway(final Length maxDistance) throws GTUException, NetworkException
    {
        LanePathInfo lpi = getLanePathInfo();
        return forwardHeadway(lpi, maxDistance);
    }

    /**
     * Determine which GTU is in front of this GTU. This method uses a given lanePathInfo to look forward, but not further than
     * the value of the given maxDistance. The minimum headway is returned of all Lanes where the GTU is registered. When no GTU
     * is found within the given maxDistance, a HeadwayGTU with <b>null</b> as the gtuId and maxDistance as the distance is
     * returned. The search will extend into successive lanes if the maxDistance is larger than the remaining length on the
     * lane. When Lanes (or underlying CrossSectionLinks) diverge, a route planner may be used to determine which kinks and
     * lanes to take into account and which ones not. When the Lanes (or underlying CrossSectionLinks) converge, "parallel"
     * traffic is not taken into account in the headway calculation. Instead, gap acceptance algorithms or their equivalent
     * should guide the merging behavior.<br>
     * <b>Note:</b> Headway is the net headway and calculated on a front-to-back basis.
     * @param lpi the lanePathInfo object that informs the headway algorithm in which lanes to look, and from which position on
     *            the first lane.
     * @param maxDistance the maximum distance to look for the nearest GTU; positive values search forwards; negative values
     *            search backwards
     * @return HeadwayGTU; the headway and the GTU information
     * @throws GTUException when there is an error with the next lanes in the network.
     * @throws NetworkException when there is a problem with the route planner
     */
    private Headway forwardHeadway(final LanePathInfo lpi, final Length maxDistance) throws GTUException, NetworkException
    {
        Throw.when(maxDistance.le(Length.ZERO), GTUException.class, "forwardHeadway: maxDistance should be positive");

        int ldIndex = 0;
        LaneDirection ld = lpi.getReferenceLaneDirection();
        double gtuPosFrontSI = lpi.getReferencePosition().si;
        if (lpi.getReferenceLaneDirection().getDirection().isPlus())
        {
            gtuPosFrontSI += getGtu().getFront().getDx().si;
        }
        else
        {
            gtuPosFrontSI -= getGtu().getFront().getDx().si;
        }

        while ((gtuPosFrontSI > ld.getLane().getLength().si || gtuPosFrontSI < 0.0)
            && ldIndex < lpi.getLaneDirectionList().size() - 1)
        {
            ldIndex++;
            if (ld.getDirection().isPlus()) // e.g. 1005 on length of lane = 1000
            {
                if (lpi.getLaneDirectionList().get(ldIndex).getDirection().isPlus())
                {
                    gtuPosFrontSI -= ld.getLane().getLength().si;
                }
                else
                {
                    gtuPosFrontSI = lpi.getLaneDirectionList().get(ldIndex).getLane().getLength().si - gtuPosFrontSI;
                }
                ld = lpi.getLaneDirectionList().get(ldIndex);
            }
            else
            // e.g. -5 on lane of whatever length
            {
                if (lpi.getLaneDirectionList().get(ldIndex).getDirection().isPlus())
                {
                    gtuPosFrontSI += ld.getLane().getLength().si;
                }
                else
                {
                    gtuPosFrontSI += lpi.getLaneDirectionList().get(ldIndex).getLane().getLength().si;
                }
                ld = lpi.getLaneDirectionList().get(ldIndex);
            }
        }

        Time time = getGtu().getSimulator().getSimulatorTime().getTime();

        // look forward based on the provided lanePathInfo.
        return headwayLane(ld, gtuPosFrontSI, 0.0, time);

    }

    /**
     * Determine the positive headway on a lane, or null if no GTU can be found on this lane.
     * @param laneDirection the lane and direction to look
     * @param startPosSI the start position to look from in meters
     * @param cumDistSI the cumulative distance that has already been observed on other lanes
     * @param now the current time to determine the GTU positions on the lane
     * @return the HeadwayGTU, containing information on a GTU that is ahead of the given start position, or null if no GTU can
     *         be found on this lane
     * @throws GTUException when the GTUs ahead on the lane cannot be determined
     */
    private Headway headwayLane(final LaneDirection laneDirection, final double startPosSI, final double cumDistSI,
        final Time now) throws GTUException
    {
        Lane lane = laneDirection.getLane();
        LaneBasedGTU laneBasedGTU =
            lane.getGtuAhead(new Length(startPosSI, LengthUnit.SI), laneDirection.getDirection(), RelativePosition.REAR, now);
        if (laneBasedGTU == null)
        {
            return null;
        }
        double distanceSI = Math.abs(laneBasedGTU.position(lane, laneBasedGTU.getRear()).si - startPosSI);
        return new HeadwayGTUSimple(laneBasedGTU.getId(), laneBasedGTU.getGTUType(), new Length(cumDistSI + distanceSI,
            LengthUnit.SI), laneBasedGTU.getLength(), laneBasedGTU.getSpeed(), laneBasedGTU.getAcceleration());
    }

    /**
     * Determine which GTU is behind this GTU. This method looks in all lanes where this GTU is registered, and not further back
     * than the absolute value of the given maxDistance. The minimum net headway is returned of all Lanes where the GTU is
     * registered. When no GTU is found within the given maxDistance, <b>null</b> is returned. The search will extend into
     * successive lanes if the maxDistance is larger than the remaining length on the lane. When Lanes (or underlying
     * CrossSectionLinks) diverge, the headway algorithms have to look at multiple Lanes and return the minimum headway in each
     * of the Lanes. When the Lanes (or underlying CrossSectionLinks) converge, "parallel" traffic is not taken into account in
     * the headway calculation. Instead, gap acceptance algorithms or their equivalent should guide the merging behavior.<br>
     * <b>Note:</b> Headway is the net headway and calculated on a back-to-front basis.
     * @param maxDistance the maximum distance to look for the nearest GTU; it should have a negative value to search backwards
     * @return HeadwayGTU; the headway and the GTU information
     * @throws GTUException when there is an error with the next lanes in the network.
     * @throws NetworkException when there is a problem with the route planner
     */
    private Headway backwardHeadway(final Length maxDistance) throws GTUException, NetworkException
    {
        Throw.when(maxDistance.ge(Length.ZERO), GTUException.class, "backwardHeadway: maxDistance should be negative");
        Time time = getGtu().getSimulator().getSimulatorTime().getTime();
        double maxDistanceSI = maxDistance.si;
        Headway foundHeadway = new HeadwayDistance(-maxDistanceSI);
        for (Lane lane : getGtu().positions(getGtu().getRear()).keySet())
        {
            Headway closest =
                headwayRecursiveBackwardSI(lane, getGtu().getLanes().get(lane), getGtu().position(lane, getGtu().getRear(),
                    time).getSI(), 0.0, -maxDistanceSI, time);
            if (closest.getDistance().si < -maxDistanceSI && closest.getDistance().si < -foundHeadway.getDistance().si)
            {
                foundHeadway = closest;
            }
        }
        if (foundHeadway instanceof AbstractHeadwayGTU)
        {
            return new HeadwayGTUSimple(foundHeadway.getId(), ((AbstractHeadwayGTU) foundHeadway).getGtuType(), foundHeadway
                .getDistance().multiplyBy(-1.0), foundHeadway.getLength(), foundHeadway.getSpeed(), null);
        }
        return null;
    }

    /**
     * Calculate the minimum headway, possibly on subsequent lanes, in backward direction (so between our back, and the other
     * GTU's front). Note: this method returns a POSITIVE number.
     * @param lane the lane where we are looking right now
     * @param direction the direction we are driving on that lane
     * @param lanePositionSI from which position on this lane do we start measuring? This is the current position of the rear of
     *            the GTU when we measure in the lane where the original GTU is positioned, and lane.getLength() for each
     *            subsequent lane.
     * @param cumDistanceSI the distance we have already covered searching on previous lanes. Note: This is a POSITIVE number.
     * @param maxDistanceSI the maximum distance to look for in SI units; stays the same in subsequent calls. Note: this is a
     *            POSITIVE number.
     * @param when the current or future time for which to calculate the headway
     * @return the headway in SI units when we have found the GTU, or a null GTU with a distance of Double.MAX_VALUE meters when
     *         no other GTU could not be found within maxDistanceSI meters
     * @throws GTUException when there is a problem with the geometry of the network
     */
    private Headway headwayRecursiveBackwardSI(final Lane lane, final GTUDirectionality direction,
        final double lanePositionSI, final double cumDistanceSI, final double maxDistanceSI, final Time when)
        throws GTUException
    {
        LaneBasedGTU otherGTU =
            lane.getGtuBehind(new Length(lanePositionSI, LengthUnit.SI), direction, RelativePosition.FRONT, when);
        if (otherGTU != null)
        {
            double distanceM = cumDistanceSI + lanePositionSI - otherGTU.position(lane, otherGTU.getFront(), when).getSI();
            if (distanceM > 0 && distanceM <= maxDistanceSI)
            {
                return new HeadwayGTUSimple(otherGTU.getId(), otherGTU.getGTUType(), new Length(distanceM, LengthUnit.SI),
                    otherGTU.getLength(), otherGTU.getSpeed(), null);
            }
            return new HeadwayDistance(Double.MAX_VALUE);
        }

        // Continue search on predecessor lanes.
        if (cumDistanceSI + lanePositionSI < maxDistanceSI)
        {
            // is there a predecessor link?
            if (lane.prevLanes(getGtu().getGTUType()).size() > 0)
            {
                Headway foundMaxGTUDistanceSI = new HeadwayDistance(Double.MAX_VALUE);
                for (Lane prevLane : lane.prevLanes(getGtu().getGTUType()).keySet())
                {
                    // What is behind us is INDEPENDENT of the followed route!
                    double traveledDistanceSI = cumDistanceSI + lanePositionSI;
                    // WRONG - adapt method to forward perception method!
                    Headway closest =
                        headwayRecursiveBackwardSI(prevLane, direction, prevLane.getLength().getSI(), traveledDistanceSI,
                            maxDistanceSI, when);
                    if (closest.getDistance().si < maxDistanceSI
                        && closest.getDistance().si < foundMaxGTUDistanceSI.getDistance().si)
                    {
                        foundMaxGTUDistanceSI = closest;
                    }
                }
                return foundMaxGTUDistanceSI;
            }
        }

        // No other GTU was not on one of the current lanes or their successors.
        return new HeadwayDistance(Double.MAX_VALUE);
    }

}
