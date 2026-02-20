package org.opentrafficsim.editor.listeners;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.DefaultCellEditor;
import javax.swing.JTable;

import org.opentrafficsim.editor.AttributesTableModel;
import org.opentrafficsim.editor.DocumentReader.NodeAnnotation;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;

/**
 * Listener on the attributes table, and its editor, to show a description of the selected attribute when F1 is pressed.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AttributesKeyListener extends KeyAdapter
{

    /** Editor. */
    private final OtsEditor editor;

    /** Attributes table. */
    private final JTable attributesTable;

    /**
     * Constructor.
     * @param editor editor.
     * @param attributesTable tree table.
     */
    public AttributesKeyListener(final OtsEditor editor, final JTable attributesTable)
    {
        this.editor = editor;
        this.attributesTable = attributesTable;
        this.attributesTable.addKeyListener(this);
        ((DefaultCellEditor) this.attributesTable.getDefaultEditor(String.class)).getComponent().addKeyListener(this);
    }

    @Override
    public void keyPressed(final KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_F1)
        {
            XsdTreeNode node = ((AttributesTableModel) this.attributesTable.getModel()).getNode();
            int attribute = this.attributesTable.getSelectedRow();
            NodeAnnotation.DESCRIPTION.get(node.getAttributeNode(attribute))
                    .ifPresent((d) -> this.editor.showDescription(d, node.getAttributeNameByIndex(attribute)));
        }
    }

}
