package org.opentrafficsim.core.network;

import java.io.Serializable;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * A Network consists of a set of links. Each link has, in its turn, a start node and an end node. In addition, a network can be
 * an (expanded) node as well. An example is shown below:
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
 * Node O in the example is expanded into the subnetwork consisting of nodes A, B, C, D, and E, and links AB, AC, AD, BC, CD,
 * BE, CE, and DE. It also means that when node expansion takes place, the links to node O have to be replaced. In the example
 * below:
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
 * the node XO is replaced by XA, YO is replaced by YB, OZ is replaced by DZ, and OW is replaced by EW in the network. The
 * reverse takes place when we do node collapse.
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
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
            throw new NetworkException("Adding Node " + addThis.getId().toString() + ". This Node is  already in the Set");
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
     * @throws NetworkException
     */
    public boolean deleteNode(AbstractNode<?, ?> deleteThis) throws NetworkException
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
    public boolean expandNode(AbstractNode<?, ?> node) throws NetworkException
    {
        if (expansionOfNode == null)
        {
            throw new NetworkException("This Node" + node.getId().toString() + " is not able to expand.");
        }
        else
        {
             
            
            
        }
    }

    public boolean collapseToNode(HashSet<AbstractNodes> nodeSet)
    {
       Iterator<L> it = l.iterator();
       Set<L> neighbours = new HashSet<L> ();
       Set<AbstractNode> neighbourNodes= new HashSet<AbstractNode>();  
       while(it.hasNext()){
           AbstractLink link =it.next();
           AbstractNode<?, ?> node1 = link.getEndNode();
           AbstractNode<?, ?> node2 = link.getStartNode();
           boolean n1 = network.isInNetwork(node1);
           boolean n2 = network.isInNetwork(node2);
           
           if (!n1 || !n2  ){
               neighbours.add(link);
               if(!n1){
                   // neighbourNodes.add(node1);
                   network.addNode(node2);
               }
               else{
                   // neighbourNodes.add(node2);
                       network.addNode(node1);
                   }
                       
               }
           }
      
       
       
       
    }
        
 */
     

} // End of class
