package org.opentrafficsim.importexport.osm;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.opentrafficsim.importexport.osm.events.ProgressEvent;
import org.opentrafficsim.importexport.osm.events.ProgressListener;
import org.opentrafficsim.importexport.osm.events.WarningListener;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 31 dec. 2014 <br>
 * @author <a>Moritz Bergmann</a>
 */
public class OSMNetwork
{
    /** The Nodes of the Network. */
    private Map<Long, OSMNode> nodes;

    /** The Ways of the Network. */
    private Map<Long, OSMWay> ways;

    /** The Relations of the Network. */
    private Map<Long, OSMRelation> relations;

    /** The name of the Network (immutable). */
    private final String name;

    /** The Links of the Network. */
    private List<OSMLink> links;

    /**
     * Construct a new OSMNetwork.
     * @param name String; the name of the new Network
     */
    public OSMNetwork(final String name)
    {
        this.name = name;
        this.nodes = new HashMap<Long, OSMNode>();
        this.ways = new HashMap<Long, OSMWay>();
        this.relations = new HashMap<Long, OSMRelation>();
        this.links = new ArrayList<OSMLink>();
    }

    /**
     * Construct a new Network out of another Network.
     * @param net
     */
    public OSMNetwork(final OSMNetwork net)
    {
        this.name = net.getName();
        this.nodes = new HashMap<Long, OSMNode>(net.getNodes());
        this.ways = new HashMap<Long, OSMWay>(net.getWays());
        this.relations = new HashMap<Long, OSMRelation>(net.getRelations());
        this.links = new ArrayList<OSMLink>(net.getLinks());
    }

    /**
     * Retrieve a list of Nodes that form a Way from this Network.
     * @param wayid long; the id of the Way
     * @return List&lt;Long*gt;; the list of NodeIds of way with the specified id
     * @throws IOException when no Way with the specified id exists in this Network
     */
    public final List<Long> getNodesFromWay(final Long wayid) throws IOException
    {
        OSMWay w = this.ways.get(wayid);
        if (w == null)
        {
            throw new IOException("Way with the ID: " + wayid + "was not found");
        }
        else
        {
            return w.getNodes();
        }
    }

    /**
     * Retrieve a Node from this Network.
     * @param nodeid long; the id of the Node
     * @return node Node; the node with the specified id
     * @throws IOException when no Node with the specified id exist in this Network
     */
    public final OSMNode getNode(final long nodeid) throws IOException
    {
        OSMNode n = this.nodes.get(nodeid);
        if (n == null)
        {
            throw new IOException("Node with the ID: " + nodeid + "was not found");
        }
        else
        {
            return n;
        }
    }

    /**
     * @return HashMap of all Nodes in this Network
     */
    public final Map<Long, OSMNode> getNodes()
    {
        return this.nodes;
    }

    /**
     * Retrieve a Relation from this Network.
     * @param relid long; the id of the Relation
     * @return Relation
     * @throws IOException when no Relation with the specified id exists in this Network
     */
    public final OSMRelation getRelation(final long relid) throws IOException
    {
        OSMRelation r = this.relations.get(relid);
        if (r == null)
        {
            throw new IOException("Relation with the ID: " + relid + "was not found");
        }
        else
        {
            return r;
        }
    }

    /**
     * @return HashMap of all relations in the network
     */
    public final Map<Long, OSMRelation> getRelations()
    {
        return this.relations;
    }

