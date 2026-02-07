package org.opentrafficsim.editor.decoration.validation;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.regex.Pattern;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.editor.DocumentReader;
import org.opentrafficsim.editor.XsdTreeNode;
import org.w3c.dom.Node;

/**
 * Common functionality between a key/unique validator, and a keyref validator. In case field paths refer to child nodes within
 * a node selected by the selector of the key/unique/keyref, a validator should be assigned to the child node's value or the
 * child node's relevant attribute. The method {@code Field.getValidPathIndex()} returns a valid path index for such nodes, and
 * this method can be used to determine whether to assign a validator to a node.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public abstract class XPathValidator implements ValueValidator
{

    /** The node defining the xsd:key, xsd:keyref or xsd:unique. */
    private final Node keyNode;

    /** Path where the key was defined, defining the context. */
    private final String keyPath;

    /** Fields of this xsd:key, xsd:unique or xsd:keyref. */
    private final List<Field> fields;

    /**
     * Constructor.
     * @param keyNode node defining the xsd:key, xsd:unique or xsd:keyref.
     * @param keyPath path where the key was defined, defining the context.
     */
    public XPathValidator(final Node keyNode, final String keyPath)
    {
        Throw.whenNull(keyNode, "Key node may not be null.");
        Throw.whenNull(keyPath, "Key path may not be null.");
        this.keyNode = keyNode;
        this.keyPath = keyPath;
        this.fields = new ArrayList<>();
        for (Node field : DocumentReader.getChildren(keyNode, "xsd:field"))
        {
            this.fields.add(new Field(DocumentReader.getAttribute(field, "xpath").get()));
        }
    }

    /**
     * Returns the key XSD node.
     * @return the key XSD node
     */
    Node getKeyNode()
    {
        return this.keyNode;
    }

    /**
     * Returns the path at which the xsd:key, xsd:unique or xsd:keyref is defined.
     * @return path at which the xsd:key or xsd:keyref is defined.
     */
    String getKeyPath()
    {
        return this.keyPath;
    }

    /**
     * Returns the fields of the key, unique or keyref.
     * @return the fields of the key, unique or keyref
     */
    List<Field> getFields()
    {
        return this.fields;
    }

    /**
     * Returns the name of the key, i.e. {@code <xsd:keyref name="Name">}.
     * @return name of the key.
     */
    public String getKeyName()
    {
        return DocumentReader.getAttribute(this.keyNode, "name").get();
    }

    /**
     * Returns the type {@code String} for which the xsd:key, xsd:unique or xsd:keyref applies, i.e. "GtuTypes.GtuType" for
     * {@code <xsd:selector xpath=".//ots:GtuTypes/ots:GtuType" />}. Note that multiple paths may be defined separated by "|".
     * @return type for which the xsd:key or xsd:keyref applies.
     */
    public String[] getSelectorTypeString()
    {
        return DocumentReader.getAttribute(DocumentReader.getChild(this.keyNode, "xsd:selector").get(), "xpath").get()
                .replace("ots:", "").split("\\|");
    }

    /**
     * Gathers all the field values, i.e. attribute, child element value, or own value. The given node may either be a child
     * node that a field path points to (who's value or attribute is relevant for one field) or the key node as selected by the
     * selector. Empty values are returned as {@code null}.
     * @param keyOrChildNode node for which to get the information.
     * @return field values.
     */
    List<String> gatherFieldValues(final XsdTreeNode keyOrChildNode)
    {
        List<String> values = new ArrayList<>();
        if (keyOrChildNode.getPathString().endsWith("DefaultInputParameters.String"))
        {
            Throw.when(this.fields.size() != 1, IllegalStateException.class,
                    "Key %s is defined as possibly being a default input parameter, but it has multiple fields.", getKeyName());
            Throw.when(!this.fields.get(0).getFullFieldName().contains("@Id"), IllegalStateException.class,
                    "Key %s is defined as possibly being a default input parameter, but it does not have a field @Id",
                    getKeyName());
            values.add(keyOrChildNode.getId());
        }
        else
        {
            for (Field field : this.fields)
            {
                values.add(field.getValue(keyOrChildNode));
            }
        }
        values.replaceAll((v) -> "".equals(v) ? null : v);
        return values;
    }

    /**
     * Returns a node that represents the proper context. This is a parent node of the given node, at the level where the
     * xsd:key, xsd:unique or xsd:keyref was defined.
     * @param node any node somewhere in the context, i.e. subtree.
     * @return node that represents the proper context.
     */
    XsdTreeNode getContext(final XsdTreeNode node)
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
     * Returns the path at which the xsd:key, xsd:unique or xsd:keyref is defined, with dots escaped.
     * @return path at which the xsd:key, xsd:unique or xsd:keyref is defined.
     */
    private String getKeyPathPattern()
    {
        return this.keyPath.replace(".", "\\.");
    }

    /**
     * Returns whether the given node is of the correct type by the selector and in the correct context for this validator.
     * @param node node.
     * @return whether the given node is of the correct type by the selector and in the correct context for this validator.
     */
    boolean isSelectedInContext(final XsdTreeNode node)
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
    public abstract void addNode(XsdTreeNode node);

    /**
     * Remove node. It is removed from all contexts and listening keyrefs. This method is called by a listener that the root
     * node has set up, for every removed node.
     * @param node node to remove.
     */
    public abstract void removeNode(XsdTreeNode node);

    /**
     * Returns part of a matching pattern that can be used to find the right nodes. In particular:
     * <ul>
     * <li>".//" at the start will become "..*" meaning any nodes in between.</li>
     * <li>"/@" and anything after is cut, to ignore a final attribute.</li>
     * <li>"/" separator is replaced with ".".</li>
     * <li>"./" at the start will be cut, it refers to a node itself.</li>
     * <li>"." will be placed at the start, unless the path starts with "@" or is ".", in which case an empty string is
     * returned.</li>
     * </ul>
     * All characters are escaped for regular expression as required.
     * @param path xpath.
     * @return suitable for pattern appending.
     */
    private static String appendPattern(final String path)
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
     * Transforms an xpath specified path to a path that is presentable to the user. In particular:
     * <ul>
     * <li>".//" is removed.</li>
     * <li>"@" is removed.</li>
     * <li>"/" separator is replaced with ".".</li>
     * </ul>
     * @param path xpath specified path.
     * @return path that is presentable to the user.
     */
    static String userFriendlyXPath(final String path)
    {
        return path.replace(".//", "").replace("@", "").replace("/", ".");
    }

    /**
     * The field class represents an xsd:field tag within an xsd:key, xsd:unique or xsd:keyref. It can return values for any
     * node that is consistent with the xsd:selector and the xsd:field value of this field. This class can deal with node
     * values, attributes, and nested child elements. It can also deal with multiple field names defined for one field, e.g.
     * "@Id|ots:ChildElement".
     */
    class Field
    {
        /** Complete field path. */
        private final String fullFieldPath;

        /** Separate field paths that were separated by "|". Any superfluous occurrence of "/./" has been changed to "/". */
        private final String[] fieldPaths;

        /**
         * Constructor.
         * @param fullFieldPath complete field path, e.g. "@GtuType" or "@Id|ots:TrafficLight"
         */
        Field(final String fullFieldPath)
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
         * Returns which field path is valid for the given node. That is for a field with "@Id|ots:TrafficLight" it can be
         * either 0 or 1 for matched nodes, or -1 for unmatched nodes. This method does not return the field index, as this
         * class represents only a single field. If there is a valid path index the input node should be validated by the
         * xsd:key, xsd:unique or xsd:keyref validator.
         * @param node node.
         * @return index of the valid field name, or -1 if no such field.
         */
        int getValidPathIndex(final XsdTreeNode node)
        {
            String nodePath = node.getPathString();
            for (String selector : getSelectorTypeString())
            {
                for (int i = 0; i < this.fieldPaths.length; i++)
                {
                    String fieldPath = this.fieldPaths[i];
                    if (Pattern.matches(getKeyPathPattern() + appendPattern(selector) + appendPattern(fieldPath), nodePath))
                    {
                        int attr = fieldPath.indexOf("@");
                        if (attr < 0 || fieldPath.equals("."))
                        {
                            if (node.isEditable())
                            {
                                return i;
                            }
                        }
                        else
                        {
                            String attribute = fieldPath.substring(attr + 1);
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
        String getFieldPath(final int index)
        {
            return this.fieldPaths[index];
        }

        /**
         * Returns the field value for the given node. The node can be any of three different types of nodes:
         * <ul>
         * <li>A key node at the level of the selector.</li>
         * <li>A child node related to this field and any of the contained field paths (separated by "|").</li>
         * <li>Some other child node not pertaining to this field, but pertaining to some other field.</li>
         * </ul>
         * In the last case, the parent node that is the key node is found. From this key node, as well as for the first case, a
         * relevant child node for this field is recursively found and used to return the value or attribute value.
         * <p>
         * @param keyOrChildNode node.
         * @return value of the given node for this field.
         */
        private String getValue(final XsdTreeNode keyOrChildNode)
        {
            int validPathIndex = getValidPathIndex(keyOrChildNode);
            if (validPathIndex >= 0)
            {
                // input node is child node pertaining to this field (case 2)
                String fieldPath = this.fieldPaths[validPathIndex];
                int attr = fieldPath.indexOf("@");
                if (attr < 0)
                {
                    if (keyOrChildNode.isEditable())
                    {
                        return keyOrChildNode.getValue();
                    }
                    throw new OtsRuntimeException("Field path " + fieldPath + " points to a node that cannot give a value.");
                }
                String attribute = fieldPath.substring(attr + 1);
                if (keyOrChildNode.hasAttribute(attribute))
                {
                    return keyOrChildNode.getAttributeValue(attribute);
                }
                throw new OtsRuntimeException(
                        "Field path " + fieldPath + " points to a node that does not have attribute " + attribute + " .");
            }
            // input node is child node not pertaining to this field (case 3), or it is the key node (case 1)
            XsdTreeNode keyLevelNode = keyOrChildNode;
            while (keyLevelNode != null && !isSelectedInContext(keyLevelNode)) // no loops for case 1
            {
                keyLevelNode = keyLevelNode.getParent();
            }
            Throw.when(keyLevelNode == null, OtsRuntimeException.class, "Unable to get valued from node " + keyOrChildNode
                    + " within key/unique/keyref " + getKeyName() + " field " + this.fullFieldPath);
            for (String fieldPath : this.fieldPaths)
            {
                try
                {
                    return getChildValue(keyLevelNode, fieldPath);
                }
                catch (NoSuchElementException ex)
                {
                    // there can be more field names which may supply a value
                }
            }
            throw new OtsRuntimeException("Field " + this.fullFieldPath + " cannot be found for node " + keyOrChildNode);
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
                    // ".//" has become "..*", substring(1) to ignore the initial dot, then it's effectively an endsWith()
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
            // Recursively go down the layers, note paths must refer to children of which there can only be 1 by XSD rules
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
