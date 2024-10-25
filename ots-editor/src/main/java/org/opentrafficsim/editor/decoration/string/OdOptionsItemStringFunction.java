package org.opentrafficsim.editor.decoration.string;

import java.util.function.Function;

import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdPaths;
import org.opentrafficsim.editor.XsdTreeNode;

/**
 * Displays the options scope: Global, LinkType, Origin or Link+Lane.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class OdOptionsItemStringFunction extends AbstractStringFunction
{

    /** */
    private static final long serialVersionUID = 20230911L;

    /**
     * Constructor.
     * @param editor editor.
     */
    public OdOptionsItemStringFunction(final OtsEditor editor)
    {
        super(editor, (node) -> node.getPathString().equals(XsdPaths.OD_OPTIONS_ITEM));
    }

    @Override
    public Function<XsdTreeNode, String> getStringFunction()
    {
        return (node) ->
        {
            for (XsdTreeNode child : node.getChildren())
            {
                if ("Global".equals(child.getNodeName()))
                {
                    return "Global";
                }
                if ("LinkType".equals(child.getNodeName()) || "Origin".equals(child.getNodeName()))
                {
                    return child.getValue() == null || child.getValue().isEmpty() ? child.getNodeName()
                            : child.getNodeName() + " " + child.getValue();
                }
                if ("Lane".equals(child.getNodeName()))
                {
                    String link = child.getAttributeValue("Link");
                    String lane = child.getAttributeValue("Lane");
                    if (link == null || link.isEmpty())
                    {
                        if (lane == null || lane.isEmpty())
                        {
                            return "Lane";
                        }
                        return "Lane " + child.getAttributeValue("Lane");
                    }
                    else if (lane == null || lane.isEmpty())
                    {
                        return "Lane " + child.getAttributeValue("Link") + ".";
                    }
                    return "Lane " + child.getAttributeValue("Link") + "." + child.getAttributeValue("Lane");
                }
            }
            return null;
        };
    }

}
