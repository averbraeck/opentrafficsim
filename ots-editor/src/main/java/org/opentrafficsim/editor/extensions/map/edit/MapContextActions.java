package org.opentrafficsim.editor.extensions.map.edit;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;

import org.djunits.value.vdouble.scalar.Direction;
import org.djutils.draw.line.Ray2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.animation.data.util.IconUtil;
import org.opentrafficsim.base.geometry.OtsGeometryUtil;
import org.opentrafficsim.core.geometry.BezierCubic2d;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.Undo.ActionType;
import org.opentrafficsim.editor.XsdPaths;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.extensions.map.EditorMap;
import org.opentrafficsim.editor.extensions.map.MapData;
import org.opentrafficsim.editor.extensions.map.MapLinkData;
import org.opentrafficsim.editor.extensions.map.MapVisualizationPanel;
import org.opentrafficsim.road.network.CrossSectionLink.Priority;
import org.opentrafficsim.xml.bindings.types.ArcDirectionType.ArcDirection;

/**
 * Functionality regarding context menu actions on map elements, such as aligning elements of the network.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 */
final class MapContextActions
{

    /** Icon size in context menu items. */
    private static final int ICON_SIZE = 18;

    /** Utility class. */
    private MapContextActions()
    {
        //
    }

    /**
     * Shows a context menu for one or more map elements. The menu is not shown if there are no such items.
     * @param dataList map elements for which to show the context menu
     * @param editorMap editor map
     * @param visualizationPanel visualization panel
     * @param event mouse event that triggered the context menu
     * @return whether a menu with items was shown
     */
    public static boolean showContextMenu(final List<MapData> dataList, final EditorMap editorMap,
            final MapVisualizationPanel visualizationPanel, final MouseEvent event)
    {
        JPopupMenu menu = new JPopupMenu();
        fillContextMenu(dataList, editorMap, menu, event);
        if (menu.getComponentCount() == 0)
        {
            return false;
        }
        menu.show(visualizationPanel, (int) event.getPoint().getX(), (int) event.getPoint().getY());
        return true;
    }

    /**
     * Fills the given context menu with items that are specific to the map elements.
     * @param dataList map element for which to show the context menu
     * @param editorMap editor map
     * @param menu context menu to fill
     * @param event mouse event that triggered the context menu
     */
    private static void fillContextMenu(final List<MapData> dataList, final EditorMap editorMap, final JPopupMenu menu,
            final MouseEvent event)
    {
        if (dataList.size() == 1)
        {
            MapData data = dataList.get(0);
            if (data instanceof MapLinkData mapLinkData)
            {
                fillContextMenuLink(mapLinkData, editorMap, menu, event);
            }
        }
        else
        {
            fillContextMenuMultiple(dataList, menu, event);
        }
    }

    /**
     * Fills the context menu with actions specific to multiple elements.
     * @param dataList map elements for which to show the context menu
     * @param menu context menu to fill
     * @param event mouse event that triggered the context menu
     */
    private static void fillContextMenuMultiple(final List<MapData> dataList, final JPopupMenu menu, final MouseEvent event)
    {
        boolean allLink = true;
        Set<Priority> priorities = new LinkedHashSet<>();
        for (MapData data : dataList)
        {
            if (data instanceof MapLinkData mapLinkData && !mapLinkData.isConnector())
            {
                String current = data.getNode().getAttributeValueOrDefault("Priority");
                Priority priority = current == null ? Priority.NONE
                        : Arrays.stream(Priority.values()).noneMatch(e -> e.name().equals(current)) ? null
                                : Priority.valueOf(current);
                priorities.add(priority);
            }
            else
            {
                allLink = false;
            }
        }
        if (allLink)
        {
            addPriorityContextMenu(menu, dataList, priorities);
        }
    }

    /**
     * Adds an item to a context menu.
     * @param menu context menu
     * @param label item label
     * @param icon icon, may be {@code null} to have no icon
     * @param action action to perform when the item is selected
     * @param editor editor
     */
    private static void addContextMenuItem(final JComponent menu, final String label, final Icon icon, final Runnable action,
            final OtsEditor editor)
    {
        JMenuItem item = new JMenuItem(label);
        if (icon != null)
        {
            item.setIcon(icon);
        }
        item.addActionListener((e) ->
        {
            action.run();
            editor.invalidateAndRepaint();
        });
        menu.add(item);
    }

