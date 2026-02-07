package org.opentrafficsim.editor.listeners;

import javax.swing.JComboBox;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.ScenarioWrapper;
import org.opentrafficsim.editor.XsdPaths;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeRoot;

/**
 * Listener to all changes. This listener:
 * <ul>
 * <li>Notifies the user subtly that changes were made by adding a "*" in the title bar.</li>
 * <li>Keeps the scenario drop down list up to date.</li>
 * </ul>
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ChangesListener implements EventListener
{

    /** Editor. */
    private final OtsEditor editor;

    /** Scenario selection. */
    private final JComboBox<ScenarioWrapper> scenario;

    /** Sub-listener to update scenario name as the user types. */
    private final EventListener scenarioNameListener;

    /**
     * Constructor.
     * @param editor editor.
     * @param scenario scenario drop down list.
     */
    public ChangesListener(final OtsEditor editor, final JComboBox<ScenarioWrapper> scenario)
    {
        this.editor = editor;
        this.scenario = scenario;
        this.scenarioNameListener = (event) -> this.scenario.repaint();
    }

    @Override
    public void notify(final Event event)
    {
        if (event.getType().equals(XsdTreeNodeRoot.NODE_CREATED))
        {
            XsdTreeNode node = (XsdTreeNode) ((Object[]) event.getContent())[0];
            node.addListener(this, XsdTreeNode.MOVED);
            node.addListener(this, XsdTreeNode.VALUE_CHANGED);
            node.addListener(this, XsdTreeNode.OPTION_CHANGED);
            node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
            node.addListener(this, XsdTreeNode.ACTIVATION_CHANGED);
            if (node.getPathString().equals(XsdPaths.SCENARIO))
            {
                insertScenario(node);
            }
        }
        else if (event.getType().equals(XsdTreeNodeRoot.NODE_REMOVED))
        {
            XsdTreeNode node = (XsdTreeNode) ((Object[]) event.getContent())[0];
            node.removeListener(this, XsdTreeNode.MOVED);
            node.removeListener(this, XsdTreeNode.VALUE_CHANGED);
            node.removeListener(this, XsdTreeNode.OPTION_CHANGED);
            node.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
            node.removeListener(this, XsdTreeNode.ACTIVATION_CHANGED);
            if (node.getPathString().equals(XsdPaths.SCENARIO))
            {
                removeScenario(node);
            }
        }
        else if (event.getType().equals(XsdTreeNode.MOVED))
        {
            XsdTreeNode node = (XsdTreeNode) ((Object[]) event.getContent())[0];
            if (node.getPathString().equals(XsdPaths.SCENARIO))
            {
                for (int i = 0; i < this.scenario.getItemCount(); i++)
                {
                    ScenarioWrapper scenarioWrapper = this.scenario.getItemAt(i);
                    if (scenarioWrapper.isScenario(node))
                    {
                        ScenarioWrapper selected = this.scenario.getItemAt(this.scenario.getSelectedIndex());
                        this.scenario.removeItemAt(i);
                        this.scenario.insertItemAt(scenarioWrapper, node.getParent().getChildren().indexOf(node));
                        this.scenario.setSelectedItem(selected);
                        break;
                    }
                }
            }
        }
        else if (event.getType().equals(XsdTreeNode.ACTIVATION_CHANGED))
        {
            Object[] content = ((Object[]) event.getContent());
            XsdTreeNode node = (XsdTreeNode) content[0];
            if (node.getPathString().equals(XsdPaths.SCENARIOS))
            {
                boolean activated = (boolean) content[1];
                for (XsdTreeNode child : node.getChildren())
                {
                    if (child.getNodeName().equals("Scenario"))
                    {
                        if (activated)
                        {
                            insertScenario(child);
                        }
                        else
                        {
                            removeScenario(child);
                        }
                    }
                }
            }
        }
        this.editor.setUnsavedChanges(true);
    }

    /**
     * Insert scenario node in drop down list.
     * @param node scenario node Ots.Scenarios.Scenario.
     */
    private void insertScenario(final XsdTreeNode node)
    {
        int index = 0;
        ScenarioWrapper itemOfNode = null;
        for (XsdTreeNode child : node.getParent().getChildren())
        {
            if (child.getPathString().equals(XsdPaths.SCENARIO))
            {
                index++;
                if (child.equals(node))
                {
                    itemOfNode = this.scenario.getItemAt(index);
                    break;
                }
            }
        }
        // inserted at end, or in between (else item already in scenario list)
        if (itemOfNode == null || !itemOfNode.scenarioNode().equals(node))
        {
            this.scenario.insertItemAt(new ScenarioWrapper(node), index);
            node.addListener(this.scenarioNameListener, XsdTreeNode.ATTRIBUTE_CHANGED);
        }
    }

    /**
     * Remove scenario node from drop down list.
     * @param node scenario node Ots.Scenarios.Scenario.
     */
    private void removeScenario(final XsdTreeNode node)
    {
        for (int i = 0; i < this.scenario.getItemCount(); i++)
        {
            if (this.scenario.getItemAt(i).isScenario(node))
            {
                this.scenario.removeItemAt(i);
                break;
            }
        }
        node.removeListener(this.scenarioNameListener, XsdTreeNode.ATTRIBUTE_CHANGED);
    }

}
