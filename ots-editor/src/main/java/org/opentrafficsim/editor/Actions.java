package org.opentrafficsim.editor;

import java.awt.event.ActionEvent;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.TreePath;

import org.opentrafficsim.editor.DocumentReader.NodeAnnotation;

import de.javagl.treetable.JTreeTable;

/**
 * This class houses all the actions that are made available on the editor. They can be bound to a component with a key-stroke
 * using {@link Actions#bind(JComponent, int, Action)}, or they can be programmatically invoked using
 * {@link Actions#invoke(Action)}.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
@SuppressWarnings("serial") // all the defined AbstractAction's want a serialVersionUID, let's ignore to prevent code cluttering
public class Actions
{

    /** Editor. */
    private final OtsEditor editor;

    /** Tree table. */
    private JTreeTable treeTable;

    /** Attributes table. */
    private final JTable attributesTable;

    /** Actions (methods) to perform on nodes. */
    private NodeActions nodeActions;

    /**
     * Constructor.
     * @param editor editor
     * @param attributesTable attributes table
     */
    public Actions(final OtsEditor editor, final JTable attributesTable)
    {
        this.editor = editor;
        this.attributesTable = attributesTable;
    }

    /**
     * Sets a new tree table as a new file is created or loaded.
     * @param treeTable tree table
     */
    public void setTreeTable(final JTreeTable treeTable)
    {
        this.treeTable = treeTable;
        this.nodeActions = new NodeActions(this.editor, this.treeTable);
    }

    /**
     * Returns the node actions.
     * @return node actions
     */
    public NodeActions getNodeActions()
    {
        return this.nodeActions;
    }

    // ----- Static helper methods. ----- //

    /**
     * Binds a key stroke to an action on the component. The key stroke is taken from the action's
     * {@code Action.ACCELERATOR_KEY} value.
     * @param component component
     * @param state int, for example JComponent.WHEN_FOCUSED
     * @param action action
     */
    public static void bind(final JComponent component, final int state, final Action action)
    {
        // the action itself is used as a consistent and unique key that the input map and action map share
        component.getInputMap(state).put((KeyStroke) action.getValue(Action.ACCELERATOR_KEY), action);
        component.getActionMap().put(action, action);
    }

    /**
     * Invoke action programmatically. If the action is disabled it will be ignored.
     * @param action action to invoke
     */
    public static void invoke(final Action action)
    {
        if (action != null && action.isEnabled())
        {
            action.actionPerformed(new ActionEvent(action, ActionEvent.ACTION_PERFORMED, null));
        }
    }

    /**
     * Returns a document listener that executes the runnable on any change.
     * @param runnable runnable to run on any change
     * @return document listener that executes the runnable on any change
     */
    public static DocumentListener documentListener(final Runnable runnable)
    {
        return new DocumentListener()
        {
            @Override
            public void insertUpdate(final DocumentEvent e)
            {
                runnable.run();
            }

            @Override
            public void removeUpdate(final DocumentEvent e)
            {
                runnable.run();
            }

            @Override
            public void changedUpdate(final DocumentEvent e)
            {
                // this is only used for non-plain text changes
            }
        };
    }

    // ----- Helper methods for the actions. ----- //

    /**
     * Returns the selected tree node.
     * @return selected tree node
     */
    private XsdTreeNode getSelectedTreeNode()
    {
        return (XsdTreeNode) this.treeTable.getTree().getSelectionPath().getLastPathComponent();
    }

    /**
     * Returns the selected attributes node.
     * @return selected attributes node
     */
    private XsdTreeNode getSelectedAttributeNode()
    {
        // this should always be the same as the selected tree node, but is a bit more robust for attribute events
        return ((AttributesTableModel) Actions.this.attributesTable.getModel()).getNode();
    }

    // ----- Actions; pairs of methods to obtain them and private final fields that store them. ----- //

    /**
     * Action to show the description of a tree node. Ignored of the node does not have a description.
     * @return action to show the description of a tree node
     */
    public Action showTreeNodeDescription()
    {
        return this.showTreeNodeDescriptionAction;
    }

    /** Action to show the description of a tree node. */
    private final Action showTreeNodeDescriptionAction = new AbstractAction("Description...")
    {
        {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("pressed F1"));
        }

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            XsdTreeNode node = getSelectedTreeNode();
            node.getDescription().ifPresent((d) -> Actions.this.editor.dialogs().showDescription(d, node.getNodeName()));
        }
    };

    /**
     * Action to show the invalid message on a tree node. Ignored if the node is valid.
     * @return action to show the invalid message on a tree node
     */
    public Action showTreeNodeInvalid()
    {
        return this.showTreeNodeInvalidAction;
    }

    /** Action to show the invalid message on a tree node. */
    private final Action showTreeNodeInvalidAction = new AbstractAction("Invalid...")
    {
        {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("pressed F2"));
        }

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            XsdTreeNode node = getSelectedTreeNode();
            String status = node.isSelfValid() ? null : node.reportInvalidNode()
                    .orElseGet(() -> node.reportInvalidValue().orElseGet(() -> node.reportInvalidId().orElse(null)));
            if (status != null)
            {
                Actions.this.editor.dialogs().showInvalidMessage(status, node.getNodeName());
            }
        }
    };

    /**
     * Action to show the description of a tree node. Ignored of the node does not have a description.
     * @return action to show the description of a tree node
     */
    public Action showAttributeDescription()
    {
        return this.showAttributeDescriptionAction;
    }

    /** Action to show the description of an attribute. */
    private final Action showAttributeDescriptionAction = new AbstractAction("Description...")
    {
        {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("pressed F1"));
        }

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            XsdTreeNode node = getSelectedAttributeNode();
            int attribute = Actions.this.attributesTable.getSelectedRow();
            NodeAnnotation.DESCRIPTION.get(node.getAttributeNode(attribute)).ifPresent(
                    (d) -> Actions.this.editor.dialogs().showDescription(d, node.getAttributeNameByIndex(attribute)));
        }
    };

    /**
     * Action to show the invalid message on an attribute. Ignored if the attribute is valid.
     * @return action to show the invalid message on an attribute
     */
    public Action showAttributeInvalid()
    {
        return this.showAttributeInvalidAction;
    }

    /** Action to show the invalid message on an attribute. */
    private final Action showAttributeInvalidAction = new AbstractAction("Invalid...")
    {
        {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("pressed F2"));
        }

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            XsdTreeNode node = getSelectedAttributeNode();
            int attribute = Actions.this.attributesTable.getSelectedRow();
            node.reportInvalidAttributeValue(attribute).ifPresent(
                    (m) -> Actions.this.editor.dialogs().showInvalidMessage(m, node.getAttributeNameByIndex(attribute)));
        }
    };

    /**
     * Action to add node.
     * @return action to add node
     */
    public Action addNode()
    {
        return this.addNodeAction;
    }

    /** Action to add node. */
    private final Action addNodeAction = new AbstractAction("Add")
    {
        {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl W"));
        }

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            XsdTreeNode node = getSelectedTreeNode();
            if (node.isAddable())
            {
                getNodeActions().add(node);
            }
        }
    };

    /**
     * Action to duplicate node.
     * @return action to duplicate node
     */
    public Action duplicateNode()
    {
        return this.duplicateNodeAction;
    }

    /** Action to duplicate node. */
    private final Action duplicateNodeAction = new AbstractAction("Duplicate")
    {
        {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl D"));
        }

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            XsdTreeNode node = getSelectedTreeNode();
            if (node.isAddable())
            {
                getNodeActions().duplicate(node);
            }
        }
    };

    /**
     * Action to delete node.
     * @return action to delete node
     */
    public Action deleteNode()
    {
        return this.deleteNodeAction;
    }

    /** Action to delete node. */
    private final Action deleteNodeAction = new AbstractAction("Remove")
    {
        {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("DELETE"));
        }

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            XsdTreeNode node = getSelectedTreeNode();
            if (node.isRemovable())
            {
                if (Actions.this.editor.dialogs().confirmNodeRemoval(node))
                {
                    getNodeActions().remove(node);
                }
            }
        }
    };

    /**
     * Action to copy node.
     * @return action to copy node
     */
    public Action copyNode()
    {
        return this.copyNodeAction;
    }

    /** Action to copy node. */
    private final Action copyNodeAction = new AbstractAction("Copy")
    {
        {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl C"));
        }

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            XsdTreeNode node = getSelectedTreeNode();
            if (node.isActive() && (node.isRemovable() || node.isAddable()))
            {
                getNodeActions().copy(node);
            }
        }
    };

    /**
     * Action to cut node.
     * @return action to cut node
     */
    public Action cutNode()
    {
        return this.cutNodeAction;
    }

    /** Action to cut node. */
    private final Action cutNodeAction = new AbstractAction("Cut")
    {
        {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl X"));
        }

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            XsdTreeNode node = getSelectedTreeNode();
            if (node.isActive() && node.isRemovable())
            {
                getNodeActions().cut(node);
            }
        }
    };

    /**
     * Action to insert node.
     * @return action to insert node
     */
    public Action insertNode()
    {
        return this.insertNodeAction;
    }

    /** Action to insert node. */
    private final Action insertNodeAction = new AbstractAction("Insert")
    {
        {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("INSERT"));
        }

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            XsdTreeNode node = getSelectedTreeNode();
            if (Actions.this.editor.getClipboard() != null && node.canContain(Actions.this.editor.getClipboard())
                    && node.isActive() && node.isAddable())
            {
                getNodeActions().insert(node);
            }
        }
    };

    /**
     * Action to paste node.
     * @return action to paste node
     */
    public Action pasteNode()
    {
        return this.pasteNodeAction;
    }

    /** Action to paste node. */
    private final Action pasteNodeAction = new AbstractAction("Paste")
    {
        {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl V"));
        }

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            XsdTreeNode node = getSelectedTreeNode();
            if (Actions.this.editor.getClipboard() != null && node.canContain(Actions.this.editor.getClipboard())
                    && (!node.isActive() || node.isAddable()))
            {
                getNodeActions().paste(node);
            }
        }
    };

    /**
     * Action to revolve node.
     * @return action to revolve node
     */
    public Action revolveNode()
    {
        return this.revolveNodeAction;
    }

    /** Action to revolve node. */
    private final Action revolveNodeAction = new AbstractAction("Revolve option")
    {
        {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl R"));
        }

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            XsdTreeNode node = getSelectedTreeNode();
            List<XsdOption> options = node.getOptions();
            if (node.isChoice() && options.size() > 1)
            {
                getNodeActions().revolveOption(node, options);
            }
        }
    };

    /**
     * Action to collapse all.
     * @return action to collapse all
     */
    public Action collapseAll()
    {
        return this.collapseAllAction;
    }

    /** Action to collapse all. */
    private final Action collapseAllAction = new AbstractAction("Collapse all")
    {
        @Override
        public void actionPerformed(final ActionEvent e)
        {
            for (int i = Actions.this.treeTable.getTree().getRowCount() - 1; i > 0; i--)
            {
                Actions.this.treeTable.getTree().collapseRow(i);
            }
        }
    };

    /**
     * Action to expand or collapse node.
     * @return action to expand or collapse node
     */
    public Action expandOrCollapseNode()
    {
        return this.expandOrCollapseNodeAction;
    }

    /** Action to collapse node. */
    private final Action expandOrCollapseNodeAction = new AbstractAction()
    {
        // name is set by XsdTreeListener as it depends on node expanded status
        {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl E"));
        }

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            TreePath path = Actions.this.treeTable.getTree().getSelectionPath();
            XsdTreeNode node = (XsdTreeNode) path.getLastPathComponent();
            boolean expanded = Actions.this.treeTable.getTree().isExpanded(path);
            getNodeActions().expand(node, path, expanded);
        }
    };

    /**
     * Action to move node up.
     * @return action to move node up
     */
    public Action moveNodeUp()
    {
        return this.moveNodeUpAction;
    }

    /** Action to move node up. */
    private final Action moveNodeUpAction = new AbstractAction("Move up")
    {
        {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl UP"));
        }

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            XsdTreeNode node = getSelectedTreeNode();
            if (node.canMoveUp())
            {
                getNodeActions().move(node, -1);
            }
        }
    };

    /**
     * Action to move node down.
     * @return action to move node down
     */
    public Action moveNodeDown()
    {
        return this.moveNodeDownAction;
    }

    /** Action to move node down. */
    private final Action moveNodeDownAction = new AbstractAction("Move down")
    {
        {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("ctrl DOWN"));
        }

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            XsdTreeNode node = getSelectedTreeNode();
            if (node.canMoveDown())
            {
                getNodeActions().move(node, 1);
            }
        }
    };

}
