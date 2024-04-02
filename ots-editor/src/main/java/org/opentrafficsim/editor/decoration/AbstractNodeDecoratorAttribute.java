package org.opentrafficsim.editor.decoration;

import java.rmi.RemoteException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;

import org.djutils.event.Event;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;

/**
 * General implementation of node decorators, such as validators and string functions, that also need to trigger on changed
 * attributes. This class will listen to events of the editor, and trigger on nodes being created, or attributes being changed.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class AbstractNodeDecoratorAttribute extends AbstractNodeDecorator
{

    /** */
    private static final long serialVersionUID = 20230910L;

    /** Predicate to accept nodes that should have this attribute decorator. */
    protected final Predicate<XsdTreeNode> predicate;

    /** Attributes to trigger on. */
    protected final List<String> attributes;

    /**
     * Constructor.
     * @param editor OtsEditor; editor.
     * @param predicate Predicate&lt;XsdTreeNode&gt;; predicate to accept nodes that should have this attribute decorator.
     * @param attributes String...; attributes to trigger on.
     */
    public AbstractNodeDecoratorAttribute(final OtsEditor editor, final Predicate<XsdTreeNode> predicate,
            final String... attributes)
    {
        super(editor);
        this.predicate = predicate;
        this.attributes = Arrays.asList(attributes);
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
    {
        super.notify(event); // NODE_CREATED
        if (event.getType().equals(XsdTreeNode.ATTRIBUTE_CHANGED))
        {
            Object[] content = (Object[]) event.getContent();
            String attribute = (String) content[1];
            if (AbstractNodeDecoratorAttribute.this.attributes.contains(attribute))
            {
                AbstractNodeDecoratorAttribute.this.notifyAttributeChanged((XsdTreeNode) content[0], attribute);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notifyCreated(final XsdTreeNode node)
    {
        if (this.predicate.test(node))
        {
            node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
        }
    }

    /**
     * Notified when a node has been removed.
     * @param node XsdTreeNode; removed node.
     * @param attribute String; attribute that has changed.
     */
    public abstract void notifyAttributeChanged(XsdTreeNode node, String attribute);

}
