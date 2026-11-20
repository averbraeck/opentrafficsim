package org.opentrafficsim.editor.extensions.map.edit;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;

import org.opentrafficsim.base.AlphabeticIdGenerator;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.Undo.ActionType;
import org.opentrafficsim.editor.XsdOption;
import org.opentrafficsim.editor.XsdPaths;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.extensions.map.EditorMap;
import org.opentrafficsim.editor.extensions.map.MapNodeData;

/**
 * Functionality to add elements to the network.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.<br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * @author Wouter Schakel
 */
final class MapAdder
{

    /** Utility class. */
    private MapAdder()
    {
        //
    }

    /**
     * Add a node or centroid at a snapped coordinate.
     * @param editor editor
     * @param map editor map
     * @param coordinate XML coordinate string
     * @param centroid whether to add a centroid
     * @param eventSource node on which event was started, can be {@code null}
     * @return added element, or {@code null} if it could not be created
     */
    public static XsdTreeNode addPoint(final OtsEditor editor, final EditorMap map, final String coordinate,
            final boolean centroid, final XsdTreeNode eventSource)
    {
        XsdTreeNode added = addNetworkElement(editor, map, centroid ? XsdPaths.CENTROID : XsdPaths.NODE, eventSource);
        if (added == null)
        {
            return null;
        }
        added.setAttributeValue("Id", nextAlphabeticId(networkIds(map, Set.of(XsdPaths.NODE, XsdPaths.CENTROID))));
        added.setAttributeValue("Coordinate", coordinate);
        return added;
    }

    /**
     * Add a link between two nodes or a connector between a node and centroid.
     * @param editor editor
     * @param map editor map
     * @param start starting map node
     * @param end ending map node
     * @param shape selected link shape, may be {@code null}
     * @param roadLayout selected road layout, may be {@code null}
     * @param linkType selected link type, may be {@code null}
     * @return added element, or {@code null} if it could not be created
     */
    public static XsdTreeNode addConnection(final OtsEditor editor, final EditorMap map, final MapNodeData start,
            final MapNodeData end, final String shape, final String roadLayout, final String linkType)
    {
        return start.isCentroid() || end.isCentroid() ? addConnector(editor, map, start, end, linkType)
                : addLink(editor, map, start, end, shape, roadLayout, linkType);
    }

    /**
     * Add a link between two network nodes and apply the selected shape and road layout.
     * @param editor editor
     * @param map editor map
     * @param start starting node
     * @param end ending node
     * @param shape selected shape, or {@code null}
     * @param roadLayout selected road layout, or {@code null}
     * @param linkType selected link type, or {@code null}
     * @return added link, or {@code null} if it could not be created
     */
    private static XsdTreeNode addLink(final OtsEditor editor, final EditorMap map, final MapNodeData start,
            final MapNodeData end, final String shape, final String roadLayout, final String linkType)
    {
        XsdTreeNode link = addNetworkElement(editor, map, XsdPaths.LINK, null);
        if (link == null)
        {
            return null;
        }
        link.setAttributeValue("NodeStart", start.getId());
        link.setAttributeValue("NodeEnd", end.getId());
        if (linkType != null)
        {
            link.setAttributeValue("Type", linkType);
        }
        Set<String> ids = networkIds(map, Set.of(XsdPaths.LINK, XsdPaths.CONNECTOR));
        String candidate = start.getId() + "-" + end.getId();
        link.setAttributeValue("Id", ids.contains(candidate) ? nextAlphabeticId(ids) : candidate);
        if (shape != null)
        {
            selectChoiceOption(link, (option) -> option.getNodeName().equalsIgnoreCase(shape));
        }
        if (roadLayout != null)
        {
            XsdTreeNode defined = selectChoiceOption(link, (option) -> "Defined".equals(option.getShortString()));
            if (defined != null && defined.hasChild("DefinedLayout"))
            {
                XsdTreeNode layout = defined.getFirstChild("DefinedLayout");
                layout.setActive();
                layout.setValue(roadLayout);
            }
        }
        MapContextActions.autoFixAddedLink(map, link, start.getNode(), end.getNode());
        return link;
    }

    /**
     * Add a connector between a centroid and a node, setting its direction and ID.
     * @param editor editor
     * @param map editor map
     * @param start starting map node
     * @param end ending map node
     * @param linkType selected link type, or {@code null}
     * @return added connector, or {@code null} if it could not be created
     */
    private static XsdTreeNode addConnector(final OtsEditor editor, final EditorMap map, final MapNodeData start,
            final MapNodeData end, final String linkType)
    {
        XsdTreeNode connector = addNetworkElement(editor, map, XsdPaths.CONNECTOR, null);
        if (connector == null)
        {
            return null;
        }
        MapNodeData centroid = start.isCentroid() ? start : end;
        MapNodeData node = start.isCentroid() ? end : start;
        connector.setAttributeValue("Centroid", centroid.getId());
        connector.setAttributeValue("Node", node.getId());
        connector.setAttributeValue("Outbound", start.isCentroid() ? "true" : "false");
        if (linkType != null)
        {
            connector.setAttributeValue("Type", linkType);
        }
        Set<String> ids = networkIds(map, Set.of(XsdPaths.LINK, XsdPaths.CONNECTOR));
        String candidate = start.getId() + "-" + end.getId();
        connector.setAttributeValue("Id", ids.contains(candidate) ? nextAlphabeticId(ids) : candidate);
        return connector;
    }

