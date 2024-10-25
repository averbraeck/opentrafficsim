package org.opentrafficsim.editor.listeners;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.TreePath;

import org.opentrafficsim.editor.AttributesTableModel;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.Undo.ActionType;
import org.opentrafficsim.editor.XsdOption;
import org.opentrafficsim.editor.XsdTreeNode;

import de.javagl.treetable.JTreeTable;

/**
 * Listener for mouse events on the tree.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class XsdTreeMouseListener extends MouseAdapter
{

    /** Editor. */
    private final OtsEditor editor;

    /** Tree table. */
    private final JTreeTable treeTable;

    /** Attributes table. */
    private final JTable attributesTable;

    /**
     * Constructor.
     * @param editor editor.
     * @param treeTable tree table.
     * @param attributesTable attributes table.
     */
    public XsdTreeMouseListener(final OtsEditor editor, final JTreeTable treeTable, final JTable attributesTable)
    {
        this.editor = editor;
        this.treeTable = treeTable;
        this.attributesTable = attributesTable;
    }

    @Override
    public void mousePressed(final MouseEvent e)
    {
        if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1)
        {
            TableCellEditor editor = this.attributesTable.getCellEditor();
            if (editor != null)
            {
                // This prevents a null pointer when editing in the attributes and clicking on the table:
                // Exception in thread "AWT-EventQueue-0" java.lang.NullPointerException: Cannot invoke
                // "javax.swing.JTree.getToggleClickCount()" because "this.tree" is null at
                // java.desktop/javax.swing.plaf.basic.BasicTreeUI.isToggleEvent(BasicTreeUI.java:2697)
                editor.stopCellEditing();
            }

            // show value options popup
            int row = this.treeTable.rowAtPoint(e.getPoint());
            int treeCol = this.treeTable.convertColumnIndexToView(0); // columns may have been moved in view
            XsdTreeNode treeNode = (XsdTreeNode) this.treeTable.getValueAt(row, treeCol);
            int col = this.treeTable.columnAtPoint(e.getPoint());
            if (this.treeTable.isCellEditable(row, col))
            {
                int colNumber = this.treeTable.convertColumnIndexToModel(col);
                if (colNumber == 1 && treeNode.isIdentifiable())
                {
                    List<String> allOptions = treeNode.getIdRestrictions();
                    this.editor.optionsPopup(allOptions, this.treeTable, (t) ->
                    {
                        this.editor.getUndo().startAction(ActionType.ID_CHANGE, treeNode, "Id");
                        treeNode.setId(t);
                    });
                }
                else if (colNumber == 2)
                {
                    List<String> allOptions = treeNode.getValueRestrictions();
                    this.editor.optionsPopup(allOptions, this.treeTable, (t) ->
                    {
                        this.editor.getUndo().startAction(ActionType.VALUE_CHANGE, treeNode, null);
                        treeNode.setValue(t);
                    });
                }
            }
        }
    }

    @Override
    public void mouseClicked(final MouseEvent e)
    {
        // show choice popup
        if (e.getButton() == MouseEvent.BUTTON1)
        {
            if (e.getClickCount() > 1
                    && this.treeTable.convertColumnIndexToModel(this.treeTable.columnAtPoint(e.getPoint())) == 0)
            {
                int row = this.treeTable.rowAtPoint(e.getPoint());
                int col = this.treeTable.convertColumnIndexToView(0); // columns may have been moved in view
                XsdTreeNode treeNode = (XsdTreeNode) this.treeTable.getValueAt(row, col);
                if (!treeNode.isActive() && !treeNode.isInclude())
                {
                    this.editor.getUndo().startAction(ActionType.ACTIVATE, treeNode, null);
                    treeNode.setActive();
                    this.treeTable.updateUI();
                    this.attributesTable.setModel(new AttributesTableModel(treeNode, this.treeTable));
                }
                return;
            }
            int row = this.treeTable.rowAtPoint(e.getPoint());
            int treeCol = this.treeTable.convertColumnIndexToView(0); // columns may have been moved in view
            XsdTreeNode treeNode = (XsdTreeNode) this.treeTable.getValueAt(row, treeCol);
            if (!treeNode.isActive() || treeNode.isInclude())
            {
                return;
            }
            Rectangle labelPortion = this.treeTable.getTree()
                    .getPathBounds(this.treeTable.getTree().getPathForLocation(e.getPoint().x, e.getPoint().y));
            if (labelPortion != null && labelPortion.contains(e.getPoint()) && this.editor.mayPresentChoice()
                    && treeNode.isChoice())
            {
                JPopupMenu popup = new JPopupMenu();
                boolean firstEntry = true;
                for (XsdOption option : treeNode.getOptions())
                {
                    if (option.firstInGroup())
                    {
                        if (!firstEntry)
                        {
                            popup.add(new JSeparator());
                        }
                    }
                    firstEntry = false;
                    JMenuItem button = new JMenuItem(option.optionNode().getShortString());
                    if (!option.selected())
                    {
                        button.addActionListener(new ChoiceListener(option.choice(), option.optionNode(), row, this.editor,
                                this.treeTable, this.attributesTable));
                    }
                    button.setFont(this.treeTable.getFont());
                    popup.add(button);
                    firstEntry = false;
                }
                this.editor.preparePopupRemoval(popup, this.treeTable);
                this.treeTable.setComponentPopupMenu(popup);
                this.editor.setChoiceNode(treeNode);
                popup.show(this.treeTable, (int) labelPortion.getMinX(), (int) labelPortion.getMaxY() - 1);
            }
        }
        // show actions popup
        else if (e.getButton() == MouseEvent.BUTTON3)
        {
            int row = this.treeTable.rowAtPoint(e.getPoint());
            int col = this.treeTable.columnAtPoint(e.getPoint());
            int treeCol = this.treeTable.convertColumnIndexToView(0); // columns may have been moved in view
            XsdTreeNode treeNode = (XsdTreeNode) this.treeTable.getValueAt(row, treeCol);
            if (col == treeCol)
            {
                this.treeTable.setRowSelectionInterval(row, row);
                createRightClickPopup(e, treeNode);
            }
        }
    }

    /**
     * Creates a popup panel with options for a node. These contain active consumers (editors), moving up/down, and addition and
     * removal.
     * @param e mouse event.
     * @param treeNode node that was clicked on.
     */
    private void createRightClickPopup(final MouseEvent e, final XsdTreeNode treeNode)
    {
        JPopupMenu popup = new JPopupMenu();
        boolean anyAdded = false;

        if (treeNode.isActive())
        {
            for (String menuItem : treeNode.getConsumerMenuItems())
            {
                JMenuItem item = new JMenuItem(menuItem);
                item.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(final ActionEvent e)
                    {
                        treeNode.consume(menuItem);
                    }
                });
                item.setFont(this.treeTable.getFont());
                popup.add(item);
                anyAdded = true;
            }
        }
        if (treeNode.getDescription() != null) // description is the only thing we show with the node disabled
        {
            JMenuItem item = new JMenuItem("Description...");
            item.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    XsdTreeMouseListener.this.editor.showDescription(treeNode.getDescription());
                }
            });
            item.setFont(this.treeTable.getFont());
            popup.add(item);
            anyAdded = true;
        }

        if (!treeNode.isInclude())
        {
            anyAdded = addDefaultActions(treeNode, popup, anyAdded);
        }

        if (anyAdded)
        {
            this.editor.preparePopupRemoval(popup, this.treeTable);
            popup.setFont(this.treeTable.getFont());
            this.treeTable.setComponentPopupMenu(popup);
            popup.show(this.treeTable, e.getX(), e.getY());
        }
    }

    /**
     * Adds default menu actions Add, Remove, Move up, and Move down, for each when appropriate.
     * @param treeNode node.
     * @param popup popup menu.
     * @param added whether any menu items were added before (which requires a separator).
     * @return whether any items were added, going in to this method, or during the method.
     */
    private boolean addDefaultActions(final XsdTreeNode treeNode, final JPopupMenu popup, final boolean added)
    {
        boolean anyAdded = added;
        boolean separatorNeeded = anyAdded;
        boolean groupAdded = false;

        /*
         * The accelerators are added so they are shown in the menu as shortcuts. However, they will only trigger on key events
         * when the right-click menu is actually shown. Therefore XsdTreeKeyListener also implements the responses for when keys
         * are pressed. The below code executes when a menu item is clicked, or the accelerator is used while the menu shows.
         */
        if (treeNode.isActive() && treeNode.isAddable())
        {
            if (separatorNeeded)
            {
                separatorNeeded = false;
                popup.add(new JSeparator());
            }
            JMenuItem add = new JMenuItem("Add");
            add.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    XsdTreeMouseListener.this.editor.getNodeActions().add(treeNode);
                }
            });
            add.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK));
            add.setFont(this.treeTable.getFont());
            popup.add(add);
            JMenuItem duplicate = new JMenuItem("Duplicate");
            duplicate.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    XsdTreeMouseListener.this.editor.getNodeActions().duplicate(treeNode);
                }
            });
            duplicate.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK));
            duplicate.setFont(this.treeTable.getFont());
            popup.add(duplicate);
            anyAdded = true;
            groupAdded = true;
        }
        if (treeNode.isActive() && treeNode.isRemovable())
        {
            if (separatorNeeded)
            {
                separatorNeeded = false;
                popup.add(new JSeparator());
            }
            JMenuItem remove = new JMenuItem("Remove");
            remove.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    if (XsdTreeMouseListener.this.editor.confirmNodeRemoval(treeNode))
                    {
                        XsdTreeMouseListener.this.editor.getNodeActions().remove(treeNode);
                    }
                }
            });
            remove.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
            remove.setFont(this.treeTable.getFont());
            popup.add(remove);
            anyAdded = true;
            groupAdded = true;
        }
        if (treeNode.isActive() && (treeNode.isRemovable() || treeNode.isAddable()))
        {
            if (separatorNeeded)
            {
                separatorNeeded = false;
                popup.add(new JSeparator());
            }
            JMenuItem copy = new JMenuItem("Copy");
            copy.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    XsdTreeMouseListener.this.editor.getNodeActions().copy(treeNode);
                }
            });
            copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, KeyEvent.CTRL_DOWN_MASK));
            copy.setFont(this.treeTable.getFont());
            popup.add(copy);
            anyAdded = true;
            groupAdded = true;
        }
        if (treeNode.isActive() && treeNode.isRemovable())
        {
            if (separatorNeeded)
            {
                separatorNeeded = false;
                popup.add(new JSeparator());
            }
            JMenuItem cut = new JMenuItem("Cut");
            cut.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    XsdTreeMouseListener.this.editor.getNodeActions().cut(treeNode);
                }
            });
            cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, KeyEvent.CTRL_DOWN_MASK));
            cut.setFont(this.treeTable.getFont());
            popup.add(cut);
            anyAdded = true;
            groupAdded = true;
        }

        if (this.editor.getClipboard() != null && treeNode.canContain(this.editor.getClipboard()))
        {
            if (treeNode.isActive() && treeNode.isAddable())
            {
                if (separatorNeeded)
                {
                    separatorNeeded = false;
                    popup.add(new JSeparator());
                }
                JMenuItem cut = new JMenuItem("Insert");
                cut.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(final ActionEvent e)
                    {
                        XsdTreeMouseListener.this.editor.getNodeActions().insert(treeNode);
                    }
                });
                cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_INSERT, 0));
                cut.setFont(this.treeTable.getFont());
                popup.add(cut);
                anyAdded = true;
                groupAdded = true;
            }
            if (!treeNode.isActive() || treeNode.isAddable())
            {
                if (separatorNeeded)
                {
                    separatorNeeded = false;
                    popup.add(new JSeparator());
                }
                JMenuItem cut = new JMenuItem("Paste");
                cut.addActionListener(new ActionListener()
                {
                    @Override
                    public void actionPerformed(final ActionEvent e)
                    {
                        XsdTreeMouseListener.this.editor.getNodeActions().paste(treeNode);
                    }
                });
                cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK));
                cut.setFont(this.treeTable.getFont());
                popup.add(cut);
                anyAdded = true;
                groupAdded = true;
            }
        }

        separatorNeeded = groupAdded;
        groupAdded = false;

        List<XsdOption> options = treeNode.getOptions();
        if (treeNode.isActive() && treeNode.isChoice() && options.size() > 1)
        {
            if (separatorNeeded)
            {
                separatorNeeded = false;
                popup.add(new JSeparator());
            }
            JMenuItem revolve = new JMenuItem("Revolve option");
            revolve.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    XsdTreeMouseListener.this.editor.getNodeActions().revolveOption(treeNode, options);
                }
            });
            revolve.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK));
            revolve.setFont(this.treeTable.getFont());
            popup.add(revolve);
            anyAdded = true;
            groupAdded = true;
        }
        if (!treeNode.isActive() || treeNode.getChildCount() > 0)
        {
            if (separatorNeeded)
            {
                separatorNeeded = false;
                popup.add(new JSeparator());
            }
            TreePath path = XsdTreeMouseListener.this.treeTable.getTree().getSelectionPath();
            boolean expanded = XsdTreeMouseListener.this.treeTable.getTree().isExpanded(path);
            JMenuItem expand = new JMenuItem(expanded ? "Collapse" : "Expand");
            expand.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    XsdTreeMouseListener.this.editor.getNodeActions().expand(treeNode, path, expanded);
                }
            });
            expand.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK));
            expand.setFont(this.treeTable.getFont());
            popup.add(expand);
            anyAdded = true;
            groupAdded = true;
        }

        separatorNeeded = groupAdded;

        if (treeNode.isActive() && treeNode.canMoveUp())
        {
            if (separatorNeeded)
            {
                separatorNeeded = false;
                popup.add(new JSeparator());
            }
            JMenuItem moveUp = new JMenuItem("Move up");
            moveUp.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    XsdTreeMouseListener.this.editor.getNodeActions().move(treeNode, -1);
                }
            });
            moveUp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK));
            moveUp.setFont(this.treeTable.getFont());
            popup.add(moveUp);
            anyAdded = true;
        }
        if (treeNode.isActive() && treeNode.canMoveDown())
        {
            if (separatorNeeded)
            {
                separatorNeeded = false;
                popup.add(new JSeparator());
            }
            JMenuItem moveDown = new JMenuItem("Move down");
            moveDown.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    XsdTreeMouseListener.this.editor.getNodeActions().move(treeNode, 1);
                }
            });
            moveDown.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, KeyEvent.CTRL_DOWN_MASK));
            moveDown.setFont(this.treeTable.getFont());
            popup.add(moveDown);
            anyAdded = true;
        }
        return anyAdded;
    }
}
