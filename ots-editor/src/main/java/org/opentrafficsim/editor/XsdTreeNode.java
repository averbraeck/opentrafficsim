package org.opentrafficsim.editor;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import javax.xml.parsers.ParserConfigurationException;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.event.EventListenerMap;
import org.djutils.event.EventType;
import org.djutils.event.LocalEventProducer;
import org.djutils.event.reference.Reference;
import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.ImmutableArrayList;
import org.djutils.immutablecollections.ImmutableList;
import org.djutils.metadata.MetaData;
import org.djutils.metadata.ObjectDescriptor;
import org.opentrafficsim.editor.decoration.validation.CoupledValidator;
import org.opentrafficsim.editor.decoration.validation.KeyValidator;
import org.opentrafficsim.editor.decoration.validation.ValueValidator;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 * Underlying data structure object of the editor. Starting with the root node "Ots", all the information is stored in a tree.
 * The tree follows the XSD logic, e.g. "Ots.Network.Link". {@code XsdTreeNode}'s have a {@code Node} object from the XSD DOM
 * tree. From this information it can be derived what the child nodes should be, and which attributes are contained.<br>
 * <br>
 * This class is mostly straightforward in the sense that there are direct parent-child relations, and that changing an option
 * replaces a node. When an xsd:sequence is part of an xsd:choice or xsd:all, things become complex as the xsd:sequence is a
 * single option. Therefore the xsd:sequence becomes a node visible in the tree, when it's an option under a choice.
 * Furthermore, for each xsd:choice or xsd:all node an {@code XsdTreeNode} is created that is not visible in the tree. It stores
 * all options {@code XsdTreeNode}'s and knows what option is selected. Only one options is ever in the list of children of the
 * parent node.
 * <p>
 * Copyright (c) 2023-2024 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="https://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * @author <a href="https://github.com/wjschakel">Wouter Schakel</a>
 */
public class XsdTreeNode extends LocalEventProducer implements Serializable
{

    /** */
    private static final long serialVersionUID = 20230224L;

    /** Event when a node value is changed. */
    public static final EventType VALUE_CHANGED = new EventType("VALUECHANGED",
            new MetaData("Value changed", "Value changed on node",
                    new ObjectDescriptor("Node", "Node with changed value", XsdTreeNode.class),
                    new ObjectDescriptor("Previous", "Previous node value", String.class)));

    /** Event when an attribute value is changed. */
    public static final EventType ATTRIBUTE_CHANGED = new EventType("ATTRIBUTECHANGED",
            new MetaData("Attribute changed", "Attribute changed on node",
                    new ObjectDescriptor("Node", "Node with changed attribute value", XsdTreeNode.class),
                    new ObjectDescriptor("Attribute", "Name of the attribute", String.class),
                    new ObjectDescriptor("Previous", "Previous attribute value", String.class)));

    /** Event when an option is changed. */
    public static final EventType OPTION_CHANGED = new EventType("OPTIONCHANGED",
            new MetaData("Option changed", "Option changed on node",
                    new ObjectDescriptor("Node", "Node on which the event is called", XsdTreeNode.class),
                    new ObjectDescriptor("Selected", "Newly selected option node", XsdTreeNode.class),
                    new ObjectDescriptor("Previous", "Previously selected option node", XsdTreeNode.class)));

    /** Event when an option is changed. */
    public static final EventType ACTIVATION_CHANGED = new EventType("ACTIVATIONCHANGED",
            new MetaData("Activation changed", "Activation changed on node",
                    new ObjectDescriptor("Node", "Node with changed activation.", XsdTreeNode.class),
                    new ObjectDescriptor("Activation", "New activation state.", Boolean.class)));

    /** Event when a node is moved. */
    public static final EventType MOVED = new EventType("MOVED",
            new MetaData("Node moved", "Node moved", new ObjectDescriptor("Node", "Node that was moved.", XsdTreeNode.class),
                    new ObjectDescriptor("OldIndex", "Old index.", Integer.class),
                    new ObjectDescriptor("NewIndex", "New index.", Integer.class)));

    /** Limit on displayed option name to avoid huge menu's. */
    private static final int MAX_OPTIONNAME_LENGTH = 64;

    /** Parent node. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    XsdTreeNode parent;

    /** Node from XSD that this {@code XsdTreeNode} represents. Most typically an xsd:element node. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Node xsdNode;

    /** Nodes from XSD that are between the XSD node of the parent, and this tree node's XSD node. */
    private final ImmutableList<Node> hiddenNodes;

    /**
     * Element defining node that referred to a type. The type is defined by {@code .xsdNode}, the referring node is used for
     * original information on name and occurrence. For simple element nodes this is {@code null}.
     */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    Node referringXsdNode;

    /** XSD schema from which to get type and element nodes that are referred to. */
    private final Schema schema;

    /** Minimum number of this element under the parent node, as defined in minOccurs in XSD. */
    private int minOccurs = 0;

    /** Maximum number of this element under the parent node, as defined in maxOccurs in XSD. */
    private int maxOccurs = -1;

    /**
     * Path string of this element, e.g. "OTS.Definitions.RoadLayouts". This is used to identify each unique type of element.
     */
    private final String pathString;

    // ====== Choice/Options ======

    /** Choice node, represents an xsd:choice of which 1 option is shown. All options are {@code XsdTreeNode}'s themselves. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    XsdTreeNode choice;

    /** Option nodes. These can be directly applicable in the tree, or they can represent an xsd:sequence. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    List<XsdTreeNode> options;

    /** Currently selection option in the choice node. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    XsdTreeNode selected;

    // ====== Children ======

    /** Children nodes. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    List<XsdTreeNode> children;

    // ====== Attributes ======

    /** Attribute XSD nodes. */
    private List<Node> attributeNodes;

    /** Attribute values. */
    private List<String> attributeValues;

    // ====== Properties to expose to the GUI ======

    /** Whether the node is active. Inactive nodes show the user what type of node can be created in its place. */
    @SuppressWarnings("checkstyle:visibilitymodifier")
    boolean active;

    /**
     * When the node has been deactivated, activation should only set {@code active = true}. Other parts of the activation
     * should be ignored as the node was in an active state before, i.e. those parts are in tact. Deactivation does not affect
     * those parts.
     */
    private boolean deactivated;

    /** Whether this node is identifiable, i.e. has an Id attribute. */
    private Boolean isIdentifiable;

    /** Attribute index of Id. */
    private int idIndex;

    /** Whether this node is editable, i.e. has a simple value, e.g. &lt;Node&gt;Simple value&lt;/Node&gt;. */
    private Boolean isEditable;

    /** Stored simple value of the node. */
    private String value;

    /** Whether this node is loaded from an include file, and hence should not be editable at all. */
    private boolean isInclude;

    // ====== Interaction with visualization ======

    /** This function can be set externally and supplies an additional {@code String} to clarify this node in the tree. */
    private Function<XsdTreeNode, String> stringFunction;

    /** A consumer can be set externally and will receive this node when its menu item is selected. */
    private Map<String, Consumer<XsdTreeNode>> consumers = new LinkedHashMap<>();

    /** Specificity of the current description. This value is pointless once the most specific description was found. */
    private int descriptionSpecificity;

    /** The description, may be {@code null}. */
    private String description;

    /** Validators for the node itself, e.g. duplicate in parent check. */
    private Set<Function<XsdTreeNode, String>> nodeValidators = new LinkedHashSet<>();

    /** Validators for the value, sorted so CoupledValidators are first and couple to keys even for otherwise invalid nodes. */
    private SortedMap<ValueValidator, Object> valueValidators = new TreeMap<>();

    /**
     * Validators for each attribute, sorted so CoupledValidators are first and couple to keys even for otherwise invalid nodes.
     */
    private Map<String, SortedSet<ValueValidator>> attributeValidators = new LinkedHashMap<>();

    /** Field objects for each value validator and the attribute it validates. */
    private Map<String, Map<ValueValidator, Object>> attributeValidatorFields = new LinkedHashMap<>();

    /** Stored valid status, excluding children. {@code null} means unknown and that it needs to be derived. */
    private Boolean isSelfValid = null;

    /** Stored valid status, including children. {@code null} means unknown and that it needs to be derived. */
    private Boolean isValid = null;

    /** Stored value valid status. {@code null} means unknown and that it needs to be derived. */
    private Boolean valueValid = null;

    /** Value invalid message. */
    private String valueInvalidMessage = null;

    /** Stored node valid status. {@code null} means unknown and that it needs to be derived. */
    private Boolean nodeValid = null;

    /** Node invalid message (applies only to node itself, e.g. no duplicate nodes in parent). */
    private String nodeInvalidMessage = null;

    /** Stored attribute valid status. {@code null} means unknown and that it needs to be derived. */
    private List<Boolean> attributeValid;

    /** Attribute invalid message. */
    private List<String> attributeInvalidMessage;

    /**
     * Constructor for root node, based on an {@code XsdSchema}. Note: {@code XsdTreeNodeRoot} should be used for the root. The
     * {@code XsdSchema} that will be available to all nodes in the tree.
     * @param schema XSD schema.
     */
    protected XsdTreeNode(final Schema schema)
    {
        Throw.whenNull(schema, "XsdSchema may not be null.");
        this.parent = null;
        this.hiddenNodes = new ImmutableArrayList<>(Collections.emptyList());
        this.schema = schema;
        this.xsdNode = this.schema.getRoot();
        this.referringXsdNode = null;
        setOccurs();
        this.pathString = buildPathLocation();
        this.active = true;
        this.isInclude = false;
    }

    /**
     * Construct a node without referring node.
     * @param parent parent.
     * @param xsdNode XSD node that this tree node represents.
     * @param hiddenNodes nodes between the XSD node of the parent, and this tree node's XSD node.
     */
    XsdTreeNode(final XsdTreeNode parent, final Node xsdNode, final ImmutableList<Node> hiddenNodes)
    {
        this(parent, xsdNode, hiddenNodes, null);
    }

