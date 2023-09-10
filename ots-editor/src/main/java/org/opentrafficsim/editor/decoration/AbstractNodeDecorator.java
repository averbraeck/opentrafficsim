package org.opentrafficsim.editor.decoration;

import java.rmi.RemoteException;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeRoot;

/**
 * General implementation of node decorators, such as validators and string functions. This class will listen to events of the
 * editor, and trigger on nodes being created.
 * @author wjschakel
 */
public abstract class AbstractNodeDecorator implements EventListener
{

    /** */
    private static final long serialVersionUID = 20230910L;

    /**
     * Constructor.
     * @param editor OtsEditor; editor.
     * @throws RemoteException if an exception occurs while adding as a listener.
     */
    public AbstractNodeDecorator(final OtsEditor editor) throws RemoteException
    {
        editor.addListener(this, OtsEditor.NEW_FILE);
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
    {
        if (event.getType().equals(OtsEditor.NEW_FILE))
        {
            XsdTreeNodeRoot root = (XsdTreeNodeRoot) event.getContent();
            root.addListener(new EventListener()
            {
                /** */
                private static final long serialVersionUID = 20230910L;

                /** {@inheritDoc} */
                @Override
                public void notify(final Event event) throws RemoteException
                {
                    if (event.getType().equals(XsdTreeNodeRoot.NODE_CREATED))
                    {
                        AbstractNodeDecorator.this.notifyCreated((XsdTreeNode) event.getContent());
                    }
                }
            }, XsdTreeNodeRoot.NODE_CREATED);
        }
    }

    /**
     * Notified when a node has been created.
     * @param node XsdTreeNode; created node.
     */
    public abstract void notifyCreated(XsdTreeNode node);

}
