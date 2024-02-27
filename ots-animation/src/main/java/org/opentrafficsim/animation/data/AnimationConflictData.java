package org.opentrafficsim.animation.data;

import java.awt.Color;
import java.util.List;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.point.OrientedPoint2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.base.geometry.OtsBounds2d;
import org.opentrafficsim.draw.road.ConflictAnimation.ConflictData;
import org.opentrafficsim.road.network.lane.conflict.Conflict;

/**
 * Animation data of a Conflict.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class AnimationConflictData implements ConflictData
{

    /** Conflict. */
    private final Conflict conflict;

    /** Contour. */
    private List<Point2d> contour = null;

    /**
     * Constructor.
     * @param conflict Conflict; conflict.
     */
    public AnimationConflictData(final Conflict conflict)
    {
        this.conflict = conflict;
    }

    /** {@inheritDoc} */
    @Override
    public Length getLaneWidth()
    {
        return this.conflict.getLane().getWidth(this.conflict.getLongitudinalPosition());
    }

    /** {@inheritDoc} */
    @Override
    public OrientedPoint2d getLocation()
    {
        return this.conflict.getLocation();
    }

    /** {@inheritDoc} */
    @Override
    public OtsBounds2d getOtsBounds()
    {
        return this.conflict.getOtsBounds();
    }

    /** {@inheritDoc} */
    @Override
    public String getId()
    {
        return this.conflict.getFullId();
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public List<Point2d> getContour()
    {
        if (this.contour == null)
        {
            // this creates a new list every time, so we cache it
            this.contour = this.conflict.getGeometry().getPointList();
        }
        return this.contour;
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCrossing()
    {
        return this.conflict.getConflictType().isCrossing();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isPermitted()
    {
        return this.conflict.isPermitted();
    }

    /**
     * Returns the Conflict.
     * @return Conflict; GTU.
     */
    public Conflict getConflict()
    {
        return this.conflict;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Conflict " + this.conflict.getLane().getFullId() + " " + this.conflict.getLongitudinalPosition();
    }

}
