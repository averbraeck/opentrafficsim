package org.opentrafficsim.road.gtu.lane.perception.mental.channel;

import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.GtuException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.perception.AbstractPerceptionCategory;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.AbstractPerceptionReiterable;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.Anticipation;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.DirectNeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.Estimation;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.NeighborsPerception;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.PerceivedGtuType;
import org.opentrafficsim.road.gtu.lane.perception.categories.neighbors.PerceivedGtuType.AnticipationPerceivedGtuType;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtu;
import org.opentrafficsim.road.gtu.lane.perception.structure.NavigatingIterable.Entry;

/**
 * This class is highly similar to {@link DirectNeighborsPerception}, but creates and uses 4 different perceived GTU types for
 * the 4 standard channels. These headway GTU types dynamically obtain a perception delay from the mental module.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class NeighborsPerceptionChannel extends AbstractPerceptionCategory<LaneBasedGtu, LanePerception>
        implements NeighborsPerception
{

    /** Left headway GTU type. */
    private final PerceivedGtuType perceivedGtuTypeLeft;

    /** Right headway GTU type. */
    private final PerceivedGtuType perceivedGtuTypeRight;

    /** Front headway GTU type. */
    private final PerceivedGtuType perceivedGtuTypeFront;

    /** Rear headway GTU type. */
    private final PerceivedGtuType perceivedGtuTypeRear;

    /**
     * Constructor.
     * @param perception perception.
     * @param estimation estimation.
     * @param anticipation anticipation.
     */
    public NeighborsPerceptionChannel(final LanePerception perception, final Estimation estimation,
            final Anticipation anticipation)
    {
        super(perception);
        Throw.when(!(getPerception().getMental() instanceof ChannelMental), IllegalArgumentException.class,
                "Mental module is not channel based.");
        ChannelMental mental = (ChannelMental) getPerception().getMental();
        this.perceivedGtuTypeLeft =
                new AnticipationPerceivedGtuType(estimation, anticipation, () -> mental.getPerceptionDelay(ChannelTask.LEFT));
        this.perceivedGtuTypeRight =
                new AnticipationPerceivedGtuType(estimation, anticipation, () -> mental.getPerceptionDelay(ChannelTask.RIGHT));
        this.perceivedGtuTypeFront =
                new AnticipationPerceivedGtuType(estimation, anticipation, () -> mental.getPerceptionDelay(ChannelTask.FRONT));
        this.perceivedGtuTypeRear =
                new AnticipationPerceivedGtuType(estimation, anticipation, () -> mental.getPerceptionDelay(ChannelTask.REAR));
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<PerceivedGtu> getFirstLeaders(final LateralDirectionality lat)
            throws ParameterException, NullPointerException, IllegalArgumentException
    {
        checkLateralDirectionality(lat);
        return computeIfAbsent("firstLeaders", () -> computeFirstLeaders(lat), lat);
    }

    /**
     * Computes the first leaders regarding splits.
     * @param lat lateral directionality
     * @return first leaders
     */
    private SortedSet<PerceivedGtu> computeFirstLeaders(final LateralDirectionality lat)
    {
        try
        {
            SortedSet<PerceivedGtu> set = new TreeSet<>();
            PerceivedGtuType perceivedGtuType = lat.isLeft() ? this.perceivedGtuTypeLeft : this.perceivedGtuTypeRight;
            for (Entry<LaneBasedGtu> entry : getPerception().getLaneStructure().getFirstDownstreamGtus(new RelativeLane(lat, 1),
                    RelativePosition.FRONT, RelativePosition.REAR, RelativePosition.FRONT, RelativePosition.REAR))
            {
                set.add(perceivedGtuType.createPerceivedGtu(getGtu(), getGtu(), entry.object(), entry.distance(), true));
            }
            return set;
        }
        catch (ParameterException | GtuException | IllegalArgumentException exception)
        {
            throw new RuntimeException("Unexpected exception while computing first leaders.", exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public SortedSet<PerceivedGtu> getFirstFollowers(final LateralDirectionality lat)
            throws ParameterException, NullPointerException, IllegalArgumentException
    {
        checkLateralDirectionality(lat);
        return computeIfAbsent("firstFollowers", () -> computeFirstFollowers(lat), lat);
    }

    /**
     * Computes the first followers regarding splits.
     * @param lat lateral directionality
     * @return first followers
     */
    private SortedSet<PerceivedGtu> computeFirstFollowers(final LateralDirectionality lat)
    {
        try
        {
            SortedSet<PerceivedGtu> set = new TreeSet<>();
            PerceivedGtuType perceivedGtuType = lat.isLeft() ? this.perceivedGtuTypeLeft : this.perceivedGtuTypeRight;
            for (Entry<LaneBasedGtu> entry : getPerception().getLaneStructure().getFirstUpstreamGtus(new RelativeLane(lat, 1),
                    RelativePosition.REAR, RelativePosition.FRONT, RelativePosition.REAR, RelativePosition.FRONT))
            {
                set.add(perceivedGtuType.createPerceivedGtu(getGtu(), getGtu(), entry.object(), entry.distance(), true));
            }
            return set;
        }
        catch (ParameterException | GtuException | IllegalArgumentException exception)
        {
            throw new RuntimeException("Unexpected exception while computing first followers.", exception);
        }
    }

    /** {@inheritDoc} */
    @Override
    public boolean isGtuAlongside(final LateralDirectionality lat)
            throws ParameterException, NullPointerException, IllegalArgumentException
    {
        checkLateralDirectionality(lat);
        return computeIfAbsent("gtuAlongside", () -> computeGtuAlongside(lat), lat);
    }

    /**
     * Computes whether there is a GTU alongside.
     * @param lat lateral directionality
     * @return whether there is a GTU alongside
     */
    public boolean computeGtuAlongside(final LateralDirectionality lat)
    {
        try
        {
            // check if any GTU is downstream of the rear, within the vehicle length
            for (Entry<LaneBasedGtu> entry : getPerception().getLaneStructure().getFirstDownstreamGtus(new RelativeLane(lat, 1),
                    RelativePosition.REAR, RelativePosition.FRONT, RelativePosition.FRONT, RelativePosition.REAR))
            {
                if (entry.distance().le0())
                {
                    return true;
                }
            }

            // check if any GTU is upstream of the front, within the vehicle length
            for (Entry<LaneBasedGtu> entry : getPerception().getLaneStructure().getFirstUpstreamGtus(new RelativeLane(lat, 1),
                    RelativePosition.FRONT, RelativePosition.REAR, RelativePosition.REAR, RelativePosition.FRONT))
            {
                if (entry.distance().le0())
                {
                    return true;
                }
            }
        }
        catch (ParameterException | IllegalArgumentException exception) // | GtuException
        {
            throw new RuntimeException("Unexpected exception while computing gtu alongside.", exception);
        }
        // no such GTU
        return false;
    }

    /** {@inheritDoc} */
    @Override
    public PerceptionCollectable<PerceivedGtu, LaneBasedGtu> getLeaders(final RelativeLane lane)
    {
        Throw.whenNull(lane, "Lane may not be null.");
        return computeIfAbsent("leaders", () -> computeLeaders(lane), lane);
    }

    /**
     * Computes leaders.
     * @param lane lane
     * @return perception iterable for leaders
     */
    private PerceptionCollectable<PerceivedGtu, LaneBasedGtu> computeLeaders(final RelativeLane lane)
    {
        Iterable<Entry<LaneBasedGtu>> iterable = Try.assign(() -> getPerception().getLaneStructure().getDownstreamGtus(lane,
                RelativePosition.FRONT, RelativePosition.FRONT, RelativePosition.FRONT, RelativePosition.REAR), "");
        PerceivedGtuType perceivedGtuType = lane.getLateralDirectionality().isNone() ? this.perceivedGtuTypeFront
                : (lane.getLateralDirectionality().isLeft() ? this.perceivedGtuTypeLeft : this.perceivedGtuTypeRight);
        return new AbstractPerceptionReiterable<>(Try.assign(() -> getGtu(), "GtuException"))
        {
            /** {@inheritDoc} */
            @Override
            protected Iterator<PrimaryIteratorEntry> primaryIterator()
            {
                Iterator<Entry<LaneBasedGtu>> iterator = iterable.iterator();
                return new Iterator<>()
                {
                    /** {@inheritDoc} */
                    @Override
                    public boolean hasNext()
                    {
                        return iterator.hasNext();
                    }

                    /** {@inheritDoc} */
                    @Override
                    public AbstractPerceptionReiterable<LaneBasedGtu, PerceivedGtu, LaneBasedGtu>.PrimaryIteratorEntry next()
                    {
                        Entry<LaneBasedGtu> entry = iterator.next();
                        return new PrimaryIteratorEntry(entry.object(), entry.distance());
                    }
                };
            }

            @Override
            protected PerceivedGtu perceive(final LaneBasedGtu perceivedGtu, final Length distance)
                    throws GtuException, ParameterException
            {
                return perceivedGtuType.createPerceivedGtu(getGtu(), getGtu(), perceivedGtu, distance, true);
            }
        };
    }

    /** {@inheritDoc} */
    @Override
    public PerceptionCollectable<PerceivedGtu, LaneBasedGtu> getFollowers(final RelativeLane lane)
    {
        Throw.whenNull(lane, "Lane may not be null.");
        return computeIfAbsent("followers", () -> computeFollowers(lane), lane);
    }

    /**
     * Computes followers.
     * @param lane lane
     * @return perception iterable for followers
     */
    private PerceptionCollectable<PerceivedGtu, LaneBasedGtu> computeFollowers(final RelativeLane lane)
    {
        Iterable<Entry<LaneBasedGtu>> iterable = Try.assign(() -> getPerception().getLaneStructure().getUpstreamGtus(lane,
                RelativePosition.FRONT, RelativePosition.FRONT, RelativePosition.REAR, RelativePosition.FRONT), "");
        PerceivedGtuType perceivedGtuType = lane.getLateralDirectionality().isNone() ? this.perceivedGtuTypeRear
                : (lane.getLateralDirectionality().isLeft() ? this.perceivedGtuTypeLeft : this.perceivedGtuTypeRight);
        return new AbstractPerceptionReiterable<>(Try.assign(() -> getGtu(), "GtuException"))
        {
            /** {@inheritDoc} */
            @Override
            protected Iterator<PrimaryIteratorEntry> primaryIterator()
            {
                Iterator<Entry<LaneBasedGtu>> iterator = iterable.iterator();
                return new Iterator<>()
                {
                    /** {@inheritDoc} */
                    @Override
                    public boolean hasNext()
                    {
                        return iterator.hasNext();
                    }

                    /** {@inheritDoc} */
                    @Override
                    public AbstractPerceptionReiterable<LaneBasedGtu, PerceivedGtu, LaneBasedGtu>.PrimaryIteratorEntry next()
                    {
                        Entry<LaneBasedGtu> entry = iterator.next();
                        return new PrimaryIteratorEntry(entry.object(), entry.distance());
                    }
                };
            }

            @Override
            protected PerceivedGtu perceive(final LaneBasedGtu perceivedGtu, final Length distance)
                    throws GtuException, ParameterException
            {
                return perceivedGtuType.createPerceivedGtu(getGtu(), getGtu(), perceivedGtu, distance, false);
            }
        };
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
        Throw.whenNull(lat, "Lateral directionality may not be null.");
        Throw.when(lat.equals(LateralDirectionality.NONE), IllegalArgumentException.class,
                "Lateral directionality may not be NONE.");
        Throw.when(!getPerception().getLaneStructure().exists(lat.isLeft() ? RelativeLane.LEFT : RelativeLane.RIGHT),
                IllegalArgumentException.class, "Lateral directionality may only point to an existing adjacent lane.");
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "NeighborsPerceptionChannel";
    }

}
