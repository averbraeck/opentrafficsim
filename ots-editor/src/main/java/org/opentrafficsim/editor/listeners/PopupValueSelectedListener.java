package org.opentrafficsim.editor.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

import javax.swing.CellEditor;
import javax.swing.JTable;

import de.javagl.treetable.JTreeTable;

/**
 * Listener to when a value is selected in a popup showing a list of values.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class PopupValueSelectedListener implements ActionListener
{

    /** Option value. */
    private final String option;

    /** The table where the value is selected (tree or attributes). */
    private final JTable table;

    /** The resulting action upon option selection. */
    private final Consumer<String> action;

    /** Tree table which needs to be visually updated when the option is selected. */
    private final JTreeTable treeTable;

    /**
     * Constructor.
     * @param option option value
     * @param table the table where the value is selected (tree or attributes)
     * @param action the resulting action upon option selection
     * @param treeTable tree table which needs to be visually updated when the option is selected
     */
    public PopupValueSelectedListener(final String option, final JTable table, final Consumer<String> action,
            final JTreeTable treeTable)
    {
        this.option = option;
        this.table = table;
        this.action = action;
        this.treeTable = treeTable;
    }

    @Override
    public void actionPerformed(final ActionEvent e)
    {
        this.action.accept(this.option);
        CellEditor cellEditor = this.table.getCellEditor();
        if (cellEditor != null)
        {
            cellEditor.cancelCellEditing();
        }
        this.treeTable.updateUI();
    }
}
