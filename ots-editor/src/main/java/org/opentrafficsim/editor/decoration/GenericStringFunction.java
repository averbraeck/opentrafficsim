package org.opentrafficsim.editor.decoration;

import java.rmi.RemoteException;
import java.util.function.Function;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeRoot;

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

    /** Separator between attribute values. */
    private String separator = ", ";

    /**
     * Constructor.
     * @param editor OtsEditor; editor.
     * @param path String; path of nodes to register a string function with, used in a {@code String.endsWith()} manner.
     * @param attributes String...; attributes to show in the node name.
     * @throws RemoteException if an exception occurs while listening for new nodes.
     */
    public GenericStringFunction(final OtsEditor editor, final String path, final String... attributes) throws RemoteException
    {
        editor.addListener(this, OtsEditor.NEW_FILE);
        this.path = path;
        this.attributes = attributes;
    }

    /**
     * Sets the separator. Default is ", ".
     * @param separator String; separator between attribute values.
     */
    public void setSeparator(final String separator)
    {
        this.separator = separator;
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
    {
        XsdTreeNodeRoot root = (XsdTreeNodeRoot) event.getContent();
        root.addListener(new EventListener()
        {
            /** */
            private static final long serialVersionUID = 20230301L;

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
                        /** {@inheritDoc} */
                        @Override
                        public String apply(final XsdTreeNode t)
                        {
                            String sep = "";
                            String out = "";
                            for (String attribute : GenericStringFunction.this.attributes)
                            {
                                String value = node.getAttributeValue(attribute);
                                if (value != null)
                                {
                                    out = out + sep + value;
                                    sep = GenericStringFunction.this.separator;
                                }
                            }
                            return out;
                        }
                    });
                }
            }
        }, XsdTreeNodeRoot.NODE_CREATED);
    }

}
