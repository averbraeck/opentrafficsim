package org.opentrafficsim.importexport.osm.input;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
import org.opentrafficsim.importexport.osm.Network;

/**
 * <p>
 * Copyright (c) 2013-2014 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights
 * reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/node/13">OpenTrafficSim License</a>.
 * <p>
 * @version 31 dec. 2014 <br>
 * @author <a href="http://www.tbm.tudelft.nl/mzhang">Mingxin Zhang </a>
 * @author <a>Moritz Bergmann</a>
 */
public class NewSink implements Sink
{
    /** */
    private Network net = new Network("tempnet");

    /** */
    private List<Tag> wantedTags;

    /** */
    private List<String> filterKeys;

    @Override
    public void initialize(final Map<String, Object> arg0)
    {
        // TODO Auto-generated method stub
    }

    /**
     * @param entityContainer Nodes: Initially in the process method all nodes are imported. Later on in the complete
     *            method unused nodes are detected and deleted. Ways: All ways which contain the specified wanted Tags
     *            are imported. The imported Tags for the Ways are filtered by the filterKeys
     */
    public final void process(final EntityContainer entityContainer)
    {
        Entity entity = entityContainer.getEntity();
        if (entity instanceof Node)
        {
            org.opentrafficsim.importexport.osm.Node node1 =
                    new org.opentrafficsim.importexport.osm.Node(entity.getId(), ((Node) entity).getLongitude(),
                            ((Node) entity).getLatitude());
            Iterator<Tag> tagint = entity.getTags().iterator();
            while (tagint.hasNext())
            {
                Tag nodetag = tagint.next();
                //if (true/* this.wantedTags.contains(nodetag) || this.wantedTags.isEmpty() */)
                //{
                    if (this.filterKeys.contains(nodetag.getKey()) || this.filterKeys.isEmpty())
                    {
                        org.opentrafficsim.importexport.osm.Tag tag =
                                new org.opentrafficsim.importexport.osm.Tag(nodetag.getKey(), nodetag.getValue());
                        node1.addTag(tag);
                    }
                //}
            }
            this.net.addNode(node1);
        }
        else if (entity instanceof Way)
        {
            boolean wanted = false;
            checkTags: for (Tag t : entity.getTags())
            {
                for (Tag t2 : this.wantedTags)
                {
                    if (t.getKey().equals(t2.getKey()) && t.getValue().equals(t2.getValue()))
                    {
                        wanted = true;
                        break checkTags;
                    }
                }
            }
            if (wanted)
            {
                Iterator<Tag> tagint = entity.getTags().iterator();
                org.opentrafficsim.importexport.osm.Way way1 =
                        new org.opentrafficsim.importexport.osm.Way(entity.getId());
                while (tagint.hasNext())
                {
                    Tag waytag = tagint.next();
                    if (this.filterKeys.contains(waytag.getKey()) || this.filterKeys.isEmpty())
                    {
                        org.opentrafficsim.importexport.osm.Tag tag =
                                new org.opentrafficsim.importexport.osm.Tag(waytag.getKey(), waytag.getValue());
                        way1.addTag(tag);
                    }
                }
                Iterator<WayNode> wayint1 = ((Way) entity).getWayNodes().iterator();
                while (wayint1.hasNext())
                {
                    WayNode waynode1 = wayint1.next();
                    way1.appendNode(waynode1.getNodeId());
                }
                this.net.addWay(way1);
            }
        }
        else if (entity instanceof Relation)
        { //A relation is a Set of Ways and Nodes
            boolean wanted = false;
            checkTags: for (Tag t : entity.getTags())
            {
                for (Tag t2 : this.wantedTags)
                {
                    if (t.getKey().equals(t2.getKey()) && t.getValue().equals(t2.getValue()))
                    {
                        wanted = true;
                        break checkTags;
                    }
                }
            }
            if (wanted)
            {
                Iterator<Tag> tagIterator = entity.getTags().iterator();
                org.opentrafficsim.importexport.osm.Relation rel1 =
                        new org.opentrafficsim.importexport.osm.Relation(entity.getId());
                while (tagIterator.hasNext())
                {
                    Tag reltag = tagIterator.next();
                    if (this.filterKeys.contains(reltag.getKey()) || this.filterKeys.isEmpty())
                    {
                        org.opentrafficsim.importexport.osm.Tag tag =
                                new org.opentrafficsim.importexport.osm.Tag(reltag.getKey(), reltag.getValue());
                        rel1.addTag(tag);
                    }
                }
                Iterator<RelationMember> relIterator = ((Relation) entity).getMembers().iterator();
                while (relIterator.hasNext())
                {
                    RelationMember relMember = relIterator.next();
                    if (relMember.getMemberType().equals(EntityType.Node))
                    {
                        rel1.addNode(relMember.getMemberId());
                    }
                    if (relMember.getMemberType().equals(EntityType.Way))
                    {
                        rel1.addWay(relMember.getMemberId());
                    }
                }
                this.net.addRelation(rel1);
            }
        }
    }

    /** */
    public void release()
    {
        // todo
    }

    /** The complete method is called after all Entities are processed. It removes all Nodes which are not used within any imported way or relation. */
    public final void complete()
    {
        HashMap<Long, org.opentrafficsim.importexport.osm.Node> usedNodes =
                new HashMap<Long, org.opentrafficsim.importexport.osm.Node>();
        for (Long wid : this.net.getWays().keySet())
        {
            try
            {
                org.opentrafficsim.importexport.osm.Way w = this.net.getWay(wid);
                for (Long nid : w.getNodes())
                {
                    if (this.net.getNodes().keySet().contains(nid) && !usedNodes.containsKey(nid))
                    {
                        usedNodes.put(nid, this.net.getNode(nid));
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        for (Long rid : this.net.getRelations().keySet())
        {
            try
            {
                org.opentrafficsim.importexport.osm.Relation r = this.net.getRelation(rid);
                for (Long nid : r.getNodes())
                {
                    if (this.net.getNodes().keySet().contains(nid) && !usedNodes.containsKey(nid))
                    {
                        usedNodes.put(nid, this.net.getNode(nid));
                    }
                }
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
        this.net.setNodes(usedNodes);
    }

    /**
     * @return the whole Network
     */
    public final Network getNetwork()
    {
        return this.net;
    }

    /**
     * Set/replace the list of wanted tags.<br>
     * This method makes a deep copy of the supplied list.
     * @param tags List&lt;Tag&gt;; the list of wanted tags
     */
    public final void setWantedTags(final List<org.opentrafficsim.importexport.osm.Tag> tags)
    {
        this.wantedTags = new ArrayList<Tag>();
        for (org.opentrafficsim.importexport.osm.Tag t1 : tags)
        {
            Tag t2 = new Tag(t1.getKey(), t1.getValue());
            this.wantedTags.add(t2);
        }
    }

    /**
     * Set/replace the filter keys.<br/>
     * The provided list is <b>not copied</b>; the caller should not modify the list afterwards.
     * @param keys List&lt;String&gt; list of filter keys
     */
    public final void setFilterKeys(final List<String> keys)
    {
        this.filterKeys = keys;
    }
}
