package org.opentrafficsim.editor.listeners;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.JComponent;

import org.opentrafficsim.editor.Actions;
import org.opentrafficsim.editor.OtsEditor;

import de.javagl.treetable.JTreeTable;

/**
 * Listener for key events on the tree table. This listener:
 * <ul>
 * <li>Executes keyboard shortcuts on tree table.</li>
 * <li>Starts undo action (i.e. ending previous one) when navigating through the tree table with keys.</li>
 * </ul>
 * The key actions in this class are also defined in {@code XsdTreeListener}, where they are defined as responses to key presses
 * when the right-click menu on a node is shown.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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

        Actions.bind(treeTable, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this.editor.actions().showTreeNodeDescription());
        Actions.bind(treeTable, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this.editor.actions().showTreeNodeInvalid());
        Actions.bind(treeTable, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this.editor.actions().addNode());
        Actions.bind(treeTable, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this.editor.actions().duplicateNode());
        Actions.bind(treeTable, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this.editor.actions().deleteNode());
        Actions.bind(treeTable, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this.editor.actions().copyNode());
        Actions.bind(treeTable, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this.editor.actions().cutNode());
        Actions.bind(treeTable, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this.editor.actions().insertNode());
        Actions.bind(treeTable, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this.editor.actions().pasteNode());
        Actions.bind(treeTable, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this.editor.actions().revolveNode());
        Actions.bind(treeTable, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this.editor.actions().expandOrCollapseNode());
        Actions.bind(treeTable, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this.editor.actions().moveNodeUp());
        Actions.bind(treeTable, JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT, this.editor.actions().moveNodeDown());
    }

    @Override
    public void keyPressed(final KeyEvent e)
    {
        if (e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_LEFT
                || e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_ENTER
                || e.getKeyCode() == KeyEvent.VK_TAB)
        {
            this.editor.startUndoActionOnTreeTable();
        }
    }

}
