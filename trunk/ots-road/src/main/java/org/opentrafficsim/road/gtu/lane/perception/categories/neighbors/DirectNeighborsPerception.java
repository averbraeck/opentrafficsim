package org.opentrafficsim.road.gtu.lane.perception.categories.neighbors;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.TimeStampedObject;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGTU;
import org.opentrafficsim.road.gtu.lane.perception.DownstreamNeighborsIterable;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LaneStructureRecord;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.UpstreamNeighborsIterable;
import org.opentrafficsim.road.gtu.lane.perception.categories.LaneBasedAbstractPerceptionCategory;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsUtil.DistanceGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGTU;

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
     * @param perception LanePerception; perception
     * @param headwayGtuType HeadwayGtuType; type of headway gtu to generate
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
        SortedSet<HeadwayGTU> headwaySet = NeighborsUtil.perceive(
                NeighborsUtil.getFirstDownstreamGTUs(
                        getPerception().getLaneStructure().getFirstRecord(new RelativeLane(lat, 1)), getGtu().getFront(),
                        getGtu().getFront(), RelativePosition.REAR, getTimestamp()),
                this.headwayGtuType, getGtu(), true);
        this.firstLeaders.put(lat, new TimeStampedObject<>(headwaySet, getTimestamp()));
    }

    /** {@inheritDoc} */
    @Override
    public final void updateFirstFollowers(final LateralDirectionality lat)
            throws GTUException, ParameterException, NetworkException
    {
        checkLateralDirectionality(lat);
        SortedSet<HeadwayGTU> headwaySet = NeighborsUtil.perceive(
                NeighborsUtil.getFirstUpstreamGTUs(getPerception().getLaneStructure().getFirstRecord(new RelativeLane(lat, 1)),
                        getGtu().getRear(), getGtu().getRear(), RelativePosition.FRONT, getTimestamp()),
                this.headwayGtuType, getGtu(), false);
        this.firstFollowers.put(lat, new TimeStampedObject<>(headwaySet, getTimestamp()));
    }

    /** {@inheritDoc} */
    @Override
    public final void updateGtuAlongside(final LateralDirectionality lat) throws GTUException, ParameterException
    {

        checkLateralDirectionality(lat);
        // check if any GTU is downstream of the rear, within the vehicle length
        SortedSet<DistanceGTU> headwaySet = NeighborsUtil.getFirstDownstreamGTUs(
                getPerception().getLaneStructure().getFirstRecord(new RelativeLane(lat, 1)), getGtu().getRear(),
                getGtu().getFront(), RelativePosition.FRONT, getTimestamp());
        if (!headwaySet.isEmpty() && headwaySet.first().getDistance().le0())
        {
            this.gtuAlongside.put(lat, new TimeStampedObject<>(true, getTimestamp()));
            return;
        }
        // check if any GTU is upstream of the front, within the vehicle length
        headwaySet =
                NeighborsUtil.getFirstUpstreamGTUs(getPerception().getLaneStructure().getFirstRecord(new RelativeLane(lat, 1)),
                        getGtu().getFront(), getGtu().getRear(), RelativePosition.REAR, getTimestamp());
        if (!headwaySet.isEmpty() && headwaySet.first().getDistance().le0())
        {
            this.gtuAlongside.put(lat, new TimeStampedObject<>(true, getTimestamp()));
            return;
        }
        // no such GTU
        this.gtuAlongside.put(lat, new TimeStampedObject<>(false, getTimestamp()));

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
        PerceptionCollectable<HeadwayGTU,
                LaneBasedGTU> it = new DownstreamNeighborsIterable(getGtu(), record, Length.max(Length.ZERO, pos),
                        getGtu().getParameters().getParameter(LOOKAHEAD), getGtu().getFront(), this.headwayGtuType, getGtu(),
                        lane, ignoreIfUpstream);
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
     * @param lat LateralDirectionality; LEFT or RIGHT
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
     * @param lat LateralDirectionality; LEFT or RIGHT
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
     * @param lat LateralDirectionality; LEFT or RIGHT
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
     * @param lane RelativeLane; relative lateral lane
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
     * @param lane RelativeLane; relative lateral lane
     * @return set of followers on a lane, including adjacent GTU's who's REAR is back of the own vehicle REAR
     */
    public final TimeStampedObject<PerceptionCollectable<HeadwayGTU, LaneBasedGTU>> getTimeStampedFollowers(
            final RelativeLane lane)
    {
        return this.followers.get(lane);
    }

    /**
     * Checks that lateral directionality is either left or right and an existing lane.
     * @param lat LateralDirectionality; LEFT or RIGHT
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

}
