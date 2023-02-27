package org.opentrafficsim.editor;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.djutils.event.Event;
import org.djutils.event.EventListener;
import org.djutils.exceptions.Throw;
import org.djutils.immutablecollections.Immutable;
import org.djutils.immutablecollections.ImmutableArrayList;
import org.djutils.immutablecollections.ImmutableList;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * Underlying data structure object of the editor. Starting with the root node "OTS", all the information is stored in a tree.
 * The tree follows the XSD logic, e.g. "OTS.NETWORK.LINK". {@code XsdTreeNode}'s have a {@code Node} object from the XSD DOM
 * tree. From this information it can be derived what the child nodes should be, and which attributes are contained.<br>
 * <br>
 * This class is mostly straightforward in the sense that there are direct parent-child relations, and that changing an option
 * replaces a node. When an xsd:sequence is part of an xsd:choice, things become complex as the xsd:sequence is a single option.
 * In particular, the xsd:sequence may itself have xsd:choice's. In that case the notion of a {@code parentChoice} comes in to
 * play. The resulting structure will in that case be:
 * 
 * <pre>
 *  xsd:element  v-. (lane)
 *      xsd:choice-'<-------------------.
 *       |> xsd:element (centeroffeset) |
 * option|> xsd:element (leftoffset)    |
 *       |> xsd:element (rightoffset)   | parentChoice
 *       '> xsd:sequence v-.            |
 *              xsd:choice-'------------'
 *               |> xsd:element (centeroffesetstart)
 *         option|> xsd:element (leftoffsetstart)
 *               '> xsd:element (rightoffsetstart)
 *              xsd:choice
 *               |> xsd:element (centeroffesetend)
 *         option|> xsd:element (leftoffsetend)     {identical relations as 'start'}
 *               '> xsd:element (rightoffsetend)
 * </pre>
 * 
 * Note that here each shown xsd:{} becomes an {@code XsdTreeNode}, but only the xsd:element's are ever shown in the tree.
 * Normally, xsd:sequence nodes do not result in an {@code XsdTreeNode}; their content is usually flattened with higher levels.
 * All sub-xsd:element's shown here have as a parent the highest xsd:element, and it's children are only the other
 * xsd:element's. But only when actively selected. When another option is chosen, nodes are exchanged at the highest level. The
 * option nodes under an xsd:choice however reference to the choice with the {@code choice} property. In this way they are aware
 * that they are an option, and should show different available options. The choice also self-references as such. Each node base
 * on an xsd:choice has all options under the {@code options} property, with the selected one stored under {@code selected}. As
 * the lowest elements in this diagram should also allow the user to select an option back at the higher level, the
 * {@code parentChoice} property of the lower xsd:choice node is required.<br>
 * <br>
 * If in the future we are faced with more deeply layered, or variously layered, structures, a simpler and more robust approach
 * is to let each xsd:sequence be a visible {@code XsdTreeNode}, possibly with choice and drop-down icon to match. This is then
 * a virtual layer not representing XML reading and writing.
 * @author wjschakel
 */
public class XsdTreeNode implements Serializable
{

    /** */
    private static final long serialVersionUID = 20230224L;

    /** Limit on displayed option name to avoid huge menu's. */
    private static final int MAX_OPTIONNAME_LENGTH = 64;

    /** Parent node. */
    private XsdTreeNode parent;

    /** Node from XSD that this {@code XsdTreeNode} represents. Most typically an xsd:element node. */
    private Node xsdNode;

    /** Nodes from XSD that are between the XSD node of the parent, and this tree node's XSD node. */
    private final ImmutableList<Node> hiddenNodes;

    /**
     * Element defining node that referred to a type. The type is defined by {@code .xsdNode}, the referring node is used for
     * original information on name and occurrence. For simple element nodes this is {@code null}.
     */
    private Node referringXsdNode;

    /** XSD schema from which to get type and element nodes that are referred to. */
    private final XsdSchema schema;

    /** Minimum number of this element under the parent node, as defined in minOccurs in XSD. */
    private int minOccurs = 0;

    /** Maximum number of this element under the parent node, as defined in maxOccurs in XSD. */
    private int maxOccurs = -1;

    /**
     * Path string of this element, e.g. "OTS.DEFINITIONS.ROADLAYOUTS". This is used to identify each unique type of element.
     */
    private final String pathString;

    // ====== Choice/Options ======

    /** Choice node in case this choice is in a sequence of choices, where the sequence is an option in the parent choice. */
    private XsdTreeNode parentChoice;

    /** Choice node, represents an xsd:choice of which 1 option is shown. All options are {@code XsdTreeNode}'s themselves. */
    private XsdTreeNode choice;

    /** Option nodes. These can be directly applicable in the tree, or they can represent an xsd:sequence. */
    private List<XsdTreeNode> options;

    /** Currently selection option in the choice node. */
    private XsdTreeNode selected;

    /** Prevents cyclical removal through choice nodes that remove all their options. */
    private boolean ignoreRemove = false;

    // ====== Children ======

    /** Children nodes. */
    private List<XsdTreeNode> children;

    // ====== Attributes ======

    /** Attribute XSD nodes. */
    private List<Node> attributeNodes;

    /** Attribute values. */
    private List<String> attributeValues;

    // ====== Properties to expose to the GUI ======

    /** Whether the node is active. Inactive nodes show the user what type of node can be created in its place. */
    private boolean active;

    /**
     * When the node has been deactivated, activation should only set {@code active = true}. Other parts of the activation
     * should be ignored as the node was in an active state before, i.e. those parts are in tact. Deactivation does not affect
     * those parts.
     */
    private boolean deactivated;

    /** Whether this node is identifiable, i.e. has an ID attribute. */
    private Boolean isIdentifiable;

    /** Attribute index of ID. */
    private int idIndex;

    /** Whether this node is editable, i.e. has a simple value, e.g. &lt;NODE&gt;Simple value&lt;/NODE&gt;. */
    private Boolean isEditable;

    /** Stored simple value of the node. */
    private String value;

    /**
     * Position of this node in a group as it should be visualized to the user. This always involves a sequence in a choice,
     * i.e. the group is one option.
     */
    private GroupPosition groupPosition;

