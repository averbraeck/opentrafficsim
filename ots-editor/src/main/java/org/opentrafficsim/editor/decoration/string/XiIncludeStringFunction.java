package org.opentrafficsim.editor.decoration.string;

import java.util.function.Function;

import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.decoration.validation.ValueValidator;

/**
 * Adds the included file name to the include node.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class XiIncludeStringFunction extends AbstractStringFunction
{

    /**
     * Constructor.
     * @param editor editor.
     */
    public XiIncludeStringFunction(final OtsEditor editor)
    {
        super(editor, (n) -> n.getNodeName().equals("xi:include"));
        setOverwrite(false);
    }

    @Override
    public Function<XsdTreeNode, String> getStringFunction()
    {
        return (node) ->
        {
            String fileValue = node.getAttributeValueOrDefault(0);
            String fallbackValue = node.getAttributeValueOrDefault(1);
            if (fileValue == null || node.getParent() == null)
            {
                return null;
            }
            String message = ValueValidator.checkImport(node.getRoot().getBaseUri(), fileValue);
            if (message == null)
            {
                return fileValue;
            }
            message = ValueValidator.checkImport(node.getRoot().getBaseUri(), fallbackValue);
            if (message == null)
            {
                return fallbackValue + " [fallback]";
            }
            return null;
        };
    }

}
