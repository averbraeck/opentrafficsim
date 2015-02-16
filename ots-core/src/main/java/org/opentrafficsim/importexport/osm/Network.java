package org.opentrafficsim.importexport.osm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import com.vividsolutions.jts.geom.Coordinate;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 31 dec. 2014 <br>
 * @author <a>Moritz Bergmann</a>
 */
public class Network
{
    /** The Nodes of the Network. */
    private HashMap<Long, Node> nodes;

    /** The Ways of the Network. */
    private HashMap<Long, Way> ways;

    /** The Relations of the Network. */
    private HashMap<Long, Relation> relations;

    /** The name of the Network. */
    private String name;

    /** The Links of the Network. */
    private List<Link> links;

    /**
     * Construct a new Network.
     * @param name String; the name of the new Network
     */
    public Network(final String name)
    {
        this.setName(name);
        this.nodes = new HashMap<Long, Node>();
        this.ways = new HashMap<Long, Way>();
        this.relations = new HashMap<Long, Relation>();
        this.links = new ArrayList<Link>();
    }
    
    /**
     * Construct a new Network out of another Network.
     * @param net 
     */
    public Network(final Network net)
    {
        this.setName(net.getName());
        this.nodes = new HashMap<Long, Node>(net.getNodes());
        this.ways = new HashMap<Long, Way>(net.getWays());
        this.relations = new HashMap<Long, Relation>(net.getRelations());
        this.links = new ArrayList<Link>(net.getLinks());
    }

    /**
     * Retrieve a list of Nodes that form a Way from this Network.
     * @param wayid long; the id of the Way
     * @return List&lt;Long*gt;; the list of NodeIds of way with the specified id
     * @throws IOException when no Way with the specified id exists in this Network
     */
    public final List<Long> getNodesFromWay(final Long wayid) throws IOException
    {
        if (this.ways.get(wayid) == null)
        {
            throw new IOException("Way with the ID: " + wayid + "was not found");
        }
        else
        {
            return this.ways.get(wayid).getNodes();
        }
    }

    /**
     * Retrieve a Node from this Network.
     * @param nodeid long; the id of the Node
     * @return node Node; the node with the specified id
     * @throws IOException when no Node with the specified id exist in this Network
     */
    public final Node getNode(final long nodeid) throws IOException
    {
        if (this.nodes.get(nodeid) == null)
        {
            throw new IOException("Node with the ID: " + nodeid + "was not found");
        }
        else
        {
            return this.nodes.get(nodeid);
        }
    }

    /**
     * @return HashMap of all Nodes in this Network
     */
    public final HashMap<Long, Node> getNodes()
    {
        return this.nodes;
    }

    /**
     * Retrieve a Relation from this Network.
     * @param relid long; the id of the Relation
     * @return Relation
     * @throws IOException when no Relation with the specified id exists in this Network
     */
    public final Relation getRelation(final long relid) throws IOException
    {
        if (this.relations.get(relid) == null)
        {
            throw new IOException("Relation with the ID: " + relid + "was not found");
        }
        else
        {
            return this.relations.get(relid);
        }
    }

    /**
     * @return HashMap of all relations in the network
     */
    public final HashMap<Long, Relation> getRelations()
    {
        return this.relations;
    }

    /**
     * Retrieve a Way from this Network.
     * @param wayid long; the id of a Way
     * @return Way
     * @throws IOException when no Way with the specified id exist in this network
     */
    public final Way getWay(final long wayid) throws IOException
    {
        if (this.ways.get(wayid) == null)
        {
            throw new IOException("Way with the ID: " + wayid + "was not found");
        }
        else
        {
            return this.ways.get(wayid);
        }
    }

    /**
     * @return name
     */
    public final String getName()
    {
        return this.name;
    }

    /**
     * Set/update the name of this Network.
     * @param name String; the new name
     */
    public final void setName(final String name)
    {
        this.name = name;
    }

    /**
     * Set/replace the Nodes of this Network.<br>
     * The provided list is <b>not copied</b>; the caller should not modify the list after setting it.
     * @param newnodes HashMap&lt;Long, Node&gt;; the (new) Nodes for this Network
     */
    public final void setNodes(final HashMap<Long, Node> newnodes)
    {
        this.nodes = newnodes;
    }

    /**
     * Add a Node to this Network.
     * @param node Node; the node to add to this Network
     */
    public final void addNode(final Node node)
    {
        this.nodes.put(node.getID(), node);
    }

    /**
     * Delete a Node from this Network.
     * @param node Node; the Node to delete
     */
    public final void delNode(final Node node)
    {
        this.nodes.remove(node.getID());
    }

    /**
     * Delete a Node from this Network.
     * @param nodeid Long; the id of the Node to delete
     */
    public final void delNode(final Long nodeid)
    {
        this.nodes.remove(nodeid);
    }

    /**
     * Add a Way to this Network.
     * @param way Way; the Way to add
     */
    public final void addWay(final Way way)
    {
        this.ways.put(way.getID(), way);
    }

    /**
     * Add a Relation to this Network.
     * @param rel Relation; the relation to add
     */
    public final void addRelation(final Relation rel)
    {
        this.relations.put(rel.getID(), rel);
    }

    /**
     * @return ways
     */
    public final HashMap<Long, Way> getWays()
    {
        return this.ways;
    }

    /**
     * @return links
     */
    public final List<Link> getLinks()
    {
        return this.links;
    }

