package org.opentrafficsim.editor.extensions;

import java.rmi.RemoteException;
import java.time.LocalDateTime;
import java.util.function.Consumer;

import javax.swing.JLabel;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeRoot;

/**
 * Editor for TrafCod program.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TrafCodEditor implements EventListener, Consumer<XsdTreeNode>
{
    /** Editor. */
    private OtsEditor editor;

    /**
     * Constructor.
     * @param editor editor.
     * @throws RemoteException if listener cannot be added.
     */
    public TrafCodEditor(final OtsEditor editor) throws RemoteException
    {
        editor.addListener(this, OtsEditor.NEW_FILE);
        this.editor = editor;
    }

    @Override
    public void notify(final Event event)
    {
        // TODO: this is a dummy implementation
        if (event.getType().equals(OtsEditor.NEW_FILE))
        {
            XsdTreeNodeRoot root = (XsdTreeNodeRoot) event.getContent();
            root.addListener(this, XsdTreeNodeRoot.NODE_CREATED);
        }
        else if (event.getType().equals(XsdTreeNodeRoot.NODE_CREATED))
        {
            XsdTreeNode node = (XsdTreeNode) ((Object[]) event.getContent())[0];
            if (node.isType("Ots.Control.TrafCod.Program"))
            {
                node.addConsumer("Configure...", this);
            }
        }
    }

    @Override
    public void accept(final XsdTreeNode t)
    {
        JLabel label = (JLabel) this.editor.getTab("Text").get();
        label.setText(LocalDateTime.now().toString());
        this.editor.focusTab("Text");
    }
}
