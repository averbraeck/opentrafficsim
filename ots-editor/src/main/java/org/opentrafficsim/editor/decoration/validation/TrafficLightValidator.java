package org.opentrafficsim.editor.decoration.validation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.djutils.event.Event;
import org.djutils.event.reference.ReferenceType;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.decoration.AbstractNodeDecoratorRemove;
import org.opentrafficsim.editor.decoration.validation.RoadLayoutElementValidator.LayoutCoupling;

/**
 * Checks that attribute TrafficLightId can be found on link under attribute Link (with keyref) having the same lane under
 * attribute Lane. This class relies on a {@code RoadLayoutElementValidator} to validate the Lane attribute with the Link
 * attribute, which will invalidate the node if either attribute is changed. This second validator is create in the constructor
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

    /** Path location of nodes to attach to. */
    private final String path;

    /** Coupled nodes that are successfully coupled to. */
    private final Map<XsdTreeNode, XsdTreeNode> coupledNodes = new LinkedHashMap<>();

    /**
     * Constructor.
     * @param editor OtsEditor; editor.
     * @param path String; path.
     */
    public TrafficLightValidator(final OtsEditor editor, final String path)
    {
        super(editor);
        this.path = path;
        new RoadLayoutElementValidator(editor, path, LayoutCoupling.LINK_ATTRIBUTE, "Lane");
    }

    /** {@inheritDoc} */
    @Override
    public void notifyCreated(final XsdTreeNode node)
    {
        if (this.path.equals(node.getPathString()))
        {
            node.addAttributeValidator("TrafficLightId", this);
            node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notifyRemoved(final XsdTreeNode node)
    {
        if (this.path.equals(node.getPathString()))
        {
            node.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
    {
        if (event.getType().equals(XsdTreeNode.ATTRIBUTE_CHANGED))
        {
            ((XsdTreeNode) ((Object[]) event.getContent())[0]).invalidate();
        }
        else
        {
            super.notify(event);
        }
    }

    /** {@inheritDoc} */
    @Override
    public String validate(final XsdTreeNode node)
    {
        String id = node.getAttributeValue("TrafficLightId");
        if (id != null && !id.isEmpty())
        {
            XsdTreeNode linkNode = node.getCoupledKeyrefNodeAttribute("Link");
            if (linkNode != null)
            {
                String lane = node.getAttributeValue("Lane");
                if (lane != null)
                {
                    for (XsdTreeNode child : linkNode.getChildren())
                    {
                        if (child.getNodeName().equals("TrafficLight") && child.isActive()
                                && lane.equals(child.getAttributeValue("Lane")))
                        {
                            if (id.equals(child.getId()))
                            {
                                this.coupledNodes.put(node, child);
                                return null;
                            }
                        }
                    }
                    this.coupledNodes.remove(node);
                    return "There is no traffic light with id " + id + " on link " + node.getAttributeValue("Link")
                            + " at lane " + lane + ".";
                }
            }
        }
        this.coupledNodes.remove(node);
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getOptions(final XsdTreeNode node, final String field)
    {
        XsdTreeNode linkNode = node.getCoupledKeyrefNodeAttribute("Link");
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
    public XsdTreeNode getCoupledKeyrefNode(final XsdTreeNode node)
    {
        return this.coupledNodes.get(node);
    }

}
