package org.opentrafficsim.editor;

import java.awt.Component;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.table.TableCellRenderer;

/**
 * Renderer for cells in the attributes table.
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

    /**
     * Constructor.
     * @param infoIcon Icon; info icon.
     */
    public AttributeCellRenderer(final Icon infoIcon)
    {
        setOpaque(true);
        this.infoIcon = infoIcon;
    }

    /** {@inheritDoc} */
    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
            final boolean hasFocus, final int row, final int column)
    {
        if (table.convertColumnIndexToModel(column) < 3)
        {
            setText(value == null ? "" : value.toString());
        }
        else
        {
            setText("");
        }
        setFont(table.getFont());
        table.setGridColor(UIManager.getColor("Panel.background"));
        Border border = EMPTY_BORDER;
        setToolTipText(null);
        setIcon(null);
        if (table.convertColumnIndexToModel(column) == 1)
        {
            XsdTreeNode node = ((XsdAttributesTableModel) table.getModel()).getNode();
            String message = node.reportInvalidAttributeValue(row);
            if (message != null)
            {
                setToolTipText(OtsEditor.limitTooltip(message));
                setBackground(OtsEditor.INVALID_COLOR);
            }
            else
            {
                setToolTipText(value == null ? null : value.toString());
                setBackground(UIManager.getColor("Table.background"));
            }
            border = new LineBorder(UIManager.getColor("Table.gridColor"));
        }
        else
        {
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
        setBorder(border);
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
