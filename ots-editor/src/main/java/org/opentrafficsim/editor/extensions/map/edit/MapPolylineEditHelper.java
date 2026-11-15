package org.opentrafficsim.editor.extensions.map.edit;

import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;

import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Ray2d;
import org.djutils.draw.point.Point2d;
import org.djutils.math.AngleUtil;
import org.opentrafficsim.base.geometry.OtsGeometryUtil;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.Undo.ActionType;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.extensions.map.EditorMap;
import org.opentrafficsim.editor.extensions.map.MapLinkData;
import org.opentrafficsim.editor.extensions.map.edit.DraggableAnnotation.Draggable;
import org.opentrafficsim.editor.extensions.map.edit.DraggableAnnotation.UpdateMode;
import org.opentrafficsim.editor.extensions.map.edit.HelperAnnotation.Helper;
import org.opentrafficsim.editor.extensions.map.edit.HelperAnnotation.Scaling;

/**
 * Helper that builds and updates polyline draggables and their prediction helpers.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
final class MapPolylineEditHelper
{

    /** Minimum distance between point on polyline. */
    private static final double POLYLINE_DIST_MIN_M = 0.05;

    /** Editor map. */
    private final EditorMap editorMap;

    /** Editor. */
    private final OtsEditor editor;

    /** Snap to grid. */
    private final UnaryOperator<Point2d> snapToGrid;

    /** Predicate whether to display draggable helper annotation. */
    private final Function<DraggableAnnotation, Boolean> draggablePredicate;

    /** Selected draggable setter. */
    private final Consumer<DraggableAnnotation> selectedDraggableSetter;

    /** Callback to register draggables. */
    private final Consumer<DraggableAnnotation> registerDraggable;

    /** Coordinate string formatter. */
    private final Function<Point2d, String> coordinateFormatter;

    /** Node of poly line coordinate that is selected. */
    private XsdTreeNode selectedPolyLineCoordinateNode;

    /**
     * Constructor.
     * @param editorMap editor map
     * @param editor editor
     * @param snapToGrid snap to grid
     * @param draggablePredicate predicate whether to display draggable helper annotation
     * @param selectedDraggableSetter selected draggable setter
     * @param registerDraggable callback to register draggables
     * @param coordinateFormatter coordinate string formatter
     */
    MapPolylineEditHelper(final EditorMap editorMap, final OtsEditor editor, final UnaryOperator<Point2d> snapToGrid,
            final Function<DraggableAnnotation, Boolean> draggablePredicate,
            final Consumer<DraggableAnnotation> selectedDraggableSetter, final Consumer<DraggableAnnotation> registerDraggable,
            final Function<Point2d, String> coordinateFormatter)
    {
        this.editorMap = editorMap;
        this.editor = editor;
        this.snapToGrid = snapToGrid;
        this.draggablePredicate = draggablePredicate;
        this.selectedDraggableSetter = selectedDraggableSetter;
        this.registerDraggable = registerDraggable;
        this.coordinateFormatter = coordinateFormatter;
    }

    /**
     * Get selected polyline coordinate node.
     * @return selected polyline coordinate node
     */
    XsdTreeNode getSelectedPolyLineCoordinateNode()
    {
        return this.selectedPolyLineCoordinateNode;
    }

    /**
     * Set selected polyline coordinate node.
     * @param selectedPolyLineCoordinateNode selected polyline coordinate node
     */
    void setSelectedPolyLineCoordinateNode(final XsdTreeNode selectedPolyLineCoordinateNode)
    {
        this.selectedPolyLineCoordinateNode = selectedPolyLineCoordinateNode;
    }

    /**
     * Adds polyline link draggables. The link is checked to be a polyline.
     * @param linkData link data
     */
    void addPolylineDraggables(final MapLinkData linkData)
    {
        NavigableMap<XsdTreeNode, Point2d> coordinates = linkData.getPolylineCoordinates();
        if (coordinates.isEmpty())
        {
            // link in process of being deleted
            return;
        }
        if (coordinates.values().contains(null))
        {
            if (coordinates.size() > 1)
            {
                // at least one coordinate is invalid, show no editing draggables
                return;
            }
            // one coordinate tree node with empty or invalid coordinate value, allow map to show 1 draggable
            coordinates.clear();
        }

        PolyLine2d line = linkData.getCenterLine();
        Point2d prev = line.getFirst();
        DraggableAnnotation intermediary;
        Ray2d startRay = new Ray2d(linkData.getDesignLine().getStartPoint());
        Ray2d endRay = new Ray2d(linkData.getDesignLine().getEndPoint(),
                AngleUtil.normalizeAroundZero(linkData.getDesignLine().getEndDirection() + Math.PI));
        Point2d intermediaryLocation =
                coordinates.isEmpty() ? null : line.getFirst().interpolate(coordinates.firstEntry().getValue(), 0.5);

        for (Entry<XsdTreeNode, Point2d> entry : coordinates.entrySet())
        {
            Ray2d snapStartRay = coordinates.firstKey().equals(entry.getKey()) ? startRay : null;
            Ray2d snapEndRay = coordinates.lastKey().equals(entry.getKey()) ? endRay : null;

            if (prev.distance(entry.getValue()) > POLYLINE_DIST_MIN_M * 2)
            {
                intermediary = addPolyLineDraggable(line, intermediaryLocation, MapInputListener.CIRCLE, (p) ->
                {
                    String value = this.coordinateFormatter.apply(p);
                    this.editor.getUndo().startAction(ActionType.ADD, entry.getKey(), null);
                    XsdTreeNode newNode = entry.getKey().add();
                    newNode.move(-1);
                    this.editor.getUndo().setPostActionShowNode(newNode);
                    this.selectedPolyLineCoordinateNode = newNode;
                    newNode.setValue(value);
                }, snapStartRay, null);
                addPolyLinePrediction(linkData, entry.getKey(), intermediary, true, coordinates);
            }

            DraggableAnnotation coordinate =
                    addPolyLineDraggable(line, coordinates.get(entry.getKey()), MapInputListener.SQUARE, (p) ->
                    {
                        String value = this.coordinateFormatter.apply(p);
                        this.editor.getUndo().startAction(ActionType.VALUE_CHANGE, entry.getKey(), null);
                        entry.getKey().setValue(value);
                    }, snapStartRay, snapEndRay);
            coordinate.getSource().setNode(entry.getKey()).setDeleter(() ->
            {
                if (entry.getKey().isRemovable())
                {
                    this.editor.getUndo().startAction(ActionType.REMOVE, entry.getKey(), null);
                    entry.getKey().remove();
                }
            });
            if (this.selectedPolyLineCoordinateNode != null && this.selectedPolyLineCoordinateNode.equals(entry.getKey()))
            {
                coordinate.setHighlightIfDeletable(true);
                this.selectedDraggableSetter.accept(coordinate);
            }
            addPolyLinePrediction(linkData, entry.getKey(), coordinate, false, coordinates);

            Entry<XsdTreeNode, Point2d> priorNode = coordinates.higherEntry(entry.getKey());
            intermediaryLocation =
                    priorNode == null ? null : priorNode.getValue().interpolate(coordinates.get(entry.getKey()), 0.5);
            prev = entry.getValue();
        }

        if (prev.distance(line.getLast()) > POLYLINE_DIST_MIN_M * 2)
        {
            Ray2d snapStartRay = coordinates.isEmpty() ? startRay : null;
            Point2d from = coordinates.isEmpty() || coordinates.size() == 1 && coordinates.values().contains(null)
                    ? line.getFirst() : coordinates.lastEntry().getValue();
            intermediaryLocation = from.interpolate(line.getLast(), 0.5);
            intermediary = addPolyLineDraggable(line, intermediaryLocation, MapInputListener.CIRCLE, (p) ->
            {
                String value = this.coordinateFormatter.apply(p);
                XsdTreeNode newNode;
                if (coordinates.isEmpty())
                {
                    // special case of a single node with wrong coordinate that can be set through the map
                    newNode = linkData.getNode().getChild(0).getChild(0);
                    this.editor.getUndo().startAction(ActionType.VALUE_CHANGE, newNode, null);
                }
                else
                {
                    this.editor.getUndo().startAction(ActionType.ADD, coordinates.lastKey(), null);
                    newNode = coordinates.lastKey().add();
                }
                this.editor.getUndo().setPostActionShowNode(newNode);
                this.selectedPolyLineCoordinateNode = newNode;
                newNode.setValue(value);
            }, snapStartRay, endRay);
            addPolyLinePrediction(linkData, null, intermediary, true, coordinates);
        }
    }

    /**
     * Add polyline draggable, either on an existing point, or intermediary.
     * @param line current line, to stay away from current points
     * @param initialLocation initial location
     * @param annotation annotation (square or circle)
     * @param valueWriter value writer upon mouse release
     * @param startRay start point ray relevant for first intermediary draggable and first coordinate, {@code null} otherwise
     * @param endRay end point ray relevant for last intermediary draggable and last coordinate, {@code null} otherwise
     * @return draggable
     */
    private DraggableAnnotation addPolyLineDraggable(final PolyLine2d line, final Point2d initialLocation,
            final PolyLine2d annotation, final Consumer<Point2d> valueWriter, final Ray2d startRay, final Ray2d endRay)
    {
        AtomicReference<Point2d> lastPoint = new AtomicReference<>(initialLocation);
        DraggableAnnotation draggable = new DraggableAnnotation(new Draggable<>(initialLocation, annotation, (p) ->
        {
            Point2d point = snapToRaysOrGrid(p, startRay, endRay);
            if (line.getPointList().stream()
                    .noneMatch((c) -> c.distance(point) < POLYLINE_DIST_MIN_M && c.distance(initialLocation) > 1e-9))
            {
                lastPoint.set(point);
            }
            return lastPoint.get();
        }, (p) -> p, valueWriter), this.editorMap);
        draggable.setDynamic(true);
        this.registerDraggable.accept(draggable);
        return draggable;
    }

    /**
     * Snap mouse point to rays (if both or either not {@code null}), or snap to grid.
     * @param point mouse point
     * @param startRay start ray
     * @param endRay end ray
     * @return snapped point
     */
    private Point2d snapToRaysOrGrid(final Point2d point, final Ray2d startRay, final Ray2d endRay)
    {
        if (startRay == null)
        {
            if (endRay == null)
            {
                return this.snapToGrid.apply(point);
            }
            return closestPointOnRayAtMinimumDistance(point, endRay, POLYLINE_DIST_MIN_M);
        }
        if (endRay == null)
        {
            return closestPointOnRayAtMinimumDistance(point, startRay, POLYLINE_DIST_MIN_M);
        }
        return intersectionOrMidPoint(startRay, endRay);
    }

    /**
     * Snaps input point to the ray, but never closer than minimum distance to the end point of the ray.
     * @param point point
     * @param ray ray
     * @param minimumDistance minimum distance from ray origin
     * @return input point snapped to ray at a minimum distance from the ray end point
     */
    private static Point2d closestPointOnRayAtMinimumDistance(final Point2d point, final Ray2d ray,
            final double minimumDistance)
    {
        Point2d closest = ray.closestPointOnRay(point);
        return ray.distance(closest) < minimumDistance ? OtsGeometryUtil.offsetRay(ray, minimumDistance + 1e-9) : closest;
    }

    /**
     * Returns the intersection of two rays, or the mid point between both origins if they do not intersect or if they overlap.
     * @param ray1 ray 1
     * @param ray2 ray 2
     * @return intersection of two rays, or the mid point between both origins if they do not intersect or if they overlap
     */
    private static Point2d intersectionOrMidPoint(final Ray2d ray1, final Ray2d ray2)
    {
        double dx1 = Math.cos(ray1.dirZ);
        double dy1 = Math.sin(ray1.dirZ);
        double dx2 = Math.cos(ray2.dirZ);
        double dy2 = Math.sin(ray2.dirZ);
        double det = dx1 * dy2 - dy1 * dx2;
        if (det > 1e-9)
        {
            double rx = ray2.x - ray1.x;
            double ry = ray2.y - ray1.y;
            double t1 = (rx * dy2 - ry * dx2) / det;
            double t2 = (rx * dy1 - ry * dx1) / det;
            if (t1 >= -1e-9 && t2 >= -1e-9)
            {
                return new Point2d(ray1.x + t1 * dx1, ray1.y + t1 * dy1);
            }
        }
        return new Point2d(0.5 * (ray1.x + ray2.x), 0.5 * (ray1.y + ray2.y));
    }

    /**
     * Shows a prediction line on any draggable of a polyline when it is being dragged.
     * @param linkData link data
     * @param node XSD node of the latter coordinate, can be {@code null} for last intermediary node
     * @param draggable draggable for which to show prediction line
     * @param intermediate whether this concerns a draggable of an intermediary node
     * @param coordinates coordinates
     */
    private void addPolyLinePrediction(final MapLinkData linkData, final XsdTreeNode node, final DraggableAnnotation draggable,
            final boolean intermediate, final NavigableMap<XsdTreeNode, Point2d> coordinates)
    {
        Function<Point2d, PolyLine2d> predictionFunction = (point) ->
        {
            Point2d prior;
            Point2d posterior;
            if (coordinates.isEmpty() || coordinates.size() == 1 && coordinates.values().contains(null))
            {
                prior = linkData.getCenterLine().getFirst();
                posterior = linkData.getCenterLine().getLast();
            }
            else
            {
                Entry<XsdTreeNode, Point2d> priorEntry = node == null ? coordinates.lastEntry() : coordinates.lowerEntry(node);
                prior = priorEntry == null ? linkData.getCenterLine().getFirst() : priorEntry.getValue();
                Entry<XsdTreeNode, Point2d> posteriorEntry =
                        node == null ? null : (intermediate ? coordinates.ceilingEntry(node) : coordinates.higherEntry(node));
                posterior = posteriorEntry == null ? linkData.getCenterLine().getLast() : posteriorEntry.getValue();
            }
            PolyLine2d line = new PolyLine2d(prior, point, posterior);
            return OtsShape.transformLine(line, point);
        };
        Supplier<Boolean> predicate = () -> this.draggablePredicate.apply(draggable);
        // () -> this.draggingSupplier.get() && draggable.equals(this.selectedDraggableSupplier.get());
        HelperAnnotation predictionLine =
                new HelperAnnotation(new Helper(draggable.getSource().getLocation(), predictionFunction), this.editorMap,
                        Scaling.SCALE).setPredicate(predicate);
        draggable.addHelperAnnotation(predictionLine, UpdateMode.DYNAMIC);
    }
}
