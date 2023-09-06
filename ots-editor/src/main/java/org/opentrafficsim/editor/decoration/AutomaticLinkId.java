package org.opentrafficsim.editor.decoration;

import java.rmi.RemoteException;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeRoot;

/**
 * Sets the ID of a link if it is not defined yet, and both NodeStart and NodeEnd are defined.
 * @author wjschakel
 */
public class AutomaticLinkId implements EventListener
{
    /** */
    private static final long serialVersionUID = 20230313L;

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
    {
        XsdTreeNodeRoot root = (XsdTreeNodeRoot) event.getContent();
        root.addListener(new EventListener()
        {
            /** */
            private static final long serialVersionUID = 20230313L;

            /** {@inheritDoc} */
            @Override
            public void notify(final Event event) throws RemoteException
            {
                if (event.getType().equals(XsdTreeNodeRoot.NODE_CREATED))
                {
                    XsdTreeNode node = (XsdTreeNode) event.getContent();
                    if (node.isType("Ots.Network.Link"))
                    {
                        node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
                    }
                }
                else if (event.getType().equals(XsdTreeNode.ATTRIBUTE_CHANGED))
                {
                    Object[] content = (Object[]) event.getContent();
                    String attribute = (String) content[1];
                    if (attribute.equals("NodeStart") || attribute.equals("NodeEnd"))
                    {
                        XsdTreeNode node = (XsdTreeNode) content[0];
                        String nodeStart = node.getAttributeValue("NodeStart");
                        String nodeEnd = node.getAttributeValue("NodeEnd");
                        String id = node.getAttributeValue("Id");
                        if (nodeStart != null && !nodeStart.isBlank() && nodeEnd != null && !nodeEnd.isBlank()
                                && (id == null || id.isBlank()))
                        {
                            node.setAttributeValue("Id", nodeStart + "-" + nodeEnd);
                        }
                    }
                }
            }
        }, XsdTreeNodeRoot.NODE_CREATED);
    }
}