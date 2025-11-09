package org.opentrafficsim.editor.decoration.validation;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.djutils.event.Event;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdPaths;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.decoration.AbstractNodeDecoratorRemove;
import org.opentrafficsim.editor.decoration.validation.RoadLayoutElementValidator.LayoutCoupling;
import org.opentrafficsim.editor.decoration.validation.RoadLayoutElementValidator.RoadLayoutElementAttribute;

/**
 * Checks that attribute TrafficLightId can be found on link under attribute Link (with keyref) having the same lane under
 * attribute Lane. This class relies on a {@code RoadLayoutElementValidator} to validate the Lane attribute with the Link
 * attribute, which will invalidate the node if either attribute is changed. This second validator is created in the constructor
 * of this validator.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class TrafficLightValidator extends AbstractNodeDecoratorRemove implements CoupledValidator
{

    /** */
    private static final long serialVersionUID = 20240306L;

    /** All nodes bing validated. */
    private final Set<XsdTreeNode> validatingNodes = new LinkedHashSet<>();

    /** SignalGroup.TrafficLight to Link.TrafficLight coupling. */
    private final Map<XsdTreeNode, XsdTreeNode> coupledNodes = new LinkedHashMap<>();

    /**
     * Constructor.
     * @param editor editor.
     * @param path path.
     */
    public TrafficLightValidator(final OtsEditor editor, final String path)
    {
        super(editor, (n) -> n.getPathString().endsWith(path) || n.getPathString().equals(XsdPaths.TRAFFIC_LIGHT));
        new RoadLayoutElementValidator(editor, path, LayoutCoupling.LINK_ATTRIBUTE, RoadLayoutElementAttribute.LANE);
    }

    @Override
    public void notifyCreated(final XsdTreeNode node)
    {
        if (!node.getPathString().equals(XsdPaths.TRAFFIC_LIGHT))
        {
            // Validated node
            this.validatingNodes.add(node);
            node.addAttributeValidator("TrafficLightId", this);
        }
        // Validated node or traffic light node
        node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
    }

    @Override
    public void notifyRemoved(final XsdTreeNode node)
    {
        this.validatingNodes.remove(node);
        node.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
    }

    @Override
    public void notify(final Event event)
    {
        if (event.getType().equals(XsdTreeNode.ATTRIBUTE_CHANGED))
        {
            Object[] content = (Object[]) event.getContent();
            XsdTreeNode node = (XsdTreeNode) content[0];
            if (node.getPathString().equals(XsdPaths.TRAFFIC_LIGHT))
            {
                // Traffic light node
                if ("Id".equals(content[1]))
                {
                    if (getCouplings().containsValue(node))
                    {
                        // Id changed on coupled node
                        String newId = node.getId();
                        for (Entry<XsdTreeNode, XsdTreeNode> entry : getCouplings().entrySet())
                        {
                            if (entry.getValue().equals(node))
                            {
                                CoupledValidator.setAttributeIfNotNull(entry.getKey(), "TrafficLightId", newId);
                            }
                        }
                    }

                    // Id changed on traffic light, any could couple to this
                    this.validatingNodes.forEach((n) -> n.invalidate());
                }
            }
            else
            {
                // Id change on validated node
                node.invalidate();
            }
        }
        else
        {
            super.notify(event);
        }
    }

    @Override
    public String validate(final XsdTreeNode node)
    {
        String trafficLightId = node.getAttributeValue("TrafficLightId");
        if (trafficLightId != null && !trafficLightId.isEmpty())
        {
            XsdTreeNode linkNode = node.getCoupledNodeAttribute("Link");
            if (linkNode == null)
            {
                removeCoupling(node);
                return "Unable to find traffic light due to invalid Link value.";
            }
            String lane = node.getAttributeValue("Lane");
            if (lane == null)
            {
                removeCoupling(node);
                return "Unable to find traffic light due to missing Lane value.";
            }
            for (XsdTreeNode child : linkNode.getChildren())
            {
                if (child.getNodeName().equals("TrafficLight") && child.isActive()
                        && lane.equals(child.getAttributeValue("Lane")))
                {
                    if (trafficLightId.equals(child.getId()))
                    {
                        addCoupling(node, child);
                        return null;
                    }
                }
            }
            removeCoupling(node);
            return "There is no traffic light with id " + trafficLightId + " on link " + node.getAttributeValue("Link")
                    + " at lane " + lane + ".";
        }
        // let default missing required value message notify about a missing id
        removeCoupling(node);
        return null;
    }

    @Override
    public List<String> getOptions(final XsdTreeNode node, final Object field)
    {
        XsdTreeNode linkNode = node.getCoupledNodeAttribute("Link");
        if (linkNode != null)
        {
            String lane = node.getAttributeValue("Lane");
            if (lane != null)
            {
                List<String> options = new ArrayList<>();
                for (XsdTreeNode child : linkNode.getChildren())
                {
                    if (child.getNodeName().equals("TrafficLight") && child.isActive()
                            && lane.equals(child.getAttributeValue("Lane")))
                    {
                        options.add(child.getId());
                    }
                }
                return options;
            }
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public Map<XsdTreeNode, XsdTreeNode> getCouplings()
    {
        return this.coupledNodes;
    }

}
