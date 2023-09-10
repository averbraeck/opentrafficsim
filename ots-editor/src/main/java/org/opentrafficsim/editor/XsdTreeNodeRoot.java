package org.opentrafficsim.editor;

import java.rmi.RemoteException;
import java.util.LinkedHashSet;
import java.util.Map.Entry;
import java.util.Set;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.EventType;
import org.djutils.event.reference.ReferenceType;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.editor.decoration.validation.KeyValidator;
import org.w3c.dom.Node;

/**
 * Extends {@code XsdTreeNode} with event producer capabilities. In this way there is a clear central point for subscription to
 * events on node creation and removal. Because this node itself is created, and the tree table model will expand two layers,
 * for none of those nodes creation events can be thrown regularly after listeners have had a change to register with this root
 * node. Therefore, this class overrides {@code addListener(...)} to throw a creation event for all existing nodes in the tree.
 * <br>
 * <br>
 * This class also sets up a listener for all xsd:key, xsd:keyref and xsd:unique from the schema.
 * @author wjschakel
 */
public class XsdTreeNodeRoot extends XsdTreeNode
{

    /** */
    private static final long serialVersionUID = 20230224L;

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

    /** Directory, relevant for relative paths in include nodes. */
    private String directory;

    /**
     * Constructor for root node, based on a schema.
     * @param schema XsdSchema; XSD Schema.
     * @throws RemoteException when unable to listen for created nodes.
     */
    public XsdTreeNodeRoot(final Schema schema) throws RemoteException
    {
        super(schema);
        // pointless to fire NODE_CREATED event, no one can be listening yet
        setupXPathListener(schema);
    }

    /**
     * Returns the directory.
     * @return String; directory.
     */
    public String getDirectory()
    {
        return this.directory;
    }

    /**
     * Set the directory.
     * @param directory String; directory.
     */
    public void setDirectory(final String directory)
    {
        this.directory = directory;
    }

    /**
     * {@inheritDoc} Overridden to throw events on existing nodes to the listener.
     */
    @Override
    public boolean addListener(final EventListener listener, final EventType eventType, final int position,
            final ReferenceType referenceType)
    {
        if (NODE_CREATED.equals(eventType))
        {
            try
            {
                XsdTreeNodeUtil.fireCreatedEventOnExistingNodes(this, listener);
            }
            catch (RemoteException exception)
            {
                throw new RuntimeException("Unexpected remote exception in local context.", exception);
            }
        }
        return super.addListener(listener, eventType, position, referenceType);
    }

    /**
     * Sets up the listener that reports on new and removed nodes for each xsd:key, xsd:keyref and xsd:unique. It is up to each
     * key to determine whether the node is relevant for the key.
     * @param schema XsdSchema; schema.
     * @throws RemoteException when unable to listen for created nodes.
     */
    private void setupXPathListener(final Schema schema) throws RemoteException
    {

        Set<KeyValidator> keys = new LinkedHashSet<>();
        for (Entry<Node, String> entry : schema.keys().entrySet())
        {
            keys.add(new KeyValidator(entry.getKey(), entry.getValue(), null));
        }
        Set<KeyValidator> keyrefs = new LinkedHashSet<>();
        for (Entry<Node, String> entry : schema.keyrefs().entrySet())
        {
            String keyName = DocumentReader.getAttribute(entry.getKey(), "refer").replace("ots:", "");
            for (KeyValidator key : keys)
            {
                if (key.getKeyName().equals(keyName))
                {
                    keyrefs.add(new KeyValidator(entry.getKey(), entry.getValue(), key));
                    break;
                }
            }
        }
        Set<KeyValidator> uniques = new LinkedHashSet<>();
        for (Entry<Node, String> entry : schema.uniques().entrySet())
        {
            uniques.add(new KeyValidator(entry.getKey(), entry.getValue(), null));
        }

        EventListener listener = new EventListener()
        {
            /** */
            private static final long serialVersionUID = 20230228L;

            /** {@inheritDoc} */
            @Override
            public void notify(final Event event) throws RemoteException
            {
                XsdTreeNode node = (XsdTreeNode) event.getContent();
                int iteration = 0;
                Set<KeyValidator> keysIteration = keys;
                while (iteration < 3)
                {
                    for (KeyValidator key : keysIteration)
                    {
                        if (event.getType().equals(NODE_CREATED))
                        {
                            key.addNode(node);
                        }
                        else
                        {
                            key.removeNode(node);
                        }
                    }
                    keysIteration = iteration == 0 ? keyrefs : uniques;
                    iteration++;
                }
            }
        };

        addListener(listener, NODE_CREATED);
        addListener(listener, NODE_REMOVED);
    }

}
