package org.opentrafficsim.editor;

import java.rmi.RemoteException;

import javax.swing.SwingUtilities;

import org.w3c.dom.Document;

import de.javagl.treetable.AbstractTreeTableModel;
import de.javagl.treetable.JTreeTable;
import de.javagl.treetable.TreeTableModel;

/**
 * Defines the columns in the {@code JTreeTable}. Most functionality is forwarded to the tree with {@code XsdTreeNode}'s.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class XsdTreeTableModel extends AbstractTreeTableModel
{

    /** Column names. */
    private static final String[] COLUMN_NAMES = new String[] {"Item", "Id", "Value", "#"};

    /** Minimum column widths. */
    private static final int[] MIN_COLUMN_WIDTHS = new int[] {100, 50, 50, 30};

    /** Preferred column widths. */
    private static final int[] PREFERRED_COLUMN_WIDTHS = new int[] {600, 200, 200, 30};

    /** Column classes. */
    private static final Class<?>[] COLUMN_CLASSES =
            new Class<?>[] {TreeTableModel.class, String.class, String.class, String.class};

    /** Tree table, so it can be updated visually when a value has changed. */
    private JTreeTable treeTable;

    /**
     * Constructor.
     * @param document Document; XSD document.
     * @throws RemoteException when unable to listen for created nodes.
     */
    protected XsdTreeTableModel(final Document document) throws RemoteException
    {
        super(document == null ? null : new XsdTreeNodeRoot(new Schema(document)));
    }

    /**
     * Sets the tree table.
     * @param treeTable JTreeTable; tree table.
     */
    public void setTreeTable(final JTreeTable treeTable)
    {
        this.treeTable = treeTable;
    }

    /** {@inheritDoc} */
    @Override
    public int getColumnCount()
    {
        return COLUMN_CLASSES.length;
    }

    /** {@inheritDoc} */
    @Override
    public String getColumnName(final int column)
    {
        return COLUMN_NAMES[column];
    }

    /** {@inheritDoc} */
    @Override
    public Class<?> getColumnClass(final int column)
    {
        return COLUMN_CLASSES[column];
    }

    /** {@inheritDoc} */
    @Override
    public Object getValueAt(final Object node, final int column)
    {
        if (column == 0)
        {
            return node; // required for tree view of column 0
        }
        if (column == 1)
        {
            if (((XsdTreeNode) node).isIdentifiable())
            {
                return ((XsdTreeNode) node).getId();
            }
            return "";
        }
        else if (column == 2)
        {
            if (((XsdTreeNode) node).isEditable())
            {
                return ((XsdTreeNode) node).getValue();
            }
            return "";
        }
        return occurs(((XsdTreeNode) node).minOccurs(), ((XsdTreeNode) node).maxOccurs());
    }

    /**
     * Creates a string to display minOccurs and maxOccurs.
     * @param minOccurs int; minOccurs.
     * @param maxOccurs int; maxOccurs.
     * @return String; string to display minOccurs and maxOccurs.
     */
    public String occurs(final int minOccurs, final int maxOccurs)
    {
        if (minOccurs == maxOccurs)
        {
            return Integer.toString(minOccurs);
        }
        return Integer.valueOf(minOccurs) + ".." + (maxOccurs == -1 ? "∞" : Integer.valueOf(maxOccurs));
    }

    /** {@inheritDoc} */
    @Override
    public Object getChild(final Object parent, final int index)
    {
        return ((XsdTreeNode) parent).getChild(index);
    }

    /** {@inheritDoc} */
    @Override
    public int getChildCount(final Object parent)
    {
        return ((XsdTreeNode) parent).getChildCount();
    }

    /** {@inheritDoc} */
    @Override
    public boolean isCellEditable(final Object node, final int column)
    {
        if (column == 0)
        {
            return true; // required for tree in column 0
        }
        XsdTreeNode treeNode = (XsdTreeNode) node;
        if (column == 1)
        {
            return treeNode.isIdentifiable() && !treeNode.isInclude();
        }
        return treeNode.isEditable() && column == 2 && !treeNode.isInclude();
    }

    /** {@inheritDoc} */
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
        // invoking directly results in a NullPointerException when clicking on the table during editing of a value in the table
        SwingUtilities.invokeLater(() -> this.treeTable.updateUI());
    }

    /**
     * Apply the column widths to a newly created tree table.
     * @param treeTable JTreeTable; tree table.
     */
    public static void applyColumnWidth(final JTreeTable treeTable)
    {
        for (int i = 0; i < treeTable.getColumnCount(); i++)
        {
            treeTable.getColumn(COLUMN_NAMES[i]).setMinWidth(MIN_COLUMN_WIDTHS[i]);
            treeTable.getColumn(COLUMN_NAMES[i]).setPreferredWidth(PREFERRED_COLUMN_WIDTHS[i]);
        }
    }

}
