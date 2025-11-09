package org.opentrafficsim.editor.decoration;

import java.util.Arrays;
import java.util.function.Predicate;

import org.djutils.event.Event;
import org.djutils.event.reference.ReferenceType;
import org.djutils.immutablecollections.ImmutableArrayList;
import org.djutils.immutablecollections.ImmutableList;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeRoot;

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

    /** Attributes to trigger on. */
    private final ImmutableList<String> attributes;

    /**
     * Constructor.
     * @param editor editor.
     * @param predicate predicate to accept nodes that should have this decorator.
     * @param attributes attributes to trigger on.
     */
    public AbstractNodeDecoratorAttribute(final OtsEditor editor, final Predicate<XsdTreeNode> predicate,
            final String... attributes)
    {
        super(editor, predicate);
        this.attributes = new ImmutableArrayList<>(Arrays.asList(attributes));
    }

    @Override
    public void notify(final Event event)
    {
        super.notify(event); // NEW_FILE -> NODE_CREATED and NODE_CREATED -> notifyCreated()
        if (event.getType().equals(XsdTreeNodeRoot.NODE_CREATED))
        {
            // NODE_CREATED -> ATTRIBUTE_CHANGED
            XsdTreeNode node = (XsdTreeNode) ((Object[]) event.getContent())[0];
            if (acceptNode(node))
            {
                node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
            }
        }
        else if (event.getType().equals(XsdTreeNode.ATTRIBUTE_CHANGED))
        {
            // ATTRIBUTE_CHANGED -> notifyAttributeChanged()
            Object[] content = (Object[]) event.getContent();
            String attribute = (String) content[1];
            if (AbstractNodeDecoratorAttribute.this.attributes.contains(attribute))
            {
                notifyAttributeChanged((XsdTreeNode) content[0], attribute);
            }
        }
    }

    /**
     * Returns the attributes.
     * @return attributes
     */
    public ImmutableList<String> getAttributes()
    {
        return this.attributes;
    }

    /**
     * Notified when a node has been removed.
     * @param node removed node.
     * @param attribute attribute that has changed.
     */
    public abstract void notifyAttributeChanged(XsdTreeNode node, String attribute);

}
