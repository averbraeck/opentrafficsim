package org.opentrafficsim.editor.extensions.map.edit;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.image.ImageObserver;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import org.djutils.draw.bounds.Bounds2d;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.animation.DrawLevel;
import org.opentrafficsim.animation.PaintLine;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.OtsEditorProperties;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.extensions.map.EditorMap;
import org.opentrafficsim.editor.extensions.map.edit.DraggableAnnotation.Draggable;

import nl.tudelft.simulation.dsol.animation.d2.RenderableScale;
import nl.tudelft.simulation.naming.context.Contextualized;

/**
 * Draggable by which the user can influence a value in the data structure.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 */
public class DraggableAnnotation extends MapAnnotation<Draggable<?>>
{

    /** Fill color. */
    private static final Color FILL_COLOR =
            OtsEditorProperties.PROPERTIES_STORE.getColorOrDefault("map.draggableFillColor", Color.WHITE);

    /** Expression color. */
    private static final Color EXPRESSION_COLOR =
            OtsEditorProperties.PROPERTIES_STORE.getColorOrDefault("map.draggableExpressionColor", Color.RED);

    /** Edge color. */
    private static final Color EDGE_COLOR =
            OtsEditorProperties.PROPERTIES_STORE.getColorOrDefault("map.draggableEdgeColor", Color.BLACK);

    /** Pixel margin within which draggables are selected. */
    private static final int DRAGGABLE_SELECT_MARGIN_PX =
            OtsEditorProperties.PROPERTIES_STORE.getIntegerOrDefault("map.draggableSelectMarginPx", 3);

    /** Annotations that are static in the life cycle up to a mouse release. */
    private final Set<HelperAnnotation> staticAnnotations = new LinkedHashSet<>();

    /** Annotations that are continuously updated while dragging. */
    private final Set<HelperAnnotation> dynamicAnnotations = new LinkedHashSet<>();

    /** Whether the draggable is highlighted. */
    private boolean highlight;

    /**
     * Constructor.
     * @param source source
     * @param map map
     */
    public DraggableAnnotation(final Draggable<?> source, final EditorMap map)
    {
        super(source, map);
        setScale(false);
        setRotate(false);
    }

    /**
     * Sets whether the draggable is highlighted based on whether it is selected, and it is deletable.
     * @param selected whether the draggable is selected
     */
    public void setHighlightIfDeletable(final boolean selected)
    {
        this.highlight = selected && isDeletable();
    }

    /**
     * Returns whether the draggable is deletable.
     * @return whether the draggable is deletable
     */
    public boolean isDeletable()
    {
        return getSource().deleter != null;
    }

    /**
     * Shows the attribute and/or node in the tree that is affected by the draggable.
     * @param editor editor
     */
    public void showValue(final OtsEditor editor)
    {
        if (getSource().node != null)
        {
            editor.show(getSource().node, getSource().attribute);
        }
    }

    /**
     * Delete represented data of draggable.
     */
    public void delete()
    {
        if (isDeletable())
        {
            getSource().deleter.run();
        }
    }

    @Override
    public void paint(final Graphics2D graphics, final ImageObserver observer)
    {
        setRendering(graphics);
        graphics.setStroke(new BasicStroke(1.0f));
        graphics.setColor(getSource().isExpression ? EXPRESSION_COLOR : FILL_COLOR);
        Shape shape = PaintLine.getPath(ZERO, getSource().annotation);
        graphics.fill(shape);
        graphics.setColor(this.highlight ? SelectionAnnotation.SELECTION_COLOR : EDGE_COLOR);
        graphics.draw(shape);
        resetRendering(graphics);
    }

    /**
     * Mouse was dragged.
     * @param mousePoint world coordinate of mouse
     */
    public void mouseDragged(final Point2d mousePoint)
    {
        getSource().setPointSnapped(mousePoint);
        for (HelperAnnotation annotation : this.dynamicAnnotations)
        {
            annotation.getSource().setLocation(getSource().getLocation());
        }
    }

    /**
     * Mouse was released.
     * @param mousePoint world coordinate of mouse
     */
    public void mouseReleased(final Point2d mousePoint)
    {
        getSource().setPointSnapped(mousePoint);
        getSource().setValue();
        // there is no need to update this.staticAnnotations, all annotations are reloaded on MapData.MAP_DATA_CHANGED event
    }

    /**
     * Add helper annotation. The update mode determines when the location is updated, which is set to the location of the
     * animated draggable.
     * @param annotation annotation
     * @param updateMode whether the annotation is update while dragging or only on mouse release
     */
    public void addHelperAnnotation(final HelperAnnotation annotation, final UpdateMode updateMode)
    {
        (updateMode == UpdateMode.DYNAMIC ? this.dynamicAnnotations : this.staticAnnotations).add(annotation);
    }

    @Override
    public synchronized void destroy(final Contextualized contextProvider)
    {
        super.destroy(contextProvider);
        this.staticAnnotations.forEach((a) ->
        {
            a.destroy(contextProvider);
        });
        this.dynamicAnnotations.forEach((a) ->
        {
            a.destroy(contextProvider);
        });
    }

    @Override
    public boolean contains(final Point2d pointRelativeTo00, final RenderableScale scale, final double worldMargin,
            final double pixelMargin, final double xScale, final double yScale)
    {
        return getSource().signedDistance(pointRelativeTo00.x / xScale,
                pointRelativeTo00.y / yScale) < DRAGGABLE_SELECT_MARGIN_PX;
    }