    /**
     * Create a Network element, reusing an inactive placeholder when the network is empty.
     * @param editor editor
     * @param map editor map
     * @param path XSD path of the element to create
     * @param eventSource node on which event was started, can be {@code null}
     * @return created element, or {@code null} if no matching choice is available
     */
    private static XsdTreeNode addNetworkElement(final OtsEditor editor, final EditorMap map, final String path,
            final XsdTreeNode eventSource)
    {
        XsdTreeNode network = map.getNetworkNode();
        if (network == null)
        {
            return null;
        }
        List<XsdTreeNode> elements =
                network.getChildren().stream().filter((node) -> networkElementRank(node.getPathString()) >= 0).toList();
        if (elements.size() == 1 && !elements.get(0).isActive())
        {
            XsdTreeNode placeholder = elements.get(0);
            XsdTreeNode option = findOption(placeholder, path);
            if (option != null)
            {
                editor.getUndo().startAction(ActionType.ADD, placeholder, null);
                placeholder.setOption(option);
                option.setActive();
                reorderNetworkElement(network, option);
                editor.getUndo().setPostActionShowNode(option);
                return option;
            }
        }
        XsdTreeNode addSource = network.getChildren().stream().filter(
                (node) -> node.getOptions().stream().anyMatch((option) -> option.optionNode().getPathString().equals(path)))
                .findFirst().orElse(null);
        if (addSource == null)
        {
            return null;
        }
        editor.getUndo().startAction(ActionType.ADD, eventSource == null ? addSource : eventSource, null);
        XsdTreeNode added = addSource.add();
        if (!added.getPathString().equals(path))
        {
            XsdTreeNode option = findOption(added, path);
            if (option == null)
            {
                return null;
            }
            added.setOption(option);
            added = option;
            added.setActive();
        }
        reorderNetworkElement(network, added);
        editor.getUndo().setPostActionShowNode(added);
        return added;
    }

    /**
     * Find the option in a choice node that matches an XSD path.
     * @param node choice node
     * @param path requested option path
     * @return matching option, or {@code null} if none exists
     */
    private static XsdTreeNode findOption(final XsdTreeNode node, final String path)
    {
        return node.getOptions().stream().filter((option) -> option.optionNode().getPathString().equals(path))
                .map(XsdOption::optionNode).findFirst().orElse(null);
    }

    /**
     * Place the added element after the last element of its type and in the required network type order.
     * @param network Network node
     * @param added element to move
     */
    private static void reorderNetworkElement(final XsdTreeNode network, final XsdTreeNode added)
    {
        int rank = networkElementRank(added.getPathString());
        List<XsdTreeNode> siblings = network.getChildren().stream().filter((child) -> !child.equals(added)).toList();
        int insertionIndex = siblings.size();
        for (int index = siblings.size() - 1; index >= 0; index--)
        {
            int childRank = networkElementRank(siblings.get(index).getPathString());
            if (childRank < 0)
            {
                insertionIndex = index;
                continue;
            }
            if (childRank <= rank)
            {
                insertionIndex = index + 1;
                break;
            }
            insertionIndex = index;
        }
        int currentIndex = network.getChildren().indexOf(added);
        if (insertionIndex != currentIndex)
        {
            added.move(insertionIndex - currentIndex);
        }
    }

    /**
     * Return the order rank for a supported Network element.
     * @param path XSD path
     * @return rank, or {@code -1} for elements outside the requested order
     */
    private static int networkElementRank(final String path)
    {
        return switch (path)
        {
            case XsdPaths.CENTROID -> 0;
            case XsdPaths.CONNECTOR -> 1;
            case XsdPaths.NODE -> 2;
            case XsdPaths.LINK -> 3;
            default -> -1;
        };
    }

    /**
     * Select and activate the option in a child choice that matches the predicate.
     * @param parent parent element
     * @param matcher option predicate
     * @return selected option, or {@code null} if no matching child choice exists
     */
    private static XsdTreeNode selectChoiceOption(final XsdTreeNode parent, final Predicate<XsdTreeNode> matcher)
    {
        for (XsdTreeNode child : parent.getChildren())
        {
            XsdOption option = child.getOptions().stream().filter((candidate) -> matcher.test(candidate.optionNode()))
                    .findFirst().orElse(null);
            if (option != null)
            {
                child.setOption(option.optionNode());
                option.optionNode().setActive();
                return option.optionNode();
            }
        }
        return null;
    }

    /**
     * Collect IDs from identifiable Network children whose paths are in the requested set.
     * @param map editor map
     * @param paths element paths to include
     * @return existing IDs
     */
    private static Set<String> networkIds(final EditorMap map, final Set<String> paths)
    {
        Set<String> ids = new LinkedHashSet<>();
        XsdTreeNode network = map.getNetworkNode();
        if (network != null)
        {
            for (XsdTreeNode child : network.getChildren())
            {
                if (paths.contains(child.getPathString()) && child.isIdentifiable() && child.getId() != null)
                {
                    ids.add(child.getId());
                }
            }
        }
        return ids;
    }

    /**
     * Generate the first alphabetic ID that is not already in use.
     * @param ids existing IDs
     * @return unused alphabetic ID
     */
    private static String nextAlphabeticId(final Set<String> ids)
    {
        AlphabeticIdGenerator generator = new AlphabeticIdGenerator();
        String id;
        do
        {
            id = generator.get();
        }
        while (ids.contains(id));
        return id;
    }

}
