package org.opentrafficsim.editor.render;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;

import org.opentrafficsim.editor.AttributesTableModel;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;

/**
 * Renderer for cells in the attributes table. Provides a {@code JCheckBox} for boolean-type attributes (those that cannot be
 * specified with an expression).
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class AttributeCellRenderer extends JLabel implements TableCellRenderer
{

    /** */
    private static final long serialVersionUID = 20230226L;

    /** Empty border for re-use. */
    private static final Border EMPTY_BORDER = new EmptyBorder(0, 0, 0, 0);

    /** Info icon. */
    private final Icon infoIcon;

    /** Checkbox to use for boolean types. */
    private final JCheckBox checkBox = new JCheckBox();

    /**
     * Constructor.
     * @param infoIcon Icon; info icon.
     */
    public AttributeCellRenderer(final Icon infoIcon)
    {
        setOpaque(true);
        this.infoIcon = infoIcon;
        this.checkBox.setBorder(EMPTY_BORDER);
    }

    /** {@inheritDoc} */
    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
            final boolean hasFocus, final int row, final int column)
    {
        XsdTreeNode node = null;
        if (table.convertColumnIndexToModel(column) == 1)
        {
            node = ((AttributesTableModel) table.getModel()).getNode();
            String baseType = node.getAttributeBaseType(row);
            if ("xsd:boolean".equals(baseType))
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
                    else if (node.attributeIsExpression(row))
                    {
                        this.checkBox.setBackground(OtsEditor.EXPRESSION_COLOR);
                    }
                    else
                    {
                        this.checkBox.setBackground(UIManager.getColor("Panel.background"));
                    }
                }
                if (value == null || value.toString().isEmpty())
                {
                    String defaultValue = node.getDefaultAttributeValue(row);
                    this.checkBox.setSelected(defaultValue != null && defaultValue.toString().equalsIgnoreCase("true"));
                    this.checkBox.setText(" (default)");
                    this.checkBox.setFont(table.getFont());
                }
                else
                {
                    this.checkBox.setSelected(value.toString().equalsIgnoreCase("true"));
                    this.checkBox.setText("");
                }
                // All xsd:boolean attributes under Ots.Definitions are 'Default' that should be disabled and false by default.
                if (node.getPathString().startsWith("Ots.Definitions"))
                {
                    this.checkBox.setEnabled(false);
                }
                else
                {
                    this.checkBox.setEnabled(true);
                }
                return this.checkBox;
            }
        }

        boolean showingDefault = false;
        if (table.convertColumnIndexToModel(column) == 1)
        {
            if (value == null || value.toString().isEmpty())
            {
                node = ((AttributesTableModel) table.getModel()).getNode();
                String defaultValue = node.getDefaultAttributeValue(row);
                showingDefault = defaultValue != null;
                setText(showingDefault ? defaultValue : "");
            }
            else
            {
                setText(value.toString());
            }
        }
        else if (table.convertColumnIndexToModel(column) < 3)
        {
            setText(value == null ? "" : value.toString());
        }
        else
        {
            setText("");
        }
        setFont(table.getFont());
        table.setGridColor(UIManager.getColor("Panel.background"));
        setIcon(null);
        setForeground(showingDefault ? OtsEditor.INACTIVE_COLOR : UIManager.getColor("Table.foreground"));
        if (table.convertColumnIndexToModel(column) == 1)
        {
            String message = node.isSelfValid() ? null : node.reportInvalidAttributeValue(row);
            if (message != null)
            {
                setToolTipText(OtsEditor.limitTooltip(message));
                setBackground(OtsEditor.INVALID_COLOR);
            }
            else
            {
                setToolTipText(value == null || value.toString().isEmpty() ? null : value.toString());
                if (node.isInclude())
                {
                    setBackground(UIManager.getColor("Panel.background"));
                }
                else if (node.attributeIsExpression(row))
                {
                    setBackground(OtsEditor.EXPRESSION_COLOR);
                }
                else
                {
                    setBackground(UIManager.getColor("Table.background"));
                }
            }
            setBorder(new LineBorder(UIManager.getColor("Table.gridColor")));
        }
        else
        {
            setToolTipText(null);
            setBorder(EMPTY_BORDER);
            if (table.convertColumnIndexToModel(column) == 3 && value != null)
            {
                setIcon(this.infoIcon);
            }
            if (isSelected)
            {
                setBackground(UIManager.getColor("Table.selectionBackground"));
            }
            else
            {
                setBackground(UIManager.getColor("Panel.background"));
            }
        }
        if (table.convertColumnIndexToModel(column) > 1)
        {
            setHorizontalAlignment(SwingConstants.CENTER);
        }
        else
        {
            setHorizontalAlignment(SwingConstants.LEFT);
        }
        return this;
    }

}
