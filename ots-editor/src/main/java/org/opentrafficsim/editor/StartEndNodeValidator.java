package org.opentrafficsim.editor;

import java.rmi.RemoteException;

import org.djutils.event.Event;
import org.djutils.event.EventListener;

/**
 * Validates that the start node and end node of a link are not the same.
 * @author wjschakel
 */
public class StartEndNodeValidator implements ValueValidator, EventListener
{

    /** */
    private static final long serialVersionUID = 20230319L;

    /** {@inheritDoc} */
    @Override
    public String validate(final XsdTreeNode node)
    {
        if (!node.isActive())
        {
            return null;
        }
        String startNode = node.getAttributeValue("NodeStart");
        if (startNode == null || startNode.isBlank())
        {
            return null;
        }
        String endNode = node.getAttributeValue("NodeEnd");
        if (endNode == null || endNode.isBlank() || !endNode.equals(startNode))
        {
            return null;
        }
        return "Start node and end node may not be equal.";
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
    {
        XsdTreeNodeRoot root = (XsdTreeNodeRoot) event.getContent();
        EventListener listener = new EventListener()
        {
            /** */
            private static final long serialVersionUID = 20230319L;

            /** {@inheritDoc} */
            @Override
            public void notify(final Event event) throws RemoteException
            {
                if (event.getType().equals(XsdTreeNodeRoot.NODE_CREATED))
                {
                    XsdTreeNode node = (XsdTreeNode) event.getContent();
                    if (node.getPathString().equals("Ots.Network.Link"))
                    {
                        node.addAttributeValidator("NodeStart", StartEndNodeValidator.this);
                        node.addAttributeValidator("NodeEnd", StartEndNodeValidator.this);
                    }
                }
            }
        };
        root.addListener(listener, XsdTreeNodeRoot.NODE_CREATED);
    }

}
