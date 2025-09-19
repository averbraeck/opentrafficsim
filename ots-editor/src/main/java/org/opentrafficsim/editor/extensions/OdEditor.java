package org.opentrafficsim.editor.extensions;

import java.io.IOException;
import java.rmi.RemoteException;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JLabel;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.decoration.DefaultDecorator;

/**
 * Editor for OD.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class OdEditor implements EventListener
{

    /** */
    private static final long serialVersionUID = 20230313L;

    /** Editor. */
    private final OtsEditor editor;

    /**
     * Constructor.
     * @param editor editor.
     * @throws IOException if icon cannot be loaded or listener cannot be added.
     */
    public OdEditor(final OtsEditor editor) throws IOException
    {
        ImageIcon odIcon = DefaultDecorator.loadIcon("./Table_blue.png", 16, 16, -1, -1);
        editor.addTab("OD", odIcon, buildOdPane(), null);
        editor.addListener(this, OtsEditor.SELECTION_CHANGED);
        this.editor = editor;
    }

    /**
     * Temporary stub to create OD pane.
     * @return component.
     */
    private static JComponent buildOdPane()
    {
        JLabel od = new JLabel("od");
        od.setOpaque(true);
        od.setHorizontalAlignment(JLabel.CENTER);
        return od;
    }

    @Override
    public void notify(final Event event) throws RemoteException
    {
        // TODO: this is a dummy implementation
        if (event.getType().equals(OtsEditor.SELECTION_CHANGED))
        {
            JLabel label = ((JLabel) this.editor.getTab("OD"));
            XsdTreeNode node = (XsdTreeNode) event.getContent();
            if (node.getPathString().startsWith("Ots.Demand.Od"))
            {
                label.setText(node.getPathString());
            }
            else
            {
                label.setText("od");
            }
        }
    }

}
