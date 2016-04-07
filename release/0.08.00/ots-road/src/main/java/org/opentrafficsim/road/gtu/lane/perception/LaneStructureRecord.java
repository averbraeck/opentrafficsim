package org.opentrafficsim.road.gtu.lane.perception;

import java.util.List;

import org.opentrafficsim.core.gtu.GTUDirectionality;
import org.opentrafficsim.core.network.Node;
import org.opentrafficsim.road.network.lane.Lane;

/**
 * A LaneStructureRecord contains information about the lanes that can be accessed from this lane by a GTUType. It tells whether
 * there is a left and/or right lane by pointing to other LaneStructureRecords, and which successor LaneStructureRecord(s) there
 * are at the end of the lane of this LaneStructureRecord. All information (left, right, next) is calculated relative to the
 * driving direction of the GTU that owns this structure.
 * <p>
 * Copyright (c) 2013-2015 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Feb 21, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 * @author <a href="http://www.tudelft.nl/pknoppers">Peter Knoppers</a>
 */
public class LaneStructureRecord
{
    /** The lane of the LSR. */
    private final Lane lane;

    /** The direction in which we process this lane. */
    private final GTUDirectionality direction;

    /** The left LSR or null if not available. Left and right are relative to the <b>driving</b> direction. */
    private LaneStructureRecord left;

    /** The right LSR or null if not available. Left and right are relative to the <b>driving</b> direction. */
    private LaneStructureRecord right;

    /**
     * The next LSRs. The list is empty if no LSRs are available. Next is relative to the driving direction, not to the design
     * line direction.
     */
    private List<LaneStructureRecord> nextList;

    /**
     * @param lane the lane of the LSR
     * @param direction the direction on which we process this lane
     */
    public LaneStructureRecord(final Lane lane, final GTUDirectionality direction)
    {
        super();
        this.lane = lane;
        this.direction = direction;
    }

    /**
     * @return the 'from' node of the link belonging to this lane, in the driving direction.
     */
    public final Node getFromNode()
    {
        return this.direction.isPlus() ? this.lane.getParentLink().getStartNode() : this.lane.getParentLink()
            .getEndNode();
    }

    /**
     * @return the 'to' node of the link belonging to this lane, in the driving direction.
     */
    public final Node getToNode()
    {
        return this.direction.isPlus() ? this.lane.getParentLink().getEndNode() : this.lane.getParentLink()
            .getStartNode();
    }

    /**
     * @return whether the link to which this lane belongs splits, i.e. some of the parallel, connected lanes lead to a
     *         different destination than others
     */
    public boolean isLinkSplit()
    {
        Node toNode = getToNode();
        boolean hasLeft = this.left != null;
        LaneStructureRecord lsr = this;
        while (hasLeft)
        {
            lsr = lsr.getLeft();
            if (!lsr.getToNode().equals(toNode))
            {
                return false;
            }
            hasLeft = lsr.getLeft() != null;
        }
        boolean hasRight = this.right != null;
        lsr = this;
        while (hasRight)
        {
            lsr = lsr.getRight();
            if (!lsr.getToNode().equals(toNode))
            {
                return false;
            }
            hasRight = lsr.getRight() != null;
        }
        return true;
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
     */
    public final void setNext(final List<LaneStructureRecord> nextList)
    {
        this.nextList = nextList;
    }

    /**
     * @param next a next LSRs to add. Next is relative to the driving direction, not to the design line direction.
     */
    public final void addNext(final LaneStructureRecord next)
    {
        this.nextList.add(next);
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

}
