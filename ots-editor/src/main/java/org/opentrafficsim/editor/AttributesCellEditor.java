package org.opentrafficsim.editor;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

/**
 * Editor for attribute table cells. Extends the default cell editor with checkboxes for boolean attributes. This involves the
 * following:
 * <ul>
 * <li>It must return a {@code JCheckBox} as editor for boolean-typed attributes in {@code getTableCellEditorComponent()}.</li>
 * <li>Changing the checked status should directly set the value, even though we remain in editor mode. If the user e.g. clicks
 * on another node, setting the whole attributes window for that other node, we want the changed checkmark to have a lasting
 * effect. This is different from a {@code JTextField} active in editing mode. Then, clicking away during typing can be regarded
 * as a cancel action.</li>
 * <li>When the table system requests {@code getCellEditorValue()} as a response of the user clicking elsewhere in the table and
 * thus ending editing mode, the checked-status of the checkbox should be returned as "true" or "false", if it was the last
 * component for editing returned.</li>
 * </ul>
 * @author wjschakel
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

    /**
     * Constructor.
     */
    public AttributesCellEditor()
    {
        super(new JTextField());
        setClickCountToStart(1);
        this.checkBox.setBorder(new EmptyBorder(0, 0, 0, 0));
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
        if (table.convertColumnIndexToModel(column) == 1)
        {
            this.checkBox.setVisible(false);
            XsdTreeNode node = ((AttributesTableModel) table.getModel()).getNode();
            if ("xsd:boolean".equals(node.getAttributeBaseType(row)))
            {
                String message = node.reportInvalidAttributeValue(row);
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