    /**
     * Constructor with referring node for extended types. If the node is xsd:choice or xsd:all, this node will represent the
     * choice. All options are then created as separate {@code XsdTreeNode}'s by this constructor. For each option that is an
     * xsd:sequence, this constructor will also create the nodes in that sequence, as those nodes function on the child-level of
     * this node. They are coupled to this choice by their {@code parentChoice}, allowing them to delete and add on this
     * level.<br>
     * <br>
     * The hidden nodes are all elements between the parent element and this element. For example {xsd:complexType, xsd:choice}
     * between the following child element and its containing object:
     * 
     * <pre>
     * &lt;xsd:element name="OBJECT"&gt;
     *   &lt;xsd:complexType&gt;
     *     &lt;xsd:choice&gt;
     *       &lt;xsd:element name="CHILD" /&gt;
     *     &lt;/xsd:choice&gt;
     *   &lt;/xsd:complexType&gt;
     * &lt;/xsd:element&gt;
     * </pre>
     * 
     * The hidden nodes will not include a referring node. For example the following "OBJECT" element will result in hidden
     * nodes {xsd:complexType, xsd:sequence} and the referring node is the {@code Node} with the ref="OBJECT" attribute. The
     * {@code XsdTreeNode} representing this element will itself wrap the node with name="OBJECT".
     * 
     * <pre>
     * &lt;xsd:element name="PARENT"&gt;
     *   &lt;xsd:complexType&gt;
     *     &lt;xsd:sequence&gt;
     *       &lt;xsd:element ref="OBJECT" /&gt;
     *     &lt;/xsd:sequence&gt;
     *   &lt;/xsd:complexType&gt;
     * &lt;/xsd:element&gt;
     * </pre>
     * 
     * @param parent parent.
     * @param xsdNode XSD node that this tree node represents.
     * @param hiddenNodes nodes between the XSD node of the parent, and this tree node's XSD node.
     * @param referringXsdNode original node that referred to {@code Node} through a ref={ref} or type={type} attribute, it is
     *            used for naming and occurrence, may be {@code null} if not applicable.
     */
    XsdTreeNode(final XsdTreeNode parent, final Node xsdNode, final ImmutableList<Node> hiddenNodes,
            final Node referringXsdNode)
    {
        Throw.whenNull(xsdNode, "Node may not be null.");
        this.parent = parent;
        this.xsdNode = xsdNode;
        this.hiddenNodes = hiddenNodes;
        this.referringXsdNode = referringXsdNode;
        this.schema = parent.schema;
        setOccurs();
        this.active = this.minOccurs > 0;
        this.pathString = buildPathLocation();
        this.isInclude = parent.isInclude;
        this.value = referringXsdNode == null ? (xsdNode == null ? null : DocumentReader.getAttribute(xsdNode, "default"))
                : DocumentReader.getAttribute(referringXsdNode, "default");
    }

    /**
     * Sets the minOccurs and maxOccurs values based on the relevant XSD node and hidden nodes. Note that this does not comply
     * to the full XSD logic. Here, the product of minOccurs and maxOccurs is derived. For OBJECT as below this results in
     * minOccurs = 1x4 and maxOccurs = 2x5. The complete logic is that any specific combination is allowed, i.e 1x4, 1x5, 2x4,
     * 2x5 and {1x4 + 1x5}, i.e. 4, 5, 8, 9 and 10. We ignore this, as things can become highly complex when multiple choices
     * and sequences are sequenced in a parent choice or sequence.
     * 
     * <pre>
     * &lt;xsd:sequence minOccurs="1" maxOccurs="2"&gt;
     *   &lt;xsd:sequence minOccurs="4" maxOccurs="5"&gt;
     *     &lt;xsd:element name="OBJECT"&gt;
     *   &lt;/xsd:sequence&gt;
     * &lt;/xsd:sequence&gt;
     * </pre>
     */
    private void setOccurs()
    {
        Node node = this.choice != null ? this.choice.xsdNode
                : (this.referringXsdNode == null ? this.xsdNode : this.referringXsdNode);
        this.minOccurs = XsdTreeNodeUtil.getOccurs(node, "minOccurs");
        this.maxOccurs = XsdTreeNodeUtil.getOccurs(node, "maxOccurs");
        if (getNodeName().equals("xsd:all"))
        {
            NodeList children = this.xsdNode.getChildNodes();
            int childCount = 0;
            for (int i = 0; i < children.getLength(); i++)
            {
                Node child = children.item(i);
                if (!child.getNodeName().equals("#text"))
                {
                    childCount++;
                }
            }
            this.maxOccurs *= childCount;
        }
    }

    /**
     * Builds the path location, e.g. "Ots.Definitions.RoadLayouts".
     * @return the path location.
     */
    private String buildPathLocation()
    {
        List<XsdTreeNode> path = getPath();
        StringBuilder pathStr = new StringBuilder(((XsdTreeNode) path.get(0)).getNodeName());
        for (int i = 1; i < path.size(); i++)
        {
            String nodeString = ((XsdTreeNode) path.get(i)).getNodeName();
            if ((!nodeString.equals("xsd:choice") && !nodeString.equals("xsd:all") && !nodeString.equals("xsd:sequence")
                    && !nodeString.equals("xi:include")) || i == path.size() - 1)
            {
                pathStr.append(".").append(nodeString);
            }
        }
        return pathStr.toString();
    }

    /**
     * Returns the path from root to this node.
     * @return list of node along the path.
     */
    public List<XsdTreeNode> getPath()
    {
        List<XsdTreeNode> path = this.parent != null ? this.parent.getPath() : new ArrayList<>();
        path.add(this);
        return path;
    }

    /**
     * Returns the root node.
     * @return root node.
     */
    public XsdTreeNodeRoot getRoot()
    {
        return this.parent.getRoot();
    }

    /**
     * Returns the name of this node, as appropriate in XML. Examples are Node, RoadLayout, and TacticalPlanner. Most typically
     * this is the "name" attribute of an xsd:element. In other cases it is the ref={ref} attribute of the referring
     * {@code Node}. In rare cases it is "xi:include".
     * @return name of this node, as appropriate in XML.
     */
    public String getNodeName()
    {
        Node node = this.referringXsdNode == null ? this.xsdNode : this.referringXsdNode;
        String ref = DocumentReader.getAttribute(node, "ref");
        if (ref != null)
        {
            return ref.replace("ots:", "");
        }
        String name = DocumentReader.getAttribute(node, "name");
        if (name != null)
        {
            return name.replace("ots:", "");
        }
        return node.getNodeName().replace("ots:", "");
    }

    // ====== Choice/Options ======

    /**
     * Returns whether this node is (part of) a choice, i.e. should show an option can be set here.
     * @return whether this node is (part of) a choice, i.e. should show an option can be set here.
     */
    public boolean isChoice()
    {
        return this.choice != null;
    }

    /**
     * Returns a list of options.
     * @return list of options.
     */
    public List<XsdOption> getOptions()
    {
        List<XsdOption> out = new ArrayList<>();
        boolean first = true;
        if (this.choice != null)
        {
            for (XsdTreeNode node : this.choice.options)
            {
                out.add(new XsdOption(node, this.choice, first, node.equals(this.choice.selected)));
                first = false;
            }
        }
        return out;
    }

    /**
     * Sets the node as newly selected option. All current nodes from the choice set are removed from the parent node.
     * @param node newly selected node. Must be part of the choice that this node represents.
     */
    public void setOption(final XsdTreeNode node)
    {
        Throw.when(!isChoice(), IllegalStateException.class, "Setting option on node that is not (part of) a choice.");
        Throw.when(!this.choice.options.contains(node) && !this.choice.equals(node), IllegalStateException.class,
                "Setting option on node that does not have this option.");
        XsdTreeNode previous = this.choice.selected == null ? this.choice : this.choice.selected;
        if (node.equals(previous))
        {
            return;
        }
        this.choice.selected = node;
        int index = removeAnyFromParent();
        this.parent.children.add(index, node);
        node.invalidate();
        this.choice.options.forEach((n) -> n.fireEvent(XsdTreeNodeRoot.OPTION_CHANGED, new Object[] {n, node, previous}));
    }

    /**
     * Returns the selected option.
     * @return selected option.
     */
    public XsdTreeNode getOption()
    {
        return this.choice.selected;
    }

    /**
     * Sets the given node as child of this node.
     * @param index index to insert the node.
     * @param child child node.
     */
    public void setChild(final int index, final XsdTreeNode child)
    {
        if (index >= this.children.size())
        {
            this.children.add(child);
        }
        else
        {
            this.children.add(index, child);
        }
        child.parent = this;
        child.invalidate();
    }

    /**
     * Remove all option values from parent, and return appropriate index to insert newly chosen option.
     * @return insertion index for new options.
     */
    private int removeAnyFromParent()
    {
        int insertIndex = -1;
        int removeIndex = this.parent.children.indexOf(this);
        if (removeIndex >= 0)
        {
            insertIndex = XsdTreeNodeUtil.resolveInsertion(insertIndex, removeIndex);
            this.parent.children.remove(removeIndex);
        }
        for (XsdTreeNode node : this.choice.options)
        {
            removeIndex = this.parent.children.indexOf(node);
            if (removeIndex >= 0)
            {
                insertIndex = XsdTreeNodeUtil.resolveInsertion(insertIndex, removeIndex);
                this.parent.children.remove(removeIndex);
            }
        }
        return insertIndex < 0 ? this.parent.children.size() : insertIndex;
    }

    /**
     * Creates the option nodes as part of an xsd:choice or xsd:all node.
     */
    void createOptions()
    {
        Throw.when(!this.xsdNode.getNodeName().equals("xsd:choice") && !this.xsdNode.getNodeName().equals("xsd:all"),
                IllegalStateException.class, "Can only add options for a node of type xsd:choice or xsd:all.");
        this.options = new ArrayList<>();
        XsdTreeNodeUtil.addChildren(this.xsdNode, this.parent, this.options, this.hiddenNodes, this.schema, false, -1);
        this.choice = this;
        for (XsdTreeNode option : this.options)
        {
            option.minOccurs = this.minOccurs;
            option.maxOccurs = this.maxOccurs;
            if (this.minOccurs > 0)
            {
                option.setActive();
            }
            option.choice = this;
        }
        if (this.choice.xsdNode.getNodeName().equals("xsd:all"))
        {
            XsdTreeNodeUtil.addXsdAllValidator(this.choice, this.choice);
            for (XsdTreeNode option : this.options)
            {
                XsdTreeNodeUtil.addXsdAllValidator(this.choice, option);
            }
        }
    }

    // ====== Children ======

    /**
     * Returns the number of children; directly for an {@code XsdTreeTableModel}.
     * @return number of children.
     */
    public int getChildCount()
    {
        if (!this.active)
        {
            return 0;
        }
        assureChildren();
        return this.children.size();
    }

    /**
     * Returns the child at given index; directly for an {@code XsdTreeTableModel}.
     * @param index child index.
     * @return child.
     */
    public XsdTreeNode getChild(final int index)
    {
        assureChildren();
        return this.children.get(index);
    }

    /**
     * Returns the first child with given name. The node may be within a series of xsd:sequence and xsd:choice/xsd:all
     * intermediate nodes.
     * @param name child name.
     * @return child.
     */
    public XsdTreeNode getFirstChild(final String name)
    {
        assureChildren();
        for (XsdTreeNode child : this.children)
        {
            if (child.getNodeName().equals("xsd:sequence") || child.getNodeName().equals("xsd:choice")
                    || child.getNodeName().equals("xsd:all"))
            {
                try
                {
                    return child.getFirstChild(name);
                }
                catch (NoSuchElementException ex)
                {
                    // continue search at other children
                }
            }
            if (child.getNodeName().equals(name))
            {
                return child;
            }
        }
        throw new NoSuchElementException("Node does not have a child named " + name);
    }

