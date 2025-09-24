package org.opentrafficsim.editor.decoration;

import java.rmi.RemoteException;
import java.util.function.Predicate;

import org.djutils.event.Event;
import org.djutils.event.reference.ReferenceType;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeRoot;

/**
 * General implementation of node decorators, such as validators and string functions, that also need to trigger on removed
 * nodes. This class will listen to events of the editor, and trigger on nodes being created and removed.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class AbstractNodeDecoratorRemove extends AbstractNodeDecorator
{

    /** */
    private static final long serialVersionUID = 20230910L;

    /**
     * Constructor.
     * @param editor editor.
     * @param predicate predicate to accept nodes that should have this decorator.
     */
    public AbstractNodeDecoratorRemove(final OtsEditor editor, final Predicate<XsdTreeNode> predicate)
    {
        super(editor, predicate);
    }

    @Override
    public void notify(final Event event) throws RemoteException
    {
        super.notify(event); // NEW_FILE -> NODE_CREATED and NODE_CREATED -> notifyCreated()
        if (event.getType().equals(OtsEditor.NEW_FILE))
        {
            // NEW_FILE -> NODE_REMOVED
            XsdTreeNodeRoot root = (XsdTreeNodeRoot) event.getContent();
            root.addListener(this, XsdTreeNodeRoot.NODE_REMOVED, ReferenceType.WEAK);
        }
        else if (event.getType().equals(XsdTreeNodeRoot.NODE_REMOVED))
        {
            // NODE_REMOVED -> notifyRemoved()
            XsdTreeNode node = (XsdTreeNode) ((Object[]) event.getContent())[0];
            if (acceptNode(node))
            {
                notifyRemoved(node);
            }
        }
    }

    /**
     * Notified when a node has been removed.
     * @param node removed node.
     */
    public abstract void notifyRemoved(XsdTreeNode node);

}
