package org.opentrafficsim.editor.decoration.validation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.swing.SwingUtilities;

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
 * Validates that an attribute 'X', or attribute Id on an element named 'X', matches any Id of a road layout element named 'X'
 * within a coupled road layout. For example, LaneOverride.Lane and Ots.Animation.RoadLayout.Lane.Id (both attributes) should
 * refer to a Lane.Id within a coupled road layout. The road layout can be coupled in different ways as described in
 * {@code LayoutCoupling}. In many cases this is via a Link, within which either a road layout is defined, or a defined road
 * layout is referred to. Example usage is:
 *
 * <pre>
 * new RoadLayoutElementValidator(editor, "Ots.Network.Link.LaneOverride", LayoutCoupling.PARENT_IS_LINK,
 *         RoadLayoutElementAttribute.LANE);
 * new RoadLayoutElementValidator(editor, "Ots.Animation.Link.Lane", LayoutCoupling.LINK_BY_PARENT_ID,
 *         RoadLayoutElementAttribute.ID);
 * </pre>
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class RoadLayoutElementValidator extends AbstractNodeDecoratorRemove implements CoupledValidator
{

    // TODO listen to Link.Id change

    /*
     * Implementation note: To validate attributes referring to elements within road layouts, this class often relies on the
     * coupling of Link.DefinedRoadLayout (node value) to a Definitions.RoadLayouts.RoadLayout (Id attribute value). When
     * creating RoadLayout nodes, assigning them an Id, removing them, undoing the remove, having a remove effectively be a
     * de-activation as it is the last RoadLayout, etc., it becomes non-trivial that the coupling from a Link to its RoadLayout
     * is correctly created, or broken, when a specific attribute is validated by this class. EventListeners are sorted by:
     * KeyValidators, KeyrefValidators, CoupledValidator, and then all other listeners. This does not guarantee that in the
     * cascade of nodes being altered and the GUI repainting, the node coupling and painting on screen is correct. With selected
     * usage of SwingUtilities.invokeLater(), which should specifically not be used in other locations, the implementation below
     * seems to produce the right result for all the actions mentioned above. This appears more arbitrarily so, than that it's
     * guaranteed by a clear flow in the program. To make the system more robust and simpler to understand and use, it might be
     * better to allocate validation to a separate thread. This thread could absorb various requests to re-validate nodes, but
     * only do so when any user action results in interrupting the thread. This interruption would be triggered after any change
     * to a node is made, including a cascade of nested changes.
     */

    /** */
    private static final long serialVersionUID = 20240305L;

    /** Path location of nodes to attach to. */
    private final String path;

    /** Defines how the layout is coupled to each node this validator will validate. */
    private final LayoutCoupling layoutCoupling;

    /** Attribute that is validated. */
    private final RoadLayoutElementAttribute elementAttribute;

    /** Map of link node to all validated nodes that depend on a specific link node. */
    private final Map<XsdTreeNode, Set<XsdTreeNode>> linkValidatedNodes = new LinkedHashMap<>();

    /** Map of layout node to all validated nodes that depend on a specific layout node. */
    private final Map<XsdTreeNode, Set<XsdTreeNode>> layoutValidatedNodes = new LinkedHashMap<>();

    /** Map of validated node to link node. This can be {@code null}. This key set is a subset of validatingLayout key set. */
    private final Map<XsdTreeNode, XsdTreeNode> validatingLink = new LinkedHashMap<>();

    /** Map of validated node to layout node. */
    private final Map<XsdTreeNode, XsdTreeNode> validatingLayout = new LinkedHashMap<>();

    /** Map of validated node with LayoutCoupling LINK_BY_PARENT_ID or LAYOUT_BY_PARENT_ID and their parent Id listeners. */
    private final Map<XsdTreeNode, EventListener> parentIdListener = new LinkedHashMap<>();

    /** All other validated nodes that may depend on layout id (LINK_ATTRIBUTE, PARENT_IS_LINK or PARENT_PARENT_IS_LINK). */
    private final Set<XsdTreeNode> nonParentId = new LinkedHashSet<>();

    /** Map of layout node to their respective element id listener. */
    private final Map<XsdTreeNode, LayoutIdListener> layoutIdListener = new LinkedHashMap<>();

    /** Coupled layout element nodes that are successfully validated to. */
    private final Map<XsdTreeNode, XsdTreeNode> coupledNodes = new LinkedHashMap<>();

    /**
     * Constructor.
     * @param editor editor.
     * @param path path of nodes to validate.
     * @param layoutCoupling defines how the layout is coupled.
     * @param attribute attribute that is validated.
     */
    public RoadLayoutElementValidator(final OtsEditor editor, final String path, final LayoutCoupling layoutCoupling,
            final RoadLayoutElementAttribute attribute)
    {
        super(editor, (n) -> true); // two conditions in notifyCreated() so prevent duplicate check here
        Throw.whenNull(path, "Path may not be null.");
        Throw.whenNull(layoutCoupling, "LayoutCoupling may not be null.");
        this.path = path;
        this.layoutCoupling = layoutCoupling;
        this.elementAttribute = attribute;
    }

    @Override
    public void notifyCreated(final XsdTreeNode node)
    {
        // notify() has already captured all layout and layout element nodes
        if (node.getPathString().endsWith(this.path))
        {
            // node that is validated to a road layout
            node.addAttributeValidator(this.elementAttribute.attributeName, this, this.elementAttribute);
            node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
            switch (this.layoutCoupling)
            {
                case LAYOUT_BY_PARENT_ID:
                case LINK_BY_PARENT_ID:
                {
                    // listen to changes in {node}.{parent}.id with small dedicated listener
                    EventListener listener = (event) ->
                    {
                        if (((String) ((Object[]) event.getContent())[1]).equals("Id"))
                        {
                            update(node);
                        }
                    };
                    node.getParent().addListener(listener, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
                    this.parentIdListener.put(node, listener);
                    break;
                }
                default:
                {
                    this.nonParentId.add(node);
                }
            }
            SwingUtilities.invokeLater(() -> update(node)); // during creation not all info might be available yet
        }
    }

    @Override
    public void notifyRemoved(final XsdTreeNode node)
    {
        // notify() has already captured all layout and layout element nodes
        if (node.getPathString().endsWith(this.path))
        {
            // node that is validated to a road layout
            XsdTreeNode linkNode = this.validatingLink.remove(node);
            if (linkNode != null && this.linkValidatedNodes.containsKey(linkNode))
            {
                this.linkValidatedNodes.get(linkNode).remove(node);
            }
            XsdTreeNode layoutNode = this.validatingLayout.remove(node);
            if (layoutNode != null)
            {
                if (this.layoutValidatedNodes.containsKey(layoutNode))
                {
                    this.layoutValidatedNodes.get(layoutNode).remove(node);
                }
                if (this.layoutIdListener.containsKey(layoutNode))
                {
                    this.layoutIdListener.get(layoutNode).nodes.remove(node);
                }
            }
            if (this.parentIdListener.containsKey(node))
            {
                node.getParent().removeListener(this.parentIdListener.remove(node), XsdTreeNode.ATTRIBUTE_CHANGED);
            }
            this.nonParentId.remove(node);
        }
    }

    @Override
    public void notify(final Event event) throws RemoteException
    {
        if (event.getType().equals(XsdTreeNode.ATTRIBUTE_CHANGED))
        {
            XsdTreeNode node = (XsdTreeNode) ((Object[]) event.getContent())[0];
            if (node.getPathString().equals(XsdPaths.DEFINED_ROADLAYOUT) || node.getPathString().equals(XsdPaths.LINK))
            {
                // Id change on defined layout or link, update all nodes that use this validator
                SwingUtilities.invokeLater(() -> updateAllActive());
            }
            else
            {
                // Id change on node that is validated to a road layout, update this node
                update(node);
            }
        }
        else if (event.getType().equals(XsdTreeNode.OPTION_CHANGED))
        {
            // road layout option in link changed
            XsdTreeNode linkNode = ((XsdTreeNode) ((Object[]) event.getContent())[0]).getParent();
            updateAllByLink(linkNode);
        }
        else if (event.getType().equals(XsdTreeNode.VALUE_CHANGED))
        {
            // value of defined layout changed
            XsdTreeNode linkNode = ((XsdTreeNode) ((Object[]) event.getContent())[0]).getParent().getParent();
            updateAllByLink(linkNode);
        }
        else if (event.getType().equals(XsdTreeNode.ACTIVATION_CHANGED))
        {
            // activation of defined layout or link changed
            updateAllActive();
        }
        else
        {
            // Capture NODE_REMOVED of layout (element) before notifyRemoved() as the parent is then null (we can only get it
            // here from event content). For symmetry also capture NODE_CREATED.
            if (event.getType().equals(XsdTreeNodeRoot.NODE_CREATED))
            {
                Object[] content = (Object[]) event.getContent();
                XsdTreeNode node = (XsdTreeNode) content[0];
                if (node.getPathString().equals(XsdPaths.DEFINED_ROADLAYOUT) || node.getPathString().equals(XsdPaths.LINK))
                {
                    // defined layout
                    SwingUtilities.invokeLater(() -> updateAllActive()); // during creation not all info might be available yet
                    node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
                    node.addListener(this, XsdTreeNode.ACTIVATION_CHANGED);
                    return;
                }
                XsdTreeNode parent = (XsdTreeNode) content[1];
                if (parent != null && (parent.getPathString().equals(XsdPaths.ROADLAYOUT)
                        || parent.getPathString().equals(XsdPaths.DEFINED_ROADLAYOUT)))
                {
                    // parent is layout node, i.e. this node is an element; listen to id change and update
                    LayoutIdListener listener = this.layoutIdListener.computeIfAbsent(parent, (n) -> new LayoutIdListener());
                    node.addListener(listener, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
                    updateAllByLayout(parent);
                    return;
                }
            }
            if (event.getType().equals(XsdTreeNodeRoot.NODE_REMOVED))
            {
                Object[] content = (Object[]) event.getContent();
                XsdTreeNode node = (XsdTreeNode) content[0];
                if (node.getPathString().equals(XsdPaths.DEFINED_ROADLAYOUT) || node.getPathString().equals(XsdPaths.LINK))
                {
                    // defined layout
                    updateAllActive();
                    node.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
                    node.removeListener(this, XsdTreeNode.ACTIVATION_CHANGED);
                    return;
                }
                XsdTreeNode parent = (XsdTreeNode) content[1];
                if (parent != null && (parent.getPathString().equals(XsdPaths.ROADLAYOUT)
                        || parent.getPathString().equals(XsdPaths.DEFINED_ROADLAYOUT)))
                {
                    // parent is layout node, i.e. this node is an element; un-listen to id change and update
                    LayoutIdListener listener = this.layoutIdListener.computeIfAbsent(parent, (n) -> new LayoutIdListener());
                    node.removeListener(listener, XsdTreeNode.ATTRIBUTE_CHANGED);
                    updateAllByLayout(parent);
                    return;
                }
            }
            super.notify(event);
        }
    }

    /**
     * Updates all nodes that are depending on the given link node.
     * @param link link node
     */
    private void updateAllByLink(final XsdTreeNode link)
    {
        if (this.linkValidatedNodes.containsKey(link))
        {
            this.linkValidatedNodes.get(link).forEach((n) -> update(n));
        }
    }

    /**
     * Updates all nodes that are depending on the given layout node.
     * @param layout layout node
     */
    private void updateAllByLayout(final XsdTreeNode layout)
    {
        if (this.layoutValidatedNodes.containsKey(layout))
        {
            this.layoutValidatedNodes.get(layout).forEach((n) -> update(n));
        }
    }

    /**
     * Updates all dependent nodes, if they are active.
     */
    private void updateAllActive()
    {
        for (XsdTreeNode node : this.parentIdListener.keySet())
        {
            if (node.isActive())
            {
                update(node);
            }
        }
        for (XsdTreeNode node : this.nonParentId)
        {
            if (node.isActive())
            {
                update(node);
            }
        }
    }

    /**
     * Updates node that is validated by a road layout.
     * @param node node to update.
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
                layoutNode = node.getParent().getCoupledNodeAttribute("Id");
                break;
            }
            case LINK_BY_PARENT_ID:
            {
                linkNode = node.getParent().getCoupledNodeAttribute("Id");
                layoutNode = getLayoutFromLink(linkNode);
                break;
            }
            case LINK_ATTRIBUTE:
            {
                linkNode = node.getCoupledNodeAttribute("Link");
                layoutNode = getLayoutFromLink(linkNode);
                break;
            }
            case PARENT_IS_LINK:
            {
                linkNode = node.getParent();
                if (linkNode.isSequence())
                {
                    linkNode = linkNode.getParent();
                }
                layoutNode = getLayoutFromLink(linkNode);
                break;
            }
            default:
            {
                throw new RuntimeException("Unsupported layout coupling " + this.layoutCoupling);
            }
        }

        // add node as validated by link/layout
        XsdTreeNode formerLinkNode =
                linkNode == null ? this.validatingLink.remove(node) : this.validatingLink.put(node, linkNode);
        if (linkNode != null && linkNode.getChildCount() > 1)
        {
            this.linkValidatedNodes.computeIfAbsent(linkNode, (n) -> new LinkedHashSet<>()).add(node);
            for (XsdOption option : linkNode.getChild(1).getOptions()) // 2nd child is RoadLayout/DefinedRoadLayout choice
            {
                option.optionNode().addListener(this, XsdTreeNode.OPTION_CHANGED);
                if (option.optionNode().getNodeName().equals("xsd:sequence"))
                {
                    // defined layout
                    option.optionNode().getChild(0).addListener(this, XsdTreeNode.VALUE_CHANGED); // Defined Layout node
                    break;
                }
            }
        }
        XsdTreeNode formerLayoutNode =
                layoutNode == null ? this.validatingLayout.remove(node) : this.validatingLayout.put(node, layoutNode);
        if (layoutNode != null)
        {
            this.layoutValidatedNodes.computeIfAbsent(layoutNode, (n) -> new LinkedHashSet<>()).add(node);
            if (!layoutNode.equals(formerLayoutNode))
            {
                LayoutIdListener listener = this.layoutIdListener.computeIfAbsent(layoutNode, (n) -> new LayoutIdListener());
                listener.nodes.add(node);
                layoutNode.addListener(listener, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
                for (XsdTreeNode element : layoutNode.getChildren())
                {
                    element.addListener(listener, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
                }
            }
        }

        // remove node as validated by former link/layout
        if (formerLinkNode != null && !formerLinkNode.equals(linkNode))
        {
            this.linkValidatedNodes.computeIfAbsent(formerLinkNode, (n) -> new LinkedHashSet<>()).remove(node);
            for (XsdOption option : formerLinkNode.getChild(1).getOptions()) // 2nd child is RoadLayout/DefinedRoadLayout choice
            {
                option.optionNode().removeListener(this, XsdTreeNode.OPTION_CHANGED);
                if (option.optionNode().getNodeName().equals("xsd:sequence"))
                {
                    // defined layout
                    option.optionNode().getChild(0).removeListener(this, XsdTreeNode.VALUE_CHANGED); // Defined Layout node
                    break;
                }
            }
        }
        if (formerLayoutNode != null && !formerLayoutNode.equals(layoutNode))
        {
            this.layoutValidatedNodes.computeIfAbsent(formerLayoutNode, (n) -> new LinkedHashSet<>()).remove(node);
            LayoutIdListener listener = this.layoutIdListener.computeIfAbsent(formerLayoutNode, (n) -> new LayoutIdListener());
            listener.nodes.remove(node);
            formerLayoutNode.removeListener(listener, XsdTreeNode.ATTRIBUTE_CHANGED);
            for (XsdTreeNode element : formerLayoutNode.getChildren())
            {
                element.removeListener(listener, XsdTreeNode.ATTRIBUTE_CHANGED);
            }
        }
        node.invalidate();
    }

    /**
     * Return the layout node attached to a link, i.e. the layout defined under it, or a defined layout that is referred to.
     * @param linkNode link node.
     * @return layout node.
     */
    private XsdTreeNode getLayoutFromLink(final XsdTreeNode linkNode)
    {
        if (linkNode == null || !linkNode.isActive() || linkNode.getChildCount() <= 1)
        {
            return null;
        }
        XsdTreeNode layout = linkNode.getChild(1);
        if ("RoadLayout".equals(layout.getNodeName()))
        {
            return layout;
        }
        layout.getChild(0).invalidate();
        layout.getChild(0).isValid(); // recouple if things have changed
        return layout.getChild(0).getCoupledNodeValue(); // sequence of which DefinedLayout is the first node
    }

    @Override
    public String validate(final XsdTreeNode node)
    {
        String value = node.getAttributeValue(this.elementAttribute.attributeName);
        if (value == null || value.isEmpty())
        {
            removeCoupling(node);
            if (this.layoutCoupling.equals(LayoutCoupling.LINK_ATTRIBUTE))
            {
                // missing Lane value is only ok if the Link value is also missing
                String link = node.getAttributeValue("Link");
                if (link == null || link.isEmpty())
                {
                    return null;
                }
                // let the code below find no coupledRoadLayoutElement
            }
            else
            {
                // missing Lane value is ok for this validator, it can have use="required" on its own
                return null;
            }
        }
        String attribute = this.elementAttribute.attributeName;
        String layoutElement = "Id".equals(attribute) ? node.getNodeName() : attribute; // e.g. "Stripe" for Stripe.Id
        XsdTreeNode layoutNode = this.validatingLayout.get(node);
        XsdTreeNode coupledRoadLayoutElement = null;
        if (layoutNode != null)
        {
            for (XsdTreeNode child : layoutNode.getChildren())
            {
                if (child.getNodeName().equals(layoutElement) && child.getId() != null && !child.getId().isEmpty()
                        && child.getId().equals(value) && child.reportInvalidId() == null)
                {
                    coupledRoadLayoutElement = child;
                    break;
                }
            }
        }
        if (coupledRoadLayoutElement == null)
        {
            removeCoupling(node);
            return this.elementAttribute.attributeName + " " + value + " does not refer to a valid " + layoutElement
                    + " in the road layout.";
        }
        addCoupling(node, coupledRoadLayoutElement);
        return null;
    }

    @Override
    public Map<XsdTreeNode, XsdTreeNode> getCouplings()
    {
        return this.coupledNodes;
    }

    @Override
    public List<String> getOptions(final XsdTreeNode node, final Object field)
    {
        XsdTreeNode layoutNode = this.validatingLayout.get(node);
        if (layoutNode != null)
        {
            String attribute = ((RoadLayoutElementAttribute) field).attributeName;
            String layoutElement = "Id".equals(attribute) ? node.getNodeName() : attribute; // e.g. "Stripe" for Stripe.Id
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
     * Defines how the node is coupled to a road layout.
     */
    public enum LayoutCoupling
    {
        /** {Node}.{Parent}.Id refers to {RoadLayout}.Id. */
        LAYOUT_BY_PARENT_ID,

        /** {Node}.{Parent}.Id refers to a link from which the road layout is taken. */
        LINK_BY_PARENT_ID,

        /** {Node}.Link refers to a link from which the road layout is taken. */
        LINK_ATTRIBUTE,

        /**
         * {Node}.{Parent} is a link from which the road layout is taken. If the parent is a sequence, the parent of that
         * sequence is used.
         */
        PARENT_IS_LINK,
    }

    /**
     * Defines the attribute pointing to the road layout element.
     */
    public enum RoadLayoutElementAttribute
    {
        /** SomeWhere.{Node}.Id = RoadLayout.{Node}.Id, where 'Node' is either Lane, Stripe or Shoulder. */
        ID("Id"),

        /** Node.Lane = RoadLayout.Lane.Id. */
        LANE("Lane"),

        /** Node.Stripe = RoadLayout.Stripe.Id. */
        STRIPE("Stripe"),

        /** Node.Shoulder = RoadLayout.Shoulder.Id. */
        SHOULDER("Shoulder");

        /** Attribute. */
        private final String attributeName;

        /**
         * Constructor.
         * @param attributeName attribute name
         */
        RoadLayoutElementAttribute(final String attributeName)
        {
            this.attributeName = attributeName;
        }
    }

    /**
     * Listens to changes of Id in road layout and road layout element. Changes values of coupled elements, or updates all that
     * maybe should become coupled.
     */
    private final class LayoutIdListener implements EventListener
    {
        /** */
        private static final long serialVersionUID = 20240305L;

        /** Nodes that need to update when the id changes. */
        private final Set<XsdTreeNode> nodes = new LinkedHashSet<>();

        @Override
        public void notify(final Event event) throws RemoteException
        {
            Object[] content = (Object[]) event.getContent();
            String attribute = (String) content[1];
            if (attribute.equals("Id"))
            {
                XsdTreeNode changedNode = (XsdTreeNode) content[0];
                String element = changedNode.getNodeName();
                if (!"RoadLayout".equals(element))
                {
                    // element id was changed
                    for (XsdTreeNode node : this.nodes)
                    {
                        if (changedNode.equals(getCouplings().get(node)))
                        {
                            // change value on node that was coupled to element with the changed id
                            if (node.hasAttribute(element))
                            {
                                CoupledValidator.setAttributeIfNotNull(node, element, changedNode.getAttributeValue(attribute));
                            }
                            else if (node.getNodeName().equals(element) && node.isIdentifiable())
                            {
                                CoupledValidator.setAttributeIfNotNull(node, "Id", changedNode.getAttributeValue(attribute));
                            }
                        }
                    }
                    this.nodes.forEach((n) -> n.invalidate()); // any can now have the right value with new element id
                }
                else
                {
                    // road layout id was changed, need to update defined layout in link and all nodes
                    updateAllActive();
                }
            }
        }
    }

}
