package org.opentrafficsim.animation.data;

import java.awt.Color;
import java.awt.geom.RectangularShape;

import org.djunits.value.vdouble.scalar.Length;
import org.opentrafficsim.animation.gtu.colorer.GtuColorerManager;
import org.opentrafficsim.base.geometry.OtsLine2d;
import org.opentrafficsim.draw.gtu.DefaultCarAnimation.GtuData;
import org.opentrafficsim.road.gtu.lane.LaneBasedGtu;

/**
 * Animation data of a LaneBasedGtu.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AnimationGtuData extends AnimationIdentifiableShape<LaneBasedGtu> implements GtuData
{

    /** GTU colorer. */
    private final GtuColorerManager gtuColorerManager;

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
        super(gtu);
        this.gtuColorerManager = gtuColorerManager;
        this.marker = marker;
    }

    @Override
    public Color getColor()
    {
        return this.gtuColorerManager.getColor(getObject());
    }

    @Override
    public Length getLength()
    {
        return getObject().getLength();
    }

    @Override
    public Length getWidth()
    {
        return getObject().getWidth();
    }

    @Override
    public Length getFront()
    {
        return getObject().getFront().dx();
    }

    @Override
    public Length getRear()
    {
        return getObject().getRear().dx();
    }

    @Override
    public boolean leftIndicatorOn()
    {
        return getObject().getTurnIndicatorStatus().isLeftOrBoth();
    }

    @Override
    public boolean rightIndicatorOn()
    {
        return getObject().getTurnIndicatorStatus().isRightOrBoth();
    }

    @Override
    public RectangularShape getMarker()
    {
        return this.marker.getShape();
    }

    @Override
    public boolean isBrakingLightsOn()
    {
        return getObject().isBrakingLightsOn();
    }

    @Override
    public String toString()
    {
        return "Gtu " + getId();
    }

    @Override
    public OtsLine2d getPath()
    {
        return getObject().getOperationalPlan().getPath();
    }

}