    /**
     * Returns a list of the child nodes.
     * @return list of the child nodes; safe copy.
     */
    public List<XsdTreeNode> getChildren()
    {
        assureChildren();
        return new ArrayList<>(this.children);
    }

    /**
     * Assures children are present. If a child has minOccurs &gt; 1, additional child nodes are added. Result is cached.
     */
    protected void assureChildren()
    {
        if (this.children != null)
        {
            return;
        }
        if (!this.active)
        {
            return;
        }
        this.children = new ArrayList<>();
        if (this.xsdNode.equals(XiIncludeNode.XI_INCLUDE))
        {
            if (this.attributeValues == null || (this.attributeValues.get(0) == null && this.attributeValues.get(1) == null))
            {
                return;
            }
            File file = new File(this.attributeValues.get(0));
            if (!file.isAbsolute())
            {
                file = new File(getRoot().getDirectory() + this.attributeValues.get(0));
            }
            if (!file.exists() && this.attributeValues.get(1) != null)
            {
                file = new File(this.attributeValues.get(1));
                if (!file.isAbsolute())
                {
                    file = new File(getRoot().getDirectory() + this.attributeValues.get(1));
                }
            }
            if (file.exists())
            {
                Document document;
                try
                {
                    document = DocumentReader.open(file.toURI());
                }
                catch (SAXException | IOException | ParserConfigurationException exception)
                {
                    return;
                }
                Node xsdIncludeNode = document.getFirstChild();
                String nameXml = xsdIncludeNode.getNodeName().replace("ots:", "");
                for (XsdTreeNode sibling : this.parent.children)
                {
                    if (sibling.isRelevantNode(nameXml))
                    {
                        XsdTreeNode child =
                                new XsdTreeNode(this, sibling.xsdNode, sibling.hiddenNodes, sibling.referringXsdNode);
                        child.isInclude = true;
                        this.children.add(child);
                        getRoot().fireEvent(XsdTreeNodeRoot.NODE_CREATED,
                                new Object[] {child, child.parent, child.parent.children.indexOf(child)});
                        child.loadXmlNodes(xsdIncludeNode);
                        return;
                    }
                }
            }
        }
        else if (!this.xsdNode.hasChildNodes())
        {
            return;
        }
        Map<Node, ImmutableList<Node>> relevantNodes = XsdTreeNodeUtil.getRelevantNodesWithChildren(this.xsdNode,
                new ImmutableArrayList<>(Collections.emptyList()), this.schema);
        for (Entry<Node, ImmutableList<Node>> entry : relevantNodes.entrySet())
        {
            XsdTreeNodeUtil.addChildren(entry.getKey(), this, this.children, entry.getValue(), this.schema, false, -1);
        }
        for (int index = 0; index < this.children.size(); index++)
        {
            XsdTreeNode child = this.children.get(index);
            for (int occurs = 1; occurs < child.minOccurs; occurs++)
            {
                child.add();
                index++;
            }
        }
    }

    /**
     * Returns the parent node.
     * @return parent node, is {@code null} for the root.
     */
    public XsdTreeNode getParent()
    {
        return this.parent;
    }

    // ====== Attributes ======

    /**
     * Finds all attributes that meet the following structure. If the attribute also specifies a {@code default}, it is also
     * stored. Finds the most specific description along the way.
     *
     * <pre>
     * &lt;xsd:element ...&gt;
     *   ...
     *   &lt;xsd:complexType&gt;
     *     ...
     *     &lt;xsd:attribute name="NAME" use="USE" ...&gt;
     *     ...
     *   &lt;/xsd:complexType&gt;
     *   ...
     * &lt;/xsd:element&gt;
     * </pre>
     */
    private synchronized void assureAttributesAndDescription()
    {
        if (this.attributeNodes != null)
        {
            return;
        }
        this.attributeNodes = new ArrayList<>();
        this.attributeValues = new ArrayList<>();
        this.attributeValid = new ArrayList<>();
        this.attributeInvalidMessage = new ArrayList<>();
        if (this.referringXsdNode != null)
        {
            this.description = DocumentReader.getAnnotation(this.referringXsdNode, "xsd:documentation", "description");
        }
        if (this.description == null)
        {
            this.description = DocumentReader.getAnnotation(this.xsdNode, "xsd:documentation", "description");
        }
        this.descriptionSpecificity = this.description != null ? 0 : Integer.MIN_VALUE;
        Node complexType =
                (this.xsdNode.getNodeName().equals("xsd:complexType") || this.xsdNode.equals(XiIncludeNode.XI_INCLUDE))
                        ? this.xsdNode : DocumentReader.getChild(this.xsdNode, "xsd:complexType");
        if (complexType != null && this.xsdNode.hasChildNodes())
        {
            findAttributes(complexType, -1);
        }
    }

    /**
     * Finds attributes in a nested way, looking up base types.
     * @param node node.
     * @param specificity specificity of type, reflects layers, used to find the most specific description.
     */
    private void findAttributes(final Node node, final int specificity)
    {
        String descript = DocumentReader.getAnnotation(node, "xsd:documentation", "description");
        if (descript != null && this.descriptionSpecificity < specificity)
        {
            this.descriptionSpecificity = specificity;
            this.description = descript;
        }
        for (int childIndex = 0; childIndex < node.getChildNodes().getLength(); childIndex++)
        {
            Node child = node.getChildNodes().item(childIndex);
            if (child.getNodeName().equals("xsd:attribute") && DocumentReader.getAttribute(child, "name") != null)
            {
                this.attributeNodes.add(child);
                this.attributeValues.add(null);
                this.attributeValid.add(null);
                this.attributeInvalidMessage.add(null);
            }
            if (child.getNodeName().equals("xsd:complexContent") || child.getNodeName().equals("xsd:simpleContent"))
            {
                Node extension = DocumentReader.getChild(child, "xsd:extension");
                if (extension != null)
                {
                    findAttributes(extension, specificity - 1);
                    String base = DocumentReader.getAttribute(extension, "base");
                    Node baseNode = this.schema.getType(base);
                    if (baseNode != null)
                    {
                        findAttributes(baseNode, specificity - 2);
                    }
                }
                Node restriction = DocumentReader.getChild(child, "xsd:restriction");
                if (restriction != null)
                {
                    String base = DocumentReader.getAttribute(restriction, "base");
                    Node baseNode = this.schema.getType(base);
                    if (baseNode != null)
                    {
                        findAttributes(baseNode, specificity - 2);
                    }
                }
            }
        }
    }

    /**
     * Returns the number of attributes; directly for an {@code XsdAttributesTableModel}.
     * @return number of attributes.
     */
    public int attributeCount()
    {
        if (!this.active)
        {
            return 0;
        }
        assureAttributesAndDescription();
        return this.attributeNodes.size();
    }

    /**
     * Returns the attributes at given index; directly for an {@code XsdAttributesTableModel}.
     * @param index attribute index.
     * @return attribute node.
     */
    public Node getAttributeNode(final int index)
    {
        assureAttributesAndDescription();
        return this.attributeNodes.get(index);
    }

    /**
     * Sets an attribute value; directly for an {@code XsdAttributesTableModel}.
     * @param index index of the attribute.
     * @param value value of the attribute.
     */
    @SuppressWarnings("checkstyle:hiddenfield")
    public void setAttributeValue(final int index, final String value)
    {
        setAttributeValue(getAttributeNameByIndex(index), value);
    }

    /**
     * Sets an attribute value; directly for an {@code XsdAttributesTableModel}.
     * @param name name of the attribute.
     * @param value value of the attribute.
     */
    @SuppressWarnings("checkstyle:hiddenfield")
    public void setAttributeValue(final String name, final String value)
    {
        assureAttributesAndDescription();
        int index = getAttributeIndexByName(name);
        String previous = this.attributeValues.get(index);
        if (!XsdTreeNodeUtil.valuesAreEqual(previous, value))
        {
            boolean isDefaultBoolean = false;
            if ("xsd:boolean".equals(DocumentReader.getAttribute(this.attributeNodes.get(index), "type")))
            {
                isDefaultBoolean = getDefaultAttributeValue(index).equals(value);
            }
            this.attributeValues.set(index, (value == null || value.isEmpty() || isDefaultBoolean) ? null : value);
            if (this.xsdNode.equals(XiIncludeNode.XI_INCLUDE))
            {
                removeChildren();
                this.children = null;
                assureChildren();
            }
            invalidate();
            fireEvent(ATTRIBUTE_CHANGED, new Object[] {this, name, previous});
        }
    }

    /**
     * Returns an attribute value; directly for an {@code XsdAttributesTableModel}.
     * @param index index of the attribute.
     * @return value of the attribute.
     */
    public String getAttributeValue(final int index)
    {
        assureAttributesAndDescription();
        Throw.when(index < 0 || index >= this.attributeCount(), IndexOutOfBoundsException.class, "Index out of bounds.");
        return this.attributeValues.get(index);
    }

    /**
     * Returns the default value for the attribute.
     * @param index attribute index.
     * @return default value for the attribute, or {@code null} if there is no default value.
     */
    public String getDefaultAttributeValue(final int index)
    {
        assureAttributesAndDescription();
        Throw.when(index < 0 || index >= this.attributeCount(), IndexOutOfBoundsException.class, "Index out of bounds.");
        return DocumentReader.getAttribute(this.attributeNodes.get(index), "default");
    }

    /**
     * Returns the value of an attribute.
     * @param attribute name of the attribute.
     * @return value of the attribute.
     * @throws IllegalStateException when the node does not have the attribute.
     */
    public String getAttributeValue(final String attribute)
    {
        assureAttributesAndDescription();
        return this.attributeValues.get(getAttributeIndexByName(attribute));
    }

    /**
     * Returns the index of the named attribute.
     * @param attribute attribute name.
     * @return index of the named attribute.
     * @throws NoSuchElementException when the attribute is not in this node.
     */
    public int getAttributeIndexByName(final String attribute)
    {
        assureAttributesAndDescription();
        if (this.xsdNode.equals(XiIncludeNode.XI_INCLUDE))
        {
            // this is a bit dirty, at loading attribute 'href' and later attribute 'File' are mapped to 0
            // attribute 'Fallback' is explicitly set from child nodes (that are otherwise skipped) during loading
            return "Fallback".equals(attribute) ? 1 : 0;
        }
        for (int index = 0; index < this.attributeNodes.size(); index++)
        {
            Node attr = this.attributeNodes.get(index);
            if (DocumentReader.getAttribute(attr, "name").equals(attribute))
            {
                return index;
            }
        }
        throw new NoSuchElementException("Attribute " + attribute + " is not in node " + getNodeName() + ".");
    }

    /**
     * Returns the name of the indexed attribute.
     * @param index attribute index.
     * @return name of the indexed attribute.
     */
    public String getAttributeNameByIndex(final int index)
    {
        String name = DocumentReader.getAttribute(this.attributeNodes.get(index), "name");
        return name;
    }

