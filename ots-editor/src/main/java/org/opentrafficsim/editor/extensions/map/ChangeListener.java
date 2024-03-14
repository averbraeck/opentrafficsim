package org.opentrafficsim.editor.extensions.map;

import java.rmi.RemoteException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.Supplier;

import org.djutils.eval.Eval;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.EventType;
import org.djutils.event.LocalEventProducer;
import org.djutils.event.reference.ReferenceType;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeRoot;
import org.opentrafficsim.editor.EvalWrapper.EvalListener;

/**
 * Generic implementation to listen to any changes under a node, including its own attributes and activation status. The node
 * represents some logical data in which any change requires an update. For example, this can be used to listen to any change in
 * a road layout, causing all links that use the layout to be redrawn.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param <T> type of data that listeners require from what the main node represents.
 */
public abstract class ChangeListener<T> extends LocalEventProducer implements EventListener, EvalListener
{

    /** */
    private static final long serialVersionUID = 20231114L;

    /** Event that any change happened to the road layout. */
    public static final EventType CHANGE_EVENT = new EventType("CHANGEEVENT", new MetaData("Change event",
            "When any element under a node has changed", new ObjectDescriptor("Node", "Main node", XsdTreeNode.class)));

    /** Root element. */
    private final XsdTreeNodeRoot root;
    
    /** Main node, under which elements are located. */
    private final XsdTreeNode node;

    /** Set of all elements that, when removed, change the layout. */
    private final Set<XsdTreeNode> elementNodes = new LinkedHashSet<>();

    /** Expression evaluator supplier. */
    private final Supplier<Eval> eval;

    /** Flag when change occurred and offsets need to be recalculated. */
    private boolean dataIsDirty = true;

    /** Cached data. */
    private T data;

    /**
     * Constructor.
     * @param node XsdTreeNode; node of the layout, either in definitions or under a link.
     * @param eval Supplier&lt;Eval&gt;; supplier of expression evaluator, either from the main map, or from a map link data.
     */
    public ChangeListener(final XsdTreeNode node, final Supplier<Eval> eval)
    {
        this.node = node;
        this.eval = eval;
        node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
        node.addListener(this, XsdTreeNode.ACTIVATION_CHANGED, ReferenceType.WEAK);
        this.root = this.node.getRoot();
        this.root.addListener(this, XsdTreeNodeRoot.NODE_CREATED, ReferenceType.WEAK);
        this.root.addListener(this, XsdTreeNodeRoot.NODE_REMOVED, ReferenceType.WEAK);
    }

    /**
     * Remove all listeners.
     */
    public void destroy()
    {
        removeAsListener(this.node);
        this.node.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
        this.node.removeListener(this, XsdTreeNode.ACTIVATION_CHANGED);
        this.elementNodes.clear();
        this.root.removeListener(this, XsdTreeNodeRoot.NODE_CREATED);
        this.root.removeListener(this, XsdTreeNodeRoot.NODE_REMOVED);
        removeAllListeners();
        destroyData();
    }

    /**
     * Destroys the data. Can be overridden by subclasses. By default does nothing.
     */
    protected void destroyData()
    {
        //
    }

    /**
     * Removes this as listener from all nodes under the node recursively.
     * @param node XsdTreeNode; node to remove as listener from, including from child nodes.
     */
    private void removeAsListener(final XsdTreeNode node)
    {
        if (canBeIgnored(node))
        {
            return;
        }
        if (node.getChildCount() > 0)
        {
            for (XsdTreeNode child : node.getChildren())
            {
                removeAsListener(child);
            }
        }
        node.removeListener(this, XsdTreeNode.ACTIVATION_CHANGED);
        node.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
        node.removeListener(this, XsdTreeNode.MOVED);
        node.removeListener(this, XsdTreeNode.OPTION_CHANGED);
        node.removeListener(this, XsdTreeNode.VALUE_CHANGED);
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
    {
        // for all events this listens to, the node is the first object in the content
        XsdTreeNode node = (XsdTreeNode) ((Object[]) event.getContent())[0];
        if (event.getType().equals(XsdTreeNodeRoot.NODE_CREATED))
        {
            if (!node.getPath().contains(this.node) || canBeIgnored(node))
            {
                // not the node of this listener
                return;
            }
            node.addListener(this, XsdTreeNode.ACTIVATION_CHANGED, ReferenceType.WEAK);
            node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
            node.addListener(this, XsdTreeNode.MOVED, ReferenceType.WEAK);
            node.addListener(this, XsdTreeNode.OPTION_CHANGED, ReferenceType.WEAK);
            node.addListener(this, XsdTreeNode.VALUE_CHANGED, ReferenceType.WEAK);
            // remember the nodes, as their paths are empty after being removed, hence .contains(this.node) does not work
            this.elementNodes.add(node);
        }
        else if (event.getType().equals(XsdTreeNodeRoot.NODE_REMOVED))
        {
            if (!this.elementNodes.remove(node))
            {
                return;
            }
            node.removeListener(this, XsdTreeNode.ACTIVATION_CHANGED);
            node.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
            node.removeListener(this, XsdTreeNode.MOVED);
            node.removeListener(this, XsdTreeNode.OPTION_CHANGED);
            node.removeListener(this, XsdTreeNode.VALUE_CHANGED);
        }
        // for all events for which this method did not return, indicate a change
        this.dataIsDirty = true;
        fireEvent(CHANGE_EVENT, this.node);
    }

    /**
     * Returns a current valid evaluator.
     * @return Eval; evaluator.
     */
    public Eval getEval()
    {
        return this.eval.get();
    }

    /**
     * Returns whether the node may be ignored. This can be overridden by subclasses.
     * @param node XsdTreeNode; node.
     * @return boolean; whether the node may be ignored.
     */
    protected boolean canBeIgnored(final XsdTreeNode node)
    {
        return false;
    }

    /**
     * Returns the main node.
     * @return XsdTreeNode; main node.
     */
    public XsdTreeNode getNode()
    {
        return this.node;
    }

    /**
     * Returns the data that the node of this listener represents.
     * @return T; data that the node of this listener represents.
     */
    public final T getData()
    {
        if (this.dataIsDirty)
        {
            this.data = calculateData();
            this.dataIsDirty = false;
        }
        return this.data;
    }

    /**
     * Calculates the data. This is called when data is required by a listener and the there have been changes.
     * @return T; calculated data based on content of the main node.
     */
    abstract T calculateData();

    /** {@inheritDoc} */
    @Override
    public void evalChanged()
    {
        this.dataIsDirty = true;
    }

}
