package org.opentrafficsim.editor.decoration.validation;

import java.util.Map;
import java.util.Objects;

import org.djutils.event.EventType;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
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
 * Couplings should be returned in implementing classes by returning a contained Map in {@code getCoupling()}. The implementing
 * class should not change this Map, but should invoke the methods in this interface. Both {@code addCoupling()} and
 * {@code removeCoupling()} trigger events for a consistent undo queue, and should therefore be used to add or remove a
 * coupling. This information is required in the undo queue because e.g. changing an id of a key may make it coupled for some
 * keyref that happened to point to that value. If the id change on the key is then undone, the now coupled keyref should
 * <b>not</b> change the id in lock step. Therefore, the coupling should first be undone, before the old id value is reset on
 * the key.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public interface CoupledValidator extends ValueValidator
{

    /** Coupling event type. */
    EventType COUPLING = new EventType(new MetaData("COUPLING", "Coupling is added or removed",
            new ObjectDescriptor("Validator", "Validator with the coupling", CoupledValidator.class),
            new ObjectDescriptor("FromNode", "From node of coupling", XsdTreeNode.class),
            new ObjectDescriptor("ToNode", "To node of coupling, null for a removed coupling", XsdTreeNode.class),
            new ObjectDescriptor("PrevToNode", "Previous to node, can be null", XsdTreeNode.class)));

    /**
     * Returns a map with all node couplings. This method is intended for subclasses and CoupledValidator itself only.
     * @return a map with all node couplings
     */
    Map<XsdTreeNode, XsdTreeNode> getCouplings();

    /**
     * Returns the coupled node. This is e.g. a LinkType node for the LinkType attribute of a Link node.
     * @param node node with attribute or value that is bounded by this validator.
     * @return node to which the given node is coupled.
     */
    default XsdTreeNode getCoupledNode(final XsdTreeNode node)
    {
        return getCouplings().get(node);
    }

    /**
     * Add coupling from one node to another. Makes the root throw a COUPLING event.
     * @param fromNode from node
     * @param toNode to node
     */
    default void addCoupling(final XsdTreeNode fromNode, final XsdTreeNode toNode)
    {
        XsdTreeNode prevToNode = getCouplings().put(fromNode, toNode);
        if (!Objects.equals(toNode, prevToNode))
        {
            fromNode.getRoot().fireEvent(COUPLING, new Object[] {this, fromNode, toNode, prevToNode});
        }
    }

    /**
     * Remove coupling of node. Makes the root throw a COUPLING event (with {@code null} for toNode value).
     * @param fromNode from node of coupling
     */
    default void removeCoupling(final XsdTreeNode fromNode)
    {
        XsdTreeNode prevToNode = getCouplings().remove(fromNode);
        if (prevToNode != null)
        {
            fromNode.getRoot().fireEvent(COUPLING, new Object[] {this, fromNode, null, prevToNode});
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

}
