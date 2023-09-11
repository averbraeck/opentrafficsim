package org.opentrafficsim.editor.decoration;

import java.rmi.RemoteException;

import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;

/**
 * Sets the Id of a link if it is not defined yet, and both NodeStart and NodeEnd are defined.
 * @author wjschakel
 */
public class AutomaticLinkId extends AbstractNodeDecoratorAttribute
{

    /** */
    private static final long serialVersionUID = 20230910L;

    /**
     * Constructor.
     * @param editor OtsEditor; editor.
     * @throws RemoteException if an exception occurs while adding as a listener.
     */
    public AutomaticLinkId(final OtsEditor editor) throws RemoteException
    {
        super(editor, (node) -> node.isType("Ots.Network.Link"), "NodeStart", "NodeEnd");
    }

    /** {@inheritDoc} */
    @Override
    public void notifyAttributeChanged(final XsdTreeNode node, final String attribute)
    {
        String nodeStart = node.getAttributeValue("NodeStart");
        String nodeEnd = node.getAttributeValue("NodeEnd");
        String id = node.getAttributeValue("Id");
        if (nodeStart != null && !nodeStart.isEmpty() && nodeEnd != null && !nodeEnd.isEmpty() && (id == null || id.isEmpty()))
        {
            node.setAttributeValue("Id", nodeStart + "-" + nodeEnd);
        }
    }
}
