package org.opentrafficsim.editor;

/**
 * Wraps a scenario node as an item in the dropdown menu.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 * @param scenarioNode node of the scenario
 */
public record ScenarioWrapper(XsdTreeNode scenarioNode)
{

    /**
     * Returns whether this wraps the given node.
     * @param node node.
     * @return whether this wraps the given node.
     */
    public boolean isScenario(final XsdTreeNode node)
    {
        return node.equals(this.scenarioNode);
    }

    /**
     * Returns the scenario node.
     * @return scenario node.
     */
    public XsdTreeNode scenarioNode()
    {
        return this.scenarioNode;
    }

    @Override
    public String toString()
    {
        if (this.scenarioNode == null)
        {
            return "(Default)";
        }
        String id = this.scenarioNode.getId();
        return id == null ? "(no id)" : id;
    }

}
