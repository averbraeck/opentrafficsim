package org.opentrafficsim.editor.extensions.map.edit;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.ImageObserver;
import java.util.function.Function;
import java.util.function.Supplier;

import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.animation.DrawLevel;
import org.opentrafficsim.animation.PaintLine;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.editor.OtsEditorProperties;
import org.opentrafficsim.editor.extensions.map.EditorMap;
import org.opentrafficsim.editor.extensions.map.edit.HelperAnnotation.Helper;

/**
 * Annotations that helps provide context to a draggable annotation. For example snapping lines.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 */
public class HelperAnnotation extends MapAnnotation<Helper>
{

    /** Helper color. */
    private static final Color HELPER_COLOR =
            OtsEditorProperties.PROPERTIES_STORE.getColorOrDefault("map.helperColor", Color.LIGHT_GRAY.darker());

    /** Predicate for draggable. */
    private Supplier<Boolean> predicate = () -> true;

    /**
     * Constructor.
     * @param source source
     * @param map map
     * @param scale whether to scale this annotation
     */
    public HelperAnnotation(final Helper source, final EditorMap map, final Scaling scale)
    {
        super(source, map);
        setScale(scale == Scaling.SCALE);
    }

    /**
     * Set predicate. If the predicate is false, the draggable is not painted.
     * @param predicate predicate
     * @return this, for method chaining
     */
    public HelperAnnotation setPredicate(final Supplier<Boolean> predicate)
    {
        this.predicate = predicate;
        return this;
    }

    @Override
    public void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        if (!this.predicate.get())
        {
            return;
        }
        setRendering(graphics);
        graphics.setStroke(new BasicStroke(px(1.5f), BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND, 10.0f,
                px(new float[] {3.0f, 6.0f}), 10.0f));
        graphics.setColor(HELPER_COLOR);
        graphics.draw(PaintLine.getPath(ZERO, getSource().getLine()));
        resetRendering(graphics);
    }

    /**
     * Helper annotation data.
     */
    public static class Helper implements OtsShape
    {

        /** Location (same as draggable being helped). */
        private DirectedPoint2d location;

        /** Function to derive line from location. */
        private final Function<? super Point2d, ? extends PolyLine2d> lineFunction;

        /** Line. */
        private PolyLine2d line;

        /**
         * Constructor.
         * @param initialLocation initial location
         * @param lineFunction function to derive line from location draggable location
         */
        public Helper(final Point2d initialLocation, final Function<? super Point2d, ? extends PolyLine2d> lineFunction)
        {
            this.location = new DirectedPoint2d(initialLocation, 0.0);
            this.lineFunction = lineFunction;
            this.line = this.lineFunction.apply(this.location);
        }

        /**
         * Sets the location (same as draggable being helped) and updates the line. Invoked by {@link DraggableAnnotation} when
         * the draggable is moved or released.
         * @param location location
         */
        public void setLocation(final Point2d location)
        {
            this.location = new DirectedPoint2d(location, 0.0);
            this.line = this.lineFunction.apply(location);
        }

        @Override
        public double getZ()
        {
            return DrawLevel.SNAP.getZ();
        }

        @Override
        public DirectedPoint2d getLocation()
        {
            return this.location;
        }

        /**
         * Returns the line.
         * @return line
         */
        public PolyLine2d getLine()
        {
            return this.line;
        }

        @Override
        public Polygon2d getRelativeContour()
        {
            if (this.line instanceof Polygon2d pol)
            {
                return pol;
            }
            return new Polygon2d(this.line.getPointList());
        }
    }

    /**
     * Scaling option.
     */
    public enum Scaling
    {
        /** Scale the annotation. */
        SCALE,

        /** Do not scale the annotation. */
        NO_SCALE;
    }

}
