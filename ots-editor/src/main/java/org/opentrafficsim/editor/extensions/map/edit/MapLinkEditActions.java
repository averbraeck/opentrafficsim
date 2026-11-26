package org.opentrafficsim.editor.extensions.map.edit;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.NavigableMap;
import java.util.Optional;
import java.util.Set;

import org.djunits.value.vdouble.scalar.Length;
import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.djutils.eval.Eval;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.Undo.ActionType;
import org.opentrafficsim.editor.XsdPaths;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.decoration.validation.KeyValidator;
import org.opentrafficsim.editor.extensions.Adapters;
import org.opentrafficsim.editor.extensions.map.EditorMap;
import org.opentrafficsim.editor.extensions.map.MapLinkData;
import org.opentrafficsim.road.network.factory.xml.utils.ParseUtil;
import org.opentrafficsim.xml.bindings.types.LengthBeginEndType;
import org.opentrafficsim.xml.bindings.types.LengthBeginEndType.LengthBeginEnd;

/**
 * Utility actions for link split and merge operations.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
final class MapLinkEditActions
{

    /** Utility class. */
    private MapLinkEditActions()
    {
        //
    }

    /**
     * Split a non-connector link at the clicked screen location.
     * @param editorMap editor map
     * @param linkData selected link data
     * @param screenPoint clicked screen location
     * @return new node representing second new part, or {@code null} if not split for some reason
     */
    static XsdTreeNode splitLink(final EditorMap editorMap, final MapLinkData linkData, final Point2D screenPoint)
    {
        if (linkData == null || linkData.isConnector() || screenPoint == null)
        {
            return null;
        }
        Point2d world = editorMap.getSnappedWorldCoordinate(screenPoint);
        if (world == null)
        {
            return null;
        }
        PolyLine2d centerLine = linkData.getCenterLine();
        if (centerLine == null)
        {
            return null;
        }
        double len = centerLine.getLength();
        double min = MapInputListener.DIST_MIN_M / len;
        double max = (len - MapInputListener.DIST_MIN_M) / len;
        double f = centerLine.projectOrthogonalFractionalExtended(world);
        if (f < min || f > max)
        {
            linkData.getEditor().dialogs().notification(
                    "The split location is " + (f > max ? "beyond the end" : "before the start") + " of the link",
                    "Invalid split location");
            return null;
        }
        DirectedPoint2d split = centerLine.getLocationFraction(f);
        if (split == null)
        {
            return null;
        }

        XsdTreeNode linkNode = linkData.getNode();
        String startId = linkNode.getAttributeValueOrDefault("NodeStart");
        String endId = linkNode.getAttributeValueOrDefault("NodeEnd");
        if (startId == null || endId == null)
        {
            return null;
        }

        XsdTreeNode addedNode = MapAdder.addPoint(linkData.getEditor(), editorMap, "(" + split.x + "," + split.y + ")", false,
                linkData.getNode());
        if (addedNode == null || addedNode.getId() == null)
        {
            return null;
        }
        addedNode.setAttributeValue("Direction", Math.toDegrees(split.dirZ) + " deg");

        List<XsdTreeNode> latterCoordinateNodes = new ArrayList<>();
        int formerCoordinateNodes = 0;
        if (linkData.isPolyLine())
        {
            NavigableMap<XsdTreeNode, Point2d> polyCoords = linkData.getPolyLineCoordinates();
            for (Map.Entry<XsdTreeNode, Point2d> entry : polyCoords.entrySet())
            {
                Point2d point = entry.getValue();
                if (point == null)
                {
                    continue;
                }
                double cf = centerLine.projectOrthogonalFractionalExtended(point);
                if (cf >= f)
                {
                    latterCoordinateNodes.add(entry.getKey());
                }
                else
                {
                    formerCoordinateNodes++;
                }
            }
        }

        String oldLinkId = linkNode.getId();
        String newNodeId = addedNode.getId();

        String formerLinkId;
        String latterLinkId;
        if ((startId + "-" + endId).equals(oldLinkId))
        {
            formerLinkId = startId + "-" + newNodeId;
            latterLinkId = newNodeId + "-" + endId;
        }
        else
        {
            Set<String> usedLinkIds = collectLinkIds(linkNode.getParent());
            usedLinkIds.remove(oldLinkId);
            formerLinkId = uniqueAppendedId(oldLinkId, usedLinkIds);
            usedLinkIds.add(formerLinkId);
            latterLinkId = uniqueAppendedId(oldLinkId, usedLinkIds);
        }

        linkNode.setAttributeValue("NodeEnd", newNodeId);
        XsdTreeNode newLink = linkNode.duplicate();
        newLink.setAttributeValue("NodeStart", newNodeId);
        newLink.setAttributeValue("NodeEnd", endId);
        newLink.setId(latterLinkId);

        assignSplitChildren(linkNode, newLink, f, len, linkData.getEditor().getEval(), latterLinkId);

        if (formerCoordinateNodes > 0)
        {
            XsdTreeNode newShapeNode = newLink.getChildCount() > 0 ? newLink.getChild(0) : null;
            if (newShapeNode != null)
            {
                for (int i = 0; i < formerCoordinateNodes; i++)
                {
                    if (newShapeNode.getChildCount() > 0)
                    {
                        newShapeNode.getChild(0).remove();
                    }
                }
            }
        }
        if (!latterCoordinateNodes.isEmpty())
        {
            for (XsdTreeNode coordNodeOrigLink : latterCoordinateNodes)
            {
                if (coordNodeOrigLink.getParent() != null)
                {
                    coordNodeOrigLink.remove();
                }
            }
        }

        linkNode.setId(formerLinkId);
        return newLink;
    }

    /**
     * Assign children that are located along a link to the correct part after the link has been split. Any direct child of the
     * link with both a {@code Lane} and a {@code Position} attribute is considered, e.g. {@code TrafficLight} and
     * {@code TrafficLightSensor}. Children at or beyond the split location remain on the duplicated link representing the
     * latter part, with their {@code Position} recalculated for that part. Children before the split location remain on the
     * original link. This method is generic in the child type; no specific child names are used.
     * @param linkNode original link node, representing the former part
     * @param newLink duplicate of the original link node, representing the latter part
     * @param splitFraction fractional location of the split on the original link
     * @param linkLength length of the original link
     * @param eval expression evaluator used to parse {@code Position} values, may be {@code null}
     * @param newLinkId id that the latter link will receive, used to shift couplings to children that remain on that link
     */
    private static void assignSplitChildren(final XsdTreeNode linkNode, final XsdTreeNode newLink, final double splitFraction,
            final double linkLength, final Eval eval, final String newLinkId)
    {
        if (linkNode == null || newLink == null)
        {
            return;
        }
        List<XsdTreeNode> originalChildren = findPositionedLinkChildren(linkNode);
        List<XsdTreeNode> copiedChildren = findPositionedLinkChildren(newLink);
        for (int index = 0; index < Math.min(originalChildren.size(), copiedChildren.size()); index++)
        {
            XsdTreeNode original = originalChildren.get(index);
            XsdTreeNode copy = copiedChildren.get(index);
            String positionValue = original.getAttributeValueOrDefault("Position");
            LengthBeginEnd position = Adapters.parseOrNull(eval, positionValue, LengthBeginEnd.class);
            double positionFraction = positionFraction(position, linkLength);
            if (!Double.isNaN(positionFraction) && positionFraction >= splitFraction)
            {
                String adaptedPosition = adaptSplitPosition(position, splitFraction, linkLength);
                shiftCouplings(linkNode, original, newLinkId);
                if (original.getParent() != null)
                {
                    original.remove();
                }
                copy.setAttributeValue("Position", adaptedPosition);
            }
            else if (copy.getParent() != null)
            {
                copy.remove();
            }
        }
    }

    /**
     * Returns the fractional location along the link of a position, or {@code NaN} if the position cannot be resolved.
     * @param position position, may be {@code null}
     * @param linkLength length of the link
     * @return fractional location along the link, or {@code NaN}
     */
    private static double positionFraction(final LengthBeginEnd position, final double linkLength)
    {
        if (position == null || linkLength <= 0.0)
        {
            return Double.NaN;
        }
        return ParseUtil.parseLengthBeginEnd(position, Length.ofSI(linkLength)).si / linkLength;
    }

    /**
     * Returns the position expressed for the latter part of a split link. An offset from the end does not change as the end of
     * the link remains the same. A fraction or an offset from the begin is recalculated against the begin of the latter link.
     * @param position parsed original position
     * @param splitFraction fractional location of the split on the original link
     * @param linkLength length of the original link
     * @return adapted position attribute value
     */
    private static String adaptSplitPosition(final LengthBeginEnd position, final double splitFraction, final double linkLength)
    {
        if (!position.isAbsolute())
        {
            if (splitFraction >= 1.0)
            {
                return marshalPosition(new LengthBeginEnd(0.0));
            }
            double fraction = (position.getFraction() - splitFraction) / (1.0 - splitFraction);
            return marshalPosition(new LengthBeginEnd(Math.max(0.0, Math.min(1.0, fraction))));
        }
        if (!position.isBegin())
        {
            return marshalPosition(position);
        }
        double offset = position.getOffset().si - splitFraction * linkLength;
        return marshalPosition(new LengthBeginEnd(true, Length.ofSI(Math.max(0.0, offset))));
    }

    /**
     * Merge adjacent upstream and downstream links when deleting a node. Coordinates and positioned children of the downstream
     * link are assigned to the upstream link, and references to moved children are changed to the remaining link ID.
     * @param editor editor
     * @param editorMap editor map
     * @param node node to be deleted
     * @return whether merge was performed
     */
    static Optional<Boolean> tryMergeNodeOnDelete(final OtsEditor editor, final EditorMap editorMap, final XsdTreeNode node)
    {
        if (node == null || !XsdPaths.NODE.equals(node.getPathString()) || !node.isActive() || node.getParent() == null
                || node.getId() == null)
        {
            return Optional.of(false);
        }

        XsdTreeNode upstream = null;
        XsdTreeNode downstream = null;
        for (XsdTreeNode element : node.getParent().getChildren())
        {
            if (!element.isActive() || !XsdPaths.LINK.equals(element.getPathString()))
            {
                continue;
            }
            Optional<XsdTreeNode> end = element.getCoupledNodeAttribute("NodeEnd");
            if (end.isPresent() && node.getId().equals(end.get().getId()))
            {
                if (upstream != null)
                {
                    return Optional.of(false); // not a node on a corridor; we can't merge the links
                }
                upstream = element;
            }
            Optional<XsdTreeNode> start = element.getCoupledNodeAttribute("NodeStart");
            if (start.isPresent() && node.getId().equals(start.get().getId()))
            {
                if (downstream != null)
                {
                    return Optional.of(false); // not a node on a corridor; we can't merge the links
                }
                downstream = element;
            }
        }
        if (upstream == null || downstream == null)
        {
            return Optional.of(false);
        }

        String nodeId = node.getId();
        String upstreamNodeId = upstream.getAttributeValueOrDefault("NodeStart");
        String downstreamNodeId = downstream.getAttributeValueOrDefault("NodeEnd");
        if (upstreamNodeId == null || downstreamNodeId == null)
        {
            return Optional.of(false);
        }
        String upstreamId = upstream.getId();

        MapLinkData upstreamData = (MapLinkData) editorMap.getData(upstream).orElse(null);
        MapLinkData downstreamData = (MapLinkData) editorMap.getData(downstream).orElse(null);
        if (upstreamData == null || !upstreamData.isValid() || downstreamData == null || !downstreamData.isValid())
        {
            return Optional.of(false);
        }

        Optional<Boolean> ans =
                editor.dialogs().confirmYesNo("Merge adjacent links while deleting node '" + node.getId() + "'?");
        if (ans.isEmpty() || !ans.get())
        {
            return ans;
        }

        double upstreamLength = upstreamData.getCenterLine().getLength();
        double downstreamLength = downstreamData.getCenterLine().getLength();
        List<XsdTreeNode> downstreamCoordinates = collectCoordinateNodes(downstream, downstreamData);

        editor.getUndo().startAction(ActionType.REMOVE, node.getParent(), null);
        upstream.setAttributeValue("NodeEnd", downstreamNodeId);
        if ((upstreamNodeId + "-" + nodeId).equals(upstreamId))
        {
            Set<String> usedLinkIds = collectLinkIds(upstream.getParent());
            String candidate = upstreamNodeId + "-" + downstreamNodeId;
            if (usedLinkIds.contains(candidate))
            {
                candidate = uniqueAppendedId(candidate, usedLinkIds);
            }
            upstream.setId(candidate);
        }
        String remainingLinkId = upstream.getId();

        mergeCoordinates(upstream, downstreamCoordinates);
        movePositionedChildren(upstream, downstream, upstreamLength, downstreamLength, remainingLinkId, editor.getEval());
        downstream.remove();
        node.remove();
        return Optional.of(true);
    }

    /**
     * Returns the explicit polyline coordinate nodes of a link.
     * @param linkNode link node
     * @param linkData map data of the link
     * @return explicit coordinate nodes in polyline order
     */
    private static List<XsdTreeNode> collectCoordinateNodes(final XsdTreeNode linkNode, final MapLinkData linkData)
    {
        List<XsdTreeNode> coordinates = new ArrayList<>();
        if (linkData != null && linkData.isPolyLine())
        {
            coordinates.addAll(linkData.getPolyLineCoordinates().keySet());
        }
        else
        {
            XsdTreeNode shape = linkNode == null || linkNode.getChildCount() == 0 ? null : linkNode.getChild(0);
            if (shape != null && "PolyLine".equals(shape.getNodeName()))
            {
                for (XsdTreeNode child : shape.getChildren())
                {
                    if ("Coordinate".equals(child.getNodeName()))
                    {
                        coordinates.add(child);
                    }
                }
            }
        }
        return coordinates;
    }

    /**
     * Merges explicit polyline coordinates into the {@code PolyLine} shape of the remaining link, after any coordinates it
     * already has. Coordinates are ignored if the remaining link does not have a {@code PolyLine} shape.
     * @param upstream link node that remains after the merge
     * @param coordinates coordinate nodes of the link that is removed
     */
    private static void mergeCoordinates(final XsdTreeNode upstream, final List<XsdTreeNode> coordinates)
    {
        if (coordinates.isEmpty())
        {
            return;
        }
        XsdTreeNode target = upstream == null || upstream.getChildCount() == 0 ? null : upstream.getChild(0);
        if (target == null || !"PolyLine".equals(target.getNodeName()))
        {
            return; // merged link will have the shape of the upstream link; ignore coordinates of the downstream link
        }
        int index = target.getChildCount();
        for (XsdTreeNode coordinate : coordinates)
        {
            if (coordinate.getParent() == null)
            {
                continue;
            }
            coordinate.createCopy(target, index++);
            coordinate.remove();
        }
    }

    /**
     * Moves positioned children of the downstream link to the upstream link, adapts their positions to the merged link and
     * shifts references to the moved children to the remaining link ID.
     * @param upstream link node that remains after the merge
     * @param downstream link node that is removed
     * @param upstreamLength length of the upstream link
     * @param downstreamLength length of the downstream link
     * @param remainingLinkId ID of the remaining link
     * @param eval expression evaluator used to parse {@code Position} values, may be {@code null}
     */
    private static void movePositionedChildren(final XsdTreeNode upstream, final XsdTreeNode downstream,
            final double upstreamLength, final double downstreamLength, final String remainingLinkId, final Eval eval)
    {
        List<XsdTreeNode> originals = findPositionedLinkChildren(downstream);
        List<XsdTreeNode> copies = new ArrayList<>();
        for (XsdTreeNode original : originals)
        {
            copies.add(original.createCopy(upstream, upstream.getChildCount()));
        }
        for (XsdTreeNode original : originals)
        {
            shiftCouplings(downstream, original, remainingLinkId);
            original.remove();
        }
        double mergedLength = upstreamLength + downstreamLength;
        for (XsdTreeNode copy : copies)
        {
            LengthBeginEnd position =
                    Adapters.parseOrNull(eval, copy.getAttributeValueOrDefault("Position"), LengthBeginEnd.class);
            String adaptedPosition = adaptMergePosition(position, upstreamLength, downstreamLength, mergedLength);
            if (adaptedPosition != null)
            {
                copy.setAttributeValue("Position", adaptedPosition);
            }
        }
    }

    /**
     * Returns the position expressed for a merged link. The merged link starts at the start of the upstream link and ends at
     * the end of the downstream link. An offset from the begin is increased with the upstream length, a fraction is rescaled,
     * and an offset from the end is unchanged.
     * @param position parsed original position
     * @param upstreamLength length of the upstream link
     * @param downstreamLength length of the downstream link
     * @param mergedLength length of the merged link
     * @return adapted position attribute value, or {@code null} if the position cannot be adapted
     */
    private static String adaptMergePosition(final LengthBeginEnd position, final double upstreamLength,
            final double downstreamLength, final double mergedLength)
    {
        if (position == null || Double.isNaN(upstreamLength) || Double.isNaN(downstreamLength) || mergedLength <= 0.0)
        {
            return null;
        }
        if (!position.isAbsolute())
        {
            double fraction = (upstreamLength + position.getFraction() * downstreamLength) / mergedLength;
            return marshalPosition(new LengthBeginEnd(Math.max(0.0, Math.min(1.0, fraction))));
        }
        if (!position.isBegin())
        {
            return marshalPosition(position);
        }
        double offset = upstreamLength + position.getOffset().si;
        return marshalPosition(new LengthBeginEnd(true, Length.ofSI(offset)));
    }

    // Utility methods for both splitting and merging

    /**
     * Collects all active link IDs in the network.
     * @param network network node
     * @return all active link IDs in the network
     */
    private static Set<String> collectLinkIds(final XsdTreeNode network)
    {
        Set<String> ids = new LinkedHashSet<>();
        if (network == null)
        {
            return ids;
        }
        for (XsdTreeNode element : network.getChildren())
        {
            if (element.isActive() && XsdPaths.LINK.equals(element.getPathString()) && element.getId() != null)
            {
                ids.add(element.getId());
            }
        }
        return ids;
    }

    /**
     * Returns a unique ID by appending "(#)" to the base ID.
     * @param baseId base ID
     * @param usedIds IDs that are already used
     * @return unique ID by appending "(#)" to the base ID
     */
    private static String uniqueAppendedId(final String baseId, final Set<String> usedIds)
    {
        String base = baseId == null || baseId.isBlank() ? "Link" : baseId;
        int index = 1;

        int open = base.lastIndexOf('(');
        int close = base.endsWith(")") ? base.length() - 1 : -1;
        if (open >= 1 && close > open + 1)
        {
            String numberPart = base.substring(open + 1, close);
            if (numberPart.chars().allMatch(Character::isDigit))
            {
                index = Integer.parseInt(numberPart);
                base = base.substring(0, open);
            }
        }

        String candidate;
        do
        {
            candidate = base + "(" + index + ")";
            index++;
        }
        while (usedIds.contains(candidate));
        return candidate;
    }

    /**
     * Returns all direct positioned children of the given link node, i.e. children that have both a {@code Lane} and a
     * {@code Position} attribute.
     * @param linkNode link node
     * @return positioned children of the link node
     */
    private static List<XsdTreeNode> findPositionedLinkChildren(final XsdTreeNode linkNode)
    {
        List<XsdTreeNode> children = new ArrayList<>();
        for (XsdTreeNode child : linkNode.getChildren())
        {
            if (child.hasAttribute("Lane") && child.hasAttribute("Position"))
            {
                children.add(child);
            }
        }
        return children;
    }

    /**
     * Forwards the moved child and the target link ID to the key validator on the link ID. The validator chain determines which
     * referring nodes are coupled to the moved child's sub-tree and updates their link reference.
     * @param linkNode link node on which the key validator is found
     * @param movedChild child that is assigned to the surviving link
     * @param targetLinkId ID of the surviving link
     */
    private static void shiftCouplings(final XsdTreeNode linkNode, final XsdTreeNode movedChild, final String targetLinkId)
    {
        Optional<KeyValidator> keyValidator = linkNode.getKeyValidator("Id");
        if (keyValidator.isPresent() && targetLinkId != null)
        {
            keyValidator.get().shiftCoupling(movedChild, targetLinkId);
        }
    }

    /**
     * Converts a position into its XML attribute representation.
     * @param position position
     * @return XML attribute value
     */
    private static String marshalPosition(final LengthBeginEnd position)
    {
        return Adapters.get(LengthBeginEnd.class).marshal(new LengthBeginEndType(position));
    }

}
