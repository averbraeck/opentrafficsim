package org.opentrafficsim.editor;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Queue;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.djutils.exceptions.Throw;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

/**
 * Reads the XML Schema in XSD format for OTS. This class contains various methods that the editor can use to present relevant
 * structure and information to the user.
 * @author wjschakel
 */
public class Schema
{

    /** Root OTS node. */
    private Node root;

    /** List of read files. */
    private final Set<String> readFiles = new LinkedHashSet<>();

    /** All loaded types, xsd:simpleType or xsd:complexType with name={name} attribute. */
    private final Map<String, Node> types = new LinkedHashMap<>();

    /** Paths of all types that are sub-classed using xsd:extension, coupled to a set of the extending elements. */
    private final Map<String, Set<String>> extendedTypes = new LinkedHashMap<>();

    /**
     * Paths of types that are referred to by xsd:element or xsd:attribute with type={type}, coupled to a set of the referring
     * elements.
     */
    private final Map<String, Set<String>> referredTypes = new LinkedHashMap<>();

    /** All loaded elements, path and node. */
    private final Map<String, Node> elements = new LinkedHashMap<>();

    /** Paths of elements that are referred to by xsd:element with ref={ref}, coupled to a set of the referring elements. */
    private final Map<String, Set<String>> referredElements = new LinkedHashMap<>();

    /** Documentation xsd:documentation stored at its path. */
    private final Map<String, String> documentation = new LinkedHashMap<>();

    /** Nodes xsd:key. */
    private final Map<String, Node> keys = new LinkedHashMap<>();

    /** Paths where xsd:key are defined, same order as {@code keys}. */
    private final Map<String, String> keysPath = new LinkedHashMap<>();

    /** Nodes xsd:keyref. */
    private final Map<String, Node> keyrefs = new LinkedHashMap<>();

    /** Paths where xsd:keyref are defined, same order as {@code keyrefs}. */
    private final Map<String, String> keyrefsPath = new LinkedHashMap<>();

    /** Nodes xsd:unique. */
    private final Map<String, Node> uniques = new LinkedHashMap<>();

    /** Paths where xsd:unique are defined, same order as {@code uniques}. */
    private final Map<String, String> uniquesPath = new LinkedHashMap<>();

    /** Reading queue. */
    private final Queue<RecursionElement> queue = new LinkedList<>();

    /** Boolean to prevent infinite loop of self-queuing as a parent type is not found. */
    private boolean blockLoop = false;

    /**
     * Constructs the XML Schema information from a document.
     * @param document Document; main document, other files may be included from within the file.
     */
    public Schema(final Document document)
    {
        this.readFiles.add(document.getDocumentURI());

        queue("", document, true);
        while (!this.queue.isEmpty())
        {
            RecursionElement next = this.queue.poll();
            read(next.getPath(), next.getNode(), next.isExtendPath());
        }

        // all elements with type={type} have that node stored, replace it with the referred node
        for (Entry<String, Node> entry : this.elements.entrySet())
        {
            String referredTypeName = DocumentReader.getAttribute(entry.getValue(), "type");
            if (referredTypeName != null && !referredTypeName.startsWith("xsd:"))
            {
                Node referredType = getType(referredTypeName);
                entry.setValue(referredType);
            }
        }

        // checks
        Set<String> allTypes = new LinkedHashSet<>(this.types.keySet());
        for (String str : this.extendedTypes.keySet())
        {
            allTypes.removeIf((val) -> val.startsWith(str));
        }
        for (String str : this.referredTypes.keySet())
        {
            allTypes.removeIf((val) -> val.startsWith(str));
        }
        if (!allTypes.isEmpty())
        {
            System.out.println(allTypes.size() + " types are defined but never extended or referred to.");
            // allTypes.forEach((str) -> System.out.println(" + " + str));
        }

        Set<String> allElements = new LinkedHashSet<>(this.elements.keySet());
        for (String str : this.referredElements.keySet())
        {
            allElements.removeIf((val) -> val.startsWith(str));
        }
        for (String str : this.types.keySet())
        {
            allElements.removeIf((val) -> val.startsWith(str));
        }
        allElements.removeIf((path) -> path.startsWith("OTS"));
        if (!allElements.isEmpty())
        {
            System.out.println(allElements.size() + " elements are defined but never referred to, nor are they a type.");
            // allElements.forEach((str) -> System.out.println(" + " + str));
        }

        checkKeys();
        checkKeyrefs();
        checkUniques();

        // allElements = new LinkedHashSet<>(this.elements.keySet());
        // allElements.removeIf((key) -> !key.startsWith("OTS."));
        // allElements.forEach((key) -> System.out.println(key));

        System.out.println("Root found as '" + DocumentReader.getAttribute(this.getRoot(), "name") + "'.");
        System.out.println("Read " + this.readFiles.size() + " files.");
        System.out.println("Read " + this.elements.size() + " elements.");
        System.out.println("Read " + this.types.size() + " types.");
        System.out.println("Read " + this.extendedTypes.size() + " extended types.");
        System.out.println("Read " + this.documentation.size() + " documentations.");
        System.out.println("Read " + this.keys.size() + " keys.");
        System.out.println("Read " + this.keyrefs.size() + " keyrefs.");
        System.out.println("Read " + this.uniques.size() + " uniques.");
        for (String type : this.extendedTypes.keySet())
        {
            if (!this.types.containsKey(type) && !type.startsWith("xsd:"))
            {
                System.err.println("Type '" + type + "' is extended but was not found.");
            }
        }
    }

