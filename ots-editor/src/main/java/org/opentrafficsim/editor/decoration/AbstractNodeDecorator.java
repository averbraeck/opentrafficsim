package org.opentrafficsim.editor.decoration;

import java.util.function.Predicate;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeRoot;

/**
 * General implementation of node decorators, such as validators and string functions. This class will listen to events of the
 * editor, and triggers on nodes being created.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class AbstractNodeDecorator implements EventListener
{

    /** */
    private static final long serialVersionUID = 20230910L;

    /** Predicate to accept nodes that should have this attribute decorator. */
    private final Predicate<XsdTreeNode> predicate;

    /**
     * Constructor.
     * @param editor editor.
     * @param predicate predicate to accept nodes that should have this decorator.
     */
    public AbstractNodeDecorator(final OtsEditor editor, final Predicate<XsdTreeNode> predicate)
    {
        this.predicate = predicate;
        editor.addListener(this, OtsEditor.NEW_FILE);
    }

    /**
     * Accept node by internal predicate.
     * @param node node to accept
     * @return whether the node should be accepted for this decorator
     */
    protected boolean acceptNode(final XsdTreeNode node)
    {
        return this.predicate.test(node);
    }

    @Override
    public void notify(final Event event)
    {
        if (event.getType().equals(OtsEditor.NEW_FILE))
        {
            XsdTreeNodeRoot root = (XsdTreeNodeRoot) event.getContent();
            root.addListener(this, XsdTreeNodeRoot.NODE_CREATED);
        }
        else if (event.getType().equals(XsdTreeNodeRoot.NODE_CREATED))
        {
            XsdTreeNode node = (XsdTreeNode) ((Object[]) event.getContent())[0];
            if (acceptNode(node))
            {
                notifyCreated(node);
            }
        }
    }

    /**
     * Notified when a node has been created.
     * @param node created node.
     */
    public abstract void notifyCreated(XsdTreeNode node);

}
