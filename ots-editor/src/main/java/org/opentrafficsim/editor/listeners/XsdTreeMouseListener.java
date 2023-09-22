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

import org.opentrafficsim.editor.AttributesTableModel;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdOption;
import org.opentrafficsim.editor.XsdTreeNode;

import de.javagl.treetable.JTreeTable;

/**
 * Listener for mouse events on the tree.
 * @author wjschakel
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
     * @param editor OtsEditor; editor.
     * @param treeTable JTreeTable; tree table.
     * @param attributesTable JTable; attributes table.
     */
    public XsdTreeMouseListener(final OtsEditor editor, final JTreeTable treeTable, final JTable attributesTable)
    {
        this.editor = editor;
        this.treeTable = treeTable;
        this.attributesTable = attributesTable;
    }

    /** {@inheritDoc} */
    @Override
    public void mousePressed(final MouseEvent e)
    {
        if (e.getButton() == MouseEvent.BUTTON1 && e.getClickCount() == 1)
        {
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
                        this.editor.getUndo().startAction("id change", treeNode, "Id");
                        treeNode.setId(t);
                    });
                }
                else if (colNumber == 2)
                {
                    List<String> allOptions = treeNode.getValueRestrictions();
                    this.editor.optionsPopup(allOptions, this.treeTable, (t) ->
                    {
                        this.editor.getUndo().startAction("value change", treeNode, null);
                        treeNode.setValue(t);
                    });
                }
            }
        }
    }

    /** {@inheritDoc} */
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
                    this.editor.getUndo().startAction("activate", treeNode, null);
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
                    if (option.isFirstInGroup())
                    {
                        if (!firstEntry)
                        {
                            popup.add(new JSeparator());
                        }
                    }
                    firstEntry = false;
                    JMenuItem button = new JMenuItem(option.getOptionNode().getShortString());
                    if (!option.isSelected())
                    {
                        button.addActionListener(new ChoiceListener(option.getChoice(), option.getOptionNode(), row,
                                this.editor, this.treeTable, this.attributesTable));
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
     * @param e MouseEvent; mouse event.
     * @param treeNode XsdTreeNode; node that was clicked on.
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
                    /** {@inheritDoc} */
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
                /** {@inheritDoc} */
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

        if (treeNode.isActive() && !treeNode.isInclude())
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
     * @param treeNode XsdTreeNode; node.
     * @param popup JPopupMenu; popup menu.
     * @param added boolean; whether any menu items were added before (which requires a separator).
     * @return boolean; whether any items were added, going in to this method, or during the method.
     */
    private boolean addDefaultActions(final XsdTreeNode treeNode, final JPopupMenu popup, final boolean added)
    {
        boolean anyAdded = added;
        boolean separatorNeeded = anyAdded;
        boolean groupAdded = false;

        if (treeNode.isAddable())
        {
            if (separatorNeeded)
            {
                separatorNeeded = false;
                popup.add(new JSeparator());
            }
            JMenuItem add = new JMenuItem("Add");
            add.addActionListener(new ActionListener()
            {
                /** {@inheritDoc} */
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    XsdTreeMouseListener.this.editor.getUndo().startAction("add", treeNode, null);
                    treeNode.add();
                    XsdTreeMouseListener.this.treeTable.updateUI();
                }
            });
            add.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_W, KeyEvent.CTRL_DOWN_MASK));
            add.setFont(this.treeTable.getFont());
            popup.add(add);
            JMenuItem copy = new JMenuItem("Duplicate");
            copy.addActionListener(new ActionListener()
            {
                /** {@inheritDoc} */
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    XsdTreeMouseListener.this.editor.getUndo().startAction("duplicate", treeNode, null);
                    treeNode.duplicate();
                    XsdTreeMouseListener.this.treeTable.updateUI();
                }
            });
            copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_D, KeyEvent.CTRL_DOWN_MASK));
            copy.setFont(this.treeTable.getFont());
            popup.add(copy);
            anyAdded = true;
            groupAdded = true;
        }
        if (treeNode.isRemovable())
        {
            if (separatorNeeded)
            {
                separatorNeeded = false;
                popup.add(new JSeparator());
            }
            JMenuItem remove = new JMenuItem("Remove");
            remove.addActionListener(new ActionListener()
            {
                /** {@inheritDoc} */
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    if (XsdTreeMouseListener.this.editor.confirmNodeRemoval(treeNode))
                    {
                        int selected = XsdTreeMouseListener.this.treeTable.getTree().getLeadSelectionRow();
                        XsdTreeMouseListener.this.editor.getUndo().startAction("remove", treeNode, null);
                        treeNode.remove();
                        XsdTreeMouseListener.this.treeTable.updateUI();
                        XsdTreeMouseListener.this.treeTable.getSelectionModel().setSelectionInterval(selected, selected);
                        int column = XsdTreeMouseListener.this.treeTable.convertColumnIndexToView(0);
                        if (XsdTreeMouseListener.this.treeTable.getValueAt(selected, column).equals(treeNode))
                        {
                            XsdTreeMouseListener.this.attributesTable
                                    .setModel(new AttributesTableModel(null, XsdTreeMouseListener.this.treeTable));
                        }
                    }
                }
            });
            remove.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0));
            remove.setFont(this.treeTable.getFont());
            popup.add(remove);
            anyAdded = true;
            groupAdded = true;
        }
        List<XsdOption> options = treeNode.getOptions();
        if (treeNode.isChoice() && options.size() > 1)
        {
            if (separatorNeeded)
            {
                separatorNeeded = false;
                popup.add(new JSeparator());
            }
            JMenuItem revolve = new JMenuItem("Revolve option");
            revolve.addActionListener(new ActionListener()
            {
                /** {@inheritDoc} */
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    int optionIndex = 0;
                    for (int i = 0; i < options.size(); i++)
                    {
                        if (options.get(i).getOptionNode().equals(treeNode))
                        {
                            optionIndex = i + 1;
                            break;
                        }
                    }
                    if (optionIndex >= options.size())
                    {
                        optionIndex = 0;
                    }
                    XsdTreeMouseListener.this.editor.getUndo().startAction("option", treeNode, null);
                    treeNode.setOption(options.get(optionIndex).getOptionNode());
                    XsdTreeMouseListener.this.treeTable.updateUI();
                }
            });
            revolve.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_R, KeyEvent.CTRL_DOWN_MASK));
            revolve.setFont(this.treeTable.getFont());
            popup.add(revolve);
            anyAdded = true;
            groupAdded = true;
        }
        if (treeNode.getChildCount() > 0)
        {
            if (separatorNeeded)
            {
                separatorNeeded = false;
                popup.add(new JSeparator());
            }
            JMenuItem expand = new JMenuItem("Expand");
            expand.addActionListener(new ActionListener()
            {
                /** {@inheritDoc} */
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    if (!treeNode.isActive())
                    {
                        XsdTreeMouseListener.this.editor.getUndo().startAction("activate", treeNode, null);
                        treeNode.setActive();
                    }
                    XsdTreeMouseListener.this.treeTable.getTree()
                            .expandPath(XsdTreeMouseListener.this.treeTable.getTree().getSelectionPath());
                    XsdTreeMouseListener.this.editor.show(treeNode, null);
                }
            });
            expand.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_E, KeyEvent.CTRL_DOWN_MASK));
            expand.setFont(this.treeTable.getFont());
            popup.add(expand);
            anyAdded = true;
            groupAdded = true;
        }

        separatorNeeded = groupAdded;

        if (treeNode.canMoveUp())
        {
            if (separatorNeeded)
            {
                separatorNeeded = false;
                popup.add(new JSeparator());
            }
            JMenuItem moveUp = new JMenuItem("Move up");
            moveUp.addActionListener(new ActionListener()
            {
                /** {@inheritDoc} */
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    XsdTreeMouseListener.this.editor.getUndo().startAction("move", treeNode, null);
                    treeNode.move(-1);
                    XsdTreeMouseListener.this.treeTable.updateUI();
                }
            });
            moveUp.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_UP, KeyEvent.CTRL_DOWN_MASK));
            moveUp.setFont(this.treeTable.getFont());
            popup.add(moveUp);
            anyAdded = true;
        }
        if (treeNode.canMoveDown())
        {
            if (separatorNeeded)
            {
                separatorNeeded = false;
                popup.add(new JSeparator());
            }
            JMenuItem moveDown = new JMenuItem("Move down");
            moveDown.addActionListener(new ActionListener()
            {
                /** {@inheritDoc} */
                @Override
                public void actionPerformed(final ActionEvent e)
                {
                    XsdTreeMouseListener.this.editor.getUndo().startAction("move", treeNode, null);
                    treeNode.move(1);
                    XsdTreeMouseListener.this.treeTable.updateUI();
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
