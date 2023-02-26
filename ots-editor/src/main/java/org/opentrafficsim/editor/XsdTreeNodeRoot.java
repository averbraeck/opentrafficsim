package org.opentrafficsim.editor;

import java.rmi.RemoteException;

import org.djutils.event.EventListener;
import org.djutils.event.EventListenerMap;
import org.djutils.event.EventProducer;
import org.djutils.event.EventType;
import org.djutils.event.reference.ReferenceType;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.w3c.dom.Document;

/**
 * Extends {@code XsdTreeNode} with event producer capabilities. In this way there is a clear central point for subscription to
 * events on node creation and removal. Because this node itself is created, and the treetable model will expand two layers, for
 * none of those nodes creation events can be thrown regularly after listeners have had a change to register with this root
 * node. Therefore, this class override {@code addListener(...)} to throw a creation event for all existing nodes in the tree.
 * @author wjschakel
 */
public class XsdTreeNodeRoot extends XsdTreeNode implements EventProducer
{

    /** */
    private static final long serialVersionUID = 20230224L;

    /** Map with listeners. */
    private final EventListenerMap eventMap = new EventListenerMap();

    /**
     * Event when a node is created. This event is always thrown by the root of the data structure. Listeners should register
     * with the root.
     */
    public static final EventType NODE_CREATED = new EventType("NODECREATED", new MetaData("Node created", "Created tree node",
            new ObjectDescriptor("Node created", "Created tree node", XsdTreeNode.class)));

    /**
     * Event when a node is removed. Invoked for each individual node, including all child nodes of a node that a user removes.
     * This event is always thrown by the root of the data structure. Listeners should register with the root.
     */
    public static final EventType NODE_REMOVED = new EventType("NODEREMOVEDD", new MetaData("Node removed", "Removed tree node",
            new ObjectDescriptor("Node removed", "Removed tree node", XsdTreeNode.class)));

    /**
     * Constructor for root node, based on a document. The document is forwarded to an {@code XsdSchema}.
     * @param document Document; XSD document.
     */
    public XsdTreeNodeRoot(final Document document)
    {
        super(document);
        // pointless to fire NODE_CREATED event, no one can be listening yet
    }

    /** {@inheritDoc} */
    @Override
    public EventListenerMap getEventListenerMap() throws RemoteException
    {
        return this.eventMap;
    }

    /**
     * {@inheritDoc} Overridden to throw events on existing nodes to the listener.
     */
    @Override
    public boolean addListener(final EventListener listener, final EventType eventType, final int position,
            final ReferenceType referenceType) throws RemoteException
    {
        if (NODE_CREATED.equals(eventType))
        {
            fireCreatedEventOnExistingNodes(this, listener);
        }
        return EventProducer.super.addListener(listener, eventType, position, referenceType);
    }

}
