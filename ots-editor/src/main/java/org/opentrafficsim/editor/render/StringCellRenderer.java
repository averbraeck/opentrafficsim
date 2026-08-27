package org.opentrafficsim.editor.render;

import java.awt.Component;
import java.util.Optional;
import java.util.function.Supplier;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;

import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeTableModel;

import de.javagl.treetable.JTreeTable;

/**
 * Renderer for columns in the tree table.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class StringCellRenderer extends JLabel implements TableCellRenderer
{

    /** Serialization version UID. */
    private static final long serialVersionUID = 20230218L;

    /** Tree table. */
    private final JTreeTable treeTable;

    /**
     * Constructor.
     * @param treeTable tree table.
     */
    public StringCellRenderer(final JTreeTable treeTable)
    {
        this.treeTable = treeTable;
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
            final boolean hasFocus, final int row, final int column)
    {
        setFont(table.getFont());
        int treeColumn = this.treeTable.convertColumnIndexToView(XsdTreeTableModel.TREE_COLUMN); // columns may be moved
        int idColumn = this.treeTable.convertColumnIndexToView(XsdTreeTableModel.ID_COLUMN);
        int valueColumn = this.treeTable.convertColumnIndexToView(XsdTreeTableModel.VALUE_COLUMN);
        XsdTreeNode node = (XsdTreeNode) this.treeTable.getValueAt(row, treeColumn);
        String val = value == null ? null : value.toString();
        boolean defaultValue = false;
        if (val == null)
        {
            Supplier<Optional<String>> def;
            if (column == idColumn)
            {
                val = node.getAttributeValueOrDefault("Id");
                def = () -> node.getDefaultAttributeValue(node.getAttributeIndexByName("Id"));
            }
            else if (column == valueColumn)
            {
                val = node.getValue();
                def = () -> node.getDefaultValue();
            }
            else
            {
                val = "";
                def = () -> Optional.empty();
            }
            if (val == null)
            {
                Optional<String> defaultVal = def.get();
                val = defaultVal.orElse("");
                defaultValue = defaultVal.isPresent();
            }
        }
        setText(val);
        setForeground(defaultValue ? OtsEditor.getInactiveColor() : UIManager.getColor("Table.foreground"));
        String message = node.isSelfValid() ? null : (column == idColumn ? node.reportInvalidId().orElse(null)
                : (column == valueColumn ? node.reportInvalidValue().orElse(null) : null));
        if (this.treeTable.isCellEditable(row, column))
        {
            if (message != null)
            {
                setToolTipText(OtsEditor.limitTooltip(message));
                setBackground(OtsEditor.getInvalidColor());
            }
            else
            {
                setToolTipText(
                        OtsEditor.limitTooltip(!val.isEmpty() && (column == idColumn || column == valueColumn) ? val : null));
                boolean expression =
                        column == idColumn ? node.idIsExpression() : (column == valueColumn ? node.valueIsExpression() : false);
                if (expression)
                {
                    setBackground(OtsEditor.getExpressionColor());
                }
                else
                {
                    setBackground(isSelected ? UIManager.getColor("Table.selectionBackground")
                            : UIManager.getColor("Table.background"));
                }
            }
        }
        else
        {
            setToolTipText(null);
            setBackground(
                    isSelected ? UIManager.getColor("Table.selectionBackground") : UIManager.getColor("Table.background"));
        }

        Border border;
        if (this.treeTable.isCellEditable(row, column))
        {
            border = new LineBorder(UIManager.getColor("Table.gridColor"));
        }
        else
        {
            border = new EmptyBorder(0, 0, 0, 0);
        }
        setBorder(border);

        return this;
    }

}