    /**
     * Reads the next node. If recursion is found, the node is ignored.<br>
     * <br>
     * If the node is xsd:extension, reading the extended type in place of the path is queued. If, however, the extended type is
     * not yet loaded, reading this node is queued to be read again. The method will continue to read the parts of the node
     * defined additional to the extended type. To prevent that this is read again later, this is captured in the later read,
     * which will then be ignored.<br>
     * <br>
     * Extends the path for xsd:element's with either a name={name} or ref={ref} attribute. If the name is "OTS", this element
     * is stored as the root for the whole schema. If there is a ref={ref} attribute, rather than reading the given node, the
     * referred node is read at its place in the path.<br>
     * <br>
     * Also extends the path for an xsd:simpleType or xsd:complexType node, of it has a name={name} attribute.<br>
     * <br>
     * Finally, loops all the children of the node to read and processes them in the following manner:
     * <ul>
     * <li>#text nodes are ignored</li>
     * <li>xsd:include, xsd:attribute, xsd:element and xsd:documentation nodes are forwarded to dedicated methods.</li>
     * <li>xsd:key, xsd:keyref and xsd:unique nodes are stored for later checks.</li>
     * <li>All other child nodes are recursively read.</li>
     * </ul>
     * @param path String; node path.
     * @param node Node; xsd:attribute node.
     * @param extendPath boolean; whether the path should be extended with this node.
     */
    private void read(final String path, final Node node, final boolean extendPath)
    {
        if (recursion(path))
        {
            System.out.println("Recursion found at " + path + ", further expansion is halted.");
            return;
        }

        if (node.getNodeName().equals("xsd:extension"))
        {
            String base = DocumentReader.getAttribute(node, "base").replace("ots:", "");
            if (!base.startsWith("xsd:"))
            {
                Node baseNode = getType(base);
                if (baseNode == null)
                {
                    if (this.blockLoop)
                    {
                        return;
                    }
                    this.blockLoop = true;
                    // this occurs if a type has a base, with the base being defined later in the XSD
                    queue(path, node, false);
                }
                else
                {
                    queue(path, baseNode, false);
                }
            }
            if (this.extendedTypes.containsKey(base) && this.extendedTypes.get(base).contains(path))
            {
                // this occurs if a type has a base, with the base being defined later in the XSD
                return;
            }
            this.extendedTypes.computeIfAbsent(base, (key) -> new LinkedHashSet<String>()).add(path);
        }
        this.blockLoop = false;

        String nextPath = path;
        Node nextNode = node;
        if (node.getNodeName().equals("xsd:element") && node.hasAttributes())
        {
            // an xsd:element can not have a name and a ref attribute
            String name = DocumentReader.getAttribute(node, "name");
            if (name != null)
            {
                if (name.equals("OTS"))
                {
                    this.root = node;
                }
                nextPath = extendPath ? (nextPath.isEmpty() ? name : nextPath + "." + name) : nextPath;
                this.elements.put(nextPath, node);
            }
            String ref = DocumentReader.getAttribute(node, "ref");
            if (ref != null)
            {
                ref = ref.replace("ots:", "");
                nextNode = getElement(ref);
                /*
                 * There might be more exotic referring situations than this one. Here, we have an <xsd:element ref="MODEL">
                 * pointing to a <xsd:element name="MODEL" type="MODELTYPE" /> being typed by an <xsd:complexType
                 * name="MODELTYPE">.
                 */
                if (DocumentReader.getAttribute(nextNode, "type") != null)
                {
                    element(path, nextNode);
                }
                nextPath = extendPath ? (nextPath.isEmpty() ? ref : nextPath + "." + ref) : nextPath;
                this.elements.put(nextPath, nextNode);
                Throw.whenNull(nextNode, "Element %s refers to a type that was not loaded in first pass.", nextPath);
            }
        }

        String name = DocumentReader.getAttribute(nextNode, "name");
        String nodeName = nextNode.getNodeName();
        if (name != null && (nodeName.equals("xsd:complexType") || nodeName.equals("xsd:simpleType")))
        {
            this.types.put(name, nextNode);
            nextPath = extendPath ? (nextPath.isEmpty() ? name : nextPath + "." + name) : nextPath;
        }

        if (!nextNode.hasChildNodes())
        {
            return;
        }
        for (int childIndex = 0; childIndex < nextNode.getChildNodes().getLength(); childIndex++)
        {
            Node child = nextNode.getChildNodes().item(childIndex);
            switch (child.getNodeName())
            {
                case "#text":
                    break;
                case "xsd:include":
                    include(nextPath, child);
                    break;
                case "xsd:element":
                    element(nextPath, child);
                    break;
                case "xsd:attribute":
                    attribute(nextPath, child);
                    break;
                case "xsd:documentation":
                    documentation(nextPath, child);
                    break;
                case "xsd:key":
                    this.keys.put(DocumentReader.getAttribute(child, "name"), child);
                    this.keysPath.put(DocumentReader.getAttribute(child, "name"), nextPath);
                    break;
                case "xsd:keyref":
                    this.keyrefs.put(DocumentReader.getAttribute(child, "name"), child);
                    this.keyrefsPath.put(DocumentReader.getAttribute(child, "name"), nextPath);
                    break;
                case "xsd:unique":
                    this.uniques.put(DocumentReader.getAttribute(child, "name"), child);
                    this.uniquesPath.put(DocumentReader.getAttribute(child, "name"), nextPath);
                    break;
                default:
                    read(nextPath, child, true);
            }
        }
    }

