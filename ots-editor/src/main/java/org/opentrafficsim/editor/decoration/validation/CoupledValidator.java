package org.opentrafficsim.editor.decoration.validation;

import org.opentrafficsim.editor.XsdTreeNode;

/**
 * Coupled validators can return the node to which a node attribute or value is coupled. This is e.g. a LinkType node for the
 * LinkType attribute of a Link node.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface CoupledValidator extends ValueValidator
{
    
    /**
     * Returns the coupled node. This is e.g. a LinkType node for the LinkType attribute of a Link node.
     * @param node XsdTreeNode; node with attribute or value that is bounded by this validator.
     * @return XsdTreeNode; node to which the given node is coupled.
     */
    public XsdTreeNode getCoupledKeyrefNode(final XsdTreeNode node);
    
}
