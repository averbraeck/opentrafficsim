package org.opentrafficsim.road.gtu.lane.perception.categories.neighbors;

import java.util.SortedSet;
import java.util.TreeSet;

import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.parameters.ParameterException;
import org.opentrafficsim.core.gtu.RelativePosition;
import org.opentrafficsim.core.gtu.perception.AbstractPerceptionCategory;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;
import org.opentrafficsim.road.gtu.lane.perception.LanePerception;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionCollectable;
import org.opentrafficsim.road.gtu.lane.perception.PerceptionReiterable;
import org.opentrafficsim.road.gtu.lane.perception.RelativeLane;
import org.opentrafficsim.road.gtu.lane.perception.object.PerceivedGtu;
import org.opentrafficsim.road.gtu.lane.perception.structure.NavigatingIterable.Entry;

/**
 * Perception of surrounding traffic on the own road, i.e. without crossing traffic.
 * <p>
 * Copyright (c) 2013-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class DirectNeighborsPerception extends AbstractPerceptionCategory<LaneBasedGtu, LanePerception>
        implements NeighborsPerception
{

    /** Perception GTU type that should be used. */
    private final PerceivedGtuType perceptionGtuType;

    /**
     * Constructor.
     * @param perception perception
     * @param perceptionGtuType type of perception gtu to generate
     */
    public DirectNeighborsPerception(final LanePerception perception, final PerceivedGtuType perceptionGtuType)
    {
        super(perception);
        this.perceptionGtuType = perceptionGtuType;
    }

    @Override
    public final SortedSet<PerceivedGtu> getFirstLeaders(final LateralDirectionality lat)
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
            for (Entry<LaneBasedGtu> entry : getPerception().getLaneStructure().getFirstDownstreamGtus(new RelativeLane(lat, 1),
                    RelativePosition.FRONT, RelativePosition.REAR, RelativePosition.FRONT, RelativePosition.REAR))
            {
                set.add(this.perceptionGtuType.createPerceivedGtu(getGtu(), getGtu(), entry.object(), entry.distance(), true));
            }
            return set;
        }
        catch (ParameterException | IllegalArgumentException exception)
        {
            throw new OtsRuntimeException("Unexpected exception while computing first leaders.", exception);
        }
    }

    @Override
    public final SortedSet<PerceivedGtu> getFirstFollowers(final LateralDirectionality lat)
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
            for (Entry<LaneBasedGtu> entry : getPerception().getLaneStructure().getFirstUpstreamGtus(new RelativeLane(lat, 1),
                    RelativePosition.REAR, RelativePosition.FRONT, RelativePosition.REAR, RelativePosition.FRONT))
            {
                set.add(this.perceptionGtuType.createPerceivedGtu(getGtu(), getGtu(), entry.object(), entry.distance(), false));
            }
            return set;
        }
        catch (ParameterException | IllegalArgumentException exception)
        {
            throw new OtsRuntimeException("Unexpected exception while computing first followers.", exception);
        }
    }

    @Override
    public final boolean isGtuAlongside(final LateralDirectionality lat)
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
            throw new OtsRuntimeException("Unexpected exception while computing gtu alongside.", exception);
        }
        // no such GTU
        return false;
    }

    @Override
    public final PerceptionCollectable<PerceivedGtu, LaneBasedGtu> getLeaders(final RelativeLane lane)
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
        Iterable<Entry<LaneBasedGtu>> iterable = Try.assign(() ->
        {
            return getPerception().getLaneStructure().getDownstreamGtus(lane, RelativePosition.FRONT, RelativePosition.FRONT,
                    RelativePosition.FRONT, RelativePosition.REAR);
        }, "Unable to get leaders from LaneStructure");
        return new PerceptionReiterable<>(getGtu(), iterable, (object, distance) ->
        {
            return Try.assign(() -> DirectNeighborsPerception.this.perceptionGtuType.createPerceivedGtu(getGtu(), getGtu(),
                    object, distance, true), "Unable to create PerceivedGtu");
        });
    }

    @Override
    public final PerceptionCollectable<PerceivedGtu, LaneBasedGtu> getFollowers(final RelativeLane lane)
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
        Iterable<Entry<LaneBasedGtu>> iterable = Try.assign(() ->
        {
            return getPerception().getLaneStructure().getUpstreamGtus(lane, RelativePosition.FRONT, RelativePosition.FRONT,
                    RelativePosition.REAR, RelativePosition.FRONT);
        }, "Unable to get followers from LaneStructure");
        return new PerceptionReiterable<>(getGtu(), iterable, (object, distance) ->
        {
            return Try.assign(() -> DirectNeighborsPerception.this.perceptionGtuType.createPerceivedGtu(getGtu(), getGtu(),
                    object, distance, false), "Unable to create PerceivedGtu");
        });
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

    @Override
    public final String toString()
    {
        return "DirectNeighborsPerception";
    }

}
