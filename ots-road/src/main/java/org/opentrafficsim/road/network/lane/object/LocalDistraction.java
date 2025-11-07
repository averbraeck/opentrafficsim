package org.opentrafficsim.road.network.lane.object;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.core.network.LateralDirectionality;
import org.opentrafficsim.core.network.NetworkException;
import org.opentrafficsim.road.network.lane.LanePosition;

/**
 * Local distraction.
 * <p>
 * Copyright (c) 2024-2025s Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LocalDistraction extends AbstractLaneBasedObject
{

    /** */
    private static final long serialVersionUID = 20250617L;

    /** Range of the distraction (upstream of the location). */
    private final Length range;

    /** Level of the distraction. */
    private final double distractionLevel;

    /** Side of the distraction. */
    private final LateralDirectionality side;

    /**
     * Constructor.
     * @param id id
     * @param position position
     * @param range range of the distraction (upstream of the location)
     * @param distractionLevel level of the distraction in the range [0...1)
     * @param side side if the distraction
     * @throws NetworkException when the position is not correct
     * @throws IllegalArgumentException when the range is not a positive {@code Length}, or the level is not in range [0...1)
     * @throws NullPointerException when side is {@code null}
     */
    public LocalDistraction(final String id, final LanePosition position, final Length range, final double distractionLevel,
            final LateralDirectionality side) throws NetworkException
    {
        super(id, position.lane(), position.position(), LaneBasedObject.makeLine(position.lane(), position.position()));
        Throw.when(range == null || range.lt0(), IllegalArgumentException.class, "Distance should be a positive Length.");
        Throw.when(distractionLevel < 0 || distractionLevel >= 1.0, IllegalArgumentException.class,
                "Distraction level should be in the range [0...1).");
        Throw.whenNull(side, "side");
        this.range = range;
        this.distractionLevel = distractionLevel;
        this.side = side;
        position.lane().addLaneBasedObject(this);
    }

    /**
     * Returns the range of the distraction, which applies upstream of the location.
     * @return range of the distraction
     */
    public Length getRange()
    {
        return this.range;
    }

    /**
     * Returns the distraction level as normalized task demand.
     * @return distraction level
     */
    public double getDistractionLevel()
    {
        return this.distractionLevel;
    }

    /**
     * Returns the side of the distraction, relative to the driving direction.
     * @return side of the distraction
     */
    public LateralDirectionality getSide()
    {
        return this.side;
    }

}
