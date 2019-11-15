package org.opentrafficsim.road.network.factory.osm;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.opentrafficsim.road.network.factory.osm.events.ProgressEvent;
import org.opentrafficsim.road.network.factory.osm.events.ProgressListener;
import org.opentrafficsim.road.network.factory.osm.events.WarningEvent;
import org.opentrafficsim.road.network.factory.osm.events.WarningListener;

/**
 * Container for all imported entities of an OpenStreetMap file.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-26 01:01:13 +0200 (Sun, 26 Jul 2015) $, @version $Revision: 1155 $, by $Author: averbraeck $,
 * initial version 31 dec. 2014 <br>
 * @author <a>Moritz Bergmann</a>
 */
public class OSMNetwork implements Serializable
{
    /** */
    private static final long serialVersionUID = 20141231L;

    /** The name of the Network (immutable). */
    private final String name;

    /** The Nodes of the Network. */
    private Map<Long, OSMNode> nodes = new LinkedHashMap<Long, OSMNode>();

    /** The Ways of the Network. */
    private Map<Long, OSMWay> ways = new LinkedHashMap<Long, OSMWay>();

    /** The Relations of the Network. */
    private Map<Long, OSMRelation> relations = new LinkedHashMap<Long, OSMRelation>();

    /** The Links of the Network. */
    private List<OSMLink> links = new ArrayList<OSMLink>();

    /**
     * Construct a new OSMNetwork.
     * @param name String; the name of the new Network
     */
    public OSMNetwork(final String name)
    {
        this.name = name;
    }

    /**
     * Retrieve a list of Nodes that form a Way from this Network.
     * @param wayId Long; the id of the Way
     * @return List&lt;Long&gt;; the list of OSMNode ids of the OSMWay with the specified id
     * @throws IOException when no Way with the specified id exists in this Network
     */
    public final List<Long> getNodesFromWay(final Long wayId) throws IOException
    {
        OSMWay osmWay = this.ways.get(wayId);
        if (osmWay == null)
        {
            throw new IOException("Way with the ID: " + wayId + "was not found");
        }
        return osmWay.getNodes();
    }

    /**
     * Retrieve a Node from this Network.
     * @param nodeId long; the id of the Node
     * @return node OSMNode; the node with the specified id
     * @throws IOException when no OSMNode with the specified id exist in this Network
     */
    public final OSMNode getNode(final long nodeId) throws IOException
    {
        OSMNode result = this.nodes.get(nodeId);
        if (result == null)
        {
            throw new IOException("Node with the id: " + nodeId + "was not found");
        }
        return result;
    }

    /**
     * Retrieve the map of OSMNode ids to OSMNodes of this OSMNetwork.
     * @return Map&lt;Long, OSMNode&gt;; the map of all Nodes in this Network (modifications of the returned object are
     *         reflected in this OSMNetwork)
     */
    public final Map<Long, OSMNode> getNodes()
    {
        return this.nodes;
    }

    /**
     * Retrieve a Relation from this Network.
     * @param relid long; the id of the Relation
     * @return OSMRelation; modifications of the result are reflected in this OSMNetwork
     * @throws IOException when no Relation with the specified id exists in this Network
     */
    public final OSMRelation getRelation(final long relid) throws IOException
    {
        OSMRelation result = this.relations.get(relid);
        if (result == null)
        {
            throw new IOException("Relation with the ID: " + relid + "was not found");
        }
        return result;
    }

    /**
     * Retrieve the map of OSMRelations of this OSMNetwork.
     * @return Map&lt;Long, OSMRelation&gt;; the map of all OSMRelations in the network (modifications of the returned object
     *         are reflected in this OSMNetwork)
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
     * Retrieve the name of this OSMNetwork.
     * @return name String; the name of this OSMNetwork
     */
    public final String getName()
    {
        return this.name;
    }

    /**
     * Set/replace the Nodes of this Network.<br>
     * The provided list is <b>not copied</b>; the caller should not modify the list after setting it.
     * @param newnodes LinkedHashMap&lt;Long, OSMNode&gt;; the (new) Nodes for this Network
     */
    public final void setNodes(final LinkedHashMap<Long, OSMNode> newnodes)
    {
        this.nodes = newnodes;
    }

