package org.opentrafficsim.editor.extensions.map.edit;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.djutils.draw.curve.Flattener2d;
import org.djutils.draw.curve.Flattener2d.NumSegments;
import org.djutils.draw.curve.Straight2d;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.line.Ray2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.base.geometry.OtsGeometryUtil;
import org.opentrafficsim.base.geometry.OtsShape;
import org.opentrafficsim.core.geometry.BezierCubic2d;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.Undo.ActionType;
import org.opentrafficsim.editor.extensions.map.EditorMap;
import org.opentrafficsim.editor.extensions.map.MapLinkData;
import org.opentrafficsim.editor.extensions.map.edit.DraggableAnnotation.Draggable;
import org.opentrafficsim.editor.extensions.map.edit.DraggableAnnotation.UpdateMode;
import org.opentrafficsim.editor.extensions.map.edit.HelperAnnotation.Helper;
import org.opentrafficsim.editor.extensions.map.edit.HelperAnnotation.Scaling;

import com.google.common.util.concurrent.AtomicDouble;

/**
 * Helper that builds and updates Bezier draggables and their prediction helpers.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 */
final class MapBezierDraggablesHelper
{

    /** Minimum Bezier shape value when dragging. */
    static final double BEZIES_SHAPE_MIN = 0.01;

    /** Minimum Bezier shape value when dragging. */
    static final double BEZIES_SHAPE_MAX = 2.0;

    /** Format for arbitrary double values. */
    private static final DecimalFormat DOUBLE_FORMAT = new DecimalFormat("#.###", DecimalFormatSymbols.getInstance(Locale.US));

    /** Utility class. */
    private MapBezierDraggablesHelper()
    {
        //
    }