    /**
     * Checks for recursion. This is recognized as the some end of the path, is duplicated in an equal sub-path before that end.
     * For example CARFOLLOWINGMODEL{.SOCIO}{.SOCIO} or CARFOLLOWINGMODEL{.SOCIO.PARENT}{.SOCIO.PARENT}.
     * @param path String; node path.
     * @return boolean; true if the path contains recursion.
     */
    private boolean recursion(final String path)
    {
        StringBuffer sub = (new StringBuffer(path)).reverse();
        int fromIndex = 0;
        while (fromIndex < sub.length())
        {
            int dot = sub.indexOf(".", fromIndex);
            if (dot < 0)
            {
                return false; // no dot in path
            }
            int toIndex = 2 * (dot + 1);
            if (toIndex > sub.length())
            {
                return false; // first dot beyond middle
            }
            if (sub.substring(0, dot + 1).equals(sub.substring(dot + 1, toIndex)))
            {
                return true;
            }
            fromIndex = dot + 1;
        }
        return false;
    }

    /**
     * Queues reading the node at specified path. This is used to start all the reading, and to delay reading that is dependent
     * on parent types to be loaded. When the read is queued as a parent type is not yet loaded, the path should not be extended
     * upon the queued read. The path will have been extended in processing the child element itself.
     * @param path String; node path.
     * @param node Node; node.
     * @param extendPath boolean; whether the path should be extended with this node.
     */
    private void queue(final String path, final Node node, final boolean extendPath)
    {
        this.queue.add(new RecursionElement(path, node, extendPath));
    }

    /**
     * Reads further from an included file.
     * @param path String; node path.
     * @param node Node; xsd:include node.
     */
    private void include(final String path, final Node node)
    {
        String schemaLocation = DocumentReader.getAttribute(node, "schemaLocation");
        String schemaPath = folder(node) + schemaLocation;
        if (!this.readFiles.add(schemaPath))
        {
            return;
        }
        try
        {
            read(path, DocumentReader.open(new URI(schemaPath)), true);
        }
        catch (SAXException | IOException | ParserConfigurationException | URISyntaxException e)
        {
            throw new RuntimeException("Unable to find resource " + folder(node) + schemaLocation);
        }
    }

