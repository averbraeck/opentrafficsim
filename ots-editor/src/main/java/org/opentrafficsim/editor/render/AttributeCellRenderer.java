package org.opentrafficsim.editor.render;

import java.awt.Color;
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
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
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

    /** Selection color. */
    private final Color selectionColor = UIManager.getColor("Table.selectionBackground");

    /** Selection background color. */
    private final Color tableSelectionBackgroundColor = UIManager.getColor("Table.selectionBackground");

    /** Foreground color. */
    private final Color tableForgroundColor = UIManager.getColor("Table.foreground");

    /** Background color. */
    private final Color tableBackroundColor = UIManager.getColor("Table.background");

    /** Panel color. */
    private final Color panelBackgroundColor = UIManager.getColor("Panel.background");

    /** Line border for editable column. */
    private final Border lineBorder = new LineBorder(UIManager.getColor("Table.gridColor"));

    /**
     * Constructor.
     * @param infoIcon info icon.
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
                        this.checkBox.setBackground(this.selectionColor);
                    }
                    else if (node.attributeIsExpression(row))
                    {
                        this.checkBox.setBackground(OtsEditor.EXPRESSION_COLOR);
                    }
                    else
                    {
                        this.checkBox.setBackground(table.getBackground());
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
        table.setGridColor(table.getBackground());
        setIcon(null);
        setForeground(showingDefault ? OtsEditor.INACTIVE_COLOR : this.tableForgroundColor);
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
                    setBackground(this.panelBackgroundColor);
                }
                else if (node.attributeIsExpression(row))
                {
                    setBackground(OtsEditor.EXPRESSION_COLOR);
                }
                else
                {
                    setBackground(this.tableBackroundColor);
                }
            }
            setBorder(this.lineBorder);
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
                setBackground(this.tableSelectionBackgroundColor);
            }
            else
            {
                setBackground(table.getBackground());
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
