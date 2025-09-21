package org.opentrafficsim.editor.decoration.validation;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.reference.ReferenceType;
import org.djutils.exceptions.Throw;
import org.opentrafficsim.editor.XsdTreeNode;
import org.w3c.dom.Node;

/**
 * Validator for xsd:key and xsd:unique. Functionality these are very similar, with both allowing to define multiple fields.
 * They register with the right nodes in the same way. Both xsd:key or xsd:unique will check a range of values for uniqueness
 * and only differ in whether all values need to be present. This class will maintain a list of nodes (fed by an external
 * listener) and validate against (set of) field(s) uniqueness over those nodes.
 * <p>
 * The KeyValidator class is prepared to work together with an KeyrefValidator, by maintaining a list of dependent
 * KeyrefValidators, and notifying them if a field value in this KeyValidator was changed.
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

    /** Selected nodes maintained for an xsd:key or xsd:unique, grouped per context node. */
    private final Map<XsdTreeNode, Set<XsdTreeNode>> nodes = new LinkedHashMap<>();

    /** For each node, this map remembers which field depends on what attribute. */
    private final Map<XsdTreeNode, Map<String, Integer>> dependentAttributes = new LinkedHashMap<>();

    /** For each node, this map remembers which field depends on its value. */
    private final Map<XsdTreeNode, Integer> dependentValues = new LinkedHashMap<>();

    /**
     * Constructor.
     * @param keyNode node defining the xsd:key or xsd:unique.
     * @param keyPath path where the key was defined.
     */
    public KeyValidator(final Node keyNode, final String keyPath)
    {
        super(keyNode, keyPath);
        Throw.when(!keyNode.getNodeName().equals("xsd:key") && !keyNode.getNodeName().equals("xsd:unique"),
                IllegalArgumentException.class, "The given node is not an xsd:key or xsd:unique node.");
    }

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
                    node.addListener(this, XsdTreeNode.VALUE_CHANGED, ReferenceType.WEAK);
                    this.dependentValues.put(node, fieldIndex);
                }
                else
                {
                    String attribute = path.substring(attr + 1);
                    node.addAttributeValidator(attribute, this, field);
                    node.addListener(this, XsdTreeNode.ATTRIBUTE_CHANGED, ReferenceType.WEAK);
                    this.dependentAttributes.computeIfAbsent(node, (n) -> new LinkedHashMap<>()).put(attribute, fieldIndex);
                }
                node.addListener(this, XsdTreeNode.ACTIVATION_CHANGED, ReferenceType.WEAK);
                if (node.isChoice())
                {
                    node.addListener(this, XsdTreeNode.OPTION_CHANGED, ReferenceType.WEAK);
                }
            }
        }

        if (isSelectedInContext(node))
        {
            node.addListener(this, XsdTreeNode.ACTIVATION_CHANGED, ReferenceType.WEAK);
            XsdTreeNode context = getContext(node);
            this.nodes.computeIfAbsent(context, (key) -> new LinkedHashSet<>()).add(node);
            invalidateAllDependent(); // new node may contain value set that a keyref want to couple to
        }
    }

    @Override
    public void removeNode(final XsdTreeNode node)
    {
        removeNodeKeepListening(node);
        node.removeListener(this, XsdTreeNode.VALUE_CHANGED);
        node.removeListener(this, XsdTreeNode.ATTRIBUTE_CHANGED);
        node.removeListener(this, XsdTreeNode.ACTIVATION_CHANGED);
        node.removeListener(this, XsdTreeNode.OPTION_CHANGED);
    }

    @Override
    public String validate(final XsdTreeNode node)
    {
        if (node.getParent() == null)
        {
            return null; // Node was deleted, but is still visible in the GUI tree for a moment
        }
        List<String> values = gatherFieldValues(node);
        if (values.contains(null))
        {
            if (this.keyNode.getNodeName().equals("xsd:key"))
            {
                // xsd:key; all must be present
                List<String> missing = new ArrayList<>();
                for (int i = 0; i < values.size(); i++)
                {
                    if (values.get(i) == null)
                    {
                        missing.add(user(this.fields.get(i).getFullFieldName()));
                    }
                }
                if (missing.size() == 1)
                {
                    return "Insufficient number of values, missing " + missing.get(0) + ".";
                }
                return "Insufficient number of values, missing " + missing + ".";
            }
            else
            {
                // xsd:unique with null value means this set of values is excluded from comparison
                return null;
            }
        }
        Collection<List<String>> set = getAllValueSets(node).values();
        if (Collections.frequency(set, values) > 1)
        {
            if (this.fields.size() == 1)
            {
                String name = user(this.fields.get(0).getFullFieldName());
                return "Value " + values.get(0) + " for " + name + " is not unique within " + user(this.keyPath) + ".";
            }
            return "Values " + values + " are not unique within " + user(this.keyPath) + ".";
        }
        return null;
    }

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
            activationOrChoiceStatusChanged(node, active);
            invalidateAllDependent();
        }
        else if (XsdTreeNode.VALUE_CHANGED.equals(event.getType()))
        {
            Object[] content = (Object[]) event.getContent();
            XsdTreeNode node = (XsdTreeNode) content[0];
            updateReferringKeyrefs(node, this.dependentValues.get(node), node.getValue());
            invalidateAllDependent();
        }
        else if (XsdTreeNode.ATTRIBUTE_CHANGED.equals(event.getType()))
        {
            Object[] content = (Object[]) event.getContent();
            XsdTreeNode node = (XsdTreeNode) content[0];
            if (!this.dependentAttributes.containsKey(node))
            {
                return;
            }
            String attribute = (String) content[1];
            Map<String, Integer> attributeFields = this.dependentAttributes.get(node);
            if (!attributeFields.containsKey(attribute))
            {
                return;
            }
            updateReferringKeyrefs(node, attributeFields.get(attribute), node.getAttributeValue(attribute));
            invalidateAllDependent();
        }
        else if (XsdTreeNode.OPTION_CHANGED.equals(event.getType()))
        {
            Object[] content = (Object[]) event.getContent();
            XsdTreeNode node = (XsdTreeNode) content[0];
            activationOrChoiceStatusChanged(node, node.getOption().equals(node));
            invalidateAllDependent();
        }
    }

    /**
     * Remove node. This method is called internally for children of deactivated nodes, in which case we do not want to remove
     * this validator as listener on the node, for when it gets activated later.
     * @param node node to remove.
     */
    private void removeNodeKeepListening(final XsdTreeNode node)
    {
        for (KeyrefValidator keyref : this.listeningKeyrefValidators)
        {
            keyref.removeNode(node);
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
     * Returns the present values of the fields for each node within the given context. Value sets containing {@code null} are
     * not returned, as these are invalid for xsd:key's, and ignored for xsd:unique's.
     * @param node node that is in the right context.
     * @return list of all values per key node.
     */
    protected Map<XsdTreeNode, List<String>> getAllValueSets(final XsdTreeNode node)
    {
        XsdTreeNode context = getContext(node);
        Map<XsdTreeNode, List<String>> map = new LinkedHashMap<>();
        for (XsdTreeNode otherNode : this.nodes.computeIfAbsent(context, (key) -> new LinkedHashSet<>()))
        {
            if (otherNode.isActive())
            {
                List<String> values = gatherFieldValues(otherNode);
                // only sets without null (xsd:key must have all values, xsd:unique ignores those sets)
                if (!values.contains(null))
                {
                    map.put(otherNode, values);
                }
            }
        }
        return map;
    }

    /**
     * Recursively removes or adds the children from an activated or deactivated node to/from this key. Children of a
     * deactivated node no longer have valid key values. Only active nodes are considered.
     * @param node node to remove or add.
     * @param active when node was activated, child nodes are added, otherwise removed.
     */
    private void activationOrChoiceStatusChanged(final XsdTreeNode node, final boolean active)
    {
        if (active)
        {
            addNode(node);
        }
        else
        {
            removeNodeKeepListening(node);
        }
        if (node.isActive())
        {
            for (XsdTreeNode child : node.getChildren())
            {
                activationOrChoiceStatusChanged(child, active);
            }
        }
    }

    /**
     * Update value in nodes that refer with xsd:keyref to a value that was changed, but only if the key is not duplicate and
     * the value is not empty.
     * @param node node on which the value was changed.
     * @param fieldIndex index of field that was changed.
     * @param newValue new value.
     */
    private void updateReferringKeyrefs(final XsdTreeNode node, final int fieldIndex, final String newValue)
    {
        if (canUpdateKeyRefs(node, fieldIndex, newValue))
        {
            for (KeyrefValidator validator : this.listeningKeyrefValidators)
            {
                validator.updateFieldValue(node, fieldIndex, newValue);
            }
        }
    }

    /**
     * Returns whether keyrefs can be updated. This is not true if there are duplicate keys or the new value is empty.
     * @param keyNode node where key is changed.
     * @param fieldIndex index of the field.
     * @param newValue new value.
     * @return whether keyrefs can be updated.
     */
    private boolean canUpdateKeyRefs(final XsdTreeNode keyNode, final int fieldIndex, final String newValue)
    {
        // TODO
        /*
         * This approach allows the following to happen. A coupled id is 'Abcd'. The user wants to remove the id by hitting
         * backspace a few times. The value changes each time, and each change is allowed to change the coupled values. Finally
         * the 'A' is also removed causing an empty value and the coupled values not to be updated. Effectively, removing the id
         * value has now changed all coupled values from 'Abcd' to 'A'. A similar thing can happen with the last valid value
         * before a duplicate new value is encountered. It is difficult to define a closed logic that can know when there is a
         * final change of the id. Any operation in the editor can change id values, such as an extension, not only editor
         * fields in the tree table or attributes table. The JTreeTable also has no clear event when editing starts, so we can't
         * set some boolean to prevent updating the keyrefs during an episode of editing.
         */
        if (newValue == null || newValue.isEmpty())
        {
            return false; // value is empty
        }
        Map<XsdTreeNode, List<String>> allValues = getAllValueSets(keyNode);
        List<String> values = allValues.remove(keyNode);
        if (values == null)
        {
            return true;
        }
        if (allValues.containsValue(values))
        {
            return false; // there are duplicates
        }
        return true;
    }

    /**
     * Invalidates all nodes that depend on this key, as a key node was added or changed.
     */
    private void invalidateAllDependent()
    {
        for (KeyrefValidator validator : this.listeningKeyrefValidators)
        {
            validator.invalidateNodes();
        }
    }

    /**
     * Adds a keyref validator as listening to this key.
     * @param keyrefValidator keyref validator.
     */
    public void addListeningKeyrefValidator(final KeyrefValidator keyrefValidator)
    {
        this.listeningKeyrefValidators.add(keyrefValidator);
    }

}
