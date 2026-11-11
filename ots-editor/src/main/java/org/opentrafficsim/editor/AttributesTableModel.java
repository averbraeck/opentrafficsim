package org.opentrafficsim.editor;

import java.util.Arrays;
import java.util.Optional;

import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.editor.DocumentReader.NodeAnnotation;
import org.opentrafficsim.swing.gui.OtsSimulationPanel;
import org.w3c.dom.Node;

/**
 * Model for a {@code JTable} to display the attributes of a {@code XsdTreeNode}.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class AttributesTableModel extends AbstractTableModel implements EventListener
{

    /** Serialization version UID. */
    private static final long serialVersionUID = 20230217L;

    /** Column names. */
    private static final String[] COLUMN_NAMES = new String[] {"Property", "Value", "Use", ""};

    /** Index of the property column. */
    public static final int PROPERTY_COLUMN = Arrays.asList(COLUMN_NAMES).indexOf("Property");

    /** Index of the value column. */
    public static final int VALUE_COLUMN = Arrays.asList(COLUMN_NAMES).indexOf("Value");

    /** Index of the use column. */
    public static final int USE_COLUMN = Arrays.asList(COLUMN_NAMES).indexOf("Use");

    /** Index of the description column. */
    public static final int DESCRIPTION_COLUMN = Arrays.asList(COLUMN_NAMES).indexOf("");

    /** Minimum column widths. */
    private static final int[] MIN_COLUMN_WIDTHS = new int[] {50, 50, 30, 20};

    /** Preferred column widths. */
    private static final int[] PREFERRED_COLUMN_WIDTHS = new int[] {400, 400, 30, 20};

    /** The node of which the attributes are displayed. */
    private XsdTreeNode node;

    /**
     * Sets the node of which the attributes are displayed. A previous node, if any, is no longer listened to.
     * @param node node of which the attributes are displayed, may be {@code null}
     */
    public void setNode(final XsdTreeNode node)
    {
        if (this.node == node)
        {
            return;
        }
        if (this.node != null)
        {
            this.node.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
            this.node.removeListener(this, XsdTreeNode.ACTIVATION_CHANGED);
        }
        this.node = node;
        if (this.node != null)
        {
            this.node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
            this.node.addListener(this, XsdTreeNode.ACTIVATION_CHANGED);
        }
        fireTableDataChanged();
    }

    @Override
    public void notify(final Event event)
    {
        Object[] content = (Object[]) event.getContent();
        if (content[0] != this.node)
        {
            return;
        }
        if (event.getType().equals(XsdTreeNode.ATTRIBUTE_CHANGED))
        {
            int index = this.node.getAttributeIndexByName((String) content[1]);
            fireTableRowsUpdated(index, index);
        }
        else if (event.getType().equals(XsdTreeNode.ACTIVATION_CHANGED))
        {
            fireTableDataChanged();
        }
    }

    @Override
    public int getRowCount()
    {
        return this.node == null || !this.node.isActive() ? 0 : this.node.attributeCount();
    }

    @Override
    public int getColumnCount()
    {
        return COLUMN_NAMES.length;
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
        if (this.node.getPathString().startsWith(XsdPaths.DEFINITIONS)
                && "Builtin".equals(this.node.getAttributeNameByIndex(rowIndex))
                && "xsd:boolean".equals(this.node.getAttributeBaseType(rowIndex)))
        {
            // disable check boxes regarding the 'Builtin' status of definitions as definitions edited in the editor never are
            return false;
        }
        return columnIndex == VALUE_COLUMN && !this.node.isIncluded();
    }

    @Override
    public Object getValueAt(final int rowIndex, final int columnIndex)
    {
        Node attribute = this.node.getAttributeNode(rowIndex);
        if (columnIndex == PROPERTY_COLUMN)
        {
            return OtsSimulationPanel.separatedName(DocumentReader.getAttribute(attribute, "name").orElse(null));
        }
        else if (columnIndex == VALUE_COLUMN)
        {
            Object value = this.node.getAttributeValue(rowIndex);
            return value;
        }
        else if (columnIndex == USE_COLUMN)
        {
            Optional<String> use = DocumentReader.getAttribute(attribute, "use");
            return use.isPresent() && use.get().equals("required") ? "*" : "";
        }
        else if (columnIndex == DESCRIPTION_COLUMN)
        {
            return NodeAnnotation.DESCRIPTION.get(attribute).isPresent() ? "i" : null;
        }
        throw new IndexOutOfBoundsException();
    }

    @Override
    public void setValueAt(final Object aValue, final int rowIndex, final int columnIndex)
    {
        Throw.when(columnIndex != VALUE_COLUMN, IllegalArgumentException.class,
                "Attribute table model requested to set a value from a column that does not represent the attribute value.");
        if (aValue == null)
        {
            return;
        }
        if (this.node == null)
        {
            // node was deleted
            return;
        }
        this.node.setAttributeValue(rowIndex, aValue.toString());
    }

    /**
     * Returns the underlying node for which attributes are shown.
     * @return underlying node for which attributes are shown.
     */
    public XsdTreeNode getNode()
    {
        return this.node;
    }

    /**
     * Apply the column widths to a newly created table.
     * @param attributeTable table.
     */
    public static void applyColumnWidth(final JTable attributeTable)
    {
        Throw.when(attributeTable.getColumnCount() != COLUMN_NAMES.length, IllegalArgumentException.class,
                "The number of columns in the table to show node attributes is not equal to the number of defined columns.");
        for (int i = 0; i < attributeTable.getColumnCount(); i++)
        {
            attributeTable.getColumn(COLUMN_NAMES[i]).setMinWidth(MIN_COLUMN_WIDTHS[i]);
            attributeTable.getColumn(COLUMN_NAMES[i]).setPreferredWidth(PREFERRED_COLUMN_WIDTHS[i]);
        }
    }

}
