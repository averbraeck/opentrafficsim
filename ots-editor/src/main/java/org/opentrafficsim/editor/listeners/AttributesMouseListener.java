package org.opentrafficsim.editor.listeners;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JTable;

import org.opentrafficsim.editor.AttributesTableModel;
import org.opentrafficsim.editor.DocumentReader.NodeAnnotation;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;

/**
 * Listener to the mouse for the attributes table.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AttributesMouseListener extends MouseAdapter
{

    /** Editor. */
    private final OtsEditor editor;

    /** Table with attributes. */
    private final JTable attributesTable;

    /**
     * Constructor.
     * @param editor editor.
     * @param attributesTable table with attributes.
     */
    public AttributesMouseListener(final OtsEditor editor, final JTable attributesTable)
    {
        this.editor = editor;
        this.attributesTable = attributesTable;
    }

    @Override
    public void mouseClicked(final MouseEvent e)
    {
        // makes description appear when information icon was clicked
        int col = this.attributesTable.columnAtPoint(e.getPoint());
        int row = this.attributesTable.rowAtPoint(e.getPoint());
        XsdTreeNode node = ((AttributesTableModel) this.attributesTable.getModel()).getNode();
        String description = NodeAnnotation.DESCRIPTION.get(node.getAttributeNode(row));
        if (this.attributesTable.convertColumnIndexToModel(col) == 3
                && this.attributesTable.getModel().getValueAt(row, col) != null)
        {
            this.editor.showDescription(description);
        }
        this.editor.removeStatusLabel();
        String status = null;
        if (!node.isSelfValid())
        {
            status = node.reportInvalidAttributeValue(row);
        }
        if (status == null)
        {
            status = description;
        }
        if (status != null)
        {
            this.editor.setStatusLabel(status);
        }
    }

    @Override
    public void mouseReleased(final MouseEvent e)
    {
        // shows popup for attributes with a selection of allowable values (xsd:keyref, xsd:enumeration)
        int col = this.attributesTable.columnAtPoint(e.getPoint());
        if (this.attributesTable.convertColumnIndexToModel(col) == 1)
        {
            int row = this.attributesTable.rowAtPoint(e.getPoint());
            XsdTreeNode node = ((AttributesTableModel) this.attributesTable.getModel()).getNode();
            if (!node.isInclude())
            {
                List<String> allOptions = node.getAttributeRestrictions(row);
                this.editor.valueOptionsPopup(allOptions, this.attributesTable, (t) -> node.setAttributeValue(row, t));
            }
        }
    }

}