    /**
     * Data for draggable renderable.
     * @param <T> value type that the draggable sets
     */
    public static class Draggable<T> implements OtsShape
    {

        /** Location of draggable. */
        private DirectedPoint2d location;

        /** Annotation shape around point. */
        private final PolyLine2d annotation;

        /** Logic to snap mouse world coordinate to world point of draggable. */
        private final UnaryOperator<Point2d> snapper;

        /** Function that determines the value from the draggable point. */
        private final Function<Point2d, T> valueFunction;

        /** Writes the value typically in an {@link XsdTreeNode}. */
        private final Consumer<T> valueWriter;

        /** Tree node of which this draggable affects the value (or attribute), if any. */
        private XsdTreeNode node;

        /** Attribute of tree node that this draggable affects, if any. */
        private String attribute;

        /** Deletes something in the data structure. */
        private Runnable deleter = null;

        /** Last value. */
        private T value;

        /** Whether a default value was set. */
        private boolean defaultValueSet = false;

        /** Default value. */
        private T defaultValue = null;

        /** Whether this draggable concerns a value that is currently an expression. */
        private boolean isExpression;

        /**
         * Constructor.
         * @param initialLocation initial location
         * @param annotation annotation shape around point
         * @param snapper logic to snap mouse world coordinate to world point of draggable
         * @param valueFunction function that determines the value from the draggable point
         * @param valueWriter set the value typically in an {@link XsdTreeNode}
         */
        public Draggable(final Point2d initialLocation, final PolyLine2d annotation, final UnaryOperator<Point2d> snapper,
                final Function<Point2d, T> valueFunction, final Consumer<T> valueWriter)
        {
            this.location = new DirectedPoint2d(initialLocation, 0.0);
            this.annotation = annotation;
            this.snapper = snapper;
            this.valueFunction = valueFunction;
            this.valueWriter = valueWriter;
        }

        /**
         * Set default value (which is set after the user double clicks the draggable).
         * @param defaultValue default value
         * @return this, for method chaining
         */
        public Draggable<T> setDefaultValue(final T defaultValue)
        {
            this.defaultValue = defaultValue;
            this.defaultValueSet = true;
            return this;
        }

        /**
         * Sets node of which this draggable affects the value. This is for display purposes (shift-click draggable to show in
         * tree).
         * @param node node of which this draggable affects the value
         * @return this, for method chaining
         */
        public Draggable<T> setNode(final XsdTreeNode node)
        {
            this.node = node;
            this.attribute = null;
            this.isExpression = node.valueIsExpression();
            return this;
        }

        /**
         * Returns the affected node.
         * @return affected node.
         */
        XsdTreeNode getNode()
        {
            return this.node;
        }

        /**
         * Sets node and attribute of the node of which this draggable affects the value. This is for display purposes
         * (shift-click draggable to show in tree).
         * @param node node of which this draggable affects an attribute value
         * @param attribute attribute name
         * @return this, for method chaining
         */
        @SuppressWarnings("hiddenfield")
        public Draggable<T> setNodeAttribute(final XsdTreeNode node, final String attribute)
        {
            this.node = node;
            this.attribute = attribute;
            this.isExpression = node.attributeIsExpression(node.getAttributeIndexByName(attribute));
            return this;
        }

        /**
         * Sets a deleter which deletes something in the data structure when the draggable is deleted by the user.
         * @param deleter deletes something in the data structure
         * @return this, for method chaining
         */
        public Draggable<T> setDeleter(final Runnable deleter)
        {
            this.deleter = deleter;
            return this;
        }

        /**
         * Writes the default value.
         */
        public void writeDefaultValue()
        {
            if (this.defaultValueSet && !this.isExpression)
            {
                this.value = this.defaultValue;
                this.valueWriter.accept(this.value);
            }
        }

        /**
         * Set the point based on mouse world coordinate. Also updates the internal draggable value, but not the value in the
         * data tree.
         * @param mousePoint mouse world coordinate
         */
        public void setPointSnapped(final Point2d mousePoint)
        {
            if (this.isExpression)
            {
                return;
            }
            Point2d point = this.snapper.apply(mousePoint);
            this.location = new DirectedPoint2d(point, 0.0);
            this.value = this.valueFunction.apply(point);
        }

        /**
         * Sets the value typically in an {@link XsdTreeNode}.
         */
        public void setValue()
        {
            if (this.isExpression)
            {
                return;
            }
            this.valueWriter.accept(this.value);
        }

        /**
         * Returns the current value based on the draggable location. That can be regarded as a continuous and temporary value
         * that is coupled to the live location of the draggable. This live value may be, and likely is, inconsistent with the
         * current value in the data structure (e.g. value in a {@link XsdTreeNode}). The live value is intended to be used by
         * live visual aides that indicate to the user what the draggable location will result in. For example, a dashed line
         * indicating a new centerline.
         * @return current value based on the draggable location
         */
        public T getValue()
        {
            return this.value;
        }

        @Override
        public double getZ()
        {
            return DrawLevel.ANNOTATION.getZ();
        }

        @Override
        public DirectedPoint2d getLocation()
        {
            return this.location;
        }

        @Override
        public Bounds2d getRelativeBounds()
        {
            return this.annotation.getAbsoluteBounds();
        }

        @Override
        public Polygon2d getRelativeContour()
        {
            return new Polygon2d(this.annotation.getPointList());
        }

    }

    /**
     * Update mode option.
     */
    public enum UpdateMode
    {
        /** Update draggable while dragging. */
        DYNAMIC,

        /** Update draggable only on mouse release. */
        STATIC;
    }

}
