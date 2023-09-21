package org.opentrafficsim.editor.decoration;

import java.rmi.RemoteException;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeRoot;

/**
 * General implementation of node decorators, such as validators and string functions, that also need to trigger on removed
 * nodes. This class will listen to events of the editor, and trigger on nodes being created and removed.
 * @author wjschakel
 */
public abstract class AbstractNodeDecoratorRemove extends AbstractNodeDecorator
{

    /** */
    private static final long serialVersionUID = 20230910L;

    /**
     * Constructor.
     * @param editor OtsEditor; editor.
     * @throws RemoteException if an exception occurs while adding as a listener.
     */
    public AbstractNodeDecoratorRemove(final OtsEditor editor) throws RemoteException
    {
        super(editor);
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
    {
        if (event.getType().equals(OtsEditor.NEW_FILE))
        {
            super.notify(event); // NODE_CREATED
            XsdTreeNodeRoot root = (XsdTreeNodeRoot) event.getContent();
            root.addListener(new EventListener()
            {
                /** */
                private static final long serialVersionUID = 20230910L;

                /** {@inheritDoc} */
                @Override
                public void notify(final Event event) throws RemoteException
                {
                    if (event.getType().equals(XsdTreeNodeRoot.NODE_REMOVED))
                    {
                        XsdTreeNode node = (XsdTreeNode) ((Object[]) event.getContent())[0];
                        AbstractNodeDecoratorRemove.this.notifyRemoved(node);
                    }
                }
            }, XsdTreeNodeRoot.NODE_REMOVED);
        }
    }

    /**
     * Notified when a node has been removed.
     * @param node XsdTreeNode; removed node.
     */
    public abstract void notifyRemoved(XsdTreeNode node);

}