    /**
     * Returns whether this node has an attribute with given name.
     * @param attribute attribute name.
     * @return whether this node has an attribute with given name.
     */
    public boolean hasAttribute(final String attribute)
    {
        assureAttributesAndDescription();
        for (int index = 0; index < this.attributeNodes.size(); index++)
        {
            Node attr = this.attributeNodes.get(index);
            if (DocumentReader.getAttribute(attr, "name").equals(attribute))
            {
                return true;
            }
        }
        return false;
    }

    // ====== Methods to expose to the GUI ======

    /**
     * Returns whether the node is active. If not, it only exists to show the user what type of node may be created here, or as
     * a choice option currently not chosen.
     * @return whether the node is active.
     */
    public boolean isActive()
    {
        if (this.choice != null && !this.choice.selected.equals(this))
        {
            return false;
        }
        return this.active;
    }

    /**
     * Sets this node to be active if it is not already. If this node is the selected node within a choice, it also activates
     * all other options of the choice.
     */
    public void setActive()
    {
        if (!this.active)
        {
            this.active = true;
            if (this.deactivated)
            {
                if (this.xsdNode.equals(XiIncludeNode.XI_INCLUDE) || this.isInclude)
                {
                    // included children
                    for (XsdTreeNode child : this.children)
                    {
                        child.setActive();
                    }
                }
                invalidate();
                fireEvent(new Event(XsdTreeNodeRoot.ACTIVATION_CHANGED, new Object[] {this, true}));
                return; // deactivated from an active state in the past; all parts below are already in place
            }
            this.children = null;
            this.attributeNodes = null;
            this.isIdentifiable = null;
            this.isEditable = null;
            assureAttributesAndDescription();
            assureChildren();
            if (this.choice != null && this.choice.selected.equals(this))
            {
                this.choice.active = true;
                for (XsdTreeNode option : this.choice.options)
                {
                    if (!option.equals(this))
                    {
                        option.setActive();
                    }
                }
            }
            invalidate();
            fireEvent(new Event(XsdTreeNodeRoot.ACTIVATION_CHANGED, new Object[] {this, true}));
        }
    }

    /**
     * Deactivates this node.
     */
    public void setInactive()
    {
        if (this.active)
        {
            this.deactivated = true;
            this.active = false;
            invalidate();
            this.parent.invalidate();
            fireEvent(new Event(XsdTreeNode.ACTIVATION_CHANGED, new Object[] {this, false}));
            // included children
            if (this.xsdNode.equals(XiIncludeNode.XI_INCLUDE) || this.isInclude)
            {
                for (XsdTreeNode child : this.children)
                {
                    child.setInactive();
                }
            }
        }
    }

    /**
     * Returns whether this node has an attribute named "Id".
     * @return whether this node has an attribute named "Id".
     */
    public boolean isIdentifiable()
    {
        if (!this.active)
        {
            return false;
        }
        if (this.isIdentifiable == null)
        {
            assureAttributesAndDescription();
            for (int index = 0; index < this.attributeCount(); index++)
            {
                Node node = this.attributeNodes.get(index);
                if (DocumentReader.getAttribute(node, "name").equals("Id"))
                {
                    this.isIdentifiable = true;
                    this.idIndex = index;
                    return true;
                }
            }
            this.isIdentifiable = false;
        }
        return this.isIdentifiable;
    }

    /**
     * Sets the value for an attribute with name "Id".
     * @param id value to set.
     */
    public void setId(final String id)
    {
        Throw.when(!isIdentifiable(), IllegalStateException.class, "Setting id on non-identifiable node.");
        setAttributeValue(this.idIndex, id);
    }

    /**
     * Returns the value of an attribute with name "Id".
     * @return value of an attribute with name "Id".
     */
    public String getId()
    {
        Throw.when(!isIdentifiable(), IllegalStateException.class, "Getting id from non-identifiable node.");
        return this.attributeValues.get(this.idIndex);
    }

    /**
     * Returns whether this node is editable; i.e. whether a value can be set on the node, i.e. has a simple value, e.g.
     * &lt;Node&gt;Simple value&lt;/Node&gt;.
     * @return whether this node is editable, i.e. whether a value can be set on the node.
     */
    public boolean isEditable()
    {
        if (!this.active)
        {
            return false;
        }
        if (this.isEditable == null)
        {
            if (this.xsdNode.equals(XiIncludeNode.XI_INCLUDE))
            {
                this.isEditable = false;
                return false;
            }
            if (this.xsdNode.getChildNodes().getLength() == DocumentReader.getChildren(this.xsdNode, "#text").size()
                    && this.xsdNode.getChildNodes().getLength() > 0)
            {
                // #text children only means a simple type
                this.isEditable = true;
                return true;
            }
            Node simpleType = this.xsdNode.getNodeName().equals("xsd:simpleType") ? this.xsdNode
                    : DocumentReader.getChild(this.xsdNode, "xsd:simpleType");
            if (simpleType != null)
            {
                this.isEditable = true;
                return true;
            }
            Node complexType = this.xsdNode.getNodeName().equals("xsd:complexType") ? this.xsdNode
                    : DocumentReader.getChild(this.xsdNode, "xsd:complexType");
            boolean isComplex = complexType != null;
            while (complexType != null)
            {
                Node simpleContent = DocumentReader.getChild(complexType, "xsd:simpleContent");
                if (simpleContent != null)
                {
                    this.isEditable = true;
                    return this.isEditable;
                }
                Node complexContent = DocumentReader.getChild(complexType, "xsd:complexContent");
                complexType = null;
                if (complexContent != null)
                {
                    Node extension = DocumentReader.getChild(complexContent, "xsd:extension");
                    if (extension != null)
                    {
                        String base = DocumentReader.getAttribute(extension, "base");
                        complexType = this.schema.getType(base);
                    }
                }
            }
            if (isComplex)
            {
                // complex and never found simpleContent through extension
                this.isEditable = false;
                return false;
            }
            String type = DocumentReader.getAttribute(this.xsdNode, "type");
            if (this.xsdNode.getNodeName().equals("xsd:element") && (type == null || type.startsWith("xsd:")))
            {
                this.isEditable = true;
                return true;
            }
            this.isEditable = false;
        }
        return this.isEditable;
    }

    /**
     * Returns whether this node exists as its loaded from an include.
     * @return whether this node exists as its loaded from an include.
     */
    public boolean isInclude()
    {
        return this.isInclude;
    }

    /**
     * Sets the value on this node.
     * @param value value to set.
     */
    public void setValue(final String value)
    {
        Throw.when(!isEditable(), IllegalStateException.class,
                "Node is not an xsd:simpleType or xsd:complexType with xsd:simpleContent, hence no value is allowed.");
        String previous = this.value;
        if (!XsdTreeNodeUtil.valuesAreEqual(previous, value))
        {
            this.value = (value == null || value.isEmpty()) ? null : value;
            invalidate();
            fireEvent(new Event(VALUE_CHANGED, new Object[] {this, previous}));
        }
    }

    /**
     * Returns the value of this node.
     * @return value of this node.
     */
    public String getValue()
    {
        return this.value;
    }

    /**
     * Returns whether a node of the same type may be added next to this node under the parent. This is based on maxOccurs.
     * @return whether a node of the same type may be added next to this node under the parent.
     */
    public boolean isAddable()
    {
        return this.maxOccurs == -1 || (this.parent != null && siblingPositions().size() < this.maxOccurs);
    }

    /**
     * Adds a node of similar type next to this node under the parent. If the node is a choice, the same selected option will be
     * set in the added node. In this way the user sees that node option copied.
     * @return added node.
     */
    public XsdTreeNode add()
    {
        if (this.choice != null)
        {
            int index = this.parent.children.indexOf(this) + 1;
            XsdTreeNode node = new XsdTreeNode(this.choice.parent, this.choice.xsdNode, this.choice.hiddenNodes,
                    this.choice.referringXsdNode);
            getRoot().fireEvent(XsdTreeNodeRoot.NODE_CREATED,
                    new Object[] {node, node.parent, node.parent.children.indexOf(this) + 1});
            node.createOptions();
            int indexSelected = this.choice.options.indexOf(this.choice.selected);
            XsdTreeNode selectedOption = node.options.get(indexSelected);
            node.choice.setOption(selectedOption);
            this.parent.children.remove(selectedOption); // needs to be at the right index
            this.parent.children.add(index, selectedOption);
            node.options.get(indexSelected).setActive();
            return selectedOption;
        }
        else
        {
            int index = this.parent.children.indexOf(this) + 1;
            XsdTreeNode node = new XsdTreeNode(this.parent, this.xsdNode, this.hiddenNodes, this.referringXsdNode);
            this.parent.children.add(index, node);
            node.active = true;
            getRoot().fireEvent(XsdTreeNodeRoot.NODE_CREATED,
                    new Object[] {node, node.parent, node.parent.children.indexOf(node)});
            return node;
        }
    }

    /**
     * Creates a full copy of this node, next to this node under the same parent.
     * @return newly created node.
     */
    public XsdTreeNode duplicate()
    {
        return duplicate(this.parent);
    }

    /**
     * Duplicates this node, but under the given parent node.
     * @param newParent parent node.
     * @return newly created node.
     */
    public XsdTreeNode duplicate(final XsdTreeNode newParent)
    {
        // empty copy
        XsdTreeNode copyNode = emptyCopy(newParent);
        copyNode.active = this.active;
        copyInto(copyNode);
        copyNode.invalidate();
        getRoot().fireEvent(XsdTreeNodeRoot.NODE_CREATED,
                new Object[] {copyNode, newParent, newParent.children.indexOf(copyNode)});
        invalidate(); // due to e.g. duplicate ID, this node may also become invalid
        return copyNode;
    }

    /**
     * Creates an empty copy of this node, i.e. without children, options, attributes.
     * @return empty copy.
     */
    public XsdTreeNode emptyCopy()
    {
        return emptyCopy(this.parent);
    }

    /**
     * Returns an empty copy of this node under the given parent.
     * @param newParent new parent.
     * @return empty copy.
     */
    private XsdTreeNode emptyCopy(final XsdTreeNode newParent)
    {
        int indexOfNode = this.parent.children.indexOf(this);
        if (newParent.equals(this.parent))
        {
            indexOfNode++; // its a copy so does not matter, but in case of exceptions it is clearer the copy is below 'this'
        }
        XsdTreeNode copyNode = new XsdTreeNode(newParent, this.xsdNode, this.hiddenNodes, this.referringXsdNode);
        if (newParent.children == null)
        {
            newParent.children = new ArrayList<>();
        }
        newParent.children.add(indexOfNode, copyNode);
        copyNode.parent = newParent;
        return copyNode;
    }

