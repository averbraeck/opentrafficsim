package org.opentrafficsim.editor;

import java.rmi.RemoteException;
import java.util.function.Function;

import org.djutils.event.Event;
import org.djutils.event.EventListener;

/**
 * Generic implementation to enhance the information in nodes as displayed in the tree, by showing a few attribute values.
 * @author wjschakel
 */
public class GenericStringFunction implements EventListener
{

    /** */
    private static final long serialVersionUID = 20220301L;

    /** Path of nodes to register a string function with. */
    private final String path;

    /** Attributes to show in the node name. */
    private final String[] attributes;

    /**
     * Constructor.
     * @param editor OtsEditor; editor.
     * @param path String; path of nodes to register a string function with, used in a {@code String.endsWith()} manner.
     * @param attributes String...; attributes to show in the node name.
     * @throws RemoteException if an exception occurs while listening for new nodes.
     */
    public GenericStringFunction(final OtsEditor editor, final String path, final String... attributes) throws RemoteException
    {
        editor.addListener(this, OtsEditor.SCHEMA_LOADED);
        this.path = path;
        this.attributes = attributes;
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
    {
        XsdTreeNodeRoot root = (XsdTreeNodeRoot) event.getContent();
        root.addListener(new EventListener()
        {
            /** */
            private static final long serialVersionUID = 20220301L;

            /** {@inheritDoc} */
            @Override
            public void notify(final Event event) throws RemoteException
            {
                XsdTreeNode node = (XsdTreeNode) event.getContent();
                if (event.getType().equals(XsdTreeNodeRoot.NODE_CREATED)
                        && node.getPathString().endsWith(GenericStringFunction.this.path))
                {
                    node.setStringFunction(new Function<XsdTreeNode, String>()
                    {
                        @Override
                        public String apply(final XsdTreeNode t)
                        {
                            String sep = "";
                            String out = "";
                            for (String attribute : GenericStringFunction.this.attributes)
                            {
                                out = out + sep + node.getAttributeValue(attribute);
                                sep = ", ";
                            }
                            return out;
                        }
                    });
                }
            }
        }, XsdTreeNodeRoot.NODE_CREATED);
    }

}
