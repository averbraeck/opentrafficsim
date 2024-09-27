package org.opentrafficsim.editor.listeners;

import java.rmi.RemoteException;

import javax.swing.CellEditor;
import javax.swing.JTable;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.opentrafficsim.editor.AttributesTableModel;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;

import de.javagl.treetable.JTreeTable;

/**
 * Listener for selection events on the tree table.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class XsdTreeSelectionListener implements TreeSelectionListener, EventListener
{

    /** */
    private static final long serialVersionUID = 20230923L;

    /** Editor. */
    private final OtsEditor editor;

    /** Tree table. */
    private final JTreeTable treeTable;

    /** Attributes table. */
    private final JTable attributesTable;

    /** Node that we are listening to. */
    private XsdTreeNode listening;

    /**
     * Constructor.
     * @param editor editor.
     * @param treeTable tree table.
     * @param attributesTable attributes table.
     */
    public XsdTreeSelectionListener(final OtsEditor editor, final JTreeTable treeTable, final JTable attributesTable)
    {
        this.editor = editor;
        this.treeTable = treeTable;
        this.attributesTable = attributesTable;
    }

    /** {@inheritDoc} */
    @Override
    public void valueChanged(final TreeSelectionEvent e)
    {
        TreePath[] paths = e.getPaths();
        if (paths.length > 0)
        {
            XsdTreeNode node = (XsdTreeNode) paths[0].getLastPathComponent();
            if (this.listening != null)
            {
                this.listening.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
            }
            if (node.isIdentifiable())
            {
                this.editor.setCoupledNode(node.getCoupledKeyrefNodeAttribute("Id"), node, null);
                this.listening = node;
                this.listening.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
            }
            else if (node.isEditable())
            {
                this.editor.setCoupledNode(node.getCoupledKeyrefNodeValue(), node, null);
                this.listening = node;
                this.listening.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
            }
            else
            {
                this.editor.setCoupledNode(null, null, null);
                this.listening = null;
            }
            this.editor.removeStatusLabel();
            String status = null;
            if (!node.isSelfValid())
            {
                status = node.reportInvalidNode();
                if (status == null)
                {
                    status = node.reportInvalidValue();
                }
                if (status == null)
                {
                    status = node.reportInvalidId();
                }
            }
            if (status == null)
            {
                status = node.getDescription();
            }
            if (status != null)
            {
                this.editor.setStatusLabel(status);
            }
            CellEditor editor = this.attributesTable.getCellEditor();
            if (editor != null)
            {
                editor.stopCellEditing();
            }
            editor = this.treeTable.getCellEditor();
            if (editor != null)
            {
                editor.stopCellEditing();
            }
            this.attributesTable.setModel(new AttributesTableModel(node, this.treeTable));
            try
            {
                this.editor.fireEvent(OtsEditor.SELECTION_CHANGED, node);
            }
            catch (RemoteException exception)
            {
                exception.printStackTrace();
            }
        }
        else
        {
            this.attributesTable.setModel(new AttributesTableModel(null, this.treeTable));
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
    {
        if ("Id".equals(((Object[]) event.getContent())[1]))
        {
            XsdTreeNode coupled = this.listening.getCoupledKeyrefNodeAttribute(this.listening.getAttributeIndexByName("Id"));
            this.editor.setCoupledNode(coupled, this.listening, null);
        }
    }

}
