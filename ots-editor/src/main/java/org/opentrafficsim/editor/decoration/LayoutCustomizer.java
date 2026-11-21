package org.opentrafficsim.editor.decoration;

import java.rmi.RemoteException;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;

import org.djutils.event.EventListener;
import org.opentrafficsim.editor.OtsEditor;
import org.opentrafficsim.editor.Undo.ActionType;
import org.opentrafficsim.editor.XsdOption;
import org.opentrafficsim.editor.XsdPaths;
import org.opentrafficsim.editor.XsdTreeNode;
import org.opentrafficsim.editor.XsdTreeNodeRoot;

/**
 * Allows a defined road layout selected at a link, to be copied in to a customizable road layout at the link.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class LayoutCustomizer extends AbstractNodeDecoratorRemove implements EventListener, Consumer<XsdTreeNode>
{

    /** Stripe attributes shared by stripe types and custom stripes. */
    private static final List<String> STRIPE_ATTRIBUTES =
            List.of("LeftChangeLane", "RightChangeLane", "Block", "LateralSync");

    /** Editor. */
    private OtsEditor editor;

    /**
     * Constructor.
     * @param editor editor.
     * @throws RemoteException if listener cannot be added.
     */
    public LayoutCustomizer(final OtsEditor editor) throws RemoteException
    {
        super(editor, (n) -> true);
        this.editor = editor;
    }

    @Override
    public void notifyCreated(final XsdTreeNode node)
    {
        if (node.getPathString().equals(XsdPaths.LINK + ".xsd:sequence") && node.getChildCount() > 0
                && node.getChild(0).getPathString().equals(XsdPaths.LINK + ".DefinedLayout"))
        {
            node.addConsumer("Customize", this);
        }
    }

    @Override
    public void notifyRemoved(final XsdTreeNode node)
    {
        node.removeListener(this, XsdTreeNodeRoot.NODE_REMOVED);
    }

    @Override
    public void accept(final XsdTreeNode node)
    {
        Optional<XsdTreeNode> defined = node.getChild(0).getCoupledNodeValue();
        if (defined.isEmpty())
        {
            // do nothing if there is no coupled defined road layout
            return;
        }
        XsdTreeNode custom = null;
        for (XsdOption option : node.getOptions())
        {
            if (!option.optionNode().equals(node))
            {
                custom = option.optionNode();
                break;
            }
        }
        if (custom == null)
        {
            return;
        }
        Map<String, XsdTreeNode> laneOverrides = new LinkedHashMap<>();
        Map<String, XsdTreeNode> stripeOverrides = new LinkedHashMap<>();
        for (XsdTreeNode child : node.getChildren())
        {
            if (!child.isActive())
            {
                continue;
            }
            if (child.getNodeName().equals("LaneOverride"))
            {
                laneOverrides.put(child.getAttributeValueOrDefault("Lane"), child);
            }
            else if (child.getNodeName().equals("StripeOverride"))
            {
                stripeOverrides.put(child.getAttributeValueOrDefault("Stripe"), child);
            }
        }
        List<XsdTreeNode> formerChildren = custom.getChildren();
        this.editor.getUndo().startAction(ActionType.ACTION, node, null);
        for (XsdTreeNode definedChild : defined.get().getChildren())
        {
            definedChild.createCopy(custom, custom.getChildCount());
        }
        for (XsdTreeNode formerChild : formerChildren)
        {
            formerChild.remove();
        }
        custom.setAttributeValue("LaneKeeping", defined.get().getAttributeValue("LaneKeeping"));
        applyLaneOverrides(node.getParent(), custom, laneOverrides);
        applyStripeOverrides(custom, stripeOverrides);
        node.setOption(custom);
        this.editor.show(custom, null);
    }

    /**
     * Applies the lane overrides to the copied lanes. A lane cannot carry speed limits, so a dedicated
     * lane type is created for each overridden lane.
     * @param link link node.
     * @param custom copied custom road layout.
     * @param overrides lane overrides, by lane id.
     */
    private static void applyLaneOverrides(final XsdTreeNode link, final XsdTreeNode custom,
            final Map<String, XsdTreeNode> overrides)
    {
        if (overrides.isEmpty())
        {
            return;
        }
        XsdTreeNode laneTypes = findNode(link.getRoot(), XsdPaths.DEFINITIONS + ".LaneTypes");
        if (laneTypes == null)
        {
            return;
        }
        Set<String> usedIds = collectIds(link.getRoot(), XsdPaths.DEFINED_LANETYPE);
        laneTypes.setActive();
        for (Map.Entry<String, XsdTreeNode> entry : overrides.entrySet())
        {
            XsdTreeNode lane = findElement(custom, "Lane", entry.getKey());
            if (lane == null)
            {
                continue;
            }
            Optional<XsdTreeNode> baseLaneType = lane.getCoupledNodeAttribute("LaneType");
            if (baseLaneType.isEmpty())
            {
                baseLaneType = findDefinition(link.getRoot(), XsdPaths.DEFINED_LANETYPE,
                        lane.getAttributeValueOrDefault("LaneType"));
            }
            if (baseLaneType.isEmpty())
            {
                continue;
            }
            String id = uniqueId(baseLaneType.get().getId(), entry.getKey(), usedIds);
            XsdTreeNode laneType = baseLaneType.get().createCopy(laneTypes, laneTypes.getChildCount());
            laneType.setId(id);
            laneType.setAttributeValue("Builtin", "false");
            applyLaneOverride(entry.getValue(), laneType);
            lane.setAttributeValue("LaneType", id);
        }
    }

    /**
     * Applies one lane override to a copied lane type.
     * @param override lane override.
     * @param laneType copied lane type.
     */
    private static void applyLaneOverride(final XsdTreeNode override, final XsdTreeNode laneType)
    {
        XsdTreeNode speedLimit = findActiveChild(override, "SpeedLimit");
        if (speedLimit != null)
        {
            copyChoiceChild(speedLimit, laneType, "SpeedLimit");
        }
        else
        {
            XsdTreeNode temporalSpeedLimit = findActiveChild(override, "TemporalSpeedLimit");
            if (temporalSpeedLimit != null)
            {
                copyChoiceChild(temporalSpeedLimit, laneType, "TemporalSpeedLimit");
            }
        }
        for (XsdTreeNode child : override.getChildren())
        {
            if (!child.isActive() || !child.getNodeName().equals("GtuTypeSpeedLimit"))
            {
                continue;
            }
            XsdTreeNode existing = findActiveChild(laneType, "GtuTypeSpeedLimit",
                    child.getAttributeValueOrDefault("Id"));
            if (existing == null)
            {
                child.createCopy(laneType, laneType.getChildCount());
            }
            else
            {
                child.copyInto(existing);
            }
        }
    }

    /**
     * Applies the stripe overrides of the link to the copied stripes.
     * @param custom copied custom road layout.
     * @param overrides stripe overrides, by stripe id.
     */
    private static void applyStripeOverrides(final XsdTreeNode custom, final Map<String, XsdTreeNode> overrides)
    {
        for (Map.Entry<String, XsdTreeNode> entry : overrides.entrySet())
        {
            XsdTreeNode stripe = findElement(custom, "Stripe", entry.getKey());
            if (stripe == null)
            {
                continue;
            }
            XsdTreeNode definedStripe = findActiveChild(entry.getValue(), "DefinedStripe");
            if (definedStripe != null)
            {
                XsdTreeNode target = setChoiceOption(stripe, "DefinedStripe");
                if (target != null)
                {
                    definedStripe.copyInto(target);
                }
                continue;
            }
            XsdTreeNode overrideCustom = findActiveChild(entry.getValue(), "Custom");
            if (overrideCustom == null)
            {
                continue;
            }
            XsdTreeNode target = findActiveChild(stripe, "Custom");
            if (target == null)
            {
                Optional<XsdTreeNode> stripeType = findStripeType(stripe);
                if (stripeType.isEmpty() && findActiveChild(overrideCustom, "Elements") == null)
                {
                    continue;
                }
                target = setChoiceOption(stripe, "Custom");
                if (target == null)
                {
                    continue;
                }
                if (stripeType.isPresent())
                {
                    copyStripeType(stripeType.get(), target);
                }
            }
            applyCustomStripeOverride(overrideCustom, target);
        }
    }

    /**
     * Copies the definition of a defined stripe into a custom stripe option, mimicking the network parser.
     * @param stripeType stripe type definition.
     * @param custom custom stripe option.
     */
    private static void copyStripeType(final XsdTreeNode stripeType, final XsdTreeNode custom)
    {
        XsdTreeNode elements = findActiveChild(stripeType, "Elements");
        if (elements != null)
        {
            elements.createCopy(custom, custom.getChildCount());
        }
        XsdTreeNode dashOffset = findActiveChild(stripeType, "DashOffset");
        if (dashOffset != null)
        {
            dashOffset.createCopy(custom, custom.getChildCount());
        }
        for (String attribute : STRIPE_ATTRIBUTES)
        {
            String value = stripeType.getAttributeValueOrDefault(attribute);
            if (value != null)
            {
                custom.setAttributeValue(attribute, value);
            }
        }
    }

    /**
     * Applies the custom part of a stripe override to a custom stripe option.
     * @param override custom override.
     * @param custom custom stripe option.
     */
    private static void applyCustomStripeOverride(final XsdTreeNode override, final XsdTreeNode custom)
    {
        XsdTreeNode elements = findActiveChild(override, "Elements");
        if (elements != null)
        {
            copyChild(elements, custom, "Elements");
        }
        XsdTreeNode dashOffset = findActiveChild(override, "DashOffset");
        if (dashOffset != null)
        {
            copyChild(dashOffset, custom, "DashOffset");
        }
        for (XsdTreeNode child : override.getChildren())
        {
            if (child.isActive() && child.getNodeName().equals("Compatibility"))
            {
                child.createCopy(custom, custom.getChildCount());
            }
        }
        for (String attribute : STRIPE_ATTRIBUTES)
        {
            String value = override.getAttributeValue(attribute);
            if (value != null)
            {
                custom.setAttributeValue(attribute, value);
            }
        }
    }

    /**
     * Returns the stripe type referred to by a stripe.
     * @param stripe stripe.
     * @return referred stripe type, if found.
     */
    private static Optional<XsdTreeNode> findStripeType(final XsdTreeNode stripe)
    {
        XsdTreeNode defined = findActiveChild(stripe, "DefinedStripe");
        if (defined == null)
        {
            return Optional.empty();
        }
        Optional<XsdTreeNode> coupled = defined.getCoupledNodeValue();
        if (coupled.isPresent())
        {
            return coupled;
        }
        return findDefinition(stripe.getRoot(), XsdPaths.DEFINITIONS + ".StripeTypes.StripeType",
                defined.getValueOrDefault());
    }

    /**
     * Copies a child with given name into the target parent, replacing an active child with that name if present.
     * @param source source child.
     * @param targetParent target parent.
     * @param name name of the child.
     */
    private static void copyChild(final XsdTreeNode source, final XsdTreeNode targetParent, final String name)
    {
        XsdTreeNode target = findActiveChild(targetParent, name);
        if (target == null)
        {
            source.createCopy(targetParent, targetParent.getChildCount());
        }
        else
        {
            source.copyInto(target);
        }
    }

    /**
     * Copies a choice option into the target parent, selecting the option with the given name in that parent.
     * @param source source option.
     * @param targetParent target parent.
     * @param name name of the option.
     */
    private static void copyChoiceChild(final XsdTreeNode source, final XsdTreeNode targetParent, final String name)
    {
        XsdTreeNode target = setChoiceOption(targetParent, name);
        if (target == null)
        {
            source.createCopy(targetParent, targetParent.getChildCount());
        }
        else
        {
            source.copyInto(target);
        }
    }

    /**
     * Selects and activates the option with given name in a parent node.
     * @param parent parent node.
     * @param name option name.
     * @return selected option, {@code null} if no such option is present.
     */
    private static XsdTreeNode setChoiceOption(final XsdTreeNode parent, final String name)
    {
        for (XsdTreeNode child : parent.getChildren())
        {
            for (XsdOption option : child.getOptions())
            {
                if (option.optionNode().getNodeName().equals(name))
                {
                    child.setOption(option.optionNode());
                    option.optionNode().setActive();
                    return option.optionNode();
                }
            }
        }
        return null;
    }

    /**
     * Returns the first active child with given name.
     * @param parent parent node.
     * @param name child name.
     * @return active child, {@code null} if not present.
     */
    private static XsdTreeNode findActiveChild(final XsdTreeNode parent, final String name)
    {
        for (XsdTreeNode child : parent.getChildren())
        {
            if (child.isActive() && child.getNodeName().equals(name))
            {
                return child;
            }
        }
        return null;
    }

    /**
     * Returns the first active child with given name and id.
     * @param parent parent node.
     * @param name child name.
     * @param id child id.
     * @return active child, {@code null} if not present.
     */
    private static XsdTreeNode findActiveChild(final XsdTreeNode parent, final String name, final String id)
    {
        if (id == null)
        {
            return null;
        }
        for (XsdTreeNode child : parent.getChildren())
        {
            if (child.isActive() && child.getNodeName().equals(name)
                    && id.equals(child.getAttributeValueOrDefault("Id")))
            {
                return child;
            }
        }
        return null;
    }

    /**
     * Finds a named and identifiable element, nested anywhere below the given parent.
     * @param parent parent node.
     * @param name element name.
     * @param id element id.
     * @return element, {@code null} if not present.
     */
    private static XsdTreeNode findElement(final XsdTreeNode parent, final String name, final String id)
    {
        if (id == null)
        {
            return null;
        }
        for (XsdTreeNode child : parent.getChildren())
        {
            if (child.isActive() && child.getNodeName().equals(name)
                    && id.equals(child.getAttributeValueOrDefault("Id")))
            {
                return child;
            }
            XsdTreeNode found = findElement(child, name, id);
            if (found != null)
            {
                return found;
            }
        }
        return null;
    }

    /**
     * Finds a definition node by path and id.
     * @param root root node.
     * @param pathString path string of the definition.
     * @param id definition id.
     * @return definition, empty if not present.
     */
    private static Optional<XsdTreeNode> findDefinition(final XsdTreeNode root, final String pathString,
            final String id)
    {
        if (id == null)
        {
            return Optional.empty();
        }
        for (XsdTreeNode child : root.getChildren())
        {
            if (child.isActive() && child.getPathString().equals(pathString)
                    && id.equals(child.getAttributeValueOrDefault("Id")))
            {
                return Optional.of(child);
            }
            Optional<XsdTreeNode> found = findDefinition(child, pathString, id);
            if (found.isPresent())
            {
                return found;
            }
        }
        return Optional.empty();
    }

    /**
     * Finds the first non-included node with given path.
     * @param node node to search from.
     * @param pathString path string.
     * @return node, {@code null} if not present.
     */
    private static XsdTreeNode findNode(final XsdTreeNode node, final String pathString)
    {
        if (node.getPathString().equals(pathString) && !node.isIncluded())
        {
            return node;
        }
        for (XsdTreeNode child : node.getChildren())
        {
            if (child.isIncluded())
            {
                continue;
            }
            XsdTreeNode found = findNode(child, pathString);
            if (found != null)
            {
                return found;
            }
        }
        return null;
    }

    /**
     * Collects the ids of all identifiable nodes of given path.
     * @param node node to search from.
     * @param pathString path string.
     * @return used ids.
     */
    private static Set<String> collectIds(final XsdTreeNode node, final String pathString)
    {
        Set<String> ids = new HashSet<>();
        collectIds(node, pathString, ids);
        return ids;
    }

    /**
     * Collects the ids of all identifiable nodes of given path.
     * @param node node to search from.
     * @param pathString path string.
     * @param ids ids collected so far.
     */
    private static void collectIds(final XsdTreeNode node, final String pathString, final Set<String> ids)
    {
        if (node.isActive() && node.getPathString().equals(pathString) && node.isIdentifiable())
        {
            String id = node.getId();
            if (id != null)
            {
                ids.add(id);
            }
        }
        for (XsdTreeNode child : node.getChildren())
        {
            collectIds(child, pathString, ids);
        }
    }

    /**
     * Returns an id that is not yet used.
     * @param baseId id of the original definition.
     * @param elementId id of the lane or stripe for which a definition is created.
     * @param usedIds ids already used.
     * @return unique id.
     */
    private static String uniqueId(final String baseId, final String elementId, final Set<String> usedIds)
    {
        String base = baseId == null || baseId.isBlank() ? "LaneType" : baseId;
        String element = elementId == null || elementId.isBlank() ? "override" : elementId;
        String candidate = base + "." + element + ".override";
        int index = 1;
        while (usedIds.contains(candidate))
        {
            candidate = base + "." + element + ".override." + index;
            index++;
        }
        usedIds.add(candidate);
        return candidate;
    }

}
