package org.opentrafficsim.editor.decoration.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.opentrafficsim.editor.DocumentReader;
import org.opentrafficsim.editor.XsdTreeNode;
import org.w3c.dom.Node;

/**
 * Validator for xsd:key, xsd:keyref and xsd:unique. Functionality is very similar, with all allowing to define multiple
 * fields. They register with the right nodes in the same way. If no {@code KeyValidator} is given in the constructor, the
 * instance will behave as an xsd:key or xsd:unique. These check a range of values for uniqueness and officially only differ
 * in whether all values need to be present. Here this is ignored and they are treated the same. This class will maintain a
 * list of nodes (fed by an external listener) and validate against field uniqueness over those nodes. If another
 * {@code KeyValidator} is given in the constructor, the instance will behave as an xsd:keyref and validate that the field
 * values are, as a set, within the given {@code KeyValidator}.
 * @author wjschakel
 */
public class KeyValidator implements ValueValidator
{
    /** The node defining the xsd:key, xsd:keyref or xsd:unique. */
    private final Node keyNode;

    /** Path where the key was defined. */
    private final String keyPath;

    /** Key that is referred to by an xsd:keyref. */
    private final KeyValidator refer;

    /** Nodes maintained for an xsd:key. */
    private final Map<XsdTreeNode, Set<XsdTreeNode>> nodes = new LinkedHashMap<>();

    /** Name of the attribute the key points to, {@code null} if it points to a child element. */
    private final List<String> attributeNames = new ArrayList<>();

    /** Name of the child element the key points to, {@code null} if it points to an argument. */
    private final List<String> childNames = new ArrayList<>();

    /** Whether to include the value of the node itself (i.e. field reference "."). */
    private boolean includeSelfValue;