    /**
     * Returns the path, with separator at the end, relative to which an include in the node should be found.
     * @param node Node; node.
     * @return String; path, with separator at the end, relative to which an include in the node should be found.
     */
    private String folder(final Node node)
    {
        String uri = node.getBaseURI();
        if (uri == null)
        {
            return "";
        }
        int a = uri.lastIndexOf("\\");
        int b = uri.lastIndexOf("/");
        return uri.substring(0, (a > b ? a : b) + 1);
    }

    /**
     * Reads further from an element node.
     * @param path String; node path.
     * @param node Node; xsd:element node.
     */
    private void element(final String path, final Node node)
    {
        if (DocumentReader.getAttribute(node, "ref") != null)
        {
            ref(path, node);
            return;
        }
        String type = DocumentReader.getAttribute(node, "type");
        if (type != null && !type.startsWith("xsd:"))
        {
            type = type.replace("ots:", "");
            this.referredTypes.computeIfAbsent(type, (key) -> new LinkedHashSet<>()).add(path.isEmpty()
                    ? DocumentReader.getAttribute(node, "name") : path + "." + DocumentReader.getAttribute(node, "name"));
            Node referred = getType(type);
            if (referred == null)
            {
                queue(path, node, true);
                return; // prevents reading the type now and later from queue
            }
            else
            {
                queue(path + "." + DocumentReader.getAttribute(node, "name"), referred, false);
            }
        }
        read(path, node, true);
    }

    /**
     * Reads further from an element node with a ref={ref} attribute. If the ref equals "xi:include" it is ignored, as this
     * specifies that the XML file can include another XML file. It does not specify the schema further. If the referred type is
     * not yet loaded, reading from this ref node is placed at the back of the queue.
     * @param path String; node path.
     * @param node Node; xsd:element node.
     */
    private void ref(final String path, final Node node)
    {
        String ref = DocumentReader.getAttribute(node, "ref").replace("ots:", "");
        if (ref.equals("xi:include"))
        {
            return;
        }
        this.referredElements.computeIfAbsent(ref, (key) -> new LinkedHashSet<>()).add(path);
        Node refNode = getElement(ref);
        if (refNode == null)
        {
            queue(path, node, true);
        }
        else
        {
            read(path, node, true);
        }
    }

    /**
     * Reads further from an attribute node.
     * @param path String; node path.
     * @param node Node; xsd:attribute node.
     */
    private void attribute(final String path, final Node node)
    {
        if (DocumentReader.getAttribute(node, "type") != null)
        {
            String type = DocumentReader.getAttribute(node, "type");
            String name = DocumentReader.getAttribute(node, "name");
            this.referredTypes.computeIfAbsent(type, (key) -> new LinkedHashSet<>()).add(path + "." + name);
        }
        read(path, node, true);
    }

    /**
     * Stores documentation at the current path.
     * @param path String; node path.
     * @param node Node; xsd:attribute node.
     */
    private void documentation(final String path, final Node node)
    {
        this.documentation.put(path, DocumentReader.getChild(node, "#text").getNodeValue().trim().replaceAll("\r\n", " ")
                .replaceAll("\n", " ").replaceAll("\r", " ").replaceAll("  ", ""));
    }

    /**
     * Stores the information to read in a queue.
     * @author wjschakel
     */
    private class RecursionElement
    {
        /** Path. */
        private final String path;

        /** xsd:attribute node. */
        private final Node node;

        /** Whether to extend the path. */
        private final boolean extendPath;

        /**
         * Constructor.
         * @param path String; node path.
         * @param node Node; xsd:attribute node.
         * @param extendPath boolean; whether the path should be extended with this node.
         */
        RecursionElement(final String path, final Node node, final boolean extendPath)
        {
            this.path = path;
            this.node = node;
            this.extendPath = extendPath;
        }

        /**
         * Returns the path.
         * @return String; path.
         */
        public String getPath()
        {
            return this.path;
        }

        /**
         * Returns the node.
         * @return Node; node.
         */
        public Node getNode()
        {
            return this.node;
        }

        /**
         * Returns whether to extend the path.
         * @return boolean; whether to extend the path.
         */
        public boolean isExtendPath()
        {
            return this.extendPath;
        }

    }

    /**
     * Checks that all xsd:key refer to a loaded type with their xsd:selector node. And that all xsd:field nodes point to
     * existing attributes in loaded types. This method assumes only attributes (@) and no elements (ots:) are checked.
     */
    private void checkKeys()
    {
        checkKeyOrUniques("Key", this.keys, null);
    }