    /**
     * Creates links out of the ways in this network.
     * @throws IOException on read errors
     */
    public final void makeLinks() throws IOException
    {
        List<Link> links2 = new ArrayList<Link>();
        double length;
        Node n1;
        Node n2;
        double x1;
        double x2;
        double y1;
        double y2;
        for (Long wayid : this.ways.keySet())
        {
            List<Long> waynodes = this.getWay(wayid).getNodes();
            for (int i = 0; i < (waynodes.size() - 1); i++)
            {
                n1 = this.getNode(waynodes.get(i).longValue());
                n2 = this.getNode(waynodes.get(i + 1).longValue());
                // Workaround for bug in OSM near Mettmann Germany
                if (n1 == n2)
                {
                    continue;
                }
                x1 = n1.getLatitude();
                x2 = n2.getLatitude();
                y1 = n1.getLongitude();
                y2 = n2.getLongitude();
                length = distanceLongLat(x1, y1, x2, y2);
                Link e = new Link(n1, n2, this.getWay(wayid).getTags(), length);
                links2.add(e);
            }
        }
        this.links = links2;
    }

    /**
     * @param xx1 Latitude of point 1
     * @param yy1 Longitude of point 1
     * @param xx2 Latitude of point 2
     * @param yy2 Longitude of point 2
     * @return distance in meter between the (x1,y1) and (x2,y2) This method utilizes great circle calculation
     */
    private static double distanceLongLat(final double xx1, final double yy1, final double xx2, final double yy2)
    {

        double y1 = yy1;
        double y2 = yy2;
        if (y1 < 0)
        {
            y1 = 360 + yy1;
        }
        if (y2 < 0)
        {
            y2 = 360 + yy2;
        }
        double x1 = 90 - xx1;
        double x2 = 90 - xx2;
        x1 = x1 * (2 * Math.PI / 360);
        x2 = x2 * (2 * Math.PI / 360);
        y1 = y1 * (2 * Math.PI / 360);
        y2 = y2 * (2 * Math.PI / 360);
        double distance;
        distance =
                Math.acos(Math.sin(x1) * Math.sin(x2) * Math.cos(y1 - y2) + Math.cos(x1) * Math.cos(x2)) * 6371 * 1000;
        return distance;
    }

    /**
     * This function checks for and removes redundancies between the networks links.
     */
    public final void removeRedundancy()
    {
        boolean again = false;
        do
        {
            again = this.redundancyCheck();
        }
        while (again);
    }

    /**
     * This function checks the networks links for redundancy.
     * @return boolean; true if the Network was modified (further reduction may be possible by calling this method
     *         again)
     */
    private boolean redundancyCheck()
    {
        List<Link> checkedLinks = new ArrayList<Link>();
        List<Link> removedLinks = new ArrayList<Link>();
        boolean redundancy = false;
        for (Link l1 : this.links)
        {
            if (removedLinks.contains(l1))
            {
                continue;
            }
            String name1 = "1";
            for (Tag t1 : l1.getTags())
            {
                if (t1.getKey().equals("name"))
                {
                    name1 = t1.getValue();
                }
            }
            for (Link l2 : this.links)
            {
                if (removedLinks.contains(l2))
                {
                    continue;
                }
                String name2 = "2";
                for (Tag t2 : l2.getTags())
                {
                    if (t2.getKey().equals("name"))
                    {
                        name2 = t2.getValue();
                    }
                }
                if (l1.getEnd().equals(l2.getStart()) && name1.equalsIgnoreCase(name2)
                        && l1.getEnd().getTags().isEmpty() && l1.getTags().containsAll(l2.getTags())
                        && l1.getLanes() == l2.getLanes() && l1.getForwardLanes() == l2.getForwardLanes()
                        && l1.getStart() != l2.getEnd())
                {
                    if (removedLinks.contains(l1))
                    {
                        for (int i = 0; i < this.links.size(); i++)
                        {
                            Link l = this.links.get(i);
                            if (l == l1)
                            {
                                System.out.println("found link " + l + " at position " + i);
                            }
                        }
                        throw new Error("about to add " + l1 + " to removeLinks which already contains that link");
                    }
                    redundancy = true;
                    Link lnew =
                            new Link(l1.getStart(), l2.getEnd(), l1.getTags(), l1.getLength() + l2.getLength(),
                                    l1.getLanes(), l1.getForwardLanes());
                    if (!l1.getSplineList().isEmpty())
                    {
                        for (Node n1 : l1.getSplineList())
                        {
                            lnew.addSpline(n1);
                        }
                    }
                    if (!l2.getSplineList().isEmpty())
                    {
                        for (Node n2 : l2.getSplineList())
                        {
                            lnew.addSpline(n2);
                        }
                    }
                    // System.out.println("removing " + l1 + " and " + l2);
                    // System.out.println("adding " + lnew);
                    checkedLinks.add(lnew);
                    removedLinks.add(l1);
                    removedLinks.add(l2);
                    break; // don't merge any other links with l1; do that on the next iteration.
                }
            }
        }
        // System.out.println("Combining " + removedLinks.size() + " to " + checkedLinks.size());
        // if (removedLinks.size() < 40)
        // {
        // for (Link l : removedLinks)
        // System.out.println("Removing link " + l);
        // }
        this.links.removeAll(removedLinks);
        this.links.addAll(checkedLinks);
        //System.out.println("there are now " + this.links.size() + " links");
        return redundancy;
    }
}