    /**
     * Fills the context menu with actions specific to a link.
     * @param data map element for which to show the context menu
     * @param editorMap editor map
     * @param menu context menu
     * @param event mouse event that triggered the context menu
     */
    private static void fillContextMenuLink(final MapLinkData data, final EditorMap editorMap, final JPopupMenu menu,
            final MouseEvent event)
    {
        if (!data.getNode().getPathString().equals(XsdPaths.LINK) || !data.hasShapeNode())
        {
            return;
        }
        // Note: each action should specify its own suitable undo action
        Icon autoFix = IconUtil.of("AutoFix24.png").imageSize(ICON_SIZE, ICON_SIZE).get();
        if (data.isStraight())
        {
            addContextMenuItem(menu, "Align node directions", autoFix, () -> alignStraightNodeDirections(data),
                    data.getEditor());
        }
        else if (data.isArc())
        {
            if (data.getStartDirection() != null)
            {
                addContextMenuItem(menu, "Align with nodes and start direction", autoFix, () -> alignArc(data, true),
                        data.getEditor());
            }
            if (data.getEndDirection() != null)
            {
                addContextMenuItem(menu, "Align with nodes and end direction", autoFix, () -> alignArc(data, false),
                        data.getEditor());
            }
        }
        else if (data.isBezier())
        {
            String un = data.isWeightedBezier() ? "un" : "";
            addContextMenuItem(menu, "Set " + un + "weighted", autoFix, () -> flipBezierWeighted(data), data.getEditor());
            addContextMenuItem(menu, "Minimize curvature", autoFix, () -> minimizeBezierCurvature(data), data.getEditor());
        }
        if (!data.isConnector())
        {
            Point2D clickPoint = event.getPoint();
            Icon icon = IconUtil.of("Node24.png").imageSize(ICON_SIZE, ICON_SIZE).get();
            addContextMenuItem(menu, "Split link", icon, () ->
            {
                XsdTreeNode newLink = MapLinkEditActions.splitLink(editorMap, data, clickPoint);
                if (newLink != null)
                {
                    data.getEditor().getUndo().setPostActionShowNode(newLink);
                }
            }, data.getEditor());

            addLinkShapeContextMenu(menu, data);
            String current = data.getNode().getAttributeValueOrDefault("Priority");
            Set<Priority> currentPriority = current == null ? Set.of(Priority.NONE)
                    : Arrays.stream(Priority.values()).noneMatch(e -> e.name().equals(current)) ? Collections.emptySet()
                            : Set.of(Priority.valueOf(current));
            menu.add(new JSeparator());
            addPriorityContextMenu(menu, List.of(data), currentPriority);
        }
        // otherwise no items; menu will be skipped
    }

    /**
     * Add link shape context menu.
     * @param menu context menu
     * @param linkData link data
     */
    private static void addLinkShapeContextMenu(final JPopupMenu menu, final MapLinkData linkData)
    {
        JMenuItem shapeMenu = new JMenu("Shape");
        String current = linkData.getShapeNode().getNodeName();
        shapeMenu.setIcon(IconUtil.of("Bezier24.png").imageSize(ICON_SIZE, ICON_SIZE).get());
        Icon check = IconUtil.of("Check24.png").imageSize(ICON_SIZE, ICON_SIZE).get();
        for (String shape : EditorMap.LINK_SHAPES)
        {
            addContextMenuItem(shapeMenu, shape, current.equals(shape) ? check : null, () -> setShape(linkData, shape),
                    linkData.getEditor());
        }
        menu.add(shapeMenu);
    }

    /**
     * Adds priority context menu item for one or multiple links.
     * @param menu context menu
     * @param links links, one or a verified multi-selection of {@link MapLinkData}
     * @param currentPriorities current values
     */
    private static void addPriorityContextMenu(final JPopupMenu menu, final List<MapData> links,
            final Set<Priority> currentPriorities)
    {
        JMenuItem priorityMenu = new JMenu("Priority");
        priorityMenu.setIcon(IconUtil.of("Priority24.png").imageSize(ICON_SIZE, ICON_SIZE).get());
        menu.add(priorityMenu);
        IconUtil util = IconUtil.of("Check24.png").imageSize(ICON_SIZE, ICON_SIZE);
        if (currentPriorities.size() > 1)
        {
            util = util.gray();
        }
        Icon icon = util.get();
        for (Priority priority : Priority.values())
        {
            addContextMenuItem(priorityMenu, menuFriendlyName(priority.name()),
                    currentPriorities.contains(priority) ? icon : null, () -> setPriority(links, priority),
                    links.get(0).getEditor());
        }
    }