    /**
     * Add one OSMNode to this OSMNetwork.
     * @param node OSMNode; the node to add to this OSMNetwork
     */
    public final void addNode(final OSMNode node)
    {
        this.nodes.put(node.getId(), node);
    }

    /**
     * Add one OSMWay to this OSMNetwork.
     * @param way OSMWay; the OSMWay to add
     */
    public final void addWay(final OSMWay way)
    {
        this.ways.put(way.getId(), way);
    }

    /**
     * Add one OSMRelation to this Network.
     * @param osmRelation OSMRelation; the OSMRelation to add
     */
    public final void addRelation(final OSMRelation osmRelation)
    {
        this.relations.put(osmRelation.getId(), osmRelation);
    }

    /**
     * Retrieve the map of OSMWays of this OSMNetwork.
     * @return ways Map&lt;Long, OSMWay&gt;; the map of OSMWays of this OSMNetwork; modifications of the result are reflected in
     *         this OSMNetwork
     */
    public final Map<Long, OSMWay> getWays()
    {
        return this.ways;
    }

    /**
     * Retrieve the list of OSMLinks of this OSMNetwork.
     * @return links List&lt;OSMLink&gt;; the list of OSMLinks of this OSMNetwork; modifications of the result are reflected in
     *         this OSMNetwork
     */
    public final List<OSMLink> getLinks()
    {
        return this.links;
    }

    /**
     * Creates links out of the ways in this network.
     * @param warningListener WarningListener; the warning listener that will receive warning events
     * @param progressListener ProgressListener; the progress listener that will receive progress events
     * @throws IOException on read errors
     */
    public final void makeLinks(final WarningListener warningListener, final ProgressListener progressListener)
            throws IOException
    {
        progressListener.progress(new ProgressEvent(this, "Starting link creation:"));

        List<OSMLink> newLinks = new ArrayList<OSMLink>();
        for (Long wayId : this.ways.keySet())
        {
            OSMWay osmWay = this.getWay(wayId);
            List<Long> waynodes = osmWay.getNodes();
            for (int i = 0; i < (waynodes.size() - 1); i++)
            {
                OSMNode fromNode = this.getNode(waynodes.get(i).longValue());
                OSMNode toNode = this.getNode(waynodes.get(i + 1).longValue());
                // Workaround for bug in OSM near Mettmann Germany
                if (fromNode == toNode)
                {
                    continue;
                }
                // Something similar occurs in Duesseldorf (PK), but here the node ids are different; work-around below
                if (fromNode.getLatitude() == toNode.getLatitude() && fromNode.getLongitude() == toNode.getLongitude())
                {
                    warningListener.warning(new WarningEvent(this, String.format("Node clash %s vs %s", fromNode, toNode)));
                    // FIXME: should probably assign all link end points of toNode to fromNode
                    continue;
                }
                newLinks.add(
                        new OSMLink(fromNode, toNode, osmWay.getTags(), distanceLongLat(fromNode, toNode), warningListener));
            }
        }
        this.links = newLinks;
        progressListener.progress(new ProgressEvent(this, "Link creation finished. Created " + this.links.size() + " links."));
    }

    /**
     * Compute the distance between two OSMNodes.
     * @param fromNode OSMNode; the first location
     * @param toNode OSMNode; the second location
     * @return distance in meters from point 1 to point 2. This method utilizes great circle calculation
     */
    private double distanceLongLat(final OSMNode fromNode, final OSMNode toNode)
    {
        double y1 = fromNode.getLatitude();
        double y2 = toNode.getLatitude();
        if (y1 < 0)
        {
            y1 += 360;
        }
        if (y2 < 0)
        {
            y2 += 360;
        }
        double x1 = 90 - fromNode.getLongitude();
        double x2 = 90 - toNode.getLongitude();
        x1 = Math.toRadians(x1);
        x2 = Math.toRadians(x2);
        y1 = Math.toRadians(y1);
        y2 = Math.toRadians(y2);
        final double earthRadius = 6371 * 1000;
        return Math.acos(Math.sin(x1) * Math.sin(x2) * Math.cos(y1 - y2) + Math.cos(x1) * Math.cos(x2)) * earthRadius;
    }

