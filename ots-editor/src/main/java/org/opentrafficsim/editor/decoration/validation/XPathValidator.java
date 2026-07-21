package org.opentrafficsim.editor.decoration.validation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import org.djutils.exceptions.Throw;
import org.opentrafficsim.base.OtsRuntimeException;
import org.opentrafficsim.base.logger.Logger;
import org.opentrafficsim.editor.DocumentReader;
import org.opentrafficsim.editor.XsdTreeNode;
import org.w3c.dom.Node;

/**
 * Common functionality between a key/unique validator, and a keyref validator. In case field paths refer to child nodes within
 * a node selected by the selector of the key/unique/keyref, a validator should be assigned to the child node's value or the
 * child node's relevant attribute. The method {@link Field#getValidPathIndex} returns a valid path index for such nodes, and
 * this method can be used to determine whether to assign a validator to a node.
 * <p>
 * Copyright (c) 2024-2026 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author Wouter Schakel
 */
public abstract class XPathValidator implements ValueValidator
{

    /** Ordering id. **/
    private final long orderingId = NEXT_ID.incrementAndGet();

    /** The node defining the xsd:key, xsd:keyref or xsd:unique. */
    private final Node keyNode;

    /** Path where the key was defined, defining the context. */
    private final String keyPath;

    /** Path where the key was defined, defining the context, with dots escaped. */
    private final String keyPathPattern;

    /** Boolean supplier with logic on when to ignore changes. */
    private final Supplier<Boolean> ignoreChanges;

    /** Selectors, as separated by "|". */
    private final String[] selectors;

    /** Matching pattern per selector. */
    private final Pattern[] selectorPatterns;

    /** Fields of this xsd:key, xsd:unique or xsd:keyref. */
    private final List<Field> fields;

    /**
     * Constructor.
     * @param keyNode node defining the xsd:key, xsd:unique or xsd:keyref
     * @param keyPath path where the key was defined, defining the context
     * @param ignoreChanges boolean supplier with logic on when to ignore changes
     */
    public XPathValidator(final Node keyNode, final String keyPath, final Supplier<Boolean> ignoreChanges)
    {
        Throw.whenNull(keyNode, "keyNode");
        Throw.whenNull(keyPath, "keyPath");
        Throw.whenNull(ignoreChanges, "ignoreChanges");
        this.keyNode = keyNode;
        this.keyPath = keyPath;
        this.keyPathPattern = this.keyPath.replace(".", "\\.");
        this.ignoreChanges = ignoreChanges;

        // selectors
        this.selectors = DocumentReader.getAttribute(DocumentReader.getChild(this.keyNode, "xsd:selector").get(), "xpath").get()
                .replace("ots:", "").split("\\|");
        this.selectorPatterns = new Pattern[this.selectors.length];
        for (int patternIndex = 0; patternIndex < this.selectors.length; patternIndex++)
        {
            this.selectorPatterns[patternIndex] =
                    Pattern.compile(this.keyPathPattern + appendPattern(this.selectors[patternIndex]));
        }

        // fields
        this.fields = new ArrayList<>();
        for (Node field : DocumentReader.getChildren(keyNode, "xsd:field"))
        {
            this.fields.add(new Field(DocumentReader.getAttribute(field, "xpath").get()));
        }
    }

    /**
     * Whether validation should be ignored.
     * @return whether validation should be ignored
     */
    protected boolean ignoreChanges()
    {
        return this.ignoreChanges.get();
    }

    /**
     * Returns the key XSD node.
     * @return the key XSD node
     */
    protected Node getKeyNode()
    {
        return this.keyNode;
    }

    /**
     * Returns the path at which the xsd:key, xsd:unique or xsd:keyref is defined.
     * @return path at which the xsd:key or xsd:keyref is defined.
     */
    protected String getKeyPath()
    {
        return this.keyPath;
    }

    /**
     * Returns the fields of the key, unique or keyref.
     * @return the fields of the key, unique or keyref (unsafe, not a copy)
     */
    protected List<Field> getFields()
    {
        return this.fields;
    }

