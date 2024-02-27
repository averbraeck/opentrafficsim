package org.opentrafficsim.editor;

/**
 * Wraps a scenario node as an item in the dropdown menu. 
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class ScenarioWrapper
{

    /** Scenario node. */
    private final XsdTreeNode scenarioNode;
    
    /**
     * Constructor.
     * @param scenarioNode XsdTreeNode; node of the scenario.
     */
    public ScenarioWrapper(final XsdTreeNode scenarioNode)
    {
        this.scenarioNode = scenarioNode;
    }

    /**
     * Returns whether this wraps the given node.
     * @param node XsdTreeNode; node.
     * @return boolean; whether this wraps the given node.
     */
    public boolean isScenario(final XsdTreeNode node)
    {
        return node.equals(this.scenarioNode);
    }
    
    /**
     * Returns the scenario node.
     * @return XsdTreeNode; scenario node.
     */
    public XsdTreeNode getScenarioNode()
    {
        return this.scenarioNode;
    }
    
    /** {@inheritDoc} */
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
