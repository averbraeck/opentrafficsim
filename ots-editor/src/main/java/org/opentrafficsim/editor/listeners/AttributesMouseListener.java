package org.opentrafficsim.editor.listeners;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Optional;

import javax.swing.JTable;

import org.opentrafficsim.editor.AttributesTableModel;
import org.opentrafficsim.editor.DocumentReader;
import org.opentrafficsim.editor.DocumentReader.NodeAnnotation;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;

/**
 * Listener to the mouse for the attributes table. This listener:
 * <ul>
 * <li>Creates a dialog showing an attribute description when the "i" icon is clicked.</li>
 * <li>Sets the editor status label as either the invalidation message of an attribute, or the attribute description.</li>
 * <li>Creates a popup when an attribute is selected that has restriction by having an xsd:keyref or being an
 * xsd:enumaration.</li>
 * </ul>
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
        // make description appear when information icon was clicked
        int col = this.attributesTable.columnAtPoint(e.getPoint());
        int row = this.attributesTable.rowAtPoint(e.getPoint());
        XsdTreeNode node = ((AttributesTableModel) this.attributesTable.getModel()).getNode();
        Optional<String> description = NodeAnnotation.DESCRIPTION.get(node.getAttributeNode(row));
        if (description.isPresent()
                && this.attributesTable.convertColumnIndexToModel(col) == AttributesTableModel.DESCRIPTION_COLUMN
                && this.attributesTable.getModel().getValueAt(row, col) != null)
        {
            this.editor.showDescription(description.orElse(null), node.getAttributeNameByIndex(row));
        }
        // set status label to invalid message or description
        this.editor.removeStatusLabel();
        String status = null;
        if (!node.isSelfValid())
        {
            status = node.reportInvalidAttributeValue(row).orElse(null);
        }
        if (status == null)
        {
            status = DocumentReader.filterHtml(description.orElse(null));
        }
        if (status != null)
        {
            this.editor.setStatusLabel(status);
        }
    }

    @Override
    public void mouseReleased(final MouseEvent e)
    {
        // show popup for attributes with a selection of allowable values (xsd:keyref, xsd:enumeration)
        int col = this.attributesTable.columnAtPoint(e.getPoint());
        if (this.attributesTable.convertColumnIndexToModel(col) == AttributesTableModel.VALUE_COLUMN)
        {
            int row = this.attributesTable.rowAtPoint(e.getPoint());
            XsdTreeNode node = ((AttributesTableModel) this.attributesTable.getModel()).getNode();
            if (!node.isIncluded())
            {
                List<String> allOptions = node.getAttributeRestrictions(row);
                this.editor.valueOptionsPopup(allOptions, this.attributesTable, (t) -> node.setAttributeValue(row, t),
                        node.getAttributeValue(row));
            }
        }
    }

}