    /**
     * Returns the name of the key, i.e. {@code <xsd:keyref name="Name">}.
     * @return name of the key
     */
    public String getKeyName()
    {
        return DocumentReader.getAttribute(this.keyNode, "name").get();
    }

    /**
     * Returns the type {@code String}s for which the xsd:key, xsd:unique or xsd:keyref applies, i.e. "GtuTypes.GtuType" for
     * {@code <xsd:selector xpath=".//ots:GtuTypes/ots:GtuType" />}. Note that multiple paths may be defined separated by "|".
     * @return type for which the xsd:key or xsd:keyref applies
     */
    public String[] getSelectorTypeString()
    {
        return this.selectors;
    }

    /**
     * Gathers all the field values, i.e. attribute, child element value, or own value. The given node may either be a child
     * node that a field path points to (who's value or attribute is relevant for one field) or the key node as selected by the
     * selector. Empty values are returned as {@code null}.
     * @param keyOrChildNode node for which to get the information
     * @return field values
     */
    protected List<String> gatherFieldValues(final XsdTreeNode keyOrChildNode)
    {
        List<String> values = new ArrayList<>();
        if (keyOrChildNode.getPathString().endsWith("DefaultInputParameters.String"))
        {
            Throw.when(this.fields.size() != 1, IllegalStateException.class,
                    "Key %s is defined as possibly being a default input parameter, but the key has multiple fields.",
                    getKeyName());
            Throw.when(!Arrays.stream(this.fields.get(0).fieldPaths).anyMatch((s) -> "@Id".equals(s)),
                    IllegalStateException.class,
                    "Key %s is defined as possibly being a default input parameter, but the key does not have a field @Id",
                    getKeyName());
            values.add(keyOrChildNode.getValue());
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
     * @param node any node somewhere in the context, i.e. subtree
     * @return node that represents the proper context
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
     * Returns whether the given node is of the correct type by any selector and in the correct context for this validator.
     * @param node node
     * @return whether the given node is of the correct type by any selector and in the correct context for this validator
     */
    protected boolean isSelectedInContext(final XsdTreeNode node)
    {
        for (int selectorIndex = 0; selectorIndex < this.selectors.length; selectorIndex++)
        {
            if (isSelectedInContext(node, selectorIndex))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Returns whether the given node is of the correct type by the selector and in the correct context for this validator.
     * @param node node
     * @param selectorIndex index of the selector
     * @return whether the given node is of the correct type by the selector and in the correct context for this validator
     */
    protected boolean isSelectedInContext(final XsdTreeNode node, final int selectorIndex)
    {
        return this.selectorPatterns[selectorIndex].matcher(node.getPathString()).matches();
    }

    /**
     * Adds node to this key, if applicable. Nodes are stored per parent instance that defines the context at the level of the
     * path at which the key was defined. This method is called by a listener that the root node has set up, for every created
     * node.
     * @param node node to add
     */
    public abstract void addNode(XsdTreeNode node);

    /**
     * Remove node. It is removed from all contexts and listening keyrefs. This method is called by a listener that the root
     * node has set up, for every removed node.
     * @param node node to remove
     */
    public void removeNode(final XsdTreeNode node)
    {
        for (Field field : this.fields)
        {
            Iterator<FieldValue> it = field.attachedNodes.values().iterator();
            while (it.hasNext())
            {
                FieldValue next = it.next();
                if (node.equals(next.valueNode()))
                {
                    it.remove();
                }
            }
        }
    }

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
     * @param path xpath
     * @return suitable for pattern appending
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
     * @param path xpath specified path
     * @return path that is presentable to the user
     */
    static String userFriendlyXPath(final String path)
    {
        return path.replace(".//", "").replace("@", "").replace("/", ".");
    }

    @Override
    public long getOrderingId()
    {
        return this.orderingId;
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

        /** Cached patterns for each selector, and each fieldPath. */
        private final Pattern[][] patterns;

        /** Cached attributes in field paths. */
        private final String[] attributes;

        /** Cached direct field path matches. */
        private final boolean[] directMatch;

        /** Mapping from {@link XsdTreeNode} to {@link FieldValue} to recognize when there is not a single value. */
        private final Map<XsdTreeNode, FieldValue> attachedNodes = new LinkedHashMap<>();

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

            // pre-compute cache
            this.patterns = new Pattern[XPathValidator.this.selectors.length][this.fieldPaths.length];
            for (int s = 0; s < XPathValidator.this.selectors.length; s++)
            {
                for (int f = 0; f < this.fieldPaths.length; f++)
                {
                    this.patterns[s][f] = Pattern.compile(XPathValidator.this.keyPathPattern
                            + appendPattern(XPathValidator.this.selectors[s]) + appendPattern(this.fieldPaths[f]));
                }
            }
            this.attributes = new String[this.fieldPaths.length];
            this.directMatch = new boolean[this.fieldPaths.length];
            for (int f = 0; f < this.fieldPaths.length; f++)
            {
                int attr = this.fieldPaths[f].indexOf('@');
                if (attr < 0 || this.fieldPaths[f].equals("."))
                {
                    this.directMatch[f] = true;
                }
                else
                {
                    this.attributes[f] = this.fieldPaths[f].substring(attr + 1);
                }
            }
        }

        /**
         * Attach to field path if there is a valid field for the given node. As per XSD Schema semantics, only one value may
         * attach to a field. If there is a valid path index the input node should be validated by the xsd:key, xsd:unique or
         * xsd:keyref validator.
         * @param node node
         * @return index of the valid field name, or -1 if no such field
         * @throws IllegalStateException when this validator can attach to multiple field paths
         */
        int attach(final XsdTreeNode node)
        {
            return getValidPathIndex0(node, true);
        }

        /**
         * Returns which field path is valid for the given node. That is for a field with path "@Id|ots:TrafficLight" it can be
         * either 0 or 1 for matched nodes, or -1 for unmatched nodes. This method does not return the field index, as this
         * class {@link Field} represents only a single field of the xsd:key, xsd:unique or xsd:keyref. If there is a valid path
         * index the input node should be validated by the xsd:key, xsd:unique or xsd:keyref validator.
         * @param node node
         * @return index of the valid field name, or -1 if no such field
         * @throws IllegalStateException when this validator can attach to multiple field paths
         */
        int getValidPathIndex(final XsdTreeNode node)
        {
            return getValidPathIndex0(node, false);
        }

        /**
         * Returns which field path is valid for the given node. That is for a field with path "@Id|ots:TrafficLight" it can be
         * either 0 or 1 for matched nodes, or -1 for unmatched nodes. This method does not return the field index, as this
         * class {@link Field} represents only a single field of the xsd:key, xsd:unique or xsd:keyref. If there is a valid path
         * index the input node should be validated by the xsd:key, xsd:unique or xsd:keyref validator.
         * @param node node
         * @param attach whether to attach
         * @return index of the valid field name, or -1 if no such field
         * @throws IllegalStateException when this validator can attach to multiple field paths
         */
        private int getValidPathIndex0(final XsdTreeNode node, final boolean attach)
        {
            String nodePath = node.getPathString();
            int out = -1;
            for (int s = 0; s < XPathValidator.this.selectors.length; s++)
            {
                for (int f = 0; f < this.fieldPaths.length; f++)
                {
                    if (this.patterns[s][f].matcher(nodePath).matches())
                    {
                        if (this.directMatch[f] || node.hasAttribute(this.attributes[f]))
                        {
                            if (attach)
                            {
                                attach(s, f, node);
                                out = f; // do not return so attach can detect multiple value (attribute or node) linking
                            }
                            else
                            {
                                return f;
                            }
                        }
                    }
                }
            }
            return out;
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
         * Attaches this field to the node on a path index.
         * @param selectorIndex selector index
         * @param fieldPathIndex index of the valid field name
         * @param node node
         */
        private void attach(final int selectorIndex, final int fieldPathIndex, final XsdTreeNode node)
        {
            FieldValue next = new FieldValue(node, fieldPathIndex);
            FieldValue prev = this.attachedNodes.put(getSelectorNode(node, selectorIndex), next);// new
                                                                                                 // SelectedNode(getContext(node),
                                                                                                 // getSelectorNode(node,
                                                                                                 // selectorIndex)), next);
            if (prev != null && !prev.equals(next))
            {
                Logger.ots().error("Node {} already attached to field {} on key \"{}\". This field expression resolves to"
                        + " multiple field paths. XML Schema identity constraints require each field to identify a single"
                        + " value. Only one of the alternatives should exist for each selected node.", node.getPathString(),
                        this.fullFieldPath, getKeyName());
            }
        }

        /**
         * Obtains the node to which a selector applies from a node that attaches to any of the field paths.
         * @param node node attaching to a field path
         * @param selectorIndex selector that is attache through
         * @return the node to which a selector applies from a node that attaches to any of the field paths
         */
        private XsdTreeNode getSelectorNode(final XsdTreeNode node, final int selectorIndex)
        {
            XsdTreeNode selectorNode = node;
            while (selectorNode != null && !isSelectedInContext(selectorNode, selectorIndex)) // no loops for case 1
            {
                selectorNode = selectorNode.getParent();
            }
            return selectorNode;
        }

        /**
         * Returns the field value for the given node. The node can be any of three different types of nodes:
         * <ul>
         * <li>A key node at the level of the selector.</li>
         * <li>A child node related to this field and any of the contained field paths (separated by "|").</li>
         * <li>Some other child node not pertaining to this field, but pertaining to some other field in the same
         * key/unique/keyref.</li>
         * </ul>
         * In the last case, the parent node that is the key node is found. From this key node, as well as for the first case, a
         * relevant child node for this field is recursively found and used to return the value or attribute value.
         * <p>
         * @param keyOrChildNode node
         * @return value of the given node for this field
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
            Throw.when(keyLevelNode == null, OtsRuntimeException.class, "Unable to get value from node " + keyOrChildNode
                    + " within key/unique/keyref " + getKeyName() + " field " + this.fullFieldPath);
            for (String fieldPath : this.fieldPaths)
            {
                try
                {
                    return getChildValue(keyLevelNode, fieldPath, null);
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
         * @param node node
         * @param path path
         * @param pathPattern path pattern, may be {@code null} in which case it is created if needed and recursively forwarded
         * @return value of child element
         * @throws NoSuchElementException if there is no such child that can deliver a value
         */
        private String getChildValue(final XsdTreeNode node, final String path, final Pattern pathPattern)
                throws NoSuchElementException
        {
            if (path.startsWith(".//"))
            {
                Pattern pattern = pathPattern;
                // It can be any child node whose final path matches the path in the context
                for (XsdTreeNode child : node.getChildren())
                {
                    // ".//" has become "..*", substring(1) to ignore the initial dot, then it's effectively an endsWith()
                    pattern = pattern != null ? pattern : Pattern.compile(appendPattern(path).substring(1));
                    if (pattern.matcher(child.getPathString()).matches())
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
                    getChildValue(child, path, pattern);
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
            return getChildValue(node.getFirstChild(path.substring(0, sep)), path.substring(sep + 1), null);
        }

        /**
         * Returns the field name.
         * @return field name
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

    /**
     * Small helper data container to recognize when different {@link FieldValue} attach to the same selected
     * {@link XsdTreeNode}.
     * @param valueNode node to which the key attaches for a value
     * @param fieldPathIndex index of the path, e.g. 1 for "@Id" when the field has an xpath "@GtuType|@Id"
     */
    private record FieldValue(XsdTreeNode valueNode, int fieldPathIndex)
    {
    };

}