    // ====== Interaction with visualization ======

    /** This function can be set externally and supplies an additional {@code String} to clarify this node in the tree. */
    private Function<XsdTreeNode, String> stringFunction;

    /** A consumer can be set externally and will receive this node when its menu item is selected. */
    private Map<String, Consumer<XsdTreeNode>> consumers = new LinkedHashMap<>();

    /**
     * Constructor for root node, based on a document. Note: {@code XsdTreeNodeRoot} should be used for the root. The document
     * is forwarded to an {@code XsdSchema} that will be available to all nodes in the tree.
     * @param document Document; XSD document.
     */
    protected XsdTreeNode(final Document document)
    {
        Throw.whenNull(document, "Document may not be null.");
        this.parent = null;
        this.hiddenNodes = new ImmutableArrayList<>(Collections.emptyList());
        this.schema = new XsdSchema(document);
        this.xsdNode = this.schema.getRoot();
        this.referringXsdNode = null;
        setOccurs();
        this.pathString = buildPathLocation();
        this.active = true;
    }

    /**
     * Construct a node without referring node.
     * @param parent XsdTreeNode; parent.
     * @param xsdNode Node; XSD node that this tree node represents.
     * @param hiddenNodes ImmutableList&lt;Node&gt;; nodes between the XSD node of the parent, and this tree node's XSD node.
     */
    private XsdTreeNode(final XsdTreeNode parent, final Node xsdNode, final ImmutableList<Node> hiddenNodes)
    {
        this(parent, xsdNode, hiddenNodes, null);
    }

    /**
     * Constructor with referring node for extended types. If the node is xsd:choice, this node will represent the choice. All
     * options are then created as separate {@code XsdTreeNode}'s by this constructor. For each option that is an xsd:sequence,
     * this constructor will also create the nodes in that sequence, as those nodes function on the child-level of this node.
     * They are coupled to this choice by their {@code parentChoice}, allowing them to delete and add on this level.<br>
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
     * @param parent XsdTreeNode; parent.
     * @param xsdNode Node; XSD node that this tree node represents.
     * @param hiddenNodes ImmutableList&lt;Node&gt;; nodes between the XSD node of the parent, and this tree node's XSD node.
     * @param referringXsdNode Node; original node that referred to {@code Node} through a ref={ref} or type={type} attribute,
     *            it is used for naming and occurrence, may be {@code null} if not applicable.
     */
    private XsdTreeNode(final XsdTreeNode parent, final Node xsdNode, final ImmutableList<Node> hiddenNodes,
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
        if (xsdNode.getNodeName().equals("xsd:choice"))
        {
            this.options = new ArrayList<>();
            addChildren(xsdNode, parent, this.options, hiddenNodes, this.schema, false);
            this.choice = this;
            for (XsdTreeNode option : this.options)
            {
                option.minOccurs = this.minOccurs;
                option.maxOccurs = this.maxOccurs;
                if (this.minOccurs > 0)
                {
                    option.setActive();
                }
                if (option.xsdNode.getNodeName().equals("xsd:sequence"))
                {
                    option.children = new ArrayList<>();
                    addChildren(option.xsdNode, parent, option.children, option.hiddenNodes, this.schema, false);
                    option.children.forEach((child) -> child.parentChoice = this);
                    setSequenceOrder(option.children);
                }
                option.choice = this;
            }
        }
        try
        {
            ((XsdTreeNodeRoot) getPath().get(0)).fireEvent(XsdTreeNodeRoot.NODE_CREATED, this);
        }
        catch (RemoteException exception)
        {
            throw new RuntimeException("Unexpected exception when throwing NOCE_CREATED event.");
        }
    }

    /**
     * Recursively throws creation events for all current nodes in the tree. This method is for {@code XsdTreeNodeRoot}.
     * @param node XsdTreeNode; node.
     * @param listener EventListener; listener.
     * @throws RemoteException if event cannot be fired.
     */
    protected void fireCreatedEventOnExistingNodes(final XsdTreeNode node, final EventListener listener) throws RemoteException
    {
        Event event = new Event(XsdTreeNodeRoot.NODE_CREATED, node);
        listener.notify(event);
        Set<XsdTreeNode> subNodes = node.children == null ? new LinkedHashSet<>() : new LinkedHashSet<>(node.children);
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
    }

    /**
     * Sets the {@code GroupPosition} for the nodes in the sequence.
     * @param sequence List&lt;XsdTreeNode&gt;; sequence of nodes, part of one xsd:squence.
     */
    private static void setSequenceOrder(final List<XsdTreeNode> sequence)
    {
        if (sequence.size() == 1)
        {
            sequence.get(0).groupPosition = GroupPosition.SOLO;
            return;
        }
        sequence.get(0).groupPosition = GroupPosition.FIRST;
        for (int index = 1; index < sequence.size() - 1; index++)
        {
            sequence.get(index).groupPosition = GroupPosition.MIDDLE;
        }
        sequence.get(sequence.size() - 1).groupPosition = GroupPosition.LAST;
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
        this.minOccurs = getOccurs(node, "minOccurs");
        this.maxOccurs = getOccurs(node, "maxOccurs");
        for (int index = this.hiddenNodes.size() - 1; index >= 0; index--)
        {
            Node hiddenNode = this.hiddenNodes.get(index);
            if (hiddenNode.getNodeName().equals("xsd:sequence") || hiddenNode.getNodeName().equals("xsd:choice"))
            {
                this.minOccurs *= getOccurs(hiddenNode, "minOccurs");
                int max = getOccurs(hiddenNode, "maxOccurs");
                this.maxOccurs = this.maxOccurs < 0 || max < 0 ? -1 : this.maxOccurs * max;
            }
            else
            {
                break; // as soon as we bump up another type of node, no more sequences and choices can appear until parent type
            }
        }
    }

