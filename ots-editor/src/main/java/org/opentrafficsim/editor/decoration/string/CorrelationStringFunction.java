package org.opentrafficsim.editor.decoration.string;

import java.util.function.Function;

import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdPaths;
import org.opentrafficsim.editor.XsdTreeNode;

/**
 * Displays the expression on a correlation node.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class CorrelationStringFunction extends AbstractStringFunction
{

    /** */
    private static final long serialVersionUID = 20231011L;

    /**
     * Constructor.
     * @param editor editor.
     */
    public CorrelationStringFunction(final OtsEditor editor)
    {
        super(editor, (node) -> node.getPathString().equals(XsdPaths.CORRELATION));
    }

    @Override
    public Function<XsdTreeNode, String> getStringFunction()
    {
        return (node) ->
        {
            String out = node.getAttributeValue("Expression");
            if (out == null)
            {
                return "";
            }
            out = "then = " + out;
            if (node.getChild(0).isActive() && node.getChild(0).getChild(0).getValue() != null)
            {
                out = out.replace("first", node.getChild(0).getChild(0).getValue());
            }
            if (node.getChild(1).getChild(0).getValue() != null)
            {
                out = out.replace("then", node.getChild(1).getChild(0).getValue());
            }
            return out;
        };
    }

}
