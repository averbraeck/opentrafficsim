package org.opentrafficsim.road.gtu.lane.perception;

import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;

import org.djunits.value.vdouble.scalar.Length;
import org.djunits.value.vdouble.scalar.Time;
import org.opentrafficsim.core.gtu.GTUException;
import org.opentrafficsim.core.network.LateralDirectionality;

import nl.tudelft.simulation.language.Throw;

/**
 * This data structure can clearly indicate the lane structure ahead of us, e.g. in the following situation:
 * 
 * <pre>
 *     (---- a ----)(---- b ----)(---- c ----)(---- d ----)(---- e ----)(---- f ----)(---- g ----)  
 *                                             __________                             __________
 *                                            / _________ 1                          / _________ 2
 *                                           / /                                    / /
 *                                __________/ /             _______________________/ /
 *  1  ____________ ____________ /_ _ _ _ _ _/____________ /_ _ _ _ _ _ _ _ _ _ _ _ /      
 *  0 |_ _X_ _ _ _ |_ _ _ _ _ _ |_ _ _ _ _ _ |_ _ _ _ _ _ |_ _ _ _ _ _ |_ _ _ _ _ _ \____________
 * -1 |____________|_ _ _ _ _ _ |____________|____________|  __________|____________|____________| 3
 * -2              / __________/                           \ \  
 *        ________/ /                                       \ \___________  
 *      5 _________/                                         \____________  4
 * </pre>
 * 
 * When the GTU is looking ahead, it needs to know that when it continues to destination 3, it needs to shift one lane to the
 * right at some point, but <b>not</b> two lanes to the right in link b, and not later than at the end of link f. When it needs
 * to go to destination 1, it needs to shift to the left in link c. When it has to go to destination 2, it has to shift to the
 * left, but not earlier than at link e. At node [de], it is possible to leave the rightmost lane of link e, and go to
 * destination 4. The rightmost lane just splits into two lanes at the end of link d, and the GTU can either continue driving to
 * destination 3, turn right to destination 4. This means that the right lane of link d has <b>two</b> successor lanes.
 * <p>
 * In the data structures, lanes are numbered laterally. Suppose that the lane where vehicle X resides would be number 0.
 * Consistent with "left is positive" for angles, the lane right of X would have number -1, and entry 5 would have number -2.
 * <p>
 * In the data structure, this can be indicated as follows (N = next, P = previous, L = left, R = right, D = lane drop, . =
 * continued but not in this structure). The merge lane in b is considered "off limits" for the GTUs on the "main" lane -1; the
 * "main" lane 0 is considered off limits from the exit lanes on c, e, and f. Still, we need to maintain pointers to these
 * lanes, as we are interested in the GTUs potentially driving next to us, feeding into our lane, etc.
 * 
 * <pre>
 *       1                0               -1               -2
 *       
 *                       ROOT 
 *                   _____|_____      ___________      ___________            
 *                  |_-_|_._|_R_|----|_L_|_._|_-_|    |_-_|_._|_-_|  a           
 *                        |                |                |
 *                   _____V_____      _____V_____      _____V_____            
 *                  |_-_|_N_|_R_|----|_L_|_N_|_R_|&lt;---|_L_|_D_|_-_|  b           
 *                        |                |                 
 *  ___________      _____V_____      _____V_____                 
 * |_-_|_N_|_R_|&lt;---|_L_|_N_|_R_|----|_L_|_N_|_-_|                   c
 *       |                |                |                 
 *  _____V_____      _____V_____      _____V_____                 
 * |_-_|_._|_-_|    |_-_|_N_|_R_|----|_L_|_NN|_-_|                   d          
 *                        |                ||_______________ 
 *  ___________      _____V_____      _____V_____      _____V_____            
 * |_-_|_N_|_R_|&lt;---|_L_|_N_|_R_|----|_L_|_N_|_-_|    |_-_|_N_|_-_|  e          
 *       |                |                |                |
 *  _____V_____      _____V_____      _____V_____      _____V_____            
 * |_-_|_N_|_R_|&lt;---|_L_|_D_|_R_|----|_L_|_N_|_-_|    |_-_|_._|_-_|  f          
 *       |                                 |                 
 *  _____V_____                       _____V_____                             
 * |_-_|_._|_-_|                     |_-_|_._|_-_|                   g
 * 
 * 
 * </pre>
 * <p>
 * Copyright (c) 2013-2016 Delft University of Technology, PO Box 5, 2600 AA, Delft, the Netherlands. All rights reserved. <br>
 * BSD-style license. See <a href="http://opentrafficsim.org/docs/license.html">OpenTrafficSim License</a>.
 * </p>
 * $LastChangedDate: 2015-07-24 02:58:59 +0200 (Fri, 24 Jul 2015) $, @version $Revision: 1147 $, by $Author: averbraeck $,
 * initial version Feb 20, 2016 <br>
 * @author <a href="http://www.tbm.tudelft.nl/averbraeck">Alexander Verbraeck</a>
 */
