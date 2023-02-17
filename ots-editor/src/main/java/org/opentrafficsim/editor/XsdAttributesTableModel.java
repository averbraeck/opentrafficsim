package org.opentrafficsim.editor;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.djutils.exceptions.Throw;
import org.w3c.dom.Node;

/**
 * Model for a {@code JTable} to display the attributes of a {@code XsdTreeNode}. 
 * @author wjschakel
 */
public class XsdAttributesTableModel extends AbstractTableModel
{

    private static final long serialVersionUID = 20230217L;

    private static final String[] COLUMN_NAMES = new String[] {"Use", "Name", "Value"};

    private static final int[] MIN_COLUMN_WIDTHS = new int[] {25, 50, 50};

    private static final int[] PREFERRED_COLUMN_WIDTHS = new int[] {50, 200, 200};

    private final XsdTreeNode node;

    public XsdAttributesTableModel(final XsdTreeNode node)
    {
        this.node = node;
    }

    @Override
    public int getRowCount()
    {
        return this.node == null ? 0 : this.node.attributeCount();
    }

    @Override
    public int getColumnCount()
    {
        return 3;
    }

    @Override
    public String getColumnName(final int columnIndex)
    {
        return COLUMN_NAMES[columnIndex];
    }

    @Override
    public Class<?> getColumnClass(final int columnIndex)
    {
        return String.class;
    }

    @Override
    public boolean isCellEditable(final int rowIndex, final int columnIndex)
    {
        return columnIndex == 2;
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex)
    {
        Node attribute = this.node.getAttributeNode(rowIndex);
        switch (columnIndex)
        {
            case 0:
                return attribute.getAttributes().getNamedItem("use").getNodeValue().equals("required") ? "*" : null;
            case 1:
                return attribute.getAttributes().getNamedItem("name").getNodeValue();
            case 2:
                return this.node.getAttributeValue(rowIndex);
            default:
                throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex)
    {
        Throw.when(columnIndex != 2, IllegalStateException.class,
                "Attribute table model requested to set a value from a column that does not represent the attribute value.");
        this.node.setAttributeValue(rowIndex, aValue.toString());
    }

    public static void applyColumnWidth(final JTable attributeTable)
    {
        for (int i = 0; i < attributeTable.getColumnCount(); i++)
        {
            attributeTable.getColumn(COLUMN_NAMES[i]).setMinWidth(MIN_COLUMN_WIDTHS[i]);
            attributeTable.getColumn(COLUMN_NAMES[i]).setPreferredWidth(PREFERRED_COLUMN_WIDTHS[i]);
        }
    }

}
