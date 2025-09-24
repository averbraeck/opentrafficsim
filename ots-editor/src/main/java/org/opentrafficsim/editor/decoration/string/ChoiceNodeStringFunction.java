package org.opentrafficsim.editor.decoration.string;

import java.rmi.RemoteException;
import java.util.function.Function;

import org.djutils.event.Event;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.decoration.AbstractNodeDecorator;

/**
 * In nodes that may only contain one child node which is a choice, displays the chosen child value.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ChoiceNodeStringFunction extends AbstractNodeDecorator
{

    /** */
    private static final long serialVersionUID = 20230910L;

    /**
     * Constructor.
     * @param editor editor.
     */
    public ChoiceNodeStringFunction(final OtsEditor editor)
    {
        super(editor, (n) -> true);
    }

    @Override
    public void notify(final Event event) throws RemoteException
    {
        if (event.getType().equals(XsdTreeNode.ACTIVATION_CHANGED))
        {
            Object[] content = (Object[]) event.getContent();
            XsdTreeNode node = (XsdTreeNode) content[0];
            setStringFunctionWhenOnlyChoice(node);
            node.removeListener(this, XsdTreeNode.ACTIVATION_CHANGED);
        }
        super.notify(event);
    }

    @Override
    public void notifyCreated(final XsdTreeNode node)
    {
        /*
         * Checking whether the node may only contain a single choice node, and no other nodes, requires the children to be
         * present. This cannot be done on an inactive node. Therefore, if the node is inactive, this class will listen to the
         * activation status and do the check upon first activation.
         */
        if (!node.isActive())
        {
            node.addListener(ChoiceNodeStringFunction.this, XsdTreeNode.ACTIVATION_CHANGED);
        }
        else
        {
            setStringFunctionWhenOnlyChoice(node);
        }
    }

    /**
     * Sets the right string function if the node may only contain one child node which is a choice.
     * @param node node.
     */
    private void setStringFunctionWhenOnlyChoice(final XsdTreeNode node)
    {
        if (node.isSingleChoiceType())
        {
            node.setStringFunction(new Function<XsdTreeNode, String>()
            {
                @Override
                public String apply(final XsdTreeNode node)
                {
                    // if 0 children, node was deleted
                    return node.getChildCount() > 0 ? node.getChild(0).toString() : "";
                }
            }, false);
        }
    }

}
