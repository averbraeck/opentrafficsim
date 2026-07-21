package org.opentrafficsim.editor.extensions.map.edit;

import org.djutils.draw.point.Point2d;
import org.opentrafficsim.animation.OtsRenderable;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.editor.extensions.map.EditorMap;
import org.opentrafficsim.editor.extensions.map.MapData;

/**
 * Convenience class where sub-classes can use {@link #px(double)} or {@link #px(float[])} to obtain a paint stroke defined at
 * pixel level for a locatable that is otherwise scale in world coordinates. This class can also listen to
 * {@link MapData#MAP_DATA_CHANGED} on relevant {@link MapData} instances to update derived animation data and coupled
 * annotations.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 * @param <L> locatable type
 */
public abstract class MapAnnotation<L extends OtsShape> extends OtsRenderable<L>
{

    /** Default point. */
    static final Point2d ZERO = new Point2d(0.0, 0.0);

    /** Map. */
    private final EditorMap map;

    /**
     * Constructor.
     * @param source source
     * @param map map
     */
    public MapAnnotation(final L source, final EditorMap map)
    {
        super(source, map.getContextualized());
        this.map = map;
    }

    /**
     * Returns the map.
     * @return map
     */
    protected EditorMap getMap()
    {
        return this.map;
    }

    /**
     * Returns a world line width that corresponds to a pixel width of the input value.
     * @param width intended pixel line width
     * @return world line width that corresponds to a pixel width of the input value
     */
    protected float px(final double width)
    {
        return isScale() ? (float) (width * this.map.getPanel().pxScale()) : (float) width;
    }

    /**
     * Scales the value in the input array such that an intended pixel dash pattern is translated in to current world
     * coordinates.
     * @param dash dash pattern
     * @return input array
     */
    protected float[] px(final float[] dash)
    {
        if (!isScale())
        {
            return dash;
        }
        double scale = this.map.getPanel().pxScale();
        for (int i = 0; i < dash.length; i++)
        {
            dash[i] = (float) (dash[i] * scale);
        }
        return dash;
    }

}