    /**
     * Aligns the directions of both connected nodes with the straight link.
     * @param data map element for which to show the context menu
     */
    private static void alignStraightNodeDirections(final MapLinkData data)
    {
        Direction direction = direction(data.getStartPoint(), data.getEndPoint());
        if (direction == null)
        {
            return;
        }
        data.getUndo().startAction(ActionType.ATTRIBUTE_CHANGE, data.getNode(), null);
        setNodeDirection(data.getStartNode(), direction);
        setNodeDirection(data.getEndNode(), direction);
    }

    /**
     * Returns the direction from one point to another.
     * @param from from point
     * @param to to point
     * @return direction from one point to another, or {@code null} if the points are not available or coincide
     */
    private static Direction direction(final Point2d from, final Point2d to)
    {
        if (from == null || to == null || from.distance(to) < 1e-9)
        {
            return null;
        }
        return Direction.ofSI(Math.atan2(to.y - from.y, to.x - from.x));
    }

    /**
     * Aligns the arc with the coordinates of both nodes and the direction of one of the nodes. The radius and direction of the
     * arc are set, as well as the direction of the node that is not kept.
     * @param data map element for which to show the context menu
     * @param keepStart whether to keep the start node direction, otherwise the end node direction is kept
     */
    private static void alignArc(final MapLinkData data, final boolean keepStart)
    {
        if (data.getStartPoint() == null || data.getEndPoint() == null)
        {
            return;
        }
        Direction kept = keepStart ? data.getStartDirection() : data.getEndDirection();
        Point2d pointEnd = data.getEndPoint();
        Point2d pointStart = data.getStartPoint();
        double dx = pointEnd.x - pointStart.x;
        double dy = pointEnd.y - pointStart.y;
        double lengthSquared = dx * dx + dy * dy;
        if (kept == null || lengthSquared < 1e-9)
        {
            return;
        }
        double sin = Math.sin(kept.si);
        double cos = Math.cos(kept.si);
        // dot product of the chord with the normal at the kept side
        double dot = keepStart ? -dx * sin + dy * cos : dx * sin - dy * cos;
        if (Math.abs(dot) < 1e-9 * Math.sqrt(lengthSquared))
        {
            return; // nodes and direction are collinear, there is no finite arc
        }
        double radius = lengthSquared / (2.0 * Math.abs(dot));
        boolean left = dot > 0.0;
        double chordDirection = Math.atan2(dy, dx);
        Direction alignedDirection = Direction.ofSI(2.0 * chordDirection - kept.si);
        data.getUndo().startAction(ActionType.ATTRIBUTE_CHANGE, data.getShapeNode(), null);
        data.getShapeNode().setAttributeValue("Radius", radius + "m");
        data.getShapeNode().setAttributeValue("Direction", left ? ArcDirection.LEFT.toString() : ArcDirection.RIGHT.toString());
        setNodeDirection(keepStart ? data.getEndNode() : data.getStartNode(), alignedDirection);
    }

    /**
     * Sets the direction attribute of a node. This does not start an undo action.
     * @param node node
     * @param direction direction
     */
    private static void setNodeDirection(final XsdTreeNode node, final Direction direction)
    {
        if (node != null)
        {
            node.setAttributeValue("Direction", Math.toDegrees(direction.si) + " deg");
        }
    }

    /**
     * Flip weighted state of Bezier shape.
     * @param data map element for which to show the context menu
     */
    private static void flipBezierWeighted(final MapLinkData data)
    {
        data.getUndo().startAction(ActionType.ATTRIBUTE_CHANGE, data.getShapeNode(), "Weighted");
        data.getShapeNode().setAttributeValue("Weighted",
                data.getShapeNode().getAttributeValueOrDefault("Weighted").equalsIgnoreCase("true") ? "false" : "true");
    }

