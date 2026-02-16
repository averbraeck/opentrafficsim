package org.opentrafficsim.editor.listeners;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.function.Consumer;

import javax.swing.CellEditor;
import javax.swing.Icon;
import javax.swing.JTable;

import org.opentrafficsim.animation.IconUtil;

import de.javagl.treetable.JTreeTable;

/**
 * Listener to when a value is selected in a popup showing a list of values. This is either in the tree table or the attributes
 * tables. The resulting action depends on the creator of the listener, e.g. setting an Id.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/averbraeck">Alexander Verbraeck</a>
 * @author <a href="https://github.com/peter-knoppers">Peter Knoppers</a>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class PopupValueSelectedListener implements ActionListener
{

    /** Icon for remove option. */
    public static final Icon REMOVE_ICON = IconUtil.of("Delete24.png").imageSize(14, 14).get();

    /** Icon for up scrolling. */
    public static final Icon UP_ICON = IconUtil.of("Up24.png").imageSize(14, 14).get();

    /** Icon for down scrolling. */
    public static final Icon DOWN_ICON = IconUtil.of("Expanded24.png").imageSize(14, 14).get();

    /** Menu item string for removal of value. */
    public static final String REMOVE_OPTION = "<html><i>remove</i></html>";

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
        if (REMOVE_OPTION.equals(this.option))
        {
            this.action.accept(null);
        }
        else
        {
            this.action.accept(this.option);
        }
        CellEditor cellEditor = this.table.getCellEditor();
        if (cellEditor != null)
        {
            cellEditor.cancelCellEditing();
        }
        this.treeTable.updateUI();
    }
}
