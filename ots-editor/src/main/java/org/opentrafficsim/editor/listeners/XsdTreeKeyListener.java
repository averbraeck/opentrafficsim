package org.opentrafficsim.editor.listeners;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.CellEditor;
import javax.swing.JTable;
import javax.swing.tree.TreePath;

import org.opentrafficsim.editor.AttributesTableModel;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.Undo.ActionType;
import org.opentrafficsim.editor.XsdOption;
import org.opentrafficsim.editor.XsdTreeNode;

import de.javagl.treetable.JTreeTable;

/**
 * Listener for key events on the tree table.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class XsdTreeKeyListener extends KeyAdapter
{
    
    /** Editor. */
    private final OtsEditor editor;
    
    /** Tree table. */
    private final JTreeTable treeTable;
    
    /** Attributes table. */
    private final JTable attributesTable;

    /**
     * Constructor.
     * @param editor OtsEditor; editor.
     * @param treeTable JTreeTable; tree table.
     * @param attributesTable JTable; attributes table.
     */
    public XsdTreeKeyListener(final OtsEditor editor, final JTreeTable treeTable, final JTable attributesTable)
    {
        this.editor = editor;
        this.treeTable = treeTable;
        this.attributesTable = attributesTable;
    }

    /** {@inheritDoc} */
    @Override
    public void keyReleased(final KeyEvent e)
    {
        if (this.treeTable.isEditing())
        {
            // prevents row i being removed, being replaced by i+1, and editing then setting the value of i+1 now at i
            return;
        }
        else if (e.getKeyCode() == KeyEvent.VK_W && e.isControlDown())
        {
            XsdTreeNode node =
                    (XsdTreeNode) this.treeTable.getTree().getSelectionPath().getLastPathComponent();
            if (node.isAddable())
            {
                this.editor.getUndo().startAction(ActionType.ADD, node, null);
                XsdTreeNode added = node.add();
                this.editor.show(added, null);
            }
        }
        else if (e.getKeyCode() == KeyEvent.VK_D && e.isControlDown())
        {
            XsdTreeNode node =
                    (XsdTreeNode) this.treeTable.getTree().getSelectionPath().getLastPathComponent();
            if (node.isAddable())
            {
                this.editor.getUndo().startAction(ActionType.DUPLICATE, node, null);
                XsdTreeNode added = node.duplicate();
                this.editor.show(added, null);
            }
        }
        else if (e.getKeyCode() == KeyEvent.VK_DELETE)
        {
            XsdTreeNode node =
                    (XsdTreeNode) this.treeTable.getTree().getSelectionPath().getLastPathComponent();
            if (node.isRemovable())
            {
                if (this.editor.confirmNodeRemoval(node))
                {
                    CellEditor editor = this.treeTable.getCellEditor();
                    if (editor != null)
                    {
                        editor.stopCellEditing();
                    }
                    int selected = this.treeTable.getSelectedRow();
                    this.editor.getUndo().startAction(ActionType.REMOVE, node, null);
                    node.remove();
                    this.treeTable.updateUI();
                    this.treeTable.getSelectionModel().setSelectionInterval(selected, selected);
                    TreePath path = this.treeTable.getTree().getSelectionPath();
                    if (path != null) // can be null if last node was removed causing no effective selection
                    {
                        this.attributesTable.setModel(new AttributesTableModel(
                                (XsdTreeNode) path.getLastPathComponent(), this.treeTable));
                    }
                }
            }
        }
        else if (e.getKeyCode() == KeyEvent.VK_R && e.isControlDown())
        {
            XsdTreeNode node =
                    (XsdTreeNode) this.treeTable.getTree().getSelectionPath().getLastPathComponent();
            List<XsdOption> options = node.getOptions();
            if (node.isChoice() && options.size() > 1)
            {
                int optionIndex = 0;
                for (int i = 0; i < options.size(); i++)
                {
                    if (options.get(i).getOptionNode().equals(node))
                    {
                        optionIndex = i + 1;
                        break;
                    }
                }
                if (optionIndex >= options.size())
                {
                    optionIndex = 0;
                }
                this.editor.getUndo().startAction(ActionType.OPTION, node, null);
                XsdTreeNode next = options.get(optionIndex).getOptionNode();
                node.setOption(next);
                this.editor.show(next, null);
            }
        }
        else if (e.getKeyCode() == KeyEvent.VK_E && e.isControlDown())
        {
            XsdTreeNode node =
                    (XsdTreeNode) this.treeTable.getTree().getSelectionPath().getLastPathComponent();
            if (!node.isActive())
            {
                this.editor.getUndo().startAction(ActionType.ACTIVATE, node, null);
                node.setActive();
            }
            this.treeTable.getTree().expandPath(this.treeTable.getTree().getSelectionPath());
            this.editor.show(node, null);
        }
        else if (e.getKeyCode() == KeyEvent.VK_UP && e.isControlDown())
        {
            XsdTreeNode node =
                    (XsdTreeNode) this.treeTable.getTree().getSelectionPath().getLastPathComponent();
            if (node.canMoveUp())
            {
                this.editor.getUndo().startAction(ActionType.MOVE, node, null);
                node.move(-1);
            }
            this.editor.show(node, null);
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN && e.isControlDown())
        {
            XsdTreeNode node =
                    (XsdTreeNode) this.treeTable.getTree().getSelectionPath().getLastPathComponent();
            if (node.canMoveDown())
            {
                this.editor.getUndo().startAction(ActionType.MOVE, node, null);
                node.move(1);
            }
            this.editor.show(node, null);
        }
        else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN
                || e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT
                || e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_TAB)
        {
            this.editor.startUndoActionOnTreeTable();
        }
    }
    
}
