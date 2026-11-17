package org.opentrafficsim.editor;

import java.util.Arrays;
import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

import javax.swing.SwingUtilities;
import javax.swing.tree.TreePath;

import org.djutils.eval.Eval;
import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.w3c.dom.Document;

import com.google.common.base.Objects;

import de.javagl.treetable.AbstractTreeTableModel;
import de.javagl.treetable.JTreeTable;
import de.javagl.treetable.TreeTableModel;

/**
 * Defines the columns in the {@code JTreeTable}. Most functionality is forwarded to the tree with {@code XsdTreeNode}'s.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class XsdTreeTableModel extends AbstractTreeTableModel implements EventListener
{

    /** Column names. */
    private static final String[] COLUMN_NAMES = new String[] {"Item", "Id", "Value", "#"};

    /** Index of the tree column. */
    public static final int TREE_COLUMN = Arrays.asList(COLUMN_NAMES).indexOf("Item");

    /** Index of the Id column. */
    public static final int ID_COLUMN = Arrays.asList(COLUMN_NAMES).indexOf("Id");

    /** Index of the Value column. */
    public static final int VALUE_COLUMN = Arrays.asList(COLUMN_NAMES).indexOf("Value");

    /** Minimum column widths. */
    private static final int[] MIN_COLUMN_WIDTHS = new int[] {100, 50, 50, 30};

    /** Preferred column widths. */
    private static final int[] PREFERRED_COLUMN_WIDTHS = new int[] {600, 200, 200, 30};

    /** Column classes. */
    private static final Class<?>[] COLUMN_CLASSES =
            new Class<?>[] {TreeTableModel.class, String.class, String.class, String.class};

    /** Tree table, so it can be repainted when a value or attribute has changed. */
    private JTreeTable treeTable;

    /** Node that is selected, or that should be selected when the tree table has processed pending changes. */
    private XsdTreeNode selectedNode;

    /** Whether the model is currently processing a data structure event. */
    private boolean updatingSelection;

    /** Whether the selection is currently being restored from the selected node. */
    private boolean restoringSelection;

    /** Root of the data structure. */
    private final XsdTreeNodeRoot dataStructureRoot;

    /**
     * Nodes that have been inserted into the tree table model. This is used to prevent removal or replacement events for nodes
     * that are not visible in the tree, e.g. options of a choice that are not selected.
     */
    private final Set<XsdTreeNode> inserted = Collections.newSetFromMap(new IdentityHashMap<>());

    /** Whether changes should be ignored, e.g. while loading a file. */
    private final Supplier<Boolean> ignoreChanges;

    /**
     * Constructor.
     * @param document XSD document.
     * @param ignoreChanges whether to ignore changes
     * @param evalSupplier eval supplier
     */
    protected XsdTreeTableModel(final Document document, final Supplier<Boolean> ignoreChanges,
            final Supplier<Eval> evalSupplier)
    {
        super(document == null ? null : new XsdTreeNodeRoot(new Schema(document), ignoreChanges, evalSupplier));
        this.dataStructureRoot = (XsdTreeNodeRoot) this.root; // created in line above, set by super
        this.ignoreChanges = ignoreChanges;
        if (this.dataStructureRoot != null)
        {
            this.dataStructureRoot.addAllNodesListener(this, XsdTreeNodeRoot.NODE_CREATED);
            this.dataStructureRoot.addAllNodesListener(this, XsdTreeNodeRoot.NODE_REMOVED);
            this.dataStructureRoot.addAllNodesListener(this, XsdTreeNode.VALUE_CHANGED);
            this.dataStructureRoot.addAllNodesListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
            this.dataStructureRoot.addAllNodesListener(this, XsdTreeNode.OPTION_CHANGED);
            this.dataStructureRoot.addAllNodesListener(this, XsdTreeNode.ACTIVATION_CHANGED);
            this.dataStructureRoot.addAllNodesListener(this, XsdTreeNode.MOVED);
        }
    }

    /**
     * Changes the root of the tree table model (not of the data structure as such).
     * @param root new root, may be {@code null} to show the complete data structure
     */
    public void setRoot(final XsdTreeNode root)
    {
        XsdTreeNode potentialRoot = root == null ? this.dataStructureRoot : root;
        if (!Objects.equal(potentialRoot, this.root))
        {
            this.root = potentialRoot;
            fireTreeStructureChanged(this, new Object[] {this.root}, null, null);
        }
    }

    /**
     * Sets the tree table. This is used to repaint the tree table when a node value or attribute changes, and to track the
     * selected node so the selection can be restored when the tree table processes changes in the tree structure.
     * @param treeTable tree table.
     */
    public void setTreeTable(final JTreeTable treeTable)
    {
        this.treeTable = treeTable;
        if (treeTable != null)
        {
            TreePath path = treeTable.getTree().getSelectionPath();
            this.selectedNode = path == null ? null : (XsdTreeNode) path.getLastPathComponent();
            treeTable.getTree().addTreeSelectionListener((event) ->
            {
                if (!this.updatingSelection && !this.restoringSelection)
                {
                    TreePath selectionPath = treeTable.getTree().getSelectionPath();
                    if (selectionPath != null)
                    {
                        this.selectedNode = (XsdTreeNode) selectionPath.getLastPathComponent();
                    }
                }
            });
        }
    }

    /**
     * Returns the root of the data structure, rather than of the tree table view.
     * @return the root of the data structure, rather than of the tree table view
     */
    public XsdTreeNodeRoot getDataRoot()
    {
        return this.dataStructureRoot;
    }

    /**
     * Refreshes the entire tree table. This can be used after bulk changes to the data structure, such as loading a file, where
     * the individual events would result in many redundant updates. This also rebuilds the administration of nodes that are
     * currently visible.
     */
    public void refresh()
    {
        this.inserted.clear();
        if (this.dataStructureRoot != null)
        {
            markInserted(this.dataStructureRoot);
        }
        if (this.root != null)
        {
            fireTreeStructureChanged(this, new Object[] {this.root}, null, null);
        }
    }

    @Override
    public void notify(final Event event)
    {
        if (this.ignoreChanges != null && this.ignoreChanges.get())
        {
            return; // bulk changes, e.g. loading a file, are followed by a refresh
        }
        this.updatingSelection = true;
        boolean structuralEvent = true;
        try
        {
            Object[] content = (Object[]) event.getContent();
            if (event.getType().equals(XsdTreeNodeRoot.NODE_CREATED))
            {
                nodeCreated((XsdTreeNode) content[0]);
            }
            else if (event.getType().equals(XsdTreeNodeRoot.NODE_REMOVED))
            {
                nodeRemoved((XsdTreeNode) content[0], (XsdTreeNode) content[1], (Integer) content[2]);
            }
            else if (event.getType().equals(XsdTreeNode.VALUE_CHANGED) || event.getType().equals(XsdTreeNode.ATTRIBUTE_CHANGED))
            {
                structuralEvent = false;
                nodeChanged((XsdTreeNode) content[0]);
            }
            else if (event.getType().equals(XsdTreeNode.OPTION_CHANGED))
            {
                optionChanged((XsdTreeNode) content[0], (XsdTreeNode) content[1], (XsdTreeNode) content[2]);
            }
            else if (event.getType().equals(XsdTreeNode.ACTIVATION_CHANGED))
            {
                activationChanged((XsdTreeNode) content[0]);
            }
            else if (event.getType().equals(XsdTreeNode.MOVED))
            {
                nodeMoved((XsdTreeNode) content[0], (Integer) content[1], (Integer) content[2]);
            }
        }
        finally
        {
            this.updatingSelection = false;
        }
        if (structuralEvent)
        {
            // The tree table defers its table data change, and clears its selection when processing it. Restore the selection
            // only after a structural change by the above events.
            SwingUtilities.invokeLater(this::restoreSelection);
        }
    }

    /**
     * Handles a created node. Nodes that are not (yet) part of the visible tree, such as options of a choice that are not
     * selected, are ignored.
     * @param node created node
     */
    private void nodeCreated(final XsdTreeNode node)
    {
        XsdTreeNode parent = node.getParent();
        if (parent == null || !parent.isActive())
        {
            return; // root, or parent is not visible
        }
        Object[] path = pathTo(parent);
        if (path == null)
        {
            return;
        }
        int index = parent.children == null ? -1 : parent.children.indexOf(node);
        if (index < 0)
        {
            return; // node is not part of the visible tree (yet)
        }
        markInserted(node);
        if (this.selectedNode == null || !this.selectedNode.equals(parent))
        {
            // A new node becomes the selection, unless it is created while its parent is selected. The latter typically
            // happens when children are created lazily as a result of expanding the selected parent.
            this.selectedNode = node;
        }
        try
        {
            fireTreeNodesInserted(this, path, new int[] {index}, new Object[] {node});
        }
        catch (IndexOutOfBoundsException ex)
        {
            // fallback; we do not want the GUI to hang over this -- there is a hole somewhere in the registration
            fireTreeStructureChanged(this, path, null, null);
        }
    }

    /**
     * Handles a removed node.
     * @param node removed node
     * @param parent parent of the removed node
     * @param index index at which the node was located in the parent
     */
    private void nodeRemoved(final XsdTreeNode node, final XsdTreeNode parent, final int index)
    {
        if (parent == null || !parent.isActive() || index < 0)
        {
            return;
        }
        Object[] path = pathTo(parent);
        if (path == null || !this.inserted.remove(node))
        {
            return;
        }
        if (isDescendantOrSelf(node, this.selectedNode))
        {
            int childCount = parent.children == null ? 0 : parent.children.size();
            this.selectedNode = childCount == 0 ? parent : parent.children.get(Math.min(index, childCount - 1));
        }
        try
        {
            fireTreeNodesRemoved(this, path, new int[] {index}, new Object[] {node});
        }
        catch (IndexOutOfBoundsException ex)
        {
            // fallback; we do not want the GUI to hang over this -- there is a hole somewhere in the registration
            fireTreeStructureChanged(this, path, null, null);
        }
    }

    /**
     * Handles a change of a node value or attribute. The tree table is repainted directly, rather than firing a tree model
     * event. The {@code TreeTableModelAdapter} translates any tree model event into a full table data change, and
     * {@code JTable} clears its selection on such an event. That would deselect the row after the first keystroke while
     * editing. A value or attribute change does not alter the tree structure, so a repaint is sufficient and keeps the
     * selection and the cell editor intact.
     * @param node changed node
     */
    private void nodeChanged(final XsdTreeNode node)
    {
        if (this.treeTable != null && pathTo(node) != null)
        {
            node.invalidate();
            this.treeTable.repaint();
        }
    }

    /**
     * Handles a change of the selected option of a choice. The event is fired for every option, so only the event of the newly
     * selected option is used; events for cleared selections, and for options that are not selected, are ignored. Changes on
     * nodes that are not part of the visible tree, e.g. because another node is the root of the tree table model, are also
     * ignored, as no tree event can be fired for them.
     * @param option option on which the event was fired
     * @param selected newly selected option, or {@code null} when the selection was cleared
     * @param previous previously selected option
     */
    private void optionChanged(final XsdTreeNode option, final XsdTreeNode selected, final XsdTreeNode previous)
    {
        if (previous == selected)
        {
            return; // no structural change, e.g. an event used to trigger a listener
        }
        if (selected == null)
        {
            // A cleared selection does not change the structure of the visible tree, so there is nothing to fire.
            return;
        }
        if (option != selected)
        {
            return;
        }
        XsdTreeNode parent = selected.getParent();
        if (parent == null || !parent.isActive())
        {
            return; // parent is not visible in the current tree
        }
        Object[] path = pathTo(parent);
        if (path == null)
        {
            // The parent is not part of the visible tree, e.g. because another node is the root of the tree table model;
            // firing an event would apply an index of a hidden parent to the current tree root.
            return;
        }
        int index = parent.children == null ? -1 : parent.children.indexOf(selected);
        if (index < 0)
        {
            return;
        }
        if (isDescendantOrSelf(previous, this.selectedNode))
        {
            this.selectedNode = equivalentNode(previous, selected, this.selectedNode);
        }
        boolean removedPrevious = previous != null && this.inserted.remove(previous);
        try
        {
            if (removedPrevious)
            {
                fireTreeNodesRemoved(this, path, new int[] {index}, new Object[] {previous});
            }
            if (removedPrevious || !this.inserted.contains(selected))
            {
                markInserted(selected);
                fireTreeNodesInserted(this, path, new int[] {index}, new Object[] {selected});
            }
        }
        catch (IndexOutOfBoundsException ex)
        {
            // fallback; we do not want the GUI to hang over this -- there is a hole somewhere in the registration
            fireTreeStructureChanged(this, path, null, null);
        }
    }

    /**
     * Handles a change of the activation state of a node. The children of the node are added or removed, which is a change of
     * the structure at the node.
     * @param node node with changed activation
     */
    private void activationChanged(final XsdTreeNode node)
    {
        Object[] path = pathTo(node);
        if (this.selectedNode != null && pathTo(this.selectedNode) == null && path != null)
        {
            this.selectedNode = node;
        }
        if (path != null)
        {
            fireTreeStructureChanged(this, path, null, null);
        }
    }

    /**
     * Handles a moved node.
     * @param node moved node
     * @param oldIndex old index of the node in the parent
     * @param newIndex new index of the node in the parent
     */
    private void nodeMoved(final XsdTreeNode node, final int oldIndex, final int newIndex)
    {
        XsdTreeNode parent = node.getParent();
        if (parent == null || !parent.isActive())
        {
            return;
        }
        Object[] path = pathTo(parent);
        if (path == null || !this.inserted.contains(node))
        {
            return;
        }
        try
        {
            fireTreeNodesRemoved(this, path, new int[] {oldIndex}, new Object[] {node});
            fireTreeNodesInserted(this, path, new int[] {newIndex}, new Object[] {node});
        }
        catch (IndexOutOfBoundsException ex)
        {
            // fallback; we do not want the GUI to hang over this -- there is a hole somewhere in the registration
            fireTreeStructureChanged(this, path, null, null);
        }
    }

    /**
     * Restores the selection after the tree table has processed a change in the tree structure. The tree table clears its
     * selection when its table model changes, which is triggered by the tree model events that keep the tree table up to date.
     * A node that is not currently visible, for example because an ancestor is collapsed, is left unselected. Selecting it
     * would expand the collapsed ancestor.
     */
    private void restoreSelection()
    {
        this.restoringSelection = true;
        try
        {
            if (this.treeTable == null || this.selectedNode == null)
            {
                return;
            }
            if (this.ignoreChanges != null && this.ignoreChanges.get())
            {
                return;
            }
            Object[] path = pathTo(this.selectedNode);
            if (path != null)
            {
                int row = this.treeTable.getTree().getRowForPath(new TreePath(path));
                if (row >= 0 && !this.treeTable.isRowSelected(row))
                {
                    this.treeTable.setRowSelectionInterval(row, row);
                }
            }
        }
        finally
        {
            this.restoringSelection = false;
        }
    }

    /**
     * Clears the selection in the tree. Any pending restoration of the selection is cancelled.
     */
    public void clearSelection()
    {
        this.selectedNode = null;
        if (this.treeTable != null)
        {
            this.treeTable.getTree().clearSelection();
        }
    }

    /**
     * Returns whether the given node is equal to, or a descendant of, the possible ancestor.
     * @param ancestor possible ancestor
     * @param node possible descendant
     * @return whether the given node is equal to, or a descendant of, the possible ancestor
     */
    private static boolean isDescendantOrSelf(final XsdTreeNode ancestor, final XsdTreeNode node)
    {
        if (ancestor == null || node == null)
        {
            return false;
        }
        for (XsdTreeNode current = node; current != null; current = current.getParent())
        {
            if (current == ancestor)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns the node in the newly selected option that corresponds to the selection in the previously selected option. If no
     * corresponding node can be found, the newly selected option itself is returned.
     * @param previous previously selected option
     * @param selected newly selected option
     * @param selection selected node, which is the previous option or a descendant of it
     * @return node in the newly selected option that corresponds to the selection
     */
    private static XsdTreeNode equivalentNode(final XsdTreeNode previous, final XsdTreeNode selected,
            final XsdTreeNode selection)
    {
        if (selection == previous)
        {
            return selected;
        }
        List<XsdTreeNode> path = selection.getPath();
        int index = path.indexOf(previous);
        if (index < 0)
        {
            return selected;
        }
        XsdTreeNode equivalent = selected;
        for (int i = index + 1; i < path.size(); i++)
        {
            XsdTreeNode source = path.get(i);
            int childIndex = source.getParent() == null || source.getParent().children == null ? -1
                    : source.getParent().children.indexOf(source);
            if (childIndex < 0 || equivalent.children == null || childIndex >= equivalent.children.size())
            {
                return selected;
            }
            equivalent = equivalent.children.get(childIndex);
        }
        return equivalent;
    }

    /**
     * Marks a node and all currently existing visible descendants as inserted. This is required as nodes can be created while
     * they are not visible, e.g. options of a choice that is not selected, and become visible later without new creation
     * events.
     * @param node node that has become visible
     */
    private void markInserted(final XsdTreeNode node)
    {
        this.inserted.add(node);
        if (node.children != null)
        {
            for (XsdTreeNode child : node.children)
            {
                markInserted(child);
            }
        }
    }

    /**
     * Returns the path from the current root of the model to the given node, or {@code null} if the node is not visible in the
     * current model.
     * @param node node
     * @return path from the current root of the model to the given node, or {@code null} if the node is not visible in the
     *         current model
     */
    private Object[] pathTo(final XsdTreeNode node)
    {
        if (node == null || this.root == null)
        {
            return null;
        }
        List<XsdTreeNode> path = node.getPath();
        int index = path.indexOf(this.root);
        if (index < 0)
        {
            return null;
        }
        for (int i = index + 1; i < path.size(); i++)
        {
            XsdTreeNode parent = path.get(i - 1);
            if (!parent.isActive() || parent.children == null || !parent.children.contains(path.get(i)))
            {
                return null; // node is not part of the visible tree
            }
        }
        return path.subList(index, path.size()).toArray();
    }

    @Override
    public int getColumnCount()
    {
        return COLUMN_CLASSES.length;
    }

    @Override
    public String getColumnName(final int column)
    {
        return COLUMN_NAMES[column];
    }

    @Override
    public Class<?> getColumnClass(final int column)
    {
        return COLUMN_CLASSES[column];
    }

    @Override
    public Object getValueAt(final Object node, final int column)
    {
        if (column == TREE_COLUMN)
        {
            return node; // required for tree view of column 0
        }
        if (column == ID_COLUMN)
        {
            if (((XsdTreeNode) node).isIdentifiable())
            {
                return ((XsdTreeNode) node).getId();
            }
            return "";
        }
        else if (column == VALUE_COLUMN)
        {
            if (((XsdTreeNode) node).isEditable())
            {
                return ((XsdTreeNode) node).getValue();
            }
            return "";
        }
        return occurs(((XsdTreeNode) node).minOccurs(), ((XsdTreeNode) node).maxOccurs());
    }

    /**
     * Creates a string to display minOccurs and maxOccurs.
     * @param minOccurs minOccurs.
     * @param maxOccurs maxOccurs.
     * @return string to display minOccurs and maxOccurs.
     */
    public String occurs(final int minOccurs, final int maxOccurs)
    {
        if (minOccurs == maxOccurs)
        {
            return Integer.toString(minOccurs);
        }
        return Integer.valueOf(minOccurs) + ".." + (maxOccurs == -1 ? "∞" : Integer.valueOf(maxOccurs));
    }

    @Override
    public Object getChild(final Object parent, final int index)
    {
        return ((XsdTreeNode) parent).getChild(index);
    }

    @Override
    public int getChildCount(final Object parent)
    {
        return ((XsdTreeNode) parent).getChildCount();
    }

    @Override
    public boolean isCellEditable(final Object node, final int column)
    {
        if (column == TREE_COLUMN)
        {
            return true; // required for tree in column 0
        }
        XsdTreeNode treeNode = (XsdTreeNode) node;
        if (column == ID_COLUMN)
        {
            return treeNode.isIdentifiable() && !treeNode.isIncluded();
        }
        return treeNode.isEditable() && column == VALUE_COLUMN && !treeNode.isIncluded();
    }

    @Override
    public void setValueAt(final Object aValue, final Object node, final int column)
    {
        if (column == ID_COLUMN)
        {
            ((XsdTreeNode) node).setId(aValue.toString());
        }
        else if (column == VALUE_COLUMN)
        {
            ((XsdTreeNode) node).setValue(aValue.toString());
        }
    }

    /**
     * Apply the column widths to a newly created tree table.
     * @param treeTable tree table.
     */
    public static void applyColumnWidth(final JTreeTable treeTable)
    {
        for (int i = 0; i < treeTable.getColumnCount(); i++)
        {
            treeTable.getColumn(COLUMN_NAMES[i]).setMinWidth(MIN_COLUMN_WIDTHS[i]);
            treeTable.getColumn(COLUMN_NAMES[i]).setPreferredWidth(PREFERRED_COLUMN_WIDTHS[i]);
        }
    }

}
