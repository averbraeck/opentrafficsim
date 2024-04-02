package org.opentrafficsim.editor.decoration.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.editor.DocumentReader;
import org.opentrafficsim.editor.XsdTreeNode;
import org.w3c.dom.Node;

/**
 * Validator for xsd:keyref, which allows to define multiple fields. This class will maintain a list of nodes (fed by an
 * external listener) and validate that the field values are, as a set, within the given {@code KeyValidator}.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class KeyrefValidator extends XPathValidator implements CoupledValidator
{

    /** Key that is referred to by an xsd:keyref. */
    private final KeyValidator refer;

    /** Nodes who's value has to match some field in this validator. */
    private final Set<XsdTreeNode> valueValidating = new LinkedHashSet<>();

    /** Nodes who's attribute has to match some field in this validator, grouped per attribute name. */
    private final Map<String, Set<XsdTreeNode>> attributeValidating = new LinkedHashMap<>();

    /** Mapping of node coupled by this keyref validator, to the key node its coupled with in some other key validator. */
    private final Map<XsdTreeNode, XsdTreeNode> coupledKeyrefNodes = new LinkedHashMap<>();

    /**
     * Constructor.
     * @param keyNode Node; node defining the xsd:keyref.
     * @param keyPath String; path where the keyref was defined.
     * @param refer KeyValidator; key that is referred to by this xsd:keyref.
     */
    public KeyrefValidator(final Node keyNode, final String keyPath, final KeyValidator refer)
    {
        super(keyNode, keyPath);
        Throw.when(!keyNode.getNodeName().equals("xsd:keyref"), IllegalArgumentException.class,
                "The given node is not an xsd:keyref node.");
        Throw.whenNull(refer, "Refer validator may not be null.");
        String referName = DocumentReader.getAttribute(keyNode, "refer").replace("ots:", "");
        Throw.when(!referName.equals(refer.getKeyName()), IllegalArgumentException.class,
                "The key node refers to key/unique %s, but the provided refer validator has name %s.", referName,
                refer.getKeyName());
        this.refer = refer;
        refer.addListeningKeyrefValidator(this);
    }

    /** {@inheritDoc} */
    @Override
    public void addNode(final XsdTreeNode node)
    {
        for (int fieldIndex = 0; fieldIndex < this.fields.size(); fieldIndex++)
        {
            Field field = this.fields.get(fieldIndex);
            int pathIndex = field.getValidPathIndex(node);
            if (pathIndex >= 0)
            {
                String path = field.getFieldPath(pathIndex);
                int attr = path.indexOf("@");
                if (attr < 0)
                {
                    node.addValueValidator(this, field);
                    this.valueValidating.add(node);
                }
                else
                {
                    String attribute = path.substring(attr + 1);
                    node.addAttributeValidator(attribute, this, field);
                    this.attributeValidating.computeIfAbsent(attribute, (n) -> new LinkedHashSet<>()).add(node);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void removeNode(final XsdTreeNode node)
    {
        this.valueValidating.remove(node);
        this.attributeValidating.values().forEach((s) -> s.remove(node));
        this.coupledKeyrefNodes.remove(node);
    }

    /** {@inheritDoc} */
    @Override
    public String validate(final XsdTreeNode node)
    {
        if (node.getParent() == null)
        {
            return null; // Node was deleted, but is still visible in the GUI tree for a moment
        }
        List<String> values = gatherFieldValues(node);
        if (values.stream().allMatch((v) -> v == null))
        {
            return null;
        }
        // xsd:keyref referred value is present ?
        Map<XsdTreeNode, List<String>> valueMap = this.refer.getAllValueSets(node);
        XsdTreeNode matched = null;
        for (Entry<XsdTreeNode, List<String>> entry : valueMap.entrySet())
        {
            if (matchingKeyref(entry.getValue(), values))
            {
                if (matched != null)
                {
                    // duplicate match based on subset of values (there are null's), do not couple but also do not invalidate
                    this.coupledKeyrefNodes.remove(node);
                    return null;
                }
                matched = entry.getKey();
            }
        }
        if (matched != null)
        {
            this.coupledKeyrefNodes.put(node, matched);
            return null;
        }
        // not matched
        this.coupledKeyrefNodes.remove(node);
        String[] types = this.refer.getSelectorTypeString();
        String typeString = user(types.length == 1 ? types[0] : Arrays.asList(types).toString());
        if (values.size() == 1)
        {
            String value = values.get(0);
            String name = user(this.fields.get(0).getFullFieldName());
            return "Value " + value + " for " + name + " does not refer to a known and unique " + typeString + " within "
                    + this.keyPath + ".";
        }
        values.removeIf((value) -> value != null && value.startsWith("{") && value.endsWith("}")); // expressions
        return "Values " + values + " do not refer to a known and unique " + typeString + " within " + this.keyPath + ".";
    }

    /**
     * Checks that a set of values in a key, matches the values in a keyref node. Note, in dealing with null values the two sets
     * should <b>not</b> be given in the wrong order. It does not matter whether the keyref refers to a key or unique. In both
     * cases the values from a keyref are a match if all its non-null values match respective values in the key.
     * @param keyValues List&lt;String&gt;; set of values from a key node.
     * @param keyrefValues List&lt;String&gt;; set of values from a keyref node.
     * @return boolean; whether the key values match the keyref values.
     */
    private boolean matchingKeyref(final List<String> keyValues, final List<String> keyrefValues)
    {
        for (int i = 0; i < keyValues.size(); i++)
        {
            if (keyrefValues.get(i) != null && !keyrefValues.get(i).equals(keyValues.get(i)))
            {
                return false;
            }
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getOptions(final XsdTreeNode node, final Object field)
    {
        /*
         * We gather values from the referred xsd:key, drawing the appropriate context from the node relevant somewhere in the
         * xsd:keyref context. The xsd:keyref may not have a more specific context than the xsd:key. If the xsd:keyref has a
         * bigger context, there may be only one instance of a more specific context of the xsd:key. If both contexts are the
         * same, this is trivially ok.
         */
        XsdTreeNode contextKeyref = getContext(node);
        XsdTreeNode contextKey = this.refer.getContext(node); // can be null when out of context
        boolean uniqueScope = contextKeyref.equals(contextKey);
        if (!uniqueScope)
        {
            List<XsdTreeNode> contextKeyPath = contextKey.getPath();
            if (contextKeyPath.contains(contextKeyref)) // xsd:key more specific, i.e. longer path
            {
                contextKeyPath.removeAll(contextKeyref.getPath());
                Set<XsdTreeNode> containedKeyScopes = new LinkedHashSet<>();
                gatherScopes(contextKeyref, contextKeyPath, containedKeyScopes);
                uniqueScope = containedKeyScopes.size() == 1;
            }
        }
        if (!uniqueScope)
        {
            return null;
        }
        Map<XsdTreeNode, List<String>> values = this.refer.getAllValueSets(node);
        List<String> result = new ArrayList<>(values.size());
        int index = this.fields.indexOf(field);
        values.forEach((n, list) -> result.add(list.get(index)));
        result.removeIf((v) -> v == null || v.isEmpty());
        return result;
    }

    /**
     * Gathers xsd:key-level contexts from an xsd:keyref context that is larger than the key's.
     * @param node XsdTreeNode; current node to browse the children of, or return in the set.
     * @param remainingPath List&lt;XsdTreeNode&gt;; remaining intermediate levels, starting with the sub-level of the keyref.
     * @param set Set&lt;XsdTreeNode&gt;; set to gather contexts in.
     */
    private void gatherScopes(final XsdTreeNode node, final List<XsdTreeNode> remainingPath, final Set<XsdTreeNode> set)
    {
        String path = node.getPathString() + "." + remainingPath.get(0);
        for (XsdTreeNode child : node.getChildren())
        {
            if (child.getPathString().equals(path))
            {
                if (remainingPath.size() == 1)
                {
                    set.add(child);
                }
                else
                {
                    gatherScopes(child, remainingPath.subList(1, remainingPath.size()), set);
                }
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public XsdTreeNode getCoupledKeyrefNode(final XsdTreeNode node)
    {
        return this.coupledKeyrefNodes.get(node);
    }

    /**
     * Update value as it was changed at the key.
     * @param node XsdTreeNode; node on which the value was changed.
     * @param fieldIndex int; index of field that was changed.
     * @param newValue String; new value.
     */
    public void updateFieldValue(final XsdTreeNode node, final int fieldIndex, final String newValue)
    {
        for (Entry<XsdTreeNode, XsdTreeNode> entry : this.coupledKeyrefNodes.entrySet())
        {
            if (entry.getValue().equals(node))
            {
                if (this.valueValidating.contains(entry.getKey()))
                {
                    CoupledValidator.setValueIfNotNull(entry.getKey(), newValue);
                }
                else
                {
                    for (Entry<String, Set<XsdTreeNode>> attrEntry : this.attributeValidating.entrySet())
                    {
                        if (attrEntry.getValue().contains(entry.getKey()))
                        {
                            int index = this.fields.get(fieldIndex).getValidPathIndex(entry.getKey());
                            if (index >= 0)
                            {
                                String field = this.fields.get(fieldIndex).getFieldPath(index);
                                int attr = field.indexOf("@");
                                String attribute = field.substring(attr + 1);
                                CoupledValidator.setAttributeIfNotNull(entry.getKey(), attribute, newValue);
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Invalidates all nodes that are validating through this keyref. Used by a key validator to notify when nodes are added or
     * removed, activated or deactivated, or an attribute or value is changed, possibly making any keyref valid or invalid.
     */
    public void invalidateNodes()
    {
        for (XsdTreeNode node : this.valueValidating)
        {
            node.invalidate();
        }
        for (Set<XsdTreeNode> set : this.attributeValidating.values())
        {
            for (XsdTreeNode node : set)
            {
                node.invalidate();
            }
        }
    }

}
