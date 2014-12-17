package org.opentrafficsim.core.network;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.opentrafficsim.core.unit.FrequencyUnit;
import org.opentrafficsim.core.unit.LengthUnit;
import org.opentrafficsim.core.value.vdouble.scalar.DoubleScalar;

/**
 * A Network consists of a set of links. Each link has, in its turn, a start node and an end node. In addition, a
 * network can be an (expanded) node as well. An example is shown below:
 * 
 * <pre>
 *            |
 *     -------O--------
 *            |
 * </pre>
 * 
 * can be expanded into:
 * 
 * <pre>
 *            |
 *            A
 *           /|\
 *          / | \
 *    -----B--C--D-----
 *          \ | /
 *           \|/
 *            E
 *            |
 * </pre>
 * 
 * Node O in the example is expanded into the subnetwork consisting of nodes A, B, C, D, and E, and links AB, AC, AD,
 * BC, CD, BE, CE, and DE. It also means that when node expansion takes place, the links to node O have to be replaced.
 * In the example below:
 * 
 * <pre>
 *            X
 *            |
 *     Y------O-------Z
 *            |
 *            W
 * </pre>
 * 
 * can be expanded into:
 * 
 * <pre>
 *            X
 *            |
 *            A
 *           /|\
 *          / | \
 *    Y----B--C--D----Z
 *          \ | /
 *           \|/
 *            E
 *            |
 *            W
 * </pre>
 * 
 * the node XO is replaced by XA, YO is replaced by YB, OZ is replaced by DZ, and OW is replaced by EW in the network.
 * The reverse takes place when we do node collapse.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version Aug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @param <ID> the ID type of the network.
 * @param <L>
 */
