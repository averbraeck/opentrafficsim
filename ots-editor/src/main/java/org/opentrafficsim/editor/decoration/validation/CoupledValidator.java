package org.opentrafficsim.editor.decoration.validation;

import org.opentrafficsim.editor.XsdTreeNode;

/**
 * Coupled validators can return the node to which a node attribute or value is coupled. This is e.g. a LinkType node for the
 * LinkType attribute of a Link node. As such, the user can navigate towards the coupled node.
 * <p>
 * Desirable behaviour of any validator implementing this interface is that when a value is changed in a node that is coupled
 * to, the appropriate value that was coupling to it is automatically updated. For example, when a Node id is changed, all links
 * that refer to this node with NodeStart or NodeEnd should update that value. Implementations should setup listeners to do
 * this. If a coupling is made including a null value, e.g. {"A", "B", null} is successfully coupled to {"A", "B", "C"}, then
 * when the value for "C" is changed in the coupled node, the null value should not be updated.
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
     * @param node node with attribute or value that is bounded by this validator.
     * @return node to which the given node is coupled.
     */
    XsdTreeNode getCoupledKeyrefNode(final XsdTreeNode node);

    /**
     * Sets the given value on the attribute of the node, if the current value is not {@code null}. The node is invalidated
     * afterwards. This is used to not change values in keyrefs that match including null values.
     * @param node node.
     * @param attribute attribute.
     * @param value value.
     */
    static void setAttributeIfNotNull(final XsdTreeNode node, final String attribute, final String value)
    {
        String oldValue = node.getAttributeValue(attribute);
        if (oldValue != null && !oldValue.isEmpty())
        {
            node.setAttributeValue(attribute, value);
            node.invalidate();
        }
    }

    /**
     * Sets the given value on the node, if the current value is not {@code null}. The node is invalidated afterwards.
     * @param node node. This is used to not change values in keyrefs that match including null values.
     * @param value value.
     */
    static void setValueIfNotNull(final XsdTreeNode node, final String value)
    {
        String oldValue = node.getValue();
        if (oldValue != null && !oldValue.isEmpty())
        {
            node.setValue(value);
            node.invalidate();
        }
    }

}