    /**
     * Checks that all xsd:keyrefs refer to loaded keys. That they refer to a loaded type with their xsd:selector node. And that
     * all xsd:field nodes point to existing attributes (@), or elements (ots:), in loaded types.
     */
    private void checkKeyrefs()
    {
        for (String keyref : this.keyrefs.keySet())
        {
            Node node = this.keyrefs.get(keyref);
            if (!this.keys.containsKey(DocumentReader.getAttribute(node, "refer").replace("ots:", "")))
            {
                System.out.println(
                        "Keyref " + keyref + " refers to non existing key " + DocumentReader.getAttribute(node, "refer") + ".");
            }
            Node selected = getSelectedElement(node);
            if (selected == null)
            {
                System.out.println("Keyref " + keyref + " (" + getXpath(node) + ") not found among elements.");
            }
            else
            {
                for (Node field : DocumentReader.getChildren(node, "xsd:field"))
                {
                    String xpathField = DocumentReader.getAttribute(field, "xpath");
                    if (xpathField.startsWith("@"))
                    {
                        xpathField = xpathField.substring(1); // removes '@'
                        if (!hasElementAttribute(selected, xpathField))
                        {
                            System.out.println("Keyref " + keyref + " (" + getXpath(node) + ") points to non existing field "
                                    + xpathField + ".");
                        }
                    }
                    else
                    {
                        String type = xpathField.substring(4); // removes 'xsd:'
                        if (!this.elements.containsKey((getXpath(node) + "." + type).replace("ots:", "")))
                        {
                            System.out.println("Keyref " + keyref + " (" + getXpath(node) + ") points to non existing field "
                                    + xpathField.substring(4) + ".");
                        }
                    }
                }
            }
        }
    }

    /**
     * Checks that all xsd:unique refer to a loaded type with their xsd:selector node. And that all xsd:field nodes point to
     * existing attributes in loaded types. This method assumes only attributes (@) and no elements (ots:) are checked.
     */
    private void checkUniques()
    {
        checkKeyOrUniques("Unique", this.uniques, this.uniquesPath);
    }

    /**
     * Checks that all xsd:key or xsd:unique refer to a loaded type with their xsd:selector node. And that all xsd:field nodes
     * point to existing attributes in loaded types. This method assumes only attributes (@) and no elements (ots:) are checked.
     * @param label String; "Key" or "Unique" for command line messaging.
     * @param map Map&lt;String, Node&gt;; map of nodes, either xsd:key or xsd:unique.
     * @param paths Map&lt;String, String&gt;; map of paths where xsd:key or xsd:unique was defined. May be {@code null}, which
     *            means all are defined at root level.
     */
    private void checkKeyOrUniques(final String label, final Map<String, Node> map, final Map<String, String> paths)
    {
        for (String element : map.keySet())
        {
            Node node = map.get(element);
            String path;
            if (paths == null)
            {
                path = getXpath(node);
            }
            else
            {
                path = paths.get(element);
                path = path.isEmpty() ? getXpath(node) : path + "." + getXpath(node);
            }
            Node selected = getElement(path);
            if (selected == null)
            {
                System.out.println(label + " " + element + " (" + getXpath(node) + ") not found among elements.");
            }
            else
            {
                for (Node field : DocumentReader.getChildren(node, "xsd:field"))
                {
                    String xpathField = DocumentReader.getAttribute(field, "xpath");
                    xpathField = xpathField.substring(1); // removes '@'
                    if (!hasElementAttribute(selected, xpathField))
                    {
                        System.out.println(label + " " + element + " (" + getXpath(node) + ") points to non existing field "
                                + xpathField + ".");
                    }
                }
            }
        }
    }

    /**
     * Returns loaded element referred to from an xsd:selector child of the given node.
     * @param node Node; node (xsd:key, xsd:keyref or xsd:unique).
     * @return Node; element referred to from an xsd:selector child of the given node.
     */
    private Node getSelectedElement(final Node node)
    {
        return getElement(getXpath(node));
    }

    /**
     * Reads the xpath from an xsd:selector child of the given node.
     * @param node Node; node (xsd:key, xsd:keyref or xsd:unique).
     * @return String; xpath from an xsd:selector child of the given node.
     */
    private String getXpath(final Node node)
    {
        Node child = DocumentReader.getChild(node, "xsd:selector");
        String xpath = DocumentReader.getAttribute(child, "xpath");
        xpath = xpath.replace(".//ots:", "").replace("/ots:", ".").replace("ots:", "");
        return xpath;
    }

