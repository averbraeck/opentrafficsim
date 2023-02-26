package org.opentrafficsim.editor;

import java.awt.Color;
import java.awt.Component;

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

    /**
     * Constructor.
     */
    public AttributeCellRenderer()
    {
        setOpaque(true);
        setForeground(Color.BLACK);
    }
    
    /** {@inheritDoc} */
    @Override
    public Component getTableCellRendererComponent(final JTable table, final Object value, final boolean isSelected,
            final boolean hasFocus, final int row, final int column)
    {
        setText(value == null ? "" : value.toString());
        setFont(table.getFont());
        table.setGridColor(UIManager.getColor("Panel.background"));
        setHorizontalAlignment(SwingConstants.RIGHT);
        Border border;
        if (table.convertColumnIndexToModel(column) == 1)
        {
            setBackground(UIManager.getColor("Table.background"));
            border = new LineBorder(UIManager.getColor("Table.gridColor"));
        }
        else
        {
            if (isSelected)
            {
                setBackground(UIManager.getColor("Table.selectionBackground"));
            }
            else
            {
                setBackground(UIManager.getColor("Panel.background"));
            }
            border = new EmptyBorder(0, 0, 0, 0);
        }
        setBorder(border);
        if (table.convertColumnIndexToModel(column) == 2)
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