public class LaneStructure implements Serializable
{
    /** */
    private static final long serialVersionUID = 20160400L;

    /** The length of this structure, to see if it needs updating. */
    private Length length;

    /** The lanes from which we observe the situation. */
    private LaneStructureRecord rootLSR;

    /** Lane structure records of the cross section. */
    private TreeMap<RelativeLane, LaneStructureRecord> crossSectionRecords;

    /** Time of last cross section update. */
    private Time crossSectionUpdateTime;

    /**
     * @param initialRootLSR the initial rot record.
     */
    public LaneStructure(final LaneStructureRecord initialRootLSR)
    {
        setRootLSR(initialRootLSR);
    }

    /**
     * @return rootLSR
     */
    public final LaneStructureRecord getRootLSR()
    {
        return this.rootLSR;
    }

    /**
     * @param rootLSR set rootLSR
     */
    public final void setRootLSR(final LaneStructureRecord rootLSR)
    {
        this.rootLSR = rootLSR;
    }

    /**
     * @return length
     */
    public final Length getLength()
    {
        return this.length;
    }

    /**
     * Returns the cross section.
     * @param now current time to check if the cross section needs to be updated
     * @return cross section
     */
    public final SortedSet<RelativeLane> getCrossSection(final Time now)
    {
        updateCrossSection(now);
        return this.crossSectionRecords.navigableKeySet();
    }
    
    /**
     * @param now current time to check if the cross section needs to be updated
     */
    private void updateCrossSection(final Time now)
    {
        if (this.crossSectionRecords == null || now.gt(this.crossSectionUpdateTime))
        {
            this.crossSectionRecords = new TreeMap<>();
            // current lane
            this.crossSectionRecords.put(RelativeLane.CURRENT, getRootLSR());
            // left
            LaneStructureRecord lane = getRootLSR();
            int left = 1;
            while (lane.getLeft() != null)
            {
                RelativeLane relLane = new RelativeLane(LateralDirectionality.LEFT, left);
                this.crossSectionRecords.put(relLane, lane.getLeft());
                left++;
                lane = lane.getLeft();
            }
            addFirstMergeToCrossSection(lane, LateralDirectionality.LEFT, left);
            // right
            lane = getRootLSR();
            int right = 1;
            while (lane.getRight() != null)
            {
                RelativeLane relLane = new RelativeLane(LateralDirectionality.RIGHT, right);
                this.crossSectionRecords.put(relLane, lane.getRight());
                right++;
                lane = lane.getRight();
            }
            addFirstMergeToCrossSection(lane, LateralDirectionality.RIGHT, right);
        }
    }
    
