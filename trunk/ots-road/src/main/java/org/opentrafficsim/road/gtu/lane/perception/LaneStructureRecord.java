package org.opentrafficsim.road.gtu.lane.perception;

import java.io.Serializable;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.opentrafficsim.core.Throw;
import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * A LaneStructureRecord contains information about the lanes that can be accessed from this lane by a GTUType. It tells whether
 * there is a left and/or right lane by pointing to other LaneStructureRecords, and which successor LaneStructureRecord(s) there
 * are at the end of the lane of this LaneStructureRecord. All information (left, right, next) is calculated relative to the
 * driving direction of the GTU that owns this structure.
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Feb 21, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneStructureRecord implements Serializable
{
    /** */
    private static final long serialVersionUID = 20160400L;

    /** The lane of the LSR. */
    private final Lane lane;

    /** The direction in which we process this lane. */
    private final GTUDirectionality direction;

    /** The left LSR or null if not available. Left and right are relative to the <b>driving</b> direction. */
    private LaneStructureRecord left;

    /** The right LSR or null if not available. Left and right are relative to the <b>driving</b> direction. */
    private LaneStructureRecord right;

    /** Whether this lane has no next records as the lane structure was cut-off. */
    private boolean cutOffEnd = false;

    /** Whether this lane has no previous records as the lane structure was cut-off. */
    private boolean cutOffStart = false;

    /**
     * The next LSRs. The list is empty if no LSRs are available. Next is relative to the driving direction, not to the design
     * line direction.
     */
    private List<LaneStructureRecord> nextList;

    /**
     * The previous LSRs. The list is empty if no LSRs are available. Previous is relative to the driving direction, not to the
     * design line direction.
     */
    private List<LaneStructureRecord> prevList;

    /**
     * @param lane the lane of the LSR
     * @param direction the direction on which we process this lane
     */
    public LaneStructureRecord(final Lane lane, final GTUDirectionality direction)
    {
        this.lane = lane;
        this.direction = direction;
    }

    /**
     * @return the 'from' node of the link belonging to this lane, in the driving direction.
     */
    public final Node getFromNode()
    {
        return this.direction.isPlus() ? this.lane.getParentLink().getStartNode() : this.lane.getParentLink().getEndNode();
    }

    /**
     * @return the 'to' node of the link belonging to this lane, in the driving direction.
     */
    public final Node getToNode()
    {
        return this.direction.isPlus() ? this.lane.getParentLink().getEndNode() : this.lane.getParentLink().getStartNode();
    }

    /**
     * @return whether the link to which this lane belongs splits, i.e. some of the parallel, connected lanes lead to a
     *         different destination than others
     */
    public final boolean isLinkSplit()
    {
        Set<Node> toNodes = new HashSet<>();
        LaneStructureRecord lsr = this;
        while (lsr != null)
        {
            for (LaneStructureRecord next : lsr.getNext())
            {
                toNodes.add(next.getToNode());
            }
            lsr = lsr.getLeft();
        }
        lsr = this.getRight();
        while (lsr != null)
        {
            for (LaneStructureRecord next : lsr.getNext())
            {
                toNodes.add(next.getToNode());
            }
            lsr = lsr.getRight();
        }
        return toNodes.size() > 1;
    }

    /**
     * @return whether the link to which this lane belongs merges, i.e. some of the parallel, connected lanes follow from a
     *         different origin than others
     */
    public final boolean isLinkMerge()
    {
        Set<Node> fromNodes = new HashSet<>();
        LaneStructureRecord lsr = this;
        while (lsr != null)
        {
            for (LaneStructureRecord prev : lsr.getPrev())
            {
                fromNodes.add(prev.getFromNode());
            }
            lsr = lsr.getLeft();
        }
        lsr = this.getRight();
        while (lsr != null)
        {
            for (LaneStructureRecord prev : lsr.getPrev())
            {
                fromNodes.add(prev.getFromNode());
            }
            lsr = lsr.getRight();
        }
        return fromNodes.size() > 1;
    }

    /**
     * @return the left LSR or null if not available. Left and right are relative to the <b>driving</b> direction.
     */
    public final LaneStructureRecord getLeft()
    {
        return this.left;
    }

    /**
     * @param left set the left LSR or null if not available. Left and right are relative to the <b>driving</b> direction.
     */
    public final void setLeft(final LaneStructureRecord left)
    {
        this.left = left;
    }

    /**
     * @return the right LSR or null if not available. Left and right are relative to the <b>driving</b> direction
     */
    public final LaneStructureRecord getRight()
    {
        return this.right;
    }

    /**
     * @param right set the right LSR or null if not available. Left and right are relative to the <b>driving</b> direction
     */
    public final void setRight(final LaneStructureRecord right)
    {
        this.right = right;
    }

    /**
     * @return the next LSRs. The list is empty if no LSRs are available. Next is relative to the driving direction, not to the
     *         design line direction.
     */
    public final List<LaneStructureRecord> getNext()
    {
        return this.nextList;
    }

    /**
     * @param nextList set the next LSRs. The list is empty if no LSRs are available. Next is relative to the driving direction,
     *            not to the design line direction.
     * @throws GTUException if the records is cut-off at the end
     */
    public final void setNextList(final List<LaneStructureRecord> nextList)  throws GTUException
    {
        Throw.when(this.cutOffEnd && !nextList.isEmpty(), GTUException.class,
                "Cannot set next records to a record that was cut-off at the end.");
        this.nextList = nextList;
    }

    /**
     * @param next a next LSRs to add. Next is relative to the driving direction, not to the design line direction.
     * @throws GTUException if the records is cut-off at the end
     */
    public final void addNext(final LaneStructureRecord next) throws GTUException
    {
        Throw.when(this.cutOffEnd, GTUException.class,
                "Cannot add next records to a record that was cut-off at the end.");
        this.nextList.add(next);
    }

    /**
     * @return the previous LSRs. The list is empty if no LSRs are available. Previous is relative to the driving direction, not
     *         to the design line direction.
     */
    public final List<LaneStructureRecord> getPrev()
    {
        return this.prevList;
    }

    /**
     * @param prevList set the next LSRs. The list is empty if no LSRs are available. Previous is relative to the driving
     *            direction, not to the design line direction.
     * @throws GTUException if the records is cut-off at the start
     */
    public final void setPrevList(final List<LaneStructureRecord> prevList) throws GTUException
    {
        Throw.when(this.cutOffStart && !prevList.isEmpty(), GTUException.class,
                "Cannot set previous records to a record that was cut-off at the start.");
        this.prevList = prevList;
    }

    /**
     * @param prev a previous LSRs to add. Previous is relative to the driving direction, not to the design line direction.
     * @throws GTUException if the records is cut-off at the start
     */
    public final void addPrev(final LaneStructureRecord prev) throws GTUException
    {
        Throw.when(this.cutOffStart, GTUException.class,
            "Cannot add previous records to a record that was cut-off at the start.");
        this.prevList.add(prev);
    }

    /**
     * Sets this record as being cut-off, i.e. there are no next records due to cut-off.
     * @throws GTUException if there are next records
     */
    public final void setCutOffEnd() throws GTUException
    {
        Throw.when(!this.nextList.isEmpty(), GTUException.class,
            "Setting lane record as cut-off end, but there are next records.");
        this.cutOffEnd = true;
    }

    /**
     * Sets this record as being cut-off, i.e. there are no previous records due to cut-off.
     * @throws GTUException if there are previous records
     */
    public final void setCutOffStart() throws GTUException
    {
        Throw.when(!this.prevList.isEmpty(), GTUException.class,
            "Setting lane record as cut-off start, but there are previous records.");
        this.cutOffStart = true;
    }
    
    /**
     * Returns whether this lane has no next records as the lane structure was cut-off.
     * @return whether this lane has no next records as the lane structure was cut-off
     */
    public final boolean isCutOffEnd()
    {
        return this.cutOffEnd;
    }
    
    /**
     * Returns whether this lane has no previous records as the lane structure was cut-off.
     * @return whether this lane has no previous records as the lane structure was cut-off
     */
    public final boolean isCutOffStart()
    {
        return this.cutOffStart;
    }

    /**
     * @return the lane of the LSR
     */
    public final Lane getLane()
    {
        return this.lane;
    }

    /**
     * @return the direction in which we process this lane
     */
    public final GTUDirectionality getDirection()
    {
        return this.direction;
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneStructureRecord [lane=" + this.lane + ", direction=" + this.direction + ", left=" + this.left
            + ", right=" + this.right + ", nextList=" + this.nextList + "]";
    }

}
