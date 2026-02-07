package org.opentrafficsim.editor.decoration.string;

import java.util.function.Function;

import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;

/**
 * Generic implementation to enhance the information in nodes as displayed in the tree, by showing a few attribute values.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AttributesStringFunction extends AbstractStringFunction
{

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

    @Override
    public Function<XsdTreeNode, String> getStringFunction()
    {
        return (node) ->
        {
            // This will usually be a single attribute, and otherwise only a few. StringBuilder overhead probably not worth it.
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
