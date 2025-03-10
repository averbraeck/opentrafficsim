package org.opentrafficsim.animation.data;

import java.awt.Color;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.opentrafficsim.base.geometry.OtsLocatable;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.draw.ClickableLocatable;
import org.opentrafficsim.draw.road.ConflictAnimation.ConflictData;
import org.opentrafficsim.road.network.lane.conflict.Conflict;

/**
 * Animation data of a Conflict.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AnimationConflictData implements ConflictData
{

    /** Conflict. */
    private final Conflict conflict;

    /** Shape (cached). */
    private OtsShape shape;

    /**
     * Constructor.
     * @param conflict conflict.
     */
    public AnimationConflictData(final Conflict conflict)
    {
        this.conflict = conflict;
    }

    @Override
    public Length getLaneWidth()
    {
        return this.conflict.getLane().getWidth(this.conflict.getLongitudinalPosition());
    }

    @Override
    public DirectedPoint2d getLocation()
    {
        return this.conflict.getLocation();
    }

    @Override
    public String getId()
    {
        return this.conflict.getFullId();
    }

    @Override
    public Color getColor()
    {
        switch (this.conflict.conflictPriority())
        {
            case SPLIT:
                return Color.BLUE;
            case PRIORITY:
                return Color.GREEN;
            case YIELD:
                return Color.ORANGE;
            default:
                return Color.RED;
        }
    }

    @Override
    public Bounds2d getBounds()
    {
        return ClickableLocatable.getBounds(this);
    }

    @Override
    public Polygon2d getContour()
    {
        return this.conflict.getContour();
    }

    @Override
    public OtsShape getShape()
    {
        if (this.shape == null)
        {
            this.shape = ConflictData.super.getShape();
        }
        return this.shape;
    }

    @Override
    public PolyLine2d getLine()
    {
        return OtsLocatable.transformLine(this.conflict.getLine(), getLocation());
    }

    @Override
    public boolean isCrossing()
    {
        return this.conflict.getConflictType().isCrossing();
    }

    @Override
    public boolean isPermitted()
    {
        return this.conflict.isPermitted();
    }

    /**
     * Returns the Conflict.
     * @return GTU.
     */
    public Conflict getConflict()
    {
        return this.conflict;
    }

    @Override
    public String toString()
    {
        return "Conflict " + this.conflict.getLane().getFullId() + " " + this.conflict.getLongitudinalPosition();
    }

}