    /**
     * FIXME Network looks 'crooked' after using this. This function checks for and removes redundancies between the networks
     * links.
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
     * @return boolean; true if the Network was modified (further reduction may be possible by calling this method again)
     */
    private boolean redundancyCheck()
    {
        List<OSMLink> checkedLinks = new ArrayList<OSMLink>();
        List<OSMLink> removedLinks = new ArrayList<OSMLink>();
        boolean redundancyRemoved = false;
        // FIXME PK thinks that this method could remove a junction from a through-road
        for (OSMLink link1 : this.links)
        {
            if (removedLinks.contains(link1))
            {
                continue;
            }
            String link1Name = "1";
            for (OSMTag t1 : link1.getTags())
            {
                if (t1.getKey().equals("name"))
                {
                    link1Name = t1.getValue();
                }
            }
            for (OSMLink link2 : this.links)
            {
                if (removedLinks.contains(link2))
                {
                    continue;
                }
                String link2Name = "2";
                for (OSMTag t2 : link2.getTags())
                {
                    if (t2.getKey().equals("name"))
                    {
                        link2Name = t2.getValue();
                    }
                }
                if (link1.getEnd().equals(link2.getStart()) && link1Name.equalsIgnoreCase(link2Name)
                        && link1.getEnd().hasNoTags() && link1.containsAllTags(link2.getTags())
                        && link1.getLanes() == link2.getLanes() && link1.getForwardLanes() == link2.getForwardLanes()
                        && link1.getStart() != link2.getEnd())
                {
                    if (removedLinks.contains(link1))
                    {
                        for (int i = 0; i < this.links.size(); i++)
                        {
                            OSMLink l = this.links.get(i);
                            if (l == link1)
                            {
                                System.out.println("found link " + l + " at position " + i);
                            }
                        }
                        throw new Error("about to add " + link1 + " to removeLinks which already contains that link");
                    }
                    redundancyRemoved = true;
                    OSMLink replacementLink = new OSMLink(link1.getStart(), link2.getEnd(), link1.getTags(),
                            link1.getLength() + link2.getLength(), link1.getLanes(), link1.getForwardLanes());
                    if (!link1.getSplineList().isEmpty())
                    {
                        for (OSMNode n1 : link1.getSplineList())
                        {
                            replacementLink.addSpline(n1);
                        }
                    }
                    if (!link2.getSplineList().isEmpty())
                    {
                        for (OSMNode n2 : link2.getSplineList())
                        {
                            replacementLink.addSpline(n2);
                        }
                    }
                    checkedLinks.add(replacementLink);
                    removedLinks.add(link1);
                    removedLinks.add(link2);
                    break; // don't merge any other links with l1; do that on the next iteration.
                }
            }
        }
        this.links.removeAll(removedLinks);
        this.links.addAll(checkedLinks);
        return redundancyRemoved;
    }

    /**
     * Finds the link that follows a given OSMLink.
     * @param link OSMLink; OSMLink for which a successor OSMLink is sought
     * @return OSMLink (one of) the successor OSMLink(s) of the given OSMLink, or null if the given OSMLink has no successors
     */
    public final OSMLink findFollowingLink(final OSMLink link)
    {
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
     * Finds an OSMLink that precedes the given OSMLink.
     * @param link OSMLink; the OSMLink for which a predecessor OSMLink is sought
     * @return OSMLink (one of) the predecessor OSMLink(s) of the given OSMLink, or null if the given OSMLink has no
     *         predecessors
     */
    public final OSMLink findPrecedingLink(final OSMLink link)
    {
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
     * @param link OSMLink; the link for which the caller wants to know whether there is a preceding link
     * @return boolean; true if the given link has one or more predecessors; false otherwise
     */
    public final boolean hasPrecedingLink(final OSMLink link)
    {
        return null != findPrecedingLink(link);
    }

    /**
     * Returns true if the given OSMLink has a following OSMLink.
     * @param link OSMLink; the OSMLink for which the caller wants to know if it has a follower OSMLink
     * @return boolean; true if the specified OSMLink has a follower link in this OSMNetwork
     */
    public final boolean hasFollowingLink(final OSMLink link)
    {
        return null != findFollowingLink(link);
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "OSMNetwork [name=" + this.name + ", nodes.size=" + this.nodes.size() + ", ways.size=" + this.ways.size()
                + ", relations.size=" + this.relations.size() + ", links.size=" + this.links.size() + "]";
    }
}