    /**
     * Minimize the maximum Bezier curvature.
     * @param data map element for which to show the context menu
     */
    private static void minimizeBezierCurvature(final MapLinkData data)
    {
        boolean weighted = data.getShapeNode().getAttributeValueOrDefault("Weighted").equalsIgnoreCase("true");
        Ray2d fromPoint = new Ray2d(data.getStartPoint(), data.getStartDirection().si);
        Ray2d toPoint = new Ray2d(data.getEndPoint(), data.getEndDirection().si);
        if (data.getStartOffset() != null)
        {
            fromPoint = OtsGeometryUtil.offsetRay(fromPoint, data.getStartOffset().si);
        }
        if (data.getEndOffset() != null)
        {
            toPoint = OtsGeometryUtil.offsetRay(toPoint, data.getEndOffset().si);
        }
        double s = optimalBezierShape(fromPoint, toPoint, weighted);
        data.getUndo().startAction(ActionType.ATTRIBUTE_CHANGE, data.getShapeNode(), "Shape");
        data.getShapeNode().setAttributeValue("Shape", Double.toString(s));
    }

    /**
     * Computes the optimal Bezier shape value that minimizes the maximum curvature.
     * @param start start ray of design line
     * @param end end ray of design line
     * @param weighted whether the Bezier shape is weighted
     * @return optimal Bezier shape value that minimizes the maximum curvature
     */
    private static double optimalBezierShape(final Ray2d start, final Ray2d end, final boolean weighted)
    {
        final int n = 4;
        final int m = 20;
        double lo = MapBezierDraggablesHelper.BEZIES_SHAPE_MIN;
        double hi = MapBezierDraggablesHelper.BEZIES_SHAPE_MAX;
        // each level searches "m" points spanning range (lo, hi) with spacing ds = (hi - lo)/m finding best point "s"
        // for the next level the range is narrowed to (s-ds, s+ds), i.e. between adjacent evaluations around "s"
        for (int level = 0; level < n; level++)
        {
            double bestK = Double.POSITIVE_INFINITY;
            double bestS = lo;
            for (int i = 0; i <= m; i++)
            {
                double s = lo + (hi - lo) * i / m;
                double k = maxBezierCurvature(start, end, s, weighted);
                if (k < bestK)
                {
                    bestK = k;
                    bestS = s;
                }
            }
            double span = (hi - lo) / m;
            lo = Math.max(MapBezierDraggablesHelper.BEZIES_SHAPE_MIN, bestS - span);
            hi = bestS + span;
        }
        return 0.5 * (lo + hi);
    }

    /**
     * Computes the maximum curvature of a Bezier numerically by evaluating 101 points linear in Bezier "t-space".
     * @param start start ray of design line
     * @param end end ray of design line
     * @param s shape
     * @param weighted whether the Bezier shape is weighted
     * @return maximum curvature of a Bezier
     */
    private static double maxBezierCurvature(final Ray2d start, final Ray2d end, final double s, final boolean weighted)
    {
        BezierCubic2d bezier = new BezierCubic2d(start, end, s, weighted);
        double k = 0;
        int m = 100;
        for (int j = 0; j <= m; j++)
        {
            k = Math.max(k, Math.abs(bezier.curvature((double) j / m)));
        }
        return k;
    }

    /**
     * Represents any name as a menu friendly name.
     * @param name name
     * @return menu friendly name
     */
    private static String menuFriendlyName(final String name)
    {
        if (name == null || name.isBlank())
        {
            return name;
        }
        String result = name.replaceAll("_+", " ").trim().toLowerCase();
        return Character.toUpperCase(result.charAt(0)) + result.substring(1);
    }

    /**
     * Sets link shape.
     * @param linkData link data
     * @param shape shape
     */
    private static void setShape(final MapLinkData linkData, final String shape)
    {
        linkData.getUndo().startAction(ActionType.OPTION, linkData.getShapeNode(), null);
        linkData.getShapeNode().setOption(linkData.getShapeNode().getOptions().stream().map((o) -> o.optionNode())
                .filter((n) -> n.getNodeName().equals(shape)).findFirst().get());
    }

    /**
     * Sets link priority.
     * @param dataList map elements on which to set the priority
     * @param priority priority
     */
    private static void setPriority(final List<MapData> dataList, final Priority priority)
    {
        dataList.get(0).getUndo().startAction(ActionType.ATTRIBUTE_CHANGE,
                dataList.size() == 1 ? dataList.get(0).getNode() : dataList.get(0).getNode().getParent(),
                dataList.size() == 1 ? "Priority" : null);
        dataList.forEach((data) -> data.getNode().setAttributeValue("Priority",
                priority.equals(Priority.NONE) ? null : priority.name()));
    }

}
