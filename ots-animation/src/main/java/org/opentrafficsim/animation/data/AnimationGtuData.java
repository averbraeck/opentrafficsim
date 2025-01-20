package org.opentrafficsim.animation.data;

import java.awt.Color;
import java.awt.geom.RectangularShape;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.opentrafficsim.animation.gtu.colorer.GtuColorerManager;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.draw.gtu.DefaultCarAnimation.GtuData;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;

/**
 * Animation data of a LaneBasedGtu.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AnimationGtuData implements GtuData
{

    /** GTU colorer. */
    private final GtuColorerManager gtuColorerManager;

    /** Gtu. */
    private final LaneBasedGtu gtu;

    /** Marker. */
    private final GtuMarker marker;

    /**
     * Constructor.
     * @param gtuColorerManager factory.
     * @param gtu GTU.
     * @param marker marker
     */
    public AnimationGtuData(final GtuColorerManager gtuColorerManager, final LaneBasedGtu gtu, final GtuMarker marker)
    {
        this.gtuColorerManager = gtuColorerManager;
        this.gtu = gtu;
        this.marker = marker;
    }

    @Override
    public OrientedPoint2d getLocation()
    {
        return this.gtu.getLocation();
    }

    @Override
    public Polygon2d getContour()
    {
        return this.gtu.getContour();
    }

    @Override
    public Bounds2d getBounds()
    {
        return this.gtu.getBounds();
    }

    @Override
    public OtsShape getShape()
    {
        return this.gtu.getShape();
    }

    @Override
    public String getId()
    {
        return this.gtu.getId();
    }

    @Override
    public Color getColor()
    {
        return this.gtuColorerManager.getColor(this.gtu);
    }

    @Override
    public Length getLength()
    {
        return this.gtu.getLength();
    }

    @Override
    public Length getWidth()
    {
        return this.gtu.getWidth();
    }

    @Override
    public Length getFront()
    {
        return this.gtu.getFront().dx();
    }

    @Override
    public Length getRear()
    {
        return this.gtu.getRear().dx();
    }

    @Override
    public boolean leftIndicatorOn()
    {
        return this.gtu.getTurnIndicatorStatus().isLeftOrBoth();
    }

    @Override
    public boolean rightIndicatorOn()
    {
        return this.gtu.getTurnIndicatorStatus().isRightOrBoth();
    }

    @Override
    public RectangularShape getMarker()
    {
        return this.marker.getShape();
    }

    @Override
    public boolean isBrakingLightsOn()
    {
        return this.gtu.isBrakingLightsOn();
    }

    /**
     * Returns the GTU.
     * @return GTU.
     */
    public LaneBasedGtu getGtu()
    {
        return this.gtu;
    }

    @Override
    public String toString()
    {
        return "Gtu " + this.gtu.getId();
    }

}
