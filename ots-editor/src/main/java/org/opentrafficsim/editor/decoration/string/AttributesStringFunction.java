package org.opentrafficsim.editor.decoration.string;

import java.util.function.Function;

import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;

/**
 * Generic implementation to enhance the information in nodes as displayed in the tree, by showing a few attribute values.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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
     * @param editor editor.
     * @param path path of nodes to register a string function with, used in a {@code String.endsWith()} manner.
     * @param attributes attributes to show in the node name.
     */
    public AttributesStringFunction(final OtsEditor editor, final String path, final String... attributes)
    {
        super(editor, (node) -> node.getPathString().endsWith(path));
        editor.addListener(this, OtsEditor.NEW_FILE);
        this.attributes = attributes;
    }

    /**
     * Sets the separator. Default is ", ".
     * @param separator separator between attribute values.
     */
    public void setSeparator(final String separator)
    {
        this.separator = separator;
    }

    /** {@inheritDoc} */
    @Override
    public Function<XsdTreeNode, String> getStringFunction()
    {
        return (node) ->
        {
            String sep = "";
            String out = "";
            for (String attribute : AttributesStringFunction.this.attributes)
            {
                String value = node.getAttributeValue(attribute);
                if (value != null && !value.isEmpty())
                {
                    out = out + sep + value;
                    sep = AttributesStringFunction.this.separator;
                }
            }
            return out;
        };
    }

}
