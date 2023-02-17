package org.opentrafficsim.editor;

import org.w3c.dom.Document;

import de.javagl.treetable.AbstractTreeTableModel;
import de.javagl.treetable.JTreeTable;
import de.javagl.treetable.TreeTableModel;

/**
 * Defines the columns in the {@code JTreeTable}. Most functionality is forwarded to the tree with {@code XsdTreeNode}'s.
 * @author wjschakel
 */
public class XsdTreeTableModel extends AbstractTreeTableModel
{

    private static final String[] COLUMN_NAMES = new String[] {"Item", "Id", "Value", "#"};

    private static final int[] MIN_COLUMN_WIDTHS = new int[] {100, 50, 50, 50};

    private static final int[] PREFERRED_COLUMN_WIDTHS = new int[] {500, 150, 150, 50};

    private static final Class<?>[] COLUMN_CLASSES =
            new Class<?>[] {TreeTableModel.class, String.class, Object.class, String.class};

    protected XsdTreeTableModel(final Document root)
    {
        super(new XsdTreeNode(null, root));
    }

    @Override
    public int getColumnCount()
    {
        return COLUMN_CLASSES.length;
    }

    @Override
    public String getColumnName(final int column)
    {
        return COLUMN_NAMES[column];
    }

    @Override
    public Class<?> getColumnClass(final int column)
    {
        return COLUMN_CLASSES[column];
    }

    @Override
    public Object getValueAt(final Object node, final int column)
    {
        if (column == 0)
        {
            return node; // required for tree view of column 0
        }
        if (column == 1)
        {
            return ((XsdTreeNode) node).getId();
        }
        else if (column == 2)
        {
            return ((XsdTreeNode) node).getValue();
        }
        // TODO: column 3 with 1, 0..1, 0..inf, 1..inf, etc.
        return "dummy value";
    }

    @Override
    public Object getChild(final Object parent, final int index)
    {
        return ((XsdTreeNode) parent).getChild(index);
    }

    @Override
    public int getChildCount(final Object parent)
    {
        return ((XsdTreeNode) parent).getChildCount();
    }

    @Override
    public boolean isCellEditable(final Object node, final int column)
    {
        System.out.println("Node editable? " + node);
        if (column == 0)
        {
            return true; // required for tree in column 0
        }
        return ((XsdTreeNode) node).isEditable() && column == 2;
    }

    @Override
    public void setValueAt(final Object aValue, final Object node, final int column)
    {
        if (column == 1)
        {
            ((XsdTreeNode) node).setId(aValue.toString());
        }
        else if (column == 2)
        {
            ((XsdTreeNode) node).setValue(aValue.toString());
        }
    }

    public static void applyColumnWidth(final JTreeTable treeTable)
    {
        for (int i = 0; i < treeTable.getColumnCount(); i++)
        {
            treeTable.getColumn(COLUMN_NAMES[i]).setMinWidth(MIN_COLUMN_WIDTHS[i]);
            treeTable.getColumn(COLUMN_NAMES[i]).setPreferredWidth(PREFERRED_COLUMN_WIDTHS[i]);
        }
    }

}
