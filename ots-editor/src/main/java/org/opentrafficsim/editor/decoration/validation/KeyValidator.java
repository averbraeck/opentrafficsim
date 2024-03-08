package org.opentrafficsim.editor.decoration.validation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.opentrafficsim.editor.XsdTreeNode;
import org.w3c.dom.Node;

/**
 * Validator for xsd:key and xsd:unique. Functionality these are very similar, with both allowing to define multiple fields.
 * They register with the right nodes in the same way. Both xsd:key or xsd:unique will check a range of values for uniqueness
 * and only differ in whether all values need to be present. This class will maintain a list of nodes (fed by an external
 * listener) and validate against field uniqueness over those nodes.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class KeyValidator extends XPathValidator implements EventListener
{

    /** */
    private static final long serialVersionUID = 20230912L;

    /** Key validators (xsd:keyref) that are using this key validator (xsd:key) to validate. */
    private Set<KeyrefValidator> listeningKeyrefValidators = new LinkedHashSet<>();

    /** Nodes maintained for an xsd:key or xsd:unique. */
    protected final Map<XsdTreeNode, Set<XsdTreeNode>> nodes = new LinkedHashMap<>();

    /**
     * Constructor.
     * @param keyNode Node; node defining the xsd:key or xsd:unique.
     * @param keyPath String; path where the key was defined.
     */
    public KeyValidator(final Node keyNode, final String keyPath)
    {
        super(keyNode, keyPath);
    }

    /** {@inheritDoc} */
    @Override
    public String validate(final XsdTreeNode node)
    {
        if (node.getPath().size() == 1)
        {
            return null; // Node was deleted, but is still visible in the GUI tree for a moment
        }
        List<String> values = gatherFields(node);
        // xsd:key; all must be present
        if (this.keyNode.getNodeName().equals("xsd:key") && values.contains(null))
        {
            List<String> missing = new ArrayList<>();
            for (int i = 0; i < values.size(); i++)
            {
                if (values.get(i) == null)
                {
                    if (i < this.attributeNames.size())
                    {
                        missing.add(this.attributeNames.get(i));
                    }
                    else if (i < this.attributeNames.size() + this.childNames.size())
                    {
                        missing.add(this.childNames.get(i - this.attributeNames.size()));
                    }
                    else
                    {
                        missing.add("Value");
                    }
                }
            }
            if (missing.size() == 1)
            {
                return "Insufficient number of values, missing " + missing.get(0) + ".";
            }
            return "Insufficient number of values, missing " + missing + ".";
        }
        // xsd:key or xsd:unique; all must be present allowing null==null on xsd:unique as was captured above for xsd:key.
        if (Collections.frequency(getValues(node).values(), values) > 1)
        {
            if (this.childNames.size() + this.attributeNames.size() == 1)
            {
                String name = this.attributeNames.isEmpty() ? this.childNames.get(0) : this.attributeNames.get(0);
                return "Value " + values.get(0) + " for " + name + " is not unique within " + this.keyPath + ".";
            }
            return "Values " + values + " are not unique within " + this.keyPath + ".";
        }
        return null;
    }

    /** {@inheritDoc} */
    @Override
    public void addNode(final XsdTreeNode node)
    {
        boolean isType = false;
        for (String path : getTypeString())
        {
            isType = node.isType(getPath().equals("Ots") ? path : getPath() + "." + path);
            if (isType)
            {
                break;
            }
        }
        if (isType)
        {
            node.addListener(this, XsdTreeNode.ACTIVATION_CHANGED);
            XsdTreeNode context = getContext(node);
            this.nodes.computeIfAbsent(context, (key) -> new LinkedHashSet<>()).add(node);
            invalidateAllDependent();
            if (this.includeSelfValue)
            {
                node.addValueValidator(this, XPathFieldType.VALUE);
                node.addListener(this, XsdTreeNode.VALUE_CHANGED);
            }
            if (!this.attributeNames.isEmpty())
            {
                for (String attribute : this.attributeNames)
                {
                    node.addAttributeValidator(attribute, this);
                }
                node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
            }
        }
        for (String child : this.childNames)
        {
            for (String path : getTypeString())
            {
                String fullPath = path + "." + child;
                if (node.getPathString().endsWith(fullPath))
                {
                    node.addValueValidator(this, XPathFieldType.CHILD);
                    if (!this.listeningKeyrefValidators.isEmpty())
                    {
                        node.addListener(this, XsdTreeNode.VALUE_CHANGED);
                        node.addListener(this, XsdTreeNode.ACTIVATION_CHANGED);
                    }
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removeNode(final XsdTreeNode node)
    {
        removeNodeKeepListening(node);
        node.removeListener(this, XsdTreeNode.VALUE_CHANGED);
        node.removeListener(this, XsdTreeNode.ACTIVATION_CHANGED);
        node.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
    }

    /**
     * Remove node. It is removed from all contexts and listening keyrefs. This method is called indirectly by a listener that
     * the root node has set up, for every removed node. This method is called internally for children of deactivated nodes, in
     * which case we do not want to remove this validator as listener on the node, for when it gets activated later.
     * @param node XsdTreeNode; node to remove.
     */
    private void removeNodeKeepListening(final XsdTreeNode node)
    {
        for (KeyrefValidator keyref : this.listeningKeyrefValidators)
        {
            keyref.removeNodeAsValidating(node);
        }
        for (Set<XsdTreeNode> set : this.nodes.values())
        {
            if (set.contains(node))
            {
                invalidateAllDependent();
                set.remove(node);
            }
        }
        if (!this.listeningKeyrefValidators.isEmpty())
        {
            node.removeListener(this, XsdTreeNode.VALUE_CHANGED);
            node.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
        }
    }

    /**
     * Returns the present values of the fields for each node within the given context.
     * @param node XsdTreeNode; node that is in the right context.
     * @return Map&lt;XsdTreeNode, List&lt;String&gt;&gt;; list of all values per key node.
     */
    protected Map<XsdTreeNode, List<String>> getValues(final XsdTreeNode node)
    {
        XsdTreeNode context = getContext(node);
        Map<XsdTreeNode, List<String>> map = new LinkedHashMap<>();
        for (XsdTreeNode otherNode : this.nodes.computeIfAbsent(context, (key) -> new LinkedHashSet<>()))
        {
            if (otherNode.isActive())
            {
                map.put(otherNode, gatherFields(otherNode));
            }
        }
        return map;
    }

    /** {@inheritDoc} */
    @Override
    public void notify(final Event event) throws RemoteException
    {
        for (Set<XsdTreeNode> nodes : this.nodes.values())
        {
            for (XsdTreeNode node : nodes)
            {
                node.invalidate();
            }
        }
        if (XsdTreeNode.ACTIVATION_CHANGED.equals(event.getType()))
        {
            Object[] content = (Object[]) event.getContent();
            XsdTreeNode node = (XsdTreeNode) content[0];
            boolean active = (boolean) content[1];
            activationChanged(node, active, true);
            invalidateAllDependent();
        }
        else if (XsdTreeNode.VALUE_CHANGED.equals(event.getType()))
        {
            Object[] content = (Object[]) event.getContent();
            XsdTreeNode keyNode = (XsdTreeNode) content[0];
            String previous = (String) content[1];
            boolean updateKeyrefs = !duplicateKeys(keyNode, (n) -> n.getValue(), previous);
            if (updateKeyrefs)
            {
                updateReferringKeyrefs(keyNode, this.attributeNames.size() + this.childNames.size(), keyNode.getValue());
            }
            invalidateAllDependent();
        }
        else if (XsdTreeNode.ATTRIBUTE_CHANGED.equals(event.getType()))
        {
            Object[] content = (Object[]) event.getContent();
            String attribute = (String) content[1];
            if (!this.attributeNames.contains(attribute))
            {
                return;
            }
            XsdTreeNode keyNode = (XsdTreeNode) content[0];
            String previous = (String) content[2];
            boolean updateKeyrefs = !duplicateKeys(keyNode, (n) -> n.getAttributeValue(attribute), previous);
            if (updateKeyrefs)
            {
                updateReferringKeyrefs(keyNode, this.attributeNames.indexOf(attribute), keyNode.getAttributeValue(attribute));
            }
            invalidateAllDependent();
        }
    }

    /**
     * Returns whether there are or were duplicate keys such that no key change should result in a change of value at the
     * keyrefs.
     * @param keyNode XsdTreeNode; node where key is changed.
     * @param valueProvider Function&lt;XsdTreeNode, String&gt;; function to provide the right value from the key nodes.
     * @param previous String; previous value.
     * @return boolean; whether there are duplicate keys.
     */
    // TODO: keyref could refer to key with multiple fields
    private boolean duplicateKeys(final XsdTreeNode keyNode, final Function<XsdTreeNode, String> valueProvider,
            final String previous)
    {
        Set<XsdTreeNode> keyNodes = this.nodes.get(getContext(keyNode));
        boolean duplicates = false;
        if (keyNodes != null)
        {
            Set<String> values = new LinkedHashSet<>();
            for (XsdTreeNode node : keyNodes)
            {
                if (node.isActive())
                {
                    String value = valueProvider.apply(node);
                    if (value != null)
                    {
                        duplicates = duplicates || !values.add(value) || value.equals(previous);
                    }
                }
            }
        }
        return duplicates;
    }

    /**
     * Recursively removes or adds the children from an activated or deactivated node to/from this key. Children of a
     * deactivated node no longer have valid key values. Only active nodes are considered. However, when a node gets
     * deactivated, its children should be removed too. The argument {@code forceDoChildren} is {@code true} in that case.
     * @param node XsdTreeNode; node to remove or add.
     * @param active boolean; when node was activated, child nodes are add. Otherwise removed.
     * @param forceDoChildren boolean; {@code true} on the originally (de)activated node.
     */
    private void activationChanged(final XsdTreeNode node, final boolean active, final boolean forceDoChildren)
    {
        if (active)
        {
            addNode(node);
        }
        else
        {
            removeNodeKeepListening(node);
        }
        if (node.isActive() || forceDoChildren)
        {
            for (XsdTreeNode child : node.getChildren())
            {
                activationChanged(child, active, false);
            }
        }
    }

    /**
     * Update value in nodes that refer with xsd:keyref to a value that was changed.
     * @param node XsdTreeNode; node on which the value was changed.
     * @param fieldIndex int; index of field that was changed.
     * @param newValue String; new value.
     */
    private void updateReferringKeyrefs(final XsdTreeNode node, final int fieldIndex, final String newValue)
    {
        for (KeyrefValidator validator : this.listeningKeyrefValidators)
        {
            validator.updateFieldValue(node, fieldIndex, newValue);
        }
    }

    /**
     * Invalidates all nodes that depend on this key, as a key node was added, removed, or made inactive.
     */
    private void invalidateAllDependent()
    {
        for (KeyrefValidator keyref : this.listeningKeyrefValidators)
        {
            keyref.invalidateNodes();
        }
    }

    /**
     * Adds a keyref validator as listening to this key.
     * @param keyrefValidator KeyrefValidator; keyref validator.
     */
    public void addListeningKeyrefValidator(final KeyrefValidator keyrefValidator)
    {
        this.listeningKeyrefValidators.add(keyrefValidator);
    }

}
