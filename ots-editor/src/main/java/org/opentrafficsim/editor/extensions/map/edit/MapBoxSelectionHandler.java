package org.opentrafficsim.editor.extensions.map.edit;

import java.awt.event.InputEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.function.Supplier;

import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Polygon2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.extensions.map.EditorMap;
import org.opentrafficsim.editor.extensions.map.MapData;
import org.opentrafficsim.editor.extensions.map.MapNodeData;
import org.opentrafficsim.editor.extensions.map.edit.HelperAnnotation.Helper;
import org.opentrafficsim.editor.extensions.map.edit.HelperAnnotation.Scaling;

/**
 * Helper handling Ctrl-drag box selection gesture state, visualization, and selection updates.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
final class MapBoxSelectionHandler
{

    /** Editor map. */
    private final EditorMap editorMap;

    /** Editor. */
    private final OtsEditor editor;

    /** Supplier of latest mouse-pressed screen location. */
    private final Supplier<Point2D> mousePressedSupplier;

    /** Supplier of active add-start node. */
    private final Supplier<MapNodeData> addingStartSupplier;

    /** Supplier of active draggable. */
    private final Supplier<DraggableAnnotation> selectedDraggableSupplier;

    /** Predicate to test current map selection membership. */
    private final Predicate<MapData> selectedPredicate;

    /** Callback to toggle map selection using add=true semantics. */
    private final Consumer<MapData> toggleSelection;

    /** Anchor point in world coordinates for Ctrl-drag box selection. */
    private Point2d anchor;

    /** Helper rectangle shown during Ctrl-drag box selection. */
    private HelperAnnotation helper;

    /**
     * Constructor.
     * @param editorMap editor map
     * @param editor editor
     * @param mousePressedSupplier supplier of latest mouse-pressed screen location
     * @param addingStartSupplier supplier of active add-start node
     * @param selectedDraggableSupplier supplier of active draggable
     * @param selectedPredicate predicate to test current map selection membership
     * @param toggleSelection callback to toggle map selection using add=true semantics
     */
    MapBoxSelectionHandler(final EditorMap editorMap, final OtsEditor editor, final Supplier<Point2D> mousePressedSupplier,
            final Supplier<MapNodeData> addingStartSupplier, final Supplier<DraggableAnnotation> selectedDraggableSupplier,
            final Predicate<MapData> selectedPredicate, final Consumer<MapData> toggleSelection)
    {
        this.editorMap = editorMap;
        this.editor = editor;
        this.mousePressedSupplier = mousePressedSupplier;
        this.addingStartSupplier = addingStartSupplier;
        this.selectedDraggableSupplier = selectedDraggableSupplier;
        this.selectedPredicate = selectedPredicate;
        this.toggleSelection = toggleSelection;
    }

    /**
     * Returns whether the current drag should be interpreted as a Ctrl-drag box selection gesture.
     * @param event mouse event
     * @return whether the current drag should be interpreted as a Ctrl-drag box selection gesture
     */
    boolean isGesture(final MouseEvent event)
    {
        return this.mousePressedSupplier.get() != null && this.addingStartSupplier.get() == null
                && this.selectedDraggableSupplier.get() == null && event.isControlDown()
                && (event.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0;
    }

    /**
     * Returns whether the box-selection helper is currently active.
     * @return whether the box-selection helper is currently active
     */
    boolean isActive()
    {
        return this.helper != null;
    }

    /**
     * Updates the helper rectangle shown while Ctrl-dragging to box-select map elements.
     * @param point current pointer location in screen coordinates
     */
    void update(final Point2D point)
    {
        Point2d world = worldCoordinate(point);
        if (this.anchor == null)
        {
            this.anchor = worldCoordinate(this.mousePressedSupplier.get());
        }
        if (this.anchor == null || world == null)
        {
            return;
        }
        if (this.helper == null)
        {
            this.helper = new HelperAnnotation(new Helper(world, (p) -> boxSelectionLine(this.anchor, p)), this.editorMap,
                    Scaling.SCALE);
            this.editorMap.addAnimation(this.helper);
        }
        else
        {
            this.helper.getSource().setLocation(world);
        }
        this.editorMap.update();
    }

    /**
     * Applies box-selection behavior when Ctrl-drag ends.
     * @param event release event
     */
    void apply(final MouseEvent event)
    {
        Point2d end = worldCoordinate(event.getPoint());
        Point2D mousePressed = this.mousePressedSupplier.get();
        if (this.anchor == null || end == null || mousePressed == null)
        {
            clear();
            return;
        }
        boolean fullyContained = event.getPoint().getX() >= mousePressed.getX();
        boolean remove = event.isShiftDown();

        Area selectionArea = boxSelectionArea(this.anchor, end);
        this.editor.setHighlight(null);
        this.editorMap.forEachMapData((mapData) ->
        {
            if (!mapData.isValid() || !matchesSelectionArea(mapData, selectionArea, fullyContained))
            {
                return;
            }
            if (remove)
            {
                if (this.selectedPredicate.test(mapData))
                {
                    this.toggleSelection.accept(mapData);
                }
            }
            else if (!this.selectedPredicate.test(mapData))
            {
                this.toggleSelection.accept(mapData);
            }
        });
        clear();
    }

    /**
     * Removes the helper rectangle for Ctrl-drag box selection.
     */
    void clear()
    {
        if (this.helper == null && this.anchor == null)
        {
            return;
        }
        if (this.helper != null)
        {
            this.editorMap.removeAnimation(this.helper);
            this.helper = null;
        }
        this.anchor = null;
        this.editorMap.update();
    }

    /**
     * Returns world coordinates from a screen location.
     * @param point screen location
     * @return world coordinates
     */
    private Point2d worldCoordinate(final Point2D point)
    {
        if (point == null)
        {
            return null;
        }
        return this.editorMap.getPanel().getRenderableScale().getWorldCoordinates(point, this.editorMap.getPanel().getExtent(),
                this.editorMap.getPanel().getSize());
    }

    /**
     * Returns whether map data matches box-selection criteria.
     * @param mapData map data
     * @param selectionArea selection area
     * @param fullyContained whether full containment is required
     * @return whether map data matches box-selection criteria
     */
    private static boolean matchesSelectionArea(final MapData mapData, final Area selectionArea, final boolean fullyContained)
    {
        Polygon2d contour = mapData.getAbsoluteContour();
        Area contourArea = polygonArea(contour);
        if (fullyContained)
        {
            if (contourArea.isEmpty())
            {
                return contour.getPointList().stream().allMatch((p) -> selectionArea.contains(p.x, p.y));
            }
            Area outside = new Area(contourArea);
            outside.subtract(selectionArea);
            return outside.isEmpty();
        }
        if (contourArea.isEmpty())
        {
            return contour.getPointList().stream().anyMatch((p) -> selectionArea.contains(p.x, p.y));
        }
        Area overlap = new Area(contourArea);
        overlap.intersect(selectionArea);
        return !overlap.isEmpty();
    }

    /**
     * Returns the helper polyline of a drag rectangle between anchor and current point.
     * @param anchor anchor point
     * @param current current point
     * @return helper polyline of a drag rectangle
     */
    private static PolyLine2d boxSelectionLine(final Point2d anchor, final Point2d current)
    {
        double dx = anchor.x - current.x;
        double dy = anchor.y - current.y;
        return new PolyLine2d(new Point2d(dx, dy), new Point2d(0.0, dy), new Point2d(0.0, 0.0), new Point2d(dx, 0.0),
                new Point2d(dx, dy));
    }

    /**
     * Returns the world-space area of the drag rectangle.
     * @param anchor anchor point
     * @param current current point
     * @return world-space area of the drag rectangle
     */
    private static Area boxSelectionArea(final Point2d anchor, final Point2d current)
    {
        double minX = Math.min(anchor.x, current.x);
        double minY = Math.min(anchor.y, current.y);
        double width = Math.max(Math.abs(anchor.x - current.x), 1e-9);
        double height = Math.max(Math.abs(anchor.y - current.y), 1e-9);
        return new Area(new Rectangle2D.Double(minX, minY, width, height));
    }

    /**
     * Returns an area for a contour polygon.
     * @param contour contour polygon
     * @return area for contour polygon
     */
    private static Area polygonArea(final Polygon2d contour)
    {
        List<Point2d> points = contour.getPointList();
        if (points.isEmpty())
        {
            return new Area();
        }
        Path2D.Double path = new Path2D.Double();
        path.moveTo(points.get(0).x, points.get(0).y);
        for (int i = 1; i < points.size(); i++)
        {
            path.lineTo(points.get(i).x, points.get(i).y);
        }
        path.closePath();
        return new Area(path);
    }

}
