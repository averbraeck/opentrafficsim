package org.opentrafficsim.editor.listeners;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.rmi.RemoteException;
import java.util.List;

import javax.swing.CellEditor;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.table.TableCellEditor;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreePath;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.opentrafficsim.editor.AttributesTableModel;
import org.opentrafficsim.editor.DocumentReader;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.Undo.ActionType;
import org.opentrafficsim.editor.XsdOption;
import org.opentrafficsim.editor.XsdTreeNode;

import de.javagl.treetable.JTreeTable;

/**
 * Listener to the tree table or its underlying tree to organize: navigation, status label, updating attributes table, prevent
 * choice pop-up in case of node expansion/collapse, allow choice pop-up at mouse movement, set tree tooltip, show value options
 * pop-up, activation, show choice pop-up, show actions pop-up.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class XsdTreeListener extends MouseAdapter implements TreeSelectionListener, TreeWillExpandListener, EventListener
{

    /** */
    private static final long serialVersionUID = 20250917L;

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
     * @param editor editor
     * @param treeTable tree table
     * @param attributesTable attributes table
     */
    public XsdTreeListener(final OtsEditor editor, final JTreeTable treeTable, final JTable attributesTable)
    {
        this.editor = editor;
        this.treeTable = treeTable;
        this.attributesTable = attributesTable;
        this.treeTable.getTree().addTreeSelectionListener(this);
        this.treeTable.getTree().addTreeWillExpandListener(this);
        this.treeTable.addMouseMotionListener(this);
        this.treeTable.addMouseListener(this);
    }

    // treetable tree selection listener

    @Override
    public void valueChanged(final TreeSelectionEvent e)
    {
        if (e.getPaths().length > 0)
        {
            // listen to attribute change, and set potential coupled node for navigation
            XsdTreeNode node = (XsdTreeNode) (e.getPaths().length == 1 ? e.getPaths()[0] : e.getNewLeadSelectionPath())
                    .getLastPathComponent();
            if (this.listening != null)
            {
                this.listening.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
            }
            if (node.isIdentifiable())
            {
                this.editor.setCoupledNode(node.getCoupledNodeAttribute("Id"), node, null);
                this.listening = node;
                this.listening.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
            }
            else if (node.isEditable())
            {
                this.editor.setCoupledNode(node.getCoupledNodeValue(), node, null);
                this.listening = node;
                this.listening.addListener(this, XsdTreeNode.VALUE_CHANGED);
            }
            else
            {
                this.editor.setCoupledNode(null, null, null);
                this.listening = null;
            }

            // update status at bottom of screen pertaining to selected cell
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
                status = DocumentReader.filterHtml(node.getDescription());
            }
            if (status != null)
            {
                this.editor.setStatusLabel(status);
            }

            // cancel possible editing in attributes table, and create new table for selected node
            CellEditor cellEditor = this.attributesTable.getCellEditor();
            if (cellEditor != null)
            {
                cellEditor.stopCellEditing();
            }
            cellEditor = this.treeTable.getCellEditor();
            if (cellEditor != null)
            {
                cellEditor.stopCellEditing();
            }
            this.attributesTable.setModel(new AttributesTableModel(node, this.treeTable));

            // fire selection changed event
            try
            {
                this.editor.fireEvent(OtsEditor.SELECTION_CHANGED, node);
            }
            catch (RemoteException exception)
            {
                // TODO log
                exception.printStackTrace();
            }
        }
        else
        {
            // no selection, clear attributes table
            this.attributesTable.setModel(new AttributesTableModel(null, this.treeTable));
        }
    }

    @Override
    public void notify(final Event event) throws RemoteException
    {
        if (this.listening != null)
        {
            if (event.getType().equals(XsdTreeNode.ATTRIBUTE_CHANGED) && "Id".equals(((Object[]) event.getContent())[1]))
            {
                this.editor.setCoupledNode(this.listening.getCoupledNodeAttribute("Id"), this.listening, "Id");
            }
            else if (event.getType().equals(XsdTreeNode.VALUE_CHANGED))
            {
                this.editor.setCoupledNode(this.listening.getCoupledNodeValue(), this.listening, null);
            }
        }
    }

    // treetable tree will expand listener

    @Override
    public void treeWillExpand(final TreeExpansionEvent event) throws ExpandVetoException
    {
        this.editor.setMayPresentChoice(false);
    }

    @Override
    public void treeWillCollapse(final TreeExpansionEvent event) throws ExpandVetoException
    {
        this.editor.setMayPresentChoice(false);
    }

    // treetable mouse motion listener

    @Override
    public void mouseMoved(final MouseEvent e)
    {
        this.editor.setMayPresentChoice(true);

        // ToolTip
        XsdTreeNode treeNode = this.editor.getTreeNodeAtPoint(e.getPoint());
        try
        {
            if (!treeNode.isSelfValid())
            {
                this.treeTable.getTree().setToolTipText(treeNode.reportInvalidNode());
            }
            else
            {
                this.treeTable.getTree().setToolTipText(null);
            }
        }
        catch (Exception ex)
        {
            // TODO log
            if (treeNode.isIdentifiable())
            {
                System.out.println("Node " + treeNode.getId() + " is not valid.");
            }
            else
            {
                System.out.println("Node " + treeNode.getNodeName() + " is not valid.");
            }
        }
    }

    // mouse listener

    @Override
    public void mousePressed(final MouseEvent e)
    {
        if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1)
        {
            TableCellEditor cellEditor = this.attributesTable.getCellEditor();
            if (cellEditor != null)
            {
                // This prevents a null pointer when editing in the attributes and clicking on the table:
                // Exception in thread "AWT-EventQueue-0" java.lang.NullPointerException: Cannot invoke
                // "javax.swing.JTree.getToggleClickCount()" because "this.tree" is null at
                // java.desktop/javax.swing.plaf.basic.BasicTreeUI.isToggleEvent(BasicTreeUI.java:2697)
                cellEditor.stopCellEditing();
            }

            // show value options popup
            int row = this.treeTable.rowAtPoint(e.getPoint());
            int col = this.treeTable.columnAtPoint(e.getPoint());
            if (this.treeTable.isCellEditable(row, col))
            {
                XsdTreeNode treeNode = this.editor.getTreeNodeAtPoint(e.getPoint());
                int colNumber = this.treeTable.convertColumnIndexToModel(col);
                if (colNumber == 1 && treeNode.isIdentifiable())
                {
                    List<String> allOptions = treeNode.getIdRestrictions();
                    this.editor.valueOptionsPopup(allOptions, this.treeTable, (t) ->
                    {
                        this.editor.getUndo().startAction(ActionType.ID_CHANGE, treeNode, "Id");
                        treeNode.setId(t);
                    });
                }
                else if (colNumber == 2)
                {
                    List<String> allOptions = treeNode.getValueRestrictions();
                    this.editor.valueOptionsPopup(allOptions, this.treeTable, (t) ->
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
        XsdTreeNode treeNode = this.editor.getTreeNodeAtPoint(e.getPoint());
        if (e.getButton() == MouseEvent.BUTTON1)
        {
            if (e.getClickCount() > 1
                    && this.treeTable.convertColumnIndexToModel(this.treeTable.columnAtPoint(e.getPoint())) == 0)
            {
                // activate
                if (!treeNode.isActive() && !treeNode.isIncluded())
                {
                    this.editor.getUndo().startAction(ActionType.ACTIVATE, treeNode, null);
                    treeNode.setActive();
                    this.treeTable.updateUI();
                    this.attributesTable.setModel(new AttributesTableModel(treeNode, this.treeTable));
                }
                return;
            }
            // show choice popup
            if (!treeNode.isActive() || treeNode.isIncluded())
            {
                return;
            }
            int col = this.treeTable.columnAtPoint(e.getPoint());
            if (this.treeTable.convertColumnIndexToModel(col) != 0)
            {
                return;
            }
            int x = 0; // columns can be reordered, so adjust x coordinate to frame of tree column within the tree table
            for (int c = 0; c < col; c++)
            {
                x += this.treeTable.getColumnModel().getColumn(c).getWidth();
            }
            Rectangle labelPortion = this.treeTable.getTree()
                    .getPathBounds(this.treeTable.getTree().getPathForLocation(e.getPoint().x - x, e.getPoint().y));
            if (labelPortion != null && labelPortion.contains(e.getPoint().x - x, e.getPoint().y)
                    && this.editor.mayPresentChoice() && treeNode.isChoice())
            {
                JPopupMenu popup = new JPopupMenu();
                for (XsdOption option : treeNode.getOptions())
                {
                    JMenuItem button = new JMenuItem(option.optionNode().getShortString());
                    if (!option.selected())
                    {
                        button.addActionListener(new ChoiceListener(option.optionNode(), this.editor));
                    }
                    button.setFont(this.treeTable.getFont());
                    popup.add(button);
                }
                this.editor.preparePopupRemoval(popup, this.treeTable);
                this.treeTable.setComponentPopupMenu(popup);
                this.editor.setChoiceNode(treeNode);
                popup.show(this.treeTable, (int) labelPortion.getMinX() + x, (int) labelPortion.getMaxY() - 1);
            }
        }
        else if (e.getButton() == MouseEvent.BUTTON3)
        {
            // show actions popup
            int row = this.treeTable.rowAtPoint(e.getPoint());
            this.treeTable.setRowSelectionInterval(row, row);
            createRightClickPopup(e, treeNode);
        }
    }

    /**
     * Creates a panel with actions for a node. These contain active consumers (editors), moving up/down, and addition and
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
                    XsdTreeListener.this.editor.showDescription(treeNode.getDescription());
                }
            });
            item.setFont(this.treeTable.getFont());
            popup.add(item);
            anyAdded = true;
        }

        if (!treeNode.isIncluded())
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
    @SuppressWarnings("methodlength")
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
                    XsdTreeListener.this.editor.getNodeActions().add(treeNode);
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
                    XsdTreeListener.this.editor.getNodeActions().duplicate(treeNode);
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
                    if (XsdTreeListener.this.editor.confirmNodeRemoval(treeNode))
                    {
                        XsdTreeListener.this.editor.getNodeActions().remove(treeNode);
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
                    XsdTreeListener.this.editor.getNodeActions().copy(treeNode);
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
                    XsdTreeListener.this.editor.getNodeActions().cut(treeNode);
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
                        XsdTreeListener.this.editor.getNodeActions().insert(treeNode);
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
                        XsdTreeListener.this.editor.getNodeActions().paste(treeNode);
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
                    XsdTreeListener.this.editor.getNodeActions().revolveOption(treeNode, options);
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
            TreePath path = this.treeTable.getTree().getSelectionPath();
            boolean expanded = this.treeTable.getTree().isExpanded(path);
            JMenuItem expand = new JMenuItem(expanded ? "Collapse" : "Expand");
            expand.addActionListener(new ActionListener()
            {
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    XsdTreeListener.this.editor.getNodeActions().expand(treeNode, path, expanded);
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
                    XsdTreeListener.this.editor.getNodeActions().move(treeNode, -1);
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
                    XsdTreeListener.this.editor.getNodeActions().move(treeNode, 1);
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
