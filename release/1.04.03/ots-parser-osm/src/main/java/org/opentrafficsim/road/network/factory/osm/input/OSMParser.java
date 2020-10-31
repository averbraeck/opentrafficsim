package org.opentrafficsim.road.network.factory.osm.input;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.openstreetmap.osmosis.core.container.v0_6.EntityContainer;
import org.openstreetmap.osmosis.core.domain.v0_6.Entity;
import org.openstreetmap.osmosis.core.domain.v0_6.EntityType;
import org.openstreetmap.osmosis.core.domain.v0_6.Node;
import org.openstreetmap.osmosis.core.domain.v0_6.Relation;
import org.openstreetmap.osmosis.core.domain.v0_6.RelationMember;
import org.openstreetmap.osmosis.core.domain.v0_6.Tag;
import org.openstreetmap.osmosis.core.domain.v0_6.Way;
import org.openstreetmap.osmosis.core.domain.v0_6.WayNode;
import org.openstreetmap.osmosis.core.task.v0_6.Sink;
import org.opentrafficsim.road.network.factory.osm.OSMNetwork;
import org.opentrafficsim.road.network.factory.osm.OSMNode;
import org.opentrafficsim.road.network.factory.osm.OSMRelation;
import org.opentrafficsim.road.network.factory.osm.OSMTag;
import org.opentrafficsim.road.network.factory.osm.OSMWay;
import org.opentrafficsim.road.network.factory.osm.events.ProgressEvent;
import org.opentrafficsim.road.network.factory.osm.events.ProgressListener;

/**
 * Build a structure from the elements in an OSM file.
 * <p>
 * Copyright (c) 2013-2020 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-26 01:01:13 +0200 (Sun, 26 Jul 2015) $, @version $Revision: 1155 $, by $Author: averbraeck $,
 * initial version 31 dec. 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/mzhang">Mingxin Zhang </a>
 * @author <a>Moritz Bergmann</a>
 */
public class OSMParser implements Sink, Serializable
{
    /** */
    private static final long serialVersionUID = 20141231L;

    /** The OSMNetwork. */
    private OSMNetwork net = new OSMNetwork("tempnet");

    /** The wanted Tags. If null, all Tags are wanted. */
    private List<Tag> wantedTags;

    /** */
    private List<String> filterKeys;

    /** ProgressListener. */
    private ProgressListener progressListener;

    /**
     * Construct a new OSMParser and set wantedTags and filteredKeys in one call.
     * @param wantedTags List&lt;OSMTag&gt;; the list of wantedTags
     * @param filteredKeys List&lt;String&gt;; the list of filtered keys
     */
    public OSMParser(final List<OSMTag> wantedTags, final List<String> filteredKeys)
    {
        setWantedTags(wantedTags);
        this.filterKeys = filteredKeys;
    }

    /** {@inheritDoc} */
    @Override
    public void initialize(final Map<String, Object> arg0)
    {
        // Nothing needs to be initialized in this Sink implementation
    }

