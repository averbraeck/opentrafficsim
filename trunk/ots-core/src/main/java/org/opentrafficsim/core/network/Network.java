package org.opentrafficsim.core.network;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

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
 * <p />
 * Copyright (c) 2002-2014 Delft University of Technology, Jaffalaan 5, 2628 BX Delft, the Netherlands. All rights
 * reserved.
 * <p />
 * See for project information <a href="http://www.simulation.tudelft.nl/"> www.simulation.tudelft.nl</a>.
 * <p>
 * The OpenTrafficSim project is distributed under the following BSD-style license:<br>
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
 * following conditions are met:
 * <ul>
 * <li>Redistributions of source code must retain the above copyright notice, this list of conditions and the following
 * disclaimer.</li>
 * <li>Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the
 * following disclaimer in the documentation and/or other materials provided with the distribution.</li>
 * <li>Neither the name of Delft University of Technology, nor the names of its contributors may be used to endorse or
 * promote products derived from this software without specific prior written permission.</li>
 * </ul>
 * This software is provided by the copyright holders and contributors "as is" and any express or implied warranties,
 * including, but not limited to, the implied warranties of merchantability and fitness for a particular purpose are
 * disclaimed. In no event shall the copyright holder or contributors be liable for any direct, indirect, incidental,
 * special, exemplary, or consequential damages (including, but not limited to, procurement of substitute goods or
 * services; loss of use, data, or profits; or business interruption) however caused and on any theory of liability,
 * whether in contract, strict liability, or tort (including negligence or otherwise) arising in any way out of the use
 * of this software, even if advised of the possibility of such damage.
 * @version Aug 19, 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 * @author <a href="http://www.citg.tudelft.nl">Guus Tamminga</a>
 * @param <ID> the ID type of the network.
 * @param <L>
 */
public class Network<ID, L extends Link<?>> extends HashSet<L> implements Serializable
{
    /** */
    private static final long serialVersionUID = 20140822L;

    /** network id */
    private final ID id;

    /** Node of which this network is an expansion */
    private Node<?> expansionOfNode = null;

    /** HashSet of Nodes */
    private Set<Node<?>> nodeSet = new HashSet<Node<?>>();

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
    public Network(final ID id, final Collection<? extends L> collection, final Node<?> expansionNode)
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
    public Node<?> getExpansionOfNode()
    {
        return this.expansionOfNode;
    }

    /**
     * @param expansionOfNode set expansionOfNode
     * @throws NetworkException when expansion node is part of the node collection.
     */
    public void setExpansionOfNode(Node<?> expansionOfNode) throws NetworkException
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
    public Set<Node<?>> getNodeSet()
    {
        return this.nodeSet;
    }

    /**
     * @param nodeSet set nodeSet
     */
    public void setNodeSet(Set<Node<?>> nodeSet)
    {
        this.nodeSet = nodeSet;
    }

    /**
     * @param node
     * @return true or false
     */
    public boolean isInNetwork(Node<?> node)
    {

        if (this.nodeSet.contains(node))
        {
            return true;
        }
        else
        {
            for (Node<?> n : this.nodeSet)
            {
                if (n instanceof ExpansionNode)
                {
                    if (((ExpansionNode<?>) n).getNetwork().isInNetwork(node))
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
    public boolean addNode(Node<?> addThis) throws NetworkException
    {
        if (isInNetwork(addThis))
        {
            throw new NetworkException("Adding Node " + addThis.getId().toString() + ". Node "
                    + addThis.getNodeName().toString() + " is  already in the Set");
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
    public boolean isInNetworkLevel(Node<?> node)
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
    public Network<?, ?> getSubNetworkConsistNode(Node<?> node) throws NetworkException
    {
        if (isInNetwork(node))
        {
            if (isInNetworkLevel(node))
            {
                return this;
            }
            else
            {
                for (Node<?> n : this.nodeSet)
                {
                    if (n instanceof ExpansionNode)
                    {
                        if (((ExpansionNode<?>) n).getNetwork().isInNetworkLevel(node))
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
     * @throws NetworkException
     */
    public boolean deleteNode(Node<?> deleteThis) throws NetworkException
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
     * public boolean expandNode(Node<?> node) throws NetworkException{ if (expansionOfNode == null){ throw new
     * NetworkException("This Node"+node.getNodeID().toString()+"is not able to expand.") } else{ } } public boolean
     * collapseToNode(Network<?,?> node){ }
     */

} // End of class