    /**
     * Copies the active status, value, choice, attributes and children of this node in to the given node.
     * @param copyNode node to copy data in to.
     */
    public void copyInto(final XsdTreeNode copyNode)
    {
        if (this.equals(copyNode))
        {
            return;
        }
        copyNode.value = this.value;
        // copy choice
        if (this.choice != null)
        {
            XsdTreeNode choiceNode = new XsdTreeNode(copyNode.parent, this.choice.xsdNode, this.choice.hiddenNodes,
                    this.choice.referringXsdNode);
            choiceNode.choice = choiceNode;
            // populate options, but skip the copyNode option that was created above, insert it afterwards
            int selectedIndex = this.choice.options.indexOf(this);
            choiceNode.options = new ArrayList<>();
            XsdTreeNodeUtil.addChildren(this.choice.xsdNode, copyNode.parent, choiceNode.options, this.choice.hiddenNodes,
                    this.schema, false, selectedIndex);
            choiceNode.options.add(selectedIndex, copyNode);
            choiceNode.selected = choiceNode.options.get(selectedIndex);
            if (this.choice.getNodeName().equals("xsd:all"))
            {
                XsdTreeNodeUtil.addXsdAllValidator(this.choice, this);
            }
            for (int index = 0; index < choiceNode.options.size(); index++)
            {
                XsdTreeNode option = choiceNode.options.get(index);
                if (this.choice.getNodeName().equals("xsd:all"))
                {
                    XsdTreeNodeUtil.addXsdAllValidator(this.choice, option);
                }
                option.minOccurs = choiceNode.minOccurs;
                option.maxOccurs = choiceNode.maxOccurs;
                if (choiceNode.minOccurs > 0)
                {
                    option.setActive();
                }
                option.active = this.choice.options.get(index).active;
                option.choice = choiceNode;
            }
        }
        // copy attributes
        copyNode.assureAttributesAndDescription();
        for (int index = 0; index < attributeCount(); index++)
        {
            copyNode.attributeValues.set(index, this.attributeValues.get(index));
        }
        // copy children, recursive
        if (copyNode.children != null)
        {
            for (XsdTreeNode child : copyNode.getChildren())
            {
                int index = copyNode.children.indexOf(child);
                copyNode.children.remove(index);
                child.parent = null;
                getRoot().fireEvent(XsdTreeNodeRoot.NODE_REMOVED, new Object[] {child, copyNode, index});
            }
        }
        if (this.children != null)
        {
            for (int index = 0; index < this.children.size(); index++)
            {
                this.children.get(index).duplicate(copyNode);
            }
        }
    }

    /**
     * Returns whether this node may be removed without violating "minOccurs" constraints.
     * @return whether this node may be removed without violating "minOccurs" constraints.
     */
    public boolean isRemovable()
    {
        return isActive() && siblingPositions().size() > this.minOccurs;
    }

    /**
     * Removes this node from the tree structure. For nodes with minOccurs = 0 that are the last of their type in the context of
     * their parent, the node is deactivated rather than removed. This method also explicitly removes all children nodes
     * recursively. If the node is part of a sequence that is an option in a {@code parentChoice}, the node is removed from
     * there.
     */
    public final void remove()
    {
        int numberOfTypeOrChoiceInParent;
        if (this.choice != null && this.choice.selected.equals(this))
        {
            numberOfTypeOrChoiceInParent = 0;
            for (XsdTreeNode sibling : this.parent.children)
            {
                for (XsdTreeNode option : this.choice.options)
                {
                    if (XsdTreeNodeUtil.haveSameType(option, sibling))
                    {
                        numberOfTypeOrChoiceInParent++;
                    }
                }
            }
        }
        else
        {
            numberOfTypeOrChoiceInParent = siblingPositions().size();
        }
        if (this.minOccurs == 0 && numberOfTypeOrChoiceInParent == 1 && !this.isInclude)
        {
            if (this.active)
            {
                setInactive();
            }
            return;
        }
        if (this.choice != null && this.choice.selected.equals(this))
        {
            for (XsdTreeNode option : this.choice.options)
            {
                if (!this.choice.selected.equals(this))
                {
                    option.remove();
                }
            }
        }
        removeChildren();
        XsdTreeNode parent = this.parent;
        int index = this.parent.children.indexOf(this);
        this.parent.children.remove(this);
        XsdTreeNodeRoot root = getRoot(); // can't get it later as we set parent to null
        this.parent = null;
        parent.children.forEach((c) -> c.invalidate());
        parent.invalidate();
        root.fireEvent(XsdTreeNodeRoot.NODE_REMOVED, new Object[] {this, parent, index});
    }

    /**
     * Removes all children.
     */
    private void removeChildren()
    {
        if (this.children != null)
        {
            // copy to prevent ConcurrentModificationException as child removes itself from this node
            List<XsdTreeNode> childs = new ArrayList<>(this.children);
            for (XsdTreeNode child : childs)
            {
                child.remove();
            }
        }
    }

    /**
     * Returns whether the node can be moved up in the parent.
     * @return whether the node can be moved up in the parent.
     */
    public boolean canMoveUp()
    {
        List<Integer> positions = siblingPositions();
        return !positions.isEmpty() && this.parent.children.indexOf(this) > positions.get(0);
    }

    /**
     * Returns whether the node can be moved down in the parent.
     * @return whether the node can be moved down in the parent.
     */
    public boolean canMoveDown()
    {
        List<Integer> positions = siblingPositions();
        return !positions.isEmpty() && this.parent.children.indexOf(this) < positions.get(positions.size() - 1);
    }

    /**
     * Returns an ordered list of indices within the parents child list, regarding sibling nodes of the same type. What is
     * considered the same type differs between a choice node, and a regular node. In case of a choice, all siblings that have a
     * type equal to <i>any</i> of the choice options, are considered siblings. They are all instances of the same choice,
     * although they are different options.
     * @return list of indices within the parents child list, regarding sibling nodes of the same type.
     */
    private List<Integer> siblingPositions()
    {
        List<Integer> siblingPositions = new ArrayList<>();
        if (this.parent == null)
        {
            return siblingPositions;
        }
        if (this.choice != null)
        {
            for (int index = 0; index < this.parent.children.size(); index++)
            {
                for (XsdTreeNode option : this.choice.options)
                {
                    if (XsdTreeNodeUtil.haveSameType(option, this.parent.children.get(index)))
                    {
                        siblingPositions.add(index);
                        break;
                    }
                }
            }
        }
        else
        {
            for (int index = 0; index < this.parent.children.size(); index++)
            {
                if (XsdTreeNodeUtil.haveSameType(this, this.parent.children.get(index)))
                {
                    siblingPositions.add(index);
                }
            }
        }
        return siblingPositions;
    }

    /**
     * Move the node to a different position in the parent, relative to the current position.
     * @param down number of moves down. May be negative for up.
     */
    public void move(final int down)
    {
        int oldIndex = this.parent.children.indexOf(this);
        this.parent.children.remove(this);
        int newIndex = oldIndex + down;
        this.parent.children.add(newIndex, this);
        fireEvent(MOVED, new Object[] {this, oldIndex, newIndex});
    }

    /**
     * Returns the minimum number of this element under the parent node, as defined in minOccurs in XSD.
     * @return minimum number of this element under the parent node, as defined in minOccurs in XSD.
     */
    public int minOccurs()
    {
        return this.minOccurs;
    }

    /**
     * Returns the maximum number of this element under the parent node, as defined in maxOccurs in XSD. The XSD value
     * "unbounded" results in a value of -1.
     * @return maximum number of this element under the parent node, as defined in maxOccurs in XSD.
     */
    public int maxOccurs()
    {
        return this.maxOccurs;
    }

    /**
     * Returns the path string of this element, e.g. "Ots.Definitions.RoadLayouts". This is used to identify each unique type of
     * element.
     * @return path string of this element, e.g. "Ots.Definitions.RoadLayouts".
     */
    public String getPathString()
    {
        return this.pathString;
    }

    /**
     * Returns whether the contents of the attributes, value and other aspects of the node itself are valid. This excludes child
     * nodes.
     * @return whether the contents of the attributes, value and other aspects of the node itself are valid.
     */
    public boolean isSelfValid()
    {
        if (this.isSelfValid == null)
        {
            if (!this.active)
            {
                this.isSelfValid = true;
            }
            else if (reportInvalidNode() != null)
            {
                this.isSelfValid = false;
            }
            else if (reportInvalidValue() != null)
            {
                this.isSelfValid = false;
            }
            else
            {
                boolean attributesValid = true;
                for (int index = 0; index < attributeCount(); index++)
                {
                    if (reportInvalidAttributeValue(index) != null)
                    {
                        attributesValid = false;
                    }
                }
                this.isSelfValid = attributesValid;
            }
        }
        return this.isSelfValid;
    }

    /**
     * Returns whether the node, and all its children recursively, is valid. This means all required values are supplied, and
     * all supplied values comply to their respective types and constraints.
     * @return whether the node is valid.
     */
    public boolean isValid()
    {
        // TODO: check whether node should have children if there are none; can we do this without already exploding the tree?
        if (this.isValid == null)
        {
            if (!isActive())
            {
                this.isValid = true;
            }
            else if (!isSelfValid())
            {
                this.isValid = false;
            }
            else
            {
                boolean childrenValid = true;
                if (this.children != null)
                {
                    for (XsdTreeNode child : this.children)
                    {
                        if (!child.isValid())
                        {
                            childrenValid = false;
                        }
                    }
                }
                this.isValid = childrenValid;
            }
        }
        return this.isValid;
    }

    /**
     * Sets the valid status of this node and all parent nodes to unknown.
     */
    public void invalidate()
    {
        this.isSelfValid = null;
        this.isValid = null;
        if (this.parent != null)
        {
            this.parent.invalidate();
        }
        this.valueValid = null;
        this.valueInvalidMessage = null;
        this.nodeValid = null;
        this.nodeInvalidMessage = null;
        assureAttributesAndDescription();
        Collections.fill(this.attributeValid, null);
        Collections.fill(this.attributeInvalidMessage, null);
    }

    /**
     * Invalidates entire tree in a nested manner. Triggered after the path of the current file changes in the root node.
     * @param node node to nest through.
     */
    void invalidateAll(final XsdTreeNode node)
    {
        if (node.xsdNode.equals(XiIncludeNode.XI_INCLUDE))
        {
            node.removeChildren();
            node.children = null;
            node.assureChildren();
        }
        else if (node.children != null)
        {
            for (XsdTreeNode child : node.children)
            {
                invalidateAll(child);
            }
        }
        node.invalidate();
    }

