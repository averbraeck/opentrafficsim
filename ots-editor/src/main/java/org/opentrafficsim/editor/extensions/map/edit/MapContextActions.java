package org.opentrafficsim.editor.extensions.map.edit;

import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.SwingUtilities;

import org.djunits.value.vdouble.scalar.Direction;
import org.djunits.value.vdouble.scalar.Length;
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
        visualizationPanel.showContextMenu(menu, event);
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
        Icon autoFix = IconUtil.of("AutoFix24.png").imageSize(OtsEditor.ICON_SIZE, OtsEditor.ICON_SIZE).get();
        if (data.isStraight())
        {
            addContextMenuItem(menu, "Align node directions", autoFix,
                    () -> alignStraightNodeDirections(editorMap, data, new HashSet<>()), data.getEditor());
        }
        else if (data.isArc())
        {
            if (data.getStartDirection() != null)
            {
                addContextMenuItem(menu, "Align with nodes and start direction", autoFix,
                        () -> alignArc(editorMap, data, true, new HashSet<>()), data.getEditor());
            }
            if (data.getEndDirection() != null)
            {
                addContextMenuItem(menu, "Align with nodes and end direction", autoFix,
                        () -> alignArc(editorMap, data, false, new HashSet<>()), data.getEditor());
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
            Icon icon = IconUtil.of("Node24.png").imageSize(OtsEditor.ICON_SIZE, OtsEditor.ICON_SIZE).get();
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
        shapeMenu.setIcon(IconUtil.of("Bezier24.png").imageSize(OtsEditor.ICON_SIZE, OtsEditor.ICON_SIZE).get());
        Icon check = IconUtil.of("Check24.png").imageSize(OtsEditor.ICON_SIZE, OtsEditor.ICON_SIZE).get();
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
        priorityMenu.setIcon(IconUtil.of("Priority24.png").imageSize(OtsEditor.ICON_SIZE, OtsEditor.ICON_SIZE).get());
        menu.add(priorityMenu);
        IconUtil util = IconUtil.of("Check24.png").imageSize(OtsEditor.ICON_SIZE, OtsEditor.ICON_SIZE);
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
     * @param map editor map
     * @param data map element for which to show the context menu
     * @param visited visited nodes
     */
    static void alignStraightNodeDirections(final EditorMap map, final MapLinkData data, final Set<XsdTreeNode> visited)
    {
        alignStraightNodeDirections(map, data, true, visited);
    }

    /**
     * Aligns the directions of both connected nodes with the straight link.
     * @param map editor map
     * @param data map element for which to show the context menu
     * @param interactive whether to show a dialog when there is no valid geometry
     * @param visited visited nodes
     */
    private static void alignStraightNodeDirections(final EditorMap map, final MapLinkData data, final boolean interactive,
            final Set<XsdTreeNode> visited)
    {
        Direction direction =
                straightDirection(data.getStartPoint(), data.getEndPoint(), data.getStartOffset(), data.getEndOffset());
        if (direction == null)
        {
            if (interactive)
            {
                noValidGeometryDialog(data);
            }
            return;
        }
        if (interactive)
        {
            data.getUndo().startAction(ActionType.ATTRIBUTE_CHANGE, data.getNode(), null);
        }
        setNodeDirection(map, data.getStartNode(), direction, visited);
        setNodeDirection(map, data.getEndNode(), direction, visited);
    }

    /**
     * Returns the direction of the straight design line that is tangent to the offset circle at both points. Both offsets are
     * measured perpendicular to the returned direction, positive to the left. With {@code n} the left normal of the direction
     * {@code theta}, the tangency points are {@code from + startOffset * n} and {@code to + endOffset * n}. Both lie on the
     * design line if {@code (to - from) . n == startOffset - endOffset}, which with {@code d} the distance between the points
     * and {@code phi} the direction from one point to the other boils down to
     * {@code d * sin(phi - theta) == diff == startOffset - endOffset}. The solution near {@code phi} is
     * {@code theta = phi - asin(diff / d)}.
     * @param from from point
     * @param to to point
     * @param startOffset offset at the from point, may be {@code null} for no offset
     * @param endOffset offset at the to point, may be {@code null} for no offset
     * @return direction of the design line through both offset points, or {@code null} if the points are not available,
     *         coincide, or the offsets are too large for a common tangent line
     */
    private static Direction straightDirection(final Point2d from, final Point2d to, final Length startOffset,
            final Length endOffset)
    {
        if (from == null || to == null)
        {
            return null;
        }
        double dx = to.x - from.x;
        double dy = to.y - from.y;
        double distance = Math.hypot(dx, dy);
        double diff = (startOffset == null ? 0.0 : startOffset.si) - (endOffset == null ? 0.0 : endOffset.si);
        if (distance < 1e-9 || Math.abs(diff) >= distance)
        {
            return null;
        }
        return Direction.ofSI(Math.atan2(dy, dx) - Math.asin(diff / distance));
    }

    /**
     * Aligns the arc with the coordinates and offsets of both nodes and the direction of one of the nodes. The radius and
     * direction of the arc are set, as well as the direction of the node that is not kept.
     * @param map editor map
     * @param data map element for which to show the context menu
     * @param keepStart whether to keep the start node direction, otherwise the end node direction is kept
     * @param visited visited nodes
     */
    static void alignArc(final EditorMap map, final MapLinkData data, final boolean keepStart, final Set<XsdTreeNode> visited)
    {
        alignArc(map, data, keepStart, true, visited);
    }

    /**
     * Aligns the arc with the coordinates and offsets of both nodes and the direction of one of the nodes. The radius and
     * direction of the arc are set, as well as the direction of the node that is not kept.
     * @param map editor map
     * @param data map element for which to show the context menu
     * @param keepStart whether to keep the start node direction, otherwise the end node direction is kept
     * @param interactive whether to show a dialog when there is no valid geometry
     * @param visited visited nodes
     */
    private static void alignArc(final EditorMap map, final MapLinkData data, final boolean keepStart,
            final boolean interactive, final Set<XsdTreeNode> visited)
    {
        Point2d pointStart = data.getStartPoint();
        Point2d pointEnd = data.getEndPoint();
        if (pointStart == null || pointEnd == null)
        {
            return;
        }
        Direction kept = keepStart ? data.getStartDirection() : data.getEndDirection();
        if (kept == null)
        {
            return;
        }
        Point2d keptPoint = keepStart ? pointStart : pointEnd;
        Point2d otherPoint = keepStart ? pointEnd : pointStart;
        Length keptOffset = keepStart ? data.getStartOffset() : data.getEndOffset();
        Length otherOffset = keepStart ? data.getEndOffset() : data.getStartOffset();
        double ok = keptOffset == null ? 0.0 : keptOffset.si;
        double oa = otherOffset == null ? 0.0 : otherOffset.si;
        double dx = otherPoint.x - keptPoint.x;
        double dy = otherPoint.y - keptPoint.y;
        double lengthSquared = dx * dx + dy * dy;
        if (lengthSquared < 1e-9)
        {
            if (interactive)
            {
                noValidGeometryDialog(data);
            }
            return;
        }
        double sin = Math.sin(kept.si);
        double cos = Math.cos(kept.si);
        // dot product of the chord (from kept point to other point) with the normal at the kept side
        double dot = -dx * sin + dy * cos;
        double diff = ok - oa;
        double sum = ok + oa;
        if (Math.abs(dot - diff) < 1e-9 * Math.sqrt(lengthSquared))
        {
            if (interactive)
            {
                noValidGeometryDialog(data);
            }
            return; // nodes, directions and offsets are collinear, there is no finite arc
        }
        /*
         * The centre of the circle is at keptPoint + (ok + signedRadius) * n, with n the normal at the kept side and
         * signedRadius positive for a left turn and negative for a right turn. At the other side the centre is at (oa +
         * signedRadius) * n from the other node. Equating both centre distances gives a linear equation: d^2 - 2 * ok * dot +
         * (ok - oa) * (ok + oa) = 2 * (dot - ok + oa) * signedRadius.
         */
        double signedRadius = (lengthSquared - 2.0 * dot * ok + diff * sum) / (2.0 * (dot - diff));
        if (Math.abs(signedRadius) < 1e-9)
        {
            if (interactive)
            {
                noValidGeometryDialog(data);
            }
            return; // degenerate arc
        }
        double centreX = keptPoint.x - (ok + signedRadius) * sin;
        double centreY = keptPoint.y + (ok + signedRadius) * cos;
        Direction alignedDirection = null;
        if (Math.abs(oa + signedRadius) >= 1e-9)
        {
            // the normal at the other side points from the other node to the centre, scaled by oa + signedRadius
            double normalX = (centreX - otherPoint.x) / (oa + signedRadius);
            double normalY = (centreY - otherPoint.y) / (oa + signedRadius);
            alignedDirection = Direction.ofSI(Math.atan2(-normalX, normalY));
        }
        if (interactive)
        {
            data.getUndo().startAction(ActionType.ATTRIBUTE_CHANGE, data.getShapeNode(), null);
        }
        data.getShapeNode().setAttributeValue("Radius", Math.abs(signedRadius) + "m");
        data.getShapeNode().setAttributeValue("Direction",
                signedRadius > 0.0 ? ArcDirection.LEFT.toString() : ArcDirection.RIGHT.toString());
        if (alignedDirection != null)
        {
            XsdTreeNode node = keepStart ? data.getEndNode() : data.getStartNode();
            setNodeDirection(map, node, alignedDirection, visited);
        }
    }

    /**
     * Shows a dialog that there is no valid geometry.
     * @param data link data for which the geometry was attempted to be calculated
     */
    private static void noValidGeometryDialog(final MapLinkData data)
    {
        data.getEditor().dialogs().notification("There is no solution for given node positions and link start and end offest.",
                "No solution");
    }

    /**
     * Sets the direction attribute of a node. This does not start an undo action.
     * @param map editor map
     * @param node node
     * @param direction direction
     * @param visited visited nodes
     */
    private static void setNodeDirection(final EditorMap map, final XsdTreeNode node, final Direction direction,
            final Set<XsdTreeNode> visited)
    {
        if (node != null && visited.add(node) && visited.size() < 1000)
        {
            node.setAttributeValue("Direction", Math.toDegrees(direction.si) + " deg");
            // this is where auto-fix can cascade through the network, only adjusting node directions and never positions
            autoFixNodeDirectionChanged(map, node, visited);
        }
    }

    /**
     * Aligns the first or last intermediate polyline coordinate with the line from the node in the direction of the node at the
     * given end, taking link offset into account. The coordinate is projected onto that line closest to its current position.
     * The coordinate is not moved if there are fewer than two intermediate coordinates, or if the projected point is closer
     * than {@link MapInputListener#DIST_MIN_M} to a neighboring point.
     * @param data link data
     * @param atStart {@code true} to align the first coordinate, {@code false} to align the last coordinate
     */
    private static void alignPolyLineCoordinate(final MapLinkData data, final boolean atStart)
    {
        NavigableMap<XsdTreeNode, Point2d> coordinates = data.getPolyLineCoordinates();
        if (coordinates.size() < 2)
        {
            return;
        }
        XsdTreeNode coordNode;
        Point2d current;
        Point2d neighbor;
        Point2d boundary;
        Direction direction;
        if (atStart)
        {
            Map.Entry<XsdTreeNode, Point2d> first = coordinates.firstEntry();
            coordNode = first.getKey();
            current = first.getValue();
            neighbor = coordinates.higherEntry(coordNode).getValue();
            boundary = data.getOffsetStartPoint();
            direction = data.getStartDirection();
        }
        else
        {
            Map.Entry<XsdTreeNode, Point2d> last = coordinates.lastEntry();
            coordNode = last.getKey();
            current = last.getValue();
            neighbor = coordinates.lowerEntry(coordNode).getValue();
            boundary = data.getOffsetEndPoint();
            direction = data.getEndDirection();
        }
        if (current == null || neighbor == null || boundary == null || direction == null)
        {
            return;
        }
        double cos = Math.cos(direction.si);
        double sin = Math.sin(direction.si);
        double t = (current.x - boundary.x) * cos + (current.y - boundary.y) * sin;
        if ((t <= 0.0 && atStart) || (t >= 0.0 && !atStart))
        {
            return;
        }
        Point2d projected = new Point2d(boundary.x + t * cos, boundary.y + t * sin);
        if (projected.distance(boundary) < MapInputListener.DIST_MIN_M
                || projected.distance(neighbor) < MapInputListener.DIST_MIN_M)
        {
            return;
        }
        // no undo, this is always part of another change
        coordNode.setValue(String.format(Locale.US, "(%f,%f)", projected.x, projected.y));
    }

    /**
     * Returns all link data attached to the given node.
     * @param map editor map
     * @param node node
     * @return attached link data
     */
    static List<MapLinkData> getAttachedLinks(final EditorMap map, final XsdTreeNode node)
    {
        List<MapLinkData> links = new ArrayList<>();
        XsdTreeNode network = map.getNetworkNode();
        if (network == null)
        {
            return links;
        }
        for (XsdTreeNode child : network.getChildren())
        {
            if (!XsdPaths.LINK.equals(child.getPathString()))
            {
                continue;
            }
            MapData data = map.getData(child).orElse(null);
            if (data instanceof MapLinkData linkData
                    && (node.equals(linkData.getStartNode()) || node.equals(linkData.getEndNode())))
            {
                links.add(linkData);
            }
        }
        return links;
    }

    /**
     * Applies auto-fix for a newly added link, if enabled and applicable.
     * @param map editor map
     * @param linkNode link node
     * @param startNode start node
     * @param endNode end node
     */
    static void autoFixAddedLink(final EditorMap map, final XsdTreeNode linkNode, final XsdTreeNode startNode,
            final XsdTreeNode endNode)
    {
        if (!map.isAutoFixMode() || linkNode == null)
        {
            return;
        }
        SwingUtilities.invokeLater(() ->
        {
            MapData data = map.getData(linkNode).orElse(null);
            if (!(data instanceof MapLinkData linkData) || !linkData.isValid())
            {
                return;
            }
            if (linkData.isStraight())
            {
                alignStraightNodeDirections(map, linkData, false, new HashSet<>());
            }
            else if (linkData.isArc())
            {
                int startAttached = getAttachedLinks(map, startNode).size();
                int endAttached = getAttachedLinks(map, endNode).size();
                alignArc(map, linkData, startAttached >= endAttached, false, new HashSet<>());
            }
        });
    }

    /**
     * Applies auto-fix after a node was repositioned.
     * @param map editor map
     * @param node node
     */
    static void autoFixNodeRepositioned(final EditorMap map, final XsdTreeNode node)
    {
        if (!map.isAutoFixMode() || node == null)
        {
            return;
        }
        SwingUtilities.invokeLater(() ->
        {
            Set<XsdTreeNode> visited = new HashSet<>();
            for (MapLinkData linkData : getAttachedLinks(map, node))
            {
                boolean atStart = node.equals(linkData.getStartNode());
                boolean atEnd = node.equals(linkData.getEndNode());
                if (linkData.isStraight())
                {
                    alignStraightNodeDirections(map, linkData, false, visited);
                }
                if (linkData.isArc())
                {
                    if (atStart)
                    {
                        alignArc(map, linkData, false, false, visited);
                    }
                    else if (atEnd)
                    {
                        alignArc(map, linkData, true, false, visited);
                    }
                }
                if (linkData.isPolyLine())
                {
                    if (atStart)
                    {
                        alignPolyLineCoordinate(linkData, true);
                    }
                    else if (atEnd)
                    {
                        alignPolyLineCoordinate(linkData, false);
                    }
                }
            }
        });
    }

    /**
     * Applies auto-fix after a node direction changed.
     * @param map editor map
     * @param node node
     * @param visited visited nodes
     */
    static void autoFixNodeDirectionChanged(final EditorMap map, final XsdTreeNode node, final Set<XsdTreeNode> visited)
    {
        if (!map.isAutoFixMode() || node == null)
        {
            return;
        }
        SwingUtilities.invokeLater(() ->
        {
            for (MapLinkData linkData : getAttachedLinks(map, node))
            {
                boolean atStart = node.equals(linkData.getStartNode());
                boolean atEnd = node.equals(linkData.getEndNode());
                if (linkData.isArc())
                {
                    if (atStart)
                    {
                        alignArc(map, linkData, true, false, visited);
                    }
                    else if (atEnd)
                    {
                        alignArc(map, linkData, false, false, visited);
                    }
                }
                if (linkData.isPolyLine())
                {
                    if (atStart)
                    {
                        alignPolyLineCoordinate(linkData, true);
                    }
                    else if (atEnd)
                    {
                        alignPolyLineCoordinate(linkData, false);
                    }
                }
            }
        });
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
