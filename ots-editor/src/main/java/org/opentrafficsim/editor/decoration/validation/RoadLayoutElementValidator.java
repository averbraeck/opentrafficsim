package org.opentrafficsim.editor.decoration.validation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.reference.ReferenceType;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.XsdOption;
import org.opentrafficsim.editor.XsdPaths;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeRoot;
import org.opentrafficsim.editor.decoration.AbstractNodeDecoratorRemove;

/**
 * Validates that attributes named Stripe, Lane, Shoulder and NoTrafficLane of a node, or the Id attribute of a node itself
 * named Stripe, Lane, Shoulder or NoTrafficLane, match any id of the respective type within a coupled road layout. The road
 * layout can be coupled in different ways as described in {@code LayoutCoupling}.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class RoadLayoutElementValidator extends AbstractNodeDecoratorRemove implements CoupledValidator
{

    /** */
    private static final long serialVersionUID = 20240305L;

    /** Path location of nodes to attach to. */
    private final String path;

    /** Defines how the layout is coupled to each node this validator will validate. */
    private final LayoutCoupling layoutCoupling;

    /** Attribute that is validated. */
    private final String attribute;

    /** Map of link node to all validated nodes that depend on a specific link node. */
    private final Map<XsdTreeNode, Set<XsdTreeNode>> coupledLinks = new LinkedHashMap<>();

    /** Map of layout node to all validated nodes that depend on a specific layout node. */
    private final Map<XsdTreeNode, Set<XsdTreeNode>> coupledLayouts = new LinkedHashMap<>();

    /** Map of validated node to link node. This can be {@code null}. */
    private final Map<XsdTreeNode, XsdTreeNode> linkNodes = new LinkedHashMap<>();

    /** Map of validated node to layout node. */
    private final Map<XsdTreeNode, XsdTreeNode> layoutNodes = new LinkedHashMap<>();

    /** Map of node with LayoutCoupling LAYOUT_BY_PARENT_ID to and their parent Id listeners. */
    private final Map<XsdTreeNode, EventListener> allLayoutByParentId = new LinkedHashMap<>();

    /** Map of node with LayoutCoupling LINK_BY_PARENT_ID to and their parent Id listeners. */
    private final Map<XsdTreeNode, EventListener> allLinkByParentId = new LinkedHashMap<>();
    
    /** All other nodes that may depend on layout id. */
    private final Set<XsdTreeNode> allOther = new LinkedHashSet<>();

    /** Map of layout node to their respective elements id listener. */
    private final Map<XsdTreeNode, IdListener> layoutListeners = new LinkedHashMap<>();

    /** Coupled layout element nodes that are successfully validated to. */
    private final Map<XsdTreeNode, XsdTreeNode> coupledNode = new LinkedHashMap<>();

    /**
     * Constructor.
     * @param editor OtsEditor; editor.
     * @param path String; path of nodes to validate.
     * @param layoutCoupling LayoutCoupling; defines how the layout is coupled.
     * @param attribute String; attribute that is validated.
     */
    public RoadLayoutElementValidator(final OtsEditor editor, final String path, final LayoutCoupling layoutCoupling,
            final String attribute)
    {
        super(editor);
        Throw.whenNull(path, "Path may not be null.");
        Throw.whenNull(layoutCoupling, "LayoutCoupling may not be null.");
        this.path = path;
        this.layoutCoupling = layoutCoupling;
        this.attribute = attribute;
    }

    /** {@inheritDoc} */
    @Override
    public void notifyCreated(final XsdTreeNode node)
    {
        if (node.getPathString().endsWith(this.path))
        {
            node.addAttributeValidator(this.attribute, this);
            node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
            switch (this.layoutCoupling)
            {
                case LAYOUT_BY_PARENT_ID:
                {
                    // listen to changes in {node}.{parent}.id with small dedicated listener
                    EventListener listener = new IdListener(true).addNode(node);
                    node.getParent().addListener(listener, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
                    this.allLayoutByParentId.put(node, listener);
                    break;
                }
                case LINK_ATTRIBUTE:
                case PARENT_IS_LINK:
                {
                    this.allOther.add(node);
                    break;
                }
                case LINK_BY_PARENT_ID:
                {
                    // listen to changes in {node}.{parent}.id with small dedicated listener
                    EventListener listener = new IdListener(true).addNode(node);
                    node.getParent().addListener(listener, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
                    this.allLinkByParentId.put(node, listener);
                    break;
                }
                default:
                {
                    throw new RuntimeException("Usupported layout coupling " + this.layoutCoupling);
                }
            }
            update(node); // take current info during creation, respond to further changes by listeners
        }
        else if (node.getPathString().equals("Ots.Definitions.RoadLayouts.RoadLayout"))
        {
            node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
        }
    }

    /** {@inheritDoc} */
    @Override
    public void notifyRemoved(final XsdTreeNode node)
    {
        if (this.path.equals(node.getPathString()))
        {
            XsdTreeNode linkNode = this.linkNodes.remove(node);
            if (linkNode != null)
            {
                this.coupledLinks.computeIfAbsent(linkNode, (n) -> new LinkedHashSet<>()).remove(node);
            }
            XsdTreeNode layoutNode = this.layoutNodes.remove(node);
            if (layoutNode != null)
            {
                this.coupledLayouts.computeIfAbsent(layoutNode, (n) -> new LinkedHashSet<>()).remove(node);
                IdListener listener = this.layoutListeners.get(layoutNode);
                if (listener != null)
                {
                    listener.removeNode(node);
                }
            }
            if (this.allLayoutByParentId.containsKey(node))
            {
                node.getParent().removeListener(this.allLayoutByParentId.get(node), XsdTreeNode.ATTRIBUTE_CHANGED);
                this.allLayoutByParentId.remove(node);
            }
            if (this.allLinkByParentId.containsKey(node))
            {
                node.getParent().removeListener(this.allLinkByParentId.get(node), XsdTreeNode.ATTRIBUTE_CHANGED);
                this.allLinkByParentId.remove(node);
            }
        }
        else if (node.getPathString().equals("Ots.Definitions.RoadLayouts.RoadLayout"))
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
            XsdTreeNode node = (XsdTreeNode) ((Object[]) event.getContent())[0];
            if (node.getPathString().equals("Ots.Definitions.RoadLayouts.RoadLayout"))
            {
                // Id change on defined layout, update all nodes that use this validator
                updateAllActive(this.allLayoutByParentId.keySet());
                updateAllActive(this.allLinkByParentId.keySet());
                updateAllActive(this.allOther);
                return;
            }
            update(node);
        }
        else if (event.getType().equals(XsdTreeNode.OPTION_CHANGED))
        {
            // road layout option in link changed
            Object[] content = (Object[]) event.getContent();
            XsdTreeNode linkNode = ((XsdTreeNode) content[0]).getParent();
            if (linkNode != null && this.coupledLinks.containsKey(linkNode))
            {
                this.coupledLinks.get(linkNode).forEach((node) -> update(node));
            }
        }
        else if (event.getType().equals(XsdTreeNode.VALUE_CHANGED))
        {
            // value of defined layout changed
            Object[] content = (Object[]) event.getContent();
            XsdTreeNode linkNode = ((XsdTreeNode) content[0]).getParent().getParent();
            if (linkNode != null && this.coupledLinks.containsKey(linkNode))
            {
                this.coupledLinks.get(linkNode).forEach((node) -> update(node));
            }
        }
        else
        {
            if (event.getType().equals(XsdTreeNodeRoot.NODE_CREATED))
            {
                Object[] content = (Object[]) event.getContent();
                XsdTreeNode parent = (XsdTreeNode) content[1];
                if (parent != null && (parent.getPathString().equals(XsdPaths.ROADLAYOUT)
                        || parent.getPathString().equals(XsdPaths.DEFINED_ROADLAYOUT)))
                {
                    // parent is layout node, i.e. this node is an element; listen to id change and update
                    IdListener listener = this.layoutListeners.computeIfAbsent(parent, (n) -> new IdListener(false));
                    ((XsdTreeNode) content[0]).addListener(listener, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
                    this.coupledLayouts.computeIfAbsent(parent, (n) -> new LinkedHashSet<>()).forEach((n) -> update(n));
                    return;
                }
            }
            if (event.getType().equals(XsdTreeNodeRoot.NODE_REMOVED))
            {
                Object[] content = (Object[]) event.getContent();
                XsdTreeNode parent = (XsdTreeNode) content[1];
                if (parent != null && (parent.getPathString().equals(XsdPaths.ROADLAYOUT)
                        || parent.getPathString().equals(XsdPaths.DEFINED_ROADLAYOUT)))
                {
                    // parent is layout node, i.e. this node is an element; un-listen to id change and update
                    IdListener listener = this.layoutListeners.computeIfAbsent(parent, (n) -> new IdListener(false));
                    ((XsdTreeNode) content[0]).removeListener(listener, XsdTreeNode.ATTRIBUTE_CHANGED);
                    this.coupledLayouts.computeIfAbsent(parent, (n) -> new LinkedHashSet<>()).forEach((n) -> update(n));
                    return;
                }
            }
            super.notify(event);
        }
    }
    
    /**
     * Updates all nodes in the given set, if they are active.
     * @param set Set&lt;XsdTreeNode&gt;; set.
     */
    private final void updateAllActive(final Set<XsdTreeNode> set)
    {
        for (XsdTreeNode node : set)
        {
            if (node.isActive())
            {
                update(node);
            }
        }
    }

    /**
     * Updates node.
     * @param node XsdTreeNode; node to update.
     */
    private void update(final XsdTreeNode node)
    {
        XsdTreeNode linkNode;
        XsdTreeNode layoutNode;
        switch (this.layoutCoupling)
        {
            case LAYOUT_BY_PARENT_ID:
            {
                linkNode = null;
                layoutNode = node.getParent().getCoupledKeyrefNodeAttribute("Id");
                break;
            }
            case LINK_BY_PARENT_ID:
            {
                linkNode = node.getParent().getCoupledKeyrefNodeAttribute("Id");
                layoutNode = getLayoutFromLink(linkNode);
                break;
            }
            case LINK_ATTRIBUTE:
            {
                linkNode = node.getCoupledKeyrefNodeAttribute("Link");
                layoutNode = getLayoutFromLink(linkNode);
                break;
            }
            case PARENT_IS_LINK:
            {
                linkNode = node.getParent();
                layoutNode = getLayoutFromLink(linkNode);
                break;
            }
            default:
            {
                throw new RuntimeException("Usupported layout coupling " + this.layoutCoupling);
            }
        }

        // add node as validated by link/layout
        if (linkNode != null)
        {
            this.coupledLinks.computeIfAbsent(linkNode, (n) -> new LinkedHashSet<>()).add(node);
            for (XsdOption option : linkNode.getChild(1).getOptions())
            {
                option.getOptionNode().addListener(this, XsdTreeNode.OPTION_CHANGED, ReferenceType.WEAK);
                if (option.getOptionNode().getNodeName().equals("xsd:sequence"))
                {
                    // defined layout
                    option.getOptionNode().getChild(0).addListener(this, XsdTreeNode.VALUE_CHANGED, ReferenceType.WEAK);
                    break;
                }
            }
        }
        XsdTreeNode formerLayoutNode = this.layoutNodes.get(node);
        if (layoutNode != null)
        {
            this.coupledLayouts.computeIfAbsent(layoutNode, (n) -> new LinkedHashSet<>()).add(node);
            formerLayoutNode = this.layoutNodes.put(node, layoutNode);
            if (!layoutNode.equals(formerLayoutNode))
            {
                IdListener listener = this.layoutListeners.computeIfAbsent(layoutNode, (n) -> new IdListener(false));
                listener.addNode(node);
                layoutNode.addListener(listener, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
                for (XsdTreeNode element : layoutNode.getChildren())
                {
                    element.addListener(listener, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
                }
            }
        }
        else
        {
            this.layoutNodes.remove(node);
        }

        // remove node as validated by former link/layout
        XsdTreeNode formerLinkNode = this.linkNodes.put(node, linkNode);
        if (formerLinkNode != null && !formerLinkNode.equals(linkNode))
        {
            this.coupledLinks.computeIfAbsent(formerLinkNode, (n) -> new LinkedHashSet<>()).remove(node);
            for (XsdOption option : formerLinkNode.getChild(1).getOptions())
            {
                option.getOptionNode().removeListener(this, XsdTreeNode.OPTION_CHANGED);
                if (option.getOptionNode().getNodeName().equals("xsd:sequence"))
                {
                    // defined layout
                    option.getOptionNode().getChild(0).removeListener(this, XsdTreeNode.VALUE_CHANGED);
                    break;
                }
            }
        }
        if (formerLayoutNode != null && !formerLayoutNode.equals(layoutNode))
        {
            this.coupledLayouts.computeIfAbsent(formerLayoutNode, (n) -> new LinkedHashSet<>()).remove(node);
            IdListener listener = this.layoutListeners.computeIfAbsent(formerLayoutNode, (n) -> new IdListener(false));
            listener.removeNode(node);
            formerLayoutNode.removeListener(listener, XsdTreeNode.ATTRIBUTE_CHANGED);
            for (XsdTreeNode element : formerLayoutNode.getChildren())
            {
                element.removeListener(listener, XsdTreeNode.ATTRIBUTE_CHANGED);
            }
        }
        node.invalidate();
    }

    /** {@inheritDoc} */
    @Override
    public String validate(final XsdTreeNode node)
    {
        String value = node.getAttributeValue(this.attribute);
        String layoutElement = "Id".equals(this.attribute) ? node.getNodeName() : this.attribute;
        XsdTreeNode layoutNode = this.layoutNodes.get(node);
        XsdTreeNode coupled = null;
        if (layoutNode != null)
        {
            for (XsdTreeNode child : layoutNode.getChildren())
            {
                if (child.getNodeName().equals(layoutElement) && child.getId() != null && !child.getId().isEmpty()
                        && child.getId().equals(value) && child.reportInvalidId() == null)
                {
                    coupled = child;
                    break;
                }
            }
        }
        if (coupled == null)
        {
            this.coupledNode.remove(node);
            return this.attribute + " " + value + " does not refer to a valid " + layoutElement + " in the road layout.";
        }
        this.coupledNode.put(node, coupled);
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getOptions(final XsdTreeNode node, final String field, final XPathFieldType fieldType)
    {
        XsdTreeNode layoutNode = this.layoutNodes.get(node);
        String layoutElement = "Id".equals(field) ? node.getNodeName() : field;
        if (layoutNode != null)
        {
            List<String> options = new ArrayList<>();
            for (XsdTreeNode child : layoutNode.getChildren())
            {
                if (child.getNodeName().equals(layoutElement) && child.getId() != null && !child.getId().isEmpty())
                {
                    options.add(child.getId());
                }
            }
            return options;
        }
        return null;
    }

    /**
     * Return the layout node attached to a link, i.e. the layout defined under it, or a defined layout that is referred to.
     * @param linkNode XsdTreeNode; link node.
     * @return XsdTreeNode; layout node.
     */
    private final XsdTreeNode getLayoutFromLink(final XsdTreeNode linkNode)
    {
        if (linkNode == null || !linkNode.isActive())
        {
            return null;
        }
        XsdTreeNode layout = linkNode.getChild(1);
        if ("RoadLayout".equals(layout.getNodeName()))
        {
            return layout;
        }
        return layout.getChild(0).getCoupledKeyrefNodeValue(); // sequence of which DefinedLayout is the first node
    }

    /** {@inheritDoc} */
    @Override
    public XsdTreeNode getCoupledKeyrefNode(final XsdTreeNode node)
    {
        return this.coupledNode.get(node);
    };

    /**
     * Defines how the node is coupled to a road layout.
     * <p>
     * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public static enum LayoutCoupling
    {
        /** {Node}.Id refers to {RoadLayout}.Id. */
        LAYOUT_BY_PARENT_ID,

        /** {Node}.{Parent}.Id refers to a link from which the road layout is taken. */
        LINK_BY_PARENT_ID,

        /** {Node}.Link refers to a link from which the road layout is taken. */
        LINK_ATTRIBUTE,

        /** {Node}.{Parent} is a link from which the road layout is taken. */
        PARENT_IS_LINK;
    }

    /**
     * Simple id attribute listener that will update or invalidate a wrapped node.
     * <p>
     * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    private class IdListener implements EventListener
    {
        /** */
        private static final long serialVersionUID = 20240305L;

        /** Node that needs to update when the id changes. */
        private final Set<XsdTreeNode> nodes = new LinkedHashSet<>();

        /** Whether to update, or only change coupled id's and invalidate. */
        private final boolean update;

        /**
         * Constructor.
         * @param update boolean; whether to update, or only change coupled id's and invalidate.
         */
        public IdListener(final boolean update)
        {
            this.update = update;
        }

        /**
         * Adds node that will be triggered upon id change.
         * @param node XsdTreeNode; node to add.
         * @return IdListener; this for method chaining.
         */
        private IdListener addNode(final XsdTreeNode node)
        {
            this.nodes.add(node);
            return this;
        }

        /**
         * Removes node that will be triggered upon id change.
         * @param node XsdTreeNode; node to remove.
         */
        private void removeNode(final XsdTreeNode node)
        {
            this.nodes.remove(node);
        }

        /** {@inheritDoc} */
        @Override
        public void notify(final Event event) throws RemoteException
        {
            Object[] content = (Object[]) event.getContent();
            String attribute = (String) content[1];
            if (attribute.equals("Id"))
            {
                if (this.update)
                {
                    this.nodes.forEach((n) -> update(n));
                }
                else
                {
                    XsdTreeNode changedNode = (XsdTreeNode) content[0];
                    String element = changedNode.getNodeName();
                    String previous = (String) content[2];
                    if (!"RoadLayout".equals(element))
                    {
                        for (XsdTreeNode node : this.nodes)
                        {
                            if (node.hasAttribute(element) && node.getAttributeValue(element).equals(previous))
                            {
                                node.setAttributeValue(element, changedNode.getAttributeValue(attribute));
                            }
                        }
                        this.nodes.forEach((n) -> n.invalidate());
                    }
                    else
                    {
                        // road layout id was changed, need to update defined layout in link and all nodes
                        Set<XsdTreeNode> set = new LinkedHashSet<>(this.nodes); // copy due to concurrent modification
                        set.forEach((n) -> update(n));
                    }
                    
                }
            }
        }
    }

}
