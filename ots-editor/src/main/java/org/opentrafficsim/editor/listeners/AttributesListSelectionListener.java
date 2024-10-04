package org.opentrafficsim.editor.listeners;

import java.rmi.RemoteException;

import javax.swing.JTable;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.opentrafficsim.editor.AttributesTableModel;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;

/**
 * Listener for events when an attribute is selected in the table.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class AttributesListSelectionListener implements ListSelectionListener, EventListener
{

    /** */
    private static final long serialVersionUID = 20230923L;

    /** Editor. */
    private final OtsEditor editor;

    /** Table with attributes. */
    private final JTable attributesTable;

    /** Node that we are listening to. */
    private XsdTreeNode listening;

    /** Attribute of selected row. */
    private String attribute;

    /**
     * Constructor.
     * @param editor editor.
     * @param attributesTable table with attributes.
     */
    public AttributesListSelectionListener(final OtsEditor editor, final JTable attributesTable)
    {
        this.editor = editor;
        this.attributesTable = attributesTable;
    }

    /** {@inheritDoc} */
    @Override
    public void valueChanged(final ListSelectionEvent e)
    {
        int index = this.attributesTable.getSelectedRow();
        XsdTreeNode node = ((AttributesTableModel) this.attributesTable.getModel()).getNode();
        if (index >= 0)
        {
            this.attribute = node.getAttributeNameByIndex(index);
            this.editor.setCoupledNode(node.getCoupledKeyrefNodeAttribute(index), node, this.attribute);
        }
        if (this.listening != null)
        {
            this.listening.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
        }
        if (node != null)
        {
            node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
        }
        this.listening = node;
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
    {
        if (this.attribute.equals(((Object[]) event.getContent())[1]))
        {
            XsdTreeNode coupled =
                    this.listening.getCoupledKeyrefNodeAttribute(this.listening.getAttributeIndexByName(this.attribute));
            this.editor.setCoupledNode(coupled, this.listening, this.attribute);
        }
    }

}
