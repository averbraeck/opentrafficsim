package org.opentrafficsim.editor.decoration;

import java.rmi.RemoteException;
import java.util.function.Function;

import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;

/**
 * Generic implementation to enhance the information in nodes as displayed in the tree, by showing a few attribute values.
 * @author wjschakel
 */
public class AttributesStringFunction extends AbstractStringFunction
{

    /** */
    private static final long serialVersionUID = 20220301L;

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
    public AttributesStringFunction(final OtsEditor editor, final String path, final String... attributes)
            throws RemoteException
    {
        super(editor, (node) -> node.getPathString().endsWith(path));
        editor.addListener(this, OtsEditor.NEW_FILE);
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
    public Function<XsdTreeNode, String> getStringFunction()
    {
        return new Function<XsdTreeNode, String>()
        {
            /** {@inheritDoc} */
            @Override
            public String apply(final XsdTreeNode node)
            {
                String sep = "";
                String out = "";
                for (String attribute : AttributesStringFunction.this.attributes)
                {
                    String value = node.getAttributeValue(attribute);
                    if (value != null)
                    {
                        out = out + sep + value;
                        sep = AttributesStringFunction.this.separator;
                    }
                }
                return out;
            }
        };
    }

}
