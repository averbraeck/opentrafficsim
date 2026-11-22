package org.opentrafficsim.editor.decoration;

import java.rmi.RemoteException;
import java.util.ArrayList;
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
 * Allows a defined road layout selected at a link, to be copied in to a customizable road layout at the link. The copy is made
 * by node name rather than by node type, so definition nodes and network nodes may have different XSD types. Overrides are
 * applied to the copied nodes using the same generic copying mechanism.
 * <p>
 * Copyright (c) 2023-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public class LayoutCustomizer extends AbstractNodeDecoratorRemove implements EventListener, Consumer<XsdTreeNode>
{
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
        XsdTreeNode template = null;
        for (XsdTreeNode child : formerChildren)
        {
            if (child.isActive() && !isStructural(child))
            {
                template = child;
            }
        }
        Set<XsdTreeNode> createdNodes = new HashSet<>();
        this.editor.getUndo().startAction(ActionType.ACTION, node, null);
        for (XsdTreeNode definedChild : defined.get().getChildren())
        {
            if (!definedChild.isActive() || isStructural(definedChild))
            {
                continue;
            }
            XsdTreeNode copiedChild = createLayoutElement(custom, definedChild.getNodeName(), template);
            if (copiedChild != null)
            {
                copyState(definedChild, copiedChild);
                createdNodes.add(copiedChild);
                template = copiedChild;
            }
        }
        for (XsdTreeNode formerChild : formerChildren)
        {
            if (!createdNodes.contains(formerChild))
            {
                formerChild.remove();
            }
        }
        copyAttributes(defined.get(), custom, Set.of("Id", "Builtin"));
        applyElementOverrides(custom, "Lane", laneOverrides);
        applyElementOverrides(custom, "Stripe", stripeOverrides);
        this.editor.getUndo().startAction(ActionType.OPTION, node, null);
        node.setOption(custom);
        this.editor.show(custom, null);
    }

    /**
     * Applies overrides to copied elements with the given node name.
     * @param custom copied custom road layout.
     * @param elementName node name of the elements to override.
     * @param overrides overrides, by element id.
     */
    private static void applyElementOverrides(final XsdTreeNode custom, final String elementName,
            final Map<String, XsdTreeNode> overrides)
    {
        for (Map.Entry<String, XsdTreeNode> entry : overrides.entrySet())
        {
            XsdTreeNode element = findElement(custom, elementName, entry.getKey());
            if (element != null)
            {
                applyOverride(element, entry.getValue());
            }
        }
    }

    /**
     * Applies one override by selecting and filling the corresponding choice option in the target element.
     * @param target copied layout element.
     * @param override override node.
     */
    private static void applyOverride(final XsdTreeNode target, final XsdTreeNode override)
    {
        for (XsdTreeNode sourceOption : override.getChildren())
        {
            if (!sourceOption.isActive() || isStructural(sourceOption))
            {
                continue;
            }
            XsdTreeNode targetOption = selectChoiceOption(target, sourceOption.getNodeName());
            if (targetOption != null)
            {
                copyState(sourceOption, targetOption);
            }
            return;
        }
    }

    /**
     * Creates the next Stripe, Lane or Shoulder element in a road layout. The new element is created through the target's own
     * choice structure, so it uses the network XSD types, and is appended after the template to preserve the source order.
     * @param parent custom road layout.
     * @param name node name of the element to create.
     * @param template last created layout element, {@code null} if the parent has no element yet.
     * @return created element, {@code null} if the requested option is not available.
     */
    private static XsdTreeNode createLayoutElement(final XsdTreeNode parent, final String name, final XsdTreeNode template)
    {
        XsdTreeNode created;
        if (template == null)
        {
            created = selectChoiceOption(parent, name);
        }
        else
        {
            created = template.add();
            if (!created.getNodeName().equals(name))
            {
                XsdTreeNode option = findOption(created, name);
                if (option == null)
                {
                    return null;
                }
                created.setOption(option);
                created = created.getOption();
            }
        }
        if (created != null)
        {
            created.setActive();
        }
        return created;
    }

    /**
     * Returns the option with the given name in the choice that the given node is part of.
     * @param node node that is part of a choice.
     * @param name option name.
     * @return option node, {@code null} if not present.
     */
    private static XsdTreeNode findOption(final XsdTreeNode node, final String name)
    {
        for (XsdOption option : node.getOptions())
        {
            if (option.optionNode().getNodeName().equals(name))
            {
                return option.optionNode();
            }
        }
        return null;
    }

    /**
     * Creates a node under the given parent using a node of the same name as structure template. This avoids copying definition
     * XSD types into a network layout, while retaining the node name and all state that the two types have in common.
     * @param parent parent node.
     * @param name node name.
     * @param templates nodes that may be used as structure templates.
     * @return newly created node, {@code null} if the parent has no structure for the requested name.
     */
    private static XsdTreeNode createNodeLike(final XsdTreeNode parent, final String name,
            final List<XsdTreeNode> templates)
    {
        for (XsdTreeNode template : templates)
        {
            if (template.getNodeName().equals(name))
            {
                return template.add();
            }
        }
        for (XsdTreeNode child : parent.getChildren())
        {
            if (child.getNodeName().equals(name))
            {
                return child.add();
            }
        }
        XsdOption option = findChoiceOption(parent, name);
        if (option == null)
        {
            return null;
        }
        boolean alreadyPresent = option.selected() && parent.getChildren().contains(option.optionNode());
        option.choice().setOption(option.optionNode());
        option.optionNode().setActive();
        return alreadyPresent ? option.optionNode().add() : option.optionNode();
    }

    /**
     * Copies activation, value, attributes and children by name. Nodes that have different XSD types but the same names are
     * treated as equivalent.
     * @param source source node.
     * @param target target node.
     */
    private static void copyState(final XsdTreeNode source, final XsdTreeNode target)
    {
        target.setActive();
        if (source.isEditable() && target.isEditable())
        {
            String value = source.getValue();
            if (value != null)
            {
                target.setValue(value);
            }
        }
        copyAttributes(source, target, Set.of());
        copyReferenceAttributes(source, target);
        copyChildren(source, target, new ArrayList<>());
    }

    /**
     * Copies active children by name. A source sequence that is a choice option is copied into the matching sequence option in
     * the target, after which its children are copied as normal.
     * @param source source node.
     * @param target target node.
     * @param usedChildren target children already used by an earlier source child, modified.
     */
    private static void copyChildren(final XsdTreeNode source, final XsdTreeNode target,
            final List<XsdTreeNode> usedChildren)
    {
        for (XsdTreeNode sourceChild : source.getChildren())
        {
            if (!sourceChild.isActive())
            {
                continue;
            }
            if (sourceChild.isSequence())
            {
                XsdTreeNode targetSequence = selectSequenceOption(target, sourceChild, usedChildren);
                if (targetSequence != null)
                {
                    usedChildren.add(targetSequence);
                    copyChildren(sourceChild, targetSequence, new ArrayList<>());
                }
                continue;
            }
            if (isStructural(sourceChild))
            {
                continue;
            }
            XsdTreeNode targetChild = findOrCreateChild(target, sourceChild.getNodeName(), usedChildren);
            if (targetChild != null)
            {
                usedChildren.add(targetChild);
                copyState(sourceChild, targetChild);
            }
        }
    }

    /**
     * Selects the target sequence option that can contain the same child names as the given source sequence.
     * @param target target node that owns the choice.
     * @param sourceSequence active source sequence option.
     * @param usedChildren target children already used by an earlier source child.
     * @return selected target sequence, {@code null} if no matching option exists.
     */
    private static XsdTreeNode selectSequenceOption(final XsdTreeNode target, final XsdTreeNode sourceSequence,
            final List<XsdTreeNode> usedChildren)
    {
        List<XsdTreeNode> sourceActivated = new ArrayList<>();
        Set<String> sourceNames = collectNames(sourceSequence, sourceActivated);
        deactivate(sourceActivated);
        for (XsdOption option : collectOptions(target))
        {
            XsdTreeNode candidate = option.optionNode();
            if (!candidate.isSequence() || usedChildren.contains(candidate))
            {
                continue;
            }
            List<XsdTreeNode> activated = new ArrayList<>();
            boolean matches = collectNames(candidate, activated).containsAll(sourceNames);
            deactivate(activated);
            if (matches)
            {
                option.choice().setOption(candidate);
                candidate.setActive();
                return candidate;
            }
        }
        return null;
    }

    /**
     * Returns an unused child with the given name, creating one if needed.
     * @param parent parent node.
     * @param name child name.
     * @param usedChildren children already used for a source child.
     * @return child, {@code null} if no matching child structure exists.
     */
    private static XsdTreeNode findOrCreateChild(final XsdTreeNode parent, final String name,
            final List<XsdTreeNode> usedChildren)
    {
        XsdTreeNode directChild = findUnusedChild(parent, name, usedChildren);
        if (directChild != null)
        {
            return directChild;
        }
        for (XsdOption option : collectOptions(parent))
        {
            XsdTreeNode candidate = option.optionNode();
            if (candidate.getNodeName().equals(name) && !usedChildren.contains(candidate))
            {
                option.choice().setOption(candidate);
                candidate.setActive();
                return candidate;
            }
        }
        for (XsdOption option : collectOptions(parent))
        {
            XsdTreeNode candidate = option.optionNode();
            if (!candidate.isSequence())
            {
                continue;
            }
            List<XsdTreeNode> activated = new ArrayList<>();
            boolean matches = collectNames(candidate, activated).contains(name);
            deactivate(activated);
            if (matches)
            {
                option.choice().setOption(candidate);
                candidate.setActive();
                return findOrCreateChild(candidate, name, usedChildren);
            }
        }
        return createNodeLike(parent, name, parent.getChildren());
    }

    /**
     * Returns an unused direct child with the given name.
     * @param parent parent node.
     * @param name child name.
     * @param usedChildren children already used by an earlier source child.
     * @return unused child, {@code null} if not present.
     */
    private static XsdTreeNode findUnusedChild(final XsdTreeNode parent, final String name,
            final List<XsdTreeNode> usedChildren)
    {
        for (XsdTreeNode child : parent.getChildren())
        {
            if (child.getNodeName().equals(name) && !usedChildren.contains(child))
            {
                return child;
            }
        }
        return null;
    }

    /**
     * Collects all choice options that are reachable from the given node, including options inside sequences.
     * @param node node to start from.
     * @return reachable options.
     */
    private static List<XsdOption> collectOptions(final XsdTreeNode node)
    {
        List<XsdOption> options = new ArrayList<>();
        Set<XsdTreeNode> seen = new HashSet<>();
        collectOptions(node, options, seen);
        return options;
    }

    /**
     * Recursively collects choice options that are reachable from the given node.
     * @param node node to inspect.
     * @param options collected options, modified.
     * @param seen nodes already inspected, modified.
     */
    private static void collectOptions(final XsdTreeNode node, final List<XsdOption> options,
            final Set<XsdTreeNode> seen)
    {
        if (!node.isActive())
        {
            return;
        }
        for (XsdTreeNode child : node.getChildren())
        {
            for (XsdOption option : child.getOptions())
            {
                if (seen.add(option.optionNode()))
                {
                    options.add(option);
                }
            }
            collectOptions(child, options, seen);
        }
    }

    /**
     * Collects element names that can occur below the given node, including names of unselected choice options.
     * @param node node to inspect.
     * @param activated nodes that were activated during collection, modified.
     * @return element names.
     */
    private static Set<String> collectNames(final XsdTreeNode node, final List<XsdTreeNode> activated)
    {
        Set<String> names = new HashSet<>();
        collectNames(node, names, activated, new HashSet<>());
        return names;
    }

    /**
     * Recursively collects element names that can occur below the given node.
     * @param node node to inspect.
     * @param names collected names, modified.
     * @param activated nodes that were activated during collection, modified.
     * @param seen nodes already inspected, modified.
     */
    private static void collectNames(final XsdTreeNode node, final Set<String> names,
            final List<XsdTreeNode> activated, final Set<XsdTreeNode> seen)
    {
        if (!seen.add(node))
        {
            return;
        }
        if (!node.isActive())
        {
            node.setActive();
            activated.add(node);
        }
        if (!isStructural(node))
        {
            names.add(node.getNodeName());
        }
        for (XsdTreeNode child : node.getChildren())
        {
            collectNames(child, names, activated, seen);
        }
        for (XsdOption option : node.getOptions())
        {
            collectNames(option.optionNode(), names, activated, seen);
        }
    }

    /**
     * Reverts nodes that were activated only to inspect their contents.
     * @param activated nodes to deactivate.
     */
    private static void deactivate(final List<XsdTreeNode> activated)
    {
        for (XsdTreeNode node : activated)
        {
            node.setInactive();
        }
    }

    /**
     * Copies attributes that both nodes have. Attributes only present in the source are ignored.
     * @param source source node.
     * @param target target node.
     * @param excluded attribute names not to copy.
     */
    private static void copyAttributes(final XsdTreeNode source, final XsdTreeNode target, final Set<String> excluded)
    {
        for (int index = 0; index < source.attributeCount(); index++)
        {
            String name = source.getAttributeNameByIndex(index);
            if (!excluded.contains(name) && target.hasAttribute(name))
            {
                String value = source.getAttributeValue(index);
                if (value != null)
                {
                    target.setAttributeValue(name, value);
                }
            }
        }
    }

    /**
     * Copies references that are attributes in the source, but choice elements in the target. For example {@code StripeType} in
     * a defined layout becomes {@code DefinedStripe} in a network layout. The child name is derived from the attribute name so
     * that similar references do not require dedicated code.
     * @param source source node.
     * @param target target node.
     */
    private static void copyReferenceAttributes(final XsdTreeNode source, final XsdTreeNode target)
    {
        for (int index = 0; index < source.attributeCount(); index++)
        {
            String name = source.getAttributeNameByIndex(index);
            if (target.hasAttribute(name) || !name.endsWith("Type"))
            {
                continue;
            }
            String childName = "Defined" + name.substring(0, name.length() - "Type".length());
            XsdTreeNode child = selectChoiceOption(target, childName);
            String value = source.getAttributeValue(index);
            if (child != null && value != null && child.isEditable())
            {
                child.setValue(value);
            }
        }
    }

    /**
     * Selects and activates the option with given name in a parent node.
     * @param parent parent node.
     * @param name option name.
     * @return selected option, {@code null} if no such option is present.
     */
    private static XsdTreeNode selectChoiceOption(final XsdTreeNode parent, final String name)
    {
        XsdOption option = findChoiceOption(parent, name);
        if (option == null)
        {
            return null;
        }
        option.choice().setOption(option.optionNode());
        option.optionNode().setActive();
        return option.optionNode();
    }

    /**
     * Finds a choice option with given name among the direct children of a parent.
     * @param parent parent node.
     * @param name option name.
     * @return option, {@code null} if not present.
     */
    private static XsdOption findChoiceOption(final XsdTreeNode parent, final String name)
    {
        for (XsdTreeNode child : parent.getChildren())
        {
            for (XsdOption option : child.getOptions())
            {
                if (option.optionNode().getNodeName().equals(name))
                {
                    return option;
                }
            }
        }
        return null;
    }

    /**
     * Returns whether a node is an XSD structure node that has no counterpart in the XML.
     * @param node node.
     * @return whether the node is structural.
     */
    private static boolean isStructural(final XsdTreeNode node)
    {
        return node.getNodeName().startsWith("xsd:") || node.getNodeName().startsWith("#");
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
            if (child.isActive())
            {
                if (child.getNodeName().equals(name) && id.equals(child.getId()))
                {
                    return child;
                }
                XsdTreeNode found = findElement(child, name, id);
                if (found != null)
                {
                    return found;
                }
            }
        }
        return null;
    }

}