    /**
     * Add Bezier link draggables. The link is checked to be a Bezier.
     * @param editorMap editor map
     * @param editor editor
     * @param linkData link data
     * @param draggingFunction dragging state function
     * @param registerDraggable callback to register draggables
     */
    // () -> this.dragging
    static void addBezierDraggables(final EditorMap editorMap, final OtsEditor editor, final MapLinkData linkData,
            final Function<DraggableAnnotation, Boolean> draggingFunction,
            final Consumer<DraggableAnnotation> registerDraggable)
    {
        if (linkData.getDesignLine() instanceof Straight2d)
        {
            // cannot change a straight "Bezier"
            return;
        }

        // define geometry
        Function<Boolean, Double> computeWeight = (startSide) ->
        {
            if (!linkData.isWeightedBezier())
            {
                return 0.5;
            }
            DirectedPoint2d start = linkData.getDesignLine().getStartPoint();
            DirectedPoint2d end = linkData.getDesignLine().getEndPoint();
            double dStart = start.distance(new Ray2d(end).projectOrthogonalExtended(start));
            double dEnd = end.distance(new Ray2d(start).projectOrthogonalExtended(end));
            return (startSide ? dStart : dEnd) / (dStart + dEnd);
        };
        Supplier<PolyLine2d> lineSupplier1 = () ->
        {
            DirectedPoint2d start = linkData.getDesignLine().getStartPoint();
            DirectedPoint2d end = linkData.getDesignLine().getEndPoint();
            double distance = start.distance(end);
            double weight = computeWeight.apply(true);
            return new PolyLine2d(OtsGeometryUtil.translatePoint(start, distance * weight * BEZIES_SHAPE_MIN),
                    OtsGeometryUtil.translatePoint(start, distance * weight * BEZIES_SHAPE_MAX));
        };
        Supplier<PolyLine2d> lineSupplier2 = () ->
        {
            DirectedPoint2d start = linkData.getDesignLine().getStartPoint();
            DirectedPoint2d end = linkData.getDesignLine().getEndPoint();
            double distance = start.distance(end);
            double weight = computeWeight.apply(false);
            return new PolyLine2d(OtsGeometryUtil.translatePoint(end, -distance * weight * BEZIES_SHAPE_MIN),
                    OtsGeometryUtil.translatePoint(end, -distance * weight * BEZIES_SHAPE_MAX));
        };

        // value conversions
        AtomicDouble shapeValueHolder = new AtomicDouble();
        shapeValueHolder.set(linkData.getBezierShape());
        Function<Double, Double> fractionToShape = (f) -> BEZIES_SHAPE_MIN + f * (BEZIES_SHAPE_MAX - BEZIES_SHAPE_MIN);
        Function<Double, Double> shapeToFraction = (s) -> (s - BEZIES_SHAPE_MIN) / (BEZIES_SHAPE_MAX - BEZIES_SHAPE_MIN);
        Function<Supplier<PolyLine2d>, Function<Point2d, Double>> valueFunctionFactory = (lineSupplier) -> (p) ->
        {
            double v = fractionToShape.apply(lineSupplier.get().projectOrthogonalFractionalExtended(p));
            shapeValueHolder.set(v);
            return v;
        };
        Function<Point2d, Double> valueFunction1 = valueFunctionFactory.apply(lineSupplier1);
        Function<Point2d, Double> valueFunction2 = valueFunctionFactory.apply(lineSupplier2);

        // create draggables
        Consumer<Double> valueWriter = (v) ->
        {
            String value = v == null ? null : DOUBLE_FORMAT.format(v);
            editor.getUndo().startAction(ActionType.ATTRIBUTE_CHANGE, linkData.getNode().getChild(0), "Shape");
            linkData.getNode().getChild(0).setAttributeValue("Shape", value);
        };
        DraggableAnnotation draggable1 = addBezierDraggable(linkData, lineSupplier1, valueFunction1, valueWriter,
                shapeToFraction, editorMap, registerDraggable);
        DraggableAnnotation draggable2 = addBezierDraggable(linkData, lineSupplier2, valueFunction2, valueWriter,
                shapeToFraction, editorMap, registerDraggable);

        // shape prediction line
        Flattener2d flattener = new NumSegments(32);
        Function<Point2d, PolyLine2d> predictionFunction = (p) ->
        {
            double f = shapeToFraction.apply(shapeValueHolder.get());
            // this updates the location of the small circular shape draggables live
            draggable1.getSource().setPointSnapped(lineSupplier1.get().getLocationFractionExtended(f));
            draggable2.getSource().setPointSnapped(lineSupplier2.get().getLocationFractionExtended(f));
            PolyLine2d line = new BezierCubic2d(linkData.getDesignLine().getStartPoint(), draggable1.getSource().getLocation(),
                    draggable2.getSource().getLocation(), linkData.getDesignLine().getEndPoint()).toPolyLine(flattener);
            return OtsShape.transformLine(line, p);
        };
        HelperAnnotation predictionLine =
                new HelperAnnotation(new Helper(linkData.getLocation(), predictionFunction), editorMap, Scaling.SCALE)
                        .setPredicate(() -> draggingFunction.apply(draggable1) || draggingFunction.apply(draggable2));
        draggable1.addHelperAnnotation(predictionLine, UpdateMode.DYNAMIC);
        draggable2.addHelperAnnotation(predictionLine, UpdateMode.DYNAMIC);
    }

    /**
     * Create Bezier draggable.
     * @param linkData link data
     * @param lineSupplier line supplier
     * @param valueFunction value supplier
     * @param valueWriter value writer
     * @param shapeToFraction shape to fraction function
     * @param editorMap editor map
     * @param registerDraggable callback to register draggables
     * @return draggable
     */
    private static DraggableAnnotation addBezierDraggable(final MapLinkData linkData, final Supplier<PolyLine2d> lineSupplier,
            final Function<Point2d, Double> valueFunction, final Consumer<Double> valueWriter,
            final Function<Double, Double> shapeToFraction, final EditorMap editorMap,
            final Consumer<DraggableAnnotation> registerDraggable)
    {
        Point2d point = lineSupplier.get().getLocationFractionExtended(shapeToFraction.apply(linkData.getBezierShape()));
        DraggableAnnotation draggable = new DraggableAnnotation(new Draggable<Double>(point, MapInputListener.CIRCLE,
                p -> lineSupplier.get().closestPointOnPolyLine(p), valueFunction, valueWriter).setDefaultValue(null)
                        .setNodeAttribute(linkData.getNode().getChild(0), "Shape"),
                editorMap);
        registerDraggable.accept(draggable);
        draggable.addHelperAnnotation(
                new HelperAnnotation(new Helper(linkData.getLocation(), p -> OtsShape.transformLine(lineSupplier.get(), p)),
                        editorMap, Scaling.SCALE),
                UpdateMode.STATIC);
        return draggable;
    }

}
