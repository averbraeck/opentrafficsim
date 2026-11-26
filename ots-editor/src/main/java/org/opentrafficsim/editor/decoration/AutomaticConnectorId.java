package org.opentrafficsim.editor.decoration;

import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdPaths;
import org.opentrafficsim.editor.XsdTreeNode;

/**
 * Sets the Id of a connector when it is not defined yet, and Node, Centroid and Outbound are defined. An Id following
 * the {@code Node-Centroid} or {@code Centroid-Node} pattern is updated immediately when one of those attributes
 * changes.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class AutomaticConnectorId extends AutomaticLinkId
{

    /**
     * Constructor.
     * @param editor editor.
     */
    public AutomaticConnectorId(final OtsEditor editor)
    {
        super(editor, (node) -> node.isType(XsdPaths.CONNECTOR), "Node", "Centroid", "Outbound");
    }

    @Override
    public void notifyAttributeChanged(final XsdTreeNode node, final String attribute)
    {
        String nodeId = node.getAttributeValueOrDefault("Node");
        String centroidId = node.getAttributeValueOrDefault("Centroid");
        String outboundValue = node.getAttributeValueOrDefault("Outbound");
        if (nodeId == null || centroidId == null
                || !("true".equalsIgnoreCase(outboundValue) || "false".equalsIgnoreCase(outboundValue)))
        {
            return;
        }
        boolean outbound = "true".equalsIgnoreCase(outboundValue);
        String start = outbound ? debrace(centroidId) : debrace(nodeId);
        String end = outbound ? debrace(nodeId) : debrace(centroidId);
        String targetId = start + "-" + end;
        String id = node.getAttributeValueOrDefault("Id");
        if (id != null && targetId.equals(debrace(id)))
        {
            return;
        }
        if ("Outbound".equals(attribute))
        {
            // Outbound reverses the pattern; accept the current Id if it exactly matches the previous pattern.
            if (!isPatternId(end, start, id))
            {
                return;
            }
        }
        else
        {
            boolean startChanged;
            boolean endChanged;
            if ("Node".equals(attribute))
            {
                startChanged = !outbound;
                endChanged = outbound;
            }
            else if ("Centroid".equals(attribute))
            {
                startChanged = outbound;
                endChanged = !outbound;
            }
            else
            {
                return;
            }
            if (!isPatternId(startChanged, endChanged, start, end, id))
            {
                return;
            }
        }
        node.setId(targetId);
    }
}
