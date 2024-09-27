package org.opentrafficsim.editor.render;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import org.opentrafficsim.editor.AttributesTableModel;
import org.opentrafficsim.editor.DocumentReader;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.Undo.ActionType;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.swing.gui.AppearanceControlTextField;

/**
 * Editor for attribute table cells. Extends the default cell editor with checkboxes for boolean attributes (those that cannot
 * be specified with an expression).
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AttributesCellEditor extends DefaultCellEditor
{
    /** */
    private static final long serialVersionUID = 20230203L;

    /** Checkbox to use as editor for boolean attributes. */
    private JCheckBox checkBox = new JCheckBox();

    /** Listener that will set the boolean on the right attribute, must be unregistered for a next edited boolean attribute. */
    private ActionListener lastActionListener;

    /** Whether the editor is currently using a checkbox. */
    private boolean checkMode;

    /** Editor. */
    private final OtsEditor editor;

    /**
     * Constructor.
     * @param table table of the attributes.
     * @param editor editor.
     */
    public AttributesCellEditor(final JTable table, final OtsEditor editor)
    {
        super(new AppearanceControlTextField());
        getComponent().addKeyListener(new KeyAdapter()
        {
            /** {@inheritDoc} */
            @Override
            public void keyReleased(final KeyEvent e)
            {
                int col = table.getSelectedColumn();
                int editorCol = table.convertColumnIndexToView(col);
                if (editorCol == 1)
                {
                    int row = table.getSelectedRow();
                    String value = ((JTextField) e.getComponent()).getText();
                    table.getModel().setValueAt(value, row, col);
                }
            }
        });
        setClickCountToStart(1);
        this.checkBox.setBorder(new EmptyBorder(0, 0, 0, 0));
        this.editor = editor;
    }

    /** {@inheritDoc} */
    @Override
    public Object getCellEditorValue()
    {
        if (this.checkMode)
        {
            this.checkMode = false;
            return Boolean.toString(this.checkBox.isSelected());
        }
        return super.getCellEditorValue();
    }

    /** {@inheritDoc} */
    @Override
    public Component getTableCellEditorComponent(final JTable table, final Object value, final boolean isSelected,
            final int row, final int column)
    {
        XsdTreeNode node = ((AttributesTableModel) table.getModel()).getNode();
        String attribute = DocumentReader.getAttribute(node.getAttributeNode(row), "name");
        this.editor.getUndo().startAction(ActionType.ATTRIBUTE_CHANGE, node, attribute);
        if (table.convertColumnIndexToModel(column) == 1)
        {
            this.checkBox.setVisible(false);
            if ("xsd:boolean".equals(node.getAttributeBaseType(row)))
            {
                String message = node.isSelfValid() ? null : node.reportInvalidAttributeValue(row);
                if (message != null)
                {
                    this.checkBox.setToolTipText(OtsEditor.limitTooltip(message));
                    this.checkBox.setBackground(OtsEditor.INVALID_COLOR);
                }
                else
                {
                    this.checkBox.setToolTipText(null);
                    if (isSelected)
                    {
                        this.checkBox.setBackground(UIManager.getColor("Table.selectionBackground"));
                    }
                    else
                    {
                        this.checkBox.setBackground(UIManager.getColor("Panel.background"));
                    }
                }
                this.checkBox.setSelected(value != null && value.toString().equalsIgnoreCase("true"));
                this.checkBox.setVisible(true);
                this.checkBox.removeActionListener(this.lastActionListener);
                this.lastActionListener = new ActionListener()
                {
                    /** {@inheritDoc} */
                    @Override
                    public void actionPerformed(final ActionEvent e)
                    {
                        int dataColumn = table.convertColumnIndexToModel(column);
                        table.getModel().setValueAt(Boolean.toString(AttributesCellEditor.this.checkBox.isSelected()), row,
                                dataColumn);
                        AttributesCellEditor.this.checkBox.setToolTipText(null);
                        AttributesCellEditor.this.checkBox.setBackground(UIManager.getColor("Table.selectionBackground"));
                    }
                };
                this.checkBox.addActionListener(this.lastActionListener);
                this.checkMode = true;
                return this.checkBox;
            }
        }
        this.checkMode = false;
        // JTable.GenericEditor cannot be extended, setting a black border on the wrapped JTextField is the main thing it does
        ((JComponent) getComponent()).setBorder(new LineBorder(Color.black));
        return super.getTableCellEditorComponent(table, value, isSelected, row, column);
    }
}
