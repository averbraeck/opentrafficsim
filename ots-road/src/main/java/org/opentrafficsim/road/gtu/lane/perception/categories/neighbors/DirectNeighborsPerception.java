package org.opentrafficsim.road.gtu.lane.perception.categories.neighbors;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.base.parameters.ParameterTypeLength;
import org.opentrafficsim.base.parameters.ParameterTypes;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.DownstreamNeighborsIterable;
import org.opentrafficsim.road.gtu.lane.perception.LaneBasedObjectIterable;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.LaneStructureRecord;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.UpstreamNeighborsIterable;
import org.opentrafficsim.road.gtu.lane.perception.categories.LaneBasedAbstractPerceptionCategory;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsUtil.DistanceGTU;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayConflict;
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.network.lane.conflict.Conflict;
import org.opentrafficsim.road.network.lane.object.LaneBasedObject;

/**
 * Perception of surrounding traffic on the own road, i.e. without crossing traffic.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://tudelft.nl/staff/p.knoppers-1">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class DirectNeighborsPerception extends LaneBasedAbstractPerceptionCategory implements NeighborsPerception
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** Look ahead parameter type. */
    protected static final ParameterTypeLength LOOKAHEAD = ParameterTypes.LOOKAHEAD;

    /** Look back parameter type. */
    protected static final ParameterTypeLength LOOKBACK = ParameterTypes.LOOKBACK;

    /** Headway GTU type that should be used. */
    private final HeadwayGtuType headwayGtuType;

    /** Headway GTU type that should be used to assess gaps. */
    private final HeadwayGtuType headwayGtuTypeGap;

    /**
     * @param perception LanePerception; perception
     * @param headwayGtuType HeadwayGtuType; type of headway gtu to generate
     */
    public DirectNeighborsPerception(final LanePerception perception, final HeadwayGtuType headwayGtuType)
    {
        this(perception, headwayGtuType, headwayGtuType);
    }

    /**
     * @param perception LanePerception; perception
     * @param headwayGtuType HeadwayGtuType; type of headway gtu to generate
     * @param headwayGtuTypeGap HeadwayGtuType; type of headway gtu to assess gaps
     */
    public DirectNeighborsPerception(final LanePerception perception, final HeadwayGtuType headwayGtuType,
            final HeadwayGtuType headwayGtuTypeGap)
    {
        super(perception);
        this.headwayGtuType = headwayGtuType;
        this.headwayGtuTypeGap = headwayGtuTypeGap;
    }

    /** {@inheritDoc} */
    @Override
    public final void updateAll() throws GtuException, NetworkException, ParameterException
    {
        // lazy evaluation
    }

    /** {@inheritDoc} */
    @Override
    public final SortedSet<HeadwayGtu> getFirstLeaders(final LateralDirectionality lat)
            throws ParameterException, NullPointerException, IllegalArgumentException
    {
        checkLateralDirectionality(lat);
        return computeIfAbsent("firstLeaders", () -> computeFirstLeaders(lat), lat);
    }

    /**
     * Computes the first leaders regarding splits.
     * @param lat LateralDirectionality; lateral directionality
     * @return SortedSet&lt;HeadwayGtu&gt;; first leaders
     */
    private SortedSet<HeadwayGtu> computeFirstLeaders(final LateralDirectionality lat)
    {
        try
        {
            return NeighborsUtil.perceive(
                    NeighborsUtil.getFirstDownstreamGTUs(
                            getPerception().getLaneStructure().getFirstRecord(new RelativeLane(lat, 1)), getGtu().getFront(),
                            getGtu().getFront(), RelativePosition.REAR, getTimestamp()),
                    this.headwayGtuTypeGap, getGtu(), true);
        }
        catch (ParameterException | GtuException | IllegalArgumentException exception)
        {
            throw new RuntimeException("Unexpected exception while computing first leaders.", exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final SortedSet<HeadwayGtu> getFirstFollowers(final LateralDirectionality lat)
            throws ParameterException, NullPointerException, IllegalArgumentException
    {
        checkLateralDirectionality(lat);
        return computeIfAbsent("firstFollowers", () -> computeFirstFollowers(lat), lat);
    }

    /**
     * Computes the first followers regarding splits.
     * @param lat LateralDirectionality; lateral directionality
     * @return SortedSet&lt;HeadwayGtu&gt;; first followers
     */
    private SortedSet<HeadwayGtu> computeFirstFollowers(final LateralDirectionality lat)
    {
        try
        {
            return NeighborsUtil.perceive(
                    NeighborsUtil.getFirstUpstreamGTUs(
                            getPerception().getLaneStructure().getFirstRecord(new RelativeLane(lat, 1)), getGtu().getRear(),
                            getGtu().getRear(), RelativePosition.FRONT, getTimestamp()),
                    this.headwayGtuTypeGap, getGtu(), false);
        }
        catch (ParameterException | GtuException | IllegalArgumentException exception)
        {
            throw new RuntimeException("Unexpected exception while computing first followers.", exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final boolean isGtuAlongside(final LateralDirectionality lat)
            throws ParameterException, NullPointerException, IllegalArgumentException
    {
        checkLateralDirectionality(lat);
        return computeIfAbsent("gtuAlongside", () -> computeGtuAlongside(lat), lat);
    }

    /**
     * Computes whether there is a GTU alongside.
     * @param lat LateralDirectionality; lateral directionality
     * @return boolean; whether there is a GTU alongside
     */
    public boolean computeGtuAlongside(final LateralDirectionality lat)
    {
        try
        {
            // check if any GTU is downstream of the rear, within the vehicle length
            SortedSet<DistanceGTU> headwaySet = NeighborsUtil.getFirstDownstreamGTUs(
                    getPerception().getLaneStructure().getFirstRecord(new RelativeLane(lat, 1)), getGtu().getRear(),
                    getGtu().getFront(), RelativePosition.FRONT, getTimestamp());
            if (!headwaySet.isEmpty() && headwaySet.first().getDistance().le0())
            {
                return true;
            }
            // check if any GTU is upstream of the front, within the vehicle length
            headwaySet = NeighborsUtil.getFirstUpstreamGTUs(
                    getPerception().getLaneStructure().getFirstRecord(new RelativeLane(lat, 1)), getGtu().getFront(),
                    getGtu().getRear(), RelativePosition.REAR, getTimestamp());
            if (!headwaySet.isEmpty() && headwaySet.first().getDistance().le0())
            {
                return true;
            }
        }
        catch (ParameterException | GtuException | IllegalArgumentException exception)
        {
            throw new RuntimeException("Unexpected exception while computing gtu alongside.", exception);
        }
        // no such GTU
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public final PerceptionCollectable<HeadwayGtu, LaneBasedGtu> getLeaders(final RelativeLane lane)
    {
        Throw.whenNull(lane, "Lane may not be null.");
        return computeIfAbsent("leaders", () -> computeLeaders(lane), lane);
    }

    /**
     * Computes leaders.
     * @param lane RelativeLane; lane
     * @return perception iterable for leaders
     */
    private PerceptionCollectable<HeadwayGtu, LaneBasedGtu> computeLeaders(final RelativeLane lane)
    {
        try
        {
            if (!getPerception().getLaneStructure().getExtendedCrossSection().contains(lane))
            {
                return null;
            }
            LaneStructureRecord record = getPerception().getLaneStructure().getFirstRecord(lane);
            Length pos = record.getStartDistance().neg();

            /*
             * On adjacent lanes we ignore GTUs that are upstream of any conflict on our current lane. For instance on the left
             * lane towards a turbo roundabout, the left adjacent lane is the lane that will come from the right, as it will
             * cross our lane and become our left lane. GTUs upstream of the crossing conflict, should hence be ignored.
             */
            if (!lane.isCurrent())
            {
                // find all conflicting conflicts
                Set<Conflict> conflicts = new LinkedHashSet<>();
                LaneStructureRecord currentRecord = getPerception().getLaneStructure().getFirstRecord(RelativeLane.CURRENT);
                boolean downstream = true;
                LaneBasedObjectIterable<HeadwayConflict,
                        Conflict> confs = new LaneBasedObjectIterable<HeadwayConflict, Conflict>(getGtu(), Conflict.class,
                                currentRecord, currentRecord.getStartDistance().neg(), downstream,
                                getGtu().getParameters().getParameter(ParameterTypes.LOOKAHEAD),
                                getGtu().getRelativePositions().get(RelativePosition.REFERENCE),
                                getGtu().getStrategicalPlanner().getRoute())
                        {
                            /** {@inheritDoc} */
                            @Override
                            protected HeadwayConflict perceive(final LaneBasedGtu perceivingGtu, final Conflict object,
                                    final Length distance) throws GtuException, ParameterException
                            {
                                return null;
                            }
                        };
                conflicts.addAll(confs.collect(() -> new LinkedHashSet<Conflict>(), (i, u, d) ->
                {
                    i.getObject().add(u.getOtherConflict());
                    return i;
                }, (i) -> i));

                // loop downstream towards the most downstream conflicting conflict, if any
                boolean conflictFound = false;
                LaneStructureRecord recordToConflict = null;
                Length posToConflict = null;
                LaneStructureRecord recordLoop = record;
                Length posLoop = pos;
                Length lookBack = getGtu().getParameters().getParameter(ParameterTypes.LOOKBACK);
                while (recordLoop != null && recordLoop.getStartDistance().lt(lookBack))
                {
                    List<LaneBasedObject> list = recordLoop.getLane().getLaneBasedObjects(posLoop, recordLoop.getLength());
                    for (LaneBasedObject object : list)
                    {
                        if (conflicts.contains(object))
                        {
                            Conflict c = (Conflict) object;
                            if ((c.getConflictType().isCrossing() || c.getConflictType().isMerge()))
                            {
                                conflictFound = true;
                                recordToConflict = recordLoop;
                                posToConflict = posLoop;
                            }
                        }
                    }
                    if (recordLoop.getNext().size() == 1)
                    {
                        recordLoop = recordLoop.getNext().get(0);
                    }
                    else
                    {
                        recordLoop = null;
                    }
                    posLoop = Length.ZERO;
                }

                // if any found, start search for downstream GTUs at that point
                if (conflictFound)
                {
                    record = recordToConflict;
                    pos = posToConflict;
                }
            }

            pos = pos.plus(getGtu().getFront().dx());
            boolean ignoreIfUpstream = true;
            return new DownstreamNeighborsIterable(getGtu(), record, Length.max(Length.ZERO, pos),
                    getGtu().getParameters().getParameter(LOOKAHEAD), getGtu().getFront(), this.headwayGtuType, lane,
                    ignoreIfUpstream);
        }
        catch (ParameterException | GtuException exception)
        {
            throw new RuntimeException("Unexpected exception while computing gtu alongside.", exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final PerceptionCollectable<HeadwayGtu, LaneBasedGtu> getFollowers(final RelativeLane lane)
    {
        Throw.whenNull(lane, "Lane may not be null.");
        return computeIfAbsent("followers", () -> computeFollowers(lane), lane);
    }

    /**
     * Computes followers.
     * @param lane RelativeLane; lane
     * @return perception iterable for followers
     */
    private PerceptionCollectable<HeadwayGtu, LaneBasedGtu> computeFollowers(final RelativeLane lane)
    {
        try
        {
            if (!getPerception().getLaneStructure().getExtendedCrossSection().contains(lane))
            {
                return null;
            }
            Throw.whenNull(lane, "Lane may not be null.");
            LaneStructureRecord record = getPerception().getLaneStructure().getFirstRecord(lane);
            Length pos;
            pos = record.getStartDistance().neg();
            pos = pos.plus(getGtu().getFront().dx());
            return new UpstreamNeighborsIterable(getGtu(), record, Length.max(Length.ZERO, pos),
                    getGtu().getParameters().getParameter(LOOKBACK), getGtu().getRear(), this.headwayGtuType, lane);
        }
        catch (ParameterException | GtuException exception)
        {
            throw new RuntimeException("Unexpected exception while computing gtu alongside.", exception);
        }
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
