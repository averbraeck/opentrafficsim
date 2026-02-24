package org.opentrafficsim.editor;

import java.awt.event.ActionEvent;
import java.util.LinkedList;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

/**
 * Navigation through coupled nodes.
 * <p>
 * Copyright (c) 2026-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class Navigation
{

    /** OTS editor. */
    private final OtsEditor editor;

    /** Maximum number of navigation steps in the list. */
    private final int maxNavigate;

    /** Candidate keyref node that was coupled from to a key node, may be {@code null}. */
    private XsdTreeNode candidateBackNode;

    /** Keyref node that was coupled from to a key node, may be {@code null}. */
    private final LinkedList<XsdTreeNode> backNode = new LinkedList<>();

    /** Candidate attribute of back node referring to coupled node, may be {@code null}. */
    private String candidateBackAttribute;

    /** Attribute of back node referring to coupled node, may be {@code null}. */
    private final LinkedList<String> backAttribute = new LinkedList<>();

    /** Key node that is coupled to from a keyref node, may be {@code null}. */
    private XsdTreeNode coupledNode;

    /**
     * Constructor.
     * @param editor editor
     * @param maxNavigate maximum number of navigation steps
     */
    public Navigation(final OtsEditor editor, final int maxNavigate)
    {
        this.editor = editor;
        this.maxNavigate = maxNavigate;
        clear();
    }

    /**
     * Clear navigation list.
     */
    public void clear()
    {
        this.goBackAction.setEnabled(false);
        this.goBackAction.putValue(Action.NAME, "Go back");
        this.goToCoupledNodeAction.setEnabled(false);
        this.goToCoupledNodeAction.putValue(Action.NAME, "Go to coupled item");
    }

    /**
     * Sets coupled node from user action, i.e. the node that contains the key value to which a user selected node with keyref
     * refers to.
     * @param toNode key node that is coupled to from a keyref node, may be {@code null}.
     * @param fromNode keyref node that is coupled from to a key node, may be {@code null}.
     * @param fromAttribute attribute in keyref node that refers to coupled node, may be {@code null}.
     */
    public void setCoupledNode(final XsdTreeNode toNode, final XsdTreeNode fromNode, final String fromAttribute)
    {
        if (toNode == null)
        {
            this.goToCoupledNodeAction.setEnabled(false);
            this.goToCoupledNodeAction.putValue(Action.NAME, "Go to coupled item");
        }
        else
        {
            this.goToCoupledNodeAction.setEnabled(true);
            this.goToCoupledNodeAction.putValue(Action.NAME,
                    "Go to " + (fromAttribute != null ? fromNode.getAttributeValue(fromAttribute)
                            : (fromNode.isIdentifiable() ? fromNode.getId() : fromNode.getValue())));
        }
        this.coupledNode = toNode;
        this.candidateBackNode = fromNode;
        this.candidateBackAttribute = fromAttribute;
    }

    /**
     * Returns the go-back action.
     * @return go-back action
     */
    public Action getGoBackAction()
    {
        return this.goBackAction;
    }

    /** Go-back action. */
    @SuppressWarnings("serial")
    private final Action goBackAction = new AbstractAction()
    {
        {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("pressed F3"));
        }

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            Navigation.this.editor.show(Navigation.this.backNode.pollLast(), Navigation.this.backAttribute.pollLast());
            if (Navigation.this.backNode.isEmpty())
            {
                putValue(NAME, "Go back");
                setEnabled(false);
            }
            else
            {
                XsdTreeNode back = Navigation.this.backNode.peekLast();
                Navigation.this.goBackAction.putValue(NAME,
                        "Go back to " + back.getNodeName() + (back.isIdentifiable() ? " " + back.getId() : ""));
                Navigation.this.goBackAction.setEnabled(true);
            }
        }
    };

    /**
     * Returns go-to-coupled-item action.
     * @return go-to-coupled-item action
     */
    public Action getGotoCoupledNodeAction()
    {
        return this.goToCoupledNodeAction;
    }

    /** Go-to-coupled-item action. */
    @SuppressWarnings("serial")
    private final Action goToCoupledNodeAction = new AbstractAction()
    {
        {
            putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("pressed F4"));
        }

        @Override
        public void actionPerformed(final ActionEvent e)
        {
            if (Navigation.this.coupledNode != null)
            {
                Navigation.this.backNode.add(Navigation.this.candidateBackNode);
                Navigation.this.backAttribute.add(Navigation.this.candidateBackAttribute);
                while (Navigation.this.backNode.size() > Navigation.this.maxNavigate)
                {
                    Navigation.this.backNode.remove();
                    Navigation.this.backAttribute.remove();
                }
                XsdTreeNode back = Navigation.this.backNode.peekLast();
                Navigation.this.goBackAction.putValue(NAME,
                        "Go back to " + back.getNodeName() + (back.isIdentifiable() ? " " + back.getId() : ""));
                Navigation.this.goBackAction.setEnabled(Navigation.this.backNode.peekLast() != null);
                Navigation.this.editor.show(Navigation.this.coupledNode, null);
            }
        }
    };

}
