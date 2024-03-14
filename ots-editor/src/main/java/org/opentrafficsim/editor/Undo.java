package org.opentrafficsim.editor;

import java.rmi.RemoteException;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;

import javax.swing.AbstractButton;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.exceptions.Throw;
import org.djutils.exceptions.Try;

/**
 * Undo unit for the OTS editor. This class stores an internal queue of actions. Changes to XsdTreeNodes should be grouped per
 * single user input in an action. All actions need to be initiated externally using {@code startAction()}. This class will
 * itself listen to all relevant changes in the tree and add incoming sub-actions under the started action.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class Undo implements EventListener
{

    /** */
    private static final long serialVersionUID = 20230921L;

    /** Maximum number of undo actions stored. */
    private static final int MAX_UNDO = 50;

    /** Queue of actions. */
    private LinkedList<Action> queue = new LinkedList<>();

    /** Location of most recent undo action. */
    private int cursor = -1;

    /** Current queue of sub-actions from a single user input. */
    private Deque<SubAction> currentSet;

    /** OTS editor. */
    private final OtsEditor editor;

    /** Undo GUI item. */
    private final AbstractButton undoItem;

    /** Redo GUI item. */
    private final AbstractButton redoItem;

    /** Boolean to ignore changes during undo/redo, so no new undo/redo is made. */
    boolean ignoreChanges = false;

    /**
     * Constructor.
     * @param editor OtsEditor; editor.
     * @param undoItem AbstractButton; undo GUI item.
     * @param redoItem AbstractButton; redo GUI item.
     */
    public Undo(final OtsEditor editor, final AbstractButton undoItem, final AbstractButton redoItem)
    {
        this.editor = editor;
        this.undoItem = undoItem;
        this.redoItem = redoItem;
        this.undoItem.setEnabled(false);
        this.redoItem.setEnabled(false);
        Try.execute(() -> editor.addListener(this, OtsEditor.NEW_FILE), "Remote exception when listening for NEW_FILE events.");
    }

    /**
     * Clears the entire queue, suitable for when a new tree is loaded.
     */
    public void clear()
    {
        this.ignoreChanges = false;
        this.currentSet = null;
        this.cursor = -1;
        this.queue = new LinkedList<>();
    }

    /**
     * Tells the undo unit to ignore all changes. Reset this by calling {@code clear()}. Useful during file loading.
     */
    public void setIgnoreChanges()
    {
        this.ignoreChanges = true;
    }

    /**
     * Starts a new action, which groups all sub-actions until a new action is started.
     * @param type ActionType; action type.
     * @param node XsdTreeNode; node on which the action is applied, i.e. node that should be selected on undo/redo.
     * @param attribute String; attribute name, may be {@null} for actions that are not an attribute value change.
     */
    public void startAction(final ActionType type, final XsdTreeNode node, final String attribute)
    {
        if (this.ignoreChanges)
        {
            return;
        }
        if (this.currentSet != null && this.currentSet.isEmpty())
        {
            // last action set never resulted in any sub-action, overwrite it
            this.queue.pollLast();
        }

        // remove any possible redos fresher in the queue than our current pointer
        while (this.cursor < this.queue.size() - 1)
        {
            this.queue.pollLast();
        }

        // add new entry in queue
        this.currentSet = new ArrayDeque<>();
        this.queue.add(new Action(type, this.currentSet, node, node.parent, attribute));
        while (this.queue.size() > MAX_UNDO)
        {
            this.queue.pollFirst();
        }

        // set pointer to last non-empty element
        this.cursor = this.queue.size() - 2;
        updateButtons();
    }

    /**
     * Adds sub action to current action.
     * @param subAction SubAction; sub action.
     */
    private void add(final SubAction subAction)
    {
        Throw.when(this.currentSet == null, IllegalStateException.class,
                "Adding undo action without having called startUndoAction()");
        if (this.currentSet.isEmpty())
        {
            this.cursor = this.queue.size() - 1; // now the latest undo actually has content
            updateButtons();
        }
        this.currentSet.add(subAction);
    }

    /**
     * Returns whether an undo is available.
     * @return boolean; whether an undo is available.
     */
    public boolean canUndo()
    {
        return this.cursor >= 0;
    }

    /**
     * Returns whether a redo is available.
     * @return boolean; whether a redo is available.
     */
    public boolean canRedo()
    {
        return this.cursor < this.queue.size() - 1 && !this.queue.get(this.cursor + 1).subActions.isEmpty();
    }

    /**
     * Performs an undo.
     */
    public synchronized void undo()
    {
        if (this.ignoreChanges)
        {
            return;
        }
        this.ignoreChanges = true;

        Action action = this.queue.get(this.cursor);
        Iterator<SubAction> iterator = action.subActions.descendingIterator();
        while (iterator.hasNext())
        {
            iterator.next().undo();
        }
        action.parent.children.forEach((n) -> n.invalidate());
        action.parent.invalidate();
        this.editor.show(action.node, action.attribute);
        this.cursor--;
        updateButtons();
        this.ignoreChanges = false;
    }

    /**
     * Performs a redo.
     */
    public synchronized void redo()
    {
        if (this.ignoreChanges)
        {
            return;
        }
        this.ignoreChanges = true;
        this.cursor++;
        Action action = this.queue.get(this.cursor);
        Iterator<SubAction> iterator = action.subActions.iterator();
        while (iterator.hasNext())
        {
            iterator.next().redo();
        }
        action.parent.children.forEach((n) -> n.invalidate());
        action.parent.invalidate();
        this.editor.show(action.postActionShowNode, action.attribute);
        updateButtons();
        this.ignoreChanges = false;
    }

    /**
     * Update the enabled state and text of the undo and redo button.
     */
    public void updateButtons()
    {
        this.undoItem.setEnabled(canUndo());
        this.undoItem.setText(canUndo() ? ("Undo " + this.queue.get(this.cursor).type) : "Undo");
        this.redoItem.setEnabled(canRedo());
        this.redoItem.setText(canRedo() ? ("Redo " + this.queue.get(this.cursor + 1).type) : "Redo");
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
    {
        // listen and unlisten
        if (event.getType().equals(OtsEditor.NEW_FILE))
        {
            XsdTreeNodeRoot root = (XsdTreeNodeRoot) event.getContent();
            root.addListener(this, XsdTreeNodeRoot.NODE_CREATED);
            root.addListener(this, XsdTreeNodeRoot.NODE_REMOVED);
        }
        else if (event.getType().equals(XsdTreeNodeRoot.NODE_CREATED))
        {
            XsdTreeNode node = (XsdTreeNode) ((Object[]) event.getContent())[0];
            node.addListener(this, XsdTreeNode.VALUE_CHANGED);
            node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
            node.addListener(this, XsdTreeNode.OPTION_CHANGED);
            node.addListener(this, XsdTreeNode.ACTIVATION_CHANGED);
            node.addListener(this, XsdTreeNode.MOVED);
        }
        else if (event.getType().equals(XsdTreeNodeRoot.NODE_REMOVED))
        {
            XsdTreeNode node = (XsdTreeNode) ((Object[]) event.getContent())[0];
            node.removeListener(this, XsdTreeNode.VALUE_CHANGED);
            node.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
            node.removeListener(this, XsdTreeNode.OPTION_CHANGED);
            node.removeListener(this, XsdTreeNode.ACTIVATION_CHANGED);
            node.removeListener(this, XsdTreeNode.MOVED);
        }

        // ignore any changes during an undo or redo; these should not result in another undo or redo
        if (this.ignoreChanges)
        {
            return;
        }

        // store action for each change
        if (event.getType().equals(XsdTreeNodeRoot.NODE_CREATED))
        {
            Object[] content = (Object[]) event.getContent();
            XsdTreeNode node = (XsdTreeNode) content[0];
            XsdTreeNode parent = (XsdTreeNode) content[1];
            int index = (int) content[2];
            XsdTreeNode root = node.getRoot();
            add(new SubActionRunnable(() ->
            {
                parent.children.remove(node);
                node.parent = null;
                root.fireEvent(XsdTreeNodeRoot.NODE_REMOVED, new Object[] {node, parent, index});
            }, () ->
            {
                if (index >= 0)
                {
                    parent.setChild(index, node);
                }
                node.parent = parent;
                root.fireEvent(XsdTreeNodeRoot.NODE_CREATED, new Object[] {node, parent, index});
            }, "Create " + node.getPathString()));
        }
        else if (event.getType().equals(XsdTreeNodeRoot.NODE_REMOVED))
        {
            Object[] content = (Object[]) event.getContent();
            XsdTreeNode node = (XsdTreeNode) content[0];
            XsdTreeNode parent = (XsdTreeNode) content[1];
            int index = (int) content[2];
            XsdTreeNode root = parent.getRoot();
            add(new SubActionRunnable(() ->
            {
                if (index < 0)
                {
                    // non selected choice node
                    node.parent = parent;
                    root.fireEvent(XsdTreeNodeRoot.NODE_CREATED, new Object[] {node, parent, parent.children.indexOf(node)});
                }
                else
                {
                    parent.setChild(index, node);
                    root.fireEvent(XsdTreeNodeRoot.NODE_CREATED, new Object[] {node, parent, index});
                }
            }, () ->
            {
                node.parent.children.remove(node);
                node.parent = null;
                root.fireEvent(XsdTreeNodeRoot.NODE_REMOVED, new Object[] {node, parent, index});
            }, "Remove " + node.getPathString()));
        }
        else if (event.getType().equals(XsdTreeNode.VALUE_CHANGED))
        {
            Object[] content = (Object[]) event.getContent();
            XsdTreeNode node = (XsdTreeNode) content[0];
            String value = node.getValue();
            add(new SubActionRunnable(() ->
            {
                node.setValue((String) content[1]); // invokes event
                node.invalidate();
            }, () ->
            {
                node.setValue(value); // invokes event
                node.invalidate();
            }, "Change " + node.getPathString() + " value: " + value));
        }
        else if (event.getType().equals(XsdTreeNode.ATTRIBUTE_CHANGED))
        {
            Object[] content = (Object[]) event.getContent();
            XsdTreeNode node = (XsdTreeNode) content[0];
            String attribute = (String) content[1];
            String value = node.getAttributeValue(attribute);
            // for include nodes, setAttributeValue will trigger addition and removal of nodes, we can ignore these events
            if (node.xsdNode.equals(XiIncludeNode.XI_INCLUDE))
            {
                this.currentSet.clear();
            }
            add(new SubActionRunnable(() ->
            {
                node.setAttributeValue(attribute, (String) content[2]); // invokes event
                node.invalidate();
            }, () ->
            {
                node.setAttributeValue(attribute, value); // invokes event
                node.invalidate();
            }, "Create " + node.getPathString() + ".@" + attribute + ": " + value));
        }
        else if (event.getType().equals(XsdTreeNode.ACTIVATION_CHANGED))
        {
            Object[] content = (Object[]) event.getContent();
            XsdTreeNode node = (XsdTreeNode) content[0];
            boolean activated = (boolean) content[1];
            add(new SubActionRunnable(() ->
            {
                node.active = !activated;
                node.fireEvent(XsdTreeNode.ACTIVATION_CHANGED, new Object[] {node, !activated});
            }, () ->
            {
                node.active = activated;
                node.fireEvent(XsdTreeNode.ACTIVATION_CHANGED, new Object[] {node, activated});
            }, "Activation " + node.getPathString() + " " + activated));
        }
        else if (event.getType().equals(XsdTreeNode.OPTION_CHANGED))
        {
            Object[] content = (Object[]) event.getContent();
            XsdTreeNode node = (XsdTreeNode) content[1];
            XsdTreeNode previous = (XsdTreeNode) content[2];
            if (previous != null)
            {
                add(new SubActionRunnable(() ->
                {
                    node.setOption(previous); // invokes event
                }, () ->
                {
                    previous.setOption(node); // invokes event
                }, "Set option " + node.getPathString()));
            }
        }
        else if (event.getType().equals(XsdTreeNode.MOVED))
        {
            Object[] content = (Object[]) event.getContent();
            XsdTreeNode node = (XsdTreeNode) content[0];
            int oldIndex = (int) content[1];
            int newIndex = (int) content[2];
            add(new SubActionRunnable(() ->
            {
                node.parent.children.remove(node);
                node.parent.children.add(oldIndex, node);
                node.fireEvent(XsdTreeNode.MOVED, new Object[] {node, newIndex, oldIndex});
            }, () ->
            {
                node.parent.children.remove(node);
                node.parent.children.add(newIndex, node);
                node.fireEvent(XsdTreeNode.MOVED, new Object[] {node, oldIndex, newIndex});
            }, "Move " + node.getPathString()));

        }
    }

    /**
     * Sets the node to show in the tree after the action.
     * @param node XsdTreeNode; node to show in the tree after the action.
     */
    public void setPostActionShowNode(final XsdTreeNode node)
    {
        this.queue.get(this.cursor).postActionShowNode = node;
    }

    /**
     * Interface for any sub-action reflecting any change.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    private interface SubAction
    {
        /**
         * Undo the change.
         */
        void undo();

        /**
         * Redo the change.
         */
        void redo();
    }

    /**
     * Implements {@code SubAction} using two {@code Runnable}'s, definable as an lambda expression.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    private static class SubActionRunnable implements SubAction
    {
        /** Undo runnable. */
        private Runnable undo;

        /** Redo runnable. */
        private Runnable redo;

        /** String representation of this sub action. */
        private String string;

        /**
         * Constructor.
         * @param undo Runnable; undo runnable.
         * @param redo Runnable; redo runnable.
         * @param string String; string representation of this sub action.
         */
        public SubActionRunnable(final Runnable undo, final Runnable redo, final String string)
        {
            this.undo = undo;
            this.redo = redo;
            this.string = string;
        }

        /** {@inheritDoc} */
        @Override
        public void undo()
        {
            this.undo.run();
        }

        /** {@inheritDoc} */
        @Override
        public void redo()
        {
            this.redo.run();
        }

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return this.string;
        }
    }

    /**
     * Class that groups information around an action.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    private class Action
    {
        /** Name of the action, as presented with the undo/redo buttons. */
        final ActionType type;

        /** Queue of sub actions. */
        final Deque<SubAction> subActions;

        /** Node involved in the action. */
        final XsdTreeNode node;

        /** Parent node of the node involved in the action. */
        final XsdTreeNode parent;

        /** Attribute for an attribute change, {@code null} otherwise. */
        final String attribute;

        /** Node to gain focus after the action. */
        XsdTreeNode postActionShowNode;

        /**
         * Constructor.
         * @param type ActionType; type of the action, as presented with the undo/redo buttons.
         * @param subActions Deque&lt;SubAction&gt;; queue of sub actions.
         * @param node XsdTreeNode; node involved in the action.
         * @param parent XsdTreeNode; parent node of the node involved in the action.
         * @param attribute String; attribute for an attribute change, {@code null} otherwise.
         */
        public Action(final ActionType type, final Deque<SubAction> subActions, final XsdTreeNode node,
                final XsdTreeNode parent, final String attribute)
        {
            this.type = type;
            this.subActions = subActions;
            this.node = node;
            this.parent = parent;
            this.attribute = attribute;
            this.postActionShowNode = node;
        }
    }

    /**
     * Type of actions for undo.
     * <p>
     * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public enum ActionType
    {
        /** Node activated. */
        ACTIVATE,

        /** Node added. */
        ADD,

        /** Attribute changed. */
        ATTRIBUTE_CHANGE,

        /** Cut. */
        CUT,

        /** Node duplicated. */
        DUPLICATE,

        /** Id changed. */
        ID_CHANGE,

        /** INSERT. */
        INSERT,

        /** Node moved. */
        MOVE,

        /** Option set. */
        OPTION,

        /** Paste. */
        PASTE,

        /** Node removed. */
        REMOVE,

        /** Node value changed. */
        VALUE_CHANGE,

        /** Action on node, by custom decoration. */
        ACTION;

        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return name().toLowerCase().replace("_", " ");
        }
    }

}
