package org.opentrafficsim.editor.extensions.map.edit;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;

import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.opentrafficsim.animation.DrawLevel;
import org.opentrafficsim.animation.PaintLine;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.extensions.map.EditorMap;
import org.opentrafficsim.editor.extensions.map.MapData;
import org.opentrafficsim.editor.extensions.map.MapNodeData;
import org.opentrafficsim.editor.extensions.map.edit.SelectionAnnotation.ZAdjustedMapData;

/**
 * Renderable that indicates the selection of an object. It paints a line along the contour of the object.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 */
public class SelectionAnnotation extends MapAnnotation<ZAdjustedMapData>
{

    /** Selection color. */
    static final Color SELECTION_COLOR = OtsEditor.PROPERTIES_STORE.getColorOrDefault("map.selectionColor", Color.ORANGE);

    /** Invalid color. */
    static final Color INVALID_COLOR = OtsEditor.PROPERTIES_STORE.getColorOrDefault("map.invalidColor", Color.RED);

    /**
     * Constructor.
     * @param source source
     * @param map map
     */
    public SelectionAnnotation(final MapData source, final EditorMap map)
    {
        this(source, map, DrawLevel.SELECTION.getZ());
    }

    /**
     * Constructor.
     * @param source source
     * @param map map
     * @param z z-level
     */
    protected SelectionAnnotation(final MapData source, final EditorMap map, final double z)
    {
        super(new ZAdjustedMapData(source, z), map);
        setScaleY(true);
        setRotate(!(source instanceof MapNodeData));
    }

    @Override
    public void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        setRendering(graphics);
        graphics.setStroke(new BasicStroke(px(1.75f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f));
        graphics.setColor(getColor());
        graphics.draw(PaintLine.getPath(getSource().getLocation(), getSource().getAbsoluteContour()));
        resetRendering(graphics);
    }

    /**
     * Returns the color.
     * @return color
     */
    protected Color getColor()
    {
        return getSource().mapData().isValid() ? SELECTION_COLOR : INVALID_COLOR;
    }

    /**
     * Helper class that wraps a {@link MapData} and acts like it, except with an adjusted z-value as given.
     * @param mapData map data
     * @param adjustedZ adjusted z-value, see {@link DrawLevel} for values
     */
    public record ZAdjustedMapData(MapData mapData, double adjustedZ) implements OtsShape
    {

        @Override
        public double getZ()
        {
            return adjustedZ();
        }

        @Override
        public DirectedPoint2d getLocation()
        {
            return mapData().getLocation();
        }

        @Override
        public Polygon2d getRelativeContour()
        {
            return mapData().getRelativeContour();
        }

    }

}
