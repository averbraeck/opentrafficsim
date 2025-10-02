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
 * Validator for xsd:keyref, which allows to define multiple fields. This class will maintain a list of nodes (fed automatically
 * by an external listener) and validate that the field values are, as a set, within the given {@code KeyValidator}.
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
    private final Set<XsdTreeNode> valueValidatingKeyrefNodes = new LinkedHashSet<>();

    /** Nodes who's attribute has to match some field in this validator, grouped per attribute name. */
    private final Map<String, Set<XsdTreeNode>> attributeValidatingKeyrefNodes = new LinkedHashMap<>();

    /** Mapping of node coupled by this keyref validator, to the key node its coupled with in some other key validator. */
    private final Map<XsdTreeNode, XsdTreeNode> coupledNodes = new LinkedHashMap<>();

    /**
     * Constructor.
     * @param keyrefNode node defining the xsd:keyref.
     * @param keyrefPath path where the keyref was defined, defining the context.
     * @param refer key that is referred to by this xsd:keyref.
     */
    public KeyrefValidator(final Node keyrefNode, final String keyrefPath, final KeyValidator refer)
    {
        super(keyrefNode, keyrefPath);
        Throw.when(!keyrefNode.getNodeName().equals("xsd:keyref"), IllegalArgumentException.class,
                "The given node is not an xsd:keyref node.");
        Throw.whenNull(refer, "Refer validator may not be null.");
        String referName = DocumentReader.getAttribute(keyrefNode, "refer").replace("ots:", "");
        Throw.when(!referName.equals(refer.getKeyName()), IllegalArgumentException.class,
                "The key node refers to key/unique %s, but the provided refer validator has name %s.", referName,
                refer.getKeyName());
        this.refer = refer;
        refer.addListeningKeyrefValidator(this);
    }

    @Override
    public void addNode(final XsdTreeNode node)
    {
        for (int fieldIndex = 0; fieldIndex < getFields().size(); fieldIndex++)
        {
            Field field = getFields().get(fieldIndex);
            int pathIndex = field.getValidPathIndex(node);
            if (pathIndex >= 0)
            {
                String path = field.getFieldPath(pathIndex);
                int attr = path.indexOf("@");
                if (attr < 0)
                {
                    node.addValueValidator(this, field);
                    this.valueValidatingKeyrefNodes.add(node);
                }
                else
                {
                    String attribute = path.substring(attr + 1); // remove '@'
                    node.addAttributeValidator(attribute, this, field);
                    this.attributeValidatingKeyrefNodes.computeIfAbsent(attribute, (n) -> new LinkedHashSet<>()).add(node);
                }
            }
        }
    }

    @Override
    public void removeNode(final XsdTreeNode node)
    {
        this.valueValidatingKeyrefNodes.remove(node);
        this.attributeValidatingKeyrefNodes.values().forEach((s) -> s.remove(node));
        removeCoupling(node);
    }

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
            removeCoupling(node);
            return null;
        }
        // xsd:keyref referred value is present?
        Map<XsdTreeNode, List<String>> valueMap = this.refer.gatherFieldValuesInContext(node);
        XsdTreeNode matchedNode = null;
        for (Entry<XsdTreeNode, List<String>> entry : valueMap.entrySet())
        {
            if (matchingKeyref(entry.getValue(), values))
            {
                if (matchedNode != null)
                {
                    // duplicate match based on subset of values (there are null's), do not couple but also do not invalidate
                    removeCoupling(node);
                    return null;
                }
                matchedNode = entry.getKey();
            }
        }
        if (matchedNode != null)
        {
            addCoupling(node, matchedNode);
            return null;
        }
        // not matched
        removeCoupling(node);
        String[] types = this.refer.getSelectorTypeString();
        String typeString = userFriendlyXPath(types.length == 1 ? types[0] : Arrays.asList(types).toString());
        if (values.size() == 1)
        {
            return "Value " + values.get(0) + " for " + userFriendlyXPath(getFields().get(0).getFullFieldName())
                    + " does not refer to a known and unique " + typeString + " within " + getKeyPath() + ".";
        }
        values.removeIf((value) -> value != null && value.startsWith("{") && value.endsWith("}")); // remove expressions
        return "Values " + values + " do not refer to a known and unique " + typeString + " within " + getKeyPath() + ".";
    }

    /**
     * Checks that a set of values in a key, matches the values in a keyref node. Note, in dealing with null values the two sets
     * should <b>not</b> be given in the wrong order. It does not matter whether the keyref refers to a key or unique. In both
     * cases the values from a keyref are a match if all its non-null values match respective values in the key. Non-null
     * requirements of a key pertain to the key itself, not the matching here.
     * @param keyValues set of values from a key node.
     * @param keyrefValues set of values from a keyref node.
     * @return whether the key values match the keyref values.
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
        Map<XsdTreeNode, List<String>> values = this.refer.gatherFieldValuesInContext(node);
        List<String> result = new ArrayList<>(values.size());
        int index = getFields().indexOf(field);
        values.forEach((n, list) -> result.add(list.get(index)));
        result.removeIf((v) -> v == null || v.isEmpty());
        return result;
    }

    /**
     * Gathers xsd:key-level contexts from an xsd:keyref context that is larger than the key's.
     * @param node current node to browse the children of, or return in the set.
     * @param remainingPath remaining intermediate levels, starting with the sub-level of the keyref.
     * @param set set to gather contexts in.
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

    @Override
    public Map<XsdTreeNode, XsdTreeNode> getCouplings()
    {
        return this.coupledNodes;
    }

    /**
     * Update value in keyref nodes as it was changed in the coupled key node. Used by a key validator.
     * @param keyNode key node on which the value was changed.
     * @param fieldIndex index of field that was changed.
     * @param newValue new value.
     */
    void updateFieldValue(final XsdTreeNode keyNode, final int fieldIndex, final String newValue)
    {
        for (Entry<XsdTreeNode, XsdTreeNode> keyrefToKeyCoupling : getCouplings().entrySet())
        {
            XsdTreeNode keyrefNode = keyrefToKeyCoupling.getKey();
            if (keyrefToKeyCoupling.getValue().equals(keyNode))
            {
                if (this.valueValidatingKeyrefNodes.contains(keyrefNode))
                {
                    CoupledValidator.setValueIfNotNull(keyrefNode, newValue);
                }
                else
                {
                    for (Set<XsdTreeNode> attributeKeyrefNodes : this.attributeValidatingKeyrefNodes.values())
                    {
                        if (attributeKeyrefNodes.contains(keyrefNode))
                        {
                            int index = getFields().get(fieldIndex).getValidPathIndex(keyrefNode);
                            if (index >= 0)
                            {
                                String field = getFields().get(fieldIndex).getFieldPath(index);
                                int attr = field.indexOf("@");
                                String attribute = field.substring(attr + 1);
                                CoupledValidator.setAttributeIfNotNull(keyrefNode, attribute, newValue);
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
    void invalidateKeyrefNodes()
    {
        this.valueValidatingKeyrefNodes.forEach((node) -> node.invalidate());
        this.attributeValidatingKeyrefNodes.values().forEach((set) -> set.forEach((node) -> node.invalidate()));
    }

}
