package org.opentrafficsim.draw;

import org.djutils.draw.bounds.Bounds2d;
import org.opentrafficsim.base.geometry.OtsShape;

/**
 * This class returns bounds that respond to {@code contains(x, y)} by checking the actual shape, while also accounting for a
 * minimum clickable expanse. For line objects use {@code ClickableLineLocatable}.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface ClickableLocatable extends OtsShape
{

    /** Minimum expanse to click on object. */
    double EXPANSE = 2.0;

    @Override
    default Bounds2d getBounds()
    {
        return getBounds(this);
    }

    /**
     * Returns bounds that comply to the actual shape.
     * @param locatable locatable
     * @return bounds that comply to the actual shape.
     */
    static Bounds2d getBounds(final ClickableLocatable locatable)
    {
        /*
         * The reason this method is implemented as static, is such that sub-sub-classes can use this functionality, while an
         * intermediate sub-class overrides this. In particular, ClickableLineLocatable returns bounds regarding a line.
         * AnimationConflictData extends that via LaneBasedObjectData, but should be clickable over the entire region of the
         * conflict as a ClickableLocatable.
         */
        Bounds2d bounds = locatable.getRelativeContour().getBounds();
        boolean xExpand = bounds.getDeltaX() < EXPANSE;
        boolean yExpand = bounds.getDeltaY() < EXPANSE;
        return new Bounds2d(xExpand ? EXPANSE : bounds.getDeltaX(), yExpand ? EXPANSE : bounds.getDeltaY())
        {
            /** */
            private static final long serialVersionUID = 20241006L;

            @Override
            public boolean contains(final double x, final double y)
            {
                if (xExpand || yExpand)
                {
                    return getMinX() <= x && x <= getMaxX() && getMinY() <= y && y <= getMaxY();
                }
                return bounds.contains(x, y);
            }
        };
    }

}
