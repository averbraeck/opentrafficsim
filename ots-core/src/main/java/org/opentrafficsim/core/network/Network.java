package org.opentrafficsim.core.network;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
            throw new NetworkException("Creating Network " + this.id + " with initial collection. Expansion node "
                    + expansionNode.toString() + " is part of the initial collection");
        this.expansionOfNode = expansionNode;
    }

    /**
     * @return expansionOfNode
     */
    public AbstractNode<?, ?> getExpansionOfNode()
    {
        return this.expansionOfNode;
    }

    /**
     * @param expansionOfNode set expansionOfNode
     * @throws NetworkException when expansion node is part of the node collection.
     */
    public void setExpansionOfNode(AbstractNode<?, ?> expansionOfNode) throws NetworkException
    {
        this.expansionOfNode = expansionOfNode;
    }

    /**
     * @return id
     */
    public ID getId()
    {
        return this.id;
    }

    /**
     * @return nodeSet
     */
    public Set<AbstractNode<?, ?>> getNodeSet()
    {
        return this.nodeSet;
    }

    /**
     * @param nodeSet set nodeSet
     */
    public void setNodeSet(Set<AbstractNode<?, ?>> nodeSet)
    {
        this.nodeSet = nodeSet;
    }

    /**
     * @param node
     * @return true or false
     */
    public boolean isInNetwork(AbstractNode<?, ?> node)
    {

        if (this.nodeSet.contains(node))
        {
            return true;
        }
        else
        {
            for (AbstractNode<?, ?> n : this.nodeSet)
            {
                if (n instanceof AbstractExpansionNode)
                {
                    if (((AbstractExpansionNode<?, ?>) n).getNetwork().isInNetwork(node))
                    {
                        return true;
                    }
                }
            }
        }

        return false;
    }

    /**
     * @param addThis
     * @return true or false
     * @throws NetworkException
     */
    public boolean addNode(AbstractNode<?, ?> addThis) throws NetworkException
    {
        if (isInNetwork(addThis))
        {
            throw new NetworkException("Adding Node " + addThis.getId().toString()
                    + ". This Node is  already in the Set");
        }
        else
        {
            this.nodeSet.add(addThis);
            return true;
        }

    }

    /**
     * @param node
     * @return boolean
     */
    public boolean isInNetworkLevel(AbstractNode<?, ?> node)
    {
        if (this.nodeSet.contains(node))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * @param node
     * @return network
     * @throws NetworkException
     */
    public Network<?, ?> getSubNetworkConsistNode(AbstractNode<?, ?> node) throws NetworkException
    {
        if (isInNetwork(node))
        {
            if (isInNetworkLevel(node))
            {
                return this;
            }
            else
            {
                for (AbstractNode<?, ?> n : this.nodeSet)
                {
                    if (n instanceof AbstractExpansionNode)
                    {
                        if (((AbstractExpansionNode<?, ?>) n).getNetwork().isInNetworkLevel(node))
                        {
                            return getSubNetworkConsistNode(node);
                        }
                    }
                }
            }
        }
        else
        {
            throw new NetworkException("The network does not contain the Node" + node.getId().toString() + ".");
        }
        return null;
    }

    /**
     * @param deleteThis
     * @return boolean
     * @throws NetworkException on network inconsistency
     */
    public boolean deleteNode(final AbstractNode<?, ?> deleteThis) throws NetworkException
    {
        if (isInNetwork(deleteThis))
        {
            if (isInNetworkLevel(deleteThis))
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
        else
        {
            throw new NetworkException("Deleting" + deleteThis.getId().toString() + "is failed. Possible cause:"
                    + " node is not a member of the given Network");
        }
    }

    /*
     * public boolean expandNode(AbstractNode<?, ?> node) throws NetworkException { if (expansionOfNode == null) { throw
     * new NetworkException("This Node" + node.getId().toString() + " is not able to expand."); } else { } }
     */
    public boolean collapseToNode(final HashSet<AbstractNode<?, ?>> nodeSet)
    {
        AbstractLink<?, AbstractNode<?, ?>>[] setOfLinks = (AbstractLink<?, AbstractNode<?, ?>>[]) super.toArray();
        Set<L> insideLinks = new HashSet<L>();
        Set<L> neighbourLinks = new HashSet<L>();

        for (AbstractNode<?, ?> node : nodeSet)
        {
            for (AbstractLink<?, AbstractNode<?, ?>> link : setOfLinks)
            {
                if ((node.equals(link.getStartNode()) && node.equals(link.getEndNode())))
                {

                    insideLinks.add((L) link);
                    // Subnetwork add link
                    super.remove(link);
                    this.nodeSet.remove(node);

                }
                else if ((node.equals(link.getStartNode()) || node.equals(link.getEndNode())))
                {
                    neighbourLinks.add((L) link);

                    super.remove(link);
                    this.nodeSet.remove(node);

                }
                else
                {
                    ;
                }

            }
        }

        // add nodes to subnetwork
        // add links to subnetwork
        // constructor call newnode with new links

        return true;
    }

    /**
     * @param node1
     * @param node2
     * @return true if successful, false if it is not possible to collapse nodes
     */
    public boolean collapseLinks(final AbstractNode<?, ?> node1, final AbstractNode<?, ?> node2)
    {
        AbstractLink<?, AbstractNode<?, ?>>[] setOfLinks = (AbstractLink<?, AbstractNode<?, ?>>[]) super.toArray();
        float sumCapacityFrom1 = 0.0f; // One direction
        float sumCapacityFrom2 = 0.0f; // Other direction
        DoubleScalar<LengthUnit> shortestLengthFrom1 =
                new DoubleScalar.Abs<LengthUnit>(Double.MAX_VALUE, LengthUnit.METER);
        DoubleScalar<LengthUnit> shortestLengthFrom2 =
                new DoubleScalar.Abs<LengthUnit>(Double.MAX_VALUE, LengthUnit.METER);
        final ID idNewFrom1;
        final ID idNewFrom2;

        for (AbstractLink<?, AbstractNode<?, ?>> link : setOfLinks)
        {
            if ((node1.equals(link.getStartNode()) && node2.equals(link.getEndNode())))
            {
                super.remove(link);

                sumCapacityFrom1 += link.getCapacity().floatValue();

                if (shortestLengthFrom1.floatValue() > link.getLenght().floatValue())
                {

                    shortestLengthFrom1 = link.getLenght();
                }

            }
            else if ((node2.equals(link.getStartNode()) && node1.equals(link.getEndNode())))
            {
                super.remove(link);

                sumCapacityFrom2 += link.getCapacity().floatValue();

                if (shortestLengthFrom2.floatValue() > link.getLenght().floatValue())
                {

                    shortestLengthFrom2 = link.getLenght();

                }
            }
            else
            {
                return false;
            }
        }

        /*
         * Link<?, ?> newLinkFrom1= Link(idNewFrom1,node1,node2,sumCapacityFrom1); Link<?, ?> newLinkFrom2=; // TODO:
         * Contructor call, ask AV. newLinkFrom1.setCapacity(new DoubleScalar.Abs<FrequencyUnit>(sumCapacityFrom1,
         * FrequencyUnit.PER_SECOND)); newLinkFrom2.setCapacity(new DoubleScalar.Abs<FrequencyUnit>(sumCapacityFrom2,
         * FrequencyUnit.PER_SECOND)); super.add((L) newLinkFrom1); super.add((L) newLinkFrom2);
         */
        return true;
    }

    /**
     * @param hierarchyLevel int;
     * @return Set&lt;L&gt;
     * @throws NetworkException on network inconsistency
     */
    public Set<L> findLinkHierarchyBelow(final int hierarchyLevel) throws NetworkException
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

    /**
     * @param hierarchyLevel int
     * @return Set&lt;L&gt;
     * @throws NetworkException on network inconsistency
     */
    public Set<L> findLinkHierarchyEqualOrBelow(final int hierarchyLevel) throws NetworkException
    {
        AbstractLink<?, AbstractNode<?, ?>>[] setOfLinks = (AbstractLink<?, AbstractNode<?, ?>>[]) super.toArray();
        Set<L> linksAboveLevel = new HashSet<L>();

        for (AbstractLink<?, AbstractNode<?, ?>> link : setOfLinks)
        {
            if (link.getHierarchy() >= hierarchyLevel)
            {
                linksAboveLevel.add((L) link);
            }
        }

        return linksAboveLevel;

    }

} // End of class
