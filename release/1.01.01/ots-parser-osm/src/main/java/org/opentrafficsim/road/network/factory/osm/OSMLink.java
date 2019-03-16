package org.opentrafficsim.road.network.factory.osm;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.opentrafficsim.base.Identifiable;
import org.opentrafficsim.road.network.factory.osm.events.WarningEvent;
import org.opentrafficsim.road.network.factory.osm.events.WarningListener;

/**
 * OpenStreetMap Link.
 * <p>
 * Copyright (c) 2013-2019 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * <p>
 * $LastChangedDate: 2015-07-26 01:01:13 +0200 (Sun, 26 Jul 2015) $, @version $Revision: 1155 $, by $Author: averbraeck $,
 * initial version 31 dec. 2014 <br>
 * @author <a>Moritz Bergmann</a>
 */
public class OSMLink implements Serializable, Identifiable
{
    /** */
    private static final long serialVersionUID = 20141231L;

    /** The Link ID. It is generated out of the Start ID and the End ID and (if present) the name tag. */
    private final String id;

    /** The start Node of the OSMLink. */
    private final OSMNode start;

    /** The end Node of the OSMLink. */
    private final OSMNode end;

    /** The List of nodes that are used only to define shape (for mapping purposes). */
    private List<OSMNode> intermediateNodes;

    /** The length of the OSMLink. */
    private final double length;

    /** The tags that this OSMLink inherits from it's way. */
    private List<OSMTag> tags;

    /** The number of lanes on this OSMLink. */
    private byte lanes;

    /** The number of lanes going forward (the <i>design</i> direction). */
    private byte forwardLanes;

    /** Is this OSMLink one way? */
    private boolean oneway;

    /**
     * Construct a new OSMLink.
     * @param fromNode OSMNode; the OSMNode where this OSMLinks begins
     * @param toNode OSMNode; the OSMNode where this OSMLink ends
     * @param tags List&lt;OSMTag&gt;; the OSMTags (inherited from the OSMWay that causes this OSMLink to be constructed)
     * @param length double; the length of the new OSMLink
     * @param warningListener WarningListener; the warning listener that will receive warning events
     */
    public OSMLink(final OSMNode fromNode, final OSMNode toNode, final List<OSMTag> tags, final double length,
            final WarningListener warningListener)
    {
        if (fromNode == toNode)
        {
            throw new Error("Start and end of link are the same Node: " + fromNode);
        }
        String name = "";
        for (OSMTag tag : tags)
        {
            if (tag.getKey().equals("name"))
            {
                name = ": " + tag.getValue();
            }
        }
        this.id = Objects.toString(fromNode.getId()) + Objects.toString(toNode.getId()) + name;
        this.start = fromNode;
        this.end = toNode;
        this.length = length;
        this.lanes = 1;
        this.forwardLanes = 1;
        boolean forwardDefined = false;

        List<OSMTag> linkTags = new ArrayList<OSMTag>(tags);
        for (OSMTag tag : tags)
        {
            if (tag.getKey().equals("oneway") && tag.getValue().equals("yes"))
            {
                this.setOneway(true);
                linkTags.remove(tag);
                this.forwardLanes = this.lanes;
            }
            if (tag.getKey().equals("highway") && tag.getValue().equals("motorway_link"))
            {
                this.setOneway(true);
                this.lanes = 1;
                this.forwardLanes = this.lanes;
            }
            if (tag.getKey().equals("highway") && tag.getValue().equals("motorway"))
            {
                this.setOneway(true);
                this.forwardLanes = this.lanes;
            }
            if (tag.getKey().equals("highway") && (tag.getValue().equals("cycleway") || tag.getValue().equals("footway")
                    || tag.getValue().equals("pedestrian") || tag.getValue().equals("steps")))
            {
                this.lanes = 1;
            }
        }

        for (OSMTag tag2 : new ArrayList<OSMTag>(linkTags))
        {
            if (tag2.getKey().equals("lanes"))
            {
                if (isByte(tag2.getValue()))
                {
                    this.lanes = Byte.parseByte(tag2.getValue());
                    linkTags.remove(tag2);
                    if (this.oneway)
                    {
                        this.forwardLanes = this.lanes;
                        forwardDefined = true;
                    }
                }
                else
                {
                    String warning = "Illegal value for the tag 'lanes' at link " + this.id;
                    warningListener.warning(new WarningEvent(this, warning));
                }
            }
            if (tag2.getKey().equals("lanes:forward"))
            {
                if (isByte(tag2.getValue()))
                {
                    this.forwardLanes = Byte.parseByte(tag2.getValue());
                    linkTags.remove(tag2);
                    forwardDefined = true;
                }
                else
                {
                    String warning = "Illegal value for the tag 'lanes:forward' at link " + this.id;
                    warningListener.warning(new WarningEvent(this, warning));
                }
            }
        }
        this.tags = linkTags;
        if (!forwardDefined && this.lanes > 1)
        {
            this.forwardLanes = (byte) (this.lanes / 2);
            String warning = "No forward lanes defined at link " + this.id;
            warningListener.warning(new WarningEvent(this, warning));
        }
        this.intermediateNodes = new ArrayList<OSMNode>();
    }

