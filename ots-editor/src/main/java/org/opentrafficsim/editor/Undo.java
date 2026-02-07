package org.opentrafficsim.editor;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.function.Consumer;

import javax.swing.AbstractButton;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.editor.decoration.validation.CoupledValidator;

/**
 * Undo unit for the OTS editor. This class stores an internal queue of actions. Changes to XsdTreeNodes should be grouped per
 * single user input in an action. All actions need to be initiated externally using {@code startAction()}. This class will
 * itself listen to all relevant changes in the tree and add incoming sub-actions under the started action.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class Undo implements EventListener
{

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
    private boolean ignoreChanges = false;

    /** Allocated next action to make concrete on first actual change. */
    private Action nextAction;

    /**
     * Constructor.
     * @param editor editor.
     * @param undoItem undo GUI item.
     * @param redoItem redo GUI item.
     */
    public Undo(final OtsEditor editor, final AbstractButton undoItem, final AbstractButton redoItem)
    {
        this.editor = editor;
        this.undoItem = undoItem;
        this.redoItem = redoItem;
        this.undoItem.setEnabled(false);
        this.redoItem.setEnabled(false);
        editor.addListener(this, OtsEditor.NEW_FILE);
    }

    /**
     * Clears the entire queue, suitable for when a new tree is loaded. Also sets ignore changes to false.
     */
    public void clear()
    {
        this.ignoreChanges = false;
        this.currentSet = null;
        this.cursor = -1;
        this.queue = new LinkedList<>();
        this.undoItem.setEnabled(false);
        this.redoItem.setEnabled(false);
    }

    /**
     * Tells the undo unit to ignore all changes. Reset this by calling {@code clear()}. Useful during file loading.
     * @param ignore ignore changes.
     */
    public void setIgnoreChanges(final boolean ignore)
    {
        this.ignoreChanges = ignore;
    }

    /**
     * Starts a new action, which groups all sub-actions until a new action is started. This method can be called without being
     * sure concrete changes will be made. Internal listeners listen to all changes and will combine them in to one undo action,
     * up to the point the next action is started with this method. If no actual changes were made in between, the former start
     * of an action does not result in anything the user can undo or redo. When the user has stepped back a few undo actions,
     * and then makes a new change, rolled back undo steps can no longer be redone. Clearing rolled back undo steps is performed
     * lazily on the first concrete change by a sub-action. Starting a new action does not clear rolled back undo steps by
     * itself.
     * @param type action type.
     * @param node node on which the action is applied, i.e. node that should be selected on undo/redo.
     * @param attribute attribute name, may be {@code null} for actions that are not an attribute value change.
     */
    public void startAction(final ActionType type, final XsdTreeNode node, final String attribute)
    {
        if (this.ignoreChanges)
        {
            return;
        }
        // allocate a next action with the right type, nodes and attribute, but with an empty set of sub-actions for now
        // this does not yet represent an actual undoable action until any sub-action is added to it
        this.nextAction = new Action(type, new ArrayDeque<>(), node, node.parent, attribute);
    }

    /**
     * Adds sub-action to current action.
     * @param subAction sub-action.
     */
    private void add(final SubAction subAction)
    {
        if (this.ignoreChanges)
        {
            return;
        }
        // make allocated next action a concrete next action in the queue
        if (this.nextAction != null)
        {
            // remove any possible redos fresher in the queue than our current pointer (i.e. rolled back undo steps)
            while (this.cursor < this.queue.size() - 1)
            {
                this.queue.pollLast();
            }
            this.currentSet = this.nextAction.subActions;
            this.queue.add(this.nextAction);
            while (this.queue.size() > MAX_UNDO)
            {
                this.queue.pollFirst();
            }
            this.nextAction = null;
            this.cursor = this.queue.size() - 1;
            updateButtons();
        }
        Throw.when(this.currentSet == null, IllegalStateException.class,
                "Adding undo action without having called startUndoAction()");
        this.currentSet.add(subAction);
    }

    /**
     * Returns whether an undo is available.
     * @return whether an undo is available.
     */
    public boolean canUndo()
    {
        return this.cursor >= 0;
    }

    /**
     * Returns whether a redo is available.
     * @return whether a redo is available.
     */
    public boolean canRedo()
    {
        return this.cursor < this.queue.size() - 1;
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
        if (action.type.equals(ActionType.ACTIVATE))
        {
            this.editor.collapse(action.node);
        }
        // In case of Java 21: action.subActions.reversed().forEach((a) -> a.undo());
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
        action.subActions.forEach((a) -> a.redo());
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

    @Override
    @SuppressWarnings("methodlength")
    public void notify(final Event event)
    {
        listenAndUnlisten(event);

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
            add(new SubAction(() ->
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
            add(new SubAction(() ->
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
            add(new SubAction(() ->
            {
                node.setValue((String) content[1]); // invokes event
            }, () ->
            {
                node.setValue(value); // invokes event
            }, "Change " + node.getPathString() + " value: " + value));
        }
        else if (event.getType().equals(XsdTreeNode.ATTRIBUTE_CHANGED))
        {
            Object[] content = (Object[]) event.getContent();
            XsdTreeNode node = (XsdTreeNode) content[0];
            String attribute = (String) content[1];
            String prevValue = (String) content[2];
            String value = node.getAttributeValue(attribute);
            // for include nodes, setAttributeValue will trigger addition and removal of nodes, we can ignore these events
            if (node.xsdNode.equals(XiIncludeNode.XI_INCLUDE))
            {
                this.currentSet.clear();
            }
            add(new SubAction(() ->
            {
                node.setAttributeValue(attribute, prevValue); // invokes event
            }, () ->
            {
                node.setAttributeValue(attribute, value); // invokes event
            }, "Create " + node.getPathString() + ".@" + attribute + ": " + value));
        }
        else if (event.getType().equals(XsdTreeNode.ACTIVATION_CHANGED))
        {
            Object[] content = (Object[]) event.getContent();
            XsdTreeNode node = (XsdTreeNode) content[0];
            boolean activated = (boolean) content[1];
            add(new SubAction(() ->
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
                add(new SubAction(() ->
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
            add(new SubAction(() ->
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
        else if (event.getType().equals(CoupledValidator.COUPLING))
        {
            if (this.currentSet == null)
            {
                return; // We can ignore couplings created by node expansion after loading a file
            }
            Object[] content = (Object[]) event.getContent();
            CoupledValidator validator = (CoupledValidator) content[0];
            XsdTreeNode fromNode = (XsdTreeNode) content[1];
            XsdTreeNode toNode = (XsdTreeNode) content[2];
            XsdTreeNode prevToNode = (XsdTreeNode) content[3];
            Consumer<XsdTreeNode> consumer = (node) -> // this works either way, towards prevToNode (undo) or toNode (redo)
            {
                if (node == null)
                {
                    validator.removeCoupling(fromNode);
                }
                else
                {
                    validator.addCoupling(fromNode, node);
                }
                fromNode.invalidate();
            };
            add(new SubAction(() -> consumer.accept(prevToNode), () -> consumer.accept(toNode),
                    "Coupling " + fromNode.getNodeName()));
        }
    }

    /**
     * Listen and un-listen to all possible changes.
     * @param event event
     */
    private void listenAndUnlisten(final Event event)
    {
        if (event.getType().equals(OtsEditor.NEW_FILE))
        {
            XsdTreeNodeRoot root = (XsdTreeNodeRoot) event.getContent();
            root.addListener(this, XsdTreeNodeRoot.NODE_CREATED);
            root.addListener(this, XsdTreeNodeRoot.NODE_REMOVED);
            root.addListener(this, CoupledValidator.COUPLING);
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
    }

    /**
     * Sets the node to show in the tree after the action. This is for example useful to set the selection on the duplicate of a
     * duplicated node when redoing the duplication. Note that the node of the action that is otherwise shown would be the
     * duplicated node, rather than the duplicate.
     * @param node node to show in the tree after the action.
     */
    public void setPostActionShowNode(final XsdTreeNode node)
    {
        this.queue.get(this.cursor).postActionShowNode = node;
    }

    /**
     * Class that groups information around an action.
     */
    private class Action
    {
        // can't be a record due to mutable postActionShowNode

        /** Name of the action, as presented with the undo/redo buttons. */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        final ActionType type;

        /** Queue of sub-actions. */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        final Deque<SubAction> subActions;

        /** Node involved in the action. */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        final XsdTreeNode node;

        /** Parent node of the node involved in the action. */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        final XsdTreeNode parent;

        /** Attribute for an attribute change, {@code null} otherwise. */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        final String attribute;

        /** Node to gain focus after the action. */
        @SuppressWarnings("checkstyle:visibilitymodifier")
        XsdTreeNode postActionShowNode;

        /**
         * Constructor.
         * @param type type of the action, as presented with the undo/redo buttons.
         * @param subActions queue of sub-actions.
         * @param node node involved in the action.
         * @param parent parent node of the node involved in the action.
         * @param attribute attribute for an attribute change, {@code null} otherwise.
         */
        Action(final ActionType type, final Deque<SubAction> subActions, final XsdTreeNode node, final XsdTreeNode parent,
                final String attribute)
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

        @Override
        public String toString()
        {
            return name().toLowerCase().replace("_", " ");
        }
    }

    /**
     * Sub-action defined by using two {@code Runnable}'s, definable as an lambda expression.
     */
    private static class SubAction
    {
        /** Undo runnable. */
        private Runnable undo;

        /** Redo runnable. */
        private Runnable redo;

        /** String representation of this sub-action. */
        private String string;

        /**
         * Constructor.
         * @param undo undo runnable.
         * @param redo redo runnable.
         * @param string string representation of this sub-action.
         */
        SubAction(final Runnable undo, final Runnable redo, final String string)
        {
            this.undo = undo;
            this.redo = redo;
            this.string = string;
        }

        /**
         * Undo the sub-action.
         */
        public void undo()
        {
            this.undo.run();
        }

        /**
         * Redo the sub-action.
         */
        public void redo()
        {
            this.redo.run();
        }

        @Override
        public String toString()
        {
            return this.string;
        }
    }

}
