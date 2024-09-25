package org.opentrafficsim.road.gtu.lane.perception.categories.neighbors;

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
import org.opentrafficsim.road.gtu.lane.perception.headway.HeadwayGtu;
import org.opentrafficsim.road.gtu.lane.perception.structure.LaneStructure.Entry;

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
public class DirectNeighborsPerception extends AbstractPerceptionCategory<LaneBasedGtu, LanePerception>
        implements NeighborsPerception
{

    /** */
    private static final long serialVersionUID = 20160811L;

    /** Headway GTU type that should be used. */
    private final HeadwayGtuType headwayGtuType;

    /**
     * Constructor.
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
            SortedSet<HeadwayGtu> set = new TreeSet<>();
            for (Entry<LaneBasedGtu> entry : getPerception().getLaneStructure().getFirstDownstreamGtus(new RelativeLane(lat, 1),
                    RelativePosition.FRONT, RelativePosition.REAR, RelativePosition.FRONT, RelativePosition.REAR))
            {
                set.add(this.headwayGtuType.createDownstreamGtu(getGtu(), entry.object(), entry.distance()));
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
            SortedSet<HeadwayGtu> set = new TreeSet<>();
            for (Entry<LaneBasedGtu> entry : getPerception().getLaneStructure().getFirstUpstreamGtus(new RelativeLane(lat, 1),
                    RelativePosition.REAR, RelativePosition.FRONT, RelativePosition.REAR, RelativePosition.FRONT))
            {
                set.add(this.headwayGtuType.createUpstreamGtu(getGtu(), entry.object(), entry.distance()));
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
        Iterable<Entry<LaneBasedGtu>> iterable = Try.assign(() -> getPerception().getLaneStructure().getDownstreamGtus(lane,
                RelativePosition.FRONT, RelativePosition.FRONT, RelativePosition.FRONT, RelativePosition.REAR), "");
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
                    public AbstractPerceptionReiterable<HeadwayGtu, LaneBasedGtu>.PrimaryIteratorEntry next()
                    {
                        Entry<LaneBasedGtu> entry = iterator.next();
                        return new PrimaryIteratorEntry(entry.object(), entry.distance());
                    }
                };
            }

            /** {@inheritDoc} */
            @Override
            protected HeadwayGtu perceive(final LaneBasedGtu perceivingGtu, final LaneBasedGtu object, final Length distance)
                    throws GtuException, ParameterException
            {
                return DirectNeighborsPerception.this.headwayGtuType.createDownstreamGtu(perceivingGtu, object, distance);
            }
        };
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
        Iterable<Entry<LaneBasedGtu>> iterable = Try.assign(() -> getPerception().getLaneStructure().getUpstreamGtus(lane,
                RelativePosition.FRONT, RelativePosition.FRONT, RelativePosition.REAR, RelativePosition.FRONT), "");
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
                    public AbstractPerceptionReiterable<HeadwayGtu, LaneBasedGtu>.PrimaryIteratorEntry next()
                    {
                        Entry<LaneBasedGtu> entry = iterator.next();
                        return new PrimaryIteratorEntry(entry.object(), entry.distance());
                    }
                };
            }

            /** {@inheritDoc} */
            @Override
            protected HeadwayGtu perceive(final LaneBasedGtu perceivingGtu, final LaneBasedGtu object, final Length distance)
                    throws GtuException, ParameterException
            {
                return DirectNeighborsPerception.this.headwayGtuType.createUpstreamGtu(perceivingGtu, object, distance);
            }
        };
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
        Throw.when(!getPerception().getLaneStructure().exists(lat.isLeft() ? RelativeLane.LEFT : RelativeLane.RIGHT),
                IllegalArgumentException.class, "Lateral directionality may only point to an existing adjacent lane.");
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "DirectNeighborsPerception";
    }

}
