package org.opentrafficsim.road.gtu.lane.perception.categories.neighbors;

import java.util.SortedSet;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
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
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
    public final void updateAll() throws GTUException, NetworkException, ParameterException
    {
        // lazy evaluation
    }

    /** {@inheritDoc} */
    @Override
    public final SortedSet<HeadwayGTU> getFirstLeaders(final LateralDirectionality lat)
            throws ParameterException, NullPointerException, IllegalArgumentException
    {
        checkLateralDirectionality(lat);
        return computeIfAbsent("firstLeaders", () -> computeFirstLeaders(lat), lat);
    }

    /**
     * Computes the first leaders regarding splits.
     * @param lat LateralDirectionality; lateral directionality
     * @return SortedSet&lt;HeadwayGTU&gt;; first leaders
     */
    private SortedSet<HeadwayGTU> computeFirstLeaders(final LateralDirectionality lat)
    {
        try
        {
            return NeighborsUtil.perceive(
                    NeighborsUtil.getFirstDownstreamGTUs(
                            getPerception().getLaneStructure().getFirstRecord(new RelativeLane(lat, 1)), getGtu().getFront(),
                            getGtu().getFront(), RelativePosition.REAR, getTimestamp()),
                    this.headwayGtuTypeGap, getGtu(), true);
        }
        catch (ParameterException | GTUException | IllegalArgumentException exception)
        {
            throw new RuntimeException("Unexpected exception while computing first leaders.", exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final SortedSet<HeadwayGTU> getFirstFollowers(final LateralDirectionality lat)
            throws ParameterException, NullPointerException, IllegalArgumentException
    {
        checkLateralDirectionality(lat);
        return computeIfAbsent("firstFollowers", () -> computeFirstFollowers(lat), lat);
    }

    /**
     * Computes the first followers regarding splits.
     * @param lat LateralDirectionality; lateral directionality
     * @return SortedSet&lt;HeadwayGTU&gt;; first followers
     */
    private SortedSet<HeadwayGTU> computeFirstFollowers(final LateralDirectionality lat)
    {
        try
        {
            return NeighborsUtil.perceive(
                    NeighborsUtil.getFirstUpstreamGTUs(
                            getPerception().getLaneStructure().getFirstRecord(new RelativeLane(lat, 1)), getGtu().getRear(),
                            getGtu().getRear(), RelativePosition.FRONT, getTimestamp()),
                    this.headwayGtuTypeGap, getGtu(), false);
        }
        catch (ParameterException | GTUException | IllegalArgumentException exception)
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
        catch (ParameterException | GTUException | IllegalArgumentException exception)
        {
            throw new RuntimeException("Unexpected exception while computing gtu alongside.", exception);
        }
        // no such GTU
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public final PerceptionCollectable<HeadwayGTU, LaneBasedGTU> getLeaders(final RelativeLane lane)
    {
        Throw.whenNull(lane, "Lane may not be null.");
        return computeIfAbsent("leaders", () -> computeLeaders(lane), lane);
    }

    /**
     * Computes leaders.
     * @param lane RelativeLane; lane
     * @return perception iterable for leaders
     */
    private PerceptionCollectable<HeadwayGTU, LaneBasedGTU> computeLeaders(final RelativeLane lane)
    {
        try
        {
            if (!getPerception().getLaneStructure().getExtendedCrossSection().contains(lane))
            {
                return null;
            }
            LaneStructureRecord record = getPerception().getLaneStructure().getFirstRecord(lane);
            Length pos = record.getStartDistance().neg();
            pos = record.getDirection().isPlus() ? pos.plus(getGtu().getFront().getDx())
                    : pos.minus(getGtu().getFront().getDx());
            boolean ignoreIfUpstream = true;
            return new DownstreamNeighborsIterable(getGtu(), record, Length.max(Length.ZERO, pos),
                    getGtu().getParameters().getParameter(LOOKAHEAD), getGtu().getFront(), this.headwayGtuType, lane,
                    ignoreIfUpstream);
        }
        catch (ParameterException | GTUException exception)
        {
            throw new RuntimeException("Unexpected exception while computing gtu alongside.", exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public final PerceptionCollectable<HeadwayGTU, LaneBasedGTU> getFollowers(final RelativeLane lane)
    {
        Throw.whenNull(lane, "Lane may not be null.");
        return computeIfAbsent("followers", () -> computeFollowers(lane), lane);
    }

    /**
     * Computes followers.
     * @param lane RelativeLane; lane
     * @return perception iterable for followers
     */
    private PerceptionCollectable<HeadwayGTU, LaneBasedGTU> computeFollowers(final RelativeLane lane)
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
            pos = record.getDirection().isPlus() ? pos.plus(getGtu().getFront().getDx())
                    : pos.minus(getGtu().getFront().getDx());
            return new UpstreamNeighborsIterable(getGtu(), record, Length.max(Length.ZERO, pos),
                    getGtu().getParameters().getParameter(LOOKBACK), getGtu().getRear(), this.headwayGtuType, lane);
        }
        catch (ParameterException | GTUException exception)
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
