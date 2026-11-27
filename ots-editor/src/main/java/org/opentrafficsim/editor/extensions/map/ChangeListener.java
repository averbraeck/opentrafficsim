package org.opentrafficsim.editor.extensions.map;

import java.util.LinkedHashSet;
import java.util.Set;

import org.djutils.eval.Eval;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.EventType;
import org.djutils.event.LocalEventProducer;
import org.djutils.event.reference.ReferenceType;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.editor.EvalWrapper.EvalListener;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeRoot;

/**
 * Generic implementation to listen to any changes under a node, including its own attributes and activation status. All events
 * are forwarded in to a CHANGE_EVENT. This includes the case where {@link Eval} is updated.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class ChangeListener extends LocalEventProducer implements EventListener, EvalListener
{

    /** Event that any change happened to the main node or anything under it. */
    public static final EventType CHANGE_EVENT = new EventType("CHANGEEVENT", new MetaData("Change event",
            "When any element under a node has changed", new ObjectDescriptor("Node", "Main node", XsdTreeNode.class)));

    /** Root element. */
    private final XsdTreeNodeRoot root;

    /** Main node, under which elements are located. */
    private final XsdTreeNode node;

    /** Set of all elements that, when removed, change the layout. */
    private final Set<XsdTreeNode> elementNodes = new LinkedHashSet<>();

    /**
     * Constructor.
     * @param node node of the layout, either in definitions or under a link.
     */
    public ChangeListener(final XsdTreeNode node)
    {
        this.node = node;
        addListeners(node);
        this.root = this.node.getRoot();
        this.root.addListener(this, XsdTreeNodeRoot.NODE_CREATED, ReferenceType.WEAK);
        this.root.addListener(this, XsdTreeNodeRoot.NODE_REMOVED, ReferenceType.WEAK);
        addExistingNodes(node);
    }

    /**
     * Adds listeners for all relevant event types to the given node.
     * @param xsdNode node to listen to.
     */
    private void addListeners(final XsdTreeNode xsdNode)
    {
        xsdNode.addListener(this, XsdTreeNode.ACTIVATION_CHANGED, ReferenceType.WEAK);
        xsdNode.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
        xsdNode.addListener(this, XsdTreeNode.MOVED, ReferenceType.WEAK);
        xsdNode.addListener(this, XsdTreeNode.OPTION_CHANGED, ReferenceType.WEAK);
        xsdNode.addListener(this, XsdTreeNode.VALUE_CHANGED, ReferenceType.WEAK);
    }

    /**
     * Remove listeners for all relevant event types to the given node.
     * @param xsdNode node to no longer listen to.
     */
    private void removeListeners(final XsdTreeNode xsdNode)
    {
        xsdNode.removeListener(this, XsdTreeNode.ACTIVATION_CHANGED);
        xsdNode.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
        xsdNode.removeListener(this, XsdTreeNode.MOVED);
        xsdNode.removeListener(this, XsdTreeNode.OPTION_CHANGED);
        xsdNode.removeListener(this, XsdTreeNode.VALUE_CHANGED);
    }

    /**
     * Adds listeners to nodes that already existed when this listener was created. Nodes created later are registered in
     * {@link #listen(Event)}.
     * @param xsdNode node to inspect, including its children.
     */
    private void addExistingNodes(final XsdTreeNode xsdNode)
    {
        if (xsdNode != this.node && !canBeIgnored(xsdNode))
        {
            addListeners(xsdNode);
            this.elementNodes.add(xsdNode);
        }
        if (xsdNode.getChildCount() > 0)
        {
            for (XsdTreeNode child : xsdNode.getChildren())
            {
                addExistingNodes(child);
            }
        }
    }

    /**
     * Remove all listeners.
     */
    public void destroy()
    {
        removeAsListener(this.node);
        this.elementNodes.clear();
        this.root.removeListener(this, XsdTreeNodeRoot.NODE_CREATED);
        this.root.removeListener(this, XsdTreeNodeRoot.NODE_REMOVED);
        removeAllListeners();
    }

    /**
     * Removes this as listener from all nodes under the node recursively.
     * @param xsdNode node to remove as listener from, including from child nodes.
     */
    private void removeAsListener(final XsdTreeNode xsdNode)
    {
        if (!canBeIgnored(xsdNode))
        {
            removeListeners(xsdNode);
        }
        if (xsdNode.getChildCount() > 0)
        {
            for (XsdTreeNode child : xsdNode.getChildren())
            {
                removeAsListener(child);
            }
        }
    }

    @Override
    public void notify(final Event event)
    {
        if (listen(event))
        {
            fireEvent(CHANGE_EVENT, this.node);
        }
    }

    /**
     * Listen to node.
     * @param event event
     * @return whether the event is relevant for this listener
     */
    protected boolean listen(final Event event)
    {
        // for all events this listens to, the node is the first object in the content
        XsdTreeNode xsdNode = (XsdTreeNode) ((Object[]) event.getContent())[0];
        if (event.getType().equals(XsdTreeNodeRoot.NODE_CREATED))
        {
            if (!xsdNode.getPath().contains(this.node) || canBeIgnored(xsdNode))
            {
                // not the node of this listener
                return false;
            }
            addListeners(xsdNode);
            // remember the nodes, as their paths are empty after being removed, hence .contains(this.node) does not work
            this.elementNodes.add(xsdNode);
        }
        else if (event.getType().equals(XsdTreeNodeRoot.NODE_REMOVED))
        {
            if (!this.elementNodes.remove(xsdNode))
            {
                return false;
            }
            removeListeners(xsdNode);
        }
        else
        {
            return xsdNode == this.node || this.elementNodes.contains(xsdNode);
        }
        return true;
    }

    /**
     * Returns whether the node may be ignored. This can be overridden by subclasses.
     * @param xsdNode node.
     * @return whether the node may be ignored.
     */
    protected boolean canBeIgnored(final XsdTreeNode xsdNode)
    {
        return false;
    }

    @Override
    public void evalChanged()
    {
        fireEvent(CHANGE_EVENT, this.node);
    }

    /**
     * Returns the main node.
     * @return main node.
     */
    public XsdTreeNode getNode()
    {
        return this.node;
    }

}
