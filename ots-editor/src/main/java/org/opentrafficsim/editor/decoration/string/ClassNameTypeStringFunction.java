package org.opentrafficsim.editor.decoration.string;

import java.util.function.Function;

import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;

/**
 * Displays the simple class name in nodes of ClassNameType.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ClassNameTypeStringFunction extends AbstractStringFunction
{

    /** */
    private static final long serialVersionUID = 20230910L;

    /**
     * Constructor.
     * @param editor OtsEditor; editor.
     */
    public ClassNameTypeStringFunction(final OtsEditor editor)
    {
        super(editor, (node) -> node.isType("ClassNameType"));
        this.overwrite = false;
    }

    /** {@inheritDoc} */
    @Override
    public Function<XsdTreeNode, String> getStringFunction()
    {
        return (node) ->
        {
            String value = node.getValue();
            if (value == null || value.isEmpty())
            {
                return "";
            }
            int dot = value.lastIndexOf(".");
            if (dot < 0 || dot == value.length() - 1)
            {
                return value;
            }
            return value.substring(dot + 1, value.length());
        };
    }

}
