package org.opentrafficsim.editor.extensions;

import java.rmi.RemoteException;
import java.util.function.Consumer;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.reference.ReferenceType;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdPaths;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeRoot;

/**
 * Allows the user to save definitions separately.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class DefinitionsSaver implements EventListener, Consumer<XsdTreeNode>
{
    /** */
    private static final long serialVersionUID = 20230914L;

    /** Editor. */
    private OtsEditor editor;

    /**
     * Constructor.
     * @param editor OtsEditor; editor.
     * @throws RemoteException if listener cannot be added.
     */
    public DefinitionsSaver(final OtsEditor editor) throws RemoteException
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
            if (!node.getPathString().equals(XsdPaths.DEFINITIONS + ".xi:include") && node.getParent() != null
                    && node.getParent().getPathString().equals(XsdPaths.DEFINITIONS))
            {
                node.addConsumer("Save as include file...", this);
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
        this.editor.saveFileAs(node);
    }
}
