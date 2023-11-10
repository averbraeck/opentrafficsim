package org.opentrafficsim.editor.decoration;

import java.rmi.RemoteException;
import java.util.List;
import java.util.function.Consumer;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.reference.ReferenceType;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdOption;
import org.opentrafficsim.editor.XsdPaths;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeRoot;
import org.opentrafficsim.editor.Undo.ActionType;

/**
 * Allows a defined road layout selected at a link, to be copied in to a customizable road layout at the link.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class LayoutCustomizer implements EventListener, Consumer<XsdTreeNode>
{
    /** */
    private static final long serialVersionUID = 20231110L;

    /** Editor. */
    private OtsEditor editor;

    /**
     * Constructor.
     * @param editor OtsEditor; editor.
     * @throws RemoteException if listener cannot be added.
     */
    public LayoutCustomizer(final OtsEditor editor) throws RemoteException
    {
        editor.addListener(this, OtsEditor.NEW_FILE);
        this.editor = editor;
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
    {
        if (event.getType().equals(OtsEditor.NEW_FILE))
        {
            XsdTreeNodeRoot root = (XsdTreeNodeRoot) event.getContent();
            root.addListener(this, XsdTreeNodeRoot.NODE_CREATED, ReferenceType.WEAK);
            root.addListener(this, XsdTreeNodeRoot.NODE_REMOVED, ReferenceType.WEAK);
        }
        else if (event.getType().equals(XsdTreeNodeRoot.NODE_CREATED))
        {
            XsdTreeNode node = (XsdTreeNode) ((Object[]) event.getContent())[0];
            if (node.getPathString().equals(XsdPaths.LINK + ".xsd:sequence")
                    && node.getChild(0).getPathString().equals(XsdPaths.LINK + ".DefinedLayout"))
            {
                node.addConsumer("Customize", this);
            }
        }
        else if (event.getType().equals(XsdTreeNodeRoot.NODE_REMOVED))
        {
            XsdTreeNode node = (XsdTreeNode) ((Object[]) event.getContent())[0];
            node.removeListener(this, XsdTreeNodeRoot.NODE_REMOVED);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void accept(final XsdTreeNode node)
    {
        XsdTreeNode defined = node.getChild(0).getCoupledKeyrefNodeValue();
        if (defined == null)
        {
            // do nothing if there is no coupled defined road layout
            return;
        }
        List<XsdOption> options = node.getOptions();
        XsdTreeNode custom = null;
        for (XsdOption option : options)
        {
            if (!option.getOptionNode().equals(node))
            {
                custom = option.getOptionNode();
                break;
            }
        }
        List<XsdTreeNode> formerChildren = custom.getChildren();
        this.editor.getUndo().startAction(ActionType.ACTION, node, null);
        for (XsdTreeNode definedChild : defined.getChildren())
        {
            definedChild.duplicate(custom);
        }
        for (XsdTreeNode formerChild : formerChildren)
        {
            formerChild.remove();
        }
        custom.setAttributeValue("LaneKeeping", defined.getAttributeValue("LaneKeeping"));
        node.setOption(custom);
        this.editor.show(custom, null);
    }

}