public class Network<ID, L extends AbstractLink<?, ?>> extends HashSet<L> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20140822L;

    /** network id. */
    private final ID id;

    /** Node of which this network is an expansion. */
    private AbstractNode<?, ?> expansionOfNode = null;

    /** HashSet of Nodes. */
    private Set<AbstractNode<?, ?>> nodeSet = new HashSet<>();

    /**
     * Construction of an empty network.
     * @param id the network id.
     */
    public Network(final ID id)
    {
        super();
        this.id = id;
    }

    /**
     * Construction of a network with an initial set of links.
     * @param id the network id.
     * @param collection the initial collection of links.
     */
    public Network(final ID id, final Collection<? extends L> collection)
    {
        super(collection);
        this.id = id;
    }

    /**
     * Construction of a network with an initial set of links, and an expansion node.
     * @param id the network id.
     * @param collection the initial collection of links.
     * @param expansionNode Node of which this network is an expansion.
     * @throws NetworkException when expansion node is part of the initial collection.
     */
    public Network(final ID id, final Collection<? extends L> collection, final AbstractNode<?, ?> expansionNode)
            throws NetworkException
    {
        super(collection);
        this.id = id;
        if (collection.contains(expansionNode))
        {
            throw new NetworkException("Creating Network " + this.id + " with initial collection. Expansion node "
                    + expansionNode.toString() + " is part of the initial collection");
        }
        this.expansionOfNode = expansionNode;
    }

    /**
     * @return expansionOfNode
     */
    public final AbstractNode<?, ?> getExpansionOfNode()
    {
        return this.expansionOfNode;
    }

    /**
     * @param expansionOfNode set expansionOfNode
     * @throws NetworkException when expansion node is part of the node collection.
     */
    public final void setExpansionOfNode(final AbstractNode<?, ?> expansionOfNode) throws NetworkException
    {
        this.expansionOfNode = expansionOfNode;
    }

    /**
     * @return id
     */
    public final ID getId()
    {
        return this.id;
    }

    /**
     * @return nodeSet
     */
    public final Set<AbstractNode<?, ?>> getNodeSet()
    {
        return this.nodeSet;
    }

    /**
     * @param nodeSet set nodeSet
     */
    public final void setNodeSet(final Set<AbstractNode<?, ?>> nodeSet)
    {
        this.nodeSet = nodeSet;
    }

    /**
     * Determine if a node is part of this Network.
     * @param node AbstractNode&lt;?, ?&gt;; the node
     * @param recurse boolean; if true also search sub-networks
     * @return true or false
     */
    public final boolean isInNetwork(final AbstractNode<?, ?> node, final boolean recurse)
    {
        if (this.nodeSet.contains(node))
        {
            return true;
        }
        else if (recurse)
        {
            for (AbstractNode<?, ?> n : this.nodeSet)
            {
                if (n instanceof AbstractExpansionNode
                        && ((AbstractExpansionNode<?, ?>) n).getNetwork().isInNetwork(node, true))
                {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Add a node to this Network.
     * @param node Node; the node that must be added
     * @throws NetworkException if the node is already part of this network
     */
    public final void addNode(final AbstractNode<?, ?> node) throws NetworkException
    {
        if (isInNetwork(node, true))
        {
            throw new NetworkException("Adding Node " + node.getId().toString() + ". This Node is  already in the Set");
        }
        else
        {
            this.nodeSet.add(node);
        }
    }

    /**
     * Return the sub network that directly owns a specified node.
     * @param node AbstractNode&lt;?, ?&gt;; the node
     * @return Network
     * @throws NetworkException if the specified node is not contained in this Network or any of its sub Networks
     */
    public final Network<?, ?> getSubNetworkConsistNode(final AbstractNode<?, ?> node) throws NetworkException
    {
        if (isInNetwork(node, true))
        {
            // FIXME going through the tree once more is inefficient
            if (isInNetwork(node, false))
            {
                return this;
            }
            else
            {
                for (AbstractNode<?, ?> n : this.nodeSet)
                {
                    if (n instanceof AbstractExpansionNode
                            && ((AbstractExpansionNode<?, ?>) n).getNetwork().isInNetwork(node, false))
                    {
                        return getSubNetworkConsistNode(node);
                    }
                }
            }
        }
        throw new NetworkException("The network does not contain the Node" + node.getId().toString() + ".");
    }

    /**
     * Delete a node from this network (or a sub-network of this network).
     * @param deleteThis AbstractNode&lt;?, ?&gt;; the node that must be deleted
     * @return boolean
     * @throws NetworkException on network inconsistency
     */
    public final boolean deleteNode(final AbstractNode<?, ?> deleteThis) throws NetworkException
    {
        // TODO ensure that no links are orphaned due to removal of the node
        if (isInNetwork(deleteThis, true))
        {
            // FIXME inefficient (searches once more)
            if (isInNetwork(deleteThis, false))
            {
                this.nodeSet.remove(deleteThis);
                return true;
            }
            else
            {
                Network<?, ?> n = getSubNetworkConsistNode(deleteThis);
                n.remove(deleteThis);
                return true;
            }
        }
        throw new NetworkException("Deleting" + deleteThis.getId().toString() + "is failed. Possible cause:"
                + " node is not a member of the given Network");
    }

    /**
     * Collapse a nodes into a sub network.
     * @param nodesOfSubNetwork HashSet&lt;AbstractNode&lt;?, ?&gt;&gt;; the nodes that go into the new sub network
     * @return boolean; Currently always true (because some checks have not been implemented...)
     */
    public final boolean collapseToNode(final HashSet<AbstractNode<?, ?>> nodesOfSubNetwork)
    {
        AbstractLink<?, AbstractNode<?, ?>>[] setOfLinks = (AbstractLink<?, AbstractNode<?, ?>>[]) super.toArray();
        Set<L> insideLinks = new HashSet<L>();
        Set<L> neighbourLinks = new HashSet<L>();

        for (AbstractNode<?, ?> node : nodesOfSubNetwork)
        {
            for (AbstractLink<?, AbstractNode<?, ?>> link : setOfLinks)
            {
                if (nodesOfSubNetwork.contains(link.getStartNode()) && nodesOfSubNetwork.contains(link.getEndNode()))
                // wrong: if (node.equals(link.getStartNode()) && node.equals(link.getEndNode()))
                {
                    // This link is internal to the collapsing area
                    insideLinks.add((L) link);
                    // Subnetwork add link
                    super.remove(link);
                    // Still wrong; attempts to remove the link N times and adds it N times.
                    this.nodeSet.remove(node);
                }
                else if (node.equals(link.getStartNode()) || node.equals(link.getEndNode()))
                {
                    // Link connects an internal node to an external node
                    neighbourLinks.add((L) link);

                    super.remove(link);
                    this.nodeSet.remove(node);
                }
                // else This link is not part of the collapsed area;
            }
        }

        // add nodes to subnetwork
        // add links to subnetwork
        // constructor call new node with new links
        // TODO add replacement links for each outgoing or incoming link (these are listed in neighborLinks).
        // TODO create a Node that owns the sub network (includes figuring out the location for it)

        return true;
    }

    /**
     * Collapse all links between two given nodes into one forward and one reverse link.
     * @param node1 Node; the first node
     * @param node2 Node; the second node
     * @return true if successful, false there are no links between the nodes
     */
    public final boolean collapseLinks(final AbstractNode<?, ?> node1, final AbstractNode<?, ?> node2)
    {
        AbstractLink<?, AbstractNode<?, ?>>[] setOfLinks = (AbstractLink<?, AbstractNode<?, ?>>[]) super.toArray();
        float forwardCapacity = 0.0f; // One direction
        float reverseCapacity = 0.0f; // Other direction
        DoubleScalar.Rel<LengthUnit> shortestLengthFrom1 =
                new DoubleScalar.Rel<LengthUnit>(Double.MAX_VALUE, LengthUnit.METER);
        DoubleScalar.Rel<LengthUnit> shortestLengthFrom2 =
                new DoubleScalar.Rel<LengthUnit>(Double.MAX_VALUE, LengthUnit.METER);

        int forwardLinksFound = 0;
        int reverseLinksFound = 0;
        for (AbstractLink<?, AbstractNode<?, ?>> link : setOfLinks)
        {
            if (node1.equals(link.getStartNode()) && node2.equals(link.getEndNode()))
            {
                super.remove(link);

                forwardCapacity += link.getCapacity().floatValue();

                if (shortestLengthFrom1.floatValue() > link.getLength().floatValue())
                {
                    shortestLengthFrom1 = link.getLength();
                }
                forwardLinksFound++;
            }
            else if (node2.equals(link.getStartNode()) && node1.equals(link.getEndNode()))
            {
                super.remove(link);

                reverseCapacity += link.getCapacity().floatValue();

                if (shortestLengthFrom2.floatValue() > link.getLength().floatValue())
                {
                    shortestLengthFrom2 = link.getLength();
                }
                reverseLinksFound++;
            }
        }
        if (0 == forwardLinksFound && 0 == reverseLinksFound)
        {
            return false;
        }

        if (forwardLinksFound > 0)
        {
            Link<?, ?> newLinkFrom1 = new Link(node1.getId(), node1, node2, shortestLengthFrom1);
            newLinkFrom1.setCapacity(new DoubleScalar.Abs<FrequencyUnit>(forwardCapacity, FrequencyUnit.PER_SECOND));
            super.add((L) newLinkFrom1);
        }
        if (reverseLinksFound > 0)
        {
            Link<?, ?> newLinkFrom2 = new Link(node2.getId(), node2, node1, shortestLengthFrom2);
            newLinkFrom2.setCapacity(new DoubleScalar.Abs<FrequencyUnit>(reverseCapacity, FrequencyUnit.PER_SECOND));
            super.add((L) newLinkFrom2);
        }
        return true;
    }

    /**
     * Find all links that have a hierarchy level not exceeding the specified value. <br>
     * Highest hierarchy value is 0; 1 is next lower level, etc.
     * @param hierarchyLevel int; the maximum hierarchy level of the returned links
     * @return Set&lt;L&gt;
     * @throws NetworkException on network inconsistency
     */
    public final Set<L> findLinkHierarchyBelow(final int hierarchyLevel) throws NetworkException
    {
        AbstractLink<?, AbstractNode<?, ?>>[] setOfLinks = (AbstractLink<?, AbstractNode<?, ?>>[]) super.toArray();
        Set<L> linksAboveLevel = new HashSet<L>();

        for (AbstractLink<?, AbstractNode<?, ?>> link : setOfLinks)
        {
            if (link.getHierarchy() > hierarchyLevel)
            {
                linksAboveLevel.add((L) link);
            }
        }
        return linksAboveLevel;
    }

}
