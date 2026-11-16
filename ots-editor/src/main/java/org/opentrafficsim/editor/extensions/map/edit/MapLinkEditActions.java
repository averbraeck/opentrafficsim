package org.opentrafficsim.editor.extensions.map.edit;

import java.awt.geom.Point2D;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import org.djutils.draw.line.PolyLine2d;
import org.djutils.draw.point.DirectedPoint2d;
import org.djutils.draw.point.Point2d;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.Undo.ActionType;
import org.opentrafficsim.editor.XsdPaths;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.extensions.map.EditorMap;
import org.opentrafficsim.editor.extensions.map.MapLinkData;

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
     */
    static void splitLink(final EditorMap editorMap, final MapLinkData linkData, final Point2D screenPoint)
    {
        if (linkData == null || linkData.isConnector() || screenPoint == null)
        {
            return;
        }
        Point2d world = editorMap.getPanel().getRenderableScale().getWorldCoordinates(screenPoint,
                editorMap.getPanel().getExtent(), editorMap.getPanel().getSize());
        if (world == null)
        {
            return;
        }
        PolyLine2d centerLine = linkData.getCenterLine();
        if (centerLine == null)
        {
            return;
        }

        double f = centerLine.projectOrthogonalFractional(world);
        if (f <= 0.0 || f >= 1.0)
        {
            return;
        }
        DirectedPoint2d split = centerLine.getLocationFraction(f);
        if (split == null)
        {
            return;
        }

        XsdTreeNode linkNode = linkData.getNode();
        String startId = linkNode.getAttributeValueOrDefault("NodeStart");
        String endId = linkNode.getAttributeValueOrDefault("NodeEnd");
        if (startId == null || endId == null)
        {
            return;
        }

        XsdTreeNode addedNode =
                MapAdder.addPoint(linkData.getEditor(), editorMap, "(" + split.x + "," + split.y + ")", false);
        if (addedNode == null || addedNode.getId() == null)
        {
            return;
        }
        addedNode.setAttributeValue("Direction", Math.toDegrees(split.dirZ) + " deg");

        String oldLinkId = linkNode.getId();
        String newNodeId = addedNode.getId();

        linkNode.setAttributeValue("NodeEnd", newNodeId);
        XsdTreeNode newLink = linkNode.add();
        newLink.setAttributeValue("NodeStart", newNodeId);
        newLink.setAttributeValue("NodeEnd", endId);

        newLink.setAttributeValue("Type", linkNode.getAttributeValue("Type"));
        newLink.setAttributeValue("OffsetStart", linkNode.getAttributeValue("OffsetStart"));
        newLink.setAttributeValue("OffsetEnd", linkNode.getAttributeValue("OffsetEnd"));
        newLink.setAttributeValue("LaneKeeping", linkNode.getAttributeValue("LaneKeeping"));
        newLink.setAttributeValue("Priority", linkNode.getAttributeValue("Priority"));

        // TODO: deep copy of shape and road layout
        // TODO: distribute traffic lights over both links
        // TODO: undo - redo leaves invalid visual tree structure

        if ((startId + "-" + endId).equals(oldLinkId))
        {
            linkNode.setId(startId + "-" + newNodeId);
            newLink.setId(newNodeId + "-" + endId);
            return;
        }

        Set<String> usedLinkIds = collectLinkIds(linkNode.getParent());
        usedLinkIds.remove(oldLinkId);
        String first = uniqueAppendedId(oldLinkId, usedLinkIds);
        usedLinkIds.add(first);
        String second = uniqueAppendedId(oldLinkId, usedLinkIds);
        linkNode.setId(first);
        newLink.setId(second);
    }

    /**
     * Merge adjacent incoming and outgoing links when deleting a node.
     * @param editor editor
     * @param node node to be deleted
     * @return whether merge was performed
     */
    static boolean tryMergeNodeOnDelete(final OtsEditor editor, final XsdTreeNode node)
    {
        if (node == null || !XsdPaths.NODE.equals(node.getPathString()) || !node.isActive() || node.getParent() == null
                || node.getId() == null)
        {
            return false;
        }

        XsdTreeNode incoming = null;
        XsdTreeNode outgoing = null;
        for (XsdTreeNode element : node.getParent().getChildren())
        {
            if (!element.isActive() || !XsdPaths.LINK.equals(element.getPathString()))
            {
                continue;
            }
            Optional<XsdTreeNode> end =  element.getCoupledNodeAttribute("NodeEnd");
            if (end.isPresent() && node.getId().equals(end.get().getId()))
            {
                if (incoming != null)
                {
                    return false; // not a node on a corridor; we can't merge the links
                }
                incoming = element;
            }
            Optional<XsdTreeNode> start =  element.getCoupledNodeAttribute("NodeStart");
            if (start.isPresent() && node.getId().equals(start.get().getId()))
            {
                if (outgoing != null)
                {
                    return false; // not a node on a corridor; we can't merge the links
                }
                outgoing = element;
            }
        }
        if (incoming == null || outgoing == null)
        {
            return false;
        }

        if (!editor.dialogs().confirmGeneral("Merge adjacent links while deleting node '" + node.getId() + "'?"))
        {
            return false;
        }

        String nodeId = node.getId();
        String a = incoming.getAttributeValueOrDefault("NodeStart");
        String c = outgoing.getAttributeValueOrDefault("NodeEnd");
        if (a == null || c == null)
        {
            return false;
        }
        String incomingId = incoming.getId();

        editor.getUndo().startAction(ActionType.REMOVE, node.getParent(), null);
        incoming.setAttributeValue("NodeEnd", c);
        if ((a + "-" + nodeId).equals(incomingId))
        {
            incoming.setId(a + "-" + c);
        }
        outgoing.remove();
        node.remove();
        return true;
    }

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
        String candidate;
        do
        {
            candidate = base + "(" + index + ")";
            index++;
        }
        while (usedIds.contains(candidate));
        return candidate;
    }

}
