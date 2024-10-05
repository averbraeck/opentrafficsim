package org.opentrafficsim.animation.data;

import java.awt.Color;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.OrientedPoint2d;
import org.opentrafficsim.animation.gtu.colorer.GtuColorer;
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
    private final GtuColorer gtuColorer;

    /** Gtu. */
    private final LaneBasedGtu gtu;

    /**
     * Constructor.
     * @param gtuColorer factory.
     * @param gtu GTU.
     */
    public AnimationGtuData(final GtuColorer gtuColorer, final LaneBasedGtu gtu)
    {
        this.gtuColorer = gtuColorer;
        this.gtu = gtu;
    }

    /** {@inheritDoc} */
    @Override
    public OrientedPoint2d getLocation()
    {
        return this.gtu.getLocation();
    }

    /** {@inheritDoc} */
    @Override
    public Bounds2d getBounds()
    {
        return this.gtu.getBounds();
    }
    
    /** {@inheritDoc} */
    @Override
    public Polygon2d getContour()
    {
        return this.gtu.getContour();
    }

    /** {@inheritDoc} */
    @Override
    public String getId()
    {
        return this.gtu.getId();
    }

    /** {@inheritDoc} */
    @Override
    public Color getColor()
    {
        return this.gtuColorer.getColor(this.gtu);
    }

    /** {@inheritDoc} */
    @Override
    public Length getLength()
    {
        return this.gtu.getLength();
    }

    /** {@inheritDoc} */
    @Override
    public Length getWidth()
    {
        return this.gtu.getWidth();
    }

    /** {@inheritDoc} */
    @Override
    public Length getFront()
    {
        return this.gtu.getFront().dx();
    }

    /** {@inheritDoc} */
    @Override
    public Length getRear()
    {
        return this.gtu.getRear().dx();
    }

    /** {@inheritDoc} */
    @Override
    public boolean leftIndicatorOn()
    {
        return this.gtu.getTurnIndicatorStatus().isLeftOrBoth();
    }

    /** {@inheritDoc} */
    @Override
    public boolean rightIndicatorOn()
    {
        return this.gtu.getTurnIndicatorStatus().isRightOrBoth();
    }

    /** {@inheritDoc} */
    @Override
    public RectangularShape getMarker()
    {
        switch (this.gtu.getType().getMarker())
        {
            case CIRCLE:
                return new Ellipse2D.Double(0, 0, 0, 0);
            case SQUARE:
                return new Rectangle2D.Double(0, 0, 0, 0);
            default:
                return new Ellipse2D.Double(0, 0, 0, 0);
        }
    }

    /** {@inheritDoc} */
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

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "Gtu " + this.gtu.getId();
    }

}
