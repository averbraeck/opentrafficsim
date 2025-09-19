package org.opentrafficsim.editor.listeners;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.List;

import javax.swing.tree.TreePath;

import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdOption;
import org.opentrafficsim.editor.XsdTreeNode;

import de.javagl.treetable.JTreeTable;

/**
 * Listener for key events on the tree table. This involves keyboard shortcuts and starting (i.e. ending previous) undo actions.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class XsdTreeKeyListener extends KeyAdapter
{

    /** Editor. */
    private final OtsEditor editor;

    /** Tree table. */
    private final JTreeTable treeTable;

    /**
     * Constructor.
     * @param editor editor.
     * @param treeTable tree table.
     */
    public XsdTreeKeyListener(final OtsEditor editor, final JTreeTable treeTable)
    {
        this.editor = editor;
        this.treeTable = treeTable;
        this.treeTable.addKeyListener(this);
    }

    @Override
    public void keyPressed(final KeyEvent e)
    {
        if (this.treeTable.isEditing())
        {
            // prevents row i being removed, being replaced by i+1, and editing then setting the value of i+1 now at i
            return;
        }
        else if (e.getKeyCode() == KeyEvent.VK_W && e.isControlDown())
        {
            XsdTreeNode node = (XsdTreeNode) this.treeTable.getTree().getSelectionPath().getLastPathComponent();
            if (node.isAddable())
            {
                this.editor.getNodeActions().add(node);
            }
            e.consume();
        }
        else if (e.getKeyCode() == KeyEvent.VK_D && e.isControlDown())
        {
            XsdTreeNode node = (XsdTreeNode) this.treeTable.getTree().getSelectionPath().getLastPathComponent();
            if (node.isAddable())
            {
                this.editor.getNodeActions().duplicate(node);
            }
            e.consume();
        }
        else if (e.getKeyCode() == KeyEvent.VK_DELETE)
        {
            XsdTreeNode node = (XsdTreeNode) this.treeTable.getTree().getSelectionPath().getLastPathComponent();
            if (node.isRemovable())
            {
                if (this.editor.confirmNodeRemoval(node))
                {
                    this.editor.getNodeActions().remove(node);
                }
            }
            e.consume();
        }
        else if (e.getKeyCode() == KeyEvent.VK_C && e.isControlDown())
        {
            XsdTreeNode node = (XsdTreeNode) this.treeTable.getTree().getSelectionPath().getLastPathComponent();
            if (node.isActive() && (node.isRemovable() || node.isAddable()))
            {
                this.editor.getNodeActions().copy(node);
            }
            e.consume();
        }
        else if (e.getKeyCode() == KeyEvent.VK_X && e.isControlDown())
        {
            XsdTreeNode node = (XsdTreeNode) this.treeTable.getTree().getSelectionPath().getLastPathComponent();
            if (node.isActive() && node.isRemovable())
            {
                this.editor.getNodeActions().cut(node);
            }
            e.consume();
        }
        else if (e.getKeyCode() == KeyEvent.VK_INSERT)
        {
            XsdTreeNode node = (XsdTreeNode) this.treeTable.getTree().getSelectionPath().getLastPathComponent();
            if (this.editor.getClipboard() != null && node.canContain(this.editor.getClipboard()) && node.isActive()
                    && node.isAddable())
            {
                this.editor.getNodeActions().insert(node);
            }
            e.consume();
        }
        else if (e.getKeyCode() == KeyEvent.VK_V && e.isControlDown())
        {
            XsdTreeNode node = (XsdTreeNode) this.treeTable.getTree().getSelectionPath().getLastPathComponent();
            if (this.editor.getClipboard() != null && node.canContain(this.editor.getClipboard())
                    && (!node.isActive() || node.isAddable()))
            {
                this.editor.getNodeActions().paste(node);
            }
            e.consume();
        }
        else if (e.getKeyCode() == KeyEvent.VK_R && e.isControlDown())
        {
            XsdTreeNode node = (XsdTreeNode) this.treeTable.getTree().getSelectionPath().getLastPathComponent();
            List<XsdOption> options = node.getOptions();
            if (node.isChoice() && options.size() > 1)
            {
                this.editor.getNodeActions().revolveOption(node, options);
            }
            e.consume();
        }
        else if (e.getKeyCode() == KeyEvent.VK_E && e.isControlDown())
        {
            TreePath path = this.treeTable.getTree().getSelectionPath();
            XsdTreeNode node = (XsdTreeNode) path.getLastPathComponent();
            boolean expanded = this.treeTable.getTree().isExpanded(path);
            this.editor.getNodeActions().expand(node, path, expanded);
            e.consume();
        }
        else if (e.getKeyCode() == KeyEvent.VK_UP && e.isControlDown())
        {
            XsdTreeNode node = (XsdTreeNode) this.treeTable.getTree().getSelectionPath().getLastPathComponent();
            if (node.canMoveUp())
            {
                this.editor.getNodeActions().move(node, -1);
            }
            e.consume();
        }
        else if (e.getKeyCode() == KeyEvent.VK_DOWN && e.isControlDown())
        {
            XsdTreeNode node = (XsdTreeNode) this.treeTable.getTree().getSelectionPath().getLastPathComponent();
            if (node.canMoveDown())
            {
                this.editor.getNodeActions().move(node, 1);
            }
            e.consume();
        }
        else if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_LEFT
                || e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_ENTER
                || e.getKeyCode() == KeyEvent.VK_TAB)
        {
            this.editor.startUndoActionOnTreeTable();
        }
    }

}