    /**
     * Returns whether the given node defines an element to have a specified attribute. This can be either because the node is
     * xsd:complexType in which an xsd:attribute with the specified name is defined, because it has a child xsd:complexType
     * meeting the same criteria, or because it is found in an underlying xsd:extension. For an xsd:extension, the attribute is
     * sought in both the base type, and in the specified extension elements.
     * @param node Node; node.
     * @param name String; attribute name, i.e. &lt;xsd:attribute name={name} ... &gt;.
     * @return whether the given node defines an element to have a specified attribute.
     */
    private boolean hasElementAttribute(final Node node, final String name)
    {
        if (node.getNodeName().equals("xsd:complexType"))
        {
            // node is a "xsd:complexType"
            return hasElementAttribute(node, name, null);
        }
        // node is a "xsd:element" with a "xsd:complexType"
        return hasElementAttribute(node, name, "xsd:complexType");
    }

    /**
     * Searches for an xsd:attribute in a given node. If no viaType is specified, this happens on the children of the given
     * node. Otherwise, first a child node of viaType is taken, and the children nodes of that node are considered. If this
     * method encounters an xsd:complexContent child that itself has a xsd:extension child, both the extended type and the
     * specified extension elements, are considered.
     * @param node Node; node.
     * @param name String; attribute name, i.e. &lt;xsd:attribute name={name} ... &gt;.
     * @param viaType String; viaType, can be used for recursion with or without an intermediate child layer.
     * @return whether the given node defines an element to have a specified attribute.
     */
    private boolean hasElementAttribute(final Node node, final String name, final String viaType)
    {
        Node via = viaType == null ? node : DocumentReader.getChild(node, viaType);
        for (int childIndex = 0; childIndex < via.getChildNodes().getLength(); childIndex++)
        {
            Node child = via.getChildNodes().item(childIndex);
            String childName = DocumentReader.getAttribute(child, "name");
            if (child.getNodeName().equals("xsd:attribute") && name.equals(childName))
            {
                return true;
            }
            if (child.getNodeName().equals("xsd:complexContent"))
            {
                Node extension = DocumentReader.getChild(child, "xsd:extension");
                String base = DocumentReader.getAttribute(extension, "base");
                if (base != null)
                {
                    Node baseNode = getType(base);
                    boolean has = hasElementAttribute(baseNode, name, null); // null, referred types are already complex
                    if (has)
                    {
                        return has;
                    }
                }
                boolean has = hasElementAttribute(extension, name, null); // null, xsd:extension directly contains xsd:attribute
                if (has)
                {
                    return has;
                }
            }
        }
        return false;
    }

    /**
     * Get the root node.
     * @return Node; root node.
     */
    public Node getRoot()
    {
        return this.root;
    }

    /**
     * Returns the node for the given path.
     * @param path String; path.
     * @return Node; type.
     */
    public Node getElement(final String path)
    {
        return this.elements.get(path.replace("ots:", ""));
    }

    /**
     * Returns the type, as pointed to by base={base}.
     * @param base String; type.
     * @return String; type, as pointed to by base={base}.
     */
    public Node getType(final String base)
    {
        return this.types.get(base.replace("ots:", ""));
    }

    /**
     * Returns the xsd:key and the paths where they are defined.
     * @return Map&lt;Node, String&gt;; xsd:key and the paths where they are defined.
     */
    public Map<Node, String> keys()
    {
        Map<Node, String> map = new LinkedHashMap<>();
        this.keys.forEach((key, value) -> map.put(value, this.keysPath.get(key)));
        return map;
    }

    /**
     * Returns the xsd:keyref and the paths where they are defined.
     * @return Map&lt;Node, String&gt;; xsd:keyref and the paths where they are defined.
     */
    public Map<Node, String> keyrefs()
    {
        Map<Node, String> map = new LinkedHashMap<>();
        this.keyrefs.forEach((key, value) -> map.put(value, this.keyrefsPath.get(key)));
        return map;
    }

    /**
     * /** Returns the xsd:unique and the paths where they are defined.
     * @return Map&lt;Node, String&gt;; xsd:unique and the paths where they are defined.
     */
    public Map<Node, String> uniques()
    {
        Map<Node, String> map = new LinkedHashMap<>();
        this.uniques.forEach((key, value) -> map.put(value, this.uniquesPath.get(key)));
        return map;
    }

}