    /**
     * Parses the minOcccurs or maxOccurs value from given node. If it is not supplied, the default of 1 is given.
     * @param node Node; node.
     * @param attribute String; "minOccurs" or "maxOccurs".
     * @return int; value of occurs, -1 represents "unbounded".
     */
    private static int getOccurs(final Node node, final String attribute)
    {
        String occurs = XsdSchema.getAttribute(node, attribute);
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
     * Builds the path location, e.g. "OTS.DEFINITIONS.ROADLAYOUTS".
     * @return String; the path location.
     */
    private String buildPathLocation()
    {
        List<XsdTreeNode> path = getPath();
        StringBuilder pathStr = new StringBuilder(((XsdTreeNode) path.get(0)).getNodeString());
        for (int i = 1; i < path.size(); i++)
        {
            pathStr.append(".").append(((XsdTreeNode) path.get(i)).getNodeString());
        }
        return pathStr.toString();
    }

    /**
     * Returns the path from root to this node.
     * @return List&lt;XsdTreeNode&gt;; list of node along the path.
     */
    public List<XsdTreeNode> getPath()
    {
        List<XsdTreeNode> path = this.parent != null ? this.parent.getPath() : new ArrayList<>();
        path.add(this);
        return path;
    }

    /**
     * Returns the name of this node, as appropriate in XML. Examples are NODE, ROADLAYOUT, and TACTICALPLANNER. Most typically
     * this is the "name" attribute of an xsd:element. In other cases it is the ref={ref} attribute of the referring
     * {@code Node}. In rare cases it is "xi:include".
     * @return String; name of this node, as appropriate in XML.
     */
    private String getNodeString()
    {
        Node node = this.referringXsdNode == null ? this.xsdNode : this.referringXsdNode;
        String ref = XsdSchema.getAttribute(node, "ref");
        if (ref != null)
        {
            return ref.replace("ots:", "");
        }
        String name = XsdSchema.getAttribute(node, "name");
        if (name != null)
        {
            return name.replace("ots:", "");
        }
        return node.getNodeName().replace("ots:", "");
    }

    // ====== Choice/Options ======

    /**
     * Returns whether this node is (part of) a choice, i.e. should show an option can be set here. This is true if it is
     * directly an option, or as part of an xsd:sequence which is an option. In the latter case this is known through the
     * {@code parentChoice} property.
     * @return boolean; whether this node is (part of) a choice, i.e. should show an option can be set here.
     */
    public boolean isChoice()
    {
        return this.choice != null || this.parentChoice != null;
    }

    /**
     * Returns a list of options. In case of a {@code parentChoice}, this will also contain the options relevant to that choice.
     * @return List&lt;XsdOption&gt;; list of options.
     */
    public List<XsdOption> getOptions()
    {
        List<XsdOption> out = new ArrayList<>();
        boolean first = true;
        int shift = toTopOfOption();
        if (this.choice != null)
        {
            for (XsdTreeNode node : this.choice.options)
            {
                out.add(new XsdOption(node, this.choice, first, node.equals(this.choice.selected), shift));
                first = false;
            }
            if (this.choice.parentChoice != null)
            {
                first = true;
                shift = this.choice.parentChoice.selected.children.indexOf(this.choice);
                for (XsdTreeNode node : this.choice.parentChoice.options)
                {
                    out.add(new XsdOption(node, this.choice.parentChoice, first, node.equals(this.choice.parentChoice.selected),
                            shift));
                    first = false;
                }
            }
        }
        else if (this.parentChoice != null)
        {
            for (XsdTreeNode node : this.parentChoice.options)
            {
                out.add(new XsdOption(node, this.parentChoice, first, node.equals(this.parentChoice.selected), shift));
                first = false;
            }
        }
        return out;
    }

    /**
     * Returns the shift towards the top of an xsd:sequence that is at the whole an option. If this node is not in a sequence, 0
     * is returned.
     * @return int; shift towards the top of an xsd:sequence that is at the whole an option.
     */
    private int toTopOfOption()
    {
        Throw.when(!isChoice(), IllegalStateException.class, "Setting option on node that is not (part of) a choice.");
        XsdTreeNode relevantChoice = this.parentChoice == null ? this.choice : this.parentChoice;
        int index = relevantChoice.options.indexOf(this);
        if (index >= 0)
        {
            return 0; // option with single node
        }
        for (XsdTreeNode option : relevantChoice.options)
        {
            if (option.xsdNode.getNodeName().equals("xsd:sequence"))
            {
                index = option.children.indexOf(this);
                if (index >= 0)
                {
                    return index;
                }
            }
        }
        return 0;
    }

    /**
     * Sets the node as newly selected option. All current nodes from the choice set are removed from the parent node.
     * @param node XsdTreeNode; newly selected node. Must be part of the choice that this node represents.
     */
    public void setOption(final XsdTreeNode node)
    {
        Throw.when(!isChoice(), IllegalStateException.class, "Setting option on node that is not (part of) a choice.");
        Throw.when(!this.choice.options.contains(node), IllegalStateException.class,
                "Setting option on node that does not have this option.");
        this.choice.selected = node;
        int index = removeAnyFromParent();
        if (node.xsdNode.getNodeName().equals("xsd:sequence"))
        {
            List<XsdTreeNode> added = new ArrayList<>();
            for (XsdTreeNode child : node.children)
            {
                if (child.choice != null)
                {
                    this.parent.children.add(index, child.selected);
                    added.add(child.selected);
                }
                else
                {
                    this.parent.children.add(index, child);
                    added.add(child);
                }
                index++;
            }
            setSequenceOrder(added);
        }
        else
        {
            // 1-on-1 exchange, copy possible sequence location (or null)
            this.parent.children.add(index, node);
            node.groupPosition = this.choice.groupPosition;
        }
    }

    /**
     * Remove all option values from parent, and return appropriate index to insert newly chosen option. This is a deep
     * procedure, considering all options, in the case of an xsd:sequence option those elements, and if those have options those
     * too.<br>
     * <br>
     * Note: deeper structures are possible in XSD, but not used in OTS.
     * @return int; insertion index for new options.
     */
    private int removeAnyFromParent()
    {
        int insertIndex = -1;
        int removeIndex = this.parent.children.indexOf(this);
        if (removeIndex >= 0)
        {
            insertIndex = resolveInsertion(insertIndex, removeIndex);
            this.parent.children.remove(removeIndex);
        }
        for (XsdTreeNode node : this.choice.options)
        {
            if (node.xsdNode.getNodeName().equals("xsd:sequence"))
            {
                for (XsdTreeNode child : node.children)
                {
                    if (child.options != null)
                    {
                        for (XsdTreeNode childElement : child.options)
                        {
                            removeIndex = this.parent.children.indexOf(childElement);
                            if (removeIndex >= 0)
                            {
                                insertIndex = resolveInsertion(insertIndex, removeIndex);
                                this.parent.children.remove(removeIndex);
                            }
                        }
                    }
                    removeIndex = this.parent.children.indexOf(child);
                    if (removeIndex >= 0)
                    {
                        insertIndex = resolveInsertion(insertIndex, removeIndex);
                        this.parent.children.remove(removeIndex);
                    }
                }
            }
            removeIndex = this.parent.children.indexOf(node);
            if (removeIndex >= 0)
            {
                insertIndex = resolveInsertion(insertIndex, removeIndex);
                this.parent.children.remove(removeIndex);
            }
        }
        return insertIndex < 0 ? this.parent.children.size() : insertIndex;
    }

    /**
     * Takes the minimum of both indices, while ignoring negative values (indicating an element was not found for deletion).
     * @param insertIndex int; previously determined insertion index; may be updated to lower value.
     * @param removeIndex int; index of element that is removed.
     * @return int; minimum of both indices, while ignoring negative values.
     */
    private int resolveInsertion(final int insertIndex, final int removeIndex)
    {
        int tmp = insertIndex < 0 ? removeIndex : insertIndex;
        return tmp < removeIndex ? tmp : removeIndex;
    }

    // ====== Children ======

    /**
     * Returns the number of children; directly for an {@code XsdTreeTableModel}.
     * @return int; number of children.
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
     * @param index int; child index.
     * @return XsdTreeNode; child.
     */
    public XsdTreeNode getChild(final int index)
    {
        assureChildren();
        return this.children.get(index);
    }

    /**
     * Returns a list of the child nodes.
     * @return List&lt;XsdTreeNode&gt;; list of the child nodes; safe copy.
     */
    public List<XsdTreeNode> getChildren()
    {
        assureChildren();
        return new ArrayList<>(this.children);
    }

    /**
     * Assures children are present. If a child has minOccurs > 1, additional child nodes are added. Result is cached.
     */
    private void assureChildren()
    {
        if (this.children != null)
        {
            return;
        }
        this.children = new ArrayList<>();
        if (!this.active)
        {
            return;
        }
        if (!this.xsdNode.hasChildNodes())
        {
            return;
        }
        Map<Node, ImmutableList<Node>> relevantNodes =
                getRelevantNodesWithChildren(this.xsdNode, new ImmutableArrayList<>(Collections.emptyList()), this.schema);
        for (Entry<Node, ImmutableList<Node>> entry : relevantNodes.entrySet())
        {
            addChildren(entry.getKey(), this, this.children, entry.getValue(), this.schema, true);
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
     * Returns from the XSD definition the appropriate nodes to take children from at the level of the input node, in the order
     * in which they should appear. This is often the xsd:complexType within an xsd:element, but can become as complex as
     * containing multiple xsd:extension and their referred base types. An xsd:sequence is also common. Adding children in the
     * order as they appear per {@code Node}, and in the order the {@Node}'s are given, results in an overall order suitable for
     * XML.
     * @param node Node; node to expand further.
     * @param hiddenNodes ImmutableList&lt;Node&gt;; nodes between the XSD node of the parent, and this tree node's XSD node.
     * @param schema XsdSchema; schema to retrieve types.
     * @return Map&lt;Node, ImmutableList&lt;Node&gt;&gt;; map of nodes containing relevant children at the level of the input
     *         node, and their appropriate hidden nodes.
     */
    private static Map<Node, ImmutableList<Node>> getRelevantNodesWithChildren(final Node node,
            final ImmutableList<Node> hiddenNodes, final XsdSchema schema)
    {
        Node complexType = node.getNodeName().equals("xsd:complexType") ? node : XsdSchema.getChild(node, "xsd:complexType");
        if (complexType != null)
        {
            Node sequence = XsdSchema.getChild(complexType, "xsd:sequence");
            if (sequence != null)
            {
                return Map.of(sequence, append(hiddenNodes, complexType));
            }
            Node complexContent = XsdSchema.getChild(complexType, "xsd:complexContent");
            if (complexContent != null)
            {
                Node extension = XsdSchema.getChild(complexContent, "xsd:extension");
                if (extension != null)
                {
                    ImmutableList<Node> hiddenExtension = append(append(hiddenNodes, complexType), complexContent);
                    LinkedHashMap<Node, ImmutableList<Node>> elements = new LinkedHashMap<>();
                    String base = XsdSchema.getAttribute(extension, "base");
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
     * Main expansion algorithm. Loops all child XSD nodes, and selects those that define next elements.
     * @param node Node; node to get the children of.
     * @param parentNode XsdTreeNode; parent node for the created children.
     * @param children List&lt;XsdTreeNode&gt;; list to add the children to. This may be different from
     *            {@code parentNode.children} due to layered choice structures.
     * @param hiddenNodes ImmutableList&lt;Node&gt;; nodes between the XSD node of the parent, and this tree node's XSD node.
     * @param schema XsdSchema; schema to get types and referred elements from.
     * @param flattenSequence boolean; when true, treats an xsd:sequence child as an extension of the node. In the context of a
     *            choice this should remain separated.
     */
    private static void addChildren(final Node node, final XsdTreeNode parentNode, final List<XsdTreeNode> children,
            final ImmutableList<Node> hiddenNodes, final XsdSchema schema, final boolean flattenSequence)
    {
        for (int childIndex = 0; childIndex < node.getChildNodes().getLength(); childIndex++)
        {
            Node child = node.getChildNodes().item(childIndex);
            switch (child.getNodeName())
            {
                case "xsd:element":
                    XsdTreeNode element;
                    String ref = XsdSchema.getAttribute(child, "ref");
                    String type = XsdSchema.getAttribute(child, "type");
                    if (ref != null)
                    {
                        element = new XsdTreeNode(parentNode, ref(child, ref, schema), append(hiddenNodes, node), child);
                    }
                    else if (type != null)
                    {
                        Node typedNode = type(child, type, schema);
                        if (typedNode == null)
                        {
                            // xsd:string or other basic type
                            element = new XsdTreeNode(parentNode, child, append(hiddenNodes, node));
                        }
                        else
                        {
                            element = new XsdTreeNode(parentNode, typedNode, append(hiddenNodes, node), child);
                        }
                    }
                    else
                    {
                        element = new XsdTreeNode(parentNode, child, append(hiddenNodes, node));
                    }
                    children.add(element);
                    break;
                case "xsd:sequence":
                    if (flattenSequence)
                    {
                        addChildren(child, parentNode, children, append(hiddenNodes, node), schema, flattenSequence);
                    }
                    else
                    {
                        // add sequence as option, 'children' is a list of options for a choice
                        children.add(new XsdTreeNode(parentNode, child, append(hiddenNodes, node)));
                    }
                    break;
                case "xsd:choice":
                    XsdTreeNode choice = new XsdTreeNode(parentNode, child, append(hiddenNodes, node));
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
                    children.add(new XsdTreeNode(parentNode, child, append(hiddenNodes, node)));
                    break;
                case "xsd:attribute":
                case "xsd:annotation":
                case "xsd:simpleType": // only defines xsd:restriction with xsd:pattern/xsd:enumeration
                case "xsd:restriction":
                case "xsd:simpleContent": // bit of a late capture, followed "type" attribute and did not check what it was
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
    private static ImmutableList<Node> append(final ImmutableList<Node> hiddenNodes, final Node node)
    {
        List<Node> list = new ArrayList<>(hiddenNodes.size() + 1);
        list.addAll(hiddenNodes.toCollection());
        list.add(node);
        return new ImmutableArrayList<>(list, Immutable.WRAP);
    }

    /**
     * Returns the element referred to by ref={ref} in an xsd:element. Will return {@code XiIncludeNode.XI_INCLUDE} for
     * xi:include.
     * @param node Node; node, must have ref={ref} attribute.
     * @param ref String; value of ref={ref}.
     * @param schema XsdSchema; schema to take element from.
     * @return Node; element referred to by ref={ref} in an xsd:element.
     */
    private static Node ref(final Node node, final String ref, final XsdSchema schema)
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
    private static Node type(final Node node, final String type, final XsdSchema schema)
    {
        if (type.startsWith("xsd:"))
        {
            return null;
        }
        Node typeNode = schema.getType(type);
        Throw.when(typeNode == null, RuntimeException.class, "Unable to load type for %s from XSD schema.", type);
        return typeNode;
    }

    // ====== Attributes ======

    /**
     * Finds all attributes that meet the following structure. If the attribute also specifies a {@code default}, it is also
     * stored.
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
    private synchronized void assureAttributes()
    {
        if (this.attributeNodes != null)
        {
            return;
        }
        this.attributeNodes = new ArrayList<>();
        this.attributeValues = new ArrayList<>();
        if (!this.active)
        {
            return;
        }
        Node complexType = this.xsdNode.getNodeName().equals("xsd:complexType") ? this.xsdNode
                : XsdSchema.getChild(this.xsdNode, "xsd:complexType");
        if (complexType != null && this.xsdNode.hasChildNodes())
        {
            findAttributes(complexType);
        }
    }

    /**
     * Finds attributes in a nested way, looking up base types.
     * @param node Node; node.
     */
    private void findAttributes(final Node node)
    {
        for (int childIndex = 0; childIndex < node.getChildNodes().getLength(); childIndex++)
        {
            Node child = node.getChildNodes().item(childIndex);
            if (child.getNodeName().equals("xsd:attribute") && XsdSchema.getAttribute(child, "name") != null)
            {
                this.attributeNodes.add(child);
                this.attributeValues.add(XsdSchema.getAttribute(child, "default")); // may be null
            }
            if (child.getNodeName().equals("xsd:complexContent") || child.getNodeName().equals("xsd:simpleContent"))
            {
                Node extension = XsdSchema.getChild(child, "xsd:extension");
                if (extension != null)
                {
                    findAttributes(extension);
                    String base = XsdSchema.getAttribute(extension, "base");
                    Node baseNode = this.schema.getType(base);
                    if (baseNode != null)
                    {
                        findAttributes(baseNode);
                    }
                }
            }
        }
    }

    /**
     * Returns the number of attributes; directly for an {@code XsdAttributesTableModel}.
     * @return int; number of attributes.
     */
    public int attributeCount()
    {
        if (!this.active)
        {
            return 0;
        }
        assureAttributes();
        return this.attributeNodes.size();
    }

    /**
     * Returns the attributes at given index; directly for an {@code XsdAttributesTableModel}.
     * @param index int; attribute index.
     * @return Node; attribute node.
     */
    public Node getAttributeNode(final int index)
    {
        assureAttributes();
        return this.attributeNodes.get(index);
    }

    /**
     * Sets an attribute value; directly for an {@code XsdAttributesTableModel}.
     * @param index int; index of the attribute.
     * @param value String; value of the attribute.
     */
    @SuppressWarnings("checkstyle:hiddenfield")
    public void setAttributeValue(final int index, final String value)
    {
        assureAttributes();
        this.attributeValues.set(index, value);
    }

    /**
     * Returns an attribute value; directly for an {@code XsdAttributesTableModel}.
     * @param index int; index of the attribute.
     * @return String; value of the attribute.
     */
    public String getAttributeValue(final int index)
    {
        assureAttributes();
        Throw.when(index < 0 || index >= this.attributeCount(), IndexOutOfBoundsException.class, "Index out of bounds.");
        return this.attributeValues.get(index);
    }

    /**
     * Returns the value of an attribute.
     * @param attribute String; name of the attribute.
     * @return String; value of the attribute.
     * @throws IllegalStateException when the node does not have the attribute.
     */
    public String getAttributeValue(final String attribute)
    {
        assureAttributes();
        for (int index = 0; index < this.attributeCount(); index++)
        {
            Node attr = this.attributeNodes.get(index);
            if (XsdSchema.getAttribute(attr, "name").equals(attribute))
            {
                return this.attributeValues.get(index);
            }
        }
        throw new IllegalStateException("Attribute " + attribute + " is not in node " + getNodeString() + ".");
    }

    // ====== Methods to expose to the GUI ======

    /**
     * Returns whether the node is active. If not, it only exists to show the user what type of node may be created here.
     * @return boolean; whether the node is active.
     */
    public boolean isActive()
    {
        return this.active;
    }

    /**
     * Sets this node to be active if it is not already. If this node is the selected node within a choice, it also activates
     * all other options of the choice. If the node is a sequence within an option, all children are activated if they are
     * themselves not a choice. If they are a choice, the selected node in the choice is activated (which itself will also
     * activate the other options).
     */
    public void setActive()
    {
        if (!this.active)
        {
            this.active = true;
            if (this.choice != null && this.choice.parentChoice != null)
            {
                for (XsdTreeNode siblingChoice : this.choice.parentChoice.selected.children)
                {
                    for (XsdTreeNode sibling : siblingChoice.options)
                    {
                        sibling.active = true;
                    }
                }
            }
            if (this.deactivated)
            {
                return; // deactivated from an active state in the past; all parts below are already in place
            }
            if (this.xsdNode.getNodeName().equals("xsd:sequence") && this.choice != null)
            {
                for (XsdTreeNode child : this.children)
                {
                    if (!child.isChoice())
                    {
                        child.setActive();
                    }
                    else
                    {
                        child.selected.setActive();
                    }
                }
            }
            else
            {
                this.children = null;
                this.attributeNodes = null;
                this.isIdentifiable = null;
                this.isEditable = null;
                assureChildren();
                assureAttributes();
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
            }
        }
    }

    /**
     * Returns whether this node has an attribute named "ID".
     * @return boolean; whether this node has an attribute named "ID".
     */
    public boolean isIdentifiable()
    {
        if (!this.active)
        {
            return false;
        }
        if (this.isIdentifiable == null)
        {
            assureAttributes();
            for (int index = 0; index < this.attributeCount(); index++)
            {
                Node node = this.attributeNodes.get(index);
                if (XsdSchema.getAttribute(node, "name").equals("ID"))
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
     * Sets the value for an attribute with name "ID".
     * @param id String; value to set.
     */
    public void setId(final String id)
    {
        Throw.when(!isIdentifiable(), IllegalStateException.class, "Setting id on non-identifiable node.");
        this.attributeValues.set(this.idIndex, id);
    }

    /**
     * Returns the value of an attribute with name "ID".
     * @return String; value of an attribute with name "ID".
     */
    public String getId()
    {
        Throw.when(!isIdentifiable(), IllegalStateException.class, "Getting id from non-identifiable node.");
        return this.attributeValues.get(this.idIndex);
    }

    /**
     * Returns whether this node is editable; i.e. whether a value can be set on the node, i.e. has a simple value, e.g.
     * &lt;NODE&gt;Simple value&lt;/NODE&gt;.
     * @return boolean; whether this node is editable, i.e. whether a value can be set on the node.
     */
    public boolean isEditable()
    {
        if (!this.active)
        {
            return false;
        }
        if (this.isEditable == null)
        {
            if (this.xsdNode.getChildNodes().getLength() == XsdSchema.getChildren(this.xsdNode, "#text").size())
            {
                // #text children only means a simple type
                this.isEditable = true;
                return true;
            }
            Node simpleType = this.xsdNode.getNodeName().equals("xsd:simpleType") ? this.xsdNode
                    : XsdSchema.getChild(this.xsdNode, "xsd:simpleType");
            if (simpleType != null)
            {
                this.isEditable = true;
                return true;
            }
            Node complexType = this.xsdNode.getNodeName().equals("xsd:complexType") ? this.xsdNode
                    : XsdSchema.getChild(this.xsdNode, "xsd:complexType");
            if (complexType != null)
            {
                Node simpleContent = XsdSchema.getChild(complexType, "xsd:simpleContent");
                this.isEditable = simpleContent != null;
            }
            else
            {
                this.isEditable = false;
            }
        }
        return this.isEditable;
    }

    /**
     * Sets the value on this node.
     * @param value String; value to set.
     */
    public void setValue(final String value)
    {
        Throw.when(!isEditable(), IllegalStateException.class,
                "Node is not an xsd:simpleType or xsd:complexType with xsd:simpleContent, hence no value is allowed.");
        this.value = value;
    }

    /**
     * Returns the value of this node.
     * @return String; value of this node.
     */
    public String getValue()
    {
        return this.value;
    }

    /**
     * Returns whether a node of the same type may be added next to this node under the parent. This is based on maxOccurs.
     * @return boolean; whether a node of the same type may be added next to this node under the parent.
     */
    public boolean isAddable()
    {
        return this.maxOccurs == -1 || (this.parent != null && siblingPositions().size() < this.maxOccurs);
    }

    /**
     * Adds a node of similar type next to this node under the parent. If the node is a choice, the same selected option will be
     * set in the added node. In this way the user sees that node option copied. This method will also add the new node to a
     * sequence under an {@code parentChoice} if it exists.
     */
    public void add()
    {
        if (this.choice != null)
        {
            int index = this.parent.children.indexOf(this) + 1;
            XsdTreeNode node = new XsdTreeNode(this.choice.parent, this.choice.xsdNode, this.choice.hiddenNodes,
                    this.choice.referringXsdNode);
            int indexSelected = this.choice.options.indexOf(this.choice.selected);
            node.choice.setOption(node.options.get(indexSelected));
            this.parent.children.remove(node.options.get(indexSelected)); // needs to be at the right index
            this.parent.children.add(index, node.options.get(indexSelected));
            node.options.get(indexSelected).setActive();
        }
        else
        {
            int index = this.parent.children.indexOf(this) + 1;
            XsdTreeNode node = new XsdTreeNode(this.parent, this.xsdNode, this.hiddenNodes, this.referringXsdNode);
            if (this.parentChoice != null)
            {
                XsdTreeNode sequence = getContainingSequence();
                int indexSequence = sequence.children.indexOf(this) + 1;
                node.parentChoice = this.parentChoice;
                sequence.children.add(indexSequence, node);
                setSequenceOrder(sequence.children);
            }
            this.parent.children.add(index, node);
            node.setActive();
        }
    }

    /**
     * Returns the sequence node that contains this node, which is an option in a {@code parentChoice}.
     * @return XsdTreeNode; sequence node that contains this node, which is an option in a {@code parentChoice}.
     */
    private XsdTreeNode getContainingSequence()
    {
        for (XsdTreeNode option : this.parentChoice.options)
        {
            if (option.xsdNode.getNodeName().equals("xsd:sequence") && option.children.contains(this))
            {
                return option;
            }
        }
        throw new IllegalStateException("Obtaining containing sequence for node not in any of its parentChoice's options.");
    }

    /**
     * Returns whether this node may be removed without violating "minOccurs" constraints.
     * @return boolean; whether this node may be removed without violating "minOccurs" constraints.
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
        if (this.choice != null)
        {
            numberOfTypeOrChoiceInParent = 0;
            for (XsdTreeNode sibling : this.parent.children)
            {
                for (XsdTreeNode option : this.choice.options)
                {
                    if (haveSameType(option, sibling))
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
        if (this.minOccurs == 0 && numberOfTypeOrChoiceInParent == 1)
        {
            this.deactivated = true;
            this.active = false;
            if (this.choice != null && this.choice.parentChoice != null)
            {
                for (XsdTreeNode siblingChoice : this.choice.parentChoice.selected.children)
                {
                    for (XsdTreeNode sibling : siblingChoice.options)
                    {
                        sibling.deactivated = true;
                        sibling.active = false;
                    }
                }
            }
            return;
        }
        if (this.ignoreRemove)
        {
            return;
        }
        this.ignoreRemove = true;
        if (this.children != null)
        {
            // copy to prevent ConcurrentModificationException as child removes itself from this node
            List<XsdTreeNode> childs = new ArrayList<>(this.children);
            for (XsdTreeNode child : childs)
            {
                child.remove();
            }
        }
        if (this.parentChoice != null)
        {
            XsdTreeNode sequence = getContainingSequence();
            sequence.children.remove(this);
            setSequenceOrder(sequence.children);
        }
        this.parent.children.remove(this);
        XsdTreeNodeRoot root = (XsdTreeNodeRoot) getPath().get(0); // can't get path later as we set parent to null
        this.parent = null;
        try
        {
            root.fireEvent(XsdTreeNodeRoot.NODE_REMOVED, this);
        }
        catch (RemoteException exception)
        {
            throw new RuntimeException("Unexpected exception when throwing NODE_REMOVED event.");
        }
    }

    /**
     * Returns whether the node can be moved up in the parent.
     * @return boolean; whether the node can be moved up in the parent.
     */
    public boolean canMoveUp()
    {
        List<Integer> positions = siblingPositions();
        return !positions.isEmpty() && this.parent.children.indexOf(this) > positions.get(0);
    }

    /**
     * Returns whether the node can be moved down in the parent.
     * @return boolean; whether the node can be moved down in the parent.
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
     * @return List&lt;Integer&gt;; list of indices within the parents child list, regarding sibling nodes of the same type.
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
                    if (haveSameType(option, this.parent.children.get(index)))
                    {
                        siblingPositions.add(index);
                    }
                }
            }
        }
        else
        {
            for (int index = 0; index < this.parent.children.size(); index++)
            {
                if (haveSameType(this, this.parent.children.get(index)))
                {
                    siblingPositions.add(index);
                }
            }
        }
        return siblingPositions;
    }

    /**
     * Returns whether nodes are of the same type. This regards the referring XSD node if it exists, otherwise it regards the
     * regular XSD node.
     * @param node1 XsdTreeNode; node 1.
     * @param node2 XsdTreeNode; node 1.
     * @return boolean; whether nodes are of the same type.
     */
    private static boolean haveSameType(final XsdTreeNode node1, final XsdTreeNode node2)
    {
        return (node1.referringXsdNode != null && node1.referringXsdNode.equals(node2.referringXsdNode))
                || (node1.referringXsdNode == null && node1.xsdNode.equals(node2.xsdNode));
    }

    /**
     * Move the node to a different position in the parent, relative to the current position.
     * @param down int; number of moves down. May be negative for up.
     */
    public void move(final int down)
    {
        if (this.parentChoice != null)
        {
            XsdTreeNode sequence = getContainingSequence();
            int index = sequence.children.indexOf(this);
            sequence.children.remove(this);
            sequence.children.add(index + down, this);
            setSequenceOrder(sequence.children);
        }
        int index = this.parent.children.indexOf(this);
        this.parent.children.remove(this);
        this.parent.children.add(index + down, this);
    }

    /**
     * Returns the minimum number of this element under the parent node, as defined in minOccurs in XSD.
     * @return int; minimum number of this element under the parent node, as defined in minOccurs in XSD.
     */
    public int minOccurs()
    {
        return this.minOccurs;
    }

    /**
     * Returns the maximum number of this element under the parent node, as defined in maxOccurs in XSD. The XSD value
     * "unbounded" results in a value of -1.
     * @return int; maximum number of this element under the parent node, as defined in maxOccurs in XSD.
     */
    public int maxOccurs()
    {
        return this.maxOccurs;
    }

    /**
     * Returns the path string of this element, e.g. "OTS.DEFINITIONS.ROADLAYOUTS". This is used to identify each unique type of
     * element.
     * @return String; path string of this element, e.g. "OTS.DEFINITIONS.ROADLAYOUTS".
     */
    public String getPathString()
    {
        return this.pathString;
    }

    /**
     * Returns the position of this node in a group as it should be visualized to the user.
     * @return GroupPosition; position of this node in a group as it should be visualized to the user.
     */
    public GroupPosition getGroupPosition()
    {
        return this.groupPosition;
    }

    /**
     * Returns whether the node, and all its children recursively, is valid. This means all required values are supplied, and
     * all supplied values comply to their respective types and constraints.
     * @return boolean; whether the node is valid.
     */
    public boolean isValid()
    {
        if (!this.active)
        {
            return true;
        }
        if (reportInvalidValue() != null)
        {
            return false;
        }
        for (int index = 0; index < attributeCount(); index++)
        {
            if (reportInvalidAttributeValue(index) != null)
            {
                return false;
            }
        }
        // TODO: check whether node should have children if there are none; can we do this without already exploding the tree?
        if (this.children != null)
        {
            for (XsdTreeNode child : this.children)
            {
                if (!child.isValid())
                {
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Returns a message why the id is invalid, or {@code null} if it is valid. This should only be used to determine a GUI
     * indication on an invalid ID. For other cases processing the attributes includes the ID.
     * @return String; message why the id is invalid, or {@code null} if it is valid.
     */
    public String reportInvalidId()
    {
        return isIdentifiable() ? ValueValidator.reportInvalidAttributeValue(getAttributeNode(this.idIndex),
                getAttributeValue(this.idIndex), this.schema) : null;
    }

    /**
     * Returns a message why the value is invalid, or {@code null} if it is valid.
     * @return String; message why the value is invalid, or {@code null} if it is valid.
     */
    public String reportInvalidValue()
    {
        return isEditable() ? ValueValidator.reportInvalidValue(this.xsdNode, this.value, this.schema) : null;
    }

    /**
     * Returns a message why the attribute value is invalid, or {@code null} if it is valid.
     * @param index int; index of the attribute.
     * @return String; message why the attribute value is invalid, or {@code null} if it is valid.
     */
    public String reportInvalidAttributeValue(final int index)
    {
        return ValueValidator.reportInvalidAttributeValue(getAttributeNode(index), getAttributeValue(index), this.schema);
    }

    // ====== Interaction with visualization ======

    /**
     * This function can be set externally and supplies an additional {@code String} to clarify this node in the tree.
     * @param stringFunction Function&lt;XsdTreeNode, String&gt; string function.
     */
    public void setStringFunction(final Function<XsdTreeNode, String> stringFunction)
    {
        this.stringFunction = stringFunction;
    }

    /**
     * A consumer can be set externally and will receive this node when its menu item is selected.
     * @param menuItem String; name of item as presented to the user.
     * @param consumer Consumer&lt;XsdTreeNode>&gt;; editor.
     */
    public void addConsumer(final String menuItem, final Consumer<XsdTreeNode> consumer)
    {
        this.consumers.put(menuItem, consumer);
    }

    /**
     * Returns whether this node has any consumers.
     * @return boolean; whether this node has any consumers.
     */
    public boolean hasConsumer()
    {
        return !this.consumers.isEmpty();
    }

    /**
     * Returns the menu items for which this node has consumers.
     * @return Set&lt;String&gt;; menu items for which this node has consumers.
     */
    public Set<String> getConsumerMenuItems()
    {
        return this.consumers.keySet();
    }

    /**
     * Triggers the node to be consumed.
     * @param menuItem String; menu item.
     */
    public void consume(final String menuItem)
    {
        Throw.when(!this.consumers.containsKey(menuItem), IllegalArgumentException.class, "Unable to consume node for %s.",
                menuItem);
        this.consumers.get(menuItem).accept(this);
    }

    /**
     * Returns a string that is the name of the node, without any additional information on id and additional string function.
     * @return String; string that is the name of the node.
     */
    public String getShortString()
    {
        if (this.options != null)
        {
            // this name may appear as part of a sequence which is an option for an xsd:choice
            return this.options.toString().toLowerCase();
        }
        if (this.xsdNode.getNodeName().equals("xsd:sequence"))
        {
            // this name may appear as an option for an xsd:choice
            StringBuilder stringBuilder = new StringBuilder();
            String annotation = getAnnotation("xsd:appinfo", "name");
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
                for (XsdTreeNode child : this.children)
                {
                    if (!coveredTypes.contains(child.getPathString()))
                    {
                        stringBuilder.append(separator).append(child.getShortString());
                        separator = " | ";
                        coveredTypes.add(child.getPathString());
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
            return "include";
        }
        return getNodeString().toLowerCase();
    }

    /**
     * Returns the short string, appended with any additional information on id and additional string function.
     * @return String; short string, appended with any additional information on id and additional string function.
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

    /**
     * Returns an annotation value. These are defined as below, for either xsd:appinfo or xsd:documentation.
     * 
     * <pre>
     * &lt;xsd:sequence&gt;
     *   &lt;xsd:annotation&gt;
     *     &lt;xsd:appinfo source="name"&gt;annotates the sequence&lt;/xsd:appinfo&gt;
     *   &lt;/xsd:annotation&gt;
     * &lt;/xsd:sequence&gt;
     * </pre>
     * 
     * @param element String; either "xsd:documentation" or "xsd:appinfo".
     * @param source String; name that the source attribute of the annotation should have.
     * @return String; annotation value, {@code null} if not found.
     */
    private String getAnnotation(final String element, final String source)
    {
        Node relevantNode = this.referringXsdNode == null ? this.xsdNode : this.referringXsdNode;
        for (Node child : XsdSchema.getChildren(relevantNode, "xsd:annotation"))
        {
            for (Node annotation : XsdSchema.getChildren(child, element))
            {
                String appInfoSource = XsdSchema.getAttribute(annotation, "source");
                if (appInfoSource != null && appInfoSource.equals(source))
                {
                    StringBuilder str = new StringBuilder();
                    for (int appIndex = 0; appIndex < annotation.getChildNodes().getLength(); appIndex++)
                    {
                        Node appInfo = annotation.getChildNodes().item(appIndex);
                        if (appInfo.getNodeName().equals("#text"))
                        {
                            str.append(appInfo.getNodeValue());
                        }
                    }
                    // tabs, line break, etc. to blanks, then remove consecutive blanks, then trailing/leading blanks
                    return str.toString().replaceAll("\\s", " ").replaceAll("\\s{2,}", " ").trim();
                }
            }
        }
        return null;
    }

    /**
     * Location of a node in a group of nodes that should be visualized as a group to the user.
     * @author wjschakel
     */
    public enum GroupPosition
    {
        /** First in a group of at least 2. */
        FIRST,

        /** Somewhere not at the end-points of a group of at least 3. */
        MIDDLE,

        /** Last in a group of at least 2. */
        LAST,

        /** Only member in a group. */
        SOLO;
    }

}