    /**
     * Retrieve a Way from this Network.
     * @param wayid long; the id of a Way
     * @return Way
     * @throws IOException when no Way with the specified id exist in this network
     */
    public final OSMWay getWay(final long wayid) throws IOException
    {
        OSMWay w = this.ways.get(wayid);
        if (w == null)
        {
            throw new IOException("Way with the ID: " + wayid + "was not found");
        }
        else
        {
            return w;
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
     * Set/replace the Nodes of this Network.<br>
     * The provided list is <b>not copied</b>; the caller should not modify the list after setting it.
     * @param newnodes HashMap&lt;Long, Node&gt;; the (new) Nodes for this Network
     */
    public final void setNodes(final HashMap<Long, OSMNode> newnodes)
    {
        this.nodes = newnodes;
    }

    /**
     * Add a Node to this Network.
     * @param node Node; the node to add to this Network
     */
    public final void addNode(final OSMNode node)
    {
        this.nodes.put(node.getId(), node);
    }

    /**
     * Delete a Node from this Network.
     * @param node Node; the Node to delete
     */
    public final void delNode(final OSMNode node)
    {
        this.nodes.remove(node.getId());
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
    public final void addWay(final OSMWay way)
    {
        this.ways.put(way.getID(), way);
    }

    /**
     * Add a Relation to this Network.
     * @param rel Relation; the relation to add
     */
    public final void addRelation(final OSMRelation rel)
    {
        this.relations.put(rel.getID(), rel);
    }

    /**
     * @return ways
     */
    public final Map<Long, OSMWay> getWays()
    {
        return this.ways;
    }

    /**
     * @return links
     */
    public final List<OSMLink> getLinks()
    {
        return this.links;
    }

    /**
     * Creates links out of the ways in this network.
     * @param warningListener
     * @param progressListener
     * @throws IOException on read errors
     */
    public final void makeLinks(final WarningListener warningListener, final ProgressListener progressListener)
            throws IOException
    {
        progressListener.progress(new ProgressEvent(this, "Starting link creation:"));

        List<OSMLink> links2 = new ArrayList<OSMLink>();
        for (Long wayid : this.ways.keySet())
        {
            List<Long> waynodes = this.getWay(wayid).getNodes();
            for (int i = 0; i < (waynodes.size() - 1); i++)
            {
                OSMNode fromNode = this.getNode(waynodes.get(i).longValue());
                OSMNode toNode = this.getNode(waynodes.get(i + 1).longValue());
                // Workaround for bug in OSM near Mettmann Germany
                if (fromNode == toNode)
                {
                    continue;
                }
                double x1 = fromNode.getLatitude();
                double x2 = toNode.getLatitude();
                double y1 = fromNode.getLongitude();
                double y2 = toNode.getLongitude();
                double length = distanceLongLat(x1, y1, x2, y2);
                OSMLink e = new OSMLink(fromNode, toNode, this.getWay(wayid).getTags(), length, warningListener);
                links2.add(e);
            }
        }
        this.links = links2;
        progressListener.progress(new ProgressEvent(this, "Link creation finished. Created " + this.links.size()
                + " links."));
    }

    /**
     * Compute the distance between two locations specified by longitude and latitude on Earth
     * @param fromLongitude Latitude of point 1 in degrees
     * @param fromLatitude Longitude of point 1 in degrees
     * @param toLongitude Latitude of point 2 in degrees
     * @param toLatitude Longitude of point 2 in degrees
     * @return distance in meters from point 1 to point 2. This method utilizes great circle calculation
     */
    private static double distanceLongLat(final double fromLongitude, final double fromLatitude,
            final double toLongitude, final double toLatitude)
    {
        double y1 = fromLatitude;
        double y2 = toLatitude;
        if (y1 < 0)
        {
            y1 = 360 + fromLatitude;
        }
        if (y2 < 0)
        {
            y2 = 360 + toLatitude;
        }
        double x1 = 90 - fromLongitude;
        double x2 = 90 - toLongitude;
        x1 = Math.toRadians(x1);
        x2 = Math.toRadians(x2);
        y1 = Math.toRadians(y1);
        y2 = Math.toRadians(y2);
        final double earthRadius = 6371 * 1000;
        return Math.acos(Math.sin(x1) * Math.sin(x2) * Math.cos(y1 - y2) + Math.cos(x1) * Math.cos(x2)) * earthRadius;
    }

    /**
     * FIXME Network looks 'crooked' after using this. This function checks for and removes redundancies between the
     * networks links.
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
        List<OSMLink> checkedLinks = new ArrayList<OSMLink>();
        List<OSMLink> removedLinks = new ArrayList<OSMLink>();
        boolean redundancyRemoved = false;
        // FIXME PK thinks that this method could remove a junction from a through-road
        for (OSMLink l1 : this.links)
        {
            if (removedLinks.contains(l1))
            {
                continue;
            }
            String name1 = "1";
            for (OSMTag t1 : l1.getTags())
            {
                if (t1.getKey().equals("name"))
                {
                    name1 = t1.getValue();
                }
            }
            for (OSMLink l2 : this.links)
            {
                if (removedLinks.contains(l2))
                {
                    continue;
                }
                String name2 = "2";
                for (OSMTag t2 : l2.getTags())
                {
                    if (t2.getKey().equals("name"))
                    {
                        name2 = t2.getValue();
                    }
                }
                if (l1.getEnd().equals(l2.getStart()) && name1.equalsIgnoreCase(name2) && l1.getEnd().hasNoTags()
                        && l1.containsAllTags(l2.getTags()) && l1.getLanes() == l2.getLanes()
                        && l1.getForwardLanes() == l2.getForwardLanes() && l1.getStart() != l2.getEnd())
                {
                    if (removedLinks.contains(l1))
                    {
                        for (int i = 0; i < this.links.size(); i++)
                        {
                            OSMLink l = this.links.get(i);
                            if (l == l1)
                            {
                                System.out.println("found link " + l + " at position " + i);
                            }
                        }
                        throw new Error("about to add " + l1 + " to removeLinks which already contains that link");
                    }
                    redundancyRemoved = true;
                    OSMLink replacementLink =
                            new OSMLink(l1.getStart(), l2.getEnd(), l1.getTags(), l1.getLength() + l2.getLength(),
                                    l1.getLanes(), l1.getForwardLanes());
                    if (!l1.getSplineList().isEmpty())
                    {
                        for (OSMNode n1 : l1.getSplineList())
                        {
                            replacementLink.addSpline(n1);
                        }
                    }
                    if (!l2.getSplineList().isEmpty())
                    {
                        for (OSMNode n2 : l2.getSplineList())
                        {
                            replacementLink.addSpline(n2);
                        }
                    }
                    checkedLinks.add(replacementLink);
                    removedLinks.add(l1);
                    removedLinks.add(l2);
                    break; // don't merge any other links with l1; do that on the next iteration.
                }
            }
        }
        this.links.removeAll(removedLinks);
        this.links.addAll(checkedLinks);
        return redundancyRemoved;
    }

    /**
     * Finds the link which follows the given link. If it exists.
     * @param link
     * @return Link
     */
    public final OSMLink findFollowingLink(final OSMLink link)
    {
        /*-
        if (!this.links.contains(link))
        {
            throw new Error("This link does not exist in this network: " + link);
        }
         */
        final OSMNode startNode = link.getEnd();
        for (OSMLink l2 : this.links)
        {
            if (startNode.equals(l2.getStart()))
            {
                return l2;
            }
        }
        return null;
    }

    /**
     * Finds the link which precedes the given link. If it exists.
     * @param link
     * @return Link
     */
    public final OSMLink findPrecedingLink(final OSMLink link)
    {
        /*-
        if (!this.links.contains(link))
        {
            throw new Error("This link does not exist in this network: " + link);
        }
         */
        final OSMNode endNode = link.getStart();
        for (OSMLink l2 : this.links)
        {
            if (endNode.equals(l2.getEnd()))
            {
                return l2;
            }
        }
        return null;
    }

    /**
     * Returns true if the given link has a preceding link.
     * @param link
     * @return boolean
     */
    public final boolean hasPrecedingLink(final OSMLink link)
    {
        return null != findPrecedingLink(link);
    }

    /**
     * Returns true if the given link has a following link.
     * @param link
     * @return boolean
     */
    public final boolean hasFollowingLink(final OSMLink link)
    {
        return null != findFollowingLink(link);
    }
}
