package org.opentrafficsim.editor.listeners;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.DefaultCellEditor;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;

import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;

import de.javagl.treetable.JTreeTable;

/**
 * Sets Id or Value while typing in the JTreeTable. Starts undo action (ending the previous one) whenever editing stops or focus
 * is gained or lost.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class XsdTreeEditorListener extends KeyAdapter implements FocusListener, CellEditorListener
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
        cellEditor.getComponent().addKeyListener(this);
        cellEditor.getComponent().addFocusListener(this);
        cellEditor.addCellEditorListener(this);

        // tree table key listener (when editing is initiated by typing with an editable cell selected)
        this.treeTable.addKeyListener(new KeyAdapter()
        {
            @Override
            public void keyReleased(final KeyEvent e)
            {
                if (XsdTreeEditorListener.this.treeTable.isEditing())
                {
                    setValue((JTextField) XsdTreeEditorListener.this.treeTable.getEditorComponent());
                }
            }
        });
    }

    // editor text field key listener (when editing is initiated by double clicking a cell)

    @Override
    public void keyReleased(final KeyEvent e)
    {
        setValue((JTextField) e.getComponent());
    }

    /**
     * Set value based on content of JTextField, which is the Component of either the default String or Object editor.
     * @param textField text field
     */
    private void setValue(final JTextField textField)
    {
        int editorCol = this.treeTable.convertColumnIndexToView(this.treeTable.getSelectedColumn());
        if (editorCol == 1 || editorCol == 2)
        {
            int row = this.treeTable.getSelectedRow();
            int col = this.treeTable.convertColumnIndexToView(0); // columns may have been moved in view
            XsdTreeNode treeNode = (XsdTreeNode) this.treeTable.getValueAt(row, col);
            if (editorCol == 1)
            {
                treeNode.setId(textField.getText());
            }
            else if (editorCol == 2)
            {
                treeNode.setValue(textField.getText());
            }
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
