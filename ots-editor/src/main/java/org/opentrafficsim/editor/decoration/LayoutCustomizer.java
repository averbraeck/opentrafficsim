package org.opentrafficsim.editor.decoration;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

import org.djutils.event.EventListener;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.Undo.ActionType;
import org.opentrafficsim.editor.XsdOption;
import org.opentrafficsim.editor.XsdPaths;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeRoot;

/**
 * Allows a defined road layout selected at a link, to be copied in to a customizable road layout at the link.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class LayoutCustomizer extends AbstractNodeDecoratorRemove implements EventListener, Consumer<XsdTreeNode>
{
    /** Editor. */
    private OtsEditor editor;

    /**
     * Constructor.
     * @param editor editor.
     * @throws RemoteException if listener cannot be added.
     */
    public LayoutCustomizer(final OtsEditor editor) throws RemoteException
    {
        super(editor, (n) -> true);
        this.editor = editor;
    }

    @Override
    public void notifyCreated(final XsdTreeNode node)
    {
        if (node.getPathString().equals(XsdPaths.LINK + ".xsd:sequence") && node.getChildCount() > 0
                && node.getChild(0).getPathString().equals(XsdPaths.LINK + ".DefinedLayout"))
        {
            node.addConsumer("Customize", this);
        }
    }

    @Override
    public void notifyRemoved(final XsdTreeNode node)
    {
        node.removeListener(this, XsdTreeNodeRoot.NODE_REMOVED);
    }

    @Override
    public void accept(final XsdTreeNode node)
    {
        Optional<XsdTreeNode> defined = node.getChild(0).getCoupledNodeValue();
        if (defined.isEmpty())
        {
            // do nothing if there is no coupled defined road layout
            return;
        }
        List<XsdOption> options = node.getOptions();
        XsdTreeNode custom = null;
        for (XsdOption option : options)
        {
            if (!option.optionNode().equals(node))
            {
                custom = option.optionNode();
                break;
            }
        }
        List<XsdTreeNode> formerChildren = custom.getChildren();
        this.editor.getUndo().startAction(ActionType.ACTION, node, null);
        for (XsdTreeNode definedChild : defined.get().getChildren())
        {
            definedChild.duplicate(custom);
        }
        for (XsdTreeNode formerChild : formerChildren)
        {
            formerChild.remove();
        }
        custom.setAttributeValue("LaneKeeping", defined.get().getAttributeValue("LaneKeeping"));
        node.setOption(custom);
        this.editor.show(custom, null);
    }

}
