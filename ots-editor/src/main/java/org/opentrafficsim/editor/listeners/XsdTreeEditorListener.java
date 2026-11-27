package org.opentrafficsim.editor.listeners;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;

import org.opentrafficsim.editor.Actions;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeTableModel;

import de.javagl.treetable.JTreeTable;

/**
 * Listener to tree editors. This listener:
 * <ul>
 * <li>Sets Id or Value while typing in the JTreeTable, using the duplicate-key guard.</li>
 * <li>Commits the Id or Value on an explicit Enter, bypassing the duplicate-key guard.</li>
 * <li>Starts undo action (ending the previous one) whenever editing stops or focus is gained or lost in the JTreeTable
 * editor.</li>
 * </ul>
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class XsdTreeEditorListener implements FocusListener, CellEditorListener
{

    /** Editor. */
    private final OtsEditor editor;

    /** Tree table. */
    private final JTreeTable treeTable;

    /**
     * Constructor.
     * @param editor editor
     * @param treeTable tree table
     */
    public XsdTreeEditorListener(final OtsEditor editor, final JTreeTable treeTable)
    {
        this.editor = editor;
        this.treeTable = treeTable;
        DefaultCellEditor cellEditor = (DefaultCellEditor) this.treeTable.getDefaultEditor(String.class);
        cellEditor.getComponent().addFocusListener(this);
        cellEditor.addCellEditorListener(this);

        JTextField field = ((JTextField) cellEditor.getComponent());
        field.getDocument().addDocumentListener(Actions.documentListener(() ->
        {
            /*
             * This check before setting the value is quite important to prevent loss of typed data. As the JTextField is
             * re-used when editing another cell, the JTreeTable sets its value to null in the transition. This null value
             * should not be set on either the previous or next edited node.
             */
            if (XsdTreeEditorListener.this.treeTable.isEditing())
            {
                setValue(field, false);
            }
        }));
        field.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyPressed(final KeyEvent e)
            {
                if (e.getKeyCode() == KeyEvent.VK_ENTER && XsdTreeEditorListener.this.treeTable.isEditing())
                {
                    // Explicit Enter commits the typed value, even when it duplicates an existing Id or is empty.
                    setValue(field, true);
                }
            }
        });
    }

    /**
     * Set value based on content of JTextField, which is the Component of either the default String or Object editor.
     * @param textField text field
     * @param force whether to bypass the duplicate-key guard
     */
    private void setValue(final JTextField textField, final boolean force)
    {
        int modelCol = this.treeTable.convertColumnIndexToModel(this.treeTable.getSelectedColumn());
        if (modelCol == XsdTreeTableModel.ID_COLUMN || modelCol == XsdTreeTableModel.VALUE_COLUMN)
        {
            int row = this.treeTable.getSelectedRow();
            int treeCol = this.treeTable.convertColumnIndexToView(XsdTreeTableModel.TREE_COLUMN); // may be moved
            XsdTreeNode treeNode = (XsdTreeNode) this.treeTable.getValueAt(row, treeCol);
            XsdTreeTableModel model = (XsdTreeTableModel) this.treeTable.getTree().getModel();
            model.setValueAt(textField.getText(), treeNode, modelCol, force);
        }
    }

    // editor text field focus listener

    @Override
    public void focusGained(final FocusEvent e)
    {
        this.editor.startUndoActionOnTreeTable();
    }

    @Override
    public void focusLost(final FocusEvent e)
    {
        this.editor.startUndoActionOnTreeTable();
    }

    // cell editor listener

    @Override
    public void editingStopped(final ChangeEvent e)
    {
        this.editor.startUndoActionOnTreeTable();
    }

    @Override
    public void editingCanceled(final ChangeEvent e)
    {
        this.editor.startUndoActionOnTreeTable();
    }

}