    /**
     * Constructor.
     * @param keyNode Node; node defining the xsd:key or xsd:keyref.
     * @param keyPath String; path where the key was defined.
     * @param refer KeyValidator; key that is referred to by an xsd:keyref, {@code null} for an xsd:key.
     */
    public KeyValidator(final Node keyNode, final String keyPath, final KeyValidator refer)
    {
        this.keyNode = keyNode;
        this.keyPath = keyPath;
        this.refer = refer;
        List<Node> fields = DocumentReader.getChildren(keyNode, "xsd:field");
        for (Node field : fields)
        {
            String value = DocumentReader.getAttribute(field, "xpath");
            if (value.startsWith("@"))
            {
                this.attributeNames.add(value.substring(1));
                continue;
            }
            else if (value.startsWith("ots:"))
            {
                this.childNames.add(value.substring(4));
                continue;
            }
            else if (value.equals("."))
            {
                this.includeSelfValue = true;
                continue;
            }
            throw new UnsupportedOperationException(
                    "Unable to validate keyref that does not point to an attribute (@) or OTS type (ots:).");
        }
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
        if (this.refer == null)
        {
            if (Collections.frequency(getValues(node), values) > 1)
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
        if (this.refer.getValues(node).contains(values))
        {
            return null;
        }
        if (values.size() == 1)
        {
            String value = values.get(0);
            if (value.startsWith("{") && value.endsWith("}"))
            {
                return null; // expression
            }
            String name = this.attributeNames.isEmpty() ? (this.childNames.isEmpty() ? "node" : this.childNames.get(0))
                    : this.attributeNames.get(0);
            return "Value " + value + " for " + name + " does not refer to a known " + this.refer.getTypeString()
                    + " within " + this.keyPath + ".";
        }
        values.removeIf((value) -> value.startsWith("{") && value.endsWith("}")); // expressions
        return "Values " + values + " do not refer to a known " + this.refer.getTypeString() + " within " + this.keyPath
                + ".";
    }

    /**
     * Adds node to this key. Nodes are stored per parent instance that defines the context at the level of the path at
     * which the key was defined.
     * @param node XsdTreeNode; node to add.
     */
    public void addNode(final XsdTreeNode node)
    {
        String path = getPath().equals("Ots") ? getTypeString() : getPath() + "." + getTypeString();
        boolean isType = node.isType(path);
        if (isType && this.refer == null)
        {
            XsdTreeNode context = getContext(node);
            this.nodes.computeIfAbsent(context, (key) -> new LinkedHashSet<>()).add(node);
        }
        if (isType)
        {
            if (this.includeSelfValue)
            {
                node.addValueValidator(this);
            }
            for (String attribute : this.attributeNames)
            {
                node.addAttributeValidator(attribute, this);
            }
        }
        for (String child : this.childNames)
        {
            String fullPath = getTypeString() + "." + child;
            isType = node.getPathString().endsWith(fullPath);
            if (isType)
            {
                node.addValueValidator(this);
            }
        }
    }

    /**
     * Returns a node that represent the proper context.
     * @param node XsdTreeNode; any node somewhere in the context.
     * @return XsdTreeNode; node that represent the proper context.
     */
    private XsdTreeNode getContext(final XsdTreeNode node)
    {
        XsdTreeNode context = null;
        List<XsdTreeNode> path = node.getPath();
        for (int index = path.size() - 1; index >= 0; index--)
        {
            if (path.get(index).getPathString().endsWith(getPath()))
            {
                context = path.get(index);
                break;
            }
        }
        return context;
    }

    /**
     * Remove node. It is removed from all contexts.
     * @param node XsdTreeNode; node to remove.
     */
    public void removeNode(final XsdTreeNode node)
    {
        this.nodes.values().forEach((set) -> set.remove(node));
    }

    /**
     * Returns the present values of the fields for each node within the given context.
     * @param node XsdTreeNode; node that is in the right context.
     * @return List&lt;List&lt;String&gt;&gt;; list of all values.
     */
    private List<List<String>> getValues(final XsdTreeNode node)
    {
        XsdTreeNode context = getContext(node);
        List<List<String>> list = new ArrayList<>();
        boolean isKey = this.keyNode.getNodeName().equals("xsd:key");
        for (XsdTreeNode otherNode : this.nodes.computeIfAbsent(context, (key) -> new LinkedHashSet<>()))
        {
            if (otherNode.isActive())
            {
                List<String> nodeList = gatherFields(otherNode);
                List<String> filtered = new ArrayList<>(nodeList);
                filtered.removeIf((val) -> val == null || val.isBlank());
                if (filtered.size() < nodeList.size() && isKey)
                {
                    // xsd:key requires all fields to be present, with missing values we cannot compare with this node
                    continue;
                }
                list.add(nodeList);
            }
        }
        return list;
    }

    /**
     * Gathers all the field values, i.e. attribute or child element value. As validators are registered with the node that
     * has the value, attributes are gathered from the given node, while element values are taken from the correctly named
     * children of the parent.
     * @param node XsdTreeNode; node for which to get the information.
     * @return List&lt;String&gt;; field values.
     */
    private List<String> gatherFields(final XsdTreeNode node)
    {
        List<String> nodeList = new ArrayList<>();
        for (String attribute : this.attributeNames)
        {
            nodeList.add(node.getAttributeValue(attribute));
        }
        // a child calls this method to validate its value, need to gather all children's values via parent
        List<XsdTreeNode> path = node.getPath();
        XsdTreeNode parent = path.get(path.size() - 2);
        for (String child : this.childNames)
        {
            for (XsdTreeNode treeChild : parent.getChildren())
            {
                if (treeChild.getNodeName().equals(child))
                {
                    nodeList.add(treeChild.getValue());
                }
            }
        }
        if (this.includeSelfValue)
        {
            nodeList.add(node.getValue());
        }
        return nodeList;
    }

    /** {@inheritDoc} */
    @Override
    public List<String> getOptions(final XsdTreeNode node, final String field)
    {
        if (this.refer == null)
        {
            return null;
        }
        /*
         * The following is not robust. The field index might be wrong if 'field', which is the xsd-node name, is equal
         * among attributes, child nodes, and the node itself. E.g. when we are at Route.Node.Node and field = "Node", do we
         * need the value of Route.Node (the node itself), or of either a child node or attribute named "Node", which may
         * also both exist?
         */
        int fieldIndex = this.attributeNames.indexOf(field);
        if (fieldIndex < 0)
        {
            int deltaFieldIndex = this.childNames.indexOf(field);
            if (deltaFieldIndex < 0)
            {
                fieldIndex = this.attributeNames.size() + this.childNames.size(); // value of node itself appended
            }
            else
            {
                fieldIndex = this.attributeNames.size() + deltaFieldIndex;
            }
        }
        /*
         * We gather values from the referred xsd:key, drawing the appropriate context from the node relevant to the
         * xsd:keyref. Can the context of the referred xsd:key be different?
         */
        int index = fieldIndex;
        List<List<String>> values = this.refer.getValues(node);
        List<String> result = new ArrayList<>(values.size());
        values.forEach((list) -> result.add(list.get(index)));
        return result;
    }

    /**
     * Returns the name of the key, i.e. {@code <xsd:keyref name="NAME">}.
     * @return String; name of the key.
     */
    public String getKeyName()
    {
        return DocumentReader.getAttribute(this.keyNode, "name");
    }

    /**
     * Returns the path at which the xsd:key or xsd:keyref is defined.
     * @return String; path at which the xsd:key or xsd:keyref is defined.
     */
    public String getPath()
    {
        return this.keyPath;
    }

    /**
     * Returns the type {@code String} for which the xsd:key or xsd:keyref applies, i.e. "GTUTYPES.GTUTYPE" for
     * {@code <xsd:selector xpath=".//ots:GTUTYPES/ots:GTUTYPE" />}.
     * @return String; type for which the xsd:key or xsd:keyref applies.
     */
    public String getTypeString()
    {
        return DocumentReader.getAttribute(DocumentReader.getChild(this.keyNode, "xsd:selector"), "xpath")
                .replace(".//ots:", "").replace("ots:", "").replace("/", ".");
    }

}