    /**
     * Returns whether the value, any of the attributes, or any of the sub-elements, has an expression.
     * @return whether the node has an expression.
     */
    public boolean hasExpression()
    {
        if (!this.active)
        {
            return false;
        }
        if (valueIsExpression())
        {
            return true;
        }
        for (int index = 0; index < attributeCount(); index++)
        {
            if (attributeIsExpression(index))
            {
                return true;
            }
        }
        if (this.children != null)
        {
            for (XsdTreeNode child : this.children)
            {
                if (child.hasExpression())
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Returns whether the value is an expression.
     * @return whether the value is an expression.
     */
    public boolean valueIsExpression()
    {
        return this.value != null && this.value.startsWith("{") && this.value.endsWith("}");
    }

    /**
     * Returns whether the attribute is an expression.
     * @param index attribute index.
     * @return whether the attribute is an expression.
     */
    public boolean attributeIsExpression(final int index)
    {
        String attributeValue = this.attributeValues.get(index);
        return attributeValue != null && attributeValue.startsWith("{") && attributeValue.endsWith("}");
    }

    /**
     * Returns whether the Id is an expression.
     * @return whether the Id is an expression.
     */
    public boolean idIsExpression()
    {
        return attributeIsExpression(getAttributeIndexByName("Id"));
    }

    /**
     * Adds a validator for the node.
     * @param validator validator.
     */
    public void addNodeValidator(final Function<XsdTreeNode, String> validator)
    {
        this.nodeValidators.add(validator);
    }

    /**
     * Adds a validator for the value.
     * @param validator validator.
     * @param field field.
     */
    public void addValueValidator(final ValueValidator validator, final Object field)
    {
        this.valueValidators.put(validator, field);
    }

    /**
     * Adds a validator for the value of an attribute.
     * @param attribute attribute name.
     * @param validator validator.
     */
    public void addAttributeValidator(final String attribute, final ValueValidator validator)
    {
        addAttributeValidator(attribute, validator, null);
    }

    /**
     * Adds a validator for the value of an attribute. The field object is any object that is returned to the validator in its
     * {@code getOptions()} method, such that it can know for which field option values should be given.
     * @param attribute attribute name.
     * @param validator validator.
     * @param field field.
     */
    public void addAttributeValidator(final String attribute, final ValueValidator validator, final Object field)
    {
        this.attributeValidators.computeIfAbsent(attribute, (key) -> new TreeSet<>()).add(validator);
        this.attributeValidatorFields.computeIfAbsent(attribute, (key) -> new LinkedHashMap<>()).put(validator, field);
    }

    /**
     * Returns a message why the id is invalid, or {@code null} if it is valid. This should only be used to determine a GUI
     * indication on an invalid Id. For other cases processing the attributes includes the Id.
     * @return message why the id is invalid, or {@code null} if it is valid.
     */
    public String reportInvalidId()
    {
        if (!isActive())
        {
            return null;
        }
        return isIdentifiable() ? reportInvalidAttributeValue(getAttributeIndexByName("Id")) : null;
    }

    /**
     * Returns a message why the node is invalid, or {@code null} if it is valid. This only concerns validators on node level,
     * i.e. not on attribute or value level. E.g. because the node is duplicate in its parent.
     * @return message why the id is invalid, or {@code null} if it is valid.
     */
    public String reportInvalidNode()
    {
        if (this.nodeValid == null)
        {
            for (Function<XsdTreeNode, String> validator : this.nodeValidators)
            {
                String message = validator.apply(this);
                if (message != null)
                {
                    this.nodeInvalidMessage = message;
                    this.nodeValid = false;
                    return message;
                }
            }
            this.nodeValid = true;
        }
        return this.nodeInvalidMessage;
    }

    /**
     * Returns a message why the value is invalid, or {@code null} if it is valid.
     * @return message why the value is invalid, or {@code null} if it is valid.
     */
    public String reportInvalidValue()
    {
        if (this.valueValid == null)
        {
            if (!isEditable() || !isActive())
            {
                this.valueInvalidMessage = null;
                this.valueValid = true;
                return null;
            }
            if (this.value != null && !this.value.isEmpty())
            {
                for (ValueValidator validator : this.valueValidators.keySet())
                {
                    String message = validator.validate(this);
                    if (message != null)
                    {
                        this.valueInvalidMessage = message;
                        this.valueValid = false;
                        return message;
                    }
                }
            }
            this.valueInvalidMessage = ValueValidator.reportInvalidValue(this.xsdNode, this.value, this.schema);
            this.valueValid = this.valueInvalidMessage == null;
        }
        return this.valueInvalidMessage;
    }

    /**
     * Returns a message why the attribute value is invalid, or {@code null} if it is valid.
     * @param index index of the attribute.
     * @return message why the attribute value is invalid, or {@code null} if it is valid.
     */
    public String reportInvalidAttributeValue(final int index)
    {
        if (this.attributeValid.get(index) == null)
        {
            if (!isActive())
            {
                this.attributeInvalidMessage.set(index, null);
                this.attributeValid.set(index, true);
                return null;
            }
            if (this.xsdNode.equals(XiIncludeNode.XI_INCLUDE))
            {
                XsdTreeNode root = getPath().get(0);
                if (root instanceof XsdTreeNodeRoot)
                {
                    String message = ValueValidator.reportInvalidInclude(this.attributeValues.get(0),
                            this.attributeValues.get(1), ((XsdTreeNodeRoot) root).getDirectory());
                    this.attributeInvalidMessage.set(index, message);
                    this.attributeValid.set(index, message == null);
                    return message;
                }
                else
                {
                    // node is being deleted and has no parent anymore
                    this.attributeInvalidMessage.set(index, null);
                    this.attributeValid.set(index, true);
                    return null;
                }
            }
            String attribute = DocumentReader.getAttribute(getAttributeNode(index), "name");
            for (ValueValidator validator : this.attributeValidators.computeIfAbsent(attribute, (key) -> new TreeSet<>()))
            {
                String message = validator.validate(this);
                if (message != null)
                {
                    this.attributeInvalidMessage.set(index, message);
                    this.attributeValid.set(index, false);
                    return message;
                }
            }
            String message =
                    ValueValidator.reportInvalidAttributeValue(getAttributeNode(index), getAttributeValue(index), this.schema);
            this.attributeInvalidMessage.set(index, message);
            this.attributeValid.set(index, message == null);
            return message;
        }
        return this.attributeInvalidMessage.get(index);
    }

    /**
     * Returns all restrictions for the value. These are not sorted and may contain duplicates.
     * @return list of restrictions for the value.
     */
    public List<String> getValueRestrictions()
    {
        Node relevantNode = this.referringXsdNode == null ? this.xsdNode : this.referringXsdNode;
        if ("ots:boolean".equals(DocumentReader.getAttribute(relevantNode, "type"))
                || "xsd:boolean".equals(DocumentReader.getAttribute(relevantNode, "type")))
        {
            return List.of("true", "false");
        }
        List<String> valueOptions = getOptionsFromValidators(this.valueValidators, getNodeName());
        if (!valueOptions.isEmpty())
        {
            return valueOptions;
        }
        return XsdTreeNodeUtil.getOptionsFromRestrictions(ValueValidator.getRestrictions(this.xsdNode, this.schema));
    }

    /**
     * Returns all restrictions for the given attribute. These are not sorted and may contain duplicates.
     * @param index attribute number.
     * @return list of restrictions for the attribute.
     */
    public List<String> getAttributeRestrictions(final int index)
    {
        if ("ots:boolean".equals(DocumentReader.getAttribute(this.attributeNodes.get(index), "type")))
        {
            return List.of("true", "false");
        }
        String field = getAttributeNameByIndex(index);
        Map<ValueValidator, Object> map = new LinkedHashMap<>();
        this.attributeValidators.computeIfAbsent(field, (f) -> new TreeSet<>())
                .forEach((v) -> map.put(v, this.attributeValidatorFields.get(field).get(v)));
        List<String> valueOptions = getOptionsFromValidators(map, field);
        if (!valueOptions.isEmpty() || this.xsdNode.equals(XiIncludeNode.XI_INCLUDE))
        {
            return valueOptions;
        }
        return XsdTreeNodeUtil
                .getOptionsFromRestrictions(ValueValidator.getRestrictions(this.attributeNodes.get(index), this.schema));
    }

    /**
     * Returns the node to which an attribute refers via a KeyValidator.
     * @param index index of the attribute.
     * @return node to which an attribute refers via a KeyValidator, or {@code null} if no such node.
     */
    public XsdTreeNode getCoupledKeyrefNodeAttribute(final int index)
    {
        return getCoupledKeyrefNodeAttribute(getAttributeNameByIndex(index));
    }

    /**
     * Returns the node to which an attribute refers via a KeyValidator.
     * @param attribute attribute name.
     * @return node to which an attribute refers via a KeyValidator, or {@code null} if no such node.
     */
    public XsdTreeNode getCoupledKeyrefNodeAttribute(final String attribute)
    {
        if (this.attributeValidators.containsKey(attribute))
        {
            return getCoupledKeyrefNode(this.attributeValidators.get(attribute));
        }
        return null;
    }

    /**
     * Returns the node to which the value refers via a KeyValidator.
     * @return node to which the value refers via a KeyValidator, or {@code null} if no such node.
     */
    public XsdTreeNode getCoupledKeyrefNodeValue()
    {
        return getCoupledKeyrefNode(this.valueValidators.keySet());
    }

    /**
     * Return coupled node via a key validator.
     * @param validators validators.
     * @return coupled node via a key validator.
     */
    private XsdTreeNode getCoupledKeyrefNode(final Set<ValueValidator> validators)
    {
        for (ValueValidator validator : validators)
        {
            if (validator instanceof CoupledValidator)
            {
                CoupledValidator key = (CoupledValidator) validator;
                key.validate(this); // to trigger finding the right node should value have changed
                return key.getCoupledKeyrefNode(this);
            }
        }
        return null;
    }

    /**
     * Returns all restrictions for Id attribute. These are not sorted and may contain duplicates. Id restrictions may be valid
     * if the Id field point to another element.
     * @return list of restrictions for the Id.
     */
    public List<String> getIdRestrictions()
    {
        return getAttributeRestrictions(this.idIndex);
    }

    /**
     * Returns options based on a set of validators.
     * @param validators validators.
     * @param field field, attribute or child element, for which to obtain the options.
     * @return list of options.
     */
    private List<String> getOptionsFromValidators(final Map<ValueValidator, Object> validators, final String field)
    {
        List<String> combined = null;
        for (Entry<ValueValidator, Object> entry : validators.entrySet())
        {
            ValueValidator validator = entry.getKey();
            List<String> valueOptions = validator.getOptions(this, entry.getValue());
            if (valueOptions != null && combined != null)
            {
                combined = combined.stream().filter(valueOptions::contains).collect(Collectors.toList());
            }
            else if (valueOptions != null)
            {
                combined = valueOptions;
            }
        }
        if (combined != null && !combined.isEmpty())
        {
            return combined;
        }
        return Collections.emptyList();
    }

    /**
     * Returns the base type of the attribute, e.g. xsd:double.
     * @param index attribute index.
     * @return base type of the attribute, e.g. xsd:double.
     */
    public String getAttributeBaseType(final int index)
    {
        if (this.xsdNode.equals(XiIncludeNode.XI_INCLUDE))
        {
            return "xsd:anyURI";
        }
        return ValueValidator.getBaseType(this.attributeNodes.get(index), this.schema);
    }

    /**
     * Returns whether this node is of the type defined by the path.
     * @param path path of the type in dotted xpath notation, e.g. "SignalGroup.TrafficLight".
     * @return whether this node is of the type defined by the path.
     */
    public boolean isType(final String path)
    {
        boolean isType = getPathString().endsWith("." + path);
        if (isType)
        {
            return isType;
        }
        int dot = path.lastIndexOf(".");
        if (dot > -1)
        {
            if (this.parent == null)
            {
                return false; // Node was deleted, but is still visible in the GUI tree for a moment
            }
            isType = isType(path.substring(dot + 1)) && this.parent.isType(path.substring(0, dot));
            if (isType)
            {
                return isType;
            }
        }
        return this.schema.isType(this.xsdNode, path);
    }

    /**
     * Returns whether this node can contain the information of the given node. This only check equivalence of the underlying
     * XSD node. The referring node may be different, as two elements may refer to the same type.
     * @param copied node that was copied, and may be pasted/inserted here.
     * @return whether this node can contain the information of the given node.
     */
    public boolean canContain(final XsdTreeNode copied)
    {
        return this.xsdNode == copied.xsdNode || (this.referringXsdNode != null && copied.referringXsdNode != null
                && DocumentReader.getAttribute(this.referringXsdNode, "type") != null
                && DocumentReader.getAttribute(this.referringXsdNode, "type")
                        .equals(DocumentReader.getAttribute(copied.referringXsdNode, "type")));
    }

    // ====== Interaction with visualization ======

    /**
     * This function can be set externally and supplies an additional {@code String} to clarify this node in the tree.
     * @param stringFunction Function&lt;XsdTreeNode, String&gt; string function.
     * @param overwrite overwrite existing. When {@code true}, a possible existing string function is overwritten.
     */
    public void setStringFunction(final Function<XsdTreeNode, String> stringFunction, final boolean overwrite)
    {
        if (this.stringFunction == null || overwrite)
        {
            this.stringFunction = stringFunction;
        }
    }

    /**
     * A consumer can be set externally and will receive this node when its menu item is selected.
     * @param menuItem name of item as presented to the user.
     * @param consumer editor.
     */
    public void addConsumer(final String menuItem, final Consumer<XsdTreeNode> consumer)
    {
        this.consumers.put(menuItem, consumer);
    }

    /**
     * Returns whether this node has any consumers.
     * @return whether this node has any consumers.
     */
    public boolean hasConsumer()
    {
        return !this.consumers.isEmpty();
    }

    /**
     * Returns the description of this node.
     * @return description of this node, {@code null} if there is none.
     */
    public String getDescription()
    {
        assureAttributesAndDescription();
        return this.description;
    }

    /**
     * Returns the menu items for which this node has consumers.
     * @return menu items for which this node has consumers.
     */
    public Set<String> getConsumerMenuItems()
    {
        return this.consumers.keySet();
    }

    /**
     * Triggers the node to be consumed.
     * @param menuItem menu item.
     */
    public void consume(final String menuItem)
    {
        Throw.when(!this.consumers.containsKey(menuItem), IllegalArgumentException.class, "Unable to consume node for %s.",
                menuItem);
        this.consumers.get(menuItem).accept(this);
    }

    /**
     * Returns a string that is the name of the node, without any additional information on id and additional string function.
     * @return string that is the name of the node.
     */
    public String getShortString()
    {
        if (this.options != null)
        {
            // this name may appear as part of a sequence which is an option for an xsd:choice or xsd:all
            return this.options.toString().toLowerCase();
        }
        if (this.xsdNode.getNodeName().equals("xsd:sequence"))
        {
            // this name may appear as an option for an xsd:choice or xsd:all
            StringBuilder stringBuilder = new StringBuilder();
            Node relevantNode = this.referringXsdNode == null ? this.xsdNode : this.referringXsdNode;
            String annotation = DocumentReader.getAnnotation(relevantNode, "xsd:appinfo", "name");
            if (annotation != null)
            {
                stringBuilder.append(annotation).append("...");
            }
            else
            {
                // no name for sequence specified, build one from child elements (per type to prevent repetition).
                Set<String> coveredTypes = new LinkedHashSet<>();
                String separator = "";
                assureChildren();
                if (!this.active)
                {
                    stringBuilder.append("(inactive)");
                }
                else
                {
                    for (XsdTreeNode child : this.children)
                    {
                        if (!coveredTypes.contains(child.getPathString()) || child.xsdNode.getNodeName().equals("xsd:sequence")
                                || child.xsdNode.getNodeName().equals("xsd:choice")
                                || child.xsdNode.getNodeName().equals("xsd:all"))
                        {
                            stringBuilder.append(separator).append(child.getShortString());
                            separator = " | ";
                            coveredTypes.add(child.getPathString());
                        }
                    }
                }
            }
            if (stringBuilder.length() > MAX_OPTIONNAME_LENGTH)
            {
                return stringBuilder.substring(0, MAX_OPTIONNAME_LENGTH - 2) + "..";
            }
            return stringBuilder.toString();
        }
        if (this.xsdNode.getNodeName().equals("xi:include"))
        {
            return "Include";
        }
        return XsdTreeNodeUtil.separatedName(getNodeName());
    }

    /**
     * Returns the short string, appended with any additional information on id and additional string function.
     * @return short string, appended with any additional information on id and additional string function.
     */
    @Override
    public String toString()
    {
        String string = getShortString();
        if (!this.active)
        {
            return string;
        }
        if (isIdentifiable() && getId() != null && !getId().isEmpty())
        {
            string = string + " " + getId();
        }
        /*-
        String occurs;
        if (this.minOccurs == this.maxOccurs)
        {
            occurs = Integer.toString(this.minOccurs);
        }
        else
        {
            occurs = Integer.valueOf(this.minOccurs) + ".." + (this.maxOccurs == -1 ? "∞" : Integer.valueOf(this.maxOccurs));
        }
        string = string + " [" + occurs + "]";
        */
        if (this.stringFunction != null)
        {
            string = string + " (" + this.stringFunction.apply(this) + ")";
        }
        return string;
    }

    // ====== Save / load ======

    /**
     * Saves the content of this node in a new XML element under the given XML parent. This involves a value, attributes, and
     * children. Children are further saved in a recursive manner. If this node is not active, this method does nothing.<br>
     * <br>
     * If this node represents a sequence as a choice option, all children are saved directly under the given XML parent node,
     * and no information of this node itself is saved (as such nodes have no attributes or value). In other words, in that case
     * this node is only a virtual layer presented to the user, but does not result in a layer in XML.
     * @param document used to create the new XML node.
     * @param xmlParent parent XML node.
     */
    public void saveXmlNodes(final Document document, final Node xmlParent)
    {
        if (!this.active)
        {
            return;
        }

        // xi:include
        if (this.xsdNode.equals(XiIncludeNode.XI_INCLUDE))
        {
            Element element = document.createElement(getNodeName());
            xmlParent.appendChild(element);
            if (this.attributeValues != null && this.attributeValues.get(0) != null)
            {
                element.setAttribute("href", this.attributeValues.get(0));
                if (this.attributeValues.get(1) != null)
                {
                    Element fallback = document.createElement("xi:fallback");
                    element.appendChild(fallback);
                    Element include = document.createElement("xi:include");
                    fallback.appendChild(include);
                    include.setAttribute("href", this.attributeValues.get(1));
                }
            }
            return;
        }

        // sequences do not add a level in the xml, forward directly under parent
        if (this.xsdNode.getNodeName().equals("xsd:sequence"))
        {
            for (int index = 0; index < this.getChildCount(); index++)
            {
                this.children.get(index).saveXmlNodes(document, xmlParent);
            }
            return;
        }

        Element element = document.createElement("ots:" + getNodeName());
        xmlParent.appendChild(element);

        if (this.value != null && !this.value.isEmpty())
        {
            element.setTextContent(this.value);
        }

        for (int index = 0; index < this.attributeCount(); index++)
        {
            String attributeValue = this.attributeValues.get(index);
            if (attributeValue != null && !attributeValue.isEmpty())
            {
                element.setAttribute(getAttributeNameByIndex(index), attributeValue);
            }
        }

        for (int index = 0; index < this.getChildCount(); index++)
        {
            if (!this.children.get(index).isInclude)
            {
                this.children.get(index).saveXmlNodes(document, element);
            }
        }
    }

    /**
     * Parses the information from an XML node into this node. This entails a tag value, attributes, and children, for as far as
     * each of these is present. In a recursive manner, all child nodes are further loaded.
     * @param nodeXml node from XML.
     */
    public void loadXmlNodes(final Node nodeXml)
    {
        setActive();

        // value
        String candidateValue = "";
        if (nodeXml.getChildNodes() != null)
        {
            for (int indexXml = 0; indexXml < nodeXml.getChildNodes().getLength(); indexXml++)
            {
                if (nodeXml.getChildNodes().item(indexXml).getNodeName().equals("#text"))
                {
                    String value = nodeXml.getChildNodes().item(indexXml).getNodeValue();
                    if (!value.isBlank())
                    {
                        candidateValue += value;
                    }
                }
            }
        }
        if (!candidateValue.isEmpty())
        {
            String previous = this.value;
            this.value = candidateValue;
            fireEvent(new Event(VALUE_CHANGED, new Object[] {this, previous}));
        }

        // attributes
        assureAttributesAndDescription();
        if (nodeXml.getAttributes() != null)
        {
            for (int index = 0; index < nodeXml.getAttributes().getLength(); index++)
            {
                Node attributeNode = nodeXml.getAttributes().item(index);
                switch (attributeNode.getNodeName())
                {
                    case "xmlns:ots":
                    case "xmlns:xi":
                    case "xmlns:xsi":
                        continue;
                    case "xsi:schemaLocation":
                        if (this instanceof XsdTreeNodeRoot)
                        {
                            ((XsdTreeNodeRoot) this).setSchemaLocation(attributeNode.getNodeValue());
                        } // else its an include file
                        continue;
                    default:
                        try
                        {
                            setAttributeValue(attributeNode.getNodeName(), attributeNode.getNodeValue());
                        }
                        catch (NoSuchElementException e)
                        {
                            System.err.println("Unable to load attribute " + attributeNode.getNodeName() + "=\""
                                    + attributeNode.getNodeValue() + "\" in " + getShortString());
                        }
                }
            }
        }

        // children
        assureChildren();
        if (nodeXml.getChildNodes() != null)
        {
            List<Integer> indices = new ArrayList<>();
            indices.add(0);
            indices.add(0);
            loadChildren(indices, nodeXml.getChildNodes(), false);
            // while (indices.get(0) < nodeXml.getChildNodes().getLength())
            // {
            // System.err.println(
            // "Unable to load element with name " + nodeXml.getChildNodes().item(indices.get(0)).getNodeName() + ".");
            // indices.set(0, indices.get(0) + 1);
            // loadChildren(indices, nodeXml.getChildNodes());
            // }
            // In included nodes, remove nodes that will not contain any of the loaded xml data. For example remove nodes that
            // are otherwise shown as inactive and allow a user to enable it, which makes no sense for imported nodes.
            if (this.isInclude)
            {
                int index = 0;
                while (index < this.children.size())
                {
                    boolean relevantForAny = false;
                    for (int indexXml = 0; indexXml < nodeXml.getChildNodes().getLength(); indexXml++)
                    {
                        String xmlName = nodeXml.getChildNodes().item(indexXml).getNodeName().replace("ots:", "");
                        if (this.children.get(index).isRelevantNode(xmlName))
                        {
                            relevantForAny = true;
                            break;
                        }
                    }
                    if (!relevantForAny)
                    {
                        // can't do a remove() on the node, as it might just become deactivated and still be visible
                        this.children.remove(index);
                    }
                    else
                    {
                        index++;
                    }
                }
            }
        }
        invalidate();
    }

    /**
     * Parses child nodes from XML in to this node's children, as far as it can given available inactive child nodes. Note that
     * these child nodes are derived from the XSD schema. This method will first find a relevant node to load each child XML
     * node into. The relevant node can be found in two ways:
     * <ol>
     * <li>The previous child node is relevant for the XML child node. This happens when XML specifies multiple nodes of the
     * same type, in a sequence or choice with multiple occurrence. The previous child node will be added, such that information
     * of the XML child node can be loaded in to the added child node.</li>
     * <li>We move to the next child node until we find a node that is relevant for the XML child node. This should only skip
     * inactive nodes for which XML specifies no information.</li>
     * </ol>
     * Next, the information from XML is loaded in to the relevant child node. This can happen in four ways:
     * <ol>
     * <li>The relevant node is not a choice or sequence, information is loaded in to it with {@code loadXmlNodes}.</li>
     * <li>The relevant node is a sequence. The relevant child in the sequence is found, and all XML child nodes that can be
     * loaded in to it, are by calling {@code loadChildren}.</li>
     * <li>The relevant node is a choice, where the relevant option is not a sequence. The option will be set in the choice.
     * Information is loaded in to the selected option with {@code loadXmlNodes}.</li>
     * <li>The relevant node is a choice, where the relevant option is a sequence. The option (sequence node) will be set in the
     * choice. The relevant child in the sequence is found, and all XML child nodes that can be loaded in to it, are by calling
     * {@code loadChildren}.</li>
     * </ol>
     * Note that for case 3, the child content of a deeper {@code XsdChildNode} is defined at the same level in XML. Hence, only
     * some of the XML children may be loaded in the deeper level. To keep track of which XML child nodes are loaded where, the
     * value {@code indices[0]} is given as input (previous nodes have already been loaded at a higher level or in another
     * choice sequence). In this value also the index of the first XML child node that could not be loaded in the choice
     * sequence is returned.<br>
     * <br>
     * The parameter {@code indices} is also used when an XML node cannot be loaded at all because it does not comply with the
     * XSD schema. This will cause the loading to run through all children to see whether it can be loaded there. The second
     * value {@code indices[1]} is used as input to know where to continue in a second call to this method after an earlier call
     * came across an XML node that could not be loaded. In {@code indices[1]} the index of the last child node in to which XML
     * data was loaded is given.
     * @param indices index of the first XML child node to load, and first XsdTreeNode index to use.
     * @param childrenXml list of XML child nodes as specified within one parent XML tag.
     * @param loadingSubSequence whether this call is loading children as a sub-sequence.
     */
    protected void loadChildren(final List<Integer> indices, final NodeList childrenXml, final boolean loadingSubSequence)
    {
        List<XsdTreeNode> loadedChildren = new ArrayList<>();
        int passes = 0;
        int maxPasses = 1;
        int loadedDuringPass = 0;
        Node complexType = DocumentReader.getChild(this.xsdNode, "xsd:complexType");
        if (complexType != null)
        {
            Node sequence = DocumentReader.getChild(complexType, "xsd:sequence");
            if (sequence != null)
            {
                String maxOccurs = DocumentReader.getAttribute(sequence, "maxOccurs");
                if (maxOccurs != null)
                {
                    maxPasses = maxOccurs.equalsIgnoreCase("unbounded") ? Integer.MAX_VALUE : Integer.valueOf(maxOccurs);
                }
            }
        }

        int indexXml = indices.get(0);
        int childIndex = indices.get(1);
        while (indexXml < childrenXml.getLength())
        {
            Node childNodeXml = childrenXml.item(indexXml);
            if (childNodeXml.getNodeName().equals("#text"))
            {
                indexXml++;
                continue;
            }

            // catch fallback
            String nameXml = childNodeXml.getNodeName().replace("ots:", "");
            if (this.xsdNode == XiIncludeNode.XI_INCLUDE && "xi:fallback".equals(nameXml))
            {
                String fallback = childNodeXml.getChildNodes().item(1).getAttributes().getNamedItem("href").getNodeValue();
                setAttributeValue(1, fallback);
                indexXml++;
                continue;
            }

            // find relevant node: previous node, or skip to next until we find the relevant node
            if (childIndex > 0 && this.children.get(childIndex - 1).isRelevantNode(nameXml))
            {
                if (childIndex >= this.children.size() || !this.children.get(childIndex).isRelevantNode(nameXml))
                {
                    this.children.get(childIndex - 1).add();
                }
            }
            else
            {
                while (childIndex < this.children.size() && (!this.children.get(childIndex).isRelevantNode(nameXml)
                        || loadedChildren.contains(this.children.get(childIndex))))
                {
                    childIndex++;
                }
                if (childIndex >= this.children.size())
                {
                    if (loadedDuringPass == 0)
                    {
                        System.err.println("Failing to load " + nameXml + ", it is not a valid node.");
                        indexXml++;
                        childIndex = 0; // start next pass
                        continue;
                    }
                    else
                    {
                        passes++;
                        if (passes >= maxPasses)
                        {
                            if (!loadingSubSequence)
                            {
                                System.err.println("Failing to load " + nameXml + ", maximum number of passes reached.");
                            }
                            indices.set(0, indexXml);
                            return;
                        }
                    }
                    childIndex = 0; // start next pass
                    loadedDuringPass = 0;
                    continue;
                }
            }

            // load information in relevant node, can be a choice, can be a sequence in a choice
            XsdTreeNode relevantChild = this.children.get(childIndex);
            if (relevantChild.choice == null)
            {
                if (relevantChild.getNodeName().equals("xsd:sequence"))
                {
                    List<Integer> sequenceIndices = new ArrayList<>();
                    sequenceIndices.add(indexXml);
                    sequenceIndices.add(0);
                    relevantChild.loadChildren(sequenceIndices, childrenXml, true);
                    loadedChildren.add(relevantChild);
                    indexXml = sequenceIndices.get(0) - 1;
                }
                else
                {
                    relevantChild.loadXmlNodes(childNodeXml);
                    loadedChildren.add(relevantChild);
                }
            }
            else
            {
                boolean optionSet = false;
                for (XsdTreeNode option : relevantChild.choice.options)
                {
                    if (option.xsdNode.getNodeName().equals("xsd:sequence"))
                    {
                        for (XsdTreeNode child : option.children)
                        {
                            if (child.isRelevantNode(nameXml))
                            {
                                relevantChild.choice.setOption(option);
                                List<Integer> optionIndices = new ArrayList<>();
                                optionIndices.add(indexXml);
                                optionIndices.add(0);
                                option.loadChildren(optionIndices, childrenXml, true);
                                loadedChildren.add(option);
                                indexXml = optionIndices.get(0) - 1;
                                optionSet = true;
                                break;
                            }
                        }
                    }
                    if (option.getNodeName().equals(nameXml))
                    {
                        relevantChild.choice.setOption(option);
                        option.loadXmlNodes(childNodeXml);
                        optionSet = true;
                    }
                    if (optionSet)
                    {
                        for (XsdTreeNode otherOption : relevantChild.choice.options)
                        {
                            if (!otherOption.equals(option))
                            {
                                otherOption.setActive();
                            }
                        }
                        break;
                    }
                }
            }
            loadedDuringPass++;
            childIndex++;
            indices.set(1, childIndex);
            indexXml++;
        }
        indices.set(0, indexXml);
    }

    /**
     * Checks whether this node is relevant to contain the information of the given tag name from XML. Being relevant means any
     * of the following:
     * <ol>
     * <li>The name of this node is equal to the tag, and thus directly contains the tag information.</li>
     * <li>This node is a sequence that has a child element that is considered relevant (in a recursive manner).</li>
     * <li>This node is a choice, and an option of this choice is considered relevant (in a recursive manner).</li>
     * <li>This node is a choice, and one of its options is a sequence that has a child element that is considered relevant (in
     * a recursive manner).</li>
     * </ol>
     * Given the recursive nature of 2, 3 and 4, in the end some node has a name equal to the tag from XML.
     * @param nameXml tag name from XML.
     * @return whether this node is relevant to contain the information of the given tag name from XML.
     */
    private boolean isRelevantNode(final String nameXml)
    {
        if (this.getNodeName().equals(nameXml))
        {
            return true;
        }
        if (this.choice == null)
        {
            if (getNodeName().equals("xsd:sequence"))
            {
                this.active = true;
                assureChildren();
                for (XsdTreeNode child : this.children)
                {
                    boolean relevant = child.isRelevantNode(nameXml);
                    if (relevant)
                    {
                        return relevant;
                    }
                }
            }
            return false;
        }
        if (this.choice.selected.equals(this))
        {
            for (XsdTreeNode option : this.choice.options)
            {
                if (option.xsdNode.getNodeName().equals("xsd:sequence"))
                {
                    option.active = true;
                    option.assureChildren();
                    for (XsdTreeNode child : option.children)
                    {
                        boolean relevant = child.isRelevantNode(nameXml);
                        if (relevant)
                        {
                            return relevant;
                        }
                    }
                }
                else if (!option.equals(this))
                {
                    boolean relevant = option.isRelevantNode(nameXml);
                    if (relevant)
                    {
                        return relevant;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public boolean addListener(final EventListener listener, final EventType eventType)
    {
        boolean result = super.addListener(listener, eventType);
        /*
         * Prioritizes KeyValidators: when an Id attribute is changed this will update any referring nodes to the new value
         * first, before any other listener may break the coupling. Coupling is broken when for example validation is performed
         * with the new Id value, to obtain the coupled node.
         */
        if (eventType.equals(XsdTreeNode.ATTRIBUTE_CHANGED))
        {
            EventListenerMap map = getEventListenerMap();
            List<Reference<EventListener>> list = map.get(eventType);
            List<Reference<EventListener>> keys = new ArrayList<>();
            for (Reference<EventListener> listen : list)
            {
                if (listen.get() instanceof KeyValidator)
                {
                    keys.add(listen);
                }
            }
            list.removeAll(keys);
            list.addAll(0, keys);
        }
        return result;
    }

}