    /**
     * Adds a single lane of the other link to the current cross section at a merge.
     * @param farMost record on far-most left or right side of current link
     * @param dir direction to search in, left or right
     * @param n number of lanes in left or right direction that the next lane will be
     */
    private void
        addFirstMergeToCrossSection(final LaneStructureRecord farMost, final LateralDirectionality dir, final int n)
    {
        Length cumulLengthDown = farMost.getLane().getLength();
        LaneStructureRecord next = getNextOnSide(farMost, dir);
        LaneStructureRecord mergeRecord = null; // first downstream record past merge
        while (next != null)
        {
            if (next.isLinkMerge())
            {
                mergeRecord = next;
                next = null;
            }
            else
            {
                cumulLengthDown = cumulLengthDown.plus(next.getLane().getLength());
                next = getNextOnSide(next, dir);
            }
        }
        if (mergeRecord != null)
        {
            LaneStructureRecord adjacentRecord =
                dir.equals(LateralDirectionality.LEFT) ? mergeRecord.getLeft() : mergeRecord.getRight();
            if (adjacentRecord == null)
            {
                // merge is on other side, add nothing
                return;
            }
            adjacentRecord = getPrevOnSide(adjacentRecord, dir);
            Length cumulLengthUp = Length.ZERO;
            while (adjacentRecord != null)
            {
                cumulLengthUp = cumulLengthUp.plus(adjacentRecord.getLane().getLength());
                if (cumulLengthUp.ge(cumulLengthDown))
                {
                    RelativeLane relLane = new RelativeLane(dir, n);
                    this.crossSectionRecords.put(relLane, adjacentRecord);
                    return;
                }
                adjacentRecord = getPrevOnSide(adjacentRecord, dir);
            }
        }
    }
    
    /**
     * Returns the correct next record for searching the next merge.
     * @param lane current lane record
     * @param dir direction of search
     * @return correct next record for searching the next merge
     */
    private LaneStructureRecord getNextOnSide(final LaneStructureRecord lane, final LateralDirectionality dir)
    {
        if (lane.getNext().size() == 1)
        {
            return lane.getNext().get(0);
        }
        for (LaneStructureRecord next : lane.getNext())
        {
            if ((dir.equals(LateralDirectionality.LEFT) && next.getLeft() == null)
                || (dir.equals(LateralDirectionality.RIGHT) && next.getRight() == null))
            {
                return next;
            }
        }
        return null;
    }
    
    /**
     * Returns the correct previous record for searching upstream from the next merge.
     * @param lane current lane record
     * @param dir direction of search
     * @return correct previous record for searching upstream from the next merge
     */
    private LaneStructureRecord getPrevOnSide(final LaneStructureRecord lane, final LateralDirectionality dir)
    {
        if (lane.getPrev().size() == 1)
        {
            return lane.getPrev().get(0);
        }
        for (LaneStructureRecord prev : lane.getPrev())
        {
            // note: looking left from current link, requires looking right from left adjacent link at merge
            if ((dir.equals(LateralDirectionality.LEFT) && prev.getRight() == null)
                || (dir.equals(LateralDirectionality.RIGHT) && prev.getLeft() == null))
            {
                return prev;
            }
        }
        return null;
    }
    
    /**
     * @param lane lane to check
     * @param now current time to check if the cross section needs to be updated
     * @return record at given lane
     * @throws GTUException if the lane is not in the cross section
     */
    public final LaneStructureRecord getLaneLSR(final RelativeLane lane, final Time now)  throws GTUException
    {
        updateCrossSection(now);
        Throw.when(!this.crossSectionRecords.containsKey(lane), GTUException.class,
            "The requeasted lane %s is not in the most recent cross section.", lane);
        return this.crossSectionRecords.get(lane);
    }
    
    /**
     * Removes all mappings to relative lanes that are not in the most recent cross section.
     * @param map map to clear mappings from
     * @param now current time to check if the cross section needs to be updated
     */
    public final void removeInvalidMappings(final Map<RelativeLane, ?> map, final Time now)
    {
        updateCrossSection(now);
        Iterator<RelativeLane> iterator = map.keySet().iterator();
        while (iterator.hasNext())
        {
            RelativeLane lane = iterator.next();
            if (!this.crossSectionRecords.containsKey(lane))
            {
                iterator.remove();
            }
        }
    }

    /** {@inheritDoc} */
    @Override
    public final String toString()
    {
        return "LaneStructure [length=" + this.length + ", rootLSR=" + this.rootLSR + "]";
    }

}
