package org.opentrafficsim.editor.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JTable;

import org.opentrafficsim.editor.AttributesTableModel;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.Undo.ActionType;
import org.opentrafficsim.editor.XsdTreeNode;

import de.javagl.treetable.JTreeTable;

/**
 * Listener for selecting choice options.
 * <p>
 * Copyright (c) 2023-2023 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://dittlab.tudelft.nl">Wouter Schakel</a>
 */
public class ChoiceListener implements ActionListener
{
    /** Choice node of the clicked option. */
    private XsdTreeNode choiceNode;

    /** Option. */
    private XsdTreeNode option;

    /** Row to reset the selection at. */
    private int reselectionRow;

    /** Editor. */
    private final OtsEditor editor;
    
    /** Tree table. */
    private final JTreeTable treeTable;

    /** Attributes table. */
    private final JTable attributesTable;

    /**
     * Constructor.
     * @param choiceNode XsdTreeNode; choice node of the choice.
     * @param option XsdTreeNode; possibly selected option.
     * @param reselectionRow int; row to reset selection.
     * @param editor OtsEditor; editor.
     * @param treeTable JTreeTable; tree table.
     * @param attributesTable JTable; attributes table.
     */
    public ChoiceListener(final XsdTreeNode choiceNode, final XsdTreeNode option, final int reselectionRow,
            final OtsEditor editor, final JTreeTable treeTable, final JTable attributesTable)
    {
        this.choiceNode = choiceNode;
        this.option = option;
        this.reselectionRow = reselectionRow;
        this.editor = editor;
        this.treeTable = treeTable;
        this.attributesTable = attributesTable;
    }

    /** {@inheritDoc} */
    @Override
    public void actionPerformed(final ActionEvent e)
    {
        this.editor.getUndo().startAction(ActionType.OPTION, this.option.getOption(), null);
        this.choiceNode.setOption(this.option);
        this.treeTable.setRowSelectionInterval(this.reselectionRow, this.reselectionRow);
        this.treeTable.updateUI();
        this.attributesTable.setModel(new AttributesTableModel(this.option, this.treeTable));
    }
}
