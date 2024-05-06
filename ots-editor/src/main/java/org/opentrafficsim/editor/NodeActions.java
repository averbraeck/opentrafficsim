package org.opentrafficsim.editor;

import java.util.List;

import javax.swing.CellEditor;
import javax.swing.tree.TreePath;

import org.opentrafficsim.editor.Undo.ActionType;

import de.javagl.treetable.JTreeTable;

/**
 * This class houses actions that can be performed on tree nodes. The actions will always be executed. Any check on whether the
 * state of a node is appropriate for an action, is up to the caller. Callers are typically mouse event or key listeners on the
 * tree.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class NodeActions
{

    /** Editor. */
    private final OtsEditor editor;

    /** Tree table. */
    private final JTreeTable treeTable;

    /**
     * Constructor.
     * @param editor OtsEditor; editor.
     * @param treeTable JTreeTable; tree table.
     */
    public NodeActions(final OtsEditor editor, final JTreeTable treeTable)
    {
        this.editor = editor;
        this.treeTable = treeTable;
    }

    /**
     * Add node.
     * @param node XsdTreeNode; node.
     */
    public void add(final XsdTreeNode node)
    {
        this.editor.getUndo().startAction(ActionType.ADD, node, null);
        XsdTreeNode added = node.add();
        this.editor.getUndo().setPostActionShowNode(added);
        this.editor.show(added, null);
    }

    /**
     * Duplicate node.
     * @param node XsdTreeNode; node.
     */
    public void duplicate(final XsdTreeNode node)
    {
        this.editor.getUndo().startAction(ActionType.DUPLICATE, node, null);
        XsdTreeNode added = node.duplicate();
        this.editor.getUndo().setPostActionShowNode(added);
        this.editor.show(added, null);
    }

    /**
     * Remove node.
     * @param node XsdTreeNode; node.
     */
    public void remove(final XsdTreeNode node)
    {
        CellEditor editor = this.treeTable.getCellEditor();
        if (editor != null)
        {
            editor.stopCellEditing();
        }
        this.editor.getUndo().startAction(ActionType.REMOVE, node, null);
        XsdTreeNode parent = node.getParent();
        int index = parent.getChildren().indexOf(node);
        node.remove();
        index = Math.min(index, parent.getChildren().size() - 1);
        XsdTreeNode replaced = parent.getChild(index);
        this.editor.getUndo().setPostActionShowNode(replaced);
        this.editor.show(replaced, null);
    }

    /**
     * Copy node.
     * @param node XsdTreeNode; node.
     */
    public void copy(final XsdTreeNode node)
    {
        this.editor.setClipboard(node, false);
    }

    /**
     * Cut node.
     * @param node XsdTreeNode; node.
     */
    public void cut(final XsdTreeNode node)
    {
        this.editor.setClipboard(node, true);
        this.editor.getUndo().startAction(ActionType.CUT, node, null);
        node.setInactive();
        this.editor.show(node, null);
    }

    /**
     * Insert node.
     * @param node XsdTreeNode; node.
     */
    public void insert(final XsdTreeNode node)
    {
        this.editor.getUndo().startAction(ActionType.INSERT, node, null);
        XsdTreeNode newNode = node.emptyCopy();
        newNode.getRoot().fireEvent(XsdTreeNodeRoot.NODE_CREATED,
                new Object[] {newNode, newNode.getParent(), newNode.getParent().getChildren().indexOf(newNode)});
        newNode.move(-1);
        this.editor.getClipboard().copyInto(newNode);
        this.editor.removeClipboardWhenCut();
        this.editor.getUndo().setPostActionShowNode(newNode);
        this.editor.show(newNode, null);
    }

    /**
     * Paste node.
     * @param node XsdTreeNode; node.
     */
    public void paste(final XsdTreeNode node)
    {
        this.editor.getUndo().startAction(ActionType.PASTE, node, null);
        XsdTreeNode newNode;
        if (!node.isActive())
        {
            node.setActive();
            newNode = node;
        }
        else
        {
            newNode = node.emptyCopy();
            newNode.getRoot().fireEvent(XsdTreeNodeRoot.NODE_CREATED,
                    new Object[] {newNode, newNode.getParent(), newNode.getParent().getChildren().indexOf(newNode)});
        }
        this.editor.getClipboard().copyInto(newNode);
        if (!node.equals(newNode))
        {
            this.editor.removeClipboardWhenCut();
        }
        else
        {
            this.editor.setClipboard(null, false);
        }
        this.editor.getUndo().setPostActionShowNode(newNode);
        this.editor.show(newNode, null);
    }

    /**
     * Revolve to the next option of the node.
     * @param node XsdTreeNode; node.
     * @param options List&lt;XsdOption&gt;; options of the node. These are obtainable from the node, but already gathered by
     *            the caller of this method and therefore forwarded for efficieny.
     */
    public void revolveOption(final XsdTreeNode node, final List<XsdOption> options)
    {
        int optionIndex = 0;
        for (int i = 0; i < options.size(); i++)
        {
            if (options.get(i).optionNode().equals(node))
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
        XsdTreeNode next = options.get(optionIndex).optionNode();
        node.setOption(next);
        this.editor.show(next, null);
    }

    /**
     * Expand, or collapse, node.
     * @param node XsdTreeNode; node.
     * @param path TreePath; path in the tree of the node.
     * @param expanded boolean; whether the node is currently expanded.
     */
    public void expand(final XsdTreeNode node, final TreePath path, final boolean expanded)
    {
        if (expanded)
        {
            this.treeTable.getTree().collapsePath(path);
        }
        else
        {
            if (!node.isActive())
            {
                this.editor.getUndo().startAction(ActionType.ACTIVATE, node, null);
                node.setActive();
            }
            this.treeTable.getTree().expandPath(path);
        }
        this.editor.show(node, null);
    }

    /**
     * Move node.
     * @param node XsdTreeNode; node.
     * @param down int; number of rows to move the node down.
     */
    public void move(final XsdTreeNode node, final int down)
    {
        this.editor.getUndo().startAction(ActionType.MOVE, node, null);
        node.move(down);
        this.editor.show(node, null);
    }

}