    /**
     * Construct a new OSMLink with specified number of lanes and forward lanes.
     * @param startNode OSMNode; the start OSMNode of the new OSMLink
     * @param endNode OSMNode; the end OSMNode of the new OSMLink
     * @param tags List&lt;OSMTag&gt;; List of Tags inherited from way
     * @param length double; length of link
     * @param lanes byte; the total number of lanes
     * @param flanes byte; the number of forward lanes
     */
    public OSMLink(final OSMNode startNode, final OSMNode endNode, final List<OSMTag> tags, final double length,
            final byte lanes, final byte flanes)
    {
        if (startNode == endNode)
        {
            throw new Error("start and end of link are the same Node: " + startNode);
        }
        this.id = Objects.toString(startNode.getId()) + Objects.toString(endNode.getId());
        this.start = startNode;
        this.end = endNode;
        this.tags = tags;
        this.length = length;
        this.lanes = lanes;
        this.forwardLanes = flanes;
        this.intermediateNodes = new ArrayList<OSMNode>();
    }

    /** {@inheritDoc} */
    @Override
    public final String getId()
    {
        return this.id;
    }

    /**
     * @return start.
     */
    public final OSMNode getStart()
    {
        return this.start;
    }

    /**
     * @return end.
     */
    public final OSMNode getEnd()
    {
        return this.end;
    }

    /**
     * Retrieve the tags of this OSMLink.
     * @return List&lt;OSMTab&gt;; the returned object is a copy; modifications of the returned object do not affect this
     *         OSMLink
     */
    public final List<OSMTag> getTags()
    {
        return new ArrayList<OSMTag>(this.tags); // Create and return a copy (the tags themselves are immutable).
    }

    /**
     * Indicate if this OSMLink is one way.
     * @return boolean; true if this OSMLink is one way; false if this OSMLink is not one way
     */
    public final boolean isOneway()
    {
        return this.oneway;
    }

    /**
     * Set the one way status of this OSMLink.
     * @param isOneWay boolean; the new value for the one way status of this OSMLink
     */
    public final void setOneway(final boolean isOneWay)
    {
        this.oneway = isOneWay;
    }

    /**
     * Retrieve the total number of lanes on this OSMLink.
     * @return byte; the total number of lanes on this OSMLink
     */
    public final byte getLanes()
    {
        return this.lanes;
    }

    /**
     * Retrieve the total number of forward lanes on this OSMLink; forward lanes are lanes that may only be traveled from
     * startNode towards endNode.
     * @return byte; the number of forward lanes on this OSMLink
     */
    public final byte getForwardLanes()
    {
        return this.forwardLanes;
    }

    /**
     * Add an OSMTag to this Link.
     * @param tag OSMTag; the OSMTag that must be added
     */
    public final void addTag(final OSMTag tag)
    {
        this.tags.add(tag);
    }

    /**
     * Retrieve the length of this OSMLink.
     * @return double; the length of this OSMLink in meters
     */
    public final double getLength()
    {
        return this.length;
    }

    /**
     * Retrieve the list of OSMNodes that define the shape of this OSMLink.
     * @return List&lt;OSMNode&gt;; the list of OSMNodes that define the shape of this OSMLink
     */
    public final List<OSMNode> getSplineList()
    {
        return this.intermediateNodes;
    }

    /**
     * Append a Node to the list of OSMNodes of this OSMLink that define the shape of this OSMLink.
     * @param shapeNode OSMNode; the OSMNode to add to the list of OSMNodes that define the shape of this OSMLink
     */
    public final void addSpline(final OSMNode shapeNode)
    {
        this.intermediateNodes.add(shapeNode);
    }

    /**
     * Returns true if the link has an OSMTag with the specified key.
     * @param key String; the key of the sought OSMTag
     * @return boolean; true if this OSMLink has (one or more) OSMTag(s) with the specified key
     */
    public final boolean hasTag(final String key)
    {
        for (OSMTag t : this.tags)
        {
            if (t.getKey().equals(key))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Determine if a string represents a number that can stored in a byte.
     * @param s String; the string
     * @return is the given String a byte.
     */
    private boolean isByte(final String s)
    {
        try
        {
            Byte.parseByte(s);
        }
        catch (NumberFormatException e)
        {
            return false;
        }
        return true;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return String.format("Link %s from %d to %d", getId(), getStart().getId(), getEnd().getId());
    }

    /**
     * Report if this OSMLink has all tags in a supplied set.
     * @param tagsToCheck List&lt;OSMTag&gt;; the supplied set of tags
     * @return boolean; true if this Link has all the supplied tags; false otherwise
     */
    public final boolean containsAllTags(final List<OSMTag> tagsToCheck)
    {
        return this.tags.containsAll(tagsToCheck);
    }

}
