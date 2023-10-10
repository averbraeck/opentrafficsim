package org.opentrafficsim.editor.decoration;

import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;

/**
 * Sets the Id of a connector if it is not defined yet, and Node, Centroid and Outbound are defined.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class AutomaticConnectorId extends AutomaticLinkId
{

    /** */
    private static final long serialVersionUID = 20230910L;

    /**
     * Constructor.
     * @param editor OtsEditor; editor.
     */
    public AutomaticConnectorId(final OtsEditor editor)
    {
        super(editor, (node) -> node.isType("Ots.Network.Connector"), "Node", "Centroid", "Outbound");
        editor.addAttributeCellEditorListener(this);
    }

    /** {@inheritDoc} */
    @Override
    public void notifyAttributeChanged(final XsdTreeNode node, final String attribute)
    {
        String nodeId = node.getAttributeValue("Node");
        String centroid = node.getAttributeValue("Centroid");
        String outbound = node.getAttributeValue("Outbound");
        String id = node.getAttributeValue("Id");
        if (nodeId != null && centroid != null && ("true".equalsIgnoreCase(outbound) || "false".equalsIgnoreCase(outbound))
                && id == null)
        {
            this.lastNode = node;
            this.lastId = debrace("true".equalsIgnoreCase(outbound) ? (centroid + "-" + nodeId) : (nodeId + "-" + centroid));
        }
        else
        {
            this.lastNode = null;
            this.lastId = null;
        }
    }
}
