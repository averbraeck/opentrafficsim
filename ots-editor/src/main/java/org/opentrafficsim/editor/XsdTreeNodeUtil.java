package org.opentrafficsim.editor;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.Immutable;
import org.djutils.immutablecollections.ImmutableArrayList;
import org.djutils.immutablecollections.ImmutableList;
import org.opentrafficsim.editor.decoration.validation.XsdAllValidator;
import org.w3c.dom.Node;

/**
 * This class exists to keep {@code XsdTreeNode} at manageable size. It houses all static methods used in {@code XsdTreeNode}.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public final class XsdTreeNodeUtil
{

    /** Pattern for regular expression to split string by upper case without disregarding the upper case itself. */
    private static final Pattern UPPER_PATTERN = Pattern.compile("(?=\\p{Lu})");

    /** Validators for xsd:all nodes and their children. */
    private final static Map<String, XsdAllValidator> XSD_ALL_VALIDATORS = new LinkedHashMap<>();

    /**
     * Private constructor.
     */
    private XsdTreeNodeUtil()
    {

    }

    /**
     * Add xsd all validator to the given node.
     * @param shared XsdTreeNode; shared xsd:all node.
     * @param node XsdTreeNode; xsd:all node, or one of its children.
     */
    public static void addXsdAllValidator(final XsdTreeNode shared, final XsdTreeNode node)
    {
        String path = shared.getPathString();
        XsdAllValidator validator = XSD_ALL_VALIDATORS.computeIfAbsent(path, (p) -> new XsdAllValidator(node.getRoot()));
        node.addNodeValidator(validator);
        validator.addNode(node);
    }

    /**
     * Parses the minOcccurs or maxOccurs value from given node. If it is not supplied, the default of 1 is given.
     * @param node Node; node.
     * @param attribute String; "minOccurs" or "maxOccurs".
     * @return int; value of occurs, -1 represents "unbounded".
     */
    static int getOccurs(final Node node, final String attribute)
    {
        String occurs = DocumentReader.getAttribute(node, attribute);
        if (occurs == null)
        {
            return 1;
        }
        if ("unbounded".equals(occurs))
        {
            return -1;
        }
        return Integer.valueOf(occurs);
    }

    /**
     * Main expansion algorithm. Loops all child XSD nodes, and selects those that define next elements.
     * @param node Node; node to get the children of.
     * @param parentNode XsdTreeNode; parent node for the created children.
     * @param children List&lt;XsdTreeNode&gt;; list to add the children to. This may be different from
     *            {@code parentNode.children} due to layered choice structures.
     * @param hiddenNodes ImmutableList&lt;Node&gt;; nodes between the XSD node of the parent, and this tree node's XSD node.
     * @param schema XsdSchema; schema to get types and referred elements from.
     * @param flattenSequence boolean; when true, treats an xsd:sequence child as an extension of the node. In the context of a
     *            choice this should remain separated.
     * @param skip int; child index to skip, this is used when copying choice options from an option that is already created.
     */
    static void addChildren(final Node node, final XsdTreeNode parentNode, final List<XsdTreeNode> children,
            final ImmutableList<Node> hiddenNodes, final Schema schema, final boolean flattenSequence, final int skip)
    {
        int skipIndex = skip;
        XsdTreeNode root = parentNode.getRoot();
        for (int childIndex = 0; childIndex < node.getChildNodes().getLength(); childIndex++)
        {
            Node child = node.getChildNodes().item(childIndex);
            switch (child.getNodeName())
            {
                case "xsd:element":
                    if (children.size() == skipIndex)
                    {
                        skipIndex = -1;
                        break;
                    }
                    XsdTreeNode element;
                    String ref = DocumentReader.getAttribute(child, "ref");
                    String type = DocumentReader.getAttribute(child, "type");
                    if (ref != null)
                    {
                        element = new XsdTreeNode(parentNode, XsdTreeNodeUtil.ref(child, ref, schema),
                                XsdTreeNodeUtil.append(hiddenNodes, node), child);
                    }
                    else if (type != null)
                    {
                        Node typedNode = XsdTreeNodeUtil.type(child, type, schema);
                        if (typedNode == null)
                        {
                            // xsd:string or other basic type
                            element = new XsdTreeNode(parentNode, child, XsdTreeNodeUtil.append(hiddenNodes, node));
                        }
                        else
                        {
                            element = new XsdTreeNode(parentNode, typedNode, XsdTreeNodeUtil.append(hiddenNodes, node), child);
                        }
                    }
                    else
                    {
                        element = new XsdTreeNode(parentNode, child, XsdTreeNodeUtil.append(hiddenNodes, node));
                    }
                    children.add(element);
                    root.fireEvent(XsdTreeNodeRoot.NODE_CREATED,
                            new Object[] {element, parentNode, parentNode.children.indexOf(element)});
                    break;
                case "xsd:sequence":
                    if (children.size() == skipIndex)
                    {
                        skipIndex = -1;
                        break;
                    }
                    if (flattenSequence)
                    {
                        addChildren(child, parentNode, children, XsdTreeNodeUtil.append(hiddenNodes, node), schema,
                                flattenSequence, -1);
                    }
                    else
                    {
                        // add sequence as option, 'children' is a list of options for a choice
                        XsdTreeNode sequence = new XsdTreeNode(parentNode, child, XsdTreeNodeUtil.append(hiddenNodes, node));
                        children.add(sequence);
                        root.fireEvent(XsdTreeNodeRoot.NODE_CREATED,
                                new Object[] {sequence, parentNode, parentNode.children.indexOf(sequence)});
                    }
                    break;
                case "xsd:choice":
                case "xsd:all":
                    if (children.size() == skipIndex)
                    {
                        skipIndex = -1;
                        break;
                    }
                    XsdTreeNode choice = new XsdTreeNode(parentNode, child, XsdTreeNodeUtil.append(hiddenNodes, node));
                    root.fireEvent(XsdTreeNodeRoot.NODE_CREATED,
                            new Object[] {choice, parentNode, parentNode.children.indexOf(choice)});
                    choice.createOptions();
                    /*
                     * We add the choice node, which is usually overwritten by the consecutive setting of an option. But not if
                     * this choice is part of a sequence, that is itself an option in a parentChoice. Then, the option is set at
                     * the level of the parent choice. The sequence option of the parentChoice in fact needs to be populated by
                     * the choice nodes. If we don't add it here, the sequence will be empty.
                     */
                    children.add(choice);
                    choice.setOption(choice.options.get(0));
                    break;
                case "xsd:extension":
                    if (children.size() == skipIndex)
                    {
                        skipIndex = -1;
                        break;
                    }
                    XsdTreeNode extension = new XsdTreeNode(parentNode, child, XsdTreeNodeUtil.append(hiddenNodes, node));
                    root.fireEvent(XsdTreeNodeRoot.NODE_CREATED,
                            new Object[] {extension, parentNode, parentNode.children.indexOf(extension)});
                    children.add(extension);
                    break;
                case "xsd:attribute":
                case "xsd:annotation":
                case "xsd:simpleType": // only defines xsd:restriction with xsd:pattern/xsd:enumeration
                case "xsd:restriction":
                case "xsd:simpleContent": // bit of a late capture, followed "type" attribute and did not check what it was
                case "xsd:union":
                case "#text":
                    // nothing, not even report ignoring, these are not relevant regarding element structure
                    break;
                default:
                    System.out.println("Ignoring a " + child.getNodeName());
            }
        }
    }

    /**
     * Returns a copy of the input list, with the extra node appended at the end.
     * @param hiddenNodes ImmutableList&lt;Node&gt;; hidden nodes list.
     * @param node Node; node to append.
     * @return ImmutableList&lt;Node&gt;; copy of the input list, with the extra node appended at the end.
     */
    static ImmutableList<Node> append(final ImmutableList<Node> hiddenNodes, final Node node)
    {
        List<Node> list = new ArrayList<>(hiddenNodes.size() + 1);
        list.addAll(hiddenNodes.toCollection());
        list.add(node);
        return new ImmutableArrayList<>(list, Immutable.WRAP);
    }

    /**
     * Returns a list of options derived from a list of restrictions (xsd:restriction).
     * @param restrictions List&lt;Node&gt;; list of restrictions.
     * @return List&lt;String&gt;; list of options.
     */
    static List<String> getOptionsFromRestrictions(final List<Node> restrictions)
    {
        List<String> options = new ArrayList<>();
        for (Node restriction : restrictions)
        {
            List<Node> enumerations = DocumentReader.getChildren(restriction, "xsd:enumeration");
            for (Node enumeration : enumerations)
            {
                options.add(DocumentReader.getAttribute(enumeration, "value"));
            }
        }
        return options;
    }

    /**
     * Recursively throws creation events for all current nodes in the tree. This method is for {@code XsdTreeNodeRoot}.
     * @param node XsdTreeNode; node.
     * @param listener EventListener; listener.
     * @throws RemoteException if event cannot be fired.
     */
    protected static void fireCreatedEventOnExistingNodes(final XsdTreeNode node, final EventListener listener)
            throws RemoteException
    {
        List<XsdTreeNode> subNodes = node.children == null ? new ArrayList<>() : new ArrayList<>(node.children);
        // only selected node extends towards choice, otherwise infinite recursion
        if (node.choice != null && node.choice.selected.equals(node))
        {
            subNodes.add(node.choice);
            subNodes.addAll(node.choice.options);
            subNodes.remove(node);
        }
        for (XsdTreeNode child : subNodes)
        {
            fireCreatedEventOnExistingNodes(child, listener);
        }
        Event event = new Event(XsdTreeNodeRoot.NODE_CREATED, new Object[] {node, node.getParent(), subNodes.indexOf(node)});
        listener.notify(event);
    }

    /**
     * Takes the minimum of both indices, while ignoring negative values (indicating an element was not found for deletion).
     * @param insertIndex int; previously determined insertion index; may be updated to lower value.
     * @param removeIndex int; index of element that is removed.
     * @return int; minimum of both indices, while ignoring negative values.
     */
    static int resolveInsertion(final int insertIndex, final int removeIndex)
    {
        int tmp = insertIndex < 0 ? removeIndex : insertIndex;
        return tmp < removeIndex ? tmp : removeIndex;
    }

    /**
     * Returns from the XSD definition the appropriate nodes to take children from at the level of the input node, in the order
     * in which they should appear. This is often the xsd:complexType within an xsd:element, but can become as complex as
     * containing multiple xsd:extension and their referred base types. An xsd:sequence is also common. Adding children in the
     * order as they appear per {@code Node}, and in the order the {@code Node}'s are given, results in an overall order
     * suitable for XML.
     * @param node Node; node to expand further.
     * @param hiddenNodes ImmutableList&lt;Node&gt;; nodes between the XSD node of the parent, and this tree node's XSD node.
     * @param schema XsdSchema; schema to retrieve types.
     * @return Map&lt;Node, ImmutableList&lt;Node&gt;&gt;; map of nodes containing relevant children at the level of the input
     *         node, and their appropriate hidden nodes.
     */
    static Map<Node, ImmutableList<Node>> getRelevantNodesWithChildren(final Node node, final ImmutableList<Node> hiddenNodes,
            final Schema schema)
    {
        Node complexType =
                node.getNodeName().equals("xsd:complexType") ? node : DocumentReader.getChild(node, "xsd:complexType");
        if (complexType != null)
        {
            Node sequence = DocumentReader.getChild(complexType, "xsd:sequence");
            if (sequence != null)
            {
                return Map.of(sequence, append(hiddenNodes, complexType));
            }
            Node complexContent = DocumentReader.getChild(complexType, "xsd:complexContent");
            if (complexContent != null)
            {
                Node extension = DocumentReader.getChild(complexContent, "xsd:extension");
                if (extension != null)
                {
                    ImmutableList<Node> hiddenExtension = append(append(hiddenNodes, complexType), complexContent);
                    LinkedHashMap<Node, ImmutableList<Node>> elements = new LinkedHashMap<>();
                    String base = DocumentReader.getAttribute(extension, "base");
                    if (base != null)
                    {
                        Node baseNode = schema.getType(base);
                        if (baseNode != null)
                        {
                            elements.putAll(getRelevantNodesWithChildren(baseNode, append(hiddenExtension, extension), schema));
                        }
                    }
                    elements.put(extension, hiddenExtension);
                    return elements;
                }
            }
            return Map.of(complexType, hiddenNodes);
        }
        return Map.of(node, hiddenNodes);
    }

    /**
     * Returns whether nodes are of the same type. This regards the referring XSD node if it exists, otherwise it regards the
     * regular XSD node.
     * @param node1 XsdTreeNode; node 1.
     * @param node2 XsdTreeNode; node 1.
     * @return boolean; whether nodes are of the same type.
     */
    static boolean haveSameType(final XsdTreeNode node1, final XsdTreeNode node2)
    {
        return (node1.referringXsdNode != null && node1.referringXsdNode.equals(node2.referringXsdNode))
                || (node1.referringXsdNode == null && node1.xsdNode.equals(node2.xsdNode));
    }

    /**
     * Returns the element referred to by ref={ref} in an xsd:element. Will return {@code XiIncludeNode.XI_INCLUDE} for
     * xi:include.
     * @param node Node; node, must have ref={ref} attribute.
     * @param ref String; value of ref={ref}.
     * @param schema XsdSchema; schema to take element from.
     * @return Node; element referred to by ref={ref} in an xsd:element.
     */
    static Node ref(final Node node, final String ref, final Schema schema)
    {
        if (ref.equals("xi:include"))
        {
            return XiIncludeNode.XI_INCLUDE;
        }
        Node refNode = schema.getElement(ref);
        Throw.when(refNode == null, RuntimeException.class, "Unable to load ref for %s from XSD schema.", ref);
        return refNode;
    }

    /**
     * Returns the element referred to by type={type} in an xsd:element. Ignores all types starting with "xsd:" as these are
     * standard types to which user input can be validated directly.
     * @param node Node; node, must have type={type} attribute.
     * @param type String; value of type={type}.
     * @param schema XsdSchema; schema to take type from.
     * @return Node; element referred to by type={type} in an xsd:element.
     */
    static Node type(final Node node, final String type, final Schema schema)
    {
        if (type.startsWith("xsd:"))
        {
            return null;
        }
        Node typeNode = schema.getType(type);
        Throw.when(typeNode == null, RuntimeException.class, "Unable to load type for %s from XSD schema.", type);
        return typeNode;
    }

    /**
     * Adds a thin space before each capital character in a {@code String}, except the first.
     * @param name String; name of node.
     * @return String; input string but with a thin space before each capital character, except the first.
     */
    static String separatedName(final String name)
    {
        String[] parts = UPPER_PATTERN.split(name);
        if (parts.length == 1)
        {
            return parts[0];
        }
        String separator = "";
        StringBuilder stringBuilder = new StringBuilder();
        for (String part : parts)
        {
            stringBuilder.append(separator).append(part);
            separator = " ";
        }
        return stringBuilder.toString();
    }

    /**
     * Returns whether the two values are equal, where {@code null} is consider equal to an empty string.
     * @param value1 String; value 1.
     * @param value2 String; value 2.
     * @return whether the two values are equal, where {@code null} is consider equal to an empty string.
     */
    public static boolean valuesAreEqual(final String value1, final String value2)
    {
        boolean value1Empty = value1 == null || value1.isEmpty();
        boolean value2Empty = value2 == null || value2.isEmpty();
        return (value1Empty && value2Empty) || (value1 != null && value1.equals(value2));
    }

}
