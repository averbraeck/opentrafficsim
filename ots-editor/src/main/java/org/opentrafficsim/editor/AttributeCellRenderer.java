package org.opentrafficsim.editor;

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

/**
 * Renderer for cells in the attributes table. Provides a {JCheckBox} for boolean-type attributes.
 * @author wjschakel
 */
public class AttributeCellRenderer extends JLabel implements TableCellRenderer
{

    /** */
    private static final long serialVersionUID = 20230226L;

    /** Empty border for re-use. */
    private static final Border EMPTY_BORDER = new EmptyBorder(0, 0, 0, 0);

    /** Info icon. */
    private Icon infoIcon;

    /** Checkbox to use for boolean types. */
    private JCheckBox checkBox = new JCheckBox();

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
        setForeground(
                showingDefault ? OtsEditor.INACTIVE_COLOR : UIManager.getColor("Table.foreground"));
        if (table.convertColumnIndexToModel(column) == 1)
        {
            String message = node.reportInvalidAttributeValue(row);
            if (message != null)
            {
                setToolTipText(OtsEditor.limitTooltip(message));
                setBackground(OtsEditor.INVALID_COLOR);
            }
            else
            {
                setToolTipText(value == null || value.toString().isBlank() ? null : value.toString());
                if (node.isInclude())
                {
                    setBackground(UIManager.getColor("Panel.background"));
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
