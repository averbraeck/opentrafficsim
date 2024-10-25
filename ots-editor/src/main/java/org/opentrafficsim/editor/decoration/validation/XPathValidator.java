package org.opentrafficsim.editor.decoration.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.editor.DocumentReader;
import org.opentrafficsim.editor.XsdTreeNode;
import org.w3c.dom.Node;

/**
 * Common functionality between a key/unique validator, and a keyref validator.
 * <p>
 * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class XPathValidator implements ValueValidator
{

    /** The node defining the xsd:key, xsd:keyref or xsd:unique. */
    protected final Node keyNode;

    /** Path where the key was defined. */
    protected final String keyPath;

    /** Fields of this xsd:key, xsd:unique or xsd:keyref. */
    protected final List<Field> fields;

    /**
     * Constructor.
     * @param keyNode node defining the xsd:key, xsd:unique or xsd:keyref.
     * @param keyPath path where the key was defined.
     */
    public XPathValidator(final Node keyNode, final String keyPath)
    {
        Throw.whenNull(keyNode, "Key node may not be null.");
        Throw.whenNull(keyPath, "Key path may not be null.");
        this.keyNode = keyNode;
        this.keyPath = keyPath;
        List<Node> fields = DocumentReader.getChildren(keyNode, "xsd:field");
        this.fields = new ArrayList<>();
        for (Node field : fields)
        {
            this.fields.add(new Field(DocumentReader.getAttribute(field, "xpath")));
        }
    }

    /**
     * Returns the name of the key, i.e. {@code <xsd:keyref name="Name">}.
     * @return name of the key.
     */
    public String getKeyName()
    {
        return DocumentReader.getAttribute(this.keyNode, "name");
    }

    /**
     * Returns the type {@code String} for which the xsd:key, xsd:unique or xsd:keyref applies, i.e. "GtuTypes.GtuType" for
     * {@code <xsd:selector xpath=".//ots:GtuTypes/ots:GtuType" />}. Note that multiple paths may be defined separated by "|".
     * @return type for which the xsd:key or xsd:keyref applies.
     */
    public String[] getSelectorTypeString()
    {
        return DocumentReader.getAttribute(DocumentReader.getChild(this.keyNode, "xsd:selector"), "xpath").replace("ots:", "")
                .split("\\|");
    }

    /**
     * Gathers all the field values, i.e. attribute, child element value, or own value. As validators are registered with the
     * node that has the value, attributes are gathered from the given node, while element values are taken from the correctly
     * named children of the parent. Empty values are returned as {@code null}.
     * @param node node for which to get the information.
     * @return field values.
     */
    protected List<String> gatherFieldValues(final XsdTreeNode node)
    {
        List<String> values = new ArrayList<>();
        if (node.getPathString().endsWith("DefaultInputParameters.String"))
        {
            Throw.when(this.fields.size() != 1, IllegalStateException.class,
                    "Key %s is defined as possibly being a default input parameter, but it has multiple fields.", getKeyName());
            Throw.when(!this.fields.get(0).getFullFieldName().contains("@Id"), IllegalStateException.class,
                    "Key %s is defined as possibly being a default input parameter, but it does not have a field @Id",
                    getKeyName());
            values.add(node.getId());
        }
        else
        {
            for (Field field : this.fields)
            {
                values.add(field.getValue(node));
            }
        }
        values.replaceAll((v) -> "".equals(v) ? null : v);
        return values;
    }

    /**
     * Returns a node that represent the proper context. This is a parent node of the given node, at the level where the
     * xsd:key, xsd:unique or xsd:keyref was defined.
     * @param node any node somewhere in the context, i.e. subtree.
     * @return node that represents the proper context.
     */
    protected XsdTreeNode getContext(final XsdTreeNode node)
    {
        XsdTreeNode context = null;
        List<XsdTreeNode> path = node.getPath();
        for (int index = path.size() - 1; index >= 0; index--)
        {
            if (path.get(index).getPathString().endsWith(getKeyPath()))
            {
                context = path.get(index);
                break;
            }
        }
        return context;
    }

    /**
     * Returns the path at which the xsd:key, xsd:unique or xsd:keyref is defined.
     * @return path at which the xsd:key or xsd:keyref is defined.
     */
    public String getKeyPath()
    {
        return this.keyPath;
    }

    /**
     * Returns the path at which the xsd:key, xsd:unique or xsd:keyref is defined, with dots escaped.
     * @return path at which the xsd:key or xsd:keyref is defined.
     */
    public String getKeyPathPattern()
    {
        return this.keyPath.replace(".", "\\.");
    }

    /**
     * Returns whether the given node is of the correct type and in the correct context for this validator.
     * @param node node.
     * @return whether the given node is of the correct type and in the correct context for this validator.
     */
    protected boolean isSelectedInContext(final XsdTreeNode node)
    {
        for (String selectorPath : getSelectorTypeString())
        {
            String nodePath = node.getPathString();
            if (Pattern.matches(getKeyPathPattern() + appendPattern(selectorPath), nodePath))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Adds node to this key, if applicable. Nodes are stored per parent instance that defines the context at the level of the
     * path at which the key was defined. This method is called by a listener that the root node has set up, for every created
     * node.
     * @param node node to add.
     */
    abstract public void addNode(XsdTreeNode node);

    /**
     * Remove node. It is removed from all contexts and listening keyrefs. This method is called by a listener that the root
     * node has set up, for every removed node.
     * @param node node to remove.
     */
    abstract public void removeNode(XsdTreeNode node);

    /**
     * Returns part of a pattern that can be used to find the right nodes. In particular:
     * <ul>
     * <li>".//" at the start will become ".*" meaning any nodes in between.</li>
     * <li>"/@" and anything after is cut, to ignore a final attribute.</li>
     * <li>"/" separator is replaced with ".".</li>
     * <li>"./" at the start will be cut, it refers to a node itself.</li>
     * <li>"." will be placed at the start, unless the path starts with "@" in which case an empty string is returned.</li>
     * </ul>
     * All characters are escaped for regular expression as required.
     * @param path xpath.
     * @return suitable for pattern appending.
     */
    protected static String appendPattern(final String path)
    {
        if (path.startsWith("@") || path.equals("."))
        {
            return "";
        }
        int attr = path.indexOf("/@");
        String p;
        if (attr < 0)
        {
            p = path;
        }
        else
        {
            p = path.substring(0, attr);
        }
        if (p.startsWith(".//"))
        {
            return "\\..*" + p.substring(3).replace("/", "\\.");
        }
        if (p.startsWith("./"))
        {
            return "\\." + p.substring(2).replace("/", "\\.");
        }
        return "\\." + p.replace("/", "\\.");
    }

    /**
     * Transforms an xpath specified path to a path that is presentable to the user.
     * @param path xpath specified path.
     * @return path that is presentable to the user.
     */
    protected static String user(final String path)
    {
        return path.replace(".//", "").replace("@", "").replace("/", ".");
    }

    /**
     * The field class represents an xsd:field tag within an xsd:key, xsd:unique or xsd:keyref. It can return values for a node
     * that is consistent with the xsd:selector. This class can deal with node values, attributes, and nested child elements. It
     * can also deal with multiple field names defined for one field, e.g. "@Id|ots:ChildElement".
     * <p>
     * Copyright (c) 2024-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved.
     * <br>
     * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
     * </p>
     * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
     */
    public class Field
    {
        /** Complete field path. */
        private final String fullFieldPath;

        /** Separate field paths that were separated by "|". Any superfluous occurrence of "/./" has been changed to "/". */
        private final String[] fieldPaths;

        /**
         * Constructor.
         * @param fullFieldPath complete field path, e.g. "@GtuType" or "@Id|ots:TrafficLight"
         */
        public Field(final String fullFieldPath)
        {
            this.fullFieldPath = fullFieldPath.replace("ots:", "").replace(" ", "");
            this.fieldPaths = this.fullFieldPath.split("\\|");
            for (int i = 0; i < this.fieldPaths.length; i++)
            {
                if (this.fieldPaths[i].startsWith(".//"))
                {
                    this.fieldPaths[i] = ".//" + this.fieldPaths[i].substring(3).replace("./", "");
                }
                else
                {
                    this.fieldPaths[i] = this.fieldPaths[i].replace("./", "");
                }
            }
        }

        /**
         * Returns which field path is valid for the given node.
         * @param node node.
         * @return index of the valid field name.
         */
        public int getValidPathIndex(final XsdTreeNode node)
        {
            String nodePath = node.getPathString();
            for (String selector : getSelectorTypeString())
            {
                for (int i = 0; i < this.fieldPaths.length; i++)
                {
                    String fieldName = this.fieldPaths[i];
                    if (Pattern.matches(getKeyPathPattern() + appendPattern(selector) + appendPattern(fieldName), nodePath))
                    {
                        int attr = fieldName.indexOf("@");
                        if (attr < 0 || fieldName.equals("."))
                        {
                            if (node.isEditable())
                            {
                                return i;
                            }
                        }
                        else
                        {
                            String attribute = fieldName.substring(attr + 1);
                            if (node.hasAttribute(attribute))
                            {
                                return i;
                            }
                        }
                    }
                }
            }
            return -1;
        }

        /**
         * Returns the field path at the given index. Any superfluous occurrence of "/./" has been changed to "/".
         * @param index index.
         * @return field path at the given index.
         */
        public String getFieldPath(final int index)
        {
            return this.fieldPaths[index];
        }

        /**
         * Returns the field value of the given node.
         * @param node node.
         * @return value of the given node for this field.
         */
        public String getValue(final XsdTreeNode node)
        {
            for (String fieldPath : this.fieldPaths)
            {
                if (fieldPath.equals(".") && node.isEditable())
                {
                    return node.getValue();
                }
                if (fieldPath.startsWith("@"))
                {
                    String attribute = fieldPath.substring(1);
                    if (node.hasAttribute(attribute))
                    {
                        return node.getAttributeValue(attribute);
                    }
                }
                // a child node may be calling this method, check whether the given node is the child directly
                String nodePath = node.getPathString();
                for (String selector : getSelectorTypeString())
                {
                    if (Pattern.matches(getKeyPathPattern() + appendPattern(selector) + appendPattern(fieldPath), nodePath))
                    {
                        if (node.isEditable())
                        {
                            return node.getValue();
                        }
                        throw new RuntimeException(
                                "Field " + this.fullFieldPath + " points to a node that cannot give a value.");
                    }
                }
                // if not a child directly, recursively find it
                try
                {
                    return getChildValue(node, fieldPath);
                }
                catch (NoSuchElementException ex)
                {
                    // there can be more field names which may supply a value
                }
            }
            throw new RuntimeException("Field " + this.fullFieldPath + " cannot be found in node " + node);
        }

        /**
         * Returns child value by recursively moving down nodes. If the child path ends with an attribute, the attribute value
         * of the node is returned.
         * @param node node.
         * @param path path.
         * @return value of child element.
         * @throws NoSuchElementException if there is no such child that can deliver a value.
         */
        private String getChildValue(final XsdTreeNode node, final String path) throws NoSuchElementException
        {
            if (path.startsWith(".//"))
            {
                // It can be any child node whose final path matches the path in the context
                for (XsdTreeNode child : node.getChildren())
                {
                    // substring(1) to ignore the initial dot
                    if (Pattern.matches(appendPattern(path).substring(1), child.getPathString()))
                    {
                        int attr = path.indexOf("/@");
                        if (attr < 0)
                        {
                            if (child.isEditable())
                            {
                                return node.getValue();
                            }
                        }
                        else
                        {
                            String attribute = path.substring(attr + 2);
                            if (node.hasAttribute(attribute))
                            {
                                return node.getAttributeValue(attribute);
                            }
                        }
                        throw new NoSuchElementException("Node " + node + " does not have a field " + path + ".");
                    }
                    getChildValue(child, path);
                }
                throw new NoSuchElementException("Node " + node + " does not have a field " + path + ".");
            }
            // Path without unknown intermediate layers
            int sep = path.indexOf("/");
            if (sep < 0)
            {
                // Leaf of the path
                if (path.startsWith("@"))
                {
                    String attribute = path.substring(1);
                    if (node.hasAttribute(attribute))
                    {
                        return node.getAttributeValue(attribute);
                    }
                }
                else
                {
                    XsdTreeNode child = node.getFirstChild(path);
                    if (child.isEditable())
                    {
                        return node.getValue();
                    }
                }
                throw new NoSuchElementException("Node " + node + " does not have a field " + path + ".");
            }
            // Recursively go down the layers
            return getChildValue(node.getFirstChild(path.substring(0, sep)), path.substring(sep + 1));
        }

        /**
         * Returns the field name.
         * @return field name.
         */
        public String getFullFieldName()
        {
            return this.fullFieldPath;
        }

        @Override
        public String toString()
        {
            return "Field " + this.fullFieldPath;
        }
    }

}