    /** {@inheritDoc} */
    @Override
    public final void process(final EntityContainer entityContainer)
    {
        Entity entity = entityContainer.getEntity();
        if (entity instanceof Node)
        {
            Node node = (Node) entity;
            OSMNode osmNode = new OSMNode(entity.getId(), node.getLongitude(), node.getLatitude());
            Iterator<Tag> tagint = entity.getTags().iterator();
            while (tagint.hasNext())
            {
                Tag nodetag = tagint.next();
                if (this.filterKeys.contains(nodetag.getKey()) || this.filterKeys.isEmpty())
                {
                    OSMTag tag = new OSMTag(nodetag.getKey(), nodetag.getValue());
                    osmNode.addTag(tag);
                }
            }
            this.net.addNode(osmNode);
        }
        else if (entity instanceof Way)
        {
            boolean wanted = this.wantedTags.size() == 0;
            if (!wanted)
            {
                checkTags: for (Tag wayTag : entity.getTags())
                {
                    for (Tag wantedTag : this.wantedTags)
                    {
                        if (wayTag.getKey().equals(wantedTag.getKey()) && wayTag.getValue().equals(wantedTag.getValue()))
                        {
                            wanted = true;
                            break checkTags;
                        }
                    }
                }
            }
            if (wanted)
            {
                Iterator<Tag> tagint = entity.getTags().iterator();
                OSMWay osmWay = new OSMWay(entity.getId());
                while (tagint.hasNext())
                {
                    Tag waytag = tagint.next();
                    if (this.filterKeys.contains(waytag.getKey()) || this.filterKeys.isEmpty())
                    {
                        OSMTag tag = new OSMTag(waytag.getKey(), waytag.getValue());
                        osmWay.addTag(tag);
                    }
                }
                Iterator<WayNode> wayint1 = ((Way) entity).getWayNodes().iterator();
                while (wayint1.hasNext())
                {
                    WayNode waynode1 = wayint1.next();
                    osmWay.appendNode(waynode1.getNodeId());
                }
                this.net.addWay(osmWay);
            }
        }
        else if (entity instanceof Relation)
        { // A relation is a Set of Ways and Nodes
            boolean wanted = false;
            checkTags: for (Tag entityTag : entity.getTags())
            {
                for (Tag wantedTag : this.wantedTags)
                {
                    if (entityTag.getKey().equals(wantedTag.getKey()) && entityTag.getValue().equals(wantedTag.getValue()))
                    {
                        wanted = true;
                        break checkTags;
                    }
                }
            }
            if (wanted)
            {
                Iterator<Tag> tagIterator = entity.getTags().iterator();
                OSMRelation osmRelation = new OSMRelation(entity.getId());
                while (tagIterator.hasNext())
                {
                    Tag reltag = tagIterator.next();
                    if (this.filterKeys.contains(reltag.getKey()) || this.filterKeys.isEmpty())
                    {
                        OSMTag tag = new OSMTag(reltag.getKey(), reltag.getValue());
                        osmRelation.addTag(tag);
                    }
                }
                Iterator<RelationMember> relIterator = ((Relation) entity).getMembers().iterator();
                while (relIterator.hasNext())
                {
                    RelationMember relMember = relIterator.next();
                    if (relMember.getMemberType().equals(EntityType.Node))
                    {
                        osmRelation.addNode(relMember.getMemberId());
                    }
                    if (relMember.getMemberType().equals(EntityType.Way))
                    {
                        osmRelation.addWay(relMember.getMemberId());
                    }
                }
                this.net.addRelation(osmRelation);
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public void close()
    {
        // Nothing needs to be done after the entire network has been parsed
    }

    /** {@inheritDoc} */
    @Override
    public final void complete()
    {
        LinkedHashMap<Long, OSMNode> usedNodes = new LinkedHashMap<Long, OSMNode>();
        double total = this.net.getWays().size() + this.net.getRelations().size();
        double counter = 0;
        double percentageStep = 20.0d;
        double nextPercentage = percentageStep;
        this.progressListener.progress(new ProgressEvent(this, "Removing unused nodes"));
        for (Long wid : this.net.getWays().keySet())
        {
            try
            {
                OSMWay w = this.net.getWay(wid);
                for (Long nid : w.getNodes())
                {
                    if (this.net.getNodes().containsKey(nid) && !usedNodes.containsKey(nid))
                    {
                        usedNodes.put(nid, this.net.getNode(nid));
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            if (++counter / total * 100 >= nextPercentage)
            {
                this.progressListener.progress(new ProgressEvent(this, "Removing unused nodes " + nextPercentage + "%"));
                nextPercentage += percentageStep;
            }
        }
        for (OSMRelation r : this.net.getRelations().values())
        {
            try
            {
                // OSMRelation r = this.net.getRelation(rid);
                for (Long nid : r.getNodes())
                {
                    if (this.net.getNodes().containsKey(nid) && !usedNodes.containsKey(nid))
                    {
                        usedNodes.put(nid, this.net.getNode(nid));
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
            if (++counter / total * 100 >= nextPercentage)
            {
                this.progressListener.progress(new ProgressEvent(this, "Removing unused nodes " + nextPercentage + "%"));
                nextPercentage += percentageStep;
            }
        }
        this.net.setNodes(usedNodes);
        this.progressListener.progress(new ProgressEvent(this, "Cleanup complete."));
    }

    /**
     * @return the whole Network
     */
    public final OSMNetwork getNetwork()
    {
        return this.net;
    }

    /**
     * Set/replace the list of wanted tags.<br>
     * This method makes a deep copy of the supplied list.
     * @param tags List&lt;OSMTag&gt;; the list of wanted tags
     */
    public final void setWantedTags(final List<OSMTag> tags)
    {
        this.wantedTags = new ArrayList<Tag>();
        for (OSMTag osmTag : tags)
        {
            this.wantedTags.add(new Tag(osmTag.getKey(), osmTag.getValue()));
        }
    }

    /**
     * Set/replace the filter keys.<br>
     * The provided list is <b>not copied</b>; the caller should not modify the list afterwards.
     * @param keys List&lt;String&gt;; List&lt;String&gt; list of filter keys
     */
    public final void setFilterKeys(final List<String> keys)
    {
        this.filterKeys = keys;
    }

    /**
     * @return progressListener.
     */
    public final ProgressListener getProgressListener()
    {
        return this.progressListener;
    }

    /**
     * @param progressListener ProgressListener; set progressListener.
     */
    public final void setProgressListener(final ProgressListener progressListener)
    {
        this.progressListener = progressListener;
    }

    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "OSMParser [net=" + this.net + ", wantedTags=" + this.wantedTags + ", filterKeys=" + this.filterKeys + "]";
    }
}
